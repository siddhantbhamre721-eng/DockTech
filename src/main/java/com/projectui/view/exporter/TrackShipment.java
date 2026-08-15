package com.projectui.view.exporter;





import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TrackShipment {

    private Scene scene;

    public Scene getTrackShipmentScene(
            Runnable callbackAction
    ) {

        VBox sidebar =
                createSidebar();

        VBox content =
                new VBox(18);

        content.setPadding(
                new Insets(30, 40, 30, 35)
        );

        content.setStyle(
                "-fx-background-color: #F4F8FC;"
        );

        content.getChildren().add(createHeader("Track Shipment", "Dock Tech / Exporter / Track Shipment"));

        // Search bar
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPrefHeight(50);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search anything...");
        searchField.setPrefWidth(350);
        searchField.setStyle(
                "-fx-padding: 10px;" +
                "-fx-font-size: 14px;" +
                "-fx-control-inner-background: white;" +
                "-fx-border-color: #E2E7ED;" +
                "-fx-border-radius: 8px;" +
                "-fx-padding: 10 15 10 15;"
        );

        searchBox.getChildren().add(searchField);

        Text title =
                new Text("Track Shipment");

        title.setStyle(
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;"
        );

        Text subtitle =
                new Text(
                        "Track the current status of your exported cargo."
                );

        subtitle.setStyle(
                "-fx-fill: #778394;"
        );

        HBox main =
                new HBox(20);

        // LEFT

        VBox left =
                new VBox(18);

        left.setPrefWidth(370);

        // Search

        VBox search =
                new VBox(12);

        search.setPadding(
                new Insets(22)
        );

        search.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18px;" +
                "-fx-border-color: #E0E5EB;" +
                "-fx-border-radius: 18px;"
        );

        Text searchTitle =
                new Text("Shipment Search");

        searchTitle.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;"
        );

        TextField id =
                new TextField("SHP-1024");

        Button track =
                new Button("Track");

        track.setPrefWidth(80);
        track.setPrefHeight(40);

        track.setStyle(
                "-fx-background-color: #287FF0;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 9px;"
        );

        HBox searchRow =
                new HBox(
                        10,
                        id,
                        track
                );

        search.getChildren().addAll(
                searchTitle,
                new Label(
                        "Enter a shipment ID to view live export status."
                ),
                searchRow
        );

        // Details

        VBox details =
                new VBox(15);

        details.setPadding(
                new Insets(22)
        );

        details.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18px;" +
                "-fx-border-color: #E0E5EB;" +
                "-fx-border-radius: 18px;"
        );

        Text detailsTitle =
                new Text("Shipment Details");

        detailsTitle.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;"
        );

        HBox row1 =
                new HBox(
                        50,
                        info("SHIPMENT", "SHP-1024"),
                        info("CARGO", "Electronics")
                );

        HBox row2 =
                new HBox(
                        50,
                        info("CONTAINER", "CONT-2045"),
                        info("ORIGIN", "Mumbai")
                );

        HBox row3 =
                new HBox(
                        50,
                        info("DESTINATION", "Singapore"),
                        info("ETA", "14 Aug 2026")
                );

        details.getChildren().addAll(
                detailsTitle,
                new Label(
                        "Current export shipment summary."
                ),
                row1,
                row2,
                row3
        );

        // Progress

        VBox progress =
                new VBox(15);

        progress.setPadding(
                new Insets(22)
        );

        progress.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18px;" +
                "-fx-border-color: #E0E5EB;" +
                "-fx-border-radius: 18px;"
        );

        Text progressTitle =
                new Text("Shipment progress");

        progressTitle.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;"
        );

        progress.getChildren().addAll(
                progressTitle,
                new Label(
                        "Simple timeline for the current shipment stage."
                ),
                stage("●", "Cargo Registered", true),
                stage("●", "Container Booked", true),
                stage("●", "Cargo Loaded", true),
                stage("●", "In Transit", true),
                stage("○", "Arrived", false),
                stage("○", "Delivered", false)
        );

        left.getChildren().addAll(
                search,
                details,
                progress
        );

        // RIGHT MAP AREA

        VBox right =
                new VBox();

        right.setPrefWidth(320);
        right.setStyle(
                "-fx-background-color: #DDEAFF;" +
                "-fx-background-radius: 22px;"
        );

        Region mapSpace =
                new Region();

        VBox.setVgrow(
                mapSpace,
                javafx.scene.layout.Priority.ALWAYS
        );

        Label location =
                new Label(
                        "●  Shipment location"
                );

        location.setPadding(
                new Insets(20)
        );

        location.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 15px;"
        );

        right.getChildren().addAll(
                mapSpace,
                location
        );

        main.getChildren().addAll(
                left,
                right
        );

        Label transit =
                new Label(
                        "✓  In Transit"
                );

        transit.setMaxWidth(
                Double.MAX_VALUE
        );

        transit.setPadding(
                new Insets(12)
        );

        transit.setStyle(
                "-fx-background-color: #E5FAEF;" +
                "-fx-text-fill: #23855D;" +
                "-fx-background-radius: 10px;"
        );

        Label delayed =
                new Label(
                        "⚠  Shipment delayed"
                );

        delayed.setMaxWidth(
                Double.MAX_VALUE
        );

        delayed.setPadding(
                new Insets(12)
        );

        delayed.setStyle(
                "-fx-background-color: #FFF0E2;" +
                "-fx-text-fill: #C55B20;" +
                "-fx-background-radius: 10px;"
        );

        content.getChildren().addAll(
                searchBox,
                title,
                subtitle,
                main,
                transit,
                delayed
        );

        BorderPane root =
                new BorderPane();

        root.setLeft(sidebar);
        root.setCenter(content);

        scene =
                new Scene(root);

        return scene;
    }

    private VBox info(
            String title,
            String value
    ) {

        VBox box =
                new VBox(5);

        Text t =
                new Text(title);

        t.setStyle(
                "-fx-fill: #8A97A8;" +
                "-fx-font-size: 10px;"
        );

        Text v =
                new Text(value);

        v.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;"
        );

        box.getChildren().addAll(
                t,
                v
        );

        return box;
    }

    private HBox stage(
            String icon,
            String text,
            boolean active
    ) {

        HBox row =
                new HBox(15);

        Text dot =
                new Text(icon);

        dot.setStyle(
                active
                        ?
                        "-fx-fill: #19B879;" +
                        "-fx-font-size: 18px;"
                        :
                        "-fx-fill: #C9D0D8;" +
                        "-fx-font-size: 18px;"
        );

        Text value =
                new Text(text);

        value.setStyle(
                "-fx-font-size: 14px;" +
                (active
                        ?
                        "-fx-font-weight: bold;"
                        :
                        "-fx-fill: #8A97A8;")
        );

        row.getChildren().addAll(
                dot,
                value
        );

        return row;
    }

    private VBox createSidebar() {

        VBox sidebar =
                new VBox(14);

        sidebar.setPadding(
                new Insets(30, 20, 20, 20)
        );

        sidebar.setPrefWidth(285);
        sidebar.setPrefHeight(800);

        sidebar.setStyle(
                "-fx-background-color: #050A10;"
        );

        Text logo =
                new Text("⚓  DOCK TECH");

        logo.setStyle(
                "-fx-fill: white;" +
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;"
        );

        Text sub =
                new Text(
                        "S M A R T   P O R T\nO P E R A T I O N S"
                );

        sub.setStyle(
                "-fx-fill: #8FA0B2;" +
                "-fx-font-size: 11px;"
        );

        Button dashboard =
                menu("🏠   Dashboard", false);

        Button register =
                menu("📦   Register Cargo", false);

        Button book =
                menu("🚢   Book Container", false);

        Button upload =
                menu("📄   Upload Document", false);

        Button payment =
                menu("💳   Make Payment", false);

        Button track =
                menu("📥   Track Shipment", true);

        Button logout =
                menu("⇥   Logout", false);

        dashboard.setOnAction(e -> back());

        register.setOnAction(e -> {

            RegisterCargo p =
                    new RegisterCargo();

            Dashboard.dashboardStage.setScene(
                    p.getRegisterCargoScene(
                            this::back
                    )
            );
            Dashboard.dashboardStage.setFullScreen(true);
        });

        book.setOnAction(e -> {

            BookContainer p =
                    new BookContainer();

            Dashboard.dashboardStage.setScene(
                    p.getBookContainerScene(
                            this::back
                    )
            );
            Dashboard.dashboardStage.setFullScreen(true);
        });

        upload.setOnAction(e -> {

            UploadDocument p =
                    new UploadDocument();

            Dashboard.dashboardStage.setScene(
                    p.getUploadDocumentScene(
                            this::back
                    )
            );
            Dashboard.dashboardStage.setFullScreen(true);
        });

        payment.setOnAction(e -> {

            MakePayment p =
                    new MakePayment();

            Dashboard.dashboardStage.setScene(
                    p.getMakePaymentScene(
                            this::back
                    )
            );
            Dashboard.dashboardStage.setFullScreen(true);
        });

        logout.setOnAction(e ->
                new Logout().showLogout()
        );

        Region space =
                new Region();

        VBox.setVgrow(
                space,
                javafx.scene.layout.Priority.ALWAYS
        );

        sidebar.getChildren().addAll(
                logo,
                sub,
                dashboard,
                register,
                book,
                upload,
                payment,
                track,
                space,
                logout
        );

        return sidebar;
    }

    private Button menu(
            String text,
            boolean active
    ) {

        Button button =
                new Button(text);

        button.setPrefWidth(245);
        button.setPrefHeight(45);
        button.setMinWidth(245);
        button.setMinHeight(45);

        button.setStyle(
                active
                        ?
                        "-fx-background-color: #287FF0;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 9px;" +
                        "-fx-font-size: 15px;" +
                        "-fx-alignment: CENTER_LEFT;" +
                        "-fx-padding: 0 15 0 15;" +
                        "-fx-cursor: hand;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;"
                        :
                        "-fx-background-color: transparent;" +
                        "-fx-text-fill: #C1CBD6;" +
                        "-fx-font-size: 15px;" +
                        "-fx-alignment: CENTER_LEFT;" +
                        "-fx-padding: 0 15 0 15;" +
                        "-fx-cursor: hand;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;"
        );

        return button;
    }

    private void back() {

        new Dashboard().start(
                Dashboard.dashboardStage
        );
    }

    private HBox createHeader(String pageTitle, String breadcrumb) {

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 22, 12, 25));
        header.setPrefHeight(65);
        header.setStyle("-fx-background-color:white; -fx-border-color:#e5ebf3; -fx-border-width:0 0 1 0;");

        VBox pageInfo = new VBox(3);

        Label title = new Label(pageTitle);
        title.setStyle("-fx-text-fill:#111827; -fx-font-size:17; -fx-font-weight:bold;");

        Label bc = new Label(breadcrumb);
        bc.setStyle("-fx-text-fill:#8a93a1; -fx-font-size:10;");

        pageInfo.getChildren().addAll(title, bc);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label status = new Label("● System Operational");
        status.setStyle("-fx-text-fill:#168548; -fx-background-color:#e7f7ee; -fx-background-radius:14; -fx-padding:6 10 6 10; -fx-font-size:10; -fx-font-weight:bold;");

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-text-fill:#4b5563; -fx-font-size:11; -fx-font-weight:bold;");

        Label notification = new Label("🔔");
        notification.setStyle("-fx-text-fill:#111827; -fx-font-size:17;");

        Label avatar = new Label("A");
        avatar.setMinSize(32, 32);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle("-fx-background-color:#dbeafe; -fx-background-radius:50; -fx-text-fill:#0f5fff; -fx-font-weight:bold;");

        VBox profile = new VBox(1);
        Label user = new Label("Dock Tech Admin");
        user.setStyle("-fx-text-fill:#111827; -fx-font-size:11; -fx-font-weight:bold;");
        Label role = new Label("Administrator");
        role.setStyle("-fx-text-fill:#8a93a1; -fx-font-size:9;");
        profile.getChildren().addAll(user, role);

        header.getChildren().addAll(pageInfo, space, status, dateLabel, notification, avatar, profile);
        return header;
    }
}

