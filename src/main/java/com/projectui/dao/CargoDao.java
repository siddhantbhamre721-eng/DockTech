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

public class CargoDao extends Dao {

    private static final Map<String, Map<String, Object>> localCargoStore = new ConcurrentHashMap<>();
    private static int cargoCounter = 1027;

    static {
        // Initial Seed Records for display consistency
        seedCargo("CGO-1024", "Electronics", "General Cargo", "500 units", "1,200 kg", "Port of Mumbai", "Port of Singapore", "Registered");
        seedCargo("CGO-1025", "Machinery", "Machinery", "120 units", "3,400 kg", "Port of Mumbai", "Port of Dubai", "In Transit");
        seedCargo("CGO-1026", "Textiles", "Textiles", "800 units", "850 kg", "Port of Chennai", "Port of London", "Pending");
    }

    private static void seedCargo(String id, String name, String type, String qty, String weight, String origin, String dest, String status) {
        Map<String, Object> data = new HashMap<>();
        data.put("cargoId", id);
        data.put("cargoName", name);
        data.put("cargoType", type);
        data.put("quantity", qty);
        data.put("weight", weight);
        data.put("originPort", origin);
        data.put("destinationPort", dest);
        data.put("status", status);
        data.put("createdAt", LocalDateTime.now().toString());
        localCargoStore.put(id, data);
    }

    public synchronized String generateCargoId() {
        return "CGO-" + (cargoCounter++);
    }

    public CompletableFuture<Map<String, Object>> saveCargo(String cargoId, String cargoName, String cargoType,
            String quantity, String weight, String originPort, String destinationPort, String status) {
        
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        try {
            if (cargoId == null || cargoId.isBlank()) {
                cargoId = generateCargoId();
            }

            Map<String, Object> cargoData = new HashMap<>();
            cargoData.put("cargoId", cargoId);
            cargoData.put("cargoName", cargoName != null ? cargoName.trim() : "Cargo");
            cargoData.put("cargoType", cargoType != null ? cargoType.trim() : "General Cargo");
            cargoData.put("quantity", quantity != null ? quantity.trim() : "1");
            cargoData.put("weight", weight != null ? weight.trim() : "0 kg");
            cargoData.put("originPort", originPort != null ? originPort.trim() : "Port of Mumbai");
            cargoData.put("destinationPort", destinationPort != null ? destinationPort.trim() : "Port of Singapore");
            cargoData.put("status", status != null && !status.isBlank() ? status : "Registered");
            cargoData.put("createdAt", LocalDateTime.now().toString());

            // Save to local thread-safe store
            localCargoStore.put(cargoId, cargoData);
            System.out.println("[CARGO DAO] Saved cargo to local store: " + cargoId);

            // Attempt Firestore save if connected
            try {
                Firestore db = db();
                if (db != null) {
                    com.google.api.core.ApiFuture<com.google.cloud.firestore.WriteResult> apiFuture = 
                            db.collection("cargos").document(cargoId).set(cargoData);
                    
                    apiFuture.addListener(() -> {
                        try {
                            apiFuture.get();
                            System.out.println("[CARGO DAO] Saved cargo to Firestore: " + cargoData.get("cargoId"));
                            future.complete(cargoData);
                        } catch (Exception ex) {
                            System.err.println("[CARGO DAO] Firestore async write warning: " + ex.getMessage());
                            future.complete(cargoData);
                        }
                    }, Runnable::run);
                } else {
                    future.complete(cargoData);
                }
            } catch (Exception ex) {
                System.err.println("[CARGO DAO] Firestore save warning: " + ex.getMessage());
                future.complete(cargoData);
            }
        } catch (Exception ex) {
            System.err.println("[CARGO DAO] Cargo save failed: " + ex.getMessage());
            future.completeExceptionally(ex);
        }
        return future;
    }

    public List<Map<String, Object>> getAllCargos() {
        List<Map<String, Object>> list = new ArrayList<>(localCargoStore.values());
        // Sort newest first
        Collections.reverse(list);
        return list;
    }
}
