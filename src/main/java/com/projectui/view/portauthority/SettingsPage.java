package com.projectui.view.portauthority;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class SettingsPage {

    private VBox root;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public SettingsPage() {

        root = new VBox(20);

        root.setPadding(new Insets(30));

        root.setStyle(
                "-fx-background-color: #f4f8fc;"
        );

        // =====================================================
        // PAGE TITLE
        // =====================================================

        Label title = new Label("Settings");

        title.setStyle(
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #14213d;"
        );

        Label subtitle = new Label(
                "Manage your Port Authority account and preferences"
        );

        subtitle.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: #7890ad;"
        );

        VBox heading = new VBox(5);

        heading.getChildren().addAll(
                title,
                subtitle
        );


        // =====================================================
        // PORT AUTHORITY PROFILE
        // =====================================================

        VBox profileCard = createCard();

        Label profileTitle =
                new Label("Port Authority Profile");

        profileTitle.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #14213d;"
        );


        TextField fullName =
                new TextField("Port Authority Admin");

        TextField email =
                new TextField("authority@docktech.com");

        TextField phone =
                new TextField("+91 XXXXX XXXXX");

        TextField port =
                new TextField("Port of Mumbai");

        TextField role =
                new TextField("Port Authority");

        // =====================================================
        // PROFILE FIELDS ARE READ-ONLY INITIALLY
        // =====================================================

        fullName.setEditable(false);
        email.setEditable(false);
        phone.setEditable(false);
        port.setEditable(false);
        role.setEditable(false);


        profileCard.getChildren().addAll(
                profileTitle,

                createField("FULL NAME", fullName),
                createField("EMAIL", email),
                createField("PHONE", phone),
                createField("PORT", port),
                createField("ROLE", role)
        );


        // =====================================================
        // EDIT PROFILE BUTTON
        // =====================================================

        Button editProfile =
                new Button("Edit Profile");

        editProfile.setMaxWidth(Double.MAX_VALUE);

        editProfile.setStyle(
                "-fx-background-color: #2879ed;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10px;"
        );

        editProfile.setOnAction(e -> {

            fullName.setEditable(true);
            email.setEditable(true);
            phone.setEditable(true);
            port.setEditable(true);

            showMessage(
                    "Profile Editing",
                    "Profile fields are now editable."
            );
        });


        profileCard.getChildren().add(editProfile);


        // =====================================================
        // SECURITY SECTION
        // =====================================================

        VBox securityCard = createCard();

        Label securityTitle =
                new Label("Security");

        securityTitle.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #14213d;"
        );


        PasswordField currentPassword =
                new PasswordField();

        currentPassword.setPromptText("Current Password");


        PasswordField newPassword =
                new PasswordField();

        newPassword.setPromptText("New Password");


        PasswordField confirmPassword =
                new PasswordField();

        confirmPassword.setPromptText("Confirm Password");


        securityCard.getChildren().addAll(

                securityTitle,

                createField(
                        "CURRENT PASSWORD",
                        currentPassword
                ),

                createField(
                        "NEW PASSWORD",
                        newPassword
                ),

                createField(
                        "CONFIRM PASSWORD",
                        confirmPassword
                )
        );


        // =====================================================
        // TWO FACTOR AUTHENTICATION
        // =====================================================

        CheckBox twoFactor =
                new CheckBox(
                        "Two-Factor Authentication"
                );

        twoFactor.setSelected(true);


        Label twoFactorInfo =
                new Label(
                        "Add an extra layer of security to your account."
                );

        twoFactorInfo.setStyle(
                "-fx-text-fill: #7890ad;" +
                "-fx-font-size: 12px;"
        );


        VBox twoFactorBox =
                new VBox(
                        3,
                        twoFactor,
                        twoFactorInfo
                );

        twoFactorBox.setPadding(
                new Insets(12)
        );

        twoFactorBox.setStyle(
                "-fx-background-color: #f8fafc;" +
                "-fx-border-color: #e1e8f0;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;"
        );


        Button changePassword =
                new Button("Change Password");

        changePassword.setStyle(
                "-fx-background-color: #2879ed;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;"
        );


        changePassword.setOnAction(e -> {

            if (newPassword.getText().isEmpty()
                    || confirmPassword.getText().isEmpty()) {

                showMessage(
                        "Change Password",
                        "Please enter the new password."
                );

                return;
            }


            if (!newPassword.getText()
                    .equals(confirmPassword.getText())) {

                showMessage(
                        "Change Password",
                        "New passwords do not match."
                );

                return;
            }


            showMessage(
                    "Change Password",
                    "Password changed successfully."
            );

            currentPassword.clear();
            newPassword.clear();
            confirmPassword.clear();
        });


        securityCard.getChildren().addAll(
                twoFactorBox,
                changePassword
        );


        // =====================================================
        // APPLICATION PREFERENCES
        // =====================================================

        VBox preferencesCard =
                createCard();

        Label preferencesTitle =
                new Label("Application Preferences");

        preferencesTitle.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #14213d;"
        );


        ComboBox<String> language =
                new ComboBox<>();

        language.getItems().addAll(
                "English",
                "Marathi",
                "Hindi"
        );

        language.setValue("English");


        ComboBox<String> dateFormat =
                new ComboBox<>();

        dateFormat.getItems().addAll(
                "DD/MM/YYYY",
                "MM/DD/YYYY",
                "YYYY-MM-DD"
        );

        dateFormat.setValue("DD/MM/YYYY");


        ComboBox<String> timeZone =
                new ComboBox<>();

        timeZone.getItems().addAll(
                "Asia/Kolkata",
                "UTC",
                "Asia/Dubai",
                "Asia/Singapore"
        );

        timeZone.setValue("Asia/Kolkata");


        preferencesCard.getChildren().addAll(

                preferencesTitle,

                createComboField(
                        "LANGUAGE",
                        language
                ),

                createComboField(
                        "DATE FORMAT",
                        dateFormat
                ),

                createComboField(
                        "TIME ZONE",
                        timeZone
                )
        );


        // =====================================================
        // NOTIFICATION PREFERENCES
        // =====================================================

        VBox notificationCard =
                createCard();

        Label notificationTitle =
                new Label("Notification Preferences");

        notificationTitle.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #14213d;"
        );


        notificationCard.getChildren().add(
                notificationTitle
        );


        notificationCard.getChildren().add(
                createNotificationRow(
                        "Vessel Arrival Alerts"
                )
        );

        notificationCard.getChildren().add(
                createNotificationRow(
                        "Vessel Departure Alerts"
                )
        );

        notificationCard.getChildren().add(
                createNotificationRow(
                        "Cargo Alerts"
                )
        );

        notificationCard.getChildren().add(
                createNotificationRow(
                        "Container Alerts"
                )
        );

        notificationCard.getChildren().add(
                createNotificationRow(
                        "Payment Notifications"
                )
        );

        notificationCard.getChildren().add(
                createNotificationRow(
                        "System Notifications"
                )
        );


        // =====================================================
        // SAVE / RESET BUTTONS
        // =====================================================

        Button save =
                new Button("Save Changes");

        save.setStyle(
                "-fx-background-color: #2879ed;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10px 20px;"
        );


        Button reset =
                new Button("Reset");

        reset.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #ccd6e2;" +
                "-fx-padding: 10px 20px;"
        );


        save.setOnAction(e -> {

            showMessage(
                    "Settings",
                    "Your settings have been saved successfully."
            );

        });


        reset.setOnAction(e -> {

            language.setValue("English");
            dateFormat.setValue("DD/MM/YYYY");
            timeZone.setValue("Asia/Kolkata");
            twoFactor.setSelected(true);

            showMessage(
                    "Settings",
                    "Settings have been reset."
            );

        });


        HBox bottomButtons =
                new HBox(
                        10,
                        save,
                        reset
                );

        bottomButtons.setAlignment(
                Pos.CENTER_LEFT
        );


        // =====================================================
        // ADD EVERYTHING TO PAGE
        // =====================================================

        root.getChildren().addAll(

                heading,

                profileCard,

                securityCard,

                preferencesCard,

                notificationCard,

                bottomButtons

        );
    }


    // =====================================================
    // CREATE WHITE CARD
    // =====================================================

    private VBox createCard() {

        VBox card = new VBox(12);

        card.setPadding(
                new Insets(20)
        );

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #dce5ef;" +
                "-fx-border-radius: 10;"
        );

        return card;
    }


    // =====================================================
    // CREATE TEXT FIELD WITH LABEL
    // =====================================================

    private VBox createField(
            String labelText,
            Control field
    ) {

        Label label =
                new Label(labelText);

        label.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: #7890ad;" +
                "-fx-font-weight: bold;"
        );


        field.setMaxWidth(
                Double.MAX_VALUE
        );


        VBox box =
                new VBox(
                        5,
                        label,
                        field
                );

        return box;
    }


    // =====================================================
    // CREATE COMBOBOX WITH LABEL
    // =====================================================

    private VBox createComboField(
            String labelText,
            ComboBox<String> combo
    ) {

        Label label =
                new Label(labelText);

        label.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: #7890ad;" +
                "-fx-font-weight: bold;"
        );


        combo.setMaxWidth(180);


        return new VBox(
                5,
                label,
                combo
        );
    }


    // =====================================================
    // CREATE NOTIFICATION ROW
    // =====================================================

    // =====================================================
    // CREATE NOTIFICATION TOGGLE ROW
    // =====================================================

    private HBox createNotificationRow(
                                        String text) {

            // Notification name
            Label label =
                    new Label(text);

            label.setStyle(
                    "-fx-font-size: 13px;" +
                    "-fx-text-fill: #24344d;"
            );


            // Toggle button
            ToggleButton toggle =
                    new ToggleButton("ON");

            toggle.setSelected(true);

            toggle.setPrefWidth(55);
            toggle.setPrefHeight(25);


            // Initial ON appearance
            toggle.setStyle(
                    "-fx-background-color: #2879ed;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 10px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 15;"
            );


            // Change appearance when clicked
            toggle.setOnAction(e -> {

                if (toggle.isSelected()) {

                    toggle.setText("ON");

                    toggle.setStyle(
                            "-fx-background-color: #2879ed;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 15;"
                    );

                } else {

                    toggle.setText("OFF");

                    toggle.setStyle(
                            "-fx-background-color: #ccd6e2;" +
                            "-fx-text-fill: #53657a;" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 15;"
                    );
                }
            });


            // Push toggle to the right
            Region spacer =
                    new Region();

            HBox.setHgrow(
                    spacer,
                    Priority.ALWAYS
            );


            HBox row =
                    new HBox(
                            10,
                            label,
                            spacer,
                            toggle
                    );

            row.setAlignment(
                    Pos.CENTER_LEFT
            );

            row.setPadding(
                    new Insets(8)
            );

            return row;
    }


    // =====================================================
    // SHOW MESSAGE
    // =====================================================

    private void showMessage(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }


    // =====================================================
    // RETURN ROOT
    // =====================================================

    public VBox getContent() {

        return root;
    }
}