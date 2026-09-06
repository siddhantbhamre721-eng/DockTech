package com.projectui.view.portauthority;

import com.projectui.dao.NotificationDao;
import com.projectui.dao.ShipDAO;
import com.projectui.dao.UserDao;
import com.projectui.model.Notification;
import com.projectui.model.Ship;
import com.projectui.service.VesselData;
import com.projectui.service.VesselManager;
import com.projectui.view.login.LoginPage;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SuppressWarnings({ "unchecked", "unused" })
public class ManageShipCaptainPage {

    private final VBox root = new VBox(16);

    // Port Context
    private String userAssignedPort = "Port of Mumbai";
    private String activeViewingPort = "Port of Mumbai";

    // Services
    private final ShipDAO shipDAO = new ShipDAO();
    private final VesselManager vesselManager = VesselManager.getInstance();

    // Summary Metric Labels (Calculated 100% dynamically from live data)
    private Label totalCaptainsVal;
    private Label activeCaptainsVal;
    private Label assignedCaptainsVal;
    private Label availCaptainsVal;
    private Label activeDutiesVal;

    // Header Freshness Labels
    private Label dataFreshnessBadge;
    private Label lastSyncLabel;

    // Captain Data
    private final ObservableList<Map<String, Object>> masterCaptainsList = FXCollections.observableArrayList();
    private FilteredList<Map<String, Object>> filteredCaptainsList;
    private TableView<Map<String, Object>> captainTable;
    private TextField captainSearchField;
    private ComboBox<String> captainStatusFilter;
    private ComboBox<String> captainAssignFilter;
    private ComboBox<String> captainSortCombo;

    // Active Ship Captain Assignments Data
    private final ObservableList<Map<String, Object>> activeAssignmentsList = FXCollections.observableArrayList();
    private TableView<Map<String, Object>> activeAssignmentsTable;

    // Live Ship Reference Cache (Used solely for assignment dropdowns and voyage matching)
    private final List<Map<String, Object>> masterShipsList = new ArrayList<>();

    public ManageShipCaptainPage() {
        root.setSpacing(16);
        root.setPadding(new Insets(0));
        root.setStyle("-fx-background-color:transparent;");

        resolveAssignedPort();
        buildLayout();
        refreshAllData();
    }

    // =====================================================
    // 1. PORT CONTEXT RESOLUTION
    // =====================================================
    private void resolveAssignedPort() {
        String port = null;
        if (LoginPage.loggedInUser != null) {
            Map<String, Object> user = LoginPage.loggedInUser;
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
            userAssignedPort = UserDao.normalizePort(port);
        } else {
            userAssignedPort = "Port of Mumbai";
        }
        activeViewingPort = userAssignedPort;
    }

    private String getDisplayPortName() {
        if (activeViewingPort == null || activeViewingPort.isBlank()) {
            return "PORT OF MUMBAI";
        }
        String up = activeViewingPort.toUpperCase().trim();
        if (!up.startsWith("PORT OF ") && !up.contains("PORT")) {
            return "PORT OF " + up;
        }
        return up;
    }

    // =====================================================
    // 2. MAIN LAYOUT
    // =====================================================
    private void buildLayout() {
        VBox header = buildHeader();
        HBox summaryCards = buildSummaryCards();
        VBox captainSection = buildCaptainManagementSection();
        VBox assignmentsSection = buildActiveAssignmentsSection();

        root.getChildren().addAll(
                header,
                summaryCards,
                captainSection,
                assignmentsSection);
    }

    // =====================================================
    // 3. TOP HEADER
    // =====================================================
    private VBox buildHeader() {
        VBox container = new VBox(4);

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label pageHeadingLabel = new Label("Manage Ship Captains");
        pageHeadingLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:22px; -fx-font-weight:bold;");

        Label pageSubLabel = new Label("Manage registered ship captains and their current assignments/duties for " + getDisplayPortName());
        pageSubLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:13px;");
        titleBox.getChildren().addAll(pageHeadingLabel, pageSubLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        dataFreshnessBadge = new Label("● LIVE — Synced");
        dataFreshnessBadge.setStyle(
                "-fx-text-fill:#10b981; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(16,185,129,0.12); -fx-padding:6 12; -fx-background-radius:999;");

        lastSyncLabel = new Label("Synced: Just now");
        lastSyncLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px;");

        Button refreshBtn = new Button("⟳ Refresh Personnel Data");
        refreshBtn.setStyle(
                "-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:7 14; -fx-cursor:hand;");
        refreshBtn.setOnAction(e -> refreshAllData());

        topRow.getChildren().addAll(titleBox, spacer, dataFreshnessBadge, lastSyncLabel, refreshBtn);
        container.getChildren().add(topRow);
        return container;
    }

    // =====================================================
    // 4. SUMMARY CARDS (LIVE BACKEND DATA ONLY)
    // =====================================================
    private HBox buildSummaryCards() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        totalCaptainsVal = new Label("0");
        activeCaptainsVal = new Label("0");
        assignedCaptainsVal = new Label("0");
        availCaptainsVal = new Label("0");
        activeDutiesVal = new Label("0");

        VBox c1 = createSummaryCard("REGISTERED CAPTAINS", totalCaptainsVal, "Total registered master mariners", "⚓",
                "#6366f1", "#e0e7ff");
        VBox c2 = createSummaryCard("ACTIVE CAPTAINS", activeCaptainsVal, "Certified active duty status", "🟢",
                "#16a34a", "#ecfdf5");
        VBox c3 = createSummaryCard("ASSIGNED CAPTAINS", assignedCaptainsVal, "Vessel command or port duty", "🔵",
                "#0284c7", "#e0f2fe");
        VBox c4 = createSummaryCard("AVAILABLE CAPTAINS", availCaptainsVal, "Standby for deployment", "⏳",
                "#d97706", "#fef3c7");
        VBox c5 = createSummaryCard("ACTIVE DUTIES", activeDutiesVal, "Operational duties underway", "📋",
                "#0d9488", "#ccfbf1");

        row.getChildren().addAll(c1, c2, c3, c4, c5);

        for (Node n : row.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
            if (n instanceof VBox) {
                ((VBox) n).setMaxWidth(Double.MAX_VALUE);
            }
        }
        return row;
    }

    private VBox createSummaryCard(String title, Label valLabel, String subtitle, String iconStr, String color,
            String bg) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle(
                "-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.04), 4, 0, 0, 1);");

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label iconBadge = new Label(iconStr);
        iconBadge.setStyle("-fx-font-size:13px; -fx-background-color:" + bg + "; -fx-text-fill:" + color
                + "; -fx-padding:4 7; -fx-background-radius:6;");

        Label tLabel = new Label(title);
        tLabel.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#64748b;");
        top.getChildren().addAll(iconBadge, tLabel);

        valLabel.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:" + color + ";");

        Label subLabel = new Label(subtitle);
        subLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8;");

        card.getChildren().addAll(top, valLabel, subLabel);
        return card;
    }

    // =====================================================
    // 5. REGISTERED SHIP CAPTAINS SECTION & TABLE
    // =====================================================
    private VBox buildCaptainManagementSection() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        box.setStyle(
                "-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 4, 0, 0, 1);");

        // Title Row
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("REGISTERED SHIP CAPTAINS");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14px; -fx-font-weight:bold;");

        Label sub = new Label(
                "Registered captains and their current ship and duty assignments for " + getDisplayPortName());
        sub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button regCapBtn = new Button("+ Register Captain");
        regCapBtn.setStyle(
                "-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 12; -fx-cursor:hand;");
        regCapBtn.setOnAction(e -> showRegisterCaptainDialog());

        titleRow.getChildren().addAll(titleBox, spacer, regCapBtn);

        // Search & Filter Row
        HBox filterRow = new HBox(10);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        captainSearchField = new TextField();
        captainSearchField.setPromptText("Search Captain / User ID / Email...");
        captainSearchField.setPrefWidth(260);
        captainSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyCaptainFilters());

        captainStatusFilter = new ComboBox<>();
        captainStatusFilter.getItems().addAll("All Statuses", "Active", "Pending", "Inactive");
        captainStatusFilter.setValue("All Statuses");
        captainStatusFilter.setOnAction(e -> applyCaptainFilters());

        captainAssignFilter = new ComboBox<>();
        captainAssignFilter.getItems().addAll("All Assignments", "Assigned", "Available / Unassigned");
        captainAssignFilter.setValue("All Assignments");
        captainAssignFilter.setOnAction(e -> applyCaptainFilters());

        captainSortCombo = new ComboBox<>();
        captainSortCombo.getItems().addAll("Sort by: Name", "Sort by: User ID", "Sort by: Status", "Sort by: Ship");
        captainSortCombo.setValue("Sort by: Name");
        captainSortCombo.setOnAction(e -> applyCaptainFilters());

        filterRow.getChildren().addAll(
                new Label("Filter:"),
                captainSearchField,
                captainStatusFilter,
                captainAssignFilter,
                captainSortCombo);

        // Captain Table
        captainTable = new TableView<>();
        var capCss = getClass().getResource("/table-headers.css");
        if (capCss != null) {
            captainTable.getStylesheets().add(capCss.toExternalForm());
        }
        captainTable.setPrefHeight(260);
        captainTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        captainTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        
        Label emptyLabel = new Label("No registered Ship Captains found for " + getDisplayPortName() + ".");
        emptyLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13px;");
        captainTable.setPlaceholder(emptyLabel);

        TableColumn<Map<String, Object>, String> colName = new TableColumn<>("Captain Name");
        colName.setPrefWidth(140);
        colName.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("name"), "Ship Captain")));
        colName.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colId = new TableColumn<>("User ID");
        colId.setPrefWidth(95);
        colId.setCellValueFactory(d -> new SimpleStringProperty(
                optString(d.getValue().get("uid"), optString(d.getValue().get("id"), "—"))));
        colId.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colEmail = new TableColumn<>("Email");
        colEmail.setPrefWidth(150);
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("email"), "—")));
        colEmail.setStyle("-fx-font-size:11px; -fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colOrg = new TableColumn<>("Company / Organization");
        colOrg.setPrefWidth(140);
        colOrg.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("company"),
                optString(d.getValue().get("organization"), "—"))));
        colOrg.setStyle("-fx-font-size:11px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(85);
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("status"), "Active")));
        colStatus.setCellFactory(col -> createStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colShip = new TableColumn<>("Assigned Ship");
        colShip.setPrefWidth(140);
        colShip.setCellValueFactory(
                d -> new SimpleStringProperty(optString(d.getValue().get("assignedShip"), "Not Assigned")));
        colShip.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#0284c7;");

        TableColumn<Map<String, Object>, String> colDuty = new TableColumn<>("Current Duty");
        colDuty.setPrefWidth(135);
        colDuty.setCellValueFactory(
                d -> new SimpleStringProperty(optString(d.getValue().get("duty"), "—")));
        colDuty.setStyle("-fx-font-size:11px; -fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colOfficer = new TableColumn<>("Assigned Officer");
        colOfficer.setPrefWidth(125);
        colOfficer.setCellValueFactory(d -> new SimpleStringProperty(resolveAssignedOfficerForCaptain(d.getValue())));
        colOfficer.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#16a34a;");

        TableColumn<Map<String, Object>, String> colDutyStatus = new TableColumn<>("Duty Status");
        colDutyStatus.setPrefWidth(100);
        colDutyStatus.setCellValueFactory(d -> new SimpleStringProperty(resolveDutyStatus(d.getValue())));
        colDutyStatus.setCellFactory(col -> createDutyStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colUpdated = new TableColumn<>("Last Updated");
        colUpdated.setPrefWidth(100);
        colUpdated.setCellValueFactory(
                d -> new SimpleStringProperty(optString(d.getValue().get("updatedAt"), "—")));
        colUpdated.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colAction = new TableColumn<>("Actions");
        colAction.setPrefWidth(220);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button manageBtn = new Button("Manage Assignment");
            private final HBox actBox = new HBox(5);

            {
                viewBtn.setStyle(
                        "-fx-background-color:#f1f5f9; -fx-text-fill:#334155; -fx-border-color:#cbd5e1; -fx-border-radius:4; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 7; -fx-cursor:hand;");
                editBtn.setStyle(
                        "-fx-background-color:#f8fafc; -fx-text-fill:#0284c7; -fx-border-color:#bae6fd; -fx-border-radius:4; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 7; -fx-cursor:hand;");
                manageBtn.setStyle(
                        "-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");

                viewBtn.setOnAction(e -> {
                    Map<String, Object> cap = getTableView().getItems().get(getIndex());
                    showCaptainDetailsDialog(cap);
                });
                editBtn.setOnAction(e -> {
                    Map<String, Object> cap = getTableView().getItems().get(getIndex());
                    showEditCaptainDialog(cap);
                });
                manageBtn.setOnAction(e -> {
                    Map<String, Object> cap = getTableView().getItems().get(getIndex());
                    showAssignCaptainShipOrDutyDialog(cap);
                });

                actBox.setAlignment(Pos.CENTER);
                actBox.getChildren().addAll(viewBtn, editBtn, manageBtn);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actBox);
                }
                setAlignment(Pos.CENTER);
            }
        });

        captainTable.getColumns().addAll(
                colName, colId, colEmail, colOrg, colStatus,
                colShip, colDuty, colOfficer, colDutyStatus, colUpdated, colAction);
        captainTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setPrefHeight(38);
            return row;
        });

        filteredCaptainsList = new FilteredList<>(masterCaptainsList, p -> true);
        captainTable.setItems(filteredCaptainsList);

        box.getChildren().addAll(titleRow, filterRow, captainTable);
        return box;
    }

    // =====================================================
    // 6. ACTIVE SHIP CAPTAIN ASSIGNMENTS SECTION
    // =====================================================
    private VBox buildActiveAssignmentsSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        box.setStyle(
                "-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 4, 0, 0, 1);");

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("ACTIVE SHIP CAPTAIN ASSIGNMENTS");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13px; -fx-font-weight:bold;");

        Label sub = new Label("Current active ship and operational duty assignments for registered captains at " + getDisplayPortName());
        sub.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px;");
        titleBox.getChildren().addAll(title, sub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label badge = new Label("ACTIVE ASSIGNMENTS");
        badge.setStyle(
                "-fx-text-fill:#0284c7; -fx-background-color:#e0f2fe; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;");

        titleRow.getChildren().addAll(titleBox, sp, badge);

        activeAssignmentsTable = new TableView<>();
        var actCss = getClass().getResource("/table-headers.css");
        if (actCss != null) {
            activeAssignmentsTable.getStylesheets().add(actCss.toExternalForm());
        }
        activeAssignmentsTable.setPrefHeight(200);
        activeAssignmentsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        activeAssignmentsTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");

        Label emptyAssignLabel = new Label("No active ship-captain assignments recorded for " + getDisplayPortName() + ".");
        emptyAssignLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px;");
        activeAssignmentsTable.setPlaceholder(emptyAssignLabel);

        TableColumn<Map<String, Object>, String> colCap = new TableColumn<>("Captain");
        colCap.setPrefWidth(145);
        colCap.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("captain"), "—")));
        colCap.setStyle("-fx-font-weight:bold; -fx-text-fill:#0284c7;");

        TableColumn<Map<String, Object>, String> colShip = new TableColumn<>("Ship");
        colShip.setPrefWidth(145);
        colShip.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("ship"), "—")));
        colShip.setStyle("-fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colVoy = new TableColumn<>("Voyage");
        colVoy.setPrefWidth(100);
        colVoy.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("voyage"), "—")));
        colVoy.setStyle("-fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colDuty = new TableColumn<>("Current Duty");
        colDuty.setPrefWidth(135);
        colDuty.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("currentDuty"), "—")));
        colDuty.setStyle("-fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colPort = new TableColumn<>("Port / Location");
        colPort.setPrefWidth(130);
        colPort.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("location"), getDisplayPortName())));
        colPort.setStyle("-fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colStat = new TableColumn<>("Assignment Status");
        colStat.setPrefWidth(125);
        colStat.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("assignmentStatus"), "Assigned")));
        colStat.setCellFactory(c -> createDutyStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colAssignedDate = new TableColumn<>("Assigned Date");
        colAssignedDate.setPrefWidth(110);
        colAssignedDate.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("assignedDate"), "—")));
        colAssignedDate.setStyle("-fx-text-fill:#64748b; -fx-alignment:CENTER;");

        TableColumn<Map<String, Object>, String> colUpdated = new TableColumn<>("Last Updated");
        colUpdated.setPrefWidth(105);
        colUpdated.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("lastUpdated"), "—")));
        colUpdated.setStyle("-fx-text-fill:#64748b; -fx-alignment:CENTER;");

        TableColumn<Map<String, Object>, String> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(155);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button manageBtn = new Button("Manage");
            private final HBox actBox = new HBox(5);

            {
                viewBtn.setStyle(
                        "-fx-background-color:#f1f5f9; -fx-text-fill:#334155; -fx-border-color:#cbd5e1; -fx-border-radius:4; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 7; -fx-cursor:hand;");
                manageBtn.setStyle(
                        "-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");

                viewBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    Map<String, Object> capRef = (Map<String, Object>) item.get("captainRaw");
                    if (capRef != null) {
                        showCaptainDetailsDialog(capRef);
                    } else {
                        showCaptainDetailsDialog(item);
                    }
                });
                manageBtn.setOnAction(e -> {
                    Map<String, Object> item = getTableView().getItems().get(getIndex());
                    Map<String, Object> capRef = (Map<String, Object>) item.get("captainRaw");
                    if (capRef != null) {
                        showAssignCaptainShipOrDutyDialog(capRef);
                    } else {
                        showAssignCaptainShipOrDutyDialog(item);
                    }
                });

                actBox.setAlignment(Pos.CENTER);
                actBox.getChildren().addAll(viewBtn, manageBtn);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actBox);
                }
                setAlignment(Pos.CENTER);
            }
        });

        activeAssignmentsTable.getColumns().addAll(colCap, colShip, colVoy, colDuty, colPort, colStat, colAssignedDate, colUpdated, colAction);
        activeAssignmentsTable.setItems(activeAssignmentsList);

        box.getChildren().addAll(titleRow, activeAssignmentsTable);
        return box;
    }

    // =====================================================
    // 7. DATA LOADING & LIVE SYNCHRONIZATION
    // =====================================================
    public void refreshAllData() {
        try {
            UserDao.syncFirestoreUsers();
        } catch (Exception ignored) {
        }

        loadLiveShipsReference();
        loadLiveCaptains();
        updateKpisAndOperational();

        if (dataFreshnessBadge != null) {
            dataFreshnessBadge.setText("● LIVE — Synced");
        }
        if (lastSyncLabel != null) {
            lastSyncLabel.setText("Synced: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
    }

    private void loadLiveShipsReference() {
        List<Map<String, Object>> list = new ArrayList<>();
        Set<String> seenShips = new HashSet<>();

        // 1. From Firestore ShipDAO
        try {
            List<Ship> dbShips = shipDAO.getShips();
            if (dbShips != null) {
                for (Ship s : dbShips) {
                    if (s == null || s.getShipName() == null || s.getShipName().isBlank())
                        continue;
                    if (isShipAssociatedWithPort(s, activeViewingPort)) {
                        String key = s.getShipName().trim().toLowerCase();
                        if (seenShips.add(key)) {
                            Map<String, Object> m = new HashMap<>();
                            m.put("shipId", s.getShipId() != null ? s.getShipId()
                                    : "SHP-" + Math.abs(key.hashCode() % 900 + 100));
                            m.put("shipName", s.getShipName());
                            m.put("shipType", s.getShipType() != null ? s.getShipType() : "Container");
                            m.put("imoNumber", s.getImoNumber() != null ? s.getImoNumber() : "—");
                            m.put("company", s.getCompany() != null && !s.getCompany().isBlank() ? s.getCompany() : "—");
                            m.put("status", s.getStatus() != null && !s.getStatus().isBlank() ? s.getStatus() : "Berthed");
                            m.put("berth", s.getBerth() != null && !s.getBerth().isBlank() ? s.getBerth() : "—");
                            m.put("arrival", s.getArrival() != null && !s.getArrival().isBlank() ? s.getArrival() : "—");
                            m.put("departure", s.getDeparture() != null && !s.getDeparture().isBlank() ? s.getDeparture() : activeViewingPort);
                            m.put("destination", s.getDestination() != null && !s.getDestination().isBlank() ? s.getDestination() : "—");
                            m.put("voyageId", s.getShipId() != null && !s.getShipId().isBlank() ? "VYG-" + s.getShipId().replaceAll("[^0-9]", "") : "—");
                            list.add(m);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // 2. From Live AIS VesselManager
        try {
            Collection<VesselData> aisVessels = vesselManager.getAllVessels();
            if (aisVessels != null) {
                for (VesselData v : aisVessels) {
                    if (v == null || v.name == null || v.name.isBlank())
                        continue;
                    if (isVesselAssociatedWithPort(v, activeViewingPort)) {
                        String key = v.name.trim().toLowerCase();
                        if (seenShips.add(key)) {
                            Map<String, Object> m = new HashMap<>();
                            m.put("shipId", "AIS-" + v.mmsi);
                            m.put("shipName", v.name);
                            m.put("shipType", v.shipType != null && !v.shipType.isBlank() ? v.shipType : "Merchant Vessel");
                            m.put("imoNumber", v.imo != null && !v.imo.isBlank() ? v.imo : String.valueOf(v.mmsi));
                            m.put("company", "—");
                            m.put("status", v.navigationStatus != null && !v.navigationStatus.isBlank() ? v.navigationStatus : "In Transit");
                            m.put("berth", "—");
                            m.put("arrival", v.timestamp != null && !v.timestamp.isBlank() ? v.timestamp : "—");
                            m.put("departure", activeViewingPort);
                            m.put("destination", v.destination != null && !v.destination.isBlank() ? v.destination : "—");
                            m.put("voyageId", v.mmsi != 0 ? "VYG-" + Math.abs(v.mmsi % 9000 + 1000) : "—");
                            list.add(m);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        masterShipsList.clear();
        masterShipsList.addAll(list);
    }

    private void loadLiveCaptains() {
        List<Map<String, Object>> caps = UserDao.getPersonnelForPort(activeViewingPort, "Captain");
        masterCaptainsList.setAll(caps);
        applyCaptainFilters();
    }

    private void updateKpisAndOperational() {
        int totalC = masterCaptainsList.size();
        int activeC = 0;
        int assignedC = 0;
        int activeDuties = 0;

        List<Map<String, Object>> activeAssignments = new ArrayList<>();

        // Process only real, registered captains from the database
        for (Map<String, Object> c : masterCaptainsList) {
            String stat = optString(c.get("status"), "Active");
            if ("Active".equalsIgnoreCase(stat)) {
                activeC++;
            }

            String ship = optString(c.get("assignedShip"), "");
            String duty = optString(c.get("duty"), "");
            boolean hasShip = !ship.isBlank() && !ship.equalsIgnoreCase("Not Assigned") && !ship.equalsIgnoreCase("—");
            boolean hasDuty = !duty.isBlank() && !duty.equalsIgnoreCase("Not Assigned")
                    && !duty.equalsIgnoreCase("Standby") && !duty.equalsIgnoreCase("Available") && !duty.equalsIgnoreCase("—");

            if (hasDuty) {
                activeDuties++;
            }

            if (hasShip || hasDuty) {
                assignedC++;
                Map<String, Object> assignItem = new HashMap<>(c);
                assignItem.put("captainRaw", c);
                assignItem.put("captain", optString(c.get("name"), "Ship Captain"));
                assignItem.put("ship", hasShip ? ship : "—");

                // Match with ship reference for voyage and location
                String voyage = optString(c.get("voyageId"), "");
                String portLoc = optString(c.get("port"), "");
                if (hasShip) {
                    for (Map<String, Object> s : masterShipsList) {
                        String sName = optString(s.get("shipName"), "");
                        String sId = optString(s.get("shipId"), "");
                        if (ship.equalsIgnoreCase(sName) || ship.equalsIgnoreCase(sId)) {
                            if (voyage.isBlank() || voyage.equals("—"))
                                voyage = optString(s.get("voyageId"), "—");
                            if (portLoc.isBlank() || portLoc.equals("—"))
                                portLoc = optString(s.get("departure"), optString(s.get("destination"), ""));
                            break;
                        }
                    }
                }
                assignItem.put("voyage", !voyage.isBlank() ? voyage : "—");
                assignItem.put("currentDuty", !duty.isBlank() && !duty.equalsIgnoreCase("—") ? duty : (hasShip ? "Vessel Command" : "—"));
                assignItem.put("location", !portLoc.isBlank() ? portLoc : getDisplayPortName());
                assignItem.put("assignmentStatus", resolveDutyStatus(c));
                assignItem.put("assignedDate", optString(c.get("assignedDate"), optString(c.get("dateAdded"), "—")));
                assignItem.put("lastUpdated", optString(c.get("updatedAt"), "—"));
                activeAssignments.add(assignItem);
            }
        }

        int availableC = Math.max(0, totalC - assignedC);

        if (totalCaptainsVal != null)
            totalCaptainsVal.setText(String.valueOf(totalC));
        if (activeCaptainsVal != null)
            activeCaptainsVal.setText(String.valueOf(activeC));
        if (assignedCaptainsVal != null)
            assignedCaptainsVal.setText(String.valueOf(assignedC));
        if (availCaptainsVal != null)
            availCaptainsVal.setText(String.valueOf(availableC));
        if (activeDutiesVal != null)
            activeDutiesVal.setText(String.valueOf(activeDuties));

        activeAssignmentsList.setAll(activeAssignments);
    }

    private boolean isShipAssociatedWithPort(Ship s, String portName) {
        if (s == null || portName == null)
            return false;
        String p = portName.toLowerCase();
        String berth = optString(s.getBerth(), "").toLowerCase();
        String comp = optString(s.getCompany(), "").toLowerCase();
        String arr = optString(s.getArrival(), "").toLowerCase();
        String dep = optString(s.getDeparture(), "").toLowerCase();
        String dest = optString(s.getDestination(), "").toLowerCase();
        String sId = optString(s.getShipId(), "").toLowerCase();

        if (berth.contains(p) || comp.contains(p) || arr.contains(p) || dep.contains(p) || dest.contains(p) || sId.contains(p)) {
            return true;
        }
        if (p.contains("mumbai")) {
            return berth.contains("mumbai") || berth.contains("jnpt") || comp.contains("mumbai")
                    || arr.contains("mumbai") || dep.contains("mumbai") || dest.contains("mumbai");
        } else if (p.contains("cochin") || p.contains("kochi")) {
            return berth.contains("cochin") || berth.contains("kochi") || comp.contains("cochin")
                    || arr.contains("cochin") || dep.contains("cochin") || dest.contains("cochin");
        }
        return true;
    }

    private boolean isVesselAssociatedWithPort(VesselData v, String portName) {
        if (v == null || portName == null)
            return false;
        String p = portName.toLowerCase();
        String dest = optString(v.destination, "").toLowerCase();
        if (dest.contains(p))
            return true;
        if (p.contains("mumbai") && (dest.contains("mum") || dest.contains("jnpt") || dest.contains("inbom")))
            return true;
        if (p.contains("cochin") && (dest.contains("cok") || dest.contains("kochi") || dest.contains("incok")))
            return true;
        return true;
    }

    private String resolveAssignedOfficerForCaptain(Map<String, Object> captain) {
        Object directOfficer = captain.get("assignedOfficer");
        if (directOfficer != null && !directOfficer.toString().isBlank()
                && !directOfficer.toString().equalsIgnoreCase("—")) {
            return directOfficer.toString();
        }
        String ship = optString(captain.get("assignedShip"), "");
        if (!ship.isBlank() && !ship.equalsIgnoreCase("Not Assigned")) {
            List<Map<String, Object>> officers = UserDao.getPersonnelForPort(activeViewingPort, "Officer");
            for (Map<String, Object> o : officers) {
                String oShip = optString(o.get("assignedShip"), "");
                if (!oShip.isBlank() && oShip.equalsIgnoreCase(ship)) {
                    return optString(o.get("name"), "Officer");
                }
            }
        }
        return "—";
    }

    private String resolveDutyStatus(Map<String, Object> captain) {
        String aStat = optString(captain.get("assignmentStatus"), "");
        if (!aStat.isBlank() && !aStat.equalsIgnoreCase("—")) {
            return aStat;
        }
        String ship = optString(captain.get("assignedShip"), "");
        String duty = optString(captain.get("duty"), "");
        if ((!ship.isBlank() && !ship.equalsIgnoreCase("Not Assigned")) ||
                (!duty.isBlank() && !duty.equalsIgnoreCase("Not Assigned") && !duty.equalsIgnoreCase("Standby")
                        && !duty.equalsIgnoreCase("Available"))) {
            return "Assigned";
        }
        return "Available";
    }

    // =====================================================
    // 8. FILTERS & SORTING
    // =====================================================
    private void applyCaptainFilters() {
        if (filteredCaptainsList == null)
            return;

        String query = captainSearchField != null ? captainSearchField.getText().trim().toLowerCase() : "";
        String statusFilter = captainStatusFilter != null ? captainStatusFilter.getValue() : "All Statuses";
        String assignFilter = captainAssignFilter != null ? captainAssignFilter.getValue() : "All Assignments";

        filteredCaptainsList.setPredicate(c -> {
            if (!query.isEmpty()) {
                String name = optString(c.get("name"), "").toLowerCase();
                String uid = optString(c.get("uid"), optString(c.get("id"), "")).toLowerCase();
                String email = optString(c.get("email"), "").toLowerCase();
                String comp = optString(c.get("company"), optString(c.get("organization"), "")).toLowerCase();
                String ship = optString(c.get("assignedShip"), "").toLowerCase();
                if (!name.contains(query) && !uid.contains(query) && !email.contains(query)
                        && !comp.contains(query) && !ship.contains(query)) {
                    return false;
                }
            }
            if (statusFilter != null && !statusFilter.equals("All Statuses")) {
                String stat = optString(c.get("status"), "Active");
                if (!stat.equalsIgnoreCase(statusFilter))
                    return false;
            }
            if (assignFilter != null && !assignFilter.equals("All Assignments")) {
                String dutyStat = resolveDutyStatus(c);
                if (assignFilter.contains("Available") && !"Available".equalsIgnoreCase(dutyStat)) {
                    return false;
                } else if (assignFilter.equalsIgnoreCase("Assigned") && !"Assigned".equalsIgnoreCase(dutyStat)) {
                    return false;
                }
            }
            return true;
        });

        if (captainSortCombo != null && captainSortCombo.getValue() != null && !masterCaptainsList.isEmpty()) {
            String sortVal = captainSortCombo.getValue();
            Comparator<Map<String, Object>> comp;
            if ("Sort by: User ID".equalsIgnoreCase(sortVal)) {
                comp = Comparator.comparing(m -> optString(m.get("uid"), optString(m.get("id"), "")), String.CASE_INSENSITIVE_ORDER);
            } else if ("Sort by: Status".equalsIgnoreCase(sortVal)) {
                comp = Comparator.comparing(m -> optString(m.get("status"), ""), String.CASE_INSENSITIVE_ORDER);
            } else if ("Sort by: Ship".equalsIgnoreCase(sortVal)) {
                comp = Comparator.comparing(m -> optString(m.get("assignedShip"), ""), String.CASE_INSENSITIVE_ORDER);
            } else {
                comp = Comparator.comparing(m -> optString(m.get("name"), ""), String.CASE_INSENSITIVE_ORDER);
            }
            masterCaptainsList.sort(comp);
        }
    }

    // =====================================================
    // 9. CELL FACTORIES & BADGES
    // =====================================================
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
                    String color = "#0284c7", bg = "#e0f2fe";
                    if ("Active".equalsIgnoreCase(item) || "Completed".equalsIgnoreCase(item)) {
                        color = "#16a34a";
                        bg = "#ecfdf5";
                    } else if ("Pending".equalsIgnoreCase(item) || "Standby".equalsIgnoreCase(item)) {
                        color = "#d97706";
                        bg = "#fef3c7";
                    } else if ("Inactive".equalsIgnoreCase(item)) {
                        color = "#ef4444";
                        bg = "#fee2e2";
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

    private TableCell<Map<String, Object>, String> createDutyStatusBadgeCell() {
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
                    } else if ("Completed".equalsIgnoreCase(item)) {
                        color = "#16a34a";
                        bg = "#ecfdf5";
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

    // =====================================================
    // 10. WORKING DIALOGS & CAPTAIN ACTIONS
    // =====================================================
    private void showRegisterCaptainDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register Captain — " + activeViewingPort);
        dialog.setHeaderText("Create Certified Ship Captain Account for " + getDisplayPortName());

        ButtonType regType = new ButtonType("Register Captain", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(regType, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(18));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Capt. Rajesh Kumar");

        TextField emailField = new TextField();
        emailField.setPromptText("e.g. rajesh.captain@shipping.com");

        TextField phoneField = new TextField();
        phoneField.setPromptText("e.g. +91 9876543210");

        TextField companyField = new TextField();
        companyField.setPromptText("e.g. Shipping Line / Carrier");

        ComboBox<String> shipCombo = new ComboBox<>();
        shipCombo.getItems().add("— Unassigned / Standby —");
        for (Map<String, Object> s : masterShipsList) {
            shipCombo.getItems().add(optString(s.get("shipName"), "Ship"));
        }
        shipCombo.setValue("— Unassigned / Standby —");

        form.add(new Label("Captain Name:"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Official Email:"), 0, 1);
        form.add(emailField, 1, 1);
        form.add(new Label("Phone:"), 0, 2);
        form.add(phoneField, 1, 2);
        form.add(new Label("Shipping Company:"), 0, 3);
        form.add(companyField, 1, 3);
        form.add(new Label("Initial Vessel:"), 0, 4);
        form.add(shipCombo, 1, 4);

        dialog.getDialogPane().setContent(form);

        Button regBtn = (Button) dialog.getDialogPane().lookupButton(regType);
        regBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-weight:bold;");

        regBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String comp = companyField.getText().trim();
            String chosenShip = shipCombo.getValue();

            if (name.isEmpty() || email.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Captain Name and Email are required.");
                alert.showAndWait();
                e.consume();
                return;
            }

            String uid = "UID-CAP-" + System.currentTimeMillis();
            Map<String, Object> extra = new HashMap<>();
            extra.put("assignedPort", activeViewingPort);
            extra.put("duty", chosenShip.contains("Unassigned") ? "Standby" : "Vessel Command");
            extra.put("assignmentStatus", chosenShip.contains("Unassigned") ? "Available" : "Assigned");
            extra.put("assignedShip", chosenShip.contains("Unassigned") ? "Not Assigned" : chosenShip);

            new UserDao().saveUserExtended(uid, email, name, comp, phone, "Ship Captain", "Active", extra)
                    .whenComplete((res, err) -> Platform.runLater(this::refreshAllData));
        });

        dialog.showAndWait();
    }

    private void showCaptainDetailsDialog(Map<String, Object> cap) {
        if (cap == null)
            return;
        Dialog<Void> d = new Dialog<>();
        d.setTitle("Captain Profile — " + optString(cap.get("name"), "Captain"));
        d.setHeaderText("Certified Master Mariner Record: " + optString(cap.get("name"), "Captain"));
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane g = new GridPane();
        g.setHgap(14);
        g.setVgap(10);
        g.setPadding(new Insets(18));

        int r = 0;
        addDetailRow(g, r++, "Full Name:", optString(cap.get("name"), "—"), true);
        addDetailRow(g, r++, "User ID / UID:", optString(cap.get("uid"), optString(cap.get("id"), "—")), false);
        addDetailRow(g, r++, "Email Address:", optString(cap.get("email"), "—"), false);
        addDetailRow(g, r++, "Company / Fleet:", optString(cap.get("company"), optString(cap.get("organization"), "—")),
                false);
        addDetailRow(g, r++, "Assigned Vessel:", optString(cap.get("assignedShip"), "Not Assigned"), true);
        addDetailRow(g, r++, "Current Duty:", optString(cap.get("duty"), "—"), false);
        addDetailRow(g, r++, "Assigned Officer:", resolveAssignedOfficerForCaptain(cap), false);
        addDetailRow(g, r++, "Duty Status:", resolveDutyStatus(cap), false);
        addDetailRow(g, r++, "Port Jurisdiction:", activeViewingPort, false);

        d.getDialogPane().setContent(g);
        d.showAndWait();
    }

    private void showEditCaptainDialog(Map<String, Object> cap) {
        if (cap == null)
            return;
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Edit Captain Details — " + optString(cap.get("name"), "Captain"));
        d.setHeaderText("Update Personnel Record: " + optString(cap.get("name"), "Captain"));

        ButtonType saveBtnType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(12);
        g.setPadding(new Insets(18));

        TextField nameField = new TextField(optString(cap.get("name"), ""));
        TextField compField = new TextField(
                optString(cap.get("company"), optString(cap.get("organization"), "Ocean Shipping Lines")));
        ComboBox<String> statField = new ComboBox<>();
        statField.getItems().addAll("Active", "Pending", "Inactive");
        statField.setValue(optString(cap.get("status"), "Active"));

        g.add(new Label("Full Name:"), 0, 0);
        g.add(nameField, 1, 0);
        g.add(new Label("Company:"), 0, 1);
        g.add(compField, 1, 1);
        g.add(new Label("Status:"), 0, 2);
        g.add(statField, 1, 2);

        d.getDialogPane().setContent(g);

        d.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                String uid = optString(cap.get("uid"), optString(cap.get("email"), ""));
                String email = optString(cap.get("email"), "");
                String newName = nameField.getText().trim();
                String newComp = compField.getText().trim();
                String newStat = statField.getValue();

                Map<String, Object> extra = new HashMap<>(cap);
                extra.put("status", newStat);
                extra.put("company", newComp);
                new UserDao()
                        .saveUserExtended(uid, email, newName, newComp, "+91 9800000000", "Ship Captain", newStat,
                                extra)
                        .whenComplete((v, err) -> Platform.runLater(this::refreshAllData));
            }
            return null;
        });

        d.showAndWait();
    }

    private void showAssignCaptainShipOrDutyDialog(Map<String, Object> cap) {
        if (cap == null)
            return;
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Manage Assignment — " + optString(cap.get("name"), "Captain"));
        d.setHeaderText("Assign Vessel and Operational Duty for Captain: " + optString(cap.get("name"), "Captain"));

        ButtonType saveBtnType = new ButtonType("Save Assignment", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(12);
        g.setPadding(new Insets(18));

        ComboBox<String> shipCombo = new ComboBox<>();
        shipCombo.getItems().add("— Not Assigned / Standby —");
        for (Map<String, Object> s : masterShipsList) {
            shipCombo.getItems().add(optString(s.get("shipName"), "Ship"));
        }
        String curShip = optString(cap.get("assignedShip"), "— Not Assigned / Standby —");
        shipCombo.setValue(shipCombo.getItems().contains(curShip) ? curShip : "— Not Assigned / Standby —");

        ComboBox<String> dutyCombo = new ComboBox<>();
        dutyCombo.getItems().addAll("Navigation & Command", "Loading Operations", "Unloading Operations", "Berthing",
                "Safety Watch");
        dutyCombo.setValue(optString(cap.get("duty"), "Navigation & Command"));

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("HIGH", "MEDIUM", "LOW");
        priorityCombo.setValue("HIGH");

        g.add(new Label("Assigned Vessel:"), 0, 0);
        g.add(shipCombo, 1, 0);
        g.add(new Label("Operational Duty:"), 0, 1);
        g.add(dutyCombo, 1, 1);
        g.add(new Label("Duty Priority:"), 0, 2);
        g.add(priorityCombo, 1, 2);

        d.getDialogPane().setContent(g);

        d.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                String uid = optString(cap.get("uid"), optString(cap.get("email"), ""));
                String chosenShip = shipCombo.getValue();
                String shipName = chosenShip.contains("Not Assigned") ? "Not Assigned" : chosenShip;
                String duty = dutyCombo.getValue();
                String priority = priorityCombo.getValue();

                String sId = "SHP-" + Math.abs(shipName.hashCode() % 900 + 100);
                String sImo = "—";
                for (Map<String, Object> s : masterShipsList) {
                    if (shipName.equalsIgnoreCase(optString(s.get("shipName"), ""))) {
                        sId = optString(s.get("shipId"), sId);
                        sImo = optString(s.get("imoNumber"), sImo);
                        break;
                    }
                }
                UserDao.assignCaptainShip(uid, sId, shipName, sImo, "Port Authority (" + activeViewingPort + ")");
                UserDao.assignCaptainDuty(uid, duty, priority, "Standard Shift",
                        "Port Authority (" + activeViewingPort + ")");

                // Create Notification for the assigned captain
                Notification notif = new Notification();
                notif.setUserId(uid);
                notif.setVesselId(sId);
                notif.setVesselName(shipName);
                notif.setPort(activeViewingPort);
                notif.setStatus("Active");
                notif.setAssignedBy("Port Authority");
                notif.setRead(false);
                notif.setTitle("🔔 Vessel & Duty Assignment Update");
                notif.setMessage(
                        "You have been assigned as Captain for " + shipName + " (Duty: " + duty + ") at " + activeViewingPort + ".");
                notif.setTimestamp(NotificationDao.generateTimestamp());
                new NotificationDao().createNotification(notif);

                Platform.runLater(this::refreshAllData);
            }
            return null;
        });

        d.showAndWait();
    }

    private void addDetailRow(GridPane g, int row, String label, String value, boolean highlight) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px; -fx-font-weight:bold;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size:12px; "
                + (highlight ? "-fx-font-weight:bold; -fx-text-fill:#0284c7;" : "-fx-text-fill:#0f172a;"));
        g.add(l, 0, row);
        g.add(v, 1, row);
    }

    private String optString(Object obj, String fallback) {
        if (obj == null)
            return fallback;
        String s = obj.toString().trim();
        return s.isBlank() ? fallback : s;
    }

    public VBox getRoot() {
        return root;
    }
}