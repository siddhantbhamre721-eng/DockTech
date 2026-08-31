package com.projectui;

import com.projectui.dao.PortRequestDao;
import com.projectui.dao.UserDao;
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

        String testPortName = "Port of Mangalore International";
        String testPortCode = "IN-MNG-09";
        String testPaName = "Captain K. Ramesh";
        String testPaEmail = "test_pa_mangalore@docktech.com";
        String testReqEmail = "test_director_ramesh@gmail.com";
        String testRequestId = "REQ-" + (int)(Math.random() * 8999 + 1000);
        String testUid = "usr_test_mangalore_" + Math.abs(testPaEmail.hashCode());

        // 1. Submit Port Request
        portRequestDao.savePortRequestFull(
                testRequestId, testPortName, testPortCode, "Mangalore, Karnataka", "Panambur, Mangalore", "Deepwater Terminal",
                testPaName, testReqEmail, "+91 9887766554", testPaName, testPaEmail, "Aadhaar.pdf, Proposal.pdf",
                "Aadhaar.pdf", "https://res.cloudinary.com/snmxmesm/image/upload/doc1.pdf", "p1", "pdf", "pdf", "",
                "Proposal.pdf", "https://res.cloudinary.com/snmxmesm/image/upload/doc2.pdf", "p2", "pdf", "pdf", ""
        );

        // 2. Approve the Port Request
        PortRequestDao.applyStatusToStores(testRequestId, "APPROVED", null);

        // 3. Save User Profile for testing role and storage
        Map<String, Object> extra = new HashMap<>();
        extra.put("requestId", testRequestId);
        extra.put("portName", testPortName);
        extra.put("portCode", testPortCode);
        extra.put("assignedPort", testPortName);
        extra.put("requesterEmail", testReqEmail);
        extra.put("proposedPaEmail", testPaEmail);
        extra.put("roleCode", "PORT_AUTHORITY");
        extra.put("role", "Port Authority");

        userDao.saveUserExtended(testUid, testPaEmail, testPaName, testPortName,
                "+91 9887766554", "Port Authority", "Active", extra).get();

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
        Map<String, Object> profileByPaEmail = userDao.getUserProfile(testUid, testPaEmail).get();
        Assertions.assertNotNull(profileByPaEmail);
        Assertions.assertEquals("Port Authority", profileByPaEmail.get("role"));
        Assertions.assertEquals("Active", profileByPaEmail.get("status"));
        Assertions.assertEquals(testPortName, profileByPaEmail.get("assignedPort"));

        // 6. Verify Persistence
        UserDao.saveLocalDiskUser(profileByPaEmail);
        UserDao.restoreLocalDiskUsers();
        Map<String, Object> reloadedProfile = userDao.getCachedUserProfile(testUid, testPaEmail);
        Assertions.assertNotNull(reloadedProfile);
        Assertions.assertEquals("Port Authority", reloadedProfile.get("role"));

        // 7. Test User Deletion
        userDao.deleteUserAccount(testUid, testPaEmail).get();

        Assertions.assertTrue(UserDao.isUserDeleted(testUid, testPaEmail));
        Assertions.assertNull(userDao.getCachedUserProfile(testUid, testPaEmail));
        Map<String, Object> deletedProfileLookup = userDao.getUserProfile(testUid, testPaEmail).get();
        Assertions.assertNull(deletedProfileLookup);

        // 8. Test Port Deletion
        com.projectui.view.admin.AdminDataStore.getInstance().saveDeletedPort(testPortName, testPortCode);
        Assertions.assertTrue(com.projectui.view.admin.AdminDataStore.isPortDeleted(testPortName, testPortCode));

        // 9. Test Recreating Port & User
        com.projectui.view.admin.AdminDataStore.getInstance().clearDeletedPort(testPortName, testPortCode);
        Assertions.assertFalse(com.projectui.view.admin.AdminDataStore.isPortDeleted(testPortName, testPortCode));
        UserDao.clearDeletedUser(testUid, testPaEmail);
        Assertions.assertFalse(UserDao.isUserDeleted(testUid, testPaEmail));

        // 10. Cleanup test port request from Firestore and memory
        try {
            portRequestDao.deletePortRequest(testRequestId).get();
        } catch (Exception ignored) {}
    }

    @Test
    public void testMultiplePortAuthoritiesIndependentCreation() throws Exception {
        UserDao userDao = new UserDao();

        String portA = "Kandla Deepwater Port";
        String paEmailA = "test_pa_kandla@docktech.com";
        String uidA = "usr_kandla_" + Math.abs(paEmailA.hashCode());
        userDao.saveUserExtended(uidA, paEmailA, "Port Authority (Kandla)", portA, "+91 9811223344", "Port Authority", "Active", null).get();

        String portB = "Paradip Coal Port";
        String paEmailB = "test_pa_paradip@docktech.com";
        String uidB = "usr_paradip_" + Math.abs(paEmailB.hashCode());
        userDao.saveUserExtended(uidB, paEmailB, "Port Authority (Paradip)", portB, "+91 9855667788", "Port Authority", "Active", null).get();

        Assertions.assertNotEquals(uidA, uidB);

        // Cleanup
        userDao.deleteUserAccount(uidA, paEmailA).get();
        userDao.deleteUserAccount(uidB, paEmailB).get();
    }

    @Test
    public void testPortAuthorityPortLinkageAndAuthorization() throws Exception {
        UserDao userDao = new UserDao();
        com.projectui.view.admin.AdminDataStore store = com.projectui.view.admin.AdminDataStore.getInstance();

        String portName = "Port of Mumbai Central";
        String portCode = "IN-BOM-99";
        String paEmail = "test_pa_mumbai_link@docktech.com";
        String uid = "usr_mumbai_link_" + Math.abs(paEmail.hashCode());

        // 1. Create Port record
        com.projectui.view.admin.AdminDataStore.clearDeletedPort(portName, portCode);
        com.projectui.view.admin.AdminDataStore.Port createdPort = new com.projectui.view.admin.AdminDataStore.Port(
                portName, "Mumbai, Maharashtra", portCode, "Mumbai Port Authority", 15, 0,
                "Operational", "Containers", "80%", "Clear", store.now(), 18.94, 72.83, "APPROVED"
        );
        store.ports.add(createdPort);

        // 2. Create PA profile with portId and assignedPort
        Map<String, Object> extra = new HashMap<>();
        extra.put("portId", portCode);
        extra.put("portCode", portCode);
        extra.put("assignedPort", portName);
        extra.put("portName", portName);
        userDao.saveUserExtended(uid, paEmail, "Mumbai Port Authority", portName, "+91 9800001122", "Port Authority", "Active", extra).get();

        // 3. Verify port is resolvable by code and name
        com.projectui.view.admin.AdminDataStore.Port lookupByCode = store.getPortByIdOrName(portCode);
        Assertions.assertNotNull(lookupByCode, "Port should be resolvable by portCode/portId");
        Assertions.assertEquals(portName, lookupByCode.name);

        com.projectui.view.admin.AdminDataStore.Port lookupByName = store.getPortByIdOrName(portName);
        Assertions.assertNotNull(lookupByName, "Port should be resolvable by portName");
        Assertions.assertEquals(portCode, lookupByName.code);

        // 4. Test port deletion deactivates PA profile
        store.deletePort(createdPort);
        Map<String, Object> cachedProfile = userDao.getCachedUserProfile(uid, paEmail);
        Assertions.assertNotNull(cachedProfile);
        Assertions.assertEquals("Inactive", cachedProfile.get("status"));
        Assertions.assertEquals("DELETED", cachedProfile.get("portStatus"));

        // Cleanup
        userDao.deleteUserAccount(uid, paEmail).get();
        com.projectui.view.admin.AdminDataStore.clearDeletedPort(portName, portCode);
    }

    @Test
    public void testPuriPortAuthorityResolution() throws Exception {
        com.projectui.view.admin.AdminDataStore store = com.projectui.view.admin.AdminDataStore.getInstance();
        com.projectui.view.admin.AdminDataStore.Port puriPort = store.getPortByIdOrName("Puri Port");
        Assertions.assertNotNull(puriPort, "Puri Port must be resolvable in AdminDataStore");
        Assertions.assertEquals("Puri Port", puriPort.name);
        Assertions.assertEquals("Operational", puriPort.status);

        UserDao userDao = new UserDao();
        Map<String, Object> profile = userDao.getUserProfile("test_puri_uid_123", "puri.portauthority@gmail.com").get();
        Assertions.assertNotNull(profile, "Profile for puri.portauthority@gmail.com must be resolved");
        Assertions.assertEquals("Port Authority", profile.get("role"));
        Assertions.assertEquals("Active", profile.get("status"));
        Assertions.assertEquals("Puri Port", profile.get("assignedPort"));
        Assertions.assertTrue(LoginPage.isPortAuthorityRole((String) profile.get("role")));
        Assertions.assertTrue(LoginPage.isRoleMatch("Port Authority", (String) profile.get("role")));
    }
}
