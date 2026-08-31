package com.projectui;

import com.projectui.dao.PortRequestDao;
import com.projectui.view.admin.AdminDataStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortRequestFlowValidationTest {

    @Test
    public void testNoAutomaticOrSeedRequestsOnStartup() {
        // 1. Verify static stores do not contain default hardcoded demo requests like NPR-101
        for (AdminDataStore.PortRequest req : AdminDataStore.getInstance().portRequests) {
            Assertions.assertFalse("NPR-101".equalsIgnoreCase(req.requestId), "NPR-101 seed request must not exist in AdminDataStore");
            Assertions.assertFalse(PortRequestDao.isFakeOrTestRequest(req), "No fake/test request should be in AdminDataStore on startup: " + req.portName);
        }

        for (Map<String, Object> reqData : PortRequestDao.getLocalRequestStore().values()) {
            Assertions.assertFalse("NPR-101".equalsIgnoreCase((String) reqData.get("requestId")), "NPR-101 seed request must not exist in localRequestStore");
            Assertions.assertFalse(PortRequestDao.isFakeOrTestRequest(reqData, (String) reqData.get("requestId")), "No fake/test request should be in localRequestStore on startup");
        }
    }

    @Test
    public void testFakeRequestIdentificationAndPreservationOfRealRequests() {
        // Test fake request patterns
        Map<String, Object> fakeMangalore = new HashMap<>();
        fakeMangalore.put("requestId", "REQ-1234");
        fakeMangalore.put("portName", "Port of Mangalore International");
        fakeMangalore.put("requesterEmail", "test_director_ramesh@gmail.com");
        fakeMangalore.put("proposedPaEmail", "test_pa_mangalore@docktech.com");
        Assertions.assertTrue(PortRequestDao.isFakeOrTestRequest(fakeMangalore, "REQ-1234"), "Mangalore test request must be identified as fake");

        Map<String, Object> fakeHaziraSeed = new HashMap<>();
        fakeHaziraSeed.put("requestId", "NPR-101");
        fakeHaziraSeed.put("portName", "Port of Hazira");
        fakeHaziraSeed.put("portCode", "IN-HAZ-01");
        Assertions.assertTrue(PortRequestDao.isFakeOrTestRequest(fakeHaziraSeed, "NPR-101"), "NPR-101 Hazira seed request must be identified as fake");

        Map<String, Object> fakePrefixTest = new HashMap<>();
        fakePrefixTest.put("requestId", "TEST-NPR-999");
        fakePrefixTest.put("portName", "Port of Test Verif");
        Assertions.assertTrue(PortRequestDao.isFakeOrTestRequest(fakePrefixTest, "TEST-NPR-999"), "TEST- prefix request must be identified as fake");

        // Test REAL request (PORT OF MUMBAI) - MUST NOT be identified as fake
        Map<String, Object> realMumbai = new HashMap<>();
        realMumbai.put("requestId", "NPR-8821");
        realMumbai.put("portName", "Port of Mumbai");
        realMumbai.put("portCode", "IN-BOM-01");
        realMumbai.put("location", "Mumbai, Maharashtra");
        realMumbai.put("address", "Mumbai Port Trust, Ballard Estate");
        realMumbai.put("requesterName", "Siddhant Bhamre");
        realMumbai.put("requesterEmail", "siddhant@docktech.com");
        realMumbai.put("proposedPaName", "Mumbai Port Authority");
        realMumbai.put("proposedPaEmail", "pa_mumbai_authority@docktech.com");
        Assertions.assertFalse(PortRequestDao.isFakeOrTestRequest(realMumbai, "NPR-8821"), "Real user submitted Mumbai request must NOT be identified as fake");
    }

    @Test
    public void testSingleSourceOfTruthAndNoDuplicatesOnSync() throws Exception {
        PortRequestDao portRequestDao = new PortRequestDao();

        // 1. Sync with database
        List<AdminDataStore.PortRequest> synced = portRequestDao.syncWithDatabase().get();
        Assertions.assertNotNull(synced);

        // 2. Ensure every request in synced list is genuine and unique
        java.util.Set<String> seenIds = new java.util.HashSet<>();
        for (AdminDataStore.PortRequest req : synced) {
            Assertions.assertFalse(PortRequestDao.isFakeOrTestRequest(req), "Synced list must not contain fake/test requests: " + req.portName + " (" + req.requestId + ")");
            Assertions.assertTrue(seenIds.add(req.requestId.toLowerCase()), "Synced list must not contain duplicate requests for requestId: " + req.requestId);
        }

        // 3. Ensure syncAdminDataStore maintains consistency without reviving deleted or fake requests
        PortRequestDao.syncAdminDataStore(synced);
        Assertions.assertEquals(synced.size(), AdminDataStore.getInstance().portRequests.size());
    }
}
