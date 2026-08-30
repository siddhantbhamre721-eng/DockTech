package com.projectui.view.portauthority;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.projectui.controller.ShipController;
import com.projectui.model.Ship;
import com.projectui.service.VesselData;
import com.projectui.service.VesselFinderService;
import com.projectui.service.VesselManager;
import com.projectui.view.admin.AdminDataStore;

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
public class ShipManagementPage {

    private final VBox root = new VBox(16);

    // Backend Services (Shared Source of Truth with Admin & Live AIS)
    private final VesselManager vesselManager = VesselManager.getInstance();
    private final VesselFinderService vesselFinderService = VesselFinderService.getInstance();
    private final ShipController shipController = new ShipController();
    private final AdminDataStore adminDataStore = AdminDataStore.getInstance();

    // Authenticated Port Authority Context
    private String userAssignedPort = "Cochin Port";
    private String activeViewingPort = "Cochin Port";

    // Header & Telemetry Freshness UI
    private Label pageHeadingLabel;
    private Label pageSubLabel;
    private Label dataFreshnessBadge;
    private Label lastSyncLabel;

    // Operational Summary KPI Cards
    private Label totalShipsVal;
    private Label arrivingVal;
    private Label anchoredVal;
    private Label berthedVal;
    private Label departingVal;

    // Master & Filtered Vessels Data
    private final List<VesselData> masterPortVessels = new ArrayList<>();
    private final List<VesselData> filteredVessels = new ArrayList<>();
    private final ObservableList<VesselData> pagedVesselsList = FXCollections.observableArrayList();

    // Table & Controls
    private TableView<VesselData> shipTable;
    private TextField searchField;
    private ComboBox<String> typeFilterCombo;
    private ComboBox<String> statusFilterCombo;
    private ComboBox<String> sortCombo;

    // Pagination
    private int currentPage = 1;
    private final int pageSize = 10;
    private int totalPages = 1;
    private Label paginationInfoLabel;
    private Button prevPageBtn;
    private Button nextPageBtn;

    // State Tracking
    private long lastSuccessfulUpdateEpoch = System.currentTimeMillis();

    public ShipManagementPage() {
        root.setSpacing(16);
        root.setPadding(new Insets(0));
        root.setStyle("-fx-background-color:transparent;");

        resolveAssignedPort();
        buildLayout();
        connectLiveAISService();
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
    // 2. LAYOUT COMPOSITION
    // =====================================================
    private void buildLayout() {
        VBox header = buildHeader();
        HBox summaryCards = buildSummaryCards();
        VBox mainTableSection = buildTableSection();

        root.getChildren().addAll(
                header,
                summaryCards,
                mainTableSection
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
        pageHeadingLabel = new Label("Ship Management");
        pageHeadingLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:22px; -fx-font-weight:bold;");

        pageSubLabel = new Label("Live vessel operations, telemetry tracking & quayside management for " + activeViewingPort);
        pageSubLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:13px;");
        titleBox.getChildren().addAll(pageHeadingLabel, pageSubLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Freshness Indicator
        dataFreshnessBadge = new Label("● LIVE — AIS Active");
        dataFreshnessBadge.setStyle("-fx-text-fill:#10b981; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(16,185,129,0.12); -fx-padding:6 12; -fx-background-radius:999;");

        lastSyncLabel = new Label("Synced: Just now");
        lastSyncLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px;");

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 14; -fx-cursor:hand;");
        refreshBtn.setOnAction(e -> refreshShipData());

        topRow.getChildren().addAll(titleBox, spacer, dataFreshnessBadge, lastSyncLabel, refreshBtn);
        container.getChildren().add(topRow);
        return container;
    }

    // =====================================================
    // 4. SUMMARY KPI CARDS
    // =====================================================
    private HBox buildSummaryCards() {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);

        totalShipsVal = new Label("0");
        arrivingVal = new Label("0");
        anchoredVal = new Label("0");
        berthedVal = new Label("0");
        departingVal = new Label("0");

        VBox cTotal = createKpiCard("Total Ships", totalShipsVal, "Assigned to " + activeViewingPort, "🚢", "#0284c7", "#e0f2fe");
        VBox cArriving = createKpiCard("Arriving", arrivingVal, "Inbound Channel Approach", "📥", "#3b82f6", "#eff6ff");
        VBox cAnchored = createKpiCard("Anchored", anchoredVal, "Outer Roads / Waiting", "⚓", "#d97706", "#fef3c7");
        VBox cBerthed = createKpiCard("Berthed / Moored", berthedVal, "Active Quayside Operations", "🏢", "#16a34a", "#ecfdf5");
        VBox cDeparting = createKpiCard("Departing", departingVal, "Outbound Cleared Vessels", "📤", "#8b5cf6", "#f5f3ff");

        row.getChildren().addAll(cTotal, cArriving, cAnchored, cBerthed, cDeparting);

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
    // 5. MAIN LIVE SHIP TABLE & CONTROLS SECTION
    // =====================================================
    private VBox buildTableSection() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        // Controls Bar
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label tblTitle = new Label("Live Ship Operations");
        tblTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:15px; -fx-font-weight:bold;");
        Label tblSub = new Label("Live AIS telemetry & registered vessel operations for " + activeViewingPort);
        tblSub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(tblTitle, tblSub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("🔍 Search Ship / IMO / MMSI / Call Sign...");
        searchField.setPrefWidth(260);
        searchField.setPrefHeight(34);
        searchField.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-padding:5 10;");
        searchField.textProperty().addListener((obs, ov, nv) -> applyFiltersAndPagination());

        typeFilterCombo = new ComboBox<>();
        typeFilterCombo.getItems().addAll("All Types", "Container", "Tanker", "Bulk Carrier", "General Cargo", "Passenger", "Other");
        typeFilterCombo.setValue("All Types");
        typeFilterCombo.setPrefHeight(34);
        typeFilterCombo.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-pref-width:130;");
        typeFilterCombo.setOnAction(e -> applyFiltersAndPagination());

        statusFilterCombo = new ComboBox<>();
        statusFilterCombo.getItems().addAll("All Status", "Arriving", "Anchored", "Berthed", "Departing", "Underway");
        statusFilterCombo.setValue("All Status");
        statusFilterCombo.setPrefHeight(34);
        statusFilterCombo.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-pref-width:130;");
        statusFilterCombo.setOnAction(e -> applyFiltersAndPagination());

        sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Sort: Name (A-Z)", "Sort: Speed (High-Low)", "Sort: Status", "Sort: ETA");
        sortCombo.setValue("Sort: Name (A-Z)");
        sortCombo.setPrefHeight(34);
        sortCombo.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-pref-width:145;");
        sortCombo.setOnAction(e -> applyFiltersAndPagination());

        Button registerShipBtn = new Button("+ Register Ship");
        registerShipBtn.setPrefHeight(34);
        registerShipBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 14; -fx-cursor:hand;");
        registerShipBtn.setOnAction(e -> showRegisterShipDialog());

        controls.getChildren().addAll(titleBox, sp, searchField, typeFilterCombo, statusFilterCombo, sortCombo, registerShipBtn);

        // Table
        shipTable = new TableView<>();
        shipTable.setPrefHeight(450);
        shipTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        shipTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        shipTable.setPlaceholder(new Label("No ships currently registered or operating for " + activeViewingPort));

        TableColumn<VesselData, String> colVessel = new TableColumn<>("Ship Name");
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
        colImo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().imo != null && !d.getValue().imo.isBlank() ? d.getValue().imo : "N/A"));
        colImo.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<VesselData, String> colMmsi = new TableColumn<>("MMSI");
        colMmsi.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().mmsi)));
        colMmsi.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<VesselData, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().shipType != null ? d.getValue().shipType : "N/A"));
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
                    badge.setStyle("-fx-text-fill:" + color + "; -fx-background-color:" + bg + "; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;");
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
        colDest.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().destination != null ? d.getValue().destination : "N/A"));
        colDest.setStyle("-fx-font-size:12px; -fx-text-fill:#475569;");

        TableColumn<VesselData, String> colEta = new TableColumn<>("ETA");
        colEta.setCellValueFactory(d -> new SimpleStringProperty(deriveEta(d.getValue())));
        colEta.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-text-fill:#2563eb; -fx-font-weight:bold;");

        TableColumn<VesselData, String> colUpdate = new TableColumn<>("Last Update");
        colUpdate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().timestamp != null ? d.getValue().timestamp : "N/A"));
        colUpdate.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<VesselData, String> colAction = new TableColumn<>("Action");
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button detailsBtn = new Button("View Details");
            {
                detailsBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:5; -fx-padding:4 10; -fx-cursor:hand;");
                detailsBtn.setOnAction(e -> {
                    VesselData v = getTableView().getItems().get(getIndex());
                    showShipDetailsDialog(v);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(detailsBtn);
                setAlignment(Pos.CENTER);
            }
        });

        shipTable.getColumns().addAll(colVessel, colImo, colMmsi, colType, colStatus, colSpeed, colCourse, colDest, colEta, colUpdate, colAction);
        shipTable.setItems(pagedVesselsList);

        shipTable.setRowFactory(tv -> {
            TableRow<VesselData> row = new TableRow<>();
            row.setPrefHeight(38);
            row.setStyle("-fx-cursor:hand;");
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showShipDetailsDialog(row.getItem());
                }
            });
            return row;
        });

        // Pagination Bar
        HBox paginationBar = buildPaginationBar();

        container.getChildren().addAll(controls, shipTable, paginationBar);
        return container;
    }

    private HBox buildPaginationBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(8, 0, 0, 0));

        paginationInfoLabel = new Label("Showing 0 of 0 ships");
        paginationInfoLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");

        prevPageBtn = new Button("⟨ Previous");
        prevPageBtn.setStyle("-fx-background-color:#f1f5f9; -fx-border-color:#cbd5e1; -fx-border-radius:5; -fx-background-radius:5; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-cursor:hand;");
        prevPageBtn.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                updatePagedView();
            }
        });

        nextPageBtn = new Button("Next ⟩");
        nextPageBtn.setStyle("-fx-background-color:#f1f5f9; -fx-border-color:#cbd5e1; -fx-border-radius:5; -fx-background-radius:5; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-cursor:hand;");
        nextPageBtn.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                updatePagedView();
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(paginationInfoLabel, spacer, prevPageBtn, nextPageBtn);
        return bar;
    }

    // =====================================================
    // 6. DATA FILTERING, SORTING & PAGINATION
    // =====================================================
    private void applyFiltersAndPagination() {
        String query = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String statusFilter = statusFilterCombo != null && statusFilterCombo.getValue() != null ? statusFilterCombo.getValue() : "All Status";
        String typeFilter = typeFilterCombo != null && typeFilterCombo.getValue() != null ? typeFilterCombo.getValue() : "All Types";
        String sortOption = sortCombo != null && sortCombo.getValue() != null ? sortCombo.getValue() : "Sort: Name (A-Z)";

        filteredVessels.clear();

        for (VesselData v : masterPortVessels) {
            if (v == null) continue;

            boolean matchesSearch = query.isEmpty()
                    || (v.name != null && v.name.toLowerCase().contains(query))
                    || (v.imo != null && v.imo.toLowerCase().contains(query))
                    || String.valueOf(v.mmsi).contains(query)
                    || (v.callSign != null && v.callSign.toLowerCase().contains(query))
                    || (v.shipType != null && v.shipType.toLowerCase().contains(query))
                    || (v.destination != null && v.destination.toLowerCase().contains(query));

            if (!matchesSearch) continue;

            if (!"All Status".equalsIgnoreCase(statusFilter)) {
                String cat = deriveVesselCategory(v);
                if ("Berthed".equalsIgnoreCase(statusFilter)) {
                    if (!"Berthed".equalsIgnoreCase(cat) && !"Moored".equalsIgnoreCase(cat)) continue;
                } else if (!cat.equalsIgnoreCase(statusFilter)) {
                    continue;
                }
            }

            if (!"All Types".equalsIgnoreCase(typeFilter)) {
                String t = v.shipType != null ? v.shipType.toLowerCase() : "";
                String target = typeFilter.toLowerCase();
                if (!t.contains(target)) continue;
            }

            filteredVessels.add(v);
        }

        // Apply Sorting
        if ("Sort: Speed (High-Low)".equalsIgnoreCase(sortOption)) {
            filteredVessels.sort((a, b) -> Double.compare(b.speed, a.speed));
        } else if ("Sort: Status".equalsIgnoreCase(sortOption)) {
            filteredVessels.sort(Comparator.comparing(this::deriveVesselCategory));
        } else if ("Sort: ETA".equalsIgnoreCase(sortOption)) {
            filteredVessels.sort(Comparator.comparing(this::deriveEta));
        } else {
            filteredVessels.sort(Comparator.comparing(v -> v.name != null ? v.name : ""));
        }

        currentPage = 1;
        updatePagedView();
    }

    private void updatePagedView() {
        int total = filteredVessels.size();
        totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));

        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);

        List<VesselData> sublist = (total > 0 && startIndex < total)
                ? filteredVessels.subList(startIndex, endIndex)
                : new ArrayList<>();

        pagedVesselsList.setAll(sublist);

        if (paginationInfoLabel != null) {
            if (total == 0) {
                paginationInfoLabel.setText("Showing 0 ships");
            } else {
                paginationInfoLabel.setText(String.format("Showing %d–%d of %d ships (Page %d of %d)",
                        startIndex + 1, endIndex, total, currentPage, totalPages));
            }
        }

        if (prevPageBtn != null) prevPageBtn.setDisable(currentPage <= 1);
        if (nextPageBtn != null) nextPageBtn.setDisable(currentPage >= totalPages);
    }

    // =====================================================
    // 7. LIVE AIS DATA SYNC & LOAD
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

    public void refreshShipData() {
        loadPortVesselsFromSource();
        updateFreshnessBadge();
    }

    private void updateFreshnessBadge() {
        if (dataFreshnessBadge == null) return;
        long diffSec = (System.currentTimeMillis() - lastSuccessfulUpdateEpoch) / 1000;

        if (vesselFinderService.isRateLimited()) {
            dataFreshnessBadge.setText("⚠ AIS RATE LIMITED (CACHED)");
            dataFreshnessBadge.setStyle("-fx-text-fill:#d97706; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(217,119,6,0.12); -fx-padding:6 12; -fx-background-radius:999;");
        } else if (!vesselFinderService.isConnected() && masterPortVessels.isEmpty()) {
            dataFreshnessBadge.setText("⚠ CONNECTION ISSUE");
            dataFreshnessBadge.setStyle("-fx-text-fill:#ef4444; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(239,68,68,0.12); -fx-padding:6 12; -fx-background-radius:999;");
        } else if (diffSec > 180) {
            dataFreshnessBadge.setText("⚠ STALE (" + (diffSec / 60) + "m ago)");
            dataFreshnessBadge.setStyle("-fx-text-fill:#f59e0b; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(245,158,11,0.12); -fx-padding:6 12; -fx-background-radius:999;");
        } else {
            dataFreshnessBadge.setText("● LIVE — Updated " + Math.max(1, diffSec) + "s ago");
            dataFreshnessBadge.setStyle("-fx-text-fill:#10b981; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(16,185,129,0.12); -fx-padding:6 12; -fx-background-radius:999;");
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

        // Also incorporate any persistent ships registered under this port
        try {
            List<Ship> registeredShips = shipController.getShips();
            if (registeredShips != null) {
                for (Ship s : registeredShips) {
                    if (s == null) continue;
                    boolean belongs = isRegisteredShipForPort(s, activeViewingPort);
                    if (belongs) {
                        long mmsi = Math.abs((s.getShipId() != null ? s.getShipId() : s.getShipName()).hashCode());
                        boolean alreadyPresent = false;
                        for (VesselData v : matchedPortVessels) {
                            if (v.mmsi == mmsi || (s.getImoNumber() != null && s.getImoNumber().equalsIgnoreCase(v.imo))) {
                                alreadyPresent = true;
                                break;
                            }
                        }
                        if (!alreadyPresent) {
                            double[] coords = getPortCoordinates(activeViewingPort);
                            VesselData regVessel = new VesselData(
                                    mmsi,
                                    s.getImoNumber(),
                                    s.getShipName(),
                                    "V-" + (mmsi % 9000 + 1000),
                                    coords[0],
                                    coords[1],
                                    "Arriving".equalsIgnoreCase(s.getStatus()) ? 6.5 : ("Departing".equalsIgnoreCase(s.getStatus()) ? 11.2 : 0.0),
                                    90.0,
                                    90.0,
                                    activeViewingPort,
                                    s.getStatus(),
                                    s.getShipType(),
                                    s.getArrival() != null ? s.getArrival() : "Just now"
                            );
                            matchedPortVessels.add(regVessel);
                            vesselManager.updateOrAddVessel(regVessel);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        masterPortVessels.clear();
        masterPortVessels.addAll(matchedPortVessels);

        updateKpiCounts(masterPortVessels);
        applyFiltersAndPagination();
    }

    private boolean isRegisteredShipForPort(Ship s, String portName) {
        if (s == null || portName == null) return false;
        String pName = portName.toLowerCase();
        String berth = s.getBerth() != null ? s.getBerth().toLowerCase() : "";
        String comp = s.getCompany() != null ? s.getCompany().toLowerCase() : "";
        String shipId = s.getShipId() != null ? s.getShipId().toLowerCase() : "";

        if (berth.contains(pName) || comp.contains(pName) || shipId.contains(pName)) return true;

        if (pName.contains("cochin") || pName.contains("kochi")) {
            return comp.contains("cochin") || comp.contains("kochi") || berth.contains("cochin") || berth.contains("cok");
        } else if (pName.contains("mumbai")) {
            return comp.contains("mumbai") || comp.contains("jnpt") || berth.contains("mum");
        } else if (pName.contains("chennai")) {
            return comp.contains("chennai") || comp.contains("madras") || berth.contains("maa");
        } else if (pName.contains("vizag")) {
            return comp.contains("vizag") || comp.contains("visakhapatnam") || berth.contains("vtz");
        } else if (pName.contains("kolkata")) {
            return comp.contains("kolkata") || comp.contains("calcutta") || berth.contains("ccu");
        }
        return true;
    }

    private boolean isVesselAssociatedWithPort(VesselData v, String portName) {
        if (v == null || portName == null) return false;
        String pName = portName.toLowerCase();

        String dest = v.destination != null ? v.destination.toLowerCase() : "";
        if (pName.contains("cochin") || pName.contains("kochi")) {
            if (dest.contains("cochin") || dest.contains("kochi") || dest.contains("cok")) return true;
        } else if (pName.contains("mumbai")) {
            if (dest.contains("mumbai") || dest.contains("jnpt") || dest.contains("nhava") || dest.contains("mum")) return true;
        } else if (pName.contains("chennai")) {
            if (dest.contains("chennai") || dest.contains("madras") || dest.contains("maa")) return true;
        } else if (pName.contains("vizag") || pName.contains("visakhapatnam")) {
            if (dest.contains("vizag") || dest.contains("visakhapatnam") || dest.contains("vtz")) return true;
        } else if (pName.contains("kolkata")) {
            if (dest.contains("kolkata") || dest.contains("calcutta") || dest.contains("ccu") || dest.contains("haldia")) return true;
        }

        double[] coords = getPortCoordinates(portName);
        double dLat = Math.abs(v.latitude - coords[0]);
        double dLng = Math.abs(v.longitude - coords[1]);

        return (dLat <= 2.2 && dLng <= 2.2);
    }

    private double[] getPortCoordinates(String portName) {
        String p = portName.toLowerCase();
        if (p.contains("cochin") || p.contains("kochi")) return new double[]{9.9600, 76.2600};
        if (p.contains("mumbai")) return new double[]{18.9439, 72.8354};
        if (p.contains("chennai")) return new double[]{13.0827, 80.2707};
        if (p.contains("vizag") || p.contains("visakhapatnam")) return new double[]{17.6868, 83.2185};
        if (p.contains("kolkata")) return new double[]{22.5726, 88.3639};
        return new double[]{9.9600, 76.2600};
    }

    private String deriveVesselCategory(VesselData v) {
        if (v == null) return "Underway";
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
        if (v == null) return "—";
        String cat = deriveVesselCategory(v);
        if ("Berthed".equalsIgnoreCase(cat) || "Moored".equalsIgnoreCase(cat)) return "Berthed in Port";
        if ("Anchored".equalsIgnoreCase(cat)) return "At Anchor";
        if ("Departing".equalsIgnoreCase(cat)) return "Outbound";
        int mins = 20 + (int)(Math.abs(v.mmsi % 95));
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
            if ("Arriving".equalsIgnoreCase(cat)) arriving++;
            else if ("Anchored".equalsIgnoreCase(cat)) anchored++;
            else if ("Berthed".equalsIgnoreCase(cat) || "Moored".equalsIgnoreCase(cat)) berthed++;
            else if ("Departing".equalsIgnoreCase(cat)) departing++;
            else {
                if (v.speed > 8.0) arriving++;
                else anchored++;
            }
        }

        if (totalShipsVal != null) totalShipsVal.setText(String.valueOf(total));
        if (arrivingVal != null) arrivingVal.setText(String.valueOf(arriving));
        if (anchoredVal != null) anchoredVal.setText(String.valueOf(anchored));
        if (berthedVal != null) berthedVal.setText(String.valueOf(berthed));
        if (departingVal != null) departingVal.setText(String.valueOf(departing));
    }

    // =====================================================
    // 8. SHIP DETAILS MODAL DIALOG
    // =====================================================
    private void showShipDetailsDialog(VesselData v) {
        if (v == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Vessel Particulars & Telemetry — " + v.name);
        dialog.setHeaderText("Complete live telemetry, AIS parameters and registered specs for " + v.name);

        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeBtn);

        VBox contentBox = new VBox(12);
        contentBox.setPadding(new Insets(16));
        contentBox.setPrefWidth(460);

        // Header Title
        HBox head = new HBox(8);
        head.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label nameLbl = new Label(v.name != null ? v.name : "Vessel #" + v.mmsi);
        nameLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16px; -fx-font-weight:bold;");
        Label flagType = new Label((v.shipType != null ? v.shipType : "Merchant Vessel") + " • Flag: India 🇮🇳 • Port: " + activeViewingPort);
        flagType.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(nameLbl, flagType);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        String cat = deriveVesselCategory(v);
        Label statusPill = new Label(cat);
        statusPill.setStyle("-fx-text-fill:#0284c7; -fx-background-color:#e0f2fe; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;");

        head.getChildren().addAll(titleBox, sp, statusPill);

        // Section 1: VESSEL DETAILS
        Label g1 = new Label("VESSEL DETAILS");
        g1.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 0 2 0;");

        VBox g1Box = new VBox(3,
                detailLine("Vessel Name", v.name),
                detailLine("IMO Number", v.imo),
                detailLine("MMSI", String.valueOf(v.mmsi)),
                detailLine("Call Sign", v.callSign),
                detailLine("Vessel Type", v.shipType),
                detailLine("Operating Port", activeViewingPort),
                detailLine("Flag State", "India 🇮🇳")
        );

        // Section 2: LIVE TELEMETRY
        Label g2 = new Label("LIVE TELEMETRY");
        g2.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 0 2 0;");

        VBox g2Box = new VBox(3,
                detailLine("Navigation Status", v.navigationStatus),
                detailLine("Current Speed", String.format("%.1f knots", v.speed)),
                detailLine("Course over Ground", String.format("%.1f°", v.course)),
                detailLine("True Heading", String.format("%.1f°", v.heading)),
                detailLine("Latitude Coordinate", String.format("%.4f° N", v.latitude)),
                detailLine("Longitude Coordinate", String.format("%.4f° E", v.longitude)),
                detailLine("Destination Port", v.destination),
                detailLine("Estimated Arrival (ETA)", deriveEta(v)),
                detailLine("Telemetry Last Update", v.timestamp)
        );

        // Section 3: VESSEL PARTICULARS
        Label g3 = new Label("VESSEL PARTICULARS");
        g3.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 0 2 0;");

        long seed = Math.abs(v.mmsi);
        int len = 180 + (int)(seed % 140);
        int beam = 25 + (int)(seed % 15);
        double draught = 8.0 + (seed % 50) / 10.0;
        int tonnage = 20000 + (int)(seed % 30000);

        VBox g3Box = new VBox(3,
                detailLine("Overall Length (LOA)", len + " m"),
                detailLine("Beam (Breadth)", beam + " m"),
                detailLine("Maximum Draught", String.format("%.1f m", draught)),
                detailLine("Gross Tonnage", String.format("%,d GT", tonnage))
        );

        contentBox.getChildren().addAll(head, g1, g1Box, g2, g2Box, g3, g3Box);
        dialog.getDialogPane().setContent(contentBox);
        dialog.showAndWait();
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
    // 9. REGISTER NEW SHIP DIALOG (BOUND TO LOGGED IN PORT)
    // =====================================================
    private void showRegisterShipDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register Vessel — " + activeViewingPort);
        dialog.setHeaderText("Register new vessel authorized under " + activeViewingPort);

        ButtonType regType = new ButtonType("Register Ship", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(regType, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(16));

        TextField shipIdField = new TextField("SHP-" + (int)(Math.random() * 899 + 100));
        TextField shipNameField = new TextField();
        shipNameField.setPromptText("e.g. MV Cochin Star");

        ComboBox<String> shipTypeField = new ComboBox<>();
        shipTypeField.getItems().addAll("Container", "Tanker", "Bulk Carrier", "General Cargo", "Passenger", "Other");
        shipTypeField.setValue("Container");

        ComboBox<String> statusField = new ComboBox<>();
        statusField.getItems().addAll("Arriving", "Berthed", "Anchored", "Departing");
        statusField.setValue("Arriving");

        TextField imoField = new TextField("9" + (int)(Math.random() * 899999 + 100000));
        TextField mmsiField = new TextField(String.valueOf((int)(Math.random() * 899999999 + 100000000)));

        TextField companyField = new TextField(activeViewingPort + " Lines");
        TextField berthField = new TextField("Berth B-" + (int)(Math.random() * 12 + 1));

        TextField portField = new TextField(activeViewingPort);
        portField.setEditable(false);
        portField.setStyle("-fx-background-color:#f1f5f9;");

        form.add(new Label("Ship ID:"), 0, 0);
        form.add(shipIdField, 1, 0);
        form.add(new Label("Ship Name:"), 0, 1);
        form.add(shipNameField, 1, 1);
        form.add(new Label("Ship Type:"), 0, 2);
        form.add(shipTypeField, 1, 2);
        form.add(new Label("Initial Status:"), 0, 3);
        form.add(statusField, 1, 3);
        form.add(new Label("IMO Number:"), 0, 4);
        form.add(imoField, 1, 4);
        form.add(new Label("MMSI:"), 0, 5);
        form.add(mmsiField, 1, 5);
        form.add(new Label("Operator / Company:"), 0, 6);
        form.add(companyField, 1, 6);
        form.add(new Label("Assigned Berth:"), 0, 7);
        form.add(berthField, 1, 7);
        form.add(new Label("Authorized Port:"), 0, 8);
        form.add(portField, 1, 8);

        dialog.getDialogPane().setContent(form);

        Button regBtn = (Button) dialog.getDialogPane().lookupButton(regType);
        regBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-weight:bold;");

        regBtn.setOnAction(event -> {
            String shipId = shipIdField.getText().trim();
            String shipName = shipNameField.getText().trim();
            String shipType = shipTypeField.getValue();
            String status = statusField.getValue();
            String imo = imoField.getText().trim();
            String company = companyField.getText().trim();
            String berth = berthField.getText().trim();
            String mmsiStr = mmsiField.getText().trim();

            if (shipId.isEmpty() || shipName.isEmpty() || imo.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Incomplete Information");
                alert.setHeaderText("Missing Required Fields");
                alert.setContentText("Please provide Ship Name, IMO, and ID.");
                alert.showAndWait();
                event.consume();
                return;
            }

            long mmsi = 0;
            try {
                mmsi = Long.parseLong(mmsiStr);
            } catch (Exception e) {
                mmsi = Math.abs(shipId.hashCode());
            }

            String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm · dd MMM"));

            // 1. Save to Firestore / ShipDAO
            Ship ship = new Ship(
                    shipId,
                    shipName,
                    shipType,
                    imo,
                    company,
                    nowStr,
                    "—",
                    berth,
                    status
            );
            shipController.saveShip(ship);

            // 2. Register into Live VesselManager
            double[] coords = getPortCoordinates(activeViewingPort);
            VesselData newVessel = new VesselData(
                    mmsi,
                    imo,
                    shipName,
                    "V-" + (mmsi % 9000 + 1000),
                    coords[0],
                    coords[1],
                    "Arriving".equalsIgnoreCase(status) ? 6.5 : ("Departing".equalsIgnoreCase(status) ? 11.2 : 0.0),
                    90.0,
                    90.0,
                    activeViewingPort,
                    status,
                    shipType,
                    nowStr
            );
            vesselManager.updateOrAddVessel(newVessel);

            // 3. Refresh View
            loadPortVesselsFromSource();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Ship Registered");
            success.setHeaderText("Vessel Registration Successful");
            success.setContentText(shipName + " (IMO: " + imo + ") is now active under " + activeViewingPort + ".");
            success.showAndWait();
        });

        dialog.showAndWait();
    }

    // =====================================================
    // 10. GET ROOT
    // =====================================================
    public VBox getRoot() {
        return root;
    }
}
