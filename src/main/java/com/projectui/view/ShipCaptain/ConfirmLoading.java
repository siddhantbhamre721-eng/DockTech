package com.projectui.view.ShipCaptain;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.projectui.model.Ship;
import com.projectui.service.VesselData;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ConfirmLoading {

    private static final String BLUE = "#1769E8";
    private static final String TEXT = "#111827";
    private static final String MUTED = "#64748b";
    private static final String CARD_STYLE = "-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-radius:8; -fx-background-radius:8; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.06), 14, 0, 0, 4);";

    private TableView<Map<String, Object>> cargoTable;
    private Label selectedCargoLabel = new Label("Select a Cargo Item");
    private Label selectedDetailsLabel = new Label("Click a table row to inspect and verify.");
    private Map<String, Object> selectedCargo = null;

    public Node getContent() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color:transparent;");

        // Header
        Label title = new Label("Confirm Loading");
        title.setStyle("-fx-text-fill:#05070c; -fx-font-size:24; -fx-font-weight:bold;");
        Label subtitle = new Label("Cargo Verification, Manifest Inspection & Vessel Loading Readiness System.");
        subtitle.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:13;");
        VBox titleBox = new VBox(3, title, subtitle);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox headerRow = new HBox(12, titleBox, space);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(headerRow);

        Ship ship = CaptainSessionService.getAssignedShip();
        VesselData v = CaptainSessionService.getAssignedVesselData();

        if (ship == null) {
            container.getChildren().add(buildEmptyState());
        } else {
            List<Map<String, Object>> cargos = CaptainSessionService.getAssignedCargos();
            List<Map<String, Object>> containers = CaptainSessionService.getAssignedContainers();

            container.getChildren().addAll(
                    buildKpiRow(cargos, containers, ship),
                    buildMainSection(cargos, containers, ship),
                    buildLoadingProgressAndStatusRow(cargos, containers, ship, v),
                    buildVerificationAndReadinessRow(cargos, containers, ship)
            );
        }

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color:#f5f8fc; -fx-background:transparent; -fx-border-color:transparent;");
        return scroll;
    }

    public Parent getRoot() {
        return new DashBoard().getRoot(null, null, "Confirm Loading");
    }

    public Parent getRoot(Stage stage, Runnable logoutAction) {
        return new DashBoard().getRoot(stage, logoutAction, "Confirm Loading");
    }

    public Scene getScene() {
        return new Scene(getRoot(), 1280, 800);
    }

    private VBox buildEmptyState() {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(60, 30, 60, 30));
        card.setStyle(CARD_STYLE);

        Label icon = new Label("⚓");
        icon.setStyle("-fx-font-size:44; -fx-text-fill:#94a3b8;");

        Label msg = new Label("No Vessel Assigned");
        msg.setStyle("-fx-font-size:18; -fx-font-weight:bold; -fx-text-fill:#334155;");

        Label sub = new Label("You must have an assigned vessel to review and confirm cargo loading.");
        sub.setStyle("-fx-font-size:14; -fx-text-fill:#64748b;");

        card.getChildren().addAll(icon, msg, sub);
        return card;
    }

    private HBox buildKpiRow(List<Map<String, Object>> cargos, List<Map<String, Object>> containers, Ship ship) {
        int totalCargos = cargos.size();
        int loadedCount = 0;
        double totalWeight = 0;
        for (Map<String, Object> c : cargos) {
            String st = c.getOrDefault("status", "").toString().toLowerCase();
            if (st.contains("loaded") || st.contains("completed") || st.contains("verified")) {
                loadedCount++;
            }
            try {
                if (c.get("weight") != null) {
                    totalWeight += Double.parseDouble(c.get("weight").toString().replaceAll("[^0-9.]", ""));
                }
            } catch (Exception ignored) {}
        }
        int pending = totalCargos - loadedCount;

        String shipType = ship.getShipType() != null ? ship.getShipType() : "Vessel";
        boolean isTanker = shipType.toLowerCase().contains("tanker");
        String fourthCardTitle = isTanker ? "Liquid Tonnage (Est.)" : "Allocated Containers";
        String fourthCardVal = isTanker ? String.format("%.0f Tons", totalWeight > 0 ? totalWeight : 1250.0) : String.valueOf(containers.size());
        String fourthCardSub = isTanker ? "Bulk Compartment Load" : "Allocated Cargo Units";

        HBox row = new HBox(12,
                kpiCard("▣", "Assigned Cargos", String.valueOf(totalCargos), "Total Manifest Items", "#1d4ed8", "#eaf3ff"),
                kpiCard("✓", "Loaded & Verified", String.valueOf(loadedCount), "Completed Items", "#0f9f6e", "#e7f8ef"),
                kpiCard("◷", "Pending Loading", String.valueOf(pending >= 0 ? pending : 0), "Awaiting Confirmation", "#d97706", "#fef3c7"),
                kpiCard(isTanker ? "🛢" : "⬟", fourthCardTitle, fourthCardVal, fourthCardSub, "#6d4aff", "#f0edff")
        );
        row.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        return row;
    }

    private HBox buildMainSection(List<Map<String, Object>> cargos, List<Map<String, Object>> containers, Ship ship) {
        HBox row = new HBox(14);

        // Left Table: Live Assigned Cargos
        VBox tableCard = new VBox(12);
        tableCard.setPadding(new Insets(18));
        tableCard.setStyle(CARD_STYLE);
        HBox.setHgrow(tableCard, Priority.ALWAYS);

        Label tableTitle = new Label("Live Cargo & Container Loading Manifest");
        tableTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        if (cargos.isEmpty()) {
            Label noCargo = new Label("No cargo assigned to this voyage.");
            noCargo.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:14; -fx-padding:30 0;");
            tableCard.getChildren().addAll(tableTitle, noCargo);
        } else {
            cargoTable = new TableView<>();
            cargoTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            cargoTable.setPrefHeight(260);
            cargoTable.setStyle("-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-radius:6;");

            TableColumn<Map<String, Object>, String> colId = new TableColumn<>("Cargo ID");
            colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("cargoId", "-").toString()));

            TableColumn<Map<String, Object>, String> colName = new TableColumn<>("Goods / Description");
            colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("cargoName", "-").toString()));

            TableColumn<Map<String, Object>, String> colType = new TableColumn<>("Cargo Type");
            colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("cargoType", "-").toString()));

            TableColumn<Map<String, Object>, String> colQty = new TableColumn<>("Quantity");
            colQty.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("quantity", "1").toString()));

            TableColumn<Map<String, Object>, String> colWeight = new TableColumn<>("Weight (Tons)");
            colWeight.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("weight", "-").toString()));

            TableColumn<Map<String, Object>, String> colOrigin = new TableColumn<>("Origin");
            colOrigin.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("originPort", "—").toString()));

            TableColumn<Map<String, Object>, String> colDest = new TableColumn<>("Destination");
            colDest.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("destinationPort", "—").toString()));

            TableColumn<Map<String, Object>, String> colStatus = new TableColumn<>("Loading Status");
            colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("status", "Pending").toString()));

            TableColumn<Map<String, Object>, String> colCustoms = new TableColumn<>("Customs Clearance");
            colCustoms.setCellValueFactory(data -> new SimpleStringProperty(
                    data.getValue().getOrDefault("customsClearanceStatus",
                            data.getValue().getOrDefault("customsStatus", "Pending Clearance")).toString()
            ));
            colCustoms.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        Label badge = new Label(item);
                        boolean isCleared = "Cleared".equalsIgnoreCase(item) || "Approved".equalsIgnoreCase(item) || "Passed".equalsIgnoreCase(item) || "Customs Cleared".equalsIgnoreCase(item);
                        boolean isHeld = "Held".equalsIgnoreCase(item) || "Rejected".equalsIgnoreCase(item) || "Flagged".equalsIgnoreCase(item) || "Customs Held".equalsIgnoreCase(item);
                        String color = isCleared ? "#16a34a" : (isHeld ? "#ef4444" : "#d97706");
                        String bg = isCleared ? "#dcfce7" : (isHeld ? "#fee2e2" : "#fef3c7");
                        badge.setStyle("-fx-text-fill:" + color + "; -fx-background-color:" + bg + "; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;");
                        setGraphic(badge);
                        setAlignment(Pos.CENTER);
                        setText(null);
                    }
                }
            });

            cargoTable.getColumns().addAll(colId, colName, colType, colQty, colWeight, colOrigin, colDest, colCustoms, colStatus);
            ObservableList<Map<String, Object>> list = FXCollections.observableArrayList(cargos);
            cargoTable.setItems(list);

            cargoTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV != null) {
                    selectedCargo = newV;
                    updateActionPanel(newV);
                }
            });

            if (!cargos.isEmpty()) {
                selectedCargo = cargos.get(0);
                cargoTable.getSelectionModel().select(0);
            }

            tableCard.getChildren().addAll(tableTitle, cargoTable);
        }

        // Right Card: Captain Confirmation Action Panel
        VBox actionCard = new VBox(12);
        actionCard.setPrefWidth(380);
        actionCard.setMinWidth(360);
        actionCard.setPadding(new Insets(18));
        actionCard.setStyle(CARD_STYLE);

        Label actionTitle = new Label("Verify & Confirm Cargo");
        actionTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        selectedCargoLabel.setText(selectedCargo != null ? "Selected: " + selectedCargo.getOrDefault("cargoId", "") : "Select a Cargo Item");
        selectedCargoLabel.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:14; -fx-font-weight:bold;");

        selectedDetailsLabel.setText(selectedCargo != null ? selectedCargo.getOrDefault("cargoName", "") + " • " + selectedCargo.getOrDefault("weight", "") + " Tons" : "Click a table row to inspect and verify.");
        selectedDetailsLabel.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12;");

        VBox selectInfoBox = new VBox(3, selectedCargoLabel, selectedDetailsLabel);
        selectInfoBox.setPadding(new Insets(8));
        selectInfoBox.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:6;");

        Label checkTitle = new Label("Readiness Checklist");
        checkTitle.setStyle("-fx-text-fill:#1e293b; -fx-font-size:12; -fx-font-weight:bold;");

        CheckBox cb1 = new CheckBox("Cargo manifest & quantity verified");
        cb1.setSelected(true);
        cb1.setStyle("-fx-font-size:12;");

        CheckBox cb2 = new CheckBox("Compartment / container secured");
        cb2.setSelected(true);
        cb2.setStyle("-fx-font-size:12;");

        CheckBox cb3 = new CheckBox("Berth & safety clearance confirmed");
        cb3.setSelected(true);
        cb3.setStyle("-fx-font-size:12;");

        Label statusTitle = new Label("Confirm Loading Condition");
        statusTitle.setStyle("-fx-text-fill:#1e293b; -fx-font-size:12; -fx-font-weight:bold;");

        ComboBox<String> loadStatusCombo = new ComboBox<>();
        loadStatusCombo.getItems().addAll("Loading In Progress", "Loading Completed", "Verified & Ready");
        loadStatusCombo.setValue("Loading Completed");
        loadStatusCombo.setMaxWidth(Double.MAX_VALUE);
        loadStatusCombo.setStyle("-fx-background-color:white; -fx-border-color:#d1d5db; -fx-padding:7 12; -fx-font-size:13; -fx-background-radius:6;");

        TextArea remarks = new TextArea();
        remarks.setPromptText("Enter optional captain confirmation notes...");
        remarks.setPrefRowCount(2);
        remarks.setStyle("-fx-background-color:white; -fx-border-color:#d1d5db; -fx-padding:6; -fx-font-size:12; -fx-background-radius:6;");

        Label alert = new Label();
        alert.setVisible(false);
        alert.setManaged(false);
        alert.setMaxWidth(Double.MAX_VALUE);
        alert.setPadding(new Insets(8, 12, 8, 12));

        Button confirmBtn = new Button("Confirm Cargo Loading");
        confirmBtn.setStyle("-fx-background-color:" + BLUE + "; -fx-text-fill:white; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:10 18; -fx-background-radius:6; -fx-cursor:hand;");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);

        confirmBtn.setOnAction(e -> {
            String cId = selectedCargo != null ? (String) selectedCargo.get("cargoId") : null;
            String st = loadStatusCombo.getValue();
            String rem = remarks.getText().trim();
            String cClearance = selectedCargo != null ? String.valueOf(selectedCargo.getOrDefault("customsClearanceStatus", selectedCargo.getOrDefault("customsStatus", "Pending Clearance"))) : "Pending Clearance";

            boolean isCustomsCleared = "Cleared".equalsIgnoreCase(cClearance) || "Approved".equalsIgnoreCase(cClearance) || "Passed".equalsIgnoreCase(cClearance) || "Customs Cleared".equalsIgnoreCase(cClearance);
            boolean isCustomsHeld = "Held".equalsIgnoreCase(cClearance) || "Rejected".equalsIgnoreCase(cClearance) || "Flagged".equalsIgnoreCase(cClearance) || "Customs Held".equalsIgnoreCase(cClearance);

            if (isCustomsHeld) {
                alert.setText("⛔ Loading Blocked: Cargo " + (cId != null ? cId : "") + " is Held/Rejected by Customs. Clearance required.");
                alert.setStyle("-fx-background-color:#fef2f2; -fx-text-fill:#991b1b; -fx-border-color:#fecaca; -fx-border-radius:6; -fx-font-weight:bold; -fx-font-size:12;");
                alert.setVisible(true);
                alert.setManaged(true);
                return;
            }

            boolean ok = CaptainSessionService.confirmLoading(cId, st, rem);
            if (ok) {
                if (!isCustomsCleared) {
                    alert.setText("✓ Loading updated (" + st + "). ⚠️ Notice: Departure blocked until Customs Officer clears shipment.");
                    alert.setStyle("-fx-background-color:#fffbeb; -fx-text-fill:#92400e; -fx-border-color:#fde68a; -fx-border-radius:6; -fx-font-weight:bold; -fx-font-size:12;");
                } else {
                    alert.setText("✓ Loading confirmed & Customs Cleared for " + (cId != null ? cId : "vessel") + " (" + st + "). Ready for departure.");
                    alert.setStyle("-fx-background-color:#ecfdf5; -fx-text-fill:#065f46; -fx-border-color:#a7f3d0; -fx-border-radius:6; -fx-font-weight:bold; -fx-font-size:12;");
                }
                alert.setVisible(true);
                alert.setManaged(true);
            }
        });

        actionCard.getChildren().addAll(
                actionTitle,
                selectInfoBox,
                checkTitle,
                cb1, cb2, cb3,
                statusTitle,
                loadStatusCombo,
                remarks,
                alert,
                confirmBtn
        );

        row.getChildren().addAll(tableCard, actionCard);
        return row;
    }

    private HBox buildLoadingProgressAndStatusRow(List<Map<String, Object>> cargos, List<Map<String, Object>> containers, Ship ship, VesselData v) {
        HBox row = new HBox(14);

        int totalCargos = cargos.size();
        int loadedCount = 0;
        double totalWeight = 0;
        for (Map<String, Object> c : cargos) {
            String st = c.getOrDefault("status", "").toString().toLowerCase();
            if (st.contains("loaded") || st.contains("completed") || st.contains("verified")) {
                loadedCount++;
            }
            try {
                if (c.get("weight") != null) {
                    totalWeight += Double.parseDouble(c.get("weight").toString().replaceAll("[^0-9.]", ""));
                }
            } catch (Exception ignored) {}
        }
        int pending = totalCargos - loadedCount;
        double pct = totalCargos > 0 ? ((double) loadedCount / totalCargos) : 1.0;

        // Left Card: Live Loading Progress
        VBox progressCard = new VBox(12);
        progressCard.setPadding(new Insets(16));
        progressCard.setStyle(CARD_STYLE);
        HBox.setHgrow(progressCard, Priority.ALWAYS);

        Label pTitle = new Label("Live Loading Progress & Capacity");
        pTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        HBox pTop = new HBox();
        Label pPct = new Label("Loading Progress: " + (int)(pct * 100) + "%");
        pPct.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:14; -fx-font-weight:bold;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label pLoaded = new Label(loadedCount + " of " + totalCargos + " Cargos Secured");
        pLoaded.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12;");
        pTop.getChildren().addAll(pPct, sp, pLoaded);

        ProgressBar bar = new ProgressBar(pct);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(8);
        bar.setStyle("-fx-accent:" + BLUE + ";");

        HBox pStats = new HBox(16,
                miniStat("Loaded Cargo", loadedCount + " Items"),
                miniStat("Pending Cargo", (pending >= 0 ? pending : 0) + " Items"),
                miniStat("Total Weight", String.format("%.0f Tons", totalWeight > 0 ? totalWeight : 850.0)),
                miniStat("Remaining Tonnage", String.format("%.0f Tons", totalWeight > 0 && totalCargos > 0 ? (totalWeight * (1 - pct)) : 0.0))
        );

        progressCard.getChildren().addAll(pTitle, pTop, bar, pStats);

        // Right Card: Vessel Loading Status (Vessel-Type Aware)
        VBox vesselCard = new VBox(12);
        vesselCard.setPadding(new Insets(16));
        vesselCard.setStyle(CARD_STYLE);
        HBox.setHgrow(vesselCard, Priority.ALWAYS);

        Label vTitle = new Label("Vessel Loading Status");
        vTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        GridPane vGrid = new GridPane();
        vGrid.setHgap(16);
        vGrid.setVgap(10);

        String shipType = ship.getShipType() != null ? ship.getShipType() : "Container Vessel";
        String berth = ship.getBerth() != null && !ship.getBerth().isBlank() ? ship.getBerth() : "Berth B04";
        String loadState = loadedCount == totalCargos && totalCargos > 0 ? "Loading Completed" : "Loading In Progress (" + (int)(pct * 100) + "%)";
        String lastUpdate = v != null && v.timestamp != null ? v.timestamp : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String captain = CaptainSessionService.getCaptainName();

        vGrid.add(fieldCell("Assigned Vessel", ship.getShipName()), 0, 0);
        vGrid.add(fieldCell("Vessel Type", shipType), 1, 0);
        vGrid.add(fieldCell("Current Port/Berth", berth), 0, 1);
        vGrid.add(fieldCell("Loading Condition", loadState), 1, 1);
        vGrid.add(fieldCell("Captain in Command", captain), 0, 2);
        vGrid.add(fieldCell("Last Loading Update", lastUpdate), 1, 2);

        vesselCard.getChildren().addAll(vTitle, vGrid);

        row.getChildren().addAll(progressCard, vesselCard);
        return row;
    }

    private HBox buildVerificationAndReadinessRow(List<Map<String, Object>> cargos, List<Map<String, Object>> containers, Ship ship) {
        HBox row = new HBox(14);

        int totalCargos = cargos.size();
        int loadedCount = 0;
        for (Map<String, Object> c : cargos) {
            String st = c.getOrDefault("status", "").toString().toLowerCase();
            if (st.contains("loaded") || st.contains("completed") || st.contains("verified")) {
                loadedCount++;
            }
        }
        int pending = totalCargos - loadedCount;

        // Left Card: Cargo Verification Summary
        VBox verCard = new VBox(12);
        verCard.setPadding(new Insets(16));
        verCard.setStyle(CARD_STYLE);
        HBox.setHgrow(verCard, Priority.ALWAYS);

        Label verTitle = new Label("Cargo & Container Verification Summary");
        verTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        String shipType = ship.getShipType() != null ? ship.getShipType() : "Vessel";
        boolean isTanker = shipType.toLowerCase().contains("tanker");

        VBox verList = new VBox(8,
                statusItem("▣", "Total Assigned Manifest Items", totalCargos + " Items Registered", "#1d4ed8"),
                statusItem("✓", "Verified & Confirmed Cargo", loadedCount + " of " + totalCargos + " Verified", "#0f9f6e"),
                statusItem("◷", "Pending Captain Verification", (pending >= 0 ? pending : 0) + " Items Awaiting Inspection", "#d97706"),
                statusItem(isTanker ? "🛢" : "⬟", isTanker ? "Tank Compartment Allocation" : "Allocated Container Units", isTanker ? "Liquid Compartments Secured" : containers.size() + " Containers Assigned", "#6d4aff")
        );

        verCard.getChildren().addAll(verTitle, verList);

        // Right Card: Loading Readiness Checklist
        VBox readyCard = new VBox(12);
        readyCard.setPadding(new Insets(16));
        readyCard.setStyle(CARD_STYLE);
        HBox.setHgrow(readyCard, Priority.ALWAYS);

        Label rTitle = new Label("Loading Readiness & Operational Clearance");
        rTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        VBox rList = new VBox(8,
                statusItem("✓", "Cargo Assigned & Logged", totalCargos > 0 ? "Manifest linked to vessel voyage" : "Awaiting voyage manifest", totalCargos > 0 ? "#0f9f6e" : "#64748b"),
                statusItem("✓", "Port & Customs Manifest Clearance", "Duty and cargo declaration approved", "#0f9f6e"),
                statusItem("✓", "Berth Allocation & Crane Operation", "Assigned berth operational for loading", "#0f9f6e"),
                statusItem("✓", "Vessel Safety & Loading Confirmation", "Vessel certified and ready for outbound transit", "#0f9f6e")
        );

        readyCard.getChildren().addAll(rTitle, rList);

        row.getChildren().addAll(verCard, readyCard);
        return row;
    }

    private void updateActionPanel(Map<String, Object> cargo) {
        if (cargo == null || selectedCargoLabel == null || selectedDetailsLabel == null) return;
        String id = cargo.getOrDefault("cargoId", "").toString();
        String name = cargo.getOrDefault("cargoName", "").toString();
        String weight = cargo.getOrDefault("weight", "").toString();
        String type = cargo.getOrDefault("cargoType", "").toString();
        String orig = cargo.getOrDefault("originPort", "—").toString();
        String dest = cargo.getOrDefault("destinationPort", "—").toString();
        String cClearance = cargo.getOrDefault("customsClearanceStatus", cargo.getOrDefault("customsStatus", "Pending Clearance")).toString();
        String cOfficer = cargo.getOrDefault("assignedCustomsOfficerName", cargo.getOrDefault("assignedCustomsOfficer", "")).toString();
        String custInfo = " • Customs: " + cClearance + (!cOfficer.isBlank() ? " (" + cOfficer + ")" : "");

        selectedCargoLabel.setText("Selected: " + id);
        selectedDetailsLabel.setText(name + " • " + weight + " Tons (" + type + ") • " + orig + " ➔ " + dest + custInfo);
    }

    private HBox statusItem(String icon, String title, String sub, String color) {
        Label i = new Label(icon);
        i.setStyle("-fx-text-fill:" + color + "; -fx-font-weight:bold; -fx-font-size:13;");
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-weight:bold; -fx-font-size:13;");
        Label s = new Label(sub);
        s.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12;");
        VBox tb = new VBox(1, t, s);

        HBox item = new HBox(8, i, tb);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    private VBox miniStat(String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:11; -fx-font-weight:bold;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:14; -fx-font-weight:bold;");
        return new VBox(2, l, v);
    }

    private VBox fieldCell(String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12; -fx-font-weight:bold;");
        Label v = new Label(value != null && !value.isBlank() ? value : "N/A");
        v.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:14; -fx-font-weight:bold;");
        return new VBox(3, l, v);
    }

    private VBox kpiCard(String icon, String title, String value, String sub, String accentColor, String bgColor) {
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size:16; -fx-text-fill:" + accentColor + "; -fx-font-weight:bold; -fx-background-color:" + bgColor + "; -fx-background-radius:6; -fx-padding:6 10;");

        Label t = new Label(title);
        t.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:13; -fx-font-weight:bold;");

        HBox top = new HBox(8, iconLbl, t);
        top.setAlignment(Pos.CENTER_LEFT);

        Label v = new Label(value != null ? value : "-");
        v.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:20; -fx-font-weight:bold;");

        Label s = new Label(sub);
        s.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12;");

        VBox card = new VBox(6, top, v, s);
        card.setPadding(new Insets(16));
        card.setStyle(CARD_STYLE);
        return card;
    }
}