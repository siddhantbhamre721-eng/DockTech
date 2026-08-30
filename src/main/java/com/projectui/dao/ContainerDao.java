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
                                localBookingStore.put(doc.getId(), new HashMap<>(doc.getData()));
                            }
                        }
                        notifyListeners();
                    }

                    // Attach real-time snapshot listener
                    db.collection("container_bookings").addSnapshotListener((snapshot, err) -> {
                        if (snapshot != null && !snapshot.isEmpty()) {
                            for (com.google.cloud.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                                if (doc.exists() && doc.getData() != null) {
                                    localBookingStore.put(doc.getId(), new HashMap<>(doc.getData()));
                                }
                            }
                            notifyListeners();
                        }
                    });
                }
            } catch (Exception ignored) {}
        });
    }

    public synchronized String generateBookingId() {
        return "CONT-" + (bookingCounter++);
    }

    public CompletableFuture<Map<String, Object>> saveBooking(String bookingId, String cargoId, String cargoName,
            String containerType, String quantity, String preferredPort, String origin, String destination, String status) {
        return saveBooking(bookingId, cargoId, cargoName, containerType, quantity, preferredPort, origin, destination, 0.0, status, "Exporter", "Global Logistics Ltd.");
    }

    public CompletableFuture<Map<String, Object>> saveBooking(String bookingId, String cargoId, String cargoName,
            String containerType, String quantity, String preferredPort, String origin, String destination, double totalAmount, String status) {
        return saveBooking(bookingId, cargoId, cargoName, containerType, quantity, preferredPort, origin, destination, totalAmount, status, "Exporter", "Global Logistics Ltd.");
    }

    public CompletableFuture<Map<String, Object>> saveBooking(String bookingId, String cargoId, String cargoName,
            String containerType, String quantity, String preferredPort, String origin, String destination, double totalAmount, String status,
            String customerType, String exporterName) {

        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        try {
            final String finalBookingId = (bookingId == null || bookingId.isBlank()) ? generateBookingId() : bookingId;

            Map<String, Object> bookingData = localBookingStore.getOrDefault(finalBookingId, new HashMap<>());
            bookingData.put("bookingId", finalBookingId);
            bookingData.put("cargoId", cargoId != null ? cargoId : "CGO-1024");
            bookingData.put("cargoName", cargoName != null ? cargoName : "Electronics");
            bookingData.put("containerType", containerType != null ? containerType : "20 ft Standard Dry");
            bookingData.put("quantity", quantity != null ? quantity : "1");
            bookingData.put("preferredPort", preferredPort != null ? preferredPort : "Port of Mumbai (JNPT)");
            bookingData.put("originPort", origin != null ? origin : "Port of Mumbai");
            bookingData.put("destinationPort", destination != null ? destination : "Port of Singapore");
            bookingData.put("totalAmount", totalAmount > 0 ? totalAmount : 0.0);
            bookingData.put("status", status != null && !status.isBlank() ? status : "Booked");
            bookingData.put("customerType", customerType != null ? customerType : "Exporter");
            bookingData.put("exporter", exporterName != null ? exporterName : "Global Logistics Ltd.");
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
        return null;
    }

    public List<Map<String, Object>> getAllBookings() {
        List<Map<String, Object>> list = new ArrayList<>(localBookingStore.values());
        Collections.reverse(list);
        return list;
    }

    public List<Map<String, Object>> getAllContainers() {
        return getAllBookings();
    }
}
