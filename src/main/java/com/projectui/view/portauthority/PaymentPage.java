package com.projectui.view.portauthority;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;



// =====================================================
// PAYMENT MANAGEMENT PAGE
// =====================================================

public class PaymentPage {

    // =====================================================
    // MAIN PAGE CONTAINER
    // =====================================================

    private VBox root;


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
        // SUMMARY CARDS
        // =================================================

        HBox summaryCards =
                createSummaryCards();

        // =================================================
        // PAYMENT TRANSACTIONS TABLE
        // =================================================

        VBox paymentTable = createPaymentTable();


        // =================================================
        // ADD COMPONENTS TO PAGE
        // =================================================

        root.getChildren().addAll(
                titleBox,
                summaryCards,
                paymentTable
        );
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

        VBox totalRevenue =
                createSummaryCard(
                        "Total Revenue",
                        "₹12.5L",
                        "+8.2% vs last month",
                        "#00a878"
                );


        // =================================================
        // PAID
        // =================================================

        VBox paid =
                createSummaryCard(
                        "Paid",
                        "₹9.8L",
                        "784 transactions",
                        "#7b879c"
                );


        // =================================================
        // PENDING
        // =================================================

        VBox pending =
                createSummaryCard(
                        "Pending",
                        "₹2.1L",
                        "62 invoices",
                        "#ff8a00"
                );


        // =================================================
        // OVERDUE
        // =================================================

        VBox overdue =
                createSummaryCard(
                        "Overdue",
                        "₹60K",
                        "9 accounts overdue",
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
            String value,
            String description,
            String descriptionColor
    ) {

        // -------------------------------------------------
        // CARD HEADING
        // -------------------------------------------------

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


        // -------------------------------------------------
        // MAIN VALUE
        // -------------------------------------------------

        Label valueLabel =
                new Label(value);

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


        // -------------------------------------------------
        // DESCRIPTION
        // -------------------------------------------------

        Label descriptionLabel =
                new Label(description);

        descriptionLabel.setFont(
                Font.font(
                        "Arial",
                        13
                )
        );

        descriptionLabel.setTextFill(
                Color.web(descriptionColor)
        );


        // -------------------------------------------------
        // CARD
        // -------------------------------------------------

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

        card.setPrefHeight(120);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #dce5f0;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );


        return card;
    }

    // =====================================================
    // PAYMENT TRANSACTIONS TABLE
    // =====================================================

    private VBox createPaymentTable() {

        VBox tableBox = new VBox(10);

        // -------------------------------------------------
        // SECTION TITLE
        // -------------------------------------------------

        Label tableTitle =
                new Label("Payment Transactions");

        Label tableSubtitle =
                new Label("All port and cargo payment records");

        VBox titleBox = new VBox(3);

        titleBox.getChildren().addAll(
                tableTitle,
                tableSubtitle
        );


        // -------------------------------------------------
        // FILTERS
        // -------------------------------------------------


        // -------------------------------------------------
        // SEARCH PAYMENT
        // -------------------------------------------------

        TextField searchField =
                new TextField();

        searchField.setPromptText(
                "Search Payment..."
        );

        searchField.setPrefWidth(260);

        // Payment Method Filter
        ComboBox<String> methodFilter = new ComboBox<>();

        methodFilter.getItems().addAll(
                "All Methods",
                "Bank Transfer",
                "UPI",
                "Card"
        );

        methodFilter.setValue("All Methods");

        methodFilter.setPrefWidth(150);


        // Payment Status Filter
        ComboBox<String> statusFilter = new ComboBox<>();

        statusFilter.getItems().addAll(
                "All Statuses",
                "Paid",
                "Pending",
                "Failed",
                "Overdue"
        );

        statusFilter.setValue("All Statuses");

        statusFilter.setPrefWidth(150);


        // Put both dropdowns beside each other
        HBox filters = new HBox(10);

        filters.getChildren().addAll(
                searchField,
                methodFilter,
                statusFilter
        );


        // -------------------------------------------------
        // TABLE
        // -------------------------------------------------

        TableView<String[]> table =
                new TableView<>();

        table.setPrefHeight(400);


        // -------------------------------------------------
        // TABLE COLUMNS
        // -------------------------------------------------

        TableColumn<String[], String> paymentId =
                createColumn(
                        "Payment ID",
                        100,
                        0
                );


        TableColumn<String[], String> cargoId =
                createColumn(
                        "Cargo ID",
                        90,
                        1
                );


        TableColumn<String[], String> ship =
                createColumn(
                        "Ship",
                        120,
                        2
                );


        TableColumn<String[], String> customer =
                createColumn(
                        "Customer",
                        150,
                        3
                );


        TableColumn<String[], String> amount =
                createColumn(
                        "Amount",
                        110,
                        4
                );


        TableColumn<String[], String> date =
                createColumn(
                        "Date",
                        100,
                        5
                );


        TableColumn<String[], String> method =
                createColumn(
                        "Method",
                        120,
                        6
                );


        TableColumn<String[], String> status =
                createColumn(
                        "Status",
                        100,
                        7
                );


        // -------------------------------------------------
        // ACTION COLUMN
        // -------------------------------------------------

        TableColumn<String[], String> action =
                new TableColumn<>("Action");

        action.setPrefWidth(150);


        action.setCellFactory(column ->
                new TableCell<String[], String>() {

                    private final Button viewButton =
                            new Button("View Payment");

                    {
                        viewButton.setOnAction(event -> {

                            String[] payment =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            Alert alert =
                                    new Alert(
                                            Alert.AlertType.INFORMATION
                                    );

                            alert.setTitle("Payment Details");

                            alert.setHeaderText(
                                    "Payment Details"
                            );

                            alert.setContentText(
                                    "Payment ID: " + payment[0] + "\n" +
                                    "Cargo ID: " + payment[1] + "\n" +
                                    "Ship: " + payment[2] + "\n" +
                                    "Customer: " + payment[3] + "\n" +
                                    "Amount: " + payment[4] + "\n" +
                                    "Date: " + payment[5] + "\n" +
                                    "Method: " + payment[6] + "\n" +
                                    "Status: " + payment[7]
                            );

                            alert.showAndWait();
                        });
                    }


                    @Override
                    protected void updateItem(
                            String item,
                            boolean empty
                    ) {

                        super.updateItem(item, empty);

                        if (empty) {

                            setGraphic(null);

                        } else {

                            setGraphic(viewButton);
                        }
                    }
                });


        // -------------------------------------------------
        // ADD COLUMNS TO TABLE
        // -------------------------------------------------

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
        // SAMPLE PAYMENT DATA
        // =================================================

        ObservableList<String[]> paymentData =
                FXCollections.observableArrayList();


        paymentData.addAll(

                new String[]{
                        "PAY-4821",
                        "CGO-1287",
                        "Oceanic Star",
                        "Bharat Logistics",
                        "₹1,45,000",
                        "12 Feb 2025",
                        "Bank Transfer",
                        "Paid"
                },

                new String[]{
                        "PAY-4820",
                        "CGO-1284",
                        "Blue Horizon",
                        "Maritime Freight Co.",
                        "₹88,500",
                        "11 Feb 2025",
                        "UPI",
                        "Pending"
                },

                new String[]{
                        "PAY-4819",
                        "CGO-1279",
                        "Neptune Voyager",
                        "Coastal Traders Ltd",
                        "₹2,10,000",
                        "10 Feb 2025",
                        "Card",
                        "Failed"
                },

                new String[]{
                        "PAY-4818",
                        "CGO-1271",
                        "Pacific Dawn",
                        "Global Shipping Inc",
                        "₹1,20,000",
                        "08 Feb 2025",
                        "Bank Transfer",
                        "Paid"
                },

                new String[]{
                        "PAY-4817",
                        "CGO-1268",
                        "Silver Wave",
                        "Anchor Exports",
                        "₹60,000",
                        "05 Feb 2025",
                        "UPI",
                        "Overdue"
                },

                new String[]{
                        "PAY-4816",
                        "CGO-1260",
                        "Golden Tide",
                        "Harbor Trade LLP",
                        "₹1,75,000",
                        "03 Feb 2025",
                        "Card",
                        "Paid"
                },

                new String[]{
                        "PAY-4815",
                        "CGO-1254",
                        "Coral Queen",
                        "Seaway Cargo Pvt",
                        "₹95,000",
                        "01 Feb 2025",
                        "Bank Transfer",
                        "Pending"
                }
        );


        // -------------------------------------------------
        // FILTER PAYMENT DATA
        // -------------------------------------------------

        FilteredList<String[]> filteredData =
                new FilteredList<>(
                        paymentData,
                        payment -> true
                );


       // -------------------------------------------------
        // METHOD FILTER
        // -------------------------------------------------

        methodFilter.setOnAction(event -> {

            applyPaymentFilters(
                    filteredData,
                    searchField,
                    methodFilter,
                    statusFilter
            );
        });


        // -------------------------------------------------
        // STATUS FILTER
        // -------------------------------------------------

        statusFilter.setOnAction(event -> {

            applyPaymentFilters(
                    filteredData,
                    searchField,
                    methodFilter,
                    statusFilter
            );
        });


        // -------------------------------------------------
        // SEARCH FIELD
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
        // SET FILTERED DATA IN TABLE
        // -------------------------------------------------

        table.setItems(filteredData);


        // -------------------------------------------------
        // ADD EVERYTHING TO TABLE BOX
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

    private TableColumn<String[], String> createColumn(
            String title,
            double width,
            int index
            ) {

        // Create the column with its heading
        TableColumn<String[], String> column =
                new TableColumn<>(title);

        // Set column width
        column.setPrefWidth(width);

        // Tell the column which value to display
        column.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue()[index]
                        )
        );

        return column;
    }

    // =====================================================
    // APPLY PAYMENT SEARCH + FILTERS
    // =====================================================

    private void applyPaymentFilters(
            FilteredList<String[]> filteredData,
            TextField searchField,
            ComboBox<String> methodFilter,
            ComboBox<String> statusFilter
    ) {

        filteredData.setPredicate(payment -> {

            // ---------------------------------------------
            // SEARCH TEXT
            // ---------------------------------------------

            String searchText =
                    searchField.getText()
                            .trim()
                            .toLowerCase();


            // ---------------------------------------------
            // SELECTED METHOD
            // ---------------------------------------------

            String selectedMethod =
                    methodFilter.getValue();


            // ---------------------------------------------
            // SELECTED STATUS
            // ---------------------------------------------

            String selectedStatus =
                    statusFilter.getValue();


            // ---------------------------------------------
            // SEARCH MATCH
            // ---------------------------------------------

            boolean searchMatches =
                    searchText.isEmpty()
                    ||
                    payment[0].toLowerCase().contains(searchText)
                    ||
                    payment[1].toLowerCase().contains(searchText)
                    ||
                    payment[2].toLowerCase().contains(searchText)
                    ||
                    payment[3].toLowerCase().contains(searchText)
                    ||
                    payment[4].toLowerCase().contains(searchText);


            // ---------------------------------------------
            // METHOD MATCH
            // ---------------------------------------------

            boolean methodMatches =
                    selectedMethod.equals("All Methods")
                    ||
                    payment[6].equals(selectedMethod);


            // ---------------------------------------------
            // STATUS MATCH
            // ---------------------------------------------

            boolean statusMatches =
                    selectedStatus.equals("All Statuses")
                    ||
                    payment[7].equals(selectedStatus);


            // ---------------------------------------------
            // ALL CONDITIONS MUST MATCH
            // ---------------------------------------------

            return searchMatches
                    && methodMatches
                    && statusMatches;
        });
    }
}