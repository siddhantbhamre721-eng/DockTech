package com.projectui.ShipCaptain;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashBoard {
    private BorderPane root = new BorderPane();

    public void start(Stage stage) {

        root.setStyle("-fx-background-color:#f3f8ff;");
        root.setLeft(sidebar());
        root.setTop(header());
        root.setCenter(dashboard());

        stage.setScene(new Scene(root, 1550, 800));
        stage.setTitle("Ship Captain Dashboard");
        stage.show();
    }

    // SIDEBAR
    VBox sidebar() {

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

        Button dashboard = nav("⌂","Dashboard");
        Button ship = nav("●","View Assigned Ship");
        Button status = nav("◆","Update Ship Status");
        Button loading = nav("♟","Confirm Loading");
        Button document = nav("▣","Upload Document");
        Button sos = nav("⚙","Send SOS");

        dashboard.setOnAction(e -> {
            active(side, dashboard);
            root.setCenter(dashboard());
        });

        ship.setOnAction(e -> {
            active(side, ship);
            root.setCenter(new ViewAssignedShip().getshipContent());
        });

        status.setOnAction(e -> {
            active(side, status);
            root.setCenter(page("Update Ship Status"));
        });

        loading.setOnAction(e -> {
            active(side, loading);
            root.setCenter(page("Confirm Loading"));
        });

        document.setOnAction(e -> {
            active(side, document);
            root.setCenter(page("Upload Document"));
        });

        sos.setOnAction(e -> {
            active(side, sos);
            root.setCenter(page("Send SOS"));
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

        active(side, dashboard);

        return side;
    }

    // NAV BUTTON
    Button nav(String icon, String text) {

        Button b = new Button(icon + "   " + text);

        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(42);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(0,10,0,12));

        b.setStyle(
            "-fx-background-color:transparent;" +
            "-fx-text-fill:#aebbd0;" +
            "-fx-font-size:12;" +
            "-fx-font-weight:bold;" +
            "-fx-background-radius:8;"
        );

        return b;
    }

    // ACTIVE BUTTON
    void active(VBox side, Button selected) {

        for (Node n : side.getChildren()) {

            if (n instanceof Button) {
                n.setStyle(
                    "-fx-background-color:transparent;" +
                    "-fx-text-fill:#aebbd0;" +
                    "-fx-font-size:12;" +
                    "-fx-font-weight:bold;" +
                    "-fx-background-radius:8;"
                );
            }
        }

        selected.setStyle(
            "-fx-background-color:#1769e8;" +
            "-fx-text-fill:white;" +
            "-fx-font-size:12;" +
            "-fx-font-weight:bold;" +
            "-fx-background-radius:8;"
        );
    }

    // HEADER
    HBox header() {

        HBox h = new HBox(15);

        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(12,22,12,25));
        h.setPrefHeight(65);
        h.setStyle("-fx-background-color:white;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("Search...");
        search.setPrefWidth(230);
        search.setPrefHeight(35);

        search.setStyle(
            "-fx-background-color:#f8fafc;" +
            "-fx-border-color:#e2e8f0;" +
            "-fx-border-radius:6;" +
            "-fx-background-radius:6;" +
            "-fx-font-size:13;"
        );

        Label system = new Label("● System Operational");

        system.setStyle(
            "-fx-text-fill:#168548;" +
            "-fx-background-color:#e7f7ee;" +
            "-fx-padding:6 10;"
        );

        Label avatar = new Label("A");

        avatar.setMinSize(32,32);
        avatar.setAlignment(Pos.CENTER);

        avatar.setStyle(
            "-fx-background-color:#dbeafe;" +
            "-fx-background-radius:50;" +
            "-fx-text-fill:#0f5fff;"
        );

        Label captain = new Label("Ship Captain");

        captain.setStyle(
            "-fx-font-size:13;" +
            "-fx-font-weight:bold;"
        );

        h.getChildren().addAll(
            space,
            search,
            system,
            new Label("🔔"),
            avatar,
            captain
        );

        return h;
    }

    // DASHBOARD
    VBox dashboard() {

        VBox main = new VBox(14);
        main.setPadding(new Insets(18,20,20,20));

        Label title = new Label("Ship Captain Dashboard");

        title.setStyle(
            "-fx-font-size:22;" +
            "-fx-font-weight:bold;"
        );

        Label sub = new Label(
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

        status.setPadding(new Insets(22));
        status.setMinHeight(165);

        status.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:10;"
        );

        status.getChildren().get(0).setStyle(
            "-fx-font-size:12;" +
            "-fx-font-weight:bold;" +
            "-fx-text-fill:#697386;"
        );

        status.getChildren().get(1).setStyle(
            "-fx-font-size:18;" +
            "-fx-font-weight:bold;" +
            "-fx-text-fill:#168548;"
        );

        status.getChildren().get(2).setStyle(
            "-fx-font-size:14;" +
            "-fx-text-fill:#596579;"
        );

        row1.getChildren().addAll(ship,status);

        HBox.setHgrow(ship,Priority.ALWAYS);
        HBox.setHgrow(status,Priority.ALWAYS);

        HBox row2 = new HBox(14);

        VBox operation = new VBox(
            10,
            new Label("NEXT OPERATION"),
            new Label("Cargo Loading"),
            new Label("10:30 AM")
        );

        operation.setPadding(new Insets(20));
        operation.setMinHeight(150);

        operation.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:10;"
        );

        operation.getChildren().get(0).setStyle(
            "-fx-font-size:12;" +
            "-fx-font-weight:bold;" +
            "-fx-text-fill:#697386;"
        );

        operation.getChildren().get(1).setStyle(
            "-fx-font-size:18;" +
            "-fx-font-weight:bold;"
        );

        operation.getChildren().get(2).setStyle(
            "-fx-font-size:13;" +
            "-fx-text-fill:#1684c5;"
        );

        ProgressBar progress = new ProgressBar(.72);
        progress.setMaxWidth(Double.MAX_VALUE);

        VBox loading = new VBox(
            10,
            new Label("LOADING PROGRESS"),
            new Label("72%"),
            progress,
            new Label("612 of 850 Tons loaded")
        );

        loading.setPadding(new Insets(20));
        loading.setMinHeight(150);

        loading.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:10;"
        );

        loading.getChildren().get(0).setStyle(
            "-fx-font-size:12;" +
            "-fx-font-weight:bold;" +
            "-fx-text-fill:#697386;"
        );

        loading.getChildren().get(1).setStyle(
            "-fx-font-size:18;" +
            "-fx-font-weight:bold;"
        );

        row2.getChildren().addAll(operation,loading);

        HBox.setHgrow(operation,Priority.ALWAYS);
        HBox.setHgrow(loading,Priority.ALWAYS);

        VBox activity = new VBox(
            10,
            new Label("Recent Activity"),
            new Label("✓ Vessel berthed at B04 - Today 10:20 AM"),
            new Label("✓ Status updated to At Berth - Today 10:24 AM"),
            new Label("✓ Cargo loading started - Today 10:30 AM")
        );

        activity.setPadding(new Insets(20));

        activity.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:10;"
        );

        activity.getChildren().get(0).setStyle(
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

    // SHIP CARD
    VBox card(String small, String big, String details) {

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

        box.getChildren().addAll(s,b,d);

        box.setPadding(new Insets(20));
        box.setMinHeight(165);

        box.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:10;"
        );

        return box;
    }

    // OTHER PAGES
    VBox page(String name) {

        VBox box = new VBox(15);
        box.setPadding(new Insets(25));

        Label title = new Label(name);

        title.setStyle(
            "-fx-font-size:22;" +
            "-fx-font-weight:bold;"
        );

        Label text = new Label(name + " Page");

        VBox card = new VBox(text);
        card.setPadding(new Insets(25));

        card.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:10;"
        );

        box.getChildren().addAll(title,card);

        return box;
    }

}
    

