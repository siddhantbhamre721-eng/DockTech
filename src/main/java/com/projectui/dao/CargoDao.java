package com.projectui.dao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.google.cloud.firestore.Firestore;

@SuppressWarnings({"null", "unchecked"})
public class CargoDao extends Dao {

    private static final Map<String, Map<String, Object>> localCargoStore = new ConcurrentHashMap<>();
    private static int cargoCounter = 1027;

    static {
        syncFromFirestore();
    }

    private static final List<Runnable> changeListeners = new java.util.concurrent.CopyOnWriteArrayList<>();

    public static void addChangeListener(Runnable r) {
        if (r != null && !changeListeners.contains(r)) {
            changeListeners.add(r);
        }
    }

    public static void notifyListeners() {
        for (Runnable r : changeListeners) {
            try {
                javafx.application.Platform.runLater(r);
            } catch (Exception ex) {
                try { r.run(); } catch (Exception ignored) {}
            }
        }
    }

    public static void syncFromFirestore() {
        java.util.concurrent.ForkJoinPool.commonPool().execute(() -> {
            try {
                Firestore db = com.projectui.config.FirebaseConfig.getFirestore();
                if (db != null) {
                    com.google.cloud.firestore.QuerySnapshot snap = db.collection("cargos").get().get();
                    if (snap != null && !snap.isEmpty()) {
                        for (com.google.cloud.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                            if (doc.exists() && doc.getData() != null) {
                                Map<String, Object> data = new HashMap<>(doc.getData());
                                String origin = (String) data.get("originPort");
                                String dest = (String) data.get("destinationPort");
                                if ((origin != null && com.projectui.view.admin.AdminDataStore.isPortDeleted(origin, null))
                                        || (dest != null && com.projectui.view.admin.AdminDataStore.isPortDeleted(dest, null))) {
                                    continue;
                                }
                                localCargoStore.put(doc.getId(), data);
                            }
                        }
                        notifyListeners();
                    }

                    // Also attach snapshot listener for real-time live synchronization
                    db.collection("cargos").addSnapshotListener((snapshot, err) -> {
                        if (snapshot != null && !snapshot.isEmpty()) {
                            for (com.google.cloud.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                                if (doc.exists() && doc.getData() != null) {
                                    Map<String, Object> data = new HashMap<>(doc.getData());
                                    String origin = (String) data.get("originPort");
                                    String dest = (String) data.get("destinationPort");
                                    if ((origin != null && com.projectui.view.admin.AdminDataStore.isPortDeleted(origin, null))
                                            || (dest != null && com.projectui.view.admin.AdminDataStore.isPortDeleted(dest, null))) {
                                        continue;
                                    }
                                    localCargoStore.put(doc.getId(), data);
                                }
                            }
                            notifyListeners();
                        }
                    });
                }
            } catch (Exception ignored) {}
        });
    }

    public synchronized String generateCargoId() {
        return "CGO-" + (cargoCounter++);
    }

    public CompletableFuture<Map<String, Object>> saveCargo(String cargoId, String cargoName, String cargoType,
            String quantity, String weight, String originPort, String destinationPort, String status) {
        return saveCargo(cargoId, cargoName, cargoType, quantity, weight, "0", originPort, destinationPort, "Normal", status);
    }

    public CompletableFuture<Map<String, Object>> saveCargo(String cargoId, String cargoName, String cargoType,
            String quantity, String weight, String value, String originPort, String destinationPort, String priority, String status) {
        
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        try {
            if (cargoId == null || cargoId.isBlank()) {
                cargoId = generateCargoId();
            }

            Map<String, Object> cargoData = localCargoStore.getOrDefault(cargoId, new HashMap<>());
            cargoData.put("cargoId", cargoId);
            if (cargoName != null) cargoData.put("cargoName", cargoName.trim());
            if (cargoType != null) cargoData.put("cargoType", cargoType.trim());
            if (quantity != null) cargoData.put("quantity", quantity.trim());
            if (weight != null) cargoData.put("weight", weight.trim());
            if (value != null) cargoData.put("value", value.trim());
            if (originPort != null && !originPort.isBlank()) {
                cargoData.put("originPort", originPort.trim());
                if (!cargoData.containsKey("originPortId") || cargoData.get("originPortId") == null || cargoData.get("originPortId").toString().isBlank()) {
                    com.projectui.view.admin.AdminDataStore.Port p = com.projectui.view.admin.AdminDataStore.getInstance().getPortByIdOrName(originPort);
                    if (p != null && p.code != null && !p.code.isBlank()) {
                        cargoData.put("originPortId", p.code);
                    } else {
                        cargoData.put("originPortId", "PORT-" + Math.abs(originPort.hashCode() % 900 + 100));
                    }
                }
            }
            if (destinationPort != null && !destinationPort.isBlank()) {
                cargoData.put("destinationPort", destinationPort.trim());
                if (!cargoData.containsKey("destinationPortId") || cargoData.get("destinationPortId") == null || cargoData.get("destinationPortId").toString().isBlank()) {
                    com.projectui.view.admin.AdminDataStore.Port p = com.projectui.view.admin.AdminDataStore.getInstance().getPortByIdOrName(destinationPort);
                    if (p != null && p.code != null && !p.code.isBlank()) {
                        cargoData.put("destinationPortId", p.code);
                    } else {
                        cargoData.put("destinationPortId", "PORT-" + Math.abs(destinationPort.hashCode() % 900 + 100));
                    }
                }
            }
            if (priority != null) cargoData.put("priority", priority.trim());
            if (status != null && !status.isBlank()) {
                cargoData.put("status", status);
                cargoData.put("customsStatus", status);
            }
            if (!cargoData.containsKey("paymentStatus")) {
                cargoData.put("paymentStatus", "PAYMENT PENDING");
            }

            if (com.projectui.view.login.LoginPage.loggedInUser != null) {
                Map<String, Object> u = com.projectui.view.login.LoginPage.loggedInUser;
                Object uid = u.get("uid");
                Object email = u.get("email");
                Object name = u.get("name") != null ? u.get("name") : u.get("fullName");
                Object comp = u.get("company") != null ? u.get("company") : u.get("organization");

                if (uid != null && !uid.toString().isBlank() && !cargoData.containsKey("ownerUid")) {
                    cargoData.put("ownerUid", uid.toString());
                    cargoData.put("exporterUid", uid.toString());
                    cargoData.put("userId", uid.toString());
                }
                if (email != null && !email.toString().isBlank() && !cargoData.containsKey("exporterEmail")) {
                    cargoData.put("ownerEmail", email.toString());
                    cargoData.put("exporterEmail", email.toString());
                }
                if (name != null && !name.toString().isBlank() && !cargoData.containsKey("exporterName")) {
                    cargoData.put("exporterName", name.toString());
                    cargoData.put("exporter", name.toString());
                }
                if (comp != null && !comp.toString().isBlank() && !cargoData.containsKey("exporterCompany")) {
                    cargoData.put("exporterCompany", comp.toString());
                    cargoData.put("shipper", comp.toString());
                }
            }

            cargoData.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
            if (!cargoData.containsKey("createdAt")) {
                cargoData.put("createdAt", LocalDateTime.now().toString());
            }

            localCargoStore.put(cargoId, cargoData);
            notifyListeners();

            // Attempt Firestore save if connected
            try {
                Firestore db = db();
                if (db != null) {
                    com.google.api.core.ApiFuture<com.google.cloud.firestore.WriteResult> apiFuture = 
                            db.collection("cargos").document(cargoId).set(cargoData, com.google.cloud.firestore.SetOptions.merge());
                    
                    apiFuture.addListener(() -> {
                        try {
                            apiFuture.get();
                            future.complete(cargoData);
                        } catch (Exception ex) {
                            future.complete(cargoData);
                        }
                    }, Runnable::run);
                } else {
                    future.complete(cargoData);
                }
            } catch (Exception ex) {
                future.complete(cargoData);
            }
        } catch (Exception ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }

    public boolean assignShipToCargo(String cargoId, String shipId, String shipName, String shipImo, String assignedBy) {
        return assignShipToCargo(cargoId, shipId, shipName, shipImo, null, null, null, assignedBy);
    }

    public boolean assignShipToCargo(String cargoId, String shipId, String shipName, String shipImo,
                                    String captainUid, String captainName, String captainEmail, String assignedBy) {
        if (cargoId == null || cargoId.isBlank()) return false;
        Map<String, Object> cargo = localCargoStore.get(cargoId);
        if (cargo == null) {
            cargo = new HashMap<>();
            cargo.put("cargoId", cargoId);
            localCargoStore.put(cargoId, cargo);
        }

        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
        
        String cleanShipName = (shipName != null && !shipName.isBlank()) ? shipName.trim() : "Not Assigned";
        String cleanShipId = (shipId != null) ? shipId.trim() : "";
        String cleanShipImo = (shipImo != null) ? shipImo.trim() : "";

        cargo.put("assignedShipId", cleanShipId);
        cargo.put("assignedShip", cleanShipName);
        cargo.put("shipName", cleanShipName);
        cargo.put("shipId", cleanShipId);
        cargo.put("shipImo", cleanShipImo);
        cargo.put("vessel", cleanShipName);
        cargo.put("vesselName", cleanShipName);
        cargo.put("assignedBy", assignedBy != null ? assignedBy : "Port Authority");
        cargo.put("assignedAt", nowStr);
        cargo.put("updatedAt", nowStr);

        // Preserve and advance status cleanly
        String curStatus = cargo.get("status") != null ? cargo.get("status").toString() : "";
        if (curStatus.isBlank() || "Pending".equalsIgnoreCase(curStatus) || "Request Submitted".equalsIgnoreCase(curStatus)
                || "Registered".equalsIgnoreCase(curStatus) || "Pending Assignment".equalsIgnoreCase(curStatus)
                || "Approved".equalsIgnoreCase(curStatus)) {
            cargo.put("status", "Ship Assigned");
        }

        String finalCapUid = captainUid != null ? captainUid.trim() : "";
        String finalCapName = captainName != null ? captainName.trim() : "";
        String finalCapEmail = captainEmail != null ? captainEmail.trim() : "";

        // If captain not explicitly passed, dynamically resolve from registered Captain assigned to this ship
        if (finalCapUid.isBlank() && finalCapEmail.isBlank()) {
            Map<String, Object> captainProf = UserDao.getCaptainForShip(cleanShipName, cleanShipId, cleanShipImo);
            if (captainProf != null) {
                finalCapName = captainProf.get("name") != null ? captainProf.get("name").toString() : (captainProf.get("fullName") != null ? captainProf.get("fullName").toString() : "Ship Captain");
                finalCapUid = captainProf.get("uid") != null ? captainProf.get("uid").toString() : "";
                finalCapEmail = captainProf.get("email") != null ? captainProf.get("email").toString() : "";
            }
        }

        if (!finalCapUid.isBlank() || !finalCapEmail.isBlank() || !finalCapName.isBlank()) {
            cargo.put("assignedCaptain", finalCapName);
            cargo.put("assignedCaptainName", finalCapName);
            cargo.put("captainName", finalCapName);
            cargo.put("assignedCaptainUid", finalCapUid);
            cargo.put("captainUid", finalCapUid);
            cargo.put("assignedCaptainEmail", finalCapEmail);
            cargo.put("captainEmail", finalCapEmail);
            cargo.put("captainStatus", "Assigned");

            // Update Captain's existing user profile in UserDao
            String orig = cargo.get("originPort") != null ? cargo.get("originPort").toString() : "";
            String dest = cargo.get("destinationPort") != null ? cargo.get("destinationPort").toString() : "";
            String dutyStr = "Voyage: " + (!orig.isBlank() ? orig : "Origin") + " ➔ " + (!dest.isBlank() ? dest : "Destination");

            String uidOrEmail = !finalCapUid.isBlank() ? finalCapUid : finalCapEmail;
            UserDao.assignCaptainShipAndDuty(uidOrEmail, cleanShipId, cleanShipName, cleanShipImo, dutyStr, "Active", assignedBy != null ? assignedBy : "Port Authority", "HIGH", "In Transit");

            // Notify Captain
            try {
                com.projectui.model.Notification notif = new com.projectui.model.Notification();
                notif.setUserId(!finalCapUid.isBlank() ? finalCapUid : finalCapEmail);
                notif.setVesselId(cleanShipId);
                notif.setVesselName(cleanShipName);
                notif.setPort(orig);
                notif.setTitle("📦 Cargo & Vessel Assigned");
                notif.setType("CARGO_ASSIGNED");
                notif.setStatus("Active");
                notif.setAssignedBy(assignedBy != null ? assignedBy : "Port Authority");
                notif.setRead(false);
                notif.setTimestamp(NotificationDao.generateTimestamp());
                notif.setMessage("Cargo " + cargoId + " (" + (cargo.get("cargoName") != null ? cargo.get("cargoName") : "Cargo") + ") and Vessel " + cleanShipName + " have been assigned to you.\n\n"
                        + "Route: " + (!orig.isBlank() ? orig : "Origin") + " ➔ " + (!dest.isBlank() ? dest : "Destination") + "\n"
                        + "Weight: " + cargo.getOrDefault("weight", "N/A") + " | Qty: " + cargo.getOrDefault("quantity", "1") + "\n"
                        + "Assigned By: " + (assignedBy != null ? assignedBy : "Port Authority"));
                new NotificationDao().createNotification(notif);
            } catch (Exception ignored) {}
        }

        // Also update any matching container booking in ContainerDao
        try {
            new ContainerDao().syncShipAssignmentForCargo(cargoId, cleanShipId, cleanShipName, cleanShipImo);
        } catch (Exception ignored) {}

        notifyListeners();

        // Sync with Firestore
        try {
            Firestore db = db();
            if (db != null) {
                db.collection("cargos").document(cargoId).set(cargo, com.google.cloud.firestore.SetOptions.merge());
            }
        } catch (Exception ignored) {}

        return true;
    }

    public Map<String, Object> getCargo(String cargoId) {
        if (cargoId == null) return null;
        return localCargoStore.get(cargoId);
    }

    public List<Map<String, Object>> getAllCargos() {
        List<Map<String, Object>> list = new ArrayList<>(localCargoStore.values());
        Collections.reverse(list);
        return list;
    }

    public List<Map<String, Object>> getAllCargo() {
        return getAllCargos();
    }

    public List<Map<String, Object>> getCargosByPort(String portName) {
        List<Map<String, Object>> res = new ArrayList<>();
        if (portName == null || portName.isBlank() || "all ports".equalsIgnoreCase(portName.trim())) {
            return getAllCargos();
        }
        String norm = UserDao.normalizePort(portName).toLowerCase();

        for (Map<String, Object> c : localCargoStore.values()) {
            if (c == null) continue;
            String origin = c.get("originPort") != null ? c.get("originPort").toString() : (c.get("origin") != null ? c.get("origin").toString() : "");
            String dest = c.get("destinationPort") != null ? c.get("destinationPort").toString() : (c.get("destination") != null ? c.get("destination").toString() : "");
            String assigned = c.get("assignedPort") != null ? c.get("assignedPort").toString() : (c.get("port") != null ? c.get("port").toString() : "");

            String oNorm = UserDao.normalizePort(origin).toLowerCase();
            String dNorm = UserDao.normalizePort(dest).toLowerCase();
            String aNorm = UserDao.normalizePort(assigned).toLowerCase();

            if (oNorm.contains(norm) || dNorm.contains(norm) || aNorm.contains(norm) || (origin.isBlank() && dest.isBlank())) {
                res.add(c);
            }
        }
        Collections.reverse(res);
        return res;
    }

    public List<Map<String, Object>> getCargosForCustomsOfficer(String officerUid, String officerEmail, String portName) {
        List<Map<String, Object>> res = new ArrayList<>();
        String uId = officerUid != null ? officerUid.trim().toLowerCase() : "";
        String uEmail = officerEmail != null ? officerEmail.trim().toLowerCase() : "";
        String norm = (portName != null && !portName.isBlank() && !"all ports".equalsIgnoreCase(portName.trim())) 
                ? UserDao.normalizePort(portName).toLowerCase() : "";

        for (Map<String, Object> c : localCargoStore.values()) {
            if (c == null) continue;
            String cOffUid = c.get("assignedCustomsOfficerUid") != null ? c.get("assignedCustomsOfficerUid").toString().trim().toLowerCase() : "";
            String cOffEmail = c.get("assignedCustomsOfficerEmail") != null ? c.get("assignedCustomsOfficerEmail").toString().trim().toLowerCase() : "";

            boolean explicitOfficerMatch = (!uId.isBlank() && cOffUid.equals(uId)) || (!uEmail.isBlank() && cOffEmail.equals(uEmail));

            String origin = c.get("originPort") != null ? c.get("originPort").toString() : (c.get("origin") != null ? c.get("origin").toString() : "");
            String dest = c.get("destinationPort") != null ? c.get("destinationPort").toString() : (c.get("destination") != null ? c.get("destination").toString() : "");
            String assigned = c.get("assignedPort") != null ? c.get("assignedPort").toString() : (c.get("port") != null ? c.get("port").toString() : "");

            String oNorm = UserDao.normalizePort(origin).toLowerCase();
            String dNorm = UserDao.normalizePort(dest).toLowerCase();
            String aNorm = UserDao.normalizePort(assigned).toLowerCase();

            boolean portMatch = norm.isEmpty() || oNorm.contains(norm) || dNorm.contains(norm) || aNorm.contains(norm) || (origin.isBlank() && dest.isBlank());

            if (explicitOfficerMatch || portMatch) {
                res.add(c);
            }
        }
        Collections.reverse(res);
        return res;
    }

    public boolean addDocumentToCargo(String cargoId, Map<String, Object> docData) {
        if (cargoId == null || docData == null) return false;
        Map<String, Object> cargo = localCargoStore.get(cargoId);
        if (cargo == null) return false;

        List<Map<String, Object>> docs = (List<Map<String, Object>>) cargo.get("documents");
        if (docs == null) {
            docs = new ArrayList<>();
            cargo.put("documents", docs);
        }

        String docId = docData.get("docId") != null ? docData.get("docId").toString() : ("DOC-" + System.currentTimeMillis() % 100000);
        docData.putIfAbsent("docId", docId);
        docData.putIfAbsent("status", "Pending");
        docData.putIfAbsent("uploadedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));

        boolean found = false;
        for (int i = 0; i < docs.size(); i++) {
            Map<String, Object> existing = docs.get(i);
            if (docId.equalsIgnoreCase(String.valueOf(existing.get("docId")))) {
                docs.set(i, docData);
                found = true;
                break;
            }
        }
        if (!found) {
            docs.add(docData);
        }

        cargo.put("documents", docs);
        cargo.put("verificationStatus", "Pending");
        cargo.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        notifyListeners();

        try {
            Firestore db = db();
            if (db != null) {
                db.collection("cargos").document(cargoId).set(cargo, com.google.cloud.firestore.SetOptions.merge());
            }
        } catch (Exception ignored) {}

        return true;
    }

    public boolean updateDocumentStatus(String cargoId, String docId, String newStatus, String officer, String notes) {
        if (cargoId == null || docId == null) return false;
        Map<String, Object> cargo = localCargoStore.get(cargoId);
        if (cargo == null) return false;

        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
        List<Map<String, Object>> docs = (List<Map<String, Object>>) cargo.get("documents");
        boolean updated = false;

        if (docs != null) {
            for (Map<String, Object> d : docs) {
                if (docId.equalsIgnoreCase(String.valueOf(d.get("docId")))) {
                    d.put("status", newStatus);
                    d.put("verifiedBy", officer != null ? officer : "Customs Officer");
                    d.put("verifiedAt", nowStr);
                    if (notes != null) d.put("notes", notes);
                    updated = true;
                    break;
                }
            }
        }

        // Check if all docs are verified
        boolean allVerified = true;
        boolean anyRejected = false;
        boolean anyPending = false;
        if (docs != null && !docs.isEmpty()) {
            for (Map<String, Object> d : docs) {
                String st = String.valueOf(d.get("status"));
                if ("Rejected".equalsIgnoreCase(st)) anyRejected = true;
                else if (!"Verified".equalsIgnoreCase(st) && !"Approved".equalsIgnoreCase(st)) allVerified = false;
                if ("Pending".equalsIgnoreCase(st)) anyPending = true;
            }
        }

        if (anyRejected) {
            cargo.put("docStatus", "Rejected");
            cargo.put("verificationStatus", "Rejected");
        } else if (allVerified && docs != null && !docs.isEmpty()) {
            cargo.put("docStatus", "Verified");
            cargo.put("verificationStatus", "Verified");
        } else if (anyPending) {
            cargo.put("docStatus", "Pending Review");
            cargo.put("verificationStatus", "Pending");
        }

        cargo.put("updatedAt", nowStr);

        // Firestore sync
        try {
            Firestore db = db();
            if (db != null) {
                db.collection("cargos").document(cargoId).set(cargo, com.google.cloud.firestore.SetOptions.merge());
            }
        } catch (Exception ignored) {}

        return updated;
    }

    public boolean updateInspectionStatus(String cargoId, String newInspectionStatus, String officer, String notes, String reason) {
        if (cargoId == null) return false;
        Map<String, Object> cargo = localCargoStore.get(cargoId);
        if (cargo == null) return false;

        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
        cargo.put("inspectionStatus", newInspectionStatus);
        cargo.put("inspectedBy", officer != null ? officer : "Customs Officer");
        cargo.put("inspectionDate", nowStr);
        cargo.put("updatedAt", nowStr);
        if (notes != null) cargo.put("inspectionNotes", notes);
        if (reason != null) cargo.put("inspectionReason", reason);

        // If Passed, make eligible for clearance
        if ("Passed".equalsIgnoreCase(newInspectionStatus) || "Cleared".equalsIgnoreCase(newInspectionStatus)) {
            if ("Verified".equalsIgnoreCase(String.valueOf(cargo.get("verificationStatus")))) {
                cargo.put("customsStatus", "Cleared");
                cargo.put("status", "Cleared");
            }
        } else if ("Failed".equalsIgnoreCase(newInspectionStatus) || "Rejected".equalsIgnoreCase(newInspectionStatus)) {
            cargo.put("customsStatus", "Rejected");
            cargo.put("status", "Rejected");
        } else if ("Hold".equalsIgnoreCase(newInspectionStatus) || "Quarantined".equalsIgnoreCase(newInspectionStatus)) {
            cargo.put("customsStatus", "Held");
            cargo.put("status", "Held");
        }

        // Firestore sync
        try {
            Firestore db = db();
            if (db != null) {
                db.collection("cargos").document(cargoId).set(cargo, com.google.cloud.firestore.SetOptions.merge());
            }
        } catch (Exception ignored) {}

        return true;
    }

    public boolean assignCustomsOfficerToCargo(String cargoId, String officerUid, String officerName, String officerEmail, String assignedBy) {
        if (cargoId == null || cargoId.isBlank()) return false;
        Map<String, Object> cargo = localCargoStore.get(cargoId);
        if (cargo == null) {
            cargo = new HashMap<>();
            cargo.put("cargoId", cargoId);
            localCargoStore.put(cargoId, cargo);
        }

        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
        String cleanName = (officerName != null && !officerName.isBlank()) ? officerName.trim() : "Customs Officer";
        String cleanUid = (officerUid != null) ? officerUid.trim() : "";
        String cleanEmail = (officerEmail != null) ? officerEmail.trim() : "";

        cargo.put("assignedCustomsOfficerUid", cleanUid);
        cargo.put("customsOfficerUid", cleanUid);
        cargo.put("assignedCustomsOfficer", cleanName);
        cargo.put("assignedCustomsOfficerName", cleanName);
        cargo.put("customsOfficerName", cleanName);
        cargo.put("assignedCustomsOfficerEmail", cleanEmail);
        cargo.put("customsOfficerEmail", cleanEmail);
        cargo.put("customsOfficerAssignedAt", nowStr);
        cargo.put("customsStatus", "Pending Clearance");
        cargo.put("customsClearanceStatus", "Pending Clearance");
        cargo.put("verificationStatus", "Pending");
        cargo.put("inspectionStatus", "Pending");
        cargo.put("updatedAt", nowStr);

        // Update Customs Officer's user profile in UserDao
        String orig = cargo.get("originPort") != null ? cargo.get("originPort").toString() : "";
        String dest = cargo.get("destinationPort") != null ? cargo.get("destinationPort").toString() : "";
        String dutyStr = "Customs Verification & Clearance: Cargo " + cargoId + " (" + (!orig.isBlank() ? orig : "Origin") + " ➔ " + (!dest.isBlank() ? dest : "Destination") + ")";
        String uidOrEmail = !cleanUid.isBlank() ? cleanUid : cleanEmail;
        UserDao.assignCustomsOfficerDuty(uidOrEmail, dutyStr, cargoId, orig, assignedBy != null ? assignedBy : "Port Authority");

        // Notify Customs Officer
        try {
            com.projectui.model.Notification notif = new com.projectui.model.Notification();
            notif.setUserId(!cleanUid.isBlank() ? cleanUid : cleanEmail);
            notif.setVesselId(cargo.get("assignedShipId") != null ? cargo.get("assignedShipId").toString() : "");
            notif.setVesselName(cargo.get("assignedShip") != null ? cargo.get("assignedShip").toString() : "");
            notif.setPort(orig);
            notif.setTitle("🛃 Cargo Assigned for Customs Clearance");
            notif.setType("CUSTOMS_ASSIGNED");
            notif.setStatus("Active");
            notif.setAssignedBy(assignedBy != null ? assignedBy : "Port Authority");
            notif.setRead(false);
            notif.setTimestamp(NotificationDao.generateTimestamp());
            notif.setMessage("Cargo " + cargoId + " (" + (cargo.get("cargoName") != null ? cargo.get("cargoName") : "Cargo") + ") has been assigned to you for inspection & regulatory clearance.\n\n"
                    + "Route: " + (!orig.isBlank() ? orig : "Origin") + " ➔ " + (!dest.isBlank() ? dest : "Destination") + "\n"
                    + "Assigned Vessel: " + cargo.getOrDefault("assignedShip", "Not Assigned") + " | Captain: " + cargo.getOrDefault("assignedCaptainName", "Not Assigned") + "\n"
                    + "Assigned By: " + (assignedBy != null ? assignedBy : "Port Authority"));
            new NotificationDao().createNotification(notif);
        } catch (Exception ignored) {}

        notifyListeners();

        // Firestore sync
        try {
            Firestore db = db();
            if (db != null) {
                db.collection("cargos").document(cargoId).set(cargo, com.google.cloud.firestore.SetOptions.merge());
            }
        } catch (Exception ignored) {}

        return true;
    }

    public boolean updateCustomsClearance(String cargoId, String newCustomsStatus, String officer, String reason) {
        if (cargoId == null) return false;
        Map<String, Object> cargo = localCargoStore.get(cargoId);
        if (cargo == null) return false;

        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
        cargo.put("customsStatus", newCustomsStatus);
        cargo.put("customsClearanceStatus", newCustomsStatus);
        cargo.put("clearedBy", officer != null ? officer : "Customs Officer");
        cargo.put("clearedAt", nowStr);
        cargo.put("updatedAt", nowStr);
        if (reason != null) cargo.put("clearanceNotes", reason);

        if ("Cleared".equalsIgnoreCase(newCustomsStatus) || "Approved".equalsIgnoreCase(newCustomsStatus)) {
            cargo.put("customsClearanceStatus", "Cleared");
            cargo.put("customsStatus", "Cleared");
            cargo.put("verificationStatus", "Verified");
            cargo.put("inspectionStatus", "Passed");

            // Advance cargo status cleanly if still Ship Assigned or Pending Clearance
            String curSt = cargo.get("status") != null ? cargo.get("status").toString() : "";
            if (curSt.isBlank() || "Ship Assigned".equalsIgnoreCase(curSt) || "Pending Clearance".equalsIgnoreCase(curSt) || "Approved".equalsIgnoreCase(curSt)) {
                cargo.put("status", "Customs Cleared");
            }

            // Notify Assigned Captain that cargo is cleared for loading/departure
            String capUid = cargo.get("assignedCaptainUid") != null ? cargo.get("assignedCaptainUid").toString() : "";
            String capEmail = cargo.get("assignedCaptainEmail") != null ? cargo.get("assignedCaptainEmail").toString() : "";
            if (!capUid.isBlank() || !capEmail.isBlank()) {
                try {
                    com.projectui.model.Notification notif = new com.projectui.model.Notification();
                    notif.setUserId(!capUid.isBlank() ? capUid : capEmail);
                    notif.setVesselId(cargo.get("assignedShipId") != null ? cargo.get("assignedShipId").toString() : "");
                    notif.setVesselName(cargo.get("assignedShip") != null ? cargo.get("assignedShip").toString() : "");
                    notif.setPort(cargo.get("originPort") != null ? cargo.get("originPort").toString() : "");
                    notif.setTitle("✅ Customs Clearance Approved");
                    notif.setType("CUSTOMS_CLEARED");
                    notif.setStatus("Active");
                    notif.setAssignedBy(officer != null ? officer : "Customs Officer");
                    notif.setRead(false);
                    notif.setTimestamp(NotificationDao.generateTimestamp());
                    notif.setMessage("Customs clearance completed. Cargo " + cargoId + " is cleared by " + (officer != null ? officer : "Customs Officer") + " and you may proceed with loading/departure according to the existing workflow.\n\n"
                            + "Vessel: " + cargo.getOrDefault("assignedShip", "Assigned Vessel") + "\n"
                            + "Route: " + cargo.getOrDefault("originPort", "Origin") + " ➔ " + cargo.getOrDefault("destinationPort", "Destination"));
                    new NotificationDao().createNotification(notif);
                } catch (Exception ignored) {}
            }
        } else if ("Rejected".equalsIgnoreCase(newCustomsStatus)) {
            cargo.put("status", "Customs Rejected");
            cargo.put("rejectionReason", reason != null ? reason : "Customs regulatory non-compliance");
        } else if ("Held".equalsIgnoreCase(newCustomsStatus)) {
            cargo.put("status", "Customs Held");
            cargo.put("holdReason", reason != null ? reason : "Quarantined / regulatory documentation hold");
        }

        notifyListeners();

        // Firestore sync
        try {
            Firestore db = db();
            if (db != null) {
                db.collection("cargos").document(cargoId).set(cargo, com.google.cloud.firestore.SetOptions.merge());
            }
        } catch (Exception ignored) {}

        return true;
    }

    public boolean updateCargoStatus(String cargoId, String newStatus) {
        if (cargoId == null) return false;
        Map<String, Object> cargo = localCargoStore.get(cargoId);
        if (cargo == null) {
            cargo = new HashMap<>();
            cargo.put("cargoId", cargoId);
            localCargoStore.put(cargoId, cargo);
        }
        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
        cargo.put("status", newStatus);
        cargo.put("updatedAt", nowStr);
        notifyListeners();

        try {
            Firestore db = db();
            if (db != null) {
                db.collection("cargos").document(cargoId).set(cargo, com.google.cloud.firestore.SetOptions.merge());
            }
        } catch (Exception ignored) {}

        return true;
    }

    public boolean updateCargoPaymentStatus(String cargoId, String paymentStatus, String paymentId, String orderId, double amount, String receiptNo) {
        if (cargoId == null) return false;
        Map<String, Object> cargo = localCargoStore.get(cargoId);
        if (cargo == null) {
            cargo = new HashMap<>();
            cargo.put("cargoId", cargoId);
            localCargoStore.put(cargoId, cargo);
        }
        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
        cargo.put("paymentStatus", paymentStatus != null ? paymentStatus : "PAID");
        if (paymentId != null) cargo.put("razorpayPaymentId", paymentId);
        if (orderId != null) cargo.put("razorpayOrderId", orderId);
        if (amount > 0) cargo.put("paymentAmount", amount);
        if (receiptNo != null) cargo.put("receiptNumber", receiptNo);
        cargo.put("paidAt", nowStr);
        cargo.put("updatedAt", nowStr);
        notifyListeners();

        try {
            Firestore db = db();
            if (db != null) {
                db.collection("cargos").document(cargoId).set(cargo, com.google.cloud.firestore.SetOptions.merge());
            }
        } catch (Exception ignored) {}

        return true;
    }

    public boolean confirmDelivery(String cargoId, String confirmedBy, String notes) {
        if (cargoId == null || cargoId.isBlank()) return false;
        Map<String, Object> cargo = localCargoStore.get(cargoId);
        if (cargo == null) return false;

        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
        cargo.put("status", "Delivered");
        cargo.put("deliveryStatus", "Delivered");
        cargo.put("deliveredAt", nowStr);
        cargo.put("deliveredBy", confirmedBy != null ? confirmedBy : "Importer");
        if (notes != null) cargo.put("deliveryNotes", notes);
        cargo.put("updatedAt", nowStr);
        notifyListeners();

        try {
            Firestore db = db();
            if (db != null) {
                db.collection("cargos").document(cargoId).set(cargo, com.google.cloud.firestore.SetOptions.merge());
            }
        } catch (Exception ignored) {}

        return true;
    }

    public List<Map<String, Object>> getShipmentsForImporter(String importerIdentifier) {
        List<Map<String, Object>> res = new ArrayList<>();
        if (importerIdentifier == null || importerIdentifier.isBlank() || "all".equalsIgnoreCase(importerIdentifier.trim())) {
            return res;
        }

        String filter = importerIdentifier.trim().toLowerCase();

        for (Map<String, Object> c : localCargoStore.values()) {
            if (c == null) continue;
            if (isCargoMatchingImporter(c, filter)) {
                res.add(c);
            }
        }
        Collections.reverse(res);
        return res;
    }

    public List<Map<String, Object>> getShipmentsForImporter(Map<String, Object> importerUser) {
        List<Map<String, Object>> res = new ArrayList<>();
        if (importerUser == null || importerUser.isEmpty()) {
            return res;
        }

        for (Map<String, Object> c : localCargoStore.values()) {
            if (c == null) continue;
            if (isCargoAssignedToImporter(c, importerUser)) {
                res.add(c);
            }
        }
        Collections.reverse(res);
        return res;
    }

    public static boolean isCargoMatchingImporter(Map<String, Object> c, String filter) {
        if (c == null || filter == null || filter.isBlank()) return false;
        String f = filter.trim().toLowerCase();

        String impId = getTrimmedLower(c, "importerId", "importerUid", "importerUserId", "userId");
        if (!impId.isEmpty() && (impId.equals(f) || f.equals(impId))) return true;

        String impEmail = getTrimmedLower(c, "importerEmail");
        if (!impEmail.isEmpty() && (impEmail.equals(f) || impEmail.contains(f) || f.contains(impEmail))) return true;

        String impName = getTrimmedLower(c, "importerName", "importer", "assignedImporter");
        if (!impName.isEmpty() && (impName.equals(f) || impName.contains(f) || (f.length() >= 3 && f.contains(impName)))) return true;

        String impComp = getTrimmedLower(c, "importerCompany");
        if (!impComp.isEmpty() && (impComp.equals(f) || impComp.contains(f) || (f.length() >= 3 && f.contains(impComp)))) return true;

        String consignee = getTrimmedLower(c, "consignee");
        if (!consignee.isEmpty() && (consignee.equals(f) || consignee.contains(f) || (f.length() >= 3 && f.contains(consignee)))) return true;

        return false;
    }

    public static boolean isCargoAssignedToImporter(Map<String, Object> c, Map<String, Object> u) {
        if (c == null || u == null || u.isEmpty()) return false;

        String uUid = getTrimmedLower(u, "uid");
        String uEmail = getTrimmedLower(u, "email");
        String uName = getTrimmedLower(u, "name", "fullName", "displayName");
        String uComp = getTrimmedLower(u, "company", "organization");

        if (!uUid.isEmpty()) {
            String cId = getTrimmedLower(c, "importerId", "importerUid", "importerUserId", "userId");
            if (!cId.isEmpty() && (cId.equals(uUid) || cId.equalsIgnoreCase(uUid))) return true;
        }

        if (!uEmail.isEmpty()) {
            String cEmail = getTrimmedLower(c, "importerEmail");
            if (!cEmail.isEmpty() && (cEmail.equals(uEmail) || cEmail.contains(uEmail) || uEmail.contains(cEmail))) return true;
            String cId = getTrimmedLower(c, "importerId", "importerUid", "importerUserId");
            if (!cId.isEmpty() && (cId.equals(uEmail) || cId.contains(uEmail) || uEmail.contains(cId))) return true;
        }

        if (!uName.isEmpty()) {
            String cName = getTrimmedLower(c, "importerName", "importer", "assignedImporter");
            if (!cName.isEmpty() && (cName.equals(uName) || cName.contains(uName) || (uName.length() >= 3 && uName.contains(cName)))) return true;
            String cCons = getTrimmedLower(c, "consignee");
            if (!cCons.isEmpty() && (cCons.equals(uName) || cCons.contains(uName) || (uName.length() >= 3 && uName.contains(cCons)))) return true;
        }

        if (!uComp.isEmpty()) {
            String cComp = getTrimmedLower(c, "importerCompany");
            if (!cComp.isEmpty() && (cComp.equals(uComp) || cComp.contains(uComp) || (uComp.length() >= 3 && uComp.contains(cComp)))) return true;
            String cName = getTrimmedLower(c, "importerName", "importer", "assignedImporter");
            if (!cName.isEmpty() && (cName.equals(uComp) || cName.contains(uComp) || (uComp.length() >= 3 && uComp.contains(cName)))) return true;
            String cCons = getTrimmedLower(c, "consignee");
            if (!cCons.isEmpty() && (cCons.equals(uComp) || cCons.contains(uComp) || (uComp.length() >= 3 && uComp.contains(cCons)))) return true;
        }

        return false;
    }

    public List<Map<String, Object>> getShipmentsForExporter(Map<String, Object> exporterUser) {
        List<Map<String, Object>> res = new ArrayList<>();
        if (exporterUser == null || exporterUser.isEmpty()) {
            return res;
        }

        for (Map<String, Object> c : localCargoStore.values()) {
            if (c == null) continue;
            if (isCargoAssignedToExporter(c, exporterUser)) {
                res.add(c);
            }
        }
        Collections.reverse(res);
        return res;
    }

    public static boolean isCargoAssignedToExporter(Map<String, Object> c, Map<String, Object> u) {
        if (c == null || u == null || u.isEmpty()) return false;

        String uUid = getTrimmedLower(u, "uid", "userId", "id");
        String uEmail = getTrimmedLower(u, "email");
        String cOwnerUid = getTrimmedLower(c, "ownerUid", "exporterUid", "exporterId", "userId", "createdBy");

        if (!cOwnerUid.isEmpty()) {
            return !uUid.isEmpty() && (cOwnerUid.equals(uUid) || cOwnerUid.equalsIgnoreCase(uUid));
        }

        if (!uEmail.isEmpty()) {
            String cEmail = getTrimmedLower(c, "ownerEmail", "exporterEmail", "email", "userEmail");
            if (!cEmail.isEmpty() && (cEmail.equals(uEmail) || cEmail.equalsIgnoreCase(uEmail))) return true;
        }

        return false;
    }

    public synchronized void deleteCargosForPort(String portName, String portCode) {
        if (portName == null && portCode == null) return;
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : localCargoStore.entrySet()) {
            Map<String, Object> c = entry.getValue();
            if (c != null && isCargoAssociatedWithPort(c, portName, portCode)) {
                toRemove.add(entry.getKey());
            }
        }
        for (String id : toRemove) {
            localCargoStore.remove(id);
            try {
                Firestore db = db();
                if (db != null) {
                    db.collection("cargos").document(id).delete();
                }
            } catch (Exception ignored) {}
        }
        if (!toRemove.isEmpty()) {
            notifyListeners();
        }
    }

    public static boolean isCargoAssociatedWithPort(Map<String, Object> c, String portName, String portCode) {
        if (c == null) return false;
        if (portName == null && portCode == null) return false;

        String pLower = portName != null ? portName.trim().toLowerCase() : "";
        String cLower = portCode != null ? portCode.trim().toLowerCase() : "";

        String pPort = getTrimmedLower(c, "port", "originPort", "destinationPort", "assignedPort");
        String pOrig = getTrimmedLower(c, "originPort", "origin");
        String pDest = getTrimmedLower(c, "destinationPort", "destination");

        List<String> list = List.of(pPort, pOrig, pDest);
        for (String s : list) {
            if (s.isEmpty()) continue;
            if (!pLower.isEmpty() && (s.equals(pLower) || s.contains(pLower) || pLower.contains(s))) return true;
            if (!cLower.isEmpty() && (s.equals(cLower) || s.contains(cLower))) return true;
        }
        return false;
    }

    private static String getTrimmedLower(Map<String, Object> map, String... keys) {
        if (map == null) return "";
        for (String k : keys) {
            Object v = map.get(k);
            if (v != null && !v.toString().isBlank()) {
                return v.toString().trim().toLowerCase();
            }
        }
        return "";
    }
}
