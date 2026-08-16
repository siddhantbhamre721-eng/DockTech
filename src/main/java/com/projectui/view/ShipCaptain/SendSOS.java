package com.projectui.view.ShipCaptain;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class SendSOS {

    private final BorderPane root = new BorderPane();

    public Scene getSOSScene() {

        root.setStyle("-fx-background-color:#f3f8ff;");

        root.setTop(header());
        root.setCenter(emergencyPage());

        return new Scene(root, 1330, 735);
    }

    // ================= HEADER =================

    private HBox header() {

        HBox h = new HBox(15);

        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(12,22,12,25));
        h.setPrefHeight(65);
        h.setStyle("-fx-background-color:white;");

        Label title =
                new Label("Emergency SOS");

        title.setStyle(
            "-fx-font-size:22;" +
            "-fx-font-weight:bold;" +
            "-fx-text-fill:#172033;"
        );

        Label sub =
                new Label(
                    "Send an emergency alert to the Port Authority."
                );

        sub.setStyle(
            "-fx-font-size:13;" +
            "-fx-text-fill:#687386;"
        );

        VBox titleBox =
                new VBox(3, title, sub);

        Region space = new Region();

        HBox.setHgrow(
            space,
            Priority.ALWAYS
        );

       

        
       

      
        h.getChildren().addAll(
            titleBox,
            space
            
        );

        return h;
    }

    // ================= EMERGENCY PAGE =================

    private VBox emergencyPage() {

        VBox main =
                new VBox(18);

        main.setPadding(
            new Insets(30,70,25,70)
        );

        // ================= ALERT =================

        HBox alert =
                new HBox(15);

        alert.setAlignment(
            Pos.CENTER_LEFT
        );

        alert.setPadding(
            new Insets(17,22,17,22)
        );

        alert.setStyle(
            "-fx-background-color:#fff5f5;" +
            "-fx-border-color:#f0baba;" +
            "-fx-border-radius:9;" +
            "-fx-background-radius:9;"
        );

        // RED WARNING SYMBOL
        Label alertIcon =
                new Label("⚠");

        alertIcon.setMinSize(42,42);
        alertIcon.setPrefSize(42,42);
        alertIcon.setAlignment(Pos.CENTER);

        alertIcon.setStyle(
            "-fx-font-size:20;" +
            "-fx-font-weight:bold;" +
            "-fx-text-fill:#dc3d3d;" +
            "-fx-background-color:#ffe8e8;" +
            "-fx-background-radius:50;"
        );

        Label alertTitle =
                new Label(
                    "Emergency Alert Channel"
                );

        alertTitle.setStyle(
            "-fx-font-size:14;" +
            "-fx-font-weight:bold;" +
            "-fx-text-fill:#dc3d3d;"
        );

        Label alertSub =
                new Label(
                    "Only use this to notify the Port Authority of a real emergency."
                );

        alertSub.setStyle(
            "-fx-font-size:12;" +
            "-fx-text-fill:#687386;"
        );

        VBox alertText =
                new VBox(
                    3,
                    alertTitle,
                    alertSub
                );

        alert.getChildren().addAll(
            alertIcon,
            alertText
        );

        // ================= DETAILS CARD =================

        VBox card =
                new VBox(17);

        card.setPadding(
            new Insets(25)
        );

        card.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:10;" +
            "-fx-border-color:#e1e6ed;" +
            "-fx-border-radius:10;"
        );

        Label details =
                new Label("Emergency Details");

        details.setStyle(
            "-fx-font-size:17;" +
            "-fx-font-weight:bold;"
        );

        Label detailsSub =
                new Label(
                    "Confirm your vessel information and describe the situation."
                );

        detailsSub.setStyle(
            "-fx-font-size:12;" +
            "-fx-text-fill:#687386;"
        );

        // ================= SHIP / LOCATION =================

        HBox info =
                new HBox(15);

        VBox ship =
                infoBox(
                    "⚓  Current Ship",
                    "Oceanic Star"
                );

        VBox location =
                infoBox(
                    "⌖  Current Location",
                    "Port of Mumbai — Berth B04"
                );

        HBox.setHgrow(
            ship,
            Priority.ALWAYS
        );

        HBox.setHgrow(
            location,
            Priority.ALWAYS
        );

        info.getChildren().addAll(
            ship,
            location
        );

        // ================= EMERGENCY TYPE =================

        Label typeLabel =
                new Label("Emergency Type");

        typeLabel.setStyle(
            "-fx-font-size:12;" +
            "-fx-font-weight:bold;"
        );

        ComboBox<String> type =
                new ComboBox<>();

        type.getItems().addAll(
            "Medical Emergency",
            "Fire",
            "Collision",
            "Engine Failure",
            "Security Threat",
            "Other"
        );

        type.setPromptText(
            "Select Emergency"
        );

        type.setMaxWidth(
            Double.MAX_VALUE
        );

        type.setPrefHeight(40);

        VBox typeBox =
                new VBox(
                    7,
                    typeLabel,
                    type
                );

        // ================= INFORMATION =================

        Label infoLabel =
                new Label(
                    "Additional Information"
                );

        infoLabel.setStyle(
            "-fx-font-size:12;" +
            "-fx-font-weight:bold;"
        );

        TextArea description =
                new TextArea();

        description.setPromptText(
            "Describe the emergency..."
        );

        description.setPrefRowCount(4);
        description.setWrapText(true);

        VBox descriptionBox =
                new VBox(
                    7,
                    infoLabel,
                    description
                );

        // ================= SEND SOS =================

        Button send =
                new Button(
                    "⚠   SEND SOS"
                );

        send.setMaxWidth(
            Double.MAX_VALUE
        );

        send.setPrefHeight(52);

        send.setStyle(
            "-fx-background-color:#1769e8;" +
            "-fx-text-fill:white;" +
            "-fx-font-size:14;" +
            "-fx-font-weight:bold;" +
            "-fx-background-radius:6;"
        );

        // ================= SEND ACTION =================

        send.setOnAction(e -> {

            if (type.getValue() == null) {

                Alert alertBox =
                        new Alert(
                            Alert.AlertType.WARNING
                        );

                alertBox.setTitle(
                    "Emergency Type Required"
                );

                alertBox.setHeaderText(null);

                alertBox.setContentText(
                    "Please select an emergency type."
                );

                alertBox.showAndWait();

                return;
            }

            // ================= CUSTOM CONFIRMATION =================

            Dialog<ButtonType> dialog =
                    new Dialog<>();

            dialog.setTitle(
                "Emergency SOS"
            );

            ButtonType cancelButton =
                    new ButtonType(
                        "Cancel",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                    );

            ButtonType sosButton =
                    new ButtonType(
                        "⚠  Send SOS",
                        ButtonBar.ButtonData.OK_DONE
                    );

            dialog.getDialogPane()
                    .getButtonTypes()
                    .addAll(
                        cancelButton,
                        sosButton
                    );

            // WARNING ICON

            Label dialogIcon =
                    new Label("⚠");

            dialogIcon.setMinSize(
                48,
                48
            );

            dialogIcon.setPrefSize(
                48,
                48
            );

            dialogIcon.setAlignment(
                Pos.CENTER
            );

            dialogIcon.setStyle(
                "-fx-font-size:24;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#dc2626;" +
                "-fx-background-color:#ffe7e7;" +
                "-fx-background-radius:50;"
            );

            // TITLE

            Label dialogTitle =
                    new Label(
                        "Confirm Emergency SOS"
                    );

            dialogTitle.setStyle(
                "-fx-font-size:16;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#172033;"
            );

            // MESSAGE

            Label dialogMessage =
                    new Label(
                        "Are you sure you want to send an emergency SOS?"
                    );

            dialogMessage.setStyle(
                "-fx-font-size:12;" +
                "-fx-text-fill:#687386;"
            );

            VBox dialogContent =
                    new VBox(
                        12,
                        dialogIcon,
                        dialogTitle,
                        dialogMessage
                    );

            dialogContent.setAlignment(
                Pos.CENTER
            );

            dialogContent.setPadding(
                new Insets(15,20,10,20)
            );

            dialog.getDialogPane()
                    .setContent(
                        dialogContent
                    );

            dialog.getDialogPane()
                    .setPrefWidth(380);

            dialog.showAndWait();
        });

        // ================= CARD CONTENT =================

        card.getChildren().addAll(
            details,
            detailsSub,
            info,
            typeBox,
            descriptionBox,
            send
        );

        // ================= BOTTOM =================

        Label bottom =
                new Label(
                    "The Port Authority and nearby vessels will be notified immediately."
                );

        bottom.setStyle(
            "-fx-font-size:12;" +
            "-fx-text-fill:#687386;"
        );

        HBox bottomBox =
                new HBox(bottom);

        bottomBox.setAlignment(
            Pos.CENTER
        );

        // ================= FINAL CONTENT =================

        main.getChildren().addAll(
            alert,
            card,
            bottomBox
        );

        return main;
    }

    // ================= INFO BOX =================

    private VBox infoBox(
            String heading,
            String value
    ) {

        Label h =
                new Label(heading);

        h.setStyle(
            "-fx-font-size:11;" +
            "-fx-text-fill:#687386;"
        );

        Label v =
                new Label(value);

        v.setStyle(
            "-fx-font-size:14;" +
            "-fx-font-weight:bold;" +
            "-fx-text-fill:#172033;"
        );

        VBox box =
                new VBox(
                    6,
                    h,
                    v
                );

        box.setPadding(
            new Insets(14)
        );

        box.setMinHeight(72);

        box.setStyle(
            "-fx-background-color:white;" +
            "-fx-border-color:#e1e6ed;" +
            "-fx-border-radius:7;" +
            "-fx-background-radius:7;"
        );

        return box;
    }
}