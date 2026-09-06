package com.projectui.view.exporter;

import com.projectui.dao.CargoDao;
import com.projectui.dao.ContainerDao;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TrackShipment {

    private Scene scene;

    // Design Tokens
    private static final String BG_COLOR = "#F4F8FC";
    private static final String CARD_BG = "#FFFFFF";
    private static final String TEXT_PRIMARY = "#0F172A";
    private static final String TEXT_MUTED = "#64748B";
    private static final String PRIMARY_BLUE = "#1769FF";
    private static final String BORDER_COLOR = "#E2E8F0";

    public Scene getTrackShipmentScene() {
        return getTrackShipmentScene(null);
    }

    public Scene getTrackShipmentScene(Runnable callbackAction) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");
        root.setTop(createHeader("Track Shipment", "DockTech / Exporter / Track Shipment"));
        root.setLeft(createSidebar());
        root.setCenter(getScrollableContent());

        scene = new Scene(root);
        com.projectui.view.login.LoginPage.applyInvisibleScrollbar(scene);
        return scene;
    }

    private final CargoDao cargoDao = new CargoDao();
    private final ContainerDao containerDao = new ContainerDao();
    private boolean listenersRegistered;

    public ScrollPane getScrollableContent() {
        // =========================================================
        // MAIN WORKSPACE
        // =========================================================
        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 32, 36, 32));
        content.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // Header Banner
        VBox headerBanner = new VBox(6);
        Text title = new Text("Track Shipment");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text subtitle = new Text("Real-time vessel tracking, route progress, and shipment milestones.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-fill: " + TEXT_MUTED + ";");
        headerBanner.getChildren().addAll(title, subtitle);

        List<Map<String, Object>> allCargos = cargoDao.getShipmentsForExporter(com.projectui.view.login.LoginPage.loggedInUser);
        if (allCargos == null || allCargos.isEmpty()) {
            allCargos = cargoDao.getAllCargos();
        }

        if (allCargos == null || allCargos.isEmpty()) {
            // Empty State
            VBox emptyCard = createCard();
            emptyCard.setAlignment(Pos.CENTER);
            emptyCard.setPadding(new Insets(48));

            Label emptyIcon = new Label("📦");
            emptyIcon.setStyle("-fx-font-size: 48px;");

            Text emptyTitle = new Text("No Active Shipments Available");
            emptyTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

            Text emptyDesc = new Text(
                    "You have not registered any cargo or booked a container yet. Once cargo is registered and booked, real-time live telemetry and milestones will appear here.");
            emptyDesc.setStyle("-fx-font-size: 14px; -fx-fill: " + TEXT_MUTED + "; -fx-text-alignment: center;");
            emptyDesc.setWrappingWidth(500);

            Button registerNavBtn = new Button("Register Cargo Now →");
            registerNavBtn.setStyle("-fx-background-color: " + PRIMARY_BLUE
                    + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 24; -fx-background-radius: 8px; -fx-cursor: hand;");
            registerNavBtn.setOnAction(e -> {
                if (ExporterDashboard.getInstance() != null) {
                    ExporterDashboard.getInstance().switchPage("Register Cargo");
                }
            });

            emptyCard.getChildren().addAll(emptyIcon, emptyTitle, emptyDesc, registerNavBtn);
            content.getChildren().addAll(headerBanner, emptyCard);

            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(false);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + BG_COLOR
                    + "; -fx-border-color: transparent;");
            return scrollPane;
        }

        // =========================================================
        // 1. TOP SHIPMENT SEARCH CARD
        // =========================================================
        VBox searchCard = createCard();
        searchCard.setPadding(new Insets(20, 24, 20, 24));

        Text searchTitle = new Text("📥  Live Shipment Tracker");
        searchTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        HBox searchBar = new HBox(12);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> shipmentSelector = new ComboBox<>();
        for (Map<String, Object> c : allCargos) {
            String cId = c.get("cargoId") != null ? c.get("cargoId").toString() : "CGO";
            String cName = c.get("cargoName") != null ? c.get("cargoName").toString() : "";
            shipmentSelector.getItems().add(cId + " — " + cName);
        }
        String defaultSel = !shipmentSelector.getItems().isEmpty() ? shipmentSelector.getItems().get(0) : null;
        for (String item : shipmentSelector.getItems()) {
            if (item.toUpperCase().startsWith("CGO-1027")) {
                defaultSel = item;
                break;
            }
        }
        if (defaultSel != null) {
            shipmentSelector.setValue(defaultSel);
        }
        shipmentSelector.setPrefHeight(44);
        shipmentSelector.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(shipmentSelector, Priority.ALWAYS);
        shipmentSelector.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-border-color: #CBD5E1;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 4px 12px;" +
                        "-fx-font-size: 14px;");

        searchBar.getChildren().addAll(new Label("Select Active Shipment:"), shipmentSelector);
        searchCard.getChildren().addAll(searchTitle, searchBar);

        // =========================================================
        // 2. MAIN TRACKING DETAILS & TIMELINE (2 COLUMNS)
        // =========================================================
        HBox workspace = new HBox(24);

        // LEFT COLUMN: SUMMARY & LIVE MAP STATUS
        VBox leftCol = new VBox(22);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        // A. Shipment Information Grid Card
        VBox infoCard = createCard();
        Text infoTitle = new Text("Shipment Overview");
        infoTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(16);
        infoGrid.setVgap(16);

        infoCard.getChildren().addAll(infoTitle, infoGrid);

        // B. Shipment Progress Timeline Card
        VBox timelineCard = createCard();
        Text timelineTitle = new Text("Shipment Progress Timeline");
        timelineTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text timelineSub = new Text("Milestones and clearance stages.");
        timelineSub.setStyle("-fx-font-size: 13px; -fx-fill: " + TEXT_MUTED + ";");

        VBox stagesList = new VBox(14);
        timelineCard.getChildren().addAll(timelineTitle, timelineSub, stagesList);

        leftCol.getChildren().addAll(infoCard, timelineCard);

        // RIGHT COLUMN: ROUTE VISUALIZATION & LIVE STATUS (40% Width)
        VBox rightCol = new VBox(22);
        rightCol.setPrefWidth(380);
        rightCol.setMinWidth(340);

        // A. Vessel Route Card
        VBox routeCard = createCard();
        Text routeTitle = new Text("🚢  Live Vessel Route");
        routeTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        VBox routeDiagram = new VBox(16);
        routeDiagram.setPadding(new Insets(20));
        routeDiagram.setStyle(
                "-fx-background-color: #F0F7FF; -fx-background-radius: 12px; -fx-border-color: #BAE6FD; -fx-border-radius: 12px;");

        routeCard.getChildren().addAll(routeTitle, routeDiagram);

        // B. Status Alerts Card
        VBox alertCard = createCard();
        Text alertTitle = new Text("Shipment Status Alerts");
        alertTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        VBox alerts = new VBox(10);
        alertCard.getChildren().addAll(alertTitle, alerts);

        rightCol.getChildren().addAll(routeCard, alertCard);

        workspace.getChildren().addAll(leftCol, rightCol);

        // Dynamic update function when shipment changes
        Runnable updateTrackingData = () -> {
            String sel = shipmentSelector.getValue();
            String cId = "";
            if (sel != null) {
                cId = sel.split(" — ")[0].trim();
            }
            Map<String, Object> c = (!cId.isBlank()) ? cargoDao.getCargo(cId) : null;
            String cName = c != null && c.get("cargoName") != null ? c.get("cargoName").toString() : "N/A";
            String cStatus = c != null && c.get("status") != null ? c.get("status").toString() : "Registered";
            String originPort = c != null && c.get("originPort") != null ? c.get("originPort").toString()
                    : "Origin Port";
            String destPort = c != null && c.get("destinationPort") != null ? c.get("destinationPort").toString()
                    : "Destination Port";

            Map<String, Object> booking = (!cId.isBlank()) ? containerDao.getBookingByCargoId(cId) : null;
            String bId = booking != null && booking.get("bookingId") != null ? booking.get("bookingId").toString()
                    : "Pending Allocation";
            String bType = booking != null && booking.get("containerType") != null
                    ? booking.get("containerType").toString()
                    : "Pending Selection";

            if (bId.equalsIgnoreCase("Pending Allocation")) {
                String altBId = getString(c, "bookingId", "containerId", "containerNo");
                if (!altBId.isBlank()) bId = altBId;
            }
            if (bType.equalsIgnoreCase("Pending Selection")) {
                String altBType = getString(c, "containerType", "type");
                if (!altBType.isBlank()) bType = altBType;
            }

            boolean isDelivered = containsStatus(cStatus, "deliver")
                    || containsStatus(getStatus(c, "deliveryStatus", "arrivalStatus", "status"), "deliver")
                    || (c != null && c.get("deliveredAt") != null && !c.get("deliveredAt").toString().isBlank());

            if (isDelivered) {
                cStatus = "Delivered";
                if (c != null && (!"Delivered".equalsIgnoreCase(getString(c, "status")) || !"Delivered".equalsIgnoreCase(getString(c, "deliveryStatus")))) {
                    c.put("status", "Delivered");
                    c.put("deliveryStatus", "Delivered");
                    cargoDao.updateCargoStatus(cId, "Delivered");
                }
            }

            infoGrid.getChildren().clear();
            infoGrid.add(createTile("SHIPMENT ID",
                    !cId.isBlank() ? ("SHP-" + Math.abs(cId.hashCode() % 8999 + 1000)) : "N/A"), 0, 0);
            infoGrid.add(createTile("CARGO", cName), 1, 0);
            infoGrid.add(createTile("CONTAINER ID", bId), 0, 1);
            infoGrid.add(createTile("CONTAINER TYPE", bType), 1, 1);
            infoGrid.add(createTile("ORIGIN PORT", originPort), 0, 2);
            infoGrid.add(createTile("DESTINATION", destPort), 1, 2);

            timelineSub.setText(
                    "Milestones and clearance stages for " + (!cId.isBlank() ? cId : "Shipment") + " (" + cName + ")");

            boolean isArrived = isDelivered
                    || containsStatus(getStatus(c, "status", "deliveryStatus", "arrivalStatus"), "arrived", "delivered", "at destination")
                    || vesselHasArrived(c, destPort);

            boolean isDeparted = isArrived
                    || containsStatus(getStatus(c, "status", "loadingStatus", "vesselStatus", "shipStatus"), "transit", "underway", "departed", "departing", "arrived", "delivered")
                    || vesselHasDeparted(c);

            boolean isPaid = isDeparted
                    || (c != null && com.projectui.dao.PaymentDao.isPaidCargo(c))
                    || containsStatus(getStatus(c, "paymentStatus", "payment"), "paid", "success", "settled")
                    || (c != null && c.get("paidAt") != null && !c.get("paidAt").toString().isBlank())
                    || (c != null && c.get("razorpayPaymentId") != null && !c.get("razorpayPaymentId").toString().isBlank());

            boolean isUploaded = isPaid
                    || hasRequiredDocuments(c)
                    || containsStatus(getStatus(c, "customsStatus", "customsClearanceStatus", "docStatus", "verificationStatus"), "cleared", "verified", "passed", "approved")
                    || (c != null && c.get("clearedAt") != null && !c.get("clearedAt").toString().isBlank());

            boolean isBooked = isUploaded
                    || (booking != null && hasValue(booking, "bookingId") && !containsStatus(booking, "pending", "rejected", "cancelled"))
                    || (!bId.isBlank() && !bId.equalsIgnoreCase("Pending Allocation"))
                    || containsStatus(getStatus(c, "loadingStatus", "status"), "allocated", "loading", "loaded", "departed", "transit", "arrived", "delivered");

            String deliveredAt = getString(c, "deliveredAt", "confirmedAt");
            String deliveredBy = getString(c, "deliveredBy", "confirmedBy", "importerName", "importer");
            String deliverySubtitle;
            if (isDelivered) {
                if (!deliveredBy.isBlank() && !deliveredAt.isBlank()) {
                    deliverySubtitle = "Delivery Confirmed by " + deliveredBy + " • " + deliveredAt;
                } else if (!deliveredBy.isBlank()) {
                    deliverySubtitle = "Delivery Confirmed by " + deliveredBy;
                } else if (!deliveredAt.isBlank()) {
                    deliverySubtitle = "Delivery Confirmed • " + deliveredAt;
                } else {
                    deliverySubtitle = "Delivered / Delivery Confirmed";
                }
            } else {
                deliverySubtitle = isArrived ? "Awaiting Importer Confirmation" : "Pending Delivery Confirmation";
            }

            String transitSubtitle;
            if (isDeparted) {
                transitSubtitle = (isArrived || isDelivered)
                        ? "Vessel Departed from " + originPort + " (Voyage Completed)"
                        : "Vessel Departed from " + originPort + " — In Transit";
            } else {
                transitSubtitle = "Awaiting Vessel Departure";
            }

            String arrivalSubtitle;
            if (isArrived || isDelivered) {
                arrivalSubtitle = "Vessel Arrived at " + destPort;
            } else {
                arrivalSubtitle = "Destination Schedule — Pending Arrival";
            }

            stagesList.getChildren().clear();
            stagesList.getChildren().addAll(
                    createTimelineStage("Cargo Registered", originPort, true, true),
                    createTimelineStage("Container Allocated (" + bId + ")",
                            isBooked ? "Container Booking Completed" : "Awaiting Container Booking", isBooked, isBooked),
                    createTimelineStage("Customs Documentation",
                            isUploaded ? "Required Documents Uploaded & Verified" : "Pending Document Upload", isUploaded, isUploaded),
                    createTimelineStage("Payment Settled",
                            isPaid ? "Payment Successful" : "Pending Payment", isPaid, isPaid),
                    createTimelineStage("In Transit to Destination",
                            transitSubtitle, isDeparted, isDeparted),
                    createTimelineStage("Arriving at " + destPort,
                            arrivalSubtitle, isArrived, isArrived),
                    createTimelineStage("Delivered / Delivery Confirmed",
                            deliverySubtitle, isDelivered, isDelivered));

            routeDiagram.getChildren().clear();
            HBox originNode = createRouteNode(originPort, "Origin Port", "#16A34A");
            Region vLine1 = createVerticalLine(isDeparted);
            HBox currentLocationNode = createRouteNode(
                    isDelivered ? "Delivered at " + destPort : (isDeparted ? "In Transit (" + originPort + " → " + destPort + ")" : originPort + " Terminal"),
                    isDelivered ? "Consignment Delivered • Confirmed" : (isDeparted ? "Vessel Departed • In Transit" : "Terminal Staging"),
                    isDelivered ? "#16A34A" : PRIMARY_BLUE);
            Region vLine2 = createVerticalLine(isArrived || isDelivered);
            HBox destNode = createRouteNode(destPort,
                    isDelivered ? "Delivered / Delivery Confirmed" : (isArrived ? "Vessel Arrived" : "Destination Port"),
                    (isArrived || isDelivered) ? "#16A34A" : "#64748B");

            routeDiagram.getChildren().addAll(originNode, vLine1, currentLocationNode, vLine2, destNode);

            alerts.getChildren().clear();
            if (isDelivered) {
                alerts.getChildren().add(createStatusAlert(
                        "✓  Shipment Delivered — Confirmed by Importer" + (!deliveredBy.isBlank() ? (" (" + deliveredBy + ")") : ""),
                        "#DCFCE7", "#15803D", "#86EFAC"));
                alerts.getChildren().add(createStatusAlert(
                        "✓  Delivery Confirmed — All milestones completed and verified",
                        "#DBEAFE", "#1D4ED8", "#93C5FD"));
            } else {
                if (isArrived) {
                    alerts.getChildren().add(createStatusAlert(
                            "✓  Vessel Arrived at " + destPort + " — Awaiting Importer Delivery Confirmation",
                            "#DCFCE7", "#15803D", "#86EFAC"));
                } else if (isDeparted) {
                    alerts.getChildren().add(createStatusAlert(
                            "✓  Vessel Departed from " + originPort + " — In Transit",
                            "#DBEAFE", "#1D4ED8", "#93C5FD"));
                }
                if (isPaid) {
                    alerts.getChildren().add(createStatusAlert(
                            "✓  Payment Verified — Ready for international customs clearance",
                            "#DCFCE7", "#15803D", "#86EFAC"));
                }
                if (isBooked) {
                    alerts.getChildren().add(createStatusAlert(
                            "✓  Container Allocated — " + bId + " ready for loading",
                            "#DBEAFE", "#1D4ED8", "#93C5FD"));
                } else {
                    alerts.getChildren().add(createStatusAlert(
                            "⚠️  Action Required — Please book a container to proceed",
                            "#FFF7ED", "#C2410C", "#FDBA74"));
                }
            }
        };

        Runnable syncSelectorAndData = () -> {
            List<Map<String, Object>> latestCargos = cargoDao.getShipmentsForExporter(com.projectui.view.login.LoginPage.loggedInUser);
            if (latestCargos == null || latestCargos.isEmpty()) {
                latestCargos = cargoDao.getAllCargos();
            }
            if (latestCargos != null && !latestCargos.isEmpty()) {
                String currentSel = shipmentSelector.getValue();
                String currentCId = (currentSel != null) ? currentSel.split(" — ")[0].trim() : "";
                List<String> newItems = new java.util.ArrayList<>();
                String reselect = null;
                for (Map<String, Object> itemCargo : latestCargos) {
                    String id = itemCargo.get("cargoId") != null ? itemCargo.get("cargoId").toString() : "CGO";
                    String name = itemCargo.get("cargoName") != null ? itemCargo.get("cargoName").toString() : "";
                    String itemStr = id + " — " + name;
                    newItems.add(itemStr);
                    if (!currentCId.isBlank() && id.equalsIgnoreCase(currentCId)) {
                        reselect = itemStr;
                    }
                }
                if (!newItems.equals(new java.util.ArrayList<>(shipmentSelector.getItems()))) {
                    shipmentSelector.getItems().setAll(newItems);
                    if (reselect != null) {
                        shipmentSelector.setValue(reselect);
                    } else if (!shipmentSelector.getItems().isEmpty()) {
                        shipmentSelector.setValue(shipmentSelector.getItems().get(0));
                    }
                }
            }
            updateTrackingData.run();
        };

        shipmentSelector.setOnAction(e -> updateTrackingData.run());
        if (!listenersRegistered) {
            CargoDao.addChangeListener(syncSelectorAndData);
            ContainerDao.addChangeListener(syncSelectorAndData);
            com.projectui.view.ShipCaptain.CaptainSessionService.addChangeListener(syncSelectorAndData);
            listenersRegistered = true;
        }
        updateTrackingData.run();

        content.getChildren().addAll(headerBanner, searchCard, workspace);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
                "-fx-background-color: transparent; -fx-background: " + BG_COLOR + "; -fx-border-color: transparent;");
        return scrollPane;
    }

    private boolean hasRequiredDocuments(Map<String, Object> cargo) {
        Object rawDocuments = cargo != null ? cargo.get("documents") : null;
        if (!(rawDocuments instanceof List<?> documents) || documents.isEmpty()) return false;
        Set<String> requiredTypes = Set.of("commercial invoice", "packing list", "bill of lading / shipping order");
        for (String requiredType : requiredTypes) {
            boolean found = false;
            for (Object rawDocument : documents) {
                if (!(rawDocument instanceof Map<?, ?> document)) continue;
                Object rawType = document.containsKey("docType") ? document.get("docType") : "";
                String type = String.valueOf(rawType).trim().toLowerCase();
                Object rawStatus = document.containsKey("status") ? document.get("status") : "Pending";
                String status = String.valueOf(rawStatus);
                if (type.equals(requiredType) && !status.equalsIgnoreCase("Rejected")) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private boolean vesselHasDeparted(Map<String, Object> cargo) {
        String status = getStatus(cargo, "status", "loadingStatus", "vesselStatus", "shipStatus");
        if (containsStatus(status, "transit", "underway", "departed", "departing", "arrived", "delivered")) return true;
        String vesselName = getString(cargo, "assignedShip", "shipName", "vesselName", "vessel");
        com.projectui.service.VesselData vessel = com.projectui.service.VesselManager.getInstance().getVesselByName(vesselName);
        String navigation = vessel != null ? vessel.navigationStatus : "";
        if (containsStatus(navigation, "transit", "underway", "departed", "departing", "arrived", "delivered")) return true;
        return containsStatus(findPersistedShipStatus(cargo), "transit", "underway", "departed", "departing", "arrived", "delivered");
    }

    private boolean vesselHasArrived(Map<String, Object> cargo, String destination) {
        String status = getStatus(cargo, "arrivalStatus", "destinationStatus", "deliveryStatus", "status");
        if (containsStatus(status, "arrived", "delivered", "at destination")) return true;
        String vesselName = getString(cargo, "assignedShip", "shipName", "vesselName", "vessel");
        com.projectui.service.VesselData vessel = com.projectui.service.VesselManager.getInstance().getVesselByName(vesselName);
        boolean destinationMatches = vessel != null && (vessel.destination == null || vessel.destination.isBlank()
            || vessel.destination.equalsIgnoreCase(destination)
            || vessel.destination.toLowerCase().contains(destination.toLowerCase())
            || destination.toLowerCase().contains(vessel.destination.toLowerCase()));
        if (destinationMatches && (containsStatus(vessel.navigationStatus, "arrived", "delivered")
                || containsStatus(vessel.navigationStatus, "at berth", "moored", "docked"))) return true;
        String persistedStatus = findPersistedShipStatus(cargo);
        return containsStatus(persistedStatus, "arrived", "delivered", "at destination")
                || (containsStatus(persistedStatus, "at berth", "moored", "docked")
                        && persistedShipMatchesDestination(cargo, destination));
    }

    private String findPersistedShipStatus(Map<String, Object> cargo) {
        String shipId = getString(cargo, "assignedShipId", "shipId", "shipImo", "imoNumber");
        String shipName = getString(cargo, "assignedShip", "shipName", "vesselName", "vessel");
        try {
            for (com.projectui.model.Ship ship : new com.projectui.dao.ShipDAO().getShips()) {
                if ((shipId != null && !shipId.isBlank() && (shipId.equalsIgnoreCase(ship.getShipId())
                        || shipId.equalsIgnoreCase(ship.getImoNumber())))
                        || (shipName != null && !shipName.isBlank() && ship.getShipName() != null
                                && shipName.equalsIgnoreCase(ship.getShipName()))) {
                    return ship.getStatus() != null ? ship.getStatus() : "";
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    private boolean persistedShipMatchesDestination(Map<String, Object> cargo, String destination) {
        String shipId = getString(cargo, "assignedShipId", "shipId", "shipImo", "imoNumber");
        String shipName = getString(cargo, "assignedShip", "shipName", "vesselName", "vessel");
        try {
            for (com.projectui.model.Ship ship : new com.projectui.dao.ShipDAO().getShips()) {
                boolean matches = (shipId != null && !shipId.isBlank() && (shipId.equalsIgnoreCase(ship.getShipId())
                        || shipId.equalsIgnoreCase(ship.getImoNumber())))
                        || (shipName != null && !shipName.isBlank() && ship.getShipName() != null
                                && shipName.equalsIgnoreCase(ship.getShipName()));
                if (matches && ship.getDeparture() != null && !ship.getDeparture().isBlank()) {
                    return ship.getDeparture().equalsIgnoreCase(destination)
                            || ship.getDeparture().toLowerCase().contains(destination.toLowerCase())
                            || destination.toLowerCase().contains(ship.getDeparture().toLowerCase());
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean hasValue(Map<String, Object> map, String key) {
        return map != null && map.get(key) != null && !map.get(key).toString().isBlank();
    }

    private String getString(Map<String, Object> map, String... keys) {
        if (map != null) {
            for (String key : keys) {
                if (map.get(key) != null && !map.get(key).toString().isBlank()) return map.get(key).toString();
            }
        }
        return "";
    }

    private String getStatus(Map<String, Object> map, String... keys) {
        return getString(map, keys).toLowerCase();
    }

    private boolean containsStatus(Map<String, Object> map, String... values) {
        return containsStatus(getString(map, "status"), values);
    }

    private boolean containsStatus(String status, String... values) {
        String normalized = status != null ? status.toLowerCase() : "";
        for (String value : values) if (normalized.contains(value.toLowerCase())) return true;
        return false;
    }

    // =========================================================
    // HELPER COMPONENTS
    // =========================================================

    private VBox createCard() {
        VBox card = new VBox(18);
        card.setPadding(new Insets(24));
        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 16px;" +
                        "-fx-border-color: " + BORDER_COLOR + ";" +
                        "-fx-border-radius: 16px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.04), 10, 0, 0, 4);");
        return card;
    }

    private VBox createTile(String label, String value) {
        VBox tile = new VBox(4);
        tile.setPadding(new Insets(12, 14, 12, 14));
        GridPane.setHgrow(tile, Priority.ALWAYS);
        tile.setStyle(
                "-fx-background-color: #F8FAFC; -fx-background-radius: 8px; -fx-border-color: #F1F5F9; -fx-border-radius: 8px;");

        Text l = new Text(label);
        l.setStyle("-fx-font-size: 11px; -fx-fill: " + TEXT_MUTED + "; -fx-font-weight: bold;");

        Text v = new Text(value);
        v.setStyle("-fx-font-size: 14px; -fx-fill: " + TEXT_PRIMARY + "; -fx-font-weight: bold;");

        tile.getChildren().addAll(l, v);
        return tile;
    }

    private HBox createTimelineStage(String stageTitle, String timestamp, boolean completed, boolean showCheck) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);

        Label circle = new Label(showCheck ? "✓" : (completed ? "●" : "○"));
        circle.setMinSize(28, 28);
        circle.setAlignment(Pos.CENTER);

        if (completed) {
            circle.setStyle(
                    "-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50; -fx-font-size: 12px;");
        } else {
            circle.setStyle(
                    "-fx-background-color: #E2E8F0; -fx-text-fill: #64748B; -fx-font-weight: bold; -fx-background-radius: 50; -fx-font-size: 12px;");
        }

        VBox text = new VBox(2);
        Text titleText = new Text(stageTitle);
        titleText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: "
                + (completed ? TEXT_PRIMARY : TEXT_MUTED) + ";");

        Text timeText = new Text(timestamp);
        timeText.setStyle("-fx-font-size: 12px; -fx-fill: " + TEXT_MUTED + ";");

        text.getChildren().addAll(titleText, timeText);
        row.getChildren().addAll(circle, text);

        return row;
    }

    private HBox createRouteNode(String portName, String sub, String color) {
        HBox node = new HBox(10);
        node.setAlignment(Pos.CENTER_LEFT);

        Label dot = new Label("●");
        dot.setStyle("-fx-font-size: 18px; -fx-text-fill: " + color + ";");

        VBox info = new VBox(2);
        Text p = new Text(portName);
        p.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text s = new Text(sub);
        s.setStyle("-fx-font-size: 11px; -fx-fill: " + TEXT_MUTED + ";");
        info.getChildren().addAll(p, s);

        node.getChildren().addAll(dot, info);
        return node;
    }

    private Region createVerticalLine(boolean active) {
        Region line = new Region();
        line.setPrefWidth(3);
        line.setPrefHeight(20);
        line.setStyle("-fx-background-color: " + (active ? "#16A34A" : "#CBD5E1") + "; -fx-margin: 0 0 0 5;");
        return line;
    }

    private HBox createStatusAlert(String text, String bg, String fg, String border) {
        HBox item = new HBox();
        item.setPadding(new Insets(12));
        item.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + border
                + "; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + fg + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        item.getChildren().add(label);
        return item;
    }

    // =========================================================
    // SIDEBAR & NAVIGATION — PRESERVED
    // =========================================================

    private VBox createSidebar() {

        Map<String, Button> menuButtons = new LinkedHashMap<>();

        Button dashboard = new Button("🏠   Dashboard");
        Button register = new Button("📦  Register Cargo");
        Button book = new Button("🚢   Book Container");
        Button upload = new Button("📄   Upload Document");
        Button payment = new Button("💳   Make Payment");
        Button track = new Button("📥   Track Shipment");

        menuButtons.put("Dashboard", dashboard);
        menuButtons.put("Register Cargo", register);
        menuButtons.put("Book Container", book);
        menuButtons.put("Upload Document", upload);
        menuButtons.put("Make Payment", payment);
        menuButtons.put("Track Shipment", track);

        dashboard.setOnAction(e -> {
            if (ExporterDashboard.getInstance() != null) {
                ExporterDashboard.getInstance().switchPage("Dashboard");
            } else {
                back();
            }
        });
        register.setOnAction(e -> {
            if (ExporterDashboard.getInstance() != null) {
                ExporterDashboard.getInstance().switchPage("Register Cargo");
            } else {
                RegisterCargo p = new RegisterCargo();
                com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage,
                        p.getRegisterCargoScene(this::back).getRoot());
            }
        });

        book.setOnAction(e -> {
            if (ExporterDashboard.getInstance() != null) {
                ExporterDashboard.getInstance().switchPage("Book Container");
            } else {
                BookContainer p = new BookContainer();
                com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage,
                        p.getBookContainerScene(this::back).getRoot());
            }
        });

        upload.setOnAction(e -> {
            if (ExporterDashboard.getInstance() != null) {
                ExporterDashboard.getInstance().switchPage("Upload Document");
            } else {
                UploadDocument p = new UploadDocument();
                com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage,
                        p.getUploadDocumentScene(this::back).getRoot());
            }
        });

        payment.setOnAction(e -> {
            if (ExporterDashboard.getInstance() != null) {
                ExporterDashboard.getInstance().switchPage("Make Payment");
            } else {
                MakePayment p = new MakePayment();
                com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage,
                        p.getMakePaymentScene(this::back).getRoot());
            }
        });

        track.setOnAction(e -> {
            if (ExporterDashboard.getInstance() != null) {
                ExporterDashboard.getInstance().switchPage("Track Shipment");
            }
        });

        return com.projectui.view.login.DashboardLayoutHelper.createSidebar(ExporterDashboard.dashboardStage,
                "Exporter", "Track Shipment", menuButtons);
    }

    private void back() {
        if (ExporterDashboard.getInstance() != null) {
            ExporterDashboard.getInstance().switchPage("Dashboard");
        } else {
            new ExporterDashboard().start(ExporterDashboard.dashboardStage);
        }
    }

    private HBox createHeader(String pageTitle, String breadcrumb) {
        Runnable logoutAction = () -> {
            Logout page = new Logout();
            page.showLogout();
        };
        return com.projectui.view.login.DashboardLayoutHelper.createHeader(
                ExporterDashboard.dashboardStage,
                pageTitle,
                breadcrumb,
                "Exporter",
                logoutAction,
                null);
    }
}
