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
import javafx.scene.text.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MakePayment {

    private Scene scene;
    


    public Scene getMakePaymentScene(
            Runnable callbackAction
    ) {

        VBox sidebar =
                createSidebar();

        VBox content =
                new VBox(20);

        content.setPadding(
                new Insets(35, 55, 30, 55)
        );

        content.setStyle(
                "-fx-background-color: #F0F8FC;"
        );

        content.getChildren().add(createHeader("Make Payment", "Dock Tech / Exporter / Make Payment"));

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
                new Text("Make Payment");

        title.setStyle(
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;"
        );

        Text subtitle =
                new Text(
                        "Complete payment for your cargo shipment."
                );

        subtitle.setStyle(
                "-fx-fill: #778394;"
        );

        HBox main =
                new HBox(25);

        // =========================
        // LEFT
        // =========================

        VBox summary =
                new VBox(18);

        summary.setPadding(
                new Insets(30)
        );

        summary.setPrefWidth(500);
        summary.setPrefHeight(600);

        summary.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18px;" +
                "-fx-border-color: #E0E5EB;" +
                "-fx-border-radius: 18px;"
        );

        Text summaryTitle =
                new Text("Shipment Summary");

        summaryTitle.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;"
        );

        Text summarySub =
                new Text(
                        "Review the selected order before making payment."
                );

        summarySub.setStyle(
                "-fx-fill: #778394;"
        );

        HBox info =
                new HBox(15);

        info.getChildren().addAll(
                box("SHIPMENT", "SHP-1024"),
                box("CARGO", "Electronics")
        );

        HBox info2 =
                new HBox(15);

        info2.getChildren().addAll(
                box("CONTAINER", "CONT-2045"),
                box("AMOUNT DUE", "₹1,25,000")
        );

        VBox paymentBox =
                new VBox(15);

        paymentBox.setPadding(
                new Insets(20)
        );

        paymentBox.setStyle(
                "-fx-background-color: #EAF6FF;" +
                "-fx-background-radius: 15px;"
        );

        Text method =
                new Text("▭  Payment Method");

        method.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;"
        );

        Text methodSub =
                new Text(
                        "Choose a secure payment option"
                );

        methodSub.setStyle(
                "-fx-fill: #778394;"
        );

        TextField paymentMethod =
                new TextField("UPI");

        paymentMethod.setPrefHeight(45);

        Button pay =
                new Button("Pay Now");

        pay.setMaxWidth(
                Double.MAX_VALUE
        );

        pay.setPrefHeight(45);

        pay.setStyle(
                "-fx-background-color: #287FF0;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 9px;"
        );

        Label success =
                new Label(
                        "✓  Payment Successful\n" +
                        "Transaction ID: TXN-10245"
                );

        success.setVisible(false);

        success.setPadding(
                new Insets(15)
        );

        success.setStyle(
                "-fx-background-color: #E7FAF1;" +
                "-fx-text-fill: #23855D;" +
                "-fx-background-radius: 10px;"
        );

        pay.setOnAction(e ->
                success.setVisible(true)
        );

        paymentBox.getChildren().addAll(
                method,
                methodSub,
                paymentMethod,
                pay,
                success
        );

        summary.getChildren().addAll(
                summaryTitle,
                summarySub,
                info,
                info2,
                paymentBox
        );

        // =========================
        // RIGHT
        // =========================

        VBox details =
                new VBox(20);

        details.setPadding(
                new Insets(30)
        );

        details.setPrefWidth(330);
        details.setPrefHeight(600);

        details.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18px;" +
                "-fx-border-color: #E0E5EB;" +
                "-fx-border-radius: 18px;"
        );

        Text detailsTitle =
                new Text("Payment Details");

        detailsTitle.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;"
        );

        details.getChildren().addAll(
                detailsTitle,
                detail("Shipment", "SHP-1024"),
                detail("Cargo", "Electronics"),
                detail("Container", "CONT-2045"),
                detail("Amount Due", "₹1,25,000")
        );

        Label status =
                new Label("● Paid");

        status.setStyle(
                "-fx-background-color: #E7FAF1;" +
                "-fx-text-fill: #23855D;" +
                "-fx-padding: 8px 15px;" +
                "-fx-background-radius: 15px;"
        );

        details.getChildren().add(
                status
        );

        main.getChildren().addAll(
                summary,
                details
        );

        content.getChildren().addAll(
                searchBox,
                title,
                subtitle,
                main
        );

        BorderPane root =
                new BorderPane();

        root.setLeft(sidebar);
        root.setCenter(content);

        scene =
                new Scene(root);

        return scene;
    }

    private VBox box(
            String title,
            String value
    ) {

        VBox box =
                new VBox(6);

        box.setPadding(
                new Insets(15)
        );

        box.setPrefWidth(210);

        box.setStyle(
                "-fx-background-color: #F8FAFC;" +
                "-fx-background-radius: 12px;"
        );

        Text t =
                new Text(title);

        t.setStyle(
                "-fx-fill: #8290A1;" +
                "-fx-font-size: 11px;"
        );

        Text v =
                new Text(value);

        v.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;"
        );

        box.getChildren().addAll(
                t,
                v
        );

        return box;
    }

    private VBox detail(
            String title,
            String value
    ) {

        VBox box =
                new VBox(5);

        Text t =
                new Text(title);

        t.setStyle(
                "-fx-fill: #778394;"
        );

        Text v =
                new Text(value);

        v.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-font-size: 15px;"
        );

        box.getChildren().addAll(
                t,
                v
        );

        return box;
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
                menu("💳   Make Payment", true);

        Button track =
                menu("📥   Track Shipment", false);

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

        track.setOnAction(e -> {

            TrackShipment p =
                    new TrackShipment();

            Dashboard.dashboardStage.setScene(
                    p.getTrackShipmentScene(
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

