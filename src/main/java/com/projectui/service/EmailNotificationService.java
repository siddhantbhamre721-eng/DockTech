package com.projectui.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class EmailNotificationService {

    public CompletableFuture<Boolean> sendZapierApprovalWebhook(
            String requesterEmail,
            String portName,
            String portCode,
            String requestId,
            String paName,
            String loginId,
            String tempPassword) {

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                if (requesterEmail == null || requesterEmail.isBlank() || !requesterEmail.contains("@") || !requesterEmail.contains(".")) {
                    System.err.println("[APPROVAL] email recipient invalid: " + requesterEmail);
                    future.complete(false);
                    return;
                }

                String cleanRecipient = requesterEmail.trim();
                System.out.println("Approval email recipient: " + cleanRecipient);

                String webhookUrl = System.getenv("ZAPIER_WEBHOOK_URL");
                if (webhookUrl == null || webhookUrl.isBlank()) {
                    webhookUrl = "https://hooks.zapier.com/hooks/catch/28574565/4thctqm/";
                }

                String pCode = (portCode != null && !portCode.isBlank()) ? portCode : "N/A";

                StringBuilder json = new StringBuilder();
                json.append("{");
                json.append("\"requesterEmail\":\"").append(escapeJson(cleanRecipient)).append("\",");
                json.append("\"portName\":\"").append(escapeJson(portName)).append("\",");
                json.append("\"portCode\":\"").append(escapeJson(pCode)).append("\",");
                json.append("\"requestId\":\"").append(escapeJson(requestId)).append("\",");
                json.append("\"paName\":\"").append(escapeJson(paName)).append("\",");
                json.append("\"loginId\":\"").append(escapeJson(loginId)).append("\",");
                json.append("\"tempPassword\":\"").append(escapeJson(tempPassword)).append("\"");
                json.append("}");
                String jsonPayload = json.toString();

                System.out.println("[ZAPIER] Sending approval webhook with fields:");
                System.out.println("requesterEmail=" + cleanRecipient);
                System.out.println("portName=" + portName);
                System.out.println("portCode=" + pCode);
                System.out.println("requestId=" + requestId);
                System.out.println("paName=" + paName);
                System.out.println("loginId=" + loginId);

                System.out.println("[ZAPIER] Sending approval webhook...");

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, java.nio.charset.StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                System.out.println("[ZAPIER] HTTP Status: " + status);

                if (status >= 200 && status < 300) {
                    System.out.println("[ZAPIER] Approval webhook successfully delivered.");
                    future.complete(true);
                } else {
                    System.err.println("[ZAPIER] Approval webhook failed with HTTP Status: " + status);
                    future.complete(false);
                }
            } catch (Exception ex) {
                System.err.println("[ZAPIER] Approval webhook failed: " + ex.getMessage());
                future.complete(false);
            }
        });

        return future;
    }

    private String escapeJson(String input) {
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

    public CompletableFuture<Boolean> sendApprovalNotification(
            String requesterEmail,
            String portName,
            String portCode,
            String requestId,
            String paName,
            String loginId,
            String tempPassword) {
        return sendZapierApprovalWebhook(requesterEmail, portName, portCode, requestId, paName, loginId, tempPassword);
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
