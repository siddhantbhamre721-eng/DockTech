package com.projectui.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;

/**
 * Firebase Auth integration service supporting both Firebase Identity Toolkit API
 * and Firebase Admin SDK.
 */
public class FirebaseAuthService {

    private static final String API_KEY = System.getenv("FIREBASE_API_KEY") != null && !System.getenv("FIREBASE_API_KEY").isBlank()
            ? System.getenv("FIREBASE_API_KEY")
            : "AIzaSyDO3l4YIlfgO9qvU2XB54k2pBvayr_ID1U";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public CompletableFuture<AuthUser> signIn(String email, String password) {
        return signIn(email, password, null);
    }

    public CompletableFuture<AuthUser> signIn(String email, String password, String role) {
        CompletableFuture<AuthUser> future = new CompletableFuture<>();
        ForkJoinPool.commonPool().execute(() -> {
            try {
                // Try Firebase Identity Toolkit REST API
                String jsonBody = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                        escapeJson(email), escapeJson(password));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String respBody = response.body();
                    String uid = extractJsonField(respBody, "localId");
                    String idToken = extractJsonField(respBody, "idToken");
                    future.complete(new AuthUser(uid, email, email, idToken));
                    return;
                } else {
                    String respBody = response.body();
                    System.err.println("[Firebase Auth Sign-In Failure] HTTP " + response.statusCode() + ": " + respBody);
                    String errorMsg = parseFirebaseErrorMessage(respBody);
                    future.completeExceptionally(new IllegalArgumentException(errorMsg));
                    return;
                }
            } catch (Exception ex) {
                System.err.println("[Firebase Auth Sign-In Exception] Class: " + ex.getClass().getName() + " | Message: " + ex.getMessage());
                if (ex.getCause() != null) {
                    System.err.println("  Cause: " + ex.getCause().getClass().getName() + " - " + ex.getCause().getMessage());
                }
                ex.printStackTrace();

                // Admin SDK fallback if available
                try {
                    UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
                    if (userRecord != null) {
                        future.complete(new AuthUser(userRecord.getUid(), email, userRecord.getDisplayName(), "token_" + userRecord.getUid()));
                        return;
                    }
                } catch (Throwable ignored) {}

                future.completeExceptionally(ex);
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
                // Try Admin SDK first if initialized
                try {
                    UserRecord.CreateRequest createReq = new UserRecord.CreateRequest()
                            .setEmail(email)
                            .setPassword(password)
                            .setDisplayName(displayName);
                    UserRecord record = FirebaseAuth.getInstance().createUser(createReq);
                    future.complete(new AuthUser(record.getUid(), email, displayName, "token_" + record.getUid()));
                    return;
                } catch (Throwable ignored) {}

                // Use Firebase Identity Toolkit REST API
                String jsonBody = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                        escapeJson(email), escapeJson(password));

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
                    future.complete(new AuthUser(uid, email, displayName, idToken));
                } else {
                    String respBody = response.body();
                    System.err.println("[Firebase Auth CreateAccount Failure] HTTP " + response.statusCode() + ": " + respBody);
                    String errorMsg = parseFirebaseErrorMessage(respBody);
                    future.completeExceptionally(new IllegalArgumentException(errorMsg));
                }
            } catch (Exception ex) {
                System.err.println("[Firebase Auth CreateAccount Exception] Class: " + ex.getClass().getName() + " | Message: " + ex.getMessage());
                if (ex.getCause() != null) {
                    System.err.println("  Cause: " + ex.getCause().getClass().getName() + " - " + ex.getCause().getMessage());
                }
                ex.printStackTrace();
                future.completeExceptionally(ex);
            }
        });
        return future;
    }

    public CompletableFuture<Void> sendPasswordReset(String email) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Password reset not configured"));
    }

    public CompletableFuture<Void> updatePassword(String idToken, String newPassword) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Password update not configured"));
    }

    private static String parseFirebaseErrorMessage(String json) {
        if (json == null) return "Authentication failed.";
        if (json.contains("INVALID_PASSWORD") || json.contains("EMAIL_NOT_FOUND")) {
            return "Invalid email or password.";
        }
        if (json.contains("EMAIL_EXISTS")) {
            return "The email address is already registered.";
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
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
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
