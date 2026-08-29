package com.projectui.view.login;

import java.util.HashMap;
import java.util.Map;

import com.projectui.dao.UserDao;
import com.projectui.service.FirebaseAuthService;
import com.projectui.view.admin.AdminDataStore;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@SuppressWarnings({"unused", "unchecked"})
public class CreateAccount {

    private final FirebaseAuthService authService = new FirebaseAuthService();
    private final UserDao userDao = new UserDao();
    private Stage stage;

    public void show(Stage stage) {
        this.stage = stage;
        LoginPage.showRoot(stage, authRoot(formPane()));
    }

    // =====================================================
    // CREATE ACCOUNT FORM
    // =====================================================

    private VBox formPane() {

        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40));
        container.setMaxWidth(540);

        VBox card = new VBox(16);
        card.setMaxWidth(520);
        card.setPadding(new Insets(32));
        card.setStyle(LoginPage.cardStyle());

        Button back = createLink("←  Back to Login");
        back.setOnAction(event -> new LoginPage().show(stage));

        // Form Mode Selector (User Registration vs Request New Port)
        HBox modeSelector = new HBox(8);
        modeSelector.setAlignment(Pos.CENTER);
        modeSelector.setPadding(new Insets(0, 0, 8, 0));

        Button modeUserBtn = new Button("👤 User Account");
        Button modePortBtn = new Button("⚓ Request New Port");

        String activeStyle = "-fx-background-color: #0F66D0; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-background-radius: 20px; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #C7DDF0; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-background-radius: 20px; -fx-cursor: hand;";

        modeUserBtn.setStyle(activeStyle);
        modePortBtn.setStyle(inactiveStyle);

        modeSelector.getChildren().addAll(modeUserBtn, modePortBtn);

        Label title = new Label("Create Account");
        title.setStyle(
                "-fx-text-fill:white;" +
                        "-fx-font-size:32px;" +
                        "-fx-font-weight:700;");

        Label description = new Label(
                "Please fill in the details to create your account.");

        description.setWrapText(true);
        description.setStyle(
                "-fx-text-fill:#C7DDF0;" +
                        "-fx-font-size:13px;" +
                        "-fx-line-spacing:3px;");

        // Standard User Account Fields
        TextField fullName = createTextField("Full Name");
        TextField email = createTextField("name@company.com");
        TextField company = createTextField("Company Name");
        ComboBox<String> role = createRoleBox();
        TextField phone = createTextField("Phone Number");
        role.setValue("Exporter");

        LoginPage.PasswordControl password = new LoginPage.PasswordControl("Create password");
        LoginPage.PasswordControl confirm = new LoginPage.PasswordControl("Confirm password");
        CheckBox terms = new CheckBox("I agree to the Terms of Service and Privacy Policy");
        terms.setWrapText(true);
        terms.setStyle("-fx-text-fill:#C8D9EA; -fx-font-size:13px; -fx-font-weight:bold;");

        // Port Request Fields
        TextField reqPortName = createTextField("Port Name (e.g. Port of Hazira)");
        TextField reqPortCode = createTextField("Port Code (e.g. IN-HAZ-01)");
        TextField reqLocation = createTextField("Location (e.g. Hazira, Gujarat)");
        TextField reqAddress = createTextField("Port Address & Details");
        TextField reqName = createTextField("Requester Full Name");
        TextField reqEmail = createTextField("Requester Email Address");
        TextField reqPhone = createTextField("Requester Phone Number");
        TextField reqPaName = createTextField("Proposed Port Authority Name");
        TextField reqPaEmail = createTextField("Proposed Port Authority Email");

        VBox userFormBox = new VBox(12,
                createLabeledField("Full Name", fullName),
                createLabeledField("Email", email),
                createLabeledField("Company Name", company),
                createLabeledField("Role", role),
                createLabeledField("Phone Number", phone),
                createLabeledField("Password", password.root),
                createLabeledField("Confirm Password", confirm.root),
                terms
        );

        VBox portFormBox = new VBox(12,
                createLabeledField("Port Name *", reqPortName),
                createLabeledField("Port Code", reqPortCode),
                createLabeledField("Location *", reqLocation),
                createLabeledField("Address & Details", reqAddress),
                createLabeledField("Requester Name *", reqName),
                createLabeledField("Requester Email *", reqEmail),
                createLabeledField("Requester Phone *", reqPhone),
                createLabeledField("Proposed Port Authority Name *", reqPaName),
                createLabeledField("Proposed Port Authority Email *", reqPaEmail)
        );

        portFormBox.setManaged(false);
        portFormBox.setVisible(false);

        Label message = createMessage();
        Button create = createPrimaryButton("CREATE ACCOUNT");
        create.setMaxWidth(Double.MAX_VALUE);

        // Toggle Event Handlers
        modeUserBtn.setOnAction(e -> {
            modeUserBtn.setStyle(activeStyle);
            modePortBtn.setStyle(inactiveStyle);
            title.setText("Create Account");
            description.setText("Please fill in the details to create your account.");
            userFormBox.setManaged(true);
            userFormBox.setVisible(true);
            portFormBox.setManaged(false);
            portFormBox.setVisible(false);
            create.setText("CREATE ACCOUNT");
            message.setText("");
        });

        modePortBtn.setOnAction(e -> {
            new RequestNewPortPage().show(stage);
        });

        create.setOnAction(event -> {
            if (userFormBox.isVisible()) {
                createAccount(fullName, email, company, role, phone, password, confirm, terms, create, message);
            } else {
                submitPortRequest(reqPortName, reqPortCode, reqLocation, reqAddress, reqName, reqEmail, reqPhone, reqPaName, reqPaEmail, create, message);
            }
        });

        Label existing = new Label("Already have an account?");
        existing.setStyle("-fx-text-fill:#C8D9EA; -fx-font-size:13px;");
        Button login = createLink("Login");
        login.setOnAction(event -> new LoginPage().show(stage));

        HBox loginRow = new HBox(6, existing, login);
        loginRow.setAlignment(Pos.CENTER);

        Button support = createFooterLink("Help & Support");
        Button privacy = createFooterLink("Privacy Policy");
        Label separator = new Label("|");
        separator.setStyle("-fx-text-fill:rgba(190,220,240,0.35); -fx-font-size:11px;");
        HBox footer = new HBox(15, support, separator, privacy);
        footer.setAlignment(Pos.CENTER);

        card.getChildren().addAll(
                back,
                modeSelector,
                title,
                description,
                userFormBox,
                portFormBox,
                message,
                create,
                loginRow,
                footer
        );

        container.getChildren().add(card);
        return container;
    }

    // =====================================================
    // CREATE ACCOUNT LOGIC
    // =====================================================

    private void createAccount(
            TextField fullName,
            TextField email,
            TextField company,
            ComboBox<String> role,
            TextField phone,
            LoginPage.PasswordControl password,
            LoginPage.PasswordControl confirm,
            CheckBox terms,
            Button create,
            Label message) {

        String nameVal = fullName.getText().trim();
        String emailVal = email.getText().trim();
        String companyVal = company.getText().trim();
        String roleVal = role.getValue();
        String phoneVal = phone.getText().trim();
        String passVal = password.getText();

        if (nameVal.isEmpty()) {
            showError(message, "Name is required.");
            return;
        }

        if (emailVal.isEmpty()) {
            showError(message, "Email is required.");
            return;
        }

        if (!LoginPage.isEmail(emailVal)) {
            showError(message, "Please enter a valid email address.");
            return;
        }

        if (companyVal.isEmpty()) {
            showError(message, "Company is required.");
            return;
        }

        if (roleVal == null || roleVal.isBlank()) {
            showError(message, "Please select a role.");
            return;
        }

        if (!UserDao.isRoleCreationAllowed("Public", roleVal)) {
            showError(message, "Privileged roles cannot be created via public registration.");
            return;
        }

        if (!phoneVal.matches("^[0-9+()\\-\\s]{7,20}$")) {
            showError(message, "Please enter a valid phone number.");
            return;
        }

        if (passVal.isBlank()) {
            showError(message, "Password is required.");
            return;
        }

        if (passVal.length() < 6) {
            showError(message, "Password must be at least 6 characters.");
            return;
        }

        if (!passVal.equals(confirm.getText())) {
            showError(message, "Passwords do not match.");
            return;
        }

        if (!terms.isSelected()) {
            showError(message, "Please accept the Terms of Service and Privacy Policy.");
            return;
        }

        create.setDisable(true);
        showInfo(message, "Creating account...");

        authService.createAccount(emailVal, passVal, nameVal, roleVal).whenComplete((authUser, authError) -> Platform.runLater(() -> {
            if (authError != null && !LoginPage.isBackendUnavailable(authError)) {
                Throwable actual = authError.getCause() == null ? authError : authError.getCause();
                String errText = actual == null ? "Failed to create account." : actual.getMessage();
                if (errText != null && (errText.toLowerCase().contains("email_exists") || errText.toLowerCase().contains("already exists"))) {
                    create.setDisable(false);
                    showError(message, "An account with this email address already exists.");
                    return;
                }
            }

            String uid = (authUser != null && authUser.getUid() != null && !authUser.getUid().isBlank())
                    ? authUser.getUid()
                    : "usr_" + Math.abs(emailVal.toLowerCase().hashCode());

            // Prepare extra metadata fields based on selected role
            Map<String, Object> extra = new HashMap<>();

            if ("Custom Officer".equalsIgnoreCase(roleVal)) {
                extra.put("officerId", "CO-" + String.format("%03d", (int)(Math.random() * 899 + 100)));
                extra.put("duty", "Customs Clearance & Inspection");
                extra.put("assignmentStatus", "Pending Assignment");
                extra.put("assignedPort", "");
            } else if ("Ship Captain".equalsIgnoreCase(roleVal)) {
                extra.put("captainId", "CAP-" + String.format("%03d", (int)(Math.random() * 899 + 100)));
                extra.put("duty", "Navigation & Command");
                extra.put("assignmentStatus", "Pending Assignment");
                extra.put("assignedPort", "");
                extra.put("assignedShip", "");
            } else {
                extra.put("assignmentStatus", "Active");
            }

            userDao.saveUserExtended(uid, emailVal, nameVal, companyVal, phoneVal, roleVal, "Active", extra).whenComplete((unused, storeError) -> Platform.runLater(() -> {
                if (storeError != null) {
                    create.setDisable(false);
                    showError(message, "Account authentication was created, but the user profile could not be saved.");
                    return;
                }

                AdminDataStore.getInstance().users.add(0, new AdminDataStore.UserAccount(
                        nameVal, emailVal, roleVal, "Active", "Today", "Just now"
                ));

                message.setStyle("-fx-text-fill:#76E6B1; -fx-font-size:12px; -fx-font-weight:bold;");
                message.setText("Account created successfully.");

                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                pause.setOnFinished(evt -> new LoginPage().show(stage));
                pause.play();
            }));
        }));
    }

    private void submitPortRequest(
            TextField reqPortName,
            TextField reqPortCode,
            TextField reqLocation,
            TextField reqAddress,
            TextField reqName,
            TextField reqEmail,
            TextField reqPhone,
            TextField reqPaName,
            TextField reqPaEmail,
            Button submitBtn,
            Label message) {

        String portName = reqPortName.getText().trim();
        String portCode = reqPortCode.getText().trim();
        String location = reqLocation.getText().trim();
        String address = reqAddress.getText().trim();
        String name = reqName.getText().trim();
        String email = reqEmail.getText().trim();
        String phone = reqPhone.getText().trim();
        String paName = reqPaName.getText().trim();
        String paEmail = reqPaEmail.getText().trim();

        if (portName.isEmpty()) {
            showError(message, "Port name is required.");
            return;
        }
        if (location.isEmpty()) {
            showError(message, "Port location is required.");
            return;
        }
        if (name.isEmpty()) {
            showError(message, "Requester name is required.");
            return;
        }
        if (email.isEmpty() || !LoginPage.isEmail(email)) {
            showError(message, "Valid requester email is required.");
            return;
        }
        if (phone.isEmpty()) {
            showError(message, "Requester phone number is required.");
            return;
        }
        if (paName.isEmpty()) {
            showError(message, "Proposed Port Authority name is required.");
            return;
        }
        if (paEmail.isEmpty() || !LoginPage.isEmail(paEmail)) {
            showError(message, "Valid proposed Port Authority email is required.");
            return;
        }

        String reqId = "REQ-" + (int)(Math.random() * 899 + 100);
        String code = portCode.isEmpty() ? "IN-" + portName.replaceAll("[^A-Za-z]", "").toUpperCase().substring(0, Math.min(3, portName.length())) + "-01" : portCode;

        new com.projectui.dao.PortRequestDao().savePortRequestFull(
                reqId, portName, code, location, address, "New Port Proposal",
                name, email, phone, paName, paEmail, "Aadhaar_Card.pdf, Port_Establishment_Proposal.pdf",
                "Aadhaar_Card.pdf", "https://res.cloudinary.com/docktech/image/upload/docktech_port_documents/Aadhaar_Card.pdf", "doc_aadhaar_" + System.currentTimeMillis(), "pdf", "pdf", "",
                "Port_Establishment_Proposal.pdf", "https://res.cloudinary.com/docktech/image/upload/docktech_port_documents/Port_Establishment_Proposal.pdf", "doc_proposal_" + System.currentTimeMillis(), "pdf", "pdf", ""
        );

        message.setStyle("-fx-text-fill:#76E6B1; -fx-font-size:12px; -fx-font-weight:bold;");
        message.setText("✓ Port Request (" + reqId + ") submitted successfully! Status: PENDING — Awaiting Dock Tech Admin review.");

        submitBtn.setDisable(true);
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2.5));
        pause.setOnFinished(evt -> new LoginPage().show(stage));
        pause.play();
    }

    // =====================================================
    // AUTHENTICATION SHELL
    // SAME AS LOGIN PAGE
    // =====================================================

    private static Parent authRoot(
            VBox content) {

        StackPane root = new StackPane();

        root.setStyle(
                "-fx-background-color:#020B24;");

        // DO NOT CHANGE VIDEO BACKGROUND
        LoginPage.attachVideoBackground(root);

        HBox mainLayout = new HBox();

        mainLayout.setPadding(
                new Insets(
                        30,
                        50,
                        30,
                        50));

        mainLayout.setSpacing(40);

        mainLayout.setAlignment(
                Pos.CENTER);

        // =================================================
        // LEFT SIDE
        // EXACT LOGIN PAGE SIZING
        // =================================================

        VBox left = new VBox(20);

        left.setAlignment(
                Pos.CENTER_LEFT);

        left.setMaxWidth(640);
        left.setTranslateX(-20);

        Label logo = new Label(
                "⚓  DOCK TECH");

        logo.setStyle(
                "-fx-text-fill:white;" +
                        "-fx-font-size:30px;" +
                        "-fx-font-weight:bold;");

        Label subtitle = new Label(
                "SMART MARITIME OPERATIONS");

        subtitle.setStyle(
                "-fx-text-fill:#A9D8FF;" +
                        "-fx-font-size:12px;" +
                        "-fx-font-weight:bold;");

        VBox logoBox = new VBox(
                3,
                logo,
                subtitle);

        Label badge = new Label(
                "☆  SMART MARITIME OPERATIONS");

        badge.setStyle(
                "-fx-text-fill:#BDEBFF;" +
                        "-fx-background-color:rgba(0,184,255,0.13);" +
                        "-fx-background-radius:18px;" +
                        "-fx-border-color:rgba(100,220,255,0.38);" +
                        "-fx-border-radius:18px;" +
                        "-fx-padding:8px 14px;" +
                        "-fx-font-size:12px;" +
                        "-fx-font-weight:bold;");

        Label title = new Label(
                "Dock Tech\n" +
                        "Intelligent Port & Cargo\n" +
                        "Operations Platform");

        title.setStyle(
                "-fx-text-fill:white;" +
                        "-fx-font-size:42px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-line-spacing:2px;");

        Label description = new Label(
                "Connect ports, vessels, cargo and enterprise teams\n" +
                        "through one secure & intelligent workspace.");

        description.setStyle(
                "-fx-text-fill:#D7E9F9;" +
                        "-fx-font-size:15px;" +
                        "-fx-line-spacing:4px;");

        VBox features = new VBox(12);

        features.setMaxWidth(320);

        features.getChildren().addAll(
                createFeature(
                        "⚓",
                        "Smart Ports",
                        "Real-time port monitoring and management."),

                createFeature(
                        "◇",
                        "Cargo Visibility",
                        "End-to-end cargo tracking and transparency."),

                createFeature(
                        "✓",
                        "Secure Operations",
                        "Enterprise-grade security and data protection."));

        left.getChildren().addAll(
                logoBox,
                badge,
                title,
                description,
                features);

        // =================================================
        // RIGHT SIDE
        // SAME LOGIN ALIGNMENT
        // =================================================

        Node right = content;

        HBox.setHgrow(
                left,
                Priority.ALWAYS);

        HBox.setHgrow(
                right,
                Priority.SOMETIMES);

        mainLayout.getChildren().addAll(
                left,
                right);

        root.getChildren().add(
                mainLayout);

        return root;
    }

    // =====================================================
    // TEXT FIELD
    // SAME LOGIN STYLE
    // =====================================================

    private static TextField createTextField(
            String prompt) {

        TextField field = new TextField();

        field.setPromptText(prompt);

        field.setMaxWidth(
                Double.MAX_VALUE);

        field.setStyle(
                LoginPage.fieldStyle());

        return field;
    }

    // =====================================================
    // ROLE
    // SAME LOGIN STYLE
    // =====================================================

    private static ComboBox<String> createRoleBox() {

        ComboBox<String> role = new ComboBox<>();

        role.getItems().addAll("Exporter", "Importer", "Custom Officer", "Ship Captain");

        role.setMaxWidth(
                Double.MAX_VALUE);

        role.setPrefHeight(44);

        role.setStyle(
                LoginPage.fieldStyle() +
                        "-fx-padding:0 14;");

        role.setButtonCell(
                createRoleCell());

        role.setCellFactory(
                list -> createRoleCell());

        return role;
    }

    private static javafx.scene.control.ListCell<String> createRoleCell() {

        javafx.scene.control.ListCell<String> cell = new javafx.scene.control.ListCell<>() {

            @Override
            protected void updateItem(
                    String item,
                    boolean empty) {

                super.updateItem(
                        item,
                        empty);

                setText(
                        empty
                                ? null
                                : "♙  " + item);
            }
        };

        cell.setStyle(
                "-fx-background-color:#071A31;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:14px;" +
                        "-fx-padding:10px 12px;");

        return cell;
    }

    // =====================================================
    // LABEL
    // SAME LOGIN SIZE
    // =====================================================

    private static VBox createLabeledField(
            String labelText,
            Node field) {

        Label label = new Label(
                labelText);

        label.setStyle(
                "-fx-text-fill:#D8E8F7;" +
                        "-fx-font-size:13px;" +
                        "-fx-font-weight:bold;");

        return new VBox(
                6,
                label,
                field);
    }

    // =====================================================
    // BUTTONS
    // SAME LOGIN STYLE
    // =====================================================

    private static Button createPrimaryButton(
            String text) {

        Button button = new Button(text);

        button.setStyle(
                "-fx-background-color:" +
                        "linear-gradient(to right,#0F66D0,#23A8FF);" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:15px;" +
                        "-fx-font-weight:700;" +
                        "-fx-padding:14px 22px;" +
                        "-fx-background-radius:12px;" +
                        "-fx-cursor:hand;" +
                        "-fx-effect:dropshadow(" +
                        "gaussian," +
                        "rgba(35,170,255,0.18)," +
                        "18,0,0,6);");

        return button;
    }

    private static Button createLink(
            String text) {

        Button button = new Button(text);

        button.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-text-fill:#66CFFF;" +
                        "-fx-font-size:13px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-padding:0;" +
                        "-fx-cursor:hand;");

        return button;
    }

    private static Button createFooterLink(
            String text) {

        Button button = createLink(text);

        button.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-text-fill:#B7CCDE;" +
                        "-fx-font-size:11px;" +
                        "-fx-padding:0;" +
                        "-fx-cursor:hand;");

        return button;
    }

    // =====================================================
    // MESSAGE
    // =====================================================

    private static Label createMessage() {

        Label label = new Label();

        label.setWrapText(true);

        label.setMaxWidth(
                Double.MAX_VALUE);

        label.setStyle(
                "-fx-text-fill:#FF9696;" +
                        "-fx-font-size:10px;" +
                        "-fx-font-weight:bold;");

        return label;
    }

    private static void showError(
            Label label,
            String text) {

        label.setStyle(
                "-fx-text-fill:#FF9696;" +
                        "-fx-font-size:10px;" +
                        "-fx-font-weight:bold;");

        label.setText(
                text == null || text.isBlank()
                        ? "Unable to complete the request."
                        : text);
    }

    private static void showInfo(
            Label label,
            String text) {

        label.setStyle(
                "-fx-text-fill:#AFC6D9;" +
                        "-fx-font-size:10px;" +
                        "-fx-font-weight:bold;");

        label.setText(text);
    }

    // =====================================================
    // FEATURE CARDS
    // SAME LOGIN PAGE
    // =====================================================

    private static VBox createFeature(
            String icon,
            String title,
            String description) {

        Label iconLabel = new Label(icon);

        iconLabel.setMinSize(
                44,
                44);

        iconLabel.setAlignment(
                Pos.CENTER);

        iconLabel.setStyle(
                "-fx-text-fill:#A7E8FF;" +
                        "-fx-background-color:rgba(0,180,255,0.12);" +
                        "-fx-background-radius:22px;" +
                        "-fx-border-color:rgba(0,207,255,0.35);" +
                        "-fx-border-radius:22px;" +
                        "-fx-font-size:18px;");

        Label titleLabel = new Label(title);

        titleLabel.setStyle(
                "-fx-text-fill:white;" +
                        "-fx-font-size:15px;" +
                        "-fx-font-weight:bold;");

        Label descriptionLabel = new Label(description);

        descriptionLabel.setStyle(
                "-fx-text-fill:#B9D0E6;" +
                        "-fx-font-size:12px;");

        VBox text = new VBox(
                3,
                titleLabel,
                descriptionLabel);

        HBox row = new HBox(
                12,
                iconLabel,
                text);

        row.setAlignment(
                Pos.CENTER_LEFT);

        VBox box = new VBox(row);

        box.setPadding(
                new Insets(12));

        box.setStyle(
                "-fx-background-color:rgba(2,19,43,0.38);" +
                        "-fx-background-radius:9px;" +
                        "-fx-border-color:rgba(160,220,255,0.14);" +
                        "-fx-border-radius:9px;");

        return box;
    }
}