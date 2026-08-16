package com.projectui.dao;

import com.google.cloud.firestore.Firestore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ContainerDao extends Dao {

    private static final Map<String, Map<String, Object>> localBookingStore = new ConcurrentHashMap<>();
    private static int bookingCounter = 2045;

    public synchronized String generateBookingId() {
        return "CONT-" + (bookingCounter++);
    }

    public CompletableFuture<Map<String, Object>> saveBooking(String bookingId, String cargoId, String cargoName,
            String containerType, String quantity, String preferredPort, String origin, String destination, String status) {

        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        try {
            final String finalBookingId = (bookingId == null || bookingId.isBlank()) ? generateBookingId() : bookingId;

            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("bookingId", finalBookingId);
            bookingData.put("cargoId", cargoId != null ? cargoId : "CGO-1024");
            bookingData.put("cargoName", cargoName != null ? cargoName : "Electronics");
            bookingData.put("containerType", containerType != null ? containerType : "20 ft Standard Dry");
            bookingData.put("quantity", quantity != null ? quantity : "1");
            bookingData.put("preferredPort", preferredPort != null ? preferredPort : "Port of Mumbai (JNPT)");
            bookingData.put("originPort", origin != null ? origin : "Port of Mumbai");
            bookingData.put("destinationPort", destination != null ? destination : "Port of Singapore");
            bookingData.put("status", status != null && !status.isBlank() ? status : "Booked");
            bookingData.put("createdAt", LocalDateTime.now().toString());

            // Save locally
            localBookingStore.put(finalBookingId, bookingData);
            System.out.println("[CONTAINER DAO] Saved booking locally: " + finalBookingId + " for Cargo ID: " + cargoId);

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
}
