package com.projectui.view.portauthority;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

@SuppressWarnings({"unchecked", "unused"})
public class ReportsPage {

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ReportsPage() {
    }


    // =========================================================
    // MAIN PAGE
    // This method creates the complete Reports & Analytics page
    // =========================================================

    public VBox getContent() {

        VBox page = new VBox(18);

        page.setPadding(new Insets(0));

        page.setStyle(
                "-fx-background-color: transparent;"
        );


        // =====================================================
        // PAGE TITLE
        // =====================================================

        Label title =
                new Label("Reports & Analytics");

        title.setStyle(
                "-fx-text-fill:#0F172A; -fx-font-size:22px; -fx-font-weight:bold;"
        );


        Label subtitle =
                new Label(
                        "Analyze ship, cargo, container and payment operations."
                );

        subtitle.setStyle(
                "-fx-text-fill: #64748B; -fx-font-size: 13px;"
        );


        VBox heading =
                new VBox(
                        4,
                        title,
                        subtitle
                );


        // =====================================================
        // SUMMARY CARDS
        // =====================================================

        HBox summaryCards =
                createSummaryCards();


        // =====================================================
        // FILTER SECTION
        // =====================================================

        HBox filters =
                createFilters();


        // =====================================================
        // CHART SECTION
        // =====================================================

        HBox chartRow1 =
                new HBox(
                        20,
                        createShipActivityChart(),
                        createCargoMovementChart()
                );

        HBox chartRow2 =
                new HBox(
                        20,
                        createContainerAllocationChart(),
                        createPaymentSummaryChart()
                );


        chartRow1.setFillHeight(true);
        chartRow2.setFillHeight(true);


        // =====================================================
        // REPORT HISTORY
        // =====================================================

        VBox reportHistory =
                createReportHistory();


        // =====================================================
        // ADD EVERYTHING TO PAGE
        // =====================================================

        page.getChildren().addAll(
                heading,
                summaryCards,
                filters,
                chartRow1,
                chartRow2,
                reportHistory
        );


        return page;
    }


    // =========================================================
    // SUMMARY CARDS
    // =========================================================

    private HBox createSummaryCards() {

        HBox cards =
                new HBox(20);

        cards.setAlignment(Pos.CENTER);


        VBox shipCard =
                createCard(
                        "SHIP OPERATIONS",
                        "142",
                        "+8.5%"
                );


        VBox cargoCard =
                createCard(
                        "CARGO HANDLED",
                        "12,540 Tons",
                        "+12.4%"
                );


        VBox containerCard =
                createCard(
                        "CONTAINERS ALLOCATED",
                        "8,760",
                        "+6.8%"
                );


        VBox revenueCard =
                createCard(
                        "TOTAL REVENUE",
                        "₹12.5L",
                        "+15.2%"
                );


        cards.getChildren().addAll(
                shipCard,
                cargoCard,
                containerCard,
                revenueCard
        );


        return cards;
    }


    // =========================================================
    // GENERIC SUMMARY CARD
    // =========================================================

    private VBox createCard(
            String heading,
            String value,
            String percentage
    ) {

        VBox card =
                new VBox(10);

        card.setPadding(
                new Insets(20)
        );

        card.setPrefHeight(130);

        card.setPrefWidth(250);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #dbe4ef;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );


        Label headingLabel =
                new Label(heading);

        headingLabel.setStyle(
                "-fx-text-fill: #718096;" +
                "-fx-font-size: 13px;"
        );


        Label valueLabel =
                new Label(value);

        valueLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        26
                )
        );


        Label percentageLabel =
                new Label(percentage);

        percentageLabel.setStyle(
                "-fx-text-fill: #00a878;" +
                "-fx-font-size: 13px;"
        );


        card.getChildren().addAll(
                headingLabel,
                valueLabel,
                percentageLabel
        );


        return card;
    }


    // =========================================================
    // FILTER SECTION
    // =========================================================

    private HBox createFilters() {

        HBox filters =
                new HBox(15);

        filters.setPadding(
                new Insets(15)
        );

        filters.setAlignment(
                Pos.CENTER_LEFT
        );

        filters.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #dbe4ef;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );


        ComboBox<String> dateRange =
                new ComboBox<>();

        dateRange.getItems().addAll(
                "This Month",
                "Last Month",
                "Last 3 Months",
                "This Year"
        );

        dateRange.setValue(
                "This Month"
        );


        ComboBox<String> reportType =
                new ComboBox<>();

        reportType.getItems().addAll(
                "All Reports",
                "Ship",
                "Cargo",
                "Container",
                "Payment"
        );

        reportType.setValue(
                "All Reports"
        );


        ComboBox<String> ship =
                new ComboBox<>();

        ship.getItems().addAll(
                "All",
                "Oceanic Star",
                "Nordic Wave",
                "Pacific Dawn",
                "Atlantic Voyager"
        );

        ship.setValue("All");


        ComboBox<String> status =
                new ComboBox<>();

        status.getItems().addAll(
                "All",
                "Ready",
                "Pending",
                "Failed"
        );

        status.setValue("All");


        Button applyButton =
                new Button("Apply Filter");

        applyButton.setStyle(
                "-fx-background-color: #2878e8;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;"
        );


        // -----------------------------------------------------
        // FILTER BUTTON FUNCTIONALITY
        // -----------------------------------------------------

        applyButton.setOnAction(event -> {

            Alert alert =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alert.setTitle("Filters Applied");

            alert.setHeaderText(
                    "Report Filters"
            );

            alert.setContentText(
                    "Date Range: " +
                    dateRange.getValue() +

                    "\nReport Type: " +
                    reportType.getValue() +

                    "\nShip: " +
                    ship.getValue() +

                    "\nStatus: " +
                    status.getValue()
            );

            alert.showAndWait();
        });


        filters.getChildren().addAll(
                dateRange,
                reportType,
                ship,
                status,
                applyButton
        );


        return filters;
    }


    // =========================================================
    // SHIP ACTIVITY CHART
    // Arrivals / Departures / Docked
    // =========================================================

    private VBox createShipActivityChart() {

        CategoryAxis xAxis =
                new CategoryAxis();

        NumberAxis yAxis =
                new NumberAxis();

        xAxis.setLabel("Day");

        yAxis.setLabel("Ships");


        LineChart<String, Number> chart =
                new LineChart<>(
                        xAxis,
                        yAxis
                );

        chart.setTitle(
                "Ship Activity"
        );

        chart.setLegendVisible(true);

        chart.setPrefHeight(280);


        XYChart.Series<String, Number> arrivals =
                new XYChart.Series<>();

        arrivals.setName("Arrivals");


        arrivals.getData().addAll(
                new XYChart.Data<>("Mon", 12),
                new XYChart.Data<>("Tue", 15),
                new XYChart.Data<>("Wed", 13),
                new XYChart.Data<>("Thu", 17),
                new XYChart.Data<>("Fri", 14)
        );


        XYChart.Series<String, Number> departures =
                new XYChart.Series<>();

        departures.setName("Departures");


        departures.getData().addAll(
                new XYChart.Data<>("Mon", 10),
                new XYChart.Data<>("Tue", 12),
                new XYChart.Data<>("Wed", 11),
                new XYChart.Data<>("Thu", 15),
                new XYChart.Data<>("Fri", 13)
        );


        XYChart.Series<String, Number> docked =
                new XYChart.Series<>();

        docked.setName("Docked");


        docked.getData().addAll(
                new XYChart.Data<>("Mon", 8),
                new XYChart.Data<>("Tue", 9),
                new XYChart.Data<>("Wed", 10),
                new XYChart.Data<>("Thu", 11),
                new XYChart.Data<>("Fri", 9)
        );


        chart.getData().addAll(
                arrivals,
                departures,
                docked
        );


        return createChartCard(
                "Ship Activity",
                "Arrivals, departures and docked vessels",
                chart
        );
    }


    // =========================================================
    // CARGO MOVEMENT CHART
    // Received / Loaded / Dispatched / Pending
    // =========================================================

    private VBox createCargoMovementChart() {

        CategoryAxis xAxis =
                new CategoryAxis();

        NumberAxis yAxis =
                new NumberAxis();


        BarChart<String, Number> chart =
                new BarChart<>(
                        xAxis,
                        yAxis
                );

        chart.setTitle(
                "Cargo Movement"
        );

        chart.setLegendVisible(false);

        chart.setPrefHeight(280);


        XYChart.Series<String, Number> series =
                new XYChart.Series<>();


        series.getData().addAll(
                new XYChart.Data<>("Received", 7820),
                new XYChart.Data<>("Loaded", 3420),
                new XYChart.Data<>("Dispatched", 2450),
                new XYChart.Data<>("Pending", 1300)
        );


        chart.getData().add(series);


        return createChartCard(
                "Cargo Movement",
                "Received, loaded, dispatched and pending",
                chart
        );
    }


    // =========================================================
    // CONTAINER ALLOCATION CHART
    // =========================================================

    private VBox createContainerAllocationChart() {

        ObservableList<PieChart.Data> data =
                FXCollections.observableArrayList(

                        new PieChart.Data(
                                "Allocated",
                                6420
                        ),

                        new PieChart.Data(
                                "Available",
                                1850
                        ),

                        new PieChart.Data(
                                "In Transit",
                                320
                        ),

                        new PieChart.Data(
                                "Pending",
                                170
                        )
                );


        PieChart chart =
                new PieChart(data);

        chart.setTitle(
                "Container Allocation"
        );

        chart.setLegendVisible(true);

        chart.setPrefHeight(280);


        return createChartCard(
                "Container Allocation",
                "Allocation status across the port",
                chart
        );
    }


    // =========================================================
    // PAYMENT SUMMARY CHART
    // =========================================================

    private VBox createPaymentSummaryChart() {

        ObservableList<PieChart.Data> data =
                FXCollections.observableArrayList(

                        new PieChart.Data(
                                "Paid",
                                9.8
                        ),

                        new PieChart.Data(
                                "Pending",
                                2.1
                        ),

                        new PieChart.Data(
                                "Overdue",
                                0.6
                        )
                );


        PieChart chart =
                new PieChart(data);

        chart.setTitle(
                "Payment Summary"
        );

        chart.setLegendVisible(true);

        chart.setPrefHeight(280);


        return createChartCard(
                "Payment Summary",
                "Paid, pending and overdue transactions",
                chart
        );
    }


    // =========================================================
    // COMMON CHART CARD
    // =========================================================

    private VBox createChartCard(
            String title,
            String description,
            Node chart
    ) {

        VBox card =
                new VBox(8);

        card.setPadding(
                new Insets(20)
        );

        card.setPrefWidth(500);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #dbe4ef;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );


        Label titleLabel =
                new Label(title);

        titleLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        18
                )
        );


        Label descriptionLabel =
                new Label(description);

        descriptionLabel.setStyle(
                "-fx-text-fill: #718096;" +
                "-fx-font-size: 13px;"
        );


        card.getChildren().addAll(
                titleLabel,
                descriptionLabel,
                chart
        );


        HBox.setHgrow(
                card,
                Priority.ALWAYS
        );


        return card;
    }


    // =========================================================
    // REPORT HISTORY TABLE
    // =========================================================

    private VBox createReportHistory() {

        VBox section =
                new VBox(15);

        section.setPadding(
                new Insets(20)
        );

        section.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #dbe4ef;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );


        // -----------------------------------------------------
        // TITLE + BUTTONS
        // -----------------------------------------------------

        Label title =
                new Label("Report History");

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        20
                )
        );


        Label subtitle =
                new Label(
                        "Generated reports and downloadable archives"
                );

        subtitle.setStyle(
                "-fx-text-fill: #718096;"
        );


        VBox heading =
                new VBox(
                        5,
                        title,
                        subtitle
                );


        Button generateButton =
                new Button("+ Generate Report");


        Button exportButton =
                new Button("Export Report");


        generateButton.setStyle(
                "-fx-background-color: #2878e8;" +
                "-fx-text-fill: white;"
        );


        exportButton.setStyle(
                "-fx-background-color: #2878e8;" +
                "-fx-text-fill: white;"
        );


        HBox buttons =
                new HBox(
                        10,
                        generateButton,
                        exportButton
                );


        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );


        HBox top =
                new HBox(
                        heading,
                        spacer,
                        buttons
                );


        top.setAlignment(
                Pos.CENTER_LEFT
        );


        // -----------------------------------------------------
        // REPORT TABLE
        // -----------------------------------------------------

        TableView<String[]> table =
                new TableView<>();


        table.setPrefHeight(
                250
        );


        TableColumn<String[], String> reportId =
                createColumn(
                        "Report ID",
                        100,
                        0
                );


        TableColumn<String[], String> reportName =
                createColumn(
                        "Report Name",
                        180,
                        1
                );


        TableColumn<String[], String> date =
                createColumn(
                        "Generated Date",
                        130,
                        2
                );


        TableColumn<String[], String> reportType =
                createColumn(
                        "Report Type",
                        120,
                        3
                );


        TableColumn<String[], String> status =
                createColumn(
                        "Status",
                        100,
                        4
                );


        table.getColumns().addAll(
                reportId,
                reportName,
                date,
                reportType,
                status
        );


        // -----------------------------------------------------
        // SAMPLE REPORT DATA
        // -----------------------------------------------------

        ObservableList<String[]> reportData =
                FXCollections.observableArrayList();


        reportData.addAll(

                new String[]{
                        "RPT001",
                        "Monthly Ship Report",
                        "11 Aug 2026",
                        "Ship",
                        "Ready"
                },

                new String[]{
                        "RPT002",
                        "Cargo Summary",
                        "11 Aug 2026",
                        "Cargo",
                        "Ready"
                },

                new String[]{
                        "RPT003",
                        "Container Report",
                        "10 Aug 2026",
                        "Container",
                        "Ready"
                },

                new String[]{
                        "RPT004",
                        "Payment Summary",
                        "10 Aug 2026",
                        "Payment",
                        "Ready"
                }
        );


        table.setItems(
                reportData
        );


        // -----------------------------------------------------
        // GENERATE REPORT BUTTON
        // -----------------------------------------------------

        generateButton.setOnAction(event -> {

            Alert alert =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alert.setTitle(
                    "Generate Report"
            );

            alert.setHeaderText(
                    "Report Generated"
            );

            alert.setContentText(
                    "Your report has been generated successfully."
            );

            alert.showAndWait();
        });


        // -----------------------------------------------------
        // EXPORT REPORT BUTTON
        // -----------------------------------------------------

        exportButton.setOnAction(event -> {

            Alert alert =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alert.setTitle(
                    "Export Report"
            );

            alert.setHeaderText(
                    "Export Successful"
            );

            alert.setContentText(
                    "Report exported successfully."
            );

            alert.showAndWait();
        });


        section.getChildren().addAll(
                top,
                table
        );


        return section;
    }


    // =========================================================
    // TABLE COLUMN HELPER
    // =========================================================

    private TableColumn<String[], String> createColumn(
            String title,
            int width,
            int index
    ) {

        TableColumn<String[], String> column =
                new TableColumn<>(title);


        column.setPrefWidth(
                width
        );


        column.setCellValueFactory(
                data -> {

                    String[] row =
                            data.getValue();

                    return new javafx.beans.property.SimpleStringProperty(
                            row[index]
                    );
                }
        );


        return column;
    }
}