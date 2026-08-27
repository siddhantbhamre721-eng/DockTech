package com.projectui.dao.portauthority;

import java.util.ArrayList;
import java.util.List;

import com.projectui.config.portauthority.FirebaseConfig;
import com.projectui.model.portauthority.Payment;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.Firestore;


public class PaymentDAO {

    private Firestore db = FirebaseConfig.getFirestore();


    // =====================================================
    // SAVE PAYMENT
    // =====================================================

    public void savePayment(
            Payment payment
    ) {

        try {

            db.collection("Payments")
              .document(payment.getPaymentId())
              .create(payment);

            System.out.println(
                    "Payment Data Inserted"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // GET ONE PAYMENT
    // =====================================================

    public Payment getPayment(
            String paymentId
    ) {

        try {

            ApiFuture<DocumentSnapshot> future =
                    db.collection("Payments")
                      .document(paymentId)
                      .get();

            DocumentSnapshot document =
                    future.get();


            if (document.exists()) {

                return document.toObject(
                        Payment.class
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }


    // =====================================================
    // UPDATE PAYMENT
    // =====================================================

    public void updatePayment(
            Payment payment
    ) {

        try {

            db.collection("Payments")
              .document(payment.getPaymentId())
              .update(

                    "cargoId",
                    payment.getCargoId(),

                    "ship",
                    payment.getShip(),

                    "customer",
                    payment.getCustomer(),

                    "amount",
                    payment.getAmount(),

                    "date",
                    payment.getDate(),

                    "method",
                    payment.getMethod(),

                    "status",
                    payment.getStatus()
              );

            System.out.println(
                    "Payment Data Updated"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // DELETE PAYMENT
    // =====================================================

    public void deletePayment(
            String paymentId
    ) {

        try {

            db.collection("Payments")
              .document(paymentId)
              .delete()
              .get();

            System.out.println(
                    "Payment Data Deleted"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // GET ALL PAYMENTS
    // =====================================================

    public List<Payment> getPayments() {

        List<Payment> list =
                new ArrayList<>();


        try {

            ApiFuture<QuerySnapshot> future =
                    db.collection("Payments")
                      .get();

            QuerySnapshot snapshot =
                    future.get();


            for (
                    DocumentSnapshot doc :
                    snapshot.getDocuments()
            ) {

                Payment payment =
                        doc.toObject(
                                Payment.class
                        );

                list.add(
                        payment
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }


        return list;
    }
}