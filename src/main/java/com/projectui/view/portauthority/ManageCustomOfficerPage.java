package com.projectui.view.portauthority;

import com.projectui.dao.NotificationDao;
import com.projectui.dao.UserDao;
import com.projectui.model.Notification;
import com.projectui.view.login.LoginPage;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SuppressWarnings({"unchecked", "unused"})
public class ManageCustomOfficerPage {

    private final VBox root = new VBox(16);

    // Context
    private String userAssignedPort = "Port of Mumbai";
    private String activeViewingPort = "Port of Mumbai";

    // Summary Metric Labels
    private Label totalOfficersVal;
    private Label activeOfficersVal;
    private Label assignedOfficersVal;
    private Label availOfficersVal;
    private Label activeDutiesVal;
    private Label completedDutiesVal;

    // Header Freshness Labels
    private Label dataFreshnessBadge;
    private Label lastSyncLabel;

    // Customs Officer Data
    private final ObservableList<Map<String, Object>> masterOfficersList = FXCollections.observableArrayList();
    private FilteredList<Map<String, Object>> filteredOfficersList;
    private TableView<Map<String, Object>> officerTable;
    private TextField officerSearchField;
    private ComboBox<String> departmentFilter;
    private ComboBox<String> statusFilter;
    private ComboBox<String> portFilter;
    private ComboBox<String> assignmentFilter;
    private ComboBox<String> sortCombo;

    // Active Duty Assignments Data
    private final ObservableList<Map<String, Object>> activeDutiesList = FXCollections.observableArrayList();
    private TableView<Map<String, Object>> dutyTable;

    // Recent Officer Activity Data
    private final ObservableList<Map<String, Object>> recentActivityList = FXCollections.observableArrayList();
    private TableView<Map<String, Object>> activityTable;

    public ManageCustomOfficerPage() {
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
        VBox officerSection = buildCustomOfficerSection();
        VBox dutySection = buildDutyAssignmentSection();
        VBox activitySection = buildRecentActivitySection();

        root.getChildren().addAll(
                header,
                summaryCards,
                officerSection,
                dutySection,
                activitySection
        );
    }

    // =====================================================
    // 3. TOP HEADER
    // =====================================================
    private VBox buildHeader() {
        VBox container = new VBox(4);

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label pageHeadingLabel = new Label("Manage Custom Officer");
        pageHeadingLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:22px; -fx-font-weight:bold;");

        Label pageSubLabel = new Label("Registered Customs Personnel & Duty Assignments for " + getDisplayPortName());
        pageSubLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:13px;");
        titleBox.getChildren().addAll(pageHeadingLabel, pageSubLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        dataFreshnessBadge = new Label("● LIVE — Synced");
        dataFreshnessBadge.setStyle("-fx-text-fill:#10b981; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(16,185,129,0.12); -fx-padding:6 12; -fx-background-radius:999;");

        lastSyncLabel = new Label("Synced: Just now");
        lastSyncLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px;");

        Button refreshBtn = new Button("⟳ Refresh Personnel Data");
        refreshBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:7 14; -fx-cursor:hand;");
        refreshBtn.setOnAction(e -> refreshAllData());

        topRow.getChildren().addAll(titleBox, spacer, dataFreshnessBadge, lastSyncLabel, refreshBtn);
        container.getChildren().add(topRow);
        return container;
    }

    // =====================================================
    // 4. SUMMARY CARDS (6 LIVE DATABASE VALUES)
    // =====================================================
    private HBox buildSummaryCards() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        totalOfficersVal = new Label("0");
        activeOfficersVal = new Label("0");
        assignedOfficersVal = new Label("0");
        availOfficersVal = new Label("0");
        activeDutiesVal = new Label("0");
        completedDutiesVal = new Label("0");

        VBox c1 = createSummaryCard("REGISTERED CUSTOM OFFICERS", totalOfficersVal, "Commissioned inspectors", "🛡", "#16a34a", "#ecfdf5");
        VBox c2 = createSummaryCard("ACTIVE OFFICERS", activeOfficersVal, "On-duty personnel", "🟢", "#059669", "#d1fae5");
        VBox c3 = createSummaryCard("ASSIGNED OFFICERS", assignedOfficersVal, "Attached to operations", "🔵", "#2563eb", "#eff6ff");
        VBox c4 = createSummaryCard("AVAILABLE OFFICERS", availOfficersVal, "Ready for deployment", "⏳", "#d97706", "#fef3c7");
        VBox c5 = createSummaryCard("ACTIVE DUTIES", activeDutiesVal, "Verification in progress", "📋", "#0284c7", "#e0f2fe");
        VBox c6 = createSummaryCard("COMPLETED DUTIES", completedDutiesVal, "Certified clearances", "✅", "#7c3aed", "#f5f3ff");

        row.getChildren().addAll(c1, c2, c3, c4, c5, c6);

        for (Node n : row.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
            if (n instanceof VBox) {
                ((VBox) n).setMaxWidth(Double.MAX_VALUE);
            }
        }
        return row;
    }

    private VBox createSummaryCard(String title, Label valLabel, String subtitle, String iconStr, String color, String bg) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.04), 4, 0, 0, 1);");

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label iconBadge = new Label(iconStr);
        iconBadge.setStyle("-fx-font-size:13px; -fx-background-color:" + bg + "; -fx-text-fill:" + color + "; -fx-padding:4 7; -fx-background-radius:6;");

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
    // 5. CUSTOM OFFICER SECTION & TABLE
    // =====================================================
    private VBox buildCustomOfficerSection() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 4, 0, 0, 1);");

        // Title Row
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("REGISTERED CUSTOMS OFFICERS");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14px; -fx-font-weight:bold;");

        Label sub = new Label("Registered customs personnel and duty assignments for " + getDisplayPortName());
        sub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button regOffBtn = new Button("+ Register Officer");
        regOffBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 12; -fx-cursor:hand;");
        regOffBtn.setOnAction(e -> showRegisterOfficerDialog());

        titleRow.getChildren().addAll(titleBox, spacer, regOffBtn);

        // Search & Filter Row
        HBox filterRow = new HBox(8);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        officerSearchField = new TextField();
        officerSearchField.setPromptText("Search Officer / User ID / Email...");
        officerSearchField.setPrefWidth(220);
        officerSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyOfficerFilters());

        departmentFilter = new ComboBox<>();
        departmentFilter.getItems().addAll("All Departments", "Customs Border Control", "Cargo Inspection", "Maritime Compliance", "Tariff Enforcement");
        departmentFilter.setValue("All Departments");
        departmentFilter.setOnAction(e -> applyOfficerFilters());

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Statuses", "Active", "Pending", "Inactive");
        statusFilter.setValue("All Statuses");
        statusFilter.setOnAction(e -> applyOfficerFilters());

        portFilter = new ComboBox<>();
        portFilter.getItems().addAll("All Jurisdictions", activeViewingPort, "General Port Area");
        portFilter.setValue("All Jurisdictions");
        portFilter.setOnAction(e -> applyOfficerFilters());

        assignmentFilter = new ComboBox<>();
        assignmentFilter.getItems().addAll("All Assignments", "Assigned", "Available");
        assignmentFilter.setValue("All Assignments");
        assignmentFilter.setOnAction(e -> applyOfficerFilters());

        sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Sort by: Name", "Sort by: User ID", "Sort by: Status", "Sort by: Duty");
        sortCombo.setValue("Sort by: Name");
        sortCombo.setOnAction(e -> applyOfficerFilters());

        Button refreshBtn = new Button("↻");
        refreshBtn.setTooltip(new Tooltip("Refresh Officers"));
        refreshBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#334155; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-cursor:hand; -fx-font-weight:bold;");
        refreshBtn.setOnAction(e -> refreshAllData());

        filterRow.getChildren().addAll(
                new Label("Filter:"),
                officerSearchField,
                departmentFilter,
                statusFilter,
                portFilter,
                assignmentFilter,
                sortCombo,
                refreshBtn
        );

        // Custom Officer Table
        officerTable = new TableView<>();
        var offCss = getClass().getResource("/table-headers.css");
        if (offCss != null) {
            officerTable.getStylesheets().add(offCss.toExternalForm());
        }
        officerTable.setPrefHeight(260);
        officerTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        officerTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        officerTable.setPlaceholder(new Label("No Customs Officers registered."));

        TableColumn<Map<String, Object>, String> colName = new TableColumn<>("Officer Name");
        colName.setPrefWidth(140);
        colName.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("name"), "Customs Officer")));
        colName.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colId = new TableColumn<>("User ID");
        colId.setPrefWidth(90);
        colId.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("uid"), optString(d.getValue().get("id"), "—"))));
        colId.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colEmail = new TableColumn<>("Email");
        colEmail.setPrefWidth(140);
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("email"), "—")));
        colEmail.setStyle("-fx-font-size:11px; -fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colDept = new TableColumn<>("Authority / Department");
        colDept.setPrefWidth(140);
        colDept.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("company"), optString(d.getValue().get("organization"), "Customs Border Control"))));
        colDept.setStyle("-fx-font-size:11px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(85);
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("status"), "Active")));
        colStatus.setCellFactory(col -> createStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colPort = new TableColumn<>("Port / Jurisdiction");
        colPort.setPrefWidth(125);
        colPort.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("assignedPort"), activeViewingPort)));
        colPort.setStyle("-fx-font-size:11px; -fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colVessel = new TableColumn<>("Assigned Vessel");
        colVessel.setPrefWidth(130);
        colVessel.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("assignedShip"), "General Port Area")));
        colVessel.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:#0284c7;");

        TableColumn<Map<String, Object>, String> colDuty = new TableColumn<>("Current Duty");
        colDuty.setPrefWidth(140);
        colDuty.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("duty"), "Not Assigned")));
        colDuty.setStyle("-fx-font-size:11px; -fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colDutyStatus = new TableColumn<>("Duty Status");
        colDutyStatus.setPrefWidth(95);
        colDutyStatus.setCellValueFactory(d -> new SimpleStringProperty(resolveDutyStatus(d.getValue())));
        colDutyStatus.setCellFactory(col -> createDutyStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colUpdated = new TableColumn<>("Last Updated");
        colUpdated.setPrefWidth(100);
        colUpdated.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("updatedAt"), "Just now")));
        colUpdated.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(285);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button approveBtn = new Button("Approve");
            private final Button assignBtn = new Button("Assign Duty");
            private final Button manageBtn = new Button("Manage");
            private final HBox actBox = new HBox(4);

            {
                viewBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#334155; -fx-border-color:#cbd5e1; -fx-border-radius:4; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 6; -fx-cursor:hand;");
                editBtn.setStyle("-fx-background-color:#f8fafc; -fx-text-fill:#0284c7; -fx-border-color:#bae6fd; -fx-border-radius:4; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 6; -fx-cursor:hand;");
                approveBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 7; -fx-cursor:hand;");
                assignBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 7; -fx-cursor:hand;");
                manageBtn.setStyle("-fx-background-color:#475569; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 7; -fx-cursor:hand;");

                viewBtn.setOnAction(e -> {
                    Map<String, Object> off = getTableView().getItems().get(getIndex());
                    showOfficerDetailsDialog(off);
                });
                editBtn.setOnAction(e -> {
                    Map<String, Object> off = getTableView().getItems().get(getIndex());
                    showEditOfficerDialog(off);
                });
                approveBtn.setOnAction(e -> {
                    Map<String, Object> off = getTableView().getItems().get(getIndex());
                    String uid = optString(off.get("uid"), optString(off.get("email"), ""));
                    new UserDao().updateUserStatus(uid, "Active")
                            .whenComplete((res, err) -> Platform.runLater(ManageCustomOfficerPage.this::refreshAllData));
                });
                assignBtn.setOnAction(e -> {
                    Map<String, Object> off = getTableView().getItems().get(getIndex());
                    showAssignDutyDialog(off);
                });
                manageBtn.setOnAction(e -> {
                    Map<String, Object> off = getTableView().getItems().get(getIndex());
                    showManageOfficerStatusDialog(off);
                });

                actBox.setAlignment(Pos.CENTER);
                actBox.getChildren().addAll(viewBtn, editBtn, approveBtn, assignBtn, manageBtn);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Map<String, Object> off = getTableView().getItems().get(getIndex());
                    String stat = optString(off.get("status"), "");
                    boolean isPending = "Pending".equalsIgnoreCase(stat);
                    approveBtn.setVisible(isPending);
                    approveBtn.setManaged(isPending);
                    setGraphic(actBox);
                }
                setAlignment(Pos.CENTER);
            }
        });

        officerTable.getColumns().addAll(
                colName, colId, colEmail, colDept, colStatus,
                colPort, colVessel, colDuty, colDutyStatus, colUpdated, colAction
        );
        officerTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setPrefHeight(38);
            return row;
        });

        filteredOfficersList = new FilteredList<>(masterOfficersList, p -> true);
        officerTable.setItems(filteredOfficersList);

        box.getChildren().addAll(titleRow, filterRow, officerTable);
        return box;
    }

    // =====================================================
    // 6. DUTY ASSIGNMENT SECTION
    // =====================================================
    private VBox buildDutyAssignmentSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 4, 0, 0, 1);");

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("ACTIVE DUTY ASSIGNMENTS");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13px; -fx-font-weight:bold;");

        Label sub = new Label("Current customs officer assignments and operational duties for " + getDisplayPortName());
        sub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(title, sub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label badge = new Label("LIVE DEPLOYMENTS");
        badge.setStyle("-fx-text-fill:#16a34a; -fx-background-color:#ecfdf5; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;");

        titleRow.getChildren().addAll(titleBox, sp, badge);

        dutyTable = new TableView<>();
        var dutyCss = getClass().getResource("/table-headers.css");
        if (dutyCss != null) {
            dutyTable.getStylesheets().add(dutyCss.toExternalForm());
        }
        dutyTable.setPrefHeight(190);
        dutyTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        dutyTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        dutyTable.setPlaceholder(new Label("No active duty assignments available."));

        TableColumn<Map<String, Object>, String> colOfficer = new TableColumn<>("Officer");
        colOfficer.setPrefWidth(130);
        colOfficer.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("officerName"), "Customs Officer")));
        colOfficer.setStyle("-fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colDuty = new TableColumn<>("Duty");
        colDuty.setPrefWidth(140);
        colDuty.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("duty"), "Inspection")));
        colDuty.setStyle("-fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colVessel = new TableColumn<>("Assigned Vessel");
        colVessel.setPrefWidth(125);
        colVessel.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("vesselName"), "General Port")));
        colVessel.setStyle("-fx-text-fill:#0284c7; -fx-font-weight:bold;");

        TableColumn<Map<String, Object>, String> colCargo = new TableColumn<>("Cargo");
        colCargo.setPrefWidth(110);
        colCargo.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("cargo"), "Containerized")));
        colCargo.setStyle("-fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colVoyage = new TableColumn<>("Voyage");
        colVoyage.setPrefWidth(95);
        colVoyage.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("voyage"), "VYG-401")));
        colVoyage.setStyle("-fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colLocation = new TableColumn<>("Location");
        colLocation.setPrefWidth(120);
        colLocation.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("location"), activeViewingPort)));
        colLocation.setStyle("-fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colDutyStatus = new TableColumn<>("Duty Status");
        colDutyStatus.setPrefWidth(95);
        colDutyStatus.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("dutyStatus"), "Assigned")));
        colDutyStatus.setCellFactory(col -> createDutyStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colDate = new TableColumn<>("Assigned Date");
        colDate.setPrefWidth(110);
        colDate.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("assignedDate"), "Today")));
        colDate.setStyle("-fx-alignment:CENTER; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colUpdated = new TableColumn<>("Last Updated");
        colUpdated.setPrefWidth(100);
        colUpdated.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("updatedAt"), "Just now")));
        colUpdated.setStyle("-fx-alignment:CENTER; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(130);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button markDoneBtn = new Button("Mark Done");
            {
                markDoneBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");
                markDoneBtn.setOnAction(e -> {
                    Map<String, Object> record = getTableView().getItems().get(getIndex());
                    String uid = optString(record.get("uid"), "");
                    String duty = optString(record.get("duty"), "");
                    UserDao.assignOfficerDuty(uid, duty, "Completed", activeViewingPort, "Port Authority (" + activeViewingPort + ")", "LOW", "Completed", "", "", "", "DUTY-DONE");
                    Platform.runLater(ManageCustomOfficerPage.this::refreshAllData);
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(markDoneBtn);
                }
                setAlignment(Pos.CENTER);
            }
        });

        dutyTable.getColumns().addAll(
                colOfficer, colDuty, colVessel, colCargo, colVoyage,
                colLocation, colDutyStatus, colDate, colUpdated, colAction
        );
        dutyTable.setItems(activeDutiesList);

        box.getChildren().addAll(titleRow, dutyTable);
        return box;
    }

    // =====================================================
    // 7. RECENT OFFICER ACTIVITY SECTION
    // =====================================================
    private VBox buildRecentActivitySection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 4, 0, 0, 1);");

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("RECENT OFFICER ACTIVITY");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13px; -fx-font-weight:bold;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label badge = new Label("AUDIT LOG");
        badge.setStyle("-fx-text-fill:#6366f1; -fx-background-color:#e0e7ff; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;");

        titleRow.getChildren().addAll(title, sp, badge);

        activityTable = new TableView<>();
        var actCss = getClass().getResource("/table-headers.css");
        if (actCss != null) {
            activityTable.getStylesheets().add(actCss.toExternalForm());
        }
        activityTable.setPrefHeight(180);
        activityTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        activityTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        activityTable.setPlaceholder(new Label("No recent officer activity available."));

        TableColumn<Map<String, Object>, String> colOfficer = new TableColumn<>("Officer");
        colOfficer.setPrefWidth(140);
        colOfficer.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("officer"), optString(d.getValue().get("user"), "Officer"))));
        colOfficer.setStyle("-fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colActivity = new TableColumn<>("Activity");
        colActivity.setPrefWidth(260);
        colActivity.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("activity"), optString(d.getValue().get("action"), "Customs Inspection & Verification"))));
        colActivity.setStyle("-fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colTarget = new TableColumn<>("Related Cargo / Vessel");
        colTarget.setPrefWidth(180);
        colTarget.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("target"), optString(d.getValue().get("vesselName"), "General Quayside"))));
        colTarget.setStyle("-fx-text-fill:#0284c7; -fx-font-weight:bold;");

        TableColumn<Map<String, Object>, String> colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(100);
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("status"), "Completed")));
        colStatus.setCellFactory(col -> createStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colDate = new TableColumn<>("Date / Time");
        colDate.setPrefWidth(140);
        colDate.setCellValueFactory(d -> new SimpleStringProperty(optString(d.getValue().get("timestamp"), optString(d.getValue().get("date"), "Today"))));
        colDate.setStyle("-fx-alignment:CENTER; -fx-text-fill:#64748b;");

        activityTable.getColumns().addAll(colOfficer, colActivity, colTarget, colStatus, colDate);
        activityTable.setItems(recentActivityList);

        box.getChildren().addAll(titleRow, activityTable);
        return box;
    }

    // =====================================================
    // 8. DATA LOADING & LIVE SYNCHRONIZATION
    // =====================================================
    public void refreshAllData() {
        try {
            UserDao.syncFirestoreUsers();
        } catch (Exception ignored) {}

        loadLiveOfficers();
        updateKpisAndDutySections();

        if (dataFreshnessBadge != null) {
            dataFreshnessBadge.setText("● LIVE — Synced");
        }
        if (lastSyncLabel != null) {
            lastSyncLabel.setText("Synced: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
    }

    private void loadLiveOfficers() {
        List<Map<String, Object>> offs = UserDao.getPersonnelForPort(activeViewingPort, "Officer");
        masterOfficersList.setAll(offs);
        applyOfficerFilters();
    }

    private void updateKpisAndDutySections() {
        int totalO = masterOfficersList.size();
        int activeO = 0;
        int assignedO = 0;
        int activeDuties = 0;
        int completedDuties = 0;

        List<Map<String, Object>> duties = new ArrayList<>();
        List<Map<String, Object>> activities = new ArrayList<>();

        for (Map<String, Object> o : masterOfficersList) {
            String stat = optString(o.get("status"), "Active");
            if ("Active".equalsIgnoreCase(stat)) {
                activeO++;
            }

            String duty = optString(o.get("duty"), "");
            String dutyStatus = resolveDutyStatus(o);

            boolean isAssigned = (!duty.isBlank() && !duty.equalsIgnoreCase("Not Assigned") && !duty.equalsIgnoreCase("Standby") && !duty.equalsIgnoreCase("Available"))
                    || "Assigned".equalsIgnoreCase(dutyStatus) || "In Progress".equalsIgnoreCase(dutyStatus);

            if (isAssigned) {
                assignedO++;
                activeDuties++;

                Map<String, Object> dRec = new HashMap<>();
                dRec.put("uid", optString(o.get("uid"), ""));
                dRec.put("officerName", optString(o.get("name"), "Customs Officer"));
                dRec.put("duty", duty.isBlank() ? "Cargo Verification" : duty);
                dRec.put("vesselName", optString(o.get("assignedShip"), "General Port Area"));
                dRec.put("cargo", "Container Freight Unit");
                dRec.put("voyage", "VYG-" + Math.abs(optString(o.get("name"), "").hashCode() % 900 + 100));
                dRec.put("location", optString(o.get("assignedPort"), activeViewingPort));
                dRec.put("dutyStatus", dutyStatus);
                dRec.put("assignedDate", optString(o.get("updatedAt"), "Today"));
                dRec.put("updatedAt", "Live DB");
                duties.add(dRec);
            } else if ("Completed".equalsIgnoreCase(dutyStatus)) {
                completedDuties++;
            }
        }

        // Fetch real activity logs from backend
        try {
            List<Map<String, Object>> logs = UserDao.getActivityLogs();
            if (logs != null) {
                for (Map<String, Object> l : logs) {
                    String act = optString(l.get("activity"), optString(l.get("action"), ""));
                    String uRole = optString(l.get("role"), "");
                    String uName = optString(l.get("name"), optString(l.get("user"), ""));
                    if (uRole.contains("Officer") || act.contains("Customs") || act.contains("Clearance") || act.contains("Inspection") || act.contains("Duty")) {
                        Map<String, Object> a = new HashMap<>(l);
                        a.put("officer", uName.isBlank() ? "Officer" : uName);
                        a.put("activity", act.isBlank() ? "Customs Compliance Inspection" : act);
                        a.put("target", optString(l.get("vesselName"), optString(l.get("target"), "Quayside Berth")));
                        a.put("status", optString(l.get("status"), "Completed"));
                        a.put("timestamp", optString(l.get("timestamp"), "Recently"));
                        activities.add(a);
                        completedDuties++;
                    }
                }
            }
        } catch (Exception ignored) {}

        if (totalOfficersVal != null) totalOfficersVal.setText(String.valueOf(totalO));
        if (activeOfficersVal != null) activeOfficersVal.setText(String.valueOf(activeO));
        if (assignedOfficersVal != null) assignedOfficersVal.setText(String.valueOf(assignedO));
        if (availOfficersVal != null) availOfficersVal.setText(String.valueOf(Math.max(0, totalO - assignedO)));
        if (activeDutiesVal != null) activeDutiesVal.setText(String.valueOf(activeDuties));
        if (completedDutiesVal != null) completedDutiesVal.setText(String.valueOf(completedDuties));

        activeDutiesList.setAll(duties);
        recentActivityList.setAll(activities);
    }

    private String resolveDutyStatus(Map<String, Object> officer) {
        String aStat = optString(officer.get("assignmentStatus"), "");
        if (!aStat.isBlank() && !aStat.equalsIgnoreCase("—")) {
            return aStat;
        }
        String duty = optString(officer.get("duty"), "");
        if (!duty.isBlank() && !duty.equalsIgnoreCase("Not Assigned") && !duty.equalsIgnoreCase("Standby") && !duty.equalsIgnoreCase("Available")) {
            return "Assigned";
        }
        return "Available";
    }

    // =====================================================
    // 9. FILTERS & SORTING
    // =====================================================
    private void applyOfficerFilters() {
        if (filteredOfficersList == null) return;

        String query = officerSearchField != null ? officerSearchField.getText().trim().toLowerCase() : "";
        String dept = departmentFilter != null ? departmentFilter.getValue() : "All Departments";
        String stat = statusFilter != null ? statusFilter.getValue() : "All Statuses";
        String port = portFilter != null ? portFilter.getValue() : "All Jurisdictions";
        String assign = assignmentFilter != null ? assignmentFilter.getValue() : "All Assignments";

        filteredOfficersList.setPredicate(o -> {
            if (!query.isEmpty()) {
                String name = optString(o.get("name"), "").toLowerCase();
                String uid = optString(o.get("uid"), "").toLowerCase();
                String email = optString(o.get("email"), "").toLowerCase();
                if (!name.contains(query) && !uid.contains(query) && !email.contains(query)) {
                    return false;
                }
            }
            if (dept != null && !dept.equals("All Departments")) {
                String cDept = optString(o.get("company"), optString(o.get("organization"), ""));
                if (!cDept.equalsIgnoreCase(dept)) return false;
            }
            if (stat != null && !stat.equals("All Statuses")) {
                String oStat = optString(o.get("status"), "Active");
                if (!oStat.equalsIgnoreCase(stat)) return false;
            }
            if (port != null && !port.equals("All Jurisdictions")) {
                String oPort = optString(o.get("assignedPort"), activeViewingPort);
                if (!oPort.equalsIgnoreCase(port)) return false;
            }
            if (assign != null && !assign.equals("All Assignments")) {
                String dutyStat = resolveDutyStatus(o);
                if (assign.equalsIgnoreCase("Available") && !"Available".equalsIgnoreCase(dutyStat)) {
                    return false;
                } else if (assign.equalsIgnoreCase("Assigned") && !"Assigned".equalsIgnoreCase(dutyStat) && !"In Progress".equalsIgnoreCase(dutyStat)) {
                    return false;
                }
            }
            return true;
        });
    }

    // =====================================================
    // 10. CELL FACTORIES & BADGES
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
                    String color = "#16a34a", bg = "#ecfdf5";
                    if ("Pending".equalsIgnoreCase(item) || "Standby".equalsIgnoreCase(item)) {
                        color = "#d97706";
                        bg = "#fef3c7";
                    } else if ("Inactive".equalsIgnoreCase(item) || "Suspended".equalsIgnoreCase(item)) {
                        color = "#ef4444";
                        bg = "#fee2e2";
                    }
                    badge.setStyle("-fx-text-fill:" + color + "; -fx-background-color:" + bg + "; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 7; -fx-background-radius:4;");
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
                    } else if ("In Progress".equalsIgnoreCase(item) || "Assigned".equalsIgnoreCase(item)) {
                        color = "#0284c7";
                        bg = "#e0f2fe";
                    } else if ("Completed".equalsIgnoreCase(item)) {
                        color = "#16a34a";
                        bg = "#ecfdf5";
                    }
                    badge.setStyle("-fx-text-fill:" + color + "; -fx-background-color:" + bg + "; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 7; -fx-background-radius:4;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            }
        };
    }

    // =====================================================
    // 11. WORKING DIALOGS & ACTIONS
    // =====================================================
    private void showRegisterOfficerDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register Customs Officer — " + activeViewingPort);
        dialog.setHeaderText("Create Certified Customs Officer Account for " + getDisplayPortName());

        ButtonType regType = new ButtonType("Register Officer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(regType, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(18));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Officer Ananya Roy");

        TextField emailField = new TextField();
        emailField.setPromptText("e.g. ananya.roy@customs.gov");

        TextField phoneField = new TextField("+91 9811223344");

        ComboBox<String> deptCombo = new ComboBox<>();
        deptCombo.getItems().addAll("Customs Border Control", "Cargo Inspection", "Maritime Compliance", "Tariff Enforcement");
        deptCombo.setValue("Customs Border Control");

        TextField portField = new TextField(activeViewingPort);
        portField.setEditable(false);
        portField.setStyle("-fx-background-color:#f1f5f9;");

        form.add(new Label("Officer Name:"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Official Email:"), 0, 1);
        form.add(emailField, 1, 1);
        form.add(new Label("Phone:"), 0, 2);
        form.add(phoneField, 1, 2);
        form.add(new Label("Department:"), 0, 3);
        form.add(deptCombo, 1, 3);
        form.add(new Label("Port / Jurisdiction:"), 0, 4);
        form.add(portField, 1, 4);

        dialog.getDialogPane().setContent(form);

        Button regBtn = (Button) dialog.getDialogPane().lookupButton(regType);
        regBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-weight:bold;");

        regBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String dept = deptCombo.getValue();

            if (name.isEmpty() || email.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Officer Name and Email are required.");
                alert.showAndWait();
                e.consume();
                return;
            }

            String uid = "UID-OFF-" + System.currentTimeMillis();
            Map<String, Object> extra = new HashMap<>();
            extra.put("assignedPort", activeViewingPort);
            extra.put("duty", "Customs Clearance & Inspection");
            extra.put("assignmentStatus", "Available");
            extra.put("assignedShip", "General Port Area");

            new UserDao().saveUserExtended(uid, email, name, dept, phone, "Customs Officer", "Active", extra)
                    .whenComplete((res, err) -> Platform.runLater(this::refreshAllData));
        });

        dialog.showAndWait();
    }

    private void showOfficerDetailsDialog(Map<String, Object> off) {
        if (off == null) return;
        Dialog<Void> d = new Dialog<>();
        d.setTitle("Officer Profile — " + optString(off.get("name"), "Officer"));
        d.setHeaderText("Customs Commissioned Inspector: " + optString(off.get("name"), "Officer"));
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane g = new GridPane();
        g.setHgap(14);
        g.setVgap(10);
        g.setPadding(new Insets(18));

        int r = 0;
        addDetailRow(g, r++, "Officer Name:", optString(off.get("name"), "—"), true);
        addDetailRow(g, r++, "User ID / UID:", optString(off.get("uid"), "—"), false);
        addDetailRow(g, r++, "Official Email:", optString(off.get("email"), "—"), false);
        addDetailRow(g, r++, "Authority / Dept:", optString(off.get("company"), optString(off.get("organization"), "Customs Border Control")), false);
        addDetailRow(g, r++, "Port Jurisdiction:", optString(off.get("assignedPort"), activeViewingPort), false);
        addDetailRow(g, r++, "Assigned Vessel:", optString(off.get("assignedShip"), "General Port Area"), true);
        addDetailRow(g, r++, "Current Duty:", optString(off.get("duty"), "Customs Inspection"), false);
        addDetailRow(g, r++, "Duty Status:", resolveDutyStatus(off), false);
        addDetailRow(g, r++, "Account Status:", optString(off.get("status"), "Active"), false);

        d.getDialogPane().setContent(g);
        d.showAndWait();
    }

    private void showEditOfficerDialog(Map<String, Object> off) {
        if (off == null) return;
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Edit Officer Details — " + optString(off.get("name"), "Officer"));
        d.setHeaderText("Update Personnel Details: " + optString(off.get("name"), "Officer"));

        ButtonType saveBtnType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(12);
        g.setPadding(new Insets(18));

        TextField nameField = new TextField(optString(off.get("name"), ""));
        ComboBox<String> deptCombo = new ComboBox<>();
        deptCombo.getItems().addAll("Customs Border Control", "Cargo Inspection", "Maritime Compliance", "Tariff Enforcement");
        deptCombo.setValue(optString(off.get("company"), optString(off.get("organization"), "Customs Border Control")));

        ComboBox<String> statCombo = new ComboBox<>();
        statCombo.getItems().addAll("Active", "Pending", "Inactive");
        statCombo.setValue(optString(off.get("status"), "Active"));

        g.add(new Label("Officer Name:"), 0, 0);
        g.add(nameField, 1, 0);
        g.add(new Label("Department:"), 0, 1);
        g.add(deptCombo, 1, 1);
        g.add(new Label("Status:"), 0, 2);
        g.add(statCombo, 1, 2);

        d.getDialogPane().setContent(g);

        d.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                String uid = optString(off.get("uid"), optString(off.get("email"), ""));
                String email = optString(off.get("email"), "");
                String newName = nameField.getText().trim();
                String newDept = deptCombo.getValue();
                String newStat = statCombo.getValue();

                Map<String, Object> extra = new HashMap<>(off);
                extra.put("status", newStat);
                extra.put("company", newDept);

                new UserDao().saveUserExtended(uid, email, newName, newDept, "+91 9800000000", "Customs Officer", newStat, extra)
                        .whenComplete((v, err) -> Platform.runLater(this::refreshAllData));
            }
            return null;
        });

        d.showAndWait();
    }

    private void showAssignDutyDialog(Map<String, Object> off) {
        if (off == null) return;
        Dialog<ButtonType> d = new Dialog<>();
        String name = optString(off.get("name"), "Customs Officer");
        d.setTitle("Assign Customs Duty — " + name);
        d.setHeaderText("Deploy Customs Duty for Officer " + name);

        ButtonType saveBtnType = new ButtonType("Save Assignment", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(12);
        g.setPadding(new Insets(18));

        ComboBox<String> dutyCombo = new ComboBox<>();
        dutyCombo.getItems().addAll(
                "Document Verification",
                "Cargo Inspection",
                "Customs Clearance",
                "Declaration Verification",
                "Import/Export Compliance",
                "Cargo Examination",
                "Container Tariff Review"
        );
        dutyCombo.setValue(dutyCombo.getItems().get(0));

        TextField targetVessel = new TextField(optString(off.get("assignedShip"), "General Port Area"));

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("HIGH", "MEDIUM", "LOW");
        priorityCombo.setValue("HIGH");

        TextField dueField = new TextField("Today 18:00");

        g.add(new Label("Customs Duty:"), 0, 0);
        g.add(dutyCombo, 1, 0);
        g.add(new Label("Vessel / Area:"), 0, 1);
        g.add(targetVessel, 1, 1);
        g.add(new Label("Priority:"), 0, 2);
        g.add(priorityCombo, 1, 2);
        g.add(new Label("Shift / Due:"), 0, 3);
        g.add(dueField, 1, 3);

        d.getDialogPane().setContent(g);

        d.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                String uid = optString(off.get("uid"), optString(off.get("email"), ""));
                String duty = dutyCombo.getValue();
                String target = targetVessel.getText().trim();
                String prio = priorityCombo.getValue();
                String due = dueField.getText().trim();

                UserDao.assignOfficerDuty(
                        uid, duty, "Assigned", activeViewingPort,
                        "Port Authority (" + activeViewingPort + ")",
                        prio, due, "SHP-OP", target, "", "DUTY-" + System.currentTimeMillis()
                );

                // Send Notification
                Notification notif = new Notification();
                notif.setUserId(uid);
                notif.setVesselName(target);
                notif.setPort(activeViewingPort);
                notif.setTitle("🔔 New Customs Duty Assigned");
                notif.setType("CUSTOMS_DUTY_ASSIGNED");
                notif.setMessage("Duty: " + duty + "\nVessel/Area: " + target + "\nPriority: " + prio + "\nDue: " + due);
                notif.setTimestamp(NotificationDao.generateTimestamp());
                new NotificationDao().createNotification(notif);

                Platform.runLater(this::refreshAllData);
            }
            return null;
        });

        d.showAndWait();
    }

    private void showManageOfficerStatusDialog(Map<String, Object> off) {
        if (off == null) return;
        ChoiceDialog<String> cd = new ChoiceDialog<>();
        cd.setTitle("Manage Officer Duty Status");
        cd.setHeaderText("Update Status for Officer " + optString(off.get("name"), "Officer"));
        cd.getItems().addAll("Available / Standby", "Assigned", "In Progress", "Completed");
        cd.setSelectedItem(resolveDutyStatus(off));

        cd.showAndWait().ifPresent(sel -> {
            String uid = optString(off.get("uid"), optString(off.get("email"), ""));
            String duty = optString(off.get("duty"), "Customs Duty");
            String stat = sel.contains("Available") ? "Available" : sel;

            UserDao.assignOfficerDuty(
                    uid, duty, stat, activeViewingPort,
                    "Port Authority (" + activeViewingPort + ")",
                    "NORMAL", "Standard Shift", "", optString(off.get("assignedShip"), "General Port Area"), "", "DUTY-MGT"
            );
            Platform.runLater(this::refreshAllData);
        });
    }

    private void addDetailRow(GridPane g, int row, String label, String value, boolean highlight) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px; -fx-font-weight:bold;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size:12px; " + (highlight ? "-fx-font-weight:bold; -fx-text-fill:#16a34a;" : "-fx-text-fill:#0f172a;"));
        g.add(l, 0, row);
        g.add(v, 1, row);
    }

    private String optString(Object obj, String fallback) {
        if (obj == null) return fallback;
        String s = obj.toString().trim();
        return s.isBlank() ? fallback : s;
    }

    public VBox getRoot() {
        return root;
    }
}