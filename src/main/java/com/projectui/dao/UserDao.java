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
import com.projectui.view.admin.AdminDataStore;

@SuppressWarnings({"unused", "null", "unchecked"})
public class UserDao extends Dao {

    private static final Map<String, Map<String, Object>> localProfileStore = new ConcurrentHashMap<>();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String API_KEY = System.getenv("FIREBASE_API_KEY") != null
            && !System.getenv("FIREBASE_API_KEY").isBlank()
                    ? System.getenv("FIREBASE_API_KEY")
                    : "AIzaSyDO3l4YIlfgO9qvU2XB54k2pBvayr_ID1U";

    static {
        // Firebase Auth is the single source of truth for user accounts
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
            userData.put("mustChangePassword", false);
            userData.put("createdAt", LocalDateTime.now().toString());

            // Synchronize with local profile cache by exact UID and email
            localProfileStore.put(uid, userData);
            if (email != null && !email.isBlank()) {
                localProfileStore.put(email.toLowerCase(), userData);
            }

            Firestore db = db();
            if (db != null) {
                com.google.api.core.ApiFuture<com.google.cloud.firestore.WriteResult> apiFuture = db.collection("users")
                        .document(uid).set(userData, com.google.cloud.firestore.SetOptions.merge());
                if (phone != null && !phone.isBlank() && !phone.equalsIgnoreCase(uid)) {
                    db.collection("users").document(phone).set(userData, com.google.cloud.firestore.SetOptions.merge());
                }
                apiFuture.addListener(() -> {
                    try {
                        apiFuture.get();
                        System.out.println("[FIRESTORE SAVE SUCCESS] Path: users/" + uid + " | Email: " + email + " | Role: " + role + " | Status: " + status);
                        future.complete(null);
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        System.err.println("[FIRESTORE SAVE FAILURE] Path: users/" + uid + " | Error: " + cause.getMessage());
                        cause.printStackTrace();
                        future.completeExceptionally(cause);
                    }
                }, Runnable::run);
            } else {
                ForkJoinPool.commonPool().execute(() -> {
                    try {
                        writeFirestoreRest(uid, userData);
                        System.out.println("[FIRESTORE REST SAVE SUCCESS] Path: users/" + uid + " | Email: " + email + " | Role: " + role);
                        future.complete(null);
                    } catch (Exception ex) {
                        System.err.println("[FIRESTORE REST SAVE FAILURE] Path: users/" + uid + " | Error: " + ex.getMessage());
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

    public CompletableFuture<Void> saveUserExtended(String uid, String email, String name, String company, String phone, String role, String status, Map<String, Object> extraFields) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            if (phone == null || phone.isBlank()) {
                phone = "USR-" + Math.abs(email != null ? email.hashCode() : (int)(Math.random() * 899999 + 100000));
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("uid", uid);
            userData.put("email", email);
            userData.put("name", name);
            userData.put("organization", company);
            userData.put("company", company);
            userData.put("phone", phone);
            userData.put("role", role);
            userData.put("status", status != null && !status.isBlank() ? status : "Active");
            userData.put("mustChangePassword", false);
            userData.put("createdAt", LocalDateTime.now().toString());

            if (extraFields != null) {
                userData.putAll(extraFields);
            }

            localProfileStore.put(uid, userData);
            if (email != null && !email.isBlank()) {
                localProfileStore.put(email.toLowerCase(), userData);
            }

            Firestore db = db();
            if (db != null) {
                com.google.api.core.ApiFuture<com.google.cloud.firestore.WriteResult> apiFuture = db.collection("users")
                        .document(uid).set(userData, com.google.cloud.firestore.SetOptions.merge());
                if (phone != null && !phone.isBlank() && !phone.equalsIgnoreCase(uid)) {
                    db.collection("users").document(phone).set(userData, com.google.cloud.firestore.SetOptions.merge());
                }
                apiFuture.addListener(() -> {
                    try {
                        apiFuture.get();
                        System.out.println("[FIRESTORE EXTENDED SAVE SUCCESS] Path: users/" + uid + " | Email: " + email + " | Role: " + role + " | Status: " + status);
                        future.complete(null);
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        System.err.println("[FIRESTORE EXTENDED SAVE FAILURE] Path: users/" + uid + " | Error: " + cause.getMessage());
                        cause.printStackTrace();
                        future.completeExceptionally(cause);
                    }
                }, Runnable::run);
            } else {
                ForkJoinPool.commonPool().execute(() -> {
                    try {
                        writeFirestoreRest(uid, userData);
                        System.out.println("[FIRESTORE REST EXTENDED SAVE SUCCESS] Path: users/" + uid + " | Email: " + email + " | Role: " + role);
                        future.complete(null);
                    } catch (Exception ex) {
                        System.err.println("[FIRESTORE REST EXTENDED SAVE FAILURE] Path: users/" + uid + " | Error: " + ex.getMessage());
                        future.completeExceptionally(ex);
                    }
                });
            }
        } catch (Exception ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }

    public CompletableFuture<Map<String, String>> provisionAccount(String creatorRole, String name, String email, String company, String phone, String role, String status) {
        return provisionAccountExtended(creatorRole, name, email, company, phone, role, status, null);
    }

    public CompletableFuture<Map<String, String>> provisionAccountExtended(String creatorRole, String name, String email, String company, String phone, String role, String status, Map<String, Object> extraFields) {
        if ("Admin".equalsIgnoreCase(role)) {
            boolean adminExists = false;
            for (Map<String, Object> prof : localProfileStore.values()) {
                if (prof != null && "Admin".equalsIgnoreCase(String.valueOf(prof.get("role")))) {
                    String exEmail = String.valueOf(prof.get("email")).toLowerCase();
                    if (email != null && !exEmail.equalsIgnoreCase(email.trim().toLowerCase())) {
                        adminExists = true;
                        break;
                    }
                }
            }
            if (adminExists) {
                CompletableFuture<Map<String, String>> failed = new CompletableFuture<>();
                failed.completeExceptionally(new IllegalArgumentException("Admin account already exists. Only one Admin account is allowed."));
                return failed;
            }
        }

        if (!isRoleCreationAllowed(creatorRole, role)) {
            CompletableFuture<Map<String, String>> failed = new CompletableFuture<>();
            failed.completeExceptionally(new SecurityException("Unauthorized: Creator role '" + creatorRole + "' is not permitted to provision '" + role + "' accounts."));
            return failed;
        }

        // Generate secure temporary password (e.g. X7k@29Lm)
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder rnd = new StringBuilder("Pass#");
        for (int i = 0; i < 4; i++) {
            rnd.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        String tempPassword = rnd.toString();
        CompletableFuture<Map<String, String>> future = new CompletableFuture<>();

        com.projectui.service.FirebaseAuthService authService = new com.projectui.service.FirebaseAuthService();
        authService.createAccount(email, tempPassword, name, role).whenComplete((authUser, authError) -> {
            if (authError != null && authError.getMessage() != null && authError.getMessage().contains("already registered")) {
                future.completeExceptionally(new IllegalArgumentException("An account with this email address already exists."));
                return;
            }

            String uid = (authUser != null && authUser.getUid() != null && !authUser.getUid().isBlank())
                    ? authUser.getUid()
                    : "usr_" + Math.abs((email != null ? email.toLowerCase() : "").hashCode());

            System.out.println("[ACCOUNT] Firebase user created: SUCCESS");
            System.out.println("[ACCOUNT] UID: " + uid);

            String id;
            if (role.toLowerCase().contains("captain")) {
                id = "CAP-" + String.format("%03d", (int)(Math.random() * 899 + 1));
            } else if (role.toLowerCase().contains("authority")) {
                id = "PA-" + String.format("%03d", (int)(Math.random() * 899 + 1));
            } else {
                id = "CO-" + String.format("%03d", (int)(Math.random() * 899 + 1));
            }

            Map<String, Object> extended = extraFields != null ? new HashMap<>(extraFields) : new HashMap<>();
            if (role.toLowerCase().contains("captain")) {
                extended.put("captainId", id);
                if (!extended.containsKey("assignedShip")) extended.put("assignedShip", company != null ? company : "MV DockTech Leader");
                if (!extended.containsKey("assignedPort")) extended.put("assignedPort", "Mumbai Port");
            } else if (role.toLowerCase().contains("authority")) {
                extended.put("accountId", id);
                extended.put("paId", id);
                if (!extended.containsKey("assignedPort")) extended.put("assignedPort", company != null ? company : "Mumbai Port");
            } else {
                extended.put("officerId", id);
                if (!extended.containsKey("assignedPort")) extended.put("assignedPort", company != null ? company : "Mumbai Port");
            }
            extended.put("mustChangePassword", false);

            saveUserExtended(uid, email, name, company, phone, role, status != null ? status : "Active", extended).whenComplete((unused, saveError) -> {
                if (saveError != null) {
                    System.err.println("[ACCOUNT] Firestore profile: FAILED (" + saveError.getMessage() + ")");
                    future.completeExceptionally(saveError);
                } else {
                    System.out.println("[ACCOUNT] Firestore profile: SUCCESS");

                    Map<String, String> creds = new HashMap<>();
                    creds.put("uid", uid);
                    creds.put("email", email);
                    creds.put("tempPassword", tempPassword);
                    creds.put("role", role);
                    creds.put("company", company);
                    creds.put("name", name);
                    creds.put("id", id);
                    if (extended.containsKey("assignedPort")) creds.put("assignedPort", String.valueOf(extended.get("assignedPort")));
                    if (extended.containsKey("assignedShip")) creds.put("assignedShip", String.valueOf(extended.get("assignedShip")));

                    future.complete(creds);
                }
            });
        });

        return future;
    }

    public CompletableFuture<Void> updateUserMustChangePassword(String uidOrEmail, boolean mustChange) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ForkJoinPool.commonPool().execute(() -> {
            try {
                if (uidOrEmail != null && localProfileStore.containsKey(uidOrEmail.toLowerCase())) {
                    Map<String, Object> prof = localProfileStore.get(uidOrEmail.toLowerCase());
                    if (prof != null) prof.put("mustChangePassword", mustChange);
                }
                if (uidOrEmail != null && localProfileStore.containsKey(uidOrEmail)) {
                    Map<String, Object> prof = localProfileStore.get(uidOrEmail);
                    if (prof != null) prof.put("mustChangePassword", mustChange);
                }
                System.out.println("[UserDao] Updated mustChangePassword=" + mustChange + " for: " + uidOrEmail);
                future.complete(null);
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            }
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
                    // 1. Direct document lookup by doc ID (primary path: users/{uid})
                    try {
                        com.google.cloud.firestore.DocumentSnapshot doc = db.collection("users").document(uid).get().get();
                        if (doc.exists() && doc.getData() != null) {
                            Map<String, Object> data = repairIncompleteProfile(uid, doc.getData(), email);
                            System.out.println("[PROFILE FOUND via doc ID] users/" + uid + " -> email=" + data.get("email")
                                    + ", role=" + data.get("role") + ", status=" + data.get("status"));
                            localProfileStore.put(uid, data);
                            if (data.get("email") != null) localProfileStore.put(data.get("email").toString().toLowerCase(), data);
                            future.complete(data);
                            return;
                        }
                    } catch (Exception ex) {
                        System.err.println("[FIRESTORE READ EXCEPTION] " + ex.getMessage());
                    }

                    // 2. Query users collection by uid field
                    try {
                        com.google.cloud.firestore.QuerySnapshot snap = db.collection("users")
                                .whereEqualTo("uid", uid).get().get();
                        if (snap != null && !snap.isEmpty()) {
                            com.google.cloud.firestore.DocumentSnapshot doc = snap.getDocuments().get(0);
                            if (doc.exists() && doc.getData() != null) {
                                Map<String, Object> data = repairIncompleteProfile(doc.getId(), doc.getData(), email);
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

                    // 3. Query users collection by email field
                    if (email != null && !email.isBlank()) {
                        try {
                            com.google.cloud.firestore.QuerySnapshot emailSnap = db.collection("users")
                                    .whereEqualTo("email", email.trim().toLowerCase()).get().get();
                            if (emailSnap != null && !emailSnap.isEmpty()) {
                                com.google.cloud.firestore.DocumentSnapshot doc = emailSnap.getDocuments().get(0);
                                if (doc.exists() && doc.getData() != null) {
                                    Map<String, Object> data = repairIncompleteProfile(doc.getId(), doc.getData(), email);
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
                }

                Map<String, Object> restData = readFirestoreRest(uid);
                if (restData != null) {
                    future.complete(repairIncompleteProfile(uid, restData, email));
                    return;
                }

                if (email != null && !email.isBlank()) {
                    Map<String, Object> restDataEmail = readFirestoreRest(email);
                    if (restDataEmail != null) {
                        future.complete(repairIncompleteProfile(uid, restDataEmail, email));
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

    private Map<String, Object> repairIncompleteProfile(String docId, Map<String, Object> data, String hintEmail) {
        if (data == null) data = new HashMap<>();

        boolean modified = false;
        String emailVal = data.get("email") != null ? data.get("email").toString().toLowerCase().trim() : "";
        if (emailVal.isBlank() && hintEmail != null && !hintEmail.isBlank()) {
            emailVal = hintEmail.toLowerCase().trim();
            data.put("email", emailVal);
            modified = true;
        }

        String roleVal = data.get("role") != null ? data.get("role").toString().trim() : "";
        if (roleVal.isBlank()) {
            if (!emailVal.isBlank() && (emailVal.contains("admin") || emailVal.equalsIgnoreCase("babu@gmail.com"))) {
                roleVal = "Admin";
            } else if (emailVal.contains("captain")) {
                roleVal = "Ship Captain";
            } else if (emailVal.contains("officer") || emailVal.contains("custom")) {
                roleVal = "Custom Officer";
            } else if (emailVal.contains("port") || emailVal.contains("authority")) {
                roleVal = "Port Authority";
            } else {
                roleVal = "Admin";
            }
            data.put("role", roleVal);
            modified = true;
        }

        if (data.get("name") == null || data.get("name").toString().isBlank()) {
            String n = !emailVal.isBlank() && emailVal.contains("@") ? emailVal.substring(0, emailVal.indexOf("@")) : "User";
            if (n.length() > 1) n = Character.toUpperCase(n.charAt(0)) + n.substring(1);
            data.put("name", n);
            modified = true;
        }

        if (data.get("status") == null || data.get("status").toString().isBlank()) {
            data.put("status", "Active");
            modified = true;
        }

        if (data.get("uid") == null || data.get("uid").toString().isBlank()) {
            data.put("uid", docId);
            modified = true;
        }

        if (modified) {
            data.put("updatedAt", LocalDateTime.now().toString());
            try {
                Firestore db = db();
                if (db != null) {
                    db.collection("users").document(docId).set(data, com.google.cloud.firestore.SetOptions.merge());
                }
            } catch (Exception ex) {
                System.err.println("[REPAIR PROFILE EXCEPTION] " + ex.getMessage());
            }
            System.out.println("[PROFILE REPAIRED & SYNCED] users/" + docId + " -> email=" + emailVal + ", role=" + roleVal + ", status=" + data.get("status"));
        }

        return data;
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

        // 5. Automatic Auth -> Firestore User Sync if user authenticated via Firebase Auth but has no profile
        if (emailKey != null && !emailKey.isBlank()) {
            String rawEmail = emailKey.trim().toLowerCase();
            String name = rawEmail.contains("@") ? rawEmail.substring(0, rawEmail.indexOf("@")) : "User";
            if (name.length() > 1) {
                name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }

            String inferredRole = "Admin";
            if (rawEmail.contains("captain")) {
                inferredRole = "Ship Captain";
            } else if (rawEmail.contains("officer") || rawEmail.contains("custom")) {
                inferredRole = "Custom Officer";
            } else if (rawEmail.contains("port") || rawEmail.contains("authority")) {
                inferredRole = "Port Authority";
            }

            String syncUid = (uidKey != null && !uidKey.isBlank()) ? uidKey : "usr_" + Math.abs(rawEmail.hashCode());

            Map<String, Object> syncedProfile = new HashMap<>();
            syncedProfile.put("uid", syncUid);
            syncedProfile.put("email", rawEmail);
            syncedProfile.put("name", name);
            syncedProfile.put("role", inferredRole);
            syncedProfile.put("status", "Active");
            syncedProfile.put("organization", "Dock Tech Operations");
            syncedProfile.put("phone", "+91 9800000000");
            syncedProfile.put("mustChangePassword", false);
            syncedProfile.put("createdAt", LocalDateTime.now().toString());
            syncedProfile.put("updatedAt", LocalDateTime.now().toString());

            localProfileStore.put(syncUid, syncedProfile);
            localProfileStore.put(rawEmail, syncedProfile);

            try {
                Firestore db = db();
                if (db != null) {
                    db.collection("users").document(syncUid).set(syncedProfile, com.google.cloud.firestore.SetOptions.merge());
                }
            } catch (Exception ex) {
                System.err.println("[AUTO-SYNC FIRESTORE EXCEPTION] " + ex.getMessage());
            }

            System.out.println("[FIRESTORE AUTO-SYNC SUCCESS] Provisioned missing profile for Auth user: " + rawEmail + " (UID: " + syncUid + ", Role: " + inferredRole + ")");
            return syncedProfile;
        }

        System.out.println("[PROFILE NOT FOUND] uid=" + uidKey + ", email=" + emailKey);
        return null;
    }

    public static void syncExistingAuthUsers() {
        ForkJoinPool.commonPool().execute(() -> {
            try {
                System.out.println("[FIREBASE AUTH SYNC] Starting Admin SDK user synchronization...");
                com.google.firebase.auth.ListUsersPage page = com.google.firebase.auth.FirebaseAuth.getInstance().listUsers(null);
                Firestore db = new UserDao().db();

                int count = 0;
                while (page != null) {
                    for (com.google.firebase.auth.ExportedUserRecord user : page.getValues()) {
                        String uid = user.getUid();
                        String email = user.getEmail();
                        if (email != null && !email.isBlank()) {
                            String rawEmail = email.toLowerCase().trim();
                            String name = user.getDisplayName() != null && !user.getDisplayName().isBlank()
                                    ? user.getDisplayName()
                                    : (rawEmail.contains("@") ? rawEmail.substring(0, rawEmail.indexOf("@")) : "User");

                            String inferredRole = "Admin";
                            if (rawEmail.contains("captain")) inferredRole = "Ship Captain";
                            else if (rawEmail.contains("officer") || rawEmail.contains("custom")) inferredRole = "Custom Officer";
                            else if (rawEmail.contains("port") || rawEmail.contains("authority")) inferredRole = "Port Authority";

                            Map<String, Object> docData = new HashMap<>();
                            docData.put("uid", uid);
                            docData.put("email", rawEmail);
                            docData.put("name", name);
                            docData.put("role", inferredRole);
                            docData.put("status", "Active");
                            docData.put("updatedAt", LocalDateTime.now().toString());

                            if (db != null) {
                                db.collection("users").document(uid).set(docData, com.google.cloud.firestore.SetOptions.merge());
                            }
                            localProfileStore.put(uid, docData);
                            localProfileStore.put(rawEmail, docData);
                            count++;
                        }
                    }
                    page = page.getNextPage();
                }
                System.out.println("[FIREBASE AUTH SYNC] Synchronized " + count + " Firebase Auth users to Firestore collection 'users'.");
            } catch (Exception ex) {
                System.err.println("[FIREBASE AUTH SYNC ERROR] " + ex.getMessage());
            }
        });
    }

    public static void syncFirestoreUsers() {
        try {
            Firestore db = new UserDao().db();
            if (db != null) {
                com.google.cloud.firestore.QuerySnapshot snap = db.collection("users").get().get();
                if (snap != null) {
                    for (com.google.cloud.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        if (doc.exists() && doc.getData() != null) {
                            Map<String, Object> data = doc.getData();
                            String uid = data.get("uid") != null ? data.get("uid").toString() : doc.getId();
                            String email = data.get("email") != null ? data.get("email").toString() : "";

                            if (!uid.isBlank()) localProfileStore.put(uid, data);
                            if (!email.isBlank()) localProfileStore.put(email.toLowerCase(), data);
                        }
                    }
                    System.out.println("[UserDao] Synced " + snap.size() + " user documents from Firestore collection 'users'.");
                }
            }
        } catch (Exception ex) {
            System.err.println("[UserDao] syncFirestoreUsers Exception: " + ex.getMessage());
        }
    }

    public static String getUidForEmail(String email) {
        if (email == null || email.isBlank()) return null;
        Map<String, Object> prof = localProfileStore.get(email.toLowerCase().trim());
        if (prof != null && prof.get("uid") != null) {
            return prof.get("uid").toString();
        }
        return null;
    }

    public static java.util.List<Map<String, Object>> getFirebaseAuthUsersWithProfiles() {
        com.projectui.config.FirebaseConfig.getFirestore();
        syncFirestoreUsers();

        java.util.Map<String, Map<String, Object>> usersByEmail = new java.util.LinkedHashMap<>();

        try {
            com.google.firebase.auth.ListUsersPage page = com.google.firebase.auth.FirebaseAuth.getInstance().listUsers(null);
            if (page != null) {
                while (page != null) {
                    for (com.google.firebase.auth.ExportedUserRecord authUser : page.getValues()) {
                        if (authUser == null) continue;
                        String uid = authUser.getUid();
                        String email = authUser.getEmail() != null ? authUser.getEmail().toLowerCase().trim() : "";
                        String authName = authUser.getDisplayName() != null ? authUser.getDisplayName().trim() : "";

                        String key = !email.isBlank() ? email : (uid != null ? uid : "");
                        if (key.isBlank() || usersByEmail.containsKey(key)) continue;

                        Map<String, Object> profile = null;
                        if (uid != null && !uid.isBlank()) profile = localProfileStore.get(uid);
                        if (profile == null && !email.isBlank()) profile = localProfileStore.get(email);

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", uid != null && !uid.isBlank() ? uid : key);
                        userMap.put("email", email);

                        String name = authName;
                        if (name.isBlank() && profile != null && profile.get("name") != null) {
                            name = profile.get("name").toString().trim();
                        }
                        if (name.isBlank() && profile != null && profile.get("fullName") != null) {
                            name = profile.get("fullName").toString().trim();
                        }
                        if (name.isBlank() && !email.isBlank()) {
                            name = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
                        }
                        if (name.isBlank()) name = "User";
                        userMap.put("name", name);

                        String role = profile != null && profile.get("role") != null
                                ? profile.get("role").toString().trim()
                                : (email.contains("captain") ? "Ship Captain" : (email.contains("officer") || email.contains("custom") ? "Custom Officer" : "Port Authority"));
                        userMap.put("role", role);

                        String status = profile != null && profile.get("status") != null
                                ? profile.get("status").toString().trim()
                                : "Active";
                        userMap.put("status", status);

                        String createdAt = profile != null && profile.get("createdAt") != null
                                ? profile.get("createdAt").toString().trim()
                                : formatAuthTimestamp(authUser.getUserMetadata().getCreationTimestamp());
                        userMap.put("createdAt", createdAt);

                        String updatedAt = profile != null && profile.get("updatedAt") != null
                                ? profile.get("updatedAt").toString().trim()
                                : formatAuthTimestamp(authUser.getUserMetadata().getLastSignInTimestamp());
                        userMap.put("updatedAt", updatedAt);

                        usersByEmail.put(key, userMap);
                    }
                    page = page.getNextPage();
                }
            }
        } catch (Throwable ex) {
            System.err.println("[UserDao] Admin SDK listUsers notice: " + ex.getMessage());
        }

        // Deduplicate synced Firestore user profiles by canonical email/UID
        for (Map<String, Object> prof : localProfileStore.values()) {
            if (prof == null) continue;
            String email = prof.get("email") != null ? prof.get("email").toString().toLowerCase().trim() : "";
            String uid = prof.get("uid") != null ? prof.get("uid").toString().trim() : "";

            String key = !email.isBlank() ? email : uid;
            if (key.isBlank()) continue;

            if (!usersByEmail.containsKey(key)) {
                String bestUid = uid;
                if (bestUid.isBlank() || bestUid.startsWith("+")) {
                    bestUid = "usr_" + Math.abs(key.hashCode());
                }

                String name = prof.get("name") != null ? prof.get("name").toString().trim() : "";
                if (name.isBlank() && prof.get("fullName") != null) name = prof.get("fullName").toString().trim();
                if (name.isBlank() && !email.isBlank()) {
                    name = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
                }
                if (name.isBlank()) name = "User";

                String role = prof.get("role") != null ? prof.get("role").toString().trim() : "Port Authority";
                String status = prof.get("status") != null ? prof.get("status").toString().trim() : "Active";
                String createdAt = prof.get("createdAt") != null ? prof.get("createdAt").toString().trim() : "Registered";
                String updatedAt = prof.get("updatedAt") != null ? prof.get("updatedAt").toString().trim() : "Recently";

                Map<String, Object> userMap = new HashMap<>();
                userMap.put("uid", bestUid);
                userMap.put("email", email);
                userMap.put("name", name);
                userMap.put("role", role);
                userMap.put("status", status);
                userMap.put("createdAt", createdAt);
                userMap.put("updatedAt", updatedAt);

                usersByEmail.put(key, userMap);
            } else {
                Map<String, Object> existing = usersByEmail.get(key);
                String existingUid = existing.get("uid") != null ? existing.get("uid").toString() : "";
                if ((existingUid.startsWith("+") || existingUid.startsWith("usr_")) && !uid.isBlank() && !uid.startsWith("+") && !uid.startsWith("usr_")) {
                    existing.put("uid", uid);
                }
                if (prof.get("role") != null && !prof.get("role").toString().isBlank()) {
                    existing.put("role", prof.get("role").toString().trim());
                }
                if (prof.get("name") != null && !prof.get("name").toString().isBlank() && !prof.get("name").toString().equalsIgnoreCase("User")) {
                    existing.put("name", prof.get("name").toString().trim());
                }
            }
        }

        return new java.util.ArrayList<>(usersByEmail.values());
    }

    private static String formatAuthTimestamp(long millis) {
        if (millis <= 0) return "Recently";
        try {
            return java.time.Instant.ofEpochMilli(millis)
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
        } catch (Exception ex) {
            return "Recently";
        }
    }

    public static java.util.List<Map<String, Object>> getUsersByRoles(String... roles) {
        java.util.List<Map<String, Object>> allAuthUsers = getFirebaseAuthUsersWithProfiles();
        if (roles == null || roles.length == 0) {
            return allAuthUsers;
        }

        java.util.List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Map<String, Object> profile : allAuthUsers) {
            Object roleObj = profile.get("role");
            String userRole = roleObj != null ? roleObj.toString().trim() : "Port Authority";

            for (String r : roles) {
                if (r != null && (userRole.equalsIgnoreCase(r.trim()) || userRole.replaceAll("[_\\s]+", "").equalsIgnoreCase(r.trim().replaceAll("[_\\s]+", "")))) {
                    result.add(profile);
                    break;
                }
            }
        }
        return result;
    }

    public CompletableFuture<Void> updateUserStatus(String uidOrEmail, String newStatus) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (uidOrEmail == null || uidOrEmail.isBlank()) {
            future.complete(null);
            return future;
        }

        Map<String, Object> profile = localProfileStore.get(uidOrEmail.toLowerCase());
        if (profile == null) {
            profile = readFirestoreRest(uidOrEmail);
        }

        if (profile != null) {
            profile.put("status", newStatus);
            String uid = profile.get("uid") != null ? profile.get("uid").toString() : uidOrEmail;
            String email = profile.get("email") != null ? profile.get("email").toString() : uidOrEmail;

            localProfileStore.put(uid, profile);
            localProfileStore.put(email.toLowerCase(), profile);

            final String docUid = uid;
            final Map<String, Object> finalData = profile;
            Firestore db = db();
            if (db != null) {
                db.collection("users").document(docUid).update("status", newStatus);
            } else {
                ForkJoinPool.commonPool().execute(() -> {
                    try {
                        writeFirestoreRest(docUid, finalData);
                    } catch (Exception ignored) {}
                });
            }
        }

        future.complete(null);
        return future;
    }

    public CompletableFuture<Void> deleteUserAccount(String uidOrEmail) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (uidOrEmail == null || uidOrEmail.isBlank()) {
            future.complete(null);
            return future;
        }

        ForkJoinPool.commonPool().execute(() -> {
            try {
                Map<String, Object> profile = localProfileStore.get(uidOrEmail.toLowerCase());
                if (profile == null) profile = localProfileStore.get(uidOrEmail);
                if (profile == null) profile = readFirestoreRest(uidOrEmail);

                if (profile != null) {
                    String role = profile.get("role") != null ? profile.get("role").toString() : "";
                    String uid = profile.get("uid") != null ? profile.get("uid").toString() : uidOrEmail;
                    String email = profile.get("email") != null ? profile.get("email").toString() : uidOrEmail;
                    String phone = profile.get("phone") != null ? profile.get("phone").toString() : "";

                    // Security Guard: Protect the single system Admin account!
                    if (role.toLowerCase().contains("admin") || email.equalsIgnoreCase("babu@gmail.com")) {
                        System.err.println("[SECURITY GUARD] Refused to delete system Admin account: " + email);
                        future.completeExceptionally(new SecurityException("Cannot delete the only system administrator."));
                        return;
                    }

                    String userName = profile.get("name") != null ? profile.get("name").toString() : "User";

                    // Remove from local cache
                    localProfileStore.remove(uid);
                    localProfileStore.remove(email.toLowerCase());
                    if (!phone.isBlank()) localProfileStore.remove(phone);

                    // Delete from Firestore collection "users"
                    Firestore db = new UserDao().db();
                    if (db != null) {
                        try {
                            if (!phone.isBlank()) db.collection("users").document(phone).delete();
                            if (!uid.isBlank()) db.collection("users").document(uid).delete();
                        } catch (Exception ex) {
                            System.err.println("[UserDao Firestore Delete Error] " + ex.getMessage());
                        }
                    }

                    // Try Firebase Admin SDK deleteUser if available
                    try {
                        com.google.firebase.auth.FirebaseAuth.getInstance().deleteUser(uid);
                        System.out.println("[Firebase Auth] Deleted Auth user UID: " + uid);
                    } catch (Throwable ignored) {}

                    logActivity(userName, email, "USER_DELETED", "User account deleted by Admin");
                    System.out.println("[UserDao] Permanently deleted personnel profile for UID: " + uid + ", Email: " + email);
                }
                future.complete(null);
            } catch (Exception ex) {
                System.err.println("[UserDao deleteUserAccount Exception] " + ex.getMessage());
                future.completeExceptionally(ex);
            }
        });
        return future;
    }

    public static void logActivity(String userName, String email, String action, String description) {
        ForkJoinPool.commonPool().execute(() -> {
            try {
                Map<String, Object> log = new HashMap<>();
                log.put("userName", userName != null ? userName : "Admin");
                log.put("email", email != null ? email : "admin@docktech.com");
                log.put("action", action);
                log.put("description", description);
                log.put("timestamp", LocalDateTime.now().toString());

                Firestore db = new UserDao().db();
                if (db != null) {
                    db.collection("activity_logs").add(log);
                }
                System.out.println("[ACTIVITY LOGGED] " + userName + ": " + description);
            } catch (Exception ex) {
                System.err.println("[LOG ACTIVITY ERROR] " + ex.getMessage());
            }
        });
    }

    public static java.util.List<Map<String, Object>> getActivityLogs() {
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        try {
            Firestore db = new UserDao().db();
            if (db != null) {
                com.google.cloud.firestore.QuerySnapshot snap = db.collection("activity_logs")
                        .orderBy("timestamp", com.google.cloud.firestore.Query.Direction.DESCENDING)
                        .limit(10).get().get();
                if (snap != null) {
                    for (com.google.cloud.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        if (doc.exists() && doc.getData() != null) {
                            list.add(doc.getData());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("[GET ACTIVITY LOGS ERROR] " + ex.getMessage());
        }
        return list;
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
