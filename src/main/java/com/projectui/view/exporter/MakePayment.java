package com.projectui.view.exporter;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.LinkedHashMap;
import java.util.Map;

public class MakePayment {

    private Scene scene;

    // Design Tokens
    private static final String BG_COLOR = "#F4F8FC";
    private static final String CARD_BG = "#FFFFFF";
    private static final String TEXT_PRIMARY = "#0F172A";
    private static final String TEXT_MUTED = "#64748B";
    private static final String PRIMARY_BLUE = "#1769FF";
    private static final String BORDER_COLOR = "#E2E8F0";

    public Scene getMakePaymentScene(Runnable callbackAction) {

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
        Text title = new Text("Make Payment");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text subtitle = new Text("Settle freight charges, port tariffs, and custom clearance fees securely.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-fill: " + TEXT_MUTED + ";");
        headerBanner.getChildren().addAll(title, subtitle);

        // Workspace Layout (2-Columns)
        HBox workspace = new HBox(24);

        // =========================================================
        // LEFT COLUMN: SHIPMENT SUMMARY & PAYMENT FORM (60% Width)
        // =========================================================
        VBox summaryCard = createCard();
        HBox.setHgrow(summaryCard, Priority.ALWAYS);

        VBox summaryHeader = new VBox(4);
        Text summaryTitle = new Text("💳  Payment & Order Summary");
        summaryTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text summarySub = new Text("Review cargo shipment charges before processing payment.");
        summarySub.setStyle("-fx-font-size: 13px; -fx-fill: " + TEXT_MUTED + ";");
        summaryHeader.getChildren().addAll(summaryTitle, summarySub);

        // Summary Grid
        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(16);
        summaryGrid.setVgap(16);

        summaryGrid.add(createSummaryTile("SHIPMENT ID", "SHP-1024"), 0, 0);
        summaryGrid.add(createSummaryTile("CARGO", "Electronics"), 1, 0);
        summaryGrid.add(createSummaryTile("CONTAINER ID", "CONT-2045"), 0, 1);
        summaryGrid.add(createSummaryTile("AMOUNT DUE", "₹1,25,000", "#1769FF"), 1, 1);

        // Payment Method Box
        VBox paymentMethodBox = new VBox(14);
        paymentMethodBox.setPadding(new Insets(20));
        paymentMethodBox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px;");

        Text methodTitle = new Text("Select Payment Method");
        methodTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        ComboBox<String> paymentMethodCombo = new ComboBox<>();
        paymentMethodCombo.getItems().addAll("UPI (GPay / PhonePe / Paytm)", "Net Banking / NEFT / RTGS", "Corporate Credit Card", "Bank Wire Transfer");
        paymentMethodCombo.setValue("UPI (GPay / PhonePe / Paytm)");
        paymentMethodCombo.setMaxWidth(Double.MAX_VALUE);
        paymentMethodCombo.setPrefHeight(42);
        paymentMethodCombo.setStyle("-fx-font-size: 14px;");

        // Pay Button
        Button payButton = new Button("🔒  Pay ₹1,25,000 Now");
        payButton.setMaxWidth(Double.MAX_VALUE);
        payButton.setPrefHeight(46);
        payButton.setStyle(
                "-fx-background-color: " + PRIMARY_BLUE + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 9px;" +
                "-fx-cursor: hand;"
        );

        // Success Alert Banner
        Label successAlert = new Label("✓   Payment Successful!\nTransaction Reference: TXN-10245 — Status: Settled & Cleared.");
        successAlert.setVisible(false);
        successAlert.setMaxWidth(Double.MAX_VALUE);
        successAlert.setPadding(new Insets(14, 18, 14, 18));
        successAlert.setStyle(
                "-fx-background-color: #DCFCE7;" +
                "-fx-text-fill: #15803D;" +
                "-fx-border-color: #86EFAC;" +
                "-fx-border-radius: 10px;" +
                "-fx-background-radius: 10px;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;"
        );

        payButton.setOnAction(e -> successAlert.setVisible(true));

        paymentMethodBox.getChildren().addAll(methodTitle, paymentMethodCombo, payButton, successAlert);

        summaryCard.getChildren().addAll(summaryHeader, summaryGrid, paymentMethodBox);

        // =========================================================
        // RIGHT COLUMN: DETAILS & STATUS (40% Width)
        // =========================================================
        VBox rightCol = new VBox(22);
        rightCol.setPrefWidth(380);
        rightCol.setMinWidth(340);

        // 1. Payment Breakdown Card
        VBox detailsCard = createCard();
        Text detailsTitle = new Text("Payment Details");
        detailsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        VBox detailsList = new VBox(10);
        detailsList.getChildren().addAll(
                createDetailRow("Shipment Reference", "SHP-1024"),
                createDetailRow("Cargo Name", "Electronics"),
                createDetailRow("Container ID", "CONT-2045"),
                createDetailRow("Freight & Customs Fee", "₹1,25,000")
        );

        HBox statusBox = new HBox();
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setPadding(new Insets(12));
        statusBox.setStyle("-fx-background-color: #DCFCE7; -fx-background-radius: 10px; -fx-border-color: #86EFAC; -fx-border-radius: 10px;");

        Text statusText = new Text("Payment Status");
        statusText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: #15803D;");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        Label statusBadge = new Label("● Paid / Settled");
        statusBadge.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 12px;");
        statusBox.getChildren().addAll(statusText, space, statusBadge);

        detailsCard.getChildren().addAll(detailsTitle, detailsList, statusBox);

        // 2. Payment Security Notice Card
        VBox securityNotice = createCard();
        Text securityTitle = new Text("🔒  Secure Maritime Gateway");
        securityTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        Text securityDesc = new Text(
                "All transactions are protected by 256-bit SSL encryption.\n" +
                "Official tax invoices and payment receipts will be generated automatically after confirmation."
        );
        securityDesc.setStyle("-fx-font-size: 12px; -fx-fill: " + TEXT_MUTED + ";");

        securityNotice.getChildren().addAll(securityTitle, securityDesc);

        rightCol.getChildren().addAll(detailsCard, securityNotice);

        workspace.getChildren().addAll(summaryCard, rightCol);

        content.getChildren().addAll(headerBanner, workspace);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + BG_COLOR + "; -fx-border-color: transparent;");

        BorderPane root = new BorderPane();
        root.setTop(createHeader("Make Payment", "Dock Tech / Exporter / Make Payment"));
        root.setLeft(sidebar);
        root.setCenter(scrollPane);

        scene = new Scene(root);
        com.projectui.view.login.LoginPage.applyInvisibleScrollbar(scene);
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

    private VBox createSummaryTile(String label, String value) {
        return createSummaryTile(label, value, TEXT_PRIMARY);
    }

    private VBox createSummaryTile(String label, String value, String valueColor) {
        VBox tile = new VBox(4);
        tile.setPadding(new Insets(14));
        GridPane.setHgrow(tile, Priority.ALWAYS);
        tile.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px;");

        Text l = new Text(label);
        l.setStyle("-fx-font-size: 11px; -fx-fill: " + TEXT_MUTED + "; -fx-font-weight: bold;");

        Text v = new Text(value);
        v.setStyle("-fx-font-size: 16px; -fx-fill: " + valueColor + "; -fx-font-weight: bold;");

        tile.getChildren().addAll(l, v);
        return tile;
    }

    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8px; -fx-border-color: #F1F5F9; -fx-border-radius: 8px;");

        Text l = new Text(label);
        l.setStyle("-fx-font-size: 12px; -fx-fill: " + TEXT_MUTED + "; -fx-font-weight: bold;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Text v = new Text(value);
        v.setStyle("-fx-font-size: 13px; -fx-fill: " + TEXT_PRIMARY + "; -fx-font-weight: bold;");

        row.getChildren().addAll(l, space, v);
        return row;
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

        track.setOnAction(e -> {
            TrackShipment p = new TrackShipment();
            com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage, p.getTrackShipmentScene(this::back).getRoot());
        });

        return com.projectui.view.login.DashboardLayoutHelper.createSidebar(ExporterDashboard.dashboardStage, "Exporter", "Make Payment", menuButtons);
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
