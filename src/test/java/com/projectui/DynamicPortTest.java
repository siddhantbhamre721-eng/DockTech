package com.projectui;

import com.projectui.dao.UserDao;
import com.projectui.dao.CargoDao;
import com.projectui.view.admin.AdminDataStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class DynamicPortTest {

    private AdminDataStore dataStore;

    @BeforeEach
    public void setUp() {
        dataStore = AdminDataStore.getInstance();
    }

    @Test
    public void testDynamicPortAdditionAndGlobalAvailability() {
        // 1. Initial approved ports
        List<String> initialPorts = dataStore.getApprovedPortNames();
        Assertions.assertNotNull(initialPorts);
        Assertions.assertFalse(initialPorts.isEmpty());

        // 2. Add a brand new fourth / dynamic port
        String newPortName = "Port of Paradip Deepsea";
        String newPortCode = "IN-PRT-99";
        String newLocation = "Paradip, Odisha";
        String newPA = "Paradip Deepsea Port Authority";
        double newLat = 20.2600;
        double newLng = 86.6700;

        AdminDataStore.Port newPort = new AdminDataStore.Port(
                newPortName, newLocation, newPortCode, newPA, 16, 4,
                "Operational", "Bulk Minerals, Iron Ore", "82%", "Clear",
                "Just now", newLat, newLng, "APPROVED"
        );
        dataStore.ports.add(newPort);

        // 3. Verify AdminDataStore approved ports contains the new port
        List<String> updatedPorts = dataStore.getApprovedPortNames();
        Assertions.assertTrue(updatedPorts.contains(newPortName), "New port must be in getApprovedPortNames()");

        // 4. Verify UserDao.normalizePort dynamically resolves the new port name
        String normalizedDirect = UserDao.normalizePort("Port of Paradip Deepsea");
        Assertions.assertEquals(newPortName, normalizedDirect);

        String normalizedFuzzy = UserDao.normalizePort("paradip deepsea");
        Assertions.assertEquals(newPortName, normalizedFuzzy);

        // 5. Verify getPortByName returns the correct port metadata and coordinates
        AdminDataStore.Port fetched = dataStore.getPortByName("Port of Paradip Deepsea");
        Assertions.assertNotNull(fetched);
        Assertions.assertEquals(newLat, fetched.lat, 0.001);
        Assertions.assertEquals(newLng, fetched.lng, 0.001);

        // 6. Verify dynamic cargo filtering with the new port
        CargoDao cargoDao = new CargoDao();
        List<Map<String, Object>> cargosForNewPort = cargoDao.getCargosByPort(newPortName);
        Assertions.assertNotNull(cargosForNewPort);

        // 7. Cleanup
        dataStore.ports.remove(newPort);
    }
}
