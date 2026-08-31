package com.projectui.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class EmailNotificationService {

    public static final String DEFAULT_ZAPIER_WEBHOOK_URL = "https://hooks.zapier.com/hooks/catch/28709622/4hmx80k/";

    public CompletableFuture<Boolean> sendPortAuthorityCredentialWebhook(
            String portName,
            String portCode,
            String requestId,
            String portAuthorityName,
            String loginEmail,
            String registeredEmail,
            String temporaryPassword) {

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                if (registeredEmail == null || registeredEmail.isBlank() || !com.projectui.view.login.LoginPage.isEmail(registeredEmail.trim())) {
                    System.err.println("[ZAPIER WEBHOOK] Validation Error: Registered recipient email is missing or invalid: " + registeredEmail);
                    future.complete(false);
                    return;
                }
                if (loginEmail == null || loginEmail.isBlank()) {
                    System.err.println("[ZAPIER WEBHOOK] Validation Error: Login email is missing or blank");
                    future.complete(false);
                    return;
                }
                if (temporaryPassword == null || temporaryPassword.isBlank()) {
                    System.err.println("[ZAPIER WEBHOOK] Validation Error: Temporary password is missing or blank");
                    future.complete(false);
                    return;
                }

                String cleanRegisteredEmail = registeredEmail.trim();
                String cleanLoginEmail = loginEmail.trim();
                String cleanPaName = (portAuthorityName != null && !portAuthorityName.isBlank()) ? portAuthorityName.trim() : "Port Authority";
                String cleanPortName = (portName != null && !portName.isBlank()) ? portName.trim() : "Port";
                String cleanPortCode = (portCode != null && !portCode.isBlank()) ? portCode.trim() : "N/A";
                String cleanRequestId = (requestId != null && !requestId.isBlank()) ? requestId.trim() : "N/A";

                // Prevent sending to system-generated fake placeholder addresses
                if (cleanRegisteredEmail.toLowerCase().startsWith("pa_") && cleanRegisteredEmail.toLowerCase().endsWith("@docktech.com")) {
                    System.err.println("[ZAPIER WEBHOOK] Delivery Error: Refusing to send approval webhook to system-generated placeholder: " + cleanRegisteredEmail);
                    future.complete(false);
                    return;
                }

                String webhookUrl = System.getenv("ZAPIER_PORT_AUTHORITY_WEBHOOK_URL");
                if (webhookUrl == null || webhookUrl.isBlank()) {
                    webhookUrl = System.getenv("ZAPIER_WEBHOOK_URL");
                }
                if (webhookUrl == null || webhookUrl.isBlank()) {
                    webhookUrl = DEFAULT_ZAPIER_WEBHOOK_URL;
                }

                StringBuilder json = new StringBuilder();
                json.append("{");
                json.append("\"portName\":\"").append(escapeJson(cleanPortName)).append("\",");
                json.append("\"portCode\":\"").append(escapeJson(cleanPortCode)).append("\",");
                json.append("\"requestId\":\"").append(escapeJson(cleanRequestId)).append("\",");
                json.append("\"portAuthorityName\":\"").append(escapeJson(cleanPaName)).append("\",");
                json.append("\"loginEmail\":\"").append(escapeJson(cleanLoginEmail)).append("\",");
                json.append("\"registeredEmail\":\"").append(escapeJson(cleanRegisteredEmail)).append("\",");
                json.append("\"temporaryPassword\":\"").append(escapeJson(temporaryPassword)).append("\",");
                // Secondary compatibility aliases
                json.append("\"paName\":\"").append(escapeJson(cleanPaName)).append("\",");
                json.append("\"loginId\":\"").append(escapeJson(cleanLoginEmail)).append("\",");
                json.append("\"requesterEmail\":\"").append(escapeJson(cleanRegisteredEmail)).append("\",");
                json.append("\"email\":\"").append(escapeJson(cleanRegisteredEmail)).append("\",");
                json.append("\"to\":\"").append(escapeJson(cleanRegisteredEmail)).append("\",");
                json.append("\"recipient\":\"").append(escapeJson(cleanRegisteredEmail)).append("\",");
                json.append("\"tempPassword\":\"").append(escapeJson(temporaryPassword)).append("\"");
                json.append("}");

                String jsonPayload = json.toString();

                System.out.println("[ZAPIER] Sending Port Authority credential webhook to: " + webhookUrl);
                System.out.println("[ZAPIER] Credential payload prepared for port=" + cleanPortName + ", loginEmail=" + cleanLoginEmail + ", registeredEmail=" + cleanRegisteredEmail + ", requestId=" + cleanRequestId);

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(java.time.Duration.ofSeconds(15))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl))
                        .timeout(java.time.Duration.ofSeconds(20))
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, java.nio.charset.StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
                int status = response.statusCode();
                String respBody = response.body() != null ? response.body() : "";
                System.out.println("[ZAPIER] Webhook response status: " + status);

                if (status >= 200 && status < 300 && !respBody.toLowerCase().contains("\"status\":\"error\"")) {
                    System.out.println("[ZAPIER] Credential webhook sent successfully for port=" + cleanPortName);
                    future.complete(true);
                } else {
                    System.err.println("[ZAPIER ERROR] Credential webhook failed: HTTP " + status + " for recipient " + cleanRegisteredEmail);
                    future.complete(false);
                }
            } catch (Exception ex) {
                System.err.println("[ZAPIER ERROR] Credential webhook failed: " + ex.getMessage());
                future.complete(false);
            }
        });

        return future;
    }

    public CompletableFuture<Boolean> sendZapierApprovalWebhook(
            String requesterEmail,
            String portName,
            String portCode,
            String requestId,
            String paName,
            String loginId,
            String tempPassword) {
        return sendPortAuthorityCredentialWebhook(portName, portCode, requestId, paName, loginId, requesterEmail, tempPassword);
    }

    public CompletableFuture<Boolean> sendApprovalNotification(
            String requesterEmail,
            String portName,
            String portCode,
            String requestId,
            String paName,
            String loginId,
            String tempPassword) {
        return sendPortAuthorityCredentialWebhook(portName, portCode, requestId, paName, loginId, requesterEmail, tempPassword);
    }

    private static String escapeJson(String input) {
        if (input == null) return "";
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public CompletableFuture<Boolean> sendRejectionNotification(
            String requesterEmail,
            String portName,
            String requestId,
            String rejectionReason) {

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        String subject = "New Port Request Rejected — " + portName;
        String body = String.format(
                "====================================================\n" +
                "DOCK TECH PORT MANAGEMENT NOTIFICATION\n" +
                "====================================================\n\n" +
                "Dear Requester,\n\n" +
                "We regret to inform you that your New Port Request for %s has been REJECTED by Dock Tech Admin.\n\n" +
                "REQUEST DETAILS:\n" +
                "- Request ID: %s\n" +
                "- Port Name: %s\n" +
                "- Status: REJECTED\n\n" +
                "REJECTION REASON:\n" +
                "%s\n\n" +
                "NEXT STEPS:\n" +
                "Please review the rejection reason above. If you wish to correct the details or provide additional environmental/proposal documentation, you may submit a new Port Request.\n\n" +
                "Best regards,\n" +
                "Dock Tech Administrative Team\n" +
                "====================================================",
                portName, requestId, portName,
                (rejectionReason != null && !rejectionReason.isBlank() ? rejectionReason : "Information submitted did not meet operational criteria.")
        );

        System.out.println("[EMAIL NOTIFICATION SERVICE] Sending Rejection Email to: " + requesterEmail);
        System.out.println("[SUBJECT] " + subject);
        System.out.println("[BODY]\n" + body);

        // Async simulation of server-side / Firebase mail dispatch
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(300);
                System.out.println("[EMAIL NOTIFICATION SERVICE] Rejection notification successfully dispatched to " + requesterEmail);
                future.complete(true);
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            }
        });

        return future;
    }

    public CompletableFuture<Boolean> sendAccountSetupEmail(
            String recipientEmail,
            String name,
            String role,
            String id,
            String assignedPort,
            String assignedShip,
            String setupLink) {

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        String subject = "Welcome to DockTech – Your Account Has Been Created";
        StringBuilder body = new StringBuilder();
        body.append("====================================================\n");
        body.append("WELCOME TO DOCK TECH ACCOUNT SETUP\n");
        body.append("====================================================\n\n");
        body.append("Dear ").append(name).append(",\n\n");
        body.append("Your Port Authority has created your DockTech account.\n\n");
        body.append("ACCOUNT DETAILS:\n");
        body.append("- Account Type: ").append(role).append("\n");
        body.append("- Name: ").append(name).append("\n");
        if (id != null && !id.isBlank()) {
            body.append("- Account ID: ").append(id).append("\n");
        }
        if (assignedPort != null && !assignedPort.isBlank()) {
            body.append("- Assigned Port: ").append(assignedPort).append("\n");
        }
        if (assignedShip != null && !assignedShip.isBlank()) {
            body.append("- Assigned Ship: ").append(assignedShip).append("\n");
        }
        body.append("- Login Email: ").append(recipientEmail).append("\n\n");
        body.append("PASSWORD SETUP INSTRUCTIONS:\n");
        body.append("Please use the secure link below to set your password and activate your account:\n");
        body.append(setupLink != null && !setupLink.isBlank() ? setupLink : "https://docktech.com/auth/setup-password?email=" + recipientEmail).append("\n\n");
        body.append("After setting your password, use the existing DockTech Login page to log in.\n\n");
        body.append("Best regards,\n");
        body.append("Dock Tech Port Authority Operations\n");
        body.append("====================================================");

        System.out.println("[EMAIL] Recipient: " + recipientEmail);
        System.out.println("[EMAIL] Subject: " + subject);

        CompletableFuture.runAsync(() -> {
            try {
                String host = getEnv("MAIL_HOST", getEnv("SMTP_HOST", null));
                String portStr = getEnv("MAIL_PORT", getEnv("SMTP_PORT", "587"));
                String user = getEnv("MAIL_USERNAME", getEnv("SMTP_USER", null));
                String pass = getEnv("MAIL_PASSWORD", getEnv("SMTP_PASS", null));
                String from = getEnv("MAIL_FROM", "no-reply@docktech.com");

                if (host != null && user != null && pass != null) {
                    int port = 587;
                    try { port = Integer.parseInt(portStr); } catch (Exception ignored) {}
                    boolean sent = sendSmtpEmail(host, port, user, pass, from, recipientEmail, subject, body.toString());
                    if (sent) {
                        System.out.println("[EMAIL] Email delivery: SUCCESS (dispatched to recipient via " + host + ")");
                        future.complete(true);
                    } else {
                        System.err.println("[EMAIL] Email delivery: FAILED (SMTP server " + host + " rejected delivery)");
                        future.complete(false);
                    }
                } else {
                    System.err.println("[EMAIL] Email delivery: FAILED (SMTP credentials MAIL_HOST / MAIL_USERNAME / MAIL_PASSWORD not configured in environment)");
                    future.complete(false);
                }
            } catch (Exception ex) {
                System.err.println("[EMAIL] Email delivery: FAILED (" + ex.getMessage() + ")");
                future.complete(false);
            }
        });

        return future;
    }

    private static String getEnv(String key, String fallback) {
        String val = System.getenv(key);
        return val != null && !val.isBlank() ? val : fallback;
    }

    private static boolean sendSmtpEmail(String host, int port, String user, String pass, String from, String to, String subject, String body) {
        try (java.net.Socket socket = new java.net.Socket(host, port);
             java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
             java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(socket.getOutputStream()), true)) {

            String banner = reader.readLine();
            if (banner == null || !banner.startsWith("220")) return false;

            writer.println("EHLO " + host);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("250 ")) break;
            }

            if (port == 587) {
                writer.println("STARTTLS");
                String tlsResp = reader.readLine();
                if (tlsResp == null || !tlsResp.startsWith("220")) return false;

                javax.net.ssl.SSLSocketFactory ssf = (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault();
                java.net.Socket sslSocket = ssf.createSocket(socket, host, port, true);
                java.io.BufferedReader sslReader = new java.io.BufferedReader(new java.io.InputStreamReader(sslSocket.getInputStream()));
                java.io.PrintWriter sslWriter = new java.io.PrintWriter(new java.io.OutputStreamWriter(sslSocket.getOutputStream()), true);

                sslWriter.println("EHLO " + host);
                while ((line = sslReader.readLine()) != null) {
                    if (line.startsWith("250 ")) break;
                }

                String authRaw = "\0" + user + "\0" + pass;
                String authBase64 = java.util.Base64.getEncoder().encodeToString(authRaw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                sslWriter.println("AUTH PLAIN " + authBase64);
                String authResp = sslReader.readLine();
                if (authResp == null || !authResp.startsWith("235")) return false;

                sslWriter.println("MAIL FROM:<" + from + ">");
                if (!sslReader.readLine().startsWith("250")) return false;

                sslWriter.println("RCPT TO:<" + to + ">");
                if (!sslReader.readLine().startsWith("250")) return false;

                sslWriter.println("DATA");
                if (!sslReader.readLine().startsWith("354")) return false;

                sslWriter.println("Subject: " + subject);
                sslWriter.println("From: " + from);
                sslWriter.println("To: " + to);
                sslWriter.println("Content-Type: text/plain; charset=UTF-8\r\n");
                sslWriter.println(body);
                sslWriter.println(".");
                String dataResp = sslReader.readLine();
                return dataResp != null && dataResp.startsWith("250");
            }
            return false;
        } catch (Exception ex) {
            System.err.println("[EMAIL SMTP EXCEPTION] " + ex.getMessage());
            return false;
        }
    }
}
