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
                                localCargoStore.put(doc.getId(), new HashMap<>(doc.getData()));
                            }
                        }
                        notifyListeners();
                    }

                    // Also attach snapshot listener for real-time live synchronization
                    db.collection("cargos").addSnapshotListener((snapshot, err) -> {
                        if (snapshot != null && !snapshot.isEmpty()) {
                            for (com.google.cloud.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                                if (doc.exists() && doc.getData() != null) {
                                    localCargoStore.put(doc.getId(), new HashMap<>(doc.getData()));
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
            if (originPort != null) cargoData.put("originPort", originPort.trim());
            if (destinationPort != null) cargoData.put("destinationPort", destinationPort.trim());
            if (priority != null) cargoData.put("priority", priority.trim());
            if (status != null && !status.isBlank()) {
                cargoData.put("status", status);
                cargoData.put("customsStatus", status);
            }
            if (!cargoData.containsKey("paymentStatus")) {
                cargoData.put("paymentStatus", "PAYMENT PENDING");
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

    public boolean updateCustomsClearance(String cargoId, String newCustomsStatus, String officer, String reason) {
        if (cargoId == null) return false;
        Map<String, Object> cargo = localCargoStore.get(cargoId);
        if (cargo == null) return false;

        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
        cargo.put("customsStatus", newCustomsStatus);
        cargo.put("status", newCustomsStatus);
        cargo.put("clearedBy", officer != null ? officer : "Customs Officer");
        cargo.put("clearedAt", nowStr);
        cargo.put("updatedAt", nowStr);
        if (reason != null) cargo.put("clearanceNotes", reason);

        if ("Cleared".equalsIgnoreCase(newCustomsStatus) || "Approved".equalsIgnoreCase(newCustomsStatus)) {
            cargo.put("verificationStatus", "Verified");
            cargo.put("inspectionStatus", "Passed");
        } else if ("Rejected".equalsIgnoreCase(newCustomsStatus)) {
            cargo.put("rejectionReason", reason != null ? reason : "Customs regulatory non-compliance");
        } else if ("Held".equalsIgnoreCase(newCustomsStatus)) {
            cargo.put("holdReason", reason != null ? reason : "Quarantined / regulatory documentation hold");
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
            return getAllCargos();
        }

        String filter = importerIdentifier.trim().toLowerCase();

        for (Map<String, Object> c : localCargoStore.values()) {
            if (c == null) continue;
            String impName = String.valueOf(c.getOrDefault("importerName", c.getOrDefault("importer", ""))).toLowerCase();
            String impEmail = String.valueOf(c.getOrDefault("importerEmail", "")).toLowerCase();
            String impComp = String.valueOf(c.getOrDefault("importerCompany", c.getOrDefault("company", ""))).toLowerCase();
            String consignee = String.valueOf(c.getOrDefault("consignee", "")).toLowerCase();

            if (impName.contains(filter) || impEmail.contains(filter) || impComp.contains(filter) || consignee.contains(filter)
                    || filter.contains(impName) || filter.contains(impEmail) || filter.contains(impComp)) {
                res.add(c);
            }
        }
        Collections.reverse(res);
        return res;
    }
}
