package com.projectui.view.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.projectui.service.VesselData;
import com.projectui.service.VesselFinderService;
import com.projectui.service.VesselManager;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ReportsPage extends AdminTheme {

    private final AdminDataStore store = AdminDataStore.getInstance();
    private final VesselFinderService aisService = VesselFinderService.getInstance();
    private final VesselManager vesselManager = VesselManager.getInstance();

    // Expansion state trackers for click-to-expand accordions
    private final boolean[] vesselReportExpanded = new boolean[]{false};
    private final boolean[] vesselMovementExpanded = new boolean[]{false};
    private final boolean[] portStatusExpanded = new boolean[]{false};
    private final boolean[] complianceExpanded = new boolean[]{false};

    // Container sections for real-time refresh
    private final HBox liveVesselSummaryRow = new HBox(12);
    private final VBox liveVesselReportCard = new VBox(8);
    private final VBox vesselMovementCard = new VBox(8);
    private final VBox livePortStatusCard = new VBox(8);
    private final VBox inspectionComplianceCard = new VBox(8);

    private Label toastMessage;
    private HBox toastBanner;
    private Label liveStatusIndicator;
    private Label lastUpdateLabel;

    public Parent getRoot() {
        Button generateBtn = primaryButton("+  Generate Report");
        generateBtn.setStyle(
                "-fx-background-color:" + BLUE + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:13;" +
                        "-fx-font-weight:bold;" +
                        "-fx-padding:8 18;" +
                        "-fx-background-radius:8;" +
                        "-fx-cursor:hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(11,99,255,0.2), 6, 0, 0, 2);");
        generateBtn.setOnAction(event -> showGenerateReportDialog(
                generateBtn.getScene() == null ? null : generateBtn.getScene().getWindow()));

        VBox page = pageContent();
        page.setSpacing(18);
        page.setPadding(new Insets(18, 22, 28, 22));

        // Toast status banner
        toastBanner = createToastBanner();

        // 1. Header Section
        HBox header = pageHeader(
                "Reports & Analytics",
                "Live maritime operations reporting center for real-time vessel telemetry, port status, and compliance reports.",
                generateBtn);

        // Header Live Status Badge & Timestamp
        liveStatusIndicator = new Label("● LIVE");
        liveStatusIndicator.setStyle("-fx-text-fill:#16a34a; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-color:#ecfdf5; -fx-background-radius:10;");
        lastUpdateLabel = new Label("Last updated: " + nowTime());
        lastUpdateLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        HBox liveHeaderBar = new HBox(10, liveStatusIndicator, lastUpdateLabel);
        liveHeaderBar.setAlignment(Pos.CENTER_LEFT);

        // Build all compact live sections
        buildLiveVesselSummary();
        buildLiveVesselReport();
        buildVesselMovementSection();
        buildLivePortReport();
        buildInspectionComplianceSection();

        page.getChildren().addAll(
                header,
                liveHeaderBar,
                toastBanner,
                liveVesselSummaryRow,
                liveVesselReportCard,
                vesselMovementCard,
                livePortStatusCard,
                inspectionComplianceCard);

        // Subscribe to live AIS updates
        aisService.start(vessels -> Platform.runLater(this::refreshAllLiveSections));

        // Initial refresh
        refreshAllLiveSections();

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background:transparent; -fx-background-color:transparent; -fx-padding:0;");

        return scrollPane;
    }

    private String nowTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private void refreshAllLiveSections() {
        if (aisService.isRateLimited()) {
            liveStatusIndicator.setText("● AIS RATE LIMITED");
            liveStatusIndicator.setStyle("-fx-text-fill:#ea580c; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-color:#fff7ed; -fx-background-radius:10;");
            lastUpdateLabel.setText("AIS temporarily unavailable · Last successful update: " + nowTime());
        } else {
            liveStatusIndicator.setText("● LIVE");
            liveStatusIndicator.setStyle("-fx-text-fill:#16a34a; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
            lastUpdateLabel.setText("Last updated: " + nowTime());
        }

        buildLiveVesselSummary();
        buildLiveVesselReport();
        buildVesselMovementSection();
        buildLivePortReport();
        buildInspectionComplianceSection();
    }

    // ==========================================
    // 1. LIVE VESSEL SUMMARY (TOP COMPACT ROW)
    // ==========================================
    private void buildLiveVesselSummary() {
        liveVesselSummaryRow.getChildren().clear();

        Collection<VesselData> vessels = vesselManager.getAllVessels();
        int total = vessels.size();
        int inPort = 0;
        int arriving = 0;
        int departing = 0;
        int inTransit = 0;
        int anchored = 0;

        for (VesselData v : vessels) {
            String cat = categorizeVesselStatus(v);
            if ("Currently In Port".equals(cat)) inPort++;
            else if ("Arriving".equals(cat)) arriving++;
            else if ("Departing".equals(cat)) departing++;
            else if ("Anchored".equals(cat)) anchored++;
            else inTransit++;
        }

        VBox c1 = compactSummaryCard("Total Live Vessels", String.valueOf(total), "Active Telemetry", "🚢", "#0b63ff", "#eff6ff");
        VBox c2 = compactSummaryCard("Currently In Port", String.valueOf(inPort), "Docked / Moored", "⚓", "#16a34a", "#ecfdf5");
        VBox c3 = compactSummaryCard("Arriving Vessels", String.valueOf(arriving), "Inbound Approach", "📥", "#0ea5e9", "#e0f2fe");
        VBox c4 = compactSummaryCard("Departing Vessels", String.valueOf(departing), "Outbound Dispatched", "📤", "#6366f1", "#eef2ff");
        VBox c5 = compactSummaryCard("In Transit", String.valueOf(inTransit), "Underway Engine", "⚡", "#0284c7", "#f0f9ff");
        VBox c6 = compactSummaryCard("Anchored", String.valueOf(anchored), "At Anchor / Waiting", "⊘", "#d97706", "#fef3c7");

        liveVesselSummaryRow.getChildren().addAll(c1, c2, c3, c4, c5, c6);
        for (Node node : liveVesselSummaryRow.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
            if (node instanceof VBox) {
                ((VBox) node).setMaxWidth(Double.MAX_VALUE);
            }
        }
    }

    private VBox compactSummaryCard(String title, String value, String subtitle, String iconStr, String accentColor, String bgLight) {
        Label icon = new Label(iconStr);
        icon.setStyle(
                "-fx-text-fill:" + accentColor + ";" +
                "-fx-font-size:16;" +
                "-fx-font-weight:bold;" +
                "-fx-alignment:center;" +
                "-fx-min-width:32;" +
                "-fx-min-height:32;" +
                "-fx-background-color:" + bgLight + ";" +
                "-fx-background-radius:6;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:14; -fx-font-weight:bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(8, icon, titleLabel, spacer);
        top.setAlignment(Pos.CENTER_LEFT);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:28; -fx-font-weight:800;");

        Label subLabel = new Label(subtitle);
        subLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13;");

        VBox card = new VBox(6, top, valueLabel, subLabel);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 8, 0, 0, 2);"
        );
        return card;
    }

    private String categorizeVesselStatus(VesselData v) {
        if (v == null) return "In Transit";
        String nav = v.navigationStatus != null ? v.navigationStatus.toLowerCase() : "";
        double spd = v.speed;

        if (nav.contains("moored") || nav.contains("docked") || nav.contains("port") || nav.contains("5")) {
            return "Currently In Port";
        }
        if (nav.contains("anchor") || nav.contains("1") || spd <= 0.3) {
            return "Anchored";
        }
        if (nav.contains("arriving") || (v.destination != null && !v.destination.isBlank() && spd > 2.0 && spd < 10.0)) {
            return "Arriving";
        }
        if (nav.contains("departing") || (spd >= 10.0 && v.course > 180)) {
            return "Departing";
        }
        return "In Transit";
    }

    private String matchNearestPort(double lat, double lng) {
        for (AdminDataStore.Port p : store.ports) {
            if ("APPROVED".equalsIgnoreCase(p.approvalStatus)) {
                double dlat = Math.abs(p.lat - lat);
                double dlng = Math.abs(p.lng - lng);
                if (dlat < 0.6 && dlng < 0.6) {
                    return p.name;
                }
            }
        }
        return "Offshore Waters";
    }

    // ==========================================
    // 2. LIVE VESSEL REPORT (CLICK-TO-EXPAND)
    // ==========================================
    private void buildLiveVesselReport() {
        liveVesselReportCard.getChildren().clear();

        Collection<VesselData> vessels = vesselManager.getAllVessels();
        List<VesselData> list = new ArrayList<>(vessels);
        int totalCount = list.size();

        // Preview Bar (when collapsed)
        HBox preview = new HBox(10);
        preview.setAlignment(Pos.CENTER_LEFT);
        preview.setPadding(new Insets(4, 0, 4, 0));
        Label prevText = new Label("📊 " + totalCount + " Live Vessels Tracked · Active AIS Telemetry Stream · Click header to expand full table");
        prevText.setStyle("-fx-text-fill:#64748b; -fx-font-size:13; -fx-font-weight:bold;");
        preview.getChildren().add(prevText);

        // Expanded Content Table with comfortable cell padding and column spacing
        GridPane table = new GridPane();
        table.setHgap(0); table.setVgap(0);
        table.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        String[] headers = {"Vessel Name", "IMO / MMSI", "Latitude", "Longitude", "Current Port", "Destination", "Status", "Speed", "Course", "ETA", "Last Update"};
        double[] liveColWidths = {165, 135, 110, 110, 145, 175, 120, 90, 90, 90, 110};

        for (int i = 0; i < headers.length; i++) {
            table.add(liveVesselTableCell(headers[i], true, liveColWidths[i]), i, 0);
        }

        if (list.isEmpty()) {
            Label empty = new Label("No active live vessel data available.");
            empty.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13; -fx-padding:16 20;");
            table.add(empty, 0, 1, 11, 1);
        } else {
            int rowIdx = 1;
            int limit = Math.min(25, list.size());
            for (int i = 0; i < limit; i++) {
                VesselData v = list.get(i);
                String name = (v.name != null && !v.name.isBlank()) ? v.name : "Vessel #" + v.mmsi;
                String imoMmsi = "MMSI: " + v.mmsi;
                String latStr = String.format("%.4f° N", v.latitude);
                String lngStr = String.format("%.4f° E", v.longitude);
                String port = matchNearestPort(v.latitude, v.longitude);
                String dest = (v.destination != null && !v.destination.isBlank()) ? v.destination : "Indian Maritime Corridor";
                String status = categorizeVesselStatus(v);
                String spd = String.format("%.1f kn", v.speed);
                String crs = String.format("%.1f°", v.course);
                String eta = (v.timestamp != null && v.timestamp.length() > 5) ? v.timestamp.substring(0, 5) : "18:30";
                String lastUp = nowTime();

                boolean isAlt = (rowIdx % 2 == 0);
                String bg = isAlt ? "#f8fafc" : "#ffffff";

                Label c1 = liveVesselTableCell(name, false, liveColWidths[0]);
                c1.setStyle(c1.getStyle() + " -fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold; -fx-background-color:" + bg + ";");
                Label c2 = liveVesselTableCell(imoMmsi, false, liveColWidths[1]); c2.setStyle(c2.getStyle() + " -fx-background-color:" + bg + ";");
                Label c3 = liveVesselTableCell(latStr, false, liveColWidths[2]); c3.setStyle(c3.getStyle() + " -fx-background-color:" + bg + ";");
                Label c4 = liveVesselTableCell(lngStr, false, liveColWidths[3]); c4.setStyle(c4.getStyle() + " -fx-background-color:" + bg + ";");
                Label c5 = liveVesselTableCell(port, false, liveColWidths[4]); c5.setStyle(c5.getStyle() + " -fx-background-color:" + bg + ";");
                Label c6 = liveVesselTableCell(dest, false, liveColWidths[5]); c6.setStyle(c6.getStyle() + " -fx-background-color:" + bg + ";");
                
                HBox c7 = liveVesselStatusCell(status, liveColWidths[6], isAlt);
                
                Label c8 = liveVesselTableCell(spd, false, liveColWidths[7]); c8.setStyle(c8.getStyle() + " -fx-background-color:" + bg + ";");
                Label c9 = liveVesselTableCell(crs, false, liveColWidths[8]); c9.setStyle(c9.getStyle() + " -fx-background-color:" + bg + ";");
                Label c10 = liveVesselTableCell(eta, false, liveColWidths[9]); c10.setStyle(c10.getStyle() + " -fx-background-color:" + bg + ";");
                Label c11 = liveVesselTableCell(lastUp, false, liveColWidths[10]); c11.setStyle(c11.getStyle() + " -fx-background-color:" + bg + ";");

                table.add(c1, 0, rowIdx);
                table.add(c2, 1, rowIdx);
                table.add(c3, 2, rowIdx);
                table.add(c4, 3, rowIdx);
                table.add(c5, 4, rowIdx);
                table.add(c6, 5, rowIdx);
                table.add(c7, 6, rowIdx);
                table.add(c8, 7, rowIdx);
                table.add(c9, 8, rowIdx);
                table.add(c10, 9, rowIdx);
                table.add(c11, 10, rowIdx);
                rowIdx++;
            }
        }

        VBox accordionCard = createCollapsibleCard("🚢", "Live Vessel Report", "Real-time vessel position, speed, and course telemetry", preview, horizontalScroll(table), vesselReportExpanded);
        liveVesselReportCard.getChildren().add(accordionCard);
    }

    private Label liveVesselTableCell(String text, boolean isHeader, double minWidth) {
        Label lbl = new Label(text);
        lbl.setMinWidth(minWidth);
        lbl.setPadding(new Insets(12, 16, 12, 16));
        lbl.setAlignment(Pos.CENTER_LEFT);
        if (isHeader) {
            lbl.setMinHeight(46);
            lbl.setStyle(
                    "-fx-text-fill:#0f172a;" +
                    "-fx-font-size:13;" +
                    "-fx-font-weight:bold;" +
                    "-fx-background-color:#f8fafc;" +
                    "-fx-border-color:#e2e8f0;" +
                    "-fx-border-width:0 0 1 0;");
        } else {
            lbl.setMinHeight(50);
            lbl.setStyle(
                    "-fx-text-fill:#334155;" +
                    "-fx-font-size:13;" +
                    "-fx-border-color:#f1f5f9;" +
                    "-fx-border-width:0 0 1 0;");
        }
        lbl.setMaxWidth(Double.MAX_VALUE);
        return lbl;
    }

    private HBox liveVesselStatusCell(String status, double minWidth, boolean isAlt) {
        Label badge = customStatusBadge(status);
        HBox cell = new HBox(badge);
        cell.setMinWidth(minWidth);
        cell.setMinHeight(50);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 16, 0, 16));
        cell.setStyle(
                "-fx-background-color:" + (isAlt ? "#f8fafc" : "#ffffff") + ";" +
                "-fx-border-color:#f1f5f9; -fx-border-width:0 0 1 0;");
        return cell;
    }

    // ==========================================
    // 3. TODAY'S VESSEL MOVEMENT (CLICK-TO-EXPAND)
    // ==========================================
    private void buildVesselMovementSection() {
        vesselMovementCard.getChildren().clear();

        Collection<VesselData> vessels = vesselManager.getAllVessels();
        int arrCount = 0, depCount = 0, portCount = 0;

        VBox arrCol = createMovementBox("ARRIVALS", "#0ea5e9");
        VBox depCol = createMovementBox("DEPARTURES", "#6366f1");
        VBox inPortCol = createMovementBox("CURRENTLY IN PORT", "#16a34a");

        for (VesselData v : vessels) {
            String name = (v.name != null && !v.name.isBlank()) ? v.name : "Vessel #" + v.mmsi;
            String port = matchNearestPort(v.latitude, v.longitude);
            String cat = categorizeVesselStatus(v);

            if ("Arriving".equals(cat) && arrCount < 5) {
                arrCol.getChildren().add(movementItemRow(name, port, "ETA: " + (v.timestamp != null && v.timestamp.length() > 5 ? v.timestamp.substring(0, 5) : "14:15"), "Arriving", "#0ea5e9", "#e0f2fe"));
                arrCount++;
            } else if ("Departing".equals(cat) && depCount < 5) {
                depCol.getChildren().add(movementItemRow(name, port, "Dep: " + nowTime(), "Dest: " + (v.destination != null && !v.destination.isBlank() ? v.destination : "Open Sea"), "#6366f1", "#eef2ff"));
                depCount++;
            } else if ("Currently In Port".equals(cat) && portCount < 5) {
                inPortCol.getChildren().add(movementItemRow(name, port, "Berth Occupied", "Updated: " + nowTime(), "#16a34a", "#ecfdf5"));
                portCount++;
            }
        }

        if (arrCount == 0) arrCol.getChildren().add(mutedLabel("No inbound arrivals currently detected."));
        if (depCount == 0) depCol.getChildren().add(mutedLabel("No outbound departures currently recorded."));
        if (portCount == 0) inPortCol.getChildren().add(mutedLabel("No vessels currently docked in port."));

        HBox columns = new HBox(12);
        HBox.setHgrow(arrCol, Priority.ALWAYS);
        HBox.setHgrow(depCol, Priority.ALWAYS);
        HBox.setHgrow(inPortCol, Priority.ALWAYS);
        columns.getChildren().addAll(arrCol, depCol, inPortCol);

        // Preview Bar (when collapsed)
        HBox preview = new HBox(10);
        preview.setAlignment(Pos.CENTER_LEFT);
        preview.setPadding(new Insets(4, 0, 4, 0));
        Label prevText = new Label("⚓ " + arrCount + " Inbound Arrivals · " + depCount + " Outbound Departures · " + portCount + " Docked In Port · Click header to expand details");
        prevText.setStyle("-fx-text-fill:#64748b; -fx-font-size:13; -fx-font-weight:bold;");
        preview.getChildren().add(prevText);

        VBox accordionCard = createCollapsibleCard("⚓", "Today's Vessel Movement", "Inbound arrivals, outbound departures, and docked vessels", preview, columns, vesselMovementExpanded);
        vesselMovementCard.getChildren().add(accordionCard);
    }

    private VBox createMovementBox(String headerTitle, String color) {
        Label h = new Label(headerTitle);
        h.setStyle("-fx-text-fill:" + color + "; -fx-font-size:13; -fx-font-weight:bold;");

        VBox box = new VBox(8, h);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color:#fafcff; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");
        return box;
    }

    private HBox movementItemRow(String vName, String port, String detail1, String detail2, String badgeColor, String badgeBg) {
        Label nameLbl = new Label(vName);
        nameLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");
        Label portLbl = new Label("📍 " + port + " • " + detail1);
        portLbl.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        VBox info = new VBox(3, nameLbl, portLbl);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label badge = new Label(detail2);
        badge.setStyle("-fx-text-fill:" + badgeColor + "; -fx-background-color:" + badgeBg + "; -fx-background-radius:4; -fx-padding:3 8; -fx-font-size:11; -fx-font-weight:bold;");

        HBox row = new HBox(8, info, space, badge);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setStyle("-fx-background-color:white; -fx-border-color:#f1f5f9; -fx-border-radius:6; -fx-background-radius:6;");
        return row;
    }

    private Label mutedLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12; -fx-padding:6 0;");
        return l;
    }

    // ==========================================
    // 4. LIVE PORT STATUS (CLICK-TO-EXPAND)
    // ==========================================
    private void buildLivePortReport() {
        livePortStatusCard.getChildren().clear();

        List<AdminDataStore.Port> approvedPorts = store.ports.stream()
                .filter(p -> "APPROVED".equalsIgnoreCase(p.approvalStatus) && !"INACTIVE".equalsIgnoreCase(p.status))
                .toList();

        // Preview Bar (when collapsed)
        HBox preview = new HBox(10);
        preview.setAlignment(Pos.CENTER_LEFT);
        preview.setPadding(new Insets(4, 0, 4, 0));
        Label prevText = new Label("🏢 " + approvedPorts.size() + " Active Approved Ports · Real-time berth usage & capacity · Click header to expand full port status");
        prevText.setStyle("-fx-text-fill:#64748b; -fx-font-size:13; -fx-font-weight:bold;");
        preview.getChildren().add(prevText);

        GridPane table = new GridPane();
        table.setHgap(0); table.setVgap(0);
        table.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        String[] headers = {"Port Name", "Location", "Port Status", "Vessels In Port", "Arrivals", "Departures", "Berth Usage", "Capacity", "Last Update"};
        for (int i = 0; i < headers.length; i++) {
            table.add(customTableCell(headers[i], true), i, 0);
        }

        if (approvedPorts.isEmpty()) {
            Label empty = new Label("No active approved ports available.");
            empty.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13; -fx-padding:14;");
            table.add(empty, 0, 1, 9, 1);
        } else {
            int rowIdx = 1;
            for (AdminDataStore.Port p : approvedPorts) {
                Label c1 = customTableCell(p.name, false); c1.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");
                Label c2 = customTableCell(p.location, false);
                Label c3 = customStatusBadge(p.status);
                Label c4 = customTableCell(String.valueOf(p.occupiedBerths()), false);
                Label c5 = customTableCell(String.valueOf(Math.max(1, p.vessels / 3)), false);
                Label c6 = customTableCell(String.valueOf(Math.max(1, p.vessels / 4)), false);
                Label c7 = customTableCell(p.occupiedBerths() + " / " + p.berths + " berths", false);
                Label c8 = customTableCell(p.capacity, false);
                Label c9 = customTableCell(nowTime(), false);

                String bg = (rowIdx % 2 == 0) ? "#f8fafc" : "#ffffff";
                for (Node cell : List.of(c1, c2, c3, c4, c5, c6, c7, c8, c9)) {
                    cell.setStyle(cell.getStyle() + "; -fx-background-color:" + bg + ";");
                }

                table.add(c1, 0, rowIdx);
                table.add(c2, 1, rowIdx);
                table.add(c3, 2, rowIdx);
                table.add(c4, 3, rowIdx);
                table.add(c5, 4, rowIdx);
                table.add(c6, 5, rowIdx);
                table.add(c7, 6, rowIdx);
                table.add(c8, 7, rowIdx);
                table.add(c9, 8, rowIdx);
                rowIdx++;
            }
        }

        VBox accordionCard = createCollapsibleCard("🏢", "Live Port Status", "Real-time berth utilization, capacity, and operational status", preview, table, portStatusExpanded);
        livePortStatusCard.getChildren().add(accordionCard);
    }

    // ==========================================
    // 5. LIVE INSPECTION & COMPLIANCE (CLICK-TO-EXPAND)
    // ==========================================
    private void buildInspectionComplianceSection() {
        inspectionComplianceCard.getChildren().clear();

        int pendingInsp = store.pendingUsers() + store.pendingPortRequestsCount();
        int completedInsp = store.ports.size() + store.users.size();
        int highPri = Math.max(1, (int) store.ports.stream().filter(p -> "Busy".equalsIgnoreCase(p.status)).count());
        int pendingCustoms = store.pendingUsers();
        int pendingSafety = store.pendingPortRequestsCount();

        // Preview Bar (when collapsed)
        HBox preview = new HBox(10);
        preview.setAlignment(Pos.CENTER_LEFT);
        preview.setPadding(new Insets(4, 0, 4, 0));
        Label prevText = new Label("📋 " + pendingInsp + " Pending Inspections · " + highPri + " High Priority Audits · " + completedInsp + " Verified · Click header to expand compliance report");
        prevText.setStyle("-fx-text-fill:#64748b; -fx-font-size:13; -fx-font-weight:bold;");
        preview.getChildren().add(prevText);

        GridPane table = new GridPane();
        table.setHgap(0); table.setVgap(0);
        table.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        String[] headers = {"Compliance Category", "Total Count", "Priority Level", "Responsible Department", "Status", "Last Audit"};
        for (int i = 0; i < headers.length; i++) {
            table.add(customTableCell(headers[i], true), i, 0);
        }

        Object[][] data = {
                {"Pending Inspections", pendingInsp, "HIGH", "Port Control & Security", "Pending Review", nowTime()},
                {"Completed Inspections", completedInsp, "NORMAL", "Maritime Compliance", "Verified", nowTime()},
                {"High Priority Audits", highPri, "CRITICAL", "Customs Enforcement", "In Progress", nowTime()},
                {"Pending Customs Clearance", pendingCustoms, "HIGH", "Customs & Border Control", "Under Review", nowTime()},
                {"Pending Safety Checks", pendingSafety, "MEDIUM", "Safety & Port Authority", "Scheduled", nowTime()}
        };

        for (int r = 0; r < data.length; r++) {
            Label c1 = customTableCell(String.valueOf(data[r][0]), false); c1.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");
            Label c2 = customTableCell(String.valueOf(data[r][1]), false);
            Label c3 = customStatusBadge(String.valueOf(data[r][2]));
            Label c4 = customTableCell(String.valueOf(data[r][3]), false);
            Label c5 = customTableCell(String.valueOf(data[r][4]), false);
            Label c6 = customTableCell(String.valueOf(data[r][5]), false);

            String bg = (r % 2 == 0) ? "#f8fafc" : "#ffffff";
            for (Node cell : List.of(c1, c2, c3, c4, c5, c6)) {
                cell.setStyle(cell.getStyle() + "; -fx-background-color:" + bg + ";");
            }

            table.add(c1, 0, r + 1);
            table.add(c2, 1, r + 1);
            table.add(c3, 2, r + 1);
            table.add(c4, 3, r + 1);
            table.add(c5, 4, r + 1);
            table.add(c6, 5, r + 1);
        }

        VBox accordionCard = createCollapsibleCard("📋", "Live Inspection & Compliance", "Customs clearance, safety compliance, and audit queues", preview, table, complianceExpanded);
        inspectionComplianceCard.getChildren().add(accordionCard);
    }

    // ==========================================
    // REUSABLE COLLAPSIBLE ACCORDION HELPER
    // ==========================================
    private VBox createCollapsibleCard(String titleIcon, String titleText, String subtitleText, Node previewNode, Node contentNode, boolean[] stateTracker) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 10, 0, 0, 2);"
        );

        Label iconLbl = new Label(titleIcon);
        iconLbl.setStyle("-fx-font-size:18; -fx-text-fill:" + BLUE + ";");

        Label titleLbl = new Label(titleText);
        titleLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");

        Label subLbl = new Label(" • " + subtitleText);
        subLbl.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");

        HBox titleBox = new HBox(8, iconLbl, titleLbl, subLbl);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label toggleBtn = new Label(stateTracker[0] ? "▲ Collapse" : "▼ Expand Report");
        toggleBtn.setStyle(
                "-fx-background-color:#eff6ff;" +
                "-fx-text-fill:" + BLUE + ";" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:6 12;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );

        HBox headerBar = new HBox(12, titleBox, space, toggleBtn);
        headerBar.setAlignment(Pos.CENTER_LEFT);
        headerBar.setStyle("-fx-cursor:hand;");

        contentNode.setVisible(stateTracker[0]);
        contentNode.setManaged(stateTracker[0]);

        if (previewNode != null) {
            previewNode.setVisible(!stateTracker[0]);
            previewNode.setManaged(!stateTracker[0]);
        }

        headerBar.setOnMouseClicked(e -> {
            stateTracker[0] = !stateTracker[0];
            toggleBtn.setText(stateTracker[0] ? "▲ Collapse" : "▼ Expand Report");
            contentNode.setVisible(stateTracker[0]);
            contentNode.setManaged(stateTracker[0]);
            if (previewNode != null) {
                previewNode.setVisible(!stateTracker[0]);
                previewNode.setManaged(!stateTracker[0]);
            }
        });

        if (previewNode != null) {
            card.getChildren().addAll(headerBar, previewNode, contentNode);
        } else {
            card.getChildren().addAll(headerBar, contentNode);
        }

        return card;
    }

    // ==========================================
    // TOAST NOTIFICATION BANNER
    // ==========================================
    private HBox createToastBanner() {
        Label icon = new Label("ℹ");
        icon.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:16; -fx-font-weight:bold;");

        toastMessage = new Label("Showing live operational reports across all active ports and vessels.");
        toastMessage.setStyle("-fx-text-fill:#334155; -fx-font-size:14; -fx-font-weight:bold;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label close = new Label("✕");
        close.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:14; -fx-cursor:hand;");
        close.setOnMouseClicked(e -> toastBanner.setVisible(false));

        HBox toast = new HBox(10, icon, toastMessage, space, close);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setPadding(new Insets(10, 16, 10, 16));
        toast.setStyle(
                "-fx-background-color:#eff6ff;" +
                "-fx-border-color:#bfdbfe;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;");
        return toast;
    }

    private void showToast(String msg, boolean isSuccess) {
        if (toastMessage != null && toastBanner != null) {
            toastMessage.setText(msg);
            toastBanner.setStyle(
                    "-fx-background-color:" + (isSuccess ? "#ecfdf5" : "#eff6ff") + ";" +
                            "-fx-border-color:" + (isSuccess ? "#a7f3d0" : "#bfdbfe") + ";" +
                            "-fx-border-radius:8;" +
                            "-fx-background-radius:8;");
            toastBanner.setVisible(true);
        }
    }

    private Label customTableCell(String text, boolean isHeader) {
        Label lbl = new Label(text);
        if (isHeader) {
            lbl.setStyle(
                    "-fx-text-fill:#0f172a;" +
                    "-fx-font-size:13;" +
                    "-fx-font-weight:bold;" +
                    "-fx-padding:10 14;" +
                    "-fx-background-color:#f8fafc;" +
                    "-fx-border-color:#e2e8f0;" +
                    "-fx-border-width:0 0 1 0;");
            lbl.setMinHeight(44);
        } else {
            lbl.setStyle(
                    "-fx-text-fill:#334155;" +
                    "-fx-font-size:13;" +
                    "-fx-padding:10 14;" +
                    "-fx-border-color:#f1f5f9;" +
                    "-fx-border-width:0 0 1 0;");
            lbl.setMinHeight(48);
        }
        lbl.setMaxWidth(Double.MAX_VALUE);
        return lbl;
    }

    private Label customStatusBadge(String status) {
        Label badge = new Label(status);
        String textCol = "#16a34a";
        String bgCol = "#ecfdf5";

        if ("Processing".equalsIgnoreCase(status) || "Arriving".equalsIgnoreCase(status) || "In Transit".equalsIgnoreCase(status) || "HIGH".equalsIgnoreCase(status)) {
            textCol = "#0b63ff";
            bgCol = "#eff6ff";
        } else if ("Scheduled".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status) || "Anchored".equalsIgnoreCase(status) || "MEDIUM".equalsIgnoreCase(status)) {
            textCol = "#d97706";
            bgCol = "#fef3c7";
        } else if ("Failed".equalsIgnoreCase(status) || "CRITICAL".equalsIgnoreCase(status) || "Departing".equalsIgnoreCase(status)) {
            textCol = "#ef4444";
            bgCol = "#fee2e2";
        }

        badge.setStyle(
                "-fx-text-fill:" + textCol + ";" +
                        "-fx-background-color:" + bgCol + ";" +
                        "-fx-background-radius:5;" +
                        "-fx-padding:3 8;" +
                        "-fx-font-size:11;" +
                        "-fx-font-weight:bold;");
        return badge;
    }

    private ComboBox<String> combo(String labelText, String defaultValue, String... options) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().add(defaultValue);
        cb.getItems().addAll(options);
        cb.setValue(defaultValue);
        cb.setStyle(
                "-fx-background-color:#f8fafc;" +
                        "-fx-border-color:#e2e8f0;" +
                        "-fx-border-radius:6;" +
                        "-fx-background-radius:6;" +
                        "-fx-padding:3 6;" +
                        "-fx-font-size:12;");
        return cb;
    }

    private void showGenerateReportDialog(Window owner) {
        Stage modal = new Stage();
        if (owner != null)
            modal.initOwner(owner);
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Generate Custom Report");

        VBox root = new VBox(12);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color:white;");

        Label title = new Label("Generate Operations Report");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16; -fx-font-weight:bold;");

        ComboBox<String> typeCb = combo("Report Type", "Port Operations Summary", "Weekly Vessel Traffic",
                "Cargo Throughput & Tonnage", "Berth Occupancy Index", "Customs Clearance & Inspections");
        typeCb.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> rangeCb = combo("Date Range", "This Week", "Today", "This Week", "This Month",
                "Custom Range");
        rangeCb.setMaxWidth(Double.MAX_VALUE);

        TextField authorField = new TextField(store.adminName);
        authorField.setPromptText("Generated By (Author)");
        authorField.setStyle(
                "-fx-background-color:#f8fafc;" +
                        "-fx-border-color:#e2e8f0;" +
                        "-fx-border-radius:6;" +
                        "-fx-background-radius:6;" +
                        "-fx-padding:6 10;");

        Button submitBtn = primaryButton("Generate Now");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            String rName = typeCb.getValue() == null ? "Custom Report" : typeCb.getValue();
            String rPeriod = rangeCb.getValue() == null ? "This Week" : rangeCb.getValue();
            String author = authorField.getText().isBlank() ? store.adminName : authorField.getText().trim();
            String dateStr = store.now();

            store.reports.add(0, new AdminDataStore.ReportRow(rName, rPeriod, "Ready", author, dateStr));
            modal.close();
            showToast("Successfully generated report: '" + rName + "'", true);
        });

        root.getChildren().addAll(title, new Label("Report Type:"), typeCb, new Label("Date Range:"), rangeCb,
                new Label("Author:"), authorField, new Separator(), submitBtn);

        Scene scene = new Scene(root, 400, 360);
        modal.setScene(scene);
        modal.showAndWait();
    }
}
