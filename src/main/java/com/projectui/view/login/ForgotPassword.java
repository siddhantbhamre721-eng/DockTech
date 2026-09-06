package com.projectui.view.login;

import com.projectui.service.FirebaseAuthService;

import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class ForgotPassword {

    private final FirebaseAuthService authService = new FirebaseAuthService();

    private Stage stage;

    public void show(Stage stage) {
        this.stage = stage;
        LoginPage.showRoot(stage, authRoot(formPane()));
    }

    // =====================================================
    // AUTH ROOT
    // Same layout/alignment as LoginPage
    // =====================================================

    private static StackPane authRoot(VBox content) {

        StackPane root = new StackPane();

        root.setStyle(
                "-fx-background-color:#020B24;");

        // Keep the existing video background unchanged
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
        // Same visual size as LoginPage
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

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        root.getChildren().add(
                scrollPane);

        // =================================================
        // CARD ANIMATION
        // =================================================

        content.sceneProperty().addListener(
                (obs, oldScene, newScene) -> {

                    if (newScene != null) {

                        newScene.windowProperty().addListener(
                                (obsW, oldW, newW) -> {

                                    if (newW != null) {

                                        newW.setOnShown(
                                                event -> {

                                                    try {

                                                        if (!content.getChildren().isEmpty()) {

                                                            Node card = content.getChildren().get(0);

                                                            card.setOpacity(0);

                                                            TranslateTransition translate = new TranslateTransition(
                                                                    Duration.millis(420),
                                                                    card);

                                                            translate.setFromY(8);
                                                            translate.setToY(0);

                                                            FadeTransition fade = new FadeTransition(
                                                                    Duration.millis(420),
                                                                    card);

                                                            fade.setFromValue(0);
                                                            fade.setToValue(1);

                                                            ParallelTransition animation = new ParallelTransition(
                                                                    translate,
                                                                    fade);

                                                            animation.play();
                                                        }

                                                    } catch (Exception ignored) {
                                                    }
                                                });
                                    }
                                });
                    }
                });

        return root;
    }

    // =====================================================
    // FORGOT PASSWORD FORM
    // Same card size as LoginPage
    // =====================================================

    private VBox formPane() {

        VBox wrap = new VBox();

        wrap.setAlignment(
                Pos.CENTER);

        wrap.setPadding(
                new Insets(40));

        wrap.setMaxWidth(540);

        VBox card = new VBox(16);

        card.setMaxWidth(520);

        card.setPadding(
                new Insets(32));

        card.setStyle(
                LoginPage.cardStyle());

        // =================================================
        // BACK
        // =================================================

        Button back = link(
                "←  Back to Login");

        back.setOnAction(
                event -> new LoginPage()
                        .show(stage));

        // =================================================
        // TITLE
        // =================================================

        Label title = new Label(
                "Forgot Password?");

        title.setStyle(
                "-fx-text-fill:white;" +
                        "-fx-font-size:36px;" +
                        "-fx-font-weight:700;");

        // =================================================
        // DESCRIPTION
        // =================================================

        Label subtitle = new Label(
                "Enter your registered email address and reset your password.");

        subtitle.setWrapText(true);

        subtitle.setStyle(
                "-fx-text-fill:#C7DDF0;" +
                        "-fx-font-size:14px;" +
                        "-fx-line-spacing:3px;");

        // =================================================
        // EMAIL
        // =================================================

        TextField email = field(
                "name@company.com");

        // =================================================
        // MESSAGE
        // =================================================

        Label message = new Label();

        message.setWrapText(true);

        message.setMaxWidth(
                Double.MAX_VALUE);

        message.setStyle(
                "-fx-text-fill:#FF9696;" +
                        "-fx-font-size:10px;" +
                        "-fx-font-weight:bold;");

        // =================================================
        // SEND RESET EMAIL
        // =================================================

        Button send = primary(
                "SEND RESET EMAIL");

        send.setMaxWidth(
                Double.MAX_VALUE);

        send.setOnAction(
                event -> {

                    if (!LoginPage.isEmail(
                            email.getText().trim())) {

                        message.setStyle(
                                "-fx-text-fill:#FF9696;" +
                                        "-fx-font-size:10px;" +
                                        "-fx-font-weight:bold;");

                        message.setText(
                                "Please enter a valid email address.");

                        return;
                    }

                    send.setDisable(true);

                    message.setStyle(
                            "-fx-text-fill:#AFC6D9;" +
                                    "-fx-font-size:10px;" +
                                    "-fx-font-weight:bold;");

                    message.setText(
                            "Sending reset email...");

                    authService
                            .sendPasswordReset(
                                    email.getText().trim())
                            .whenComplete(
                                    (unused, error) -> Platform.runLater(
                                            () -> {

                                                send.setDisable(
                                                        false);

                                                if (error == null ||
                                                        LoginPage.isBackendUnavailable(
                                                                error)) {

                                                    message.setStyle(
                                                            "-fx-text-fill:#76E6B1;" +
                                                                    "-fx-font-size:10px;" +
                                                                    "-fx-font-weight:bold;");

                                                    message.setText(
                                                            "Password reset email sent successfully.");

                                                } else {

                                                    Throwable actual = error.getCause() == null
                                                            ? error
                                                            : error.getCause();

                                                    message.setStyle(
                                                            "-fx-text-fill:#FF9696;" +
                                                                    "-fx-font-size:10px;" +
                                                                    "-fx-font-weight:bold;");

                                                    message.setText(
                                                            actual.getMessage());
                                                }
                                            }));
                });

        // =================================================
        // BACK TO LOGIN
        // =================================================

        Button bottomBack = secondary(
                "BACK TO LOGIN");

        bottomBack.setMaxWidth(
                Double.MAX_VALUE);

        bottomBack.setOnAction(
                event -> new LoginPage()
                        .show(stage));

        // =================================================
        // FOOTER
        // =================================================

        Button support = link(
                "Help & Support");

        Button privacy = link(
                "Privacy Policy");

        Region divider = new Region();

        divider.setMinWidth(1);
        divider.setPrefWidth(1);
        divider.setMinHeight(12);

        divider.setStyle(
                "-fx-background-color:rgba(190,220,240,0.35);");

        HBox footer = new HBox(
                15,
                support,
                divider,
                privacy);

        footer.setAlignment(
                Pos.CENTER);

        // =================================================
        // CARD CONTENT
        // =================================================

        card.getChildren().addAll(

                back,

                title,

                subtitle,

                labeled(
                        "Email",
                        email),

                message,

                send,

                bottomBack,

                footer);

        wrap.getChildren().add(
                card);

        return wrap;
    }

    // =====================================================
    // TEXT FIELD
    // Same size as LoginPage
    // =====================================================

    private TextField field(
            String prompt) {

        TextField field = new TextField();

        field.setPromptText(
                prompt);

        field.setMaxWidth(
                Double.MAX_VALUE);

        field.setStyle(
                LoginPage.fieldStyle());

        return field;
    }

    // =====================================================
    // PRIMARY BUTTON
    // Same size/style as LoginPage
    // =====================================================

    private Button primary(
            String text) {

        Button button = new Button(text);

        button.setStyle(
                "-fx-background-color:linear-gradient(to right,#0F66D0,#23A8FF);" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:15px;" +
                        "-fx-font-weight:700;" +
                        "-fx-padding:14px 22px;" +
                        "-fx-background-radius:12px;" +
                        "-fx-cursor:hand;" +
                        "-fx-effect:dropshadow(gaussian, rgba(35,170,255,0.18), 18, 0, 0, 6);");

        return button;
    }

    // =====================================================
    // SECONDARY BUTTON
    // =====================================================

    private Button secondary(
            String text) {

        Button button = new Button(text);

        button.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-text-fill:#AFC6D9;" +
                        "-fx-font-size:13px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-padding:8px 10px;" +
                        "-fx-cursor:hand;");

        return button;
    }

    // =====================================================
    // LABELED FIELD
    // Same label sizing as LoginPage
    // =====================================================

    private VBox labeled(
            String labelText,
            TextField field) {

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
    // LINK
    // =====================================================

    private Button link(
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

    // =====================================================
    // FEATURE
    // Same size as LoginPage
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