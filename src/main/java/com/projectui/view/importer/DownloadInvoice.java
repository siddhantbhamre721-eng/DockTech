package com.projectui.view.importer;

import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.projectui.dao.CargoDao;
import com.projectui.dao.PaymentDao;
import com.projectui.service.ReceiptPdfService;
import com.projectui.view.login.DashboardLayoutHelper;
import com.projectui.view.login.LoginPage;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

@SuppressWarnings({"unused", "unchecked"})
public class DownloadInvoice {

    private final PaymentDao paymentDao = new PaymentDao();
    private final CargoDao cargoDao = new CargoDao();
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private VBox tableContentBox;
    private Label totalPaidLbl, totalPendingLbl, totalAmountLbl, invoiceCountLbl;
    private String activeFilter = "All";
    private String searchQuery = "";
    private final List<Map<String, Object>> allPayments = new ArrayList<>();

    private static final String CARD_STYLE = "-fx-background-color:white; -fx-background-radius:12px; -fx-border-color:#e2e8f0; -fx-border-radius:12px; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 10, 0, 0, 3);";

    public Scene getDownloadInvoiceScene(Runnable back) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#F4F8FC;");
        root.setTop(createHeader("Payment History & Invoices", "Dock Tech / Importer / Payment History"));
        root.setLeft(createSidebar(back));

        VBox content = buildMainContent(back);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color:#F4F8FC; -fx-background:transparent; -fx-border-color:transparent;");

        root.setCenter(scroll);

        // Reactive listener
        PaymentDao.addChangeListener(this::refreshData);
        refreshData();

        Scene scene = new Scene(root, 1400, 800);
        LoginPage.applyInvisibleScrollbar(scene);
        return scene;
    }

    private VBox buildMainContent(Runnable back) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20, 24, 24, 24));
        container.setStyle("-fx-background-color:#F4F8FC;");

        // 1. Header & Summary Banner
        HBox headerRow = buildHeaderBanner();

        // 2. Financial Overview KPI Cards
        HBox kpiRow = buildKpiRow();

        // 3. Search & Filter Bar
        HBox searchBar = buildSearchBar();

        // 4. Invoices & Payment Ledger Table
        VBox tableContainer = new VBox(12);
        tableContainer.setStyle(CARD_STYLE);
        tableContainer.setPadding(new Insets(16));

        HBox headerRowTable = createHeaderRow();
        tableContentBox = new VBox(8);
        tableContentBox.setPadding(new Insets(6, 0, 0, 0));

        renderPaymentRows();

        tableContainer.getChildren().addAll(headerRowTable, new Separator(), tableContentBox);

        container.getChildren().addAll(headerRow, kpiRow, searchBar, tableContainer);
        return container;
    }

    private HBox buildHeaderBanner() {
        VBox titleBox = new VBox(4);
        Text title = new Text("Financial Invoices & Payment History");
        title.setStyle("-fx-font-size:26px; -fx-font-weight:bold; -fx-fill:#101A2B;");

        String importerName = getLoggedImporterName();
        Text subtitle = new Text("Review official customs tariff settlements, download stamped PDF tax receipts, and verify transactions for " + importerName + ".");
        subtitle.setStyle("-fx-font-size:13px; -fx-fill:#64748B;");
        titleBox.getChildren().addAll(title, subtitle);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Button refreshBtn = new Button("↺ Refresh Invoices");
        refreshBtn.setStyle("-fx-background-color:white; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:6 14; -fx-font-size:12; -fx-font-weight:bold; -fx-text-fill:#334155; -fx-cursor:hand;");
        refreshBtn.setOnAction(e -> refreshData());

        HBox row = new HBox(12, titleBox, space, refreshBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox buildKpiRow() {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);

        invoiceCountLbl = new Label("0");
        totalPaidLbl = new Label("0");
        totalPendingLbl = new Label("0");
        totalAmountLbl = new Label("₹ 0");

        VBox c1 = finCard("📄", "Total Invoices", invoiceCountLbl, "#e0f2fe", "#0284c7");
        VBox c2 = finCard("✓", "Settled & Paid", totalPaidLbl, "#dcfce7", "#16a34a");
        VBox c3 = finCard("◷", "Pending Payment", totalPendingLbl, "#fef3c7", "#d97706");
        VBox c4 = finCard("₹", "Total Duty Paid", totalAmountLbl, "#f1f5f9", "#0f172a");

        row.getChildren().addAll(c1, c2, c3, c4);
        row.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        return row;
    }

    private VBox finCard(String icon, String title, Label valLbl, String bg, String col) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle(CARD_STYLE);

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label ic = new Label(icon);
        ic.setMinSize(38, 38);
        ic.setMaxSize(38, 38);
        ic.setAlignment(Pos.CENTER);
        ic.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + col + "; -fx-font-size:18px; -fx-font-weight:bold; -fx-background-radius:8px;");

        VBox textB = new VBox(2);
        valLbl.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
        Label t = new Label(title);
        t.setStyle("-fx-font-size:12px; -fx-text-fill:#64748b; -fx-font-weight:bold;");
        textB.getChildren().addAll(valLbl, t);

        top.getChildren().addAll(ic, textB);
        card.getChildren().add(top);
        return card;
    }

    private HBox buildSearchBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by Receipt No, Cargo ID, Payment ID, Commodity...");
        searchField.setPrefWidth(420);
        searchField.setPrefHeight(36);
        searchField.setStyle("-fx-background-color:white; -fx-border-color:#CBD5E1; -fx-border-radius:6px; -fx-background-radius:6px; -fx-font-size:12px; -fx-padding:6 10;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            this.searchQuery = newVal != null ? newVal.trim().toLowerCase() : "";
            renderPaymentRows();
        });

        Button allBtn = createFilterButton("All Transactions", "All".equalsIgnoreCase(activeFilter));
        Button paidBtn = createFilterButton("Paid & Cleared", "Paid".equalsIgnoreCase(activeFilter));
        Button pendingBtn = createFilterButton("Pending", "Pending".equalsIgnoreCase(activeFilter));

        allBtn.setOnAction(e -> setActiveFilterBtn(allBtn, paidBtn, pendingBtn, "All"));
        paidBtn.setOnAction(e -> setActiveFilterBtn(paidBtn, allBtn, pendingBtn, "Paid"));
        pendingBtn.setOnAction(e -> setActiveFilterBtn(pendingBtn, allBtn, paidBtn, "Pending"));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        bar.getChildren().addAll(searchField, allBtn, paidBtn, pendingBtn);
        return bar;
    }

    private void setActiveFilterBtn(Button active, Button b1, Button b2, String filter) {
        this.activeFilter = filter;
        active.setStyle("-fx-background-color:#0b63ff; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:6px; -fx-padding:6 14; -fx-cursor:hand;");
        b1.setStyle("-fx-background-color:white; -fx-text-fill:#64748b; -fx-border-color:#cbd5e1; -fx-border-radius:6px; -fx-background-radius:6px; -fx-padding:6 14; -fx-cursor:hand;");
        b2.setStyle("-fx-background-color:white; -fx-text-fill:#64748b; -fx-border-color:#cbd5e1; -fx-border-radius:6px; -fx-background-radius:6px; -fx-padding:6 14; -fx-cursor:hand;");
        renderPaymentRows();
    }

    private void refreshData() {
        Platform.runLater(() -> {
            List<Map<String, Object>> payments;
            if (LoginPage.loggedInUser != null && !LoginPage.loggedInUser.isEmpty()) {
                payments = paymentDao.getPaymentsForImporter(LoginPage.loggedInUser);
            } else {
                String loggedUser = getLoggedImporterFilter();
                payments = paymentDao.getPaymentsForImporter(loggedUser);
            }

            allPayments.clear();
            if (payments != null) {
                allPayments.addAll(payments);
            }

            updateSummaryKpis();
            renderPaymentRows();
        });
    }

    private void updateSummaryKpis() {
        int count = allPayments.size();
        int paid = 0;
        int pending = 0;
        double sum = 0.0;

        for (Map<String, Object> p : allPayments) {
            if (p == null) continue;
            String st = getString(p, "paymentStatus", "PAID");
            double amt = getDouble(p, "amount", getDouble(p, "paymentAmount", 0.0));
            if (st.toUpperCase().contains("PAID") || st.toUpperCase().contains("COMPLET")) {
                paid++;
                sum += amt;
            } else {
                pending++;
            }
        }

        if (invoiceCountLbl != null) invoiceCountLbl.setText(String.valueOf(count));
        if (totalPaidLbl != null) totalPaidLbl.setText(String.valueOf(paid));
        if (totalPendingLbl != null) totalPendingLbl.setText(String.valueOf(pending));
        if (totalAmountLbl != null) totalAmountLbl.setText(formatCurrency(sum));
    }

    private void renderPaymentRows() {
        if (tableContentBox == null) return;
        tableContentBox.getChildren().clear();

        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> p : allPayments) {
            if (p == null) continue;
            String rNo = getString(p, "receiptNumber", "");
            String cId = getString(p, "cargoId", "");
            String cName = getString(p, "cargoName", "");
            String payId = getString(p, "razorpayPaymentId", getString(p, "paymentId", ""));
            String st = getString(p, "paymentStatus", "PAID");

            boolean matchesSearch = searchQuery.isEmpty()
                    || rNo.toLowerCase().contains(searchQuery)
                    || cId.toLowerCase().contains(searchQuery)
                    || cName.toLowerCase().contains(searchQuery)
                    || payId.toLowerCase().contains(searchQuery);

            if (!matchesSearch) continue;

            if ("Paid".equalsIgnoreCase(activeFilter) && !st.toUpperCase().contains("PAID")) continue;
            if ("Pending".equalsIgnoreCase(activeFilter) && st.toUpperCase().contains("PAID")) continue;

            filtered.add(p);
        }

        if (filtered.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(40));
            Label emptyLbl = new Label(allPayments.isEmpty()
                    ? "No invoices are available for your shipments."
                    : "No payment or invoice records found matching your query.");
            emptyLbl.setStyle("-fx-text-fill:#94A3B8; -fx-font-size:14px; -fx-font-weight:bold;");
            emptyBox.getChildren().add(emptyLbl);
            tableContentBox.getChildren().add(emptyBox);
            return;
        }

        for (Map<String, Object> p : filtered) {
            tableContentBox.getChildren().add(createPaymentRow(p));
        }
    }

    private HBox createHeaderRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color:#F8FAFC; -fx-background-radius:8px;");

        Label col1 = createCellLabel("RECEIPT / INVOICE NO.", 160, true);
        Label col2 = createCellLabel("CONSIGNMENT & CARGO", 220, true);
        Label col3 = createCellLabel("PAYMENT DATE", 140, true);
        Label col4 = createCellLabel("DUTY AMOUNT", 130, true);
        Label col5 = createCellLabel("TRANSACTION ID", 170, true);
        Label col6 = createCellLabel("STATUS", 100, true);
        Label col7 = createCellLabel("ACTIONS & DOCUMENTS", 180, true);

        row.getChildren().addAll(col1, col2, col3, col4, col5, col6, col7);
        return row;
    }

    private HBox createPaymentRow(Map<String, Object> p) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        row.setStyle("-fx-background-color:white; -fx-background-radius:8px; -fx-border-color:#F1F5F9; -fx-border-radius:8px;");

        String receiptNo = getString(p, "receiptNumber", getString(p, "paymentId", "RCT-N/A"));
        String cargoId = getString(p, "cargoId", "—");
        String cargoName = getString(p, "cargoName", "—");
        String date = getString(p, "paymentDate", getString(p, "paidAt", "Today"));
        double amt = getDouble(p, "amount", getDouble(p, "paymentAmount", 0.0));
        String payId = getString(p, "razorpayPaymentId", getString(p, "paymentId", "—"));
        String status = getString(p, "paymentStatus", "PAID");
        boolean isPaid = "PAID".equalsIgnoreCase(status) || status.toUpperCase().contains("PAID") || status.toUpperCase().contains("COMPLET");

        Label lReceipt = createCellLabel(receiptNo, 160, false);
        lReceipt.setStyle("-fx-font-weight:bold; -fx-text-fill:#0284C7; -fx-font-size:12px;");

        Label lCargo = createCellLabel(cargoId.equals("—") ? "—" : (cargoId + " (" + cargoName + ")"), 220, false);
        lCargo.setStyle("-fx-text-fill:#1E293B; -fx-font-size:12px; -fx-font-weight:bold;");

        Label lDate = createCellLabel(date, 140, false);
        lDate.setStyle("-fx-text-fill:#64748B; -fx-font-size:11px;");

        Label lAmt = createCellLabel(formatCurrency(amt), 130, false);
        lAmt.setStyle("-fx-font-weight:bold; -fx-text-fill:#0F172A; -fx-font-size:12px;");

        Label lPayId = createCellLabel(payId, 170, false);
        lPayId.setStyle("-fx-text-fill:#64748B; -fx-font-size:11px;");

        Label lStatus = new Label(isPaid ? "✓ PAID" : "PENDING");
        lStatus.setPrefWidth(100);
        String color = isPaid ? "#16A34A" : "#D97706";
        String bg = isPaid ? "#DCFCE7" : "#FEF3C7";
        lStatus.setStyle("-fx-text-fill:" + color + "; -fx-background-color:" + bg + "; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;");

        HBox actionBox = new HBox(6);
        actionBox.setPrefWidth(180);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        Button viewPdfBtn = new Button("📄 PDF Receipt");
        viewPdfBtn.setStyle("-fx-background-color:#0b63ff; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6px; -fx-padding:5 10; -fx-cursor:hand;");
        viewPdfBtn.setOnAction(e -> {
            File pdf = ReceiptPdfService.generateReceipt(p);
            if (pdf != null) {
                ReceiptPdfService.openReceipt(pdf);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Receipt Error");
                alert.setHeaderText("Could not generate PDF");
                alert.setContentText("Please verify file system permissions.");
                alert.showAndWait();
            }
        });

        Button detailsBtn = new Button("🔍 Details");
        detailsBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#334155; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6px; -fx-padding:5 8; -fx-cursor:hand;");
        detailsBtn.setOnAction(e -> showInvoiceDetailsModal(p));

        actionBox.getChildren().addAll(viewPdfBtn, detailsBtn);

        row.getChildren().addAll(lReceipt, lCargo, lDate, lAmt, lPayId, lStatus, actionBox);
        return row;
    }

    private void showInvoiceDetailsModal(Map<String, Object> p) {
        Stage dialog = new Stage();
        Window owner = ImporterDashboard.importerStage != null ? ImporterDashboard.importerStage : null;
        if (owner != null) dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Tax Invoice & Payment Breakdown");
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setPrefWidth(580);
        root.setStyle("-fx-background-color:white; -fx-background-radius:12;");

        String receiptNo = getString(p, "receiptNumber", getString(p, "paymentId", "RCT-N/A"));
        String cargoId = getString(p, "cargoId", "—");
        String cargoName = getString(p, "cargoName", "—");
        String payId = getString(p, "razorpayPaymentId", getString(p, "paymentId", "—"));
        double amt = getDouble(p, "amount", getDouble(p, "paymentAmount", 0.0));
        String date = getString(p, "paymentDate", getString(p, "paidAt", "Today"));
        String status = getString(p, "paymentStatus", "PAID");

        HBox hdr = new HBox(12);
        hdr.setAlignment(Pos.CENTER_LEFT);
        Label b = new Label("INVOICE " + receiptNo);
        b.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0284c7; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:6;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label st = new Label(status.toUpperCase());
        st.setStyle("-fx-background-color:#dcfce7; -fx-text-fill:#16a34a; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;");
        hdr.getChildren().addAll(b, sp, st);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.add(detailCell("Related Consignment ID", cargoId), 0, 0);
        grid.add(detailCell("Cargo Description", cargoName), 1, 0);
        grid.add(detailCell("Transaction / Payment ID", payId), 0, 1);
        grid.add(detailCell("Payment Settlement Date", date), 1, 1);
        grid.add(detailCell("Consignee / Importer", getLoggedImporterName()), 0, 2);
        grid.add(detailCell("Duty Amount Settled", formatCurrency(amt)), 1, 2);

        HBox acts = new HBox(12);
        acts.setAlignment(Pos.CENTER_RIGHT);
        Button close = new Button("Close");
        close.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#334155; -fx-font-weight:bold; -fx-padding:8 16; -fx-background-radius:6; -fx-cursor:hand;");
        close.setOnAction(e -> dialog.close());

        Button openPdf = new Button("📄 Open PDF Receipt");
        openPdf.setStyle("-fx-background-color:#0b63ff; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:8 16; -fx-background-radius:6; -fx-cursor:hand;");
        openPdf.setOnAction(e -> {
            dialog.close();
            File pdf = ReceiptPdfService.generateReceipt(p);
            if (pdf != null) ReceiptPdfService.openReceipt(pdf);
        });

        acts.getChildren().addAll(close, openPdf);

        root.getChildren().addAll(hdr, new Separator(), grid, new Separator(), acts);
        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.show();
    }

    private static VBox detailCell(String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px; -fx-font-weight:bold;");
        Label v = new Label(value != null && !value.isBlank() ? value : "N/A");
        v.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13px; -fx-font-weight:bold;");
        return new VBox(2, l, v);
    }

    private Label createCellLabel(String text, double width, boolean isHeader) {
        Label l = new Label(text);
        l.setPrefWidth(width);
        if (isHeader) {
            l.setStyle("-fx-text-fill:#64748B; -fx-font-size:11px; -fx-font-weight:bold;");
        } else {
            l.setStyle("-fx-text-fill:#334155; -fx-font-size:12px;");
        }
        return l;
    }

    private Button createFilterButton(String text, boolean active) {
        Button b = new Button(text);
        if (active) {
            b.setStyle("-fx-background-color:#0b63ff; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:6px; -fx-padding:6 14; -fx-cursor:hand;");
        } else {
            b.setStyle("-fx-background-color:white; -fx-text-fill:#64748b; -fx-border-color:#cbd5e1; -fx-border-radius:6px; -fx-background-radius:6px; -fx-padding:6 14; -fx-cursor:hand;");
        }
        return b;
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

        delivery.setOnAction(e -> {
            ConfirmDelivery page = new ConfirmDelivery();
            LoginPage.showRoot(ImporterDashboard.importerStage, page.getConfirmDeliveryScene(back).getRoot());
        });

        return DashboardLayoutHelper.createSidebar(ImporterDashboard.importerStage, "Importer", "Download Invoice", menuButtons);
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

    private String formatCurrency(double amount) {
        try {
            return currencyFormatter.format(amount);
        } catch (Exception e) {
            return String.format(Locale.US, "₹ %,.0f", amount);
        }
    }

    private String getString(Map<String, Object> map, String key, String fallback) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) return fallback;
        String val = map.get(key).toString().trim();
        return val.isEmpty() ? fallback : val;
    }

    private double getDouble(Map<String, Object> map, String key, double fallback) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) return fallback;
        try {
            return Double.parseDouble(map.get(key).toString().replace("₹", "").replace(",", "").trim());
        } catch (Exception ignored) {
            return fallback;
        }
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
}
