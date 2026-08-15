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
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UploadDocument {

    private Scene scene;

    public Scene getUploadDocumentScene(
            Runnable callbackAction
    ) {

        VBox sidebar =
                createSidebar();

        VBox content =
                new VBox(20);

        content.setPadding(
                new Insets(35, 50, 30, 50)
        );

        content.setStyle(
                "-fx-background-color: #F4F8FC;"
        );

        content.getChildren().add(createHeader("Upload Document", "Dock Tech / Exporter / Upload Document"));

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
                new Text("Upload Document");

        title.setStyle(
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;"
        );

        Text subtitle =
                new Text(
                        "Upload required documents for your cargo shipment."
                );

        subtitle.setStyle(
                "-fx-fill: #778394;"
        );

        HBox main =
                new HBox(25);

        // =========================
        // UPLOAD CARD
        // =========================

        VBox uploadCard =
                new VBox(15);

        uploadCard.setPadding(
                new Insets(25)
        );

        uploadCard.setPrefWidth(500);
        uploadCard.setPrefHeight(620);

        uploadCard.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18px;" +
                "-fx-border-color: #E0E5EB;" +
                "-fx-border-radius: 18px;"
        );

        Text uploadTitle =
                new Text("Document Upload");

        uploadTitle.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
        );

        Text uploadSub =
                new Text(
                        "Keep cargo paperwork organized and shipment-ready."
                );

        uploadSub.setStyle(
                "-fx-fill: #778394;"
        );

        ComboBox<String> cargo =
                new ComboBox<>();

        cargo.getItems().addAll(
                "CGO-1024",
                "CGO-1025",
                "CGO-1026"
        );

        cargo.setValue("CGO-1024");

        cargo.setMaxWidth(
                Double.MAX_VALUE
        );

        cargo.setPrefHeight(42);

        ComboBox<String> document =
                new ComboBox<>();

        document.getItems().addAll(
                "Commercial Invoice",
                "Packing List",
                "Shipping Document"
        );

        document.setValue(
                "Commercial Invoice"
        );

        document.setMaxWidth(
                Double.MAX_VALUE
        );

        document.setPrefHeight(42);

        VBox fileArea =
                new VBox(10);

        fileArea.setAlignment(
                Pos.CENTER
        );

        fileArea.setPrefHeight(220);

        fileArea.setStyle(
                "-fx-background-color: #F8FAFC;" +
                "-fx-border-color: #D5DDE6;" +
                "-fx-border-style: dashed;" +
                "-fx-border-radius: 15px;" +
                "-fx-background-radius: 15px;"
        );

        Text fileIcon =
                new Text("▤");

        fileIcon.setStyle(
                "-fx-font-size: 35px;" +
                "-fx-fill: #287FF0;"
        );

        Text uploadFile =
                new Text("Upload File");

        uploadFile.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;"
        );

        Text choose =
                new Text(
                        "Choose document file"
                );

        choose.setStyle(
                "-fx-fill: #778394;"
        );

        Label supported =
                new Label(
                        "PDF, JPG, PNG supported"
                );

        supported.setStyle(
                "-fx-background-color: white;" +
                "-fx-padding: 8px 12px;" +
                "-fx-background-radius: 15px;" +
                "-fx-border-color: #E0E5EB;" +
                "-fx-border-radius: 15px;"
        );

        fileArea.getChildren().addAll(
                fileIcon,
                uploadFile,
                choose,
                supported
        );

        Button chooseFile =
                new Button("Choose File");

        chooseFile.setMaxWidth(
                Double.MAX_VALUE
        );

        chooseFile.setOnAction(event -> {

            FileChooser chooser =
                    new FileChooser();

            chooser.setTitle(
                    "Select Document"
            );

            File file =
                    chooser.showOpenDialog(
                            Dashboard.dashboardStage
                    );

            if (file != null) {

                choose.setText(
                        file.getName()
                );
            }
        });

        Button uploadButton =
                new Button("Upload Document");

        uploadButton.setMaxWidth(
                Double.MAX_VALUE
        );

        uploadButton.setPrefHeight(48);

        uploadButton.setStyle(
                "-fx-background-color: #287FF0;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 9px;"
        );

        Label uploaded =
                new Label(
                        "● Commercial Invoice     Uploaded\n" +
                        "● Packing List              Uploaded\n" +
                        "● Shipping Document     Pending"
                );

        uploaded.setPadding(
                new Insets(15)
        );

        uploaded.setStyle(
                "-fx-background-color: #F8FAFC;" +
                "-fx-background-radius: 12px;" +
                "-fx-text-fill: #35705A;"
        );

        uploadButton.setOnAction(
                event ->
                        uploaded.setText(
                                "● Commercial Invoice     Uploaded\n" +
                                "● Packing List              Uploaded\n" +
                                "● Shipping Document     Uploaded"
                        )
        );

        uploadCard.getChildren().addAll(
                uploadTitle,
                uploadSub,
                new Label("CARGO"),
                cargo,
                new Label("DOCUMENT TYPE"),
                document,
                fileArea,
                chooseFile,
                uploadButton,
                uploaded
        );

        // =========================
        // RIGHT CARDS
        // =========================

        VBox right =
                new VBox(20);

        VBox cargoCard =
                simpleCard(
                        "▣  Cargo",
                        "Cargo ID       CGO-1024",
                        "Cargo          Electronics",
                        "Weight         1200 kg"
                );

        VBox statusCard =
                simpleCard(
                        "✓  Upload Status",
                        "Commercial Invoice        Uploaded",
                        "Packing List                    Uploaded",
                        "Shipping Document       Pending"
                );

        right.getChildren().addAll(
                cargoCard,
                statusCard
        );

        main.getChildren().addAll(
                uploadCard,
                right
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

    private VBox simpleCard(
            String title,
            String line1,
            String line2,
            String line3
    ) {

        VBox box =
                new VBox(15);

        box.setPadding(
                new Insets(25)
        );

        box.setPrefWidth(330);

        box.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18px;" +
                "-fx-border-color: #E0E5EB;" +
                "-fx-border-radius: 18px;"
        );

        Text t =
                new Text(title);

        t.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
        );

        box.getChildren().addAll(
                t,
                new Label(line1),
                new Label(line2),
                new Label(line3)
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
                menu("📄   Upload Document", true);

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

        book.setOnAction(e -> {

            BookContainer page =
                    new BookContainer();

            Dashboard.dashboardStage.setScene(
                    page.getBookContainerScene(
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

