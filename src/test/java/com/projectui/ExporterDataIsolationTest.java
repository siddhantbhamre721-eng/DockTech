package com.projectui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.projectui.dao.CargoDao;
import com.projectui.dao.ContainerDao;
import com.projectui.dao.PaymentDao;
import com.projectui.view.login.LoginPage;

public class ExporterDataIsolationTest {

    @Test
    public void testExporterDataIsolationFlow() throws Exception {
        CargoDao cargoDao = new CargoDao();
        ContainerDao containerDao = new ContainerDao();
        PaymentDao paymentDao = new PaymentDao();

        // 1. Setup mock user Pravin (New Exporter)
        String pravinUid = "UID_PRAVIN_" + System.currentTimeMillis();
        Map<String, Object> pravinUser = new HashMap<>();
        pravinUser.put("uid", pravinUid);
        pravinUser.put("email", "pravin@gmail.com");
        pravinUser.put("name", "Pravin Chavan");
        pravinUser.put("role", "Exporter");
        pravinUser.put("company", "Chavan Exports Ltd.");

        // 2. Setup mock user Other Exporter
        String otherUid = "UID_OTHER_" + System.currentTimeMillis();
        Map<String, Object> otherUser = new HashMap<>();
        otherUser.put("uid", otherUid);
        otherUser.put("email", "other_exporter@gmail.com");
        otherUser.put("name", "Other Exporter");
        otherUser.put("role", "Exporter");
        otherUser.put("company", "Other Logistics");

        // Set active session as Pravin
        LoginPage.loggedInUser = pravinUser;

        // Verify initially Pravin has 0 cargos, 0 containers, 0 payments
        List<Map<String, Object>> pravinInitialCargos = cargoDao.getShipmentsForExporter(pravinUser);
        List<Map<String, Object>> pravinInitialContainers = containerDao.getContainersForExporter(pravinUser);
        List<Map<String, Object>> pravinInitialPayments = paymentDao.getPaymentsForExporter(pravinUser);

        assertEquals(0, pravinInitialCargos.size(), "New exporter must have 0 initial cargos");
        assertEquals(0, pravinInitialContainers.size(), "New exporter must have 0 initial containers");
        assertEquals(0, pravinInitialPayments.size(), "New exporter must have 0 initial payments");

        // 3. Pravin creates a new Cargo
        String cargoId = "CGO-PRAVIN-" + System.currentTimeMillis();
        cargoDao.saveCargo(cargoId, "Solar Inverters", "Electronics", "10", "1500", "500000", "Port of Mumbai", "Port of Dubai", "High", "Registered").get();

        // Verify Pravin now has 1 cargo
        List<Map<String, Object>> pravinCargos = cargoDao.getShipmentsForExporter(pravinUser);
        assertEquals(1, pravinCargos.size(), "Pravin should now see his 1 registered cargo");
        assertEquals(cargoId, pravinCargos.get(0).get("cargoId"));
        assertEquals(pravinUid, pravinCargos.get(0).get("ownerUid"));

        // Verify Other Exporter does NOT see Pravin's cargo
        List<Map<String, Object>> otherCargos = cargoDao.getShipmentsForExporter(otherUser);
        assertEquals(0, otherCargos.size(), "Other exporter must NOT see Pravin's cargo");

        // 4. Pravin books a container
        String bookingId = "CONT-PRAVIN-" + System.currentTimeMillis();
        containerDao.saveBooking(bookingId, cargoId, "Solar Inverters", "20 ft Standard Dry", "1", "Port of Mumbai", "Port of Mumbai", "Port of Dubai", 45000.0, "Booked").get();

        // Verify Pravin sees his container booking
        List<Map<String, Object>> pravinContainers = containerDao.getContainersForExporter(pravinUser);
        System.out.println("[TEST DEBUG] pravinContainers count=" + pravinContainers.size() + " items=" + pravinContainers);
        assertEquals(1, pravinContainers.size(), "Pravin should see his 1 container booking");
        assertEquals(bookingId, pravinContainers.get(0).get("bookingId"));
        assertEquals(pravinUid, pravinContainers.get(0).get("ownerUid"));

        // Verify Other Exporter does NOT see Pravin's container
        List<Map<String, Object>> otherContainers = containerDao.getContainersForExporter(otherUser);
        assertEquals(0, otherContainers.size(), "Other exporter must NOT see Pravin's container");

        // 5. Pravin records a payment
        Map<String, Object> paymentData = new HashMap<>();
        String paymentId = "PAY-PRAVIN-" + System.currentTimeMillis();
        paymentData.put("paymentId", paymentId);
        paymentData.put("cargoId", cargoId);
        paymentData.put("amount", 45000.0);
        paymentData.put("status", "PAID");
        paymentDao.savePayment(paymentData);

        // Verify Pravin sees his payment
        List<Map<String, Object>> pravinPayments = paymentDao.getPaymentsForExporter(pravinUser);
        assertEquals(1, pravinPayments.size(), "Pravin should see his 1 payment");
        assertEquals(paymentId, pravinPayments.get(0).get("paymentId"));
        assertEquals(pravinUid, pravinPayments.get(0).get("ownerUid"));

        // Verify Other Exporter does NOT see Pravin's payment
        List<Map<String, Object>> otherPayments = paymentDao.getPaymentsForExporter(otherUser);
        assertEquals(0, otherPayments.size(), "Other exporter must NOT see Pravin's payment");

        // 6. Reset session
        LoginPage.loggedInUser = null;
    }
}
