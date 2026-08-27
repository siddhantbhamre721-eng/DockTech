package com.projectui.view.portauthority;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.Node;

import java.text.NumberFormat;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Comparator;

import com.projectui.controller.portauthority.ShipController;
import com.projectui.controller.portauthority.CargoController;
import com.projectui.controller.portauthority.ContainerController;
import com.projectui.controller.portauthority.PaymentController;
import com.projectui.controller.portauthority.NotificationController;

import com.projectui.model.portauthority.Ship;
import com.projectui.model.portauthority.Cargo;
import com.projectui.model.portauthority.Container;
import com.projectui.model.portauthority.Payment;
import com.projectui.model.portauthority.Notification;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
import java.time.Duration;


// =========================================================
// PORT AUTHORITY DASHBOARD
// =========================================================

public class DashboardPage {

    private VBox root =
            new VBox();


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

        private NotificationController notificationController =
                new NotificationController();


    // =====================================================
    // DATA
    // =====================================================

    private List<Ship> shipData;

    private List<Cargo> cargoData;

    private List<Container> containerData;

    private List<Payment> paymentData;


    // =====================================================
    // DYNAMIC CARD VALUES
    // =====================================================

    private Label totalShipsValue;

    private Label totalCargoValue;

    private Label totalContainersValue;

    private Label containerUtilizationValue;

    private Label pendingPaymentsValue;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public DashboardPage() {

        root.setSpacing(
                18
        );


        // =====================================================
        // LOAD REAL DATA
        // =====================================================

        loadDashboardData();


        // =====================================================
        // DASHBOARD TITLE
        // =====================================================

        Label title =
                new Label(
                        "Dashboard"
                );


        title.setStyle(
                "-fx-text-fill:#111827;" +
                "-fx-font-size:22;" +
                "-fx-font-weight:bold;"
        );


        Label subtitle =
                new Label(
                        "Welcome back, Port Authority."
                );


        subtitle.setStyle(
                "-fx-text-fill:#8a93a1;" +
                "-fx-font-size:12;"
        );


        VBox heading =
                new VBox(
                        3,
                        title,
                        subtitle
                );


        // =====================================================
        // STATISTIC CARDS
        // =====================================================

        HBox cards =
                new HBox(
                        14
                );


        cards.setAlignment(
                Pos.CENTER_LEFT
        );


        // =====================================================
        // TOTAL SHIPS
        // =====================================================

        totalShipsValue =
                new Label(
                        "0"
                );


        VBox totalShips =
                createDynamicCard(
                        "S",
                        totalShipsValue,
                        "Total Ships"
                );


        // =====================================================
        // TOTAL CARGO
        // =====================================================

        totalCargoValue =
                new Label(
                        "0"
                );


        VBox totalCargo =
                createDynamicCard(
                        "C",
                        totalCargoValue,
                        "Total Cargo (Tons)"
                );


        // =====================================================
        // TOTAL CONTAINERS
        // =====================================================

        totalContainersValue =
                new Label(
                        "0"
                );


        VBox totalContainers =
                createDynamicCard(
                        "T",
                        totalContainersValue,
                        "Total Containers"
                );


        // =====================================================
        // CONTAINER UTILIZATION
        // =====================================================

        containerUtilizationValue =
                new Label(
                        "0%"
                );


        VBox containerUtilization =
                createDynamicCard(
                        "U",
                        containerUtilizationValue,
                        "Container Utilization"
                );


        // =====================================================
        // PENDING PAYMENTS
        // =====================================================

        pendingPaymentsValue =
                new Label(
                        "0"
                );


        VBox pendingPayments =
                createDynamicCard(
                        "P",
                        pendingPaymentsValue,
                        "Pending Payments"
                );


        cards.getChildren().addAll(
                totalShips,
                totalCargo,
                totalContainers,
                containerUtilization,
                pendingPayments
        );


        // =====================================================
        // REFRESH CARD VALUES
        // =====================================================

        refreshDashboardCards();


        // =====================================================
        // SHIP ACTIVITY CHART
        // =====================================================

        VBox shipActivity =
                createShipActivityChart();


        // =====================================================
        // CARGO OVERVIEW CHART
        // =====================================================

        VBox cargoOverview =
                createCargoOverviewChart();


        // =====================================================
        // CONTAINER STATUS CHART
        // =====================================================

        VBox containerStatus =
                createContainerStatusChart();


        // =====================================================
        // CHART ROW
        // =====================================================

        HBox chartRow =
                new HBox(
                        14
                );


        chartRow.setAlignment(
                Pos.CENTER_LEFT
        );


        HBox.setHgrow(
                shipActivity,
                Priority.ALWAYS
        );


        HBox.setHgrow(
                cargoOverview,
                Priority.ALWAYS
        );


        HBox.setHgrow(
                containerStatus,
                Priority.ALWAYS
        );


        chartRow.getChildren().addAll(
                shipActivity,
                cargoOverview,
                containerStatus
        );


        // =====================================================
        // RECENT SHIP ACTIVITY
        // =====================================================

        VBox recentShipActivity = createRecentShipActivity();


        // =====================================================
        // RECENT NOTIFICATIONS
        // =====================================================

        VBox recentNotifications =
                createRecentNotifications();


        // =====================================================
        // BOTTOM ROW
        // =====================================================

        HBox bottomRow =
                new HBox(
                        14
                );


        bottomRow.setAlignment(
                Pos.CENTER_LEFT
        );


        HBox.setHgrow(
                recentShipActivity,
                Priority.ALWAYS
        );


        HBox.setHgrow(
                recentNotifications,
                Priority.ALWAYS
        );


        bottomRow.getChildren().addAll(
                recentShipActivity,
                recentNotifications
        );


        // =====================================================
        // ADD EVERYTHING TO ROOT
        // =====================================================

        root.getChildren().addAll(
                heading,
                cards,
                chartRow,
                bottomRow
        );
    }


    // =========================================================
    // LOAD DASHBOARD DATA
    // =========================================================

    private void loadDashboardData() {

        try {

            // -------------------------------------------------
            // SHIPS
            // -------------------------------------------------

            shipData =
                    shipController.getShips();


            // -------------------------------------------------
            // CARGO
            // -------------------------------------------------

            cargoData =
                    cargoController.getCargos();


            // -------------------------------------------------
            // CONTAINERS
            // -------------------------------------------------

            containerData =
                    containerController.getContainers();


            // -------------------------------------------------
            // PAYMENTS
            // -------------------------------------------------

            paymentData =
                    paymentController.getPayments();


        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // REFRESH DASHBOARD CARDS
    // =========================================================

    private void refreshDashboardCards() {

        // -----------------------------------------------------
        // TOTAL SHIPS
        // -----------------------------------------------------

        int totalShips =
                shipData == null
                        ? 0
                        : shipData.size();


        totalShipsValue.setText(
                formatInteger(
                        totalShips
                )
        );


        // -----------------------------------------------------
        // TOTAL CARGO
        // -----------------------------------------------------

        double cargoWeight =
                calculateTotalCargoWeight();


        totalCargoValue.setText(
                formatNumber(
                        cargoWeight
                )
        );


        // -----------------------------------------------------
        // TOTAL CONTAINERS
        // -----------------------------------------------------

        int totalContainers =
                containerData == null
                        ? 0
                        : containerData.size();


        totalContainersValue.setText(
                formatInteger(
                        totalContainers
                )
        );


        // -----------------------------------------------------
        // CONTAINER UTILIZATION
        // -----------------------------------------------------

        double utilization =
                calculateContainerUtilization();


        containerUtilizationValue.setText(
                formatPercentage(
                        utilization
                )
        );


        // -----------------------------------------------------
        // PENDING PAYMENTS
        // -----------------------------------------------------

        int pendingPayments =
                calculatePendingPayments();


        pendingPaymentsValue.setText(
                formatInteger(
                        pendingPayments
                )
        );
    }


    // =========================================================
    // CALCULATE TOTAL CARGO WEIGHT
    // =========================================================

    private double calculateTotalCargoWeight() {

        if (cargoData == null) {

            return 0;
        }


        double total =
                0;


        for (Cargo cargo : cargoData) {

            if (cargo == null) {

                continue;
            }


            String weight =
                    cargo.getWeight();


            if (
                    weight == null
                    ||
                    weight.trim().isEmpty()
            ) {

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
    // CALCULATE CONTAINER UTILIZATION
    // =========================================================

    private double calculateContainerUtilization() {

        if (
                containerData == null
                ||
                containerData.isEmpty()
        ) {

            return 0;
        }


        int allocatedContainers =
                0;


        int totalContainers =
                containerData.size();


        for (Container container :
                containerData) {

            if (container == null) {

                continue;
            }


            String status =
                    safeValue(
                            container.getStatus()
                    );


            if (
                    status.equalsIgnoreCase(
                            "Allocated"
                    )
            ) {

                allocatedContainers++;
            }
        }


        return (
                (double) allocatedContainers
                /
                totalContainers
        ) * 100;
    }


    // =========================================================
    // CALCULATE PENDING PAYMENTS
    // =========================================================

    private int calculatePendingPayments() {

        if (paymentData == null) {

            return 0;
        }


        int count =
                0;


        for (Payment payment :
                paymentData) {

            if (payment == null) {

                continue;
            }


            String status =
                    safeValue(
                            payment.getStatus()
                    );


            if (
                    status.equalsIgnoreCase(
                            "Pending"
                    )
            ) {

                count++;
            }
        }


        return count;
    }


    // =========================================================
    // CREATE DYNAMIC CARD
    // =========================================================

    private VBox createDynamicCard(

            String icon,

            Label valueLabel,

            String text

    ) {

        VBox card =
                new VBox(
                        8
                );


        card.setPrefWidth(
                180
        );


        card.setPrefHeight(
                105
        );


        card.setPadding(
                new Insets(
                        14
                )
        );


        card.setAlignment(
                Pos.CENTER_LEFT
        );


        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:12;" +
                "-fx-border-color:#e5ebf3;" +
                "-fx-border-radius:12;"
        );


        Label iconLabel =
                new Label(
                        icon
                );


        iconLabel.setPrefSize(
                30,
                30
        );


        iconLabel.setAlignment(
                Pos.CENTER
        );


        iconLabel.setStyle(
                "-fx-background-color:#eaf2ff;" +
                "-fx-background-radius:8;" +
                "-fx-text-fill:#0f5fff;" +
                "-fx-font-size:14;" +
                "-fx-font-weight:bold;"
        );


        valueLabel.setStyle(
                "-fx-text-fill:#111827;" +
                "-fx-font-size:22;" +
                "-fx-font-weight:bold;"
        );


        Label textLabel =
                new Label(
                        text
                );


        textLabel.setStyle(
                "-fx-text-fill:#8a93a1;" +
                "-fx-font-size:10;"
        );


        card.getChildren().addAll(
                iconLabel,
                valueLabel,
                textLabel
        );


        return card;
    }


        // =========================================================
        // SHIP ACTIVITY CHART
        // =========================================================

        private VBox createShipActivityChart() {

                VBox chartBox =
                        new VBox(
                                10
                        );


                chartBox.setMaxWidth(
                        Double.MAX_VALUE
                );


                chartBox.setPrefWidth(
                        450
                );


                chartBox.setPadding(
                        new Insets(
                                18
                        )
                );


                chartBox.setPrefHeight(
                        280
                );


                chartBox.setStyle(
                        "-fx-background-color:white;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:#e5ebf3;" +
                        "-fx-border-radius:12;"
                );


                // =====================================================
                // TITLE
                // =====================================================

                Label title =
                        new Label(
                                "Ship Activity"
                        );


                title.setStyle(
                        "-fx-text-fill:#111827;" +
                        "-fx-font-size:14;" +
                        "-fx-font-weight:bold;"
                );


                Label subtitle =
                        new Label(
                                "Last 7 days"
                        );


                subtitle.setStyle(
                        "-fx-text-fill:#8a93a1;" +
                        "-fx-font-size:10;"
                );


                // =====================================================
                // AXES
                // =====================================================

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


                // =====================================================
                // LINE CHART
                // =====================================================

                LineChart<String, Number> chart =
                        new LineChart<>(
                                xAxis,
                                yAxis
                        );


                chart.setLegendVisible(
                        false
                );


                chart.setCreateSymbols(
                        true
                );


                chart.setAnimated(
                        false
                );


                chart.setPrefHeight(
                        190
                );


                // =====================================================
                // SERIES
                // =====================================================

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


                // =====================================================
                // LAST 7 DAYS
                // =====================================================

                LocalDate today =
                        LocalDate.now();


                for (int i = 6; i >= 0; i--) {

                        LocalDate date =
                                today.minusDays(
                                        i
                                );


                        String dateLabel =
                                date.format(
                                        DateTimeFormatter.ofPattern(
                                                "dd MMM"
                                        )
                                );


                        // -------------------------------------------------
                        // ARRIVALS
                        // -------------------------------------------------

                        int arrivalCount =
                                calculateShipActivityForDate(
                                        date,
                                        "Arrivals"
                                );


                        // -------------------------------------------------
                        // DEPARTURES
                        // -------------------------------------------------

                        int departureCount =
                                calculateShipActivityForDate(
                                        date,
                                        "Departures"
                                );


                        // -------------------------------------------------
                        // DOCKED
                        // -------------------------------------------------

                        int dockedCount =
                                calculateDockedShipsForDate(
                                        date
                                );


                        arrivals.getData().add(
                                new XYChart.Data<>(
                                        dateLabel,
                                        arrivalCount
                                )
                        );


                        departures.getData().add(
                                new XYChart.Data<>(
                                        dateLabel,
                                        departureCount
                                )
                        );


                        docked.getData().add(
                                new XYChart.Data<>(
                                        dateLabel,
                                        dockedCount
                                )
                        );
                }


                // =====================================================
                // ADD SERIES
                // =====================================================

                chart.getData().addAll(
                        arrivals,
                        departures,
                        docked
                );

                // =====================================================
                // SET SHIP ACTIVITY SERIES COLORS
                // =====================================================

                Platform.runLater(() -> {

                // Force JavaFX to create the chart nodes
                chart.applyCss();
                chart.layout();


                // =================================================
                // ARRIVALS = GREEN
                // =================================================

                if (arrivals.getNode() != null) {

                        arrivals.getNode().setStyle(
                                "-fx-stroke: #4CAF50;" +
                                "-fx-stroke-width: 2.5px;"
                        );
                }


                // =================================================
                // DEPARTURES = ORANGE
                // =================================================

                if (departures.getNode() != null) {

                        departures.getNode().setStyle(
                                "-fx-stroke: #FFA726;" +
                                "-fx-stroke-width: 2.5px;"
                        );
                }


                // =================================================
                // DOCKED = RED
                // =================================================

                if (docked.getNode() != null) {

                        docked.getNode().setStyle(
                                "-fx-stroke: #F44336;" +
                                "-fx-stroke-width: 2.5px;"
                        );
                }
                });

                // =====================================================
                // CUSTOM SHIP ACTIVITY LEGEND
                // =====================================================

                HBox legend = createShipActivityLegend();


                


                // =====================================================
                // ADD TO BOX
                // =====================================================

                chartBox.getChildren().addAll(
                        title,
                        subtitle,
                        chart,
                        legend
                );


                return chartBox;
        }

        // =========================================================
        // SHIP ACTIVITY LEGEND
        // =========================================================

        private HBox createShipActivityLegend() {

                HBox legend =
                        new HBox(
                                18
                        );


                legend.setAlignment(
                        Pos.CENTER
                );


                // =====================================================
                // ARRIVALS - GREEN
                // =====================================================

                HBox arrivalsItem =
                        createLegendItem(
                                "Arrivals",
                                "#4CAF50"
                        );


                // =====================================================
                // DEPARTURES - ORANGE
                // =====================================================

                HBox departuresItem =
                        createLegendItem(
                                "Departures",
                                "#FFA726"
                        );


                // =====================================================
                // DOCKED - RED
                // =====================================================

                HBox dockedItem =
                        createLegendItem(
                                "Docked",
                                "#F44336"
                        );


                legend.getChildren().addAll(
                        arrivalsItem,
                        departuresItem,
                        dockedItem
                );


                legend.setStyle(
                        "-fx-background-color:#f4f4f4;" +
                        "-fx-background-radius:4;" +
                        "-fx-border-color:#dddddd;" +
                        "-fx-border-radius:4;" +
                        "-fx-padding:7 10 7 10;"
                );


                return legend;
        }

        // =========================================================
        // CREATE LEGEND ITEM
        // =========================================================

        private HBox createLegendItem(

                        String text,

                        String color

                ) {

                HBox item =
                        new HBox(
                                5
                        );


                item.setAlignment(
                        Pos.CENTER_LEFT
                );


                // =====================================================
                // COLOR CIRCLE
                // =====================================================

                Circle circle =
                        new Circle(
                                4
                        );


                circle.setFill(
                        Color.web(
                                color
                        )
                );


                // =====================================================
                // LABEL
                // =====================================================

                Label label =
                        new Label(
                                text
                        );


                label.setStyle(
                        "-fx-text-fill:#4b5563;" +
                        "-fx-font-size:10;"
                );


                item.getChildren().addAll(
                        circle,
                        label
                );


                return item;
        }

        // =========================================================
        // CALCULATE SHIP ACTIVITY FOR DATE
        // =========================================================

        private int calculateShipActivityForDate(

                        LocalDate targetDate,

                        String activity

                ) {

                if (shipData == null) {

                        return 0;
                }


                int count =
                        0;


                for (Ship ship :
                        shipData) {

                        if (ship == null) {

                        continue;
                        }


                        String dateString;


                        // -------------------------------------------------
                        // ARRIVAL
                        // -------------------------------------------------

                        if (
                                "Arrivals".equals(
                                        activity
                                )
                        ) {

                        dateString =
                                ship.getArrival();
                        }


                        // -------------------------------------------------
                        // DEPARTURE
                        // -------------------------------------------------

                        else {

                        dateString =
                                ship.getDeparture();
                        }


                        LocalDate shipDate =
                                parseShipDate(
                                        dateString
                                );


                        if (shipDate == null) {

                        continue;
                        }


                        if (
                                shipDate.equals(
                                        targetDate
                                )
                        ) {

                        count++;
                        }
                }


                return count;
        }

        // =========================================================
        // CALCULATE DOCKED SHIPS FOR DATE
        // =========================================================

        private int calculateDockedShipsForDate(

                        LocalDate targetDate

                ) {

                if (shipData == null) {

                        return 0;
                }


                int count =
                        0;


                for (Ship ship :
                        shipData) {

                        if (ship == null) {

                        continue;
                        }


                        LocalDate arrivalDate =
                                parseShipDate(
                                        ship.getArrival()
                                );


                        if (arrivalDate == null) {

                        continue;
                        }


                        // -------------------------------------------------
                        // SHIP HAS NOT ARRIVED YET
                        // -------------------------------------------------

                        if (
                                arrivalDate.isAfter(
                                        targetDate
                                )
                        ) {

                        continue;
                        }


                        // -------------------------------------------------
                        // CHECK DEPARTURE
                        // -------------------------------------------------

                        LocalDate departureDate =
                                parseShipDate(
                                        ship.getDeparture()
                                );


                        // -------------------------------------------------
                        // NO DEPARTURE
                        // -------------------------------------------------
                        //
                        // If the ship has arrived and has no departure
                        // date, consider it docked.
                        //

                        if (departureDate == null) {

                        count++;

                        continue;
                        }


                        // -------------------------------------------------
                        // SHIP IS STILL DOCKED
                        // -------------------------------------------------

                        if (
                                departureDate.isAfter(
                                        targetDate
                                )
                        ) {

                        count++;
                        }
                }


                return count;
        }

        // =========================================================
        // PARSE SHIP DATE
        // =========================================================

        private LocalDate parseShipDate(String dateString) {

                if (
                        dateString == null
                        ||
                        dateString.trim().isEmpty()
                        ||
                        dateString.trim().equals("—")
                ) {

                        return null;
                }


                String value =
                        dateString.trim();


                // =====================================================
                // FIND DAY + MONTH + OPTIONAL YEAR
                // =====================================================

                Pattern pattern =
                        Pattern.compile(
                                "(\\d{1,2})\\s+([A-Za-z]{3,9})(?:\\s+(\\d{4}))?"
                        );


                Matcher matcher =
                        pattern.matcher(
                                value
                        );


                if (!matcher.find()) {

                        return null;
                }


                try {

                        int day =
                                Integer.parseInt(
                                        matcher.group(1)
                                );


                        String monthText =
                                matcher.group(2);


                        int year;


                        // -------------------------------------------------
                        // YEAR PROVIDED
                        // -------------------------------------------------

                        if (
                                matcher.group(3) != null
                        ) {

                        year =
                                Integer.parseInt(
                                        matcher.group(3)
                                );
                        }


                        // -------------------------------------------------
                        // YEAR NOT PROVIDED
                        // -------------------------------------------------
                        //
                        // Ship Management allows:
                        // "08:20 · 15 Aug"
                        //
                        // In that case, use the current year.
                        //

                        else {

                        year =
                                LocalDate.now()
                                        .getYear();
                        }


                        // -------------------------------------------------
                        // NORMALIZE MONTH
                        // -------------------------------------------------

                        String normalizedMonth =
                                monthText.substring(
                                        0,
                                        1
                                ).toUpperCase(
                                        Locale.ENGLISH
                                )
                                +
                                monthText.substring(
                                        1
                                ).toLowerCase(
                                        Locale.ENGLISH
                                );


                        DateTimeFormatter formatter =
                                DateTimeFormatter.ofPattern(
                                        "d MMM yyyy",
                                        Locale.ENGLISH
                                );


                        return LocalDate.parse(
                                day
                                +
                                " "
                                +
                                normalizedMonth
                                +
                                " "
                                +
                                year,
                                formatter
                        );


                } catch (
                        NumberFormatException
                        |
                        DateTimeParseException e
                ) {

                        return null;
                }
        }


        // =========================================================
        // CARGO OVERVIEW CHART
        // =========================================================

        private VBox createCargoOverviewChart() {

                VBox chartBox =
                        new VBox(
                                10
                        );


                chartBox.setMaxWidth(
                        Double.MAX_VALUE
                );


                chartBox.setPrefWidth(
                        450
                );


                chartBox.setPadding(
                        new Insets(
                                18
                        )
                );


                chartBox.setPrefHeight(
                        280
                );


                chartBox.setStyle(
                        "-fx-background-color:white;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:#e5ebf3;" +
                        "-fx-border-radius:12;"
                );


                // =====================================================
                // TITLE
                // =====================================================

                Label title =
                        new Label(
                                "Cargo Overview"
                        );


                title.setStyle(
                        "-fx-text-fill:#111827;" +
                        "-fx-font-size:14;" +
                        "-fx-font-weight:bold;"
                );


                Label subtitle =
                        new Label(
                                "Cargo movement overview"
                        );


                subtitle.setStyle(
                        "-fx-text-fill:#8a93a1;" +
                        "-fx-font-size:10;"
                );


                // =====================================================
                // AXES
                // =====================================================

                CategoryAxis xAxis =
                        new CategoryAxis();


                NumberAxis yAxis =
                        new NumberAxis();


                xAxis.setLabel(
                        "Status"
                );


                yAxis.setLabel(
                        "Cargo (Tons)"
                );


                // =====================================================
                // BAR CHART
                // =====================================================

                BarChart<String, Number> chart =
                        new BarChart<>(
                                xAxis,
                                yAxis
                        );


                chart.setLegendVisible(
                        false
                );


                chart.setAnimated(
                        false
                );


                chart.setPrefHeight(
                        190
                );


                // =====================================================
                // CARGO TOTALS
                // =====================================================

                double received =
                        0;


                double pending =
                        0;


                double loaded =
                        0;


                double inTransit =
                        0;


                double delivered =
                        0;


                // =====================================================
                // CALCULATE CARGO BY STATUS
                // =====================================================

                if (cargoData != null) {

                        for (Cargo cargo :
                                cargoData) {

                        if (cargo == null) {

                                continue;
                        }


                        double weight =
                                parseCargoWeight(
                                        cargo.getWeight()
                                );


                        String status =
                                safeValue(
                                        cargo.getStatus()
                                );


                        // -------------------------------------------------
                        // RECEIVED
                        // -------------------------------------------------

                        if (
                                status.equalsIgnoreCase(
                                        "Received"
                                )
                        ) {

                                received +=
                                        weight;
                        }


                        // -------------------------------------------------
                        // PENDING
                        // -------------------------------------------------
                        //
                        // Both Pending and Pending Inspection
                        // are displayed under Pending.
                        //

                        else if (
                                status.equalsIgnoreCase(
                                        "Pending"
                                )
                                ||
                                status.equalsIgnoreCase(
                                        "Pending Inspection"
                                )
                        ) {

                                pending +=
                                        weight;
                        }


                        // -------------------------------------------------
                        // LOADED
                        // -------------------------------------------------

                        else if (
                                status.equalsIgnoreCase(
                                        "Loaded"
                                )
                        ) {

                                loaded +=
                                        weight;
                        }


                        // -------------------------------------------------
                        // IN TRANSIT
                        // -------------------------------------------------

                        else if (
                                status.equalsIgnoreCase(
                                        "In Transit"
                                )
                        ) {

                                inTransit +=
                                        weight;
                        }


                        // -------------------------------------------------
                        // DELIVERED
                        // -------------------------------------------------

                        else if (
                                status.equalsIgnoreCase(
                                        "Delivered"
                                )
                        ) {

                                delivered +=
                                        weight;
                        }
                        }
                }


                // =====================================================
                // CHART SERIES
                // =====================================================

                XYChart.Series<String, Number> cargo =
                        new XYChart.Series<>();


                cargo.setName(
                        "Cargo"
                );


                // -----------------------------------------------------
                // RECEIVED
                // -----------------------------------------------------

                cargo.getData().add(
                        new XYChart.Data<>(
                                "Received",
                                received
                        )
                );


                // -----------------------------------------------------
                // PENDING
                // -----------------------------------------------------

                cargo.getData().add(
                        new XYChart.Data<>(
                                "Pending",
                                pending
                        )
                );


                // -----------------------------------------------------
                // LOADED
                // -----------------------------------------------------

                cargo.getData().add(
                        new XYChart.Data<>(
                                "Loaded",
                                loaded
                        )
                );


                // -----------------------------------------------------
                // IN TRANSIT
                // -----------------------------------------------------

                cargo.getData().add(
                        new XYChart.Data<>(
                                "In Transit",
                                inTransit
                        )
                );


                // -----------------------------------------------------
                // DELIVERED
                // -----------------------------------------------------

                cargo.getData().add(
                        new XYChart.Data<>(
                                "Delivered",
                                delivered
                        )
                );


                chart.getData().add(
                        cargo
                );


                // =====================================================
                // ADD TO CHART BOX
                // =====================================================

                chartBox.getChildren().addAll(
                        title,
                        subtitle,
                        chart
                );


                return chartBox;
        }


        // =========================================================
        // PARSE CARGO WEIGHT
        // =========================================================

        private double parseCargoWeight(
                        String weight
                ) {

                if (
                        weight == null
                        ||
                        weight.trim().isEmpty()
                ) {

                        return 0;
                }


                try {

                        String numeric =
                                weight.replaceAll(
                                        "[^0-9.\\-]",
                                        ""
                                );


                        if (numeric.isEmpty()) {

                        return 0;
                        }


                        return Double.parseDouble(
                                numeric
                        );


                } catch (Exception e) {

                        return 0;
                }
        }


        // =========================================================
        // CONTAINER STATUS CHART
        // =========================================================


        private VBox createContainerStatusChart() {

                VBox chartBox =
                        new VBox(
                                10
                        );


                chartBox.setMaxWidth(
                        Double.MAX_VALUE
                );


                chartBox.setPrefWidth(
                        380
                );


                chartBox.setPadding(
                        new Insets(
                                18
                        )
                );


                chartBox.setPrefHeight(
                        280
                );


                chartBox.setStyle(
                        "-fx-background-color:white;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:#e5ebf3;" +
                        "-fx-border-radius:12;"
                );


                // =====================================================
                // TITLE
                // =====================================================

                Label title =
                        new Label(
                                "Container Status"
                        );


                title.setStyle(
                        "-fx-text-fill:#111827;" +
                        "-fx-font-size:14;" +
                        "-fx-font-weight:bold;"
                );


                Label subtitle =
                        new Label(
                                "Current container distribution"
                        );


                subtitle.setStyle(
                        "-fx-text-fill:#8a93a1;" +
                        "-fx-font-size:10;"
                );


                // =====================================================
                // STATUS COUNTERS
                // =====================================================

                int allocated =
                        0;


                int available =
                        0;


                int inTransit =
                        0;


                int pending =
                        0;


                // =====================================================
                // CALCULATE STATUS COUNTS
                // =====================================================

                if (containerData != null) {

                        for (Container container :
                                containerData) {

                        if (container == null) {

                                continue;
                        }


                        String status =
                                safeValue(
                                        container.getStatus()
                                );


                        // -------------------------------------------------
                        // ALLOCATED
                        // -------------------------------------------------

                        if (
                                status.equalsIgnoreCase(
                                        "Allocated"
                                )
                        ) {

                                allocated++;
                        }


                        // -------------------------------------------------
                        // AVAILABLE
                        // -------------------------------------------------

                        else if (
                                status.equalsIgnoreCase(
                                        "Available"
                                )
                        ) {

                                available++;
                        }


                        // -------------------------------------------------
                        // IN TRANSIT
                        // -------------------------------------------------

                        else if (
                                status.equalsIgnoreCase(
                                        "In Transit"
                                )
                        ) {

                                inTransit++;
                        }


                        // -------------------------------------------------
                        // PENDING
                        // -------------------------------------------------

                        else if (
                                status.equalsIgnoreCase(
                                        "Pending"
                                )
                        ) {

                                pending++;
                        }
                        }
                }


                // =====================================================
                // TOTAL CONTAINERS
                // =====================================================

                int total =
                        allocated
                        +
                        available
                        +
                        inTransit
                        +
                        pending;


                // =====================================================
                // PIE CHART
                // =====================================================

                PieChart chart =
                        new PieChart();


                chart.setPrefSize(
                        350,
                        200
                );


                chart.setMinSize(
                        350,
                        200
                );


                chart.setMaxSize(
                        Double.MAX_VALUE,
                        210
                );


                // -----------------------------------------------------
                // IMPORTANT
                // -----------------------------------------------------
                //
                // Labels are displayed around their respective
                // pie sections with JavaFX leader lines.
                //

                chart.setLabelsVisible(
                        true
                );


                chart.setLegendVisible(
                        false
                );


                chart.setStartAngle(
                        90
                );


                chart.setClockwise(
                        true
                );


                chart.setAnimated(
                        false
                );


                // =====================================================
                // CALCULATE PERCENTAGES
                // =====================================================

                double allocatedPercentage =
                        calculatePercentage(
                                allocated,
                                total
                        );


                double availablePercentage =
                        calculatePercentage(
                                available,
                                total
                        );


                double inTransitPercentage =
                        calculatePercentage(
                                inTransit,
                                total
                        );


                double pendingPercentage =
                        calculatePercentage(
                                pending,
                                total
                        );


                // =====================================================
                // PIE DATA
                // =====================================================

                PieChart.Data allocatedData =
                        new PieChart.Data(
                                createPieLabel(
                                        "Allocated",
                                        allocated,
                                        allocatedPercentage
                                ),
                                allocated
                        );


                PieChart.Data availableData =
                        new PieChart.Data(
                                createPieLabel(
                                        "Available",
                                        available,
                                        availablePercentage
                                ),
                                available
                        );


                PieChart.Data inTransitData =
                        new PieChart.Data(
                                createPieLabel(
                                        "In Transit",
                                        inTransit,
                                        inTransitPercentage
                                ),
                                inTransit
                        );


                PieChart.Data pendingData =
                        new PieChart.Data(
                                createPieLabel(
                                        "Pending",
                                        pending,
                                        pendingPercentage
                                ),
                                pending
                        );


                chart.getData().addAll(

                        allocatedData,

                        availableData,

                        inTransitData,

                        pendingData
                );


                // =====================================================
                // ADD TO CHART BOX
                // =====================================================

                chartBox.getChildren().addAll(
                        title,
                        subtitle,
                        chart
                );


                return chartBox;
        }

        // =========================================================
        // CREATE PIE LABEL
        // =========================================================

        private String createPieLabel(

                        String status,

                        int value,

                        double percentage

                ) {

                return status
                        +
                        "\n"
                        +
                        value
                        +
                        " ("
                        +
                        formatPercentage(
                                percentage
                        )
                        +
                        ")";
        }

        // =========================================================
        // CALCULATE PERCENTAGE
        // =========================================================

        private double calculatePercentage(

                        int value,

                        int total

                ) {

                if (total <= 0) {

                        return 0;
                }


                return (
                        (double) value
                        /
                        total
                ) * 100;
        }


        // =========================================================
        // RECENT SHIP ACTIVITY
        // =========================================================

        private VBox createRecentShipActivity() {

                VBox box =
                        new VBox(
                                10
                        );


                box.setPadding(
                        new Insets(
                                18
                        )
                );


                box.setStyle(
                        "-fx-background-color:white;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:#e5ebf3;" +
                        "-fx-border-radius:12;"
                );


                // =====================================================
                // TITLE
                // =====================================================

                Label title =
                        new Label(
                                "Recent Ship Activity"
                        );


                title.setStyle(
                        "-fx-text-fill:#111827;" +
                        "-fx-font-size:14;" +
                        "-fx-font-weight:bold;"
                );


                // =====================================================
                // TABLE
                // =====================================================

                TableView<Ship> table =
                        new TableView<>();


                table.setPrefHeight(
                        150
                );


                table.setColumnResizePolicy(
                        TableView.CONSTRAINED_RESIZE_POLICY
                );


                // =====================================================
                // SHIP COLUMN
                // =====================================================

                TableColumn<Ship, String> shipColumn =
                        new TableColumn<>(
                                "Ship"
                        );


                shipColumn.setCellValueFactory(
                        data ->
                                new javafx.beans.property.SimpleStringProperty(
                                        safeValue(
                                                data.getValue()
                                                        .getShipName()
                                        )
                                )
                );


                // =====================================================
                // STATUS COLUMN
                // =====================================================

                TableColumn<Ship, String> statusColumn =
                        new TableColumn<>(
                                "Status"
                        );


                statusColumn.setCellValueFactory(
                        data ->
                                new javafx.beans.property.SimpleStringProperty(
                                        safeValue(
                                                data.getValue()
                                                        .getStatus()
                                        )
                                )
                );


                // =====================================================
                // ARRIVAL COLUMN
                // =====================================================

                TableColumn<Ship, String> arrivalColumn =
                        new TableColumn<>(
                                "Arrival"
                        );


                arrivalColumn.setCellValueFactory(
                        data ->
                                new javafx.beans.property.SimpleStringProperty(
                                        safeValue(
                                                data.getValue()
                                                        .getArrival()
                                        )
                                )
                );


                // =====================================================
                // BERTH COLUMN
                // =====================================================

                TableColumn<Ship, String> berthColumn =
                        new TableColumn<>(
                                "Berth"
                        );


                berthColumn.setCellValueFactory(
                        data ->
                                new javafx.beans.property.SimpleStringProperty(
                                        safeValue(
                                                data.getValue()
                                                        .getBerth()
                                        )
                                )
                );


                table.getColumns().addAll(
                        shipColumn,
                        statusColumn,
                        arrivalColumn,
                        berthColumn
                );

                // =====================================================
                // VISUAL STYLE - SHIP COLUMN
                // =====================================================

                shipColumn.setCellFactory(
                        column -> new TableCell<Ship, String>() {

                        private final Label shipLabel =
                                new Label();

                        {
                                shipLabel.setStyle(
                                        "-fx-text-fill:#111827;" +
                                        "-fx-font-size:10;" +
                                        "-fx-font-weight:bold;"
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


                                if (
                                        empty
                                        ||
                                        item == null
                                ) {

                                setGraphic(null);
                                setText(null);

                                return;
                                }


                                shipLabel.setText(
                                        "⚓  " + item
                                );


                                setGraphic(
                                        shipLabel
                                );


                                setText(null);


                                setStyle(
                                        "-fx-background-color:white;" +
                                        "-fx-border-color:transparent transparent #edf0f4 transparent;" +
                                        "-fx-padding:0 8 0 8;"
                                );
                        }
                        }
                );

                // =====================================================
                // VISUAL STYLE - STATUS COLUMN
                // =====================================================

                statusColumn.setCellFactory(
                        column -> new TableCell<Ship, String>() {

                        private final Label statusLabel =
                                new Label();

                        {
                                statusLabel.setPadding(
                                        new Insets(
                                                3,
                                                8,
                                                3,
                                                8
                                        )
                                );

                                statusLabel.setStyle(
                                        "-fx-font-size:9;" +
                                        "-fx-font-weight:bold;" +
                                        "-fx-background-radius:10;"
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


                                if (
                                        empty
                                        ||
                                        item == null
                                ) {

                                setGraphic(null);
                                setText(null);

                                return;
                                }


                                String status =
                                        item.trim();


                                statusLabel.setText(
                                        status
                                );


                                // =================================================
                                // DOCKED
                                // =================================================

                                if (
                                        status.equalsIgnoreCase(
                                                "Docked"
                                        )
                                ) {

                                statusLabel.setStyle(
                                        "-fx-background-color:#dcfce7;" +
                                        "-fx-text-fill:#15803d;" +
                                        "-fx-font-size:9;" +
                                        "-fx-font-weight:bold;" +
                                        "-fx-background-radius:10;"
                                );
                                }


                                // =================================================
                                // ARRIVING
                                // =================================================

                                else if (
                                        status.equalsIgnoreCase(
                                                "Arriving"
                                        )
                                ) {

                                statusLabel.setStyle(
                                        "-fx-background-color:#dbeafe;" +
                                        "-fx-text-fill:#2563eb;" +
                                        "-fx-font-size:9;" +
                                        "-fx-font-weight:bold;" +
                                        "-fx-background-radius:10;"
                                );
                                }


                                // =================================================
                                // DEPARTING
                                // =================================================

                                else if (
                                        status.equalsIgnoreCase(
                                                "Departing"
                                        )
                                ) {

                                statusLabel.setStyle(
                                        "-fx-background-color:#ffedd5;" +
                                        "-fx-text-fill:#ea580c;" +
                                        "-fx-font-size:9;" +
                                        "-fx-font-weight:bold;" +
                                        "-fx-background-radius:10;"
                                );
                                }


                                // =================================================
                                // DEFAULT
                                // =================================================

                                else {

                                statusLabel.setStyle(
                                        "-fx-background-color:#f1f5f9;" +
                                        "-fx-text-fill:#475569;" +
                                        "-fx-font-size:9;" +
                                        "-fx-font-weight:bold;" +
                                        "-fx-background-radius:10;"
                                );
                                }


                                setGraphic(
                                        statusLabel
                                );


                                setText(null);


                                setAlignment(
                                        Pos.CENTER
                                );


                                setStyle(
                                        "-fx-background-color:white;" +
                                        "-fx-border-color:transparent transparent #edf0f4 transparent;"
                                );
                        }
                        }
                );

                // =====================================================
                // VISUAL STYLE - ARRIVAL COLUMN
                // =====================================================

                arrivalColumn.setCellFactory(
                        column -> new TableCell<Ship, String>() {

                        @Override
                        protected void updateItem(
                                String item,
                                boolean empty
                        ) {

                                super.updateItem(
                                        item,
                                        empty
                                );


                                if (
                                        empty
                                        ||
                                        item == null
                                ) {

                                setText(null);

                                return;
                                }


                                setText(
                                        item
                                );


                                setStyle(
                                        "-fx-text-fill:#475569;" +
                                        "-fx-font-size:10;" +
                                        "-fx-background-color:white;" +
                                        "-fx-border-color:transparent transparent #edf0f4 transparent;" +
                                        "-fx-padding:0 8 0 8;"
                                );
                        }
                        }
                );


                // =====================================================
                // VISUAL STYLE - BERTH COLUMN
                // =====================================================

                berthColumn.setCellFactory(
                        column -> new TableCell<Ship, String>() {

                        @Override
                        protected void updateItem(
                                String item,
                                boolean empty
                        ) {

                                super.updateItem(
                                        item,
                                        empty
                                );


                                if (
                                        empty
                                        ||
                                        item == null
                                ) {

                                setText(null);

                                return;
                                }


                                setText(
                                        item
                                );


                                setStyle(
                                        "-fx-text-fill:#475569;" +
                                        "-fx-font-size:10;" +
                                        "-fx-background-color:white;" +
                                        "-fx-border-color:transparent transparent #edf0f4 transparent;" +
                                        "-fx-padding:0 8 0 8;"
                                );
                        }
                        }
                );




                // =====================================================
                // GET RECENT SHIPS
                // =====================================================

                List<Ship> recentShips =
                        new ArrayList<>();


                if (shipData != null) {

                        for (Ship ship :
                                shipData) {

                        if (ship != null) {

                                recentShips.add(
                                        ship
                                );
                        }
                        }
                }


                // =====================================================
                // SORT BY ARRIVAL DATE
                // NEWEST FIRST
                // =====================================================

                recentShips.sort(
                        new Comparator<Ship>() {

                                @Override
                                public int compare(
                                        Ship ship1,
                                        Ship ship2
                                ) {

                                LocalDate date1 =
                                        parseShipDate(
                                                ship1.getArrival()
                                        );


                                LocalDate date2 =
                                        parseShipDate(
                                                ship2.getArrival()
                                        );


                                // -----------------------------------------
                                // BOTH DATES INVALID
                                // -----------------------------------------

                                if (
                                        date1 == null
                                        &&
                                        date2 == null
                                ) {

                                        return 0;
                                }


                                // -----------------------------------------
                                // SHIP 1 INVALID
                                // Put it after ship 2
                                // -----------------------------------------

                                if (date1 == null) {

                                        return 1;
                                }


                                // -----------------------------------------
                                // SHIP 2 INVALID
                                // Put it after ship 1
                                // -----------------------------------------

                                if (date2 == null) {

                                        return -1;
                                }


                                // -----------------------------------------
                                // NEWEST FIRST
                                // -----------------------------------------

                                return date2.compareTo(
                                        date1
                                );
                                }
                        }
                );


                // =====================================================
                // SHOW ONLY 4 MOST RECENT SHIPS
                // =====================================================

                int numberOfShips =
                        Math.min(
                                4,
                                recentShips.size()
                        );


                for (
                        int i = 0;
                        i < numberOfShips;
                        i++
                ) {

                        table.getItems().add(
                                recentShips.get(
                                        i
                                )
                        );
                }


                // =====================================================
                // TABLE STYLE
                // =====================================================

                table.setStyle(
                        "-fx-background-color:transparent;" +
                        "-fx-border-color:transparent;" +
                        "-fx-font-size:10;"
                );


                table.setFixedCellSize(
                        34
                );


                table.setPrefHeight(
                        170
                );


                table.setMinHeight(
                        170
                );


                table.setMaxHeight(
                        170
                );

                // =====================================================
                // TABLE ROW STYLE
                // =====================================================

                table.setRowFactory(
                        tv -> {

                        javafx.scene.control.TableRow<Ship> row =
                                new javafx.scene.control.TableRow<>();


                        row.setStyle(
                                "-fx-background-color:white;"
                        );


                        return row;
                        }
                );

                // =====================================================
                // TABLE HEADER STYLE
                // =====================================================

                Platform.runLater(() -> {

                table.applyCss();


                Node headerBackground =
                        table.lookup(
                                ".column-header-background"
                        );


                if (headerBackground != null) {

                        headerBackground.setStyle(
                                "-fx-background-color:white;" +
                                "-fx-border-color:transparent transparent #e5e7eb transparent;"
                        );
                }


                Node filler =
                        table.lookup(
                                ".filler"
                        );


                if (filler != null) {

                        filler.setStyle(
                                "-fx-background-color:white;" +
                                "-fx-border-color:transparent transparent #e5e7eb transparent;"
                        );
                }
                });

                // =====================================================
                // ADD TO BOX
                // =====================================================

                box.getChildren().addAll(
                        title,
                        table
                );


                return box;
        }

        // =========================================================
        // RECENT NOTIFICATIONS
        // =========================================================

        private VBox createRecentNotifications() {

                VBox box =
                        new VBox(
                                12
                        );


                box.setPadding(
                        new Insets(
                                18
                        )
                );


                box.setStyle(
                        "-fx-background-color:white;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:#e5ebf3;" +
                        "-fx-border-radius:12;"
                );


                // =====================================================
                // TITLE
                // =====================================================

                Label title =
                        new Label(
                                "Recent Notifications"
                        );


                title.setStyle(
                        "-fx-text-fill:#111827;" +
                        "-fx-font-size:14;" +
                        "-fx-font-weight:bold;"
                );


                // =====================================================
                // NOTIFICATION CONTAINER
                // =====================================================

                VBox notifications =
                        new VBox(
                                12
                        );


                // =====================================================
                // GET LATEST 8 NOTIFICATIONS
                // =====================================================

                List<Notification> notificationList =
                        notificationController
                                .getRecentNotifications(
                                        8
                                );


                // =====================================================
                // ADD NOTIFICATIONS
                // =====================================================

                if (
                        notificationList != null
                        &&
                        !notificationList.isEmpty()
                ) {

                        for (
                                Notification notification :
                                notificationList
                        ) {

                        if (notification == null) {

                                continue;
                        }


                        String icon =
                                getNotificationIcon(
                                        notification.getType()
                                );


                        HBox notificationRow =
                                createNotification(

                                        icon,

                                        safeValue(
                                                notification.getTitle()
                                        ),

                                        safeValue(
                                                notification.getMessage()
                                        ),

                                        safeValue(
                                                notification.getCreatedDate()
                                        )
                                );


                        notifications.getChildren().add(
                                notificationRow
                        );
                        }

                } else {

                        // -------------------------------------------------
                        // NO NOTIFICATIONS
                        // -------------------------------------------------

                        Label emptyLabel =
                                new Label(
                                        "No recent notifications."
                                );


                        emptyLabel.setStyle(
                                "-fx-text-fill:#8a93a1;" +
                                "-fx-font-size:10;"
                        );


                        notifications.getChildren().add(
                                emptyLabel
                        );
                }


                // =====================================================
                // SCROLLABLE NOTIFICATION AREA
                // =====================================================

                javafx.scene.control.ScrollPane scrollPane =
                        new javafx.scene.control.ScrollPane(
                                notifications
                        );


                scrollPane.setFitToWidth(
                        true
                );


                scrollPane.setPrefHeight(
                        190
                );


                scrollPane.setMaxHeight(
                        190
                );


                scrollPane.setHbarPolicy(
                        javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER
                );


                scrollPane.setVbarPolicy(
                        javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER
                );


                scrollPane.setStyle(
                        "-fx-background-color:transparent;" +
                        "-fx-background:#ffffff;"
                );


                // =====================================================
                // ADD TO BOX
                // =====================================================

                box.getChildren().addAll(
                        title,
                        scrollPane
                );


                return box;
        }

        // =========================================================
        // GET NOTIFICATION ICON
        // =========================================================

        private String getNotificationIcon(
                        String type
                ) {

                String notificationType =
                        safeValue(
                                type
                        );


                // =====================================================
                // ADD
                // =====================================================

                if (
                        notificationType.equalsIgnoreCase(
                                "ADD"
                        )
                ) {

                        return "+";
                }


                // =====================================================
                // EDIT
                // =====================================================

                if (
                        notificationType.equalsIgnoreCase(
                                "EDIT"
                        )
                ) {

                        return "✎";
                }


                // =====================================================
                // DELETE
                // =====================================================

                if (
                        notificationType.equalsIgnoreCase(
                                "DELETE"
                        )
                ) {

                        return "×";
                }


                // =====================================================
                // ALLOCATION
                // =====================================================

                if (
                        notificationType.equalsIgnoreCase(
                                "ALLOCATION"
                        )
                ) {

                        return "A";
                }


                // =====================================================
                // REPORT
                // =====================================================

                if (
                        notificationType.equalsIgnoreCase(
                                "REPORT"
                        )
                ) {

                        return "R";
                }


                // =====================================================
                // DEFAULT
                // =====================================================

                return "!";
        }


        // =========================================================
        // NOTIFICATION
        // =========================================================

        private HBox createNotification(

                        String icon,

                        String titleText,

                        String messageText,

                        String createdDate

                ) {

                HBox notification =
                        new HBox(
                                10
                        );


                notification.setAlignment(
                        Pos.TOP_LEFT
                );


                // =====================================================
                // ICON
                // =====================================================

                Label iconLabel =
                        new Label(
                                icon
                        );


                iconLabel.setPrefSize(
                        24,
                        24
                );


                iconLabel.setAlignment(
                        Pos.CENTER
                );


                iconLabel.setStyle(
                        "-fx-background-color:#eaf2ff;" +
                        "-fx-background-radius:12;" +
                        "-fx-text-fill:#0f5fff;" +
                        "-fx-font-weight:bold;"
                );


                // =====================================================
                // TEXT AREA
                // =====================================================

                VBox textBox =
                        new VBox(
                                2
                        );


                HBox.setHgrow(
                        textBox,
                        Priority.ALWAYS
                );


                // =====================================================
                // TITLE + TIMESTAMP
                // =====================================================

                HBox titleRow =
                        new HBox();


                titleRow.setAlignment(
                        Pos.CENTER_LEFT
                );


                Label titleLabel =
                        new Label(
                                titleText
                        );


                titleLabel.setStyle(
                        "-fx-text-fill:#111827;" +
                        "-fx-font-size:11;" +
                        "-fx-font-weight:bold;"
                );


                // =====================================================
                // SPACE BETWEEN TITLE AND TIMESTAMP
                // =====================================================

                Region titleSpacer =
                        new Region();


                HBox.setHgrow(
                        titleSpacer,
                        Priority.ALWAYS
                );


                // =====================================================
                // TIMESTAMP
                // =====================================================

                Label timeLabel =
                        new Label(
                                formatNotificationTime(
                                        createdDate
                                )
                        );


                timeLabel.setStyle(
                        "-fx-text-fill:#9aa3af;" +
                        "-fx-font-size:9;"
                );


                titleRow.getChildren().addAll(

                        titleLabel,

                        titleSpacer,

                        timeLabel
                );


                // =====================================================
                // MESSAGE
                // =====================================================

                Label messageLabel =
                        new Label(
                                messageText
                        );


                messageLabel.setStyle(
                        "-fx-text-fill:#8a93a1;" +
                        "-fx-font-size:9;"
                );


                // =====================================================
                // ADD TITLE ROW + MESSAGE
                // =====================================================

                textBox.getChildren().addAll(

                        titleRow,

                        messageLabel
                );


                // =====================================================
                // ADD EVERYTHING
                // =====================================================

                notification.getChildren().addAll(

                        iconLabel,

                        textBox
                );


                return notification;
        }


        // =========================================================
        // FORMAT NOTIFICATION TIME
        // =========================================================

        private String formatNotificationTime(

                        String createdDate

                ) {

                if (
                        createdDate == null
                        ||
                        createdDate.trim().isEmpty()
                ) {

                        return "";
                }


                try {

                        LocalDateTime notificationTime =
                                LocalDateTime.parse(
                                        createdDate,
                                        DateTimeFormatter.ofPattern(
                                                "yyyy-MM-dd HH:mm:ss"
                                        )
                                );


                        LocalDateTime now =
                                LocalDateTime.now();


                        Duration duration =
                                Duration.between(
                                        notificationTime,
                                        now
                                );


                        long seconds =
                                duration.getSeconds();


                        // =================================================
                        // FUTURE / INVALID TIME
                        // =================================================

                        if (seconds < 0) {

                        return "Just now";
                        }


                        // =================================================
                        // LESS THAN 1 MINUTE
                        // =================================================

                        if (seconds < 60) {

                        return "Just now";
                        }


                        // =================================================
                        // MINUTES
                        // =================================================

                        long minutes =
                                seconds / 60;


                        if (minutes < 60) {

                        return minutes
                                +
                                (
                                        minutes == 1
                                                ? " min ago"
                                                : " mins ago"
                                );
                        }


                        // =================================================
                        // HOURS
                        // =================================================

                        long hours =
                                minutes / 60;


                        if (hours < 24) {

                        return hours
                                +
                                (
                                        hours == 1
                                                ? " hour ago"
                                                : " hours ago"
                                );
                        }


                        // =================================================
                        // DAYS
                        // =================================================

                        long days =
                                hours / 24;


                        if (days == 1) {

                        return "Yesterday";
                        }


                        if (days < 7) {

                        return days
                                +
                                " days ago";
                        }


                        // =================================================
                        // OLDER NOTIFICATIONS
                        // =================================================

                        return notificationTime.format(

                                DateTimeFormatter.ofPattern(
                                        "dd MMM, hh:mm a"
                                )

                        );

                } catch (Exception e) {

                        return "";
                }
        }


    // =========================================================
    // FORMAT INTEGER
    // =========================================================

    private String formatInteger(
            int value
    ) {

        return NumberFormat
                .getIntegerInstance(
                        Locale.US
                )
                .format(
                        value
                );
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
    // FORMAT PERCENTAGE
    // =========================================================

    private String formatPercentage(
            double value
    ) {

        NumberFormat formatter =
                NumberFormat.getNumberInstance(
                        Locale.US
                );


        formatter.setMaximumFractionDigits(
                1
        );


        return formatter.format(
                value
        ) + "%";
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


        return value.trim();
    }


    // =========================================================
    // GET ROOT
    // =========================================================

    public VBox getRoot() {

        return root;
    }
}