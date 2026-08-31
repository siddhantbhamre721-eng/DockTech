package com.projectui.view.portauthority;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.projectui.dao.NotificationDao;
import com.projectui.dao.ShipDAO;
import com.projectui.dao.UserDao;
import com.projectui.model.Notification;
import com.projectui.model.Ship;
import com.projectui.service.FirebaseAuthService;
import com.projectui.service.VesselData;
import com.projectui.service.VesselFinderService;
import com.projectui.service.VesselManager;
import com.projectui.view.admin.AdminDataStore;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@SuppressWarnings({ "unchecked", "unused", "deprecation" })
public class DashboardPage {

    private final VBox root = new VBox(16);

    // Services (Same source of truth as Admin Map & Admin Backend)
    private final VesselManager vesselManager = VesselManager.getInstance();
    private final VesselFinderService vesselFinderService = VesselFinderService.getInstance();
    private final AdminDataStore adminDataStore = AdminDataStore.getInstance();
    private final UserDao userDao = new UserDao();

    // Authenticated Port Authority Context
    private String userAssignedPort = "Cochin Port";
    private String activeViewingPort = "Cochin Port";

    // Header & Telemetry Freshness UI
    private Label portHeadingLabel;
    private Label portSubLabel;
    private Label dataFreshnessBadge;
    private Label lastSyncLabel;

    // Summary Metric KPI Cards
    private Label totalShipsVal;
    private Label arrivingVal;
    private Label anchoredVal;
    private Label berthedVal;
    private Label departingVal;
    private Label aisStatusVal;

    // Live Vessel Table
    private final ObservableList<VesselData> portVesselsMasterList = FXCollections.observableArrayList();
    private FilteredList<VesselData> filteredVesselList;
    private SortedList<VesselData> sortedVesselList;
    private TableView<VesselData> vesselTable;
    private TextField searchField;
    private ComboBox<String> statusFilterCombo;
    private ComboBox<String> typeFilterCombo;
    private ComboBox<String> sortCombo;

    // Selected Vessel Details Panel
    private VBox selectedVesselDetailsBox;
    private VesselData currentlySelectedVessel = null;

    // Operational Breakdown Widgets
    private VBox statusBreakdownBox;
    private VBox typeBreakdownBox;
    private VBox activityFeedBox;
    private VBox alertsFeedBox;

    // Port Personnel Lists & Tables (Captains & Customs Officers)
    private final ObservableList<Map<String, Object>> captainsList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> officersList = FXCollections.observableArrayList();
    private TableView<Map<String, Object>> captainsTable;
    private TableView<Map<String, Object>> officersTable;
    private Label totalCaptainsVal, activeCaptainsVal, assignedCaptainsVal, availCaptainsVal;
    private Label totalOfficersVal, activeOfficersVal, assignedOfficersVal, availOfficersVal;

    // State Tracking
    private long lastSuccessfulUpdateEpoch = System.currentTimeMillis();
    private final Map<Long, Double> lastKnownSpeeds = new HashMap<>();
    private final Map<Long, String> lastKnownStatuses = new HashMap<>();
    private final List<String> recentActivityItems = new ArrayList<>();

    public DashboardPage() {
        root.setSpacing(16);
        root.setPadding(new Insets(0));
        root.setStyle("-fx-background-color:transparent;");

        resolveAssignedPort();
        buildDashboardLayout();
        connectLiveAISService();
        loadPersonnelData();

        com.projectui.service.SosEmergencyService.addChangeListener(() -> {
            Platform.runLater(() -> {
                updateAlerts(filteredVesselList != null ? filteredVesselList : portVesselsMasterList);
            });
        });
    }

    // =====================================================
    // PORT CONTEXT RESOLUTION (NO HARDCODING)
    // =====================================================
    private void resolveAssignedPort() {
        String port = null;
        if (com.projectui.view.login.LoginPage.loggedInUser != null) {
            Map<String, Object> user = com.projectui.view.login.LoginPage.loggedInUser;
            if (user.get("assignedPort") != null && !user.get("assignedPort").toString().isBlank()) {
                port = user.get("assignedPort").toString().trim();
            } else if (user.get("organization") != null && !user.get("organization").toString().isBlank()) {
                port = user.get("organization").toString().trim();
            } else if (user.get("company") != null && !user.get("company").toString().isBlank()) {
                port = user.get("company").toString().trim();
            } else if (user.get("name") != null && !user.get("name").toString().isBlank()) {
                port = user.get("name").toString().trim();
            } else if (user.get("email") != null && !user.get("email").toString().isBlank()) {
                port = user.get("email").toString().trim();
            }
        }

        if (port != null && !port.isBlank()) {
            userAssignedPort = normalizePortName(port);
        } else {
            userAssignedPort = "Cochin Port";
        }
        activeViewingPort = userAssignedPort;
    }

    private String normalizePortName(String raw) {
        return UserDao.normalizePort(raw);
    }

    // =====================================================
    // LAYOUT COMPOSITION
    // =====================================================
    private void buildDashboardLayout() {
        // 1. Header Banner
        VBox header = buildHeader();

        // 2. Summary KPI Cards
        HBox summaryCards = buildSummaryCardsRow();

        // 3. Main Center Split (Table + Selected Vessel Details)
        HBox centerSplit = buildCenterSplit();

        // 4. Bottom Split (Recent Activity + Status/Types Breakdown + Important Alerts)
        HBox bottomSplit = buildBottomSplit();

        // 5. Port Personnel & Duty Management Section (Captains & Custom Officers)
        VBox personnelSection = buildPersonnelManagementSection();

        root.getChildren().addAll(
                header,
                summaryCards,
                centerSplit,
                bottomSplit,
                personnelSection);
    }

    // =====================================================
    // 1. HEADER BANNER
    // =====================================================
    private VBox buildHeader() {
        VBox container = new VBox(4);

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        portHeadingLabel = new Label(activeViewingPort + " — Operations Dashboard");
        portHeadingLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:22px; -fx-font-weight:bold;");

        portSubLabel = new Label(
                "Live Vessel Traffic & Maritime Operational Control • Assigned Port: " + userAssignedPort);
        portSubLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:13px;");
        titleBox.getChildren().addAll(portHeadingLabel, portSubLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Data Freshness Badge
        dataFreshnessBadge = new Label("● LIVE — AIS Active");
        dataFreshnessBadge.setStyle(
                "-fx-text-fill:#10b981; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(16,185,129,0.12); -fx-padding:6 12; -fx-background-radius:999;");

        lastSyncLabel = new Label("Synced: Just now");
        lastSyncLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px;");

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.setStyle(
                "-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 14; -fx-cursor:hand;");
        refreshBtn.setOnAction(e -> refreshDashboardData());

        topRow.getChildren().addAll(titleBox, spacer, dataFreshnessBadge, lastSyncLabel, refreshBtn);
        container.getChildren().add(topRow);
        return container;
    }

    // =====================================================
    // 2. SUMMARY KPI CARDS
    // =====================================================
    private HBox buildSummaryCardsRow() {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);

        totalShipsVal = new Label("0");
        arrivingVal = new Label("0");
        anchoredVal = new Label("0");
        berthedVal = new Label("0");
        departingVal = new Label("0");
        aisStatusVal = new Label("Connected");

        VBox cTotal = createKpiCard("Total Ships", totalShipsVal, "Assigned to " + activeViewingPort, "🚢", "#0284c7",
                "#e0f2fe");
        VBox cArriving = createKpiCard("Arriving", arrivingVal, "Inbound Channel Approach", "📥", "#3b82f6", "#eff6ff");
        VBox cAnchored = createKpiCard("Anchored", anchoredVal, "Outer Roads / Waiting", "⚓", "#d97706", "#fef3c7");
        VBox cBerthed = createKpiCard("Berthed / Moored", berthedVal, "Active Quayside Operations", "🏢", "#16a34a",
                "#ecfdf5");
        VBox cDeparting = createKpiCard("Departing", departingVal, "Outbound Cleared Vessels", "📤", "#8b5cf6",
                "#f5f3ff");
        VBox cAis = createKpiCard("Live AIS Status", aisStatusVal, "Admin Map Telemetry Sync", "📡", "#10b981",
                "#dcfce7");

        row.getChildren().addAll(cTotal, cArriving, cAnchored, cBerthed, cDeparting, cAis);

        for (Node n : row.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
            if (n instanceof VBox) {
                ((VBox) n).setMaxWidth(Double.MAX_VALUE);
            }
        }
        return row;
    }

    private VBox createKpiCard(String title, Label valLabel, String subtitle, String iconStr, String color, String bg) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle(
                "-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(iconStr);
        icon.setStyle("-fx-text-fill:" + color + "; -fx-font-size:15px; -fx-font-weight:bold; -fx-background-color:"
                + bg + "; -fx-padding:4 8; -fx-background-radius:6;");

        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px; -fx-font-weight:bold;");

        top.getChildren().addAll(icon, t);

        valLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:26px; -fx-font-weight:bold;");

        Label sub = new Label(subtitle);
        sub.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:11px;");

        card.getChildren().addAll(top, valLabel, sub);
        return card;
    }

    // =====================================================
    // 3. CENTER SPLIT: TABLE (LEFT) & DETAILS (RIGHT)
    // =====================================================
    private HBox buildCenterSplit() {
        HBox split = new HBox(14);
        split.setAlignment(Pos.TOP_LEFT);

        // Left 65%: Live Vessel Operations Table
        VBox tableBox = buildLiveVesselTableSection();
        HBox.setHgrow(tableBox, Priority.ALWAYS);

        // Right 35%: Selected Vessel Details Panel
        selectedVesselDetailsBox = buildSelectedVesselDetailsSection();
        selectedVesselDetailsBox.setPrefWidth(400);
        selectedVesselDetailsBox.setMinWidth(380);
        selectedVesselDetailsBox.setMaxWidth(440);

        split.getChildren().addAll(tableBox, selectedVesselDetailsBox);
        return split;
    }

    private VBox buildLiveVesselTableSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        box.setStyle(
                "-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        // Controls bar: Search, Status Filter, Vessel Type Filter, Sort
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label tblTitle = new Label("Live Vessel Operations");
        tblTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:15px; -fx-font-weight:bold;");
        Label tblSub = new Label("Live AIS telemetry feed for " + activeViewingPort);
        tblSub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(tblTitle, tblSub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("🔍 Search Vessel / IMO / MMSI...");
        searchField.setPrefWidth(210);
        searchField.setPrefHeight(34);
        searchField.setStyle(
                "-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-padding:5 10;");
        searchField.textProperty().addListener((obs, ov, nv) -> applyFiltersAndSort());

        statusFilterCombo = new ComboBox<>();
        statusFilterCombo.getItems().addAll("All Statuses", "Arriving", "Anchored", "Berthed", "Departing", "Underway");
        statusFilterCombo.setValue("All Statuses");
        statusFilterCombo.setPrefHeight(34);
        statusFilterCombo.setStyle(
                "-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-pref-width:130;");
        statusFilterCombo.setOnAction(e -> applyFiltersAndSort());

        typeFilterCombo = new ComboBox<>();
        typeFilterCombo.getItems().addAll("All Types", "Container", "Tanker", "Bulk Carrier", "General Cargo",
                "Passenger", "Other");
        typeFilterCombo.setValue("All Types");
        typeFilterCombo.setPrefHeight(34);
        typeFilterCombo.setStyle(
                "-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-pref-width:130;");
        typeFilterCombo.setOnAction(e -> applyFiltersAndSort());

        sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Sort: Name (A-Z)", "Sort: Speed (High-Low)", "Sort: Status");
        sortCombo.setValue("Sort: Name (A-Z)");
        sortCombo.setPrefHeight(34);
        sortCombo.setStyle(
                "-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-pref-width:145;");
        sortCombo.setOnAction(e -> applyFiltersAndSort());

        controls.getChildren().addAll(titleBox, sp, searchField, statusFilterCombo, typeFilterCombo, sortCombo);

        // Table
        vesselTable = new TableView<>();
        vesselTable.setPrefHeight(500);
        VBox.setVgrow(vesselTable, Priority.ALWAYS);
        vesselTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        vesselTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");

        TableColumn<VesselData, String> colVessel = new TableColumn<>("Vessel");
        colVessel.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));
        colVessel.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox h = new HBox(6);
                    h.setAlignment(Pos.CENTER_LEFT);
                    Label ic = new Label("🚢");
                    ic.setStyle("-fx-font-size:13px;");
                    Label nm = new Label(item);
                    nm.setStyle("-fx-text-fill:#0f172a; -fx-font-size:12px; -fx-font-weight:bold;");
                    h.getChildren().addAll(ic, nm);
                    setGraphic(h);
                    setText(null);
                }
            }
        });

        TableColumn<VesselData, String> colImo = new TableColumn<>("IMO");
        colImo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().imo != null ? d.getValue().imo : "N/A"));
        colImo.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<VesselData, String> colMmsi = new TableColumn<>("MMSI");
        colMmsi.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().mmsi)));
        colMmsi.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<VesselData, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().shipType != null ? d.getValue().shipType : "N/A"));
        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String clean = item.replace(" / Cargo Ship", "").replace(" / Oil Vessel", "");
                    setText(clean);
                    setStyle("-fx-font-size:12px; -fx-text-fill:#334155;");
                }
            }
        });

        TableColumn<VesselData, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(deriveVesselCategory(d.getValue())));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    String color = "#0284c7";
                    String bg = "#e0f2fe";
                    if ("Arriving".equalsIgnoreCase(item)) {
                        color = "#2563eb";
                        bg = "#dbeafe";
                    } else if ("Anchored".equalsIgnoreCase(item)) {
                        color = "#d97706";
                        bg = "#fef3c7";
                    } else if ("Berthed".equalsIgnoreCase(item) || "Moored".equalsIgnoreCase(item)) {
                        color = "#16a34a";
                        bg = "#dcfce7";
                    } else if ("Departing".equalsIgnoreCase(item)) {
                        color = "#7c3aed";
                        bg = "#ede9fe";
                    }
                    badge.setStyle("-fx-text-fill:" + color + "; -fx-background-color:" + bg
                            + "; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            }
        });

        TableColumn<VesselData, String> colSpeed = new TableColumn<>("Speed");
        colSpeed.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.1f kn", d.getValue().speed)));
        colSpeed.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#0f172a; -fx-font-weight:bold;");

        TableColumn<VesselData, String> colCourse = new TableColumn<>("Course");
        colCourse.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.0f°", d.getValue().course)));
        colCourse.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<VesselData, String> colDest = new TableColumn<>("Destination");
        colDest.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().destination != null ? d.getValue().destination : "N/A"));
        colDest.setStyle("-fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<VesselData, String> colEta = new TableColumn<>("ETA");
        colEta.setCellValueFactory(d -> new SimpleStringProperty(deriveEta(d.getValue())));
        colEta.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#2563eb; -fx-font-weight:bold;");

        TableColumn<VesselData, String> colUpdate = new TableColumn<>("Last Update");
        colUpdate.setCellValueFactory(
                d -> new SimpleStringProperty(d.getValue().timestamp != null ? d.getValue().timestamp : "N/A"));
        colUpdate.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#64748b;");

        vesselTable.getColumns().addAll(colVessel, colImo, colMmsi, colType, colStatus, colSpeed, colCourse, colDest,
                colEta, colUpdate);

        filteredVesselList = new FilteredList<>(portVesselsMasterList, p -> true);
        sortedVesselList = new SortedList<>(filteredVesselList);
        sortedVesselList.comparatorProperty().bind(vesselTable.comparatorProperty());
        vesselTable.setItems(sortedVesselList);

        vesselTable.setRowFactory(tv -> {
            TableRow<VesselData> row = new TableRow<>();
            row.setPrefHeight(38);
            row.setStyle("-fx-cursor:hand;");
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    selectVessel(row.getItem());
                }
            });
            return row;
        });

        box.getChildren().addAll(controls, vesselTable);
        return box;
    }

    private void applyFiltersAndSort() {
        if (filteredVesselList == null)
            return;

        String query = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase()
                : "";
        String statusFilter = statusFilterCombo != null && statusFilterCombo.getValue() != null
                ? statusFilterCombo.getValue()
                : "All Statuses";
        String typeFilter = typeFilterCombo != null && typeFilterCombo.getValue() != null ? typeFilterCombo.getValue()
                : "All Types";
        String sortOption = sortCombo != null && sortCombo.getValue() != null ? sortCombo.getValue()
                : "Sort: Name (A-Z)";

        filteredVesselList.setPredicate(v -> {
            if (v == null)
                return false;

            boolean matchesSearch = query.isEmpty()
                    || (v.name != null && v.name.toLowerCase().contains(query))
                    || (v.imo != null && v.imo.toLowerCase().contains(query))
                    || String.valueOf(v.mmsi).contains(query)
                    || (v.shipType != null && v.shipType.toLowerCase().contains(query))
                    || (v.destination != null && v.destination.toLowerCase().contains(query));

            if (!matchesSearch)
                return false;

            if (!"All Statuses".equalsIgnoreCase(statusFilter)) {
                String cat = deriveVesselCategory(v);
                if ("Berthed".equalsIgnoreCase(statusFilter)) {
                    if (!"Berthed".equalsIgnoreCase(cat) && !"Moored".equalsIgnoreCase(cat))
                        return false;
                } else if (!cat.equalsIgnoreCase(statusFilter)) {
                    return false;
                }
            }

            if (!"All Types".equalsIgnoreCase(typeFilter)) {
                String t = v.shipType != null ? v.shipType.toLowerCase() : "";
                String target = typeFilter.toLowerCase();
                if (!t.contains(target))
                    return false;
            }

            return true;
        });

        if (vesselTable != null && vesselTable.getSortOrder().isEmpty()) {
            if ("Sort: Speed (High-Low)".equalsIgnoreCase(sortOption)) {
                portVesselsMasterList.sort((a, b) -> Double.compare(b.speed, a.speed));
            } else if ("Sort: Status".equalsIgnoreCase(sortOption)) {
                portVesselsMasterList.sort(Comparator.comparing(this::deriveVesselCategory));
            } else {
                portVesselsMasterList.sort(Comparator.comparing(v -> v.name != null ? v.name : ""));
            }
        }
    }

    // =====================================================
    // 4. SELECTED VESSEL DETAILS PANEL
    // =====================================================
    private VBox buildSelectedVesselDetailsSection() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setStyle(
                "-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        renderVesselDetails(panel, currentlySelectedVessel);
        return panel;
    }

    private void selectVessel(VesselData v) {
        this.currentlySelectedVessel = v;
        if (selectedVesselDetailsBox != null) {
            selectedVesselDetailsBox.getChildren().clear();
            renderVesselDetails(selectedVesselDetailsBox, v);
        }
    }

    private void renderVesselDetails(VBox panel, VesselData v) {
        if (v == null) {
            Label title = new Label("Selected Vessel Details");
            title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:15px; -fx-font-weight:bold;");
            Label prompt = new Label(
                    "Click any vessel in the live operations table to inspect complete AIS parameters, telemetry, and voyage particulars.");
            prompt.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px; -fx-wrap-text:true;");
            panel.getChildren().addAll(title, prompt);
            return;
        }

        HBox head = new HBox(8);
        head.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label nameLbl = new Label(v.name != null ? v.name : "Vessel #" + v.mmsi);
        nameLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16px; -fx-font-weight:bold;");
        Label flagType = new Label((v.shipType != null ? v.shipType : "Merchant Vessel") + " • Flag: India 🇮🇳");
        flagType.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(nameLbl, flagType);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        String cat = deriveVesselCategory(v);
        Label statusPill = new Label(cat);
        statusPill.setStyle(
                "-fx-text-fill:#0284c7; -fx-background-color:#e0f2fe; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;");

        head.getChildren().addAll(titleBox, sp, statusPill);

        Label g1 = new Label("VESSEL DETAILS");
        g1.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:5 0 2 0;");

        VBox g1Box = new VBox(3,
                detailLine("Vessel Name", v.name),
                detailLine("IMO", v.imo),
                detailLine("MMSI", String.valueOf(v.mmsi)),
                detailLine("Call Sign", v.callSign),
                detailLine("Type", v.shipType),
                detailLine("Flag", "India 🇮🇳"));

        Label g2 = new Label("LIVE TELEMETRY");
        g2.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:5 0 2 0;");

        VBox g2Box = new VBox(3,
                detailLine("Navigation Status", v.navigationStatus),
                detailLine("Speed", String.format("%.1f knots", v.speed)),
                detailLine("Course", String.format("%.1f°", v.course)),
                detailLine("Heading", String.format("%.1f°", v.heading)),
                detailLine("Latitude", String.format("%.4f° N", v.latitude)),
                detailLine("Longitude", String.format("%.4f° E", v.longitude)),
                detailLine("Destination", v.destination),
                detailLine("ETA", deriveEta(v)),
                detailLine("Last Update", v.timestamp));

        Label g3 = new Label("VESSEL PARTICULARS");
        g3.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:5 0 2 0;");

        long seed = Math.abs(v.mmsi);
        int len = 180 + (int) (seed % 140);
        int beam = 25 + (int) (seed % 15);
        double draught = 8.0 + (seed % 50) / 10.0;
        int tonnage = 20000 + (int) (seed % 30000);

        VBox g3Box = new VBox(3,
                detailLine("Length", len + " m"),
                detailLine("Beam", beam + " m"),
                detailLine("Draught", String.format("%.1f m", draught)),
                detailLine("Gross Tonnage", String.format("%,d GT", tonnage)));

        panel.getChildren().addAll(head, g1, g1Box, g2, g2Box, g3, g3Box);
    }

    private HBox detailLine(String label, String value) {
        HBox line = new HBox(8);
        line.setAlignment(Pos.CENTER_LEFT);
        line.setStyle("-fx-border-color:rgba(226,232,240,0.5); -fx-border-width:0 0 1 0; -fx-padding:3 0;");

        Label l = new Label(label);
        l.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label v = new Label(value != null && !value.isBlank() ? value : "N/A");
        v.setStyle("-fx-text-fill:#0f172a; -fx-font-size:12px; -fx-font-weight:bold;");

        line.getChildren().addAll(l, sp, v);
        return line;
    }

    // =====================================================
    // 5. BOTTOM SPLIT: ACTIVITY + BREAKDOWNS + ALERTS
    // =====================================================
    private HBox buildBottomSplit() {
        HBox split = new HBox(14);
        split.setAlignment(Pos.TOP_LEFT);

        // 1. Recent Ship Activity (Left 38%)
        VBox activityBox = buildActivitySection();
        HBox.setHgrow(activityBox, Priority.ALWAYS);

        // 2. Status & Types Breakdown (Center 30%)
        VBox breakdownContainer = buildBreakdownSection();
        breakdownContainer.setPrefWidth(290);
        breakdownContainer.setMinWidth(270);

        // 3. Important Alerts (Right 32%)
        VBox alertsBox = buildAlertsSection();
        alertsBox.setPrefWidth(330);
        alertsBox.setMinWidth(300);

        split.getChildren().addAll(activityBox, breakdownContainer, alertsBox);
        return split;
    }

    private VBox buildActivitySection() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(14, 16, 14, 16));
        box.setStyle(
                "-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Recent Ship Activity (" + activeViewingPort + ")");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14px; -fx-font-weight:bold;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label badge = new Label("LIVE STREAM");
        badge.setStyle(
                "-fx-text-fill:#0284c7; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-color:#e0f2fe; -fx-padding:3 7; -fx-background-radius:4;");

        top.getChildren().addAll(title, sp, badge);

        activityFeedBox = new VBox(4);
        activityFeedBox.setPadding(new Insets(4, 0, 0, 0));

        box.getChildren().addAll(top, activityFeedBox);
        return box;
    }

    private VBox buildBreakdownSection() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(14, 16, 14, 16));
        box.setStyle(
                "-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        Label title = new Label("Port Traffic Breakdown");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14px; -fx-font-weight:bold;");

        statusBreakdownBox = new VBox(3);
        typeBreakdownBox = new VBox(3);

        box.getChildren().addAll(title, statusBreakdownBox, typeBreakdownBox);
        return box;
    }

    private VBox buildAlertsSection() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(14, 16, 14, 16));
        box.setStyle(
                "-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Important Vessel Alerts");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14px; -fx-font-weight:bold;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label badge = new Label("OPERATIONAL");
        badge.setStyle(
                "-fx-text-fill:#d97706; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-color:#fef3c7; -fx-padding:3 7; -fx-background-radius:4;");

        top.getChildren().addAll(title, sp, badge);

        alertsFeedBox = new VBox(6);
        alertsFeedBox.setPadding(new Insets(4, 0, 0, 0));

        ScrollPane scroller = new ScrollPane(alertsFeedBox);
        scroller.setFitToWidth(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setPannable(true);
        scroller.setPrefHeight(155);
        scroller.setMinHeight(155);
        scroller.setMaxHeight(155);
        scroller.setStyle(
                "-fx-background:transparent; -fx-background-color:transparent; -fx-border-color:transparent; -fx-padding:0;");

        box.getChildren().addAll(top, scroller);
        return box;
    }

    // =====================================================
    // 6. PORT PERSONNEL & DUTY MANAGEMENT SECTION
    // =====================================================
    private VBox buildPersonnelManagementSection() {
        VBox section = new VBox(14);
        section.setPadding(new Insets(18));
        section.setStyle(
                "-fx-background-color:#ffffff; -fx-background-radius:12; -fx-border-color:#e2e8f0; -fx-border-radius:12; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.04), 8, 0, 0, 2);");

        // Section Title Bar
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label secTitle = new Label("PORT PERSONNEL & OPERATIONAL DUTY MANAGEMENT");
        secTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16px; -fx-font-weight:bold;");

        Label secSub = new Label("• Registered Port Personnel & Duty Assignments for " + activeViewingPort);
        secSub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(secTitle, secSub);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Button refreshBtn = new Button("⟳ Refresh Personnel Data");
        refreshBtn.setStyle(
                "-fx-background-color:#f8fafc; -fx-text-fill:#334155; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 12; -fx-cursor:hand;");
        refreshBtn.setOnAction(e -> refreshDashboardData());

        topBar.getChildren().addAll(titleBox, topSpacer, refreshBtn);

        // Two Cards Side-by-Side: Registered Ship Captains & Registered Customs
        // Officers
        HBox split = new HBox(14);
        split.setAlignment(Pos.TOP_LEFT);

        VBox captainsCard = buildCaptainsCard();
        HBox.setHgrow(captainsCard, Priority.ALWAYS);

        VBox officersCard = buildOfficersCard();
        HBox.setHgrow(officersCard, Priority.ALWAYS);

        split.getChildren().addAll(captainsCard, officersCard);
        section.getChildren().addAll(topBar, split);
        return section;
    }

    // =====================================================
    // CARD 1: REGISTERED SHIP CAPTAINS
    // =====================================================
    private VBox buildCaptainsCard() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(14));
        box.setStyle(
                "-fx-background-color:#f8fafc; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10;");

        HBox h = new HBox(10);
        h.setAlignment(Pos.CENTER_LEFT);

        Label t = new Label("⚓ REGISTERED SHIP CAPTAINS");
        t.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13px; -fx-font-weight:bold;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button assignCapBtn = new Button("⚓ Assign Captain");
        assignCapBtn.setStyle(
                "-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:5 10; -fx-cursor:hand;");
        assignCapBtn.setOnAction(e -> {
            Map<String, Object> sel = captainsTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                showAssignCaptainShipDialog(sel);
            } else {
                showSelectAndAssignCaptainDialog();
            }
        });

        h.getChildren().addAll(t, sp, assignCapBtn);

        // Summary Metric chips
        HBox kpiRow = new HBox(8);
        kpiRow.setAlignment(Pos.CENTER_LEFT);

        totalCaptainsVal = new Label("0");
        activeCaptainsVal = new Label("0");
        assignedCaptainsVal = new Label("0");
        availCaptainsVal = new Label("0");

        kpiRow.getChildren().addAll(
                miniKpiChip("Total", totalCaptainsVal, "#0284c7", "#e0f2fe"),
                miniKpiChip("Active", activeCaptainsVal, "#16a34a", "#ecfdf5"),
                miniKpiChip("Assigned", assignedCaptainsVal, "#2563eb", "#eff6ff"),
                miniKpiChip("Available", availCaptainsVal, "#d97706", "#fef3c7"));

        // Captains Table
        captainsTable = new TableView<>();
        captainsTable.setPrefHeight(230);
        captainsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        captainsTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        captainsTable.setPlaceholder(new Label("No Ship Captains registered."));

        TableColumn<Map<String, Object>, String> colName = new TableColumn<>("Captain Name");
        colName.setPrefWidth(135);
        colName.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("name") != null ? d.getValue().get("name").toString() : "Captain"));
        colName.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colId = new TableColumn<>("User ID");
        colId.setPrefWidth(85);
        colId.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("uid") != null ? d.getValue().get("uid").toString()
                        : (d.getValue().get("id") != null ? d.getValue().get("id").toString()
                                : (d.getValue().get("captainId") != null ? d.getValue().get("captainId").toString()
                                        : "—"))));
        colId.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colEmail = new TableColumn<>("Email");
        colEmail.setPrefWidth(135);
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("email") != null ? d.getValue().get("email").toString() : "—"));
        colEmail.setStyle("-fx-font-size:11px; -fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colOrg = new TableColumn<>("Company / Org");
        colOrg.setPrefWidth(120);
        colOrg.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("company") != null ? d.getValue().get("company").toString()
                        : (d.getValue().get("organization") != null ? d.getValue().get("organization").toString()
                                : "Ocean Shipping")));
        colOrg.setStyle("-fx-font-size:11px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(80);
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("status") != null ? d.getValue().get("status").toString() : "Active"));
        colStatus.setCellFactory(col -> createBadgeCell("#16a34a", "#ecfdf5"));

        TableColumn<Map<String, Object>, String> colShip = new TableColumn<>("Assigned Ship");
        colShip.setPrefWidth(130);
        colShip.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("assignedShip") != null && !d.getValue().get("assignedShip").toString().isBlank()
                        ? d.getValue().get("assignedShip").toString()
                        : "Not Assigned"));
        colShip.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#0284c7;");

        TableColumn<Map<String, Object>, String> colDuty = new TableColumn<>("Current Duty");
        colDuty.setPrefWidth(135);
        colDuty.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("duty") != null && !d.getValue().get("duty").toString().isBlank()
                        ? d.getValue().get("duty").toString()
                        : "Not Assigned"));
        colDuty.setStyle("-fx-font-size:11px; -fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colOfficer = new TableColumn<>("Assigned Officer");
        colOfficer.setPrefWidth(125);
        colOfficer.setCellValueFactory(d -> {
            Map<String, Object> map = d.getValue();
            Object off = map.get("assignedOfficer");
            if (off != null && !off.toString().isBlank())
                return new SimpleStringProperty(off.toString());
            Object ship = map.get("assignedShip");
            if (ship != null && !ship.toString().isBlank() && !ship.toString().equalsIgnoreCase("Not Assigned")) {
                for (Map<String, Object> o : officersList) {
                    Object oShip = o.get("assignedShip");
                    if (oShip != null && oShip.toString().equalsIgnoreCase(ship.toString())) {
                        Object oName = o.get("name");
                        if (oName != null && !oName.toString().isBlank()) {
                            return new SimpleStringProperty(oName.toString());
                        }
                    }
                }
            }
            return new SimpleStringProperty("—");
        });
        colOfficer.setStyle("-fx-font-size:11px; -fx-text-fill:#16a34a; -fx-font-weight:bold;");

        TableColumn<Map<String, Object>, String> colAssignStatus = new TableColumn<>("Duty Status");
        colAssignStatus.setPrefWidth(95);
        colAssignStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("assignmentStatus") != null
                        && !d.getValue().get("assignmentStatus").toString().isBlank()
                                ? d.getValue().get("assignmentStatus").toString()
                                : (d.getValue().get("duty") != null
                                        && !d.getValue().get("duty").toString().equalsIgnoreCase("Not Assigned")
                                                ? "Assigned"
                                                : "Available")));
        colAssignStatus.setCellFactory(col -> createAssignmentStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colUpdated = new TableColumn<>("Last Updated");
        colUpdated.setPrefWidth(100);
        colUpdated.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("updatedAt") != null ? d.getValue().get("updatedAt").toString() : "—"));
        colUpdated.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(225);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button assignShipBtn = new Button("Assign Ship");
            private final Button changeShipBtn = new Button("Change Ship");
            private final Button assignDutyBtn = new Button("Assign Duty");
            private final Button deleteBtn = new Button("Delete");
            private final HBox actionBox = new HBox(5);
            {
                assignShipBtn.setStyle(
                        "-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:4 8; -fx-cursor:hand;");
                changeShipBtn.setStyle(
                        "-fx-background-color:#f1f5f9; -fx-text-fill:#0284c7; -fx-border-color:#cbd5e1; -fx-border-radius:4; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 7; -fx-cursor:hand;");
                assignDutyBtn.setStyle(
                        "-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:4 8; -fx-cursor:hand;");
                deleteBtn.setStyle(
                        "-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:4 8; -fx-cursor:hand;");

                assignShipBtn.setOnAction(e -> {
                    Map<String, Object> cap = getTableView().getItems().get(getIndex());
                    showAssignCaptainShipDialog(cap);
                });
                changeShipBtn.setOnAction(e -> {
                    Map<String, Object> cap = getTableView().getItems().get(getIndex());
                    showAssignCaptainShipDialog(cap);
                });
                assignDutyBtn.setOnAction(e -> {
                    Map<String, Object> cap = getTableView().getItems().get(getIndex());
                    showAssignCaptainDutyDialog(cap);
                });
                deleteBtn.setOnAction(e -> {
                    Map<String, Object> cap = getTableView().getItems().get(getIndex());
                    if (cap == null)
                        return;
                    String name = cap.get("name") != null ? cap.get("name").toString() : "this captain";
                    javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Captain");
                    confirm.setHeaderText("Delete Ship Captain: " + name + "?");
                    confirm.setContentText(
                            "This will permanently remove the captain's account and all associated records. This action cannot be undone.");
                    confirm.showAndWait().ifPresent(btn -> {
                        if (btn == javafx.scene.control.ButtonType.OK) {
                            String uid = cap.get("uid") != null ? cap.get("uid").toString() : "";
                            String email = cap.get("email") != null ? cap.get("email").toString() : "";
                            captainsList.remove(cap);
                            UserDao.deletePersonnel(uid, email).whenComplete((v, err) -> {
                                Platform.runLater(() -> loadPersonnelData());
                            });
                        }
                    });
                });
                actionBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Map<String, Object> cap = getTableView().getItems().get(getIndex());
                    Object sh = cap != null ? cap.get("assignedShip") : null;
                    boolean hasShip = sh != null && !sh.toString().isBlank()
                            && !sh.toString().equalsIgnoreCase("Not Assigned");

                    actionBox.getChildren().clear();
                    if (hasShip) {
                        actionBox.getChildren().addAll(assignDutyBtn, changeShipBtn, deleteBtn);
                    } else {
                        actionBox.getChildren().addAll(assignShipBtn, deleteBtn);
                    }
                    setGraphic(actionBox);
                }
                setAlignment(Pos.CENTER);
            }
        });

        captainsTable.getColumns().addAll(colName, colId, colEmail, colOrg, colStatus, colShip, colDuty, colOfficer,
                colAssignStatus, colUpdated, colAction);
        captainsTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setPrefHeight(38);
            return row;
        });
        captainsTable.setItems(captainsList);

        box.getChildren().addAll(h, kpiRow, captainsTable);
        return box;
    }

    private VBox buildOfficersCard() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(14));
        box.setStyle(
                "-fx-background-color:#f8fafc; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10;");

        HBox h = new HBox(10);
        h.setAlignment(Pos.CENTER_LEFT);

        Label t = new Label("🛡 REGISTERED CUSTOMS OFFICERS");
        t.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13px; -fx-font-weight:bold;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button assignOffBtn = new Button("🛡 Assign Duty");
        assignOffBtn.setStyle(
                "-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:5 10; -fx-cursor:hand;");
        assignOffBtn.setOnAction(e -> {
            Map<String, Object> sel = officersTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                showAssignOfficerDutyDialog(sel);
            } else {
                showSelectAndAssignOfficerDialog();
            }
        });

        h.getChildren().addAll(t, sp, assignOffBtn);

        HBox kpiRow = new HBox(8);
        kpiRow.setAlignment(Pos.CENTER_LEFT);

        totalOfficersVal = new Label("0");
        activeOfficersVal = new Label("0");
        assignedOfficersVal = new Label("0");
        availOfficersVal = new Label("0");

        kpiRow.getChildren().addAll(
                miniKpiChip("Total", totalOfficersVal, "#16a34a", "#ecfdf5"),
                miniKpiChip("Active", activeOfficersVal, "#16a34a", "#ecfdf5"),
                miniKpiChip("Assigned", assignedOfficersVal, "#2563eb", "#eff6ff"),
                miniKpiChip("Available", availOfficersVal, "#d97706", "#fef3c7"));

        officersTable = new TableView<>();
        officersTable.setPrefHeight(230);
        officersTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        officersTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        officersTable.setPlaceholder(new Label("No Customs Officers registered."));

        TableColumn<Map<String, Object>, String> colName = new TableColumn<>("Officer Name");
        colName.setPrefWidth(135);
        colName.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("name") != null ? d.getValue().get("name").toString() : "Officer"));
        colName.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colId = new TableColumn<>("User ID");
        colId.setPrefWidth(85);
        colId.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("uid") != null ? d.getValue().get("uid").toString()
                        : (d.getValue().get("id") != null ? d.getValue().get("id").toString()
                                : (d.getValue().get("officerId") != null ? d.getValue().get("officerId").toString()
                                        : "—"))));
        colId.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colEmail = new TableColumn<>("Email");
        colEmail.setPrefWidth(135);
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("email") != null ? d.getValue().get("email").toString() : "—"));
        colEmail.setStyle("-fx-font-size:11px; -fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colOrg = new TableColumn<>("Authority / Dept");
        colOrg.setPrefWidth(120);
        colOrg.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("company") != null ? d.getValue().get("company").toString()
                        : (d.getValue().get("organization") != null ? d.getValue().get("organization").toString()
                                : "Customs Border Control")));
        colOrg.setStyle("-fx-font-size:11px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(80);
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("status") != null ? d.getValue().get("status").toString() : "Active"));
        colStatus.setCellFactory(col -> createBadgeCell("#16a34a", "#ecfdf5"));

        TableColumn<Map<String, Object>, String> colPort = new TableColumn<>("Port / Jurisdiction");
        colPort.setPrefWidth(120);
        colPort.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("assignedPort") != null && !d.getValue().get("assignedPort").toString().isBlank()
                        ? d.getValue().get("assignedPort").toString()
                        : activeViewingPort));
        colPort.setStyle("-fx-font-size:11px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colShipOff = new TableColumn<>("Assigned Vessel");
        colShipOff.setPrefWidth(130);
        colShipOff.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("assignedShip") != null && !d.getValue().get("assignedShip").toString().isBlank()
                        ? d.getValue().get("assignedShip").toString()
                        : "General Port Area"));
        colShipOff.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#0284c7;");

        TableColumn<Map<String, Object>, String> colDuty = new TableColumn<>("Current Duty");
        colDuty.setPrefWidth(140);
        colDuty.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("duty") != null && !d.getValue().get("duty").toString().isBlank()
                        ? d.getValue().get("duty").toString()
                        : "Not Assigned"));
        colDuty.setStyle("-fx-font-size:11px; -fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colAssignStatus = new TableColumn<>("Duty Status");
        colAssignStatus.setPrefWidth(95);
        colAssignStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("assignmentStatus") != null
                        && !d.getValue().get("assignmentStatus").toString().isBlank()
                                ? d.getValue().get("assignmentStatus").toString()
                                : (d.getValue().get("duty") != null
                                        && !d.getValue().get("duty").toString().equalsIgnoreCase("Not Assigned")
                                                ? "Assigned"
                                                : "Available")));
        colAssignStatus.setCellFactory(col -> createAssignmentStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colUpdated = new TableColumn<>("Last Updated");
        colUpdated.setPrefWidth(100);
        colUpdated.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("updatedAt") != null ? d.getValue().get("updatedAt").toString() : "—"));
        colUpdated.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(160);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Assign Duty");
            private final Button deleteBtn = new Button("Delete");
            private final HBox actionBox = new HBox(5);
            {
                btn.setStyle(
                        "-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:4 10; -fx-cursor:hand;");
                deleteBtn.setStyle(
                        "-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:4 8; -fx-cursor:hand;");
                btn.setOnAction(e -> {
                    Map<String, Object> off = getTableView().getItems().get(getIndex());
                    showAssignOfficerDutyDialog(off);
                });
                deleteBtn.setOnAction(e -> {
                    Map<String, Object> off = getTableView().getItems().get(getIndex());
                    if (off == null)
                        return;
                    String name = off.get("name") != null ? off.get("name").toString() : "this officer";
                    javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Customs Officer");
                    confirm.setHeaderText("Delete Customs Officer: " + name + "?");
                    confirm.setContentText(
                            "This will permanently remove the officer's account and all associated records. This action cannot be undone.");
                    confirm.showAndWait().ifPresent(btnType -> {
                        if (btnType == javafx.scene.control.ButtonType.OK) {
                            String uid = off.get("uid") != null ? off.get("uid").toString() : "";
                            String email = off.get("email") != null ? off.get("email").toString() : "";
                            officersList.remove(off);
                            UserDao.deletePersonnel(uid, email).whenComplete((v, err) -> {
                                Platform.runLater(() -> loadPersonnelData());
                            });
                        }
                    });
                });
                actionBox.setAlignment(Pos.CENTER);
                actionBox.getChildren().addAll(btn, deleteBtn);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    actionBox.getChildren().clear();
                    actionBox.getChildren().addAll(btn, deleteBtn);
                    setGraphic(actionBox);
                }
                setAlignment(Pos.CENTER);
            }
        });

        officersTable.getColumns().addAll(colName, colId, colEmail, colOrg, colStatus, colPort, colShipOff, colDuty,
                colAssignStatus, colUpdated, colAction);
        officersTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setPrefHeight(38);
            return row;
        });
        officersTable.setItems(officersList);

        box.getChildren().addAll(h, kpiRow, officersTable);
        return box;
    }

    private TableCell<Map<String, Object>, String> createBadgeCell(String textHex, String bgHex) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle("-fx-text-fill:" + textHex + "; -fx-background-color:" + bgHex
                            + "; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 7; -fx-background-radius:4;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            }
        };
    }

    private TableCell<Map<String, Object>, String> createAssignmentStatusBadgeCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    String color = "#2563eb", bg = "#eff6ff";
                    if ("Available".equalsIgnoreCase(item) || "Unassigned".equalsIgnoreCase(item)) {
                        color = "#d97706";
                        bg = "#fef3c7";
                    } else if ("In Progress".equalsIgnoreCase(item) || "Started".equalsIgnoreCase(item)) {
                        color = "#0284c7";
                        bg = "#e0f2fe";
                    } else if ("Completed".equalsIgnoreCase(item)) {
                        color = "#16a34a";
                        bg = "#ecfdf5";
                    } else if ("Pending".equalsIgnoreCase(item)) {
                        color = "#8b5cf6";
                        bg = "#f5f3ff";
                    }
                    badge.setStyle("-fx-text-fill:" + color + "; -fx-background-color:" + bg
                            + "; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 7; -fx-background-radius:4;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            }
        };
    }

    private HBox miniKpiChip(String label, Label valLabel, String color, String bg) {
        HBox chip = new HBox(6);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(4, 8, 4, 8));
        chip.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:5;");

        Label l = new Label(label + ":");
        l.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px;");

        valLabel.setStyle("-fx-text-fill:" + color + "; -fx-font-size:13px; -fx-font-weight:bold;");

        chip.getChildren().addAll(l, valLabel);
        return chip;
    }

    // =====================================================
    // 7. ASSIGNMENT MODALS (REAL LIVE VESSEL DATA ONLY)
    // =====================================================
    private static class AssignableVessel {
        String id;
        String name;
        String type;
        String imo;
        String status;
        String berth;
        String destination;
        String eta;
        VesselData liveData;
        Ship dbShip;
    }

    private void showAssignCaptainShipDialog(Map<String, Object> captain) {
        if (captain == null)
            return;

        Dialog<Boolean> dialog = new Dialog<>();
        String name = captain.get("name") != null ? captain.get("name").toString() : "Ship Captain";
        dialog.setTitle("Assign Ship");
        dialog.setHeaderText("Assign Vessel for Captain: " + name);

        ButtonType assignBtnType = new ButtonType("Assign Ship", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        Label pNameVal = new Label(name);
        pNameVal.setStyle("-fx-font-weight:bold; -fx-text-fill:#0f172a; -fx-font-size:14px;");

        // Load Real Live Ships from Live Vessel Operations / AIS / Database
        List<AssignableVessel> vesselOptions = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        // 1. Live vessels from current Port Authority master list (Live Vessel
        // Operations table)
        for (VesselData v : portVesselsMasterList) {
            if (v == null || v.name == null || v.name.isBlank())
                continue;
            String vId = String.valueOf(v.mmsi);
            if (seenKeys.add(v.name.toLowerCase().trim())) {
                AssignableVessel av = new AssignableVessel();
                av.id = vId;
                av.name = v.name;
                av.type = (v.shipType != null && !v.shipType.isBlank()) ? v.shipType : "Merchant Vessel";
                av.imo = (v.imo != null && !v.imo.isBlank()) ? v.imo : String.valueOf(v.mmsi);
                av.status = deriveVesselCategory(v);
                av.berth = "Berth " + (Math.abs(v.mmsi % 12) + 1);
                av.destination = (v.destination != null && !v.destination.isBlank()) ? v.destination
                        : activeViewingPort;
                av.eta = deriveEta(v);
                av.liveData = v;
                vesselOptions.add(av);
            }
        }

        // 2. Live vessels from VesselManager matching this Port's jurisdiction
        for (VesselData v : vesselManager.getAllVessels()) {
            if (v == null || v.name == null || v.name.isBlank())
                continue;
            if (isVesselAssociatedWithPort(v, activeViewingPort)) {
                String vId = String.valueOf(v.mmsi);
                if (seenKeys.add(v.name.toLowerCase().trim())) {
                    AssignableVessel av = new AssignableVessel();
                    av.id = vId;
                    av.name = v.name;
                    av.type = (v.shipType != null && !v.shipType.isBlank()) ? v.shipType : "Merchant Vessel";
                    av.imo = (v.imo != null && !v.imo.isBlank()) ? v.imo : String.valueOf(v.mmsi);
                    av.status = deriveVesselCategory(v);
                    av.berth = "Berth " + (Math.abs(v.mmsi % 12) + 1);
                    av.destination = (v.destination != null && !v.destination.isBlank()) ? v.destination
                            : activeViewingPort;
                    av.eta = deriveEta(v);
                    av.liveData = v;
                    vesselOptions.add(av);
                }
            }
        }

        // 3. Fallback to general live vessels if port association yielded 0
        if (vesselOptions.isEmpty()) {
            for (VesselData v : vesselManager.getAllVessels()) {
                if (v == null || v.name == null || v.name.isBlank())
                    continue;
                String vId = String.valueOf(v.mmsi);
                if (seenKeys.add(v.name.toLowerCase().trim())) {
                    AssignableVessel av = new AssignableVessel();
                    av.id = vId;
                    av.name = v.name;
                    av.type = (v.shipType != null && !v.shipType.isBlank()) ? v.shipType : "Merchant Vessel";
                    av.imo = (v.imo != null && !v.imo.isBlank()) ? v.imo : String.valueOf(v.mmsi);
                    av.status = deriveVesselCategory(v);
                    av.berth = "Berth " + (Math.abs(v.mmsi % 12) + 1);
                    av.destination = (v.destination != null && !v.destination.isBlank()) ? v.destination
                            : activeViewingPort;
                    av.eta = deriveEta(v);
                    av.liveData = v;
                    vesselOptions.add(av);
                }
            }
        }

        // 4. Firestore Ships collection
        try {
            List<Ship> sList = new ShipDAO().getShips();
            if (sList != null) {
                for (Ship s : sList) {
                    if (s == null || s.getShipName() == null || s.getShipName().isBlank())
                        continue;
                    String sId = s.getShipId() != null ? s.getShipId() : s.getShipName();
                    if (seenKeys.add(s.getShipName().toLowerCase().trim())) {
                        AssignableVessel av = new AssignableVessel();
                        av.id = sId;
                        av.name = s.getShipName();
                        av.type = s.getShipType() != null ? s.getShipType() : "Merchant Vessel";
                        av.imo = s.getImoNumber() != null ? s.getImoNumber() : "—";
                        av.status = s.getStatus() != null ? s.getStatus() : "Berthed";
                        av.berth = s.getBerth() != null ? s.getBerth() : "TBD";
                        av.destination = s.getDeparture() != null ? s.getDeparture() : activeViewingPort;
                        av.eta = s.getArrival() != null ? s.getArrival() : "—";
                        av.dbShip = s;
                        vesselOptions.add(av);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // 5. Existing Ship accounts created through the application account/login
        // system
        try {
            for (Map<String, Object> u : UserDao.getFirebaseAuthUsersWithProfiles()) {
                if (u == null)
                    continue;
                String r = u.get("role") != null ? u.get("role").toString().toLowerCase() : "";
                if (r.contains("ship") && !r.contains("captain")) {
                    String sName = u.get("name") != null ? u.get("name").toString()
                            : (u.get("company") != null ? u.get("company").toString() : "Ship Vessel");
                    if (sName.isBlank())
                        continue;
                    String sId = u.get("uid") != null ? u.get("uid").toString() : sName;
                    if (seenKeys.add(sName.toLowerCase().trim())) {
                        AssignableVessel av = new AssignableVessel();
                        av.id = sId;
                        av.name = sName;
                        av.type = u.get("shipType") != null ? u.get("shipType").toString() : "Merchant Vessel";
                        av.imo = u.get("imo") != null ? u.get("imo").toString()
                                : (u.get("imoNumber") != null ? u.get("imoNumber").toString() : "—");
                        av.status = u.get("status") != null ? u.get("status").toString() : "Berthed";
                        av.berth = u.get("berth") != null ? u.get("berth").toString() : "Berth Assigned";
                        av.destination = u.get("destination") != null ? u.get("destination").toString()
                                : activeViewingPort;
                        av.eta = u.get("eta") != null ? u.get("eta").toString() : "Scheduled";
                        vesselOptions.add(av);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // Map of already assigned vessels
        Map<String, String> shipToCaptain = new HashMap<>();
        for (Map<String, Object> c : captainsList) {
            if (c == null)
                continue;
            Object sName = c.get("assignedShip");
            Object cName = c.get("name");
            if (sName != null && !sName.toString().isBlank() && !sName.toString().equalsIgnoreCase("Not Assigned")) {
                shipToCaptain.put(sName.toString().toLowerCase().trim(),
                        cName != null ? cName.toString() : "Another Captain");
            }
        }

        ComboBox<String> shipCombo = new ComboBox<>();
        shipCombo.setPrefWidth(420);

        if (vesselOptions.isEmpty()) {
            shipCombo.getItems().add("No ships available for assignment.");
            shipCombo.getSelectionModel().select(0);
        } else {
            shipCombo.getItems().add("— Select Existing Ship ▼ —");

            int selectedShipIdx = 0;
            String curShipName = captain.get("assignedShip") != null ? captain.get("assignedShip").toString() : "";
            String curShipId = captain.get("assignedShipId") != null ? captain.get("assignedShipId").toString() : "";
            String curShipImo = captain.get("shipImo") != null ? captain.get("shipImo").toString() : "";

            for (int i = 0; i < vesselOptions.size(); i++) {
                AssignableVessel av = vesselOptions.get(i);
                String label = av.name + " (" + av.type + " | IMO: " + av.imo + " | " + av.status + ")";
                String assignedTo = shipToCaptain.get(av.name.toLowerCase().trim());
                if (assignedTo != null && !assignedTo.equalsIgnoreCase(name)) {
                    label += " — [Assigned to: " + assignedTo + "]";
                }
                shipCombo.getItems().add(label);

                if (!curShipId.isBlank() && av.id.equalsIgnoreCase(curShipId)) {
                    selectedShipIdx = i + 1;
                } else if (selectedShipIdx == 0 && !curShipName.isBlank() && av.name.equalsIgnoreCase(curShipName)) {
                    selectedShipIdx = i + 1;
                } else if (selectedShipIdx == 0 && !curShipImo.isBlank() && av.imo.equalsIgnoreCase(curShipImo)) {
                    selectedShipIdx = i + 1;
                }
            }
            shipCombo.getSelectionModel().select(selectedShipIdx);
        }

        grid.add(new Label("Captain:"), 0, 0);
        grid.add(pNameVal, 1, 0);
        grid.add(new Label("Ship:"), 0, 1);
        grid.add(shipCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == assignBtnType) {
                String uid = captain.get("uid") != null ? captain.get("uid").toString()
                        : (captain.get("email") != null ? captain.get("email").toString() : null);
                int selIdx = shipCombo.getSelectionModel().getSelectedIndex();
                String sId = "";
                String sName = "Not Assigned";
                String sImo = "";

                if (selIdx > 0 && selIdx - 1 < vesselOptions.size()) {
                    AssignableVessel chosen = vesselOptions.get(selIdx - 1);
                    sId = chosen.id;
                    sName = chosen.name;
                    sImo = chosen.imo;

                    // Ensure Firestore Ships collection has this real vessel record
                    try {
                        Ship s = new Ship(
                                chosen.id,
                                chosen.name,
                                chosen.type,
                                chosen.imo,
                                chosen.liveData != null && chosen.liveData.callSign != null ? chosen.liveData.callSign
                                        : "Indian Maritime Agency",
                                chosen.eta,
                                chosen.destination,
                                chosen.berth,
                                chosen.status);
                        new ShipDAO().saveShip(s);
                    } catch (Exception ignored) {
                    }
                }

                if (uid != null) {
                    String oldShip = captain.get("assignedShip") != null ? captain.get("assignedShip").toString() : "";
                    String oldPort = captain.get("assignedPort") != null ? captain.get("assignedPort").toString() : "";
                    String timeStr = NotificationDao.generateTimestamp();

                    UserDao.assignCaptainShip(
                            uid,
                            sId,
                            sName,
                            sImo,
                            "Port Authority (" + activeViewingPort + ")");

                    // Create Notification for Ship Captain
                    if (!sName.equalsIgnoreCase("Not Assigned") && !sName.isBlank()) {
                        Notification notif = new Notification();
                        notif.setUserId(uid);
                        notif.setVesselId(sId);
                        notif.setVesselName(sName);
                        notif.setPort(activeViewingPort);
                        notif.setStatus("Active");
                        notif.setAssignedBy("Port Authority (" + activeViewingPort + ")");
                        notif.setRead(false);
                        notif.setTimestamp(timeStr);

                        if (oldShip.isBlank() || oldShip.equalsIgnoreCase("Not Assigned")) {
                            notif.setTitle("🔔 New Ship Assigned");
                            notif.setType("SHIP_ASSIGNED");
                            notif.setMessage("A new vessel has been assigned to you.\n\n" +
                                    "Vessel: " + sName + "\n" +
                                    "Port: " + activeViewingPort + "\n" +
                                    "Status: Active\n" +
                                    "Assigned by: Port Authority\n" +
                                    "Time: " + timeStr);
                        } else if (!oldShip.equalsIgnoreCase(sName)) {
                            notif.setTitle("🔔 Ship Assignment Updated");
                            notif.setType("SHIP_CHANGED");
                            notif.setMessage("Your assigned vessel has been changed.\n\n" +
                                    "Previous Vessel: " + oldShip + "\n" +
                                    "New Vessel: " + sName + "\n" +
                                    "Port: " + activeViewingPort + "\n" +
                                    "Time: " + timeStr);
                        } else if (!oldPort.isBlank() && !oldPort.equalsIgnoreCase(activeViewingPort)) {
                            notif.setTitle("🔔 Port Assignment Updated");
                            notif.setType("PORT_CHANGED");
                            notif.setMessage("Your vessel assignment has been updated.\n\n" +
                                    "Vessel: " + sName + "\n" +
                                    "Previous Port: " + oldPort + "\n" +
                                    "New Port: " + activeViewingPort + "\n" +
                                    "Assigned By: Port Authority\n" +
                                    "Time: " + timeStr);
                        } else {
                            notif.setTitle("🔔 Ship Assignment Updated");
                            notif.setType("SHIP_CHANGED");
                            notif.setMessage("Your vessel assignment has been updated.\n\n" +
                                    "Vessel: " + sName + "\n" +
                                    "Port: " + activeViewingPort + "\n" +
                                    "Time: " + timeStr);
                        }
                        new NotificationDao().createNotification(notif);
                    }

                    loadPersonnelData();
                    return true;
                }
            }
            return false;
        });

        dialog.showAndWait();
    }

    private void showAssignCaptainDutyDialog(Map<String, Object> captain) {
        if (captain == null)
            return;

        Dialog<Boolean> dialog = new Dialog<>();
        String name = captain.get("name") != null ? captain.get("name").toString() : "Ship Captain";
        String assignedShip = captain.get("assignedShip") != null ? captain.get("assignedShip").toString()
                : "Not Assigned";
        dialog.setTitle("Assign Duty");
        dialog.setHeaderText("Assign Operational Duty for Captain: " + name);

        ButtonType assignBtnType = new ButtonType("Assign Duty", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        Label pNameVal = new Label(name);
        pNameVal.setStyle("-fx-font-weight:bold; -fx-text-fill:#0f172a; -fx-font-size:13px;");

        Label pShipVal = new Label(assignedShip);
        pShipVal.setStyle("-fx-font-weight:bold; -fx-text-fill:#0284c7; -fx-font-size:13px;");

        ComboBox<String> dutyCombo = new ComboBox<>();
        dutyCombo.setPrefWidth(350);
        dutyCombo.getItems().addAll(
                "Navigation & Command",
                "Loading",
                "Unloading",
                "Berthing",
                "Departure Preparation",
                "Vessel Operation",
                "Cargo Verification & Lashing",
                "Safety Watch",
                "Vessel Master");
        String curDuty = captain.get("duty") != null ? captain.get("duty").toString() : "";
        if (!curDuty.isBlank() && dutyCombo.getItems().contains(curDuty)) {
            dutyCombo.setValue(curDuty);
        } else {
            dutyCombo.setValue("Navigation & Command");
        }

        TextField targetField = new TextField("Vessel: " + assignedShip + " (" + activeViewingPort + ")");
        targetField.setEditable(false);
        targetField.setStyle("-fx-background-color:#f1f5f9;");

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("HIGH", "MEDIUM", "LOW");
        priorityCombo.setValue(captain.get("dutyPriority") != null ? captain.get("dutyPriority").toString() : "HIGH");

        TextField dueField = new TextField();
        dueField.setPromptText("e.g. Today 18:00 or 24 Hours");
        if (captain.get("dutyDueDate") != null) {
            dueField.setText(captain.get("dutyDueDate").toString());
        }

        grid.add(new Label("Captain:"), 0, 0);
        grid.add(pNameVal, 1, 0);
        grid.add(new Label("Assigned Ship:"), 0, 1);
        grid.add(pShipVal, 1, 1);
        grid.add(new Label("Operational Duty:"), 0, 2);
        grid.add(dutyCombo, 1, 2);
        grid.add(new Label("Target / Area:"), 0, 3);
        grid.add(targetField, 1, 3);
        grid.add(new Label("Priority:"), 0, 4);
        grid.add(priorityCombo, 1, 4);
        grid.add(new Label("Due Date / Time:"), 0, 5);
        grid.add(dueField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == assignBtnType) {
                String uid = captain.get("uid") != null ? captain.get("uid").toString()
                        : (captain.get("email") != null ? captain.get("email").toString() : null);
                String duty = dutyCombo.getValue();
                String priority = priorityCombo.getValue();
                String due = dueField.getText().trim();

                if (uid != null) {
                    String timeStr = NotificationDao.generateTimestamp();
                    UserDao.assignCaptainDuty(
                            uid,
                            duty,
                            priority,
                            due.isBlank() ? null : due,
                            "Port Authority (" + activeViewingPort + ")");

                    Notification notif = new Notification();
                    notif.setUserId(uid);
                    notif.setVesselName(assignedShip);
                    notif.setPort(activeViewingPort);
                    notif.setTitle("🔔 New Operational Duty");
                    notif.setType("DUTY_ASSIGNED");
                    notif.setAssignedBy("Port Authority (" + activeViewingPort + ")");
                    notif.setRead(false);
                    notif.setTimestamp(timeStr);
                    notif.setMessage("A new operational duty has been assigned to you.\n\n" +
                            "Vessel: " + assignedShip + "\n" +
                            "Duty: " + duty + "\n" +
                            "Port: " + activeViewingPort + "\n" +
                            "Priority: " + priority + "\n" +
                            "Due: " + (due.isBlank() ? "Standard Shift" : due) + "\n" +
                            "Time: " + timeStr);
                    new NotificationDao().createNotification(notif);

                    loadPersonnelData();
                    return true;
                }
            }
            return false;
        });

        dialog.showAndWait();
    }

    private void showAssignOfficerDutyDialog(Map<String, Object> officer) {
        if (officer == null)
            return;

        Dialog<Boolean> dialog = new Dialog<>();
        String name = officer.get("name") != null ? officer.get("name").toString() : "Customs Officer";
        dialog.setTitle("Assign Duty");
        dialog.setHeaderText("Assign Customs Duty for Officer " + name);

        ButtonType assignBtnType = new ButtonType("Save Assignment", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));

        Label pNameVal = new Label(name);
        pNameVal.setStyle("-fx-font-weight:bold; -fx-text-fill:#0f172a; -fx-font-size:13px;");

        Label pPortVal = new Label(activeViewingPort);
        pPortVal.setStyle("-fx-font-weight:bold; -fx-text-fill:#16a34a; -fx-font-size:12px;");

        ComboBox<String> vesselCombo = new ComboBox<>();
        vesselCombo.setPrefWidth(350);
        List<String> vesselOptions = new ArrayList<>();
        List<String> vesselIds = new ArrayList<>();
        List<String> capUids = new ArrayList<>();

        for (Map<String, Object> cap : captainsList) {
            Object sh = cap.get("assignedShip");
            if (sh != null && !sh.toString().isBlank() && !sh.toString().equalsIgnoreCase("Not Assigned")) {
                String capName = cap.get("name") != null ? cap.get("name").toString() : "Captain";
                String capUid = cap.get("uid") != null ? cap.get("uid").toString() : "";
                String sId = cap.get("assignedShipId") != null ? cap.get("assignedShipId").toString() : "";
                vesselOptions.add(sh.toString() + " (Captain: " + capName + ")");
                vesselIds.add(sId);
                capUids.add(capUid);
            }
        }
        vesselOptions.add("General Port Operations (" + activeViewingPort + ")");
        vesselIds.add("");
        capUids.add("");

        vesselCombo.getItems().addAll(vesselOptions);
        String curShip = officer.get("assignedShip") != null ? officer.get("assignedShip").toString() : "";
        int foundIdx = -1;
        if (!curShip.isBlank() && !curShip.equalsIgnoreCase("General Port Area")
                && !curShip.equalsIgnoreCase("Not Assigned")) {
            for (int i = 0; i < vesselOptions.size(); i++) {
                if (vesselOptions.get(i).contains(curShip)) {
                    foundIdx = i;
                    break;
                }
            }
        }
        if (foundIdx >= 0) {
            vesselCombo.setValue(vesselOptions.get(foundIdx));
        } else {
            vesselCombo.setValue(vesselOptions.get(0));
        }

        ComboBox<String> dutyCombo = new ComboBox<>();
        dutyCombo.setPrefWidth(350);
        dutyCombo.getItems().addAll(
                "Document Verification",
                "Cargo Inspection",
                "Customs Clearance",
                "Declaration Verification",
                "Import/Export Compliance",
                "Cargo Examination",
                "Customs Approval/Review");

        String curDuty = officer.get("duty") != null ? officer.get("duty").toString() : "";
        if (!curDuty.isBlank() && dutyCombo.getItems().contains(curDuty)) {
            dutyCombo.setValue(curDuty);
        } else {
            dutyCombo.setValue(dutyCombo.getItems().get(0));
        }

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("HIGH", "MEDIUM", "LOW");
        priorityCombo.setValue(officer.get("dutyPriority") != null ? officer.get("dutyPriority").toString() : "HIGH");

        TextField dueField = new TextField();
        dueField.setPromptText("e.g. Today 18:00 or 24 Hours");
        if (officer.get("dutyDueDate") != null) {
            dueField.setText(officer.get("dutyDueDate").toString());
        }

        grid.add(new Label("Officer:"), 0, 0);
        grid.add(pNameVal, 1, 0);
        grid.add(new Label("Port / Jurisdiction:"), 0, 1);
        grid.add(pPortVal, 1, 1);
        grid.add(new Label("Operation / Vessel:"), 0, 2);
        grid.add(vesselCombo, 1, 2);
        grid.add(new Label("Customs Duty:"), 0, 3);
        grid.add(dutyCombo, 1, 3);
        grid.add(new Label("Priority:"), 0, 4);
        grid.add(priorityCombo, 1, 4);
        grid.add(new Label("Due Date / Time:"), 0, 5);
        grid.add(dueField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == assignBtnType) {
                String uid = officer.get("uid") != null ? officer.get("uid").toString()
                        : (officer.get("email") != null ? officer.get("email").toString() : null);
                String duty = dutyCombo.getValue();
                String priority = priorityCombo.getValue();
                String due = dueField.getText().trim();

                int selVesselIdx = vesselCombo.getSelectionModel().getSelectedIndex();
                String chosenShipName = "General Port Operations";
                String chosenShipId = "";
                String chosenCapUid = "";
                if (selVesselIdx >= 0 && selVesselIdx < vesselOptions.size()) {
                    String rawOpt = vesselOptions.get(selVesselIdx);
                    if (rawOpt.contains(" (Captain:")) {
                        chosenShipName = rawOpt.substring(0, rawOpt.indexOf(" (Captain:"));
                    } else {
                        chosenShipName = rawOpt;
                    }
                    chosenShipId = vesselIds.get(selVesselIdx);
                    chosenCapUid = capUids.get(selVesselIdx);
                }

                if (uid != null) {
                    String timeStr = NotificationDao.generateTimestamp();
                    String dutyId = "DUTY-" + System.currentTimeMillis();
                    UserDao.assignOfficerDuty(
                            uid,
                            duty,
                            "Assigned",
                            activeViewingPort,
                            "Port Authority (" + activeViewingPort + ")",
                            priority,
                            due.isBlank() ? null : due,
                            chosenShipId,
                            chosenShipName,
                            chosenCapUid,
                            dutyId);

                    Notification notif = new Notification();
                    notif.setUserId(uid);
                    notif.setVesselName(chosenShipName);
                    notif.setVesselId(chosenShipId);
                    notif.setPort(activeViewingPort);
                    notif.setTitle("🔔 New Customs Duty");
                    notif.setType("CUSTOMS_DUTY_ASSIGNED");
                    notif.setAssignedBy("Port Authority (" + activeViewingPort + ")");
                    notif.setRead(false);
                    notif.setTimestamp(timeStr);
                    notif.setMessage("A new customs duty has been assigned to you.\n\n" +
                            "Operation / Vessel: " + chosenShipName + "\n" +
                            "Port: " + activeViewingPort + "\n" +
                            "Duty: " + duty + "\n" +
                            "Priority: " + priority + "\n" +
                            "Due: " + (due.isBlank() ? "Standard Shift" : due) + "\n" +
                            "Assigned By: Port Authority\n" +
                            "Time: " + timeStr);
                    new NotificationDao().createNotification(notif);

                    loadPersonnelData();
                    return true;
                }
            }
            return false;
        });

        dialog.showAndWait();
    }

    private void showSelectAndAssignCaptainDialog() {
        if (captainsList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Assign Captain");
            alert.setHeaderText("No Registered Captains Found");
            alert.setContentText(
                    "No Captain accounts are registered in Firebase yet. Captains must create their accounts via the standard account registration/login system.");
            alert.showAndWait();
            return;
        }

        ChoiceDialog<String> choice = new ChoiceDialog<>();
        choice.setTitle("Select Captain to Assign");
        choice.setHeaderText("Choose a Registered Captain to Assign Vessel:");
        for (Map<String, Object> cap : captainsList) {
            String name = cap.get("name") != null ? cap.get("name").toString() : "Captain";
            String email = cap.get("email") != null ? cap.get("email").toString() : "";
            choice.getItems().add(name + " (" + email + ")");
        }
        choice.setSelectedItem(choice.getItems().get(0));

        choice.showAndWait().ifPresent(selStr -> {
            for (Map<String, Object> cap : captainsList) {
                String name = cap.get("name") != null ? cap.get("name").toString() : "Captain";
                String email = cap.get("email") != null ? cap.get("email").toString() : "";
                if (selStr.equals(name + " (" + email + ")")) {
                    showAssignCaptainShipDialog(cap);
                    break;
                }
            }
        });
    }

    private void showSelectAndAssignOfficerDialog() {
        if (officersList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Assign Customs Duty");
            alert.setHeaderText("No Registered Customs Officers Found");
            alert.setContentText(
                    "No Customs Officer accounts are registered in Firebase yet. Officers must create their accounts via the standard account registration/login system.");
            alert.showAndWait();
            return;
        }

        ChoiceDialog<String> choice = new ChoiceDialog<>();
        choice.setTitle("Select Customs Officer to Assign");
        choice.setHeaderText("Choose a Registered Customs Officer to Assign Duty:");
        for (Map<String, Object> off : officersList) {
            String name = off.get("name") != null ? off.get("name").toString() : "Customs Officer";
            String email = off.get("email") != null ? off.get("email").toString() : "";
            choice.getItems().add(name + " (" + email + ")");
        }
        choice.setSelectedItem(choice.getItems().get(0));

        choice.showAndWait().ifPresent(selStr -> {
            for (Map<String, Object> off : officersList) {
                String name = off.get("name") != null ? off.get("name").toString() : "Customs Officer";
                String email = off.get("email") != null ? off.get("email").toString() : "";
                if (selStr.equals(name + " (" + email + ")")) {
                    showAssignOfficerDutyDialog(off);
                    break;
                }
            }
        });
    }

    // =====================================================
    // 8. LIVE AIS SERVICE & DATA SYNC
    // =====================================================
    private void connectLiveAISService() {
        loadPortVesselsFromSource();

        vesselFinderService.start(updatedVessels -> {
            Platform.runLater(() -> {
                lastSuccessfulUpdateEpoch = System.currentTimeMillis();
                updateFreshnessBadge();
                loadPortVesselsFromSource();
            });
        });
    }

    public void refreshDashboardData() {
        loadPortVesselsFromSource();
        loadPersonnelData();
        updateFreshnessBadge();
    }

    private void updateFreshnessBadge() {
        if (dataFreshnessBadge == null)
            return;
        long diffSec = (System.currentTimeMillis() - lastSuccessfulUpdateEpoch) / 1000;

        if (vesselFinderService.isRateLimited()) {
            dataFreshnessBadge.setText("⚠ AIS RATE LIMITED (CACHED)");
            dataFreshnessBadge.setStyle(
                    "-fx-text-fill:#d97706; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(217,119,6,0.12); -fx-padding:6 12; -fx-background-radius:999;");
            if (aisStatusVal != null)
                aisStatusVal.setText("Rate Limited");
        } else if (!vesselFinderService.isConnected() && portVesselsMasterList.isEmpty()) {
            dataFreshnessBadge.setText("⚠ LIVE AIS UNAVAILABLE");
            dataFreshnessBadge.setStyle(
                    "-fx-text-fill:#ef4444; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(239,68,68,0.12); -fx-padding:6 12; -fx-background-radius:999;");
            if (aisStatusVal != null)
                aisStatusVal.setText("Offline");
        } else if (diffSec > 180) {
            dataFreshnessBadge.setText("⚠ DATA STALE (" + (diffSec / 60) + "m ago)");
            dataFreshnessBadge.setStyle(
                    "-fx-text-fill:#f59e0b; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(245,158,11,0.12); -fx-padding:6 12; -fx-background-radius:999;");
            if (aisStatusVal != null)
                aisStatusVal.setText("Stale");
        } else {
            dataFreshnessBadge.setText("● LIVE — Updated " + Math.max(1, diffSec) + "s ago");
            dataFreshnessBadge.setStyle(
                    "-fx-text-fill:#10b981; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(16,185,129,0.12); -fx-padding:6 12; -fx-background-radius:999;");
            if (aisStatusVal != null)
                aisStatusVal.setText("Connected");
        }

        if (lastSyncLabel != null) {
            lastSyncLabel.setText("Synced: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
    }

    private void loadPortVesselsFromSource() {
        Collection<VesselData> allVessels = vesselManager.getAllVessels();
        List<VesselData> matchedPortVessels = new ArrayList<>();

        for (VesselData v : allVessels) {
            if (isVesselAssociatedWithPort(v, activeViewingPort)) {
                matchedPortVessels.add(v);
            }
        }

        // Also incorporate any persistent ships registered under this port from
        // Firestore
        try {
            List<com.projectui.model.Ship> registeredShips = new com.projectui.dao.ShipDAO().getShips();
            if (registeredShips != null) {
                for (com.projectui.model.Ship s : registeredShips) {
                    if (s == null || s.getShipName() == null || s.getShipName().isBlank())
                        continue;
                    boolean belongs = isRegisteredShipForPort(s, activeViewingPort);
                    if (belongs) {
                        long mmsi = Math.abs((s.getShipId() != null ? s.getShipId() : s.getShipName()).hashCode());
                        boolean alreadyPresent = false;
                        for (VesselData v : matchedPortVessels) {
                            if (v.mmsi == mmsi || (s.getImoNumber() != null && s.getImoNumber().equalsIgnoreCase(v.imo))
                                    || (v.name != null && v.name.equalsIgnoreCase(s.getShipName()))) {
                                alreadyPresent = true;
                                break;
                            }
                        }
                        if (!alreadyPresent) {
                            double[] coords = getPortCoordinates(activeViewingPort);
                            VesselData regVessel = new VesselData(
                                    mmsi,
                                    s.getImoNumber() != null ? s.getImoNumber() : "—",
                                    s.getShipName(),
                                    "V-" + (mmsi % 9000 + 1000),
                                    coords[0],
                                    coords[1],
                                    "Arriving".equalsIgnoreCase(s.getStatus()) ? 6.5
                                            : ("Departing".equalsIgnoreCase(s.getStatus()) ? 11.2 : 0.0),
                                    90.0,
                                    90.0,
                                    activeViewingPort,
                                    s.getStatus() != null ? s.getStatus() : "Berthed",
                                    s.getShipType() != null ? s.getShipType() : "Container / Cargo Ship",
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                            matchedPortVessels.add(regVessel);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        for (VesselData v : matchedPortVessels) {
            Double oldSpd = lastKnownSpeeds.put(v.mmsi, v.speed);
            String oldStat = lastKnownStatuses.put(v.mmsi, v.navigationStatus);

            if (oldSpd != null && Math.abs(oldSpd - v.speed) >= 0.5) {
                String act = v.timestamp + " — " + v.name + " speed changed to " + String.format("%.1f kn", v.speed);
                if (!recentActivityItems.contains(act)) {
                    recentActivityItems.add(0, act);
                    if (recentActivityItems.size() > 8)
                        recentActivityItems.remove(recentActivityItems.size() - 1);
                }
            }
            if (oldStat != null && !oldStat.equalsIgnoreCase(v.navigationStatus)) {
                String act = v.timestamp + " — " + v.name + " navigation status updated to " + v.navigationStatus;
                if (!recentActivityItems.contains(act)) {
                    recentActivityItems.add(0, act);
                    if (recentActivityItems.size() > 8)
                        recentActivityItems.remove(recentActivityItems.size() - 1);
                }
            }
        }

        portVesselsMasterList.setAll(matchedPortVessels);
        applyFiltersAndSort();

        if (currentlySelectedVessel == null && !matchedPortVessels.isEmpty()) {
            selectVessel(matchedPortVessels.get(0));
        } else if (currentlySelectedVessel != null) {
            for (VesselData v : matchedPortVessels) {
                if (v.mmsi == currentlySelectedVessel.mmsi) {
                    selectVessel(v);
                    break;
                }
            }
        }

        updateKpiCounts(matchedPortVessels);
        updateBreakdownWidgets(matchedPortVessels);
        updateActivityStream(matchedPortVessels);
        updateAlerts(matchedPortVessels);
    }

    private boolean isRegisteredShipForPort(com.projectui.model.Ship s, String portName) {
        if (s == null || portName == null)
            return false;
        String pName = portName.toLowerCase();
        String berth = s.getBerth() != null ? s.getBerth().toLowerCase() : "";
        String comp = s.getCompany() != null ? s.getCompany().toLowerCase() : "";
        String arr = s.getArrival() != null ? s.getArrival().toLowerCase() : "";
        String dep = s.getDeparture() != null ? s.getDeparture().toLowerCase() : "";
        String shipId = s.getShipId() != null ? s.getShipId().toLowerCase() : "";

        if (berth.contains(pName) || comp.contains(pName) || arr.contains(pName) || dep.contains(pName)
                || shipId.contains(pName)) {
            return true;
        }

        if (pName.contains("cochin") || pName.contains("kochi")) {
            return berth.contains("cochin") || berth.contains("kochi") || berth.contains("cok")
                    || comp.contains("cochin") || comp.contains("kochi") || arr.contains("cochin")
                    || dep.contains("cochin");
        } else if (pName.contains("mumbai")) {
            return berth.contains("mumbai") || berth.contains("jnpt") || berth.contains("mum")
                    || comp.contains("mumbai") || comp.contains("jnpt") || arr.contains("mumbai")
                    || dep.contains("mumbai");
        } else if (pName.contains("chennai")) {
            return berth.contains("chennai") || berth.contains("madras") || berth.contains("maa")
                    || comp.contains("chennai") || comp.contains("madras") || arr.contains("chennai")
                    || dep.contains("chennai");
        } else if (pName.contains("vizag") || pName.contains("visakhapatnam")) {
            return berth.contains("vizag") || berth.contains("visakhapatnam") || berth.contains("vtz")
                    || comp.contains("vizag") || comp.contains("visakhapatnam") || arr.contains("vizag")
                    || dep.contains("vizag");
        } else if (pName.contains("kolkata")) {
            return berth.contains("kolkata") || berth.contains("calcutta") || berth.contains("ccu")
                    || comp.contains("kolkata") || comp.contains("calcutta") || arr.contains("kolkata")
                    || dep.contains("kolkata");
        } else if (pName.contains("hazira")) {
            return berth.contains("hazira") || comp.contains("hazira") || arr.contains("hazira")
                    || dep.contains("hazira");
        }
        return berth.contains(pName) || comp.contains(pName) || arr.contains(pName) || dep.contains(pName);
    }

    private void loadPersonnelData() {
        try {
            UserDao.syncFirestoreUsers();
        } catch (Exception ignored) {
        }

        List<Map<String, Object>> caps = UserDao.getPersonnelForPort(activeViewingPort, "Captain");
        List<Map<String, Object>> offs = UserDao.getPersonnelForPort(activeViewingPort, "Officer");

        captainsList.setAll(caps);
        officersList.setAll(offs);

        // Update Captains KPIs
        int totalC = caps.size();
        int assignedC = 0;
        for (Map<String, Object> c : caps) {
            Object duty = c.get("duty");
            Object ship = c.get("assignedShip");
            Object assignStatus = c.get("assignmentStatus");
            boolean hasDuty = duty != null && !duty.toString().isBlank()
                    && !duty.toString().equalsIgnoreCase("Not Assigned")
                    && !duty.toString().equalsIgnoreCase("Standby")
                    && !duty.toString().equalsIgnoreCase("Standby / Available")
                    && !duty.toString().equalsIgnoreCase("Completed")
                    && (assignStatus == null || (!assignStatus.toString().equalsIgnoreCase("Available")
                            && !assignStatus.toString().equalsIgnoreCase("Completed")));
            boolean hasShip = ship != null && !ship.toString().isBlank()
                    && !ship.toString().equalsIgnoreCase("Not Assigned");
            if (hasDuty || hasShip)
                assignedC++;
        }
        if (totalCaptainsVal != null)
            totalCaptainsVal.setText(String.valueOf(totalC));
        if (activeCaptainsVal != null)
            activeCaptainsVal.setText(String.valueOf(totalC));
        if (assignedCaptainsVal != null)
            assignedCaptainsVal.setText(String.valueOf(assignedC));
        if (availCaptainsVal != null)
            availCaptainsVal.setText(String.valueOf(Math.max(0, totalC - assignedC)));

        // Update Officers KPIs
        int totalO = offs.size();
        int assignedO = 0;
        for (Map<String, Object> o : offs) {
            Object duty = o.get("duty");
            Object assignStatus = o.get("assignmentStatus");
            boolean hasDuty = duty != null && !duty.toString().isBlank()
                    && !duty.toString().equalsIgnoreCase("Not Assigned")
                    && !duty.toString().equalsIgnoreCase("Standby")
                    && !duty.toString().equalsIgnoreCase("Standby / Available")
                    && !duty.toString().equalsIgnoreCase("Completed")
                    && (assignStatus == null || (!assignStatus.toString().equalsIgnoreCase("Available")
                            && !assignStatus.toString().equalsIgnoreCase("Completed")));
            if (hasDuty)
                assignedO++;
        }
        if (totalOfficersVal != null)
            totalOfficersVal.setText(String.valueOf(totalO));
        if (activeOfficersVal != null)
            activeOfficersVal.setText(String.valueOf(totalO));
        if (assignedOfficersVal != null)
            assignedOfficersVal.setText(String.valueOf(assignedO));
        if (availOfficersVal != null)
            availOfficersVal.setText(String.valueOf(Math.max(0, totalO - assignedO)));
    }

    private boolean isVesselAssociatedWithPort(VesselData v, String portName) {
        if (v == null || portName == null || portName.isBlank())
            return false;
        String pName = portName.toLowerCase().trim();

        // 1. Check destination
        String dest = v.destination != null ? v.destination.toLowerCase().trim() : "";
        if (pName.contains("cochin") || pName.contains("kochi") || pName.contains("cok")) {
            if (dest.contains("cochin") || dest.contains("kochi") || dest.contains("cok"))
                return true;
        } else if (pName.contains("mumbai") || pName.contains("jnpt")) {
            if (dest.contains("mumbai") || dest.contains("jnpt") || dest.contains("nhava") || dest.contains("mum")
                    || dest.contains("bombay"))
                return true;
        } else if (pName.contains("chennai") || pName.contains("madras")) {
            if (dest.contains("chennai") || dest.contains("madras") || dest.contains("maa"))
                return true;
        } else if (pName.contains("vizag") || pName.contains("visakhapatnam")) {
            if (dest.contains("vizag") || dest.contains("visakhapatnam") || dest.contains("vtz"))
                return true;
        } else if (pName.contains("kolkata") || pName.contains("calcutta") || pName.contains("haldia")) {
            if (dest.contains("kolkata") || dest.contains("calcutta") || dest.contains("ccu")
                    || dest.contains("haldia"))
                return true;
        } else if (pName.contains("hazira")) {
            if (dest.contains("hazira") || dest.contains("haz") || dest.contains("surat"))
                return true;
        } else if (pName.contains("kandla") || pName.contains("mundra")) {
            if (dest.contains("kandla") || dest.contains("mundra") || dest.contains("kutch"))
                return true;
        } else if (pName.contains("paradip")) {
            if (dest.contains("paradip"))
                return true;
        } else if (pName.contains("tuticorin")) {
            if (dest.contains("tuticorin"))
                return true;
        } else if (pName.contains("mormugao") || pName.contains("goa")) {
            if (dest.contains("mormugao") || dest.contains("goa"))
                return true;
        } else if (pName.contains("mangalore")) {
            if (dest.contains("mangalore"))
                return true;
        }

        // 2. Check geographical proximity to this port
        double[] coords = getPortCoordinates(portName);
        double dLat = Math.abs(v.latitude - coords[0]);
        double dLng = Math.abs(v.longitude - coords[1]);

        return (dLat <= 2.8 && dLng <= 2.8);
    }

    private double[] getPortCoordinates(String portName) {
        try {
            com.projectui.view.admin.AdminDataStore.Port adminPort = com.projectui.view.admin.AdminDataStore
                    .getInstance().getPortByName(portName);
            if (adminPort != null && (adminPort.lat != 0.0 || adminPort.lng != 0.0)) {
                return new double[] { adminPort.lat, adminPort.lng };
            }
        } catch (Exception ignored) {
        }

        String p = portName != null ? portName.toLowerCase().trim() : "";
        if (p.contains("cochin") || p.contains("kochi") || p.contains("cok"))
            return new double[] { 9.9600, 76.2600 };
        if (p.contains("mumbai") || p.contains("jnpt"))
            return new double[] { 18.9439, 72.8354 };
        if (p.contains("chennai") || p.contains("madras"))
            return new double[] { 13.0827, 80.2707 };
        if (p.contains("vizag") || p.contains("visakhapatnam"))
            return new double[] { 17.6868, 83.2185 };
        if (p.contains("kolkata") || p.contains("haldia") || p.contains("calcutta"))
            return new double[] { 22.5726, 88.3639 };
        if (p.contains("hazira") || p.contains("surat"))
            return new double[] { 21.1122, 72.6411 };
        return new double[] { 18.9439, 72.8354 };
    }

    private String deriveVesselCategory(VesselData v) {
        if (v == null)
            return "Underway";
        String nav = v.navigationStatus != null ? v.navigationStatus.toLowerCase() : "";
        double spd = v.speed;

        if (nav.contains("moored") || nav.contains("docked") || nav.contains("port") || nav.contains("5")) {
            return "Berthed";
        }
        if (nav.contains("anchor") || nav.contains("1") || spd <= 0.4) {
            return "Anchored";
        }
        if (nav.contains("arriving") || (spd >= 1.5 && spd < 10.0)) {
            return "Arriving";
        }
        if (nav.contains("departing") || (spd >= 10.0 && v.course > 180)) {
            return "Departing";
        }
        return "Underway";
    }

    private String deriveEta(VesselData v) {
        if (v == null)
            return "—";
        String cat = deriveVesselCategory(v);
        if ("Berthed".equalsIgnoreCase(cat) || "Moored".equalsIgnoreCase(cat))
            return "Berthed in Port";
        if ("Anchored".equalsIgnoreCase(cat))
            return "At Anchor";
        if ("Departing".equalsIgnoreCase(cat))
            return "Outbound";
        int mins = 20 + (int) (Math.abs(v.mmsi % 95));
        return LocalDateTime.now().plusMinutes(mins).format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void updateKpiCounts(List<VesselData> vessels) {
        int total = vessels.size();
        int arriving = 0;
        int anchored = 0;
        int berthed = 0;
        int departing = 0;

        for (VesselData v : vessels) {
            String cat = deriveVesselCategory(v);
            if ("Arriving".equalsIgnoreCase(cat))
                arriving++;
            else if ("Anchored".equalsIgnoreCase(cat))
                anchored++;
            else if ("Berthed".equalsIgnoreCase(cat) || "Moored".equalsIgnoreCase(cat))
                berthed++;
            else if ("Departing".equalsIgnoreCase(cat))
                departing++;
            else {
                if (v.speed > 8.0)
                    arriving++;
                else
                    anchored++;
            }
        }

        if (totalShipsVal != null)
            totalShipsVal.setText(String.valueOf(total));
        if (arrivingVal != null)
            arrivingVal.setText(String.valueOf(arriving));
        if (anchoredVal != null)
            anchoredVal.setText(String.valueOf(anchored));
        if (berthedVal != null)
            berthedVal.setText(String.valueOf(berthed));
        if (departingVal != null)
            departingVal.setText(String.valueOf(departing));
    }

    private void updateBreakdownWidgets(List<VesselData> vessels) {
        if (statusBreakdownBox != null) {
            statusBreakdownBox.getChildren().clear();
            Label sTitle = new Label("VESSEL STATUS BREAKDOWN");
            sTitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:2 0 4 0;");
            statusBreakdownBox.getChildren().add(sTitle);

            int arr = 0, anc = 0, ber = 0, dep = 0, und = 0, oth = 0;
            for (VesselData v : vessels) {
                String c = deriveVesselCategory(v);
                if ("Arriving".equalsIgnoreCase(c))
                    arr++;
                else if ("Anchored".equalsIgnoreCase(c))
                    anc++;
                else if ("Berthed".equalsIgnoreCase(c))
                    ber++;
                else if ("Departing".equalsIgnoreCase(c))
                    dep++;
                else if ("Underway".equalsIgnoreCase(c))
                    und++;
                else
                    oth++;
            }

            statusBreakdownBox.getChildren().addAll(
                    miniBarRow("Arriving", arr, vessels.size(), "#2563eb"),
                    miniBarRow("Anchored", anc, vessels.size(), "#d97706"),
                    miniBarRow("Berthed / Moored", ber, vessels.size(), "#16a34a"),
                    miniBarRow("Departing", dep, vessels.size(), "#7c3aed"),
                    miniBarRow("Underway", und, vessels.size(), "#0284c7"));
        }

        if (typeBreakdownBox != null) {
            typeBreakdownBox.getChildren().clear();
            Label tTitle = new Label("VESSEL TYPES BREAKDOWN");
            tTitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:6 0 4 0;");
            typeBreakdownBox.getChildren().add(tTitle);

            int cont = 0, tank = 0, bulk = 0, cargo = 0, pass = 0, oth = 0;
            for (VesselData v : vessels) {
                String t = v.shipType != null ? v.shipType.toLowerCase() : "";
                if (t.contains("container"))
                    cont++;
                else if (t.contains("tanker"))
                    tank++;
                else if (t.contains("bulk"))
                    bulk++;
                else if (t.contains("cargo"))
                    cargo++;
                else if (t.contains("passenger"))
                    pass++;
                else
                    oth++;
            }

            typeBreakdownBox.getChildren().addAll(
                    miniBarRow("Container Ship", cont, vessels.size(), "#0284c7"),
                    miniBarRow("Tanker", tank, vessels.size(), "#0ea5e9"),
                    miniBarRow("Bulk / General Cargo", cargo + bulk, vessels.size(), "#3b82f6"),
                    miniBarRow("Other / Tug / Passenger", oth + pass, vessels.size(), "#64748b"));
        }
    }

    private HBox miniBarRow(String name, int count, int total, String color) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(name);
        lbl.setStyle("-fx-text-fill:#475569; -fx-font-size:12px; -fx-min-width:125;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label cnt = new Label(String.valueOf(count));
        cnt.setStyle("-fx-text-fill:#0f172a; -fx-font-size:12px; -fx-font-weight:bold;");

        row.getChildren().addAll(lbl, sp, cnt);
        return row;
    }

    private void updateActivityStream(List<VesselData> vessels) {
        if (activityFeedBox == null)
            return;
        activityFeedBox.getChildren().clear();

        if (vessels.isEmpty()) {
            Label empty = new Label("No recent vessel activity recorded for " + activeViewingPort + ".");
            empty.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px;");
            activityFeedBox.getChildren().add(empty);
            return;
        }

        if (recentActivityItems.isEmpty()) {
            DateTimeFormatter tf = DateTimeFormatter.ofPattern("hh:mm a");
            int i = 0;
            for (VesselData v : vessels) {
                if (i++ >= 5)
                    break;
                String cat = deriveVesselCategory(v);
                String desc;
                if ("Arriving".equalsIgnoreCase(cat))
                    desc = v.name + " arriving at " + activeViewingPort + " approach (Speed: "
                            + String.format("%.1f kn", v.speed) + ")";
                else if ("Anchored".equalsIgnoreCase(cat))
                    desc = v.name + " status: At Anchor";
                else if ("Berthed".equalsIgnoreCase(cat))
                    desc = v.name + " moored at container terminal";
                else if ("Departing".equalsIgnoreCase(cat))
                    desc = v.name + " departing for open waters";
                else
                    desc = v.name + " underway at " + String.format("%.1f kn", v.speed) + " (Heading "
                            + String.format("%.0f°", v.heading) + ")";

                String time = LocalDateTime.now().minusMinutes(i * 5L + 1L).format(tf);
                recentActivityItems.add(time + " — " + desc);
            }
        }

        for (String act : recentActivityItems) {
            String[] parts = act.split(" — ", 2);
            String time = parts.length > 1 ? parts[0] : "";
            String text = parts.length > 1 ? parts[1] : act;

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-border-color:rgba(226,232,240,0.5); -fx-border-width:0 0 1 0; -fx-padding:4 0;");

            Label tLbl = new Label(time);
            tLbl.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:11px; -fx-font-weight:bold; -fx-min-width:68;");

            Label ic = new Label("•");
            ic.setStyle("-fx-text-fill:#0284c7; -fx-font-size:13px; -fx-font-weight:bold;");

            Label dLbl = new Label(text);
            dLbl.setStyle("-fx-text-fill:#1e293b; -fx-font-size:12px; -fx-wrap-text:true;");

            row.getChildren().addAll(tLbl, ic, dLbl);
            activityFeedBox.getChildren().add(row);
        }
    }

    private void updateAlerts(List<VesselData> vessels) {
        if (alertsFeedBox == null)
            return;
        alertsFeedBox.getChildren().clear();

        // 1. Live Emergency SOS Alerts from Ship Captains (Scoped to Port Authority
        // port)
        List<com.projectui.service.SosEmergencyService.SosAlert> sosAlerts = com.projectui.service.SosEmergencyService
                .getAlertsForRoleAndUser("Port Authority", com.projectui.view.login.LoginPage.loggedInUser);
        for (com.projectui.service.SosEmergencyService.SosAlert a : sosAlerts) {
            boolean isAck = "ACKNOWLEDGED".equalsIgnoreCase(a.getStatus());
            HBox card = new HBox(8);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(8, 10, 8, 10));
            card.setStyle(isAck
                    ? "-fx-background-color:#f0fdf4; -fx-background-radius:6; -fx-border-color:#16a34a; -fx-border-width:0 0 0 3;"
                    : "-fx-background-color:#fef2f2; -fx-background-radius:6; -fx-border-color:#dc2626; -fx-border-width:0 0 0 3;");

            VBox box = new VBox(2);
            HBox.setHgrow(box, Priority.ALWAYS);

            Label t = new Label(isAck ? "✓ SOS ACKNOWLEDGED: " + a.getVesselName().toUpperCase()
                    : "🔴 EMERGENCY SOS: " + a.getVesselName().toUpperCase() + " [" + a.getSeverity() + "]");
            t.setStyle(
                    "-fx-text-fill:" + (isAck ? "#16a34a" : "#dc2626") + "; -fx-font-size:12px; -fx-font-weight:bold;");

            Label m = new Label("Incident: " + a.getIncidentId() + " • " + a.getCategory() + " | Captain: "
                    + a.getCaptainName() + "\n"
                    + "Location: " + a.getLocation() + " (Speed: " + a.getSpeed() + ", Heading: " + a.getHeading()
                    + ") | " + a.getTimestamp());
            m.setStyle("-fx-text-fill:#334155; -fx-font-size:11px; -fx-wrap-text:true;");

            box.getChildren().addAll(t, m);

            Button viewSosBtn = new Button(isAck ? "✓ View" : "⚡ Acknowledge");
            viewSosBtn.setStyle(isAck
                    ? "-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4; -fx-cursor:hand;"
                    : "-fx-background-color:#dc2626; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4; -fx-cursor:hand;");
            viewSosBtn.setOnAction(ev -> {
                com.projectui.service.SosEmergencyService.showSosDetailsDialog(
                        root.getScene() != null ? root.getScene().getWindow() : null,
                        a,
                        userAssignedPort + " Authority",
                        () -> updateAlerts(filteredVesselList != null ? filteredVesselList : portVesselsMasterList));
            });

            card.getChildren().addAll(box, viewSosBtn);
            alertsFeedBox.getChildren().add(card);
        }

        if (vessels.isEmpty() && sosAlerts.isEmpty()) {
            alertsFeedBox.getChildren().add(alertCard("✓ Normal Operations",
                    "No active vessel traffic warnings for " + activeViewingPort + ".", "#10b981", "#ecfdf5"));
            return;
        }

        int alerts = 0;
        for (VesselData v : vessels) {
            if (v.speed > 12.0 && alerts < 2) {
                alertsFeedBox.getChildren().add(alertCard(
                        "⚠️ Approach Speed Advisory",
                        v.name + " speed (" + String.format("%.1f kn", v.speed)
                                + ") exceeds 10 kn harbor approach limit.",
                        "#ef4444", "#fef2f2"));
                alerts++;
            }
        }

        if (alerts == 0 && sosAlerts.isEmpty()) {
            alertsFeedBox.getChildren().add(alertCard(
                    "✓ Navigational Fairway Clear",
                    "Navigational channel and harbor approaches operating smoothly.",
                    "#10b981", "#ecfdf5"));
        }

        alertsFeedBox.getChildren().add(alertCard(
                "ℹ Pilotage & AIS Telemetry",
                vessels.size() + " vessels actively tracked via Admin Map AIS telemetry.",
                "#0284c7", "#f0f9ff"));
    }

    private HBox alertCard(String title, String msg, String color, String bg) {
        HBox card = new HBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(7, 10, 7, 10));
        card.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:6; -fx-border-color:" + color
                + "; -fx-border-width:0 0 0 3;");

        VBox box = new VBox(2);
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:" + color + "; -fx-font-size:12px; -fx-font-weight:bold;");

        Label m = new Label(msg);
        m.setStyle("-fx-text-fill:#334155; -fx-font-size:11px; -fx-wrap-text:true;");

        box.getChildren().addAll(t, m);
        card.getChildren().add(box);
        return card;
    }

    public VBox getRoot() {
        return root;
    }
}