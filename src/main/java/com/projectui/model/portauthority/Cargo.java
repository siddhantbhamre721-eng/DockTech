package com.projectui.model.portauthority;

public class Cargo {

    private String cargoId;
    private String ship;
    private String cargoType;
    private String weight;
    private String origin;
    private String destination;
    private String containers;
    private String status;

    public Cargo() {
        // Required by Firestore
    }

    public Cargo(
            String cargoId,
            String ship,
            String cargoType,
            String weight,
            String origin,
            String destination,
            String containers,
            String status) {
                
        this.cargoId = cargoId;
        this.ship = ship;
        this.cargoType = cargoType;
        this.weight = weight;
        this.origin = origin;
        this.destination = destination;
        this.containers = containers;
        this.status = status;
    }

    public String getCargoId() {
        return cargoId;
    }

    public void setCargoId(String cargoId) {
        this.cargoId = cargoId;
    }

    public String getShip() {
        return ship;
    }

    public void setShip(String ship) {
        this.ship = ship;
    }

    public String getCargoType() {
        return cargoType;
    }

    public void setCargoType(String cargoType) {
        this.cargoType = cargoType;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getContainers() {
        return containers;
    }

    public void setContainers(String containers) {
        this.containers = containers;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}