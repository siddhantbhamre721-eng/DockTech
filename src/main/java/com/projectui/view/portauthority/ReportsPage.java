package com.projectui.view.portauthority;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.stage.FileChooser;

import com.projectui.controller.portauthority.ShipController;
import com.projectui.controller.portauthority.CargoController;
import com.projectui.controller.portauthority.ContainerController;
import com.projectui.controller.portauthority.PaymentController;
import com.projectui.controller.portauthority.ReportController;
import com.projectui.controller.portauthority.ReportCloudinaryController;
import com.projectui.controller.portauthority.NotificationController;

import com.projectui.model.portauthority.Ship;
import com.projectui.model.portauthority.Cargo;
import com.projectui.model.portauthority.Container;
import com.projectui.model.portauthority.Payment;
import com.projectui.model.portauthority.Report;

import com.projectui.service.portauthority.PdfReportGenerator;


// =========================================================
// REPORTS & ANALYTICS PAGE
// =========================================================

public class ReportsPage {

    // =====================================================
    // CONTROLLERS
    // =====================================================

    private ShipController shipController =
            new ShipController();

    private CargoController cargoController =
            new CargoController();

    private ContainerController containerController =
            new ContainerController();

    private PaymentController paymentController =
            new PaymentController();

    private ReportController reportController =
            new ReportController();

    private ReportCloudinaryController reportCloudinaryController =
            new ReportCloudinaryController();

        // =====================================================
        // NOTIFICATION CONTROLLER
        // =====================================================

        private NotificationController notificationController =
                new NotificationController();


    // =====================================================
    // LOCAL DATA
    // =====================================================

    private ObservableList<Ship> shipData =
            FXCollections.observableArrayList();

    private ObservableList<Cargo> cargoData =
            FXCollections.observableArrayList();

    private ObservableList<Container> containerData =
            FXCollections.observableArrayList();

    private ObservableList<Payment> paymentData =
            FXCollections.observableArrayList();

    private ObservableList<Report> reportData =
            FXCollections.observableArrayList();


    // =====================================================
    // SUMMARY CARD LABELS
    // =====================================================

    private Label shipOperationsValue;

    private Label cargoHandledValue;

    private Label containersAllocatedValue;

    private Label totalRevenueValue;


    // =====================================================
    // CHART CONTAINER
    // =====================================================

    private VBox chartsContainer;


    // =====================================================
    // REPORT HISTORY TABLE
    // =====================================================

    private TableView<Report> reportTable;


    // =====================================================
    // DATE FILTER
    // =====================================================

    private ComboBox<String> dateRangeBox;

    private String selectedDateRange =
            "This Month";


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public ReportsPage() {

        loadData();
    }


    // =====================================================
    // LOAD DATA FROM FIREBASE
    // =====================================================

    private void loadData() {

        try {

            // -------------------------------------------------
            // LOAD SHIPS
            // -------------------------------------------------

            List<Ship> ships =
                    shipController.getShips();

            if (ships != null) {

                shipData.setAll(
                        ships
                );
            }


            // -------------------------------------------------
            // LOAD CARGO
            // -------------------------------------------------

            List<Cargo> cargos =
                    cargoController.getCargos();

            if (cargos != null) {

                cargoData.setAll(
                        cargos
                );
            }


            // -------------------------------------------------
            // LOAD CONTAINERS
            // -------------------------------------------------

            List<Container> containers =
                    containerController.getContainers();

            if (containers != null) {

                containerData.setAll(
                        containers
                );
            }


            // -------------------------------------------------
            // LOAD PAYMENTS
            // -------------------------------------------------

            List<Payment> payments =
                    paymentController.getPayments();

            if (payments != null) {

                paymentData.setAll(
                        payments
                );
            }


            // -------------------------------------------------
            // LOAD REPORTS
            // -------------------------------------------------

            List<Report> reports =
                    reportController.getReports();

            if (reports != null) {

                reportData.setAll(
                        reports
                );
            }


        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // MAIN PAGE
    // =========================================================

    public VBox getContent() {

        VBox page =
                new VBox(
                        20
                );

        page.setPadding(
                new Insets(30)
        );

        page.setStyle(
                "-fx-background-color: #f4f8fc;"
        );


        // =====================================================
        // PAGE TITLE
        // =====================================================

        Label title =
                new Label(
                        "Reports & Analytics"
                );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        32
                )
        );

        title.setTextFill(
                Color.web("#12203a")
        );


        Label subtitle =
                new Label(
                        "Analyze ship, cargo, container and payment operations."
                );

        subtitle.setStyle(
                "-fx-text-fill: #718096;" +
                "-fx-font-size: 14px;"
        );


        VBox heading =
                new VBox(
                        5,
                        title,
                        subtitle
                );


        // =====================================================
        // SUMMARY CARDS
        // =====================================================

        HBox summaryCards =
                createSummaryCards();


        // =====================================================
        // MONTH FILTER
        // =====================================================

        HBox filters =
                createFilters();


        // =====================================================
        // CHART CONTAINER
        // =====================================================

        chartsContainer =
                new VBox(
                        20
                );

        refreshCharts();


        // =====================================================
        // REPORT HISTORY
        // =====================================================

        VBox reportHistory =
                createReportHistory();


        // =====================================================
        // ADD EVERYTHING
        // =====================================================

        page.getChildren().addAll(
                heading,
                summaryCards,
                filters,
                chartsContainer,
                reportHistory
        );


        return page;
    }


    // =========================================================
    // SUMMARY CARDS
    // =========================================================

    private HBox createSummaryCards() {

        HBox cards =
                new HBox(
                        20
                );

        cards.setAlignment(
                Pos.CENTER
        );


        // -----------------------------------------------------
        // SHIP OPERATIONS
        // -----------------------------------------------------

        shipOperationsValue =
                new Label(
                        "0"
                );

        VBox shipCard =
                createCard(
                        "SHIP OPERATIONS",
                        shipOperationsValue,
                        "Ships in selected period"
                );


        // -----------------------------------------------------
        // CARGO HANDLED
        // -----------------------------------------------------

        cargoHandledValue =
                new Label(
                        "0 Tons"
                );

        VBox cargoCard =
                createCard(
                        "CARGO HANDLED",
                        cargoHandledValue,
                        "Total registered cargo"
                );


        // -----------------------------------------------------
        // CONTAINERS ALLOCATED
        // -----------------------------------------------------

        containersAllocatedValue =
                new Label(
                        "0"
                );

        VBox containerCard =
                createCard(
                        "CONTAINERS ALLOCATED",
                        containersAllocatedValue,
                        "Currently allocated"
                );


        // -----------------------------------------------------
        // TOTAL REVENUE
        // -----------------------------------------------------

        totalRevenueValue =
                new Label(
                        "₹0"
                );

        VBox revenueCard =
                createCard(
                        "TOTAL REVENUE",
                        totalRevenueValue,
                        "Payments in selected period"
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

            Label valueLabel,

            String description

    ) {

        VBox card =
                new VBox(
                        10
                );

        card.setPadding(
                new Insets(20)
        );

        card.setPrefHeight(
                130
        );

        card.setPrefWidth(
                250
        );

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #dbe4ef;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );


        Label headingLabel =
                new Label(
                        heading
                );

        headingLabel.setStyle(
                "-fx-text-fill: #718096;" +
                "-fx-font-size: 13px;"
        );


        valueLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        26
                )
        );

        valueLabel.setTextFill(
                Color.web("#12203a")
        );


        Label descriptionLabel =
                new Label(
                        description
                );

        descriptionLabel.setStyle(
                "-fx-text-fill: #00a878;" +
                "-fx-font-size: 13px;"
        );


        card.getChildren().addAll(
                headingLabel,
                valueLabel,
                descriptionLabel
        );


        return card;
    }


    // =========================================================
    // REFRESH SUMMARY CARDS
    // =========================================================

    private void refreshSummaryCards() {

        int shipCount =
                calculateShipOperations();


        double cargoWeight =
                calculateCargoWeight();


        int allocatedContainers =
                calculateAllocatedContainers();


        double revenue =
                calculateRevenue();


        if (shipOperationsValue != null) {

            shipOperationsValue.setText(
                    String.valueOf(
                            shipCount
                    )
            );
        }


        if (cargoHandledValue != null) {

            cargoHandledValue.setText(
                    formatNumber(
                            cargoWeight
                    )
                    +
                    " Tons"
            );
        }


        if (containersAllocatedValue != null) {

            containersAllocatedValue.setText(
                    String.valueOf(
                            allocatedContainers
                    )
            );
        }


        if (totalRevenueValue != null) {

            totalRevenueValue.setText(
                    formatCurrencyShort(
                            revenue
                    )
            );
        }
    }


    // =========================================================
    // MONTH FILTER
    // =========================================================

    private HBox createFilters() {

        HBox filters =
                new HBox(
                        15
                );

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


        Label filterLabel =
                new Label(
                        "Period:"
                );

        filterLabel.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #12203a;"
        );


        dateRangeBox =
                new ComboBox<>();

        dateRangeBox.getItems().addAll(
                "This Month",
                "Last Month",
                "Last 3 Months",
                "This Year",
                "All Time"
        );

        dateRangeBox.setValue(
                selectedDateRange
        );

        dateRangeBox.setPrefWidth(
                160
        );


        Button applyButton =
                new Button(
                        "Apply Filter"
                );

        applyButton.setStyle(
                "-fx-background-color: #2878e8;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;"
        );


        applyButton.setOnAction(
                event -> {

                    selectedDateRange =
                            dateRangeBox.getValue();


                    refreshSummaryCards();

                    refreshCharts();
                }
        );


        filters.getChildren().addAll(
                filterLabel,
                dateRangeBox,
                applyButton
        );


        return filters;
    }


    // =========================================================
    // REFRESH ALL CHARTS
    // =========================================================

    private void refreshCharts() {

        if (chartsContainer == null) {

            return;
        }


        chartsContainer.getChildren().clear();


        refreshSummaryCards();


        HBox chartRow1 =
                new HBox(
                        20
                );

        chartRow1.setFillHeight(
                true
        );


        chartRow1.getChildren().addAll(
                createShipActivityChart(),
                createCargoMovementChart()
        );


        HBox chartRow2 =
                new HBox(
                        20
                );

        chartRow2.setFillHeight(
                true
        );


        chartRow2.getChildren().addAll(
                createContainerAllocationChart(),
                createPaymentSummaryChart()
        );


        chartsContainer.getChildren().addAll(
                chartRow1,
                chartRow2
        );
    }


    // =========================================================
    // SHIP ACTIVITY CHART
    // =========================================================

    private VBox createShipActivityChart() {

        CategoryAxis xAxis =
                new CategoryAxis();

        NumberAxis yAxis =
                new NumberAxis();


        xAxis.setLabel(
                "Date"
        );

        yAxis.setLabel(
                "Ships"
        );


        LineChart<String, Number> chart =
                new LineChart<>(
                        xAxis,
                        yAxis
                );


        chart.setTitle(
                "Ship Activity"
        );

        chart.setLegendVisible(
                true
        );

        chart.setCreateSymbols(
                true
        );

        chart.setAnimated(
                false
        );

        chart.setPrefHeight(
                280
        );


        XYChart.Series<String, Number> arrivals =
                new XYChart.Series<>();

        arrivals.setName(
                "Arrivals"
        );


        XYChart.Series<String, Number> departures =
                new XYChart.Series<>();

        departures.setName(
                "Departures"
        );


        XYChart.Series<String, Number> docked =
                new XYChart.Series<>();

        docked.setName(
                "Docked"
        );


        LocalDate startDate =
                getPeriodStartDate();

        LocalDate endDate =
                getPeriodEndDate();


        if (startDate != null
                &&
                endDate != null) {

            LocalDate current =
                    startDate;


            while (!current.isAfter(endDate)) {

                String label =
                        String.valueOf(
                                current.getDayOfMonth()
                        );


                arrivals.getData().add(
                        new XYChart.Data<>(
                                label,
                                calculateShipActivityForDate(
                                        current,
                                        "Arrivals"
                                )
                        )
                );


                departures.getData().add(
                        new XYChart.Data<>(
                                label,
                                calculateShipActivityForDate(
                                        current,
                                        "Departures"
                                )
                        )
                );


                docked.getData().add(
                        new XYChart.Data<>(
                                label,
                                calculateShipActivityForDate(
                                        current,
                                        "Docked"
                                )
                        )
                );


                current =
                        current.plusDays(
                                1
                        );
            }
        }


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
    // =========================================================

    private VBox createCargoMovementChart() {

        CategoryAxis xAxis =
                new CategoryAxis();

        NumberAxis yAxis =
                new NumberAxis();


        xAxis.setLabel(
                "Status"
        );

        yAxis.setLabel(
                "Cargo"
        );


        BarChart<String, Number> chart =
                new BarChart<>(
                        xAxis,
                        yAxis
                );


        chart.setTitle(
                "Cargo Movement"
        );

        chart.setLegendVisible(
                false
        );

        chart.setAnimated(
                false
        );

        chart.setPrefHeight(
                280
        );


        XYChart.Series<String, Number> series =
                new XYChart.Series<>();


        series.getData().addAll(

                new XYChart.Data<>(
                        "Received",
                        countCargoByStatus(
                                "Received"
                        )
                ),

                new XYChart.Data<>(
                        "Loaded",
                        countCargoByStatus(
                                "Loaded"
                        )
                ),

                new XYChart.Data<>(
                        "Dispatched",
                        countCargoByStatus(
                                "Dispatched"
                        )
                ),

                new XYChart.Data<>(
                        "Pending",
                        countCargoByStatus(
                                "Pending"
                        )
                )
        );


        chart.getData().add(
                series
        );


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
                FXCollections.observableArrayList();


        data.add(
                new PieChart.Data(
                        "Allocated",
                        countContainersByStatus(
                                "Allocated"
                        )
                )
        );


        data.add(
                new PieChart.Data(
                        "Available",
                        countContainersByStatus(
                                "Available"
                        )
                )
        );


        data.add(
                new PieChart.Data(
                        "In Transit",
                        countContainersByStatus(
                                "In Transit"
                        )
                )
        );


        data.add(
                new PieChart.Data(
                        "Pending",
                        countContainersByStatus(
                                "Pending"
                        )
                )
        );


        PieChart chart =
                new PieChart(
                        data
                );


        chart.setTitle(
                "Container Allocation"
        );

        chart.setLegendVisible(
                true
        );

        chart.setPrefHeight(
                280
        );


        return createChartCard(
                "Container Allocation",
                "Current allocation status across the port",
                chart
        );
    }


    // =========================================================
    // PAYMENT SUMMARY CHART
    // =========================================================

    private VBox createPaymentSummaryChart() {

        ObservableList<PieChart.Data> data =
                FXCollections.observableArrayList();


        data.add(
                new PieChart.Data(
                        "Paid",
                        calculatePaymentAmount(
                                "Paid"
                        )
                )
        );


        data.add(
                new PieChart.Data(
                        "Pending",
                        calculatePaymentAmount(
                                "Pending"
                        )
                )
        );


        data.add(
                new PieChart.Data(
                        "Overdue",
                        calculatePaymentAmount(
                                "Overdue"
                        )
                )
        );


        PieChart chart =
                new PieChart(
                        data
                );


        chart.setTitle(
                "Payment Summary"
        );

        chart.setLegendVisible(
                true
        );

        chart.setPrefHeight(
                280
        );


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
                new VBox(
                        8
                );


        card.setPadding(
                new Insets(20)
        );


        card.setPrefWidth(
                500
        );


        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #dbe4ef;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );


        Label titleLabel =
                new Label(
                        title
                );


        titleLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        18
                )
        );


        titleLabel.setTextFill(
                Color.web("#12203a")
        );


        Label descriptionLabel =
                new Label(
                        description
                );


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
        // REPORT HISTORY
        // =========================================================

        private VBox createReportHistory() {

                VBox section =
                        new VBox(
                                15
                        );


                section.setPadding(
                        new Insets(20)
                );


                section.setStyle(
                        "-fx-background-color: white;" +
                        "-fx-border-color: #dbe4ef;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
                );


                // =====================================================
                // TITLE
                // =====================================================

                Label title =
                        new Label(
                                "Report History"
                        );


                title.setFont(
                        Font.font(
                                "Arial",
                                FontWeight.BOLD,
                                20
                        )
                );


                title.setTextFill(
                        Color.web("#12203a")
                );


                Label subtitle =
                        new Label(
                                "Generated overall port operation reports"
                        );


                subtitle.setStyle(
                        "-fx-text-fill: #718096;" +
                        "-fx-font-size: 13px;"
                );


                VBox heading =
                        new VBox(
                                5,
                                title,
                                subtitle
                        );


                // =====================================================
                // GENERATE BUTTON
                // =====================================================

                Button generateButton =
                        new Button(
                                "+ Generate Report"
                        );


                generateButton.setStyle(
                        "-fx-background-color: #2878e8;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 7 12 7 12;" +
                        "-fx-cursor: hand;"
                );


                HBox buttons =
                        new HBox(
                                generateButton
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


                // =====================================================
                // REPORT TABLE
                // =====================================================

                reportTable =
                        new TableView<>();


                reportTable.setPrefHeight(
                        250
                );


                reportTable.setFixedCellSize(
                        38
                );


                reportTable.setStyle(
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-table-cell-border-color: transparent;" +
                        "-fx-font-size: 11px;"
                );


                // =====================================================
                // REPORT ID
                // =====================================================

                TableColumn<Report, String> reportId =
                        createReportColumn(
                                "Report ID",
                                110,
                                Report::getReportId
                        );


                // =====================================================
                // REPORT NAME
                // =====================================================

                TableColumn<Report, String> reportName =
                        createReportColumn(
                                "Report Name",
                                220,
                                Report::getReportName
                        );


                // =====================================================
                // GENERATED DATE
                // =====================================================

                TableColumn<Report, String> date =
                        createReportColumn(
                                "Generated Date",
                                150,
                                Report::getGeneratedDate
                        );


                // =====================================================
                // STATUS
                // =====================================================

                TableColumn<Report, String> status =
                        createReportColumn(
                                "Status",
                                110,
                                Report::getStatus
                        );


                // =====================================================
                // STYLE NORMAL COLUMNS
                // =====================================================

                styleReportColumn(
                        reportName
                );


                styleReportColumn(
                        date
                );


                // =====================================================
                // STYLE IMPORTANT COLUMNS
                // =====================================================

                styleReportBoldColumn(
                        reportId
                );


                // =====================================================
                // STATUS BADGE
                // =====================================================

                status.setCellFactory(
                        column ->
                                new TableCell<Report, String>() {

                                        private final Label statusLabel =
                                                new Label();


                                        @Override
                                        protected void updateItem(
                                                String item,
                                                boolean empty
                                        ) {

                                        super.updateItem(
                                                item,
                                                empty
                                        );


                                        if (empty || item == null) {

                                                setGraphic(
                                                        null
                                                );

                                                setText(
                                                        null
                                                );

                                                return;
                                        }


                                        statusLabel.setText(
                                                item
                                        );


                                        // =============================================
                                        // READY
                                        // =============================================

                                        if (item.equalsIgnoreCase(
                                                "Ready"
                                        )) {

                                                statusLabel.setStyle(
                                                        "-fx-background-color:#dcfce7;" +
                                                        "-fx-text-fill:#15803d;" +
                                                        "-fx-background-radius:12;" +
                                                        "-fx-font-size:10;" +
                                                        "-fx-font-weight:bold;" +
                                                        "-fx-padding:4 10 4 10;"
                                                );


                                        // =============================================
                                        // GENERATING
                                        // =============================================

                                        } else if (item.equalsIgnoreCase(
                                                "Generating"
                                        )) {

                                                statusLabel.setStyle(
                                                        "-fx-background-color:#ffedd5;" +
                                                        "-fx-text-fill:#ea580c;" +
                                                        "-fx-background-radius:12;" +
                                                        "-fx-font-size:10;" +
                                                        "-fx-font-weight:bold;" +
                                                        "-fx-padding:4 10 4 10;"
                                                );


                                        // =============================================
                                        // DEFAULT
                                        // =============================================

                                        } else {

                                                statusLabel.setStyle(
                                                        "-fx-background-color:#f1f5f9;" +
                                                        "-fx-text-fill:#475569;" +
                                                        "-fx-background-radius:12;" +
                                                        "-fx-font-size:10;" +
                                                        "-fx-font-weight:bold;" +
                                                        "-fx-padding:4 10 4 10;"
                                                );
                                        }


                                        setGraphic(
                                                statusLabel
                                        );


                                        setText(
                                                null
                                        );


                                        setAlignment(
                                                Pos.CENTER
                                        );


                                        setStyle(
                                                "-fx-background-color:white;" +
                                                "-fx-border-color:transparent transparent #e8edf2 transparent;"
                                        );
                                        }
                                }
                );


                // =====================================================
                // ACTION COLUMN
                // =====================================================

                TableColumn<Report, String> action =
                        new TableColumn<>(
                                "Action"
                        );


                action.setPrefWidth(
                        250
                );


                action.setCellFactory(
                        column ->
                                new TableCell<Report, String>() {

                                        private final Button viewButton =
                                                new Button(
                                                        "View"
                                                );


                                        private final Button exportButton =
                                                new Button(
                                                        "Export"
                                                );


                                        private final Button deleteButton =
                                                new Button(
                                                        "Delete"
                                                );


                                        {

                                        // =============================================
                                        // VIEW BUTTON
                                        // =============================================

                                        viewButton.setStyle(
                                                "-fx-background-color:#f1f5f9;" +
                                                "-fx-text-fill:#334155;" +
                                                "-fx-font-size:10px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-background-radius:5;" +
                                                "-fx-border-color:#cbd5e1;" +
                                                "-fx-border-radius:5;" +
                                                "-fx-padding:5 10 5 10;" +
                                                "-fx-cursor:hand;"
                                        );


                                        // =============================================
                                        // EXPORT BUTTON
                                        // =============================================

                                        exportButton.setStyle(
                                                "-fx-background-color:#eff6ff;" +
                                                "-fx-text-fill:#2563eb;" +
                                                "-fx-font-size:10px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-background-radius:5;" +
                                                "-fx-border-color:#bfdbfe;" +
                                                "-fx-border-radius:5;" +
                                                "-fx-padding:5 10 5 10;" +
                                                "-fx-cursor:hand;"
                                        );


                                        // =============================================
                                        // DELETE BUTTON
                                        // =============================================

                                        deleteButton.setStyle(
                                                "-fx-background-color:#fff1f2;" +
                                                "-fx-text-fill:#dc2626;" +
                                                "-fx-font-size:10px;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-background-radius:5;" +
                                                "-fx-border-color:#fecdd3;" +
                                                "-fx-border-radius:5;" +
                                                "-fx-padding:5 10 5 10;" +
                                                "-fx-cursor:hand;"
                                        );


                                        // =============================================
                                        // VIEW
                                        // =============================================

                                        viewButton.setOnAction(
                                                event -> {

                                                        Report report =
                                                                getTableView()
                                                                        .getItems()
                                                                        .get(
                                                                                getIndex()
                                                                        );


                                                        if (report != null) {

                                                        viewReport(
                                                                report
                                                        );
                                                        }
                                                }
                                        );


                                        // =============================================
                                        // EXPORT
                                        // =============================================

                                        exportButton.setOnAction(
                                                event -> {

                                                        Report report =
                                                                getTableView()
                                                                        .getItems()
                                                                        .get(
                                                                                getIndex()
                                                                        );


                                                        if (report != null) {

                                                        exportReport(
                                                                report
                                                        );
                                                        }
                                                }
                                        );


                                        // =============================================
                                        // DELETE
                                        // =============================================

                                        deleteButton.setOnAction(
                                                event -> {

                                                        Report report =
                                                                getTableView()
                                                                        .getItems()
                                                                        .get(
                                                                                getIndex()
                                                                        );


                                                        if (report != null) {

                                                        deleteReport(
                                                                report
                                                        );
                                                        }
                                                }
                                        );
                                        }


                                        @Override
                                        protected void updateItem(
                                                String item,
                                                boolean empty
                                        ) {

                                        super.updateItem(
                                                item,
                                                empty
                                        );


                                        if (empty) {

                                                setGraphic(
                                                        null
                                                );

                                                setText(
                                                        null
                                                );

                                        } else {

                                                HBox box =
                                                        new HBox(
                                                                6,
                                                                viewButton,
                                                                exportButton,
                                                                deleteButton
                                                        );


                                                box.setAlignment(
                                                        Pos.CENTER_LEFT
                                                );


                                                setGraphic(
                                                        box
                                                );


                                                setText(
                                                        null
                                                );


                                                setStyle(
                                                        "-fx-background-color:white;" +
                                                        "-fx-border-color:transparent transparent #e8edf2 transparent;" +
                                                        "-fx-padding:0 5 0 5;"
                                                );
                                        }
                                        }
                                }
                );


                // =====================================================
                // ADD COLUMNS
                // =====================================================

                reportTable.getColumns().addAll(
                        reportId,
                        reportName,
                        date,
                        status,
                        action
                );


                // =====================================================
                // STYLE TABLE HEADER
                // =====================================================

                reportTable.skinProperty().addListener(
                        (observable, oldSkin, newSkin) -> {

                                if (newSkin != null) {

                                javafx.application.Platform.runLater(
                                        () ->
                                                styleReportTableHeader(
                                                        reportTable
                                                )
                                );
                                }
                        }
                );


                // =====================================================
                // LOAD REPORT DATA
                // =====================================================

                refreshReportTable();


                // =====================================================
                // GENERATE REPORT
                // =====================================================

                generateButton.setOnAction(
                        event -> {

                                showGenerateReportDialog();
                        }
                );


                // =====================================================
                // ADD EVERYTHING
                // =====================================================

                section.getChildren().addAll(
                        top,
                        reportTable
                );


                return section;
        }

        // =========================================================
        // STYLE NORMAL REPORT COLUMN
        // =========================================================

        private void styleReportColumn(
                        TableColumn<Report, String> column
                ) {

                column.setCellFactory(
                        col ->
                                new TableCell<Report, String>() {

                                        @Override
                                        protected void updateItem(
                                                String item,
                                                boolean empty
                                        ) {

                                        super.updateItem(
                                                item,
                                                empty
                                        );


                                        if (empty || item == null) {

                                                setText(
                                                        null
                                                );

                                                return;
                                        }


                                        setText(
                                                item
                                        );


                                        setStyle(
                                                "-fx-background-color:white;" +
                                                "-fx-text-fill:#475569;" +
                                                "-fx-font-size:11;" +
                                                "-fx-border-color:transparent transparent #e8edf2 transparent;" +
                                                "-fx-padding:0 8 0 8;"
                                        );


                                        setAlignment(
                                                Pos.CENTER_LEFT
                                        );
                                        }
                                }
                );
        }

        // =========================================================
        // STYLE BOLD REPORT COLUMN
        // =========================================================

        private void styleReportBoldColumn(
                        TableColumn<Report, String> column
                ) {

                column.setCellFactory(
                        col ->
                                new TableCell<Report, String>() {

                                        @Override
                                        protected void updateItem(
                                                String item,
                                                boolean empty
                                        ) {

                                        super.updateItem(
                                                item,
                                                empty
                                        );


                                        if (empty || item == null) {

                                                setText(
                                                        null
                                                );

                                                return;
                                        }


                                        setText(
                                                item
                                        );


                                        setStyle(
                                                "-fx-background-color:white;" +
                                                "-fx-text-fill:#1e293b;" +
                                                "-fx-font-size:11;" +
                                                "-fx-font-weight:bold;" +
                                                "-fx-border-color:transparent transparent #e8edf2 transparent;" +
                                                "-fx-padding:0 8 0 8;"
                                        );


                                        setAlignment(
                                                Pos.CENTER_LEFT
                                        );
                                        }
                                }
                );
        }


        // =========================================================
        // MODERN REPORT TABLE HEADER
        // =========================================================

        private void styleReportTableHeader(
                        TableView<Report> table
                ) {

                table.applyCss();


                // =====================================================
                // HEADER BACKGROUND
                // =====================================================

                Node headerBackground =
                        table.lookup(
                                ".column-header-background"
                        );


                if (headerBackground != null) {

                        headerBackground.setStyle(
                                "-fx-background-color:#f1f5f9;" +
                                "-fx-background-radius:0;" +
                                "-fx-border-color:transparent;"
                        );
                }


                // =====================================================
                // INDIVIDUAL COLUMN HEADERS
                // =====================================================

                for (
                        Node node :
                        table.lookupAll(".column-header")
                ) {

                        node.setStyle(
                                "-fx-background-color:#f1f5f9;" +
                                "-fx-border-color:transparent transparent #e2e8f0 transparent;" +
                                "-fx-background-radius:0;"
                        );
                }


                // =====================================================
                // HEADER TEXT
                // =====================================================

                for (
                        Node node :
                        table.lookupAll(".column-header .label")
                ) {

                        node.setStyle(
                                "-fx-text-fill:#334155;" +
                                "-fx-font-size:10;" +
                                "-fx-font-weight:bold;" +
                                "-fx-background-color:transparent;"
                        );
                }


                // =====================================================
                // EMPTY HEADER / FILLER
                // =====================================================

                Node filler =
                        table.lookup(
                                ".filler"
                        );


                if (filler != null) {

                        filler.setStyle(
                                "-fx-background-color:#f1f5f9;" +
                                "-fx-border-color:transparent;"
                        );
                }
        }


    // =========================================================
    // CREATE REPORT TABLE COLUMN
    // =========================================================

    private TableColumn<Report, String> createReportColumn(

            String title,

            double width,

            java.util.function.Function<Report, String> getter

    ) {

        TableColumn<Report, String> column =
                new TableColumn<>(
                        title
                );


        column.setPrefWidth(
                width
        );


        column.setCellValueFactory(
                data -> {

                    Report report =
                            data.getValue();


                    if (report == null) {

                        return new SimpleStringProperty(
                                ""
                        );
                    }


                    String value =
                            getter.apply(
                                    report
                            );


                    return new SimpleStringProperty(
                            safeValue(
                                    value
                            )
                    );
                }
        );


        return column;
    }


    // =========================================================
    // REFRESH REPORT TABLE
    // =========================================================

    private void refreshReportTable() {

        try {

            List<Report> reports =
                    reportController.getReports();


            reportData.clear();


            if (reports != null) {

                reportData.addAll(
                        reports
                );
            }


            if (reportTable != null) {

                reportTable.setItems(
                        reportData
                );

                reportTable.refresh();
            }


        } catch (Exception e) {

            e.printStackTrace();


            showWarning(
                    "Unable to load reports from Firebase."
            );
        }
    }


    // =========================================================
    // GENERATE OVERALL REPORT DIALOG
    // =========================================================

    private void showGenerateReportDialog() {

        Dialog<ButtonType> dialog =
                new Dialog<>();


        dialog.setTitle(
                "Generate Report"
        );


        dialog.setHeaderText(
                "Generate Overall Port Operations Report"
        );


        TextField nameField =
                new TextField();


        nameField.setPromptText(
                "Report Name"
        );


        nameField.setPrefWidth(
                250
        );


        GridPane grid =
                new GridPane();


        grid.setHgap(
                10
        );


        grid.setVgap(
                12
        );


        grid.setPadding(
                new Insets(20)
        );


        grid.add(
                new Label(
                        "Report Name:"
                ),
                0,
                0
        );


        grid.add(
                nameField,
                1,
                0
        );


        dialog.getDialogPane().setContent(
                grid
        );


        ButtonType generateButton =
                new ButtonType(
                        "Generate",
                        ButtonBar.ButtonData.OK_DONE
                );


        ButtonType cancelButton =
                new ButtonType(
                        "Cancel",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );


        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        generateButton,
                        cancelButton
                );


        dialog.setResultConverter(
                button -> {

                    if (button != generateButton) {

                        return button;
                    }


                    String reportName =
                            nameField
                                    .getText()
                                    .trim();


                    if (reportName.isEmpty()) {

                        showWarning(
                                "Please enter a Report Name."
                        );


                        return null;
                    }


                    generateAndStoreReport(
                            reportName
                    );


                    return button;
                }
        );


        dialog.showAndWait();
    }


    // =========================================================
    // GENERATE AND STORE REPORT
    // =========================================================

    private void generateAndStoreReport(
            String reportName
    ) {

        File temporaryPdf =
                null;


        try {

            // =================================================
            // CREATE REPORT OBJECT
            // =================================================

            String reportId =
                    generateReportId();


            String generatedDate =
                    LocalDate.now()
                            .toString();


            Report report =
                    new Report(

                            reportId,

                            reportName,

                            generatedDate,

                            "Generating"
                    );


            // =================================================
            // CREATE TEMPORARY PDF
            // =================================================

            temporaryPdf =
                    File.createTempFile(
                            "docktech_"
                            +
                            reportId
                            +
                            "_",
                            ".pdf"
                    );


            PdfReportGenerator.generateReport(

                    report,

                    temporaryPdf,

                    shipData,

                    cargoData,

                    containerData,

                    paymentData
            );


            // =================================================
            // VERIFY PDF
            // =================================================

            if (!temporaryPdf.exists()
                    ||
                    temporaryPdf.length() == 0) {

                showWarning(
                        "The report PDF could not be generated."
                );

                return;
            }


            // =================================================
            // UPLOAD TO CLOUDINARY
            // =================================================

            Map<String, Object> uploadResult =
                    reportCloudinaryController
                            .uploadReport(
                                    temporaryPdf
                            );


            if (uploadResult == null) {

                showWarning(
                        "The PDF was generated, but it could not "
                        +
                        "be uploaded to Cloudinary."
                );

                return;
            }


            // =================================================
            // GET CLOUDINARY URL
            // =================================================

            String pdfUrl =
                    reportCloudinaryController
                            .getSecureUrl(
                                    uploadResult
                            );


            String publicId =
                    reportCloudinaryController
                            .getPublicId(
                                    uploadResult
                            );


            if (pdfUrl == null
                    ||
                    pdfUrl.trim().isEmpty()) {

                showWarning(
                        "Cloudinary upload completed, but no "
                        +
                        "PDF URL was returned."
                );

                return;
            }


            // =================================================
            // UPDATE REPORT
            // =================================================

            report.setPdfUrl(
                    pdfUrl
            );


            report.setCloudinaryPublicId(
                    publicId
            );


            report.setStatus(
                    "Ready"
            );


            // =================================================
            // SAVE REPORT TO FIRESTORE
            // =================================================

            reportController.saveReport(
                    report
            );

            // =================================================
                // CREATE NOTIFICATION
                // =================================================

                notificationController.createNotification(

                        "Report generated",

                        report.getReportName()
                                +
                                " was generated successfully.",

                        "REPORT"
                );


            // =================================================
            // UPDATE LOCAL TABLE
            // =================================================

            reportData.add(
                    report
            );


            if (reportTable != null) {

                reportTable.refresh();
            }


            // =================================================
            // SUCCESS
            // =================================================

            showSuccess(

                    "Overall report generated successfully."
                    +
                    "\n\n"
                    +
                    "Report ID: "
                    +
                    reportId
                    +
                    "\n\n"
                    +
                    "PDF uploaded to Cloudinary."
            );


        } catch (Exception e) {

            e.printStackTrace();


            showWarning(

                    "Unable to generate the report.\n\n"
                    +
                    e.getClass().getSimpleName()
                    +
                    "\n\n"
                    +
                    safeValue(
                            e.getMessage()
                    )
            );


        } finally {

            // =================================================
            // DELETE TEMPORARY LOCAL PDF
            // =================================================

            if (temporaryPdf != null
                    &&
                    temporaryPdf.exists()) {

                try {

                    Files.deleteIfExists(
                            temporaryPdf.toPath()
                    );

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }
    }


    // =========================================================
    // GENERATE REPORT ID
    // =========================================================

    private String generateReportId() {

        int maxNumber =
                0;


        for (Report report : reportData) {

            if (report == null
                    ||
                    report.getReportId() == null) {

                continue;
            }


            String id =
                    report.getReportId()
                            .trim()
                            .toUpperCase();


            if (!id.startsWith(
                    "RPT"
            )) {

                continue;
            }


            try {

                String number =
                        id.substring(
                                3
                        )
                        .replace(
                                "-",
                                ""
                        );


                int value =
                        Integer.parseInt(
                                number
                        );


                if (value > maxNumber) {

                    maxNumber =
                            value;
                }

            } catch (Exception ignored) {
            }
        }


        return String.format(
                "RPT-%03d",
                maxNumber + 1
        );
    }


    // =========================================================
    // VIEW REPORT
    // =========================================================

    private void viewReport(
            Report report
    ) {

        String pdfUrl =
                report.getPdfUrl();


        if (pdfUrl == null
                ||
                pdfUrl.trim().isEmpty()) {

            showWarning(

                    "This report does not have a PDF stored "
                    +
                    "on Cloudinary.\n\n"
                    +
                    "This may be an older report created before "
                    +
                    "Cloudinary integration."
            );

            return;
        }


        try {

            if (!Desktop.isDesktopSupported()) {

                showWarning(
                        "Your system does not support opening "
                        +
                        "the report automatically."
                );

                return;
            }


            Desktop desktop =
                    Desktop.getDesktop();


            if (!desktop.isSupported(
                    Desktop.Action.BROWSE
            )) {

                showWarning(
                        "Your system does not support opening "
                        +
                        "the report URL."
                );

                return;
            }


            desktop.browse(
                    new URI(
                            pdfUrl
                    )
            );


        } catch (Exception e) {

            e.printStackTrace();


            showWarning(

                    "Unable to open the report.\n\n"
                    +
                    e.getClass().getSimpleName()
                    +
                    "\n\n"
                    +
                    safeValue(
                            e.getMessage()
                    )
            );
        }
    }


    // =========================================================
    // EXPORT REPORT
    // =========================================================

    private void exportReport(
            Report report
    ) {

        String pdfUrl =
                report.getPdfUrl();


        if (pdfUrl == null
                ||
                pdfUrl.trim().isEmpty()) {

            showWarning(

                    "This report does not have a PDF stored "
                    +
                    "on Cloudinary.\n\n"
                    +
                    "This may be an older report created before "
                    +
                    "Cloudinary integration."
            );

            return;
        }


        FileChooser fileChooser =
                new FileChooser();


        fileChooser.setTitle(
                "Save Overall Report as PDF"
        );


        fileChooser.setInitialFileName(
                report.getReportName()
                +
                ".pdf"
        );


        FileChooser.ExtensionFilter pdfFilter =
                new FileChooser.ExtensionFilter(
                        "PDF Files (*.pdf)",
                        "*.pdf"
                );


        fileChooser.getExtensionFilters().add(
                pdfFilter
        );


        File file =
                fileChooser.showSaveDialog(
                        reportTable
                                .getScene()
                                .getWindow()
                );


        // -----------------------------------------------------
        // USER CANCELLED
        // -----------------------------------------------------

        if (file == null) {

            return;
        }


        // -----------------------------------------------------
        // MAKE SURE .PDF EXTENSION EXISTS
        // -----------------------------------------------------

        if (!file.getName()
                .toLowerCase()
                .endsWith(
                        ".pdf"
                )) {

            file =
                    new File(
                            file.getAbsolutePath()
                                    +
                                    ".pdf"
                    );
        }


        try {

            // =================================================
            // DOWNLOAD EXISTING CLOUDINARY PDF
            // =================================================

            URL url =
                    new URL(
                            pdfUrl
                    );


            try (
                    InputStream inputStream =
                            url.openStream()
            ) {

                Files.copy(

                        inputStream,

                        file.toPath(),

                        StandardCopyOption.REPLACE_EXISTING
                );
            }


            // =================================================
            // VERIFY FILE
            // =================================================

            if (!file.exists()
                    ||
                    file.length() == 0) {

                showWarning(
                        "The PDF could not be downloaded."
                );

                return;
            }


            // =================================================
            // SUCCESS
            // =================================================

            showSuccess(

                    "Report exported successfully."
                    +
                    "\n\n"
                    +
                    "Saved to:\n"
                    +
                    file.getAbsolutePath()
                    +
                    "\n\n"
                    +
                    "File size: "
                    +
                    file.length()
                    +
                    " bytes"
            );


        } catch (Exception e) {

            e.printStackTrace();


            showWarning(

                    "Unable to export the report.\n\n"
                    +
                    e.getClass().getSimpleName()
                    +
                    "\n\n"
                    +
                    safeValue(
                            e.getMessage()
                    )
            );
        }
    }


        // =========================================================
        // DELETE REPORT
        // =========================================================

        private void deleteReport(Report report){

                Alert alert =
                        new Alert(
                                Alert.AlertType.CONFIRMATION
                        );


                alert.setTitle(
                        "Delete Report"
                );


                alert.setHeaderText(
                        "Delete Report?"
                );


                alert.setContentText(

                        "Are you sure you want to delete "
                        +
                        safeValue(
                                report.getReportId()
                        )
                        +
                        "?"
                        +
                        "\n\n"
                        +
                        "The stored PDF will also be permanently "
                        +
                        "deleted from Cloudinary."
                );


                ButtonType deleteButton =
                        new ButtonType(
                                "Delete",
                                ButtonBar.ButtonData.OK_DONE
                        );


                ButtonType cancelButton =
                        new ButtonType(
                                "Cancel",
                                ButtonBar.ButtonData.CANCEL_CLOSE
                        );


                alert.getButtonTypes().setAll(
                        deleteButton,
                        cancelButton
                );


                alert.showAndWait()
                        .ifPresent(
                                response -> {

                                        if (response != deleteButton) {

                                        return;
                                        }


                                        // =============================================
                                        // DELETE CLOUDINARY PDF FIRST
                                        // =============================================

                                        String publicId =
                                                report.getCloudinaryPublicId();


                                        if (publicId != null
                                                &&
                                                !publicId.trim().isEmpty()) {

                                        boolean cloudinaryDeleted =
                                                reportCloudinaryController
                                                        .deleteReport(
                                                                publicId
                                                        );


                                        if (!cloudinaryDeleted) {

                                                showWarning(

                                                        "The PDF could not be deleted "
                                                        +
                                                        "from Cloudinary."
                                                        +
                                                        "\n\n"
                                                        +
                                                        "The report has NOT been deleted "
                                                        +
                                                        "from Firestore."
                                                );

                                                return;
                                        }
                                        }


                                        // =============================================
                                        // DELETE FIRESTORE RECORD
                                        // =============================================

                                        reportController.deleteReport(
                                                report.getReportId()
                                        );

                                        // =============================================
                                        // CREATE NOTIFICATION
                                        // =============================================

                                        notificationController.createNotification(

                                                "Report deleted",

                                                report.getReportName()
                                                        +
                                                        " was deleted from the system.",

                                                "DELETE"
                                        );


                                        // =============================================
                                        // REMOVE FROM LOCAL TABLE
                                        // =============================================

                                        reportData.remove(
                                                report
                                        );


                                        if (reportTable != null) {

                                        reportTable.refresh();
                                        }


                                        showSuccess(
                                                "Report deleted successfully."
                                        );
                                }
                        );
        }


    // =========================================================
    // SHIP OPERATIONS
    // =========================================================

    private int calculateShipOperations() {

        int count =
                0;


        for (Ship ship : shipData) {

            if (ship == null) {

                continue;
            }


            LocalDate arrivalDate =
                    parseDate(
                            ship.getArrival()
                    );


            LocalDate departureDate =
                    parseDate(
                            ship.getDeparture()
                    );


            boolean arrivalMatches =
                    arrivalDate != null
                    &&
                    matchesDate(
                            arrivalDate
                    );


            boolean departureMatches =
                    departureDate != null
                    &&
                    matchesDate(
                            departureDate
                    );


            if (arrivalMatches
                    ||
                    departureMatches) {

                count++;

                continue;
            }


            if (arrivalDate == null
                    &&
                    departureDate == null) {

                count++;
            }
        }


        return count;
    }


    // =========================================================
    // CARGO WEIGHT
    // =========================================================

    private double calculateCargoWeight() {

        double total =
                0;


        for (Cargo cargo : cargoData) {

            if (cargo == null) {

                continue;
            }


            String weight =
                    cargo.getWeight();


            if (weight == null) {

                continue;
            }


            try {

                String numeric =
                        weight.replaceAll(
                                "[^0-9.\\-]",
                                ""
                        );


                if (!numeric.isEmpty()) {

                    total +=
                            Double.parseDouble(
                                    numeric
                            );
                }

            } catch (Exception ignored) {
            }
        }


        return total;
    }


    // =========================================================
    // ALLOCATED CONTAINERS
    // =========================================================

    private int calculateAllocatedContainers() {

        int count =
                0;


        for (Container container : containerData) {

            if (container == null) {

                continue;
            }


            String status =
                    safeValue(
                            container.getStatus()
                    );


            if ("Allocated".equalsIgnoreCase(
                    status
            )) {

                count++;
            }
        }


        return count;
    }


    // =========================================================
    // PAYMENT REVENUE
    // =========================================================

    private double calculateRevenue() {

        double total =
                0;


        for (Payment payment : paymentData) {

            if (payment == null) {

                continue;
            }


            if (!matchesDate(
                    payment.getDate()
            )) {

                continue;
            }


            total +=
                    payment.getAmount();
        }


        return total;
    }


    // =========================================================
    // CARGO STATUS COUNTER
    // =========================================================

    private int countCargoByStatus(
            String targetStatus
    ) {

        int count =
                0;


        for (Cargo cargo : cargoData) {

            if (cargo == null) {

                continue;
            }


            String status =
                    safeValue(
                            cargo.getStatus()
                    )
                    .trim();


            // -------------------------------------------------
            // Pending Inspection is shown as Pending
            // -------------------------------------------------

            if (status.equalsIgnoreCase(
                    "Pending Inspection"
            )) {

                status =
                        "Pending";
            }


            if (targetStatus.equalsIgnoreCase(
                    status
            )) {

                count++;
            }
        }


        return count;
    }


    // =========================================================
    // CONTAINER STATUS COUNTER
    // =========================================================

    private int countContainersByStatus(
            String targetStatus
    ) {

        int count =
                0;


        for (Container container : containerData) {

            if (container == null) {

                continue;
            }


            String status =
                    safeValue(
                            container.getStatus()
                    );


            if (targetStatus.equalsIgnoreCase(
                    status
            )) {

                count++;
            }
        }


        return count;
    }


    // =========================================================
    // PAYMENT AMOUNT BY STATUS
    // =========================================================

    private double calculatePaymentAmount(
            String targetStatus
    ) {

        double total =
                0;


        for (Payment payment : paymentData) {

            if (payment == null) {

                continue;
            }


            if (!matchesDate(
                    payment.getDate()
            )) {

                continue;
            }


            String status =
                    safeValue(
                            payment.getStatus()
                    );


            if (!targetStatus.equalsIgnoreCase(
                    status
            )) {

                continue;
            }


            total +=
                    payment.getAmount();
        }


        return total;
    }


    // =========================================================
    // SHIP ACTIVITY FOR SPECIFIC DATE
    // =========================================================

    private int calculateShipActivityForDate(

            LocalDate targetDate,

            String activity

    ) {

        int count =
                0;


        for (Ship ship : shipData) {

            if (ship == null) {

                continue;
            }


            LocalDate date =
                    null;


            if ("Arrivals".equals(
                    activity
            )) {

                date =
                        parseDate(
                                ship.getArrival()
                        );
            }


            else if (
                    "Departures".equals(
                            activity
                    )
            ) {

                date =
                        parseDate(
                                ship.getDeparture()
                        );
            }


            else if (
                    "Docked".equals(
                            activity
                    )
            ) {

                if (!"Docked".equalsIgnoreCase(
                        safeValue(
                                ship.getStatus()
                        )
                )) {

                    continue;
                }


                date =
                        parseDate(
                                ship.getArrival()
                        );


                if (date == null) {

                    date =
                            parseDate(
                                    ship.getDeparture()
                            );
                }
            }


            if (date == null) {

                continue;
            }


            if (date.equals(
                    targetDate
            )) {

                count++;
            }
        }


        return count;
    }


    // =========================================================
    // DATE FILTER
    // =========================================================

    private boolean matchesDate(
            String dateString
    ) {

        LocalDate date =
                parseDate(
                        dateString
                );


        if (date == null) {

            return true;
        }


        return matchesDate(
                date
        );
    }


    // =========================================================
    // DATE FILTER
    // =========================================================

    private boolean matchesDate(
            LocalDate date
    ) {

        if (date == null) {

            return true;
        }


        if ("All Time".equals(
                selectedDateRange
        )) {

            return true;
        }


        LocalDate today =
                LocalDate.now();


        switch (selectedDateRange) {

            case "This Month":

                return date.getYear()
                        ==
                        today.getYear()

                        &&

                        date.getMonth()
                                ==
                                today.getMonth();


            case "Last Month":

                LocalDate lastMonth =
                        today.minusMonths(
                                1
                        );


                return date.getYear()
                        ==
                        lastMonth.getYear()

                        &&

                        date.getMonth()
                                ==
                                lastMonth.getMonth();


            case "Last 3 Months":

                LocalDate threeMonthsAgo =
                        today.minusMonths(
                                3
                        );


                return !date.isBefore(
                        threeMonthsAgo
                )
                &&
                !date.isAfter(
                        today
                );


            case "This Year":

                return date.getYear()
                        ==
                        today.getYear();


            default:

                return true;
        }
    }


    // =========================================================
    // PERIOD START DATE
    // =========================================================

    private LocalDate getPeriodStartDate() {

        LocalDate today =
                LocalDate.now();


        switch (selectedDateRange) {

            case "This Month":

                return today.withDayOfMonth(
                        1
                );


            case "Last Month":

                LocalDate lastMonth =
                        today.minusMonths(
                                1
                        );


                return lastMonth.withDayOfMonth(
                        1
                );


            case "Last 3 Months":

                return today.minusMonths(
                        3
                );


            case "This Year":

                return today.withDayOfYear(
                        1
                );


            case "All Time":

                return findEarliestShipDate();


            default:

                return today.withDayOfMonth(
                        1
                );
        }
    }


    // =========================================================
    // PERIOD END DATE
    // =========================================================

    private LocalDate getPeriodEndDate() {

        LocalDate today =
                LocalDate.now();


        switch (selectedDateRange) {

            case "This Month":

                return today.withDayOfMonth(
                        today.lengthOfMonth()
                );


            case "Last Month":

                LocalDate lastMonth =
                        today.minusMonths(
                                1
                        );


                return lastMonth.withDayOfMonth(
                        lastMonth.lengthOfMonth()
                );


            case "Last 3 Months":

                return today;


            case "This Year":

                return today.withDayOfYear(
                        today.lengthOfYear()
                );


            case "All Time":

                return findLatestShipDate();


            default:

                return today.withDayOfMonth(
                        today.lengthOfMonth()
                );
        }
    }


    // =========================================================
    // FIND EARLIEST SHIP DATE
    // =========================================================

    private LocalDate findEarliestShipDate() {

        LocalDate earliest =
                null;


        for (Ship ship : shipData) {

            if (ship == null) {

                continue;
            }


            LocalDate arrival =
                    parseDate(
                            ship.getArrival()
                    );


            LocalDate departure =
                    parseDate(
                            ship.getDeparture()
                    );


            if (arrival != null
                    &&
                    (
                            earliest == null
                            ||
                            arrival.isBefore(
                                    earliest
                            )
                    )) {

                earliest =
                        arrival;
            }


            if (departure != null
                    &&
                    (
                            earliest == null
                            ||
                            departure.isBefore(
                                    earliest
                            )
                    )) {

                earliest =
                        departure;
            }
        }


        if (earliest == null) {

            return LocalDate.now();
        }


        return earliest;
    }


    // =========================================================
    // FIND LATEST SHIP DATE
    // =========================================================

    private LocalDate findLatestShipDate() {

        LocalDate latest =
                null;


        for (Ship ship : shipData) {

            if (ship == null) {

                continue;
            }


            LocalDate arrival =
                    parseDate(
                            ship.getArrival()
                    );


            LocalDate departure =
                    parseDate(
                            ship.getDeparture()
                    );


            if (arrival != null
                    &&
                    (
                            latest == null
                            ||
                            arrival.isAfter(
                                    latest
                            )
                    )) {

                latest =
                        arrival;
            }


            if (departure != null
                    &&
                    (
                            latest == null
                            ||
                            departure.isAfter(
                                    latest
                            )
                    )) {

                latest =
                        departure;
            }
        }


        if (latest == null) {

            return LocalDate.now();
        }


        return latest;
    }


    // =========================================================
    // DATE PARSER
    // =========================================================

    private LocalDate parseDate(
            String value
    ) {

        if (value == null
                ||
                value.trim().isEmpty()) {

            return null;
        }


        String original =
                value.trim();


        // =====================================================
        // 1. ISO FORMAT
        // =====================================================

        try {

            return LocalDate.parse(
                    original
            );

        } catch (DateTimeParseException ignored) {
        }


        // =====================================================
        // 2. STANDARD FULL DATE
        // =====================================================

        String[] standardFormats = {

                "dd MMM yyyy",

                "d MMM yyyy",

                "dd MMMM yyyy",

                "d MMMM yyyy",

                "dd/MM/yyyy",

                "d/M/yyyy",

                "dd-MM-yyyy",

                "d-M-yyyy"
        };


        for (String pattern :
                standardFormats) {

            try {

                return LocalDate.parse(
                        original,
                        DateTimeFormatter.ofPattern(
                                pattern,
                                Locale.ENGLISH
                        )
                );

            } catch (DateTimeParseException ignored) {
            }
        }


        // =====================================================
        // 3. EXTRACT DATE FROM STRING
        // =====================================================

        String cleaned =
                original
                        .replace(
                                "·",
                                " "
                        )
                        .replace(
                                ":",
                                " "
                        )
                        .replace(
                                ",",
                                " "
                        )
                        .replaceAll(
                                "\\s+",
                                " "
                        )
                        .trim();


        String[] tokens =
                cleaned.split(
                        " "
                );


        for (int i = 0;
                i < tokens.length;
                i++) {

            String token =
                    tokens[i];


            if (!isMonthName(
                    token
            )) {

                continue;
            }


            if (i == 0) {

                continue;
            }


            String dayToken =
                    tokens[i - 1];


            int day;


            try {

                day =
                        Integer.parseInt(
                                dayToken
                        );

            } catch (Exception e) {

                continue;
            }


            int year =
                    LocalDate.now()
                            .getYear();


            if (i + 1 < tokens.length) {

                try {

                    year =
                            Integer.parseInt(
                                    tokens[i + 1]
                            );

                } catch (Exception ignored) {
                }
            }


            try {

                return LocalDate.of(
                        year,
                        monthNumber(
                                token
                        ),
                        day
                );

            } catch (Exception ignored) {
            }
        }


        // =====================================================
        // 4. SEARCH ORIGINAL STRING
        // =====================================================

        String lower =
                original.toLowerCase(
                        Locale.ENGLISH
                );


        String[] months = {

                "jan",
                "feb",
                "mar",
                "apr",
                "may",
                "jun",
                "jul",
                "aug",
                "sep",
                "oct",
                "nov",
                "dec"
        };


        for (int monthIndex = 0;
                monthIndex < months.length;
                monthIndex++) {

            String month =
                    months[monthIndex];


            int monthPosition =
                    lower.indexOf(
                            month
                    );


            if (monthPosition < 0) {

                continue;
            }


            String before =
                    lower.substring(
                            0,
                            monthPosition
                    ).trim();


            StringBuilder digits =
                    new StringBuilder();


            for (int i = before.length() - 1;
                    i >= 0;
                    i--) {

                char c =
                        before.charAt(
                                i
                        );


                if (Character.isDigit(
                        c
                )) {

                    digits.insert(
                            0,
                            c
                    );

                } else if (
                        digits.length() > 0
                ) {

                    break;
                }
            }


            if (digits.length() == 0) {

                continue;
            }


            try {

                int day =
                        Integer.parseInt(
                                digits.toString()
                        );


                int year =
                        extractYearAfter(
                                lower,
                                monthPosition
                        );


                return LocalDate.of(
                        year,
                        monthIndex + 1,
                        day
                );

            } catch (Exception ignored) {
            }
        }


        return null;
    }


    // =========================================================
    // CHECK MONTH NAME
    // =========================================================

    private boolean isMonthName(
            String value
    ) {

        if (value == null) {

            return false;
        }


        String lower =
                value.toLowerCase(
                        Locale.ENGLISH
                );


        return lower.equals("jan")
                || lower.equals("january")

                || lower.equals("feb")
                || lower.equals("february")

                || lower.equals("mar")
                || lower.equals("march")

                || lower.equals("apr")
                || lower.equals("april")

                || lower.equals("may")

                || lower.equals("jun")
                || lower.equals("june")

                || lower.equals("jul")
                || lower.equals("july")

                || lower.equals("aug")
                || lower.equals("august")

                || lower.equals("sep")
                || lower.equals("september")

                || lower.equals("oct")
                || lower.equals("october")

                || lower.equals("nov")
                || lower.equals("november")

                || lower.equals("dec")
                || lower.equals("december");
    }


    // =========================================================
    // MONTH NUMBER
    // =========================================================

    private int monthNumber(
            String value
    ) {

        String lower =
                value.toLowerCase(
                        Locale.ENGLISH
                );


        switch (lower) {

            case "jan":
            case "january":
                return 1;

            case "feb":
            case "february":
                return 2;

            case "mar":
            case "march":
                return 3;

            case "apr":
            case "april":
                return 4;

            case "may":
                return 5;

            case "jun":
            case "june":
                return 6;

            case "jul":
            case "july":
                return 7;

            case "aug":
            case "august":
                return 8;

            case "sep":
            case "september":
                return 9;

            case "oct":
            case "october":
                return 10;

            case "nov":
            case "november":
                return 11;

            case "dec":
            case "december":
                return 12;

            default:
                return 1;
        }
    }


    // =========================================================
    // EXTRACT YEAR AFTER MONTH
    // =========================================================

    private int extractYearAfter(

            String value,

            int monthPosition

    ) {

        String after =
                value.substring(
                        monthPosition
                        +
                        3
                ).trim();


        StringBuilder digits =
                new StringBuilder();


        for (int i = 0;
                i < after.length();
                i++) {

            char c =
                    after.charAt(
                            i
                    );


            if (Character.isDigit(
                    c
            )) {

                digits.append(
                        c
                );

                if (digits.length() == 4) {

                    break;
                }

            } else if (
                    digits.length() > 0
            ) {

                break;
            }
        }


        if (digits.length() == 4) {

            try {

                return Integer.parseInt(
                        digits.toString()
                );

            } catch (Exception ignored) {
            }
        }


        return LocalDate.now()
                .getYear();
    }


    // =========================================================
    // FORMAT NUMBER
    // =========================================================

    private String formatNumber(
            double value
    ) {

        NumberFormat formatter =
                NumberFormat.getNumberInstance(
                        Locale.US
                );


        formatter.setMaximumFractionDigits(
                2
        );


        return formatter.format(
                value
        );
    }


    // =========================================================
    // FORMAT SHORT CURRENCY
    // =========================================================

    private String formatCurrencyShort(
            double amount
    ) {

        if (amount >= 10000000) {

            return String.format(
                    Locale.US,
                    "₹%.1fCr",
                    amount / 10000000
            );
        }


        if (amount >= 100000) {

            return String.format(
                    Locale.US,
                    "₹%.1fL",
                    amount / 100000
            );
        }


        if (amount >= 1000) {

            return String.format(
                    Locale.US,
                    "₹%.1fK",
                    amount / 1000
            );
        }


        return String.format(
                Locale.US,
                "₹%.0f",
                amount
        );
    }


    // =========================================================
    // SAFE VALUE
    // =========================================================

    private String safeValue(
            String value
    ) {

        if (value == null) {

            return "";
        }


        return value;
    }


    // =========================================================
    // WARNING MESSAGE
    // =========================================================

    private void showWarning(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );


        alert.setTitle(
                "Reports & Analytics"
        );


        alert.setHeaderText(
                null
        );


        alert.setContentText(
                message
        );


        alert.showAndWait();
    }


    // =========================================================
    // SUCCESS MESSAGE
    // =========================================================

    private void showSuccess(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );


        alert.setTitle(
                "Reports & Analytics"
        );


        alert.setHeaderText(
                null
        );


        alert.setContentText(
                message
        );


        alert.showAndWait();
    }
}