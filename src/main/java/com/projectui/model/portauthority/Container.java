package com.projectui.model.portauthority;

public class Container {

    private String containerId;
    private String type;
    private String cargoId;
    private String ship;
    private String location;
    private String size;
    private String weight;
    private String status;
    private String allocationDate;


    public Container() {
    }

    public Container(
            String containerId,
            String type,
            String cargoId,
            String ship,
            String location,
            String size,
            String weight,
            String status
    ) {

        this.containerId = containerId;
        this.type = type;
        this.cargoId = cargoId;
        this.ship = ship;
        this.location = location;
        this.size = size;
        this.weight = weight;
        this.status = status;
    }


    // Used when allocation date is also available.

    public Container(
            String containerId,
            String type,
            String cargoId,
            String ship,
            String location,
            String size,
            String weight,
            String status,
            String allocationDate
    ) {

        this.containerId = containerId;
        this.type = type;
        this.cargoId = cargoId;
        this.ship = ship;
        this.location = location;
        this.size = size;
        this.weight = weight;
        this.status = status;
        this.allocationDate = allocationDate;
    }


    // =====================================================
    // GETTERS
    // =====================================================

    public String getContainerId() {
        return containerId;
    }

    public String getType() {
        return type;
    }

    public String getCargoId() {
        return cargoId;
    }

    public String getShip() {
        return ship;
    }

    public String getLocation() {
        return location;
    }

    public String getSize() {
        return size;
    }

    public String getWeight() {
        return weight;
    }

    public String getStatus() {
        return status;
    }

    public String getAllocationDate() {
        return allocationDate;
    }


    // =====================================================
    // SETTERS
    // =====================================================

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCargoId(String cargoId) {
        this.cargoId = cargoId;
    }

    public void setShip(String ship) {
        this.ship = ship;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAllocationDate(String allocationDate) {
        this.allocationDate = allocationDate;
    }
}