package com.projectui.view.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
// MenuButton/MenuItem removed; using a Popup for profile dropdown
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.stage.Stage;
import javafx.geometry.Bounds;
import javafx.scene.control.Separator;
import java.time.format.DateTimeFormatter;

public class DockTechAdmin extends AdminTheme {

    private BorderPane center;
    private Runnable logoutAction;
    private String currentPage = "Dashboard";
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private Label headerTitleLabel;

    public Parent getRoot(Runnable logoutAction, String pageName) {
        this.logoutAction = logoutAction;
        this.currentPage = pageName == null || pageName.isBlank() ? "Dashboard" : pageName;

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(0));
        root.setStyle("-fx-background-color:#f5f8fc;");
        center = new BorderPane();
        center.setPadding(new Insets(18));

        Label title = new Label(currentPage);
        title.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:18; -fx-font-weight:bold;");
        this.headerTitleLabel = title;

        root.setLeft(sidebar(title));
        root.setTop(header(title));
        root.setCenter(center);
        showPage(currentPage);

        // Preserve window size/state when this root is attached to a Scene/Window.
        root.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene == null)
                return;
            newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                if (newWin == null)
                    return;
                if (newWin instanceof Stage) {
                    Stage stage = (Stage) newWin;
                    // Bind root preferred size to stage to avoid unexpected shrinking.
                    root.prefWidthProperty().bind(stage.widthProperty());
                    root.prefHeightProperty().bind(stage.heightProperty());
                }
            });
        });

        return root;
    }

    private static void showCustomPopup(Window owner, String title1) {
        String nameVal = "";
        String emailVal = "";
        String phoneVal = "";
        String companyVal = "";
        String roleVal = "Administrator";
        String uidVal = "";
        String statusVal = "Active";

        if (com.projectui.view.login.LoginPage.loggedInUser != null) {
            Map<String, Object> u = com.projectui.view.login.LoginPage.loggedInUser;
            if (u.get("name") != null) nameVal = u.get("name").toString();
            else if (u.get("fullName") != null) nameVal = u.get("fullName").toString();

            if (u.get("email") != null) emailVal = u.get("email").toString();
            if (u.get("role") != null) roleVal = u.get("role").toString();
            if (u.get("phone") != null) phoneVal = u.get("phone").toString();
            if (u.get("company") != null) companyVal = u.get("company").toString();
            else if (u.get("organization") != null) companyVal = u.get("organization").toString();
            if (u.get("uid") != null) uidVal = u.get("uid").toString();
            if (u.get("status") != null) statusVal = u.get("status").toString();
        }

        if (nameVal.isBlank()) {
            AdminDataStore store = AdminDataStore.getInstance();
            if (store.adminName != null && !store.adminName.isBlank()) nameVal = store.adminName;
            if (store.adminEmail != null && !store.adminEmail.isBlank()) emailVal = store.adminEmail;
        }
        if (nameVal.isBlank()) nameVal = "System Administrator";
        if (emailVal.isBlank()) emailVal = "admin@docktech.com";
        if (phoneVal.isBlank()) phoneVal = "+91 98765 43210";
        if (companyVal.isBlank()) companyVal = "Dock Tech Administration";
        if (uidVal.isBlank()) uidVal = "ADM-" + Math.abs(emailVal.hashCode() % 8999 + 1000);

        String initials = "AD";
        String nameTrim = nameVal.trim();
        if (!nameTrim.isEmpty()) {
            String[] parts = nameTrim.split("\\s+");
            String a = parts[0].substring(0, 1);
            String b = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
            initials = (a + b).toUpperCase();
        }

        com.projectui.view.login.DashboardLayoutHelper.showCustomPopup(
                owner, nameVal, emailVal, phoneVal, roleVal, companyVal, uidVal, statusVal, initials
        );
    }

    private HBox header(Label title) {
        HBox header = new HBox(14);
        header.setPadding(new Insets(12, 18, 12, 18));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:white; -fx-border-color:transparent transparent #e8edf3 transparent;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label notification = new Label("🔔");
        notification.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16;");

        String date = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-text-fill:#6b7280; -fx-font-size:13;");

        // live time label (updates every second)
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm:ss a");
        Label timeLabel = new Label(java.time.LocalTime.now().format(timeFmt));
        timeLabel.setStyle("-fx-text-fill:#6b7280; -fx-font-size:13; -fx-padding:0 6 0 6;");

        final Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            try {
                timeLabel.setText(java.time.LocalTime.now().format(timeFmt));
            } catch (Exception ex) {
                // ignore
            }
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        // stop clock when header is detached from window
        header.sceneProperty().addListener((obsS, oldS, newS) -> {
            if (newS == null) {
                clock.stop();
            } else {
                newS.windowProperty().addListener((obsW, oldW, newW) -> {
                    if (newW == null)
                        clock.stop();
                    else
                        newW.setOnHidden(e -> clock.stop());
                });
            }
        });

        Label system = new Label("● System Operational");
        system.setStyle(
                "-fx-text-fill:#047857; -fx-background-color:#ecfdf5; -fx-background-radius:8; -fx-padding:6 12; -fx-font-size:13; -fx-font-weight:bold;");

        // Circular profile avatar button showing initials
        AdminDataStore store = AdminDataStore.getInstance();
        String adminName = store.adminName == null ? "" : store.adminName.trim();
        String initialsLocal = "DT";
        if (!adminName.isBlank()) {
            String[] parts = adminName.split("\\s+");
            String a = parts[0].substring(0, 1);
            String b = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
            initialsLocal = (a + b).toUpperCase();
        }
        final String initials = initialsLocal;

        Button profile = new Button(initials);
        profile.setStyle("-fx-background-color:" + BLUE
                + "; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-background-radius:999; -fx-min-width:38; -fx-min-height:38; -fx-pref-width:38; -fx-pref-height:38; -fx-cursor:hand;");

        profile.setOnAction(evt -> {
            // build a compact popup dropdown aligned below the avatar, right-aligned
            Popup popup = new Popup();
            popup.setAutoHide(true);

            VBox box = new VBox(8);
            box.setStyle(
                    "-fx-background-color:white; -fx-padding:14; -fx-background-radius:10; -fx-effect: dropshadow(gaussian, rgba(14,30,70,0.12), 8, 0.5, 0, 3);");

            // header with circular avatar and labels
            Label bigAvatar = new Label(initials);
            bigAvatar.setStyle("-fx-background-color:" + BLUE
                    + "; -fx-text-fill:white; -fx-font-size:18; -fx-font-weight:bold; -fx-alignment:center; -fx-min-width:48; -fx-min-height:48; -fx-background-radius:999;");
            Label nameLbl = new Label(adminName.isBlank() ? "Dock Tech Admin" : adminName);
            nameLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:15; -fx-font-weight:bold;");
            Label roleLbl = new Label("Administrator");
            roleLbl.setStyle("-fx-text-fill:#6b7280; -fx-font-size:13;");

            HBox headerBox = new HBox(10, bigAvatar, new VBox(2, nameLbl, roleLbl));
            headerBox.setAlignment(Pos.CENTER_LEFT);

            // menu options
            Button viewProfile = new Button("👤  View Profile");
            viewProfile.setStyle(
                    "-fx-background-color:transparent; -fx-text-fill:#0f172a; -fx-font-size:14; -fx-alignment:center-left; -fx-padding:8 6; -fx-cursor:hand;");
            viewProfile.setOnAction(ae -> {
                popup.hide();
                Window owner = profile.getScene() == null ? null : profile.getScene().getWindow();
                showCustomPopup(owner, "Profile");
            });

            Button changePass = new Button("🔒  Change Password");
            changePass.setStyle(
                    "-fx-background-color:transparent; -fx-text-fill:#0f172a; -fx-font-size:14; -fx-alignment:center-left; -fx-padding:8 6; -fx-cursor:hand;");
            changePass.setOnAction(ae -> {
                popup.hide();
                switchPage("Settings", title);
            });

            Button settingsBtn = new Button("⚙  Settings");
            settingsBtn.setStyle(
                    "-fx-background-color:transparent; -fx-text-fill:#0f172a; -fx-font-size:14; -fx-alignment:center-left; -fx-padding:8 6; -fx-cursor:hand;");
            settingsBtn.setOnAction(ae -> {
                popup.hide();
                switchPage("Settings", title);
            });

            Separator sep = new Separator();

            Button logoutBtn = new Button("🚪  Logout");
            logoutBtn.setStyle(
                    "-fx-background-color:transparent; -fx-text-fill:#ef4444; -fx-font-size:14; -fx-alignment:center-left; -fx-padding:8 6; -fx-cursor:hand; -fx-font-weight:bold;");
            logoutBtn.setOnAction(ae -> {
                popup.hide();
                showLogoutConfirmation();
            });

            box.getChildren().addAll(headerBox, viewProfile, changePass, settingsBtn, sep, logoutBtn);

            popup.getContent().add(box);

            // position popup below avatar, right-aligned
            Bounds b = profile.localToScreen(profile.getBoundsInLocal());
            double popupWidth = 220;
            double x = b.getMaxX() - popupWidth;
            double y = b.getMaxY() + 6;
            popup.show(profile.getScene().getWindow(), x, y);
        });

        header.getChildren().addAll(title, spacer, notification, dateLabel, timeLabel, system, profile);
        return header;
    }

    private VBox sidebar(Label headerTitle) {
        VBox nav = new VBox(10);
        nav.setPrefWidth(215);
        nav.setPadding(new Insets(20, 12, 18, 12));
        nav.setStyle("-fx-background-color:#051029;");

        Label brand = new Label("DOCK TECH");
        brand.setStyle("-fx-text-fill:white; -fx-font-size:18; -fx-font-weight:bold;");
        Label small = new Label("SMART PORT OPERATIONS");
        small.setStyle("-fx-text-fill:#9aaac0; -fx-font-size:10; -fx-font-weight:bold;");
        Label logoIcon = new Label("⚓");
        logoIcon.setStyle("-fx-text-fill:white; -fx-font-size:20; -fx-font-weight:bold; -fx-padding:0 4 0 0;");
        VBox textBox = new VBox(2, brand, small);
        HBox brandBox = new HBox(8, logoIcon, textBox);
        brandBox.setPadding(new Insets(0, 0, 22, 6));

        Button dashboard = navButton("Dashboard");
        Button ports = navButton("Port Management");
        Button users = navButton("User Management");
        Button reports = navButton("Reports & Analytics");
        Button settings = navButton("Settings");
        navButtons.clear();
        navButtons.put("Dashboard", dashboard);
        navButtons.put("Port Management", ports);
        navButtons.put("User Management", users);
        navButtons.put("Reports & Analytics", reports);
        navButtons.put("Settings", settings);

        dashboard.setOnAction(event -> switchPage("Dashboard", headerTitle));
        ports.setOnAction(event -> switchPage("Port Management", headerTitle));
        users.setOnAction(event -> switchPage("User Management", headerTitle));
        reports.setOnAction(event -> switchPage("Reports & Analytics", headerTitle));
        settings.setOnAction(event -> switchPage("Settings", headerTitle));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        nav.getChildren().addAll(brandBox, dashboard, ports, users, reports, settings, spacer);
        return nav;
    }

    private Button navButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(navStyle(false, false));
        button.setOnMouseEntered(event -> button.setStyle(navStyle(true)));
        button.setOnMouseExited(event -> button.setStyle(navStyle(false, text.equals(currentPage))));
        return button;
    }

    private String navStyle(boolean active) {
        return navStyle(active, active);
    }

    private String navStyle(boolean hover, boolean selected) {
        return "-fx-background-color:" + (hover || selected ? BLUE : "transparent") + ";"
                + "-fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold;"
                + "-fx-padding:12 16; -fx-background-radius:8; -fx-cursor:hand;";
    }

    private void switchPage(String pageName, Label headerTitle) {
        if (headerTitle != null) {
            headerTitle.setText(pageName);
        } else if (headerTitleLabel != null) {
            headerTitleLabel.setText(pageName);
        }
        showPage(pageName);
    }

    private void showPage(String pageName) {
        currentPage = pageName == null || pageName.isBlank() ? "Dashboard" : pageName.trim();
        if (headerTitleLabel != null) {
            headerTitleLabel.setText(currentPage);
        }
        refreshActiveNav();

        if ("Port Management".equalsIgnoreCase(currentPage)) {
            center.setCenter(new PortManagementPage().getRoot());
        } else if ("User Management".equalsIgnoreCase(currentPage)) {
            center.setCenter(new UserManagementPage().getRoot());
        } else if ("Reports & Analytics".equalsIgnoreCase(currentPage) || "Reports".equalsIgnoreCase(currentPage)) {
            center.setCenter(new ReportsPage().getRoot());
        } else if ("Settings".equalsIgnoreCase(currentPage)) {
            center.setCenter(new SettingsPage(
                    () -> switchPage("User Management", headerTitleLabel),
                    () -> switchPage("Port Management", headerTitleLabel)
            ).getRoot());
        } else {
            center.setCenter(new DashboardPage().getRoot(
                    () -> switchPage("Port Management", headerTitleLabel),
                    () -> switchPage("User Management", headerTitleLabel),
                    () -> switchPage("Port Management", headerTitleLabel),
                    () -> switchPage("Reports & Analytics", headerTitleLabel)
            ));
        }
    }

    private void refreshActiveNav() {
        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            entry.getValue().setStyle(navStyle(false, entry.getKey().equals(currentPage)));
        }
    }

    private void showLogoutConfirmation() {
        VBox card = panelCard("Logout from Dock Tech?", "Are you sure you want to logout?");
        card.setMaxWidth(420);

        Button cancel = secondaryButton("Cancel");
        Button logout = dangerButton("Logout");
        cancel.setPrefWidth(130);
        logout.setPrefWidth(130);

        cancel.setOnAction(event -> showPage(currentPage));
        logout.setOnAction(event -> {
            // clear in-memory session/state
            AdminDataStore.getInstance().clearSession();
            if (logoutAction != null) {
                logoutAction.run();
            }
        });

        HBox actions = new HBox(12, cancel, logout);
        actions.setAlignment(Pos.CENTER_RIGHT);
        card.getChildren().add(actions);

        VBox wrapper = new VBox(card);
        wrapper.setAlignment(Pos.CENTER);
        center.setCenter(wrapper);
    }
}
