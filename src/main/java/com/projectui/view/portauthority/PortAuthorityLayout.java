package com.projectui.view.portauthority;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PortAuthorityLayout {

    private final BorderPane root = new BorderPane();

    private final VBox content = new VBox();

    private final Label pageTitle = new Label();
    
    private final Label breadcrumb = new Label();

    public PortAuthorityLayout() {

        root.setStyle(
                "-fx-background-color:#f3f8ff;"
        );

        root.setLeft(createSidebar());

        root.setTop(createHeader());

        content.setPadding(
                new Insets(18, 20, 20, 20)
        );

        content.setSpacing(12);

        ScrollPane scrollPane =
                new ScrollPane();

        scrollPane.setContent(
                content
                );

        scrollPane.setFitToWidth(true);

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scrollPane.setStyle(
                "-fx-background-color: #f3f8ff;" +
                "-fx-border-color:transparent;"
        );

        scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);


        root.setCenter(
                scrollPane
        );
    }

    // =====================================================
    // MAIN CONTENT
    // =====================================================

    public void setContent(Node page, String title, String path) {

        content.getChildren().clear();

        content.getChildren().add(page);

        pageTitle.setText(
                title
        );

        breadcrumb.setText(
                    path
        );
    }

    public BorderPane getRoot() {

        return root;
    }

    // =====================================================
    // SIDEBAR
    // =====================================================

    private VBox createSidebar() {

        VBox sidebar = new VBox(8);

        sidebar.setPrefWidth(220);

        sidebar.setPadding(
                new Insets(22, 14, 20, 14)
        );

        sidebar.setStyle(
                "-fx-background-color: #050911;"
        );

        // LOGO

        Label logo =
                new Label("⚓  DOCK TECH");

        logo.setStyle(
                "-fx-text-fill:white;" +
                "-fx-font-size:18;" +
                "-fx-font-weight:bold;"
        );

        Label subtitle =
                new Label(
                        "SMART PORT\nOPERATIONS"
                );

        subtitle.setStyle(
                "-fx-text-fill:#9aaac0;" +
                "-fx-font-size:9;" +
                "-fx-font-weight:bold;"
        );

        VBox logoBox =
                new VBox(
                        3,
                        logo,
                        subtitle
                );

        logoBox.setPadding(
                new Insets(0, 0, 22, 10)
        );

        // NAVIGATION

        Button dashboard =
                navButton("⌂", "Dashboard");

        Button ship =
                navButton("●", "Ship Management");

        Button cargo =
                navButton("◆", "Cargo Management");

        Button container =
                navButton("▣", "Container Management");

        Button allocation =
                navButton("▤", "Container Allocation");

        Button payments =
                navButton("₹", "Payments");

        Button reports =
                navButton("▣", "Reports & Analytics");

        Button settings =
                navButton("⚙", "Settings");

        sidebar.getChildren().addAll(
                logoBox,
                dashboard,
                ship,
                cargo,
                container,
                allocation,
                payments,
                reports,
                settings
        );

        //-----SetOnAction-------------------------------

        dashboard.setOnAction(e -> {

                setContent(
                        new DashboardPage().getRoot(),
                        "Dashboard",
                        "Dock Tech / Port Authority / Dashboard"
                );
        });

        ship.setOnAction(e -> {

                setContent(
                        new ShipManagementPage().getRoot(),
                        "Ship Management",
                        "Dock Tech / Port Authority / Ship Management"
                );
        });

        cargo.setOnAction(e -> {

                setContent(new CargoManagementPage().getContent(), 
                "Cargo Management",
                "Dock Tech / Port Authority / Cargo Management"
                );
        });

        container.setOnAction(e -> {

                setContent(new ContainerManagementPage().getContent(),
                "Container Management",
                "Dock Tech / Port Authority / Container Management"
                );
        });

        allocation.setOnAction(e -> {
                
                setContent(new ContainerAllocationPage().getContent(), 
                "Container Allocation", 
                "Dock Teck / Port Authority / Container Allocation");
        });

        payments.setOnAction(e -> {

                setContent(new PaymentPage().getContent(),
                "Payment Management",
                 "Dock Teck / Port Authority / Payment Management");
        });

        reports.setOnAction(e -> {

                setContent(new ReportsPage().getContent(),
                "Reports & Analytics", 
                "Dock Teck / Port Authority / Reports and Analytics");
        });

        settings.setOnAction(e -> {

                setContent(new SettingsPage().getContent(),
                        "Settings",
                        "Dock Tech / Port Authority / Settings"
        );

});

        return sidebar;
    }

    // =====================================================
    // NAVIGATION BUTTON
    // =====================================================

    private Button navButton(
            String icon,
            String text) {

        Button button =
                new Button(
                        icon + "   " + text
                );

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setPrefHeight(42);

        button.setAlignment(
                Pos.CENTER_LEFT
        );

        button.setPadding(
                new Insets(
                        0,
                        10,
                        0,
                        12
                )
        );

        button.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-text-fill:#aebbd0;" +
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:8;" +
                "-fx-cursor:hand;"
        );

        button.setOnMouseEntered(e -> {

            button.setStyle(
                    "-fx-background-color:#10264a;" +
                    "-fx-text-fill:white;" +
                    "-fx-font-size:12;" +
                    "-fx-font-weight:bold;" +
                    "-fx-background-radius:8;" +
                    "-fx-cursor:hand;"
            );
        });

        button.setOnMouseExited(e -> {

            button.setStyle(
                    "-fx-background-color:transparent;" +
                    "-fx-text-fill:#aebbd0;" +
                    "-fx-font-size:12;" +
                    "-fx-font-weight:bold;" +
                    "-fx-background-radius:8;" +
                    "-fx-cursor:hand;"
            );
        });

        return button;
    }

    // =====================================================
    // TOP HEADER
    // =====================================================

    private HBox createHeader() {

        HBox header =
                new HBox(15);

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        header.setPadding(
                new Insets(
                        12,
                        22,
                        12,
                        25
                )
        );

        header.setPrefHeight(65);

        header.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color: #e5ebf3;" +
                "-fx-border-width:0 0 1 0;"
        );

        // PAGE INFORMATION

        VBox pageInfo =
                new VBox(3);

        pageTitle.setText(
                        "Dashboard"
        );

        pageTitle.setStyle(
                "-fx-text-fill: #111827;" +
                "-fx-font-size:17;" +
                "-fx-font-weight:bold;"
        );

        breadcrumb.setText(
                "Dock Tech / Port Authority / Dashboard"
        );

        breadcrumb.setStyle(
                "-fx-text-fill: #8a93a1;" +
                "-fx-font-size:10;"
        );

        pageInfo.getChildren().addAll(
                pageTitle,
                breadcrumb
        );

        // SPACE

        Region space =
                new Region();

        HBox.setHgrow(
                space,
                Priority.ALWAYS
        );

        // SYSTEM STATUS

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

        // DATE

        String date =
                LocalDateTime.now()
                        .format(
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

        // NOTIFICATION

        Button notification =
                new Button("🔔");

        notification.setStyle(
                "-fx-text-fill:#111827;" +
                "-fx-font-size:17;"
        );

        // PROFILE

        // =====================================================
        // PROFILE MENU
        // =====================================================

        MenuButton profileButton =
                new MenuButton("P  Port Authority");

        profileButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #111827;" +
                "-fx-font-size:11;" +
                "-fx-font-weight:bold;" +
                "-fx-cursor:hand;"
        );


        // =====================================================
        // LOGOUT MENU ITEM
        // =====================================================

        MenuItem logout =
                new MenuItem("Logout");

        profileButton.getItems().add(
                logout
        );


        // =====================================================
        // LOGOUT ACTION
        // =====================================================

        logout.setOnAction(e -> {

        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION
                );

        alert.setTitle("Logout");

        alert.setHeaderText(
                "Logout"
        );

        alert.setContentText(
                "Are you sure you want to logout?"
        );

        alert.showAndWait();
        });

        header.getChildren().addAll(
                pageInfo,
                space,
                status,
                dateLabel,
                notification,
                profileButton
        );

        return header;
    }
}