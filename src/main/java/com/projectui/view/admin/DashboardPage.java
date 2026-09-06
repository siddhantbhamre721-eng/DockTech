package com.projectui.view.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
import javafx.scene.shape.Polyline;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.Node;
import javafx.scene.control.Separator;

@SuppressWarnings("unused")
public class DashboardPage extends AdminTheme {

    private final AdminDataStore store = AdminDataStore.getInstance();
    private Runnable addPortAction = () -> {};
    private Runnable addUserAction = () -> {};
    private Runnable alertsAction = () -> {};
    private Runnable reportAction = () -> {};

    private static final List<DashboardPage> instances = new java.util.concurrent.CopyOnWriteArrayList<>();

    public static void refreshAllDashboards() {
        for (DashboardPage page : instances) {
            page.refreshData();
        }
    }

    private VBox pageContainer;
    private GridPane kpiGrid;
    private GridPane mainGrid;
    private GridPane bottomGrid;

        private final com.projectui.service.VesselManager vesselManager =
            com.projectui.service.VesselManager.getInstance();

    public Parent getRoot() {
        return getRoot(() -> {}, () -> {}, () -> {}, () -> {});
    }

    public Parent getRoot(Runnable addPortAction, Runnable addUserAction, Runnable alertsAction, Runnable reportAction) {
        if (!instances.contains(this)) instances.add(this);
        this.addPortAction = addPortAction;
        this.addUserAction = addUserAction;
        this.alertsAction = alertsAction;
        this.reportAction = reportAction;
        pageContainer = pageContent();
        pageContainer.setSpacing(18);
        pageContainer.widthProperty().addListener((obs, oldWidth, newWidth) -> layoutResponsive(newWidth.doubleValue()));
        buildContent();
        // First sync port_requests, then sync registered_ports from Firestore, then refresh dashboard stats.
        new com.projectui.dao.PortRequestDao().syncWithDatabase().whenComplete((list, err) -> {
            store.syncPortsFromFirestore(() -> {
                javafx.application.Platform.runLater(() -> refreshData());
            });
        });
        ScrollPane scroller = new ScrollPane(pageContainer);
        scroller.setFitToWidth(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroller.setPannable(true);
        scroller.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-padding:0;");
        return scroller;
    }

    public void refreshData() {
        if (pageContainer != null) {
            pageContainer.getChildren().clear();
            buildContent();
        }
    }

    private void buildContent() {
        int pendingInspections = store.pendingInspectionsCount();
        List<Node> kpiCards = List.of(
            kpiCard("⚓", "Total Ports", String.valueOf(store.totalPorts()), "All Registered", "#1d4ed8", "#eaf3ff"),
            kpiCard("⬟", "Active Ports", String.valueOf(store.activePorts()), "Operational", "#0f9f6e", "#e7f8ef"),
            kpiCard("▴", "Active Vessels", String.valueOf(vesselManager.getActiveVesselCount()), "Currently in Indian Ocean", "#6d4aff", "#f0edff"),
            kpiCard("!", "Pending Inspections", String.valueOf(pendingInspections), "Require Attention", "#e43f5a", "#ffecef")
        );

        kpiGrid = responsiveGrid();
        addResponsiveChildren(kpiGrid, kpiCards);

        List<Node> mainCards = new ArrayList<>();
        Region ops = portLocationMapCard();
        Region st = statusCard();
        ops.setMinWidth(0);
        ops.setMinHeight(420);
        st.setMinWidth(0);
        st.setMaxHeight(330);
        ops.setMaxWidth(Double.MAX_VALUE);
        ops.setMaxHeight(Double.MAX_VALUE);
        st.setMaxWidth(Double.MAX_VALUE);
        st.setMaxHeight(Double.MAX_VALUE);
        mainCards.add(ops);
        mainCards.add(st);
        mainGrid = responsiveGrid();
        addResponsiveChildren(mainGrid, mainCards);

        Region alerts = alertsCard();
        Region recent = recentActivityCard();
        Region opsVessel = liveVesselMovementAndOperationsCard();
        alerts.setMaxWidth(Double.MAX_VALUE);
        recent.setMaxWidth(Double.MAX_VALUE);
        opsVessel.setMaxWidth(Double.MAX_VALUE);
        alerts.setMinWidth(0);
        recent.setMinWidth(0);
        opsVessel.setMinWidth(0);
        bottomGrid = responsiveGrid();
        addResponsiveChildren(bottomGrid, List.of(alerts, recent, opsVessel));

        pageContainer.getChildren().addAll(
            kpiGrid,
            mainGrid,
            bottomGrid
        );
        layoutResponsive(pageContainer.getWidth());
    }

    private GridPane responsiveGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);
        grid.setMaxWidth(Double.MAX_VALUE);
        return grid;
    }

    private void addResponsiveChildren(GridPane grid, List<Node> children) {
        for (Node child : children) {
            if (child instanceof Region region) {
                region.setMinWidth(0);
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMaxHeight(Double.MAX_VALUE);
            }
            GridPane.setHgrow(child, Priority.ALWAYS);
            GridPane.setVgrow(child, Priority.ALWAYS);
            GridPane.setFillWidth(child, true);
            GridPane.setFillHeight(child, true);
        }
        grid.getProperties().put("responsiveChildren", children);
    }

    @SuppressWarnings("unchecked")
    private void layoutResponsive(double width) {
        if (kpiGrid == null || mainGrid == null || bottomGrid == null) return;

        boolean compact = width > 0 && width < 700;
        boolean medium = width >= 700 && width < 1100;
        layoutGrid(kpiGrid, (List<Node>) kpiGrid.getProperties().get("responsiveChildren"), compact ? 1 : (medium ? 2 : 4), 100.0);
        layoutGrid(mainGrid, (List<Node>) mainGrid.getProperties().get("responsiveChildren"), compact ? 1 : 2, compact ? 100.0 : 72.0);
        layoutGrid(bottomGrid, (List<Node>) bottomGrid.getProperties().get("responsiveChildren"), compact ? 1 : (medium ? 2 : 3), 100.0);
    }

    private void layoutGrid(GridPane grid, List<Node> children, int columns, double firstColumnPercent) {
        if (children == null || columns < 1) return;
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        for (int column = 0; column < columns; column++) {
            javafx.scene.layout.ColumnConstraints constraints = new javafx.scene.layout.ColumnConstraints();
                double percent = columns == 2 && firstColumnPercent < 100.0
                    ? (column == 0 ? firstColumnPercent : 100.0 - firstColumnPercent)
                    : 100.0 / columns;
                constraints.setPercentWidth(percent);
            constraints.setMinWidth(0);
            constraints.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(constraints);
        }
        for (int index = 0; index < children.size(); index++) {
            Node child = children.get(index);
            int column = index % columns;
            int row = index / columns;
            grid.add(child, column, row);
        }
    }

    private Region portLocationMapCard() {
        GeoTileMapCanvas map = new GeoTileMapCanvas(800, 360, false, addPortAction);
        map.setMinWidth(0);
        map.setMaxWidth(Double.MAX_VALUE);
        return map;
    }

    private static String escapeJs(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

    private double[] realPortGeoCoordinates(String location) {
        if (location == null) return new double[]{18.9439, 72.8354};
        String loc = location.toLowerCase();
        if (loc.contains("mumbai") || loc.contains("nhava") || loc.contains("jnpt")) return new double[]{18.9439, 72.8354};
        if (loc.contains("chennai") || loc.contains("enore")) return new double[]{13.0827, 80.2707};
        if (loc.contains("visakhapatnam") || loc.contains("vizag") || loc.contains("andhra")) return new double[]{17.6868, 83.2185};
        if (loc.contains("kolkata") || loc.contains("haldia") || loc.contains("bengal")) return new double[]{22.5726, 88.3639};
        if (loc.contains("hazira") || loc.contains("gujarat") || loc.contains("surat") || loc.contains("kandla")) return new double[]{21.1122, 72.6411};
        return new double[]{18.9439, 72.8354};
    }

    private Line darkRouteLine(double startX, double startY, double endX, double endY) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStyle("-fx-stroke:#0ea5e9; -fx-stroke-width:1.2; -fx-stroke-dash-array:6 4; -fx-opacity:0.45;");
        return line;
    }

    private Button darkMapButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:#0f1d30; -fx-text-fill:#38bdf8; -fx-font-size:13; -fx-font-weight:bold; -fx-background-radius:6; -fx-border-color:#1e293b; -fx-border-radius:6; -fx-min-width:30; -fx-min-height:30; -fx-padding:0; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2); -fx-cursor:hand;");
        return button;
    }

    private VBox darkMapInfoPanel(AdminDataStore.Port port) {
        VBox panel = new VBox(7);
        panel.setPrefWidth(370);
        panel.setPadding(new Insets(12));
        panel.setStyle("-fx-background-color:rgba(15,23,42,0.96); -fx-background-radius:12; -fx-border-color:#0284c7; -fx-border-radius:12; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.5), 14, 0, 0, 4);");
        updateMapInfo(panel, port);
        return panel;
    }

    public static class VesselCluster {
        public double x;
        public double y;
        public final List<com.projectui.service.DigitrafficService.VesselData> vessels = new ArrayList<>();

        public VesselCluster(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private List<VesselCluster> clusterVessels(List<com.projectui.service.DigitrafficService.VesselData> vessels, double clusterDistance) {
        List<VesselCluster> clusters = new ArrayList<>();
        for (com.projectui.service.DigitrafficService.VesselData v : vessels) {
            double[] coords = vesselMapCoordinates(v.latitude, v.longitude, v.mmsi);
            double vx = coords[0];
            double vy = coords[1];

            boolean added = false;
            for (VesselCluster c : clusters) {
                double dist = Math.hypot(c.x - vx, c.y - vy);
                if (dist <= clusterDistance) {
                    c.vessels.add(v);
                    added = true;
                    break;
                }
            }

            if (!added) {
                VesselCluster newCluster = new VesselCluster(vx, vy);
                newCluster.vessels.add(v);
                clusters.add(newCluster);
            }
        }
        return clusters;
    }

    private Node renderClusterOrVessel(VesselCluster cluster, VBox info, Pane mapPane) {
        if (cluster.vessels.size() == 1) {
            com.projectui.service.DigitrafficService.VesselData v = cluster.vessels.get(0);
            return vesselSingleMarker(v, info, mapPane, cluster.x, cluster.y);
        } else {
            return vesselClusterMarker(cluster, info);
        }
    }

    private Node vesselSingleMarker(com.projectui.service.DigitrafficService.VesselData vessel, VBox info, Pane mapPane, double vx, double vy) {
        StackPane iconPane = new StackPane();
        iconPane.setPrefSize(24, 24);
        iconPane.setLayoutX(vx - 12);
        iconPane.setLayoutY(vy - 12);
        iconPane.setStyle("-fx-cursor:hand;");

        String vColor = vessel.speedKnots > 10.0 ? "#06b6d4" : (vessel.speedKnots > 0.5 ? "#10b981" : "#f59e0b");

        Circle bg = new Circle(10);
        bg.setStyle("-fx-fill:" + vColor + "; -fx-stroke:#ffffff; -fx-stroke-width:1.5; -fx-effect:dropshadow(gaussian, " + vColor + ", 8, 0, 0, 2);");

        Label arrow = new Label("▲");
        arrow.setStyle("-fx-text-fill:white; -fx-font-size:9; -fx-font-weight:bold;");
        arrow.setRotate(vessel.courseDegrees);

        iconPane.getChildren().addAll(bg, arrow);

        iconPane.setOnMouseClicked(event -> {
            // Draw subtle vessel track trail
            Polyline trail = new Polyline(
                vx - 32 * Math.sin(Math.toRadians(vessel.courseDegrees)), vy + 32 * Math.cos(Math.toRadians(vessel.courseDegrees)),
                vx - 16 * Math.sin(Math.toRadians(vessel.courseDegrees)), vy + 16 * Math.cos(Math.toRadians(vessel.courseDegrees)),
                vx, vy
            );
            trail.setStyle("-fx-stroke:#06b6d4; -fx-stroke-width:2; -fx-stroke-dash-array:4 4; -fx-opacity:0.75;");
            mapPane.getChildren().removeIf(n -> "vesselTrail".equals(n.getUserData()));
            trail.setUserData("vesselTrail");
            mapPane.getChildren().add(1, trail);

            updateVesselMapInfo(info, vessel);
            positionMapInfo(info, vx, vy);
            info.setVisible(true);
        });

        iconPane.setOnMouseEntered(event -> {
            iconPane.setScaleX(1.35);
            iconPane.setScaleY(1.35);
        });

        iconPane.setOnMouseExited(event -> {
            iconPane.setScaleX(1.0);
            iconPane.setScaleY(1.0);
        });

        return iconPane;
    }

    private Node vesselClusterMarker(VesselCluster cluster, VBox info) {
        StackPane clusterPane = new StackPane();
        clusterPane.setPrefSize(26, 26);
        clusterPane.setLayoutX(cluster.x - 13);
        clusterPane.setLayoutY(cluster.y - 13);
        clusterPane.setStyle("-fx-cursor:hand;");

        Circle bg = new Circle(13);
        bg.setStyle("-fx-fill:#0284c7; -fx-stroke:white; -fx-stroke-width:2; -fx-effect:dropshadow(gaussian, rgba(2,132,199,0.7), 10, 0, 0, 3);");

        Label badge = new Label(String.valueOf(cluster.vessels.size()));
        badge.setStyle("-fx-text-fill:white; -fx-font-size:11; -fx-font-weight:bold;");

        clusterPane.getChildren().addAll(bg, badge);

        clusterPane.setOnMouseClicked(event -> {
            updateClusterMapInfo(info, cluster);
            positionMapInfo(info, cluster.x, cluster.y);
            info.setVisible(true);
        });

        clusterPane.setOnMouseEntered(event -> {
            clusterPane.setScaleX(1.25);
            clusterPane.setScaleY(1.25);
        });

        clusterPane.setOnMouseExited(event -> {
            clusterPane.setScaleX(1.0);
            clusterPane.setScaleY(1.0);
        });

        return clusterPane;
    }

    private void updateClusterMapInfo(VBox panel, VesselCluster cluster) {
        panel.getChildren().clear();
        Label title = new Label("⚓ Maritime Zone Cluster (" + cluster.vessels.size() + " Ships)");
        title.setStyle("-fx-text-fill:#38bdf8; -fx-font-size:13; -fx-font-weight:bold;");

        Label subtitle = new Label("Live AIS vessels operating in this sea sector");
        subtitle.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:11;");

        panel.getChildren().addAll(title, subtitle, new Separator());

        int limit = Math.min(4, cluster.vessels.size());
        for (int i = 0; i < limit; i++) {
            com.projectui.service.DigitrafficService.VesselData v = cluster.vessels.get(i);
            Label vRow = new Label("• " + v.name + " (" + v.shipType + ") → " + v.destination);
            vRow.setStyle("-fx-text-fill:#f8fafc; -fx-font-size:11; -fx-font-weight:bold;");
            panel.getChildren().add(vRow);
        }
        if (cluster.vessels.size() > limit) {
            Label extra = new Label("+ " + (cluster.vessels.size() - limit) + " additional vessels in sector");
            extra.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:10; -fx-italic:true;");
            panel.getChildren().add(extra);
        }
    }

    private void updateVesselMapInfo(VBox panel, com.projectui.service.DigitrafficService.VesselData vessel) {
        panel.getChildren().clear();
        if (vessel == null) {
            Label empty = new Label("No vessel data available");
            empty.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12; -fx-font-weight:bold;");
            panel.getChildren().add(empty);
            return;
        }

        Label name = new Label("🚢 " + vessel.name);
        name.setStyle("-fx-text-fill:#38bdf8; -fx-font-size:14; -fx-font-weight:bold;");
        Label typeLabel = new Label(vessel.shipType + " • " + vessel.navStat);
        typeLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:11;");
        Label statusLabel = new Label("🟢 LIVE AIS TRANSMISSION");
        statusLabel.setStyle("-fx-text-fill:#10b981; -fx-font-size:10; -fx-font-weight:bold;");

        panel.getChildren().addAll(
                name,
                typeLabel,
                statusLabel,
                new Separator(),
                mapDarkInfoRow("MMSI", String.valueOf(vessel.mmsi)),
                mapDarkInfoRow("IMO Number", vessel.imo),
                mapDarkInfoRow("Call Sign", vessel.callSign),
                mapDarkInfoRow("Destination", vessel.destination),
                mapDarkInfoRow("Speed / SOG", String.format("%.1f kn", vessel.speedKnots)),
                mapDarkInfoRow("Course / COG", String.format("%.0f°", vessel.courseDegrees)),
                mapDarkInfoRow("Position", String.format("%.4f° N, %.4f° E", vessel.latitude, vessel.longitude)),
                mapDarkInfoRow("Last Updated", vessel.lastUpdate)
        );
    }

    private HBox mapDarkInfoRow(String labelText, String value) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:11; -fx-font-weight:bold;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill:#f8fafc; -fx-font-size:11; -fx-font-weight:bold;");
        valueLabel.setWrapText(true);
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox row = new HBox(8, label, space, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private double[] vesselMapCoordinates(double lat, double lng, long mmsi) {
        if (lat >= 5.0 && lat <= 27.0 && lng >= 65.0 && lng <= 95.0) {
            double x = 40.0 + ((lng - 66.0) / 26.0) * 620.0;
            double y = 295.0 - ((lat - 6.0) / 20.0) * 250.0;
            return new double[]{Math.max(20, Math.min(640, x)), Math.max(20, Math.min(280, y))};
        } else {
            double x = 70.0 + (Math.abs(mmsi * 37) % 560);
            double y = 50.0 + (Math.abs(mmsi * 17) % 220);
            return new double[]{x, y};
        }
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
            if (port.approvalStatus != null && !"APPROVED".equalsIgnoreCase(port.approvalStatus)) continue;
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
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setStyle(cardStyle());
        HBox top = cardHeader("Operational Alerts", "View All Alerts  →", alertsAction);
        card.getChildren().add(top);

        int alertCount = 0;
        for (AdminDataStore.Port p : store.ports) {
            if ("APPROVED".equalsIgnoreCase(p.approvalStatus)) {
                if ("Busy".equalsIgnoreCase(p.status)) {
                    card.getChildren().add(alertLine("!", p.name, "High congestion watch active", "Live", "#e43f5a"));
                    alertCount++;
                } else if ("Maintenance".equalsIgnoreCase(p.status)) {
                    card.getChildren().add(alertLine("⌁", p.name, p.maintenance != null && !p.maintenance.isBlank() ? p.maintenance : "Maintenance in progress", "Live", BLUE));
                    alertCount++;
                }
            }
        }

        int pendingInspections = store.pendingInspectionsCount();
        if (pendingInspections > 0) {
            card.getChildren().add(alertLine("▣", pendingInspections + " Inspections", "Pending admin verification & compliance check", "Action Required", "#f97316"));
            alertCount++;
        }

        if (store.pendingPortRequestsCount() > 0) {
            card.getChildren().add(alertLine("⚓", store.pendingPortRequestsCount() + " New Port Requests", "Pending Admin approval", "Action Required", "#6d4aff"));
            alertCount++;
        }

        if (alertCount == 0) {
            Label noAlerts = new Label("✓ All ports operational. No active alerts.");
            noAlerts.setStyle("-fx-text-fill:#16a34a; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:10;");
            card.getChildren().add(noAlerts);
        }

        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private VBox recentActivityCard() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setStyle(cardStyle());
        HBox top = cardHeader("Recent Activity", "View All Activity  →", addUserAction);
        card.getChildren().add(top);

        int actCount = 0;
        for (AdminDataStore.Port p : store.ports) {
            if ("APPROVED".equalsIgnoreCase(p.approvalStatus) && actCount < 4) {
                card.getChildren().add(activityLine("⚓", p.name, "Operational port status verified", p.lastUpdated != null ? p.lastUpdated : "Live", BLUE));
                actCount++;
            }
        }

        for (AdminDataStore.UserAccount u : store.users) {
            if (actCount < 4) {
                card.getChildren().add(activityLine("👤", u.name, "User account (" + u.role + ") active", u.lastLogin != null ? u.lastLogin : "Today", "#16a34a"));
                actCount++;
            }
        }

        if (actCount == 0) {
            Label noAct = new Label("No recent system activity.");
            noAct.setStyle("-fx-text-fill:#64748b; -fx-font-size:12; -fx-padding:10;");
            card.getChildren().add(noAct);
        }

        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private VBox liveVesselMovementAndOperationsCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(14));
        card.setStyle(cardStyle());

        HBox header = cardHeader("Live Vessel & Port Ops", "Reports Center  →", reportAction);

        // Vessel movement statistics from VesselManager
        java.util.Collection<com.projectui.service.VesselData> vessels = vesselManager.getAllVessels();
        int inPort = 0, arriving = 0, departing = 0, inTransit = 0;
        for (com.projectui.service.VesselData v : vessels) {
            if (v == null || !v.isValid()) continue;
            String vesselStatus = com.projectui.service.VesselManager.categorizeStatus(v);
            if ("Currently In Port".equals(vesselStatus)) {
                inPort++;
            } else if ("Anchored".equals(vesselStatus)) {
                inPort++;
            } else if ("Arriving".equals(vesselStatus)) {
                arriving++;
            } else if ("Departing".equals(vesselStatus)) {
                departing++;
            } else {
                inTransit++;
            }
        }

        // Port operations statistics from AdminDataStore
        int opPorts = store.activePorts();
        int maintPorts = store.maintenancePorts();
        int busyPorts = (int) store.ports.stream().filter(p -> "Busy".equalsIgnoreCase(p.status) && "APPROVED".equalsIgnoreCase(p.approvalStatus)).count();
        int inactivePorts = (int) store.ports.stream().filter(p -> "Inactive".equalsIgnoreCase(p.status)).count();

        VBox vSec = new VBox(6);
        Label vHeader = new Label("LIVE VESSEL MOVEMENT");
        vHeader.setStyle("-fx-text-fill:#64748b; -fx-font-size:11; -fx-font-weight:bold;");

        HBox vRow = new HBox(8,
                statusSummary("In Port", inPort, "#16a34a"),
                statusSummary("Arriving", arriving, "#0ea5e9"),
                statusSummary("Departing", departing, "#6366f1"),
                statusSummary("In Transit", inTransit, "#0284c7")
        );
        vRow.setPadding(new Insets(8));
        vRow.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:6; -fx-background-radius:6;");
        vRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        vSec.getChildren().addAll(vHeader, vRow);

        VBox pSec = new VBox(6);
        Label pHeader = new Label("PORT OPERATIONS");
        pHeader.setStyle("-fx-text-fill:#64748b; -fx-font-size:11; -fx-font-weight:bold;");

        HBox pRow = new HBox(8,
                statusSummary("Operational", opPorts, "#16a34a"),
                statusSummary("Maintenance", maintPorts, BLUE),
                statusSummary("High Congestion", busyPorts, "#ef4444"),
                statusSummary("Inactive", inactivePorts, "#94a3b8")
        );
        pRow.setPadding(new Insets(8));
        pRow.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:6; -fx-background-radius:6;");
        pRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        pSec.getChildren().addAll(pHeader, pRow);

        card.getChildren().addAll(header, vSec, pSec);
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
