package com.projectui.dao.portauthority;

import java.util.ArrayList;
import java.util.List;

import com.projectui.config.portauthority.FirebaseConfig;
import com.projectui.model.portauthority.Notification;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;


// =====================================================
// NOTIFICATION DAO
// =====================================================

public class NotificationDAO {

    private Firestore db =
            FirebaseConfig.getFirestore();


    // =====================================================
    // SAVE NOTIFICATION
    // =====================================================

    public void saveNotification(
            Notification notification
    ) {

        try {

            db.collection("Notifications")
              .document(
                      notification.getNotificationId()
              )
              .create(notification)
              .get();


            System.out.println(
                    "Notification Data Inserted"
            );


        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // GET ONE NOTIFICATION
    // =====================================================

    public Notification getNotification(
            String notificationId
    ) {

        try {

            ApiFuture<DocumentSnapshot> future =
                    db.collection("Notifications")
                      .document(notificationId)
                      .get();


            DocumentSnapshot document =
                    future.get();


            if (document.exists()) {

                return document.toObject(
                        Notification.class
                );
            }


        } catch (Exception e) {

            e.printStackTrace();
        }


        return null;
    }


    // =====================================================
    // DELETE NOTIFICATION
    // =====================================================

    public void deleteNotification(
            String notificationId
    ) {

        try {

            db.collection("Notifications")
              .document(notificationId)
              .delete()
              .get();


            System.out.println(
                    "Notification Data Deleted"
            );


        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // GET ALL NOTIFICATIONS
    // =====================================================

    public List<Notification> getNotifications() {

        List<Notification> list =
                new ArrayList<>();


        try {

            ApiFuture<QuerySnapshot> future =
                    db.collection("Notifications")
                      .orderBy(
                              "createdDate",
                              Query.Direction.DESCENDING
                      )
                      .get();


            QuerySnapshot snapshot =
                    future.get();


            for (
                    DocumentSnapshot doc :
                    snapshot.getDocuments()
            ) {

                Notification notification =
                        doc.toObject(
                                Notification.class
                        );


                if (notification != null) {

                    list.add(
                            notification
                    );
                }
            }


        } catch (Exception e) {

            e.printStackTrace();
        }


        return list;
    }


    // =====================================================
    // GET RECENT NOTIFICATIONS
    // =====================================================

    public List<Notification> getRecentNotifications(
            int limit
    ) {

        List<Notification> list =
                new ArrayList<>();


        try {

            ApiFuture<QuerySnapshot> future =
                    db.collection("Notifications")
                      .orderBy(
                              "createdDate",
                              Query.Direction.DESCENDING
                      )
                      .limit(
                              limit
                      )
                      .get();


            QuerySnapshot snapshot =
                    future.get();


            for (
                    DocumentSnapshot doc :
                    snapshot.getDocuments()
            ) {

                Notification notification =
                        doc.toObject(
                                Notification.class
                        );


                if (notification != null) {

                    list.add(
                            notification
                    );
                }
            }


        } catch (Exception e) {

            e.printStackTrace();
        }


        return list;
    }
}