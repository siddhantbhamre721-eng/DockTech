package com.projectui.view.exporter;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class UploadDocument {

    private Scene scene;

    // Design Tokens
    private static final String BG_COLOR = "#F4F8FC";
    private static final String CARD_BG = "#FFFFFF";
    private static final String TEXT_PRIMARY = "#0F172A";
    private static final String TEXT_MUTED = "#64748B";
    private static final String PRIMARY_BLUE = "#1769FF";
    private static final String BORDER_COLOR = "#E2E8F0";

    public Scene getUploadDocumentScene(Runnable callbackAction) {

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
        Text title = new Text("Upload Document");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text subtitle = new Text("Upload and manage mandatory shipping, customs clearance, and commercial documentation.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-fill: " + TEXT_MUTED + ";");
        headerBanner.getChildren().addAll(title, subtitle);

        // Workspace Layout (2 Columns: Upload Left, Details & Checklist Right)
        HBox workspace = new HBox(24);

        // =========================================================
        // LEFT COLUMN: DOCUMENT UPLOAD CARD (58% Width)
        // =========================================================
        VBox uploadCard = createCard();
        HBox.setHgrow(uploadCard, Priority.ALWAYS);

        VBox uploadHeader = new VBox(4);
        Text uploadTitle = new Text("📄  Document Upload Management");
        uploadTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text uploadSub = new Text("Attach official PDF or image documents for customs inspection.");
        uploadSub.setStyle("-fx-font-size: 13px; -fx-fill: " + TEXT_MUTED + ";");
        uploadHeader.getChildren().addAll(uploadTitle, uploadSub);

        VBox formControls = new VBox(14);

        ComboBox<String> cargoCombo = new ComboBox<>();
        cargoCombo.getItems().addAll("CGO-1024 (Electronics)", "CGO-1025 (Machinery)", "CGO-1026 (Textiles)");
        cargoCombo.setValue("CGO-1024 (Electronics)");

        ComboBox<String> docTypeCombo = new ComboBox<>();
        docTypeCombo.getItems().addAll("Commercial Invoice", "Packing List", "Shipping Document", "Certificate of Origin");
        docTypeCombo.setValue("Commercial Invoice");

        formControls.getChildren().addAll(
                createFormField("Select Cargo", cargoCombo),
                createFormField("Document Type", docTypeCombo)
        );

        // Drag & Drop File Box Area
        VBox fileArea = new VBox(10);
        fileArea.setAlignment(Pos.CENTER);
        fileArea.setPadding(new Insets(24));
        fileArea.setPrefHeight(200);
        fileArea.setStyle(
                "-fx-background-color: #F8FAFC;" +
                "-fx-border-color: #CBD5E1;" +
                "-fx-border-style: dashed;" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 12px;" +
                "-fx-background-radius: 12px;"
        );

        Text fileIcon = new Text("📁");
        fileIcon.setStyle("-fx-font-size: 36px;");

        Text uploadFileTitle = new Text("Drop your file here or click below");
        uploadFileTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        Text chooseFileText = new Text("No file selected");
        chooseFileText.setStyle("-fx-font-size: 13px; -fx-fill: " + TEXT_MUTED + ";");

        Label supportedBadge = new Label("Supported Formats: PDF, JPG, PNG (Max 15MB)");
        supportedBadge.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 4px 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-font-size: 11px; -fx-text-fill: #64748B;");

        fileArea.getChildren().addAll(fileIcon, uploadFileTitle, chooseFileText, supportedBadge);

        // Choose File Button
        Button chooseFileBtn = new Button("📂  Choose File");
        chooseFileBtn.setMaxWidth(Double.MAX_VALUE);
        chooseFileBtn.setPrefHeight(40);
        chooseFileBtn.setStyle(
                "-fx-background-color: #F1F5F9;" +
                "-fx-text-fill: #334155;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8px;" +
                "-fx-cursor: hand;"
        );

        chooseFileBtn.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Document File");
            File file = chooser.showOpenDialog(ExporterDashboard.dashboardStage);
            if (file != null) {
                chooseFileText.setText("Selected: " + file.getName());
            }
        });

        // Upload Button
        Button uploadButton = new Button("⬆️  Upload Document");
        uploadButton.setMaxWidth(Double.MAX_VALUE);
        uploadButton.setPrefHeight(46);
        uploadButton.setStyle(
                "-fx-background-color: " + PRIMARY_BLUE + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 9px;" +
                "-fx-cursor: hand;"
        );

        // Upload Status Log
        VBox statusLog = new VBox(6);
        statusLog.setPadding(new Insets(14));
        statusLog.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px;");

        Label log1 = new Label("✓  Commercial Invoice — Uploaded");
        log1.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label log2 = new Label("✓  Packing List — Uploaded");
        log2.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label log3 = new Label("⚠️  Shipping Document — Pending");
        log3.setStyle("-fx-text-fill: #D97706; -fx-font-size: 12px; -fx-font-weight: bold;");

        statusLog.getChildren().addAll(log1, log2, log3);

        uploadButton.setOnAction(event -> {
            log3.setText("✓  Shipping Document — Uploaded");
            log3.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px; -fx-font-weight: bold;");
        });

        uploadCard.getChildren().addAll(uploadHeader, formControls, fileArea, chooseFileBtn, uploadButton, statusLog);

        // =========================================================
        // RIGHT COLUMN: CARGO INFO & CHECKLIST (42% Width)
        // =========================================================
        VBox rightCol = new VBox(22);
        rightCol.setPrefWidth(400);
        rightCol.setMinWidth(350);

        // 1. Cargo Summary Card
        VBox cargoCard = createCard();
        Text cargoTitle = new Text("Cargo & Shipment Reference");
        cargoTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        VBox cargoDetails = new VBox(10);
        cargoDetails.getChildren().addAll(
                createDetailRow("Cargo ID", "CGO-1024"),
                createDetailRow("Cargo Name", "Electronics"),
                createDetailRow("Gross Weight", "1,200 kg"),
                createDetailRow("Destination", "Port of Singapore")
        );

        cargoCard.getChildren().addAll(cargoTitle, cargoDetails);

        // 2. Document Checklist & Completion Tracker Card
        VBox checklistCard = createCard();
        Text checkTitle = new Text("Document Checklist");
        checkTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        // Progress Bar
        VBox progressBox = new VBox(6);
        HBox progressHeader = new HBox();
        Text progressText = new Text("Documents Completed");
        progressText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        Text progressVal = new Text("2 / 3 Required");
        progressVal.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: " + PRIMARY_BLUE + ";");
        progressHeader.getChildren().addAll(progressText, space, progressVal);

        ProgressBar pb = new ProgressBar(0.66);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setPrefHeight(10);
        pb.setStyle("-fx-accent: " + PRIMARY_BLUE + ";");

        progressBox.getChildren().addAll(progressHeader, pb);

        VBox checklistItems = new VBox(10);
        checklistItems.getChildren().addAll(
                createChecklistItem("Commercial Invoice", "Uploaded", true),
                createChecklistItem("Packing List", "Uploaded", true),
                createChecklistItem("Shipping Document", "Required", false)
        );

        checklistCard.getChildren().addAll(checkTitle, progressBox, checklistItems);

        rightCol.getChildren().addAll(cargoCard, checklistCard);

        workspace.getChildren().addAll(uploadCard, rightCol);

        content.getChildren().addAll(headerBanner, workspace);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + BG_COLOR + "; -fx-border-color: transparent;");

        BorderPane root = new BorderPane();
        root.setTop(createHeader("Upload Document", "Dock Tech / Exporter / Upload Document"));
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

    private VBox createFormField(String labelText, javafx.scene.Node field) {
        VBox box = new VBox(6);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        if (field instanceof ComboBox) {
            ComboBox<?> cb = (ComboBox<?>) field;
            cb.setPrefHeight(42);
            cb.setMaxWidth(Double.MAX_VALUE);
            cb.setStyle("-fx-font-size: 14px; -fx-padding: 2px 6px;");
        }

        box.getChildren().addAll(label, field);
        return box;
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

    private HBox createChecklistItem(String title, String status, boolean uploaded) {
        HBox item = new HBox();
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12));
        item.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px;");

        Label icon = new Label(uploaded ? "✓" : "⚠️");
        icon.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (uploaded ? "#16A34A" : "#D97706") + "; -fx-font-weight: bold;");

        VBox info = new VBox(2);
        info.setPadding(new Insets(0, 0, 0, 8));
        Text t = new Text(title);
        t.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        info.getChildren().add(t);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label badge = new Label(status);
        badge.setStyle("-fx-background-color: " + (uploaded ? "#DCFCE7" : "#FEF3C7") + "; -fx-text-fill: " + (uploaded ? "#15803D" : "#B45309") + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 12px;");

        item.getChildren().addAll(icon, info, space, badge);
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
            RegisterCargo page = new RegisterCargo();
            com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage, page.getRegisterCargoScene(this::back).getRoot());
        });

        book.setOnAction(e -> {
            BookContainer page = new BookContainer();
            com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage, page.getBookContainerScene(this::back).getRoot());
        });

        payment.setOnAction(e -> {
            MakePayment page = new MakePayment();
            com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage, page.getMakePaymentScene(this::back).getRoot());
        });

        track.setOnAction(e -> {
            TrackShipment page = new TrackShipment();
            com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage, page.getTrackShipmentScene(this::back).getRoot());
        });

        return com.projectui.view.login.DashboardLayoutHelper.createSidebar(ExporterDashboard.dashboardStage, "Exporter", "Upload Document", menuButtons);
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
