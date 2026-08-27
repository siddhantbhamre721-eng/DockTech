package com.projectui.view.portauthority;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import com.projectui.controller.portauthority.PaymentController;
import com.projectui.controller.portauthority.CargoController;
import com.projectui.controller.portauthority.ShipController;
import com.projectui.controller.portauthority.NotificationController;

import com.projectui.model.portauthority.Payment;
import com.projectui.model.portauthority.Cargo;
import com.projectui.model.portauthority.Ship;


// =====================================================
// PAYMENT MANAGEMENT PAGE
// =====================================================

public class PaymentPage {

    // =====================================================
    // MAIN PAGE CONTAINER
    // =====================================================

    private VBox root;


    // =====================================================
    // PAYMENT CONTROLLER
    // =====================================================

    private PaymentController paymentController =
            new PaymentController();


    // =====================================================
    // NOTIFICATION CONTROLLER
    // =====================================================

    private NotificationController notificationController =
            new NotificationController();


    // =====================================================
    // CARGO CONTROLLER
    // =====================================================

    private CargoController cargoController =
            new CargoController();


    // =====================================================
    // SHIP CONTROLLER
    // =====================================================

    private ShipController shipController =
            new ShipController();


    // =====================================================
    // LOCAL PAYMENT DATA
    // =====================================================

    private ObservableList<Payment> paymentData =
            FXCollections.observableArrayList();


    // =====================================================
    // LOCAL CARGO DATA
    // =====================================================

    private ObservableList<Cargo> cargoData =
            FXCollections.observableArrayList();


    // =====================================================
    // LOCAL SHIP DATA
    // =====================================================

    private ObservableList<Ship> shipData =
            FXCollections.observableArrayList();


    // =====================================================
    // SUMMARY CARD VALUES
    // =====================================================

    private Label totalRevenueValue;

    private Label paidValue;

    private Label pendingValue;

    private Label overdueValue;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public PaymentPage() {

        root = new VBox();

        root.setSpacing(20);

        root.setPadding(
                new Insets(28)
        );

        root.setStyle(
                "-fx-background-color: #f3f8ff;"
        );


        // =================================================
        // PAGE TITLE
        // =================================================

        Label title =
                new Label(
                        "Payment Management"
                );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        28
                )
        );

        title.setTextFill(
                Color.web("#12203a")
        );


        // =================================================
        // PAGE DESCRIPTION
        // =================================================

        Label subtitle =
                new Label(
                        "Monitor payment transactions related to port and cargo operations."
                );

        subtitle.setFont(
                Font.font(
                        "Arial",
                        14
                )
        );

        subtitle.setTextFill(
                Color.web("#7b879c")
        );


        // =================================================
        // TITLE + DESCRIPTION
        // =================================================

        VBox titleBox =
                new VBox(
                        5,
                        title,
                        subtitle
                );


        // =================================================
        // LOAD PAYMENTS FROM FIREBASE
        // =================================================

        List<Payment> payments =
                paymentController.getPayments();


        paymentData.addAll(
                payments
        );


        // =================================================
        // LOAD CARGO FROM FIREBASE
        // =================================================

        List<Cargo> cargos =
                cargoController.getCargos();


        cargoData.addAll(
                cargos
        );


        // =================================================
        // LOAD SHIPS FROM FIREBASE
        // =================================================

        List<Ship> ships =
                shipController.getShips();


        shipData.addAll(
                ships
        );


        // =================================================
        // SUMMARY CARDS
        // =================================================

        HBox summaryCards =
                createSummaryCards();


        // =================================================
        // PAYMENT TRANSACTIONS TABLE
        // =================================================

        VBox paymentTable =
                createPaymentTable();


        // =================================================
        // ADD COMPONENTS TO PAGE
        // =================================================

        root.getChildren().addAll(
                titleBox,
                summaryCards,
                paymentTable
        );


        // =================================================
        // INITIAL STATISTICS
        // =================================================

        refreshStatistics();
    }


    // =====================================================
    // GET PAGE CONTENT
    // =====================================================

    public VBox getContent() {

        return root;
    }


    // =====================================================
    // CREATE SUMMARY CARDS
    // =====================================================

    private HBox createSummaryCards() {

        HBox cards =
                new HBox();

        cards.setSpacing(18);


        // =================================================
        // TOTAL REVENUE
        // =================================================

        totalRevenueValue =
                new Label("₹0");


        VBox totalRevenue =
                createSummaryCard(
                        "Total Revenue",
                        totalRevenueValue,
                        "All payment transactions",
                        "#00a878"
                );


        // =================================================
        // PAID
        // =================================================

        paidValue =
                new Label("₹0");


        VBox paid =
                createSummaryCard(
                        "Paid",
                        paidValue,
                        "Completed payments",
                        "#7b879c"
                );


        // =================================================
        // PENDING
        // =================================================

        pendingValue =
                new Label("₹0");


        VBox pending =
                createSummaryCard(
                        "Pending",
                        pendingValue,
                        "Payments awaiting completion",
                        "#ff8a00"
                );


        // =================================================
        // OVERDUE
        // =================================================

        overdueValue =
                new Label("₹0");


        VBox overdue =
                createSummaryCard(
                        "Overdue",
                        overdueValue,
                        "Overdue payments",
                        "#e53935"
                );


        // =================================================
        // ADD CARDS
        // =================================================

        cards.getChildren().addAll(
                totalRevenue,
                paid,
                pending,
                overdue
        );


        return cards;
    }


    // =====================================================
    // CREATE ONE SUMMARY CARD
    // =====================================================

    private VBox createSummaryCard(
            String heading,
            Label valueLabel,
            String description,
            String descriptionColor
    ) {

        Label headingLabel =
                new Label(
                        heading.toUpperCase()
                );

        headingLabel.setFont(
                Font.font(
                        "Arial",
                        13
                )
        );

        headingLabel.setTextFill(
                Color.web("#7b879c")
        );


        valueLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        28
                )
        );

        valueLabel.setTextFill(
                Color.web("#12203a")
        );


        Label descriptionLabel =
                new Label(
                        description
                );

        descriptionLabel.setFont(
                Font.font(
                        "Arial",
                        13
                )
        );

        descriptionLabel.setTextFill(
                Color.web(descriptionColor)
        );


        VBox card =
                new VBox(
                        10,
                        headingLabel,
                        valueLabel,
                        descriptionLabel
                );

        card.setPadding(
                new Insets(20)
        );

        card.setPrefHeight(
                120
        );

        card.setPrefWidth(
                220
        );

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #dce5f0;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );


        return card;
    }


    // =====================================================
    // REFRESH PAYMENT STATISTICS
    // =====================================================

    private void refreshStatistics() {

        double totalRevenue = 0;

        double paid = 0;

        double pending = 0;

        double overdue = 0;


        // =================================================
        // CALCULATE VALUES
        // =================================================

        for (Payment payment : paymentData) {

            if (payment == null) {
                continue;
            }


            double amount =
                    payment.getAmount();


            totalRevenue += amount;


            String status =
                    payment.getStatus();


            if (status == null) {
                continue;
            }


            switch (status) {

                case "Paid":

                    paid += amount;

                    break;


                case "Pending":

                    pending += amount;

                    break;


                case "Overdue":

                    overdue += amount;

                    break;
            }
        }


        // =================================================
        // UPDATE CARDS
        // =================================================

        totalRevenueValue.setText(
                formatCurrency(
                        totalRevenue
                )
        );


        paidValue.setText(
                formatCurrency(
                        paid
                )
        );


        pendingValue.setText(
                formatCurrency(
                        pending
                )
        );


        overdueValue.setText(
                formatCurrency(
                        overdue
                )
        );
    }


    // =====================================================
    // FORMAT CURRENCY
    // =====================================================

    private String formatCurrency(
            double amount
    ) {

        NumberFormat formatter =
                NumberFormat.getCurrencyInstance(
                        new Locale(
                                "en",
                                "IN"
                        )
                );


        return formatter.format(
                amount
        );
    }


    // =====================================================
    // PAYMENT TRANSACTIONS TABLE
    // =====================================================

    private VBox createPaymentTable() {

        VBox tableBox =
                new VBox(
                        10
                );


        // -------------------------------------------------
        // SECTION TITLE
        // -------------------------------------------------

        Label tableTitle =
                new Label(
                        "Payment Transactions"
                );


        tableTitle.setStyle(
                "-fx-font-size:18px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#12203a;"
        );


        Label tableSubtitle =
                new Label(
                        "All port and cargo payment records"
                );


        tableSubtitle.setStyle(
                "-fx-font-size:13px;" +
                "-fx-text-fill:#7b879c;"
        );


        VBox titleBox =
                new VBox(
                        3
                );


        titleBox.getChildren().addAll(
                tableTitle,
                tableSubtitle
        );


        // -------------------------------------------------
        // SEARCH PAYMENT
        // -------------------------------------------------

        TextField searchField =
                new TextField();


        searchField.setPromptText(
                "Search Payment..."
        );


        searchField.setPrefWidth(
                260
        );


        // -------------------------------------------------
        // PAYMENT METHOD FILTER
        // -------------------------------------------------

        ComboBox<String> methodFilter =
                new ComboBox<>();


        methodFilter.getItems().addAll(
                "All Methods",
                "Bank Transfer",
                "UPI",
                "Card"
        );


        methodFilter.setValue(
                "All Methods"
        );


        methodFilter.setPrefWidth(
                150
        );


        // -------------------------------------------------
        // PAYMENT STATUS FILTER
        // -------------------------------------------------

        ComboBox<String> statusFilter =
                new ComboBox<>();


        statusFilter.getItems().addAll(
                "All Statuses",
                "Paid",
                "Pending",
                "Failed",
                "Overdue"
        );


        statusFilter.setValue(
                "All Statuses"
        );


        statusFilter.setPrefWidth(
                150
        );


        // -------------------------------------------------
        // ADD PAYMENT BUTTON
        // -------------------------------------------------

        Button addPaymentButton =
                new Button(
                        "+ Add Payment"
                );


        // -------------------------------------------------
        // FILTERS
        // -------------------------------------------------

        HBox filters =
                new HBox(
                        10
                );


        filters.getChildren().addAll(
                searchField,
                methodFilter,
                statusFilter,
                addPaymentButton
        );


        // -------------------------------------------------
        // TABLE
        // -------------------------------------------------

        TableView<Payment> table =
                new TableView<>();


        // =================================================
        // MODERN TABLE DIMENSIONS
        // =================================================

        table.setFixedCellSize(
                38
        );


        table.setPrefHeight(
                400
        );


        // =================================================
        // MODERN TABLE BASE STYLE
        // =================================================

        table.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-border-color:transparent;" +
                "-fx-table-cell-border-color:transparent;" +
                "-fx-font-size:10;"
        );


        // -------------------------------------------------
        // TABLE COLUMNS
        // -------------------------------------------------

        TableColumn<Payment, String> paymentId =
                createColumn(
                        "Payment ID",
                        100,
                        Payment::getPaymentId
                );


        TableColumn<Payment, String> cargoId =
                createColumn(
                        "Cargo ID",
                        90,
                        Payment::getCargoId
                );


        TableColumn<Payment, String> ship =
                createColumn(
                        "Ship",
                        120,
                        Payment::getShip
                );


        TableColumn<Payment, String> customer =
                createColumn(
                        "Customer",
                        150,
                        Payment::getCustomer
                );


        TableColumn<Payment, String> amount =
                new TableColumn<>(
                        "Amount"
                );


        amount.setPrefWidth(
                110
        );


        amount.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                formatCurrency(
                                        data.getValue()
                                                .getAmount()
                                )
                        )
        );


        TableColumn<Payment, String> date =
                createColumn(
                        "Date",
                        100,
                        Payment::getDate
                );


        TableColumn<Payment, String> method =
                createColumn(
                        "Method",
                        120,
                        Payment::getMethod
                );


        TableColumn<Payment, String> status =
                createColumn(
                        "Status",
                        100,
                        Payment::getStatus
                );


        // =================================================
        // NORMAL COLUMN STYLING
        // =================================================

        stylePaymentColumn(
                amount
        );


        stylePaymentColumn(
                date
        );


        stylePaymentColumn(
                method
        );


        stylePaymentColumn(
                customer
        );


        // =================================================
        // BOLD IMPORTANT COLUMNS
        // =================================================

        stylePaymentBoldColumn(
                paymentId
        );


        stylePaymentBoldColumn(
                cargoId
        );


        stylePaymentBoldColumn(
                ship
        );


        // -------------------------------------------------
        // ACTION COLUMN
        // -------------------------------------------------

        TableColumn<Payment, String> action =
                new TableColumn<>(
                        "Action"
                );


        action.setPrefWidth(
                220
        );


        action.setCellFactory(
                column ->
                        new TableCell<Payment, String>() {

                            private final Button viewButton =
                                    new Button(
                                            "View"
                                    );


                            private final Button editButton =
                                    new Button(
                                            "Edit"
                                    );


                            private final Button deleteButton =
                                    new Button(
                                            "Delete"
                                    );


                            {

                                // =============================================
                                // VIEW BUTTON STYLE
                                // =============================================

                                viewButton.setStyle(
                                        "-fx-background-color:#f1f5f9;" +
                                        "-fx-text-fill:#334155;" +
                                        "-fx-font-size:9;" +
                                        "-fx-font-weight:bold;" +
                                        "-fx-background-radius:5;" +
                                        "-fx-border-color:#cbd5e1;" +
                                        "-fx-border-radius:5;" +
                                        "-fx-padding:4 8 4 8;" +
                                        "-fx-cursor:hand;"
                                );


                                // =============================================
                                // EDIT BUTTON STYLE
                                // =============================================

                                editButton.setStyle(
                                        "-fx-background-color:#eff6ff;" +
                                        "-fx-text-fill:#2563eb;" +
                                        "-fx-font-size:9;" +
                                        "-fx-font-weight:bold;" +
                                        "-fx-background-radius:5;" +
                                        "-fx-border-color:#bfdbfe;" +
                                        "-fx-border-radius:5;" +
                                        "-fx-padding:4 8 4 8;" +
                                        "-fx-cursor:hand;"
                                );


                                // =============================================
                                // DELETE BUTTON STYLE
                                // =============================================

                                deleteButton.setStyle(
                                        "-fx-background-color:#fff1f2;" +
                                        "-fx-text-fill:#dc2626;" +
                                        "-fx-font-size:9;" +
                                        "-fx-font-weight:bold;" +
                                        "-fx-background-radius:5;" +
                                        "-fx-border-color:#fecdd3;" +
                                        "-fx-border-radius:5;" +
                                        "-fx-padding:4 8 4 8;" +
                                        "-fx-cursor:hand;"
                                );


                                // =============================================
                                // VIEW PAYMENT
                                // =============================================

                                viewButton.setOnAction(
                                        event -> {

                                            Payment payment =
                                                    getTableView()
                                                            .getItems()
                                                            .get(
                                                                    getIndex()
                                                            );


                                            showPaymentDetails(
                                                    payment
                                            );
                                        }
                                );


                                // =============================================
                                // EDIT PAYMENT
                                // =============================================

                                editButton.setOnAction(
                                        event -> {

                                            Payment payment =
                                                    getTableView()
                                                            .getItems()
                                                            .get(
                                                                    getIndex()
                                                            );


                                            showPaymentDialog(
                                                    payment,
                                                    table
                                            );
                                        }
                                );


                                // =============================================
                                // DELETE PAYMENT
                                // =============================================

                                deleteButton.setOnAction(
                                        event -> {

                                            Payment payment =
                                                    getTableView()
                                                            .getItems()
                                                            .get(
                                                                    getIndex()
                                                            );


                                            showDeleteConfirmation(
                                                    payment,
                                                    table
                                            );
                                        }
                                );
                            }


                            @Override
                            protected void updateItem(
                                    String item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );


                                if (empty) {

                                    setGraphic(
                                            null
                                    );

                                    setText(
                                            null
                                    );

                                } else {

                                    HBox buttons =
                                            new HBox(
                                                    5,
                                                    viewButton,
                                                    editButton,
                                                    deleteButton
                                            );


                                    buttons.setAlignment(
                                            Pos.CENTER_LEFT
                                    );


                                    setGraphic(
                                            buttons
                                    );


                                    setText(
                                            null
                                    );


                                    setStyle(
                                            "-fx-background-color:white;" +
                                            "-fx-border-color:transparent transparent #e8edf2 transparent;" +
                                            "-fx-padding:0 4 0 4;"
                                    );
                                }
                            }
                        }
        );


        // =================================================
        // STATUS BADGES
        // =================================================

        status.setCellFactory(
                column ->
                        new TableCell<Payment, String>() {

                            private final Label statusLabel =
                                    new Label();


                            @Override
                            protected void updateItem(
                                    String item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );


                                if (empty || item == null) {

                                    setGraphic(
                                            null
                                    );

                                    setText(
                                            null
                                    );

                                    return;
                                }


                                statusLabel.setText(
                                        item
                                );


                                // =============================================
                                // PAID
                                // =============================================

                                if (item.equalsIgnoreCase(
                                        "Paid"
                                )) {

                                    statusLabel.setStyle(
                                            "-fx-background-color:#dcfce7;" +
                                            "-fx-text-fill:#15803d;" +
                                            "-fx-background-radius:12;" +
                                            "-fx-font-size:9;" +
                                            "-fx-font-weight:bold;" +
                                            "-fx-padding:4 9 4 9;"
                                    );


                                // =============================================
                                // PENDING
                                // =============================================

                                } else if (item.equalsIgnoreCase(
                                        "Pending"
                                )) {

                                    statusLabel.setStyle(
                                            "-fx-background-color:#ffedd5;" +
                                            "-fx-text-fill:#ea580c;" +
                                            "-fx-background-radius:12;" +
                                            "-fx-font-size:9;" +
                                            "-fx-font-weight:bold;" +
                                            "-fx-padding:4 9 4 9;"
                                    );


                                // =============================================
                                // FAILED
                                // =============================================

                                } else if (item.equalsIgnoreCase(
                                        "Failed"
                                )) {

                                    statusLabel.setStyle(
                                            "-fx-background-color:#fee2e2;" +
                                            "-fx-text-fill:#dc2626;" +
                                            "-fx-background-radius:12;" +
                                            "-fx-font-size:9;" +
                                            "-fx-font-weight:bold;" +
                                            "-fx-padding:4 9 4 9;"
                                    );


                                // =============================================
                                // OVERDUE
                                // =============================================

                                } else if (item.equalsIgnoreCase(
                                        "Overdue"
                                )) {

                                    statusLabel.setStyle(
                                            "-fx-background-color:#fef3c7;" +
                                            "-fx-text-fill:#b45309;" +
                                            "-fx-background-radius:12;" +
                                            "-fx-font-size:9;" +
                                            "-fx-font-weight:bold;" +
                                            "-fx-padding:4 9 4 9;"
                                    );


                                // =============================================
                                // DEFAULT
                                // =============================================

                                } else {

                                    statusLabel.setStyle(
                                            "-fx-background-color:#f1f5f9;" +
                                            "-fx-text-fill:#475569;" +
                                            "-fx-background-radius:12;" +
                                            "-fx-font-size:9;" +
                                            "-fx-font-weight:bold;" +
                                            "-fx-padding:4 9 4 9;"
                                    );
                                }


                                setGraphic(
                                        statusLabel
                                );


                                setText(
                                        null
                                );


                                setAlignment(
                                        Pos.CENTER
                                );


                                setStyle(
                                        "-fx-background-color:white;" +
                                        "-fx-border-color:transparent transparent #e8edf2 transparent;"
                                );
                            }
                        }
        );


        // =================================================
        // ADD COLUMNS
        // =================================================

        table.getColumns().addAll(
                paymentId,
                cargoId,
                ship,
                customer,
                amount,
                date,
                method,
                status,
                action
        );


        // =================================================
        // MODERN TABLE HEADER
        // =================================================

        table.skinProperty().addListener(
                (observable, oldSkin, newSkin) -> {

                    if (newSkin != null) {

                        javafx.application.Platform.runLater(
                                () ->
                                        stylePaymentTableHeader(
                                                table
                                        )
                        );
                    }
                }
        );


        // -------------------------------------------------
        // FILTERED DATA
        // -------------------------------------------------

        FilteredList<Payment> filteredData =
                new FilteredList<>(
                        paymentData,
                        payment -> true
                );


        // -------------------------------------------------
        // METHOD FILTER
        // -------------------------------------------------

        methodFilter.setOnAction(
                event -> {

                    applyPaymentFilters(
                            filteredData,
                            searchField,
                            methodFilter,
                            statusFilter
                    );
                }
        );


        // -------------------------------------------------
        // STATUS FILTER
        // -------------------------------------------------

        statusFilter.setOnAction(
                event -> {

                    applyPaymentFilters(
                            filteredData,
                            searchField,
                            methodFilter,
                            statusFilter
                    );
                }
        );


        // -------------------------------------------------
        // SEARCH
        // -------------------------------------------------

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    applyPaymentFilters(
                            filteredData,
                            searchField,
                            methodFilter,
                            statusFilter
                    );
                }
        );


        // -------------------------------------------------
        // SET TABLE DATA
        // -------------------------------------------------

        table.setItems(
                filteredData
        );


        // -------------------------------------------------
        // ADD PAYMENT
        // -------------------------------------------------

        addPaymentButton.setOnAction(
                event -> {

                    showPaymentDialog(
                            null,
                            table
                    );
                }
        );


        // -------------------------------------------------
        // ADD EVERYTHING
        // -------------------------------------------------

        tableBox.getChildren().addAll(
                titleBox,
                filters,
                table
        );


        return tableBox;
    }


    // =====================================================
    // CREATE TABLE COLUMN
    // =====================================================

    private TableColumn<Payment, String> createColumn(

            String title,
            double width,
            Function<Payment, String> getter

    ) {

        TableColumn<Payment, String> column =
                new TableColumn<>(
                        title
                );


        column.setPrefWidth(
                width
        );


        column.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                safeValue(
                                        getter.apply(
                                                data.getValue()
                                        )
                                )
                        )
        );


        return column;
    }


    // =====================================================
    // STYLE NORMAL PAYMENT COLUMN
    // =====================================================

    private void stylePaymentColumn(
            TableColumn<Payment, String> column
    ) {

        column.setCellFactory(
                col ->
                        new TableCell<Payment, String>() {

                            @Override
                            protected void updateItem(
                                    String item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );


                                if (empty || item == null) {

                                    setText(
                                            null
                                    );

                                    return;
                                }


                                setText(
                                        item
                                );


                                setStyle(
                                        "-fx-background-color:white;" +
                                        "-fx-text-fill:#475569;" +
                                        "-fx-font-size:10;" +
                                        "-fx-border-color:transparent transparent #e8edf2 transparent;" +
                                        "-fx-padding:0 8 0 8;"
                                );


                                setAlignment(
                                        Pos.CENTER_LEFT
                                );
                            }
                        }
        );
    }


    // =====================================================
    // STYLE BOLD PAYMENT COLUMN
    // =====================================================

    private void stylePaymentBoldColumn(
            TableColumn<Payment, String> column
    ) {

        column.setCellFactory(
                col ->
                        new TableCell<Payment, String>() {

                            @Override
                            protected void updateItem(
                                    String item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );


                                if (empty || item == null) {

                                    setText(
                                            null
                                    );

                                    return;
                                }


                                setText(
                                        item
                                );


                                setStyle(
                                        "-fx-background-color:white;" +
                                        "-fx-text-fill:#1e293b;" +
                                        "-fx-font-size:10;" +
                                        "-fx-font-weight:bold;" +
                                        "-fx-border-color:transparent transparent #e8edf2 transparent;" +
                                        "-fx-padding:0 8 0 8;"
                                );


                                setAlignment(
                                        Pos.CENTER_LEFT
                                );
                            }
                        }
        );
    }


    // =====================================================
    // MODERN PAYMENT TABLE HEADER
    // =====================================================

    private void stylePaymentTableHeader(
            TableView<Payment> table
    ) {

        table.applyCss();


        // =================================================
        // HEADER BACKGROUND
        // =================================================

        Node headerBackground =
                table.lookup(
                        ".column-header-background"
                );


        if (headerBackground != null) {

            headerBackground.setStyle(
                    "-fx-background-color:#f1f5f9;" +
                    "-fx-background-radius:0;" +
                    "-fx-border-color:transparent;"
            );
        }


        // =================================================
        // INDIVIDUAL COLUMN HEADERS
        // =================================================

        for (
                Node node :
                table.lookupAll(".column-header")
        ) {

            node.setStyle(
                    "-fx-background-color:#f1f5f9;" +
                    "-fx-border-color:transparent transparent #e2e8f0 transparent;" +
                    "-fx-background-radius:0;"
            );
        }


        // =================================================
        // HEADER TEXT
        // =================================================

        for (
                Node node :
                table.lookupAll(".column-header .label")
        ) {

            node.setStyle(
                    "-fx-text-fill:#334155;" +
                    "-fx-font-size:10;" +
                    "-fx-font-weight:bold;" +
                    "-fx-background-color:transparent;"
            );
        }


        // =================================================
        // EMPTY HEADER/FILLER
        // =================================================

        Node filler =
                table.lookup(
                        ".filler"
                );


        if (filler != null) {

            filler.setStyle(
                    "-fx-background-color:#f1f5f9;" +
                    "-fx-border-color:transparent;"
            );
        }
    }


    // =====================================================
    // APPLY SEARCH + FILTERS
    // =====================================================

    private void applyPaymentFilters(

            FilteredList<Payment> filteredData,

            TextField searchField,

            ComboBox<String> methodFilter,

            ComboBox<String> statusFilter

    ) {

        filteredData.setPredicate(
                payment -> {

                    String searchText =
                            searchField
                                    .getText()
                                    .trim()
                                    .toLowerCase();


                    String selectedMethod =
                            methodFilter.getValue();


                    String selectedStatus =
                            statusFilter.getValue();


                    boolean searchMatches =

                            searchText.isEmpty()

                            ||

                            contains(
                                    payment.getPaymentId(),
                                    searchText
                            )

                            ||

                            contains(
                                    payment.getCargoId(),
                                    searchText
                            )

                            ||

                            contains(
                                    payment.getShip(),
                                    searchText
                            )

                            ||

                            contains(
                                    payment.getCustomer(),
                                    searchText
                            )

                            ||

                            contains(
                                    String.valueOf(
                                            payment.getAmount()
                                    ),
                                    searchText
                            )

                            ||

                            contains(
                                    payment.getMethod(),
                                    searchText
                            )

                            ||

                            contains(
                                    payment.getStatus(),
                                    searchText
                            );


                    boolean methodMatches =

                            selectedMethod == null

                            ||

                            selectedMethod.equals(
                                    "All Methods"
                            )

                            ||

                            selectedMethod.equals(
                                    payment.getMethod()
                            );


                    boolean statusMatches =

                            selectedStatus == null

                            ||

                            selectedStatus.equals(
                                    "All Statuses"
                            )

                            ||

                            selectedStatus.equals(
                                    payment.getStatus()
                            );


                    return searchMatches
                            &&
                            methodMatches
                            &&
                            statusMatches;
                }
        );
    }


    // =====================================================
    // SHOW PAYMENT DETAILS
    // =====================================================

    private void showPaymentDetails(
            Payment payment
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );


        alert.setTitle(
                "Payment Details"
        );


        alert.setHeaderText(
                "Payment "
                + safeValue(
                        payment.getPaymentId()
                )
        );


        alert.setContentText(

                "Payment ID: "
                + safeValue(
                        payment.getPaymentId()
                )

                + "\n\n"

                + "Cargo ID: "
                + safeValue(
                        payment.getCargoId()
                )

                + "\n\n"

                + "Ship: "
                + safeValue(
                        payment.getShip()
                )

                + "\n\n"

                + "Customer: "
                + safeValue(
                        payment.getCustomer()
                )

                + "\n\n"

                + "Amount: "
                + formatCurrency(
                        payment.getAmount()
                )

                + "\n\n"

                + "Date: "
                + safeValue(
                        payment.getDate()
                )

                + "\n\n"

                + "Method: "
                + safeValue(
                        payment.getMethod()
                )

                + "\n\n"

                + "Status: "
                + safeValue(
                        payment.getStatus()
                )
        );


        alert.showAndWait();
    }


    // =====================================================
    // ADD / EDIT PAYMENT DIALOG
    // =====================================================

    private void showPaymentDialog(

            Payment existingPayment,

            TableView<Payment> table

    ) {

        boolean isEdit =
                existingPayment != null;


        Dialog<ButtonType> dialog =
                new Dialog<>();


        dialog.setTitle(
                isEdit
                        ? "Edit Payment"
                        : "Add Payment"
        );


        dialog.setHeaderText(
                isEdit
                        ? "Edit Payment "
                        + existingPayment.getPaymentId()
                        : "Add New Payment"
        );


        // =================================================
        // PAYMENT ID
        // =================================================

        TextField paymentIdField =
                new TextField();


        paymentIdField.setPromptText(
                "Payment ID"
        );


        // =================================================
        // CARGO DROPDOWN
        // =================================================

        ComboBox<Cargo> cargoBox =
                new ComboBox<>();


        cargoBox.setPrefWidth(
                250
        );


        cargoBox.setItems(
                FXCollections.observableArrayList(
                        cargoData
                )
        );


        // =================================================
        // DISPLAY CARGO ID
        // =================================================

        cargoBox.setCellFactory(
                listView ->
                        new ListCell<Cargo>() {

                            @Override
                            protected void updateItem(
                                    Cargo item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );


                                if (empty || item == null) {

                                    setText(
                                            null
                                    );

                                } else {

                                    setText(
                                            item.getCargoId()
                                    );
                                }
                            }
                        }
        );


        cargoBox.setButtonCell(
                new ListCell<Cargo>() {

                    @Override
                    protected void updateItem(
                            Cargo item,
                            boolean empty
                    ) {

                        super.updateItem(
                                item,
                                empty
                        );


                        if (empty || item == null) {

                            setText(
                                    null
                            );

                        } else {

                            setText(
                                    item.getCargoId()
                            );
                        }
                    }
                }
        );


        // =================================================
        // SHIP DROPDOWN
        // =================================================

        ComboBox<Ship> shipBox =
                new ComboBox<>();


        shipBox.setPrefWidth(
                250
        );


        shipBox.setItems(
                FXCollections.observableArrayList(
                        shipData
                )
        );


        // =================================================
        // DISPLAY SHIP NAME + ID
        // =================================================

        shipBox.setCellFactory(
                listView ->
                        new ListCell<Ship>() {

                            @Override
                            protected void updateItem(
                                    Ship item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );


                                if (empty || item == null) {

                                    setText(
                                            null
                                    );

                                } else {

                                    setText(
                                            item.getShipName()
                                            + " ("
                                            + item.getShipId()
                                            + ")"
                                    );
                                }
                            }
                        }
        );


        shipBox.setButtonCell(
                new ListCell<Ship>() {

                    @Override
                    protected void updateItem(
                            Ship item,
                            boolean empty
                    ) {

                        super.updateItem(
                                item,
                                empty
                        );


                        if (empty || item == null) {

                            setText(
                                    null
                            );

                        } else {

                            setText(
                                    item.getShipName()
                                    + " ("
                                    + item.getShipId()
                                    + ")"
                            );
                        }
                    }
                }
        );


        // =================================================
        // AUTOMATIC SHIP SELECTION FROM CARGO
        // =================================================

        cargoBox.setOnAction(
                event -> {

                    Cargo selectedCargo =
                            cargoBox.getValue();


                    if (selectedCargo == null) {

                        shipBox.getSelectionModel()
                                .clearSelection();

                        return;
                    }


                    String cargoShipId =
                            selectedCargo.getShip();


                    if (cargoShipId == null
                            ||
                            cargoShipId.trim().isEmpty()) {

                        shipBox.getSelectionModel()
                                .clearSelection();

                        return;
                    }


                    for (Ship ship : shipData) {

                        if (ship == null) {

                            continue;
                        }


                        String shipId =
                                ship.getShipId();


                        if (shipId != null
                                &&
                                shipId.trim()
                                        .equalsIgnoreCase(
                                                cargoShipId.trim()
                                        )) {

                            shipBox.setValue(
                                    ship
                            );

                            return;
                        }
                    }


                    shipBox.getSelectionModel()
                            .clearSelection();
                }
        );


        // =================================================
        // CUSTOMER
        // =================================================

        TextField customerField =
                new TextField();


        customerField.setPromptText(
                "Customer"
        );


        customerField.setPrefWidth(
                250
        );


        // =================================================
        // AMOUNT
        // =================================================

        TextField amountField =
                new TextField();


        amountField.setPromptText(
                "Amount"
        );


        // =================================================
        // PAYMENT DATE
        // =================================================

        DatePicker datePicker =
                new DatePicker();


        datePicker.setValue(
                LocalDate.now()
        );


        // =================================================
        // PAYMENT METHOD
        // =================================================

        ComboBox<String> methodBox =
                new ComboBox<>();


        methodBox.getItems().addAll(
                "Bank Transfer",
                "UPI",
                "Card"
        );


        methodBox.setValue(
                "Bank Transfer"
        );


        // =================================================
        // PAYMENT STATUS
        // =================================================

        ComboBox<String> statusBox =
                new ComboBox<>();


        statusBox.getItems().addAll(
                "Paid",
                "Pending",
                "Failed",
                "Overdue"
        );


        statusBox.setValue(
                "Pending"
        );


        // =================================================
        // LOAD EXISTING PAYMENT FOR EDIT
        // =================================================

        if (isEdit) {

            paymentIdField.setText(
                    safeValueForField(
                            existingPayment.getPaymentId()
                    )
            );


            for (Cargo cargo : cargoData) {

                if (cargo == null) {

                    continue;
                }


                if (safeEquals(
                        cargo.getCargoId(),
                        existingPayment.getCargoId()
                )) {

                    cargoBox.setValue(
                            cargo
                    );


                    String cargoShipId =
                            cargo.getShip();


                    for (Ship ship : shipData) {

                        if (ship == null) {

                            continue;
                        }


                        if (safeEquals(
                                ship.getShipId(),
                                cargoShipId
                        )) {

                            shipBox.setValue(
                                    ship
                            );

                            break;
                        }
                    }


                    break;
                }
            }


            customerField.setText(
                    safeValueForField(
                            existingPayment.getCustomer()
                    )
            );


            amountField.setText(
                    String.valueOf(
                            existingPayment.getAmount()
                    )
            );


            String existingDate =
                    existingPayment.getDate();


            if (existingDate != null
                    &&
                    !existingDate.isEmpty()) {

                try {

                    datePicker.setValue(
                            LocalDate.parse(
                                    existingDate
                            )
                    );

                } catch (Exception e) {

                    datePicker.setValue(
                            LocalDate.now()
                    );
                }
            }


            if (existingPayment.getMethod() != null
                    &&
                    methodBox.getItems().contains(
                            existingPayment.getMethod()
                    )) {

                methodBox.setValue(
                        existingPayment.getMethod()
                );
            }


            if (existingPayment.getStatus() != null
                    &&
                    statusBox.getItems().contains(
                            existingPayment.getStatus()
                    )) {

                statusBox.setValue(
                        existingPayment.getStatus()
                );
            }


            paymentIdField.setEditable(
                    false
            );
        }


        // =================================================
        // FORM
        // =================================================

        GridPane grid =
                new GridPane();


        grid.setHgap(
                10
        );


        grid.setVgap(
                12
        );


        grid.setPadding(
                new Insets(20)
        );


        grid.add(
                new Label("Payment ID:"),
                0,
                0
        );


        grid.add(
                paymentIdField,
                1,
                0
        );


        grid.add(
                new Label("Cargo ID:"),
                0,
                1
        );


        grid.add(
                cargoBox,
                1,
                1
        );


        grid.add(
                new Label("Ship:"),
                0,
                2
        );


        grid.add(
                shipBox,
                1,
                2
        );


        grid.add(
                new Label("Customer:"),
                0,
                3
        );


        grid.add(
                customerField,
                1,
                3
        );


        grid.add(
                new Label("Amount:"),
                0,
                4
        );


        grid.add(
                amountField,
                1,
                4
        );


        grid.add(
                new Label("Date:"),
                0,
                5
        );


        grid.add(
                datePicker,
                1,
                5
        );


        grid.add(
                new Label("Method:"),
                0,
                6
        );


        grid.add(
                methodBox,
                1,
                6
        );


        grid.add(
                new Label("Status:"),
                0,
                7
        );


        grid.add(
                statusBox,
                1,
                7
        );


        dialog.getDialogPane().setContent(
                grid
        );


        // =================================================
        // BUTTONS
        // =================================================

        ButtonType saveButton =
                new ButtonType(
                        "Save",
                        ButtonBar.ButtonData.OK_DONE
                );


        ButtonType cancelButton =
                new ButtonType(
                        "Cancel",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );


        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        saveButton,
                        cancelButton
                );


        // =================================================
        // SAVE PAYMENT
        // =================================================

        dialog.setResultConverter(
                button -> {

                    if (button != saveButton) {

                        return button;
                    }


                    String paymentId =
                            paymentIdField
                                    .getText()
                                    .trim();


                    if (paymentId.isEmpty()) {

                        showWarning(
                                "Please enter Payment ID."
                        );

                        return null;
                    }


                    Cargo selectedCargo =
                            cargoBox.getValue();


                    if (selectedCargo == null) {

                        showWarning(
                                "Please select a Cargo."
                        );

                        return null;
                    }


                    Ship selectedShip =
                            shipBox.getValue();


                    if (selectedShip == null) {

                        showWarning(
                                "Please select a Ship."
                        );

                        return null;
                    }


                    String customer =
                            customerField
                                    .getText()
                                    .trim();


                    if (customer.isEmpty()) {

                        showWarning(
                                "Please enter Customer."
                        );

                        return null;
                    }


                    double amount;


                    try {

                        amount =
                                Double.parseDouble(
                                        amountField
                                                .getText()
                                                .trim()
                                );


                        if (amount < 0) {

                            showWarning(
                                    "Amount cannot be negative."
                            );

                            return null;
                        }

                    } catch (NumberFormatException e) {

                        showWarning(
                                "Please enter a valid amount."
                        );

                        return null;
                    }


                    LocalDate selectedDate =
                            datePicker.getValue();


                    if (selectedDate == null) {

                        showWarning(
                                "Please select a payment date."
                        );

                        return null;
                    }


                    Payment payment;


                    if (isEdit) {

                        payment =
                                existingPayment;

                    } else {

                        payment =
                                new Payment();
                    }


                    payment.setPaymentId(
                            paymentId
                    );


                    payment.setCargoId(
                            selectedCargo.getCargoId()
                    );


                    payment.setShip(
                            selectedShip.getShipName()
                    );


                    payment.setCustomer(
                            customer
                    );


                    payment.setAmount(
                            amount
                    );


                    payment.setDate(
                            selectedDate.toString()
                    );


                    payment.setMethod(
                            methodBox.getValue()
                    );


                    payment.setStatus(
                            statusBox.getValue()
                    );


                    // =============================================
                    // SAVE TO FIREBASE
                    // =============================================

                    if (isEdit) {

                        paymentController.updatePayment(
                                payment
                        );

                    } else {

                        paymentController.savePayment(
                                payment
                        );
                    }


                    // =============================================
                    // CREATE NOTIFICATION
                    // =============================================

                    if (isEdit) {

                        notificationController.createNotification(

                                "Payment record updated",

                                payment.getPaymentId()
                                        +
                                        " was updated successfully.",

                                "EDIT"
                        );

                    } else {

                        notificationController.createNotification(

                                "Payment registered",

                                payment.getPaymentId()
                                        +
                                        " was added successfully.",

                                "ADD"
                        );
                    }


                    // =============================================
                    // UPDATE LOCAL LIST
                    // =============================================

                    if (!isEdit) {

                        paymentData.add(
                                payment
                        );
                    }


                    // =============================================
                    // REFRESH TABLE
                    // =============================================

                    if (table != null) {

                        table.refresh();
                    }


                    // =============================================
                    // REFRESH CARDS
                    // =============================================

                    refreshStatistics();


                    // =============================================
                    // SUCCESS MESSAGE
                    // =============================================

                    showSuccess(

                            isEdit
                                    ? "Payment updated successfully."
                                    : "Payment added successfully."
                    );


                    return button;
                }
        );


        dialog.showAndWait();
    }


    // =====================================================
    // DELETE PAYMENT CONFIRMATION
    // =====================================================

    private void showDeleteConfirmation(

            Payment payment,

            TableView<Payment> table

    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );


        alert.setTitle(
                "Delete Payment"
        );


        alert.setHeaderText(
                "Delete Payment?"
        );


        alert.setContentText(

                "Are you sure you want to delete "
                + payment.getPaymentId()
                + "?"
        );


        ButtonType deleteButton =
                new ButtonType(
                        "Delete",
                        ButtonBar.ButtonData.OK_DONE
                );


        ButtonType cancelButton =
                new ButtonType(
                        "Cancel",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );


        alert.getButtonTypes().setAll(
                deleteButton,
                cancelButton
        );


        alert.showAndWait()
                .ifPresent(
                        response -> {

                            if (response == deleteButton) {

                                // =============================================
                                // DELETE FROM FIREBASE
                                // =============================================

                                paymentController.deletePayment(
                                        payment.getPaymentId()
                                );


                                // =============================================
                                // CREATE NOTIFICATION
                                // =============================================

                                notificationController.createNotification(

                                        "Payment deleted",

                                        payment.getPaymentId()
                                                +
                                                " was deleted from the system.",

                                        "DELETE"
                                );


                                // =============================================
                                // REMOVE FROM LOCAL LIST
                                // =============================================

                                paymentData.remove(
                                        payment
                                );


                                // =============================================
                                // REFRESH TABLE
                                // =============================================

                                if (table != null) {

                                    table.refresh();
                                }


                                // =============================================
                                // REFRESH CARDS
                                // =============================================

                                refreshStatistics();


                                showSuccess(
                                        "Payment deleted successfully."
                                );
                            }
                        }
                );
    }


    // =====================================================
    // SAFE STRING VALUE
    // =====================================================

    private String safeValue(
            String value
    ) {

        if (value == null
                ||
                value.isEmpty()) {

            return "—";
        }


        return value;
    }


    // =====================================================
    // SAFE FIELD VALUE
    // =====================================================

    private String safeValueForField(
            String value
    ) {

        if (value == null) {

            return "";
        }


        return value;
    }


    // =====================================================
    // SAFE EQUALS
    // =====================================================

    private boolean safeEquals(
            String first,
            String second
    ) {

        if (first == null
                &&
                second == null) {

            return true;
        }


        if (first == null
                ||
                second == null) {

            return false;
        }


        return first.equalsIgnoreCase(
                second
        );
    }


    // =====================================================
    // SEARCH HELPER
    // =====================================================

    private boolean contains(
            String value,
            String search
    ) {

        if (value == null) {

            return false;
        }


        return value
                .toLowerCase()
                .contains(search);
    }


    // =====================================================
    // WARNING MESSAGE
    // =====================================================

    private void showWarning(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );


        alert.setTitle(
                "Warning"
        );


        alert.setHeaderText(
                null
        );


        alert.setContentText(
                message
        );


        alert.showAndWait();
    }


    // =====================================================
    // SUCCESS MESSAGE
    // =====================================================

    private void showSuccess(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );


        alert.setTitle(
                "Success"
        );


        alert.setHeaderText(
                null
        );


        alert.setContentText(
                message
        );


        alert.showAndWait();
    }
}