package com.projectui;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.projectui.model.Ship;
import com.projectui.service.VesselData;
import com.projectui.view.admin.AdminDataStore;
import com.projectui.view.admin.AdminDataStore.Port;

public class PortVesselAndBerthCountTest {

    @Test
    public void testPortBerthCalculationZeroOccupied() {
        Port p = new Port("PORT OF GOA", "Goa, India", 12, 0, "Operational", "Mixed", "85%", "Clear", "Just now");
        assertEquals(0, p.occupiedBerths(), "Empty port should have 0 occupied berths");
        assertEquals(12, p.availableBerths(), "Empty port with 12 berths should have 12 available");
    }

    @Test
    public void testPortBerthCalculationNonZeroOccupied() {
        Port p = new Port("PORT OF MUMBAI", "Mumbai, Maharashtra", 12, 5, "Operational", "Mixed", "85%", "Clear", "Just now");
        p.occupiedBerths = 3;
        assertEquals(3, p.occupiedBerths(), "Should accurately report 3 occupied berths");
        assertEquals(9, p.availableBerths(), "Should accurately report 9 available berths");
    }

    @Test
    public void testMatchesPortIdentifier() {
        Port p = new Port("PORT OF MUMBAI", "Mumbai, Maharashtra, India", "IN-MUM-86", "PA", 12, 0, "Operational", "Mixed", "85%", "Clear", "Just now", 18.9439, 72.8354, "APPROVED");

        assertTrue(AdminDataStore.matchesPortIdentifier("Port of Mumbai", p));
        assertTrue(AdminDataStore.matchesPortIdentifier("PORT OF MUMBAI", p));
        assertTrue(AdminDataStore.matchesPortIdentifier("Mumbai", p));
        assertTrue(AdminDataStore.matchesPortIdentifier("JNPT / Nhava Sheva", p));
        assertTrue(AdminDataStore.matchesPortIdentifier("IN-MUM-86", p));

        assertFalse(AdminDataStore.matchesPortIdentifier("Port of Chennai", p));
        assertFalse(AdminDataStore.matchesPortIdentifier("Goa Port", p));
    }

    @Test
    public void testIsShipBerthedDetection() {
        Ship s1 = new Ship("S1", "Vessel A", "Container", "912345", "Line A", "10:00", "Port of Mumbai", "Berth 4", "At Anchor");
        assertTrue(AdminDataStore.isShipBerthed(s1), "Ship with 'Berth 4' should be counted as occupying a berth");

        Ship s2 = new Ship("S2", "Vessel B", "Container", "912346", "Line B", "10:00", "Port of Mumbai", "Not Assigned", "Berthed");
        assertTrue(AdminDataStore.isShipBerthed(s2), "Ship with status 'Berthed' should be counted as occupying a berth");

        Ship s3 = new Ship("S3", "Vessel C", "Container", "912347", "Line C", "10:00", "Port of Mumbai", "Not Assigned", "Underway");
        assertFalse(AdminDataStore.isShipBerthed(s3), "Ship underway without berth allocation should not be berthed");
    }

    @Test
    public void testLiveVesselAssociationAndBerthing() {
        Port p = new Port("PORT OF MUMBAI", "Mumbai", "IN-MUM-86", "PA", 12, 0, "Operational", "Mixed", "85%", "Clear", "Just now", 18.9439, 72.8354, "APPROVED");

        VesselData v1 = new VesselData(123456789L, "911111", "MV Mumbai Trader", "V123", 18.9100, 72.7800, 0.1, 90.0, 90.0, "Port of Mumbai", "Moored", "Cargo", "12:00");
        assertTrue(AdminDataStore.isLiveVesselAssociatedWithPort(v1, p));
        assertTrue(AdminDataStore.isLiveVesselBerthed(v1, p));

        VesselData v2 = new VesselData(987654321L, "922222", "MV Atlantic Star", "V987", 13.0800, 80.2700, 14.5, 90.0, 90.0, "Port of Chennai", "Underway", "Cargo", "12:00");
        assertFalse(AdminDataStore.isLiveVesselAssociatedWithPort(v2, p));
    }
}
