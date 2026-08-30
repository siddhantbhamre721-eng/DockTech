package com.projectui.service;

public class VesselPositionValidator {

    /**
     * Validates whether a given latitude/longitude represents a valid maritime/water position.
     * Returns false if the coordinate falls on inland landmass.
     */
    public static boolean isWaterCoordinate(double lat, double lng) {
        if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) return false;
        if (lat == 0.0 && lng == 0.0) return false;

        // Specific Port Harbors & Coastal Approaches (Valid Water - Always Preserve):
        // Mumbai Harbor / JNPT: 18.82 - 18.99° N, 72.70 - 72.96° E
        if (lat >= 18.82 && lat <= 18.99 && lng >= 72.70 && lng <= 72.96) return true;
        // Mormugao / Goa Harbor & Approach: 15.20 - 15.60° N, 73.50 - 73.85° E
        if (lat >= 15.20 && lat <= 15.60 && lng >= 73.50 && lng <= 73.85) return true;
        // Mangalore Harbor & Approach: 12.75 - 13.05° N, 74.50 - 74.85° E
        if (lat >= 12.75 && lat <= 13.05 && lng >= 74.50 && lng <= 74.85) return true;
        // Kochi / Cochin Harbor & Coastal Approach: 9.00 - 10.50° N, 75.00 - 76.28° E
        if (lat >= 9.00 && lat <= 10.50 && lng >= 75.00 && lng <= 76.28) return true;
        // Tuticorin Harbor: 8.70 - 8.85° N, 78.15 - 78.30° E
        if (lat >= 8.70 && lat <= 8.85 && lng >= 78.15 && lng <= 78.30) return true;
        // Chennai Port Approach: 13.05 - 13.15° N, 80.25 - 80.40° E
        if (lat >= 13.05 && lat <= 13.15 && lng >= 80.25 && lng <= 80.40) return true;
        // Visakhapatnam / Vizag Port Approach: 17.65 - 17.75° N, 83.22 - 83.40° E
        if (lat >= 17.65 && lat <= 17.75 && lng >= 83.22 && lng <= 83.40) return true;
        // Paradip Port Approach: 20.20 - 20.35° N, 86.65 - 86.85° E
        if (lat >= 20.20 && lat <= 20.35 && lng >= 86.65 && lng <= 86.85) return true;
        // Haldia River Channel / Kolkata approach: 21.50 - 22.10° N, 87.80 - 88.25° E
        if (lat >= 21.50 && lat <= 22.10 && lng >= 87.80 && lng <= 88.25) return true;
        // Gulf of Kutch water: 22.3-22.9°N, 69.0-70.3°E
        if (lat >= 22.3 && lat <= 22.9 && lng >= 69.0 && lng <= 70.3) return true;
        // Gulf of Khambhat water: 20.5-21.8°N, 72.1-72.7°E
        if (lat >= 20.5 && lat <= 21.8 && lng >= 72.1 && lng <= 72.7) return true;

        // Strict Inland Landmass Checks for Indian Subcontinent (Return false for all inland areas):
        // 1. Northern & Central Inland (lat >= 22.5 N):
        if (lat >= 22.5 && lat <= 38.0 && lng >= 68.0 && lng <= 95.0) {
            return false;
        }

        // 2. Gujarat / Maharashtra / Odisha (lat 20.0 to 22.5 N):
        if (lat >= 20.0 && lat < 22.5) {
            if (lng >= 69.6 && lng <= 72.1) return false; // Saurashtra peninsula
            if (lng >= 73.0 && lng <= 87.2) return false; // Central India (MH, MP, CG, OD)
            if (lng > 87.2 && lng < 88.0 && lat > 21.5) return false; // Inland West Bengal
        }

        // 3. Maharashtra / Telangana / Andhra / Odisha (lat 17.5 to 20.0 N):
        if (lat >= 17.5 && lat < 20.0) {
            double eastCoastLng = 83.2 + (lat - 17.5) * 1.38;
            if (lng >= 73.0 && lng < eastCoastLng) return false;
        }

        // 4. Goa / Karnataka / Andhra Pradesh (lat 14.5 to 17.5 N):
        if (lat >= 14.5 && lat < 17.5) {
            double eastCoastLng = 80.2 + (lat - 14.5) * 0.85;
            if (lng >= 73.5 && lng < eastCoastLng) return false;
        }

        // 5. Karnataka / Tamil Nadu / Andhra (lat 11.5 to 14.5 N):
        if (lat >= 11.5 && lat < 14.5) {
            if (lng >= 74.8 && lng < 80.25) return false;
        }

        // 6. Kerala / Tamil Nadu / Southern Tip (lat 8.0 to 11.5 N):
        if (lat >= 8.0 && lat < 11.5) {
            if (lng >= 76.5 && lng <= 79.8) return false;
        }

        // 7. Sri Lanka Landmass:
        if (lat >= 5.8 && lat <= 9.8 && lng >= 79.6 && lng <= 81.9) {
            return false;
        }

        // Valid Maritime Water Zones:
        // Arabian Sea:
        if (lat >= -10.0 && lat <= 25.0 && lng >= 45.0 && lng <= 77.5) {
            return true;
        }

        // Bay of Bengal & Andaman Sea:
        if (lat >= -10.0 && lat <= 22.5 && lng >= 80.2 && lng <= 98.0) {
            return true;
        }

        // Southern Indian Ocean:
        if (lat >= -35.0 && lat < 8.0 && lng >= 45.0 && lng <= 105.0) {
            return true;
        }

        return false;
    }

    /**
     * Returns a valid fallback maritime water position for a vessel if its initial position falls on land.
     */
    public static double[] getFallbackWaterPosition(long mmsi, String destination) {
        String dest = destination != null ? destination.toLowerCase() : "";
        if (dest.contains("chennai") || dest.contains("vizag") || dest.contains("kolkata") || dest.contains("paradip") || dest.contains("bengal")) {
            // Bay of Bengal water position
            double lat = 10.0 + (Math.abs(mmsi * 17) % 900) / 100.0; // 10.0 to 19.0° N
            double lng = 83.5 + (Math.abs(mmsi * 31) % 900) / 100.0; // 83.5 to 92.5° E
            return new double[]{lat, lng};
        } else if (dest.contains("cochin") || dest.contains("kochi")) {
            // Arabian Sea - Cochin / Malabar Coast Approach
            double lat = 9.40 + (Math.abs(mmsi * 13) % 90) / 100.0; // 9.40 to 10.30° N
            double lng = 75.40 + (Math.abs(mmsi * 29) % 80) / 100.0; // 75.40 to 76.20° E
            return new double[]{lat, lng};
        } else {
            // Arabian Sea water position
            double lat = 9.0 + (Math.abs(mmsi * 13) % 1100) / 100.0; // 9.0 to 20.0° N
            double lng = 63.0 + (Math.abs(mmsi * 29) % 850) / 100.0; // 63.0 to 71.5° E
            return new double[]{lat, lng};
        }
    }
}
