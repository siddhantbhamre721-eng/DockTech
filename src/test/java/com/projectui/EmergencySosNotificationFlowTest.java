package com.projectui;

import com.projectui.dao.NotificationDao;
import com.projectui.model.Notification;
import com.projectui.service.SosEmergencyService;
import com.projectui.service.SosEmergencyService.SosAlert;
import com.projectui.view.ShipCaptain.CaptainSessionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class EmergencySosNotificationFlowTest {

    @Test
    public void testShipCaptainSosToPortAuthorityNotificationFlow() {
        // 1. Setup change listener
        AtomicBoolean listenerTriggered = new AtomicBoolean(false);
        SosEmergencyService.addChangeListener(() -> listenerTriggered.set(true));

        // 2. Transmit SOS from Ship Captain
        Map<String, Object> sosRec = CaptainSessionService.sendEmergencySOS(
                "CRITICAL (MAYDAY)",
                "Engine & Main Propulsion Failure",
                "Total black out in auxiliary engine generator #2. Drifting at 12 knots."
        );

        Assertions.assertNotNull(sosRec, "SOS record must be created");
        String incidentId = String.valueOf(sosRec.get("incidentId"));
        Assertions.assertTrue(incidentId.startsWith("SOS-"), "Incident ID must start with SOS-");
        Assertions.assertEquals("TRANSMITTED", sosRec.get("status"), "Initial status must be TRANSMITTED");

        // 3. Verify SosEmergencyService received and cached the alert
        SosAlert alert = SosEmergencyService.getAlert(incidentId);
        Assertions.assertNotNull(alert, "SosEmergencyService must have alert for " + incidentId);
        Assertions.assertEquals("Engine & Main Propulsion Failure", alert.getCategory());
        Assertions.assertEquals("CRITICAL (MAYDAY)", alert.getSeverity());
        Assertions.assertNotNull(alert.getLocation());
        Assertions.assertNotNull(alert.getSpeed());
        Assertions.assertNotNull(alert.getHeading());
        Assertions.assertNotNull(alert.getVesselName());
        Assertions.assertNotNull(alert.getCaptainName());

        // 4. Verify unread emergency count
        Assertions.assertTrue(SosEmergencyService.getUnreadEmergencyCount() >= 1, "Unread emergency count must be at least 1");

        // 5. Verify Port Authority NotificationDao has the SOS notification
        NotificationDao notifDao = new NotificationDao();
        List<Notification> portNotifs = notifDao.getNotificationsForUser("PORT_AUTHORITY_GLOBAL");
        boolean foundNotif = false;
        for (Notification n : portNotifs) {
            if (n != null && n.getId() != null && n.getId().contains(incidentId)) {
                foundNotif = true;
                Assertions.assertTrue(n.getTitle().contains("EMERGENCY SOS RECEIVED"));
                Assertions.assertEquals("EMERGENCY_SOS", n.getType());
                break;
            }
        }
        Assertions.assertTrue(foundNotif, "Port Authority global notifications must contain the SOS alert");

        // 6. Test Port Authority Acknowledgment
        SosEmergencyService.acknowledgeAlert(incidentId, "Cochin Port Authority Duty Officer");

        SosAlert ackAlert = SosEmergencyService.getAlert(incidentId);
        Assertions.assertNotNull(ackAlert);
        Assertions.assertEquals("ACKNOWLEDGED", ackAlert.getStatus(), "Status must be updated to ACKNOWLEDGED");
        Assertions.assertEquals("Cochin Port Authority Duty Officer", ackAlert.getAcknowledgedBy());
        Assertions.assertNotNull(ackAlert.getAcknowledgedAt());

        // 7. Verify status synchronization with Ship Captain
        Map<String, Object> captainRec = CaptainSessionService.getLatestSosRecord();
        Assertions.assertNotNull(captainRec);
        Assertions.assertEquals("ACKNOWLEDGED", captainRec.get("status"), "Ship Captain must see synchronized ACKNOWLEDGED status");
    }

    @Test
    public void testMultipleSosAlertsSupport() {
        SosAlert alert1 = new SosAlert();
        alert1.setIncidentId("SOS-TEST-ALPHA");
        alert1.setVesselName("MV PACIFIC VOYAGER");
        alert1.setCaptainName("Captain S. Verma");
        alert1.setSeverity("CRITICAL (MAYDAY)");
        alert1.setCategory("Severe Hull Breach / Ingress of Water");
        alert1.setLocation("15.2345° N, 73.1234° E");
        alert1.setSpeed("14.2 Knots");
        alert1.setHeading("192.0°");
        alert1.setPort("Port of Goa");
        alert1.setDestination("Port of Colombo");
        alert1.setTimestamp("30 Aug 2026, 23:45:00");
        alert1.setStatus("TRANSMITTED");
        alert1.setCreatedAtEpoch(System.currentTimeMillis() - 1000);

        SosAlert alert2 = new SosAlert();
        alert2.setIncidentId("SOS-TEST-BETA");
        alert2.setVesselName("MV ARABIAN STAR");
        alert2.setCaptainName("Captain R. Nair");
        alert2.setSeverity("URGENT (PAN-PAN)");
        alert2.setCategory("Medical Emergency / Critical Evacuation");
        alert2.setLocation("09.9654° N, 76.2134° E");
        alert2.setSpeed("10.5 Knots");
        alert2.setHeading("085.0°");
        alert2.setPort("Cochin Port");
        alert2.setDestination("Port of Mumbai");
        alert2.setTimestamp("30 Aug 2026, 23:50:00");
        alert2.setStatus("TRANSMITTED");
        alert2.setCreatedAtEpoch(System.currentTimeMillis());

        SosEmergencyService.registerSosAlert(alert1);
        SosEmergencyService.registerSosAlert(alert2);

        Assertions.assertNotNull(SosEmergencyService.getAlert("SOS-TEST-ALPHA"));
        Assertions.assertNotNull(SosEmergencyService.getAlert("SOS-TEST-BETA"));

        List<SosAlert> all = SosEmergencyService.getAllAlerts();
        Assertions.assertTrue(all.size() >= 2);
    }

    @Test
    public void testImporterSosFilteringOnlyAssignedShipCargo() {
        String importerUid = "IMP-UID-" + System.currentTimeMillis();
        String importerEmail = "importer." + System.currentTimeMillis() + "@test.com";

        Map<String, Object> importerProfile = Map.of(
                "uid", importerUid,
                "email", importerEmail,
                "role", "Importer",
                "name", "Test Importer Corp"
        );

        // 1. Assign cargo to this Importer on ship "MV IMPORTER SHIP"
        com.projectui.dao.CargoDao cargoDao = new com.projectui.dao.CargoDao();
        String cargoId = "CGO-IMP-" + System.currentTimeMillis();
        Map<String, Object> cargoData = new java.util.HashMap<>();
        cargoData.put("cargoId", cargoId);
        cargoData.put("importerUid", importerUid);
        cargoData.put("importerEmail", importerEmail);
        cargoData.put("assignedShip", "MV IMPORTER SHIP");
        cargoData.put("status", "In Transit");
        cargoDao.saveCargo(cargoId, "Electronics", "Container", "10", "5000", "0", "Port of Mumbai", "Port of Goa", "Normal", "In Transit");
        // Update extra fields
        cargoDao.getAllCargos();
        try {
            java.lang.reflect.Field field = com.projectui.dao.CargoDao.class.getDeclaredField("localCargoStore");
            field.setAccessible(true);
            ((Map<String, Map<String, Object>>) field.get(null)).put(cargoId, cargoData);
        } catch (Exception ignored) {}

        // 2. Register two SOS alerts: one for "MV IMPORTER SHIP", one for unrelated "MV UNRELATED SHIP"
        SosAlert alert1 = new SosAlert();
        alert1.setIncidentId("SOS-IMP-TEST-1");
        alert1.setVesselName("MV IMPORTER SHIP");
        alert1.setCaptainName("Captain A");
        alert1.setSeverity("CRITICAL (MAYDAY)");
        alert1.setStatus("TRANSMITTED");
        alert1.setCreatedAtEpoch(System.currentTimeMillis());

        SosAlert alert2 = new SosAlert();
        alert2.setIncidentId("SOS-IMP-TEST-2");
        alert2.setVesselName("MV UNRELATED SHIP");
        alert2.setCaptainName("Captain B");
        alert2.setSeverity("CRITICAL (MAYDAY)");
        alert2.setStatus("TRANSMITTED");
        alert2.setCreatedAtEpoch(System.currentTimeMillis() - 100);

        SosEmergencyService.registerSosAlert(alert1);
        SosEmergencyService.registerSosAlert(alert2);

        // 3. Query SOS alerts for Importer A -> must see ONLY alert1
        List<SosAlert> impAlerts = SosEmergencyService.getAlertsForRoleAndUser("Importer", importerProfile);
        Assertions.assertEquals(1, impAlerts.size(), "Importer must see only 1 SOS alert for their assigned ship");
        Assertions.assertEquals("SOS-IMP-TEST-1", impAlerts.get(0).getIncidentId());
        Assertions.assertEquals("MV IMPORTER SHIP", impAlerts.get(0).getVesselName());

        // 4. Query SOS alerts for an unrelated Importer -> must see 0 alerts
        Map<String, Object> otherImporter = Map.of(
                "uid", "OTHER-IMP-UID",
                "email", "other@importer.com",
                "role", "Importer"
        );
        List<SosAlert> otherAlerts = SosEmergencyService.getAlertsForRoleAndUser("Importer", otherImporter);
        Assertions.assertTrue(otherAlerts.isEmpty(), "Unrelated Importer must not see any SOS alerts");
    }

    @Test
    public void testExporterSosFilteringOnlyAssignedShipCargo() {
        String exporterUid = "EXP-UID-" + System.currentTimeMillis();
        String exporterEmail = "exporter." + System.currentTimeMillis() + "@test.com";

        Map<String, Object> exporterProfile = Map.of(
                "uid", exporterUid,
                "email", exporterEmail,
                "role", "Exporter",
                "name", "Test Exporter Corp"
        );

        // 1. Assign cargo to this Exporter on ship "MV EXPORTER CARRIER"
        com.projectui.dao.CargoDao cargoDao = new com.projectui.dao.CargoDao();
        String cargoId = "CGO-EXP-" + System.currentTimeMillis();
        Map<String, Object> cargoData = new java.util.HashMap<>();
        cargoData.put("cargoId", cargoId);
        cargoData.put("ownerUid", exporterUid);
        cargoData.put("ownerEmail", exporterEmail);
        cargoData.put("assignedShip", "MV EXPORTER CARRIER");
        cargoData.put("status", "Loaded");
        try {
            java.lang.reflect.Field field = com.projectui.dao.CargoDao.class.getDeclaredField("localCargoStore");
            field.setAccessible(true);
            ((Map<String, Map<String, Object>>) field.get(null)).put(cargoId, cargoData);
        } catch (Exception ignored) {}

        // 2. Register SOS alerts
        SosAlert alert1 = new SosAlert();
        alert1.setIncidentId("SOS-EXP-TEST-1");
        alert1.setVesselName("MV EXPORTER CARRIER");
        alert1.setCaptainName("Captain X");
        alert1.setSeverity("URGENT (PAN-PAN)");
        alert1.setStatus("TRANSMITTED");
        alert1.setCreatedAtEpoch(System.currentTimeMillis());

        SosAlert alert2 = new SosAlert();
        alert2.setIncidentId("SOS-EXP-TEST-2");
        alert2.setVesselName("MV UNRELATED VESSEL 99");
        alert2.setCaptainName("Captain Y");
        alert2.setSeverity("CRITICAL (MAYDAY)");
        alert2.setStatus("TRANSMITTED");
        alert2.setCreatedAtEpoch(System.currentTimeMillis() - 100);

        SosEmergencyService.registerSosAlert(alert1);
        SosEmergencyService.registerSosAlert(alert2);

        // 3. Query SOS alerts for Exporter -> must see ONLY alert1
        List<SosAlert> expAlerts = SosEmergencyService.getAlertsForRoleAndUser("Exporter", exporterProfile);
        Assertions.assertEquals(1, expAlerts.size(), "Exporter must see only 1 SOS alert for their assigned ship");
        Assertions.assertEquals("SOS-EXP-TEST-1", expAlerts.get(0).getIncidentId());
        Assertions.assertEquals("MV EXPORTER CARRIER", expAlerts.get(0).getVesselName());

        // 4. Query SOS alerts for an unrelated Exporter -> must see 0 alerts
        Map<String, Object> otherExporter = Map.of(
                "uid", "OTHER-EXP-UID",
                "email", "other@exporter.com",
                "role", "Exporter"
        );
        List<SosAlert> otherAlerts = SosEmergencyService.getAlertsForRoleAndUser("Exporter", otherExporter);
        Assertions.assertTrue(otherAlerts.isEmpty(), "Unrelated Exporter must not see any SOS alerts");
    }

    @Test
    public void testCreateAccountAndPublicRoleNoSosAlerts() {
        List<SosAlert> createAccAlerts = SosEmergencyService.getAlertsForRoleAndUser("CreateAccount", Map.of("role", "CreateAccount"));
        Assertions.assertTrue(createAccAlerts.isEmpty(), "CreateAccount page must never receive SOS alerts");

        List<SosAlert> publicAlerts = SosEmergencyService.getAlertsForRoleAndUser("Public", Map.of("role", "Public"));
        Assertions.assertTrue(publicAlerts.isEmpty(), "Public role must never receive SOS alerts");
    }
}
