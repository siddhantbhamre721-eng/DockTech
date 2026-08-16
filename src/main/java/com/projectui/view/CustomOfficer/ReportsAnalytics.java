package com.projectui.view.CustomOfficer;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class ReportsAnalytics {

    private final String TEXT = "#172033";
    private final String GRAY = "#7b8495";

    // =====================================================
    // ONLY CENTER CONTENT
    // =====================================================

    public VBox getContent() {

        VBox content =
            new VBox(18);

        content.setPadding(
            new Insets(
                25,
                35,
                35,
                35
            )
        );

        content.setStyle(
            "-fx-background-color:#f6f8fb;"
        );

        Label title =
            label(
                "Reports & Analytics",
                30,
                true,
                TEXT
            );

        Label subtitle =
            label(
                "View customs activity, shipment performance and operational insights.",
                12,
                false,
                GRAY
            );

        content.getChildren().addAll(

            title,
            subtitle,

            createFilterBar(),

            createCards(),

            createCharts(),

            createInsights(),

            createTable()
        );

        ScrollPane scroll =
            new ScrollPane(content);

        scroll.setFitToWidth(true);

        scroll.setStyle(
            "-fx-background-color:transparent;" +
            "-fx-border-color:transparent;"
        );

        VBox wrapper =
            new VBox(scroll);

        VBox.setVgrow(
            scroll,
            Priority.ALWAYS
        );

        return wrapper;
    }

    // =====================================================
    // FILTER
    // =====================================================

    private HBox createFilterBar() {

        HBox bar =
            new HBox(10);

        bar.setAlignment(
            Pos.CENTER_LEFT
        );

        bar.getChildren().add(
            label(
                "Report Period",
                13,
                true,
                TEXT
            )
        );

        bar.getChildren().addAll(

            filterButton(
                "Today",
                false
            ),

            filterButton(
                "This Week",
                false
            ),

            filterButton(
                "This Month",
                true
            ),

            filterButton(
                "This Year",
                false
            )
        );

        Region gap =
            new Region();

        HBox.setHgrow(
            gap,
            Priority.ALWAYS
        );

        Button export =
            new Button(
                "⇩  Export Report"
            );

        export.setStyle(
            "-fx-background-color:#172b4d;" +
            "-fx-text-fill:white;" +
            "-fx-font-weight:bold;" +
            "-fx-padding:10 18;" +
            "-fx-background-radius:7;"
        );

        bar.getChildren().addAll(
            gap,
            export
        );

        return bar;
    }

    private Button filterButton(
        String text,
        boolean active
    ) {

        Button b =
            new Button(text);

        b.setStyle(

            active ?

            "-fx-background-color:#1769ff;" +
            "-fx-text-fill:white;" +
            "-fx-font-weight:bold;" +
            "-fx-padding:9 16;" +
            "-fx-background-radius:7;"

            :

            "-fx-background-color:white;" +
            "-fx-text-fill:#475467;" +
            "-fx-padding:9 16;" +
            "-fx-background-radius:7;" +
            "-fx-border-color:#dce1e8;" +
            "-fx-border-radius:7;"
        );

        return b;
    }

    // =====================================================
    // CARDS
    // =====================================================

    private HBox createCards() {

        HBox cards =
            new HBox(15);

        cards.getChildren().addAll(

            card(
                "TOTAL SHIPMENTS",
                "1,284",
                "+12.8%",
                "from last month",
                "#1769ff"
            ),

            card(
                "CLEARED",
                "1,096",
                "+9.4%",
                "successful clearance",
                "#16a34a"
            ),

            card(
                "PENDING",
                "126",
                "-4.2%",
                "awaiting inspection",
                "#f59e0b"
            ),

            card(
                "DELAYED",
                "62",
                "+2.1%",
                "requires attention",
                "#dc3545"
            )
        );

        return cards;
    }

    private VBox card(
        String title,
        String value,
        String change,
        String desc,
        String color
    ) {

        VBox card =
            new VBox(6);

        card.setPadding(
            new Insets(18)
        );

        card.setPrefHeight(120);

        card.setStyle(
            box()
        );

        card.getChildren().addAll(

            label(
                title,
                11,
                true,
                GRAY
            ),

            label(
                value,
                28,
                true,
                TEXT
            ),

            label(
                change +
                "   " +
                desc,
                12,
                true,
                color
            )
        );

        HBox.setHgrow(
            card,
            Priority.ALWAYS
        );

        return card;
    }

    // =====================================================
    // CHARTS
    // =====================================================

    private HBox createCharts() {

        CategoryAxis x =
            new CategoryAxis();

        NumberAxis y =
            new NumberAxis();

        x.setLabel("Month");
        y.setLabel("Shipments");

        BarChart<String,Number> bar =
            new BarChart<>(x,y);

        bar.setTitle(
            "Monthly Customs Activity"
        );

        bar.setLegendVisible(false);
        bar.setAnimated(false);
        bar.setPrefHeight(280);

        XYChart.Series<String,Number> series =
            new XYChart.Series<>();

        String[] months = {
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug"
        };

        int[] values = {
            820,
            910,
            980,
            1050,
            1140,
            1284
        };

        for(int i = 0; i < months.length; i++) {

            series.getData().add(
                new XYChart.Data<>(
                    months[i],
                    values[i]
                )
            );
        }

        bar.getData().add(series);

        CategoryAxis lx =
            new CategoryAxis();

        NumberAxis ly =
            new NumberAxis();

        lx.setLabel("Week");
        ly.setLabel("Clearance Rate %");

        LineChart<String,Number> line =
            new LineChart<>(lx,ly);

        line.setTitle(
            "Clearance Performance"
        );

        line.setLegendVisible(false);
        line.setAnimated(false);
        line.setPrefHeight(280);

        XYChart.Series<String,Number> lineSeries =
            new XYChart.Series<>();

        String[] weeks = {
            "W1",
            "W2",
            "W3",
            "W4",
            "W5"
        };

        int[] rates = {
            78,
            82,
            86,
            91,
            94
        };

        for(int i = 0; i < weeks.length; i++) {

            lineSeries.getData().add(
                new XYChart.Data<>(
                    weeks[i],
                    rates[i]
                )
            );
        }

        line.getData().add(
            lineSeries
        );

        VBox left =
            chartBox(
                bar,
                "Customs processing volume over the selected period."
            );

        VBox right =
            chartBox(
                line,
                "Average clearance efficiency has improved this month."
            );

        HBox charts =
            new HBox(
                15,
                left,
                right
            );

        HBox.setHgrow(
            left,
            Priority.ALWAYS
        );

        HBox.setHgrow(
            right,
            Priority.ALWAYS
        );

        return charts;
    }

    private VBox chartBox(
        Chart chart,
        String description
    ) {

        VBox box =
            new VBox(8);

        box.setPadding(
            new Insets(15)
        );

        box.setStyle(
            box()
        );

        box.getChildren().addAll(

            chart,

            label(
                description,
                11,
                false,
                GRAY
            )
        );

        return box;
    }

    // =====================================================
    // INSIGHTS
    // =====================================================

    private HBox createInsights() {

        VBox insights =
            new VBox(14);

        insights.setPadding(
            new Insets(20)
        );

        insights.setStyle(
            box()
        );

        insights.getChildren().addAll(

            label(
                "Operational Insights",
                18,
                true,
                TEXT
            ),

            insight(
                "↗️",
                "Clearance Efficiency",
                "94.2%",
                "↑ 5.6% compared to previous month"
            ),

            insight(
                "◉",
                "Average Processing Time",
                "3.8 hrs",
                "↓ 24 minutes faster than last month"
            ),

            insight(
                "⚠",
                "Inspection Rate",
                "8.7%",
                "126 shipments currently pending"
            )
        );

        VBox status =
            new VBox(12);

        status.setPadding(
            new Insets(20)
        );

        status.setStyle(
            box()
        );

        status.getChildren().add(
            label(
                "Shipment Status",
                18,
                true,
                TEXT
            )
        );

        addProgress(
            status,
            "Cleared",
            85
        );

        addProgress(
            status,
            "Under Inspection",
            10
        );

        addProgress(
            status,
            "Pending Documents",
            5
        );

        HBox.setHgrow(
            insights,
            Priority.ALWAYS
        );

        HBox.setHgrow(
            status,
            Priority.ALWAYS
        );

        return new HBox(
            15,
            insights,
            status
        );
    }

    private Label insight(
        String icon,
        String title,
        String value,
        String desc
    ) {

        Label l =
            new Label(
                icon +
                "   " +
                title +
                "\n\n" +
                value +
                "    " +
                desc
            );

        l.setWrapText(true);

        l.setFont(
            Font.font(13)
        );

        l.setTextFill(
            Color.web("#475467")
        );

        return l;
    }

    private void addProgress(
        VBox parent,
        String name,
        double value
    ) {

        Label l =
            label(
                name +
                "                         " +
                (int)value +
                "%",
                12,
                true,
                "#475467"
            );

        ProgressBar progress =
            new ProgressBar(
                value / 100
            );

        progress.setMaxWidth(
            Double.MAX_VALUE
        );

        parent.getChildren().addAll(
            l,
            progress
        );
    }

    // =====================================================
    // TABLE
    // =====================================================

    private VBox createTable() {

        VBox box =
            new VBox(12);

        box.setPadding(
            new Insets(20)
        );

        box.setStyle(
            box()
        );

        Label title =
            label(
                "Recent Customs Activity",
                18,
                true,
                TEXT
            );

        TableView<Activity> table =
            new TableView<>();

        table.setPrefHeight(220);

        TableColumn<Activity,String> shipment =
            column(
                "Shipment ID",
                a -> a.shipment
            );

        TableColumn<Activity,String> port =
            column(
                "Port",
                a -> a.port
            );

        TableColumn<Activity,String> activity =
            column(
                "Activity",
                a -> a.activity
            );

        TableColumn<Activity,String> status =
            column(
                "Status",
                a -> a.status
            );

        TableColumn<Activity,String> time =
            column(
                "Time",
                a -> a.time
            );

        table.getColumns().addAll(
            shipment,
            port,
            activity,
            status,
            time
        );

        table.getItems().addAll(

            new Activity(
                "SHP-10482",
                "Mumbai",
                "Customs Clearance",
                "Cleared",
                "10:42 PM"
            ),

            new Activity(
                "SHP-10479",
                "JNPT",
                "Document Verification",
                "Pending",
                "10:25 PM"
            ),

            new Activity(
                "SHP-10471",
                "Chennai",
                "Inspection Completed",
                "Cleared",
                "09:58 PM"
            ),

            new Activity(
                "SHP-10468",
                "Kandla",
                "Cargo Inspection",
                "In Progress",
                "09:41 PM"
            ),

            new Activity(
                "SHP-10452",
                "Mumbai",
                "Duty Assessment",
                "Completed",
                "09:18 PM"
            )
        );

        box.getChildren().addAll(
            title,
            table
        );

        return box;
    }

    private TableColumn<Activity,String> column(
        String name,
        java.util.function.Function<
            Activity,
            SimpleStringProperty
        > getter
    ) {

        TableColumn<Activity,String> column =
            new TableColumn<>(
                name
            );

        column.setCellValueFactory(
            data ->
                getter.apply(
                    data.getValue()
                )
        );

        return column;
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private Label label(
        String text,
        double size,
        boolean bold,
        String color
    ) {

        Label l =
            new Label(text);

        l.setFont(
            Font.font(
                "Arial",
                bold
                    ? FontWeight.BOLD
                    : FontWeight.NORMAL,
                size
            )
        );

        l.setTextFill(
            Color.web(color)
        );

        return l;
    }

    private String box() {

        return
            "-fx-background-color:white;" +
            "-fx-background-radius:12;" +
            "-fx-border-color:#e5e9f0;" +
            "-fx-border-radius:12;";
    }

    // =====================================================
    // TABLE MODEL
    // =====================================================

    public static class Activity {

        SimpleStringProperty shipment;
        SimpleStringProperty port;
        SimpleStringProperty activity;
        SimpleStringProperty status;
        SimpleStringProperty time;

        public Activity(
            String shipment,
            String port,
            String activity,
            String status,
            String time
        ) {

            this.shipment =
                new SimpleStringProperty(
                    shipment
                );

            this.port =
                new SimpleStringProperty(
                    port
                );

            this.activity =
                new SimpleStringProperty(
                    activity
                );

            this.status =
                new SimpleStringProperty(
                    status
                );

            this.time =
                new SimpleStringProperty(
                    time
                );
        }
    }
}
