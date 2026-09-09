package com.projectui.dao;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

import com.google.cloud.firestore.Firestore;
import com.projectui.model.Notification;
import com.projectui.model.Ship;
import com.projectui.service.VesselData;
import com.projectui.service.VesselManager;
import com.projectui.view.admin.AdminDataStore;

@SuppressWarnings({"unused", "null", "unchecked"})
public class UserDao extends Dao {

    public static final Map<String, Map<String, Object>> localProfileStore = new ConcurrentHashMap<>();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String API_KEY = com.projectui.config.FirebaseConfig.API_KEY;
    private static final String PROJECT_ID = com.projectui.config.FirebaseConfig.PROJECT_ID;
    public static final String BABU_UID = "yaU5iKXkN5aAhm6YNmTH5P0bZkv1";
    public static final String BABU_EMAIL = "babu@gmail.com";

    private static final String PERSIST_USERS_FILE = System.getProperty("user.home") + "/.docktech_users.properties";
    private static final String PERSIST_DELETED_USERS = System.getProperty("user.home") + "/.docktech_deleted_users.properties";
    private static final java.util.Set<String> deletedUserKeys = java.util.concurrent.ConcurrentHashMap.newKeySet();

    static {
        loadDeletedUsers();
        // NOTE: Do NOT force-clear any user's deleted status here.
        // All user visibility is determined by Firestore records.
        restoreLocalDiskUsers();
        ensureDefaultPortAuthorities();
        ensureMasterAdmin();
    }

    private static void loadDeletedUsers() {
        if (deletedUserKeys == null) return;
        try {
            java.io.File file = new java.io.File(PERSIST_DELETED_USERS);
            if (file.exists()) {
                java.util.Properties props = new java.util.Properties();
                try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
                    props.load(in);
                }
                for (String key : props.stringPropertyNames()) {
                    if (key != null && !key.isBlank()) {
                        deletedUserKeys.add(key.trim().toLowerCase());
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public static String resolveUserDocId(String uidOrEmail) {
        if (uidOrEmail == null || uidOrEmail.isBlank()) return null;
        String clean = uidOrEmail.trim().toLowerCase();
        if (clean.contains("@")) return clean;

        Map<String, Object> prof = localProfileStore.get(uidOrEmail);
        if (prof != null && prof.get("email") != null && !prof.get("email").toString().isBlank()) {
            return prof.get("email").toString().trim().toLowerCase();
        }
        for (Map<String, Object> p : localProfileStore.values()) {
            if (p != null && uidOrEmail.equals(p.get("uid"))) {
                if (p.get("email") != null && !p.get("email").toString().isBlank()) {
                    return p.get("email").toString().trim().toLowerCase();
                }
            }
        }
        return uidOrEmail;
    }

    public static void ensureMasterAdmin() {
        ForkJoinPool.commonPool().execute(() -> {
            try {
                String email = "docktech@gmail.com";
                String name = "Docktech";
                String role = "Admin";

                Map<String, Object> adminData = new HashMap<>();
                adminData.put("uid", "master-admin-docktech");
                adminData.put("email", email);
                adminData.put("name", name);
                adminData.put("role", role);
                adminData.put("status", "Active");
                adminData.put("createdAt", "01 Jan 2026, 09:00");
                adminData.put("updatedAt", "Just now");

                // Check and standardize Firestore user document to users/docktech@gmail.com
                try {
                    Firestore db = com.projectui.config.FirebaseConfig.getFirestore();
                    if (db != null) {
                        com.google.cloud.firestore.DocumentSnapshot doc = db.collection("users").document(email).get().get();
                        if (doc != null && doc.exists() && doc.getData() != null) {
                            adminData.putAll(doc.getData());
                        } else {
                            // Check legacy users/master-admin-docktech and migrate to users/docktech@gmail.com
                            com.google.cloud.firestore.DocumentSnapshot legacyDoc = db.collection("users").document("master-admin-docktech").get().get();
                            if (legacyDoc != null && legacyDoc.exists() && legacyDoc.getData() != null) {
                                adminData.putAll(legacyDoc.getData());
                                db.collection("users").document(email).set(adminData, com.google.cloud.firestore.SetOptions.merge());
                                try {
                                    db.collection("users").document("master-admin-docktech").delete();
                                    System.out.println("[FIRESTORE MIGRATION] Migrated master-admin-docktech -> users/" + email);
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                } catch (Exception ignored) {}

                localProfileStore.put(email, adminData);

                if (com.projectui.config.FirebaseConfig.hasAdminAuth()) {
                    try {
                        com.google.firebase.auth.UserRecord record = com.google.firebase.auth.FirebaseAuth.getInstance().getUserByEmail(email);
                        if (record != null) {
                            adminData.put("uid", record.getUid());
                            localProfileStore.put(record.getUid(), adminData);
                            // Ensure the uid field in users/docktech@gmail.com has the actual Auth UID
                            try {
                                Firestore db = com.projectui.config.FirebaseConfig.getFirestore();
                                if (db != null) {
                                    db.collection("users").document(email).set(adminData, com.google.cloud.firestore.SetOptions.merge());
                                }
                            } catch (Exception ignored) {}
                        }
                    } catch (Exception ignored) {}
                }
            } catch (Exception ex) {
                System.err.println("[MasterAdmin Setup Notice] " + ex.getMessage());
            }
        });
    }

    public static void saveDeletedUser(String uid, String email) {
        if (deletedUserKeys == null) return;
        if (email != null && !email.isBlank()) {
            deletedUserKeys.add(email.trim().toLowerCase());
        }
        if (uid != null && !uid.isBlank()) {
            deletedUserKeys.add(uid.trim().toLowerCase());
        }
        try {
            java.util.Properties props = new java.util.Properties();
            for (String key : deletedUserKeys) {
                props.setProperty(key, "DELETED");
            }
            java.io.File file = new java.io.File(PERSIST_DELETED_USERS);
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                props.store(out, "DockTech Deleted Users");
            }
        } catch (Exception ignored) {}
    }

    public static void clearDeletedUser(String uid, String email) {
        if (deletedUserKeys == null) return;
        if (email != null && !email.isBlank()) {
            deletedUserKeys.remove(email.trim().toLowerCase());
        }
        if (uid != null && !uid.isBlank()) {
            deletedUserKeys.remove(uid.trim().toLowerCase());
        }
        try {
            java.util.Properties props = new java.util.Properties();
            for (String key : deletedUserKeys) {
                props.setProperty(key, "DELETED");
            }
            java.io.File file = new java.io.File(PERSIST_DELETED_USERS);
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                props.store(out, "DockTech Deleted Users");
            }
        } catch (Exception ignored) {}
    }

    public static boolean isUserDeleted(String uid, String email) {
        if (deletedUserKeys == null || deletedUserKeys.isEmpty()) return false;
        if (email != null && deletedUserKeys.contains(email.trim().toLowerCase())) return true;
        if (uid != null && deletedUserKeys.contains(uid.trim().toLowerCase())) return true;
        return false;
    }

    public static void removeLocalDiskUser(String uid, String email) {
        try {
            java.io.File file = new java.io.File(PERSIST_USERS_FILE);
            if (file.exists()) {
                java.util.Properties props = new java.util.Properties();
                try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
                    props.load(in);
                }
                List<String> keysToRemove = new ArrayList<>();
                for (String key : props.stringPropertyNames()) {
                    String val = props.getProperty(key);
                    if (email != null && !email.isBlank()) {
                        String emLower = email.trim().toLowerCase();
                        if (key.toLowerCase().contains(emLower) || (val != null && val.toLowerCase().contains(emLower))) {
                            keysToRemove.add(key);
                        }
                    }
                    if (uid != null && !uid.isBlank()) {
                        String uidLower = uid.trim().toLowerCase();
                        if (key.toLowerCase().contains(uidLower) || (val != null && val.contains(uidLower))) {
                            keysToRemove.add(key);
                        }
                    }
                }
                for (String k : keysToRemove) {
                    props.remove(k);
                }
                try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                    props.store(out, "DockTech User Profiles");
                }
            }
        } catch (Exception ignored) {}
    }

    public static void restoreLocalDiskUsers() {
        try {
            java.io.File file = new java.io.File(PERSIST_USERS_FILE);
            if (file.exists()) {
                java.util.Properties props = new java.util.Properties();
                try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
                    props.load(in);
                }
                for (String key : props.stringPropertyNames()) {
                    if (key.endsWith(".role")) {
                        String prefix = key.substring(0, key.length() - 5);
                        String email = props.getProperty(prefix + ".email", prefix);
                        String uid = props.getProperty(prefix + ".uid", "usr_" + Math.abs(email.toLowerCase().hashCode()));
                        if (isUserDeleted(uid, email)) continue;

                        String name = props.getProperty(prefix + ".name", "User");
                        String role = props.getProperty(prefix + ".role", "Port Authority");
                        String roleCode = props.getProperty(prefix + ".roleCode", "PORT_AUTHORITY");
                        String status = props.getProperty(prefix + ".status", "Active");
                        String company = props.getProperty(prefix + ".company", "Port Authority");
                        String assignedPort = props.getProperty(prefix + ".assignedPort", "Port of Mumbai");
                        String tempPassword = props.getProperty(prefix + ".tempPassword", "");
                        String accountId = props.getProperty(prefix + ".accountId", email);
                        String phone = props.getProperty(prefix + ".phone", "");

                        Map<String, Object> data = new HashMap<>();
                        data.put("uid", uid);
                        data.put("email", email.toLowerCase().trim());
                        data.put("name", name);
                        data.put("role", role);
                        data.put("roleCode", roleCode);
                        data.put("status", status);
                        data.put("company", company);
                        data.put("organization", company);
                        data.put("assignedPort", assignedPort);
                        data.put("accountId", accountId);
                        data.put("phone", phone);
                        data.put("mustChangePassword", false);
                        data.put("createdAt", LocalDateTime.now().toString());

                        localProfileStore.put(uid, data);
                        localProfileStore.put(email.toLowerCase().trim(), data);
                        if (!accountId.isBlank()) {
                            localProfileStore.put(accountId.toLowerCase().trim(), data);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void saveLocalDiskUser(Map<String, Object> userData) {
        if (userData == null) return;
        Object emailObj = userData.get("email");
        if (emailObj == null || emailObj.toString().isBlank()) return;
        String email = emailObj.toString().toLowerCase().trim();

        try {
            java.util.Properties props = new java.util.Properties();
            java.io.File file = new java.io.File(PERSIST_USERS_FILE);
            if (file.exists()) {
                try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
                    props.load(in);
                } catch (Exception ignored) {}
            }

            String prefix = email;
            props.setProperty(prefix + ".email", email);
            if (userData.get("uid") != null) props.setProperty(prefix + ".uid", userData.get("uid").toString());
            if (userData.get("name") != null) props.setProperty(prefix + ".name", userData.get("name").toString());
            if (userData.get("role") != null) props.setProperty(prefix + ".role", userData.get("role").toString());
            if (userData.get("roleCode") != null) props.setProperty(prefix + ".roleCode", userData.get("roleCode").toString());
            if (userData.get("status") != null) props.setProperty(prefix + ".status", userData.get("status").toString());
            if (userData.get("company") != null) props.setProperty(prefix + ".company", userData.get("company").toString());
            if (userData.get("assignedPort") != null) props.setProperty(prefix + ".assignedPort", userData.get("assignedPort").toString());
            if (userData.get("accountId") != null) props.setProperty(prefix + ".accountId", userData.get("accountId").toString());
            if (userData.get("phone") != null) props.setProperty(prefix + ".phone", userData.get("phone").toString());

            try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                props.store(out, "DockTech User Profiles");
            }
        } catch (Exception ignored) {}
    }

    public static void ensureDefaultPortAuthorities() {
        // Link any real existing Port Authority user profiles from storage to their respective ports
        AdminDataStore store = AdminDataStore.getInstance();
        for (Map.Entry<String, Map<String, Object>> entry : localProfileStore.entrySet()) {
            Map<String, Object> prof = entry.getValue();
            if (prof == null) continue;
            String role = prof.get("role") != null ? prof.get("role").toString() : "";
            if ("Port Authority".equalsIgnoreCase(role)) {
                String assignedPort = prof.get("assignedPort") != null ? prof.get("assignedPort").toString() : "";
                if (!assignedPort.isBlank()) {
                    AdminDataStore.Port port = store.getPortByName(assignedPort);
                    if (port != null && prof.get("name") != null) {
                        port.portAuthority = prof.get("name").toString();
                    }
                }
            }
        }
        cleanseAdminAccounts();
    }

    public static void deactivatePortAuthorityForPort(String portName, String portCode) {
        try {
            for (Map.Entry<String, Map<String, Object>> entry : localProfileStore.entrySet()) {
                Map<String, Object> prof = entry.getValue();
                if (prof == null) continue;
                String role = prof.get("role") != null ? prof.get("role").toString() : "";
                if ("Port Authority".equalsIgnoreCase(role)) {
                    String assigned = prof.get("assignedPort") != null ? prof.get("assignedPort").toString() : "";
                    String pId = prof.get("portId") != null ? prof.get("portId").toString() : "";
                    if ((portName != null && !portName.isBlank() && portName.equalsIgnoreCase(assigned))
                            || (portCode != null && !portCode.isBlank() && portCode.equalsIgnoreCase(pId))) {
                        prof.put("status", "Inactive");
                        prof.put("portStatus", "DELETED");
                        String uid = prof.get("uid") != null ? prof.get("uid").toString() : entry.getKey();
                        com.google.cloud.firestore.Firestore db = com.projectui.config.FirebaseConfig.getFirestore();
                        if (db != null && uid != null && !uid.startsWith("+")) {
                            db.collection("users").document(uid).update("status", "Inactive", "portStatus", "DELETED");
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public static void cleanseAdminAccounts() {
        try {
            // 1. Reassign any rogue non-Babu Admin accounts in localProfileStore
            for (Map.Entry<String, Map<String, Object>> entry : localProfileStore.entrySet()) {
                Map<String, Object> prof = entry.getValue();
                if (prof == null) continue;
                String email = prof.get("email") != null ? prof.get("email").toString().trim().toLowerCase() : "";
                String role = prof.get("role") != null ? prof.get("role").toString().trim() : "";
                if ("Admin".equalsIgnoreCase(role) && !email.equalsIgnoreCase("babu@gmail.com") && !email.contains("admin")) {
                    if (isPortEmailOrKeywords(email) || hasApprovedPortAuthority(email)) {
                        prof.put("role", "Port Authority");
                        prof.put("roleCode", "PORT_AUTHORITY");
                    } else if (email.contains("captain")) {
                        prof.put("role", "Ship Captain");
                    } else if (email.contains("officer") || email.contains("custom")) {
                        prof.put("role", "Custom Officer");
                    } else if (email.contains("importer")) {
                        prof.put("role", "Importer");
                    } else {
                        prof.put("role", "Exporter");
                    }
                }
            }

            // 2. Admin profile in local storage is populated from Firestore when the admin logs in.
            // Do NOT hardcode admin profiles here — they must come from real Firestore data.

            // 3. Remove non-genuine Admin entries from AdminDataStore.users
            AdminDataStore store = AdminDataStore.getInstance();
            // Always ensure the store reflects Babu as the admin display identity (read from Firestore on login)
            store.adminName = "Babu";
            store.adminEmail = BABU_EMAIL;
            store.users.removeIf(u -> u.email != null && ("Admin".equalsIgnoreCase(u.role) && !u.email.equalsIgnoreCase(BABU_EMAIL)));

            // 4. Sync the admin display name from localProfileStore if Babu has already logged in this session
            Map<String, Object> babuProfile = localProfileStore.get(BABU_EMAIL);
            if (babuProfile == null) babuProfile = localProfileStore.get(BABU_UID);
            if (babuProfile != null) {
                String babuName = babuProfile.get("name") != null ? babuProfile.get("name").toString() : "Babu";
                store.adminName = babuName;
            }

            // 4. Background purge of invalid admin docs from Firestore and ~/.docktech_users.properties
            ForkJoinPool.commonPool().execute(() -> {
                try {
                    Firestore db = new UserDao().db();
                    if (db != null) {
                        com.google.cloud.firestore.QuerySnapshot snap = db.collection("users").get().get();
                        if (snap != null) {
                            for (com.google.cloud.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                                if (doc.exists() && doc.getData() != null) {
                                    String em = doc.getString("email");
                                    String rl = doc.getString("role");
                                    String dId = doc.getId();
                                    if ("Admin".equalsIgnoreCase(rl) && em != null && !em.equalsIgnoreCase(BABU_EMAIL) && !em.contains("admin")) {
                                        db.collection("users").document(dId).delete();
                                        System.out.println("[CLEANSE] Deleted invalid Admin document from Firestore: " + em);
                                    }
                                }
                            }
                        }
                        // NOTE: Do NOT write a hardcoded Babu profile to Firestore here.
                        // The admin profile is managed exclusively via the real login workflow.
                    }
                } catch (Throwable ignored) {}


                try {
                    java.io.File file = new java.io.File(PERSIST_USERS_FILE);
                    if (file.exists()) {
                        java.util.Properties props = new java.util.Properties();
                        try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
                            props.load(in);
                        }
                        List<String> pToRemove = new ArrayList<>();
                        for (String key : props.stringPropertyNames()) {
                            String val = props.getProperty(key);
                            if (val != null && val.contains("role=Admin") && !key.equalsIgnoreCase("babu@gmail.com") && !val.contains("babu@gmail.com")) {
                                pToRemove.add(key);
                            }
                        }
                        for (String k : pToRemove) {
                            props.remove(k);
                        }
                        try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                            props.store(out, "DockTech User Storage (Cleansed)");
                        }
                    }
                } catch (Throwable ignored) {}
            });
        } catch (Exception ignored) {}
    }

    public static String normalizePort(String raw) {
        if (raw == null || raw.isBlank()) {
            try {
                List<String> ports = AdminDataStore.getInstance().getApprovedPortNames();
                if (ports != null && !ports.isEmpty()) return ports.get(0);
            } catch (Exception ignored) {}
            return "Port of Mumbai";
        }
        String lower = raw.toLowerCase().trim();
        try {
            // 1. Direct match with registered port names
            for (AdminDataStore.Port p : AdminDataStore.getInstance().ports) {
                if (p != null && p.name != null && !AdminDataStore.isPortDeleted(p.name, p.code)) {
                    String pLower = p.name.toLowerCase().trim();
                    if (pLower.equals(lower) || lower.contains(pLower) || pLower.contains(lower)) {
                        return p.name;
                    }
                    if (p.code != null && !p.code.isBlank() && lower.contains(p.code.toLowerCase().trim())) {
                        return p.name;
                    }
                }
            }

            // 2. Common regional aliases mapped to existing registered port names
            for (AdminDataStore.Port p : AdminDataStore.getInstance().ports) {
                if (p != null && p.name != null && !AdminDataStore.isPortDeleted(p.name, p.code)) {
                    String pLower = p.name.toLowerCase();
                    if ((lower.contains("cochin") || lower.contains("kochi") || lower.contains("cok"))
                            && (pLower.contains("cochin") || pLower.contains("kochi") || pLower.contains("cok"))) {
                        return p.name;
                    }
                    if ((lower.contains("mumbai") || lower.contains("bombay") || lower.contains("jnpt") || lower.contains("mum"))
                            && (pLower.contains("mumbai") || pLower.contains("bombay") || pLower.contains("jnpt") || pLower.contains("mum"))) {
                        return p.name;
                    }
                    if ((lower.contains("chennai") || lower.contains("madras") || lower.contains("maa"))
                            && (pLower.contains("chennai") || pLower.contains("madras") || pLower.contains("maa"))) {
                        return p.name;
                    }
                    if ((lower.contains("vizag") || lower.contains("visakhapatnam") || lower.contains("vtz"))
                            && (pLower.contains("vizag") || pLower.contains("visakhapatnam") || pLower.contains("vtz"))) {
                        return p.name;
                    }
                    if ((lower.contains("kolkata") || lower.contains("calcutta") || lower.contains("ccu"))
                            && (pLower.contains("kolkata") || pLower.contains("calcutta") || pLower.contains("ccu"))) {
                        return p.name;
                    }
                    if ((lower.contains("hazira") || lower.contains("haz"))
                            && (pLower.contains("hazira") || pLower.contains("haz"))) {
                        return p.name;
                    }
                    if ((lower.contains("puri") || lower.contains("odisha") || lower.contains("orissa"))
                            && (pLower.contains("puri") || pLower.contains("odisha") || pLower.contains("orissa"))) {
                        return p.name;
                    }
                }
            }
        } catch (Exception ignored) {}

        return raw.trim();
    }

    public static List<Map<String, Object>> getPersonnelForPort(String portName, String roleKeyword) {
        try {
            syncFirestoreUsers();
        } catch (Exception ignored) {}

        List<Map<String, Object>> list = new ArrayList<>();
        if (roleKeyword == null) return list;
        String normPort = portName != null ? normalizePort(portName) : "";

        for (Map<String, Object> prof : localProfileStore.values()) {
            if (prof == null) continue;
            Object r = prof.get("role");
            if (r == null || r.toString().isBlank()) r = prof.get("roleCode");
            if (r == null || r.toString().isBlank()) r = prof.get("userRole");
            if (r == null) continue;
            String rStr = r.toString().trim().toLowerCase();
            
            boolean roleMatches = false;
            if (roleKeyword.equalsIgnoreCase("Captain")) {
                roleMatches = rStr.contains("captain")
                        && !rStr.contains("port")
                        && !rStr.contains("admin")
                        && !rStr.contains("custom")
                        && !rStr.contains("officer")
                        && !rStr.contains("exporter")
                        && !rStr.contains("importer");
            } else if (roleKeyword.equalsIgnoreCase("Officer") || roleKeyword.equalsIgnoreCase("Customs") || roleKeyword.equalsIgnoreCase("Custom")) {
                boolean isCustoms = rStr.contains("custom");
                boolean isPortAuthority = rStr.contains("portauthority") || (rStr.contains("port") && !isCustoms && (rStr.contains("authority") || rStr.contains("manager") || rStr.contains("master")));
                roleMatches = (isCustoms || rStr.contains("officer"))
                        && !isPortAuthority
                        && !rStr.contains("admin")
                        && !rStr.contains("captain")
                        && !rStr.contains("exporter")
                        && !rStr.contains("importer");
            } else {
                roleMatches = rStr.contains(roleKeyword.toLowerCase());
            }
            if (!roleMatches) continue;

            // Skip deleted users
            String pUid = prof.get("uid") != null ? prof.get("uid").toString() : "";
            String pEmail = prof.get("email") != null ? prof.get("email").toString() : "";
            if (isUserDeleted(pUid, pEmail)) continue;

            Object p = prof.get("assignedPort");
            if (p == null || p.toString().isBlank()) p = prof.get("port");
            if (p == null || p.toString().isBlank()) p = prof.get("portName");
            if (p == null || p.toString().isBlank()) p = prof.get("location");
            String assignedPortStr = (p != null) ? p.toString().trim() : "";

            // Only filter out if user is explicitly assigned to a DIFFERENT specific port
            if (!assignedPortStr.isBlank() && !normPort.isBlank()) {
                String normAssigned = normalizePort(assignedPortStr);
                if (!normAssigned.equalsIgnoreCase(normPort) && !normAssigned.equalsIgnoreCase("All") && !normAssigned.equalsIgnoreCase("All Ports") && !assignedPortStr.equalsIgnoreCase("Not Assigned")) {
                    continue;
                }
            }

            // Deduplicate by uid (same profile stored under multiple keys)
            boolean exists = false;
            for (Map<String, Object> item : list) {
                String existUid = item.get("uid") != null ? item.get("uid").toString() : "";
                String candidateUid = prof.get("uid") != null ? prof.get("uid").toString() : "";
                if (!existUid.isBlank() && !candidateUid.isBlank() && existUid.equals(candidateUid)) {
                    exists = true;
                    break;
                }
                // Also deduplicate by email when uid is absent
                String existEmail = item.get("email") != null ? item.get("email").toString().toLowerCase() : "";
                String candidateEmail = prof.get("email") != null ? prof.get("email").toString().toLowerCase() : "";
                if (!existEmail.isBlank() && !candidateEmail.isBlank() && existEmail.equals(candidateEmail)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                list.add(new HashMap<>(prof));
            }
        }

        return list;
    }

    /**
     * Permanently delete a Ship Captain or Customs Officer account by UID and/or email.
     * Removes from Firestore, local store, disk cache, and marks as deleted so it
     * does not reappear after a refresh.
     */
    public static CompletableFuture<Void> deletePersonnel(String uid, String email) {
        return new UserDao().deleteUserAccount(uid != null ? uid : "", email != null ? email : "");
    }

    public static void assignPersonnelDuty(String uidOrEmail, String shipName, String duty, String assignmentStatus) {
        assignCaptainShipAndDuty(uidOrEmail, "", shipName, "", duty, assignmentStatus, "Port Authority", "HIGH", null);
    }

    public static void assignPersonnelDutyExtended(String uidOrEmail, String shipName, String duty, String assignmentStatus, String assignedBy, String priority, String dueDate) {
        assignCaptainShipAndDuty(uidOrEmail, "", shipName, "", duty, assignmentStatus, assignedBy, priority, dueDate);
    }

    public static void assignCaptainShip(String uidOrEmail, String shipId, String shipName, String shipImo, String assignedBy) {
        assignCaptainShipAndDuty(uidOrEmail, shipId, shipName, shipImo, null, "Active", assignedBy, null, null);
    }

    public static void assignCaptainDuty(String uidOrEmail, String duty, String priority, String dueDate, String assignedBy) {
        assignCaptainShipAndDuty(uidOrEmail, null, null, null, duty, "Active", assignedBy, priority, dueDate);
    }

    public static void assignCaptainShipAndDuty(String uidOrEmail, String shipId, String shipName, String shipImo, String duty, String assignmentStatus, String assignedBy, String priority, String dueDate) {
        if (uidOrEmail == null || uidOrEmail.isBlank()) return;
        String rawKey = uidOrEmail.trim();
        String lowerKey = rawKey.toLowerCase();
        String nowStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));

        Map<String, Object> targetProf = localProfileStore.get(rawKey);
        if (targetProf == null) targetProf = localProfileStore.get(lowerKey);

        if (targetProf == null) {
            for (Map<String, Object> prof : localProfileStore.values()) {
                if (prof == null) continue;
                Object u = prof.get("uid");
                Object e = prof.get("email");
                if ((u != null && u.toString().equalsIgnoreCase(rawKey)) || (e != null && e.toString().equalsIgnoreCase(lowerKey))) {
                    targetProf = prof;
                    break;
                }
            }
        }

        if (targetProf == null) {
            targetProf = new HashMap<>();
            if (rawKey.contains("@")) {
                targetProf.put("email", lowerKey);
                targetProf.put("name", lowerKey.substring(0, lowerKey.indexOf("@")));
            } else {
                targetProf.put("uid", rawKey);
                targetProf.put("name", "Ship Captain");
            }
            targetProf.put("role", "Ship Captain");
            targetProf.put("status", "Active");
        }

        // Apply field updates
        if (shipId != null) targetProf.put("assignedShipId", shipId);
        if (shipName != null && !shipName.isBlank()) {
            targetProf.put("assignedShip", shipName);
            targetProf.put("ship", shipName);
            targetProf.put("shipName", shipName);
            targetProf.put("vessel", shipName);
        }
        if (shipImo != null && !shipImo.isBlank()) targetProf.put("shipImo", shipImo);
        if (duty != null && !duty.isBlank()) targetProf.put("duty", duty);
        if (assignmentStatus != null && !assignmentStatus.isBlank()) targetProf.put("assignmentStatus", assignmentStatus);
        if (assignedBy != null && !assignedBy.isBlank()) targetProf.put("assignedBy", assignedBy);
        if (priority != null && !priority.isBlank()) targetProf.put("dutyPriority", priority);
        if (dueDate != null && !dueDate.isBlank()) targetProf.put("dutyDueDate", dueDate);
        targetProf.put("updatedAt", nowStr);

        // Store into local cache under all possible lookup keys
        Object docUidObj = targetProf.get("uid");
        String docUid = docUidObj != null ? docUidObj.toString() : (!rawKey.contains("@") ? rawKey : null);
        Object emailObj = targetProf.get("email");
        String emailStr = emailObj != null ? emailObj.toString().toLowerCase() : (rawKey.contains("@") ? lowerKey : null);

        if (docUid != null) localProfileStore.put(docUid, targetProf);
        if (emailStr != null) localProfileStore.put(emailStr, targetProf);
        localProfileStore.put(rawKey, targetProf);
        localProfileStore.put(lowerKey, targetProf);

        // Prepare Firestore update map
        Map<String, Object> update = new HashMap<>();
        if (targetProf.containsKey("assignedShipId")) update.put("assignedShipId", targetProf.get("assignedShipId"));
        if (targetProf.containsKey("assignedShip")) update.put("assignedShip", targetProf.get("assignedShip"));
        if (targetProf.containsKey("ship")) update.put("ship", targetProf.get("ship"));
        if (targetProf.containsKey("shipName")) update.put("shipName", targetProf.get("shipName"));
        if (targetProf.containsKey("vessel")) update.put("vessel", targetProf.get("vessel"));
        if (targetProf.containsKey("shipImo")) update.put("shipImo", targetProf.get("shipImo"));
        if (targetProf.containsKey("duty")) update.put("duty", targetProf.get("duty"));
        if (targetProf.containsKey("assignmentStatus")) update.put("assignmentStatus", targetProf.get("assignmentStatus"));
        if (targetProf.containsKey("assignedBy")) update.put("assignedBy", targetProf.get("assignedBy"));
        if (targetProf.containsKey("dutyPriority")) update.put("dutyPriority", targetProf.get("dutyPriority"));
        if (targetProf.containsKey("dutyDueDate")) update.put("dutyDueDate", targetProf.get("dutyDueDate"));
        update.put("updatedAt", nowStr);

        final String finalTargetId = resolveUserDocId(emailStr != null ? emailStr : ((docUid != null && !docUid.isBlank()) ? docUid : rawKey));
        ForkJoinPool.commonPool().execute(() -> {
            try {
                Firestore db = new UserDao().db();
                if (db != null && finalTargetId != null) {
                    db.collection("users").document(finalTargetId).set(update, com.google.cloud.firestore.SetOptions.merge());
                }
            } catch (Exception ex) {
                System.err.println("[UserDao assignCaptain Firestore DB Warning] " + ex.getMessage());
            }

            try {
                if (finalTargetId != null && !finalTargetId.contains("@")) {
                    writeFirestoreRest(finalTargetId, update);
                }
            } catch (Exception ex) {
                System.err.println("[UserDao assignCaptain REST Warning] " + ex.getMessage());
            }
        });
    }

    public static void assignOfficerDuty(String uidOrEmail, String duty, String assignmentStatus, String assignedPort, String assignedBy, String priority, String dueDate) {
        assignOfficerDuty(uidOrEmail, duty, assignmentStatus, assignedPort, assignedBy, priority, dueDate, null, null, null, null);
    }

    public static void assignOfficerDuty(String uidOrEmail, String duty, String assignmentStatus, String assignedPort, String assignedBy, String priority, String dueDate, String shipId, String shipName, String captainUid, String dutyId) {
        if (uidOrEmail == null || uidOrEmail.isBlank()) return;
        String rawKey = uidOrEmail.trim();
        String lowerKey = rawKey.toLowerCase();
        String nowStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));

        Map<String, Object> targetProf = localProfileStore.get(rawKey);
        if (targetProf == null) targetProf = localProfileStore.get(lowerKey);

        if (targetProf == null) {
            for (Map<String, Object> prof : localProfileStore.values()) {
                if (prof == null) continue;
                Object u = prof.get("uid");
                Object e = prof.get("email");
                if ((u != null && u.toString().equalsIgnoreCase(rawKey)) || (e != null && e.toString().equalsIgnoreCase(lowerKey))) {
                    targetProf = prof;
                    break;
                }
            }
        }

        if (targetProf == null) {
            targetProf = new HashMap<>();
            if (rawKey.contains("@")) {
                targetProf.put("email", lowerKey);
                targetProf.put("name", lowerKey.substring(0, lowerKey.indexOf("@")));
            } else {
                targetProf.put("uid", rawKey);
                targetProf.put("name", "Customs Officer");
            }
            targetProf.put("role", "Customs Officer");
            targetProf.put("status", "Active");
        }

        targetProf.put("duty", (duty != null && !duty.isBlank()) ? duty : "Not Assigned");
        targetProf.put("assignmentStatus", (assignmentStatus != null && !assignmentStatus.isBlank()) ? assignmentStatus : "Assigned");
        if (assignedPort != null && !assignedPort.isBlank()) targetProf.put("assignedPort", assignedPort);
        if (assignedBy != null && !assignedBy.isBlank()) targetProf.put("assignedBy", assignedBy);
        if (priority != null && !priority.isBlank()) targetProf.put("dutyPriority", priority);
        if (dueDate != null && !dueDate.isBlank()) targetProf.put("dutyDueDate", dueDate);
        if (shipId != null && !shipId.isBlank()) targetProf.put("assignedShipId", shipId);
        if (shipName != null && !shipName.isBlank() && !shipName.equalsIgnoreCase("Not Assigned")) {
            targetProf.put("assignedShip", shipName);
            targetProf.put("ship", shipName);
            targetProf.put("shipName", shipName);
            targetProf.put("vessel", shipName);
        }
        if (captainUid != null && !captainUid.isBlank()) targetProf.put("captainUid", captainUid);
        if (dutyId != null && !dutyId.isBlank()) targetProf.put("dutyId", dutyId);
        else if (!targetProf.containsKey("dutyId")) targetProf.put("dutyId", "DUTY-" + System.currentTimeMillis());
        targetProf.put("assignedAt", nowStr);
        targetProf.put("updatedAt", nowStr);

        Object docUidObj = targetProf.get("uid");
        String docUid = docUidObj != null ? docUidObj.toString() : (!rawKey.contains("@") ? rawKey : null);
        Object emailObj = targetProf.get("email");
        String emailStr = emailObj != null ? emailObj.toString().toLowerCase() : (rawKey.contains("@") ? lowerKey : null);

        if (docUid != null) localProfileStore.put(docUid, targetProf);
        if (emailStr != null) localProfileStore.put(emailStr, targetProf);
        localProfileStore.put(rawKey, targetProf);
        localProfileStore.put(lowerKey, targetProf);

        if (com.projectui.view.login.LoginPage.loggedInUser != null) {
            Object curUid = com.projectui.view.login.LoginPage.loggedInUser.get("uid");
            Object curEmail = com.projectui.view.login.LoginPage.loggedInUser.get("email");
            if ((curUid != null && curUid.toString().equalsIgnoreCase(rawKey)) ||
                (curEmail != null && curEmail.toString().equalsIgnoreCase(lowerKey)) ||
                (docUid != null && curUid != null && curUid.toString().equalsIgnoreCase(docUid))) {
                com.projectui.view.login.LoginPage.loggedInUser.putAll(targetProf);
            }
        }

        // If captainUid or ship is associated, update Captain assignment status if it was Pending Assignment
        if (captainUid != null && !captainUid.isBlank()) {
            Map<String, Object> capProf = localProfileStore.get(captainUid);
            if (capProf != null) {
                capProf.put("assignmentStatus", "Assigned");
                capProf.put("assignedOfficer", targetProf.get("name") != null ? targetProf.get("name").toString() : "Customs Officer");
                capProf.put("assignedOfficerUid", docUid);
                capProf.put("updatedAt", nowStr);
            }
        } else if (shipName != null && !shipName.isBlank() && !shipName.equalsIgnoreCase("Not Assigned")) {
            for (Map<String, Object> p : localProfileStore.values()) {
                if (p == null) continue;
                Object r = p.get("role");
                Object s = p.get("assignedShip");
                if (r != null && r.toString().toLowerCase().contains("captain") && s != null && s.toString().equalsIgnoreCase(shipName)) {
                    p.put("assignmentStatus", "Assigned");
                    p.put("assignedOfficer", targetProf.get("name") != null ? targetProf.get("name").toString() : "Customs Officer");
                    p.put("assignedOfficerUid", docUid);
                    p.put("updatedAt", nowStr);
                }
            }
        }

        // Prepare Firestore update map
        Map<String, Object> update = new HashMap<>();
        update.put("duty", targetProf.get("duty"));
        update.put("assignmentStatus", targetProf.get("assignmentStatus"));
        if (targetProf.containsKey("assignedPort")) update.put("assignedPort", targetProf.get("assignedPort"));
        if (targetProf.containsKey("assignedBy")) update.put("assignedBy", targetProf.get("assignedBy"));
        if (targetProf.containsKey("dutyPriority")) update.put("dutyPriority", targetProf.get("dutyPriority"));
        if (targetProf.containsKey("dutyDueDate")) update.put("dutyDueDate", targetProf.get("dutyDueDate"));
        if (targetProf.containsKey("assignedShip")) update.put("assignedShip", targetProf.get("assignedShip"));
        if (targetProf.containsKey("assignedShipId")) update.put("assignedShipId", targetProf.get("assignedShipId"));
        if (targetProf.containsKey("captainUid")) update.put("captainUid", targetProf.get("captainUid"));
        if (targetProf.containsKey("dutyId")) update.put("dutyId", targetProf.get("dutyId"));
        update.put("assignedAt", nowStr);
        update.put("updatedAt", nowStr);

        final String finalTargetId = resolveUserDocId(emailStr != null ? emailStr : ((docUid != null && !docUid.isBlank()) ? docUid : rawKey));
        saveLocalDiskUser(targetProf);
        try {
            Firestore db = new UserDao().db();
            if (db != null && finalTargetId != null) {
                db.collection("users").document(finalTargetId).set(update, com.google.cloud.firestore.SetOptions.merge()).get(5, java.util.concurrent.TimeUnit.SECONDS);
            }
        } catch (Exception ex) {
            ForkJoinPool.commonPool().execute(() -> {
                try {
                    Firestore db = new UserDao().db();
                    if (db != null && finalTargetId != null) {
                        db.collection("users").document(finalTargetId).set(update, com.google.cloud.firestore.SetOptions.merge());
                    }
                } catch (Exception ignored) {}
            });
        }
    }

    public static java.util.concurrent.CompletableFuture<Boolean> completeOfficerDuty(String uidOrEmail, String officerName, String remarks) {
        java.util.concurrent.CompletableFuture<Boolean> future = new java.util.concurrent.CompletableFuture<>();
        if (uidOrEmail == null || uidOrEmail.isBlank()) {
            future.complete(false);
            return future;
        }

        java.util.concurrent.ForkJoinPool.commonPool().execute(() -> {
            try {
                String key = uidOrEmail.toLowerCase().trim();
                String nowStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));

                for (Map<String, Object> prof : localProfileStore.values()) {
                    if (prof == null) continue;
                    Object u = prof.get("uid");
                    Object e = prof.get("email");
                    if ((u != null && u.toString().equalsIgnoreCase(key)) || (e != null && e.toString().equalsIgnoreCase(key))) {
                        String currentDuty = prof.get("duty") != null ? prof.get("duty").toString() : "Customs Inspection";
                        String currentShip = prof.get("assignedShip") != null ? prof.get("assignedShip").toString() : "General Port Area";
                        String port = prof.get("assignedPort") != null ? prof.get("assignedPort").toString() : "Port of Mumbai";
                        String resolvedName = (officerName != null && !officerName.isBlank()) ? officerName : (prof.get("name") != null ? prof.get("name").toString() : "Custom Officer");

                        prof.put("duty", "Standby / Available");
                        prof.put("assignmentStatus", "Available");
                        prof.put("lastCompletedDuty", currentDuty);
                        prof.put("lastCompletedVessel", currentShip);
                        prof.put("completedBy", resolvedName);
                        prof.put("completedAt", nowStr);
                        prof.put("updatedAt", nowStr);
                        if (remarks != null && !remarks.isBlank()) prof.put("dutyRemarks", remarks);

                        if (com.projectui.view.login.LoginPage.loggedInUser != null) {
                            com.projectui.view.login.LoginPage.loggedInUser.put("duty", "Standby / Available");
                            com.projectui.view.login.LoginPage.loggedInUser.put("assignmentStatus", "Available");
                            com.projectui.view.login.LoginPage.loggedInUser.put("lastCompletedDuty", currentDuty);
                            com.projectui.view.login.LoginPage.loggedInUser.put("lastCompletedVessel", currentShip);
                            com.projectui.view.login.LoginPage.loggedInUser.put("completedBy", resolvedName);
                            com.projectui.view.login.LoginPage.loggedInUser.put("completedAt", nowStr);
                            com.projectui.view.login.LoginPage.loggedInUser.put("updatedAt", nowStr);
                        }

                        // Persist to Firestore
                        String docUid = resolveUserDocId(e != null ? e.toString() : (u != null ? u.toString() : key));
                        Firestore db = new UserDao().db();
                        if (db != null) {
                            Map<String, Object> update = new HashMap<>();
                            update.put("duty", "Standby / Available");
                            update.put("assignmentStatus", "Available");
                            update.put("lastCompletedDuty", currentDuty);
                            update.put("lastCompletedVessel", currentShip);
                            update.put("completedBy", resolvedName);
                            update.put("completedAt", nowStr);
                            update.put("updatedAt", nowStr);
                            if (remarks != null && !remarks.isBlank()) update.put("dutyRemarks", remarks);
                            db.collection("users").document(docUid).set(update, com.google.cloud.firestore.SetOptions.merge());
                        }

                        // Log activity for 30-day history / audit trail
                        logActivity(
                            resolvedName,
                            e != null ? e.toString() : key,
                            "DUTY_COMPLETED",
                            "Operational Duty '" + currentDuty + "' on '" + currentShip + "' was completed and certified at " + port + "."
                        );

                        // Mark notifications as read
                        new NotificationDao().markDutyNotificationsAsCompleted(docUid);
                        break;
                    }
                }
                future.complete(true);
            } catch (Exception ex) {
                System.err.println("[completeOfficerDuty error] " + ex.getMessage());
                future.complete(false);
            }
        });
        return future;
    }

    public static void assignPersonnelDuty(String uidOrEmail, String shipName, String duty) {
        assignPersonnelDuty(uidOrEmail, shipName, duty, (shipName != null && !shipName.isBlank() && !shipName.equalsIgnoreCase("Not Assigned")) ? "Assigned" : "Available");
    }

    public static Ship getAssignedShipForCaptain(Map<String, Object> user) {
        if (user == null) return null;

        String uid = user.get("uid") != null ? user.get("uid").toString() : "";
        String email = user.get("email") != null ? user.get("email").toString() : "";
        String shipId = "";
        String shipName = "";
        String shipImo = "";

        // Check direct profile properties
        Object sId = user.get("assignedShipId");
        if (sId != null && !sId.toString().isBlank()) shipId = sId.toString().trim();

        Object sName = user.get("assignedShip");
        if (sName == null || sName.toString().isBlank() || sName.toString().equalsIgnoreCase("Not Assigned")) {
            sName = user.get("assignedShipName");
        }
        if (sName == null || sName.toString().isBlank() || sName.toString().equalsIgnoreCase("Not Assigned")) {
            sName = user.get("ship");
        }
        if (sName == null || sName.toString().isBlank() || sName.toString().equalsIgnoreCase("Not Assigned")) {
            sName = user.get("shipName");
        }
        if (sName == null || sName.toString().isBlank() || sName.toString().equalsIgnoreCase("Not Assigned")) {
            sName = user.get("vessel");
        }
        if (sName == null || sName.toString().isBlank() || sName.toString().equalsIgnoreCase("Not Assigned")) {
            sName = user.get("vesselName");
        }

        if (sName != null && !sName.toString().isBlank() && !sName.toString().equalsIgnoreCase("Not Assigned")) {
            shipName = sName.toString().trim();
        }

        if (user.get("shipImo") != null) shipImo = user.get("shipImo").toString().trim();
        if (shipImo.isBlank() && user.get("imo") != null) shipImo = user.get("imo").toString().trim();

        // If ship is still not found, check CargoDao for any cargo assigned to this captain
        if (shipName.isBlank() && (!uid.isBlank() || !email.isBlank())) {
            try {
                List<Map<String, Object>> cargos = new CargoDao().getAllCargos();
                if (cargos != null) {
                    for (Map<String, Object> c : cargos) {
                        if (c == null) continue;
                        String cCapUid = c.get("assignedCaptainUid") != null ? c.get("assignedCaptainUid").toString().trim() : "";
                        String cCapEmail = c.get("assignedCaptainEmail") != null ? c.get("assignedCaptainEmail").toString().trim() : "";
                        if ((!uid.isBlank() && uid.equalsIgnoreCase(cCapUid)) || (!email.isBlank() && email.equalsIgnoreCase(cCapEmail))) {
                            Object sN = c.get("assignedShip");
                            if (sN != null && !sN.toString().isBlank() && !sN.toString().equalsIgnoreCase("Not Assigned")) {
                                shipName = sN.toString().trim();
                                if (c.get("assignedShipId") != null) shipId = c.get("assignedShipId").toString().trim();
                                if (c.get("shipImo") != null) shipImo = c.get("shipImo").toString().trim();
                                user.put("assignedShip", shipName);
                                user.put("assignedShipId", shipId);
                                user.put("shipImo", shipImo);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // If ship is still not found, check the user's notifications in Firestore for any SHIP_ASSIGNED / CARGO_ASSIGNED notification
        if (shipName.isBlank() && (!uid.isBlank() || !email.isBlank())) {
            try {
                List<Notification> notifs = new NotificationDao().getNotificationsForUser(!uid.isBlank() ? uid : email);
                if (notifs != null) {
                    for (Notification n : notifs) {
                        if (n != null && n.getVesselName() != null && !n.getVesselName().isBlank() && !n.getVesselName().equalsIgnoreCase("Not Assigned")) {
                            shipName = n.getVesselName().trim();
                            if (n.getVesselId() != null && !n.getVesselId().isBlank()) {
                                shipId = n.getVesselId().trim();
                            }
                            if (n.getPort() != null && !n.getPort().isBlank()) {
                                user.put("assignedPort", n.getPort());
                            }
                            user.put("assignedShip", shipName);
                            user.put("assignedShipId", shipId);
                            user.put("assignmentStatus", "Active");
                            assignCaptainShip(!uid.isBlank() ? uid : email, shipId, shipName, shipImo, n.getAssignedBy());
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        if (shipName.isBlank() && shipId.isBlank() && shipImo.isBlank()) {
            return null;
        }

        // Query ShipDAO
        Ship found = null;
        try {
            if (!shipId.isBlank()) {
                found = new ShipDAO().getShip(shipId);
            }
            if (found == null) {
                List<Ship> ships = new ShipDAO().getShips();
                if (ships != null) {
                    for (Ship s : ships) {
                        if (s == null) continue;
                        if (!shipId.isBlank() && s.getShipId() != null && s.getShipId().equalsIgnoreCase(shipId)) {
                            found = s;
                            break;
                        }
                        if (!shipName.isBlank() && s.getShipName() != null && s.getShipName().equalsIgnoreCase(shipName)) {
                            found = s;
                            break;
                        }
                        if (!shipImo.isBlank() && s.getImoNumber() != null && s.getImoNumber().equalsIgnoreCase(shipImo)) {
                            found = s;
                            break;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        if (found != null) return found;

        // Query VesselManager live AIS data
        try {
            java.util.Collection<VesselData> allVessels = VesselManager.getInstance().getAllVessels();
            if (allVessels != null) {
                for (VesselData v : allVessels) {
                    if (v == null) continue;
                    boolean match = (!shipName.isBlank() && v.name != null && v.name.equalsIgnoreCase(shipName))
                            || (!shipId.isBlank() && String.valueOf(v.mmsi).equalsIgnoreCase(shipId))
                            || (!shipImo.isBlank() && v.imo != null && v.imo.equalsIgnoreCase(shipImo));
                    if (match) {
                        Ship s = new Ship(
                                String.valueOf(v.mmsi),
                                v.name != null ? v.name : shipName,
                                v.shipType != null ? v.shipType : "Container Vessel",
                                v.imo != null ? v.imo : (!shipImo.isBlank() ? shipImo : "IMO 923456"),
                                v.callSign != null ? v.callSign : "Ocean Maritime",
                                "18:00",
                                v.destination != null ? v.destination : "Port of Singapore",
                                "Berth 4",
                                v.navigationStatus != null ? v.navigationStatus : "At Berth"
                        );
                        new ShipDAO().saveShip(s);
                        return s;
                    }
                }
            }
        } catch (Exception ignored) {}

        return null;
    }

    public static Map<String, Object> getCaptainForShip(String shipName, String shipId, String shipImo) {
        if ((shipName == null || shipName.isBlank() || shipName.equalsIgnoreCase("Not Assigned"))
                && (shipId == null || shipId.isBlank())
                && (shipImo == null || shipImo.isBlank())) {
            return null;
        }

        String sName = (shipName != null && !shipName.equalsIgnoreCase("Not Assigned")) ? shipName.trim().toLowerCase() : "";
        String sId = (shipId != null) ? shipId.trim().toLowerCase() : "";
        String sImo = (shipImo != null) ? shipImo.trim().toLowerCase() : "";

        // Search through localProfileStore for captain profiles
        for (Map<String, Object> prof : localProfileStore.values()) {
            if (prof == null) continue;
            Object r = prof.get("role");
            if (r == null || !r.toString().toLowerCase().contains("captain")) continue;

            String pUid = prof.get("uid") != null ? prof.get("uid").toString() : "";
            String pEmail = prof.get("email") != null ? prof.get("email").toString() : "";
            if (isUserDeleted(pUid, pEmail)) continue;

            String cShip = prof.get("assignedShip") != null ? prof.get("assignedShip").toString().trim().toLowerCase() : "";
            if (cShip.isBlank() && prof.get("ship") != null) cShip = prof.get("ship").toString().trim().toLowerCase();
            if (cShip.isBlank() && prof.get("shipName") != null) cShip = prof.get("shipName").toString().trim().toLowerCase();

            String cShipId = prof.get("assignedShipId") != null ? prof.get("assignedShipId").toString().trim().toLowerCase() : "";
            String cImo = prof.get("shipImo") != null ? prof.get("shipImo").toString().trim().toLowerCase() : "";

            boolean match = (!sId.isBlank() && !cShipId.isBlank() && sId.equals(cShipId))
                    || (!sName.isBlank() && !cShip.isBlank() && (sName.equals(cShip) || cShip.contains(sName) || sName.contains(cShip)))
                    || (!sImo.isBlank() && !cImo.isBlank() && sImo.equals(cImo));

            if (match) {
                return new HashMap<>(prof);
            }
        }
        return null;
    }

    public static List<Map<String, Object>> getEligibleCaptainsForRoute(String originPort, String destinationPort, String shipName) {
        try {
            syncFirestoreUsers();
        } catch (Exception ignored) {}

        List<Map<String, Object>> list = new ArrayList<>();
        String normOrigin = originPort != null ? normalizePort(originPort) : "";
        String normDest = destinationPort != null ? normalizePort(destinationPort) : "";
        String sName = (shipName != null && !shipName.equalsIgnoreCase("Not Assigned")) ? shipName.trim().toLowerCase() : "";

        for (Map<String, Object> prof : localProfileStore.values()) {
            if (prof == null) continue;
            Object r = prof.get("role");
            if (r == null || r.toString().isBlank()) r = prof.get("roleCode");
            if (r == null || r.toString().isBlank()) r = prof.get("userRole");
            if (r == null) continue;
            String rStr = r.toString().trim().toLowerCase();

            boolean isCaptain = rStr.contains("captain")
                    && !rStr.contains("port")
                    && !rStr.contains("admin")
                    && !rStr.contains("custom")
                    && !rStr.contains("officer")
                    && !rStr.contains("exporter")
                    && !rStr.contains("importer");
            if (!isCaptain) continue;

            String pUid = prof.get("uid") != null ? prof.get("uid").toString() : "";
            String pEmail = prof.get("email") != null ? prof.get("email").toString() : "";
            if (isUserDeleted(pUid, pEmail)) continue;

            // Check if already assigned to this ship
            String cShip = prof.get("assignedShip") != null ? prof.get("assignedShip").toString().trim().toLowerCase() : "";
            boolean isAssignedToThisShip = !sName.isBlank() && !cShip.isBlank() && (cShip.equals(sName) || cShip.contains(sName) || sName.contains(cShip));

            // Check port association
            Object p = prof.get("assignedPort");
            if (p == null || p.toString().isBlank()) p = prof.get("port");
            if (p == null || p.toString().isBlank()) p = prof.get("portName");
            String assignedPortStr = (p != null) ? p.toString().trim() : "";
            String normAssigned = !assignedPortStr.isBlank() ? normalizePort(assignedPortStr) : "";

            boolean portEligible = normAssigned.isBlank() 
                    || normAssigned.equalsIgnoreCase("All") 
                    || normAssigned.equalsIgnoreCase("All Ports") 
                    || assignedPortStr.equalsIgnoreCase("Not Assigned")
                    || (!normOrigin.isBlank() && normAssigned.equalsIgnoreCase(normOrigin))
                    || (!normDest.isBlank() && normAssigned.equalsIgnoreCase(normDest));

            // Deduplicate by uid / email
            boolean exists = false;
            for (Map<String, Object> item : list) {
                String existUid = item.get("uid") != null ? item.get("uid").toString() : "";
                String candidateUid = prof.get("uid") != null ? prof.get("uid").toString() : "";
                if (!existUid.isBlank() && !candidateUid.isBlank() && existUid.equals(candidateUid)) {
                    exists = true;
                    break;
                }
                String existEmail = item.get("email") != null ? item.get("email").toString().toLowerCase() : "";
                String candidateEmail = prof.get("email") != null ? prof.get("email").toString().toLowerCase() : "";
                if (!existEmail.isBlank() && !candidateEmail.isBlank() && existEmail.equals(candidateEmail)) {
                    exists = true;
                    break;
                }
            }
            if (!exists && (isAssignedToThisShip || portEligible)) {
                list.add(new HashMap<>(prof));
            }
        }

        // Fallback: If no port-specific captains found, return all registered captains
        if (list.isEmpty()) {
            return getPersonnelForPort(null, "Captain");
        }

        return list;
    }

    public static List<Map<String, Object>> getEligibleCustomsOfficersForRoute(String originPort, String destinationPort) {
        try {
            syncFirestoreUsers();
        } catch (Exception ignored) {}

        List<Map<String, Object>> list = new ArrayList<>();
        String normOrigin = originPort != null ? normalizePort(originPort) : "";
        String normDest = destinationPort != null ? normalizePort(destinationPort) : "";

        for (Map<String, Object> prof : localProfileStore.values()) {
            if (prof == null) continue;
            Object r = prof.get("role");
            if (r == null || r.toString().isBlank()) r = prof.get("roleCode");
            if (r == null || r.toString().isBlank()) r = prof.get("userRole");
            if (r == null) continue;
            String rStr = r.toString().trim().toLowerCase();

            boolean isCustoms = (rStr.contains("custom") || rStr.contains("officer"))
                    && !rStr.contains("port")
                    && !rStr.contains("admin")
                    && !rStr.contains("captain")
                    && !rStr.contains("exporter")
                    && !rStr.contains("importer");
            if (!isCustoms) continue;

            String pUid = prof.get("uid") != null ? prof.get("uid").toString() : "";
            String pEmail = prof.get("email") != null ? prof.get("email").toString() : "";
            if (isUserDeleted(pUid, pEmail)) continue;

            Object p = prof.get("assignedPort");
            if (p == null || p.toString().isBlank()) p = prof.get("port");
            if (p == null || p.toString().isBlank()) p = prof.get("portName");
            String assignedPortStr = (p != null) ? p.toString().trim() : "";
            String normAssigned = !assignedPortStr.isBlank() ? normalizePort(assignedPortStr) : "";

            boolean portEligible = normAssigned.isBlank() 
                    || normAssigned.equalsIgnoreCase("All") 
                    || normAssigned.equalsIgnoreCase("All Ports") 
                    || assignedPortStr.equalsIgnoreCase("Not Assigned")
                    || (!normOrigin.isBlank() && normAssigned.equalsIgnoreCase(normOrigin))
                    || (!normDest.isBlank() && normAssigned.equalsIgnoreCase(normDest));

            boolean exists = false;
            for (Map<String, Object> item : list) {
                String existUid = item.get("uid") != null ? item.get("uid").toString() : "";
                String candidateUid = prof.get("uid") != null ? prof.get("uid").toString() : "";
                if (!existUid.isBlank() && !candidateUid.isBlank() && existUid.equals(candidateUid)) {
                    exists = true;
                    break;
                }
                String existEmail = item.get("email") != null ? item.get("email").toString().toLowerCase() : "";
                String candidateEmail = prof.get("email") != null ? prof.get("email").toString().toLowerCase() : "";
                if (!existEmail.isBlank() && !candidateEmail.isBlank() && existEmail.equals(candidateEmail)) {
                    exists = true;
                    break;
                }
            }
            if (!exists && portEligible) {
                list.add(new HashMap<>(prof));
            }
        }

        if (list.isEmpty()) {
            return getPersonnelForPort(null, "Customs");
        }
        return list;
    }

    public static void assignCustomsOfficerDuty(String uidOrEmail, String duty, String cargoId, String port, String assignedBy) {
        if (uidOrEmail == null || uidOrEmail.isBlank()) return;
        String rawKey = uidOrEmail.trim();
        String lowerKey = rawKey.toLowerCase();
        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));

        Map<String, Object> targetProf = localProfileStore.get(rawKey);
        if (targetProf == null) targetProf = localProfileStore.get(lowerKey);
        if (targetProf == null) {
            for (Map<String, Object> prof : localProfileStore.values()) {
                if (prof == null) continue;
                Object u = prof.get("uid");
                Object e = prof.get("email");
                if ((u != null && u.toString().equalsIgnoreCase(rawKey)) || (e != null && e.toString().equalsIgnoreCase(lowerKey))) {
                    targetProf = prof;
                    break;
                }
            }
        }

        if (targetProf != null) {
            if (duty != null && !duty.isBlank()) targetProf.put("duty", duty);
            if (cargoId != null && !cargoId.isBlank()) targetProf.put("assignedCargoId", cargoId);
            if (port != null && !port.isBlank()) targetProf.put("assignedPort", port);
            targetProf.put("assignmentStatus", "Active");
            targetProf.put("assignedBy", assignedBy != null ? assignedBy : "Port Authority");
            targetProf.put("updatedAt", nowStr);

            String uUid = resolveUserDocId(targetProf.get("email") != null ? targetProf.get("email").toString() : (targetProf.get("uid") != null ? targetProf.get("uid").toString() : rawKey));
            try {
                Firestore db = com.projectui.config.FirebaseConfig.getFirestore();
                if (db != null && uUid != null && !uUid.isBlank()) {
                    db.collection("users").document(uUid).set(targetProf, com.google.cloud.firestore.SetOptions.merge());
                }
            } catch (Exception ignored) {}
        }
    }

    public static boolean isRoleCreationAllowed(String creatorRole, String targetRole) {
        if (targetRole == null || targetRole.isBlank()) return false;

        String normTarget = targetRole.trim();
        boolean isAdminOrAuthority = normTarget.equalsIgnoreCase("Admin") ||
                                    normTarget.equalsIgnoreCase("Administrator") ||
                                    normTarget.equalsIgnoreCase("Dock Tech Admin") ||
                                    normTarget.equalsIgnoreCase("Port Authority") ||
                                    normTarget.equalsIgnoreCase("PORT_AUTHORITY") ||
                                    normTarget.equalsIgnoreCase("PORT AUTHORITY");

        // Public / Unauthenticated registration
        if (creatorRole == null || creatorRole.isBlank() || creatorRole.equalsIgnoreCase("Public")) {
            return !isAdminOrAuthority; // Allows Exporter, Importer, Custom Officer, Ship Captain
        }

        String normCreator = creatorRole.trim();
        if (normCreator.equalsIgnoreCase("Admin") || normCreator.equalsIgnoreCase("Administrator") || normCreator.equalsIgnoreCase("Dock Tech Admin")) {
            // Dock Tech Admin can manage/create Port Authority, Exporter, Importer
            return normTarget.equalsIgnoreCase("Port Authority") ||
                   normTarget.equalsIgnoreCase("PORT_AUTHORITY") ||
                   normTarget.equalsIgnoreCase("PORT AUTHORITY") ||
                   normTarget.equalsIgnoreCase("Exporter") ||
                   normTarget.equalsIgnoreCase("Importer");
        }

        if (normCreator.equalsIgnoreCase("Port Authority") || normCreator.equalsIgnoreCase("PORT_AUTHORITY")) {
            // Port Authority can manage/create Custom Officer, Ship Captain, Exporter, Importer
            return normTarget.equalsIgnoreCase("Custom Officer") ||
                   normTarget.equalsIgnoreCase("Customs Officer") ||
                   normTarget.equalsIgnoreCase("Ship Captain") ||
                   normTarget.equalsIgnoreCase("Exporter") ||
                   normTarget.equalsIgnoreCase("Importer");
        }

        // Custom Officer, Ship Captain, Importer, Exporter cannot create privileged accounts
        return !isAdminOrAuthority;
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
            String docId = (email != null && !email.isBlank()) ? email.trim().toLowerCase() : uid;
            System.out.println("Firestore document ID (email): " + docId);
            System.out.println("Firebase Auth UID: " + uid);

            clearDeletedUser(uid, email);

            Map<String, Object> userData = new HashMap<>();
            userData.put("uid", uid);
            userData.put("email", email);
            userData.put("name", name);
            userData.put("organization", company);
            userData.put("company", company);
            userData.put("phone", phone);
            userData.put("role", role);
            if ("Port Authority".equalsIgnoreCase(role) || "PORT_AUTHORITY".equalsIgnoreCase(role)) {
                userData.put("roleCode", "PORT_AUTHORITY");
            }
            userData.put("status", status != null && !status.isBlank() ? status : "Active");
            userData.put("mustChangePassword", false);
            userData.put("createdAt", LocalDateTime.now().toString());

            // Synchronize with local profile cache by exact UID and email
            localProfileStore.put(docId, userData);
            if (uid != null && !uid.isBlank()) {
                localProfileStore.put(uid, userData);
            }
            saveLocalDiskUser(userData);

            Firestore db = db();
            if (db != null) {
                com.google.api.core.ApiFuture<com.google.cloud.firestore.WriteResult> apiFuture = db.collection("users")
                        .document(docId).set(userData, com.google.cloud.firestore.SetOptions.merge());
                apiFuture.addListener(() -> {
                    try {
                        apiFuture.get();
                        System.out.println("[FIRESTORE SAVE SUCCESS] Path: users/" + docId + " | UID: " + uid + " | Role: " + role + " | Status: " + status);
                        future.complete(null);
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        System.err.println("[FIRESTORE SAVE FAILURE] Path: users/" + docId + " | Error: " + cause.getMessage());
                        cause.printStackTrace();
                        future.completeExceptionally(cause);
                    }
                }, Runnable::run);
            } else {
                ForkJoinPool.commonPool().execute(() -> {
                    try {
                        writeFirestoreRest(docId, userData);
                        System.out.println("[FIRESTORE REST SAVE SUCCESS] Path: users/" + docId + " | UID: " + uid + " | Role: " + role);
                        future.complete(null);
                    } catch (Exception ex) {
                        System.err.println("[FIRESTORE REST SAVE FAILURE] Path: users/" + docId + " | Error: " + ex.getMessage());
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
            clearDeletedUser(uid, email);
            String docId = (email != null && !email.isBlank()) ? email.trim().toLowerCase() : uid;
            if (phone == null || phone.isBlank()) {
                phone = "USR-" + Math.abs(email != null ? email.hashCode() : (int)(Math.random() * 899999 + 100000));
            }

            Map<String, Object> existingProfile = getCachedUserProfile(uid, email);
            Map<String, Object> userData = new HashMap<>();
            if (existingProfile != null) {
                userData.putAll(existingProfile);
            }
            userData.put("uid", uid);
            userData.put("email", email);
            if (name != null && !name.isBlank()) userData.put("name", name);
            if (company != null && !company.isBlank()) {
                userData.put("organization", company);
                userData.put("company", company);
            }
            userData.put("phone", phone);
            if (role != null && !role.isBlank()) userData.put("role", role);
            if ("Port Authority".equalsIgnoreCase(role) || "PORT_AUTHORITY".equalsIgnoreCase(role)) {
                userData.put("roleCode", "PORT_AUTHORITY");
            }
            userData.put("status", status != null && !status.isBlank() ? status : "Active");
            userData.put("mustChangePassword", false);
            if (!userData.containsKey("createdAt")) {
                userData.put("createdAt", LocalDateTime.now().toString());
            }
            userData.put("updatedAt", LocalDateTime.now().toString());

            if (extraFields != null) {
                userData.putAll(extraFields);
            }

            localProfileStore.put(docId, userData);
            if (uid != null && !uid.isBlank()) {
                localProfileStore.put(uid, userData);
            }
            if (extraFields != null) {
                if (extraFields.get("accountId") != null) {
                    localProfileStore.put(extraFields.get("accountId").toString().toLowerCase().trim(), userData);
                }
                if (extraFields.get("proposedPaEmail") != null) {
                    localProfileStore.put(extraFields.get("proposedPaEmail").toString().toLowerCase().trim(), userData);
                }
                if (extraFields.get("requesterEmail") != null) {
                    localProfileStore.put(extraFields.get("requesterEmail").toString().toLowerCase().trim(), userData);
                }
            }
            saveLocalDiskUser(userData);

            Firestore db = db();
            if (db != null) {
                com.google.api.core.ApiFuture<com.google.cloud.firestore.WriteResult> apiFuture = db.collection("users")
                        .document(docId).set(userData, com.google.cloud.firestore.SetOptions.merge());
                apiFuture.addListener(() -> {
                    try {
                        apiFuture.get();
                        System.out.println("[FIRESTORE EXTENDED SAVE SUCCESS] Path: users/" + docId + " | UID: " + uid + " | Role: " + role + " | Status: " + status);
                        future.complete(null);
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        System.err.println("[FIRESTORE EXTENDED SAVE FAILURE] Path: users/" + docId + " | Error: " + cause.getMessage());
                        cause.printStackTrace();
                        future.completeExceptionally(cause);
                    }
                }, Runnable::run);
            } else {
                ForkJoinPool.commonPool().execute(() -> {
                    try {
                        writeFirestoreRest(docId, userData);
                        System.out.println("[FIRESTORE REST EXTENDED SAVE SUCCESS] Path: users/" + docId + " | UID: " + uid + " | Role: " + role);
                        future.complete(null);
                    } catch (Exception ex) {
                        System.err.println("[FIRESTORE REST EXTENDED SAVE FAILURE] Path: users/" + docId + " | Error: " + ex.getMessage());
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

        // Generate secure temporary password
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder rnd = new StringBuilder("Pass#");
        for (int i = 0; i < 4; i++) {
            rnd.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        String tempPassword = rnd.toString();
        CompletableFuture<Map<String, String>> future = new CompletableFuture<>();

        com.projectui.service.FirebaseAuthService authService = new com.projectui.service.FirebaseAuthService();
        System.out.println("[FIREBASE] Creating Port Authority Authentication account for: " + email);
        authService.createAccount(email, tempPassword, name, role).whenComplete((authUser, authError) -> {
            String uid = null;
            boolean accountCreated = false;
            if (authError != null) {
                String errMsg = authError.getMessage() != null ? authError.getMessage() : "";
                if (errMsg.contains("already exists") || errMsg.contains("EMAIL_EXISTS")) {
                    System.out.println("[FIREBASE] Firebase user already exists for: " + email + ". Reusing existing authentication record.");
                    try {
                        com.google.firebase.auth.UserRecord rec = com.google.firebase.auth.FirebaseAuth.getInstance().getUserByEmail(email.trim().toLowerCase());
                        if (rec != null) uid = rec.getUid();
                    } catch (Throwable ignored) {}
                    if (uid == null) {
                        uid = getUidForEmail(email);
                    }
                    if (uid == null) {
                        uid = "usr_" + Math.abs(email.trim().toLowerCase().hashCode());
                    }
                } else {
                    System.err.println("[FIREBASE] Firebase Auth creation failed: " + authError.getMessage());
                    future.completeExceptionally(authError);
                    return;
                }
            } else if (authUser != null && authUser.getUid() != null && !authUser.getUid().isBlank()) {
                uid = authUser.getUid();
                accountCreated = true;
                System.out.println("[FIREBASE] Authentication account created successfully. UID: " + uid);
            } else {
                System.err.println("[FIREBASE] Firebase Auth creation returned invalid user data.");
                future.completeExceptionally(new IllegalStateException("Failed to create Firebase Authentication account."));
                return;
            }

            final String realUid = uid;
            final boolean createdAccount = accountCreated;
            String id;
            if (role.toLowerCase().contains("captain")) {
                id = "CAP-" + String.format("%03d", (int)(Math.random() * 899 + 1));
            } else if (role.toLowerCase().contains("authority") || role.toUpperCase().contains("PORT")) {
                id = (email != null && !email.isBlank()) ? email.trim() : ("PA-" + String.format("%03d", (int)(Math.random() * 899 + 1)));
            } else {
                id = "CO-" + String.format("%03d", (int)(Math.random() * 899 + 1));
            }

            Map<String, Object> extended = extraFields != null ? new HashMap<>(extraFields) : new HashMap<>();
            extended.put("uid", realUid);
            if (role.toLowerCase().contains("captain")) {
                extended.put("captainId", id);
                extended.put("roleCode", "SHIP_CAPTAIN");
                if (!extended.containsKey("assignedShip")) extended.put("assignedShip", company != null ? company : "MV DockTech Leader");
                if (!extended.containsKey("assignedPort")) extended.put("assignedPort", "Mumbai Port");
            } else if (role.toLowerCase().contains("authority") || role.toUpperCase().contains("PORT")) {
                extended.put("accountId", id);
                extended.put("paId", id);
                extended.put("roleCode", "PORT_AUTHORITY");
                extended.put("role", "Port Authority");
                if (!extended.containsKey("assignedPort") && company != null && !company.isBlank()) extended.put("assignedPort", company);
                if (!extended.containsKey("portId") && extended.containsKey("portCode")) extended.put("portId", extended.get("portCode"));
                if (!extended.containsKey("portName") && extended.containsKey("assignedPort")) extended.put("portName", extended.get("assignedPort"));
            } else {
                extended.put("officerId", id);
                extended.put("roleCode", "CUSTOMS_OFFICER");
                if (!extended.containsKey("assignedPort")) extended.put("assignedPort", company != null ? company : "Port of Mumbai");
            }
            extended.put("mustChangePassword", false);

            saveUserExtended(realUid, email, name, company, phone, role, status != null ? status : "Active", extended).whenComplete((unused, saveError) -> {
                if (saveError != null) {
                    System.err.println("[ACCOUNT] Firestore profile: FAILED (" + saveError.getMessage() + ")");
                    future.completeExceptionally(saveError);
                } else {
                    System.out.println("[ACCOUNT] Firestore profile: SUCCESS for UID: " + realUid);

                    Map<String, String> creds = new HashMap<>();
                    creds.put("uid", realUid);
                    creds.put("email", email);
                    if (createdAccount) creds.put("tempPassword", tempPassword);
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

    public static Map<String, Object> getCachedUserProfile(String uid, String email) {
        if (isUserDeleted(uid, email)) return null;
        String uidKey = uid != null ? uid.trim() : null;
        String emailKey = email != null ? email.trim().toLowerCase() : null;

        if (uidKey != null && localProfileStore.containsKey(uidKey)) {
            Map<String, Object> prof = localProfileStore.get(uidKey);
            if (prof != null && prof.containsKey("role") && prof.get("role") != null) {
                if (!isUserDeleted(prof.get("uid") != null ? prof.get("uid").toString() : uidKey, prof.get("email") != null ? prof.get("email").toString() : emailKey)) {
                    return prof;
                }
            }
        }
        if (emailKey != null && localProfileStore.containsKey(emailKey)) {
            Map<String, Object> prof = localProfileStore.get(emailKey);
            if (prof != null && prof.containsKey("role") && prof.get("role") != null) {
                if (!isUserDeleted(prof.get("uid") != null ? prof.get("uid").toString() : uidKey, prof.get("email") != null ? prof.get("email").toString() : emailKey)) {
                    return prof;
                }
            }
        }
        return null;
    }

    public static Map<String, Object> getProfile(String uidOrEmail) {
        if (uidOrEmail == null || uidOrEmail.isBlank()) return null;
        return getCachedUserProfile(uidOrEmail, uidOrEmail);
    }

    public CompletableFuture<Map<String, Object>> getUserProfile(String uid) {
        return getUserProfile(uid, null);
    }

    public CompletableFuture<Map<String, Object>> getUserProfile(String uid, String email) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

        if (isUserDeleted(uid, email)) {
            future.complete(null);
            return future;
        }

        // Check local profile store cache first to prevent repeated Firestore network lookups
        Map<String, Object> cached = getCachedUserProfile(uid, email);
        if (cached != null) {
            future.complete(cached);
            return future;
        }

        System.out.println("[FIRESTORE LOOKUP] searching for uid=" + uid + (email != null ? ", email=" + email : ""));

        ForkJoinPool.commonPool().execute(() -> {
            try {
                Firestore db = db();
                if (db != null) {
                    String cleanEmail = (email != null && !email.isBlank()) ? email.trim().toLowerCase() : null;

                    // 1. Primary path: Direct document lookup by email document ID (users/{email})
                    if (cleanEmail != null) {
                        try {
                            com.google.cloud.firestore.DocumentSnapshot doc = db.collection("users").document(cleanEmail).get().get();
                            if (doc.exists() && doc.getData() != null) {
                                Map<String, Object> data = repairIncompleteProfile(doc.getId(), doc.getData(), email);
                                if (uid != null && !uid.isBlank()) {
                                    data.put("uid", uid);
                                }
                                String dUid = data.get("uid") != null ? data.get("uid").toString() : uid;
                                String dEmail = data.get("email") != null ? data.get("email").toString() : email;
                                if (isUserDeleted(dUid, dEmail)) {
                                    future.complete(null);
                                    return;
                                }
                                System.out.println("[PROFILE FOUND via email doc ID] users/" + cleanEmail + " -> role="
                                        + data.get("role") + ", status=" + data.get("status"));
                                if (uid != null) localProfileStore.put(uid, data);
                                localProfileStore.put(cleanEmail, data);
                                future.complete(data);
                                return;
                            }
                        } catch (Exception ex) {
                            System.err.println("[FIRESTORE READ EMAIL DOC EXCEPTION] " + ex.getMessage());
                        }

                        // 2. Query users collection by email field
                        try {
                            com.google.cloud.firestore.QuerySnapshot emailSnap = db.collection("users")
                                    .whereEqualTo("email", cleanEmail).get().get();
                            if (emailSnap != null && !emailSnap.isEmpty()) {
                                com.google.cloud.firestore.DocumentSnapshot doc = emailSnap.getDocuments().get(0);
                                if (doc.exists() && doc.getData() != null) {
                                    Map<String, Object> data = repairIncompleteProfile(doc.getId(), doc.getData(), email);
                                    if (uid != null && !uid.isBlank()) {
                                        data.put("uid", uid);
                                    }
                                    // If found under a legacy non-email doc ID, migrate to users/{cleanEmail}
                                    if (!doc.getId().equalsIgnoreCase(cleanEmail)) {
                                        try {
                                            db.collection("users").document(cleanEmail).set(data, com.google.cloud.firestore.SetOptions.merge());
                                            db.collection("users").document(doc.getId()).delete();
                                            System.out.println("[FIRESTORE MIGRATION] Migrated doc ID " + doc.getId() + " -> users/" + cleanEmail);
                                        } catch (Exception ignored) {}
                                    }
                                    if (uid != null) localProfileStore.put(uid, data);
                                    localProfileStore.put(cleanEmail, data);
                                    future.complete(data);
                                    return;
                                }
                            }
                        } catch (Exception ex) {
                            System.err.println("[FIRESTORE QUERY EMAIL EXCEPTION] " + ex.getMessage());
                        }

                        // 3. Try candidate legacy doc IDs like "master-admin-docktech"
                        for (String candidateDocId : new String[] { "master-admin-docktech", "docktech" }) {
                            try {
                                com.google.cloud.firestore.DocumentSnapshot doc = db.collection("users").document(candidateDocId).get().get();
                                if (doc.exists() && doc.getData() != null) {
                                    Map<String, Object> data = repairIncompleteProfile(doc.getId(), doc.getData(), email);
                                    if (uid != null && !uid.isBlank()) {
                                        data.put("uid", uid);
                                    }
                                    try {
                                        db.collection("users").document(cleanEmail).set(data, com.google.cloud.firestore.SetOptions.merge());
                                        db.collection("users").document(candidateDocId).delete();
                                        System.out.println("[FIRESTORE MIGRATION] Migrated " + candidateDocId + " -> users/" + cleanEmail);
                                    } catch (Exception ignored) {}
                                    if (uid != null) localProfileStore.put(uid, data);
                                    localProfileStore.put(cleanEmail, data);
                                    future.complete(data);
                                    return;
                                }
                            } catch (Exception ignored) {}
                        }
                    }

                    // 4. Fallback lookup by legacy UID doc ID (users/{uid})
                    if (uid != null && !uid.isBlank()) {
                        try {
                            com.google.cloud.firestore.DocumentSnapshot doc = db.collection("users").document(uid).get().get();
                            if (doc.exists() && doc.getData() != null) {
                                Map<String, Object> data = repairIncompleteProfile(uid, doc.getData(), email);
                                String dEmail = data.get("email") != null ? data.get("email").toString().toLowerCase().trim() : cleanEmail;
                                if (dEmail != null && dEmail.contains("@")) {
                                    try {
                                        db.collection("users").document(dEmail).set(data, com.google.cloud.firestore.SetOptions.merge());
                                        if (!uid.equalsIgnoreCase(dEmail)) {
                                            db.collection("users").document(uid).delete();
                                            System.out.println("[FIRESTORE MIGRATION] Migrated UID doc " + uid + " -> users/" + dEmail);
                                        }
                                    } catch (Exception ignored) {}
                                }
                                localProfileStore.put(uid, data);
                                if (dEmail != null) localProfileStore.put(dEmail, data);
                                future.complete(data);
                                return;
                            }
                        } catch (Exception ex) {
                            System.err.println("[FIRESTORE READ UID EXCEPTION] " + ex.getMessage());
                        }

                        try {
                            com.google.cloud.firestore.QuerySnapshot snap = db.collection("users")
                                    .whereEqualTo("uid", uid).get().get();
                            if (snap != null && !snap.isEmpty()) {
                                com.google.cloud.firestore.DocumentSnapshot doc = snap.getDocuments().get(0);
                                if (doc.exists() && doc.getData() != null) {
                                    Map<String, Object> data = repairIncompleteProfile(doc.getId(), doc.getData(), email);
                                    String dEmail = data.get("email") != null ? data.get("email").toString().toLowerCase().trim() : cleanEmail;
                                    if (dEmail != null && dEmail.contains("@") && !doc.getId().equalsIgnoreCase(dEmail)) {
                                        try {
                                            db.collection("users").document(dEmail).set(data, com.google.cloud.firestore.SetOptions.merge());
                                            db.collection("users").document(doc.getId()).delete();
                                            System.out.println("[FIRESTORE MIGRATION] Migrated query UID doc " + doc.getId() + " -> users/" + dEmail);
                                        } catch (Exception ignored) {}
                                    }
                                    localProfileStore.put(uid, data);
                                    if (dEmail != null) localProfileStore.put(dEmail, data);
                                    future.complete(data);
                                    return;
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }

                Map<String, Object> restData = readFirestoreRest(uid);
                if (restData != null) {
                    Map<String, Object> data = repairIncompleteProfile(uid, restData, email);
                    String dUid = data.get("uid") != null ? data.get("uid").toString() : uid;
                    String dEmail = data.get("email") != null ? data.get("email").toString() : email;
                    if (!isUserDeleted(dUid, dEmail)) {
                        future.complete(data);
                        return;
                    }
                }

                if (email != null && !email.isBlank()) {
                    Map<String, Object> restDataEmail = readFirestoreRest(email);
                    if (restDataEmail != null) {
                        Map<String, Object> data = repairIncompleteProfile(uid, restDataEmail, email);
                        String dUid = data.get("uid") != null ? data.get("uid").toString() : uid;
                        String dEmail = data.get("email") != null ? data.get("email").toString() : email;
                        if (!isUserDeleted(dUid, dEmail)) {
                            future.complete(data);
                            return;
                        }
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

    public static boolean hasApprovedPortAuthority(String email) {
        if (email == null || email.isBlank()) return false;
        String e = email.toLowerCase().trim();

        try {
            for (AdminDataStore.PortRequest req : AdminDataStore.getInstance().portRequests) {
                if ("APPROVED".equalsIgnoreCase(req.status)) {
                    if ((req.proposedPaEmail != null && req.proposedPaEmail.equalsIgnoreCase(e))
                     || (req.requesterEmail != null && req.requesterEmail.equalsIgnoreCase(e))
                     || (req.requestId != null && req.requestId.equalsIgnoreCase(e))) {
                        return true;
                    }
                }
            }
            for (Map<String, Object> reqMap : PortRequestDao.getLocalRequestStore().values()) {
                Object st = reqMap.get("status");
                if (st != null && "APPROVED".equalsIgnoreCase(st.toString())) {
                    Object paE = reqMap.get("proposedPaEmail");
                    Object reqE = reqMap.get("requesterEmail");
                    Object reqId = reqMap.get("requestId");
                    if ((paE != null && paE.toString().equalsIgnoreCase(e))
                     || (reqE != null && reqE.toString().equalsIgnoreCase(e))
                     || (reqId != null && reqId.toString().equalsIgnoreCase(e))) {
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {}

        return isPortEmailOrKeywords(email);
    }

    public static boolean isPortEmailOrKeywords(String email) {
        if (email == null || email.isBlank()) return false;
        String e = email.toLowerCase().trim();
        if (e.contains("admin") || e.contains("docktech") || e.equalsIgnoreCase("babu@gmail.com")) return false;

        if (e.contains("port") || e.contains("authority") || e.startsWith("pa_") || e.startsWith("pa-") || e.contains("pa@") || e.contains("pa.")
                || e.contains("kochin") || e.contains("cochin") || e.contains("mumbai") || e.contains("chennai")
                || e.contains("vizag") || e.contains("visakhapatnam") || e.contains("kolkata") || e.contains("calcutta")
                || e.contains("hazira") || e.contains("jnpt") || e.contains("kandla") || e.contains("mundra")
                || e.contains("paradip") || e.contains("paradeep") || e.contains("tuticorin") || e.contains("mormugao")
                || e.contains("mangalore") || e.contains("harbor") || e.contains("dock")) {
            return true;
        }

        return false;
    }

    private Map<String, Object> repairIncompleteProfile(String docId, Map<String, Object> rawData, String hintEmail) {
        if (rawData == null)
            return null;
        Map<String, Object> data = new HashMap<>(rawData);

        boolean modified = false;
        String emailVal = data.get("email") != null ? data.get("email").toString().toLowerCase().trim() : "";
        if (emailVal.isBlank() && hintEmail != null && !hintEmail.isBlank()) {
            emailVal = hintEmail.toLowerCase().trim();
            data.put("email", emailVal);
            modified = true;
        }

        String roleVal = data.get("role") != null ? data.get("role").toString().trim() : "";
        if (roleVal.isBlank()) {
            for (String altKey : new String[] { "roleCode", "userRole", "accountRole", "user_role", "roleName", "role_name", "type", "accountType", "roles" }) {
                if (data.get(altKey) != null && !data.get(altKey).toString().isBlank()) {
                    roleVal = data.get(altKey).toString().trim();
                    break;
                }
            }
        }
        if (roleVal.isBlank()) {
            if (!emailVal.isBlank() && (emailVal.contains("admin") || emailVal.contains("docktech") || emailVal.equalsIgnoreCase("babu@gmail.com"))) {
                roleVal = "Admin";
            } else if (emailVal.contains("captain")) {
                roleVal = "Ship Captain";
            } else if (emailVal.contains("officer") || emailVal.contains("custom")) {
                roleVal = "Custom Officer";
            } else if (isPortEmailOrKeywords(emailVal) || hasApprovedPortAuthority(emailVal)) {
                roleVal = "Port Authority";
                data.put("roleCode", "PORT_AUTHORITY");
            } else if (emailVal.contains("importer") || emailVal.contains("import")) {
                roleVal = "Importer";
            } else {
                roleVal = "Exporter";
            }
            data.put("role", roleVal);
            modified = true;
        }

        if ("Port Authority".equalsIgnoreCase(roleVal) || isPortEmailOrKeywords(emailVal)) {
            if (data.get("assignedPort") == null && data.get("port") == null && data.get("portName") == null) {
                if (emailVal.contains("puri") || (data.get("organization") != null && data.get("organization").toString().toLowerCase().contains("puri"))) {
                    data.put("assignedPort", "Puri Port");
                    data.put("port", "Puri Port");
                    data.put("portName", "Puri Port");
                    data.put("organization", "Puri Port Authority");
                    data.put("company", "Puri Port Authority");
                    modified = true;
                }
            } else if (data.get("port") != null && (data.get("assignedPort") == null || data.get("assignedPort").toString().isBlank())) {
                data.put("assignedPort", data.get("port"));
                modified = true;
            } else if (data.get("assignedPort") != null && (data.get("port") == null || data.get("port").toString().isBlank())) {
                data.put("port", data.get("assignedPort"));
                modified = true;
            }
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

        if (isUserDeleted(uidKey, emailKey)) {
            return null;
        }

        // 1. Direct local store match by UID
        if (uidKey != null && localProfileStore.containsKey(uidKey)) {
            Map<String, Object> data = localProfileStore.get(uidKey);
            if (data != null && !isUserDeleted(data.get("uid") != null ? data.get("uid").toString() : uidKey, data.get("email") != null ? data.get("email").toString() : emailKey)) {
                System.out.println("[PROFILE FOUND (local)] uidKey=" + uidKey + " -> email=" + data.get("email") + ", role="
                        + data.get("role") + ", status=" + data.get("status"));
                return data;
            }
        }

        // 2. Direct local store match by Email
        if (emailKey != null && localProfileStore.containsKey(emailKey.toLowerCase())) {
            Map<String, Object> data = localProfileStore.get(emailKey.toLowerCase());
            if (data != null && !isUserDeleted(data.get("uid") != null ? data.get("uid").toString() : uidKey, data.get("email") != null ? data.get("email").toString() : emailKey)) {
                System.out.println("[PROFILE FOUND (local)] emailKey=" + emailKey + " -> role=" + data.get("role") + ", status="
                        + data.get("status"));
                return data;
            }
        }

        if (uidKey != null && localProfileStore.containsKey(uidKey.toLowerCase())) {
            Map<String, Object> data = localProfileStore.get(uidKey.toLowerCase());
            if (data != null && !isUserDeleted(data.get("uid") != null ? data.get("uid").toString() : uidKey, data.get("email") != null ? data.get("email").toString() : emailKey)) {
                System.out.println("[PROFILE FOUND (local)] key=" + uidKey + " -> role=" + data.get("role") + ", status="
                        + data.get("status"));
                return data;
            }
        }

        // 3. Scan local store values for matching uid or email field
        for (Map<String, Object> profile : localProfileStore.values()) {
            if (profile == null) continue;
            Object objUid = profile.get("uid");
            Object objEmail = profile.get("email");
            String pUid = objUid != null ? objUid.toString() : null;
            String pEmail = objEmail != null ? objEmail.toString() : null;
            if (isUserDeleted(pUid, pEmail)) continue;

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

        // 4. Scan AdminDataStore for matching user or port requests
        try {
            for (AdminDataStore.UserAccount ua : AdminDataStore.getInstance().users) {
                if (ua == null) continue;
                String accountUid = "usr_" + Math.abs(ua.email.toLowerCase().hashCode());
                if (isUserDeleted(accountUid, ua.email)) continue;

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

                    if (ua.email.toLowerCase().contains("mumbai") || ua.name.toLowerCase().contains("mumbai")) data.put("assignedPort", "Port of Mumbai");
                    else if (ua.email.toLowerCase().contains("chennai") || ua.name.toLowerCase().contains("chennai")) data.put("assignedPort", "Port of Chennai");
                    else if (ua.email.toLowerCase().contains("vizag") || ua.name.toLowerCase().contains("visakhapatnam")) data.put("assignedPort", "Port of Vizag");
                    else if (ua.email.toLowerCase().contains("kolkata") || ua.name.toLowerCase().contains("kolkata")) data.put("assignedPort", "Port of Kolkata");
                    else if (ua.email.toLowerCase().contains("cochin") || ua.name.toLowerCase().contains("cochin") || ua.email.toLowerCase().contains("kochi") || ua.name.toLowerCase().contains("kochi") || ua.email.toLowerCase().contains("kochin")) data.put("assignedPort", "Cochin Port");
                    else data.put("assignedPort", "Port of Mumbai");

                    localProfileStore.put(accountUid, data);
                    localProfileStore.put(ua.email.toLowerCase(), data);
                    System.out.println(
                            "[PROFILE FOUND (AdminStore)] email=" + ua.email + " -> role=" + ua.role + ", status=" + ua.status);
                    return data;
                }
            }

            for (AdminDataStore.PortRequest req : AdminDataStore.getInstance().portRequests) {
                if (req == null || !"APPROVED".equalsIgnoreCase(req.status)) continue;
                if (isUserDeleted(null, req.proposedPaEmail) || isUserDeleted(null, req.requesterEmail)) continue;
                if (AdminDataStore.isPortDeleted(req.portName, req.portCode)) continue;

                if ((emailKey != null && ((req.proposedPaEmail != null && req.proposedPaEmail.equalsIgnoreCase(emailKey))
                        || (req.requesterEmail != null && req.requesterEmail.equalsIgnoreCase(emailKey))))
                 || (uidKey != null && req.proposedPaEmail != null && ("usr_" + Math.abs(req.proposedPaEmail.toLowerCase().hashCode())).equals(uidKey))) {
                    String accountUid = (uidKey != null && !uidKey.isBlank()) ? uidKey : "usr_" + Math.abs(req.proposedPaEmail.toLowerCase().hashCode());
                    Map<String, Object> data = new HashMap<>();
                    data.put("uid", accountUid);
                    data.put("email", req.proposedPaEmail.toLowerCase().trim());
                    data.put("name", req.proposedPaName != null && !req.proposedPaName.isBlank() ? req.proposedPaName : "Port Authority");
                    data.put("organization", req.portName != null ? req.portName : "Port Authority Operations");
                    data.put("company", req.portName != null ? req.portName : "Port Authority");
                    data.put("assignedPort", req.portName != null ? req.portName : "Port of Mumbai");
                    data.put("phone", req.requesterPhone != null ? req.requesterPhone : "+91 9800000000");
                    data.put("role", "Port Authority");
                    data.put("roleCode", "PORT_AUTHORITY");
                    data.put("status", "Active");
                    data.put("createdAt", LocalDateTime.now().toString());

                    localProfileStore.put(accountUid, data);
                    localProfileStore.put(data.get("email").toString().toLowerCase(), data);
                    saveLocalDiskUser(data);
                    System.out.println("[PROFILE FOUND (PortRequest)] email=" + data.get("email") + " -> role=Port Authority");
                    return data;
                }
            }

            for (Map<String, Object> reqMap : PortRequestDao.getLocalRequestStore().values()) {
                if (reqMap == null) continue;
                Object st = reqMap.get("status");
                if (st == null || !"APPROVED".equalsIgnoreCase(st.toString())) continue;
                Object paE = reqMap.get("proposedPaEmail");
                Object reqE = reqMap.get("requesterEmail");
                Object reqId = reqMap.get("requestId");
                String paEmailStr = paE != null ? paE.toString() : null;
                String reqEmailStr = reqE != null ? reqE.toString() : null;
                if (isUserDeleted(null, paEmailStr) || isUserDeleted(null, reqEmailStr)) continue;
                Object pName = reqMap.get("portName");
                if (pName != null && AdminDataStore.isPortDeleted(pName.toString(), null)) continue;

                if ((emailKey != null && ((paE != null && paE.toString().equalsIgnoreCase(emailKey))
                        || (reqE != null && reqE.toString().equalsIgnoreCase(emailKey))
                        || (reqId != null && reqId.toString().equalsIgnoreCase(emailKey))))
                 || (uidKey != null && paE != null && ("usr_" + Math.abs(paE.toString().toLowerCase().hashCode())).equals(uidKey))) {
                    String cleanPaEmail = paE != null ? paE.toString().toLowerCase().trim() : (emailKey != null ? emailKey : "");
                    String accountUid = (uidKey != null && !uidKey.isBlank()) ? uidKey : "usr_" + Math.abs(cleanPaEmail.hashCode());
                    Map<String, Object> data = new HashMap<>();
                    data.put("uid", accountUid);
                    data.put("email", cleanPaEmail);
                    Object paN = reqMap.get("proposedPaName");
                    data.put("name", paN != null && !paN.toString().isBlank() ? paN.toString() : "Port Authority");
                    String pNameStr = pName != null ? pName.toString() : "Port of Mumbai";
                    data.put("organization", pNameStr);
                    data.put("company", pNameStr);
                    data.put("assignedPort", pNameStr);
                    data.put("role", "Port Authority");
                    data.put("roleCode", "PORT_AUTHORITY");
                    data.put("status", "Active");
                    data.put("createdAt", LocalDateTime.now().toString());

                    localProfileStore.put(accountUid, data);
                    localProfileStore.put(cleanPaEmail, data);
                    if (emailKey != null) localProfileStore.put(emailKey.toLowerCase().trim(), data);
                    saveLocalDiskUser(data);
                    System.out.println("[PROFILE FOUND (PortRequestDao)] email=" + data.get("email") + " -> role=Port Authority");
                    return data;
                }
            }
        } catch (Exception ignored) {
        }

        // Auto-heal profile for legitimate Firebase-authenticated users
        if (emailKey != null && !emailKey.isBlank()) {
            String cleanEmail = emailKey.toLowerCase().trim();
            String bestUid = (uidKey != null && !uidKey.isBlank()) ? uidKey : "usr_" + Math.abs(cleanEmail.hashCode());
            String name = cleanEmail.contains("@") ? cleanEmail.substring(0, cleanEmail.indexOf("@")) : cleanEmail;
            name = Character.toUpperCase(name.charAt(0)) + (name.length() > 1 ? name.substring(1) : "");

            String inferredRole = "Exporter";
            if (cleanEmail.equalsIgnoreCase("docktech@gmail.com") || cleanEmail.contains("docktech") || cleanEmail.equalsIgnoreCase("babu@gmail.com") || cleanEmail.contains("admin")) inferredRole = "Admin";
            else if (cleanEmail.contains("captain")) inferredRole = "Ship Captain";
            else if (cleanEmail.contains("officer") || cleanEmail.contains("custom")) inferredRole = "Custom Officer";
            else if (isPortEmailOrKeywords(cleanEmail) || hasApprovedPortAuthority(cleanEmail)) inferredRole = "Port Authority";
            else if (cleanEmail.contains("importer")) inferredRole = "Importer";

            Map<String, Object> data = new HashMap<>();
            data.put("uid", bestUid);
            data.put("email", cleanEmail);
            data.put("name", name);
            data.put("role", inferredRole);
            if ("Port Authority".equalsIgnoreCase(inferredRole)) {
                data.put("roleCode", "PORT_AUTHORITY");
                String assigned = "Port of Mumbai";
                if (cleanEmail.contains("puri")) assigned = "Puri Port";
                else if (cleanEmail.contains("chennai")) assigned = "Port of Chennai";
                else if (cleanEmail.contains("vizag")) assigned = "Port of Vizag";
                else if (cleanEmail.contains("kolkata")) assigned = "Port of Kolkata";
                else if (cleanEmail.contains("cochin") || cleanEmail.contains("kochi")) assigned = "Cochin Port";
                data.put("assignedPort", assigned);
                data.put("port", assigned);
                data.put("portName", assigned);
                data.put("organization", assigned + " Authority");
                data.put("company", assigned + " Authority");
            }
            data.put("status", "Active");
            data.put("createdAt", LocalDateTime.now().toString());

            localProfileStore.put(bestUid, data);
            localProfileStore.put(cleanEmail, data);

            try {
                Firestore db = com.projectui.config.FirebaseConfig.getFirestore();
                if (db != null) {
                    db.collection("users").document(bestUid).set(data, com.google.cloud.firestore.SetOptions.merge());
                }
            } catch (Throwable ignored) {}

            saveLocalDiskUser(data);
            System.out.println("[PROFILE AUTO-HEALED] email=" + cleanEmail + " -> uid=" + bestUid + ", role=" + inferredRole);
            return data;
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
                            if (isUserDeleted(uid, rawEmail)) {
                                continue;
                            }

                            Map<String, Object> existingProf = getCachedUserProfile(uid, rawEmail);
                            String existingRole = (existingProf != null && existingProf.get("role") != null && !existingProf.get("role").toString().isBlank())
                                    ? existingProf.get("role").toString().trim() : null;

                            String name = user.getDisplayName() != null && !user.getDisplayName().isBlank()
                                    ? user.getDisplayName()
                                    : (existingProf != null && existingProf.get("name") != null && !existingProf.get("name").toString().isBlank()
                                            ? existingProf.get("name").toString()
                                            : (rawEmail.contains("@") ? rawEmail.substring(0, rawEmail.indexOf("@")) : "User"));

                            String inferredRole = (existingRole != null && !existingRole.isBlank()) ? existingRole : "Exporter";
                            if (existingRole == null) {
                                if (rawEmail.equalsIgnoreCase("docktech@gmail.com") || rawEmail.contains("docktech") || rawEmail.equalsIgnoreCase("babu@gmail.com") || rawEmail.contains("admin")) inferredRole = "Admin";
                                else if (rawEmail.contains("captain")) inferredRole = "Ship Captain";
                                else if (rawEmail.contains("officer") || rawEmail.contains("custom")) inferredRole = "Custom Officer";
                                else if (isPortEmailOrKeywords(rawEmail) || hasApprovedPortAuthority(rawEmail)) inferredRole = "Port Authority";
                                else if (rawEmail.contains("importer")) inferredRole = "Importer";
                            }

                            Map<String, Object> docData = new HashMap<>();
                            if (existingProf != null) {
                                docData.putAll(existingProf);
                            }
                            docData.put("uid", uid);
                            docData.put("email", rawEmail);
                            docData.put("name", name);
                            docData.put("role", inferredRole);
                            docData.put("status", "Active");
                            docData.put("updatedAt", LocalDateTime.now().toString());

                            if (db != null) {
                                db.collection("users").document(rawEmail).set(docData, com.google.cloud.firestore.SetOptions.merge());
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
                    // Build into a temp map first so the store is never left empty on partial failure
                    Map<String, Map<String, Object>> tmpStore = new java.util.concurrent.ConcurrentHashMap<>();
                    for (com.google.cloud.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        if (doc.exists() && doc.getData() != null) {
                            Map<String, Object> data = new HashMap<>(doc.getData());
                            String uid = data.get("uid") != null ? data.get("uid").toString() : doc.getId();
                            String email = data.get("email") != null ? data.get("email").toString() : "";
                            data.put("uid", uid);

                            if (isUserDeleted(uid, email)) {
                                continue;
                            }

                            if (!email.isBlank() && email.contains("@")) {
                                String cleanEmail = email.trim().toLowerCase();
                                if (!doc.getId().equalsIgnoreCase(cleanEmail)) {
                                    try {
                                        db.collection("users").document(cleanEmail).set(data, com.google.cloud.firestore.SetOptions.merge());
                                        db.collection("users").document(doc.getId()).delete();
                                        System.out.println("[FIRESTORE MIGRATION] Migrated legacy user doc ID '" + doc.getId() + "' -> users/" + cleanEmail);
                                    } catch (Exception ex) {
                                        System.err.println("[FIRESTORE MIGRATION WARNING] " + ex.getMessage());
                                    }
                                }
                            }

                            if (data.get("name") == null || data.get("name").toString().isBlank()) {
                                if (data.get("fullName") != null && !data.get("fullName").toString().isBlank()) {
                                    data.put("name", data.get("fullName").toString());
                                } else if (data.get("displayName") != null && !data.get("displayName").toString().isBlank()) {
                                    data.put("name", data.get("displayName").toString());
                                }
                            }
                            if (data.get("role") == null || data.get("role").toString().isBlank()) {
                                if (data.get("roleCode") != null && !data.get("roleCode").toString().isBlank()) {
                                    data.put("role", data.get("roleCode").toString());
                                } else if (data.get("userRole") != null && !data.get("userRole").toString().isBlank()) {
                                    data.put("role", data.get("userRole").toString());
                                }
                            }
                            if (data.get("assignedPort") == null || data.get("assignedPort").toString().isBlank()) {
                                if (data.get("port") != null && !data.get("port").toString().isBlank()) {
                                    data.put("assignedPort", data.get("port").toString());
                                } else if (data.get("portName") != null && !data.get("portName").toString().isBlank()) {
                                    data.put("assignedPort", data.get("portName").toString());
                                }
                            }

                            if (!uid.isBlank()) tmpStore.put(uid, data);
                            if (!email.isBlank()) tmpStore.put(email.toLowerCase().trim(), data);
                        }
                    }
                    // Merge fetched data into the live store (preserving in-memory updates)
                    for (Map.Entry<String, Map<String, Object>> entry : tmpStore.entrySet()) {
                        String key = entry.getKey();
                        Map<String, Object> remoteData = entry.getValue();
                        Map<String, Object> existing = localProfileStore.get(key);
                        if (existing == null) {
                            localProfileStore.put(key, new HashMap<>(remoteData));
                        } else {
                            for (Map.Entry<String, Object> field : remoteData.entrySet()) {
                                Object rVal = field.getValue();
                                Object eVal = existing.get(field.getKey());
                                if (eVal == null || eVal.toString().isBlank() || eVal.toString().equalsIgnoreCase("Not Assigned")) {
                                    existing.put(field.getKey(), rVal);
                                } else if (rVal != null && !rVal.toString().isBlank() && !rVal.toString().equalsIgnoreCase("Not Assigned")) {
                                    existing.put(field.getKey(), rVal);
                                }
                            }
                        }
                    }
                    // Remove from live store any entries that Firestore says are gone (except deleted-user-guarded ones)
                    localProfileStore.entrySet().removeIf(entry -> {
                        Map<String, Object> p = entry.getValue();
                        if (p == null) return true;
                        String pUid = p.get("uid") != null ? p.get("uid").toString() : "";
                        String pEmail = p.get("email") != null ? p.get("email").toString() : "";
                        return !pUid.isBlank() && !tmpStore.containsKey(pUid)
                            && !pEmail.isBlank() && !tmpStore.containsKey(pEmail.toLowerCase().trim());
                    });
                    System.out.println("[UserDao] Synced " + snap.size() + " user documents from Firestore collection 'users'.");
                    return;
                }
            }
        } catch (Exception ex) {
            System.err.println("[UserDao] syncFirestoreUsers Exception: " + ex.getMessage());
        }

        // REST fallback when Admin SDK is not initialized
        try {
            URI uri = URI.create("https://firestore.googleapis.com/v1/projects/" + PROJECT_ID + "/databases/(default)/documents/users?key=" + API_KEY);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                parseAndStoreFirestoreDocuments(response.body());
                System.out.println("[UserDao] Synced user documents via Firestore REST API.");
            }
        } catch (Exception ex) {
            System.err.println("[UserDao] syncFirestoreUsers REST Exception: " + ex.getMessage());
        }
    }

    private static void parseAndStoreFirestoreDocuments(String json) {
        if (json == null || !json.contains("\"documents\"")) return;
        try {
            String[] docChunks = json.split("\\{\\s*\"name\"\\s*:\\s*\"");
            for (String chunk : docChunks) {
                if (!chunk.contains("/documents/users/")) continue;
                int pathStart = chunk.indexOf("/documents/users/") + "/documents/users/".length();
                int quoteEnd = chunk.indexOf("\"", pathStart);
                if (quoteEnd == -1) continue;
                String docId = chunk.substring(pathStart, quoteEnd);

                Map<String, Object> data = new HashMap<>();
                String uid = extractFirestoreField(chunk, "uid");
                if (uid == null || uid.isBlank()) uid = docId;
                data.put("uid", uid);
                data.put("email", extractFirestoreField(chunk, "email"));
                
                String name = extractFirestoreField(chunk, "name");
                if (name == null || name.isBlank()) name = extractFirestoreField(chunk, "fullName");
                if (name == null || name.isBlank()) name = extractFirestoreField(chunk, "displayName");
                data.put("name", name);

                data.put("organization", extractFirestoreField(chunk, "organization"));
                data.put("company", extractFirestoreField(chunk, "company"));
                data.put("phone", extractFirestoreField(chunk, "phone"));
                
                String role = extractFirestoreField(chunk, "role");
                if (role == null || role.isBlank()) role = extractFirestoreField(chunk, "roleCode");
                if (role == null || role.isBlank()) role = extractFirestoreField(chunk, "userRole");
                data.put("role", role);

                String st = extractFirestoreField(chunk, "status");
                data.put("status", st != null && !st.isBlank() ? st : "Active");
                data.put("assignedShip", extractFirestoreField(chunk, "assignedShip"));
                data.put("assignedShipId", extractFirestoreField(chunk, "assignedShipId"));
                data.put("shipImo", extractFirestoreField(chunk, "shipImo"));
                data.put("duty", extractFirestoreField(chunk, "duty"));
                data.put("assignmentStatus", extractFirestoreField(chunk, "assignmentStatus"));

                String port = extractFirestoreField(chunk, "assignedPort");
                if (port == null || port.isBlank()) port = extractFirestoreField(chunk, "port");
                if (port == null || port.isBlank()) port = extractFirestoreField(chunk, "portName");
                data.put("assignedPort", port);

                data.put("assignedBy", extractFirestoreField(chunk, "assignedBy"));
                data.put("dutyPriority", extractFirestoreField(chunk, "dutyPriority"));
                data.put("dutyDueDate", extractFirestoreField(chunk, "dutyDueDate"));
                data.put("captainId", extractFirestoreField(chunk, "captainId"));
                data.put("officerId", extractFirestoreField(chunk, "officerId"));
                data.put("createdAt", extractFirestoreField(chunk, "createdAt"));
                data.put("updatedAt", extractFirestoreField(chunk, "updatedAt"));

                String email = data.get("email") != null ? data.get("email").toString() : "";
                if (isUserDeleted(uid, email)) continue;

                if (!uid.isBlank()) {
                    Map<String, Object> exUid = localProfileStore.get(uid);
                    if (exUid == null) {
                        localProfileStore.put(uid, new HashMap<>(data));
                    } else {
                        for (Map.Entry<String, Object> f : data.entrySet()) {
                            Object rV = f.getValue();
                            Object eV = exUid.get(f.getKey());
                            if (eV == null || eV.toString().isBlank() || eV.toString().equalsIgnoreCase("Not Assigned")) {
                                exUid.put(f.getKey(), rV);
                            } else if (rV != null && !rV.toString().isBlank() && !rV.toString().equalsIgnoreCase("Not Assigned")) {
                                exUid.put(f.getKey(), rV);
                            }
                        }
                    }
                }
                if (!email.isBlank()) {
                    String emKey = email.toLowerCase().trim();
                    Map<String, Object> exEm = localProfileStore.get(emKey);
                    if (exEm == null) {
                        localProfileStore.put(emKey, new HashMap<>(data));
                    } else {
                        for (Map.Entry<String, Object> f : data.entrySet()) {
                            Object rV = f.getValue();
                            Object eV = exEm.get(f.getKey());
                            if (eV == null || eV.toString().isBlank() || eV.toString().equalsIgnoreCase("Not Assigned")) {
                                exEm.put(f.getKey(), rV);
                            } else if (rV != null && !rV.toString().isBlank() && !rV.toString().equalsIgnoreCase("Not Assigned")) {
                                exEm.put(f.getKey(), rV);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("[UserDao parseAndStoreFirestoreDocuments Error] " + ex.getMessage());
        }
    }

    public static String getUidForEmail(String email) {
        if (email == null || email.isBlank()) return null;
        String e = email.toLowerCase().trim();
        Map<String, Object> p = localProfileStore.get(e);
        if (p != null && p.get("uid") != null) return p.get("uid").toString();
        for (Map<String, Object> val : localProfileStore.values()) {
            if (val != null && val.get("email") != null && e.equalsIgnoreCase(val.get("email").toString())) {
                if (val.get("uid") != null) return val.get("uid").toString();
            }
        }
        return null;
    }

    public static java.util.List<Map<String, Object>> getFirebaseAuthUsersWithProfiles() {
        cleanseAdminAccounts();
        com.projectui.config.FirebaseConfig.getFirestore();
        syncFirestoreUsers();

        java.util.Map<String, Map<String, Object>> usersByEmail = new java.util.LinkedHashMap<>();

        // Always register Docktech as the primary Master Admin
        Map<String, Object> masterAdminEntry = new HashMap<>();
        masterAdminEntry.put("uid", "master-admin-docktech");
        masterAdminEntry.put("email", "docktech@gmail.com");
        masterAdminEntry.put("name", "Docktech");
        masterAdminEntry.put("role", "Master Admin");
        masterAdminEntry.put("status", "Active");
        masterAdminEntry.put("createdAt", "01 Jan 2026, 09:00");
        masterAdminEntry.put("updatedAt", "Just now");
        usersByEmail.put("docktech@gmail.com", masterAdminEntry);

        // Populate strictly from genuine Firestore user profiles
        for (Map<String, Object> prof : localProfileStore.values()) {
            if (prof == null) continue;
            String email = prof.get("email") != null ? prof.get("email").toString().toLowerCase().trim() : "";
            String uid = prof.get("uid") != null ? prof.get("uid").toString().trim() : "";

            if (isUserDeleted(uid, email)) {
                continue;
            }

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

                String role = prof.get("role") != null ? prof.get("role").toString().trim() : "Exporter";
                if (("Admin".equalsIgnoreCase(role) || "Master Admin".equalsIgnoreCase(role)) && !email.equalsIgnoreCase("docktech@gmail.com") && !email.equalsIgnoreCase("babu@gmail.com") && !email.contains("admin")) {
                    role = "Exporter";
                }
                String status = prof.get("status") != null ? prof.get("status").toString().trim() : "Active";
                String createdAt = prof.get("createdAt") != null ? prof.get("createdAt").toString().trim() : "Registered";
                String updatedAt = prof.get("updatedAt") != null ? prof.get("updatedAt").toString().trim() : "Recently";

                Map<String, Object> userMap = new HashMap<>(prof);
                userMap.put("uid", bestUid);
                userMap.put("email", email);
                userMap.put("name", name);
                userMap.put("role", role);
                userMap.put("status", status);
                userMap.put("createdAt", createdAt);
                userMap.put("updatedAt", updatedAt);

                usersByEmail.put(key, userMap);
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

            final String docUid = resolveUserDocId(email != null && !email.isBlank() ? email : uid);
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

    public CompletableFuture<Boolean> updateUserProfileFields(String uidOrEmail, Map<String, Object> fieldsToUpdate) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        if (uidOrEmail == null || uidOrEmail.isBlank() || fieldsToUpdate == null || fieldsToUpdate.isEmpty()) {
            future.complete(false);
            return future;
        }

        ForkJoinPool.commonPool().execute(() -> {
            try {
                String key = uidOrEmail.toLowerCase().trim();
                Map<String, Object> profile = localProfileStore.get(key);
                if (profile == null) profile = localProfileStore.get(uidOrEmail.trim());
                if (profile == null) profile = readFirestoreRest(uidOrEmail);
                if (profile == null) profile = new HashMap<>();

                profile.putAll(fieldsToUpdate);
                String uid = profile.get("uid") != null ? profile.get("uid").toString() : uidOrEmail;
                String email = profile.get("email") != null ? profile.get("email").toString().toLowerCase() : key;

                localProfileStore.put(uid, profile);
                localProfileStore.put(email, profile);

                if (com.projectui.view.login.LoginPage.loggedInUser != null) {
                    Object currUid = com.projectui.view.login.LoginPage.loggedInUser.get("uid");
                    Object currEmail = com.projectui.view.login.LoginPage.loggedInUser.get("email");
                    if ((currUid != null && currUid.toString().equalsIgnoreCase(uid)) ||
                        (currEmail != null && currEmail.toString().equalsIgnoreCase(email))) {
                        com.projectui.view.login.LoginPage.loggedInUser.putAll(fieldsToUpdate);
                    }
                }

                String docId = resolveUserDocId(email != null && !email.isBlank() ? email : uid);
                Firestore db = db();
                if (db != null) {
                    db.collection("users").document(docId).set(fieldsToUpdate, com.google.cloud.firestore.SetOptions.merge());
                } else {
                    writeFirestoreRest(docId, fieldsToUpdate);
                }
                future.complete(true);
            } catch (Exception ex) {
                System.err.println("[UserDao updateUserProfileFields] " + ex.getMessage());
                future.complete(false);
            }
        });
        return future;
    }

    public CompletableFuture<Void> deleteUserAccount(String uidOrEmail) {
        return deleteUserAccount(uidOrEmail, uidOrEmail);
    }

    public CompletableFuture<Void> deleteUserAccount(String targetUid, String targetEmail) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if ((targetUid == null || targetUid.isBlank()) && (targetEmail == null || targetEmail.isBlank())) {
            future.complete(null);
            return future;
        }

        ForkJoinPool.commonPool().execute(() -> {
            try {
                String email = targetEmail != null ? targetEmail.toLowerCase().trim() : "";
                String uid = targetUid != null ? targetUid.trim() : "";

                Map<String, Object> profile = null;
                if (!uid.isBlank()) profile = localProfileStore.get(uid);
                if (profile == null && !email.isBlank()) profile = localProfileStore.get(email);
                if (profile == null && !uid.isBlank()) profile = readFirestoreRest(uid);
                if (profile == null && !email.isBlank()) profile = readFirestoreRest(email);

                String role = profile != null && profile.get("role") != null ? profile.get("role").toString() : "";
                if (email.isBlank() && profile != null && profile.get("email") != null) {
                    email = profile.get("email").toString().toLowerCase().trim();
                }
                if (uid.isBlank() && profile != null && profile.get("uid") != null) {
                    uid = profile.get("uid").toString().trim();
                }

                // Security Guard: Protect the system Administrator accounts
                if ("Admin".equalsIgnoreCase(role) || "Master Admin".equalsIgnoreCase(role) || "docktech@gmail.com".equalsIgnoreCase(email) || "babu@gmail.com".equalsIgnoreCase(email) || "admin@docktech.com".equalsIgnoreCase(email)) {
                    System.err.println("[SECURITY GUARD] Refused to delete system Admin account: " + email);
                    future.completeExceptionally(new SecurityException("Cannot delete the system administrator."));
                    return;
                }

                String userName = profile != null && profile.get("name") != null ? profile.get("name").toString() : (email.contains("@") ? email.substring(0, email.indexOf("@")) : "User");

                // 1. Delete from Firebase Authentication
                boolean authDeleted = false;
                if (com.projectui.config.FirebaseConfig.hasAdminAuth()) {
                    try {
                        if (!uid.isBlank() && !uid.startsWith("usr_")) {
                            com.google.firebase.auth.FirebaseAuth.getInstance().deleteUser(uid);
                            authDeleted = true;
                            System.out.println("[Firebase Auth] Deleted user by UID: " + uid);
                        }
                    } catch (Throwable ex) {
                        System.err.println("[Firebase Auth deleteUser by UID warning] " + ex.getMessage());
                    }

                    if (!authDeleted && !email.isBlank()) {
                        try {
                            com.google.firebase.auth.UserRecord authRec = com.google.firebase.auth.FirebaseAuth.getInstance().getUserByEmail(email);
                            if (authRec != null && authRec.getUid() != null) {
                                com.google.firebase.auth.FirebaseAuth.getInstance().deleteUser(authRec.getUid());
                                System.out.println("[Firebase Auth] Deleted user by Email lookup UID: " + authRec.getUid());
                            }
                        } catch (Throwable ex) {
                            System.err.println("[Firebase Auth getUserByEmail delete warning] " + ex.getMessage());
                        }
                    }
                }

                // 2. Delete from Firestore Database
                Firestore db = new UserDao().db();
                if (db != null) {
                    try {
                        if (!uid.isBlank()) {
                            db.collection("users").document(uid).delete().get();
                        }
                        if (!email.isBlank()) {
                            db.collection("users").document(email).delete().get();
                        }

                        // Query collection for any leftover records by email
                        if (!email.isBlank()) {
                            com.google.api.core.ApiFuture<com.google.cloud.firestore.QuerySnapshot> snapFuture = db.collection("users").whereEqualTo("email", email).get();
                            com.google.cloud.firestore.QuerySnapshot snap = snapFuture.get();
                            if (snap != null) {
                                for (com.google.cloud.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                                    doc.getReference().delete();
                                }
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("[UserDao Firestore Delete Error] " + ex.getMessage());
                    }
                }

                // REST fallback deletion
                if (!uid.isBlank()) deleteFirestoreRest(uid);
                if (!email.isBlank()) deleteFirestoreRest(email);

                // 3. Clean up local in-memory stores and persist deletion
                if (!uid.isBlank()) localProfileStore.remove(uid);
                if (!email.isBlank()) {
                    localProfileStore.remove(email);
                    localProfileStore.remove(email.toLowerCase());
                }
                final String finalUid = uid;
                final String finalEmail = email;
                saveDeletedUser(finalUid, finalEmail);
                removeLocalDiskUser(finalUid, finalEmail);
                com.projectui.view.admin.AdminDataStore.getInstance().users.removeIf(u ->
                        (finalUid != null && !finalUid.isBlank() && finalUid.equalsIgnoreCase(u.uid)) ||
                        (finalEmail != null && !finalEmail.isBlank() && finalEmail.equalsIgnoreCase(u.email))
                );

                logActivity(userName, email, "USER_DELETED", "User account " + userName + " (" + email + ") permanently deleted by Admin");
                System.out.println("[UserDao] Permanently deleted user: " + userName + " (UID: " + uid + ", Email: " + email + ")");

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

    private static void writeFirestoreRest(String uidOrEmail, Map<String, Object> userData) throws Exception {
        String docId = resolveUserDocId(userData != null && userData.get("email") != null ? userData.get("email").toString() : uidOrEmail);
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
                "https://firestore.googleapis.com/v1/projects/" + PROJECT_ID + "/databases/(default)/documents/users/" + docId
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

    private static Map<String, Object> readFirestoreRest(String uidOrEmail) {
        try {
            String docId = resolveUserDocId(uidOrEmail);
            URI uri = URI.create(
                    "https://firestore.googleapis.com/v1/projects/" + PROJECT_ID + "/databases/(default)/documents/users/"
                            + docId + "?key=" + API_KEY);
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
                    System.out.println("[PROFILE FOUND (REST)] users/" + docId + " -> email=" + data.get("email")
                            + ", role=" + data.get("role") + ", status=" + data.get("status"));
                    localProfileStore.put(docId, data);
                    if (data.get("uid") != null) localProfileStore.put(data.get("uid").toString(), data);
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

    private static void deleteFirestoreRest(String uid) {
        if (uid == null || uid.isBlank()) return;
        try {
            URI uri = URI.create(
                    "https://firestore.googleapis.com/v1/projects/" + PROJECT_ID + "/databases/(default)/documents/users/" + uid
                            + "?key=" + API_KEY);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .DELETE()
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignored) {}
    }
}
