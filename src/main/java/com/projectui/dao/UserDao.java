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

    public static boolean isRoleCreationAllowed(String creatorRole, String targetRole) {
        if (targetRole == null || targetRole.isBlank()) return false;

        String normTarget = targetRole.trim();
        boolean isPrivilegedTarget = normTarget.equalsIgnoreCase("Admin") ||
                                    normTarget.equalsIgnoreCase("Administrator") ||
                                    normTarget.equalsIgnoreCase("Dock Tech Admin") ||
                                    normTarget.equalsIgnoreCase("Port Authority") ||
                                    normTarget.equalsIgnoreCase("Custom Officer") ||
                                    normTarget.equalsIgnoreCase("Customs Officer") ||
                                    normTarget.equalsIgnoreCase("Ship Captain");

        // Public / Unauthenticated registration
        if (creatorRole == null || creatorRole.isBlank() || creatorRole.equalsIgnoreCase("Public")) {
            return !isPrivilegedTarget; // Only Exporter & Importer allowed
        }

        String normCreator = creatorRole.trim();
        if (normCreator.equalsIgnoreCase("Admin") || normCreator.equalsIgnoreCase("Administrator") || normCreator.equalsIgnoreCase("Dock Tech Admin")) {
            // Dock Tech Admin can manage/create Port Authority, Exporter, Importer
            return normTarget.equalsIgnoreCase("Port Authority") ||
                   normTarget.equalsIgnoreCase("Exporter") ||
                   normTarget.equalsIgnoreCase("Importer");
        }

        if (normCreator.equalsIgnoreCase("Port Authority")) {
            // Port Authority can manage/create Custom Officer, Ship Captain, Exporter, Importer
            return normTarget.equalsIgnoreCase("Custom Officer") ||
                   normTarget.equalsIgnoreCase("Customs Officer") ||
                   normTarget.equalsIgnoreCase("Ship Captain") ||
                   normTarget.equalsIgnoreCase("Exporter") ||
                   normTarget.equalsIgnoreCase("Importer");
        }

        // Custom Officer, Ship Captain, Importer, Exporter cannot create privileged accounts
        return !isPrivilegedTarget;
    }

    public CompletableFuture<Void> saveUser(String uid, String email, String name, String company, String phone,
            String role, String status) {

        String creatorRole = com.projectui.view.login.LoginPage.loggedInUser != null && com.projectui.view.login.LoginPage.loggedInUser.get("role") != null
                ? com.projectui.view.login.LoginPage.loggedInUser.get("role").toString()
                : "Public";

        if (!isRoleCreationAllowed(creatorRole, role)) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(new SecurityException(
                    "Role creation denied: '" + creatorRole + "' is not authorized to assign the privileged role '" + role + "'."
            ));
            return failed;
        }

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
            System.err.println("saveUser error: " + ex.getMessage());
            future.completeExceptionally(ex);
        }
        return future;
    }

    public CompletableFuture<Map<String, String>> provisionAccount(String creatorRole, String name, String email, String company, String phone, String role, String status) {
        if (!isRoleCreationAllowed(creatorRole, role)) {
            CompletableFuture<Map<String, String>> failed = new CompletableFuture<>();
            failed.completeExceptionally(new SecurityException("Unauthorized: Creator role '" + creatorRole + "' is not permitted to provision '" + role + "' accounts."));
            return failed;
        }

        String tempPassword = "Pass#" + (int)(Math.random() * 8999 + 1000);
        CompletableFuture<Map<String, String>> future = new CompletableFuture<>();

        com.projectui.service.FirebaseAuthService authService = new com.projectui.service.FirebaseAuthService();
        authService.createAccount(email, tempPassword, name, role).whenComplete((authUser, authError) -> {
            String uid = (authUser != null && authUser.getUid() != null && !authUser.getUid().isBlank())
                    ? authUser.getUid()
                    : "usr_" + Math.abs(email.toLowerCase().hashCode());

            saveUser(uid, email, name, company, phone, role, status != null ? status : "Active").whenComplete((unused, saveError) -> {
                if (saveError != null) {
                    future.completeExceptionally(saveError);
                } else {
                    Map<String, String> creds = new HashMap<>();
                    creds.put("uid", uid);
                    creds.put("email", email);
                    creds.put("tempPassword", tempPassword);
                    creds.put("role", role);
                    creds.put("company", company);
                    creds.put("name", name);
                    future.complete(creds);
                }
            });
        });

        return future;
    }

    public CompletableFuture<Map<String, Object>> getUserProfile(String uid) {
        return getUserProfile(uid, null);
    }

    public CompletableFuture<Map<String, Object>> getUserProfile(String uid, String email) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        System.out.println("[FIRESTORE LOOKUP] searching for uid=" + uid + (email != null ? ", email=" + email : ""));

        ForkJoinPool.commonPool().execute(() -> {
            try {
                Firestore db = db();
                if (db != null) {
                    // 1. Query users collection by uid field
                    try {
                        com.google.cloud.firestore.QuerySnapshot snap = db.collection("users")
                                .whereEqualTo("uid", uid).get().get();
                        if (snap != null && !snap.isEmpty()) {
                            com.google.cloud.firestore.DocumentSnapshot doc = snap.getDocuments().get(0);
                            if (doc.exists() && doc.getData() != null) {
                                Map<String, Object> data = doc.getData();
                                System.out.println("[PROFILE FOUND via uid field] uid=" + uid + " -> email=" + data.get("email")
                                        + ", role=" + data.get("role") + ", status=" + data.get("status"));
                                localProfileStore.put(uid, data);
                                if (data.get("email") != null) {
                                    localProfileStore.put(data.get("email").toString().toLowerCase(), data);
                                }
                                future.complete(data);
                                return;
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("[FIRESTORE QUERY UID EXCEPTION] " + ex.getMessage());
                    }

                    // 2. Query users collection by email field if uid query yielded no documents
                    if (email != null && !email.isBlank()) {
                        try {
                            com.google.cloud.firestore.QuerySnapshot emailSnap = db.collection("users")
                                    .whereEqualTo("email", email.trim().toLowerCase()).get().get();
                            if (emailSnap != null && !emailSnap.isEmpty()) {
                                com.google.cloud.firestore.DocumentSnapshot doc = emailSnap.getDocuments().get(0);
                                if (doc.exists() && doc.getData() != null) {
                                    Map<String, Object> data = doc.getData();
                                    System.out.println("[PROFILE FOUND via email field] email=" + email + " -> role="
                                            + data.get("role") + ", status=" + data.get("status"));
                                    localProfileStore.put(uid, data);
                                    localProfileStore.put(email.toLowerCase(), data);
                                    future.complete(data);
                                    return;
                                }
                            }
                        } catch (Exception ex) {
                            System.err.println("[FIRESTORE QUERY EMAIL EXCEPTION] " + ex.getMessage());
                        }
                    }

                    // 3. Direct document lookup by doc ID
                    try {
                        com.google.cloud.firestore.DocumentSnapshot doc = db.collection("users").document(uid).get().get();
                        if (doc.exists() && doc.getData() != null) {
                            Map<String, Object> data = doc.getData();
                            System.out.println("[PROFILE FOUND via doc ID] users/" + uid + " -> email=" + data.get("email")
                                    + ", role=" + data.get("role") + ", status=" + data.get("status"));
                            localProfileStore.put(uid, data);
                            future.complete(data);
                            return;
                        }
                    } catch (Exception ex) {
                        System.err.println("[FIRESTORE READ EXCEPTION] " + ex.getMessage());
                    }
                }

                Map<String, Object> restData = readFirestoreRest(uid);
                if (restData != null) {
                    future.complete(restData);
                    return;
                }

                if (email != null && !email.isBlank()) {
                    Map<String, Object> restDataEmail = readFirestoreRest(email);
                    if (restDataEmail != null) {
                        future.complete(restDataEmail);
                        return;
                    }
                }

                Map<String, Object> fallback = resolveFallbackProfile(uid, email);
                future.complete(fallback);
            } catch (Exception ex) {
                System.err.println("[FIRESTORE LOOKUP EXCEPTION] " + ex.getMessage());
                future.complete(resolveFallbackProfile(uid, email));
            }
        });

        return future;
    }

    private Map<String, Object> resolveFallbackProfile(String uidKey) {
        return resolveFallbackProfile(uidKey, null);
    }

    private Map<String, Object> resolveFallbackProfile(String uidKey, String emailKey) {
        if (uidKey == null && emailKey == null)
            return null;

        // 1. Direct local store match by UID
        if (uidKey != null && localProfileStore.containsKey(uidKey)) {
            Map<String, Object> data = localProfileStore.get(uidKey);
            System.out.println("[PROFILE FOUND (local)] uidKey=" + uidKey + " -> email=" + data.get("email") + ", role="
                    + data.get("role") + ", status=" + data.get("status"));
            return data;
        }

        // 2. Direct local store match by Email
        if (emailKey != null && localProfileStore.containsKey(emailKey.toLowerCase())) {
            Map<String, Object> data = localProfileStore.get(emailKey.toLowerCase());
            System.out.println("[PROFILE FOUND (local)] emailKey=" + emailKey + " -> role=" + data.get("role") + ", status="
                    + data.get("status"));
            return data;
        }

        if (uidKey != null && localProfileStore.containsKey(uidKey.toLowerCase())) {
            Map<String, Object> data = localProfileStore.get(uidKey.toLowerCase());
            System.out.println("[PROFILE FOUND (local)] key=" + uidKey + " -> role=" + data.get("role") + ", status="
                    + data.get("status"));
            return data;
        }

        // 3. Scan local store values for matching uid or email field
        for (Map<String, Object> profile : localProfileStore.values()) {
            Object objUid = profile.get("uid");
            Object objEmail = profile.get("email");
            if (uidKey != null && objUid != null && uidKey.equals(objUid.toString())) {
                System.out.println("[PROFILE FOUND (local scan uid)] uid=" + uidKey + " -> email=" + profile.get("email") + ", role="
                        + profile.get("role"));
                return profile;
            }
            if (emailKey != null && objEmail != null && emailKey.equalsIgnoreCase(objEmail.toString())) {
                System.out.println("[PROFILE FOUND (local scan email)] email=" + emailKey + " -> role=" + profile.get("role"));
                return profile;
            }
        }

        // 4. Scan AdminDataStore for matching user
        try {
            for (AdminDataStore.UserAccount ua : AdminDataStore.getInstance().users) {
                String accountUid = "usr_" + Math.abs(ua.email.toLowerCase().hashCode());
                if ((uidKey != null && (accountUid.equals(uidKey) || ua.email.equalsIgnoreCase(uidKey)))
                 || (emailKey != null && ua.email.equalsIgnoreCase(emailKey))) {
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
                            "[PROFILE FOUND (AdminStore)] email=" + ua.email + " -> role=" + ua.role + ", status=" + ua.status);
                    return data;
                }
            }
        } catch (Exception ignored) {
        }

        System.out.println("[PROFILE NOT FOUND] uid=" + uidKey + ", email=" + emailKey);
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
