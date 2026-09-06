package com.projectui;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.projectui.service.FirebaseAuthService;

public class FirebaseAuthValidationTest {

    @Test
    public void testNonExistentUserSignInFails() {
        FirebaseAuthService authService = new FirebaseAuthService();
        ExecutionException nonExistentEx = assertThrows(ExecutionException.class, () -> {
            authService.signIn("nonexistent_test_user_" + System.currentTimeMillis() + "@docktechtest.com", "InvalidPass999", "Importer").get();
        });
        Throwable cause = nonExistentEx.getCause();
        assertNotNull(cause);
        assertTrue(cause.getMessage().toLowerCase().contains("invalid email or password"),
                "Nonexistent user error message must be 'Invalid email or password.', but was: " + cause.getMessage());
    }

    @Test
    public void testRoleNormalizationAndMatching() {
        assertTrue(com.projectui.view.login.LoginPage.isRoleMatch("Importer", "Importer"));
        assertTrue(com.projectui.view.login.LoginPage.isRoleMatch("Exporter", "Exporter"));
        assertTrue(com.projectui.view.login.LoginPage.isRoleMatch("Admin", "Admin"));
        assertTrue(com.projectui.view.login.LoginPage.isRoleMatch("Port Authority", "PORT_AUTHORITY"));
        assertTrue(com.projectui.view.login.LoginPage.isRoleMatch("Ship Captain", "SHIP_CAPTAIN"));
        assertTrue(com.projectui.view.login.LoginPage.isRoleMatch("Custom Officer", "CUSTOMS_OFFICER"));
    }

    @Test
    public void testMasterAdminFirebaseAuthSignIn() throws Exception {
        FirebaseAuthService authService = new FirebaseAuthService();
        FirebaseAuthService.AuthUser authUser = authService.signIn("docktech@gmail.com", "123456", "Admin").get();
        assertNotNull(authUser, "AuthUser must not be null for valid Firebase credentials");
        assertEquals("docktech@gmail.com", authUser.getEmail().toLowerCase());
        assertNotNull(authUser.getUid());
        assertNotNull(authUser.getIdToken());
    }

    @Test
    public void testPuriPortAuthorityDiagnostics() throws Exception {
        FirebaseAuthService authService = new FirebaseAuthService();
        String resetLink = authService.generatePasswordResetLink("puri.portauthority@gmail.com").get();
        System.out.println("[DIAGNOSTIC] Password reset link for puri.portauthority@gmail.com: " + resetLink);
        assertNotNull(resetLink);
    }
}
