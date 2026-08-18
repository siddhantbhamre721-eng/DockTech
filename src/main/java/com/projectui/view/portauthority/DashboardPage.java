package com.projectui.view.portauthority;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings({"unchecked", "deprecation", "unused"})
public class DashboardPage {

    private VBox root = new VBox();
    private VBox managedPersonnelSectionNode;

    public DashboardPage() {

        root.setSpacing(18);

        // =====================================================
        // DASHBOARD TITLE
        // =====================================================

        Label title =
                new Label("Dashboard");

        title.setStyle(
                "-fx-text-fill:#0F172A;" +
                "-fx-font-size:22px;" +
                "-fx-font-weight:bold;"
        );

        Label subtitle =
                new Label(
                        "Welcome back, Port Authority."
                );

        subtitle.setStyle(
                "-fx-text-fill:#64748B;" +
                "-fx-font-size:13px;"
        );

        VBox heading =
                new VBox(
                        4,
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

        managedPersonnelSectionNode = createManagedPersonnelSection();

        root.getChildren().addAll(
                heading,
                cards,
                chartRow,
                bottomRow,
                managedPersonnelSectionNode
        );      
    }

    public void refreshPersonnelSection() {
        if (root != null) {
            int idx = managedPersonnelSectionNode != null ? root.getChildren().indexOf(managedPersonnelSectionNode) : -1;
            managedPersonnelSectionNode = createManagedPersonnelSection();
            if (idx >= 0 && idx < root.getChildren().size()) {
                root.getChildren().set(idx, managedPersonnelSectionNode);
            } else {
                root.getChildren().add(managedPersonnelSectionNode);
            }
        }
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
    // MANAGED PERSONNEL & ACCOUNT STATUS MONITORING
    // =====================================================

    private VBox createManagedPersonnelSection() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#e2e8f0; -fx-border-radius:12;");

        Label sectionTitle = new Label("Managed Personnel & Account Status");
        sectionTitle.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0F172A;");

        Label sectionSub = new Label("Monitor, provision, and control account status for Custom Officers and Ship Captains.");
        sectionSub.setStyle("-fx-font-size:13px; -fx-text-fill:#64748B;");

        VBox secHead = new VBox(4, sectionTitle, sectionSub);

        // Fetch managed users
        java.util.List<Map<String, Object>> officers = com.projectui.dao.UserDao.getUsersByRoles("Custom Officer", "Customs Officer");
        java.util.List<Map<String, Object>> captains = com.projectui.dao.UserDao.getUsersByRoles("Ship Captain");

        VBox officerCard = buildPersonnelCard("Custom Officers", "Custom Officer", officers, () -> openCreatePersonnelModal("Custom Officer", container));
        VBox captainCard = buildPersonnelCard("Ship Captains", "Ship Captain", captains, () -> openCreatePersonnelModal("Ship Captain", container));

        HBox grid = new HBox(16, officerCard, captainCard);
        HBox.setHgrow(officerCard, Priority.ALWAYS);
        HBox.setHgrow(captainCard, Priority.ALWAYS);

        container.getChildren().addAll(secHead, grid);
        return container;
    }

    private VBox buildPersonnelCard(String title, String role, java.util.List<Map<String, Object>> list, Runnable onCreate) {
        VBox box = new VBox(14);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:#f8fafc; -fx-background-radius:10; -fx-border-color:#cbd5e1; -fx-border-radius:10;");

        int total = list.size();
        int active = 0, pending = 0, inactive = 0;
        for (Map<String, Object> u : list) {
            String st = u.get("status") != null ? u.get("status").toString() : "Active";
            if (st.equalsIgnoreCase("Active") || st.equalsIgnoreCase("Approved") || st.equalsIgnoreCase("Operational")) active++;
            else if (st.equalsIgnoreCase("Pending")) pending++;
            else inactive++;
        }

        Label tLabel = new Label(title);
        tLabel.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");

        Button addBtn = new Button("+ Create " + role);
        addBtn.setStyle("-fx-background-color:#0b63ff; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:6; -fx-cursor:hand;");
        addBtn.setOnAction(e -> onCreate.run());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox header = new HBox(10, tLabel, sp, addBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        // Stats row
        HBox stats = new HBox(12);
        stats.getChildren().addAll(
                buildMiniStat("Total", String.valueOf(total), "#334155"),
                buildMiniStat("Active", String.valueOf(active), "#15803d"),
                buildMiniStat("Pending", String.valueOf(pending), "#b45309"),
                buildMiniStat("Inactive", String.valueOf(inactive), "#b91c1c")
        );

        // Table List
        VBox rows = new VBox(8);
        if (list.isEmpty()) {
            Label empty = new Label("No " + title.toLowerCase() + " registered yet.");
            empty.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px; -fx-padding:10 0;");
            rows.getChildren().add(empty);
        } else {
            for (Map<String, Object> user : list) {
                rows.getChildren().add(buildUserRow(user, box));
            }
        }

        ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(220);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-color:transparent;");

        box.getChildren().addAll(header, stats, scroll);
        return box;
    }

    private VBox buildMiniStat(String label, String value, String colorHex) {
        VBox stat = new VBox(2);
        stat.setAlignment(Pos.CENTER);
        stat.setPadding(new Insets(6, 12, 6, 12));
        stat.setStyle("-fx-background-color:white; -fx-background-radius:6; -fx-border-color:#e2e8f0; -fx-border-radius:6;");

        Label val = new Label(value);
        val.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + colorHex + ";");

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:10px; -fx-text-fill:#64748B;");

        stat.getChildren().addAll(val, lbl);
        return stat;
    }

    private HBox buildUserRow(Map<String, Object> user, VBox parentContainer) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setStyle("-fx-background-color:white; -fx-background-radius:8; -fx-border-color:#f1f5f9; -fx-border-radius:8;");

        String name = user.get("name") != null ? user.get("name").toString() : "User";
        String email = user.get("email") != null ? user.get("email").toString() : "";
        String role = user.get("role") != null ? user.get("role").toString() : "";
        String status = user.get("status") != null ? user.get("status").toString() : "Active";
        String company = user.get("company") != null ? user.get("company").toString() : (user.get("organization") != null ? user.get("organization").toString() : "Mumbai Port");
        String uid = user.get("uid") != null ? user.get("uid").toString() : email;

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill:#0f172a;");

        String subInfo = role.contains("Captain") ? "Ship: " + company + " | Port: Mumbai Port" : "Assigned: " + company;
        Label detailsLabel = new Label(email + " • " + subInfo);
        detailsLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");

        VBox info = new VBox(2, nameLabel, detailsLabel);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        boolean isActive = status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("Approved") || status.equalsIgnoreCase("Operational");
        Label statusBadge = new Label(status);
        String bg = isActive ? "#dcfce7" : "#fee2e2";
        String fg = isActive ? "#15803d" : "#b91c1c";
        statusBadge.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + fg + "; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 8; -fx-background-radius:6;");

        Button toggleBtn = new Button(isActive ? "Deactivate" : "Activate");
        String btnBg = isActive ? "#ef4444" : "#16a34a";
        toggleBtn.setStyle("-fx-background-color:" + btnBg + "; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:6; -fx-cursor:hand;");

        toggleBtn.setOnAction(e -> {
            String newStatus = isActive ? "Inactive" : "Active";
            new com.projectui.dao.UserDao().updateUserStatus(uid, newStatus);
            user.put("status", newStatus);
            statusBadge.setText(newStatus);
            String newBg = !isActive ? "#dcfce7" : "#fee2e2";
            String newFg = !isActive ? "#15803d" : "#b91c1c";
            statusBadge.setStyle("-fx-background-color:" + newBg + "; -fx-text-fill:" + newFg + "; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 8; -fx-background-radius:6;");
            toggleBtn.setText(!isActive ? "Deactivate" : "Activate");
            toggleBtn.setStyle("-fx-background-color:" + (!isActive ? "#ef4444" : "#16a34a") + "; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:6; -fx-cursor:hand;");
            refreshPersonnelSection();
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color:#b91c1c; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:6; -fx-cursor:hand;");
        deleteBtn.setOnAction(e -> {
            String roleName = role.toLowerCase().contains("captain") ? "Ship Captain" : "Custom Officer";
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Account?");
            confirm.setHeaderText("Delete " + roleName + " Account?");
            confirm.setContentText("Are you sure you want to permanently delete this " + roleName + " account (" + name + ")?");

            ButtonType deleteBtnType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
            confirm.getButtonTypes().setAll(ButtonType.CANCEL, deleteBtnType);

            confirm.showAndWait().ifPresent(btn -> {
                if (btn == deleteBtnType) {
                    new com.projectui.dao.UserDao().deleteUserAccount(uid != null ? uid : email)
                            .whenComplete((unused, err) -> javafx.application.Platform.runLater(() -> {
                                refreshPersonnelSection();
                            }));
                }
            });
        });

        row.getChildren().addAll(info, sp, statusBadge, toggleBtn, deleteBtn);
        return row;
    }

    private void openCreatePersonnelModal(String role, VBox parentContainer) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create " + role + " Account");
        dialog.setHeaderText("Provision new " + role + " credentials and operational assignment.");

        ButtonType createButtonType = new ButtonType("Create Account", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 40, 20, 20));

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");
        TextField portField = new TextField("Mumbai Port");
        portField.setPromptText("Assigned Port");

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Assigned Port:"), 0, 3);
        grid.add(portField, 1, 3);

        TextField shipField = new TextField();
        TextField imoField = new TextField();
        TextField deptField = new TextField();
        TextField dutyField = new TextField();
        TextField shiftField = new TextField();

        if (role.toLowerCase().contains("captain")) {
            shipField.setPromptText("e.g. MV DockTech Leader");
            imoField.setPromptText("e.g. IMO-9876543");
            dutyField.setPromptText("e.g. Vessel Operations & Navigation");
            shiftField.setPromptText("e.g. Morning / Night");

            grid.add(new Label("Assigned Ship:"), 0, 4);
            grid.add(shipField, 1, 4);
            grid.add(new Label("Ship / IMO ID:"), 0, 5);
            grid.add(imoField, 1, 5);
            grid.add(new Label("Duty:"), 0, 6);
            grid.add(dutyField, 1, 6);
            grid.add(new Label("Shift:"), 0, 7);
            grid.add(shiftField, 1, 7);
        } else {
            deptField.setPromptText("e.g. Customs Clearance & Inspection");
            dutyField.setPromptText("e.g. Cargo & Container Verification");
            shiftField.setPromptText("e.g. Morning / Evening");

            grid.add(new Label("Department:"), 0, 4);
            grid.add(deptField, 1, 4);
            grid.add(new Label("Duty:"), 0, 5);
            grid.add(dutyField, 1, 5);
            grid.add(new Label("Shift:"), 0, 6);
            grid.add(shiftField, 1, 6);
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String n = nameField.getText().trim();
                String em = emailField.getText().trim();
                String ph = phoneField.getText().trim();
                String pt = portField.getText().trim();

                if (!n.isEmpty() && !em.isEmpty()) {
                    Map<String, Object> extra = new HashMap<>();
                    extra.put("assignedPort", pt.isEmpty() ? "Mumbai Port" : pt);
                    if (role.toLowerCase().contains("captain")) {
                        extra.put("assignedShip", shipField.getText().trim().isEmpty() ? "MV DockTech Leader" : shipField.getText().trim());
                        extra.put("shipImo", imoField.getText().trim().isEmpty() ? "IMO-9876543" : imoField.getText().trim());
                        extra.put("duty", dutyField.getText().trim().isEmpty() ? "Vessel Operations" : dutyField.getText().trim());
                        extra.put("shift", shiftField.getText().trim().isEmpty() ? "Morning" : shiftField.getText().trim());
                    } else {
                        extra.put("department", deptField.getText().trim().isEmpty() ? "Customs Inspection" : deptField.getText().trim());
                        extra.put("duty", dutyField.getText().trim().isEmpty() ? "Cargo Verification" : dutyField.getText().trim());
                        extra.put("shift", shiftField.getText().trim().isEmpty() ? "Morning" : shiftField.getText().trim());
                    }

                    new com.projectui.dao.UserDao().provisionAccountExtended(
                            "Port Authority", n, em, pt.isEmpty() ? "Mumbai Port" : pt, ph.isEmpty() ? "+91 9800000000" : ph, role, "Active", extra
                    ).whenComplete((creds, err) -> javafx.application.Platform.runLater(() -> {
                        if (err != null) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Account Creation Failed: " + err.getMessage());
                            alert.setTitle("Error");
                            alert.setHeaderText("Account Provisioning Error");
                            alert.showAndWait();
                        } else {
                            showAccountCreatedPopup(creds, parentContainer);
                        }
                    }));
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showAccountCreatedPopup(Map<String, String> creds, VBox parentContainer) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Account Created Successfully");
        dialog.setHeaderText("Account Created Successfully");

        ButtonType copyBtn = new ButtonType("Copy Credentials", ButtonBar.ButtonData.LEFT);
        ButtonType okBtn = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(copyBtn, okBtn);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: #FFFFFF;");

        String idStr = creds.get("id") != null ? creds.get("id") : "ID-001";
        String roleStr = creds.get("role") != null ? creds.get("role") : "Personnel";

        StringBuilder credsText = new StringBuilder();
        credsText.append("Name: ").append(creds.get("name")).append("\n");
        credsText.append("Email: ").append(creds.get("email")).append("\n");
        credsText.append("Account ID: ").append(idStr).append("\n");
        credsText.append("Role: ").append(roleStr).append("\n");
        if (creds.containsKey("assignedPort") && creds.get("assignedPort") != null) {
            credsText.append("Assigned Port: ").append(creds.get("assignedPort")).append("\n");
        }
        if (creds.containsKey("assignedShip") && creds.get("assignedShip") != null) {
            credsText.append("Assigned Ship: ").append(creds.get("assignedShip")).append("\n");
        }
        credsText.append("Temporary Password: ").append(creds.get("tempPassword"));

        Label infoLabel = new Label(credsText.toString());
        infoLabel.setStyle("-fx-font-family: monospace; -fx-font-size: 13px; -fx-text-fill: #0F172A; -fx-background-color: #F8FAFC; -fx-padding: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label warningLabel = new Label("Save these credentials securely. The temporary password will not be shown again.");
        warningLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #D97706; -fx-font-weight: bold;");

        content.getChildren().addAll(infoLabel, warningLabel);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == copyBtn) {
                javafx.scene.input.ClipboardContent clip = new javafx.scene.input.ClipboardContent();
                clip.putString(credsText.toString());
                javafx.scene.input.Clipboard.getSystemClipboard().setContent(clip);

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Credentials copied to clipboard!");
                alert.showAndWait();
            }
            refreshPersonnelSection();
            return btn;
        });

        dialog.showAndWait();
        refreshPersonnelSection();
    }

    // =====================================================
    // GET ROOT
    // =====================================================

    public VBox getRoot() {

        return root;
    }
}