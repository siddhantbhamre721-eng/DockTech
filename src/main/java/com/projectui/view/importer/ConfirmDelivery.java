package com.projectui.view.importer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.projectui.dao.CargoDao;
import com.projectui.dao.ContainerDao;
import com.projectui.view.login.DashboardLayoutHelper;
import com.projectui.view.login.LoginPage;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

@SuppressWarnings({"unused", "unchecked"})
public class ConfirmDelivery {

    private final CargoDao cargoDao = new CargoDao();
    private final ContainerDao containerDao = new ContainerDao();

    private final List<Map<String, Object>> eligibleShipments = new ArrayList<>();
    private Map<String, Object> selectedShipment = null;

    private VBox shipmentSelectorList;
    private VBox deliveryDetailsContainer;
    private Label lastSyncBadge;

    private CheckBox chkConditionVerified;
    private CheckBox chkSealIntact;
    private CheckBox chkAcceptanceSign;
    private TextArea deliveryNotesArea;
    private Button confirmDeliveryBtn;
    private Label deliverySuccessMessage;

    private static final String CARD_STYLE = "-fx-background-color:white; -fx-background-radius:12px; -fx-border-color:#e2e8f0; -fx-border-radius:12px; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 10, 0, 0, 3);";

    public Scene getConfirmDeliveryScene(Runnable back) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#F4F8FC;");
        root.setTop(createHeader("Confirm Delivery", "Dock Tech / Importer / Confirm Delivery"));
        root.setLeft(createSidebar(back));

        VBox content = buildMainContent(back);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color:#F4F8FC; -fx-background: transparent; -fx-border-color:transparent;");

        root.setCenter(scrollPane);

        // Reactive Listeners
        CargoDao.addChangeListener(this::refreshData);
        refreshData();

        Scene sc = new Scene(root, 1400, 800);
        LoginPage.applyInvisibleScrollbar(sc);
        return sc;
    }

    private VBox buildMainContent(Runnable back) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20, 24, 24, 24));
        container.setStyle("-fx-background-color:#F4F8FC;");

        // 1. Header Banner
        HBox headerRow = buildHeaderBanner();

        // 2. Main Two-Column Layout (Shipments Selector + Delivery Verification & Confirmation Panel)
        HBox splitRow = buildSplitRow();

        container.getChildren().addAll(headerRow, splitRow);
        return container;
    }

    private HBox buildHeaderBanner() {
        VBox titleBox = new VBox(4);
        Text title = new Text("Confirm Consignment Delivery");
        title.setStyle("-fx-font-size:26px; -fx-font-weight:bold; -fx-fill:#101A2B;");

        String importerName = getLoggedImporterName();
        Text subtitle = new Text("Verify container seal condition, acknowledge receipt at warehouse, and execute delivery sign-off for " + importerName + ".");
        subtitle.setStyle("-fx-font-size:13px; -fx-fill:#64748B;");
        titleBox.getChildren().addAll(title, subtitle);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        lastSyncBadge = new Label("● Database Connected (" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ")");
        lastSyncBadge.setStyle("-fx-background-color:#ecfdf5; -fx-text-fill:#059669; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:20; -fx-border-color:#a7f3d0; -fx-border-radius:20;");

        Button refreshBtn = new Button("↺ Refresh");
        refreshBtn.setStyle("-fx-background-color:white; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:6 14; -fx-font-size:12; -fx-font-weight:bold; -fx-text-fill:#334155; -fx-cursor:hand;");
        refreshBtn.setOnAction(e -> refreshData());

        HBox row = new HBox(12, titleBox, space, lastSyncBadge, refreshBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox buildSplitRow() {
        HBox row = new HBox(18);
        row.setAlignment(Pos.TOP_LEFT);

        // Left Panel: Eligible Incoming Shipments
        VBox leftPanel = new VBox(12);
        leftPanel.setPrefWidth(420);
        leftPanel.setMinWidth(380);
        leftPanel.setPadding(new Insets(16));
        leftPanel.setStyle(CARD_STYLE);

        Label selTitle = new Label("📦 Shipments Awaiting Receipt");
        selTitle.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        shipmentSelectorList = new VBox(8);
        ScrollPane listScroll = new ScrollPane(shipmentSelectorList);
        listScroll.setFitToWidth(true);
        listScroll.setPrefHeight(520);
        listScroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-color:transparent;");

        leftPanel.getChildren().addAll(selTitle, new Separator(), listScroll);

        // Right Panel: Verification & Confirmation Console
        deliveryDetailsContainer = new VBox(16);
        deliveryDetailsContainer.setPadding(new Insets(20));
        deliveryDetailsContainer.setStyle(CARD_STYLE);
        HBox.setHgrow(deliveryDetailsContainer, Priority.ALWAYS);

        renderDeliveryDetails();

        row.getChildren().addAll(leftPanel, deliveryDetailsContainer);
        return row;
    }

    private void refreshData() {
        Platform.runLater(() -> {
            String loggedUser = getLoggedImporterFilter();
            List<Map<String, Object>> cargos = cargoDao.getShipmentsForImporter(loggedUser);
            if (cargos == null || cargos.isEmpty()) {
                cargos = cargoDao.getAllCargos();
            }

            eligibleShipments.clear();
            if (cargos != null) {
                eligibleShipments.addAll(cargos);
            }

            if ((selectedShipment == null || !eligibleShipments.contains(selectedShipment)) && !eligibleShipments.isEmpty()) {
                selectedShipment = eligibleShipments.get(0);
            }

            renderShipmentList();
            renderDeliveryDetails();

            if (lastSyncBadge != null) {
                lastSyncBadge.setText("● Database Connected (" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ")");
            }
        });
    }

    private void renderShipmentList() {
        if (shipmentSelectorList == null) return;
        shipmentSelectorList.getChildren().clear();

        if (eligibleShipments.isEmpty()) {
            VBox emp = new VBox(8);
            emp.setAlignment(Pos.CENTER);
            emp.setPadding(new Insets(30));
            Label empIcon = new Label("📦");
            empIcon.setStyle("-fx-font-size:28px; -fx-text-fill:#94a3b8;");
            Label empMsg = new Label("No shipments assigned to your account.");
            empMsg.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#64748b;");
            emp.getChildren().addAll(empIcon, empMsg);
            shipmentSelectorList.getChildren().add(emp);
            return;
        }

        for (Map<String, Object> c : eligibleShipments) {
            String cId = str(c.get("cargoId"), "CGO-1024");
            String cName = str(c.get("cargoName"), "Industrial Freight");
            String vName = str(c.get("assignedVessel"), str(c.get("vesselName"), "AFRAPEARL II"));
            String status = str(c.get("status"), "In Transit");
            boolean isDelivered = "Delivered".equalsIgnoreCase(status);

            HBox item = new HBox(10);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(10, 12, 10, 12));

            boolean isSel = selectedShipment != null && cId.equalsIgnoreCase(str(selectedShipment.get("cargoId"), ""));
            item.setStyle(isSel
                    ? "-fx-background-color:#eff6ff; -fx-border-color:#3b82f6; -fx-border-radius:8; -fx-background-radius:8; -fx-cursor:hand;"
                    : "-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8; -fx-cursor:hand;");

            item.setOnMouseClicked(e -> {
                selectedShipment = c;
                renderShipmentList();
                renderDeliveryDetails();
            });

            Label ic = new Label(isDelivered ? "✓" : "🚢");
            ic.setStyle("-fx-font-size:18px; -fx-text-fill:" + (isDelivered ? "#16a34a" : "#0284c7") + ";");

            VBox vb = new VBox(2);
            HBox.setHgrow(vb, Priority.ALWAYS);
            Label idLbl = new Label(cId + " • " + cName);
            idLbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
            Label sub = new Label("Vessel: " + vName + "  |  Status: " + status);
            sub.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");
            vb.getChildren().addAll(idLbl, sub);

            Label stBadge = new Label(status.toUpperCase());
            stBadge.setStyle(statusBadgeStyle(status));

            item.getChildren().addAll(ic, vb, stBadge);
            shipmentSelectorList.getChildren().add(item);
        }
    }

    private void renderDeliveryDetails() {
        if (deliveryDetailsContainer == null) return;
        deliveryDetailsContainer.getChildren().clear();

        if (selectedShipment == null) {
            VBox emp = new VBox(12);
            emp.setAlignment(Pos.CENTER);
            emp.setPadding(new Insets(60));
            Label empIcon = new Label("📦");
            empIcon.setStyle("-fx-font-size:36px; -fx-text-fill:#94a3b8;");
            Label empMsg = new Label("Select a consignment from the left panel to execute delivery confirmation.");
            empMsg.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#64748b;");
            emp.getChildren().addAll(empIcon, empMsg);
            deliveryDetailsContainer.getChildren().add(emp);
            return;
        }

        Map<String, Object> c = selectedShipment;
        String cId = str(c.get("cargoId"), "CGO-1024");
        String cName = str(c.get("cargoName"), "Industrial Freight");
        String cType = str(c.get("cargoType"), "General Cargo");
        String qty = str(c.get("quantity"), "1") + " Units";
        String wt = str(c.get("weight"), "1000") + " kg";
        String exp = str(c.get("exporterName"), str(c.get("exporter"), "Nayara Energy Ltd."));
        String imp = getLoggedImporterName();
        String orig = str(c.get("originPort"), "Port of Mumbai");
        String dest = str(c.get("destinationPort"), "Cochin Port");
        String vName = str(c.get("assignedVessel"), str(c.get("vesselName"), "AFRAPEARL II"));
        String berth = "Berth B-" + (Math.abs(cId.hashCode() % 5) + 1);
        String warehouse = "DockTech Central CFS Warehouse #4, " + dest;
        String status = str(c.get("status"), "In Transit");
        boolean isDelivered = "Delivered".equalsIgnoreCase(status);

        String expDelivery = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("dd MMM yyyy, 18:00"));
        String actArrival = isDelivered ? str(c.get("deliveredAt"), "Completed") : "Berthing on Schedule (" + expDelivery + ")";

        // 1. Top Header Banner
        HBox topHdr = new HBox(12);
        topHdr.setAlignment(Pos.CENTER_LEFT);
        Label idTag = new Label("CONSIGNMENT " + cId);
        idTag.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0284c7; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:6;");
        Label titleLbl = new Label("📦  " + cName);
        titleLbl.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label statusTag = new Label(status.toUpperCase());
        statusTag.setStyle(statusBadgeStyle(status));
        topHdr.getChildren().addAll(idTag, titleLbl, sp, statusTag);

        // 2. Logistics & Delivery Specification Grid
        VBox gridCard = new VBox(10);
        gridCard.setPadding(new Insets(14));
        gridCard.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:8;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);

        grid.add(detailCell("Consignor (Exporter)", exp), 0, 0);
        grid.add(detailCell("Consignee (Importer)", imp), 1, 0);
        grid.add(detailCell("Commodity / Quantity", cName + " (" + qty + ")"), 2, 0);

        grid.add(detailCell("Gross Weight", wt), 0, 1);
        grid.add(detailCell("Assigned Vessel & Berth", vName + " • " + berth), 1, 1);
        grid.add(detailCell("Port of Discharge", dest), 2, 1);

        grid.add(detailCell("CFS Receiving Warehouse", warehouse), 0, 2);
        grid.add(detailCell("Expected Delivery Schedule", expDelivery), 1, 2);
        grid.add(detailCell("Actual Arrival / Release Time", actArrival), 2, 2);

        gridCard.getChildren().addAll(grid);

        // 3. Milestone Progress Bar
        VBox milestoneBox = buildMilestoneBox(status);

        // 4. Delivery Verification & Signing Form
        VBox signBox = new VBox(12);
        signBox.setPadding(new Insets(16));
        signBox.setStyle("-fx-background-color:" + (isDelivered ? "#f0fdf4" : "#f0f9ff") + "; -fx-border-color:" + (isDelivered ? "#bbf7d0" : "#bae6fd") + "; -fx-border-radius:8;");

        Label formTitle = new Label(isDelivered ? "✓ Consignment Successfully Delivered & Stamped" : "✍ Consignee Delivery Acceptance & Gate-Out Authorization");
        formTitle.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + (isDelivered ? "#15803d" : "#0369a1") + ";");

        if (isDelivered) {
            String delTime = str(c.get("deliveredAt"), LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
            String delBy = str(c.get("deliveredBy"), imp);
            String notes = str(c.get("deliveryNotes"), "Container seal intact. Consignment received in full condition.");

            Label confInfo = new Label("Delivery confirmed by: " + delBy + " on " + delTime + "\nRemarks: " + notes);
            confInfo.setStyle("-fx-font-size:12px; -fx-text-fill:#166534; -fx-font-weight:bold;");
            signBox.getChildren().addAll(formTitle, confInfo);
        } else {
            chkConditionVerified = new CheckBox("Physical Cargo Inspection Verified — Goods match manifest specifications without damage.");
            chkConditionVerified.setStyle("-fx-font-size:12px; -fx-text-fill:#334155;");

            chkSealIntact = new CheckBox("Customs Container Seal Intact — Tamper-evident seal numbers match Bill of Lading.");
            chkSealIntact.setStyle("-fx-font-size:12px; -fx-text-fill:#334155;");

            chkAcceptanceSign = new CheckBox("Authorized Consignee Sign-Off — I hereby confirm receipt and release this consignment.");
            chkAcceptanceSign.setStyle("-fx-font-size:12px; -fx-text-fill:#334155; -fx-font-weight:bold;");

            deliveryNotesArea = new TextArea();
            deliveryNotesArea.setPromptText("Enter consignee delivery remarks, warehouse bay number, or receiving inspector notes (Optional)...");
            deliveryNotesArea.setPrefRowCount(2);
            deliveryNotesArea.setStyle("-fx-background-color:white; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-font-size:12;");

            deliverySuccessMessage = new Label();
            deliverySuccessMessage.setVisible(false);
            deliverySuccessMessage.setStyle("-fx-text-fill:#16a34a; -fx-font-size:13px; -fx-font-weight:bold;");

            confirmDeliveryBtn = new Button("📦 Confirm Consignment Delivery & Release");
            confirmDeliveryBtn.setStyle("-fx-background-color:#0b63ff; -fx-text-fill:white; -fx-font-size:13px; -fx-font-weight:bold; -fx-padding:10 20; -fx-background-radius:6; -fx-cursor:hand;");
            confirmDeliveryBtn.setOnAction(e -> handleConfirmDelivery(cId, imp));

            signBox.getChildren().addAll(formTitle, chkConditionVerified, chkSealIntact, chkAcceptanceSign, deliveryNotesArea, confirmDeliveryBtn, deliverySuccessMessage);
        }

        deliveryDetailsContainer.getChildren().addAll(topHdr, gridCard, milestoneBox, signBox);
    }

    private void handleConfirmDelivery(String cargoId, String importerName) {
        if (chkAcceptanceSign != null && !chkAcceptanceSign.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Delivery Acceptance Required");
            alert.setHeaderText("Confirmation Checkbox Required");
            alert.setContentText("Please check 'Authorized Consignee Sign-Off' before submitting delivery confirmation.");
            alert.showAndWait();
            return;
        }

        String notes = deliveryNotesArea != null && deliveryNotesArea.getText() != null ? deliveryNotesArea.getText().trim() : "Consignment delivered in good condition.";
        if (notes.isBlank()) notes = "Consignment received and signed off by " + importerName;

        boolean ok = cargoDao.confirmDelivery(cargoId, importerName, notes);
        if (ok) {
            if (deliverySuccessMessage != null) {
                deliverySuccessMessage.setText("✓ Delivery confirmed successfully and synchronized with Exporter & Customs records.");
                deliverySuccessMessage.setVisible(true);
            }

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Delivery Confirmed");
            success.setHeaderText("Consignment Delivered Successfully");
            success.setContentText("Shipment " + cargoId + " status has been updated to DELIVERED.\nTimestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
            success.showAndWait();

            refreshData();
        } else {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Delivery Error");
            error.setHeaderText("Failed to confirm delivery");
            error.setContentText("Please verify database connection and try again.");
            error.showAndWait();
        }
    }

    private VBox buildMilestoneBox(String status) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8;");

        Label t = new Label("Milestone Progression Summary");
        t.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        boolean isDelivered = "Delivered".equalsIgnoreCase(status);

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(
                step("1. Cargo Created", true),
                arrow(),
                step("2. Container Staged", true),
                arrow(),
                step("3. Customs Cleared", true),
                arrow(),
                step("4. Vessel In Transit", true),
                arrow(),
                step("5. Port Arrived", true),
                arrow(),
                step("6. Delivery Confirmed", isDelivered)
        );

        box.getChildren().addAll(t, row);
        return box;
    }

    private Label step(String name, boolean done) {
        Label l = new Label((done ? "✓ " : "○ ") + name);
        l.setStyle(done
                ? "-fx-background-color:#dcfce7; -fx-text-fill:#16a34a; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:4 8; -fx-background-radius:4;"
                : "-fx-background-color:#f1f5f9; -fx-text-fill:#94a3b8; -fx-font-size:10px; -fx-padding:4 8; -fx-background-radius:4;");
        return l;
    }

    private Label arrow() {
        Label a = new Label("➔");
        a.setStyle("-fx-text-fill:#cbd5e1; -fx-font-size:11;");
        return a;
    }

    private static VBox detailCell(String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px; -fx-font-weight:bold;");
        Label v = new Label(value != null && !value.isBlank() ? value : "N/A");
        v.setStyle("-fx-text-fill:#0f172a; -fx-font-size:12px;");
        return new VBox(2, l, v);
    }

    private String statusBadgeStyle(String status) {
        if ("Delivered".equalsIgnoreCase(status)) {
            return "-fx-background-color:#dcfce7; -fx-text-fill:#16a34a; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;";
        } else if ("Arriving Soon".equalsIgnoreCase(status)) {
            return "-fx-background-color:#fef3c7; -fx-text-fill:#d97706; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;";
        } else if ("Delayed / At Risk".equalsIgnoreCase(status) || "Under Inspection".equalsIgnoreCase(status)) {
            return "-fx-background-color:#fee2e2; -fx-text-fill:#dc2626; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;";
        }
        return "-fx-background-color:#e0f2fe; -fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;";
    }

    private static String str(Object o, String fallback) {
        return (o != null && !o.toString().isBlank()) ? o.toString().trim() : fallback;
    }

    private String getLoggedImporterName() {
        if (LoginPage.loggedInUser != null) {
            Object n = LoginPage.loggedInUser.get("name");
            if (n != null && !n.toString().isBlank()) return n.toString().trim();
            Object f = LoginPage.loggedInUser.get("fullName");
            if (f != null && !f.toString().isBlank()) return f.toString().trim();
        }
        return "Harsh Patel";
    }

    private String getLoggedImporterFilter() {
        if (LoginPage.loggedInUser != null) {
            Object c = LoginPage.loggedInUser.get("company");
            if (c != null && !c.toString().isBlank()) return c.toString().trim();
            Object e = LoginPage.loggedInUser.get("email");
            if (e != null && !e.toString().isBlank()) return e.toString().trim();
            Object n = LoginPage.loggedInUser.get("name");
            if (n != null && !n.toString().isBlank()) return n.toString().trim();
        }
        return "Harsh";
    }

    private VBox createSidebar(Runnable back) {
        Map<String, Button> menuButtons = new java.util.LinkedHashMap<>();
        Button dashboard = new Button("🏠   Dashboard");
        Button track = new Button("📥   Track Shipment");
        Button invoice = new Button("📄   Download Invoice");
        Button delivery = new Button("📦   Confirm Delivery");

        menuButtons.put("Dashboard", dashboard);
        menuButtons.put("Track Shipment", track);
        menuButtons.put("Download Invoice", invoice);
        menuButtons.put("Confirm Delivery", delivery);

        dashboard.setOnAction(e -> back.run());

        track.setOnAction(e -> {
            TrackShipment page = new TrackShipment();
            LoginPage.showRoot(ImporterDashboard.importerStage, page.getTrackShipmentScene(back).getRoot());
        });

        invoice.setOnAction(e -> {
            DownloadInvoice page = new DownloadInvoice();
            LoginPage.showRoot(ImporterDashboard.importerStage, page.getDownloadInvoiceScene(back).getRoot());
        });

        return DashboardLayoutHelper.createSidebar(ImporterDashboard.importerStage, "Importer", "Confirm Delivery", menuButtons);
    }

    private HBox createHeader(String title, String breadcrumb) {
        return DashboardLayoutHelper.createHeader(
                ImporterDashboard.importerStage,
                title,
                breadcrumb,
                "Importer",
                () -> {
                    if (ImporterDashboard.importerStage != null) {
                        new LoginPage().show(ImporterDashboard.importerStage);
                    }
                },
                null);
    }
}
