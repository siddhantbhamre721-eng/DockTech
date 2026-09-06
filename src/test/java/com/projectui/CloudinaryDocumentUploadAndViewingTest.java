package com.projectui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;

import com.projectui.dao.PortRequestDao;
import com.projectui.service.CloudinaryService;

public class CloudinaryDocumentUploadAndViewingTest {

    @Test
    public void testCompleteUploadAndViewingFlow() throws Exception {
        System.out.println("========== [TEST] STARTING CLOUDINARY UPLOAD & VIEWING FLOW ==========");

        // 1. Create a genuine, valid test PDF file locally
        File tempPdf = File.createTempFile("Aadhaar_Card_Test_", ".pdf");
        tempPdf.deleteOnExit();

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("DockTech Verification Document: Aadhaar Card PDF");
                contentStream.endText();
            }
            doc.save(tempPdf);
        }

        assertTrue(tempPdf.exists(), "Local PDF file should exist");
        assertTrue(tempPdf.length() > 0, "Local PDF file should have content");
        System.out.println("[TEST] Generated local test PDF: " + tempPdf.getAbsolutePath() + " (" + tempPdf.length() + " bytes)");

        // 2. Upload to Cloudinary using CloudinaryService
        String reqId = "NPR-TEST-" + System.currentTimeMillis() % 10000;
        System.out.println("[TEST] Uploading PDF to Cloudinary for Request ID: " + reqId + "...");
        CloudinaryService.DocumentMetadata meta = CloudinaryService.uploadDocument(tempPdf, reqId, "aadhaar")
                .get(30, TimeUnit.SECONDS);

        assertNotNull(meta, "Returned DocumentMetadata should not be null");
        assertNotNull(meta.cloudinaryUrl, "Cloudinary URL should not be null");
        assertTrue(!meta.cloudinaryUrl.isBlank(), "Cloudinary URL should not be blank");
        assertTrue(meta.cloudinaryUrl.startsWith("https://res.cloudinary.com/"), "URL must be a secure Cloudinary URL");
        assertNotNull(meta.publicId, "Public ID should not be null");
        assertTrue(!meta.publicId.isBlank(), "Public ID should not be blank");
        assertEquals("raw", meta.resourceType, "Resource type for PDF must be raw");

        System.out.println("[TEST] Cloudinary upload successful!");
        System.out.println("  Cloudinary URL: " + meta.cloudinaryUrl);
        System.out.println("  Public ID:      " + meta.publicId);
        System.out.println("  Resource Type:  " + meta.resourceType);

        // 3. Download the exact Cloudinary secure_url directly
        System.out.println("[TEST] Fetching document bytes from Cloudinary URL: " + meta.cloudinaryUrl);
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(meta.cloudinaryUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .header("Accept", "application/pdf,*/*")
                .GET()
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200, response.statusCode(), "Cloudinary fetch should return HTTP 200 OK");
        byte[] fetchedBytes = response.body();
        assertNotNull(fetchedBytes, "Fetched bytes should not be null");
        assertTrue(fetchedBytes.length > 0, "Fetched bytes should have content");
        System.out.println("[TEST] Successfully retrieved " + fetchedBytes.length + " bytes from Cloudinary.");

        // 4. Verify Apache PDFBox can parse the downloaded document
        try (PDDocument downloadedDoc = PDDocument.load(fetchedBytes)) {
            assertNotNull(downloadedDoc, "PDFBox should successfully load the downloaded PDF");
            assertTrue(downloadedDoc.getNumberOfPages() >= 1, "PDF document must have at least 1 page");
            System.out.println("[TEST] Apache PDFBox successfully loaded document! Page count: " + downloadedDoc.getNumberOfPages());
        }

        // 5. Verify Firestore persistence flow
        System.out.println("[TEST] Saving Port Request with Cloudinary URL to Firestore...");
        PortRequestDao dao = new PortRequestDao();
        Map<String, Object> saved = dao.savePortRequestFull(
                reqId, "Test Port", "IN-TST-01", "Mumbai, Maharashtra", "Gateway Port", "New Port Proposal",
                "Requester Name", "requester@docktech.com", "+91 99999 11111",
                "Port Authority Name", "pa@docktech.com",
                tempPdf.getName() + ", proposal.pdf",
                tempPdf.getName(), meta.cloudinaryUrl, meta.publicId, meta.resourceType, meta.format, meta.uploadedAt,
                "proposal.pdf", meta.cloudinaryUrl, meta.publicId, meta.resourceType, meta.format, meta.uploadedAt
        ).get(15, TimeUnit.SECONDS);

        assertNotNull(saved, "Saved Firestore record should not be null");
        assertEquals(meta.cloudinaryUrl, saved.get("aadhaarUrl"), "Saved aadhaarUrl must match exact Cloudinary secure_url");
        assertEquals(meta.publicId, saved.get("aadhaarPublicId"), "Saved aadhaarPublicId must match Cloudinary public_id");

        System.out.println("[TEST] Firestore verification passed! Saved record has exact Cloudinary URL.");
        System.out.println("========== [TEST] FLOW COMPLETED SUCCESSFULLY ==========");
    }
}
