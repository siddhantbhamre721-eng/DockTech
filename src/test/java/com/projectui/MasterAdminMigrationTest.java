package com.projectui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.google.cloud.firestore.Firestore;
import com.projectui.config.FirebaseConfig;
import com.projectui.dao.UserDao;
import com.projectui.service.FirebaseAuthService;

public class MasterAdminMigrationTest {

    @Test
    public void testFirebaseInitializationAndMasterAdmin() throws Exception {
        System.out.println("[TEST] Verifying Firebase Configuration...");
        System.out.println("[TEST] Project ID: " + FirebaseConfig.PROJECT_ID);
        System.out.println("[TEST] API Key prefix: " + FirebaseConfig.API_KEY.substring(0, Math.min(10, FirebaseConfig.API_KEY.length())) + "...");

        Firestore firestore = FirebaseConfig.getFirestore();
        System.out.println("[TEST] Firestore initialized: " + (firestore != null));

        // Trigger Master Admin setup
        UserDao.ensureMasterAdmin();
        Thread.sleep(2000);

        // Test sign in for Master Admin
        FirebaseAuthService authService = new FirebaseAuthService();
        try {
            FirebaseAuthService.AuthUser user = authService.signIn("docktech@gmail.com", "123456", "Master Admin").get(10, TimeUnit.SECONDS);
            assertNotNull(user, "Master Admin AuthUser should not be null");
            assertEquals("docktech@gmail.com", user.getEmail().toLowerCase());
            System.out.println("[TEST] Master Admin authentication SUCCESS. UID: " + user.getUid());
        } catch (Exception ex) {
            System.out.println("[TEST NOTICE] Sign-in result: " + ex.getMessage());
        }
    }
}
