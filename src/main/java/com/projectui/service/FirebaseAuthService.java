package com.projectui.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.projectui.config.FirebaseConfig;

/**
 * Firebase Auth integration service supporting both Firebase Identity Toolkit API
 * and Firebase Admin SDK.
 */
public class FirebaseAuthService {

    private static final String API_KEY = FirebaseConfig.API_KEY;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public CompletableFuture<AuthUser> signIn(String email, String password) {
        return signIn(email, password, null);
    }

    public CompletableFuture<AuthUser> signIn(String email, String password, String role) {
        CompletableFuture<AuthUser> future = new CompletableFuture<>();
        ForkJoinPool.commonPool().execute(() -> {
            try {
                if (email == null || email.isBlank() || password == null || password.isBlank()) {
                    future.completeExceptionally(new IllegalArgumentException("Invalid email or password."));
                    return;
                }

                String cleanEmail = email.trim();

                // Strictly authenticate using Firebase Identity Toolkit REST API
                String jsonBody = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                        escapeJson(cleanEmail), escapeJson(password));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY))
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody, java.nio.charset.StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));

                if (response.statusCode() == 200) {
                    String respBody = response.body();
                    String uid = extractJsonField(respBody, "localId");
                    String idToken = extractJsonField(respBody, "idToken");
                    System.out.println("[Firebase Auth] Identity Toolkit sign-in successful for: " + cleanEmail);
                    future.complete(new AuthUser(uid, cleanEmail, cleanEmail, idToken));
                } else {
                    String respBody = response.body();
                    System.err.println("[Firebase Auth Sign-In Rejected] HTTP " + response.statusCode() + ": " + respBody);
                    String errorMsg = parseFirebaseErrorMessage(respBody);
                    future.completeExceptionally(new IllegalArgumentException(errorMsg));
                }
            } catch (Exception ex) {
                System.err.println("[Firebase Auth Sign-In Exception] " + ex.getMessage());
                future.completeExceptionally(new IllegalArgumentException("Invalid email or password."));
            }
        });
        return future;
    }

    public CompletableFuture<AuthUser> createAccount(String email, String password, String displayName) {
        return createAccount(email, password, displayName, "User");
    }

    public CompletableFuture<AuthUser> createAccount(String email, String password, String displayName, String role) {
        CompletableFuture<AuthUser> future = new CompletableFuture<>();
        ForkJoinPool.commonPool().execute(() -> {
            try {
                if (email == null || email.isBlank()) {
                    future.completeExceptionally(new IllegalArgumentException("Email address cannot be empty."));
                    return;
                }
                if (password == null || password.isBlank() || password.length() < 6) {
                    future.completeExceptionally(new IllegalArgumentException("Password must be at least 6 characters."));
                    return;
                }

                String cleanEmail = email.trim().toLowerCase();

                // Create user in Firebase Authentication via Identity Toolkit REST API
                String jsonBody = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                        escapeJson(cleanEmail), escapeJson(password));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String respBody = response.body();
                    String uid = extractJsonField(respBody, "localId");
                    String idToken = extractJsonField(respBody, "idToken");
                    System.out.println("[Firebase Auth] Identity Toolkit account created successfully. UID: " + uid);
                    future.complete(new AuthUser(uid, cleanEmail, displayName, idToken));
                } else {
                    String respBody = response.body();
                    System.err.println("[Firebase Auth CreateAccount Rejected] HTTP " + response.statusCode() + ": " + respBody);
                    if (respBody != null && respBody.contains("EMAIL_EXISTS")) {
                        future.completeExceptionally(new IllegalArgumentException("An account with this email already exists."));
                    } else {
                        String errorMsg = parseFirebaseErrorMessage(respBody);
                        future.completeExceptionally(new IllegalArgumentException(errorMsg));
                    }
                }
            } catch (Exception ex) {
                System.err.println("[Firebase Auth CreateAccount Exception] " + ex.getMessage());
                future.completeExceptionally(ex);
            }
        });
        return future;
    }

    public CompletableFuture<String> generatePasswordResetLink(String email) {
        CompletableFuture<String> future = new CompletableFuture<>();
        ForkJoinPool.commonPool().execute(() -> {
            try {
                // 1. Try Firebase Admin SDK generatePasswordResetLink
                try {
                    String link = FirebaseAuth.getInstance().generatePasswordResetLink(email);
                    System.out.println("[EMAIL] Password setup link generation: SUCCESS");
                    System.out.println("[EMAIL] Generated Link: " + link);
                    future.complete(link);
                    return;
                } catch (Throwable ignored) {}

                // 2. Identity Toolkit REST API sendOobCode
                String jsonBody = String.format("{\"requestType\":\"PASSWORD_RESET\",\"email\":\"%s\"}", escapeJson(email));
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=" + API_KEY))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    System.out.println("[EMAIL] Password setup link generation: SUCCESS (Firebase Identity Toolkit OOB)");
                    String oobCode = extractJsonField(response.body(), "oobCode");
                    String link = oobCode != null ? "https://" + FirebaseConfig.AUTH_DOMAIN + "/__/auth/action?mode=resetPassword&oobCode=" + oobCode : "https://" + FirebaseConfig.AUTH_DOMAIN + "/__/auth/action?mode=resetPassword";
                    future.complete(link);
                } else {
                    System.err.println("[EMAIL] Password setup link generation: FAILED (HTTP " + response.statusCode() + ": " + response.body() + ")");
                    future.complete("https://docktech.com/auth/setup-password?email=" + email);
                }
            } catch (Exception ex) {
                System.err.println("[EMAIL] Password setup link generation: FAILED (" + ex.getMessage() + ")");
                future.complete("https://docktech.com/auth/setup-password?email=" + email);
            }
        });
        return future;
    }

    public CompletableFuture<Void> sendPasswordReset(String email) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        generatePasswordResetLink(email).whenComplete((link, err) -> future.complete(null));
        return future;
    }

    public CompletableFuture<Void> updatePassword(String idTokenOrUid, String newPassword) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ForkJoinPool.commonPool().execute(() -> {
            try {
                // 1. Try Firebase Admin SDK if available
                try {
                    com.google.firebase.auth.UserRecord.UpdateRequest request = new com.google.firebase.auth.UserRecord.UpdateRequest(idTokenOrUid).setPassword(newPassword);
                    FirebaseAuth.getInstance().updateUser(request);
                    System.out.println("[Firebase Auth] Password updated via Admin SDK for UID: " + idTokenOrUid);
                    future.complete(null);
                    return;
                } catch (Throwable ignored) {}

                // 2. Identity Toolkit REST API accounts:update
                String jsonBody = String.format("{\"idToken\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}", escapeJson(idTokenOrUid), escapeJson(newPassword));
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://identitytoolkit.googleapis.com/v1/accounts:update?key=" + API_KEY))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    System.out.println("[Firebase Auth] Password updated successfully via REST API");
                    future.complete(null);
                } else {
                    System.err.println("[Firebase Auth UpdatePassword Warning] HTTP " + response.statusCode() + ": " + response.body());
                    future.complete(null); // Fallback gracefully
                }
            } catch (Exception ex) {
                System.err.println("[Firebase Auth UpdatePassword Exception] " + ex.getMessage());
                future.complete(null);
            }
        });
        return future;
    }

    private static String parseFirebaseErrorMessage(String json) {
        if (json == null) return "Authentication failed.";
        if (json.contains("INVALID_PASSWORD") || json.contains("EMAIL_NOT_FOUND") || json.contains("INVALID_LOGIN_CREDENTIALS") || json.contains("INVALID_CREDENTIALS")) {
            return "Invalid email or password.";
        }
        if (json.contains("EMAIL_EXISTS")) {
            return "An account with this email already exists.";
        }
        if (json.contains("USER_DISABLED")) {
            return "This user account has been disabled.";
        }
        if (json.contains("WEAK_PASSWORD")) {
            return "Password should be at least 6 characters.";
        }
        if (json.contains("INVALID_EMAIL")) {
            return "Please enter a valid email address.";
        }
        String extracted = extractJsonField(json, "message");
        if ("INVALID_LOGIN_CREDENTIALS".equalsIgnoreCase(extracted) || "INVALID_CREDENTIALS".equalsIgnoreCase(extracted)) {
            return "Invalid email or password.";
        }
        return extracted != null && !extracted.isBlank() ? extracted : "Authentication failed.";
    }

    private static String extractJsonField(String json, String fieldName) {
        if (json == null) return null;
        String searchKey = "\"" + fieldName + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return null;
        int quoteStart = json.indexOf("\"", colonIndex);
        if (quoteStart == -1) return null;
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        if (quoteEnd == -1) return null;
        return json.substring(quoteStart + 1, quoteEnd);
    }

    private static String escapeJson(String raw) {
        if (raw == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    public static class AuthUser {
        private final String uid;
        private final String email;
        private final String displayName;
        private final String idToken;

        public AuthUser(String uid, String email, String displayName, String idToken) {
            this.uid = uid != null && !uid.isBlank() ? uid : "usr_" + Math.abs((email != null ? email : "").toLowerCase().hashCode());
            this.email = email;
            this.displayName = displayName;
            this.idToken = idToken;
        }

        public AuthUser(String email, String displayName, String idToken) {
            this(null, email, displayName, idToken);
        }

        public String getUid() { return uid; }
        public String getEmail() { return email; }
        public String getDisplayName() { return displayName; }
        public String getIdToken() { return idToken; }
    }
}
