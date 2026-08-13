package com.projectui.service;

import java.util.concurrent.CompletableFuture;

/**
 * Minimal compatibility shim for authentication. If a real Firebase
 * authentication implementation exists in your stash, prefer restoring it.
 * This stub returns a failed future to indicate auth is not configured,
 * allowing the UI to start without silently succeeding.
 */
public class FirebaseAuthService {

    public CompletableFuture<AuthUser> signIn(String email, String password) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Firebase auth not configured"));
    }

    public CompletableFuture<AuthUser> createAccount(String email, String password, String displayName) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Firebase auth not configured"));
    }

    public CompletableFuture<Void> sendPasswordReset(String email) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Firebase auth not configured"));
    }

    public static class AuthUser {
        private final String email;
        private final String displayName;
        private final String idToken;

        public AuthUser(String email, String displayName, String idToken) {
            this.email = email;
            this.displayName = displayName;
            this.idToken = idToken;
        }

        public String getEmail() { return email; }
        public String getDisplayName() { return displayName; }
        public String getIdToken() { return idToken; }
    }
}
