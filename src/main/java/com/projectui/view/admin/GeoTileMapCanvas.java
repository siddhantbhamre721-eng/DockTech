package com.projectui.view.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Real Geographic OpenStreetMap / CartoDB Tile Map Engine for DockTech Maritime Operations.
 * Renders small map inside Port Network Overview card & opens a large 90% screen overlay modal on click.
 */
public class GeoTileMapCanvas extends StackPane {

    private static final List<GeoTileMapCanvas> instances = new CopyOnWriteArrayList<>();

    public static void refreshAllMapInstances() {
        for (GeoTileMapCanvas canvas : instances) {
            canvas.reloadPorts();
        }
    }

    private double centerLat = 19.5;
    private double centerLng = 78.5;
    private int zoom = 5;

    private double offsetX = 0;
    private double offsetY = 0;

    private double dragStartX = 0;
    private double dragStartY = 0;
    private double initialOffsetX = 0;
    private double initialOffsetY = 0;

    private final double canvasWidth;
    private final double canvasHeight;
    private final boolean isExpandedMode;

    private final Canvas canvas;
    private final GraphicsContext gc;

    private final Map<String, Image> tileCache = new ConcurrentHashMap<>();
    private final List<PortMarker> ports = new ArrayList<>();
    private final List<VesselMarker> vessels = new ArrayList<>();
    private final Map<Long, VesselMarker> vesselMap = new ConcurrentHashMap<>();

    private VesselMarker selectedVessel = null;
    private PortMarker selectedPort = null;

    private String searchQuery = "";
    private boolean showVesselsLayer = true;

    private final VBox infoCard;
    private Label liveBadge;
    private Label timeBadge;
    private Label vesselCountBadge;
    private Label portCountBadge;

    private final VBox cardContainer = new VBox(10);
    private final Pane mapContainer = new Pane();

    public GeoTileMapCanvas(double width, double height, boolean isExpandedMode, Runnable onViewDetails) {
        this.canvasWidth = width;
        this.canvasHeight = height;
        this.isExpandedMode = isExpandedMode;

        instances.add(this);

        setPrefSize(width, height);
        setMinSize(width, height);

        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();

        infoCard = createDarkInfoCard();
        infoCard.setVisible(false);

        cardContainer.setPadding(new Insets(14));
        cardContainer.setSpacing(10);
        cardContainer.setMaxWidth(Double.MAX_VALUE);
        cardContainer.setPrefHeight(height + 60);
        cardContainer.setMaxHeight(height + 60);
        HBox.setHgrow(cardContainer, Priority.ALWAYS);
        cardContainer.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        if (!isExpandedMode) {
            Label title = new Label("Port Network Overview");
            title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16; -fx-font-weight:bold;");
            Label sub = new Label("Real-time maritime traffic & vessel control center");
            sub.setStyle("-fx-text-fill:#475569; -fx-font-size:12;");
            VBox copy = new VBox(2, title, sub);

            Region space = new Region();
            HBox.setHgrow(space, Priority.ALWAYS);

            liveBadge = new Label("● LIVE MARINE DATA");
            liveBadge.setStyle("-fx-text-fill:#10b981; -fx-font-size:11; -fx-font-weight:bold; -fx-background-color:rgba(16,185,129,0.12); -fx-padding:3 8; -fx-background-radius:999;");
            timeBadge = new Label("Last updated: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
            timeBadge.setStyle("-fx-text-fill:#64748b; -fx-font-size:11;");
            VBox statusBox = new VBox(2, liveBadge, timeBadge);
            statusBox.setAlignment(Pos.CENTER_RIGHT);

            Button details = new Button("View Details");
            details.setStyle("-fx-background-color:#ffffff; -fx-text-fill:#0f172a; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:6 12; -fx-cursor:hand;");
            details.setOnAction(e -> {
                if (onViewDetails != null) onViewDetails.run();
            });

            Label expand = new Label("↗");
            expand.setStyle("-fx-text-fill:#0f172a; -fx-font-size:15; -fx-font-weight:bold; -fx-cursor:hand;");
            expand.setOnMouseClicked(e -> openExpandedOverlay());

            HBox top = new HBox(12, copy, space, statusBox, details, expand);
            top.setAlignment(Pos.CENTER_LEFT);

            cardContainer.getChildren().add(top);
        }

        mapContainer.setPrefSize(width, height);
        mapContainer.setMaxWidth(Double.MAX_VALUE);
        mapContainer.setPrefHeight(height);
        mapContainer.setMaxHeight(height);
        HBox.setHgrow(mapContainer, Priority.ALWAYS);
        mapContainer.setStyle("-fx-background-color:#07111a; -fx-background-radius:10; -fx-border-color:#1e293b; -fx-border-radius:10;");

        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        mapContainer.setClip(clip);

        Button zoomInBtn = darkMapBtn("+");
        Button zoomOutBtn = darkMapBtn("-");
        Button resetBtn = darkMapBtn("◎");
        Button fitVesselsBtn = darkMapBtn("🚢");
        Button fitPortsBtn = darkMapBtn("⚓");

        zoomInBtn.setOnAction(e -> zoomIn());
        zoomOutBtn.setOnAction(e -> zoomOut());
        resetBtn.setOnAction(e -> resetView());
        fitVesselsBtn.setOnAction(e -> fitVessels());
        fitPortsBtn.setOnAction(e -> fitPorts());

        VBox controls = new VBox(6, zoomInBtn, zoomOutBtn, resetBtn, fitVesselsBtn, fitPortsBtn);
        controls.setLayoutX(12);
        controls.setLayoutY(16);

        infoCard.setLayoutX(width - 260);
        infoCard.setLayoutY(16);

        mapContainer.getChildren().addAll(canvas, controls, infoCard);

        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseClicked(this::handleMouseClicked);
        canvas.setOnScroll(this::handleScroll);

        cardContainer.getChildren().add(mapContainer);

        getChildren().add(cardContainer);

        widthProperty().addListener((obs, oldV, newV) -> {
            if (!isExpandedMode && newV.doubleValue() > 100) {
                double w = newV.doubleValue() - 28;
                canvas.setWidth(w);
                mapContainer.setPrefWidth(w);
                Rectangle origClip = new Rectangle(w, canvasHeight);
                origClip.setArcWidth(16);
                origClip.setArcHeight(16);
                mapContainer.setClip(origClip);
                infoCard.setLayoutX(w - 260);
                redraw();
            }
        });

        reloadPorts();
        java.util.Collection<com.projectui.service.VesselData> initialVessels = com.projectui.service.VesselManager.getInstance().getAllVessels();
        if (initialVessels != null && !initialVessels.isEmpty()) {
            processVesselUpdate(initialVessels);
        }
        fetchLiveVessels();
    }

    private Button darkMapBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:rgba(15,29,48,0.92); -fx-text-fill:#38bdf8; -fx-font-size:12; -fx-font-weight:bold; -fx-background-radius:6; -fx-border-color:#1e293b; -fx-border-radius:6; -fx-min-width:30; -fx-min-height:30; -fx-padding:0; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.5), 6, 0, 0, 2); -fx-cursor:hand;");
        return b;
    }

    private static Label legendBadge(String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:" + color + "; -fx-font-size:11; -fx-font-weight:bold;");
        return l;
    }

    public void reloadPorts() {
        ports.clear();
        AdminDataStore store = AdminDataStore.getInstance();
        if (store.ports != null) {
            for (AdminDataStore.Port p : store.ports) {
                if (p.approvalStatus != null && !"APPROVED".equalsIgnoreCase(p.approvalStatus)) continue;
                if ("INACTIVE".equalsIgnoreCase(p.status) || "Closed".equalsIgnoreCase(p.status) || "Deactivated".equalsIgnoreCase(p.status)) continue;

                double lat = p.lat;
                double lng = p.lng;
                if (lat == 0.0 && lng == 0.0) {
                    double[] coords = getPortCoordinates(p.location);
                    lat = coords[0];
                    lng = coords[1];
                    p.lat = lat;
                    p.lng = lng;
                }

                String berthsStr = p.occupiedBerths() + "/" + p.berths;
                ports.add(new PortMarker(p.name, p.code, p.location, lat, lng, p.status, p.vessels, berthsStr, p.capacity, p.maintenance, p.portAuthority, p.lastUpdated));
            }
        }
        if (selectedPort != null) {
            boolean stillExists = ports.stream().anyMatch(p -> p.name != null && p.name.equalsIgnoreCase(selectedPort.name));
            if (!stillExists) {
                selectedPort = null;
                infoCard.setVisible(false);
            }
        }
        updateCounts();
        redraw();
    }

    private double[] getPortCoordinates(String location) {
        if (location == null) return new double[]{18.9439, 72.8354};
        String loc = location.toLowerCase();
        if (loc.contains("mumbai")) return new double[]{18.9439, 72.8354};
        if (loc.contains("jnpt") || loc.contains("nhava")) return new double[]{18.9500, 72.9500};
        if (loc.contains("mundra")) return new double[]{22.7400, 69.7000};
        if (loc.contains("kandla") || loc.contains("deendayal")) return new double[]{23.0100, 70.2200};
        if (loc.contains("pipavav")) return new double[]{20.9000, 71.5000};
        if (loc.contains("goa") || loc.contains("mormugao")) return new double[]{15.4200, 73.7800};
        if (loc.contains("mangalore")) return new double[]{12.9200, 74.8000};
        if (loc.contains("kochi") || loc.contains("cochin")) return new double[]{9.9600, 76.2600};
        if (loc.contains("tuticorin") || loc.contains("chidambaranar")) return new double[]{8.7500, 78.1800};
        if (loc.contains("chennai")) return new double[]{13.0827, 80.2707};
        if (loc.contains("ennore") || loc.contains("kamarajar")) return new double[]{13.2600, 80.3300};
        if (loc.contains("krishnapatnam")) return new double[]{14.2500, 80.1300};
        if (loc.contains("visakhapatnam") || loc.contains("vizag")) return new double[]{17.6868, 83.2185};
        if (loc.contains("paradip")) return new double[]{20.2600, 86.6700};
        if (loc.contains("haldia")) return new double[]{22.0300, 88.0600};
        if (loc.contains("kolkata")) return new double[]{22.5726, 88.3639};
        if (loc.contains("hazira")) return new double[]{21.1122, 72.6411};
        return new double[]{18.9439, 72.8354};
    }

    private final com.projectui.service.VesselFinderService vesselFinderService = com.projectui.service.VesselFinderService.getInstance();

    public void fetchLiveVessels() {
        vesselFinderService.start(updatedVessels -> {
            if (vesselFinderService.isRateLimited()) {
                if (liveBadge != null) {
                    liveBadge.setText("● AIS RATE LIMITED");
                    liveBadge.setStyle("-fx-text-fill:#f59e0b; -fx-font-size:11; -fx-font-weight:bold; -fx-background-color:rgba(245,158,11,0.12); -fx-padding:3 8; -fx-background-radius:999;");
                }
            } else if (updatedVessels != null && !updatedVessels.isEmpty()) {
                if (liveBadge != null) {
                    liveBadge.setText("● LIVE MARINE DATA");
                    liveBadge.setStyle("-fx-text-fill:#10b981; -fx-font-size:11; -fx-font-weight:bold; -fx-background-color:rgba(16,185,129,0.12); -fx-padding:3 8; -fx-background-radius:999;");
                }
                if (timeBadge != null) {
                    timeBadge.setText("Last updated: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
                }
                processVesselUpdate(updatedVessels);
            } else {
                if (liveBadge != null) {
                    if (vessels.isEmpty()) {
                        liveBadge.setText("● CONNECTING TO AIS...");
                        liveBadge.setStyle("-fx-text-fill:#38bdf8; -fx-font-size:11; -fx-font-weight:bold; -fx-background-color:rgba(56,189,248,0.12); -fx-padding:3 8; -fx-background-radius:999;");
                    } else {
                        liveBadge.setText("● AIS RATE LIMITED");
                        liveBadge.setStyle("-fx-text-fill:#f59e0b; -fx-font-size:11; -fx-font-weight:bold; -fx-background-color:rgba(245,158,11,0.12); -fx-padding:3 8; -fx-background-radius:999;");
                    }
                }
            }
        });
    }

    public void processVesselUpdate(java.util.Collection<com.projectui.service.VesselData> updatedVessels) {
        if (updatedVessels == null || updatedVessels.isEmpty()) return;

        // 1. Collect all strictly valid water vessels
        List<com.projectui.service.VesselData> validWaterVessels = new ArrayList<>();
        for (com.projectui.service.VesselData v : updatedVessels) {
            if (v == null || !v.isValid()) continue;
            if (v.latitude < -90.0 || v.latitude > 90.0 || v.longitude < -180.0 || v.longitude > 180.0 || (v.latitude == 0.0 && v.longitude == 0.0)) continue;
            if (!com.projectui.service.VesselPositionValidator.isWaterCoordinate(v.latitude, v.longitude)) continue;
            validWaterVessels.add(v);
        }

        if (validWaterVessels.isEmpty()) return;

        // 2. Select a representative, geographically distributed subset of ~45-55 vessels (target ~50)
        List<com.projectui.service.VesselData> mapSubset;
        int targetCount = 50;

        if (validWaterVessels.size() <= targetCount) {
            mapSubset = validWaterVessels;
        } else {
            // Spatial grid bucket distribution across coordinates (5x5 degree grid cells)
            Map<String, List<com.projectui.service.VesselData>> buckets = new LinkedHashMap<>();
            for (com.projectui.service.VesselData v : validWaterVessels) {
                int gridLat = (int) Math.floor(v.latitude / 5.0);
                int gridLng = (int) Math.floor(v.longitude / 5.0);
                String cellKey = gridLat + "_" + gridLng;
                buckets.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(v);
            }

            mapSubset = new ArrayList<>();
            // Round-robin selection from spatial buckets for wide geographic spread across oceans
            boolean addedInRound = true;
            int round = 0;
            while (mapSubset.size() < targetCount && addedInRound) {
                addedInRound = false;
                for (List<com.projectui.service.VesselData> cellList : buckets.values()) {
                    if (round < cellList.size()) {
                        mapSubset.add(cellList.get(round));
                        addedInRound = true;
                        if (mapSubset.size() >= targetCount) break;
                    }
                }
                round++;
            }
        }

        // 3. Update existing markers or create new ones without duplicating
        Set<Long> activeMmsis = new HashSet<>();
        for (com.projectui.service.VesselData v : mapSubset) {
            activeMmsis.add(v.mmsi);
            double incomingLat = v.latitude;
            double incomingLng = v.longitude;

            VesselMarker vm = vesselMap.get(v.mmsi);
            if (vm != null) {
                if (Math.abs(vm.lat - incomingLat) > 0.0001 || Math.abs(vm.lng - incomingLng) > 0.0001) {
                    vm.trail.add(new double[]{vm.lat, vm.lng});
                    if (vm.trail.size() > 12) vm.trail.remove(0);
                }
                vm.lat = incomingLat;
                vm.lng = incomingLng;
                vm.lastValidLat = incomingLat;
                vm.lastValidLng = incomingLng;
                vm.hasLandWarning = false;
                vm.speed = v.speed;
                vm.course = v.course;
                vm.heading = v.heading;
                vm.destination = v.destination;
                vm.navStat = v.navigationStatus;
                vm.lastUpdate = v.timestamp;
            } else {
                vm = new VesselMarker(v.name, v.mmsi, v.imo, v.callSign, v.destination, incomingLat, incomingLng, v.speed, v.course, v.heading, v.shipType, v.navigationStatus, v.timestamp);
                vm.lastValidLat = incomingLat;
                vm.lastValidLng = incomingLng;
                vm.hasLandWarning = false;
                vesselMap.put(v.mmsi, vm);
            }
        }

        // Remove markers not in current active subset
        vesselMap.keySet().removeIf(mmsi -> !activeMmsis.contains(mmsi));
        vessels.clear();
        vessels.addAll(vesselMap.values());

        updateCounts();
        redraw();
    }

    private void updateCounts() {
        if (vesselCountBadge != null) vesselCountBadge.setText("Vessels: " + vessels.size());
        if (portCountBadge != null) portCountBadge.setText("Ports: " + ports.size());
    }

    private void zoomIn() {
        if (zoom < 11) {
            zoom++;
            redraw();
        }
    }

    private void zoomOut() {
        if (zoom > 2) {
            zoom--;
            redraw();
        }
    }

    private void fitVessels() {
        if (vessels.isEmpty()) return;
        double minLat = 90, maxLat = -90, minLng = 180, maxLng = -180;
        for (VesselMarker v : vessels) {
            if (v.lat < minLat) minLat = v.lat;
            if (v.lat > maxLat) maxLat = v.lat;
            if (v.lng < minLng) minLng = v.lng;
            if (v.lng > maxLng) maxLng = v.lng;
        }
        centerLat = (minLat + maxLat) / 2.0;
        centerLng = (minLng + maxLng) / 2.0;
        zoom = 3;
        offsetX = 0;
        offsetY = 0;
        redraw();
    }

    private void resetView() {
        zoom = 5;
        centerLat = 19.5;
        centerLng = 78.5;
        offsetX = 0;
        offsetY = 0;
        redraw();
    }

    private void toggleVesselsLayer() {
        showVesselsLayer = !showVesselsLayer;
        redraw();
    }

    private void fitPorts() {
        if (ports.isEmpty()) return;
        double minLat = 90, maxLat = -90, minLng = 180, maxLng = -180;
        for (PortMarker p : ports) {
            if (p.lat < minLat) minLat = p.lat;
            if (p.lat > maxLat) maxLat = p.lat;
            if (p.lng < minLng) minLng = p.lng;
            if (p.lng > maxLng) maxLng = p.lng;
        }
        centerLat = (minLat + maxLat) / 2.0;
        centerLng = (minLng + maxLng) / 2.0;
        zoom = 5;
        offsetX = 0;
        offsetY = 0;
        redraw();
    }

    private void handleMousePressed(MouseEvent event) {
        dragStartX = event.getX();
        dragStartY = event.getY();
        initialOffsetX = offsetX;
        initialOffsetY = offsetY;
    }

    private void handleMouseDragged(MouseEvent event) {
        offsetX = initialOffsetX + (event.getX() - dragStartX);
        offsetY = initialOffsetY + (event.getY() - dragStartY);
        redraw();
    }

    private void handleScroll(ScrollEvent event) {
        if (event.getDeltaY() > 0) {
            zoomIn();
        } else if (event.getDeltaY() < 0) {
            zoomOut();
        }
    }

    private void handleMouseClicked(MouseEvent event) {
        if (Math.abs(event.getX() - dragStartX) < 5 && Math.abs(event.getY() - dragStartY) < 5) {
            double clickX = event.getX();
            double clickY = event.getY();

            VesselMarker hitVessel = null;
            for (VesselMarker v : vessels) {
                if (isFilteredOut(v.name, String.valueOf(v.mmsi))) continue;
                double[] p = latLngToPixel(v.lat, v.lng);
                double dx = clickX - p[0];
                double dy = clickY - p[1];
                if (dx * dx + dy * dy <= 169) {
                    hitVessel = v;
                    break;
                }
            }

            if (hitVessel != null) {
                selectedVessel = hitVessel;
                selectedPort = null;
                centerLat = hitVessel.lat;
                centerLng = hitVessel.lng;
                offsetX = 0;
                offsetY = 0;
                updateInfoCardForVessel(hitVessel);
                infoCard.setVisible(true);
                redraw();
                return;
            }

            PortMarker hitPort = null;
            for (PortMarker port : ports) {
                if (isFilteredOut(port.name, port.location)) continue;
                double[] p = latLngToPixel(port.lat, port.lng);
                double dx = clickX - p[0];
                double dy = clickY - p[1];
                if (dx * dx + dy * dy <= 256) {
                    hitPort = port;
                    break;
                }
            }

            if (hitPort != null) {
                selectedPort = hitPort;
                selectedVessel = null;
                centerLat = hitPort.lat;
                centerLng = hitPort.lng;
                offsetX = 0;
                offsetY = 0;
                updateInfoCardForPort(hitPort);
                infoCard.setVisible(true);
                redraw();
                return;
            }

            selectedVessel = null;
            selectedPort = null;
            infoCard.setVisible(false);
            redraw();

            if (!isExpandedMode && activeExpandedPopup == null) {
                openExpandedOverlay();
            }
        }
    }

    private boolean isFilteredOut(String name, String extra) {
        if (searchQuery == null || searchQuery.isBlank()) return false;
        String q = searchQuery.toLowerCase().trim();
        boolean m1 = name != null && name.toLowerCase().contains(q);
        boolean m2 = extra != null && extra.toLowerCase().contains(q);
        return !(m1 || m2);
    }

    private double[] latLngToPixel(double lat, double lng) {
        int n = 1 << zoom;
        double totalPx = n * 256.0;

        double centerPxX = (centerLng + 180.0) / 360.0 * totalPx;
        double centerRad = Math.toRadians(centerLat);
        double centerPxY = (1.0 - Math.log(Math.tan(centerRad) + 1.0 / Math.cos(centerRad)) / Math.PI) / 2.0 * totalPx;

        double targetPxX = (lng + 180.0) / 360.0 * totalPx;
        double targetRad = Math.toRadians(lat);
        double targetPxY = (1.0 - Math.log(Math.tan(targetRad) + 1.0 / Math.cos(targetRad)) / Math.PI) / 2.0 * totalPx;

        double screenX = canvas.getWidth() / 2.0 + (targetPxX - centerPxX) + offsetX;
        double screenY = canvas.getHeight() / 2.0 + (targetPxY - centerPxY) + offsetY;

        return new double[]{screenX, screenY};
    }

    public void redraw() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.setFill(Color.web("#07111a"));
        gc.fillRect(0, 0, w, h);

        int n = 1 << zoom;
        double totalPx = n * 256.0;

        double centerPxX = (centerLng + 180.0) / 360.0 * totalPx;
        double centerRad = Math.toRadians(centerLat);
        double centerPxY = (1.0 - Math.log(Math.tan(centerRad) + 1.0 / Math.cos(centerRad)) / Math.PI) / 2.0 * totalPx;

        int minTx = (int) Math.floor((centerPxX - w / 2.0 - offsetX) / 256.0);
        int maxTx = (int) Math.floor((centerPxX + w / 2.0 - offsetX) / 256.0);
        int minTy = (int) Math.floor((centerPxY - h / 2.0 - offsetY) / 256.0);
        int maxTy = (int) Math.floor((centerPxY + h / 2.0 - offsetY) / 256.0);

        for (int tx = minTx; tx <= maxTx; tx++) {
            for (int ty = minTy; ty <= maxTy; ty++) {
                if (tx >= 0 && tx < n && ty >= 0 && ty < n) {
                    double tileScreenX = w / 2.0 + (tx * 256.0 - centerPxX) + offsetX;
                    double tileScreenY = h / 2.0 + (ty * 256.0 - centerPxY) + offsetY;

                    String tileUrl = String.format("https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/%d/%d/%d", zoom, ty, tx);
                    Image tileImg = tileCache.get(tileUrl);
                    if (tileImg == null) {
                        tileImg = new Image(tileUrl, true);
                        tileCache.put(tileUrl, tileImg);
                        tileImg.progressProperty().addListener((obs, oldV, newV) -> {
                            if (newV.doubleValue() >= 1.0) {
                                javafx.application.Platform.runLater(this::redraw);
                            }
                        });
                    }

                    if (tileImg.getProgress() >= 1.0 && !tileImg.isError()) {
                        gc.drawImage(tileImg, tileScreenX, tileScreenY, 256, 256);
                    } else {
                        gc.setFill(Color.web("#aadaff"));
                        gc.fillRect(tileScreenX, tileScreenY, 256, 256);
                        gc.setStroke(Color.web("rgba(2,132,199,0.15)"));
                        gc.strokeRect(tileScreenX, tileScreenY, 256, 256);
                    }
                }
            }
        }

        double[] arabianPos = latLngToPixel(15.0, 68.0);
        gc.setFill(Color.web("rgba(15,23,42,0.75)"));
        gc.setFont(javafx.scene.text.Font.font("system-ui", javafx.scene.text.FontWeight.BOLD, 12));
        gc.fillText("ARABIAN SEA", arabianPos[0], arabianPos[1]);

        double[] bayPos = latLngToPixel(15.0, 88.0);
        gc.fillText("BAY OF BENGAL", bayPos[0], bayPos[1]);

        if (selectedVessel != null) {
            double[] vp = latLngToPixel(selectedVessel.lat, selectedVessel.lng);
            gc.setStroke(Color.web("#06b6d4"));
            gc.setLineWidth(2.5);
            gc.setLineDashes(6, 4);

            double rad = Math.toRadians(selectedVessel.course - 180);
            double tailX = vp[0] + Math.sin(rad) * 75;
            double tailY = vp[1] - Math.cos(rad) * 75;

            gc.strokeLine(vp[0], vp[1], tailX, tailY);
            gc.setLineDashes(null);
        }

        // Database Ports
        for (PortMarker p : ports) {
            if (isFilteredOut(p.name, p.location)) continue;

            double[] pos = latLngToPixel(p.lat, p.lng);
            boolean isSel = (selectedPort == p);

            Color pColor = getStatusColor(p.status);
            gc.setFill(pColor);
            gc.fillOval(pos[0] - 7, pos[1] - 7, 14, 14);

            gc.setStroke(isSel ? Color.web("#38bdf8") : Color.WHITE);
            gc.setLineWidth(isSel ? 2.5 : 1.5);
            gc.strokeOval(pos[0] - 7, pos[1] - 7, 14, 14);

            gc.setFill(Color.web("rgba(15,23,42,0.92)"));
            gc.fillRoundRect(pos[0] - 38, pos[1] - 25, 76, 16, 8, 8);
            gc.setStroke(pColor);
            gc.setLineWidth(1.0);
            gc.strokeRoundRect(pos[0] - 38, pos[1] - 25, 76, 16, 8, 8);

            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("system-ui", javafx.scene.text.FontWeight.BOLD, 9));
            gc.fillText("⚓ " + p.shortName(), pos[0] - 31, pos[1] - 14);
        }

        // Live AIS Vessels
        if (!showVesselsLayer) return;

        for (VesselMarker v : vessels) {
            drawSingleVessel(v);
        }
    }

    private void drawSingleVessel(VesselMarker v) {
        if (v == null || isFilteredOut(v.name, String.valueOf(v.mmsi))) return;
        if (v.lat < -90.0 || v.lat > 90.0 || v.lng < -180.0 || v.lng > 180.0 || (v.lat == 0.0 && v.lng == 0.0)) return;

        double[] pos = latLngToPixel(v.lat, v.lng);
        boolean isSel = (selectedVessel == v);

        Color vColor = v.speed > 10.0 ? Color.web("#06b6d4") : (v.speed > 0.5 ? Color.web("#10b981") : Color.web("#f59e0b"));

        gc.save();
        gc.translate(pos[0], pos[1]);
        gc.rotate(v.course);

        gc.setFill(vColor);
        gc.fillPolygon(new double[]{0, 6, -6}, new double[]{-9, 7, 7}, 3);

        gc.setStroke(isSel ? Color.web("#38bdf8") : Color.web("rgba(255,255,255,0.8)"));
        gc.setLineWidth(isSel ? 2.2 : 1.0);
        gc.strokePolygon(new double[]{0, 6, -6}, new double[]{-9, 7, 7}, 3);

        gc.restore();
    }

    @SuppressWarnings("unused")
    private static class ClusterNode {
        public double lat;
        public double lng;
        public int count = 0;
        public VesselMarker singleVessel;
    }

    private List<ClusterNode> clusterVessels(List<VesselMarker> vesselList, double radiusPx) {
        List<ClusterNode> result = new ArrayList<>();
        for (VesselMarker v : vesselList) {
            double[] p = latLngToPixel(v.lat, v.lng);
            boolean merged = false;
            for (ClusterNode c : result) {
                double[] cp = latLngToPixel(c.lat, c.lng);
                double dx = p[0] - cp[0];
                double dy = p[1] - cp[1];
                if (dx * dx + dy * dy <= radiusPx * radiusPx) {
                    c.count++;
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                ClusterNode cn = new ClusterNode();
                cn.lat = v.lat;
                cn.lng = v.lng;
                cn.count = 1;
                cn.singleVessel = v;
                result.add(cn);
            }
        }
        return result;
    }

    private Color getStatusColor(String status) {
        if (status == null) return Color.web("#10b981");
        String s = status.toLowerCase();
        if (s.contains("busy") || s.contains("congestion") || s.contains("high")) return Color.web("#ef4444");
        if (s.contains("maintenance")) return Color.web("#0284c7");
        if (s.contains("inactive")) return Color.web("#64748b");
        return Color.web("#10b981");
    }

    private VBox createDarkInfoCard() {
        VBox card = new VBox(6);
        card.setPrefWidth(250);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color:rgba(15,23,42,0.96); -fx-background-radius:10; -fx-border-color:#0284c7; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.6), 12, 0, 0, 4);");
        return card;
    }

    private void updateInfoCardForVessel(VesselMarker v) {
        infoCard.getChildren().clear();

        Label headerLbl = new Label("VESSEL DETAILS");
        headerLbl.setStyle("-fx-text-fill:#38bdf8; -fx-font-size:11; -fx-font-weight:bold;");

        Button centerBtn = new Button("◎ Center");
        centerBtn.setStyle("-fx-background-color:#0f2744; -fx-text-fill:#38bdf8; -fx-border-color:#1e3a5f; -fx-border-radius:4; -fx-background-radius:4; -fx-font-size:10; -fx-font-weight:bold; -fx-padding:2 6; -fx-cursor:hand;");
        centerBtn.setOnAction(e -> {
            centerLat = v.lat;
            centerLng = v.lng;
            zoom = 8;
            offsetX = 0;
            offsetY = 0;
            redraw();
        });

        Label closeBtn = new Label("✕");
        closeBtn.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12; -fx-font-weight:bold; -fx-cursor:hand;");
        closeBtn.setOnMouseClicked(e -> {
            infoCard.setVisible(false);
            selectedVessel = null;
            redraw();
        });

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox topRow = new HBox(8, headerLbl, sp, centerBtn, closeBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(v.name);
        title.setStyle("-fx-text-fill:#f8fafc; -fx-font-size:14; -fx-font-weight:bold;");

        Label shipTypeBanner = new Label("🚢 " + v.type);
        shipTypeBanner.setStyle("-fx-text-fill:#38bdf8; -fx-font-size:11; -fx-font-weight:bold; -fx-background-color:linear-gradient(to right, #0f2744, #16365c); -fx-padding:4 8; -fx-background-radius:6;");

        VBox box = new VBox(4, topRow, title, shipTypeBanner);

        if (v.hasLandWarning || isCoordinateOnInlandLandmass(v.lat, v.lng)) {
            Label posWarn = new Label("⚠️ Position warning — AIS coordinate appears to be on land.");
            posWarn.setStyle("-fx-text-fill:#f59e0b; -fx-font-size:10; -fx-font-weight:bold; -fx-background-color:rgba(245,158,11,0.12); -fx-padding:4 6; -fx-background-radius:4; -fx-wrap-text:true;");
            box.getChildren().add(posWarn);
        }

        box.setStyle("-fx-border-color:rgba(255,255,255,0.08); -fx-border-width:0 0 1 0; -fx-padding:0 0 6 0;");

        Label sectionPos = new Label("LIVE POSITION & AIS");
        sectionPos.setStyle("-fx-text-fill:#38bdf8; -fx-font-size:10; -fx-font-weight:bold; -fx-padding:4 0 0 0;");

        Label sectionVoy = new Label("VOYAGE INFO");
        sectionVoy.setStyle("-fx-text-fill:#38bdf8; -fx-font-size:10; -fx-font-weight:bold; -fx-padding:4 0 0 0;");

        Label sectionSpec = new Label("VESSEL PARTICULARS");
        sectionSpec.setStyle("-fx-text-fill:#38bdf8; -fx-font-size:10; -fx-font-weight:bold; -fx-padding:4 0 0 0;");

        infoCard.getChildren().addAll(
                box,
                sectionPos,
                infoRow("Status", v.navStat),
                infoRow("Speed", String.format("%.1f kn", v.speed)),
                infoRow("Course / Heading", String.format("%.1f° / %.1f°", v.course, v.heading)),
                infoRow("Position", String.format("%.4f° N, %.4f° E", v.lat, v.lng)),
                infoRow("Destination", v.destination),
                infoRow("● LIVE AIS", v.lastUpdate),
                sectionVoy,
                infoRow("Last Port", "Port of Mumbai"),
                infoRow("Last Country", "India 🇮🇳"),
                sectionSpec,
                infoRow("IMO Number", v.imo),
                infoRow("MMSI", String.valueOf(v.mmsi)),
                infoRow("Call Sign", v.callSign),
                infoRow("Flag", "India 🇮🇳"),
                infoRow("Gross Tonnage", "42,500 GT")
        );

        // Port Assignment & Proximity Metadata
        String assignedPortName = null;
        if (v.destination != null && !v.destination.isBlank() && !"En Route".equalsIgnoreCase(v.destination)) {
            assignedPortName = v.destination;
        }

        try {
            List<com.projectui.model.Ship> ships = new com.projectui.dao.ShipDAO().getShips();
            if (ships != null) {
                for (com.projectui.model.Ship s : ships) {
                    if (s != null) {
                        boolean matchName = s.getShipName() != null && !s.getShipName().isBlank() && v.name.toLowerCase().contains(s.getShipName().toLowerCase());
                        boolean matchImo = s.getImoNumber() != null && !s.getImoNumber().isBlank() && v.imo.equalsIgnoreCase(s.getImoNumber());
                        if (matchName || matchImo) {
                            if (s.getCompany() != null && !s.getCompany().isBlank()) {
                                assignedPortName = s.getCompany();
                            }
                            String assign = s.getBerth() != null && !s.getBerth().isBlank() ? ("Berth " + s.getBerth()) : "Registered Fleet Vessel";
                            infoCard.getChildren().add(infoRow("Fleet Assignment", assign));
                            break;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        // Calculate nearest port for relationship metadata
        PortMarker nearbyPort = null;
        double minDistance = Double.MAX_VALUE;
        for (PortMarker p : ports) {
            double dLat = p.lat - v.lat;
            double dLng = p.lng - v.lng;
            double dist = Math.sqrt(dLat * dLat + dLng * dLng);
            if (dist < minDistance) {
                minDistance = dist;
                nearbyPort = p;
            }
        }

        if (assignedPortName != null && !assignedPortName.isBlank()) {
            infoCard.getChildren().add(infoRow("Assigned Port", assignedPortName));
        }

        if (nearbyPort != null && minDistance <= 3.5) {
            infoCard.getChildren().add(infoRow("Nearby Port", nearbyPort.name));
        }
    }

    private void updateInfoCardForPort(PortMarker p) {
        infoCard.getChildren().clear();

        Label headerLbl = new Label("APPROVED PORT DETAILS");
        headerLbl.setStyle("-fx-text-fill:#38bdf8; -fx-font-size:11; -fx-font-weight:bold;");

        Button centerBtn = new Button("◎ Center");
        centerBtn.setStyle("-fx-background-color:#0f2744; -fx-text-fill:#38bdf8; -fx-border-color:#1e3a5f; -fx-border-radius:4; -fx-background-radius:4; -fx-font-size:10; -fx-font-weight:bold; -fx-padding:2 6; -fx-cursor:hand;");
        centerBtn.setOnAction(e -> {
            centerLat = p.lat;
            centerLng = p.lng;
            zoom = 8;
            offsetX = 0;
            offsetY = 0;
            redraw();
        });

        Label closeBtn = new Label("✕");
        closeBtn.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12; -fx-font-weight:bold; -fx-cursor:hand;");
        closeBtn.setOnMouseClicked(e -> {
            infoCard.setVisible(false);
            selectedPort = null;
            redraw();
        });

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox topRow = new HBox(8, headerLbl, sp, centerBtn, closeBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(p.name);
        title.setStyle("-fx-text-fill:#f8fafc; -fx-font-size:14; -fx-font-weight:bold;");
        Label sub = new Label(p.location + " • Status: " + p.status);
        sub.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:11;");

        VBox box = new VBox(2, topRow, title, sub);
        box.setStyle("-fx-border-color:rgba(255,255,255,0.08); -fx-border-width:0 0 1 0; -fx-padding:0 0 6 0;");

        infoCard.getChildren().addAll(
                box,
                infoRow("Port Name", p.name),
                infoRow("Port Code", p.code),
                infoRow("Location", p.location),
                infoRow("Status", p.status),
                infoRow("Port Authority", p.portAuthority),
                infoRow("Capacity", p.capacity),
                infoRow("Vessels Docked", p.vessels + " vessels"),
                infoRow("Berths Occupied", p.berths),
                infoRow("Maintenance", p.maintenance),
                infoRow("Last Updated", p.lastUpdated)
        );
    }

    private boolean isCoordinateOnInlandLandmass(double lat, double lng) {
        return !com.projectui.service.VesselPositionValidator.isWaterCoordinate(lat, lng);
    }

    private HBox infoRow(String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:11;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label val = new Label(value == null ? "N/A" : value);
        val.setStyle("-fx-text-fill:#f8fafc; -fx-font-size:11; -fx-font-weight:bold;");

        HBox row = new HBox(8, lbl, sp, val);
        row.setStyle("-fx-border-color:rgba(255,255,255,0.04); -fx-border-width:0 0 1 0; -fx-padding:2 0;");
        return row;
    }

    private void showPortNetworkDetailsModal() {
        Pane rootPane = null;
        Scene scene = getScene();
        if (scene != null && scene.getRoot() instanceof Pane) {
            rootPane = (Pane) scene.getRoot();
        } else {
            for (Window w : Window.getWindows()) {
                if (w.isShowing() && w.getScene() != null && w.getScene().getRoot() instanceof Pane) {
                    rootPane = (Pane) w.getScene().getRoot();
                    break;
                }
            }
        }

        if (rootPane == null) return;

        double modalW = Math.min(640, rootPane.getWidth() * 0.85);
        double modalH = Math.min(480, rootPane.getHeight() * 0.80);

        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color:rgba(7,17,26,0.75);");
        backdrop.setPrefSize(rootPane.getWidth(), rootPane.getHeight());

        VBox modalCard = new VBox(14);
        modalCard.setMaxSize(modalW, modalH);
        modalCard.setPrefSize(modalW, modalH);
        modalCard.setPadding(new Insets(20));
        modalCard.setStyle("-fx-background-color:#0f1d30; -fx-background-radius:12; -fx-border-color:#1e3a5f; -fx-border-radius:12; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.6), 20, 0, 0, 6);");

        Label title = new Label("PORT NETWORK & TELEMETRY DETAILS");
        title.setStyle("-fx-text-fill:#f8fafc; -fx-font-size:15; -fx-font-weight:bold;");

        Button closeBtn = new Button("✕ Close");
        closeBtn.setStyle("-fx-background-color:#1e293b; -fx-text-fill:#f8fafc; -fx-border-color:#334155; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:6 12; -fx-cursor:hand;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox header = new HBox(12, title, space, closeBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(8);
        content.setPadding(new Insets(10, 0, 0, 0));

        AdminDataStore store = AdminDataStore.getInstance();
        int totalPorts = store.ports != null ? store.ports.size() : 0;
        int activePorts = store.activePorts();
        int activeVessels = vessels.size();

        content.getChildren().addAll(
            detailRow("Total Registered Ports", totalPorts + " ports across Indian coastline"),
            detailRow("Operational Status", activePorts + " Active, " + (totalPorts - activePorts) + " Under Maintenance / Congested"),
            detailRow("Live Vessels Tracked", activeVessels + " maritime vessels receiving telemetry"),
            detailRow("AIS Stream Status", liveBadge != null ? liveBadge.getText() : "● LIVE MARINE DATA"),
            detailRow("Coverage Region", "Indian Ocean (0.0° N - 28.0° N, 55.0° E - 98.0° E)"),
            detailRow("Update Frequency", "Live Telemetry (120-second AIS poll interval)"),
            detailRow("Basemap Provider", "CartoDB Voyager / OpenStreetMap Tile Engine")
        );

        modalCard.getChildren().addAll(header, content);
        backdrop.getChildren().add(modalCard);

        final Pane finalRoot = rootPane;
        Runnable closeModal = () -> finalRoot.getChildren().remove(backdrop);

        closeBtn.setOnAction(e -> closeModal.run());
        backdrop.setOnMouseClicked(e -> {
            if (e.getTarget() == backdrop) closeModal.run();
        });
        backdrop.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) closeModal.run();
        });

        finalRoot.getChildren().add(backdrop);
        backdrop.requestFocus();
    }

    private HBox detailRow(String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12; -fx-pref-width:180;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill:#f8fafc; -fx-font-size:12; -fx-font-weight:bold; -fx-wrap-text:true;");
        HBox row = new HBox(12, lbl, val);
        row.setStyle("-fx-border-color:rgba(255,255,255,0.06); -fx-border-width:0 0 1 0; -fx-padding:6 0;");
        return row;
    }

    private Popup activeExpandedPopup = null;

    // ================= EXPANDABLE MAP WINDOW OVERLAY =================

    public void openExpandedOverlay() {
        if (activeExpandedPopup != null) return;

        Window window = null;
        if (getScene() != null && getScene().getWindow() != null) {
            window = getScene().getWindow();
        } else {
            for (Window w : Window.getWindows()) {
                if (w.isShowing()) {
                    window = w;
                    break;
                }
            }
        }
        if (window == null) return;

        double winW = window.getWidth();
        double winH = window.getHeight();

        double originalW = canvas.getWidth() > 100 ? canvas.getWidth() : canvasWidth;

        double targetW = Math.max(850, winW * 0.88);
        double targetH = Math.max(520, winH * 0.84);

        StackPane overlayBackdrop = new StackPane();
        overlayBackdrop.setStyle("-fx-background-color:rgba(7,17,26,0.82);");
        overlayBackdrop.setPrefSize(winW, winH);
        overlayBackdrop.setMinSize(winW, winH);
        overlayBackdrop.setMaxSize(winW, winH);
        overlayBackdrop.setAlignment(Pos.CENTER);

        VBox expandedModalCard = new VBox(12);
        expandedModalCard.setMaxSize(targetW, targetH);
        expandedModalCard.setPrefSize(targetW, targetH);
        expandedModalCard.setPadding(new Insets(16));
        expandedModalCard.setStyle("-fx-background-color:#07111a; -fx-background-radius:14; -fx-border-color:#1e293b; -fx-border-radius:14; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.88), 30, 0, 0, 10);");
        StackPane.setAlignment(expandedModalCard, Pos.CENTER);

        Label modalTitle = new Label("PORT NETWORK OVERVIEW — LIVE MARITIME MAP");
        modalTitle.setStyle("-fx-text-fill:#f8fafc; -fx-font-size:15; -fx-font-weight:bold;");

        Label liveStatus = new Label("● LIVE MARINE DATA");
        liveStatus.setStyle("-fx-text-fill:#10b981; -fx-font-size:11; -fx-font-weight:bold; -fx-background-color:rgba(16,185,129,0.12); -fx-padding:4 10; -fx-background-radius:999;");

        Button minimizeBtn = new Button("🗗 Minimize");
        minimizeBtn.setStyle("-fx-background-color:#0f2744; -fx-text-fill:#38bdf8; -fx-border-color:#1e3a5f; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:6 14; -fx-cursor:hand;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox modalHeader = new HBox(12, modalTitle, liveStatus, space, minimizeBtn);
        modalHeader.setAlignment(Pos.CENTER_LEFT);

        double expandedCanvasW = targetW - 32;
        double expandedCanvasH = targetH - 68;

        cardContainer.getChildren().remove(mapContainer);

        canvas.setWidth(expandedCanvasW);
        canvas.setHeight(expandedCanvasH);
        mapContainer.setPrefSize(expandedCanvasW, expandedCanvasH);
        mapContainer.setMinSize(expandedCanvasW, expandedCanvasH);
        mapContainer.setMaxSize(expandedCanvasW, expandedCanvasH);

        Rectangle expClip = new Rectangle(expandedCanvasW, expandedCanvasH);
        expClip.setArcWidth(16);
        expClip.setArcHeight(16);
        mapContainer.setClip(expClip);
        infoCard.setLayoutX(expandedCanvasW - 260);

        expandedModalCard.getChildren().addAll(modalHeader, mapContainer);
        overlayBackdrop.getChildren().add(expandedModalCard);

        Popup popup = new Popup();
        popup.setAutoHide(false);
        popup.getContent().add(overlayBackdrop);

        Runnable minimizeAction = () -> {
            popup.hide();
            activeExpandedPopup = null;

            expandedModalCard.getChildren().remove(mapContainer);

            double restoredW = originalW > 100 ? originalW : canvasWidth;

            canvas.setWidth(restoredW);
            canvas.setHeight(canvasHeight);

            mapContainer.setPrefSize(restoredW, canvasHeight);
            mapContainer.setMinSize(restoredW, canvasHeight);
            mapContainer.setMaxWidth(Double.MAX_VALUE);
            mapContainer.setMaxHeight(canvasHeight);

            Rectangle origClip = new Rectangle(restoredW, canvasHeight);
            origClip.setArcWidth(16);
            origClip.setArcHeight(16);
            mapContainer.setClip(origClip);
            infoCard.setLayoutX(restoredW - 260);
            infoCard.setLayoutY(16);

            if (!cardContainer.getChildren().contains(mapContainer)) {
                cardContainer.getChildren().add(mapContainer);
            }

            cardContainer.requestLayout();
            mapContainer.requestLayout();

            javafx.application.Platform.runLater(() -> {
                redraw();
                requestLayout();
            });
        };

        minimizeBtn.setOnAction(e -> minimizeAction.run());
        overlayBackdrop.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) minimizeAction.run();
        });

        activeExpandedPopup = popup;
        popup.show(window, window.getX(), window.getY());

        redraw();
    }

    public static class PortMarker {
        public String name;
        public String code;
        public String location;
        public double lat;
        public double lng;
        public String status;
        public int vessels;
        public String berths;
        public String capacity;
        public String maintenance;
        public String portAuthority;
        public String lastUpdated;

        public PortMarker(String name, String code, String location, double lat, double lng, String status, int vessels, String berths, String capacity, String maintenance, String portAuthority, String lastUpdated) {
            this.name = name;
            this.code = code != null && !code.isBlank() ? code : "IN-PORT-01";
            this.location = location;
            this.lat = lat;
            this.lng = lng;
            this.status = status;
            this.vessels = vessels;
            this.berths = berths;
            this.capacity = capacity;
            this.maintenance = maintenance;
            this.portAuthority = portAuthority != null && !portAuthority.isBlank() ? portAuthority : "Port Authority (" + name + ")";
            this.lastUpdated = lastUpdated != null ? lastUpdated : "Just now";
        }

        public String shortName() {
            if (name == null) return "Port";
            if (name.contains("Mumbai")) return "Mumbai";
            if (name.contains("Chennai")) return "Chennai";
            if (name.contains("Visakhapatnam") || name.contains("Vizag")) return "Vizag";
            if (name.contains("Kolkata")) return "Kolkata";
            if (name.contains("Hazira")) return "Hazira";
            return name.length() > 10 ? name.substring(0, 10) : name;
        }
    }

    public static class VesselMarker {
        public String name;
        public long mmsi;
        public String imo;
        public String callSign;
        public String destination;
        public double lat;
        public double lng;
        public double lastValidLat;
        public double lastValidLng;
        public boolean hasLandWarning = false;
        public double speed;
        public double course;
        public double heading;
        public String type;
        public String navStat;
        public String lastUpdate;
        public final List<double[]> trail = new ArrayList<>();

        public VesselMarker(String name, long mmsi, String imo, String callSign, String destination, double lat, double lng, double speed, double course, double heading, String type, String navStat, String lastUpdate) {
            this.name = name;
            this.mmsi = mmsi;
            this.imo = imo;
            this.callSign = callSign;
            this.destination = destination;
            this.lat = lat;
            this.lng = lng;
            this.lastValidLat = lat;
            this.lastValidLng = lng;
            this.speed = speed;
            this.course = course;
            this.heading = heading;
            this.type = type;
            this.navStat = navStat;
            this.lastUpdate = lastUpdate;
        }
    }
}
