package com.projectui.view.exporter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.projectui.controller.ExpContainerController;
import com.projectui.dao.CargoDao;
import com.projectui.view.login.DashboardLayoutHelper;
import com.projectui.view.login.LoginPage;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ContainerBooking {

        // Admin Design System Tokens
        private static final String BLUE = "#0b63ff";
        private static final String BG = "#f5f8fc";
        private static final String TEXT = "#111827";
        private static final String MUTED = "#64748b";
        private static final String CARD_STYLE = "-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-radius:8; -fx-background-radius:8; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.06), 14, 0, 0, 4);";

        private final CargoDao cargoDao = new CargoDao();
        private final ExpContainerController expContainerController = new ExpContainerController();

        // Form Controls
        private ComboBox<String> containerType;
        private ComboBox<String> shippingLine;
        private ComboBox<String> preferredPort;
        private ComboBox<String> bookingDate;

        private TextField quantity;
        private Label totalLabel;
        private VBox successBox;
        private ProgressBar progressBar;

        // Dynamic Cargo Values
        private String currentCargoId = "";
        private String currentCargoName = "";
        private String currentWeight = "";
        private String currentRoute = "";
        private String currentValue = "";
        private String currentOrigin = "";
        private String currentDestination = "";

        private Label cargoIdLabel;
        private Label cargoNameLabel;
        private Label cargoWeightLabel;
        private Label cargoRouteLabel;
        private Label cargoValueLabel;
        private ComboBox<String> cargoSelector;

        // =========================================================
        // SCENE GENERATORS
        // =========================================================

        public Scene getContainerBookingScene(Runnable callbackAction) {
                return getContainerBookingScene();
        }

        public Scene getContainerBookingScene() {
                BorderPane root = new BorderPane();
                root.setStyle("-fx-background-color:" + BG + ";");

                // Admin standard header & sidebar via shared layout helper
                root.setTop(createHeader("Book Container", "DockTech / Exporter / Book Container"));
                root.setLeft(createSidebar());

                root.setCenter(getScrollableContent());

                Scene sc = new Scene(root, 1550, 800);
                LoginPage.applyInvisibleScrollbar(sc);
                return sc;
        }

        public ScrollPane getScrollableContent() {
                loadDynamicCargoData();
                VBox bookingPage = createBookingPage();

                ScrollPane scrollPane = new ScrollPane(bookingPage);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(false);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scrollPane.setPannable(true);
                scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + BG
                                + "; -fx-padding:0; -fx-border-color: transparent;");
                return scrollPane;
        }

        private void loadDynamicCargoData() {
                try {
                        List<Map<String, Object>> cargos = cargoDao.getAllCargos();
                        if (cargos != null && !cargos.isEmpty()) {
                                Map<String, Object> c = cargos.get(0);
                                applyCargoMap(c);
                        }
                } catch (Exception ex) {
                        System.err.println("[CONTAINER BOOKING] Warning loading cargo data: " + ex.getMessage());
                }
        }

        private void applyCargoMap(Map<String, Object> c) {
                if (c == null)
                        return;
                if (c.get("cargoId") != null)
                        currentCargoId = c.get("cargoId").toString();
                if (c.get("cargoName") != null)
                        currentCargoName = c.get("cargoName").toString();
                if (c.get("originPort") != null)
                        currentOrigin = c.get("originPort").toString();
                if (c.get("destinationPort") != null)
                        currentDestination = c.get("destinationPort").toString();
                currentRoute = currentOrigin + " → " + currentDestination;
                if (c.get("weight") != null)
                        currentWeight = c.get("weight").toString() + " kg";
                if (c.get("value") != null)
                        currentValue = "₹ " + c.get("value").toString();

                if (cargoIdLabel != null)
                        cargoIdLabel.setText(currentCargoId);
                if (cargoNameLabel != null)
                        cargoNameLabel.setText(currentCargoName);
                if (cargoWeightLabel != null)
                        cargoWeightLabel.setText(currentWeight);
                if (cargoRouteLabel != null)
                        cargoRouteLabel.setText(currentRoute);
                if (cargoValueLabel != null)
                        cargoValueLabel.setText(currentValue);

                if (preferredPort != null && currentOrigin != null) {
                        if (preferredPort.getItems().contains(currentOrigin)) {
                                preferredPort.setValue(currentOrigin);
                        }
                }
                calculateTotal();
        }

        // =========================================================
        // MAIN PAGE CONTENT
        // =========================================================

        public VBox createBookingPage() {
                VBox main = new VBox(18);
                main.setPadding(new Insets(18, 22, 28, 22));
                main.setStyle("-fx-background-color:transparent;");

                // 1. Top Page Header with Title & Operational Status (Admin Page Pattern)
                Label title = new Label("Book Container");
                title.setStyle("-fx-text-fill:#05070c; -fx-font-size:26; -fx-font-weight:bold;");

                Label subtitle = new Label(
                                "Reserve and allocate certified ISO freight containers for your verified export cargo.");
                subtitle.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:14;");

                VBox titleCopy = new VBox(4, title, subtitle);

                Region titleSpace = new Region();
                HBox.setHgrow(titleSpace, Priority.ALWAYS);

                Label statusBadge = new Label("● CARGO READY FOR ALLOCATION");
                statusBadge.setStyle(
                                "-fx-background-color:#ecfdf5; -fx-text-fill:#047857; -fx-background-radius:8; -fx-padding:8 16; -fx-font-size:13; -fx-font-weight:bold;");

                HBox headerRow = new HBox(12, titleCopy, titleSpace, statusBadge);
                headerRow.setAlignment(Pos.CENTER_LEFT);

                double baseCost = com.projectui.service.FreightCalculationService.getInstance()
                                .calculateContainerBookingCost("20 ft Standard Dry", 1, 1000.0, currentOrigin, currentDestination);
                String baseRateStr = "₹ " + String.format("%,.0f", baseCost);

                // 2. Summary KPI Metric Row (Admin Pattern)
                HBox kpiRow = new HBox(12);
                kpiRow.getChildren().addAll(
                                createStatCard("SELECTED CARGO", currentCargoId, "#0b63ff", currentCargoName),
                                createStatCard("CONTAINER SIZES", "4 Options", "#111827", "20ft, 40ft, 45ft"),
                                createStatCard("CARRIERS AVAILABLE", "5 Lines", "#16a34a", "Active Port Berth"),
                                createStatCard("BASE RATE", baseRateStr, "#d97706", "Standard Dry 20ft"));

                // 3. Main Workspace Area (2 Columns)
                HBox workspace = new HBox(18);
                workspace.setAlignment(Pos.TOP_LEFT);

                // =====================================================
                // LEFT COLUMN: REGISTERED CARGO CARD (Admin Design System)
                // =====================================================
                VBox cargoCard = new VBox(14);
                cargoCard.setPrefWidth(380);
                cargoCard.setMinWidth(360);
                cargoCard.setMaxWidth(420);
                cargoCard.setPadding(new Insets(18));
                cargoCard.setStyle(CARD_STYLE);

                HBox cargoHeader = new HBox(10);
                cargoHeader.setAlignment(Pos.CENTER_LEFT);

                Label cargoIcon = new Label("📋");
                cargoIcon.setMinSize(40, 40);
                cargoIcon.setMaxSize(40, 40);
                cargoIcon.setAlignment(Pos.CENTER);
                cargoIcon.setStyle("-fx-background-color:#eff6ff; -fx-background-radius:8; -fx-text-fill:" + BLUE
                                + "; -fx-font-size:18; -fx-font-weight:bold;");

                VBox cargoTitleBox = new VBox(2);
                Label cTitle = new Label("Registered Cargo");
                cTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:18; -fx-font-weight:bold;");
                Label cSub = new Label("Select manifest for allocation");
                cSub.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:13;");
                cargoTitleBox.getChildren().addAll(cTitle, cSub);
                cargoHeader.getChildren().addAll(cargoIcon, cargoTitleBox);

                // Dynamic Cargo Selector ComboBox
                cargoSelector = new ComboBox<>();
                cargoSelector.setMaxWidth(Double.MAX_VALUE);
                cargoSelector.setPrefHeight(38);
                cargoSelector.setStyle(
                                "-fx-background-color:white; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:13;");

                List<Map<String, Object>> allCargos = cargoDao.getAllCargos();
                if (allCargos != null && !allCargos.isEmpty()) {
                        for (Map<String, Object> c : allCargos) {
                                String cId = c.get("cargoId") != null ? c.get("cargoId").toString() : "CGO";
                                String cName = c.get("cargoName") != null ? c.get("cargoName").toString() : "";
                                cargoSelector.getItems().add(cId + " — " + cName);
                        }
                        cargoSelector.setValue(cargoSelector.getItems().get(0));
                } else {
                        cargoSelector.getItems().add("No Registered Cargo");
                        cargoSelector.setValue("No Registered Cargo");
                }

                cargoSelector.setOnAction(e -> {
                        String sel = cargoSelector.getValue();
                        if (sel != null) {
                                String selId = sel.split(" — ")[0].trim();
                                Map<String, Object> found = cargoDao.getCargo(selId);
                                if (found != null) {
                                        applyCargoMap(found);
                                        calculateTotal();
                                }
                        }
                });

                cargoIdLabel = new Label(currentCargoId);
                cargoNameLabel = new Label(currentCargoName);
                cargoWeightLabel = new Label(currentWeight);
                cargoRouteLabel = new Label(currentRoute);
                cargoValueLabel = new Label(currentValue);

                VBox cargoIdBox = cargoInfoTileDirect("CARGO ID", cargoIdLabel);
                VBox cargoNameBox = cargoInfoTileDirect("CARGO NAME", cargoNameLabel);
                VBox cargoWeightBox = cargoInfoTileDirect("WEIGHT", cargoWeightLabel);
                VBox cargoRouteBox = cargoInfoTileDirect("ROUTE", cargoRouteLabel);
                VBox cargoValueBox = cargoInfoTileDirect("DECLARED VALUE", cargoValueLabel);

                ImageView shipImage = createShipImage();

                cargoCard.getChildren().addAll(
                                cargoHeader,
                                new VBox(4, new Label("SELECT REGISTERED CARGO"), cargoSelector),
                                cargoIdBox,
                                cargoNameBox,
                                cargoWeightBox,
                                cargoRouteBox,
                                cargoValueBox,
                                shipImage);

                // =====================================================
                // RIGHT COLUMN: CONTAINER BOOKING CARD (Admin Design System)
                // =====================================================
                VBox bookingCard = new VBox(16);
                HBox.setHgrow(bookingCard, Priority.ALWAYS);
                bookingCard.setPadding(new Insets(20));
                bookingCard.setStyle(CARD_STYLE);

                VBox bookingHeader = new VBox(4);
                Label bookingTitle = new Label("Container Booking");
                bookingTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:18; -fx-font-weight:bold;");
                Label bookingSub = new Label(
                                "Configure equipment specifications, carrier preferences and required shipping schedule.");
                bookingSub.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:13;");
                bookingHeader.getChildren().addAll(bookingTitle, bookingSub);

                GridPane form = new GridPane();
                form.setHgap(16);
                form.setVgap(14);

                ColumnConstraints col1 = new ColumnConstraints();
                col1.setPercentWidth(50);
                col1.setHgrow(Priority.ALWAYS);

                ColumnConstraints col2 = new ColumnConstraints();
                col2.setPercentWidth(50);
                col2.setHgrow(Priority.ALWAYS);

                form.getColumnConstraints().addAll(col1, col2);

                containerType = combo("20 ft Standard Dry", "40 ft Standard Dry", "40 ft High Cube",
                                "20 ft Refrigerated (Reefer)", "40 ft Refrigerated (Reefer)", "45 ft High Cube");
                containerType.setOnAction(e -> calculateTotal());

                quantity = new TextField("1");
                styleTextField(quantity);
                quantity.textProperty().addListener((obs, oldV, newV) -> calculateTotal());

                shippingLine = combo("Maersk Line", "MSC Mediterranean", "CMA CGM", "Hapag-Lloyd",
                                "ONE (Ocean Network)");

                // Dynamic Ports from Admin Data Store
                List<String> dynamicPorts = new java.util.ArrayList<>();
                for (com.projectui.view.admin.AdminDataStore.Port p : com.projectui.view.admin.AdminDataStore
                                .getInstance().ports) {
                        if (p.name != null && !com.projectui.view.admin.AdminDataStore.isPortDeleted(p.name, p.code)) {
                                if ("APPROVED".equalsIgnoreCase(p.approvalStatus) || p.approvalStatus == null) {
                                        dynamicPorts.add(p.name);
                                }
                        }
                }
                if (dynamicPorts.isEmpty()) {
                        dynamicPorts.add("Port of Mumbai");
                        dynamicPorts.add("Port of Chennai");
                        dynamicPorts.add("Port of Vizag");
                        dynamicPorts.add("Port of Kolkata");
                        dynamicPorts.add("Cochin Port");
                }
                preferredPort = combo(dynamicPorts.toArray(new String[0]));
                if (preferredPort.getItems().contains(currentOrigin)) {
                        preferredPort.setValue(currentOrigin);
                }

                bookingDate = combo(
                                "Within 3 Days",
                                "Within 7 Days",
                                "Within 15 Days",
                                "Within 30 Days");

                addField(form, "Container Type", containerType, 0, 0);
                addField(form, "Quantity", quantity, 1, 0);
                addField(form, "Shipping Line", shippingLine, 0, 1);
                addField(form, "Preferred Port", preferredPort, 1, 1);
                addField(form, "Required By", bookingDate, 0, 2);

                // ESTIMATED COST PANEL
                VBox priceBox = new VBox(6);
                priceBox.setPadding(new Insets(14));
                priceBox.setStyle(
                                "-fx-background-color:#f0f7ff; -fx-background-radius:8; -fx-border-color:#dbeafe; -fx-border-radius:8;");

                Label estimateLabel = new Label("ESTIMATED BOOKING COST");
                estimateLabel.setStyle("-fx-text-fill:#475467; -fx-font-size:12; -fx-font-weight:bold;");

                totalLabel = new Label("₹ 45,000");
                totalLabel.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:24; -fx-font-weight:bold;");

                Label priceInfo = new Label(
                                "Final charges may vary based on terminal handling, equipment availability, and bunker surcharges.");
                priceInfo.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12;");

                priceBox.getChildren().addAll(estimateLabel, totalLabel, priceInfo);

                // Action Buttons Row
                Button calculateBtn = new Button("↻  Calculate Total");
                calculateBtn.setStyle(
                                "-fx-background-color:#f2f4f8; -fx-text-fill:#111827; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:9 16; -fx-background-radius:8; -fx-cursor:hand;");
                calculateBtn.setOnAction(e -> calculateTotal());

                Button bookBtn = new Button("✓  Book Container");
                bookBtn.setStyle("-fx-background-color:" + BLUE
                                + "; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:10 20; -fx-background-radius:8; -fx-cursor:hand; -fx-effect: dropshadow(gaussian, rgba(11,99,255,0.25), 8, 0, 0, 2);");
                HBox.setHgrow(bookBtn, Priority.ALWAYS);
                bookBtn.setMaxWidth(Double.MAX_VALUE);
                bookBtn.setOnAction(e -> bookContainer());

                HBox actionRow = new HBox(12, calculateBtn, bookBtn);
                actionRow.setAlignment(Pos.CENTER_LEFT);

                progressBar = new ProgressBar(0);
                progressBar.setPrefHeight(6);
                progressBar.setMaxWidth(Double.MAX_VALUE);
                progressBar.setVisible(false);
                progressBar.setManaged(false);
                progressBar.setStyle("-fx-accent:" + BLUE + ";");

                successBox = createSuccessBox();
                successBox.setVisible(false);
                successBox.setManaged(false);

                bookingCard.getChildren().addAll(
                                bookingHeader,
                                form,
                                priceBox,
                                actionRow,
                                progressBar,
                                successBox);

                workspace.getChildren().addAll(cargoCard, bookingCard);

                main.getChildren().addAll(headerRow, kpiRow, workspace);
                return main;
        }

        // =========================================================
        // UI STATS & TILE HELPERS
        // =========================================================

        private VBox createStatCard(String title, String value, String color, String sub) {
                Label t = new Label(title);
                t.setStyle("-fx-text-fill:#475467; -fx-font-size:12; -fx-font-weight:bold;");

                Label v = new Label(value);
                v.setStyle("-fx-text-fill:" + color + "; -fx-font-size:24; -fx-font-weight:bold;");

                Label s = new Label(sub);
                s.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12;");

                VBox box = new VBox(4, t, v, s);
                HBox.setHgrow(box, Priority.ALWAYS);
                box.setPadding(new Insets(14, 16, 14, 16));
                box.setStyle(CARD_STYLE);
                return box;
        }

        private VBox cargoInfoTileDirect(String title, Label valLabel) {
                VBox box = new VBox(3);
                box.setPadding(new Insets(10, 12, 10, 12));
                box.setStyle("-fx-background-color:#f8fafc; -fx-background-radius:8; -fx-border-color:#e2e8f0; -fx-border-radius:8;");

                Label t = new Label(title);
                t.setStyle("-fx-text-fill:#64748b; -fx-font-size:11; -fx-font-weight:bold;");

                valLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14; -fx-font-weight:bold;");

                box.getChildren().addAll(t, valLabel);
                return box;
        }

        private VBox cargoInfoTile(String title, String value) {
                VBox box = new VBox(3);
                box.setPadding(new Insets(10, 12, 10, 12));
                box.setStyle("-fx-background-color:#f8fafc; -fx-background-radius:8; -fx-border-color:#e2e8f0; -fx-border-radius:8;");

                Label t = new Label(title);
                t.setStyle("-fx-text-fill:#64748b; -fx-font-size:11; -fx-font-weight:bold;");

                Label v = new Label(value);
                v.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14; -fx-font-weight:bold;");

                box.getChildren().addAll(t, v);
                return box;
        }

        private ImageView createShipImage() {
                ImageView imageView = new ImageView();
                try {
                        Image image = new Image(getClass().getResourceAsStream("/Assets/images/container_ship.png"));
                        imageView.setImage(image);
                } catch (Exception ignored) {
                        try {
                                Image fallback = new Image(
                                                getClass().getResourceAsStream("/Assets\\images\\container_ship.png"));
                                imageView.setImage(fallback);
                        } catch (Exception ignoredAgain) {
                                // UI remains cleanly usable if image is unavailable
                        }
                }

                imageView.setFitWidth(340);
                imageView.setFitHeight(110);
                imageView.setPreserveRatio(false);
                imageView.setSmooth(true);
                return imageView;
        }

        private ComboBox<String> combo(String... values) {
                ComboBox<String> box = new ComboBox<>();
                box.getItems().addAll(values);
                if (values.length > 0)
                        box.setValue(values[0]);
                box.setMaxWidth(Double.MAX_VALUE);
                box.setPrefHeight(40);
                box.setStyle("-fx-background-color:white; -fx-border-color:#e6eef8; -fx-border-radius:8; -fx-background-radius:8; -fx-padding:4 8; -fx-font-size:14;");
                return box;
        }

        private void styleTextField(TextField tf) {
                tf.setPrefHeight(40);
                tf.setStyle("-fx-background-color:white; -fx-border-color:#e6eef8; -fx-border-radius:8; -fx-background-radius:8; -fx-padding:8 12; -fx-font-size:14;");
        }

        private void addField(GridPane grid, String text, javafx.scene.control.Control control, int column, int row) {
                VBox box = new VBox(6);
                Label label = new Label(text);
                label.setStyle("-fx-text-fill:#374151; -fx-font-size:13; -fx-font-weight:bold;");

                control.setPrefHeight(40);
                control.setMaxWidth(Double.MAX_VALUE);

                box.getChildren().addAll(label, control);
                grid.add(box, column, row);
                GridPane.setHgrow(box, Priority.ALWAYS);
        }

        // =========================================================
        // CALCULATION & BOOKING LOGIC
        // =========================================================

        private double getCalculatedContainerCost() {
                int qty = 1;
                if (quantity != null && quantity.getText() != null && isPositiveNumber(quantity.getText())) {
                        try {
                                qty = Integer.parseInt(quantity.getText().trim());
                        } catch (Exception ignored) {}
                }
                double wt = 1200.0;
                try {
                        wt = Double.parseDouble(currentWeight.replaceAll("[^0-9.]", "").trim());
                } catch (Exception ignored) {}
                if (wt <= 0) wt = 1200.0;

                String selectedType = containerType != null && containerType.getValue() != null ? containerType.getValue() : "20 ft Standard Dry";
                String orig = currentOrigin != null && !currentOrigin.isBlank() ? currentOrigin : (preferredPort != null ? preferredPort.getValue() : "Port of Mumbai");
                String dest = currentDestination != null && !currentDestination.isBlank() ? currentDestination : "Port of Singapore";

                return com.projectui.service.FreightCalculationService.getInstance()
                                .calculateContainerBookingCost(selectedType, qty, wt, orig, dest);
        }

        private void calculateTotal() {
                double total = getCalculatedContainerCost();
                if (totalLabel != null) {
                        totalLabel.setText("₹ " + String.format("%,.0f", total));
                }
        }

        private void bookContainer() {
                if (!isPositiveNumber(quantity.getText())) {
                        showError("Please enter a valid container quantity.");
                        quantity.requestFocus();
                        return;
                }

                calculateTotal();

                successBox.setVisible(false);
                successBox.setManaged(false);

                progressBar.setVisible(true);
                progressBar.setManaged(true);
                progressBar.setProgress(0);

                new Thread(() -> {
                        try {
                                Thread.sleep(250);
                                Platform.runLater(() -> progressBar.setProgress(0.35));

                                Thread.sleep(250);
                                Platform.runLater(() -> progressBar.setProgress(0.70));

                                Thread.sleep(300);
                                Platform.runLater(() -> {
                                        try {
                                                progressBar.setProgress(1);

                                                double totalBookingCost = getCalculatedContainerCost();

                                                String id = expContainerController.bookContainer(
                                                                currentCargoId,
                                                                currentCargoName,
                                                                containerType.getValue(),
                                                                quantity.getText().trim(),
                                                                preferredPort.getValue(),
                                                                currentOrigin,
                                                                currentDestination,
                                                                shippingLine.getValue(),
                                                                totalBookingCost);

                                                showBookingSuccess(id);
                                        } catch (Exception ex) {
                                                progressBar.setVisible(false);
                                                progressBar.setManaged(false);
                                                showError("Failed to book container: " + ex.getMessage());
                                        }
                                });
                        } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                        }
                }).start();
        }

        private VBox createSuccessBox() {
                VBox box = new VBox(10);
                box.setPadding(new Insets(16));
                box.setStyle("-fx-background-color:#ecfdf5; -fx-background-radius:8; -fx-border-color:#bbf7d0; -fx-border-radius:8;");

                Label title = new Label("✓  Container Allocated & Booked Successfully!");
                title.setStyle("-fx-text-fill:#15803d; -fx-font-size:15; -fx-font-weight:bold;");

                Label id = new Label("Booking Reference: CONT-0000");
                id.setStyle("-fx-text-fill:#166534; -fx-font-size:13; -fx-font-weight:bold;");

                Label message = new Label(
                                "Your freight container has been allocated in the port terminal database and is ready for documentation.");
                message.setWrapText(true);
                message.setStyle("-fx-text-fill:#166534; -fx-font-size:12;");

                Button next = new Button("Continue to Document Upload  →");
                next.setPrefHeight(38);
                next.setMaxWidth(Double.MAX_VALUE);
                next.setStyle("-fx-background-color:" + BLUE
                                + "; -fx-text-fill:white; -fx-background-radius:8; -fx-font-size:13; -fx-font-weight:bold; -fx-cursor:hand;");
                next.setOnAction(e -> {
                        if (ExporterDashboard.getInstance() != null) {
                                ExporterDashboard.getInstance().switchPage("Upload Document");
                        } else {
                                UploadDocument page = new UploadDocument();
                                LoginPage.showRoot(ExporterDashboard.dashboardStage,
                                                page.getUploadDocumentScene(this::back).getRoot());
                        }
                });

                box.getChildren().addAll(title, id, message, next);
                return box;
        }

        private void showBookingSuccess(String containerId) {
                progressBar.setVisible(false);
                progressBar.setManaged(false);

                Label id = (Label) successBox.getChildren().get(1);
                id.setText("Booking Reference: " + containerId);

                successBox.setVisible(true);
                successBox.setManaged(true);
        }

        private boolean isPositiveNumber(String value) {
                if (value == null)
                        return false;
                try {
                        return Integer.parseInt(value.trim()) > 0;
                } catch (Exception e) {
                        return false;
                }
        }

        private void showError(String message) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Container Booking");
                alert.setHeaderText("Invalid Information");
                alert.setContentText(message);
                alert.showAndWait();
        }

        private void showInfo(String title, String message) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
        }

        // =========================================================
        // SIDEBAR & HEADER
        // =========================================================

        private VBox createSidebar() {
                Map<String, Button> menuButtons = new LinkedHashMap<>();
                Button dashboard = new Button("🏠   Dashboard");
                Button register = new Button("📦  Register Cargo");
                Button book = new Button("🚢   Book Container");
                Button upload = new Button("📄   Upload Document");
                Button payment = new Button("💳   Make Payment");
                Button track = new Button("📥   Track Shipment");

                menuButtons.put("Dashboard", dashboard);
                menuButtons.put("Register Cargo", register);
                menuButtons.put("Book Container", book);
                menuButtons.put("Upload Document", upload);
                menuButtons.put("Make Payment", payment);
                menuButtons.put("Track Shipment", track);

                dashboard.setOnAction(e -> {
                        if (ExporterDashboard.getInstance() != null) {
                                ExporterDashboard.getInstance().switchPage("Dashboard");
                        } else {
                                back();
                        }
                });
                register.setOnAction(e -> {
                        if (ExporterDashboard.getInstance() != null) {
                                ExporterDashboard.getInstance().switchPage("Register Cargo");
                        } else {
                                RegisterCargo page = new RegisterCargo();
                                LoginPage.showRoot(ExporterDashboard.dashboardStage,
                                                page.getRegisterCargoScene(this::back).getRoot());
                        }
                });

                book.setOnAction(e -> {
                        if (ExporterDashboard.getInstance() != null) {
                                ExporterDashboard.getInstance().switchPage("Book Container");
                        }
                });

                upload.setOnAction(e -> {
                        if (ExporterDashboard.getInstance() != null) {
                                ExporterDashboard.getInstance().switchPage("Upload Document");
                        } else {
                                UploadDocument page1 = new UploadDocument();
                                LoginPage.showRoot(ExporterDashboard.dashboardStage,
                                                page1.getUploadDocumentScene(this::back).getRoot());
                        }
                });

                payment.setOnAction(e -> {
                        if (ExporterDashboard.getInstance() != null) {
                                ExporterDashboard.getInstance().switchPage("Make Payment");
                        } else {
                                MakePayment page1 = new MakePayment();
                                LoginPage.showRoot(ExporterDashboard.dashboardStage,
                                                page1.getMakePaymentScene(this::back).getRoot());
                        }
                });

                track.setOnAction(e -> {
                        if (ExporterDashboard.getInstance() != null) {
                                ExporterDashboard.getInstance().switchPage("Track Shipment");
                        } else {
                                TrackShipment page2 = new TrackShipment();
                                LoginPage.showRoot(ExporterDashboard.dashboardStage,
                                                page2.getTrackShipmentScene(this::back).getRoot());
                        }
                });

                return DashboardLayoutHelper.createSidebar(ExporterDashboard.dashboardStage, "Exporter",
                                "Book Container", menuButtons);
        }

        private void back() {
                if (ExporterDashboard.getInstance() != null) {
                        ExporterDashboard.getInstance().switchPage("Dashboard");
                } else {
                        new ExporterDashboard().start(ExporterDashboard.dashboardStage);
                }
        }

        private HBox createHeader(String pageTitle, String breadcrumb) {
                return DashboardLayoutHelper.createHeader(ExporterDashboard.dashboardStage, pageTitle, breadcrumb,
                                "Exporter", () -> {
                                        Logout page = new Logout();
                                        page.showLogout();
                                }, null);
        }
}