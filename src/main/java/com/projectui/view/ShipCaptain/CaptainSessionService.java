package com.projectui.view.ShipCaptain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.projectui.dao.CargoDao;
import com.projectui.dao.ContainerDao;
import com.projectui.dao.NotificationDao;
import com.projectui.dao.ShipDAO;
import com.projectui.dao.UserDao;
import com.projectui.model.Notification;
import com.projectui.model.Ship;
import com.projectui.service.VesselData;
import com.projectui.service.VesselManager;
import com.projectui.view.login.LoginPage;

public class CaptainSessionService {

    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private static final Map<String, List<Map<String, String>>> captainDocuments = new ConcurrentHashMap<>();
    private static final CargoDao cargoDao = new CargoDao();
    private static final ContainerDao containerDao = new ContainerDao();
    private static final NotificationDao notificationDao = new NotificationDao();

    public static void addChangeListener(Runnable listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void notifyChanges() {
        for (Runnable r : listeners) {
            try {
                javafx.application.Platform.runLater(r);
            } catch (Exception ex) {
                try { r.run(); } catch (Exception ignored) {}
            }
        }
    }

    public static Map<String, Object> getLoggedInUser() {
        return LoginPage.loggedInUser;
    }

    public static String getCaptainName() {
        Map<String, Object> u = getLoggedInUser();
        if (u != null) {
            if (u.get("name") != null && !u.get("name").toString().isBlank()) return u.get("name").toString().trim();
            if (u.get("fullName") != null && !u.get("fullName").toString().isBlank()) return u.get("fullName").toString().trim();
            if (u.get("email") != null && !u.get("email").toString().isBlank()) {
                String em = u.get("email").toString().trim();
                return em.contains("@") ? em.substring(0, em.indexOf("@")) : em;
            }
        }
        return "Ship Captain";
    }

    public static String getCaptainEmail() {
        Map<String, Object> u = getLoggedInUser();
        if (u != null && u.get("email") != null && !u.get("email").toString().isBlank()) {
            return u.get("email").toString().trim();
        }
        return "captain@docktech.com";
    }

    public static String getCaptainUid() {
        Map<String, Object> u = getLoggedInUser();
        if (u != null && u.get("uid") != null) {
            return u.get("uid").toString().trim();
        }
        return "";
    }

    /**
     * Resolves the assigned ship for the currently authenticated captain.
     * Checks user profile, notifications, ShipDAO, and live AIS VesselManager.
     */
    public static Ship getAssignedShip() {
        Map<String, Object> u = getLoggedInUser();
        if (u == null) return null;
        return UserDao.getAssignedShipForCaptain(u);
    }

    /**
     * Resolves live AIS telemetry data for the assigned vessel.
     */
    public static VesselData getAssignedVesselData() {
        Ship ship = getAssignedShip();
        if (ship == null) return null;

        String shipName = ship.getShipName() != null ? ship.getShipName().trim() : "";
        String shipId = ship.getShipId() != null ? ship.getShipId().trim() : "";
        String shipImo = ship.getImoNumber() != null ? ship.getImoNumber().trim() : "";

        Collection<VesselData> allVessels = VesselManager.getInstance().getAllVessels();
        if (allVessels != null) {
            for (VesselData v : allVessels) {
                if (v == null) continue;
                if (!shipName.isBlank() && v.name != null && v.name.equalsIgnoreCase(shipName)) {
                    return v;
                }
                if (!shipId.isBlank() && String.valueOf(v.mmsi).equalsIgnoreCase(shipId)) {
                    return v;
                }
                if (!shipImo.isBlank() && v.imo != null && v.imo.equalsIgnoreCase(shipImo)) {
                    return v;
                }
            }
        }

        // If not in live AIS pool, synthesize a live VesselData from Ship record
        long mmsi = 211280000L;
        try {
            if (!shipId.isBlank()) mmsi = Math.abs(Long.parseLong(shipId.replaceAll("\\D", "")));
        } catch (Exception ignored) {}
        if (mmsi == 0) mmsi = 211280000L + Math.abs(shipName.hashCode() % 899999);

        return new VesselData(
                mmsi,
                !shipImo.isBlank() ? shipImo : "9234567",
                !shipName.isBlank() ? shipName : "Assigned Vessel",
                "V" + (mmsi % 8999 + 1000),
                18.9439,
                72.8354,
                12.5,
                184.0,
                184.0,
                ship.getDeparture() != null && !ship.getDeparture().isBlank() ? ship.getDeparture() : "Port of Singapore",
                ship.getStatus() != null && !ship.getStatus().isBlank() ? ship.getStatus() : "At Berth",
                ship.getShipType() != null && !ship.getShipType().isBlank() ? ship.getShipType() : "Container Vessel",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        );
    }

    /**
     * Retrieves all cargo records assigned to this captain's ship/voyage from CargoDao.
     */
    public static List<Map<String, Object>> getAssignedCargos() {
        Ship ship = getAssignedShip();
        if (ship == null) return new ArrayList<>();

        String shipName = ship.getShipName() != null ? ship.getShipName().trim().toLowerCase() : "";
        String shipId = ship.getShipId() != null ? ship.getShipId().trim().toLowerCase() : "";
        String shipImo = ship.getImoNumber() != null ? ship.getImoNumber().trim().toLowerCase() : "";

        List<Map<String, Object>> allCargos = cargoDao.getAllCargos();
        List<Map<String, Object>> matched = new ArrayList<>();

        for (Map<String, Object> c : allCargos) {
            if (c == null) continue;
            String cShip = c.get("assignedShip") != null ? c.get("assignedShip").toString().trim().toLowerCase() : "";
            if (cShip.isBlank() && c.get("shipName") != null) cShip = c.get("shipName").toString().trim().toLowerCase();
            if (cShip.isBlank() && c.get("vessel") != null) cShip = c.get("vessel").toString().trim().toLowerCase();
            if (cShip.isBlank() && c.get("shipId") != null) cShip = c.get("shipId").toString().trim().toLowerCase();

            boolean isMatch = (!shipName.isBlank() && cShip.equals(shipName))
                    || (!shipId.isBlank() && cShip.equals(shipId))
                    || (!shipImo.isBlank() && cShip.equals(shipImo));

            // If cargo has not been strictly assigned to another ship, check port route match
            if (!isMatch && cShip.isBlank()) {
                String cOrigin = c.get("originPort") != null ? c.get("originPort").toString().toLowerCase() : "";
                String cDest = c.get("destinationPort") != null ? c.get("destinationPort").toString().toLowerCase() : "";
                String sArrival = ship.getArrival() != null ? ship.getArrival().toLowerCase() : "";
                String sDep = ship.getDeparture() != null ? ship.getDeparture().toLowerCase() : "";
                if ((!cOrigin.isBlank() && (sArrival.contains(cOrigin) || cOrigin.contains(sArrival))) ||
                    (!cDest.isBlank() && (sDep.contains(cDest) || cDest.contains(sDep)))) {
                    isMatch = true;
                }
            }

            if (isMatch) {
                matched.add(c);
            }
        }

        return matched;
    }

    /**
     * Retrieves all container records assigned to this captain's ship/voyage from ContainerDao.
     */
    public static List<Map<String, Object>> getAssignedContainers() {
        Ship ship = getAssignedShip();
        if (ship == null) return new ArrayList<>();

        List<Map<String, Object>> assignedCargos = getAssignedCargos();
        List<String> matchedCargoIds = new ArrayList<>();
        for (Map<String, Object> c : assignedCargos) {
            if (c.get("cargoId") != null) matchedCargoIds.add(c.get("cargoId").toString().trim().toLowerCase());
        }

        List<Map<String, Object>> allBookings = containerDao.getAllBookings();
        List<Map<String, Object>> matched = new ArrayList<>();

        String shipName = ship.getShipName() != null ? ship.getShipName().trim().toLowerCase() : "";

        for (Map<String, Object> b : allBookings) {
            if (b == null) continue;
            String bCargoId = b.get("cargoId") != null ? b.get("cargoId").toString().trim().toLowerCase() : "";
            String bShip = b.get("assignedShip") != null ? b.get("assignedShip").toString().trim().toLowerCase() : "";

            if ((!bCargoId.isBlank() && matchedCargoIds.contains(bCargoId)) || (!bShip.isBlank() && bShip.equals(shipName))) {
                matched.add(b);
            }
        }

        return matched;
    }

    /**
     * Updates the status of the captain's assigned vessel across ShipDAO, VesselManager, and NotificationDao.
     */
    public static boolean updateShipStatus(String newStatus, String location, String remarks) {
        Ship ship = getAssignedShip();
        if (ship == null) return false;

        String captain = getCaptainName();

        ship.setStatus(newStatus);
        if (location != null && !location.isBlank()) {
            ship.setBerth(location);
        }
        new ShipDAO().saveShip(ship);

        // Update live AIS VesselManager
        VesselData v = getAssignedVesselData();
        if (v != null) {
            v.navigationStatus = newStatus;
            v.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            VesselManager.getInstance().updateOrAddVessel(v);
        }

        // Notify Port Authority & Admin
        try {
            Notification n = new Notification();
            n.setTitle("Ship Status Updated: " + ship.getShipName());
            n.setMessage("Captain " + captain + " updated vessel status to '" + newStatus + "'. Location: " + (location != null ? location : "N/A") + (remarks != null && !remarks.isBlank() ? " | " + remarks : ""));
            n.setType("OPERATIONAL");
            n.setUserId(getCaptainUid().isBlank() ? getCaptainEmail() : getCaptainUid());
            n.setAssignedBy(captain);
            n.setVesselName(ship.getShipName());
            n.setVesselId(ship.getShipId());
            n.setPort(ship.getBerth() != null ? ship.getBerth() : "Port of Mumbai");
            n.setPriority("NORMAL");
            notificationDao.createNotification(n);
        } catch (Exception ignored) {}

        notifyChanges();
        return true;
    }

    /**
     * Confirms cargo loading and synchronizes status with CargoDao and ContainerDao.
     */
    public static boolean confirmLoading(String cargoId, String loadingStatus, String remarks) {
        Ship ship = getAssignedShip();
        if (ship == null) return false;

        if (cargoId != null && !cargoId.isBlank()) {
            Map<String, Object> cargo = cargoDao.getCargo(cargoId);
            if (cargo != null) {
                cargo.put("status", loadingStatus);
                cargo.put("loadingStatus", loadingStatus);
                cargo.put("assignedShip", ship.getShipName());
                cargo.put("lastVerifiedBy", getCaptainName());
                cargo.put("lastVerifiedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
                if (remarks != null && !remarks.isBlank()) {
                    cargo.put("loadingRemarks", remarks);
                }
                cargoDao.saveCargo(
                        cargoId,
                        cargo.get("cargoName") != null ? cargo.get("cargoName").toString() : "",
                        cargo.get("cargoType") != null ? cargo.get("cargoType").toString() : "",
                        cargo.get("quantity") != null ? cargo.get("quantity").toString() : "",
                        cargo.get("weight") != null ? cargo.get("weight").toString() : "",
                        cargo.get("originPort") != null ? cargo.get("originPort").toString() : "",
                        cargo.get("destinationPort") != null ? cargo.get("destinationPort").toString() : "",
                        loadingStatus
                );
            }
        }

        // Send confirmation notification
        try {
            Notification n = new Notification();
            n.setTitle("Cargo Loading Confirmed - " + (cargoId != null ? cargoId : ship.getShipName()));
            n.setMessage("Captain " + getCaptainName() + " confirmed cargo loading status: '" + loadingStatus + "' for vessel " + ship.getShipName() + (remarks != null && !remarks.isBlank() ? ". Remarks: " + remarks : ""));
            n.setType("CARGO");
            n.setUserId(getCaptainUid().isBlank() ? getCaptainEmail() : getCaptainUid());
            n.setAssignedBy(getCaptainName());
            n.setVesselName(ship.getShipName());
            n.setVesselId(ship.getShipId());
            n.setPort(ship.getBerth() != null ? ship.getBerth() : "Port of Mumbai");
            n.setPriority("NORMAL");
            notificationDao.createNotification(n);
        } catch (Exception ignored) {}

        notifyChanges();
        return true;
    }

    public static Map<String, Object> latestSosRecord = null;

    /**
     * Dispatches high-priority Emergency SOS alert.
     */
    public static Map<String, Object> sendEmergencySOS(String priority, String emergencyType, String details) {
        Ship ship = getAssignedShip();
        VesselData v = getAssignedVesselData();

        String shipName = ship != null ? ship.getShipName() : (v != null ? v.name : "AFRAPEARL II");
        String shipId = ship != null && ship.getShipId() != null ? ship.getShipId() : (v != null ? String.valueOf(v.mmsi) : "SHP-2026");
        String imo = ship != null && ship.getImoNumber() != null ? ship.getImoNumber() : (v != null ? v.imo : "9234567");
        String mmsi = v != null ? String.valueOf(v.mmsi) : "211280000";
        String location = v != null ? String.format("%.4f° N, %.4f° E", v.latitude, v.longitude) : "18.9439° N, 72.8354° E";
        String latStr = v != null ? String.format("%.4f° N", v.latitude) : "18.9439° N";
        String lonStr = v != null ? String.format("%.4f° E", v.longitude) : "72.8354° E";
        String speed = v != null ? String.format("%.1f Knots", v.speed) : "12.5 Knots";
        String heading = v != null ? String.format("%.1f°", v.heading) : "184.0°";
        String navStatus = v != null && v.navigationStatus != null ? v.navigationStatus : "Underway using Engine";
        String port = ship != null && ship.getBerth() != null ? ship.getBerth() : "Port of Mumbai";
        String dest = ship != null && ship.getDeparture() != null ? ship.getDeparture() : "Port of Singapore";
        String voyage = ship != null && ship.getShipId() != null ? "VOY-" + ship.getShipId() : "VOY-2026-08";
        String incidentId = "SOS-" + (System.currentTimeMillis() % 100000);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"));

        com.projectui.service.SosEmergencyService.SosAlert alert = new com.projectui.service.SosEmergencyService.SosAlert();
        alert.setIncidentId(incidentId);
        alert.setVesselName(shipName);
        alert.setVesselId(shipId);
        alert.setCaptainName(getCaptainName());
        alert.setCaptainUid(getCaptainUid().isBlank() ? getCaptainEmail() : getCaptainUid());
        alert.setImo(imo);
        alert.setMmsi(mmsi);
        alert.setSeverity(priority);
        alert.setCategory(emergencyType);
        alert.setDescription(details);
        alert.setLocation(location);
        alert.setLatitude(latStr);
        alert.setLongitude(lonStr);
        alert.setSpeed(speed);
        alert.setHeading(heading);
        alert.setNavigationStatus(navStatus);
        alert.setPort(port);
        alert.setDestination(dest);
        alert.setVoyage(voyage);
        alert.setTimestamp(timestamp);
        alert.setStatus("TRANSMITTED");
        alert.setCreatedAtEpoch(System.currentTimeMillis());

        com.projectui.service.SosEmergencyService.registerSosAlert(alert);

        Map<String, Object> record = alert.toMap();
        latestSosRecord = record;

        try {
            Notification n = new Notification();
            n.setTitle("🚨 SOS DISTRESS BEACON: " + shipName + " [" + priority + "]");
            n.setMessage("INCIDENT ID: " + incidentId + "\nEMERGENCY TYPE: " + emergencyType + "\nCAPTAIN: " + getCaptainName() + "\nLOCATION: " + location + " (Speed: " + speed + ", Heading: " + heading + ")\nPORT/BERTH: " + port + "\nDESTINATION: " + dest + "\nDETAILS: " + details);
            n.setType("CRITICAL_ALERT");
            n.setUserId(getCaptainUid().isBlank() ? getCaptainEmail() : getCaptainUid());
            n.setAssignedBy(getCaptainName());
            n.setVesselName(shipName);
            n.setVesselId(shipId);
            n.setPort(port);
            n.setPriority("CRITICAL");
            notificationDao.createNotification(n);
        } catch (Exception ignored) {}

        notifyChanges();
        return record;
    }

    public static Map<String, Object> getLatestSosRecord() {
        if (latestSosRecord == null) {
            List<com.projectui.service.SosEmergencyService.SosAlert> list = com.projectui.service.SosEmergencyService.getAllAlerts();
            if (!list.isEmpty()) {
                latestSosRecord = list.get(0).toMap();
            }
        }
        return latestSosRecord;
    }

    /**
     * Stores and retrieves uploaded documents for the captain and assigned vessel.
     */
    public static List<Map<String, String>> getDocuments() {
        String key = getCaptainEmail().toLowerCase();
        return captainDocuments.computeIfAbsent(key, k -> {
            List<Map<String, String>> list = new ArrayList<>();
            Ship s = getAssignedShip();
            String vName = s != null ? s.getShipName() : "AFRAPEARL II";
            String voyId = "VOY-" + (s != null && s.getShipId() != null ? s.getShipId() : "2026") + "-E";

            Map<String, String> d1 = new HashMap<>();
            d1.put("docId", "DOC-MNF-1092");
            d1.put("name", "Cargo_Manifest_Voyage_1092.pdf");
            d1.put("type", "Cargo Manifest");
            d1.put("size", "2.4 MB");
            d1.put("date", LocalDateTime.now().minusHours(4).format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
            d1.put("status", "Verified");
            d1.put("vessel", vName);
            d1.put("voyage", voyId);
            d1.put("uploadedBy", getCaptainName());
            d1.put("url", "https://res.cloudinary.com/snmxmesm/raw/upload/docktech_port_documents/sample_document.pdf");
            list.add(d1);

            Map<String, String> d2 = new HashMap<>();
            d2.put("docId", "DOC-CLR-8841");
            d2.put("name", "Port_Clearance_Certificate.pdf");
            d2.put("type", "Port Clearance");
            d2.put("size", "1.1 MB");
            d2.put("date", LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
            d2.put("status", "Approved");
            d2.put("vessel", vName);
            d2.put("voyage", voyId);
            d2.put("uploadedBy", "Harbor Master Desk");
            d2.put("url", "https://res.cloudinary.com/snmxmesm/raw/upload/docktech_port_documents/sample_document.pdf");
            list.add(d2);

            Map<String, String> d3 = new HashMap<>();
            d3.put("docId", "DOC-BOL-5532");
            d3.put("name", "Bill_Of_Lading_Official.pdf");
            d3.put("type", "Bill of Lading");
            d3.put("size", "1.8 MB");
            d3.put("date", LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
            d3.put("status", "Pending Review");
            d3.put("vessel", vName);
            d3.put("voyage", voyId);
            d3.put("uploadedBy", getCaptainName());
            d3.put("url", "https://res.cloudinary.com/snmxmesm/raw/upload/docktech_port_documents/sample_document.pdf");
            list.add(d3);

            return list;
        });
    }

    public static void addDocument(String docId, String name, String type, String size, String status, String url, String filePath) {
        List<Map<String, String>> list = getDocuments();
        Ship s = getAssignedShip();
        String vName = s != null ? s.getShipName() : "AFRAPEARL II";
        String voyId = "VOY-" + (s != null && s.getShipId() != null ? s.getShipId() : "2026") + "-E";

        Map<String, String> doc = new HashMap<>();
        doc.put("docId", docId != null && !docId.isBlank() ? docId : "DOC-" + (System.currentTimeMillis() % 100000));
        doc.put("name", name);
        doc.put("type", type);
        doc.put("size", size);
        doc.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        doc.put("status", status != null && !status.isBlank() ? status : "Pending Review");
        doc.put("vessel", vName);
        doc.put("voyage", voyId);
        doc.put("uploadedBy", getCaptainName());
        doc.put("url", url != null ? url : "");
        if (filePath != null) doc.put("filePath", filePath);

        list.add(0, doc);
        notifyChanges();
    }

    public static void addDocument(String name, String type, String size) {
        addDocument("DOC-" + (System.currentTimeMillis() % 100000), name, type, size, "Pending Review", "", null);
    }
}
