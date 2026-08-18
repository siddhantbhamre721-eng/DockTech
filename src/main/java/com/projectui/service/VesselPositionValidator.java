package com.projectui.service;

public class VesselPositionValidator {

    /**
     * Validates whether a given latitude/longitude represents a valid maritime/water position.
     * Returns false if the coordinate falls on inland landmass.
     */
    public static boolean isWaterCoordinate(double lat, double lng) {
        if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) return false;

        // Open Sea / Ocean Zones around South Asia / Indian Ocean
        // Arabian Sea: West of 72.5°E (or south of 20°N west of 72.8°E)
        if (lng >= 50.0 && lng <= 72.5 && lat >= 0.0 && lat <= 25.0) {
            // Inland Gujarat check
            if (lat > 22.0 && lng > 70.8 && lng < 72.5) {
                // Gulf of Kutch water: 22.3-22.9°N, 69.0-70.5°E
                if (lat >= 22.3 && lat <= 22.9 && lng >= 69.0 && lng <= 70.5) return true;
                return false; // Inland Gujarat land
            }
            return true;
        }

        // Gulf of Khambhat water: 20.5-22.0°N, 72.0-72.7°E
        if (lat >= 20.5 && lat <= 22.0 && lng >= 72.0 && lng <= 72.7) return true;

        // Bay of Bengal: East of 81.5°E
        if (lng >= 81.5 && lng <= 98.0 && lat >= 0.0 && lat <= 22.0) {
            if (lat > 20.5 && lng < 86.5) return false; // Inland Odisha
            if (lat > 22.0) return false; // Inland West Bengal / Bangladesh
            return true;
        }

        // Deep Southern Indian Ocean below 7.5°N
        if (lat >= -30.0 && lat <= 7.5 && lng >= 55.0 && lng <= 100.0) return true;

        // Specific Port Harbors & Coastal Approaches (Valid Water):
        // Mumbai Harbor: 18.82 - 18.98° N, 72.72 - 72.88° E
        if (lat >= 18.82 && lat <= 18.98 && lng >= 72.72 && lng <= 72.88) return true;
        // JNPT Harbor: 18.90 - 18.97° N, 72.89 - 72.96° E
        if (lat >= 18.90 && lat <= 18.97 && lng >= 72.89 && lng <= 72.96) return true;
        // Mormugao / Goa Harbor: 15.35 - 15.45° N, 73.70 - 73.82° E
        if (lat >= 15.35 && lat <= 15.45 && lng >= 73.70 && lng <= 73.82) return true;
        // Mangalore Harbor: 12.85 - 12.95° N, 74.72 - 74.82° E
        if (lat >= 12.85 && lat <= 12.95 && lng >= 74.72 && lng <= 74.82) return true;
        // Kochi Harbor / Approach: 9.90 - 10.02° N, 76.15 - 76.26° E
        if (lat >= 9.90 && lat <= 10.02 && lng >= 76.15 && lng <= 76.26) return true;
        // Tuticorin Harbor: 8.70 - 8.80° N, 78.15 - 78.25° E
        if (lat >= 8.70 && lat <= 8.80 && lng >= 78.15 && lng <= 78.25) return true;
        // Chennai Port Approach: 13.05 - 13.15° N, 80.28 - 80.38° E
        if (lat >= 13.05 && lat <= 13.15 && lng >= 80.28 && lng <= 80.38) return true;
        // Visakhapatnam Port Approach: 17.65 - 17.72° N, 83.26 - 83.36° E
        if (lat >= 17.65 && lat <= 17.72 && lng >= 83.26 && lng <= 83.36) return true;
        // Paradip Port Approach: 20.20 - 20.30° N, 86.68 - 86.78° E
        if (lat >= 20.20 && lat <= 20.30 && lng >= 86.68 && lng <= 86.78) return true;
        // Haldia River Channel: 21.90 - 22.05° N, 88.00 - 88.12° E
        if (lat >= 21.90 && lat <= 22.05 && lng >= 88.00 && lng <= 88.12) return true;

        // Strict Inland Indian Subcontinent Landmass Guard
        if (lat >= 8.5 && lat <= 28.0 && lng >= 73.0 && lng <= 86.0) {
            return false; // Inland Landmass!
        }

        return true;
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
        } else {
            // Arabian Sea water position
            double lat = 9.0 + (Math.abs(mmsi * 13) % 1100) / 100.0; // 9.0 to 20.0° N
            double lng = 63.0 + (Math.abs(mmsi * 29) % 850) / 100.0; // 63.0 to 71.5° E
            return new double[]{lat, lng};
        }
    }
}
