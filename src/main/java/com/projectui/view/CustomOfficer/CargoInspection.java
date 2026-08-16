package com.projectui.view.CustomOfficer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class CargoInspection {
    private final VBox content;

    // Counts
    private int pendingCount = 18;
    private int underInspectionCount = 12;
    private int approvedCount = 142;
    private int rejectedCount = 7;

    private Label pendingLabel;
    private Label underInspectionLabel;
    private Label approvedLabel;
    private Label rejectedLabel;

    private VBox tableRowsContainer;
    private List<CargoItem> cargoDataList = new ArrayList<>();
    private String activeFilter = "All";

    public CargoInspection() {
        initData();

        content = new VBox(22);
        content.setPadding(new Insets(25, 35, 35, 35));
        content.setStyle("-fx-background-color:#f6f8fb;");

        // HEADER
        Label title = new Label("Cargo Inspection");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        title.setTextFill(Color.web("#172033"));

        Label subtitle = new Label(
                "Inspect cargo and approve or reject shipments based on customs verification.");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#64748b"));

        VBox heading = new VBox(5, title, subtitle);

        // CARDS
        HBox cards = new HBox(18);
        VBox cardPending = createCard("Pending Inspection", String.valueOf(pendingCount), "Awaiting review", "◷", "#f59e0b");
        VBox cardUnder = createCard("Under Inspection", String.valueOf(underInspectionCount), "Currently in progress", "⌕", "#087cf5");
        VBox cardApproved = createCard("Approved", String.valueOf(approvedCount), "Cleared customs", "✓", "#10b981");
        VBox cardRejected = createCard("Rejected", String.valueOf(rejectedCount), "Inspection failed", "✕", "#ef4444");

        this.pendingLabel = (Label) ((VBox) ((HBox) cardPending.getChildren().get(0)).getChildren().get(0)).getChildren().get(1);
        this.underInspectionLabel = (Label) ((VBox) ((HBox) cardUnder.getChildren().get(0)).getChildren().get(0)).getChildren().get(1);
        this.approvedLabel = (Label) ((VBox) ((HBox) cardApproved.getChildren().get(0)).getChildren().get(0)).getChildren().get(1);
        this.rejectedLabel = (Label) ((VBox) ((HBox) cardRejected.getChildren().get(0)).getChildren().get(0)).getChildren().get(1);

        cards.getChildren().addAll(cardPending, cardUnder, cardApproved, cardRejected);

        // QUEUE SECTION
        VBox tableBox = new VBox(16);
        tableBox.setPadding(new Insets(22));
        tableBox.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:14;" +
                "-fx-border-color:#e5e7eb;" +
                "-fx-border-radius:14;");

        Label tableTitle = new Label("Cargo Inspection Queue");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        tableTitle.setTextFill(Color.web("#172033"));

        Label tableSubtitle = new Label("Review cargo shipments requiring customs inspection.");
        tableSubtitle.setFont(Font.font("Arial", 14));
        tableSubtitle.setTextFill(Color.web("#64748b"));

        VBox tableHeading = new VBox(4, tableTitle, tableSubtitle);

        // FILTER BAR
        HBox filterBar = createFilterBar();

        // TABLE CONTAINER
        VBox tableHeaderRow = createTableHeader();
        tableRowsContainer = new VBox();
        renderRows(cargoDataList);

        tableBox.getChildren().addAll(tableHeading, filterBar, tableHeaderRow, tableRowsContainer);
        VBox.setVgrow(tableBox, Priority.ALWAYS);

        content.getChildren().addAll(heading, cards, tableBox);
    }

    // Data structure
    private static class CargoItem {
        String id;
        String name;
        String company;
        String category;
        String weight;
        String inspectionStatus; // Pending, Under Inspection, Completed, Hold
        String customsStatus;    // Pending, Approved, Rejected, Requires Inspection
        String priority;         // High, Medium, Low
        String submitted;
        String origin;
        String destination;
        String containerNo;
        String notes;

        CargoItem(String id, String name, String company, String category, String weight,
                  String inspectionStatus, String customsStatus, String priority, String submitted,
                  String origin, String destination, String containerNo) {
            this.id = id;
            this.name = name;
            this.company = company;
            this.category = category;
            this.weight = weight;
            this.inspectionStatus = inspectionStatus;
            this.customsStatus = customsStatus;
            this.priority = priority;
            this.submitted = submitted;
            this.origin = origin;
            this.destination = destination;
            this.containerNo = containerNo;
            this.notes = "";
        }
    }

    private void initData() {
        cargoDataList.add(new CargoItem("CGO-1287", "Ocean Queen", "Ocean Shipping Ltd.", "Electronics", "120 Tons", "Under Inspection", "Pending", "High", "10 Aug 2026", "Tokyo, JP", "Mumbai, IN", "MSCU-8842109"));
        cargoDataList.add(new CargoItem("CGO-1290", "Sea Dragon", "Global Logistics", "Machinery", "180 Tons", "Completed", "Approved", "Medium", "09 Aug 2026", "Hamburg, DE", "JNPT, IN", "CMAU-9921443"));
        cargoDataList.add(new CargoItem("CGO-1281", "Pacific Star", "Maritime Cargo", "Textiles", "95 Tons", "Hold", "Rejected", "High", "08 Aug 2026", "Shanghai, CN", "Chennai, IN", "MAEU-7731022"));
        cargoDataList.add(new CargoItem("CGO-1301", "Coral Queen", "Ocean Shipping Ltd.", "Pharmaceuticals", "210 Tons", "Pending", "Pending", "High", "11 Aug 2026", "Singapore, SG", "Kandla, IN", "COSU-6610994"));
        cargoDataList.add(new CargoItem("CGO-1295", "Blue Marlin", "Global Traders", "Chemicals", "145 Tons", "Under Inspection", "Pending", "Medium", "10 Aug 2026", "Rotterdam, NL", "Mumbai, IN", "HLBU-5542011"));
        cargoDataList.add(new CargoItem("CGO-1299", "Silver Wave", "Pacific Cargo Co.", "Auto Parts", "160 Tons", "Pending", "Pending", "Low", "11 Aug 2026", "Busan, KR", "JNPT, IN", "ONEU-4431988"));
    }

    private VBox createCard(String title, String value, String desc, String icon, String color) {
        VBox card = new VBox(8);
        card.setPrefHeight(120);
        card.setPadding(new Insets(18));
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:14;" +
                "-fx-border-color:#e5e7eb;" +
                "-fx-border-radius:14;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.web("#64748b"));

        Label number = new Label(value);
        number.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        number.setTextFill(Color.web("#172033"));

        Label descLabel = new Label(desc);
        descLabel.setFont(Font.font("Arial", 11));
        descLabel.setTextFill(Color.web("#94a3b8"));

        Label iconLabel = new Label(icon);
        iconLabel.setMinSize(40, 40);
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        iconLabel.setStyle(
                "-fx-background-color:" + color + "20;" +
                "-fx-text-fill:" + color + ";" +
                "-fx-background-radius:10;");

        VBox numbers = new VBox(2, titleLabel, number, descLabel);
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox row = new HBox(12, numbers, space, iconLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(row);

        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private HBox createFilterBar() {
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Filter by:");
        filterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        filterLabel.setTextFill(Color.web("#64748b"));
        filterBar.getChildren().add(filterLabel);

        String[] filters = {"All", "Pending", "Under Inspection", "Approved", "Rejected", "High Priority"};
        for (String f : filters) {
            Button btn = new Button(f);
            boolean isActive = f.equalsIgnoreCase(activeFilter);
            btn.setStyle(isActive ?
                    "-fx-background-color:#1769ff; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:8 16; -fx-background-radius:7; -fx-cursor:hand;" :
                    "-fx-background-color:#f1f5f9; -fx-text-fill:#475467; -fx-font-size:13px; -fx-padding:8 16; -fx-background-radius:7; -fx-cursor:hand;");

            btn.setOnAction(e -> {
                activeFilter = f;
                updateFilterBarStyles(filterBar);
                applyFilter();
            });
            filterBar.getChildren().add(btn);
        }
        return filterBar;
    }

    private void updateFilterBarStyles(HBox filterBar) {
        for (int i = 1; i < filterBar.getChildren().size(); i++) {
            Button btn = (Button) filterBar.getChildren().get(i);
            boolean isActive = btn.getText().equalsIgnoreCase(activeFilter);
            btn.setStyle(isActive ?
                    "-fx-background-color:#1769ff; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:8 16; -fx-background-radius:7; -fx-cursor:hand;" :
                    "-fx-background-color:#f1f5f9; -fx-text-fill:#475467; -fx-font-size:13px; -fx-padding:8 16; -fx-background-radius:7; -fx-cursor:hand;");
        }
    }

    private void applyFilter() {
        List<CargoItem> filtered;
        if ("All".equalsIgnoreCase(activeFilter)) {
            filtered = cargoDataList;
        } else if ("High Priority".equalsIgnoreCase(activeFilter)) {
            filtered = cargoDataList.stream().filter(item -> "High".equalsIgnoreCase(item.priority)).collect(Collectors.toList());
        } else {
            filtered = cargoDataList.stream().filter(item ->
                item.inspectionStatus.equalsIgnoreCase(activeFilter) ||
                item.customsStatus.equalsIgnoreCase(activeFilter)
            ).collect(Collectors.toList());
        }
        renderRows(filtered);
    }

    private VBox createTableHeader() {
        HBox header = new HBox(10);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-width:1 0 1 0;");
        header.setAlignment(Pos.CENTER_LEFT);

        header.getChildren().addAll(
                colHeader("Cargo ID", 90),
                colHeader("Cargo", 110),
                colHeader("Shipping Co.", 130),
                colHeader("Category", 110),
                colHeader("Weight", 85),
                colHeader("Inspection Status", 125),
                colHeader("Customs Status", 115),
                colHeader("Priority", 85),
                colHeader("Submitted", 95),
                colHeader("Action", 160)
        );

        return new VBox(header);
    }

    private Label colHeader(String text, double width) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        l.setTextFill(Color.web("#64748b"));
        l.setPrefWidth(width);
        return l;
    }

    private void renderRows(List<CargoItem> items) {
        tableRowsContainer.getChildren().clear();
        for (CargoItem item : items) {
            tableRowsContainer.getChildren().add(createRow(item));
        }
    }

    private HBox createRow(CargoItem item) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(14, 16, 14, 16));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color:#e2e8f0; -fx-border-width:0 0 1 0;");

        Label idLbl = cell(item.id, 90, true, "#172033");
        Label nameLbl = cell(item.name, 110, false, "#334155");
        Label compLbl = cell(item.company, 130, false, "#334155");
        Label catLbl = cell(item.category, 110, false, "#475467");
        Label wLbl = cell(item.weight, 85, false, "#475467");

        Label inspLbl = statusBadge(item.inspectionStatus, 125);
        Label custLbl = statusBadge(item.customsStatus, 115);
        Label prioLbl = priorityBadge(item.priority, 85);
        Label subLbl = cell(item.submitted, 95, false, "#64748b");

        // Actions
        Button inspectBtn = new Button("Inspect");
        inspectBtn.setStyle("-fx-background-color:#1769ff; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 12; -fx-cursor:hand;");
        inspectBtn.setOnAction(e -> openInspectionModal(item));

        Button approveBtn = new Button("Approve");
        approveBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 12; -fx-cursor:hand;");
        approveBtn.setOnAction(e -> approveCargo(item));

        Button rejectBtn = new Button("Reject");
        rejectBtn.setStyle("-fx-background-color:#dc2626; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 12; -fx-cursor:hand;");
        rejectBtn.setOnAction(e -> rejectCargo(item));

        HBox actions = new HBox(6, inspectBtn, approveBtn, rejectBtn);
        actions.setPrefWidth(160);
        actions.setAlignment(Pos.CENTER_LEFT);

        row.getChildren().addAll(
                idLbl, nameLbl, compLbl, catLbl, wLbl,
                inspLbl, custLbl, prioLbl, subLbl, actions
        );

        return row;
    }

    private Label cell(String text, double width, boolean bold, String color) {
        Label l = new Label(text);
        l.setPrefWidth(width);
        l.setFont(Font.font("Arial", bold ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        l.setTextFill(Color.web(color));
        return l;
    }

    private Label statusBadge(String status, double width) {
        Label l = new Label(status);
        l.setPrefWidth(width);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        String bg = "#f1f5f9";
        String fg = "#475467";

        if ("Approved".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
            bg = "#dcfce7"; fg = "#15803d";
        } else if ("Rejected".equalsIgnoreCase(status) || "Hold".equalsIgnoreCase(status)) {
            bg = "#fee2e2"; fg = "#b91c1c";
        } else if ("Under Inspection".equalsIgnoreCase(status)) {
            bg = "#dbeafe"; fg = "#1d4ed8";
        } else if ("Pending".equalsIgnoreCase(status)) {
            bg = "#fef3c7"; fg = "#b45309";
        }

        l.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + fg + "; -fx-padding:4 8; -fx-background-radius:6; -fx-alignment:center;");
        return l;
    }

    private Label priorityBadge(String priority, double width) {
        Label l = new Label(priority);
        l.setPrefWidth(width);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        String bg = "#f1f5f9";
        String fg = "#475467";

        if ("High".equalsIgnoreCase(priority)) {
            bg = "#fee2e2"; fg = "#dc2626";
        } else if ("Medium".equalsIgnoreCase(priority)) {
            bg = "#fef3c7"; fg = "#d97706";
        } else if ("Low".equalsIgnoreCase(priority)) {
            bg = "#e0f2fe"; fg = "#0369a1";
        }

        l.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + fg + "; -fx-padding:4 8; -fx-background-radius:6; -fx-alignment:center;");
        return l;
    }

    // APPROVE WORKFLOW
    private void approveCargo(CargoItem item) {
        item.inspectionStatus = "Completed";
        item.customsStatus = "Approved";

        recalculateStats();
        applyFilter();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cargo Approved");
        alert.setHeaderText(null);
        alert.setContentText("Cargo " + item.id + " (" + item.name + ") has been approved and cleared customs.");
        alert.showAndWait();
    }

    // REJECT WORKFLOW
    private void rejectCargo(CargoItem item) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Cargo");
        dialog.setHeaderText("Specify Rejection Reason for " + item.id);
        dialog.setContentText("Enter rejection reason:");

        dialog.showAndWait().ifPresent(reason -> {
            if (reason.trim().isEmpty()) {
                Alert warning = new Alert(Alert.AlertType.WARNING, "Rejection reason cannot be empty.");
                warning.showAndWait();
                return;
            }

            item.inspectionStatus = "Hold";
            item.customsStatus = "Rejected";
            item.notes = "Rejection Reason: " + reason;

            recalculateStats();
            applyFilter();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Cargo Rejected");
            alert.setHeaderText("Cargo Inspection Rejected");
            alert.setContentText("Cargo " + item.id + " has been rejected.\n\nReason: " + reason);
            alert.showAndWait();
        });
    }

    private void recalculateStats() {
        int p = 0, u = 0, a = 0, r = 0;
        for (CargoItem item : cargoDataList) {
            if ("Pending".equalsIgnoreCase(item.inspectionStatus)) p++;
            else if ("Under Inspection".equalsIgnoreCase(item.inspectionStatus)) u++;

            if ("Approved".equalsIgnoreCase(item.customsStatus)) a++;
            else if ("Rejected".equalsIgnoreCase(item.customsStatus)) r++;
        }
        this.pendingCount = p;
        this.underInspectionCount = u;
        this.approvedCount = a;
        this.rejectedCount = r;

        pendingLabel.setText(String.valueOf(pendingCount));
        underInspectionLabel.setText(String.valueOf(underInspectionCount));
        approvedLabel.setText(String.valueOf(approvedCount));
        rejectedLabel.setText(String.valueOf(rejectedCount));
    }

    // INSPECTION MODAL DIALOG
    private void openInspectionModal(CargoItem item) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (content.getScene() != null && content.getScene().getWindow() != null) {
            stage.initOwner(content.getScene().getWindow());
        }
        stage.setTitle("Cargo Inspection Detail - " + item.id);

        VBox modalContent = new VBox(18);
        modalContent.setPadding(new Insets(25));
        modalContent.setStyle("-fx-background-color:#ffffff;");

        // Title
        Label mTitle = new Label("Cargo Inspection");
        mTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        mTitle.setTextFill(Color.web("#172033"));

        Label mSub = new Label("Detailed cargo specs, customs checklist, and inspection results.");
        mSub.setFont(Font.font("Arial", 13));
        mSub.setTextFill(Color.web("#64748b"));

        VBox mHeader = new VBox(4, mTitle, mSub);

        // Info Grid
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(14));
        infoGrid.setStyle("-fx-background-color:#f8fafc; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10;");

        addInfoRow(infoGrid, 0, "Cargo ID:", item.id, "Cargo Name:", item.name);
        addInfoRow(infoGrid, 1, "Shipping Co:", item.company, "Category:", item.category);
        addInfoRow(infoGrid, 2, "Weight:", item.weight, "Container No:", item.containerNo);
        addInfoRow(infoGrid, 3, "Origin:", item.origin, "Destination:", item.destination);
        addInfoRow(infoGrid, 4, "Inspection Status:", item.inspectionStatus, "Customs Status:", item.customsStatus);

        // Checklist
        Label checkTitle = new Label("Inspection Checklist");
        checkTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        checkTitle.setTextFill(Color.web("#172033"));

        VBox checklistBox = new VBox(8);
        String[] checks = {
                "Cargo details verified",
                "Shipping documents verified",
                "Cargo quantity verified",
                "Weight verified",
                "Container/seal information verified",
                "Cargo condition checked",
                "Supporting documents checked",
                "Additional inspection required"
        };

        for (int i = 0; i < checks.length; i++) {
            CheckBox cb = new CheckBox(checks[i]);
            cb.setSelected(i < 6);
            cb.setFont(Font.font("Arial", 13));
            checklistBox.getChildren().add(cb);
        }

        // Notes
        Label notesTitle = new Label("Inspection Notes");
        notesTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        notesTitle.setTextFill(Color.web("#172033"));

        TextArea notesArea = new TextArea(item.notes);
        notesArea.setPromptText("Enter customs officer inspection observations...");
        notesArea.setPrefHeight(90);
        notesArea.setFont(Font.font("Arial", 13));

        // Result Selector
        Label resultLabel = new Label("Inspection Result:");
        resultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        ComboBox<String> resultCombo = new ComboBox<>();
        resultCombo.getItems().addAll("Approved", "Rejected", "Requires Additional Inspection");
        resultCombo.setValue("Approved".equalsIgnoreCase(item.customsStatus) ? "Approved" :
                            "Rejected".equalsIgnoreCase(item.customsStatus) ? "Rejected" : "Requires Additional Inspection");
        resultCombo.setStyle("-fx-font-size:13px;");

        HBox resultRow = new HBox(12, resultLabel, resultCombo);
        resultRow.setAlignment(Pos.CENTER_LEFT);

        // Buttons
        Button saveBtn = new Button("Save Inspection");
        saveBtn.setStyle("-fx-background-color:#1769ff; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:9 18; -fx-background-radius:7; -fx-cursor:hand;");

        Button approveBtn = new Button("Approve Cargo");
        approveBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:9 18; -fx-background-radius:7; -fx-cursor:hand;");

        Button rejectBtn = new Button("Reject Cargo");
        rejectBtn.setStyle("-fx-background-color:#dc2626; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:9 18; -fx-background-radius:7; -fx-cursor:hand;");

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#475467; -fx-font-weight:bold; -fx-padding:9 18; -fx-background-radius:7; -fx-cursor:hand;");
        closeBtn.setOnAction(ev -> stage.close());

        saveBtn.setOnAction(ev -> {
            item.notes = notesArea.getText();
            String sel = resultCombo.getValue();
            if ("Approved".equalsIgnoreCase(sel)) {
                item.inspectionStatus = "Completed";
                item.customsStatus = "Approved";
            } else if ("Rejected".equalsIgnoreCase(sel)) {
                item.inspectionStatus = "Hold";
                item.customsStatus = "Rejected";
            } else {
                item.inspectionStatus = "Under Inspection";
                item.customsStatus = "Requires Inspection";
            }
            recalculateStats();
            applyFilter();
            stage.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Inspection details saved successfully.");
            alert.showAndWait();
        });

        approveBtn.setOnAction(ev -> {
            stage.close();
            approveCargo(item);
        });

        rejectBtn.setOnAction(ev -> {
            stage.close();
            rejectCargo(item);
        });

        HBox btnRow = new HBox(10, saveBtn, approveBtn, rejectBtn, closeBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        modalContent.getChildren().addAll(mHeader, infoGrid, checkTitle, checklistBox, notesTitle, notesArea, resultRow, btnRow);

        ScrollPane modalScroll = new ScrollPane(modalContent);
        modalScroll.setFitToWidth(true);
        modalScroll.setStyle("-fx-background-color:transparent; -fx-border-color:transparent;");

        Scene scene = new Scene(modalScroll, 600, 680);
        stage.setScene(scene);
        stage.show();
    }

    private void addInfoRow(GridPane grid, int row, String k1, String v1, String k2, String v2) {
        Label lblK1 = new Label(k1);
        lblK1.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblK1.setTextFill(Color.web("#64748b"));

        Label lblV1 = new Label(v1);
        lblV1.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblV1.setTextFill(Color.web("#172033"));

        Label lblK2 = new Label(k2);
        lblK2.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblK2.setTextFill(Color.web("#64748b"));

        Label lblV2 = new Label(v2);
        lblV2.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblV2.setTextFill(Color.web("#172033"));

        grid.add(lblK1, 0, row);
        grid.add(lblV1, 1, row);
        grid.add(lblK2, 2, row);
        grid.add(lblV2, 3, row);
    }

    public VBox getContent() {
        return content;
    }

    public Parent getRoot() {
        return content;
    }
}
