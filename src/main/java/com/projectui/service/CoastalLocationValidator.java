package com.projectui.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Geographic validation service for verifying whether coordinates or locations
 * are situated on or in immediate proximity to a coastal or sea-accessible maritime area.
 */
public class CoastalLocationValidator {

    public static final double MAX_COASTAL_DISTANCE_KM = 55.0; // Max permissible distance from sea / coastline for a port
    public static final String REJECTION_MESSAGE = "Invalid port location. Ports can only be registered in coastal or sea-accessible areas.";

    public static class GeoPoint {
        public final double lat;
        public final double lng;

        public GeoPoint(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    public static class ValidationResult {
        private final boolean valid;
        private final double distanceKm;
        private final String message;

        public ValidationResult(boolean valid, double distanceKm, String message) {
            this.valid = valid;
            this.distanceKm = distanceKm;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public double getDistanceKm() { return distanceKm; }
        public String getMessage() { return message; }
    }

    // High-resolution continuous maritime shoreline nodes (India and major global sea basins)
    private static final List<GeoPoint> COASTLINE_NODES = new ArrayList<>();

    static {
        // 1. Gujarat Coastline (Gulf of Kutch, Saurashtra, Gulf of Khambhat, South Gujarat)
        addNode(23.71, 68.18); // Kori Creek / Sir Creek
        addNode(23.40, 68.45);
        addNode(23.15, 68.75); // Jakhau
        addNode(22.82, 69.35); // Mandvi
        addNode(22.83, 69.70); // Mundra
        addNode(23.00, 70.13); // Kandla / Gandhidham
        addNode(22.75, 70.45); // Navlakhi
        addNode(22.50, 69.80); // Bedi / Jamnagar
        addNode(22.35, 69.05); // Okha / Dwarka
        addNode(21.62, 69.60); // Porbandar
        addNode(20.90, 70.36); // Veraval / Somnath
        addNode(20.71, 70.98); // Diu
        addNode(20.88, 71.49); // Pipavav / Jafrabad
        addNode(21.48, 72.18); // Alang
        addNode(21.76, 72.15); // Bhavnagar
        addNode(22.18, 72.35); // Khambhat
        addNode(21.65, 72.58); // Dahej
        addNode(21.11, 72.63); // Hazira / Surat
        addNode(20.60, 72.85); // Valsad
        addNode(20.40, 72.83); // Daman
        addNode(20.12, 72.75); // Umargam

        // 2. Maharashtra & Goa Coastline
        addNode(19.98, 72.72); // Dahanu
        addNode(19.69, 72.75); // Tarapur
        addNode(19.38, 72.80); // Vasai / Palghar
        addNode(19.18, 72.82); // Mumbai North / Versova
        addNode(18.95, 72.82); // Mumbai Harbour / Port of Mumbai
        addNode(18.95, 72.95); // JNPT / Navi Mumbai
        addNode(18.64, 72.87); // Alibaug
        addNode(18.28, 72.99); // Revdanda / Murud
        addNode(18.18, 73.01); // Dighi Port
        addNode(17.98, 73.05); // Shrivardhan
        addNode(17.50, 73.18); // Dabhol / Guhagar
        addNode(17.00, 73.28); // Ratnagiri / Jaigad
        addNode(16.50, 73.33); // Vijaydurg
        addNode(16.05, 73.47); // Malvan
        addNode(15.75, 73.68); // Vengurla
        addNode(15.50, 73.76); // North Goa / Panaji
        addNode(15.39, 73.80); // Mormugao Port / Vasco da Gama

        // 3. Karnataka Coastline
        addNode(14.81, 74.13); // Karwar
        addNode(14.53, 74.31); // Ankola
        addNode(14.43, 74.39); // Kumta
        addNode(14.28, 74.44); // Honnavar
        addNode(13.98, 74.55); // Bhatkal
        addNode(13.62, 74.69); // Kundapura / Gangolli
        addNode(13.35, 74.70); // Malpe / Udupi
        addNode(12.87, 74.84); // New Mangalore Port / Mangalore

        // 4. Kerala Coastline
        addNode(12.50, 74.98); // Kasaragod
        addNode(11.87, 75.36); // Kannur / Azhikkal
        addNode(11.75, 75.49); // Thalassery
        addNode(11.25, 75.77); // Kozhikode / Beypore
        addNode(10.79, 75.92); // Ponnani
        addNode(10.20, 76.19); // Azhikode
        addNode(9.93, 76.26);  // Cochin Port / Kochi / Vallarpadam
        addNode(9.50, 76.32);  // Alappuzha
        addNode(8.88, 76.58);  // Kollam
        addNode(8.48, 76.95);  // Thiruvananthapuram / Vizhinjam

        // 5. Tamil Nadu Coastline (Kanyakumari to Chennai)
        addNode(8.08, 77.55);  // Kanyakumari / Cape Comorin
        addNode(8.35, 77.92);  // Tiruchendur
        addNode(8.76, 78.13);  // V.O. Chidambaranar Port / Tuticorin
        addNode(9.15, 78.85);  // Kilakarai
        addNode(9.28, 79.13);  // Ramanathapuram
        addNode(9.28, 79.31);  // Rameswaram
        addNode(9.88, 79.05);  // Tondi
        addNode(10.35, 79.35); // Muthupet
        addNode(10.76, 79.84); // Nagapattinam / Karaikal
        addNode(11.15, 79.85); // Poompuhar / Tarangambadi
        addNode(11.50, 79.77); // Cuddalore
        addNode(11.93, 79.83); // Puducherry
        addNode(12.50, 80.15); // Mahabalipuram
        addNode(12.83, 80.24); // Kovalam / South Chennai
        addNode(13.08, 80.28); // Chennai Port / Chennai
        addNode(13.26, 80.33); // Kamarajar Port / Ennore
        addNode(13.43, 80.31); // Kattupalli Port

        // 6. Andhra Pradesh Coastline
        addNode(13.98, 80.15); // Dugarajapatnam / Pulicat
        addNode(14.25, 80.13); // Krishnapatnam Port
        addNode(15.00, 80.05); // Kavali
        addNode(15.50, 80.05); // Ongole / Vadarevu
        addNode(15.82, 80.57); // Nizampatnam
        addNode(16.18, 81.14); // Machilipatnam
        addNode(16.50, 81.70); // Antarvedi
        addNode(16.98, 82.25); // Kakinada Port
        addNode(17.38, 82.85); // Payakaraopeta
        addNode(17.68, 83.21); // Visakhapatnam Port / Gangavaram
        addNode(18.25, 83.90); // Kalingapatnam
        addNode(18.80, 84.40); // Bhavanapadu
        addNode(19.10, 84.80); // Baruva

        // 7. Odisha & West Bengal Coastline
        addNode(19.30, 85.00); // Gopalpur Port
        addNode(19.80, 85.82); // Puri
        addNode(19.98, 86.30); // Konark
        addNode(20.31, 86.61); // Paradip Port
        addNode(20.80, 86.95); // Dhamra Port
        addNode(21.48, 87.05); // Chandipur / Balasore
        addNode(21.62, 87.52); // Digha
        addNode(21.80, 87.95); // Sagar Island / Kakdwip
        addNode(22.06, 88.06); // Haldia Port / Hooghly Estuary
        addNode(22.35, 88.15); // Diamond Harbour
        addNode(22.55, 88.35); // Syama Prasad Mookerjee Port / Kolkata Dock System
        addNode(21.70, 88.80); // Bakkhali / Sundarbans
        addNode(21.60, 89.15); // Bangladesh Border / Raimangal

        // 8. Indian Islands (Andaman, Nicobar, Lakshadweep)
        addNode(11.66, 92.73); // Port Blair
        addNode(12.92, 92.90); // Diglipur
        addNode(9.15, 92.80);  // Car Nicobar
        addNode(7.00, 93.80);  // Great Nicobar / Campbell Bay
        addNode(10.57, 72.64); // Kavaratti
        addNode(11.20, 72.80); // Amini / Agatti
        addNode(8.30, 73.04);  // Minicoy

        // 9. Major International Maritime Port Basins (Middle East, SE Asia, Europe, Americas)
        addNode(25.27, 55.30); // Dubai / Jebel Ali
        addNode(24.47, 54.37); // Abu Dhabi
        addNode(26.22, 50.58); // Bahrain
        addNode(25.28, 51.53); // Doha
        addNode(29.37, 47.98); // Kuwait City
        addNode(23.61, 58.59); // Muscat / Salalah (17.01, 54.09)
        addNode(17.01, 54.09); // Salalah
        addNode(6.93, 79.85);  // Colombo Port
        addNode(6.12, 81.12);  // Hambantota
        addNode(1.35, 103.82); // Singapore Port
        addNode(3.00, 101.40); // Port Klang
        addNode(13.08, 100.90);// Laem Chabang / Bangkok
        addNode(22.30, 114.17);// Hong Kong
        addNode(31.23, 121.50);// Shanghai
        addNode(35.10, 129.04);// Busan
        addNode(35.68, 139.77);// Tokyo / Yokohama
        addNode(51.92, 4.48);  // Rotterdam
        addNode(51.22, 4.40);  // Antwerp
        addNode(53.55, 9.99);  // Hamburg
        addNode(43.30, 5.37);  // Marseille
        addNode(41.38, 2.18);  // Barcelona
        addNode(38.72, -9.14); // Lisbon
        addNode(50.90, -1.40); // Southampton
        addNode(40.71, -74.00);// New York / New Jersey
        addNode(33.74, -118.26);// Los Angeles / Long Beach
        addNode(29.76, -95.36);// Houston
        addNode(25.76, -80.19);// Miami
    }

    private static void addNode(double lat, double lng) {
        COASTLINE_NODES.add(new GeoPoint(lat, lng));
    }

    /**
     * Calculates the great-circle distance between two geographic coordinates in kilometers.
     */
    public static double haversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth's mean radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return R * c;
    }

    /**
     * Calculates the minimum distance from target coordinates to the nearest coastal segment or node.
     */
    public static double getDistanceToNearestCoastKm(double lat, double lng) {
        double minDistance = Double.MAX_VALUE;

        // 1. Direct distance to nodes
        for (int i = 0; i < COASTLINE_NODES.size(); i++) {
            GeoPoint p = COASTLINE_NODES.get(i);
            double dist = haversineDistanceKm(lat, lng, p.lat, p.lng);
            if (dist < minDistance) {
                minDistance = dist;
            }

            // 2. Interpolate segments between consecutive nodes in the same coastal sector
            if (i < COASTLINE_NODES.size() - 1) {
                GeoPoint next = COASTLINE_NODES.get(i + 1);
                double nodeGap = haversineDistanceKm(p.lat, p.lng, next.lat, next.lng);
                // If consecutive nodes are within 250km (continuous coastline segment)
                if (nodeGap < 250.0) {
                    int steps = Math.max(3, (int)(nodeGap / 20.0));
                    for (int s = 1; s < steps; s++) {
                        double f = (double) s / steps;
                        double midLat = p.lat + f * (next.lat - p.lat);
                        double midLng = p.lng + f * (next.lng - p.lng);
                        double midDist = haversineDistanceKm(lat, lng, midLat, midLng);
                        if (midDist < minDistance) {
                            minDistance = midDist;
                        }
                    }
                }
            }
        }
        return minDistance;
    }

    /**
     * Verifies whether target coordinates are located in a coastal/sea-accessible location.
     */
    public static boolean isCoastal(double lat, double lng) {
        // Reject invalid / impossible coordinates
        if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
            return false;
        }
        double distKm = getDistanceToNearestCoastKm(lat, lng);
        return distKm <= MAX_COASTAL_DISTANCE_KM;
    }

    /**
     * Comprehensive validation checking geographic coordinates and distance to coastline.
     */
    public static ValidationResult validateLocation(double lat, double lng) {
        if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
            return new ValidationResult(false, Double.MAX_VALUE, "Invalid coordinates range.");
        }

        double dist = getDistanceToNearestCoastKm(lat, lng);
        if (dist <= MAX_COASTAL_DISTANCE_KM) {
            return new ValidationResult(true, dist, String.format(java.util.Locale.US, "Valid coastal location (%.1f km from nearest sea/coastline).", dist));
        } else {
            return new ValidationResult(false, dist, REJECTION_MESSAGE);
        }
    }
}
