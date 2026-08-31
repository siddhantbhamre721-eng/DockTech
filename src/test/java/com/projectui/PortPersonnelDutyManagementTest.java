package com.projectui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.projectui.dao.UserDao;

public class PortPersonnelDutyManagementTest {

    private final String CAPTAIN_UID = "UID_TEST_CAPTAIN_" + System.currentTimeMillis();
    private final String CAPTAIN_EMAIL = "test_captain_" + System.currentTimeMillis() + "@shipping.com";

    private final String OFFICER_UID = "UID_TEST_OFFICER_" + System.currentTimeMillis();
    private final String OFFICER_EMAIL = "test_officer_" + System.currentTimeMillis() + "@customs.gov";

    private final String VIEWING_PORT = "Port of Mumbai";
    private final UserDao userDao = new UserDao();

    @BeforeEach
    public void setUp() throws Exception {
        // 1. Simulate Captain registered via application account creation system
        Map<String, Object> capExtra = new HashMap<>();
        capExtra.put("uid", CAPTAIN_UID);
        capExtra.put("email", CAPTAIN_EMAIL);
        capExtra.put("name", "Captain Vikram Sharma");
        capExtra.put("role", "Ship Captain");
        capExtra.put("company", "Pacific Maritime Agency");
        capExtra.put("organization", "Pacific Maritime Agency");
        capExtra.put("phone", "+91 9876543210");
        capExtra.put("status", "Active");
        capExtra.put("assignedShip", "Not Assigned");
        capExtra.put("duty", "Not Assigned");
        capExtra.put("assignmentStatus", "Available");

        userDao.saveUserExtended(CAPTAIN_UID, CAPTAIN_EMAIL, "Captain Vikram Sharma",
                "Pacific Maritime Agency", "+91 9876543210", "Ship Captain", "Active", capExtra).get();

        // 2. Simulate Customs Officer registered via application account creation system
        Map<String, Object> offExtra = new HashMap<>();
        offExtra.put("uid", OFFICER_UID);
        offExtra.put("email", OFFICER_EMAIL);
        offExtra.put("name", "Officer Ananya Roy");
        offExtra.put("role", "Customs Officer");
        offExtra.put("company", "Indian Customs & Border Security");
        offExtra.put("organization", "Indian Customs & Border Security");
        offExtra.put("phone", "+91 9811223344");
        offExtra.put("status", "Active");
        offExtra.put("duty", "Not Assigned");
        offExtra.put("assignmentStatus", "Available");

        userDao.saveUserExtended(OFFICER_UID, OFFICER_EMAIL, "Officer Ananya Roy",
                "Indian Customs & Border Security", "+91 9811223344", "Customs Officer", "Active", offExtra).get();
    }

    @AfterEach
    public void tearDown() {
        userDao.deleteUserAccount(CAPTAIN_UID, CAPTAIN_EMAIL).join();
        userDao.deleteUserAccount(OFFICER_UID, OFFICER_EMAIL).join();
    }

    @Test
    public void testExistingPersonnelLoadedWithoutDuplicateAccounts() {
        // Retrieve Captains for Port of Mumbai
        List<Map<String, Object>> captains = UserDao.getPersonnelForPort(VIEWING_PORT, "Captain");
        assertNotNull(captains);

        Map<String, Object> foundCap = null;
        int capCount = 0;
        for (Map<String, Object> c : captains) {
            if (CAPTAIN_UID.equals(c.get("uid")) || CAPTAIN_EMAIL.equalsIgnoreCase((String) c.get("email"))) {
                foundCap = c;
                capCount++;
            }
        }

        assertNotNull(foundCap, "Existing registered Captain must appear in Port Personnel list");
        assertEquals(1, capCount, "Captain must appear exactly once (no duplicate records)");
        assertEquals("Captain Vikram Sharma", foundCap.get("name"));
        assertEquals(CAPTAIN_EMAIL, foundCap.get("email"));
        assertEquals("Pacific Maritime Agency", foundCap.get("company"));

        // Retrieve Customs Officers for Port of Mumbai
        List<Map<String, Object>> officers = UserDao.getPersonnelForPort(VIEWING_PORT, "Officer");
        assertNotNull(officers);

        Map<String, Object> foundOff = null;
        int offCount = 0;
        for (Map<String, Object> o : officers) {
            if (OFFICER_UID.equals(o.get("uid")) || OFFICER_EMAIL.equalsIgnoreCase((String) o.get("email"))) {
                foundOff = o;
                offCount++;
            }
        }

        assertNotNull(foundOff, "Existing registered Customs Officer must appear in Port Personnel list");
        assertEquals(1, offCount, "Officer must appear exactly once (no duplicate records)");
        assertEquals("Officer Ananya Roy", foundOff.get("name"));
        assertEquals(OFFICER_EMAIL, foundOff.get("email"));
    }

    @Test
    public void testAssignmentPreservesExistingUidAndDoesNotCreateDuplicates() {
        // Assign Captain to Ship
        String shipName = "MV Ocean Pioneer";
        String shipId = "IMO-9812734";
        UserDao.assignCaptainShip(CAPTAIN_UID, shipId, shipName, "9812734", "Port Authority (" + VIEWING_PORT + ")");

        List<Map<String, Object>> captains = UserDao.getPersonnelForPort(VIEWING_PORT, "Captain");
        int count = 0;
        Map<String, Object> updatedCap = null;
        for (Map<String, Object> c : captains) {
            if (CAPTAIN_UID.equals(c.get("uid"))) {
                count++;
                updatedCap = c;
            }
        }

        assertEquals(1, count, "Captain must maintain a single profile post-assignment");
        assertNotNull(updatedCap);
        assertEquals(shipName, updatedCap.get("assignedShip"));

        // Assign Customs Officer a duty
        String duty = "Cargo Inspection";
        UserDao.assignOfficerDuty(OFFICER_UID, duty, "Active", VIEWING_PORT,
                "Port Authority (" + VIEWING_PORT + ")", "HIGH", "Today 18:00");

        List<Map<String, Object>> officers = UserDao.getPersonnelForPort(VIEWING_PORT, "Officer");
        int offCount = 0;
        Map<String, Object> updatedOff = null;
        for (Map<String, Object> o : officers) {
            if (OFFICER_UID.equals(o.get("uid"))) {
                offCount++;
                updatedOff = o;
            }
        }

        assertEquals(1, offCount, "Officer must maintain a single profile post-duty assignment");
        assertNotNull(updatedOff);
        assertEquals(duty, updatedOff.get("duty"));
    }

    @Test
    public void testDutyAssignmentFlowWithShipAndCaptainLinking() throws Exception {
        String shipName = "MV Ocean Pioneer";
        String shipId = "IMO-9812734";
        String dutyId = "DUTY-TEST-" + System.currentTimeMillis();

        // 1. Initial State: Captain assigned Ship, status is "Pending Assignment"
        Map<String, Object> initialCap = userDao.getCachedUserProfile(CAPTAIN_UID, CAPTAIN_EMAIL);
        assertNotNull(initialCap);
        initialCap.put("assignmentStatus", "Pending Assignment");
        UserDao.assignCaptainShip(CAPTAIN_UID, shipId, shipName, "9812734", "Port Authority (" + VIEWING_PORT + ")");

        // 2. Port Authority assigns duty to existing Customs Officer for this Ship/Captain operation
        String dutyType = "Customs Clearance";
        UserDao.assignOfficerDuty(
                OFFICER_UID,
                dutyType,
                "Assigned",
                VIEWING_PORT,
                "Port Authority (" + VIEWING_PORT + ")",
                "HIGH",
                "Today 18:00",
                shipId,
                shipName,
                CAPTAIN_UID,
                dutyId
        );

        // 3. Verify Customs Officer profile retrieved by Firebase UID contains assigned duty and ship
        Map<String, Object> officerProfile = userDao.getCachedUserProfile(OFFICER_UID, OFFICER_EMAIL);
        assertNotNull(officerProfile, "Customs Officer profile must exist for UID");
        assertEquals(OFFICER_UID, officerProfile.get("uid"), "Must preserve existing Firebase UID");
        assertEquals(dutyType, officerProfile.get("duty"), "Duty type must match assignment");
        assertEquals(shipName, officerProfile.get("assignedShip"), "Assigned ship must match");
        assertEquals("Assigned", officerProfile.get("assignmentStatus"), "Duty status must be Assigned");
        assertEquals(CAPTAIN_UID, officerProfile.get("captainUid"), "Captain UID must be linked");

        // 4. Verify Captain profile is updated (status no longer remains "Pending Assignment")
        Map<String, Object> capProfile = userDao.getCachedUserProfile(CAPTAIN_UID, CAPTAIN_EMAIL);
        assertNotNull(capProfile, "Captain profile must exist");
        assertEquals("Assigned", capProfile.get("assignmentStatus"), "Captain status must no longer be Pending Assignment");
        assertEquals("Officer Ananya Roy", capProfile.get("assignedOfficer"), "Captain must show assigned Customs Officer");

        // 5. Simulate Customs Officer completing duty from dashboard
        boolean completed = UserDao.completeOfficerDuty(OFFICER_UID, "Officer Ananya Roy", "Inspection completed without discrepancy").get();
        assertEquals(true, completed, "Duty completion must succeed");

        // 6. Verify Officer profile post-completion
        Map<String, Object> completedOfficer = userDao.getCachedUserProfile(OFFICER_UID, OFFICER_EMAIL);
        assertNotNull(completedOfficer);
        assertEquals("Standby / Available", completedOfficer.get("duty"));
        assertEquals("Available", completedOfficer.get("assignmentStatus"));
        assertEquals(dutyType, completedOfficer.get("lastCompletedDuty"));
        assertEquals(shipName, completedOfficer.get("lastCompletedVessel"));
    }
}
