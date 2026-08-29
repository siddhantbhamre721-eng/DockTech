package com.projectui.service;

public class VesselPositionValidator {

    /**
     * Validates whether a given latitude/longitude represents a valid maritime/water position.
     * Returns false if the coordinate falls on inland landmass.
     */
    public static boolean isWaterCoordinate(double lat, double lng) {
        if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) return false;

        // Specific Port Harbors & Coastal Approaches (Valid Water - Always Preserve):
        // Mumbai Harbor: 18.82 - 18.98° N, 72.72 - 72.88° E
        if (lat >= 18.82 && lat <= 18.98 && lng >= 72.72 && lng <= 72.88) return true;
        // JNPT Harbor: 18.90 - 18.97° N, 72.89 - 72.96° E
        if (lat >= 18.90 && lat <= 18.97 && lng >= 72.89 && lng <= 72.96) return true;
        // Mormugao / Goa Harbor & Approach: 15.20 - 15.60° N, 73.50 - 73.85° E
        if (lat >= 15.20 && lat <= 15.60 && lng >= 73.50 && lng <= 73.85) return true;
        // Mangalore Harbor & Approach: 12.75 - 13.05° N, 74.50 - 74.85° E
        if (lat >= 12.75 && lat <= 13.05 && lng >= 74.50 && lng <= 74.85) return true;
        // Kochi / Cochin Harbor & Coastal Approach: 9.00 - 10.50° N, 75.00 - 76.28° E
        if (lat >= 9.00 && lat <= 10.50 && lng >= 75.00 && lng <= 76.28) return true;
        // Malabar Coast Approach (Kerala): 8.00 - 12.50° N, 74.80 - 76.80° E
        if (lat >= 8.00 && lat <= 12.50 && lng >= 74.80 && lng <= 76.30) return true;
        // Tuticorin Harbor: 8.70 - 8.80° N, 78.15 - 78.25° E
        if (lat >= 8.70 && lat <= 8.80 && lng >= 78.15 && lng <= 78.25) return true;
        // Chennai Port Approach: 13.05 - 13.15° N, 80.28 - 80.38° E
        if (lat >= 13.05 && lat <= 13.15 && lng >= 80.28 && lng <= 80.38) return true;
        // Visakhapatnam / Vizag Port Approach: 17.65 - 17.72° N, 83.26 - 83.36° E
        if (lat >= 17.65 && lat <= 17.72 && lng >= 83.26 && lng <= 83.36) return true;
        // Paradip Port Approach: 20.20 - 20.30° N, 86.68 - 86.78° E
        if (lat >= 20.20 && lat <= 20.30 && lng >= 86.68 && lng <= 86.78) return true;
        // Haldia River Channel / Kolkata approach: 21.50 - 22.10° N, 87.80 - 88.25° E
        if (lat >= 21.50 && lat <= 22.10 && lng >= 87.80 && lng <= 88.25) return true;

        // Gulf of Kutch water: 22.3-22.9°N, 69.0-70.5°E
        if (lat >= 22.3 && lat <= 22.9 && lng >= 69.0 && lng <= 70.5) return true;
        // Gulf of Khambhat water: 20.5-22.0°N, 72.0-72.7°E
        if (lat >= 20.5 && lat <= 22.0 && lng >= 72.0 && lng <= 72.7) return true;

        // Open Sea / Ocean Zones:
        // Arabian Sea: West of Indian West Coast
        if (lat >= -10.0 && lat <= 25.0 && lng >= 45.0) {
            if (lat >= 20.0 && lng <= 72.8) return true;
            if (lat >= 15.0 && lat < 20.0 && lng <= 73.2) return true;
            if (lat >= 12.0 && lat < 15.0 && lng <= 74.7) return true;
            if (lat >= 8.0 && lat < 12.0 && lng <= 76.28) return true;
            if (lat < 8.0 && lng <= 77.5) return true;
        }

        // Bay of Bengal: East of coast line up to 98°E
        if (lng >= 80.2 && lng <= 98.0 && lat >= -10.0 && lat <= 22.0) return true;

        // Southern Indian Ocean
        if (lat >= -30.0 && lat <= 7.5 && lng >= 50.0 && lng <= 100.0) return true;

        // 1. Inland Landmass Guard for Indian Subcontinent:
        // West Coast Inland Landmass (Gujarat to Kerala):
        if (lat >= 8.5 && lat <= 24.0 && lng >= 73.0 && lng <= 79.5) {
            if (lat > 20.0 && lng > 70.8 && lng < 72.0) return false; // Inland Gujarat
            if (lat >= 15.0 && lat <= 20.0 && lng >= 73.5 && lng <= 79.5) return false; // Maharashtra / Karnataka inland
            if (lat >= 8.5 && lat < 15.0 && lng >= 76.5 && lng <= 79.5) return false; // South India inland
        }

        // East Coast Inland Landmass (Tamil Nadu, Andhra Pradesh, Odisha):
        if (lat >= 13.5 && lat <= 18.0) {
            double coastalMinLng = 80.2 + (lat - 13.5) * (3.1 / 4.5);
            if (lng >= 78.0 && lng < coastalMinLng) return false;
        }

        if (lat >= 18.0 && lat <= 21.5) {
            double coastalMinLng = 83.3 + (lat - 18.0) * (3.4 / 3.5);
            if (lng >= 79.0 && lng < coastalMinLng) return false;
        }

        if (lat > 21.5 && lng < 88.0) return false; // Inland West Bengal / Bangladesh

        // Generic broad land guard
        if (lat >= 8.5 && lat <= 35.0 && lng >= 68.0 && lng <= 92.0) {
            return false;
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
