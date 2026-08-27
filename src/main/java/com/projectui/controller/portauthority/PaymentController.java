package com.projectui.controller.portauthority;

import java.util.List;

import com.projectui.dao.portauthority.PaymentDAO;
import com.projectui.model.portauthority.Payment;


public class PaymentController {

    private PaymentDAO paymentDAO =
            new PaymentDAO();


    // =====================================================
    // SAVE PAYMENT
    // =====================================================

    public void savePayment(
            Payment payment
    ) {

        paymentDAO.savePayment(
                payment
        );
    }


    // =====================================================
    // GET ONE PAYMENT
    // =====================================================

    public Payment getPayment(
            String paymentId
    ) {

        return paymentDAO.getPayment(
                paymentId
        );
    }


    // =====================================================
    // UPDATE PAYMENT
    // =====================================================

    public void updatePayment(
            Payment payment
    ) {

        paymentDAO.updatePayment(
                payment
        );
    }


    // =====================================================
    // DELETE PAYMENT
    // =====================================================

    public void deletePayment(
            String paymentId
    ) {

        paymentDAO.deletePayment(
                paymentId
        );
    }


    // =====================================================
    // GET ALL PAYMENTS
    // =====================================================

    public List<Payment> getPayments() {

        return paymentDAO.getPayments();
    }
}