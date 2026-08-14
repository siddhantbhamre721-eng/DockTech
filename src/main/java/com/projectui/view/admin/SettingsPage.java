package com.projectui.view.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.projectui.service.FirebaseAuthService;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SettingsPage extends AdminTheme {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private final AdminDataStore store = AdminDataStore.getInstance();
    private final FirebaseAuthService authService = new FirebaseAuthService();

    // Feedback Toast Banner
    private Label toastMessage;
    private HBox toastBanner;

    // Password Fields & State
    private PasswordField currentPassField;
    private TextField currentPassText;
    private PasswordField newPassField;
    private TextField newPassText;
    private PasswordField confirmPassField;
    private TextField confirmPassText;
    private boolean showPasswords = false;

    // Navigation callbacks
    private Runnable goToUsersAction;
    private Runnable goToPortsAction;

    public SettingsPage() {
        this(() -> {}, () -> {});
    }

    public SettingsPage(Runnable goToUsersAction, Runnable goToPortsAction) {
        this.goToUsersAction = goToUsersAction != null ? goToUsersAction : () -> {};
        this.goToPortsAction = goToPortsAction != null ? goToPortsAction : () -> {};
    }

    public Parent getRoot() {
        return getRoot(this.goToUsersAction, this.goToPortsAction);
    }

    public Parent getRoot(Runnable goToUsers, Runnable goToPorts) {
        if (goToUsers != null) this.goToUsersAction = goToUsers;
        if (goToPorts != null) this.goToPortsAction = goToPorts;

        VBox page = pageContent();
        page.setSpacing(14);
        page.setPadding(new Insets(16, 20, 20, 20));

        // 1. Toast Notification Banner
        toastBanner = createToastBanner();

        // 2. Page Header
        HBox header = createSettingsHeader(
                "Settings",
                "Manage administrator profile, security credentials, notification preferences and active sessions."
        );

        // 3. Row 1: Admin Profile, Change Password, Notification Preferences
        HBox row1 = createRow1();

        // 4. Row 2: Account Settings, Active Sessions, Quick Account Actions
        HBox row2 = createRow2();

        page.getChildren().addAll(
                header,
                toastBanner,
                row1,
                row2
        );

        // Smooth ScrollPane wrapper
        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background:transparent; -fx-background-color:transparent; -fx-padding:0;");

        return scrollPane;
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

        toastMessage = new Label("Administrator session active. Settings saved to profile data store.");
        toastMessage.setStyle("-fx-text-fill:#334155; -fx-font-size:14; -fx-font-weight:bold;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label close = new Label("✕");
        close.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:14; -fx-cursor:hand;");
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
        }
    }

    // ==========================================
    // ROW 1: 3 CARDS
    // 1. Admin Profile ⭐⭐⭐
    // 2. Change Password ⭐⭐⭐
    // 3. Notification Preferences ⭐⭐
    // ==========================================
    private HBox createRow1() {
        VBox card1 = adminProfileCard();
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

    // 1. Admin Profile Card ⭐⭐⭐
    private VBox adminProfileCard() {
        Label title = new Label("Admin Profile");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Manage administrator profile details & registered email");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        String currentAdmin = store.adminName == null || store.adminName.isBlank() ? "Aarav Mehta" : store.adminName;
        String currentEmail = store.adminEmail == null ? "" : store.adminEmail;

        TextField nameField = new TextField(currentAdmin);
        nameField.setPromptText("Enter full name");
        styleCustomField(nameField);

        TextField emailField = new TextField(currentEmail);
        emailField.setPromptText("Enter profile email address");
        styleCustomField(emailField);

        VBox fields = new VBox(8,
                labeledControl("Full Name *", nameField),
                labeledControl("Email Address *", emailField)
        );

        VBox details = new VBox(4);
        details.getChildren().addAll(
                compactInfoRow("Role", "Administrator (Full Access)"),
                compactInfoRow("Account Status", "Active & Verified"),
                compactInfoRow("Last Login", "Today, 09:00 AM"),
                compactInfoRow("Active Session", "Live (Current Device)")
        );

        Button saveProfileBtn = new Button("💾  Save Profile Changes");
        saveProfileBtn.setMaxWidth(Double.MAX_VALUE);
        saveProfileBtn.setStyle(
                "-fx-background-color:" + BLUE + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-size:14;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:9 16;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );
        saveProfileBtn.setOnAction(e -> {
            String newName = nameField.getText().trim();
            String newEmail = emailField.getText().trim();
            if (!newName.isEmpty()) store.adminName = newName;
            store.adminEmail = newEmail;
            showToast("Admin profile updated: " + store.adminName + " (" + (newEmail.isEmpty() ? "No Email" : newEmail) + ")", true);
        });

        VBox content = new VBox(10,
                titleBox,
                new Separator(),
                fields,
                details,
                new Separator(),
                saveProfileBtn
        );

        return cardContainer(content);
    }

    // 2. Change Password Card ⭐⭐⭐
    private VBox changePasswordCard() {
        Label title = new Label("Change Password");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Update your administrator account password");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        currentPassField = new PasswordField(); currentPassField.setPromptText("Current password"); styleCustomField(currentPassField);
        currentPassText = new TextField(); currentPassText.setPromptText("Current password"); styleCustomField(currentPassText); currentPassText.setManaged(false); currentPassText.setVisible(false);

        newPassField = new PasswordField(); newPassField.setPromptText("New password (min 6 chars)"); styleCustomField(newPassField);
        newPassText = new TextField(); newPassText.setPromptText("New password (min 6 chars)"); styleCustomField(newPassText); newPassText.setManaged(false); newPassText.setVisible(false);

        confirmPassField = new PasswordField(); confirmPassField.setPromptText("Confirm new password"); styleCustomField(confirmPassField);
        confirmPassText = new TextField(); confirmPassText.setPromptText("Confirm new password"); styleCustomField(confirmPassText); confirmPassText.setManaged(false); confirmPassText.setVisible(false);

        Button toggleShowBtn = new Button("👁  Show Password");
        toggleShowBtn.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-text-fill:" + BLUE + ";" +
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;" +
                "-fx-cursor:hand;"
        );
        toggleShowBtn.setOnAction(e -> {
            showPasswords = !showPasswords;
            if (showPasswords) {
                currentPassText.setText(currentPassField.getText()); currentPassField.setManaged(false); currentPassField.setVisible(false); currentPassText.setManaged(true); currentPassText.setVisible(true);
                newPassText.setText(newPassField.getText()); newPassField.setManaged(false); newPassField.setVisible(false); newPassText.setManaged(true); newPassText.setVisible(true);
                confirmPassText.setText(confirmPassField.getText()); confirmPassField.setManaged(false); confirmPassField.setVisible(false); confirmPassText.setManaged(true); confirmPassText.setVisible(true);
                toggleShowBtn.setText("🙈  Hide Password");
            } else {
                currentPassField.setText(currentPassText.getText()); currentPassText.setManaged(false); currentPassText.setVisible(false); currentPassField.setManaged(true); currentPassField.setVisible(true);
                newPassField.setText(newPassText.getText()); newPassText.setManaged(false); newPassText.setVisible(false); newPassField.setManaged(true); newPassField.setVisible(true);
                confirmPassField.setText(confirmPassText.getText()); confirmPassText.setManaged(false); confirmPassText.setVisible(false); confirmPassField.setManaged(true); confirmPassField.setVisible(true);
                toggleShowBtn.setText("👁  Show Password");
            }
        });

        HBox passHeader = new HBox(8, labeledControl("Current Password *", new StackPane(currentPassField, currentPassText)), toggleShowBtn);
        HBox.setHgrow(passHeader.getChildren().get(0), Priority.ALWAYS);
        passHeader.setAlignment(Pos.BOTTOM_LEFT);

        Label strengthLabel = new Label("● Password Strength: Strong (12+ chars, letters & numbers)");
        strengthLabel.setStyle("-fx-text-fill:#16a34a; -fx-font-size:12; -fx-font-weight:bold;");

        Button updatePassBtn = new Button("🔑  Update Password");
        updatePassBtn.setMaxWidth(Double.MAX_VALUE);
        updatePassBtn.setStyle(
                "-fx-background-color:#eff6ff;" +
                "-fx-text-fill:" + BLUE + ";" +
                "-fx-font-size:14;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:9 16;" +
                "-fx-background-radius:6;" +
                "-fx-border-color:#bfdbfe;" +
                "-fx-border-radius:6;" +
                "-fx-cursor:hand;"
        );
        updatePassBtn.setOnAction(e -> {
            String curr = showPasswords ? currentPassText.getText() : currentPassField.getText();
            String newP = showPasswords ? newPassText.getText() : newPassField.getText();
            String conf = showPasswords ? confirmPassText.getText() : confirmPassField.getText();

            if (curr.isBlank()) {
                showToast("Please enter your current password.", false);
                return;
            }
            if (store.adminPassword != null && !store.adminPassword.isBlank() && !store.adminPassword.equals(curr)) {
                showToast("Current password incorrect.", false);
                return;
            }
            if (newP.length() < 6) {
                showToast("New password must contain at least 6 characters.", false);
                return;
            }
            if (!newP.equals(conf)) {
                showToast("New password and confirmation do not match.", false);
                return;
            }

            store.adminPassword = newP;
            if (store.authToken != null && !store.authToken.isBlank()) {
                authService.updatePassword(store.authToken, newP)
                        .whenComplete((unused, err) -> Platform.runLater(() -> {
                            if (err == null) {
                                showToast("Password updated successfully in database authentication.", true);
                            } else {
                                showToast("Password saved locally: " + err.getMessage(), true);
                            }
                        }));
            } else {
                showToast("Administrator password updated successfully.", true);
            }
        });

        VBox content = new VBox(8,
                titleBox,
                new Separator(),
                passHeader,
                labeledControl("New Password *", new StackPane(newPassField, newPassText)),
                labeledControl("Confirm New Password *", new StackPane(confirmPassField, confirmPassText)),
                strengthLabel,
                new Separator(),
                updatePassBtn
        );

        return cardContainer(content);
    }

    // 3. Notification Preferences Card ⭐⭐
    private VBox notificationPreferencesCard() {
        Label title = new Label("Notification Preferences");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Configure alert notifications & dispatch delivery");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        CheckBox emailNotify = customSwitch("Email Notifications", "Receive daily summaries & alerts via email", true);
        CheckBox systemNotify = customSwitch("System Notifications", "In-app alerts & desktop push notifications", true);
        CheckBox securityNotify = customSwitch("Security Alerts", "Immediate alerts on login & password changes", true);
        CheckBox operationalNotify = customSwitch("Operational Alerts", "Port congestion & berth status changes", true);

        VBox switches = new VBox(8, emailNotify, systemNotify, securityNotify, operationalNotify);

        Button savePrefBtn = new Button("💾  Save Preferences");
        savePrefBtn.setMaxWidth(Double.MAX_VALUE);
        savePrefBtn.setStyle(
                "-fx-background-color:" + BLUE + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-size:14;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:9 16;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );
        savePrefBtn.setOnAction(e -> {
            store.notifications = emailNotify.isSelected();
            showToast("Notification preferences saved successfully.", true);
        });

        VBox content = new VBox(10,
                titleBox,
                new Separator(),
                switches,
                new Separator(),
                savePrefBtn
        );

        return cardContainer(content);
    }

    // ==========================================
    // ROW 2: 3 CARDS
    // 4. Account Settings ⭐⭐
    // 5. Active Sessions ⭐⭐
    // 6. Quick Account Actions ⭐⭐
    // ==========================================
    private HBox createRow2() {
        VBox card1 = accountSettingsCard();
        VBox card2 = activeSessionsCard();
        VBox card3 = quickAccountActionsCard();

        HBox row = new HBox(14, card1, card2, card3);
        card1.prefWidthProperty().bind(row.widthProperty().multiply(0.33));
        card2.prefWidthProperty().bind(row.widthProperty().multiply(0.34));
        card3.prefWidthProperty().bind(row.widthProperty().multiply(0.33));

        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);

        return row;
    }

    // 4. Account Settings Card ⭐⭐
    private VBox accountSettingsCard() {
        Label title = new Label("Account Settings");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("System preferences & user-facing account options");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        ComboBox<String> timeoutCombo = combo("Timeout", "15 Minutes", "30 Minutes", "1 Hour", "4 Hours");
        timeoutCombo.setValue("30 Minutes");
        timeoutCombo.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> langCombo = combo("Language", "English", "Spanish", "French", "German");
        langCombo.setValue(store.language == null || store.language.isBlank() ? "English" : store.language);
        langCombo.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> formatCombo = combo("Date Format", "DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD");
        formatCombo.setValue(store.dateFormat == null || store.dateFormat.isBlank() ? "DD/MM/YYYY" : store.dateFormat);
        formatCombo.setMaxWidth(Double.MAX_VALUE);

        VBox fields = new VBox(8,
                labeledControl("Session Inactivity Timeout", timeoutCombo),
                labeledControl("Interface Language", langCombo),
                labeledControl("Date Display Format", formatCombo)
        );

        Button saveSettingsBtn = new Button("💾  Save Account Settings");
        saveSettingsBtn.setMaxWidth(Double.MAX_VALUE);
        saveSettingsBtn.setStyle(
                "-fx-background-color:" + BLUE + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-size:14;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:9 16;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );
        saveSettingsBtn.setOnAction(e -> {
            store.language = langCombo.getValue();
            store.dateFormat = formatCombo.getValue();
            showToast("Account settings saved (Language: " + store.language + ", Format: " + store.dateFormat + ")", true);
        });

        VBox content = new VBox(10,
                titleBox,
                new Separator(),
                fields,
                new Separator(),
                saveSettingsBtn
        );

        return cardContainer(content);
    }

    // 5. Active Sessions Card ⭐⭐
    private VBox activeSessionsCard() {
        Label title = new Label("Active Sessions");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Currently authorized device & browser sessions");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        VBox sessionList = new VBox(6);
        sessionList.getChildren().addAll(
                sessionRow("💻 macOS (Chrome 122.0)", "Mumbai, India", "Active Now (This Device)", true),
                sessionRow("📱 iOS (Dock Tech Mobile App)", "Mumbai, India", "12 mins ago", false)
        );

        Button signOutOthersBtn = new Button("🚪  Sign Out All Other Sessions");
        signOutOthersBtn.setMaxWidth(Double.MAX_VALUE);
        signOutOthersBtn.setStyle(
                "-fx-background-color:#fff1f2;" +
                "-fx-text-fill:#e11d48;" +
                "-fx-font-size:14;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:9 16;" +
                "-fx-background-radius:6;" +
                "-fx-border-color:#fecdd3;" +
                "-fx-border-radius:6;" +
                "-fx-cursor:hand;"
        );
        signOutOthersBtn.setOnAction(e -> {
            sessionList.getChildren().clear();
            sessionList.getChildren().add(sessionRow("💻 macOS (Chrome 122.0)", "Mumbai, India", "Active Now (This Device)", true));
            showToast("Signed out all other active sessions successfully.", true);
        });

        VBox content = new VBox(10,
                titleBox,
                new Separator(),
                sessionList,
                new Separator(),
                signOutOthersBtn
        );

        return cardContainer(content);
    }

    private HBox sessionRow(String device, String loc, String timeText, boolean isCurrent) {
        Circle dot = new Circle(3.5);
        dot.setStyle("-fx-fill:" + (isCurrent ? "#16a34a" : "#64748b") + ";");

        Label devLbl = new Label(device);
        devLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");

        Label subLbl = new Label(loc + " • " + timeText);
        subLbl.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        VBox copy = new VBox(2, devLbl, subLbl);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label tag = new Label(isCurrent ? "Current" : "Active");
        tag.setStyle(
                "-fx-text-fill:" + (isCurrent ? "#16a34a" : "#475569") + ";" +
                "-fx-background-color:" + (isCurrent ? "#ecfdf5" : "#f1f5f9") + ";" +
                "-fx-background-radius:4;" +
                "-fx-padding:3 8;" +
                "-fx-font-size:11;" +
                "-fx-font-weight:bold;"
        );

        HBox row = new HBox(8, dot, copy, space, tag);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 8, 6, 8));
        row.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#f1f5f9; -fx-border-radius:6; -fx-background-radius:6;");
        return row;
    }

    // 6. Quick Account Actions Card ⭐⭐
    private VBox quickAccountActionsCard() {
        Label title = new Label("Quick Account Actions");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Direct administrator account management shortcuts");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        Button activityBtn = accountActionButton("📋  Account Activity", "View audit trail & log history", () -> showSystemLogsDialog(titleBox.getScene().getWindow()));
        Button historyBtn = accountActionButton("📜  Login History", "View recent login timestamps", () -> showLoginHistoryDialog(titleBox.getScene().getWindow()));
        Button downloadBtn = accountActionButton("💾  Download My Data", "Export account profile JSON", () -> {
            showToast("Profile data exported: DockTech_Profile_" + store.adminName.replace(" ", "_") + ".json", true);
        });
        Button deactivateBtn = accountActionButton("⚠️  Deactivate Account", "Request account suspension", () -> showDeactivateDialog(titleBox.getScene().getWindow()));

        VBox actionsList = new VBox(6, activityBtn, historyBtn, downloadBtn, deactivateBtn);

        VBox content = new VBox(10,
                titleBox,
                new Separator(),
                actionsList
        );

        return cardContainer(content);
    }

    private Button accountActionButton(String btnTitle, String btnSub, Runnable onClick) {
        Label t = new Label(btnTitle);
        t.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14; -fx-font-weight:bold;");

        Label s = new Label(btnSub);
        s.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        VBox box = new VBox(2, t, s);
        box.setAlignment(Pos.CENTER_LEFT);

        Button b = new Button("", box);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(8, 12, 8, 12));
        b.setStyle(
                "-fx-background-color:#f8fafc;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:6;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );
        b.setOnMouseEntered(e -> b.setStyle(
                "-fx-background-color:#eff6ff;" +
                "-fx-border-color:#bfdbfe;" +
                "-fx-border-radius:6;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        ));
        b.setOnMouseExited(e -> b.setStyle(
                "-fx-background-color:#f8fafc;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:6;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        ));
        b.setOnAction(e -> {
            if (onClick != null) onClick.run();
        });
        return b;
    }

    // ==========================================
    // HELPER STYLES & DIALOGS
    // ==========================================
    private CheckBox customSwitch(String labelText, String subText, boolean defaultSelected) {
        CheckBox cb = new CheckBox(labelText);
        cb.setSelected(defaultSelected);
        cb.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14; -fx-font-weight:bold; -fx-cursor:hand;");
        return cb;
    }

    private VBox cardContainer(Node content) {
        VBox card = new VBox(content);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.04), 6, 0, 0, 2);"
        );
        return card;
    }

    private HBox compactInfoRow(String labelText, String valueText) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill:#64748b; -fx-font-size:13; -fx-font-weight:bold;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label val = new Label(valueText);
        val.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14; -fx-font-weight:bold;");

        HBox row = new HBox(6, label, space, val);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));
        row.setStyle("-fx-border-color:#f1f5f9; -fx-border-width:0 0 1 0;");
        return row;
    }

    private void showSystemLogsDialog(javafx.stage.Window owner) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setTitle("Dock Tech Account Activity Logs");

        Label title = new Label("Account & Security Audit Logs");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Chronological record of account events and security logons");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");

        VBox logList = new VBox(6);
        logList.getChildren().addAll(
                auditLogRow("2 mins ago", "Aarav Mehta (Admin)", "Security session authorized via Admin Console", "#16a34a"),
                auditLogRow("14 mins ago", "Port Manager Mumbai", "Berth B-04 status changed to OCCUPIED", "#0b63ff"),
                auditLogRow("42 mins ago", "System Dispatcher", "Vessel MV Ocean Star arrival coordinates recorded", "#0ea5e9"),
                auditLogRow("1 hr ago", "Customs Authority", "Customs declaration cleared for 2,450 MT Bulk Cargo", "#6366f1"),
                auditLogRow("3 hrs ago", "Automated Health Monitor", "System session pool healthy", "#16a34a"),
                auditLogRow("Yesterday", "Security System", "Routine 24h vulnerability & auth sweep: Clean", "#16a34a")
        );

        ScrollPane sp = new ScrollPane(logList);
        sp.setFitToWidth(true);
        sp.setPrefHeight(230);
        sp.setStyle("-fx-background:white; -fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:6;");

        Button closeBtn = new Button("Close");
        closeBtn.setStyle(
                "-fx-background-color:#f1f5f9;" +
                "-fx-text-fill:#475569;" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:8 18;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );
        closeBtn.setOnAction(e -> stage.close());

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox btnRow = new HBox(space, closeBtn);

        VBox layout = new VBox(12,
                new VBox(2, title, subtitle),
                new Separator(),
                sp,
                new Separator(),
                btnRow
        );
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color:white;");

        Scene scene = new Scene(layout, 540, 410);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void showLoginHistoryDialog(javafx.stage.Window owner) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setTitle("Login History");

        Label title = new Label("📜 Administrator Login History");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Recent successful and attempted logons to the admin console");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");

        VBox list = new VBox(6);
        list.getChildren().addAll(
                auditLogRow("Today, 09:00 AM", "macOS Chrome", "Successful Logon — IP: 103.22.45.12", "#16a34a"),
                auditLogRow("Yesterday, 04:30 PM", "macOS Chrome", "Successful Logon — IP: 103.22.45.12", "#16a34a"),
                auditLogRow("12 Aug 2026, 11:15 AM", "iOS Mobile App", "Successful Logon — IP: 103.22.48.90", "#0b63ff"),
                auditLogRow("10 Aug 2026, 08:45 AM", "macOS Chrome", "Successful Logon — IP: 103.22.45.12", "#16a34a")
        );

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#475569; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:8 18; -fx-background-radius:6; -fx-cursor:hand;");
        closeBtn.setOnAction(e -> stage.close());

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        VBox layout = new VBox(12, new VBox(2, title, subtitle), new Separator(), list, new Separator(), new HBox(space, closeBtn));
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color:white;");

        Scene scene = new Scene(layout, 480, 360);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void showDeactivateDialog(javafx.stage.Window owner) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) stage.initOwner(owner);
        stage.setTitle("Deactivate Account");

        Label title = new Label("⚠️ Request Account Deactivation");
        title.setStyle("-fx-text-fill:#ef4444; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Deactivating your administrator account will revoke access to the Dock Tech management portal.");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        subtitle.setWrapText(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#475569; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:8 16; -fx-background-radius:6; -fx-cursor:hand;");
        cancelBtn.setOnAction(e -> stage.close());

        Button confirmBtn = new Button("Confirm Deactivation Request");
        confirmBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:8 16; -fx-background-radius:6; -fx-cursor:hand;");
        confirmBtn.setOnAction(e -> {
            stage.close();
            showToast("Deactivation request submitted to Dock Tech System Administrator.", false);
        });

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox btnRow = new HBox(8, space, cancelBtn, confirmBtn);

        VBox layout = new VBox(14, new VBox(4, title, subtitle), new Separator(), btnRow);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color:white;");

        Scene scene = new Scene(layout, 460, 200);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private HBox auditLogRow(String time, String actor, String eventText, String dotColor) {
        Circle dot = new Circle(3.5);
        dot.setStyle("-fx-fill:" + dotColor + ";");

        Label t = new Label(time);
        t.setMinWidth(75);
        t.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12;");

        Label act = new Label(actor);
        act.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");

        Label ev = new Label(" — " + eventText);
        ev.setStyle("-fx-text-fill:#475569; -fx-font-size:13;");

        HBox row = new HBox(6, dot, t, act, ev);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 6, 4, 6));
        row.setStyle("-fx-border-color:#f8fafc; -fx-border-width:0 0 1 0;");
        return row;
    }

    private void styleCustomField(TextField f) {
        f.setStyle(
                "-fx-background-color:#f8fafc;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:6;" +
                "-fx-background-radius:6;" +
                "-fx-padding:8 12 8 12;" +
                "-fx-font-size:14;"
        );
    }

    private VBox labeledControl(String labelText, Node control) {
        Label l = new Label(labelText);
        l.setStyle("-fx-text-fill:#334155; -fx-font-size:14; -fx-font-weight:bold;");
        VBox item = new VBox(4, l, control);
        return item;
    }
}
