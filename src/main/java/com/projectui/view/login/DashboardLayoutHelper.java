package com.projectui.view.login;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DashboardLayoutHelper {

    private static final String BLUE = "#0b63ff";
    private static final String TEXT = "#111827";

    public static HBox createHeader(Stage stage, String pageTitle, String breadcrumb, String roleName, Runnable logoutAction, Button profileBtn) {
        HBox header = new HBox(14);
        header.setPadding(new Insets(12, 18, 12, 18));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:white; -fx-border-color:transparent transparent #e8edf3 transparent;");

        VBox pageInfo = new VBox(3);
        Label title = new Label(pageTitle);
        title.setStyle("-fx-text-fill:#111827; -fx-font-size:17; -fx-font-weight:bold;");
        Label bc = new Label(breadcrumb);
        bc.setStyle("-fx-text-fill:#8a93a1; -fx-font-size:10;");
        pageInfo.getChildren().addAll(title, bc);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label notification = new Label("🔔");
        notification.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16;");

        String date = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-text-fill:#6b7280; -fx-font-size:13;");

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm:ss a");
        Label timeLabel = new Label(java.time.LocalTime.now().format(timeFmt));
        timeLabel.setStyle("-fx-text-fill:#6b7280; -fx-font-size:13; -fx-padding:0 6 0 6;");

        final Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            try {
                timeLabel.setText(java.time.LocalTime.now().format(timeFmt));
            } catch (Exception ignored) {}
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        header.sceneProperty().addListener((obsS, oldS, newS) -> {
            if (newS == null) {
                clock.stop();
            } else {
                newS.windowProperty().addListener((obsW, oldW, newW) -> {
                    if (newW == null) clock.stop();
                    else newW.setOnHidden(e -> clock.stop());
                });
            }
        });

        Label system = new Label("● System Operational");
        system.setStyle("-fx-text-fill:#047857; -fx-background-color:#ecfdf5; -fx-background-radius:8; -fx-padding:6 12; -fx-font-size:13; -fx-font-weight:bold;");

        // Retrieve dynamic user profile from session / auth
        String nameVal = "";
        String emailVal = "";
        String phoneVal = "";
        String companyVal = "";
        String displayRole = roleName;
        String uidVal = "";
        String statusVal = "Active";

        if (com.projectui.view.login.LoginPage.loggedInUser != null) {
            Map<String, Object> u = com.projectui.view.login.LoginPage.loggedInUser;
            if (u.get("name") != null) nameVal = u.get("name").toString();
            else if (u.get("fullName") != null) nameVal = u.get("fullName").toString();
            else if (u.get("displayName") != null) nameVal = u.get("displayName").toString();

            if (u.get("email") != null) emailVal = u.get("email").toString();

            if (u.get("role") != null) displayRole = u.get("role").toString();

            if (u.get("phone") != null) phoneVal = u.get("phone").toString();
            else if (u.get("mobile") != null) phoneVal = u.get("mobile").toString();
            else if (u.get("phoneNumber") != null) phoneVal = u.get("phoneNumber").toString();

            if (u.get("company") != null) companyVal = u.get("company").toString();
            else if (u.get("organization") != null) companyVal = u.get("organization").toString();
            else if (u.get("department") != null) companyVal = u.get("department").toString();

            if (u.get("uid") != null) uidVal = u.get("uid").toString();
            else if (u.get("userId") != null) uidVal = u.get("userId").toString();
            else if (u.get("id") != null) uidVal = u.get("id").toString();

            if (u.get("status") != null) statusVal = u.get("status").toString();
        }

        // Fallbacks ONLY if fields remain unpopulated when loggedInUser is absent
        if (nameVal.isBlank()) {
            if ("Admin".equalsIgnoreCase(roleName) || "Administrator".equalsIgnoreCase(roleName) || "Port Authority".equalsIgnoreCase(roleName)) {
                com.projectui.view.admin.AdminDataStore store = com.projectui.view.admin.AdminDataStore.getInstance();
                if (store.adminName != null && !store.adminName.isBlank()) nameVal = store.adminName;
                if (store.adminEmail != null && !store.adminEmail.isBlank()) emailVal = store.adminEmail;
                displayRole = "Administrator";
            } else if ("Importer".equalsIgnoreCase(roleName)) {
                nameVal = "Harsh Patel";
                emailVal = "harsh@gmail.com";
                displayRole = "Importer";
                phoneVal = "9876543210";
                companyVal = "Reliance";
            } else {
                nameVal = "Pravin Chavan";
                emailVal = "pravin@gmail.com";
                phoneVal = "1234567896";
                companyVal = "Nayara";
            }
        }
        if (emailVal.isBlank()) emailVal = "user@docktech.com";
        if (phoneVal.isBlank()) phoneVal = "+91 98765 43210";
        if (companyVal.isBlank()) companyVal = "Dock Tech Operations";
        if (uidVal.isBlank()) uidVal = "USR-" + Math.abs(emailVal.hashCode() % 8999 + 1000);
        if (statusVal.isBlank()) statusVal = "Active";

        String initials = "DT";
        String nameTrim = nameVal.trim();
        if (!nameTrim.isEmpty()) {
            String[] parts = nameTrim.split("\\s+");
            String a = parts[0].substring(0, 1);
            String b = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
            initials = (a + b).toUpperCase();
        }

        final String finalInitials = initials;
        final String finalName = nameVal;
        final String finalEmail = emailVal;
        final String finalPhone = phoneVal;
        final String finalCompany = companyVal;
        final String finalRole = displayRole;
        final String finalUid = uidVal;
        final String finalStatus = statusVal;
        final Runnable finalLogoutAction = logoutAction != null ? logoutAction : () -> {
            if (stage != null) {
                new com.projectui.view.login.LoginPage().show(stage);
            }
        };

        Button profile = profileBtn != null ? profileBtn : new Button(initials);
        profile.setText(initials);
        profile.setStyle("-fx-background-color:" + BLUE + "; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-background-radius:999; -fx-min-width:38; -fx-min-height:38; -fx-pref-width:38; -fx-pref-height:38; -fx-cursor:hand;");

        profile.setOnAction(evt -> {
            Popup popup = new Popup();
            popup.setAutoHide(true);

            VBox box = new VBox(8);
            box.setStyle("-fx-background-color:white; -fx-padding:14; -fx-background-radius:10; -fx-effect: dropshadow(gaussian, rgba(14,30,70,0.12), 8, 0.5, 0, 3);");

            Label bigAvatar = new Label(finalInitials);
            bigAvatar.setStyle("-fx-background-color:" + BLUE + "; -fx-text-fill:white; -fx-font-size:18; -fx-font-weight:bold; -fx-alignment:center; -fx-min-width:48; -fx-min-height:48; -fx-background-radius:999;");
            Label nameLbl = new Label(finalName);
            nameLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:15; -fx-font-weight:bold;");
            Label roleLbl = new Label(finalRole);
            roleLbl.setStyle("-fx-text-fill:#6b7280; -fx-font-size:13;");

            HBox headerBox = new HBox(10, bigAvatar, new VBox(2, nameLbl, roleLbl));
            headerBox.setAlignment(Pos.CENTER_LEFT);

            Button viewProfile = new Button("👤  View Profile");
            viewProfile.setStyle("-fx-background-color:transparent; -fx-text-fill:#0f172a; -fx-font-size:14; -fx-alignment:center-left; -fx-padding:8 6; -fx-cursor:hand;");
            viewProfile.setOnAction(ae -> {
                popup.hide();
                Window owner = profile.getScene() == null ? null : profile.getScene().getWindow();
                showCustomPopup(owner, finalName, finalEmail, finalPhone, finalRole, finalCompany, finalUid, finalStatus, finalInitials);
            });

            Separator sep = new Separator();

            Button logoutBtn = new Button("🚪  Logout");
            logoutBtn.setStyle("-fx-background-color:transparent; -fx-text-fill:#ef4444; -fx-font-size:14; -fx-alignment:center-left; -fx-padding:8 6; -fx-cursor:hand; -fx-font-weight:bold;");
            logoutBtn.setOnAction(ae -> {
                popup.hide();
                finalLogoutAction.run();
            });

            box.getChildren().addAll(headerBox, viewProfile, sep, logoutBtn);
            popup.getContent().add(box);

            Bounds b = profile.localToScreen(profile.getBoundsInLocal());
            double popupWidth = 220;
            double x = b.getMaxX() - popupWidth;
            double y = b.getMaxY() + 6;
            popup.show(profile.getScene().getWindow(), x, y);
        });

        header.getChildren().addAll(pageInfo, spacer, notification, dateLabel, timeLabel, system, profile);
        return header;
    }

    public static void showCustomPopup(Window owner, String nameVal, String emailVal, String phoneVal, String roleVal, String companyVal, String uidVal, String statusVal, String initialsVal) {
        Stage popup = new Stage();
        if (owner != null && owner instanceof Stage) {
            popup.initOwner(owner);
            popup.initModality(Modality.WINDOW_MODAL);
        } else {
            popup.initModality(Modality.APPLICATION_MODAL);
        }
        popup.setTitle("User Profile Overview");
        popup.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new Insets(24, 28, 24, 28));
        root.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 16px;");

        // Top Modal Header Bar
        HBox modalHeader = new HBox();
        modalHeader.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("User Profile Overview");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Button closeX = new Button("✕");
        closeX.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748B; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
        closeX.setOnAction(e -> popup.close());

        modalHeader.getChildren().addAll(titleLabel, topSpacer, closeX);

        // Profile Avatar & Main Info Header Card
        VBox profileBanner = new VBox(12);
        profileBanner.setAlignment(Pos.CENTER);
        profileBanner.setPadding(new Insets(20));
        profileBanner.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 14px; -fx-border-color: #E2E8F0; -fx-border-radius: 14px;");

        Label avatarCircle = new Label(initialsVal != null && !initialsVal.isBlank() ? initialsVal : "DT");
        avatarCircle.setStyle(
                "-fx-background-color: " + BLUE + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 24px;" +
                "-fx-font-weight: bold;" +
                "-fx-alignment: center;" +
                "-fx-min-width: 68px;" +
                "-fx-min-height: 68px;" +
                "-fx-pref-width: 68px;" +
                "-fx-pref-height: 68px;" +
                "-fx-background-radius: 999px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(11,99,255,0.25), 8, 0, 0, 3);"
        );

        Label nameDisplay = new Label(nameVal);
        nameDisplay.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        HBox badgesBox = new HBox(8);
        badgesBox.setAlignment(Pos.CENTER);

        Label roleBadge = new Label(roleVal);
        roleBadge.setStyle("-fx-background-color: #E0F2FE; -fx-text-fill: #0284C7; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4px 12px; -fx-background-radius: 12px;");

        Label statusBadge = new Label("● " + (statusVal != null ? statusVal : "Active"));
        statusBadge.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #15803D; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4px 12px; -fx-background-radius: 12px;");

        badgesBox.getChildren().addAll(roleBadge, statusBadge);
        profileBanner.getChildren().addAll(avatarCircle, nameDisplay, badgesBox);

        // Detailed Information Grid
        VBox infoGrid = new VBox(10);
        infoGrid.getChildren().addAll(
                createProfileDetailRow("👤  Full Name", nameVal),
                createProfileDetailRow("✉️  Email Address", emailVal),
                createProfileDetailRow("📱  Phone Number", phoneVal),
                createProfileDetailRow("🏢  Company / Org", companyVal),
                createProfileDetailRow("🛡️  User Role", roleVal),
                createProfileDetailRow("🆔  User ID (UID)", uidVal),
                createProfileDetailRow("🟢  Account Status", statusVal)
        );

        // Footer Action Button
        Button closeBtn = new Button("Close Profile");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setPrefHeight(42);
        closeBtn.setStyle(
                "-fx-background-color: " + BLUE + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 9px;" +
                "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> popup.close());

        root.getChildren().addAll(modalHeader, profileBanner, infoGrid, closeBtn);

        Scene scene = new Scene(root, 440, 600);
        popup.setScene(scene);

        if (owner != null) {
            popup.setX(owner.getX() + Math.max(0, (owner.getWidth() - 440) / 2));
            popup.setY(owner.getY() + Math.max(0, (owner.getHeight() - 600) / 2));
        }

        popup.show();
    }

    private static HBox createProfileDetailRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 12, 9, 12));
        row.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8px; -fx-border-color: #F1F5F9; -fx-border-radius: 8px;");

        Label l = new Label(label);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label v = new Label(value != null && !value.isBlank() ? value : "N/A");
        v.setStyle("-fx-font-size: 13px; -fx-text-fill: #0F172A; -fx-font-weight: bold;");

        row.getChildren().addAll(l, spacer, v);
        return row;
    }

    public static VBox createSidebar(Stage stage, String roleName, String currentPage, Map<String, Button> menuButtons) {
        VBox nav = new VBox(10);
        nav.setPrefWidth(215);
        nav.setMinWidth(215);
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

        nav.getChildren().add(brandBox);

        for (Map.Entry<String, Button> entry : menuButtons.entrySet()) {
            String name = entry.getKey();
            Button btn = entry.getValue();
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setStyle(navStyle(false, name.equalsIgnoreCase(currentPage)));
            btn.setOnMouseEntered(event -> btn.setStyle(navStyle(true, btn.getStyle().contains("#0b63ff"))));
            btn.setOnMouseExited(event -> btn.setStyle(navStyle(false, name.equalsIgnoreCase(currentPage))));
            nav.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        nav.getChildren().add(spacer);

        return nav;
    }

    private static String navStyle(boolean hover, boolean selected) {
        return "-fx-background-color:" + (hover || selected ? BLUE : "transparent") + ";"
                + "-fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold;"
                + "-fx-padding:12 16; -fx-background-radius:8; -fx-cursor:hand;";
    }
}
