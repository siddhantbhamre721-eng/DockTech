package com.projectui.controller;

import java.util.UUID;

public class ExpDocumentController {

    public String uploadDocument(String cargoId, String documentType, String fileName, String filePath) {
        String docId = "DOC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        System.out.println("[DOC CONTROLLER] Uploaded " + documentType + " for cargo " + cargoId + " -> " + docId);
        return docId;
    }
}
