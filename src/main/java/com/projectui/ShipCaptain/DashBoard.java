package com.projectui.ShipCaptain;

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

public class DashBoard extends Application {

    private final BorderPane root = new BorderPane();
    private Button activeButton;
    private Stage stage;

    @Override
    public void start(Stage stage) {

        this.stage = stage;

        root.setStyle("-fx-background-color:#f3f8ff;");
        root.setLeft(sidebar());
        root.setTop(header());
        root.setCenter(dashboard());

        stage.setScene(getDashboardScene());
        stage.setTitle("Ship Captain Dashboard");
        stage.show();
    }

    // ================= DASHBOARD SCENE =================

    public Scene getDashboardScene() {
        return new Scene(root, 1550, 800);
    }

    // ================= SIDEBAR =================

    private VBox sidebar() {

        VBox side = new VBox(8);
        side.setPrefWidth(220);
        side.setPadding(new Insets(22,14,20,14));
        side.setStyle("-fx-background-color:#050911;");

        Label logo = new Label("⚓  DOCK TECH");
        logo.setStyle(
            "-fx-text-fill:white;" +
            "-fx-font-size:18;" +
            "-fx-font-weight:bold;"
        );

        Label sub = new Label("SMART PORT\nOPERATIONS");
        sub.setStyle(
            "-fx-text-fill:#9aaac0;" +
            "-fx-font-size:9;" +
            "-fx-font-weight:bold;"
        );

        VBox logoBox = new VBox(3, logo, sub);
        logoBox.setPadding(new Insets(0,0,22,10));

        Button dashboard = nav("⌂", "Dashboard");
        Button ship = nav("●", "View Assigned Ship");
        Button status = nav("◆", "Update Ship Status");
        Button loading = nav("♟", "Confirm Loading");
        Button document = nav("▣", "Upload Document");
        Button sos = nav("⚙", "Send SOS");

        // DASHBOARD
        dashboard.setOnAction(e -> {
            active(dashboard);
            root.setCenter(dashboard());
        });

        // ASSIGNED SHIP
        ship.setOnAction(e -> {
            active(ship);
            root.setCenter(
                new ViewAssignedShip().getShipScene().getRoot()
            );
        });

        // UPDATE STATUS
        status.setOnAction(e -> {
            active(status);
            root.setCenter(
                new UpdateShipStatus().getShipScene().getRoot()
            );
        });

        // CONFIRM LOADING
        loading.setOnAction(e -> {
            active(loading);
            root.setCenter(
                new ConfirmLoading().getScene().getRoot()
            );
        });

        // UPLOAD DOCUMENT
        document.setOnAction(e -> {
            active(document);
            root.setCenter(
                new UploadDocuments().getUploadContent()
            );
        });

        // SOS
        sos.setOnAction(e -> {
            active(sos);
            root.setCenter(
                new SendSOS().getSOSScene().getRoot()
            );
        });

        side.getChildren().addAll(
            logoBox,
            dashboard,
            ship,
            status,
            loading,
            document,
            sos
        );

        active(dashboard);

        return side;
    }

    // ================= NAV BUTTON =================

    private Button nav(String icon, String text) {

        Button b = new Button(icon + "   " + text);

        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(42);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(0,10,0,12));

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
            "-fx-background-radius:8;"
        );

        activeButton = selected;
    }

    private void normalStyle(Button b) {

        b.setStyle(
            "-fx-background-color:transparent;" +
            "-fx-text-fill:#aebbd0;" +
            "-fx-font-size:12;" +
            "-fx-font-weight:bold;" +
            "-fx-background-radius:8;"
        );
    }

    // ================= HEADER =================

    private HBox header() {

        HBox h = new HBox(15);

        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(12,22,12,25));
        h.setPrefHeight(65);
        h.setStyle("-fx-background-color:white;");

        Label title = new Label("Dashboard");

        title.setStyle(
            "-fx-font-size:22;" +
            "-fx-font-weight:bold;" +
            "-fx-text-fill:#172033;"
        );

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
            "-fx-padding:6 10;"
        );

        // ================= DATE & TIME =================

        Label date = new Label();
        Label time = new Label();

        date.setStyle(
            "-fx-font-size:12;" +
            "-fx-text-fill:#596579;" +
            "-fx-font-weight:bold;"
        );

        time.setStyle(
            "-fx-font-size:12;" +
            "-fx-text-fill:#172033;" +
            "-fx-font-weight:bold;"
        );

        VBox dateTime = new VBox(2, date, time);
        dateTime.setAlignment(Pos.CENTER_RIGHT);

        DateTimeFormatter dateFormat =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

        DateTimeFormatter timeFormat =
            DateTimeFormatter.ofPattern("hh:mm:ss a");

        Timeline clock = new Timeline(
            new KeyFrame(
                Duration.ZERO,
                e -> {

                    LocalDateTime now =
                        LocalDateTime.now();

                    date.setText(
                        now.format(dateFormat)
                    );

                    time.setText(
                        now.format(timeFormat)
                    );
                }
            ),
            new KeyFrame(
                Duration.seconds(1)
            )
        );

        clock.setCycleCount(
            Timeline.INDEFINITE
        );

        clock.play();

        // ================= NOTIFICATION =================

        Label notification = new Label("🔔");

        notification.setStyle(
            "-fx-font-size:18;"
        );

        // ================= AVATAR =================

        Label avatar = new Label("A");

        avatar.setMinSize(32,32);
        avatar.setAlignment(Pos.CENTER);

        avatar.setStyle(
            "-fx-background-color:#dbeafe;" +
            "-fx-background-radius:50;" +
            "-fx-text-fill:#0f5fff;" +
            "-fx-font-weight:bold;"
        );

        Label captain = new Label("Ship Captain");

        captain.setStyle(
            "-fx-font-size:13;" +
            "-fx-font-weight:bold;"
        );

        h.getChildren().addAll(
            title,
            space,
            search,
            system,
            dateTime,
            notification,
            avatar,
            captain
        );

        return h;
    }

    // ================= DASHBOARD =================

    private VBox dashboard() {

        VBox main = new VBox(14);
        main.setPadding(new Insets(18,20,20,20));

        Label title =
            new Label("Ship Captain Dashboard");

        title.setStyle(
            "-fx-font-size:22;" +
            "-fx-font-weight:bold;"
        );

        Label sub =
            new Label(
                "Monitor your assigned vessel and current voyage operations"
            );

        sub.setStyle(
            "-fx-font-size:13;" +
            "-fx-text-fill:#687386;"
        );

        HBox row1 = new HBox(15);

        VBox ship = card(
            "ASSIGNED SHIP",
            "Oceanic Star",
            "⚓ Captain: John Smith\nIMO: 1234567"
        );

        VBox status = new VBox(
            15,
            new Label("CURRENT STATUS"),
            new Label("● At Berth"),
            new Label("Current Berth: B04")
        );

        styleStatus(status);

        row1.getChildren().addAll(
            ship,
            status
        );

        HBox.setHgrow(
            ship,
            Priority.ALWAYS
        );

        HBox.setHgrow(
            status,
            Priority.ALWAYS
        );

        HBox row2 = new HBox(14);

        VBox operation = new VBox(
            10,
            new Label("NEXT OPERATION"),
            new Label("Cargo Loading"),
            new Label("10:30 AM")
        );

        styleOperation(operation);

        ProgressBar progress =
            new ProgressBar(0.72);

        progress.setMaxWidth(
            Double.MAX_VALUE
        );

        VBox loading = new VBox(
            10,
            new Label("LOADING PROGRESS"),
            new Label("72%"),
            progress,
            new Label("612 of 850 Tons loaded")
        );

        styleLoading(loading);

        row2.getChildren().addAll(
            operation,
            loading
        );

        HBox.setHgrow(
            operation,
            Priority.ALWAYS
        );

        HBox.setHgrow(
            loading,
            Priority.ALWAYS
        );

        VBox activity = new VBox(
            10,
            new Label("Recent Activity"),
            new Label(
                "✓ Vessel berthed at B04 - Today 10:20 AM"
            ),
            new Label(
                "✓ Status updated to At Berth - Today 10:24 AM"
            ),
            new Label(
                "✓ Cargo loading started - Today 10:30 AM"
            )
        );

        activity.setPadding(
            new Insets(20)
        );

        activity.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:10;"
        );

        activity.getChildren()
            .get(0)
            .setStyle(
                "-fx-font-size:14;" +
                "-fx-font-weight:bold;"
            );

        main.getChildren().addAll(
            title,
            sub,
            row1,
            row2,
            activity
        );

        return main;
    }

    // ================= CARD =================

    private VBox card(
        String small,
        String big,
        String details
    ) {

        VBox box = new VBox(15);

        Label s = new Label(small);

        s.setStyle(
            "-fx-font-size:12;" +
            "-fx-text-fill:#697386;" +
            "-fx-font-weight:bold;"
        );

        Label b = new Label(big);

        b.setStyle(
            "-fx-font-size:18;" +
            "-fx-font-weight:bold;"
        );

        Label d = new Label(details);

        d.setStyle(
            "-fx-font-size:13;" +
            "-fx-text-fill:#596579;"
        );

        box.getChildren().addAll(
            s,
            b,
            d
        );

        box.setPadding(
            new Insets(20)
        );

        box.setMinHeight(165);

        box.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:10;"
        );

        return box;
    }

    // ================= STATUS STYLE =================

    private void styleStatus(VBox box) {

        box.setPadding(
            new Insets(22)
        );

        box.setMinHeight(165);

        box.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:10;"
        );

        box.getChildren()
            .get(0)
            .setStyle(
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#697386;"
            );

        box.getChildren()
            .get(1)
            .setStyle(
                "-fx-font-size:18;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#168548;"
            );

        box.getChildren()
            .get(2)
            .setStyle(
                "-fx-font-size:14;" +
                "-fx-text-fill:#596579;"
            );
    }

    // ================= OPERATION STYLE =================

    private void styleOperation(VBox box) {

        box.setPadding(
            new Insets(20)
        );

        box.setMinHeight(150);

        box.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:10;"
        );

        box.getChildren()
            .get(0)
            .setStyle(
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#697386;"
            );

        box.getChildren()
            .get(1)
            .setStyle(
                "-fx-font-size:18;" +
                "-fx-font-weight:bold;"
            );

        box.getChildren()
            .get(2)
            .setStyle(
                "-fx-font-size:13;" +
                "-fx-text-fill:#1684c5;"
            );
    }

    // ================= LOADING STYLE =================

    private void styleLoading(VBox box) {

        box.setPadding(
            new Insets(20)
        );

        box.setMinHeight(150);

        box.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:10;"
        );

        box.getChildren()
            .get(0)
            .setStyle(
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#697386;"
            );

        box.getChildren()
            .get(1)
            .setStyle(
                "-fx-font-size:18;" +
                "-fx-font-weight:bold;"
            );
    }

    // ================= OTHER PAGE =================

    private VBox page(String name) {

        VBox box = new VBox(15);

        box.setPadding(
            new Insets(25)
        );

        Label title =
            new Label(name);

        title.setStyle(
            "-fx-font-size:22;" +
            "-fx-font-weight:bold;"
        );

        VBox card =
            new VBox(
                new Label(name + " Page")
            );

        card.setPadding(
            new Insets(25)
        );

        card.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:10;"
        );

        box.getChildren().addAll(
            title,
            card
        );

        return box;
    }

    // ================= MAIN =================

    public static void main(String[] args) {
        launch(args);
    }
}