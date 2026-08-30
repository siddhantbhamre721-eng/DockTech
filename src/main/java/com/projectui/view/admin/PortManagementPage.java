package com.projectui.view.admin;

import java.util.List;
import java.util.ArrayList;

import com.projectui.view.admin.AdminDataStore.Port;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;

import com.projectui.dao.UserDao;

public class PortManagementPage extends AdminTheme {

    private final UserDao userDao = new UserDao();
    private final com.projectui.dao.PortRequestDao portRequestDao = new com.projectui.dao.PortRequestDao();
    private final com.projectui.service.EmailNotificationService emailService = new com.projectui.service.EmailNotificationService();
    private final AdminDataStore store = AdminDataStore.getInstance();
    private final List<Port> ports = store.ports;
    private final GridPane table = new GridPane();

    // Summary Metric Labels
    private Label totalValue;
    private Label activeValue;
    private Label maintenanceValue;
    private Label congestionValue;
    private Label vesselsValue;
    private Label berthValue;

    // Filter & Search Controls
    private TextField search;
    private ComboBox<String> statusFilter;

    // Add / Edit Form Controls
    private TextField portName;
    private TextField location;
    private TextField portCodeField;
    private TextField berthCount;
    private TextField portCapacity;
    private TextField maintenanceInfo;
    private TextField latField;
    private TextField lngField;
    private ComboBox<String> status;
    private Button saveBtn;
    private Button clearBtn;
    private Button addTabBtn;
    private Button editTabBtn;
    private Label formTitle;
    private Label formSubtitle;
    private Label formModeBadge;

    private int editingIndex = -1;
    private final boolean startAddOnOpen;

    public PortManagementPage() {
        this(false);
    }

    public PortManagementPage(boolean startAddOnOpen) {
        this.startAddOnOpen = startAddOnOpen;
        this.portRequestDao.syncWithDatabase();
    }

    public Parent getRoot() {
        // Page Header Buttons
        Button addQuickBtn = primaryButton("+  Add Port");
        addQuickBtn.setOnAction(event -> startAdd());
        addQuickBtn.setStyle("-fx-background-color:" + BLUE
                + "; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:10 22; -fx-background-radius:8; -fx-cursor:hand;");

        Button editQuickBtn = new Button("✎  Edit Port");
        editQuickBtn.setStyle(
                "-fx-background-color:#0aa05f; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:10 22; -fx-background-radius:8; -fx-cursor:hand;");
        editQuickBtn.setOnAction(event -> {
            if (editingIndex >= 0 && editingIndex < ports.size()) {
                startEdit(editingIndex);
            } else if (!ports.isEmpty()) {
                startEdit(0);
            }
        });

        Label title = new Label("Port Management");
        title.setStyle("-fx-text-fill:#05070c; -fx-font-size:26; -fx-font-weight:bold;");
        Label subtitle = new Label(
                "Monitor and manage port operations, capacity, vessel traffic, berths and operational conditions.");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:14;");
        VBox titleCopy = new VBox(2, title, subtitle);
        Region titleSpace = new Region();
        HBox.setHgrow(titleSpace, Priority.ALWAYS);
        HBox titleRow = new HBox(12, titleCopy, titleSpace, addQuickBtn, editQuickBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox page = pageContent();
        page.setSpacing(14);
        page.setPadding(new Insets(16, 20, 20, 20));

        // 1. Top Summary Cards (6 KPIs)
        HBox stats = new HBox(12,
                statBox("Total Ports", "0", "All Registered", "⚓", BLUE, "#eff6ff", label -> totalValue = label),
                statBox("Active Ports", "0", "Operational", "✓", "#16a34a", "#ecfdf5", label -> activeValue = label),
                statBox("Maintenance", "0", "Under Repair", "⌁", BLUE, "#eff6ff", label -> maintenanceValue = label),
                statBox("High Congestion", "0", "Requires Attention", "!", "#ef4444", "#fef2f2",
                        label -> congestionValue = label),
                statBox("Vessels in Port", "0", "Currently Docked", "▣", "#0b63ff", "#f5f3ff",
                        label -> vesselsValue = label),
                statBox("Berth Usage", "0 / 0", "Total Berths Occupied", "▤", "#f97316", "#fff7ed",
                        label -> berthValue = label));
        stats.getChildren().forEach(node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            if (node instanceof VBox)
                ((VBox) node).setMaxWidth(Double.MAX_VALUE);
        });

        // 2. Main Section: Left Control Center Table (52%) + Right Add/Edit Panel (48%)
        Region listCard = portListCard();
        Region formCard = portFormCard();
        HBox main = new HBox(14, listCard, formCard);
        listCard.prefWidthProperty().bind(main.widthProperty().multiply(0.52));
        formCard.prefWidthProperty().bind(main.widthProperty().multiply(0.48));
        HBox.setHgrow(listCard, Priority.ALWAYS);
        HBox.setHgrow(formCard, Priority.ALWAYS);

        // 3. New Port Requests Section (Pending Admin Verification)
        VBox requestsCard = portRequestsCard();

        // 4. Supporting Bottom Cards
        HBox bottom = new HBox(12, operationalSummaryCard(), recentPortActivityCard(), capacityOverviewCard());
        bottom.getChildren().forEach(node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            if (node instanceof VBox)
                ((VBox) node).setMaxWidth(Double.MAX_VALUE);
        });

        page.getChildren().addAll(titleRow, main, requestsCard);

        refreshTable();
        refreshStats();
        if (startAddOnOpen) {
            startAdd();
        } else if (!ports.isEmpty()) {
            startEdit(0);
        }

        // Smooth ScrollPane wrapper
        ScrollPane scroller = new ScrollPane(page);
        scroller.setFitToWidth(true);
        scroller.setPannable(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroller.setStyle("-fx-background:transparent; -fx-background-color:transparent; -fx-padding:0;");

        return scroller;
    }

    // ==========================================
    // PORT OPERATIONS CONTROL CENTER TABLE (LEFT)
    // ==========================================
    private VBox portListCard() {
        search = new TextField();
        search.setPromptText("⌕  Search ports by name, location, code...");
        search.setPrefWidth(260);
        styleCustomField(search);
        search.textProperty().addListener((obs, oldValue, newValue) -> refreshTable());

        statusFilter = combo("Filter", "All Status", "Operational", "Busy", "High Congestion", "Maintenance", "Closed",
                "Restricted");
        statusFilter.setPrefWidth(140);
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> refreshTable());

        Button refresh = new Button("↻  Refresh");
        refresh.setStyle(
                "-fx-background-color:#f1f5f9;" +
                        "-fx-text-fill:#475569;" +
                        "-fx-font-size:13;" +
                        "-fx-font-weight:bold;" +
                        "-fx-padding:8 14;" +
                        "-fx-background-radius:6;" +
                        "-fx-cursor:hand;");
        refresh.setOnAction(event -> {
            refreshTable();
            refreshStats();
        });

        Label title = new Label("Port Operations Control Center");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");

        Label subtitle = new Label("Capacity, vessel traffic, berth usage and operating conditions");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox filters = new HBox(10, search, statusFilter, refresh);
        filters.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(search, Priority.ALWAYS);

        HBox top = new HBox(14, new VBox(2, title, subtitle), space);
        top.setAlignment(Pos.CENTER_LEFT);

        // 7-column table layout using ColumnConstraints
        table.setHgap(0);
        table.setVgap(0);
        table.setMaxWidth(Double.MAX_VALUE);
        table.setStyle(
                "-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(22); // Port
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(15); // Location
        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(16); // Status
        ColumnConstraints c4 = new ColumnConstraints();
        c4.setPercentWidth(10); // Vessels
        ColumnConstraints c5 = new ColumnConstraints();
        c5.setPercentWidth(17); // Berth Usage
        ColumnConstraints c6 = new ColumnConstraints();
        c6.setPercentWidth(10); // Capacity
        ColumnConstraints c7 = new ColumnConstraints();
        c7.setPercentWidth(10); // Action
        table.getColumnConstraints().setAll(c1, c2, c3, c4, c5, c6, c7);

        Label showing = new Label("Showing " + ports.size() + " of " + ports.size() + " registered ports");
        showing.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        Region pagerSpace = new Region();
        HBox.setHgrow(pagerSpace, Priority.ALWAYS);
        Label pager = new Label("‹    1    ›");
        pager.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:15; -fx-font-weight:bold;");
        HBox footer = new HBox(8, showing, pagerSpace, pager);
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(14, top, filters, table, footer);
        card.setPadding(new Insets(18));
        card.setStyle(cardStyle());
        return card;
    }

    // ==========================================
    // ADD / EDIT PORT FORM PANEL (RIGHT ⭐⭐⭐)
    // ==========================================
    private VBox portFormCard() {
        // Tab Controls
        addTabBtn = new Button("➕  Add New Port");
        editTabBtn = new Button("✎  Edit Port");

        addTabBtn.setOnAction(e -> startAdd());
        editTabBtn.setOnAction(e -> {
            if (editingIndex >= 0 && editingIndex < ports.size()) {
                startEdit(editingIndex);
            } else if (!ports.isEmpty()) {
                startEdit(0);
            }
        });

        HBox tabs = new HBox(8, addTabBtn, editTabBtn);
        tabs.setAlignment(Pos.CENTER_LEFT);

        formTitle = new Label("Add New Port");
        formTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");

        formSubtitle = new Label("Create a new registered maritime port facility.");
        formSubtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");

        formModeBadge = new Label("MODE: ADD");
        formModeBadge.setStyle(
                "-fx-background-color:#eff6ff;" +
                        "-fx-text-fill:" + BLUE + ";" +
                        "-fx-font-size:12;" +
                        "-fx-font-weight:bold;" +
                        "-fx-padding:4 10;" +
                        "-fx-background-radius:6;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox formHeaderRow = new HBox(8, new VBox(2, formTitle, formSubtitle), space, formModeBadge);
        formHeaderRow.setAlignment(Pos.CENTER_LEFT);

        // Form Fields
        portName = new TextField();
        location = new TextField();
        portCodeField = new TextField();
        berthCount = new TextField();
        portCapacity = new TextField();
        maintenanceInfo = new TextField();
        latField = new TextField();
        lngField = new TextField();
        status = combo("Status", "Operational", "Busy", "High Congestion", "Maintenance", "Closed", "Restricted");

        portName.setPromptText("e.g. Port of JNPT");
        location.setPromptText("e.g. Navi Mumbai, MH");
        portCodeField.setPromptText("e.g. IN-BOM-01");
        berthCount.setPromptText("e.g. 18");
        portCapacity.setPromptText("e.g. 85%");
        maintenanceInfo.setPromptText("Enter current maintenance schedule or operational notices...");
        latField.setPromptText("e.g. 18.9439");
        lngField.setPromptText("e.g. 72.8354");

        styleCustomField(portName);
        styleCustomField(location);
        styleCustomField(portCodeField);
        styleCustomField(berthCount);
        styleCustomField(portCapacity);
        styleCustomField(maintenanceInfo);
        styleCustomField(latField);
        styleCustomField(lngField);

        // Action Buttons
        saveBtn = primaryButton("💾  Save Changes");
        saveBtn.setStyle(
                "-fx-background-color:" + BLUE + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:14;" +
                        "-fx-font-weight:bold;" +
                        "-fx-padding:10 20;" +
                        "-fx-background-radius:6;" +
                        "-fx-cursor:hand;");
        saveBtn.setOnAction(event -> savePort());

        clearBtn = new Button("🧹  Clear Form");
        clearBtn.setStyle(
                "-fx-background-color:#f1f5f9;" +
                        "-fx-text-fill:#475569;" +
                        "-fx-font-size:14;" +
                        "-fx-font-weight:bold;" +
                        "-fx-padding:10 16;" +
                        "-fx-background-radius:6;" +
                        "-fx-cursor:hand;");
        clearBtn.setOnAction(event -> clearFormFields());

        Button deletePortBtn = new Button("🗑  Delete Port");
        deletePortBtn.setStyle(
                "-fx-background-color:#fef2f2;" +
                        "-fx-text-fill:#ef4444;" +
                        "-fx-font-size:14;" +
                        "-fx-font-weight:bold;" +
                        "-fx-padding:10 16;" +
                        "-fx-background-radius:6;" +
                        "-fx-cursor:hand;");
        deletePortBtn.setOnAction(event -> {
            if (editingIndex >= 0 && editingIndex < ports.size()) {
                deletePortAtIndex(editingIndex);
            }
        });

        HBox buttonRow = new HBox(10, deletePortBtn, clearBtn, saveBtn);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        VBox gridFields = new VBox(10,
                new HBox(10, labeledControl("Port Name *", portName), labeledControl("Location *", location)),
                new HBox(10, labeledControl("Port Code *", portCodeField), labeledControl("Port Status *", status)),
                new HBox(10, labeledControl("Latitude (-90 to +90) *", latField),
                        labeledControl("Longitude (-180 to +180) *", lngField)),
                new HBox(10, labeledControl("Port Capacity (%) *", portCapacity),
                        labeledControl("Number of Berths *", berthCount)),
                labeledControl("Maintenance Information & Notices", maintenanceInfo));

        // Allow HBox children in form grid to grow equally
        gridFields.getChildren().forEach(node -> {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                for (var child : row.getChildren()) {
                    HBox.setHgrow(child, Priority.ALWAYS);
                    if (child instanceof VBox)
                        ((VBox) child).setMaxWidth(Double.MAX_VALUE);
                }
            }
        });

        VBox card = new VBox(12,
                tabs,
                formHeaderRow,
                gridFields,
                buttonRow);
        card.setPadding(new Insets(18));
        card.setStyle(cardStyle());

        updateTabStyles();
        return card;
    }

    private void updateTabStyles() {
        boolean isEditing = editingIndex >= 0 && editingIndex < ports.size();
        if (isEditing) {
            addTabBtn.setStyle(
                    "-fx-background-color:#f1f5f9;" +
                            "-fx-text-fill:#475569;" +
                            "-fx-font-size:13;" +
                            "-fx-font-weight:bold;" +
                            "-fx-padding:7 16;" +
                            "-fx-background-radius:6;" +
                            "-fx-cursor:hand;");
            editTabBtn.setStyle(
                    "-fx-background-color:" + BLUE + ";" +
                            "-fx-text-fill:white;" +
                            "-fx-font-size:13;" +
                            "-fx-font-weight:bold;" +
                            "-fx-padding:7 16;" +
                            "-fx-background-radius:6;" +
                            "-fx-cursor:hand;");
        } else {
            addTabBtn.setStyle(
                    "-fx-background-color:" + BLUE + ";" +
                            "-fx-text-fill:white;" +
                            "-fx-font-size:13;" +
                            "-fx-font-weight:bold;" +
                            "-fx-padding:7 16;" +
                            "-fx-background-radius:6;" +
                            "-fx-cursor:hand;");
            editTabBtn.setStyle(
                    "-fx-background-color:#f1f5f9;" +
                            "-fx-text-fill:#475569;" +
                            "-fx-font-size:13;" +
                            "-fx-font-weight:bold;" +
                            "-fx-padding:7 16;" +
                            "-fx-background-radius:6;" +
                            "-fx-cursor:hand;");
        }
    }

    // ==========================================
    // REFRESH TABLE & DATA
    // ==========================================
    private void refreshTable() {
        table.getChildren().clear();

        // 7 Columns: Port, Location, Status, Vessels, Berth Usage, Capacity, Action
        String[] headers = { "Port", "Location", "Status", "Vessels", "Berth Usage", "Capacity", "Action" };
        for (int col = 0; col < headers.length; col++) {
            table.add(headerCell(headers[col]), col, 0);
        }

        int displayRow = 1;
        String query = search == null || search.getText() == null ? "" : search.getText().trim().toLowerCase();
        for (int i = 0; i < ports.size(); i++) {
            Port port = ports.get(i);
            if (!matches(port, query)) {
                continue;
            }

            HBox portCell = portNameCell(port);
            Label locationCell = bodyCell(port.location);
            HBox statusCell = statusBadge(port.status);
            Label vesselCell = bodyCell(String.valueOf(port.vessels));
            HBox berthCell = berthUsageCell(port);
            Label capacityCell = bodyCell(port.capacity);

            final int rowIndex = i;
            HBox actionCell = createEditActionCell(rowIndex);

            portCell.setOnMouseClicked(event -> startEdit(rowIndex));
            locationCell.setOnMouseClicked(event -> startEdit(rowIndex));

            table.add(portCell, 0, displayRow);
            table.add(locationCell, 1, displayRow);
            table.add(statusCell, 2, displayRow);
            table.add(vesselCell, 3, displayRow);
            table.add(berthCell, 4, displayRow);
            table.add(capacityCell, 5, displayRow);
            table.add(actionCell, 6, displayRow);
            displayRow++;
        }

        if (displayRow == 1) {
            Label empty = bodyCell("No ports found matching filter criteria.");
            empty.setMaxWidth(Double.MAX_VALUE);
            table.add(empty, 0, 1, 7, 1);
        }
    }

    private void refreshStats() {
        int active = 0;
        int maintenance = 0;
        int highCongestion = 0;
        int vessels = 0;
        int berths = 0;
        int occupied = 0;
        for (Port port : ports) {
            if ("Operational".equals(port.status)) {
                active++;
            }
            if ("Maintenance".equals(port.status)) {
                maintenance++;
            }
            if ("High Congestion".equals(port.status) || "Busy".equals(port.status)
                    || port.vessels >= port.berths * 0.85) {
                highCongestion++;
            }
            vessels += port.vessels;
            berths += port.berths;
            occupied += port.occupiedBerths();
        }

        if (totalValue != null)
            totalValue.setText(String.valueOf(ports.size()));
        if (activeValue != null)
            activeValue.setText(String.valueOf(active));
        if (maintenanceValue != null)
            maintenanceValue.setText(String.valueOf(maintenance));
        if (congestionValue != null)
            congestionValue.setText(String.valueOf(highCongestion));
        if (vesselsValue != null)
            vesselsValue.setText(String.valueOf(vessels));
        if (berthValue != null)
            berthValue.setText(occupied + " / " + berths);
    }

    private void startAdd() {
        editingIndex = -1;
        formTitle.setText("Add New Port");
        formSubtitle.setText("Create a new registered maritime port facility.");
        formModeBadge.setText("MODE: ADD");
        formModeBadge.setStyle("-fx-background-color:#eff6ff; -fx-text-fill:" + BLUE
                + "; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:6;");

        clearFormFields();
        updateTabStyles();
        if (portName != null)
            portName.requestFocus();
    }

    private void startEdit(int index) {
        if (index < 0 || index >= ports.size())
            return;
        editingIndex = index;
        Port port = ports.get(index);

        formTitle.setText("Edit Port");
        formSubtitle.setText("Modifying details for: " + port.name);
        formModeBadge.setText("MODE: EDITING #" + (index + 1));
        formModeBadge.setStyle(
                "-fx-background-color:#ecfdf5; -fx-text-fill:#047857; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:6;");

        portName.setText(port.name);
        location.setText(port.location);
        portCodeField.setText(port.code != null ? port.code
                : "IN-" + port.name.replaceAll("[^A-Za-z]", "").toUpperCase().substring(0,
                        Math.min(3, port.name.length())) + "-0" + (index + 1));
        berthCount.setText(String.valueOf(port.berths));
        portCapacity.setText(port.capacity);
        maintenanceInfo.setText(port.maintenance);
        status.setValue(port.status);
        if (latField != null)
            latField.setText(port.lat != 0.0 ? String.valueOf(port.lat) : "");
        if (lngField != null)
            lngField.setText(port.lng != 0.0 ? String.valueOf(port.lng) : "");

        updateTabStyles();
    }

    private void clearFormFields() {
        if (portName != null)
            portName.clear();
        if (location != null)
            location.clear();
        if (portCodeField != null)
            portCodeField.clear();
        if (berthCount != null)
            berthCount.clear();
        if (portCapacity != null)
            portCapacity.clear();
        if (maintenanceInfo != null)
            maintenanceInfo.clear();
        if (latField != null)
            latField.clear();
        if (lngField != null)
            lngField.clear();
        if (status != null)
            status.setValue("Operational");
    }

    private void savePort() {
        String nameValue = portName.getText().trim();
        if (nameValue.isEmpty()) {
            formSubtitle.setText("⚠️ Please enter a Port Name.");
            return;
        }

        // Check duplicate port names for new port creations
        if (editingIndex == -1) {
            boolean isDuplicate = ports.stream().anyMatch(p -> p.name != null && p.name.equalsIgnoreCase(nameValue));
            if (isDuplicate) {
                formSubtitle.setText("⚠️ A port named '" + nameValue + "' already exists in the database.");
                return;
            }
        }

        String latText = latField.getText().trim();
        String lngText = lngField.getText().trim();

        // If coordinates are already present (or editing existing port with
        // coordinates), perform save directly
        if (!latText.isEmpty() && !lngText.isEmpty()) {
            performPortSaveDirect();
            return;
        }

        // Automatically search real-world location via Geocoding API using Port Name
        formSubtitle.setText("⏳ Geocoding '" + nameValue + "' via real-world location API...");
        saveBtn.setDisable(true);

        com.projectui.service.GeocodingService.getInstance().searchPortLocation(nameValue)
                .whenComplete((locations, err) -> {
                    javafx.application.Platform.runLater(() -> {
                        saveBtn.setDisable(false);
                        if (err != null || locations == null || locations.isEmpty()) {
                            formSubtitle
                                    .setText("⚠️ Invalid or unavailable port location. Geocoding API could not locate '"
                                            + nameValue + "'. Please check spelling or enter coordinates manually.");
                            return;
                        }

                        if (locations.size() == 1) {
                            com.projectui.service.GeocodingService.GeocodedLocation loc = locations.get(0);
                            applyGeocodedLocation(loc);
                            performPortSaveDirect();
                        } else {
                            showLocationSelectionModal(nameValue, locations);
                        }
                    });
                });
    }

    private void applyGeocodedLocation(com.projectui.service.GeocodingService.GeocodedLocation loc) {
        latField.setText(String.format(java.util.Locale.US, "%.4f", loc.lat));
        lngField.setText(String.format(java.util.Locale.US, "%.4f", loc.lng));
        if (location.getText().trim().isEmpty() || location.getText().contains("e.g.")) {
            location.setText(loc.displayName);
        }
        if (portCodeField.getText().trim().isEmpty()) {
            String pName = portName.getText().trim();
            String codePrefix = pName.replaceAll("[^A-Za-z]", "").toUpperCase();
            if (codePrefix.length() < 3)
                codePrefix = (codePrefix + "PRT").substring(0, 3);
            else
                codePrefix = codePrefix.substring(0, 3);
            portCodeField.setText("IN-" + codePrefix + "-01");
        }
        if (berthCount.getText().trim().isEmpty()) {
            berthCount.setText("14");
        }
        if (portCapacity.getText().trim().isEmpty()) {
            portCapacity.setText("85%");
        }
        if (maintenanceInfo.getText().trim().isEmpty()) {
            maintenanceInfo.setText("Clear");
        }
    }

    private void showLocationSelectionModal(String portNameQuery,
            List<com.projectui.service.GeocodingService.GeocodedLocation> locations) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Select Real-World Port Location");

        VBox root = new VBox(12);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color:white;");

        Label title = new Label("📍 Select Location for '" + portNameQuery + "'");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16; -fx-font-weight:bold;");

        Label sub = new Label("Multiple matching real-world locations found via Geocoding API:");
        sub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        javafx.scene.control.ListView<com.projectui.service.GeocodingService.GeocodedLocation> listView = new javafx.scene.control.ListView<>();
        listView.getItems().addAll(locations);
        listView.getSelectionModel().select(0);
        listView.setPrefHeight(180);

        Button confirmBtn = primaryButton("Confirm Selected Location & Save Port");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setOnAction(e -> {
            com.projectui.service.GeocodingService.GeocodedLocation sel = listView.getSelectionModel()
                    .getSelectedItem();
            if (sel != null) {
                applyGeocodedLocation(sel);
                modal.close();
                performPortSaveDirect();
            }
        });

        root.getChildren().addAll(title, sub, listView, confirmBtn);
        Scene scene = new Scene(root, 540, 330);
        modal.setScene(scene);
        modal.showAndWait();
    }

    private void performPortSaveDirect() {
        String nameValue = portName.getText().trim();
        String locationValue = location.getText().trim();
        String berthsValue = berthCount.getText().trim();
        String capacityValue = portCapacity.getText().trim();
        String maintenanceValue = maintenanceInfo.getText().trim();
        String statusValue = status.getValue();
        String latText = latField.getText().trim();
        String lngText = lngField.getText().trim();

        if (nameValue.isEmpty()) {
            formSubtitle.setText("⚠️ Please enter a Port Name.");
            return;
        }

        int berths = 12;
        try {
            if (!berthsValue.isEmpty())
                berths = Integer.parseInt(berthsValue);
        } catch (Exception ignored) {
        }

        double lat = 0.0;
        double lng = 0.0;
        try {
            if (!latText.isEmpty())
                lat = Double.parseDouble(latText);
            if (!lngText.isEmpty())
                lng = Double.parseDouble(lngText);
        } catch (Exception ignored) {
        }

        String pCode = portCodeField.getText().trim();
        if (pCode.isEmpty()) {
            String prefix = nameValue.replaceAll("[^A-Za-z]", "").toUpperCase();
            pCode = "IN-" + (prefix.length() >= 3 ? prefix.substring(0, 3) : "PRT") + "-01";
            portCodeField.setText(pCode);
        }

        if (locationValue.isEmpty()) {
            locationValue = nameValue + ", Maritime Sector";
            location.setText(locationValue);
        }

        if (editingIndex >= 0 && editingIndex < ports.size()) {
            Port old = ports.get(editingIndex);
            old.name = nameValue;
            old.location = locationValue;
            old.code = pCode;
            old.berths = berths;
            old.status = statusValue != null ? statusValue : "Operational";
            old.capacity = capacityValue.isEmpty() ? "85%" : capacityValue;
            old.maintenance = maintenanceValue.isEmpty() ? "Clear" : maintenanceValue;
            old.lastUpdated = now();
            old.lat = lat;
            old.lng = lng;
            formSubtitle.setText("✓ Port updated successfully!");
        } else {
            // Check duplicate
            boolean duplicate = ports.stream().anyMatch(p -> p.name != null && p.name.equalsIgnoreCase(nameValue));
            if (duplicate) {
                formSubtitle.setText("⚠️ A port named '" + nameValue + "' already exists in the database.");
                return;
            }

            Port newPort = new Port(nameValue, locationValue, pCode, "Port Authority (" + nameValue + ")", berths, 0,
                    statusValue != null ? statusValue : "Operational", "Containers",
                    capacityValue.isEmpty() ? "85%" : capacityValue,
                    maintenanceValue.isEmpty() ? "Clear" : maintenanceValue, now(), lat, lng, "APPROVED");
            ports.add(newPort);
            editingIndex = ports.size() - 1;

            formSubtitle.setText(
                    "✓ Port '" + nameValue + "' created at Lat: " + String.format(java.util.Locale.US, "%.4f", lat)
                            + ", Lng: " + String.format(java.util.Locale.US, "%.4f", lng) + "!");

            String paEmail = "pa_" + nameValue.toLowerCase().replaceAll("[^a-z0-9]", "") + "@docktech.com";
            String paPhone = "+91 98" + (int) (Math.random() * 89999999 + 10000000);
            userDao.provisionAccount("Admin", "Port Authority (" + nameValue + ")", paEmail, nameValue, paPhone,
                    "Port Authority", "Active");
        }

        GeoTileMapCanvas.refreshAllMapInstances();
        refreshTable();
        refreshStats();
        if (editingIndex >= 0 && editingIndex < ports.size()) {
            startEdit(editingIndex);
        }
    }

    // ==========================================
    // SUPPORTING BOTTOM CARDS
    // ==========================================
    private VBox operationalSummaryCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(16));
        card.setStyle(cardStyle());
        Label title = new Label("Operational Summary");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        HBox tiles = new HBox(12,
                summaryTile("✓", "Operational", countOperational(), "#16a34a", "#ecfdf5"),
                summaryTile("⌁", "Maintenance", countMaintenance(), BLUE, "#eff6ff"),
                summaryTile("!", "High Congestion", countHighCongestion(), "#ef4444", "#fef2f2"),
                summaryTile("⊘", "Inactive", countInactive(), "#64748b", "#f8fafc"));
        tiles.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));
        card.getChildren().addAll(title, tiles);
        return card;
    }

    private VBox recentPortActivityCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle(cardStyle());
        Label title = new Label("Recent Port Activity");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label link = new Label("View all activity →");
        link.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:13; -fx-font-weight:bold; -fx-cursor:hand;");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox header = new HBox(8, title, space, link);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox rows = new VBox(8);
        int limit = Math.min(4, ports.size());
        for (int i = 0; i < limit; i++) {
            Port port = ports.get(i);
            rows.getChildren().add(activityRow(port));
        }
        card.getChildren().addAll(header, rows);
        return card;
    }

    private VBox capacityOverviewCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle(cardStyle());
        Label title = new Label("Port Capacity Overview");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label link = new Label("View full report →");
        link.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:13; -fx-font-weight:bold; -fx-cursor:hand;");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox header = new HBox(8, title, space, link);
        header.setAlignment(Pos.CENTER_LEFT);

        int average = averageCapacity();
        StackPane donut = capacityDonut(average);
        VBox legend = new VBox(8,
                capacityLegend("Operational Ports", countOperational(), ports.size(), "#16a34a"),
                capacityLegend("Maintenance Ports", countMaintenance(), ports.size(), BLUE),
                capacityLegend("High Congestion Ports", countHighCongestion(), ports.size(), "#ef4444"),
                capacityLegend("Inactive Ports", countInactive(), ports.size(), "#94a3b8"));
        HBox middle = new HBox(16, donut, legend);
        middle.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(legend, Priority.ALWAYS);

        HBox footer = new HBox(12, new Label("▣"),
                new VBox(2, boldLabel("Total Berths"),
                        mutedLabel(totalOccupiedBerths() + " / " + totalBerths() + " Used")),
                new Region(), new VBox(2, boldLabel(average + "%"), mutedLabel("Average Usage")));
        HBox.setHgrow(footer.getChildren().get(2), Priority.ALWAYS);
        footer.setPadding(new Insets(10, 12, 10, 12));
        footer.setStyle(
                "-fx-background-color:#f8fafc; -fx-border-color:#e5edf6; -fx-border-radius:8; -fx-background-radius:8;");
        footer.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(header, middle, footer);
        return card;
    }

    private VBox portRequestsCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle(cardStyle());

        Label title = new Label("📋 New Port Requests (Pending Admin Verification)");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label(
                "Review port establishment proposals submitted by requesters, approve active ports, and provision Port Authority accounts.");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:" + BLUE
                + "; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:6; -fx-cursor:hand;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox headerRow = new HBox(10, new VBox(2, title, subtitle), space, refreshBtn);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox requestsContainer = new VBox(8);
        renderPortRequests(requestsContainer);

        refreshBtn.setOnAction(e -> {
            refreshBtn.setText("⏳ Refreshing...");
            refreshBtn.setDisable(true);

            this.portRequestDao.syncWithDatabase().whenComplete((list, err) -> {
                javafx.application.Platform.runLater(() -> {
                    renderPortRequests(requestsContainer);
                    refreshBtn.setText("↻ Refresh");
                    refreshBtn.setDisable(false);
                });
            });
        });

        card.getChildren().addAll(headerRow, requestsContainer);
        return card;
    }

    private void renderPortRequests(VBox container) {
        container.getChildren().clear();

        List<AdminDataStore.PortRequest> allRequests = store.portRequests;

        // Filter ONLY PENDING requests for the Pending Admin Verification section
        List<AdminDataStore.PortRequest> pendingList = allRequests.stream()
                .filter(r -> r.status != null && "PENDING".equalsIgnoreCase(r.status.trim()))
                .filter(r -> !"APPROVED".equalsIgnoreCase(r.status) && !"REJECTED".equalsIgnoreCase(r.status)
                        && !"COMPLETED".equalsIgnoreCase(r.status) && !"CANCELLED".equalsIgnoreCase(r.status))
                .toList();

        if (pendingList.isEmpty()) {
            Label empty = new Label("No new port establishment requests pending.");
            empty.setStyle("-fx-text-fill:#64748b; -fx-font-size:13; -fx-padding:10;");
            container.getChildren().add(empty);
            return;
        }

        for (AdminDataStore.PortRequest req : pendingList) {
            HBox row = new HBox(12);
            row.setPadding(new Insets(12, 14, 12, 14));
            row.setStyle(
                    "-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");
            row.setAlignment(Pos.CENTER_LEFT);

            VBox info = new VBox(4,
                    boldLabel(req.portName + " (" + req.portCode + ")"),
                    mutedLabel("📍 " + req.location + " • " + req.address),
                    mutedLabel("👤 Requested By: " + req.requesterName + " (" + req.requesterEmail + ") • Date: "
                            + req.requestDate));

            VBox paInfo = new VBox(4,
                    boldLabel("Proposed Port Authority:"),
                    mutedLabel(req.proposedPaName + " <" + req.proposedPaEmail + ">"));

            Label statusBadge = new Label(req.status);
            statusBadge.setStyle(
                    "-fx-background-color:#fff7ed; -fx-text-fill:#f97316; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:12;");

            Region space = new Region();
            HBox.setHgrow(space, Priority.ALWAYS);

            HBox actions = new HBox(8);
            actions.setAlignment(Pos.CENTER_RIGHT);

            Button approveBtn = new Button("✓ Approve & Create Port");
            approveBtn.setStyle(
                    "-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:6; -fx-cursor:hand;");

            Button rejectBtn = new Button("✕ Reject Request");
            rejectBtn.setStyle(
                    "-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:6; -fx-cursor:hand;");

            approveBtn.setOnAction(e -> {
                if ("APPROVED".equalsIgnoreCase(req.status)) {
                    return;
                }
                approveBtn.setText("⏳ Approving...");
                approveBtn.setDisable(true);
                rejectBtn.setDisable(true);

                // Update database status first (Requirement 1 & 2)
                portRequestDao.updateRequestStatus(req.requestId, "APPROVED", null).whenComplete((dbSuccess, dbErr) -> {
                    javafx.application.Platform.runLater(() -> {
                        if (dbErr != null || Boolean.FALSE.equals(dbSuccess)) {
                            // On failure: keep request visible and show alert (Requirement 14)
                            req.status = "PENDING";
                            approveBtn.setText("✓ Approve & Create Port");
                            approveBtn.setDisable(false);
                            rejectBtn.setDisable(false);
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                    javafx.scene.control.Alert.AlertType.ERROR, "Database Update Failed: "
                                            + (dbErr != null ? dbErr.getMessage() : "Unknown error"));
                            alert.showAndWait();
                            return;
                        }

                        req.status = "APPROVED";

                        Port existingPort = null;
                        for (Port p : ports) {
                            if (p.name.equalsIgnoreCase(req.portName)
                                    || (p.code != null && p.code.equalsIgnoreCase(req.portCode))) {
                                existingPort = p;
                                break;
                            }
                        }

                        if (existingPort != null) {
                            existingPort.approvalStatus = "APPROVED";
                            existingPort.status = "Operational";
                            if (req.lat != 0.0 || req.lng != 0.0) {
                                existingPort.lat = req.lat;
                                existingPort.lng = req.lng;
                            }
                        } else {
                            ports.add(new Port(req.portName, req.location, req.portCode, req.proposedPaName, 12, 0,
                                    "Operational", "Mixed Cargo, Containers", "100%", "Clear", now(), req.lat, req.lng,
                                    "APPROVED"));
                        }

                        // ONLY AFTER database operation succeeds: refresh table, stats, map, dashboard,
                        // and pending list
                        refreshTable();
                        refreshStats();
                        GeoTileMapCanvas.refreshAllMapInstances();
                        DashboardPage.refreshAllDashboards();
                        renderPortRequests(container);

                        java.util.Map<String, Object> extraFields = new java.util.HashMap<>();
                        extraFields.put("requestId", req.requestId);
                        extraFields.put("portName", req.portName);
                        extraFields.put("portCode", req.portCode);
                        extraFields.put("assignedPort", req.portName);
                        extraFields.put("company", req.portName);
                        extraFields.put("organization", req.portName);
                        extraFields.put("requesterEmail", req.requesterEmail);
                        extraFields.put("proposedPaEmail", req.proposedPaEmail);
                        extraFields.put("roleCode", "PORT_AUTHORITY");
                        extraFields.put("role", "Port Authority");

                        userDao.provisionAccountExtended("Admin", req.proposedPaName, req.proposedPaEmail, req.portName,
                                req.requesterPhone, "Port Authority", "Active", extraFields)
                                .whenComplete((creds, err) -> javafx.application.Platform.runLater(() -> {
                                    String accId = (creds != null && creds.get("id") != null && !creds.get("id").isBlank()) ? creds.get("id")
                                            : req.proposedPaEmail;
                                    String tempPass = (creds != null && creds.get("tempPassword") != null)
                                            ? creds.get("tempPassword")
                                            : "Pass#9821";

                                    String databaseRequesterEmail = (req.requesterEmail != null && !req.requesterEmail.isBlank()) ? req.requesterEmail.trim() : req.proposedPaEmail;
                                    System.out.println("[APPROVAL] requesterEmail from database: " + databaseRequesterEmail);
                                    System.out.println("[APPROVAL] final email recipient: " + databaseRequesterEmail);

                                    emailService.sendApprovalNotification(
                                            databaseRequesterEmail,
                                            req.portName,
                                            req.portCode,
                                            req.requestId,
                                            req.proposedPaName,
                                            accId,
                                            tempPass);

                                    formSubtitle.setText("✓ Port Approved & Created! Credentials created for: "
                                            + req.proposedPaEmail + " (ID: " + accId + ")");
                                    String paUid = (creds != null && creds.get("uid") != null) ? creds.get("uid") : ("usr_" + Math.abs(req.proposedPaEmail.toLowerCase().hashCode()));
                                    store.users.add(0, new AdminDataStore.UserAccount(paUid, req.proposedPaName,
                                            req.proposedPaEmail, "Port Authority", "Active", "Today", "Just now"));

                                    showPortAuthorityAccountCreatedPopup(req.portName, req.proposedPaName,
                                            req.proposedPaEmail, accId, tempPass);
                                }));
                    });
                });
            });

            String aadhaarDocUrl = getDocumentUrl(req, true);
            String supportingDocUrl = getDocumentUrl(req, false);

            VBox docInfo = new VBox(6);
            Button viewAadhaarBtn = new Button("📄 View Aadhaar Card");
            viewAadhaarBtn.setStyle("-fx-background-color:#0b63ff; -fx-text-fill:white; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:5 10; -fx-background-radius:6; -fx-cursor:hand;");
            viewAadhaarBtn.setOnAction(e -> showDocumentViewerModal("Aadhaar Card Verification", req.aadhaarFileName, aadhaarDocUrl));

            Button dlAadhaarBtn = new Button("⬇️ Download");
            dlAadhaarBtn.setStyle("-fx-background-color:#334155; -fx-text-fill:white; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:5 8; -fx-background-radius:6; -fx-cursor:hand;");
            dlAadhaarBtn.setOnAction(e -> downloadDocument(req.aadhaarFileName, aadhaarDocUrl, container.getScene().getWindow()));

            HBox aadhaarRow = new HBox(6, viewAadhaarBtn, dlAadhaarBtn);

            Button viewSupportingBtn = new Button("📋 View Port Establishment Proposal");
            viewSupportingBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:5 10; -fx-background-radius:6; -fx-cursor:hand;");
            viewSupportingBtn.setOnAction(e -> showDocumentViewerModal("Port Establishment Proposal", req.supportingFileName, supportingDocUrl));

            Button dlSupportingBtn = new Button("⬇️ Download");
            dlSupportingBtn.setStyle("-fx-background-color:#334155; -fx-text-fill:white; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:5 8; -fx-background-radius:6; -fx-cursor:hand;");
            dlSupportingBtn.setOnAction(e -> downloadDocument(req.supportingFileName, supportingDocUrl, container.getScene().getWindow()));

            HBox proposalRow = new HBox(6, viewSupportingBtn, dlSupportingBtn);

            docInfo.getChildren().addAll(aadhaarRow, proposalRow);

            rejectBtn.setOnAction(e -> showRejectionDialog(req, container));

            actions.getChildren().addAll(docInfo, approveBtn, rejectBtn);
            row.getChildren().addAll(info, paInfo, statusBadge, space, actions);
            container.getChildren().add(row);
        }
    }

    public static String getDocumentUrl(AdminDataStore.PortRequest req, boolean isAadhaar) {
        if (req == null) return "";
        if (isAadhaar) {
            if (req.aadhaarUrl != null && !req.aadhaarUrl.isBlank()) return req.aadhaarUrl.trim();
            if (req.aadhaarFileName != null && req.aadhaarFileName.startsWith("http")) return req.aadhaarFileName.trim();
        } else {
            if (req.supportingUrl != null && !req.supportingUrl.isBlank()) return req.supportingUrl.trim();
            if (req.supportingFileName != null && req.supportingFileName.startsWith("http")) return req.supportingFileName.trim();
        }
        return "";
    }

    private static byte[] fetchDocumentBytes(String urlString) {
        if (urlString == null || urlString.isBlank()) return null;
        String trimmed = urlString.trim();

        // 1. Local file URI or file path
        if (trimmed.startsWith("file:") || new java.io.File(trimmed).exists()) {
            try {
                java.io.File localFile = trimmed.startsWith("file:") ? new java.io.File(new java.net.URI(trimmed)) : new java.io.File(trimmed);
                if (localFile.exists()) {
                    return java.nio.file.Files.readAllBytes(localFile.toPath());
                }
            } catch (Exception ex) {
                System.err.println("[LOCAL FILE FETCH NOTICE] " + ex.getMessage());
            }
        }

        // 2. Remote HTTP/HTTPS Cloudinary URL
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .followRedirects(java.net.http.HttpClient.Redirect.ALWAYS)
                    .connectTimeout(java.time.Duration.ofSeconds(15))
                    .build();

            // Correct legacy cloud name "docktech" to active "snmxmesm"
            String fixedUrl = trimmed.replace("res.cloudinary.com/docktech/", "res.cloudinary.com/snmxmesm/");

            // Generate candidate URLs to try fetching
            List<String> candidates = new ArrayList<>();
            candidates.add(fixedUrl);

            // Cloudinary resource_type variants (/image/upload/ vs /raw/upload/)
            if (fixedUrl.contains("/image/upload/")) {
                candidates.add(fixedUrl.replace("/image/upload/", "/raw/upload/"));
            } else if (fixedUrl.contains("/raw/upload/")) {
                candidates.add(fixedUrl.replace("/raw/upload/", "/image/upload/"));
            }

            // Version-stripped variants (/v1724800000/ or /v123456/ -> /)
            String noVersionUrl = fixedUrl.replaceAll("/v[0-9]+/", "/");
            if (!candidates.contains(noVersionUrl)) {
                candidates.add(noVersionUrl);
                if (noVersionUrl.contains("/image/upload/")) {
                    candidates.add(noVersionUrl.replace("/image/upload/", "/raw/upload/"));
                } else if (noVersionUrl.contains("/raw/upload/")) {
                    candidates.add(noVersionUrl.replace("/raw/upload/", "/image/upload/"));
                }
            }

            // Add .pdf extension variants if missing
            List<String> extCandidates = new ArrayList<>();
            for (String c : candidates) {
                if (!c.toLowerCase().endsWith(".pdf") && !c.toLowerCase().endsWith(".png") && !c.toLowerCase().endsWith(".jpg") && !c.toLowerCase().endsWith(".jpeg")) {
                    extCandidates.add(c + ".pdf");
                }
            }
            candidates.addAll(extCandidates);

            for (String candidate : candidates) {
                try {
                    java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create(candidate))
                            .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                            .header("Accept", "application/pdf,application/octet-stream,image/*,*/*")
                            .timeout(java.time.Duration.ofSeconds(15))
                            .GET()
                            .build();

                    java.net.http.HttpResponse<byte[]> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofByteArray());
                    if (resp.statusCode() == 200 && resp.body() != null && resp.body().length > 0) {
                        System.out.println("[DOCUMENT FETCH SUCCESS] Retrieved " + resp.body().length + " bytes from: " + candidate);
                        return resp.body();
                    }
                } catch (Exception ex) {
                    System.err.println("[DOCUMENT FETCH CANDIDATE FAILED] " + candidate + " -> " + ex.getMessage());
                }
            }
        }
        return null;
    }

    private void showDocumentViewerModal(String docTitle, String fileName, String url) {
        if (url == null || url.isBlank()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "No document URL available for " + docTitle + ".");
            alert.showAndWait();
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(docTitle + " - " + (fileName != null ? fileName : "document"));

        VBox rootNode = new VBox(14);
        rootNode.setPadding(new Insets(20));
        rootNode.setStyle("-fx-background-color:#0f172a;");

        String headerIcon = docTitle.toLowerCase().contains("aadhaar") ? "📄 " : "📋 ";
        Label titleLabel = new Label(headerIcon + docTitle);
        titleLabel.setStyle("-fx-text-fill:#38bdf8; -fx-font-size:18px; -fx-font-weight:bold;");

        Label fileLabel = new Label("File Name: " + (fileName != null ? fileName : "document"));
        fileLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px; -fx-wrap-text:true;");

        Region topSpace = new Region();
        HBox.setHgrow(topSpace, Priority.ALWAYS);

        Button closeHeaderBtn = new Button("✕");
        closeHeaderBtn.setStyle("-fx-background-color:transparent; -fx-text-fill:#94a3b8; -fx-font-size:16px; -fx-font-weight:bold; -fx-cursor:hand;");
        closeHeaderBtn.setOnAction(e -> dialog.close());

        HBox headerRow = new HBox(10, new VBox(4, titleLabel, fileLabel), topSpace, closeHeaderBtn);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        StackPane viewerContainer = new StackPane();
        viewerContainer.setPrefSize(700, 460);
        viewerContainer.setStyle("-fx-background-color:#1e293b; -fx-border-color:#334155; -fx-border-radius:8; -fx-background-radius:8;");

        VBox loadingBox = new VBox(12);
        loadingBox.setAlignment(Pos.CENTER);
        javafx.scene.control.ProgressIndicator progress = new javafx.scene.control.ProgressIndicator();
        progress.setMaxSize(40, 40);
        Label loadingLbl = new Label("Loading document preview...");
        loadingLbl.setStyle("-fx-text-fill:#38bdf8; -fx-font-size:14px; -fx-font-weight:bold;");
        loadingBox.getChildren().addAll(progress, loadingLbl);
        viewerContainer.getChildren().add(loadingBox);

        new Thread(() -> {
            byte[] bytes = fetchDocumentBytes(url);

            if (bytes == null || bytes.length == 0) {
                javafx.application.Platform.runLater(() -> {
                    viewerContainer.getChildren().clear();
                    VBox errBox = new VBox(14);
                    errBox.setAlignment(Pos.CENTER);
                    Label errIcon = new Label("⚠️");
                    errIcon.setStyle("-fx-font-size:44px;");
                    Label errLbl = new Label("Unable to load document.");
                    errLbl.setStyle("-fx-text-fill:#ef4444; -fx-font-size:14px; -fx-font-weight:bold;");

                    Button retryBtn = new Button("🔄 Retry");
                    retryBtn.setStyle("-fx-background-color:#0b63ff; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-padding:8 16; -fx-background-radius:6; -fx-cursor:hand;");
                    retryBtn.setOnAction(ev -> {
                        dialog.close();
                        showDocumentViewerModal(docTitle, fileName, url);
                    });

                    errBox.getChildren().addAll(errIcon, errLbl, retryBtn);
                    viewerContainer.getChildren().add(errBox);
                });
                return;
            }

            // 1. Try rendering as PDF document using Apache PDFBox
            try {
                org.apache.pdfbox.pdmodel.PDDocument pdfDoc = org.apache.pdfbox.pdmodel.PDDocument.load(bytes);
                org.apache.pdfbox.rendering.PDFRenderer pdfRenderer = new org.apache.pdfbox.rendering.PDFRenderer(pdfDoc);

                VBox pdfPagesBox = new VBox(14);
                pdfPagesBox.setAlignment(Pos.CENTER);
                pdfPagesBox.setPadding(new Insets(14));
                pdfPagesBox.setStyle("-fx-background-color:#0f172a;");

                int pageCount = pdfDoc.getNumberOfPages();
                for (int i = 0; i < pageCount; i++) {
                    java.awt.image.BufferedImage bImg = pdfRenderer.renderImageWithDPI(i, 130);
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    javax.imageio.ImageIO.write(bImg, "png", baos);
                    javafx.scene.image.Image fxImg = new javafx.scene.image.Image(new java.io.ByteArrayInputStream(baos.toByteArray()));
                    javafx.scene.image.ImageView pageView = new javafx.scene.image.ImageView(fxImg);
                    pageView.setFitWidth(650);
                    pageView.setPreserveRatio(true);

                    VBox pageCard = new VBox(pageView);
                    pageCard.setStyle("-fx-effect:dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 4);");
                    pdfPagesBox.getChildren().add(pageCard);
                }
                pdfDoc.close();

                javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(pdfPagesBox);
                scrollPane.setFitToWidth(true);
                scrollPane.setStyle("-fx-background:#0f172a; -fx-background-color:#0f172a; -fx-viewport-border:transparent;");
                com.projectui.view.login.LoginPage.applyInvisibleScrollbar(scrollPane);

                javafx.application.Platform.runLater(() -> {
                    viewerContainer.getChildren().clear();
                    viewerContainer.getChildren().add(scrollPane);
                });
                return;
            } catch (Exception pdfEx) {
                // Not a PDF, try rendering as direct image
                try {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(new java.io.ByteArrayInputStream(bytes));
                    if (!img.isError() && img.getWidth() > 0) {
                        javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
                        imgView.setFitWidth(660);
                        imgView.setPreserveRatio(true);

                        VBox imgBox = new VBox(imgView);
                        imgBox.setAlignment(Pos.CENTER);
                        imgBox.setPadding(new Insets(14));
                        imgBox.setStyle("-fx-background-color:#0f172a;");

                        javafx.scene.control.ScrollPane imgScrollPane = new javafx.scene.control.ScrollPane(imgBox);
                        imgScrollPane.setFitToWidth(true);
                        imgScrollPane.setStyle("-fx-background:#0f172a; -fx-background-color:#0f172a; -fx-viewport-border:transparent;");
                        com.projectui.view.login.LoginPage.applyInvisibleScrollbar(imgScrollPane);

                        javafx.application.Platform.runLater(() -> {
                            viewerContainer.getChildren().clear();
                            viewerContainer.getChildren().add(imgScrollPane);
                        });
                        return;
                    }
                } catch (Exception imgEx) {
                }
            }

            // Fallback error view if both PDF rendering and image rendering fail
            javafx.application.Platform.runLater(() -> {
                viewerContainer.getChildren().clear();
                VBox errBox = new VBox(14);
                errBox.setAlignment(Pos.CENTER);
                Label errIcon = new Label("⚠️");
                errIcon.setStyle("-fx-font-size:44px;");
                Label errLbl = new Label("Unable to load document.");
                errLbl.setStyle("-fx-text-fill:#ef4444; -fx-font-size:14px; -fx-font-weight:bold;");

                Button retryBtn = new Button("🔄 Retry");
                retryBtn.setStyle("-fx-background-color:#0b63ff; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-padding:8 16; -fx-background-radius:6; -fx-cursor:hand;");
                retryBtn.setOnAction(ev -> {
                    dialog.close();
                    showDocumentViewerModal(docTitle, fileName, url);
                });

                errBox.getChildren().addAll(errIcon, errLbl, retryBtn);
                viewerContainer.getChildren().add(errBox);
            });
        }).start();

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color:#334155; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-padding:8 16; -fx-background-radius:6; -fx-cursor:hand;");
        closeBtn.setOnAction(e -> dialog.close());

        HBox btnBox = new HBox(closeBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        rootNode.getChildren().addAll(headerRow, viewerContainer, btnBox);
        Scene scene = new Scene(rootNode, 740, 600);
        dialog.setScene(scene);
        dialog.show();
    }

    private void downloadDocument(String fileName, String url, javafx.stage.Window ownerWindow) {
        if (url == null || url.isBlank()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "No document URL available for download.");
            alert.showAndWait();
            return;
        }

        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Save Document As");
        chooser.setInitialFileName(fileName != null && !fileName.isBlank() ? fileName : "Port_Document.pdf");
        java.io.File dest = chooser.showSaveDialog(ownerWindow);
        if (dest != null) {
            new Thread(() -> {
                byte[] bytes = fetchDocumentBytes(url);
                if (bytes != null && bytes.length > 0) {
                    try {
                        java.nio.file.Files.write(dest.toPath(), bytes);
                        javafx.application.Platform.runLater(() -> {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Document downloaded successfully:\n" + dest.getAbsolutePath());
                            alert.showAndWait();
                        });
                    } catch (Exception ex) {
                        javafx.application.Platform.runLater(() -> {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Failed to write downloaded document: " + ex.getMessage());
                            alert.showAndWait();
                        });
                    }
                } else {
                    javafx.application.Platform.runLater(() -> {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Unable to download document. Please verify your connection.");
                        alert.showAndWait();
                    });
                }
            }).start();
        }
    }

    private void showRejectionDialog(AdminDataStore.PortRequest req, VBox container) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Reject Port Establishment Request");

        VBox dialogRoot = new VBox(14);
        dialogRoot.setPadding(new Insets(20));
        dialogRoot.setStyle("-fx-background-color:white; -fx-background-radius:12;");

        Label title = new Label("Rejection Reason for " + req.portName + " (" + req.requestId + ")");
        title.setStyle("-fx-font-size:16; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        Label desc = new Label(
                "Please enter the official reason for rejection. This reason will be stored in Firestore and emailed directly to the requester ("
                        + req.requesterEmail + ").");
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size:13; -fx-text-fill:#64748b;");

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText(
                "Enter rejection reason (e.g. Incomplete environmental clearance or invalid berth capacity proposal)...");
        reasonArea.setPrefRowCount(3);
        reasonArea
                .setStyle("-fx-font-size:13; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6;");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
                "-fx-background-color:#f1f5f9; -fx-text-fill:#475569; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:8 16; -fx-background-radius:6; -fx-cursor:hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button confirmBtn = new Button("Confirm Rejection & Send Email");
        confirmBtn.setStyle(
                "-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:8 16; -fx-background-radius:6; -fx-cursor:hand;");
        confirmBtn.setOnAction(e -> {
            String reasonText = reasonArea.getText().trim();
            if (reasonText.isEmpty()) {
                reasonText = "Submitted port proposal does not meet operational or safety requirements.";
            }

            req.status = "REJECTED";
            req.rejectionReason = reasonText;

            portRequestDao.updateRequestStatus(req.requestId, "REJECTED", reasonText);
            emailService.sendRejectionNotification(req.requesterEmail, req.portName, req.requestId, reasonText);

            formSubtitle.setText(
                    "✕ Request " + req.requestId + " REJECTED. Notification email sent to: " + req.requesterEmail);
            dialog.close();
            DashboardPage.refreshAllDashboards();
            renderPortRequests(container);
        });

        HBox btnRow = new HBox(10, cancelBtn, confirmBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        dialogRoot.getChildren().addAll(title, desc, reasonArea, btnRow);
        Scene scene = new Scene(dialogRoot, 460, 260);
        com.projectui.view.login.LoginPage.applyInvisibleScrollbar(scene);
        com.projectui.view.login.LoginPage.applyInvisibleScrollbar(dialog);
        dialog.setScene(scene);
        dialog.show();
    }

    private void showPortAuthorityAccountCreatedPopup(String portNameStr, String paNameStr, String emailStr,
            String accountIdStr, String tempPasswordStr) {
        javafx.scene.control.Dialog<javafx.scene.control.ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Port Authority Account Created");
        dialog.setHeaderText("Port created and Port Authority account created successfully.");

        javafx.scene.control.ButtonType copyBtn = new javafx.scene.control.ButtonType("Copy Credentials",
                javafx.scene.control.ButtonBar.ButtonData.LEFT);
        javafx.scene.control.ButtonType okBtn = new javafx.scene.control.ButtonType("OK",
                javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(copyBtn, okBtn);

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        content.setStyle("-fx-background-color:#FFFFFF;");

        StringBuilder sb = new StringBuilder();
        sb.append("Port Name: ").append(portNameStr).append("\n");
        sb.append("Port Authority Name: ").append(paNameStr).append("\n");
        sb.append("Email: ").append(emailStr).append("\n");
        sb.append("Account ID: ").append(accountIdStr).append("\n");
        sb.append("Temporary Password: ").append(tempPasswordStr);

        Label infoLabel = new Label(sb.toString());
        infoLabel.setStyle(
                "-fx-font-family: monospace; -fx-font-size: 13px; -fx-text-fill: #0F172A; -fx-background-color: #F8FAFC; -fx-padding: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label confirmEmailText = new Label("Account credentials have been sent to the registered email address.");
        confirmEmailText.setStyle("-fx-font-size: 12px; -fx-text-fill: #16A34A; -fx-font-weight: bold;");

        content.getChildren().addAll(infoLabel, confirmEmailText);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == copyBtn) {
                javafx.scene.input.ClipboardContent clip = new javafx.scene.input.ClipboardContent();
                clip.putString(sb.toString());
                javafx.scene.input.Clipboard.getSystemClipboard().setContent(clip);

                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION, "Credentials copied to clipboard!");
                alert.showAndWait();
            }
            return btn;
        });

        dialog.showAndWait();
    }

    // ==========================================
    // TABLE CELL HELPERS
    // ==========================================
    private HBox portNameCell(Port port) {
        Label icon = new Label("⚓");
        icon.setStyle("-fx-text-fill:" + statusColor(port.status) + "; -fx-font-size:15; -fx-font-weight:bold;");
        Label name = bodyCell(port.name);
        HBox cell = new HBox(8, icon, name);
        cell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setMinHeight(52);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 12, 0, 12));
        cell.setStyle(
                "-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0; -fx-cursor:hand;");
        return cell;
    }

    private HBox berthUsageCell(Port port) {
        Label value = new Label(port.occupiedBerths() + " / " + port.berths);
        value.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");
        double pct = port.berths == 0 ? 0 : (double) port.occupiedBerths() / port.berths;
        Region bar = new Region();
        bar.setPrefWidth(60);
        bar.setMinHeight(6);
        bar.setMaxHeight(6);
        bar.setStyle("-fx-background-color:#e5e7eb; -fx-background-radius:999;");
        Region fill = new Region();
        fill.setPrefWidth(Math.max(6, 60 * pct));
        fill.setMinHeight(6);
        fill.setMaxHeight(6);
        fill.setStyle("-fx-background-color:" + usageColor(pct) + "; -fx-background-radius:999;");
        StackPane progress = new StackPane(bar, fill);
        progress.setAlignment(Pos.CENTER_LEFT);
        HBox cell = new HBox(8, value, progress);
        cell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setMinHeight(52);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 12, 0, 12));
        cell.setStyle("-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return cell;
    }

    private HBox createEditActionCell(int index) {
        Button editBtn = new Button("✎ Edit");
        editBtn.setStyle(
                "-fx-background-color:#eff6ff;" +
                        "-fx-text-fill:" + BLUE + ";" +
                        "-fx-font-size:13;" +
                        "-fx-font-weight:bold;" +
                        "-fx-padding:4 10;" +
                        "-fx-background-radius:6;" +
                        "-fx-cursor:hand;");
        editBtn.setOnAction(e -> startEdit(index));

        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.setStyle(
                "-fx-background-color:#fef2f2;" +
                        "-fx-text-fill:#ef4444;" +
                        "-fx-font-size:13;" +
                        "-fx-font-weight:bold;" +
                        "-fx-padding:4 10;" +
                        "-fx-background-radius:6;" +
                        "-fx-cursor:hand;");
        deleteBtn.setOnAction(e -> deletePortAtIndex(index));

        HBox cell = new HBox(8, editBtn, deleteBtn);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setMinHeight(52);
        cell.setPadding(new Insets(0, 12, 0, 12));
        cell.setStyle("-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return cell;
    }

    private void deletePortAtIndex(int index) {
        if (index < 0 || index >= ports.size()) return;
        Port removed = ports.remove(index);
        store.deletePort(removed);
        formSubtitle.setText("✓ Port '" + removed.name + "' was permanently deleted.");
        if (editingIndex == index) {
            startAdd();
        } else if (editingIndex > index) {
            editingIndex--;
        }
        GeoTileMapCanvas.refreshAllMapInstances();
        refreshTable();
        refreshStats();
    }

    private Label headerCell(String text) {
        Label cell = new Label(text);
        cell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setMinHeight(46);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 12, 0, 12));
        cell.setStyle(
                "-fx-background-color:#f8fafc; -fx-text-fill:#0f172a; -fx-font-size:14; -fx-font-weight:bold; -fx-border-color:#e2e8f0; -fx-border-width:0 0 1 0;");
        return cell;
    }

    private Label bodyCell(String text) {
        Label cell = new Label(text);
        cell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setMinHeight(52);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 12, 0, 12));
        cell.setStyle(
                "-fx-background-color:white; -fx-text-fill:#111827; -fx-font-size:14; -fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return cell;
    }

    private HBox statusBadge(String text) {
        Label badge = new Label(text);
        badge.setPadding(new Insets(4, 10, 4, 10));
        badge.setStyle("-fx-background-color:" + statusBackground(text) + "; -fx-background-radius:12; -fx-text-fill:"
                + statusColor(text) + "; -fx-font-size:12; -fx-font-weight:bold;");

        HBox cell = new HBox(badge);
        cell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setMinHeight(52);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 12, 0, 12));
        cell.setStyle("-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return cell;
    }

    private VBox summaryTile(String iconText, String title, int value, String color, String bg) {
        Label icon = new Label(iconText);
        icon.setAlignment(Pos.CENTER);
        icon.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + color
                + "; -fx-font-size:18; -fx-font-weight:bold; -fx-background-radius:8; -fx-min-width:42; -fx-min-height:42;");
        Label number = new Label(String.valueOf(value));
        number.setStyle("-fx-text-fill:" + color + "; -fx-font-size:26; -fx-font-weight:800;");
        Label label = new Label(title);
        label.setStyle("-fx-text-fill:" + color + "; -fx-font-size:12; -fx-font-weight:bold;");
        VBox tile = new VBox(6, icon, number, label);
        tile.setAlignment(Pos.CENTER);
        tile.setPadding(new Insets(12, 10, 12, 10));
        tile.setMaxWidth(Double.MAX_VALUE);
        tile.setStyle("-fx-background-color:" + bg + "; -fx-border-color:" + color
                + "22; -fx-border-radius:8; -fx-background-radius:8;");
        return tile;
    }

    private HBox activityRow(Port port) {
        String color = statusColor(port.status);
        Label icon = new Label("⚓");
        icon.setAlignment(Pos.CENTER);
        icon.setStyle("-fx-background-color:" + statusBackground(port.status) + "; -fx-text-fill:" + color
                + "; -fx-font-size:14; -fx-font-weight:bold; -fx-background-radius:999; -fx-min-width:32; -fx-min-height:32;");
        Label title = new Label(port.name);
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");
        Label detail = new Label(alertSummary(port));
        detail.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        Label time = new Label(lastUpdatedShort(port.lastUpdated));
        time.setStyle("-fx-text-fill:#334155; -fx-font-size:12;");
        HBox row = new HBox(10, icon, new VBox(2, title, detail), space, time);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private StackPane capacityDonut(int average) {
        Arc used = new Arc(60, 60, 45, 45, 90, -(360 * average / 100.0));
        used.setType(ArcType.OPEN);
        used.setStyle("-fx-fill:transparent; -fx-stroke:#16a34a; -fx-stroke-width:18;");
        Arc warn = new Arc(60, 60, 45, 45, 90 - (360 * average / 100.0), -72);
        warn.setType(ArcType.OPEN);
        warn.setStyle("-fx-fill:transparent; -fx-stroke:" + BLUE + "; -fx-stroke-width:18;");
        Arc risk = new Arc(60, 60, 45, 45, 18, -62);
        risk.setType(ArcType.OPEN);
        risk.setStyle("-fx-fill:transparent; -fx-stroke:#ef4444; -fx-stroke-width:18;");
        Circle hole = new Circle(32);
        hole.setStyle("-fx-fill:white;");
        Label center = new Label(average + "%\nAvg Capacity");
        center.setAlignment(Pos.CENTER);
        center.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");
        StackPane chart = new StackPane(used, warn, risk, hole, center);
        chart.setPrefSize(120, 120);
        chart.setMinSize(120, 120);
        return chart;
    }

    private HBox capacityLegend(String title, int count, int total, String color) {
        Circle dot = new Circle(4);
        dot.setStyle("-fx-fill:" + color + ";");
        Label label = new Label(title);
        label.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13;");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        int pct = total == 0 ? 0 : (int) Math.round(count * 100.0 / total);
        Label value = new Label(pct + "%");
        value.setStyle("-fx-text-fill:#334155; -fx-font-size:13; -fx-font-weight:bold;");
        HBox row = new HBox(8, dot, label, space, value);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label boldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");
        return label;
    }

    private Label mutedLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");
        return label;
    }

    private VBox statBox(String title, String value, String detail, String iconText, String color, String bg,
            LabelSetter setter) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14; -fx-font-weight:bold;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill:" + color + "; -fx-font-size:28; -fx-font-weight:800;");
        setter.set(valueLabel);

        Label detailLabel = new Label(detail);
        detailLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        Label icon = new Label(iconText);
        icon.setAlignment(Pos.CENTER);
        icon.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + color
                + "; -fx-font-size:22; -fx-font-weight:bold; -fx-background-radius:999; -fx-min-width:52; -fx-min-height:52;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox row = new HBox(10, new VBox(6, titleLabel, valueLabel, detailLabel), space, icon);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(row);
        card.setMinHeight(96);
        card.setPadding(new Insets(15));
        card.setStyle(cardStyle());
        return card;
    }

    private VBox labeledControl(String labelText, Parent field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill:#334155; -fx-font-size:14; -fx-font-weight:bold;");
        VBox controlBox = new VBox(6, label, field);
        HBox.setHgrow(controlBox, Priority.ALWAYS);
        controlBox.setMaxWidth(Double.MAX_VALUE);
        return controlBox;
    }

    private String cardStyle() {
        return "-fx-background-color:white; -fx-background-radius:10; -fx-border-color:#dfe7f1; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.07), 13, 0, 0, 3);";
    }

    private void styleCustomField(javafx.scene.control.Control f) {
        f.setStyle(
                "-fx-background-color:#f8fafc;" +
                        "-fx-border-color:#e2e8f0;" +
                        "-fx-border-radius:6;" +
                        "-fx-background-radius:6;" +
                        "-fx-padding:8 12 8 12;" +
                        "-fx-font-size:14;" +
                        "-fx-text-fill:#0f172a;");
    }

    private String usageColor(double pct) {
        if (pct >= 0.85)
            return "#ef4444";
        if (pct >= 0.7)
            return "#f97316";
        if (pct >= 0.5)
            return "#0b63ff";
        return "#16a34a";
    }

    private int countOperational() {
        int count = 0;
        for (Port port : ports) {
            if ("Operational".equals(port.status))
                count++;
        }
        return count;
    }

    private int countMaintenance() {
        int count = 0;
        for (Port port : ports) {
            if ("Maintenance".equals(port.status))
                count++;
        }
        return count;
    }

    private int countHighCongestion() {
        int count = 0;
        for (Port port : ports) {
            if ("High Congestion".equals(port.status) || "Busy".equals(port.status)
                    || port.vessels >= port.berths * 0.85)
                count++;
        }
        return count;
    }

    private int countInactive() {
        int count = 0;
        for (Port port : ports) {
            if ("Closed".equals(port.status) || "Restricted".equals(port.status) || "Inactive".equals(port.status)
                    || "Deactivated".equals(port.status))
                count++;
        }
        return count;
    }

    private int totalBerths() {
        int total = 0;
        for (Port port : ports)
            total += port.berths;
        return total;
    }

    private int totalOccupiedBerths() {
        int total = 0;
        for (Port port : ports)
            total += port.occupiedBerths();
        return total;
    }

    private int averageCapacity() {
        if (ports.isEmpty())
            return 0;
        int total = 0;
        for (Port port : ports)
            total += capacityPercent(port.capacity);
        return Math.round(total / (float) ports.size());
    }

    private int capacityPercent(String value) {
        if (value == null)
            return 0;
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isBlank())
            return 0;
        try {
            return Math.max(0, Math.min(100, Integer.parseInt(digits)));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String lastUpdatedShort(String value) {
        if (value == null || value.isBlank())
            return "Now";
        int comma = value.indexOf(',');
        return comma >= 0 && comma + 2 < value.length() ? value.substring(comma + 2) : value;
    }

    private String alertSummary(Port port) {
        if (!"Clear".equalsIgnoreCase(port.maintenance))
            return port.maintenance;
        if (port.vessels >= port.berths * 0.85)
            return "High congestion";
        if ("Busy".equals(port.status))
            return "Traffic watch";
        return "No active alerts";
    }

    private boolean matches(Port port, String query) {
        String selectedStatus = statusFilter == null || statusFilter.getValue() == null ? "All Status"
                : statusFilter.getValue();
        boolean statusMatches = "All Status".equals(selectedStatus) || selectedStatus.equals(port.status);
        boolean queryMatches = query.isEmpty()
                || port.name.toLowerCase().contains(query)
                || port.location.toLowerCase().contains(query)
                || port.status.toLowerCase().contains(query)
                || port.cargo.toLowerCase().contains(query);

        return queryMatches && statusMatches;
    }

    private String statusColor(String value) {
        if ("Operational".equals(value))
            return "#16a34a";
        if ("High Congestion".equals(value) || "Busy".equals(value))
            return "#ef4444";
        if ("Available".equals(value))
            return "#16a34a";
        if ("Maintenance".equals(value))
            return BLUE;
        if ("Occupied".equals(value))
            return "#d97706";
        if ("Restricted".equals(value) || "Closed".equals(value))
            return "#64748b";
        return "#d97706";
    }

    private String statusBackground(String value) {
        if ("Operational".equals(value))
            return "#ecfdf5";
        if ("High Congestion".equals(value) || "Busy".equals(value))
            return "#fee2e2";
        if ("Maintenance".equals(value))
            return "#eff6ff";
        if ("Restricted".equals(value) || "Closed".equals(value))
            return "#f1f5f9";
        return "#fff7ed";
    }

    private interface LabelSetter {
        void set(Label label);
    }

    private String now() {
        return store.now();
    }
}
