package com.projectui.model;

public class Ship {

    private String shipId;
    private String shipName;
    private String shipType;
    private String imoNumber;
    private String company;
    private String arrival;
    private String departure;
    private String berth;
    private String status;

    public Ship(String shipId, 
        String shipName, String shipType, 
        String imoNumber, String company, 
        String arrival, String departure, 
        String berth, String status) {

        this.shipId = shipId;
        this.shipName = shipName;
        this.shipType = shipType;
        this.imoNumber = imoNumber;
        this.company = company;
        this.arrival = arrival;
        this.departure = departure;
        this.berth = berth;
        this.status = status;
    }

    public Ship(){}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getShipId() {
        return shipId;
    }

    public void setShipId(String shipId) {
        this.shipId = shipId;
    }

    public String getShipName() {
        return shipName;
    }

    public void setShipName(String shipName) {
        this.shipName = shipName;
    }

    public String getShipType() {
        return shipType;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public String getImoNumber() {
        return imoNumber;
    }

    public void setImoNumber(String imoNumber) {
        this.imoNumber = imoNumber;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getArrival() {
        return arrival;
    }

    public void setArrival(String arrival) {
        this.arrival = arrival;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getBerth() {
        return berth;
    }

    public void setBerth(String berth) {
        this.berth = berth;
    }
}
