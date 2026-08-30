package com.projectui.view.ShipCaptain;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.projectui.model.Ship;
import com.projectui.service.VesselData;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

public class DashBoard {

    // =====================================================
    // COLOR PALETTE & DESIGN SYSTEM (MATCHING ADMIN)
    // =====================================================
    private static final String BLUE = "#1769E8";
    private static final String DARK = "#0f172a";
    private static final String TEXT = "#1e293b";
    private static final String MUTED = "#64748b";
    private static final String CARD_STYLE = "-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-radius:8; -fx-background-radius:8; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.06), 14, 0, 0, 4);";

    // =====================================================
    // LAYOUT & STATE
    // =====================================================
    private final BorderPane root = new BorderPane();
    private final BorderPane center = new BorderPane();
    private Label headerTitleLabel;
    private final Map<String, Button> navButtons = new HashMap<>();
    private String currentPage = "Dashboard";
    private Stage stage;
    private Runnable logoutAction;

    // =====================================================
    // APPLICATION LIFECYCLE & ENTRY
    // =====================================================
    public void start(Stage stage) {
        this.stage = stage;
        Parent r = getRoot(stage, () -> new com.projectui.view.login.LoginPage().show(stage), "Dashboard");
        com.projectui.view.login.LoginPage.showRoot(stage, r);
        if (stage.getWidth() < 1200) stage.setWidth(1280);
        if (stage.getHeight() < 700) stage.setHeight(800);
        stage.setTitle("DockTech - Ship Captain Dashboard");
    }

    public Parent getRoot() {
        return getRoot(null, null, "Dashboard");
    }

    public static DashBoard activeInstance;

    public static void navigateTo(String pageName) {
        if (activeInstance != null) {
            javafx.application.Platform.runLater(() -> activeInstance.switchPage(pageName));
        }
    }

    public Parent getRoot(Stage stage, Runnable logoutAction, String defaultPage) {
        activeInstance = this;
        this.stage = stage;
        this.logoutAction = logoutAction;
        this.currentPage = (defaultPage == null || defaultPage.isBlank()) ? "Dashboard" : defaultPage.trim();

        root.setPadding(new Insets(0));
        root.setStyle("-fx-background-color:#f5f8fc;");

        center.setPadding(new Insets(18));
        center.setStyle("-fx-background-color:#f5f8fc;");

        Label title = new Label(currentPage);
        title.setStyle("-fx-text-fill:#05070c; -fx-font-size:20; -fx-font-weight:bold;");
        this.headerTitleLabel = title;

        root.setLeft(sidebar(title));
        root.setTop(header(title));
        root.setCenter(center);
        showPage(currentPage);

        // Window resize responsive binding
        root.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene == null) return;
            newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                if (newWin == null) return;
                if (newWin instanceof Stage s) {
                    this.stage = s;
                    root.prefWidthProperty().bind(s.widthProperty());
                    root.prefHeightProperty().bind(s.heightProperty());
                }
            });
        });

        // Listen for live data updates
        CaptainSessionService.addChangeListener(() -> {
            if ("Dashboard".equalsIgnoreCase(currentPage)) {
                center.setCenter(createScrollableDashboard());
            }
        });

        return root;
    }

    public Scene getDashboardScene() {
        return new Scene(getRoot(), 1280, 800);
    }

    // =====================================================
    // SIDEBAR (EXACT ADMIN DIMENSIONS & POSITIONING)
    // =====================================================
    private VBox sidebar(Label headerTitle) {
        VBox nav = new VBox(10);
        nav.setPrefWidth(215);
        nav.setMinWidth(215);
        nav.setMaxWidth(215);
        nav.setPadding(new Insets(20, 12, 18, 12));
        nav.setStyle("-fx-background-color:#051029;");

        // Brand / Logo matching Admin
        Label brand = new Label("DOCK TECH");
        brand.setStyle("-fx-text-fill:white; -fx-font-size:18; -fx-font-weight:bold;");
        Label small = new Label("SMART PORT OPERATIONS");
        small.setStyle("-fx-text-fill:#9aaac0; -fx-font-size:10; -fx-font-weight:bold;");
        Label logoIcon = new Label("⚓");
        logoIcon.setStyle("-fx-text-fill:white; -fx-font-size:20; -fx-font-weight:bold; -fx-padding:0 4 0 0;");
        VBox textBox = new VBox(2, brand, small);
        HBox brandBox = new HBox(8, logoIcon, textBox);
        brandBox.setPadding(new Insets(0, 0, 22, 6));

        Button dashboard = navButton("Dashboard");
        Button ship = navButton("View Assigned Ship");
        Button status = navButton("Update Ship Status");
        Button loading = navButton("Confirm Loading");
        Button document = navButton("Upload Document");
        Button sos = navButton("Send SOS");
        Button settings = navButton("Settings");

        navButtons.clear();
        navButtons.put("Dashboard", dashboard);
        navButtons.put("View Assigned Ship", ship);
        navButtons.put("Update Ship Status", status);
        navButtons.put("Confirm Loading", loading);
        navButtons.put("Upload Document", document);
        navButtons.put("Send SOS", sos);
        navButtons.put("Settings", settings);

        dashboard.setOnAction(event -> switchPage("Dashboard", headerTitle));
        ship.setOnAction(event -> switchPage("View Assigned Ship", headerTitle));
        status.setOnAction(event -> switchPage("Update Ship Status", headerTitle));
        loading.setOnAction(event -> switchPage("Confirm Loading", headerTitle));
        document.setOnAction(event -> switchPage("Upload Document", headerTitle));
        sos.setOnAction(event -> switchPage("Send SOS", headerTitle));
        settings.setOnAction(event -> switchPage("Settings", headerTitle));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        nav.getChildren().addAll(brandBox, dashboard, ship, status, loading, document, sos, settings, spacer);
        return nav;
    }

    private Button navButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(navStyle(false, text.equalsIgnoreCase(currentPage)));
        button.setOnMouseEntered(event -> button.setStyle(navStyle(true, text.equalsIgnoreCase(currentPage))));
        button.setOnMouseExited(event -> button.setStyle(navStyle(false, text.equalsIgnoreCase(currentPage))));
        return button;
    }

    private String navStyle(boolean hover, boolean selected) {
        return "-fx-background-color:" + (hover || selected ? BLUE : "transparent") + ";"
                + "-fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold;"
                + "-fx-padding:12 16; -fx-background-radius:8; -fx-cursor:hand;";
    }

    // =====================================================
    // HEADER (EXACT ADMIN SIZING & ALIGNMENT)
    // =====================================================
    private HBox header(Label title) {
        HBox header = new HBox(14);
        header.setPadding(new Insets(12, 18, 12, 18));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:white; -fx-border-color:transparent transparent #e8edf3 transparent;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label notification = new Label("🔔");
        notification.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16;");

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-text-fill:#6b7280; -fx-font-size:13;");

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm:ss a");
        Label timeLabel = new Label(LocalTime.now().format(timeFmt));
        timeLabel.setStyle("-fx-text-fill:#6b7280; -fx-font-size:13; -fx-padding:0 6 0 6;");

        final Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            try {
                timeLabel.setText(LocalTime.now().format(timeFmt));
            } catch (Exception ignored) {}
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        header.sceneProperty().addListener((obsS, oldS, newS) -> {
            if (newS == null) {
                clock.stop();
            } else {
                newS.windowProperty().addListener((obsW, oldW, newW) -> {
                    if (newW == null) clock.stop();
                    else newW.setOnHidden(e -> clock.stop());
                });
            }
        });

        Label system = new Label("● System Operational");
        system.setStyle("-fx-text-fill:#047857; -fx-background-color:#ecfdf5; -fx-background-radius:8; -fx-padding:6 12; -fx-font-size:13; -fx-font-weight:bold;");

        String captainName = CaptainSessionService.getCaptainName();
        String initials = "SC";
        String nameTrim = captainName.trim();
        if (!nameTrim.isEmpty()) {
            String[] parts = nameTrim.split("\\s+");
            String a = parts[0].substring(0, 1);
            String b = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
            initials = (a + b).toUpperCase();
        }

        Button profile = new Button(initials);
        profile.setStyle("-fx-background-color:" + BLUE + "; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-background-radius:999; -fx-min-width:38; -fx-min-height:38; -fx-pref-width:38; -fx-pref-height:38; -fx-cursor:hand;");

        profile.setOnAction(evt -> {
            Window owner = profile.getScene() == null ? null : profile.getScene().getWindow();
            showCaptainProfilePopup(owner, profile, title);
        });

        header.getChildren().addAll(title, spacer, notification, dateLabel, timeLabel, system, profile);
        return header;
    }

    private void showCaptainProfilePopup(Window owner, Button profile, Label title) {
        String nameVal = CaptainSessionService.getCaptainName();
        String emailVal = CaptainSessionService.getCaptainEmail();
        Ship s = CaptainSessionService.getAssignedShip();
        String shipVal = s != null ? s.getShipName() : "Not Assigned";

        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox box = new VBox(10);
        box.setPadding(new Insets(14, 16, 14, 16));
        box.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#e2e8f0; -fx-border-radius:12; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.12), 16, 0, 0, 4);");

        Label name = new Label(nameVal);
        name.setStyle("-fx-font-weight:bold; -fx-font-size:15; -fx-text-fill:#0f172a;");
        Label email = new Label(emailVal);
        email.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");
        Label shipLbl = new Label("Vessel: " + shipVal);
        shipLbl.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:12; -fx-font-weight:bold;");

        VBox headerBox = new VBox(2, name, email, shipLbl);

        Button viewProfile = new Button("👤  Profile & Vessel Details");
        viewProfile.setStyle("-fx-background-color:transparent; -fx-text-fill:#0f172a; -fx-font-size:14; -fx-alignment:center-left; -fx-padding:8 6; -fx-cursor:hand;");
        viewProfile.setOnAction(ae -> {
            popup.hide();
            switchPage("View Assigned Ship", title);
        });

        Button settingsBtn = new Button("⚙  Account Settings");
        settingsBtn.setStyle("-fx-background-color:transparent; -fx-text-fill:#0f172a; -fx-font-size:14; -fx-alignment:center-left; -fx-padding:8 6; -fx-cursor:hand;");
        settingsBtn.setOnAction(ae -> {
            popup.hide();
            switchPage("Settings", title);
        });

        Separator sep = new Separator();

        Button logoutBtn = new Button("🚪  Logout");
        logoutBtn.setStyle("-fx-background-color:transparent; -fx-text-fill:#ef4444; -fx-font-size:14; -fx-alignment:center-left; -fx-padding:8 6; -fx-cursor:hand; -fx-font-weight:bold;");
        logoutBtn.setOnAction(ae -> {
            popup.hide();
            showLogoutConfirmation();
        });

        box.getChildren().addAll(headerBox, viewProfile, settingsBtn, sep, logoutBtn);
        popup.getContent().add(box);

        Bounds b = profile.localToScreen(profile.getBoundsInLocal());
        double popupWidth = 220;
        double x = b.getMaxX() - popupWidth;
        double y = b.getMaxY() + 6;
        popup.show(profile.getScene().getWindow(), x, y);
    }

    private void showLogoutConfirmation() {
        Stage ownerStage = null;
        if (center != null && center.getScene() != null && center.getScene().getWindow() instanceof Stage s) {
            ownerStage = s;
        } else if (stage != null) {
            ownerStage = stage;
        }
        final Stage finalStage = ownerStage;
        com.projectui.view.login.LogoutModal.show(ownerStage, () -> {
            if (logoutAction != null) {
                logoutAction.run();
            } else if (finalStage != null) {
                new com.projectui.view.login.LoginPage().show(finalStage);
            }
        });
    }

    // =====================================================
    // PAGE ROUTING & NAVIGATION
    // =====================================================
    public void switchPage(String pageName) {
        switchPage(pageName, headerTitleLabel);
    }

    private void switchPage(String pageName, Label headerTitle) {
        if (headerTitle != null) {
            headerTitle.setText(pageName);
        } else if (headerTitleLabel != null) {
            headerTitleLabel.setText(pageName);
        }
        showPage(pageName);
    }

    private void showPage(String pageName) {
        currentPage = pageName == null || pageName.isBlank() ? "Dashboard" : pageName.trim();
        if (headerTitleLabel != null) {
            headerTitleLabel.setText(currentPage);
        }
        refreshActiveNav();

        if ("View Assigned Ship".equalsIgnoreCase(currentPage)) {
            center.setCenter(new ViewAssignedShip().getContent());
        } else if ("Update Ship Status".equalsIgnoreCase(currentPage)) {
            center.setCenter(new UpdateShipStatus().getContent());
        } else if ("Confirm Loading".equalsIgnoreCase(currentPage)) {
            center.setCenter(new ConfirmLoading().getContent());
        } else if ("Upload Document".equalsIgnoreCase(currentPage) || "Upload Documents".equalsIgnoreCase(currentPage)) {
            center.setCenter(new UploadDocuments().getContent());
        } else if ("Send SOS".equalsIgnoreCase(currentPage)) {
            center.setCenter(new SendSOS().getContent());
        } else if ("Settings".equalsIgnoreCase(currentPage)) {
            center.setCenter(new SettingsPage().getContent());
        } else {
            center.setCenter(createScrollableDashboard());
        }
    }

    private void refreshActiveNav() {
        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            entry.getValue().setStyle(navStyle(false, entry.getKey().equalsIgnoreCase(currentPage)));
        }
    }

    // =====================================================
    // CAPTAIN DASHBOARD CONTENT (MATCHING ADMIN STRUCTURE)
    // =====================================================
    private ScrollPane createScrollableDashboard() {
        VBox main = new VBox(14);
        main.setPadding(new Insets(10, 14, 20, 14));
        main.setStyle("-fx-background-color:transparent;");

        Ship ship = CaptainSessionService.getAssignedShip();
        VesselData v = CaptainSessionService.getAssignedVesselData();
        List<Map<String, Object>> cargos = CaptainSessionService.getAssignedCargos();
        List<Map<String, Object>> containers = CaptainSessionService.getAssignedContainers();

        // 1. Dynamic Greeting Header with subtle maritime visual and concise info tags
        main.getChildren().add(buildGreetingHeader(ship, v));

        if (ship == null) {
            main.getChildren().add(buildEmptyState());
        } else {
            main.getChildren().addAll(
                    buildSummaryKpis(ship, v, cargos),
                    buildMainRow(ship, v),
                    buildVoyageProgressSection(ship, v),
                    buildCargoAndOpsRow(ship, cargos, containers),
                    buildQuickOpsRow()
            );
        }

        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color:#f5f8fc; -fx-background:transparent; -fx-border-color:transparent;");
        return scroll;
    }

    // =====================================================
    // DYNAMIC GREETING & MARITIME HEADER
    // =====================================================
    private VBox buildGreetingHeader(Ship ship, VesselData v) {
        int hour = LocalTime.now().getHour();
        String timeGreeting;
        if (hour >= 5 && hour < 12) {
            timeGreeting = "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            timeGreeting = "Good Afternoon";
        } else if (hour >= 17 && hour < 21) {
            timeGreeting = "Good Evening";
        } else {
            timeGreeting = "Good Night";
        }

        String captainName = CaptainSessionService.getCaptainName();
        String welcomeTitle = timeGreeting + ", Captain " + captainName + " 👋";
        String welcomeSub = "Here's your vessel and voyage overview for today.";

        String shipName = ship != null ? ship.getShipName() : "No Vessel Assigned";
        String status = ship != null ? (ship.getStatus() != null ? ship.getStatus() : (v != null ? v.navigationStatus : "At Berth")) : "Unassigned";
        String destination = ship != null && ship.getDeparture() != null && !ship.getDeparture().isBlank() ? ship.getDeparture() : (v != null ? v.destination : "Port of Singapore");
        String lastSync = v != null && v.timestamp != null ? v.timestamp : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // Outer Greeting Banner Card
        HBox banner = new HBox(20);
        banner.setPadding(new Insets(18, 22, 18, 22));
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setStyle("-fx-background-color:linear-gradient(to right, #0b1e3b, #102a52, #173868); -fx-background-radius:10; -fx-effect:dropshadow(gaussian, rgba(11,30,59,0.18), 16, 0, 0, 4);");

        // Left Side: Text and Badges
        VBox leftBox = new VBox(8);
        HBox.setHgrow(leftBox, Priority.ALWAYS);

        Label titleLbl = new Label(welcomeTitle);
        titleLbl.setStyle("-fx-text-fill:white; -fx-font-size:20; -fx-font-weight:bold;");

        Label subLbl = new Label(welcomeSub);
        subLbl.setStyle("-fx-text-fill:#cbd5e1; -fx-font-size:13;");

        HBox tagsRow = new HBox(8);
        tagsRow.setAlignment(Pos.CENTER_LEFT);
        tagsRow.setPadding(new Insets(4, 0, 0, 0));

        tagsRow.getChildren().addAll(
                headerTag("👨‍✈️ Captain", captainName, "#e2e8f0", "rgba(255,255,255,0.12)"),
                headerTag("⚓ Vessel", shipName, "#93c5fd", "rgba(59,130,246,0.2)"),
                headerTag("➔ Voyage", destination, "#a7f3d0", "rgba(16,185,129,0.2)"),
                headerTag("● Status", status, "#fde68a", "rgba(245,158,11,0.2)"),
                headerTag("● LIVE", "Updated " + lastSync, "#6ee7b7", "rgba(16,185,129,0.25)")
        );

        leftBox.getChildren().addAll(titleLbl, subLbl, tagsRow);

        // Right Side: Subtle Maritime Graphic / Compass Badge
        VBox rightVisual = createSubtleMaritimeVisual();

        banner.getChildren().addAll(leftBox, rightVisual);
        return new VBox(banner);
    }

    private VBox createSubtleMaritimeVisual() {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10, 16, 10, 16));
        box.setStyle("-fx-background-color:rgba(255,255,255,0.08); -fx-background-radius:8; -fx-border-color:rgba(255,255,255,0.15); -fx-border-radius:8;");

        Label radarIcon = new Label("🧭");
        radarIcon.setStyle("-fx-font-size:24; -fx-text-fill:#60a5fa;");

        Label label = new Label("MARITIME OPS");
        label.setStyle("-fx-text-fill:#93c5fd; -fx-font-size:10; -fx-font-weight:bold; -fx-letter-spacing:1px;");

        Label livePill = new Label("● AIS ONLINE");
        livePill.setStyle("-fx-text-fill:#34d399; -fx-font-size:9; -fx-font-weight:bold;");

        box.getChildren().addAll(radarIcon, label, livePill);
        return box;
    }

    private HBox headerTag(String label, String value, String textColor, String bg) {
        Label l = new Label(label + ": ");
        l.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:11; -fx-font-weight:bold;");
        Label v = new Label(value != null && !value.isBlank() ? value : "N/A");
        v.setStyle("-fx-text-fill:" + textColor + "; -fx-font-size:12; -fx-font-weight:bold;");
        HBox tag = new HBox(3, l, v);
        tag.setAlignment(Pos.CENTER_LEFT);
        tag.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:6; -fx-padding:4 10;");
        return tag;
    }

    private VBox buildEmptyState() {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(60, 30, 60, 30));
        card.setStyle(CARD_STYLE);

        Label icon = new Label("⚓");
        icon.setStyle("-fx-font-size:44; -fx-text-fill:#94a3b8;");

        Label msg = new Label("No Vessel Assigned");
        msg.setStyle("-fx-font-size:18; -fx-font-weight:bold; -fx-text-fill:#334155;");

        Label sub = new Label("Please contact the Port Authority or Fleet Operations to assign a vessel to your account.");
        sub.setStyle("-fx-font-size:14; -fx-text-fill:#64748b;");

        card.getChildren().addAll(icon, msg, sub);
        return card;
    }

    private HBox buildSummaryKpis(Ship ship, VesselData v, List<Map<String, Object>> cargos) {
        String status = ship.getStatus() != null ? ship.getStatus() : (v != null ? v.navigationStatus : "At Berth");
        String location = ship.getBerth() != null && !ship.getBerth().isBlank() ? ship.getBerth() : "Port of Mumbai";
        String dest = ship.getDeparture() != null && !ship.getDeparture().isBlank() ? ship.getDeparture() : (v != null ? v.destination : "Port of Singapore");
        String eta = ship.getArrival() != null && !ship.getArrival().isBlank() ? ship.getArrival() : "10:30 AM";

        int totalCargos = cargos.size();
        int loadedCargos = 0;
        for (Map<String, Object> c : cargos) {
            String st = c.getOrDefault("status", "").toString().toLowerCase();
            if (st.contains("loaded") || st.contains("completed") || st.contains("verified")) {
                loadedCargos++;
            }
        }
        int progressPct = totalCargos > 0 ? (int) ((loadedCargos * 100.0) / totalCargos) : 100;

        // KPI 3: Loading progress card with robust spacing and no text clipping
        VBox loadingCard = createLoadingProgressKpiCard(progressPct, loadedCargos, totalCargos);

        HBox kpis = new HBox(12,
                kpiCard("⚓", "Assigned Vessel", ship.getShipName(), "In Command", "#1d4ed8", "#eaf3ff"),
                kpiCard("●", "Vessel Status", status, "Live Navigational State", "#0f9f6e", "#e7f8ef"),
                loadingCard,
                kpiCard("⌖", "Current Location", location, "Port / Berth", "#d97706", "#fef3c7"),
                kpiCard("➔", "Destination / ETA", dest, "ETA: " + eta, "#0ea5e9", "#e0f2fe")
        );
        kpis.getChildren().forEach(n -> {
            HBox.setHgrow(n, Priority.ALWAYS);
            if (n instanceof VBox vb) vb.setMinWidth(170);
        });
        return kpis;
    }

    private VBox createLoadingProgressKpiCard(int progressPct, int loadedCargos, int totalCargos) {
        Label iconLbl = new Label("▣");
        iconLbl.setStyle("-fx-font-size:16; -fx-text-fill:#6d4aff; -fx-font-weight:bold; -fx-background-color:#f0edff; -fx-background-radius:6; -fx-padding:6 10;");

        Label t = new Label("Loading Progress");
        t.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:13; -fx-font-weight:bold;");

        HBox top = new HBox(8, iconLbl, t);
        top.setAlignment(Pos.CENTER_LEFT);

        Label v = new Label(progressPct + "%");
        v.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:20; -fx-font-weight:bold;");

        ProgressBar miniBar = new ProgressBar(progressPct / 100.0);
        miniBar.setMaxWidth(Double.MAX_VALUE);
        miniBar.setPrefHeight(6);
        miniBar.setStyle("-fx-accent:#6d4aff;");

        String subText = loadedCargos + " of " + totalCargos + " Cargos Loaded";
        Label s = new Label(subText);
        s.setWrapText(true);
        s.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12;");

        VBox card = new VBox(6, top, v, miniBar, s);
        card.setPadding(new Insets(16));
        card.setStyle(CARD_STYLE);
        return card;
    }

    private HBox buildMainRow(Ship ship, VesselData v) {
        HBox row = new HBox(12);

        // Left Panel: Live Vessel Telemetry
        VBox telemetryCard = new VBox(12);
        telemetryCard.setPadding(new Insets(16));
        telemetryCard.setStyle(CARD_STYLE);
        HBox.setHgrow(telemetryCard, Priority.ALWAYS);

        Label title1 = new Label("Live Vessel Telemetry & Movement");
        title1.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        Label liveBadge = new Label("● LIVE");
        liveBadge.setStyle("-fx-text-fill:#047857; -fx-background-color:#ecfdf5; -fx-background-radius:6; -fx-padding:3 8; -fx-font-size:11; -fx-font-weight:bold;");

        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);

        String lastUpdate = v != null && v.timestamp != null ? v.timestamp : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Label updateTime = new Label("Last AIS update: " + lastUpdate);
        updateTime.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        HBox headerRow = new HBox(8, title1, liveBadge, sp1, updateTime);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        GridPane g1 = new GridPane();
        g1.setHgap(16);
        g1.setVgap(10);

        String speedStr = v != null ? String.format("%.1f Knots", v.speed) : "12.5 Knots";
        String courseStr = v != null ? String.format("%.1f°", v.course) : "184.0°";
        String coords = v != null ? String.format("%.4f° N, %.4f° E", v.latitude, v.longitude) : "18.9439° N, 72.8354° E";

        g1.add(fieldCell("Speed over Ground", speedStr), 0, 0);
        g1.add(fieldCell("Heading & Course", courseStr), 1, 0);
        g1.add(fieldCell("GPS Coordinates", coords), 0, 1);
        g1.add(fieldCell("Navigation Status", v != null ? v.navigationStatus : "Underway Using Engine"), 1, 1);

        telemetryCard.getChildren().addAll(headerRow, g1);

        // Right Panel: Voyage & Route Overview
        VBox voyageCard = new VBox(12);
        voyageCard.setPadding(new Insets(16));
        voyageCard.setStyle(CARD_STYLE);
        HBox.setHgrow(voyageCard, Priority.ALWAYS);

        Label title2 = new Label("Current Voyage & Route Snapshot");
        title2.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        GridPane g2 = new GridPane();
        g2.setHgap(16);
        g2.setVgap(10);

        String origin = ship.getArrival() != null && !ship.getArrival().isBlank() ? "Port of Chennai" : "Port of Mumbai";
        String dest = ship.getDeparture() != null && !ship.getDeparture().isBlank() ? ship.getDeparture() : (v != null ? v.destination : "Port of Singapore");
        String berth = ship.getBerth() != null && !ship.getBerth().isBlank() ? ship.getBerth() : "Berth B04";

        g2.add(fieldCell("Origin Port", origin), 0, 0);
        g2.add(fieldCell("Destination Port", dest), 1, 0);
        g2.add(fieldCell("Assigned Berth", berth), 0, 1);
        g2.add(fieldCell("Voyage Clearance", "Approved & Active"), 1, 1);

        voyageCard.getChildren().addAll(title2, g2);

        row.getChildren().addAll(telemetryCard, voyageCard);
        return row;
    }

    private VBox buildVoyageProgressSection(Ship ship, VesselData v) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle(CARD_STYLE);

        Label title = new Label("Voyage & Route Progress");
        title.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        String origin = ship.getArrival() != null && !ship.getArrival().isBlank() ? "Port of Chennai" : "Port of Mumbai";
        String dest = ship.getDeparture() != null && !ship.getDeparture().isBlank() ? ship.getDeparture() : (v != null ? v.destination : "Port of Singapore");
        String currentLoc = ship.getBerth() != null && !ship.getBerth().isBlank() ? ship.getBerth() : "Port of Mumbai (Berth B04)";
        String eta = ship.getArrival() != null && !ship.getArrival().isBlank() ? ship.getArrival() : "10:30 AM";
        String status = ship.getStatus() != null ? ship.getStatus() : (v != null ? v.navigationStatus : "At Berth");

        double progress = 0.50;
        String stLower = status.toLowerCase();
        if (stLower.contains("loading") || stLower.contains("berth")) {
            progress = 0.20;
        } else if (stLower.contains("underway") || stLower.contains("transit")) {
            progress = 0.65;
        } else if (stLower.contains("arrival") || stLower.contains("completed")) {
            progress = 0.95;
        }

        HBox timelineLabels = new HBox();
        Label lblOrigin = new Label("⚓ Departure: " + origin);
        lblOrigin.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:12; -fx-font-weight:bold;");

        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);

        Label lblMid = new Label("● Current: " + currentLoc + " (" + status + ")");
        lblMid.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:12; -fx-font-weight:bold;");

        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        Label lblDest = new Label("🏁 Destination: " + dest + " (ETA: " + eta + ")");
        lblDest.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:12; -fx-font-weight:bold;");

        timelineLabels.getChildren().addAll(lblOrigin, sp1, lblMid, sp2, lblDest);

        ProgressBar pBar = new ProgressBar(progress);
        pBar.setMaxWidth(Double.MAX_VALUE);
        pBar.setPrefHeight(8);
        pBar.setStyle("-fx-accent:" + BLUE + ";");

        HBox statsRow = new HBox(20,
                miniStat("Route Clearance", "Port Authority Approved"),
                miniStat("Distance Remaining", "840 NM (Est.)"),
                miniStat("Voyage Progress", (int)(progress * 100) + "% Completed"),
                miniStat("Current Transit Speed", v != null ? String.format("%.1f Knots", v.speed) : "12.5 Knots")
        );

        card.getChildren().addAll(title, timelineLabels, pBar, statsRow);
        return card;
    }

    private HBox buildCargoAndOpsRow(Ship ship, List<Map<String, Object>> cargos, List<Map<String, Object>> containers) {
        HBox row = new HBox(12);

        // Left: Cargo / Loading Progress Overview
        VBox cargoOverview = new VBox(12);
        cargoOverview.setPadding(new Insets(16));
        cargoOverview.setStyle(CARD_STYLE);
        HBox.setHgrow(cargoOverview, Priority.ALWAYS);

        Label cTitle = new Label("Cargo & Container Loading Overview");
        cTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        int totalCargos = cargos.size();
        int loadedCargos = 0;
        double totalWeight = 0;
        for (Map<String, Object> c : cargos) {
            String st = c.getOrDefault("status", "").toString().toLowerCase();
            if (st.contains("loaded") || st.contains("completed") || st.contains("verified")) {
                loadedCargos++;
            }
            try {
                if (c.get("weight") != null) {
                    totalWeight += Double.parseDouble(c.get("weight").toString().replaceAll("[^0-9.]", ""));
                }
            } catch (Exception ignored) {}
        }
        double pct = totalCargos > 0 ? ((double) loadedCargos / totalCargos) : 1.0;

        ProgressBar bar = new ProgressBar(pct);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(8);
        bar.setStyle("-fx-accent:" + BLUE + ";");

        HBox statsRow = new HBox(16,
                miniStat("Total Cargo", totalCargos + " Items"),
                miniStat("Total Weight", String.format("%.0f Tons", totalWeight > 0 ? totalWeight : 850.0)),
                miniStat("Containers", containers.size() + " Allocated"),
                miniStat("Loading Status", (int)(pct * 100) + "% Completed")
        );

        cargoOverview.getChildren().addAll(cTitle, statsRow, bar);

        // Right: Recent Operational Activity
        VBox opsCard = new VBox(12);
        opsCard.setPadding(new Insets(16));
        opsCard.setStyle(CARD_STYLE);
        HBox.setHgrow(opsCard, Priority.ALWAYS);

        Label opsTitle = new Label("Recent Operational Activity & Alerts");
        opsTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        VBox list = new VBox(8,
                activityItem("✓", "Vessel Status Verified", "Assigned status synchronized with Port Authority", "10:20 AM", "#0f9f6e"),
                activityItem("▣", "Cargo Manifest Checked", "Cargo loading verification in progress", "10:30 AM", "#1d4ed8"),
                activityItem("🔒", "Port Security Clearance", "Customs & Immigration clearance approved", "11:15 AM", "#6d4aff")
        );

        opsCard.getChildren().addAll(opsTitle, list);

        row.getChildren().addAll(cargoOverview, opsCard);
        return row;
    }

    private VBox buildQuickOpsRow() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle(CARD_STYLE);

        Label qTitle = new Label("Quick Operational Actions");
        qTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        HBox buttons = new HBox(10);

        Button updateStatus = actionBtn("◆  Update Ship Status", "#0f9f6e", "#ecfdf5");
        Button loadingBtn = actionBtn("▣  Confirm Loading", "#d97706", "#fef3c7");
        Button uploadDoc = actionBtn("📄  Upload Document", "#1d4ed8", "#eff6ff");
        Button sosBtn = actionBtn("🚨  Emergency SOS", "#dc2626", "#fee2e2");

        updateStatus.setOnAction(e -> switchPage("Update Ship Status"));
        loadingBtn.setOnAction(e -> switchPage("Confirm Loading"));
        uploadDoc.setOnAction(e -> switchPage("Upload Document"));
        sosBtn.setOnAction(e -> switchPage("Send SOS"));

        buttons.getChildren().addAll(updateStatus, loadingBtn, uploadDoc, sosBtn);
        buttons.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        card.getChildren().addAll(qTitle, buttons);
        return card;
    }

    private Button actionBtn(String text, String color, String bg) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + color + "; -fx-border-color:" + color + "33; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:10 14; -fx-background-radius:6; -fx-border-radius:6; -fx-cursor:hand;");
        return b;
    }

    private HBox activityItem(String icon, String title, String sub, String time, String color) {
        Label i = new Label(icon);
        i.setStyle("-fx-text-fill:" + color + "; -fx-font-weight:bold; -fx-font-size:13;");
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-weight:bold; -fx-font-size:13;");
        Label s = new Label(sub);
        s.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12;");
        VBox tb = new VBox(1, t, s);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label tm = new Label(time);
        tm.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:11;");

        HBox item = new HBox(8, i, tb, sp, tm);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    private VBox miniStat(String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:11; -fx-font-weight:bold;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:14; -fx-font-weight:bold;");
        return new VBox(2, l, v);
    }

    private VBox fieldCell(String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12; -fx-font-weight:bold;");
        Label v = new Label(value != null && !value.isBlank() ? value : "N/A");
        v.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:14; -fx-font-weight:bold;");
        return new VBox(3, l, v);
    }

    private VBox kpiCard(String icon, String title, String value, String sub, String accentColor, String bgColor) {
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size:16; -fx-text-fill:" + accentColor + "; -fx-font-weight:bold; -fx-background-color:" + bgColor + "; -fx-background-radius:6; -fx-padding:6 10;");

        Label t = new Label(title);
        t.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:13; -fx-font-weight:bold;");

        HBox top = new HBox(8, iconLbl, t);
        top.setAlignment(Pos.CENTER_LEFT);

        Label v = new Label(value != null ? value : "-");
        v.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:20; -fx-font-weight:bold;");

        Label s = new Label(sub);
        s.setWrapText(true);
        s.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12;");

        VBox card = new VBox(6, top, v, s);
        card.setPadding(new Insets(16));
        card.setStyle(CARD_STYLE);
        return card;
    }
}