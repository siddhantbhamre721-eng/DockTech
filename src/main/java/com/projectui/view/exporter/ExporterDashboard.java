package com.projectui.view.exporter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.projectui.dao.CargoDao;
import com.projectui.dao.ContainerDao;
import com.projectui.view.login.DashboardLayoutHelper;
import com.projectui.view.login.LoginPage;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@SuppressWarnings("unused")
public class ExporterDashboard extends Application {

    private static ExporterDashboard instance;
    public static Stage dashboardStage;

    private BorderPane root;
    private BorderPane center;
    private Runnable logoutAction;
    private String currentPage = "Dashboard";
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private Label headerTitleLabel;
    private Label breadcrumbLabel;

    public static ExporterDashboard getInstance() {
        return instance;
    }

    // COLORS & DESIGN TOKENS
    private static final String BG = "#f5f8fc";
    private static final String TEXT = "#111827";
    private static final String MUTED = "#64748b";
    private static final String BORDER = "#e5e7eb";
    private static final String BLUE = "#0b63ff";

    @Override
    public void start(Stage stage) {
        dashboardStage = stage;
        Parent r = getRoot(stage, () -> new LoginPage().show(stage), "Dashboard");
        LoginPage.showRoot(stage, r);
        stage.setTitle("DockTech - Exporter Dashboard");
        stage.show();
    }

    public Parent getRoot() {
        return getRoot(dashboardStage, logoutAction, "Dashboard");
    }

    public Parent getRoot(Stage stage, Runnable logoutAction) {
        return getRoot(stage, logoutAction, "Dashboard");
    }

    public Parent getRoot(Stage stage, Runnable logoutAction, String pageName) {
        instance = this;
        dashboardStage = stage;
        this.logoutAction = logoutAction;
        this.currentPage = (pageName == null || pageName.isBlank()) ? "Dashboard" : pageName;

        root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG + ";");

        center = new BorderPane();
        center.setStyle("-fx-background-color: " + BG + ";");

        headerTitleLabel = new Label(currentPage);
        headerTitleLabel.setStyle("-fx-text-fill: #111827; -fx-font-size: 17px; -fx-font-weight: bold;");

        breadcrumbLabel = new Label("DockTech / Exporter / " + currentPage);
        breadcrumbLabel.setStyle("-fx-text-fill: #8a93a1; -fx-font-size: 10px;");

        root.setLeft(createSidebar());
        root.setTop(header(stage, logoutAction));
        root.setCenter(center);

        showPage(currentPage);

        root.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene == null) return;
            newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                if (newWin == null) return;
                if (newWin instanceof Stage s) {
                    dashboardStage = s;
                    root.prefWidthProperty().bind(s.widthProperty());
                    root.prefHeightProperty().bind(s.heightProperty());
                }
            });
        });

        return root;
    }

    // PAGE SWITCHING
    public void switchPage(String pageName) {
        this.currentPage = (pageName == null || pageName.isBlank()) ? "Dashboard" : pageName;
        if (dashboardStage != null && root != null) {
            if (dashboardStage.getScene() == null || dashboardStage.getScene().getRoot() != root) {
                com.projectui.view.login.LoginPage.showRoot(dashboardStage, root);
            }
        }
        if (headerTitleLabel != null) {
            headerTitleLabel.setText(currentPage);
        }
        if (breadcrumbLabel != null) {
            if ("Dashboard".equalsIgnoreCase(currentPage)) {
                breadcrumbLabel.setText("DockTech / Exporter / Dashboard");
            } else {
                breadcrumbLabel.setText("DockTech / Exporter / " + currentPage);
            }
        }
        updateNavSelection();
        showPage(currentPage);
    }

    private void showPage(String pageName) {
        try {
            if ("Register Cargo".equalsIgnoreCase(pageName)) {
                center.setCenter(new RegistrationCargo().getScrollableContent());
            } else if ("Book Container".equalsIgnoreCase(pageName)) {
                center.setCenter(new ContainerBooking().getScrollableContent());
            } else if ("Upload Document".equalsIgnoreCase(pageName)) {
                center.setCenter(new UploadDocument().getScrollableContent());
            } else if ("Make Payment".equalsIgnoreCase(pageName)) {
                center.setCenter(new MakePayment().getScrollableContent());
            } else if ("Track Shipment".equalsIgnoreCase(pageName)) {
                center.setCenter(new TrackShipment().getScrollableContent());
            } else {
                center.setCenter(createDashboardScroll());
            }
        } catch (Throwable t) {
            System.err.println("[EXPORTER DASHBOARD] Error loading page '" + pageName + "': " + t.getMessage());
            t.printStackTrace();
        }
    }

    private void updateNavSelection() {
        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equalsIgnoreCase(currentPage);
            entry.getValue().setStyle(navStyle(false, active));
        }
    }

    // SIDEBAR
    private VBox createSidebar() {
        navButtons.clear();
        Button dashboard = new Button("🏠   Dashboard");
        Button register = new Button("📦  Register Cargo");
        Button book = new Button("🚢   Book Container");
        Button upload = new Button("📄   Upload Document");
        Button payment = new Button("💳   Make Payment");
        Button track = new Button("📥   Track Shipment");

        navButtons.put("Dashboard", dashboard);
        navButtons.put("Register Cargo", register);
        navButtons.put("Book Container", book);
        navButtons.put("Upload Document", upload);
        navButtons.put("Make Payment", payment);
        navButtons.put("Track Shipment", track);

        dashboard.setOnAction(e -> switchPage("Dashboard"));
        register.setOnAction(e -> switchPage("Register Cargo"));
        book.setOnAction(e -> switchPage("Book Container"));
        upload.setOnAction(e -> switchPage("Upload Document"));
        payment.setOnAction(e -> switchPage("Make Payment"));
        track.setOnAction(e -> switchPage("Track Shipment"));

        return DashboardLayoutHelper.createSidebar(dashboardStage, "Exporter", currentPage, navButtons);
    }

    private static String navStyle(boolean hover, boolean selected) {
        return "-fx-background-color:" + (hover || selected ? BLUE : "transparent") + ";"
                + "-fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:bold;"
                + "-fx-padding:12 16; -fx-background-radius:8px; -fx-cursor:hand;";
    }

    // HEADER
    private HBox header(Stage stage, Runnable logout) {
        return DashboardLayoutHelper.createHeader(stage != null ? stage : dashboardStage, headerTitleLabel, breadcrumbLabel, "Exporter", () -> {
            Runnable action = logout != null ? logout : this.logoutAction;
            if (action != null) {
                action.run();
            } else {
                Stage currentStage = stage != null ? stage : dashboardStage;
                if (currentStage != null) {
                    new LoginPage().show(currentStage);
                }
            }
        }, null);
    }

    // DASHBOARD CONTENT SCROLL PANE
    public ScrollPane createDashboardScroll() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color:" + BG + "; -fx-background:transparent; -fx-border-color:transparent;");

        VBox main = new VBox(20);
        main.setPadding(new Insets(24, 28, 28, 28));
        main.setStyle("-fx-background-color:" + BG + ";");

        // Welcome Header
        HBox welcome = new HBox(12);
        welcome.setAlignment(Pos.CENTER_LEFT);

        VBox welcomeText = new VBox(4);
        Label title = new Label("Exporter Dashboard");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #101A2B;");

        Label subtitle = new Label("Manage your cargo bookings, documentation, customs clearances, and shipment tracking.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
        welcomeText.getChildren().addAll(title, subtitle);

        Region welcomeSpacer = new Region();
        HBox.setHgrow(welcomeSpacer, Priority.ALWAYS);

        String todayDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));
        Label todayLabel = new Label("📅  " + todayDate);
        todayLabel.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-background-radius: 10px; -fx-border-radius: 10px; -fx-padding: 8px 14px; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #475569;");

        welcome.getChildren().addAll(welcomeText, welcomeSpacer, todayLabel);

        // Fetch Live Stats from DAOs
        CargoDao cargoDao = new CargoDao();
        ContainerDao containerDao = new ContainerDao();

        List<Map<String, Object>> allCargos = null;
        try {
            allCargos = cargoDao.getAllCargo();
        } catch (Exception ignored) {}

        List<Map<String, Object>> allBookings = null;
        try {
            allBookings = containerDao.getAllContainers();
        } catch (Exception ignored) {}

        int activeCargoCount = allCargos != null ? allCargos.size() : 0;
        int containerCount = allBookings != null ? allBookings.size() : 0;
        int docsCount = 0;
        int shipmentsCount = 0;

        if (allCargos != null) {
            for (Map<String, Object> c : allCargos) {
                String st = c.get("status") != null ? c.get("status").toString() : "";
                if (st.contains("Document") || st.contains("Pending") || st.equalsIgnoreCase("Registered") || st.contains("Booked")) {
                    docsCount++;
                }
                if (st.contains("Transit") || st.contains("Paid") || st.contains("Cleared")) {
                    shipmentsCount++;
                }
            }
        }

        // KPI Stat Cards
        HBox cards = new HBox(16);
        cards.setAlignment(Pos.CENTER_LEFT);

        VBox activeCargoCard = newStatCard("Active Cargo", String.format("%02d", activeCargoCount), "Currently registered cargo batches", "📦", "#1769FF", "+Active");
        VBox containersCard = newStatCard("Booked Containers", String.format("%02d", containerCount), "Containers allocated in port yards", "🚢", "#7C4DFF", "+Yard");
        VBox documentsCard = newStatCard("Pending Documents", String.format("%02d", docsCount), "Documents required for clearance", "📄", "#00A98F", "+Docs");
        VBox shipmentsCard = newStatCard("Active Shipments", String.format("%02d", shipmentsCount), "Cargo in transit or ready for vessel", "🚚", "#F28C28", "+Transit");

        cards.getChildren().addAll(activeCargoCard, containersCard, documentsCard, shipmentsCard);

        // Main 2-column Layout (Recent Cargo List + Live Shipment Status)
        HBox contentLayout = new HBox(20);
        VBox recentCargoBox = createModernRecentCargo(allCargos);
        HBox.setHgrow(recentCargoBox, Priority.ALWAYS);

        VBox rightColumn = new VBox(16);
        rightColumn.setMinWidth(380);
        rightColumn.setPrefWidth(400);

        VBox shipmentStatusBox = createModernShipmentStatus(allCargos);
        VBox quickActionsBox = createQuickActionsCard();
        rightColumn.getChildren().addAll(shipmentStatusBox, quickActionsBox);

        contentLayout.getChildren().addAll(recentCargoBox, rightColumn);

        // Pending Action Items Section
        VBox pendingActions = createModernPendingActions(allCargos);

        main.getChildren().addAll(welcome, cards, contentLayout, pendingActions);
        scrollPane.setContent(main);
        return scrollPane;
    }

    // STAT CARD HELPER
    private VBox newStatCard(String title, String value, String description, String icon, String color, String change) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(140);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16px; -fx-border-color: #E3EAF3; -fx-border-radius: 16px; -fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.04), 8, 0, 0, 3);");

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #64748B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label iconLabel = new Label(icon);
        iconLabel.setMinSize(36, 36);
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setStyle("-fx-background-color: " + color + "18; -fx-background-radius: 10px; -fx-font-size: 18px;");

        top.getChildren().addAll(titleLabel, spacer, iconLabel);

        HBox bottom = new HBox(8);
        bottom.setAlignment(Pos.BASELINE_LEFT);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        Label changeLabel = new Label(change);
        changeLabel.setStyle("-fx-background-color: " + color + "18; -fx-text-fill: " + color + "; -fx-background-radius: 10px; -fx-padding: 4px 8px; -fx-font-size: 11px; -fx-font-weight: bold;");

        bottom.getChildren().addAll(valueLabel, bottomSpacer, changeLabel);

        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");

        card.getChildren().addAll(top, bottom, descLabel);
        return card;
    }

    // RECENT CARGO TABLE BOX
    private VBox createModernRecentCargo(List<Map<String, Object>> allCargos) {
        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 16px; -fx-border-color: #E3EAF3; -fx-border-radius: 16px;");

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(3);
        Label title = new Label("Recent Cargo Operations");
        title.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label subtitle = new Label("Latest cargo registrations and container allocations");
        subtitle.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");
        titles.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAll = new Button("View All Cargo  →");
        viewAll.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #1E293B; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-cursor: hand; -fx-padding: 6px 12px;");
        viewAll.setOnAction(e -> switchPage("Register Cargo"));

        titleRow.getChildren().addAll(titles, spacer, viewAll);

        // Header Row
        HBox header = new HBox(12);
        header.setPadding(new Insets(10, 14, 10, 14));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8px;");

        Label h1 = tableHeader("CARGO ID", 120);
        Label h2 = tableHeader("DESCRIPTION / TYPE", 200);
        Label h3 = tableHeader("STATUS", 130);
        Label h4 = tableHeader("ETA", 100);
        HBox.setHgrow(h2, Priority.ALWAYS);

        header.getChildren().addAll(h1, h2, h3, h4);

        // Rows
        VBox rows = new VBox(6);
        if (allCargos != null && !allCargos.isEmpty()) {
            int count = 0;
            for (Map<String, Object> c : allCargos) {
                if (count++ >= 6) break;
                String cId = c.get("cargoId") != null ? c.get("cargoId").toString() : "CRG-" + count;
                String cName = c.get("cargoName") != null ? c.get("cargoName").toString() : (c.get("type") != null ? c.get("type").toString() : "General Cargo");
                String cStatus = c.get("status") != null ? c.get("status").toString() : "Registered";
                String eta = c.get("eta") != null ? c.get("eta").toString() : "Scheduled";

                String icon = "📦";
                String lower = cName.toLowerCase();
                if (lower.contains("machine") || lower.contains("industrial")) icon = "⚙️";
                else if (lower.contains("textile") || lower.contains("cotton")) icon = "🧵";
                else if (lower.contains("auto") || lower.contains("car")) icon = "🚗";
                else if (lower.contains("chemical") || lower.contains("pharma")) icon = "🧪";

                rows.getChildren().add(modernCargoRow(cId, cName, cStatus, eta, icon));
            }
        } else {
            Label emptyLbl = new Label("No registered cargo records found. Click 'Register Cargo' to start.");
            emptyLbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px; -fx-padding: 20px;");
            rows.getChildren().add(emptyLbl);
        }

        box.getChildren().addAll(titleRow, header, rows);
        return box;
    }

    private Label tableHeader(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px; -fx-font-weight: bold;");
        return label;
    }

    private HBox modernCargoRow(String id, String type, String status, String eta, String icon) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: white; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;");

        Label iconLabel = new Label(icon);
        iconLabel.setMinSize(28, 28);
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 6px; -fx-font-size: 14px;");

        Label idLabel = new Label(id);
        idLabel.setPrefWidth(90);
        idLabel.setStyle("-fx-text-fill: #0F172A; -fx-font-weight: bold; -fx-font-size: 13px;");

        HBox idBox = new HBox(8, iconLabel, idLabel);
        idBox.setPrefWidth(120);
        idBox.setAlignment(Pos.CENTER_LEFT);

        Label typeLabel = new Label(type);
        typeLabel.setPrefWidth(200);
        HBox.setHgrow(typeLabel, Priority.ALWAYS);
        typeLabel.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px;");

        Label statusLabel = new Label(status);
        statusLabel.setPrefWidth(110);
        statusLabel.setAlignment(Pos.CENTER);

        if (status.toLowerCase().contains("cleared") || status.toLowerCase().contains("paid")) {
            statusLabel.setStyle("-fx-text-fill: #15803D; -fx-background-color: #ECFDF5; -fx-background-radius: 12px; -fx-padding: 4px 10px; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else if (status.toLowerCase().contains("transit") || status.toLowerCase().contains("booked")) {
            statusLabel.setStyle("-fx-text-fill: #1D4ED8; -fx-background-color: #EFF6FF; -fx-background-radius: 12px; -fx-padding: 4px 10px; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #B45309; -fx-background-color: #FFFBEB; -fx-background-radius: 12px; -fx-padding: 4px 10px; -fx-font-size: 11px; -fx-font-weight: bold;");
        }

        Label etaLabel = new Label(eta);
        etaLabel.setPrefWidth(100);
        etaLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");

        row.getChildren().addAll(idBox, typeLabel, statusLabel, etaLabel);

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: white; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;"));
        row.setOnMouseClicked(e -> switchPage("Track Shipment"));

        return row;
    }

    // SHIPMENT STATUS RIGHT WIDGET
    private VBox createModernShipmentStatus(List<Map<String, Object>> allCargos) {
        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 16px; -fx-border-color: #E3EAF3; -fx-border-radius: 16px;");

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Active Shipments");
        title.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 15px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label live = new Label("● LIVE");
        live.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 11px; -fx-font-weight: bold;");
        titleRow.getChildren().addAll(title, spacer, live);

        box.getChildren().add(titleRow);

        int displayed = 0;
        if (allCargos != null && !allCargos.isEmpty()) {
            for (Map<String, Object> c : allCargos) {
                if (displayed >= 3) break;
                String cId = c.get("cargoId") != null ? c.get("cargoId").toString() : "CRG";
                String name = c.get("cargoName") != null ? c.get("cargoName").toString() : "Shipment";
                String org = c.get("originPort") != null ? c.get("originPort").toString() : "Origin";
                String dst = c.get("destinationPort") != null ? c.get("destinationPort").toString() : "Destination";
                String st = c.get("status") != null ? c.get("status").toString() : "Active";

                double prog = st.contains("Paid") ? 0.75 : (st.contains("Transit") ? 0.90 : (st.contains("Uploaded") ? 0.50 : 0.30));
                String color = (st.contains("Paid") || st.contains("Transit")) ? "#16A34A" : "#1769FF";

                box.getChildren().add(modernShipment(cId + " (" + name + ")", org + " → " + dst, "Status: " + st, color, prog));
                displayed++;
            }
        }

        if (displayed == 0) {
            Label noShip = new Label("No shipments actively moving currently.");
            noShip.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");
            box.getChildren().add(noShip);
        }

        return box;
    }

    private VBox modernShipment(String name, String route, String eta, String color, double progress) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(10, 12, 10, 12));
        box.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px; -fx-cursor: hand;");
        box.setOnMouseClicked(e -> switchPage("Track Shipment"));

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #0F172A;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label etaLabel = new Label(eta);
        etaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        top.getChildren().addAll(nameLabel, spacer, etaLabel);

        Label routeLabel = new Label(route);
        routeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");

        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(6);
        progressBar.setStyle("-fx-accent: " + color + ";");

        box.getChildren().addAll(top, routeLabel, progressBar);
        return box;
    }

    // QUICK ACTIONS HELPER
    private VBox createQuickActionsCard() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(18));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 16px; -fx-border-color: #E3EAF3; -fx-border-radius: 16px;");

        Label title = new Label("Quick Exporter Actions");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Button regBtn = new Button("📦  Register New Cargo");
        regBtn.setMaxWidth(Double.MAX_VALUE);
        regBtn.setStyle("-fx-background-color: #1769FF; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 8px 12px; -fx-cursor: hand;");
        regBtn.setOnAction(e -> switchPage("Register Cargo"));

        Button bookBtn = new Button("🚢  Book Yard Container");
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        bookBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #1E293B; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 8px 12px; -fx-cursor: hand;");
        bookBtn.setOnAction(e -> switchPage("Book Container"));

        Button payBtn = new Button("💳  Make Outstanding Payment");
        payBtn.setMaxWidth(Double.MAX_VALUE);
        payBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #1E293B; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 8px 12px; -fx-cursor: hand;");
        payBtn.setOnAction(e -> switchPage("Make Payment"));

        box.getChildren().addAll(title, regBtn, bookBtn, payBtn);
        return box;
    }

    // PENDING ACTIONS BOTTOM ROW
    private VBox createModernPendingActions(List<Map<String, Object>> allCargos) {
        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 16px; -fx-border-color: #E3EAF3; -fx-border-radius: 16px;");

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(3);
        Label title = new Label("Action Items & Workflows");
        title.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label subtitle = new Label("Pending steps required to progress your export containers");
        subtitle.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");
        titles.getChildren().addAll(title, subtitle);

        titleRow.getChildren().add(titles);

        HBox actions = new HBox(14);
        actions.setAlignment(Pos.CENTER_LEFT);

        HBox step1 = modernAction("📦", "Register Cargo", "Register description & weight details", "Register Cargo", "#1769FF");
        HBox step2 = modernAction("🚢", "Container Allocation", "Reserve port yard containers", "Book Container", "#7C4DFF");
        HBox step3 = modernAction("📄", "Document Upload", "Submit customs declarations & invoices", "Upload Document", "#00A98F");
        HBox step4 = modernAction("💳", "Port Duty Payment", "Settle handling and freight charges", "Make Payment", "#F28C28");

        HBox.setHgrow(step1, Priority.ALWAYS);
        HBox.setHgrow(step2, Priority.ALWAYS);
        HBox.setHgrow(step3, Priority.ALWAYS);
        HBox.setHgrow(step4, Priority.ALWAYS);

        actions.getChildren().addAll(step1, step2, step3, step4);
        box.getChildren().addAll(titleRow, actions);
        return box;
    }

    private HBox modernAction(String icon, String titleText, String description, String buttonTarget, String color) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14));
        box.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12px; -fx-border-color: #E2E8F0; -fx-border-radius: 12px;");

        Label iconLabel = new Label(icon);
        iconLabel.setMinSize(36, 36);
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setStyle("-fx-background-color: " + color + "18; -fx-background-radius: 8px; -fx-font-size: 16px;");

        VBox textBox = new VBox(2);
        Label title = new Label(titleText);
        title.setStyle("-fx-text-fill: #0F172A; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label desc = new Label(description);
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
        textBox.getChildren().addAll(title, desc);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Button actionButton = new Button("Open →");
        actionButton.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 6px 12px; -fx-cursor: hand;");
        actionButton.setOnAction(e -> switchPage(buttonTarget));

        box.getChildren().addAll(iconLabel, textBox, actionButton);
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}