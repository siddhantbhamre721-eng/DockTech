package com.projectui.view.portauthority;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Function;

import com.projectui.controller.portauthority.CargoController;
import com.projectui.controller.portauthority.ShipController;
import com.projectui.controller.portauthority.NotificationController;
import com.projectui.model.portauthority.Cargo;
import com.projectui.model.portauthority.Ship;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


public class CargoManagementPage {

    private VBox content;


    // =====================================================
    // CARGO CONTROLLER
    // =====================================================

    private CargoController cargoController =
            new CargoController();


    // =====================================================
    // NOTIFICATION CONTROLLER
    // =====================================================

    private NotificationController notificationController =
            new NotificationController();


    // =====================================================
    // SHIP CONTROLLER
    // =====================================================

    private ShipController shipController =
            new ShipController();


    // =====================================================
    // STATISTIC CARD VALUES
    // =====================================================

    private Label totalCargoValue;
    private Label receivedValue;
    private Label inTransitValue;
    private Label pendingInspectionValue;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public CargoManagementPage() {


        // =====================================================
        // MAIN CONTENT
        // =====================================================

        content = new VBox(20);

        content.setPadding(
                new Insets(25)
        );

        content.setStyle(
                "-fx-background-color: #f4f8ff;"
        );


        // =====================================================
        // PAGE TITLE
        // =====================================================

        Label title =
                new Label("Cargo Management");

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        28
                )
        );


        Label subtitle =
                new Label(
                        "Monitor cargo received, loaded and dispatched through the port."
                );

        subtitle.setTextFill(
                Color.GRAY
        );


        VBox heading =
                new VBox(5);

        heading.getChildren().addAll(
                title,
                subtitle
        );


        // =====================================================
        // SUMMARY CARDS
        // =====================================================

        HBox summaryCards =
                new HBox(15);


        totalCargoValue =
                new Label();

        receivedValue =
                new Label();

        inTransitValue =
                new Label();

        pendingInspectionValue =
                new Label();


        // =====================================================
        // TOTAL CARGO CARD
        // =====================================================

        VBox totalCargo =
                createCard(
                        "Total Cargo",
                        totalCargoValue
                );


        // =====================================================
        // RECEIVED CARD
        // =====================================================

        VBox received =
                createCard(
                        "Received",
                        receivedValue
                );


        // =====================================================
        // IN TRANSIT CARD
        // =====================================================

        VBox inTransit =
                createCard(
                        "In Transit",
                        inTransitValue
                );


        // =====================================================
        // PENDING INSPECTION CARD
        // =====================================================

        VBox pendingInspection =
                createCard(
                        "Pending Inspection",
                        pendingInspectionValue
                );


        HBox.setHgrow(
                totalCargo,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                received,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                inTransit,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                pendingInspection,
                Priority.ALWAYS
        );


        summaryCards.getChildren().addAll(
                totalCargo,
                received,
                inTransit,
                pendingInspection
        );


        // =====================================================
        // SEARCH + FILTERS + ADD CARGO
        // =====================================================

        TextField searchField =
                new TextField();

        searchField.setPromptText(
                "Search Cargo..."
        );

        searchField.setPrefWidth(250);


        // =====================================================
        // CARGO TYPE FILTER
        // =====================================================

        ComboBox<String> cargoTypeFilter =
                new ComboBox<>();

        cargoTypeFilter.getItems().addAll(
                "All Cargo Types",
                "Container",
                "Bulk",
                "Refrigerated",
                "Liquid"
        );

        cargoTypeFilter.setValue(
                "All Cargo Types"
        );

        cargoTypeFilter.setPrefWidth(180);


        // =====================================================
        // STATUS FILTER
        // =====================================================

        ComboBox<String> statusFilter =
                new ComboBox<>();

        statusFilter.getItems().addAll(
                "All Status",
                "Received",
                "Loading",
                "Loaded",
                "In Transit",
                "Delivered",
                "Pending Inspection"
        );

        statusFilter.setValue(
                "All Status"
        );

        statusFilter.setPrefWidth(160);


        // =====================================================
        // ADD CARGO BUTTON
        // =====================================================

        Button addCargoButton =
                new Button("+ Add Cargo");

        addCargoButton.setPrefWidth(
                120
        );


        addCargoButton.setStyle(
                "-fx-background-color:#2563eb;" +
                "-fx-text-fill:white;" +
                "-fx-font-size:10;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:6;" +
                "-fx-padding:8 14 8 14;" +
                "-fx-cursor:hand;"
        );


        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );


        HBox filters =
                new HBox(
                        12,
                        searchField,
                        cargoTypeFilter,
                        statusFilter,
                        spacer,
                        addCargoButton
                );

        filters.setAlignment(
                Pos.CENTER_LEFT
        );


        // =====================================================
        // CARGO TABLE
        // =====================================================

        TableView<Cargo> table =
                new TableView<>();


        // Compact table height
        table.setPrefHeight(
                360
        );

        table.setMinHeight(
                360
        );

        table.setMaxHeight(
                360
        );


        table.setFixedCellSize(
                38
        );


        table.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-border-color:transparent;" +
                "-fx-table-cell-border-color:transparent;" +
                "-fx-font-size:10;"
        );


        // =====================================================
        // TABLE COLUMNS
        // =====================================================

        TableColumn<Cargo, String> cargoId =
                createColumn(
                        "Cargo ID",
                        100,
                        Cargo::getCargoId
                );


        // =====================================================
        // SHIP ID COLUMN
        // =====================================================

        TableColumn<Cargo, String> ship =
                createColumn(
                        "Ship ID",
                        130,
                        Cargo::getShip
                );


        TableColumn<Cargo, String> cargoType =
                createColumn(
                        "Cargo Type",
                        120,
                        Cargo::getCargoType
                );


        TableColumn<Cargo, String> weight =
                createColumn(
                        "Weight",
                        100,
                        Cargo::getWeight
                );


        TableColumn<Cargo, String> origin =
                createColumn(
                        "Origin",
                        110,
                        Cargo::getOrigin
                );


        TableColumn<Cargo, String> destination =
                createColumn(
                        "Destination",
                        120,
                        Cargo::getDestination
                );


        TableColumn<Cargo, String> containers =
                createColumn(
                        "Containers",
                        100,
                        Cargo::getContainers
                );


        TableColumn<Cargo, String> status =
                createColumn(
                        "Status",
                        130,
                        Cargo::getStatus
                );


        // =====================================================
        // ACTION COLUMN
        // =====================================================

        TableColumn<Cargo, String> action =
                new TableColumn<>("Action");

        action.setPrefWidth(
                200
        );


        action.setCellFactory(
                column -> new TableCell<>() {

                    private final Button viewButton =
                            new Button("View");

                    private final Button editButton =
                            new Button("Edit");

                    private final Button deleteButton =
                            new Button("Delete");


                    {
                        // =================================================
                        // VIEW BUTTON STYLE
                        // =================================================

                        viewButton.setStyle(
                                "-fx-background-color:#f1f5f9;" +
                                "-fx-text-fill:#1e293b;" +
                                "-fx-font-size:9;" +
                                "-fx-font-weight:bold;" +
                                "-fx-background-radius:5;" +
                                "-fx-border-color:#cbd5e1;" +
                                "-fx-border-radius:5;" +
                                "-fx-padding:4 8 4 8;" +
                                "-fx-cursor:hand;"
                        );


                        // =================================================
                        // EDIT BUTTON STYLE
                        // =================================================

                        editButton.setStyle(
                                "-fx-background-color:#eff6ff;" +
                                "-fx-text-fill:#1d4ed8;" +
                                "-fx-font-size:9;" +
                                "-fx-font-weight:bold;" +
                                "-fx-background-radius:5;" +
                                "-fx-border-color:#bfdbfe;" +
                                "-fx-border-radius:5;" +
                                "-fx-padding:4 8 4 8;" +
                                "-fx-cursor:hand;"
                        );


                        // =================================================
                        // DELETE BUTTON STYLE
                        // =================================================

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


                        // =================================================
                        // VIEW BUTTON ACTION
                        // =================================================

                        viewButton.setOnAction(e -> {

                            Cargo cargo =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());


                            Alert alert =
                                    new Alert(
                                            Alert.AlertType.INFORMATION
                                    );

                            alert.setTitle(
                                    "Cargo Details"
                            );

                            alert.setHeaderText(
                                    "Cargo Details"
                            );

                            alert.setContentText(

                                    "Cargo ID: "
                                    + cargo.getCargoId()

                                    + "\nShip ID: "
                                    + cargo.getShip()

                                    + "\nCargo Type: "
                                    + cargo.getCargoType()

                                    + "\nWeight: "
                                    + cargo.getWeight()

                                    + "\nOrigin: "
                                    + cargo.getOrigin()

                                    + "\nDestination: "
                                    + cargo.getDestination()

                                    + "\nContainers: "
                                    + cargo.getContainers()

                                    + "\nStatus: "
                                    + cargo.getStatus()
                            );


                            alert.showAndWait();

                        });


                        // =================================================
                        // EDIT BUTTON ACTION
                        // =================================================

                        editButton.setOnAction(e -> {

                            Cargo cargo =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());


                            showEditCargoDialog(
                                    cargo,
                                    table
                            );

                        });


                        // =================================================
                        // DELETE BUTTON ACTION
                        // =================================================

                        deleteButton.setOnAction(e -> {

                            Cargo cargo =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());


                            showDeleteConfirmation(
                                    cargo,
                                    table
                            );

                        });

                    }


                    // =================================================
                    // UPDATE ACTION CELL
                    // =================================================

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
                                    new HBox(6);

                            buttons.setAlignment(
                                    Pos.CENTER
                            );

                            buttons.getChildren().addAll(
                                    viewButton,
                                    editButton,
                                    deleteButton
                            );

                            setGraphic(
                                    buttons
                            );

                            setText(
                                    null
                            );

                            setStyle(
                                    "-fx-background-color:white;" +
                                    "-fx-border-color:transparent transparent #e8edf2 transparent;"
                            );
                        }
                    }
                }
        );


        // =====================================================
        // ADD COLUMNS TO TABLE
        // =====================================================

        table.getColumns().addAll(
                cargoId,
                ship,
                cargoType,
                weight,
                origin,
                destination,
                containers,
                status,
                action
        );


        // =====================================================
        // NORMAL COLUMN STYLING
        // =====================================================

        styleCargoColumn(
                cargoId
        );

        styleCargoColumn(
                ship
        );

        styleCargoColumn(
                cargoType
        );

        styleCargoColumn(
                weight
        );

        styleCargoColumn(
                origin
        );

        styleCargoColumn(
                destination
        );

        styleCargoColumn(
                containers
        );


        // =====================================================
        // CARGO ID - STRONGER TEXT
        // =====================================================

        cargoId.setCellFactory(
                column -> new TableCell<Cargo, String>() {

                    @Override
                    protected void updateItem(
                            String item,
                            boolean empty
                    ) {

                        super.updateItem(
                                item,
                                empty
                        );


                        if (
                                empty
                                ||
                                item == null
                        ) {

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
                                "-fx-text-fill:#334155;" +
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


        // =====================================================
        // SHIP ID - STRONGER TEXT
        // =====================================================

        ship.setCellFactory(
                column -> new TableCell<Cargo, String>() {

                    @Override
                    protected void updateItem(
                            String item,
                            boolean empty
                    ) {

                        super.updateItem(
                                item,
                                empty
                        );


                        if (
                                empty
                                ||
                                item == null
                        ) {

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


        // =====================================================
        // STATUS BADGE
        // =====================================================

        status.setCellFactory(
                column -> new TableCell<Cargo, String>() {

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


                        if (
                                empty
                                ||
                                item == null
                        ) {

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
                        // RECEIVED
                        // =============================================

                        if (
                                item.equalsIgnoreCase(
                                        "Received"
                                )
                        ) {

                            statusLabel.setStyle(
                                    "-fx-background-color:#dbeafe;" +
                                    "-fx-text-fill:#2563eb;" +
                                    "-fx-background-radius:12;" +
                                    "-fx-font-size:9;" +
                                    "-fx-font-weight:bold;" +
                                    "-fx-padding:4 9 4 9;"
                            );
                        }


                        // =============================================
                        // LOADING
                        // =============================================

                        else if (
                                item.equalsIgnoreCase(
                                        "Loading"
                                )
                        ) {

                            statusLabel.setStyle(
                                    "-fx-background-color:#ffedd5;" +
                                    "-fx-text-fill:#ea580c;" +
                                    "-fx-background-radius:12;" +
                                    "-fx-font-size:9;" +
                                    "-fx-font-weight:bold;" +
                                    "-fx-padding:4 9 4 9;"
                            );
                        }


                        // =============================================
                        // LOADED
                        // =============================================

                        else if (
                                item.equalsIgnoreCase(
                                        "Loaded"
                                )
                        ) {

                            statusLabel.setStyle(
                                    "-fx-background-color:#cffafe;" +
                                    "-fx-text-fill:#0891b2;" +
                                    "-fx-background-radius:12;" +
                                    "-fx-font-size:9;" +
                                    "-fx-font-weight:bold;" +
                                    "-fx-padding:4 9 4 9;"
                            );
                        }


                        // =============================================
                        // IN TRANSIT
                        // =============================================

                        else if (
                                item.equalsIgnoreCase(
                                        "In Transit"
                                )
                        ) {

                            statusLabel.setStyle(
                                    "-fx-background-color:#dbeafe;" +
                                    "-fx-text-fill:#2563eb;" +
                                    "-fx-background-radius:12;" +
                                    "-fx-font-size:9;" +
                                    "-fx-font-weight:bold;" +
                                    "-fx-padding:4 9 4 9;"
                            );
                        }


                        // =============================================
                        // DELIVERED
                        // =============================================

                        else if (
                                item.equalsIgnoreCase(
                                        "Delivered"
                                )
                        ) {

                            statusLabel.setStyle(
                                    "-fx-background-color:#dcfce7;" +
                                    "-fx-text-fill:#15803d;" +
                                    "-fx-background-radius:12;" +
                                    "-fx-font-size:9;" +
                                    "-fx-font-weight:bold;" +
                                    "-fx-padding:4 9 4 9;"
                            );
                        }


                        // =============================================
                        // PENDING INSPECTION
                        // =============================================

                        else if (
                                item.equalsIgnoreCase(
                                        "Pending Inspection"
                                )
                        ) {

                            statusLabel.setStyle(
                                    "-fx-background-color:#fff7ed;" +
                                    "-fx-text-fill:#c2410c;" +
                                    "-fx-background-radius:12;" +
                                    "-fx-font-size:9;" +
                                    "-fx-font-weight:bold;" +
                                    "-fx-padding:4 9 4 9;"
                            );
                        }


                        // =============================================
                        // DEFAULT
                        // =============================================

                        else {

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


        // =====================================================
        // TABLE HEADER STYLE
        // =====================================================

        table.skinProperty().addListener(
                (observable, oldSkin, newSkin) -> {

                    if (newSkin != null) {

                        Platform.runLater(() -> {

                            styleTableHeader(
                                    table
                            );

                        });
                    }
                }
        );


        // =====================================================
        // LOAD CARGO FROM FIRESTORE
        // =====================================================

        ObservableList<Cargo> cargoData =
                FXCollections.observableArrayList();


        List<Cargo> cargos =
                cargoController.getCargos();


        cargoData.addAll(
                cargos
        );


        // =====================================================
        // FILTERED DATA
        // =====================================================

        FilteredList<Cargo> filteredData =
                new FilteredList<>(
                        cargoData,
                        cargo -> true
                );


        table.setItems(
                filteredData
        );


        // =====================================================
        // INITIAL STATISTICS
        // =====================================================

        refreshStatistics(
                cargoData
        );


        // =====================================================
        // ADD CARGO BUTTON ACTION
        // =====================================================

        addCargoButton.setOnAction(event -> {

            showAddCargoDialog(
                    cargoData,
                    table
            );

        });


        // =====================================================
        // SEARCH LISTENER
        // =====================================================

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    applyFilters(
                            filteredData,
                            searchField,
                            cargoTypeFilter,
                            statusFilter
                    );

                }
        );


        // =====================================================
        // CARGO TYPE FILTER LISTENER
        // =====================================================

        cargoTypeFilter.setOnAction(event -> {

            applyFilters(
                    filteredData,
                    searchField,
                    cargoTypeFilter,
                    statusFilter
            );

        });


        // =====================================================
        // STATUS FILTER LISTENER
        // =====================================================

        statusFilter.setOnAction(event -> {

            applyFilters(
                    filteredData,
                    searchField,
                    cargoTypeFilter,
                    statusFilter
            );

        });


        // =====================================================
        // ADD EVERYTHING TO CONTENT
        // =====================================================

        content.getChildren().addAll(
                heading,
                summaryCards,
                filters,
                table
        );
    }


    // =============================================================
    // STYLE NORMAL CARGO TABLE COLUMN
    // =============================================================

    private void styleCargoColumn(
            TableColumn<Cargo, String> column
    ) {

        column.setCellFactory(
                col -> new TableCell<Cargo, String>() {

                    @Override
                    protected void updateItem(
                            String item,
                            boolean empty
                    ) {

                        super.updateItem(
                                item,
                                empty
                        );


                        if (
                                empty
                                ||
                                item == null
                        ) {

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


    // =============================================================
    // STYLE TABLE HEADER
    // =============================================================

    private void styleTableHeader(
            TableView<Cargo> table
    ) {

        table.applyCss();


        // =====================================================
        // HEADER BACKGROUND
        // =====================================================

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


        // =====================================================
        // INDIVIDUAL HEADERS
        // =====================================================

        for (
                Node node :
                table.lookupAll(
                        ".column-header"
                )
        ) {

            node.setStyle(
                    "-fx-background-color:#f1f5f9;" +
                    "-fx-border-color:transparent transparent #e2e8f0 transparent;" +
                    "-fx-background-radius:0;"
            );
        }


        // =====================================================
        // HEADER TEXT
        // =====================================================

        for (
                Node node :
                table.lookupAll(
                        ".column-header .label"
                )
        ) {

            node.setStyle(
                    "-fx-text-fill:#334155;" +
                    "-fx-font-size:10;" +
                    "-fx-font-weight:bold;" +
                    "-fx-background-color:transparent;"
            );
        }


        // =====================================================
        // TABLE FILLER
        // =====================================================

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


    // =============================================================
    // REFRESH STATISTICS
    // =============================================================

    private void refreshStatistics(
            List<Cargo> cargos
    ) {

        double totalWeight = 0;

        double receivedWeight = 0;

        double inTransitWeight = 0;

        double pendingInspectionWeight = 0;


        for (Cargo cargo : cargos) {

            double weight =
                    parseWeight(
                            cargo.getWeight()
                    );


            totalWeight += weight;


            if ("Received".equals(
                    cargo.getStatus()
            )) {

                receivedWeight += weight;
            }


            if ("In Transit".equals(
                    cargo.getStatus()
            )) {

                inTransitWeight += weight;
            }


            if ("Pending Inspection".equals(
                    cargo.getStatus()
            )) {

                pendingInspectionWeight += weight;
            }
        }


        totalCargoValue.setText(
                formatWeight(totalWeight)
                + " Tons"
        );


        receivedValue.setText(
                formatWeight(receivedWeight)
                + " Tons"
        );


        inTransitValue.setText(
                formatWeight(inTransitWeight)
                + " Tons"
        );


        pendingInspectionValue.setText(
                formatWeight(pendingInspectionWeight)
                + " Tons"
        );
    }


    // =============================================================
    // PARSE WEIGHT
    // =============================================================

    private double parseWeight(
            String weight
    ) {

        if (
                weight == null
                ||
                weight.trim().isEmpty()
        ) {

            return 0;
        }


        try {

            String numericValue =
                    weight
                            .replace(",", "")
                            .replace("T", "")
                            .trim();


            return Double.parseDouble(
                    numericValue
            );

        } catch (Exception e) {

            return 0;
        }
    }


    // =============================================================
    // FORMAT WEIGHT
    // =============================================================

    private String formatWeight(
            double weight
    ) {

        DecimalFormat formatter =
                new DecimalFormat(
                        "#,##0.##"
                );


        return formatter.format(
                weight
        );
    }


    // =============================================================
    // APPLY ALL FILTERS
    // =============================================================

    private void applyFilters(

            FilteredList<Cargo> filteredData,

            TextField searchField,

            ComboBox<String> cargoTypeFilter,

            ComboBox<String> statusFilter

    ) {


        String searchText =
                searchField
                        .getText()
                        .toLowerCase()
                        .trim();


        String selectedType =
                cargoTypeFilter.getValue();


        String selectedStatus =
                statusFilter.getValue();


        filteredData.setPredicate(
                cargo -> {


                    // =================================================
                    // SEARCH
                    // =================================================

                    boolean searchMatch = true;


                    if (!searchText.isEmpty()) {

                        searchMatch =

                                safeContains(
                                        cargo.getCargoId(),
                                        searchText
                                )

                                ||

                                safeContains(
                                        cargo.getShip(),
                                        searchText
                                )

                                ||

                                safeContains(
                                        cargo.getCargoType(),
                                        searchText
                                )

                                ||

                                safeContains(
                                        cargo.getWeight(),
                                        searchText
                                )

                                ||

                                safeContains(
                                        cargo.getOrigin(),
                                        searchText
                                )

                                ||

                                safeContains(
                                        cargo.getDestination(),
                                        searchText
                                )

                                ||

                                safeContains(
                                        cargo.getContainers(),
                                        searchText
                                )

                                ||

                                safeContains(
                                        cargo.getStatus(),
                                        searchText
                                );
                    }


                    // =================================================
                    // CARGO TYPE
                    // =================================================

                    boolean typeMatch =

                            selectedType == null

                            ||

                            selectedType.equals(
                                    "All Cargo Types"
                            )

                            ||

                            selectedType.equals(
                                    cargo.getCargoType()
                            );


                    // =================================================
                    // STATUS
                    // =================================================

                    boolean statusMatch =

                            selectedStatus == null

                            ||

                            selectedStatus.equals(
                                    "All Status"
                            )

                            ||

                            selectedStatus.equals(
                                    cargo.getStatus()
                            );


                    return searchMatch
                            && typeMatch
                            && statusMatch;
                }
        );
    }


    // =============================================================
    // SAFE SEARCH
    // =============================================================

    private boolean safeContains(
            String value,
            String searchText
    ) {

        return value != null
                && value.toLowerCase()
                        .contains(searchText);
    }


    // =============================================================
    // LOAD SHIPS
    // =============================================================

    private ObservableList<Ship> getShipData() {

        ObservableList<Ship> shipData =
                FXCollections.observableArrayList();


        List<Ship> ships =
                shipController.getShips();


        shipData.addAll(
                ships
        );


        return shipData;
    }


    // =============================================================
    // CONFIGURE SHIP ID COMBOBOX
    // =============================================================

    private void configureShipComboBox(
            ComboBox<Ship> shipBox
    ) {

        shipBox.setCellFactory(
                listView -> new ListCell<Ship>() {

                    @Override
                    protected void updateItem(
                            Ship ship,
                            boolean empty
                    ) {

                        super.updateItem(
                                ship,
                                empty
                        );


                        if (
                                empty
                                ||
                                ship == null
                        ) {

                            setText(
                                    null
                            );

                        } else {

                            setText(
                                    ship.getShipId()
                            );
                        }
                    }
                }
        );


        shipBox.setButtonCell(
                new ListCell<Ship>() {

                    @Override
                    protected void updateItem(
                            Ship ship,
                            boolean empty
                    ) {

                        super.updateItem(
                                ship,
                                empty
                        );


                        if (
                                empty
                                ||
                                ship == null
                        ) {

                            setText(
                                    null
                            );

                        } else {

                            setText(
                                    ship.getShipId()
                            );
                        }
                    }
                }
        );
    }


    // =============================================================
    // FIND SHIP BY ID
    // =============================================================

    private Ship findShipById(
            ObservableList<Ship> ships,
            String shipId
    ) {

        if (
                shipId == null
                ||
                shipId.trim().isEmpty()
        ) {

            return null;
        }


        for (Ship ship : ships) {

            if (
                    ship != null
                    &&
                    ship.getShipId() != null
                    &&
                    ship.getShipId()
                            .trim()
                            .equalsIgnoreCase(
                                    shipId.trim()
                            )
            ) {

                return ship;
            }
        }


        return null;
    }


    // =============================================================
    // SHOW ADD CARGO DIALOG
    // =============================================================

    private void showAddCargoDialog(

            ObservableList<Cargo> cargoData,

            TableView<Cargo> table

    ) {


        Dialog<ButtonType> dialog =
                new Dialog<>();


        dialog.setTitle(
                "Add Cargo"
        );

        dialog.setHeaderText(
                "Register New Cargo"
        );


        // =====================================================
        // LOAD SHIPS
        // =====================================================

        ObservableList<Ship> shipData =
                getShipData();


        // =====================================================
        // SHIP ID FIELD
        // =====================================================

        ComboBox<Ship> shipBox =
                new ComboBox<>(
                        shipData
                );

        shipBox.setPromptText(
                "Select Ship ID"
        );

        shipBox.setPrefWidth(
                220
        );


        configureShipComboBox(
                shipBox
        );


        // =====================================================
        // CARGO TYPE
        // =====================================================

        ComboBox<String> cargoTypeField =
                new ComboBox<>();

        cargoTypeField.getItems().addAll(
                "Container",
                "Bulk",
                "Refrigerated",
                "Liquid"
        );

        cargoTypeField.setPromptText(
                "Cargo type"
        );


        // =====================================================
        // WEIGHT
        // =====================================================

        TextField weightField =
                new TextField();

        weightField.setPromptText(
                "e.g. 1500 T"
        );


        // =====================================================
        // ORIGIN
        // =====================================================

        TextField originField =
                new TextField();

        originField.setPromptText(
                "Origin"
        );


        // =====================================================
        // DESTINATION
        // =====================================================

        TextField destinationField =
                new TextField();

        destinationField.setPromptText(
                "Destination"
        );


        // =====================================================
        // CONTAINERS
        // =====================================================

        TextField containersField =
                new TextField();

        containersField.setPromptText(
                "Number of containers"
        );


        // =====================================================
        // STATUS
        // =====================================================

        ComboBox<String> statusField =
                new ComboBox<>();

        statusField.getItems().addAll(
                "Received",
                "Loading",
                "Loaded",
                "In Transit",
                "Delivered",
                "Pending Inspection"
        );

        statusField.setPromptText(
                "Status"
        );


        // =====================================================
        // FORM LAYOUT
        // =====================================================

        GridPane form =
                new GridPane();

        form.setHgap(10);

        form.setVgap(10);

        form.setPadding(
                new Insets(15)
        );


        form.add(
                new Label("Ship ID:"),
                0,
                0
        );

        form.add(
                shipBox,
                1,
                0
        );


        form.add(
                new Label("Cargo Type:"),
                0,
                1
        );

        form.add(
                cargoTypeField,
                1,
                1
        );


        form.add(
                new Label("Weight:"),
                0,
                2
        );

        form.add(
                weightField,
                1,
                2
        );


        form.add(
                new Label("Origin:"),
                0,
                3
        );

        form.add(
                originField,
                1,
                3
        );


        form.add(
                new Label("Destination:"),
                0,
                4
        );

        form.add(
                destinationField,
                1,
                4
        );


        form.add(
                new Label("Containers:"),
                0,
                5
        );

        form.add(
                containersField,
                1,
                5
        );


        form.add(
                new Label("Status:"),
                0,
                6
        );

        form.add(
                statusField,
                1,
                6
        );


        dialog.getDialogPane().setContent(
                form
        );


        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        ButtonType.OK,
                        ButtonType.CANCEL
                );


        // =====================================================
        // HANDLE ADD
        // =====================================================

        dialog.setResultConverter(
                button -> {


                    if (button == ButtonType.OK) {


                        Ship selectedShip =
                                shipBox.getValue();


                        String newCargoType =
                                cargoTypeField
                                        .getValue();


                        String newWeight =
                                weightField
                                        .getText()
                                        .trim();


                        String newOrigin =
                                originField
                                        .getText()
                                        .trim();


                        String newDestination =
                                destinationField
                                        .getText()
                                        .trim();


                        String newContainers =
                                containersField
                                        .getText()
                                        .trim();


                        String newStatus =
                                statusField
                                        .getValue();


                        // =================================================
                        // VALIDATION
                        // =================================================

                        if (
                                selectedShip == null
                                ||
                                newCargoType == null
                                ||
                                newWeight.isEmpty()
                                ||
                                newOrigin.isEmpty()
                                ||
                                newDestination.isEmpty()
                                ||
                                newContainers.isEmpty()
                                ||
                                newStatus == null
                        ) {


                            Alert alert =
                                    new Alert(
                                            Alert.AlertType.WARNING
                                    );


                            alert.setTitle(
                                    "Missing Information"
                            );

                            alert.setHeaderText(
                                    null
                            );

                            alert.setContentText(
                                    "Please fill in all cargo details."
                            );


                            alert.showAndWait();


                            return null;
                        }


                        // =================================================
                        // GENERATE CARGO ID
                        // =================================================

                        String cargoIdValue =
                                generateNextCargoId(
                                        cargoData
                                );


                        // =================================================
                        // CREATE CARGO
                        // =================================================

                        Cargo cargo =
                                new Cargo(

                                        cargoIdValue,

                                        selectedShip.getShipId(),

                                        newCargoType,

                                        newWeight,

                                        newOrigin,

                                        newDestination,

                                        newContainers,

                                        newStatus
                                );


                        // =================================================
                        // SAVE TO FIRESTORE
                        // =================================================

                        cargoController.saveCargo(
                                cargo
                        );


                        // =================================================
                        // CREATE NOTIFICATION
                        // =================================================

                        notificationController.createNotification(

                                "Cargo registered",

                                cargo.getCargoId()
                                        +
                                        " was added successfully.",

                                "ADD"
                        );


                        // =================================================
                        // ADD TO LOCAL LIST
                        // =================================================

                        cargoData.add(
                                cargo
                        );


                        // =================================================
                        // REFRESH STATISTICS
                        // =================================================

                        refreshStatistics(
                                cargoData
                        );


                        table.refresh();


                        // =================================================
                        // SUCCESS MESSAGE
                        // =================================================

                        Alert success =
                                new Alert(
                                        Alert.AlertType.INFORMATION
                                );

                        success.setTitle(
                                "Success"
                        );

                        success.setHeaderText(
                                null
                        );

                        success.setContentText(
                                "Cargo registered successfully."
                        );

                        success.showAndWait();
                    }


                    return button;
                }
        );


        dialog.showAndWait();
    }


    // =============================================================
    // GENERATE NEXT CARGO ID
    // =============================================================

    private String generateNextCargoId(
            List<Cargo> cargos
    ) {

        int highestId =
                1294;


        for (Cargo cargo : cargos) {

            try {

                String id =
                        cargo.getCargoId();


                if (
                        id != null
                        &&
                        id.startsWith("CGO-")
                ) {


                    int number =
                            Integer.parseInt(
                                    id.substring(4)
                            );


                    if (number > highestId) {

                        highestId =
                                number;
                    }
                }

            } catch (Exception e) {

                // Ignore invalid Cargo ID
            }
        }


        return "CGO-" + (highestId + 1);
    }


    // =============================================================
    // SHOW EDIT CARGO DIALOG
    // =============================================================

    private void showEditCargoDialog(

            Cargo cargo,

            TableView<Cargo> table

    ) {


        Dialog<ButtonType> dialog =
                new Dialog<>();


        dialog.setTitle(
                "Edit Cargo"
        );

        dialog.setHeaderText(
                "Edit Cargo Details"
        );


        ButtonType okButton =
                new ButtonType(
                        "OK",
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
                        okButton,
                        cancelButton
                );


        // =====================================================
        // LOAD SHIPS
        // =====================================================

        ObservableList<Ship> shipData =
                getShipData();


        // =====================================================
        // SHIP ID
        // =====================================================

        ComboBox<Ship> shipBox =
                new ComboBox<>(
                        shipData
                );

        shipBox.setPromptText(
                "Select Ship ID"
        );

        shipBox.setPrefWidth(
                220
        );


        configureShipComboBox(
                shipBox
        );


        // =====================================================
        // SELECT CURRENT SHIP
        // =====================================================

        Ship currentShip =
                findShipById(
                        shipData,
                        cargo.getShip()
                );


        if (currentShip != null) {

            shipBox.setValue(
                    currentShip
            );
        }


        // =====================================================
        // CARGO TYPE
        // =====================================================

        ComboBox<String> cargoTypeBox =
                new ComboBox<>();

        cargoTypeBox.getItems().addAll(
                "Container",
                "Bulk",
                "Refrigerated",
                "Liquid"
        );

        cargoTypeBox.setValue(
                cargo.getCargoType()
        );


        // =====================================================
        // WEIGHT
        // =====================================================

        TextField weightField =
                new TextField(
                        cargo.getWeight()
                );


        // =====================================================
        // ORIGIN
        // =====================================================

        TextField originField =
                new TextField(
                        cargo.getOrigin()
                );


        // =====================================================
        // DESTINATION
        // =====================================================

        TextField destinationField =
                new TextField(
                        cargo.getDestination()
                );


        // =====================================================
        // CONTAINERS
        // =====================================================

        TextField containersField =
                new TextField(
                        cargo.getContainers()
                );


        // =====================================================
        // STATUS
        // =====================================================

        ComboBox<String> statusBox =
                new ComboBox<>();

        statusBox.getItems().addAll(
                "Received",
                "Loading",
                "Loaded",
                "In Transit",
                "Delivered",
                "Pending Inspection"
        );

        statusBox.setValue(
                cargo.getStatus()
        );


        // =====================================================
        // FORM LAYOUT
        // =====================================================

        GridPane grid =
                new GridPane();

        grid.setHgap(10);

        grid.setVgap(10);

        grid.setPadding(
                new Insets(
                        20,
                        20,
                        10,
                        20
                )
        );


        grid.add(
                new Label("Ship ID:"),
                0,
                0
        );

        grid.add(
                shipBox,
                1,
                0
        );


        grid.add(
                new Label("Cargo Type:"),
                0,
                1
        );

        grid.add(
                cargoTypeBox,
                1,
                1
        );


        grid.add(
                new Label("Weight:"),
                0,
                2
        );

        grid.add(
                weightField,
                1,
                2
        );


        grid.add(
                new Label("Origin:"),
                0,
                3
        );

        grid.add(
                originField,
                1,
                3
        );


        grid.add(
                new Label("Destination:"),
                0,
                4
        );

        grid.add(
                destinationField,
                1,
                4
        );


        grid.add(
                new Label("Containers:"),
                0,
                5
        );

        grid.add(
                containersField,
                1,
                5
        );


        grid.add(
                new Label("Status:"),
                0,
                6
        );

        grid.add(
                statusBox,
                1,
                6
        );


        dialog.getDialogPane().setContent(
                grid
        );


        // =====================================================
        // HANDLE EDIT
        // =====================================================

        dialog.setResultConverter(button -> {


            if (button == okButton) {


                Ship selectedShip =
                        shipBox.getValue();


                // =================================================
                // VALIDATION
                // =================================================

                if (
                        selectedShip == null
                        ||
                        cargoTypeBox.getValue() == null
                        ||
                        weightField.getText()
                                .trim()
                                .isEmpty()
                        ||
                        originField.getText()
                                .trim()
                                .isEmpty()
                        ||
                        destinationField.getText()
                                .trim()
                                .isEmpty()
                        ||
                        containersField.getText()
                                .trim()
                                .isEmpty()
                        ||
                        statusBox.getValue() == null
                ) {


                    Alert alert =
                            new Alert(
                                    Alert.AlertType.WARNING
                            );


                    alert.setTitle(
                            "Missing Information"
                    );

                    alert.setHeaderText(
                            null
                    );

                    alert.setContentText(
                            "Please fill in all cargo details."
                    );


                    alert.showAndWait();


                    return null;
                }


                // =================================================
                // UPDATE CARGO OBJECT
                // =================================================

                cargo.setShip(
                        selectedShip.getShipId()
                );


                cargo.setCargoType(
                        cargoTypeBox
                                .getValue()
                );


                cargo.setWeight(
                        weightField
                                .getText()
                                .trim()
                );


                cargo.setOrigin(
                        originField
                                .getText()
                                .trim()
                );


                cargo.setDestination(
                        destinationField
                                .getText()
                                .trim()
                );


                cargo.setContainers(
                        containersField
                                .getText()
                                .trim()
                );


                cargo.setStatus(
                        statusBox
                                .getValue()
                );


                // =================================================
                // UPDATE FIRESTORE
                // =================================================

                cargoController.updateCargo(
                        cargo
                );


                // =================================================
                // CREATE NOTIFICATION
                // =================================================

                notificationController.createNotification(

                        "Cargo record updated",

                        cargo.getCargoId()
                                +
                                " was updated successfully.",

                        "EDIT"
                );


                // =================================================
                // REFRESH TABLE
                // =================================================

                table.refresh();


                // =================================================
                // REFRESH STATISTICS
                // =================================================

                refreshStatistics(
                        cargoController.getCargos()
                );


                Alert success =
                        new Alert(
                                Alert.AlertType.INFORMATION
                        );


                success.setTitle(
                        "Success"
                );

                success.setHeaderText(
                        null
                );

                success.setContentText(
                        "Cargo updated successfully."
                );


                success.showAndWait();
            }


            return button;
        });


        dialog.showAndWait();
    }


    // =============================================================
    // DELETE CARGO
    // =============================================================

    private void showDeleteConfirmation(

            Cargo cargo,

            TableView<Cargo> table

    ) {


        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );


        alert.setTitle(
                "Delete Cargo"
        );


        alert.setHeaderText(
                "Delete Cargo?"
        );


        alert.setContentText(

                "Are you sure you want to delete "

                + cargo.getCargoId()

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
                .ifPresent(response -> {


                    if (response == deleteButton) {


                        // =================================================
                        // DELETE FROM FIRESTORE
                        // =================================================

                        cargoController.deleteCargo(
                                cargo.getCargoId()
                        );


                        // =================================================
                        // CREATE NOTIFICATION
                        // =================================================

                        notificationController.createNotification(

                                "Cargo deleted",

                                cargo.getCargoId()
                                        +
                                        " was deleted from the system.",

                                "DELETE"
                        );


                        // =================================================
                        // REMOVE FROM LOCAL LIST
                        // =================================================

                        FilteredList<Cargo> filteredList =
                                (FilteredList<Cargo>)
                                        table.getItems();


                        filteredList
                                .getSource()
                                .remove(cargo);


                        // =================================================
                        // REFRESH STATISTICS
                        // =================================================

                        refreshStatistics(
                                cargoController.getCargos()
                        );


                        Alert success =
                                new Alert(
                                        Alert.AlertType.INFORMATION
                                );


                        success.setTitle(
                                "Success"
                        );


                        success.setHeaderText(
                                null
                        );


                        success.setContentText(
                                "Cargo deleted successfully."
                        );


                        success.showAndWait();
                    }
                });
    }


    // =============================================================
    // CREATE SUMMARY CARD
    // =============================================================

    private VBox createCard(
            String title,
            Label valueLabel
    ) {


        Label titleLabel =
                new Label(title);


        titleLabel.setTextFill(
                Color.GRAY
        );


        valueLabel.setText(
                "0 Tons"
        );


        valueLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        24
                )
        );


        VBox card =
                new VBox(10);


        card.setPadding(
                new Insets(15)
        );


        card.setPrefHeight(
                100
        );


        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#d9e2f0;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;"
        );


        card.getChildren().addAll(
                titleLabel,
                valueLabel
        );


        return card;
    }


    // =============================================================
    // CREATE TABLE COLUMN
    // =============================================================

    private TableColumn<Cargo, String> createColumn(

            String title,
            double width,
            Function<Cargo, String> valueGetter

    ) {


        TableColumn<Cargo, String> column =
                new TableColumn<>(
                        title
                );


        column.setPrefWidth(
                width
        );


        column.setCellValueFactory(
                data -> new SimpleStringProperty(

                        valueGetter.apply(
                                data.getValue()
                        )
                )
        );


        return column;
    }


    // =============================================================
    // GET PAGE CONTENT
    // =============================================================

    public VBox getContent() {

        return content;
    }
}