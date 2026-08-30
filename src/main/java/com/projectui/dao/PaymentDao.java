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
                                localPaymentStore.put(doc.getId(), new HashMap<>(doc.getData()));
                            }
                        }
                        notifyListeners();
                    }

                    db.collection("payments").addSnapshotListener((snapshot, err) -> {
                        if (snapshot != null && !snapshot.isEmpty()) {
                            for (com.google.cloud.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                                if (doc.exists() && doc.getData() != null) {
                                    localPaymentStore.put(doc.getId(), new HashMap<>(doc.getData()));
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
        List<Map<String, Object>> list = new ArrayList<>(localPaymentStore.values());
        Collections.reverse(list);
        return list;
    }

    public List<Map<String, Object>> getPaymentsForImporter(String importerNameOrEmailOrUid) {
        List<Map<String, Object>> res = new ArrayList<>();
        if (importerNameOrEmailOrUid == null || importerNameOrEmailOrUid.isBlank() || "all".equalsIgnoreCase(importerNameOrEmailOrUid.trim())) {
            return getAllPayments();
        }

        String filter = importerNameOrEmailOrUid.trim().toLowerCase();

        for (Map<String, Object> p : localPaymentStore.values()) {
            if (p == null) continue;
            String impName = String.valueOf(p.getOrDefault("importerName", p.getOrDefault("importer", ""))).toLowerCase();
            String impEmail = String.valueOf(p.getOrDefault("importerEmail", "")).toLowerCase();
            String impUid = String.valueOf(p.getOrDefault("importerUserId", p.getOrDefault("userId", ""))).toLowerCase();

            if (impName.contains(filter) || impEmail.contains(filter) || impUid.contains(filter)
                    || filter.contains(impName) || filter.contains(impEmail)) {
                res.add(p);
            }
        }
        Collections.reverse(res);
        return res;
    }
}
