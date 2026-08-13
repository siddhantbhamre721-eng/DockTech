package com.projectui.view.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Central admin shell providing the left sidebar, header and a center area where
 * pages are swapped in. This class intentionally keeps styling and navigation
 * minimal and delegates visual helpers to `AdminStyleManager` (via AdminTheme).
 */
public class AdminDashboard extends AdminTheme {

    public Parent getRoot(Runnable logoutAction, String pageName) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(0));

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(12, 18, 12, 18));
        header.setStyle("-fx-background-color:transparent; -fx-border-color:transparent transparent #eaeef6 transparent;");

        Label title = new Label("Dock Tech Admin");
        title.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:16; -fx-font-weight:bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logout = secondaryButton("Logout");
        logout.setOnAction(e -> {
            if (logoutAction != null) logoutAction.run();
        });

        header.getChildren().addAll(title, spacer, logout);

        // Sidebar navigation
        VBox nav = new VBox(10);
        nav.setPadding(new Insets(18));
        nav.setStyle("-fx-background-color:transparent;");

        Button dashBtn = secondaryButton("Dashboard");
        dashBtn.setMaxWidth(Double.MAX_VALUE);
        Button portsBtn = secondaryButton("Port Management");
        portsBtn.setMaxWidth(Double.MAX_VALUE);
        Button usersBtn = secondaryButton("User Management");
        usersBtn.setMaxWidth(Double.MAX_VALUE);
        Button reportsBtn = secondaryButton("Reports & Analytics");
        reportsBtn.setMaxWidth(Double.MAX_VALUE);
        Button settingsBtn = secondaryButton("Settings");
        settingsBtn.setMaxWidth(Double.MAX_VALUE);

        nav.getChildren().addAll(dashBtn, portsBtn, usersBtn, reportsBtn, settingsBtn);

        // Center content area
        BorderPane center = new BorderPane();
        center.setPadding(new Insets(18));
        center.setCenter(new DashboardPage().getRoot());

        dashBtn.setOnAction(e -> center.setCenter(new DashboardPage().getRoot()));
        portsBtn.setOnAction(e -> center.setCenter(new PortManagementPage().getRoot()));
        usersBtn.setOnAction(e -> center.setCenter(new UserManagementPage().getRoot()));
        reportsBtn.setOnAction(e -> center.setCenter(new ReportsPage().getRoot()));
        settingsBtn.setOnAction(e -> center.setCenter(new SettingsPage().getRoot()));

        // Layout
        root.setTop(header);
        root.setLeft(nav);
        root.setCenter(center);

        return root;
    }

}
