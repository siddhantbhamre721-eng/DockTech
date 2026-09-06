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
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;
import java.time.format.DateTimeFormatter;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class DashboardLayoutHelper {

    private static final String BLUE = "#0b63ff";
    private static final String TEXT = "#111827";

    public static HBox createHeader(Stage stage, String pageTitle, String breadcrumb, String roleName, Runnable logoutAction, Button profileBtn) {
        Label title = new Label(pageTitle);
        title.setStyle("-fx-text-fill:#111827; -fx-font-size:17; -fx-font-weight:bold;");
        Label bc = new Label(breadcrumb);
        bc.setStyle("-fx-text-fill:#8a93a1; -fx-font-size:10;");
        return createHeader(stage, title, bc, roleName, logoutAction, profileBtn);
    }

    public static HBox createHeader(Stage stage, Label title, Label bc, String roleName, Runnable logoutAction, Button profileBtn) {
        HBox header = new HBox(14);
        header.setPadding(new Insets(12, 18, 12, 18));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:white; -fx-border-color:transparent transparent #e8edf3 transparent;");

        VBox pageInfo = new VBox(3);
        if (title.getStyle() == null || title.getStyle().isBlank()) {
            title.setStyle("-fx-text-fill:#111827; -fx-font-size:17; -fx-font-weight:bold;");
        }
        if (bc.getStyle() == null || bc.getStyle().isBlank()) {
            bc.setStyle("-fx-text-fill:#8a93a1; -fx-font-size:10;");
        }
        pageInfo.getChildren().addAll(title, bc);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Node notification = createNotificationBellNode(stage, roleName);

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
            if ("Admin".equalsIgnoreCase(roleName) || "Administrator".equalsIgnoreCase(roleName)) {
                com.projectui.view.admin.AdminDataStore store = com.projectui.view.admin.AdminDataStore.getInstance();
                if (store.adminName != null && !store.adminName.isBlank()) nameVal = store.adminName;
                if (store.adminEmail != null && !store.adminEmail.isBlank()) emailVal = store.adminEmail;
                displayRole = "Administrator";
            } else {
                nameVal = roleName != null && !roleName.isBlank() ? roleName : "User";
            }
        }
        if (emailVal.isBlank()) emailVal = "";
        if (phoneVal.isBlank()) phoneVal = "";
        if (companyVal.isBlank()) companyVal = "";
        if (uidVal.isBlank()) uidVal = "";
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

        Button profile = profileBtn != null ? profileBtn : new Button();
        Node avatarGraphic = com.projectui.service.UserProfileImageManager.createAvatarNode(38, finalInitials, finalEmail);
        profile.setGraphic(avatarGraphic);
        profile.setText("");
        profile.setStyle("-fx-background-color:transparent; -fx-padding:0; -fx-cursor:hand; -fx-min-width:38; -fx-min-height:38; -fx-pref-width:38; -fx-pref-height:38;");

        // Dynamic reactive avatar update listener
        com.projectui.service.UserProfileImageManager.addAvatarChangeListener(() -> {
            Platform.runLater(() -> {
                profile.setGraphic(com.projectui.service.UserProfileImageManager.createAvatarNode(38, finalInitials, finalEmail));
            });
        });

        profile.setOnAction(evt -> {
            Popup popup = new Popup();
            popup.setAutoHide(true);

            VBox box = new VBox(8);
            box.setStyle("-fx-background-color:white; -fx-padding:14; -fx-background-radius:10; -fx-effect: dropshadow(gaussian, rgba(14,30,70,0.12), 8, 0.5, 0, 3);");

            Node bigAvatarNode = com.projectui.service.UserProfileImageManager.createAvatarNode(48, finalInitials, finalEmail);
            StackPane bigAvatar = new StackPane(bigAvatarNode);
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
                Window ownerWin = profile.getScene() == null ? null : profile.getScene().getWindow();
                LogoutModal.show(ownerWin, finalLogoutAction);
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
        VBox profileBanner = new VBox(10);
        profileBanner.setAlignment(Pos.CENTER);
        profileBanner.setPadding(new Insets(18));
        profileBanner.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 14px; -fx-border-color: #E2E8F0; -fx-border-radius: 14px;");

        StackPane avatarContainer = new StackPane(com.projectui.service.UserProfileImageManager.createAvatarNode(72, initialsVal, emailVal));

        Button changePhotoBtn = new Button("📷  Change Profile Photo");
        changePhotoBtn.setStyle(
                "-fx-background-color: #EFF6FF;" +
                "-fx-text-fill: " + BLUE + ";" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 5 12;" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: #BFDBFE;" +
                "-fx-border-radius: 6;" +
                "-fx-cursor: hand;"
        );

        Label uploadStatusLbl = new Label("");
        uploadStatusLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #16a34a; -fx-font-weight: bold;");
        uploadStatusLbl.setVisible(false);
        uploadStatusLbl.managedProperty().bind(uploadStatusLbl.visibleProperty());

        changePhotoBtn.setOnAction(e -> {
            changePhotoBtn.setDisable(true);
            changePhotoBtn.setText("⏳ Uploading...");
            com.projectui.service.UserProfileImageManager.chooseAndUploadProfilePicture(
                    popup,
                    emailVal,
                    uidVal,
                    () -> {
                        changePhotoBtn.setDisable(false);
                        changePhotoBtn.setText("📷  Change Profile Photo");
                        avatarContainer.getChildren().setAll(com.projectui.service.UserProfileImageManager.createAvatarNode(72, initialsVal, emailVal));
                        uploadStatusLbl.setText("✓ Profile photo updated successfully!");
                        uploadStatusLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #16a34a; -fx-font-weight: bold;");
                        uploadStatusLbl.setVisible(true);
                    },
                    err -> {
                        changePhotoBtn.setDisable(false);
                        changePhotoBtn.setText("📷  Change Profile Photo");
                        uploadStatusLbl.setText("⚠️ " + err);
                        uploadStatusLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
                        uploadStatusLbl.setVisible(true);
                    }
            );
        });

        VBox avatarWithAction = new VBox(6, avatarContainer, changePhotoBtn, uploadStatusLbl);
        avatarWithAction.setAlignment(Pos.CENTER);

        Label nameDisplay = new Label(nameVal);
        nameDisplay.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        HBox badgesBox = new HBox(8);
        badgesBox.setAlignment(Pos.CENTER);

        Label roleBadge = new Label(roleVal);
        roleBadge.setStyle("-fx-background-color: #E0F2FE; -fx-text-fill: #0284C7; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4px 12px; -fx-background-radius: 12px;");

        Label statusBadge = new Label("● " + (statusVal != null ? statusVal : "Active"));
        statusBadge.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #15803D; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4px 12px; -fx-background-radius: 12px;");

        badgesBox.getChildren().addAll(roleBadge, statusBadge);
        profileBanner.getChildren().addAll(avatarWithAction, nameDisplay, badgesBox);

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
        LoginPage.applyInvisibleScrollbar(scene);
        LoginPage.applyInvisibleScrollbar(popup);
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

    public static Node createNotificationBellNode(Stage stage, String roleName) {
        if (roleName == null || "CreateAccount".equalsIgnoreCase(roleName) || "Public".equalsIgnoreCase(roleName)) {
            Region empty = new Region();
            empty.setVisible(false);
            empty.setManaged(false);
            return empty;
        }

        Button bellBtn = new Button("🔔");
        bellBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#0f172a; -fx-font-size:14; -fx-padding:6 10; -fx-background-radius:8; -fx-cursor:hand;");

        Label badge = new Label();
        badge.setVisible(false);
        badge.setManaged(false);

        HBox wrapper = new HBox(4, bellBtn, badge);
        wrapper.setAlignment(Pos.CENTER_LEFT);

        Runnable updateBadge = () -> {
            int emCount = com.projectui.service.SosEmergencyService.getUnreadEmergencyCountForUser(roleName, com.projectui.view.login.LoginPage.loggedInUser);
            if (emCount > 0) {
                badge.setText("🔴 " + emCount + " SOS");
                badge.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#dc2626; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 7; -fx-background-radius:999; -fx-border-color:#ef4444; -fx-border-radius:999;");
                badge.setVisible(true);
                badge.setManaged(true);
                bellBtn.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#dc2626; -fx-font-size:14; -fx-padding:6 10; -fx-background-radius:8; -fx-cursor:hand; -fx-border-color:#fca5a5; -fx-border-radius:8;");
            } else {
                badge.setVisible(false);
                badge.setManaged(false);
                bellBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#0f172a; -fx-font-size:14; -fx-padding:6 10; -fx-background-radius:8; -fx-cursor:hand;");
            }
        };

        com.projectui.service.SosEmergencyService.addChangeListener(updateBadge);
        updateBadge.run();

        bellBtn.setOnAction(e -> {
            showNotificationPopup(bellBtn, stage, roleName, updateBadge);
        });

        return wrapper;
    }

    private static void showNotificationPopup(Button anchor, Stage stage, String roleName, Runnable refreshBadgeCallback) {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox contentBox = new VBox(10);
        contentBox.setStyle("-fx-background-color:white; -fx-padding:16; -fx-background-radius:12; -fx-effect: dropshadow(gaussian, rgba(14,30,70,0.18), 16, 0.5, 0, 4); -fx-border-color:#e2e8f0; -fx-border-radius:12;");
        contentBox.setPrefWidth(420);
        contentBox.setMaxWidth(440);

        // Header
        HBox topBar = new HBox(8);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label popTitle = new Label("🔔 Notifications & Alerts");
        popTitle.setStyle("-fx-font-size:15; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button markAllBtn = new Button("Mark All Read");
        markAllBtn.setStyle("-fx-background-color:transparent; -fx-text-fill:#2563eb; -fx-font-size:11; -fx-cursor:hand; -fx-font-weight:bold; -fx-padding:2 4;");

        topBar.getChildren().addAll(popTitle, sp, markAllBtn);
        contentBox.getChildren().addAll(topBar, new Separator());

        VBox listContainer = new VBox(8);

        // 1. Emergency SOS Notifications (Filtered strictly for current role & user)
        List<com.projectui.service.SosEmergencyService.SosAlert> sosList = com.projectui.service.SosEmergencyService.getAlertsForRoleAndUser(roleName, com.projectui.view.login.LoginPage.loggedInUser);
        for (com.projectui.service.SosEmergencyService.SosAlert a : sosList) {
            boolean isAck = "ACKNOWLEDGED".equalsIgnoreCase(a.getStatus());
            VBox sosCard = new VBox(6);
            sosCard.setPadding(new Insets(10, 12, 10, 12));
            sosCard.setStyle(isAck
                    ? "-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;"
                    : "-fx-background-color:#fef2f2; -fx-border-color:#fca5a5; -fx-border-radius:8; -fx-background-radius:8; -fx-effect: dropshadow(gaussian, rgba(220,38,38,0.12), 6, 0.3, 0, 2);");

            HBox cardHeader = new HBox(6);
            cardHeader.setAlignment(Pos.CENTER_LEFT);

            Label emBadge = new Label(isAck ? "✓ SOS ACKNOWLEDGED" : "🔴 EMERGENCY SOS RECEIVED");
            emBadge.setStyle(isAck
                    ? "-fx-background-color:#dcfce7; -fx-text-fill:#16a34a; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;"
                    : "-fx-background-color:#fee2e2; -fx-text-fill:#dc2626; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;");

            Region spH = new Region();
            HBox.setHgrow(spH, Priority.ALWAYS);

            Label timeLbl = new Label(a.getTimestamp());
            timeLbl.setStyle("-fx-font-size:10; -fx-text-fill:#64748b;");

            cardHeader.getChildren().addAll(emBadge, spH, timeLbl);

            Label vName = new Label("🚢 " + a.getVesselName().toUpperCase());
            vName.setStyle("-fx-font-size:13; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

            Label line1 = new Label("Captain: " + a.getCaptainName() + " | Incident ID: " + a.getIncidentId());
            line1.setStyle("-fx-font-size:11; -fx-text-fill:#475569;");

            Label line2 = new Label("Severity: " + a.getSeverity() + " — " + a.getCategory());
            line2.setStyle("-fx-font-size:11; -fx-font-weight:bold; -fx-text-fill:" + (isAck ? "#16a34a" : "#dc2626") + ";");

            Label line3 = new Label("Location: " + a.getLocation() + " (Speed: " + a.getSpeed() + ", Heading: " + a.getHeading() + ")");
            line3.setStyle("-fx-font-size:11; -fx-text-fill:#334155;");

            Label line4 = new Label("Route: " + a.getPort() + " ➔ " + a.getDestination());
            line4.setStyle("-fx-font-size:11; -fx-text-fill:#64748b;");

            Button actionBtn = new Button(isAck ? "🔍 View Incident Details" : "⚡ View SOS & Acknowledge");
            actionBtn.setStyle(isAck
                    ? "-fx-background-color:#e2e8f0; -fx-text-fill:#334155; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:5 12; -fx-background-radius:5; -fx-cursor:hand;"
                    : "-fx-background-color:#dc2626; -fx-text-fill:white; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:5 12; -fx-background-radius:5; -fx-cursor:hand;");
            actionBtn.setMaxWidth(Double.MAX_VALUE);
            actionBtn.setOnAction(ev -> {
                popup.hide();
                Window ownerWin = anchor.getScene() != null ? anchor.getScene().getWindow() : null;
                com.projectui.service.SosEmergencyService.showSosDetailsDialog(ownerWin, a, roleName + " Operations", () -> {
                    if (refreshBadgeCallback != null) refreshBadgeCallback.run();
                });
            });

            sosCard.getChildren().addAll(cardHeader, vName, line1, line2, line3, line4, actionBtn);
            listContainer.getChildren().add(sosCard);
        }

        // 2. Standard Notifications for current user / Port Authority
        String currentUid = "";
        String currentEmail = "";
        if (com.projectui.view.login.LoginPage.loggedInUser != null) {
            Object uIdObj = com.projectui.view.login.LoginPage.loggedInUser.get("uid");
            if (uIdObj != null) currentUid = uIdObj.toString();
            Object emailObj = com.projectui.view.login.LoginPage.loggedInUser.get("email");
            if (emailObj != null) currentEmail = emailObj.toString().trim().toLowerCase();
        }

        com.projectui.dao.NotificationDao notifDao = new com.projectui.dao.NotificationDao();
        List<com.projectui.model.Notification> normalList = new ArrayList<>();
        if (!currentUid.isBlank()) {
            normalList.addAll(notifDao.getNotificationsForUser(currentUid));
        }
        if (normalList.isEmpty() && !currentEmail.isBlank()) {
            normalList.addAll(notifDao.getNotificationsForUser(currentEmail));
        }
        for (com.projectui.model.Notification n : normalList) {
            if ("EMERGENCY_SOS".equalsIgnoreCase(n.getType())) continue; // already rendered above

            VBox notifRow = new VBox(3);
            notifRow.setPadding(new Insets(8, 10, 8, 10));
            notifRow.setStyle(n.isRead()
                    ? "-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:6;"
                    : "-fx-background-color:#eff6ff; -fx-border-color:#bfdbfe; -fx-border-radius:6;");

            HBox rTop = new HBox(6);
            Label nTitle = new Label(n.getTitle() != null ? n.getTitle() : "Notification");
            nTitle.setStyle("-fx-font-size:12; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
            Region spR = new Region();
            HBox.setHgrow(spR, Priority.ALWAYS);
            Label nTime = new Label(n.getTimestamp() != null ? n.getTimestamp() : "Recently");
            nTime.setStyle("-fx-font-size:10; -fx-text-fill:#64748b;");
            rTop.getChildren().addAll(nTitle, spR, nTime);

            Label nMsg = new Label(n.getMessage() != null ? n.getMessage() : "");
            nMsg.setStyle("-fx-font-size:11; -fx-text-fill:#334155; -fx-wrap-text:true;");

            notifRow.getChildren().addAll(rTop, nMsg);
            listContainer.getChildren().add(notifRow);
        }

        if (listContainer.getChildren().isEmpty()) {
            Label emptyLbl = new Label("No active notifications or emergency distress alerts.");
            emptyLbl.setStyle("-fx-text-fill:#64748b; -fx-font-size:12; -fx-padding:14; -fx-alignment:center;");
            listContainer.getChildren().add(emptyLbl);
        }

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(listContainer);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(380);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-color:transparent;");

        contentBox.getChildren().add(scroll);
        popup.getContent().add(contentBox);

        final String finalUid = currentUid;
        final String finalEmail = currentEmail;
        markAllBtn.setOnAction(e -> {
            if (!finalUid.isBlank()) {
                notifDao.markAllAsRead(finalUid);
            }
            if (!finalEmail.isBlank()) {
                notifDao.markAllAsRead(finalEmail);
            }
            popup.hide();
            if (refreshBadgeCallback != null) refreshBadgeCallback.run();
        });

        Bounds b = anchor.localToScreen(anchor.getBoundsInLocal());
        if (b != null) {
            double x = b.getMinX() - 360;
            double y = b.getMaxY() + 8;
            popup.show(anchor.getScene().getWindow(), x, y);
        }
    }

    private static String navStyle(boolean hover, boolean selected) {
        return "-fx-background-color:" + (hover || selected ? BLUE : "transparent") + ";"
                + "-fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold;"
                + "-fx-padding:12 16; -fx-background-radius:8; -fx-cursor:hand;";
    }
}
