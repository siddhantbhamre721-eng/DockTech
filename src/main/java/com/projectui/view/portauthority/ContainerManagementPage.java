package com.projectui.view.portauthority;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.projectui.dao.CargoDao;
import com.projectui.dao.ContainerDao;
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
import javafx.scene.control.DialogPane;
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

    // Backend DAOs & Services (Single source of truth with Exporter, Importer & Admin)
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

    // Top Summary KPI Card Values (All 7 Live Dynamic Metrics)
    private Label totalContainersVal;
    private Label availableVal;
    private Label allocatedVal;
    private Label inTransitVal;
    private Label pendingRequestsVal;
    private Label requiredVal;
    private Label shortageVal;
    private Label shortageSubLabel;

    // Master & Filtered Lists
    private final List<Map<String, Object>> masterContainersList = new ArrayList<>();
    private final List<Map<String, Object>> masterCargosList = new ArrayList<>();

    private final ObservableList<Map<String, Object>> containerRequestsList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> allocationRequestsList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> containerInventoryList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> availableContainersList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> activeAllocationsList = FXCollections.observableArrayList();

    // Tables & Controls
    private TableView<Map<String, Object>> requestsTable;
    private TableView<Map<String, Object>> allocationRequestsTable;
    private TableView<Map<String, Object>> inventoryTable;
    private TableView<Map<String, Object>> availableTable;
    private TableView<Map<String, Object>> activeAllocationsTable;
    private VBox availableEmptyBanner;

    private TextField searchField;
    private ComboBox<String> typeFilterCombo;
    private ComboBox<String> statusFilterCombo;
    private ComboBox<String> sortCombo;

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
        return com.projectui.dao.UserDao.normalizePort(raw);
    }

    // =====================================================
    // 2. LAYOUT COMPOSITION (COMBINED CONTAINER MANAGEMENT)
    // =====================================================
    private void buildLayout() {
        VBox header = buildHeader();
        HBox summaryCards = buildSummaryCards();
        VBox requestsSection = buildContainerRequestsSection();
        VBox allocationRequestsSection = buildAllocationRequestsSection();
        VBox inventorySection = buildContainerInventorySection();
        HBox availableAndAllocationsSplit = buildAvailableAndAllocationsSplit();

        content.getChildren().addAll(
                header,
                summaryCards,
                requestsSection,
                allocationRequestsSection,
                inventorySection,
                availableAndAllocationsSplit
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

        pageSubLabel = new Label("Live container inventory, cargo demand allocations and shipper booking management for " + activeViewingPort);
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
    // 4. SECTION 1 — TOP SUMMARY KPI CARDS (ALL 7 LIVE METRICS)
    // =====================================================
    private HBox buildSummaryCards() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        totalContainersVal = new Label("0");
        availableVal = new Label("0");
        allocatedVal = new Label("0");
        inTransitVal = new Label("0");
        pendingRequestsVal = new Label("0");
        requiredVal = new Label("0");
        shortageVal = new Label("0");

        VBox cTotal = createKpiCard("TOTAL CONTAINERS", totalContainersVal, "Registered fleet", "📦", "#0284c7", "#e0f2fe");
        VBox cAvailable = createKpiCard("AVAILABLE", availableVal, "Ready to allocate", "🟢", "#16a34a", "#ecfdf5");
        VBox cAllocated = createKpiCard("ALLOCATED", allocatedVal, "Assigned to cargo", "📑", "#2563eb", "#eff6ff");
        VBox cInTransit = createKpiCard("IN TRANSIT", inTransitVal, "En route at sea", "🚢", "#8b5cf6", "#f5f3ff");
        VBox cPending = createKpiCard("PENDING REQUESTS", pendingRequestsVal, "Awaiting allocation", "📋", "#d97706", "#fef3c7");
        VBox cRequired = createKpiCard("REQUIRED", requiredVal, "Units demanded", "🎯", "#0891b2", "#cffafe");

        // Dynamic Shortage Card
        VBox cShortage = new VBox(6);
        cShortage.setPadding(new Insets(12, 14, 12, 14));
        cShortage.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        HBox shTop = new HBox(8);
        shTop.setAlignment(Pos.CENTER_LEFT);
        Label shIcon = new Label("⚠️");
        shIcon.setStyle("-fx-text-fill:#ef4444; -fx-font-size:14px; -fx-font-weight:bold; -fx-background-color:#fee2e2; -fx-padding:3 6; -fx-background-radius:6;");
        Label shTitle = new Label("SHORTAGE");
        shTitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px; -fx-font-weight:bold;");
        shTop.getChildren().addAll(shIcon, shTitle);

        shortageVal.setStyle("-fx-text-fill:#16a34a; -fx-font-size:24px; -fx-font-weight:bold;");
        shortageSubLabel = new Label("Demand covered");
        shortageSubLabel.setStyle("-fx-text-fill:#16a34a; -fx-font-size:10px;");
        cShortage.getChildren().addAll(shTop, shortageVal, shortageSubLabel);

        row.getChildren().addAll(cTotal, cAvailable, cAllocated, cInTransit, cPending, cRequired, cShortage);

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
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(iconStr);
        icon.setStyle("-fx-text-fill:" + color + "; -fx-font-size:14px; -fx-font-weight:bold; -fx-background-color:" + bg + "; -fx-padding:3 6; -fx-background-radius:6;");

        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px; -fx-font-weight:bold;");

        top.getChildren().addAll(icon, t);

        valLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:24px; -fx-font-weight:bold;");

        Label sub = new Label(subtitle);
        sub.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:10px;");

        card.getChildren().addAll(top, valLabel, sub);
        return card;
    }

    // =====================================================
    // 5. SECTION 2 — CONTAINER REQUESTS (EXPORTER & IMPORTER BOOKINGS)
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
        Label secSub = new Label("Live container booking requests submitted by authorized Exporters and Importers for " + activeViewingPort);
        secSub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(secTitle, secSub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        topBar.getChildren().addAll(titleBox, sp);

        requestsTable = new TableView<>();
        requestsTable.setPrefHeight(220);
        requestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        requestsTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        requestsTable.setPlaceholder(new Label("No container requests from Exporters or Importers for " + activeViewingPort));

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
        colCargo.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "cargoName", "Commercial Goods") + " (" + getString(d.getValue(), "cargoId", "CGO") + ")"));
        colCargo.setStyle("-fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colRequester = new TableColumn<>("Exporter / Importer");
        colRequester.setCellValueFactory(d -> new SimpleStringProperty(getRequesterDisplay(d.getValue())));
        colRequester.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Map<String, Object> row = getTableView().getItems().get(getIndex());
                    String cType = getString(row, "customerType", "Exporter");
                    HBox box = new HBox(6);
                    box.setAlignment(Pos.CENTER_LEFT);

                    Label roleChip = new Label(cType);
                    if ("Importer".equalsIgnoreCase(cType)) {
                        roleChip.setStyle("-fx-background-color:#ede9fe; -fx-text-fill:#7c3aed; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:2 6; -fx-background-radius:4;");
                    } else {
                        roleChip.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0284c7; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:2 6; -fx-background-radius:4;");
                    }

                    Label nameLbl = new Label(item);
                    nameLbl.setStyle("-fx-font-size:12px; -fx-text-fill:#334155; -fx-font-weight:bold;");

                    box.getChildren().addAll(roleChip, nameLbl);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        TableColumn<Map<String, Object>, String> colShip = new TableColumn<>("Ship / Voyage");
        colShip.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "shipName", "MV " + activeViewingPort.replace("Port of ", "") + " Express")));
        colShip.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#2563eb; -fx-font-weight:bold;");

        TableColumn<Map<String, Object>, String> colOrigin = new TableColumn<>("Origin");
        colOrigin.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "originPort", activeViewingPort)));
        colOrigin.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colDest = new TableColumn<>("Destination");
        colDest.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "destinationPort", "Port of Singapore")));
        colDest.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#0f172a; -fx-font-weight:bold;");

        TableColumn<Map<String, Object>, String> colQty = new TableColumn<>("Qty / Wt");
        colQty.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "quantity", "1") + " units" + (d.getValue().containsKey("weight") ? " (" + d.getValue().get("weight") + ")" : "")));
        colQty.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colDate = new TableColumn<>("Request Date");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(formatIsoDate(getString(d.getValue(), "createdAt", "Today"))));
        colDate.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "status", "Booked")));
        colStatus.setCellFactory(col -> createStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(160);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final HBox btnBox = new HBox(4, viewBtn, approveBtn, rejectBtn);

            {
                btnBox.setAlignment(Pos.CENTER);
                viewBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 6; -fx-cursor:hand;");
                approveBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 6; -fx-cursor:hand;");
                rejectBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 6; -fx-cursor:hand;");

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
                colShip, colOrigin, colDest, colQty, colDate, colStatus, colAction
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

    private String getRequesterDisplay(Map<String, Object> req) {
        if (req == null) return "Global Logistics Ltd.";
        if (req.get("exporter") != null && !req.get("exporter").toString().isBlank()) {
            return req.get("exporter").toString();
        }
        if (req.get("company") != null && !req.get("company").toString().isBlank()) {
            return req.get("company").toString();
        }
        if (req.get("userName") != null && !req.get("userName").toString().isBlank()) {
            return req.get("userName").toString();
        }
        return "Global Logistics Ltd.";
    }

    // =====================================================
    // 6. SECTION 3 — CONTAINER ALLOCATION REQUESTS (CARGO DEMANDS)
    // =====================================================
    private VBox buildAllocationRequestsSection() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label secTitle = new Label("CONTAINER ALLOCATION REQUESTS — Cargo Demands");
        secTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:15px; -fx-font-weight:bold;");
        Label secSub = new Label("Cargo bookings requiring container unit allocation for " + activeViewingPort);
        secSub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(secTitle, secSub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button allocateNewBtn = new Button("+ Allocate Container");
        allocateNewBtn.setPrefHeight(34);
        allocateNewBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 14; -fx-cursor:hand;");
        allocateNewBtn.setOnAction(e -> showAllocationDialog(null, null));

        topBar.getChildren().addAll(titleBox, sp, allocateNewBtn);

        allocationRequestsTable = new TableView<>();
        allocationRequestsTable.setPrefHeight(230);
        allocationRequestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        allocationRequestsTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        allocationRequestsTable.setPlaceholder(new Label("No pending allocation requests for " + activeViewingPort));

        TableColumn<Map<String, Object>, String> colReqId = new TableColumn<>("Request ID");
        colReqId.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "bookingId", getString(d.getValue(), "cargoId", "REQ-N/A"))));
        colReqId.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0284c7;");

        TableColumn<Map<String, Object>, String> colCargo = new TableColumn<>("Cargo / Goods");
        colCargo.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "cargoName", "Commercial Cargo")));
        colCargo.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colType = new TableColumn<>("Container Type");
        colType.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "containerType", getString(d.getValue(), "cargoType", "General Cargo"))));
        colType.setStyle("-fx-font-size:12px; -fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colReq = new TableColumn<>("Required");
        colReq.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(getRequestRequiredUnits(d.getValue()))));
        colReq.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0891b2;");

        TableColumn<Map<String, Object>, String> colAlloc = new TableColumn<>("Allocated");
        colAlloc.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(getRequestAllocatedUnits(d.getValue()))));
        colAlloc.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#16a34a;");

        TableColumn<Map<String, Object>, String> colRem = new TableColumn<>("Remaining");
        colRem.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(getRequestRemainingUnits(d.getValue()))));
        colRem.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    int rem = parseQuantity(item, 0);
                    Label badge = new Label(String.valueOf(rem));
                    if (rem > 0) {
                        badge.setStyle("-fx-text-fill:#ef4444; -fx-background-color:#fee2e2; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;");
                    } else {
                        badge.setStyle("-fx-text-fill:#16a34a; -fx-background-color:#dcfce7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;");
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            }
        });

        TableColumn<Map<String, Object>, String> colRequester = new TableColumn<>("Exporter / Importer");
        colRequester.setCellValueFactory(d -> new SimpleStringProperty(getRequesterDisplay(d.getValue())));
        colRequester.setStyle("-fx-font-size:12px; -fx-text-fill:#475569;");

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

        TableColumn<Map<String, Object>, String> colDest = new TableColumn<>("Destination");
        colDest.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "destinationPort", "Port of Singapore")));
        colDest.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "status", "Pending Allocation")));
        colStatus.setCellFactory(col -> createStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(160);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button allocBtn = new Button("Allocate");
            private final Button viewBtn = new Button("View");
            private final HBox btnBox = new HBox(6, allocBtn, viewBtn);

            {
                btnBox.setAlignment(Pos.CENTER);
                allocBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");
                viewBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");

                allocBtn.setOnAction(e -> {
                    Map<String, Object> req = getTableView().getItems().get(getIndex());
                    showAllocationDialog(req, null);
                });

                viewBtn.setOnAction(e -> {
                    Map<String, Object> req = getTableView().getItems().get(getIndex());
                    showAllocationDetailsDialog(req);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Map<String, Object> req = getTableView().getItems().get(getIndex());
                    int rem = getRequestRemainingUnits(req);
                    if (rem <= 0) {
                        allocBtn.setVisible(false);
                        allocBtn.setManaged(false);
                    } else {
                        allocBtn.setVisible(true);
                        allocBtn.setManaged(true);
                        allocBtn.setText("Allocate");
                    }
                    setGraphic(btnBox);
                }
            }
        });

        allocationRequestsTable.getColumns().addAll(
                colReqId, colCargo, colType, colReq, colAlloc, colRem,
                colRequester, colShip, colDest, colStatus, colAction
        );
        allocationRequestsTable.setItems(allocationRequestsList);

        allocationRequestsTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setPrefHeight(38);
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showAllocationDetailsDialog(row.getItem());
                }
            });
            return row;
        });

        container.getChildren().addAll(topBar, allocationRequestsTable);
        return container;
    }

    // =====================================================
    // 7. SECTION 4 — CONTAINER INVENTORY & YARD STOCK
    // =====================================================
    private VBox buildContainerInventorySection() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        // Controls Bar
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label tblTitle = new Label("CONTAINER INVENTORY & YARD STOCK");
        tblTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:15px; -fx-font-weight:bold;");
        Label tblSub = new Label("Active container fleet, stacking location and status at " + activeViewingPort);
        tblSub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(tblTitle, tblSub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("🔍 Search Container / Cargo / Ship / Yard...");
        searchField.setPrefWidth(240);
        searchField.setPrefHeight(34);
        searchField.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-padding:5 10;");
        searchField.textProperty().addListener((obs, ov, nv) -> applyFilters());

        typeFilterCombo = new ComboBox<>();
        typeFilterCombo.getItems().addAll("All Types", "20 ft Standard Dry", "40 ft Standard Dry", "40 ft High Cube", "20 ft Refrigerated (Reefer)", "40 ft Flat Rack", "20 ft Tank Container");
        typeFilterCombo.setValue("All Types");
        typeFilterCombo.setPrefHeight(34);
        typeFilterCombo.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-pref-width:140;");
        typeFilterCombo.setOnAction(e -> applyFilters());

        statusFilterCombo = new ComboBox<>();
        statusFilterCombo.getItems().addAll("All Status", "Available", "Allocated", "In Transit", "Received", "Loaded", "Delivered");
        statusFilterCombo.setValue("All Status");
        statusFilterCombo.setPrefHeight(34);
        statusFilterCombo.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-pref-width:125;");
        statusFilterCombo.setOnAction(e -> applyFilters());

        sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Sort: ID (Newest)", "Sort: Status", "Sort: Type", "Sort: Location");
        sortCombo.setValue("Sort: ID (Newest)");
        sortCombo.setPrefHeight(34);
        sortCombo.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-pref-width:135;");
        sortCombo.setOnAction(e -> applyFilters());

        Button addContainerBtn = new Button("+ Register Container");
        addContainerBtn.setPrefHeight(34);
        addContainerBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 14; -fx-cursor:hand;");
        addContainerBtn.setOnAction(e -> showAddContainerDialog());

        controls.getChildren().addAll(titleBox, sp, searchField, typeFilterCombo, statusFilterCombo, sortCombo, addContainerBtn);

        // Table
        inventoryTable = new TableView<>();
        inventoryTable.setPrefHeight(280);
        inventoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
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
        colAction.setPrefWidth(100);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");

            {
                viewBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 12; -fx-cursor:hand;");
                viewBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    showContainerDetailsDialog(item);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    setGraphic(viewBtn);
                    setAlignment(Pos.CENTER);
                }
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
    // 8. SECTION 5 & 6 — AVAILABLE CONTAINERS & ACTIVE ALLOCATIONS SPLIT
    // =====================================================
    private HBox buildAvailableAndAllocationsSplit() {
        HBox split = new HBox(16);
        split.setAlignment(Pos.TOP_LEFT);

        // LEFT: Available Containers Ready to Allocate (45% Width)
        VBox leftBox = new VBox(12);
        leftBox.setPadding(new Insets(16));
        leftBox.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");
        leftBox.setPrefWidth(490);
        leftBox.setMinWidth(430);

        HBox lTop = new HBox(8);
        lTop.setAlignment(Pos.CENTER_LEFT);
        Label lIcon = new Label("🟢");
        Label lTitle = new Label("AVAILABLE CONTAINERS");
        lTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14px; -fx-font-weight:bold;");
        Label lSub = new Label("• In yard storage at " + activeViewingPort);
        lSub.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px;");
        lTop.getChildren().addAll(lIcon, lTitle, lSub);

        // Empty Banner with contextual shortage clarification
        availableEmptyBanner = new VBox(4);
        availableEmptyBanner.setPadding(new Insets(10, 12, 10, 12));
        availableEmptyBanner.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:6; -fx-background-radius:6;");
        availableEmptyBanner.setVisible(false);
        availableEmptyBanner.setManaged(false);

        availableTable = new TableView<>();
        availableTable.setPrefHeight(250);
        availableTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        availableTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        availableTable.setPlaceholder(new Label("No containers currently available for allocation"));

        TableColumn<Map<String, Object>, String> colAvId = new TableColumn<>("Container ID");
        colAvId.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "containerNo", getString(d.getValue(), "bookingId", "CONT-N/A"))));
        colAvId.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0284c7;");

        TableColumn<Map<String, Object>, String> colAvType = new TableColumn<>("Type / Size");
        colAvType.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "containerType", "20 ft Standard Dry")));
        colAvType.setStyle("-fx-font-size:11px; -fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colAvLoc = new TableColumn<>("Yard Bay");
        colAvLoc.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "location", "Yard Bay A-01")));
        colAvLoc.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colAvAction = new TableColumn<>("Action");
        colAvAction.setPrefWidth(90);
        colAvAction.setCellFactory(col -> new TableCell<>() {
            private final Button qAllocBtn = new Button("Assign");
            {
                qAllocBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");
                qAllocBtn.setOnAction(e -> {
                    Map<String, Object> cont = getTableView().getItems().get(getIndex());
                    showAllocationDialog(null, cont);
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(qAllocBtn);
                setAlignment(Pos.CENTER);
            }
        });

        availableTable.getColumns().addAll(colAvId, colAvType, colAvLoc, colAvAction);
        availableTable.setItems(availableContainersList);
        leftBox.getChildren().addAll(lTop, availableEmptyBanner, availableTable);

        // RIGHT: Active Allocations Table (55% Width)
        VBox rightBox = new VBox(12);
        rightBox.setPadding(new Insets(16));
        rightBox.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        HBox rTop = new HBox(8);
        rTop.setAlignment(Pos.CENTER_LEFT);
        Label rIcon = new Label("📑");
        Label rTitle = new Label("ACTIVE & RECENT ALLOCATIONS");
        rTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14px; -fx-font-weight:bold;");
        Label rSub = new Label("• Deployed containers linked to active manifests at " + activeViewingPort);
        rSub.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px;");
        rTop.getChildren().addAll(rIcon, rTitle, rSub);

        activeAllocationsTable = new TableView<>();
        activeAllocationsTable.setPrefHeight(250);
        activeAllocationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        activeAllocationsTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        activeAllocationsTable.setPlaceholder(new Label("No active container allocations recorded for " + activeViewingPort));

        TableColumn<Map<String, Object>, String> colActId = new TableColumn<>("Allocation ID");
        colActId.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "bookingId", "ALC-N/A")));
        colActId.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0284c7;");

        TableColumn<Map<String, Object>, String> colActCont = new TableColumn<>("Container");
        colActCont.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "containerNo", getString(d.getValue(), "bookingId", "CONT-N/A"))));
        colActCont.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colActCargo = new TableColumn<>("Cargo");
        colActCargo.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "cargoName", "Commercial Goods")));
        colActCargo.setStyle("-fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colActShip = new TableColumn<>("Ship / Voyage");
        colActShip.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "shipName", "MV " + activeViewingPort.replace("Port of ", "") + " Star")));
        colActShip.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#2563eb; -fx-font-weight:bold;");

        TableColumn<Map<String, Object>, String> colActStatus = new TableColumn<>("Status");
        colActStatus.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "status", "Allocated")));
        colActStatus.setCellFactory(col -> createStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colActAction = new TableColumn<>("Action");
        colActAction.setPrefWidth(140);
        colActAction.setCellFactory(col -> new TableCell<>() {
            private final Button vBtn = new Button("View");
            private final Button rBtn = new Button("Reallocate");
            private final HBox bBox = new HBox(4, vBtn, rBtn);
            {
                bBox.setAlignment(Pos.CENTER);
                vBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 6; -fx-cursor:hand;");
                rBtn.setStyle("-fx-background-color:#475569; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 6; -fx-cursor:hand;");

                vBtn.setOnAction(e -> {
                    Map<String, Object> alc = getTableView().getItems().get(getIndex());
                    showAllocationDetailsDialog(alc);
                });

                rBtn.setOnAction(e -> {
                    Map<String, Object> alc = getTableView().getItems().get(getIndex());
                    showReallocationDialog(alc);
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(bBox);
            }
        });

        activeAllocationsTable.getColumns().addAll(colActId, colActCont, colActCargo, colActShip, colActStatus, colActAction);
        activeAllocationsTable.setItems(activeAllocationsList);
        rightBox.getChildren().addAll(rTop, activeAllocationsTable);

        split.getChildren().addAll(leftBox, rightBox);
        return split;
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

                    if ("Pending".equalsIgnoreCase(item) || "Pending Allocation".equalsIgnoreCase(item) || "Pending Approval".equalsIgnoreCase(item) || "Booked".equalsIgnoreCase(item)) {
                        color = "#d97706";
                        bg = "#fef3c7";
                    } else if ("Partially Allocated".equalsIgnoreCase(item)) {
                        color = "#0284c7";
                        bg = "#e0f2fe";
                    } else if ("Available".equalsIgnoreCase(item) || "Completed".equalsIgnoreCase(item) || "Approved".equalsIgnoreCase(item) || "Delivered".equalsIgnoreCase(item)) {
                        color = "#16a34a";
                        bg = "#dcfce7";
                    } else if ("Allocated".equalsIgnoreCase(item) || "Loaded".equalsIgnoreCase(item) || "Allocation Completed".equalsIgnoreCase(item)) {
                        color = "#0891b2";
                        bg = "#cffafe";
                    } else if ("In Transit".equalsIgnoreCase(item)) {
                        color = "#2563eb";
                        bg = "#dbeafe";
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
    // 9. DATA LOADING, FILTERING & LIVE SYNCHRONIZATION
    // =====================================================
    private void connectLiveServices() {
        refreshContainerData();

        ContainerDao.addChangeListener(this::refreshContainerData);
        CargoDao.addChangeListener(this::refreshContainerData);

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
                matchedContainers.add(new HashMap<>(b));
            }
        }

        // Incorporate cargos requiring containers
        List<Map<String, Object>> cargos = cargoDao.getAllCargos();
        List<Map<String, Object>> matchedCargos = new ArrayList<>();

        for (Map<String, Object> c : cargos) {
            if (isCargoAssociatedWithPort(c, activeViewingPort)) {
                matchedCargos.add(new HashMap<>(c));
            }
        }

        masterContainersList.clear();
        masterContainersList.addAll(matchedContainers);

        masterCargosList.clear();
        masterCargosList.addAll(matchedCargos);

        populateUnifiedTables();
    }

    private void populateUnifiedTables() {
        List<Map<String, Object>> shipperRequests = new ArrayList<>();
        List<Map<String, Object>> cargoAllocationRequests = new ArrayList<>();
        List<Map<String, Object>> availList = new ArrayList<>();
        List<Map<String, Object>> allocList = new ArrayList<>();

        // 1. Separate fleet items by status
        for (Map<String, Object> c : masterContainersList) {
            String st = getString(c, "status", "");
            String bId = getString(c, "bookingId", "");

            if (bId.startsWith("CONT-") || bId.startsWith("REQ-") || "Booked".equalsIgnoreCase(st) || "Pending".equalsIgnoreCase(st) || "Pending Approval".equalsIgnoreCase(st)) {
                shipperRequests.add(c);
            }

            if ("Pending".equalsIgnoreCase(st) || "Booked".equalsIgnoreCase(st) || "Pending Approval".equalsIgnoreCase(st) || "Pending Allocation".equalsIgnoreCase(st)) {
                cargoAllocationRequests.add(new HashMap<>(c));
            } else if ("Available".equalsIgnoreCase(st)) {
                availList.add(new HashMap<>(c));
            } else if ("Allocated".equalsIgnoreCase(st) || "In Transit".equalsIgnoreCase(st) || "Loaded".equalsIgnoreCase(st)) {
                allocList.add(new HashMap<>(c));
            }
        }

        // 2. Incorporate cargo demands requiring container allocation
        for (Map<String, Object> cg : masterCargosList) {
            String cType = getString(cg, "cargoType", "");
            String cSt = getString(cg, "status", "");
            if (cType.toLowerCase().contains("container") || cType.toLowerCase().contains("general") || cType.toLowerCase().contains("machinery")) {
                String cgId = (String) cg.get("cargoId");
                boolean alreadyInAllocation = false;
                for (Map<String, Object> r : cargoAllocationRequests) {
                    if (cgId != null && cgId.equalsIgnoreCase(getString(r, "cargoId", ""))) {
                        alreadyInAllocation = true;
                        break;
                    }
                }
                if (!alreadyInAllocation) {
                    Map<String, Object> synthReq = new HashMap<>(cg);
                    int hash = cgId != null ? Math.abs(cgId.hashCode()) : Math.abs(cg.hashCode());
                    synthReq.put("bookingId", "REQ-" + (hash % 8999 + 1000));
                    synthReq.put("containerType", "20 ft Standard Dry");
                    synthReq.put("shipName", "MV " + activeViewingPort.replace("Port of ", "") + " Star");
                    cargoAllocationRequests.add(synthReq);
                }
            }
        }

        // 3. Synchronize each cargo allocation request with live allocations
        for (Map<String, Object> req : cargoAllocationRequests) {
            int matchingAllocations = 0;
            for (Map<String, Object> a : allocList) {
                if (matchesAllocation(a, req)) {
                    matchingAllocations++;
                }
            }
            int required = getRequestRequiredUnits(req);
            int allocated = Math.min(matchingAllocations, required);
            int remaining = Math.max(required - allocated, 0);

            req.put("requiredUnits", required);
            req.put("allocatedUnits", allocated);
            req.put("remainingUnits", remaining);

            if (allocated == 0) {
                req.put("status", "Pending Allocation");
            } else if (remaining == 0) {
                req.put("status", "Allocated");
            } else {
                req.put("status", "Partially Allocated");
            }
        }

        // 4. Filter out allocated containers from available list
        List<Map<String, Object>> trulyAvail = new ArrayList<>();
        for (Map<String, Object> av : availList) {
            String avId = getString(av, "containerNo", getString(av, "bookingId", ""));
            boolean isAllocated = false;
            for (Map<String, Object> al : allocList) {
                String alCont = getString(al, "containerNo", getString(al, "bookingId", ""));
                if (!avId.isEmpty() && avId.equalsIgnoreCase(alCont)) {
                    isAllocated = true;
                    break;
                }
            }
            if (!isAllocated) {
                trulyAvail.add(av);
            }
        }

        containerRequestsList.setAll(shipperRequests);
        allocationRequestsList.setAll(cargoAllocationRequests);
        availableContainersList.setAll(trulyAvail);
        activeAllocationsList.setAll(allocList);

        // Update KPI calculations with required units & shortage
        updateKpiCounts(trulyAvail.size(), allocList.size(), cargoAllocationRequests);

        applyFilters();
    }

    private boolean matchesAllocation(Map<String, Object> allocRecord, Map<String, Object> req) {
        if (allocRecord == null || req == null) return false;
        String aCargoId = getString(allocRecord, "cargoId", "");
        String aBookingId = getString(allocRecord, "bookingId", "");
        String aContNo = getString(allocRecord, "containerNo", "");

        String rBookingId = getString(req, "bookingId", "");
        String rCargoId = getString(req, "cargoId", "");

        if (!rBookingId.isEmpty() && (rBookingId.equalsIgnoreCase(aCargoId) || rBookingId.equalsIgnoreCase(aBookingId))) {
            return true;
        }
        if (!rCargoId.isEmpty() && (rCargoId.equalsIgnoreCase(aCargoId) || rCargoId.equalsIgnoreCase(aBookingId))) {
            return true;
        }
        return false;
    }

    private void updateKpiCounts(int availUnits, int allocUnits, List<Map<String, Object>> allocationRequests) {
        int totalUnits = availUnits + allocUnits;
        int inTransitUnits = 0;
        for (Map<String, Object> c : masterContainersList) {
            String st = getString(c, "status", "");
            if ("In Transit".equalsIgnoreCase(st)) inTransitUnits++;
        }

        int pendingRequestsCount = 0;
        int totalRequiredUnits = 0;

        for (Map<String, Object> req : allocationRequests) {
            int rem = getRequestRemainingUnits(req);
            if (rem > 0) {
                pendingRequestsCount++;
                totalRequiredUnits += rem;
            }
        }

        // SHORTAGE = MAX(REQUIRED CONTAINERS - AVAILABLE CONTAINERS, 0)
        int shortage = Math.max(totalRequiredUnits - availUnits, 0);

        if (totalContainersVal != null) totalContainersVal.setText(String.valueOf(totalUnits));
        if (availableVal != null) availableVal.setText(String.valueOf(availUnits));
        if (allocatedVal != null) allocatedVal.setText(String.valueOf(allocUnits));
        if (inTransitVal != null) inTransitVal.setText(String.valueOf(inTransitUnits));
        if (pendingRequestsVal != null) pendingRequestsVal.setText(String.valueOf(pendingRequestsCount));
        if (requiredVal != null) requiredVal.setText(String.valueOf(totalRequiredUnits));
        if (shortageVal != null) shortageVal.setText(String.valueOf(shortage));

        if (shortageSubLabel != null) {
            if (shortage > 0) {
                shortageVal.setStyle("-fx-text-fill:#ef4444; -fx-font-size:24px; -fx-font-weight:bold;");
                shortageSubLabel.setText("Deficit of " + shortage + " units");
                shortageSubLabel.setStyle("-fx-text-fill:#ef4444; -fx-font-size:10px; -fx-font-weight:bold;");
            } else {
                shortageVal.setStyle("-fx-text-fill:#16a34a; -fx-font-size:24px; -fx-font-weight:bold;");
                shortageSubLabel.setText("Demand covered (0 deficit)");
                shortageSubLabel.setStyle("-fx-text-fill:#16a34a; -fx-font-size:10px;");
            }
        }

        // Update Available Empty Banner
        if (availableEmptyBanner != null) {
            availableEmptyBanner.getChildren().clear();
            if (availUnits == 0) {
                availableEmptyBanner.setVisible(true);
                availableEmptyBanner.setManaged(true);

                HBox bRow = new HBox(8);
                bRow.setAlignment(Pos.CENTER_LEFT);

                Label statusChip = new Label("Available: 0 • Required: " + totalRequiredUnits + " • Shortage: " + shortage);
                if (shortage > 0) {
                    statusChip.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#ef4444; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;");
                } else {
                    statusChip.setStyle("-fx-background-color:#ecfdf5; -fx-text-fill:#16a34a; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;");
                }

                Label explanation = new Label(totalUnits == 0
                        ? "No containers registered in yard fleet for " + activeViewingPort + "."
                        : (shortage == 0
                            ? "All fleet containers are deployed (" + allocUnits + " allocated, " + inTransitUnits + " in transit). Current demand is 0."
                            : "All fleet units are allocated or in transit. Unit deficit of " + shortage + " required by pending requests."));
                explanation.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px;");

                availableEmptyBanner.getChildren().addAll(statusChip, explanation);
            } else {
                availableEmptyBanner.setVisible(false);
                availableEmptyBanner.setManaged(false);
            }
        }
    }

    private int getRequestRequiredUnits(Map<String, Object> req) {
        if (req == null) return 1;
        if (req.containsKey("requiredUnits")) {
            return parseQuantity(req.get("requiredUnits"), 1);
        }
        return parseQuantity(req.get("quantity"), 1);
    }

    private int getRequestAllocatedUnits(Map<String, Object> req) {
        if (req == null) return 0;
        if (req.containsKey("allocatedUnits")) {
            return parseQuantity(req.get("allocatedUnits"), 0);
        }
        String st = getString(req, "status", "");
        if ("Allocated".equalsIgnoreCase(st) || "Loaded".equalsIgnoreCase(st)) {
            return getRequestRequiredUnits(req);
        }
        return 0;
    }

    private int getRequestRemainingUnits(Map<String, Object> req) {
        if (req == null) return 0;
        if (req.containsKey("remainingUnits")) {
            return parseQuantity(req.get("remainingUnits"), 0);
        }
        int reqUnits = getRequestRequiredUnits(req);
        int allocUnits = getRequestAllocatedUnits(req);
        return Math.max(reqUnits - allocUnits, 0);
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

    private void applyFilters() {
        String query = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String typeFilter = typeFilterCombo != null && typeFilterCombo.getValue() != null ? typeFilterCombo.getValue() : "All Types";
        String statusFilter = statusFilterCombo != null && statusFilterCombo.getValue() != null ? statusFilterCombo.getValue() : "All Status";
        String sortOption = sortCombo != null && sortCombo.getValue() != null ? sortCombo.getValue() : "Sort: ID (Newest)";

        List<Map<String, Object>> filteredInventory = new ArrayList<>();

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
        }

        // Sorting
        if ("Sort: Status".equalsIgnoreCase(sortOption)) {
            filteredInventory.sort(Comparator.comparing(a -> getString(a, "status", "")));
        } else if ("Sort: Type".equalsIgnoreCase(sortOption)) {
            filteredInventory.sort(Comparator.comparing(a -> getString(a, "containerType", "")));
        } else if ("Sort: Location".equalsIgnoreCase(sortOption)) {
            filteredInventory.sort(Comparator.comparing(a -> getString(a, "location", "")));
        }

        containerInventoryList.setAll(filteredInventory);
    }

    // =====================================================
    // 10. WORKFLOW DIALOGS & ALLOCATION CONFIRMATION
    // =====================================================
    private void showAllocationDialog(Map<String, Object> preselectedRequest, Map<String, Object> preselectedContainer) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Allocate Container — " + activeViewingPort);
        dialog.setHeaderText("Assign available yard container to cargo request for " + activeViewingPort);

        ButtonType allocateBtnType = new ButtonType("Confirm Allocation", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(allocateBtnType, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.setPadding(new Insets(16));

        // 1. Cargo Request Selector
        ComboBox<String> cargoCombo = new ComboBox<>();
        cargoCombo.setPrefWidth(350);
        List<String> cargoItems = new ArrayList<>();
        Map<String, Map<String, Object>> requestMap = new HashMap<>();

        for (Map<String, Object> r : allocationRequestsList) {
            String rId = getString(r, "bookingId", getString(r, "cargoId", "REQ"));
            String cName = getString(r, "cargoName", "Cargo");
            int rem = getRequestRemainingUnits(r);
            if (rem > 0 || (preselectedRequest != null && r.equals(preselectedRequest))) {
                String label = rId + " — " + cName + " (Remaining Demand: " + rem + ")";
                cargoItems.add(label);
                requestMap.put(label, r);
            }
        }
        cargoCombo.getItems().addAll(cargoItems);
        if (preselectedRequest != null) {
            String rId = getString(preselectedRequest, "bookingId", getString(preselectedRequest, "cargoId", "REQ"));
            for (String item : cargoItems) {
                if (item.startsWith(rId)) {
                    cargoCombo.setValue(item);
                    break;
                }
            }
        }
        if (cargoCombo.getValue() == null && !cargoItems.isEmpty()) {
            cargoCombo.setValue(cargoItems.get(0));
        }

        // 2. Available Container Selector
        ComboBox<String> contCombo = new ComboBox<>();
        contCombo.setPrefWidth(350);
        List<String> contItems = new ArrayList<>();
        for (Map<String, Object> av : availableContainersList) {
            String cNo = getString(av, "containerNo", getString(av, "bookingId", "CONT"));
            String cType = getString(av, "containerType", "20 ft Standard Dry");
            contItems.add(cNo + " (" + cType + ")");
        }
        if (contItems.isEmpty()) {
            contItems.add("MSKU-748921 (20 ft Standard Dry)");
        }
        contCombo.getItems().addAll(contItems);
        if (preselectedContainer != null) {
            String cNo = getString(preselectedContainer, "containerNo", getString(preselectedContainer, "bookingId", "CONT"));
            for (String item : contItems) {
                if (item.startsWith(cNo)) {
                    contCombo.setValue(item);
                    break;
                }
            }
        }
        if (contCombo.getValue() == null && !contItems.isEmpty()) {
            contCombo.setValue(contItems.get(0));
        }

        TextField shipField = new TextField("MV " + activeViewingPort.replace("Port of ", "") + " Express");
        shipField.setPrefWidth(350);
        TextField destField = new TextField("Port of Singapore");
        destField.setPrefWidth(350);
        TextField yardField = new TextField("Yard Staging Bay S-04 • " + activeViewingPort);
        yardField.setPrefWidth(350);

        form.add(new Label("Select Cargo Request:"), 0, 0);
        form.add(cargoCombo, 1, 0);
        form.add(new Label("Select Available Container:"), 0, 1);
        form.add(contCombo, 1, 1);
        form.add(new Label("Assigned Vessel / Voyage:"), 0, 2);
        form.add(shipField, 1, 2);
        form.add(new Label("Destination Port:"), 0, 3);
        form.add(destField, 1, 3);
        form.add(new Label("Staging Yard Location:"), 0, 4);
        form.add(yardField, 1, 4);

        dialog.getDialogPane().setContent(form);

        Button allocBtn = (Button) dialog.getDialogPane().lookupButton(allocateBtnType);
        allocBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-weight:bold;");

        // Use custom result converter to avoid premature / empty dialog issues
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == allocateBtnType) {
                return allocateBtnType;
            }
            return null;
        });

        java.util.Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == allocateBtnType) {
            String selectedCargoStr = cargoCombo.getValue();
            String selectedContStr = contCombo.getValue();

            if (selectedCargoStr == null || selectedContStr == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection Required");
                alert.setHeaderText("Missing Allocation Selection");
                alert.setContentText("Please select both a cargo request and an available container.");
                alert.showAndWait();
                return;
            }

            Map<String, Object> targetReq = requestMap.get(selectedCargoStr);
            if (targetReq == null && preselectedRequest != null) {
                targetReq = preselectedRequest;
            }

            String cNo = selectedContStr.split(" ")[0].trim();
            String rId = targetReq != null ? getString(targetReq, "bookingId", getString(targetReq, "cargoId", "REQ")) : selectedCargoStr.split(" ")[0].trim();
            String cId = targetReq != null ? getString(targetReq, "cargoId", rId) : rId;
            String cName = targetReq != null ? getString(targetReq, "cargoName", "Commercial Cargo") : selectedCargoStr;
            String ship = shipField.getText().trim();
            String dest = destField.getText().trim();
            String exporterName = targetReq != null ? getRequesterDisplay(targetReq) : "Global Logistics Ltd.";
            String customerType = targetReq != null ? getString(targetReq, "customerType", "Exporter") : "Exporter";

            // 1. Update existing container booking in ContainerDao without creating duplicate records
            String orig = targetReq != null ? getString(targetReq, "originPort", "—") : "—";
            String finalDest = targetReq != null ? getString(targetReq, "destinationPort", dest) : dest;

            if (targetReq != null) {
                targetReq.put("status", "Allocated");
                targetReq.put("containerNumber", cNo);
                targetReq.put("assignedContainer", cNo);
                targetReq.put("assignedShip", ship);
                targetReq.put("originPort", orig);
                targetReq.put("destinationPort", finalDest);
            }

            containerDao.saveBooking(rId, cId, cName, "20 ft Standard Dry", "1", activeViewingPort, orig, finalDest, 0.0, "Allocated", customerType, exporterName);

            // 2. Update persistent Cargo status in CargoDao
            if (cId != null && !cId.isBlank()) {
                int rem = targetReq != null ? getRequestRemainingUnits(targetReq) : 1;
                if (rem <= 1) {
                    cargoDao.updateCargoStatus(cId, "Allocated");
                } else {
                    cargoDao.updateCargoStatus(cId, "Partially Allocated");
                }
            }

            // 3. Refresh allocation UI data immediately
            refreshContainerData();

            // 4. Show rich, populated Allocation Confirmed popup
            showAllocationConfirmedPopup(rId, cNo, cName, rId, exporterName, customerType, ship, finalDest, "Allocated");
        }
    }

    private void showAllocationConfirmedPopup(String alcId, String cNo, String cName, String rId,
                                              String exporterName, String customerType, String ship, String dest, String status) {
        Dialog<Void> popup = new Dialog<>();
        popup.setTitle("Allocation Confirmed");
        popup.setHeaderText(null);

        ButtonType okBtnType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        popup.getDialogPane().getButtonTypes().add(okBtnType);

        VBox rootBox = new VBox(14);
        rootBox.setPadding(new Insets(18, 20, 18, 20));
        rootBox.setPrefWidth(460);
        rootBox.setStyle("-fx-background-color:#ffffff;");

        // Top Success Header
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label checkIcon = new Label("✓");
        checkIcon.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#16a34a; -fx-background-color:#dcfce7; -fx-background-radius:999; -fx-padding:6 12;");

        VBox headerText = new VBox(3);
        Label mainTitle = new Label("Allocation Confirmed");
        mainTitle.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
        Label subTitle = new Label("Container allocation completed successfully.");
        subTitle.setStyle("-fx-font-size:13px; -fx-text-fill:#16a34a; -fx-font-weight:bold;");
        headerText.getChildren().addAll(mainTitle, subTitle);

        headerBox.getChildren().addAll(checkIcon, headerText);

        // Details Container Box
        VBox detailsBox = new VBox(8);
        detailsBox.setPadding(new Insets(14));
        detailsBox.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        detailsBox.getChildren().addAll(
                detailLine("Allocation ID", alcId),
                detailLine("Container", cNo),
                detailLine("Cargo", cName),
                detailLine("Request ID", rId),
                detailLine(customerType != null ? customerType : "Exporter / Importer", exporterName),
                detailLine("Ship / Voyage", ship),
                detailLine("Destination", dest),
                detailLine("Status", status)
        );

        rootBox.getChildren().addAll(headerBox, detailsBox);
        popup.getDialogPane().setContent(rootBox);

        Button okBtn = (Button) popup.getDialogPane().lookupButton(okBtnType);
        if (okBtn != null) {
            okBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 20; -fx-cursor:hand;");
        }

        popup.showAndWait();
    }

    private void showReallocationDialog(Map<String, Object> allocation) {
        if (allocation == null) return;
        Dialog<ButtonType> dialog = new Dialog<>();
        String cNo = getString(allocation, "containerNo", getString(allocation, "bookingId", "CONT"));
        dialog.setTitle("Reallocate Container — " + cNo);
        dialog.setHeaderText("Change cargo or voyage assignment for container " + cNo);

        ButtonType reallocBtnType = new ButtonType("Save Reallocation", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reallocBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField contField = new TextField(cNo);
        contField.setEditable(false);
        contField.setStyle("-fx-background-color:#f1f5f9;");

        TextField currentCargoField = new TextField(getString(allocation, "cargoName", "Commercial Goods"));
        currentCargoField.setEditable(false);
        currentCargoField.setStyle("-fx-background-color:#f1f5f9;");

        ComboBox<String> newCargoCombo = new ComboBox<>();
        for (Map<String, Object> c : masterCargosList) {
            String cId = getString(c, "cargoId", "");
            String cName = getString(c, "cargoName", "Commercial Goods");
            if (!cId.isEmpty()) {
                newCargoCombo.getItems().add(cId + " — " + cName);
            }
        }
        if (!newCargoCombo.getItems().isEmpty()) {
            newCargoCombo.setValue(newCargoCombo.getItems().get(0));
        }

        TextField shipField = new TextField(getString(allocation, "shipName", getString(allocation, "assignedShip", "")));

        grid.add(new Label("Container:"), 0, 0);
        grid.add(contField, 1, 0);
        grid.add(new Label("Current Assignment:"), 0, 1);
        grid.add(currentCargoField, 1, 1);
        grid.add(new Label("Reassign to Cargo:"), 0, 2);
        grid.add(newCargoCombo, 1, 2);
        grid.add(new Label("Updated Ship / Voyage:"), 0, 3);
        grid.add(shipField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Button reallocBtn = (Button) dialog.getDialogPane().lookupButton(reallocBtnType);
        reallocBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-weight:bold;");

        reallocBtn.setOnAction(event -> {
            String newCargo = newCargoCombo.getValue();
            allocation.put("cargoName", newCargo);
            allocation.put("shipName", shipField.getText().trim());
            allocation.put("status", "Allocated");

            containerDao.saveBooking(cNo, "CGO-REALLOC", newCargo, "20 ft Standard Dry", "1", activeViewingPort, activeViewingPort, "Port of Singapore", "Allocated");

            refreshContainerData();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reallocated");
            alert.setHeaderText("Container Reallocated Successfully");
            alert.setContentText("Container " + cNo + " reassigned to " + newCargo + ".");
            alert.showAndWait();
        });

        dialog.showAndWait();
    }

    private void showAllocationDetailsDialog(Map<String, Object> item) {
        if (item == null) return;

        Dialog<Void> dialog = new Dialog<>();
        String id = getString(item, "bookingId", getString(item, "cargoId", "ALC-N/A"));
        dialog.setTitle("Allocation Particulars & Manifest — " + id);
        dialog.setHeaderText("Complete container allocation record, live voyage tracking and cargo demand for " + id);

        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeBtn);

        VBox contentBox = new VBox(12);
        contentBox.setPadding(new Insets(16));
        contentBox.setPrefWidth(480);

        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);
        Label ic = new Label("📦");
        ic.setStyle("-fx-font-size:24px; -fx-padding:4;");
        VBox hTxt = new VBox(2);
        Label t1 = new Label("Allocation Record: " + id);
        t1.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
        Label t2 = new Label("Operating Port: " + activeViewingPort);
        t2.setStyle("-fx-font-size:12px; -fx-text-fill:#64748b;");
        hTxt.getChildren().addAll(t1, t2);
        head.getChildren().addAll(ic, hTxt);

        VBox g1Box = new VBox(4,
                detailLine("Allocated Container ID", getString(item, "containerNo", getString(item, "bookingId", "CONT-N/A"))),
                detailLine("Container Type & Spec", getString(item, "containerType", "20 ft Standard Dry")),
                detailLine("Associated Cargo Manifest", getString(item, "cargoName", "Commercial Goods")),
                detailLine("Cargo Quantity Required", String.valueOf(getRequestRequiredUnits(item))),
                detailLine("Allocated Units", String.valueOf(getRequestAllocatedUnits(item))),
                detailLine("Remaining Demand", String.valueOf(getRequestRemainingUnits(item))),
                detailLine("Exporter / Importer", getRequesterDisplay(item)),
                detailLine("Assigned Vessel", getString(item, "shipName", "MV " + activeViewingPort.replace("Port of ", "") + " Star")),
                detailLine("Destination Port", getString(item, "destinationPort", "Port of Singapore")),
                detailLine("Allocation Status", getString(item, "status", "Allocated"))
        );

        contentBox.getChildren().addAll(head, g1Box);
        dialog.getDialogPane().setContent(contentBox);
        dialog.showAndWait();
    }

    private void showAddContainerDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register New Container — " + activeViewingPort);
        dialog.setHeaderText("Enter container specifications for yard registration at " + activeViewingPort);

        ButtonType regBtnType = new ButtonType("Register Container", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(regBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField contIdField = new TextField("MSKU-" + (Math.abs(System.currentTimeMillis()) % 899999 + 100000));
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("20 ft Standard Dry", "40 ft Standard Dry", "40 ft High Cube", "20 ft Refrigerated (Reefer)", "40 ft Flat Rack", "20 ft Tank Container");
        typeCombo.setValue("20 ft Standard Dry");

        TextField cargoField = new TextField("Empty / Unassigned Yard Stock");
        TextField yardLocField = new TextField("Yard Bay A-0" + ((System.currentTimeMillis() % 8) + 1) + " • " + activeViewingPort);
        TextField tareField = new TextField("2,200 kg (Tare) / 28,000 kg (Payload)");

        grid.add(new Label("Container ISO Number:"), 0, 0);
        grid.add(contIdField, 1, 0);
        grid.add(new Label("Container Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Initial Cargo Content:"), 0, 2);
        grid.add(cargoField, 1, 2);
        grid.add(new Label("Yard Stacking Location:"), 0, 3);
        grid.add(yardLocField, 1, 3);
        grid.add(new Label("Tare Weight / Specs:"), 0, 4);
        grid.add(tareField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Button regBtn = (Button) dialog.getDialogPane().lookupButton(regBtnType);
        regBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-weight:bold;");

        regBtn.setOnAction(event -> {
            String cNo = contIdField.getText().trim();
            if (cNo.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Input");
                alert.setContentText("Container ISO Number is required.");
                alert.showAndWait();
                event.consume();
                return;
            }

            containerDao.saveBooking(cNo, "CGO-YARD-STOCK", cargoField.getText().trim(), typeCombo.getValue(), "1", activeViewingPort, activeViewingPort, "Port of Singapore", "Available");

            refreshContainerData();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText("Container Added to Yard Fleet");
            alert.setContentText("Container " + cNo + " registered and available in " + yardLocField.getText() + ".");
            alert.showAndWait();
        });

        dialog.showAndWait();
    }

    private void handleApproveBooking(Map<String, Object> item) {
        if (item == null) return;
        String bId = getString(item, "bookingId", "REQ");
        item.put("status", "Approved");
        containerDao.saveBooking(bId, getString(item, "cargoId", "CGO"), getString(item, "cargoName", "Goods"),
                getString(item, "containerType", "20 ft Dry"), getString(item, "quantity", "1"),
                activeViewingPort, activeViewingPort, getString(item, "destinationPort", "Port of Singapore"),
                0.0, "Approved", getString(item, "customerType", "Exporter"), getRequesterDisplay(item));
        refreshContainerData();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Approved");
        alert.setHeaderText("Container Request Approved");
        alert.setContentText("Booking " + bId + " has been approved for quayside loading.");
        alert.showAndWait();
    }

    private void handleRejectBooking(Map<String, Object> item) {
        if (item == null) return;
        String bId = getString(item, "bookingId", "REQ");
        item.put("status", "Rejected");
        containerDao.saveBooking(bId, getString(item, "cargoId", "CGO"), getString(item, "cargoName", "Goods"),
                getString(item, "containerType", "20 ft Dry"), getString(item, "quantity", "1"),
                activeViewingPort, activeViewingPort, getString(item, "destinationPort", "Port of Singapore"),
                0.0, "Rejected", getString(item, "customerType", "Exporter"), getRequesterDisplay(item));
        refreshContainerData();

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Booking Rejected");
        alert.setHeaderText("Container Request Rejected");
        alert.setContentText("Booking " + bId + " has been rejected.");
        alert.showAndWait();
    }

    private void showContainerDetailsDialog(Map<String, Object> item) {
        if (item == null) return;

        Dialog<Void> dialog = new Dialog<>();
        String id = getString(item, "containerNo", getString(item, "bookingId", "CONT-N/A"));
        dialog.setTitle("Container Particulars & Telemetry — " + id);
        dialog.setHeaderText("Comprehensive yard telemetry, booking manifest and status for container " + id);

        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeBtn);

        VBox contentBox = new VBox(12);
        contentBox.setPadding(new Insets(16));
        contentBox.setPrefWidth(480);

        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);
        Label ic = new Label("📦");
        ic.setStyle("-fx-font-size:24px; -fx-padding:4;");
        VBox hTxt = new VBox(2);
        Label t1 = new Label("Container ISO: " + id);
        t1.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
        Label t2 = new Label("Operating Port: " + activeViewingPort);
        t2.setStyle("-fx-font-size:12px; -fx-text-fill:#64748b;");
        hTxt.getChildren().addAll(t1, t2);
        head.getChildren().addAll(ic, hTxt);

        VBox g1Box = new VBox(4,
                detailLine("Booking / Request ID", getString(item, "bookingId", "N/A")),
                detailLine("Container Type", getString(item, "containerType", "20 ft Standard Dry")),
                detailLine("Manifest Cargo Name", getString(item, "cargoName", "Commercial Goods")),
                detailLine("Cargo ID", getString(item, "cargoId", "CGO-1024")),
                detailLine(getString(item, "customerType", "Exporter / Importer"), getRequesterDisplay(item)),
                detailLine("Assigned Vessel", getString(item, "shipName", "MV " + activeViewingPort.replace("Port of ", "") + " Star")),
                detailLine("Destination Port", getString(item, "destinationPort", "Port of Singapore")),
                detailLine("Current Yard Bay", getString(item, "location", "Yard Bay A-01 • " + activeViewingPort)),
                detailLine("Current Operational Status", getString(item, "status", "Available"))
        );

        contentBox.getChildren().addAll(head, g1Box);
        dialog.getDialogPane().setContent(contentBox);
        dialog.showAndWait();
    }

    private HBox detailLine(String label, String value) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px; -fx-pref-width:180px;");
        Label val = new Label(value != null ? value : "—");
        val.setStyle("-fx-text-fill:#0f172a; -fx-font-size:12px; -fx-font-weight:bold;");
        row.getChildren().addAll(lbl, val);
        return row;
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

    private String getString(Map<String, Object> map, String key, String fallback) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) return fallback;
        return map.get(key).toString();
    }

    private int parseQuantity(Object qtyObj, int fallback) {
        if (qtyObj == null) return fallback;
        String str = qtyObj.toString().trim();
        String digits = str.replaceAll("[^0-9]", "");
        if (!digits.isEmpty()) {
            try {
                return Integer.parseInt(digits);
            } catch (Exception ignored) {}
        }
        return fallback;
    }

    public VBox getContent() {
        return content;
    }

    public VBox getRoot() {
        return content;
    }
}