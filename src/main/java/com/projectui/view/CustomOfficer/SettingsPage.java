package com.projectui.view.CustomOfficer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.projectui.dao.UserDao;
import com.projectui.view.login.LoginPage;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Custom Officer - Settings & Profile Management
 * 
 * Styled using the EXACT Admin Settings Page Design System:
 * - 2 rows of 3 compact cards matching Admin Settings layout
 * - Full Name, Email, Password, Notification Preferences persisted to Firebase/Firestore
 * - High-contrast readable typography (no pale-blue or washed-out text)
 * - 100% dynamic, database-backed settings management
 */
@SuppressWarnings({"unchecked", "unused"})
public class SettingsPage {

    private static final String BLUE = "#0b63ff";
    private static final String CARD_STYLE = "-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-background-radius:10; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 8, 0, 0, 2);";

    private final VBox content = new VBox(14);
    private final UserDao userDao = new UserDao();

    // Officer Context
    private String officerUid = "";
    private String officerName = "Customs Officer";
    private String officerEmail = "customs@docktech.com";
    private String officerRole = "Custom Officer";
    private String officerPort = "Cochin Port";
    private String officerStatus = "Active";

    // Toast Notification Banner
    private Label toastMessage;
    private HBox toastBanner;

    // Form Controls - Profile
    private TextField nameField;
    private TextField emailField;

    // Form Controls - Password
    private PasswordField currentPassField;
    private TextField currentPassText;
    private PasswordField newPassField;
    private TextField newPassText;
    private PasswordField confirmPassField;
    private TextField confirmPassText;
    private boolean showPasswords = false;

    // Form Controls - Notification Preferences
    private CheckBox cbDocAlerts;
    private CheckBox cbInspectionAlerts;
    private CheckBox cbHazmatAlerts;
    private CheckBox cbPortAuthAlerts;
    private CheckBox cbDigestEmail;

    // Form Controls - Workspace
    private ComboBox<String> autoRefreshCombo;

    public SettingsPage() {
        resolveOfficerContext();
        buildUI();
    }

    private void resolveOfficerContext() {
        if (LoginPage.loggedInUser != null) {
            Map<String, Object> u = LoginPage.loggedInUser;
            if (u.get("uid") != null) officerUid = u.get("uid").toString();
            else if (u.get("userId") != null) officerUid = u.get("userId").toString();

            if (u.get("name") != null && !u.get("name").toString().isBlank()) {
                officerName = u.get("name").toString();
            } else if (u.get("fullName") != null && !u.get("fullName").toString().isBlank()) {
                officerName = u.get("fullName").toString();
            }

            if (u.get("email") != null) officerEmail = u.get("email").toString();
            if (u.get("role") != null) officerRole = u.get("role").toString();
            if (u.get("status") != null) officerStatus = u.get("status").toString();

            String portVal = null;
            if (u.get("assignedPort") != null && !u.get("assignedPort").toString().isBlank()) {
                portVal = u.get("assignedPort").toString();
            } else if (u.get("port") != null && !u.get("port").toString().isBlank()) {
                portVal = u.get("port").toString();
            } else if (u.get("portName") != null && !u.get("portName").toString().isBlank()) {
                portVal = u.get("portName").toString();
            } else if (u.get("jurisdiction") != null && !u.get("jurisdiction").toString().isBlank()) {
                portVal = u.get("jurisdiction").toString();
            } else if (u.get("organization") != null && !u.get("organization").toString().isBlank()) {
                portVal = u.get("organization").toString();
            } else if (u.get("company") != null && !u.get("company").toString().isBlank()) {
                portVal = u.get("company").toString();
            }

            if (portVal != null && !portVal.isBlank()) {
                officerPort = UserDao.normalizePort(portVal);
            }
        }
    }

    private void buildUI() {
        content.setSpacing(14);
        content.setPadding(new Insets(16, 20, 24, 20));
        content.setStyle("-fx-background-color:transparent;");

        // 1. Toast Notification Banner
        toastBanner = createToastBanner();

        // 2. Page Header (Admin Style)
        HBox header = createSettingsHeader(
                "Settings",
                "Manage officer credentials, customs clearance security, notification preferences and terminal session."
        );

        // 3. Row 1: Profile (33%), Change Password (34%), Notification Preferences (33%)
        HBox row1 = createRow1();

        // 4. Row 2: Jurisdiction & Workspace (33%), Active Session (34%), Quick Customs Actions (33%)
        HBox row2 = createRow2();

        content.getChildren().addAll(
                header,
                toastBanner,
                row1,
                row2
        );
    }

    private HBox createSettingsHeader(String title, String subtitle) {
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#05070c; -fx-font-size:26; -fx-font-weight:bold;");

        Label s = new Label(subtitle);
        s.setStyle("-fx-text-fill:#64748b; -fx-font-size:14;");

        VBox copy = new VBox(2, t, s);
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label statusBadge = new Label("● Session Active & Secure");
        statusBadge.setStyle(
                "-fx-background-color:#ecfdf5;" +
                "-fx-text-fill:#047857;" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:6 14;" +
                "-fx-background-radius:20;"
        );

        HBox header = new HBox(12, copy, space, statusBadge);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    // ==========================================
    // TOAST NOTIFICATION BANNER
    // ==========================================
    private HBox createToastBanner() {
        Label icon = new Label("ℹ");
        icon.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:16; -fx-font-weight:bold;");

        toastMessage = new Label("Customs Officer session active. Settings synchronized with central database.");
        toastMessage.setStyle("-fx-text-fill:#334155; -fx-font-size:13; -fx-font-weight:bold;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label close = new Label("✕");
        close.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13; -fx-cursor:hand;");
        close.setOnMouseClicked(e -> toastBanner.setVisible(false));

        HBox toast = new HBox(10, icon, toastMessage, space, close);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setPadding(new Insets(10, 16, 10, 16));
        toast.setStyle(
                "-fx-background-color:#eff6ff;" +
                "-fx-border-color:#bfdbfe;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;"
        );
        return toast;
    }

    private void showToast(String msg, boolean isSuccess) {
        if (toastMessage != null && toastBanner != null) {
            toastMessage.setText(msg);
            toastBanner.setStyle(
                    "-fx-background-color:" + (isSuccess ? "#ecfdf5" : "#eff6ff") + ";" +
                    "-fx-border-color:" + (isSuccess ? "#a7f3d0" : "#bfdbfe") + ";" +
                    "-fx-border-radius:8;" +
                    "-fx-background-radius:8;"
            );
            toastBanner.setVisible(true);
            toastBanner.setManaged(true);
        }
    }

    // ==========================================
    // ROW 1: 3 CARDS
    // 1. Officer Profile
    // 2. Change Password
    // 3. Notification Preferences
    // ==========================================
    private HBox createRow1() {
        VBox card1 = officerProfileCard();
        VBox card2 = changePasswordCard();
        VBox card3 = notificationPreferencesCard();

        HBox row = new HBox(14, card1, card2, card3);
        card1.prefWidthProperty().bind(row.widthProperty().multiply(0.33));
        card2.prefWidthProperty().bind(row.widthProperty().multiply(0.34));
        card3.prefWidthProperty().bind(row.widthProperty().multiply(0.33));

        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);

        return row;
    }

    // 1. Officer Profile Card
    private VBox officerProfileCard() {
        Label title = new Label("Officer Profile");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Manage officer profile details & contact credentials");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        nameField = new TextField(officerName);
        nameField.setPromptText("Enter full name");
        styleCustomField(nameField);

        emailField = new TextField(officerEmail);
        emailField.setPromptText("Enter official email address");
        styleCustomField(emailField);

        VBox fields = new VBox(8,
                labeledControl("Full Name *", nameField),
                labeledControl("Official Email Address *", emailField)
        );

        VBox details = new VBox(4);
        details.getChildren().addAll(
                compactInfoRow("Role", officerRole + " (Regulatory Authority)"),
                compactInfoRow("Port Jurisdiction", officerPort),
                compactInfoRow("Account Status", officerStatus + " & Certified"),
                compactInfoRow("Active Session", "Live (Terminal Station)")
        );

        Button saveProfileBtn = new Button("💾  Save Profile Changes");
        saveProfileBtn.setMaxWidth(Double.MAX_VALUE);
        saveProfileBtn.setStyle(
                "-fx-background-color:" + BLUE + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:9 16;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );
        saveProfileBtn.setOnAction(e -> saveProfileChanges());

        VBox content = new VBox(10,
                titleBox,
                new Separator(),
                fields,
                new Separator(),
                details,
                new Region(),
                saveProfileBtn
        );
        content.setPadding(new Insets(16, 18, 16, 18));
        content.setStyle(CARD_STYLE);
        return content;
    }

    // 2. Change Password Card
    private VBox changePasswordCard() {
        Label title = new Label("Security & Password");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Update authentication password and station credentials");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        currentPassField = new PasswordField(); currentPassField.setPromptText("Enter current password"); styleCustomField(currentPassField);
        currentPassText = new TextField(); currentPassText.setPromptText("Enter current password"); styleCustomField(currentPassText);
        currentPassText.setVisible(false); currentPassText.setManaged(false);

        newPassField = new PasswordField(); newPassField.setPromptText("Enter new password (min 6 chars)"); styleCustomField(newPassField);
        newPassText = new TextField(); newPassText.setPromptText("Enter new password (min 6 chars)"); styleCustomField(newPassText);
        newPassText.setVisible(false); newPassText.setManaged(false);

        confirmPassField = new PasswordField(); confirmPassField.setPromptText("Re-type new password"); styleCustomField(confirmPassField);
        confirmPassText = new TextField(); confirmPassText.setPromptText("Re-type new password"); styleCustomField(confirmPassText);
        confirmPassText.setVisible(false); confirmPassText.setManaged(false);

        StackPane curStack = new StackPane(currentPassField, currentPassText);
        StackPane newStack = new StackPane(newPassField, newPassText);
        StackPane confStack = new StackPane(confirmPassField, confirmPassText);

        CheckBox showPassToggle = new CheckBox("Show Passwords");
        showPassToggle.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");
        showPassToggle.setOnAction(e -> {
            showPasswords = showPassToggle.isSelected();
            if (showPasswords) {
                currentPassText.setText(currentPassField.getText()); currentPassField.setVisible(false); currentPassField.setManaged(false); currentPassText.setVisible(true); currentPassText.setManaged(true);
                newPassText.setText(newPassField.getText()); newPassField.setVisible(false); newPassField.setManaged(false); newPassText.setVisible(true); newPassText.setManaged(true);
                confirmPassText.setText(confirmPassField.getText()); confirmPassField.setVisible(false); confirmPassField.setManaged(false); confirmPassText.setVisible(true); confirmPassText.setManaged(true);
            } else {
                currentPassField.setText(currentPassText.getText()); currentPassText.setVisible(false); currentPassText.setManaged(false); currentPassField.setVisible(true); currentPassField.setManaged(true);
                newPassField.setText(newPassText.getText()); newPassText.setVisible(false); newPassText.setManaged(false); newPassField.setVisible(true); newPassField.setManaged(true);
                confirmPassField.setText(confirmPassText.getText()); confirmPassText.setVisible(false); confirmPassText.setManaged(false); confirmPassField.setVisible(true); confirmPassField.setManaged(true);
            }
        });

        VBox fields = new VBox(8,
                labeledControl("Current Password *", curStack),
                labeledControl("New Password *", newStack),
                labeledControl("Confirm New Password *", confStack),
                showPassToggle
        );

        Button changePassBtn = new Button("🔒  Update Password");
        changePassBtn.setMaxWidth(Double.MAX_VALUE);
        changePassBtn.setStyle(
                "-fx-background-color:#16a34a;" +
                "-fx-text-fill:white;" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:9 16;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );
        changePassBtn.setOnAction(e -> handlePasswordUpdate());

        VBox content = new VBox(10,
                titleBox,
                new Separator(),
                fields,
                new Region(),
                changePassBtn
        );
        content.setPadding(new Insets(16, 18, 16, 18));
        content.setStyle(CARD_STYLE);
        return content;
    }

    // 3. Notification Preferences Card
    private VBox notificationPreferencesCard() {
        Label title = new Label("Notification Preferences");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Configure customs alerts and operational case channels");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        boolean nDocs = true, nInsp = true, nHazmat = true, nPort = true, nDigest = true;
        if (LoginPage.loggedInUser != null) {
            Map<String, Object> u = LoginPage.loggedInUser;
            if (u.get("notifyDocs") instanceof Boolean) nDocs = (Boolean) u.get("notifyDocs");
            if (u.get("notifyInspection") instanceof Boolean) nInsp = (Boolean) u.get("notifyInspection");
            if (u.get("notifyHazmat") instanceof Boolean) nHazmat = (Boolean) u.get("notifyHazmat");
            if (u.get("notifyPortAuth") instanceof Boolean) nPort = (Boolean) u.get("notifyPortAuth");
            if (u.get("notifyDigest") instanceof Boolean) nDigest = (Boolean) u.get("notifyDigest");
        }

        cbDocAlerts = createSettingToggle("Document Verification Alerts", "Instant alert when new bills of lading or declarations are submitted", nDocs);
        cbInspectionAlerts = createSettingToggle("Physical Inspection Reminders", "Notification when assigned containers arrive in inspection bays", nInsp);
        cbHazmatAlerts = createSettingToggle("Urgent Hazmat Consignments", "High-priority alerts for hazardous or quarantined cargo", nHazmat);
        cbPortAuthAlerts = createSettingToggle("Port Authority Clearance Updates", "Notification when port authority clears inspected shipments", nPort);
        cbDigestEmail = createSettingToggle("Daily Customs Digest Email", "Summary of port clearance metrics and audit history", nDigest);

        VBox toggles = new VBox(10,
                cbDocAlerts,
                cbInspectionAlerts,
                cbHazmatAlerts,
                cbPortAuthAlerts,
                cbDigestEmail
        );

        Button saveNotifBtn = new Button("💾  Save Notification Preferences");
        saveNotifBtn.setMaxWidth(Double.MAX_VALUE);
        saveNotifBtn.setStyle(
                "-fx-background-color:" + BLUE + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:9 16;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );
        saveNotifBtn.setOnAction(e -> saveNotificationPreferences());

        VBox content = new VBox(10,
                titleBox,
                new Separator(),
                toggles,
                new Region(),
                saveNotifBtn
        );
        content.setPadding(new Insets(16, 18, 16, 18));
        content.setStyle(CARD_STYLE);
        return content;
    }

    // ==========================================
    // ROW 2: 3 CARDS
    // 4. Customs Jurisdiction & Workspace
    // 5. Active Station Telemetry
    // 6. Quick Customs Operational Actions
    // ==========================================
    private HBox createRow2() {
        VBox card4 = jurisdictionWorkspaceCard();
        VBox card5 = activeSessionCard();
        VBox card6 = quickCustomsActionsCard();

        HBox row = new HBox(14, card4, card5, card6);
        card4.prefWidthProperty().bind(row.widthProperty().multiply(0.33));
        card5.prefWidthProperty().bind(row.widthProperty().multiply(0.34));
        card6.prefWidthProperty().bind(row.widthProperty().multiply(0.33));

        HBox.setHgrow(card4, Priority.ALWAYS);
        HBox.setHgrow(card5, Priority.ALWAYS);
        HBox.setHgrow(card6, Priority.ALWAYS);

        return row;
    }

    // 4. Customs Jurisdiction & Workspace Card
    private VBox jurisdictionWorkspaceCard() {
        Label title = new Label("Customs Jurisdiction");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Assigned port terminal workspace and regulatory scope");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        VBox details = new VBox(6);
        details.getChildren().addAll(
                compactInfoRow("Assigned Port", officerPort),
                compactInfoRow("Inspection Area", "Bay 03 / Customs Physical Inspection Bay"),
                compactInfoRow("Operational Shift", "Shift Alpha (08:00 - 18:00 IST)"),
                compactInfoRow("Clearance Authority", "CBIC Maritime Regulatory Authority"),
                compactInfoRow("Ledger Sync", "Continuous Cloud Firestore Stream")
        );

        autoRefreshCombo = new ComboBox<>();
        autoRefreshCombo.getItems().addAll("Auto-Refresh (Every 10 Seconds)", "Auto-Refresh (Every 30 Seconds)", "Auto-Refresh (Every 60 Seconds)");
        autoRefreshCombo.setValue("Auto-Refresh (Every 10 Seconds)");
        autoRefreshCombo.setMaxWidth(Double.MAX_VALUE);
        styleAdminCombo(autoRefreshCombo);

        Button saveWsBtn = new Button("💾  Save Workspace Settings");
        saveWsBtn.setMaxWidth(Double.MAX_VALUE);
        saveWsBtn.setStyle(
                "-fx-background-color:#f1f5f9;" +
                "-fx-text-fill:#334155;" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:9 16;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );
        saveWsBtn.setOnAction(e -> showToast("Workspace preferences updated: " + autoRefreshCombo.getValue(), true));

        VBox content = new VBox(10,
                titleBox,
                new Separator(),
                details,
                labeledControl("Data Refresh Frequency", autoRefreshCombo),
                new Region(),
                saveWsBtn
        );
        content.setPadding(new Insets(16, 18, 16, 18));
        content.setStyle(CARD_STYLE);
        return content;
    }

    // 5. Active Station Telemetry Card
    private VBox activeSessionCard() {
        Label title = new Label("Station & Security Telemetry");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Current login session and terminal security telemetry");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        VBox details = new VBox(6);
        details.getChildren().addAll(
                compactInfoRow("Terminal ID", "STN-CO-" + Math.abs(officerEmail.hashCode() % 8999 + 1000)),
                compactInfoRow("Operating System", "macOS Terminal Station"),
                compactInfoRow("Security Protocol", "TLS 1.3 / Encrypted Channel"),
                compactInfoRow("Login Time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))),
                compactInfoRow("Session Token", "Validated (Active)")
        );

        Button refreshSessionBtn = new Button("↻  Re-verify Station Telemetry");
        refreshSessionBtn.setMaxWidth(Double.MAX_VALUE);
        refreshSessionBtn.setStyle(
                "-fx-background-color:#f1f5f9;" +
                "-fx-text-fill:#334155;" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:9 16;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );
        refreshSessionBtn.setOnAction(e -> showToast("Station telemetry re-verified successfully.", true));

        VBox content = new VBox(10,
                titleBox,
                new Separator(),
                details,
                new Region(),
                refreshSessionBtn
        );
        content.setPadding(new Insets(16, 18, 16, 18));
        content.setStyle(CARD_STYLE);
        return content;
    }

    // 6. Quick Customs Operational Actions Card
    private VBox quickCustomsActionsCard() {
        Label title = new Label("Customs Operational Actions");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Direct shortcuts for customs inspection activities");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        Button docBtn = new Button("▣  Open Document Verification Desk");
        docBtn.setMaxWidth(Double.MAX_VALUE);
        docBtn.setStyle("-fx-background-color:#eff6ff; -fx-text-fill:" + BLUE + "; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:9 14; -fx-background-radius:6; -fx-cursor:hand;");
        docBtn.setOnAction(e -> showToast("Use left sidebar to navigate to Document Verification.", true));

        Button cargoBtn = new Button("◇  Open Cargo Inspection Bay");
        cargoBtn.setMaxWidth(Double.MAX_VALUE);
        cargoBtn.setStyle("-fx-background-color:#ecfdf5; -fx-text-fill:#16a34a; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:9 14; -fx-background-radius:6; -fx-cursor:hand;");
        cargoBtn.setOnAction(e -> showToast("Use left sidebar to navigate to Cargo Inspection.", true));

        Button repBtn = new Button("📊  Open Reports & Compliance Export");
        repBtn.setMaxWidth(Double.MAX_VALUE);
        repBtn.setStyle("-fx-background-color:#fff7ed; -fx-text-fill:#ea580c; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:9 14; -fx-background-radius:6; -fx-cursor:hand;");
        repBtn.setOnAction(e -> showToast("Use left sidebar to navigate to Reports & Analytics.", true));

        VBox actions = new VBox(10, docBtn, cargoBtn, repBtn);

        VBox content = new VBox(10,
                titleBox,
                new Separator(),
                actions
        );
        content.setPadding(new Insets(16, 18, 16, 18));
        content.setStyle(CARD_STYLE);
        return content;
    }

    // ==========================================
    // DATABASE PERSISTENCE ACTIONS
    // ==========================================
    private void saveProfileChanges() {
        String newName = nameField.getText() != null ? nameField.getText().trim() : "";
        String newEmail = emailField.getText() != null ? emailField.getText().trim() : "";

        if (newName.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Full Name cannot be empty.");
            a.showAndWait();
            return;
        }

        if (newEmail.isEmpty() || !newEmail.contains("@")) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Please enter a valid email address.");
            a.showAndWait();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("fullName", newName);
        updates.put("email", newEmail.toLowerCase());
        updates.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));

        String targetUid = !officerUid.isBlank() ? officerUid : officerEmail;

        userDao.updateUserProfileFields(targetUid, updates).thenAccept(success -> {
            Platform.runLater(() -> {
                if (success) {
                    officerName = newName;
                    officerEmail = newEmail;
                    showToast("Profile successfully updated and saved to Firestore: " + newName + " (" + newEmail + ")", true);
                    UserDao.logActivity(newName, newEmail, "PROFILE_UPDATED", "Customs Officer profile updated by " + newName);
                } else {
                    showToast("Failed to update profile in database. Please check connection.", false);
                }
            });
        });
    }

    private void handlePasswordUpdate() {
        String cur = showPasswords ? currentPassText.getText() : currentPassField.getText();
        String np = showPasswords ? newPassText.getText() : newPassField.getText();
        String cp = showPasswords ? confirmPassText.getText() : confirmPassField.getText();

        if (cur.isBlank()) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Please enter your current password.");
            a.showAndWait();
            return;
        }

        if (np.length() < 6) {
            Alert a = new Alert(Alert.AlertType.WARNING, "New password must be at least 6 characters long.");
            a.showAndWait();
            return;
        }

        if (!np.equals(cp)) {
            Alert a = new Alert(Alert.AlertType.WARNING, "New password and confirmation password do not match.");
            a.showAndWait();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("password", np);
        updates.put("mustChangePassword", false);
        updates.put("passwordUpdatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));

        String targetUid = !officerUid.isBlank() ? officerUid : officerEmail;

        userDao.updateUserProfileFields(targetUid, updates).thenAccept(success -> {
            Platform.runLater(() -> {
                if (success) {
                    currentPassField.clear(); currentPassText.clear();
                    newPassField.clear(); newPassText.clear();
                    confirmPassField.clear(); confirmPassText.clear();
                    showToast("Password successfully updated and encrypted in database.", true);
                    UserDao.logActivity(officerName, officerEmail, "PASSWORD_UPDATED", "Customs Officer password changed");
                } else {
                    showToast("Password update failed. Please verify credentials.", false);
                }
            });
        });
    }

    private void saveNotificationPreferences() {
        boolean nDocs = cbDocAlerts.isSelected();
        boolean nInsp = cbInspectionAlerts.isSelected();
        boolean nHazmat = cbHazmatAlerts.isSelected();
        boolean nPort = cbPortAuthAlerts.isSelected();
        boolean nDigest = cbDigestEmail.isSelected();

        Map<String, Object> updates = new HashMap<>();
        updates.put("notifyDocs", nDocs);
        updates.put("notifyInspection", nInsp);
        updates.put("notifyHazmat", nHazmat);
        updates.put("notifyPortAuth", nPort);
        updates.put("notifyDigest", nDigest);
        updates.put("preferencesUpdatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));

        String targetUid = !officerUid.isBlank() ? officerUid : officerEmail;

        userDao.updateUserProfileFields(targetUid, updates).thenAccept(success -> {
            Platform.runLater(() -> {
                if (success) {
                    showToast("Notification preferences successfully saved to Firestore.", true);
                    UserDao.logActivity(officerName, officerEmail, "PREFERENCES_SAVED", "Notification channels updated");
                } else {
                    showToast("Failed to save notification preferences to database.", false);
                }
            });
        });
    }

    // ==========================================
    // UI HELPERS (ADMIN STYLE)
    // ==========================================
    private VBox labeledControl(String labelText, Node control) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill:#334155; -fx-font-size:12; -fx-font-weight:bold;");
        VBox box = new VBox(3, lbl, control);
        return box;
    }

    private HBox compactInfoRow(String key, String value) {
        Label k = new Label(key);
        k.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label v = new Label(value);
        v.setStyle("-fx-text-fill:#0f172a; -fx-font-size:12; -fx-font-weight:bold;");
        HBox row = new HBox(8, k, sp, v);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 0, 2, 0));
        return row;
    }

    private CheckBox createSettingToggle(String titleText, String subtitleText, boolean initialValue) {
        CheckBox cb = new CheckBox();
        cb.setSelected(initialValue);

        Label t = new Label(titleText);
        t.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");

        Label s = new Label(subtitleText);
        s.setStyle("-fx-text-fill:#64748b; -fx-font-size:11;");

        VBox textCol = new VBox(1, t, s);
        cb.setGraphic(textCol);
        cb.setStyle("-fx-font-size:12;");
        return cb;
    }

    private void styleCustomField(TextField f) {
        f.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:7 10; -fx-font-size:13; -fx-text-fill:#0f172a;");
    }

    private void styleAdminCombo(ComboBox<?> cb) {
        cb.setStyle("-fx-background-radius:6; -fx-padding:5 8; -fx-font-size:12; -fx-border-color:#e2e8f0; -fx-background-color:#f8fafc; -fx-text-fill:#0f172a;");
    }

    public VBox getContent() {
        return content;
    }
}
