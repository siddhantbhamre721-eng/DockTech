package com.projectui.view.admin;

import com.projectui.service.FirebaseAuthService;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SettingsPage extends AdminTheme {

    private final AdminDataStore store = AdminDataStore.getInstance();
    private final FirebaseAuthService authService = new FirebaseAuthService();

    public Parent getRoot() {
        VBox page = pageContent();
        page.getChildren().addAll(
                pageHeader("Settings", "Manage your profile and system preferences.", null),
                tabRow(),
                new HBox(18, leftColumn(), rightColumn())
        );
        return page;
    }

    private HBox tabRow() {
        HBox row = new HBox(34, tab("Profile", true), tab("Preferences", false), tab("Security", false));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color:#dce4ef; -fx-border-width:0 0 1 0;");
        return row;
    }

    private Label tab(String text, boolean active) {
        Label label = new Label(text);
        label.setPadding(new Insets(0, 0, 9, 0));
        label.setStyle("-fx-text-fill:" + (active ? BLUE : "#111827") + "; -fx-font-size:12; -fx-font-weight:bold; -fx-border-color:" + (active ? BLUE : "transparent") + "; -fx-border-width:0 0 2 0;");
        return label;
    }

    private VBox leftColumn() {
        VBox column = new VBox(18, profileCard(), quickActionsCard());
        column.setPrefWidth(360);
        return column;
    }

    private VBox rightColumn() {
        VBox column = new VBox(18, preferencesCard(), securityCard());
        column.setPrefWidth(470);
        return column;
    }

    private VBox profileCard() {
        TextField name = new TextField(store.adminName);
        TextField email = new TextField(store.adminEmail);
        TextField role = new TextField("Administrator");
        styleField(name);
        styleField(email);
        styleField(role);
        role.setEditable(false);

        Label message = messageLabel(store.settingsMessage);
        Button save = primaryButton("Save Changes");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setOnAction(event -> {
            String nameValue = name.getText().trim();
            String emailValue = email.getText().trim();
            if (nameValue.isEmpty()) {
                message.setText("Please enter an admin name.");
                return;
            }
            if (!emailValue.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                message.setText("Invalid email address.");
                return;
            }
            store.adminName = nameValue;
            store.adminEmail = emailValue;
            store.settingsMessage = "Settings saved successfully.";
            message.setText(store.settingsMessage);
        });

        VBox card = panelCard("Admin Profile", "Update your personal information.");
        card.setPrefWidth(360);
        card.getChildren().addAll(
                labeledControl("Full Name", name),
                labeledControl("Email", email),
                labeledControl("Role", role),
                message,
                save
        );
        return card;
    }

    private VBox quickActionsCard() {
        Label message = messageLabel("");
        Button reset = secondaryButton("Reset to Default");
        reset.setMaxWidth(Double.MAX_VALUE);
        reset.setOnAction(event -> message.setText("Default settings restored."));

        VBox card = panelCard("Quick Actions", "Reset your settings to default.");
        card.setPrefWidth(360);
        card.getChildren().addAll(reset, message);
        return card;
    }

    private VBox preferencesCard() {
        ComboBox<String> notifications = combo("Notifications", "Enabled", "Disabled");
        ComboBox<String> language = combo("Language", "English", "Hindi", "Marathi");
        ComboBox<String> dateFormat = combo("Date Format", "DD MMM, YYYY", "DD/MM/YYYY", "YYYY-MM-DD");
        notifications.setValue(store.notifications ? "Enabled" : "Disabled");
        language.setValue(store.language);
        dateFormat.setValue(store.dateFormat);
        notifications.setOnAction(event -> store.notifications = "Enabled".equals(notifications.getValue()));
        language.setOnAction(event -> store.language = language.getValue());
        dateFormat.setOnAction(event -> store.dateFormat = dateFormat.getValue());

        VBox card = panelCard("Preferences", "Customize your system preferences.");
        card.setPrefWidth(470);
        card.getChildren().addAll(
                labeledControl("Notifications", notifications),
                labeledControl("Language", language),
                labeledControl("Date Format", dateFormat)
        );
        return card;
    }

    private VBox securityCard() {
        PasswordField current = passwordField("Current Password");
        PasswordField next = passwordField("New Password");
        PasswordField confirm = passwordField("Confirm New Password");
        Label message = messageLabel("");
        Button update = primaryButton("Update Password");
        update.setPrefWidth(220);
        update.setOnAction(event -> {
            if (current.getText().isBlank()) {
                message.setText("Please enter your current password to confirm.");
                return;
            }
            if (next.getText().isBlank()) {
                message.setText("Please enter a new password.");
                return;
            }
            if (!next.getText().equals(confirm.getText())) {
                message.setText("Passwords do not match.");
                return;
            }
            if (store.authToken.isBlank()) {
                message.setText("Please log in again before changing your password.");
                return;
            }

            update.setDisable(true);
            message.setText("Updating password...");
            authService.updatePassword(store.authToken, next.getText())
                    .whenComplete((unused, error) -> Platform.runLater(() -> {
                        update.setDisable(false);
                        if (error != null) {
                            message.setText(error.getCause() == null ? error.getMessage() : error.getCause().getMessage());
                            return;
                        }
                        current.clear();
                        next.clear();
                        confirm.clear();
                        message.setText("Password updated in Firebase Authentication.");
                    }));
        });

        VBox card = panelCard("Security", "Change your password regularly.");
        card.setPrefWidth(470);
        card.getChildren().addAll(
                labeledControl("Current Password", current),
                labeledControl("New Password", next),
                labeledControl("Confirm New Password", confirm),
                message,
                update
        );
        return card;
    }

    private PasswordField passwordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        styleField(field);
        return field;
    }

    private VBox labeledControl(String labelText, Parent field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill:#111827; -fx-font-size:14; -fx-font-weight:bold;");
        return new VBox(8, label, field);
    }

    private Label messageLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:#747884; -fx-font-size:13; -fx-font-weight:bold;");
        return label;
    }
}
