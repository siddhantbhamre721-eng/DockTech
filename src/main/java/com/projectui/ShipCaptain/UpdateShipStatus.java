package com.projectui.ShipCaptain;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class UpdateShipStatus {
    public Scene getShipScene() {

        VBox main = new VBox(25);

        main.setPadding(
            new Insets(20,35,30,35)
        );

        main.setStyle(
            "-fx-background-color:#f7faff;"
        );

        // ================= HEADER =================

        VBox heading = new VBox(5);

        Label title = new Label(
            "Update Ship Status"
        );

        title.setFont(
            Font.font("Arial",FontWeight.BOLD,26)
        );

        Label desc = new Label(
            "Update the current operational status of your vessel."
        );

        desc.setTextFill(Color.GRAY);

        heading.getChildren().addAll(
            title,
            desc
        );

        // ================= CONTENT =================

        HBox contentRow = new HBox(20);

        // ================= MAIN CARD =================

        VBox card = new VBox(20);

        card.setPrefWidth(760);
        card.setPadding(new Insets(35));

        card.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:12;" +
            "-fx-border-color:#e1e5ea;" +
            "-fx-border-radius:12;"
        );

        // ================= SHIP =================

        HBox shipHeader = new HBox(15);
        shipHeader.setAlignment(Pos.CENTER_LEFT);

        Label shipIcon = new Label("⚓");
        shipIcon.setFont(Font.font(25));
        shipIcon.setTextFill(Color.web("#1677ff"));

        StackPane iconBox = new StackPane(shipIcon);
        iconBox.setPrefSize(52,52);
        iconBox.setStyle(
            "-fx-background-color:#edf7ff;" +
            "-fx-background-radius:10;"
        );

        VBox shipText = new VBox(2);

        Label current = new Label("CURRENT SHIP");
        current.setTextFill(Color.web("#8b929d"));

        Label shipName = new Label("Oceanic Star");
        shipName.setFont(
            Font.font("Arial",FontWeight.BOLD,20)
        );

        shipText.getChildren().addAll(
            current,
            shipName
        );

        shipHeader.getChildren().addAll(
            iconBox,
            shipText
        );

        // ================= CURRENT STATUS =================

        Label currentLabel = new Label(
            "Current Status"
        );

        currentLabel.setFont(
            Font.font("Arial",FontWeight.BOLD,14)
        );

        Label currentStatus = new Label(
            "●  At Berth"
        );

        currentStatus.setStyle(
            "-fx-background-color:#e4f8ed;" +
            "-fx-text-fill:#168548;" +
            "-fx-padding:8 18;" +
            "-fx-background-radius:20;" +
            "-fx-font-weight:bold;"
        );

        Region space = new Region();
        HBox.setHgrow(space,Priority.ALWAYS);

        HBox currentBox = new HBox(
            currentLabel,
            space,
            currentStatus
        );

        currentBox.setAlignment(Pos.CENTER_LEFT);
        currentBox.setPadding(new Insets(15));

        // ================= NEW STATUS =================

        Label newStatus = new Label("New Status");
        newStatus.setFont(
            Font.font("Arial",FontWeight.BOLD,14)
        );

        ComboBox<String> combo = new ComboBox<>();

        combo.getItems().addAll(
            "At Berth",
            "Loading",
            "Loaded",
            "Ready to Sail",
            "Departed",
            "In Transit",
            "Arrived"
        );

        combo.setPromptText("Select Status");
        combo.setPrefHeight(46);
        combo.setMaxWidth(Double.MAX_VALUE);

        // ================= REMARKS =================

        Label remarksLabel = new Label(
            "Remarks (optional)"
        );

        remarksLabel.setFont(
            Font.font("Arial",FontWeight.BOLD,14)
        );

        TextField remarks = new TextField();
        remarks.setPromptText(
            "Enter short remarks..."
        );
        remarks.setPrefHeight(46);

        // ================= BUTTON =================

        Button update = new Button(
            "⟳  Update Status"
        );

        update.setPrefHeight(48);
        update.setMaxWidth(Double.MAX_VALUE);

        update.setStyle(
            "-fx-background-color:#1677ff;" +
            "-fx-text-fill:white;" +
            "-fx-font-weight:bold;" +
            "-fx-background-radius:7;"
        );

        update.setOnAction(e -> {

            if (combo.getValue() == null) {

                Alert alert = new Alert(
                    Alert.AlertType.WARNING
                );

                alert.setHeaderText(
                    "Status Required"
                );

                alert.setContentText(
                    "Please select a new ship status."
                );

                alert.showAndWait();

            } else {

                Alert alert = new Alert(
                    Alert.AlertType.INFORMATION
                );

                alert.setHeaderText(
                    "Status Updated"
                );

                alert.setContentText(
                    "Ship status updated to: "
                    + combo.getValue()
                );

                alert.showAndWait();
            }
        });

        card.getChildren().addAll(
            shipHeader,
            currentBox,
            newStatus,
            combo,
            remarksLabel,
            remarks,
            update
        );

        // ================= HISTORY =================

        VBox history = new VBox(25);

        history.setPrefWidth(350);
        history.setPadding(new Insets(25));

        history.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:12;" +
            "-fx-border-color:#e1e6ed;" +
            "-fx-border-radius:12;"
        );

        Label historyTitle = new Label(
            "Status History"
        );

        historyTitle.setFont(
            Font.font("Arial",FontWeight.BOLD,19)
        );

        history.getChildren().addAll(
            historyTitle,
            historyRow("●","At Berth","Today, 10:20 AM","#36c878"),
            historyLine(),
            historyRow("●","Arrived","Today, 09:45 AM","#287be0"),
            historyLine(),
            historyRow("●","In Transit","Yesterday, 08:30 PM","#287be0"),
            historyLine(),
            historyRow("●","Departed","Yesterday, 06:00 PM","#8993a3")
        );

        contentRow.getChildren().addAll(
            card,
            history
        );

        main.getChildren().addAll(
            heading,
            contentRow
        );

        return new Scene(main,1060,760);
    }

    // ================= HISTORY ROW =================

    HBox historyRow(
        String icon,
        String status,
        String time,
        String color
    ) {

        Label dot = new Label(icon);
        dot.setTextFill(Color.web(color));

        Label s = new Label(status);
        s.setFont(
            Font.font("Arial",FontWeight.BOLD,13)
        );

        Label t = new Label(time);
        t.setTextFill(Color.web("#8993a3"));

        VBox text = new VBox(3,s,t);

        HBox row = new HBox(10,dot,text);
        row.setAlignment(Pos.CENTER_LEFT);

        return row;
    }

    // ================= LINE =================

    Region historyLine() {

        Region line = new Region();

        line.setPrefHeight(1);

        line.setStyle(
            "-fx-background-color:#edf0f4;"
        );

        return line;
    }
}
    

