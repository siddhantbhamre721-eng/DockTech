package com.projectui.view.ShipCaptain;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("unused")
public class DashBoard extends Application {

    private final BorderPane root = new BorderPane();
    private Button activeButton;
    private Stage stage;

    private String currentPage = "Dashboard";

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        root.setStyle("-fx-background-color:#f3f8ff;");

        showPage("Dashboard", dashboard());

        stage.setTitle("Dock Tech - Ship Captain");
        com.projectui.view.login.LoginPage.showRoot(stage, root);
        stage.show();
    }

    public Scene getDashboardScene() {
        Scene scene = new Scene(root, 1550, 800);
        com.projectui.view.login.LoginPage.applyInvisibleScrollbar(scene);
        return scene;
    }

    private void showPage(String pageName, javafx.scene.Node pageContent) {
        this.currentPage = pageName;
        root.setCenter(pageContent);
        root.setLeft(createSidebar());
        root.setTop(createHeader());
    }

    private HBox createHeader() {
        String breadcrumb = "Dock Tech / Ship Captain / " + currentPage;
        return com.projectui.view.login.DashboardLayoutHelper.createHeader(stage, currentPage, breadcrumb,
                "Ship Captain", () -> {
                    new com.projectui.view.login.LoginPage().show(stage);
                }, null);
    }

    private VBox createSidebar() {
        java.util.Map<String, Button> menuButtons = new java.util.LinkedHashMap<>();
        Button dashboard = new Button("⌂   Dashboard");
        Button ship = new Button("●   View Assigned Ship");
        Button status = new Button("◆   Update Ship Status");
        Button loading = new Button("♟   Confirm Loading");
        Button document = new Button("▣   Upload Document");
        Button sos = new Button("⚡   Send SOS");
        Button settings = new Button("⚙   Settings");

        menuButtons.put("Dashboard", dashboard);
        menuButtons.put("View Assigned Ship", ship);
        menuButtons.put("Update Ship Status", status);
        menuButtons.put("Confirm Loading", loading);
        menuButtons.put("Upload Document", document);
        menuButtons.put("Send SOS", sos);
        menuButtons.put("Settings", settings);

        dashboard.setOnAction(e -> showPage("Dashboard", dashboard()));
        ship.setOnAction(e -> showPage("View Assigned Ship", new ViewAssignedShip().getShipScene().getRoot()));
        status.setOnAction(e -> showPage("Update Ship Status", new UpdateShipStatus().getShipScene().getRoot()));
        loading.setOnAction(e -> showPage("Confirm Loading", new ConfirmLoading().getScene().getRoot()));
        document.setOnAction(e -> showPage("Upload Document", new UploadDocuments().getUploadContent()));
        sos.setOnAction(e -> showPage("Send SOS", new SendSOS().getSOSScene().getRoot()));
        settings.setOnAction(e -> showPage("Settings", new SettingsPage().getContent()));

        return com.projectui.view.login.DashboardLayoutHelper.createSidebar(stage, "Ship Captain", currentPage,
                menuButtons);
    }

    // ================= NAV BUTTON =================

    private Button nav(String icon, String text) {

        Button b = new Button(icon + "   " + text);

        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(42);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(0, 10, 0, 12));

        normalStyle(b);

        return b;
    }

    // ================= ACTIVE =================

    private void active(Button selected) {

        if (activeButton != null)
            normalStyle(activeButton);

        selected.setStyle(
                "-fx-background-color:#1769e8;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:12;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:8;");

        activeButton = selected;
    }

    private void normalStyle(Button b) {

        b.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-text-fill:#aebbd0;" +
                        "-fx-font-size:12;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:8;");
    }

    // ================= HEADER =================

    private HBox header() {

        HBox h = new HBox(15);

        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(12, 22, 12, 25));
        h.setPrefHeight(65);
        h.setStyle("-fx-background-color:white;");

        Label title = new Label("Dashboard");

        title.setStyle(
                "-fx-font-size:22;" +
                        "-fx-font-weight:bold;" +
                        "-fx-text-fill:#172033;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("Search...");
        search.setPrefWidth(230);
        search.setPrefHeight(35);

        Label system = new Label("● System Operational");

        system.setStyle(
                "-fx-text-fill:#168548;" +
                        "-fx-background-color:#e7f7ee;" +
                        "-fx-padding:6 10;");

        // ================= DATE & TIME =================

        Label date = new Label();
        Label time = new Label();

        date.setStyle(
                "-fx-font-size:12;" +
                        "-fx-text-fill:#596579;" +
                        "-fx-font-weight:bold;");

        time.setStyle(
                "-fx-font-size:12;" +
                        "-fx-text-fill:#172033;" +
                        "-fx-font-weight:bold;");

        VBox dateTime = new VBox(2, date, time);
        dateTime.setAlignment(Pos.CENTER_RIGHT);

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy");

        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm:ss a");

        Timeline clock = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        e -> {

                            LocalDateTime now = LocalDateTime.now();

                            date.setText(
                                    now.format(dateFormat));

                            time.setText(
                                    now.format(timeFormat));
                        }),
                new KeyFrame(
                        Duration.seconds(1)));

        clock.setCycleCount(
                Timeline.INDEFINITE);

        clock.play();

        // ================= NOTIFICATION =================

        Label notification = new Label("🔔");

        notification.setStyle(
                "-fx-font-size:18;");

        // ================= AVATAR =================

        Label avatar = new Label("A");

        avatar.setMinSize(32, 32);
        avatar.setAlignment(Pos.CENTER);

        avatar.setStyle(
                "-fx-background-color:#dbeafe;" +
                        "-fx-background-radius:50;" +
                        "-fx-text-fill:#0f5fff;" +
                        "-fx-font-weight:bold;");

        Label captain = new Label("Ship Captain");

        captain.setStyle(
                "-fx-font-size:13;" +
                        "-fx-font-weight:bold;");

        h.getChildren().addAll(
                title,
                space,
                search,
                system,
                dateTime,
                notification,
                avatar,
                captain);

        return h;
    }

    // ================= DASHBOARD =================

    private VBox dashboard() {

        VBox main = new VBox(14);
        main.setPadding(new Insets(18, 20, 20, 20));

        Label title = new Label("Ship Captain Dashboard");

        title.setStyle(
                "-fx-font-size:22;" +
                        "-fx-font-weight:bold;");

        Label sub = new Label(
                "Monitor your assigned vessel and current voyage operations");

        sub.setStyle(
                "-fx-font-size:13;" +
                        "-fx-text-fill:#687386;");

        // 1. Fetch current logged-in captain profile
        java.util.Map<String, Object> user = com.projectui.view.login.LoginPage.loggedInUser;
        String captainName = "Ship Captain";
        String assignedShip = "";
        String shipImo = "";
        String assignedPort = "Mumbai Port";
        String duty = "Cargo Loading";

        if (user != null) {
            if (user.get("name") != null) captainName = user.get("name").toString();
            if (user.get("assignedShip") != null && !user.get("assignedShip").toString().isBlank()) {
                assignedShip = user.get("assignedShip").toString();
            } else if (user.get("company") != null && !user.get("company").toString().isBlank()) {
                assignedShip = user.get("company").toString();
            }
            if (user.get("shipImo") != null) shipImo = user.get("shipImo").toString();
            if (user.get("assignedPort") != null) assignedPort = user.get("assignedPort").toString();
            if (user.get("duty") != null && !user.get("duty").toString().isBlank()) duty = user.get("duty").toString();
        }

        // 2. Query ShipDAO for ship matching assignedShip or shipImo
        com.projectui.model.Ship matchedShip = null;
        try {
            java.util.List<com.projectui.model.Ship> ships = new com.projectui.dao.ShipDAO().getShips();
            if (ships != null) {
                for (com.projectui.model.Ship s : ships) {
                    if (s != null) {
                        if (!assignedShip.isBlank() && s.getShipName() != null && s.getShipName().equalsIgnoreCase(assignedShip)) {
                            matchedShip = s;
                            break;
                        }
                        if (!shipImo.isBlank() && s.getImoNumber() != null && s.getImoNumber().equalsIgnoreCase(shipImo)) {
                            matchedShip = s;
                            break;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        // Fallback display strings
        String shipDisplay = !assignedShip.isBlank() ? assignedShip : (matchedShip != null ? matchedShip.getShipName() : "No ship assigned");
        String imoDisplay = matchedShip != null && matchedShip.getImoNumber() != null && !matchedShip.getImoNumber().isBlank()
                ? matchedShip.getImoNumber()
                : (!shipImo.isBlank() ? shipImo : "Not Assigned");
        String statusDisplay = matchedShip != null && matchedShip.getStatus() != null && !matchedShip.getStatus().isBlank()
                ? matchedShip.getStatus()
                : (!assignedShip.isBlank() ? "At Berth" : "No Ship Assigned");
        String berthDisplay = matchedShip != null && matchedShip.getBerth() != null && !matchedShip.getBerth().isBlank()
                ? matchedShip.getBerth()
                : (!assignedShip.isBlank() ? "B04" : "Not Assigned");
        String operationDisplay = !assignedShip.isBlank() ? duty : "No upcoming operation";
        String timeDisplay = !assignedShip.isBlank() ? "10:30 AM" : "--";

        // 3. Row 1: Assigned Ship & Current Status
        HBox row1 = new HBox(15);
        VBox ship = card("ASSIGNED SHIP", shipDisplay, "⚓ Captain: " + captainName + "\nIMO: " + imoDisplay);

        VBox status = new VBox(
                15,
                new Label("CURRENT STATUS"),
                new Label("● " + statusDisplay),
                new Label("Current Berth: " + berthDisplay));

        styleStatus(status);

        row1.getChildren().addAll(
                ship,
                status);

        HBox.setHgrow(
                ship,
                Priority.ALWAYS);

        HBox.setHgrow(
                status,
                Priority.ALWAYS);

        // 4. Row 2: Next Operation & Loading Progress
        HBox row2 = new HBox(14);

        VBox operation = new VBox(
                10,
                new Label("NEXT OPERATION"),
                new Label(operationDisplay),
                new Label(timeDisplay));

        styleOperation(operation);

        // Calculate progress dynamically
        double progressVal = 0.0;
        String pctText = "0%";
        String cargoDetailText = "No cargo data available";

        if (!assignedShip.isBlank()) {
            java.util.List<java.util.Map<String, Object>> cargos = new com.projectui.dao.CargoDao().getAllCargos();
            int totalTons = 0;
            int loadedTons = 0;
            boolean foundCargo = false;
            if (cargos != null) {
                for (java.util.Map<String, Object> c : cargos) {
                    if (c != null) {
                        totalTons += 250;
                        String st = c.get("status") != null ? c.get("status").toString() : "";
                        if (st.equalsIgnoreCase("Registered") || st.equalsIgnoreCase("Loaded") || st.equalsIgnoreCase("In Transit") || st.equalsIgnoreCase("Completed")) {
                            loadedTons += 180;
                        }
                        foundCargo = true;
                    }
                }
            }
            if (foundCargo && totalTons > 0) {
                progressVal = Math.min(1.0, (double) loadedTons / totalTons);
                int pct = (int) Math.round(progressVal * 100);
                pctText = pct + "%";
                cargoDetailText = loadedTons + " of " + totalTons + " Tons loaded";
            } else {
                progressVal = 0.72;
                pctText = "72%";
                cargoDetailText = "612 of 850 Tons loaded";
            }
        }

        ProgressBar progress = new ProgressBar(progressVal);

        progress.setMaxWidth(
                Double.MAX_VALUE);

        VBox loading = new VBox(
                10,
                new Label("LOADING PROGRESS"),
                new Label(pctText),
                progress,
                new Label(cargoDetailText));

        styleLoading(loading);

        row2.getChildren().addAll(
                operation,
                loading);

        HBox.setHgrow(
                operation,
                Priority.ALWAYS);

        HBox.setHgrow(
                loading,
                Priority.ALWAYS);

        // 5. Voyage Map Card
        VBox voyageMapCard = new VBox(14);
        voyageMapCard.setPadding(new Insets(20));
        voyageMapCard.setStyle("-fx-background-color:white; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10;");

        Label mapTitle = new Label("VOYAGE MAP & ACTIVE ROUTE");
        mapTitle.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#172033;");

        String originPort = !assignedPort.isBlank() ? assignedPort : "Port of Mumbai";
        String currLoc = !assignedShip.isBlank() ? "Arabian Sea (18.9°N, 71.5°E)" : "Docked";
        String destPort = "Port of Singapore";

        HBox routeLine = new HBox(16);
        routeLine.setAlignment(Pos.CENTER);
        routeLine.setPadding(new Insets(14, 10, 14, 10));

        VBox startNode = new VBox(4,
                new Label("STARTING POINT"),
                new Label("📍 " + originPort)
        );
        startNode.setAlignment(Pos.CENTER);
        startNode.setStyle("-fx-background-color:#f8fafc; -fx-padding:10 16; -fx-background-radius:8; -fx-border-color:#cbd5e1; -fx-border-radius:8;");
        startNode.getChildren().get(0).setStyle("-fx-font-size:10px; -fx-text-fill:#64748b; -fx-font-weight:bold;");
        startNode.getChildren().get(1).setStyle("-fx-font-size:13px; -fx-text-fill:#0f172a; -fx-font-weight:bold;");

        Label arrow1 = new Label("────────►");
        arrow1.setStyle("-fx-text-fill:#0b63ff; -fx-font-size:16px; -fx-font-weight:bold;");

        VBox currentLocNode = new VBox(4,
                new Label("CURRENT LOCATION"),
                new Label("🚢 " + currLoc)
        );
        currentLocNode.setAlignment(Pos.CENTER);
        currentLocNode.setStyle("-fx-background-color:#eff6ff; -fx-padding:10 16; -fx-background-radius:8; -fx-border-color:#3b82f6; -fx-border-radius:8;");
        currentLocNode.getChildren().get(0).setStyle("-fx-font-size:10px; -fx-text-fill:#1d4ed8; -fx-font-weight:bold;");
        currentLocNode.getChildren().get(1).setStyle("-fx-font-size:13px; -fx-text-fill:#1e3a8a; -fx-font-weight:bold;");

        Label arrow2 = new Label("────────►");
        arrow2.setStyle("-fx-text-fill:#0b63ff; -fx-font-size:16px; -fx-font-weight:bold;");

        VBox destNode = new VBox(4,
                new Label("DESTINATION"),
                new Label("🏁 " + destPort)
        );
        destNode.setAlignment(Pos.CENTER);
        destNode.setStyle("-fx-background-color:#f0fdf4; -fx-padding:10 16; -fx-background-radius:8; -fx-border-color:#22c55e; -fx-border-radius:8;");
        destNode.getChildren().get(0).setStyle("-fx-font-size:10px; -fx-text-fill:#15803d; -fx-font-weight:bold;");
        destNode.getChildren().get(1).setStyle("-fx-font-size:13px; -fx-text-fill:#14532d; -fx-font-weight:bold;");

        routeLine.getChildren().addAll(startNode, arrow1, currentLocNode, arrow2, destNode);
        HBox.setHgrow(startNode, Priority.ALWAYS);
        HBox.setHgrow(currentLocNode, Priority.ALWAYS);
        HBox.setHgrow(destNode, Priority.ALWAYS);

        voyageMapCard.getChildren().addAll(mapTitle, routeLine);

        // 6. Activity Card
        VBox activity = new VBox(10);
        activity.setPadding(new Insets(20));
        activity.setStyle("-fx-background-color:white; -fx-background-radius:10;");

        Label actHead = new Label("Recent Activity");
        actHead.setStyle("-fx-font-size:14; -fx-font-weight:bold;");
        activity.getChildren().add(actHead);

        if (!assignedShip.isBlank()) {
            activity.getChildren().addAll(
                    new Label("✓ Vessel " + shipDisplay + " berthed at " + berthDisplay + " - Today 10:20 AM"),
                    new Label("✓ Status updated to " + statusDisplay + " - Today 10:24 AM"),
                    new Label("✓ " + operationDisplay + " started at " + assignedPort + " - Today 10:30 AM")
            );
        } else {
            Label noAct = new Label("No recent activity recorded for this vessel.");
            noAct.setStyle("-fx-font-size:12; -fx-text-fill:#94a3b8;");
            activity.getChildren().add(noAct);
        }

        main.getChildren().addAll(
                title,
                sub,
                row1,
                row2,
                voyageMapCard,
                activity);

        return main;
    }

    // ================= CARD =================

    private VBox card(
            String small,
            String big,
            String details) {

        VBox box = new VBox(15);

        Label s = new Label(small);

        s.setStyle(
                "-fx-font-size:12;" +
                        "-fx-text-fill:#697386;" +
                        "-fx-font-weight:bold;");

        Label b = new Label(big);

        b.setStyle(
                "-fx-font-size:18;" +
                        "-fx-font-weight:bold;");

        Label d = new Label(details);

        d.setStyle(
                "-fx-font-size:13;" +
                        "-fx-text-fill:#596579;");

        box.getChildren().addAll(
                s,
                b,
                d);

        box.setPadding(
                new Insets(20));

        box.setMinHeight(165);

        box.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:10;");

        return box;
    }

    // ================= STATUS STYLE =================

    private void styleStatus(VBox box) {

        box.setPadding(
                new Insets(22));

        box.setMinHeight(165);

        box.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:10;");

        box.getChildren()
                .get(0)
                .setStyle(
                        "-fx-font-size:12;" +
                                "-fx-font-weight:bold;" +
                                "-fx-text-fill:#697386;");

        box.getChildren()
                .get(1)
                .setStyle(
                        "-fx-font-size:18;" +
                                "-fx-font-weight:bold;" +
                                "-fx-text-fill:#168548;");

        box.getChildren()
                .get(2)
                .setStyle(
                        "-fx-font-size:14;" +
                                "-fx-text-fill:#596579;");
    }

    // ================= OPERATION STYLE =================

    private void styleOperation(VBox box) {

        box.setPadding(
                new Insets(20));

        box.setMinHeight(150);

        box.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:10;");

        box.getChildren()
                .get(0)
                .setStyle(
                        "-fx-font-size:12;" +
                                "-fx-font-weight:bold;" +
                                "-fx-text-fill:#697386;");

        box.getChildren()
                .get(1)
                .setStyle(
                        "-fx-font-size:18;" +
                                "-fx-font-weight:bold;");

        box.getChildren()
                .get(2)
                .setStyle(
                        "-fx-font-size:13;" +
                                "-fx-text-fill:#1684c5;");
    }

    // ================= LOADING STYLE =================

    private void styleLoading(VBox box) {

        box.setPadding(
                new Insets(20));

        box.setMinHeight(150);

        box.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:10;");

        box.getChildren()
                .get(0)
                .setStyle(
                        "-fx-font-size:12;" +
                                "-fx-font-weight:bold;" +
                                "-fx-text-fill:#697386;");

        box.getChildren()
                .get(1)
                .setStyle(
                        "-fx-font-size:18;" +
                                "-fx-font-weight:bold;");
    }

    // ================= OTHER PAGE =================

    private VBox page(String name) {

        VBox box = new VBox(15);

        box.setPadding(
                new Insets(25));

        Label title = new Label(name);

        title.setStyle(
                "-fx-font-size:22;" +
                        "-fx-font-weight:bold;");

        VBox card = new VBox(
                new Label(name + " Page"));

        card.setPadding(
                new Insets(25));

        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:10;");

        box.getChildren().addAll(
                title,
                card);

        return box;
    }

    // ================= MAIN =================

}