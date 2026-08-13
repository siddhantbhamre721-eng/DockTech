package com.projectui.view.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class PortManagementPage extends AdminTheme {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private final List<Port> ports = new ArrayList<>();
    private final GridPane table = new GridPane();

    private Label totalValue;
    private Label activeValue;
    private Label maintenanceValue;
    private TextField search;
    private TextField portName;
    private TextField location;
    private TextField berthCount;
    private TextField portCapacity;
    private TextField maintenanceInfo;
    private ComboBox<String> status;
    private Button save;
    private Label formTitle;
    private Label formSubtitle;
    private VBox detailsBox;
    private GridPane berthTable;
    private int editingIndex = -1;

    private final boolean startAddOnOpen;

    public PortManagementPage() {
        this(false);
    }

    public PortManagementPage(boolean startAddOnOpen) {
        this.startAddOnOpen = startAddOnOpen;
        ports.add(new Port("Port of Mumbai", "Mumbai", 18, 12, "Operational", "Mixed cargo, containers", "82%", "Clear", now()));
        ports.add(new Port("Port of Chennai", "Chennai", 14, 8, "Operational", "Automobiles, containers", "76%", "Clear", now()));
        ports.add(new Port("Port of Vizag", "Visakhapatnam", 12, 5, "Maintenance", "Bulk cargo, minerals", "58%", "Crane service in progress", now()));
        ports.add(new Port("Port of Kolkata", "Kolkata", 10, 9, "Busy", "River cargo, containers", "91%", "High congestion watch", now()));
    }

    public Parent getRoot() {
        Button add = primaryButton("+  Add Port");
        add.setOnAction(event -> startAdd());

        VBox page = pageContent();
        page.getChildren().addAll(
                pageHeader("Port Management", "Manage registered ports", add),
                new HBox(16,
                        statBox("Total Ports", "0", TEXT, label -> totalValue = label),
                        statBox("Active Ports", "0", "#0aa072", label -> activeValue = label),
                        statBox("Maintenance", "0", "#f97316", label -> maintenanceValue = label)
                ),
                new HBox(18, portListCard(), portFormCard())
        );

        refreshTable();
        refreshStats();
        if (startAddOnOpen) {
            startAdd();
        } else if (!ports.isEmpty()) {
            startEdit(0);
        }
        return page;
    }

    private VBox portListCard() {
        search = new TextField();
        search.setPromptText("⌕  Search ports");
        search.setPrefWidth(155);
        styleField(search);
        search.setStyle(search.getStyle() + "-fx-background-color:#f2f2f5; -fx-border-color:transparent;");
        search.textProperty().addListener((obs, oldValue, newValue) -> refreshTable());

        Label title = new Label("Port List");
        title.setStyle("-fx-text-fill:#05070c; -fx-font-size:17; -fx-font-weight:bold;");

        Label subtitle = new Label("View and edit registered ports");
        subtitle.setStyle("-fx-text-fill:#747884; -fx-font-size:12;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox top = new HBox(14, new VBox(8, title, subtitle), space, search);
        top.setAlignment(Pos.CENTER_LEFT);

        styleTable();

        VBox card = new VBox(20, top, horizontalScroll(table));
        card.setPrefWidth(650);
        card.setPadding(new Insets(22));
        card.setStyle(CARD_STYLE);
        return card;
    }

    private VBox portFormCard() {
        formTitle = new Label("Add / Edit Port");
        formTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:21; -fx-font-weight:bold;");

        formSubtitle = new Label("Update port details");
        formSubtitle.setStyle("-fx-text-fill:#747884; -fx-font-size:14;");

        portName = new TextField();
        location = new TextField();
        berthCount = new TextField();
        portCapacity = new TextField();
        maintenanceInfo = new TextField();
        status = combo("Status", "Operational", "Busy", "Maintenance", "Restricted");
        styleField(portName);
        styleField(location);
        styleField(berthCount);
        styleField(portCapacity);
        styleField(maintenanceInfo);

        save = primaryButton("Save");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setOnAction(event -> savePort());

        detailsBox = new VBox(8);
        berthTable = new GridPane();

        VBox card = new VBox(10, formTitle, formSubtitle);
        card.setPrefWidth(330);
        card.setPadding(new Insets(22));
        card.setStyle(CARD_STYLE);
        card.getChildren().addAll(
                labeledControl("Port Name", portName),
                labeledControl("Location", location),
                labeledControl("Number of Berths", berthCount),
                labeledControl("Port Capacity", portCapacity),
                labeledControl("Status", status),
                labeledControl("Maintenance Information", maintenanceInfo),
                detailsBox,
                berthTable,
                save
        );
        return card;
    }

    private void refreshTable() {
        table.getChildren().clear();
        styleTable();

        String[] headers = {"Port Name", "Location", "Berths", "Vessels", "Status", "Action"};
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

            table.add(bodyCell(port.name), 0, displayRow);
            table.add(bodyCell(port.location), 1, displayRow);
            table.add(bodyCell(String.valueOf(port.berths)), 2, displayRow);
            table.add(bodyCell(String.valueOf(port.vessels)), 3, displayRow);
            table.add(statusBadge(port.status), 4, displayRow);
            table.add(actionCell(i), 5, displayRow);
            displayRow++;
        }

        if (displayRow == 1) {
            Label empty = bodyCell("No ports found");
            empty.setMinWidth(768);
            table.add(empty, 0, 1, 6, 1);
        }
    }

    private void refreshStats() {
        int active = 0;
        int maintenance = 0;
        for (Port port : ports) {
            if ("Operational".equals(port.status)) {
                active++;
            }
            if ("Maintenance".equals(port.status)) {
                maintenance++;
            }
        }

        totalValue.setText(String.valueOf(ports.size()));
        activeValue.setText(String.valueOf(active));
        maintenanceValue.setText(String.valueOf(maintenance));
    }

    private void startAdd() {
        editingIndex = -1;
        formTitle.setText("Add Port");
        formSubtitle.setText("Create a new registered port");
        portName.clear();
        location.clear();
        berthCount.clear();
        portCapacity.clear();
        maintenanceInfo.clear();
        status.setValue("Operational");
        detailsBox.getChildren().clear();
        berthTable.getChildren().clear();
        setFormEditable(true);
        save.setText("Save");
        portName.requestFocus();
    }

    private void startView(int index) {
        editingIndex = index;
        Port port = ports.get(index);
        formTitle.setText("View Port");
        formSubtitle.setText("Port details");
        portName.setText(port.name);
        location.setText(port.location);
        berthCount.setText(String.valueOf(port.berths));
        portCapacity.setText(port.capacity);
        maintenanceInfo.setText(port.maintenance);
        status.setValue(port.status);
        showPortDetails(port);
        setFormEditable(false);
        save.setText("Edit");
        save.setOnAction(event -> startEdit(index));
    }

    private void startEdit(int index) {
        editingIndex = index;
        Port port = ports.get(index);
        formTitle.setText("Add / Edit Port");
        formSubtitle.setText("Update port details");
        portName.setText(port.name);
        location.setText(port.location);
        berthCount.setText(String.valueOf(port.berths));
        portCapacity.setText(port.capacity);
        maintenanceInfo.setText(port.maintenance);
        status.setValue(port.status);
        detailsBox.getChildren().clear();
        berthTable.getChildren().clear();
        setFormEditable(true);
        save.setText("Save");
        save.setOnAction(event -> savePort());
    }

    private void savePort() {
        String nameValue = portName.getText().trim();
        String locationValue = location.getText().trim();
        String berthsValue = berthCount.getText().trim();
        String capacityValue = portCapacity.getText().trim();
        String maintenanceValue = maintenanceInfo.getText().trim();
        String statusValue = status.getValue();

        if (nameValue.isEmpty()) {
            formSubtitle.setText("Please enter a port name.");
            return;
        }
        if (locationValue.isEmpty()) {
            formSubtitle.setText("Please enter a location.");
            return;
        }
        if (berthsValue.isEmpty()) {
            formSubtitle.setText("Please enter number of berths.");
            return;
        }
        if (capacityValue.isEmpty()) {
            formSubtitle.setText("Please enter port capacity.");
            return;
        }
        if (statusValue == null || statusValue.isBlank()) {
            formSubtitle.setText("Please select a status.");
            return;
        }
        int berths;
        try {
            berths = Integer.parseInt(berthsValue);
            if (berths <= 0) {
                formSubtitle.setText("Number of berths must be greater than zero.");
                return;
            }
        } catch (NumberFormatException ex) {
            formSubtitle.setText("Number of berths must be numeric.");
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
            formSubtitle.setText("Port updated successfully");
        } else {
            ports.add(new Port(nameValue, locationValue, berths, 0, statusValue, "Containers", capacityValue, maintenanceValue.isEmpty() ? "Clear" : maintenanceValue, now()));
            editingIndex = ports.size() - 1;
            formSubtitle.setText("Port added successfully");
        }

        refreshTable();
        refreshStats();
        startEdit(editingIndex);
    }

    private void setFormEditable(boolean editable) {
        portName.setEditable(editable);
        location.setEditable(editable);
        berthCount.setEditable(editable);
        portCapacity.setEditable(editable);
        maintenanceInfo.setEditable(editable);
        status.setDisable(!editable);
        portName.setOpacity(1);
        location.setOpacity(1);
        berthCount.setOpacity(1);
        portCapacity.setOpacity(1);
        maintenanceInfo.setOpacity(1);
        status.setOpacity(1);
    }

    private boolean matches(Port port, String query) {
        return query.isEmpty()
                || port.name.toLowerCase().contains(query)
                || port.location.toLowerCase().contains(query)
                || port.status.toLowerCase().contains(query);
    }

    private void showPortDetails(Port port) {
        detailsBox.getChildren().clear();
        berthTable.getChildren().clear();

        detailsBox.getChildren().addAll(
                detailRow("Total Berths", String.valueOf(port.berths)),
                detailRow("Occupied Berths", String.valueOf(port.occupiedBerths())),
                detailRow("Available Berths", String.valueOf(port.availableBerths())),
                detailRow("Active Vessels", String.valueOf(port.vessels)),
                detailRow("Cargo Being Handled", port.cargo),
                detailRow("Port Capacity", port.capacity),
                detailRow("Maintenance Status", port.maintenance),
                detailRow("Last Updated", port.lastUpdated)
        );

        berthTable.setHgap(0);
        berthTable.setVgap(0);
        berthTable.setStyle("-fx-background-color:white; -fx-border-color:#dfe3ea; -fx-border-radius:12; -fx-background-radius:12;");
        String[] headers = {"Berth", "Vessel", "Status", "ETA"};
        for (int col = 0; col < headers.length; col++) {
            berthTable.add(smallCell(headers[col], true), col, 0);
        }

        String[][] rows = {
                {"B01", "Oceanic Star", "Occupied", "10:30"},
                {"B02", "-", "Available", "-"},
                {"B03", "Sea Voyager", "Occupied", "14:20"},
                {"B04", "-", "Maintenance", "-"}
        };
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < headers.length; col++) {
                berthTable.add(smallCell(rows[row][col], false), col, row + 1);
            }
        }
    }

    private void styleTable() {
        table.setHgap(0);
        table.setVgap(0);
        table.setPadding(new Insets(0));
        table.setStyle("-fx-background-color:white; -fx-border-color:#dfe3ea; -fx-border-radius:16; -fx-background-radius:16;");
    }

    private Label headerCell(String text) {
        Label cell = new Label(text);
        cell.setMinWidth(105);
        cell.setMinHeight(48);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 18, 0, 18));
        cell.setStyle("-fx-background-color:#f7f9fc; -fx-text-fill:#1c1d22; -fx-font-size:12; -fx-font-weight:bold; -fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return cell;
    }

    private Label bodyCell(String text) {
        Label cell = new Label(text);
        cell.setMinWidth(105);
        cell.setMinHeight(54);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 18, 0, 18));
        cell.setStyle("-fx-background-color:white; -fx-text-fill:#111827; -fx-font-size:12; -fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return cell;
    }

    private HBox statusBadge(String text) {
        Label badge = new Label(text);
        badge.setPadding(new Insets(5, 12, 5, 12));
        badge.setStyle("-fx-background-color:" + statusBackground(text) + "; -fx-background-radius:12; -fx-text-fill:" + statusColor(text) + "; -fx-font-size:11; -fx-font-weight:bold;");

        HBox cell = new HBox(badge);
        cell.setMinWidth(105);
        cell.setMinHeight(54);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 12, 0, 12));
        cell.setStyle("-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return cell;
    }

    private HBox actionCell(int index) {
        Button view = linkButton("View");
        Button edit = linkButton("Edit");
        Label divider = new Label("|");
        divider.setStyle("-fx-text-fill:#d1d5db; -fx-font-size:16;");

        view.setOnAction(event -> startView(index));
        edit.setOnAction(event -> startEdit(index));

        HBox cell = new HBox(10, view, divider, edit);
        cell.setMinWidth(105);
        cell.setMinHeight(54);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 12, 0, 12));
        cell.setStyle("-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return cell;
    }

    private Button linkButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:transparent; -fx-text-fill:" + BLUE + "; -fx-font-size:11; -fx-font-weight:bold; -fx-cursor:hand; -fx-padding:0;");
        return button;
    }

    private VBox statBox(String title, String value, String color, LabelSetter setter) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#475467; -fx-font-size:12; -fx-font-weight:bold;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill:" + color + "; -fx-font-size:28; -fx-font-weight:bold;");
        setter.set(valueLabel);

        VBox card = new VBox(6, titleLabel, valueLabel);
        card.setPrefWidth(210);
        card.setMinHeight(112);
        card.setPadding(new Insets(18, 20, 16, 20));
        card.setStyle(CARD_STYLE);
        return card;
    }

    private HBox detailRow(String labelText, String value) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill:#747884; -fx-font-size:13; -fx-font-weight:bold;");

        Label valueLabel = new Label(value);
        valueLabel.setWrapText(true);
        valueLabel.setStyle("-fx-text-fill:#111827; -fx-font-size:13; -fx-font-weight:bold;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox row = new HBox(10, label, space, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 0, 6, 0));
        row.setStyle("-fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return row;
    }

    private Label smallCell(String text, boolean header) {
        Label cell = new Label(text);
        cell.setMinWidth(80);
        cell.setMinHeight(header ? 38 : 42);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 8, 0, 8));
        cell.setStyle(
                "-fx-background-color:" + (header ? "#f7f7fa" : "white") + ";" +
                "-fx-text-fill:" + (header ? TEXT : smallCellColor(text)) + ";" +
                "-fx-font-size:12;" +
                "-fx-font-weight:" + (header || isBerthStatus(text) ? "bold" : "normal") + ";" +
                "-fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;"
        );
        return cell;
    }

    private VBox labeledControl(String labelText, Parent field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill:#111827; -fx-font-size:14; -fx-font-weight:bold;");
        return new VBox(8, label, field);
    }

    private String statusColor(String value) {
        if ("Operational".equals(value)) return "#0aa072";
        if ("Available".equals(value)) return "#0aa072";
        if ("Maintenance".equals(value)) return BLUE;
        if ("Occupied".equals(value)) return "#d97706";
        if ("Restricted".equals(value)) return "#ef4444";
        return "#d97706";
    }

    private String statusBackground(String value) {
        if ("Operational".equals(value)) return "#c8f5df";
        if ("Maintenance".equals(value)) return "#dbeafe";
        if ("Restricted".equals(value)) return "#fee2e2";
        return "#fff3cf";
    }

    private boolean isBerthStatus(String value) {
        return "Available".equals(value) || "Occupied".equals(value) || "Maintenance".equals(value);
    }

    private String smallCellColor(String value) {
        return isBerthStatus(value) ? statusColor(value) : "#747884";
    }

    private interface LabelSetter {
        void set(Label label);
    }

    private String now() {
        return LocalDateTime.now().format(DATE_TIME);
    }

    private static class Port {
        private String name;
        private String location;
        private int berths;
        private int vessels;
        private String status;
        private String cargo;
        private String capacity;
        private String maintenance;
        private String lastUpdated;

        private Port(String name, String location, int berths, int vessels, String status, String cargo, String capacity, String maintenance, String lastUpdated) {
            this.name = name;
            this.location = location;
            this.berths = berths;
            this.vessels = vessels;
            this.status = status;
            this.cargo = cargo;
            this.capacity = capacity;
            this.maintenance = maintenance;
            this.lastUpdated = lastUpdated;
        }

        private int occupiedBerths() {
            return Math.min(berths, vessels);
        }

        private int availableBerths() {
            return Math.max(0, berths - occupiedBerths());
        }
    }

}
