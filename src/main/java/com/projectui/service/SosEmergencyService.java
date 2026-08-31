package com.projectui.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.projectui.config.FirebaseConfig;
import com.projectui.dao.Dao;
import com.projectui.dao.NotificationDao;
import com.projectui.model.Notification;
import com.projectui.view.ShipCaptain.CaptainSessionService;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class SosEmergencyService extends Dao {

    public static class SosAlert {
        private String incidentId;
        private String vesselName;
        private String vesselId;
        private String captainName;
        private String captainUid;
        private String imo;
        private String mmsi;
        private String severity;
        private String category;
        private String description;
        private String location;
        private String latitude;
        private String longitude;
        private String speed;
        private String heading;
        private String navigationStatus;
        private String port;
        private String destination;
        private String voyage;
        private String timestamp;
        private String status; // TRANSMITTED, RECEIVED, ACKNOWLEDGED
        private String acknowledgedBy;
        private String acknowledgedAt;
        private boolean readByPortAuthority;
        private long createdAtEpoch;

        public SosAlert() {
        }

        public String getIncidentId() {
            return incidentId;
        }

        public void setIncidentId(String incidentId) {
            this.incidentId = incidentId;
        }

        public String getVesselName() {
            return vesselName;
        }

        public void setVesselName(String vesselName) {
            this.vesselName = vesselName;
        }

        public String getVesselId() {
            return vesselId;
        }

        public void setVesselId(String vesselId) {
            this.vesselId = vesselId;
        }

        public String getCaptainName() {
            return captainName;
        }

        public void setCaptainName(String captainName) {
            this.captainName = captainName;
        }

        public String getCaptainUid() {
            return captainUid;
        }

        public void setCaptainUid(String captainUid) {
            this.captainUid = captainUid;
        }

        public String getImo() {
            return imo;
        }

        public void setImo(String imo) {
            this.imo = imo;
        }

        public String getMmsi() {
            return mmsi;
        }

        public void setMmsi(String mmsi) {
            this.mmsi = mmsi;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getSpeed() {
            return speed;
        }

        public void setSpeed(String speed) {
            this.speed = speed;
        }

        public String getHeading() {
            return heading;
        }

        public void setHeading(String heading) {
            this.heading = heading;
        }

        public String getNavigationStatus() {
            return navigationStatus;
        }

        public void setNavigationStatus(String navigationStatus) {
            this.navigationStatus = navigationStatus;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getPortAssigned() {
            return port;
        }

        public void setPortAssigned(String portAssigned) {
            this.port = portAssigned;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public String getVoyage() {
            return voyage;
        }

        public void setVoyage(String voyage) {
            this.voyage = voyage;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getAcknowledgedBy() {
            return acknowledgedBy;
        }

        public void setAcknowledgedBy(String acknowledgedBy) {
            this.acknowledgedBy = acknowledgedBy;
        }

        public String getAcknowledgedAt() {
            return acknowledgedAt;
        }

        public void setAcknowledgedAt(String acknowledgedAt) {
            this.acknowledgedAt = acknowledgedAt;
        }

        public boolean isReadByPortAuthority() {
            return readByPortAuthority;
        }

        public void setReadByPortAuthority(boolean readByPortAuthority) {
            this.readByPortAuthority = readByPortAuthority;
        }

        public long getCreatedAtEpoch() {
            return createdAtEpoch;
        }

        public void setCreatedAtEpoch(long createdAtEpoch) {
            this.createdAtEpoch = createdAtEpoch;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("incidentId", incidentId);
            map.put("vessel", vesselName);
            map.put("vesselName", vesselName);
            map.put("vesselId", vesselId);
            map.put("imo", imo);
            map.put("mmsi", mmsi);
            map.put("captain", captainName);
            map.put("captainName", captainName);
            map.put("captainUid", captainUid);
            map.put("priority", severity);
            map.put("severity", severity);
            map.put("emergencyType", category);
            map.put("category", category);
            map.put("details", description);
            map.put("description", description);
            map.put("location", location);
            map.put("latitude", latitude);
            map.put("longitude", longitude);
            map.put("speed", speed);
            map.put("heading", heading);
            map.put("navigationStatus", navigationStatus);
            map.put("port", port);
            map.put("destination", destination);
            map.put("voyage", voyage);
            map.put("timestamp", timestamp);
            map.put("status", status);
            map.put("acknowledgedBy", acknowledgedBy);
            map.put("acknowledgedAt", acknowledgedAt);
            map.put("readByPortAuthority", readByPortAuthority);
            map.put("createdAtEpoch", createdAtEpoch);
            return map;
        }

        public static SosAlert fromMap(Map<String, Object> map) {
            if (map == null)
                return null;
            SosAlert a = new SosAlert();
            a.incidentId = str(map.get("incidentId"), "SOS-0000");
            a.vesselName = str(map.get("vesselName"), str(map.get("vessel"), "Vessel"));
            a.vesselId = str(map.get("vesselId"), "SHP-0000");
            a.captainName = str(map.get("captainName"), str(map.get("captain"), "Captain"));
            a.captainUid = str(map.get("captainUid"), "");
            a.imo = str(map.get("imo"), "9234567");
            a.mmsi = str(map.get("mmsi"), "211280000");
            a.severity = str(map.get("severity"), str(map.get("priority"), "CRITICAL"));
            a.category = str(map.get("category"), str(map.get("emergencyType"), "General Distress"));
            a.description = str(map.get("description"), str(map.get("details"), "Emergency Distress Beacon"));
            a.location = str(map.get("location"), "18.9439° N, 72.8354° E");
            a.latitude = str(map.get("latitude"), "18.9439° N");
            a.longitude = str(map.get("longitude"), "72.8354° E");
            a.speed = str(map.get("speed"), "12.0 Knots");
            a.heading = str(map.get("heading"), "180.0°");
            a.navigationStatus = str(map.get("navigationStatus"), "Under Way Using Engine");
            a.port = str(map.get("port"), "Port Authority");
            a.destination = str(map.get("destination"), "Port Authority Anchorage");
            a.voyage = str(map.get("voyage"), "VOY-2026-01");
            a.timestamp = str(map.get("timestamp"),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss")));
            a.status = str(map.get("status"), "TRANSMITTED");
            a.acknowledgedBy = str(map.get("acknowledgedBy"), "");
            a.acknowledgedAt = str(map.get("acknowledgedAt"), "");
            Object r = map.get("readByPortAuthority");
            a.readByPortAuthority = r instanceof Boolean ? (Boolean) r : false;
            Object ep = map.get("createdAtEpoch");
            a.createdAtEpoch = ep instanceof Number ? ((Number) ep).longValue() : System.currentTimeMillis();
            return a;
        }

        private static String str(Object o, String fallback) {
            return (o != null && !o.toString().isBlank()) ? o.toString().trim() : fallback;
        }
    }

    private static final Map<String, SosAlert> alertsCache = new ConcurrentHashMap<>();
    private static final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();
    private static final NotificationDao notificationDao = new NotificationDao();
    private static boolean firestoreSynced = false;

    static {
        syncFromFirestoreAsync();
    }

    private static Firestore getDb() {
        return FirebaseConfig.getFirestore();
    }

    public static void addChangeListener(Runnable listener) {
        if (listener != null && !changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }

    public static void removeChangeListener(Runnable listener) {
        if (listener != null) {
            changeListeners.remove(listener);
        }
    }

    private static void notifyListeners() {
        for (Runnable r : changeListeners) {
            try {
                if (Platform.isFxApplicationThread()) {
                    r.run();
                } else {
                    Platform.runLater(r);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static void registerSosAlert(SosAlert alert) {
        if (alert == null || alert.getIncidentId() == null)
            return;
        if (alert.getCreatedAtEpoch() == 0) {
            alert.setCreatedAtEpoch(System.currentTimeMillis());
        }
        if (alert.getStatus() == null || alert.getStatus().isBlank()) {
            alert.setStatus("TRANSMITTED");
        }

        alertsCache.put(alert.getIncidentId(), alert);

        // Update Captain session service
        CaptainSessionService.latestSosRecord = alert.toMap();

        // Create a Port Authority notification in NotificationDao
        try {
            Notification n = new Notification();
            n.setId("NOTIF-SOS-" + alert.getIncidentId());
            n.setTitle("🔴 EMERGENCY SOS RECEIVED: " + alert.getVesselName() + " [" + alert.getSeverity() + "]");
            n.setMessage("Emergency SOS received from " + alert.getVesselName() + ".\n"
                    + alert.getSeverity() + " — " + alert.getCategory() + ".\n"
                    + "Location: " + alert.getLocation() + " (Speed: " + alert.getSpeed() + ", Heading: "
                    + alert.getHeading() + ")\n"
                    + "Received: " + alert.getTimestamp());
            n.setType("EMERGENCY_SOS");
            n.setUserId("PORT_AUTHORITY_GLOBAL");
            n.setVesselName(alert.getVesselName());
            n.setVesselId(alert.getVesselId());
            n.setPort(alert.getPort());
            n.setStatus(alert.getStatus());
            n.setPriority("CRITICAL");
            n.setAssignedBy(alert.getCaptainName());
            n.setRead(false);
            n.setCreatedAtEpoch(alert.getCreatedAtEpoch());
            n.setTimestamp(alert.getTimestamp());
            notificationDao.createNotification(n);
        } catch (Exception ignored) {
        }

        // Persist to Firestore
        java.util.concurrent.ForkJoinPool.commonPool().execute(() -> {
            try {
                Firestore db = getDb();
                if (db != null) {
                    db.collection("sos_alerts").document(alert.getIncidentId()).set(alert.toMap());
                    System.out.println("[SOS SERVICE] SOS alert persisted to Firestore: " + alert.getIncidentId() + " ("
                            + alert.getVesselName() + ")");
                }
            } catch (Exception ex) {
                System.err.println("[SOS SERVICE] Firestore save error: " + ex.getMessage());
            }
        });

        notifyListeners();
    }

    public static void acknowledgeAlert(String incidentId, String portAuthorityName) {
        if (incidentId == null)
            return;
        SosAlert alert = alertsCache.get(incidentId);
        if (alert != null) {
            alert.setStatus("ACKNOWLEDGED");
            alert.setAcknowledgedBy(portAuthorityName != null && !portAuthorityName.isBlank() ? portAuthorityName
                    : "Port Authority Operations");
            alert.setAcknowledgedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss")));
            alert.setReadByPortAuthority(true);

            // Update CaptainSessionService
            if (CaptainSessionService.latestSosRecord != null) {
                String curInc = String.valueOf(CaptainSessionService.latestSosRecord.get("incidentId"));
                if (incidentId.equalsIgnoreCase(curInc)) {
                    CaptainSessionService.latestSosRecord.put("status", "ACKNOWLEDGED");
                    CaptainSessionService.latestSosRecord.put("acknowledgedBy", alert.getAcknowledgedBy());
                    CaptainSessionService.latestSosRecord.put("acknowledgedAt", alert.getAcknowledgedAt());
                }
            }

            // Update Notification in NotificationDao
            notificationDao.markAsRead("NOTIF-SOS-" + incidentId);

            // Persist acknowledgment to Firestore
            java.util.concurrent.ForkJoinPool.commonPool().execute(() -> {
                try {
                    Firestore db = getDb();
                    if (db != null) {
                        Map<String, Object> update = new HashMap<>();
                        update.put("status", "ACKNOWLEDGED");
                        update.put("acknowledgedBy", alert.getAcknowledgedBy());
                        update.put("acknowledgedAt", alert.getAcknowledgedAt());
                        update.put("readByPortAuthority", true);
                        db.collection("sos_alerts").document(incidentId).set(update,
                                com.google.cloud.firestore.SetOptions.merge());
                        System.out.println("[SOS SERVICE] SOS alert acknowledged in Firestore: " + incidentId);
                    }
                } catch (Exception ex) {
                    System.err.println("[SOS SERVICE] Firestore acknowledge error: " + ex.getMessage());
                }
            });

            notifyListeners();
        }
    }

    public static List<SosAlert> getAllAlerts() {
        List<SosAlert> list = new ArrayList<>(alertsCache.values());
        list.sort((a, b) -> Long.compare(b.getCreatedAtEpoch(), a.getCreatedAtEpoch()));
        return list;
    }

    public static List<SosAlert> getActiveAlerts() {
        List<SosAlert> active = new ArrayList<>();
        for (SosAlert a : alertsCache.values()) {
            if (!"ACKNOWLEDGED".equalsIgnoreCase(a.getStatus()) || !a.isReadByPortAuthority()) {
                active.add(a);
            }
        }
        active.sort((a, b) -> Long.compare(b.getCreatedAtEpoch(), a.getCreatedAtEpoch()));
        return active;
    }

    public static int getUnreadEmergencyCount() {
        int count = 0;
        for (SosAlert a : alertsCache.values()) {
            if (!a.isReadByPortAuthority() || !"ACKNOWLEDGED".equalsIgnoreCase(a.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public static List<SosAlert> getAlertsForRoleAndUser(String role, Map<String, Object> userProfile) {
        if (role == null || role.isBlank() || "CreateAccount".equalsIgnoreCase(role) || "Public".equalsIgnoreCase(role)) {
            return Collections.emptyList();
        }

        String cleanRole = role.trim();

        // 1. Admin sees all alerts. Port Authority and Customs Officer see alerts associated with their assigned port.
        if ("Admin".equalsIgnoreCase(cleanRole)) {
            return getAllAlerts();
        }

        if ("Port Authority".equalsIgnoreCase(cleanRole) || "Custom Officer".equalsIgnoreCase(cleanRole) || "Customs Officer".equalsIgnoreCase(cleanRole)) {
            String userPort = "";
            if (userProfile != null) {
                if (userProfile.get("assignedPort") != null) userPort = userProfile.get("assignedPort").toString().trim();
                if (userPort.isEmpty() && userProfile.get("port") != null) userPort = userProfile.get("port").toString().trim();
            }
            if (userPort.isEmpty()) {
                return getAllAlerts();
            }
            String normUserPort = com.projectui.dao.UserDao.normalizePort(userPort).toLowerCase();
            List<SosAlert> filtered = new ArrayList<>();
            for (SosAlert a : getAllAlerts()) {
                if (a == null) continue;
                String alertPort = a.getPort() != null ? a.getPort().trim() : "";
                String normAlertPort = com.projectui.dao.UserDao.normalizePort(alertPort).toLowerCase();
                if (alertPort.isEmpty() || normAlertPort.contains(normUserPort) || normUserPort.contains(normAlertPort)) {
                    filtered.add(a);
                }
            }
            return filtered;
        }

        // 2. Ship Captain sees their own vessel's transmitted alerts
        if ("Ship Captain".equalsIgnoreCase(cleanRole) || "Captain".equalsIgnoreCase(cleanRole)) {
            String myShip = "";
            String myUid = "";
            if (userProfile != null) {
                if (userProfile.get("assignedShip") != null) myShip = userProfile.get("assignedShip").toString().trim().toLowerCase();
                if (userProfile.get("uid") != null) myUid = userProfile.get("uid").toString().trim().toLowerCase();
            }
            List<SosAlert> filtered = new ArrayList<>();
            for (SosAlert a : getAllAlerts()) {
                if (a == null) continue;
                String vName = a.getVesselName() != null ? a.getVesselName().trim().toLowerCase() : "";
                String capUid = a.getCaptainUid() != null ? a.getCaptainUid().trim().toLowerCase() : "";
                if ((!myShip.isEmpty() && vName.contains(myShip)) || (!myUid.isEmpty() && capUid.equals(myUid))) {
                    filtered.add(a);
                }
            }
            return filtered;
        }

        // 3. Importer: Filter strictly by Importer's assigned ships / cargo / shipments
        if ("Importer".equalsIgnoreCase(cleanRole)) {
            if (userProfile == null || userProfile.isEmpty()) {
                return Collections.emptyList();
            }

            java.util.Set<String> userShipNames = new java.util.HashSet<>();
            java.util.Set<String> userCargoIds = new java.util.HashSet<>();

            // Find all cargos matching this Importer
            com.projectui.dao.CargoDao cargoDao = new com.projectui.dao.CargoDao();
            for (Map<String, Object> c : cargoDao.getAllCargos()) {
                if (c != null && com.projectui.dao.CargoDao.isCargoAssignedToImporter(c, userProfile)) {
                    if (c.get("cargoId") != null) userCargoIds.add(c.get("cargoId").toString().trim().toLowerCase());
                    if (c.get("assignedShip") != null) userShipNames.add(c.get("assignedShip").toString().trim().toLowerCase());
                    if (c.get("vesselName") != null) userShipNames.add(c.get("vesselName").toString().trim().toLowerCase());
                    if (c.get("ship") != null) userShipNames.add(c.get("ship").toString().trim().toLowerCase());
                    if (c.get("shipName") != null) userShipNames.add(c.get("shipName").toString().trim().toLowerCase());
                }
            }

            // Find all container bookings matching this Importer
            com.projectui.dao.ContainerDao containerDao = new com.projectui.dao.ContainerDao();
            for (Map<String, Object> b : containerDao.getAllBookings()) {
                if (b != null) {
                    String cargoId = b.get("cargoId") != null ? b.get("cargoId").toString().trim().toLowerCase() : "";
                    boolean isMyCargo = !cargoId.isEmpty() && userCargoIds.contains(cargoId);
                    boolean isMyBooking = com.projectui.dao.CargoDao.isCargoAssignedToImporter(b, userProfile);
                    if (isMyCargo || isMyBooking) {
                        if (b.get("assignedShip") != null) userShipNames.add(b.get("assignedShip").toString().trim().toLowerCase());
                        if (b.get("vesselName") != null) userShipNames.add(b.get("vesselName").toString().trim().toLowerCase());
                        if (b.get("ship") != null) userShipNames.add(b.get("ship").toString().trim().toLowerCase());
                    }
                }
            }

            if (userShipNames.isEmpty()) {
                return Collections.emptyList();
            }

            List<SosAlert> filtered = new ArrayList<>();
            for (SosAlert a : getAllAlerts()) {
                if (a == null) continue;
                String vName = a.getVesselName() != null ? a.getVesselName().trim().toLowerCase() : "";
                String vId = a.getVesselId() != null ? a.getVesselId().trim().toLowerCase() : "";
                String imo = a.getImo() != null ? a.getImo().trim().toLowerCase() : "";
                String mmsi = a.getMmsi() != null ? a.getMmsi().trim().toLowerCase() : "";

                boolean matchesShip = false;
                for (String s : userShipNames) {
                    if (s.isEmpty()) continue;
                    if (vName.contains(s) || s.contains(vName) || vId.equalsIgnoreCase(s) || imo.equalsIgnoreCase(s) || mmsi.equalsIgnoreCase(s)) {
                        matchesShip = true;
                        break;
                    }
                }
                if (matchesShip) {
                    filtered.add(a);
                }
            }
            return filtered;
        }

        // 4. Exporter: Filter strictly by Exporter's assigned ships / cargo / shipments
        if ("Exporter".equalsIgnoreCase(cleanRole)) {
            if (userProfile == null || userProfile.isEmpty()) {
                return Collections.emptyList();
            }

            java.util.Set<String> userShipNames = new java.util.HashSet<>();
            java.util.Set<String> userCargoIds = new java.util.HashSet<>();

            // Find all cargos matching this Exporter
            com.projectui.dao.CargoDao cargoDao = new com.projectui.dao.CargoDao();
            for (Map<String, Object> c : cargoDao.getShipmentsForExporter(userProfile)) {
                if (c != null) {
                    if (c.get("cargoId") != null) userCargoIds.add(c.get("cargoId").toString().trim().toLowerCase());
                    if (c.get("assignedShip") != null) userShipNames.add(c.get("assignedShip").toString().trim().toLowerCase());
                    if (c.get("vesselName") != null) userShipNames.add(c.get("vesselName").toString().trim().toLowerCase());
                    if (c.get("ship") != null) userShipNames.add(c.get("ship").toString().trim().toLowerCase());
                    if (c.get("shipName") != null) userShipNames.add(c.get("shipName").toString().trim().toLowerCase());
                }
            }

            // Find all container bookings matching this Exporter
            com.projectui.dao.ContainerDao containerDao = new com.projectui.dao.ContainerDao();
            for (Map<String, Object> b : containerDao.getContainersForExporter(userProfile)) {
                if (b != null) {
                    if (b.get("cargoId") != null) userCargoIds.add(b.get("cargoId").toString().trim().toLowerCase());
                    if (b.get("assignedShip") != null) userShipNames.add(b.get("assignedShip").toString().trim().toLowerCase());
                    if (b.get("vesselName") != null) userShipNames.add(b.get("vesselName").toString().trim().toLowerCase());
                    if (b.get("ship") != null) userShipNames.add(b.get("ship").toString().trim().toLowerCase());
                }
            }

            if (userShipNames.isEmpty()) {
                return Collections.emptyList();
            }

            List<SosAlert> filtered = new ArrayList<>();
            for (SosAlert a : getAllAlerts()) {
                if (a == null) continue;
                String vName = a.getVesselName() != null ? a.getVesselName().trim().toLowerCase() : "";
                String vId = a.getVesselId() != null ? a.getVesselId().trim().toLowerCase() : "";
                String imo = a.getImo() != null ? a.getImo().trim().toLowerCase() : "";
                String mmsi = a.getMmsi() != null ? a.getMmsi().trim().toLowerCase() : "";

                boolean matchesShip = false;
                for (String s : userShipNames) {
                    if (s.isEmpty()) continue;
                    if (vName.contains(s) || s.contains(vName) || vId.equalsIgnoreCase(s) || imo.equalsIgnoreCase(s) || mmsi.equalsIgnoreCase(s)) {
                        matchesShip = true;
                        break;
                    }
                }
                if (matchesShip) {
                    filtered.add(a);
                }
            }
            return filtered;
        }

        return Collections.emptyList();
    }

    public static int getUnreadEmergencyCountForUser(String role, Map<String, Object> userProfile) {
        List<SosAlert> list = getAlertsForRoleAndUser(role, userProfile);
        int count = 0;
        for (SosAlert a : list) {
            if (a != null && (!a.isReadByPortAuthority() || !"ACKNOWLEDGED".equalsIgnoreCase(a.getStatus()))) {
                count++;
            }
        }
        return count;
    }

    public static SosAlert getAlert(String incidentId) {
        if (incidentId == null) return null;
        return alertsCache.get(incidentId);
    }

    public static void syncFromFirestoreAsync() {
        if (firestoreSynced) return;
        firestoreSynced = true;
        java.util.concurrent.ForkJoinPool.commonPool().execute(() -> {
            try {
                Firestore db = getDb();
                if (db != null) {
                    ApiFuture<QuerySnapshot> future = db.collection("sos_alerts").get();
                    QuerySnapshot snapshot = future.get();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) {
                            SosAlert a = SosAlert.fromMap(data);
                            alertsCache.put(a.getIncidentId(), a);
                        }
                    }
                    notifyListeners();
                }
            } catch (Exception ignored) {}
        });
    }

    /**
     * Renders a comprehensive, modern Modal Dialog displaying all dynamic details for an Emergency SOS.
     */
    public static void showSosDetailsDialog(Window ownerWindow, SosAlert alert, String portAuthorityName, Runnable onAcknowledgeCallback) {
        if (alert == null) return;

        Stage dialog = new Stage();
        if (ownerWindow != null && ownerWindow instanceof Stage) {
            dialog.initOwner(ownerWindow);
            dialog.initModality(Modality.WINDOW_MODAL);
        } else {
            dialog.initModality(Modality.APPLICATION_MODAL);
        }
        dialog.setTitle("🔴 EMERGENCY SOS INCIDENT: " + alert.getIncidentId() + " — " + alert.getVesselName());
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setPrefWidth(680);
        root.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12;");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label badge = new Label("🔴 EMERGENCY SOS INCIDENT");
        badge.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#dc2626; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:6;");

        Label incIdLbl = new Label("ID: " + alert.getIncidentId());
        incIdLbl.setStyle("-fx-font-size:16; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boolean isAck = "ACKNOWLEDGED".equalsIgnoreCase(alert.getStatus());
        Label statusTag = new Label(isAck ? "✓ ACKNOWLEDGED" : "⚡ " + alert.getStatus());
        statusTag.setStyle(isAck
                ? "-fx-background-color:#dcfce7; -fx-text-fill:#16a34a; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:6;"
                : "-fx-background-color:#fef2f2; -fx-text-fill:#dc2626; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:6;");

        header.getChildren().addAll(badge, incIdLbl, spacer, statusTag);

        // Vessel Banner Box
        VBox vesselCard = new VBox(6);
        vesselCard.setPadding(new Insets(14));
        vesselCard.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:8;");

        Label vesselTitle = new Label("🚢  " + alert.getVesselName().toUpperCase());
        vesselTitle.setStyle("-fx-font-size:18; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        Label subVessel = new Label("Captain: " + alert.getCaptainName() + "  |  IMO: " + alert.getImo() + "  |  MMSI: " + alert.getMmsi());
        subVessel.setStyle("-fx-font-size:12; -fx-text-fill:#64748b;");

        vesselCard.getChildren().addAll(vesselTitle, subVessel);

        // Grid Details
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);

        grid.add(detailCell("Emergency Severity", alert.getSeverity(), "#dc2626"), 0, 0);
        grid.add(detailCell("Incident Category", alert.getCategory(), "#0f172a"), 1, 0);

        grid.add(detailCell("Current Location", alert.getLocation(), "#0f172a"), 0, 1);
        grid.add(detailCell("Speed & Heading", alert.getSpeed() + " • " + alert.getHeading(), "#0f172a"), 1, 1);

        grid.add(detailCell("Voyage & Destination", alert.getVoyage() + " ➔ " + alert.getDestination(), "#0f172a"), 0, 2);
        grid.add(detailCell("Assigned Port / Berth", alert.getPort(), "#0f172a"), 1, 2);

        grid.add(detailCell("Navigation Status", alert.getNavigationStatus() != null ? alert.getNavigationStatus() : "Distress / Not Under Command", "#0f172a"), 0, 3);
        grid.add(detailCell("Time Received", alert.getTimestamp(), "#0f172a"), 1, 3);

        // Incident Description Box
        VBox descBox = new VBox(6);
        descBox.setPadding(new Insets(12));
        descBox.setStyle("-fx-background-color:#fff1f2; -fx-border-color:#fecdd3; -fx-border-radius:8;");

        Label descTitle = new Label("Situation & Incident Description:");
        descTitle.setStyle("-fx-font-size:12; -fx-font-weight:bold; -fx-text-fill:#9f1239;");

        Label descContent = new Label(alert.getDescription() != null && !alert.getDescription().isBlank()
                ? alert.getDescription()
                : "Automated emergency transponder broadcast received. Main propulsion and life safety dispatch requested.");
        descContent.setStyle("-fx-font-size:12; -fx-text-fill:#334155; -fx-wrap-text:true;");
        descContent.setMaxWidth(620);

        descBox.getChildren().addAll(descTitle, descContent);

        // Action Buttons
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#334155; -fx-font-weight:bold; -fx-padding:9 18; -fx-background-radius:6; -fx-cursor:hand;");
        closeBtn.setOnAction(e -> dialog.close());

        Button ackBtn = new Button("✓  ACKNOWLEDGE DISTRESS BEACON");
        ackBtn.setStyle("-fx-background-color:#dc2626; -fx-text-fill:white; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:9 22; -fx-background-radius:6; -fx-cursor:hand;");

        if (isAck) {
            ackBtn.setText("✓  DISTRESS ACKNOWLEDGED");
            ackBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:9 22; -fx-background-radius:6;");
            ackBtn.setDisable(true);
        }

        ackBtn.setOnAction(e -> {
            acknowledgeAlert(alert.getIncidentId(), portAuthorityName);
            ackBtn.setText("✓  DISTRESS ACKNOWLEDGED");
            ackBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:9 22; -fx-background-radius:6;");
            ackBtn.setDisable(true);
            statusTag.setText("✓ ACKNOWLEDGED");
            statusTag.setStyle("-fx-background-color:#dcfce7; -fx-text-fill:#16a34a; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:6;");
            if (onAcknowledgeCallback != null) {
                onAcknowledgeCallback.run();
            }
        });

        actions.getChildren().addAll(closeBtn, ackBtn);

        root.getChildren().addAll(header, vesselCard, grid, descBox, new Separator(), actions);

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        dialog.setScene(scene);
        dialog.show();
    }

    private static VBox detailCell(String label, String value, String valueColor) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:#64748b; -fx-font-size:11; -fx-font-weight:bold;");
        Label v = new Label(value != null && !value.isBlank() ? value : "N/A");
        v.setStyle("-fx-text-fill:" + valueColor + "; -fx-font-size:13; -fx-font-weight:bold;");
        return new VBox(2, l, v);
    }
}
