package com.projectui.CustomOfficer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CargoInspection {
    private final VBox content;

    public CargoInspection() {

        content = new VBox(22);
        content.setPadding(new Insets(25, 35, 30, 35));
        content.setStyle("-fx-background-color:#f3f8fc;");

        // HEADER
        Label title = new Label("Cargo Inspection");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));

        Label subtitle = new Label(
                "Inspect cargo and approve or reject shipments based on customs verification.");
        subtitle.setTextFill(Color.web("#7b8494"));

        VBox heading = new VBox(5, title, subtitle);

        TextField search = new TextField();
        search.setPromptText("⌕  Search cargo...");
        search.setPrefWidth(220);
        search.setPrefHeight(38);
        search.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:8;" +
                "-fx-border-color:#dfe4ea;" +
                "-fx-border-radius:8;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox header = new HBox(heading, space, search);
        header.setAlignment(Pos.CENTER_LEFT);

        // CARDS
        HBox cards = new HBox(15);
        cards.getChildren().addAll(
                createCard("Pending Inspection", "18", "◷", "#f59e0b"),
                createCard("Under Inspection", "12", "⌕", "#087cf5"),
                createCard("Approved", "142", "✓", "#10b981"),
                createCard("Rejected", "07", "✕", "#ef4444")
        );

        // TABLE
        VBox tableBox = new VBox(15);
        tableBox.setPadding(new Insets(20));
        tableBox.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:14;" +
                "-fx-border-color:#e5e7eb;" +
                "-fx-border-radius:14;");

        Label tableTitle = new Label("Cargo Inspection");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 19));

        Label tableSubtitle = new Label(
                "Review and inspect cargo shipments.");
        tableSubtitle.setTextFill(Color.web("#8a93a3"));

        VBox tableHeading = new VBox(4, tableTitle, tableSubtitle);

        GridPane table = new GridPane();
        table.setHgap(15);
        table.setVgap(5);

        String[] headers = {
                "Cargo ID", "Cargo", "Shipping Co.", "Category",
                "Weight", "Inspection Status", "Customs Status", "Action"
        };

        for (int i = 0; i < headers.length; i++) {
            Label l = new Label(headers[i]);
            l.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            l.setTextFill(Color.web("#6b7280"));
            l.setPadding(new Insets(10));
            table.add(l, i, 0);
        }

        addRow(table, 1, "CGO-1287", "Ocean Queen",
                "Ocean Shipping Ltd.", "Electronics", "120 Tons",
                "Under Inspection", "Pending");

        addRow(table, 2, "CGO-1290", "Sea Dragon",
                "Global Logistics", "Machinery", "180 Tons",
                "Completed", "Approved");

        addRow(table, 3, "CGO-1281", "Pacific Star",
                "Maritime Cargo", "Textiles", "95 Tons",
                "Hold", "Rejected");

        addRow(table, 4, "CGO-1301", "Coral Queen",
                "Ocean Shipping Ltd.", "Pharmaceuticals", "210 Tons",
                "Pending", "Pending");

        ScrollPane scroll = new ScrollPane(table);
        scroll.setFitToWidth(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-border-color:transparent;");

        tableBox.getChildren().addAll(tableHeading, scroll);
        VBox.setVgrow(tableBox, Priority.ALWAYS);

        content.getChildren().addAll(header, cards, tableBox);
    }

    private VBox createCard(String title, String value,
                            String icon, String color) {

        VBox card = new VBox(7);
        card.setPrefHeight(110);
        card.setPadding(new Insets(17));
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:13;" +
                "-fx-border-color:#e5e7eb;" +
                "-fx-border-radius:13;");

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web("#7b8494"));

        Label number = new Label(value);
        number.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        number.setTextFill(Color.web("#111827"));

        Label iconLabel = new Label(icon);
        iconLabel.setMinSize(35, 35);
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        iconLabel.setStyle(
                "-fx-background-color:" + color + "25;" +
                "-fx-text-fill:" + color + ";" +
                "-fx-background-radius:9;");

        VBox numbers = new VBox(4, titleLabel, number);
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox row = new HBox(10, numbers, space, iconLabel);
        card.getChildren().add(row);

        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private void addRow(GridPane table, int row,
                         String id, String cargo, String company,
                         String category, String weight,
                         String inspection, String customs) {

        String[] values = {id, cargo, company, category, weight};

        for (int i = 0; i < values.length; i++) {
            table.add(cell(values[i]), i, row);
        }

        Label inspectionLabel = cell(inspection);
        Label customsLabel = cell(customs);

        table.add(inspectionLabel, 5, row);
        table.add(customsLabel, 6, row);

        Button inspect = new Button("Inspect");
        inspect.setStyle(
                "-fx-background-color:#0f5fff;" +
                "-fx-text-fill:white;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:8;");

        Button reject = new Button("Reject");
        reject.setStyle(
                "-fx-background-color:#ef4444;" +
                "-fx-text-fill:white;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:8;");

        inspect.setOnAction(e -> {
            inspectionLabel.setText("Completed");
            inspectionLabel.setTextFill(Color.web("#10b981"));

            customsLabel.setText("Approved");
            customsLabel.setTextFill(Color.web("#10b981"));

            showSuccessAlert(id);
        });

        reject.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Reject Cargo");
            dialog.setHeaderText("Reason for rejecting Cargo " + id);
            dialog.setContentText("Enter rejection reason:");

            dialog.showAndWait().ifPresent(reason -> {
                if (reason.trim().isEmpty()) {
                    new Alert(Alert.AlertType.WARNING,
                            "Please enter a reason before rejecting the cargo.")
                            .showAndWait();
                    return;
                }

                inspectionLabel.setText("Rejected");
                inspectionLabel.setTextFill(Color.web("#ef4444"));

                customsLabel.setText("Rejected");
                customsLabel.setTextFill(Color.web("#ef4444"));

                showRejectAlert(id, reason);
            });
        });

        table.add(new HBox(8, inspect, reject), 7, row);
    }

    private Label cell(String text) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setPadding(new Insets(15, 8, 15, 8));
        l.setFont(Font.font(11));
        l.setTextFill(Color.web("#374151"));
        return l;
    }

    private void showSuccessAlert(String id) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Inspection Successful");
        alert.setHeaderText(null);
        alert.setContentText(
                "Cargo " + id +
                " has been successfully inspected and approved.");
        alert.showAndWait();
    }

    private void showRejectAlert(String id, String reason) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Cargo Rejected");
        alert.setHeaderText("Cargo Rejected");
        alert.setContentText(
                "Cargo " + id +
                " has been rejected successfully.\n\nReason: " + reason);
        alert.showAndWait();
    }

    public VBox getContent() {
        return content;
    }

    public Parent getRoot() {
        return content;
    }
}
    

