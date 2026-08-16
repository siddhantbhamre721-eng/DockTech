package com.projectui.view.portauthority;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DashboardPage {

    private VBox root = new VBox();

    public DashboardPage() {

        root.setSpacing(18);

        // =====================================================
        // DASHBOARD TITLE
        // =====================================================

        Label title =
                new Label("Dashboard");

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
                new HBox(14);

        cards.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox totalShips =
                createCard(
                        "⚓",
                        "42",
                        "Total Ships"
                );

        VBox activeShips =
                createCard(
                        "⚓",
                        "18",
                        "Active Ships"
                );

        VBox cargoHandled =
                createCard(
                        "◆",
                        "12,540",
                        "Cargo Handled (Tons)"
                );

        VBox containers =
                createCard(
                        "▣",
                        "8,760",
                        "Containers"
                );

        VBox pendingPayments =
                createCard(
                        "₹",
                        "24",
                        "Pending Payments"
                );

        cards.getChildren().addAll(
                totalShips,
                activeShips,
                cargoHandled,
                containers,
                pendingPayments
        );

        // =====================================================
        // SHIP ACTIVITY CHART, Cargo Overview Chart
        // =====================================================

        VBox shipActivity =
                createShipActivityChart();
        
        VBox cargoOverview =
                createCargoOverviewChart();

        VBox containerStatus =
                createContainerStatusChart();

        // =====================================================
        // CHART ROW
        // =====================================================

        HBox chartRow =
                new HBox(14);

        chartRow.setAlignment(
                Pos.CENTER_LEFT
        );

        HBox.setHgrow(shipActivity,Priority.ALWAYS);

        HBox.setHgrow(cargoOverview, Priority.ALWAYS);

        HBox.setHgrow(containerStatus, Priority.ALWAYS);

        chartRow.getChildren().addAll(
                shipActivity,
                cargoOverview,
                containerStatus
        );

        VBox recentShipActivity =
                createRecentShipActivity();

        VBox recentNotifications =
                createRecentNotifications();

        // =====================================================
        // BOTTOM ROW
        // =====================================================

        HBox bottomRow =
                new HBox(14);

        bottomRow.setAlignment(
                Pos.CENTER_LEFT
        );

        HBox.setHgrow(
                recentShipActivity,
                javafx.scene.layout.Priority.ALWAYS
        );

        HBox.setHgrow(
                recentNotifications,
                javafx.scene.layout.Priority.ALWAYS
        );

        bottomRow.getChildren().addAll(
                recentShipActivity,
                recentNotifications
        );

        // =====================================================
        // ADD TO ROOT
        // =====================================================

        root.getChildren().addAll(
                heading,
                cards,
                chartRow,
                bottomRow
        );      
    }

    //Constructor End
    //================================================


    // =====================================================
    // CREATE STATISTIC CARD
    // =====================================================

    private VBox createCard(
            String icon,
            String value,
            String text) {

        VBox card =
                new VBox(8);

        card.setPrefWidth(180);

        card.setPrefHeight(105);

        card.setPadding(
                new Insets(14)
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
                new Label(icon);

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
                "-fx-font-size:14;"
        );

        Label valueLabel =
                new Label(value);

        valueLabel.setStyle(
                "-fx-text-fill:#111827;" +
                "-fx-font-size:22;" +
                "-fx-font-weight:bold;"
        );

        Label textLabel =
                new Label(text);

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

    // =====================================================
    // SHIP ACTIVITY CHART
    
    private VBox createShipActivityChart() {

        VBox chartBox =
                new VBox(10);

        chartBox.setMaxWidth(
                Double.MAX_VALUE
        );

        chartBox.setPrefWidth(450);

        chartBox.setPadding(
                new Insets(18)
        );

        chartBox.setPrefHeight(280);

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
                new Label("Ship Activity");

        title.setStyle(
                "-fx-text-fill:#111827;" +
                "-fx-font-size:14;" +
                "-fx-font-weight:bold;"
        );

        Label subtitle =
                new Label("Last 7 days");

        subtitle.setStyle(
                "-fx-text-fill:#8a93a1;" +
                "-fx-font-size:10;"
        );

        // =====================================================
        // X AXIS
        // =====================================================

        CategoryAxis xAxis =
                new CategoryAxis();

        xAxis.setCategories(
                javafx.collections.FXCollections.observableArrayList(
                        "Tue",
                        "Wed",
                        "Thu",
                        "Fri",
                        "Sat",
                        "Sun"
                )
        );

        // =====================================================
        // Y AXIS
        // =====================================================

        NumberAxis yAxis =
                new NumberAxis();

        yAxis.setAutoRanging(true);

        yAxis.setTickLabelsVisible(false);

        yAxis.setTickMarkVisible(false);

        // =====================================================
        // LINE CHART
        // =====================================================

        LineChart<String, Number> chart =
                new LineChart<>(
                        xAxis,
                        yAxis
                );

        chart.setPrefHeight(180);

        chart.setAnimated(false);

        chart.setCreateSymbols(true);

        chart.setLegendVisible(false);

        chart.setHorizontalGridLinesVisible(true);

        chart.setVerticalGridLinesVisible(false);

        // =====================================================
        // ARRIVALS
        // =====================================================

        XYChart.Series<String, Number> arrivals =
                new XYChart.Series<>();

        arrivals.setName("Arrivals");

        arrivals.getData().add(
                new XYChart.Data<>("Tue", 12)
        );

        arrivals.getData().add(
                new XYChart.Data<>("Wed", 15)
        );

        arrivals.getData().add(
                new XYChart.Data<>("Thu", 10)
        );

        arrivals.getData().add(
                new XYChart.Data<>("Fri", 18)
        );

        arrivals.getData().add(
                new XYChart.Data<>("Sat", 14)
        );

        arrivals.getData().add(
                new XYChart.Data<>("Sun", 20)
        );

        // =====================================================
        // DEPARTURES
        // =====================================================

        XYChart.Series<String, Number> departures =
                new XYChart.Series<>();

        departures.setName("Departures");

        departures.getData().add(
                new XYChart.Data<>("Tue", 8)
        );

        departures.getData().add(
                new XYChart.Data<>("Wed", 12)
        );

        departures.getData().add(
                new XYChart.Data<>("Thu", 9)
        );

        departures.getData().add(
                new XYChart.Data<>("Fri", 15)
        );

        departures.getData().add(
                new XYChart.Data<>("Sat", 11)
        );

        departures.getData().add(
                new XYChart.Data<>("Sun", 16)
        );

        // =====================================================
        // DOCKED
        // =====================================================

        XYChart.Series<String, Number> docked =
                new XYChart.Series<>();

        docked.setName("Docked");

        docked.getData().add(
                new XYChart.Data<>("Tue", 6)
        );

        docked.getData().add(
                new XYChart.Data<>("Wed", 9)
        );

        docked.getData().add(
                new XYChart.Data<>("Thu", 7)
        );

        docked.getData().add(
                new XYChart.Data<>("Fri", 10)
        );

        docked.getData().add(
                new XYChart.Data<>("Sat", 8)
        );

        docked.getData().add(
                new XYChart.Data<>("Sun", 13)
        );

        chart.getData().addAll(
                arrivals,
                departures,
                docked
        );

        // =====================================================
        // LEGEND
        // =====================================================

        HBox legend =
                new HBox(18);

        legend.setAlignment(
                Pos.CENTER_LEFT
        );

        Label arrivalsLegend =
                new Label("● Arrivals");

        arrivalsLegend.setStyle(
                "-fx-text-fill: #2f80ed;" +
                "-fx-font-size:10;"
        );

        Label departuresLegend =
                new Label("● Departures");

        departuresLegend.setStyle(
                "-fx-text-fill: #18a6c9;" +
                "-fx-font-size:10;"
        );

        Label dockedLegend =
                new Label("● Docked");

        dockedLegend.setStyle(
                "-fx-text-fill: #12a66a;" +
                "-fx-font-size:10;"
        );

        legend.getChildren().addAll(
                arrivalsLegend,
                departuresLegend,
                dockedLegend
        );

        // =====================================================
        // ADD EVERYTHING
        // =====================================================

        chartBox.getChildren().addAll(
                title,
                subtitle,
                chart,
                legend
        );

                return chartBox;
    }   
    
    //Ship Activity Chart end
    //======================================================

    //====================================
    //Cargo overview chart

        private VBox createCargoOverviewChart() {

                VBox chartBox =
                        new VBox(10);

                chartBox.setMaxWidth(
                        Double.MAX_VALUE
                );

                chartBox.setPrefWidth(450);

                chartBox.setPadding(
                        new Insets(18)
                );

                chartBox.setPrefHeight(280);

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
                        new Label("Cargo Overview");

                title.setStyle(
                        "-fx-text-fill:#111827;" +
                        "-fx-font-size:14;" +
                        "-fx-font-weight:bold;"
                );

                Label subtitle =
                        new Label("Tons");

                subtitle.setStyle(
                        "-fx-text-fill:#8a93a1;" +
                        "-fx-font-size:10;"
                );

                // =====================================================
                // X AXIS
                // =====================================================

                CategoryAxis xAxis =
                        new CategoryAxis();

                xAxis.setCategories(
                        javafx.collections.FXCollections.observableArrayList(
                                "Received",
                                "Loaded",
                                "Dispatched",
                                "Pending"
                        )
                );

                // =====================================================
                // Y AXIS
                // =====================================================

                NumberAxis yAxis =
                        new NumberAxis();

                yAxis.setAutoRanging(true);

                yAxis.setTickLabelsVisible(false);

                yAxis.setTickMarkVisible(false);

                // =====================================================
                // BAR CHART
                // =====================================================

                BarChart<String, Number> chart =
                        new BarChart<>(
                                xAxis,
                                yAxis
                        );

                chart.setPrefHeight(180);

                chart.setLegendVisible(false);

                chart.setAnimated(false);

                chart.setHorizontalGridLinesVisible(true);

                chart.setVerticalGridLinesVisible(false);

                // =====================================================
                // CARGO DATA
                // =====================================================

                XYChart.Series<String, Number> cargo =
                        new XYChart.Series<>();

                cargo.getData().add(
                        new XYChart.Data<>(
                                "Received",
                                7820
                        )
                );

                cargo.getData().add(
                        new XYChart.Data<>(
                                "Loaded",
                                3420
                        )
                );

                cargo.getData().add(
                        new XYChart.Data<>(
                                "Dispatched",
                                2600
                        )
                );

                cargo.getData().add(
                        new XYChart.Data<>(
                                "Pending",
                                1300
                        )
                );

                chart.getData().add(
                        cargo
                );

                // =====================================================
                // LEGEND
                // =====================================================

                HBox legend =
                        new HBox(14);

                legend.setAlignment(
                        Pos.CENTER_LEFT
                );

                Label received =
                        new Label("● Received");

                received.setStyle(
                        "-fx-text-fill:#2f80ed;" +
                        "-fx-font-size:10;"
                );

                Label loaded =
                        new Label("● Loaded");

                loaded.setStyle(
                        "-fx-text-fill:#18a6c9;" +
                        "-fx-font-size:10;"
                );

                Label dispatched =
                        new Label("● Dispatched");

                dispatched.setStyle(
                        "-fx-text-fill:#f39c12;" +
                        "-fx-font-size:10;"
                );

                Label pending =
                        new Label("● Pending");

                pending.setStyle(
                        "-fx-text-fill:#9aa0ad;" +
                        "-fx-font-size:10;"
                );

                legend.getChildren().addAll(
                        received,
                        loaded,
                        dispatched,
                        pending
                );

                // =====================================================
                // ADD EVERYTHING
                // =====================================================

                chartBox.getChildren().addAll(
                        title,
                        subtitle,
                        chart,
                        legend
                );

                return chartBox;
        }

        //Vargo overview chart end
        //==================================================

        //===============================================
        //Container Status chart

        private VBox createContainerStatusChart() {

                VBox chartBox =
                        new VBox(10);

                chartBox.setMaxWidth(
                        Double.MAX_VALUE
                );

                chartBox.setPrefWidth(380);

                chartBox.setPadding(
                        new Insets(18)
                );

                chartBox.setPrefHeight(280);

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
                        new Label("Container Status");

                title.setStyle(
                        "-fx-text-fill:#111827;" +
                        "-fx-font-size:14;" +
                        "-fx-font-weight:bold;"
                );

                Label subtitle =
                        new Label("8,760 total");

                subtitle.setStyle(
                        "-fx-text-fill:#8a93a1;" +
                        "-fx-font-size:10;"
                );

                // =====================================================
                // PIE CHART
                // =====================================================

                PieChart chart =
                        new PieChart();

                chart.setPrefSize(
                        170,
                        170
                );

                chart.setLegendVisible(false);

                chart.setLabelsVisible(false);

                chart.setStartAngle(
                        90
                );

                chart.setClockwise(
                        true
                );

                // =====================================================
                // CONTAINER DATA
                // =====================================================

                PieChart.Data allocated =
                        new PieChart.Data(
                                "Allocated",
                                6420
                        );

                PieChart.Data available =
                        new PieChart.Data(
                                "Available",
                                1850
                        );

                PieChart.Data inTransit =
                        new PieChart.Data(
                                "In Transit",
                                320
                        );

                PieChart.Data pending =
                        new PieChart.Data(
                                "Pending",
                                170
                        );

                chart.getData().addAll(
                        allocated,
                        available,
                        inTransit,
                        pending
                );

                // =====================================================
                // DONUT EFFECT
                // =====================================================

                Label centerLabel =
                        new Label(
                                "8,760"
                        );

                centerLabel.setStyle(
                        "-fx-text-fill: #111827;" +
                        "-fx-font-size:18;" +
                        "-fx-font-weight:bold;"
                );

                // =====================================================
                // CHART
                // =====================================================

                // =====================================================
                // DONUT CHART
                // =====================================================

                StackPane donutChart =
                        new StackPane();

                donutChart.setPrefSize(
                        170,
                        170
                );

                donutChart.getChildren().add(
                        chart
                );

                // WHITE CIRCLE
                // Creates the hole in the middle

                javafx.scene.shape.Circle circle =
                        new javafx.scene.shape.Circle(
                                38
                        );

                circle.setStyle(
                        "-fx-fill:white;"
                );

                // CENTER VALUE

                Label centerValue =
                        new Label(
                                "8,760"
                        );

                centerValue.setStyle(
                        "-fx-text-fill:#111827;" +
                        "-fx-font-size:17;" +
                        "-fx-font-weight:bold;"
                );

                donutChart.getChildren().addAll(
                        circle,
                        centerValue
                );

                // =====================================================
                // LEGEND
                // =====================================================

                HBox legend =
                        new HBox(12);

                legend.setAlignment(
                        Pos.CENTER
                );

                Label allocatedLabel =
                        new Label(
                                "● Allocated"
                        );

                allocatedLabel.setStyle(
                        "-fx-text-fill:#12a66a;" +
                        "-fx-font-size:9;"
                );

                Label availableLabel =
                        new Label(
                                "● Available"
                        );

                availableLabel.setStyle(
                        "-fx-text-fill:#2f80ed;" +
                        "-fx-font-size:9;"
                );

                Label inTransitLabel =
                        new Label(
                                "● In Transit"
                        );

                inTransitLabel.setStyle(
                        "-fx-text-fill:#f39c12;" +
                        "-fx-font-size:9;"
                );

                Label pendingLabel =
                        new Label(
                                "● Pending"
                        );

                pendingLabel.setStyle(
                        "-fx-text-fill:#9aa0ad;" +
                        "-fx-font-size:9;"
                );

                legend.getChildren().addAll(
                        allocatedLabel,
                        availableLabel,
                        inTransitLabel,
                        pendingLabel
                );

                // =====================================================
                // ADD EVERYTHING
                // =====================================================

                chartBox.getChildren().addAll(
                        title,
                        subtitle,
                        donutChart,
                        legend
                );

                return chartBox;
        }

        //Chart Ends
        //==========================================

        //=========================================
        //Recent Ship Activity table

        private VBox createRecentShipActivity() {

                VBox box =
                        new VBox(10);

                box.setPadding(
                        new Insets(18)
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
                        new Label("Recent Ship Activity");

                title.setStyle(
                        "-fx-text-fill:#111827;" +
                        "-fx-font-size:14;" +
                        "-fx-font-weight:bold;"
                );

                // =====================================================
                // TABLE
                // =====================================================

                TableView<String[]> table =
                        new TableView<>();

                table.setPrefHeight(150);

                table.setColumnResizePolicy(
                        TableView.CONSTRAINED_RESIZE_POLICY
                );

                // =====================================================
                // SHIP COLUMN
                // =====================================================

                TableColumn<String[], String> shipColumn =
                        new TableColumn<>("Ship");

                shipColumn.setCellValueFactory(
                        data ->
                                new javafx.beans.property.SimpleStringProperty(
                                        data.getValue()[0]
                                )
                );

                // =====================================================
                // STATUS COLUMN
                // =====================================================

                TableColumn<String[], String> statusColumn =
                        new TableColumn<>("Status");

                statusColumn.setCellValueFactory(
                        data ->
                                new javafx.beans.property.SimpleStringProperty(
                                        data.getValue()[1]
                                )
                );

                // =====================================================
                // ARRIVAL COLUMN
                // =====================================================

                TableColumn<String[], String> arrivalColumn =
                        new TableColumn<>("Arrival");

                arrivalColumn.setCellValueFactory(
                        data ->
                                new javafx.beans.property.SimpleStringProperty(
                                        data.getValue()[2]
                                )
                );

                // =====================================================
                // BERTH COLUMN
                // =====================================================

                TableColumn<String[], String> berthColumn =
                        new TableColumn<>("Berth");

                berthColumn.setCellValueFactory(
                        data ->
                                new javafx.beans.property.SimpleStringProperty(
                                        data.getValue()[3]
                                )
                );

                table.getColumns().addAll(
                        shipColumn,
                        statusColumn,
                        arrivalColumn,
                        berthColumn
                );

                // =====================================================
                // SAMPLE DATA
                // =====================================================

                table.getItems().addAll(

                        new String[]{
                                "MSC Aurora",
                                "Docked",
                                "10:30 AM",
                                "A-02"
                        },

                        new String[]{
                                "Maersk Titan",
                                "Arriving",
                                "11:45 AM",
                                "B-01"
                        },

                        new String[]{
                                "CMA CGM Leo",
                                "Departed",
                                "09:20 AM",
                                "C-03"
                        },

                        new String[]{
                                "Ever Green",
                                "Docked",
                                "08:50 AM",
                                "A-04"
                        }
                );

                // =====================================================
                // TABLE STYLE
                // =====================================================

                table.setStyle(
                        "-fx-font-size:10;"
                );

                box.getChildren().addAll(
                        title,
                        table
                );

                return box;
        }

        //recent ship activity ended
        //================================================

        //================================================
        // recent notification

        private VBox createRecentNotifications() {

                VBox box =
                        new VBox(12);

                box.setPadding(
                        new Insets(18)
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
                        new Label("Recent Notifications");

                title.setStyle(
                        "-fx-text-fill:#111827;" +
                        "-fx-font-size:14;" +
                        "-fx-font-weight:bold;"
                );

                // =====================================================
                // NOTIFICATIONS
                // =====================================================

                VBox notifications =
                        new VBox(12);

                // Notification 1

                HBox notification1 =
                        createNotification(
                                "●",
                                "New ship arrived",
                                "MSC Aurora has docked at Berth A-02"
                        );

                // Notification 2

                HBox notification2 =
                        createNotification(
                                "●",
                                "Cargo updated",
                                "12,540 tons of cargo handled"
                        );

                // Notification 3

                HBox notification3 =
                        createNotification(
                                "●",
                                "Payment pending",
                                "24 payments are waiting for approval"
                        );

                // Notification 4

                HBox notification4 =
                        createNotification(
                                "●",
                                "Container allocated",
                                "New containers have been assigned"
                        );

                notifications.getChildren().addAll(
                        notification1,
                        notification2,
                        notification3,
                        notification4
                );

                box.getChildren().addAll(
                        title,
                        notifications
                );

                return box;
        }

        //---------Notification---------

        private HBox createNotification(

                String icon,
                String titleText,
                String messageText) {

                HBox notification =
                        new HBox(10);

                notification.setAlignment(
                        Pos.TOP_LEFT
                );

                // =====================================================
                // ICON
                // =====================================================

                Label iconLabel =
                        new Label(icon);

                iconLabel.setStyle(
                        "-fx-text-fill:#0f5fff;" +
                        "-fx-font-size:12;"
                );

                // =====================================================
                // TEXT
                // =====================================================

                VBox text =
                        new VBox(3);

                Label title =
                        new Label(titleText);

                title.setStyle(
                        "-fx-text-fill:#111827;" +
                        "-fx-font-size:11;" +
                        "-fx-font-weight:bold;"
                );

                Label message =
                        new Label(messageText);

                message.setStyle(
                        "-fx-text-fill:#8a93a1;" +
                        "-fx-font-size:9;"
                );

                text.getChildren().addAll(
                        title,
                        message
                );

                notification.getChildren().addAll(
                        iconLabel,
                        text
                );

                return notification;
        }

    // =====================================================
    // GET ROOT
    // =====================================================

    public VBox getRoot() {

        return root;
    }
}