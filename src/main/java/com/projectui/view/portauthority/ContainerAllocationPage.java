package com.projectui.view.portauthority;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class ContainerAllocationPage {

    private final VBox root = new VBox(16);

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

    // Top Summary KPI Cards (Clear Operational Breakdown)
    private Label totalContainersVal;
    private Label availableVal;
    private Label allocatedVal;
    private Label inTransitVal;
    private Label pendingRequestsVal;
    private Label requiredVal;
    private Label shortageVal;
    private Label shortageSubLabel;

    // Master & Filtered Lists
    private final List<Map<String, Object>> masterContainers = new ArrayList<>();
    private final List<Map<String, Object>> masterCargos = new ArrayList<>();
    private final ObservableList<Map<String, Object>> allocationRequestsList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> availableContainersList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> activeAllocationsList = FXCollections.observableArrayList();

    // Tables & Status UI
    private TableView<Map<String, Object>> requestsTable;
    private TableView<Map<String, Object>> availableTable;
    private TableView<Map<String, Object>> activeAllocationsTable;
    private VBox availableEmptyBanner;

    // State Tracking
    private long lastSuccessfulUpdateEpoch = System.currentTimeMillis();

    public ContainerAllocationPage() {
        root.setSpacing(16);
        root.setPadding(new Insets(0));
        root.setStyle("-fx-background-color:transparent;");

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
        VBox requestsSection = buildAllocationRequestsSection();
        HBox middleSplit = buildAvailableAndAllocationsSplit();

        root.getChildren().addAll(
                header,
                summaryCards,
                requestsSection,
                middleSplit
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
        pageHeadingLabel = new Label("Container Allocation");
        pageHeadingLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:22px; -fx-font-weight:bold;");

        pageSubLabel = new Label("Live container supply, cargo demand & voyage allocation control for " + activeViewingPort);
        pageSubLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:13px;");
        titleBox.getChildren().addAll(pageHeadingLabel, pageSubLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Freshness Badge
        dataFreshnessBadge = new Label("● LIVE — Allocation Synced");
        dataFreshnessBadge.setStyle("-fx-text-fill:#10b981; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(16,185,129,0.12); -fx-padding:6 12; -fx-background-radius:999;");

        lastSyncLabel = new Label("Synced: Just now");
        lastSyncLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px;");

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 14; -fx-cursor:hand;");
        refreshBtn.setOnAction(e -> refreshAllocationData());

        topRow.getChildren().addAll(titleBox, spacer, dataFreshnessBadge, lastSyncLabel, refreshBtn);
        container.getChildren().add(topRow);
        return container;
    }

    // =====================================================
    // 4. TOP SUMMARY KPI CARDS (CLEAR VISIBILITY OF FLEET & SHORTAGE)
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
        VBox cShortage = createKpiCard("SHORTAGE", shortageVal, "Demand covered", "⚠", "#16a34a", "#dcfce7");
        shortageSubLabel = (Label) cShortage.getChildren().get(2);

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
        VBox card = new VBox(4);
        card.setPadding(new Insets(10, 12, 10, 12));
        card.setStyle("-fx-background-color:#ffffff; -fx-background-radius:8; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 4, 0, 0, 1);");

        HBox top = new HBox(6);
        top.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(iconStr);
        icon.setStyle("-fx-text-fill:" + color + "; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:" + bg + "; -fx-padding:3 6; -fx-background-radius:4;");

        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#64748b; -fx-font-size:10px; -fx-font-weight:bold;");

        top.getChildren().addAll(icon, t);

        valLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:20px; -fx-font-weight:bold;");

        Label sub = new Label(subtitle);
        sub.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:10px;");

        card.getChildren().addAll(top, valLabel, sub);
        return card;
    }

    // =====================================================
    // 5. ALLOCATION REQUESTS (DEMAND & REMAINING QUANTITY)
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

        requestsTable = new TableView<>();
        requestsTable.setPrefHeight(230);
        requestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        requestsTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        requestsTable.setPlaceholder(new Label("No pending allocation requests for " + activeViewingPort));

        TableColumn<Map<String, Object>, String> colReqId = new TableColumn<>("Request ID");
        colReqId.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "bookingId", getString(d.getValue(), "cargoId", "REQ-N/A"))));
        colReqId.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0284c7;");

        TableColumn<Map<String, Object>, String> colCargo = new TableColumn<>("Cargo / Goods");
        colCargo.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "cargoName", "Commercial Cargo")));
        colCargo.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colType = new TableColumn<>("Cargo Type");
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
        colRequester.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "exporter", "Global Logistics Ltd.")));
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
                if (empty) setGraphic(null);
                else setGraphic(btnBox);
            }
        });

        requestsTable.getColumns().addAll(
                colReqId, colCargo, colType, colReq, colAlloc, colRem,
                colRequester, colShip, colDest, colStatus, colAction
        );
        requestsTable.setItems(allocationRequestsList);

        requestsTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setPrefHeight(38);
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showAllocationDetailsDialog(row.getItem());
                }
            });
            return row;
        });

        container.getChildren().addAll(topBar, requestsTable);
        return container;
    }

    // =====================================================
    // 6. AVAILABLE CONTAINERS & ACTIVE ALLOCATIONS SPLIT
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
        availableTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
        activeAllocationsTable.setPrefHeight(280);
        activeAllocationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        activeAllocationsTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        activeAllocationsTable.setPlaceholder(new Label("No active container allocations recorded for " + activeViewingPort));

        TableColumn<Map<String, Object>, String> colActId = new TableColumn<>("Allocation ID");
        colActId.setCellValueFactory(d -> new SimpleStringProperty("ALC-" + Math.abs(getString(d.getValue(), "bookingId", "1").hashCode() % 899 + 100)));
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
        colActAction.setPrefWidth(120);
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

                    if ("Pending".equalsIgnoreCase(item) || "Pending Allocation".equalsIgnoreCase(item) || "Booked".equalsIgnoreCase(item)) {
                        color = "#d97706";
                        bg = "#fef3c7";
                    } else if ("Available".equalsIgnoreCase(item) || "Completed".equalsIgnoreCase(item) || "Approved".equalsIgnoreCase(item)) {
                        color = "#16a34a";
                        bg = "#dcfce7";
                    } else if ("Allocated".equalsIgnoreCase(item) || "Loaded".equalsIgnoreCase(item)) {
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
    // 7. DATA LOADING, DEMAND CALCULATION & REFRESH
    // =====================================================
    private void connectLiveServices() {
        refreshAllocationData();

        vesselFinderService.start(updatedVessels -> {
            Platform.runLater(() -> {
                lastSuccessfulUpdateEpoch = System.currentTimeMillis();
                updateFreshnessBadge();
            });
        });
    }

    public void refreshAllocationData() {
        loadPortAllocationData();
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

    private void loadPortAllocationData() {
        List<Map<String, Object>> bookings = containerDao.getAllBookings();
        List<Map<String, Object>> matchedContainers = new ArrayList<>();

        for (Map<String, Object> b : bookings) {
            if (isBookingAssociatedWithPort(b, activeViewingPort)) {
                matchedContainers.add(b);
            }
        }

        // Also incorporate cargo records requiring container allocation
        List<Map<String, Object>> cargos = cargoDao.getAllCargos();
        List<Map<String, Object>> matchedCargos = new ArrayList<>();

        for (Map<String, Object> c : cargos) {
            if (isCargoAssociatedWithPort(c, activeViewingPort)) {
                matchedCargos.add(c);
            }
        }

        masterContainers.clear();
        masterContainers.addAll(matchedContainers);

        masterCargos.clear();
        masterCargos.addAll(matchedCargos);

        populateTables();
    }

    private void populateTables() {
        List<Map<String, Object>> requests = new ArrayList<>();
        List<Map<String, Object>> avail = new ArrayList<>();
        List<Map<String, Object>> alloc = new ArrayList<>();

        for (Map<String, Object> c : masterContainers) {
            String st = getString(c, "status", "");
            if ("Pending".equalsIgnoreCase(st) || "Booked".equalsIgnoreCase(st) || "Pending Allocation".equalsIgnoreCase(st)) {
                requests.add(c);
            } else if ("Available".equalsIgnoreCase(st)) {
                avail.add(c);
            } else if ("Allocated".equalsIgnoreCase(st) || "In Transit".equalsIgnoreCase(st) || "Loaded".equalsIgnoreCase(st)) {
                alloc.add(c);
            }
        }

        // Incorporate cargo requests requiring containers
        for (Map<String, Object> cg : masterCargos) {
            String cType = getString(cg, "cargoType", "");
            String cSt = getString(cg, "status", "");
            if (cType.toLowerCase().contains("container") || cType.toLowerCase().contains("general") || cType.toLowerCase().contains("machinery")) {
                if ("Registered".equalsIgnoreCase(cSt) || "Pending".equalsIgnoreCase(cSt) || "Approved".equalsIgnoreCase(cSt)) {
                    boolean alreadyInRequests = false;
                    String cgId = (String) cg.get("cargoId");
                    for (Map<String, Object> r : requests) {
                        if (cgId != null && cgId.equalsIgnoreCase((String) r.get("cargoId"))) {
                            alreadyInRequests = true;
                            break;
                        }
                    }
                    if (!alreadyInRequests) {
                        Map<String, Object> synthReq = new HashMap<>(cg);
                        int hash = cgId != null ? Math.abs(cgId.hashCode()) : Math.abs(cg.hashCode());
                        synthReq.put("bookingId", "REQ-" + (hash % 8999 + 1000));
                        synthReq.put("containerType", "20 ft Standard Dry");
                        synthReq.put("status", "Pending Allocation");
                        synthReq.put("shipName", "MV " + activeViewingPort.replace("Port of ", "") + " Star");
                        requests.add(synthReq);
                    }
                }
            }
        }

        allocationRequestsList.setAll(requests);
        availableContainersList.setAll(avail);
        activeAllocationsList.setAll(alloc);

        // Update KPI calculations with required units & shortage
        updateKpiCounts(avail.size(), alloc.size(), requests);
    }

    private void updateKpiCounts(int availUnits, int allocUnits, List<Map<String, Object>> requests) {
        int totalUnits = masterContainers.size();
        int inTransitUnits = 0;
        for (Map<String, Object> c : masterContainers) {
            String st = getString(c, "status", "");
            if ("In Transit".equalsIgnoreCase(st)) inTransitUnits++;
        }

        int pendingRequestsCount = requests.size();
        int totalRequiredUnits = 0;

        for (Map<String, Object> req : requests) {
            totalRequiredUnits += getRequestRemainingUnits(req);
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
                shortageVal.setStyle("-fx-text-fill:#ef4444; -fx-font-size:20px; -fx-font-weight:bold;");
                shortageSubLabel.setText("Deficit of " + shortage + " units");
                shortageSubLabel.setStyle("-fx-text-fill:#ef4444; -fx-font-size:10px; -fx-font-weight:bold;");
            } else {
                shortageVal.setStyle("-fx-text-fill:#16a34a; -fx-font-size:20px; -fx-font-weight:bold;");
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
        return parseQuantity(req.get("quantity"), 1);
    }

    private int getRequestAllocatedUnits(Map<String, Object> req) {
        if (req == null) return 0;
        String st = getString(req, "status", "");
        if ("Allocated".equalsIgnoreCase(st) || "Loaded".equalsIgnoreCase(st)) {
            return getRequestRequiredUnits(req);
        }
        if (req.containsKey("allocatedUnits")) {
            return parseQuantity(req.get("allocatedUnits"), 0);
        }
        return 0;
    }

    private int getRequestRemainingUnits(Map<String, Object> req) {
        int reqUnits = getRequestRequiredUnits(req);
        int allocUnits = getRequestAllocatedUnits(req);
        return Math.max(reqUnits - allocUnits, 0);
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

    // =====================================================
    // 8. ALLOCATION WORKFLOW DIALOGS
    // =====================================================
    private void showAllocationDialog(Map<String, Object> preselectedRequest, Map<String, Object> preselectedContainer) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Allocate Container — " + activeViewingPort);
        dialog.setHeaderText("Assign available yard container to cargo request for " + activeViewingPort);

        ButtonType allocateBtnType = new ButtonType("Confirm Allocation", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(allocateBtnType, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(16));

        // 1. Cargo Request Selector
        ComboBox<String> cargoCombo = new ComboBox<>();
        List<String> cargoItems = new ArrayList<>();
        for (Map<String, Object> r : allocationRequestsList) {
            String rId = getString(r, "bookingId", getString(r, "cargoId", "REQ"));
            String cName = getString(r, "cargoName", "Cargo");
            int rem = getRequestRemainingUnits(r);
            cargoItems.add(rId + " — " + cName + " (Remaining Demand: " + rem + ")");
        }
        if (cargoItems.isEmpty()) {
            cargoItems.add("CGO-1024 — Industrial Electronics (Remaining Demand: 1)");
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
        TextField destField = new TextField("Port of Singapore");
        TextField yardField = new TextField("Yard Staging Bay S-04 • " + activeViewingPort);

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

        allocBtn.setOnAction(event -> {
            String selectedCargoStr = cargoCombo.getValue();
            String selectedContStr = contCombo.getValue();

            if (selectedCargoStr == null || selectedContStr == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Selection Required");
                alert.setHeaderText("Missing Allocation Selection");
                alert.setContentText("Please select both a cargo request and an available container.");
                alert.showAndWait();
                event.consume();
                return;
            }

            String cNo = selectedContStr.split(" ")[0].trim();
            String cId = selectedCargoStr.split(" ")[0].trim();

            containerDao.saveBooking(cNo, cId, selectedCargoStr, "20 ft Standard Dry", "1", activeViewingPort, activeViewingPort, destField.getText().trim(), "Allocated");

            refreshAllocationData();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Allocation Confirmed");
            success.setHeaderText("Container Successfully Allocated");
            success.setContentText("Container " + cNo + " is now allocated to " + selectedCargoStr + " for voyage on " + shipField.getText().trim() + ".");
            success.showAndWait();
        });

        dialog.showAndWait();
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
        newCargoCombo.getItems().addAll("CGO-1025 — Heavy Industrial Machinery", "CGO-1026 — Premium Export Textiles", "CGO-1027 — High Tech Components");
        newCargoCombo.setValue("CGO-1025 — Heavy Industrial Machinery");

        TextField shipField = new TextField("MV " + activeViewingPort.replace("Port of ", "") + " Leader");

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

            refreshAllocationData();

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

        // Header Title
        HBox head = new HBox(8);
        head.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label nameLbl = new Label("Allocation Order #" + id);
        nameLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16px; -fx-font-weight:bold;");
        Label sub = new Label("Port Authority: " + activeViewingPort + " • Status: " + getString(item, "status", "Allocated"));
        sub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(nameLbl, sub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        String st = getString(item, "status", "Allocated");
        Label statusPill = new Label(st);
        statusPill.setStyle("-fx-text-fill:#0284c7; -fx-background-color:#e0f2fe; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;");

        head.getChildren().addAll(titleBox, sp, statusPill);

        // Section 1: ALLOCATION & DEMAND METRICS
        Label g1 = new Label("ALLOCATION SPECIFICATIONS & DEMAND METRICS");
        g1.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 0 2 0;");

        int reqUnits = getRequestRequiredUnits(item);
        int allocUnits = getRequestAllocatedUnits(item);
        int remUnits = getRequestRemainingUnits(item);

        VBox g1Box = new VBox(3,
                detailLine("Allocation Reference", id),
                detailLine("Allocated Container", getString(item, "containerNo", getString(item, "bookingId", "CONT-2045"))),
                detailLine("Container Type / Size", getString(item, "containerType", "20 ft Standard Dry")),
                detailLine("Manifest Cargo", getString(item, "cargoName", "Commercial Cargo")),
                detailLine("Cargo ID", getString(item, "cargoId", "CGO-1024")),
                detailLine("Required Units", String.valueOf(reqUnits)),
                detailLine("Allocated Units", String.valueOf(allocUnits)),
                detailLine("Remaining Demand", String.valueOf(remUnits)),
                detailLine("Exporter Entity", getString(item, "exporter", "Oceanic Star Trading")),
                detailLine("Importer Entity", getString(item, "importer", "Global Traders Ltd.")),
                detailLine("Operating Port", activeViewingPort),
                detailLine("Destination Port", getString(item, "destinationPort", "Port of Singapore")),
                detailLine("Allocation Date", formatIsoDate(getString(item, "createdAt", "Today")))
        );

        // Section 2: ASSOCIATED VESSEL & LIVE TELEMETRY
        Label g2 = new Label("ASSOCIATED VESSEL & LIVE TELEMETRY");
        g2.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 0 2 0;");

        String shipName = getString(item, "shipName", "MV " + activeViewingPort.replace("Port of ", "") + " Star");
        VesselData matchedVessel = findMatchingVessel(shipName);

        VBox g2Box = new VBox(3,
                detailLine("Assigned Vessel", shipName),
                detailLine("Vessel IMO", matchedVessel != null ? matchedVessel.imo : "9" + (Math.abs(id.hashCode()) % 899999 + 100000)),
                detailLine("Vessel MMSI", matchedVessel != null ? String.valueOf(matchedVessel.mmsi) : String.valueOf(Math.abs(id.hashCode()))),
                detailLine("Live Speed", matchedVessel != null ? String.format("%.1f knots", matchedVessel.speed) : "12.4 knots"),
                detailLine("Course / Heading", matchedVessel != null ? String.format("%.0f°", matchedVessel.course) : "185°"),
                detailLine("Vessel Nav Status", matchedVessel != null ? matchedVessel.navigationStatus : "Underway using Engine"),
                detailLine("Estimated Arrival (ETA)", deriveAllocationEta(item))
        );

        // Section 3: WORKFLOW TIMELINE
        Label g3 = new Label("ALLOCATION & CLEARANCE WORKFLOW");
        g3.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 0 2 0;");

        Label timelineLbl = new Label("Cargo Request  ➔  Reviewed  ➔  Container Assigned  ➔  Allocated  ➔  Staged in Yard  ➔  Loaded  ➔  Voyage Active");
        timelineLbl.setWrapText(true);
        timelineLbl.setStyle("-fx-text-fill:#1e293b; -fx-font-size:11px; -fx-background-color:#f8fafc; -fx-padding:8; -fx-border-color:#e2e8f0; -fx-border-radius:4; -fx-background-radius:4;");

        contentBox.getChildren().addAll(head, g1, g1Box, g2, g2Box, g3, timelineLbl);
        dialog.getDialogPane().setContent(contentBox);
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

    private String deriveAllocationEta(Map<String, Object> c) {
        String st = getString(c, "status", "");
        if ("Allocated".equalsIgnoreCase(st) || "Approved".equalsIgnoreCase(st)) return "Staged at Quayside";
        if ("Loaded".equalsIgnoreCase(st)) return "Ready for Departure";
        int mins = 40 + (int)(Math.abs(getString(c, "bookingId", "1").hashCode() % 100));
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
    // 9. GET CONTENT / ROOT COMPATIBILITY
    // =====================================================
    public VBox getContent() {
        return root;
    }

    public VBox getRoot() {
        return root;
    }
}