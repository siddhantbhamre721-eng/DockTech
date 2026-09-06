package com.projectui.view.ShipCaptain;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SendSOS {

    private static final String RED = "#dc2626";
    private static final String TEXT = "#111827";
    private static final String MUTED = "#64748b";
    private static final String CARD_STYLE = "-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-radius:8; -fx-background-radius:8; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.06), 14, 0, 0, 4);";

    private Label previewPrioVal;
    private Label previewCatVal;
    private Label previewDescVal;
    private VBox transmissionStatusCard;

    public Node getContent() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color:transparent;");

        // Header
        Label title = new Label("Emergency SOS Console");
        title.setStyle("-fx-text-fill:#991b1b; -fx-font-size:24; -fx-font-weight:bold;");
        Label subtitle = new Label("Maritime Emergency Broadcast, Automated VHF Distress Beacon & High-Priority Search and Rescue Dispatch.");
        subtitle.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:13;");
        VBox titleBox = new VBox(3, title, subtitle);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox headerRow = new HBox(12, titleBox, space);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(headerRow);

        Ship ship = CaptainSessionService.getAssignedShip();
        VesselData v = CaptainSessionService.getAssignedVesselData();

        container.getChildren().addAll(
                buildKpiRow(ship, v),
                buildMainRow(ship, v),
                buildTransmissionStatusSection()
        );

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color:#f5f8fc; -fx-background:transparent; -fx-border-color:transparent;");
        return scroll;
    }

    public Parent getRoot() {
        return new DashBoard().getRoot(null, null, "Send SOS");
    }

    public Parent getRoot(Stage stage, Runnable logoutAction) {
        return new DashBoard().getRoot(stage, logoutAction, "Send SOS");
    }

    public Scene getSOSScene() {
        return new Scene(getRoot());
    }

    private HBox buildKpiRow(Ship ship, VesselData v) {
        Map<String, Object> sosRec = CaptainSessionService.getLatestSosRecord();
        String statusText = sosRec != null ? "TRANSMITTED / ACTIVE" : "ARMED & READY";
        String statusColor = sosRec != null ? "#dc2626" : "#0f9f6e";
        String statusBg = sosRec != null ? "#fee2e2" : "#e7f8ef";

        String lastSync = v != null && v.timestamp != null ? v.timestamp : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        HBox row = new HBox(12,
                kpiCard("🚨", "Distress Status", statusText, "Emergency Transponder", statusColor, statusBg),
                kpiCard("●", "Live Telemetry", "CONNECTED", "AIS Transponder Active", "#0f9f6e", "#e7f8ef"),
                kpiCard("◷", "Last Position Update", lastSync, "GPS / AIS Fix", "#d97706", "#fef3c7"),
                kpiCard("📡", "SOS Channel", "VHF Ch 16", "156.800 MHz Distress", "#1d4ed8", "#eaf3ff")
        );
        row.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        return row;
    }

    private HBox buildMainRow(Ship ship, VesselData v) {
        HBox row = new HBox(14);

        // Left Panel: Transmit Maritime Distress Alert Form
        VBox formCard = new VBox(12);
        formCard.setPadding(new Insets(18));
        formCard.setStyle(CARD_STYLE);
        HBox.setHgrow(formCard, Priority.ALWAYS);

        Label formTitle = new Label("Transmit Maritime Distress Alert");
        formTitle.setStyle("-fx-text-fill:#991b1b; -fx-font-size:16; -fx-font-weight:bold;");

        Label prioLabel = new Label("Emergency Severity Level");
        prioLabel.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12; -fx-font-weight:bold;");

        ToggleGroup prioGroup = new ToggleGroup();
        RadioButton rb1 = new RadioButton("CRITICAL (Mayday - Immediate Threat to Life / Vessel)");
        rb1.setToggleGroup(prioGroup);
        rb1.setSelected(true);
        rb1.setStyle("-fx-text-fill:#dc2626; -fx-font-weight:bold; -fx-font-size:12;");

        RadioButton rb2 = new RadioButton("URGENT (Pan-Pan - Urgent Safety / Navigational Hazard)");
        rb2.setToggleGroup(prioGroup);
        rb2.setStyle("-fx-text-fill:#d97706; -fx-font-weight:bold; -fx-font-size:12;");

        RadioButton rb3 = new RadioButton("HIGH PRIORITY (Security Alert / Medical Evacuation)");
        rb3.setToggleGroup(prioGroup);
        rb3.setStyle("-fx-text-fill:#2563eb; -fx-font-weight:bold; -fx-font-size:12;");

        prioGroup.selectedToggleProperty().addListener((obs, o, n) -> updatePreview(prioGroup, null, null));

        VBox prioBox = new VBox(6, rb1, rb2, rb3);
        prioBox.setPadding(new Insets(8));
        prioBox.setStyle("-fx-background-color:#fef2f2; -fx-border-color:#fecaca; -fx-border-radius:6;");

        Label catLabel = new Label("Emergency Incident Category");
        catLabel.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12; -fx-font-weight:bold;");

        ComboBox<String> catCombo = new ComboBox<>();
        catCombo.getItems().addAll(
                "Engine & Main Propulsion Failure",
                "Severe Hull Breach / Ingress of Water",
                "Fire / Hazardous Cargo Spill",
                "Medical Emergency / Critical Evacuation",
                "Collision Risk / Steering Failure",
                "Extreme Weather & Navigational Distress",
                "Security Threat / Piracy Alert"
        );
        catCombo.setValue("Engine & Main Propulsion Failure");
        catCombo.setMaxWidth(Double.MAX_VALUE);
        catCombo.setStyle("-fx-background-color:white; -fx-border-color:#d1d5db; -fx-padding:7 12; -fx-font-size:13; -fx-background-radius:6;");
        catCombo.valueProperty().addListener((obs, o, n) -> updatePreview(prioGroup, catCombo, null));

        Label descLabel = new Label("Incident Details & Situation Description");
        descLabel.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12; -fx-font-weight:bold;");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Enter crew onboard status, nature of emergency, required assistance, and present condition...");
        descArea.setPrefRowCount(3);
        descArea.setStyle("-fx-background-color:white; -fx-border-color:#d1d5db; -fx-padding:6; -fx-font-size:12; -fx-background-radius:6;");
        descArea.textProperty().addListener((obs, o, n) -> updatePreview(prioGroup, catCombo, descArea));

        Label alertBanner = new Label();
        alertBanner.setVisible(false);
        alertBanner.setManaged(false);
        alertBanner.setMaxWidth(Double.MAX_VALUE);
        alertBanner.setPadding(new Insets(10, 14, 10, 14));

        Button sosBtn = new Button("🚨  TRANSMIT DISTRESS BEACON");
        sosBtn.setStyle("-fx-background-color:" + RED + "; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:11 20; -fx-background-radius:6; -fx-cursor:hand;");
        sosBtn.setMaxWidth(Double.MAX_VALUE);

        sosBtn.setOnAction(e -> {
            String prio = rb1.isSelected() ? "CRITICAL (MAYDAY)" : (rb2.isSelected() ? "URGENT (PAN-PAN)" : "HIGH PRIORITY");
            String cat = catCombo.getValue();
            String desc = descArea.getText().trim();

            Map<String, Object> rec = CaptainSessionService.sendEmergencySOS(prio, cat, desc);
            if (rec != null) {
                alertBanner.setText("🚨 DISTRESS BEACON TRANSMITTED! Port Authority Coast Guard & Search and Rescue Command have received incident " + rec.get("incidentId") + ".");
                alertBanner.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#991b1b; -fx-border-color:#fca5a5; -fx-border-radius:6; -fx-font-weight:bold;");
                alertBanner.setVisible(true);
                alertBanner.setManaged(true);
                refreshTransmissionStatusCard(rec);
            }
        });

        formCard.getChildren().addAll(
                formTitle,
                prioLabel,
                prioBox,
                catLabel,
                catCombo,
                descLabel,
                descArea,
                alertBanner,
                sosBtn
        );

        // Right Panel: Live Emergency Position & Automated Distress Broadcast Payload Preview
        VBox rightCard = new VBox(12);
        rightCard.setPadding(new Insets(18));
        rightCard.setStyle(CARD_STYLE);
        HBox.setHgrow(rightCard, Priority.ALWAYS);

        Label rightTitle = new Label("Live Emergency Position & Broadcast Payload");
        rightTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(8);

        String shipName = ship != null ? ship.getShipName() : "";
        String shipType = ship != null && ship.getShipType() != null ? ship.getShipType() : "Tanker";
        String imo = ship != null && ship.getImoNumber() != null ? ship.getImoNumber() : (v != null ? v.imo : "");
        String mmsi = v != null ? String.valueOf(v.mmsi) : "";
        String coords = v != null ? String.format("%.4f° N, %.4f° E", v.latitude, v.longitude) : "18.9439° N, 72.8354° E";
        String speed = v != null ? String.format("%.1f Knots", v.speed) : "12.5 Knots";
        String heading = v != null ? String.format("%.1f°", v.heading) : "184.0°";
        String captain = CaptainSessionService.getCaptainName();
        String port = ship != null && ship.getBerth() != null ? ship.getBerth() : CaptainSessionService.getAssignedVoyageOrigin();
        String dest = CaptainSessionService.getAssignedVoyageDestination();

        grid.add(fieldCell("Vessel Name", shipName + " (" + shipType + ")"), 0, 0);
        grid.add(fieldCell("IMO / MMSI", imo + " / " + mmsi), 1, 0);
        grid.add(fieldCell("Captain in Command", captain), 0, 1);
        grid.add(fieldCell("Live GPS Fix", coords), 1, 1);
        grid.add(fieldCell("Speed / Heading", speed + " / " + heading), 0, 2);
        grid.add(fieldCell("Port / Destination", port + " ➔ " + dest), 1, 2);

        // Dynamic Payload preview box
        VBox payloadBox = new VBox(6);
        payloadBox.setPadding(new Insets(10));
        payloadBox.setStyle("-fx-background-color:#fef2f2; -fx-background-radius:6; -fx-border-color:#fecaca; -fx-border-radius:6;");

        Label pTitle = new Label("📡 Distress Broadcast Payload Preview");
        pTitle.setStyle("-fx-text-fill:#991b1b; -fx-font-size:12; -fx-font-weight:bold;");

        previewPrioVal = new Label("Severity: CRITICAL (MAYDAY)");
        previewPrioVal.setStyle("-fx-text-fill:#991b1b; -fx-font-size:11; -fx-font-weight:bold;");

        previewCatVal = new Label("Category: Engine & Main Propulsion Failure");
        previewCatVal.setStyle("-fx-text-fill:#334155; -fx-font-size:11;");

        previewDescVal = new Label("Situation: Standard distress beacon with real-time GPS telemetry broadcast.");
        previewDescVal.setWrapText(true);
        previewDescVal.setStyle("-fx-text-fill:#64748b; -fx-font-size:11;");

        payloadBox.getChildren().addAll(pTitle, previewPrioVal, previewCatVal, previewDescVal);

        Label protocol = new Label("Emergency Guidelines:\n• VHF radio tuned to Marine Channel 16 (156.800 MHz).\n• Activate EPIRB & SART beacons.\n• Port Emergency Dispatch center acknowledges within 60 seconds.");
        protocol.setWrapText(true);
        protocol.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12; -fx-background-color:#f8fafc; -fx-padding:8; -fx-border-color:#e2e8f0; -fx-border-radius:6;");

        rightCard.getChildren().addAll(rightTitle, grid, payloadBox, protocol);

        row.getChildren().addAll(formCard, rightCard);
        return row;
    }

    private VBox buildTransmissionStatusSection() {
        transmissionStatusCard = new VBox(12);
        transmissionStatusCard.setPadding(new Insets(16));
        transmissionStatusCard.setStyle(CARD_STYLE);

        Map<String, Object> sosRec = CaptainSessionService.getLatestSosRecord();
        refreshTransmissionStatusCard(sosRec);

        CaptainSessionService.addChangeListener(() -> {
            javafx.application.Platform.runLater(() -> {
                refreshTransmissionStatusCard(CaptainSessionService.getLatestSosRecord());
            });
        });

        return transmissionStatusCard;
    }

    private void refreshTransmissionStatusCard(Map<String, Object> sosRec) {
        if (transmissionStatusCard == null) return;
        transmissionStatusCard.getChildren().clear();

        Label cardTitle = new Label("Current SOS Transmission Status & Port Authority Dispatch");
        cardTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        if (sosRec == null) {
            Label noSos = new Label("No active distress alert transmitted. The emergency beacon is armed and synchronized with live GPS telemetry.");
            noSos.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:13;");
            transmissionStatusCard.getChildren().addAll(cardTitle, noSos);
        } else {
            HBox statusStrip = new HBox(12);
            statusStrip.setAlignment(Pos.CENTER_LEFT);
            statusStrip.setPadding(new Insets(10, 14, 10, 14));
            statusStrip.setStyle("-fx-background-color:#fef2f2; -fx-border-color:#fca5a5; -fx-border-radius:6;");

            Label sIcon = new Label("🚨");
            sIcon.setStyle("-fx-font-size:18;");

            VBox sInfo = new VBox(2);
            Label sTitle = new Label("INCIDENT ID: " + sosRec.getOrDefault("incidentId", "SOS-2026") + " • " + sosRec.getOrDefault("priority", "CRITICAL"));
            sTitle.setStyle("-fx-text-fill:#991b1b; -fx-font-size:13; -fx-font-weight:bold;");

            String curStatus = String.valueOf(sosRec.getOrDefault("status", "TRANSMITTED"));
            boolean isAck = "ACKNOWLEDGED".equalsIgnoreCase(curStatus);
            String ackBy = String.valueOf(sosRec.getOrDefault("acknowledgedBy", "Port Authority Command"));

            Label sDetail = new Label("Transmitted at " + sosRec.getOrDefault("timestamp", "") + " • Receiving Authority: " + sosRec.getOrDefault("authority", "Port Authority"));
            sDetail.setStyle("-fx-text-fill:#475569; -fx-font-size:12;");
            sInfo.getChildren().addAll(sTitle, sDetail);

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Label badge = new Label();
            if (isAck) {
                badge.setText("✓ ACKNOWLEDGED BY PORT AUTHORITY");
                badge.setStyle("-fx-background-color:#0f9f6e; -fx-text-fill:white; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;");
            } else {
                badge.setText("● SOS TRANSMITTED (PENDING ACKNOWLEDGEMENT)");
                badge.setStyle("-fx-background-color:#dc2626; -fx-text-fill:white; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;");
            }

            statusStrip.getChildren().addAll(sIcon, sInfo, sp, badge);

            GridPane logGrid = new GridPane();
            logGrid.setHgap(16);
            logGrid.setVgap(8);

            logGrid.add(fieldCell("Incident Category", String.valueOf(sosRec.getOrDefault("emergencyType", "-"))), 0, 0);
            logGrid.add(fieldCell("Reported Position", String.valueOf(sosRec.getOrDefault("location", "-"))), 1, 0);
            logGrid.add(fieldCell("Assigned Vessel", String.valueOf(sosRec.getOrDefault("vessel", "-"))), 0, 1);
            logGrid.add(fieldCell("Port & Destination", String.valueOf(sosRec.getOrDefault("port", "-")) + " ➔ " + String.valueOf(sosRec.getOrDefault("destination", "-"))), 1, 1);

            transmissionStatusCard.getChildren().addAll(cardTitle, statusStrip, logGrid);
        }
    }

    private void updatePreview(ToggleGroup prioGroup, ComboBox<String> catCombo, TextArea descArea) {
        if (previewPrioVal != null && prioGroup != null && prioGroup.getSelectedToggle() != null) {
            RadioButton sel = (RadioButton) prioGroup.getSelectedToggle();
            previewPrioVal.setText("Severity: " + (sel.getText().contains("Mayday") ? "CRITICAL (MAYDAY)" : (sel.getText().contains("Pan-Pan") ? "URGENT (PAN-PAN)" : "HIGH PRIORITY")));
        }
        if (previewCatVal != null && catCombo != null && catCombo.getValue() != null) {
            previewCatVal.setText("Category: " + catCombo.getValue());
        }
        if (previewDescVal != null && descArea != null) {
            String text = descArea.getText().trim();
            previewDescVal.setText("Situation: " + (text.isEmpty() ? "Standard distress beacon with real-time GPS telemetry broadcast." : text));
        }
    }

    private VBox fieldCell(String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:11; -fx-font-weight:bold;");
        Label v = new Label(value != null && !value.isBlank() ? value : "N/A");
        v.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:13; -fx-font-weight:bold;");
        return new VBox(2, l, v);
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