package com.projectui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.projectui.service.SosEmergencyService;
import com.projectui.service.SosEmergencyService.SosAlert;
import com.projectui.view.ShipCaptain.CaptainSessionService;
import com.projectui.view.login.LoginPage;

public class SosNotificationFlowTest {

    @Test
    public void testSosTransmissionAndPortAuthorityNotificationFlow() {
        // 1. Simulate logged-in Captain
        Map<String, Object> captainUser = new HashMap<>();
        captainUser.put("uid", "CAP_TEST_001");
        captainUser.put("name", "Captain Vikram Rathore");
        captainUser.put("email", "captain.vikram@docktech.com");
        captainUser.put("role", "Ship Captain");
        captainUser.put("assignedShip", "MV OCEAN EXPLORER");
        captainUser.put("assignedPort", "PORT OF COCHIN");
        LoginPage.loggedInUser = captainUser;

        // 2. Transmit Emergency SOS
        String priority = "CRITICAL (MAYDAY)";
        String emergencyType = "Engine Room Fire / Loss of Power";
        String details = "Smoke detected in generator room. Auxiliary power online, requesting immediate tug dispatch.";

        Map<String, Object> sosRecord = CaptainSessionService.sendEmergencySOS(priority, emergencyType, details);
        assertNotNull(sosRecord, "SOS record must be created");
        String incidentId = (String) sosRecord.get("incidentId");
        assertNotNull(incidentId, "Incident ID must be generated");

        // 3. Verify SOS alert registered in SosEmergencyService
        SosAlert alert = SosEmergencyService.getAlert(incidentId);
        assertNotNull(alert, "Alert must be registered in SosEmergencyService cache");
        assertEquals("MV OCEAN EXPLORER", alert.getVesselName());
        assertEquals("Captain Vikram Rathore", alert.getCaptainName());
        assertEquals("CRITICAL (MAYDAY)", alert.getSeverity());
        assertEquals("Engine Room Fire / Loss of Power", alert.getCategory());
        assertEquals("TRANSMITTED", alert.getStatus());
        assertNotNull(alert.getTimestamp());

        // 4. Simulate Port Authority login for PORT OF COCHIN
        Map<String, Object> portAuthorityUser = new HashMap<>();
        portAuthorityUser.put("uid", "PA_COCHIN_001");
        portAuthorityUser.put("name", "Cochin Port Authority Operations");
        portAuthorityUser.put("role", "Port Authority");
        portAuthorityUser.put("assignedPort", "PORT OF COCHIN");

        List<SosAlert> paAlerts = SosEmergencyService.getAlertsForRoleAndUser("Port Authority", portAuthorityUser);
        assertFalse(paAlerts.isEmpty(), "Port Authority of Cochin must receive the SOS alert");

        boolean found = false;
        for (SosAlert a : paAlerts) {
            if (incidentId.equals(a.getIncidentId())) {
                found = true;
                assertEquals("MV OCEAN EXPLORER", a.getVesselName());
                assertEquals("CRITICAL (MAYDAY)", a.getSeverity());
                assertEquals("Engine Room Fire / Loss of Power", a.getCategory());
                break;
            }
        }
        assertTrue(found, "The transmitted SOS alert must appear in Port Authority notifications list");

        // 5. Test unread count calculation
        int unreadCount = SosEmergencyService.getUnreadEmergencyCountForUser("Port Authority", portAuthorityUser);
        assertTrue(unreadCount >= 1, "Unread SOS count for Port Authority must be at least 1");

        // 6. Test Acknowledge functionality
        SosEmergencyService.acknowledgeAlert(incidentId, "Cochin Port Operations Center");
        SosAlert ackedAlert = SosEmergencyService.getAlert(incidentId);
        assertNotNull(ackedAlert);
        assertEquals("ACKNOWLEDGED", ackedAlert.getStatus());
        assertEquals("Cochin Port Operations Center", ackedAlert.getAcknowledgedBy());
        assertTrue(ackedAlert.isReadByPortAuthority());

        // 7. Verify no duplicate alerts on re-query
        List<SosAlert> paAlertsAfterAck = SosEmergencyService.getAlertsForRoleAndUser("Port Authority", portAuthorityUser);
        long matchingCount = paAlertsAfterAck.stream().filter(a -> incidentId.equals(a.getIncidentId())).count();
        assertEquals(1, matchingCount, "Should have exactly 1 record for this incident, no duplicates");
    }
}
