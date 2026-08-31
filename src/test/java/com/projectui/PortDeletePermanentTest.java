package com.projectui;

import com.projectui.dao.ShipDAO;
import com.projectui.dao.UserDao;
import com.projectui.model.Ship;
import com.projectui.service.VesselData;
import com.projectui.service.VesselManager;
import com.projectui.view.admin.AdminDataStore;
import com.projectui.view.admin.GeoTileMapCanvas;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PortDeletePermanentTest {

    private AdminDataStore store;

    @BeforeEach
    public void setUp() {
        store = AdminDataStore.getInstance();
    }

    @Test
    public void testDeletePortPermanenceAndIsolation() {
        String testPortName = "Port of Veraval Deepwater";
        String testPortCode = "IN-VRL-99";
        String testPA = "Veraval Port Authority";

        AdminDataStore.clearDeletedPort(testPortName, testPortCode);

        // 1. Create Port
        AdminDataStore.Port testPort = new AdminDataStore.Port(
                testPortName, "Veraval, Gujarat", testPortCode, testPA, 10, 2,
                "Operational", "Containers, Bulk Cargo", "75%", "Clear", store.now(), 20.90, 70.36, "APPROVED"
        );
        store.ports.add(testPort);

        // Add an unrelated port
        String unrelatedPortName = "Port of Cochin Anchor";
        String unrelatedPortCode = "IN-COK-88";
        AdminDataStore.clearDeletedPort(unrelatedPortName, unrelatedPortCode);
        AdminDataStore.Port unrelatedPort = new AdminDataStore.Port(
                unrelatedPortName, "Kochi, Kerala", unrelatedPortCode, "Cochin PA", 12, 1,
                "Operational", "Liquid Cargo", "60%", "Clear", store.now(), 9.93, 76.26, "APPROVED"
        );
        store.ports.add(unrelatedPort);

        // 2. Add an associated vessel and an unrelated vessel in VesselManager
        VesselManager vm = VesselManager.getInstance();
        VesselData associatedVessel = new VesselData(
                999111222L, "9991112", "MV VERAVAL EXPRESS", "VRV01",
                20.89, 70.35, 8.5, 90.0, 90.0, testPortName, "Underway", "Cargo", "12:00:00"
        );
        VesselData unrelatedVessel = new VesselData(
                888222333L, "8882223", "MV COCHIN TRADER", "COK01",
                9.92, 76.25, 12.0, 180.0, 180.0, unrelatedPortName, "Underway", "Tanker", "12:00:00"
        );
        vm.updateOrAddVessel(associatedVessel);
        vm.updateOrAddVessel(unrelatedVessel);

        Assertions.assertNotNull(vm.getVessel(999111222L));
        Assertions.assertNotNull(vm.getVessel(888222333L));

        int initialTotalPorts = store.totalPorts();
        int initialActivePorts = store.activePorts();

        // 3. Delete the test port
        boolean deleted = store.deletePort(testPort);
        Assertions.assertTrue(deleted, "deletePort must return true");

        // 4. Verify test port is removed from store.ports
        Assertions.assertNull(store.getPortByIdOrName(testPortName));
        Assertions.assertNull(store.getPortByIdOrName(testPortCode));
        Assertions.assertTrue(AdminDataStore.isPortDeleted(testPortName, testPortCode));

        // 5. Verify unrelated port is still present
        Assertions.assertNotNull(store.getPortByIdOrName(unrelatedPortName));
        Assertions.assertEquals(unrelatedPortCode, store.getPortByIdOrName(unrelatedPortName).code);

        // 6. Verify associated vessel was removed, while unrelated vessel remains
        Assertions.assertNull(vm.getVessel(999111222L), "Associated vessel must be removed when port is deleted");
        Assertions.assertNotNull(vm.getVessel(888222333L), "Unrelated vessel must remain intact");

        // 7. Verify counters updated dynamically
        Assertions.assertEquals(initialTotalPorts - 1, store.totalPorts());
        Assertions.assertEquals(initialActivePorts - 1, store.activePorts());

        // 8. Verify getPortByName / fallback does not restore the deleted port
        Assertions.assertNull(store.getPortByName(testPortName));

        // Cleanup
        store.ports.remove(unrelatedPort);
        vm.removeVessel(888222333L);
        AdminDataStore.clearDeletedPort(testPortName, testPortCode);
        AdminDataStore.clearDeletedPort(unrelatedPortName, unrelatedPortCode);
    }
}
