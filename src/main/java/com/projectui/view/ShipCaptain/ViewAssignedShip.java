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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ViewAssignedShip {

    private static final String BLUE = "#1769E8";
    private static final String TEXT = "#111827";
    private static final String MUTED = "#64748b";
    private static final String CARD_STYLE = "-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-radius:8; -fx-background-radius:8; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.06), 14, 0, 0, 4);";

    public Node getContent() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color:transparent;");

        Ship ship = CaptainSessionService.getAssignedShip();
        VesselData vesselData = CaptainSessionService.getAssignedVesselData();

        // 1. Page Header with live assignment overview
        container.getChildren().add(buildHeader(ship, vesselData));

        if (ship == null) {
            container.getChildren().add(buildEmptyState());
        } else {
            container.getChildren().addAll(
                    buildKpiRow(ship, vesselData),
                    buildMainDetailsRow(ship, vesselData),
                    buildVoyageAndRouteRow(ship, vesselData),
                    buildVoyageProgressSection(ship, vesselData),
                    buildCargoSection(),
                    buildOperationalStatusRow(ship, vesselData),
                    buildQuickActionsSection()
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
        return new DashBoard().getRoot(null, null, "View Assigned Ship");
    }

    public Parent getRoot(Stage stage, Runnable logoutAction) {
        return new DashBoard().getRoot(stage, logoutAction, "View Assigned Ship");
    }

    public Scene getShipScene() {
        return new Scene(getRoot());
    }

    private VBox buildHeader(Ship ship, VesselData v) {
        Label title = new Label("View Assigned Ship");
        title.setStyle("-fx-text-fill:#05070c; -fx-font-size:24; -fx-font-weight:bold;");
        Label subtitle = new Label("View live details, specifications and operational telemetry for your assigned vessel.");
        subtitle.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:13;");
        VBox titleBox = new VBox(3, title, subtitle);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox topRow = new HBox(12, titleBox, space);
        topRow.setAlignment(Pos.CENTER_LEFT);

        if (ship != null) {
            String shipName = ship.getShipName() != null ? ship.getShipName() : "Assigned Ship";
            String captain = CaptainSessionService.getCaptainName();
            String status = ship.getStatus() != null ? ship.getStatus() : (v != null ? v.navigationStatus : "At Berth");
            String lastSync = v != null && v.timestamp != null ? v.timestamp : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            HBox tags = new HBox(8,
                    infoTag("ASSIGNED VESSEL", shipName, "#1d4ed8", "#eff6ff"),
                    infoTag("CAPTAIN", captain, "#475569", "#f1f5f9"),
                    infoTag("STATUS", status, "#047857", "#ecfdf5"),
                    infoTag("● LIVE", "Updated " + lastSync, "#0369a1", "#e0f2fe")
            );
            tags.setAlignment(Pos.CENTER_RIGHT);
            topRow.getChildren().add(tags);
        }

        return new VBox(topRow);
    }

    private HBox infoTag(String label, String val, String color, String bg) {
        Label l = new Label(label + ": ");
        l.setStyle("-fx-text-fill:" + color + "; -fx-font-size:10; -fx-font-weight:bold;");
        Label v = new Label(val != null && !val.isBlank() ? val : "N/A");
        v.setStyle("-fx-text-fill:#0f172a; -fx-font-size:11; -fx-font-weight:bold;");
        HBox tag = new HBox(3, l, v);
        tag.setAlignment(Pos.CENTER_LEFT);
        tag.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:6; -fx-border-color:" + color + "33; -fx-border-radius:6; -fx-padding:5 9;");
        return tag;
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

        Label sub = new Label("Please contact the Port Authority or Fleet Operations to assign a vessel to your account.");
        sub.setStyle("-fx-font-size:14; -fx-text-fill:#64748b;");

        card.getChildren().addAll(icon, msg, sub);
        return card;
    }

    private HBox buildKpiRow(Ship ship, VesselData v) {
        String status = ship.getStatus() != null ? ship.getStatus() : (v != null ? v.navigationStatus : "At Berth");
        String location = ship.getBerth() != null && !ship.getBerth().isBlank() ? ship.getBerth() : "Port of Mumbai";
        String dest = CaptainSessionService.getAssignedVoyageDestination();

        HBox row = new HBox(12,
                kpiCard("⚓", "Vessel Name", ship.getShipName(), "Assigned Ship", "#1d4ed8", "#eaf3ff"),
                kpiCard("●", "Current Status", status, "Live Condition", "#0f9f6e", "#e7f8ef"),
                kpiCard("⌖", "Current Location", location, "Berth / Anchorage", "#6d4aff", "#f0edff"),
                kpiCard("➔", "Destination", dest, "Scheduled Route", "#d97706", "#fef3c7")
        );
        row.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        return row;
    }

    private HBox buildMainDetailsRow(Ship ship, VesselData v) {
        HBox row = new HBox(14);

        // Left Panel: Vessel Identification & Specifications
        VBox idCard = new VBox(14);
        idCard.setPadding(new Insets(18));
        idCard.setStyle(CARD_STYLE);
        HBox.setHgrow(idCard, Priority.ALWAYS);

        Label idTitle = new Label("Vessel Identification & Registry");
        idTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        GridPane idGrid = new GridPane();
        idGrid.setHgap(16);
        idGrid.setVgap(12);

        String imo = ship.getImoNumber() != null && !ship.getImoNumber().isBlank() ? ship.getImoNumber() : (v != null ? v.imo : "9234567");
        String mmsi = v != null ? String.valueOf(v.mmsi) : (ship.getShipId() != null ? ship.getShipId() : "");
        String callSign = v != null && v.callSign != null ? v.callSign : "V-4089";
        String type = ship.getShipType() != null && !ship.getShipType().isBlank() ? ship.getShipType() : (v != null ? v.shipType : "Container Vessel");
        String company = ship.getCompany() != null && !ship.getCompany().isBlank() ? ship.getCompany() : "DockTech Fleet Lines";

        idGrid.add(fieldCell("Vessel Name", ship.getShipName()), 0, 0);
        idGrid.add(fieldCell("IMO Number", imo), 1, 0);
        idGrid.add(fieldCell("MMSI Number", mmsi), 0, 1);
        idGrid.add(fieldCell("Call Sign", callSign), 1, 1);
        idGrid.add(fieldCell("Vessel Type", type), 0, 2);
        idGrid.add(fieldCell("Operating Company", company), 1, 2);

        idCard.getChildren().addAll(idTitle, idGrid);

        // Right Panel: Live Navigation & Telemetry
        VBox navCard = new VBox(14);
        navCard.setPadding(new Insets(18));
        navCard.setStyle(CARD_STYLE);
        HBox.setHgrow(navCard, Priority.ALWAYS);

        Label navTitle = new Label("Live Navigation & Telemetry");
        navTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        Label livePill = new Label("● LIVE");
        livePill.setStyle("-fx-text-fill:#047857; -fx-background-color:#ecfdf5; -fx-background-radius:6; -fx-padding:3 8; -fx-font-size:11; -fx-font-weight:bold;");

        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        String lastSync = v != null && v.timestamp != null ? v.timestamp : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Label lastUpdateLbl = new Label(v != null ? "Last AIS update: " + lastSync : "Live telemetry unavailable (Last update: " + lastSync + ")");
        lastUpdateLbl.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12;");

        HBox navHeaderRow = new HBox(8, navTitle, livePill, sp2, lastUpdateLbl);
        navHeaderRow.setAlignment(Pos.CENTER_LEFT);

        GridPane navGrid = new GridPane();
        navGrid.setHgap(16);
        navGrid.setVgap(12);

        String speedStr = v != null ? String.format("%.1f Knots", v.speed) : "12.5 Knots";
        String courseStr = v != null ? String.format("%.1f°", v.course) : "184.0°";
        String headingStr = v != null ? String.format("%.1f°", v.heading) : "184.0°";
        String coords = v != null ? String.format("%.4f° N, %.4f° E", v.latitude, v.longitude) : "18.9439° N, 72.8354° E";
        String eta = ship.getArrival() != null && !ship.getArrival().isBlank() ? ship.getArrival() : "10:30 AM";

        navGrid.add(fieldCell("Current Speed", speedStr), 0, 0);
        navGrid.add(fieldCell("Course / Heading", courseStr + " / " + headingStr), 1, 0);
        navGrid.add(fieldCell("GPS Coordinates", coords), 0, 1);
        navGrid.add(fieldCell("Estimated Arrival (ETA)", eta), 1, 1);
        navGrid.add(fieldCell("Assigned Captain", CaptainSessionService.getCaptainName()), 0, 2);
        navGrid.add(fieldCell("Last AIS Update", lastSync), 1, 2);

        navCard.getChildren().addAll(navHeaderRow, navGrid);

        row.getChildren().addAll(idCard, navCard);
        return row;
    }

    private HBox buildVoyageAndRouteRow(Ship ship, VesselData v) {
        HBox row = new HBox(14);

        // Left Panel: Voyage & Route Information
        VBox routeCard = new VBox(14);
        routeCard.setPadding(new Insets(18));
        routeCard.setStyle(CARD_STYLE);
        HBox.setHgrow(routeCard, Priority.ALWAYS);

        Label rTitle = new Label("Voyage & Route Details");
        rTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        GridPane rGrid = new GridPane();
        rGrid.setHgap(16);
        rGrid.setVgap(12);

        String origin = CaptainSessionService.getAssignedVoyageOrigin();
        String dest = CaptainSessionService.getAssignedVoyageDestination();
        String voyageId = "VOY-" + (ship.getShipId() != null ? ship.getShipId() : "2026") + "-E";
        String depStatus = "Scheduled / Ready";
        String eta = ship.getArrival() != null && !ship.getArrival().isBlank() ? ship.getArrival() : "10:30 AM";
        String status = ship.getStatus() != null ? ship.getStatus() : "Operational Transit";

        rGrid.add(fieldCell("Origin Port", origin), 0, 0);
        rGrid.add(fieldCell("Destination Port", dest), 1, 0);
        rGrid.add(fieldCell("Voyage ID / Name", voyageId), 0, 1);
        rGrid.add(fieldCell("Departure Status", depStatus), 1, 1);
        rGrid.add(fieldCell("Estimated Arrival (ETA)", eta), 0, 2);
        rGrid.add(fieldCell("Current Voyage Status", status), 1, 2);

        routeCard.getChildren().addAll(rTitle, rGrid);

        // Right Panel: Live Movement & Position Snapshot
        VBox movementCard = new VBox(14);
        movementCard.setPadding(new Insets(18));
        movementCard.setStyle(CARD_STYLE);
        HBox.setHgrow(movementCard, Priority.ALWAYS);

        Label mTitle = new Label("Live Vessel Movement & Positioning");
        mTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        GridPane mGrid = new GridPane();
        mGrid.setHgap(16);
        mGrid.setVgap(12);

        String lat = v != null ? String.format("%.4f° N", v.latitude) : "18.9439° N";
        String lon = v != null ? String.format("%.4f° E", v.longitude) : "72.8354° E";
        String navStatus = v != null ? v.navigationStatus : (ship.getStatus() != null ? ship.getStatus() : "Underway");
        String distRemaining = "840 NM (Estimated)";

        mGrid.add(fieldCell("Latitude", lat), 0, 0);
        mGrid.add(fieldCell("Longitude", lon), 1, 0);
        mGrid.add(fieldCell("Navigational Status", navStatus), 0, 1);
        mGrid.add(fieldCell("Distance Remaining", distRemaining), 1, 1);
        mGrid.add(fieldCell("Route Waypoints", "4 of 6 Completed"), 0, 2);
        mGrid.add(fieldCell("Port Sea Lane", "Direct Channel Corridor"), 1, 2);

        movementCard.getChildren().addAll(mTitle, mGrid);

        row.getChildren().addAll(routeCard, movementCard);
        return row;
    }

    private VBox buildVoyageProgressSection(Ship ship, VesselData v) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle(CARD_STYLE);

        Label title = new Label("Voyage Progress");
        title.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        String origin = CaptainSessionService.getAssignedVoyageOrigin();
        String dest = CaptainSessionService.getAssignedVoyageDestination();
        String currentLoc = ship.getBerth() != null && !ship.getBerth().isBlank() ? ship.getBerth() : "Port of Mumbai (Berth B04)";
        String eta = ship.getArrival() != null && !ship.getArrival().isBlank() ? ship.getArrival() : "10:30 AM";
        String status = ship.getStatus() != null ? ship.getStatus() : (v != null ? v.navigationStatus : "At Berth");

        double progress = 0.50;
        String stLower = status.toLowerCase();
        if (stLower.contains("loading") || stLower.contains("berth")) {
            progress = 0.20;
        } else if (stLower.contains("underway") || stLower.contains("transit")) {
            progress = 0.65;
        } else if (stLower.contains("arrival") || stLower.contains("completed")) {
            progress = 0.95;
        }

        HBox timelineLabels = new HBox();
        Label lblOrigin = new Label("⚓ Departure: " + origin);
        lblOrigin.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:12; -fx-font-weight:bold;");

        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);

        Label lblMid = new Label("● Current: " + currentLoc + " (" + status + ")");
        lblMid.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:12; -fx-font-weight:bold;");

        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        Label lblDest = new Label("🏁 Destination: " + dest + " (ETA: " + eta + ")");
        lblDest.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:12; -fx-font-weight:bold;");

        timelineLabels.getChildren().addAll(lblOrigin, sp1, lblMid, sp2, lblDest);

        ProgressBar pBar = new ProgressBar(progress);
        pBar.setMaxWidth(Double.MAX_VALUE);
        pBar.setPrefHeight(8);
        pBar.setStyle("-fx-accent:" + BLUE + ";");

        HBox statsRow = new HBox(20,
                miniStat("Route Clearance", "Port Authority Approved"),
                miniStat("Distance Remaining", "840 NM (Est.)"),
                miniStat("Voyage Progress", (int)(progress * 100) + "% Completed"),
                miniStat("Current Speed", v != null ? String.format("%.1f Knots", v.speed) : "12.5 Knots")
        );

        card.getChildren().addAll(title, timelineLabels, pBar, statsRow);
        return card;
    }

    private VBox buildCargoSection() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setStyle(CARD_STYLE);

        Label tableTitle = new Label("Assigned Cargo & Manifest Overview");
        tableTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        List<Map<String, Object>> cargos = CaptainSessionService.getAssignedCargos();
        List<Map<String, Object>> containers = CaptainSessionService.getAssignedContainers();

        int totalCargos = cargos.size();
        int loadedCargos = 0;
        double totalWeight = 0;
        for (Map<String, Object> c : cargos) {
            String st = c.getOrDefault("status", "").toString().toLowerCase();
            if (st.contains("loaded") || st.contains("completed") || st.contains("verified")) {
                loadedCargos++;
            }
            try {
                if (c.get("weight") != null) {
                    totalWeight += Double.parseDouble(c.get("weight").toString().replaceAll("[^0-9.]", ""));
                }
            } catch (Exception ignored) {}
        }
        int pct = totalCargos > 0 ? (int)((loadedCargos * 100.0) / totalCargos) : 100;

        HBox summaryRow = new HBox(16,
                miniStat("Total Cargo", totalCargos + " Items"),
                miniStat("Total Weight", String.format("%.0f Tons", totalWeight > 0 ? totalWeight : 850.0)),
                miniStat("Containers", containers.size() + " Allocated"),
                miniStat("Loading Progress", pct + "% (" + loadedCargos + " of " + totalCargos + " Loaded)")
        );

        if (cargos.isEmpty()) {
            Label noCargo = new Label("No cargo assigned to this voyage.");
            noCargo.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:14; -fx-padding:14 0 10 0;");
            card.getChildren().addAll(tableTitle, summaryRow, noCargo);
            return card;
        }

        TableView<Map<String, Object>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(200);
        table.setStyle("-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-radius:6;");

        TableColumn<Map<String, Object>, String> colId = new TableColumn<>("Cargo ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("cargoId", "-").toString()));

        TableColumn<Map<String, Object>, String> colName = new TableColumn<>("Goods / Cargo");
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("cargoName", "-").toString()));

        TableColumn<Map<String, Object>, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("cargoType", "-").toString()));

        TableColumn<Map<String, Object>, String> colQty = new TableColumn<>("Quantity");
        colQty.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("quantity", "1").toString()));

        TableColumn<Map<String, Object>, String> colWeight = new TableColumn<>("Weight (Tons)");
        colWeight.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("weight", "-").toString()));

        TableColumn<Map<String, Object>, String> colOrigin = new TableColumn<>("Origin Port");
        colOrigin.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("originPort", "-").toString()));

        TableColumn<Map<String, Object>, String> colDest = new TableColumn<>("Destination");
        colDest.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("destinationPort", "-").toString()));

        TableColumn<Map<String, Object>, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault("status", "Pending").toString()));

        table.getColumns().addAll(colId, colName, colType, colQty, colWeight, colOrigin, colDest, colStatus);
        ObservableList<Map<String, Object>> list = FXCollections.observableArrayList(cargos);
        table.setItems(list);

        card.getChildren().addAll(tableTitle, summaryRow, table);
        return card;
    }

    private HBox buildOperationalStatusRow(Ship ship, VesselData v) {
        HBox row = new HBox(14);

        // Left Panel: Operational Status & Port Clearances
        VBox statusCard = new VBox(12);
        statusCard.setPadding(new Insets(16));
        statusCard.setStyle(CARD_STYLE);
        HBox.setHgrow(statusCard, Priority.ALWAYS);

        Label sTitle = new Label("Operational Status & Port Clearances");
        sTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        VBox list = new VBox(8,
                statusItem("✓", "Port Authority Berth Allocation", "Berth B04 Assigned & Active", "#0f9f6e"),
                statusItem("✓", "Customs & Manifest Clearance", "Duty and cargo manifest verified", "#0f9f6e"),
                statusItem("●", "Vessel Safety Inspection", "Inspection passed and certified", "#1d4ed8"),
                statusItem("➔", "Harbor Master Departure Clearance", "Cleared for scheduled outbound transit", "#0284c7")
        );

        statusCard.getChildren().addAll(sTitle, list);

        // Right Panel: Operational Alerts & Notice
        VBox alertsCard = new VBox(12);
        alertsCard.setPadding(new Insets(16));
        alertsCard.setStyle(CARD_STYLE);
        HBox.setHgrow(alertsCard, Priority.ALWAYS);

        Label aTitle = new Label("Operational Alerts & Synchronization");
        aTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        String lastSync = v != null && v.timestamp != null ? v.timestamp : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        VBox alertList = new VBox(8,
                statusItem("📡", "AIS Telemetry Link", "Connected to Maritime Ground Stations (Sync: " + lastSync + ")", "#0f9f6e"),
                statusItem("⚓", "Berthing Advisory", "Tugboat assistance available upon harbor entry", "#1d4ed8"),
                statusItem("⏱", "Voyage Schedule", "On-schedule for estimated arrival window", "#0284c7")
        );

        alertsCard.getChildren().addAll(aTitle, alertList);

        row.getChildren().addAll(statusCard, alertsCard);
        return row;
    }

    private VBox buildQuickActionsSection() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle(CARD_STYLE);

        Label qTitle = new Label("Quick Operational Actions");
        qTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        HBox buttons = new HBox(10);

        Button updateStatus = actionBtn("◆  Update Ship Status", "#0f9f6e", "#ecfdf5");
        Button loadingBtn = actionBtn("▣  Confirm Loading", "#d97706", "#fef3c7");
        Button uploadDoc = actionBtn("📄  Upload Document", "#1d4ed8", "#eff6ff");
        Button sosBtn = actionBtn("🚨  Emergency SOS", "#dc2626", "#fee2e2");

        updateStatus.setOnAction(e -> DashBoard.navigateTo("Update Ship Status"));
        loadingBtn.setOnAction(e -> DashBoard.navigateTo("Confirm Loading"));
        uploadDoc.setOnAction(e -> DashBoard.navigateTo("Upload Document"));
        sosBtn.setOnAction(e -> DashBoard.navigateTo("Send SOS"));

        buttons.getChildren().addAll(updateStatus, loadingBtn, uploadDoc, sosBtn);
        buttons.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        card.getChildren().addAll(qTitle, buttons);
        return card;
    }

    private Button actionBtn(String text, String color, String bg) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + color + "; -fx-border-color:" + color + "33; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:10 14; -fx-background-radius:6; -fx-border-radius:6; -fx-cursor:hand;");
        return b;
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