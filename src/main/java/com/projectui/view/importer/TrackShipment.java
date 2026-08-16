package com.projectui.view.importer;



import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javafx.scene.image.Image;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.scene.text.Text;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TrackShipment {

    public Scene getTrackShipmentScene(
            Runnable back
    ) {

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#F4F8FC;");
        root.setTop(createHeader("Track Shipment", "Dock Tech / Importer / Track Shipment"));
        VBox sidebar = createSidebar(back);
        root.setLeft(sidebar);

        VBox content = new VBox(22);
        content.setPadding(new Insets(18));
        content.setStyle("-fx-background-color:#F4F8FC;");

        // Top header is handled in root BorderPane layout

        // =====================================================
        // TITLE
        // =====================================================

        Text title =
                new Text(
                        "Track Shipment"
                );

        title.setStyle(
                "-fx-font-size:30px;" +
                "-fx-font-weight:bold;" +
                "-fx-fill:#101A2B;"
        );

        Text subtitle =
                new Text(
                        "Track the current status of your incoming shipment."
                );

        subtitle.setStyle(
                "-fx-font-size:15px;" +
                "-fx-fill:#778394;"
        );

        // =====================================================
        // SEARCH
        // =====================================================

        VBox searchCard =
                new VBox(12);

        searchCard.setPadding(
                new Insets(22)
        );

        searchCard.setStyle(
                cardStyle()
        );

        Label searchTitle =
                new Label(
                        "Search Shipment"
                );

        searchTitle.setStyle(
                "-fx-font-size:17px;" +
                "-fx-font-weight:bold;"
        );

        HBox search =
                new HBox(10);

        TextField field =
                new TextField();

        field.setText(
                "SHP-1024"
        );

        field.setPrefHeight(40);

        HBox.setHgrow(
                field,
                Priority.ALWAYS
        );

        Button track =
                new Button(
                        "♧  Track"
                );

        track.setPrefWidth(100);

        track.setPrefHeight(40);

        track.setStyle(
                "-fx-background-color:#287FF0;" +
                "-fx-text-fill:white;" +
                "-fx-background-radius:8px;" +
                "-fx-font-weight:bold;"
        );

        search.getChildren().addAll(
                field,
                track
        );

        searchCard.getChildren().addAll(
                searchTitle,
                search
        );

        // =====================================================
        // SHIPMENT CARD
        // =====================================================

        VBox shipmentCard =
                new VBox();

        shipmentCard.setStyle(
                cardStyle()
        );

        // =====================================================
        // SHIPMENT HEADER
        // BACKGROUND IMAGE ADDED HERE ONLY
        // =====================================================

        HBox shipHeader =
                new HBox();

        shipHeader.setAlignment(
                Pos.CENTER_LEFT
        );

        shipHeader.setPadding(
                new Insets(25)
        );

        // =====================================================
        // BACKGROUND IMAGE
        // =====================================================

        URL imageURL =
                getClass().getResource(
                        "/assets\\images\\2.png"
                );

        if (imageURL != null) {

            Image shipmentImage =
                    new Image(
                            imageURL.toExternalForm()
                    );

            BackgroundSize imageSize =
                    new BackgroundSize(
                            100,
                            100,
                            true,
                            true,
                            false,
                            false
                    );

            BackgroundImage backgroundImage =
                    new BackgroundImage(
                            shipmentImage,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.CENTER,
                            imageSize
                    );

            shipHeader.setBackground(
                    new Background(
                            backgroundImage
                    )
            );

        } else {

            // Original background if image is not found
            shipHeader.setStyle(
                    "-fx-background-color:#173A57;" +
                    "-fx-background-radius:18 18 0 0;"
            );
        }

        // =====================================================
        // SHIPMENT INFORMATION
        // =====================================================

        VBox shipInfo =
                new VBox(5);

        Label shipName =
                new Label(
                        "♧  Oceanic Star"
                );

        shipName.setStyle(
                "-fx-text-fill:white;" +
                "-fx-font-size:19px;" +
                "-fx-font-weight:bold;"
        );

        Label shipId =
                new Label(
                        "Shipment ID: SHP-1024"
                );

        shipId.setStyle(
                "-fx-text-fill:#D6E2EE;"
        );

        shipInfo.getChildren().addAll(
                shipName,
                shipId
        );

        // =====================================================
        // SPACE
        // =====================================================

        Region space =
                new Region();

        HBox.setHgrow(
                space,
                Priority.ALWAYS
        );

        // =====================================================
        // STATUS
        // =====================================================

        Label status =
                new Label(
                        "In Transit"
                );

        status.setStyle(
                "-fx-background-color:#36C275;" +
                "-fx-text-fill:white;" +
                "-fx-background-radius:15px;" +
                "-fx-padding:7 13;"
        );

        shipHeader.getChildren().addAll(
                shipInfo,
                space,
                status
        );

        // =====================================================
        // SHIPMENT DETAILS
        // =====================================================

        HBox details =
                new HBox(60);

        details.setPadding(
                new Insets(22)
        );

        details.getChildren().addAll(

                detail(
                        "CARGO",
                        "▣  Electronics"
                ),

                detail(
                        "ORIGIN",
                        "⌖  Mumbai"
                ),

                detail(
                        "DESTINATION",
                        "➤  Singapore"
                ),

                detail(
                        "ETA",
                        "▣  14 Aug 2026"
                )
        );

        shipmentCard.getChildren().addAll(
                shipHeader,
                details
        );

        // =====================================================
        // STATUS
        // =====================================================

        VBox statusCard =
                new VBox(16);

        statusCard.setPadding(
                new Insets(25)
        );

        statusCard.setStyle(
                cardStyle()
        );

        Label statusTitle =
                new Label(
                        "Shipment Status"
                );

        statusTitle.setStyle(
                "-fx-font-size:18px;" +
                "-fx-font-weight:bold;"
        );

        statusCard.getChildren().add(
                statusTitle
        );

        statusCard.getChildren().addAll(

                timeline(
                        "✓",
                        "Shipment Booked",
                        "02 Aug 2026 · 09:12",
                        "done"
                ),

                timeline(
                        "✓",
                        "Cargo Loaded",
                        "04 Aug 2026 · 14:40",
                        "done"
                ),

                timeline(
                        "✓",
                        "Departed Port",
                        "05 Aug 2026 · 06:20",
                        "done"
                ),

                timeline(
                        "*",
                        "In Transit",
                        "Currently at sea · Andaman Sea",
                        "current"
                ),

                timeline(
                        "○",
                        "Arrived",
                        "Pending",
                        "pending"
                ),

                timeline(
                        "○",
                        "Delivered",
                        "Pending",
                        "pending"
                )
        );

        // =====================================================
        // ADD CONTENT
        // =====================================================

        content.getChildren().addAll(
                title,
                subtitle,
                searchCard,
                shipmentCard,
                statusCard
        );

        root.setCenter(content);

        return new Scene(root);
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

        invoice.setOnAction(e -> {
            DownloadInvoice page = new DownloadInvoice();
            com.projectui.view.login.LoginPage.showRoot(ImporterDashboard.importerStage, page.getDownloadInvoiceScene(back).getRoot());
        });

        delivery.setOnAction(e -> {
            ConfirmDelivery page = new ConfirmDelivery();
            com.projectui.view.login.LoginPage.showRoot(ImporterDashboard.importerStage, page.getConfirmDeliveryScene(back).getRoot());
        });

        return com.projectui.view.login.DashboardLayoutHelper.createSidebar(ImporterDashboard.importerStage, "Importer", "Track Shipment", menuButtons);
    }

    private HBox createHeader(String title, String breadcrumb) {
        return com.projectui.view.login.DashboardLayoutHelper.createHeader(ImporterDashboard.importerStage, title, breadcrumb, "Importer", () -> {
            Logout page = new Logout();
            page.showLogout();
        }, null);
    }

    // =========================================================
    // MENU
    // =========================================================

    private Button menuButton(
            String text,
            boolean active
    ) {

        Button button =
                new Button(text);

        button.setPrefWidth(245);

        button.setPrefHeight(45);

        button.setMinWidth(245);

        button.setMinHeight(45);

        if (active) {

            button.setStyle(
                    "-fx-background-color:#287FF0;" +
                    "-fx-text-fill:white;" +
                    "-fx-background-radius:9px;" +
                    "-fx-font-size:15px;" +
                    "-fx-font-weight:bold;" +
                    "-fx-alignment:CENTER_LEFT;" +
                    "-fx-padding:0 15;"
            );

        } else {

            button.setStyle(
                    "-fx-background-color:transparent;" +
                    "-fx-text-fill:#C1CBD6;" +
                    "-fx-font-size:15px;" +
                    "-fx-alignment:CENTER_LEFT;" +
                    "-fx-padding:0 15;"
            );
        }

        return button;
    }

    // =========================================================
    // DETAIL
    // =========================================================

    private VBox detail(
            String title,
            String value
    ) {

        VBox box =
                new VBox(5);

        Label t =
                new Label(title);

        t.setStyle(
                "-fx-text-fill:#7A8795;" +
                "-fx-font-size:10px;"
        );

        Label v =
                new Label(value);

        v.setStyle(
                "-fx-font-size:14px;" +
                "-fx-font-weight:bold;"
        );

        box.getChildren().addAll(
                t,
                v
        );

        return box;
    }

    // =========================================================
    // TIMELINE
    // =========================================================

    private HBox timeline(
            String icon,
            String title,
            String subtitle,
            String type
    ) {

        HBox row =
                new HBox(15);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        Label circle =
                new Label(icon);

        circle.setMinSize(
                34,
                34
        );

        circle.setAlignment(
                Pos.CENTER
        );

        if (type.equals("done")) {

            circle.setStyle(
                    "-fx-background-color:#38C172;" +
                    "-fx-text-fill:white;" +
                    "-fx-background-radius:50%;" +
                    "-fx-font-weight:bold;"
            );

        } else if (type.equals("current")) {

            circle.setStyle(
                    "-fx-background-color:#E4F0FF;" +
                    "-fx-text-fill:#287FF0;" +
                    "-fx-background-radius:50%;" +
                    "-fx-font-weight:bold;"
            );

        } else {

            circle.setStyle(
                    "-fx-background-color:#F1F3F5;" +
                    "-fx-text-fill:#A8B0B8;" +
                    "-fx-background-radius:50%;"
            );
        }

        VBox text =
                new VBox(2);

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-font-weight:bold;"
        );

        Label sub =
                new Label(subtitle);

        sub.setStyle(
                "-fx-text-fill:#778394;" +
                "-fx-font-size:11px;"
        );

        text.getChildren().addAll(
                titleLabel,
                sub
        );

        row.getChildren().addAll(
                circle,
                text
        );

        return row;
    }

    // =========================================================
    // CARD STYLE
    // =========================================================

    private String cardStyle() {

        return
                "-fx-background-color:white;" +
                "-fx-background-radius:18px;" +
                "-fx-border-color:#E2E7ED;" +
                "-fx-border-radius:18px;";
    }
}
