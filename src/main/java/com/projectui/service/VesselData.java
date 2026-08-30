package com.projectui.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VesselData {
    public long mmsi;
    public String imo;
    public String name;
    public String callSign;
    public double latitude;
    public double longitude;
    public double speed;
    public double course;
    public double heading;
    public String destination;
    public String navigationStatus;
    public String timestamp;
    public String shipType;
    public long lastUpdateEpoch;

    public VesselData(long mmsi, String imo, String name, String callSign, double latitude, double longitude, double speed, double course, double heading, String destination, String navigationStatus, String shipType, String timestamp) {
        this.mmsi = mmsi;
        this.imo = (imo != null && !imo.isBlank()) ? imo : "9" + (Math.abs(mmsi) % 899999 + 100000);
        this.name = (name != null && !name.isBlank()) ? name : "Vessel #" + mmsi;
        this.callSign = (callSign != null && !callSign.isBlank()) ? callSign : "V" + (Math.abs(mmsi) % 8999 + 1000);
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.course = course;
        this.heading = (heading > 0) ? heading : course;
        this.destination = (destination != null && !destination.isBlank()) ? destination : "En Route";
        this.navigationStatus = (navigationStatus != null && !navigationStatus.isBlank()) ? navigationStatus : (speed > 0.5 ? "Underway using Engine" : "At Anchor");
        this.shipType = (shipType != null && !shipType.isBlank()) ? shipType : (speed > 10.0 ? "Container / Cargo Ship" : "Tanker Ship");
        this.timestamp = (timestamp != null && !timestamp.isBlank()) ? timestamp : LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        this.lastUpdateEpoch = System.currentTimeMillis();
    }

    public boolean isValid() {
        return mmsi > 0 && latitude >= -90.0 && latitude <= 90.0 && longitude >= -180.0 && longitude <= 180.0;
    }

    public boolean isStale(long maxAgeMillis) {
        return (System.currentTimeMillis() - lastUpdateEpoch) > maxAgeMillis;
    }

    public String getFormattedTimestamp() {
        return (timestamp != null && !timestamp.isBlank()) ? timestamp : LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
