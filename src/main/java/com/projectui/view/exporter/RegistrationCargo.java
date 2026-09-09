package com.projectui.view.exporter;

import javafx.geometry.Insets;

import javafx.geometry.Pos;

import javafx.scene.Scene;

import javafx.scene.control.*;

import javafx.scene.layout.*;

import javafx.scene.paint.Color;

import javafx.scene.image.Image;

import javafx.scene.image.ImageView;

import java.util.Random;
import java.util.Map;

import com.projectui.controller.ExpCargoController;

public class RegistrationCargo {

        // =========================================================

        // COLORS

        // =========================================================

        private static final String BLUE = "#1769FF";

        private static final String DARK = "#08111F";

        private static final String BG = "#F4F7FB";

        private static final String TEXT = "#172033";

        private static final String MUTED = "#8A95A7";

        // =========================================================

        // FIELDS

        // =========================================================

        private TextField cargoName;

        private TextField quantity;

        private TextField weight;

        private TextField cargoValue;

        private ComboBox<String> cargoType;

        private ComboBox<String> originPort;

        private ComboBox<String> destinationPort;

        private ComboBox<String> priority;

        private ComboBox<String> importerSelect;

        private final java.util.List<Map<String, Object>> loadedImporters = new java.util.ArrayList<>();

        private Label successMessage;

        private Label cargoIdLabel;

        private ProgressBar progressBar;

        private Button registerButton;

        private boolean registrationInProgress;

        // =========================================================

        // SCENE

        // =========================================================

        public Scene getScene() {

                BorderPane root = new BorderPane();

                root.setStyle(

                                "-fx-background-color:" + BG + ";"

                );

                root.setTop(createHeader("Register Cargo", "DockTech / Exporter / Register Cargo"));
                root.setLeft(createSidebar());

                root.setCenter(getScrollableContent());

                Scene sc = new Scene(root);
                com.projectui.view.login.LoginPage.applyInvisibleScrollbar(sc);
                return sc;
        }

        public ScrollPane getScrollableContent() {
                ScrollPane scrollPane = new ScrollPane(getRegistrationCargoPage());
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(false);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + BG
                                + "; -fx-border-color:transparent;");
                return scrollPane;
        }

        // =========================================================

        // MAIN PAGE

        // =========================================================

        public VBox getRegistrationCargoPage() {

                VBox main = new VBox();

                main.setPadding(new Insets(24, 24, 24, 24));

                main.setStyle("-fx-background-color:" + BG + ";");

                HBox card = new HBox();

                card.setMaxWidth(Double.MAX_VALUE);

                card.setMaxHeight(Double.MAX_VALUE);

                card.setStyle(

                                "-fx-background-color:white;" +

                                                "-fx-background-radius:22;" +

                                                "-fx-border-color:#DCE5F2;" +

                                                "-fx-border-radius:22;"

                );

                // =====================================================

                // LEFT ILLUSTRATION PANEL

                // =====================================================

                VBox illustrationPanel = new VBox(10);

                illustrationPanel.setPrefWidth(300);

                illustrationPanel.setMinWidth(300);

                illustrationPanel.setAlignment(Pos.TOP_CENTER);

                illustrationPanel.setPadding(new Insets(30, 18, 28, 18));

                illustrationPanel.setStyle(

                                "-fx-background-color:#F3F8FF;" +

                                                "-fx-background-radius:22 0 0 22;"

                );

                ImageView cargoImage = new ImageView();

                try {

                        var imageUrl = getClass().getResource("/Assets\\images\\cargo_crane_hd.png");

                        if (imageUrl != null) {

                                cargoImage.setImage(new Image(imageUrl.toExternalForm()));

                        }

                } catch (Exception ignored) {

                        // Keep the page usable if the optional illustration is missing.

                }

                cargoImage.setFitWidth(250);

                cargoImage.setFitHeight(500);

                cargoImage.setPreserveRatio(true);

                cargoImage.setSmooth(true);

                VBox.setVgrow(cargoImage, Priority.ALWAYS);

                Label smartCargo = new Label("Smart Cargo");

                smartCargo.setStyle(

                                "-fx-text-fill:" + TEXT + ";" +

                                                "-fx-font-size:24;" +

                                                "-fx-font-weight:bold;"

                );

                Label smartSubtitle = new Label(

                                "Register your cargo\nand start the shipping workflow."

                );

                smartSubtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

                smartSubtitle.setAlignment(Pos.CENTER);

                smartSubtitle.setStyle(

                                "-fx-text-fill:#7C8DA6;" +

                                                "-fx-font-size:16;" +

                                                "-fx-line-spacing:4;"

                );

                illustrationPanel.getChildren().addAll(

                                cargoImage,

                                smartCargo,

                                smartSubtitle

                );

                // =====================================================

                // RIGHT FORM PANEL

                // =====================================================

                VBox formPanel = new VBox(18);

                formPanel.setPadding(new Insets(32, 34, 30, 34));

                HBox.setHgrow(formPanel, Priority.ALWAYS);

                HBox cardHeader = new HBox(14);

                cardHeader.setAlignment(Pos.CENTER_LEFT);

                Label icon = new Label("📋");

                icon.setMinSize(58, 58);

                icon.setAlignment(Pos.CENTER);

                icon.setStyle(

                                "-fx-background-color:#EEF4FF;" +

                                                "-fx-background-radius:16;" +

                                                "-fx-text-fill:" + BLUE + ";" +

                                                "-fx-font-size:25;" +

                                                "-fx-font-weight:bold;"

                );

                VBox titleBox = new VBox(5);

                Label cardTitle = new Label("Cargo Registration");

                cardTitle.setStyle(

                                "-fx-text-fill:" + TEXT + ";" +

                                                "-fx-font-size:25;" +

                                                "-fx-font-weight:bold;"

                );

                Label cardSubtitle = new Label(

                                "Enter the required cargo details to continue the shipping workflow."

                );

                cardSubtitle.setStyle(

                                "-fx-text-fill:#7C8DA6;" +

                                                "-fx-font-size:16;"

                );

                titleBox.getChildren().addAll(cardTitle, cardSubtitle);

                cardHeader.getChildren().addAll(icon, titleBox);

                // =====================================================

                // FORM GRID

                // =====================================================

                GridPane form = new GridPane();

                form.setHgap(32);

                form.setVgap(18);

                form.setMaxWidth(Double.MAX_VALUE);

                cargoName = new TextField("Electronics");

                cargoName.setPromptText("Enter cargo name");

                quantity = new TextField("2");

                quantity.setPromptText("Enter quantity");

                weight = new TextField("120");

                weight.setPromptText("Weight in Tons");

                cargoValue = new TextField();

                cargoValue.setPromptText("Estimated value (Calculated)");
                cargoValue.setEditable(false);
                cargoValue.setFocusTraversable(false);

                cargoType = new ComboBox<>();

                cargoType.getItems().addAll(

                                "General Cargo", "Electronics", "Machinery",

                                "Textiles", "Automobile Parts", "Food Products",

                                "Hazardous Goods"

                );

                cargoType.setValue("General Cargo");

                originPort = new ComboBox<>();
                destinationPort = new ComboBox<>();
                loadDynamicPorts();

                priority = new ComboBox<>();

                priority.getItems().addAll("Normal", "High", "Urgent");

                priority.setValue("Normal");

                importerSelect = new ComboBox<>();
                loadDynamicImporters();

                quantity.textProperty().addListener((obs, oldV, newV) -> updateEstimatedValue());
                weight.textProperty().addListener((obs, oldV, newV) -> updateEstimatedValue());
                cargoType.setOnAction(e -> updateEstimatedValue());
                originPort.setOnAction(e -> updateEstimatedValue());
                destinationPort.setOnAction(e -> updateEstimatedValue());
                String enteredValue = cargoValue.getText() != null ? cargoValue.getText().trim() : "";
                if (!enteredValue.isBlank()) {
                        try {
                                double manualValue = Double.parseDouble(enteredValue.replaceAll("[^0-9.]", ""));
                                if (manualValue >= 50000.0) {
                                        showError("Estimated Value must be less than ₹50,000.");
                                        cargoValue.requestFocus();
                                        return main;
                                }
                        } catch (Exception ex) {
                                showError("Estimated Value must be a valid amount less than ₹50,000.");
                                cargoValue.requestFocus();
                                return main;
                        }
                }

                updateEstimatedValue();

                addField(form, "Cargo Name", cargoName, 0, 0);

                addField(form, "Cargo Type", cargoType, 1, 0);

                addField(form, "Quantity", quantity, 0, 1);

                addField(form, "Weight (tons)", weight, 1, 1);

                addField(form, "Origin Port", originPort, 0, 2);

                addField(form, "Destination Port", destinationPort, 1, 2);

                addField(form, "Estimated Value (₹)", cargoValue, 0, 3);

                addField(form, "Priority", priority, 1, 3);

                addField(form, "Assign Importer (Consignee)", importerSelect, 0, 4);
                GridPane.setColumnSpan(form.getChildren().get(form.getChildren().size() - 1), 2);

                // =====================================================

                // PROGRESS BAR

                // =====================================================

                progressBar = new ProgressBar(0);

                progressBar.setPrefHeight(5);

                progressBar.setMaxWidth(Double.MAX_VALUE);

                progressBar.setVisible(false);

                progressBar.setManaged(false);

                progressBar.setStyle("-fx-accent:" + BLUE + ";");

                GridPane.setColumnSpan(progressBar, 2);

                form.add(progressBar, 0, 5);

                // =====================================================

                // BUTTONS

                // =====================================================

                HBox buttons = new HBox(18);

                buttons.setAlignment(Pos.CENTER_LEFT);

                registerButton = new Button("Register Cargo");

                registerButton.setPrefHeight(52);

                registerButton.setMaxWidth(Double.MAX_VALUE);

                HBox.setHgrow(registerButton, Priority.ALWAYS);

                registerButton.setStyle(

                                "-fx-background-color:" + BLUE + ";" +

                                                "-fx-text-fill:white;" +

                                                "-fx-background-radius:11;" +

                                                "-fx-font-size:17;" +

                                                "-fx-font-weight:bold;" +

                                                "-fx-cursor:hand;"

                );

                registerButton.setOnAction(e -> registerCargo());

                Button clearButton = new Button("Clear Form");

                clearButton.setPrefHeight(52);

                clearButton.setPrefWidth(165);

                clearButton.setStyle(

                                "-fx-background-color:white;" +

                                                "-fx-text-fill:#25324A;" +

                                                "-fx-border-color:" + BLUE + ";" +

                                                "-fx-border-width:1.5;" +

                                                "-fx-border-radius:11;" +

                                                "-fx-background-radius:11;" +

                                                "-fx-font-size:17;" +

                                                "-fx-font-weight:bold;" +

                                                "-fx-cursor:hand;"

                );

                clearButton.setOnAction(e -> clearForm());

                buttons.getChildren().addAll(registerButton, clearButton);

                // =====================================================

                // SUCCESS BOX

                // =====================================================

                successMessage = new Label("✓  Cargo registered successfully");

                successMessage.setStyle(

                                "-fx-text-fill:#118A55;" +

                                                "-fx-font-size:16;" +

                                                "-fx-font-weight:bold;"

                );

                cargoIdLabel = new Label("Cargo ID: —");

                cargoIdLabel.setStyle(

                                "-fx-text-fill:#55776A;" +

                                                "-fx-font-size:13;"

                );

                VBox successBox = new VBox(6, successMessage, cargoIdLabel);

                successBox.setPadding(new Insets(15));

                successBox.setStyle(

                                "-fx-background-color:#EAF9F2;" +

                                                "-fx-background-radius:12;" +

                                                "-fx-border-color:#BDE8D5;" +

                                                "-fx-border-radius:12;"

                );

                successBox.setVisible(false);

                successBox.setManaged(false);

                // =====================================================

                // INFO BOX

                // =====================================================

                HBox infoBox = new HBox(12);

                infoBox.setAlignment(Pos.CENTER_LEFT);

                infoBox.setPadding(new Insets(14));

                infoBox.setStyle(

                                "-fx-background-color:#F1F6FF;" +

                                                "-fx-background-radius:12;"

                );

                Label infoIcon = new Label("ⓘ");

                infoIcon.setStyle(

                                "-fx-text-fill:" + BLUE + ";" +

                                                "-fx-font-size:19;"

                );

                Label infoText = new Label(

                                "After registration, you can book a container and upload the required documents."

                );

                infoText.setWrapText(true);

                infoText.setStyle(

                                "-fx-text-fill:#526986;" +

                                                "-fx-font-size:13;"

                );

                HBox.setHgrow(infoText, Priority.ALWAYS);

                infoBox.getChildren().addAll(infoIcon, infoText);

                formPanel.getChildren().addAll(

                                cardHeader,

                                form,

                                buttons,

                                successBox,

                                infoBox

                );

                card.getChildren().addAll(illustrationPanel, formPanel);

                VBox.setVgrow(card, Priority.ALWAYS);

                main.getChildren().add(card);

                VBox.setVgrow(card, Priority.ALWAYS);

                return main;

        }

        // =========================================================

        // ADD FIELD

        // =========================================================

        private void addField(

                        GridPane grid,

                        String labelText,

                        Control control,

                        int column,

                        int row) {

                VBox box = new VBox(7);

                Label label = new Label(labelText);

                label.setStyle(

                                "-fx-text-fill:#24324A;" +

                                                "-fx-font-size:13;" +

                                                "-fx-font-weight:bold;"

                );

                HBox inputBox = new HBox(10);

                inputBox.setAlignment(Pos.CENTER_LEFT);

                inputBox.setPrefHeight(52);

                inputBox.setMaxWidth(Double.MAX_VALUE);

                inputBox.setStyle(

                                "-fx-background-color:white;" +

                                                "-fx-border-color:#D8E2EF;" +

                                                "-fx-border-radius:11;" +

                                                "-fx-background-radius:11;"

                );

                Label fieldIcon = new Label(getFieldIcon(labelText));

                fieldIcon.setMinWidth(30);

                fieldIcon.setAlignment(Pos.CENTER);

                fieldIcon.setStyle(

                                "-fx-text-fill:" + BLUE + ";" +

                                                "-fx-font-size:17;"

                );

                control.setPrefHeight(48);

                control.setMaxWidth(Double.MAX_VALUE);

                control.setStyle(

                                "-fx-background-color:transparent;" +

                                                "-fx-border-color:transparent;" +

                                                "-fx-font-size:16;" +

                                                "-fx-text-fill:#24324A;"

                );

                HBox.setHgrow(control, Priority.ALWAYS);

                inputBox.getChildren().addAll(fieldIcon, control);

                box.getChildren().addAll(label, inputBox);

                grid.add(box, column, row);

                GridPane.setHgrow(box, Priority.ALWAYS);

                GridPane.setFillWidth(box, true);

        }

        private String getFieldIcon(String label) {

                if (label.contains("Cargo Name"))
                        return "▣";

                if (label.contains("Cargo Type"))
                        return "◇";

                if (label.contains("Quantity"))
                        return "♙";

                if (label.contains("Weight"))
                        return "♙";

                if (label.contains("Port"))
                        return "⌖";

                if (label.contains("Estimated"))
                        return "₹";

                if (label.contains("Priority"))
                        return "⚑";

                if (label.contains("Importer"))
                        return "👤";

                return "•";

        }

        // =========================================================

        // REGISTER CARGO

        // =========================================================

        private void registerCargo() {

                if (registrationInProgress)
                        return;

                if (cargoName.getText().trim().isEmpty()) {

                        showError(

                                        "Please enter cargo name."

                        );

                        cargoName.requestFocus();

                        return;

                }

                if (!isNumber(quantity.getText())) {

                        showError(

                                        "Quantity must contain numbers only."

                        );

                        quantity.requestFocus();

                        return;

                }

                if (!isNumber(weight.getText())) {

                        showError(

                                        "Weight must contain numbers only."

                        );

                        weight.requestFocus();

                        return;

                }

                if (originPort.getValue()

                                .equals(destinationPort.getValue())) {

                        showError(

                                        "Origin and destination ports cannot be the same."

                        );

                        return;

                }

                double estimatedValue;
                try {
                        estimatedValue = Double.parseDouble(cargoValue.getText().replaceAll("[^0-9.]", "").trim());
                } catch (Exception ex) {
                        showError("Estimated Value must be a valid amount less than ₹50,000.");
                        cargoValue.requestFocus();
                        return;
                }
                if (estimatedValue >= 50000.0) {
                        showError("Estimated Value must be less than ₹50,000.");
                        cargoValue.requestFocus();
                        return;
                }
                updateEstimatedValue();

                registrationInProgress = true;
                registerButton.setDisable(true);

                // =====================================================

                // PROGRESS

                // =====================================================

                progressBar.setVisible(true);

                progressBar.setProgress(0.0);

                new Thread(() -> {

                        try {

                                Thread.sleep(300);

                                javafx.application.Platform.runLater(

                                                () -> progressBar.setProgress(0.35)

                                );

                                Thread.sleep(300);

                                javafx.application.Platform.runLater(

                                                () -> progressBar.setProgress(0.70)

                                );

                                Thread.sleep(300);

                                javafx.application.Platform.runLater(() -> {

                                        try {

                                                progressBar.setProgress(1.0);

                                                ExpCargoController controller = new ExpCargoController();

                                                double calcVal = 0;
                                                try {
                                                        calcVal = Double.parseDouble(cargoValue.getText()
                                                                        .replaceAll("[^0-9.]", "").trim());
                                                } catch (Exception ignored) {
                                                }

                                                int selIdx = (importerSelect != null)
                                                                ? importerSelect.getSelectionModel().getSelectedIndex()
                                                                : -1;
                                                Map<String, Object> selImp = (selIdx >= 0
                                                                && selIdx < loadedImporters.size())
                                                                                ? loadedImporters.get(selIdx)
                                                                                : null;
                                                String impName = null, impEmail = null, impId = null, impComp = null;
                                                if (selImp != null) {
                                                        impName = selImp.get("name") != null
                                                                        ? selImp.get("name").toString().trim()
                                                                        : (selImp.get("fullName") != null
                                                                                        ? selImp.get("fullName")
                                                                                                        .toString()
                                                                                                        .trim()
                                                                                        : null);
                                                        impEmail = selImp.get("email") != null
                                                                        ? selImp.get("email").toString().trim()
                                                                        : null;
                                                        impId = selImp.get("uid") != null
                                                                        ? selImp.get("uid").toString().trim()
                                                                        : null;
                                                        impComp = selImp.get("company") != null
                                                                        ? selImp.get("company").toString().trim()
                                                                        : null;
                                                } else if (importerSelect != null && importerSelect.getValue() != null
                                                                && !importerSelect.getValue()
                                                                                .startsWith("No Registered")) {
                                                        impName = importerSelect.getValue().trim();
                                                }

                                                String id = controller.addCargo(

                                                                cargoName.getText().trim(),

                                                                cargoType.getValue(),

                                                                Integer.parseInt(quantity.getText().trim()),

                                                                Double.parseDouble(weight.getText().trim()),

                                                                calcVal,

                                                                originPort.getValue(),

                                                                destinationPort.getValue(),

                                                                priority.getValue(),

                                                                impName,

                                                                impEmail,

                                                                impId,

                                                                impComp

                                                );

                                                cargoIdLabel.setText("Cargo ID: " + id);

                                                successMessage.setText("✓  Cargo registered successfully");

                                                successMessage.getParent().setVisible(true);

                                                successMessage.getParent().setManaged(true);

                                        } catch (Exception ex) {

                                                progressBar.setVisible(false);

                                                registrationInProgress = false;
                                                registerButton.setDisable(false);

                                                showError("Failed to save: " + ex.getMessage());

                                        }

                                });

                        } catch (InterruptedException ex) {

                                Thread.currentThread()

                                                .interrupt();

                        }

                }).start();

        }

        private void updateEstimatedValue() {
                try {
                        String qtyStr = quantity != null && quantity.getText() != null ? quantity.getText().trim()
                                        : "1";
                        String wtStr = weight != null && weight.getText() != null ? weight.getText().trim() : "1000";
                        int qty = 1;
                        double wt = 1000.0;
                        try {
                                qty = Integer.parseInt(qtyStr.replaceAll("[^0-9]", ""));
                        } catch (Exception ignored) {
                        }
                        try {
                                wt = Double.parseDouble(wtStr.replaceAll("[^0-9.]", ""));
                        } catch (Exception ignored) {
                        }
                        if (qty <= 0)
                                qty = 1;
                        if (wt <= 0)
                                wt = 1000.0;

                        String type = cargoType != null && cargoType.getValue() != null ? cargoType.getValue()
                                        : "General Cargo";
                        String orig = originPort != null && originPort.getValue() != null ? originPort.getValue()
                                        : "Port of Mumbai";
                        String dest = destinationPort != null && destinationPort.getValue() != null
                                        ? destinationPort.getValue()
                                        : "Port of Singapore";

                        double calculatedValue = com.projectui.service.FreightCalculationService.getInstance()
                                        .calculateEstimatedCargoValue(type, wt, qty, orig, dest);
                        if (cargoValue != null) {
                                cargoValue.setText("₹ " + String.format("%,.0f", calculatedValue));
                        }
                } catch (Exception ignored) {
                }
        }

        private void loadDynamicPorts() {
                java.util.List<String> validPorts = new java.util.ArrayList<>(
                                com.projectui.view.admin.AdminDataStore.getInstance().getApprovedPortNames());
                if (validPorts.isEmpty()) {
                        validPorts.add("Port of Mumbai");
                        validPorts.add("Port of Chennai");
                        validPorts.add("Port of Vizag");
                        validPorts.add("Port of Kolkata");
                        validPorts.add("Cochin Port");
                }
                originPort.getItems().setAll(validPorts);
                originPort.setValue(validPorts.get(0));

                java.util.List<String> destList = new java.util.ArrayList<>(validPorts);
                for (String intl : java.util.List.of("International", "Regional", "local")) {
                        if (!destList.contains(intl)) {
                                destList.add(intl);
                        }
                }
                destinationPort.getItems().setAll(destList);
                destinationPort.setValue(destList.size() > 1 ? destList.get(1) : destList.get(0));
        }

        private void loadDynamicImporters() {
                if (importerSelect == null)
                        return;
                loadedImporters.clear();
                java.util.List<Map<String, Object>> importers = com.projectui.dao.UserDao.getUsersByRoles("Importer");
                java.util.List<String> displayList = new java.util.ArrayList<>();
                if (importers != null && !importers.isEmpty()) {
                        for (Map<String, Object> imp : importers) {
                                if (imp == null)
                                        continue;
                                loadedImporters.add(imp);
                                String name = imp.get("name") != null ? imp.get("name").toString().trim() : "";
                                if (name.isBlank() && imp.get("fullName") != null)
                                        name = imp.get("fullName").toString().trim();
                                String email = imp.get("email") != null ? imp.get("email").toString().trim() : "";
                                String company = imp.get("company") != null ? imp.get("company").toString().trim() : "";

                                String label = !name.isBlank() ? name : (!company.isBlank() ? company : email);
                                if (!company.isBlank() && !company.equalsIgnoreCase(label)) {
                                        label = label + " (" + company + ")";
                                } else if (!email.isBlank() && !label.contains("@")) {
                                        label = label + " <" + email + ">";
                                }
                                displayList.add(label);
                        }
                }
                if (displayList.isEmpty()) {
                        importerSelect.getItems().setAll("No Registered Importers");
                        importerSelect.setValue("No Registered Importers");
                } else {
                        importerSelect.getItems().setAll(displayList);
                        importerSelect.setValue(displayList.get(0));
                }
        }

        // =========================================================

        // CLEAR FORM

        // =========================================================

        private void clearForm() {

                registrationInProgress = false;
                if (registerButton != null)
                        registerButton.setDisable(false);

                cargoName.clear();

                quantity.setText("1");

                weight.setText("1000");

                cargoType.setValue(

                                "General Cargo"

                );

                loadDynamicPorts();
                loadDynamicImporters();

                priority.setValue(

                                "Normal"

                );

                updateEstimatedValue();

                progressBar.setProgress(0);

                progressBar.setVisible(false);

                if (successMessage.getParent() != null) {

                        successMessage.getParent()

                                        .setVisible(false);

                        successMessage.getParent()

                                        .setManaged(false);

                }

        }

        // =========================================================

        // NUMBER VALIDATION

        // =========================================================

        private boolean isNumber(String value) {

                try {

                        Double.parseDouble(

                                        value.trim()

                        );

                        return true;

                } catch (Exception e) {

                        return false;

                }

        }

        // =========================================================

        // ERROR

        // =========================================================

        private void showError(String message) {

                Alert alert = new Alert(

                                Alert.AlertType.ERROR

                );

                alert.setTitle(

                                "Cargo Registration"

                );

                alert.setHeaderText(

                                "Invalid Information"

                );

                alert.setContentText(

                                message

                );

                alert.showAndWait();

        }

        // =========================================================

        // INFO

        // =========================================================

        private void showInfo(

                        String title,

                        String message) {

                Alert alert = new Alert(

                                Alert.AlertType.INFORMATION

                );

                alert.setTitle(title);

                alert.setHeaderText(null);

                alert.setContentText(message);

                alert.showAndWait();

        }

        // =========================================================

        // SIDEBAR
        // =========================================================

        private VBox createSidebar() {
                java.util.Map<String, Button> menuButtons = new java.util.LinkedHashMap<>();
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
                        }
                });
                book.setOnAction(e -> {
                        if (ExporterDashboard.getInstance() != null) {
                                ExporterDashboard.getInstance().switchPage("Book Container");
                        } else {
                                BookContainer page = new BookContainer();
                                com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage,
                                                page.getBookContainerScene(this::back).getRoot());
                        }
                });

                upload.setOnAction(e -> {
                        if (ExporterDashboard.getInstance() != null) {
                                ExporterDashboard.getInstance().switchPage("Upload Document");
                        } else {
                                UploadDocument page = new UploadDocument();
                                com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage,
                                                page.getUploadDocumentScene(this::back).getRoot());
                        }
                });

                payment.setOnAction(e -> {
                        if (ExporterDashboard.getInstance() != null) {
                                ExporterDashboard.getInstance().switchPage("Make Payment");
                        } else {
                                MakePayment page = new MakePayment();
                                com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage,
                                                page.getMakePaymentScene(this::back).getRoot());
                        }
                });

                track.setOnAction(e -> {
                        if (ExporterDashboard.getInstance() != null) {
                                ExporterDashboard.getInstance().switchPage("Track Shipment");
                        } else {
                                TrackShipment page = new TrackShipment();
                                com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage,
                                                page.getTrackShipmentScene(this::back).getRoot());
                        }
                });

                return com.projectui.view.login.DashboardLayoutHelper.createSidebar(ExporterDashboard.dashboardStage,
                                "Exporter", "Register Cargo", menuButtons);
        }

        private void back() {
                if (ExporterDashboard.getInstance() != null) {
                        ExporterDashboard.getInstance().switchPage("Dashboard");
                } else {
                        new ExporterDashboard().start(ExporterDashboard.dashboardStage);
                }
        }

        private HBox createHeader(String pageTitle, String breadcrumb) {
                Runnable logoutAction = () -> {
                        Logout page = new Logout();
                        page.showLogout();
                };
                return com.projectui.view.login.DashboardLayoutHelper.createHeader(
                                ExporterDashboard.dashboardStage,
                                pageTitle,
                                breadcrumb,
                                "Exporter",
                                logoutAction,
                                null);
        }
}
