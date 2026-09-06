package com.projectui.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.cloud.firestore.Firestore;

@SuppressWarnings({"null", "unchecked"})
public class ContainerDao extends Dao {

    private static final Map<String, Map<String, Object>> localBookingStore = new ConcurrentHashMap<>();
    private static int bookingCounter = 2045;
    private static final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();

    static {
        syncFromFirestore();
    }

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
                    com.google.cloud.firestore.QuerySnapshot snap = db.collection("container_bookings").get().get();
                    if (snap != null && !snap.isEmpty()) {
                        for (com.google.cloud.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                            if (doc.exists() && doc.getData() != null) {
                                Map<String, Object> data = new HashMap<>(doc.getData());
                                String orig = (String) data.get("originPort");
                                String pref = (String) data.get("preferredPort");
                                String dest = (String) data.get("destinationPort");
                                if ((orig != null && com.projectui.view.admin.AdminDataStore.isPortDeleted(orig, null))
                                        || (pref != null && com.projectui.view.admin.AdminDataStore.isPortDeleted(pref, null))
                                        || (dest != null && com.projectui.view.admin.AdminDataStore.isPortDeleted(dest, null))) {
                                    continue;
                                }
                                localBookingStore.put(doc.getId(), data);
                                updateCounterFromId(doc.getId());
                                if (data.get("bookingId") != null) {
                                    updateCounterFromId(data.get("bookingId").toString());
                                }
                            }
                        }
                        notifyListeners();
                    }

                    // Attach real-time snapshot listener
                    db.collection("container_bookings").addSnapshotListener((snapshot, err) -> {
                        if (snapshot != null && !snapshot.isEmpty()) {
                            for (com.google.cloud.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                                if (doc.exists() && doc.getData() != null) {
                                    Map<String, Object> data = new HashMap<>(doc.getData());
                                    String orig = (String) data.get("originPort");
                                    String pref = (String) data.get("preferredPort");
                                    String dest = (String) data.get("destinationPort");
                                    if ((orig != null && com.projectui.view.admin.AdminDataStore.isPortDeleted(orig, null))
                                            || (pref != null && com.projectui.view.admin.AdminDataStore.isPortDeleted(pref, null))
                                            || (dest != null && com.projectui.view.admin.AdminDataStore.isPortDeleted(dest, null))) {
                                        continue;
                                    }
                                    localBookingStore.put(doc.getId(), data);
                                    updateCounterFromId(doc.getId());
                                    if (data.get("bookingId") != null) {
                                        updateCounterFromId(data.get("bookingId").toString());
                                    }
                                }
                            }
                            notifyListeners();
                        }
                    });
                }
            } catch (Exception ignored) {}
        });
    }

    private static synchronized void updateCounterFromId(String idStr) {
        if (idStr != null && idStr.trim().startsWith("CONT-")) {
            try {
                int num = Integer.parseInt(idStr.trim().substring(5).trim());
                if (num >= bookingCounter) {
                    bookingCounter = num + 1;
                }
            } catch (Exception ignored) {}
        }
    }

    public synchronized String generateBookingId() {
        for (String k : localBookingStore.keySet()) {
            updateCounterFromId(k);
        }
        for (Map<String, Object> b : localBookingStore.values()) {
            if (b != null && b.get("bookingId") != null) {
                updateCounterFromId(b.get("bookingId").toString());
            }
        }
        while (localBookingStore.containsKey("CONT-" + bookingCounter)) {
            bookingCounter++;
        }
        return "CONT-" + (bookingCounter++);
    }

    public CompletableFuture<Map<String, Object>> saveBooking(String bookingId, String cargoId, String cargoName,
            String containerType, String quantity, String preferredPort, String origin, String destination, String status) {
        return saveBooking(bookingId, cargoId, cargoName, containerType, quantity, preferredPort, origin, destination, 0.0, status, "Exporter", "");
    }

    public CompletableFuture<Map<String, Object>> saveBooking(String bookingId, String cargoId, String cargoName,
            String containerType, String quantity, String preferredPort, String origin, String destination, double totalAmount, String status) {
        return saveBooking(bookingId, cargoId, cargoName, containerType, quantity, preferredPort, origin, destination, totalAmount, status, "Exporter", "");
    }

    public CompletableFuture<Map<String, Object>> saveBooking(String bookingId, String cargoId, String cargoName,
            String containerType, String quantity, String preferredPort, String origin, String destination, double totalAmount, String status,
            String customerType, String exporterName) {

        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        try {
            final String finalBookingId = (bookingId == null || bookingId.isBlank()) ? generateBookingId() : bookingId;

            Map<String, Object> bookingData = localBookingStore.getOrDefault(finalBookingId, new HashMap<>());
            bookingData.put("bookingId", finalBookingId);
            bookingData.put("cargoId", cargoId != null ? cargoId : "");
            bookingData.put("cargoName", cargoName != null ? cargoName : "Commercial Cargo");
            bookingData.put("containerType", containerType != null ? containerType : "20 ft Standard Dry");
            bookingData.put("quantity", quantity != null ? quantity : "1");
            bookingData.put("preferredPort", preferredPort != null ? preferredPort : "");
            bookingData.put("originPort", origin != null ? origin : "");
            bookingData.put("destinationPort", destination != null ? destination : "");
            bookingData.put("totalAmount", totalAmount > 0 ? totalAmount : 0.0);
            bookingData.put("status", status != null && !status.isBlank() ? status : "Booked");
            bookingData.put("customerType", customerType != null ? customerType : "Exporter");
            bookingData.put("exporter", exporterName != null ? exporterName : "");

            if (com.projectui.view.login.LoginPage.loggedInUser != null) {
                Map<String, Object> u = com.projectui.view.login.LoginPage.loggedInUser;
                Object uid = u.get("uid");
                Object email = u.get("email");
                Object name = u.get("name") != null ? u.get("name") : u.get("fullName");
                Object comp = u.get("company") != null ? u.get("company") : u.get("organization");

                if (uid != null && !uid.toString().isBlank() && !bookingData.containsKey("ownerUid")) {
                    bookingData.put("ownerUid", uid.toString());
                    bookingData.put("exporterUid", uid.toString());
                    bookingData.put("userId", uid.toString());
                }
                if (email != null && !email.toString().isBlank() && !bookingData.containsKey("exporterEmail")) {
                    bookingData.put("ownerEmail", email.toString());
                    bookingData.put("exporterEmail", email.toString());
                }
                if (name != null && !name.toString().isBlank()) {
                    bookingData.put("exporterName", name.toString());
                    bookingData.put("exporter", name.toString());
                }
                if (comp != null && !comp.toString().isBlank()) {
                    bookingData.put("company", comp.toString());
                }
            }

            if (!bookingData.containsKey("createdAt")) {
                bookingData.put("createdAt", LocalDateTime.now().toString());
            }

            // Save locally
            localBookingStore.put(finalBookingId, bookingData);
            System.out.println("[CONTAINER DAO] Saved booking locally: " + finalBookingId + " for Cargo ID: " + cargoId);
            notifyListeners();

            // Attempt Firestore save
            try {
                Firestore db = db();
                if (db != null) {
                    com.google.api.core.ApiFuture<com.google.cloud.firestore.WriteResult> apiFuture = 
                            db.collection("container_bookings").document(finalBookingId).set(bookingData);

                    apiFuture.addListener(() -> {
                        try {
                            apiFuture.get();
                            System.out.println("[CONTAINER DAO] Saved booking to Firestore: " + finalBookingId);
                            future.complete(bookingData);
                        } catch (Exception ex) {
                            System.err.println("[CONTAINER DAO] Firestore write warning: " + ex.getMessage());
                            future.complete(bookingData);
                        }
                    }, Runnable::run);
                } else {
                    future.complete(bookingData);
                }
            } catch (Exception ex) {
                System.err.println("[CONTAINER DAO] Firestore save warning: " + ex.getMessage());
                future.complete(bookingData);
            }
        } catch (Exception ex) {
            System.err.println("[CONTAINER DAO] Booking save failed: " + ex.getMessage());
            future.completeExceptionally(ex);
        }
        return future;
    }

    public Map<String, Object> getBookingByCargoId(String cargoId) {
        if (cargoId == null) return null;
        for (Map<String, Object> booking : localBookingStore.values()) {
            if (cargoId.equalsIgnoreCase((String) booking.get("cargoId"))) {
                return booking;
            }
        }
        try {
            Firestore db = db();
            if (db != null) {
                com.google.cloud.firestore.QuerySnapshot snap = db.collection("container_bookings")
                        .whereEqualTo("cargoId", cargoId).get().get();
                if (snap != null && !snap.isEmpty()) {
                    for (com.google.cloud.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        if (doc.exists() && doc.getData() != null) {
                            Map<String, Object> data = new HashMap<>(doc.getData());
                            localBookingStore.put(doc.getId(), data);
                            return data;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public List<Map<String, Object>> getContainersForExporter(Map<String, Object> exporterUser) {
        List<Map<String, Object>> res = new ArrayList<>();
        if (exporterUser == null || exporterUser.isEmpty()) {
            return res;
        }

        for (Map<String, Object> b : localBookingStore.values()) {
            if (b == null) continue;
            if (isContainerAssignedToExporter(b, exporterUser)) {
                res.add(b);
            }
        }
        Collections.reverse(res);
        return res;
    }

    public List<Map<String, Object>> getContainersForImporter(Map<String, Object> importerUser) {
        List<Map<String, Object>> res = new ArrayList<>();
        if (importerUser == null || importerUser.isEmpty()) {
            return res;
        }

        for (Map<String, Object> b : localBookingStore.values()) {
            if (b == null) continue;
            if (isContainerAssignedToImporter(b, importerUser)) {
                res.add(b);
            }
        }
        Collections.reverse(res);
        return res;
    }

    public static boolean isContainerAssignedToImporter(Map<String, Object> b, Map<String, Object> u) {
        if (b == null || u == null || u.isEmpty()) return false;

        String uUid = getTrimmedLower(u, "uid", "userId", "id");
        String uEmail = getTrimmedLower(u, "email");
        String uName = getTrimmedLower(u, "name", "fullName", "displayName");
        String uComp = getTrimmedLower(u, "company", "organization");

        String bImpUid = getTrimmedLower(b, "importerUid", "importerId", "importerUserId", "ownerUid", "userId");
        if (!bImpUid.isEmpty() && !uUid.isEmpty() && (bImpUid.equals(uUid) || bImpUid.equalsIgnoreCase(uUid))) {
            return true;
        }

        String bImpEmail = getTrimmedLower(b, "importerEmail", "ownerEmail", "email", "userEmail");
        if (!bImpEmail.isEmpty() && !uEmail.isEmpty() && (bImpEmail.equals(uEmail) || bImpEmail.equalsIgnoreCase(uEmail))) {
            return true;
        }

        String bImpName = getTrimmedLower(b, "importerName", "importer", "assignedImporter", "consignee");
        if (!bImpName.isEmpty()) {
            if (!uName.isEmpty() && (bImpName.equals(uName) || bImpName.contains(uName) || uName.contains(bImpName))) return true;
            if (!uComp.isEmpty() && (bImpName.equals(uComp) || bImpName.contains(uComp) || uComp.contains(bImpName))) return true;
        }

        return false;
    }

    public static boolean isContainerAssignedToExporter(Map<String, Object> b, Map<String, Object> u) {
        if (b == null || u == null || u.isEmpty()) return false;

        String uUid = getTrimmedLower(u, "uid", "userId", "id");
        String uEmail = getTrimmedLower(u, "email");
        String bOwnerUid = getTrimmedLower(b, "ownerUid", "exporterUid", "exporterId", "userId", "createdBy");

        if (!bOwnerUid.isEmpty()) {
            return !uUid.isEmpty() && (bOwnerUid.equals(uUid) || bOwnerUid.equalsIgnoreCase(uUid));
        }

        if (!uEmail.isEmpty()) {
            String bEmail = getTrimmedLower(b, "ownerEmail", "exporterEmail", "email", "userEmail");
            if (!bEmail.isEmpty() && (bEmail.equals(uEmail) || bEmail.equalsIgnoreCase(uEmail))) return true;
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

    public List<Map<String, Object>> getAllBookings() {
        List<Map<String, Object>> list = new ArrayList<>(localBookingStore.values());
        Collections.reverse(list);
        return list;
    }

    /**
     * Permanently deletes a single container booking from both local in-memory store
     * and Firestore. Does NOT affect any other booking records.
     */
    public boolean deleteBooking(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) return false;
        String cleanId = bookingId.trim();
        List<String> keysToRemove = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : localBookingStore.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(cleanId)) {
                keysToRemove.add(entry.getKey());
                continue;
            }
            Map<String, Object> data = entry.getValue();
            if (data != null) {
                Object bId = data.get("bookingId");
                Object cId = data.get("cargoId");
                Object id = data.get("id");
                Object docId = data.get("docId");
                if ((bId != null && bId.toString().trim().equalsIgnoreCase(cleanId)) ||
                    (cId != null && cId.toString().trim().equalsIgnoreCase(cleanId)) ||
                    (id != null && id.toString().trim().equalsIgnoreCase(cleanId)) ||
                    (docId != null && docId.toString().trim().equalsIgnoreCase(cleanId))) {
                    keysToRemove.add(entry.getKey());
                }
            }
        }
        for (String k : keysToRemove) {
            localBookingStore.remove(k);
        }
        localBookingStore.remove(cleanId);
        notifyListeners();
        try {
            Firestore db = db();
            if (db != null) {
                for (String k : keysToRemove) {
                    db.collection("container_bookings").document(k).delete();
                }
                db.collection("container_bookings").document(cleanId).delete();
            }
        } catch (Exception ignored) {}
        return true;
    }

    public boolean deleteBookingForCargo(String cargoId) {
        if (cargoId == null || cargoId.isBlank()) return false;
        List<String> bookingIds = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : localBookingStore.entrySet()) {
            Map<String, Object> data = entry.getValue();
            if (data != null && cargoId.equalsIgnoreCase(String.valueOf(data.get("cargoId")))) {
                bookingIds.add(entry.getKey());
            }
        }
        for (String bookingId : bookingIds) deleteBooking(bookingId);
        return !bookingIds.isEmpty();
    }

    public synchronized void deleteBookingsForPort(String portName, String portCode) {
        if (portName == null && portCode == null) return;
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : localBookingStore.entrySet()) {
            Map<String, Object> b = entry.getValue();
            if (b != null && isBookingAssociatedWithPort(b, portName, portCode)) {
                toRemove.add(entry.getKey());
            }
        }
        for (String id : toRemove) {
            localBookingStore.remove(id);
            try {
                Firestore db = db();
                if (db != null) {
                    db.collection("container_bookings").document(id).delete();
                }
            } catch (Exception ignored) {}
        }
        if (!toRemove.isEmpty()) {
            notifyListeners();
        }
    }

    public static boolean isBookingAssociatedWithPort(Map<String, Object> b, String portName, String portCode) {
        if (b == null) return false;
        if (portName == null && portCode == null) return false;

        String pLower = portName != null ? portName.trim().toLowerCase() : "";
        String cLower = portCode != null ? portCode.trim().toLowerCase() : "";

        String bPref = getTrimmedLower(b, "preferredPort", "port");
        String bOrig = getTrimmedLower(b, "originPort", "origin");
        String bDest = getTrimmedLower(b, "destinationPort", "destination");
        String bCurr = getTrimmedLower(b, "currentPort");

        List<String> list = List.of(bPref, bOrig, bDest, bCurr);
        for (String s : list) {
            if (s.isEmpty()) continue;
            if (!pLower.isEmpty() && (s.equals(pLower) || s.contains(pLower) || pLower.contains(s))) return true;
            if (!cLower.isEmpty() && (s.equals(cLower) || s.contains(cLower))) return true;
        }

        if (!pLower.isEmpty()) {
            if (pLower.contains("cochin") || pLower.contains("kochi")) {
                for (String s : list) if (s.contains("cochin") || s.contains("kochi")) return true;
            } else if (pLower.contains("mumbai") || pLower.contains("jnpt")) {
                for (String s : list) if (s.contains("mumbai") || s.contains("jnpt")) return true;
            } else if (pLower.contains("chennai") || pLower.contains("madras")) {
                for (String s : list) if (s.contains("chennai") || s.contains("madras")) return true;
            } else if (pLower.contains("vizag") || pLower.contains("visakhapatnam")) {
                for (String s : list) if (s.contains("vizag") || s.contains("visakhapatnam")) return true;
            } else if (pLower.contains("kolkata") || pLower.contains("calcutta")) {
                for (String s : list) if (s.contains("kolkata") || s.contains("calcutta")) return true;
            }
        }

        return false;
    }

    public Map<String, Object> getBooking(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) return null;
        String clean = bookingId.trim();
        Map<String, Object> found = localBookingStore.get(clean);
        if (found != null) return found;
        for (Map.Entry<String, Map<String, Object>> e : localBookingStore.entrySet()) {
            if (e.getKey().equalsIgnoreCase(clean)) return e.getValue();
            Map<String, Object> b = e.getValue();
            if (b != null) {
                Object id = b.get("bookingId");
                if (id != null && id.toString().trim().equalsIgnoreCase(clean)) return b;
            }
        }
        return null;
    }

    public void syncShipAssignmentForCargo(String cargoId, String shipId, String shipName, String shipImo) {
        if (cargoId == null || cargoId.isBlank()) return;
        for (Map.Entry<String, Map<String, Object>> entry : localBookingStore.entrySet()) {
            Map<String, Object> b = entry.getValue();
            if (b == null) continue;
            String cId = b.get("cargoId") != null ? b.get("cargoId").toString().trim() : "";
            if (cId.equalsIgnoreCase(cargoId.trim()) || entry.getKey().equalsIgnoreCase(cargoId.trim())) {
                b.put("assignedShipId", shipId != null ? shipId : "");
                b.put("assignedShip", shipName != null ? shipName : "");
                b.put("shipName", shipName != null ? shipName : "");
                b.put("shipImo", shipImo != null ? shipImo : "");
                b.put("vessel", shipName != null ? shipName : "");
                // Do NOT automatically set status to "Allocated"! Keep authentic DB workflow status.
                b.put("updatedAt", LocalDateTime.now().toString());

                try {
                    Firestore db = db();
                    if (db != null) {
                        db.collection("container_bookings").document(entry.getKey()).set(b, com.google.cloud.firestore.SetOptions.merge());
                    }
                } catch (Exception ignored) {}
            }
        }
        notifyListeners();
    }
}
