package com.projectui.dao;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
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

    private static final Map<String, Map<String, Object>> localProfileStore = new ConcurrentHashMap<>();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String API_KEY = System.getenv("FIREBASE_API_KEY") != null
            && !System.getenv("FIREBASE_API_KEY").isBlank()
                    ? System.getenv("FIREBASE_API_KEY")
                    : "AIzaSyDO3l4YIlfgO9qvU2XB54k2pBvayr_ID1U";

    private static final String PERSIST_USERS_FILE = System.getProperty("user.home") + "/.docktech_users.properties";

    static {
        restoreLocalDiskUsers();
        ensureDefaultPortAuthorities();
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
                        data.put("tempPassword", tempPassword);
                        data.put("password", tempPassword);
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
            if (userData.get("tempPassword") != null) props.setProperty(prefix + ".tempPassword", userData.get("tempPassword").toString());
            if (userData.get("accountId") != null) props.setProperty(prefix + ".accountId", userData.get("accountId").toString());
            if (userData.get("phone") != null) props.setProperty(prefix + ".phone", userData.get("phone").toString());

            try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                props.store(out, "DockTech User Profiles");
            }
        } catch (Exception ignored) {}
    }

    public static void ensureDefaultPortAuthorities() {
        seedPortAuthority("pa_mumbai@docktech.com", "Mumbai Port Authority", "Port of Mumbai");
        seedPortAuthority("pa_chennai@docktech.com", "Chennai Port Authority", "Port of Chennai");
        seedPortAuthority("pa_vizag@docktech.com", "Visakhapatnam Port Authority", "Port of Vizag");
        seedPortAuthority("pa_kolkata@docktech.com", "Kolkata Port Authority", "Port of Kolkata");
        seedPortAuthority("pa_cochin@docktech.com", "Cochin Port Authority", "Cochin Port");
        seedPortAuthority("pa_kochi@docktech.com", "Cochin Port Authority", "Cochin Port");
        cleanseAdminAccounts();
    }

    public static void cleanseAdminAccounts() {
        try {
            // 1. Remove unwanted admin keys from in-memory localProfileStore
            List<String> keysToRemove = new ArrayList<>();
            for (Map.Entry<String, Map<String, Object>> entry : localProfileStore.entrySet()) {
                Map<String, Object> prof = entry.getValue();
                if (prof == null) continue;
                String email = prof.get("email") != null ? prof.get("email").toString().trim().toLowerCase() : "";
                String role = prof.get("role") != null ? prof.get("role").toString().trim() : "";
                if (email.equalsIgnoreCase("pravin@gmail.com") || email.equalsIgnoreCase("goa@gmail.com")) {
                    keysToRemove.add(entry.getKey());
                } else if ("Admin".equalsIgnoreCase(role) && !email.equalsIgnoreCase("babu@gmail.com") && !email.contains("admin")) {
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
            for (String k : keysToRemove) {
                localProfileStore.remove(k);
            }

            // 2. Ensure Babu is the single Admin profile in local storage
            Map<String, Object> babuProfile = new HashMap<>();
            babuProfile.put("uid", "usr_" + Math.abs("babu@gmail.com".hashCode()));
            babuProfile.put("email", "babu@gmail.com");
            babuProfile.put("name", "Babu");
            babuProfile.put("role", "Admin");
            babuProfile.put("roleCode", "ADMIN");
            babuProfile.put("status", "Active");
            babuProfile.put("organization", "Dock Tech Administration");
            babuProfile.put("company", "Dock Tech");
            babuProfile.put("createdAt", "01 Jan 2026, 09:00");
            localProfileStore.put("babu@gmail.com", babuProfile);
            localProfileStore.put(babuProfile.get("uid").toString(), babuProfile);
            saveLocalDiskUser(babuProfile);

            // 3. Remove unwanted Admin entries from AdminDataStore.users
            AdminDataStore store = AdminDataStore.getInstance();
            store.adminName = "Babu";
            store.adminEmail = "babu@gmail.com";
            store.users.removeIf(u -> u.email != null && (u.email.equalsIgnoreCase("pravin@gmail.com") || u.email.equalsIgnoreCase("goa@gmail.com") || ("Admin".equalsIgnoreCase(u.role) && !u.email.equalsIgnoreCase("babu@gmail.com"))));

            boolean hasBabu = false;
            for (AdminDataStore.UserAccount u : store.users) {
                if (u.email != null && u.email.equalsIgnoreCase("babu@gmail.com")) {
                    hasBabu = true;
                    u.name = "Babu";
                    u.role = "Admin";
                    break;
                }
            }
            if (!hasBabu) {
                store.users.add(0, new AdminDataStore.UserAccount(babuProfile.get("uid").toString(), "Babu", "babu@gmail.com", "Admin", "Active", "01 Jan 2026, 09:00", "Just now"));
            }

            // 4. Background purge from Firestore and ~/.docktech_users.properties
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
                                    if (em != null && (em.equalsIgnoreCase("pravin@gmail.com") || em.equalsIgnoreCase("goa@gmail.com"))) {
                                        db.collection("users").document(dId).delete();
                                        System.out.println("[CLEANSE] Deleted invalid user from Firestore: " + em);
                                    } else if ("Admin".equalsIgnoreCase(rl) && em != null && !em.equalsIgnoreCase("babu@gmail.com") && !em.contains("admin")) {
                                        db.collection("users").document(dId).delete();
                                        System.out.println("[CLEANSE] Deleted invalid Admin document from Firestore: " + em);
                                    }
                                }
                            }
                        }
                        db.collection("users").document(babuProfile.get("uid").toString()).set(babuProfile, com.google.cloud.firestore.SetOptions.merge());
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
                            if (key.toLowerCase().contains("pravin@gmail.com") || key.toLowerCase().contains("goa@gmail.com") || (val != null && (val.toLowerCase().contains("pravin@gmail.com") || val.toLowerCase().contains("goa@gmail.com")))) {
                                pToRemove.add(key);
                            } else if (val != null && val.contains("role=Admin") && !key.equalsIgnoreCase("babu@gmail.com") && !val.contains("babu@gmail.com")) {
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
                }
            }
        } catch (Exception ignored) {}

        return raw.trim();
    }

    public static List<Map<String, Object>> getPersonnelForPort(String portName, String roleKeyword) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (portName == null || roleKeyword == null) return list;
        String normPort = normalizePort(portName);

        for (Map<String, Object> prof : localProfileStore.values()) {
            if (prof == null) continue;
            Object r = prof.get("role");
            if (r == null) continue;
            String rStr = r.toString().toLowerCase();
            if (!rStr.contains(roleKeyword.toLowerCase())) continue;

            Object p = prof.get("assignedPort");
            String pStr = p != null ? p.toString() : (prof.get("company") != null ? prof.get("company").toString() : "");
            if (pStr.isBlank() || normalizePort(pStr).equalsIgnoreCase(normPort)) {
                boolean exists = false;
                for (Map<String, Object> item : list) {
                    if (item.get("uid") != null && item.get("uid").equals(prof.get("uid"))) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    list.add(new HashMap<>(prof));
                }
            }
        }

        return list;
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

        final String finalTargetId = (docUid != null && !docUid.isBlank()) ? docUid : rawKey;
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
        if (uidOrEmail == null) return;
        String key = uidOrEmail.toLowerCase().trim();
        String nowStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));

        for (Map<String, Object> prof : localProfileStore.values()) {
            if (prof == null) continue;
            Object u = prof.get("uid");
            Object e = prof.get("email");
            if ((u != null && u.toString().equalsIgnoreCase(key)) || (e != null && e.toString().equalsIgnoreCase(key))) {
                prof.put("duty", (duty != null && !duty.isBlank()) ? duty : "Not Assigned");
                prof.put("assignmentStatus", (assignmentStatus != null && !assignmentStatus.isBlank()) ? assignmentStatus : "Active");
                if (assignedPort != null && !assignedPort.isBlank()) prof.put("assignedPort", assignedPort);
                if (assignedBy != null && !assignedBy.isBlank()) prof.put("assignedBy", assignedBy);
                if (priority != null && !priority.isBlank()) prof.put("dutyPriority", priority);
                if (dueDate != null && !dueDate.isBlank()) prof.put("dutyDueDate", dueDate);
                prof.put("updatedAt", nowStr);

                if (u != null) {
                    try {
                        Firestore db = new UserDao().db();
                        if (db != null) {
                            Map<String, Object> update = new HashMap<>();
                            update.put("duty", prof.get("duty"));
                            update.put("assignmentStatus", prof.get("assignmentStatus"));
                            if (assignedPort != null && !assignedPort.isBlank()) update.put("assignedPort", assignedPort);
                            if (prof.containsKey("assignedBy")) update.put("assignedBy", prof.get("assignedBy"));
                            if (prof.containsKey("dutyPriority")) update.put("dutyPriority", prof.get("dutyPriority"));
                            if (prof.containsKey("dutyDueDate")) update.put("dutyDueDate", prof.get("dutyDueDate"));
                            update.put("updatedAt", nowStr);
                            db.collection("users").document(u.toString()).set(update, com.google.cloud.firestore.SetOptions.merge());
                        }
                    } catch (Exception ignored) {}
                }
            }
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
                        String docUid = u != null ? u.toString() : key;
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

        // If ship is still not found, check the user's notifications in Firestore for any SHIP_ASSIGNED / SHIP_CHANGED notification
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

        // Fallback: construct full Ship from real assigned name
        if (!shipName.isBlank()) {
            Ship s = new Ship(
                    !shipId.isBlank() ? shipId : "SHIP-" + Math.abs(shipName.hashCode()),
                    shipName,
                    "Container Vessel",
                    !shipImo.isBlank() ? shipImo : "IMO 923456",
                    "Ocean Shipping Ltd",
                    "18:00",
                    "Port of Singapore",
                    "Berth 4",
                    "At Berth"
            );
            new ShipDAO().saveShip(s);
            return s;
        }

        return null;
    }

    private static void seedPortAuthority(String email, String name, String portName) {
        String uid = "usr_" + Math.abs(email.toLowerCase().hashCode());
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("email", email);
        data.put("name", name);
        data.put("organization", name);
        data.put("company", name);
        data.put("assignedPort", portName);
        data.put("role", "Port Authority");
        data.put("status", "Active");
        data.put("createdAt", LocalDateTime.now().toString());

        localProfileStore.put(uid, data);
        localProfileStore.put(email.toLowerCase(), data);
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
            System.out.println("Firestore document UID: " + uid);

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
            localProfileStore.put(uid, userData);
            if (email != null && !email.isBlank()) {
                localProfileStore.put(email.toLowerCase().trim(), userData);
            }
            saveLocalDiskUser(userData);

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
            if ("Port Authority".equalsIgnoreCase(role) || "PORT_AUTHORITY".equalsIgnoreCase(role)) {
                userData.put("roleCode", "PORT_AUTHORITY");
            }
            userData.put("status", status != null && !status.isBlank() ? status : "Active");
            userData.put("mustChangePassword", false);
            userData.put("createdAt", LocalDateTime.now().toString());

            if (extraFields != null) {
                userData.putAll(extraFields);
            }

            localProfileStore.put(uid, userData);
            if (email != null && !email.isBlank()) {
                localProfileStore.put(email.toLowerCase().trim(), userData);
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
            } else if (role.toLowerCase().contains("authority") || role.toUpperCase().contains("PORT")) {
                id = (email != null && !email.isBlank()) ? email.trim() : ("PA-" + String.format("%03d", (int)(Math.random() * 899 + 1)));
            } else {
                id = "CO-" + String.format("%03d", (int)(Math.random() * 899 + 1));
            }

            Map<String, Object> extended = extraFields != null ? new HashMap<>(extraFields) : new HashMap<>();
            extended.put("tempPassword", tempPassword);
            extended.put("password", tempPassword);
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
                if (!extended.containsKey("assignedPort")) extended.put("assignedPort", company != null ? company : "Mumbai Port");
            } else {
                extended.put("officerId", id);
                extended.put("roleCode", "CUSTOMS_OFFICER");
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

    public Map<String, Object> getCachedUserProfile(String uid, String email) {
        String uidKey = uid != null ? uid.trim() : null;
        String emailKey = email != null ? email.trim().toLowerCase() : null;

        if (uidKey != null && localProfileStore.containsKey(uidKey)) {
            Map<String, Object> prof = localProfileStore.get(uidKey);
            if (prof != null && prof.containsKey("role") && prof.get("role") != null) {
                return prof;
            }
        }
        if (emailKey != null && localProfileStore.containsKey(emailKey)) {
            Map<String, Object> prof = localProfileStore.get(emailKey);
            if (prof != null && prof.containsKey("role") && prof.get("role") != null) {
                return prof;
            }
        }
        return null;
    }

    public CompletableFuture<Map<String, Object>> getUserProfile(String uid) {
        return getUserProfile(uid, null);
    }

    public CompletableFuture<Map<String, Object>> getUserProfile(String uid, String email) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

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
        if (e.contains("admin") || e.equalsIgnoreCase("babu@gmail.com")) return false;

        if (e.contains("port") || e.contains("authority") || e.startsWith("pa_") || e.startsWith("pa-") || e.contains("pa@") || e.contains("pa.")
                || e.contains("kochin") || e.contains("cochin") || e.contains("mumbai") || e.contains("chennai")
                || e.contains("vizag") || e.contains("visakhapatnam") || e.contains("kolkata") || e.contains("calcutta")
                || e.contains("hazira") || e.contains("jnpt") || e.contains("kandla") || e.contains("mundra")
                || e.contains("paradip") || e.contains("paradeep") || e.contains("tuticorin") || e.contains("mormugao")
                || e.contains("mangalore") || e.contains("harbor") || e.contains("dock")) {
            return true;
        }

        try {
            for (AdminDataStore.PortRequest req : AdminDataStore.getInstance().portRequests) {
                if ((req.proposedPaEmail != null && req.proposedPaEmail.equalsIgnoreCase(e))
                 || (req.requesterEmail != null && req.requesterEmail.equalsIgnoreCase(e))) {
                    return true;
                }
            }
            for (Map<String, Object> reqMap : PortRequestDao.getLocalRequestStore().values()) {
                Object paE = reqMap.get("proposedPaEmail");
                Object reqE = reqMap.get("requesterEmail");
                if ((paE != null && paE.toString().equalsIgnoreCase(e))
                 || (reqE != null && reqE.toString().equalsIgnoreCase(e))) {
                    return true;
                }
            }
            for (AdminDataStore.Port p : AdminDataStore.getInstance().ports) {
                if (p.name != null) {
                    String pClean = p.name.toLowerCase().replaceAll("[^a-z0-9]", "");
                    String eClean = e.replaceAll("[^a-z0-9]", "");
                    if (eClean.contains(pClean) || pClean.contains(eClean.split("@")[0])) return true;
                }
            }
        } catch (Throwable ignored) {}

        return false;
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
            for (String altKey : new String[] { "roleCode", "userRole", "accountRole", "user_role", "roleName", "role_name", "type", "accountType", "roles" }) {
                if (data.get(altKey) != null && !data.get(altKey).toString().isBlank()) {
                    roleVal = data.get(altKey).toString().trim();
                    break;
                }
            }
        }
        if (roleVal.isBlank() || ((roleVal.equalsIgnoreCase("Admin") || roleVal.equalsIgnoreCase("User")) && (!emailVal.equalsIgnoreCase("babu@gmail.com") && !emailVal.contains("admin")))) {
            if (!emailVal.isBlank() && (emailVal.contains("admin") || emailVal.equalsIgnoreCase("babu@gmail.com"))) {
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

        // 4. Scan AdminDataStore for matching user or port requests
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
                Object paE = reqMap.get("proposedPaEmail");
                Object reqE = reqMap.get("requesterEmail");
                Object reqId = reqMap.get("requestId");
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
                    Object pName = reqMap.get("portName");
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

        // 5. Automatic Auth -> Firestore User Sync if user authenticated via Firebase Auth but has no profile
        if (emailKey != null && !emailKey.isBlank()) {
            String rawEmail = emailKey.trim().toLowerCase();
            String name = rawEmail.contains("@") ? rawEmail.substring(0, rawEmail.indexOf("@")) : "User";
            if (name.length() > 1) {
                name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }

            String inferredRole = "Exporter";
            if (rawEmail.equalsIgnoreCase("babu@gmail.com") || rawEmail.contains("admin")) {
                inferredRole = "Admin";
            } else if (rawEmail.contains("captain")) {
                inferredRole = "Ship Captain";
            } else if (rawEmail.contains("officer") || rawEmail.contains("custom")) {
                inferredRole = "Custom Officer";
            } else if (isPortEmailOrKeywords(rawEmail) || hasApprovedPortAuthority(rawEmail)) {
                inferredRole = "Port Authority";
            } else if (rawEmail.contains("importer") || rawEmail.contains("import")) {
                inferredRole = "Importer";
            } else if (rawEmail.contains("exporter") || rawEmail.contains("export")) {
                inferredRole = "Exporter";
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
                            if (rawEmail.equalsIgnoreCase("pravin@gmail.com") || rawEmail.equalsIgnoreCase("goa@gmail.com")) {
                                continue;
                            }
                            String name = user.getDisplayName() != null && !user.getDisplayName().isBlank()
                                    ? user.getDisplayName()
                                    : (rawEmail.contains("@") ? rawEmail.substring(0, rawEmail.indexOf("@")) : "User");

                            String inferredRole = "Exporter";
                            if (rawEmail.equalsIgnoreCase("babu@gmail.com") || rawEmail.contains("admin")) inferredRole = "Admin";
                            else if (rawEmail.contains("captain")) inferredRole = "Ship Captain";
                            else if (rawEmail.contains("officer") || rawEmail.contains("custom")) inferredRole = "Custom Officer";
                            else if (isPortEmailOrKeywords(rawEmail) || hasApprovedPortAuthority(rawEmail)) inferredRole = "Port Authority";
                            else if (rawEmail.contains("importer")) inferredRole = "Importer";

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
                            Map<String, Object> data = new HashMap<>(doc.getData());
                            String uid = data.get("uid") != null ? data.get("uid").toString() : doc.getId();
                            String email = data.get("email") != null ? data.get("email").toString() : "";
                            data.put("uid", uid);

                            if (email.equalsIgnoreCase("pravin@gmail.com") || email.equalsIgnoreCase("goa@gmail.com")) {
                                continue;
                            }

                            if (!uid.isBlank()) localProfileStore.put(uid, data);
                            if (!email.isBlank()) localProfileStore.put(email.toLowerCase(), data);
                        }
                    }
                    System.out.println("[UserDao] Synced " + snap.size() + " user documents from Firestore collection 'users'.");
                }
            }

            // Sync and backfill from Firebase Auth if Admin SDK is available
            if (com.projectui.config.FirebaseConfig.hasAdminAuth()) {
                try {
                    com.google.firebase.auth.ListUsersPage page = com.google.firebase.auth.FirebaseAuth.getInstance().listUsers(null);
                    while (page != null) {
                        for (com.google.firebase.auth.ExportedUserRecord user : page.getValues()) {
                            if (user == null) continue;
                            String uid = user.getUid();
                            String email = user.getEmail();
                            if (email != null && !email.isBlank()) {
                                String rawEmail = email.toLowerCase().trim();
                                if (rawEmail.equalsIgnoreCase("pravin@gmail.com") || rawEmail.equalsIgnoreCase("goa@gmail.com")) {
                                    continue;
                                }
                                Map<String, Object> existing = localProfileStore.get(uid);
                                if (existing == null) existing = localProfileStore.get(rawEmail);

                                if (existing == null) {
                                    String name = user.getDisplayName() != null && !user.getDisplayName().isBlank()
                                            ? user.getDisplayName()
                                            : (rawEmail.contains("@") ? rawEmail.substring(0, rawEmail.indexOf("@")) : "User");

                                    String inferredRole = "Exporter";
                                    if (rawEmail.equalsIgnoreCase("babu@gmail.com") || rawEmail.contains("admin")) inferredRole = "Admin";
                                    else if (rawEmail.contains("captain")) inferredRole = "Ship Captain";
                                    else if (rawEmail.contains("officer") || rawEmail.contains("custom")) inferredRole = "Custom Officer";
                                    else if (isPortEmailOrKeywords(rawEmail) || hasApprovedPortAuthority(rawEmail)) inferredRole = "Port Authority";
                                    else if (rawEmail.contains("importer")) inferredRole = "Importer";

                                    Map<String, Object> docData = new HashMap<>();
                                    docData.put("uid", uid);
                                    docData.put("email", rawEmail);
                                    docData.put("name", name);
                                    docData.put("role", inferredRole);
                                    docData.put("status", "Active");
                                    docData.put("company", "Ocean Shipping");
                                    docData.put("organization", "Ocean Shipping");
                                    docData.put("assignedPort", "Port of Mumbai");
                                    docData.put("assignedShip", "Not Assigned");
                                    docData.put("duty", "Not Assigned");
                                    docData.put("assignmentStatus", "Available");
                                    docData.put("id", (inferredRole.contains("Captain") ? "CAP-" : "CO-") + String.format("%03d", Math.abs(uid.hashCode() % 900 + 100)));
                                    docData.put("createdAt", LocalDateTime.now().toString());
                                    docData.put("updatedAt", LocalDateTime.now().toString());

                                    if (db != null) {
                                        db.collection("users").document(uid).set(docData, com.google.cloud.firestore.SetOptions.merge());
                                    }
                                    localProfileStore.put(uid, docData);
                                    localProfileStore.put(rawEmail, docData);
                                }
                            }
                        }
                        page = page.getNextPage();
                    }
                } catch (Throwable ignored) {}

                // Ensure any Port Authority accounts have real Firebase Auth accounts
                try {
                    for (Map<String, Object> prof : new java.util.ArrayList<>(localProfileStore.values())) {
                        if (prof == null) continue;
                        String role = prof.get("role") != null ? prof.get("role").toString() : "";
                        String email = prof.get("email") != null ? prof.get("email").toString().trim().toLowerCase() : "";
                        String name = prof.get("name") != null ? prof.get("name").toString() : "Port Authority";

                        if (role.toLowerCase().contains("authority") && !email.isBlank()) {
                            try {
                                com.google.firebase.auth.UserRecord exRec = com.google.firebase.auth.FirebaseAuth.getInstance().getUserByEmail(email);
                                if (exRec != null) {
                                    prof.put("uid", exRec.getUid());
                                    localProfileStore.put(exRec.getUid(), prof);
                                    if (db != null) {
                                        db.collection("users").document(exRec.getUid()).set(prof, com.google.cloud.firestore.SetOptions.merge());
                                    }
                                }
                            } catch (com.google.firebase.auth.FirebaseAuthException fae) {
                                if (fae.getMessage() != null && (fae.getMessage().contains("No user record found") || fae.getMessage().contains("user-not-found"))) {
                                    try {
                                        com.google.firebase.auth.UserRecord.CreateRequest cr = new com.google.firebase.auth.UserRecord.CreateRequest()
                                                .setEmail(email)
                                                .setPassword("Pass#9821")
                                                .setDisplayName(name);
                                        com.google.firebase.auth.UserRecord newRec = com.google.firebase.auth.FirebaseAuth.getInstance().createUser(cr);
                                        System.out.println("[Firebase Auth] Backfilled missing Port Authority Auth account: " + email + " (UID: " + newRec.getUid() + ")");
                                        prof.put("uid", newRec.getUid());
                                        localProfileStore.put(newRec.getUid(), prof);
                                        if (db != null) {
                                            db.collection("users").document(newRec.getUid()).set(prof, com.google.cloud.firestore.SetOptions.merge());
                                        }
                                    } catch (Throwable ignored) {}
                                }
                            } catch (Throwable ignored) {}
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Exception ex) {
            System.err.println("[UserDao] syncFirestoreUsers Exception: " + ex.getMessage());
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

        // Always register Babu as the primary Admin
        Map<String, Object> babuEntry = new HashMap<>();
        String babuUid = "usr_" + Math.abs("babu@gmail.com".hashCode());
        babuEntry.put("uid", babuUid);
        babuEntry.put("email", "babu@gmail.com");
        babuEntry.put("name", "Babu");
        babuEntry.put("role", "Admin");
        babuEntry.put("status", "Active");
        babuEntry.put("createdAt", "01 Jan 2026, 09:00");
        babuEntry.put("updatedAt", "Just now");
        usersByEmail.put("babu@gmail.com", babuEntry);

        if (com.projectui.config.FirebaseConfig.hasAdminAuth()) {
            try {
                com.google.firebase.auth.ListUsersPage page = com.google.firebase.auth.FirebaseAuth.getInstance().listUsers(null);
                if (page != null) {
                    while (page != null) {
                        for (com.google.firebase.auth.ExportedUserRecord authUser : page.getValues()) {
                            if (authUser == null) continue;
                            String uid = authUser.getUid();
                            String email = authUser.getEmail() != null ? authUser.getEmail().toLowerCase().trim() : "";
                            String authName = authUser.getDisplayName() != null ? authUser.getDisplayName().trim() : "";

                            if (email.equalsIgnoreCase("pravin@gmail.com") || email.equalsIgnoreCase("goa@gmail.com")) {
                                continue;
                            }

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
                                    : (email.equalsIgnoreCase("babu@gmail.com") || email.contains("admin") ? "Admin" : (email.contains("captain") ? "Ship Captain" : (email.contains("officer") || email.contains("custom") ? "Custom Officer" : (isPortEmailOrKeywords(email) || hasApprovedPortAuthority(email) ? "Port Authority" : "Exporter"))));

                            // Strict enforcement: Only Babu can have Admin role
                            if ("Admin".equalsIgnoreCase(role) && !email.equalsIgnoreCase("babu@gmail.com") && !email.contains("admin")) {
                                role = (isPortEmailOrKeywords(email) || hasApprovedPortAuthority(email)) ? "Port Authority" : (email.contains("captain") ? "Ship Captain" : (email.contains("officer") || email.contains("custom") ? "Custom Officer" : "Exporter"));
                            }
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
        }

        // Deduplicate synced Firestore user profiles by canonical email/UID
        for (Map<String, Object> prof : localProfileStore.values()) {
            if (prof == null) continue;
            String email = prof.get("email") != null ? prof.get("email").toString().toLowerCase().trim() : "";
            String uid = prof.get("uid") != null ? prof.get("uid").toString().trim() : "";

            if (email.equalsIgnoreCase("pravin@gmail.com") || email.equalsIgnoreCase("goa@gmail.com")) {
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

                String role = prof.get("role") != null ? prof.get("role").toString().trim() : "Port Authority";
                if ("Admin".equalsIgnoreCase(role) && !email.equalsIgnoreCase("babu@gmail.com") && !email.contains("admin")) {
                    role = (isPortEmailOrKeywords(email) || hasApprovedPortAuthority(email)) ? "Port Authority" : "Exporter";
                }
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
                    String r = prof.get("role").toString().trim();
                    if (!"Admin".equalsIgnoreCase(r) || email.equalsIgnoreCase("babu@gmail.com") || email.contains("admin")) {
                        existing.put("role", r);
                    }
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

                Firestore db = db();
                if (db != null) {
                    db.collection("users").document(uid).set(fieldsToUpdate, com.google.cloud.firestore.SetOptions.merge());
                } else {
                    writeFirestoreRest(uid, fieldsToUpdate);
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
                if ("Admin".equalsIgnoreCase(role) || "babu@gmail.com".equalsIgnoreCase(email) || "admin@docktech.com".equalsIgnoreCase(email)) {
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

                // 3. Clean up local in-memory stores
                if (!uid.isBlank()) localProfileStore.remove(uid);
                if (!email.isBlank()) {
                    localProfileStore.remove(email);
                    localProfileStore.remove(email.toLowerCase());
                }
                final String finalUid = uid;
                final String finalEmail = email;
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

    private static void deleteFirestoreRest(String uid) {
        if (uid == null || uid.isBlank()) return;
        try {
            URI uri = URI.create(
                    "https://firestore.googleapis.com/v1/projects/fir-demo-4dbd9/databases/(default)/documents/users/" + uid
                            + "?key=" + API_KEY);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .DELETE()
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignored) {}
    }
}
