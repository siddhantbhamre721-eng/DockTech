package com.projectui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class ConfirmLoadingViewTest {

    @Test
    public void testConfirmLoadingHeadersAndChecklistChanges() throws Exception {
        Path path = Path.of("src/main/java/com/projectui/view/ShipCaptain/ConfirmLoading.java");
        assertTrue(Files.exists(path), "ConfirmLoading.java should exist");

        String content = Files.readString(path);

        // Verify table-headers.css is added to cargoTable for #111827 header text color
        assertTrue(content.contains("cargoTable.getStylesheets().add(tableCss.toExternalForm())"),
                "cargoTable should load table-headers.css with #111827 text color");

        // Verify the 3 checklist items from Verify & Confirm Cargo are removed
        assertFalse(content.contains("Cargo manifest & quantity verified"), "Manifest checkbox should be removed");
        assertFalse(content.contains("Compartment / container secured"), "Container secured checkbox should be removed");
        assertFalse(content.contains("Berth & safety clearance confirmed"), "Berth clearance checkbox should be removed");

        // Verify remaining elements of Verify & Confirm Cargo panel are preserved
        assertTrue(content.contains("Verify & Confirm Cargo"), "Verify & Confirm Cargo title should remain");
        assertTrue(content.contains("Confirm Loading Condition"), "Confirm Loading Condition should remain");
        assertTrue(content.contains("Loading Completed"), "Loading Completed combo item should remain");
        assertTrue(content.contains("Enter optional captain confirmation notes..."), "Notes placeholder should remain");
        assertTrue(content.contains("Confirm Cargo Loading"), "Confirm Cargo Loading button should remain");
    }
}
