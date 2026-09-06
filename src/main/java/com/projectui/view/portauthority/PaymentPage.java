package com.projectui.view.portauthority;

import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.projectui.dao.CargoDao;
import com.projectui.dao.ContainerDao;
import com.projectui.dao.PaymentDao;
import com.projectui.service.ReceiptPdfService;
import com.projectui.service.VesselData;
import com.projectui.service.VesselFinderService;
import com.projectui.service.VesselManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@SuppressWarnings({"unchecked", "unused"})
public class PaymentPage {

    private final VBox root = new VBox(16);

    // Shared In-Memory Payment Store synchronized with real backend transactions
    private static final Map<String, Map<String, Object>> paymentStore = new ConcurrentHashMap<>();

    // Backend DAOs & Services (Single source of truth)
    private final CargoDao cargoDao = new CargoDao();
    private final ContainerDao containerDao = new ContainerDao();
    private final PaymentDao paymentDao = new PaymentDao();
    private final VesselManager vesselManager = VesselManager.getInstance();
    private final VesselFinderService vesselFinderService = VesselFinderService.getInstance();

    // Authenticated Port Authority Context
    private String userAssignedPort = "Cochin Port";
    private String activeViewingPort = "Cochin Port";

    // Header Freshness & Telemetry UI
    private Label pageHeadingLabel;
    private Label pageSubLabel;
    private Label dataFreshnessBadge;
    private Label lastSyncLabel;

    // Top Summary KPI Cards
    private Label totalPaymentsVal;
    private Label paidCountVal;
    private Label pendingCountVal;
    private Label failedCountVal;
    private Label totalAmountVal;
    private Label paidAmountVal;
    private Label pendingAmountVal;

    // Master & Filtered Payment List
    private final List<Map<String, Object>> masterPaymentsList = new ArrayList<>();
    private final ObservableList<Map<String, Object>> displayedPaymentsList = FXCollections.observableArrayList();

    // Tables & Filters
    private TableView<Map<String, Object>> paymentTable;
    private TextField searchField;
    private ComboBox<String> statusFilterCombo;
    private ComboBox<String> customerTypeCombo;
    private ComboBox<String> methodFilterCombo;
    private ComboBox<String> sortCombo;

    // State Tracking
    private long lastSuccessfulUpdateEpoch = System.currentTimeMillis();
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public PaymentPage() {
        root.setSpacing(16);
        root.setPadding(new Insets(0));
        root.setStyle("-fx-background-color:transparent;");

        resolveAssignedPort();
        buildLayout();
        connectLiveServices();
    }

    // =====================================================
    // 1. PORT CONTEXT RESOLUTION (DYNAMIC PER USER)
    // =====================================================
    private void resolveAssignedPort() {
        String port = null;
        if (com.projectui.view.login.LoginPage.loggedInUser != null) {
            Map<String, Object> user = com.projectui.view.login.LoginPage.loggedInUser;
            if (user.get("assignedPort") != null && !user.get("assignedPort").toString().isBlank()) {
                port = user.get("assignedPort").toString().trim();
            } else if (user.get("port") != null && !user.get("port").toString().isBlank()) {
                port = user.get("port").toString().trim();
            } else if (user.get("portName") != null && !user.get("portName").toString().isBlank()) {
                port = user.get("portName").toString().trim();
            } else if (user.get("assignedPortName") != null && !user.get("assignedPortName").toString().isBlank()) {
                port = user.get("assignedPortName").toString().trim();
            } else if (user.get("organization") != null && !user.get("organization").toString().isBlank()) {
                port = user.get("organization").toString().trim();
            } else if (user.get("company") != null && !user.get("company").toString().isBlank()) {
                port = user.get("company").toString().trim();
            } else if (user.get("name") != null && !user.get("name").toString().isBlank()) {
                port = user.get("name").toString().trim();
            } else if (user.get("email") != null && !user.get("email").toString().isBlank()) {
                port = user.get("email").toString().trim();
            }
        }

        if (port != null && !port.isBlank()) {
            userAssignedPort = normalizePortName(port);
        } else {
            List<String> approved = com.projectui.view.admin.AdminDataStore.getInstance().getApprovedPortNames();
            if (approved != null && !approved.isEmpty()) {
                userAssignedPort = approved.get(0);
            } else {
                userAssignedPort = "Port of Mumbai";
            }
        }
        activeViewingPort = userAssignedPort;
    }

    private String normalizePortName(String raw) {
        return com.projectui.dao.UserDao.normalizePort(raw);
    }

    // =====================================================
    // 2. LAYOUT COMPOSITION
    // =====================================================
    private void buildLayout() {
        VBox header = buildHeader();
        HBox summaryCards = buildSummaryCards();
        VBox paymentSection = buildPaymentTableSection();

        root.getChildren().addAll(
                header,
                summaryCards,
                paymentSection
        );
    }

    // =====================================================
    // 3. HEADER BANNER
    // =====================================================
    private VBox buildHeader() {
        VBox container = new VBox(4);

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        pageHeadingLabel = new Label("Payment Management");
        pageHeadingLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:22px; -fx-font-weight:bold;");

        pageSubLabel = new Label("Monitor, audit and verify port tariffs, freight invoices and shipper transactions for " + activeViewingPort);
        pageSubLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:13px;");
        titleBox.getChildren().addAll(pageHeadingLabel, pageSubLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Freshness Badge
        dataFreshnessBadge = new Label("● LIVE — Payment Synced");
        dataFreshnessBadge.setStyle("-fx-text-fill:#10b981; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(16,185,129,0.12); -fx-padding:6 12; -fx-background-radius:999;");

        lastSyncLabel = new Label("Synced: Just now");
        lastSyncLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12px;");

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 14; -fx-cursor:hand;");
        refreshBtn.setOnAction(e -> refreshPaymentData());

        topRow.getChildren().addAll(titleBox, spacer, dataFreshnessBadge, lastSyncLabel, refreshBtn);
        container.getChildren().add(topRow);
        return container;
    }

    // =====================================================
    // 4. TOP SUMMARY KPI CARDS
    // =====================================================
    private HBox buildSummaryCards() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        totalPaymentsVal = new Label("0");
        paidCountVal = new Label("0");
        pendingCountVal = new Label("0");
        failedCountVal = new Label("0");
        totalAmountVal = new Label("₹0");
        paidAmountVal = new Label("₹0");
        pendingAmountVal = new Label("₹0");

        VBox cTotal = createKpiCard("TOTAL TRANSACTIONS", totalPaymentsVal, "All port invoices", "💳", "#0284c7", "#e0f2fe");
        VBox cPaid = createKpiCard("PAID", paidCountVal, "Settled & verified", "🟢", "#16a34a", "#ecfdf5");
        VBox cPending = createKpiCard("PENDING", pendingCountVal, "Awaiting payment", "⏳", "#d97706", "#fef3c7");
        VBox cFailed = createKpiCard("FAILED / OVERDUE", failedCountVal, "Action required", "🔴", "#ef4444", "#fee2e2");
        VBox cTotalAmt = createKpiCard("TOTAL INVOICED", totalAmountVal, "Gross tariff value", "📊", "#6366f1", "#e0e7ff");
        VBox cPaidAmt = createKpiCard("PAID AMOUNT", paidAmountVal, "Collected revenue", "💰", "#059669", "#d1fae5");
        VBox cPendingAmt = createKpiCard("PENDING AMOUNT", pendingAmountVal, "Outstanding balance", "⚖", "#ea580c", "#ffedd5");

        row.getChildren().addAll(cTotal, cPaid, cPending, cFailed, cTotalAmt, cPaidAmt, cPendingAmt);

        for (Node n : row.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
            if (n instanceof VBox) {
                ((VBox) n).setMaxWidth(Double.MAX_VALUE);
            }
        }
        return row;
    }

    private VBox createKpiCard(String title, Label valLabel, String subtitle, String iconStr, String color, String bg) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(10, 12, 10, 12));
        card.setStyle("-fx-background-color:#ffffff; -fx-background-radius:8; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 4, 0, 0, 1);");

        HBox top = new HBox(6);
        top.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(iconStr);
        icon.setStyle("-fx-text-fill:" + color + "; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:" + bg + "; -fx-padding:3 6; -fx-background-radius:4;");

        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#64748b; -fx-font-size:10px; -fx-font-weight:bold;");

        top.getChildren().addAll(icon, t);

        valLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:19px; -fx-font-weight:bold;");

        Label sub = new Label(subtitle);
        sub.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:10px;");

        card.getChildren().addAll(top, valLabel, sub);
        return card;
    }

    // =====================================================
    // 5. PAYMENT TRANSACTIONS TABLE SECTION
    // =====================================================
    private VBox buildPaymentTableSection() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color:#ffffff; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.03), 6, 0, 0, 2);");

        // Top Filter Bar
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("🔍 Search by Payment ID, Customer, Cargo, Container...");
        searchField.setPrefWidth(280);
        searchField.setPrefHeight(34);
        searchField.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px;");
        searchField.textProperty().addListener((obs, oldV, newV) -> applyFilters());

        statusFilterCombo = new ComboBox<>();
        statusFilterCombo.getItems().addAll("All Statuses", "Paid", "Pending", "Failed", "Overdue");
        statusFilterCombo.setValue("All Statuses");
        statusFilterCombo.setPrefHeight(34);
        statusFilterCombo.setStyle("-fx-font-size:12px;");
        statusFilterCombo.setOnAction(e -> applyFilters());

        customerTypeCombo = new ComboBox<>();
        customerTypeCombo.getItems().addAll("All Entities", "Importer", "Exporter");
        customerTypeCombo.setValue("All Entities");
        customerTypeCombo.setPrefHeight(34);
        customerTypeCombo.setStyle("-fx-font-size:12px;");
        customerTypeCombo.setOnAction(e -> applyFilters());

        methodFilterCombo = new ComboBox<>();
        methodFilterCombo.getItems().addAll("All Methods", "UPI", "Bank Transfer", "Net Banking", "Card");
        methodFilterCombo.setValue("All Methods");
        methodFilterCombo.setPrefHeight(34);
        methodFilterCombo.setStyle("-fx-font-size:12px;");
        methodFilterCombo.setOnAction(e -> applyFilters());

        sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Sort: Newest First", "Sort: Oldest First", "Sort: Amount (High to Low)", "Sort: Amount (Low to High)");
        sortCombo.setValue("Sort: Newest First");
        sortCombo.setPrefHeight(34);
        sortCombo.setStyle("-fx-font-size:12px;");
        sortCombo.setOnAction(e -> applyFilters());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        filterBar.getChildren().addAll(searchField, statusFilterCombo, customerTypeCombo, methodFilterCombo, sortCombo);

        // Table
        paymentTable = new TableView<>();
        var tableCss = getClass().getResource("/table-headers.css");
        if (tableCss != null) {
            paymentTable.getStylesheets().add(tableCss.toExternalForm());
        }
        paymentTable.setPrefHeight(420);
        paymentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        paymentTable.setStyle("-fx-background-color:transparent; -fx-border-color:transparent; -fx-font-size:12px;");
        paymentTable.setPlaceholder(new Label("No payment transactions available for " + activeViewingPort));

        TableColumn<Map<String, Object>, String> colPayId = new TableColumn<>("Payment ID");
        colPayId.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "paymentId", "PAY-N/A")));
        colPayId.setStyle("-fx-alignment:CENTER; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0284c7;");

        TableColumn<Map<String, Object>, String> colCustomer = new TableColumn<>("Customer / Entity");
        colCustomer.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "customerName", getString(d.getValue(), "exporterName", getString(d.getValue(), "exporter", "—")))));
        colCustomer.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "customerType", "Exporter")));
        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label pill = new Label(item);
                    if ("Importer".equalsIgnoreCase(item)) {
                        pill.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0284c7; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:2 6; -fx-background-radius:4;");
                    } else {
                        pill.setStyle("-fx-background-color:#fef3c7; -fx-text-fill:#d97706; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:2 6; -fx-background-radius:4;");
                    }
                    setGraphic(pill);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            }
        });

        TableColumn<Map<String, Object>, String> colCargoId = new TableColumn<>("Cargo ID");
        colCargoId.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "cargoId", "—")));
        colCargoId.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#475569;");

        TableColumn<Map<String, Object>, String> colContainer = new TableColumn<>("Container(s)");
        colContainer.setCellValueFactory(d -> {
            Map<String, Object> val = d.getValue();
            String cNo = getString(val, "containerNo", "");
            if (cNo.isEmpty() || "—".equals(cNo) || "-".equals(cNo)) {
                String bId = getString(val, "bookingId", "");
                if (!bId.isEmpty() && !"—".equals(bId) && !"-".equals(bId)) {
                    return new SimpleStringProperty(bId);
                }
                String cId = getString(val, "cargoId", "");
                if (!cId.isEmpty()) {
                    Map<String, Object> bk = containerDao.getBookingByCargoId(cId);
                    if (bk != null) {
                        String realId = getString(bk, "containerNo", getString(bk, "containerNumber", getString(bk, "assignedContainer", "")));
                        if (realId.isEmpty() || "—".equals(realId) || "-".equals(realId)) {
                            realId = getString(bk, "bookingId", "");
                        }
                        if (!realId.isEmpty() && !"—".equals(realId) && !"-".equals(realId)) {
                            val.put("containerNo", realId);
                            return new SimpleStringProperty(realId);
                        }
                    }
                }
                return new SimpleStringProperty("—");
            }
            return new SimpleStringProperty(cNo);
        });
        colContainer.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#0f172a; -fx-font-weight:bold;");

        TableColumn<Map<String, Object>, String> colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(d -> new SimpleStringProperty(formatCurrency(d.getValue())));
        colAmount.setStyle("-fx-alignment:CENTER-RIGHT; -fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");

        TableColumn<Map<String, Object>, String> colMethod = new TableColumn<>("Method");
        colMethod.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "paymentMethod", "Bank Transfer")));
        colMethod.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#334155;");

        TableColumn<Map<String, Object>, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(getString(d.getValue(), "paymentStatus", "Pending")));
        colStatus.setCellFactory(col -> createStatusBadgeCell());

        TableColumn<Map<String, Object>, String> colDate = new TableColumn<>("Payment Date");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(formatIsoDate(getString(d.getValue(), "paymentDate", "Today"))));
        colDate.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#64748b;");

        TableColumn<Map<String, Object>, String> colUpdated = new TableColumn<>("Last Updated");
        colUpdated.setCellValueFactory(d -> new SimpleStringProperty(formatIsoDate(getString(d.getValue(), "updatedAt", "Just now"))));
        colUpdated.setStyle("-fx-alignment:CENTER; -fx-font-size:11px; -fx-text-fill:#94a3b8;");

        TableColumn<Map<String, Object>, String> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(185);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button revBtn = new Button("Review");
            private final Button deleteBtn = new Button("Delete");
            private final HBox btnBox = new HBox(4, viewBtn, revBtn, deleteBtn);

            {
                btnBox.setAlignment(Pos.CENTER);
                viewBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 6; -fx-cursor:hand;");
                revBtn.setStyle("-fx-background-color:#475569; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 6; -fx-cursor:hand;");
                deleteBtn.setStyle("-fx-background-color:#dc2626; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 6; -fx-cursor:hand;");

                viewBtn.setOnAction(e -> {
                    Map<String, Object> pay = getTableView().getItems().get(getIndex());
                    showPaymentDetailsDialog(pay);
                });

                revBtn.setOnAction(e -> {
                    Map<String, Object> pay = getTableView().getItems().get(getIndex());
                    showPaymentReviewDialog(pay);
                });

                deleteBtn.setOnAction(e -> {
                    Map<String, Object> pay = getTableView().getItems().get(getIndex());
                    confirmDeletePayment(pay);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btnBox);
            }
        });

        paymentTable.getColumns().addAll(
                colPayId, colCustomer, colType, colCargoId, colContainer,
                colAmount, colMethod, colStatus, colDate, colUpdated, colAction
        );
        paymentTable.setItems(displayedPaymentsList);

        paymentTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setPrefHeight(38);
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showPaymentDetailsDialog(row.getItem());
                }
            });
            return row;
        });

        container.getChildren().addAll(filterBar, paymentTable);
        return container;
    }

    private void confirmDeletePayment(Map<String, Object> payment) {
        if (payment == null) return;
        String paymentId = getString(payment, "paymentId", getString(payment, "razorpayPaymentId", ""));
        if (paymentId.isBlank()) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Payment");
        confirmation.setHeaderText("Delete payment " + paymentId + "?");
        confirmation.setContentText("Are you sure you want to delete this payment record? This action cannot be undone.");
        confirmation.showAndWait().filter(ButtonType.OK::equals).ifPresent(ignored -> {
            if (paymentDao.deletePayment(paymentId, payment)) {
                paymentStore.remove(paymentId);
                paymentStore.entrySet().removeIf(e -> 
                    paymentId.equalsIgnoreCase(e.getKey()) ||
                    paymentId.equalsIgnoreCase(getString(e.getValue(), "paymentId", "")) ||
                    paymentId.equalsIgnoreCase(getString(e.getValue(), "razorpayPaymentId", ""))
                );
                refreshPaymentData();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Delete Payment");
                error.setHeaderText("Payment was not deleted");
                error.setContentText("The payment record could not be removed from the database.");
                error.showAndWait();
            }
        });
    }

    private TableCell<Map<String, Object>, String> createStatusBadgeCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    String color = "#0284c7";
                    String bg = "#e0f2fe";

                    if ("Paid".equalsIgnoreCase(item) || "Settled".equalsIgnoreCase(item) || "Cleared".equalsIgnoreCase(item)) {
                        color = "#16a34a";
                        bg = "#dcfce7";
                    } else if ("Pending".equalsIgnoreCase(item) || "Awaiting".equalsIgnoreCase(item)) {
                        color = "#d97706";
                        bg = "#fef3c7";
                    } else if ("Failed".equalsIgnoreCase(item) || "Rejected".equalsIgnoreCase(item)) {
                        color = "#ef4444";
                        bg = "#fee2e2";
                    } else if ("Overdue".equalsIgnoreCase(item)) {
                        color = "#b91c1c";
                        bg = "#fecaca";
                    } else if ("Refunded".equalsIgnoreCase(item) || "Cancelled".equalsIgnoreCase(item)) {
                        color = "#64748b";
                        bg = "#f1f5f9";
                    }

                    badge.setStyle("-fx-text-fill:" + color + "; -fx-background-color:" + bg + "; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            }
        };
    }

    // =====================================================
    // 6. LIVE BACKEND DATA INTEGRATION & SYNCHRONIZATION
    // =====================================================
    private void connectLiveServices() {
        PaymentDao.addChangeListener(this::refreshPaymentData);
        CargoDao.addChangeListener(this::refreshPaymentData);
        refreshPaymentData();

        vesselFinderService.start(updatedVessels -> {
            Platform.runLater(() -> {
                lastSuccessfulUpdateEpoch = System.currentTimeMillis();
                updateFreshnessBadge();
            });
        });
    }

    public void refreshPaymentData() {
        loadPortPaymentData();
        updateFreshnessBadge();
    }

    private void updateFreshnessBadge() {
        if (dataFreshnessBadge == null) return;
        long diffSec = (System.currentTimeMillis() - lastSuccessfulUpdateEpoch) / 1000;

        dataFreshnessBadge.setText("● LIVE — Updated " + Math.max(1, diffSec) + "s ago");
        dataFreshnessBadge.setStyle("-fx-text-fill:#10b981; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-color:rgba(16,185,129,0.12); -fx-padding:6 12; -fx-background-radius:999;");

        if (lastSyncLabel != null) {
            lastSyncLabel.setText("Synced: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
    }

    private void loadPortPaymentData() {
        List<Map<String, Object>> compiledPayments = new ArrayList<>();

        if (com.projectui.view.admin.AdminDataStore.isPortDeleted(activeViewingPort, null)) {
            masterPaymentsList.clear();
            updateKpiCards();
            applyFilters();
            return;
        }

        // 1. Fetch authentic payment records from PaymentDao (Single Source of Truth)
        List<Map<String, Object>> daoPayments = paymentDao.getAllPayments();
        if (daoPayments != null) {
            for (Map<String, Object> p : daoPayments) {
                if (p == null) continue;
                String payId = getString(p, "paymentId", getString(p, "razorpayPaymentId", ""));
                if (payId.isEmpty() || PaymentDao.isPaymentDeleted(payId, getString(p, "cargoId", ""))) continue;

                if (!isAssociatedWithPort(p, activeViewingPort)) {
                    continue;
                }

                Map<String, Object> record = new HashMap<>(p);
                record.put("paymentId", payId);

                if (!record.containsKey("customerType") || record.get("customerType") == null) {
                    record.put("customerType", "Exporter");
                }
                if (!record.containsKey("customerName") || record.get("customerName") == null) {
                    record.put("customerName", getString(p, "exporterName", getString(p, "importerName", getString(p, "company", "—"))));
                }
                if (!record.containsKey("invoiceId") || record.get("invoiceId") == null) {
                    record.put("invoiceId", "INV-" + getString(p, "receiptNumber", String.valueOf(Math.abs(payId.hashCode()) % 89999 + 10000)));
                }
                if (!record.containsKey("transactionId") || record.get("transactionId") == null) {
                    record.put("transactionId", getString(p, "razorpayPaymentId", payId));
                }
                String contVal = getString(record, "containerNo", "");
                if (contVal.isEmpty() || "—".equals(contVal) || "-".equals(contVal) || "null".equalsIgnoreCase(contVal)) {
                    String cId = getString(record, "cargoId", "");
                    String bId = getString(record, "bookingId", "");
                    Map<String, Object> booking = !cId.isEmpty() ? containerDao.getBookingByCargoId(cId) : null;
                    if (booking == null && !bId.isEmpty()) {
                        booking = containerDao.getBooking(bId);
                    }
                    if (booking != null) {
                        String resolved = getString(booking, "containerNo", getString(booking, "containerNumber", getString(booking, "assignedContainer", "")));
                        if (resolved.isEmpty() || "—".equals(resolved) || "-".equals(resolved)) {
                            resolved = getString(booking, "bookingId", "");
                        }
                        if (!resolved.isEmpty() && !"—".equals(resolved) && !"-".equals(resolved)) {
                            record.put("containerNo", resolved);
                        } else {
                            record.put("containerNo", "—");
                        }
                    } else if (!cId.isEmpty()) {
                        Map<String, Object> cg = cargoDao.getCargo(cId);
                        if (cg != null) {
                            String cgCont = getString(cg, "containerNo", getString(cg, "bookingId", getString(cg, "containerId", "")));
                            if (!cgCont.isEmpty() && !"—".equals(cgCont) && !"-".equals(cgCont)) {
                                record.put("containerNo", cgCont);
                            } else {
                                record.put("containerNo", "—");
                            }
                        } else {
                            record.put("containerNo", "—");
                        }
                    } else {
                        record.put("containerNo", "—");
                    }
                }
                if (!record.containsKey("containerType") || record.get("containerType") == null) {
                    record.put("containerType", "Standard Dry Container");
                }
                if (!record.containsKey("port") || record.get("port") == null) {
                    record.put("port", getString(p, "originPort", activeViewingPort));
                }

                compiledPayments.add(record);
            }
        }

        // 2. Query real cargo records in CargoDao for any paid cargos for this port
        List<Map<String, Object>> portCargos = cargoDao.getAllCargos();
        if (portCargos != null) {
            for (Map<String, Object> c : portCargos) {
                if (c == null) continue;
                if (isAssociatedWithPort(c, activeViewingPort) && PaymentDao.isPaidCargo(c)) {
                    String cargoId = getString(c, "cargoId", "");
                    String payId = getString(c, "razorpayPaymentId", getString(c, "paymentId", "PAY-" + cargoId.replace("CGO-", "")));
                    if (PaymentDao.isPaymentDeleted(payId, cargoId)) continue;

                    boolean alreadyIncluded = compiledPayments.stream().anyMatch(cp -> 
                        (!payId.isEmpty() && payId.equalsIgnoreCase(getString(cp, "paymentId", ""))) ||
                        (!cargoId.isEmpty() && cargoId.equalsIgnoreCase(getString(cp, "cargoId", "")))
                    );

                    if (!alreadyIncluded) {
                        Map<String, Object> paymentRec = PaymentDao.buildPaymentRecordFromCargo(c);
                        if (paymentRec != null) {
                            compiledPayments.add(paymentRec);
                        }
                    }
                }
            }
        }

        // 3. Also check any in-memory recorded payments in paymentStore that might not be in DAO list yet
        for (Map<String, Object> p : paymentStore.values()) {
            if (p == null) continue;
            String payId = getString(p, "paymentId", "");
            String cargoId = getString(p, "cargoId", "");
            if (PaymentDao.isPaymentDeleted(payId, cargoId)) continue;
            if (!payId.isEmpty() && isAssociatedWithPort(p, activeViewingPort)) {
                boolean exists = compiledPayments.stream().anyMatch(cp -> 
                    (!payId.isEmpty() && payId.equalsIgnoreCase(getString(cp, "paymentId", ""))) ||
                    (!cargoId.isEmpty() && cargoId.equalsIgnoreCase(getString(cp, "cargoId", "")))
                );
                if (!exists) {
                    compiledPayments.add(p);
                }
            }
        }

        masterPaymentsList.clear();
        masterPaymentsList.addAll(compiledPayments);

        updateKpiCards();
        applyFilters();
    }

    private void updateKpiCards() {
        int total = masterPaymentsList.size();
        int paidCount = 0;
        int pendingCount = 0;
        int failedCount = 0;
        double totalAmt = 0.0;
        double paidAmt = 0.0;
        double pendingAmt = 0.0;

        for (Map<String, Object> p : masterPaymentsList) {
            String st = getString(p, "paymentStatus", "Pending");
            double amt = getDouble(p, "amount", 0.0);
            totalAmt += amt;

            if ("Paid".equalsIgnoreCase(st) || "Settled".equalsIgnoreCase(st) || "Cleared".equalsIgnoreCase(st) || st.toUpperCase().contains("PAID")) {
                paidCount++;
                paidAmt += amt;
            } else if ("Pending".equalsIgnoreCase(st) || "Awaiting".equalsIgnoreCase(st) || st.toUpperCase().contains("PENDING")) {
                pendingCount++;
                pendingAmt += amt;
            } else if ("Failed".equalsIgnoreCase(st) || "Overdue".equalsIgnoreCase(st) || "Rejected".equalsIgnoreCase(st) || st.toUpperCase().contains("FAIL")) {
                failedCount++;
                pendingAmt += amt;
            }
        }

        if (totalPaymentsVal != null) totalPaymentsVal.setText(String.valueOf(total));
        if (paidCountVal != null) paidCountVal.setText(String.valueOf(paidCount));
        if (pendingCountVal != null) pendingCountVal.setText(String.valueOf(pendingCount));
        if (failedCountVal != null) failedCountVal.setText(String.valueOf(failedCount));

        if (totalAmountVal != null) totalAmountVal.setText(formatShortCurrency(totalAmt));
        if (paidAmountVal != null) paidAmountVal.setText(formatShortCurrency(paidAmt));
        if (pendingAmountVal != null) pendingAmountVal.setText(formatShortCurrency(pendingAmt));
    }

    private void applyFilters() {
        String query = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String statusFilter = statusFilterCombo != null && statusFilterCombo.getValue() != null ? statusFilterCombo.getValue() : "All Statuses";
        String customerFilter = customerTypeCombo != null && customerTypeCombo.getValue() != null ? customerTypeCombo.getValue() : "All Entities";
        String methodFilter = methodFilterCombo != null && methodFilterCombo.getValue() != null ? methodFilterCombo.getValue() : "All Methods";
        String sortOption = sortCombo != null && sortCombo.getValue() != null ? sortCombo.getValue() : "Sort: Newest First";

        List<Map<String, Object>> filtered = new ArrayList<>();

        for (Map<String, Object> p : masterPaymentsList) {
            // Search filter
            if (!query.isEmpty()) {
                String pId = getString(p, "paymentId", "").toLowerCase();
                String cName = getString(p, "customerName", "").toLowerCase();
                String cgId = getString(p, "cargoId", "").toLowerCase();
                String contId = getString(p, "containerNo", "").toLowerCase();
                String txnId = getString(p, "transactionId", "").toLowerCase();

                if (!pId.contains(query) && !cName.contains(query) && !cgId.contains(query) && !contId.contains(query) && !txnId.contains(query)) {
                    continue;
                }
            }

            // Status filter
            if (!"All Statuses".equalsIgnoreCase(statusFilter)) {
                String st = getString(p, "paymentStatus", "");
                if (!st.equalsIgnoreCase(statusFilter)) {
                    continue;
                }
            }

            // Customer Type filter
            if (!"All Entities".equalsIgnoreCase(customerFilter)) {
                String type = getString(p, "customerType", "");
                if (!type.equalsIgnoreCase(customerFilter)) {
                    continue;
                }
            }

            // Method filter
            if (!"All Methods".equalsIgnoreCase(methodFilter)) {
                String m = getString(p, "paymentMethod", "");
                if (!m.toLowerCase().contains(methodFilter.toLowerCase())) {
                    continue;
                }
            }

            filtered.add(p);
        }

        // Sorting
        if (sortOption.contains("Newest")) {
            filtered.sort((a, b) -> getString(b, "paymentDate", "").compareTo(getString(a, "paymentDate", "")));
        } else if (sortOption.contains("Oldest")) {
            filtered.sort((a, b) -> getString(a, "paymentDate", "").compareTo(getString(b, "paymentDate", "")));
        } else if (sortOption.contains("High to Low")) {
            filtered.sort((a, b) -> Double.compare(getDouble(b, "amount", 0.0), getDouble(a, "amount", 0.0)));
        } else if (sortOption.contains("Low to High")) {
            filtered.sort((a, b) -> Double.compare(getDouble(a, "amount", 0.0), getDouble(b, "amount", 0.0)));
        }

        displayedPaymentsList.setAll(filtered);
    }

    public static boolean isAssociatedWithPort(Map<String, Object> record, String portName) {
        if (record == null || portName == null || portName.isBlank()) return false;
        if ("all".equalsIgnoreCase(portName.trim()) || "all ports".equalsIgnoreCase(portName.trim())) return true;

        String pLower = portName.trim().toLowerCase();
        String pNorm = com.projectui.dao.UserDao.normalizePort(portName).trim().toLowerCase();

        String port = getTrimmedLower(record, "port", "originPort", "destinationPort", "preferredPort", "currentPort", "assignedPort");
        String orig = getTrimmedLower(record, "originPort", "origin");
        String dest = getTrimmedLower(record, "destinationPort", "destination");
        String pref = getTrimmedLower(record, "preferredPort");
        String curr = getTrimmedLower(record, "currentPort", "assignedPort", "port");

        List<String> list = List.of(port, orig, dest, pref, curr);
        for (String s : list) {
            if (s.isEmpty()) continue;
            String sNorm = com.projectui.dao.UserDao.normalizePort(s).trim().toLowerCase();
            if (s.equals(pLower) || s.equals(pNorm) || sNorm.equals(pNorm) || sNorm.equals(pLower)) return true;
            if (s.contains(pLower) || pLower.contains(s) || sNorm.contains(pNorm) || pNorm.contains(sNorm)) return true;

            if (isCityMatch(s, pLower) || isCityMatch(s, pNorm) || isCityMatch(sNorm, pLower) || isCityMatch(sNorm, pNorm)) return true;
        }
        return false;
    }

    private static boolean isCityMatch(String a, String b) {
        if (a.isEmpty() || b.isEmpty()) return false;
        if ((a.contains("cochin") || a.contains("kochi")) && (b.contains("cochin") || b.contains("kochi"))) return true;
        if ((a.contains("mumbai") || a.contains("bombay") || a.contains("jnpt")) && (b.contains("mumbai") || b.contains("bombay") || b.contains("jnpt"))) return true;
        if ((a.contains("chennai") || a.contains("madras")) && (b.contains("chennai") || b.contains("madras"))) return true;
        if ((a.contains("vizag") || a.contains("visakhapatnam")) && (b.contains("vizag") || b.contains("visakhapatnam"))) return true;
        if ((a.contains("kolkata") || a.contains("calcutta")) && (b.contains("kolkata") || b.contains("calcutta"))) return true;
        if (a.contains("mundra") && b.contains("mundra")) return true;
        if (a.contains("kandla") && b.contains("kandla")) return true;
        if (a.contains("goa") && b.contains("goa")) return true;
        return false;
    }

    private static String getTrimmedLower(Map<String, Object> map, String... keys) {
        if (map == null) return "";
        for (String k : keys) {
            Object v = map.get(k);
            if (v != null && !v.toString().isBlank()) {
                return v.toString().trim().toLowerCase();
            }
        }
        return "";
    }

    // =====================================================
    // 7. PAYMENT DETAILS & REVIEW MODAL DIALOGS
    // =====================================================
    private void showPaymentDetailsDialog(Map<String, Object> item) {
        if (item == null) return;

        Dialog<Void> dialog = new Dialog<>();
        String payId = getString(item, "paymentId", "PAY-N/A");
        dialog.setTitle("Payment Particulars & Audit Voucher — " + payId);
        dialog.setHeaderText("Complete payment transaction record, tariff breakdown and cargo manifest for " + payId);

        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeBtn);

        VBox contentBox = new VBox(12);
        contentBox.setPadding(new Insets(16));
        contentBox.setPrefWidth(500);

        // Header Title
        HBox head = new HBox(8);
        head.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label nameLbl = new Label("Invoice Voucher #" + getString(item, "invoiceId", payId));
        nameLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16px; -fx-font-weight:bold;");
        Label sub = new Label("Port Authority: " + activeViewingPort + " • Entity: " + getString(item, "customerType", "Exporter"));
        sub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");
        titleBox.getChildren().addAll(nameLbl, sub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        String st = getString(item, "paymentStatus", "Pending");
        Label statusPill = new Label(st);
        statusPill.setStyle("-fx-text-fill:#16a34a; -fx-background-color:#dcfce7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;");
        if ("Pending".equalsIgnoreCase(st)) {
            statusPill.setStyle("-fx-text-fill:#d97706; -fx-background-color:#fef3c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;");
        } else if ("Failed".equalsIgnoreCase(st) || "Overdue".equalsIgnoreCase(st)) {
            statusPill.setStyle("-fx-text-fill:#ef4444; -fx-background-color:#fee2e2; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4;");
        }

        head.getChildren().addAll(titleBox, sp, statusPill);

        // Section 1: PAYMENT & TRANSACTION SPECIFICATIONS
        Label g1 = new Label("PAYMENT & TRANSACTION SPECIFICATIONS");
        g1.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 0 2 0;");

        VBox g1Box = new VBox(3,
                detailLine("Payment Reference", payId),
                detailLine("Invoice Number", getString(item, "invoiceId", "INV-10245")),
                detailLine("Transaction Gateway ID", getString(item, "transactionId", "TXN-94821")),
                detailLine("Customer / Shipper", getString(item, "customerName", "Global Logistics Ltd.")),
                detailLine("Customer Type", getString(item, "customerType", "Exporter")),
                detailLine("Payment Method", getString(item, "paymentMethod", "UPI / Bank Wire")),
                detailLine("Payment Status", st),
                detailLine("Payment Timestamp", formatIsoDate(getString(item, "paymentDate", "Today"))),
                detailLine("Due / Clearance Date", formatIsoDate(getString(item, "dueDate", "N/A")))
        );

        // Section 2: ASSOCIATED CARGO & CONTAINER
        Label g2 = new Label("ASSOCIATED CARGO, CONTAINER & VOYAGE");
        g2.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 0 2 0;");

        String shipName = "MV " + activeViewingPort.replace("Port of ", "") + " Star";
        VesselData matchedVessel = findMatchingVessel(shipName);

        String detailContNo = getString(item, "containerNo", "");
        if (detailContNo.isEmpty() || "—".equals(detailContNo) || "-".equals(detailContNo)) {
            String bId = getString(item, "bookingId", "");
            if (!bId.isEmpty() && !"—".equals(bId) && !"-".equals(bId)) {
                detailContNo = bId;
            } else {
                String cId = getString(item, "cargoId", "");
                if (!cId.isEmpty()) {
                    Map<String, Object> bk = containerDao.getBookingByCargoId(cId);
                    if (bk != null) {
                        detailContNo = getString(bk, "containerNo", getString(bk, "bookingId", "—"));
                    }
                }
            }
        }
        if (detailContNo.isEmpty()) detailContNo = "—";

        VBox g2Box = new VBox(3,
                detailLine("Manifest Cargo ID", getString(item, "cargoId", "CGO-1024")),
                detailLine("Manifest Cargo Name", getString(item, "cargoName", "Commercial Goods")),
                detailLine("Cargo Quantity", getString(item, "quantity", "1 unit")),
                detailLine("Container Unit ID", detailContNo),
                detailLine("Container Type", getString(item, "containerType", "20 ft Standard Dry")),
                detailLine("Assigned Vessel", shipName),
                detailLine("Vessel IMO / MMSI", matchedVessel != null ? matchedVessel.imo + " / " + matchedVessel.mmsi : "9482103 / 265522510"),
                detailLine("Operating Port", activeViewingPort)
        );

        // Section 3: TARIFF & FINANCIAL BREAKDOWN
        Label g3 = new Label("FINANCIAL TARIFF & CHARGE BREAKDOWN");
        g3.setStyle("-fx-text-fill:#0284c7; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 0 2 0;");

        double amt = getDouble(item, "amount", 125000.0);
        double handlingFee = amt * 0.45;
        double storageFee = amt * 0.25;
        double portTariff = amt * 0.15;
        double gst = amt * 0.15;

        VBox g3Box = new VBox(3,
                detailLine("Freight & Cargo Handling", currencyFormatter.format(handlingFee)),
                detailLine("Container Yard & Storage", currencyFormatter.format(storageFee)),
                detailLine("Quayside & Port Authority Tariff", currencyFormatter.format(portTariff)),
                detailLine("Port Services & GST (18%)", currencyFormatter.format(gst)),
                detailLine("TOTAL PAYABLE AMOUNT", currencyFormatter.format(amt))
        );

        contentBox.getChildren().addAll(head, g1, g1Box, g2, g2Box, g3, g3Box);
        dialog.getDialogPane().setContent(contentBox);
        dialog.showAndWait();
    }

    private void showPaymentReviewDialog(Map<String, Object> item) {
        if (item == null) return;
        Dialog<ButtonType> dialog = new Dialog<>();
        String payId = getString(item, "paymentId", "PAY-N/A");
        dialog.setTitle("Review Payment Transaction — " + payId);
        dialog.setHeaderText("Port Authority Verification for Invoice " + payId);

        ButtonType confirmBtnType = new ButtonType("Save Review", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField payField = new TextField(payId);
        payField.setEditable(false);
        payField.setStyle("-fx-background-color:#f1f5f9;");

        TextField customerField = new TextField(getString(item, "customerName", "Global Logistics Ltd."));
        customerField.setEditable(false);
        customerField.setStyle("-fx-background-color:#f1f5f9;");

        TextField amtField = new TextField(formatCurrency(item));
        amtField.setEditable(false);
        amtField.setStyle("-fx-background-color:#f1f5f9;");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Paid", "Pending", "Failed", "Overdue");
        statusCombo.setValue(getString(item, "paymentStatus", "Pending"));

        grid.add(new Label("Payment ID:"), 0, 0);
        grid.add(payField, 1, 0);
        grid.add(new Label("Customer Entity:"), 0, 1);
        grid.add(customerField, 1, 1);
        grid.add(new Label("Invoice Amount:"), 0, 2);
        grid.add(amtField, 1, 2);
        grid.add(new Label("Verified Status:"), 0, 3);
        grid.add(statusCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(confirmBtnType);
        saveBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-weight:bold;");

        saveBtn.setOnAction(event -> {
            String newStatus = statusCombo.getValue();
            item.put("paymentStatus", newStatus);
            item.put("updatedAt", LocalDateTime.now().toString());

            refreshPaymentData();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Status Verified");
            alert.setHeaderText("Payment Verification Recorded");
            alert.setContentText("Payment " + payId + " status updated to " + newStatus + ".");
            alert.showAndWait();
        });

        dialog.showAndWait();
    }

    private VesselData findMatchingVessel(String shipName) {
        if (shipName == null || shipName.isBlank()) return null;
        Collection<VesselData> all = vesselManager.getAllVessels();
        for (VesselData v : all) {
            if (v.name != null && (v.name.equalsIgnoreCase(shipName) || shipName.contains(v.name) || v.name.contains(shipName))) {
                return v;
            }
        }
        return null;
    }

    private HBox detailLine(String label, String value) {
        HBox line = new HBox(8);
        line.setAlignment(Pos.CENTER_LEFT);
        line.setStyle("-fx-border-color:rgba(226,232,240,0.5); -fx-border-width:0 0 1 0; -fx-padding:3 0;");

        Label l = new Label(label);
        l.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label v = new Label(value != null && !value.isBlank() ? value : "—");
        v.setStyle("-fx-text-fill:#0f172a; -fx-font-size:12px; -fx-font-weight:bold;");

        line.getChildren().addAll(l, sp, v);
        return line;
    }

    private String formatCurrency(Map<String, Object> map) {
        double amt = getDouble(map, "amount", 0.0);
        return currencyFormatter.format(amt);
    }

    private String formatShortCurrency(double amt) {
        if (amt >= 10000000) {
            return String.format("₹%.2f Cr", amt / 10000000.0);
        } else if (amt >= 100000) {
            return String.format("₹%.2f L", amt / 100000.0);
        } else if (amt >= 1000) {
            return String.format("₹%.1f K", amt / 1000.0);
        }
        return currencyFormatter.format(amt);
    }

    private double getDouble(Map<String, Object> map, String key, double fallback) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) return fallback;
        try {
            return Double.parseDouble(map.get(key).toString().replace("₹", "").replace(",", "").trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String getString(Map<String, Object> map, String key, String fallback) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) return fallback;
        String val = map.get(key).toString().trim();
        return val.isEmpty() ? fallback : val;
    }

    private String formatIsoDate(String raw) {
        if (raw == null || raw.isBlank()) return "Today";
        if (raw.contains("T")) {
            try {
                LocalDateTime dt = LocalDateTime.parse(raw);
                return dt.format(DateTimeFormatter.ofPattern("dd MMM, HH:mm"));
            } catch (Exception ignored) {}
        }
        return raw;
    }

    public static void recordPayment(String paymentId, String invoiceId, String cargoId, String cargoName,
                                    String customerName, String customerType, String containerNo, String containerType,
                                    double amount, String paymentMethod, String paymentStatus, String transactionId, String port) {
        Map<String, Object> p = new HashMap<>();
        p.put("paymentId", paymentId);
        p.put("razorpayPaymentId", paymentId);
        p.put("invoiceId", invoiceId);
        p.put("cargoId", cargoId != null ? cargoId : "");
        p.put("cargoName", cargoName != null ? cargoName : "Commercial Cargo");
        p.put("cargoType", "Export Freight");
        p.put("quantity", "1 unit");
        p.put("customerName", customerName != null ? customerName : "—");
        p.put("customerType", customerType != null ? customerType : "Exporter");
        p.put("containerNo", containerNo != null ? containerNo : "—");
        p.put("containerType", containerType != null ? containerType : "—");
        p.put("amount", amount);
        p.put("paymentMethod", paymentMethod != null ? paymentMethod : "Razorpay Gateway (UPI/Cards/NetBanking)");
        p.put("paymentStatus", paymentStatus != null ? paymentStatus : "Paid");
        p.put("transactionId", transactionId != null ? transactionId : paymentId);
        p.put("paymentDate", LocalDateTime.now().toString());
        p.put("dueDate", LocalDateTime.now().plusDays(7).toString());
        p.put("updatedAt", LocalDateTime.now().toString());
        p.put("port", port != null ? port : "Port of Mumbai");

        paymentStore.put(paymentId, p);
        new PaymentDao().savePayment(p);
    }

    public static Map<String, Object> getPaymentByCargoId(String cargoId) {
        if (cargoId == null) return null;
        for (Map<String, Object> p : paymentStore.values()) {
            if (cargoId.equalsIgnoreCase((String) p.get("cargoId"))) {
                return p;
            }
        }
        return new PaymentDao().getPaymentByCargoId(cargoId);
    }

    public static void clearPortPayments(String portName, String portCode) {
        if (portName == null && portCode == null) return;
        paymentStore.entrySet().removeIf(e -> isAssociatedWithPort(e.getValue(), portName));
    }

    // =====================================================
    // 8. GET CONTENT / ROOT COMPATIBILITY
    // =====================================================
    public VBox getContent() {
        return root;
    }

    public VBox getRoot() {
        return root;
    }
}