package com.projectui;

import com.projectui.dao.CargoDao;
import com.projectui.dao.ContainerDao;
import com.projectui.dao.PaymentDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImporterDashboardDataFlowTest {

    private CargoDao cargoDao;
    private ContainerDao containerDao;
    private PaymentDao paymentDao;

    @BeforeEach
    public void setup() {
        cargoDao = new CargoDao();
        containerDao = new ContainerDao();
        paymentDao = new PaymentDao();
    }

    @Test
    public void testScenarioA_ImporterWithAssignedShipments() {
        // Create shipments assigned to Importer A
        String cargoIdA = "CGO-TEST-A1";
        cargoDao.saveCargo(
                cargoIdA,
                "Solar Inverters",
                "Electronics",
                "100",
                "5000",
                "2500000",
                "Port of Mumbai",
                "Cochin Port",
                "High",
                "In Transit"
        );
        Map<String, Object> cargoA = cargoDao.getCargo(cargoIdA);
        cargoA.put("importerName", "Apex Imports Ltd.");
        cargoA.put("importerEmail", "contact@apeximports.com");
        cargoA.put("importerId", "usr_apex_001");

        // Filter for Importer A
        Map<String, Object> importerUserA = new HashMap<>();
        importerUserA.put("uid", "usr_apex_001");
        importerUserA.put("email", "contact@apeximports.com");
        importerUserA.put("name", "Apex Imports Ltd.");

        List<Map<String, Object>> shipmentsA = cargoDao.getShipmentsForImporter(importerUserA);
        Assertions.assertFalse(shipmentsA.isEmpty(), "Importer A should see assigned shipments");
        Assertions.assertTrue(shipmentsA.stream().anyMatch(c -> cargoIdA.equals(c.get("cargoId"))));
    }

    @Test
    public void testScenarioB_ImporterWithNoShipmentsReturnsEmptyList() {
        // Query for an importer with no shipments
        Map<String, Object> newImporter = new HashMap<>();
        newImporter.put("uid", "usr_new_importer_999");
        newImporter.put("email", "newimporter@domain.com");
        newImporter.put("name", "Brand New Importer Corp");

        List<Map<String, Object>> shipments = cargoDao.getShipmentsForImporter(newImporter);
        Assertions.assertNotNull(shipments);
        Assertions.assertTrue(shipments.isEmpty(), "Importer with no shipments must receive an empty list, NEVER all cargos!");

        // Also test query by blank string
        List<Map<String, Object>> blankResult = cargoDao.getShipmentsForImporter("");
        Assertions.assertTrue(blankResult.isEmpty(), "Blank identifier must return empty list");

        List<Map<String, Object>> nullResult = cargoDao.getShipmentsForImporter((String) null);
        Assertions.assertTrue(nullResult.isEmpty(), "Null identifier must return empty list");
    }

    @Test
    public void testScenarioC_StrictDataIsolationBetweenImporters() {
        // Cargo for Importer Alpha
        String cargoAlphaId = "CGO-ALPHA-01";
        cargoDao.saveCargo(cargoAlphaId, "Industrial Pumps", "Machinery", "20", "4000", "1200000", "Port of Mumbai", "Cochin Port", "Normal", "In Transit");
        Map<String, Object> cargoAlpha = cargoDao.getCargo(cargoAlphaId);
        cargoAlpha.put("importerName", "Alpha Logistics");
        cargoAlpha.put("importerEmail", "ops@alphalogistics.com");
        cargoAlpha.put("importerId", "usr_alpha_101");

        // Cargo for Importer Beta
        String cargoBetaId = "CGO-BETA-01";
        cargoDao.saveCargo(cargoBetaId, "Medical Equipment", "Healthcare", "50", "2000", "4500000", "Port of Chennai", "Port of Mumbai", "High", "In Transit");
        Map<String, Object> cargoBeta = cargoDao.getCargo(cargoBetaId);
        cargoBeta.put("importerName", "Beta Healthcare Imports");
        cargoBeta.put("importerEmail", "supply@betamedical.com");
        cargoBeta.put("importerId", "usr_beta_202");

        // Alpha queries
        Map<String, Object> userAlpha = new HashMap<>();
        userAlpha.put("uid", "usr_alpha_101");
        userAlpha.put("email", "ops@alphalogistics.com");
        userAlpha.put("name", "Alpha Logistics");

        List<Map<String, Object>> alphaShipments = cargoDao.getShipmentsForImporter(userAlpha);
        Assertions.assertTrue(alphaShipments.stream().anyMatch(c -> cargoAlphaId.equals(c.get("cargoId"))), "Alpha should see Alpha cargo");
        Assertions.assertFalse(alphaShipments.stream().anyMatch(c -> cargoBetaId.equals(c.get("cargoId"))), "Alpha must NEVER see Beta cargo!");

        // Beta queries
        Map<String, Object> userBeta = new HashMap<>();
        userBeta.put("uid", "usr_beta_202");
        userBeta.put("email", "supply@betamedical.com");
        userBeta.put("name", "Beta Healthcare Imports");

        List<Map<String, Object>> betaShipments = cargoDao.getShipmentsForImporter(userBeta);
        Assertions.assertTrue(betaShipments.stream().anyMatch(c -> cargoBetaId.equals(c.get("cargoId"))), "Beta should see Beta cargo");
        Assertions.assertFalse(betaShipments.stream().anyMatch(c -> cargoAlphaId.equals(c.get("cargoId"))), "Beta must NEVER see Alpha cargo!");
    }

    @Test
    public void testScenarioD_MultipleCargosByExporterOnlyAssignedVisibleToImporter() {
        // Exporter registers 3 cargos with different consignees
        String c1 = "CGO-MULTI-01";
        String c2 = "CGO-MULTI-02";
        String c3 = "CGO-MULTI-03";

        cargoDao.saveCargo(c1, "Wheat Flour", "Agri", "500", "25000", "800000", "Port of Vizag", "Port of Mumbai", "Normal", "In Transit");
        cargoDao.saveCargo(c2, "Sugar Bags", "Agri", "300", "15000", "600000", "Port of Vizag", "Port of Mumbai", "Normal", "In Transit");
        cargoDao.saveCargo(c3, "Basmati Rice", "Agri", "400", "20000", "900000", "Port of Vizag", "Cochin Port", "Normal", "In Transit");

        cargoDao.getCargo(c1).put("consignee", "Target Importer Corp");
        cargoDao.getCargo(c2).put("consignee", "Other Importer Inc");
        cargoDao.getCargo(c3).put("consignee", "Third Party Trading");

        List<Map<String, Object>> targetShipments = cargoDao.getShipmentsForImporter("Target Importer Corp");
        Assertions.assertEquals(1, targetShipments.size());
        Assertions.assertEquals(c1, targetShipments.get(0).get("cargoId"));
    }

    @Test
    public void testScenarioE_DeliveryConfirmationWorkflow() {
        String cargoId = "CGO-DELIV-001";
        cargoDao.saveCargo(cargoId, "Automotive Parts", "Industrial", "150", "8000", "3000000", "Port of Mumbai", "Cochin Port", "Normal", "In Transit");
        cargoDao.getCargo(cargoId).put("importerName", "Delta Motors Importers");

        // Confirm delivery
        boolean confirmed = cargoDao.confirmDelivery(cargoId, "Delta Motors Importers", "Received at Warehouse Bay 3");
        Assertions.assertTrue(confirmed);

        Map<String, Object> updated = cargoDao.getCargo(cargoId);
        Assertions.assertEquals("Delivered", updated.get("status"));
        Assertions.assertEquals("Delivered", updated.get("deliveryStatus"));
        Assertions.assertEquals("Delta Motors Importers", updated.get("deliveredBy"));
        Assertions.assertNotNull(updated.get("deliveredAt"));
    }

    @Test
    public void testScenarioF_InvoiceAndPaymentDataIsolation() {
        // Payment 1 for Importer X
        Map<String, Object> pay1 = new HashMap<>();
        pay1.put("paymentId", "pay_IMP_X_001");
        pay1.put("cargoId", "CGO-PAY-01");
        pay1.put("importerName", "Importer X Trading");
        pay1.put("importerEmail", "billing@importerx.com");
        pay1.put("amount", 75000.0);
        pay1.put("paymentStatus", "PAID");
        paymentDao.savePayment(pay1);

        // Payment 2 for Importer Y
        Map<String, Object> pay2 = new HashMap<>();
        pay2.put("paymentId", "pay_IMP_Y_002");
        pay2.put("cargoId", "CGO-PAY-02");
        pay2.put("importerName", "Importer Y Corp");
        pay2.put("importerEmail", "accounts@importery.com");
        pay2.put("amount", 120000.0);
        pay2.put("paymentStatus", "PAID");
        paymentDao.savePayment(pay2);

        // Query for Importer X
        List<Map<String, Object>> xPayments = paymentDao.getPaymentsForImporter("Importer X Trading");
        Assertions.assertTrue(xPayments.stream().anyMatch(p -> "pay_IMP_X_001".equals(p.get("paymentId"))));
        Assertions.assertFalse(xPayments.stream().anyMatch(p -> "pay_IMP_Y_002".equals(p.get("paymentId"))));

        // Query for unknown importer -> returns empty list, never all payments
        List<Map<String, Object>> emptyPayments = paymentDao.getPaymentsForImporter("Unknown Importer Nowhere");
        Assertions.assertTrue(emptyPayments.isEmpty(), "Unknown importer must receive empty payment list");
    }
}
