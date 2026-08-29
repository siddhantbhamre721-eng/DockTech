package com.projectui.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CloudinaryService {

    private static final String CLOUD_NAME = System.getenv("CLOUDINARY_CLOUD_NAME") != null
            && !System.getenv("CLOUDINARY_CLOUD_NAME").isBlank()
            ? System.getenv("CLOUDINARY_CLOUD_NAME")
            : "snmxmesm";

    private static final String UPLOAD_PRESET = System.getenv("CLOUDINARY_UPLOAD_PRESET") != null
            && !System.getenv("CLOUDINARY_UPLOAD_PRESET").isBlank()
            ? System.getenv("CLOUDINARY_UPLOAD_PRESET")
            : "docktech_port_documents";

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
            this.resourceType = resourceType != null ? resourceType : (fileName != null && fileName.toLowerCase().endsWith(".pdf") ? "pdf" : "image");
            this.format = format != null ? format : (fileName != null && fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1) : "pdf");
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
            future.complete(new DocumentMetadata("document.pdf", "https://res.cloudinary.com/" + CLOUD_NAME + "/raw/upload/docktech_port_documents/sample_document.pdf", "doc_sample", "pdf", "pdf", null));
            return future;
        }

        // File validation: Size limit 10MB (10 * 1024 * 1024 bytes)
        if (file.length() > 10 * 1024 * 1024) {
            future.completeExceptionally(new IllegalArgumentException("File size exceeds maximum allowed limit of 10MB."));
            return future;
        }

        java.util.concurrent.ForkJoinPool.commonPool().execute(() -> {
            try {
                String fileName = file.getName();
                String targetResourceType = fileName.toLowerCase().endsWith(".pdf") ? "raw" : "auto";
                String folderPath = "port-requests/" + (requestId != null ? requestId : "general") + "/" + (docCategory != null ? docCategory : "documents");
                String boundary = "---CloudinaryBoundary" + System.currentTimeMillis();
                URL uploadUrl = new URL("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/" + targetResourceType + "/upload");
                HttpURLConnection conn = (HttpURLConnection) uploadUrl.openConnection();
                conn.setUseCaches(false);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(12000);

                try (OutputStream outputStream = conn.getOutputStream();
                     PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {

                    // upload_preset field using docktech_port_documents
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n");
                    writer.append(UPLOAD_PRESET).append("\r\n");

                    // folder field for structured path: port-requests/{requestId}/{category}
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"folder\"\r\n\r\n");
                    writer.append(folderPath).append("\r\n");

                    // file field
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileName).append("\"\r\n");
                    writer.append("Content-Type: application/octet-stream\r\n\r\n");
                    writer.flush();

                    try (FileInputStream inputStream = new FileInputStream(file)) {
                        byte[] buffer = new byte[4096];
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
                if (status == 200) {
                    String jsonResponse;
                    try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                        jsonResponse = sb.toString();
                    }

                    String secureUrl = extractJsonValue(jsonResponse, "secure_url");
                    String publicId = extractJsonValue(jsonResponse, "public_id");
                    String resourceType = extractJsonValue(jsonResponse, "resource_type");
                    String format = extractJsonValue(jsonResponse, "format");

                    if (secureUrl.isBlank()) {
                        secureUrl = extractJsonValue(jsonResponse, "url");
                    }

                    if (!secureUrl.isBlank()) {
                        System.out.println("[CLOUDINARY UPLOAD DIAGNOSTIC]");
                        System.out.println("  Cloud name        : " + CLOUD_NAME);
                        System.out.println("  Public ID         : " + publicId);
                        System.out.println("  Resource type     : " + resourceType);
                        System.out.println("  Format            : " + format);
                        System.out.println("  Secure URL        : " + secureUrl);
                        System.out.println("  Original filename : " + fileName);

                        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
                        future.complete(new DocumentMetadata(fileName, secureUrl, publicId, resourceType, format, timeStr));
                        return;
                    }
                }
            } catch (Exception ex) {
                System.err.println("[CLOUDINARY] Upload notice: " + ex.getMessage());
            }

            // Fallback to local file URI if offline/unconfigured so local file content can be rendered
            String cleanName = file.getName().replaceAll("[^A-Za-z0-9._-]", "_");
            String resourcePath = cleanName.toLowerCase().endsWith(".pdf") ? "raw" : "image";
            String fallbackUrl = file.exists() ? file.toURI().toString() : ("https://res.cloudinary.com/" + CLOUD_NAME + "/" + resourcePath + "/upload/docktech_port_documents/" + cleanName);
            String publicId = "doc_" + Math.abs(cleanName.hashCode());
            String resType = cleanName.toLowerCase().endsWith(".pdf") ? "pdf" : "image";
            String fmt = cleanName.toLowerCase().endsWith(".pdf") ? "pdf" : "jpg";
            future.complete(new DocumentMetadata(file.getName(), fallbackUrl, publicId, resType, fmt, null));
        });

        return future;
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
