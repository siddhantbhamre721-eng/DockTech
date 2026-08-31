package com.projectui;

import com.projectui.dao.UserDao;
import com.projectui.view.admin.AdminDataStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class AdminAccountCleanseTest {

    @Test
    public void testSingleAdminBabuCleanse() {
        // 1. Run cleansing
        UserDao.cleanseAdminAccounts();

        AdminDataStore store = AdminDataStore.getInstance();

        // 2. Check AdminDataStore admin defaults
        Assertions.assertEquals("Babu", store.adminName);
        Assertions.assertEquals("babu@gmail.com", store.adminEmail);

        // 3. Check store.users for Admin count
        int adminCountInStore = 0;
        for (AdminDataStore.UserAccount u : store.users) {
            if ("Admin".equalsIgnoreCase(u.role)) {
                adminCountInStore++;
                Assertions.assertEquals("babu@gmail.com", u.email.toLowerCase());
                Assertions.assertEquals("Babu", u.name);
            }
        }
        Assertions.assertEquals(1, adminCountInStore, "There must be exactly 1 Admin account in store.users (Babu).");

        // 4. Check getFirebaseAuthUsersWithProfiles()
        List<Map<String, Object>> allUsers = UserDao.getFirebaseAuthUsersWithProfiles();
        int adminCountInAuth = 0;
        for (Map<String, Object> u : allUsers) {
            String email = u.get("email") != null ? u.get("email").toString().trim().toLowerCase() : "";
            String role = u.get("role") != null ? u.get("role").toString().trim() : "";

            if ("Admin".equalsIgnoreCase(role)) {
                adminCountInAuth++;
                Assertions.assertEquals("babu@gmail.com", email);
            }
        }
        Assertions.assertEquals(1, adminCountInAuth, "There must be exactly 1 Admin account in live users list (Babu).");
    }
}
