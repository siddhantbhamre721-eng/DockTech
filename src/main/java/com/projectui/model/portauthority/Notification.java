package com.projectui.model.portauthority;

public class Notification {

    private String notificationId;
    private String title;
    private String message;
    private String type;
    private String createdDate;


    // =====================================================
    // REQUIRED BY FIRESTORE
    // =====================================================

    public Notification() {
    }


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public Notification(
            String notificationId,
            String title,
            String message,
            String type,
            String createdDate
    ) {

        this.notificationId = notificationId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.createdDate = createdDate;
    }


    // =====================================================
    // GETTERS
    // =====================================================

    public String getNotificationId() {
        return notificationId;
    }


    public String getTitle() {
        return title;
    }


    public String getMessage() {
        return message;
    }


    public String getType() {
        return type;
    }


    public String getCreatedDate() {
        return createdDate;
    }


    // =====================================================
    // SETTERS
    // =====================================================

    public void setNotificationId(
            String notificationId
    ) {

        this.notificationId = notificationId;
    }


    public void setTitle(
            String title
    ) {

        this.title = title;
    }


    public void setMessage(
            String message
    ) {

        this.message = message;
    }


    public void setType(
            String type
    ) {

        this.type = type;
    }


    public void setCreatedDate(
            String createdDate
    ) {

        this.createdDate = createdDate;
    }
}