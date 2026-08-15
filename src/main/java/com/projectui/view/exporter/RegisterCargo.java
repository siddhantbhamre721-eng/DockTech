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

public class RegisterCargo {

    private Scene scene;

    public Scene getRegisterCargoScene(
            Runnable callbackAction
    ) {

        // =========================================================
        // SIDEBAR - NOT CHANGED
        // =========================================================

        VBox sidebar =
                createSidebar();

        // =========================================================
        // MAIN CONTENT
        // =========================================================

        VBox content =
                new VBox(20);

        content.setPadding(
                new Insets(35, 70, 35, 60)
        );

        content.setStyle(
                "-fx-background-color: #F4F8FC;"
        );

        // =========================================================
        // HEADER - NOT CHANGED
        // =========================================================

        content.getChildren().add(
                createHeader(
                        "Register Cargo",
                        "Dock Tech / Exporter / Register Cargo"
                )
        );

        // =========================================================
        // SEARCH BAR
        // =========================================================

        HBox searchBox =
                new HBox(10);

        searchBox.setAlignment(
                Pos.CENTER_LEFT
        );

        searchBox.setPrefHeight(50);

        TextField searchField =
                new TextField();

        searchField.setPromptText(
                "🔍 Search anything..."
        );

        searchField.setPrefWidth(350);

        searchField.setStyle(
                "-fx-padding: 10px;" +
                "-fx-font-size: 14px;" +
                "-fx-control-inner-background: white;" +
                "-fx-border-color: #E2E7ED;" +
                "-fx-border-radius: 8px;" +
                "-fx-padding: 10 15 10 15;"
        );

        searchBox.getChildren().add(
                searchField
        );

        // =========================================================
        // PAGE TITLE
        // =========================================================

        Text title =
                new Text("Register Cargo");

        title.setStyle(
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;"
        );

        Text subtitle =
                new Text(
                        "Register cargo before container booking and shipment."
                );

        subtitle.setStyle(
                "-fx-fill: #778394;" +
                "-fx-font-size: 15px;"
        );

        // =========================================================
        // CARGO REGISTRATION FORM
        // =========================================================

        VBox form =
                new VBox(18);

        form.setPadding(
                new Insets(35)
        );

        form.setMaxWidth(660);

        form.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18px;" +
                "-fx-border-color: #E0E5EB;" +
                "-fx-border-radius: 18px;"
        );

        Text formTitle =
                new Text("📦  Cargo Registration");

        formTitle.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;"
        );

        Text formSub =
                new Text(
                        "Enter the required cargo details to continue the export workflow."
                );

        formSub.setStyle(
                "-fx-fill: #778394;"
        );

        // =========================================================
        // FIELDS
        // =========================================================

        TextField cargoName =
                new TextField("Electronics");

        TextField quantity =
                new TextField("500");

        TextField weight =
                new TextField("1200 kg");

        ComboBox<String> cargoType =
                new ComboBox<>();

        cargoType.getItems().addAll(
                "General Cargo",
                "Electronics",
                "Machinery",
                "Textiles"
        );

        cargoType.setValue(
                "General Cargo"
        );

        ComboBox<String> origin =
                new ComboBox<>();

        origin.getItems().addAll(
                "Mumbai",
                "Chennai",
                "Kolkata"
        );

        origin.setValue(
                "Mumbai"
        );

        ComboBox<String> destination =
                new ComboBox<>();

        destination.getItems().addAll(
                "Singapore",
                "Dubai",
                "London"
        );

        destination.setValue(
                "Singapore"
        );

        // =========================================================
        // ROW 1
        // =========================================================

        HBox row1 =
                new HBox(15);

        VBox nameBox =
                fieldBox(
                        "Cargo Name",
                        cargoName
                );

        VBox typeBox =
                fieldBox(
                        "Cargo Type",
                        cargoType
                );

        row1.getChildren().addAll(
                nameBox,
                typeBox
        );

        // =========================================================
        // ROW 2
        // =========================================================

        HBox row2 =
                new HBox(15);

        VBox quantityBox =
                fieldBox(
                        "Quantity",
                        quantity
                );

        VBox weightBox =
                fieldBox(
                        "Weight",
                        weight
                );

        row2.getChildren().addAll(
                quantityBox,
                weightBox
        );

        // =========================================================
        // ROW 3
        // =========================================================

        HBox row3 =
                new HBox(15);

        VBox originBox =
                fieldBox(
                        "Origin Port",
                        origin
                );

        VBox destinationBox =
                fieldBox(
                        "Destination Port",
                        destination
                );

        row3.getChildren().addAll(
                originBox,
                destinationBox
        );

        // =========================================================
        // REGISTER BUTTON
        // =========================================================

        Button register =
                new Button("Register Cargo");

        register.setMaxWidth(
                Double.MAX_VALUE
        );

        register.setPrefHeight(48);

        register.setStyle(
                "-fx-background-color: #287FF0;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 9px;"
        );

        // =========================================================
        // SUCCESS MESSAGE
        // =========================================================

        Label success =
                new Label(
                        "✓  Cargo registered successfully\n" +
                        "Cargo ID: CGO-1027"
                );

        success.setVisible(false);

        success.setMaxWidth(
                Double.MAX_VALUE
        );

        success.setPadding(
                new Insets(15)
        );

        success.setStyle(
                "-fx-background-color: #E7FAF1;" +
                "-fx-text-fill: #23855D;" +
                "-fx-background-radius: 10px;" +
                "-fx-font-size: 14px;"
        );

        register.setOnAction(event -> {

            success.setVisible(true);

        });

        // =========================================================
        // ADD FORM COMPONENTS
        // =========================================================

        form.getChildren().addAll(
                formTitle,
                formSub,
                row1,
                row2,
                row3,
                register,
                success
        );

        // =========================================================
        // IMPORTANT:
        // CENTER ONLY THE FORM
        // SIDEBAR, HEADER, SEARCH AND TITLE ARE NOT CHANGED
        // =========================================================

        HBox formContainer =
                new HBox();

        formContainer.setAlignment(
                Pos.CENTER
        );

        formContainer.setMaxWidth(
                Double.MAX_VALUE
        );

        formContainer.getChildren().add(
                form
        );

        // =========================================================
        // ADD CONTENT
        // =========================================================

        content.getChildren().addAll(
                searchBox,
                title,
                subtitle,
                formContainer
        );

        // =========================================================
        // ROOT
        // =========================================================

        BorderPane root =
                new BorderPane();

        root.setLeft(sidebar);

        root.setCenter(content);

        scene =
                new Scene(root);

        return scene;
    }

    // =============================================================
    // SIDEBAR
    // =============================================================

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
                        "S M A R T   P O R T\n" +
                        "O P E R A T I O N S"
                );

        sub.setStyle(
                "-fx-fill: #8FA0B2;" +
                "-fx-font-size: 11px;"
        );

        Button dashboard =
                menu(
                        "🏠   Dashboard",
                        false
                );

        Button register =
                menu(
                        "📦   Register Cargo",
                        true
                );

        Button book =
                menu(
                        "🚢   Book Container",
                        false
                );

        Button upload =
                menu(
                        "📄   Upload Document",
                        false
                );

        Button payment =
                menu(
                        "💳   Make Payment",
                        false
                );

        Button track =
                menu(
                        "📥   Track Shipment",
                        false
                );

        Button logout =
                menu(
                        "⇥   Logout",
                        false
                );

        dashboard.setOnAction(
                e -> openDashboard()
        );

        book.setOnAction(e -> {

            BookContainer page =
                    new BookContainer();

            Dashboard.dashboardStage.setScene(
                    page.getBookContainerScene(
                            this::back
                    )
            );

        });

        upload.setOnAction(e -> {

            UploadDocument page =
                    new UploadDocument();

            Dashboard.dashboardStage.setScene(
                    page.getUploadDocumentScene(
                            this::back
                    )
            );

        });

        payment.setOnAction(e -> {

            MakePayment page =
                    new MakePayment();

            Dashboard.dashboardStage.setScene(
                    page.getMakePaymentScene(
                            this::back
                    )
            );

        });

        track.setOnAction(e -> {

            TrackShipment page =
                    new TrackShipment();

            Dashboard.dashboardStage.setScene(
                    page.getTrackShipmentScene(
                            this::back
                    )
            );

        });

        logout.setOnAction(
                e -> new Logout().showLogout()
        );

        Region space =
                new Region();

        VBox.setVgrow(
                space,
                Priority.ALWAYS
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

    // =============================================================
    // MENU BUTTON
    // =============================================================

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

        if (active) {

            button.setStyle(
                    "-fx-background-color: #287FF0;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 9px;" +
                    "-fx-font-size: 15px;" +
                    "-fx-alignment: CENTER_LEFT;" +
                    "-fx-padding: 0 15 0 15;" +
                    "-fx-cursor: hand;" +
                    "-fx-focus-color: transparent;" +
                    "-fx-faint-focus-color: transparent;"
            );

        } else {

            button.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: #C1CBD6;" +
                    "-fx-font-size: 15px;" +
                    "-fx-alignment: CENTER_LEFT;" +
                    "-fx-padding: 0 15 0 15;" +
                    "-fx-cursor: hand;" +
                    "-fx-focus-color: transparent;" +
                    "-fx-faint-focus-color: transparent;"
            );
        }

        return button;
    }

    // =============================================================
    // FIELD BOX
    // =============================================================

    private VBox fieldBox(
            String name,
            javafx.scene.Node field
    ) {

        VBox box =
                new VBox(7);

        box.setPrefWidth(280);

        Label label =
                new Label(name);

        label.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;"
        );

        if (field instanceof TextField) {

            ((TextField) field).setPrefHeight(42);

        }

        if (field instanceof ComboBox) {

            ((ComboBox<?>) field).setPrefHeight(42);

            ((ComboBox<?>) field).setMaxWidth(
                    Double.MAX_VALUE
            );

        }

        box.getChildren().addAll(
                label,
                field
        );

        return box;
    }

    // =============================================================
    // DASHBOARD
    // =============================================================

    private void openDashboard() {

        new Dashboard().start(
                Dashboard.dashboardStage
        );
    }

    private void back() {

        new Dashboard().start(
                Dashboard.dashboardStage
        );
    }

    // =============================================================
    // HEADER - NOT CHANGED
    // =============================================================

    private HBox createHeader(
            String pageTitle,
            String breadcrumb
    ) {

        HBox header =
                new HBox(15);

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        header.setPadding(
                new Insets(12, 22, 12, 25)
        );

        header.setPrefHeight(65);

        header.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e5ebf3;" +
                "-fx-border-width:0 0 1 0;"
        );

        VBox pageInfo =
                new VBox(3);

        Label title =
                new Label(pageTitle);

        title.setStyle(
                "-fx-text-fill:#111827;" +
                "-fx-font-size:17;" +
                "-fx-font-weight:bold;"
        );

        Label bc =
                new Label(breadcrumb);

        bc.setStyle(
                "-fx-text-fill:#8a93a1;" +
                "-fx-font-size:10;"
        );

        pageInfo.getChildren().addAll(
                title,
                bc
        );

        Region space =
                new Region();

        HBox.setHgrow(
                space,
                Priority.ALWAYS
        );

        Label status =
                new Label(
                        "● System Operational"
                );

        status.setStyle(
                "-fx-text-fill:#168548;" +
                "-fx-background-color:#e7f7ee;" +
                "-fx-background-radius:14;" +
                "-fx-padding:6 10 6 10;" +
                "-fx-font-size:10;" +
                "-fx-font-weight:bold;"
        );

        String date =
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "dd MMM yyyy"
                        )
                );

        Label dateLabel =
                new Label(date);

        dateLabel.setStyle(
                "-fx-text-fill:#4b5563;" +
                "-fx-font-size:11;" +
                "-fx-font-weight:bold;"
        );

        Label notification =
                new Label("🔔");

        notification.setStyle(
                "-fx-text-fill:#111827;" +
                "-fx-font-size:17;"
        );

        Label avatar =
                new Label("A");

        avatar.setMinSize(
                32,
                32
        );

        avatar.setAlignment(
                Pos.CENTER
        );

        avatar.setStyle(
                "-fx-background-color:#dbeafe;" +
                "-fx-background-radius:50;" +
                "-fx-text-fill:#0f5fff;" +
                "-fx-font-weight:bold;"
        );

        VBox profile =
                new VBox(1);

        Label user =
                new Label("Dock Tech Admin");

        user.setStyle(
                "-fx-text-fill:#111827;" +
                "-fx-font-size:11;" +
                "-fx-font-weight:bold;"
        );

        Label role =
                new Label("Administrator");

        role.setStyle(
                "-fx-text-fill:#8a93a1;" +
                "-fx-font-size:9;"
        );

        profile.getChildren().addAll(
                user,
                role
        );

        header.getChildren().addAll(
                pageInfo,
                space,
                status,
                dateLabel,
                notification,
                avatar,
                profile
        );

        return header;
    }
}