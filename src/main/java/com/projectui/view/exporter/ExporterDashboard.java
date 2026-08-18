package com.projectui.view.exporter;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ExporterDashboard extends Application {

    public static Stage dashboardStage;

    private Scene dashboardScene;
    private BorderPane dashboardRoot;
    private static boolean styleInitialized = false;

    // Design Tokens
    private static final String BG_COLOR = "#F4F8FC";
    private static final String CARD_BG = "#FFFFFF";
    private static final String TEXT_PRIMARY = "#0F172A";
    private static final String TEXT_MUTED = "#64748B";
    private static final String PRIMARY_BLUE = "#1769FF";
    private static final String BORDER_COLOR = "#E2E8F0";

    @Override
    public void start(Stage stage) {

        dashboardStage = stage;

        if (!styleInitialized) {
            if (!dashboardStage.isShowing()) {
                try {
                    dashboardStage.initStyle(StageStyle.UNDECORATED);
                } catch (Exception ignored) {
                }
            }
            styleInitialized = true;
        }

        // =========================
        // SIDEBAR
        // =========================
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

        dashboard.setOnAction(event -> showDashboard());

        register.setOnAction(event -> {
            RegisterCargo page = new RegisterCargo();
            com.projectui.view.login.LoginPage.showRoot(dashboardStage,
                    page.getRegisterCargoScene(this::showDashboard).getRoot());
        });

        book.setOnAction(event -> {
            BookContainer page = new BookContainer();
            com.projectui.view.login.LoginPage.showRoot(dashboardStage,
                    page.getBookContainerScene(this::showDashboard).getRoot());
        });

        upload.setOnAction(event -> {
            UploadDocument page = new UploadDocument();
            com.projectui.view.login.LoginPage.showRoot(dashboardStage,
                    page.getUploadDocumentScene(this::showDashboard).getRoot());
        });

        payment.setOnAction(event -> {
            MakePayment page = new MakePayment();
            com.projectui.view.login.LoginPage.showRoot(dashboardStage,
                    page.getMakePaymentScene(this::showDashboard).getRoot());
        });

        track.setOnAction(event -> {
            TrackShipment page = new TrackShipment();
            com.projectui.view.login.LoginPage.showRoot(dashboardStage,
                    page.getTrackShipmentScene(this::showDashboard).getRoot());
        });

        VBox sidebar = com.projectui.view.login.DashboardLayoutHelper.createSidebar(dashboardStage, "Exporter",
                "Dashboard", menuButtons);

        // =========================
        // MAIN CONTENT
        // =========================
        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 32, 36, 32));
        content.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // Page Header Banner
        VBox headerBanner = new VBox(6);
        Text title = new Text("Exporter Dashboard");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text subtitle = new Text("Overview of your cargo registrations, container bookings, documents, and live shipments.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-fill: " + TEXT_MUTED + ";");
        headerBanner.getChildren().addAll(title, subtitle);

        // =========================
        // TOP KPI CARDS SECTION
        // =========================
        HBox kpiRow = new HBox(16);
        kpiRow.getChildren().addAll(
                createKpiCard("Active Cargo", "5", "📦", "#EBF3FF", PRIMARY_BLUE),
                createKpiCard("Containers", "3", "🚢", "#F0FDF4", "#16A34A"),
                createKpiCard("Documents", "4", "📄", "#FEF3C7", "#D97706"),
                createKpiCard("Shipments", "2", "📥", "#F3E8FF", "#9333EA"),
                createKpiCard("Payment Pending", "₹1,25,000", "💳", "#FEE2E2", "#DC2626")
        );

        // =========================
        // MAIN TWO-COLUMN CONTENT
        // =========================
        HBox mainLayout = new HBox(24);

        // LEFT COLUMN (62% Width)
        VBox leftCol = new VBox(22);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        // 1. Recent Cargo Card
        VBox recentCargoCard = createCard();
        VBox cargoHeader = new VBox(4);
        Text cargoTitle = new Text("Recent Cargo Registrations");
        cargoTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text cargoSub = new Text("Latest registered cargo items and their current clearance status.");
        cargoSub.setStyle("-fx-font-size: 13px; -fx-fill: " + TEXT_MUTED + ";");
        cargoHeader.getChildren().addAll(cargoTitle, cargoSub);

        VBox cargoTable = new VBox(10);
        cargoTable.getChildren().addAll(
                createTableHeader(),
                createTableRow("CGO-1024", "Electronics", "500 units", "1,200 kg", "Registered", "#E0F2FE", "#0284C7"),
                createTableRow("CGO-1025", "Machinery", "120 units", "3,400 kg", "In Transit", "#DCFCE7", "#16A34A"),
                createTableRow("CGO-1026", "Textiles", "800 units", "850 kg", "Pending", "#FEF3C7", "#D97706"),
                createTableRow("CGO-1027", "Auto Parts", "350 units", "2,100 kg", "Registered", "#E0F2FE", "#0284C7")
        );

        recentCargoCard.getChildren().addAll(cargoHeader, cargoTable);

        // 2. Shipment Progress Stepper Card
        VBox progressCard = createCard();
        Text progressTitle = new Text("Export Workflow Progress");
        progressTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text progressSub = new Text("End-to-end status tracker for active shipment SHP-1024.");
        progressSub.setStyle("-fx-font-size: 13px; -fx-fill: " + TEXT_MUTED + ";");

        HBox stepper = new HBox(12);
        stepper.setAlignment(Pos.CENTER_LEFT);
        stepper.setPadding(new Insets(14, 0, 6, 0));
        stepper.getChildren().addAll(
                createStepNode("1", "Cargo Registered", true),
                createStepLine(true),
                createStepNode("2", "Container Booked", true),
                createStepLine(true),
                createStepNode("3", "Documents Uploaded", true),
                createStepLine(true),
                createStepNode("4", "Payment Completed", true),
                createStepLine(false),
                createStepNode("5", "In Transit", true),
                createStepLine(false),
                createStepNode("6", "Delivered", false)
        );

        progressCard.getChildren().addAll(progressTitle, progressSub, stepper);

        leftCol.getChildren().addAll(recentCargoCard, progressCard);

        // RIGHT COLUMN (38% Width)
        VBox rightCol = new VBox(22);
        rightCol.setPrefWidth(380);
        rightCol.setMinWidth(340);

        // 1. Shipment Overview Card
        VBox shipmentCard = createCard();
        Text shipTitle = new Text("Shipment Overview");
        shipTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text shipSub = new Text("Live status of active marine shipments.");
        shipSub.setStyle("-fx-font-size: 13px; -fx-fill: " + TEXT_MUTED + ";");

        VBox shipList = new VBox(12);
        shipList.getChildren().addAll(
                createShipmentItem("Oceanic Star", "SHP-1024", "Mumbai → Singapore", "ETA: 14 Aug", "In Transit", "#DCFCE7", "#16A34A"),
                createShipmentItem("Sea Voyager", "SHP-1028", "Mumbai → Dubai", "ETA: 18 Aug", "Preparing", "#FEF3C7", "#D97706")
        );

        shipmentCard.getChildren().addAll(shipTitle, shipSub, shipList);

        // 2. Pending Actions Card
        VBox pendingCard = createCard();
        Text pendingTitle = new Text("Pending Actions");
        pendingTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text pendingSub = new Text("Actionable items requiring your prompt attention.");
        pendingSub.setStyle("-fx-font-size: 13px; -fx-fill: " + TEXT_MUTED + ";");

        VBox pendingList = new VBox(10);
        pendingList.getChildren().addAll(
                createActionItem("⚠️  Upload missing Shipping Document for CGO-1026", "#FFF7ED", "#C2410C", "#FDBA74"),
                createActionItem("⚠️  Container booking pending for CGO-1027", "#FFF7ED", "#C2410C", "#FDBA74"),
                createActionItem("💳  Payment of ₹1,25,000 pending for SHP-1024", "#FEF2F2", "#DC2626", "#FCA5A5")
        );

        pendingCard.getChildren().addAll(pendingTitle, pendingSub, pendingList);

        // 3. Quick Actions Card
        VBox quickCard = createCard();
        Text quickTitle = new Text("Quick Actions");
        quickTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        VBox quickBtns = new VBox(10);
        Button btnRegister = createQuickActionButton("📦  Register New Cargo", "#1769FF", "#FFFFFF");
        btnRegister.setOnAction(e -> {
            RegisterCargo page = new RegisterCargo();
            com.projectui.view.login.LoginPage.showRoot(dashboardStage, page.getRegisterCargoScene(this::showDashboard).getRoot());
        });

        Button btnBook = createQuickActionButton("🚢  Book Container Space", "#0F172A", "#FFFFFF");
        btnBook.setOnAction(e -> {
            BookContainer page = new BookContainer();
            com.projectui.view.login.LoginPage.showRoot(dashboardStage, page.getBookContainerScene(this::showDashboard).getRoot());
        });

        Button btnUpload = createQuickActionButton("📄  Upload Shipping Documents", "#0F172A", "#FFFFFF");
        btnUpload.setOnAction(e -> {
            UploadDocument page = new UploadDocument();
            com.projectui.view.login.LoginPage.showRoot(dashboardStage, page.getUploadDocumentScene(this::showDashboard).getRoot());
        });

        Button btnPay = createQuickActionButton("💳  Pay Outstanding Invoices", "#0F172A", "#FFFFFF");
        btnPay.setOnAction(e -> {
            MakePayment page = new MakePayment();
            com.projectui.view.login.LoginPage.showRoot(dashboardStage, page.getMakePaymentScene(this::showDashboard).getRoot());
        });

        Button btnTrack = createQuickActionButton("📥  Track Live Shipment", "#0F172A", "#FFFFFF");
        btnTrack.setOnAction(e -> {
            TrackShipment page = new TrackShipment();
            com.projectui.view.login.LoginPage.showRoot(dashboardStage, page.getTrackShipmentScene(this::showDashboard).getRoot());
        });

        quickBtns.getChildren().addAll(btnRegister, btnBook, btnUpload, btnPay, btnTrack);
        quickCard.getChildren().addAll(quickTitle, quickBtns);

        rightCol.getChildren().addAll(shipmentCard, pendingCard, quickCard);

        mainLayout.getChildren().addAll(leftCol, rightCol);

        content.getChildren().addAll(headerBanner, kpiRow, mainLayout);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + BG_COLOR + "; -fx-border-color: transparent;");

        BorderPane root = new BorderPane();
        HBox topHeader = com.projectui.view.login.DashboardLayoutHelper.createHeader(dashboardStage,
                "Dashboard", "Dock Tech / Exporter / Dashboard", "Exporter", () -> {
                    Logout page = new Logout();
                    page.showLogout();
                }, null);

        root.setTop(topHeader);
        root.setLeft(sidebar);
        root.setCenter(scrollPane);

        this.dashboardRoot = root;

        com.projectui.view.login.LoginPage.showRoot(dashboardStage, root);
        dashboardScene = dashboardStage.getScene();
        dashboardStage.setTitle("Dock Tech - Exporter Dashboard");
    }

    public void showDashboard() {
        if (dashboardStage != null && dashboardRoot != null) {
            com.projectui.view.login.LoginPage.showRoot(dashboardStage, dashboardRoot);
        }
    }

    // =========================================================
    // UI HELPER COMPONENTS
    // =========================================================

    private VBox createCard() {
        VBox card = new VBox(16);
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

    private VBox createKpiCard(String title, String value, String icon, String iconBg, String iconColor) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setPrefWidth(210);
        HBox.setHgrow(card, Priority.ALWAYS);

        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                "-fx-background-radius: 16px;" +
                "-fx-border-color: " + BORDER_COLOR + ";" +
                "-fx-border-radius: 16px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.04), 8, 0, 0, 3);"
        );

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Text titleText = new Text(title);
        titleText.setStyle("-fx-fill: " + TEXT_MUTED + "; -fx-font-size: 13px; -fx-font-weight: bold;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label iconLabel = new Label(icon);
        iconLabel.setMinSize(36, 36);
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setStyle("-fx-background-color: " + iconBg + "; -fx-background-radius: 10px; -fx-font-size: 18px;");

        top.getChildren().addAll(titleText, space, iconLabel);

        Text valText = new Text(value);
        valText.setStyle("-fx-fill: " + TEXT_PRIMARY + "; -fx-font-size: 24px; -fx-font-weight: bold;");

        card.getChildren().addAll(top, valText);
        return card;
    }

    private HBox createTableHeader() {
        HBox header = new HBox(12);
        header.setPadding(new Insets(10, 14, 10, 14));
        header.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8px;");

        header.getChildren().addAll(
                createHeaderLabel("CARGO ID", 110),
                createHeaderLabel("TYPE", 120),
                createHeaderLabel("QUANTITY", 100),
                createHeaderLabel("WEIGHT", 100),
                createHeaderLabel("STATUS", 110)
        );
        return header;
    }

    private Label createHeaderLabel(String text, double width) {
        Label l = new Label(text);
        l.setPrefWidth(width);
        l.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px; -fx-font-weight: bold;");
        return l;
    }

    private HBox createTableRow(String id, String type, String qty, String weight, String status, String badgeBg, String badgeFg) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        row.setStyle("-fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;");

        Label idLbl = new Label(id);
        idLbl.setPrefWidth(110);
        idLbl.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label typeLbl = new Label(type);
        typeLbl.setPrefWidth(120);
        typeLbl.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px;");

        Label qtyLbl = new Label(qty);
        qtyLbl.setPrefWidth(100);
        qtyLbl.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px;");

        Label weightLbl = new Label(weight);
        weightLbl.setPrefWidth(100);
        weightLbl.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px;");

        Label statusBadge = new Label(status);
        statusBadge.setPrefWidth(110);
        statusBadge.setAlignment(Pos.CENTER);
        statusBadge.setStyle("-fx-background-color: " + badgeBg + "; -fx-text-fill: " + badgeFg + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4px 10px; -fx-background-radius: 20px;");

        row.getChildren().addAll(idLbl, typeLbl, qtyLbl, weightLbl, statusBadge);
        return row;
    }

    private VBox createStepNode(String num, String label, boolean completed) {
        VBox node = new VBox(6);
        node.setAlignment(Pos.CENTER);

        Label numLbl = new Label(completed ? "✓" : num);
        numLbl.setMinSize(28, 28);
        numLbl.setAlignment(Pos.CENTER);

        if (completed) {
            numLbl.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50; -fx-font-size: 12px;");
        } else {
            numLbl.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #64748B; -fx-font-weight: bold; -fx-background-radius: 50; -fx-font-size: 12px;");
        }

        Label textLbl = new Label(label);
        textLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (completed ? TEXT_PRIMARY : TEXT_MUTED) + "; -fx-font-weight: " + (completed ? "bold" : "normal") + ";");

        node.getChildren().addAll(numLbl, textLbl);
        return node;
    }

    private Region createStepLine(boolean active) {
        Region line = new Region();
        line.setPrefHeight(3);
        line.setPrefWidth(30);
        HBox.setHgrow(line, Priority.ALWAYS);
        line.setStyle("-fx-background-color: " + (active ? "#16A34A" : "#CBD5E1") + "; -fx-background-radius: 2;");
        return line;
    }

    private VBox createShipmentItem(String name, String id, String route, String eta, String status, String badgeBg, String badgeFg) {
        VBox item = new VBox(8);
        item.setPadding(new Insets(14));
        item.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px;");

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(2);
        Text title = new Text(name + " (" + id + ")");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text r = new Text(route);
        r.setStyle("-fx-font-size: 12px; -fx-fill: " + TEXT_MUTED + ";");
        info.getChildren().addAll(title, r);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label badge = new Label(status);
        badge.setStyle("-fx-background-color: " + badgeBg + "; -fx-text-fill: " + badgeFg + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 14px;");

        top.getChildren().addAll(info, space, badge);

        Text etaLbl = new Text("🗓  " + eta);
        etaLbl.setStyle("-fx-font-size: 12px; -fx-fill: #475569; -fx-font-weight: bold;");

        item.getChildren().addAll(top, etaLbl);
        return item;
    }

    private HBox createActionItem(String text, String bg, String fg, String border) {
        HBox item = new HBox();
        item.setPadding(new Insets(12, 14, 12, 14));
        item.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + border + "; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + fg + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        item.getChildren().add(label);
        return item;
    }

    private Button createQuickActionButton(String text, String bg, String fg) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(42);
        btn.setStyle(
                "-fx-background-color: " + bg + ";" +
                "-fx-text-fill: " + fg + ";" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8px;" +
                "-fx-cursor: hand;"
        );
        return btn;
    }
}
