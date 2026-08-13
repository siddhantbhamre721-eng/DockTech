package com.projectui.view.portauthority;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

public class ShipManagementPage {

    private final VBox root =
            new VBox(18);

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
                        "42",
                        "#eaf2ff",
                        "#0f5fff"
                );

        VBox arriving =
                createStatCard(
                        "↪",
                        "Arriving",
                        "9",
                        "#e6f7f5",
                        "#008f83"
                );

        VBox docked =
                createStatCard(
                        "⚓",
                        "Docked",
                        "18",
                        "#e7f7ee",
                        "#168548"
                );

        VBox departing =
                createStatCard(
                    "↗",
                    "Departing",
                    "15",
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
                "-fx-border-color:#e5ebf3;" +
                "-fx-border-radius:7;" +
                "-fx-background-radius:7;" +
                "-fx-font-size:11;"
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

        typeFilter.setPrefHeight(
                36
        );

        typeFilter.setPrefWidth(
                165
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

        statusFilter.setPrefHeight(
                36
        );

        statusFilter.setPrefWidth(
                150
        );

        // DATE FILTER

        ComboBox<String> dateFilter =
                new ComboBox<>();

        dateFilter.getItems().addAll(
                "All Dates",
                "Today",
                "Tomorrow",
                "This Week"
        );

        dateFilter.setValue(
                "All Dates"
        );

        dateFilter.setPrefHeight(
                36
        );

        dateFilter.setPrefWidth(
                150
        );

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
                dateFilter,
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

            TableRow<String[]> row = new TableRow<>();

            row.setPrefHeight(
                    34
            );

            return row;
        });

        // =====================================================
        // TABLE STYLE
        // =====================================================

        table.setStyle(
            "-fx-background-color:white;" +
            "-fx-border-color:transparent;" +
            "-fx-table-cell-border-color: #e5e7eb;" +
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

        table.getColumns().addAll(
                idColumn,
                nameColumn,
                typeColumn,
                imoColumn,
                companyColumn,
                arrivalColumn,
                departureColumn,
                berthColumn
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

        table.skinProperty().addListener((observable, oldSkin, newSkin) -> {

            if (newSkin != null) {

                javafx.scene.Node header =
                        table.lookup(".column-header-background");

                if (header != null) {

                    header.setStyle(
                            "-fx-background-color:#edf4fa;"
                    );
                }
            }
        });

        // =====================================================
        // SAMPLE SHIP DATA
        // =====================================================


        ObservableList<String[]> shipData = FXCollections.observableArrayList();

        shipData.addAll(

                new String[]{
                        "SHP-1042",
                        "Oceanic Star",
                        "Container",
                        "9321483",
                        "Maersk Line",
                        "08:20 · 12 Jun",
                        "16:40 · 14 Jun",
                        "B-04"
                },

                new String[]{
                        "SHP-1043",
                        "Neptune Voyager",
                        "Tanker",
                        "9457821",
                        "Evergreen Marine",
                        "14:10 · 13 Jun",
                        "—",
                        "B-09"
                },

                new String[]{
                        "SHP-1044",
                        "Pacific Horizon",
                        "Bulk Carrier",
                        "9188047",
                        "MSC Group",
                        "—",
                        "—",
                        "Anchor"
                },

                new String[]{
                        "SHP-1045",
                        "Atlantic Pride",
                        "Container",
                        "9502764",
                        "CMA CGM",
                        "06:45 · 11 Jun",
                        "09:30 · 13 Jun",
                        "B-02"
                },

                new String[]{
                        "SHP-1046",
                        "Coral Enterprise",
                        "Cargo",
                        "9366210",
                        "Hapag-Lloyd",
                        "10:15 · 12 Jun",
                        "—",
                        "B-07"
                },

                new String[]{
                        "SHP-1047",
                        "Meridian Bay",
                        "Tanker",
                        "9418337",
                        "COSCO Shipping",
                        "18:00 · 14 Jun",
                        "—",
                        "B-11"
                },

                new String[]{
                        "SHP-1048",
                        "Northern Wind",
                        "Bulk Carrier",
                        "9273091",
                        "Yang Ming",
                        "—",
                        "—",
                        "Anchor"
                },

                new String[]{
                        "SHP-1049",
                        "Southern Cross",
                        "Container",
                        "9541128",
                        "ONE Line",
                        "07:30 · 10 Jun",
                        "21:15 · 12 Jun",
                        "B-05"
                }
        );

        FilteredList<String[]> filteredData = new FilteredList<>(shipData);
        table.setItems(filteredData);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {

            String searchText = newValue.toLowerCase().trim();

            filteredData.setPredicate(ship -> {

                for (String value : ship) {

                if (value.toLowerCase().contains(searchText)) {
                    return true;
                }
            }

            return false;
            });
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

        Label value =
                new Label(
                        valueText
                );

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

    // =====================================================
    // GET ROOT
    // =====================================================

    public VBox getRoot() {

        return root;
    }
}
