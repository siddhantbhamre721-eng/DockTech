package com.projectui.service;

import com.projectui.view.admin.AdminDataStore;
import java.util.HashMap;
import java.util.Map;

public class FreightCalculationService {

    private static final double MAX_BUSINESS_AMOUNT = 49900.0;

    private static final FreightCalculationService INSTANCE = new FreightCalculationService();

    public static FreightCalculationService getInstance() {
        return INSTANCE;
    }

    // Default global coordinates fallback table for ports when not explicitly saved with coordinates
    private static final Map<String, double[]> KNOWN_PORT_COORDINATES = new HashMap<>();

    static {
        KNOWN_PORT_COORDINATES.put("port of mumbai", new double[]{18.9439, 72.8354});
        KNOWN_PORT_COORDINATES.put("mumbai", new double[]{18.9439, 72.8354});
        KNOWN_PORT_COORDINATES.put("port of chennai", new double[]{13.0827, 80.2707});
        KNOWN_PORT_COORDINATES.put("chennai", new double[]{13.0827, 80.2707});
        KNOWN_PORT_COORDINATES.put("port of vizag", new double[]{17.6868, 83.2185});
        KNOWN_PORT_COORDINATES.put("visakhapatnam", new double[]{17.6868, 83.2185});
        KNOWN_PORT_COORDINATES.put("port of kolkata", new double[]{22.5726, 88.3639});
        KNOWN_PORT_COORDINATES.put("kolkata", new double[]{22.5726, 88.3639});
        KNOWN_PORT_COORDINATES.put("cochin port", new double[]{9.9600, 76.2600});
        KNOWN_PORT_COORDINATES.put("kochi", new double[]{9.9600, 76.2600});
        KNOWN_PORT_COORDINATES.put("cochin", new double[]{9.9600, 76.2600});
        KNOWN_PORT_COORDINATES.put("port of hazira", new double[]{21.1122, 72.6411});
        KNOWN_PORT_COORDINATES.put("hazira", new double[]{21.1122, 72.6411});

        // International Ports
        KNOWN_PORT_COORDINATES.put("port of singapore", new double[]{1.290270, 103.851959});
        KNOWN_PORT_COORDINATES.put("singapore", new double[]{1.290270, 103.851959});
        KNOWN_PORT_COORDINATES.put("port of dubai", new double[]{25.276987, 55.296249});
        KNOWN_PORT_COORDINATES.put("dubai", new double[]{25.276987, 55.296249});
        KNOWN_PORT_COORDINATES.put("port of london", new double[]{51.5074, 0.1278});
        KNOWN_PORT_COORDINATES.put("london", new double[]{51.5074, 0.1278});
        KNOWN_PORT_COORDINATES.put("port of rotterdam", new double[]{51.9244, 4.4777});
        KNOWN_PORT_COORDINATES.put("rotterdam", new double[]{51.9244, 4.4777});
        KNOWN_PORT_COORDINATES.put("port of new york", new double[]{40.7128, -74.0060});
        KNOWN_PORT_COORDINATES.put("new york", new double[]{40.7128, -74.0060});
        KNOWN_PORT_COORDINATES.put("port of shanghai", new double[]{31.2304, 121.4737});
        KNOWN_PORT_COORDINATES.put("shanghai", new double[]{31.2304, 121.4737});
        KNOWN_PORT_COORDINATES.put("port of hamburg", new double[]{53.5511, 9.9937});
        KNOWN_PORT_COORDINATES.put("hamburg", new double[]{53.5511, 9.9937});
        KNOWN_PORT_COORDINATES.put("port of tokyo", new double[]{35.6762, 139.6503});
        KNOWN_PORT_COORDINATES.put("tokyo", new double[]{35.6762, 139.6503});
    }

    /**
     * Resolves latitude and longitude for a given port name using AdminDataStore or fallback coordinate table.
     */
    public double[] getPortCoordinates(String portName) {
        if (portName == null || portName.isBlank()) {
            return new double[]{18.9439, 72.8354}; // Default to Port of Mumbai coordinates
        }

        // 1. Try to find in AdminDataStore
        try {
            AdminDataStore store = AdminDataStore.getInstance();
            if (store != null) {
                AdminDataStore.Port p = store.getPortByName(portName);
                if (p != null && (p.lat != 0 || p.lng != 0)) {
                    return new double[]{p.lat, p.lng};
                }
            }
        } catch (Exception ignored) {}

        // 2. Lookup in standard coordinate database
        String key = portName.trim().toLowerCase();
        if (KNOWN_PORT_COORDINATES.containsKey(key)) {
            return KNOWN_PORT_COORDINATES.get(key);
        }

        for (Map.Entry<String, double[]> entry : KNOWN_PORT_COORDINATES.entrySet()) {
            if (key.contains(entry.getKey()) || entry.getKey().contains(key)) {
                return entry.getValue();
            }
        }

        return new double[]{18.9439, 72.8354};
    }

    /**
     * Calculates great-circle nautical distance (in kilometers) between two ports using the Haversine formula.
     */
    public double calculateDistanceKm(String originPort, String destinationPort) {
        double[] p1 = getPortCoordinates(originPort);
        double[] p2 = getPortCoordinates(destinationPort);

        double lat1 = p1[0];
        double lon1 = p1[1];
        double lat2 = p2[0];
        double lon2 = p2[1];

        if (Math.abs(lat1 - lat2) < 0.0001 && Math.abs(lon1 - lon2) < 0.0001) {
            return 350.0; // Baseline coastal distance if ports evaluate to same point
        }

        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadiusKm * c;

        return Math.max(distance, 150.0);
    }

    /**
     * Dynamically calculates the estimated cargo valuation in INR (₹).
     */
    public double calculateEstimatedCargoValue(String cargoType, double weightKg, int quantity, String originPort, String destinationPort) {
        if (weightKg <= 0) weightKg = 1000.0;
        if (quantity <= 0) quantity = 1;

        double baseRatePerKg = 180.0; // General Cargo default
        String type = cargoType != null ? cargoType.toLowerCase().trim() : "";

        if (type.contains("electronic")) {
            baseRatePerKg = 650.0;
        } else if (type.contains("machinery")) {
            baseRatePerKg = 480.0;
        } else if (type.contains("automobile") || type.contains("auto")) {
            baseRatePerKg = 380.0;
        } else if (type.contains("hazardous")) {
            baseRatePerKg = 520.0;
        } else if (type.contains("textile")) {
            baseRatePerKg = 240.0;
        } else if (type.contains("food") || type.contains("spice") || type.contains("perishable")) {
            baseRatePerKg = 160.0;
        } else if (type.contains("chemical") || type.contains("mineral")) {
            baseRatePerKg = 310.0;
        }

        double distanceKm = calculateDistanceKm(originPort, destinationPort);
        double distanceFactor = 1.0 + (distanceKm / 50000.0);

        double estimatedTotal = (baseRatePerKg * weightKg * Math.max(1, quantity)) * distanceFactor;
        double roundedValue = Math.round(estimatedTotal / 100.0) * 100.0;
        return Math.max(100.0, Math.min(MAX_BUSINESS_AMOUNT, roundedValue));
    }

    /**
     * Dynamically calculates container booking cost in INR (₹).
     */
    public double calculateContainerBookingCost(String containerType, int containerQty, double weightKg, String originPort, String destinationPort) {
        if (containerQty <= 0) containerQty = 1;
        if (weightKg <= 0) weightKg = 1200.0;

        double distanceKm = calculateDistanceKm(originPort, destinationPort);

        double baseTerminalFee = 8500.0;
        double ratePerKm = 8.50;

        String ct = containerType != null ? containerType.toLowerCase() : "";
        if (ct.contains("40 ft standard")) {
            baseTerminalFee = 14000.0;
            ratePerKm = 13.50;
        } else if (ct.contains("40 ft high")) {
            baseTerminalFee = 15500.0;
            ratePerKm = 15.00;
        } else if (ct.contains("20 ft ref") || ct.contains("refrigerated")) {
            baseTerminalFee = 22000.0;
            ratePerKm = 19.50;
        } else if (ct.contains("40 ft ref")) {
            baseTerminalFee = 32000.0;
            ratePerKm = 28.00;
        } else if (ct.contains("45 ft")) {
            baseTerminalFee = 17000.0;
            ratePerKm = 16.50;
        }

        // Weight handling surcharge (per metric ton)
        double weightHandling = (weightKg / 1000.0) * 950.0;

        double costPerContainer = baseTerminalFee + (distanceKm * ratePerKm) + weightHandling;
        double total = costPerContainer * containerQty;

        return Math.round(total / 100.0) * 100.0;
    }

    /**
     * Payment itemization breakdown record.
     */
    public static class PaymentBreakdown {
        public final double declaredCargoValue;
        public final double containerFreightFee;
        public final double portHandlingFee;
        public final double customsDuty;
        public final double totalPayable;
        public final double distanceKm;
        public final String originPort;
        public final String destinationPort;
        public final String containerType;

        public PaymentBreakdown(double declaredCargoValue, double containerFreightFee, double portHandlingFee,
                                double customsDuty, double totalPayable, double distanceKm,
                                String originPort, String destinationPort, String containerType) {
            this.declaredCargoValue = declaredCargoValue;
            this.containerFreightFee = containerFreightFee;
            this.portHandlingFee = portHandlingFee;
            this.customsDuty = customsDuty;
            this.totalPayable = totalPayable;
            this.distanceKm = distanceKm;
            this.originPort = originPort;
            this.destinationPort = destinationPort;
            this.containerType = containerType;
        }
    }

    /**
     * Calculates payment breakdown from cargo and booking objects.
     */
    public PaymentBreakdown calculatePaymentBreakdown(Map<String, Object> cargo, Map<String, Object> booking) {
        if (cargo == null) {
            return new PaymentBreakdown(0, 0, 0, 0, 0, 0, "N/A", "N/A", "None");
        }

        String cargoType = cargo.get("cargoType") != null ? cargo.get("cargoType").toString() : "General Cargo";
        String originPort = cargo.get("originPort") != null ? cargo.get("originPort").toString() : "Port of Mumbai";
        String destinationPort = cargo.get("destinationPort") != null ? cargo.get("destinationPort").toString() : "Port of Singapore";

        double weightKg = 1200.0;
        if (cargo.get("weight") != null) {
            try {
                weightKg = Double.parseDouble(cargo.get("weight").toString().replaceAll("[^0-9.]", ""));
            } catch (Exception ignored) {}
        }

        int quantity = 1;
        if (cargo.get("quantity") != null) {
            try {
                quantity = Integer.parseInt(cargo.get("quantity").toString().replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}
        }

        // Declared / calculated cargo value
        double declaredValue = 0;
        if (cargo.get("value") != null && !cargo.get("value").toString().isBlank()) {
            try {
                declaredValue = Double.parseDouble(cargo.get("value").toString().replaceAll("[^0-9.]", ""));
            } catch (Exception ignored) {}
        }
        if (declaredValue <= 0 || declaredValue >= MAX_BUSINESS_AMOUNT) {
            declaredValue = calculateEstimatedCargoValue(cargoType, weightKg, quantity, originPort, destinationPort);
        }
        declaredValue = Math.max(100.0, Math.min(MAX_BUSINESS_AMOUNT, declaredValue));

        // Container allocation fee
        String containerType = "20 ft Standard Dry";
        int containerQty = 1;
        double freightFee = 0;

        if (booking != null) {
            if (booking.get("containerType") != null) {
                containerType = booking.get("containerType").toString();
            }
            if (booking.get("quantity") != null) {
                try {
                    containerQty = Integer.parseInt(booking.get("quantity").toString().replaceAll("[^0-9]", ""));
                } catch (Exception ignored) {}
            }
            if (booking.get("totalAmount") != null) {
                try {
                    freightFee = Double.parseDouble(booking.get("totalAmount").toString().replaceAll("[^0-9.]", ""));
                } catch (Exception ignored) {}
            }
        }

        if (freightFee <= 0) {
            freightFee = calculateContainerBookingCost(containerType, containerQty, weightKg, originPort, destinationPort);
        }

        double distanceKm = calculateDistanceKm(originPort, destinationPort);
        double portHandlingFee = Math.round(((weightKg / 1000.0) * 1450.0 + 2500.0) / 100.0) * 100.0;
        double customsDuty = Math.round((declaredValue * 0.08) / 100.0) * 100.0;

        double totalPayable = freightFee + portHandlingFee + customsDuty;
        if (totalPayable >= MAX_BUSINESS_AMOUNT) {
            double scale = (MAX_BUSINESS_AMOUNT - 100.0) / totalPayable;
            freightFee = Math.round(freightFee * scale / 100.0) * 100.0;
            portHandlingFee = Math.round(portHandlingFee * scale / 100.0) * 100.0;
            customsDuty = Math.round(customsDuty * scale / 100.0) * 100.0;
            totalPayable = freightFee + portHandlingFee + customsDuty;
        }

        return new PaymentBreakdown(declaredValue, freightFee, portHandlingFee, customsDuty, totalPayable,
                distanceKm, originPort, destinationPort, containerType);
    }
}
