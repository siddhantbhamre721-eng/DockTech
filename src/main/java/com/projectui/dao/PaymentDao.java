package com.projectui.dao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.cloud.firestore.Firestore;

/**
 * Persistent Payment DAO managing Razorpay transactions, receipt linkage, and payment history.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class PaymentDao extends Dao {

    private static final Map<String, Map<String, Object>> localPaymentStore = new ConcurrentHashMap<>();
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
                    com.google.cloud.firestore.QuerySnapshot snap = db.collection("payments").get().get();
                    if (snap != null && !snap.isEmpty()) {
                        for (com.google.cloud.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                            if (doc.exists() && doc.getData() != null) {
                                Map<String, Object> data = new HashMap<>(doc.getData());
                                String pName = (String) data.get("port");
                                if (pName == null) pName = (String) data.get("originPort");
                                if (pName == null) pName = (String) data.get("destinationPort");
                                if (pName != null && com.projectui.view.admin.AdminDataStore.isPortDeleted(pName, null)) {
                                    continue;
                                }
                                localPaymentStore.put(doc.getId(), data);
                            }
                        }
                        notifyListeners();
                    }

                    db.collection("payments").addSnapshotListener((snapshot, err) -> {
                        if (snapshot != null && !snapshot.isEmpty()) {
                            for (com.google.cloud.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                                if (doc.exists() && doc.getData() != null) {
                                    Map<String, Object> data = new HashMap<>(doc.getData());
                                    String pName = (String) data.get("port");
                                    if (pName == null) pName = (String) data.get("originPort");
                                    if (pName == null) pName = (String) data.get("destinationPort");
                                    if (pName != null && com.projectui.view.admin.AdminDataStore.isPortDeleted(pName, null)) {
                                        continue;
                                    }
                                    localPaymentStore.put(doc.getId(), data);
                                }
                            }
                            notifyListeners();
                        }
                    });
                }
            } catch (Exception ignored) {}
        });
    }

    /**
     * Records a successful Razorpay payment with duplicate protection.
     */
    public synchronized Map<String, Object> savePayment(Map<String, Object> paymentData) {
        if (paymentData == null) return null;

        String payId = (String) paymentData.get("razorpayPaymentId");
        if (payId == null || payId.isBlank()) {
            payId = (String) paymentData.get("paymentId");
        }
        if (payId == null || payId.isBlank()) {
            payId = "PAY-" + System.currentTimeMillis();
        }

        // Duplicate protection
        Map<String, Object> existing = localPaymentStore.get(payId);
        if (existing != null && "PAID".equalsIgnoreCase(String.valueOf(existing.get("paymentStatus")))) {
            System.out.println("[PaymentDao] Payment " + payId + " already recorded (duplicate callback ignored).");
            return existing;
        }

        Map<String, Object> record = new HashMap<>(paymentData);
        record.put("paymentId", payId);
        record.put("razorpayPaymentId", payId);

        String receiptNo = (String) record.get("receiptNumber");
        if (receiptNo == null || receiptNo.isBlank()) {
            receiptNo = "RCT-" + (System.currentTimeMillis() % 1000000);
            record.put("receiptNumber", receiptNo);
        }

        if (!record.containsKey("paymentStatus") || record.get("paymentStatus") == null) {
            record.put("paymentStatus", "PAID");
        }

        if (!record.containsKey("paymentDate") || record.get("paymentDate") == null) {
            record.put("paymentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        }

        if (com.projectui.view.login.LoginPage.loggedInUser != null) {
            Map<String, Object> u = com.projectui.view.login.LoginPage.loggedInUser;
            Object uid = u.get("uid");
            Object email = u.get("email");
            Object name = u.get("name") != null ? u.get("name") : u.get("fullName");

            if (uid != null && !uid.toString().isBlank() && !record.containsKey("ownerUid")) {
                record.put("ownerUid", uid.toString());
                record.put("userId", uid.toString());
                record.put("exporterUid", uid.toString());
            }
            if (email != null && !email.toString().isBlank() && !record.containsKey("userEmail")) {
                record.put("ownerEmail", email.toString());
                record.put("userEmail", email.toString());
                record.put("exporterEmail", email.toString());
            }
            if (name != null && !name.toString().isBlank() && !record.containsKey("userName")) {
                record.put("userName", name.toString());
            }
        }

        localPaymentStore.put(payId, record);
        notifyListeners();

        // Sync to Firestore
        try {
            Firestore db = db();
            if (db != null) {
                db.collection("payments").document(payId).set(record, com.google.cloud.firestore.SetOptions.merge());
            }
        } catch (Exception ignored) {}

        return record;
    }

    public Map<String, Object> getPayment(String paymentId) {
        if (paymentId == null) return null;
        return localPaymentStore.get(paymentId);
    }

    public Map<String, Object> getPaymentByCargoId(String cargoId) {
        if (cargoId == null || cargoId.isBlank()) return null;
        for (Map<String, Object> p : localPaymentStore.values()) {
            if (p != null && cargoId.equalsIgnoreCase(String.valueOf(p.get("cargoId")))) {
                return p;
            }
        }
        return null;
    }

    public List<Map<String, Object>> getAllPayments() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> p : localPaymentStore.values()) {
            if (p == null) continue;
            String pName = getString(p, "port", getString(p, "originPort", getString(p, "destinationPort", "")));
            if (!pName.isEmpty() && com.projectui.view.admin.AdminDataStore.isPortDeleted(pName, null)) {
                continue;
            }
            list.add(p);
        }
        Collections.reverse(list);
        return list;
    }

    public synchronized void deletePaymentsForPort(String portName, String portCode) {
        if (portName == null && portCode == null) return;
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : localPaymentStore.entrySet()) {
            Map<String, Object> p = entry.getValue();
            if (p != null && isPaymentAssociatedWithPort(p, portName, portCode)) {
                toRemove.add(entry.getKey());
            }
        }
        for (String id : toRemove) {
            localPaymentStore.remove(id);
            try {
                Firestore db = db();
                if (db != null) {
                    db.collection("payments").document(id).delete();
                }
            } catch (Exception ignored) {}
        }
        if (!toRemove.isEmpty()) {
            notifyListeners();
        }
    }

    public synchronized void deletePaymentsForCargo(String cargoId) {
        if (cargoId == null || cargoId.isBlank()) return;
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : localPaymentStore.entrySet()) {
            Map<String, Object> p = entry.getValue();
            if (p != null && cargoId.equalsIgnoreCase(getString(p, "cargoId", ""))) {
                toRemove.add(entry.getKey());
            }
        }
        for (String id : toRemove) {
            localPaymentStore.remove(id);
            try {
                Firestore db = db();
                if (db != null) {
                    db.collection("payments").document(id).delete();
                }
            } catch (Exception ignored) {}
        }
        if (!toRemove.isEmpty()) {
            notifyListeners();
        }
    }

    public static boolean isPaymentAssociatedWithPort(Map<String, Object> p, String portName, String portCode) {
        if (p == null) return false;
        if (portName == null && portCode == null) return false;

        String pLower = portName != null ? portName.trim().toLowerCase() : "";
        String cLower = portCode != null ? portCode.trim().toLowerCase() : "";

        String pPort = getTrimmedLower(p, "port", "originPort", "destinationPort", "preferredPort", "currentPort", "assignedPort");
        String pOrig = getTrimmedLower(p, "originPort", "origin");
        String pDest = getTrimmedLower(p, "destinationPort", "destination");

        List<String> list = List.of(pPort, pOrig, pDest);
        for (String s : list) {
            if (s.isEmpty()) continue;
            if (!pLower.isEmpty() && (s.equals(pLower) || s.contains(pLower) || pLower.contains(s))) return true;
            if (!cLower.isEmpty() && (s.equals(cLower) || s.contains(cLower))) return true;

            // City-level matching
            if (!pLower.isEmpty() && isCityMatch(s, pLower)) return true;
        }
        return false;
    }

    private static boolean isCityMatch(String a, String b) {
        if (a.isEmpty() || b.isEmpty()) return false;
        if ((a.contains("cochin") || a.contains("kochi")) && (b.contains("cochin") || b.contains("kochi"))) return true;
        if ((a.contains("mumbai") || a.contains("bombay") || a.contains("jnpt")) && (b.contains("mumbai") || b.contains("bombay") || b.contains("jnpt"))) return true;
        if ((a.contains("chennai") || a.contains("madras")) && (b.contains("chennai") || b.contains("madras"))) return true;
        if ((a.contains("vizag") || a.contains("visakhapatnam")) && (b.contains("vizag") || b.contains("visakhapatnam"))) return true;
        if ((a.contains("kolkata") || a.contains("calcutta")) && (b.contains("kolkata") || b.contains("calcutta"))) return true;
        if (a.contains("mundra") && b.contains("mundra")) return true;
        if (a.contains("kandla") && b.contains("kandla")) return true;
        if (a.contains("goa") && b.contains("goa")) return true;
        return false;
    }

    private static String getString(Map<String, Object> map, String key, String fallback) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) return fallback;
        String val = map.get(key).toString().trim();
        return val.isEmpty() ? fallback : val;
    }

    public List<Map<String, Object>> getPaymentsForImporter(String importerNameOrEmailOrUid) {
        List<Map<String, Object>> res = new ArrayList<>();
        if (importerNameOrEmailOrUid == null || importerNameOrEmailOrUid.isBlank() || "all".equalsIgnoreCase(importerNameOrEmailOrUid.trim())) {
            return res;
        }

        String filter = importerNameOrEmailOrUid.trim().toLowerCase();

        for (Map<String, Object> p : localPaymentStore.values()) {
            if (p == null) continue;
            if (isPaymentMatchingImporter(p, filter)) {
                res.add(p);
            }
        }
        Collections.reverse(res);
        return res;
    }

    public List<Map<String, Object>> getPaymentsForImporter(Map<String, Object> importerUser) {
        List<Map<String, Object>> res = new ArrayList<>();
        if (importerUser == null || importerUser.isEmpty()) {
            return res;
        }

        for (Map<String, Object> p : localPaymentStore.values()) {
            if (p == null) continue;
            if (isPaymentAssignedToImporter(p, importerUser)) {
                res.add(p);
            }
        }
        Collections.reverse(res);
        return res;
    }

    public static boolean isPaymentMatchingImporter(Map<String, Object> p, String filter) {
        if (p == null || filter == null || filter.isBlank()) return false;
        String f = filter.trim().toLowerCase();

        String impUid = getTrimmedLower(p, "importerUserId", "userId", "uid");
        if (!impUid.isEmpty() && (impUid.equals(f) || f.equals(impUid))) return true;

        String impEmail = getTrimmedLower(p, "importerEmail");
        if (!impEmail.isEmpty() && (impEmail.equals(f) || impEmail.contains(f) || f.contains(impEmail))) return true;

        String impName = getTrimmedLower(p, "importerName", "importer", "customerName");
        if (!impName.isEmpty() && (impName.equals(f) || impName.contains(f) || (f.length() >= 3 && f.contains(impName)))) return true;

        String consignee = getTrimmedLower(p, "consignee");
        if (!consignee.isEmpty() && (consignee.equals(f) || consignee.contains(f) || (f.length() >= 3 && f.contains(consignee)))) return true;

        return false;
    }

    public static boolean isPaymentAssignedToImporter(Map<String, Object> p, Map<String, Object> u) {
        if (p == null || u == null || u.isEmpty()) return false;

        String uUid = getTrimmedLower(u, "uid");
        String uEmail = getTrimmedLower(u, "email");
        String uName = getTrimmedLower(u, "name", "fullName", "displayName");
        String uComp = getTrimmedLower(u, "company", "organization");

        if (!uUid.isEmpty()) {
            String pUid = getTrimmedLower(p, "importerUserId", "userId", "uid");
            if (!pUid.isEmpty() && (pUid.equals(uUid) || pUid.equalsIgnoreCase(uUid))) return true;
        }

        if (!uEmail.isEmpty()) {
            String pEmail = getTrimmedLower(p, "importerEmail");
            if (!pEmail.isEmpty() && (pEmail.equals(uEmail) || pEmail.contains(uEmail) || uEmail.contains(pEmail))) return true;
        }

        if (!uName.isEmpty()) {
            String pName = getTrimmedLower(p, "importerName", "importer");
            if (!pName.isEmpty() && (pName.equals(uName) || pName.contains(uName) || (uName.length() >= 3 && uName.contains(pName)))) return true;
            String pCons = getTrimmedLower(p, "consignee");
            if (!pCons.isEmpty() && (pCons.equals(uName) || pCons.contains(uName) || (uName.length() >= 3 && uName.contains(pCons)))) return true;
        }

        if (!uComp.isEmpty()) {
            String pName = getTrimmedLower(p, "importerName", "importer", "importerCompany");
            if (!pName.isEmpty() && (pName.equals(uComp) || pName.contains(uComp) || (uComp.length() >= 3 && uComp.contains(pName)))) return true;
            String pCons = getTrimmedLower(p, "consignee");
            if (!pCons.isEmpty() && (pCons.equals(uComp) || pCons.contains(uComp) || (uComp.length() >= 3 && uComp.contains(pCons)))) return true;
        }

        return false;
    }

    public List<Map<String, Object>> getPaymentsForExporter(Map<String, Object> exporterUser) {
        List<Map<String, Object>> res = new ArrayList<>();
        if (exporterUser == null || exporterUser.isEmpty()) {
            return res;
        }

        for (Map<String, Object> p : localPaymentStore.values()) {
            if (p == null) continue;
            if (isPaymentAssignedToExporter(p, exporterUser)) {
                res.add(p);
            }
        }
        Collections.reverse(res);
        return res;
    }

    public static boolean isPaymentAssignedToExporter(Map<String, Object> p, Map<String, Object> u) {
        if (p == null || u == null || u.isEmpty()) return false;

        String uUid = getTrimmedLower(u, "uid", "userId", "id");
        String uEmail = getTrimmedLower(u, "email");
        String pOwnerUid = getTrimmedLower(p, "ownerUid", "userId", "uid", "exporterUid");

        if (!pOwnerUid.isEmpty()) {
            return !uUid.isEmpty() && (pOwnerUid.equals(uUid) || pOwnerUid.equalsIgnoreCase(uUid));
        }

        if (!uEmail.isEmpty()) {
            String pEmail = getTrimmedLower(p, "ownerEmail", "userEmail", "email", "exporterEmail");
            if (!pEmail.isEmpty() && (pEmail.equals(uEmail) || pEmail.equalsIgnoreCase(uEmail))) return true;
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
}
