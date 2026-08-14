package com.projectui.view.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

public class DashboardPage extends AdminTheme {

    private final AdminDataStore store = AdminDataStore.getInstance();
    private Runnable addPortAction = () -> {};
    private Runnable addUserAction = () -> {};
    private Runnable alertsAction = () -> {};
    private Runnable reportAction = () -> {};

    public Parent getRoot() {
        return getRoot(() -> {}, () -> {}, () -> {}, () -> {});
    }

    public Parent getRoot(Runnable addPortAction, Runnable addUserAction, Runnable alertsAction, Runnable reportAction) {
        this.addPortAction = addPortAction;
        this.addUserAction = addUserAction;
        this.alertsAction = alertsAction;
        this.reportAction = reportAction;
        VBox page = pageContent();
        page.setSpacing(10);

        int pendingInspections = 14;
        HBox kpis = new HBox(12,
            kpiCard("⚓", "Total Ports", String.valueOf(store.ports.size()), "All Registered", "#1d4ed8", "#eaf3ff"),
            kpiCard("⬟", "Active Ports", String.valueOf(store.activePorts()), "Operational", "#0f9f6e", "#e7f8ef"),
            kpiCard("▴", "Active Vessels", String.valueOf(store.activeVessels()), "Currently in Port", "#6d4aff", "#f0edff"),
            kpiCard("▥", "Cargo Handled", String.format("%,d MT", store.cargoHandled()), "Today", "#f97316", "#fff1e7"),
            kpiCard("!", "Pending Inspections", String.valueOf(pendingInspections), "Require Attention", "#e43f5a", "#ffecef")
        );
        kpis.getChildren().forEach(n -> {
            HBox.setHgrow(n, Priority.ALWAYS);
            if (n instanceof VBox) {
                ((VBox)n).setMaxWidth(Double.MAX_VALUE);
            }
        });

        HBox mainRow = new HBox(12);
        Region ops = portLocationMapCard();
        Region st = statusCard();
        ops.prefWidthProperty().bind(mainRow.widthProperty().multiply(0.68));
        st.prefWidthProperty().bind(mainRow.widthProperty().multiply(0.32));
        HBox.setHgrow(ops, Priority.ALWAYS);
        HBox.setHgrow(st, Priority.NEVER);
        ops.setMaxWidth(Double.MAX_VALUE);
        st.setMaxHeight(Double.MAX_VALUE);
        mainRow.getChildren().addAll(ops, st);

        HBox bottomRow = new HBox(12);
        Region alerts = alertsCard();
        Region recent = recentActivityCard();
        Region cargo = cargoStatsCard();
        HBox.setHgrow(alerts, Priority.ALWAYS);
        HBox.setHgrow(recent, Priority.ALWAYS);
        HBox.setHgrow(cargo, Priority.ALWAYS);
        alerts.setMaxWidth(Double.MAX_VALUE);
        recent.setMaxWidth(Double.MAX_VALUE);
        cargo.setMaxWidth(Double.MAX_VALUE);
        bottomRow.getChildren().addAll(alerts, recent, cargo);
        bottomRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        page.getChildren().addAll(
            kpis,
            mainRow,
            bottomRow
        );

        return page;
    }

    private VBox portLocationMapCard() {
        Label t = new Label("Port Network Overview");
        t.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16; -fx-font-weight:bold;");
        Label sub = new Label("Real-time status of port operations across India");
        sub.setStyle("-fx-text-fill:#475569; -fx-font-size:12;");
        VBox copy = new VBox(2, t, sub);
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        Button details = secondaryButton("View Details");
        details.setOnAction(event -> addPortAction.run());
        Label expand = new Label("↗");
        expand.setStyle("-fx-text-fill:#0f172a; -fx-font-size:15; -fx-font-weight:bold;");
        HBox top = new HBox(12, copy, space, details, expand);
        top.setAlignment(Pos.CENTER_LEFT);

        Pane map = new Pane();
        map.setPrefSize(710, 315);
        map.setMinHeight(315);
        map.setStyle("-fx-background-color:linear-gradient(to bottom,#dff5ff,#b9e4fb 55%,#9fd6f4); -fx-background-radius:8; -fx-border-color:#cfe3f5; -fx-border-radius:8;");

        Rectangle water = new Rectangle(710, 315);
        water.setArcWidth(16);
        water.setArcHeight(16);
        water.setStyle("-fx-fill:linear-gradient(to bottom,#dff7ff,#b5e4fa 58%,#94d0ef);");

        map.getChildren().add(water);
        for (int x = 56; x < 710; x += 68) {
            Line grid = new Line(x, 0, x - 105, 315);
            grid.setStyle("-fx-stroke:rgba(255,255,255,0.24); -fx-stroke-width:1;");
            map.getChildren().add(grid);
        }

        SVGPath india = new SVGPath();
        india.setContent("M332 12 L385 23 L446 54 L511 55 L559 84 L602 113 L638 152 L619 192 L575 207 L530 224 L496 266 L439 292 L392 284 L356 242 L307 226 L260 244 L216 235 L192 197 L158 173 L125 148 L103 112 L133 82 L190 62 L247 43 Z");
        india.setStyle("-fx-fill:linear-gradient(to bottom,#f8f7f1,#eee9dc); -fx-stroke:#c9d7cf; -fx-stroke-width:1.5;");
        india.setLayoutX(28);
        india.setLayoutY(0);

        SVGPath coast = new SVGPath();
        coast.setContent("M190 62 C164 93 148 135 158 173 C172 225 252 246 307 226 C337 245 361 271 392 284");
        coast.setStyle("-fx-fill:transparent; -fx-stroke:#80b8dc; -fx-stroke-width:2;");
        coast.setLayoutX(28);
        coast.setLayoutY(0);

        Line route1 = routeLine(217, 128, 398, 172);
        Line route2 = routeLine(398, 172, 516, 72);
        Line route3 = routeLine(398, 172, 365, 245);
        Line route4 = routeLine(217, 128, 365, 245);

        Label arabian = new Label("Arabian Sea");
        arabian.setStyle("-fx-text-fill:#0b63b6; -fx-font-size:12;");
        arabian.setLayoutX(80);
        arabian.setLayoutY(255);
        Label bay = new Label("Bay of Bengal");
        bay.setStyle("-fx-text-fill:#0b63b6; -fx-font-size:12;");
        bay.setLayoutX(555);
        bay.setLayoutY(248);

        Button zoomIn = mapButton("+");
        Button zoomOut = mapButton("-");
        Button reset = mapButton("◎");
        VBox controls = new VBox(8, zoomIn, zoomOut, reset);
        controls.setLayoutX(12);
        controls.setLayoutY(20);

        VBox info = mapInfoPanel(firstMappedPort());
        info.setLayoutX(498);
        info.setLayoutY(18);
        info.setVisible(false);
        info.setMouseTransparent(true);

        map.getChildren().addAll(india, coast, route1, route2, route3, route4, arabian, bay, controls);
        int mappedPorts = 0;
        for (AdminDataStore.Port port : store.ports) {
            if (hasKnownMapPosition(port.location)) {
                map.getChildren().add(portMarker(port, info));
                mappedPorts++;
            }
        }
        if (mappedPorts == 0) {
            Label emptyMap = new Label("No mapped Indian port locations available");
            emptyMap.setStyle("-fx-text-fill:#64748b; -fx-font-size:12; -fx-font-weight:bold;");
            emptyMap.setLayoutX(245);
            emptyMap.setLayoutY(158);
            map.getChildren().add(emptyMap);
        }
        map.getChildren().add(info);

        final double[] scale = {1.0};
        zoomIn.setOnAction(event -> {
            scale[0] = Math.min(1.12, scale[0] + 0.04);
            map.setScaleX(scale[0]);
            map.setScaleY(scale[0]);
        });
        zoomOut.setOnAction(event -> {
            scale[0] = Math.max(0.96, scale[0] - 0.04);
            map.setScaleX(scale[0]);
            map.setScaleY(scale[0]);
        });
        reset.setOnAction(event -> {
            scale[0] = 1.0;
            map.setScaleX(1);
            map.setScaleY(1);
        });

        HBox legend = new HBox(14,
                legendBadge("● Operational", "#16a34a"),
                legendBadge("● Busy / High Congestion", "#e43f5a"),
                legendBadge("● Maintenance", BLUE),
                legendBadge("● Inactive", "#94a3b8")
        );
        legend.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setStyle(cardStyle());
        card.getChildren().addAll(top, new StackPane(map), legend);
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private HBox portMarker(AdminDataStore.Port port, VBox info) {
        String congestion = congestionStatus(port);
        String color = markerColor(port.status, congestion);

        Circle pin = new Circle(7);
        pin.setStyle("-fx-fill:" + color + "; -fx-stroke:white; -fx-stroke-width:2;");

        Label status = new Label(markerLabel(port.status, congestion));
        status.setStyle("-fx-text-fill:" + color + "; -fx-font-size:10; -fx-font-weight:bold;");

        Label name = new Label(shortPortName(port.name) + " Port");
        name.setStyle("-fx-text-fill:#0f172a; -fx-font-size:11; -fx-font-weight:bold;");

        VBox copy = new VBox(0, name, status);
        HBox marker = new HBox(6, pin, copy);
        marker.setAlignment(Pos.CENTER_LEFT);
        marker.setPadding(new Insets(7, 10, 7, 10));
        marker.setStyle("-fx-background-color:rgba(255,255,255,0.96); -fx-background-radius:8; -fx-border-color:#d6e2ef; -fx-border-radius:8; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.16), 10, 0, 0, 3); -fx-cursor:hand;");
        double mx = portMapX(port.location);
        double my = portMapY(port.location);
        marker.setLayoutX(mx);
        marker.setLayoutY(my);

        marker.setOnMouseEntered(event -> {
            updateMapInfo(info, port);
            positionMapInfo(info, mx, my);
            info.setVisible(true);
        });
        marker.setOnMouseExited(event -> {
            info.setVisible(false);
        });
        return marker;
    }

    private void positionMapInfo(VBox info, double markerX, double markerY) {
        double cardWidth = 230;
        double cardHeight = 170;

        double targetX = markerX + 130;
        double targetY = markerY - 20;

        if (markerX > 380) {
            targetX = markerX - cardWidth - 15;
        }

        if (targetX < 10) targetX = 10;
        if (targetX + cardWidth > 700) targetX = 700 - cardWidth;
        if (targetY < 10) targetY = 10;
        if (targetY + cardHeight > 305) targetY = 305 - cardHeight;

        info.setLayoutX(targetX);
        info.setLayoutY(targetY);
    }

    private Line routeLine(double startX, double startY, double endX, double endY) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStyle("-fx-stroke:#2d8cff; -fx-stroke-width:1; -fx-stroke-dash-array:5 4; -fx-opacity:0.58;");
        return line;
    }

    private Button mapButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:white; -fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold; -fx-background-radius:6; -fx-border-color:#dbe6f2; -fx-border-radius:6; -fx-min-width:30; -fx-min-height:30; -fx-padding:0; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.16), 7, 0, 0, 2);");
        return button;
    }

    private VBox mapInfoPanel(AdminDataStore.Port port) {
        VBox panel = new VBox(6);
        panel.setPrefWidth(230);
        panel.setPadding(new Insets(12));
        panel.setStyle("-fx-background-color:rgba(255,255,255,0.96); -fx-background-radius:12; -fx-border-color:#dbeafe; -fx-border-radius:12; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.10), 10, 0, 0, 3);");
        updateMapInfo(panel, port);
        return panel;
    }

    private void updateMapInfo(VBox panel, AdminDataStore.Port port) {
        panel.getChildren().clear();
        if (port == null) {
            Label empty = new Label("No port data available");
            empty.setStyle("-fx-text-fill:#64748b; -fx-font-size:12; -fx-font-weight:bold;");
            panel.getChildren().add(empty);
            return;
        }

        String congestion = congestionStatus(port);
        String color = markerColor(port.status, congestion);
        Label name = new Label(port.name);
        name.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14; -fx-font-weight:bold;");
        Label location = new Label(port.location + regionSuffix(port.location));
        location.setStyle("-fx-text-fill:#64748b; -fx-font-size:11;");
        Label state = new Label("● " + port.status + ("Normal".equals(congestion) ? "" : " / " + congestion));
        state.setStyle("-fx-text-fill:" + color + "; -fx-font-size:12; -fx-font-weight:bold;");

        panel.getChildren().addAll(
                name,
                location,
                state,
                mapInfoRow("Active Vessels", port.vessels + " vessels"),
                mapInfoRow("Berth Usage", port.occupiedBerths() + " / " + port.berths + " berths"),
                mapInfoRow("Port Capacity", port.capacity),
                mapInfoRow("Maintenance", "Clear".equalsIgnoreCase(port.maintenance) ? "No Maintenance" : port.maintenance),
                mapInfoRow("Congestion", congestion)
        );
    }

    private HBox mapInfoRow(String labelText, String value) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill:#64748b; -fx-font-size:11; -fx-font-weight:bold;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill:#111827; -fx-font-size:11; -fx-font-weight:bold;");
        valueLabel.setWrapText(true);
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox row = new HBox(8, label, space, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label legendBadge(String text, String color) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:" + color + "; -fx-font-size:11; -fx-font-weight:bold;");
        return label;
    }

    private String congestionStatus(AdminDataStore.Port port) {
        if ("Maintenance".equals(port.status)) return "Maintenance";
        if ("Closed".equals(port.status) || "Restricted".equals(port.status) || "Inactive".equals(port.status) || "Deactivated".equals(port.status)) return "Inactive";
        if ("High Congestion".equals(port.status) || port.vessels >= port.berths * 0.85) return "High Congestion";
        if ("Busy".equals(port.status) || port.vessels >= port.berths * 0.7) return "Busy";
        return "Normal";
    }

    private String markerColor(String status, String congestion) {
        if ("Maintenance".equals(status) || "Maintenance".equals(congestion)) return BLUE;
        if ("Inactive".equals(congestion)) return "#94a3b8";
        if ("Busy".equals(status) || "Busy".equals(congestion) || "High Congestion".equals(status) || "High Congestion".equals(congestion)) return "#e43f5a";
        return "#16a34a";
    }

    private String markerLabel(String status, String congestion) {
        if ("Maintenance".equals(status)) return "Maintenance";
        if ("Inactive".equals(congestion)) return "Inactive";
        if ("High Congestion".equals(congestion)) return "High Congestion";
        if ("Busy".equals(status) || "Busy".equals(congestion)) return "Busy";
        return "Operational";
    }

    private double portMapX(String location) {
        String value = location == null ? "" : location.toLowerCase();
        if (value.contains("mumbai")) return 215;
        if (value.contains("chennai")) return 365;
        if (value.contains("kolkata")) return 516;
        if (value.contains("visakhapatnam") || value.contains("vizag")) return 398;
        if (value.contains("mundra")) return 158;
        return 320;
    }

    private double portMapY(String location) {
        String value = location == null ? "" : location.toLowerCase();
        if (value.contains("mumbai")) return 118;
        if (value.contains("chennai")) return 238;
        if (value.contains("kolkata")) return 61;
        if (value.contains("visakhapatnam") || value.contains("vizag")) return 168;
        if (value.contains("mundra")) return 82;
        return 160;
    }

    private AdminDataStore.Port firstMappedPort() {
        for (AdminDataStore.Port port : store.ports) {
            if (hasKnownMapPosition(port.location)) {
                return port;
            }
        }
        return null;
    }

    private boolean hasKnownMapPosition(String location) {
        String value = location == null ? "" : location.toLowerCase();
        return value.contains("mumbai")
                || value.contains("chennai")
                || value.contains("kolkata")
                || value.contains("visakhapatnam")
                || value.contains("vizag")
                || value.contains("mundra");
    }

    private String regionSuffix(String location) {
        String value = location == null ? "" : location.toLowerCase();
        if (value.contains("mumbai")) return ", Maharashtra";
        if (value.contains("chennai")) return ", Tamil Nadu";
        if (value.contains("kolkata")) return ", West Bengal";
        if (value.contains("visakhapatnam") || value.contains("vizag")) return ", Andhra Pradesh";
        if (value.contains("mundra")) return ", Gujarat";
        return "";
    }

    private String shortPortName(String name) {
        return name == null ? "Port" : name.replace("Port of ", "");
    }

    private VBox statusCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(14));
        card.setStyle(cardStyle());
        card.setPrefWidth(320);
        Label title = new Label("Live Port Status");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16; -fx-font-weight:bold;");
        Label link = new Label("View All Ports  →");
        link.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:11; -fx-font-weight:bold;");
        link.setOnMouseClicked(event -> addPortAction.run());
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox top = new HBox(8, title, space, link);
        top.setAlignment(Pos.CENTER_LEFT);

        HBox head = new HBox(8,
                tableHead("Port Name", 0.44),
                tableHead("Live Status", 0.33),
                tableHead("Last Update", 0.23)
        );
        head.setPadding(new Insets(9, 10, 9, 10));
        head.setStyle("-fx-background-color:#f8fafc; -fx-background-radius:7;");

        VBox rows = new VBox(0);
        for (AdminDataStore.Port port : store.ports) {
            String congestion = congestionStatus(port);
            rows.getChildren().add(statusLine(port, markerLabel(port.status, congestion), statusColor(port.status, congestion)));
        }

        HBox summary = new HBox(12,
                statusSummary("Operational", countStatus("Operational"), "#16a34a"),
                statusSummary("Maintenance", countStatus("Maintenance"), BLUE),
                statusSummary("High Congestion", countCongested(), "#e43f5a"),
                statusSummary("Inactive", countInactive(), "#94a3b8")
        );
        summary.setPadding(new Insets(12));
        summary.setStyle("-fx-background-color:#ffffff; -fx-border-color:#e5edf6; -fx-border-radius:8; -fx-background-radius:8;");
        summary.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        card.getChildren().addAll(top, head, rows, summary);
        return card;
    }

    private VBox alertsCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(14));
        card.setStyle(cardStyle());
        HBox top = cardHeader("Operational Alerts", "View All Alerts  →", alertsAction);
        card.getChildren().addAll(
            top,
            alertLine("!", "Port Kolkata", "High congestion detected", "5 mins ago", "#e43f5a"),
            alertLine("⌁", "Port of Vizag", "Crane service in progress", "15 mins ago", BLUE),
            alertLine("▣", "14 Cargo Inspections", "Pending clearance", "25 mins ago", "#f97316"),
            alertLine("⚓", "3 Vessels", "Delayed berth assignment", "40 mins ago", "#6d4aff")
        );
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private VBox recentActivityCard() {
        VBox card = new VBox(11);
        card.setPadding(new Insets(14));
        card.setStyle(cardStyle());
        card.getChildren().addAll(
            cardHeader("Recent Activity", "View All Activity  →", reportAction),
            activityLine("⚓", "Oceanic Star", "Vessel arrived at Port of Mumbai", "11:02 AM", BLUE),
            activityLine("▣", "Sea Voyager", "Cargo loading completed", "10:14 AM", "#16a34a"),
            activityLine("▴", "MSC Atlantis", "Vessel departed from Port of Chennai", "09:48 AM", "#6d4aff"),
            activityLine("⌁", "Crane Maintenance", "Maintenance completed at Port of Vizag", "07:15 AM", BLUE)
        );
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private VBox cargoStatsCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(14));
        card.setStyle(cardStyle());

        int total = store.cargoHandled();
        int containers = Math.round(total * 0.50f);
        int bulk = Math.round(total * 0.32f);
        int minerals = Math.max(0, total - containers - bulk);

        StackPane donut = donutChart(total);
        VBox legend = new VBox(11,
                cargoLine("Containers", containers, total, "#1f74ff"),
                cargoLine("Bulk Cargo", bulk, total, "#f97316"),
                cargoLine("Minerals", minerals, total, "#35b779")
        );
        HBox middle = new HBox(14, donut, legend);
        middle.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(legend, Priority.ALWAYS);

        HBox footer = new HBox(16,
                cargoFooter("+12.5%", "vs yesterday", "#16a34a"),
                sparkline(),
                cargoFooter(String.format("%,d MT", total), "Total Today", "#0f172a")
        );
        footer.setPadding(new Insets(10, 12, 10, 12));
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-background-color:#ffffff; -fx-border-color:#e5edf6; -fx-border-radius:8; -fx-background-radius:8;");

        card.getChildren().addAll(cardHeader("Cargo Statistics", "View Full Report  →", reportAction), middle, footer);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private String statusColor(String status, String congestion) {
        if ("Maintenance".equals(status)) return BLUE;
        if ("Restricted".equals(status) || "Inactive".equals(congestion)) return "#94a3b8";
        if ("High Congestion".equals(congestion) || "Busy".equals(status) || "Busy".equals(congestion)) return "#e43f5a";
        return "#16a34a";
    }

    private VBox upcomingVesselsCard() {
        String[] headers = new String[]{"Vessel", "From", "ETA", "Berth", "Status"};
        String[][] rows = new String[][]{
            {"MSC Virtuosa", "Singapore", "11:00 AM", "B-04", "Arriving"},
            {"Maersk Horizon", "Dubai", "01:30 PM", "B-02", "Scheduled"},
            {"Ever Given", "Rotterdam", "04:00 PM", "B-07", "Scheduled"},
            {"CMA CGM Atlas", "Jebel Ali", "06:45 PM", "B-03", "Scheduled"}
        };

        GridPane table = table(headers, rows);

        VBox card = panelCard("Upcoming Vessels", "Next arrivals and berth allocations");
        card.getChildren().add(table);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private VBox legendItem(String name, String value, String color) {
        Label dot = new Label("  ");
        dot.setStyle("-fx-background-color:" + color + "; -fx-background-radius:6; -fx-min-width:12; -fx-min-height:12;");
        Label n = new Label(name);
        n.setStyle("-fx-text-fill:#111827; -fx-font-size:13; -fx-font-weight:bold;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill:#6b7280; -fx-font-size:12;");
        VBox box = new VBox(3, n, v);
        HBox row = new HBox(8, dot, box);
        VBox wrapper = new VBox(row);
        return wrapper;
    }

    private VBox kpiCard(String icon, String title, String value, String detail, String color, String iconBg) {
        Label iconLabel = new Label(icon);
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setStyle("-fx-background-color:" + iconBg + "; -fx-text-fill:" + color + "; -fx-font-size:22; -fx-font-weight:bold; -fx-background-radius:999; -fx-min-width:52; -fx-min-height:52; -fx-pref-width:52; -fx-pref-height:52;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:28; -fx-font-weight:800;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14; -fx-font-weight:bold;");

        Label detailLabel = new Label(detail);
        detailLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        HBox top = new HBox(14, iconLabel, new VBox(4, titleLabel, valueLabel, detailLabel));
        top.setAlignment(Pos.CENTER_LEFT);

        Label view = new Label("View all  →");
        view.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:12; -fx-font-weight:bold; -fx-cursor:hand;");
        view.setOnMouseClicked(event -> addPortAction.run());

        Region divider = new Region();
        divider.setMinHeight(1);
        divider.setStyle("-fx-background-color:#edf2f7;");

        VBox card = new VBox(10, top, divider, view);
        card.setPadding(new Insets(14, 16, 12, 16));
        card.setStyle(cardStyle());
        card.setPrefWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    @SuppressWarnings("unused")
    private VBox vesselMovementCard() {
        GridPane table = table(
                new String[]{"Vessel", "Port", "Movement", "Status"},
                new String[][]{
                        {"Oceanic Star", "Mumbai", "Arrival", "On Time"},
                        {"Sea Voyager", "Chennai", "Loading", "In Progress"},
                        {"Pacific Trader", "Vizag", "Departure", "Delayed"}
                });

        VBox card = panelCard("Today's Vessel Movement", "Live arrival, loading and departure summary");
        card.setPrefWidth(620);
        card.getChildren().add(table);
        return card;
    }

    @SuppressWarnings("unused")
    private VBox quickActionsCard() {
        ButtonRow addPort = new ButtonRow("Add Port", "Create a registered port", addPortAction);
        ButtonRow addUser = new ButtonRow("Add User", "Register an operations user", addUserAction);
        ButtonRow viewAlerts = new ButtonRow("View Alerts", "Check congestion and delays", alertsAction);
        ButtonRow report = new ButtonRow("Generate Report", "Prepare today's operations report", reportAction);

        VBox card = panelCard("Quick Actions", "Common admin tasks");
        card.setPrefWidth(340);
        card.getChildren().addAll(addPort, addUser, viewAlerts, report);
        return card;
    }

    private Label tableHead(String text, double widthRatio) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:#64748b; -fx-font-size:12; -fx-font-weight:bold;");
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);
        return label;
    }

    private VBox statusSummary(String labelText, int value, String color) {
        Label number = new Label(String.valueOf(value));
        number.setStyle("-fx-text-fill:" + color + "; -fx-font-size:18; -fx-font-weight:bold;");
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill:#334155; -fx-font-size:12;");
        VBox box = new VBox(2, number, label);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private int countStatus(String value) {
        int count = 0;
        for (AdminDataStore.Port port : store.ports) {
            if (value.equals(markerLabel(port.status, congestionStatus(port)))) {
                count++;
            }
        }
        return count;
    }

    private int countCongested() {
        int count = 0;
        for (AdminDataStore.Port port : store.ports) {
            String label = markerLabel(port.status, congestionStatus(port));
            if ("Busy".equals(label) || "High Congestion".equals(label)) {
                count++;
            }
        }
        return count;
    }

    private int countInactive() {
        int count = 0;
        for (AdminDataStore.Port port : store.ports) {
            if ("Inactive".equals(markerLabel(port.status, congestionStatus(port)))) {
                count++;
            }
        }
        return count;
    }

    private HBox statusLine(AdminDataStore.Port port, String status, String color) {
        Label icon = new Label("⚓");
        icon.setStyle("-fx-text-fill:" + color + "; -fx-font-size:16; -fx-font-weight:bold;");

        Label nameLabel = new Label(port.name);
        nameLabel.setStyle("-fx-text-fill:#111827; -fx-font-size:14; -fx-font-weight:bold;");
        Label vessels = new Label(port.vessels + " vessels");
        vessels.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");
        VBox nameBox = new VBox(1, nameLabel, vessels);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label statusLabel = new Label(status);
        statusLabel.setPadding(new Insets(5, 10, 5, 10));
        statusLabel.setStyle("-fx-background-color:" + softBackground(color) + "; -fx-background-radius:8; -fx-text-fill:" + color + "; -fx-font-size:12; -fx-font-weight:bold;");

        Label updated = new Label(lastUpdatedShort(port.lastUpdated));
        updated.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");
        Circle dot = new Circle(3);
        dot.setStyle("-fx-fill:" + color + ";");

        HBox row = new HBox(10, icon, nameBox, space, statusLabel, updated, dot);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 10, 10, 10));
        row.setStyle("-fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return row;
    }

    private String cardStyle() {
        return "-fx-background-color:white; -fx-background-radius:10; -fx-border-color:#dfe7f1; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.07), 13, 0, 0, 3);";
    }

    private HBox cardHeader(String title, String actionText, Runnable action) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        Label actionLabel = new Label(actionText);
        actionLabel.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:13; -fx-font-weight:bold; -fx-cursor:hand;");
        actionLabel.setOnMouseClicked(event -> action.run());
        HBox row = new HBox(8, titleLabel, space, actionLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox alertLine(String iconText, String title, String detail, String time, String color) {
        Label icon = new Label(iconText);
        icon.setAlignment(Pos.CENTER);
        icon.setStyle("-fx-background-color:" + softBackground(color) + "; -fx-text-fill:" + color + "; -fx-font-size:14; -fx-font-weight:bold; -fx-background-radius:999; -fx-min-width:34; -fx-min-height:34;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#111827; -fx-font-size:14; -fx-font-weight:bold;");

        Label detailLabel = new Label(detail);
        detailLabel.setStyle("-fx-text-fill:#747884; -fx-font-size:13;");
        VBox copy = new VBox(2, titleLabel, detailLabel);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");
        Circle dot = new Circle(3);
        dot.setStyle("-fx-fill:" + color + ";");

        HBox row = new HBox(10, icon, copy, space, timeLabel, dot);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(7, 0, 7, 0));
        row.setStyle("-fx-border-color:#edf2f7; -fx-border-width:0 0 1 0;");
        return row;
    }

    @SuppressWarnings("unused")
    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:#05070c; -fx-font-size:22; -fx-font-weight:bold;");
        return label;
    }

    private HBox activityLine(String iconText, String title, String detail, String time, String color) {
        Label icon = new Label(iconText);
        icon.setAlignment(Pos.CENTER);
        icon.setStyle("-fx-background-color:" + softBackground(color) + "; -fx-text-fill:" + color + "; -fx-font-size:14; -fx-font-weight:bold; -fx-background-radius:999; -fx-min-width:28; -fx-min-height:28;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#111827; -fx-font-size:14; -fx-font-weight:bold;");

        Label detailLabel = new Label(detail);
        detailLabel.setStyle("-fx-text-fill:#747884; -fx-font-size:13;");
        VBox copy = new VBox(2, titleLabel, detailLabel);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-text-fill:#334155; -fx-font-size:12;");

        HBox row = new HBox(10, icon, copy, space, timeLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private StackPane donutChart(int total) {
        double radius = 48;
        Arc containers = donutArc(radius, 90, -180, "#1f74ff");
        Arc bulk = donutArc(radius, -90, -115, "#f97316");
        Arc minerals = donutArc(radius, -205, -65, "#35b779");

        Circle hole = new Circle(32);
        hole.setStyle("-fx-fill:white;");

        Label value = new Label(String.format("%,d\nMT", total));
        value.setAlignment(Pos.CENTER);
        value.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14; -fx-font-weight:bold;");

        Label caption = new Label("Total Cargo");
        caption.setStyle("-fx-text-fill:#64748b; -fx-font-size:10;");

        VBox center = new VBox(0, value, caption);
        center.setAlignment(Pos.CENTER);

        StackPane chart = new StackPane(containers, bulk, minerals, hole, center);
        chart.setMinSize(116, 116);
        chart.setPrefSize(116, 116);
        return chart;
    }

    private Arc donutArc(double radius, double start, double length, String color) {
        Arc arc = new Arc(58, 58, radius, radius, start, length);
        arc.setType(ArcType.OPEN);
        arc.setStyle("-fx-fill:transparent; -fx-stroke:" + color + "; -fx-stroke-width:18;");
        return arc;
    }

    private HBox cargoLine(String labelText, int value, int total, String color) {
        Circle dot = new Circle(4);
        dot.setStyle("-fx-fill:" + color + ";");
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill:#0f172a; -fx-font-size:12;");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        Label amount = new Label(String.format("%,d MT", value));
        amount.setStyle("-fx-text-fill:#0f172a; -fx-font-size:12; -fx-font-weight:bold;");
        double pct = total == 0 ? 0 : (value * 100.0 / total);
        Label percent = new Label(String.format("%.1f%%", pct));
        percent.setStyle("-fx-text-fill:#64748b; -fx-font-size:11;");
        HBox row = new HBox(8, dot, label, space, amount, percent);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox cargoFooter(String value, String label, String color) {
        Label v = new Label(value);
        v.setStyle("-fx-text-fill:" + color + "; -fx-font-size:13; -fx-font-weight:bold;");
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:#64748b; -fx-font-size:10;");
        VBox box = new VBox(3, v, l);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private Pane sparkline() {
        Pane pane = new Pane();
        pane.setMinSize(88, 34);
        pane.setPrefSize(88, 34);
        double[][] points = {{0,24},{10,17},{20,21},{30,8},{40,25},{50,14},{60,17},{70,6},{84,2}};
        for (int i = 1; i < points.length; i++) {
            Line line = new Line(points[i - 1][0], points[i - 1][1], points[i][0], points[i][1]);
            line.setStyle("-fx-stroke:#16a34a; -fx-stroke-width:2;");
            pane.getChildren().add(line);
        }
        return pane;
    }

    private String softBackground(String color) {
        if (BLUE.equals(color) || "#1f74ff".equals(color)) return "#eaf3ff";
        if ("#16a34a".equals(color) || "#35b779".equals(color)) return "#e8f8ef";
        if ("#e43f5a".equals(color)) return "#ffecef";
        if ("#f97316".equals(color)) return "#fff1e7";
        if ("#6d4aff".equals(color)) return "#f0edff";
        return "#f1f5f9";
    }

    private String lastUpdatedShort(String value) {
        if (value == null || value.isBlank()) return "now";
        return "just now";
    }

    private class ButtonRow extends HBox {
        ButtonRow(String title, String subtitle, Runnable action) {
            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:15; -fx-font-weight:bold;");

            Label subtitleLabel = new Label(subtitle);
            subtitleLabel.setStyle("-fx-text-fill:#747884; -fx-font-size:13;");

            VBox copy = new VBox(3, titleLabel, subtitleLabel);
            Region space = new Region();
            HBox.setHgrow(space, Priority.ALWAYS);

            Label arrow = new Label(">");
            arrow.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:18; -fx-font-weight:bold;");

            getChildren().addAll(copy, space, arrow);
            setAlignment(Pos.CENTER_LEFT);
            setPadding(new Insets(10, 12, 10, 12));
            setStyle("-fx-background-color:#f6f9fd; -fx-background-radius:12; -fx-border-color:#e5ebf3; -fx-border-radius:12;");
            setOnMouseClicked(event -> action.run());
            setOnMouseEntered(event -> setStyle("-fx-background-color:#eef5ff; -fx-background-radius:12; -fx-border-color:#cfe0f5; -fx-border-radius:12; -fx-cursor:hand;"));
            setOnMouseExited(event -> setStyle("-fx-background-color:#f6f9fd; -fx-background-radius:12; -fx-border-color:#e5ebf3; -fx-border-radius:12;"));
        }
    }
}
