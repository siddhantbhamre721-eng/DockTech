package com.projectui;

import com.projectui.dao.PortRequestDao;
import com.projectui.dao.UserDao;
import com.projectui.service.FirebaseAuthService;
import com.projectui.view.admin.AdminDataStore;
import com.projectui.view.login.LoginPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class PortAuthorityLoginTest {

    @Test
    public void testPortAuthorityApprovalAndLoginFlow() throws Exception {
        UserDao userDao = new UserDao();
        PortRequestDao portRequestDao = new PortRequestDao();
        FirebaseAuthService authService = new FirebaseAuthService();

        String testPortName = "Port of Mangalore International";
        String testPortCode = "IN-MNG-09";
        String testPaName = "Captain K. Ramesh";
        String testPaEmail = "pa_mangalore_" + System.currentTimeMillis() + "@docktech.com";
        String testReqEmail = "director_ramesh_" + System.currentTimeMillis() + "@gmail.com";
        String testRequestId = "REQ-" + (int)(Math.random() * 8999 + 1000);

        // 1. Submit Port Request
        portRequestDao.savePortRequestFull(
                testRequestId, testPortName, testPortCode, "Mangalore, Karnataka", "Panambur, Mangalore", "Deepwater Terminal",
                testPaName, testReqEmail, "+91 9887766554", testPaName, testPaEmail, "Aadhaar.pdf, Proposal.pdf",
                "Aadhaar.pdf", "https://res.cloudinary.com/snmxmesm/image/upload/doc1.pdf", "p1", "pdf", "pdf", "",
                "Proposal.pdf", "https://res.cloudinary.com/snmxmesm/image/upload/doc2.pdf", "p2", "pdf", "pdf", ""
        );

        // 2. Approve the Port Request
        PortRequestDao.applyStatusToStores(testRequestId, "APPROVED", null);

        // 3. Provision Port Authority Account
        Map<String, Object> extra = new HashMap<>();
        extra.put("requestId", testRequestId);
        extra.put("portName", testPortName);
        extra.put("portCode", testPortCode);
        extra.put("assignedPort", testPortName);
        extra.put("requesterEmail", testReqEmail);
        extra.put("proposedPaEmail", testPaEmail);
        extra.put("roleCode", "PORT_AUTHORITY");
        extra.put("role", "Port Authority");

        Map<String, String> creds = userDao.provisionAccountExtended("Admin", testPaName, testPaEmail, testPortName,
                "+91 9887766554", "Port Authority", "Active", extra).get();

        Assertions.assertNotNull(creds);
        String tempPass = creds.get("tempPassword");
        Assertions.assertNotNull(tempPass);
        Assertions.assertFalse(tempPass.isBlank());

        // 4. Test Role Normalization & Matching
        Assertions.assertEquals("Port Authority", LoginPage.normalizeRole("PORT_AUTHORITY"));
        Assertions.assertEquals("Port Authority", LoginPage.normalizeRole("Port Authority"));
        Assertions.assertEquals("Port Authority", LoginPage.normalizeRole("Port_Authority"));
        Assertions.assertEquals("Port Authority", LoginPage.normalizeRole("PORT AUTHORITY"));

        Assertions.assertTrue(LoginPage.isPortAuthorityRole("PORT_AUTHORITY"));
        Assertions.assertTrue(LoginPage.isPortAuthorityRole("Port Authority"));
        Assertions.assertTrue(LoginPage.isPortAuthorityRole("PORT"));
        Assertions.assertTrue(LoginPage.isPortAuthorityRole("PA"));

        Assertions.assertTrue(LoginPage.isRoleMatch("Port Authority", "PORT_AUTHORITY"));
        Assertions.assertTrue(LoginPage.isRoleMatch("Port Authority", "Port Authority"));
        Assertions.assertTrue(LoginPage.isRoleMatch("Port Authority", "PORT_AUTHORITY_OPERATIONS"));

        // 5. Test Profile Retrieval via Email and Login ID
        Map<String, Object> profileByPaEmail = userDao.getUserProfile("usr_" + Math.abs(testPaEmail.hashCode()), testPaEmail).get();
        Assertions.assertNotNull(profileByPaEmail);
        Assertions.assertEquals("Port Authority", profileByPaEmail.get("role"));
        Assertions.assertEquals("Active", profileByPaEmail.get("status"));
        Assertions.assertEquals(testPortName, profileByPaEmail.get("assignedPort"));

        // 6. Test Authentication with temporary password
        var authUser = authService.signIn(testPaEmail, tempPass, "Port Authority").get();
        Assertions.assertNotNull(authUser);
        Assertions.assertEquals(testPaEmail, authUser.getEmail());

        // 7. Verify Persistence
        UserDao.saveLocalDiskUser(profileByPaEmail);
        UserDao.restoreLocalDiskUsers();
        Map<String, Object> reloadedProfile = userDao.getCachedUserProfile(profileByPaEmail.get("uid").toString(), testPaEmail);
        Assertions.assertNotNull(reloadedProfile);
        Assertions.assertEquals("Port Authority", reloadedProfile.get("role"));
    }
}
