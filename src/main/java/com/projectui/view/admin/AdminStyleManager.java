package com.projectui.view.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Minimal shared UI helpers used across recovered admin pages.
 * Implementations aim to preserve the original look and provide
 * the helper API expected by the pages without redesigning UI.
 */
public class AdminStyleManager {

    protected static final String BLUE = "#0b63ff";
    protected static final String TEXT = "#111827";
    protected static final String CARD_STYLE = "-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-radius:12; -fx-background-radius:12;";

    protected Button primaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + BLUE + "; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:8 12; -fx-background-radius:8;");
        return b;
    }

    protected Button secondaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:#f2f4f8; -fx-text-fill:#111827; -fx-font-weight:bold; -fx-padding:6 10; -fx-background-radius:8;");
        return b;
    }

    protected Button linkButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:transparent; -fx-text-fill:" + BLUE + "; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:0;");
        return b;
    }

    protected VBox pageContent() {
        VBox v = new VBox(18);
        v.setPadding(new Insets(22));
        v.setStyle("-fx-background-color:transparent;");
        return v;
    }

    protected HBox pageHeader(String title, String subtitle, Node action) {
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#05070c; -fx-font-size:22; -fx-font-weight:bold;");

        Label s = new Label(subtitle);
        s.setStyle("-fx-text-fill:#747884; -fx-font-size:13;");

        VBox copy = new VBox(4, t, s);
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox row = new HBox(12, copy, space, action == null ? new Region() : action);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    protected VBox panelCard(String title, String subtitle) {
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");
        Label s = new Label(subtitle);
        s.setStyle("-fx-text-fill:#747884; -fx-font-size:12;");
        VBox v = new VBox(12, new VBox(4, t, s));
        v.setPadding(new Insets(16));
        v.setStyle(CARD_STYLE);
        return v;
    }

    protected VBox statCard(String title, String value, String delta) {
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#475467; -fx-font-size:12; -fx-font-weight:bold;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:28; -fx-font-weight:bold;");
        Label d = new Label(delta == null ? "" : delta);
        d.setStyle("-fx-text-fill:#10b981; -fx-font-size:12;");
        VBox box = new VBox(6, t, v, d);
        box.setPrefWidth(210);
        box.setPadding(new Insets(18, 20, 16, 20));
        box.setStyle(CARD_STYLE);
        return box;
    }

    protected VBox statBox(String title, String value, String color, LabelSetter setter) {
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#475467; -fx-font-size:12; -fx-font-weight:bold;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill:" + color + "; -fx-font-size:28; -fx-font-weight:bold;");
        setter.set(v);
        VBox box = new VBox(6, t, v);
        box.setPrefWidth(210);
        box.setMinHeight(112);
        box.setPadding(new Insets(18, 20, 16, 20));
        box.setStyle(CARD_STYLE);
        return box;
    }

    protected void styleField(TextField f) {
        f.setStyle("-fx-background-color:white; -fx-border-color:#e6eef8; -fx-padding:8; -fx-background-radius:8;");
    }

    protected <T> ComboBox<T> combo(String prompt, T... options) {
        ComboBox<T> cb = new ComboBox<>();
        cb.getItems().addAll(options);
        if (options.length > 0) cb.setValue(options[0]);
        cb.setPromptText(prompt);
        cb.setStyle("-fx-background-radius:8; -fx-padding:6; -fx-border-color:#e6eef8;");
        return cb;
    }

    protected ScrollPane horizontalScroll(Node node) {
        ScrollPane sp = new ScrollPane(node);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setFitToHeight(true);
        sp.setStyle("-fx-background:transparent; -fx-padding:0;");
        return sp;
    }

    protected interface LabelSetter { void set(Label label); }

}
