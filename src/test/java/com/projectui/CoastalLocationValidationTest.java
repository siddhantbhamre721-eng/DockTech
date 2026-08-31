package com.projectui;

import com.projectui.service.CoastalLocationValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CoastalLocationValidationTest {

    @Test
    public void testLandlockedLocationsAreRejected() {
        // 1. Pune (18.5214, 73.8545) - Landlocked inland city (~100km from coast)
        double puneLat = 18.5214;
        double puneLng = 73.8545;
        Assertions.assertFalse(CoastalLocationValidator.isCoastal(puneLat, puneLng),
                "Pune must be rejected as an inland landlocked location");
        CoastalLocationValidator.ValidationResult puneResult = CoastalLocationValidator.validateLocation(puneLat, puneLng);
        Assertions.assertFalse(puneResult.isValid(), "Pune validation result must be invalid");
        Assertions.assertEquals(CoastalLocationValidator.REJECTION_MESSAGE, puneResult.getMessage());
        Assertions.assertTrue(puneResult.getDistanceKm() > CoastalLocationValidator.MAX_COASTAL_DISTANCE_KM);

        // 2. Bengaluru / Bangalore (12.9716, 77.5946)
        Assertions.assertFalse(CoastalLocationValidator.isCoastal(12.9716, 77.5946),
                "Bengaluru must be rejected as an inland landlocked location");

        // 3. Hyderabad (17.3850, 78.4867)
        Assertions.assertFalse(CoastalLocationValidator.isCoastal(17.3850, 78.4867),
                "Hyderabad must be rejected as an inland landlocked location");

        // 4. Delhi (28.6139, 77.2090)
        Assertions.assertFalse(CoastalLocationValidator.isCoastal(28.6139, 77.2090),
                "Delhi must be rejected as an inland landlocked location");

        // 5. Nagpur (21.1458, 79.0882)
        Assertions.assertFalse(CoastalLocationValidator.isCoastal(21.1458, 79.0882),
                "Nagpur must be rejected as an inland landlocked location");

        // 6. Jaipur (26.9124, 75.7873)
        Assertions.assertFalse(CoastalLocationValidator.isCoastal(26.9124, 75.7873),
                "Jaipur must be rejected as an inland landlocked location");

        // 7. Bhopal (23.2599, 77.4126)
        Assertions.assertFalse(CoastalLocationValidator.isCoastal(23.2599, 77.4126),
                "Bhopal must be rejected as an inland landlocked location");
    }

    @Test
    public void testCoastalPortLocationsAreAccepted() {
        // 1. Mumbai (18.9220, 72.8347)
        Assertions.assertTrue(CoastalLocationValidator.isCoastal(18.9220, 72.8347),
                "Mumbai must be accepted as a coastal port location");

        // 2. Chennai (13.0827, 80.2707)
        Assertions.assertTrue(CoastalLocationValidator.isCoastal(13.0827, 80.2707),
                "Chennai must be accepted as a coastal port location");

        // 3. Visakhapatnam (17.6868, 83.2185)
        Assertions.assertTrue(CoastalLocationValidator.isCoastal(17.6868, 83.2185),
                "Visakhapatnam must be accepted as a coastal port location");

        // 4. Kochi (9.9312, 76.2673)
        Assertions.assertTrue(CoastalLocationValidator.isCoastal(9.9312, 76.2673),
                "Kochi must be accepted as a coastal port location");

        // 5. Goa / Mormugao (15.3991, 73.8016)
        Assertions.assertTrue(CoastalLocationValidator.isCoastal(15.3991, 73.8016),
                "Goa must be accepted as a coastal port location");

        // 6. Mangalore (12.8700, 74.8400)
        Assertions.assertTrue(CoastalLocationValidator.isCoastal(12.8700, 74.8400),
                "Mangalore must be accepted as a coastal port location");

        // 7. Kandla (23.0033, 70.1333)
        Assertions.assertTrue(CoastalLocationValidator.isCoastal(23.0033, 70.1333),
                "Kandla must be accepted as a coastal port location");

        // 8. Paradip (20.3167, 86.6167)
        Assertions.assertTrue(CoastalLocationValidator.isCoastal(20.3167, 86.6167),
                "Paradip must be accepted as a coastal port location");

        // 9. Hazira / Surat Coast (21.1126, 72.6369)
        Assertions.assertTrue(CoastalLocationValidator.isCoastal(21.1126, 72.6369),
                "Hazira must be accepted as a coastal port location");

        // 10. Tuticorin (8.7642, 78.1348)
        Assertions.assertTrue(CoastalLocationValidator.isCoastal(8.7642, 78.1348),
                "Tuticorin must be accepted as a coastal port location");
    }

    @Test
    public void testInternationalMajorPortsAreAccepted() {
        // Dubai (25.276987, 55.296249)
        Assertions.assertTrue(CoastalLocationValidator.isCoastal(25.276987, 55.296249),
                "Dubai must be accepted as a coastal port location");

        // Singapore (1.3521, 103.8198)
        Assertions.assertTrue(CoastalLocationValidator.isCoastal(1.3521, 103.8198),
                "Singapore must be accepted as a coastal port location");

        // Colombo (6.9271, 79.8612)
        Assertions.assertTrue(CoastalLocationValidator.isCoastal(6.9271, 79.8612),
                "Colombo must be accepted as a coastal port location");

        // Rotterdam (51.9244, 4.4777)
        Assertions.assertTrue(CoastalLocationValidator.isCoastal(51.9244, 4.4777),
                "Rotterdam must be accepted as a coastal port location");
    }

    @Test
    public void testInvalidCoordinatesBoundsAreRejected() {
        Assertions.assertFalse(CoastalLocationValidator.isCoastal(95.0, 72.0), "Latitude > 90 must be invalid");
        Assertions.assertFalse(CoastalLocationValidator.isCoastal(-95.0, 72.0), "Latitude < -90 must be invalid");
        Assertions.assertFalse(CoastalLocationValidator.isCoastal(18.0, 195.0), "Longitude > 180 must be invalid");
        Assertions.assertFalse(CoastalLocationValidator.isCoastal(18.0, -195.0), "Longitude < -180 must be invalid");
    }
}
