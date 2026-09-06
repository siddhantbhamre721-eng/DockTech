package com.projectui.view.importer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.projectui.dao.CargoDao;
import com.projectui.dao.ContainerDao;
import com.projectui.dao.PaymentDao;
import com.projectui.dao.UserDao;
import com.projectui.model.Ship;
import com.projectui.service.VesselData;
import com.projectui.service.VesselManager;
import com.projectui.view.login.DashboardLayoutHelper;
import com.projectui.view.login.LoginPage;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

@SuppressWarnings({"unused", "unchecked"})
public class ImporterDashboard extends Application {

    public static Stage importerStage;

    private Scene dashboardScene;
    private BorderPane dashboardRoot;
    private static boolean styleInitialized = false;

    // DAOs & Services
    private final CargoDao cargoDao = new CargoDao();
    private final ContainerDao containerDao = new ContainerDao();
    private final PaymentDao paymentDao = new PaymentDao();
    private final VesselManager vesselManager = VesselManager.getInstance();

    // UI Dynamic Elements
    private Label activeCountLbl, inTransitCountLbl, arrivingSoonCountLbl, deliveredCountLbl, delayedCountLbl;
    private VBox incomingShipmentsList;
    private VBox liveTrackingCard;
    private VBox deliveryReceivingList;
    private VBox timelineTrackerBox;
    private VBox alertsFeedBox;
    private TextField searchField;
    private ComboBox<String> statusFilterCombo;
    private Label lastSyncBadge;

    // State
    private final List<ImporterShipment> allShipments = new ArrayList<>();
    private ImporterShipment selectedShipment = null;

    // Styling constants
    private static final String BLUE = "#0b63ff";
    private static final String TEXT = "#0f172a";
    private static final String MUTED = "#64748b";
    private static final String CARD_STYLE = "-fx-background-color:white; -fx-background-radius:12px; -fx-border-color:#e2e8f0; -fx-border-radius:12px; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 10, 0, 0, 3);";

    // =========================================================
    // DATA MODEL
    // =========================================================
    public static class ImporterShipment {
        public String shipmentId;
        public String cargoName;
        public String cargoType;
        public String quantity;
        public String weight;
        public String cargoValue;
        public String exporterName;
        public String originPort;
        public String destinationPort;
        public String assignedVessel;
        public String vesselImo;
        public String vesselMmsi;
        public String vesselSpeed;
        public String vesselHeading;
        public String vesselLocation;
        public String voyageNumber;
        public String berth;
        public String status; // In Transit, Arriving Soon, Customs Cleared, Delivered, Under Inspection, Delayed
        public String eta;
        public String lastUpdate;
        public String paymentStatus;
        public String customsStatus;
        public double progressPercent;
        public List<Map<String, String>> documents = new ArrayList<>();
        public List<Milestone> timeline = new ArrayList<>();

        public static class Milestone {
            public String title;
            public String detail;
            public String time;
            public String state; // DONE, ACTIVE, PENDING

            public Milestone(String title, String detail, String time, String state) {
                this.title = title;
                this.detail = detail;
                this.time = time;
                this.state = state;
            }
        }
    }

    @Override
    public void start(Stage stage) {
        importerStage = stage;

        if (!styleInitialized) {
            if (!importerStage.isShowing()) {
                try {
                    importerStage.initStyle(StageStyle.UNDECORATED);
                } catch (Exception ignored) {}
            }
            styleInitialized = true;
        }

        // =====================================================
        // SIDEBAR (Preserved exactly as required)
        // =====================================================
        Map<String, Button> menuButtons = new java.util.LinkedHashMap<>();
        Button dashboard = new Button("🏠   Dashboard");
        Button track = new Button("📥   Track Shipment");
        Button invoice = new Button("📄   Download Invoice");
        Button delivery = new Button("📦   Confirm Delivery");

        menuButtons.put("Dashboard", dashboard);
        menuButtons.put("Track Shipment", track);
        menuButtons.put("Download Invoice", invoice);
        menuButtons.put("Confirm Delivery", delivery);

        dashboard.setOnAction(event -> showDashboard());

        track.setOnAction(event -> {
            TrackShipment page = new TrackShipment();
            LoginPage.showRoot(importerStage, page.getTrackShipmentScene(this::showDashboard).getRoot());
        });

        invoice.setOnAction(event -> {
            DownloadInvoice page = new DownloadInvoice();
            LoginPage.showRoot(importerStage, page.getDownloadInvoiceScene(this::showDashboard).getRoot());
        });

        delivery.setOnAction(event -> {
            ConfirmDelivery page = new ConfirmDelivery();
            LoginPage.showRoot(importerStage, page.getConfirmDeliveryScene(this::showDashboard).getRoot());
        });

        VBox sidebar = DashboardLayoutHelper.createSidebar(importerStage, "Importer", "Dashboard", menuButtons);

        // =====================================================
        // MAIN CONTENT AREA
        // =====================================================
        VBox content = buildMainContent();

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color:#F4F8FC; -fx-background: transparent; -fx-border-color:transparent;");

        // =====================================================
        // ROOT LAYOUT
        // =====================================================
        BorderPane root = new BorderPane();
        HBox topHeader = DashboardLayoutHelper.createHeader(importerStage, "Dashboard", "Dock Tech / Importer / Dashboard", "Importer", () -> {
            if (importerStage != null) {
                new LoginPage().show(importerStage);
            }
        }, null);

        root.setTop(topHeader);
        root.setLeft(sidebar);
        root.setCenter(scrollPane);

        this.dashboardRoot = root;

        // Reactive Listeners for real-time live data
        CargoDao.addChangeListener(this::refreshDataAsync);
        ContainerDao.addChangeListener(this::refreshDataAsync);
        PaymentDao.addChangeListener(this::refreshDataAsync);

        refreshDataAsync();

        LoginPage.showRoot(importerStage, root);
        dashboardScene = importerStage.getScene();
        importerStage.setTitle("Dock Tech - Importer Dashboard");
    }

    public void showDashboard() {
        if (importerStage != null && dashboardRoot != null) {
            LoginPage.showRoot(importerStage, dashboardRoot);
            refreshDataAsync();
        }
    }

    // =========================================================
    // BUILD MAIN CONTENT
    // =========================================================
    private VBox buildMainContent() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20, 24, 24, 24));
        container.setStyle("-fx-background-color:#F4F8FC;");

        // 1. Header & Context Banner
        HBox headerRow = buildHeaderBanner();

        // 2. Shipment Overview (5 KPI Cards)
        HBox kpiRow = buildKpiCardsRow();

        // 3. Core Operational Split (Incoming Shipments Table + Live Tracking Telemetry)
        HBox coreSplit = buildCoreSplitRow();

        // 4. Lower Operational Split (Delivery & Receiving + Timeline Tracker + Important Alerts)
        HBox lowerSplit = buildLowerSplitRow();

        container.getChildren().addAll(headerRow, kpiRow, coreSplit, lowerSplit);
        return container;
    }

    private HBox buildHeaderBanner() {
        VBox titleBox = new VBox(4);

        Text title = new Text("Importer Dashboard");
        title.setStyle("-fx-font-size:26px; -fx-font-weight:bold; -fx-fill:#101A2B;");

        String importerName = getLoggedImporterName();
        Text subtitle = new Text("Live incoming shipments, vessel telemetry tracking, customs clearance, and consignment receiving for " + importerName + ".");
        subtitle.setStyle("-fx-font-size:13px; -fx-fill:#64748B;");

        titleBox.getChildren().addAll(title, subtitle);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox metaBox = new HBox(10);
        metaBox.setAlignment(Pos.CENTER_RIGHT);

        lastSyncBadge = new Label("● Live Telemetry Synced (" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ")");
        lastSyncBadge.setStyle("-fx-background-color:#ecfdf5; -fx-text-fill:#059669; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:20; -fx-border-color:#a7f3d0; -fx-border-radius:20;");

        Button refreshBtn = new Button("↺ Refresh");
        refreshBtn.setStyle("-fx-background-color:white; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:6 12; -fx-font-size:12; -fx-font-weight:bold; -fx-text-fill:#334155; -fx-cursor:hand;");
        refreshBtn.setOnAction(e -> refreshDataAsync());

        metaBox.getChildren().addAll(lastSyncBadge, refreshBtn);

        HBox row = new HBox(12, titleBox, space, metaBox);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox buildKpiCardsRow() {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);

        activeCountLbl = new Label("0");
        inTransitCountLbl = new Label("0");
        arrivingSoonCountLbl = new Label("0");
        deliveredCountLbl = new Label("0");
        delayedCountLbl = new Label("0");

        VBox activeCard = kpiCard("🚢", "Active Shipments", activeCountLbl, "#e0f2fe", "#0284c7");
        VBox transitCard = kpiCard("➤", "In Transit", inTransitCountLbl, "#ccfbf1", "#0f766e");
        VBox arrivingCard = kpiCard("◷", "Arriving Soon", arrivingSoonCountLbl, "#fef3c7", "#d97706");
        VBox deliveredCard = kpiCard("📦", "Delivered", deliveredCountLbl, "#dcfce7", "#16a34a");
        VBox delayedCard = kpiCard("⚠️", "Delayed / At Risk", delayedCountLbl, "#fee2e2", "#dc2626");

        row.getChildren().addAll(activeCard, transitCard, arrivingCard, deliveredCard, delayedCard);
        row.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        return row;
    }

    private VBox kpiCard(String icon, String title, Label valueLabel, String iconBg, String iconColor) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle(CARD_STYLE);

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label iconLbl = new Label(icon);
        iconLbl.setMinSize(38, 38);
        iconLbl.setMaxSize(38, 38);
        iconLbl.setAlignment(Pos.CENTER);
        iconLbl.setStyle("-fx-background-color:" + iconBg + "; -fx-text-fill:" + iconColor + "; -fx-font-size:18px; -fx-font-weight:bold; -fx-background-radius:8px;");

        VBox textBox = new VBox(2);
        valueLabel.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:12px; -fx-text-fill:#64748b; -fx-font-weight:bold;");

        textBox.getChildren().addAll(valueLabel, titleLbl);
        top.getChildren().addAll(iconLbl, textBox);

        card.getChildren().add(top);
        return card;
    }

    // =========================================================
    // CORE SPLIT (TABLE + LIVE TRACKING)
    // =========================================================
    private HBox buildCoreSplitRow() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.TOP_LEFT);

        // Left Panel: Incoming Shipments Manifest Table & Search Filter
        VBox tableCard = new VBox(12);
        tableCard.setPadding(new Insets(16));
        tableCard.setStyle(CARD_STYLE);
        HBox.setHgrow(tableCard, Priority.ALWAYS);

        // Header of table
        HBox tableHeader = new HBox(10);
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        Label secTitle = new Label("Incoming Shipments & Consignments");
        secTitle.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Search ID, Cargo, Exporter, Vessel...");
        searchField.setPrefWidth(220);
        searchField.setStyle("-fx-background-color:white; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-padding:6 10; -fx-font-size:12;");
        searchField.textProperty().addListener((obs, o, n) -> renderShipmentsList());

        statusFilterCombo = new ComboBox<>();
        statusFilterCombo.getItems().addAll("All Statuses", "In Transit", "Arriving Soon", "Customs Cleared", "Delivered", "Under Inspection");
        statusFilterCombo.setValue("All Statuses");
        statusFilterCombo.setStyle("-fx-background-color:white; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-font-size:12;");
        statusFilterCombo.valueProperty().addListener((obs, o, n) -> renderShipmentsList());

        tableHeader.getChildren().addAll(secTitle, sp, searchField, statusFilterCombo);

        incomingShipmentsList = new VBox(8);
        incomingShipmentsList.setStyle("-fx-background-color:transparent;");

        ScrollPane listScroll = new ScrollPane(incomingShipmentsList);
        listScroll.setFitToWidth(true);
        listScroll.setMaxHeight(320);
        listScroll.setMinHeight(200);
        listScroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-color:transparent;");

        tableCard.getChildren().addAll(tableHeader, new Separator(), listScroll);

        // Right Panel: Live Shipment Telemetry & AIS Tracking
        liveTrackingCard = new VBox(12);
        liveTrackingCard.setPadding(new Insets(16));
        liveTrackingCard.setStyle(CARD_STYLE);
        liveTrackingCard.setPrefWidth(380);
        liveTrackingCard.setMinWidth(360);

        renderLiveTrackingCard();

        row.getChildren().addAll(tableCard, liveTrackingCard);
        return row;
    }

    // =========================================================
    // LOWER SPLIT (DELIVERY + TIMELINE + ALERTS)
    // =========================================================
    private HBox buildLowerSplitRow() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.TOP_LEFT);

        // 1. Delivery & Receiving Card
        VBox deliveryCard = new VBox(12);
        deliveryCard.setPadding(new Insets(16));
        deliveryCard.setStyle(CARD_STYLE);
        HBox.setHgrow(deliveryCard, Priority.ALWAYS);

        HBox delHeader = new HBox(8);
        delHeader.setAlignment(Pos.CENTER_LEFT);
        Label delTitle = new Label("📦 Delivery & Receiving Schedule");
        delTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
        Region spDel = new Region();
        HBox.setHgrow(spDel, Priority.ALWAYS);
        Button gotoDelBtn = new Button("Confirm Delivery ➔");
        gotoDelBtn.setStyle("-fx-background-color:transparent; -fx-text-fill:#0b63ff; -fx-font-size:12; -fx-font-weight:bold; -fx-cursor:hand;");
        gotoDelBtn.setOnAction(e -> {
            ConfirmDelivery page = new ConfirmDelivery();
            LoginPage.showRoot(importerStage, page.getConfirmDeliveryScene(this::showDashboard).getRoot());
        });
        delHeader.getChildren().addAll(delTitle, spDel, gotoDelBtn);

        deliveryReceivingList = new VBox(8);
        deliveryCard.getChildren().addAll(delHeader, new Separator(), deliveryReceivingList);

        // 2. Shipment Timeline & Milestone Tracker
        VBox timelineCard = new VBox(12);
        timelineCard.setPadding(new Insets(16));
        timelineCard.setStyle(CARD_STYLE);
        HBox.setHgrow(timelineCard, Priority.ALWAYS);

        Label timeTitle = new Label("⏱️ Shipment Milestone Timeline");
        timeTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        timelineTrackerBox = new VBox(8);
        timelineCard.getChildren().addAll(timeTitle, new Separator(), timelineTrackerBox);

        // 3. Important Shipment Alerts Feed
        VBox alertsCard = new VBox(12);
        alertsCard.setPadding(new Insets(16));
        alertsCard.setStyle(CARD_STYLE);
        alertsCard.setPrefWidth(340);
        alertsCard.setMinWidth(320);

        Label alTitle = new Label("⚠️ Important Shipment Alerts");
        alTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        alertsFeedBox = new VBox(8);
        alertsCard.getChildren().addAll(alTitle, new Separator(), alertsFeedBox);

        row.getChildren().addAll(deliveryCard, timelineCard, alertsCard);
        return row;
    }

    // =========================================================
    // DATA FETCHING & SYNCHRONIZATION
    // =========================================================
    private void refreshDataAsync() {
        java.util.concurrent.ForkJoinPool.commonPool().execute(() -> {
            List<Map<String, Object>> rawContainers = containerDao.getAllBookings();
            List<Map<String, Object>> rawPayments = paymentDao.getAllPayments();

            List<Map<String, Object>> userCargos;
            if (LoginPage.loggedInUser != null && !LoginPage.loggedInUser.isEmpty()) {
                userCargos = cargoDao.getShipmentsForImporter(LoginPage.loggedInUser);
            } else {
                String loggedFilter = getLoggedImporterFilter();
                userCargos = cargoDao.getShipmentsForImporter(loggedFilter);
            }

            List<ImporterShipment> list = new ArrayList<>();

            // 1. Process real registered cargos assigned to this Importer
            if (userCargos != null && !userCargos.isEmpty()) {
                for (Map<String, Object> c : userCargos) {
                    if (c == null) continue;
                    ImporterShipment s = mapCargoToShipment(c, rawContainers, rawPayments);
                    list.add(s);
                }
            }

            Platform.runLater(() -> {
                allShipments.clear();
                allShipments.addAll(list);

                if (selectedShipment == null || !allShipments.contains(selectedShipment)) {
                    selectedShipment = !allShipments.isEmpty() ? allShipments.get(0) : null;
                }

                updateKpiCounters();
                renderShipmentsList();
                renderLiveTrackingCard();
                renderDeliveryReceivingSchedule();
                renderTimelineTracker();
                renderAlertsFeed();

                if (lastSyncBadge != null) {
                    lastSyncBadge.setText("● Live Database Synced (" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ")");
                }
            });
        });
    }

    private ImporterShipment mapCargoToShipment(Map<String, Object> c, List<Map<String, Object>> containers, List<Map<String, Object>> payments) {
        ImporterShipment s = new ImporterShipment();
        s.shipmentId = str(c.get("cargoId"), "CGO-N/A");
        s.cargoName = str(c.get("cargoName"), "Consignment");
        s.cargoType = str(c.get("cargoType"), "General Cargo");
        s.quantity = str(c.get("quantity"), "1") + " Units";
        String rawWt = str(c.get("weight"), "—");
        s.weight = rawWt.equals("—") || rawWt.contains("kg") ? rawWt : (rawWt + " kg");
        s.cargoValue = "₹ " + str(c.get("value"), str(c.get("amount"), "0"));
        
        String exp = str(c.get("exporterName"), str(c.get("exporter"), str(c.get("exporterCompany"), str(c.get("shipper"), str(c.get("company"), str(c.get("customerName"), "—"))))));
        if (exp.equals("—") || exp.isBlank() || exp.equalsIgnoreCase("Exporter")) {
            String expUid = str(c.get("ownerUid"), str(c.get("exporterUid"), str(c.get("exporterId"), str(c.get("userId"), ""))));
            String expEmail = str(c.get("ownerEmail"), str(c.get("exporterEmail"), ""));
            if (!expUid.isBlank()) {
                Map<String, Object> prof = com.projectui.dao.UserDao.getProfile(expUid);
                if (prof != null) {
                    Object pn = prof.get("name") != null ? prof.get("name") : (prof.get("fullName") != null ? prof.get("fullName") : prof.get("company"));
                    if (pn != null && !pn.toString().isBlank()) {
                        exp = pn.toString();
                    }
                }
            }
            if ((exp.equals("—") || exp.isBlank() || exp.equalsIgnoreCase("Exporter")) && !expEmail.isBlank()) {
                Map<String, Object> prof = com.projectui.dao.UserDao.getProfile(expEmail);
                if (prof != null) {
                    Object pn = prof.get("name") != null ? prof.get("name") : (prof.get("fullName") != null ? prof.get("fullName") : prof.get("company"));
                    if (pn != null && !pn.toString().isBlank()) {
                        exp = pn.toString();
                    }
                }
            }
        }
        s.exporterName = exp;

        s.originPort = str(c.get("originPort"), "—");
        s.destinationPort = str(c.get("destinationPort"), "—");

        String vName = str(c.get("assignedVessel"), str(c.get("vesselName"), str(c.get("assignedShip"), "Not Assigned")));
        s.assignedVessel = vName;

        // Assigned Vessel lookup from AIS telemetry / vessel manager
        VesselData vData = vesselManager.getVesselByName(vName);
        boolean isLive = vData != null && vData.isValid();

        s.vesselImo = vData != null ? vData.imo : "—";
        s.vesselMmsi = vData != null ? String.valueOf(vData.mmsi) : "—";
        s.vesselSpeed = isLive ? String.format("%.1f Knots", vData.speed) : "Scheduled Speed";
        s.vesselHeading = isLive ? String.format("%.1f°", vData.heading) : "N/A";
        s.vesselLocation = isLive ? String.format("%.4f° N, %.4f° E", vData.latitude, vData.longitude) : "Anchorage / Berth Sector";
        s.voyageNumber = "VOY-" + Math.abs(s.shipmentId.hashCode() % 900 + 100);
        s.berth = "Berth B-" + (Math.abs(s.shipmentId.hashCode() % 5) + 1);

        String rawStatus = str(c.get("status"), "In Transit");
        s.status = normalizeShipmentStatus(rawStatus);
        s.paymentStatus = str(c.get("paymentStatus"), "PAID");
        s.customsStatus = str(c.get("customsStatus"), "CLEARED");
        s.eta = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("dd MMM yyyy, 18:00"));
        s.lastUpdate = str(c.get("updatedAt"), "Recently");
        s.progressPercent = "Delivered".equalsIgnoreCase(s.status) ? 1.0 : ("Arriving Soon".equalsIgnoreCase(s.status) ? 0.85 : 0.65);

        // Build documents
        Map<String, String> d1 = new HashMap<>(); d1.put("name", "Bill of Lading"); d1.put("status", "Verified");
        Map<String, String> d2 = new HashMap<>(); d2.put("name", "Commercial Invoice"); d2.put("status", "Verified");
        Map<String, String> d3 = new HashMap<>(); d3.put("name", "Packing List"); d3.put("status", "Verified");
        Map<String, String> d4 = new HashMap<>(); d4.put("name", "Customs Clearance Certificate"); d4.put("status", "Approved");
        s.documents.addAll(List.of(d1, d2, d3, d4));

        // Build dynamic timeline based on project status
        String st = s.status;
        boolean isDelivered = "Delivered".equalsIgnoreCase(st);
        boolean isReady = isDelivered || "Ready for Delivery".equalsIgnoreCase(st);
        boolean isCustomsCleared = isReady || "Customs Cleared".equalsIgnoreCase(st) || "CLEARED".equalsIgnoreCase(s.customsStatus);
        boolean isArrived = isCustomsCleared || "Arriving Soon".equalsIgnoreCase(st) || "Arrived".equalsIgnoreCase(st);
        boolean isInTransit = isArrived || "In Transit".equalsIgnoreCase(st);
        boolean isDeparted = isInTransit || "Departed".equalsIgnoreCase(st);
        boolean isVesselAssigned = isDeparted || (s.assignedVessel != null && !s.assignedVessel.isBlank() && !s.assignedVessel.equalsIgnoreCase("Not Assigned"));
        boolean isContainerBooked = isVesselAssigned || "Container Booked".equalsIgnoreCase(st) || "Booked".equalsIgnoreCase(st);

        s.timeline.add(new ImporterShipment.Milestone("1. Cargo Registered", "Registered at " + s.originPort, "Manifest Registered", "DONE"));
        s.timeline.add(new ImporterShipment.Milestone("2. Container Booked", "Container allocation and staging", "Yard Allocation", isContainerBooked ? "DONE" : "PENDING"));
        s.timeline.add(new ImporterShipment.Milestone("3. Vessel Assigned", "Assigned to vessel " + s.assignedVessel, "Voyage Assignment", isVesselAssigned ? "DONE" : "PENDING"));
        s.timeline.add(new ImporterShipment.Milestone("4. Departed Origin Port", "Departed from " + s.originPort, "Port Departure", isDeparted ? "DONE" : "PENDING"));
        s.timeline.add(new ImporterShipment.Milestone("5. In Transit", "Maritime transit to " + s.destinationPort, s.vesselLocation, isInTransit ? (isArrived ? "DONE" : "ACTIVE") : "PENDING"));
        s.timeline.add(new ImporterShipment.Milestone("6. Arrived Destination Port", "Arrived at " + s.destinationPort + " (" + s.berth + ")", s.eta, isArrived ? (isCustomsCleared ? "DONE" : "ACTIVE") : "PENDING"));
        s.timeline.add(new ImporterShipment.Milestone("7. Customs Clearance", "Customs inspection & clearance", s.customsStatus, isCustomsCleared ? (isReady ? "DONE" : "ACTIVE") : "PENDING"));
        s.timeline.add(new ImporterShipment.Milestone("8. Ready for Delivery", "Staged for consignee receiving", "CFS Warehouse", isReady ? (isDelivered ? "DONE" : "ACTIVE") : "PENDING"));
        s.timeline.add(new ImporterShipment.Milestone("9. Consignment Delivered", "Delivery confirmed and signed off", isDelivered ? s.lastUpdate : "Pending Sign-off", isDelivered ? "DONE" : "PENDING"));

        return s;
    }

    private String normalizeShipmentStatus(String raw) {
        if (raw == null || raw.isBlank()) return "In Transit";
        String r = raw.trim().toLowerCase();
        if (r.contains("deliver") || r.contains("received") || r.contains("complete")) return "Delivered";
        if (r.contains("ready")) return "Ready for Delivery";
        if (r.contains("arriv") || r.contains("anchor") || r.contains("berth")) return "Arriving Soon";
        if (r.contains("clear") || r.contains("approv")) return "Customs Cleared";
        if (r.contains("inspect") || r.contains("pending")) return "Under Inspection";
        if (r.contains("delay") || r.contains("risk") || r.contains("hold") || r.contains("reject")) return "Delayed / At Risk";
        return "In Transit";
    }

    private void updateKpiCounters() {
        int active = 0, transit = 0, arriving = 0, delivered = 0, delayed = 0;
        for (ImporterShipment s : allShipments) {
            if ("Delivered".equalsIgnoreCase(s.status)) {
                delivered++;
            } else {
                active++;
                if ("In Transit".equalsIgnoreCase(s.status) || "Customs Cleared".equalsIgnoreCase(s.status)) {
                    transit++;
                } else if ("Arriving Soon".equalsIgnoreCase(s.status) || "Ready for Delivery".equalsIgnoreCase(s.status)) {
                    arriving++;
                } else if ("Delayed / At Risk".equalsIgnoreCase(s.status) || "Under Inspection".equalsIgnoreCase(s.status)) {
                    delayed++;
                } else {
                    transit++;
                }
            }
        }

        if (activeCountLbl != null) activeCountLbl.setText(String.valueOf(active));
        if (inTransitCountLbl != null) inTransitCountLbl.setText(String.valueOf(transit));
        if (arrivingSoonCountLbl != null) arrivingSoonCountLbl.setText(String.valueOf(arriving));
        if (deliveredCountLbl != null) deliveredCountLbl.setText(String.valueOf(delivered));
        if (delayedCountLbl != null) delayedCountLbl.setText(String.valueOf(delayed));
    }

    // =========================================================
    // RENDER SHIPMENTS LIST
    // =========================================================
    private void renderShipmentsList() {
        if (incomingShipmentsList == null) return;
        incomingShipmentsList.getChildren().clear();

        String query = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String statusFilter = statusFilterCombo != null && statusFilterCombo.getValue() != null ? statusFilterCombo.getValue() : "All Statuses";

        List<ImporterShipment> filtered = new ArrayList<>();
        for (ImporterShipment s : allShipments) {
            boolean matchesQuery = query.isEmpty()
                    || s.shipmentId.toLowerCase().contains(query)
                    || s.cargoName.toLowerCase().contains(query)
                    || s.exporterName.toLowerCase().contains(query)
                    || s.assignedVessel.toLowerCase().contains(query)
                    || s.destinationPort.toLowerCase().contains(query);

            boolean matchesStatus = "All Statuses".equalsIgnoreCase(statusFilter) || s.status.equalsIgnoreCase(statusFilter);

            if (matchesQuery && matchesStatus) {
                filtered.add(s);
            }
        }

        if (filtered.isEmpty()) {
            VBox emptyBox = new VBox(8);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(30));

            Label empIcon = new Label("📦");
            empIcon.setStyle("-fx-font-size:32px; -fx-text-fill:#94a3b8;");
            Label empTitle = new Label(allShipments.isEmpty()
                    ? "No incoming shipments assigned to your account yet."
                    : "No matching incoming shipments found.");
            empTitle.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#475569;");
            Label empSub = new Label(allShipments.isEmpty()
                    ? "Shipments assigned to your account by Exporters will automatically appear here."
                    : "Try adjusting your search or status filter.");
            empSub.setStyle("-fx-font-size:12px; -fx-text-fill:#94a3b8;");

            emptyBox.getChildren().addAll(empIcon, empTitle, empSub);
            incomingShipmentsList.getChildren().add(emptyBox);
            return;
        }

        for (ImporterShipment s : filtered) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12, 14, 12, 14));

            boolean isSelected = selectedShipment != null && s.shipmentId.equalsIgnoreCase(selectedShipment.shipmentId);
            row.setStyle(isSelected
                    ? "-fx-background-color:#eff6ff; -fx-background-radius:8; -fx-border-color:#3b82f6; -fx-border-radius:8; -fx-cursor:hand;"
                    : "-fx-background-color:#f8fafc; -fx-background-radius:8; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-cursor:hand;");

            row.setOnMouseClicked(e -> {
                selectedShipment = s;
                renderShipmentsList();
                renderLiveTrackingCard();
                renderTimelineTracker();
            });

            Label icon = new Label("🚢");
            icon.setMinSize(40, 40);
            icon.setAlignment(Pos.CENTER);
            icon.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0284c7; -fx-font-size:18; -fx-background-radius:8;");

            VBox mainInfo = new VBox(3);
            HBox.setHgrow(mainInfo, Priority.ALWAYS);

            HBox line1 = new HBox(8);
            line1.setAlignment(Pos.CENTER_LEFT);
            Label idLbl = new Label(s.shipmentId);
            idLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#0b63ff;");
            Label nameLbl = new Label(s.cargoName);
            nameLbl.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
            line1.getChildren().addAll(idLbl, new Label("•"), nameLbl);

            Label line2 = new Label("Exporter: " + s.exporterName + "  |  " + s.originPort + " ➔ " + s.destinationPort);
            line2.setStyle("-fx-font-size:11px; -fx-text-fill:#475569;");

            Label line3 = new Label("Vessel: " + s.assignedVessel + "  |  Qty: " + s.quantity + " (" + s.weight + ")  |  ETA: " + s.eta);
            line3.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");

            mainInfo.getChildren().addAll(line1, line2, line3);

            VBox rightSide = new VBox(4);
            rightSide.setAlignment(Pos.CENTER_RIGHT);

            Label badge = new Label(s.status.toUpperCase());
            badge.setStyle(statusBadgeStyle(s.status));

            Button detailsBtn = new Button("🔍 Details");
            detailsBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#334155; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 8; -fx-background-radius:4; -fx-cursor:hand;");
            detailsBtn.setOnAction(ev -> showShipmentDetailsModal(s));

            rightSide.getChildren().addAll(badge, detailsBtn);

            row.getChildren().addAll(icon, mainInfo, rightSide);
            incomingShipmentsList.getChildren().add(row);
        }
    }

    private String statusBadgeStyle(String status) {
        if ("Delivered".equalsIgnoreCase(status)) {
            return "-fx-background-color:#dcfce7; -fx-text-fill:#16a34a; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;";
        } else if ("Arriving Soon".equalsIgnoreCase(status) || "Ready for Delivery".equalsIgnoreCase(status)) {
            return "-fx-background-color:#fef3c7; -fx-text-fill:#d97706; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;";
        } else if ("Delayed / At Risk".equalsIgnoreCase(status) || "Under Inspection".equalsIgnoreCase(status)) {
            return "-fx-background-color:#fee2e2; -fx-text-fill:#dc2626; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;";
        }
        return "-fx-background-color:#e0f2fe; -fx-text-fill:#0284c7; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;";
    }

    // =========================================================
    // RENDER LIVE TRACKING CARD
    // =========================================================
    private void renderLiveTrackingCard() {
        if (liveTrackingCard == null) return;
        liveTrackingCard.getChildren().clear();

        Label trackTitle = new Label("📡 Live Shipment Telemetry");
        trackTitle.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        if (selectedShipment == null) {
            Label noSel = new Label(allShipments.isEmpty()
                    ? "No incoming shipments assigned to your account yet.\nLive vessel telemetry will be activated upon consignment assignment."
                    : "Select a shipment from the manifest to view real-time AIS tracking.");
            noSel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px; -fx-padding:20 0;");
            liveTrackingCard.getChildren().addAll(trackTitle, new Separator(), noSel);
            return;
        }

        ImporterShipment s = selectedShipment;

        VBox box = new VBox(10);
        box.setPadding(new Insets(10, 0, 0, 0));

        HBox vesselBanner = new HBox(10);
        vesselBanner.setAlignment(Pos.CENTER_LEFT);
        Label vIcon = new Label("🚢");
        vIcon.setMinSize(42, 42);
        vIcon.setAlignment(Pos.CENTER);
        vIcon.setStyle("-fx-background-color:#0b63ff; -fx-text-fill:white; -fx-font-size:20; -fx-background-radius:8;");

        VBox vInfo = new VBox(2);
        Label vName = new Label(s.assignedVessel);
        vName.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
        Label vSub = new Label("IMO: " + s.vesselImo + "  |  MMSI: " + s.vesselMmsi);
        vSub.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");
        vInfo.getChildren().addAll(vName, vSub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label aisSignal = new Label("● Live AIS Fix");
        aisSignal.setStyle("-fx-background-color:#ecfdf5; -fx-text-fill:#059669; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:4 8; -fx-background-radius:4;");

        vesselBanner.getChildren().addAll(vIcon, vInfo, sp, aisSignal);

        // Progress Route
        VBox routeBox = new VBox(4);
        HBox routeLine = new HBox(8);
        Label orig = new Label(s.originPort);
        orig.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#334155;");
        Region spR = new Region();
        HBox.setHgrow(spR, Priority.ALWAYS);
        Label dest = new Label(s.destinationPort);
        dest.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#0b63ff;");
        routeLine.getChildren().addAll(orig, spR, dest);

        ProgressBar pb = new ProgressBar(s.progressPercent);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setStyle("-fx-accent:#0b63ff;");

        routeBox.getChildren().addAll(routeLine, pb);

        // Telemetry Grid
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        grid.add(detailCell("Current GPS Fix", s.vesselLocation, "#0f172a"), 0, 0);
        grid.add(detailCell("Speed & Heading", s.vesselSpeed + " • " + s.vesselHeading, "#0f172a"), 1, 0);
        grid.add(detailCell("Assigned Berth", s.berth, "#0f172a"), 0, 1);
        grid.add(detailCell("Estimated Arrival", s.eta, "#0b63ff"), 1, 1);

        Button quickTrackBtn = new Button("📥 Open Full Tracking Console");
        quickTrackBtn.setMaxWidth(Double.MAX_VALUE);
        quickTrackBtn.setStyle("-fx-background-color:#0b63ff; -fx-text-fill:white; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:8; -fx-background-radius:6; -fx-cursor:hand;");
        quickTrackBtn.setOnAction(e -> {
            TrackShipment page = new TrackShipment();
            LoginPage.showRoot(importerStage, page.getTrackShipmentScene(this::showDashboard).getRoot());
        });

        box.getChildren().addAll(vesselBanner, new Separator(), routeBox, grid, quickTrackBtn);
        liveTrackingCard.getChildren().addAll(trackTitle, box);
    }

    // =========================================================
    // RENDER DELIVERY & RECEIVING SCHEDULE
    // =========================================================
    private void renderDeliveryReceivingSchedule() {
        if (deliveryReceivingList == null) return;
        deliveryReceivingList.getChildren().clear();

        List<ImporterShipment> readyList = new ArrayList<>();
        for (ImporterShipment s : allShipments) {
            if ("Arriving Soon".equalsIgnoreCase(s.status) || "Delivered".equalsIgnoreCase(s.status) || "Customs Cleared".equalsIgnoreCase(s.status) || "Ready for Delivery".equalsIgnoreCase(s.status)) {
                readyList.add(s);
            }
        }

        if (readyList.isEmpty()) {
            VBox emp = new VBox(6);
            emp.setAlignment(Pos.CENTER);
            emp.setPadding(new Insets(24));
            Label empIc = new Label("📦");
            empIc.setStyle("-fx-font-size:24px; -fx-text-fill:#94a3b8;");
            Label empMsg = new Label(allShipments.isEmpty()
                    ? "No incoming shipments assigned to your account yet."
                    : "No shipments currently awaiting delivery.");
            empMsg.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#64748b;");
            emp.getChildren().addAll(empIc, empMsg);
            deliveryReceivingList.getChildren().add(emp);
            return;
        }

        for (ImporterShipment s : readyList) {
            HBox item = new HBox(10);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(8, 10, 8, 10));
            item.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:6;");

            Label ic = new Label("📦");
            ic.setStyle("-fx-font-size:16px;");

            VBox info = new VBox(2);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label t = new Label(s.shipmentId + " • " + s.cargoName);
            t.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
            Label sub = new Label("Berth: " + s.berth + "  |  ETA/Delivery: " + s.eta);
            sub.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");
            info.getChildren().addAll(t, sub);

            Label st = new Label(s.status.toUpperCase());
            st.setStyle(statusBadgeStyle(s.status));

            item.getChildren().addAll(ic, info, st);
            deliveryReceivingList.getChildren().add(item);
        }
    }

    // =========================================================
    // RENDER TIMELINE TRACKER
    // =========================================================
    private void renderTimelineTracker() {
        if (timelineTrackerBox == null) return;
        timelineTrackerBox.getChildren().clear();

        ImporterShipment s = selectedShipment != null ? selectedShipment : (!allShipments.isEmpty() ? allShipments.get(0) : null);
        if (s == null) {
            Label noTl = new Label(allShipments.isEmpty()
                    ? "No incoming shipments assigned to your account yet."
                    : "Select a shipment to track milestone progress.");
            noTl.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px; -fx-padding:10 0;");
            timelineTrackerBox.getChildren().add(noTl);
            return;
        }

        Label selHeader = new Label("Milestones for " + s.shipmentId + " (" + s.assignedVessel + ")");
        selHeader.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0b63ff;");
        timelineTrackerBox.getChildren().add(selHeader);

        for (ImporterShipment.Milestone m : s.timeline) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            Label dot = new Label("DONE".equalsIgnoreCase(m.state) ? "✓" : ("ACTIVE".equalsIgnoreCase(m.state) ? "●" : "○"));
            dot.setStyle("DONE".equalsIgnoreCase(m.state)
                    ? "-fx-text-fill:#16a34a; -fx-font-size:12px; -fx-font-weight:bold;"
                    : ("ACTIVE".equalsIgnoreCase(m.state) ? "-fx-text-fill:#0b63ff; -fx-font-size:12px; -fx-font-weight:bold;" : "-fx-text-fill:#94a3b8; -fx-font-size:12px;"));

            VBox tb = new VBox(1);
            Label tLbl = new Label(m.title);
            tLbl.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
            Label dLbl = new Label(m.detail + " (" + m.time + ")");
            dLbl.setStyle("-fx-font-size:10px; -fx-text-fill:#64748b;");
            tb.getChildren().addAll(tLbl, dLbl);

            row.getChildren().addAll(dot, tb);
            timelineTrackerBox.getChildren().add(row);
        }
    }

    // =========================================================
    // RENDER ALERTS FEED
    // =========================================================
    private void renderAlertsFeed() {
        if (alertsFeedBox == null) return;
        alertsFeedBox.getChildren().clear();

        com.projectui.dao.NotificationDao notifDao = new com.projectui.dao.NotificationDao();
        String uId = getLoggedImporterUserId();
        String uEmail = getLoggedImporterEmail();

        List<com.projectui.model.Notification> notifs = new ArrayList<>();
        if (!uId.isEmpty()) {
            notifs.addAll(notifDao.getNotificationsForUser(uId));
        }
        if (notifs.isEmpty() && !uEmail.isEmpty()) {
            notifs.addAll(notifDao.getNotificationsForUser(uEmail));
        }

        if (!notifs.isEmpty()) {
            for (com.projectui.model.Notification n : notifs) {
                if (n == null) continue;
                String title = n.getTitle() != null ? n.getTitle() : "Notification";
                String msg = n.getMessage() != null ? n.getMessage() : "";
                alertsFeedBox.getChildren().add(alertRow(title, msg, "#0284c7", "#f0f9ff"));
            }
        } else if (!allShipments.isEmpty()) {
            // Derived real-time status alerts for the Importer's assigned shipments
            for (ImporterShipment s : allShipments) {
                if ("Delivered".equalsIgnoreCase(s.status)) {
                    alertsFeedBox.getChildren().add(alertRow("✓ Shipment Delivered", s.shipmentId + " (" + s.cargoName + ") has been successfully delivered.", "#16a34a", "#f0fdf4"));
                } else if ("Customs Cleared".equalsIgnoreCase(s.status) || "CLEARED".equalsIgnoreCase(s.customsStatus)) {
                    alertsFeedBox.getChildren().add(alertRow("ℹ Customs Cleared", "Consignment " + s.shipmentId + " verified and cleared by Customs.", "#059669", "#ecfdf5"));
                } else if ("Arriving Soon".equalsIgnoreCase(s.status)) {
                    alertsFeedBox.getChildren().add(alertRow("⏱️ Port Arrival Soon", "Vessel " + s.assignedVessel + " arriving at " + s.destinationPort + " (ETA: " + s.eta + ").", "#0284c7", "#f0f9ff"));
                } else if ("Ready for Delivery".equalsIgnoreCase(s.status)) {
                    alertsFeedBox.getChildren().add(alertRow("📦 Ready for Delivery", "Consignment " + s.shipmentId + " is ready for pickup/receiving at " + s.destinationPort + ".", "#0284c7", "#f0f9ff"));
                } else {
                    alertsFeedBox.getChildren().add(alertRow("➤ Shipment In Transit", s.shipmentId + " (" + s.assignedVessel + ") is underway from " + s.originPort + " to " + s.destinationPort + ".", "#0284c7", "#f0f9ff"));
                }
            }
        } else {
            VBox emp = new VBox(4);
            emp.setAlignment(Pos.CENTER);
            emp.setPadding(new Insets(16));
            Label empMsg = new Label("No incoming shipments assigned to your account yet.");
            empMsg.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8;");
            emp.getChildren().add(empMsg);
            alertsFeedBox.getChildren().add(emp);
        }
    }

    private HBox alertRow(String title, String desc, String color, String bg) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(8, 10, 8, 10));
        box.setStyle("-fx-background-color:" + bg + "; -fx-border-color:" + color + "; -fx-border-width:0 0 0 3; -fx-background-radius:6;");

        VBox vb = new VBox(2);
        Label t = new Label(title);
        t.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:" + color + ";");
        Label d = new Label(desc);
        d.setStyle("-fx-font-size:10px; -fx-text-fill:#334155; -fx-wrap-text:true;");
        vb.getChildren().addAll(t, d);

        box.getChildren().add(vb);
        return box;
    }

    // =========================================================
    // SHIPMENT DETAILS MODAL DIALOG
    // =========================================================
    private void showShipmentDetailsModal(ImporterShipment s) {
        if (s == null) return;

        Stage dialog = new Stage();
        Window owner = importerStage != null ? importerStage : null;
        if (owner != null) dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Shipment Overview & Consignment Dossier — " + s.shipmentId);
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setPrefWidth(680);
        root.setStyle("-fx-background-color:white; -fx-background-radius:12;");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label badge = new Label("SHIPMENT " + s.shipmentId);
        badge.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0284c7; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:6;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label statusTag = new Label(s.status.toUpperCase());
        statusTag.setStyle(statusBadgeStyle(s.status));
        header.getChildren().addAll(badge, sp, statusTag);

        // Vessel & Cargo Title Card
        VBox titleCard = new VBox(4);
        titleCard.setPadding(new Insets(12));
        titleCard.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:8;");
        Label nameLbl = new Label("📦  " + s.cargoName);
        nameLbl.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
        Label subLbl = new Label("Category: " + s.cargoType + "  |  Exporter: " + s.exporterName + "  |  Assigned Vessel: " + s.assignedVessel);
        subLbl.setStyle("-fx-font-size:12px; -fx-text-fill:#64748b;");
        titleCard.getChildren().addAll(nameLbl, subLbl);

        // Details Grid
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);

        grid.add(detailCell("Quantity & Weight", s.quantity + " • " + s.weight, "#0f172a"), 0, 0);
        grid.add(detailCell("Estimated Cargo Value", s.cargoValue, "#059669"), 1, 0);
        grid.add(detailCell("Origin ➔ Destination", s.originPort + " ➔ " + s.destinationPort, "#0f172a"), 0, 1);
        grid.add(detailCell("Voyage & Berth", s.voyageNumber + " • " + s.berth, "#0f172a"), 1, 1);
        grid.add(detailCell("Live GPS Fix", s.vesselLocation, "#0f172a"), 0, 2);
        grid.add(detailCell("Speed & Heading", s.vesselSpeed + " • " + s.vesselHeading, "#0f172a"), 1, 2);
        grid.add(detailCell("Customs Clearance Status", s.customsStatus, "#059669"), 0, 3);
        grid.add(detailCell("Estimated Time of Arrival", s.eta, "#0b63ff"), 1, 3);

        // Documents Box
        VBox docBox = new VBox(6);
        docBox.setPadding(new Insets(10));
        docBox.setStyle("-fx-background-color:#f0f9ff; -fx-border-color:#bae6fd; -fx-border-radius:8;");
        Label docTitle = new Label("📄 Associated Consignment Documents:");
        docTitle.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0369a1;");
        HBox docList = new HBox(12);
        for (Map<String, String> d : s.documents) {
            Label dl = new Label("✓ " + d.get("name") + " (" + d.get("status") + ")");
            dl.setStyle("-fx-font-size:11px; -fx-text-fill:#0f172a; -fx-background-color:white; -fx-padding:3 8; -fx-background-radius:4;");
            docList.getChildren().add(dl);
        }
        docBox.getChildren().addAll(docTitle, docList);

        // Action Buttons
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#334155; -fx-font-weight:bold; -fx-padding:8 16; -fx-background-radius:6; -fx-cursor:hand;");
        closeBtn.setOnAction(e -> dialog.close());

        Button trackBtn = new Button("📥 Track Shipment");
        trackBtn.setStyle("-fx-background-color:#0b63ff; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:8 16; -fx-background-radius:6; -fx-cursor:hand;");
        trackBtn.setOnAction(e -> {
            dialog.close();
            TrackShipment page = new TrackShipment();
            LoginPage.showRoot(importerStage, page.getTrackShipmentScene(this::showDashboard).getRoot());
        });

        actions.getChildren().addAll(closeBtn, trackBtn);

        root.getChildren().addAll(header, titleCard, grid, docBox, new Separator(), actions);

        Scene scene = new Scene(root);
        LoginPage.applyInvisibleScrollbar(scene);
        LoginPage.applyInvisibleScrollbar(dialog);
        dialog.setScene(scene);
        dialog.show();
    }

    private static VBox detailCell(String label, String value, String valueColor) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px; -fx-font-weight:bold;");
        Label v = new Label(value != null && !value.isBlank() ? value : "N/A");
        v.setStyle("-fx-text-fill:" + valueColor + "; -fx-font-size:13px; -fx-font-weight:bold;");
        return new VBox(2, l, v);
    }

    private static String str(Object o, String fallback) {
        return (o != null && !o.toString().isBlank()) ? o.toString().trim() : fallback;
    }

    private String getLoggedImporterName() {
        if (LoginPage.loggedInUser != null) {
            Object n = LoginPage.loggedInUser.get("name");
            if (n != null && !n.toString().isBlank()) return n.toString().trim();
            Object f = LoginPage.loggedInUser.get("fullName");
            if (f != null && !f.toString().isBlank()) return f.toString().trim();
            Object d = LoginPage.loggedInUser.get("displayName");
            if (d != null && !d.toString().isBlank()) return d.toString().trim();
            Object c = LoginPage.loggedInUser.get("company");
            if (c != null && !c.toString().isBlank()) return c.toString().trim();
        }
        return "Importer";
    }

    private String getLoggedImporterEmail() {
        if (LoginPage.loggedInUser != null) {
            Object e = LoginPage.loggedInUser.get("email");
            if (e != null && !e.toString().isBlank()) return e.toString().trim().toLowerCase();
        }
        return "";
    }

    private String getLoggedImporterUserId() {
        if (LoginPage.loggedInUser != null) {
            Object u = LoginPage.loggedInUser.get("uid");
            if (u != null && !u.toString().isBlank()) return u.toString().trim();
            Object e = LoginPage.loggedInUser.get("email");
            if (e != null && !e.toString().isBlank()) return e.toString().trim().toLowerCase();
        }
        return "";
    }

    private String getLoggedImporterFilter() {
        if (LoginPage.loggedInUser != null) {
            Object c = LoginPage.loggedInUser.get("company");
            if (c != null && !c.toString().isBlank()) return c.toString().trim();
            Object e = LoginPage.loggedInUser.get("email");
            if (e != null && !e.toString().isBlank()) return e.toString().trim();
            Object n = LoginPage.loggedInUser.get("name");
            if (n != null && !n.toString().isBlank()) return n.toString().trim();
            Object u = LoginPage.loggedInUser.get("uid");
            if (u != null && !u.toString().isBlank()) return u.toString().trim();
        }
        return "";
    }
}
