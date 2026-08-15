package com.projectui.view.exporter;





import javafx.application.Application;
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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Dashboard extends Application {

    public static Stage dashboardStage;

    private Scene dashboardScene;
    private static boolean styleInitialized = false;

    @Override
    public void start(Stage stage) {

        dashboardStage = stage;

        if (!styleInitialized) {
            dashboardStage.initStyle(StageStyle.UNDECORATED);
            styleInitialized = true;
        }

        // =========================
        // SIDEBAR
        // =========================

        VBox sidebar = new VBox(14);

        sidebar.setPadding(
                new Insets(22, 14, 20, 14)
        );

        sidebar.setPrefWidth(285);
         sidebar.setPrefHeight(285);


        sidebar.setStyle(
                "-fx-background-color: #050A10;"
        );

        // LOGO
        Label logo = new Label("⚓  DOCK TECH");
        logo.setStyle("-fx-text-fill:white; -fx-font-size:18; -fx-font-weight:bold;");

        Label logoSubtitle = new Label("SMART PORT\nOPERATIONS");
        logoSubtitle.setStyle("-fx-text-fill:#9aaac0; -fx-font-size:9; -fx-font-weight:bold;");

        VBox logoBox = new VBox(3, logo, logoSubtitle);
        logoBox.setPadding(new Insets(0, 0, 22, 10));

        // NAVIGATION
        Button dashboard = createMenuButton("🏠   Dashboard", true);
        Button register = createMenuButton("📦  Register Cargo", false);
        Button book = createMenuButton("🚢   Book Container", false);
        Button upload = createMenuButton("📄   Upload Document", false);
        Button payment = createMenuButton("💳   Make Payment", false);
        Button track = createMenuButton("📥   Track Shipment", false);
        Button logout = createMenuButton("⇥   Logout", false);

        register.setOnAction(event -> {
            RegisterCargo page = new RegisterCargo();
            dashboardStage.setScene(page.getRegisterCargoScene(this::showDashboard));
            Dashboard.dashboardStage.setFullScreen(true);
        });

        book.setOnAction(event -> {
            BookContainer page = new BookContainer();
            dashboardStage.setScene(page.getBookContainerScene(this::showDashboard));
            Dashboard.dashboardStage.setFullScreen(true);
        });

        upload.setOnAction(event -> {
            UploadDocument page = new UploadDocument();
            dashboardStage.setScene(page.getUploadDocumentScene(this::showDashboard));
            Dashboard.dashboardStage.setFullScreen(true);
        });

        payment.setOnAction(event -> {
            MakePayment page = new MakePayment();
            dashboardStage.setScene(page.getMakePaymentScene(this::showDashboard));
            Dashboard.dashboardStage.setFullScreen(true);
        });

        track.setOnAction(event -> {
            TrackShipment page = new TrackShipment();
            dashboardStage.setScene(page.getTrackShipmentScene(this::showDashboard));
            Dashboard.dashboardStage.setFullScreen(true);
        });

        logout.setOnAction(event -> {
            Logout page = new Logout();
            page.showLogout();
        });

        sidebar.getChildren().addAll(logoBox, dashboard, register, book, upload, payment, track, logout);

        // =========================
        // MAIN CONTENT
        // =========================

        VBox content = new VBox(22);

        content.setPadding(new Insets(35, 40, 30, 40));
        content.setStyle("-fx-background-color: #F4F8FC;");

        // Header
        content.getChildren().add(createHeader("Dashboard", "Dock Tech / Port Authority / Dashboard"));

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
                new Text("Exporter Dashboard");

        title.setStyle(
                "-fx-font-size: 30px;" +
                "-fx-font-weight: bold;" +
                "-fx-fill: #101A2B;"
        );

        Text subtitle =
                new Text(
                        "Manage your cargo, containers, documents and shipments."
                );

        subtitle.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-fill: #778394;"
        );

        // =========================
        // STAT CARDS
        // =========================

        HBox stats = new HBox(18);

        stats.getChildren().addAll(
                createStatCard(
                        "Active Cargo",
                        "5",
                        "📦"
                ),
                createStatCard(
                        "Containers",
                        "3",
                        "🚢"
                ),
                createStatCard(
                        "Documents",
                        "4",
                        "📄"
                ),
                createStatCard(
                        "Shipments",
                        "2",
                        "📥"
                )
        );

        // =========================
        // RECENT CARGO
        // =========================

        VBox recentCargo =
                new VBox(15);

        recentCargo.setPadding(
                new Insets(25)
        );

        recentCargo.setPrefWidth(550);
        recentCargo.setPrefHeight(420);

        recentCargo.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18px;" +
                "-fx-border-color: #E2E7ED;" +
                "-fx-border-radius: 18px;"
        );

        Text recentTitle =
                new Text("Recent Cargo");

        recentTitle.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;"
        );

        Text recentSub =
                new Text(
                        "Latest cargo registrations and status updates."
                );

        recentSub.setStyle(
                "-fx-fill: #778394;"
        );

        recentCargo.getChildren().addAll(
                recentTitle,
                recentSub,
                cargoHeader(),
                cargoRow(
                        "CGO-1024",
                        "Electronics",
                        "Registered",
                        true
                ),
                cargoRow(
                        "CGO-1025",
                        "Machinery",
                        "In Transit",
                        true
                ),
                cargoRow(
                        "CGO-1026",
                        "Textiles",
                        "Pending",
                        false
                )
        );

        // =========================
        // RIGHT SIDE
        // =========================

        VBox rightSide =
                new VBox(22);

        rightSide.setPrefWidth(330);

        // Shipment Status

        VBox shipmentStatus =
                new VBox(18);

        shipmentStatus.setPadding(
                new Insets(25)
        );

        shipmentStatus.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18px;" +
                "-fx-border-color: #E2E7ED;" +
                "-fx-border-radius: 18px;"
        );

        Text shipmentTitle =
                new Text("Shipment Status");

        shipmentTitle.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;"
        );

        Text shipmentSub =
                new Text(
                        "Current exporter shipments and progress."
                );

        shipmentSub.setStyle(
                "-fx-fill: #778394;"
        );

        shipmentStatus.getChildren().addAll(
                shipmentTitle,
                shipmentSub,
                shipment(
                        "Oceanic Star",
                        "Mumbai → Singapore",
                        "ETA: 14 Aug",
                        "● In Transit"
                ),
                shipment(
                        "Sea Voyager",
                        "Mumbai → Dubai",
                        "ETA: Pending",
                        "● Preparing"
                )
        );

        // Pending Action

        VBox pending =
                new VBox(18);

        pending.setPadding(
                new Insets(25)
        );

        pending.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 18px;" +
                "-fx-border-color: #E2E7ED;" +
                "-fx-border-radius: 18px;"
        );

        Text pendingTitle =
                new Text("Pending Action");

        pendingTitle.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;"
        );

        Text pendingSub =
                new Text(
                        "Important tasks requiring attention."
                );

        pendingSub.setStyle(
                "-fx-fill: #778394;"
        );

        Label action1 =
                new Label(
                        "●   Document required for CGO-1026"
                );

        Label action2 =
                new Label(
                        "●   Container booking pending"
                );

        action1.setStyle(
                "-fx-background-color: #FFF2E5;" +
                "-fx-text-fill: #C65B20;" +
                "-fx-padding: 12px;" +
                "-fx-background-radius: 12px;"
        );

        action2.setStyle(
                "-fx-background-color: #FFF2E5;" +
                "-fx-text-fill: #C65B20;" +
                "-fx-padding: 12px;" +
                "-fx-background-radius: 12px;"
        );

        pending.getChildren().addAll(
                pendingTitle,
                pendingSub,
                action1,
                action2
        );

        rightSide.getChildren().addAll(
                shipmentStatus,
                pending
        );

        HBox lower =
                new HBox(
                        25,
                        recentCargo,
                        rightSide
                );

        content.getChildren().addAll(
                searchBox,
                title,
                subtitle,
                stats,
                lower
        );

        BorderPane root =
                new BorderPane();

        root.setLeft(sidebar);
        root.setCenter(content);

        dashboardScene =
                new Scene(root);

        dashboardStage.setScene(dashboardScene);

        dashboardStage.setTitle("Dock Tech - Exporter Dashboard");

        dashboardStage.setFullScreen(true);

        dashboardStage.show();
    }

    public void showDashboard() {

        dashboardStage.setScene(
                dashboardScene
        );
        dashboardStage.setFullScreen(true);
    }

    private Button createMenuButton(
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
                    "-fx-font-weight: bold;" +
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

    private VBox createStatCard(
            String name,
            String value,
            String icon
    ) {

        VBox card =
                new VBox(8);

        card.setPadding(
                new Insets(20)
        );

        card.setPrefWidth(200);
        card.setPrefHeight(100);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 15px;" +
                "-fx-border-color: #E0E5EB;" +
                "-fx-border-radius: 15px;"
        );

        Text nameText =
                new Text(name);

        nameText.setStyle(
                "-fx-fill: #697687;" +
                "-fx-font-size: 14px;"
        );

        Text valueText =
                new Text(value);

        valueText.setStyle(
                "-fx-fill: #0f1725;" +
                "-fx-font-size: 25px;" +
                "-fx-font-weight: bold;"
        );

        Text iconText =
                new Text(icon);

        iconText.setStyle(
                "-fx-fill: #1686EA;" +
                "-fx-font-size: 22px;"
        );

        HBox top =
                new HBox();

        Region space =
                new Region();

        HBox.setHgrow(
                space,
                javafx.scene.layout.Priority.ALWAYS
        );

        top.getChildren().addAll(
                nameText,
                space,
                iconText
        );

        card.getChildren().addAll(
                top,
                valueText
        );

        return card;
    }

    private HBox cargoHeader() {

        HBox row =
                new HBox();

        row.setPadding(
                new Insets(12)
        );

        row.setStyle(
                "-fx-background-color: #F8FAFC;"
        );

        row.getChildren().addAll(
                headerText("CARGO ID", 135),
                headerText("TYPE", 150),
                headerText("STATUS", 130)
        );

        return row;
    }

    private HBox cargoRow(
            String id,
            String type,
            String status,
            boolean active
    ) {

        HBox row =
                new HBox();

        row.setPadding(
                new Insets(15, 12, 15, 12)
        );

        row.getChildren().addAll(
                normalText(id, 135),
                normalText(type, 150),
                statusLabel(status, active)
        );

        return row;
    }

    private Text headerText(
            String text,
            double width
    ) {

        Text value =
                new Text(text);

        value.setWrappingWidth(width);

        value.setStyle(
                "-fx-fill: #738092;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
        );

        return value;
    }

    private Text normalText(
            String text,
            double width
    ) {

        Text value =
                new Text(text);

        value.setWrappingWidth(width);

        value.setStyle(
                "-fx-fill: #263445;" +
                "-fx-font-size: 14px;"
        );

        return value;
    }

    private Label statusLabel(
            String status,
            boolean active
    ) {

        Label label =
                new Label(status);

        if (active) {

            label.setStyle(
                    "-fx-background-color: #EAF2FF;" +
                    "-fx-text-fill: #2768C8;" +
                    "-fx-padding: 5px 10px;" +
                    "-fx-background-radius: 10px;"
            );

        } else {

            label.setStyle(
                    "-fx-background-color: #FFF0DF;" +
                    "-fx-text-fill: #C45B1C;" +
                    "-fx-padding: 5px 10px;" +
                    "-fx-background-radius: 10px;"
            );
        }

        return label;
    }

    private VBox shipment(
            String name,
            String route,
            String eta,
            String status
    ) {

        VBox box =
                new VBox(7);

        box.setPadding(
                new Insets(16)
        );

        box.setStyle(
                "-fx-background-color: #F8FAFC;" +
                "-fx-background-radius: 14px;"
        );

        Text title =
                new Text(name);

        title.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;"
        );

        Text routeText =
                new Text(route);

        routeText.setStyle(
                "-fx-fill: #758294;"
        );

        Text etaText =
                new Text(eta);

        etaText.setStyle(
                "-fx-fill: #526174;"
        );

        Text statusText =
                new Text(status);

        statusText.setStyle(
                "-fx-fill: #2575D4;" +
                "-fx-font-weight: bold;"
        );

        box.getChildren().addAll(
                title,
                routeText,
                etaText,
                statusText
        );

        return box;
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
