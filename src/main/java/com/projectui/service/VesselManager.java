package com.projectui.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VesselManager {

    private static final VesselManager instance = new VesselManager();
    private final Map<Long, VesselData> vesselsByMmsi = new ConcurrentHashMap<>();

    private VesselManager() {}

    public static VesselManager getInstance() {
        return instance;
    }

    public boolean updateOrAddVessel(VesselData newVessel) {
        if (newVessel == null || !newVessel.isValid()) {
            return false;
        }

        boolean isNew = !vesselsByMmsi.containsKey(newVessel.mmsi);
        vesselsByMmsi.put(newVessel.mmsi, newVessel);
        return isNew;
    }

    public VesselData getVessel(long mmsi) {
        return vesselsByMmsi.get(mmsi);
    }

    public VesselData getVesselByName(String name) {
        if (name == null || name.isBlank()) return null;
        String clean = name.trim().toLowerCase();
        for (VesselData v : vesselsByMmsi.values()) {
            if (v != null && v.name != null && (v.name.equalsIgnoreCase(clean) || v.name.toLowerCase().contains(clean) || clean.contains(v.name.toLowerCase()))) {
                return v;
            }
        }
        return null;
    }

    public Collection<VesselData> getAllVessels() {
        return vesselsByMmsi.values();
    }

    public int getActiveVesselCount() {
        return vesselsByMmsi.size();
    }

    public int getTotalLiveCount() {
        return vesselsByMmsi.size();
    }

    public int getInPortCount() {
        int count = 0;
        for (VesselData v : vesselsByMmsi.values()) {
            if ("Currently In Port".equals(categorizeStatus(v))) count++;
        }
        return count;
    }

    public int getArrivingCount() {
        int count = 0;
        for (VesselData v : vesselsByMmsi.values()) {
            if ("Arriving".equals(categorizeStatus(v))) count++;
        }
        return count;
    }

    public int getDepartingCount() {
        int count = 0;
        for (VesselData v : vesselsByMmsi.values()) {
            if ("Departing".equals(categorizeStatus(v))) count++;
        }
        return count;
    }

    public int getInTransitCount() {
        int count = 0;
        for (VesselData v : vesselsByMmsi.values()) {
            if ("In Transit".equals(categorizeStatus(v))) count++;
        }
        return count;
    }

    public int getAnchoredCount() {
        int count = 0;
        for (VesselData v : vesselsByMmsi.values()) {
            if ("Anchored".equals(categorizeStatus(v))) count++;
        }
        return count;
    }

    public static String categorizeStatus(VesselData v) {
        if (v == null) return "In Transit";
        String nav = v.navigationStatus != null ? v.navigationStatus.toLowerCase() : "";
        double spd = v.speed;

        if (nav.contains("moored") || nav.contains("docked") || nav.contains("port") || nav.contains("5")) {
            return "Currently In Port";
        }
        if (nav.contains("anchor") || nav.contains("1") || spd <= 0.3) {
            return "Anchored";
        }
        if (nav.contains("arriving") || (v.destination != null && !v.destination.isBlank() && spd > 2.0 && spd < 10.0)) {
            return "Arriving";
        }
        if (nav.contains("departing") || (spd >= 10.0 && v.course > 180)) {
            return "Departing";
        }
        return "In Transit";
    }

    public void removeStaleVessels(long maxAgeMillis) {
        List<Long> staleMmsis = new ArrayList<>();
        for (VesselData v : vesselsByMmsi.values()) {
            if (v.isStale(maxAgeMillis)) {
                staleMmsis.add(v.mmsi);
            }
        }
        for (Long mmsi : staleMmsis) {
            vesselsByMmsi.remove(mmsi);
        }
    }

    public void clear() {
        vesselsByMmsi.clear();
    }
}
