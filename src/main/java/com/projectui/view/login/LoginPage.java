package com.projectui.view.login;

import com.projectui.service.FirebaseAuthService;
import com.projectui.view.admin.DockTechAdmin;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.scene.Group;

public class LoginPage extends Application {

        private static final String NAVY = "#020B24";

        static final String[] ROLES = {
                        "Admin", "Port Authority", "Ship Captain",
                        "Customs Officer", "Exporter", "Importer"
        };

        private final FirebaseAuthService authService = new FirebaseAuthService();
        private final com.projectui.dao.UserDao userDao = new com.projectui.dao.UserDao();
        private Stage stage;

        private MediaPlayer backgroundPlayer;

        private static MediaPlayer sharedBackgroundPlayer;
        private static MediaView sharedBackgroundView;

        @Override
        public void start(Stage primaryStage) {
                stage = primaryStage;
                stage.setTitle("Dock Tech - Smart Maritime Operations");
                stage.setMinWidth(1100);
                stage.setMinHeight(700);
                showLoginScene();
                stage.setMaximized(true);
                stage.show();
        }

        public void show(Stage stage) {
                this.stage = stage;
                showLoginScene();
        }

        private void showLoginScene() {
                showRoot(stage, createLoginPage());
        }

        public static void showRoot(Stage stage, Parent root) {
                Scene scene = stage.getScene();
                if (scene == null)
                        stage.setScene(new Scene(root));
                else
                        scene.setRoot(root);
        }

        private Parent createLoginPage() {
                StackPane root = new StackPane();
                root.setStyle("-fx-background-color:" + NAVY + ";");
                addBackground(root);

                HBox layout = new HBox(40);
                layout.setPadding(new Insets(30, 50, 30, 50));
                layout.setAlignment(Pos.CENTER);

                Node left = createLeftSide();
                Node right = createLoginForm();

                HBox.setHgrow(left, Priority.ALWAYS);
                HBox.setHgrow(right, Priority.SOMETIMES);

                layout.getChildren().addAll(left, right);
                root.getChildren().add(layout);

                return root;
        }

        // ================= VIDEO =================

        public static void attachVideoBackground(StackPane root) {
                String path = "/Assets/images/video/DockTechBG.MP4";
                var url = LoginPage.class.getResource(path);

                if (url == null) {
                        System.err.println("Video not found: " + path);
                        return;
                }

                try {
                        if (sharedBackgroundPlayer == null) {
                                Media media = new Media(url.toExternalForm());
                                sharedBackgroundPlayer = new MediaPlayer(media);
                                sharedBackgroundPlayer.setAutoPlay(true);
                                sharedBackgroundPlayer.setMute(true);
                                sharedBackgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                                sharedBackgroundPlayer.setOnReady(
                                                () -> System.out.println("VIDEO READY: " + media.getDuration()));
                                sharedBackgroundPlayer.setOnPlaying(
                                                () -> System.out.println("VIDEO PLAYING"));
                                sharedBackgroundPlayer.play();
                        }

                        if (sharedBackgroundView == null) {
                                sharedBackgroundView = new MediaView(sharedBackgroundPlayer);
                                sharedBackgroundView.setPreserveRatio(false);
                                sharedBackgroundView.setMouseTransparent(true);
                        } else {
                                Parent parent = sharedBackgroundView.getParent();

                                if (parent instanceof Pane pane)
                                        pane.getChildren().remove(sharedBackgroundView);
                                else if (parent instanceof Group group)
                                        group.getChildren().remove(sharedBackgroundView);
                        }

                        sharedBackgroundView.fitWidthProperty().unbind();
                        sharedBackgroundView.fitHeightProperty().unbind();

                        sharedBackgroundView.fitWidthProperty().bind(root.widthProperty());
                        sharedBackgroundView.fitHeightProperty().bind(root.heightProperty());

                        root.getChildren().add(0, sharedBackgroundView);

                        sharedBackgroundPlayer.setOnError(() -> System.err.println(
                                        "VIDEO PLAYER ERROR: " +
                                                        sharedBackgroundPlayer.getError()));

                } catch (Exception e) {
                        System.err.println("VIDEO ERROR: " + e.getMessage());
                }
        }

        private void addBackground(StackPane root) {
                attachVideoBackground(root);

                Region overlay = new Region();
                overlay.setMouseTransparent(true);

                overlay.setStyle(
                                "-fx-background-color:" +
                                                "linear-gradient(" +
                                                "from 0% 0% to 100% 0%," +
                                                "rgba(2,11,36,0.25)," +
                                                "rgba(2,11,36,0.10) 52%," +
                                                "rgba(2,11,36,0.55) 100%);");

                overlay.prefWidthProperty().bind(root.widthProperty());
                overlay.prefHeightProperty().bind(root.heightProperty());

                root.getChildren().add(overlay);
        }

        private void stopBackgroundVideo() {
                if (backgroundPlayer == null)
                        return;

                try {
                        backgroundPlayer.stop();
                        backgroundPlayer.dispose();
                } catch (Exception ignored) {
                }

                backgroundPlayer = null;
        }

        // ================= LEFT SIDE =================

        private Node createLeftSide() {
                VBox content = new VBox(20);
                content.setAlignment(Pos.CENTER_LEFT);
                content.setMaxWidth(640);
                content.setTranslateX(-20);

                Label logo = label(
                                "⚓  DOCK TECH",
                                "white", 30, true);

                Label subtitle = label(
                                "SMART MARITIME OPERATIONS",
                                "#A9D8FF", 12, true);

                VBox logoBox = new VBox(3, logo, subtitle);

                Label badge = new Label("☆  SMART MARITIME OPERATIONS");
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
                                                "⚓", "Smart Ports",
                                                "Real-time port monitoring and management."),
                                createFeature(
                                                "◇", "Cargo Visibility",
                                                "End-to-end cargo tracking and transparency."),
                                createFeature(
                                                "✓", "Secure Operations",
                                                "Enterprise-grade security and data protection."));

                content.getChildren().addAll(
                                logoBox, badge, title, description, features);

                return content;
        }

        // ================= LOGIN FORM =================

        private VBox createLoginForm() {
                VBox container = new VBox();
                container.setAlignment(Pos.CENTER);
                container.setPadding(new Insets(40));
                container.setMaxWidth(540);

                VBox card = new VBox(16);
                card.setMaxWidth(520);
                card.setPadding(new Insets(32));
                card.setStyle(cardStyle());

                Label title = label("Welcome Back", "white", 36, true);

                Label description = new Label(
                                "Sign in to continue managing port operations,\n" +
                                                "cargo visibility and enterprise workflows.");

                description.setStyle(
                                "-fx-text-fill:#C7DDF0;" +
                                                "-fx-font-size:14px;" +
                                                "-fx-line-spacing:3px;");

                TextField email = createTextField("name@company.com");
                PasswordControl password = new PasswordControl("Enter your password");

                ComboBox<String> role = createRoleBox();
                role.setValue("Admin");

                CheckBox remember = new CheckBox("Remember me");
                remember.setStyle(
                                "-fx-text-fill:#C8D9EA;" +
                                                "-fx-font-size:13px;" +
                                                "-fx-font-weight:bold;");

                Button forgot = createLink("Forgot Password?");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox options = new HBox(
                                8, remember, spacer, forgot);
                options.setAlignment(Pos.CENTER_LEFT);

                Label message = createMessage();

                Button login = createPrimaryButton("LOGIN");
                login.setMaxWidth(Double.MAX_VALUE);

                login.setOnAction(e -> signIn(email, password, role, login, message));

                Label accountText = label(
                                "Don't have an account?",
                                "#C8D9EA", 13, false);

                Button createAccount = createLink("Create Account");
                createAccount.setOnAction(
                                e -> new CreateAccount().show(stage));

                forgot.setOnAction(
                                e -> new ForgotPassword().show(stage));

                HBox accountRow = new HBox(
                                6, accountText, createAccount);
                accountRow.setAlignment(Pos.CENTER);

                Button support = createFooterLink("Help & Support");
                Button privacy = createFooterLink("Privacy Policy");

                Label separator = label(
                                "|", "rgba(190,220,240,0.35)", 11, false);

                HBox footer = new HBox(
                                15, support, separator, privacy);
                footer.setAlignment(Pos.CENTER);

                card.getChildren().addAll(
                                title,
                                description,
                                createLabeledField("Email", email),
                                createLabeledField("Password", password.root),
                                createLabeledField("Role", role),
                                options,
                                message,
                                login,
                                accountRow,
                                footer);

                container.getChildren().add(card);
                return container;
        }

        // ================= LOGIN =================

        private void signIn(
                        TextField email,
                        PasswordControl password,
                        ComboBox<String> role,
                        Button login,
                        Label message) {
                String emailValue = email.getText().trim();
                String passwordValue = password.getText();

                if (!isEmail(emailValue)) {
                        showMessage(
                                        message,
                                        "Please enter a valid email address.",
                                        "#FF9696");
                        return;
                }

                if (passwordValue.isBlank()) {
                        showMessage(
                                        message,
                                        "Please enter your password.",
                                        "#FF9696");
                        return;
                }

                if (role.getValue() == null) {
                        showMessage(
                                        message,
                                        "Please select a role.",
                                        "#FF9696");
                        return;
                }

                login.setDisable(true);
                showMessage(message, "Signing in...", "#AFC6D9");

                authService.signIn(emailValue, passwordValue, role.getValue()).whenComplete((authUser, authError) -> Platform.runLater(() -> {
                        if (authError != null) {
                                login.setDisable(false);
                                Throwable actualError = authError.getCause() == null ? authError : authError.getCause();
                                showMessage(message, actualError == null ? "Unable to authenticate. Please check your connection and try again." : actualError.getMessage(), "#FF9696");
                                return;
                        }

                        if (authUser == null || authUser.getUid() == null) {
                                login.setDisable(false);
                                showMessage(message, "Unable to authenticate. Please check your connection and try again.", "#FF9696");
                                return;
                        }

                        System.out.println("[AUTH LOGIN] email=" + emailValue + ", uid=" + authUser.getUid());

                        userDao.getUserProfile(authUser.getUid()).whenComplete((profile, storeError) -> Platform.runLater(() -> {
                                login.setDisable(false);

                                if (storeError != null) {
                                        Throwable actualStoreError = storeError.getCause() == null ? storeError : storeError.getCause();
                                        showMessage(message, actualStoreError != null ? actualStoreError.getMessage() : "Account authentication succeeded, but user profile could not be read.", "#FF9696");
                                        return;
                                }

                                String selectedRole = role.getValue();

                                if (profile == null) {
                                        showMessage(message, "User profile not found in database.", "#FF9696");
                                        return;
                                }

                                Object roleObj = profile.get("role");
                                String firestoreRole = roleObj != null ? roleObj.toString() : null;

                                Object statusObj = profile.get("status");
                                String firestoreStatus = statusObj != null ? statusObj.toString() : null;

                                if (firestoreRole == null || !firestoreRole.equalsIgnoreCase(selectedRole)) {
                                        showMessage(message, "Selected role does not match this account.", "#FF9696");
                                        return;
                                }

                                if (firestoreStatus == null || (!firestoreStatus.equalsIgnoreCase("Active") && !firestoreStatus.equalsIgnoreCase("Active & Verified"))) {
                                        showMessage(message, "Your account is inactive. Please contact the administrator.", "#FF9696");
                                        return;
                                }

                                showAdmin();
                        }));
                }));
        }

        // ================= ADMIN =================

        private void showAdmin() {
                Parent root = new DockTechAdmin().getRoot(
                                this::showLoginScene,
                                "Dashboard");

                showRoot(stage, root);
        }

        // ================= FEATURE =================

        private VBox createFeature(
                        String icon,
                        String title,
                        String description) {
                Label iconLabel = new Label(icon);
                iconLabel.setMinSize(44, 44);
                iconLabel.setAlignment(Pos.CENTER);

                iconLabel.setStyle(
                                "-fx-text-fill:#A7E8FF;" +
                                                "-fx-background-color:rgba(0,180,255,0.12);" +
                                                "-fx-background-radius:22px;" +
                                                "-fx-border-color:rgba(0,207,255,0.35);" +
                                                "-fx-border-radius:22px;" +
                                                "-fx-font-size:18px;");

                Label titleLabel = label(
                                title, "white", 15, true);

                Label descriptionLabel = label(
                                description, "#B9D0E6", 12, false);

                VBox text = new VBox(
                                3, titleLabel, descriptionLabel);

                HBox row = new HBox(
                                12, iconLabel, text);
                row.setAlignment(Pos.CENTER_LEFT);

                VBox box = new VBox(row);
                box.setPadding(new Insets(12));

                box.setStyle(
                                "-fx-background-color:rgba(2,19,43,0.38);" +
                                                "-fx-background-radius:9px;" +
                                                "-fx-border-color:rgba(160,220,255,0.14);" +
                                                "-fx-border-radius:9px;");

                return box;
        }

        // ================= FIELDS =================

        private TextField createTextField(String prompt) {
                TextField field = new TextField();
                field.setPromptText(prompt);
                field.setMaxWidth(Double.MAX_VALUE);
                field.setStyle(fieldStyle());
                return field;
        }

        private ComboBox<String> createRoleBox() {
                ComboBox<String> role = new ComboBox<>();
                role.getItems().addAll(ROLES);
                role.setMaxWidth(Double.MAX_VALUE);
                role.setPrefHeight(44);
                role.setStyle(fieldStyle() + "-fx-padding:0 14px;");

                role.setButtonCell(createRoleCell());
                role.setCellFactory(list -> createRoleCell());

                return role;
        }

        private ListCell<String> createRoleCell() {
                ListCell<String> cell = new ListCell<>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(empty ? null : "♙  " + item);
                        }
                };

                cell.setStyle(
                                "-fx-background-color:#071A31;" +
                                                "-fx-text-fill:white;" +
                                                "-fx-font-size:14px;" +
                                                "-fx-padding:10px 12px;");

                return cell;
        }

        private VBox createLabeledField(
                        String text,
                        Parent field) {
                return new VBox(
                                6,
                                label(text, "#D8E8F7", 13, true),
                                field);
        }

        // ================= BUTTONS =================

        private Button createPrimaryButton(String text) {
                Button button = new Button(text);

                button.setStyle(
                                "-fx-background-color:linear-gradient(to right,#0F66D0,#23A8FF);" +
                                                "-fx-text-fill:white;" +
                                                "-fx-font-size:15px;" +
                                                "-fx-font-weight:700;" +
                                                "-fx-padding:14px 22px;" +
                                                "-fx-background-radius:12px;" +
                                                "-fx-cursor:hand;" +
                                                "-fx-effect:dropshadow(gaussian,rgba(35,170,255,0.18),18,0,0,6);");

                return button;
        }

        private Button createLink(String text) {
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

        private Button createFooterLink(String text) {
                Button button = createLink(text);

                button.setStyle(
                                "-fx-background-color:transparent;" +
                                                "-fx-text-fill:#B7CCDE;" +
                                                "-fx-font-size:11px;" +
                                                "-fx-padding:0;" +
                                                "-fx-cursor:hand;");

                return button;
        }

        // ================= MESSAGES =================

        private Label createMessage() {
                Label label = new Label();
                label.setWrapText(true);
                label.setMaxWidth(Double.MAX_VALUE);
                return label;
        }

        private static void showMessage(
                        Label label,
                        String text,
                        String color) {
                label.setStyle(
                                "-fx-text-fill:" + color + ";" +
                                                "-fx-font-size:10px;" +
                                                "-fx-font-weight:bold;");

                label.setText(
                                text == null || text.isBlank()
                                                ? "Unable to complete the request."
                                                : text);
        }

        // ================= VALIDATION =================

        static boolean isEmail(String value) {
                return value != null &&
                                value.matches(
                                                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        }

        static boolean isBackendUnavailable(Throwable error) {
                while (error != null) {
                        if (error instanceof UnsupportedOperationException)
                                return true;

                        error = error.getCause();
                }

                return false;
        }

        // ================= STYLES =================

        private static Label label(
                        String text,
                        String color,
                        int size,
                        boolean bold) {
                Label label = new Label(text);

                label.setStyle(
                                "-fx-text-fill:" + color + ";" +
                                                "-fx-font-size:" + size + "px;" +
                                                (bold ? "-fx-font-weight:bold;" : ""));

                return label;
        }

        static String fieldStyle() {
                return "-fx-background-color:rgba(6,18,34,0.48);" +
                                "-fx-text-fill:white;" +
                                "-fx-prompt-text-fill:#7F9BB5;" +
                                "-fx-border-color:rgba(80,190,255,0.28);" +
                                "-fx-border-width:1;" +
                                "-fx-border-radius:10px;" +
                                "-fx-background-radius:10px;" +
                                "-fx-padding:12px 14px;" +
                                "-fx-font-size:14px;";
        }

        static String cardStyle() {
                return "-fx-background-color:rgba(4,18,36,0.50);" +
                                "-fx-background-radius:18px;" +
                                "-fx-border-radius:18px;" +
                                "-fx-border-color:rgba(64,190,255,0.28);" +
                                "-fx-border-width:1;" +
                                "-fx-effect:dropshadow(gaussian,rgba(64,190,255,0.08),24,0,0,8);";
        }

        // ================= IMAGE FALLBACK =================

        static Image loadBackground() {
                try {
                        return new Image(
                                        LoginPage.class
                                                        .getResource(
                                                                        "/Assets/images/Background.PNG")
                                                        .toExternalForm());
                } catch (Exception e) {
                        return null;
                }
        }

        // ================= PASSWORD =================

        static class PasswordControl {

                final StackPane root = new StackPane();

                private final PasswordField passwordField = new PasswordField();

                private final TextField textField = new TextField();

                private final Button toggle = createToggleButton("Show");

                private boolean showing;

                PasswordControl(String prompt) {

                        passwordField.setPromptText(prompt);
                        textField.setPromptText(prompt);

                        passwordField.setStyle(
                                        fieldStyle() +
                                                        "-fx-padding:12px 64px 12px 14px;");

                        textField.setStyle(
                                        fieldStyle() +
                                                        "-fx-padding:12px 64px 12px 14px;");

                        textField.textProperty()
                                        .bindBidirectional(
                                                        passwordField.textProperty());

                        textField.setVisible(false);
                        textField.setManaged(false);

                        BorderPane layer = new BorderPane();
                        layer.setMouseTransparent(true);
                        layer.setRight(toggle);

                        BorderPane.setAlignment(
                                        toggle,
                                        Pos.CENTER_RIGHT);

                        BorderPane.setMargin(
                                        toggle,
                                        new Insets(0, 10, 0, 0));

                        toggle.setMouseTransparent(false);
                        toggle.setOnAction(e -> togglePassword());

                        root.getChildren().addAll(
                                        passwordField,
                                        textField,
                                        layer);
                }

                String getText() {
                        return passwordField.getText();
                }

                private void togglePassword() {
                        showing = !showing;

                        textField.setVisible(showing);
                        textField.setManaged(showing);

                        passwordField.setVisible(!showing);
                        passwordField.setManaged(!showing);

                        toggle.setText(showing ? "Hide" : "Show");

                        Node active = showing
                                        ? textField
                                        : passwordField;

                        active.requestFocus();

                        if (active instanceof TextInputControl input)
                                input.positionCaret(
                                                input.getText().length());
                }

                private static Button createToggleButton(String text) {
                        Button button = new Button(text);

                        button.setStyle(
                                        "-fx-background-color:transparent;" +
                                                        "-fx-text-fill:#66CFFF;" +
                                                        "-fx-font-size:12px;" +
                                                        "-fx-font-weight:bold;" +
                                                        "-fx-padding:0;" +
                                                        "-fx-cursor:hand;");

                        return button;
                }
        }
}