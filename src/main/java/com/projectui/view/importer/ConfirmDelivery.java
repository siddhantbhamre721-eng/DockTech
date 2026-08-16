package com.projectui.view.importer;



import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConfirmDelivery {

    public Scene getConfirmDeliveryScene(
            Runnable back
    ) {

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#F4F8FC;");
        root.setTop(createHeader("Confirm Delivery", "Dock Tech / Importer / Confirm Delivery"));
        root.setLeft(createSidebar(back));

        VBox content = new VBox(22);
        content.setPadding(new Insets(18));
        content.setStyle("-fx-background-color:#F4F8FC;");

        // Top header is handled in root BorderPane layout

        // =====================================================
        // TITLE
        // =====================================================

        Text title =
                new Text(
                        "Confirm Delivery"
                );

        title.setStyle(
                "-fx-font-size:30px;" +
                "-fx-font-weight:bold;" +
                "-fx-fill:#101A2B;"
        );

        Text subtitle =
                new Text(
                        "Confirm the successful delivery of your shipment."
                );

        subtitle.setStyle(
                "-fx-font-size:15px;" +
                "-fx-fill:#778394;"
        );

        // =====================================================
        // SHIPMENT CARD
        // =====================================================

        VBox shipmentCard =
                new VBox(18);

        shipmentCard.setPadding(
                new Insets(30)
        );

        shipmentCard.setMaxWidth(
                1030
        );

        shipmentCard.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:18px;" +
                "-fx-border-color:#E2E7ED;" +
                "-fx-border-radius:18px;"
        );

        // =====================================================
        // SHIPMENT TOP
        // =====================================================

        HBox shipmentTop =
                new HBox(16);

        shipmentTop.setAlignment(
                Pos.CENTER_LEFT
        );

        Label vesselIcon =
                new Label("🚢");

        vesselIcon.setMinSize(
                58,
                58
        );

        vesselIcon.setAlignment(
                Pos.CENTER
        );

        vesselIcon.setStyle(
                "-fx-background-color:#F5F6F8;" +
                "-fx-background-radius:16px;" +
                "-fx-font-size:25px;"
        );

        VBox vesselInfo =
                new VBox(4);

        Label vesselName =
                new Label(
                        "Oceanic Star"
                );

        vesselName.setStyle(
                "-fx-font-size:22px;" +
                "-fx-font-weight:bold;"
        );

        Label shipmentId =
                new Label(
                        "Shipment ID: SHP-1024"
                );

        shipmentId.setStyle(
                "-fx-text-fill:#737373;" +
                "-fx-font-size:15px;"
        );

        vesselInfo.getChildren().addAll(
                vesselName,
                shipmentId
        );

        Region topSpace =
                new Region();

        HBox.setHgrow(
                topSpace,
                Priority.ALWAYS
        );

        Label deliveryStatus =
                new Label(
                        "● Delivered"
                );

        deliveryStatus.setStyle(
                "-fx-text-fill:#087C35;" +
                "-fx-background-color:#DDF5E4;" +
                "-fx-background-radius:8px;" +
                "-fx-padding:5 12;" +
                "-fx-font-size:14px;"
        );

        shipmentTop.getChildren().addAll(
                vesselIcon,
                vesselInfo,
                topSpace,
                deliveryStatus
        );

        // =====================================================
        // SEPARATOR
        // =====================================================

        Region separator =
                new Region();

        separator.setPrefHeight(1);

        separator.setMaxWidth(
                Double.MAX_VALUE
        );

        separator.setStyle(
                "-fx-background-color:#E1E5E9;"
        );

        // =====================================================
        // SHIPMENT DETAILS
        // =====================================================

        HBox detailsRow1 =
                new HBox();

        detailsRow1.setSpacing(150);

        VBox cargoBox =
                detailBox(
                        "CARGO",
                        "Electronics"
                );

        VBox destinationBox =
                detailBox(
                        "DELIVERED TO",
                        "Mumbai Warehouse"
                );

        HBox.setHgrow(
                cargoBox,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                destinationBox,
                Priority.ALWAYS
        );

        detailsRow1.getChildren().addAll(
                cargoBox,
                destinationBox
        );

        HBox detailsRow2 =
                new HBox();

        detailsRow2.setSpacing(150);

        VBox dateBox =
                detailBox(
                        "DELIVERY DATE",
                        "12 Aug 2026"
                );

        VBox statusBox =
                detailBox(
                        "STATUS",
                        "Delivered"
                );

        HBox.setHgrow(
                dateBox,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                statusBox,
                Priority.ALWAYS
        );

        detailsRow2.getChildren().addAll(
                dateBox,
                statusBox
        );

        shipmentCard.getChildren().addAll(
                shipmentTop,
                separator,
                detailsRow1,
                detailsRow2
        );

        // =====================================================
        // DELIVERY CONFIRMATION CARD
        // =====================================================

        VBox confirmationCard =
                new VBox(18);

        confirmationCard.setPadding(
                new Insets(28)
        );

        confirmationCard.setMaxWidth(
                1030
        );

        confirmationCard.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:18px;" +
                "-fx-border-color:#E2E7ED;" +
                "-fx-border-radius:18px;"
        );

        Label confirmationTitle =
                new Label(
                        "Delivery Confirmation"
                );

        confirmationTitle.setStyle(
                "-fx-font-size:21px;" +
                "-fx-font-weight:bold;"
        );

        Label confirmationInfo =
                new Label(
                        "Mark this shipment as received once it arrives at the warehouse."
                );

        confirmationInfo.setStyle(
                "-fx-text-fill:#777777;" +
                "-fx-font-size:15px;"
        );

        // =====================================================
        // CHECKBOX CONTAINER
        // =====================================================

        HBox checkBoxContainer =
                new HBox(14);

        checkBoxContainer.setAlignment(
                Pos.CENTER_LEFT
        );

        checkBoxContainer.setPadding(
                new Insets(20)
        );

        checkBoxContainer.setMaxWidth(
                Double.MAX_VALUE
        );

        checkBoxContainer.setStyle(
                "-fx-background-color:#F7F7F7;" +
                "-fx-background-radius:12px;" +
                "-fx-border-color:#E1E1E1;" +
                "-fx-border-radius:12px;"
        );

        CheckBox received =
                new CheckBox();

        received.setPrefSize(
                22,
                22
        );

        Label receivedText =
                new Label(
                        "I confirm that the shipment has been received."
                );

        receivedText.setStyle(
                "-fx-font-size:16px;"
        );

        checkBoxContainer.getChildren().addAll(
                received,
                receivedText
        );

        // =====================================================
        // CONFIRM BUTTON
        // =====================================================

        Button confirm =
                new Button(
                        "✓  Confirm Delivery"
                );

        confirm.setPrefHeight(47);

        confirm.setMaxWidth(
                Double.MAX_VALUE
        );

        confirm.setStyle(
                "-fx-background-color:#171717;" +
                "-fx-text-fill:white;" +
                "-fx-background-radius:9px;" +
                "-fx-font-size:16px;"
        );

        confirm.setDisable(
                true
        );

        received.setOnAction(
                e -> confirm.setDisable(
                        !received.isSelected()
                )
        );

        confirm.setOnAction(
                event -> {

                    confirm.setText(
                            "✓  Delivery Confirmed"
                    );

                    confirm.setStyle(
                            "-fx-background-color:#21A366;" +
                            "-fx-text-fill:white;" +
                            "-fx-background-radius:9px;" +
                            "-fx-font-size:16px;"
                    );

                    received.setDisable(
                            true
                    );

                    confirm.setDisable(
                            true
                    );

                    deliveryStatus.setText(
                            "● Delivered"
                    );

                    deliveryStatus.setStyle(
                            "-fx-text-fill:#087C35;" +
                            "-fx-background-color:#DDF5E4;" +
                            "-fx-background-radius:8px;" +
                            "-fx-padding:5 12;" +
                            "-fx-font-size:14px;"
                    );
                }
        );

        confirmationCard.getChildren().addAll(
                confirmationTitle,
                confirmationInfo,
                checkBoxContainer,
                confirm
        );

        // =====================================================
        // CENTER CONTAINER
        // =====================================================

        VBox centerBoxes =
                new VBox(30);

        centerBoxes.setAlignment(
                Pos.TOP_CENTER
        );

        centerBoxes.setMaxWidth(
                1030
        );

        centerBoxes.getChildren().addAll(
                shipmentCard,
                confirmationCard
        );

        // =====================================================
        // TOGGLE ARRIVAL
        // =====================================================

        HBox arrivalBox =
                new HBox(10);

        arrivalBox.setAlignment(
                Pos.CENTER
        );

        Label arrivalLabel =
                new Label(
                        "Simulate arrival status:"
                );

        arrivalLabel.setStyle(
                "-fx-text-fill:#777777;" +
                "-fx-font-size:15px;"
        );

        Button toggleArrival =
                new Button(
                        "Toggle Arrival"
                );

        toggleArrival.setPrefHeight(
                40
        );

        toggleArrival.setStyle(
                "-fx-background-color:white;" +
                "-fx-text-fill:#666666;" +
                "-fx-border-color:#D9D9D9;" +
                "-fx-border-radius:8px;" +
                "-fx-background-radius:8px;" +
                "-fx-font-size:15px;"
        );

        toggleArrival.setOnAction(
                event -> {

                    if (deliveryStatus.getText().equals("● Delivered")) {

                        deliveryStatus.setText(
                                "● In Transit"
                        );

                        deliveryStatus.setStyle(
                                "-fx-text-fill:#A45A00;" +
                                "-fx-background-color:#FFF0D5;" +
                                "-fx-background-radius:8px;" +
                                "-fx-padding:5 12;" +
                                "-fx-font-size:14px;"
                        );

                    } else {

                        deliveryStatus.setText(
                                "● Delivered"
                        );

                        deliveryStatus.setStyle(
                                "-fx-text-fill:#087C35;" +
                                "-fx-background-color:#DDF5E4;" +
                                "-fx-background-radius:8px;" +
                                "-fx-padding:5 12;" +
                                "-fx-font-size:14px;"
                        );
                    }
                }
        );

        arrivalBox.getChildren().addAll(
                arrivalLabel,
                toggleArrival
        );

        // =====================================================
        // ADD TO CONTENT
        // =====================================================

        content.getChildren().addAll(
                title,
                subtitle,
                centerBoxes,
                arrivalBox
        );

        root.setCenter(
                content
        );

        return new Scene(root);
    }

    // =========================================================
    // DETAIL BOX
    // =========================================================

    private VBox detailBox(
            String title,
            String value
    ) {

        VBox box =
                new VBox(6);

        Label heading =
                new Label(title);

        heading.setStyle(
                "-fx-text-fill:#777777;" +
                "-fx-font-size:14px;"
        );

        Label text =
                new Label(value);

        text.setStyle(
                "-fx-font-size:18px;" +
                "-fx-font-weight:normal;"
        );

        box.getChildren().addAll(
                heading,
                text
        );

        return box;
    }

    // =========================================================
    // SIDEBAR
    // =========================================================

    private VBox createSidebar(Runnable back) {
        java.util.Map<String, Button> menuButtons = new java.util.LinkedHashMap<>();
        Button dashboard = new Button("🏠   Dashboard");
        Button track = new Button("📥   Track Shipment");
        Button invoice = new Button("📄   Download Invoice");
        Button delivery = new Button("📦   Confirm Delivery");

        menuButtons.put("Dashboard", dashboard);
        menuButtons.put("Track Shipment", track);
        menuButtons.put("Download Invoice", invoice);
        menuButtons.put("Confirm Delivery", delivery);

        dashboard.setOnAction(e -> back.run());

        track.setOnAction(e -> {
            TrackShipment page = new TrackShipment();
            com.projectui.view.login.LoginPage.showRoot(ImporterDashboard.importerStage, page.getTrackShipmentScene(back).getRoot());
        });

        invoice.setOnAction(e -> {
            DownloadInvoice page = new DownloadInvoice();
            com.projectui.view.login.LoginPage.showRoot(ImporterDashboard.importerStage, page.getDownloadInvoiceScene(back).getRoot());
        });

        return com.projectui.view.login.DashboardLayoutHelper.createSidebar(ImporterDashboard.importerStage, "Importer", "Confirm Delivery", menuButtons);
    }

    private HBox createHeader(String title, String breadcrumb) {
        return com.projectui.view.login.DashboardLayoutHelper.createHeader(ImporterDashboard.importerStage, title, breadcrumb, "Importer", () -> {
            Logout page = new Logout();
            page.showLogout();
        }, null);
    }
}
