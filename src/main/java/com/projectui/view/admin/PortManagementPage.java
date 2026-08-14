package com.projectui.view.admin;

import java.util.List;

import com.projectui.view.admin.AdminDataStore.Port;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
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
import javafx.scene.shape.Rectangle;

public class PortManagementPage extends AdminTheme {

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
    private TextArea maintenanceInfo;
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
    }

    public Parent getRoot() {
        // Page Header Buttons
        Button addQuickBtn = primaryButton("+  Add Port");
        addQuickBtn.setOnAction(event -> startAdd());
        addQuickBtn.setStyle("-fx-background-color:" + BLUE + "; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:10 22; -fx-background-radius:8; -fx-cursor:hand;");

        Button editQuickBtn = new Button("✎  Edit Port");
        editQuickBtn.setStyle("-fx-background-color:#0aa05f; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:10 22; -fx-background-radius:8; -fx-cursor:hand;");
        editQuickBtn.setOnAction(event -> {
            if (editingIndex >= 0 && editingIndex < ports.size()) {
                startEdit(editingIndex);
            } else if (!ports.isEmpty()) {
                startEdit(0);
            }
        });

        Label title = new Label("Port Management");
        title.setStyle("-fx-text-fill:#05070c; -fx-font-size:26; -fx-font-weight:bold;");
        Label subtitle = new Label("Monitor and manage port operations, capacity, vessel traffic, berths and operational conditions.");
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
                statBox("High Congestion", "0", "Requires Attention", "!", "#ef4444", "#fef2f2", label -> congestionValue = label),
                statBox("Vessels in Port", "0", "Currently Docked", "▣", "#0b63ff", "#f5f3ff", label -> vesselsValue = label),
                statBox("Berth Usage", "0 / 0", "Total Berths Occupied", "▤", "#f97316", "#fff7ed", label -> berthValue = label)
        );
        stats.getChildren().forEach(node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            if (node instanceof VBox) ((VBox) node).setMaxWidth(Double.MAX_VALUE);
        });

        // 2. Main Section: Left Control Center Table (52%) + Right Add/Edit Panel (48%)
        Region listCard = portListCard();
        Region formCard = portFormCard();
        HBox main = new HBox(14, listCard, formCard);
        listCard.prefWidthProperty().bind(main.widthProperty().multiply(0.52));
        formCard.prefWidthProperty().bind(main.widthProperty().multiply(0.48));
        HBox.setHgrow(listCard, Priority.ALWAYS);
        HBox.setHgrow(formCard, Priority.ALWAYS);

        // 3. Supporting Bottom Cards
        HBox bottom = new HBox(12, operationalSummaryCard(), recentPortActivityCard(), capacityOverviewCard());
        bottom.getChildren().forEach(node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            if (node instanceof VBox) ((VBox) node).setMaxWidth(Double.MAX_VALUE);
        });

        page.getChildren().addAll(titleRow, stats, main, bottom);

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

        statusFilter = combo("Filter", "All Status", "Operational", "Busy", "High Congestion", "Maintenance", "Closed", "Restricted");
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
                "-fx-cursor:hand;"
        );
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
        table.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(22); // Port
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(15); // Location
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(16); // Status
        ColumnConstraints c4 = new ColumnConstraints(); c4.setPercentWidth(10); // Vessels
        ColumnConstraints c5 = new ColumnConstraints(); c5.setPercentWidth(17); // Berth Usage
        ColumnConstraints c6 = new ColumnConstraints(); c6.setPercentWidth(10); // Capacity
        ColumnConstraints c7 = new ColumnConstraints(); c7.setPercentWidth(10); // Action
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
                "-fx-background-radius:6;"
        );

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
        maintenanceInfo = new TextArea();
        status = combo("Status", "Operational", "Busy", "High Congestion", "Maintenance", "Closed", "Restricted");

        portName.setPromptText("e.g. Port of JNPT");
        location.setPromptText("e.g. Navi Mumbai, MH");
        portCodeField.setPromptText("e.g. IN-BOM-01");
        berthCount.setPromptText("e.g. 18");
        portCapacity.setPromptText("e.g. 85%");
        maintenanceInfo.setPromptText("Enter current maintenance schedule or operational notices...");
        maintenanceInfo.setPrefRowCount(4);
        maintenanceInfo.setWrapText(true);

        styleCustomField(portName);
        styleCustomField(location);
        styleCustomField(portCodeField);
        styleCustomField(berthCount);
        styleCustomField(portCapacity);
        styleCustomField(maintenanceInfo);

        // Action Buttons
        saveBtn = primaryButton("💾  Save Changes");
        saveBtn.setStyle(
                "-fx-background-color:" + BLUE + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-size:14;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:10 20;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );
        saveBtn.setOnAction(event -> savePort());

        clearBtn = new Button("🧹  Clear Form");
        clearBtn.setStyle(
                "-fx-background-color:#f1f5f9;" +
                "-fx-text-fill:#475569;" +
                "-fx-font-size:14;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:10 16;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );
        clearBtn.setOnAction(event -> clearFormFields());

        HBox buttonRow = new HBox(10, clearBtn, saveBtn);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        VBox gridFields = new VBox(10,
                new HBox(10, labeledControl("Port Name *", portName), labeledControl("Location *", location)),
                new HBox(10, labeledControl("Port Code *", portCodeField), labeledControl("Port Status *", status)),
                new HBox(10, labeledControl("Port Capacity (%) *", portCapacity), labeledControl("Number of Berths *", berthCount)),
                labeledControl("Maintenance Information & Notices", maintenanceInfo)
        );

        // Allow HBox children in form grid to grow equally
        gridFields.getChildren().forEach(node -> {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                for (var child : row.getChildren()) {
                    HBox.setHgrow(child, Priority.ALWAYS);
                    if (child instanceof VBox) ((VBox) child).setMaxWidth(Double.MAX_VALUE);
                }
            }
        });

        VBox card = new VBox(12,
                tabs,
                formHeaderRow,
                gridFields,
                buttonRow
        );
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
                    "-fx-cursor:hand;"
            );
            editTabBtn.setStyle(
                    "-fx-background-color:" + BLUE + ";" +
                    "-fx-text-fill:white;" +
                    "-fx-font-size:13;" +
                    "-fx-font-weight:bold;" +
                    "-fx-padding:7 16;" +
                    "-fx-background-radius:6;" +
                    "-fx-cursor:hand;"
            );
        } else {
            addTabBtn.setStyle(
                    "-fx-background-color:" + BLUE + ";" +
                    "-fx-text-fill:white;" +
                    "-fx-font-size:13;" +
                    "-fx-font-weight:bold;" +
                    "-fx-padding:7 16;" +
                    "-fx-background-radius:6;" +
                    "-fx-cursor:hand;"
            );
            editTabBtn.setStyle(
                    "-fx-background-color:#f1f5f9;" +
                    "-fx-text-fill:#475569;" +
                    "-fx-font-size:13;" +
                    "-fx-font-weight:bold;" +
                    "-fx-padding:7 16;" +
                    "-fx-background-radius:6;" +
                    "-fx-cursor:hand;"
            );
        }
    }

    // ==========================================
    // REFRESH TABLE & DATA
    // ==========================================
    private void refreshTable() {
        table.getChildren().clear();

        // 7 Columns: Port, Location, Status, Vessels, Berth Usage, Capacity, Action
        String[] headers = {"Port", "Location", "Status", "Vessels", "Berth Usage", "Capacity", "Action"};
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
            if ("High Congestion".equals(port.status) || "Busy".equals(port.status) || port.vessels >= port.berths * 0.85) {
                highCongestion++;
            }
            vessels += port.vessels;
            berths += port.berths;
            occupied += port.occupiedBerths();
        }

        if (totalValue != null) totalValue.setText(String.valueOf(ports.size()));
        if (activeValue != null) activeValue.setText(String.valueOf(active));
        if (maintenanceValue != null) maintenanceValue.setText(String.valueOf(maintenance));
        if (congestionValue != null) congestionValue.setText(String.valueOf(highCongestion));
        if (vesselsValue != null) vesselsValue.setText(String.valueOf(vessels));
        if (berthValue != null) berthValue.setText(occupied + " / " + berths);
    }

    private void startAdd() {
        editingIndex = -1;
        formTitle.setText("Add New Port");
        formSubtitle.setText("Create a new registered maritime port facility.");
        formModeBadge.setText("MODE: ADD");
        formModeBadge.setStyle("-fx-background-color:#eff6ff; -fx-text-fill:" + BLUE + "; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:6;");

        clearFormFields();
        updateTabStyles();
        if (portName != null) portName.requestFocus();
    }

    private void startEdit(int index) {
        if (index < 0 || index >= ports.size()) return;
        editingIndex = index;
        Port port = ports.get(index);

        formTitle.setText("Edit Port");
        formSubtitle.setText("Modifying details for: " + port.name);
        formModeBadge.setText("MODE: EDITING #" + (index + 1));
        formModeBadge.setStyle("-fx-background-color:#ecfdf5; -fx-text-fill:#047857; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:6;");

        portName.setText(port.name);
        location.setText(port.location);
        portCodeField.setText("IN-" + port.name.replaceAll("[^A-Za-z]", "").toUpperCase().substring(0, Math.min(3, port.name.length())) + "-0" + (index + 1));
        berthCount.setText(String.valueOf(port.berths));
        portCapacity.setText(port.capacity);
        maintenanceInfo.setText(port.maintenance);
        status.setValue(port.status);

        updateTabStyles();
    }

    private void clearFormFields() {
        if (portName != null) portName.clear();
        if (location != null) location.clear();
        if (portCodeField != null) portCodeField.clear();
        if (berthCount != null) berthCount.clear();
        if (portCapacity != null) portCapacity.clear();
        if (maintenanceInfo != null) maintenanceInfo.clear();
        if (status != null) status.setValue("Operational");
    }

    private void savePort() {
        String nameValue = portName.getText().trim();
        String locationValue = location.getText().trim();
        String berthsValue = berthCount.getText().trim();
        String capacityValue = portCapacity.getText().trim();
        String maintenanceValue = maintenanceInfo.getText().trim();
        String statusValue = status.getValue();

        if (nameValue.isEmpty()) {
            formSubtitle.setText("⚠️ Please enter a port name.");
            return;
        }
        if (locationValue.isEmpty()) {
            formSubtitle.setText("⚠️ Please enter a location.");
            return;
        }
        if (berthsValue.isEmpty()) {
            formSubtitle.setText("⚠️ Please enter number of berths.");
            return;
        }
        if (capacityValue.isEmpty()) {
            formSubtitle.setText("⚠️ Please enter port capacity.");
            return;
        }
        if (statusValue == null || statusValue.isBlank()) {
            formSubtitle.setText("⚠️ Please select a status.");
            return;
        }

        int berths;
        try {
            berths = Integer.parseInt(berthsValue);
            if (berths <= 0) {
                formSubtitle.setText("⚠️ Number of berths must be greater than zero.");
                return;
            }
        } catch (NumberFormatException ex) {
            formSubtitle.setText("⚠️ Number of berths must be numeric.");
            return;
        }

        if (editingIndex >= 0 && editingIndex < ports.size()) {
            Port old = ports.get(editingIndex);
            old.name = nameValue;
            old.location = locationValue;
            old.berths = berths;
            old.status = statusValue;
            old.capacity = capacityValue;
            old.maintenance = maintenanceValue.isEmpty() ? "Clear" : maintenanceValue;
            old.lastUpdated = now();
            formSubtitle.setText("✓ Port updated successfully!");
        } else {
            ports.add(new Port(nameValue, locationValue, berths, 0, statusValue, "Containers", capacityValue, maintenanceValue.isEmpty() ? "Clear" : maintenanceValue, now()));
            editingIndex = ports.size() - 1;
            formSubtitle.setText("✓ New port added successfully!");
        }

        refreshTable();
        refreshStats();
        startEdit(editingIndex);
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
                summaryTile("⊘", "Inactive", countInactive(), "#64748b", "#f8fafc")
        );
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
                capacityLegend("Inactive Ports", countInactive(), ports.size(), "#94a3b8")
        );
        HBox middle = new HBox(16, donut, legend);
        middle.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(legend, Priority.ALWAYS);

        HBox footer = new HBox(12, new Label("▣"), new VBox(2, boldLabel("Total Berths"), mutedLabel(totalOccupiedBerths() + " / " + totalBerths() + " Used")), new Region(), new VBox(2, boldLabel(average + "%"), mutedLabel("Average Usage")));
        HBox.setHgrow(footer.getChildren().get(2), Priority.ALWAYS);
        footer.setPadding(new Insets(10, 12, 10, 12));
        footer.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e5edf6; -fx-border-radius:8; -fx-background-radius:8;");
        footer.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(header, middle, footer);
        return card;
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
        cell.setStyle("-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0; -fx-cursor:hand;");
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
                "-fx-cursor:hand;"
        );
        editBtn.setOnAction(e -> startEdit(index));

        HBox cell = new HBox(editBtn);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setMinHeight(52);
        cell.setPadding(new Insets(0, 12, 0, 12));
        cell.setStyle("-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return cell;
    }

    private Label headerCell(String text) {
        Label cell = new Label(text);
        cell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setMinHeight(46);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 12, 0, 12));
        cell.setStyle("-fx-background-color:#f8fafc; -fx-text-fill:#0f172a; -fx-font-size:14; -fx-font-weight:bold; -fx-border-color:#e2e8f0; -fx-border-width:0 0 1 0;");
        return cell;
    }

    private Label bodyCell(String text) {
        Label cell = new Label(text);
        cell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setMinHeight(52);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 12, 0, 12));
        cell.setStyle("-fx-background-color:white; -fx-text-fill:#111827; -fx-font-size:14; -fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return cell;
    }

    private HBox statusBadge(String text) {
        Label badge = new Label(text);
        badge.setPadding(new Insets(4, 10, 4, 10));
        badge.setStyle("-fx-background-color:" + statusBackground(text) + "; -fx-background-radius:12; -fx-text-fill:" + statusColor(text) + "; -fx-font-size:12; -fx-font-weight:bold;");

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
        icon.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + color + "; -fx-font-size:18; -fx-font-weight:bold; -fx-background-radius:8; -fx-min-width:42; -fx-min-height:42;");
        Label number = new Label(String.valueOf(value));
        number.setStyle("-fx-text-fill:" + color + "; -fx-font-size:26; -fx-font-weight:800;");
        Label label = new Label(title);
        label.setStyle("-fx-text-fill:" + color + "; -fx-font-size:12; -fx-font-weight:bold;");
        VBox tile = new VBox(6, icon, number, label);
        tile.setAlignment(Pos.CENTER);
        tile.setPadding(new Insets(12, 10, 12, 10));
        tile.setMaxWidth(Double.MAX_VALUE);
        tile.setStyle("-fx-background-color:" + bg + "; -fx-border-color:" + color + "22; -fx-border-radius:8; -fx-background-radius:8;");
        return tile;
    }

    private HBox activityRow(Port port) {
        String color = statusColor(port.status);
        Label icon = new Label("⚓");
        icon.setAlignment(Pos.CENTER);
        icon.setStyle("-fx-background-color:" + statusBackground(port.status) + "; -fx-text-fill:" + color + "; -fx-font-size:14; -fx-font-weight:bold; -fx-background-radius:999; -fx-min-width:32; -fx-min-height:32;");
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

    private VBox statBox(String title, String value, String detail, String iconText, String color, String bg, LabelSetter setter) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14; -fx-font-weight:bold;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill:" + color + "; -fx-font-size:28; -fx-font-weight:800;");
        setter.set(valueLabel);

        Label detailLabel = new Label(detail);
        detailLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        Label icon = new Label(iconText);
        icon.setAlignment(Pos.CENTER);
        icon.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + color + "; -fx-font-size:22; -fx-font-weight:bold; -fx-background-radius:999; -fx-min-width:52; -fx-min-height:52;");

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
                "-fx-text-fill:#0f172a;"
        );
    }

    private String usageColor(double pct) {
        if (pct >= 0.85) return "#ef4444";
        if (pct >= 0.7) return "#f97316";
        if (pct >= 0.5) return "#0b63ff";
        return "#16a34a";
    }

    private int countOperational() {
        int count = 0;
        for (Port port : ports) {
            if ("Operational".equals(port.status)) count++;
        }
        return count;
    }

    private int countMaintenance() {
        int count = 0;
        for (Port port : ports) {
            if ("Maintenance".equals(port.status)) count++;
        }
        return count;
    }

    private int countHighCongestion() {
        int count = 0;
        for (Port port : ports) {
            if ("High Congestion".equals(port.status) || "Busy".equals(port.status) || port.vessels >= port.berths * 0.85) count++;
        }
        return count;
    }

    private int countInactive() {
        int count = 0;
        for (Port port : ports) {
            if ("Closed".equals(port.status) || "Restricted".equals(port.status) || "Inactive".equals(port.status) || "Deactivated".equals(port.status)) count++;
        }
        return count;
    }

    private int totalBerths() {
        int total = 0;
        for (Port port : ports) total += port.berths;
        return total;
    }

    private int totalOccupiedBerths() {
        int total = 0;
        for (Port port : ports) total += port.occupiedBerths();
        return total;
    }

    private int averageCapacity() {
        if (ports.isEmpty()) return 0;
        int total = 0;
        for (Port port : ports) total += capacityPercent(port.capacity);
        return Math.round(total / (float) ports.size());
    }

    private int capacityPercent(String value) {
        if (value == null) return 0;
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isBlank()) return 0;
        try {
            return Math.max(0, Math.min(100, Integer.parseInt(digits)));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String lastUpdatedShort(String value) {
        if (value == null || value.isBlank()) return "Now";
        int comma = value.indexOf(',');
        return comma >= 0 && comma + 2 < value.length() ? value.substring(comma + 2) : value;
    }

    private String alertSummary(Port port) {
        if (!"Clear".equalsIgnoreCase(port.maintenance)) return port.maintenance;
        if (port.vessels >= port.berths * 0.85) return "High congestion";
        if ("Busy".equals(port.status)) return "Traffic watch";
        return "No active alerts";
    }

    private boolean matches(Port port, String query) {
        String selectedStatus = statusFilter == null || statusFilter.getValue() == null ? "All Status" : statusFilter.getValue();
        boolean statusMatches = "All Status".equals(selectedStatus) || selectedStatus.equals(port.status);
        boolean queryMatches = query.isEmpty()
                || port.name.toLowerCase().contains(query)
                || port.location.toLowerCase().contains(query)
                || port.status.toLowerCase().contains(query)
                || port.cargo.toLowerCase().contains(query);

        return queryMatches && statusMatches;
    }

    private String statusColor(String value) {
        if ("Operational".equals(value)) return "#16a34a";
        if ("High Congestion".equals(value) || "Busy".equals(value)) return "#ef4444";
        if ("Available".equals(value)) return "#16a34a";
        if ("Maintenance".equals(value)) return BLUE;
        if ("Occupied".equals(value)) return "#d97706";
        if ("Restricted".equals(value) || "Closed".equals(value)) return "#64748b";
        return "#d97706";
    }

    private String statusBackground(String value) {
        if ("Operational".equals(value)) return "#ecfdf5";
        if ("High Congestion".equals(value) || "Busy".equals(value)) return "#fee2e2";
        if ("Maintenance".equals(value)) return "#eff6ff";
        if ("Restricted".equals(value) || "Closed".equals(value)) return "#f1f5f9";
        return "#fff7ed";
    }

    private interface LabelSetter {
        void set(Label label);
    }

    private String now() {
        return store.now();
    }
}
