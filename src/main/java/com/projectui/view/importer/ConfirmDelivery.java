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
import com.projectui.dao.PaymentDao;
import com.projectui.service.FreightCalculationService;
import com.projectui.service.RazorpayService;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

@SuppressWarnings({"unused", "unchecked"})
public class ConfirmDelivery {

    private final CargoDao cargoDao = new CargoDao();
    private final ContainerDao containerDao = new ContainerDao();
    private final PaymentDao paymentDao = new PaymentDao();
    private final RazorpayService razorpayService = new RazorpayService();

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

        Scene sc = new Scene(root);
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
            List<Map<String, Object>> cargos;
            if (LoginPage.loggedInUser != null && !LoginPage.loggedInUser.isEmpty()) {
                cargos = cargoDao.getShipmentsForImporter(LoginPage.loggedInUser);
            } else {
                String loggedUser = getLoggedImporterFilter();
                cargos = cargoDao.getShipmentsForImporter(loggedUser);
            }

            eligibleShipments.clear();
            if (cargos != null) {
                eligibleShipments.addAll(cargos);
            }

            if (selectedShipment == null || !eligibleShipments.contains(selectedShipment)) {
                selectedShipment = !eligibleShipments.isEmpty() ? eligibleShipments.get(0) : null;
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
            Label empMsg = new Label("No incoming shipments assigned to your account yet.");
            empMsg.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#64748b;");
            emp.getChildren().addAll(empIcon, empMsg);
            shipmentSelectorList.getChildren().add(emp);
            return;
        }

        for (Map<String, Object> c : eligibleShipments) {
            String cId = str(c.get("cargoId"), "CGO-N/A");
            String cName = str(c.get("cargoName"), "Consignment");
            String vName = str(c.get("assignedVessel"), str(c.get("vesselName"), str(c.get("assignedShip"), "Not Assigned")));
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
            Label empMsg = new Label(eligibleShipments.isEmpty()
                    ? "No incoming shipments assigned to your account yet."
                    : "Select a consignment from the left panel to execute delivery confirmation.");
            empMsg.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#64748b;");
            emp.getChildren().addAll(empIcon, empMsg);
            deliveryDetailsContainer.getChildren().add(emp);
            return;
        }

        Map<String, Object> c = selectedShipment;
        String cId = str(c.get("cargoId"), "CGO-N/A");
        String cName = str(c.get("cargoName"), "Consignment");
        String cType = str(c.get("cargoType"), "General Cargo");
        String qty = str(c.get("quantity"), "1") + " Units";
        String rawWt = str(c.get("weight"), "—");
        String wt = rawWt.equals("—") || rawWt.contains("kg") ? rawWt : (rawWt + " kg");
        String exp = str(c.get("exporterName"), str(c.get("exporter"), str(c.get("exporterCompany"), str(c.get("shipper"), str(c.get("company"), str(c.get("customerName"), "—"))))));
        if (exp.equals("—") || exp.isBlank() || exp.equalsIgnoreCase("Exporter")) {
            String expUid = str(c.get("ownerUid"), str(c.get("exporterUid"), str(c.get("exporterId"), str(c.get("userId"), ""))));
            String expEmail = str(c.get("ownerEmail"), str(c.get("exporterEmail"), ""));
            if (!expUid.isBlank()) {
                Map<String, Object> prof = com.projectui.dao.UserDao.getProfile(expUid);
                if (prof != null) {
                    Object pn = prof.get("name") != null ? prof.get("name") : (prof.get("fullName") != null ? prof.get("fullName") : prof.get("company"));
                    if (pn != null && !pn.toString().isBlank()) {
                        exp = pn.toString();
                    }
                }
            }
            if ((exp.equals("—") || exp.isBlank() || exp.equalsIgnoreCase("Exporter")) && !expEmail.isBlank()) {
                Map<String, Object> prof = com.projectui.dao.UserDao.getProfile(expEmail);
                if (prof != null) {
                    Object pn = prof.get("name") != null ? prof.get("name") : (prof.get("fullName") != null ? prof.get("fullName") : prof.get("company"));
                    if (pn != null && !pn.toString().isBlank()) {
                        exp = pn.toString();
                    }
                }
            }
        }
        String imp = str(c.get("importerName"), str(c.get("importer"), str(c.get("consignee"), str(c.get("importerCompany"), getLoggedImporterName()))));
        String orig = str(c.get("originPort"), "—");
        String dest = str(c.get("destinationPort"), "—");
        String vName = str(c.get("assignedVessel"), str(c.get("vesselName"), str(c.get("assignedShip"), "Not Assigned")));
        String berth = "Berth B-" + (Math.abs(cId.hashCode() % 5) + 1);
        String warehouse = dest.equals("—") ? "DockTech Central CFS Warehouse" : ("DockTech Central CFS Warehouse, " + dest);
        String status = str(c.get("status"), "In Transit");
        boolean isDelivered = "Delivered".equalsIgnoreCase(status);

        // 1. Top Shipment Header Banner
        HBox banner = new HBox(12);
        banner.setAlignment(Pos.CENTER_LEFT);
        Label idTag = new Label("CONSIGNMENT " + cId);
        idTag.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0284c7; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:6;");
        Label nameLbl = new Label("📦  " + cName);
        nameLbl.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
        Region spB = new Region();
        HBox.setHgrow(spB, Priority.ALWAYS);
        Label statusBadge = new Label(status.toUpperCase());
        statusBadge.setStyle(statusBadgeStyle(status));
        banner.getChildren().addAll(idTag, nameLbl, spB, statusBadge);

        // 2. Consignment Metadata Grid
        VBox metaBox = new VBox(10);
        metaBox.setPadding(new Insets(14));
        metaBox.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:8;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);

        grid.add(detailCell("Consignor (Exporter)", exp), 0, 0);
        grid.add(detailCell("Consignee (Importer)", imp), 1, 0);
        grid.add(detailCell("Cargo Category & Quantity", cType + " • " + qty), 0, 1);
        grid.add(detailCell("Gross Weight", wt), 1, 1);
        grid.add(detailCell("Origin Port ➔ Destination", orig + " ➔ " + dest), 0, 2);
        grid.add(detailCell("Assigned Vessel & Berth", vName + " • " + berth), 1, 2);
        grid.add(detailCell("CFS Warehouse Staging", warehouse), 0, 3);
        grid.add(detailCell("Customs Clearance Status", str(c.get("customsStatus"), "CLEARED")), 1, 3);

        metaBox.getChildren().addAll(grid);

        // 3. Milestone Summary
        VBox milestoneBox = buildMilestoneBox(status);

        // 4. Payment to Exporter
        VBox paymentBox = buildExporterPaymentSection(c);

        // 5. Delivery Confirmation Form Card
        VBox formCard = new VBox(12);
        formCard.setPadding(new Insets(16));
        formCard.setStyle("-fx-background-color:#ffffff; -fx-border-color:#cbd5e1; -fx-border-radius:8;");

        Label formTitle = new Label("Consignee Delivery Acceptance & Gate-Out Handover");
        formTitle.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        chkAcceptanceSign = new CheckBox("Authorized Consignee Sign-Off: I confirm receipt of consignment in verified intact condition with seals matched.");
        chkAcceptanceSign.setStyle("-fx-font-size:12px; -fx-text-fill:#334155; -fx-font-weight:bold;");
        chkAcceptanceSign.setSelected(isDelivered);
        chkAcceptanceSign.setDisable(isDelivered);

        VBox notesBox = new VBox(4);
        Label notesLbl = new Label("Delivery Handover Notes / Exceptions (Optional):");
        notesLbl.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b; -fx-font-weight:bold;");
        deliveryNotesArea = new TextArea(isDelivered ? str(c.get("deliveryNotes"), "Consignment signed off and received.") : "");
        deliveryNotesArea.setPromptText("Enter container seal inspection notes or warehouse receiving bay details...");
        deliveryNotesArea.setPrefRowCount(2);
        deliveryNotesArea.setDisable(isDelivered);
        notesBox.getChildren().addAll(notesLbl, deliveryNotesArea);

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        deliverySuccessMessage = new Label("✓ Delivery confirmed successfully and synchronized with Exporter & Customs records.");
        deliverySuccessMessage.setStyle("-fx-text-fill:#16a34a; -fx-font-size:12px; -fx-font-weight:bold;");
        deliverySuccessMessage.setVisible(isDelivered);

        Region spF = new Region();
        HBox.setHgrow(spF, Priority.ALWAYS);

        Button confirmBtn = new Button(isDelivered ? "✓ Consignment Delivered" : "📦 Confirm & Accept Delivery");
        confirmBtn.setStyle(isDelivered
                ? "-fx-background-color:#dcfce7; -fx-text-fill:#16a34a; -fx-font-weight:bold; -fx-padding:10 20; -fx-background-radius:6;"
                : "-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:10 20; -fx-background-radius:6; -fx-cursor:hand;");
        confirmDeliveryBtn = confirmBtn;
        confirmBtn.setDisable(isDelivered || !isCargoPaymentPaid(cId));
        confirmBtn.setOnAction(e -> handleConfirmDelivery(cId, imp));

        btnRow.getChildren().addAll(deliverySuccessMessage, spF, confirmBtn);

        formCard.getChildren().addAll(formTitle, chkAcceptanceSign, notesBox, btnRow);

        deliveryDetailsContainer.getChildren().addAll(banner, metaBox, milestoneBox, paymentBox, formCard);
    }

    private VBox buildExporterPaymentSection(Map<String, Object> cargo) {
        String cargoId = str(cargo.get("cargoId"), "CGO-N/A");
        String cargoName = str(cargo.get("cargoName"), "Consignment");
        String exporterName = str(cargo.get("exporterName"), str(cargo.get("exporter"), str(cargo.get("exporterCompany"), "Exporter")));
        Map<String, Object> booking = new ContainerDao().getBookingByCargoId(cargoId);
        FreightCalculationService.PaymentBreakdown breakdown = FreightCalculationService.getInstance().calculatePaymentBreakdown(cargo, booking);
        double amount = getPaymentAmount(cargo, breakdown);
        Map<String, Object> existingPayment = getImporterExporterPayment(cargoId);
        String paymentStatus = str(cargo.get("paymentStatus"), "PENDING");
        if (existingPayment != null && isImporterExporterPayment(existingPayment)) {
            paymentStatus = str(existingPayment.get("paymentStatus"), paymentStatus);
        } else {
            paymentStatus = "PENDING";
        }
        boolean isPaid = "PAID".equalsIgnoreCase(paymentStatus) || paymentStatus.toUpperCase().contains("PAID");

        VBox section = new VBox(10);
        section.setPadding(new Insets(16));
        section.setStyle("-fx-background-color:#ffffff; -fx-border-color:#cbd5e1; -fx-border-radius:8;");

        Label title = new Label("Payment to Exporter");
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
        GridPane details = new GridPane();
        details.setHgap(24);
        details.setVgap(8);
        details.add(detailCell("Exporter Name", exporterName), 0, 0);
        details.add(detailCell("Cargo / Consignment ID", cargoId), 1, 0);
        details.add(detailCell("Cargo Name", cargoName), 0, 1);
        details.add(detailCell("Payment Amount", "₹ " + String.format("%,.2f", amount)), 1, 1);
        Label statusLabel = new Label(paymentStatus.toUpperCase());
        statusLabel.setStyle(isPaid
                ? "-fx-text-fill:#15803d; -fx-font-weight:bold;"
                : "-fx-text-fill:#b45309; -fx-font-weight:bold;");
        details.add(detailCell("Payment Status", ""), 0, 2);
        details.add(statusLabel, 1, 2);
        details.add(detailCell("Razorpay Payment Method", str(cargo.get("paymentMethod"), "Razorpay Standard Checkout (Cards / UPI / NetBanking)")), 0, 3, 2, 1);

        Button payButton = new Button(isPaid ? "✓ Payment Complete" : "Proceed to Pay ₹" + String.format("%,.2f", amount));
        payButton.setDisable(isPaid || amount <= 0);
        payButton.setStyle(isPaid
                ? "-fx-background-color:#dcfce7; -fx-text-fill:#15803d; -fx-font-weight:bold; -fx-padding:10 20; -fx-background-radius:6;"
                : "-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:10 20; -fx-background-radius:6; -fx-cursor:hand;");
        Label paymentMessage = new Label(isPaid ? "Payment Verified Successfully. The exporter payment record is updated." : "Complete payment to the exporter before accepting delivery.");
        paymentMessage.setWrapText(true);
        paymentMessage.setStyle(isPaid
                ? "-fx-text-fill:#15803d; -fx-font-size:12px; -fx-font-weight:bold;"
                : "-fx-text-fill:#b45309; -fx-font-size:12px; -fx-font-weight:bold;");

        payButton.setOnAction(event -> startImporterPayment(cargo, booking, amount, payButton, paymentMessage));
        HBox actionRow = new HBox(12, paymentMessage, payButton);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(paymentMessage, Priority.ALWAYS);
        section.getChildren().addAll(title, details, actionRow);
        return section;
    }

    private void startImporterPayment(Map<String, Object> cargo, Map<String, Object> booking, double amount,
            Button payButton, Label paymentMessage) {
        String cargoId = str(cargo.get("cargoId"), "");
        String cargoName = str(cargo.get("cargoName"), "Consignment");
        if (cargoId.isBlank() || amount <= 0) return;
        if (!razorpayService.hasValidCredentials()) {
            showPaymentAlert("Razorpay Configuration Required", "Razorpay credentials are not configured.");
            return;
        }

        payButton.setDisable(true);
        String receiptNo = "RCT-" + (System.currentTimeMillis() % 1000000);
        try {
            String orderId = razorpayService.createOrder(amount, "INR", receiptNo, cargoName);
            com.projectui.util.JavaFXModuleHelper.allowJavaFXWebAccess();
            WebView checkoutView = new WebView();
            checkoutView.getEngine().setJavaScriptEnabled(true);
            Stage checkoutStage = new Stage();
            checkoutStage.setTitle("Razorpay Payment");
            checkoutStage.setScene(new Scene(checkoutView, 960, 680));
            com.projectui.view.login.LoginPage.applyInvisibleScrollbar(checkoutStage);
            com.projectui.view.login.LoginPage.applyInvisibleScrollbar(checkoutView);
            checkoutStage.setOnCloseRequest(event -> razorpayService.cancelActiveCheckout());
            razorpayService.startEmbeddedCheckout(amount, orderId, cargoName, str(cargo.get("destinationPort"), ""),
                    checkoutUrl -> Platform.runLater(() -> {
                        checkoutView.getEngine().load(checkoutUrl);
                        checkoutStage.show();
                    }), new RazorpayService.PaymentCallback() {
                        @Override
                        public void onSuccess(String verifiedOrderId, String paymentId, String signature) {
                            Platform.runLater(() -> {
                                checkoutStage.close();
                                String paymentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"));
                                Map<String, Object> paymentRecord = new HashMap<>();
                                paymentRecord.put("paymentId", paymentId);
                                paymentRecord.put("razorpayPaymentId", paymentId);
                                paymentRecord.put("orderId", verifiedOrderId);
                                paymentRecord.put("razorpayOrderId", verifiedOrderId);
                                paymentRecord.put("signature", signature);
                                paymentRecord.put("receiptNumber", receiptNo);
                                paymentRecord.put("transactionId", paymentId);
                                paymentRecord.put("cargoId", cargoId);
                                paymentRecord.put("cargoName", cargoName);
                                paymentRecord.put("cargoType", str(cargo.get("cargoType"), "General Cargo"));
                                paymentRecord.put("originPort", str(cargo.get("originPort"), ""));
                                paymentRecord.put("destinationPort", str(cargo.get("destinationPort"), ""));
                                paymentRecord.put("importerName", str(cargo.get("importerName"), str(cargo.get("importer"), getLoggedImporterName())));
                                paymentRecord.put("importerEmail", str(cargo.get("importerEmail"), ""));
                                paymentRecord.put("importerUid", str(cargo.get("importerUid"), str(cargo.get("importerId"), "")));
                                paymentRecord.put("exporterName", str(cargo.get("exporterName"), str(cargo.get("exporter"), "Exporter")));
                                paymentRecord.put("exporterEmail", str(cargo.get("exporterEmail"), ""));
                                paymentRecord.put("exporterUid", str(cargo.get("exporterUid"), str(cargo.get("ownerUid"), "")));
                                paymentRecord.put("amount", amount);
                                paymentRecord.put("paymentAmount", amount);
                                paymentRecord.put("currency", "INR");
                                paymentRecord.put("paymentStatus", "PAID");
                                paymentRecord.put("paymentPurpose", "IMPORTER_TO_EXPORTER");
                                paymentRecord.put("paymentMethod", "Razorpay Standard Checkout (Cards / UPI / NetBanking)");
                                paymentRecord.put("paymentDate", paymentDate);
                                paymentRecord.put("paidAt", paymentDate);
                                paymentDao.savePayment(paymentRecord);
                                cargoDao.updateCargoPaymentStatus(cargoId, "PAID", paymentId, verifiedOrderId, amount, receiptNo, "IMPORTER_TO_EXPORTER");
                                paymentMessage.setText("Payment Verified Successfully. The exporter payment record is updated.");
                                paymentMessage.setStyle("-fx-text-fill:#15803d; -fx-font-size:12px; -fx-font-weight:bold;");
                                payButton.setText("✓ Payment Complete");
                                payButton.setStyle("-fx-background-color:#dcfce7; -fx-text-fill:#15803d; -fx-font-weight:bold; -fx-padding:10 20; -fx-background-radius:6;");
                                refreshData();
                            });
                        }

                        @Override
                        public void onFailure(String reason) {
                            Platform.runLater(() -> {
                                checkoutStage.close();
                                payButton.setDisable(false);
                                paymentMessage.setText(reason != null ? reason : "Payment was not completed. Complete payment before accepting delivery.");
                                paymentMessage.setStyle("-fx-text-fill:#b91c1c; -fx-font-size:12px; -fx-font-weight:bold;");
                            });
                        }
                    });
        } catch (Exception ex) {
            payButton.setDisable(false);
            showPaymentAlert("Payment Could Not Start", ex.getMessage() != null ? ex.getMessage() : "Unable to create the Razorpay order.");
        }
    }

    private double getPaymentAmount(Map<String, Object> cargo, FreightCalculationService.PaymentBreakdown breakdown) {
        Object storedAmount = cargo.get("paymentAmount");
        if (storedAmount != null) {
            try {
                double parsed = Double.parseDouble(storedAmount.toString().replace("₹", "").replace(",", "").trim());
                if (parsed > 0) return parsed;
            } catch (Exception ignored) {}
        }
        return breakdown != null ? breakdown.totalPayable : 0.0;
    }

    private boolean isCargoPaymentPaid(String cargoId) {
        Map<String, Object> cargo = cargoDao.getCargo(cargoId);
        if (cargo == null) return false;
        Map<String, Object> payment = getImporterExporterPayment(cargoId);
        if (payment == null || !isImporterExporterPayment(payment)) return false;
        String status = str(payment.get("paymentStatus"), "");
        String paymentId = payment != null ? str(payment.get("razorpayPaymentId"), "") : str(cargo.get("razorpayPaymentId"), "");
        return "PAID".equalsIgnoreCase(status) && !paymentId.isBlank();
    }

    private boolean isImporterExporterPayment(Map<String, Object> payment) {
        return payment != null && "IMPORTER_TO_EXPORTER".equalsIgnoreCase(str(payment.get("paymentPurpose"), ""));
    }

    private Map<String, Object> getImporterExporterPayment(String cargoId) {
        for (Map<String, Object> payment : paymentDao.getAllPayments()) {
            if (payment != null && cargoId.equalsIgnoreCase(str(payment.get("cargoId"), ""))
                    && isImporterExporterPayment(payment)) {
                return payment;
            }
        }
        return null;
    }

    private void showPaymentAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleConfirmDelivery(String cargoId, String importerName) {
        // Strict ownership verification: verify cargo belongs to the logged-in importer's eligible shipments
        boolean isAuthorized = false;
        for (Map<String, Object> c : eligibleShipments) {
            if (c != null && cargoId.equalsIgnoreCase(str(c.get("cargoId"), ""))) {
                isAuthorized = true;
                break;
            }
        }

        if (!isAuthorized) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unauthorized Action");
            alert.setHeaderText("Delivery Confirmation Denied");
            alert.setContentText("You can only confirm delivery for shipments assigned directly to your account.");
            alert.showAndWait();
            return;
        }

        if (!isCargoPaymentPaid(cargoId)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Payment Required");
            alert.setHeaderText("Delivery Confirmation Blocked");
            alert.setContentText("Complete payment to the exporter before accepting delivery.");
            alert.showAndWait();
            return;
        }

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
            try {
                com.projectui.dao.NotificationDao notifDao = new com.projectui.dao.NotificationDao();
                String uId = LoginPage.loggedInUser != null ? str(LoginPage.loggedInUser.get("uid"), str(LoginPage.loggedInUser.get("email"), importerName)) : importerName;
                com.projectui.model.Notification notif = new com.projectui.model.Notification();
                notif.setId("notif_" + System.currentTimeMillis());
                notif.setUserId(uId);
                notif.setTitle("Shipment Delivered");
                notif.setMessage("Consignment " + cargoId + " has been marked as Delivered by " + importerName + ".");
                notif.setType("CARGO");
                notif.setStatus("Delivered");
                notif.setRead(false);
                notif.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
                notifDao.createNotification(notif);
            } catch (Exception ignored) {}

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
            Object d = LoginPage.loggedInUser.get("displayName");
            if (d != null && !d.toString().isBlank()) return d.toString().trim();
            Object c = LoginPage.loggedInUser.get("company");
            if (c != null && !c.toString().isBlank()) return c.toString().trim();
        }
        return "Importer";
    }

    private String getLoggedImporterFilter() {
        if (LoginPage.loggedInUser != null) {
            Object c = LoginPage.loggedInUser.get("company");
            if (c != null && !c.toString().isBlank()) return c.toString().trim();
            Object e = LoginPage.loggedInUser.get("email");
            if (e != null && !e.toString().isBlank()) return e.toString().trim();
            Object n = LoginPage.loggedInUser.get("name");
            if (n != null && !n.toString().isBlank()) return n.toString().trim();
            Object u = LoginPage.loggedInUser.get("uid");
            if (u != null && !u.toString().isBlank()) return u.toString().trim();
        }
        return "";
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
