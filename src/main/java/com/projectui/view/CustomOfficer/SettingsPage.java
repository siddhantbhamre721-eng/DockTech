package com.projectui.view.CustomOfficer;

import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SettingsPage {

    private final String TEXT = "#172033";
    private final String GRAY = "#64748b";
    private final String BLUE = "#1769ff";

    private ComboBox<String> theme;
    private ComboBox<String> language;

    public VBox getContent() {

        VBox content = new VBox(22);
        content.setPadding(new Insets(25, 35, 35, 35));
        content.setStyle("-fx-background-color:#f6f8fb;");

        // HEADER
        VBox heading = new VBox(6,
                label("Settings", 32, true, TEXT),
                label(
                        "Manage your account, system preferences and security.",
                        15, false, GRAY));

        // SUCCESS MESSAGE
        Label success = new Label();
        success.setVisible(false);
        success.setMaxWidth(Double.MAX_VALUE);
        success.setPadding(new Insets(12, 18, 12, 18));
        success.setStyle(
                "-fx-background-color:#e8f8ef;" +
                        "-fx-text-fill:#168548;" +
                        "-fx-background-radius:10;" +
                        "-fx-font-size:15px;" +
                        "-fx-font-weight:bold;");

        // STATUS
        HBox status = new HBox(18,
                info("●", "System Status", "Operational", "#e7f7ee", "#168548"),
                info("✓", "Security", "Protected", "#eaf3ff", BLUE),
                info("◉", "Last Backup", "Today, 09:30 PM", "#fff7dc", "#b77900"));

        // DYNAMIC PROFILE EXTRACTION
        String officerName = "Custom Officer";
        String officerEmail = "officer@docktech.com";
        String officerRole = "Custom Officer";
        String officerPort = "Port of Mumbai";
        String initials = "CO";

        if (com.projectui.view.login.LoginPage.loggedInUser != null) {
            Map<String, Object> u = com.projectui.view.login.LoginPage.loggedInUser;
            if (u.get("name") != null) officerName = u.get("name").toString();
            if (u.get("email") != null) officerEmail = u.get("email").toString();
            if (u.get("role") != null) officerRole = u.get("role").toString();
            if (u.get("assignedPort") != null) officerPort = u.get("assignedPort").toString();

            String[] parts = officerName.trim().split("\\s+");
            if (parts.length > 0 && !parts[0].isEmpty()) {
                String a = parts[0].substring(0, 1);
                String b = parts.length > 1 && !parts[parts.length - 1].isEmpty() ? parts[parts.length - 1].substring(0, 1) : "";
                initials = (a + b).toUpperCase();
            }
        }

        // PROFILE
        VBox profile = card();

        HBox top = new HBox(
                new VBox(4,
                        label("Officer Profile (" + officerRole + ")", 22, true, TEXT),
                        label("Manage your officer credentials and assigned port jurisdiction.", 14, false, GRAY)),
                new Region());

        HBox.setHgrow(top.getChildren().get(1), Priority.ALWAYS);

        Label avatar = new Label(initials);
        avatar.setMinSize(56, 56);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle(
                "-fx-background-color:#dbeafe;" +
                        "-fx-background-radius:50;" +
                        "-fx-text-fill:#1769ff;" +
                        "-fx-font-size:22px;" +
                        "-fx-font-weight:bold;");

        top.getChildren().add(avatar);

        HBox fields = new HBox(16,
                field("Officer Name", officerName),
                field("Email", officerEmail),
                field("Jurisdiction Port", officerPort));

        Button saveProfile = button("Save Profile");

        final String finalEmail = officerEmail;
        saveProfile.setOnAction(e -> {
            success.setText("✓  Profile Preferences Updated Successfully for " + finalEmail);
            success.setVisible(true);
        });

        profile.getChildren().addAll(
                top, fields, saveProfile);

        // PREFERENCES
        VBox preferences = card();

        Button savePreferences = button("Save Preferences");

        savePreferences.setOnAction(e -> {
            String selectedLang = (language != null && language.getValue() != null) ? language.getValue() : "English";
            String selectedTheme = (theme != null && theme.getValue() != null) ? theme.getValue() : "Light";
            success.setText(
                    "✓  Preferences Saved Successfully (Language: " + selectedLang + ", Theme: " + selectedTheme + ")");
            success.setVisible(true);
        });

        preferences.getChildren().addAll(
                label("System Preferences", 22, true, TEXT),
                label(
                        "Customize dashboard behaviour and notifications.",
                        14, false, GRAY),
                setting(
                        "Auto Refresh",
                        "Automatically refresh dashboard data.", true),
                setting(
                        "Notifications",
                        "Show important customs alerts.", true),
                setting(
                        "Notification Sound",
                        "Play sound for new alerts.", false),
                savePreferences);

        // SECURITY
        VBox security = card();

        security.getChildren().addAll(
                label("Security & Access", 22, true, TEXT),
                label(
                        "Manage account security and access.",
                        14, false, GRAY),
                securityRow(
                        "Two-Factor Authentication",
                        "Extra security for your account.",
                        "Enabled"),
                securityRow(
                        "Active Sessions",
                        "Currently logged in devices.",
                        "3 Devices"),
                securityRow(
                        "Password",
                        "Last changed 18 days ago.",
                        "Change"));

        // APPEARANCE
        VBox appearance = card();

        ComboBox<String> theme = new ComboBox<>();
        theme.getItems().addAll(
                "Light", "System Default", "Dark");
        theme.setValue("Light");
        theme.setPrefWidth(200);
        theme.setStyle("-fx-font-size:14px; -fx-padding:4px 8px;");

        ComboBox<String> language = new ComboBox<>();
        language.getItems().addAll(
                "English", "Hindi", "Marathi");
        language.setValue("English");
        language.setPrefWidth(200);
        language.setStyle("-fx-font-size:14px; -fx-padding:4px 8px;");

        HBox choices = new HBox(
                24,
                choice("Theme", theme),
                choice("Language", language));

        appearance.getChildren().addAll(
                label("Display & Appearance", 22, true, TEXT),
                label(
                        "Customize your dashboard experience.",
                        14, false, GRAY),
                choices);

        // FOOTER
        HBox footer = new HBox(
                label(
                        "Dock Tech • Smart Port Operations • v1.0.0",
                        13, false, "#64748b"),
                new Region(),
                label(
                        "● All settings synced",
                        13, true, "#168548"));

        HBox.setHgrow(
                footer.getChildren().get(1),
                Priority.ALWAYS);

        content.getChildren().addAll(
                heading,
                success,
                status,
                profile,
                preferences,
                security,
                appearance,
                footer);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-border-color:transparent;");

        VBox result = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        return result;
    }

    // ================= INFO =================

    private VBox info(
            String icon, String title, String value,
            String bg, String color) {

        VBox box = new VBox(6);
        box.setPadding(new Insets(16));

        box.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:#e5e9f0;" +
                        "-fx-border-radius:12;");

        box.getChildren().addAll(
                label(icon, 20, true, color),
                label(title, 13, true, GRAY),
                label(value, 18, true, TEXT));

        HBox.setHgrow(box, Priority.ALWAYS);

        return box;
    }

    // ================= FIELD =================

    private VBox field(
            String title, String value) {

        TextField input = new TextField(value);
        input.setPrefHeight(42);
        input.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-padding: 8 14 8 14;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #cbd5e1;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;");

        VBox box = new VBox(6,
                label(title, 14, true, TEXT),
                input);

        HBox.setHgrow(box, Priority.ALWAYS);

        return box;
    }

    // ================= SETTING =================

    private HBox setting(
            String title, String desc, boolean selected) {

        CheckBox check = new CheckBox();
        check.setSelected(selected);
        check.setStyle("-fx-font-size: 15px;");

        VBox text = new VBox(4,
                label(title, 15, true, TEXT),
                label(desc, 13, false, GRAY));

        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);

        HBox row = new HBox(
                14, text, gap, check);

        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 8, 10, 8));

        return row;
    }

    // ================= SECURITY =================

    private HBox securityRow(
            String title, String desc, String action) {

        Button button = new Button(action);

        button.setStyle(
                "-fx-background-color:#f1f5f9;" +
                        "-fx-text-fill:#334155;" +
                        "-fx-font-size:14px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:8;" +
                        "-fx-padding:8 18;" +
                        "-fx-cursor:hand;");

        VBox text = new VBox(4,
                label(title, 15, true, TEXT),
                label(desc, 13, false, GRAY));

        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);

        HBox row = new HBox(
                text, gap, button);

        row.setAlignment(Pos.CENTER_LEFT);

        return row;
    }

    // ================= CHOICE =================

    private VBox choice(
            String title,
            ComboBox<String> combo) {

        return new VBox(
                6,
                label(title, 14, true, TEXT),
                combo);
    }

    // ================= CARD =================

    private VBox card() {

        VBox box = new VBox(16);
        box.setPadding(new Insets(22));

        box.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:14;" +
                        "-fx-border-color:#e5e9f0;" +
                        "-fx-border-radius:14;");

        return box;
    }

    // ================= BUTTON =================

    private Button button(String text) {

        Button b = new Button(text);

        b.setStyle(
                "-fx-background-color:" + BLUE + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:14px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-padding:10 22;" +
                        "-fx-background-radius:8;" +
                        "-fx-cursor:hand;");

        return b;
    }

    // ================= LABEL =================

    private Label label(
            String text, double size,
            boolean bold, String color) {

        Label l = new Label(text);

        l.setFont(
                Font.font(
                        "Arial",
                        bold ? FontWeight.BOLD : FontWeight.NORMAL,
                        size));

        l.setTextFill(Color.web(color));

        return l;
    }
}
