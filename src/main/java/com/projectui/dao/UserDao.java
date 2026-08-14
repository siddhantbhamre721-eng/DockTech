package com.projectui.dao;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

import com.google.cloud.firestore.Firestore;
import com.projectui.config.FirebaseConfig;
import com.projectui.view.admin.AdminDataStore;

public class UserDao extends Dao {

    private static final Map<String, Map<String, Object>> localProfileStore = new ConcurrentHashMap<>();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String API_KEY = System.getenv("FIREBASE_API_KEY") != null
            && !System.getenv("FIREBASE_API_KEY").isBlank()
                    ? System.getenv("FIREBASE_API_KEY")
                    : "AIzaSyDO3l4YIlfgO9qvU2XB54k2pBvayr_ID1U";

    static {
        // Seed default demo profiles so existing accounts remain fully functional
        seedDefaultProfile("aarav@docktech.com", "Aarav Mehta", "Dock Tech Ltd", "+91 9876543210", "Admin", "Active");
        seedDefaultProfile("admin@docktech.com", "System Admin", "Dock Tech Admin", "+91 9800000000", "Admin",
                "Active");
        seedDefaultProfile("john@docktech.com", "John Smith", "Mumbai Port Trust", "+91 9811111111", "Port Authority",
                "Active");
        seedDefaultProfile("rahul@docktech.com", "Rahul Patil", "Oceanic Shipping", "+91 9822222222", "Ship Captain",
                "Active");
        seedDefaultProfile("alex@docktech.com", "Alex Kumar", "Indian Customs", "+91 9833333333", "Customs Officer",
                "Pending");
        seedDefaultProfile("meera@docktech.com", "Meera Rao", "Global Exporters", "+91 9844444444", "Exporter",
                "Active");
        seedDefaultProfile("nisha@docktech.com", "Nisha Shah", "Apex Importers", "+91 9855555555", "Importer",
                "Deactivated");
    }

    private static void seedDefaultProfile(String email, String name, String company, String phone, String role,
            String status) {
        String uid = "usr_" + Math.abs(email.toLowerCase().hashCode());
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("email", email);
        data.put("name", name);
        data.put("organization", company);
        data.put("company", company);
        data.put("phone", phone);
        data.put("role", role);
        data.put("status", status);
        data.put("createdAt", LocalDateTime.now().toString());

        localProfileStore.put(uid, data);
        localProfileStore.put(email.toLowerCase(), data);
    }

    public CompletableFuture<Void> saveUser(String uid, String email, String name, String company, String phone,
            String role, String status) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            System.out.println("Creating Firestore profile...");
            System.out.println("Firestore collection: users");
            System.out.println("Firestore document UID: " + uid);

            Map<String, Object> userData = new HashMap<>();
            userData.put("uid", uid);
            userData.put("email", email);
            userData.put("name", name);
            userData.put("organization", company);
            userData.put("company", company);
            userData.put("phone", phone);
            userData.put("role", role);
            userData.put("status", status != null && !status.isBlank() ? status : "Active");
            userData.put("createdAt", LocalDateTime.now().toString());

            // Synchronize with local profile cache by exact UID and email
            localProfileStore.put(uid, userData);
            if (email != null && !email.isBlank()) {
                localProfileStore.put(email.toLowerCase(), userData);
            }

            Firestore db = db();
            if (db != null) {
                com.google.api.core.ApiFuture<com.google.cloud.firestore.WriteResult> apiFuture = db.collection("users")
                        .document(phone).set(userData);
                apiFuture.addListener(() -> {
                    try {
                        apiFuture.get();
                        System.out.println("Firestore profile saved successfully for UID: " + uid);
                        future.complete(null);
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        System.err.println("Firestore profile save failed: " + cause.getMessage());
                        future.completeExceptionally(cause);
                    }
                }, Runnable::run);
            } else {
                ForkJoinPool.commonPool().execute(() -> {
                    try {
                        writeFirestoreRest(uid, userData);
                        System.out.println("Firestore profile saved successfully for UID: " + uid);
                        future.complete(null);
                    } catch (Exception ex) {
                        System.err.println("Firestore profile save failed: " + ex.getMessage());
                        future.completeExceptionally(ex);
                    }
                });
            }
        } catch (Exception ex) {
            System.err.println("Firestore profile save failed: " + ex.getMessage());
            future.completeExceptionally(ex);
        }
        return future;
    }

    public CompletableFuture<Map<String, Object>> getUserProfile(String uid) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        System.out.println("[FIRESTORE LOOKUP] users/" + uid);

        try {
            Firestore db = db();
            if (db != null) {
                db.collection("users").document(uid).get().addListener(() -> {
                    try {
                        com.google.cloud.firestore.DocumentSnapshot doc = db.collection("users").document(uid).get()
                                .get();
                        if (doc.exists() && doc.getData() != null) {
                            Map<String, Object> data = doc.getData();
                            System.out.println("[PROFILE FOUND] users/" + uid + " -> email=" + data.get("email")
                                    + ", role=" + data.get("role") + ", status=" + data.get("status"));
                            localProfileStore.put(uid, data);
                            future.complete(data);
                            return;
                        }
                    } catch (Exception ex) {
                        System.err.println("[FIRESTORE READ EXCEPTION] " + ex.getMessage());
                    }

                    Map<String, Object> restData = readFirestoreRest(uid);
                    if (restData != null) {
                        future.complete(restData);
                        return;
                    }

                    Map<String, Object> fallback = resolveFallbackProfile(uid);
                    future.complete(fallback);
                }, Runnable::run);
                return future;
            }
        } catch (Exception ex) {
            System.err.println("[FIRESTORE LOOKUP ERROR] " + ex.getMessage());
        }

        Map<String, Object> restData = readFirestoreRest(uid);
        if (restData != null) {
            future.complete(restData);
            return future;
        }

        Map<String, Object> fallback = resolveFallbackProfile(uid);
        future.complete(fallback);
        return future;
    }

    private Map<String, Object> resolveFallbackProfile(String uidKey) {
        if (uidKey == null)
            return null;

        // 1. Direct local store match by UID
        if (localProfileStore.containsKey(uidKey)) {
            Map<String, Object> data = localProfileStore.get(uidKey);
            System.out.println("[PROFILE FOUND] users/" + uidKey + " -> email=" + data.get("email") + ", role="
                    + data.get("role") + ", status=" + data.get("status"));
            return data;
        }

        // 2. Direct local store match by Email
        String lowerKey = uidKey.toLowerCase();
        if (localProfileStore.containsKey(lowerKey)) {
            Map<String, Object> data = localProfileStore.get(lowerKey);
            System.out.println("[PROFILE FOUND] email=" + lowerKey + " -> role=" + data.get("role") + ", status="
                    + data.get("status"));
            return data;
        }

        // 3. Scan local store values for matching uid field
        for (Map<String, Object> profile : localProfileStore.values()) {
            Object objUid = profile.get("uid");
            if (objUid != null && uidKey.equals(objUid.toString())) {
                System.out.println("[PROFILE FOUND] uid=" + uidKey + " -> email=" + profile.get("email") + ", role="
                        + profile.get("role"));
                return profile;
            }
        }

        // 4. Scan AdminDataStore for matching user
        try {
            for (AdminDataStore.UserAccount ua : AdminDataStore.getInstance().users) {
                String accountUid = "usr_" + Math.abs(ua.email.toLowerCase().hashCode());
                if (accountUid.equals(uidKey) || ua.email.equalsIgnoreCase(uidKey)) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("uid", accountUid);
                    data.put("email", ua.email);
                    data.put("name", ua.name);
                    data.put("organization", "Dock Tech Operations");
                    data.put("phone", "+91 9800000000");
                    data.put("role", ua.role);
                    data.put("status", ua.status);
                    data.put("createdAt", ua.created);

                    localProfileStore.put(accountUid, data);
                    localProfileStore.put(ua.email.toLowerCase(), data);
                    System.out.println(
                            "[PROFILE FOUND] email=" + ua.email + " -> role=" + ua.role + ", status=" + ua.status);
                    return data;
                }
            }
        } catch (Exception ignored) {
        }

        System.out.println("[PROFILE NOT FOUND] users/" + uidKey);
        return null;
    }

    private static void writeFirestoreRest(String uid, Map<String, Object> userData) throws Exception {
        StringBuilder fieldsJson = new StringBuilder();
        fieldsJson.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : userData.entrySet()) {
            if (!first)
                fieldsJson.append(",");
            first = false;
            fieldsJson.append("\"").append(entry.getKey()).append("\":{\"stringValue\":\"")
                    .append(entry.getValue() != null ? entry.getValue().toString().replace("\"", "\\\"") : "")
                    .append("\"}");
        }
        fieldsJson.append("}");

        String requestBody = "{\"fields\":" + fieldsJson.toString() + "}";
        URI uri = URI.create(
                "https://firestore.googleapis.com/v1/projects/fir-demo-4dbd9/databases/(default)/documents/users/" + uid
                        + "?key=" + API_KEY);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    private static Map<String, Object> readFirestoreRest(String uid) {
        try {
            URI uri = URI.create(
                    "https://firestore.googleapis.com/v1/projects/fir-demo-4dbd9/databases/(default)/documents/users/"
                            + uid + "?key=" + API_KEY);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                Map<String, Object> data = new HashMap<>();
                data.put("uid", extractFirestoreField(body, "uid"));
                data.put("email", extractFirestoreField(body, "email"));
                data.put("name", extractFirestoreField(body, "name"));
                data.put("organization", extractFirestoreField(body, "organization"));
                data.put("company", extractFirestoreField(body, "company"));
                data.put("phone", extractFirestoreField(body, "phone"));
                data.put("role", extractFirestoreField(body, "role"));
                data.put("status", extractFirestoreField(body, "status"));

                if (data.get("email") != null) {
                    System.out.println("[PROFILE FOUND (REST)] users/" + uid + " -> email=" + data.get("email")
                            + ", role=" + data.get("role") + ", status=" + data.get("status"));
                    localProfileStore.put(uid, data);
                    return data;
                }
            }
        } catch (Exception ex) {
            System.err.println("[FIRESTORE REST READ EXCEPTION] " + ex.getMessage());
        }
        return null;
    }

    private static String extractFirestoreField(String json, String fieldName) {
        if (json == null)
            return null;
        String search = "\"" + fieldName + "\"";
        int index = json.indexOf(search);
        if (index == -1)
            return null;
        String stringValSearch = "\"stringValue\"";
        int valIndex = json.indexOf(stringValSearch, index);
        if (valIndex == -1)
            return null;
        int colonIndex = json.indexOf(":", valIndex);
        if (colonIndex == -1)
            return null;
        int quoteStart = json.indexOf("\"", colonIndex);
        if (quoteStart == -1)
            return null;
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        if (quoteEnd == -1)
            return null;
        return json.substring(quoteStart + 1, quoteEnd);
    }
}
