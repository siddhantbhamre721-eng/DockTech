package com.projectui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.projectui.view.login.LoginPage;

public class AdminLoginRoleMappingTest {

    @Test
    public void testAdminToMasterAdminRoleMapping() {
        // Requirement 1: "Treat Admin in the login dropdown as the existing Master Admin account role internally."
        // Requirement 2: "Update the role comparison/mapping logic so Admin -> Master Admin."
        assertTrue(LoginPage.isRoleMatch("Admin", "Master Admin"),
                "Selected role 'Admin' must match account role 'Master Admin'");
        
        assertTrue(LoginPage.isRoleMatch("Admin", "Dock Tech Admin"),
                "Selected role 'Admin' must match 'Dock Tech Admin'");

        assertTrue(LoginPage.isRoleMatch("Admin", "Administrator"),
                "Selected role 'Admin' must match 'Administrator'");

        assertTrue(LoginPage.isRoleMatch("Master Admin", "Master Admin"),
                "Selected role 'Master Admin' must match 'Master Admin'");

        assertTrue(LoginPage.isRoleMatch("Master Admin", "Admin"),
                "Selected role 'Master Admin' must match 'Admin'");
    }

    @Test
    public void testNormalizeRoleForAdmin() {
        assertEquals("Master Admin", LoginPage.normalizeRole("Admin"),
                "normalizeRole('Admin') must normalize to 'Master Admin'");

        assertEquals("Master Admin", LoginPage.normalizeRole("Master Admin"),
                "normalizeRole('Master Admin') must normalize to 'Master Admin'");

        assertEquals("Master Admin", LoginPage.normalizeRole("administrator"),
                "normalizeRole('administrator') must normalize to 'Master Admin'");
    }

    @Test
    public void testNonAdminRolesDoNotMatch() {
        assertFalse(LoginPage.isRoleMatch("Admin", "Customs Officer"),
                "Selected role 'Admin' must not match 'Customs Officer'");

        assertFalse(LoginPage.isRoleMatch("Admin", "Port Authority"),
                "Selected role 'Admin' must not match 'Port Authority'");

        assertFalse(LoginPage.isRoleMatch("Admin", "Exporter"),
                "Selected role 'Admin' must not match 'Exporter'");

        assertFalse(LoginPage.isRoleMatch("Admin", "Importer"),
                "Selected role 'Admin' must not match 'Importer'");

        assertFalse(LoginPage.isRoleMatch("Admin", "Ship Captain"),
                "Selected role 'Admin' must not match 'Ship Captain'");
    }

    @Test
    public void testLoginDropdownKeepsAdminLabel() {
        // Requirement: "Keep the dropdown label as 'Admin'."
        boolean foundAdmin = false;
        try {
            java.lang.reflect.Field rolesField = LoginPage.class.getDeclaredField("ROLES");
            rolesField.setAccessible(true);
            String[] roles = (String[]) rolesField.get(null);
            for (String role : roles) {
                if ("Admin".equals(role)) {
                    foundAdmin = true;
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertTrue(foundAdmin, "The login dropdown items must include 'Admin'");
    }
}
