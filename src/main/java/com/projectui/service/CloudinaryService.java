package com.projectui.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CloudinaryService {

    public static final String CLOUD_NAME = System.getenv("CLOUDINARY_CLOUD_NAME") != null
            && !System.getenv("CLOUDINARY_CLOUD_NAME").isBlank()
            ? System.getenv("CLOUDINARY_CLOUD_NAME").trim()
            : "zab3bobe";

    public static final String API_KEY = System.getenv("CLOUDINARY_API_KEY") != null
            && !System.getenv("CLOUDINARY_API_KEY").isBlank()
            ? System.getenv("CLOUDINARY_API_KEY").trim()
            : "392742456698257";

    public static final String API_SECRET = System.getenv("CLOUDINARY_API_SECRET") != null
            && !System.getenv("CLOUDINARY_API_SECRET").isBlank()
            ? System.getenv("CLOUDINARY_API_SECRET").trim()
            : "u4MvBHIEtppvkebtQ2yf74V4wZU";

    public static final String UPLOAD_PRESET = System.getenv("CLOUDINARY_UPLOAD_PRESET") != null
            && !System.getenv("CLOUDINARY_UPLOAD_PRESET").isBlank()
            ? System.getenv("CLOUDINARY_UPLOAD_PRESET").trim()
            : "";

    public static class DocumentMetadata {
        public String fileName;
        public String cloudinaryUrl;
        public String publicId;
        public String resourceType;
        public String format;
        public String uploadedAt;

        public DocumentMetadata(String fileName, String cloudinaryUrl, String publicId, String resourceType, String format, String uploadedAt) {
            this.fileName = fileName != null ? fileName : "document.pdf";
            this.cloudinaryUrl = cloudinaryUrl != null ? cloudinaryUrl : "";
            this.publicId = publicId != null ? publicId : "doc_" + UUID.randomUUID().toString().substring(0, 8);
            this.resourceType = resourceType != null ? resourceType : (this.fileName.toLowerCase().endsWith(".pdf") ? "raw" : "image");
            this.format = format != null ? format : (this.fileName.contains(".") ? this.fileName.substring(this.fileName.lastIndexOf(".") + 1) : "pdf");
            this.uploadedAt = uploadedAt != null ? uploadedAt : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
        }

        public DocumentMetadata(String fileName, String cloudinaryUrl, String publicId, String resourceType, String uploadedAt) {
            this(fileName, cloudinaryUrl, publicId, resourceType, fileName != null && fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1) : "pdf", uploadedAt);
        }
    }

    public static CompletableFuture<DocumentMetadata> uploadDocument(File file) {
        return uploadDocument(file, "general", "documents");
    }

    public static CompletableFuture<DocumentMetadata> uploadDocument(File file, String requestId, String docCategory) {
        CompletableFuture<DocumentMetadata> future = new CompletableFuture<>();
        if (file == null || !file.exists()) {
            future.completeExceptionally(new IllegalArgumentException("No file selected for upload."));
            return future;
        }

        // File validation: Size limit 10MB (10 * 1024 * 1024 bytes)
        if (file.length() > 10 * 1024 * 1024) {
            future.completeExceptionally(new IllegalArgumentException("File size exceeds maximum allowed limit of 10MB."));
            return future;
        }

        java.util.concurrent.ForkJoinPool.commonPool().execute(() -> {
            String jsonResponse = "";
            try {
                String originalFileName = file.getName();
                boolean isPdf = originalFileName.toLowerCase().endsWith(".pdf");
                String targetResourceType = isPdf ? "raw" : "auto";

                String cleanFileName = originalFileName.replaceAll("[^A-Za-z0-9._-]", "_");
                String safeReqId = (requestId != null && !requestId.isBlank()) ? requestId.replaceAll("[^A-Za-z0-9._-]", "_") : "general";
                String safeCat = (docCategory != null && !docCategory.isBlank()) ? docCategory.replaceAll("[^A-Za-z0-9._-]", "_") : "documents";

                String publicId = "port-requests/" + safeReqId + "/" + safeCat + "/" + cleanFileName;
                String boundary = "---CloudinaryBoundary" + System.currentTimeMillis();

                URL uploadUrl = new URL("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/" + targetResourceType + "/upload");
                HttpURLConnection conn = (HttpURLConnection) uploadUrl.openConnection();
                conn.setUseCaches(false);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);

                boolean useSigned = API_SECRET != null && !API_SECRET.isBlank() && API_KEY != null && !API_KEY.isBlank();
                String timestampStr = String.valueOf(System.currentTimeMillis() / 1000);
                String signature = "";

                if (useSigned) {
                    // Parameters for signature must be sorted alphabetically: public_id, timestamp
                    String toSign = "public_id=" + publicId + "&timestamp=" + timestampStr + API_SECRET;
                    signature = sha1(toSign);
                }

                try (OutputStream outputStream = conn.getOutputStream();
                     PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true)) {

                    if (useSigned) {
                        // api_key
                        writer.append("--").append(boundary).append("\r\n");
                        writer.append("Content-Disposition: form-data; name=\"api_key\"\r\n\r\n");
                        writer.append(API_KEY).append("\r\n");

                        // timestamp
                        writer.append("--").append(boundary).append("\r\n");
                        writer.append("Content-Disposition: form-data; name=\"timestamp\"\r\n\r\n");
                        writer.append(timestampStr).append("\r\n");

                        // signature
                        writer.append("--").append(boundary).append("\r\n");
                        writer.append("Content-Disposition: form-data; name=\"signature\"\r\n\r\n");
                        writer.append(signature).append("\r\n");

                        // public_id
                        writer.append("--").append(boundary).append("\r\n");
                        writer.append("Content-Disposition: form-data; name=\"public_id\"\r\n\r\n");
                        writer.append(publicId).append("\r\n");
                    } else if (UPLOAD_PRESET != null && !UPLOAD_PRESET.isBlank()) {
                        // upload_preset fallback if no API_SECRET provided
                        writer.append("--").append(boundary).append("\r\n");
                        writer.append("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n");
                        writer.append(UPLOAD_PRESET).append("\r\n");

                        writer.append("--").append(boundary).append("\r\n");
                        writer.append("Content-Disposition: form-data; name=\"folder\"\r\n\r\n");
                        writer.append("port-requests/" + safeReqId + "/" + safeCat).append("\r\n");
                    }

                    // file field
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(originalFileName).append("\"\r\n");
                    writer.append("Content-Type: ").append(isPdf ? "application/pdf" : "application/octet-stream").append("\r\n\r\n");
                    writer.flush();

                    try (FileInputStream inputStream = new FileInputStream(file)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.flush();
                    }

                    writer.append("\r\n");
                    writer.append("--").append(boundary).append("--\r\n");
                    writer.flush();
                }

                int status = conn.getResponseCode();
                java.io.InputStream responseStream = status >= 200 && status < 300
                        ? conn.getInputStream() : conn.getErrorStream();
                if (responseStream != null) {
                    try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(responseStream, StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                        jsonResponse = sb.toString();
                    }
                } else {
                    jsonResponse = "";
                }

                if (status >= 200 && status < 300) {
                    String secureUrl = extractJsonValue(jsonResponse, "secure_url");
                    if (secureUrl.isBlank()) {
                        secureUrl = extractJsonValue(jsonResponse, "url");
                    }
                    String resPublicId = extractJsonValue(jsonResponse, "public_id");
                    String resourceType = extractJsonValue(jsonResponse, "resource_type");
                    String format = extractJsonValue(jsonResponse, "format");

                    if (resourceType.isBlank()) {
                        resourceType = isPdf ? "raw" : "image";
                    }
                    if (format.isBlank() && originalFileName.contains(".")) {
                        format = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
                    }

                    if (!secureUrl.isBlank() && !resPublicId.isBlank()) {
                        System.out.println("[CLOUDINARY UPLOAD SUCCESS]");
                        System.out.println("  Cloud name        : " + CLOUD_NAME);
                        System.out.println("  Public ID         : " + resPublicId);
                        System.out.println("  Resource type     : " + resourceType);
                        System.out.println("  Format            : " + format);
                        System.out.println("  Secure URL        : " + secureUrl);
                        System.out.println("  Original filename : " + originalFileName);

                        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
                        future.complete(new DocumentMetadata(originalFileName, secureUrl, resPublicId, resourceType, format, timeStr));
                        return;
                    }
                }
            } catch (Exception ex) {
                System.err.println("[CLOUDINARY] Upload exception: " + ex.getMessage());
            }

            String errMsg = extractJsonValue(jsonResponse, "message");
            if (errMsg.isBlank()) {
                errMsg = jsonResponse.isBlank() ? "No response received from Cloudinary." : jsonResponse;
            }
            future.completeExceptionally(new IllegalStateException("Cloudinary upload failed: " + errMsg));
        });

        return future;
    }

    public static boolean deleteAsset(String publicId, String resourceType) {
        String apiKey = API_KEY;
        String apiSecret = API_SECRET;
        if (publicId == null || publicId.isBlank() || apiKey == null || apiKey.isBlank()
                || apiSecret == null || apiSecret.isBlank()) return false;
        try {
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String signature = sha1("public_id=" + publicId + "&timestamp=" + timestamp + apiSecret);
            String target = resourceType != null && !resourceType.isBlank() ? resourceType : "image";
            if ("auto".equalsIgnoreCase(target)) target = "image";
            URL deleteUrl = new URL("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/" + target + "/destroy");
            HttpURLConnection conn = (HttpURLConnection) deleteUrl.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            String body = "public_id=" + java.net.URLEncoder.encode(publicId, StandardCharsets.UTF_8)
                    + "&timestamp=" + timestamp + "&api_key=" + java.net.URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
                    + "&signature=" + signature;
            try (OutputStream output = conn.getOutputStream()) {
                output.write(body.getBytes(StandardCharsets.UTF_8));
            }
            return conn.getResponseCode() >= 200 && conn.getResponseCode() < 300;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String sha1(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-1").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : digest) result.append(String.format("%02x", b));
        return result.toString();
    }

    private static String extractJsonValue(String json, String key) {
        if (json == null || !json.contains("\"" + key + "\"")) return "";
        try {
            int idx = json.indexOf("\"" + key + "\"");
            int colon = json.indexOf(":", idx);
            int startQuote = json.indexOf("\"", colon);
            int endQuote = json.indexOf("\"", startQuote + 1);
            if (startQuote != -1 && endQuote != -1) {
                return json.substring(startQuote + 1, endQuote).replace("\\/", "/");
            }
        } catch (Exception ignored) {}
        return "";
    }
}

