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

        return null;
    }

    /**
     * Retrieves all cargo records assigned to this captain's ship/voyage from CargoDao.
     */
    public static List<Map<String, Object>> getAssignedCargos() {
        Ship ship = getAssignedShip();
        Map<String, Object> u = LoginPage.loggedInUser;
        String capUid = (u != null && u.get("uid") != null) ? u.get("uid").toString().trim().toLowerCase() : "";
        String capEmail = (u != null && u.get("email") != null) ? u.get("email").toString().trim().toLowerCase() : "";

        List<Map<String, Object>> allCargos = cargoDao.getAllCargos();
        List<Map<String, Object>> matched = new ArrayList<>();

        String shipName = (ship != null && ship.getShipName() != null) ? ship.getShipName().trim().toLowerCase() : "";
        String shipId = (ship != null && ship.getShipId() != null) ? ship.getShipId().trim().toLowerCase() : "";
        String shipImo = (ship != null && ship.getImoNumber() != null) ? ship.getImoNumber().trim().toLowerCase() : "";

        if (shipName.isBlank() && u != null) {
            if (u.get("assignedShip") != null) shipName = u.get("assignedShip").toString().trim().toLowerCase();
            else if (u.get("shipName") != null) shipName = u.get("shipName").toString().trim().toLowerCase();
        }
        if (shipId.isBlank() && u != null) {
            if (u.get("assignedShipId") != null) shipId = u.get("assignedShipId").toString().trim().toLowerCase();
            else if (u.get("shipId") != null) shipId = u.get("shipId").toString().trim().toLowerCase();
        }
        if (shipImo.isBlank() && u != null) {
            if (u.get("shipImo") != null) shipImo = u.get("shipImo").toString().trim().toLowerCase();
            else if (u.get("imoNumber") != null) shipImo = u.get("imoNumber").toString().trim().toLowerCase();
        }

        for (Map<String, Object> c : allCargos) {
            if (c == null) continue;
            String cAssignedShipId = c.get("assignedShipId") != null ? c.get("assignedShipId").toString().trim().toLowerCase() : "";
            String cShip = c.get("assignedShip") != null ? c.get("assignedShip").toString().trim().toLowerCase() : "";
            if (cShip.isBlank() && c.get("shipName") != null) cShip = c.get("shipName").toString().trim().toLowerCase();
            if (cShip.isBlank() && c.get("vessel") != null) cShip = c.get("vessel").toString().trim().toLowerCase();
            if (cShip.isBlank() && c.get("shipId") != null) cShip = c.get("shipId").toString().trim().toLowerCase();

            String cCapUid = c.get("assignedCaptainUid") != null ? c.get("assignedCaptainUid").toString().trim().toLowerCase() : "";
            String cCapEmail = c.get("assignedCaptainEmail") != null ? c.get("assignedCaptainEmail").toString().trim().toLowerCase() : "";
            if (cCapUid.isBlank() && c.get("captainUid") != null) cCapUid = c.get("captainUid").toString().trim().toLowerCase();
            if (cCapEmail.isBlank() && c.get("captainEmail") != null) cCapEmail = c.get("captainEmail").toString().trim().toLowerCase();

            boolean isMatch = (!shipId.isBlank() && !cAssignedShipId.isBlank() && cAssignedShipId.equals(shipId))
                    || (!shipName.isBlank() && !cShip.isBlank() && (cShip.equals(shipName) || cShip.contains(shipName) || shipName.contains(cShip)))
                    || (!shipId.isBlank() && !cShip.isBlank() && cShip.equals(shipId))
                    || (!shipImo.isBlank() && !cShip.isBlank() && cShip.equals(shipImo))
                    || (!capUid.isBlank() && !cCapUid.isBlank() && cCapUid.equals(capUid))
                    || (!capEmail.isBlank() && !cCapEmail.isBlank() && cCapEmail.equals(capEmail));

            if (isMatch) {
                matched.add(c);
            }
        }

        return matched;
    }

    /**
     * Resolves the destination port for the currently assigned shipment/cargo.
     * Uses the existing cargo/request destination recorded for the assigned shipment.
     * Falls back to ship default only if no assigned cargo exists.
     */
    public static String getAssignedVoyageDestination() {
        List<Map<String, Object>> cargos = getAssignedCargos();
        if (cargos != null) {
            for (Map<String, Object> c : cargos) {
                if (c == null) continue;
                Object dest = c.get("destinationPort");
                if (dest == null || dest.toString().isBlank()) dest = c.get("destination");
                if (dest == null || dest.toString().isBlank()) dest = c.get("destPort");
                if (dest != null && !dest.toString().isBlank() && !dest.toString().equalsIgnoreCase("—") && !dest.toString().equalsIgnoreCase("-")) {
                    return dest.toString().trim();
                }
            }
        }
        Ship ship = getAssignedShip();
        if (ship != null && ship.getDeparture() != null && !ship.getDeparture().isBlank() && !ship.getDeparture().equalsIgnoreCase("—") && !ship.getDeparture().equalsIgnoreCase("-")) {
            return ship.getDeparture().trim();
        }
        VesselData v = getAssignedVesselData();
        if (v != null && v.destination != null && !v.destination.isBlank()) {
            return v.destination.trim();
        }
        return "Port of Singapore";
    }

    /**
     * Resolves the origin port for the currently assigned shipment/cargo.
     * Uses the existing cargo/request origin recorded for the assigned shipment.
     * Falls back to ship default only if no assigned cargo exists.
     */
    public static String getAssignedVoyageOrigin() {
        List<Map<String, Object>> cargos = getAssignedCargos();
        if (cargos != null) {
            for (Map<String, Object> c : cargos) {
                if (c == null) continue;
                Object orig = c.get("originPort");
                if (orig == null || orig.toString().isBlank()) orig = c.get("origin");
                if (orig == null || orig.toString().isBlank()) orig = c.get("departurePort");
                if (orig != null && !orig.toString().isBlank() && !orig.toString().equalsIgnoreCase("—") && !orig.toString().equalsIgnoreCase("-")) {
                    return orig.toString().trim();
                }
            }
        }
        Ship ship = getAssignedShip();
        if (ship != null && ship.getArrival() != null && !ship.getArrival().isBlank() && !ship.getArrival().matches(".*\\d.*") && !ship.getArrival().equalsIgnoreCase("—")) {
            return ship.getArrival().trim();
        }
        return "Port of Chennai";
    }

    /**
     * Retrieves all container records assigned to this captain's ship/voyage from ContainerDao.
     */
    public static List<Map<String, Object>> getAssignedContainers() {
        Ship ship = getAssignedShip();
        List<Map<String, Object>> assignedCargos = getAssignedCargos();
        List<String> matchedCargoIds = new ArrayList<>();
        for (Map<String, Object> c : assignedCargos) {
            if (c.get("cargoId") != null) matchedCargoIds.add(c.get("cargoId").toString().trim().toLowerCase());
        }

        List<Map<String, Object>> allBookings = containerDao.getAllBookings();
        List<Map<String, Object>> matched = new ArrayList<>();

        String shipName = (ship != null && ship.getShipName() != null) ? ship.getShipName().trim().toLowerCase() : "";

        for (Map<String, Object> b : allBookings) {
            if (b == null) continue;
            String bCargoId = b.get("cargoId") != null ? b.get("cargoId").toString().trim().toLowerCase() : "";
            String bShip = b.get("assignedShip") != null ? b.get("assignedShip").toString().trim().toLowerCase() : "";

            if ((!bCargoId.isBlank() && matchedCargoIds.contains(bCargoId)) || (!shipName.isBlank() && !bShip.isBlank() && bShip.equals(shipName))) {
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
                cargo.put("loadingStatus", loadingStatus);
                cargo.put("lastVerifiedBy", getCaptainName());
                cargo.put("lastVerifiedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
                if (remarks != null && !remarks.isBlank()) {
                    cargo.put("loadingRemarks", remarks);
                }
                cargoDao.updateCargoStatus(cargoId, loadingStatus);
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

        String shipName = ship != null ? ship.getShipName() : (v != null ? v.name : "");
        String shipId = ship != null && ship.getShipId() != null ? ship.getShipId() : (v != null ? String.valueOf(v.mmsi) : "");
        String imo = ship != null && ship.getImoNumber() != null ? ship.getImoNumber() : (v != null ? v.imo : "");
        String mmsi = v != null ? String.valueOf(v.mmsi) : "";
        String location = v != null ? String.format("%.4f° N, %.4f° E", v.latitude, v.longitude) : "";
        String latStr = v != null ? String.format("%.4f° N", v.latitude) : "";
        String lonStr = v != null ? String.format("%.4f° E", v.longitude) : "";
        String speed = v != null ? String.format("%.1f Knots", v.speed) : "";
        String heading = v != null ? String.format("%.1f°", v.heading) : "";
        String navStatus = v != null && v.navigationStatus != null ? v.navigationStatus : "";
        String port = ship != null && ship.getBerth() != null ? ship.getBerth() : getAssignedVoyageOrigin();
        String dest = getAssignedVoyageDestination();
        String voyage = ship != null && ship.getShipId() != null ? "VOY-" + ship.getShipId() : "";
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
            String myShip = "";
            String myUid = getCaptainUid();
            Ship s = getAssignedShip();
            if (s != null && s.getShipName() != null) myShip = s.getShipName().trim().toLowerCase();

            List<com.projectui.service.SosEmergencyService.SosAlert> list = com.projectui.service.SosEmergencyService.getAllAlerts();
            for (com.projectui.service.SosEmergencyService.SosAlert a : list) {
                if (a == null) continue;
                String capUid = a.getCaptainUid() != null ? a.getCaptainUid().trim().toLowerCase() : "";
                String vName = a.getVesselName() != null ? a.getVesselName().trim().toLowerCase() : "";
                if ((!myUid.isEmpty() && capUid.equalsIgnoreCase(myUid)) || (!myShip.isEmpty() && vName.contains(myShip))) {
                    latestSosRecord = a.toMap();
                    break;
                }
            }
        }
        return latestSosRecord;
    }

    /**
     * Stores and retrieves uploaded documents for the captain and assigned vessel.
     */
    public static List<Map<String, String>> getDocuments() {
        String key = getCaptainEmail().toLowerCase();
        return captainDocuments.computeIfAbsent(key, k -> new ArrayList<>());
    }

    public static void addDocument(String docId, String name, String type, String size, String status, String url, String filePath) {
        List<Map<String, String>> list = getDocuments();
        Ship s = getAssignedShip();
        String vName = s != null && s.getShipName() != null ? s.getShipName() : "";
        String voyId = s != null && s.getShipId() != null && !s.getShipId().isBlank() ? "VOY-" + s.getShipId() + "-E" : "";

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
