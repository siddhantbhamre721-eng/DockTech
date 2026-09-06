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
public class PaymentDao extends Dao {

    private static final Map<String, Map<String, Object>> localPaymentStore = new ConcurrentHashMap<>();
    private static final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();

    private static final String PERSIST_DELETED_PAYMENTS = System.getProperty("user.home") + "/.docktech_deleted_payments.properties";
    private static final java.util.Set<String> deletedPaymentKeys = java.util.concurrent.ConcurrentHashMap.newKeySet();

    static {
        loadDeletedPayments();
        syncFromFirestore();
        CargoDao.addChangeListener(() -> {
            syncFromLocalCargoStore();
            notifyListeners();
        });
    }

    private static void loadDeletedPayments() {
        try {
            java.io.File file = new java.io.File(PERSIST_DELETED_PAYMENTS);
            if (file.exists()) {
                java.util.Properties props = new java.util.Properties();
                try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
                    props.load(in);
                }
                for (String key : props.stringPropertyNames()) {
                    if (key != null && !key.isBlank()) {
                        deletedPaymentKeys.add(key.trim().toLowerCase());
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public static void saveDeletedPayment(String key) {
        if (key == null || key.isBlank()) return;
        deletedPaymentKeys.add(key.trim().toLowerCase());
        try {
            java.util.Properties props = new java.util.Properties();
            for (String k : deletedPaymentKeys) {
                props.setProperty(k, "DELETED");
            }
            java.io.File file = new java.io.File(PERSIST_DELETED_PAYMENTS);
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                props.store(out, "DockTech Deleted Payments");
            }
        } catch (Exception ignored) {}
    }

    public static boolean isPaymentDeleted(String... keys) {
        if (keys == null || deletedPaymentKeys.isEmpty()) return false;
        for (String k : keys) {
            if (k != null && !k.isBlank() && deletedPaymentKeys.contains(k.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
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

    public static void syncFromLocalCargoStore() {
        try {
            CargoDao cargoDao = new CargoDao();
            List<Map<String, Object>> cargos = cargoDao.getAllCargos();
            if (cargos != null) {
                for (Map<String, Object> c : cargos) {
                    if (c == null) continue;
                    if (isPaidCargo(c)) {
                        Map<String, Object> paymentRec = buildPaymentRecordFromCargo(c);
                        if (paymentRec != null) {
                            String payId = (String) paymentRec.get("paymentId");
                            String cargoId = (String) paymentRec.get("cargoId");
                            if (isPaymentDeleted(payId, cargoId)) {
                                continue;
                            }
                            if (payId != null && !payId.isBlank()) {
                                if (cargoId != null && !cargoId.isBlank()) {
                                    localPaymentStore.entrySet().removeIf(entry -> {
                                        Map<String, Object> val = entry.getValue();
                                        return val != null && cargoId.equalsIgnoreCase((String) val.get("cargoId")) && !payId.equals(entry.getKey());
                                    });
                                }
                                Map<String, Object> existing = localPaymentStore.get(payId);
                                if (existing == null) {
                                    localPaymentStore.put(payId, paymentRec);
                                } else {
                                    for (Map.Entry<String, Object> e : paymentRec.entrySet()) {
                                        existing.put(e.getKey(), e.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public static void syncFromFirestore() {
        java.util.concurrent.ForkJoinPool.commonPool().execute(() -> {
            try {
                Firestore db = com.projectui.config.FirebaseConfig.getFirestore();
                if (db != null) {
                    // 1. Sync from 'payments' collection
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
                                String payId = (String) data.get("paymentId");
                                String rPayId = (String) data.get("razorpayPaymentId");
                                String cId = (String) data.get("cargoId");
                                if (isPaymentDeleted(doc.getId(), payId, rPayId, cId)) {
                                    continue;
                                }
                                localPaymentStore.put(doc.getId(), data);
                            }
                        }
                        notifyListeners();
                    }

                    // 2. Sync from 'cargos' collection for existing paid cargos
                    com.google.cloud.firestore.QuerySnapshot cargoSnap = db.collection("cargos").get().get();
                    if (cargoSnap != null && !cargoSnap.isEmpty()) {
                        for (com.google.cloud.firestore.DocumentSnapshot doc : cargoSnap.getDocuments()) {
                            if (doc.exists() && doc.getData() != null) {
                                Map<String, Object> cData = new HashMap<>(doc.getData());
                                if (isPaidCargo(cData)) {
                                    Map<String, Object> paymentRec = buildPaymentRecordFromCargo(cData);
                                    if (paymentRec != null) {
                                        String payId = (String) paymentRec.get("paymentId");
                                        String cId = (String) cData.get("cargoId");
                                        if (isPaymentDeleted(payId, cId)) {
                                            continue;
                                        }
                                        if (payId != null && !payId.isBlank()) {
                                            if (!localPaymentStore.containsKey(payId)) {
                                                localPaymentStore.put(payId, paymentRec);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        notifyListeners();
                    }

                    // Listeners for real-time changes
                    db.collection("payments").addSnapshotListener((snapshot, err) -> {
                        if (snapshot != null) {
                            java.util.Set<String> snapshotIds = new java.util.HashSet<>();
                            for (com.google.cloud.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                                snapshotIds.add(doc.getId());
                                if (doc.exists() && doc.getData() != null) {
                                    Map<String, Object> data = new HashMap<>(doc.getData());
                                    String pName = (String) data.get("port");
                                    if (pName == null) pName = (String) data.get("originPort");
                                    if (pName == null) pName = (String) data.get("destinationPort");
                                    if (pName != null && com.projectui.view.admin.AdminDataStore.isPortDeleted(pName, null)) {
                                        continue;
                                    }
                                    String payId = (String) data.get("paymentId");
                                    String rPayId = (String) data.get("razorpayPaymentId");
                                    String cId = (String) data.get("cargoId");
                                    if (isPaymentDeleted(doc.getId(), payId, rPayId, cId)) {
                                        continue;
                                    }
                                    localPaymentStore.put(doc.getId(), data);
                                }
                            }
                            localPaymentStore.keySet().removeIf(id -> !snapshotIds.contains(id));
                            notifyListeners();
                        }
                    });

                    db.collection("cargos").addSnapshotListener((snapshot, err) -> {
                        if (snapshot != null && !snapshot.isEmpty()) {
                            for (com.google.cloud.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                                if (doc.exists() && doc.getData() != null) {
                                    Map<String, Object> cData = new HashMap<>(doc.getData());
                                    if (isPaidCargo(cData)) {
                                        Map<String, Object> paymentRec = buildPaymentRecordFromCargo(cData);
                                        if (paymentRec != null) {
                                            String payId = (String) paymentRec.get("paymentId");
                                            String cId = (String) cData.get("cargoId");
                                            if (isPaymentDeleted(payId, cId)) {
                                                continue;
                                            }
                                            if (payId != null && !payId.isBlank()) {
                                                localPaymentStore.putIfAbsent(payId, paymentRec);
                                            }
                                        }
                                    }
                                }
                            }
                            notifyListeners();
                        }
                    });
                }
            } catch (Exception ignored) {}
            syncFromLocalCargoStore();
            notifyListeners();
        });
    }

    public static boolean isPaidCargo(Map<String, Object> c) {
        if (c == null) return false;
        String payStatus = getString(c, "paymentStatus", "");
        String paymentId = getString(c, "razorpayPaymentId", getString(c, "paymentId", ""));
        return "PAID".equalsIgnoreCase(payStatus.trim())
            && !paymentId.isBlank();
    }

    public static Map<String, Object> buildPaymentRecordFromCargo(Map<String, Object> c) {
        if (c == null) return null;
        String cargoId = getString(c, "cargoId", "");
        String payId = getString(c, "razorpayPaymentId", getString(c, "paymentId", ""));
        if (payId.isEmpty() || !isPaidCargo(c)) return null;

        String receiptNo = getString(c, "receiptNumber", getString(c, "receiptNo", "RCT-" + (cargoId.isEmpty() ? (System.currentTimeMillis() % 1000000) : (Math.abs(cargoId.hashCode()) % 899999 + 100000))));
        String invId = getString(c, "invoiceId", "INV-" + (cargoId.isEmpty() ? (System.currentTimeMillis() % 100000) : (Math.abs(cargoId.hashCode()) % 89999 + 10000)));
        String txnId = getString(c, "transactionId", getString(c, "razorpayOrderId", payId));

        String exporterName = getString(c, "exporterName", getString(c, "exporter", getString(c, "userName", getString(c, "shipper", getString(c, "customerName", "Exporter")))));
        String originPort = getString(c, "originPort", getString(c, "origin", getString(c, "port", "Port of Mumbai")));
        String destPort = getString(c, "destinationPort", getString(c, "destination", "Port of Singapore"));
        String cargoName = getString(c, "cargoName", "Commercial Freight Cargo");
        String cargoType = getString(c, "cargoType", "Export Freight");
        String weight = getString(c, "weight", "—");

        double amount = 0.0;
        if (c.get("paymentAmount") != null) {
            try { amount = Double.parseDouble(c.get("paymentAmount").toString().replace("₹", "").replace(",", "").trim()); } catch (Exception ignored) {}
        }
        if (amount <= 0 && c.get("amount") != null) {
            try { amount = Double.parseDouble(c.get("amount").toString().replace("₹", "").replace(",", "").trim()); } catch (Exception ignored) {}
        }
        if (amount <= 0) {
            Map<String, Object> booking = !cargoId.isEmpty() ? new ContainerDao().getBookingByCargoId(cargoId) : null;
            com.projectui.service.FreightCalculationService.PaymentBreakdown breakdown = com.projectui.service.FreightCalculationService.getInstance().calculatePaymentBreakdown(c, booking);
            if (breakdown != null && breakdown.totalPayable > 0) {
                amount = breakdown.totalPayable;
            } else {
                try {
                    String valStr = getString(c, "value", "0");
                    double val = Double.parseDouble(valStr.replace("₹", "").replace(",", "").trim());
                    amount = val > 0 ? (val * 0.12) : 0.0;
                } catch (Exception ignored) {
                    amount = 0.0;
                }
            }
        }

        String containerNo = "—";
        String containerType = "Standard Container";
        ContainerDao containerDao = new ContainerDao();
        Map<String, Object> booking = !cargoId.isEmpty() ? containerDao.getBookingByCargoId(cargoId) : null;
        if (booking == null && c.get("bookingId") != null) {
            booking = containerDao.getBooking(c.get("bookingId").toString());
        }
        if (booking != null) {
            String bCont = getString(booking, "containerNo", getString(booking, "containerNumber", getString(booking, "assignedContainer", "")));
            if (!bCont.isEmpty() && !"—".equals(bCont) && !"-".equals(bCont)) {
                containerNo = bCont;
            } else {
                containerNo = getString(booking, "bookingId", "—");
            }
            containerType = getString(booking, "containerType", "Standard Container");
        } else if (c.get("containerNo") != null && !c.get("containerNo").toString().isBlank() && !"—".equals(c.get("containerNo").toString().trim())) {
            containerNo = c.get("containerNo").toString().trim();
        } else if (c.get("bookingId") != null && !c.get("bookingId").toString().isBlank()) {
            containerNo = c.get("bookingId").toString().trim();
        }

        String dateStr = getString(c, "paidAt", getString(c, "paymentDate", getString(c, "updatedAt", getString(c, "createdAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))))));

        Map<String, Object> paymentRecord = new HashMap<>();
        paymentRecord.put("paymentId", payId);
        paymentRecord.put("razorpayPaymentId", payId);
        paymentRecord.put("receiptNumber", receiptNo);
        paymentRecord.put("invoiceId", invId);
        paymentRecord.put("transactionId", txnId);
        paymentRecord.put("cargoId", cargoId);
        paymentRecord.put("cargoName", cargoName);
        paymentRecord.put("cargoType", cargoType);
        paymentRecord.put("weight", weight);
        paymentRecord.put("originPort", originPort);
        paymentRecord.put("destinationPort", destPort);
        paymentRecord.put("importerName", getString(c, "importerName", getString(c, "importer", "Global Traders Ltd.")));
        paymentRecord.put("exporterName", exporterName);
        paymentRecord.put("customerName", exporterName);
        paymentRecord.put("customerType", "Exporter");
        paymentRecord.put("containerNo", containerNo);
        paymentRecord.put("containerType", containerType);
        paymentRecord.put("port", originPort);
        paymentRecord.put("amount", amount);
        paymentRecord.put("currency", "INR");
        paymentRecord.put("paymentStatus", "PAID");
        String paymentPurpose = getString(c, "paymentPurpose", "");
        if (!paymentPurpose.isBlank()) paymentRecord.put("paymentPurpose", paymentPurpose);
        paymentRecord.put("paymentMethod", getString(c, "paymentMethod", "Razorpay Standard Checkout (Cards / UPI / NetBanking)"));
        paymentRecord.put("freightCharge", amount * 0.55);
        paymentRecord.put("portHandling", amount * 0.15);
        paymentRecord.put("customsDuty", amount * 0.30);
        paymentRecord.put("paymentDate", dateStr);
        paymentRecord.put("updatedAt", dateStr);

        if (c.containsKey("ownerUid")) paymentRecord.put("ownerUid", c.get("ownerUid"));
        if (c.containsKey("exporterUid")) paymentRecord.put("exporterUid", c.get("exporterUid"));
        if (c.containsKey("exporterEmail")) paymentRecord.put("exporterEmail", c.get("exporterEmail"));
        if (c.containsKey("ownerEmail")) paymentRecord.put("ownerEmail", c.get("ownerEmail"));

        return paymentRecord;
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
            return null;
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
        syncFromLocalCargoStore();
        return localPaymentStore.get(paymentId);
    }

    public Map<String, Object> getPaymentByCargoId(String cargoId) {
        if (cargoId == null || cargoId.isBlank()) return null;
        syncFromLocalCargoStore();
        for (Map<String, Object> p : localPaymentStore.values()) {
            if (p != null && cargoId.equalsIgnoreCase(String.valueOf(p.get("cargoId")))) {
                return p;
            }
        }
        CargoDao cargoDao = new CargoDao();
        Map<String, Object> cargo = cargoDao.getCargo(cargoId);
        if (cargo != null && isPaidCargo(cargo)) {
            Map<String, Object> paymentRec = buildPaymentRecordFromCargo(cargo);
            if (paymentRec != null) {
                String payId = (String) paymentRec.get("paymentId");
                localPaymentStore.put(payId, paymentRec);
                return paymentRec;
            }
        }
        return null;
    }

    public List<Map<String, Object>> getAllPayments() {
        syncFromLocalCargoStore();
        List<Map<String, Object>> list = new ArrayList<>();
        ContainerDao containerDao = new ContainerDao();
        CargoDao cargoDao = new CargoDao();
        for (Map<String, Object> p : localPaymentStore.values()) {
            if (p == null) continue;
            String payId = getString(p, "paymentId", getString(p, "razorpayPaymentId", ""));
            String cId = getString(p, "cargoId", "");
            if (isPaymentDeleted(payId, cId)) {
                continue;
            }
            String pName = getString(p, "port", getString(p, "originPort", getString(p, "destinationPort", "")));
            if (!pName.isEmpty() && com.projectui.view.admin.AdminDataStore.isPortDeleted(pName, null)) {
                continue;
            }

            // Dynamically resolve real container if containerNo is empty or dash
            String cont = getString(p, "containerNo", "");
            if (cont.isEmpty() || "—".equals(cont) || "-".equals(cont) || "null".equalsIgnoreCase(cont)) {
                String bId = getString(p, "bookingId", "");
                Map<String, Object> booking = !cId.isEmpty() ? containerDao.getBookingByCargoId(cId) : null;
                if (booking == null && !bId.isEmpty()) {
                    booking = containerDao.getBooking(bId);
                }
                if (booking != null) {
                    String resolved = getString(booking, "containerNo", getString(booking, "containerNumber", getString(booking, "assignedContainer", "")));
                    if (resolved.isEmpty() || "—".equals(resolved) || "-".equals(resolved)) {
                        resolved = getString(booking, "bookingId", "");
                    }
                    if (!resolved.isEmpty() && !"—".equals(resolved) && !"-".equals(resolved)) {
                        p.put("containerNo", resolved);
                    }
                } else if (!cId.isEmpty()) {
                    Map<String, Object> cg = cargoDao.getCargo(cId);
                    if (cg != null) {
                        String cgCont = getString(cg, "containerNo", getString(cg, "bookingId", getString(cg, "containerId", "")));
                        if (!cgCont.isEmpty() && !"—".equals(cgCont) && !"-".equals(cgCont)) {
                            p.put("containerNo", cgCont);
                        }
                    }
                }
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

    public synchronized boolean deletePaymentById(String paymentId) {
        return deletePayment(paymentId, null);
    }

    public synchronized boolean deletePayment(String paymentId, Map<String, Object> paymentData) {
        if (paymentId == null || paymentId.isBlank()) return false;

        String cleanId = paymentId.trim();
        saveDeletedPayment(cleanId);

        // Remove from localPaymentStore
        Map<String, Object> payment = localPaymentStore.remove(cleanId);
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : localPaymentStore.entrySet()) {
            Map<String, Object> v = entry.getValue();
            if (v == null) continue;
            String pId = getString(v, "paymentId", "");
            String rId = getString(v, "razorpayPaymentId", "");
            String tId = getString(v, "transactionId", "");
            if (cleanId.equalsIgnoreCase(entry.getKey()) ||
                cleanId.equalsIgnoreCase(pId) ||
                cleanId.equalsIgnoreCase(rId) ||
                cleanId.equalsIgnoreCase(tId)) {
                toRemove.add(entry.getKey());
                if (payment == null) payment = v;
            }
        }
        for (String k : toRemove) {
            localPaymentStore.remove(k);
        }

        if (paymentData != null) {
            String pId = getString(paymentData, "paymentId", "");
            String rId = getString(paymentData, "razorpayPaymentId", "");
            String tId = getString(paymentData, "transactionId", "");
            if (!pId.isBlank()) saveDeletedPayment(pId);
            if (!rId.isBlank()) saveDeletedPayment(rId);
            if (!tId.isBlank()) saveDeletedPayment(tId);
            if (payment == null) payment = paymentData;
        }

        String cargoId = "";
        if (payment != null) {
            cargoId = getString(payment, "cargoId", "");
            String pId = getString(payment, "paymentId", "");
            String rId = getString(payment, "razorpayPaymentId", "");
            if (!pId.isBlank()) saveDeletedPayment(pId);
            if (!rId.isBlank()) saveDeletedPayment(rId);
        }

        // Clear associated cargo in CargoDao and Firestore if any
        CargoDao cargoDao = new CargoDao();
        if (!cargoId.isBlank()) {
            cargoDao.clearCargoPayment(cargoId);
        }
        cargoDao.clearCargoPaymentByPaymentId(cleanId);

        // Delete from Firestore payments collection
        try {
            Firestore firestore = db();
            if (firestore != null) {
                try {
                    firestore.collection("payments").document(cleanId).delete();
                } catch (Exception ignored) {}

                try {
                    var snap = firestore.collection("payments").whereEqualTo("paymentId", cleanId).get().get();
                    if (snap != null) {
                        for (var doc : snap.getDocuments()) {
                            doc.getReference().delete();
                        }
                    }
                } catch (Exception ignored) {}
                try {
                    var snap2 = firestore.collection("payments").whereEqualTo("razorpayPaymentId", cleanId).get().get();
                    if (snap2 != null) {
                        for (var doc : snap2.getDocuments()) {
                            doc.getReference().delete();
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        if (payment != null) {
            try {
                com.projectui.service.ReceiptPdfService.deleteReceipt(payment);
            } catch (Exception ignored) {}
        }

        notifyListeners();
        return true;
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
