package com.projectui.view.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
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
    protected static final String CARD_STYLE = "-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-radius:8; -fx-background-radius:8; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.06), 14, 0, 0, 4);";

    protected Button primaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + BLUE + "; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:9 14; -fx-background-radius:8; -fx-cursor:hand;");
        return b;
    }

    protected Button secondaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:#f2f4f8; -fx-text-fill:#111827; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:8 12; -fx-background-radius:8; -fx-cursor:hand;");
        return b;
    }

    @SuppressWarnings("unused")
    private Button linkButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:transparent; -fx-text-fill:" + BLUE + "; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:0; -fx-cursor:hand;");
        return b;
    }

    protected Button dangerButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:9 14; -fx-background-radius:8; -fx-cursor:hand;");
        return b;
    }

    protected VBox pageContent() {
        VBox v = new VBox(18);
        v.setPadding(new Insets(20));
        v.setStyle("-fx-background-color:transparent;");
        return v;
    }

    protected HBox pageHeader(String title, String subtitle, Node action) {
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#05070c; -fx-font-size:26; -fx-font-weight:bold;");

        Label s = new Label(subtitle);
        s.setStyle("-fx-text-fill:#64748b; -fx-font-size:14;");

        VBox copy = new VBox(4, t, s);
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox row = new HBox(12, copy, space, action == null ? new Region() : action);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    protected VBox panelCard(String title, String subtitle) {
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#05070c; -fx-font-size:18; -fx-font-weight:bold;");
        Label s = new Label(subtitle);
        s.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox v = new VBox(12, new VBox(4, t, s));
        v.setPadding(new Insets(16));
        v.setStyle(CARD_STYLE);
        return v;
    }

    protected VBox statCard(String title, String value, String delta) {
        return statCard(title, value, TEXT, delta);
    }

    protected VBox statCard(String title, String value, String color, String delta) {
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#475467; -fx-font-size:14; -fx-font-weight:bold;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill:" + color + "; -fx-font-size:30; -fx-font-weight:bold;");
        Label d = new Label(delta == null ? "" : delta);
        d.setStyle("-fx-text-fill:#10b981; -fx-font-size:13;");
        VBox box = new VBox(6, t, v, d);
        box.setPrefWidth(210);
        box.setPadding(new Insets(18, 20, 16, 20));
        box.setStyle(CARD_STYLE);
        return box;
    }

    protected void styleField(TextField f) {
        f.setStyle("-fx-background-color:white; -fx-border-color:#e6eef8; -fx-padding:8 12; -fx-font-size:14; -fx-background-radius:8;");
    }

    @SuppressWarnings({"unchecked","varargs"})
    protected <T> ComboBox<T> combo(String prompt, T... options) {
        ComboBox<T> cb = new ComboBox<>();
        cb.getItems().addAll(options);
        if (options.length > 0) cb.setValue(options[0]);
        cb.setPromptText(prompt);
        cb.setStyle("-fx-background-radius:8; -fx-padding:6 10; -fx-font-size:14; -fx-border-color:#e6eef8;");
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

    protected Label tableCell(String text, boolean header) {
        Label cell = new Label(text);
        cell.setMinWidth(150);
        cell.setMinHeight(header ? 48 : 52);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 16, 0, 16));
        cell.setStyle("-fx-background-color:" + (header ? "#f7f9fc" : "white")
                + "; -fx-text-fill:" + (header ? TEXT : "#111827")
                + "; -fx-font-size:14; -fx-font-weight:" + (header ? "bold" : "normal")
                + "; -fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return cell;
    }

    protected HBox statusRow(String title, String detail, String color) {
        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill:" + color + "; -fx-font-size:14; -fx-font-weight:bold;");
        Label name = new Label(title);
        name.setStyle("-fx-text-fill:#111827; -fx-font-size:14; -fx-font-weight:bold;");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        Label value = new Label(detail);
        value.setStyle("-fx-text-fill:#64748b; -fx-font-size:13; -fx-font-weight:bold;");
        HBox row = new HBox(8, dot, name, space, value);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));
        return row;
    }

    protected GridPane table(String[] headers, String[][] rows) {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setStyle("-fx-background-color:white; -fx-border-color:#dfe3ea; -fx-border-radius:8; -fx-background-radius:8;");
        for (int col = 0; col < headers.length; col++) {
            grid.add(tableCell(headers[col], true), col, 0);
        }
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < headers.length; col++) {
                grid.add(tableCell(rows[row][col], false), col, row + 1);
            }
        }
        return grid;
    }

    protected interface LabelSetter { void set(Label label); }

}
