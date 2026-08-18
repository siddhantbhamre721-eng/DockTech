package com.projectui.view.exporter;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

import com.projectui.dao.CargoDao;
import com.projectui.dao.ContainerDao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BookContainer {

    private Scene scene;

    private final CargoDao cargoDao = new CargoDao();
    private final ContainerDao containerDao = new ContainerDao();

    // Selected Cargo Data (retrieved dynamically or fallback)
    private String selectedCargoId = "CGO-1024";
    private String selectedCargoName = "Electronics";
    private String selectedCargoType = "General Cargo";
    private String selectedCargoWeight = "1,200 kg";
    private String selectedOriginPort = "Port of Mumbai";
    private String selectedDestinationPort = "Port of Singapore";

    // Design Tokens
    private static final String BG_COLOR = "#F4F8FC";
    private static final String CARD_BG = "#FFFFFF";
    private static final String TEXT_PRIMARY = "#0F172A";
    private static final String TEXT_MUTED = "#64748B";
    private static final String PRIMARY_BLUE = "#1769FF";
    private static final String BORDER_COLOR = "#E2E8F0";

    public Scene getBookContainerScene(Runnable callbackAction) {

        // Retrieve latest cargo if available
        List<Map<String, Object>> cargos = cargoDao.getAllCargos();
        if (cargos != null && !cargos.isEmpty()) {
            Map<String, Object> latest = cargos.get(0);
            selectedCargoId = (String) latest.getOrDefault("cargoId", "CGO-1024");
            selectedCargoName = (String) latest.getOrDefault("cargoName", "Electronics");
            selectedCargoType = (String) latest.getOrDefault("cargoType", "General Cargo");
            selectedCargoWeight = (String) latest.getOrDefault("weight", "1,200 kg");
            selectedOriginPort = (String) latest.getOrDefault("originPort", "Port of Mumbai");
            selectedDestinationPort = (String) latest.getOrDefault("destinationPort", "Port of Singapore");
        }

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
        Text title = new Text("Book Container");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text subtitle = new Text("Select container capacity and port allocation for your registered export cargo.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-fill: " + TEXT_MUTED + ";");
        headerBanner.getChildren().addAll(title, subtitle);

        // Workspace Layout (2 Balanced Columns)
        HBox workspace = new HBox(24);

        // =========================================================
        // LEFT COLUMN: SELECTED CARGO INFORMATION CARD — UNCHANGED
        // =========================================================
        VBox cargoCard = createCard();
        cargoCard.setPrefWidth(430);

        VBox cargoHeader = new VBox(4);
        Text cargoTitle = new Text("Selected Registered Cargo");
        cargoTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text cargoSub = new Text("Cargo details requiring container allocation.");
        cargoSub.setStyle("-fx-font-size: 13px; -fx-fill: " + TEXT_MUTED + ";");
        cargoHeader.getChildren().addAll(cargoTitle, cargoSub);

        VBox infoGrid = new VBox(12);
        infoGrid.getChildren().addAll(
                createDetailRow("Cargo ID", selectedCargoId),
                createDetailRow("Cargo Name", selectedCargoName),
                createDetailRow("Category", selectedCargoType),
                createDetailRow("Gross Weight", selectedCargoWeight),
                createDetailRow("Origin Port", selectedOriginPort),
                createDetailRow("Destination", selectedDestinationPort),
                createDetailRow("Status", "Registered (Ready for Container)")
        );

        // Booking Workflow Guidance Box
        VBox flowCard = new VBox(10);
        flowCard.setPadding(new Insets(16));
        flowCard.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px;");

        Text flowTitle = new Text("Container Booking Steps");
        flowTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        VBox steps = new VBox(6);
        Label step1 = createFlowItem("1. Cargo Selected", true);
        Label step2 = createFlowItem("2. Container Type Selected", true);
        Label step3 = createFlowItem("3. Quantity Confirmed", true);
        Label step4 = createFlowItem("4. Preferred Port Selected", true);
        Label step5 = createFlowItem("5. Container Allocation Confirmed", false);

        steps.getChildren().addAll(step1, step2, step3, step4, step5);
        flowCard.getChildren().addAll(flowTitle, steps);

        cargoCard.getChildren().addAll(cargoHeader, infoGrid, flowCard);

        // =========================================================
        // RIGHT COLUMN: CONTAINER BOOKING & ALLOCATION WORKSPACE
        // =========================================================
        VBox bookingCard = createCard();
        HBox.setHgrow(bookingCard, Priority.ALWAYS);

        VBox bookingHeader = new VBox(4);
        Text bookingTitle = new Text("🚢  Container Booking & Allocation");
        bookingTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text bookingSub = new Text("Configure container specifications and terminal preferences.");
        bookingSub.setStyle("-fx-font-size: 13px; -fx-fill: " + TEXT_MUTED + ";");
        bookingHeader.getChildren().addAll(bookingTitle, bookingSub);

        VBox formFields = new VBox(16);

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("20 ft Standard Dry", "40 ft Standard Dry", "40 ft High Cube", "40 ft Refrigerated (Reefer)", "40 ft Open Top");
        typeCombo.setValue("20 ft Standard Dry");

        TextField quantityField = new TextField("1");
        quantityField.setPromptText("Enter container quantity");

        ComboBox<String> portCombo = new ComboBox<>();
        portCombo.getItems().addAll("Port of Mumbai (JNPT)", "Port of Chennai", "Port of Kolkata");
        portCombo.setValue("Port of Mumbai (JNPT)");

        formFields.getChildren().addAll(
                createFormField("Container Type / Size", typeCombo),
                createFormField("Container Quantity", quantityField),
                createFormField("Preferred Port / Terminal", portCombo)
        );

        // =========================================================
        // 3. BOOKING SUMMARY & 4. BOOKING STATUS AREA
        // =========================================================
        VBox summaryBox = new VBox(12);
        summaryBox.setPadding(new Insets(16));
        summaryBox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px;");

        HBox summaryHeader = new HBox();
        summaryHeader.setAlignment(Pos.CENTER_LEFT);
        Text summaryTitle = new Text("Booking Summary");
        summaryTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        // Booking Status Badge
        Label statusBadge = new Label("● Ready for Booking");
        statusBadge.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #B45309; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4px 10px; -fx-background-radius: 12px;");

        summaryHeader.getChildren().addAll(summaryTitle, space, statusBadge);

        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(14);
        summaryGrid.setVgap(8);

        Text sumCargoId = new Text(selectedCargoId);
        sumCargoId.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text sumCargoName = new Text(selectedCargoName);
        sumCargoName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text sumType = new Text(typeCombo.getValue());
        sumType.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: " + PRIMARY_BLUE + ";");
        Text sumQty = new Text(quantityField.getText() + " Unit(s)");
        sumQty.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text sumPort = new Text(portCombo.getValue());
        sumPort.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text sumRoute = new Text(selectedOriginPort + " → " + selectedDestinationPort);
        sumRoute.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        summaryGrid.add(createSummaryLabel("Cargo ID:"), 0, 0);
        summaryGrid.add(sumCargoId, 1, 0);
        summaryGrid.add(createSummaryLabel("Cargo Name:"), 2, 0);
        summaryGrid.add(sumCargoName, 3, 0);

        summaryGrid.add(createSummaryLabel("Container Type:"), 0, 1);
        summaryGrid.add(sumType, 1, 1);
        summaryGrid.add(createSummaryLabel("Quantity:"), 2, 1);
        summaryGrid.add(sumQty, 3, 1);

        summaryGrid.add(createSummaryLabel("Preferred Port:"), 0, 2);
        summaryGrid.add(sumPort, 1, 2);
        summaryGrid.add(createSummaryLabel("Route:"), 2, 2);
        summaryGrid.add(sumRoute, 3, 2);

        // Reactive updates on input change
        typeCombo.setOnAction(e -> sumType.setText(typeCombo.getValue()));
        portCombo.setOnAction(e -> sumPort.setText(portCombo.getValue()));
        quantityField.textProperty().addListener((obs, oldV, newV) -> sumQty.setText(newV + " Unit(s)"));

        summaryBox.getChildren().addAll(summaryHeader, summaryGrid);

        // 5. Single Action Button
        Button bookBtn = new Button("🚢  Book Container Now");
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        bookBtn.setPrefHeight(46);
        bookBtn.setStyle(
                "-fx-background-color: " + PRIMARY_BLUE + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 9px;" +
                "-fx-cursor: hand;"
        );

        // Success Alert Banner
        Label successAlert = new Label("✓   Container booked successfully!\nBooking ID: CONT-2045 — Associated with Cargo ID: " + selectedCargoId);
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

        // DATABASE SAVE HANDLER
        bookBtn.setOnAction(e -> {
            String qtyStr = quantityField.getText();
            if (qtyStr == null || qtyStr.isBlank()) {
                qtyStr = "1";
            }

            String bookingId = containerDao.generateBookingId();
            containerDao.saveBooking(
                    bookingId,
                    selectedCargoId,
                    selectedCargoName,
                    typeCombo.getValue(),
                    qtyStr,
                    portCombo.getValue(),
                    selectedOriginPort,
                    selectedDestinationPort,
                    "Booked"
            );

            // Update UI status to Container Booked
            statusBadge.setText("✓ Container Booked");
            statusBadge.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4px 10px; -fx-background-radius: 12px;");

            step5.setStyle("-fx-font-size: 12px; -fx-text-fill: #16A34A; -fx-font-weight: bold;");
            step5.setText("✓  5. Container Allocation Confirmed (" + bookingId + ")");

            successAlert.setText("✓   Container booked & saved to database successfully!\nBooking ID: " + bookingId + " — Associated with Cargo ID: " + selectedCargoId);
            successAlert.setVisible(true);
        });

        bookingCard.getChildren().addAll(bookingHeader, formFields, summaryBox, bookBtn, successAlert);

        workspace.getChildren().addAll(cargoCard, bookingCard);

        content.getChildren().addAll(headerBanner, workspace);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + BG_COLOR + "; -fx-border-color: transparent;");

        BorderPane root = new BorderPane();
        root.setTop(createHeader("Book Container", "Dock Tech / Exporter / Book Container"));
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

    private VBox createFormField(String labelText, javafx.scene.Node field) {
        VBox box = new VBox(6);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        if (field instanceof TextField) {
            TextField tf = (TextField) field;
            tf.setPrefHeight(42);
            tf.setStyle(
                    "-fx-background-color: #FFFFFF;" +
                    "-fx-border-color: #CBD5E1;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-padding: 8px 12px;" +
                    "-fx-font-size: 14px;"
            );
        } else if (field instanceof ComboBox) {
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

    private Text createSummaryLabel(String label) {
        Text t = new Text(label);
        t.setStyle("-fx-font-size: 12px; -fx-fill: " + TEXT_MUTED + ";");
        return t;
    }

    private Label createFlowItem(String text, boolean active) {
        Label l = new Label((active ? "✓  " : "○  ") + text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (active ? "#16A34A" : TEXT_MUTED) + "; -fx-font-weight: " + (active ? "bold" : "normal") + ";");
        return l;
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

        upload.setOnAction(e -> {
            UploadDocument page = new UploadDocument();
            com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage, page.getUploadDocumentScene(this::back).getRoot());
        });

        payment.setOnAction(e -> {
            MakePayment page = new MakePayment();
            com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage, page.getMakePaymentScene(this::back).getRoot());
        });

        track.setOnAction(e -> {
            TrackShipment page = new TrackShipment();
            com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage, page.getTrackShipmentScene(this::back).getRoot());
        });

        return com.projectui.view.login.DashboardLayoutHelper.createSidebar(ExporterDashboard.dashboardStage, "Exporter", "Book Container", menuButtons);
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
