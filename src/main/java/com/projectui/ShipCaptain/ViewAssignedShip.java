package com.projectui.ShipCaptain;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ViewAssignedShip {
    public Node getshipContent() {

        // MAIN
        VBox main = new VBox(15);
        main.setPadding(new Insets(20, 35, 30, 35));
        main.setStyle("-fx-background-color:#f7faff;");

        // ================= HEADER =================

        VBox heading = new VBox(3);

        Label title = new Label("Assigned Ship");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));

        Label desc = new Label(
            "View details of your assigned vessel."
        );
        desc.setTextFill(Color.GRAY);
        desc.setFont(Font.font("Arial", 14));

        heading.getChildren().addAll(title, desc);

        TextField search = new TextField();
        search.setPromptText("⌕  Search...");
        search.setPrefWidth(280);
        search.setPrefHeight(40);

        search.setStyle(
            "-fx-background-color:white;" +
            "-fx-border-color:#e5e7eb;" +
            "-fx-border-radius:7;" +
            "-fx-background-radius:7;"
        );

        Button bell = new Button("🔔");
        bell.setPrefSize(45,40);

        bell.setStyle(
            "-fx-background-color:white;" +
            "-fx-border-color:#e5e7eb;" +
            "-fx-background-radius:7;" +
            "-fx-border-radius:7;"
        );

        Label avatar = new Label("●");
        avatar.setStyle(
            "-fx-text-fill:#1769e8;" +
            "-fx-font-size:20;"
        );

        Label captain = new Label("Ship Captain ⌄");
        captain.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        HBox user = new HBox(7, avatar, captain);
        user.setAlignment(Pos.CENTER);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox top = new HBox(
            15, heading, space, search, bell, user
        );

        top.setAlignment(Pos.CENTER_LEFT);

        // ================= SHIP CARD =================

        VBox card = new VBox();
        card.setMaxWidth(900);

        card.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:12;" +
            "-fx-border-color:#e1e6ed;" +
            "-fx-border-radius:12;"
        );

        // SHIP IMAGE
        StackPane imageBox = new StackPane();

        Image image = new Image(
            getClass()
            .getResource("/ship1.jpeg")
            .toExternalForm()
        );

        ImageView shipImage = new ImageView(image);

        shipImage.setFitWidth(900);
        shipImage.setFitHeight(230);
        shipImage.setPreserveRatio(false);

        imageBox.getChildren().add(shipImage);

        // DARK OVERLAY
        Region overlay = new Region();

        overlay.setStyle(
            "-fx-background-color:rgba(0,0,0,0.35);"
        );

        overlay.setPrefHeight(230);

        // SHIP NAME
        VBox shipName = new VBox(2);

        Label name = new Label("⚓  Oceanic Star");
        name.setTextFill(Color.WHITE);
        name.setFont(
            Font.font("Arial", FontWeight.BOLD, 25)
        );

        Label type = new Label("Container Vessel");
        type.setTextFill(Color.LIGHTGRAY);
        type.setFont(Font.font("Arial", 14));

        shipName.getChildren().addAll(name, type);

        StackPane.setAlignment(
            shipName,
            Pos.BOTTOM_LEFT
        );

        StackPane.setMargin(
            shipName,
            new Insets(0,0,25,30)
        );

        // STATUS
        Label status = new Label("●  At Berth");

        status.setStyle(
            "-fx-background-color:#36c878;" +
            "-fx-text-fill:white;" +
            "-fx-padding:8 16;" +
            "-fx-background-radius:20;" +
            "-fx-font-weight:bold;"
        );

        StackPane.setAlignment(
            status,
            Pos.TOP_RIGHT
        );

        StackPane.setMargin(
            status,
            new Insets(25,25,0,0)
        );

        imageBox.getChildren().addAll(
            overlay,
            shipName,
            status
        );

        // ================= DETAILS =================

        GridPane details = new GridPane();

        details.setPadding(
            new Insets(25,30,25,30)
        );

        details.setHgap(45);
        details.setVgap(0);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);

        details.getColumnConstraints().addAll(c1,c2);

        // LEFT COLUMN
        VBox left = new VBox();

        left.getChildren().addAll(
            row("⚓", "Ship Name", "Oceanic Star"),
            line(),
            row("◉", "Ship Type", "Container Vessel"),
            line(),
            row("♙", "Captain", "John Smith"),
            line(),
            row("⚓", "Current Berth", "B04"),
            line(),
            row("◷", "Expected Departure", "06:30 PM")
        );

        // RIGHT COLUMN
        VBox right = new VBox();

        right.getChildren().addAll(
            row("#", "IMO Number", "1234567"),
            line(),
            row("▣", "Shipping Company", "Ocean Shipping Ltd."),
            line(),
            row("⌖", "Current Port", "Mumbai"),
            line(),
            row("◴", "Arrival", "10:20 AM"),
            line(),
            statusRow()
        );

        details.add(left,0,0);
        details.add(right,1,0);

        card.getChildren().addAll(
            imageBox,
            details
        );

        main.getChildren().addAll(
            top,
            card
        );

        return main;
    }

    // ================= DETAIL ROW =================

    VBox row(
        String icon,
        String label,
        String value
    ) {

        Label i = new Label(icon);
        i.setTextFill(Color.web("#6fa7dc"));
        i.setFont(Font.font(15));

        Label l = new Label(label);
        l.setTextFill(Color.web("#8993a3"));
        l.setFont(Font.font("Arial", 13));

        HBox left = new HBox(10, i, l);
        left.setAlignment(Pos.CENTER_LEFT);

        Label v = new Label(value);
        v.setFont(
            Font.font("Arial", FontWeight.BOLD, 13)
        );

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox box = new HBox(
            left, space, v
        );

        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefHeight(55);

        return new VBox(box);
    }

    // ================= LINE =================

    Region line() {

        Region line = new Region();

        line.setPrefHeight(1);

        line.setStyle(
            "-fx-background-color:#edf0f4;"
        );

        return line;
    }

    // ================= STATUS ROW =================

    VBox statusRow() {

        Label icon = new Label("〽");
        icon.setTextFill(Color.web("#6fa7dc"));

        Label label = new Label("Current Status");
        label.setTextFill(Color.web("#8993a3"));
        label.setFont(Font.font("Arial", 13));

        HBox left = new HBox(10, icon, label);
        left.setAlignment(Pos.CENTER_LEFT);

        Label value = new Label("●  At Berth");

        value.setStyle(
            "-fx-background-color:#e5f8ed;" +
            "-fx-text-fill:#168548;" +
            "-fx-padding:7 14;" +
            "-fx-background-radius:18;" +
            "-fx-font-weight:bold;"
        );

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox box = new HBox(
            left, space, value
        );

        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefHeight(55);

        return new VBox(box);
    }
}

    
