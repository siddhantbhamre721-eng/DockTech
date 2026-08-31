package com.projectui.view.exporter;

import com.projectui.dao.CargoDao;
import com.projectui.service.RazorpayService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.scene.text.Text;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MakePayment {

    static {
        com.projectui.util.JavaFXModuleHelper.allowJavaFXWebAccess();
    }

    private Scene scene;

    // Design Tokens
    private static final String BG_COLOR = "#F4F8FC";
    private static final String CARD_BG = "#FFFFFF";
    private static final String TEXT_PRIMARY = "#0F172A";
    private static final String TEXT_MUTED = "#64748B";
    private static final String PRIMARY_BLUE = "#1769FF";
    private static final String BORDER_COLOR = "#E2E8F0";

    private final CargoDao cargoDao = new CargoDao();
    private final RazorpayService razorpayService = new RazorpayService();

    public Scene getMakePaymentScene() {
        return getMakePaymentScene(null);
    }

    public Scene getMakePaymentScene(Runnable callbackAction) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");
        root.setTop(createHeader("Make Payment", "DockTech / Exporter / Make Payment"));
        root.setLeft(createSidebar());

        root.setCenter(getScrollableContent());

        scene = new Scene(root, 1550, 800);
        com.projectui.view.login.LoginPage.applyInvisibleScrollbar(scene);
        return scene;
    }

    public StackPane getScrollableContent() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 32, 36, 32));
        content.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // Header Banner
        VBox headerBanner = new VBox(6);
        Text title = new Text("Make Payment");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text subtitle = new Text(
                "Complete secure freight forwarding, terminal handling, and port duties payments via Razorpay Gateway.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-fill: " + TEXT_MUTED + ";");
        headerBanner.getChildren().addAll(title, subtitle);

        // Main Payment Workspace (2 Columns)
        HBox workspace = new HBox(24);

        // =========================================================
        // LEFT COLUMN: PAYMENT SUMMARY & METHOD SELECTION (60% Width)
        // =========================================================
        VBox summaryCard = createCard();
        HBox.setHgrow(summaryCard, Priority.ALWAYS);

        VBox summaryHeader = new VBox(4);
        Text summaryTitle = new Text("💳  Payment Summary & Checkout");
        summaryTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");
        Text summarySub = new Text("Review cargo charges and select a payment method.");
        summarySub.setStyle("-fx-font-size: 13px; -fx-fill: " + TEXT_MUTED + ";");
        summaryHeader.getChildren().addAll(summaryTitle, summarySub);

        // Dynamic Cargo Data
        List<Map<String, Object>> cargos = cargoDao.getShipmentsForExporter(com.projectui.view.login.LoginPage.loggedInUser);
        ComboBox<String> cargoSelect = new ComboBox<>();
        cargoSelect.setMaxWidth(Double.MAX_VALUE);
        cargoSelect.setPrefHeight(38);
        cargoSelect.setStyle(
                "-fx-background-color: #F8FAFC; -fx-border-color: #CBD5E1; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-font-size: 13px;");

        if (cargos != null && !cargos.isEmpty()) {
            for (Map<String, Object> c : cargos) {
                String cId = c.get("cargoId") != null ? c.get("cargoId").toString() : "CGO";
                String cName = c.get("cargoName") != null ? c.get("cargoName").toString() : "";
                cargoSelect.getItems().add(cId + " — " + cName);
            }
            cargoSelect.getSelectionModel().select(0);
        } else {
            cargoSelect.getItems().add("No Registered Cargo Found");
            cargoSelect.getSelectionModel().select(0);
        }

        // Summary Metric Tiles (4-grid)
        GridPane metricGrid = new GridPane();
        metricGrid.setHgap(12);
        metricGrid.setVgap(12);

        VBox selectedCargoTile = createSummaryTile("SELECTED CARGO", "Loading...", "#1769FF");
        VBox declaredValueTile = createSummaryTile("ESTIMATED VALUE", "₹ 0", "#10B981");
        VBox portFeesTile = createSummaryTile("PORT & FREIGHT DUES", "₹ 0", "#F59E0B");
        VBox payableAmountTile = createSummaryTile("TOTAL PAYABLE", "₹ 0", "#6366F1");

        metricGrid.add(selectedCargoTile, 0, 0);
        metricGrid.add(declaredValueTile, 1, 0);
        metricGrid.add(portFeesTile, 0, 1);
        metricGrid.add(payableAmountTile, 1, 1);

        GridPane.setHgrow(selectedCargoTile, Priority.ALWAYS);
        GridPane.setHgrow(declaredValueTile, Priority.ALWAYS);
        GridPane.setHgrow(portFeesTile, Priority.ALWAYS);
        GridPane.setHgrow(payableAmountTile, Priority.ALWAYS);

        // Payment Method Selector
        VBox methodSection = new VBox(8);
        Text methodTitle = new Text("Select Payment Method");
        methodTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        ComboBox<String> paymentMethodCombo = new ComboBox<>();
        paymentMethodCombo.getItems().addAll(
                "Razorpay Gateway (Cards / UPI / NetBanking)",
                "Corporate Credit Line / Letter of Credit (LC)",
                "RTGS / NEFT Direct Port Settlement",
                "Port Authority Escrow Account");
        paymentMethodCombo.setValue("Razorpay Gateway (Cards / UPI / NetBanking)");
        paymentMethodCombo.setMaxWidth(Double.MAX_VALUE);
        paymentMethodCombo.setPrefHeight(38);
        paymentMethodCombo.setStyle(
                "-fx-background-color: #F8FAFC; -fx-border-color: #CBD5E1; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-font-size: 13px;");
        methodSection.getChildren().addAll(methodTitle, paymentMethodCombo);

        // Action CTA & Success Alert
        Button payButton = new Button("🔒  Proceed to Pay via Razorpay");
        payButton.setMaxWidth(Double.MAX_VALUE);
        payButton.setPrefHeight(44);
        payButton.setStyle(
                "-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 9px; -fx-cursor: hand;");

        Label successAlert = new Label();
        successAlert.setWrapText(true);
        successAlert.setMaxWidth(Double.MAX_VALUE);
        successAlert.setStyle(
                "-fx-background-color: #DCFCE7; -fx-text-fill: #15803D; -fx-border-color: #86EFAC; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 12px; -fx-font-size: 13px; -fx-font-weight: bold;");
        successAlert.setVisible(false);
        successAlert.setManaged(false);

        Button nextWorkflowBtn = new Button("Continue to Track Shipment →");
        nextWorkflowBtn.setMaxWidth(Double.MAX_VALUE);
        nextWorkflowBtn.setPrefHeight(40);
        nextWorkflowBtn.setStyle("-fx-background-color: " + PRIMARY_BLUE
                + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-cursor: hand;");
        nextWorkflowBtn.setVisible(false);
        nextWorkflowBtn.setManaged(false);
        nextWorkflowBtn.setOnAction(e -> {
            if (ExporterDashboard.getInstance() != null) {
                ExporterDashboard.getInstance().switchPage("Track Shipment");
            } else {
                TrackShipment page = new TrackShipment();
                com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage,
                        page.getTrackShipmentScene(this::back).getRoot());
            }
        });

        summaryCard.getChildren().addAll(
                summaryHeader,
                new Label("Selected Registered Cargo:"),
                cargoSelect,
                metricGrid,
                methodSection,
                payButton,
                successAlert,
                nextWorkflowBtn);

        // =========================================================
        // RIGHT COLUMN: ITEMISED BREAKDOWN & SECURITY BADGE (40% Width)
        // =========================================================
        VBox rightCol = new VBox(20);
        rightCol.setPrefWidth(420);

        VBox detailsCard = createCard();
        Text detailsTitle = new Text("🧾  Tariff & Duties Breakdown");
        detailsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: " + TEXT_PRIMARY + ";");

        VBox detailsList = new VBox(10);
        detailsCard.getChildren().addAll(detailsTitle, detailsList);

        // Security Notice Box
        VBox securityNotice = new VBox(8);
        securityNotice.setPadding(new Insets(16));
        securityNotice.setStyle(
                "-fx-background-color: #EFF6FF; -fx-border-color: #BFDBFE; -fx-border-radius: 12px; -fx-background-radius: 12px;");

        Text securityTitle = new Text("🔒 Bank-Grade 256-bit Encryption");
        securityTitle.setStyle("-fx-font-weight: bold; -fx-fill: #1E40AF; -fx-font-size: 13px;");
        Text securityDesc = new Text(
                "Transactions are secured via Razorpay Test Gateway with simulated webhook validation. DockTech never stores raw payment card data.");
        securityDesc.setStyle("-fx-fill: #3B82F6; -fx-font-size: 12px;");
        securityDesc.setWrappingWidth(330);

        securityNotice.getChildren().addAll(securityTitle, securityDesc);
        rightCol.getChildren().addAll(detailsCard, securityNotice);

        // Dynamic update function when cargo selection changes
        Runnable updateCargoDetails = () -> {
            String sel = cargoSelect.getValue();
            String cId = "";
            if (sel != null && !sel.startsWith("No Registered")) {
                cId = sel.split(" — ")[0].trim();
            }
            Map<String, Object> c = (!cId.isBlank()) ? cargoDao.getCargo(cId) : null;
            String cName = c != null && c.get("cargoName") != null ? c.get("cargoName").toString() : "N/A";
            String originPort = c != null && c.get("originPort") != null ? c.get("originPort").toString()
                    : "Origin Port";
            String destPort = c != null && c.get("destinationPort") != null ? c.get("destinationPort").toString()
                    : "Destination Port";

            Map<String, Object> booking = (!cId.isBlank())
                    ? new com.projectui.dao.ContainerDao().getBookingByCargoId(cId)
                    : null;

            com.projectui.service.FreightCalculationService.PaymentBreakdown breakdown = com.projectui.service.FreightCalculationService
                    .getInstance().calculatePaymentBreakdown(c, booking);

            String totalStr = String.format("₹ %,.0f", breakdown.totalPayable);

            // Update tiles
            ((Text) selectedCargoTile.getChildren().get(1))
                    .setText(!cId.isBlank() ? (cId + " (" + cName + ")") : "None");
            ((Text) declaredValueTile.getChildren().get(1))
                    .setText("₹ " + String.format("%,.0f", breakdown.declaredCargoValue));
            ((Text) portFeesTile.getChildren().get(1))
                    .setText("₹ " + String.format("%,.0f", breakdown.containerFreightFee + breakdown.portHandlingFee));
            ((Text) payableAmountTile.getChildren().get(1)).setText(totalStr);

            payButton.setText("🔒 Proceed to Pay " + totalStr + " via Razorpay");
            // payButton.setText("🔒 Proceed to Pay " + 100 + " via Razorpay");

            detailsList.getChildren().clear();
            detailsList.getChildren().addAll(
                    createDetailRow("Shipment Reference",
                            !cId.isBlank() ? ("SHP-" + Math.abs(cId.hashCode() % 8999 + 1000)) : "N/A"),
                    createDetailRow("Cargo Name", cName),
                    createDetailRow("Route", originPort + " → " + destPort),
                    createDetailRow("Transit Distance", String.format("%,.0f km", breakdown.distanceKm)),
                    createDetailRow("Container Freight (" + breakdown.containerType + ")",
                            String.format("₹ %,.0f", breakdown.containerFreightFee)),
                    createDetailRow("Port Handling & Dues", String.format("₹ %,.0f", breakdown.portHandlingFee)),
                    createDetailRow("Customs Duty Assessment", String.format("₹ %,.0f", breakdown.customsDuty)),
                    createDetailRow("Total Amount Payable", totalStr));
        };

        cargoSelect.setOnAction(e -> updateCargoDetails.run());
        if (!cargoSelect.getItems().isEmpty()) {
            updateCargoDetails.run();
        }

        // Embedded Razorpay Checkout Overlay
        StackPane rootStack = new StackPane();

        StackPane webOverlay = new StackPane();
        webOverlay.setStyle("-fx-background-color: rgba(15, 23, 42, 0.75);");
        webOverlay.setVisible(false);
        webOverlay.setManaged(false);

        VBox webContainer = new VBox();
        webContainer.setMaxSize(960, 680);
        webContainer.setStyle(
                "-fx-background-color: #0F172A; -fx-background-radius: 14px; -fx-border-color: #334155; -fx-border-radius: 14px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 30, 0, 0, 10);");

        HBox webHeader = new HBox(12);
        webHeader.setAlignment(Pos.CENTER_LEFT);
        webHeader.setPadding(new Insets(12, 18, 12, 18));
        webHeader.setStyle(
                "-fx-background-color: #1E293B; -fx-background-radius: 14px 14px 0 0; -fx-border-color: #334155; -fx-border-width: 0 0 1 0;");

        Label webTitle = new Label("🔒 Razorpay Secure Test Checkout (Embedded)");
        webTitle.setStyle("-fx-text-fill: #F8FAFC; -fx-font-weight: bold; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeWebBtn = new Button("✕ Cancel & Return");
        closeWebBtn.setStyle(
                "-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 14; -fx-background-radius: 6px; -fx-cursor: hand;");

        webHeader.getChildren().addAll(webTitle, spacer, closeWebBtn);
        webContainer.getChildren().add(webHeader);
        webOverlay.getChildren().add(webContainer);

        closeWebBtn.setOnAction(evt -> {
            razorpayService.cancelActiveCheckout();
            webOverlay.setVisible(false);
            webOverlay.setManaged(false);
            payButton.setDisable(false);
            payButton.setText("🔒  Proceed to Pay via Razorpay");
        });

        payButton.setOnAction(e -> {
            try {
                com.projectui.util.JavaFXModuleHelper.allowJavaFXWebAccess();
                String sel = cargoSelect.getValue();
                String cId = "";
                if (sel != null && !sel.startsWith("No Registered") && !sel.startsWith("No Cargos") && sel.contains(" — ")) {
                    cId = sel.split(" — ")[0].trim();
                } else if (sel != null && !sel.startsWith("No Registered") && !sel.startsWith("No Cargos")) {
                    cId = sel.trim();
                }
                if (cId.isBlank()) {
                    Alert noSelAlert = new Alert(Alert.AlertType.WARNING);
                    noSelAlert.setTitle("Select Cargo");
                    noSelAlert.setHeaderText("No Cargo Selected");
                    noSelAlert.setContentText("Please select a valid registered cargo consignment to proceed with payment.");
                    noSelAlert.show();
                    return;
                }
                Map<String, Object> c = cargoDao.getCargo(cId);
                String cName = c != null && c.get("cargoName") != null ? c.get("cargoName").toString() : "Cargo";
                String originPort = c != null && c.get("originPort") != null ? c.get("originPort").toString()
                        : "Port of Mumbai";

                Map<String, Object> booking = (!cId.isBlank())
                        ? new com.projectui.dao.ContainerDao().getBookingByCargoId(cId)
                        : null;
                com.projectui.service.FreightCalculationService.PaymentBreakdown breakdown = com.projectui.service.FreightCalculationService
                        .getInstance().calculatePaymentBreakdown(c, booking);

                double amt = (breakdown != null) ? breakdown.totalPayable : 0.0;
                if (amt <= 0) {
                    System.err.println("[Razorpay] Unable to determine payment amount (amount <= 0).");
                    payButton.setDisable(false);
                    payButton.setText("🔒  Proceed to Pay via Razorpay");
                    Alert errAlert = new Alert(Alert.AlertType.ERROR);
                    errAlert.setTitle("Invalid Payment Amount");
                    errAlert.setHeaderText("Unable to determine the payment amount");
                    errAlert.setContentText("Please select a valid cargo with calculated payable dues.");
                    errAlert.showAndWait();
                    return;
                }

                String receiptId = "RCPT-" + (System.currentTimeMillis() % 1000000);
                payButton.setDisable(true);
                payButton.setText("⏳  Opening Razorpay Checkout...");

                final String finalCId = cId;
                final String finalCName = cName;
                final String finalOriginPort = originPort;
                final double finalAmt = amt;

                // Check if API credentials are provided in ~/.docktech_razorpay.properties or
                // environment
                if (!razorpayService.hasValidCredentials()) {
                    System.err.println(
                            "[Razorpay] Error: Credentials not configured in ~/.docktech_razorpay.properties or environment variables.");
                    payButton.setDisable(false);
                    payButton.setText("🔒  Proceed to Pay ₹ " + String.format("%,.0f", finalAmt) + " via Razorpay");
                    Alert configAlert = new Alert(Alert.AlertType.WARNING);
                    configAlert.setTitle("Razorpay Configuration Required");
                    configAlert.setHeaderText("Razorpay API Credentials Not Found");
                    configAlert
                            .setContentText("Please configure your credentials in ~/.docktech_razorpay.properties:\n\n"
                                    + "razorpay.key_id=rzp_test_...\n"
                                    + "razorpay.key_secret=...\n\n"
                                    + "Or set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET in your environment.");
                    configAlert.showAndWait();
                    return;
                }

                // 1. Create real order via RazorpayService
                System.out.println("[Razorpay] Creating order");
                String orderId;
                try {
                    orderId = razorpayService.createOrder(finalAmt, "INR", receiptId, finalCName);
                    if (orderId == null || !orderId.startsWith("order_")) {
                        throw new IllegalStateException("Invalid order response from Razorpay: " + orderId);
                    }
                } catch (Exception orderEx) {
                    System.err.println("[Razorpay] Order creation failed: " + orderEx.getMessage());
                    payButton.setDisable(false);
                    payButton.setText("🔒  Proceed to Pay ₹ " + String.format("%,.0f", finalAmt) + " via Razorpay");
                    Alert errAlert = new Alert(Alert.AlertType.ERROR);
                    errAlert.setTitle("Razorpay Order Creation Failed");
                    errAlert.setHeaderText("Could not create Razorpay Order");
                    errAlert.setContentText(orderEx.getMessage());
                    errAlert.showAndWait();
                    return;
                }
                System.out.println("[Razorpay] Order created: " + orderId);
                System.out.println("[Razorpay] Opening real Razorpay Checkout");

                // 2. Prepare WebView container lazily
                WebView webView;
                if (webContainer.getChildren().size() > 1
                        && webContainer.getChildren().get(1) instanceof WebView existingWv) {
                    webView = existingWv;
                } else {
                    webView = new WebView();
                    webView.getEngine().setJavaScriptEnabled(true);
                    webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                        if (newState == javafx.concurrent.Worker.State.FAILED) {
                            System.err.println("[Razorpay] WebView load failed: "
                                    + webView.getEngine().getLoadWorker().getException());
                        }
                    });
                    VBox.setVgrow(webView, Priority.ALWAYS);
                    if (webContainer.getChildren().size() > 1) {
                        webContainer.getChildren().set(1, webView);
                    } else {
                        webContainer.getChildren().add(webView);
                    }
                }

                // 3. Launch Embedded Razorpay Test Checkout Flow in WebView
                razorpayService.startEmbeddedCheckout(finalAmt, orderId, finalCName, finalOriginPort,
                        checkoutUrl -> Platform.runLater(() -> {
                            webView.getEngine().load(checkoutUrl);
                            webOverlay.setVisible(true);
                            webOverlay.setManaged(true);
                            System.out.println("[Razorpay] Checkout opened");
                        }),
                        new RazorpayService.PaymentCallback() {
                            @Override
                            public void onSuccess(String verifiedOrderId, String paymentId, String signature) {
                                Platform.runLater(() -> {
                                    webOverlay.setVisible(false);
                                    webOverlay.setManaged(false);

                                    // 1. Generate unique receipt number
                                    String receiptNo = "RCT-" + (System.currentTimeMillis() % 1000000);
                                    String destPort = c != null && c.get("destinationPort") != null ? c.get("destinationPort").toString() : "Port of Singapore";
                                    String importerName = c != null && c.get("importer") != null ? c.get("importer").toString() : "Global Traders Ltd.";
                                    String exporterName = c != null && c.get("exporter") != null ? c.get("exporter").toString()
                                            : (c != null && c.get("company") != null ? c.get("company").toString() : "Bharat Logistics & Freight Ltd.");

                                    String txnId = "TXN-" + (System.currentTimeMillis() % 1000000);
                                    String invId = "INV-" + (Math.abs(finalCId.hashCode()) % 89999 + 10000);
                                    String contNo = booking != null && booking.get("bookingId") != null
                                            ? booking.get("bookingId").toString()
                                            : "—";
                                    String contType = booking != null && booking.get("containerType") != null
                                            ? booking.get("containerType").toString()
                                            : "Standard Container";

                                    // 2. Prepare comprehensive payment record
                                    Map<String, Object> paymentRecord = new HashMap<>();
                                    paymentRecord.put("paymentId", paymentId);
                                    paymentRecord.put("razorpayPaymentId", paymentId);
                                    paymentRecord.put("orderId", verifiedOrderId);
                                    paymentRecord.put("razorpayOrderId", verifiedOrderId);
                                    paymentRecord.put("signature", signature);
                                    paymentRecord.put("receiptNumber", receiptNo);
                                    paymentRecord.put("invoiceId", invId);
                                    paymentRecord.put("transactionId", txnId);
                                    paymentRecord.put("cargoId", finalCId);
                                    paymentRecord.put("cargoName", finalCName);
                                    paymentRecord.put("cargoType", c != null && c.get("cargoType") != null ? c.get("cargoType").toString() : "Export Freight");
                                    paymentRecord.put("weight", c != null && c.get("weight") != null ? c.get("weight").toString() : "—");
                                    paymentRecord.put("originPort", finalOriginPort);
                                    paymentRecord.put("destinationPort", destPort);
                                    paymentRecord.put("importerName", importerName);
                                    paymentRecord.put("exporterName", exporterName);
                                    paymentRecord.put("customerName", exporterName);
                                    paymentRecord.put("customerType", "Exporter");
                                    paymentRecord.put("containerNo", contNo);
                                    paymentRecord.put("containerType", contType);
                                    paymentRecord.put("port", finalOriginPort);
                                    paymentRecord.put("amount", finalAmt);
                                    paymentRecord.put("currency", "INR");
                                    paymentRecord.put("paymentStatus", "PAID");
                                    paymentRecord.put("paymentMethod", paymentMethodCombo.getValue() != null ? paymentMethodCombo.getValue() : "Razorpay Standard Checkout");
                                    paymentRecord.put("freightCharge", breakdown != null ? breakdown.containerFreightFee : (finalAmt * 0.55));
                                    paymentRecord.put("portHandling", breakdown != null ? breakdown.portHandlingFee : (finalAmt * 0.15));
                                    paymentRecord.put("customsDuty", breakdown != null ? breakdown.customsDuty : (finalAmt * 0.30));

                                    // 3. Save to PaymentDao (persistent + Firestore)
                                    new com.projectui.dao.PaymentDao().savePayment(paymentRecord);

                                    // 4. Generate official PDF receipt
                                    File pdfReceipt = com.projectui.service.ReceiptPdfService.generateReceipt(paymentRecord);
                                    if (pdfReceipt != null) {
                                        paymentRecord.put("receiptPdfPath", pdfReceipt.getAbsolutePath());
                                    }

                                    // 5. Update Cargo status & payment metadata in DAO
                                    cargoDao.updateCargoStatus(finalCId, "Paid / Ready for Shipment");
                                    cargoDao.updateCargoPaymentStatus(finalCId, "PAID", paymentId, verifiedOrderId, finalAmt, receiptNo);

                                    // 6. Record payment in Port Authority PaymentPage
                                    com.projectui.view.portauthority.PaymentPage.recordPayment(
                                            paymentId, invId, finalCId, finalCName,
                                            exporterName, "Exporter",
                                            contNo, contType,
                                            finalAmt, paymentMethodCombo.getValue(), "Paid", txnId, finalOriginPort);

                                    successAlert.setText("✓  Payment of ₹ " + String.format("%,.0f", finalAmt)
                                            + " verified successfully via Razorpay!\nReceipt: " + receiptNo
                                            + " • Payment ID: " + paymentId);
                                    successAlert.setVisible(true);
                                    successAlert.setManaged(true);

                                    nextWorkflowBtn.setVisible(true);
                                    nextWorkflowBtn.setManaged(true);

                                    payButton.setDisable(true);
                                    payButton.setText(
                                            "✓  Payment Complete (₹ " + String.format("%,.0f", finalAmt) + ")");
                                    payButton.setStyle(
                                            "-fx-background-color: #94A3B8; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 9px;");

                                    // 7. Display confirmation alert with option to view Receipt
                                    System.out.println("[Razorpay] Payment SUCCESS — Recorded: " + receiptNo + " for Cargo: " + finalCId);
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Payment Successful");
                                    alert.setHeaderText("✓ Payment Completed & Verified");
                                    alert.setContentText("Payment of ₹ " + String.format("%,.0f", finalAmt)
                                            + " has been successfully verified via Razorpay!\n\n"
                                            + "Receipt Number: " + receiptNo + "\n"
                                            + "Payment ID: " + paymentId + "\n"
                                            + "Order ID: " + verifiedOrderId + "\n"
                                            + "Cargo Status: Paid / Ready for Shipment\n"
                                            + "Official PDF Receipt: Generated & Saved\n\n"
                                            + "Returning to your Exporter Dashboard...");

                                    ButtonType viewReceiptBtn = new ButtonType("📄 View PDF Receipt", ButtonBar.ButtonData.OTHER);
                                    ButtonType okBtn = new ButtonType("Go to Dashboard", ButtonBar.ButtonData.OK_DONE);
                                    alert.getButtonTypes().setAll(viewReceiptBtn, okBtn);

                                    Optional<ButtonType> res = alert.showAndWait();
                                    if (res.isPresent() && res.get() == viewReceiptBtn && pdfReceipt != null) {
                                        com.projectui.service.ReceiptPdfService.openReceipt(pdfReceipt);
                                    }

                                    if (ExporterDashboard.getInstance() != null) {
                                        ExporterDashboard.getInstance().switchPage("Dashboard");
                                    } else {
                                        back();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(String reason) {
                                Platform.runLater(() -> {
                                    webOverlay.setVisible(false);
                                    webOverlay.setManaged(false);
                                    payButton.setDisable(false);
                                    payButton.setText("🔒  Proceed to Pay ₹ " + String.format("%,.0f", finalAmt)
                                            + " via Razorpay");
                                    payButton.setStyle(
                                            "-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 9px; -fx-cursor: hand;");

                                    Alert alert = new Alert(Alert.AlertType.WARNING);
                                    alert.setTitle("Payment Notice");
                                    alert.setHeaderText("Razorpay Payment Notice");
                                    alert.setContentText(reason != null ? reason : "Payment was not completed.");
                                    alert.showAndWait();
                                });
                            }
                        });

            } catch (Exception ex) {
                payButton.setDisable(false);
                payButton.setText("🔒  Proceed to Pay via Razorpay");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Payment Error");
                alert.setHeaderText("Order Creation Failed");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });

        workspace.getChildren().addAll(summaryCard, rightCol);
        content.getChildren().addAll(headerBanner, workspace);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rootStack.getChildren().addAll(scrollPane, webOverlay);
        return rootStack;
    }

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

    private VBox createSummaryTile(String label, String value) {
        return createSummaryTile(label, value, TEXT_PRIMARY);
    }

    private VBox createSummaryTile(String label, String value, String valueColor) {
        VBox tile = new VBox(4);
        tile.setPadding(new Insets(14));
        GridPane.setHgrow(tile, Priority.ALWAYS);
        tile.setStyle(
                "-fx-background-color: #F8FAFC; -fx-background-radius: 10px; -fx-border-color: #E2E8F0; -fx-border-radius: 10px;");

        Text l = new Text(label);
        l.setStyle("-fx-font-size: 11px; -fx-fill: " + TEXT_MUTED + "; -fx-font-weight: bold;");

        Text v = new Text(value);
        v.setStyle("-fx-font-size: 16px; -fx-fill: " + valueColor + "; -fx-font-weight: bold;");

        tile.getChildren().addAll(l, v);
        return tile;
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
                RegisterCargo p = new RegisterCargo();
                com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage,
                        p.getRegisterCargoScene(this::back).getRoot());
            }
        });

        book.setOnAction(e -> {
            if (ExporterDashboard.getInstance() != null) {
                ExporterDashboard.getInstance().switchPage("Book Container");
            } else {
                BookContainer p = new BookContainer();
                com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage,
                        p.getBookContainerScene(this::back).getRoot());
            }
        });

        upload.setOnAction(e -> {
            if (ExporterDashboard.getInstance() != null) {
                ExporterDashboard.getInstance().switchPage("Upload Document");
            } else {
                UploadDocument p = new UploadDocument();
                com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage,
                        p.getUploadDocumentScene(this::back).getRoot());
            }
        });

        payment.setOnAction(e -> {
            if (ExporterDashboard.getInstance() != null) {
                ExporterDashboard.getInstance().switchPage("Make Payment");
            }
        });

        track.setOnAction(e -> {
            if (ExporterDashboard.getInstance() != null) {
                ExporterDashboard.getInstance().switchPage("Track Shipment");
            } else {
                TrackShipment p = new TrackShipment();
                com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage,
                        p.getTrackShipmentScene(this::back).getRoot());
            }
        });

        return com.projectui.view.login.DashboardLayoutHelper.createSidebar(ExporterDashboard.dashboardStage,
                "Exporter", "Make Payment", menuButtons);
    }

    private void back() {
        if (ExporterDashboard.getInstance() != null) {
            ExporterDashboard.getInstance().switchPage("Dashboard");
        } else if (ExporterDashboard.dashboardStage != null) {
            Parent r = new ExporterDashboard().getRoot(ExporterDashboard.dashboardStage, null, "Dashboard");
            com.projectui.view.login.LoginPage.showRoot(ExporterDashboard.dashboardStage, r);
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