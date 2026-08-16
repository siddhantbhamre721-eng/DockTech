package com.projectui.view.importer;



import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ImporterDashboard extends Application {

    public static Stage importerStage;

    private Scene dashboardScene;

    private static boolean styleInitialized = false;

    @Override
    public void start(Stage stage) {

        importerStage = stage;

        if (!styleInitialized) {

            importerStage.initStyle(
                    StageStyle.UNDECORATED
            );

            styleInitialized = true;
        }

        // =====================================================
        // SIDEBAR
        // =====================================================

        VBox sidebar =
                new VBox(14);

        sidebar.setPadding(
                new Insets(22, 14, 20, 14)
        );

        sidebar.setPrefWidth(285);

        sidebar.setStyle(
                "-fx-background-color:#081C32;"
        );

        // =====================================================
        // LOGO
        // =====================================================

        Label logo =
                new Label("⚓  DOCK TECH");

        logo.setStyle(
                "-fx-text-fill:white;" +
                "-fx-font-size:18px;" +
                "-fx-font-weight:bold;"
        );

        Label logoSubtitle =
                new Label(
                        "SMART PORT\nOPERATIONS"
                );

        logoSubtitle.setStyle(
                "-fx-text-fill:#9AAAC0;" +
                "-fx-font-size:9px;" +
                "-fx-font-weight:bold;"
        );

        VBox logoBox =
                new VBox(
                        3,
                        logo,
                        logoSubtitle
                );

        logoBox.setPadding(
                new Insets(0, 0, 22, 10)
        );

        // =====================================================
        // MENU
        // =====================================================

        Button dashboard =
                createMenuButton(
                        "🏠   Dashboard",
                        true
                );

        Button track =
                createMenuButton(
                        "📥   Track Shipment",
                        false
                );

        Button invoice =
                createMenuButton(
                        "📄   Download Invoice",
                        false
                );

        Button delivery =
                createMenuButton(
                        "📦   Confirm Delivery",
                        false
                );

        Button logout =
                createMenuButton(
                        "⇥   Logout",
                        false
                );

        // =====================================================
        // DASHBOARD
        // =====================================================

        dashboard.setOnAction(
                event -> showDashboard()
        );

        // =====================================================
        // TRACK SHIPMENT
        // =====================================================

        track.setOnAction(event -> {

            TrackShipment page =
                    new TrackShipment();

            importerStage.setScene(
                    page.getTrackShipmentScene(
                            this::showDashboard
                    )
            );

            importerStage.setFullScreen(true);
        });

        // =====================================================
        // DOWNLOAD INVOICE
        // =====================================================

        invoice.setOnAction(event -> {

            DownloadInvoice page =
                    new DownloadInvoice();

            importerStage.setScene(
                    page.getDownloadInvoiceScene(
                            this::showDashboard
                    )
            );

            importerStage.setFullScreen(true);
        });

        // =====================================================
        // CONFIRM DELIVERY
        // =====================================================

        delivery.setOnAction(event -> {

            ConfirmDelivery page =
                    new ConfirmDelivery();

            importerStage.setScene(
                    page.getConfirmDeliveryScene(
                            this::showDashboard
                    )
            );

            importerStage.setFullScreen(true);
        });

        // =====================================================
        // LOGOUT
        // =====================================================

        logout.setOnAction(event -> {

            Logout page =
                    new Logout();

            page.showLogout();
        });

        sidebar.getChildren().addAll(
                logoBox,
                dashboard,
                track,
                invoice,
                delivery,
                logout
        );

        // =====================================================
        // MAIN CONTENT
        // =====================================================

        VBox content =
                new VBox(22);

        content.setPadding(
                new Insets(28, 40, 30, 40)
        );

        content.setStyle(
                "-fx-background-color:#F4F8FC;"
        );

        // =====================================================
        // HEADER
        // =====================================================

        content.getChildren().add(
                createHeader(
                        "Importer Dashboard",
                        "Dock Tech / Importer / Dashboard"
                )
        );

        // =====================================================
        // CENTER TITLE
        // =====================================================

        Text title =
                new Text(
                        "Importer Dashboard"
                );

        title.setStyle(
                "-fx-font-size:28px;" +
                "-fx-font-weight:bold;" +
                "-fx-fill:#101A2B;"
        );

        Text subtitle =
                new Text(
                        "Track your incoming shipments and deliveries."
                );

        subtitle.setStyle(
                "-fx-font-size:15px;" +
                "-fx-fill:#778394;"
        );

        // =====================================================
        // STAT CARDS
        // =====================================================

        HBox stats =
                new HBox(16);

        stats.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox activeStat =
                createStatCard(
                        "3",
                        "Active\nShipments",
                        "🚢",
                        "#DDEEFF",
                        "#087BE8"
                );

        VBox transitStat =
                createStatCard(
                        "2",
                        "In Transit",
                        "➤",
                        "#D7F6F7",
                        "#00AFC1"
                );

        VBox arrivingStat =
                createStatCard(
                        "1",
                        "Arriving Soon",
                        "◷",
                        "#FFEBD2",
                        "#F27A16"
                );

        VBox deliveredStat =
                createStatCard(
                        "8",
                        "Delivered",
                        "📦",
                        "#DDF6E4",
                        "#18A558"
                );

        stats.getChildren().addAll(
                activeStat,
                transitStat,
                arrivingStat,
                deliveredStat
        );

        // =====================================================
        // LEFT SIDE - ACTIVE SHIPMENTS
        // =====================================================

        VBox activeSection =
                new VBox(14);

        activeSection.setPrefWidth(560);

        Label activeTitle =
                new Label(
                        "Active Shipments"
                );

        activeTitle.setStyle(
                "-fx-font-size:20px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#111827;"
        );

        HBox oceanicStar =
                shipment(
                        "Oceanic Star",
                        "Mumbai → Singapore",
                        "ETA: 14 Aug",
                        "In Transit",
                        "#DDEEFF",
                        "#16701c",
                        "🚢"
                );

        HBox seaVoyager =
                shipment(
                        "Sea Voyager",
                        "Chennai → Mumbai",
                        "ETA: 13 Aug",
                        "Arriving Soon",
                        "#FFEBD2",
                        "#F27A16",
                        "🚢"
                );

        activeSection.getChildren().addAll(
                activeTitle,
                oceanicStar,
                seaVoyager
        );

        // =====================================================
        // RIGHT SIDE - RECENT DELIVERY
        // =====================================================

        VBox recentSection =
                new VBox(14);

        recentSection.setPrefWidth(270);

        Label recentTitle =
                new Label(
                        "Recent Delivery"
                );

        recentTitle.setStyle(
                "-fx-font-size:20px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#111827;"
        );

        VBox recentCard =
                createRecentDelivery();

        recentSection.getChildren().addAll(
                recentTitle,
                recentCard
        );

        // =====================================================
        // TWO COLUMN SECTION
        // =====================================================

        HBox lower =
                new HBox(25);

        lower.setAlignment(
                Pos.TOP_LEFT
        );

        lower.getChildren().addAll(
                activeSection,
                recentSection
        );

        // =====================================================
        // IMPORTANT ALERT
        // =====================================================

        Label alertTitle =
                new Label(
                        "Important Alert"
                );

        alertTitle.setStyle(
                "-fx-font-size:20px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#111827;"
        );

        HBox alert =
                createAlert();

        // =====================================================
        // ADD CENTER CONTENT
        // =====================================================

        content.getChildren().addAll(
                title,
                subtitle,
                stats,
                lower,
                alertTitle,
                alert
        );

        // =====================================================
        // ROOT
        // =====================================================

        BorderPane root =
                new BorderPane();

        root.setLeft(sidebar);

        root.setCenter(content);

        dashboardScene =
                new Scene(root);

        importerStage.setScene(
                dashboardScene
        );

        importerStage.setTitle(
                "Dock Tech - Importer"
        );

        importerStage.setFullScreen(true);

        importerStage.show();
    }

    // =========================================================
    // SHOW DASHBOARD
    // =========================================================

    public void showDashboard() {

        importerStage.setScene(
                dashboardScene
        );

        importerStage.setFullScreen(true);
    }

    // =========================================================
    // MENU BUTTON
    // =========================================================

    private Button createMenuButton(
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
                    "-fx-padding:0 15;" +
                    "-fx-cursor:hand;" +
                    "-fx-focus-color:transparent;" +
                    "-fx-faint-focus-color:transparent;"
            );

        } else {

            button.setStyle(
                    "-fx-background-color:transparent;" +
                    "-fx-text-fill:#C1CBD6;" +
                    "-fx-font-size:15px;" +
                    "-fx-alignment:CENTER_LEFT;" +
                    "-fx-padding:0 15;" +
                    "-fx-cursor:hand;" +
                    "-fx-focus-color:transparent;" +
                    "-fx-faint-focus-color:transparent;"
            );
        }

        return button;
    }

    // =========================================================
    // NEW STAT CARD
    // =========================================================

    private VBox createStatCard(
            String number,
            String text,
            String icon,
            String iconBackground,
            String iconColor
    ) {

        VBox card =
                new VBox();

        card.setPrefWidth(205);

        card.setPrefHeight(115);

        card.setPadding(
                new Insets(16)
        );

        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:14px;" +
                "-fx-border-color:#DDE3E9;" +
                "-fx-border-radius:14px;"
        );

        HBox row =
                new HBox(14);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        // -----------------------------------------------------
        // ICON
        // -----------------------------------------------------

        Label iconLabel =
                new Label(icon);

        iconLabel.setMinSize(
                45,
                45
        );

        iconLabel.setMaxSize(
                45,
                45
        );

        iconLabel.setAlignment(
                Pos.CENTER
        );

        iconLabel.setStyle(
                "-fx-background-color:" +
                iconBackground +
                ";" +
                "-fx-background-radius:12px;" +
                "-fx-text-fill:" +
                iconColor +
                ";" +
                "-fx-font-size:22px;" +
                "-fx-font-weight:bold;"
        );

        // -----------------------------------------------------
        // NUMBER + TEXT
        // -----------------------------------------------------

        VBox info =
                new VBox(1);

        Label numberLabel =
                new Label(number);

        numberLabel.setStyle(
                "-fx-font-size:25px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#111827;"
        );

        Label textLabel =
                new Label(text);

        textLabel.setStyle(
                "-fx-font-size:13px;" +
                "-fx-text-fill:#6B7280;"
        );

        info.getChildren().addAll(
                numberLabel,
                textLabel
        );

        row.getChildren().addAll(
                iconLabel,
                info
        );

        card.getChildren().add(
                row
        );

        return card;
    }

    // =========================================================
    // SHIPMENT CARD
    // =========================================================

    private HBox shipment(
            String name,
            String route,
            String eta,
            String status,
            String iconBackground,
            String iconColor,
            String icon
    ) {

        HBox box =
                new HBox(16);

        box.setAlignment(
                Pos.CENTER_LEFT
        );

        box.setPadding(
                new Insets(16, 20, 16, 20)
        );

        box.setPrefHeight(100);

        box.setMaxWidth(
                Double.MAX_VALUE
        );

        box.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:14px;" +
                "-fx-border-color:#DDE3E9;" +
                "-fx-border-radius:14px;"
        );

        // -----------------------------------------------------
        // ICON
        // -----------------------------------------------------

        Label iconLabel =
                new Label(icon);

        iconLabel.setMinSize(
                50,
                50
        );

        iconLabel.setMaxSize(
                50,
                50
        );

        iconLabel.setAlignment(
                Pos.CENTER
        );

        iconLabel.setStyle(
                "-fx-background-color:" +
                iconBackground +
                ";" +
                "-fx-background-radius:13px;" +
                "-fx-text-fill:" +
                iconColor +
                ";" +
                "-fx-font-size:23px;" +
                "-fx-font-weight:bold;"
        );

        // -----------------------------------------------------
        // SHIPMENT INFORMATION
        // -----------------------------------------------------

        VBox information =
                new VBox(4);

        Label nameLabel =
                new Label(name);

        nameLabel.setStyle(
                "-fx-font-size:16px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#111827;"
        );

        Label routeLabel =
                new Label(route);

        routeLabel.setStyle(
                "-fx-font-size:14px;" +
                "-fx-text-fill:#6B7280;"
        );

        information.getChildren().addAll(
                nameLabel,
                routeLabel
        );

        // -----------------------------------------------------
        // SPACER
        // -----------------------------------------------------

        Region space =
                new Region();

        HBox.setHgrow(
                space,
                Priority.ALWAYS
        );

        // -----------------------------------------------------
        // RIGHT SIDE
        // -----------------------------------------------------

        VBox right =
                new VBox(6);

        right.setAlignment(
                Pos.CENTER_RIGHT
        );

        Label statusLabel =
                new Label(status);

        if (status.equals("In Transit")) {

            statusLabel.setStyle(
                    "-fx-background-color:#E5F0FF;" +
                    "-fx-text-fill:#1670C9;" +
                    "-fx-background-radius:6px;" +
                    "-fx-padding:5px 9px;" +
                    "-fx-font-size:12px;"
            );

        } else {

            statusLabel.setStyle(
                    "-fx-background-color:#FFF0DC;" +
                    "-fx-text-fill:#C65D08;" +
                    "-fx-background-radius:6px;" +
                    "-fx-padding:5px 9px;" +
                    "-fx-font-size:12px;"
            );
        }

        Label etaLabel =
                new Label(eta);

        etaLabel.setStyle(
                "-fx-font-size:12px;" +
                "-fx-text-fill:#6B7280;"
        );

        right.getChildren().addAll(
                statusLabel,
                etaLabel
        );

        box.getChildren().addAll(
                iconLabel,
                information,
                space,
                right
        );

        return box;
    }

    // =========================================================
    // RECENT DELIVERY
    // =========================================================

    private VBox createRecentDelivery() {

        VBox card =
                new VBox(12);

        card.setPrefWidth(270);

        card.setPrefHeight(225);

        card.setPadding(
                new Insets(20)
        );

        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:14px;" +
                "-fx-border-color:#DDE3E9;" +
                "-fx-border-radius:14px;"
        );

        // -----------------------------------------------------
        // ICON
        // -----------------------------------------------------

        Label icon =
                new Label("✓");

        icon.setMinSize(
                50,
                50
        );

        icon.setMaxSize(
                50,
                50
        );

        icon.setAlignment(
                Pos.CENTER
        );

        icon.setStyle(
                "-fx-background-color:#DDF6E4;" +
                "-fx-background-radius:13px;" +
                "-fx-text-fill:#18A558;" +
                "-fx-font-size:24px;" +
                "-fx-font-weight:bold;"
        );

        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------

        Label status =
                new Label(
                        "Delivered"
                );

        status.setStyle(
                "-fx-background-color:#DDF6E4;" +
                "-fx-text-fill:#087C35;" +
                "-fx-background-radius:6px;" +
                "-fx-padding:5px 9px;" +
                "-fx-font-size:12px;"
        );

        // -----------------------------------------------------
        // INVOICE
        // -----------------------------------------------------

        Label invoice =
                new Label(
                        "INV-1024"
                );

        invoice.setStyle(
                "-fx-font-size:18px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#111827;"
        );

        // -----------------------------------------------------
        // SHIP NAME
        // -----------------------------------------------------

        Label ship =
                new Label(
                        "Oceanic Star"
                );

        ship.setStyle(
                "-fx-font-size:14px;" +
                "-fx-text-fill:#6B7280;"
        );

        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        Label date =
                new Label(
                        "▣  11 Aug 2026"
                );

        date.setStyle(
                "-fx-font-size:13px;" +
                "-fx-text-fill:#6B7280;"
        );

        card.getChildren().addAll(
                icon,
                status,
                invoice,
                ship,
                date
        );

        return card;
    }

    // =========================================================
    // IMPORTANT ALERT
    // =========================================================

    private HBox createAlert() {

        HBox alert =
                new HBox(15);

        alert.setAlignment(
                Pos.CENTER_LEFT
        );

        alert.setPadding(
                new Insets(15, 20, 15, 20)
        );

        alert.setMaxWidth(
                Double.MAX_VALUE
        );

        alert.setStyle(
                "-fx-background-color:#FFE9C9;" +
                "-fx-background-radius:12px;"
        );

        // -----------------------------------------------------
        // ALERT ICON
        // -----------------------------------------------------

        Label alertIcon =
                new Label("!");

        alertIcon.setMinSize(
                40,
                40
        );

        alertIcon.setMaxSize(
                40,
                40
        );

        alertIcon.setAlignment(
                Pos.CENTER
        );

        alertIcon.setStyle(
                "-fx-background-color:#F47712;" +
                "-fx-background-radius:50%;" +
                "-fx-text-fill:white;" +
                "-fx-font-size:20px;" +
                "-fx-font-weight:bold;"
        );

        // -----------------------------------------------------
        // MESSAGE
        // -----------------------------------------------------

        Label alertText =
                new Label(
                        "Shipment CGO-1287 arrival delayed by 4 hours."
                );

        alertText.setStyle(
                "-fx-font-size:14px;" +
                "-fx-text-fill:#8A3412;"
        );

        // -----------------------------------------------------
        // SPACER
        // -----------------------------------------------------

        Region space =
                new Region();

        HBox.setHgrow(
                space,
                Priority.ALWAYS
        );

        // -----------------------------------------------------
        // CLOSE
        // -----------------------------------------------------

        Button close =
                new Button("×");

        close.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-text-fill:#9A4819;" +
                "-fx-font-size:20px;" +
                "-fx-cursor:hand;" +
                "-fx-focus-color:transparent;" +
                "-fx-faint-focus-color:transparent;"
        );

        close.setOnAction(
                event -> alert.setVisible(false)
        );

        alert.getChildren().addAll(
                alertIcon,
                alertText,
                space,
                close
        );

        return alert;
    }

    // =========================================================
    // HEADER
    // =========================================================

    private HBox createHeader(
            String pageTitle,
            String breadcrumb
    ) {

        HBox header =
                new HBox(15);

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        header.setPadding(
                new Insets(12,22,12,25)
        );

        header.setPrefHeight(65);

        header.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#E5EBF3;" +
                "-fx-border-width:0 0 1 0;"
        );

        VBox pageInfo =
                new VBox(3);

        Label title =
                new Label(pageTitle);

        title.setStyle(
                "-fx-text-fill:#111827;" +
                "-fx-font-size:17;" +
                "-fx-font-weight:bold;"
        );

        Label bc =
                new Label(breadcrumb);

        bc.setStyle(
                "-fx-text-fill:#8A93A1;" +
                "-fx-font-size:10;"
        );

        pageInfo.getChildren().addAll(
                title,
                bc
        );

        Region space =
                new Region();

        HBox.setHgrow(
                space,
                Priority.ALWAYS
        );

        Label status =
                new Label(
                        "System Operational"
                );

        status.setStyle(
                "-fx-text-fill:#168548;" +
                "-fx-background-color:#E7F7EE;" +
                "-fx-background-radius:14;" +
                "-fx-padding:6 10;" +
                "-fx-font-size:10;" +
                "-fx-font-weight:bold;"
        );

        String date =
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "dd MMM yyyy"
                        )
                );

        Label dateLabel =
                new Label(date);

        dateLabel.setStyle(
                "-fx-text-fill:#4B5563;" +
                "-fx-font-size:11;" +
                "-fx-font-weight:bold;"
        );

        Label notification =
                new Label("🔔");

        notification.setStyle(
                "-fx-text-fill:#111827;" +
                "-fx-font-size:17;"
        );

        Label avatar =
                new Label("I");

        avatar.setMinSize(
                32,
                32
        );

        avatar.setAlignment(
                Pos.CENTER
        );

        avatar.setStyle(
                "-fx-background-color:#DBEAFE;" +
                "-fx-background-radius:50%;" +
                "-fx-text-fill:#0F5FFF;" +
                "-fx-font-weight:bold;"
        );

        VBox profile =
                new VBox(1);

        Label user =
                new Label("Dock Tech Importer");

        user.setStyle(
                "-fx-text-fill:#111827;" +
                "-fx-font-size:11;" +
                "-fx-font-weight:bold;"
        );

        Label role =
                new Label("Importer");

        role.setStyle(
                "-fx-text-fill:#8A93A1;" +
                "-fx-font-size:9;"
        );

        profile.getChildren().addAll(
                user,
                role
        );

        header.getChildren().addAll(
                pageInfo,
                space,
                status,
                dateLabel,
                notification,
                avatar,
                profile
        );

        return header;
    }
}
