package com.projectui.ShipCaptain;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class ViewAssignedShip {

    // ================= SCENE =================

    public Scene getShipScene() {

        VBox main = new VBox(15);
        main.setPadding(new Insets(20,35,30,35));
        main.setStyle("-fx-background-color:#f7faff;");

        // ================= HEADER =================

        VBox heading = new VBox(3);

        Label title = new Label("Assigned Ship");
        title.setFont(Font.font("Arial",FontWeight.BOLD,26));

        Label desc = new Label(
            "View details of your assigned vessel."
        );
        desc.setTextFill(Color.GRAY);

        heading.getChildren().addAll(title,desc);

        // ================= SHIP CARD =================

        VBox card = new VBox();

        card.setPrefWidth(900);
        card.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:12;" +
            "-fx-border-color:#e1e6ed;" +
            "-fx-border-radius:12;"
        );

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

        Region overlay = new Region();
        overlay.setStyle(
            "-fx-background-color:rgba(0,0,0,0.35);"
        );
        overlay.setPrefHeight(230);

        VBox shipName = new VBox(2);

        Label name = new Label("⚓  Oceanic Star");
        name.setTextFill(Color.WHITE);
        name.setFont(Font.font("Arial",FontWeight.BOLD,25));

        Label type = new Label("Container Vessel");
        type.setTextFill(Color.LIGHTGRAY);

        shipName.getChildren().addAll(name,type);

        StackPane.setAlignment(shipName,Pos.BOTTOM_LEFT);
        StackPane.setMargin(shipName,new Insets(0,0,25,30));

        Label status = new Label("●  At Berth");
        status.setStyle(
            "-fx-background-color:#36c878;" +
            "-fx-text-fill:white;" +
            "-fx-padding:8 16;" +
            "-fx-background-radius:20;" +
            "-fx-font-weight:bold;"
        );

        StackPane.setAlignment(status,Pos.TOP_RIGHT);
        StackPane.setMargin(status,new Insets(25));

        imageBox.getChildren().addAll(
            shipImage,
            overlay,
            shipName,
            status
        );

        // ================= DETAILS =================

        GridPane details = new GridPane();
        details.setPadding(new Insets(25,30,25,30));
        details.setHgap(45);

        ColumnConstraints c1 = new ColumnConstraints();
        ColumnConstraints c2 = new ColumnConstraints();

        c1.setPercentWidth(50);
        c2.setPercentWidth(50);

        details.getColumnConstraints().addAll(c1,c2);

        VBox left = new VBox();

        left.getChildren().addAll(
            row("⚓","Ship Name","Oceanic Star"),
            line(),
            row("◉","Ship Type","Container Vessel"),
            line(),
            row("♙","Captain","John Smith"),
            line(),
            row("⚓","Current Berth","B04"),
            line(),
            row("◷","Expected Departure","06:30 PM")
        );

        VBox right = new VBox();

        right.getChildren().addAll(
            row("#","IMO Number","1234567"),
            line(),
            row("▣","Shipping Company","Ocean Shipping Ltd."),
            line(),
            row("⌖","Current Port","Mumbai"),
            line(),
            row("◴","Arrival","10:20 AM"),
            line(),
            statusRow()
        );

        details.add(left,0,0);
        details.add(right,1,0);

        card.getChildren().addAll(imageBox,details);

        // ================= PERFORMANCE =================

        VBox ratioBox = new VBox(18);

        ratioBox.setPrefWidth(300);
        ratioBox.setPadding(new Insets(25));
        ratioBox.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:12;" +
            "-fx-border-color:#e1e6ed;" +
            "-fx-border-radius:12;"
        );

        Label ratioTitle = new Label(
            "Ship Performance Ratio"
        );
        ratioTitle.setFont(Font.font("Arial",FontWeight.BOLD,19));

        ProgressBar performance = new ProgressBar(.85);
        performance.setMaxWidth(Double.MAX_VALUE);

        ProgressBar fuel = new ProgressBar(.72);
        fuel.setMaxWidth(Double.MAX_VALUE);

        ratioBox.getChildren().addAll(
            ratioTitle,
            new Label("Overall Performance     85%"),
            performance,
            new Label("Fuel Efficiency              72%"),
            fuel,
            new HBox(
                10,
                ratioCard("85%","Performance"),
                ratioCard("72%","Efficiency"),
                ratioCard("95%","Safety")
            )
        );

        HBox content = new HBox(20,card,ratioBox);

        // ================= VOYAGE =================

        VBox voyage = new VBox(18);

        voyage.setPadding(new Insets(22,25,25,25));
        voyage.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:12;" +
            "-fx-border-color:#e1e6ed;" +
            "-fx-border-radius:12;"
        );

        Label voyageTitle = new Label("Voyage Progress");
        voyageTitle.setFont(Font.font("Arial",FontWeight.BOLD,19));

        ProgressBar voyageProgress = new ProgressBar(.65);
        voyageProgress.setMaxWidth(Double.MAX_VALUE);

        HBox stages = new HBox(
            80,
            voyageStage("●","Departure","Chennai"),
            voyageStage("●","Current Port","Mumbai"),
            voyageStage("○","Destination","Singapore")
        );

        stages.setAlignment(Pos.CENTER);

        voyage.getChildren().addAll(
            voyageTitle,
            new Label("Current journey status of Oceanic Star"),
            voyageProgress,
            stages
        );

        main.getChildren().addAll(
            heading,
            content,
            voyage
        );

        return new Scene(main,1060,760);
    }

    // ================= ROW =================

    VBox row(String icon,String label,String value) {

        Label i = new Label(icon);
        i.setTextFill(Color.web("#6fa7dc"));

        Label l = new Label(label);
        l.setTextFill(Color.web("#8993a3"));

        HBox left = new HBox(10,i,l);

        Label v = new Label(value);
        v.setFont(Font.font("Arial",FontWeight.BOLD,13));

        Region space = new Region();
        HBox.setHgrow(space,Priority.ALWAYS);

        HBox box = new HBox(left,space,v);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefHeight(55);

        return new VBox(box);
    }

    // ================= LINE =================

    Region line() {

        Region line = new Region();
        line.setPrefHeight(1);
        line.setStyle("-fx-background-color:#edf0f4;");

        return line;
    }

    // ================= STATUS =================

    VBox statusRow() {

        Label label = new Label("〽 Current Status");
        label.setTextFill(Color.web("#8993a3"));

        Label value = new Label("●  At Berth");
        value.setStyle(
            "-fx-background-color:#e5f8ed;" +
            "-fx-text-fill:#168548;" +
            "-fx-padding:7 14;" +
            "-fx-background-radius:18;" +
            "-fx-font-weight:bold;"
        );

        Region space = new Region();
        HBox.setHgrow(space,Priority.ALWAYS);

        HBox box = new HBox(label,space,value);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefHeight(55);

        return new VBox(box);
    }

    // ================= RATIO CARD =================

    VBox ratioCard(String percentage,String text) {

        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPrefSize(78,75);

        box.setStyle(
            "-fx-background-color:#f7faff;" +
            "-fx-background-radius:10;" +
            "-fx-border-color:#e5eaf0;" +
            "-fx-border-radius:10;"
        );

        Label percent = new Label(percentage);
        percent.setFont(
            Font.font("Arial",FontWeight.BOLD,18)
        );
        percent.setTextFill(Color.web("#1769e8"));

        Label name = new Label(text);
        name.setTextFill(Color.web("#8993a3"));

        box.getChildren().addAll(percent,name);

        return box;
    }

    // ================= VOYAGE STAGE =================

    VBox voyageStage(String icon,String title,String place) {

        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);

        Label i = new Label(icon);
        i.setTextFill(Color.web("#287be0"));

        Label t = new Label(title);
        t.setFont(Font.font("Arial",FontWeight.BOLD,13));

        Label p = new Label(place);
        p.setTextFill(Color.web("#8993a3"));

        box.getChildren().addAll(i,t,p);

        return box;
    }
}