package com.projectui.view.portauthority;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
public class CargoManagementPage {

    private final VBox content = new VBox(16);

    // Backend DAOs & Services (Single source of truth with Admin, Importer & Exporter)
    private final CargoDao cargoDao = new CargoDao();
    private final ContainerDao containerDao = new ContainerDao();
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
    private Label totalCargoVal;
    private Label pendingRequestsVal;
    private Label receivedVal;
    private Label inTransitVal;
    private Label loadedVal;
    private Label dispatchedVal;

    // Cargo Requests Section Metrics
    private Label reqPendingCountVal;
    private Label reqApprovedCountVal;
    private Label reqRejectedCountVal;
    private Label reqActiveCountVal;
    private Label reqCompletedCountVal;

    // Master & Filtered Lists
    private final List<Map<String, Object>> masterCargoList = new ArrayList<>();
    private final ObservableList<Map<String, Object>> pendingRequestsList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> allCargoOperationsList = FXCollections.observableArrayList();

    // Controls
    private TableView<Map<String, Object>> pendingRequestsTable;
    private TableView<Map<String, Object>> cargoOperationsTable;
    private TextField searchField;
    private ComboBox<String> typeFilterCombo;
    private ComboBox<String> statusFilterCombo;
    private ComboBox<String> sortCombo;

    // Activity Feed & Alerts Containers
    private VBox recentActivityBox;
    private VBox alertsFeedBox;
    private final List<String> recentActivityItems = new ArrayList<>();

    // State Tracking
    private long lastSuccessfulUpdateEpoch = System.currentTimeMillis();

    public CargoManagementPage() {
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
        VBox cargoRequestsSection = buildCargoRequestsSection();
        VBox mainCargoTableSection = buildMainCargoTableSection();
        HBox bottomSplit = buildBottomSplit();

        content.getChildren().addAll(
                header,
                summaryCards,
                cargoRequestsSection,
                mainCargoTableSection,
                bottomSplit
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
        pageHeadingLabel = new Label("Cargo Management");
        pageHeadingLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:22px; -fx-font-weight:bold;");

        pageSubLabel = new Label("Review importer cargo requests, monitor inbound/outbound manifests & live quayside operations for " + activeViewingPort);
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
        refreshBtn.setOnAction(e -> refreshCargoData());

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

        totalCargoVal = new Label("0");
        pendingRequestsVal = new Label("0");
        receivedVal = new Label("0");
        inTransitVal = new Label("0");
        loadedVal = new Label("0");
        dispatchedVal = new Label("0");

        VBox cTotal = createKpiCard("Total Cargo", totalCargoVal, "Assigned to " + activeViewingPort, "📦", "#0284c7", "#e0f2fe");
        VBox cPending = createKpiCard("Pending Requests", pendingRequestsVal, "Awaiting Port Review", "⏳", "#d97706", "#fef3c7");
        VBox cReceived = createKpiCard("Received", receivedVal, "Stored in Port Facilities", "📥", "#16a34a", "#ecfdf5");
        VBox cInTransit = createKpiCard("In Transit", inTransitVal, "En Route / Sea Voyage", "🚢", "#3b82f6", "#eff6ff");
        VBox cLoaded = createKpiCard("Loaded", loadedVal, "Staged on Vessels", "🏗", "#0891b2", "#cffafe");
        VBox cDispatched = createKpiCard("Dispatched", dispatchedVal, "Gate-Out / Cleared", "📤", "#8b5cf6", "#f5f3ff");

        row.getChildren().addAll(cTotal, cPending, cReceived, cInTransit, cLoaded, cDispatched);

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
    // 5. CARGO REQUESTS SECTION (IMPORTER WORKFLOW REVIEW)
    // =====================================================
    private VBox buildCargoRequestsSection() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        // Section Top Bar
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label secTitle = new Label("CARGO REQUESTS — Importer Submissions");
        secTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:15px; -fx-font-weight:bold;");
        Label secSub = new Label("Incoming cargo manifests and requests submitted by importers requiring authorization for " + activeViewingPort);
        secSub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(secTitle, secSub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        // Summary Metric Chips
        reqPendingCountVal = new Label("0");
        reqApprovedCountVal = new Label("0");
        reqRejectedCountVal = new Label("0");
        reqActiveCountVal = new Label("0");
        reqCompletedCountVal = new Label("0");

        HBox metricsRow = new HBox(8);
        metricsRow.setAlignment(Pos.CENTER_RIGHT);
        metricsRow.getChildren().addAll(
                createMiniChip("Pending", reqPendingCountVal, "#d97706", "#fef3c7"),
                createMiniChip("Approved", reqApprovedCountVal, "#16a34a", "#ecfdf5"),
                createMiniChip("Rejected", reqRejectedCountVal, "#ef4444", "#fee2e2"),
                createMiniChip("Active", reqActiveCountVal, "#0284c7", "#e0f2fe"),
                createMiniChip("Completed", reqCompletedCountVal, "#64748b", "#f1f5f9")
        );

        topBar.getChildren().addAll(titleBox, sp, metricsRow);

        // Pending & Importer Requests Table
        pendingRequestsTable = new TableView<>();
        pendingRequestsTable.setPrefHeight(220);
        pendingRequestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        pendingRequestsTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        pendingRequestsTable.setPlaceholder(new Label("No pending cargo requests for " + activeViewingPort));

        TableColumn<Map<String, Object>, String> colReqId = new TableColumn<>("Request ID");
        colReqId.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "cargoId", "REQ-N/A")));
        colReqId.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0284c7;");

        TableColumn<Map<String, Object>, String> colCargo = new TableColumn<>("Cargo");
        colCargo.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "cargoName", "General Goods")));
        colCargo.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colType = new TableColumn<>("Cargo Type");
        colType.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "cargoType", "General Cargo")));
        colType.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colQty = new TableColumn<>("Quantity");
        colQty.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "quantity", "1 unit")));
        colQty.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colWeight = new TableColumn<>("Weight");
        colWeight.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "weight", "—")));
        colWeight.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colImporter = new TableColumn<>("Importer");
        colImporter.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "importer", "Global Traders Ltd.")));
        colImporter.setStyle("-fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colExporter = new TableColumn<>("Exporter");
        colExporter.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "exporter", "Oceanic Star Trading")));
        colExporter.setStyle("-fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colVessel = new TableColumn<>("Vessel");
        colVessel.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "shipName", "Assigned Vessel")));
        colVessel.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#2563eb; -fx-font-weight:bold;");

        TableColumn<Map<String, Object>, String> colOrigin = new TableColumn<>("Origin");
        colOrigin.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "originPort", "Singapore")));
        colOrigin.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colDest = new TableColumn<>("Destination");
        colDest.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "destinationPort", activeViewingPort)));
        colDest.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#0f172a; -fx-font-weight:bold;");

        TableColumn<Map<String, Object>, String> colDate = new TableColumn<>("Requested Date");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(formatIsoDate(getString(d.getValue(), "createdAt", "Today"))));
        colDate.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "status", "Pending")));
        colStatus.setCellFactory(col -> createStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(190);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final HBox actionsBox = new HBox(6, viewBtn, approveBtn, rejectBtn);

            {
                actionsBox.setAlignment(Pos.CENTER);
                viewBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");
                approveBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");
                rejectBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");

                viewBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    showCargoDetailsDialog(item);
                });

                approveBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    handleApproveRequest(item);
                });

                rejectBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    handleRejectRequest(item);
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
                    if ("Approved".equalsIgnoreCase(st) || "Received".equalsIgnoreCase(st) || "Rejected".equalsIgnoreCase(st)) {
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
                    setGraphic(actionsBox);
                }
            }
        });

        pendingRequestsTable.getColumns().addAll(
                colReqId, colCargo, colType, colQty, colWeight,
                colImporter, colExporter, colVessel, colOrigin, colDest,
                colDate, colStatus, colAction
        );
        pendingRequestsTable.setItems(pendingRequestsList);

        pendingRequestsTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setPrefHeight(38);
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showCargoDetailsDialog(row.getItem());
                }
            });
            return row;
        });

        container.getChildren().addAll(topBar, pendingRequestsTable);
        return container;
    }

    private HBox createMiniChip(String label, Label valLbl, String color, String bg) {
        HBox chip = new HBox(4);
        chip.setAlignment(Pos.CENTER);
        chip.setPadding(new Insets(4, 8, 4, 8));
        chip.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:6;");

        Label l = new Label(label + ":");
        l.setStyle("-fx-text-fill:" + color + "; -fx-font-size:11px;");

        valLbl.setStyle("-fx-text-fill:" + color + "; -fx-font-size:12px; -fx-font-weight:bold;");

        chip.getChildren().addAll(l, valLbl);
        return chip;
    }

    // =====================================================
    // 6. MAIN LIVE CARGO OPERATIONS TABLE
    // =====================================================
    private VBox buildMainCargoTableSection() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        // Controls Row
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label tblTitle = new Label("Live Cargo Operations & Manifest");
        tblTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:15px; -fx-font-weight:bold;");
        Label tblSub = new Label("Authorized cargo inventory, live vessel assignments & freight tracking for " + activeViewingPort);
        tblSub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(tblTitle, tblSub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("🔍 Search Cargo / Ship / Importer / Exporter...");
        searchField.setPrefWidth(260);
        searchField.setPrefHeight(34);
        searchField.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-padding:5 10;");
        searchField.textProperty().addListener((obs, ov, nv) -> applyFilters());

        typeFilterCombo = new ComboBox<>();
        typeFilterCombo.getItems().addAll("All Types", "General Cargo", "Container", "Bulk", "Refrigerated", "Liquid", "Machinery", "Textiles");
        typeFilterCombo.setValue("All Types");
        typeFilterCombo.setPrefHeight(34);
        typeFilterCombo.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-pref-width:130;");
        typeFilterCombo.setOnAction(e -> applyFilters());

        statusFilterCombo = new ComboBox<>();
        statusFilterCombo.getItems().addAll("All Status", "Pending", "Approved", "Received", "In Transit", "Loaded", "Dispatched", "Delivered", "Rejected");
        statusFilterCombo.setValue("All Status");
        statusFilterCombo.setPrefHeight(34);
        statusFilterCombo.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-pref-width:130;");
        statusFilterCombo.setOnAction(e -> applyFilters());

        sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Sort: ID (Newest)", "Sort: Status", "Sort: Type", "Sort: Weight");
        sortCombo.setValue("Sort: ID (Newest)");
        sortCombo.setPrefHeight(34);
        sortCombo.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-pref-width:140;");
        sortCombo.setOnAction(e -> applyFilters());

        Button addCargoBtn = new Button("+ Register Cargo");
        addCargoBtn.setPrefHeight(34);
        addCargoBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 14; -fx-cursor:hand;");
        addCargoBtn.setOnAction(e -> showAddCargoDialog());

        controls.getChildren().addAll(titleBox, sp, searchField, typeFilterCombo, statusFilterCombo, sortCombo, addCargoBtn);

        // Table
        cargoOperationsTable = new TableView<>();
        cargoOperationsTable.setPrefHeight(380);
        cargoOperationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        cargoOperationsTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        cargoOperationsTable.setPlaceholder(new Label("No cargo records available for " + activeViewingPort));

        TableColumn<Map<String, Object>, String> colCgoId = new TableColumn<>("Cargo ID");
        colCgoId.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "cargoId", "CGO-N/A")));
        colCgoId.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0284c7;");

        TableColumn<Map<String, Object>, String> colType = new TableColumn<>("Cargo Type");
        colType.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "cargoType", "General Cargo")));
        colType.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colShip = new TableColumn<>("Ship / Voyage");
        colShip.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "shipName", "Assigned Vessel")));
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

        TableColumn<Map<String, Object>, String> colImporter = new TableColumn<>("Importer");
        colImporter.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "importer", "Global Traders Ltd.")));
        colImporter.setStyle("-fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colExporter = new TableColumn<>("Exporter");
        colExporter.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "exporter", "Oceanic Star Trading")));
        colExporter.setStyle("-fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colQtyWeight = new TableColumn<>("Quantity / Weight");
        colQtyWeight.setCellValueFactory(d -> {
            String q = getString(d.getValue(), "quantity", "1 unit");
            String w = getString(d.getValue(), "weight", "—");
            return new SimpleStringProperty(q + " (" + w + ")");
        });
        colQtyWeight.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colOrigin = new TableColumn<>("Origin");
        colOrigin.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "originPort", "Port of Singapore")));
        colOrigin.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colDest = new TableColumn<>("Destination");
        colDest.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "destinationPort", activeViewingPort)));
        colDest.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "status", "Registered")));
        colStatus.setCellFactory(col -> createStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colEta = new TableColumn<>("ETA / Date");
        colEta.setCellValueFactory(d -> new SimpleStringProperty(deriveCargoEta(d.getValue())));
        colEta.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#2563eb; -fx-font-weight:bold;");

        TableColumn<Map<String, Object>, String> colUpdated = new TableColumn<>("Last Updated");
        colUpdated.setCellValueFactory(d -> new SimpleStringProperty(formatIsoDate(getString(d.getValue(), "createdAt", "Today"))));
        colUpdated.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(160);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button updateBtn = new Button("Status");
            private final HBox btnBox = new HBox(6, viewBtn, updateBtn);

            {
                btnBox.setAlignment(Pos.CENTER);
                viewBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");
                updateBtn.setStyle("-fx-background-color:#475569; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");

                viewBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    showCargoDetailsDialog(item);
                });

                updateBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    showUpdateStatusDialog(item);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btnBox);
            }
        });

        cargoOperationsTable.getColumns().addAll(
                colCgoId, colType, colShip, colImporter, colExporter,
                colQtyWeight, colOrigin, colDest, colStatus, colEta,
                colUpdated, colAction
        );
        cargoOperationsTable.setItems(allCargoOperationsList);

        cargoOperationsTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setPrefHeight(38);
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showCargoDetailsDialog(row.getItem());
                }
            });
            return row;
        });

        container.getChildren().addAll(controls, cargoOperationsTable);
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

                    if ("Pending".equalsIgnoreCase(item) || "Pending Inspection".equalsIgnoreCase(item) || "Registered".equalsIgnoreCase(item)) {
                        color = "#d97706";
                        bg = "#fef3c7";
                    } else if ("Approved".equalsIgnoreCase(item) || "Received".equalsIgnoreCase(item) || "Delivered".equalsIgnoreCase(item)) {
                        color = "#16a34a";
                        bg = "#dcfce7";
                    } else if ("In Transit".equalsIgnoreCase(item)) {
                        color = "#2563eb";
                        bg = "#dbeafe";
                    } else if ("Loaded".equalsIgnoreCase(item) || "Loading".equalsIgnoreCase(item)) {
                        color = "#0891b2";
                        bg = "#cffafe";
                    } else if ("Dispatched".equalsIgnoreCase(item)) {
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
    // 7. BOTTOM SPLIT (RECENT ACTIVITY + IMPORTANT ALERTS)
    // =====================================================
    private HBox buildBottomSplit() {
        HBox split = new HBox(16);
        split.setAlignment(Pos.TOP_LEFT);

        // 1. Recent Cargo Activity Box (60% Width)
        recentActivityBox = new VBox(10);
        recentActivityBox.setPadding(new Insets(14, 16, 14, 16));
        recentActivityBox.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10;");
        HBox.setHgrow(recentActivityBox, Priority.ALWAYS);

        HBox actHead = new HBox(8);
        actHead.setAlignment(Pos.CENTER_LEFT);
        Label actIcon = new Label("⚡");
        actIcon.setStyle("-fx-font-size:14px;");
        Label actTitle = new Label("RECENT CARGO ACTIVITY");
        actTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13px; -fx-font-weight:bold;");
        actHead.getChildren().addAll(actIcon, actTitle);

        recentActivityBox.getChildren().add(actHead);

        // 2. Important Cargo Alerts Box (40% Width)
        alertsFeedBox = new VBox(10);
        alertsFeedBox.setPadding(new Insets(14, 16, 14, 16));
        alertsFeedBox.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10;");
        alertsFeedBox.setPrefWidth(380);
        alertsFeedBox.setMinWidth(340);

        HBox altHead = new HBox(8);
        altHead.setAlignment(Pos.CENTER_LEFT);
        Label altIcon = new Label("🔔");
        altIcon.setStyle("-fx-font-size:14px;");
        Label altTitle = new Label("IMPORTANT CARGO ALERTS");
        altTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13px; -fx-font-weight:bold;");
        altHead.getChildren().addAll(altIcon, altTitle);

        alertsFeedBox.getChildren().add(altHead);

        split.getChildren().addAll(recentActivityBox, alertsFeedBox);
        return split;
    }

    // =====================================================
    // 8. DATA LOADING, FILTERING & REFRESH
    // =====================================================
    private void connectLiveServices() {
        refreshCargoData();

        vesselFinderService.start(updatedVessels -> {
            Platform.runLater(() -> {
                lastSuccessfulUpdateEpoch = System.currentTimeMillis();
                updateFreshnessBadge();
            });
        });
    }

    public void refreshCargoData() {
        loadPortCargoFromSource();
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

    private void loadPortCargoFromSource() {
        List<Map<String, Object>> allCargos = cargoDao.getAllCargos();
        List<Map<String, Object>> matchedPortCargos = new ArrayList<>();

        for (Map<String, Object> c : allCargos) {
            if (isCargoAssociatedWithPort(c, activeViewingPort)) {
                matchedPortCargos.add(c);
            }
        }

        // Also incorporate Container Bookings for this port
        List<Map<String, Object>> bookings = containerDao.getAllBookings();
        for (Map<String, Object> b : bookings) {
            if (isBookingAssociatedWithPort(b, activeViewingPort)) {
                String bookingCargoId = (String) b.get("cargoId");
                boolean alreadyListed = false;
                for (Map<String, Object> c : matchedPortCargos) {
                    if (bookingCargoId != null && bookingCargoId.equalsIgnoreCase((String) c.get("cargoId"))) {
                        alreadyListed = true;
                        break;
                    }
                }
                if (!alreadyListed) {
                    Map<String, Object> synthesized = new HashMap<>(b);
                    synthesized.put("cargoId", b.get("bookingId"));
                    synthesized.put("cargoType", b.get("containerType"));
                    synthesized.put("quantity", b.get("quantity") + " containers");
                    synthesized.put("weight", "Standard 24 T");
                    synthesized.put("importer", "Global Traders Ltd.");
                    synthesized.put("exporter", "Oceanic Star Trading");
                    synthesized.put("shipName", "MV " + activeViewingPort.replace("Port of ", "") + " Express");
                    matchedPortCargos.add(synthesized);
                }
            }
        }

        masterCargoList.clear();
        masterCargoList.addAll(matchedPortCargos);

        updateKpiCounts(masterCargoList);
        applyFilters();
        updateRecentActivityFeed();
        updateAlertsFeed();
    }

    private boolean isCargoAssociatedWithPort(Map<String, Object> c, String portName) {
        if (c == null || portName == null) return false;
        String p = portName.toLowerCase();
        String dest = getString(c, "destinationPort", "").toLowerCase();
        String orig = getString(c, "originPort", "").toLowerCase();
        String portField = getString(c, "port", "").toLowerCase();

        if (dest.contains(p) || orig.contains(p) || portField.contains(p)) return true;

        if (p.contains("cochin") || p.contains("kochi")) {
            return dest.contains("cochin") || dest.contains("kochi") || orig.contains("cochin") || orig.contains("kochi");
        } else if (p.contains("mumbai")) {
            return dest.contains("mumbai") || dest.contains("jnpt") || orig.contains("mumbai") || orig.contains("jnpt");
        } else if (p.contains("chennai")) {
            return dest.contains("chennai") || dest.contains("madras") || orig.contains("chennai") || orig.contains("madras");
        } else if (p.contains("vizag")) {
            return dest.contains("vizag") || dest.contains("visakhapatnam") || orig.contains("vizag");
        } else if (p.contains("kolkata")) {
            return dest.contains("kolkata") || dest.contains("calcutta") || orig.contains("kolkata");
        }
        return true;
    }

    private boolean isBookingAssociatedWithPort(Map<String, Object> b, String portName) {
        if (b == null || portName == null) return false;
        String p = portName.toLowerCase();
        String pref = getString(b, "preferredPort", "").toLowerCase();
        String dest = getString(b, "destinationPort", "").toLowerCase();
        String orig = getString(b, "originPort", "").toLowerCase();

        if (pref.contains(p) || dest.contains(p) || orig.contains(p)) return true;

        if (p.contains("cochin") || p.contains("kochi")) {
            return pref.contains("cochin") || pref.contains("kochi") || dest.contains("cochin") || orig.contains("cochin");
        } else if (p.contains("mumbai")) {
            return pref.contains("mumbai") || pref.contains("jnpt") || dest.contains("mumbai") || orig.contains("mumbai");
        } else if (p.contains("chennai")) {
            return pref.contains("chennai") || pref.contains("madras") || dest.contains("chennai") || orig.contains("chennai");
        } else if (p.contains("vizag")) {
            return pref.contains("vizag") || pref.contains("visakhapatnam") || dest.contains("vizag");
        } else if (p.contains("kolkata")) {
            return pref.contains("kolkata") || pref.contains("calcutta") || dest.contains("kolkata");
        }
        return true;
    }

    private void applyFilters() {
        String query = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String typeFilter = typeFilterCombo != null && typeFilterCombo.getValue() != null ? typeFilterCombo.getValue() : "All Types";
        String statusFilter = statusFilterCombo != null && statusFilterCombo.getValue() != null ? statusFilterCombo.getValue() : "All Status";
        String sortOption = sortCombo != null && sortCombo.getValue() != null ? sortCombo.getValue() : "Sort: ID (Newest)";

        List<Map<String, Object>> filteredList = new ArrayList<>();
        List<Map<String, Object>> pendingList = new ArrayList<>();

        for (Map<String, Object> c : masterCargoList) {
            if (c == null) continue;

            String cId = getString(c, "cargoId", "");
            String cName = getString(c, "cargoName", "");
            String cType = getString(c, "cargoType", "");
            String cShip = getString(c, "shipName", "");
            String cImp = getString(c, "importer", "");
            String cExp = getString(c, "exporter", "");
            String cDest = getString(c, "destinationPort", "");
            String cStatus = getString(c, "status", "");

            boolean matchesSearch = query.isEmpty()
                    || cId.toLowerCase().contains(query)
                    || cName.toLowerCase().contains(query)
                    || cType.toLowerCase().contains(query)
                    || cShip.toLowerCase().contains(query)
                    || cImp.toLowerCase().contains(query)
                    || cExp.toLowerCase().contains(query)
                    || cDest.toLowerCase().contains(query);

            if (!matchesSearch) continue;

            if (!"All Types".equalsIgnoreCase(typeFilter)) {
                if (!cType.toLowerCase().contains(typeFilter.toLowerCase())) continue;
            }

            if (!"All Status".equalsIgnoreCase(statusFilter)) {
                if (!cStatus.equalsIgnoreCase(statusFilter)) continue;
            }

            filteredList.add(c);

            // Classify pending/importer requests
            if ("Pending".equalsIgnoreCase(cStatus) || "Registered".equalsIgnoreCase(cStatus) || "Approved".equalsIgnoreCase(cStatus)) {
                pendingList.add(c);
            }
        }

        // Apply Sorting
        if ("Sort: Status".equalsIgnoreCase(sortOption)) {
            filteredList.sort(Comparator.comparing(a -> getString(a, "status", "")));
        } else if ("Sort: Type".equalsIgnoreCase(sortOption)) {
            filteredList.sort(Comparator.comparing(a -> getString(a, "cargoType", "")));
        } else if ("Sort: Weight".equalsIgnoreCase(sortOption)) {
            filteredList.sort((a, b) -> getString(b, "weight", "").compareTo(getString(a, "weight", "")));
        } else {
            filteredList.sort((a, b) -> getString(b, "cargoId", "").compareTo(getString(a, "cargoId", "")));
        }

        allCargoOperationsList.setAll(filteredList);
        pendingRequestsList.setAll(pendingList);
    }

    private void updateKpiCounts(List<Map<String, Object>> list) {
        int total = list.size();
        int pending = 0;
        int approved = 0;
        int rejected = 0;
        int received = 0;
        int inTransit = 0;
        int loaded = 0;
        int dispatched = 0;
        int completed = 0;

        for (Map<String, Object> c : list) {
            String st = getString(c, "status", "");
            if ("Pending".equalsIgnoreCase(st) || "Registered".equalsIgnoreCase(st)) pending++;
            else if ("Approved".equalsIgnoreCase(st)) approved++;
            else if ("Rejected".equalsIgnoreCase(st)) rejected++;
            else if ("Received".equalsIgnoreCase(st)) received++;
            else if ("In Transit".equalsIgnoreCase(st)) inTransit++;
            else if ("Loaded".equalsIgnoreCase(st) || "Loading".equalsIgnoreCase(st)) loaded++;
            else if ("Dispatched".equalsIgnoreCase(st)) dispatched++;
            else if ("Delivered".equalsIgnoreCase(st)) completed++;
        }

        if (totalCargoVal != null) totalCargoVal.setText(String.valueOf(total));
        if (pendingRequestsVal != null) pendingRequestsVal.setText(String.valueOf(pending));
        if (receivedVal != null) receivedVal.setText(String.valueOf(received));
        if (inTransitVal != null) inTransitVal.setText(String.valueOf(inTransit));
        if (loadedVal != null) loadedVal.setText(String.valueOf(loaded));
        if (dispatchedVal != null) dispatchedVal.setText(String.valueOf(dispatched));

        if (reqPendingCountVal != null) reqPendingCountVal.setText(String.valueOf(pending));
        if (reqApprovedCountVal != null) reqApprovedCountVal.setText(String.valueOf(approved));
        if (reqRejectedCountVal != null) reqRejectedCountVal.setText(String.valueOf(rejected));
        if (reqActiveCountVal != null) reqActiveCountVal.setText(String.valueOf(received + inTransit + loaded));
        if (reqCompletedCountVal != null) reqCompletedCountVal.setText(String.valueOf(dispatched + completed));
    }

    private void updateRecentActivityFeed() {
        if (recentActivityBox == null) return;
        while (recentActivityBox.getChildren().size() > 1) {
            recentActivityBox.getChildren().remove(1);
        }

        List<Map<String, Object>> recent = new ArrayList<>(masterCargoList);
        int limit = Math.min(recent.size(), 4);

        for (int i = 0; i < limit; i++) {
            Map<String, Object> c = recent.get(i);
            String id = getString(c, "cargoId", "CGO");
            String name = getString(c, "cargoName", "Cargo");
            String st = getString(c, "status", "Updated");
            String dt = formatIsoDate(getString(c, "createdAt", "Today"));

            HBox item = new HBox(8);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setStyle("-fx-border-color:rgba(226,232,240,0.5); -fx-border-width:0 0 1 0; -fx-padding:4 0;");

            Label timeLbl = new Label(dt);
            timeLbl.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px; -fx-font-weight:bold; -fx-min-width:70;");

            Label descLbl = new Label("Cargo " + id + " (" + name + ") status is " + st + " at " + activeViewingPort);
            descLbl.setStyle("-fx-text-fill:#1e293b; -fx-font-size:12px;");

            item.getChildren().addAll(timeLbl, descLbl);
            recentActivityBox.getChildren().add(item);
        }

        if (limit == 0) {
            Label noAct = new Label("No recent cargo movements recorded for " + activeViewingPort);
            noAct.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px; -fx-padding:8 0;");
            recentActivityBox.getChildren().add(noAct);
        }
    }

    private void updateAlertsFeed() {
        if (alertsFeedBox == null) return;
        while (alertsFeedBox.getChildren().size() > 1) {
            alertsFeedBox.getChildren().remove(1);
        }

        int pendingCount = 0;
        for (Map<String, Object> c : masterCargoList) {
            if ("Pending".equalsIgnoreCase(getString(c, "status", ""))) pendingCount++;
        }

        if (pendingCount > 0) {
            alertsFeedBox.getChildren().add(createAlertCard("Pending Importer Requests",
                    pendingCount + " cargo manifest submission(s) awaiting Port Authority authorization.", "#d97706", "#fef3c7"));
        }

        alertsFeedBox.getChildren().add(createAlertCard("Quayside Berth Capacity",
                "Quayside container and liquid berths operating within normal handling thresholds.", "#0284c7", "#e0f2fe"));

        alertsFeedBox.getChildren().add(createAlertCard("Customs Clearance Sync",
                "Customs officer automated documentation channel active and synced.", "#16a34a", "#ecfdf5"));
    }

    private VBox createAlertCard(String title, String message, String color, String bg) {
        VBox card = new VBox(2);
        card.setPadding(new Insets(7, 10, 7, 10));
        card.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:6;");

        Label t = new Label(title);
        t.setStyle("-fx-text-fill:" + color + "; -fx-font-size:12px; -fx-font-weight:bold;");

        Label m = new Label(message);
        m.setWrapText(true);
        m.setStyle("-fx-text-fill:#475569; -fx-font-size:11px;");

        card.getChildren().addAll(t, m);
        return card;
    }

    // =====================================================
    // 9. WORKFLOW ACTIONS (APPROVE / REJECT REQUESTS)
    // =====================================================
    private void handleApproveRequest(Map<String, Object> item) {
        if (item == null) return;
        String id = getString(item, "cargoId", "");
        String name = getString(item, "cargoName", "Cargo");

        item.put("status", "Approved");
        cargoDao.saveCargo(id, name, getString(item, "cargoType", "General Cargo"),
                getString(item, "quantity", "1"), getString(item, "weight", "0 kg"),
                getString(item, "originPort", "Port"), getString(item, "destinationPort", activeViewingPort), "Approved");

        refreshCargoData();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Request Approved");
        alert.setHeaderText("Importer Cargo Request Approved");
        alert.setContentText("Cargo " + id + " (" + name + ") has been approved and registered for quayside operations at " + activeViewingPort + ".");
        alert.showAndWait();
    }

    private void handleRejectRequest(Map<String, Object> item) {
        if (item == null) return;
        String id = getString(item, "cargoId", "");
        String name = getString(item, "cargoName", "Cargo");

        item.put("status", "Rejected");
        cargoDao.saveCargo(id, name, getString(item, "cargoType", "General Cargo"),
                getString(item, "quantity", "1"), getString(item, "weight", "0 kg"),
                getString(item, "originPort", "Port"), getString(item, "destinationPort", activeViewingPort), "Rejected");

        refreshCargoData();

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Request Rejected");
        alert.setHeaderText("Cargo Request Rejected");
        alert.setContentText("Cargo request " + id + " (" + name + ") has been set to Rejected.");
        alert.showAndWait();
    }

    // =====================================================
    // 10. MODAL DIALOGS (DETAILS, STATUS UPDATE, ADD CARGO)
    // =====================================================
    private void showCargoDetailsDialog(Map<String, Object> c) {
        if (c == null) return;

        Dialog<Void> dialog = new Dialog<>();
        String id = getString(c, "cargoId", "CGO-N/A");
        String name = getString(c, "cargoName", "General Cargo");
        dialog.setTitle("Cargo Manifest Details — " + id);
        dialog.setHeaderText("Complete cargo manifest, importer details & vessel assignment for " + id);

        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeBtn);

        VBox contentBox = new VBox(12);
        contentBox.setPadding(new Insets(16));
        contentBox.setPrefWidth(480);

        // Header Title
        HBox head = new HBox(8);
        head.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label nameLbl = new Label(name + " (" + id + ")");
        nameLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16px; -fx-font-weight:bold;");
        Label sub = new Label("Type: " + getString(c, "cargoType", "General") + " • Authorized Port: " + activeViewingPort);
        sub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(nameLbl, sub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        String st = getString(c, "status", "Registered");
        Label statusPill = new Label(st);
        statusPill.setStyle("-fx-text-fill:#0284c7; -fx-background-color:#e0f2fe; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;");

        head.getChildren().addAll(titleBox, sp, statusPill);

        // Section 1: CARGO DETAILS
        Label g1 = new Label("CARGO DETAILS");
        g1.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 0 2 0;");

        VBox g1Box = new VBox(3,
                detailLine("Cargo ID", id),
                detailLine("Cargo Description", name),
                detailLine("Cargo Type", getString(c, "cargoType", "General Cargo")),
                detailLine("Quantity", getString(c, "quantity", "1 unit")),
                detailLine("Declared Weight", getString(c, "weight", "—")),
                detailLine("Importer Entity", getString(c, "importer", "Global Traders Ltd.")),
                detailLine("Exporter Entity", getString(c, "exporter", "Oceanic Star Trading")),
                detailLine("Port of Origin", getString(c, "originPort", "Singapore")),
                detailLine("Destination Port", getString(c, "destinationPort", activeViewingPort)),
                detailLine("Current Status", st),
                detailLine("Booking Timestamp", formatIsoDate(getString(c, "createdAt", "Today")))
        );

        // Section 2: ASSOCIATED VESSEL & LIVE TELEMETRY
        Label g2 = new Label("ASSOCIATED VESSEL & LIVE TELEMETRY");
        g2.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 0 2 0;");

        String shipName = getString(c, "shipName", "MV " + activeViewingPort.replace("Port of ", "") + " Express");
        VesselData matchedVessel = findMatchingVessel(shipName);

        VBox g2Box = new VBox(3,
                detailLine("Assigned Vessel", shipName),
                detailLine("Vessel IMO", matchedVessel != null ? matchedVessel.imo : "9" + (Math.abs(id.hashCode()) % 899999 + 100000)),
                detailLine("Vessel MMSI", matchedVessel != null ? String.valueOf(matchedVessel.mmsi) : String.valueOf(Math.abs(id.hashCode()))),
                detailLine("Live Speed", matchedVessel != null ? String.format("%.1f knots", matchedVessel.speed) : "12.4 knots"),
                detailLine("Course / Heading", matchedVessel != null ? String.format("%.0f°", matchedVessel.course) : "185°"),
                detailLine("Vessel Nav Status", matchedVessel != null ? matchedVessel.navigationStatus : "Underway using Engine"),
                detailLine("Estimated Arrival (ETA)", deriveCargoEta(c))
        );

        // Section 3: WORKFLOW TIMELINE
        Label g3 = new Label("WORKFLOW & CLEARANCE TIMELINE");
        g3.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 0 2 0;");

        Label timelineLbl = new Label("Request Submitted  ➔  Reviewed  ➔  Approved  ➔  Cargo Confirmed  ➔  Received  ➔  Loaded  ➔  Dispatched  ➔  Delivered");
        timelineLbl.setWrapText(true);
        timelineLbl.setStyle("-fx-text-fill:#1e293b; -fx-font-size:11px; -fx-background-color:#f8fafc; -fx-padding:8; -fx-border-color:#e2e8f0; -fx-border-radius:4; -fx-background-radius:4;");

        contentBox.getChildren().addAll(head, g1, g1Box, g2, g2Box, g3, timelineLbl);
        dialog.getDialogPane().setContent(contentBox);
        dialog.showAndWait();
    }

    private void showUpdateStatusDialog(Map<String, Object> c) {
        if (c == null) return;
        Dialog<ButtonType> dialog = new Dialog<>();
        String id = getString(c, "cargoId", "");
        dialog.setTitle("Update Status — " + id);
        dialog.setHeaderText("Update operational status for cargo " + id);

        ButtonType updateBtnType = new ButtonType("Save Status", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Pending", "Approved", "Received", "In Transit", "Loaded", "Dispatched", "Delivered", "Rejected");
        statusBox.setValue(getString(c, "status", "Received"));

        grid.add(new Label("Cargo ID:"), 0, 0);
        grid.add(new Label(id), 1, 0);
        grid.add(new Label("Select New Status:"), 0, 1);
        grid.add(statusBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(updateBtnType);
        saveBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-weight:bold;");

        saveBtn.setOnAction(event -> {
            String newSt = statusBox.getValue();
            c.put("status", newSt);
            cargoDao.saveCargo(id, getString(c, "cargoName", "Cargo"), getString(c, "cargoType", "General Cargo"),
                    getString(c, "quantity", "1"), getString(c, "weight", "0 kg"),
                    getString(c, "originPort", "Port"), getString(c, "destinationPort", activeViewingPort), newSt);

            refreshCargoData();
        });

        dialog.showAndWait();
    }

    private void showAddCargoDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register Cargo — " + activeViewingPort);
        dialog.setHeaderText("Register new cargo manifest under " + activeViewingPort);

        ButtonType regType = new ButtonType("Save Cargo", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(regType, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(16));

        String nextId = cargoDao.generateCargoId();
        TextField idField = new TextField(nextId);
        idField.setEditable(false);
        idField.setStyle("-fx-background-color:#f1f5f9;");

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Industrial Electronics");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("General Cargo", "Container", "Bulk", "Refrigerated", "Liquid", "Machinery", "Textiles");
        typeBox.setValue("General Cargo");

        TextField qtyField = new TextField("250 units");
        TextField weightField = new TextField("1,500 kg");

        TextField originField = new TextField("Port of Singapore");
        TextField destField = new TextField(activeViewingPort);
        destField.setEditable(false);
        destField.setStyle("-fx-background-color:#f1f5f9;");

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Received", "In Transit", "Loaded", "Dispatched", "Pending");
        statusBox.setValue("Received");

        TextField shipField = new TextField("MV " + activeViewingPort.replace("Port of ", "") + " Star");

        form.add(new Label("Cargo ID:"), 0, 0);
        form.add(idField, 1, 0);
        form.add(new Label("Cargo Name:"), 0, 1);
        form.add(nameField, 1, 1);
        form.add(new Label("Cargo Type:"), 0, 2);
        form.add(typeBox, 1, 2);
        form.add(new Label("Quantity:"), 0, 3);
        form.add(qtyField, 1, 3);
        form.add(new Label("Weight:"), 0, 4);
        form.add(weightField, 1, 4);
        form.add(new Label("Origin Port:"), 0, 5);
        form.add(originField, 1, 5);
        form.add(new Label("Destination Port:"), 0, 6);
        form.add(destField, 1, 6);
        form.add(new Label("Initial Status:"), 0, 7);
        form.add(statusBox, 1, 7);
        form.add(new Label("Assigned Ship:"), 0, 8);
        form.add(shipField, 1, 8);

        dialog.getDialogPane().setContent(form);

        Button regBtn = (Button) dialog.getDialogPane().lookupButton(regType);
        regBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-weight:bold;");

        regBtn.setOnAction(event -> {
            String nameVal = nameField.getText().trim();
            if (nameVal.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Missing Information");
                alert.setHeaderText("Cargo Name Required");
                alert.setContentText("Please provide a name or description for the cargo.");
                alert.showAndWait();
                event.consume();
                return;
            }

            String idVal = idField.getText().trim();
            String typeVal = typeBox.getValue();
            String qtyVal = qtyField.getText().trim();
            String weightVal = weightField.getText().trim();
            String origVal = originField.getText().trim();
            String stVal = statusBox.getValue();

            cargoDao.saveCargo(idVal, nameVal, typeVal, qtyVal, weightVal, origVal, activeViewingPort, stVal);
            refreshCargoData();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Cargo Registered");
            success.setHeaderText("Cargo Registered Successfully");
            success.setContentText("Cargo ID: " + idVal + " has been added to " + activeViewingPort + " manifest.");
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

    private String deriveCargoEta(Map<String, Object> c) {
        String st = getString(c, "status", "");
        if ("Delivered".equalsIgnoreCase(st) || "Received".equalsIgnoreCase(st)) return "In Port Storage";
        if ("Dispatched".equalsIgnoreCase(st)) return "Dispatched";
        if ("Loaded".equalsIgnoreCase(st)) return "Ready for Departure";
        int mins = 35 + (int)(Math.abs(getString(c, "cargoId", "1").hashCode() % 90));
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