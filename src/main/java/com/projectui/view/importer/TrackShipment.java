package com.projectui.view.importer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.projectui.dao.CargoDao;
import com.projectui.dao.ContainerDao;
import com.projectui.model.Ship;
import com.projectui.service.VesselData;
import com.projectui.service.VesselManager;
import com.projectui.view.login.DashboardLayoutHelper;
import com.projectui.view.login.LoginPage;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

@SuppressWarnings({"unused", "unchecked"})
public class TrackShipment {

    private final CargoDao cargoDao = new CargoDao();
    private final ContainerDao containerDao = new ContainerDao();
    private final VesselManager vesselManager = VesselManager.getInstance();

    private final List<Map<String, Object>> userShipments = new ArrayList<>();
    private Map<String, Object> selectedShipment = null;

    private VBox shipmentSelectionList;
    private VBox trackingDetailsContainer;
    private TextField searchField;
    private ComboBox<String> statusFilterCombo;
    private Label syncStatusBadge;

    private static final String CARD_STYLE = "-fx-background-color:white; -fx-background-radius:12px; -fx-border-color:#e2e8f0; -fx-border-radius:12px; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 10, 0, 0, 3);";

    public Scene getTrackShipmentScene(Runnable back) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#F4F8FC;");
        root.setTop(createHeader("Track Shipment", "Dock Tech / Importer / Track Shipment"));
        root.setLeft(createSidebar(back));

        VBox content = buildMainContent(back);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color:#F4F8FC; -fx-background: transparent; -fx-border-color:transparent;");

        root.setCenter(scrollPane);

        // Reactive Listeners
        CargoDao.addChangeListener(this::refreshData);
        ContainerDao.addChangeListener(this::refreshData);

        refreshData();

        Scene sc = new Scene(root, 1400, 800);
        LoginPage.applyInvisibleScrollbar(sc);
        return sc;
    }

    private VBox buildMainContent(Runnable back) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20, 24, 24, 24));
        container.setStyle("-fx-background-color:#F4F8FC;");

        // 1. Title & Telemetry Status Header
        HBox headerRow = buildHeaderBanner();

        // 2. Main Two-Column Tracking Console (Shipment Selector + Live Telemetry & Consignment Dossier)
        HBox consoleRow = buildConsoleRow();

        container.getChildren().addAll(headerRow, consoleRow);
        return container;
    }

    private HBox buildHeaderBanner() {
        VBox titleBox = new VBox(4);
        Text title = new Text("Live Shipment & Vessel Tracking");
        title.setStyle("-fx-font-size:26px; -fx-font-weight:bold; -fx-fill:#101A2B;");

        String importerName = getLoggedImporterName();
        Text subtitle = new Text("Real-time consignment status and live maritime vessel telemetry for " + importerName + ".");
        subtitle.setStyle("-fx-font-size:13px; -fx-fill:#64748B;");
        titleBox.getChildren().addAll(title, subtitle);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        syncStatusBadge = new Label("● AIS Telemetry Synced (" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ")");
        syncStatusBadge.setStyle("-fx-background-color:#ecfdf5; -fx-text-fill:#059669; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:20; -fx-border-color:#a7f3d0; -fx-border-radius:20;");

        Button refreshBtn = new Button("↺ Refresh Telemetry");
        refreshBtn.setStyle("-fx-background-color:white; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:6 12; -fx-font-size:12; -fx-font-weight:bold; -fx-text-fill:#334155; -fx-cursor:hand;");
        refreshBtn.setOnAction(e -> refreshData());

        HBox row = new HBox(12, titleBox, space, syncStatusBadge, refreshBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox buildConsoleRow() {
        HBox row = new HBox(18);
        row.setAlignment(Pos.TOP_LEFT);

        // Left Panel: Assigned Consignments Selector
        VBox leftSelector = new VBox(12);
        leftSelector.setPrefWidth(420);
        leftSelector.setMinWidth(380);
        leftSelector.setPadding(new Insets(16));
        leftSelector.setStyle(CARD_STYLE);

        Label selTitle = new Label("📦 Your Assigned Shipments");
        selTitle.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        searchField = new TextField();
        searchField.setPromptText("🔍 Search Shipment ID, Cargo, Vessel...");
        searchField.setStyle("-fx-background-color:white; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-padding:7 10; -fx-font-size:12;");
        searchField.textProperty().addListener((obs, o, n) -> renderShipmentList());

        statusFilterCombo = new ComboBox<>();
        statusFilterCombo.getItems().addAll("All Statuses", "In Transit", "Arriving Soon", "Customs Cleared", "Delivered", "Under Inspection");
        statusFilterCombo.setValue("All Statuses");
        statusFilterCombo.setMaxWidth(Double.MAX_VALUE);
        statusFilterCombo.setStyle("-fx-background-color:white; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-font-size:12;");
        statusFilterCombo.valueProperty().addListener((obs, o, n) -> renderShipmentList());

        shipmentSelectionList = new VBox(8);
        ScrollPane listScroll = new ScrollPane(shipmentSelectionList);
        listScroll.setFitToWidth(true);
        listScroll.setPrefHeight(520);
        listScroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-color:transparent;");

        leftSelector.getChildren().addAll(selTitle, searchField, statusFilterCombo, new Separator(), listScroll);

        // Right Panel: Live Tracking Console & Vessel Telemetry
        trackingDetailsContainer = new VBox(16);
        trackingDetailsContainer.setPadding(new Insets(20));
        trackingDetailsContainer.setStyle(CARD_STYLE);
        HBox.setHgrow(trackingDetailsContainer, Priority.ALWAYS);

        renderTrackingDetails();

        row.getChildren().addAll(leftSelector, trackingDetailsContainer);
        return row;
    }

    private void refreshData() {
        Platform.runLater(() -> {
            List<Map<String, Object>> cargos;
            if (LoginPage.loggedInUser != null && !LoginPage.loggedInUser.isEmpty()) {
                cargos = cargoDao.getShipmentsForImporter(LoginPage.loggedInUser);
            } else {
                String loggedUser = getLoggedImporterFilter();
                cargos = cargoDao.getShipmentsForImporter(loggedUser);
            }

            userShipments.clear();
            if (cargos != null) {
                userShipments.addAll(cargos);
            }

            if (selectedShipment == null || !userShipments.contains(selectedShipment)) {
                selectedShipment = !userShipments.isEmpty() ? userShipments.get(0) : null;
            }

            renderShipmentList();
            renderTrackingDetails();

            if (syncStatusBadge != null) {
                syncStatusBadge.setText("● AIS Telemetry Synced (" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ")");
            }
        });
    }

    private void renderShipmentList() {
        if (shipmentSelectionList == null) return;
        shipmentSelectionList.getChildren().clear();

        String q = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String stFilter = statusFilterCombo != null && statusFilterCombo.getValue() != null ? statusFilterCombo.getValue() : "All Statuses";

        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> c : userShipments) {
            if (c == null) continue;
            String cId = str(c.get("cargoId"), "");
            String cName = str(c.get("cargoName"), "");
            String vName = str(c.get("assignedVessel"), str(c.get("vesselName"), "AFRAPEARL II"));
            String status = str(c.get("status"), "In Transit");

            boolean matchesQ = q.isEmpty() || cId.toLowerCase().contains(q) || cName.toLowerCase().contains(q) || vName.toLowerCase().contains(q);
            boolean matchesSt = "All Statuses".equalsIgnoreCase(stFilter) || status.equalsIgnoreCase(stFilter) || (stFilter.equalsIgnoreCase("In Transit") && status.equalsIgnoreCase("Registered"));

            if (matchesQ && matchesSt) {
                filtered.add(c);
            }
        }

        if (filtered.isEmpty()) {
            VBox emp = new VBox(8);
            emp.setAlignment(Pos.CENTER);
            emp.setPadding(new Insets(30));
            Label empIcon = new Label("📦");
            empIcon.setStyle("-fx-font-size:28px; -fx-text-fill:#94a3b8;");
            Label empMsg = new Label(userShipments.isEmpty()
                    ? "No incoming shipments assigned to your account yet."
                    : "No matching shipments found.");
            empMsg.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#64748b;");
            emp.getChildren().addAll(empIcon, empMsg);
            shipmentSelectionList.getChildren().add(emp);
            return;
        }

        for (Map<String, Object> c : filtered) {
            String cId = str(c.get("cargoId"), "CGO-N/A");
            String cName = str(c.get("cargoName"), "Consignment");
            String vName = str(c.get("assignedVessel"), str(c.get("vesselName"), str(c.get("assignedShip"), "Not Assigned")));
            String status = str(c.get("status"), "In Transit");

            HBox item = new HBox(10);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(10, 12, 10, 12));

            boolean isSel = selectedShipment != null && cId.equalsIgnoreCase(str(selectedShipment.get("cargoId"), ""));
            item.setStyle(isSel
                    ? "-fx-background-color:#eff6ff; -fx-border-color:#3b82f6; -fx-border-radius:8; -fx-background-radius:8; -fx-cursor:hand;"
                    : "-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8; -fx-cursor:hand;");

            item.setOnMouseClicked(e -> {
                selectedShipment = c;
                renderShipmentList();
                renderTrackingDetails();
            });

            Label ic = new Label("🚢");
            ic.setStyle("-fx-font-size:18px;");

            VBox vb = new VBox(2);
            HBox.setHgrow(vb, Priority.ALWAYS);
            Label idLbl = new Label(cId + " • " + cName);
            idLbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
            Label sub = new Label("Vessel: " + vName + "  |  Status: " + status);
            sub.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");
            vb.getChildren().addAll(idLbl, sub);

            item.getChildren().addAll(ic, vb);
            shipmentSelectionList.getChildren().add(item);
        }
    }

    private void renderTrackingDetails() {
        if (trackingDetailsContainer == null) return;
        trackingDetailsContainer.getChildren().clear();

        if (selectedShipment == null) {
            VBox emp = new VBox(12);
            emp.setAlignment(Pos.CENTER);
            emp.setPadding(new Insets(60));
            Label empIcon = new Label("📡");
            empIcon.setStyle("-fx-font-size:36px; -fx-text-fill:#94a3b8;");
            Label empMsg = new Label("Select a shipment from the list to view live vessel telemetry & milestone status.");
            empMsg.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#64748b;");
            emp.getChildren().addAll(empIcon, empMsg);
            trackingDetailsContainer.getChildren().add(emp);
            return;
        }

        Map<String, Object> c = selectedShipment;
        String cId = str(c.get("cargoId"), "CGO-N/A");
        String cName = str(c.get("cargoName"), "Consignment");
        String cType = str(c.get("cargoType"), "General Cargo");
        String qty = str(c.get("quantity"), "1") + " Units";
        String rawWt = str(c.get("weight"), "—");
        String wt = rawWt.equals("—") || rawWt.contains("kg") ? rawWt : (rawWt + " kg");
        String exp = str(c.get("exporterName"), str(c.get("exporter"), "—"));
        String imp = getLoggedImporterName();
        String orig = str(c.get("originPort"), "—");
        String dest = str(c.get("destinationPort"), "—");
        String vName = str(c.get("assignedVessel"), str(c.get("vesselName"), str(c.get("assignedShip"), "Not Assigned")));
        String voyage = "VOY-" + Math.abs(cId.hashCode() % 900 + 100);
        String berth = "Berth B-" + (Math.abs(cId.hashCode() % 5) + 1);

        String shipmentStatus = str(c.get("status"), "In Transit");
        String customsStatus = str(c.get("customsStatus"), "Cleared");
        String deliveryStatus = str(c.get("deliveryStatus"), "Delivered".equalsIgnoreCase(shipmentStatus) ? "Delivered" : "Pending Arrival");

        // Lookup live vessel telemetry from VesselManager
        VesselData vData = vesselManager.getVesselByName(vName);
        boolean isLiveAis = vData != null && vData.isValid();

        // 1. Top Shipment Header Banner
        HBox banner = new HBox(12);
        banner.setAlignment(Pos.CENTER_LEFT);
        Label idTag = new Label("CONSIGNMENT " + cId);
        idTag.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0284c7; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:6;");
        Label cNameLbl = new Label("📦  " + cName + " (" + cType + ")");
        cNameLbl.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
        Region spB = new Region();
        HBox.setHgrow(spB, Priority.ALWAYS);
        Label statusBadge = new Label(shipmentStatus.toUpperCase());
        statusBadge.setStyle(statusBadgeStyle(shipmentStatus));
        banner.getChildren().addAll(idTag, cNameLbl, spB, statusBadge);

        // 2. Live Vessel AIS Telemetry Card
        VBox vesselCard = new VBox(10);
        vesselCard.setPadding(new Insets(14));
        vesselCard.setStyle("-fx-background-color:#0f172a; -fx-background-radius:10;");

        HBox vTop = new HBox(10);
        vTop.setAlignment(Pos.CENTER_LEFT);
        Label vIcon = new Label("🚢");
        vIcon.setStyle("-fx-font-size:22px; -fx-text-fill:#38bdf8;");

        VBox vTitles = new VBox(2);
        Label vn = new Label("Assigned Vessel: " + vName);
        vn.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:white;");
        Label vsub = new Label("Vessel Type: Container Carrier  |  Voyage: " + voyage + "  |  " + orig + " ➔ " + dest);
        vsub.setStyle("-fx-font-size:12px; -fx-text-fill:#94a3b8;");
        vTitles.getChildren().addAll(vn, vsub);

        Region vsp = new Region();
        HBox.setHgrow(vsp, Priority.ALWAYS);

        Label aisBadge = new Label(isLiveAis
                ? "● Live AIS: Online (" + String.format("%.1f kn", vData.speed) + ")"
                : "○ AIS Unavailable / Port Scheduled");
        aisBadge.setStyle(isLiveAis
                ? "-fx-background-color:#064e3b; -fx-text-fill:#34d399; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:20; -fx-border-color:#059669; -fx-border-radius:20;"
                : "-fx-background-color:#334155; -fx-text-fill:#cbd5e1; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:20;");

        vTop.getChildren().addAll(vIcon, vTitles, vsp, aisBadge);

        // Telemetry Grid
        GridPane telGrid = new GridPane();
        telGrid.setHgap(16);
        telGrid.setVgap(8);

        String locStr = isLiveAis ? String.format("%.4f° N, %.4f° E", vData.latitude, vData.longitude) : "Anchorage / Berth Sector";
        String spdStr = isLiveAis ? String.format("%.1f Knots • Heading %.1f°", vData.speed, vData.heading) : "Scheduled Transit Speed";
        String etaStr = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("dd MMM yyyy, 18:00"));
        String lastUpd = isLiveAis ? "Live Sync (" + vData.getFormattedTimestamp() + ")" : "Last updated: Today";

        telGrid.add(darkDetailCell("Current Vessel Location", locStr, "#38bdf8"), 0, 0);
        telGrid.add(darkDetailCell("Speed & Course", spdStr, "#38bdf8"), 1, 0);
        telGrid.add(darkDetailCell("Estimated Arrival (ETA)", etaStr, "#34d399"), 2, 0);
        telGrid.add(darkDetailCell("Assigned Berth", berth, "white"), 0, 1);
        telGrid.add(darkDetailCell("Vessel Movement Status", isLiveAis ? VesselManager.categorizeStatus(vData) : "In Transit", "white"), 1, 1);
        telGrid.add(darkDetailCell("Telemetry Sync", lastUpd, "#94a3b8"), 2, 1);

        vesselCard.getChildren().addAll(vTop, new Separator(), telGrid);

        // 3. Consignment & Cargo Dossier Grid
        VBox cargoDossier = new VBox(8);
        cargoDossier.setPadding(new Insets(12));
        cargoDossier.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:8;");

        Label dosTitle = new Label("📋 Consignment & Logistics Information");
        dosTitle.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        GridPane cGrid = new GridPane();
        cGrid.setHgap(16);
        cGrid.setVgap(8);

        cGrid.add(lightDetailCell("Exporter", exp), 0, 0);
        cGrid.add(lightDetailCell("Importer (Consignee)", imp), 1, 0);
        cGrid.add(lightDetailCell("Quantity & Weight", qty + " • " + wt), 2, 0);
        cGrid.add(lightDetailCell("Origin Port", orig), 0, 1);
        cGrid.add(lightDetailCell("Destination Port", dest), 1, 1);
        cGrid.add(lightDetailCell("Customs Clearance Status", customsStatus), 2, 1);

        cargoDossier.getChildren().addAll(dosTitle, cGrid);

        // 4. Milestone Timeline (Shipment Created -> Confirmed -> Loaded -> Departed -> In Transit -> Arriving -> Arrived -> Delivered)
        VBox milestoneBox = buildMilestoneTimeline(shipmentStatus, orig, dest, vName, etaStr);

        trackingDetailsContainer.getChildren().addAll(banner, vesselCard, cargoDossier, milestoneBox);
    }

    private VBox buildMilestoneTimeline(String status, String orig, String dest, String vessel, String eta) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(14));
        box.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8;");

        Label title = new Label("⏱️ Shipment Milestone Progression");
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        HBox timelineRow = new HBox(8);
        timelineRow.setAlignment(Pos.CENTER_LEFT);

        boolean isDelivered = "Delivered".equalsIgnoreCase(status);
        boolean isArrived = isDelivered || "Arrived".equalsIgnoreCase(status);
        boolean isArriving = isArrived || "Arriving Soon".equalsIgnoreCase(status);
        boolean isInTransit = isArriving || "In Transit".equalsIgnoreCase(status) || "Cleared".equalsIgnoreCase(status);
        boolean isDeparted = isInTransit || "Departed".equalsIgnoreCase(status);
        boolean isLoaded = isDeparted || "Loaded".equalsIgnoreCase(status);
        boolean isConfirmed = isLoaded || "Confirmed".equalsIgnoreCase(status);

        timelineRow.getChildren().addAll(
                milestoneNode("1. Created", true, "Exporter Created"),
                milestoneArrow(isConfirmed),
                milestoneNode("2. Confirmed", isConfirmed, "Cargo Verified"),
                milestoneArrow(isLoaded),
                milestoneNode("3. Loaded", isLoaded, "Container Sealed"),
                milestoneArrow(isDeparted),
                milestoneNode("4. Departed", isDeparted, orig),
                milestoneArrow(isInTransit),
                milestoneNode("5. In Transit", isInTransit, vessel),
                milestoneArrow(isArriving),
                milestoneNode("6. Arriving", isArriving, dest),
                milestoneArrow(isDelivered),
                milestoneNode("7. Delivered", isDelivered, "Consignee Received")
        );

        box.getChildren().addAll(title, timelineRow);
        return box;
    }

    private VBox milestoneNode(String name, boolean done, String sub) {
        VBox b = new VBox(2);
        b.setAlignment(Pos.CENTER);
        Label ic = new Label(done ? "✓" : "○");
        ic.setMinSize(24, 24);
        ic.setAlignment(Pos.CENTER);
        ic.setStyle(done
                ? "-fx-background-color:#16a34a; -fx-text-fill:white; -fx-background-radius:12; -fx-font-size:11; -fx-font-weight:bold;"
                : "-fx-background-color:#e2e8f0; -fx-text-fill:#94a3b8; -fx-background-radius:12; -fx-font-size:11;");
        Label l = new Label(name);
        l.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:" + (done ? "#0f172a" : "#94a3b8") + ";");
        Label s = new Label(sub);
        s.setStyle("-fx-font-size:9px; -fx-text-fill:#64748b;");
        b.getChildren().addAll(ic, l, s);
        return b;
    }

    private Label milestoneArrow(boolean active) {
        Label a = new Label("➔");
        a.setStyle("-fx-text-fill:" + (active ? "#16a34a" : "#cbd5e1") + "; -fx-font-size:12;");
        return a;
    }

    private static VBox darkDetailCell(String label, String value, String color) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:11px;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill:" + color + "; -fx-font-size:13px; -fx-font-weight:bold;");
        return new VBox(2, l, v);
    }

    private static VBox lightDetailCell(String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px; -fx-font-weight:bold;");
        Label v = new Label(value != null && !value.isBlank() ? value : "N/A");
        v.setStyle("-fx-text-fill:#0f172a; -fx-font-size:12px;");
        return new VBox(2, l, v);
    }

    private String statusBadgeStyle(String status) {
        if ("Delivered".equalsIgnoreCase(status)) {
            return "-fx-background-color:#dcfce7; -fx-text-fill:#16a34a; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;";
        } else if ("Arriving Soon".equalsIgnoreCase(status)) {
            return "-fx-background-color:#fef3c7; -fx-text-fill:#d97706; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;";
        } else if ("Delayed / At Risk".equalsIgnoreCase(status) || "Under Inspection".equalsIgnoreCase(status)) {
            return "-fx-background-color:#fee2e2; -fx-text-fill:#dc2626; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;";
        }
        return "-fx-background-color:#e0f2fe; -fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;";
    }

    private static String str(Object o, String fallback) {
        return (o != null && !o.toString().isBlank()) ? o.toString().trim() : fallback;
    }

    private String getLoggedImporterName() {
        if (LoginPage.loggedInUser != null) {
            Object n = LoginPage.loggedInUser.get("name");
            if (n != null && !n.toString().isBlank()) return n.toString().trim();
            Object f = LoginPage.loggedInUser.get("fullName");
            if (f != null && !f.toString().isBlank()) return f.toString().trim();
            Object d = LoginPage.loggedInUser.get("displayName");
            if (d != null && !d.toString().isBlank()) return d.toString().trim();
            Object c = LoginPage.loggedInUser.get("company");
            if (c != null && !c.toString().isBlank()) return c.toString().trim();
        }
        return "Importer";
    }

    private String getLoggedImporterFilter() {
        if (LoginPage.loggedInUser != null) {
            Object c = LoginPage.loggedInUser.get("company");
            if (c != null && !c.toString().isBlank()) return c.toString().trim();
            Object e = LoginPage.loggedInUser.get("email");
            if (e != null && !e.toString().isBlank()) return e.toString().trim();
            Object n = LoginPage.loggedInUser.get("name");
            if (n != null && !n.toString().isBlank()) return n.toString().trim();
            Object u = LoginPage.loggedInUser.get("uid");
            if (u != null && !u.toString().isBlank()) return u.toString().trim();
        }
        return "";
    }

    private VBox createSidebar(Runnable back) {
        Map<String, Button> menuButtons = new java.util.LinkedHashMap<>();
        Button dashboard = new Button("🏠   Dashboard");
        Button track = new Button("📥   Track Shipment");
        Button invoice = new Button("📄   Download Invoice");
        Button delivery = new Button("📦   Confirm Delivery");

        menuButtons.put("Dashboard", dashboard);
        menuButtons.put("Track Shipment", track);
        menuButtons.put("Download Invoice", invoice);
        menuButtons.put("Confirm Delivery", delivery);

        dashboard.setOnAction(event -> {
            if (back != null) back.run();
        });

        invoice.setOnAction(event -> {
            DownloadInvoice page = new DownloadInvoice();
            LoginPage.showRoot(ImporterDashboard.importerStage, page.getDownloadInvoiceScene(back).getRoot());
        });

        delivery.setOnAction(event -> {
            ConfirmDelivery page = new ConfirmDelivery();
            LoginPage.showRoot(ImporterDashboard.importerStage, page.getConfirmDeliveryScene(back).getRoot());
        });

        return DashboardLayoutHelper.createSidebar(ImporterDashboard.importerStage, "Importer", "Track Shipment", menuButtons);
    }

    private HBox createHeader(String pageTitle, String breadcrumb) {
        return DashboardLayoutHelper.createHeader(ImporterDashboard.importerStage, pageTitle, breadcrumb, "Importer", () -> {
            if (ImporterDashboard.importerStage != null) {
                new LoginPage().show(ImporterDashboard.importerStage);
            }
        }, null);
    }
}
