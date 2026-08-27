package com.projectui.view.portauthority;

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
import javafx.scene.layout.VBox;

import javafx.application.Platform;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.List;

import com.projectui.controller.portauthority.ShipController;
import com.projectui.model.portauthority.Ship;
import com.projectui.controller.portauthority.NotificationController;

public class ShipManagementPage {

    private final VBox root = new VBox(18);
    
    //=========================
    //Ship Controller

    private final ShipController shipController = new ShipController();

    private final NotificationController notificationController = new NotificationController();

    //State cards value  ----------------

    private Label totalShipsValue;
    private Label arrivingValue;
    private Label dockedValue;
    private Label departingValue;

    //========================

    public ShipManagementPage() {

        root.setPadding(
                new Insets(20)
        );

        root.setStyle(
                "-fx-background-color:#f3f8ff;"
        );

        // =====================================================
        // PAGE HEADING
        // =====================================================

        HBox heading =
                new HBox();

        heading.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox pageInfo =
                new VBox(4);

        Label title =
                new Label(
                        "Ship Management"
                );

        title.setStyle(
                "-fx-text-fill:#111827;" +
                "-fx-font-size:20;" +
                "-fx-font-weight:bold;"
        );

        Label subtitle =
                new Label(
                        "Monitor ships arriving, departing and operating within the port."
                );

        subtitle.setStyle(
                "-fx-text-fill:#8a93a1;" +
                "-fx-font-size:11;"
        );

        pageInfo.getChildren().addAll(
                title,
                subtitle
        );

        VBox space =
                new VBox();

        HBox.setHgrow(
                space,
                Priority.ALWAYS
        );

        heading.getChildren().addAll(
                pageInfo,
                space
        );

        

        // =====================================================
        // STATISTIC CARDS
        // =====================================================

        HBox statistics =
                new HBox(16);

        VBox totalShips =
                createStatCard(
                        "⚓",
                        "Total Ships",
                        "0",
                        "#eaf2ff",
                        "#0f5fff"
                );

        VBox arriving =
                createStatCard(
                        "↪",
                        "Arriving",
                        "0",
                        "#e6f7f5",
                        "#008f83"
                );

        VBox docked =
                createStatCard(
                        "⚓",
                        "Docked",
                        "0",
                        "#e7f7ee",
                        "#168548"
                );

        VBox departing =
                createStatCard(
                    "↗",
                    "Departing",
                    "0",
                    "#fff4e8",
                    "#c76b00"
                );

        HBox.setHgrow(
                totalShips,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                arriving,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                docked,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                departing,
                Priority.ALWAYS
        );

        statistics.getChildren().addAll(
                totalShips,
                arriving,
                docked,
                departing
        );

        refreshStatistics();

        // =====================================================
        // SEARCH AND FILTER ROW
        // =====================================================

        HBox filterRow =
                new HBox(12);

        filterRow.setAlignment(
                Pos.CENTER_LEFT
        );

        // SEARCH

        TextField searchField =
                new TextField();

        searchField.setPromptText(
                "Search"
        );

        searchField.setPrefHeight(
                36
        );

        searchField.setPrefWidth(
                200
        );

        searchField.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:7;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:7;" +
                "-fx-border-width:1;" +
                "-fx-text-fill:#334155;" +
                "-fx-prompt-text-fill:#94a3b8;" +
                "-fx-font-size:10;" +
                "-fx-padding:0 12 0 12;"
        );

        // TYPE FILTER

        ComboBox<String> typeFilter =
                new ComboBox<>();

        typeFilter.getItems().addAll(
                "All Types",
                "Container",
                "Tanker",
                "Bulk Carrier",
                "Cargo"
        );

        typeFilter.setValue(
                "All Types"
        );

        typeFilter.setPrefWidth(
                165
        );

        typeFilter.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:7;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:7;" +
                "-fx-border-width:1;" +
                "-fx-font-size:10;"
        );

        // STATUS FILTER

        ComboBox<String> statusFilter =
                new ComboBox<>();

        statusFilter.getItems().addAll(
                "All Status",
                "Arriving",
                "Docked",
                "Departing"
        );

        statusFilter.setValue(
                "All Status"
        );

        
        statusFilter.setPrefWidth(
                165
        );

        statusFilter.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:7;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:7;" +
                "-fx-border-width:1;" +
                "-fx-font-size:10;"
        );

        searchField.setPrefHeight(36);
        searchField.setMinHeight(36);

        typeFilter.setPrefHeight(36);
        typeFilter.setMinHeight(36);

        statusFilter.setPrefHeight(36);
        statusFilter.setMinHeight(36);

        

        // SPACE

        VBox filterSpace =
                new VBox();

        HBox.setHgrow(
                filterSpace,
                Priority.ALWAYS
        );

        // REGISTER SHIP

        Button registerShip =
                new Button(
                        "+ Register Ship"
                );

        registerShip.setPrefHeight(
                36
        );

        registerShip.setPadding(
                new Insets(
                        0,
                        16,
                        0,
                        16
                )
        );

        registerShip.setStyle(
                "-fx-background-color:#0f5fff;" +
                "-fx-text-fill:white;" +
                "-fx-font-size:11;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:7;" +
                "-fx-cursor:hand;"
        );

        

        filterRow.getChildren().addAll(
                searchField,
                typeFilter,
                statusFilter,
                filterSpace,
                registerShip
        );

        // =====================================================
        // SHIP TABLE
        // =====================================================

        VBox tableBox =
                new VBox();

        tableBox.setPadding(
                new Insets(0)
        );

        tableBox.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:10;" +
                "-fx-border-color:#e5ebf3;" +
                "-fx-border-radius:10;"
        );

        TableView<String[]> table =
                new TableView<>();

        table.setPrefHeight(
                430
        );

        table.setColumnResizePolicy(
        TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        table.setRowFactory(tv -> {

                TableRow<String[]> row =
                        new TableRow<>();

                row.setPrefHeight(
                        38
                );

                row.setStyle(
                        "-fx-background-color:white;"
                );

                return row;
        });

        // =====================================================
        // TABLE STYLE
        // =====================================================

        table.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:transparent;" +
                "-fx-table-cell-border-color:transparent;" +
                "-fx-font-size:11;"
        );

        // =====================================================
        // SHIP ID
        // =====================================================

        TableColumn<String[], String> idColumn =
                new TableColumn<>(
                        "Ship ID"
                );

        idColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()[0]
                        )
        );

        // =====================================================
        // SHIP NAME
        // =====================================================

        TableColumn<String[], String> nameColumn =
                new TableColumn<>(
                        "Ship Name"
                );

        nameColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()[1]
                        )
        );

        // =====================================================
        // SHIP TYPE
        // =====================================================

        TableColumn<String[], String> typeColumn =
                new TableColumn<>(
                        "Ship Type"
                );

        typeColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()[2]
                        )
        );

        // =====================================================
        // IMO NUMBER
        // =====================================================

        TableColumn<String[], String> imoColumn =
                new TableColumn<>(
                        "IMO Number"
                );

        imoColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()[3]
                        )
        );

        // =====================================================
        // COMPANY
        // =====================================================

        TableColumn<String[], String> companyColumn =
                new TableColumn<>(
                        "Company"
                );

        companyColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()[4]
                        )
        );

        // =====================================================
        // ARRIVAL
        // =====================================================

        TableColumn<String[], String> arrivalColumn =
                new TableColumn<>(
                        "Arrival"
                );

        arrivalColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()[5]
                        )
        );

        // =====================================================
        // DEPARTURE
        // =====================================================

        TableColumn<String[], String> departureColumn =
                new TableColumn<>(
                        "Departure"
                );

        departureColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()[6]
                        )
        );

        // =====================================================
        // BERTH
        // =====================================================

        TableColumn<String[], String> berthColumn =
                new TableColumn<>(
                        "Berth"
                );

        berthColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()[7]
                        )
        );

        //=============================
        //STATUS
        //============================


        TableColumn<String[], String> statusColumn =
                new TableColumn<>("Status");

        statusColumn.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()[8]
                        )
        );

        // =====================================================
        // ACTION COLUMN
        // =====================================================

        TableColumn<String[], String> actionColumn =
                new TableColumn<>("Action");

        actionColumn.setPrefWidth(160);

        actionColumn.setCellFactory(column ->
                new TableCell<String[], String>() {

                // =============================================
                // CREATE BUTTONS
                // =============================================

                private final Button viewButton = new Button("View");

                private final Button editButton = new Button("Edit");

                private final Button deleteButton = new Button("Delete");


                // =============================================
                // BUTTON ACTIONS + Styling
                // =============================================

                {

                        // =============================================
                        // BUTTON STYLING
                        // =============================================

                        viewButton.setStyle(
                                "-fx-background-color:#f1f5f9;" +
                                "-fx-text-fill:#1e293b;" +
                                "-fx-font-size:10;" +
                                "-fx-font-weight:bold;" +
                                "-fx-background-radius:5;" +
                                "-fx-border-color:#cbd5e1;" +
                                "-fx-border-radius:5;" +
                                "-fx-padding:4 8 4 8;" +
                                "-fx-cursor:hand;"
                        );


                        editButton.setStyle(
                                "-fx-background-color:#eff6ff;" +
                                "-fx-text-fill:#1d4ed8;" +
                                "-fx-font-size:10;" +
                                "-fx-font-weight:bold;" +
                                "-fx-background-radius:5;" +
                                "-fx-border-color:#bfdbfe;" +
                                "-fx-border-radius:5;" +
                                "-fx-padding:4 8 4 8;" +
                                "-fx-cursor:hand;"
                        );


                        deleteButton.setStyle(
                                "-fx-background-color:#fff1f2;" +
                                "-fx-text-fill:#dc2626;" +
                                "-fx-font-size:10;" +
                                "-fx-font-weight:bold;" +
                                "-fx-background-radius:5;" +
                                "-fx-border-color:#fecdd3;" +
                                "-fx-border-radius:5;" +
                                "-fx-padding:4 8 4 8;" +
                                "-fx-cursor:hand;"
                        );


                        // -----------------------------------------
                        // VIEW BUTTON
                        // -----------------------------------------

                        viewButton.setOnAction(event -> {

                                String[] ship =
                                        getTableView()
                                                .getItems()
                                                .get(getIndex());

                                showShipDetails(ship);
                        });


                        // -----------------------------------------
                        // EDIT BUTTON
                        // -----------------------------------------

                        editButton.setOnAction(event -> {

                                String[] ship =
                                        getTableView()
                                                .getItems()
                                                .get(getIndex());

                                showEditShipDialog(
                                        ship,
                                        getTableView()
                                );
                        });

                        // -----------------------------------------
                        // DELETE BUTTON
                        // -----------------------------------------

                        deleteButton.setOnAction(event -> {

                        String[] ship =
                                getTableView()
                                        .getItems()
                                        .get(getIndex());

                        showDeleteConfirmation(
                                ship,
                                getTableView()
                        );
                        });
                }


                // =============================================
                // DISPLAY BUTTONS
                // =============================================

                @Override
                protected void updateItem(
                        String item,
                        boolean empty) {

                        super.updateItem(
                                item,
                                empty
                        );

                        if (empty) {

                        setGraphic(null);

                        } else {

                        HBox buttons =
                                new HBox(
                                        5,
                                        viewButton,
                                        editButton,
                                        deleteButton
                                );
                        
                        buttons.setAlignment(
                                Pos.CENTER
                        );

                        setGraphic(buttons);
                        }
                }
        });


        // =====================================================
        // ADD ALL COLUMNS
        // =====================================================

        table.getColumns().addAll(
                idColumn,
                nameColumn,
                typeColumn,
                imoColumn,
                companyColumn,
                arrivalColumn,
                departureColumn,
                berthColumn,
                statusColumn,
                actionColumn
        );

        // =====================================================
        // STYLE TABLE DATA COLUMNS
        // =====================================================

        styleTableColumn(idColumn);

        styleTableColumn(nameColumn);

        styleTableColumn(typeColumn);

        styleTableColumn(imoColumn);

        styleTableColumn(companyColumn);

        styleTableColumn(arrivalColumn);

        styleTableColumn(departureColumn);

        styleTableColumn(berthColumn);

        // =====================================================
        // SHIP NAME STYLE
        // =====================================================

        nameColumn.setCellFactory(
                column -> new TableCell<String[], String>() {

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

                        setText(null);

                        return;
                        }


                        setText(
                                item
                        );


                        setStyle(
                                "-fx-background-color:white;" +
                                "-fx-text-fill:#111827;" +
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

        statusColumn.setCellFactory(
                column -> new TableCell<String[], String>() {

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

                        setGraphic(null);
                        setText(null);

                        return;
                        }


                        statusLabel.setText(
                                item
                        );


                        // =============================================
                        // ARRIVING
                        // =============================================

                        if (
                                item.equalsIgnoreCase(
                                        "Arriving"
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
                        // DOCKED
                        // =============================================

                        else if (
                                item.equalsIgnoreCase(
                                        "Docked"
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
                        // DEPARTING
                        // =============================================

                        else if (
                                item.equalsIgnoreCase(
                                        "Departing"
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


                        setText(null);


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

        idColumn.setMinWidth(100);
        nameColumn.setMinWidth(150);
        typeColumn.setMinWidth(120);
        imoColumn.setMinWidth(120);
        companyColumn.setMinWidth(150);
        arrivalColumn.setMinWidth(130);
        departureColumn.setMinWidth(130);
        berthColumn.setMinWidth(90);

        // =====================================================
        // TABLE HEADER STYLE
        // =====================================================

        table.skinProperty().addListener(
                (observable, oldSkin, newSkin) -> {

                if (newSkin != null) {

                        Platform.runLater(() -> {

                        table.applyCss();


                        // =============================================
                        // HEADER BACKGROUND
                        // =============================================

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


                        // =============================================
                        // INDIVIDUAL COLUMN HEADERS
                        // =============================================

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


                        // =============================================
                        // HEADER LABELS
                        // =============================================

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


                        // =============================================
                        // EMPTY/FILLER AREA
                        // =============================================

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
                        });
                }
                }
        );

        // =====================================================
        // SHIP DATA FROM FIRESTORE
        // =====================================================

        ObservableList<String[]> shipData =
                FXCollections.observableArrayList();

        // Get all ships from Firestore
        List<Ship> ships =
                shipController.getShips();

        // Convert Ship objects into the String[] format
        // already used by our TableView
        for (Ship ship : ships) {

                shipData.add(new String[]{

                        ship.getShipId(),

                        ship.getShipName(),

                        ship.getShipType(),

                        ship.getImoNumber(),

                        ship.getCompany(),

                        ship.getArrival(),

                        ship.getDeparture(),

                        ship.getBerth(),

                        ship.getStatus()
                });
        }

        FilteredList<String[]> filteredData = new FilteredList<>(shipData);

        table.setItems(filteredData);

        // =====================================================
        // REGISTER SHIP BUTTON ACTION
        // =====================================================

        registerShip.setOnAction(e -> {

                showRegisterShipDialog(
                        shipData,
                        filteredData
                );

                refreshStatistics();

        });


        // =====================================================
        // SEARCH + TYPE + STATUS FILTERS
        // =====================================================

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                applyFilters(
                        filteredData,
                        searchField,
                        typeFilter,
                        statusFilter
                );
                
        });


        typeFilter.setOnAction(event -> {

                applyFilters(
                        filteredData,
                        searchField,
                        typeFilter,
                        statusFilter
                );
        });

        statusFilter.setOnAction(event -> {

                applyFilters(
                        filteredData,
                        searchField,
                        typeFilter,
                        statusFilter
                );
        });



        tableBox.getChildren().add(
                table
        );


        // =====================================================
        // ADD EVERYTHING TO PAGE
        // =====================================================

        root.getChildren().addAll(
                heading,
                statistics,
                filterRow,
                tableBox
        );
    }

        // =====================================================
        // APPLY ALL SHIP FILTERS
        // =====================================================

        private void applyFilters(
                FilteredList<String[]> filteredData,
                TextField searchField,
                ComboBox<String> typeFilter,
                ComboBox<String> statusFilter
                ) {

                filteredData.setPredicate(ship -> {

                        // =================================================
                        // SEARCH
                        // =================================================

                        String searchText =
                                searchField.getText()
                                        .toLowerCase()
                                        .trim();

                        boolean searchMatches =
                                searchText.isEmpty();

                        if (!searchMatches) {

                                for (String value : ship) {

                                        if (value
                                                .toLowerCase()
                                                .contains(searchText)) {

                                        searchMatches = true;

                                        break;
                                        }
                                }
                        }


                        // =================================================
                        // TYPE FILTER
                        // =================================================

                        String selectedType =
                                typeFilter.getValue();

                        boolean typeMatches =
                                selectedType.equals("All Types")
                                ||
                                ship[2].equals(selectedType);

                        // =================================================
                        // STATUS FILTER
                        // =================================================

                        String selectedStatus =
                                statusFilter.getValue();

                        boolean statusMatches =
                                selectedStatus.equals("All Status")
                                ||
                                ship[8].equals(selectedStatus);

                        

                        // =================================================
                        // ALL FILTERS MUST MATCH
                        // =================================================

                        return searchMatches && typeMatches && statusMatches;
                });
        }



        // =====================================================
        // REGISTER NEW SHIP
        // =====================================================

        private void showRegisterShipDialog(
                ObservableList<String[]> shipData,
                FilteredList<String[]> filteredData
                ) {

                // =================================================
                // CREATE DIALOG
                // =================================================

                Dialog<ButtonType> dialog =
                        new Dialog<>();

                dialog.setTitle("Register Ship");

                dialog.setHeaderText(
                        "Register a new ship"
                );


                // =================================================
                // FORM FIELDS
                // =================================================

                TextField shipIdField =
                        new TextField();

                shipIdField.setPromptText(
                        "Ship ID"
                );


                TextField shipNameField =
                        new TextField();

                shipNameField.setPromptText(
                        "Ship Name"
                );


                ComboBox<String> shipTypeField = new ComboBox<>();

                shipTypeField.getItems().addAll(
                        "Container",
                        "Tanker",
                        "Bulk Carrier",
                        "Cargo"
                );

                shipTypeField.setPromptText(
                        "Select Ship Type"
                );

                ComboBox<String> statusField = new ComboBox<>();

                statusField.getItems().addAll(
                        "Arriving",
                        "Docked",
                        "Departing"
                );

                statusField.setPromptText(
                        "Select Status"
                );


                TextField imoField =
                        new TextField();

                imoField.setPromptText(
                        "IMO Number"
                );


                TextField companyField =
                        new TextField();

                companyField.setPromptText(
                        "Company"
                );


                TextField arrivalField =
                        new TextField();

                arrivalField.setPromptText(
                        "Example: 08:20 · 15 Aug"
                );


                TextField departureField =
                        new TextField();

                departureField.setPromptText(
                        "Example: 16:40 · 15 Aug"
                );


                TextField berthField =
                        new TextField();

                berthField.setPromptText(
                        "Example: B-04"
                );


                // =================================================
                // FORM LAYOUT
                // =================================================

                GridPane form =
                        new GridPane();

                form.setHgap(10);

                form.setVgap(10);

                form.setPadding(
                        new Insets(20)
                );


                form.add(
                        new Label("Ship ID:"),
                        0,
                        0
                );

                form.add(
                        shipIdField,
                        1,
                        0
                );


                form.add(
                        new Label("Ship Name:"),
                        0,
                        1
                );

                form.add(
                        shipNameField,
                        1,
                        1
                );


                form.add(
                        new Label("Ship Type:"),
                        0,
                        2
                );

                form.add(
                        shipTypeField,
                        1,
                        2
                );

                form.add(
                        new Label("Status:"),
                        0,
                        3
                );

                form.add(
                        statusField,
                        1,
                        3
                );


                form.add(
                        new Label("IMO Number:"),
                        0,
                        4
                );

                form.add(
                        imoField,
                        1,
                        4
                );


                form.add(
                        new Label("Company:"),
                        0,
                        5
                );


                form.add(
                        companyField,
                        1,
                        5
                );


                form.add(
                        new Label("Arrival:"),
                        0,
                        6
                );

                form.add(
                        arrivalField,
                        1,
                        6
                );


                form.add(
                        new Label("Departure:"),
                        0,
                        7
                );

                form.add(
                        departureField,
                        1,
                        7
                );


                form.add(
                        new Label("Berth:"),
                        0,
                        8
                );

                form.add(
                        berthField,
                        1,
                        8
                );


                dialog.getDialogPane()
                        .setContent(form);


                // =================================================
                // DIALOG BUTTONS
                // =================================================

                ButtonType registerType =
                        new ButtonType(
                                "Register Ship",
                                ButtonBar.ButtonData.OK_DONE
                        );

                dialog.getDialogPane()
                        .getButtonTypes()
                        .addAll(
                                registerType,
                                ButtonType.CANCEL
                        );


                // =================================================
                // REGISTER BUTTON ACTION
                // =================================================

                Button registerDialogButton =
                        (Button) dialog.getDialogPane()
                                .lookupButton(registerType);


                registerDialogButton.setOnAction(event -> {

                        // =============================================
                        // READ INPUT
                        // =============================================

                        String shipId =
                                shipIdField.getText().trim();

                        String shipName =
                                shipNameField.getText().trim();

                        String shipType =
                                shipTypeField.getValue();

                        String status =
                                statusField.getValue();

                        String imo =
                                imoField.getText().trim();

                        String company =
                                companyField.getText().trim();

                        String arrival =
                                arrivalField.getText().trim();

                        String departure =
                                departureField.getText().trim();

                        String berth =
                                berthField.getText().trim();


                        // =============================================
                        // VALIDATION
                        // =============================================

                        if (shipId.isEmpty()
                                || shipName.isEmpty()
                                || shipType == null
                                || status == null
                                || imo.isEmpty()
                                || company.isEmpty()
                                || arrival.isEmpty()
                                || berth.isEmpty()) {

                        Alert alert =
                                new Alert(
                                        Alert.AlertType.WARNING
                                );

                        alert.setTitle(
                                "Incomplete Information"
                        );

                        alert.setHeaderText(
                                "Missing Information"
                        );

                        alert.setContentText(
                                "Please fill all required fields."
                        );

                        alert.showAndWait();

                        event.consume();

                        return;
                        }


                        // =============================================
                        // CREATE SHIP OBJECT
                        // =============================================

                        Ship ship = new Ship(
                                shipId,
                                shipName,
                                shipType,
                                imo,
                                company,
                                arrival,
                                departure.isEmpty()
                                        ? "—"
                                        : departure,
                                berth,
                                status
                        );


                        // =============================================
                        // SAVE SHIP TO FIRESTORE
                        // =============================================

                        shipController.saveShip(ship);

                        // =====================================================
                        // CREATE NOTIFICATION
                        // =====================================================

                        notificationController.createNotification(

                                "Ship registered",

                                ship.getShipName() + " has been registered successfully.",

                                "ADD"
                        );


                        // =============================================
                        // ADD SHIP TO TABLE
                        // =============================================

                        String[] newShip = {

                                ship.getShipId(),
                                ship.getShipName(),
                                ship.getShipType(),
                                ship.getImoNumber(),
                                ship.getCompany(),
                                ship.getArrival(),
                                ship.getDeparture(),
                                ship.getBerth(),
                                ship.getStatus()
                        };

                        shipData.add(newShip);


                        // =============================================
                        // REFRESH FILTERED TABLE
                        // =============================================

                        filteredData.setPredicate(
                                filteredData.getPredicate()
                        );


                        // =============================================
                        // SUCCESS MESSAGE
                        // =============================================

                        Alert alert =
                                new Alert(
                                        Alert.AlertType.INFORMATION
                                );

                        alert.setTitle(
                                "Ship Registered"
                        );

                        alert.setHeaderText(
                                "Registration Successful"
                        );

                        alert.setContentText(
                                shipName +
                                " has been registered successfully."
                        );

                        alert.showAndWait();

                });


                // =================================================
                // SHOW DIALOG
                // =================================================

                dialog.showAndWait();
        }

        // =====================================================
        // EDIT SHIP
        // =====================================================

        private void showEditShipDialog(
                String[] ship, TableView<String[]> table) {

                Dialog<ButtonType> dialog =
                        new Dialog<>();

                dialog.setTitle(
                        "Edit Ship"
                );

                dialog.setHeaderText(
                        "Edit Ship Details"
                );


                // =================================================
                // FORM FIELDS
                // =================================================

                TextField shipIdField =
                        new TextField(ship[0]);

                TextField shipNameField =
                        new TextField(ship[1]);

                ComboBox<String> shipTypeField =
                        new ComboBox<>();

                shipTypeField.getItems().addAll(
                        "Container",
                        "Tanker",
                        "Bulk Carrier",
                        "Cargo"
                );

                shipTypeField.setValue(
                        ship[2]
                );

                ComboBox<String> statusField =
                        new ComboBox<>();

                statusField.getItems().addAll(
                        "Arriving",
                        "Docked",
                        "Departing"
                );

                statusField.setValue(
                        ship[8]
                );


                TextField imoField =
                        new TextField(ship[3]);

                TextField companyField =
                        new TextField(ship[4]);

                TextField arrivalField =
                        new TextField(ship[5]);

                TextField departureField =
                        new TextField(ship[6]);

                TextField berthField =
                        new TextField(ship[7]);


                // =================================================
                // FORM
                // =================================================

                GridPane form =
                        new GridPane();

                form.setHgap(10);

                form.setVgap(10);

                form.setPadding(
                        new Insets(20)
                );


                form.add(
                        new Label("Ship ID:"),
                        0,
                        0
                );

                form.add(
                        shipIdField,
                        1,
                        0
                );


                form.add(
                        new Label("Ship Name:"),
                        0,
                        1
                );

                form.add(
                        shipNameField,
                        1,
                        1
                );


                form.add(
                        new Label("Ship Type:"),
                        0,
                        2
                );

                form.add(
                        shipTypeField,
                        1,
                        2
                );

                form.add(
                        new Label("Status:"),
                        0,
                        3
                );

                form.add(
                        statusField,
                        1,
                        3
                );


                form.add(
                        new Label("IMO Number:"),
                        0,
                        4
                );

                form.add(
                        imoField,
                        1,
                        4
                );


                form.add(
                        new Label("Company:"),
                        0,
                        5
                );

                form.add(
                        companyField,
                        1,
                        5
                );


                form.add(
                        new Label("Arrival:"),
                        0,
                        6
                );

                form.add(
                        arrivalField,
                        1,
                        6
                );


                form.add(
                        new Label("Departure:"),
                        0,
                        7
                );

                form.add(
                        departureField,
                        1,
                        7
                );


                form.add(
                        new Label("Berth:"),
                        0,
                        8
                );

                form.add(
                        berthField,
                        1,
                        8
                );


                dialog.getDialogPane()
                        .setContent(form);


                // =================================================
                // BUTTONS
                // =================================================

                ButtonType saveType =
                        new ButtonType(
                                "Save Changes",
                                ButtonBar.ButtonData.OK_DONE
                        );

                dialog.getDialogPane()
                        .getButtonTypes()
                        .addAll(
                                saveType,
                                ButtonType.CANCEL
                        );


                // =================================================
                // SAVE BUTTON
                // =================================================

                Button saveButton =
                        (Button) dialog.getDialogPane()
                                .lookupButton(saveType);


                saveButton.setOnAction(event -> {

                        // =============================================
                        // CREATE UPDATED SHIP OBJECT
                        // =============================================

                        Ship updatedShip = new Ship(
                                shipIdField.getText().trim(),
                                shipNameField.getText().trim(),
                                shipTypeField.getValue(),
                                imoField.getText().trim(),
                                companyField.getText().trim(),
                                arrivalField.getText().trim(),
                                departureField.getText().trim(),
                                berthField.getText().trim(),
                                statusField.getValue()
                        );


                        // =============================================
                        // UPDATE FIRESTORE
                        // =============================================

                        shipController.updateShip(updatedShip);

                        // =====================================================
                        // CREATE NOTIFICATION
                        // =====================================================

                        notificationController.createNotification(

                                "Ship record updated",

                                updatedShip.getShipName() + " was updated successfully.",

                                "EDIT"
                        );


                        // =============================================
                        // UPDATE TABLE ROW
                        // =============================================

                        ship[0] = updatedShip.getShipId();
                        ship[1] = updatedShip.getShipName();
                        ship[2] = updatedShip.getShipType();
                        ship[3] = updatedShip.getImoNumber();
                        ship[4] = updatedShip.getCompany();
                        ship[5] = updatedShip.getArrival();
                        ship[6] = updatedShip.getDeparture();
                        ship[7] = updatedShip.getBerth();
                        ship[8] = updatedShip.getStatus();

                        // =============================================
                        // REFRESH TABLE
                        // =============================================

                        table.refresh();
                        

                        Alert alert =
                                new Alert(
                                        Alert.AlertType.INFORMATION
                                );

                        alert.setTitle(
                                "Ship Updated"
                        );

                        alert.setHeaderText(
                                "Update Successful"
                        );

                        alert.setContentText(
                                ship[1] +
                                " has been updated successfully."
                        );

                        alert.showAndWait();
                });


                // =================================================
                // SHOW DIALOG
                // =================================================

                dialog.showAndWait();
        }

        // =====================================================
        // DELETE SHIP CONFIRMATION
        // =====================================================

        private void showDeleteConfirmation(
                String[] ship,
                TableView<String[]> table) {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle(
                "Delete Ship"
        );

        alert.setHeaderText(
                "Delete Ship?"
        );

        alert.setContentText(
                "Are you sure you want to delete " +
                ship[1] +
                " (" +
                ship[0] +
                ")?"
        );

        ButtonType deleteButton =
                new ButtonType(
                        "Delete",
                        ButtonBar.ButtonData.OK_DONE
                );

        ButtonType cancelButton =
                ButtonType.CANCEL;

        alert.getButtonTypes().setAll(
                deleteButton,
                cancelButton
        );

        alert.showAndWait().ifPresent(response -> {

                if (response == deleteButton) {

                // =============================================
                // DELETE FROM FIRESTORE
                // =============================================

                shipController.deleteShip(ship[0]);

                // =============================================
                // CREATE NOTIFICATION
                // =============================================

                notificationController.createNotification(

                        "Ship deleted",

                        ship[1]
                                +
                                " was deleted from the system.",

                        "DELETE"
                );

                // =============================================
                // REMOVE FROM TABLE
                // =============================================

                FilteredList<String[]> filteredList = (FilteredList<String[]>) table.getItems();

                filteredList.getSource().remove(ship);

                // =====================================================
                // REFRESH STATISTIC CARDS
                // =====================================================

                refreshStatistics();

                // =============================================
                // SUCCESS MESSAGE
                // =============================================

                Alert success =
                        new Alert(
                                Alert.AlertType.INFORMATION
                        );

                success.setTitle(
                        "Ship Deleted"
                );

                success.setHeaderText(
                        "Deletion Successful"
                );

                success.setContentText(
                        ship[1] +
                        " has been deleted successfully."
                );

                success.showAndWait();
                }
        });
        }

        // =====================================================
        // STYLE NORMAL TABLE COLUMN
        // =====================================================

        private void styleTableColumn(
                        TableColumn<String[], String> column
                ) {

                column.setCellFactory(
                        col -> new TableCell<String[], String>() {

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

                                        setText(null);

                                        return;
                                }


                                setText(
                                        item
                                );


                                setStyle(
                                        "-fx-background-color:white;" +
                                        "-fx-text-fill:#334155;" +
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
    // STATISTIC CARD
    // =====================================================

    private VBox createStatCard(
        String iconText,
        String titleText,
        String valueText,
        String iconBackground,
        String iconColor) {

        VBox card =
                new VBox(5);

        card.setPadding(
                new Insets(14)
        );

        card.setPrefHeight(
                88
        );

        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:10;" +
                "-fx-border-color:#e5ebf3;" +
                "-fx-border-radius:10;"
        );

        // =====================================================
        // TOP ROW
        // =====================================================

        HBox topRow =
                new HBox();

        topRow.setAlignment(
                Pos.CENTER_LEFT
        );

        Label title =
                new Label(
                        titleText
                );

        title.setStyle(
                "-fx-text-fill:#8a93a1;" +
                "-fx-font-size:10;"
        );

        VBox space =
                new VBox();

        HBox.setHgrow(
                space,
                Priority.ALWAYS
        );

        // =====================================================
        // ICON
        // =====================================================

        Label icon =
                new Label(
                        iconText
                );

        icon.setMinSize(
                34,
                34
        );

        icon.setAlignment(
                Pos.CENTER
        );

        icon.setStyle(
                "-fx-background-color:" +
                iconBackground + ";" +
                "-fx-background-radius:9;" +
                "-fx-text-fill:" +
                iconColor + ";" +
                "-fx-font-size:15;"
        );

        topRow.getChildren().addAll(
                title,
                space,
                icon
        );

        // =====================================================
        // VALUE
        // =====================================================

        Label value = new Label(valueText);


        if (titleText.equals("Total Ships")) {
        totalShipsValue = value;
        }
        else if (titleText.equals("Arriving")) {
        arrivingValue = value;
        }
        else if (titleText.equals("Docked")) {
        dockedValue = value;
        }
        else if (titleText.equals("Departing")) {
        departingValue = value;
        }

        value.setStyle(
                "-fx-text-fill:#111827;" +
                "-fx-font-size:20;" +
                "-fx-font-weight:bold;"
        );

        card.getChildren().addAll(
                topRow,
                value
        );

        return card;
    }

    //==============================
    //referesh Statistics

    private void refreshStatistics() {

        List<Ship> ships =
                shipController.getShips();

        int totalCount = ships.size();
        int arrivingCount = 0;
        int dockedCount = 0;
        int departingCount = 0;

        for (Ship ship : ships) {

                if ("Arriving".equals(ship.getStatus())) {
                arrivingCount++;
                }

                if ("Docked".equals(ship.getStatus())) {
                dockedCount++;
                }

                if ("Departing".equals(ship.getStatus())) {
                departingCount++;
                }
        }

        totalShipsValue.setText(
                String.valueOf(totalCount)
        );

        arrivingValue.setText(
                String.valueOf(arrivingCount)
        );

        dockedValue.setText(
                String.valueOf(dockedCount)
        );

        departingValue.setText(
                String.valueOf(departingCount)
        );
    }

        // =====================================================
        // VIEW SHIP DETAILS
        // =====================================================

        private void showShipDetails(String[] ship) {

                Alert alert =
                        new Alert(
                                Alert.AlertType.INFORMATION
                        );

                alert.setTitle(
                        "Ship Details"
                );

                alert.setHeaderText(
                        "Ship Details"
                );

                alert.setContentText(

                        "Ship ID: " + ship[0] + "\n" +

                        "Ship Name: " + ship[1] + "\n" +

                        "Ship Type: " + ship[2] + "\n" +

                        "IMO Number: " + ship[3] + "\n" +

                        "Company: " + ship[4] + "\n" +

                        "Arrival: " + ship[5] + "\n" +

                        "Departure: " + ship[6] + "\n" +

                        "Berth: " + ship[7] + "\n" +

                        "Status: " + ship[8]
                );

                alert.showAndWait();
        }

    // =====================================================
    // GET ROOT
    // =====================================================

    public VBox getRoot() {

        return root;
    }
}
