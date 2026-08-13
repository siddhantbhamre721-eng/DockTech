package com.projectui.view.admin;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ReportsPage extends AdminTheme {

    private final AdminDataStore store = AdminDataStore.getInstance();
    private final GridPane reportTable = new GridPane();
    private ComboBox<String> reportType;
    private ComboBox<String> statusFilter;
    private TextField search;
    private String selectedPeriod = "Today";
    private Label message;

    public Parent getRoot() {
        Button generate = primaryButton("Generate Report");
        generate.setOnAction(event -> generateReports());

        VBox page = pageContent();
        page.getChildren().addAll(
                pageHeader("Reports & Analytics", "View operational reports", generate),
                new HBox(16,
                        statCard("Port Activity", "128", "+12% this week"),
                        statCard("Vessel Activity", "42", "#111827", "+8% this week"),
                        statCard("Cargo Handled", "9.8K", "#111827", "+19% this week"),
                        statCard("System Users", String.valueOf(store.activeUsers()), "#111827", "+7% this week")
                ),
                new HBox(18, reportListCard(), activityCard())
        );
        refreshReportTable();
        return page;
    }

    private VBox reportListCard() {
        HBox filters = new HBox(10,
                filterButton("Today", true),
                filterButton("This Week", false),
                filterButton("This Month", false),
                filterButton("Custom Range", false)
        );
        filters.setAlignment(Pos.CENTER_LEFT);

        reportType = combo("Report Type", "Port Activity", "Vessel Movement", "Cargo Handled", "System Users", "Pending Inspections");
        statusFilter = combo("Status", "All Status", "Ready", "Processing");
        reportType.setPrefWidth(210);
        statusFilter.setPrefWidth(160);

        HBox filterRow = new HBox(12, reportType, statusFilter);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        search = new TextField();
        search.setPromptText("Search report");
        search.setPrefWidth(220);
        styleField(search);
        search.textProperty().addListener((obs, oldValue, newValue) -> refreshReportTable());

        reportType.valueProperty().addListener((obs, oldValue, newValue) -> refreshReportTable());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> generateReports());

        message = new Label(store.reportMessage);
        message.setStyle("-fx-text-fill:#747884; -fx-font-size:14; -fx-font-weight:bold;");

        Button reset = secondaryButton("Reset Filters");
        reset.setOnAction(event -> {
            selectedPeriod = "Today";
            if (search != null) search.clear();
            if (reportType != null) reportType.setValue("Port Activity");
            if (statusFilter != null) statusFilter.setValue("All Status");
            generateReports();
        });

        HBox controls = new HBox(12, search, filterRow, reset);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox card = panelCard("Reports", "Operational reports ready for review and export.");
        card.setPrefWidth(610);
        card.getChildren().addAll(filters, controls, message, horizontalScroll(reportTable));
        return card;
    }

    private VBox activityCard() {
        HBox bars = new HBox(15);
        bars.setAlignment(Pos.BOTTOM_LEFT);
        bars.getChildren().addAll(
                bar("Mon", store.weeklyOperations()[0]),
                bar("Tue", store.weeklyOperations()[1]),
                bar("Wed", store.weeklyOperations()[2]),
                bar("Thu", store.weeklyOperations()[3]),
                bar("Fri", store.weeklyOperations()[4])
        );

        VBox card = panelCard("Port Operations Activity", "Weekly trend");
        card.setPrefWidth(360);
        card.getChildren().addAll(
                bars,
                statusRow("Port Activity", store.weeklyOperations()[3] + " operations", "#16a34a"),
                statusRow("Vessel Movement", store.activeVessels() + " active", BLUE),
                statusRow("Cargo Handled", String.format("%,d tons", store.cargoHandled()), "#16a34a"),
                statusRow("Active Users", store.activeUsers() + " users", BLUE),
                statusRow("Pending Inspections", "14 pending", "#d97706")
        );
        return card;
    }

    private Button filterButton(String text, boolean active) {
        Button button = secondaryButton(text);
        button.setPrefHeight(34);
        if (active) {
            button.setStyle("-fx-background-color:" + BLUE + "; -fx-text-fill:white; -fx-background-radius:10; -fx-font-size:13; -fx-font-weight:bold; -fx-cursor:hand; -fx-padding:0 14 0 14;");
        }
        button.setOnAction(event -> {
            selectedPeriod = text;
            generateReports();
        });
        return button;
    }

    private void generateReports() {
        store.generateReports(selectedPeriod, statusFilter == null ? "All Status" : statusFilter.getValue());
        refreshReportTable();
    }

    private void refreshReportTable() {
        reportTable.getChildren().clear();
        reportTable.setHgap(0);
        reportTable.setVgap(0);
        reportTable.setStyle("-fx-background-color:white; -fx-border-color:#dfe3ea; -fx-border-radius:16; -fx-background-radius:16;");

        String[] headers = {"Report Name", "Period", "Status", "Action"};
        for (int col = 0; col < headers.length; col++) {
            reportTable.add(tableCell(headers[col], true), col, 0);
        }

        List<AdminDataStore.ReportRow> visibleReports = new ArrayList<>();
        String query = search == null || search.getText() == null ? "" : search.getText().trim().toLowerCase();
        String selectedType = reportType == null || reportType.getValue() == null ? "All Reports" : reportType.getValue();

        for (AdminDataStore.ReportRow report : store.reports) {
            boolean matchesText = query.isEmpty() || report.name.toLowerCase().contains(query) || report.period.toLowerCase().contains(query) || report.status.toLowerCase().contains(query);
            boolean matchesType = "All Reports".equals(selectedType)
                    || report.name.toLowerCase().contains(selectedType.toLowerCase())
                    || selectedType.equalsIgnoreCase(report.name)
                    || report.status.equalsIgnoreCase(selectedType);
            if (matchesText && matchesType) {
                visibleReports.add(report);
            }
        }

        if (visibleReports.isEmpty()) {
            Label empty = tableCell("No reports available.", false);
            empty.setMinWidth(624);
            reportTable.add(empty, 0, 1, 4, 1);
        }

        for (int row = 0; row < visibleReports.size(); row++) {
            AdminDataStore.ReportRow report = visibleReports.get(row);
            reportTable.add(tableCell(report.name, false), 0, row + 1);
            reportTable.add(tableCell(report.period, false), 1, row + 1);
            reportTable.add(tableCell(report.status, false), 2, row + 1);
            reportTable.add(actionCell(report), 3, row + 1);
        }

        if (message != null) {
            message.setText(store.lastExportPath.isBlank() ? store.reportMessage : store.reportMessage + " to " + store.lastExportPath);
        }
    }

    private HBox actionCell(AdminDataStore.ReportRow report) {
        Button view = linkButton("View");
        Button export = linkButton("Export");
        Label divider = new Label("|");
        divider.setStyle("-fx-text-fill:#d1d5db; -fx-font-size:16;");
        view.setOnAction(event -> {
            store.reportMessage = report.name + " selected for viewing.";
            store.lastExportPath = "";
            refreshReportTable();
        });
        export.setOnAction(event -> {
            try {
                store.exportReport(report);
            } catch (Exception ex) {
                store.reportMessage = "Unable to export report.";
                store.lastExportPath = "";
            }
            refreshReportTable();
        });

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox cell = new HBox(10, view, divider, export, space);
        cell.setMinWidth(145);
        cell.setMinHeight(56);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setStyle("-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0; -fx-padding:0 18 0 18;");
        return cell;
    }

    private Button linkButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:transparent; -fx-text-fill:" + BLUE + "; -fx-font-size:11; -fx-font-weight:bold; -fx-cursor:hand; -fx-padding:0;");
        return button;
    }

    private VBox bar(String label, int height) {
        Region bar = new Region();
        bar.setPrefSize(44, height);
        bar.setStyle("-fx-background-color:" + BLUE + "; -fx-background-radius:12;");

        Label text = new Label(label);
        text.setStyle("-fx-text-fill:#747884; -fx-font-size:13; -fx-font-weight:bold;");

        VBox item = new VBox(10, bar, text);
        item.setAlignment(Pos.BOTTOM_CENTER);
        return item;
    }
}
