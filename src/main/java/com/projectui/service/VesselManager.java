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
            System.err.println("[AIS] Invalid vessel data received. Skipped.");
            return false;
        }

        boolean isNew = !vesselsByMmsi.containsKey(newVessel.mmsi);
        vesselsByMmsi.put(newVessel.mmsi, newVessel);

        System.out.println("[AIS] Received vessel update");
        System.out.println("[AIS] MMSI: " + newVessel.mmsi);
        System.out.println("[AIS] LAT: " + String.format("%.4f", newVessel.latitude));
        System.out.println("[AIS] LNG: " + String.format("%.4f", newVessel.longitude));
        System.out.println("[AIS] SPEED: " + String.format("%.1f kn", newVessel.speed));
        System.out.println("[AIS] COURSE: " + String.format("%.1f°", newVessel.course));
        System.out.println("[AIS] Updating marker for " + newVessel.name);

        return isNew;
    }

    public VesselData getVessel(long mmsi) {
        return vesselsByMmsi.get(mmsi);
    }

    public Collection<VesselData> getAllVessels() {
        return vesselsByMmsi.values();
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
            System.out.println("[AIS] Removed stale vessel MMSI: " + mmsi);
        }
    }

    public int getActiveVesselCount() {
        return vesselsByMmsi.size();
    }

    public void clear() {
        vesselsByMmsi.clear();
    }
}
