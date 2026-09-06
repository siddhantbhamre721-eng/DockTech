package com.projectui.view.ShipCaptain;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.projectui.model.Ship;
import com.projectui.service.VesselData;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UpdateShipStatus {

    private static final String BLUE = "#1769E8";
    private static final String TEXT = "#111827";
    private static final String MUTED = "#64748b";
    private static final String CARD_STYLE = "-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-radius:8; -fx-background-radius:8; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.06), 14, 0, 0, 4);";

    public Node getContent() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color:transparent;");

        // Header
        Label title = new Label("Update Ship Status");
        title.setStyle("-fx-text-fill:#05070c; -fx-font-size:24; -fx-font-weight:bold;");
        Label subtitle = new Label("Manage the current operational condition and navigational status of your assigned vessel.");
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
            container.getChildren().addAll(
                    buildKpiRow(ship, v),
                    buildUpdateSection(ship, v),
                    buildMovementAndVoyageRow(ship, v),
                    buildTimelineAndClearanceRow(ship, v)
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
        return new DashBoard().getRoot(null, null, "Update Ship Status");
    }

    public Parent getRoot(Stage stage, Runnable logoutAction) {
        return new DashBoard().getRoot(stage, logoutAction, "Update Ship Status");
    }

    public Scene getShipScene() {
        return new Scene(getRoot());
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

        Label sub = new Label("You must have an assigned vessel to update operational ship status.");
        sub.setStyle("-fx-font-size:14; -fx-text-fill:#64748b;");

        card.getChildren().addAll(icon, msg, sub);
        return card;
    }

    private HBox buildKpiRow(Ship ship, VesselData v) {
        String status = ship.getStatus() != null ? ship.getStatus() : (v != null ? v.navigationStatus : "At Berth");
        String location = ship.getBerth() != null && !ship.getBerth().isBlank() ? ship.getBerth() : "Port of Mumbai";
        String lastUpdated = v != null && v.timestamp != null ? v.timestamp : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        HBox row = new HBox(12,
                kpiCard("⚓", "Assigned Vessel", ship.getShipName(), "Assigned Ship", "#1d4ed8", "#eaf3ff"),
                kpiCard("●", "Current Status", status, "Live Condition", "#0f9f6e", "#e7f8ef"),
                kpiCard("⌖", "Current Berth/Port", location, "Port Location", "#6d4aff", "#f0edff"),
                kpiCard("◷", "Last Synchronized", lastUpdated, "Live AIS Sync", "#d97706", "#fef3c7")
        );
        row.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        return row;
    }

    private HBox buildUpdateSection(Ship ship, VesselData v) {
        HBox row = new HBox(14);

        // Left Form: Status Update Action
        VBox formCard = new VBox(14);
        formCard.setPadding(new Insets(18));
        formCard.setStyle(CARD_STYLE);
        HBox.setHgrow(formCard, Priority.ALWAYS);

        Label formTitle = new Label("Update Operational Status");
        formTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        Label statusLabel = new Label("Select New Operational Condition");
        statusLabel.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12; -fx-font-weight:bold;");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(
                "At Berth",
                "Cargo Loading",
                "Loading Completed",
                "Ready for Departure",
                "Underway / In Transit",
                "Anchored",
                "Maintenance & Inspection"
        );
        String currentStatus = ship.getStatus() != null ? ship.getStatus() : "At Berth";
        statusCombo.setValue(statusCombo.getItems().contains(currentStatus) ? currentStatus : "At Berth");
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        statusCombo.setStyle("-fx-background-color:white; -fx-border-color:#d1d5db; -fx-padding:8 12; -fx-font-size:13; -fx-background-radius:6;");

        Label locationLabel = new Label("Current Berth / Anchorage Location");
        locationLabel.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12; -fx-font-weight:bold;");

        TextField locationField = new TextField(ship.getBerth() != null ? ship.getBerth() : "Berth B04");
        locationField.setStyle("-fx-background-color:white; -fx-border-color:#d1d5db; -fx-padding:8 12; -fx-font-size:13; -fx-background-radius:6;");

        Label remarksLabel = new Label("Operational Remarks & Log Notes");
        remarksLabel.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12; -fx-font-weight:bold;");

        TextArea remarksArea = new TextArea();
        remarksArea.setPromptText("Enter optional operational notes, clearance status, or weather remarks...");
        remarksArea.setPrefRowCount(3);
        remarksArea.setStyle("-fx-background-color:white; -fx-border-color:#d1d5db; -fx-padding:8; -fx-font-size:13; -fx-background-radius:6;");

        Label alertBanner = new Label();
        alertBanner.setVisible(false);
        alertBanner.setManaged(false);
        alertBanner.setMaxWidth(Double.MAX_VALUE);
        alertBanner.setPadding(new Insets(10, 14, 10, 14));

        Button submitBtn = new Button("Update Status & Broadcast Telemetry");
        submitBtn.setStyle("-fx-background-color:" + BLUE + "; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:10 18; -fx-background-radius:6; -fx-cursor:hand;");
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        submitBtn.setOnAction(e -> {
            String selStatus = statusCombo.getValue();
            String loc = locationField.getText().trim();
            String rem = remarksArea.getText().trim();

            boolean success = CaptainSessionService.updateShipStatus(selStatus, loc, rem);
            if (success) {
                alertBanner.setText("✓ Ship status successfully updated to '" + selStatus + "' and broadcast to Port Authority & AIS.");
                alertBanner.setStyle("-fx-background-color:#ecfdf5; -fx-text-fill:#065f46; -fx-border-color:#a7f3d0; -fx-border-radius:6; -fx-font-weight:bold;");
                alertBanner.setVisible(true);
                alertBanner.setManaged(true);
            }
        });

        formCard.getChildren().addAll(
                formTitle,
                statusLabel,
                statusCombo,
                locationLabel,
                locationField,
                remarksLabel,
                remarksArea,
                alertBanner,
                submitBtn
        );

        // Right Panel: Live Vessel State Overview & Status Impact
        VBox stateCard = new VBox(14);
        stateCard.setPadding(new Insets(18));
        stateCard.setStyle(CARD_STYLE);
        HBox.setHgrow(stateCard, Priority.ALWAYS);

        Label stateTitle = new Label("Live Vessel Telemetry State");
        stateTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        GridPane stateGrid = new GridPane();
        stateGrid.setHgap(16);
        stateGrid.setVgap(10);

        String speedStr = v != null ? String.format("%.1f Knots", v.speed) : "12.5 Knots";
        String courseStr = v != null ? String.format("%.1f°", v.course) : "184.0°";
        String coords = v != null ? String.format("%.4f° N, %.4f° E", v.latitude, v.longitude) : "18.9439° N, 72.8354° E";
        String dest = CaptainSessionService.getAssignedVoyageDestination();
        String captain = CaptainSessionService.getCaptainName();

        stateGrid.add(fieldCell("Assigned Vessel", ship.getShipName()), 0, 0);
        stateGrid.add(fieldCell("Captain in Command", captain), 1, 0);
        stateGrid.add(fieldCell("Current Speed", speedStr), 0, 1);
        stateGrid.add(fieldCell("Heading / Course", courseStr), 1, 1);
        stateGrid.add(fieldCell("GPS Coordinates", coords), 0, 2);
        stateGrid.add(fieldCell("Scheduled Destination", dest), 1, 2);

        VBox impactBox = new VBox(6);
        impactBox.setPadding(new Insets(10, 12, 10, 12));
        impactBox.setStyle("-fx-background-color:#eff6ff; -fx-background-radius:6; -fx-border-color:#bfdbfe; -fx-border-radius:6;");

        Label impactTitle = new Label("📡 Status Synchronization Broadcast");
        impactTitle.setStyle("-fx-text-fill:#1d4ed8; -fx-font-size:12; -fx-font-weight:bold;");

        Label impactSub = new Label("Status updates immediately synchronize across DockTech Port Authority, Customs Inspection, Marine Traffic AIS, and Exporter/Importer logistics.");
        impactSub.setWrapText(true);
        impactSub.setStyle("-fx-text-fill:#334155; -fx-font-size:12;");

        impactBox.getChildren().addAll(impactTitle, impactSub);

        stateCard.getChildren().addAll(stateTitle, stateGrid, impactBox);

        row.getChildren().addAll(formCard, stateCard);
        return row;
    }

    private HBox buildMovementAndVoyageRow(Ship ship, VesselData v) {
        HBox row = new HBox(14);

        // Left Panel: Live Vessel Movement
        VBox movementCard = new VBox(12);
        movementCard.setPadding(new Insets(16));
        movementCard.setStyle(CARD_STYLE);
        HBox.setHgrow(movementCard, Priority.ALWAYS);

        Label mTitle = new Label("Live Vessel Movement & Positioning");
        mTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        GridPane mGrid = new GridPane();
        mGrid.setHgap(16);
        mGrid.setVgap(10);

        String speedStr = v != null ? String.format("%.1f Knots", v.speed) : "12.5 Knots";
        String courseStr = v != null ? String.format("%.1f°", v.course) : "184.0°";
        String headingStr = v != null ? String.format("%.1f°", v.heading) : "184.0°";
        String lat = v != null ? String.format("%.4f° N", v.latitude) : "18.9439° N";
        String lon = v != null ? String.format("%.4f° E", v.longitude) : "72.8354° E";
        String navStatus = v != null ? v.navigationStatus : (ship.getStatus() != null ? ship.getStatus() : "At Berth");
        String lastSync = v != null && v.timestamp != null ? v.timestamp : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        mGrid.add(fieldCell("Current Speed", speedStr), 0, 0);
        mGrid.add(fieldCell("Course / Heading", courseStr + " / " + headingStr), 1, 0);
        mGrid.add(fieldCell("Latitude", lat), 0, 1);
        mGrid.add(fieldCell("Longitude", lon), 1, 1);
        mGrid.add(fieldCell("Navigation Status", navStatus), 0, 2);
        mGrid.add(fieldCell("Last AIS Update", lastSync), 1, 2);

        movementCard.getChildren().addAll(mTitle, mGrid);

        // Right Panel: Voyage & Route Status
        VBox routeCard = new VBox(12);
        routeCard.setPadding(new Insets(16));
        routeCard.setStyle(CARD_STYLE);
        HBox.setHgrow(routeCard, Priority.ALWAYS);

        Label rTitle = new Label("Voyage & Route Status");
        rTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        GridPane rGrid = new GridPane();
        rGrid.setHgap(16);
        rGrid.setVgap(10);

        String origin = CaptainSessionService.getAssignedVoyageOrigin();
        String dest = CaptainSessionService.getAssignedVoyageDestination();
        String voyageId = "VOY-" + (ship.getShipId() != null ? ship.getShipId() : "2026") + "-E";
        String eta = ship.getArrival() != null && !ship.getArrival().isBlank() ? ship.getArrival() : "10:30 AM";
        String berth = ship.getBerth() != null && !ship.getBerth().isBlank() ? ship.getBerth() : "Berth B04";

        rGrid.add(fieldCell("Origin Port", origin), 0, 0);
        rGrid.add(fieldCell("Destination Port", dest), 1, 0);
        rGrid.add(fieldCell("Voyage ID / Name", voyageId), 0, 1);
        rGrid.add(fieldCell("Estimated Arrival (ETA)", eta), 1, 1);
        rGrid.add(fieldCell("Assigned Berth", berth), 0, 2);
        rGrid.add(fieldCell("Distance Remaining", "840 NM (Est.)"), 1, 2);

        routeCard.getChildren().addAll(rTitle, rGrid);

        row.getChildren().addAll(movementCard, routeCard);
        return row;
    }

    private HBox buildTimelineAndClearanceRow(Ship ship, VesselData v) {
        HBox row = new HBox(14);

        // Left Panel: Recent Status Activity
        VBox activityCard = new VBox(12);
        activityCard.setPadding(new Insets(16));
        activityCard.setStyle(CARD_STYLE);
        HBox.setHgrow(activityCard, Priority.ALWAYS);

        Label actTitle = new Label("Recent Status Activity & Log");
        actTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        String currStatus = ship.getStatus() != null ? ship.getStatus() : "Operational";
        String berth = ship.getBerth() != null ? ship.getBerth() : "Berth B04";

        VBox actList = new VBox(8,
                activityItem("✓", "Operational Status Synchronized", "Current condition set to '" + currStatus + "'", "Just Now", "#0f9f6e"),
                activityItem("⌖", "Berth Allocation Updated", "Position confirmed at " + berth, "10:25 AM", "#1d4ed8"),
                activityItem("📡", "AIS Telemetry Broadcasted", "Navigation state updated across port ground stations", "10:30 AM", "#0284c7")
        );

        activityCard.getChildren().addAll(actTitle, actList);

        // Right Panel: Port Clearance & Readiness
        VBox clearanceCard = new VBox(12);
        clearanceCard.setPadding(new Insets(16));
        clearanceCard.setStyle(CARD_STYLE);
        HBox.setHgrow(clearanceCard, Priority.ALWAYS);

        Label clrTitle = new Label("Port Clearance & Operational Readiness");
        clrTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        VBox clrList = new VBox(8,
                activityItem("✓", "Port Authority Clearance", "Inbound and berth access authorized", "Approved", "#0f9f6e"),
                activityItem("✓", "Customs & Manifest Clearance", "Duty payment and cargo declaration verified", "Approved", "#0f9f6e"),
                activityItem("●", "Berth Allocation", berth + " Assigned & Active", "Active", "#1d4ed8"),
                activityItem("✓", "Safety & Harbor Readiness", "Vessel navigation certified for departure", "Ready", "#0f9f6e")
        );

        clearanceCard.getChildren().addAll(clrTitle, clrList);

        row.getChildren().addAll(activityCard, clearanceCard);
        return row;
    }

    private HBox activityItem(String icon, String title, String sub, String time, String color) {
        Label i = new Label(icon);
        i.setStyle("-fx-text-fill:" + color + "; -fx-font-weight:bold; -fx-font-size:13;");
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-weight:bold; -fx-font-size:13;");
        Label s = new Label(sub);
        s.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12;");
        VBox tb = new VBox(1, t, s);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label tm = new Label(time);
        tm.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:11;");

        HBox item = new HBox(8, i, tb, sp, tm);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
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