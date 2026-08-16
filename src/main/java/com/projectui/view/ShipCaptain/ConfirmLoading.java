package com.projectui.view.ShipCaptain;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ConfirmLoading {

    private RadioButton progress, completed;
    private Button confirm;
    private Label success;

    public Scene getScene() {

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#f3f8fc;");

        // ================= HEADER =================

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 28, 15, 28));
        header.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e5ebf2;"
        );

        VBox heading = new VBox(3,
                label("Confirm Loading", 27, true),
                label("Verify cargo loading and confirm vessel readiness.", 11, false)
        );

        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);

      
       
        header.getChildren().addAll(
                heading, gap
        );

        root.setTop(header);

        // ================= MAIN =================

        HBox main = new HBox(20);
        main.setPadding(new Insets(25, 45, 30, 45));

        // ================= LEFT CARD =================

        VBox card = new VBox(17);
        card.setPadding(new Insets(25, 30, 28, 30));
        card.setPrefWidth(850);
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:16;" +
                "-fx-border-color:#e1e7ee;" +
                "-fx-border-radius:16;" +
                "-fx-effect:dropshadow(gaussian,rgba(31,41,55,0.08),18,0,0,5);"
        );

        Label icon = label("◉", 18, true);
        icon.setPrefSize(42, 42);
        icon.setAlignment(Pos.CENTER);
        icon.setTextFill(Color.web("#287cf5"));
        icon.setStyle(
                "-fx-background-color:#eaf3ff;" +
                "-fx-background-radius:12;"
        );

        VBox title = new VBox(3,
                label("Cargo Loading Details", 19, true),
                label("Review the details and confirm loading status.", 11, false)
        );

        HBox cardTitle = new HBox(12, icon, title);
        cardTitle.setAlignment(Pos.CENTER_LEFT);

        GridPane details = new GridPane();
        details.setHgap(15);
        details.setVgap(15);

        details.add(detail("SHIP", "Oceanic Star", false), 0, 0);
        details.add(detail("BERTH", "B04", false), 1, 0);
        details.add(detail("CARGO", "Electronics", false), 0, 1);
        details.add(detail("CARGO ID", "CGO-1287", false), 1, 1);
        details.add(detail("EXPECTED QUANTITY", "850 Tons", false), 0, 2);
        details.add(detail("LOADED QUANTITY", "850 Tons", true), 1, 2);

        Label statusTitle = label("Loading Status", 14, true);

        ToggleGroup group = new ToggleGroup();

        progress = new RadioButton("Loading in Progress");
        progress.setToggleGroup(group);

        completed = new RadioButton("Loading Completed");
        completed.setToggleGroup(group);
        completed.setSelected(true);

        Label progressInfo = label(
                "Cargo is still being loaded onto the vessel.", 10, false);
        progressInfo.setTextFill(Color.web("#7b8797"));

        Label completedInfo = label(
                "All cargo has been fully loaded and secured.", 10, false);
        completedInfo.setTextFill(Color.web("#7b8797"));

        progress.setOnAction(e -> statusChange());
        completed.setOnAction(e -> statusChange());

        VBox status = new VBox(
                7,
                statusTitle,
                new VBox(2, progress, progressInfo),
                new VBox(2, completed, completedInfo)
        );

        Label remarksTitle = label("Remarks", 13, true);

        TextField remarks = new TextField();
        remarks.setPromptText("Optional remarks...");
        remarks.setPrefHeight(42);
        remarks.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#dfe5ec;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;" +
                "-fx-padding:0 12;"
        );

        confirm = new Button("◉  Confirm Loading");
        confirm.setMaxWidth(Double.MAX_VALUE);
        confirm.setPrefHeight(45);
        confirm.setStyle(buttonStyle("#287cf5"));

        confirm.setOnAction(
                e -> confirmLoading(remarks.getText())
        );

        success = label(
                "✓  Loading confirmed successfully", 11, true);
        success.setAlignment(Pos.CENTER);
        success.setMaxWidth(Double.MAX_VALUE);
        success.setPadding(new Insets(12));
        success.setTextFill(Color.web("#15956d"));
        success.setStyle(
                "-fx-background-color:#eaf8f0;" +
                "-fx-border-color:#ccefe1;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;"
        );
        success.setVisible(false);
        success.setManaged(false);

        card.getChildren().addAll(
                cardTitle,
                details,
                status,
                remarksTitle,
                remarks,
                confirm,
                success
        );

        HBox.setHgrow(card, Priority.ALWAYS);

        // ================= RIGHT PANEL =================

        VBox side = new VBox(14);
        side.setPrefWidth(330);
        side.setPadding(new Insets(20));
        side.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:16;" +
                "-fx-border-color:#e1e7ee;" +
                "-fx-border-radius:16;" +
                "-fx-effect:dropshadow(gaussian,rgba(31,41,55,0.07),16,0,0,4);"
        );

        Label sideIcon = label("✓", 17, true);
        sideIcon.setPrefSize(38, 38);
        sideIcon.setAlignment(Pos.CENTER);
        sideIcon.setTextFill(Color.web("#16a875"));
        sideIcon.setStyle(
                "-fx-background-color:#eaf8f0;" +
                "-fx-background-radius:10;"
        );

        VBox sideTitle = new VBox(2,
                label("Vessel Readiness", 17, true),
                label("Final checks before departure", 10, false)
        );

        HBox sideHeader = new HBox(10, sideIcon, sideTitle);
        sideHeader.setAlignment(Pos.CENTER_LEFT);

        Label ready = label(
                "●   READY FOR DEPARTURE", 10, true);
        ready.setTextFill(Color.web("#15956d"));
        ready.setPadding(new Insets(11));
        ready.setMaxWidth(Double.MAX_VALUE);
        ready.setStyle(
                "-fx-background-color:#f1fbf7;" +
                "-fx-border-color:#ccefe1;" +
                "-fx-border-radius:9;" +
                "-fx-background-radius:9;"
        );

        VBox voyage = box(
                "VOYAGE SUMMARY",
                "Oceanic Star",
                "Cargo: Electronics",
                "Berth: B04"
        );

        Label checkTitle = label(
                "READINESS CHECKLIST", 10, true);
        checkTitle.setTextFill(Color.web("#8a94a3"));

        VBox checks = new VBox(
                8,
                checkTitle,
                check("Cargo quantity verified"),
                check("Cargo secured"),
                check("Loading completed"),
                check("Berth clearance confirmed")
        );

        VBox next = new VBox(5,
                label("NEXT ACTION", 10, true),
                label(
                        "Confirm the loading status to update vessel readiness.",
                        10, false
                )
        );

        next.setPadding(new Insets(12));
        next.setStyle(
                "-fx-background-color:#f7faff;" +
                "-fx-border-color:#dce9fb;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;"
        );

        VBox quick = new VBox(
                7,
                label("QUICK INFO", 10, true),
                info("Cargo ID", "CGO-1287"),
                info("Expected", "850 Tons"),
                info("Loaded", "850 Tons")
        );

        quick.setPadding(new Insets(12));
        quick.setStyle(
                "-fx-background-color:#fafbfc;" +
                "-fx-border-color:#e5eaf0;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;"
        );

        side.getChildren().addAll(
                sideHeader,
                ready,
                voyage,
                checks,
                next,
                quick
        );

        main.getChildren().addAll(card, side);
        root.setCenter(main);

        return new Scene(root, 1450, 850);
    }

    // ================= FUNCTIONS =================

    private void confirmLoading(String remarks) {

        if (!completed.isSelected()) {
            alert(
                    "Loading Not Completed",
                    "Please select Loading Completed first."
            );
            return;
        }

        success.setVisible(true);
        success.setManaged(true);

        confirm.setText("✓  Loading Confirmed");
        confirm.setStyle(buttonStyle("#16a875"));

        alert(
                "Loading Confirmed",
                "Cargo CGO-1287 has been successfully loaded on Oceanic Star."
                        + (remarks.trim().isEmpty()
                        ? ""
                        : "\n\nRemarks: " + remarks)
        );
    }

    private void statusChange() {

        if (progress.isSelected()) {
            confirm.setText("◉  Loading In Progress");
            confirm.setStyle(buttonStyle("#f59e0b"));
            success.setVisible(false);
            success.setManaged(false);
        } else {
            confirm.setText("◉  Confirm Loading");
            confirm.setStyle(buttonStyle("#287cf5"));
        }
    }

    private VBox detail(
            String title,
            String value,
            boolean blue
    ) {

        VBox box = new VBox(5);
        box.setPadding(new Insets(12, 15, 12, 15));
        box.setPrefSize(300, 68);

        box.setStyle(
                "-fx-background-color:" +
                (blue ? "#f0f7ff" : "white") + ";" +
                "-fx-background-radius:10;" +
                "-fx-border-color:#e1e7ee;" +
                "-fx-border-radius:10;"
        );

        Label t = label(title, 9, true);
        t.setTextFill(Color.web("#8a94a3"));

        Label v = label(value, 13, true);

        if (blue)
            v.setTextFill(Color.web("#287cf5"));

        box.getChildren().addAll(t, v);

        return box;
    }

    private VBox box(
            String title,
            String main,
            String line1,
            String line2
    ) {

        VBox box = new VBox(
                5,
                label(title, 9, true),
                label(main, 14, true),
                label(line1, 10, false),
                label(line2, 10, false)
        );

        box.setPadding(new Insets(12));
        box.setStyle(
                "-fx-background-color:#f8fafc;" +
                "-fx-border-color:#e5eaf0;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;"
        );

        return box;
    }

    private HBox check(String text) {

        Label tick = label("✓", 12, true);
        tick.setTextFill(Color.web("#16a875"));

        HBox row = new HBox(
                8,
                tick,
                label(text, 10, false)
        );

        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox info(String key, String value) {

        Label k = label(key, 10, false);
        k.setTextFill(Color.web("#7b8797"));

        Label v = label(value, 10, true);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox row = new HBox(k, space, v);
        row.setAlignment(Pos.CENTER_LEFT);

        return row;
    }

    private Label label(
            String text,
            double size,
            boolean bold
    ) {

        Label l = new Label(text);

        l.setFont(Font.font(
                "Arial",
                bold ? FontWeight.BOLD : FontWeight.NORMAL,
                size
        ));

        l.setTextFill(Color.web("#111827"));

        return l;
    }

    private String buttonStyle(String color) {

        return
                "-fx-background-color:" + color + ";" +
                "-fx-background-radius:8;" +
                "-fx-text-fill:white;" +
                "-fx-font-weight:bold;" +
                "-fx-cursor:hand;";
    }

    private void alert(
            String title,
            String message
    ) {

        Alert a = new Alert(
                Alert.AlertType.INFORMATION
        );

        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}