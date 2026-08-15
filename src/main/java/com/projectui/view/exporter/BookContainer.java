package com.projectui.view.exporter;





import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

public class BookContainer {

    private Scene scene;

    public Scene getBookContainerScene(
            Runnable callbackAction
    ) {

        VBox sidebar =
                createSidebar();

        VBox content =
                new VBox(20);

        content.setPadding(
                new Insets(35, 60, 30, 60)
        );

        content.setStyle(
                "-fx-background-color: #F4F8FC;"
        );

        content.getChildren().add(createHeader("Book Container", "Dock Tech / Exporter / Book Container"));

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
                new Text("Book Container");

        title.setStyle(
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;"
        );

        Text subtitle =
                new Text(
                        "Book a container for your registered cargo."
                );

        subtitle.setStyle(
                "-fx-fill: #778394;"
        );

        HBox cards =
                new HBox(30);

        // =========================
        // REGISTERED CARGO
        // =========================

        VBox cargo =
                new VBox(18);

        cargo.setPadding(
                new Insets(25)
        );

        cargo.setPrefWidth(360);
        cargo.setPrefHeight(380);

        cargo.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18px;" +
                "-fx-border-color: #E0E5EB;" +
                "-fx-border-radius: 18px;"
        );

        Text cargoTitle =
                new Text("Registered Cargo");

        cargoTitle.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
        );

        Text cargoSub =
                new Text(
                        "Selected cargo for container booking"
                );

        cargoSub.setStyle(
                "-fx-fill: #778394;"
        );

        cargo.getChildren().addAll(
                cargoTitle,
                cargoSub,
                information(
                        "C A R G O  I D",
                        "CGO-1024"
                ),
                information(
                        "C A R G O",
                        "Electronics"
                ),
                information(
                        "W E I G H T",
                        "1200 kg"
                )
        );

        // =========================
        // BOOKING
        // =========================

        VBox booking =
                new VBox(18);

        booking.setPadding(
                new Insets(25)
        );

        booking.setPrefWidth(430);
        booking.setPrefHeight(450);

        booking.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18px;" +
                "-fx-border-color: #E0E5EB;" +
                "-fx-border-radius: 18px;"
        );

        Text bookingTitle =
                new Text("Container Booking");

        bookingTitle.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
        );

        Text bookingSub =
                new Text(
                        "Only the required booking details"
                );

        bookingSub.setStyle(
                "-fx-fill: #778394;"
        );

        ComboBox<String> type =
                new ComboBox<>();

        type.getItems().addAll(
                "20 ft",
                "40 ft"
        );

        type.setValue("20 ft");

        type.setPrefHeight(42);

        TextField quantity =
                new TextField("1");

        quantity.setPrefHeight(42);

        ComboBox<String> port =
                new ComboBox<>();

        port.getItems().addAll(
                "Port of Mumbai",
                "Port of Chennai",
                "Port of Kolkata"
        );

        port.setValue(
                "Port of Mumbai"
        );

        port.setPrefHeight(42);

        Button book =
                new Button("Book Container");

        book.setPrefHeight(48);

        book.setMaxWidth(
                Double.MAX_VALUE
        );

        book.setStyle(
                "-fx-background-color: #287FF0;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 9px;"
        );

        Label success =
                new Label(
                        "✓ Container booked\n" +
                        "Container ID: CONT-2045"
                );

        success.setVisible(false);

        success.setPadding(
                new Insets(15)
        );

        success.setMaxWidth(
                Double.MAX_VALUE
        );

        success.setStyle(
                "-fx-background-color: #E7FAF1;" +
                "-fx-text-fill: #23855D;" +
                "-fx-background-radius: 10px;"
        );

        book.setOnAction(e ->
                success.setVisible(true)
        );

        booking.getChildren().addAll(
                bookingTitle,
                bookingSub,
                label("Container Type"),
                type,
                label("Quantity"),
                quantity,
                label("Preferred Port"),
                port,
                book,
                success
        );

        cards.getChildren().addAll(
                cargo,
                booking
        );

        content.getChildren().addAll(
                searchBox,
                title,
                subtitle,
                cards
        );

        BorderPane root =
                new BorderPane();

        root.setLeft(sidebar);
        root.setCenter(content);

        scene =
                new Scene(root);

        return scene;
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
                menu("🚢   Book Container", true);

        Button upload =
                menu("📄   Upload Document", false);

        Button payment =
                menu("💳   Make Payment", false);

        Button track =
                menu("📥   Track Shipment", false);

        Button logout =
                menu("⇥   Logout", false);

        dashboard.setOnAction(e -> back());
        register.setOnAction(e -> {

            RegisterCargo page =
                    new RegisterCargo();

            Dashboard.dashboardStage.setScene(
                    page.getRegisterCargoScene(
                            this::back
                    )
            );
            Dashboard.dashboardStage.setFullScreen(true);
        });

        upload.setOnAction(e -> {

            UploadDocument page =
                    new UploadDocument();

            Dashboard.dashboardStage.setScene(
                    page.getUploadDocumentScene(
                            this::back
                    )
            );
            Dashboard.dashboardStage.setFullScreen(true);
        });

        payment.setOnAction(e -> {

            MakePayment page =
                    new MakePayment();

            Dashboard.dashboardStage.setScene(
                    page.getMakePaymentScene(
                            this::back
                    )
            );
            Dashboard.dashboardStage.setFullScreen(true);
        });

        track.setOnAction(e -> {

            TrackShipment page =
                    new TrackShipment();

            Dashboard.dashboardStage.setScene(
                    page.getTrackShipmentScene(
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

    private VBox information(
            String title,
            String value
    ) {

        VBox box =
                new VBox(5);

        box.setPadding(
                new Insets(15)
        );

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

    private Label label(String text) {

        Label label =
                new Label(text);

        label.setStyle(
                "-fx-font-weight: bold;"
        );

        return label;
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

