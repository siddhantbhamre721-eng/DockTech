package com.projectui.view.portauthority;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.projectui.dao.UserDao;
import com.projectui.service.FirebaseAuthService;
import com.projectui.view.login.LoginPage;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

@SuppressWarnings({"unchecked", "unused"})
public class SettingsPage {

    private final VBox root = new VBox(16);

    // Backend DAOs & Authentication Service
    private final UserDao userDao = new UserDao();
    private final FirebaseAuthService authService = new FirebaseAuthService();

    // Authenticated Port Authority Context
    private String userUid = "usr_port_authority";
    private String userEmail = "authority@docktech.com";
    private String userName = "Port Authority";
    private String userPhone = "+91 98765 43210";
    private String userPort = "Cochin Port";
    private String userRole = "Port Authority";
    private String userAccountId = "PA-101";

    // Toast Feedback Banner
    private HBox toastBanner;
    private Label toastIcon;
    private Label toastMessage;

    // Profile UI Controls
    private TextField nameField;
    private TextField emailField;
    private TextField phoneField;
    private TextField portField;
    private TextField roleField;
    private Button editProfileBtn;
    private Button saveProfileBtn;
    private Button cancelProfileBtn;
    private HBox profileActionsBox;
    private boolean isEditingProfile = false;

    // Security UI Controls
    private PasswordField currentPassField;
    private TextField currentPassText;
    private PasswordField newPassField;
    private TextField newPassText;
    private PasswordField confirmPassField;
    private TextField confirmPassText;
    private ToggleButton showPassToggle;
    private Button changePasswordBtn;

    // Notification Preferences Toggles
    private ToggleButton vesselArrivalToggle;
    private ToggleButton vesselDepartureToggle;
    private ToggleButton cargoToggle;
    private ToggleButton containerToggle;
    private ToggleButton paymentToggle;
    private ToggleButton systemToggle;

    // Original Loaded Notification State (for Reset button)
    private boolean initVesselArrival = true;
    private boolean initVesselDeparture = true;
    private boolean initCargo = true;
    private boolean initContainer = true;
    private boolean initPayment = true;
    private boolean initSystem = true;

    public SettingsPage() {
        root.setSpacing(16);
        root.setPadding(new Insets(0));
        root.setStyle("-fx-background-color:transparent;");

        loadAuthenticatedUserContext();
        buildLayout();
    }

    // =====================================================
    // 1. AUTHENTICATED USER CONTEXT RESOLUTION
    // =====================================================
    private void loadAuthenticatedUserContext() {
        Map<String, Object> user = LoginPage.loggedInUser;
        if (user != null) {
            if (user.get("uid") != null) userUid = user.get("uid").toString();
            if (user.get("email") != null) userEmail = user.get("email").toString();
            if (user.get("name") != null) userName = user.get("name").toString();
            if (user.get("phone") != null) userPhone = user.get("phone").toString();
            if (user.get("role") != null) userRole = user.get("role").toString();

            // Port Assignment Resolution
            Object portObj = user.get("assignedPort");
            if (portObj == null || portObj.toString().isBlank()) {
                portObj = user.get("company");
            }
            if (portObj == null || portObj.toString().isBlank()) {
                portObj = user.get("organization");
            }
            if (portObj != null && !portObj.toString().isBlank()) {
                userPort = UserDao.normalizePort(portObj.toString());
            } else {
                userPort = resolvePortFromEmailOrName(userEmail, userName);
            }

            if (user.get("accountId") != null) userAccountId = user.get("accountId").toString();
            else if (user.get("paId") != null) userAccountId = user.get("paId").toString();
            else userAccountId = "PA-" + Math.abs(userEmail.toLowerCase().hashCode() % 900 + 100);

            // Load Notification Preferences
            initVesselArrival = getBooleanPref(user, "notifyVesselArrival", true);
            initVesselDeparture = getBooleanPref(user, "notifyVesselDeparture", true);
            initCargo = getBooleanPref(user, "notifyCargo", true);
            initContainer = getBooleanPref(user, "notifyContainer", true);
            initPayment = getBooleanPref(user, "notifyPayment", true);
            initSystem = getBooleanPref(user, "notifySystem", true);
        } else {
            userPort = "Cochin Port";
            userAccountId = "PA-101";
        }
    }

    private boolean getBooleanPref(Map<String, Object> map, String key, boolean defaultVal) {
        if (map == null || !map.containsKey(key)) return defaultVal;
        Object val = map.get(key);
        if (val instanceof Boolean) return (Boolean) val;
        if (val != null) {
            String s = val.toString().trim().toLowerCase();
            return "true".equals(s) || "1".equals(s) || "yes".equals(s) || "on".equals(s);
        }
        return defaultVal;
    }

    private String resolvePortFromEmailOrName(String email, String name) {
        String lower = (email + " " + name).toLowerCase();
        if (lower.contains("mumbai") || lower.contains("bombay")) return "Port of Mumbai";
        if (lower.contains("chennai") || lower.contains("madras")) return "Port of Chennai";
        if (lower.contains("vizag") || lower.contains("visakhapatnam")) return "Port of Vizag";
        if (lower.contains("kolkata") || lower.contains("calcutta")) return "Port of Kolkata";
        if (lower.contains("cochin") || lower.contains("kochi")) return "Cochin Port";
        return "Cochin Port";
    }

    // =====================================================
    // 2. MAIN LAYOUT BUILDER
    // =====================================================
    private void buildLayout() {
        root.getChildren().clear();

        // 1. Page Header & Session Status
        HBox header = createHeader();

        // 2. Dynamic Toast Banner
        toastBanner = createToastBanner();

        // 3. Port Authority Profile Card
        VBox profileCard = createProfileCard();

        // 4. Security & Password Card
        VBox securityCard = createSecurityCard();

        // 5. Notification Preferences Card
        VBox notificationCard = createNotificationCard();

        root.getChildren().addAll(
                header,
                toastBanner,
                profileCard,
                securityCard,
                notificationCard
        );
    }

    // =====================================================
    // 3. HEADER SECTION
    // =====================================================
    private HBox createHeader() {
        Label title = new Label("Settings");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Label subtitle = new Label("Manage your Port Authority profile, security credentials, and alert notification preferences.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");

        VBox titleBox = new VBox(3, title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Port Badge
        Label portBadge = new Label("📍 " + userPort);
        portBadge.setStyle(
                "-fx-background-color: #EFF6FF;" +
                "-fx-text-fill: #1D4ED8;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 6 12;" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: #BFDBFE;" +
                "-fx-border-radius: 20;"
        );

        Label sessionBadge = new Label("● Session Active");
        sessionBadge.setStyle(
                "-fx-background-color: #ECFDF5;" +
                "-fx-text-fill: #047857;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 6 12;" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: #A7F3D0;" +
                "-fx-border-radius: 20;"
        );

        HBox badges = new HBox(8, portBadge, sessionBadge);
        badges.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(12, titleBox, spacer, badges);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    // =====================================================
    // 4. TOAST NOTIFICATION BANNER
    // =====================================================
    private HBox createToastBanner() {
        toastIcon = new Label("ℹ");
        toastIcon.setStyle("-fx-text-fill: #2563EB; -fx-font-size: 16px; -fx-font-weight: bold;");

        toastMessage = new Label("Settings loaded for " + userName + " (" + userPort + ").");
        toastMessage.setStyle("-fx-text-fill: #1E293B; -fx-font-size: 13px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label closeBtn = new Label("✕");
        closeBtn.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: bold;");
        closeBtn.setOnMouseClicked(e -> toastBanner.setVisible(false));

        HBox banner = new HBox(10, toastIcon, toastMessage, spacer, closeBtn);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(10, 16, 10, 16));
        banner.setStyle(
                "-fx-background-color: #F0F9FF;" +
                "-fx-border-color: #BAE6FD;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );
        banner.setVisible(false);
        banner.managedProperty().bind(banner.visibleProperty());
        return banner;
    }

    private void showToast(String message, boolean isSuccess) {
        toastMessage.setText(message);
        if (isSuccess) {
            toastIcon.setText("✓");
            toastIcon.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 16px; -fx-font-weight: bold;");
            toastBanner.setStyle(
                    "-fx-background-color: #F0FDF4;" +
                    "-fx-border-color: #BBF7D0;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-radius: 8;"
            );
        } else {
            toastIcon.setText("⚠️");
            toastIcon.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 16px; -fx-font-weight: bold;");
            toastBanner.setStyle(
                    "-fx-background-color: #FEF2F2;" +
                    "-fx-border-color: #FECACA;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-radius: 8;"
            );
        }
        toastBanner.setVisible(true);
    }

    // =====================================================
    // 5. PORT AUTHORITY PROFILE CARD
    // =====================================================
    private VBox createProfileCard() {
        VBox card = createCard();

        // Title and Avatar Header
        HBox cardHeader = new HBox(12);
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label("PA");
        avatar.setMinSize(48, 48);
        avatar.setMaxSize(48, 48);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle(
                "-fx-background-color: #DBEAFE;" +
                "-fx-text-fill: #1D4ED8;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 50;" +
                "-fx-border-color: #93C5FD;" +
                "-fx-border-radius: 50;"
        );

        Label cardTitle = new Label("Port Authority Profile");
        cardTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Label cardSub = new Label("Authenticated administrative account identity and maritime port assignment");
        cardSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        VBox titleBox = new VBox(2, cardTitle, cardSub);
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        editProfileBtn = new Button("✏️  Edit Profile");
        editProfileBtn.setStyle(
                "-fx-background-color: #EFF6FF;" +
                "-fx-text-fill: #2563EB;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 8 16;" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: #BFDBFE;" +
                "-fx-border-radius: 6;" +
                "-fx-cursor: hand;"
        );

        saveProfileBtn = new Button("💾  Save Profile");
        saveProfileBtn.setStyle(
                "-fx-background-color: #2563EB;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 8 16;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
        );
        saveProfileBtn.setVisible(false);
        saveProfileBtn.managedProperty().bind(saveProfileBtn.visibleProperty());

        cancelProfileBtn = new Button("Cancel");
        cancelProfileBtn.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #64748B;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 8 16;" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: #CBD5E1;" +
                "-fx-border-radius: 6;" +
                "-fx-cursor: hand;"
        );
        cancelProfileBtn.setVisible(false);
        cancelProfileBtn.managedProperty().bind(cancelProfileBtn.visibleProperty());

        profileActionsBox = new HBox(8, editProfileBtn, saveProfileBtn, cancelProfileBtn);
        profileActionsBox.setAlignment(Pos.CENTER_RIGHT);

        cardHeader.getChildren().addAll(avatar, titleBox, space, profileActionsBox);

        // Fields Form Grid
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(14);

        nameField = createStyledTextField(userName, false);
        emailField = createStyledTextField(userEmail, false);
        phoneField = createStyledTextField(userPhone, false);
        portField = createStyledTextField(userPort, false);
        roleField = createStyledTextField(userRole, false);
        TextField accountIdField = createStyledTextField(userAccountId, false);

        grid.add(createFieldNode("FULL NAME *", nameField), 0, 0);
        grid.add(createFieldNode("EMAIL ADDRESS (READ-ONLY)", emailField), 1, 0);
        grid.add(createFieldNode("PHONE NUMBER", phoneField), 2, 0);

        grid.add(createFieldNode("ASSIGNED PORT (ADMIN CONTROLLED)", portField), 0, 1);
        grid.add(createFieldNode("SYSTEM ROLE (READ-ONLY)", roleField), 1, 1);
        grid.add(createFieldNode("ACCOUNT IDENTIFIER", accountIdField), 2, 1);

        // Action Handlers
        editProfileBtn.setOnAction(e -> toggleProfileEdit(true));
        cancelProfileBtn.setOnAction(e -> {
            nameField.setText(userName);
            phoneField.setText(userPhone);
            toggleProfileEdit(false);
            showToast("Profile editing cancelled.", false);
        });

        saveProfileBtn.setOnAction(e -> {
            String newName = nameField.getText().trim();
            String newPhone = phoneField.getText().trim();

            if (newName.isEmpty()) {
                showToast("Full Name cannot be empty.", false);
                return;
            }

            // Save to Backend Database & Local Store
            userName = newName;
            userPhone = newPhone;

            Map<String, Object> extra = new HashMap<>();
            extra.put("assignedPort", userPort);
            extra.put("company", userPort);
            extra.put("organization", userPort);
            extra.put("accountId", userAccountId);
            extra.put("updatedAt", LocalDateTime.now().toString());

            // Preserve notification preferences in save
            extra.put("notifyVesselArrival", vesselArrivalToggle != null ? vesselArrivalToggle.isSelected() : initVesselArrival);
            extra.put("notifyVesselDeparture", vesselDepartureToggle != null ? vesselDepartureToggle.isSelected() : initVesselDeparture);
            extra.put("notifyCargo", cargoToggle != null ? cargoToggle.isSelected() : initCargo);
            extra.put("notifyContainer", containerToggle != null ? containerToggle.isSelected() : initContainer);
            extra.put("notifyPayment", paymentToggle != null ? paymentToggle.isSelected() : initPayment);
            extra.put("notifySystem", systemToggle != null ? systemToggle.isSelected() : initSystem);

            userDao.saveUserExtended(userUid, userEmail, userName, userPort, userPhone, userRole, "Active", extra)
                    .whenComplete((unused, err) -> Platform.runLater(() -> {
                        if (err == null) {
                            if (LoginPage.loggedInUser != null) {
                                LoginPage.loggedInUser.put("name", userName);
                                LoginPage.loggedInUser.put("phone", userPhone);
                                LoginPage.loggedInUser.putAll(extra);
                            }
                            toggleProfileEdit(false);
                            showToast("Settings updated successfully. Profile changes saved to database.", true);
                        } else {
                            showToast("Failed to update profile in database: " + err.getMessage(), false);
                        }
                    }));
        });

        card.getChildren().addAll(cardHeader, new Separator(), grid);
        return card;
    }

    private void toggleProfileEdit(boolean editing) {
        isEditingProfile = editing;
        nameField.setEditable(editing);
        phoneField.setEditable(editing);

        String activeStyle = editing
                ? "-fx-background-color: white; -fx-border-color: #2563EB; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 13px; -fx-padding: 8 12; -fx-text-fill: #0F172A; -fx-font-weight: bold;"
                : "-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 13px; -fx-padding: 8 12; -fx-text-fill: #1E293B;";

        nameField.setStyle(activeStyle);
        phoneField.setStyle(activeStyle);

        editProfileBtn.setVisible(!editing);
        editProfileBtn.setManaged(!editing);
        saveProfileBtn.setVisible(editing);
        cancelProfileBtn.setVisible(editing);
    }

    // =====================================================
    // 6. SECURITY & PASSWORD MANAGEMENT CARD
    // =====================================================
    private VBox createSecurityCard() {
        VBox card = createCard();

        Label cardTitle = new Label("Security & Password Management");
        cardTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Label cardSub = new Label("Change your authentication password with encrypted backend verification.");
        cardSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        VBox titleBox = new VBox(2, cardTitle, cardSub);

        // Password Inputs
        currentPassField = new PasswordField();
        currentPassField.setPromptText("Enter current password");
        currentPassText = new TextField();
        currentPassText.setPromptText("Enter current password");
        currentPassText.setVisible(false);

        newPassField = new PasswordField();
        newPassField.setPromptText("Enter new password (min 8 characters)");
        newPassText = new TextField();
        newPassText.setPromptText("Enter new password (min 8 characters)");
        newPassText.setVisible(false);

        confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm new password");
        confirmPassText = new TextField();
        confirmPassText.setPromptText("Confirm new password");
        confirmPassText.setVisible(false);

        String passStyle = "-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 13px; -fx-padding: 8 12;";
        currentPassField.setStyle(passStyle);
        currentPassText.setStyle(passStyle);
        newPassField.setStyle(passStyle);
        newPassText.setStyle(passStyle);
        confirmPassField.setStyle(passStyle);
        confirmPassText.setStyle(passStyle);

        // Sync plaintext and password fields
        currentPassField.textProperty().bindBidirectional(currentPassText.textProperty());
        newPassField.textProperty().bindBidirectional(newPassText.textProperty());
        confirmPassField.textProperty().bindBidirectional(confirmPassText.textProperty());

        showPassToggle = new ToggleButton("👁 Show Passwords");
        showPassToggle.setStyle(
                "-fx-background-color: #F1F5F9;" +
                "-fx-text-fill: #475569;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 6 12;" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: #CBD5E1;" +
                "-fx-border-radius: 6;" +
                "-fx-cursor: hand;"
        );
        showPassToggle.setOnAction(e -> {
            boolean show = showPassToggle.isSelected();
            currentPassField.setVisible(!show);
            currentPassText.setVisible(show);
            newPassField.setVisible(!show);
            newPassText.setVisible(show);
            confirmPassField.setVisible(!show);
            confirmPassText.setVisible(show);
            showPassToggle.setText(show ? "🔒 Hide Passwords" : "👁 Show Passwords");
        });

        GridPane passGrid = new GridPane();
        passGrid.setHgap(20);
        passGrid.setVgap(14);

        passGrid.add(createFieldNode("CURRENT PASSWORD *", new StackPane(currentPassField, currentPassText)), 0, 0);
        passGrid.add(createFieldNode("NEW PASSWORD (MIN 8 CHARS) *", new StackPane(newPassField, newPassText)), 1, 0);
        passGrid.add(createFieldNode("CONFIRM NEW PASSWORD *", new StackPane(confirmPassField, confirmPassText)), 2, 0);

        Label policyLabel = new Label("🔒 Policy: Passwords must be at least 8 characters long and match confirmation.");
        policyLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        changePasswordBtn = new Button("🔑  Change Password");
        changePasswordBtn.setStyle(
                "-fx-background-color: #2563EB;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 9 20;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
        );

        changePasswordBtn.setOnAction(e -> {
            String curr = currentPassField.getText().trim();
            String newP = newPassField.getText().trim();
            String conf = confirmPassField.getText().trim();

            if (curr.isEmpty()) {
                showToast("Current password is required.", false);
                return;
            }

            if (newP.isEmpty()) {
                showToast("Please enter a new password.", false);
                return;
            }

            if (newP.length() < 8) {
                showToast("Password must contain at least 8 characters.", false);
                return;
            }

            if (!newP.equals(conf)) {
                showToast("New passwords do not match.", false);
                return;
            }

            // Backend Verification & Password Update
            changePasswordBtn.setDisable(true);
            changePasswordBtn.setText("Updating...");

            authService.signIn(userEmail, curr)
                    .whenComplete((authRes, authErr) -> {
                        if (authErr != null) {
                            Platform.runLater(() -> {
                                changePasswordBtn.setDisable(false);
                                changePasswordBtn.setText("🔑  Change Password");
                                showToast("Current password is incorrect.", false);
                            });
                            return;
                        }

                        // Authenticated successfully, perform password update
                        String uidToUpdate = authRes != null && authRes.getUid() != null ? authRes.getUid() : userUid;
                        authService.updatePassword(uidToUpdate, newP)
                                .whenComplete((unused, updateErr) -> Platform.runLater(() -> {
                                    changePasswordBtn.setDisable(false);
                                    changePasswordBtn.setText("🔑  Change Password");

                                    if (updateErr != null) {
                                        showToast("Failed to update password: " + updateErr.getMessage(), false);
                                    } else {
                                        currentPassField.clear();
                                        currentPassText.clear();
                                        newPassField.clear();
                                        newPassText.clear();
                                        confirmPassField.clear();
                                        confirmPassText.clear();
                                        showToast("Settings updated successfully. Password changed in database authentication.", true);
                                    }
                                }));
                    });
        });

        HBox bottomBar = new HBox(12, policyLabel, space, showPassToggle, changePasswordBtn);
        bottomBar.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(titleBox, new Separator(), passGrid, bottomBar);
        return card;
    }

    // =====================================================
    // 7. NOTIFICATION PREFERENCES CARD
    // =====================================================
    private VBox createNotificationCard() {
        VBox card = createCard();

        Label cardTitle = new Label("Notification Preferences");
        cardTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Label cardSub = new Label("Configure operational alerts and live event notifications for " + userPort + ".");
        cardSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        VBox titleBox = new VBox(2, cardTitle, cardSub);

        // 6 Port Authority Notification Toggles
        vesselArrivalToggle = createStyledToggle(initVesselArrival);
        vesselDepartureToggle = createStyledToggle(initVesselDeparture);
        cargoToggle = createStyledToggle(initCargo);
        containerToggle = createStyledToggle(initContainer);
        paymentToggle = createStyledToggle(initPayment);
        systemToggle = createStyledToggle(initSystem);

        VBox toggleList = new VBox(10);
        toggleList.getChildren().addAll(
                createNotificationRow("🚢  Vessel Arrival Alerts", "Real-time alerts when incoming vessels enter port boundaries or anchorage sectors", vesselArrivalToggle),
                createNotificationRow("⚓  Vessel Departure Alerts", "Notifications when cleared vessels depart from berths or anchorages", vesselDepartureToggle),
                createNotificationRow("📦  Cargo Alerts", "Notifications for new cargo registrations, manifest declarations and customs inspection milestones", cargoToggle),
                createNotificationRow("🏗️  Container Alerts", "Alerts on yard container allocations, depot moves, transfers and shortage alerts", containerToggle),
                createNotificationRow("💳  Payment Notifications", "Real-time updates on tariff invoices, fee payments, and transaction settlement verifications", paymentToggle),
                createNotificationRow("🔔  System Notifications", "Critical infrastructure alerts, weather warnings, and system synchronization notices", systemToggle)
        );

        // Save & Reset Buttons
        Button saveNotificationBtn = new Button("💾  Save Preferences");
        saveNotificationBtn.setStyle(
                "-fx-background-color: #2563EB;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 9 20;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
        );

        Button resetNotificationBtn = new Button("↺  Reset to Saved");
        resetNotificationBtn.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #475569;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 9 18;" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: #CBD5E1;" +
                "-fx-border-radius: 6;" +
                "-fx-cursor: hand;"
        );

        saveNotificationBtn.setOnAction(e -> {
            boolean a1 = vesselArrivalToggle.isSelected();
            boolean a2 = vesselDepartureToggle.isSelected();
            boolean a3 = cargoToggle.isSelected();
            boolean a4 = containerToggle.isSelected();
            boolean a5 = paymentToggle.isSelected();
            boolean a6 = systemToggle.isSelected();

            initVesselArrival = a1;
            initVesselDeparture = a2;
            initCargo = a3;
            initContainer = a4;
            initPayment = a5;
            initSystem = a6;

            Map<String, Object> extra = new HashMap<>();
            extra.put("notifyVesselArrival", a1);
            extra.put("notifyVesselDeparture", a2);
            extra.put("notifyCargo", a3);
            extra.put("notifyContainer", a4);
            extra.put("notifyPayment", a5);
            extra.put("notifySystem", a6);
            extra.put("assignedPort", userPort);
            extra.put("company", userPort);
            extra.put("organization", userPort);
            extra.put("updatedAt", LocalDateTime.now().toString());

            userDao.saveUserExtended(userUid, userEmail, userName, userPort, userPhone, userRole, "Active", extra)
                    .whenComplete((unused, err) -> Platform.runLater(() -> {
                        if (err == null) {
                            if (LoginPage.loggedInUser != null) {
                                LoginPage.loggedInUser.putAll(extra);
                            }
                            showToast("Settings updated successfully. Notification preferences saved.", true);
                        } else {
                            showToast("Failed to save notification preferences: " + err.getMessage(), false);
                        }
                    }));
        });

        resetNotificationBtn.setOnAction(e -> {
            vesselArrivalToggle.setSelected(initVesselArrival);
            updateToggleVisual(vesselArrivalToggle);

            vesselDepartureToggle.setSelected(initVesselDeparture);
            updateToggleVisual(vesselDepartureToggle);

            cargoToggle.setSelected(initCargo);
            updateToggleVisual(cargoToggle);

            containerToggle.setSelected(initContainer);
            updateToggleVisual(containerToggle);

            paymentToggle.setSelected(initPayment);
            updateToggleVisual(paymentToggle);

            systemToggle.setSelected(initSystem);
            updateToggleVisual(systemToggle);

            showToast("Notification toggles reset to last saved database state.", true);
        });

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox buttonBar = new HBox(10, space, resetNotificationBtn, saveNotificationBtn);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(titleBox, new Separator(), toggleList, new Separator(), buttonBar);
        return card;
    }

    private HBox createNotificationRow(String titleText, String descText, ToggleButton toggle) {
        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        Label desc = new Label(descText);
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        VBox copy = new VBox(2, title, desc);
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox row = new HBox(12, copy, space, toggle);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle(
                "-fx-background-color: #F8FAFC;" +
                "-fx-border-color: #E2E8F0;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );
        return row;
    }

    private ToggleButton createStyledToggle(boolean initialVal) {
        ToggleButton toggle = new ToggleButton(initialVal ? "ON" : "OFF");
        toggle.setSelected(initialVal);
        toggle.setPrefWidth(64);
        toggle.setPrefHeight(28);
        updateToggleVisual(toggle);

        toggle.setOnAction(e -> updateToggleVisual(toggle));
        return toggle;
    }

    private void updateToggleVisual(ToggleButton toggle) {
        if (toggle.isSelected()) {
            toggle.setText("ON");
            toggle.setStyle(
                    "-fx-background-color: #2563EB;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 14;" +
                    "-fx-cursor: hand;"
            );
        } else {
            toggle.setText("OFF");
            toggle.setStyle(
                    "-fx-background-color: #E2E8F0;" +
                    "-fx-text-fill: #64748B;" +
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 14;" +
                    "-fx-cursor: hand;"
            );
        }
    }

    // =====================================================
    // 8. HELPER UI BUILDERS
    // =====================================================
    private VBox createCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #E2E8F0;" +
                "-fx-border-radius: 10;"
        );
        return card;
    }

    private VBox createFieldNode(String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B; -fx-font-weight: bold;");

        VBox box = new VBox(5, label, field);
        HBox.setHgrow(box, Priority.ALWAYS);
        GridPane.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private TextField createStyledTextField(String initialValue, boolean editable) {
        TextField field = new TextField(initialValue != null ? initialValue : "");
        field.setEditable(editable);
        field.setStyle(
                "-fx-background-color: #F8FAFC;" +
                "-fx-border-color: #E2E8F0;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 8 12;" +
                "-fx-text-fill: #1E293B;" +
                (editable ? "-fx-font-weight: bold;" : "")
        );
        return field;
    }

    // =====================================================
    // 9. PUBLIC ROOT GETTER
    // =====================================================
    public VBox getContent() {
        return root;
    }
}