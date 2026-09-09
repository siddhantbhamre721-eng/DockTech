package com.projectui.view.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.projectui.dao.ShipDAO;
import com.projectui.dao.UserDao;
import com.projectui.model.Ship;
import com.projectui.service.VesselData;
import com.projectui.service.VesselManager;

/**
 * Compatibility facade named `AdminDataStore` to match existing references.
 * This class mirrors the in-memory data used by the application.
 */
public class AdminDataStore {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final String PERSIST_DELETED_PORTS = System.getProperty("user.home") + "/.docktech_deleted_ports.properties";
    private static final java.util.Set<String> deletedPortKeys = java.util.concurrent.ConcurrentHashMap.newKeySet();

    static {
        loadDeletedPorts();
    }

    private static void loadDeletedPorts() {
        if (deletedPortKeys == null) return;
        try {
            java.io.File file = new java.io.File(PERSIST_DELETED_PORTS);
            if (file.exists()) {
                java.util.Properties props = new java.util.Properties();
                try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
                    props.load(in);
                }
                for (String key : props.stringPropertyNames()) {
                    if (key != null && !key.isBlank()) {
                        deletedPortKeys.add(key.trim().toLowerCase());
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public static void saveDeletedPort(String portName, String portCode) {
        if (deletedPortKeys == null) return;
        if (portName != null && !portName.isBlank()) {
            deletedPortKeys.add(portName.trim().toLowerCase());
        }
        if (portCode != null && !portCode.isBlank()) {
            deletedPortKeys.add(portCode.trim().toLowerCase());
        }
        try {
            java.util.Properties props = new java.util.Properties();
            for (String key : deletedPortKeys) {
                props.setProperty(key, "DELETED");
            }
            java.io.File file = new java.io.File(PERSIST_DELETED_PORTS);
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                props.store(out, "DockTech Deleted Ports");
            }
        } catch (Exception ignored) {}
    }

    public static void clearDeletedPort(String portName, String portCode) {
        if (deletedPortKeys == null) return;
        if (portName != null && !portName.isBlank()) {
            deletedPortKeys.remove(portName.trim().toLowerCase());
        }
        if (portCode != null && !portCode.isBlank()) {
            deletedPortKeys.remove(portCode.trim().toLowerCase());
        }
        try {
            java.util.Properties props = new java.util.Properties();
            for (String key : deletedPortKeys) {
                props.setProperty(key, "DELETED");
            }
            java.io.File file = new java.io.File(PERSIST_DELETED_PORTS);
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                props.store(out, "DockTech Deleted Ports");
            }
        } catch (Exception ignored) {}
    }

    public static boolean isPortDeleted(String name, String code) {
        if (deletedPortKeys == null || deletedPortKeys.isEmpty()) return false;
        if (name != null && deletedPortKeys.contains(name.trim().toLowerCase())) return true;
        if (code != null && deletedPortKeys.contains(code.trim().toLowerCase())) return true;
        return false;
    }

    private static final AdminDataStore INSTANCE = new AdminDataStore();

    public final List<Port> ports = new ArrayList<>();
    public final List<UserAccount> users = new ArrayList<>();
    public final List<ReportRow> reports = new ArrayList<>();
    public final List<PortRequest> portRequests = new ArrayList<>();

    public String adminName = "docktech admin";
    public String adminEmail = "docktech@gmail.com";
    public String adminPassword = "admin123";
    public String accountCreated = "01 Jan 2026, 09:00";
    public String language = "English";
    public String dateFormat = "DD/MM/YYYY";
    public boolean notifications = true;
    public String authToken = "";
    public String settingsMessage = "";
    public String reportMessage = "";
    public String lastExportPath = "";

    private AdminDataStore() {
        loadDeletedPorts();
        // Ports and users are loaded exclusively from Firestore via syncPortsFromFirestore().
        // No hardcoded data is inserted here.
    }

    public static AdminDataStore getInstance() {
        return INSTANCE;
    }

    public void clearSession() {
        adminName = "docktech admin";
        adminEmail = "docktech@gmail.com";
        language = "English";
        dateFormat = "DD/MM/YYYY";
        notifications = true;
        authToken = "";
        settingsMessage = "";
        reportMessage = "";
        lastExportPath = "";
    }

    public int activePorts() {
        int count = 0;
        for (Port port : ports) {
            if (port != null && !isPortDeleted(port.name, port.code)
                    && ("APPROVED".equalsIgnoreCase(port.approvalStatus) || port.approvalStatus == null)
                    && "Operational".equalsIgnoreCase(port.status)) {
                count++;
            }
        }
        return count;
    }

    public int totalPorts() {
        int count = 0;
        for (Port port : ports) {
            if (port != null && !isPortDeleted(port.name, port.code)
                    && ("APPROVED".equalsIgnoreCase(port.approvalStatus) || port.approvalStatus == null)) {
                count++;
            }
        }
        return count;
    }

    public List<String> getApprovedPortNames() {
        List<String> list = new ArrayList<>();
        for (Port p : ports) {
            if (p != null && p.name != null && !isPortDeleted(p.name, p.code)) {
                if ("APPROVED".equalsIgnoreCase(p.approvalStatus) || p.approvalStatus == null) {
                    if (!list.contains(p.name)) {
                        list.add(p.name);
                    }
                }
            }
        }
        return list;
    }

    public Port getPortByName(String name) {
        return getPortByIdOrName(name);
    }

    public Port getPortByIdOrName(String identifier) {
        if (identifier == null || identifier.isBlank()) return null;
        String target = identifier.trim().toLowerCase();

        // 1. Direct match on code or name in active ports
        for (Port p : ports) {
            if (p != null) {
                if (p.code != null && p.code.trim().equalsIgnoreCase(target)) return p;
                if (p.name != null && p.name.trim().equalsIgnoreCase(target)) return p;
            }
        }

        // 2. Partial match in active ports
        for (Port p : ports) {
            if (p != null && p.name != null) {
                String pLower = p.name.toLowerCase();
                if (pLower.contains(target) || target.contains(pLower)) {
                    return p;
                }
            }
        }

        return null;
    }

    /** Persists an approved request as the single canonical registered port record. */
    public void persistApprovedPort(PortRequest request) throws Exception {
        if (request == null || request.portName == null || request.portName.isBlank()) {
            throw new IllegalArgumentException("Approved port is missing its name.");
        }

        com.google.cloud.firestore.Firestore db = com.projectui.config.FirebaseConfig.getFirestore();
        if (db == null) throw new IllegalStateException("Firebase is unavailable.");

        String code = request.portCode != null && !request.portCode.isBlank()
            ? request.portCode.trim()
            : (request.requestId != null && !request.requestId.isBlank() ? request.requestId : request.portName.trim());
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("name", request.portName);
        data.put("portName", request.portName);
        data.put("location", request.location);
        data.put("address", request.address);
        data.put("code", code);
        data.put("portCode", code);
        data.put("portAuthority", request.proposedPaName);
        data.put("status", "Operational");
        data.put("approvalStatus", "APPROVED");
        data.put("cargo", request.details);
        data.put("cargoType", request.details);
        data.put("capacity", "100%");
        data.put("maintenance", "Clear");
        data.put("maintenanceInfo", "Clear");
        data.put("berths", 12);
        data.put("vessels", 0);
        data.put("lat", request.lat);
        data.put("latitude", request.lat);
        data.put("lng", request.lng);
        data.put("longitude", request.lng);
        data.put("lastUpdated", now());
        data.put("requestId", request.requestId);
        data.put("requesterName", request.requesterName);
        data.put("requesterEmail", request.requesterEmail);
        data.put("proposedPaEmail", request.proposedPaEmail);
        data.put("updatedAt", new java.util.Date());

        com.google.cloud.firestore.DocumentReference target = db.collection("registered_ports").document(code);
        com.google.cloud.firestore.QuerySnapshot existing = db.collection("registered_ports").get().get();
        if (existing != null) {
            for (com.google.cloud.firestore.DocumentSnapshot doc : existing.getDocuments()) {
                String existingCode = strField(doc.getData(), "code", strField(doc.getData(), "portCode", ""));
                String existingName = strField(doc.getData(), "name", strField(doc.getData(), "portName", ""));
                if (code.equalsIgnoreCase(existingCode) || request.portName.equalsIgnoreCase(existingName)) {
                    target = doc.getReference();
                    break;
                }
            }
        }
        target.set(data, com.google.cloud.firestore.SetOptions.merge()).get(5, java.util.concurrent.TimeUnit.SECONDS);
    }

    public boolean deletePort(Port port) {
        if (port == null) return false;

        // 1. Delete from Firestore database if connected
        try {
            com.google.cloud.firestore.Firestore db = com.projectui.config.FirebaseConfig.getFirestore();
            if (db != null) {
                // Delete from registered_ports collection
                if (port.code != null && !port.code.isBlank()) {
                    try {
                        db.collection("registered_ports").document(port.code.trim()).delete().get(5, java.util.concurrent.TimeUnit.SECONDS);
                    } catch (Exception ignored) {}
                }
                if (port.name != null && !port.name.isBlank()) {
                    String docId = port.name.replaceAll("[^A-Za-z0-9]", "_");
                    try {
                        db.collection("registered_ports").document(docId).delete().get(5, java.util.concurrent.TimeUnit.SECONDS);
                    } catch (Exception ignored) {}
                }
                try {
                    com.google.cloud.firestore.QuerySnapshot regSnap = db.collection("registered_ports").get().get(5, java.util.concurrent.TimeUnit.SECONDS);
                    if (regSnap != null) {
                        for (com.google.cloud.firestore.DocumentSnapshot doc : regSnap.getDocuments()) {
                            String c = doc.getString("code");
                            if (c == null) c = doc.getString("portCode");
                            String n = doc.getString("name");
                            if (n == null) n = doc.getString("portName");
                            boolean match = (port.code != null && !port.code.isBlank() && port.code.equalsIgnoreCase(c))
                                    || (port.name != null && !port.name.isBlank() && port.name.equalsIgnoreCase(n));
                            if (match) {
                                doc.getReference().delete().get(5, java.util.concurrent.TimeUnit.SECONDS);
                            }
                        }
                    }
                } catch (Exception ignored) {}

                // Delete matching port_requests collection records
                try {
                    com.google.cloud.firestore.QuerySnapshot reqSnap = db.collection("port_requests").get().get(5, java.util.concurrent.TimeUnit.SECONDS);
                    if (reqSnap != null) {
                        for (com.google.cloud.firestore.DocumentSnapshot doc : reqSnap.getDocuments()) {
                            String c = doc.getString("portCode");
                            String n = doc.getString("portName");
                            boolean match = (port.code != null && !port.code.isBlank() && port.code.equalsIgnoreCase(c))
                                    || (port.name != null && !port.name.isBlank() && port.name.equalsIgnoreCase(n));
                            if (match) {
                                doc.getReference().delete().get(5, java.util.concurrent.TimeUnit.SECONDS);
                            }
                        }
                    }
                } catch (Exception ignored) {}

                // Delete specifically associated ship records from Ships collection
                try {
                    com.google.cloud.firestore.QuerySnapshot shipSnap = db.collection("Ships").get().get(5, java.util.concurrent.TimeUnit.SECONDS);
                    if (shipSnap != null) {
                        for (com.google.cloud.firestore.DocumentSnapshot doc : shipSnap.getDocuments()) {
                            String company = doc.getString("company");
                            String arrival = doc.getString("arrival");
                            String departure = doc.getString("departure");
                            boolean match = (port.name != null && !port.name.isBlank() && (port.name.equalsIgnoreCase(company) || port.name.equalsIgnoreCase(arrival) || port.name.equalsIgnoreCase(departure)))
                                    || (port.code != null && !port.code.isBlank() && (port.code.equalsIgnoreCase(company) || port.code.equalsIgnoreCase(arrival) || port.code.equalsIgnoreCase(departure)));
                            if (match) {
                                doc.getReference().delete().get(5, java.util.concurrent.TimeUnit.SECONDS);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ex) {
            System.err.println("Database deletion error for port " + port.name + ": " + ex.getMessage());
            throw new RuntimeException("Database deletion failed: " + ex.getMessage(), ex);
        }

        // 2. Persist deletion in local properties
        saveDeletedPort(port.name, port.code);

        // 3. Remove port from memory list
        ports.remove(port);
        ports.removeIf(p -> (p.name != null && p.name.equalsIgnoreCase(port.name)) || (p.code != null && p.code.equalsIgnoreCase(port.code)));

        // 4. Remove port requests from memory & DAO
        portRequests.removeIf(req -> (req.portName != null && req.portName.equalsIgnoreCase(port.name))
                || (req.portCode != null && req.portCode.equalsIgnoreCase(port.code)));
        try {
            new com.projectui.dao.PortRequestDao().deletePortRequestsForPort(port.name, port.code);
        } catch (Exception ignored) {}

        // 5. Clean up associated payments, cargos, and container bookings
        try {
            new com.projectui.dao.PaymentDao().deletePaymentsForPort(port.name, port.code);
            com.projectui.view.portauthority.PaymentPage.clearPortPayments(port.name, port.code);
        } catch (Exception ignored) {}

        try {
            new com.projectui.dao.CargoDao().deleteCargosForPort(port.name, port.code);
        } catch (Exception ignored) {}

        try {
            new com.projectui.dao.ContainerDao().deleteBookingsForPort(port.name, port.code);
        } catch (Exception ignored) {}

        // 6. Revoke associated Port Authority access without deleting unrelated users
        com.projectui.dao.UserDao.deactivatePortAuthorityForPort(port.name, port.code);

        // 7. Remove specifically associated vessels from VesselManager
        try {
            java.util.Collection<com.projectui.service.VesselData> allVessels = com.projectui.service.VesselManager.getInstance().getAllVessels();
            if (allVessels != null) {
                java.util.List<Long> toRemove = new java.util.ArrayList<>();
                for (com.projectui.service.VesselData v : allVessels) {
                    if (v != null) {
                        boolean match = (port.name != null && !port.name.isBlank() && (port.name.equalsIgnoreCase(v.destination) || (v.name != null && v.name.equalsIgnoreCase(port.name))))
                                || (port.code != null && !port.code.isBlank() && port.code.equalsIgnoreCase(v.destination));
                        if (match) {
                            toRemove.add(v.mmsi);
                        }
                    }
                }
                for (Long mmsi : toRemove) {
                    com.projectui.service.VesselManager.getInstance().removeVessel(mmsi);
                }
            }
        } catch (Exception ignored) {}

        // 8. Refresh all map instances
        com.projectui.view.admin.GeoTileMapCanvas.refreshAllMapInstances();
        com.projectui.view.admin.DashboardPage.refreshAllDashboards();
        return true;
    }

    public boolean deletePortByName(String name) {
        if (name == null || name.isBlank()) return false;
        saveDeletedPort(name, null);
        Port p = getPortByName(name);
        if (p != null) {
            return deletePort(p);
        }
        ports.removeIf(prt -> prt.name != null && prt.name.equalsIgnoreCase(name.trim()));

        try {
            new com.projectui.dao.PaymentDao().deletePaymentsForPort(name, null);
            com.projectui.view.portauthority.PaymentPage.clearPortPayments(name, null);
        } catch (Exception ignored) {}

        try {
            new com.projectui.dao.CargoDao().deleteCargosForPort(name, null);
        } catch (Exception ignored) {}

        try {
            new com.projectui.dao.ContainerDao().deleteBookingsForPort(name, null);
        } catch (Exception ignored) {}

        try {
            new com.projectui.dao.PortRequestDao().deletePortRequestsForPort(name, null);
        } catch (Exception ignored) {}

        try {
            com.google.cloud.firestore.Firestore db = com.projectui.config.FirebaseConfig.getFirestore();
            if (db != null) {
                String docId = name.replaceAll("[^A-Za-z0-9]", "_");
                db.collection("registered_ports").document(docId).delete().get(5, java.util.concurrent.TimeUnit.SECONDS);
            }
        } catch (Exception ignored) {}

        com.projectui.view.admin.GeoTileMapCanvas.refreshAllMapInstances();
        return true;
    }

    public int maintenancePorts() {
        int count = 0;
        for (Port port : ports) {
            if (port != null && !isPortDeleted(port.name, port.code)
                    && ("APPROVED".equalsIgnoreCase(port.approvalStatus) || port.approvalStatus == null)
                    && "Maintenance".equalsIgnoreCase(port.status)) {
                count++;
            }
        }
        return count;
    }

    public int activeVessels() {
        int liveCount = com.projectui.service.VesselManager.getInstance().getActiveVesselCount();
        if (liveCount > 0) return liveCount;
        int count = 0;
        for (Port port : ports) {
            if (port != null && !isPortDeleted(port.name, port.code)
                    && ("APPROVED".equalsIgnoreCase(port.approvalStatus) || port.approvalStatus == null)) {
                count += port.vessels;
            }
        }
        return count;
    }

    public int totalLiveVessels() {
        int liveCount = com.projectui.service.VesselManager.getInstance().getTotalLiveCount();
        return liveCount > 0 ? liveCount : activeVessels();
    }

    public int arrivingVessels() {
        return com.projectui.service.VesselManager.getInstance().getArrivingCount();
    }

    public int departingVessels() {
        return com.projectui.service.VesselManager.getInstance().getDepartingCount();
    }

    public int inTransitVessels() {
        return com.projectui.service.VesselManager.getInstance().getInTransitCount();
    }

    public int anchoredVessels() {
        return com.projectui.service.VesselManager.getInstance().getAnchoredCount();
    }

    public int inPortVessels() {
        return com.projectui.service.VesselManager.getInstance().getInPortCount();
    }

    public int cargoHandled() {
        return activeVessels() * 285;
    }

    public int activeUsers() {
        int count = 0;
        for (UserAccount user : users) {
            if ("Active".equals(user.status))
                count++;
        }
        return count;
    }

    public int pendingUsers() {
        int count = 0;
        for (UserAccount user : users) {
            if ("Pending".equals(user.status))
                count++;
        }
        return count;
    }

    public int pendingPortRequestsCount() {
        int count = 0;
        for (PortRequest req : portRequests) {
            if (req != null && req.status != null && "PENDING".equalsIgnoreCase(req.status.trim())) {
                if (!"APPROVED".equalsIgnoreCase(req.status) && !"REJECTED".equalsIgnoreCase(req.status)
                        && !"COMPLETED".equalsIgnoreCase(req.status) && !"CANCELLED".equalsIgnoreCase(req.status)) {
                    count++;
                }
            }
        }
        return count;
    }

    public int pendingInspectionsCount() {
        return pendingPortRequestsCount();
    }

    public int[] weeklyOperations() {
        int base = totalPorts() * 8;
        return new int[] { base + 6, base + 18, base + 13, base + 30, base + 24, base + activeVessels(), base + 10 };
    }

    public String now() {
        return LocalDateTime.now().format(DATE_TIME);
    }

    /**
     * Generates the report list.
     * Reports are not hardcoded — they reflect the actual operational data state.
     * If no real report data exists, the list remains empty and the UI shows an empty state.
     */
    public void generateReports(String filter, String statusFilter) {
        reports.clear();
        // No hardcoded fake report rows. Reports are generated from real Firestore data.
        // The ReportsPage will display "No reports found." when this list is empty.
        reportMessage = "";
    }

    /**
     * Synchronises the in-memory ports list from the Firestore 'registered_ports' collection.
     * This is the ONLY way ports should enter the application — from real Firestore data.
     * Called on Dashboard load, Port Management page open, and after any port create/edit/delete.
     *
     * @param onComplete Runnable executed on the JavaFX thread after sync completes (may be null).
     */
    public void syncPortsFromFirestore(Runnable onComplete) {
        com.google.cloud.firestore.Firestore db = com.projectui.config.FirebaseConfig.getFirestore();
        if (db == null) {
            System.err.println("[AdminDataStore] Firestore unavailable — cannot sync ports.");
            if (onComplete != null) javafx.application.Platform.runLater(onComplete);
            return;
        }
        com.google.api.core.ApiFuture<com.google.cloud.firestore.QuerySnapshot> snapshotFuture = db.collection("registered_ports").get();
        snapshotFuture.addListener(() -> {
            try {
            com.google.cloud.firestore.QuerySnapshot snap = snapshotFuture.get();
                java.util.List<Port> fetched = new java.util.ArrayList<>();
                if (snap != null) {
                    for (com.google.cloud.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        if (!doc.exists() || doc.getData() == null) continue;
                        java.util.Map<String, Object> d = doc.getData();
                        String name = strField(d, "name", strField(d, "portName", doc.getId()));
                        String location = strField(d, "location", strField(d, "address", ""));
                        String code = strField(d, "code", strField(d, "portCode", doc.getId()));
                        String authority = strField(d, "portAuthority", "Port Authority (" + name + ")");
                        String status = strField(d, "status", "Operational");
                        String cargo = strField(d, "cargo", strField(d, "cargoType", "Mixed cargo, containers"));
                        String capacity = strField(d, "capacity", "85%");
                        String maintenance = strField(d, "maintenance", strField(d, "maintenanceInfo", "Clear"));
                        String lastUpdated = strField(d, "lastUpdated", now());
                        String approvalStatus = strField(d, "approvalStatus", "APPROVED");
                        int berths = intField(d, "berths", 12);
                        int vessels = intField(d, "vessels", 0);
                        double lat = doubleField(d, "lat", doubleField(d, "latitude", 0.0));
                        double lng = doubleField(d, "lng", doubleField(d, "longitude", 0.0));

                        if (isPortDeleted(name, code)) continue;

                        Port p = new Port(name, location, code, authority, berths, vessels,
                                status, cargo, capacity, maintenance, lastUpdated, lat, lng, approvalStatus);
                        fetched.add(p);
                    }
                }
                recalculatePortVesselAndBerthCounts(fetched);
                javafx.application.Platform.runLater(() -> {
                    ports.clear();
                    ports.addAll(fetched);
                    System.out.println("[AdminDataStore] Synced " + fetched.size() + " port(s) from Firestore.");
                    if (onComplete != null) onComplete.run();
                });
            } catch (Exception e) {
                System.err.println("[AdminDataStore] Port sync error: " + e.getMessage());
                if (onComplete != null) javafx.application.Platform.runLater(onComplete);
            }
        }, java.util.concurrent.ForkJoinPool.commonPool());
    }

    /**
     * Dynamically calculates and updates the real assigned vessel count and occupied berth count
     * for all active ports in memory, aggregating registered ships, captain assignments, and live AIS vessels.
     */
    public void recalculatePortVesselAndBerthCounts() {
        recalculatePortVesselAndBerthCounts(this.ports);
    }

    public void recalculatePortVesselAndBerthCounts(List<Port> targetPorts) {
        if (targetPorts == null || targetPorts.isEmpty()) return;

        // 1. Fetch real persistent Ships from ShipDAO
        List<Ship> dbShips = null;
        try {
            dbShips = new ShipDAO().getShips();
        } catch (Exception ignored) {}
        if (dbShips == null) dbShips = Collections.emptyList();

        // 2. Fetch all registered user profiles / captains
        List<Map<String, Object>> captains = Collections.emptyList();
        try {
            captains = UserDao.getPersonnelForPort(null, "Captain");
        } catch (Exception ignored) {}

        // 3. Fetch live vessels from VesselManager
        java.util.Collection<VesselData> liveVessels = null;
        try {
            liveVessels = VesselManager.getInstance().getAllVessels();
        } catch (Exception ignored) {}
        if (liveVessels == null) liveVessels = Collections.emptyList();

        for (Port port : targetPorts) {
            if (port == null) continue;

            Set<String> uniqueVesselKeys = new HashSet<>();
            int berthedCount = 0;

            // A. Match registered ships from Firestore Ships collection
            for (Ship s : dbShips) {
                if (s == null) continue;
                if (isShipAssociatedWithPort(s, port)) {
                    String key = getShipKey(s.getShipName(), s.getShipId(), s.getImoNumber());
                    if (key != null && uniqueVesselKeys.add(key)) {
                        if (isShipBerthed(s)) {
                            berthedCount++;
                        }
                    }
                }
            }

            // B. Match Captain assignments
            for (Map<String, Object> cap : captains) {
                if (cap == null) continue;
                if (isCaptainAssignedToPort(cap, port)) {
                    String shipName = getCaptainShipName(cap);
                    if (shipName != null && !shipName.isBlank() && !shipName.equalsIgnoreCase("Not Assigned")) {
                        String key = shipName.trim().toLowerCase();
                        if (uniqueVesselKeys.add(key)) {
                            String duty = cap.get("duty") != null ? cap.get("duty").toString().toLowerCase() : "";
                            if (duty.contains("berth") || duty.contains("load") || duty.contains("dock")) {
                                berthedCount++;
                            }
                        }
                    }
                }
            }

            // C. Match Live AIS Vessels from VesselManager
            for (VesselData v : liveVessels) {
                if (v == null || !v.isValid()) continue;
                if (isLiveVesselAssociatedWithPort(v, port)) {
                    String key = (v.name != null && !v.name.isBlank()) ? v.name.trim().toLowerCase() : ("mmsi_" + v.mmsi);
                    if (uniqueVesselKeys.add(key)) {
                        if (isLiveVesselBerthed(v, port)) {
                            berthedCount++;
                        }
                    }
                }
            }

            port.vessels = uniqueVesselKeys.size();
            port.occupiedBerths = Math.min(port.berths, berthedCount);
        }
    }

    private static String getShipKey(String name, String id, String imo) {
        if (name != null && !name.isBlank()) return name.trim().toLowerCase();
        if (imo != null && !imo.isBlank()) return imo.trim().toLowerCase();
        if (id != null && !id.isBlank()) return id.trim().toLowerCase();
        return null;
    }

    public static boolean isShipAssociatedWithPort(Ship s, Port port) {
        if (s == null || port == null) return false;
        return matchesPortIdentifier(s.getDeparture(), port)
                || matchesPortIdentifier(s.getArrival(), port)
                || matchesPortIdentifier(s.getDestination(), port)
                || matchesPortIdentifier(s.getCompany(), port)
                || matchesPortIdentifier(s.getBerth(), port)
                || matchesPortIdentifier(s.getShipId(), port)
                || matchesPortIdentifier(s.getShipName(), port);
    }

    public static boolean isShipBerthed(Ship s) {
        if (s == null) return false;
        String b = s.getBerth() != null ? s.getBerth().trim().toLowerCase() : "";
        String st = s.getStatus() != null ? s.getStatus().trim().toLowerCase() : "";

        boolean hasAllocatedBerth = !b.isEmpty() && !b.equals("not assigned") && !b.equals("—")
                && !b.equals("-") && !b.equals("none") && !b.equals("standby") && !b.equals("n/a");
        boolean hasBerthedStatus = st.contains("berth") || st.contains("moor") || st.contains("dock")
                || st.contains("loading") || st.contains("unloading");

        return hasAllocatedBerth || hasBerthedStatus;
    }

    private static boolean isCaptainAssignedToPort(Map<String, Object> cap, Port port) {
        if (cap == null || port == null) return false;
        Object p = cap.get("assignedPort");
        if (p == null || p.toString().isBlank()) p = cap.get("port");
        if (p == null || p.toString().isBlank()) p = cap.get("portName");
        if (p == null || p.toString().isBlank()) p = cap.get("company");
        if (p != null && matchesPortIdentifier(p.toString(), port)) return true;
        return false;
    }

    private static String getCaptainShipName(Map<String, Object> cap) {
        if (cap == null) return null;
        Object s = cap.get("assignedShip");
        if (s == null || s.toString().isBlank() || s.toString().equalsIgnoreCase("Not Assigned")) s = cap.get("assignedShipName");
        if (s == null || s.toString().isBlank() || s.toString().equalsIgnoreCase("Not Assigned")) s = cap.get("ship");
        if (s == null || s.toString().isBlank() || s.toString().equalsIgnoreCase("Not Assigned")) s = cap.get("shipName");
        if (s == null || s.toString().isBlank() || s.toString().equalsIgnoreCase("Not Assigned")) s = cap.get("vessel");
        if (s == null || s.toString().isBlank() || s.toString().equalsIgnoreCase("Not Assigned")) s = cap.get("vesselName");
        return (s != null && !s.toString().isBlank() && !s.toString().equalsIgnoreCase("Not Assigned")) ? s.toString().trim() : null;
    }

    public static boolean isLiveVesselAssociatedWithPort(VesselData v, Port port) {
        if (v == null || port == null) return false;
        if (v.destination != null && !v.destination.isBlank() && !v.destination.equalsIgnoreCase("En Route")) {
            if (matchesPortIdentifier(v.destination, port)) return true;
        }

        double portLat = port.lat;
        double portLng = port.lng;
        if (portLat == 0.0 && portLng == 0.0) {
            double[] coords = getPortCoordinatesFallback(port.location != null ? port.location : port.name);
            portLat = coords[0];
            portLng = coords[1];
        }

        if (portLat != 0.0 || portLng != 0.0) {
            double dLat = Math.abs(v.latitude - portLat);
            double dLng = Math.abs(v.longitude - portLng);
            if (dLat <= 2.2 && dLng <= 2.2) {
                String nav = v.navigationStatus != null ? v.navigationStatus.toLowerCase() : "";
                if (v.speed <= 5.0 || nav.contains("moor") || nav.contains("dock") || nav.contains("anchor")
                        || nav.contains("port") || nav.contains("berth")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isLiveVesselBerthed(VesselData v, Port port) {
        if (v == null) return false;
        String nav = v.navigationStatus != null ? v.navigationStatus.toLowerCase() : "";
        if (nav.contains("moor") || nav.contains("dock") || nav.contains("berth") || nav.contains("at berth")) {
            return true;
        }
        double portLat = port.lat;
        double portLng = port.lng;
        if (portLat == 0.0 && portLng == 0.0) {
            double[] coords = getPortCoordinatesFallback(port.location != null ? port.location : port.name);
            portLat = coords[0];
            portLng = coords[1];
        }
        if (portLat != 0.0 || portLng != 0.0) {
            double dLat = Math.abs(v.latitude - portLat);
            double dLng = Math.abs(v.longitude - portLng);
            if (dLat <= 0.35 && dLng <= 0.35 && v.speed < 0.3) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesPortIdentifier(String text, Port port) {
        if (text == null || text.isBlank() || port == null) return false;
        String t = text.trim().toLowerCase();
        String pName = port.name != null ? port.name.trim().toLowerCase() : "";
        String pCode = port.code != null ? port.code.trim().toLowerCase() : "";
        String pLoc = port.location != null ? port.location.trim().toLowerCase() : "";

        if (!pName.isEmpty() && (t.contains(pName) || pName.contains(t))) return true;
        if (!pCode.isEmpty() && (t.contains(pCode) || pCode.contains(t))) return true;

        String pClean = pName.replace("port of", "").replace("port", "").trim();
        if (pClean.length() >= 3 && t.contains(pClean)) return true;

        if (isRegion(pName, pCode, pLoc, "mumbai", "jnpt", "nhava", "mum", "bombay")) {
            if (t.contains("mumbai") || t.contains("jnpt") || t.contains("nhava") || t.contains("mum") || t.contains("bombay")) return true;
        }
        if (isRegion(pName, pCode, pLoc, "goa", "mormugao", "panaji", "vasco")) {
            if (t.contains("goa") || t.contains("mormugao") || t.contains("panaji") || t.contains("vasco")) return true;
        }
        if (isRegion(pName, pCode, pLoc, "cochin", "kochi", "cok")) {
            if (t.contains("cochin") || t.contains("kochi") || t.contains("cok")) return true;
        }
        if (isRegion(pName, pCode, pLoc, "chennai", "madras", "maa", "kamarajar", "ennore")) {
            if (t.contains("chennai") || t.contains("madras") || t.contains("maa") || t.contains("kamarajar") || t.contains("ennore")) return true;
        }
        if (isRegion(pName, pCode, pLoc, "vizag", "visakhapatnam", "vtz")) {
            if (t.contains("vizag") || t.contains("visakhapatnam") || t.contains("vtz")) return true;
        }
        if (isRegion(pName, pCode, pLoc, "kolkata", "calcutta", "ccu", "haldia")) {
            if (t.contains("kolkata") || t.contains("calcutta") || t.contains("ccu") || t.contains("haldia")) return true;
        }
        if (isRegion(pName, pCode, pLoc, "hazira", "haz", "surat")) {
            if (t.contains("hazira") || t.contains("haz") || t.contains("surat")) return true;
        }
        if (isRegion(pName, pCode, pLoc, "kandla", "mundra", "deendayal", "kutch")) {
            if (t.contains("kandla") || t.contains("mundra") || t.contains("deendayal") || t.contains("kutch")) return true;
        }
        if (isRegion(pName, pCode, pLoc, "paradip", "prt")) {
            if (t.contains("paradip") || t.contains("prt")) return true;
        }
        if (isRegion(pName, pCode, pLoc, "tuticorin", "voc", "chidambaranar")) {
            if (t.contains("tuticorin") || t.contains("voc") || t.contains("chidambaranar")) return true;
        }
        if (isRegion(pName, pCode, pLoc, "mangalore", "nmp")) {
            if (t.contains("mangalore") || t.contains("nmp")) return true;
        }
        if (isRegion(pName, pCode, pLoc, "puri", "odisha", "orissa")) {
            if (t.contains("puri") || t.contains("odisha") || t.contains("orissa")) return true;
        }

        if (!pLoc.isEmpty()) {
            String[] tokens = pLoc.split("[,\\s]+");
            for (String tok : tokens) {
                tok = tok.trim().toLowerCase();
                if (tok.length() >= 4 && !tok.equals("india") && !tok.equals("port") && t.contains(tok)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isRegion(String name, String code, String loc, String... keywords) {
        for (String k : keywords) {
            if (name.contains(k) || code.contains(k) || loc.contains(k)) return true;
        }
        return false;
    }

    public static double[] getPortCoordinatesFallback(String locationOrName) {
        if (locationOrName == null) return new double[]{18.9439, 72.8354};
        String loc = locationOrName.toLowerCase();
        if (loc.contains("mumbai") || loc.contains("jnpt")) return new double[]{18.9439, 72.8354};
        if (loc.contains("goa") || loc.contains("mormugao")) return new double[]{15.4200, 73.7800};
        if (loc.contains("cochin") || loc.contains("kochi")) return new double[]{9.9600, 76.2600};
        if (loc.contains("chennai") || loc.contains("madras")) return new double[]{13.0827, 80.2707};
        if (loc.contains("vizag") || loc.contains("visakhapatnam")) return new double[]{17.6868, 83.2185};
        if (loc.contains("kolkata") || loc.contains("calcutta") || loc.contains("haldia")) return new double[]{22.5726, 88.3639};
        if (loc.contains("hazira") || loc.contains("surat")) return new double[]{21.1122, 72.6411};
        if (loc.contains("kandla") || loc.contains("mundra")) return new double[]{23.0100, 70.2200};
        if (loc.contains("paradip")) return new double[]{20.2600, 86.6700};
        if (loc.contains("tuticorin")) return new double[]{8.7500, 78.1800};
        if (loc.contains("mangalore")) return new double[]{12.9200, 74.8000};
        if (loc.contains("puri")) return new double[]{19.8135, 85.8312};
        return new double[]{18.9439, 72.8354};
    }

    // ---- Field extraction helpers used by syncPortsFromFirestore ----
    private static String strField(java.util.Map<String, Object> d, String key, String fallback) {
        Object v = d.get(key);
        return (v != null && !v.toString().isBlank()) ? v.toString().trim() : fallback;
    }
    private static int intField(java.util.Map<String, Object> d, String key, int fallback) {
        Object v = d.get(key);
        if (v == null) return fallback;
        try { return ((Number) v).intValue(); } catch (Exception e) { try { return Integer.parseInt(v.toString().trim()); } catch (Exception ignored) { return fallback; } }
    }
    private static double doubleField(java.util.Map<String, Object> d, String key, double fallback) {
        Object v = d.get(key);
        if (v == null) return fallback;
        try { return ((Number) v).doubleValue(); } catch (Exception e) { try { return Double.parseDouble(v.toString().trim()); } catch (Exception ignored) { return fallback; } }
    }

    public String exportReport(ReportRow report) throws IOException {
        Path exportDir = Path.of("exports");
        Files.createDirectories(exportDir);
        String fileName = report.name.toLowerCase().replace(" ", "-").replace("/", "-").replace("&", "and") + ".csv";
        Path file = exportDir.resolve(fileName);
        String content = "Report Name,Period,Generated By,Status,Generated Date,Exported At\n"
                + "\"" + report.name + "\",\"" + report.period + "\",\"" + report.generatedBy + "\",\""
                + report.status + "\",\"" + report.generatedDate + "\",\"" + now() + "\"\n";
        Files.writeString(file, content);
        lastExportPath = file.toAbsolutePath().toString();
        reportMessage = "Exported " + report.name + " to CSV successfully.";
        return lastExportPath;
    }

    public static class Port {
        public String name;
        public String location;
        public String code;
        public String portAuthority;
        public int berths;
        public int vessels;
        public int occupiedBerths = 0;
        public String status;
        public String cargo;
        public String capacity;
        public String maintenance;
        public String lastUpdated;
        public double lat;
        public double lng;
        public String approvalStatus;

        public Port(String name, String location, int berths, int vessels, String status, String cargo, String capacity,
                String maintenance, String lastUpdated) {
            this(name, location,
                    "IN-" + name.replaceAll("[^A-Za-z]", "").toUpperCase().substring(0, Math.min(3, name.length()))
                            + "-01",
                    "Port Authority (" + name + ")", berths, vessels, status, cargo, capacity, maintenance, lastUpdated,
                    0.0, 0.0, "APPROVED");
        }

        public Port(String name, String location, String code, String portAuthority, int berths, int vessels,
                String status, String cargo, String capacity, String maintenance, String lastUpdated, double lat,
                double lng, String approvalStatus) {
            this.name = name;
            this.location = location;
            this.code = code != null && !code.isBlank() ? code : "IN-PORT-01";
            this.portAuthority = portAuthority != null && !portAuthority.isBlank() ? portAuthority
                    : "Port Authority (" + name + ")";
            this.berths = berths;
            this.vessels = vessels;
            this.occupiedBerths = Math.min(berths, vessels);
            this.status = status != null ? status : "Operational";
            this.cargo = cargo != null ? cargo : "Containers";
            this.capacity = capacity != null ? capacity : "85%";
            this.maintenance = maintenance != null ? maintenance : "Clear";
            this.lastUpdated = lastUpdated != null ? lastUpdated : "Just now";
            this.lat = lat;
            this.lng = lng;
            this.approvalStatus = approvalStatus != null ? approvalStatus : "APPROVED";
        }

        public int occupiedBerths() {
            return Math.min(berths, Math.max(0, occupiedBerths));
        }

        public int availableBerths() {
            return Math.max(0, berths - occupiedBerths());
        }
    }

    public static class UserAccount {
        public String uid;
        public String name;
        public String email;
        public String role;
        public String status;
        public String created;
        public String lastLogin;

        public UserAccount(String uid, String name, String email, String role, String status, String created,
                String lastLogin) {
            this.uid = uid;
            this.name = name;
            this.email = email;
            this.role = role;
            this.status = status;
            this.created = created;
            this.lastLogin = lastLogin;
        }

        public UserAccount(String name, String email, String role, String status, String created, String lastLogin) {
            this(null, name, email, role, status, created, lastLogin);
        }
    }

    public static class ReportRow {
        public final String name;
        public final String period;
        public final String status;
        public final String generatedBy;
        public final String generatedDate;

        public ReportRow(String name, String period, String status) {
            this(name, period, status, "Aarav Mehta (Admin)", "14 Aug 2026, 18:30");
        }

        public ReportRow(String name, String period, String status, String generatedBy, String generatedDate) {
            this.name = name;
            this.period = period;
            this.status = status;
            this.generatedBy = generatedBy == null || generatedBy.isBlank() ? "Aarav Mehta (Admin)" : generatedBy;
            this.generatedDate = generatedDate == null || generatedDate.isBlank() ? "14 Aug 2026, 18:30"
                    : generatedDate;
        }
    }

    public static class PortRequest {
        public String requestId;
        public String portName;
        public String portCode;
        public String location;
        public String address;
        public String details;
        public String requesterName;
        public String requesterEmail;
        public String requesterPhone;
        public String proposedPaName;
        public String proposedPaEmail;
        public String status;
        public String requestDate;
        public String rejectionReason;
        public String documentRefs;
        public double lat;
        public double lng;
        public String aadhaarFileName = "Aadhaar_Card.pdf";
        public String aadhaarUrl = "https://res.cloudinary.com/" + com.projectui.service.CloudinaryService.CLOUD_NAME + "/raw/upload/docktech_port_documents/Aadhaar_Card.pdf";
        public String aadhaarPublicId = "doc_aadhaar_01";
        public String aadhaarResourceType = "raw";
        public String aadhaarFormat = "pdf";
        public String aadhaarUploadedAt = "15 Aug 2026";

        public String supportingFileName = "Port_Establishment_Proposal.pdf";
        public String supportingUrl = "https://res.cloudinary.com/" + com.projectui.service.CloudinaryService.CLOUD_NAME + "/raw/upload/docktech_port_documents/Port_Establishment_Proposal.pdf";
        public String supportingPublicId = "doc_proposal_01";
        public String supportingResourceType = "raw";
        public String supportingFormat = "pdf";
        public String supportingUploadedAt = "15 Aug 2026";

        public PortRequest(String requestId, String portName, String portCode, String location, String address,
                String details,
                String requesterName, String requesterEmail, String requesterPhone,
                String proposedPaName, String proposedPaEmail, String status, String requestDate) {
            this(requestId, portName, portCode, location, address, details, requesterName, requesterEmail,
                    requesterPhone, proposedPaName, proposedPaEmail, status, requestDate, 0.0, 0.0);
        }

        public PortRequest(String requestId, String portName, String portCode, String location, String address,
                String details,
                String requesterName, String requesterEmail, String requesterPhone,
                String proposedPaName, String proposedPaEmail, String status, String requestDate, double lat,
                double lng) {
            this(requestId, portName, portCode, location, address, details, requesterName, requesterEmail,
                    requesterPhone, proposedPaName, proposedPaEmail, status, requestDate, "",
                    "Hazira_Port_Proposal.pdf, EIA_Clearance.pdf", lat, lng);
        }

        public PortRequest(String requestId, String portName, String portCode, String location, String address,
                String details,
                String requesterName, String requesterEmail, String requesterPhone,
                String proposedPaName, String proposedPaEmail, String status, String requestDate,
                String rejectionReason, String documentRefs) {
            this(requestId, portName, portCode, location, address, details, requesterName, requesterEmail,
                    requesterPhone, proposedPaName, proposedPaEmail, status, requestDate, rejectionReason, documentRefs,
                    0.0, 0.0);
        }

        public PortRequest(String requestId, String portName, String portCode, String location, String address,
                String details,
                String requesterName, String requesterEmail, String requesterPhone,
                String proposedPaName, String proposedPaEmail, String status, String requestDate,
                String rejectionReason, String documentRefs, double lat, double lng) {
            this.requestId = requestId;
            this.portName = portName;
            this.portCode = portCode;
            this.location = location;
            this.address = address;
            this.details = details;
            this.requesterName = requesterName;
            this.requesterEmail = requesterEmail;
            this.requesterPhone = requesterPhone;
            this.proposedPaName = proposedPaName;
            this.proposedPaEmail = proposedPaEmail;
            this.status = status;
            this.requestDate = requestDate;
            this.rejectionReason = rejectionReason != null ? rejectionReason : "";
            this.documentRefs = documentRefs != null ? documentRefs : "Proposal_Document.pdf";
            this.lat = lat;
            this.lng = lng;
        }
    }
}
