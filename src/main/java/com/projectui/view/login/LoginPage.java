package com.projectui.view.login;

import java.util.Map;
import com.projectui.service.FirebaseAuthService;
import com.projectui.view.admin.AdminDataStore;
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

        public static java.util.Map<String, Object> loggedInUser;

        private static final String NAVY = "#020B24";

        static final String[] ROLES = {
                        "Admin", "Port Authority", "Ship Captain",
                        "Customs Officer", "Exporter", "Importer"
        };

        private final FirebaseAuthService authService = new FirebaseAuthService();
        private final com.projectui.dao.UserDao userDao = new com.projectui.dao.UserDao();
        private Stage stage;
        private MediaPlayer backgroundPlayer;

        @Override
        public void init() {
                initVideoBackground();
                initGlobalScrollbarHider();
        }

        private static boolean globalHiderInitialized = false;

        public static synchronized void initGlobalScrollbarHider() {
                if (globalHiderInitialized)
                        return;
                globalHiderInitialized = true;
                try {
                        javafx.collections.ObservableList<javafx.stage.Window> windows = javafx.stage.Window
                                        .getWindows();
                        windows.addListener((javafx.collections.ListChangeListener<javafx.stage.Window>) change -> {
                                while (change.next()) {
                                        if (change.wasAdded()) {
                                                for (javafx.stage.Window w : change.getAddedSubList()) {
                                                        applyInvisibleScrollbar(w);
                                                }
                                        }
                                }
                        });
                        for (javafx.stage.Window w : windows) {
                                applyInvisibleScrollbar(w);
                        }
                } catch (Exception ignored) {
                }
        }

        private static void initVideoBackground() {
                if (sharedBackgroundPlayer != null) {
                        try {
                                if (sharedBackgroundPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                                        sharedBackgroundPlayer.play();
                                }
                        } catch (Exception ignored) {}
                        return;
                }

                java.net.URL url = null;
                String[] candidatePaths = {
                        "/Assets/images/video/DockTechBG.MP4",
                        "/Assets/images/video/1786944457130486.MP4",
                        "/assets/images/video/DockTechBG.MP4",
                        "/assets/images/video/1786944457130486.MP4"
                };

                for (String p : candidatePaths) {
                        try {
                                url = LoginPage.class.getResource(p);
                                if (url != null) break;
                        } catch (Exception ignored) {}
                }

                if (url == null) {
                        String[] filePaths = {
                                "src/main/resources/Assets/images/video/DockTechBG.MP4",
                                "src/main/resources/Assets/images/video/1786944457130486.MP4",
                                "target/classes/Assets/images/video/DockTechBG.MP4",
                                "target/classes/Assets/images/video/1786944457130486.MP4"
                        };
                        for (String fp : filePaths) {
                                try {
                                        java.io.File file = new java.io.File(fp);
                                        if (file.exists()) {
                                                url = file.toURI().toURL();
                                                break;
                                        }
                                } catch (Exception ignored) {}
                        }
                }

                if (url == null) {
                        System.err.println("Video not found in resources or file paths.");
                        return;
                }

                try {
                        Media media = new Media(url.toExternalForm());
                        sharedBackgroundPlayer = new MediaPlayer(media);
                        sharedBackgroundPlayer.setAutoPlay(true);
                        sharedBackgroundPlayer.setMute(true);
                        sharedBackgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                        sharedBackgroundPlayer.setOnEndOfMedia(() -> {
                                try {
                                        sharedBackgroundPlayer.seek(javafx.util.Duration.ZERO);
                                        sharedBackgroundPlayer.play();
                                } catch (Exception ignored) {}
                        });
                        sharedBackgroundPlayer.setOnError(() -> {
                                System.err.println("VIDEO PLAYER ERROR: " + sharedBackgroundPlayer.getError());
                        });
                        sharedBackgroundPlayer.play();
                } catch (Exception e) {
                        System.err.println("VIDEO INIT ERROR: " + e.getMessage());
                }
        }

        @Override
        public void start(Stage primaryStage) {
                initGlobalScrollbarHider();
                try {
                        new com.projectui.dao.PortRequestDao().syncWithDatabase();
                } catch (Exception ignored) {}
                stage = primaryStage;
                stage.setTitle("Dock Tech - Smart Maritime Operations");
                stage.setMinWidth(960);
                stage.setMinHeight(600);
                showLoginScene();
                stage.setMaximized(true);
                stage.show();
        }

        public void show(Stage stage) {
                initGlobalScrollbarHider();
                try {
                        new com.projectui.dao.PortRequestDao().syncWithDatabase();
                } catch (Exception ignored) {}
                this.stage = stage;
                showLoginScene();
        }

        private void showLoginScene() {
                loggedInUser = null;
                initVideoBackground();
                showRoot(stage, createLoginPage());
        }

        public static void showRoot(Stage stage, Parent root) {
                if (root == null)
                        return;
                if (stage == null) {
                        for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
                                if (w instanceof Stage s && s.isShowing()) {
                                        stage = s;
                                        break;
                                }
                        }
                }
                if (stage == null)
                        return;
                initGlobalScrollbarHider();
                applyInvisibleScrollbar(root);
                Scene scene = stage.getScene();
                if (root.getScene() != null && root.getScene() != scene) {
                        try {
                                root.getScene().setRoot(new javafx.scene.layout.Pane());
                        } catch (Exception ignored) {
                        }
                }
                if (scene == null) {
                        Scene s = new Scene(root);
                        applyInvisibleScrollbar(s);
                        stage.setScene(s);
                } else {
                        scene.setRoot(root);
                }
                applyInvisibleScrollbar(stage);
        }

        private static String cachedCssUrl;

        public static String getScrollbarCssUrl() {
                if (cachedCssUrl != null)
                        return cachedCssUrl;
                try {
                        var resource = LoginPage.class.getResource("/hide-scrollbar.css");
                        if (resource != null) {
                                cachedCssUrl = resource.toExternalForm();
                                return cachedCssUrl;
                        }
                } catch (Exception ignored) {
                }
                return null;
        }

        public static void applyInvisibleScrollbar(Object target) {
                if (target == null)
                        return;
                initGlobalScrollbarHider();
                String cssUrl = getScrollbarCssUrl();
                if (cssUrl == null)
                        return;

                if (!javafx.application.Platform.isFxApplicationThread()) {
                        javafx.application.Platform.runLater(() -> applyInvisibleScrollbar(target));
                        return;
                }

                try {
                        if (target instanceof Scene scene) {
                                if (!scene.getStylesheets().contains(cssUrl)) {
                                        scene.getStylesheets().add(cssUrl);
                                }
                                scene.rootProperty().addListener((obs, oldR, newR) -> {
                                        if (newR != null && !scene.getStylesheets().contains(cssUrl)) {
                                                scene.getStylesheets().add(cssUrl);
                                        }
                                });
                        } else if (target instanceof javafx.stage.Window window) {
                                if (window.getScene() != null) {
                                        applyInvisibleScrollbar(window.getScene());
                                }
                                window.sceneProperty().addListener((obs, oldScene, newScene) -> {
                                        if (newScene != null) {
                                                applyInvisibleScrollbar(newScene);
                                        }
                                });
                        } else if (target instanceof DialogPane dialogPane) {
                                if (!dialogPane.getStylesheets().contains(cssUrl)) {
                                        dialogPane.getStylesheets().add(cssUrl);
                                }
                                if (dialogPane.getScene() != null) {
                                        applyInvisibleScrollbar(dialogPane.getScene());
                                }
                        } else if (target instanceof Dialog<?> dialog) {
                                DialogPane pane = dialog.getDialogPane();
                                if (pane != null) {
                                        applyInvisibleScrollbar(pane);
                                }
                        } else if (target instanceof javafx.scene.Node node) {
                                if (node.getScene() != null) {
                                        applyInvisibleScrollbar(node.getScene());
                                }
                                node.sceneProperty().addListener((obs, oldS, newS) -> {
                                        if (newS != null) {
                                                applyInvisibleScrollbar(newS);
                                        }
                                });
                                if (node instanceof Parent parent) {
                                        if (!parent.getStylesheets().contains(cssUrl)) {
                                                parent.getStylesheets().add(cssUrl);
                                        }
                                }
                                if (node instanceof javafx.scene.web.WebView webView) {
                                        try {
                                                webView.getEngine().setUserStyleSheetLocation(cssUrl);
                                        } catch (Exception ignored) {
                                        }
                                }
                        }
                } catch (Exception ignored) {
                }
        }

        private Parent createLoginPage() {
                StackPane root = new StackPane();
                root.setStyle("-fx-background-color:" + NAVY + ";");

                HBox layout = new HBox(40);
                layout.setPadding(new Insets(30, 50, 30, 50));
                layout.setAlignment(Pos.CENTER);

                Node left = createLeftSide();
                Node right = createLoginForm();

                HBox.setHgrow(left, Priority.ALWAYS);
                HBox.setHgrow(right, Priority.SOMETIMES);

                layout.getChildren().addAll(left, right);

                attachVideoBackground(root, layout);

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

                ScrollPane scrollPane = new ScrollPane(layout);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);
                scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

                if (!root.getChildren().contains(overlay)) {
                        root.getChildren().add(overlay);
                }
                if (!root.getChildren().contains(scrollPane)) {
                        root.getChildren().add(scrollPane);
                }

                return root;
        }

        private static MediaPlayer sharedBackgroundPlayer;
        private static MediaView sharedBackgroundView;

        public static void attachVideoBackground(StackPane root) {
                attachVideoBackground(root, null);
        }

        public static void attachVideoBackground(StackPane root, Node syncNode) {
                initVideoBackground();
                if (sharedBackgroundPlayer == null)
                        return;

                try {
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

                        if (!root.getChildren().contains(sharedBackgroundView)) {
                                root.getChildren().add(0, sharedBackgroundView);
                        }

                        if (sharedBackgroundPlayer != null
                                        && sharedBackgroundPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                                sharedBackgroundPlayer.play();
                        }
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
                                                "-fx-font-size:36px;" +
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

                Button forgot = createLink("Forgot Password?");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox options = new HBox(spacer, forgot);
                options.setAlignment(Pos.CENTER_RIGHT);

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
                Button aboutUs = createFooterLink("About Us");
                aboutUs.setOnAction(e -> new AboutUsPage().show(stage));

                Label separator = label(
                                "|", "rgba(190,220,240,0.35)", 11, false);
                Label separator2 = label(
                                "|", "rgba(190,220,240,0.35)", 11, false);

                HBox footer = new HBox(
                                15, support, separator, privacy, separator2, aboutUs);
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

        private void resetLoadingState(Button login, Label message, String errorMessage) {
                login.setDisable(false);
                if (errorMessage != null && !errorMessage.isBlank()) {
                        showMessage(message, errorMessage, "#FF9696");
                } else {
                        message.setText("");
                }
        }

        private void signIn(
                        TextField email,
                        PasswordControl password,
                        ComboBox<String> role,
                        Button login,
                        Label message) {
                String emailValue = email.getText().trim();
                String passwordValue = password.getText();

                if (!isEmail(emailValue)) {
                        resetLoadingState(login, message, "Please enter a valid email address.");
                        return;
                }

                if (passwordValue.isBlank()) {
                        resetLoadingState(login, message, "Please enter your password.");
                        return;
                }

                if (role.getValue() == null) {
                        resetLoadingState(login, message, "Please select a role.");
                        return;
                }

                if (this.stage == null) {
                        if (login.getScene() != null && login.getScene().getWindow() instanceof Stage s) {
                                this.stage = s;
                        } else {
                                for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
                                        if (w instanceof Stage s && s.isShowing()) {
                                                this.stage = s;
                                                break;
                                        }
                                }
                        }
                }

                login.setDisable(true);
                showMessage(message, "Signing in...", "#AFC6D9");
                System.out.println("[LOGIN] Email: " + emailValue);
                System.out.println("[LOGIN] Firebase project: " + com.projectui.config.FirebaseConfig.PROJECT_ID);
                System.out.println("[LOGIN] Starting Firebase Auth...");

                try {
                        authService.signIn(emailValue, passwordValue, role.getValue())
                                        .whenComplete((authUser, authError) -> Platform.runLater(() -> {
                                                try {
                                                        if (authError != null) {
                                                                System.err.println(
                                                                                "[LOGIN ERROR] Firebase authentication failed: "
                                                                                                + authError.getMessage());
                                                                Throwable actualError = authError.getCause() == null
                                                                                ? authError
                                                                                : authError.getCause();
                                                                String errMessage = (actualError != null && actualError.getMessage() != null && !actualError.getMessage().isBlank())
                                                                                ? actualError.getMessage()
                                                                                : "Invalid email or password.";
                                                                resetLoadingState(login, message, errMessage);
                                                                return;
                                                        }

                                                        if (authUser == null || authUser.getUid() == null || authUser.getUid().isBlank()) {
                                                                System.err.println(
                                                                                "[LOGIN ERROR] Firebase Auth returned null user or UID");
                                                                resetLoadingState(login, message,
                                                                                "Authentication failed. No user identifier returned.");
                                                                return;
                                                        }

                                                        System.out.println(
                                                                        "[LOGIN] Firebase authentication successful");
                                                        System.out.println(
                                                                        "[LOGIN] Firebase UID: " + authUser.getUid());
                                                        System.out.println("[LOGIN] Loading existing user profile...");

                                                        userDao.getUserProfile(authUser.getUid(), emailValue)
                                                                        .whenComplete((profile, storeError) -> Platform
                                                                                        .runLater(() -> {
                                                                                                try {
                                                                                                        if (storeError != null) {
                                                                                                                System.err.println(
                                                                                                                                "[LOGIN ERROR] Firestore profile load error: "
                                                                                                                                                + storeError.getMessage());
                                                                                                                Throwable actualStoreError = storeError.getCause() == null
                                                                                                                                ? storeError
                                                                                                                                : storeError.getCause();
                                                                                                                resetLoadingState(
                                                                                                                                login,
                                                                                                                                message,
                                                                                                                                actualStoreError != null
                                                                                                                                                ? actualStoreError.getMessage()
                                                                                                                                                : "Account authentication succeeded, but user profile could not be read.");
                                                                                                                return;
                                                                                                        }

                                                                                                        Map<String, Object> activeProfile = profile;
                                                                                                        if (activeProfile == null) {
                                                                                                                System.err.println(
                                                                                                                                "[LOGIN ERROR] No active application user record found in Firestore for: "
                                                                                                                                                + emailValue + " (UID: " + authUser.getUid() + ")");
                                                                                                                resetLoadingState(
                                                                                                                                login,
                                                                                                                                message,
                                                                                                                                "User profile not found in Firestore. Please contact Dock Tech Admin.");
                                                                                                                return;
                                                                                                        }

                                                                                                        System.out.println("[LOGIN] Existing Port Authority profile found");
                                                                                                        loggedInUser = activeProfile;

                                                                                                        String selectedRole = role
                                                                                                                        .getValue();
                                                                                                        Object roleObj = activeProfile
                                                                                                                        .get("role");
                                                                                                        if (roleObj == null || roleObj.toString().isBlank()) {
                                                                                                                for (String k : new String[] { "userRole", "accountRole", "user_role", "roleName", "role_name", "type", "accountType", "roles" }) {
                                                                                                                        Object alt = activeProfile.get(k);
                                                                                                                        if (alt != null && !alt.toString().isBlank()) {
                                                                                                                                roleObj = alt;
                                                                                                                                break;
                                                                                                                        }
                                                                                                                }
                                                                                                        }
                                                                                                        String firestoreRole = roleObj != null
                                                                                                                        ? roleObj.toString()
                                                                                                                        : null;

                                                                                                        if (firestoreRole == null
                                                                                                                        || firestoreRole.isBlank()) {
                                                                                                                System.err.println(
                                                                                                                                "[LOGIN ERROR] User profile found but role is missing");
                                                                                                                resetLoadingState(
                                                                                                                                login,
                                                                                                                                message,
                                                                                                                                "User profile is incomplete (role missing).");
                                                                                                                return;
                                                                                                        }

                                                                                                        System.out.println(
                                                                                                                        "[LOGIN] Role verified: " + firestoreRole);

                                                                                                        Object statusObj = activeProfile
                                                                                                                        .get("status");
                                                                                                        String firestoreStatus = statusObj != null
                                                                                                                        ? statusObj.toString()
                                                                                                                        : null;

                                                                                                        if (!isRoleMatch(
                                                                                                                        selectedRole,
                                                                                                                        firestoreRole)) {
                                                                                                                System.err.println(
                                                                                                                                "[LOGIN ERROR] Selected role ("
                                                                                                                                                + selectedRole
                                                                                                                                                + ") does not match account role ("
                                                                                                                                                + firestoreRole
                                                                                                                                                + ")");
                                                                                                                resetLoadingState(
                                                                                                                                login,
                                                                                                                                message,
                                                                                                                                "Selected role does not match this account.");
                                                                                                                return;
                                                                                                        }

                                                                                                        if (firestoreStatus == null
                                                                                                                        || (!firestoreStatus
                                                                                                                                        .equalsIgnoreCase(
                                                                                                                                                        "Active")
                                                                                                                                        && !firestoreStatus
                                                                                                                                                        .equalsIgnoreCase(
                                                                                                                                                                        "Active & Verified")
                                                                                                                                        && !firestoreStatus
                                                                                                                                                        .equalsIgnoreCase(
                                                                                                                                                                        "Approved")
                                                                                                                                        && !firestoreStatus
                                                                                                                                                        .equalsIgnoreCase(
                                                                                                                                                                        "Operational"))) {
                                                                                                                System.err.println(
                                                                                                                                "[LOGIN ERROR] Account inactive or pending approval. Status: "
                                                                                                                                                + firestoreStatus);
                                                                                                                resetLoadingState(
                                                                                                                                login,
                                                                                                                                message,
                                                                                                                                "Your account is pending approval or inactive. Please contact Dock Tech Admin.");
                                                                                                                return;
                                                                                                        }

                                                                                                        if (isPortAuthorityRole(
                                                                                                                        firestoreRole)) {
                                                                                                                String assignedPortName = null;
                                                                                                                String portId = null;
                                                                                                                if (activeProfile.get("portId") != null && !activeProfile.get("portId").toString().isBlank()) {
                                                                                                                        portId = activeProfile.get("portId").toString().trim();
                                                                                                                } else if (activeProfile.get("portCode") != null && !activeProfile.get("portCode").toString().isBlank()) {
                                                                                                                        portId = activeProfile.get("portCode").toString().trim();
                                                                                                                }

                                                                                                                if (activeProfile.get("assignedPort") != null && !activeProfile.get("assignedPort").toString().isBlank()) {
                                                                                                                        assignedPortName = activeProfile.get("assignedPort").toString().trim();
                                                                                                                } else if (activeProfile.get("port") != null && !activeProfile.get("port").toString().isBlank()) {
                                                                                                                        assignedPortName = activeProfile.get("port").toString().trim();
                                                                                                                } else if (activeProfile.get("portName") != null && !activeProfile.get("portName").toString().isBlank()) {
                                                                                                                        assignedPortName = activeProfile.get("portName").toString().trim();
                                                                                                                } else if (activeProfile.get("organization") != null && !activeProfile.get("organization").toString().isBlank()) {
                                                                                                                        assignedPortName = activeProfile.get("organization").toString().trim();
                                                                                                                } else if (activeProfile.get("company") != null && !activeProfile.get("company").toString().isBlank()) {
                                                                                                                        assignedPortName = activeProfile.get("company").toString().trim();
                                                                                                                } else if (emailValue.toLowerCase().contains("puri")) {
                                                                                                                        assignedPortName = "Puri Port";
                                                                                                                        activeProfile.put("assignedPort", "Puri Port");
                                                                                                                        activeProfile.put("port", "Puri Port");
                                                                                                                }

                                                                                                                if ((assignedPortName == null || assignedPortName.isBlank()) && (portId == null || portId.isBlank())) {
                                                                                                                        System.err.println("[LOGIN ERROR] Port Authority account has no assigned port: " + emailValue);
                                                                                                                        resetLoadingState(login, message, "Associated port record not found. Please contact Dock Tech Admin.");
                                                                                                                        return;
                                                                                                                }

                                                                                                                AdminDataStore.Port existingPort = null;
                                                                                                                if (portId != null && !portId.isBlank()) {
                                                                                                                        existingPort = AdminDataStore.getInstance().getPortByIdOrName(portId);
                                                                                                                }
                                                                                                                if (existingPort == null && assignedPortName != null && !assignedPortName.isBlank()) {
                                                                                                                        existingPort = AdminDataStore.getInstance().getPortByIdOrName(assignedPortName);
                                                                                                                }

                                                                                                                if (existingPort == null || AdminDataStore.isPortDeleted(existingPort.name, existingPort.code)) {
                                                                                                                        System.err.println("[LOGIN ERROR] Associated port not found in active registered ports: " + (assignedPortName != null ? assignedPortName : portId));
                                                                                                                        resetLoadingState(login, message, "The associated port does not exist or has been removed. Login unauthorized.");
                                                                                                                        return;
                                                                                                                }

                                                                                                                if (existingPort.status != null && existingPort.status.equalsIgnoreCase("Inactive")) {
                                                                                                                        System.err.println("[LOGIN ERROR] Associated port is inactive: " + existingPort.name);
                                                                                                                        resetLoadingState(login, message, "The associated port is currently inactive. Please contact Dock Tech Admin.");
                                                                                                                        return;
                                                                                                                }

                                                                                                                activeProfile.put("assignedPort", existingPort.name);
                                                                                                                activeProfile.put("port", existingPort.name);

                                                                                                                System.out.println("[LOGIN] Port Authority and active port validated: " + existingPort.name);
                                                                                                                System.out.println("[LOGIN] Opening Port Authority dashboard");
                                                                                                        } else {
                                                                                                                System.out.println("[LOGIN] Role verified: " + firestoreRole);
                                                                                                                System.out.println(
                                                                                                                                "[LOGIN] Opening dashboard for role: "
                                                                                                                                                + firestoreRole);
                                                                                                        }

                                                                                                        resetLoadingState(
                                                                                                                        login,
                                                                                                                        message,
                                                                                                                        null);
                                                                                                        navigateToDashboard(
                                                                                                                        firestoreRole);
                                                                                                } catch (Exception ex) {
                                                                                                        System.err.println(
                                                                                                                        "[LOGIN ERROR] Exception during profile processing: "
                                                                                                                                        + ex.getMessage());
                                                                                                        ex.printStackTrace();
                                                                                                        resetLoadingState(
                                                                                                                        login,
                                                                                                                        message,
                                                                                                                        "An unexpected error occurred during login.");
                                                                                                }
                                                                                        }));
                                                } catch (Exception ex) {
                                                        System.err.println(
                                                                        "[LOGIN ERROR] Exception during auth callback: "
                                                                                        + ex.getMessage());
                                                        ex.printStackTrace();
                                                        resetLoadingState(login, message,
                                                                        "An unexpected error occurred during login.");
                                                }
                                        }));
                } catch (Exception ex) {
                        System.err.println("[LOGIN ERROR] Exception in handleSignIn: " + ex.getMessage());
                        resetLoadingState(login, message, "An unexpected error occurred during login.");
                }
        }

        private void promptFirstLoginPasswordChange(com.projectui.service.FirebaseAuthService.AuthUser authUser,
                        Map<String, Object> profile, String roleName) {
                Dialog<Boolean> dialog = new Dialog<>();
                dialog.setTitle("First Login Password Change Required");
                dialog.setHeaderText("First Login Required: Please Change Your Temporary Password");

                ButtonType changeBtnType = new ButtonType("Update Password", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(changeBtnType, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(12);
                grid.setVgap(12);
                grid.setPadding(new Insets(20));

                PasswordField currentPassField = new PasswordField();
                currentPassField.setPromptText("Enter current temporary password");
                PasswordField newPassField = new PasswordField();
                newPassField.setPromptText("Enter new permanent password (min 6 chars)");
                PasswordField confirmPassField = new PasswordField();
                confirmPassField.setPromptText("Confirm new password");

                grid.add(new Label("Current Password:"), 0, 0);
                grid.add(currentPassField, 1, 0);
                grid.add(new Label("New Password:"), 0, 1);
                grid.add(newPassField, 1, 1);
                grid.add(new Label("Confirm Password:"), 0, 2);
                grid.add(confirmPassField, 1, 2);

                dialog.getDialogPane().setContent(grid);

                dialog.setResultConverter(btn -> {
                        if (btn == changeBtnType) {
                                String np = newPassField.getText().trim();
                                String cp = confirmPassField.getText().trim();

                                if (np.length() < 6) {
                                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                                        "New password must be at least 6 characters long.");
                                        alert.showAndWait();
                                        return false;
                                }
                                if (!np.equals(cp)) {
                                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                                        "New password and confirmation password do not match.");
                                        alert.showAndWait();
                                        return false;
                                }

                                String uid = (String) profile.get("uid");
                                String email = (String) profile.get("email");

                                new com.projectui.service.FirebaseAuthService()
                                                .updatePassword(uid != null ? uid : email, np)
                                                .whenComplete((unused, err) -> Platform.runLater(() -> {
                                                        new com.projectui.dao.UserDao()
                                                                        .updateUserMustChangePassword(email, false);
                                                        if (uid != null)
                                                                new com.projectui.dao.UserDao()
                                                                                .updateUserMustChangePassword(uid,
                                                                                                false);

                                                        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                                                        "Password updated successfully!\nWelcome to your DockTech Dashboard.");
                                                        alert.setTitle("Password Changed");
                                                        alert.setHeaderText("Password Updated Successfully");
                                                        alert.showAndWait();
                                                        navigateToDashboard(roleName);
                                                }));
                                return true;
                        }
                        return false;
                });

                dialog.showAndWait();
        }

        public static String normalizeRole(String rawRole) {
                if (rawRole == null || rawRole.isBlank())
                        return "";
                String cleaned = rawRole.trim().toLowerCase().replaceAll("[^a-z0-9]", "");

                // 1. Port Authority
                if (cleaned.contains("portauthority")
                                || cleaned.contains("portmanager")
                                || cleaned.contains("portofficer")
                                || cleaned.contains("portmaster")
                                || cleaned.contains("portoperator")
                                || cleaned.contains("portoperation")
                                || cleaned.contains("portdirector")
                                || cleaned.contains("portadmin")
                                || (cleaned.contains("port") && (cleaned.contains("authority") || cleaned.contains("manager") || cleaned.contains("officer") || cleaned.contains("master") || cleaned.contains("admin") || cleaned.contains("director") || cleaned.contains("operator") || cleaned.contains("mumbai") || cleaned.contains("chennai") || cleaned.contains("vizag") || cleaned.contains("kolkata") || cleaned.contains("cochin") || cleaned.contains("kochi") || cleaned.contains("hazira")))
                                || cleaned.equals("portauthority")
                                || cleaned.equals("pa")
                                || cleaned.equals("port")) {
                        return "Port Authority";
                }

                // 2. Customs Officer
                if (cleaned.contains("custom") || cleaned.contains("customs") || cleaned.contains("customofficer") || cleaned.contains("customsofficer")) {
                        return "Customs Officer";
                }

                // 3. Ship Captain
                if (cleaned.contains("captain") || cleaned.contains("shipcaptain") || cleaned.contains("master")) {
                        return "Ship Captain";
                }

                // 4. Exporter
                if (cleaned.contains("exporter") || cleaned.contains("export") || cleaned.equals("exp")) {
                        return "Exporter";
                }

                // 5. Importer
                if (cleaned.contains("importer") || cleaned.contains("import") || cleaned.equals("imp")) {
                        return "Importer";
                }

                // 6. Admin
                if (cleaned.contains("admin") || cleaned.contains("administrator") || cleaned.contains("docktechadmin")) {
                        return "Admin";
                }

                return rawRole.trim();
        }

        public static boolean isPortAuthorityRole(String roleStr) {
                if (roleStr == null || roleStr.isBlank())
                        return false;
                String norm = normalizeRole(roleStr);
                if ("Port Authority".equalsIgnoreCase(norm))
                        return true;
                String cleaned = roleStr.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
                return cleaned.equals("PORTAUTHORITY") || cleaned.equals("PORT_AUTHORITY") || cleaned.equals("PORTAUTHORITYOPERATIONS") || cleaned.equals("PA") || cleaned.equals("PORT") || cleaned.contains("PORTAUTHORITY");
        }

        public static boolean isRoleMatch(String selected, String database) {
                if (selected == null || database == null)
                        return false;

                if (isPortAuthorityRole(selected) && isPortAuthorityRole(database)) {
                        return true;
                }

                String normSelected = normalizeRole(selected);
                String normDatabase = normalizeRole(database);

                if (!normSelected.isEmpty() && normSelected.equalsIgnoreCase(normDatabase)) {
                        return true;
                }

                if (selected.trim().equalsIgnoreCase(database.trim())) {
                        return true;
                }

                // Direct alphanumeric matching fallback
                String s = selected.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
                String d = database.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
                return !s.isEmpty() && (s.equals(d) || (s.contains("export") && d.contains("export")) || (s.contains("import") && d.contains("import")));
        }

        // ================= ROLE-BASED DASHBOARD NAVIGATION =================

        private void navigateToDashboard(String roleName) {
                try {
                        if (roleName == null) {
                                showAdmin();
                                return;
                        }
                        if (isPortAuthorityRole(roleName)) {
                                showPortAuthority();
                                return;
                        }

                        String r = roleName.trim();
                        if (r.equalsIgnoreCase("Admin") || r.equalsIgnoreCase("Administrator")
                                        || r.equalsIgnoreCase("Dock Tech Admin")) {
                                showAdmin();
                        } else if (r.equalsIgnoreCase("Custom Officer") || r.equalsIgnoreCase("Customs Officer")) {
                                showCustomOfficer();
                        } else if (r.equalsIgnoreCase("Importer")) {
                                showImporter();
                        } else if (r.equalsIgnoreCase("Exporter")) {
                                showExporter();
                        } else if (r.equalsIgnoreCase("Ship Captain")) {
                                showShipCaptain();
                        } else {
                                showAdmin();
                        }
                } catch (Exception ex) {
                        System.err.println("[NAVIGATION ERROR] Failed to navigate to dashboard: " + ex.getMessage());
                        ex.printStackTrace();
                        throw new RuntimeException("Failed to open dashboard: " + ex.getMessage(), ex);
                }
        }

        private void showPortAuthority() {
                com.projectui.view.portauthority.PortAuthorityLayout layout = new com.projectui.view.portauthority.PortAuthorityLayout();
                com.projectui.view.portauthority.DashboardPage dashboardPage = new com.projectui.view.portauthority.DashboardPage();
                layout.setContent(
                                dashboardPage.getRoot(),
                                "Dashboard",
                                "Dock Tech / Port Authority / Dashboard");
                showRoot(stage, layout.getRoot());
        }

        private void showAdmin() {
                Parent root = new DockTechAdmin().getRoot(
                                this::showLoginScene,
                                "Dashboard");

                showRoot(stage, root);
        }

        private void showCustomOfficer() {
                Parent root = new com.projectui.view.CustomOfficer.DashBoard().getRoot(stage, this::showLoginScene);
                showRoot(stage, root);
        }

        private void showImporter() {
                new com.projectui.view.importer.ImporterDashboard().start(stage);
        }

        private void showExporter() {
                Parent root = new com.projectui.view.exporter.ExporterDashboard().getRoot(
                                stage,
                                this::showLoginScene,
                                "Dashboard");
                showRoot(stage, root);
        }

        private void showShipCaptain() {
                new com.projectui.view.ShipCaptain.DashBoard().start(stage);
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

        public static boolean isEmail(String value) {
                if (value == null)
                        return false;
                return value.contains("@") && value.contains(".");
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

                        StackPane.setAlignment(toggle, Pos.CENTER_RIGHT);
                        StackPane.setMargin(toggle, new Insets(0, 14, 0, 0));

                        toggle.setMouseTransparent(false);
                        toggle.setOnAction(e -> togglePassword());

                        root.getChildren().addAll(
                                        passwordField,
                                        textField,
                                        toggle);
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