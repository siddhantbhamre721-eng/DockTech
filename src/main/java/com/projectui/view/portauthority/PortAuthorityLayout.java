package com.projectui.view.portauthority;

import java.util.LinkedHashMap;
import java.util.Map;

import com.projectui.view.login.DashboardLayoutHelper;
import com.projectui.view.portauthority.DashboardPage;
import com.projectui.view.portauthority.ShipManagementPage;
import com.projectui.view.portauthority.CargoManagementPage;
import com.projectui.view.portauthority.ContainerManagementPage;
import com.projectui.view.portauthority.PaymentPage;
import com.projectui.view.portauthority.ReportsPage;
import com.projectui.view.portauthority.SettingsPage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PortAuthorityLayout {

    private final BorderPane root = new BorderPane();
    private final VBox content = new VBox();
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    
    private String currentPage = "Dashboard";
    private Label headerTitleLabel;
    private Label breadcrumbLabel;

    public PortAuthorityLayout() {
        root.setStyle("-fx-background-color:#f5f8fc;");

        root.setLeft(createSidebar());
        root.setTop(createHeader());

        content.setPadding(new Insets(18, 20, 20, 20));
        content.setSpacing(14);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color:#f5f8fc; -fx-background: transparent; -fx-border-color:transparent;");

        root.setCenter(scrollPane);
    }

    // =====================================================
    // MAIN CONTENT ROUTER
    // =====================================================

    public void setContent(Node page, String title, String path) {
        this.currentPage = title != null ? title : "Dashboard";
        
        if (headerTitleLabel != null) {
            headerTitleLabel.setText(currentPage);
        }
        if (breadcrumbLabel != null && path != null) {
            breadcrumbLabel.setText(path);
        }

        refreshActiveNav();

        content.getChildren().clear();
        if (page != null) {
            content.getChildren().add(page);
        }
    }

    public BorderPane getRoot() {
        return root;
    }

    // =====================================================
    // ADMIN-STYLE SIDEBAR
    // =====================================================

    private VBox createSidebar() {
        VBox nav = new VBox(10);
        nav.setPrefWidth(215);
        nav.setPadding(new Insets(20, 12, 18, 12));
        nav.setStyle("-fx-background-color:#051029;");

        // BRAND / LOGO
        Label brand = new Label("DOCK TECH");
        brand.setStyle("-fx-text-fill:white; -fx-font-size:18; -fx-font-weight:bold;");
        
        Label small = new Label("SMART PORT OPERATIONS");
        small.setStyle("-fx-text-fill:#9aaac0; -fx-font-size:10; -fx-font-weight:bold;");
        
        Label logoIcon = new Label("⚓");
        logoIcon.setStyle("-fx-text-fill:white; -fx-font-size:20; -fx-font-weight:bold; -fx-padding:0 4 0 0;");
        
        VBox textBox = new VBox(2, brand, small);
        HBox brandBox = new HBox(8, logoIcon, textBox);
        brandBox.setPadding(new Insets(0, 0, 22, 6));

        // PORT AUTHORITY NAVIGATION ITEMS
        Button dashboard = createNavButton("Dashboard");
        Button ship = createNavButton("Ship Management");
        Button cargo = createNavButton("Cargo Management");
        Button container = createNavButton("Container Management");
        Button payments = createNavButton("Payments");
        Button reports = createNavButton("Reports & Analytics");
        Button settings = createNavButton("Settings");

        navButtons.clear();
        navButtons.put("Dashboard", dashboard);
        navButtons.put("Ship Management", ship);
        navButtons.put("Cargo Management", cargo);
        navButtons.put("Container Management", container);
        navButtons.put("Payments", payments);
        navButtons.put("Reports & Analytics", reports);
        navButtons.put("Settings", settings);

        // EVENT HANDLERS
        dashboard.setOnAction(e -> setContent(
                new DashboardPage().getRoot(),
                "Dashboard",
                "Dock Tech / Port Authority / Dashboard"
        ));

        ship.setOnAction(e -> setContent(
                new ShipManagementPage().getRoot(),
                "Ship Management",
                "Dock Tech / Port Authority / Ship Management"
        ));

        cargo.setOnAction(e -> setContent(
                new CargoManagementPage().getContent(),
                "Cargo Management",
                "Dock Tech / Port Authority / Cargo Management"
        ));

        container.setOnAction(e -> setContent(
                new ContainerManagementPage().getContent(),
                "Container Management",
                "Dock Tech / Port Authority / Container Management"
        ));

        payments.setOnAction(e -> setContent(
                new PaymentPage().getContent(),
                "Payments",
                "Dock Tech / Port Authority / Payments"
        ));

        reports.setOnAction(e -> setContent(
                new ReportsPage().getContent(),
                "Reports & Analytics",
                "Dock Tech / Port Authority / Reports & Analytics"
        ));

        settings.setOnAction(e -> setContent(
                new SettingsPage().getContent(),
                "Settings",
                "Dock Tech / Port Authority / Settings"
        ));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        nav.getChildren().addAll(
                brandBox,
                dashboard,
                ship,
                cargo,
                container,
                payments,
                reports,
                settings,
                spacer
        );

        refreshActiveNav();
        return nav;
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(navStyle(false, text.equalsIgnoreCase(currentPage)));
        
        button.setOnMouseEntered(e -> button.setStyle(navStyle(true, text.equalsIgnoreCase(currentPage))));
        button.setOnMouseExited(e -> button.setStyle(navStyle(false, text.equalsIgnoreCase(currentPage))));
        return button;
    }

    private String navStyle(boolean hover, boolean selected) {
        String bg = (hover || selected) ? "#0b63ff" : "transparent";
        return "-fx-background-color:" + bg + ";"
                + "-fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold;"
                + "-fx-padding:12 16; -fx-background-radius:8; -fx-cursor:hand;";
    }

    private void refreshActiveNav() {
        navButtons.forEach((name, btn) -> {
            boolean isSel = name.equalsIgnoreCase(currentPage);
            btn.setStyle(navStyle(false, isSel));
        });
    }

    // =====================================================
    // ADMIN-STYLE TOP HEADER & USER PROFILE POPUP
    // =====================================================

    private HBox createHeader() {
        Runnable logoutRunnable = () -> {
            if (root.getScene() != null && root.getScene().getWindow() instanceof Stage) {
                new com.projectui.view.login.LoginPage().show((Stage) root.getScene().getWindow());
            }
        };

        HBox header = DashboardLayoutHelper.createHeader(
                null,
                currentPage,
                "Dock Tech / Port Authority / Dashboard",
                "Port Authority",
                logoutRunnable,
                null
        );

        // Capture internal title & breadcrumb labels from header for dynamic updates
        if (header.getChildren().size() > 0 && header.getChildren().get(0) instanceof VBox) {
            VBox pageInfo = (VBox) header.getChildren().get(0);
            if (pageInfo.getChildren().size() > 0 && pageInfo.getChildren().get(0) instanceof Label) {
                headerTitleLabel = (Label) pageInfo.getChildren().get(0);
            }
            if (pageInfo.getChildren().size() > 1 && pageInfo.getChildren().get(1) instanceof Label) {
                breadcrumbLabel = (Label) pageInfo.getChildren().get(1);
            }
        }

        return header;
    }
}