package com.projectui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.projectui.dao.UserDao;

public class CustomOfficerDataSyncTest {

    private final String GOA_OFF_UID = "UID_GOA_OFF_" + System.currentTimeMillis();
    private final String GOA_OFF_EMAIL = "goa_officer_" + System.currentTimeMillis() + "@customs.gov";

    private final String KOLKATA_OFF_UID = "UID_CCU_OFF_" + System.currentTimeMillis();
    private final String KOLKATA_OFF_EMAIL = "kolkata_officer_" + System.currentTimeMillis() + "@customs.gov";

    private final String HAZIRA_OFF_UID = "UID_HAZ_OFF_" + System.currentTimeMillis();
    private final String HAZIRA_OFF_EMAIL = "hazira_officer_" + System.currentTimeMillis() + "@customs.gov";

    private final UserDao userDao = new UserDao();

    @BeforeEach
    public void setUp() throws Exception {
        // Register Officer 1 assigned to PORT OF GOA
        Map<String, Object> goaExtra = new HashMap<>();
        goaExtra.put("uid", GOA_OFF_UID);
        goaExtra.put("email", GOA_OFF_EMAIL);
        goaExtra.put("name", "Officer Rajesh Goa");
        goaExtra.put("role", "Customs Officer");
        goaExtra.put("assignedPort", "PORT OF GOA");
        goaExtra.put("port", "PORT OF GOA");
        goaExtra.put("company", "Customs Border Control");
        goaExtra.put("status", "Active");
        goaExtra.put("duty", "Cargo Inspection");
        goaExtra.put("assignmentStatus", "Assigned");
        userDao.saveUserExtended(GOA_OFF_UID, GOA_OFF_EMAIL, "Officer Rajesh Goa",
                "Customs Border Control", "+91 9811001100", "Customs Officer", "Active", goaExtra).get();

        // Register Officer 2 assigned to PORT OF KOLKATA
        Map<String, Object> ccuExtra = new HashMap<>();
        ccuExtra.put("uid", KOLKATA_OFF_UID);
        ccuExtra.put("email", KOLKATA_OFF_EMAIL);
        ccuExtra.put("name", "Officer Sourav Kolkata");
        ccuExtra.put("role", "Customs Officer");
        ccuExtra.put("assignedPort", "PORT OF KOLKATA");
        ccuExtra.put("port", "PORT OF KOLKATA");
        ccuExtra.put("company", "Cargo Inspection");
        ccuExtra.put("status", "Active");
        ccuExtra.put("duty", "Document Verification");
        ccuExtra.put("assignmentStatus", "Available");
        userDao.saveUserExtended(KOLKATA_OFF_UID, KOLKATA_OFF_EMAIL, "Officer Sourav Kolkata",
                "Cargo Inspection", "+91 9811002200", "Customs Officer", "Active", ccuExtra).get();

        // Register Officer 3 assigned to HAZIRA PORT
        Map<String, Object> hazExtra = new HashMap<>();
        hazExtra.put("uid", HAZIRA_OFF_UID);
        hazExtra.put("email", HAZIRA_OFF_EMAIL);
        hazExtra.put("name", "Officer Amit Hazira");
        hazExtra.put("role", "Customs Officer");
        hazExtra.put("assignedPort", "HAZIRA PORT");
        hazExtra.put("port", "HAZIRA PORT");
        hazExtra.put("company", "Maritime Compliance");
        hazExtra.put("status", "Active");
        hazExtra.put("duty", "Tariff Enforcement");
        hazExtra.put("assignmentStatus", "Assigned");
        userDao.saveUserExtended(HAZIRA_OFF_UID, HAZIRA_OFF_EMAIL, "Officer Amit Hazira",
                "Maritime Compliance", "+91 9811003300", "Customs Officer", "Active", hazExtra).get();
    }

    @AfterEach
    public void tearDown() {
        userDao.deleteUserAccount(GOA_OFF_UID, GOA_OFF_EMAIL).join();
        userDao.deleteUserAccount(KOLKATA_OFF_UID, KOLKATA_OFF_EMAIL).join();
        userDao.deleteUserAccount(HAZIRA_OFF_UID, HAZIRA_OFF_EMAIL).join();
    }

    @Test
    public void testAllRegisteredCustomOfficersFetchedAcrossAllPorts() {
        // ManageCustomOfficerPage queries with null port to load all officers across all ports
        List<Map<String, Object>> allOfficers = UserDao.getPersonnelForPort(null, "Officer");
        assertNotNull(allOfficers, "Personnel list must not be null");

        Map<String, Object> goaOff = null;
        Map<String, Object> ccuOff = null;
        Map<String, Object> hazOff = null;

        for (Map<String, Object> o : allOfficers) {
            String uid = (String) o.get("uid");
            if (GOA_OFF_UID.equals(uid)) goaOff = o;
            if (KOLKATA_OFF_UID.equals(uid)) ccuOff = o;
            if (HAZIRA_OFF_UID.equals(uid)) hazOff = o;
        }

        assertNotNull(goaOff, "Officer assigned to PORT OF GOA must appear in officers list");
        assertEquals("PORT OF GOA", goaOff.get("assignedPort"), "Port/Jurisdiction must be PORT OF GOA");

        assertNotNull(ccuOff, "Officer assigned to PORT OF KOLKATA must appear in officers list");
        assertEquals("PORT OF KOLKATA", ccuOff.get("assignedPort"), "Port/Jurisdiction must be PORT OF KOLKATA");

        assertNotNull(hazOff, "Officer assigned to HAZIRA PORT must appear in officers list");
        assertEquals("HAZIRA PORT", hazOff.get("assignedPort"), "Port/Jurisdiction must be HAZIRA PORT");
    }

    @Test
    public void testDutyAssignmentPreservesOfficerPort() {
        // Assign duty to Kolkata officer
        UserDao.assignOfficerDuty(KOLKATA_OFF_UID, "Container Inspection", "Assigned", "PORT OF KOLKATA",
                "Port Authority (PORT OF KOLKATA)", "HIGH", "Today 18:00", "IMO-12345", "MV Bengal Star", "", "DUTY-TEST-1");

        List<Map<String, Object>> allOfficers = UserDao.getPersonnelForPort(null, "Officer");
        Map<String, Object> found = null;
        for (Map<String, Object> o : allOfficers) {
            if (KOLKATA_OFF_UID.equals(o.get("uid"))) {
                found = o;
                break;
            }
        }

        assertNotNull(found);
        assertEquals("Container Inspection", found.get("duty"));
        assertEquals("Assigned", found.get("assignmentStatus"));
        assertEquals("PORT OF KOLKATA", found.get("assignedPort"), "Port must be preserved as PORT OF KOLKATA");
    }
}
