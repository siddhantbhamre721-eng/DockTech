package com.projectui.controller.portauthority;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import com.projectui.dao.portauthority.NotificationDAO;
import com.projectui.model.portauthority.Notification;


// =====================================================
// NOTIFICATION CONTROLLER
// =====================================================

public class NotificationController {

    private NotificationDAO notificationDAO =
            new NotificationDAO();


    // =====================================================
    // DATE FORMAT
    // =====================================================

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
            );


    // =====================================================
    // CREATE NOTIFICATION
    // =====================================================

    public void createNotification(

            String title,

            String message,

            String type

    ) {

        try {

            // -------------------------------------------------
            // GENERATE UNIQUE ID
            // -------------------------------------------------

            String notificationId =
                    "NOT-"
                    +
                    UUID.randomUUID()
                         .toString()
                         .substring(
                                 0,
                                 8
                         );


            // -------------------------------------------------
            // CURRENT DATE AND TIME
            // -------------------------------------------------

            String createdDate =
                    LocalDateTime.now()
                            .format(
                                    DATE_FORMATTER
                            );


            // -------------------------------------------------
            // CREATE OBJECT
            // -------------------------------------------------

            Notification notification =
                    new Notification(

                            notificationId,

                            title,

                            message,

                            type,

                            createdDate
                    );


            // -------------------------------------------------
            // SAVE
            // -------------------------------------------------

            notificationDAO.saveNotification(
                    notification
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

        return notificationDAO.getNotification(
                notificationId
        );
    }


    // =====================================================
    // DELETE NOTIFICATION
    // =====================================================

    public void deleteNotification(

            String notificationId

    ) {

        notificationDAO.deleteNotification(
                notificationId
        );
    }


    // =====================================================
    // GET ALL NOTIFICATIONS
    // =====================================================

    public List<Notification> getNotifications() {

        return notificationDAO.getNotifications();
    }


    // =====================================================
    // GET RECENT NOTIFICATIONS
    // =====================================================

    public List<Notification> getRecentNotifications(

            int limit

    ) {

        return notificationDAO.getRecentNotifications(
                limit
        );
    }
}