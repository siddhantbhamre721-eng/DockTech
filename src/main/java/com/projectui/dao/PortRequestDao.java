package com.projectui.dao;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.google.cloud.firestore.Firestore;
import com.projectui.view.admin.AdminDataStore;
import com.projectui.view.admin.AdminDataStore.PortRequest;

@SuppressWarnings({ "null", "unchecked" })
public class PortRequestDao extends Dao {

    private static final Map<String, Map<String, Object>> localRequestStore = new ConcurrentHashMap<>();
    private static final Map<String, String> diskStatusCache = new ConcurrentHashMap<>();
    private static int requestCounter = 3040;
    private static final String PERSIST_FILE = System.getProperty("user.home") + "/.docktech_port_requests.properties";
    private static String lastSyncSnapshotHash = "";
    private static long lastSyncTimeMs = 0;

    static {
        // Seed default sample request for immediate UI display
        seedRequest("NPR-101", "Port of Hazira", "IN-HAZ-01", "Hazira, Gujarat", "Hazira Port Complex, Surat",
                "Industrial container & liquid cargo terminal", "Vikram Shah", "vikram@haziraport.com",
                "+91 98765 11223",
                "Rajesh Varma", "pa_hazira@docktech.com", "PENDING", "15 Aug 2026", "",
                "Hazira_Proposal.pdf, EIA_Clearance.pdf");

        // Restore persistent statuses from disk on startup
        restoreLocalDiskStatuses();
    }

    public static void restoreLocalDiskStatuses() {
        try {
            java.io.File file = new java.io.File(PERSIST_FILE);
            if (file.exists()) {
                java.util.Properties props = new java.util.Properties();
                try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
                    props.load(in);
                }
                for (String reqId : props.stringPropertyNames()) {
                    String status = props.getProperty(reqId);
                    if (status != null && !status.isBlank()) {
                        String norm = status.toUpperCase().trim();
                        diskStatusCache.put(reqId, norm);
                        applyStatusToStores(reqId, norm, null);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void saveLocalDiskStatus(String reqId, String status) {
        if (reqId == null || status == null) return;
        String normalizedStatus = status.toUpperCase().trim();
        String current = diskStatusCache.get(reqId);
        if (normalizedStatus.equals(current)) {
            return; // Already current on disk, avoid redundant disk write
        }
        try {
            java.util.Properties props = new java.util.Properties();
            java.io.File file = new java.io.File(PERSIST_FILE);
            if (file.exists()) {
                try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
                    props.load(in);
                } catch (Exception ignored) {
                }
            }
            props.setProperty(reqId, normalizedStatus);
            diskStatusCache.put(reqId, normalizedStatus);
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                props.store(out, "DockTech Port Request Statuses");
            }
        } catch (Exception ignored) {
        }
    }

    public static void applyStatusToStores(String requestId, String statusVal, String rejectionReason) {
        if (requestId == null || requestId.isBlank()) return;

        Map<String, Object> data = localRequestStore.get(requestId);
        if (data != null) {
            data.put("status", statusVal);
            if (rejectionReason != null) {
                data.put("rejectionReason", rejectionReason);
            }
            data.put("updatedAt", LocalDateTime.now().toString());
        }

        for (PortRequest req : AdminDataStore.getInstance().portRequests) {
            if (req.requestId != null && req.requestId.equalsIgnoreCase(requestId.trim())) {
                req.status = statusVal;
                if (rejectionReason != null) {
                    req.rejectionReason = rejectionReason;
                }
            }
        }
    }

    public CompletableFuture<java.util.List<PortRequest>> syncWithDatabase() {
        CompletableFuture<java.util.List<PortRequest>> future = new CompletableFuture<>();
        restoreLocalDiskStatuses();
        try {
            Firestore db = db();
            if (db != null) {
                com.google.api.core.ApiFuture<com.google.cloud.firestore.QuerySnapshot> snapFuture = db.collection("port_requests").get();
                snapFuture.addListener(() -> {
                    try {
                        com.google.cloud.firestore.QuerySnapshot snap = snapFuture.get();
                        if (snap != null && !snap.isEmpty()) {
                            StringBuilder currentHash = new StringBuilder();
                            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : snap.getDocuments()) {
                                currentHash.append(doc.getId()).append(":")
                                        .append(doc.getString("status")).append(":")
                                        .append(doc.getUpdateTime() != null ? doc.getUpdateTime().toString() : "").append(";");
                            }

                            String hashStr = currentHash.toString();
                            if (hashStr.equals(lastSyncSnapshotHash) && (System.currentTimeMillis() - lastSyncTimeMs < 15000)) {
                                // Data is already up to date, reuse existing in-memory state without redundant processing
                                future.complete(AdminDataStore.getInstance().portRequests);
                                return;
                            }

                            lastSyncSnapshotHash = hashStr;
                            lastSyncTimeMs = System.currentTimeMillis();

                            java.util.List<PortRequest> fetchedRequests = new java.util.ArrayList<>();
                            for (com.google.cloud.firestore.QueryDocumentSnapshot doc : snap.getDocuments()) {
                                Map<String, Object> data = doc.getData();
                                String reqId = doc.getId();
                                if (data == null) {
                                    data = new HashMap<>();
                                }
                                data.put("requestId", reqId);

                                String st = doc.getString("status");
                                String statusVal = (st != null && !st.isBlank()) ? st.toUpperCase().trim() : "PENDING";
                                data.put("status", statusVal);

                                String rejectionReason = doc.getString("rejectionReason");
                                if (rejectionReason != null) {
                                    data.put("rejectionReason", rejectionReason);
                                }

                                saveLocalDiskStatus(reqId, statusVal);
                                localRequestStore.put(reqId, data);

                                PortRequest reqObj = parsePortRequestFromMap(data);
                                fetchedRequests.add(reqObj);
                            }

                            mergeIntoAdminDataStore(fetchedRequests);
                            future.complete(AdminDataStore.getInstance().portRequests);
                        } else {
                            future.complete(AdminDataStore.getInstance().portRequests);
                        }
                    } catch (Exception ex) {
                        future.complete(AdminDataStore.getInstance().portRequests);
                    }
                }, Runnable::run);
            } else {
                future.complete(AdminDataStore.getInstance().portRequests);
            }
        } catch (Exception ex) {
            future.complete(AdminDataStore.getInstance().portRequests);
        }
        return future;
    }

    public static PortRequest parsePortRequestFromMap(Map<String, Object> data) {
        String reqId = (String) data.get("requestId");
        String portName = (String) data.get("portName");
        String portCode = (String) data.get("portCode");
        String location = (String) data.get("location");
        String address = (String) data.get("address");
        String details = (String) data.get("details");
        String reqName = (String) data.get("requesterName");
        String reqEmail = (String) data.get("requesterEmail");
        String reqPhone = (String) data.get("requesterPhone");
        String paName = (String) (data.get("proposedPaName") != null ? data.get("proposedPaName") : data.get("proposedPortAuthorityName"));
        String paEmail = (String) (data.get("proposedPaEmail") != null ? data.get("proposedPaEmail") : data.get("proposedPortAuthorityEmail"));
        String status = (String) data.get("status");
        String requestDate = (String) data.get("requestDate");
        String rejectionReason = (String) data.get("rejectionReason");
        String documentRefs = "";
        Object dRefsObj = data.get("documentRefs");
        if (dRefsObj instanceof String s) {
            documentRefs = s;
        } else if (data.get("documentRefsSummary") instanceof String sSummary) {
            documentRefs = sSummary;
        } else if (dRefsObj != null) {
            documentRefs = dRefsObj.toString();
        }

        double lat = 0.0;
        double lng = 0.0;
        Object latObj = data.get("lat") != null ? data.get("lat") : data.get("latitude");
        Object lngObj = data.get("lng") != null ? data.get("lng") : data.get("longitude");
        if (latObj instanceof Number n) lat = n.doubleValue();
        else if (latObj instanceof String s && !s.isBlank()) try { lat = Double.parseDouble(s); } catch (Exception ignored) {}
        if (lngObj instanceof Number n) lng = n.doubleValue();
        else if (lngObj instanceof String s && !s.isBlank()) try { lng = Double.parseDouble(s); } catch (Exception ignored) {}

        PortRequest req = new PortRequest(
                reqId != null ? reqId : "NPR-101",
                portName != null ? portName : "New Port",
                portCode != null ? portCode : "IN-PORT-01",
                location != null ? location : "Location",
                address != null ? address : "Port Address",
                details != null ? details : "New Port Proposal",
                reqName != null ? reqName : "Requester",
                reqEmail != null ? reqEmail : "requester@docktech.com",
                reqPhone != null ? reqPhone : "+91 9800000000",
                paName != null ? paName : "Port Authority",
                paEmail != null ? paEmail : "pa@docktech.com",
                status != null && !status.isBlank() ? status.toUpperCase().trim() : "PENDING",
                requestDate != null ? requestDate : "Today",
                rejectionReason != null ? rejectionReason : "",
                documentRefs != null ? documentRefs : "Proposal_Document.pdf",
                lat, lng
        );

        Object docRefsObj = data.get("documentRefs");
        if (docRefsObj instanceof Map<?, ?> docRefsMap) {
            Object aObj = docRefsMap.get("aadhaar");
            if (aObj instanceof Map<?, ?> aMap) {
                if (aMap.get("fileName") != null) req.aadhaarFileName = aMap.get("fileName").toString();
                if (aMap.get("url") != null) req.aadhaarUrl = aMap.get("url").toString();
                if (aMap.get("publicId") != null) req.aadhaarPublicId = aMap.get("publicId").toString();
                if (aMap.get("resourceType") != null) req.aadhaarResourceType = aMap.get("resourceType").toString();
                if (aMap.get("format") != null) req.aadhaarFormat = aMap.get("format").toString();
                if (aMap.get("uploadedAt") != null) req.aadhaarUploadedAt = aMap.get("uploadedAt").toString();
            }
            Object pObj = docRefsMap.get("proposal");
            if (pObj == null) pObj = docRefsMap.get("supporting");
            if (pObj instanceof Map<?, ?> pMap) {
                if (pMap.get("fileName") != null) req.supportingFileName = pMap.get("fileName").toString();
                if (pMap.get("url") != null) req.supportingUrl = pMap.get("url").toString();
                if (pMap.get("publicId") != null) req.supportingPublicId = pMap.get("publicId").toString();
                if (pMap.get("resourceType") != null) req.supportingResourceType = pMap.get("resourceType").toString();
                if (pMap.get("format") != null) req.supportingFormat = pMap.get("format").toString();
                if (pMap.get("uploadedAt") != null) req.supportingUploadedAt = pMap.get("uploadedAt").toString();
            }
        }

        Object aFile = data.get("aadhaarFileName");
        if (aFile == null) aFile = data.get("aadhaar_file_name");
        if (aFile != null) req.aadhaarFileName = (String) aFile;

        Object aUrl = data.get("aadhaarUrl");
        if (aUrl == null) aUrl = data.get("aadhaarSecureUrl");
        if (aUrl == null) aUrl = data.get("aadhaarCloudinaryUrl");
        if (aUrl == null) aUrl = data.get("aadhaarDocumentUrl");
        if (aUrl == null) aUrl = data.get("aadhaar_url");
        if (aUrl != null) req.aadhaarUrl = (String) aUrl;

        Object aPid = data.get("aadhaarPublicId");
        if (aPid == null) aPid = data.get("aadhaar_public_id");
        if (aPid != null) req.aadhaarPublicId = (String) aPid;

        Object aRes = data.get("aadhaarResourceType");
        if (aRes == null) aRes = data.get("aadhaar_resource_type");
        if (aRes != null) req.aadhaarResourceType = (String) aRes;

        Object aFmt = data.get("aadhaarFormat");
        if (aFmt == null) aFmt = data.get("aadhaar_format");
        if (aFmt != null) req.aadhaarFormat = (String) aFmt;

        Object aTime = data.get("aadhaarUploadedAt");
        if (aTime == null) aTime = data.get("aadhaar_uploaded_at");
        if (aTime != null) req.aadhaarUploadedAt = (String) aTime;

        Object supFile = data.get("supportingFileName") != null ? data.get("supportingFileName") : data.get("portEstablishmentProposalFileName");
        if (supFile == null) supFile = data.get("proposalFileName");
        if (supFile != null) req.supportingFileName = (String) supFile;

        Object supUrl = data.get("supportingUrl") != null ? data.get("supportingUrl") : data.get("portEstablishmentProposalUrl");
        if (supUrl == null) supUrl = data.get("proposalUrl");
        if (supUrl == null) supUrl = data.get("supportingSecureUrl");
        if (supUrl == null) supUrl = data.get("supportingCloudinaryUrl");
        if (supUrl != null) req.supportingUrl = (String) supUrl;

        Object supPid = data.get("supportingPublicId") != null ? data.get("supportingPublicId") : data.get("portEstablishmentProposalPublicId");
        if (supPid != null) req.supportingPublicId = (String) supPid;

        Object supRes = data.get("supportingResourceType") != null ? data.get("supportingResourceType") : data.get("portEstablishmentProposalResourceType");
        if (supRes != null) req.supportingResourceType = (String) supRes;

        Object supFmt = data.get("supportingFormat") != null ? data.get("supportingFormat") : data.get("portEstablishmentProposalFormat");
        if (supFmt != null) req.supportingFormat = (String) supFmt;

        Object supTime = data.get("supportingUploadedAt") != null ? data.get("supportingUploadedAt") : data.get("portEstablishmentProposalUploadedAt");
        if (supTime != null) req.supportingUploadedAt = (String) supTime;

        return req;
    }

    public static synchronized void mergeIntoAdminDataStore(java.util.List<PortRequest> fetchedRequests) {
        java.util.List<PortRequest> currentList = AdminDataStore.getInstance().portRequests;

        for (PortRequest fetched : fetchedRequests) {
            PortRequest existing = null;
            for (PortRequest req : currentList) {
                if (req.requestId != null && fetched.requestId != null && req.requestId.equalsIgnoreCase(fetched.requestId.trim())) {
                    existing = req;
                    break;
                }
            }

            if (existing != null) {
                existing.status = fetched.status;
                if (fetched.rejectionReason != null && !fetched.rejectionReason.isBlank()) {
                    existing.rejectionReason = fetched.rejectionReason;
                }
                if (fetched.aadhaarUrl != null && !fetched.aadhaarUrl.isBlank()) {
                    existing.aadhaarUrl = fetched.aadhaarUrl;
                    existing.aadhaarFileName = fetched.aadhaarFileName;
                    existing.aadhaarPublicId = fetched.aadhaarPublicId;
                    existing.aadhaarResourceType = fetched.aadhaarResourceType;
                    existing.aadhaarFormat = fetched.aadhaarFormat;
                    existing.aadhaarUploadedAt = fetched.aadhaarUploadedAt;
                }
                if (fetched.supportingUrl != null && !fetched.supportingUrl.isBlank()) {
                    existing.supportingUrl = fetched.supportingUrl;
                    existing.supportingFileName = fetched.supportingFileName;
                    existing.supportingPublicId = fetched.supportingPublicId;
                    existing.supportingResourceType = fetched.supportingResourceType;
                    existing.supportingFormat = fetched.supportingFormat;
                    existing.supportingUploadedAt = fetched.supportingUploadedAt;
                }
            } else {
                currentList.add(0, fetched);
            }
        }
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
        return savePortRequestExtended(requestId, portName, portCode, location, address, details, requesterName, requesterEmail, requesterPhone, proposedPaName, proposedPaEmail, documentRefs, "Aadhaar_Card.pdf", "", "Supporting_Clearance.pdf", "");
    }

    public CompletableFuture<Map<String, Object>> savePortRequestExtended(
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
            String documentRefs,
            String aadhaarFileName,
            String aadhaarUrl,
            String supportingFileName,
            String supportingUrl) {

        return savePortRequestFull(
                requestId, portName, portCode, location, address, details, requesterName, requesterEmail, requesterPhone, proposedPaName, proposedPaEmail, documentRefs,
                aadhaarFileName, aadhaarUrl, "doc_aadhaar_" + System.currentTimeMillis(), "pdf", "pdf", "",
                supportingFileName, supportingUrl, "doc_proposal_" + System.currentTimeMillis(), "pdf", "pdf", ""
        );
    }

    public CompletableFuture<Map<String, Object>> savePortRequestFull(
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
            String documentRefs,
            String aadhaarFileName,
            String aadhaarUrl,
            String aadhaarPublicId,
            String aadhaarResourceType,
            String aadhaarFormat,
            String aadhaarUploadedAt,
            String supportingFileName,
            String supportingUrl,
            String supportingPublicId,
            String supportingResourceType,
            String supportingFormat,
            String supportingUploadedAt) {

        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        try {
            final String targetReqId = (requestId == null || requestId.isBlank()) ? generateRequestId() : requestId;

            String dateStr = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));

            System.out.println("========== PORT REQUEST SAVE ==========");
            System.out.println("REQUEST ID: " + targetReqId);
            System.out.println("PORT NAME: " + portName);
            System.out.println("STATUS: PENDING");
            System.out.println("REQUESTER EMAIL: " + requesterEmail);
            System.out.println("AADHAAR URL: " + aadhaarUrl);
            System.out.println("PROPOSAL URL: " + supportingUrl);

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
            requestData.put("createdAt", LocalDateTime.now().toString());

            String aUrlStr = aadhaarUrl != null ? aadhaarUrl.trim() : "";
            String pUrlStr = supportingUrl != null ? supportingUrl.trim() : "";

            requestData.put("documentRefs", documentRefs != null ? documentRefs : ("Aadhaar: " + (aadhaarFileName != null ? aadhaarFileName : "Aadhaar_Card.pdf") + ", Proposal: " + (supportingFileName != null ? supportingFileName : "Port_Establishment_Proposal.pdf")));
            requestData.put("aadhaarDocumentUrl", aUrlStr);
            requestData.put("proposalDocumentUrl", pUrlStr);

            requestData.put("aadhaarFileName", aadhaarFileName != null ? aadhaarFileName : "Aadhaar_Card.pdf");
            requestData.put("aadhaarUrl", aUrlStr);
            requestData.put("aadhaarPublicId", aadhaarPublicId != null ? aadhaarPublicId : "");
            requestData.put("aadhaarResourceType", aadhaarResourceType != null ? aadhaarResourceType : "pdf");
            requestData.put("aadhaarFormat", aadhaarFormat != null ? aadhaarFormat : "pdf");
            requestData.put("aadhaarUploadedAt", aadhaarUploadedAt != null ? aadhaarUploadedAt : dateStr);

            requestData.put("supportingFileName", supportingFileName != null ? supportingFileName : "Port_Establishment_Proposal.pdf");
            requestData.put("supportingUrl", pUrlStr);
            requestData.put("supportingPublicId", supportingPublicId != null ? supportingPublicId : "");
            requestData.put("supportingResourceType", supportingResourceType != null ? supportingResourceType : "pdf");
            requestData.put("supportingFormat", supportingFormat != null ? supportingFormat : "pdf");
            requestData.put("supportingUploadedAt", supportingUploadedAt != null ? supportingUploadedAt : dateStr);

            requestData.put("proposedPortAuthorityName", proposedPaName != null ? proposedPaName.trim() : "Port Authority");
            requestData.put("proposedPortAuthorityEmail", proposedPaEmail != null ? proposedPaEmail.trim() : "pa@docktech.com");
            requestData.put("portEstablishmentProposalFileName", supportingFileName != null ? supportingFileName : "Port_Establishment_Proposal.pdf");
            requestData.put("portEstablishmentProposalUrl", pUrlStr);
            requestData.put("portEstablishmentProposalPublicId", supportingPublicId != null ? supportingPublicId : "");
            requestData.put("portEstablishmentProposalResourceType", supportingResourceType != null ? supportingResourceType : "pdf");
            requestData.put("portEstablishmentProposalFormat", supportingFormat != null ? supportingFormat : "pdf");
            requestData.put("portEstablishmentProposalUploadedAt", supportingUploadedAt != null ? supportingUploadedAt : dateStr);

            // Save to local store & disk
            saveLocalDiskStatus(targetReqId, "PENDING");
            localRequestStore.put(targetReqId, requestData);

            // Sync with AdminDataStore in-memory model
            PortRequest obj = parsePortRequestFromMap(requestData);

            mergeIntoAdminDataStore(java.util.List.of(obj));
            System.out.println("[PORT REQUEST DAO] Saved request to local store & AdminDataStore: " + targetReqId);

            // Save to Firestore
            try {
                Firestore db = db();
                if (db != null) {
                    com.google.api.core.ApiFuture<com.google.cloud.firestore.WriteResult> apiFuture = db
                            .collection("port_requests").document(targetReqId).set(requestData);

                    apiFuture.addListener(() -> {
                        try {
                            apiFuture.get();
                            System.out.println("========== FIRESTORE SAVE SUCCESS ==========");
                            System.out.println("REQUEST ID: " + targetReqId);
                            future.complete(requestData);
                        } catch (Exception ex) {
                            System.err.println("========== FIRESTORE SAVE FAILED ==========");
                            ex.printStackTrace();
                            future.completeExceptionally(ex);
                        }
                    }, Runnable::run);
                } else {
                    System.out.println("[PORT REQUEST DAO] Firestore unavailable; completed locally.");
                    future.complete(requestData);
                }
            } catch (Exception ex) {
                System.err.println("========== FIRESTORE SAVE FAILED ==========");
                ex.printStackTrace();
                future.completeExceptionally(ex);
            }
        } catch (Exception ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }

    public CompletableFuture<Boolean> updateRequestStatus(String requestId, String newStatus, String rejectionReason) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        final String statusVal = newStatus != null ? newStatus.toUpperCase().trim() : "PENDING";
        final String targetReqId = requestId != null ? requestId.trim() : "";

        saveLocalDiskStatus(targetReqId, statusVal);
        applyStatusToStores(targetReqId, statusVal, rejectionReason);

        try {
            Firestore db = db();
            if (db != null) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", statusVal);
                if (rejectionReason != null) {
                    updates.put("rejectionReason", rejectionReason);
                }
                updates.put("updatedAt", LocalDateTime.now().toString());

                com.google.api.core.ApiFuture<com.google.cloud.firestore.WriteResult> apiFuture = db
                        .collection("port_requests").document(targetReqId)
                        .set(updates, com.google.cloud.firestore.SetOptions.merge());

                apiFuture.addListener(() -> {
                    try {
                        apiFuture.get();
                        System.out.println("[PORT REQUEST DAO] Updated status in Firestore for document: " + targetReqId + " -> " + statusVal);
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
