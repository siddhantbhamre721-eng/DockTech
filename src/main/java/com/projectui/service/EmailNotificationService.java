package com.projectui.service;

import java.util.concurrent.CompletableFuture;

public class EmailNotificationService {

    public CompletableFuture<Boolean> sendApprovalNotification(
            String requesterEmail,
            String portName,
            String portCode,
            String requestId,
            String paName,
            String loginId,
            String tempPassword) {

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        String subject = "New Port Request Approved — " + portName;
        String body = String.format(
                "====================================================\n" +
                "DOCK TECH PORT MANAGEMENT NOTIFICATION\n" +
                "====================================================\n\n" +
                "Dear Requester,\n\n" +
                "We are pleased to inform you that your New Port Request has been APPROVED by Dock Tech Admin.\n\n" +
                "REQUEST & PORT DETAILS:\n" +
                "- Request ID: %s\n" +
                "- Port Name: %s\n" +
                "- Port Code: %s\n" +
                "- Status: APPROVED\n\n" +
                "PROVISIONED PORT AUTHORITY ACCOUNT:\n" +
                "- Port Authority Name: %s\n" +
                "- Account Status: ACTIVE\n" +
                "- Login ID: %s\n" +
                "- Temporary Password: %s\n\n" +
                "INSTRUCTIONS:\n" +
                "The designated Port Authority can now log in using the credentials above at the existing Dock Tech login page.\n\n" +
                "Best regards,\n" +
                "Dock Tech Administrative Team\n" +
                "====================================================",
                requestId, portName, (portCode != null && !portCode.isBlank() ? portCode : "N/A"),
                paName, loginId, tempPassword
        );

        System.out.println("[EMAIL NOTIFICATION SERVICE] Sending Approval Email to: " + requesterEmail);
        System.out.println("[SUBJECT] " + subject);
        System.out.println("[BODY]\n" + body);

        // Async simulation of server-side / Firebase mail dispatch
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(300);
                System.out.println("[EMAIL NOTIFICATION SERVICE] Notification successfully dispatched to " + requesterEmail);
                future.complete(true);
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            }
        });

        return future;
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
}
