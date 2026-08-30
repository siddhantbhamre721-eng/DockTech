package com.projectui;

import com.projectui.dao.CargoDao;
import com.projectui.dao.PaymentDao;
import com.projectui.service.ReceiptPdfService;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentReceiptTest {

    @Test
    public void testPaymentDaoAndReceiptPdfGeneration() {
        PaymentDao paymentDao = new PaymentDao();

        Map<String, Object> payment = new HashMap<>();
        payment.put("paymentId", "pay_TEST1234567890");
        payment.put("orderId", "order_TEST1234567890");
        payment.put("signature", "sig_test_mock_123");
        payment.put("receiptNumber", "RCT-998877");
        payment.put("cargoId", "CGO-TEST-001");
        payment.put("cargoName", "Automotive Spare Parts");
        payment.put("cargoType", "Containerized");
        payment.put("weight", "14,500 kg");
        payment.put("importerName", "Global Traders Ltd.");
        payment.put("exporterName", "Bharat Logistics & Freight Ltd.");
        payment.put("originPort", "Port of Mumbai");
        payment.put("destinationPort", "Port of Singapore");
        payment.put("amount", 106400.0);
        payment.put("currency", "INR");
        payment.put("paymentStatus", "PAID");
        payment.put("paymentMethod", "Razorpay Standard Checkout (NetBanking)");

        // 1. Save payment
        Map<String, Object> saved = paymentDao.savePayment(payment);
        assertNotNull(saved);
        assertEquals("PAID", saved.get("paymentStatus"));
        assertEquals("RCT-998877", saved.get("receiptNumber"));

        // 2. Query payment by cargo ID & payment ID
        Map<String, Object> queried = paymentDao.getPaymentByCargoId("CGO-TEST-001");
        assertNotNull(queried);
        assertEquals("pay_TEST1234567890", queried.get("paymentId"));

        List<Map<String, Object>> importerPayments = paymentDao.getPaymentsForImporter("Global Traders");
        assertFalse(importerPayments.isEmpty());

        // 3. Generate PDF Receipt
        File pdf = ReceiptPdfService.generateReceipt(saved);
        assertNotNull(pdf);
        assertTrue(pdf.exists());
        assertTrue(pdf.length() > 0);
        System.out.println("Test PDF generated at: " + pdf.getAbsolutePath() + " (size: " + pdf.length() + " bytes)");

        // 4. Test CargoDao payment update
        CargoDao cargoDao = new CargoDao();
        boolean updated = cargoDao.updateCargoPaymentStatus("CGO-TEST-001", "PAID", "pay_TEST1234567890", "order_TEST1234567890", 106400.0, "RCT-998877");
        assertTrue(updated);
    }

    @Test
    public void testOnlyRealPaymentsInPaymentManagement() {
        PaymentDao paymentDao = new PaymentDao();

        // Query real payments
        List<Map<String, Object>> allPayments = paymentDao.getAllPayments();
        assertNotNull(allPayments);

        for (Map<String, Object> p : allPayments) {
            String payId = (String) p.get("paymentId");
            assertNotNull(payId);
            assertFalse(payId.isBlank());

            // Ensure amount is positive
            double amt = Double.parseDouble(p.get("amount").toString());
            assertTrue(amt > 0);

            // Ensure customerType is valid
            String type = (String) p.get("customerType");
            if (type != null) {
                assertTrue("Exporter".equalsIgnoreCase(type) || "Importer".equalsIgnoreCase(type));
            }
        }
    }

    @Test
    public void testContainerAllocationStatusSynchronization() {
        com.projectui.dao.ContainerDao containerDao = new com.projectui.dao.ContainerDao();

        String reqId = "REQ-TEST-9988";
        String cargoName = "Precision Instruments";
        String cont1 = "MSKU-112233";
        String cont2 = "MSKU-445566";

        // Initial state: Required = 2, Allocated = 0, Remaining = 2
        int required = 2;
        int initialAllocated = 0;
        int initialRemaining = required - initialAllocated;
        assertEquals(2, initialRemaining);

        // Step 1: Allocate 1 container
        containerDao.saveBooking(cont1, reqId, cargoName, "20 ft Standard Dry", "1", "Cochin Port", "Cochin Port", "Port of Singapore", "Allocated");
        int allocCount1 = (int) containerDao.getAllBookings().stream()
                .filter(b -> reqId.equalsIgnoreCase((String) b.get("cargoId")) && "Allocated".equalsIgnoreCase((String) b.get("status")))
                .count();

        assertEquals(1, allocCount1);
        int remaining1 = required - allocCount1;
        assertEquals(1, remaining1);
        String status1 = allocCount1 == 0 ? "Pending Allocation" : (remaining1 == 0 ? "Allocated" : "Partially Allocated");
        assertEquals("Partially Allocated", status1);

        // Step 2: Allocate 2nd container
        containerDao.saveBooking(cont2, reqId, cargoName, "20 ft Standard Dry", "1", "Cochin Port", "Cochin Port", "Port of Singapore", "Allocated");
        int allocCount2 = (int) containerDao.getAllBookings().stream()
                .filter(b -> reqId.equalsIgnoreCase((String) b.get("cargoId")) && "Allocated".equalsIgnoreCase((String) b.get("status")))
                .count();

        assertEquals(2, allocCount2);
        int remaining2 = Math.max(required - allocCount2, 0);
        assertEquals(0, remaining2);
        String status2 = allocCount2 == 0 ? "Pending Allocation" : (remaining2 == 0 ? "Allocated" : "Partially Allocated");
        assertEquals("Allocated", status2);
    }
}
