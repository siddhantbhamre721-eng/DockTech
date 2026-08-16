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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RegisterCargo {

    private Scene scene;
    private final CargoDao cargoDao = new CargoDao();

    // Design Tokens
    private static final String BG_COLOR = "#F4F8FC";
    private static final String CARD_BG = "#FFFFFF";
    private static final String TEXT_PRIMARY = "#0F172A";
    private static final String TEXT_MUTED = "#64748B";
    private static final String PRIMARY_BLUE = "#1769FF";
    private static final String BORDER_COLOR = "#E2E8F0";

    public Scene getRegisterCargoScene(Runnable callbackAction) {

        // =========================================================
        // SIDEBAR - PRESERVED
        // =========================================================
        VBox sidebar = createSidebar();

        // =========================================================
        // MAIN CONTENT WORKSPACE
        // =========================================================
        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 32, 36, 32));
        content.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // Header Banner
        VBox headerBanner = new VBox(6);
        Text title = new Text("Register Cargo");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text subtitle = new Text("Register cargo details before proceeding to container booking and shipment creation.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-fill: " + TEXT_MUTED + ";");
        headerBanner.getChildren().addAll(title, subtitle);

        // Workspace Layout (2-Columns: Form Left, Guidance & Recent Right)
        HBox workspace = new HBox(24);

        // =========================================================
        // LEFT COLUMN: REGISTRATION FORM CARD (60% Width)
        // =========================================================
        VBox formCard = createCard();
        HBox.setHgrow(formCard, Priority.ALWAYS);

        VBox formHeader = new VBox(4);
        Text formTitle = new Text("📦  Cargo Registration Workspace");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text formSub = new Text("Fill in mandatory specifications for maritime cargo clearance.");
        formSub.setStyle("-fx-font-size: 13px; -fx-fill: " + TEXT_MUTED + ";");
        formHeader.getChildren().addAll(formTitle, formSub);

        // Form Grid
        GridPane formGrid = new GridPane();
        formGrid.setHgap(18);
        formGrid.setVgap(18);

        TextField cargoName = new TextField("Electronics");
        cargoName.setPromptText("Enter Cargo Name");

        ComboBox<String> cargoType = new ComboBox<>();
        cargoType.getItems().addAll("General Cargo", "Electronics", "Machinery", "Textiles");
        cargoType.setValue("General Cargo");

        TextField quantity = new TextField("500");
        quantity.setPromptText("Quantity (Units/Boxes)");

        TextField weight = new TextField("1200 kg");
        weight.setPromptText("Total Weight (e.g. 1200 kg)");

        ComboBox<String> origin = new ComboBox<>();
        origin.getItems().addAll("Port of Mumbai", "Port of Chennai", "Port of Kolkata");
        origin.setValue("Port of Mumbai");

        ComboBox<String> destination = new ComboBox<>();
        destination.getItems().addAll("Port of Singapore", "Port of Dubai", "Port of London");
        destination.setValue("Port of Singapore");

        // Add fields to GridPane
        formGrid.add(createFormField("Cargo Name", cargoName), 0, 0);
        formGrid.add(createFormField("Cargo Category / Type", cargoType), 1, 0);
        formGrid.add(createFormField("Quantity (Units)", quantity), 0, 1);
        formGrid.add(createFormField("Gross Weight", weight), 1, 1);
        formGrid.add(createFormField("Origin Port", origin), 0, 2);
        formGrid.add(createFormField("Destination Port", destination), 1, 2);

        // Primary Action Button
        Button registerBtn = new Button("📦  Register Cargo Now");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setPrefHeight(46);
        registerBtn.setStyle(
                "-fx-background-color: " + PRIMARY_BLUE + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 9px;" +
                "-fx-cursor: hand;"
        );

        // Success Alert Banner
        Label successAlert = new Label("✓   Cargo registered successfully!\nCargo ID: CGO-1027 — Ready for container booking.");
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

        // 2. Recent Registered Cargo Summary Card
        VBox recentCard = createCard();
        Text recentTitle = new Text("Recent Registered Cargo");
        recentTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        VBox recentList = new VBox(10);
        refreshRecentList(recentList);

        registerBtn.setOnAction(event -> {
            String nameVal = cargoName.getText();
            String typeVal = cargoType.getValue();
            String qtyVal = quantity.getText();
            String weightVal = weight.getText();
            String originVal = origin.getValue();
            String destVal = destination.getValue();

            if (nameVal == null || nameVal.isBlank()) {
                nameVal = "Electronics";
            }

            String generatedId = cargoDao.generateCargoId();
            cargoDao.saveCargo(generatedId, nameVal, typeVal, qtyVal, weightVal, originVal, destVal, "Registered");

            successAlert.setText("✓   Cargo registered & saved to database successfully!\nCargo ID: " + generatedId + " — Status: Registered.");
            successAlert.setVisible(true);

            refreshRecentList(recentList);
        });

        formCard.getChildren().addAll(formHeader, formGrid, registerBtn, successAlert);

        // =========================================================
        // RIGHT COLUMN: PROCESS GUIDANCE & RECENT CARGO (40% Width)
        // =========================================================
        VBox rightCol = new VBox(22);
        rightCol.setPrefWidth(380);
        rightCol.setMinWidth(340);

        // 1. Process Guidance Card
        VBox processCard = createCard();
        Text processTitle = new Text("Cargo Registration Process");
        processTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        VBox processSteps = new VBox(12);
        processSteps.getChildren().addAll(
                createProcessStep("1", "Enter Cargo Details", "Specify name, category, and quantity.", true),
                createProcessStep("2", "Select Origin & Destination", "Choose origin port and destination port.", true),
                createProcessStep("3", "Validate Information", "Verify weight and cargo classification.", true),
                createProcessStep("4", "Register Cargo", "Submit registration to generate Cargo ID.", true),
                createProcessStep("5", "Proceed to Container Booking", "Select container type and book space.", false)
        );

        processCard.getChildren().addAll(processTitle, processSteps);

        recentCard.getChildren().addAll(recentTitle, recentList);

        rightCol.getChildren().addAll(processCard, recentCard);

        workspace.getChildren().addAll(formCard, rightCol);

        content.getChildren().addAll(headerBanner, workspace);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + BG_COLOR + "; -fx-border-color: transparent;");

        BorderPane root = new BorderPane();
        root.setTop(createHeader("Register Cargo", "Dock Tech / Exporter / Register Cargo"));
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
        GridPane.setHgrow(box, Priority.ALWAYS);

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
            cb.setStyle(
                    "-fx-font-size: 14px;" +
                    "-fx-padding: 2px 6px;"
            );
        }

        box.getChildren().addAll(label, field);
        return box;
    }

    private HBox createProcessStep(String num, String title, String desc, boolean active) {
        HBox step = new HBox(12);
        step.setAlignment(Pos.CENTER_LEFT);

        Label circle = new Label(active ? "✓" : num);
        circle.setMinSize(28, 28);
        circle.setAlignment(Pos.CENTER);
        if (active) {
            circle.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50; -fx-font-size: 12px;");
        } else {
            circle.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #64748B; -fx-font-weight: bold; -fx-background-radius: 50; -fx-font-size: 12px;");
        }

        VBox text = new VBox(2);
        Text titleText = new Text(title);
        titleText.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-fill: " + (active ? TEXT_PRIMARY : TEXT_MUTED) + ";");
        Text descText = new Text(desc);
        descText.setStyle("-fx-font-size: 11px; -fx-fill: " + TEXT_MUTED + ";");
        text.getChildren().addAll(titleText, descText);

        step.getChildren().addAll(circle, text);
        return step;
    }

    private HBox createMiniCargoRow(String id, String type, String qty, String status, String bg, String fg) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px;");

        VBox info = new VBox(2);
        Text title = new Text(id + " • " + type);
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text q = new Text("Qty: " + qty);
        q.setStyle("-fx-font-size: 11px; -fx-fill: " + TEXT_MUTED + ";");
        info.getChildren().addAll(title, q);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label badge = new Label(status);
        badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 12px;");

        row.getChildren().addAll(info, space, badge);
        return row;
    }

    private void refreshRecentList(VBox recentList) {
        recentList.getChildren().clear();
        List<Map<String, Object>> cargos = cargoDao.getAllCargos();
        for (Map<String, Object> c : cargos) {
            String id = (String) c.get("cargoId");
            String type = (String) c.get("cargoType");
            String qty = (String) c.get("quantity");
            String status = (String) c.get("status");

            String bg = "#E0F2FE";
            String fg = "#0284C7";
            if ("In Transit".equalsIgnoreCase(status)) {
                bg = "#DCFCE7";
                fg = "#16A34A";
            } else if ("Pending".equalsIgnoreCase(status)) {
                bg = "#FEF3C7";
                fg = "#D97706";
            }

            recentList.getChildren().add(createMiniCargoRow(id, type, qty, status, bg, fg));
        }
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

        dashboard.setOnAction(e -> openDashboard());

        book.setOnAction(e -> {
            BookContainer page = new BookContainer();
            com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage, page.getBookContainerScene(this::back).getRoot());
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

        return com.projectui.view.login.DashboardLayoutHelper.createSidebar(ExporterDashboard.dashboardStage, "Exporter", "Register Cargo", menuButtons);
    }

    private void openDashboard() {
        new ExporterDashboard().start(ExporterDashboard.dashboardStage);
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