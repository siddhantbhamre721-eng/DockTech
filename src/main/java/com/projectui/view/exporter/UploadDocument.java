package com.projectui.view.exporter;

import com.projectui.dao.CargoDao;
import com.projectui.dao.ContainerDao;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UploadDocument {

    private Scene scene;

    // Design Tokens
    private static final String BG_COLOR = "#F4F8FC";
    private static final String CARD_BG = "#FFFFFF";
    private static final String TEXT_PRIMARY = "#0F172A";
    private static final String TEXT_MUTED = "#64748B";
    private static final String PRIMARY_BLUE = "#1769FF";
    private static final String BORDER_COLOR = "#E2E8F0";

    private final CargoDao cargoDao = new CargoDao();

    public Scene getUploadDocumentScene() {
        return getUploadDocumentScene(null);
    }

    public Scene getUploadDocumentScene(Runnable callbackAction) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");
        root.setTop(createHeader("Upload Document", "DockTech / Exporter / Upload Document"));
        root.setLeft(createSidebar());

        root.setCenter(getScrollableContent());

        scene = new Scene(root, 1550, 800);
        com.projectui.view.login.LoginPage.applyInvisibleScrollbar(scene);
        return scene;
    }

    public ScrollPane getScrollableContent() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 32, 36, 32));
        content.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // Header Banner
        VBox headerBanner = new VBox(6);
        Text title = new Text("Upload Document");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text subtitle = new Text(
                "Upload and manage mandatory shipping, customs clearance, and commercial documentation.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-fill: " + TEXT_MUTED + ";");
        headerBanner.getChildren().addAll(title, subtitle);

        // Workspace Layout (2 Columns: Upload Left, Details & Checklist Right)
        HBox workspace = new HBox(24);

        // =========================================================
        // LEFT COLUMN: DOCUMENT UPLOAD CARD (58% Width)
        // =========================================================
        VBox uploadCard = createCard();
        HBox.setHgrow(uploadCard, Priority.ALWAYS);

        VBox uploadHeader = new VBox(4);
        Text uploadTitle = new Text("📄  Document Upload Management");
        uploadTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text uploadSub = new Text("Select verified cargo and upload associated customs/shipping certificates.");
        uploadSub.setStyle("-fx-font-size: 13px; -fx-fill: " + TEXT_MUTED + ";");
        uploadHeader.getChildren().addAll(uploadTitle, uploadSub);

        // Form Fields
        CargoDao cargoDao = new CargoDao();
        ContainerDao containerDao = new ContainerDao();
        List<Map<String, Object>> allCargos = cargoDao.getAllCargos();

        ComboBox<String> cargoSelect = new ComboBox<>();
        if (allCargos != null && !allCargos.isEmpty()) {
            for (Map<String, Object> c : allCargos) {
                String cId = c.get("cargoId") != null ? c.get("cargoId").toString() : "CGO";
                String cName = c.get("cargoName") != null ? c.get("cargoName").toString() : "";
                cargoSelect.getItems().add(cId + " — " + cName);
            }
            cargoSelect.setValue(cargoSelect.getItems().get(0));
        } else {
            cargoSelect.getItems().add("No Registered Cargo");
            cargoSelect.setValue("No Registered Cargo");
        }

        ComboBox<String> docType = new ComboBox<>();
        docType.getItems().addAll(
                "Commercial Invoice",
                "Packing List",
                "Bill of Lading / Shipping Order",
                "Certificate of Origin",
                "Customs Declaration (Bill of Entry)");
        docType.setValue("Bill of Lading / Shipping Order");

        TextField docRefNumber = new TextField();
        docRefNumber.setPromptText("e.g. DOC-EXP-2026-0981");
        docRefNumber.setPrefHeight(42);
        docRefNumber.setStyle(
                "-fx-background-color: #F8FAFC; -fx-border-color: #CBD5E1; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px 12px;");

        // Drag & Drop / File Chooser Box
        VBox dropZone = new VBox(10);
        dropZone.setAlignment(Pos.CENTER);
        dropZone.setPadding(new Insets(24));
        dropZone.setStyle(
                "-fx-background-color: #F8FAFC;" +
                        "-fx-border-color: #94A3B8;" +
                        "-fx-border-style: dashed;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-background-radius: 12px;");

        Label dropIcon = new Label("☁️");
        dropIcon.setStyle("-fx-font-size: 32px;");

        Text dropText = new Text("Drag and drop your file here, or browse");
        dropText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        Text dropHint = new Text("Supports PDF, PNG, JPG, DOCX (Max: 25MB)");
        dropHint.setStyle("-fx-font-size: 12px; -fx-fill: " + TEXT_MUTED + ";");

        Button chooseFileBtn = new Button("Browse File");
        chooseFileBtn.setPrefHeight(34);
        chooseFileBtn.setStyle(
                "-fx-background-color: " + PRIMARY_BLUE + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 6px 18px;");

        Label chooseFileText = new Label("No file chosen");
        chooseFileText.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TEXT_MUTED + "; -fx-font-style: italic;");

        dropZone.getChildren().addAll(dropIcon, dropText, dropHint, chooseFileBtn, chooseFileText);

        final java.util.concurrent.atomic.AtomicReference<String> selectedFileName = new java.util.concurrent.atomic.AtomicReference<>(
                "");
        chooseFileBtn.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Document File");
            File file = chooser.showOpenDialog(ExporterDashboard.dashboardStage);
            if (file != null) {
                selectedFileName.set(file.getName());
                chooseFileText.setText("Selected: " + file.getName());
                if (docRefNumber.getText() == null || docRefNumber.getText().isBlank()) {
                    docRefNumber.setText("DOC-" + Math.abs(file.getName().hashCode() % 89999 + 10000));
                }
            }
        });

        // Upload Button
        Button uploadButton = new Button("⬆️  Upload Document");
        uploadButton.setMaxWidth(Double.MAX_VALUE);
        uploadButton.setPrefHeight(46);
        uploadButton.setStyle(
                "-fx-background-color: " + PRIMARY_BLUE + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 9px;" +
                        "-fx-cursor: hand;");

        // Upload Status Log
        VBox statusLog = new VBox(6);
        statusLog.setPadding(new Insets(14));
        statusLog.setStyle(
                "-fx-background-color: #F8FAFC; -fx-background-radius: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px;");

        Label log1 = new Label("✓  Commercial Invoice — Uploaded");
        log1.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label log2 = new Label("✓  Packing List — Uploaded");
        log2.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label log3 = new Label("⚠️  Bill of Lading / Shipping Order — Pending");
        log3.setStyle("-fx-text-fill: #D97706; -fx-font-size: 12px; -fx-font-weight: bold;");

        statusLog.getChildren().addAll(log1, log2, log3);

        uploadButton.setOnAction(event -> {
            String selectedCargoEntry = cargoSelect.getValue();
            String cId = "CGO-1024";
            if (selectedCargoEntry != null) {
                cId = selectedCargoEntry.split(" — ")[0].trim();
            }
            String dType = docType.getValue() != null ? docType.getValue() : "Shipping Document";
            String dRef = docRefNumber.getText() != null && !docRefNumber.getText().isBlank()
                    ? docRefNumber.getText().trim()
                    : ("DOC-" + System.currentTimeMillis() % 100000);
            String fName = selectedFileName.get().isBlank() ? (dType.replaceAll("\\s+", "_") + ".pdf")
                    : selectedFileName.get();

            // Save document into Cargo record
            cargoDao.updateCargoStatus(cId, "Documents Uploaded");
            log3.setText("✓  " + dType + " (" + dRef + ") — Uploaded");
            log3.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px; -fx-font-weight: bold;");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Document Uploaded");
            alert.setHeaderText("Upload Successful");
            alert.setContentText("The document '" + fName + "' (" + dRef
                    + ") has been uploaded and associated with Cargo " + cId + " for customs clearance.");
            alert.showAndWait();
        });

        uploadCard.getChildren().addAll(
                uploadHeader,
                createFormField("Selected Cargo", cargoSelect),
                createFormField("Document Classification", docType),
                createFormField("Document Reference Number", docRefNumber),
                dropZone,
                uploadButton,
                statusLog);

        // =========================================================
        // RIGHT COLUMN: CARGO DETAILS & CHECKLIST (42% Width)
        // =========================================================
        VBox rightCol = new VBox(22);
        rightCol.setPrefWidth(400);
        rightCol.setMinWidth(360);

        // 1. Cargo Reference Card
        VBox cargoRefCard = createCard();
        Text refTitle = new Text("Cargo Specification");
        refTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        VBox refDetails = new VBox(10);

        // Populate dynamic details for current selected cargo
        Map<String, Object> initialCargo = (allCargos != null && !allCargos.isEmpty()) ? allCargos.get(0)
                : new HashMap<>();
        String initCId = initialCargo.get("cargoId") != null ? initialCargo.get("cargoId").toString() : "N/A";
        String initCName = initialCargo.get("cargoName") != null ? initialCargo.get("cargoName").toString() : "N/A";
        String initOrigin = initialCargo.get("originPort") != null ? initialCargo.get("originPort").toString() : "N/A";
        String initDest = initialCargo.get("destinationPort") != null ? initialCargo.get("destinationPort").toString()
                : "N/A";
        String initWeight = initialCargo.get("weight") != null ? initialCargo.get("weight").toString() + " kg" : "N/A";

        Map<String, Object> booking = containerDao.getBookingByCargoId(initCId);
        String containerNo = booking != null && booking.get("bookingId") != null ? booking.get("bookingId").toString()
                : "Pending Booking";
        String containerType = booking != null && booking.get("containerType") != null
                ? booking.get("containerType").toString()
                : "Standard Container";

        refDetails.getChildren().addAll(
                createDetailRow("Cargo Identifier", initCId),
                createDetailRow("Container Assignment", containerNo + " (" + containerType + ")"),
                createDetailRow("Port of Loading", initOrigin),
                createDetailRow("Port of Discharge", initDest),
                createDetailRow("Commodity", initCName),
                createDetailRow("Declared Weight", initWeight),
                createDetailRow("Customs Status",
                        initialCargo.get("status") != null ? initialCargo.get("status").toString() : "Registered"));

        cargoSelect.setOnAction(e -> {
            String selectedCargoEntry = cargoSelect.getValue();
            if (selectedCargoEntry != null) {
                String cId = selectedCargoEntry.split(" — ")[0].trim();
                Map<String, Object> c = cargoDao.getCargo(cId);
                if (c != null) {
                    refDetails.getChildren().clear();
                    String cName = c.get("cargoName") != null ? c.get("cargoName").toString() : "General Cargo";
                    String org = c.get("originPort") != null ? c.get("originPort").toString() : "N/A";
                    String dst = c.get("destinationPort") != null ? c.get("destinationPort").toString() : "N/A";
                    String wt = c.get("weight") != null ? c.get("weight").toString() + " kg" : "N/A";
                    String st = c.get("status") != null ? c.get("status").toString() : "Active";
                    Map<String, Object> b = containerDao.getBookingByCargoId(cId);
                    String bId = b != null && b.get("bookingId") != null ? b.get("bookingId").toString()
                            : "Pending Booking";
                    String bType = b != null && b.get("containerType") != null ? b.get("containerType").toString()
                            : "Standard Container";

                    refDetails.getChildren().addAll(
                            createDetailRow("Cargo Identifier", cId),
                            createDetailRow("Container Assignment", bId + " (" + bType + ")"),
                            createDetailRow("Port of Loading", org),
                            createDetailRow("Port of Discharge", dst),
                            createDetailRow("Commodity", cName),
                            createDetailRow("Declared Weight", wt),
                            createDetailRow("Customs Status", st));
                }
            }
        });

        cargoRefCard.getChildren().addAll(refTitle, refDetails);

        // 2. Mandatory Clearance Checklist Card
        VBox checklistCard = createCard();
        Text checkTitle = new Text("Mandatory Clearance Checklist");
        checkTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        VBox checkList = new VBox(10);
        checkList.getChildren().addAll(
                createChecklistItem("Commercial Invoice", "Verified", true),
                createChecklistItem("Packing List", "Verified", true),
                createChecklistItem("Bill of Lading / Shipping Order", "Verified", true),
                createChecklistItem("Certificate of Origin", "Optional", true));

        checklistCard.getChildren().addAll(checkTitle, checkList);

        rightCol.getChildren().addAll(cargoRefCard, checklistCard);

        workspace.getChildren().addAll(uploadCard, rightCol);

        content.getChildren().addAll(headerBanner, workspace);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
                "-fx-background-color: transparent; -fx-background: " + BG_COLOR + "; -fx-border-color: transparent;");
        return scrollPane;
    }

    // =========================================================
    // HELPER COMPONENTS
    // =========================================================

    private VBox createCard() {
        VBox card = new VBox(18);
        card.setPadding(new Insets(24));
        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 16px;" +
                        "-fx-border-color: " + BORDER_COLOR + ";" +
                        "-fx-border-radius: 16px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.04), 10, 0, 0, 4);");
        return card;
    }

    private VBox createFormField(String labelText, javafx.scene.Node field) {
        VBox box = new VBox(6);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        if (field instanceof ComboBox) {
            ComboBox<?> cb = (ComboBox<?>) field;
            cb.setPrefHeight(42);
            cb.setMaxWidth(Double.MAX_VALUE);
            cb.setStyle("-fx-font-size: 14px; -fx-padding: 2px 6px;");
        }

        box.getChildren().addAll(label, field);
        return box;
    }

    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setStyle(
                "-fx-background-color: #F8FAFC; -fx-background-radius: 8px; -fx-border-color: #F1F5F9; -fx-border-radius: 8px;");

        Text l = new Text(label);
        l.setStyle("-fx-font-size: 12px; -fx-fill: " + TEXT_MUTED + "; -fx-font-weight: bold;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Text v = new Text(value);
        v.setStyle("-fx-font-size: 13px; -fx-fill: " + TEXT_PRIMARY + "; -fx-font-weight: bold;");

        row.getChildren().addAll(l, space, v);
        return row;
    }

    private HBox createChecklistItem(String title, String status, boolean uploaded) {
        HBox item = new HBox();
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12));
        item.setStyle(
                "-fx-background-color: #F8FAFC; -fx-background-radius: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px;");

        Label icon = new Label(uploaded ? "✓" : "⚠️");
        icon.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (uploaded ? "#16A34A" : "#D97706")
                + "; -fx-font-weight: bold;");

        VBox info = new VBox(2);
        info.setPadding(new Insets(0, 0, 0, 8));
        Text t = new Text(title);
        t.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        info.getChildren().add(t);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label badge = new Label(status);
        badge.setStyle("-fx-background-color: " + (uploaded ? "#DCFCE7" : "#FEF3C7") + "; -fx-text-fill: "
                + (uploaded ? "#15803D" : "#B45309")
                + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 12px;");

        item.getChildren().addAll(icon, info, space, badge);
        return item;
    }

    // =========================================================
    // SIDEBAR & NAVIGATION — PRESERVED
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
                com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage,
                        page.getRegisterCargoScene(this::back).getRoot());
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
                "Exporter", "Upload Document", menuButtons);
    }

    private void back() {
        if (ExporterDashboard.getInstance() != null) {
            ExporterDashboard.getInstance().switchPage("Dashboard");
        } else {
            new ExporterDashboard().start(ExporterDashboard.dashboardStage);
        }
    }

    private HBox createHeader(String pageTitle, String breadcrumb) {
        return com.projectui.view.login.DashboardLayoutHelper.createHeader(
                ExporterDashboard.dashboardStage,
                pageTitle,
                breadcrumb,
                "Exporter",
                () -> {
                    if (ExporterDashboard.dashboardStage != null) {
                        new com.projectui.view.login.LoginPage().show(ExporterDashboard.dashboardStage);
                    }
                },
                null);
    }
}
