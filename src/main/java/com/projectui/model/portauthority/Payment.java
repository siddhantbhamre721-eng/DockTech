package com.projectui.model.portauthority;

public class Payment {

    private String paymentId;
    private String cargoId;
    private String ship;
    private String customer;
    private double amount;
    private String date;
    private String method;
    private String status;


    // =====================================================
    // DEFAULT CONSTRUCTOR
    // =====================================================

    public Payment() {
    }


    // =====================================================
    // FULL CONSTRUCTOR
    // =====================================================

    public Payment(
            String paymentId,
            String cargoId,
            String ship,
            String customer,
            double amount,
            String date,
            String method,
            String status
    ) {

        this.paymentId = paymentId;
        this.cargoId = cargoId;
        this.ship = ship;
        this.customer = customer;
        this.amount = amount;
        this.date = date;
        this.method = method;
        this.status = status;
    }


    // =====================================================
    // GETTERS
    // =====================================================

    public String getPaymentId() {
        return paymentId;
    }

    public String getCargoId() {
        return cargoId;
    }

    public String getShip() {
        return ship;
    }

    public String getCustomer() {
        return customer;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getMethod() {
        return method;
    }

    public String getStatus() {
        return status;
    }


    // =====================================================
    // SETTERS
    // =====================================================

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public void setCargoId(String cargoId) {
        this.cargoId = cargoId;
    }

    public void setShip(String ship) {
        this.ship = ship;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}