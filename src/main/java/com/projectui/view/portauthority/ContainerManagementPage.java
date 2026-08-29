package com.projectui.view.portauthority;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.projectui.dao.CargoDao;
import com.projectui.dao.ContainerDao;
import com.projectui.service.VesselData;
import com.projectui.service.VesselFinderService;
import com.projectui.service.VesselManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
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

@SuppressWarnings({"unchecked", "unused"})
public class ContainerManagementPage {

    private final VBox content = new VBox(16);

    // Shared In-Memory Transfer Store for Inter-Port Transfer Workflow
    private static final Map<String, Map<String, Object>> interPortTransferStore = new ConcurrentHashMap<>();
    private static int transferCounter = 501;

    // Backend DAOs & Services (Single source of truth with Admin, Exporter, Importer)
    private final ContainerDao containerDao = new ContainerDao();
    private final CargoDao cargoDao = new CargoDao();
    private final VesselManager vesselManager = VesselManager.getInstance();
    private final VesselFinderService vesselFinderService = VesselFinderService.getInstance();

    // Authenticated Port Authority Context
    private String userAssignedPort = "Cochin Port";
    private String activeViewingPort = "Cochin Port";

    // Header Freshness & Telemetry UI
    private Label pageHeadingLabel;
    private Label pageSubLabel;
    private Label dataFreshnessBadge;
    private Label lastSyncLabel;

    // Top Summary KPI Card Values
    private Label totalContainersVal;
    private Label availableVal;
    private Label allocatedVal;
    private Label inTransitVal;
    private Label pendingRequestsVal;

    // Master & Filtered Lists
    private final List<Map<String, Object>> masterContainersList = new ArrayList<>();
    private final ObservableList<Map<String, Object>> containerRequestsList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> containerInventoryList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> incomingTransfersList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> outgoingTransfersList = FXCollections.observableArrayList();

    // Tables & Controls
    private TableView<Map<String, Object>> requestsTable;
    private TableView<Map<String, Object>> inventoryTable;
    private TableView<Map<String, Object>> transferTable;
    private TextField searchField;
    private ComboBox<String> typeFilterCombo;
    private ComboBox<String> statusFilterCombo;
    private ComboBox<String> sortCombo;
    private Button showIncomingTransfersBtn;
    private Button showOutgoingTransfersBtn;
    private boolean viewingIncomingTransfers = true;

    // State Tracking
    private long lastSuccessfulUpdateEpoch = System.currentTimeMillis();

    public ContainerManagementPage() {
        content.setSpacing(16);
        content.setPadding(new Insets(0));
        content.setStyle("-fx-background-color:transparent;");

        resolveAssignedPort();
        buildLayout();
        connectLiveServices();
    }

    // =====================================================
    // 1. PORT CONTEXT RESOLUTION (DYNAMIC PER USER)
    // =====================================================
    private void resolveAssignedPort() {
        String port = null;
        if (com.projectui.view.login.LoginPage.loggedInUser != null) {
            Map<String, Object> user = com.projectui.view.login.LoginPage.loggedInUser;
            if (user.get("assignedPort") != null && !user.get("assignedPort").toString().isBlank()) {
                port = user.get("assignedPort").toString().trim();
            } else if (user.get("company") != null && !user.get("company").toString().isBlank()) {
                port = user.get("company").toString().trim();
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
        if (raw == null) return "Cochin Port";
        String lower = raw.toLowerCase().trim();
        if (lower.contains("cochin") || lower.contains("kochi")) return "Cochin Port";
        if (lower.contains("mumbai") || lower.contains("bombay")) return "Port of Mumbai";
        if (lower.contains("chennai") || lower.contains("madras")) return "Port of Chennai";
        if (lower.contains("vizag") || lower.contains("visakhapatnam")) return "Port of Vizag";
        if (lower.contains("kolkata") || lower.contains("calcutta")) return "Port of Kolkata";
        return raw;
    }

    // =====================================================
    // 2. LAYOUT COMPOSITION
    // =====================================================
    private void buildLayout() {
        VBox header = buildHeader();
        HBox summaryCards = buildSummaryCards();
        VBox requestsSection = buildContainerRequestsSection();
        VBox inventorySection = buildContainerInventorySection();
        VBox transfersSection = buildTransfersSection();

        content.getChildren().addAll(
                header,
                summaryCards,
                requestsSection,
                inventorySection,
                transfersSection
        );
    }

    // =====================================================
    // 3. HEADER BANNER
    // =====================================================
    private VBox buildHeader() {
        VBox container = new VBox(4);

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        pageHeadingLabel = new Label("Container Management");
        pageHeadingLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:22px; -fx-font-weight:bold;");

        pageSubLabel = new Label("Manage yard container inventory, exporter/importer bookings & inter-port transfers for " + activeViewingPort);
        pageSubLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:13px;");
        titleBox.getChildren().addAll(pageHeadingLabel, pageSubLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Freshness Badge
        dataFreshnessBadge = new Label("● LIVE — Operations Active");
        dataFreshnessBadge.setStyle("-fx-text-fill:#10b981; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(16,185,129,0.12); -fx-padding:6 12; -fx-background-radius:999;");

        lastSyncLabel = new Label("Synced: Just now");
        lastSyncLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px;");

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 14; -fx-cursor:hand;");
        refreshBtn.setOnAction(e -> refreshContainerData());

        topRow.getChildren().addAll(titleBox, spacer, dataFreshnessBadge, lastSyncLabel, refreshBtn);
        container.getChildren().add(topRow);
        return container;
    }

    // =====================================================
    // 4. TOP SUMMARY KPI CARDS
    // =====================================================
    private HBox buildSummaryCards() {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);

        totalContainersVal = new Label("0");
        availableVal = new Label("0");
        allocatedVal = new Label("0");
        inTransitVal = new Label("0");
        pendingRequestsVal = new Label("0");

        VBox cTotal = createKpiCard("Total Containers", totalContainersVal, "Inventory at " + activeViewingPort, "📦", "#0284c7", "#e0f2fe");
        VBox cAvailable = createKpiCard("Available", availableVal, "Ready for Booking", "🟢", "#16a34a", "#ecfdf5");
        VBox cAllocated = createKpiCard("Allocated", allocatedVal, "Assigned to Cargo/Vessel", "📑", "#2563eb", "#eff6ff");
        VBox cInTransit = createKpiCard("In Transit", inTransitVal, "En Route / Sea Voyage", "🚢", "#8b5cf6", "#f5f3ff");
        VBox cPending = createKpiCard("Pending Requests", pendingRequestsVal, "Awaiting Port Approval", "⏳", "#d97706", "#fef3c7");

        row.getChildren().addAll(cTotal, cAvailable, cAllocated, cInTransit, cPending);

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
        card.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(iconStr);
        icon.setStyle("-fx-text-fill:" + color + "; -fx-font-size:15px; -fx-font-weight:bold; -fx-background-color:" + bg + "; -fx-padding:4 8; -fx-background-radius:6;");

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
    // 5. CONTAINER REQUESTS SECTION (EXPORTER/IMPORTER BOOKINGS)
    // =====================================================
    private VBox buildContainerRequestsSection() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label secTitle = new Label("CONTAINER REQUESTS — Exporter & Importer Bookings");
        secTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:15px; -fx-font-weight:bold;");
        Label secSub = new Label("Pending container allocation requests submitted by shippers for " + activeViewingPort);
        secSub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(secTitle, secSub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        topBar.getChildren().addAll(titleBox, sp);

        requestsTable = new TableView<>();
        requestsTable.setPrefHeight(200);
        requestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        requestsTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        requestsTable.setPlaceholder(new Label("No pending container requests for " + activeViewingPort));

        TableColumn<Map<String, Object>, String> colReqId = new TableColumn<>("Request ID");
        colReqId.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "bookingId", "REQ-N/A")));
        colReqId.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0284c7;");

        TableColumn<Map<String, Object>, String> colContNum = new TableColumn<>("Container No.");
        colContNum.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "containerNo", getString(d.getValue(), "bookingId", "CONT-N/A"))));
        colContNum.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colType = new TableColumn<>("Container Type");
        colType.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "containerType", "20 ft Standard Dry")));
        colType.setStyle("-fx-font-size:12px; -fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colCargo = new TableColumn<>("Cargo / Cargo ID");
        colCargo.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "cargoName", "Electronics") + " (" + getString(d.getValue(), "cargoId", "CGO") + ")"));
        colCargo.setStyle("-fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colRequester = new TableColumn<>("Exporter / Importer");
        colRequester.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "exporter", "Global Logistics Ltd.")));
        colRequester.setStyle("-fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colShip = new TableColumn<>("Ship / Carrier");
        colShip.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "shipName", "MV " + activeViewingPort.replace("Port of ", "") + " Express")));
        colShip.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#2563eb; -fx-font-weight:bold;");

        TableColumn<Map<String, Object>, String> colDest = new TableColumn<>("Destination");
        colDest.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "destinationPort", "Port of Singapore")));
        colDest.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#0f172a; -fx-font-weight:bold;");

        TableColumn<Map<String, Object>, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "status", "Pending")));
        colStatus.setCellFactory(col -> createStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(180);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final HBox btnBox = new HBox(6, viewBtn, approveBtn, rejectBtn);

            {
                btnBox.setAlignment(Pos.CENTER);
                viewBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");
                approveBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");
                rejectBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");

                viewBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    showContainerDetailsDialog(item);
                });

                approveBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    handleApproveBooking(item);
                });

                rejectBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    handleRejectBooking(item);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Map<String, Object> rowData = getTableView().getItems().get(getIndex());
                    String st = getString(rowData, "status", "");
                    if ("Approved".equalsIgnoreCase(st) || "Allocated".equalsIgnoreCase(st) || "Rejected".equalsIgnoreCase(st)) {
                        approveBtn.setVisible(false);
                        approveBtn.setManaged(false);
                        rejectBtn.setVisible(false);
                        rejectBtn.setManaged(false);
                    } else {
                        approveBtn.setVisible(true);
                        approveBtn.setManaged(true);
                        rejectBtn.setVisible(true);
                        rejectBtn.setManaged(true);
                    }
                    setGraphic(btnBox);
                }
            }
        });

        requestsTable.getColumns().addAll(
                colReqId, colContNum, colType, colCargo, colRequester,
                colShip, colDest, colStatus, colAction
        );
        requestsTable.setItems(containerRequestsList);

        requestsTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setPrefHeight(38);
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showContainerDetailsDialog(row.getItem());
                }
            });
            return row;
        });

        container.getChildren().addAll(topBar, requestsTable);
        return container;
    }

    // =====================================================
    // 6. MAIN CONTAINER INVENTORY SECTION
    // =====================================================
    private VBox buildContainerInventorySection() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        // Controls Bar
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label tblTitle = new Label("Container Inventory & Yard Stock");
        tblTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:15px; -fx-font-weight:bold;");
        Label tblSub = new Label("Active container fleet, stacking location and status at " + activeViewingPort);
        tblSub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(tblTitle, tblSub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("🔍 Search Container / Cargo / Ship / Yard...");
        searchField.setPrefWidth(260);
        searchField.setPrefHeight(34);
        searchField.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-padding:5 10;");
        searchField.textProperty().addListener((obs, ov, nv) -> applyFilters());

        typeFilterCombo = new ComboBox<>();
        typeFilterCombo.getItems().addAll("All Types", "20 ft Standard Dry", "40 ft Standard Dry", "40 ft High Cube", "20 ft Refrigerated (Reefer)", "40 ft Flat Rack", "20 ft Tank Container");
        typeFilterCombo.setValue("All Types");
        typeFilterCombo.setPrefHeight(34);
        typeFilterCombo.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-pref-width:145;");
        typeFilterCombo.setOnAction(e -> applyFilters());

        statusFilterCombo = new ComboBox<>();
        statusFilterCombo.getItems().addAll("All Status", "Available", "Allocated", "In Transit", "Received", "Loaded", "Delivered", "Under Transfer");
        statusFilterCombo.setValue("All Status");
        statusFilterCombo.setPrefHeight(34);
        statusFilterCombo.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-pref-width:130;");
        statusFilterCombo.setOnAction(e -> applyFilters());

        sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Sort: ID (Newest)", "Sort: Status", "Sort: Type", "Sort: Location");
        sortCombo.setValue("Sort: ID (Newest)");
        sortCombo.setPrefHeight(34);
        sortCombo.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-pref-width:140;");
        sortCombo.setOnAction(e -> applyFilters());

        Button addContainerBtn = new Button("+ Register Container");
        addContainerBtn.setPrefHeight(34);
        addContainerBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 14; -fx-cursor:hand;");
        addContainerBtn.setOnAction(e -> showAddContainerDialog());

        Button requestTransferBtn = new Button("⇄ Request Port Transfer");
        requestTransferBtn.setPrefHeight(34);
        requestTransferBtn.setStyle("-fx-background-color:#0f172a; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 14; -fx-cursor:hand;");
        requestTransferBtn.setOnAction(e -> showInitiateTransferDialog(null));

        controls.getChildren().addAll(titleBox, sp, searchField, typeFilterCombo, statusFilterCombo, sortCombo, addContainerBtn, requestTransferBtn);

        // Table
        inventoryTable = new TableView<>();
        inventoryTable.setPrefHeight(320);
        inventoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        inventoryTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        inventoryTable.setPlaceholder(new Label("No containers available in yard inventory for " + activeViewingPort));

        TableColumn<Map<String, Object>, String> colId = new TableColumn<>("Container ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "containerNo", getString(d.getValue(), "bookingId", "CONT-N/A"))));
        colId.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0284c7;");

        TableColumn<Map<String, Object>, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "containerType", "20 ft Standard Dry")));
        colType.setStyle("-fx-font-size:12px; -fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colCargo = new TableColumn<>("Cargo");
        colCargo.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "cargoName", "Commercial Goods")));
        colCargo.setStyle("-fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colShip = new TableColumn<>("Ship / Voyage");
        colShip.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "shipName", "MV " + activeViewingPort.replace("Port of ", "") + " Star")));
        colShip.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox h = new HBox(4);
                    h.setAlignment(Pos.CENTER);
                    Label ic = new Label("🚢");
                    Label txt = new Label(item);
                    txt.setStyle("-fx-text-fill:#2563eb; -fx-font-size:12px; -fx-font-weight:bold;");
                    h.getChildren().addAll(ic, txt);
                    setGraphic(h);
                    setText(null);
                }
            }
        });

        TableColumn<Map<String, Object>, String> colLocation = new TableColumn<>("Current Location");
        colLocation.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "location", "Yard Bay B-02 • " + activeViewingPort)));
        colLocation.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colSize = new TableColumn<>("Size / Tare");
        colSize.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "size", "20ft (2,200 kg)")));
        colSize.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "status", "Available")));
        colStatus.setCellFactory(col -> createStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colUpdated = new TableColumn<>("Last Updated");
        colUpdated.setCellValueFactory(d -> new SimpleStringProperty(formatIsoDate(getString(d.getValue(), "createdAt", "Today"))));
        colUpdated.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(160);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button transferBtn = new Button("Transfer");
            private final HBox btnBox = new HBox(6, viewBtn, transferBtn);

            {
                btnBox.setAlignment(Pos.CENTER);
                viewBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");
                transferBtn.setStyle("-fx-background-color:#475569; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");

                viewBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    showContainerDetailsDialog(item);
                });

                transferBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    showInitiateTransferDialog(item);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btnBox);
            }
        });

        inventoryTable.getColumns().addAll(
                colId, colType, colCargo, colShip, colLocation,
                colSize, colStatus, colUpdated, colAction
        );
        inventoryTable.setItems(containerInventoryList);

        inventoryTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setPrefHeight(38);
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showContainerDetailsDialog(row.getItem());
                }
            });
            return row;
        });

        container.getChildren().addAll(controls, inventoryTable);
        return container;
    }

    // =====================================================
    // 7. CONTAINER TRANSFERS BETWEEN PORTS SECTION
    // =====================================================
    private VBox buildTransfersSection() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label secTitle = new Label("INTER-PORT CONTAINER TRANSFERS");
        secTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:15px; -fx-font-weight:bold;");
        Label secSub = new Label("Manage container reallocation and transfer requests across authorized ports");
        secSub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(secTitle, secSub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        // Toggle Buttons: Incoming vs Outgoing Transfers
        showIncomingTransfersBtn = new Button("📥 Incoming Transfers to " + activeViewingPort);
        showIncomingTransfersBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:5 12; -fx-cursor:hand;");

        showOutgoingTransfersBtn = new Button("📤 Outgoing Transfers from " + activeViewingPort);
        showOutgoingTransfersBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#475569; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:5 12; -fx-cursor:hand;");

        showIncomingTransfersBtn.setOnAction(e -> {
            viewingIncomingTransfers = true;
            showIncomingTransfersBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:5 12; -fx-cursor:hand;");
            showOutgoingTransfersBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#475569; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:5 12; -fx-cursor:hand;");
            transferTable.setItems(incomingTransfersList);
            transferTable.setPlaceholder(new Label("No incoming transfer requests for " + activeViewingPort));
        });

        showOutgoingTransfersBtn.setOnAction(e -> {
            viewingIncomingTransfers = false;
            showOutgoingTransfersBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:5 12; -fx-cursor:hand;");
            showIncomingTransfersBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#475569; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:5 12; -fx-cursor:hand;");
            transferTable.setItems(outgoingTransfersList);
            transferTable.setPlaceholder(new Label("No outgoing transfer requests sent from " + activeViewingPort));
        });

        topBar.getChildren().addAll(titleBox, sp, showIncomingTransfersBtn, showOutgoingTransfersBtn);

        transferTable = new TableView<>();
        transferTable.setPrefHeight(200);
        transferTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        transferTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        transferTable.setPlaceholder(new Label("No incoming transfer requests for " + activeViewingPort));

        TableColumn<Map<String, Object>, String> colTransId = new TableColumn<>("Transfer ID");
        colTransId.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "transferId", "TRF-N/A")));
        colTransId.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0284c7;");

        TableColumn<Map<String, Object>, String> colCont = new TableColumn<>("Container");
        colCont.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "containerNo", "CONT-N/A")));
        colCont.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colFrom = new TableColumn<>("Origin Port");
        colFrom.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "originPort", "Port of Mumbai")));
        colFrom.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colTo = new TableColumn<>("Destination Port");
        colTo.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "destinationPort", activeViewingPort)));
        colTo.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colShip = new TableColumn<>("Carrier / Ship");
        colShip.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "shipName", "Inter-Port Feeder")));
        colShip.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#2563eb; -fx-font-weight:bold;");

        TableColumn<Map<String, Object>, String> colReason = new TableColumn<>("Reason / Details");
        colReason.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "reason", "Quayside Rebalancing")));
        colReason.setStyle("-fx-font-size:12px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colDate = new TableColumn<>("Requested Date");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(formatIsoDate(getString(d.getValue(), "createdAt", "Today"))));
        colDate.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colStatus = new TableColumn<>("Transfer Status");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "status", "Pending Approval")));
        colStatus.setCellFactory(col -> createStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(180);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final HBox btnBox = new HBox(6, viewBtn, approveBtn, rejectBtn);

            {
                btnBox.setAlignment(Pos.CENTER);
                viewBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");
                approveBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");
                rejectBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");

                viewBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    showTransferDetailsDialog(item);
                });

                approveBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    handleApproveTransfer(item);
                });

                rejectBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    handleRejectTransfer(item);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Map<String, Object> rowData = getTableView().getItems().get(getIndex());
                    String st = getString(rowData, "status", "");
                    if (viewingIncomingTransfers && ("Pending Destination Approval".equalsIgnoreCase(st) || "Pending Approval".equalsIgnoreCase(st) || "Pending".equalsIgnoreCase(st))) {
                        approveBtn.setVisible(true);
                        approveBtn.setManaged(true);
                        rejectBtn.setVisible(true);
                        rejectBtn.setManaged(true);
                    } else {
                        approveBtn.setVisible(false);
                        approveBtn.setManaged(false);
                        rejectBtn.setVisible(false);
                        rejectBtn.setManaged(false);
                    }
                    setGraphic(btnBox);
                }
            }
        });

        transferTable.getColumns().addAll(
                colTransId, colCont, colFrom, colTo, colShip,
                colReason, colDate, colStatus, colAction
        );
        transferTable.setItems(incomingTransfersList);

        transferTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setPrefHeight(38);
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showTransferDetailsDialog(row.getItem());
                }
            });
            return row;
        });

        container.getChildren().addAll(topBar, transferTable);
        return container;
    }

    private TableCell<Map<String, Object>, String> createStatusBadgeCell() {
        return new TableCell<>() {
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

                    if ("Pending".equalsIgnoreCase(item) || "Pending Approval".equalsIgnoreCase(item) || "Pending Destination Approval".equalsIgnoreCase(item) || "Booked".equalsIgnoreCase(item)) {
                        color = "#d97706";
                        bg = "#fef3c7";
                    } else if ("Available".equalsIgnoreCase(item) || "Approved".equalsIgnoreCase(item) || "Transfer Approved".equalsIgnoreCase(item) || "Delivered".equalsIgnoreCase(item)) {
                        color = "#16a34a";
                        bg = "#dcfce7";
                    } else if ("In Transit".equalsIgnoreCase(item)) {
                        color = "#2563eb";
                        bg = "#dbeafe";
                    } else if ("Allocated".equalsIgnoreCase(item) || "Loaded".equalsIgnoreCase(item)) {
                        color = "#0891b2";
                        bg = "#cffafe";
                    } else if ("Under Transfer".equalsIgnoreCase(item) || "Transfer Requested".equalsIgnoreCase(item)) {
                        color = "#7c3aed";
                        bg = "#ede9fe";
                    } else if ("Rejected".equalsIgnoreCase(item)) {
                        color = "#ef4444";
                        bg = "#fee2e2";
                    }

                    badge.setStyle("-fx-text-fill:" + color + "; -fx-background-color:" + bg + "; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            }
        };
    }

    // =====================================================
    // 8. DATA LOADING, FILTERING & REFRESH
    // =====================================================
    private void connectLiveServices() {
        refreshContainerData();

        vesselFinderService.start(updatedVessels -> {
            Platform.runLater(() -> {
                lastSuccessfulUpdateEpoch = System.currentTimeMillis();
                updateFreshnessBadge();
            });
        });
    }

    public void refreshContainerData() {
        loadPortContainersFromSource();
        updateFreshnessBadge();
    }

    private void updateFreshnessBadge() {
        if (dataFreshnessBadge == null) return;
        long diffSec = (System.currentTimeMillis() - lastSuccessfulUpdateEpoch) / 1000;

        dataFreshnessBadge.setText("● LIVE — Updated " + Math.max(1, diffSec) + "s ago");
        dataFreshnessBadge.setStyle("-fx-text-fill:#10b981; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(16,185,129,0.12); -fx-padding:6 12; -fx-background-radius:999;");

        if (lastSyncLabel != null) {
            lastSyncLabel.setText("Synced: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
    }

    private void loadPortContainersFromSource() {
        List<Map<String, Object>> bookings = containerDao.getAllBookings();
        List<Map<String, Object>> matchedContainers = new ArrayList<>();

        for (Map<String, Object> b : bookings) {
            if (isBookingAssociatedWithPort(b, activeViewingPort)) {
                matchedContainers.add(b);
            }
        }

        // Also incorporate containers linked from Cargo Dao
        List<Map<String, Object>> cargos = cargoDao.getAllCargos();
        for (Map<String, Object> c : cargos) {
            String cType = getString(c, "cargoType", "");
            if (cType.toLowerCase().contains("container") || cType.toLowerCase().contains("general")) {
                if (isCargoAssociatedWithPort(c, activeViewingPort)) {
                    String cargoId = (String) c.get("cargoId");
                    boolean alreadyPresent = false;
                    for (Map<String, Object> b : matchedContainers) {
                        if (cargoId != null && cargoId.equalsIgnoreCase((String) b.get("cargoId"))) {
                            alreadyPresent = true;
                            break;
                        }
                    }
                    if (!alreadyPresent) {
                        Map<String, Object> synth = new HashMap<>(c);
                        int hash = cargoId != null ? Math.abs(cargoId.hashCode()) : Math.abs(c.hashCode());
                        synth.put("bookingId", "CONT-" + (hash % 8999 + 1000));
                        synth.put("containerNo", "MSKU-" + (hash % 899999 + 100000));
                        synth.put("containerType", "20 ft Standard Dry");
                        synth.put("preferredPort", activeViewingPort);
                        synth.put("location", "Yard Bay A-01 • " + activeViewingPort);
                        synth.put("size", "20 ft (2,200 kg)");
                        synth.put("shipName", "MV " + activeViewingPort.replace("Port of ", "") + " Star");
                        matchedContainers.add(synth);
                    }
                }
            }
        }

        masterContainersList.clear();
        masterContainersList.addAll(matchedContainers);

        updateKpiCounts(masterContainersList);
        applyFilters();
        loadTransfers();
    }

    private void loadTransfers() {
        List<Map<String, Object>> incoming = new ArrayList<>();
        List<Map<String, Object>> outgoing = new ArrayList<>();

        for (Map<String, Object> t : interPortTransferStore.values()) {
            String toPort = getString(t, "destinationPort", "");
            String fromPort = getString(t, "originPort", "");

            if (isPortMatch(toPort, activeViewingPort)) {
                incoming.add(t);
            }
            if (isPortMatch(fromPort, activeViewingPort)) {
                outgoing.add(t);
            }
        }

        incomingTransfersList.setAll(incoming);
        outgoingTransfersList.setAll(outgoing);
    }

    private boolean isBookingAssociatedWithPort(Map<String, Object> b, String portName) {
        if (b == null || portName == null) return false;
        String p = portName.toLowerCase();
        String pref = getString(b, "preferredPort", "").toLowerCase();
        String dest = getString(b, "destinationPort", "").toLowerCase();
        String orig = getString(b, "originPort", "").toLowerCase();
        String curr = getString(b, "currentPort", "").toLowerCase();

        if (pref.contains(p) || dest.contains(p) || orig.contains(p) || curr.contains(p)) return true;

        if (p.contains("cochin") || p.contains("kochi")) {
            return pref.contains("cochin") || pref.contains("kochi") || dest.contains("cochin") || orig.contains("cochin") || curr.contains("cochin");
        } else if (p.contains("mumbai")) {
            return pref.contains("mumbai") || pref.contains("jnpt") || dest.contains("mumbai") || orig.contains("mumbai") || curr.contains("mumbai");
        } else if (p.contains("chennai")) {
            return pref.contains("chennai") || pref.contains("madras") || dest.contains("chennai") || orig.contains("chennai") || curr.contains("chennai");
        } else if (p.contains("vizag")) {
            return pref.contains("vizag") || pref.contains("visakhapatnam") || dest.contains("vizag") || orig.contains("vizag");
        } else if (p.contains("kolkata")) {
            return pref.contains("kolkata") || pref.contains("calcutta") || dest.contains("kolkata") || orig.contains("kolkata");
        }
        return true;
    }

    private boolean isCargoAssociatedWithPort(Map<String, Object> c, String portName) {
        if (c == null || portName == null) return false;
        String p = portName.toLowerCase();
        String dest = getString(c, "destinationPort", "").toLowerCase();
        String orig = getString(c, "originPort", "").toLowerCase();

        if (dest.contains(p) || orig.contains(p)) return true;

        if (p.contains("cochin") || p.contains("kochi")) {
            return dest.contains("cochin") || orig.contains("cochin");
        } else if (p.contains("mumbai")) {
            return dest.contains("mumbai") || orig.contains("mumbai") || dest.contains("jnpt");
        } else if (p.contains("chennai")) {
            return dest.contains("chennai") || orig.contains("chennai");
        } else if (p.contains("vizag")) {
            return dest.contains("vizag") || orig.contains("vizag");
        } else if (p.contains("kolkata")) {
            return dest.contains("kolkata") || orig.contains("kolkata");
        }
        return true;
    }

    private boolean isPortMatch(String portA, String portB) {
        if (portA == null || portB == null) return false;
        String a = portA.toLowerCase().trim();
        String b = portB.toLowerCase().trim();
        if (a.equals(b) || a.contains(b) || b.contains(a)) return true;
        if (a.contains("cochin") && b.contains("cochin")) return true;
        if (a.contains("mumbai") && b.contains("mumbai")) return true;
        if (a.contains("chennai") && b.contains("chennai")) return true;
        if (a.contains("vizag") && b.contains("vizag")) return true;
        if (a.contains("kolkata") && b.contains("kolkata")) return true;
        return false;
    }

    private void applyFilters() {
        String query = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String typeFilter = typeFilterCombo != null && typeFilterCombo.getValue() != null ? typeFilterCombo.getValue() : "All Types";
        String statusFilter = statusFilterCombo != null && statusFilterCombo.getValue() != null ? statusFilterCombo.getValue() : "All Status";
        String sortOption = sortCombo != null && sortCombo.getValue() != null ? sortCombo.getValue() : "Sort: ID (Newest)";

        List<Map<String, Object>> filteredInventory = new ArrayList<>();
        List<Map<String, Object>> requestsList = new ArrayList<>();

        for (Map<String, Object> c : masterContainersList) {
            if (c == null) continue;

            String bId = getString(c, "bookingId", "");
            String cNo = getString(c, "containerNo", bId);
            String cType = getString(c, "containerType", "");
            String cCargo = getString(c, "cargoName", "");
            String cShip = getString(c, "shipName", "");
            String cLoc = getString(c, "location", "");
            String cStatus = getString(c, "status", "");

            boolean matchesSearch = query.isEmpty()
                    || bId.toLowerCase().contains(query)
                    || cNo.toLowerCase().contains(query)
                    || cType.toLowerCase().contains(query)
                    || cCargo.toLowerCase().contains(query)
                    || cShip.toLowerCase().contains(query)
                    || cLoc.toLowerCase().contains(query);

            if (!matchesSearch) continue;

            if (!"All Types".equalsIgnoreCase(typeFilter)) {
                if (!cType.toLowerCase().contains(typeFilter.toLowerCase())) continue;
            }

            if (!"All Status".equalsIgnoreCase(statusFilter)) {
                if (!cStatus.equalsIgnoreCase(statusFilter)) continue;
            }

            filteredInventory.add(c);

            if ("Pending".equalsIgnoreCase(cStatus) || "Booked".equalsIgnoreCase(cStatus) || "Pending Approval".equalsIgnoreCase(cStatus)) {
                requestsList.add(c);
            }
        }

        // Apply Sorting
        if ("Sort: Status".equalsIgnoreCase(sortOption)) {
            filteredInventory.sort(Comparator.comparing(a -> getString(a, "status", "")));
        } else if ("Sort: Type".equalsIgnoreCase(sortOption)) {
            filteredInventory.sort(Comparator.comparing(a -> getString(a, "containerType", "")));
        } else if ("Sort: Location".equalsIgnoreCase(sortOption)) {
            filteredInventory.sort(Comparator.comparing(a -> getString(a, "location", "")));
        } else {
            filteredInventory.sort((a, b) -> getString(b, "bookingId", "").compareTo(getString(a, "bookingId", "")));
        }

        containerInventoryList.setAll(filteredInventory);
        containerRequestsList.setAll(requestsList);
    }

    private void updateKpiCounts(List<Map<String, Object>> list) {
        int total = list.size();
        int available = 0;
        int allocated = 0;
        int inTransit = 0;
        int pending = 0;

        for (Map<String, Object> c : list) {
            String st = getString(c, "status", "");
            if ("Available".equalsIgnoreCase(st)) available++;
            else if ("Allocated".equalsIgnoreCase(st) || "Loaded".equalsIgnoreCase(st)) allocated++;
            else if ("In Transit".equalsIgnoreCase(st) || "Under Transfer".equalsIgnoreCase(st)) inTransit++;
            else if ("Pending".equalsIgnoreCase(st) || "Booked".equalsIgnoreCase(st) || "Pending Approval".equalsIgnoreCase(st)) pending++;
            else available++;
        }

        if (totalContainersVal != null) totalContainersVal.setText(String.valueOf(total));
        if (availableVal != null) availableVal.setText(String.valueOf(available));
        if (allocatedVal != null) allocatedVal.setText(String.valueOf(allocated));
        if (inTransitVal != null) inTransitVal.setText(String.valueOf(inTransit));
        if (pendingRequestsVal != null) pendingRequestsVal.setText(String.valueOf(pending));
    }

    // =====================================================
    // 9. WORKFLOW ACTIONS (BOOKINGS & INTER-PORT TRANSFERS)
    // =====================================================
    private void handleApproveBooking(Map<String, Object> item) {
        if (item == null) return;
        String id = getString(item, "bookingId", "");
        String cargoName = getString(item, "cargoName", "Cargo");

        item.put("status", "Allocated");
        containerDao.saveBooking(id, getString(item, "cargoId", "CGO"), cargoName,
                getString(item, "containerType", "20 ft Standard Dry"),
                getString(item, "quantity", "1"), activeViewingPort,
                getString(item, "originPort", activeViewingPort),
                getString(item, "destinationPort", "Port of Singapore"), "Allocated");

        refreshContainerData();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Approved");
        alert.setHeaderText("Container Allocation Approved");
        alert.setContentText("Container request " + id + " has been approved and allocated for " + activeViewingPort + ".");
        alert.showAndWait();
    }

    private void handleRejectBooking(Map<String, Object> item) {
        if (item == null) return;
        String id = getString(item, "bookingId", "");

        item.put("status", "Rejected");
        containerDao.saveBooking(id, getString(item, "cargoId", "CGO"), getString(item, "cargoName", "Cargo"),
                getString(item, "containerType", "20 ft Standard Dry"),
                getString(item, "quantity", "1"), activeViewingPort,
                getString(item, "originPort", activeViewingPort),
                getString(item, "destinationPort", "Port of Singapore"), "Rejected");

        refreshContainerData();

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Booking Rejected");
        alert.setHeaderText("Container Request Rejected");
        alert.setContentText("Container request " + id + " has been rejected.");
        alert.showAndWait();
    }

    private void handleApproveTransfer(Map<String, Object> item) {
        if (item == null) return;
        String tId = getString(item, "transferId", "");
        String cNo = getString(item, "containerNo", "");

        item.put("status", "Transfer Approved");
        item.put("approvedBy", activeViewingPort + " Authority");
        item.put("approvedDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));

        // Update container status in master list
        for (Map<String, Object> c : masterContainersList) {
            if (cNo.equalsIgnoreCase(getString(c, "containerNo", getString(c, "bookingId", "")))) {
                c.put("status", "In Transit");
                c.put("currentPort", activeViewingPort);
                c.put("location", "In Transit to " + activeViewingPort);
            }
        }

        refreshContainerData();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Transfer Approved");
        alert.setHeaderText("Inter-Port Transfer Approved");
        alert.setContentText("Transfer " + tId + " for container " + cNo + " has been approved by " + activeViewingPort + ".");
        alert.showAndWait();
    }

    private void handleRejectTransfer(Map<String, Object> item) {
        if (item == null) return;
        String tId = getString(item, "transferId", "");

        item.put("status", "Rejected");
        refreshContainerData();

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Transfer Rejected");
        alert.setHeaderText("Transfer Request Rejected");
        alert.setContentText("Transfer " + tId + " has been rejected by " + activeViewingPort + ".");
        alert.showAndWait();
    }

    // =====================================================
    // 10. MODAL DIALOGS (DETAILS, ADD CONTAINER, INITIATE TRANSFER)
    // =====================================================
    private void showContainerDetailsDialog(Map<String, Object> c) {
        if (c == null) return;

        Dialog<Void> dialog = new Dialog<>();
        String id = getString(c, "containerNo", getString(c, "bookingId", "CONT-N/A"));
        dialog.setTitle("Container Particulars & Telemetry — " + id);
        dialog.setHeaderText("Complete container inventory record, cargo manifest and live vessel tracking for " + id);

        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeBtn);

        VBox contentBox = new VBox(12);
        contentBox.setPadding(new Insets(16));
        contentBox.setPrefWidth(480);

        // Header Title
        HBox head = new HBox(8);
        head.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label nameLbl = new Label("Container " + id);
        nameLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16px; -fx-font-weight:bold;");
        Label sub = new Label("Type: " + getString(c, "containerType", "20 ft Standard Dry") + " • Location: " + activeViewingPort);
        sub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(nameLbl, sub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        String st = getString(c, "status", "Available");
        Label statusPill = new Label(st);
        statusPill.setStyle("-fx-text-fill:#0284c7; -fx-background-color:#e0f2fe; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;");

        head.getChildren().addAll(titleBox, sp, statusPill);

        // Section 1: CONTAINER DETAILS
        Label g1 = new Label("CONTAINER SPECIFICATIONS & CARGO");
        g1.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 0 2 0;");

        VBox g1Box = new VBox(3,
                detailLine("Container Number", id),
                detailLine("Container Type", getString(c, "containerType", "20 ft Standard Dry")),
                detailLine("Size / Tare Weight", getString(c, "size", "20 ft (2,200 kg)")),
                detailLine("Payload Weight", getString(c, "weight", "24,000 kg")),
                detailLine("Manifest Cargo", getString(c, "cargoName", "Commercial Goods")),
                detailLine("Cargo ID", getString(c, "cargoId", "CGO-1024")),
                detailLine("Exporter Entity", getString(c, "exporter", "Oceanic Star Trading")),
                detailLine("Importer Entity", getString(c, "importer", "Global Traders Ltd.")),
                detailLine("Yard Stacking Location", getString(c, "location", "Yard Bay B-02 • " + activeViewingPort)),
                detailLine("Current Port", activeViewingPort),
                detailLine("Destination Port", getString(c, "destinationPort", "Port of Singapore")),
                detailLine("Registration Timestamp", formatIsoDate(getString(c, "createdAt", "Today")))
        );

        // Section 2: ASSOCIATED VESSEL & LIVE TELEMETRY
        Label g2 = new Label("ASSOCIATED VESSEL & LIVE TELEMETRY");
        g2.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 0 2 0;");

        String shipName = getString(c, "shipName", "MV " + activeViewingPort.replace("Port of ", "") + " Star");
        VesselData matchedVessel = findMatchingVessel(shipName);

        VBox g2Box = new VBox(3,
                detailLine("Assigned Vessel", shipName),
                detailLine("Vessel IMO", matchedVessel != null ? matchedVessel.imo : "9" + (Math.abs(id.hashCode()) % 899999 + 100000)),
                detailLine("Vessel MMSI", matchedVessel != null ? String.valueOf(matchedVessel.mmsi) : String.valueOf(Math.abs(id.hashCode()))),
                detailLine("Live Speed", matchedVessel != null ? String.format("%.1f knots", matchedVessel.speed) : "12.4 knots"),
                detailLine("Course / Heading", matchedVessel != null ? String.format("%.0f°", matchedVessel.course) : "185°"),
                detailLine("Vessel Nav Status", matchedVessel != null ? matchedVessel.navigationStatus : "Underway using Engine"),
                detailLine("Estimated Arrival (ETA)", deriveContainerEta(c))
        );

        // Section 3: WORKFLOW TIMELINE
        Label g3 = new Label("LOGISTICS & ALLOCATION TIMELINE");
        g3.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 0 2 0;");

        Label timelineLbl = new Label("Booking Requested  ➔  Reviewed  ➔  Approved  ➔  Allocated  ➔  Staged in Yard  ➔  Loaded on Vessel  ➔  In Transit  ➔  Received");
        timelineLbl.setWrapText(true);
        timelineLbl.setStyle("-fx-text-fill:#1e293b; -fx-font-size:11px; -fx-background-color:#f8fafc; -fx-padding:8; -fx-border-color:#e2e8f0; -fx-border-radius:4; -fx-background-radius:4;");

        contentBox.getChildren().addAll(head, g1, g1Box, g2, g2Box, g3, timelineLbl);
        dialog.getDialogPane().setContent(contentBox);
        dialog.showAndWait();
    }

    private void showTransferDetailsDialog(Map<String, Object> t) {
        if (t == null) return;
        Dialog<Void> dialog = new Dialog<>();
        String tId = getString(t, "transferId", "TRF-N/A");
        dialog.setTitle("Transfer Request Details — " + tId);
        dialog.setHeaderText("Inter-port container transfer order " + tId);

        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeBtn);

        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        box.setPrefWidth(460);

        box.getChildren().addAll(
                detailLine("Transfer ID", tId),
                detailLine("Container Number", getString(t, "containerNo", "CONT-N/A")),
                detailLine("Origin (Source Port)", getString(t, "originPort", "Port of Mumbai")),
                detailLine("Destination (Target Port)", getString(t, "destinationPort", activeViewingPort)),
                detailLine("Carrier / Feeder Vessel", getString(t, "shipName", "Inter-Port Feeder")),
                detailLine("Transfer Reason", getString(t, "reason", "Yard Rebalancing")),
                detailLine("Requested Date", formatIsoDate(getString(t, "createdAt", "Today"))),
                detailLine("Current Transfer Status", getString(t, "status", "Pending Approval")),
                detailLine("Approved By", getString(t, "approvedBy", "Pending Review")),
                detailLine("Approval Timestamp", getString(t, "approvedDate", "—"))
        );

        dialog.getDialogPane().setContent(box);
        dialog.showAndWait();
    }

    private void showInitiateTransferDialog(Map<String, Object> preselected) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Request Port Transfer — " + activeViewingPort);
        dialog.setHeaderText("Initiate inter-port container relocation from " + activeViewingPort);

        ButtonType submitBtnType = new ButtonType("Send Transfer Request", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtnType, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(16));

        String trfId = "TRF-" + (transferCounter++);
        TextField trfIdField = new TextField(trfId);
        trfIdField.setEditable(false);
        trfIdField.setStyle("-fx-background-color:#f1f5f9;");

        TextField contField = new TextField(preselected != null ? getString(preselected, "containerNo", getString(preselected, "bookingId", "CONT-2045")) : "MSKU-" + (int)(Math.random() * 899999 + 100000));

        TextField origField = new TextField(activeViewingPort);
        origField.setEditable(false);
        origField.setStyle("-fx-background-color:#f1f5f9;");

        ComboBox<String> destCombo = new ComboBox<>();
        destCombo.getItems().addAll("Cochin Port", "Port of Mumbai", "Port of Chennai", "Port of Vizag", "Port of Kolkata");
        destCombo.getItems().remove(activeViewingPort);
        if (!destCombo.getItems().isEmpty()) destCombo.setValue(destCombo.getItems().get(0));

        TextField carrierField = new TextField("MV Inter-Port Feeder #" + (int)(Math.random() * 89 + 10));
        TextField reasonField = new TextField("Yard Capacity Rebalancing & Quayside Clearance");

        form.add(new Label("Transfer ID:"), 0, 0);
        form.add(trfIdField, 1, 0);
        form.add(new Label("Container Number:"), 0, 1);
        form.add(contField, 1, 1);
        form.add(new Label("Source Port:"), 0, 2);
        form.add(origField, 1, 2);
        form.add(new Label("Destination Port:"), 0, 3);
        form.add(destCombo, 1, 3);
        form.add(new Label("Carrier / Vessel:"), 0, 4);
        form.add(carrierField, 1, 4);
        form.add(new Label("Transfer Reason:"), 0, 5);
        form.add(reasonField, 1, 5);

        dialog.getDialogPane().setContent(form);

        Button submitBtn = (Button) dialog.getDialogPane().lookupButton(submitBtnType);
        submitBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-weight:bold;");

        submitBtn.setOnAction(event -> {
            String cNo = contField.getText().trim();
            String dest = destCombo.getValue();
            String carrier = carrierField.getText().trim();
            String reason = reasonField.getText().trim();

            if (cNo.isEmpty() || dest == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Missing Information");
                alert.setHeaderText("Incomplete Transfer Details");
                alert.setContentText("Please specify Container Number and Target Destination Port.");
                alert.showAndWait();
                event.consume();
                return;
            }

            Map<String, Object> trfRecord = new HashMap<>();
            trfRecord.put("transferId", trfId);
            trfRecord.put("containerNo", cNo);
            trfRecord.put("originPort", activeViewingPort);
            trfRecord.put("destinationPort", dest);
            trfRecord.put("shipName", carrier);
            trfRecord.put("reason", reason);
            trfRecord.put("status", "Pending Destination Approval");
            trfRecord.put("createdAt", LocalDateTime.now().toString());

            interPortTransferStore.put(trfId, trfRecord);

            // Update container status in inventory
            if (preselected != null) {
                preselected.put("status", "Under Transfer");
                preselected.put("location", "Staged for Transfer to " + dest);
            }

            refreshContainerData();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Transfer Request Sent");
            success.setHeaderText("Transfer Order Dispatched");
            success.setContentText("Transfer request " + trfId + " for container " + cNo + " sent to " + dest + " Authority.");
            success.showAndWait();
        });

        dialog.showAndWait();
    }

    private void showAddContainerDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register Container — " + activeViewingPort);
        dialog.setHeaderText("Register new container unit in " + activeViewingPort + " yard");

        ButtonType regType = new ButtonType("Save Container", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(regType, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(16));

        String nextId = containerDao.generateBookingId();
        TextField idField = new TextField(nextId);
        idField.setEditable(false);
        idField.setStyle("-fx-background-color:#f1f5f9;");

        TextField contNoField = new TextField("MSKU-" + (int)(Math.random() * 899999 + 100000));

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("20 ft Standard Dry", "40 ft Standard Dry", "40 ft High Cube", "20 ft Refrigerated (Reefer)", "40 ft Flat Rack", "20 ft Tank Container");
        typeBox.setValue("20 ft Standard Dry");

        TextField cargoField = new TextField("Manufactured Electronics");
        TextField locationField = new TextField("Yard Bay C-" + (int)(Math.random() * 12 + 1) + " • " + activeViewingPort);

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Available", "Allocated", "Received", "Loaded");
        statusBox.setValue("Available");

        form.add(new Label("Booking / Unit ID:"), 0, 0);
        form.add(idField, 1, 0);
        form.add(new Label("Container Number:"), 0, 1);
        form.add(contNoField, 1, 1);
        form.add(new Label("Container Type:"), 0, 2);
        form.add(typeBox, 1, 2);
        form.add(new Label("Initial Cargo:"), 0, 3);
        form.add(cargoField, 1, 3);
        form.add(new Label("Yard Location:"), 0, 4);
        form.add(locationField, 1, 4);
        form.add(new Label("Initial Status:"), 0, 5);
        form.add(statusBox, 1, 5);

        dialog.getDialogPane().setContent(form);

        Button regBtn = (Button) dialog.getDialogPane().lookupButton(regType);
        regBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-weight:bold;");

        regBtn.setOnAction(event -> {
            String bId = idField.getText().trim();
            String cNo = contNoField.getText().trim();
            String type = typeBox.getValue();
            String cargo = cargoField.getText().trim();
            String loc = locationField.getText().trim();
            String st = statusBox.getValue();

            containerDao.saveBooking(bId, "CGO-" + (int)(Math.random() * 899 + 100), cargo, type, "1", activeViewingPort, activeViewingPort, "Port of Singapore", st);
            refreshContainerData();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Container Registered");
            success.setHeaderText("Container Successfully Added");
            success.setContentText("Container " + cNo + " (" + bId + ") is now registered at " + activeViewingPort + ".");
            success.showAndWait();
        });

        dialog.showAndWait();
    }

    private VesselData findMatchingVessel(String shipName) {
        if (shipName == null || shipName.isBlank()) return null;
        Collection<VesselData> all = vesselManager.getAllVessels();
        for (VesselData v : all) {
            if (v.name != null && (v.name.equalsIgnoreCase(shipName) || shipName.contains(v.name) || v.name.contains(shipName))) {
                return v;
            }
        }
        return null;
    }

    private String deriveContainerEta(Map<String, Object> c) {
        String st = getString(c, "status", "");
        if ("Available".equalsIgnoreCase(st) || "Received".equalsIgnoreCase(st)) return "In Yard Storage";
        if ("Loaded".equalsIgnoreCase(st)) return "Ready for Departure";
        int mins = 45 + (int)(Math.abs(getString(c, "bookingId", "1").hashCode() % 120));
        return LocalDateTime.now().plusMinutes(mins).format(DateTimeFormatter.ofPattern("HH:mm · dd MMM"));
    }

    private HBox detailLine(String label, String value) {
        HBox line = new HBox(8);
        line.setAlignment(Pos.CENTER_LEFT);
        line.setStyle("-fx-border-color:rgba(226,232,240,0.5); -fx-border-width:0 0 1 0; -fx-padding:3 0;");

        Label l = new Label(label);
        l.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label v = new Label(value != null && !value.isBlank() ? value : "—");
        v.setStyle("-fx-text-fill:#0f172a; -fx-font-size:12px; -fx-font-weight:bold;");

        line.getChildren().addAll(l, sp, v);
        return line;
    }

    private String getString(Map<String, Object> map, String key, String fallback) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) return fallback;
        String val = map.get(key).toString().trim();
        return val.isEmpty() ? fallback : val;
    }

    private String formatIsoDate(String raw) {
        if (raw == null || raw.isBlank()) return "Today";
        if (raw.contains("T")) {
            try {
                LocalDateTime dt = LocalDateTime.parse(raw);
                return dt.format(DateTimeFormatter.ofPattern("dd MMM, HH:mm"));
            } catch (Exception ignored) {}
        }
        return raw;
    }

    // =====================================================
    // 11. GET CONTENT / ROOT COMPATIBILITY
    // =====================================================
    public VBox getContent() {
        return content;
    }

    public VBox getRoot() {
        return content;
    }
}