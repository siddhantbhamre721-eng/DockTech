package com.projectui.view.exporter;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.LinkedHashMap;
import java.util.Map;

public class TrackShipment {

    private Scene scene;

    // Design Tokens
    private static final String BG_COLOR = "#F4F8FC";
    private static final String CARD_BG = "#FFFFFF";
    private static final String TEXT_PRIMARY = "#0F172A";
    private static final String TEXT_MUTED = "#64748B";
    private static final String PRIMARY_BLUE = "#1769FF";
    private static final String BORDER_COLOR = "#E2E8F0";

    public Scene getTrackShipmentScene(Runnable callbackAction) {

        // =========================================================
        // SIDEBAR - PRESERVED
        // =========================================================
        VBox sidebar = createSidebar();

        // =========================================================
        // MAIN WORKSPACE
        // =========================================================
        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 32, 36, 32));
        content.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // Header Banner
        VBox headerBanner = new VBox(6);
        Text title = new Text("Track Shipment");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text subtitle = new Text("Real-time vessel tracking, route progress, and shipment milestones.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-fill: " + TEXT_MUTED + ";");
        headerBanner.getChildren().addAll(title, subtitle);

        // =========================================================
        // 1. TOP SHIPMENT SEARCH CARD
        // =========================================================
        VBox searchCard = createCard();
        searchCard.setPadding(new Insets(20, 24, 20, 24));

        Text searchTitle = new Text("📥  Live Shipment Tracker");
        searchTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        HBox searchBar = new HBox(12);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        TextField shipmentInput = new TextField("SHP-1024");
        shipmentInput.setPromptText("Enter Shipment ID (e.g. SHP-1024)");
        shipmentInput.setPrefHeight(44);
        HBox.setHgrow(shipmentInput, Priority.ALWAYS);
        shipmentInput.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-border-color: #CBD5E1;" +
                "-fx-border-radius: 8px;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 8px 14px;" +
                "-fx-font-size: 14px;"
        );

        Button trackBtn = new Button("🔍  Track Shipment");
        trackBtn.setPrefHeight(44);
        trackBtn.setPrefWidth(160);
        trackBtn.setStyle(
                "-fx-background-color: " + PRIMARY_BLUE + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8px;" +
                "-fx-cursor: hand;"
        );

        searchBar.getChildren().addAll(shipmentInput, trackBtn);
        searchCard.getChildren().addAll(searchTitle, searchBar);

        // =========================================================
        // 2. MAIN TRACKING WORKSPACE (2 Columns)
        // =========================================================
        HBox workspace = new HBox(24);

        // LEFT COLUMN: SHIPMENT DETAILS & TIMELINE (60% Width)
        VBox leftCol = new VBox(22);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        // A. Shipment Information Grid Card
        VBox infoCard = createCard();
        Text infoTitle = new Text("Shipment Overview");
        infoTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(16);
        infoGrid.setVgap(16);

        infoGrid.add(createTile("SHIPMENT ID", "SHP-1024"), 0, 0);
        infoGrid.add(createTile("CARGO TYPE", "Electronics"), 1, 0);
        infoGrid.add(createTile("CONTAINER ID", "CONT-2045"), 0, 1);
        infoGrid.add(createTile("ORIGIN PORT", "Port of Mumbai"), 1, 1);
        infoGrid.add(createTile("DESTINATION", "Port of Singapore"), 0, 2);
        infoGrid.add(createTile("ESTIMATED ARRIVAL", "14 Aug 2026"), 1, 2);

        infoCard.getChildren().addAll(infoTitle, infoGrid);

        // B. Shipment Progress Timeline Card
        VBox timelineCard = createCard();
        Text timelineTitle = new Text("Shipment Progress Timeline");
        timelineTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text timelineSub = new Text("Milestones and clearance stages for SHP-1024.");
        timelineSub.setStyle("-fx-font-size: 13px; -fx-fill: " + TEXT_MUTED + ";");

        VBox stagesList = new VBox(14);
        stagesList.getChildren().addAll(
                createTimelineStage("Cargo Registered", "08 Aug 2026, 09:30 AM — Port of Mumbai", true, true),
                createTimelineStage("Container Booked & Loaded", "09 Aug 2026, 02:15 PM — JNPT Terminal", true, true),
                createTimelineStage("Customs Cleared", "10 Aug 2026, 11:00 AM — Custom Inspection Passed", true, true),
                createTimelineStage("In Transit (Vessel Departed)", "11 Aug 2026, 06:00 AM — MV Oceanic Star", true, false),
                createTimelineStage("Arrived at Port of Singapore", "ETA 14 Aug 2026 — Pending Berth", false, false),
                createTimelineStage("Delivered to Importer", "ETA 15 Aug 2026 — Pending Final Gate Out", false, false)
        );

        timelineCard.getChildren().addAll(timelineTitle, timelineSub, stagesList);

        leftCol.getChildren().addAll(infoCard, timelineCard);

        // RIGHT COLUMN: ROUTE VISUALIZATION & LIVE STATUS (40% Width)
        VBox rightCol = new VBox(22);
        rightCol.setPrefWidth(380);
        rightCol.setMinWidth(340);

        // A. Vessel Route Card
        VBox routeCard = createCard();
        Text routeTitle = new Text("🚢  Live Vessel Route");
        routeTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        VBox routeDiagram = new VBox(16);
        routeDiagram.setPadding(new Insets(20));
        routeDiagram.setStyle("-fx-background-color: #F0F7FF; -fx-background-radius: 12px; -fx-border-color: #BAE6FD; -fx-border-radius: 12px;");

        HBox originNode = createRouteNode("Port of Mumbai (IN)", "Departed 11 Aug", "#16A34A");
        Region vLine1 = createVerticalLine(true);
        HBox currentLocationNode = createRouteNode("Arabian Sea (18.9°N, 71.5°E)", "Current Location • MV Oceanic Star", PRIMARY_BLUE);
        Region vLine2 = createVerticalLine(false);
        HBox destNode = createRouteNode("Port of Singapore (SG)", "Destination • ETA 14 Aug", "#64748B");

        routeDiagram.getChildren().addAll(originNode, vLine1, currentLocationNode, vLine2, destNode);

        routeCard.getChildren().addAll(routeTitle, routeDiagram);

        // B. Status Alerts Card
        VBox alertCard = createCard();
        Text alertTitle = new Text("Shipment Status Alerts");
        alertTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        VBox alerts = new VBox(10);
        alerts.getChildren().addAll(
                createStatusAlert("✓  In Transit — Vessel operating on schedule", "#DCFCE7", "#15803D", "#86EFAC"),
                createStatusAlert("⚠️  Weather Notice — Normal sea conditions reported", "#FFF7ED", "#C2410C", "#FDBA74")
        );

        alertCard.getChildren().addAll(alertTitle, alerts);

        rightCol.getChildren().addAll(routeCard, alertCard);

        workspace.getChildren().addAll(leftCol, rightCol);

        content.getChildren().addAll(headerBanner, searchCard, workspace);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + BG_COLOR + "; -fx-border-color: transparent;");

        BorderPane root = new BorderPane();
        root.setTop(createHeader("Track Shipment", "Dock Tech / Exporter / Track Shipment"));
        root.setLeft(sidebar);
        root.setCenter(scrollPane);

        scene = new Scene(root);
        return scene;
    }

    // =========================================================
    // HELPER COMPONENTS
    // =========================================================

    private VBox createCard() {
        VBox card = new VBox(18);
        card.setPadding(new Insets(24));
        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                "-fx-background-radius: 16px;" +
                "-fx-border-color: " + BORDER_COLOR + ";" +
                "-fx-border-radius: 16px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.04), 10, 0, 0, 4);"
        );
        return card;
    }

    private VBox createTile(String label, String value) {
        VBox tile = new VBox(4);
        tile.setPadding(new Insets(12, 14, 12, 14));
        GridPane.setHgrow(tile, Priority.ALWAYS);
        tile.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8px; -fx-border-color: #F1F5F9; -fx-border-radius: 8px;");

        Text l = new Text(label);
        l.setStyle("-fx-font-size: 11px; -fx-fill: " + TEXT_MUTED + "; -fx-font-weight: bold;");

        Text v = new Text(value);
        v.setStyle("-fx-font-size: 14px; -fx-fill: " + TEXT_PRIMARY + "; -fx-font-weight: bold;");

        tile.getChildren().addAll(l, v);
        return tile;
    }

    private HBox createTimelineStage(String stageTitle, String timestamp, boolean completed, boolean showCheck) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);

        Label circle = new Label(showCheck ? "✓" : (completed ? "●" : "○"));
        circle.setMinSize(28, 28);
        circle.setAlignment(Pos.CENTER);

        if (completed) {
            circle.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50; -fx-font-size: 12px;");
        } else {
            circle.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #64748B; -fx-font-weight: bold; -fx-background-radius: 50; -fx-font-size: 12px;");
        }

        VBox text = new VBox(2);
        Text titleText = new Text(stageTitle);
        titleText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: " + (completed ? TEXT_PRIMARY : TEXT_MUTED) + ";");

        Text timeText = new Text(timestamp);
        timeText.setStyle("-fx-font-size: 12px; -fx-fill: " + TEXT_MUTED + ";");

        text.getChildren().addAll(titleText, timeText);
        row.getChildren().addAll(circle, text);

        return row;
    }

    private HBox createRouteNode(String portName, String sub, String color) {
        HBox node = new HBox(10);
        node.setAlignment(Pos.CENTER_LEFT);

        Label dot = new Label("●");
        dot.setStyle("-fx-font-size: 18px; -fx-text-fill: " + color + ";");

        VBox info = new VBox(2);
        Text p = new Text(portName);
        p.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text s = new Text(sub);
        s.setStyle("-fx-font-size: 11px; -fx-fill: " + TEXT_MUTED + ";");
        info.getChildren().addAll(p, s);

        node.getChildren().addAll(dot, info);
        return node;
    }

    private Region createVerticalLine(boolean active) {
        Region line = new Region();
        line.setPrefWidth(3);
        line.setPrefHeight(20);
        line.setStyle("-fx-background-color: " + (active ? "#16A34A" : "#CBD5E1") + "; -fx-margin: 0 0 0 5;");
        return line;
    }

    private HBox createStatusAlert(String text, String bg, String fg, String border) {
        HBox item = new HBox();
        item.setPadding(new Insets(12));
        item.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + border + "; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + fg + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        item.getChildren().add(label);
        return item;
    }

    // =========================================================
    // SIDEBAR & NAVIGATION — PRESERVED
    // =========================================================
    private VBox createSidebar() {
        Map<String, Button> menuButtons = new LinkedHashMap<>();
        Button dashboard = new Button("🏠   Dashboard");
        Button register = new Button("📦  Register Cargo");
        Button book = new Button("🚢   Book Container");
        Button upload = new Button("📄   Upload Document");
        Button payment = new Button("💳   Make Payment");
        Button track = new Button("📥   Track Shipment");

        menuButtons.put("Dashboard", dashboard);
        menuButtons.put("Register Cargo", register);
        menuButtons.put("Book Container", book);
        menuButtons.put("Upload Document", upload);
        menuButtons.put("Make Payment", payment);
        menuButtons.put("Track Shipment", track);

        dashboard.setOnAction(e -> back());
        register.setOnAction(e -> {
            RegisterCargo p = new RegisterCargo();
            com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage, p.getRegisterCargoScene(this::back).getRoot());
        });

        book.setOnAction(e -> {
            BookContainer p = new BookContainer();
            com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage, p.getBookContainerScene(this::back).getRoot());
        });

        upload.setOnAction(e -> {
            UploadDocument p = new UploadDocument();
            com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage, p.getUploadDocumentScene(this::back).getRoot());
        });

        payment.setOnAction(e -> {
            MakePayment p = new MakePayment();
            com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage, p.getMakePaymentScene(this::back).getRoot());
        });

        return com.projectui.view.login.DashboardLayoutHelper.createSidebar(ExporterDashboard.dashboardStage, "Exporter", "Track Shipment", menuButtons);
    }

    private void back() {
        new ExporterDashboard().start(ExporterDashboard.dashboardStage);
    }

    private HBox createHeader(String pageTitle, String breadcrumb) {
        return com.projectui.view.login.DashboardLayoutHelper.createHeader(ExporterDashboard.dashboardStage, pageTitle, breadcrumb, "Exporter", () -> {
            Logout page = new Logout();
            page.showLogout();
        }, null);
    }
}
