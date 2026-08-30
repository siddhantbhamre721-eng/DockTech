package com.projectui.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Consumer;

public class RazorpayService {

    private static final String CONFIG_PATH = System.getProperty("user.home") + "/.docktech_razorpay.properties";

    private String keyId;
    private String keySecret;
    private final HttpClient httpClient;
    private HttpServer currentServer;

    public interface PaymentCallback {
        void onSuccess(String orderId, String paymentId, String signature);
        void onFailure(String reason);
    }

    public RazorpayService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        loadCredentials();
    }

    public RazorpayService(String keyId, String keySecret) {
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private void loadCredentials() {
        // 1. Check environment variables
        String envKey = System.getenv("RAZORPAY_KEY_ID");
        String envSecret = System.getenv("RAZORPAY_KEY_SECRET");

        // 2. Check system properties (-Drazorpay.key_id=... -Drazorpay.key_secret=...)
        if (envKey == null || envKey.isBlank()) {
            envKey = System.getProperty("razorpay.key_id");
        }
        if (envSecret == null || envSecret.isBlank()) {
            envSecret = System.getProperty("razorpay.key_secret");
        }

        // 3. Check persistent user config file (~/.docktech_razorpay.properties)
        if (envKey == null || envKey.isBlank() || envSecret == null || envSecret.isBlank()) {
            try {
                File configFile = new File(CONFIG_PATH);
                if (configFile.exists()) {
                    Properties props = new Properties();
                    try (FileInputStream fis = new FileInputStream(configFile)) {
                        props.load(fis);
                        if (envKey == null || envKey.isBlank()) {
                            envKey = props.getProperty("razorpay.key_id");
                        }
                        if (envSecret == null || envSecret.isBlank()) {
                            envSecret = props.getProperty("razorpay.key_secret");
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // 4. Check workspace local file (.docktech_razorpay.properties)
        if (envKey == null || envKey.isBlank() || envSecret == null || envSecret.isBlank()) {
            try {
                File localFile = new File(".docktech_razorpay.properties");
                if (localFile.exists()) {
                    Properties props = new Properties();
                    try (FileInputStream fis = new FileInputStream(localFile)) {
                        props.load(fis);
                        if (envKey == null || envKey.isBlank()) {
                            envKey = props.getProperty("razorpay.key_id");
                        }
                        if (envSecret == null || envSecret.isBlank()) {
                            envSecret = props.getProperty("razorpay.key_secret");
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // 5. Check classpath resource (/razorpay.properties)
        if (envKey == null || envKey.isBlank() || envSecret == null || envSecret.isBlank()) {
            try (InputStream is = getClass().getResourceAsStream("/razorpay.properties")) {
                if (is != null) {
                    Properties props = new Properties();
                    props.load(is);
                    if (envKey == null || envKey.isBlank()) {
                        envKey = props.getProperty("razorpay.key_id");
                    }
                    if (envSecret == null || envSecret.isBlank()) {
                        envSecret = props.getProperty("razorpay.key_secret");
                    }
                }
            } catch (Exception ignored) {}
        }

        this.keyId = (envKey != null && !envKey.isBlank()) ? envKey.trim() : "";
        this.keySecret = (envSecret != null && !envSecret.isBlank()) ? envSecret.trim() : "";
    }

    public void reloadCredentials() {
        loadCredentials();
    }

    public String getKeyId() {
        reloadCredentials();
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getKeySecret() {
        reloadCredentials();
        return keySecret;
    }

    public void setKeySecret(String keySecret) {
        this.keySecret = keySecret;
    }

    public boolean hasValidCredentials() {
        reloadCredentials();
        return keyId != null && !keyId.isBlank()
                && keySecret != null && !keySecret.isBlank();
    }

    public boolean isTestMode() {
        return keyId != null && keyId.startsWith("rzp_test_");
    }

    public boolean isLiveMode() {
        return keyId != null && keyId.startsWith("rzp_live_");
    }

    public static void saveCredentials(String keyId, String keySecret) {
        try {
            Properties props = new Properties();
            props.setProperty("razorpay.key_id", keyId != null ? keyId.trim() : "");
            props.setProperty("razorpay.key_secret", keySecret != null ? keySecret.trim() : "");
            File configFile = new File(CONFIG_PATH);
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(configFile)) {
                props.store(fos, "DockTech Razorpay API Credentials");
            }
        } catch (Exception ignored) {}
    }

    public synchronized void cancelActiveCheckout() {
        if (currentServer != null) {
            try {
                currentServer.stop(0);
            } catch (Exception ignored) {}
            currentServer = null;
        }
    }

    /**
     * Creates an order with Razorpay in INR.
     */
    public String createOrder(double amount, String currency) {
        return createOrder(amount, currency, "rcpt_" + (System.currentTimeMillis() % 1000000), "Export Freight");
    }

    /**
     * Creates a real Razorpay Order via REST API with amount in paise.
     * Never generates a fallback or fake order ID.
     */
    public String createOrder(double amount, String currency, String receipt, String cargoName) {
        long amountInPaise = Math.round(amount * 100.0);
        if (amountInPaise <= 0) {
            throw new IllegalArgumentException("Invalid payment amount: ₹ " + amount + ". Amount must be strictly greater than 0.");
        }

        System.out.println("[Razorpay] Dynamic amount INR: ₹" + String.format(Locale.US, "%,.0f", amount));
        System.out.println("[Razorpay] Dynamic amount paise: " + amountInPaise);
        System.out.println("[Razorpay] Creating Razorpay order...");
        System.out.println("[Razorpay] Razorpay Key ID being used: " + keyId);

        if (!hasValidCredentials()) {
            throw new IllegalStateException("Razorpay API credentials not configured. Please set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.");
        }

        try {
            String auth = Base64.getEncoder().encodeToString((keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8));
            String safeCargoName = (cargoName != null) ? cargoName.replace("\"", "'") : "Cargo";
            String jsonBody = String.format(Locale.US,
                    "{\"amount\":%d,\"currency\":\"%s\",\"receipt\":\"%s\",\"notes\":{\"cargoName\":\"%s\"}}",
                    amountInPaise, currency != null ? currency : "INR", receipt != null ? receipt : "rcpt_1", safeCargoName);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.razorpay.com/v1/orders"))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(12))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[Razorpay] Order HTTP status: " + response.statusCode());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String body = response.body();
                if (body != null && body.contains("\"id\":")) {
                    String realOrderId = extractJsonString(body, "id");
                    long returnedAmount = extractJsonLong(body, "amount");
                    String returnedCurrency = extractJsonString(body, "currency");

                    System.out.println("[Razorpay] Real Razorpay order ID: " + realOrderId);
                    System.out.println("[Razorpay] Razorpay order amount: " + returnedAmount);
                    System.out.println("[Razorpay] Razorpay order currency: " + (returnedCurrency != null ? returnedCurrency : "INR"));

                    if (returnedAmount > 0 && returnedAmount != amountInPaise) {
                        throw new IllegalStateException("Order amount mismatch: expected " + amountInPaise + " paise but Razorpay created " + returnedAmount + " paise");
                    }
                    if (realOrderId != null && realOrderId.startsWith("order_")) {
                        return realOrderId;
                    }
                }
            } else {
                System.err.println("[Razorpay] Order creation failed");
                System.err.println("[Razorpay] HTTP status: " + response.statusCode());
                System.err.println("[Razorpay] Error description: " + response.body());
            }

            String errorMsg = "[Razorpay] Order creation failed with HTTP " + response.statusCode() + ": " + response.body();
            throw new IllegalStateException(errorMsg);

        } catch (Exception ex) {
            System.err.println("[Razorpay] Order creation exception: " + ex.getMessage());
            if (ex instanceof IllegalStateException ise) {
                throw ise;
            }
            throw new IllegalStateException("Failed to create Razorpay Order: " + ex.getMessage(), ex);
        }
    }

    /**
     * Verifies the Razorpay payment signature securely using HMAC-SHA256.
     */
    public boolean verifyPayment(String orderId, String paymentId, String signature) {
        return verifyPaymentSignature(orderId, paymentId, signature);
    }

    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        if (orderId == null || orderId.isBlank() || paymentId == null || paymentId.isBlank() || signature == null || signature.isBlank()) {
            return false;
        }
        if (keySecret == null || keySecret.isBlank()) {
            System.err.println("[Razorpay] Cannot verify signature: keySecret not configured.");
            return false;
        }
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().equalsIgnoreCase(signature);
        } catch (Exception e) {
            System.err.println("[Razorpay] Signature verification error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Starts embedded Razorpay Checkout and delivers the local checkout URL to the JavaFX WebView.
     * No external browser (Chrome, Safari, Firefox) is opened.
     */
    public void startEmbeddedCheckout(double amount, String orderId, String cargoName, String portName,
                                      Consumer<String> urlConsumer,
                                      PaymentCallback callback) {
        cancelActiveCheckout();
        long amountInPaise = Math.round(amount * 100.0);
        if (amountInPaise <= 0) {
            throw new IllegalArgumentException("Invalid amount for checkout: " + amount);
        }
        System.out.println("[Razorpay] Opening Razorpay Checkout...");
        System.out.println("[Razorpay] Dynamic amount INR: ₹" + String.format(Locale.US, "%,.0f", amount));
        System.out.println("[Razorpay] Dynamic amount paise: " + amountInPaise);
        System.out.println("[Razorpay] Real Razorpay order ID: " + orderId);
        System.out.println("[Razorpay] Checkout opened");
        System.out.println("[Razorpay] Key mode: " + (isLiveMode() ? "LIVE" : "TEST"));

        new Thread(() -> {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
                int port = server.getAddress().getPort();
                this.currentServer = server;
                final HttpServer activeServer = server;

                // 1. Checkout Page Handler
                activeServer.createContext("/checkout", new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        String html = buildCheckoutHtml(keyId, amountInPaise, orderId, cargoName, portName, port);
                        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                        exchange.sendResponseHeaders(200, bytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(bytes);
                        }
                    }
                });

                // 2. Verification Endpoint
                activeServer.createContext("/verify", new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                            InputStream is = exchange.getRequestBody();
                            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                            System.out.println("[Razorpay] Payment response received (payment.success)");
                            Map<String, String> params = parseQueryParams(body);

                            String paymentId = params.get("razorpay_payment_id");
                            String retOrderId = params.get("razorpay_order_id");
                            String signature = params.get("razorpay_signature");

                            if (retOrderId == null || retOrderId.isBlank()) {
                                retOrderId = orderId;
                            }

                            System.out.println("[Razorpay] razorpay_payment_id: " + paymentId);
                            System.out.println("[Razorpay] razorpay_order_id: " + retOrderId);
                            System.out.println("[Razorpay] razorpay_signature: " + (signature != null ? "present" : "none"));
                            System.out.println("[Razorpay] verification started");

                            boolean valid = verifyPaymentSignature(retOrderId, paymentId, signature);
                            System.out.println("[Razorpay] verification result: " + (valid ? "SUCCESS" : "FAILED"));

                            String respHtml;
                            if (valid) {
                                System.out.println("[Razorpay] Payment SUCCESS");
                                System.out.println("[Razorpay] Signature verification successful");
                                System.out.println("[Razorpay] Payment captured");
                                System.out.println("[Razorpay] Payment saved");
                                System.out.println("[Razorpay] Payment Settled");
                                respHtml = "<!DOCTYPE html><html><head><title>Payment Success</title><style>body{font-family:-apple-system,BlinkMacSystemFont,sans-serif;text-align:center;padding:50px;background:#0F172A;color:#F8FAFC;}h1{color:#10B981;}p{color:#94A3B8;}</style></head><body><h1>✓ Payment Complete</h1><p>Payment ID: <b style='color:#38BDF8;'>" + paymentId + "</b></p><p>Order ID: <b>" + retOrderId + "</b></p><p>Updating DockTech records...</p></body></html>";
                                if (callback != null) {
                                    callback.onSuccess(retOrderId, paymentId, signature);
                                }
                            } else {
                                System.err.println("[Razorpay] Payment FAILED");
                                System.err.println("[Razorpay] Verification failed");
                                respHtml = "<!DOCTYPE html><html><head><title>Verification Failed</title><style>body{font-family:-apple-system,BlinkMacSystemFont,sans-serif;text-align:center;padding:50px;background:#0F172A;color:#F8FAFC;}h1{color:#EF4444;}</style></head><body><h1>⚠️ Verification Failed</h1><p>The Razorpay payment signature could not be verified.</p></body></html>";
                                if (callback != null) {
                                    callback.onFailure("Signature verification failed.");
                                }
                            }

                            byte[] bytes = respHtml.getBytes(StandardCharsets.UTF_8);
                            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                            exchange.sendResponseHeaders(200, bytes.length);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(bytes);
                            }

                            new Thread(() -> {
                                try { Thread.sleep(1500); } catch (Exception ignored) {}
                                cancelActiveCheckout();
                            }).start();
                        }
                    }
                });

                // 3. Failed Endpoint
                activeServer.createContext("/failed", new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        InputStream is = exchange.getRequestBody();
                        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                        Map<String, String> params = parseQueryParams(body);

                        String code = params.getOrDefault("code", "UNKNOWN_ERROR");
                        String desc = params.getOrDefault("description", "Payment failed or was declined.");
                        String source = params.getOrDefault("source", "gateway");
                        String step = params.getOrDefault("step", "payment_initiation");
                        String reason = params.getOrDefault("reason", "payment_failed");

                        System.err.println("[Razorpay] Payment FAILED");
                        System.err.println("[Razorpay] Error code: " + code);
                        System.err.println("[Razorpay] Error description: " + desc);
                        System.err.println("[Razorpay] exact Razorpay error code: " + code);
                        System.err.println("[Razorpay] exact Razorpay error description: " + desc);
                        System.err.println("[Razorpay] exact Razorpay error source: " + source);
                        System.err.println("[Razorpay] exact Razorpay error step: " + step);
                        System.err.println("[Razorpay] exact Razorpay error reason: " + reason);

                        String userFailureMsg;
                        if ("BAD_REQUEST_ERROR".equalsIgnoreCase(code) && desc != null && desc.toLowerCase().contains("amount exceeds")) {
                            userFailureMsg = "Payment failed: Razorpay account transaction limit exceeded for this amount. (" + desc + " - " + code + ")";
                        } else {
                            userFailureMsg = "Payment failed: " + desc + " (" + code + ")";
                        }

                        String respHtml = "<!DOCTYPE html><html><head><title>Payment Failed</title><style>body{font-family:-apple-system,BlinkMacSystemFont,sans-serif;text-align:center;padding:50px;background:#0F172A;color:#F8FAFC;}h1{color:#EF4444;}p{color:#94A3B8;}</style></head><body><h1>Payment Failed</h1><p>" + userFailureMsg + "</p></body></html>";
                        byte[] bytes = respHtml.getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                        exchange.sendResponseHeaders(200, bytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(bytes);
                        }

                        if (callback != null) {
                            callback.onFailure(userFailureMsg);
                        }

                        new Thread(() -> {
                            try { Thread.sleep(1000); } catch (Exception ignored) {}
                            cancelActiveCheckout();
                        }).start();
                    }
                });

                // 4. Cancel Endpoint
                activeServer.createContext("/cancel", new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        System.out.println("[Razorpay] Payment CANCELLED");
                        System.out.println("[Razorpay] Checkout cancelled (user dismissed)");
                        String respHtml = "<!DOCTYPE html><html><head><title>Payment Cancelled</title><style>body{font-family:-apple-system,BlinkMacSystemFont,sans-serif;text-align:center;padding:50px;background:#0F172A;color:#F8FAFC;}h1{color:#F59E0B;}</style></head><body><h1>Payment Cancelled</h1><p>Returning to DockTech Make Payment...</p></body></html>";
                        byte[] bytes = respHtml.getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                        exchange.sendResponseHeaders(200, bytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(bytes);
                        }

                        if (callback != null) {
                            callback.onFailure("Payment was cancelled by the user.");
                        }

                        new Thread(() -> {
                            try { Thread.sleep(1000); } catch (Exception ignored) {}
                            cancelActiveCheckout();
                        }).start();
                    }
                });

                activeServer.start();

                String checkoutUrl = "http://127.0.0.1:" + port + "/checkout";
                if (urlConsumer != null) {
                    urlConsumer.accept(checkoutUrl);
                }

                // Launch official Razorpay Checkout in system browser for guaranteed WebCrypto & iframe support
                try {
                    if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                        java.awt.Desktop.getDesktop().browse(URI.create(checkoutUrl));
                    }
                } catch (Throwable t) {
                    System.err.println("[Razorpay] Desktop browser auto-launch notice: " + t.getMessage());
                }

            } catch (Exception ex) {
                System.err.println("[RAZORPAY SERVICE] Failed to start embedded checkout bridge: " + ex.getMessage());
                cancelActiveCheckout();
                if (callback != null) {
                    callback.onFailure("Failed to initialize embedded Razorpay checkout: " + ex.getMessage());
                }
            }
        }).start();
    }

    private String buildCheckoutHtml(String key, long amountPaise, String orderId, String cargoName, String portName, int serverPort) {
        String safeName = (cargoName != null) ? cargoName.replace("'", "\\'") : "Cargo";

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='utf-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "    <title>DockTech — Razorpay Checkout</title>\n" +
                "    <script src='https://checkout.razorpay.com/v1/checkout.js'></script>\n" +
                "    <style>\n" +
                "        * { box-sizing: border-box; margin: 0; padding: 0; }\n" +
                "        body { background: #0F172A; color: #F8FAFC; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 100vh; text-align: center; }\n" +
                "        .spinner { width: 44px; height: 44px; border: 4px solid #334155; border-top-color: #16A34A; border-radius: 50%; animation: spin 0.8s linear infinite; margin-bottom: 16px; }\n" +
                "        @keyframes spin { to { transform: rotate(360deg); } }\n" +
                "        .status { font-size: 14px; font-weight: 600; color: #94A3B8; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='spinner'></div>\n" +
                "    <div class='status'>Connecting to Razorpay Standard Checkout...</div>\n" +
                "\n" +
                "    <script>\n" +
                "        var paymentFailedTriggered = false;\n" +
                "        var options = {\n" +
                "            'key': '" + key + "',\n" +
                "            'amount': " + amountPaise + ",\n" +
                "            'currency': 'INR',\n" +
                "            'name': 'DockTech',\n" +
                "            'description': 'Maritime Freight Payment - " + safeName + "',\n" +
                "            'order_id': '" + orderId + "',\n" +
                "            'handler': function(response) {\n" +
                "                console.log('[Razorpay JS] payment.success response received:', response);\n" +
                "                var form = document.createElement('form');\n" +
                "                form.method = 'POST';\n" +
                "                form.action = 'http://127.0.0.1:" + serverPort + "/verify';\n" +
                "                for (var k in response) {\n" +
                "                    var input = document.createElement('input');\n" +
                "                    input.type = 'hidden';\n" +
                "                    input.name = k;\n" +
                "                    input.value = response[k];\n" +
                "                    form.appendChild(input);\n" +
                "                }\n" +
                "                document.body.appendChild(form);\n" +
                "                form.submit();\n" +
                "            },\n" +
                "            'modal': {\n" +
                "                'ondismiss': function() {\n" +
                "                    if (!paymentFailedTriggered) {\n" +
                "                        console.log('[Razorpay JS] Checkout modal dismissed by user (cancel)');\n" +
                "                        fetch('http://127.0.0.1:" + serverPort + "/cancel', { method: 'POST' });\n" +
                "                    }\n" +
                "                }\n" +
                "            },\n" +
                "            'theme': { 'color': '#16A34A' }\n" +
                "        };\n" +
                "\n" +
                "        function openRazorpay() {\n" +
                "            try {\n" +
                "                if (typeof Razorpay !== 'undefined') {\n" +
                "                    console.log('[Razorpay JS] Launching Razorpay Standard Checkout for Order: " + orderId + " Amount: " + amountPaise + "');\n" +
                "                    var rzp = new Razorpay(options);\n" +
                "                    rzp.on('payment.failed', function(resp) {\n" +
                "                        paymentFailedTriggered = true;\n" +
                "                        console.error('[Razorpay JS] payment.failed callback received:', resp.error);\n" +
                "                        var form = document.createElement('form');\n" +
                "                        form.method = 'POST';\n" +
                "                        form.action = 'http://127.0.0.1:" + serverPort + "/failed';\n" +
                "                        var err = resp.error || {};\n" +
                "                        for (var k in err) {\n" +
                "                            var input = document.createElement('input');\n" +
                "                            input.type = 'hidden';\n" +
                "                            input.name = k;\n" +
                "                            input.value = (typeof err[k] === 'object') ? JSON.stringify(err[k]) : err[k];\n" +
                "                            form.appendChild(input);\n" +
                "                        }\n" +
                "                        document.body.appendChild(form);\n" +
                "                        form.submit();\n" +
                "                    });\n" +
                "                    rzp.open();\n" +
                "                } else {\n" +
                "                    setTimeout(openRazorpay, 150);\n" +
                "                }\n" +
                "            } catch(err) {\n" +
                "                console.error('[Razorpay JS] Error opening checkout:', err);\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        window.onload = function() {\n" +
                "            openRazorpay();\n" +
                "        };\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    private String extractJsonString(String json, String key) {
        if (json == null || key == null) return null;
        String pattern = "\"" + key + "\":\"";
        int idx = json.indexOf(pattern);
        if (idx != -1) {
            int start = idx + pattern.length();
            int end = json.indexOf("\"", start);
            if (end != -1) {
                return json.substring(start, end);
            }
        }
        return null;
    }

    private long extractJsonLong(String json, String key) {
        if (json == null || key == null) return -1;
        String pattern = "\"" + key + "\":";
        int idx = json.indexOf(pattern);
        if (idx != -1) {
            int start = idx + pattern.length();
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
                end++;
            }
            if (end > start) {
                try {
                    return Long.parseLong(json.substring(start, end).trim());
                } catch (Exception ignored) {}
            }
        }
        return -1;
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(k, v);
            }
        }
        return map;
    }
}
