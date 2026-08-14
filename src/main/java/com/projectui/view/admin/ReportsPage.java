package com.projectui.view.admin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

public class ReportsPage extends AdminTheme {

    private final AdminDataStore store = AdminDataStore.getInstance();
    private final GridPane reportTable = new GridPane();
    private final List<Button> periodButtons = new ArrayList<>();

    private ComboBox<String> reportType;
    private ComboBox<String> statusFilter;
    private TextField search;
    private String selectedPeriod = "This Week";
    private Label toastMessage;
    private HBox toastBanner;

    // KPI Values
    private static final String CARGO_HANDLED_VAL = "9,690 MT";
    private static final String VESSEL_ARRIVALS_VAL = "66";
    private static final String VESSEL_DEPARTURES_VAL = "56";
    private static final String AVG_WAITING_VAL = "45 min";
    private static final String PENDING_INSPECTIONS_VAL = "14";

    public Parent getRoot() {
        Button generateBtn = primaryButton("+  Generate Report");
        generateBtn.setStyle(
                "-fx-background-color:" + BLUE + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-size:14;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:10 22;" +
                "-fx-background-radius:8;" +
                "-fx-cursor:hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(11,99,255,0.25), 8, 0, 0, 2);"
        );
        generateBtn.setOnMouseEntered(e -> generateBtn.setStyle(
                "-fx-background-color:#0952d6;" +
                "-fx-text-fill:white;" +
                "-fx-font-size:14;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:10 22;" +
                "-fx-background-radius:8;" +
                "-fx-cursor:hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(11,99,255,0.35), 10, 0, 0, 3);"
        ));
        generateBtn.setOnMouseExited(e -> generateBtn.setStyle(
                "-fx-background-color:" + BLUE + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-size:14;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:10 22;" +
                "-fx-background-radius:8;" +
                "-fx-cursor:hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(11,99,255,0.25), 8, 0, 0, 2);"
        ));
        generateBtn.setOnAction(event -> showGenerateReportDialog(generateBtn.getScene() == null ? null : generateBtn.getScene().getWindow()));

        VBox page = pageContent();
        page.setSpacing(18);
        page.setPadding(new Insets(18, 22, 28, 22));

        // Toast status banner for export/generation feedback
        toastBanner = createToastBanner();

        // 1. Header Section
        HBox header = pageHeader(
                "Reports & Analytics",
                "Port operations analytics center for performance, cargo throughput, vessel traffic and operational compliance.",
                generateBtn
        );

        // 2. KPI Cards Row (5 Cards)
        HBox kpiRow = createKpiRow();

        // 3. Analytics Charts Grid
        // Row 1: Vessel Activity (55%) + Cargo Performance (45%)
        VBox vesselCard = vesselActivityCard();
        VBox cargoCard = cargoPerformanceCard();
        HBox chartsRow1 = new HBox(16, vesselCard, cargoCard);
        vesselCard.prefWidthProperty().bind(chartsRow1.widthProperty().multiply(0.55));
        cargoCard.prefWidthProperty().bind(chartsRow1.widthProperty().multiply(0.45));
        HBox.setHgrow(vesselCard, Priority.ALWAYS);
        HBox.setHgrow(cargoCard, Priority.ALWAYS);

        // Row 2: Port Performance Comparison (55%) + Operational Insights (45%)
        VBox portCard = portPerformanceCard();
        VBox insightsCard = operationalInsightsCard();
        HBox chartsRow2 = new HBox(16, portCard, insightsCard);
        portCard.prefWidthProperty().bind(chartsRow2.widthProperty().multiply(0.55));
        insightsCard.prefWidthProperty().bind(chartsRow2.widthProperty().multiply(0.45));
        HBox.setHgrow(portCard, Priority.ALWAYS);
        HBox.setHgrow(insightsCard, Priority.ALWAYS);

        // 4. Report Controls & Filter Card with Reports Table
        VBox reportsSection = createReportsSection();

        page.getChildren().addAll(
                header,
                toastBanner,
                kpiRow,
                chartsRow1,
                chartsRow2,
                reportsSection
        );

        // Ensure default data is loaded
        store.generateReports(selectedPeriod, "All Status");
        refreshReportTable();

        // Smooth ScrollPane wrapper to ensure vertical scrolling without clipping
        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background:transparent; -fx-background-color:transparent; -fx-padding:0;");

        return scrollPane;
    }

    // ==========================================
    // TOAST NOTIFICATION BANNER
    // ==========================================
    private HBox createToastBanner() {
        Label icon = new Label("ℹ");
        icon.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:14; -fx-font-weight:bold;");

        toastMessage = new Label("Showing analytics data for " + selectedPeriod + " across all operational ports.");
        toastMessage.setStyle("-fx-text-fill:#334155; -fx-font-size:12; -fx-font-weight:bold;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label close = new Label("✕");
        close.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12; -fx-cursor:hand;");
        close.setOnMouseClicked(e -> toastBanner.setVisible(false));

        HBox toast = new HBox(10, icon, toastMessage, space, close);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setPadding(new Insets(10, 16, 10, 16));
        toast.setStyle(
                "-fx-background-color:#eff6ff;" +
                "-fx-border-color:#bfdbfe;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;"
        );
        return toast;
    }

    private void showToast(String msg, boolean isSuccess) {
        if (toastMessage != null && toastBanner != null) {
            toastMessage.setText(msg);
            toastBanner.setStyle(
                    "-fx-background-color:" + (isSuccess ? "#ecfdf5" : "#eff6ff") + ";" +
                    "-fx-border-color:" + (isSuccess ? "#a7f3d0" : "#bfdbfe") + ";" +
                    "-fx-border-radius:8;" +
                    "-fx-background-radius:8;"
            );
            toastBanner.setVisible(true);
        }
    }

    // ==========================================
    // KPI CARDS ROW (5 CARDS)
    // ==========================================
    private HBox createKpiRow() {
        HBox row = new HBox(12,
                kpiCard("▥", "Cargo Handled", CARGO_HANDLED_VAL, "Total Volume", "▲ +12.4% vs last week", "#0b63ff", "#e0e7ff", "#16a34a", "#ecfdf5"),
                kpiCard("⚓", "Vessel Arrivals", VESSEL_ARRIVALS_VAL, "Incoming Vessels", "▲ +8.2% weekly", "#0ea5e9", "#e0f2fe", "#16a34a", "#ecfdf5"),
                kpiCard("↗", "Vessel Departures", VESSEL_DEPARTURES_VAL, "Dispatched Ships", "▲ +5.1% on time", "#6366f1", "#eef2ff", "#16a34a", "#ecfdf5"),
                kpiCard("⏱", "Average Waiting", AVG_WAITING_VAL, "Turnaround Delay", "▼ -8 min faster", "#d97706", "#fef3c7", "#16a34a", "#ecfdf5"),
                kpiCard("⚠", "Pending Inspections", PENDING_INSPECTIONS_VAL, "Customs & Safety", "● 4 High Priority", "#ef4444", "#fee2e2", "#ea580c", "#fff7ed")
        );
        for (Node node : row.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
            if (node instanceof VBox) {
                ((VBox) node).setMaxWidth(Double.MAX_VALUE);
            }
        }
        return row;
    }

    private VBox kpiCard(String iconStr, String title, String value, String subtitle, String badgeText,
                         String iconColor, String iconBg, String badgeColor, String badgeBg) {
        // Icon badge
        Label icon = new Label(iconStr);
        icon.setStyle(
                "-fx-text-fill:" + iconColor + ";" +
                "-fx-font-size:16;" +
                "-fx-font-weight:bold;" +
                "-fx-alignment:center;" +
                "-fx-min-width:32;" +
                "-fx-min-height:32;" +
                "-fx-pref-width:32;" +
                "-fx-pref-height:32;" +
                "-fx-background-color:" + iconBg + ";" +
                "-fx-background-radius:8;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:14; -fx-font-weight:bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(8, icon, titleLabel, spacer);
        top.setAlignment(Pos.CENTER_LEFT);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:28; -fx-font-weight:800;");

        Label subLabel = new Label(subtitle);
        subLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12;");

        Label badge = new Label(badgeText);
        badge.setStyle(
                "-fx-text-fill:" + badgeColor + ";" +
                "-fx-background-color:" + badgeBg + ";" +
                "-fx-background-radius:6;" +
                "-fx-padding:3 8 3 8;" +
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;"
        );

        HBox bottom = new HBox(8, subLabel, spacer, badge);
        bottom.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(8, top, valueLabel, bottom);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 10, 0, 0, 2);"
        );
        return card;
    }

    // ==========================================
    // CHART 1: VESSEL ACTIVITY (MON - SUN)
    // ==========================================
    private VBox vesselActivityCard() {
        Label title = new Label("Vessel Activity");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Weekly vessel arrivals and departures (Monday – Sunday)");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        // Legend with total counters
        HBox legendArrivals = chartLegendItem("Arrivals (66)", "#0b63ff");
        HBox legendDepartures = chartLegendItem("Departures (56)", "#38bdf8");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox top = new HBox(12, titleBox, space, legendArrivals, legendDepartures);
        top.setAlignment(Pos.CENTER_LEFT);

        // Mon - Sun Data: [Arrivals, Departures]
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        int[] arrivals = {8, 11, 9, 12, 14, 7, 5};
        int[] departures = {7, 9, 8, 10, 11, 6, 5};
        int maxVal = 16; // Scale reference

        HBox barChartBox = new HBox(14);
        barChartBox.setAlignment(Pos.BOTTOM_CENTER);
        barChartBox.setPadding(new Insets(18, 10, 6, 10));
        barChartBox.setPrefHeight(190);
        barChartBox.setStyle(
                "-fx-background-color:#fafcff;" +
                "-fx-border-color:#f1f5f9;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;"
        );

        for (int i = 0; i < days.length; i++) {
            VBox dayGroup = createDualBarGroup(days[i], arrivals[i], departures[i], maxVal);
            HBox.setHgrow(dayGroup, Priority.ALWAYS);
            barChartBox.getChildren().add(dayGroup);
        }

        // Summary footer metric line
        HBox footer = new HBox(16,
                statSummaryBadge("Peak Day", "Friday (25 Vessels)", "#0b63ff"),
                statSummaryBadge("Avg Turnaround", "1.8 Days / Vessel", "#10b981"),
                statSummaryBadge("On-Time Rate", "94.2%", "#6366f1")
        );
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(4, 0, 0, 0));

        VBox card = new VBox(12, top, barChartBox, footer);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 10, 0, 0, 2);"
        );
        return card;
    }

    private VBox createDualBarGroup(String day, int arr, int dep, int maxVal) {
        double maxBarHeight = 110;
        double arrHeight = (arr / (double) maxVal) * maxBarHeight;
        double depHeight = (dep / (double) maxVal) * maxBarHeight;

        // Arrivals bar (Blue)
        Region arrBar = new Region();
        arrBar.setPrefWidth(16);
        arrBar.setPrefHeight(arrHeight);
        arrBar.setStyle(
                "-fx-background-color: linear-gradient(to top, #0b63ff, #3b82f6);" +
                "-fx-background-radius: 4 4 0 0;"
        );
        Tooltip.install(arrBar, new Tooltip(day + " Arrivals: " + arr + " vessels"));

        // Departures bar (Cyan / Sky Blue)
        Region depBar = new Region();
        depBar.setPrefWidth(16);
        depBar.setPrefHeight(depHeight);
        depBar.setStyle(
                "-fx-background-color: linear-gradient(to top, #0ea5e9, #38bdf8);" +
                "-fx-background-radius: 4 4 0 0;"
        );
        Tooltip.install(depBar, new Tooltip(day + " Departures: " + dep + " vessels"));

        HBox bars = new HBox(4, arrBar, depBar);
        bars.setAlignment(Pos.BOTTOM_CENTER);

        // Day label
        Label dayLabel = new Label(day);
        dayLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12; -fx-font-weight:bold;");

        // Small value total tag
        Label totalLabel = new Label(String.valueOf(arr + dep));
        totalLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:11;");

        VBox group = new VBox(6, totalLabel, bars, dayLabel);
        group.setAlignment(Pos.BOTTOM_CENTER);
        return group;
    }

    // ==========================================
    // CHART 2: CARGO PERFORMANCE (MULTI-SERIES)
    // ==========================================
    private VBox cargoPerformanceCard() {
        Label title = new Label("Cargo Performance");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Daily cargo volume (Containers, Bulk Cargo & Total)");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        HBox legTotal = chartLegendItem("Total (9,690 MT)", "#0b63ff");
        HBox legContainers = chartLegendItem("Containers (5,240 MT)", "#10b981");
        HBox legBulk = chartLegendItem("Bulk (4,450 MT)", "#0ea5e9");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox top = new HBox(10, titleBox, space, legTotal, legContainers, legBulk);
        top.setAlignment(Pos.CENTER_LEFT);

        // Modern Canvas-drawn area & line chart
        Canvas canvas = drawCargoAreaChart(460, 190);
        StackPane chartContainer = new StackPane(canvas);
        chartContainer.setAlignment(Pos.CENTER);
        chartContainer.setStyle(
                "-fx-background-color:#fafcff;" +
                "-fx-border-color:#f1f5f9;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;"
        );

        // Responsive redraw on width changes
        chartContainer.widthProperty().addListener((obs, oldW, newW) -> {
            if (newW.doubleValue() > 100) {
                canvas.setWidth(newW.doubleValue() - 20);
                redrawCargoChart(canvas);
            }
        });

        // Summary breakdown strip
        HBox metricsStrip = new HBox(14,
                statSummaryBadge("Containers", "5,240 MT (54%)", "#10b981"),
                statSummaryBadge("Bulk Cargo", "4,450 MT (46%)", "#0ea5e9"),
                statSummaryBadge("Avg Daily", "1,384 MT/day", "#0b63ff")
        );
        metricsStrip.setAlignment(Pos.CENTER_LEFT);
        metricsStrip.setPadding(new Insets(4, 0, 0, 0));

        VBox card = new VBox(12, top, chartContainer, metricsStrip);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 10, 0, 0, 2);"
        );
        return card;
    }

    private Canvas drawCargoAreaChart(double width, double height) {
        Canvas canvas = new Canvas(width, height);
        redrawCargoChart(canvas);
        return canvas;
    }

    private void redrawCargoChart(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        gc.clearRect(0, 0, w, h);

        double padLeft = 42;
        double padRight = 20;
        double padTop = 20;
        double padBottom = 32;

        double plotW = w - padLeft - padRight;
        double plotH = h - padTop - padBottom;

        // Daily Data Points (Mon - Sun): Total, Containers, Bulk
        double[] totalData = {1150, 1480, 1320, 1620, 1780, 1260, 1080}; // Sum = 9,690 MT
        double[] containerData = {620, 800, 710, 880, 960, 690, 580};      // Sum = 5,240 MT
        double[] bulkData = {530, 680, 610, 740, 820, 570, 500};           // Sum = 4,450 MT
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        double maxScale = 2000;

        // Draw horizontal grid lines & Y-axis labels
        gc.setLineWidth(1);
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        int gridSteps = 4;
        for (int i = 0; i <= gridSteps; i++) {
            double yVal = (maxScale / gridSteps) * i;
            double y = padTop + plotH - (yVal / maxScale) * plotH;

            gc.setStroke(Color.web("#e2e8f0", 0.7));
            gc.strokeLine(padLeft, y, w - padRight, y);

            gc.setFill(Color.web("#94a3b8"));
            gc.fillText(String.format("%.0f", yVal), 8, y + 4);
        }

        // Compute coordinate points
        int n = days.length;
        double[] xCoords = new double[n];
        for (int i = 0; i < n; i++) {
            xCoords[i] = padLeft + (i / (double) (n - 1)) * plotW;
        }

        // 1. Draw Total Cargo Area Fill (Soft Blue Gradient)
        gc.save();
        LinearGradient blueGrad = new LinearGradient(
                0, padTop, 0, padTop + plotH, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0b63ff", 0.22)),
                new Stop(1, Color.web("#0b63ff", 0.01))
        );
        gc.setFill(blueGrad);
        gc.beginPath();
        gc.moveTo(xCoords[0], padTop + plotH);
        for (int i = 0; i < n; i++) {
            double y = padTop + plotH - (totalData[i] / maxScale) * plotH;
            gc.lineTo(xCoords[i], y);
        }
        gc.lineTo(xCoords[n - 1], padTop + plotH);
        gc.closePath();
        gc.fill();
        gc.restore();

        // 2. Draw Series Lines
        drawChartLine(gc, xCoords, totalData, maxScale, padTop, plotH, Color.web("#0b63ff"), 2.4);
        drawChartLine(gc, xCoords, containerData, maxScale, padTop, plotH, Color.web("#10b981"), 2.0);
        drawChartLine(gc, xCoords, bulkData, maxScale, padTop, plotH, Color.web("#0ea5e9"), 2.0);

        // 3. Draw X-axis day labels
        gc.setFill(Color.web("#64748b"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        for (int i = 0; i < n; i++) {
            gc.fillText(days[i], xCoords[i] - 10, h - 10);
        }
    }

    private void drawChartLine(GraphicsContext gc, double[] xCoords, double[] data, double maxScale,
                               double padTop, double plotH, Color strokeColor, double strokeWidth) {
        int n = xCoords.length;
        gc.setStroke(strokeColor);
        gc.setLineWidth(strokeWidth);

        gc.beginPath();
        for (int i = 0; i < n; i++) {
            double y = padTop + plotH - (data[i] / maxScale) * plotH;
            if (i == 0) gc.moveTo(xCoords[i], y);
            else gc.lineTo(xCoords[i], y);
        }
        gc.stroke();

        // Data point circles
        for (int i = 0; i < n; i++) {
            double y = padTop + plotH - (data[i] / maxScale) * plotH;
            gc.setFill(Color.WHITE);
            gc.fillOval(xCoords[i] - 4, y - 4, 8, 8);
            gc.setStroke(strokeColor);
            gc.setLineWidth(2);
            gc.strokeOval(xCoords[i] - 4, y - 4, 8, 8);
        }
    }

    // ==========================================
    // CHART 3: PORT PERFORMANCE COMPARISON
    // ==========================================
    private VBox portPerformanceCard() {
        Label title = new Label("Port Performance");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Operational comparison across major terminals (JNPT, Mumbai, Chennai, Kolkata)");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        Label badge = new Label("5 Ports Active");
        badge.setStyle(
                "-fx-background-color:#ecfdf5;" +
                "-fx-text-fill:#16a34a;" +
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:4 10;" +
                "-fx-background-radius:6;"
        );
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox top = new HBox(10, titleBox, space, badge);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox rows = new VBox(10);
        rows.getChildren().addAll(
                portComparisonRow("JNPT (Navi Mumbai)", "Maharashtra", 24, 3450, 88, "Optimal", "#16a34a", "#0b63ff"),
                portComparisonRow("Port of Mumbai", "Maharashtra", 18, 2800, 82, "Operational", "#16a34a", "#0ea5e9"),
                portComparisonRow("Port of Chennai", "Tamil Nadu", 14, 1950, 76, "Operational", "#16a34a", "#6366f1"),
                portComparisonRow("Port of Kolkata", "West Bengal", 10, 1490, 91, "Congested", "#ef4444", "#ef4444"),
                portComparisonRow("Port of Vizag", "Andhra Pradesh", 8, 980, 58, "Maintenance", "#d97706", "#f59e0b")
        );

        VBox card = new VBox(14, top, rows);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 10, 0, 0, 2);"
        );
        return card;
    }

    private VBox portComparisonRow(String portName, String region, int vessels, int cargoTons, int occupancyPercent,
                                   String status, String statusColor, String barColor) {
        Label name = new Label(portName);
        name.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14; -fx-font-weight:bold;");
        Label reg = new Label(" • " + region);
        reg.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12;");

        HBox nameBox = new HBox(2, name, reg);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        Label metrics = new Label(vessels + " vessels  |  " + String.format("%,d MT", cargoTons));
        metrics.setStyle("-fx-text-fill:#64748b; -fx-font-size:13; -fx-font-weight:bold;");

        Label statusTag = new Label(status + " (" + occupancyPercent + "%)");
        statusTag.setStyle(
                "-fx-text-fill:" + statusColor + ";" +
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;"
        );

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox rowHeader = new HBox(8, nameBox, space, metrics, statusTag);
        rowHeader.setAlignment(Pos.CENTER_LEFT);

        // Progress bar for berth occupancy
        StackPane barContainer = new StackPane();
        barContainer.setAlignment(Pos.CENTER_LEFT);
        barContainer.setPrefHeight(7);
        barContainer.setStyle("-fx-background-color:#f1f5f9; -fx-background-radius:4;");

        Rectangle fill = new Rectangle();
        fill.setHeight(7);
        fill.setArcWidth(4);
        fill.setArcHeight(4);
        fill.setStyle("-fx-fill:" + barColor + ";");
        fill.widthProperty().bind(barContainer.widthProperty().multiply(occupancyPercent / 100.0));

        barContainer.getChildren().add(fill);

        VBox row = new VBox(5, rowHeader, barContainer);
        row.setPadding(new Insets(2, 0, 2, 0));
        return row;
    }

    // ==========================================
    // CARD 4: OPERATIONAL INSIGHTS
    // ==========================================
    private VBox operationalInsightsCard() {
        Label title = new Label("Operational Insights");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Real-time operational alerts, throughput peaks, and berth utilization");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        Label liveTag = new Label("● Live Insights");
        liveTag.setStyle("-fx-text-fill:#0b63ff; -fx-font-size:13; -fx-font-weight:bold;");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox top = new HBox(10, titleBox, space, liveTag);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox insightRows = new VBox(8);
        insightRows.getChildren().addAll(
                insightRow("Highest Vessel Activity", "JNPT Port", "24 vessels active · 36% traffic share", "#16a34a", "#ecfdf5"),
                insightRow("Highest Cargo Volume", "Port of Mumbai", "4,120 MT handled · Containers & Mixed", "#0b63ff", "#eff6ff"),
                insightRow("Congestion Watch", "Port of Kolkata", "91% Berth Occupancy · Delay risk: +25 min", "#ef4444", "#fee2e2"),
                insightRow("Pending Inspections", "14 Pending", "4 Customs Clearance, 6 Safety Audits, 4 Quarantine", "#f59e0b", "#fffbeb"),
                insightRow("Berth/Capacity Status", "4/4 Operational", "79% Avg Utilization · Normal Operations", "#0ea5e9", "#f0fdfa")
        );

        VBox card = new VBox(14, top, insightRows);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 10, 0, 0, 2);"
        );
        return card;
    }

    private HBox insightRow(String title, String highlight, String details, String accentColor, String bgLight) {
        Circle dot = new Circle(4);
        dot.setStyle("-fx-fill:" + accentColor + ";");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14; -fx-font-weight:bold;");

        Label badge = new Label(highlight);
        badge.setStyle(
                "-fx-text-fill:" + accentColor + ";" +
                "-fx-background-color:" + bgLight + ";" +
                "-fx-background-radius:5;" +
                "-fx-padding:3 8 3 8;" +
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;"
        );

        HBox head = new HBox(6, dot, titleLbl, badge);
        head.setAlignment(Pos.CENTER_LEFT);

        Label desc = new Label(details);
        desc.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");

        VBox content = new VBox(3, head, desc);
        HBox.setHgrow(content, Priority.ALWAYS);

        HBox row = new HBox(8, content);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setStyle(
                "-fx-background-color:#f8fafc;" +
                "-fx-border-color:#f1f5f9;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;"
        );
        return row;
    }

    // ==========================================
    // REPORT CONTROLS & TABLE SECTION
    // ==========================================
    private VBox createReportsSection() {
        // Filter Buttons
        HBox periodFilters = new HBox(8,
                filterPeriodButton("Today", "Today".equals(selectedPeriod)),
                filterPeriodButton("This Week", "This Week".equals(selectedPeriod)),
                filterPeriodButton("This Month", "This Month".equals(selectedPeriod)),
                filterPeriodButton("Custom Range", "Custom Range".equals(selectedPeriod))
        );
        periodFilters.setAlignment(Pos.CENTER_LEFT);

        // Search Input
        search = new TextField();
        search.setPromptText("⌕  Search report by title, author, or keyword...");
        search.setPrefWidth(260);
        search.setStyle(
                "-fx-background-color:#f8fafc;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;" +
                "-fx-padding:8 12 8 12;" +
                "-fx-font-size:14;"
        );
        search.textProperty().addListener((obs, oldValue, newValue) -> refreshReportTable());

        // Dropdowns
        reportType = combo("Report Type", "All Report Types", "Port Operations", "Vessel Traffic", "Cargo Throughput", "Berth Utilization", "Customs & Clearance", "Safety & Compliance");
        statusFilter = combo("Status", "All Status", "Ready", "Processing", "Scheduled");
        reportType.setPrefWidth(180);
        statusFilter.setPrefWidth(140);

        reportType.valueProperty().addListener((obs, oldValue, newValue) -> refreshReportTable());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> refreshReportTable());

        // Buttons
        Button resetBtn = new Button("Reset");
        resetBtn.setStyle(
                "-fx-background-color:#f1f5f9;" +
                "-fx-text-fill:#475569;" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:8 14;" +
                "-fx-background-radius:8;" +
                "-fx-cursor:hand;"
        );
        resetBtn.setOnAction(e -> {
            if (search != null) search.clear();
            if (reportType != null) reportType.setValue("All Report Types");
            if (statusFilter != null) statusFilter.setValue("All Status");
            selectedPeriod = "This Week";
            updatePeriodButtonStyles();
            store.generateReports(selectedPeriod, "All Status");
            refreshReportTable();
            showToast("Filters reset to default view.", true);
        });

        Button exportAllBtn = new Button("↓ Export All");
        exportAllBtn.setStyle(
                "-fx-background-color:#eff6ff;" +
                "-fx-text-fill:" + BLUE + ";" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-border-color:#bfdbfe;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;" +
                "-fx-padding:8 14;" +
                "-fx-cursor:hand;"
        );
        exportAllBtn.setOnAction(e -> {
            if (!store.reports.isEmpty()) {
                try {
                    store.exportReport(store.reports.get(0));
                    showToast("Exported all " + store.reports.size() + " active reports to " + store.lastExportPath, true);
                } catch (Exception ex) {
                    showToast("Error exporting reports: " + ex.getMessage(), false);
                }
            }
        });

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox controlsRow = new HBox(10, periodFilters, space, search, reportType, statusFilter, resetBtn, exportAllBtn);
        controlsRow.setAlignment(Pos.CENTER_LEFT);

        // Section Title & Subtitle
        Label title = new Label("Generated Operations Reports");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label sub = new Label("Detailed audit records, manifest logs, and operational summaries available for review and export");
        sub.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox headCopy = new VBox(2, title, sub);

        // Reports Table Container — Spans 100% Full Width
        reportTable.setHgap(0);
        reportTable.setVgap(0);
        reportTable.setMaxWidth(Double.MAX_VALUE);
        reportTable.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;"
        );

        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(30);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(13);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(18);
        ColumnConstraints c4 = new ColumnConstraints(); c4.setPercentWidth(12);
        ColumnConstraints c5 = new ColumnConstraints(); c5.setPercentWidth(14);
        ColumnConstraints c6 = new ColumnConstraints(); c6.setPercentWidth(13);
        reportTable.getColumnConstraints().setAll(c1, c2, c3, c4, c5, c6);

        VBox card = new VBox(14, headCopy, controlsRow, reportTable);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPadding(new Insets(18));
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 10, 0, 0, 2);"
        );
        return card;
    }

    private Button filterPeriodButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPrefHeight(34);
        stylePeriodButton(btn, active);
        btn.setOnAction(event -> {
            if ("Custom Range".equals(text)) {
                showCustomRangeDialog(btn.getScene() == null ? null : btn.getScene().getWindow());
            } else {
                selectedPeriod = text;
                updatePeriodButtonStyles();
                store.generateReports(selectedPeriod, statusFilter == null ? "All Status" : statusFilter.getValue());
                refreshReportTable();
                showToast("Reports filtered for period: " + selectedPeriod, true);
            }
        });
        periodButtons.add(btn);
        return btn;
    }

    private void stylePeriodButton(Button btn, boolean active) {
        if (active) {
            btn.setStyle(
                    "-fx-background-color:" + BLUE + ";" +
                    "-fx-text-fill:white;" +
                    "-fx-background-radius:8;" +
                    "-fx-font-size:13;" +
                    "-fx-font-weight:bold;" +
                    "-fx-padding:0 16 0 16;" +
                    "-fx-cursor:hand;"
            );
        } else {
            btn.setStyle(
                    "-fx-background-color:#f1f5f9;" +
                    "-fx-text-fill:#475569;" +
                    "-fx-background-radius:8;" +
                    "-fx-font-size:13;" +
                    "-fx-font-weight:bold;" +
                    "-fx-padding:0 16 0 16;" +
                    "-fx-cursor:hand;"
            );
        }
    }

    private void updatePeriodButtonStyles() {
        for (Button btn : periodButtons) {
            stylePeriodButton(btn, btn.getText().equals(selectedPeriod));
        }
    }

    // ==========================================
    // REFRESH REPORTS TABLE (6 COLUMNS - FULL WIDTH)
    // ==========================================
    private void refreshReportTable() {
        reportTable.getChildren().clear();

        // 6 Columns: Report Name, Period, Generated By, Status, Generated Date, Action
        String[] headers = {"Report Name", "Period", "Generated By", "Status", "Generated Date", "Action"};

        for (int col = 0; col < headers.length; col++) {
            Label headCell = customTableCell(headers[col], true);
            reportTable.add(headCell, col, 0);
        }

        List<AdminDataStore.ReportRow> visibleReports = new ArrayList<>();
        String query = search == null || search.getText() == null ? "" : search.getText().trim().toLowerCase();
        String selectedType = reportType == null || reportType.getValue() == null ? "All Report Types" : reportType.getValue();
        String selectedStat = statusFilter == null || statusFilter.getValue() == null ? "All Status" : statusFilter.getValue();

        for (AdminDataStore.ReportRow report : store.reports) {
            boolean matchesText = query.isEmpty()
                    || report.name.toLowerCase().contains(query)
                    || report.period.toLowerCase().contains(query)
                    || report.status.toLowerCase().contains(query)
                    || report.generatedBy.toLowerCase().contains(query);

            boolean matchesType = "All Report Types".equalsIgnoreCase(selectedType)
                    || report.name.toLowerCase().contains(selectedType.toLowerCase())
                    || (selectedType.contains("Port") && report.name.contains("Port"))
                    || (selectedType.contains("Vessel") && report.name.contains("Vessel"))
                    || (selectedType.contains("Cargo") && report.name.contains("Cargo"))
                    || (selectedType.contains("Berth") && report.name.contains("Berth"))
                    || (selectedType.contains("Customs") && report.name.contains("Customs"))
                    || (selectedType.contains("Safety") && report.name.contains("Safety"));

            boolean matchesStatus = "All Status".equalsIgnoreCase(selectedStat)
                    || report.status.equalsIgnoreCase(selectedStat);

            if (matchesText && matchesType && matchesStatus) {
                visibleReports.add(report);
            }
        }

        if (visibleReports.isEmpty()) {
            Label empty = new Label("No reports found matching the selected filter criteria.");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setMinHeight(60);
            empty.setAlignment(Pos.CENTER);
            empty.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:14; -fx-font-style:italic; -fx-background-color:white;");
            reportTable.add(empty, 0, 1, 6, 1);
            return;
        }

        for (int row = 0; row < visibleReports.size(); row++) {
            AdminDataStore.ReportRow report = visibleReports.get(row);
            boolean isAlt = row % 2 == 1;

            // 1. Report Name (with Icon)
            HBox nameCell = createReportNameCell(report.name, isAlt);
            // 2. Period
            Label periodCell = createTextCell(report.period, isAlt);
            // 3. Generated By
            Label authorCell = createTextCell(report.generatedBy, isAlt);
            // 4. Status Badge
            HBox statusCell = createStatusCell(report.status, isAlt);
            // 5. Generated Date
            Label dateCell = createTextCell(report.generatedDate, isAlt);
            // 6. Actions (View | Export)
            HBox actionCell = createActionCell(report, isAlt);

            reportTable.add(nameCell, 0, row + 1);
            reportTable.add(periodCell, 1, row + 1);
            reportTable.add(authorCell, 2, row + 1);
            reportTable.add(statusCell, 3, row + 1);
            reportTable.add(dateCell, 4, row + 1);
            reportTable.add(actionCell, 5, row + 1);
        }
    }

    private HBox createReportNameCell(String reportName, boolean isAlt) {
        Label icon = new Label("📄");
        icon.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:15;");

        Label name = new Label(reportName);
        name.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14; -fx-font-weight:bold;");

        HBox cell = new HBox(8, icon, name);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setMinHeight(52);
        cell.setPadding(new Insets(0, 14, 0, 14));
        cell.setStyle(
                "-fx-background-color:" + (isAlt ? "#fafcff" : "white") + ";" +
                "-fx-border-color:#f1f5f9; -fx-border-width:0 0 1 0;"
        );
        return cell;
    }

    private Label createTextCell(String text, boolean isAlt) {
        Label cell = new Label(text);
        cell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setMinHeight(52);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 14, 0, 14));
        cell.setStyle(
                "-fx-background-color:" + (isAlt ? "#fafcff" : "white") + ";" +
                "-fx-text-fill:#334155; -fx-font-size:14;" +
                "-fx-border-color:#f1f5f9; -fx-border-width:0 0 1 0;"
        );
        return cell;
    }

    private HBox createStatusCell(String status, boolean isAlt) {
        Label badge = new Label(status);
        String textColor = "#16a34a";
        String bgColor = "#ecfdf5";
        if ("Processing".equalsIgnoreCase(status)) {
            textColor = "#0b63ff";
            bgColor = "#eff6ff";
        } else if ("Scheduled".equalsIgnoreCase(status)) {
            textColor = "#6366f1";
            bgColor = "#eef2ff";
        }

        badge.setStyle(
                "-fx-text-fill:" + textColor + ";" +
                "-fx-background-color:" + bgColor + ";" +
                "-fx-background-radius:6;" +
                "-fx-padding:4 10 4 10;" +
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;"
        );

        HBox cell = new HBox(badge);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setMinHeight(52);
        cell.setPadding(new Insets(0, 14, 0, 14));
        cell.setStyle(
                "-fx-background-color:" + (isAlt ? "#fafcff" : "white") + ";" +
                "-fx-border-color:#f1f5f9; -fx-border-width:0 0 1 0;"
        );
        return cell;
    }

    private HBox createActionCell(AdminDataStore.ReportRow report, boolean isAlt) {
        Button view = linkButton("View");
        view.setOnAction(e -> showReportViewerDialog(report, view.getScene() == null ? null : view.getScene().getWindow()));

        Button export = linkButton("Export");
        export.setOnAction(e -> {
            try {
                String path = store.exportReport(report);
                showToast("Report exported successfully to " + path, true);
            } catch (Exception ex) {
                showToast("Export failed: " + ex.getMessage(), false);
            }
        });

        Label sep = new Label("|");
        sep.setStyle("-fx-text-fill:#cbd5e1; -fx-font-size:12;");

        HBox cell = new HBox(8, view, sep, export);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setMinHeight(52);
        cell.setPadding(new Insets(0, 14, 0, 14));
        cell.setStyle(
                "-fx-background-color:" + (isAlt ? "#fafcff" : "white") + ";" +
                "-fx-border-color:#f1f5f9; -fx-border-width:0 0 1 0;"
        );
        return cell;
    }

    private Label customTableCell(String text, boolean header) {
        Label cell = new Label(text);
        cell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setMinHeight(header ? 48 : 52);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 14, 0, 14));
        cell.setStyle(
                "-fx-background-color:" + (header ? "#f8fafc" : "white") + ";" +
                "-fx-text-fill:" + (header ? "#0f172a" : "#334155") + ";" +
                "-fx-font-size:14;" +
                "-fx-font-weight:" + (header ? "bold" : "normal") + ";" +
                "-fx-border-color:#e2e8f0; -fx-border-width:0 0 1 0;"
        );
        return cell;
    }

    private Button linkButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-text-fill:" + BLUE + ";" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-cursor:hand;" +
                "-fx-padding:2 4 2 4;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color:#eff6ff;" +
                "-fx-text-fill:#0952d6;" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:4;" +
                "-fx-cursor:hand;" +
                "-fx-padding:2 4 2 4;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-text-fill:" + BLUE + ";" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-cursor:hand;" +
                "-fx-padding:2 4 2 4;"
        ));
        return button;
    }

    // ==========================================
    // MODAL DIALOGS
    // ==========================================

    // 1. Generate Report Modal
    private void showGenerateReportDialog(Window owner) {
        Stage modal = new Stage();
        if (owner != null) modal.initOwner(owner);
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Generate Maritime Operations Report");
        modal.setResizable(false);

        VBox box = new VBox(14);
        box.setPadding(new Insets(24));
        box.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:12;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.15), 16, 0, 0, 4);"
        );

        Label title = new Label("📊 Generate Operations Report");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");

        Label sub = new Label("Configure parameters to create a new analytical report from real-time port telemetry.");
        sub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        // Form Fields
        Label typeLbl = new Label("Report Category");
        typeLbl.setStyle("-fx-text-fill:#334155; -fx-font-size:12; -fx-font-weight:bold;");
        ComboBox<String> typeCb = combo("Select Type", "Port Operations Summary", "Vessel Traffic & Turnaround", "Cargo Throughput & Tonnage", "Berth Utilization Analysis", "Customs & Inspections Audit", "Environmental & Safety Compliance");
        typeCb.setMaxWidth(Double.MAX_VALUE);

        Label periodLbl = new Label("Reporting Period");
        periodLbl.setStyle("-fx-text-fill:#334155; -fx-font-size:12; -fx-font-weight:bold;");
        ComboBox<String> periodCb = combo("Select Period", "Today", "This Week", "This Month", "Last 30 Days", "Current Quarter (Q3 2026)");
        periodCb.setMaxWidth(Double.MAX_VALUE);

        Label formatLbl = new Label("Export Format");
        formatLbl.setStyle("-fx-text-fill:#334155; -fx-font-size:12; -fx-font-weight:bold;");
        ComboBox<String> formatCb = combo("Select Format", "CSV Spreadsheet (.csv)", "PDF Document (.pdf)", "Excel Workbook (.xlsx)");
        formatCb.setMaxWidth(Double.MAX_VALUE);

        Button cancel = new Button("Cancel");
        cancel.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#475569; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:8 16; -fx-background-radius:6; -fx-cursor:hand;");
        cancel.setOnAction(e -> modal.close());

        Button submit = new Button("Generate & Add to Table");
        submit.setStyle("-fx-background-color:" + BLUE + "; -fx-text-fill:white; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:8 18; -fx-background-radius:6; -fx-cursor:hand;");
        submit.setOnAction(e -> {
            String repName = typeCb.getValue() == null ? "Port Operations Summary" : typeCb.getValue();
            String repPeriod = periodCb.getValue() == null ? "Today" : periodCb.getValue();
            String nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));

            AdminDataStore.ReportRow newReport = new AdminDataStore.ReportRow(
                    repName,
                    repPeriod,
                    "Ready",
                    store.adminName == null ? "Aarav Mehta (Admin)" : store.adminName,
                    nowTime
            );
            store.reports.add(0, newReport);
            refreshReportTable();
            showToast("Successfully generated: " + repName + " (" + repPeriod + ")", true);
            modal.close();
        });

        HBox actions = new HBox(10, cancel, submit);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(10, 0, 0, 0));

        box.getChildren().addAll(
                title, sub, new Separator(),
                typeLbl, typeCb,
                periodLbl, periodCb,
                formatLbl, formatCb,
                new Separator(), actions
        );

        Scene scene = new Scene(box, 440, 430);
        modal.setScene(scene);
        modal.show();
    }

    // 2. View Report Details Modal
    private void showReportViewerDialog(AdminDataStore.ReportRow report, Window owner) {
        Stage modal = new Stage();
        if (owner != null) modal.initOwner(owner);
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Report Details - " + report.name);
        modal.setResizable(false);

        VBox box = new VBox(14);
        box.setPadding(new Insets(24));
        box.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:12;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.15), 16, 0, 0, 4);"
        );

        Label title = new Label("📄 " + report.name);
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");

        Label sub = new Label("Generated on " + report.generatedDate + " by " + report.generatedBy);
        sub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        VBox metaBox = new VBox(8);
        metaBox.setPadding(new Insets(14));
        metaBox.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");
        metaBox.getChildren().addAll(
                dialogDetailRow("Report Period:", report.period),
                dialogDetailRow("Status:", report.status),
                dialogDetailRow("Author / Role:", report.generatedBy),
                dialogDetailRow("Operational Scope:", "Mumbai, Kolkata, Chennai, JNPT, Vizag"),
                dialogDetailRow("Total Cargo Covered:", CARGO_HANDLED_VAL),
                dialogDetailRow("Vessels Monitored:", "66 Arrivals / 56 Departures")
        );

        Label summaryTitle = new Label("Executive Summary & Findings");
        summaryTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");

        Label summaryText = new Label(
                "• All terminal operations are within standard safety and throughput thresholds.\n" +
                "• Turnaround times have improved by 8 minutes on average (45 min avg waiting).\n" +
                "• Port of Kolkata requires continued congestion monitoring (91% utilization).\n" +
                "• 14 inspections are currently queued with customs officers assigned."
        );
        summaryText.setStyle("-fx-text-fill:#475569; -fx-font-size:12; -fx-line-spacing:3;");

        Button close = new Button("Close");
        close.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#475569; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:8 16; -fx-background-radius:6; -fx-cursor:hand;");
        close.setOnAction(e -> modal.close());

        Button exportBtn = new Button("Download CSV");
        exportBtn.setStyle("-fx-background-color:" + BLUE + "; -fx-text-fill:white; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:8 18; -fx-background-radius:6; -fx-cursor:hand;");
        exportBtn.setOnAction(e -> {
            try {
                store.exportReport(report);
                showToast("Exported " + report.name + " to " + store.lastExportPath, true);
                modal.close();
            } catch (Exception ex) {
                showToast("Export failed: " + ex.getMessage(), false);
            }
        });

        HBox actions = new HBox(10, close, exportBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        box.getChildren().addAll(
                title, sub, new Separator(),
                metaBox,
                summaryTitle, summaryText,
                new Separator(), actions
        );

        Scene scene = new Scene(box, 500, 470);
        modal.setScene(scene);
        modal.show();
    }

    // 3. Custom Date Range Dialog
    private void showCustomRangeDialog(Window owner) {
        Stage modal = new Stage();
        if (owner != null) modal.initOwner(owner);
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Select Custom Date Range");
        modal.setResizable(false);

        VBox box = new VBox(14);
        box.setPadding(new Insets(22));
        box.setStyle("-fx-background-color:white; -fx-background-radius:12;");

        Label title = new Label("📅 Select Custom Date Range");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16; -fx-font-weight:bold;");

        TextField fromDate = new TextField("01/08/2026");
        TextField toDate = new TextField("14/08/2026");
        styleField(fromDate);
        styleField(toDate);

        Button apply = new Button("Apply Range");
        apply.setStyle("-fx-background-color:" + BLUE + "; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:8 16; -fx-background-radius:6; -fx-cursor:hand;");
        apply.setOnAction(e -> {
            selectedPeriod = fromDate.getText() + " - " + toDate.getText();
            updatePeriodButtonStyles();
            store.generateReports(selectedPeriod, statusFilter == null ? "All Status" : statusFilter.getValue());
            refreshReportTable();
            showToast("Filtered by custom range: " + selectedPeriod, true);
            modal.close();
        });

        Button cancel = new Button("Cancel");
        cancel.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#475569; -fx-font-weight:bold; -fx-padding:8 14; -fx-background-radius:6; -fx-cursor:hand;");
        cancel.setOnAction(e -> modal.close());

        HBox actions = new HBox(8, cancel, apply);
        actions.setAlignment(Pos.CENTER_RIGHT);

        box.getChildren().addAll(
                title,
                new Label("From Date (DD/MM/YYYY):"), fromDate,
                new Label("To Date (DD/MM/YYYY):"), toDate,
                new Separator(),
                actions
        );

        Scene scene = new Scene(box, 360, 290);
        modal.setScene(scene);
        modal.show();
    }

    private HBox dialogDetailRow(String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:#64748b; -fx-font-size:12; -fx-font-weight:bold; -fx-min-width:140;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill:#0f172a; -fx-font-size:12; -fx-font-weight:bold;");
        return new HBox(6, l, v);
    }

    // Helper: Chart Legend Item
    private HBox chartLegendItem(String labelText, String color) {
        Circle dot = new Circle(4);
        dot.setStyle("-fx-fill:" + color + ";");
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill:#475569; -fx-font-size:11; -fx-font-weight:bold;");
        HBox item = new HBox(5, dot, label);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    // Helper: Summary Badge
    private HBox statSummaryBadge(String label, String value, String accentColor) {
        Label l = new Label(label + ": ");
        l.setStyle("-fx-text-fill:#64748b; -fx-font-size:11;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill:" + accentColor + "; -fx-font-size:11; -fx-font-weight:bold;");
        HBox badge = new HBox(2, l, v);
        badge.setAlignment(Pos.CENTER_LEFT);
        return badge;
    }
}
