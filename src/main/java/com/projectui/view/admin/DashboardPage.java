package com.projectui.view.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DashboardPage extends AdminTheme {

    private Runnable addPortAction = () -> {};
    private Runnable addUserAction = () -> {};
    private Runnable alertsAction = () -> {};
    private Runnable reportAction = () -> {};

    public Parent getRoot() {
        return getRoot(() -> {}, () -> {}, () -> {}, () -> {});
    }

    public Parent getRoot(Runnable addPortAction, Runnable addUserAction, Runnable alertsAction, Runnable reportAction) {
        this.addPortAction = addPortAction;
        this.addUserAction = addUserAction;
        this.alertsAction = alertsAction;
        this.reportAction = reportAction;

        VBox totalPorts = statCard("Total Ports", "20", "+12% from last week");
        VBox activePorts = statCard("Active Ports", "16", "#111827", "+8% from last week");
        VBox activeVessels = statCard("Active Vessels", "44", BLUE, "+15% from last week");
        VBox cargoHandled = statCard("Cargo Handled", "12,542", "#111827", "+20% from last week");

        VBox page = pageContent();
        page.getChildren().addAll(
                pageHeader("Dashboard", "Welcome back, Dock Tech Admin", null),
                new HBox(16, totalPorts, activePorts, activeVessels, cargoHandled),
                new HBox(18, operationsCard(), statusCard()),
                new HBox(18, alertsCard(), recentActivityCard())
        );
        return page;
    }

    private VBox operationsCard() {
        LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), new NumberAxis());
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setAnimated(false);
        chart.setPrefHeight(210);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        int[] weekly = {40, 62, 48, 82, 80, 68, 108};
        series.getData().add(new XYChart.Data<>("Mon", weekly[0]));
        series.getData().add(new XYChart.Data<>("Tue", weekly[1]));
        series.getData().add(new XYChart.Data<>("Wed", weekly[2]));
        series.getData().add(new XYChart.Data<>("Thu", weekly[3]));
        series.getData().add(new XYChart.Data<>("Fri", weekly[4]));
        series.getData().add(new XYChart.Data<>("Sat", weekly[5]));
        series.getData().add(new XYChart.Data<>("Sun", weekly[6]));
        chart.getData().add(series);

        VBox card = panelCard("Port Operations Overview", "Important operational movement this week");
        card.setPrefWidth(620);
        card.getChildren().add(chart);
        return card;
    }

    private VBox statusCard() {
        VBox card = panelCard("Live Port Status", "Current port conditions");
        card.setPrefWidth(300);
        card.getChildren().addAll(
                statusLine("Port of Mumbai", "Operational", "#16a34a"),
                statusLine("Port of Chennai", "Operational", "#16a34a"),
                statusLine("Port of Kolkata", "Busy", "#d97706"),
                statusLine("Port of Vizag", "Maintenance", BLUE)
        );
        return card;
    }

    private VBox alertsCard() {
        VBox card = panelCard("Operational Alerts", "");
        card.setPrefWidth(395);
        card.getChildren().addAll(
                alertLine("Port Kolkata", "High congestion", "#d97706"),
                alertLine("Port Vizag", "Maintenance scheduled", BLUE),
                alertLine("14 cargo inspections", "Pending", "#d97706"),
                alertLine("3 vessels", "Delayed", "#ef4444")
        );
        return card;
    }

    private VBox recentActivityCard() {
        VBox card = panelCard("Recent Activity", "");
        card.setPrefWidth(525);
        card.getChildren().addAll(
                activityLine("Oceanic Star", "Vessel arrived at Port of Mumbai"),
                activityLine("Sea Voyager", "Cargo loading completed"),
                activityLine("MSC Atlantis", "Vessel departed from Port of Chennai"),
                activityLine("Cargo Inspection", "14 inspections pending")
        );
        return card;
    }

    private VBox vesselMovementCard() {
        GridPane table = table(
                new String[]{"Vessel", "Port", "Movement", "Status"},
                new String[][]{
                        {"Oceanic Star", "Mumbai", "Arrival", "On Time"},
                        {"Sea Voyager", "Chennai", "Loading", "In Progress"},
                        {"Pacific Trader", "Vizag", "Departure", "Delayed"}
                });

        VBox card = panelCard("Today's Vessel Movement", "Live arrival, loading and departure summary");
        card.setPrefWidth(620);
        card.getChildren().add(table);
        return card;
    }

    private VBox quickActionsCard() {
        ButtonRow addPort = new ButtonRow("Add Port", "Create a registered port", addPortAction);
        ButtonRow addUser = new ButtonRow("Add User", "Register an operations user", addUserAction);
        ButtonRow viewAlerts = new ButtonRow("View Alerts", "Check congestion and delays", alertsAction);
        ButtonRow report = new ButtonRow("Generate Report", "Prepare today's operations report", reportAction);

        VBox card = panelCard("Quick Actions", "Common admin tasks");
        card.setPrefWidth(340);
        card.getChildren().addAll(addPort, addUser, viewAlerts, report);
        return card;
    }

    private HBox statusLine(String name, String status, String color) {
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill:#111827; -fx-font-size:13; -fx-font-weight:bold;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label statusLabel = new Label(status);
        statusLabel.setPadding(new Insets(4, 10, 4, 10));
        statusLabel.setStyle("-fx-background-color:#eaf3ff; -fx-background-radius:10; -fx-text-fill:" + color + "; -fx-font-size:11; -fx-font-weight:bold;");

        HBox row = new HBox(12, nameLabel, space, statusLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 0, 9, 0));
        row.setStyle("-fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return row;
    }

    private HBox alertLine(String title, String detail, String color) {
        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill:" + color + "; -fx-font-size:14; -fx-font-weight:bold;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#111827; -fx-font-size:13; -fx-font-weight:bold;");

        Label detailLabel = new Label(detail);
        detailLabel.setStyle("-fx-text-fill:#747884; -fx-font-size:12;");

        HBox row = new HBox(8, dot, titleLabel, detailLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 0, 5, 0));
        return row;
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:#05070c; -fx-font-size:20; -fx-font-weight:bold;");
        return label;
    }

    private VBox activityLine(String title, String detail) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#111827; -fx-font-size:13; -fx-font-weight:bold;");

        Label detailLabel = new Label(detail);
        detailLabel.setStyle("-fx-text-fill:#747884; -fx-font-size:12;");

        VBox item = new VBox(4, titleLabel, detailLabel);
        item.setPadding(new Insets(8, 0, 8, 0));
        return item;
    }

    private class ButtonRow extends HBox {
        ButtonRow(String title, String subtitle, Runnable action) {
            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:15; -fx-font-weight:bold;");

            Label subtitleLabel = new Label(subtitle);
            subtitleLabel.setStyle("-fx-text-fill:#747884; -fx-font-size:13;");

            VBox copy = new VBox(3, titleLabel, subtitleLabel);
            Region space = new Region();
            HBox.setHgrow(space, Priority.ALWAYS);

            Label arrow = new Label(">");
            arrow.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:18; -fx-font-weight:bold;");

            getChildren().addAll(copy, space, arrow);
            setAlignment(Pos.CENTER_LEFT);
            setPadding(new Insets(10, 12, 10, 12));
            setStyle("-fx-background-color:#f6f9fd; -fx-background-radius:12; -fx-border-color:#e5ebf3; -fx-border-radius:12;");
            setOnMouseClicked(event -> action.run());
            setOnMouseEntered(event -> setStyle("-fx-background-color:#eef5ff; -fx-background-radius:12; -fx-border-color:#cfe0f5; -fx-border-radius:12; -fx-cursor:hand;"));
            setOnMouseExited(event -> setStyle("-fx-background-color:#f6f9fd; -fx-background-radius:12; -fx-border-color:#e5ebf3; -fx-border-radius:12;"));
        }
    }
}
