package com.projectui.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.google.cloud.firestore.Firestore;
import com.projectui.view.admin.AdminDataStore;
import com.projectui.view.admin.AdminDataStore.PortRequest;

public class PortRequestDao extends Dao {

    private static final Map<String, Map<String, Object>> localRequestStore = new ConcurrentHashMap<>();
    private static int requestCounter = 3040;

    static {
        // Seed default sample request for immediate UI display
        seedRequest("NPR-101", "Port of Hazira", "IN-HAZ-01", "Hazira, Gujarat", "Hazira Port Complex, Surat",
                "Industrial container & liquid cargo terminal", "Vikram Shah", "vikram@haziraport.com", "+91 98765 11223",
                "Rajesh Varma", "pa_hazira@docktech.com", "PENDING", "15 Aug 2026", "", "Hazira_Proposal.pdf, EIA_Clearance.pdf");
    }

    private static void seedRequest(String reqId, String portName, String portCode, String location, String address,
            String details, String reqName, String reqEmail, String reqPhone,
            String paName, String paEmail, String status, String date, String reason, String docs) {

        Map<String, Object> data = new HashMap<>();
        data.put("requestId", reqId);
        data.put("portName", portName);
        data.put("portCode", portCode);
        data.put("location", location);
        data.put("address", address);
        data.put("details", details);
        data.put("requesterName", reqName);
        data.put("requesterEmail", reqEmail);
        data.put("requesterPhone", reqPhone);
        data.put("proposedPaName", paName);
        data.put("proposedPaEmail", paEmail);
        data.put("status", status);
        data.put("requestDate", date);
        data.put("rejectionReason", reason);
        data.put("documentRefs", docs);
        data.put("createdAt", LocalDateTime.now().toString());

        localRequestStore.put(reqId, data);
    }

    public synchronized String generateRequestId() {
        return "NPR-" + (requestCounter++);
    }

    public CompletableFuture<Map<String, Object>> savePortRequest(
            String requestId,
            String portName,
            String portCode,
            String location,
            String address,
            String details,
            String requesterName,
            String requesterEmail,
            String requesterPhone,
            String proposedPaName,
            String proposedPaEmail,
            String documentRefs) {

        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        try {
            final String targetReqId = (requestId == null || requestId.isBlank()) ? generateRequestId() : requestId;

            String dateStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("requestId", targetReqId);
            requestData.put("portName", portName != null ? portName.trim() : "New Port");
            requestData.put("portCode", portCode != null ? portCode.trim() : "IN-PORT-01");
            requestData.put("location", location != null ? location.trim() : "Location");
            requestData.put("address", address != null ? address.trim() : "Port Address");
            requestData.put("details", details != null ? details.trim() : "New Port Proposal");
            requestData.put("requesterName", requesterName != null ? requesterName.trim() : "Requester");
            requestData.put("requesterEmail", requesterEmail != null ? requesterEmail.trim() : "requester@docktech.com");
            requestData.put("requesterPhone", requesterPhone != null ? requesterPhone.trim() : "+91 9800000000");
            requestData.put("proposedPaName", proposedPaName != null ? proposedPaName.trim() : "Port Authority");
            requestData.put("proposedPaEmail", proposedPaEmail != null ? proposedPaEmail.trim() : "pa@docktech.com");
            requestData.put("status", "PENDING");
            requestData.put("requestDate", dateStr);
            requestData.put("rejectionReason", "");
            requestData.put("documentRefs", documentRefs != null ? documentRefs : "Proposal_Document.pdf");
            requestData.put("createdAt", LocalDateTime.now().toString());

            // Save to local store
            localRequestStore.put(targetReqId, requestData);

            // Sync with AdminDataStore in-memory model
            PortRequest obj = new PortRequest(
                    targetReqId, (String)requestData.get("portName"), (String)requestData.get("portCode"),
                    (String)requestData.get("location"), (String)requestData.get("address"), (String)requestData.get("details"),
                    (String)requestData.get("requesterName"), (String)requestData.get("requesterEmail"), (String)requestData.get("requesterPhone"),
                    (String)requestData.get("proposedPaName"), (String)requestData.get("proposedPaEmail"), "PENDING", dateStr,
                    "", (String)requestData.get("documentRefs")
            );

            AdminDataStore.getInstance().portRequests.add(0, obj);
            System.out.println("[PORT REQUEST DAO] Saved request to local store: " + targetReqId);

            // Save to Firestore
            try {
                Firestore db = db();
                if (db != null) {
                    com.google.api.core.ApiFuture<com.google.cloud.firestore.WriteResult> apiFuture =
                            db.collection("port_requests").document(targetReqId).set(requestData);

                    apiFuture.addListener(() -> {
                        try {
                            apiFuture.get();
                            System.out.println("[PORT REQUEST DAO] Saved request to Firestore collection 'port_requests': " + targetReqId);
                            future.complete(requestData);
                        } catch (Exception ex) {
                            System.err.println("[PORT REQUEST DAO] Firestore write warning: " + ex.getMessage());
                            future.complete(requestData);
                        }
                    }, Runnable::run);
                } else {
                    future.complete(requestData);
                }
            } catch (Exception ex) {
                System.err.println("[PORT REQUEST DAO] Firestore exception: " + ex.getMessage());
                future.complete(requestData);
            }
        } catch (Exception ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }

    public CompletableFuture<Boolean> updateRequestStatus(String requestId, String newStatus, String rejectionReason) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Map<String, Object> data = localRequestStore.get(requestId);
        if (data != null) {
            data.put("status", newStatus);
            if (rejectionReason != null) {
                data.put("rejectionReason", rejectionReason);
            }
            data.put("updatedAt", LocalDateTime.now().toString());
        }

        // Update in AdminDataStore
        for (PortRequest req : AdminDataStore.getInstance().portRequests) {
            if (req.requestId.equalsIgnoreCase(requestId)) {
                req.status = newStatus;
                if (rejectionReason != null) {
                    req.rejectionReason = rejectionReason;
                }
                break;
            }
        }

        try {
            Firestore db = db();
            if (db != null && data != null) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", newStatus);
                if (rejectionReason != null) {
                    updates.put("rejectionReason", rejectionReason);
                }
                updates.put("updatedAt", LocalDateTime.now().toString());

                com.google.api.core.ApiFuture<com.google.cloud.firestore.WriteResult> apiFuture =
                        db.collection("port_requests").document(requestId).update(updates);

                apiFuture.addListener(() -> {
                    try {
                        apiFuture.get();
                        System.out.println("[PORT REQUEST DAO] Updated status in Firestore for document: " + requestId);
                        future.complete(true);
                    } catch (Exception ex) {
                        System.err.println("[PORT REQUEST DAO] Firestore update warning: " + ex.getMessage());
                        future.complete(true);
                    }
                }, Runnable::run);
            } else {
                future.complete(true);
            }
        } catch (Exception ex) {
            System.err.println("[PORT REQUEST DAO] Status update exception: " + ex.getMessage());
            future.complete(true);
        }

        return future;
    }
}
