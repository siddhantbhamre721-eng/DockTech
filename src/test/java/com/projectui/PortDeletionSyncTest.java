package com.projectui;

import com.projectui.view.admin.AdminDataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PortDeletionSyncTest {

    private AdminDataStore store;

    @BeforeEach
    public void setup() {
        store = AdminDataStore.getInstance();
    }

    @Test
    public void testPortCreationDeletionAndSync() {
        // 1. Initial baseline
        int initialTotal = store.totalPorts();
        int initialActive = store.activePorts();

        // 2. Add a dynamic port
        String testPortName = "Test Maritime Port " + System.currentTimeMillis();
        String testPortCode = "IN-TST-01";
        AdminDataStore.Port newPort = new AdminDataStore.Port(
                testPortName,
                "Gujarat, India",
                testPortCode,
                "Test Authority",
                10,
                0,
                "Operational",
                "Containers",
                "80%",
                "Clear",
                "Today",
                21.1234,
                72.5678,
                "APPROVED"
        );

        AdminDataStore.clearDeletedPort(testPortName, testPortCode);
        store.ports.add(newPort);

        assertEquals(initialTotal + 1, store.totalPorts(), "Total ports count must increment on creation");
        assertEquals(initialActive + 1, store.activePorts(), "Active ports count must increment when operational");
        assertFalse(AdminDataStore.isPortDeleted(testPortName, testPortCode), "Port should not be marked as deleted");

        // 3. Perform Deletion
        store.deletePort(newPort);

        // 4. Assert port is marked deleted and excluded everywhere
        assertTrue(AdminDataStore.isPortDeleted(testPortName, testPortCode), "Port must be registered in deleted ports store");
        assertEquals(initialTotal, store.totalPorts(), "Total ports count must immediately decrease on deletion");
        assertEquals(initialActive, store.activePorts(), "Active ports count must immediately decrease on deletion");
        assertFalse(store.getApprovedPortNames().contains(testPortName), "Approved port names must not contain deleted port");

        // Clean up
        AdminDataStore.clearDeletedPort(testPortName, testPortCode);
    }
}
