package com.projectui.controller;

import java.util.UUID;

public class ExpPaymentController {

    public String processPayment(String cargoId, String amount, String method) {
        String txId = "TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        System.out.println("[PAYMENT CONTROLLER] Processed payment " + txId + " amount=" + amount + " method=" + method);
        return txId;
    }

    public String makePayment(String cargoId, double amount, String method, String exporterName) {
        String txId = "TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        System.out.println("[PAYMENT CONTROLLER] Made payment " + txId + " cargo=" + cargoId + " amount=" + amount + " by=" + exporterName);
        return txId;
    }

    public String makePayment(String cargoId, Object amount, String method, String exporterName) {
        String txId = "TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        System.out.println("[PAYMENT CONTROLLER] Made payment " + txId + " cargo=" + cargoId + " amount=" + amount + " by=" + exporterName);
        return txId;
    }
}
