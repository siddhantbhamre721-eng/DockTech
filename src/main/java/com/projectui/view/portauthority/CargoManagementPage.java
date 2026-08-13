package com.projectui.view.portauthority;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CargoManagementPage {

    private VBox content;

    public CargoManagementPage() {

        // =====================================================
        // MAIN CONTENT
        // =====================================================

        content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: #f4f8ff;");

        // =====================================================
        // PAGE TITLE
        // =====================================================

        Label title = new Label("Cargo Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        Label subtitle = new Label(
                "Monitor cargo received, loaded and dispatched through the port."
        );
        subtitle.setTextFill(Color.GRAY);

        VBox heading = new VBox(5);
        heading.getChildren().addAll(title, subtitle);


        // =====================================================
        // SUMMARY CARDS
        // =====================================================

        HBox summaryCards = new HBox(15);

        VBox totalCargo = createCard(
                "Total Cargo",
                "12,540",
                "Tons"
        );

        VBox received = createCard(
                "Received",
                "7,820",
                "Tons"
        );

        VBox dispatched = createCard(
                "Dispatched",
                "3,420",
                "Tons"
        );

        VBox pending = createCard(
                "Pending",
                "1,300",
                "Tons"
        );

        HBox.setHgrow(totalCargo, Priority.ALWAYS);
        HBox.setHgrow(received, Priority.ALWAYS);
        HBox.setHgrow(dispatched, Priority.ALWAYS);
        HBox.setHgrow(pending, Priority.ALWAYS);

        summaryCards.getChildren().addAll(
                totalCargo,
                received,
                dispatched,
                pending
        );


        // =====================================================
        // SEARCH + FILTERS + ADD CARGO
        // =====================================================

        TextField searchField = new TextField();
        searchField.setPromptText("Search Cargo...");
        searchField.setPrefWidth(250);

        ComboBox<String> cargoTypeFilter = new ComboBox<>();
        cargoTypeFilter.getItems().addAll(
                "All Cargo Types",
                "Container",
                "Bulk",
                "Refrigerated",
                "Liquid"
        );
        cargoTypeFilter.setValue("All Cargo Types");
        cargoTypeFilter.setPrefWidth(180);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll(
                "All Status",
                "Received",
                "Loading",
                "Loaded",
                "In Transit",
                "Delivered",
                "Pending Inspection"
        );
        statusFilter.setValue("All Status");
        statusFilter.setPrefWidth(160);

        Button addCargoButton = new Button("+ Add Cargo");
        addCargoButton.setPrefWidth(120);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox filters = new HBox(
                12,
                searchField,
                cargoTypeFilter,
                statusFilter,
                spacer,
                addCargoButton
        );

        filters.setAlignment(Pos.CENTER_LEFT);


        // =====================================================
        // CARGO TABLE
        // =====================================================

        TableView<String[]> table = new TableView<>();

        table.setPrefHeight(500);

        TableColumn<String[], String> cargoId =
                createColumn("Cargo ID", 100, 0);

        TableColumn<String[], String> ship =
                createColumn("Ship", 130, 1);

        TableColumn<String[], String> cargoType =
                createColumn("Cargo Type", 120, 2);

        TableColumn<String[], String> weight =
                createColumn("Weight", 100, 3);

        TableColumn<String[], String> origin =
                createColumn("Origin", 110, 4);

        TableColumn<String[], String> destination =
                createColumn("Destination", 120, 5);

        TableColumn<String[], String> containers =
                createColumn("Containers", 100, 6);

        TableColumn<String[], String> status =
                createColumn("Status", 130, 7);

        table.getColumns().addAll(
                cargoId,
                ship,
                cargoType,
                weight,
                origin,
                destination,
                containers,
                status
        );


        // =====================================================
        // SAMPLE CARGO DATA
        // =====================================================

        ObservableList<String[]> cargoData =
                FXCollections.observableArrayList();

        cargoData.addAll(

                new String[]{
                        "CGO-1287",
                        "Oceanic Star",
                        "Container",
                        "1,240 T",
                        "Rotterdam",
                        "Singapore",
                        "18",
                        "Received"
                },

                new String[]{
                        "CGO-1288",
                        "Nordic Wave",
                        "Bulk",
                        "2,860 T",
                        "Hamburg",
                        "Mumbai",
                        "32",
                        "Loading"
                },

                new String[]{
                        "CGO-1289",
                        "Pacific Crown",
                        "Refrigerated",
                        "640 T",
                        "Shanghai",
                        "Los Angeles",
                        "9",
                        "Loaded"
                },

                new String[]{
                        "CGO-1290",
                        "Atlantic Voyager",
                        "Container",
                        "1,980 T",
                        "Antwerp",
                        "Dubai",
                        "24",
                        "In Transit"
                },

                new String[]{
                        "CGO-1291",
                        "Coral Mariner",
                        "Liquid",
                        "3,150 T",
                        "Houston",
                        "Yokohama",
                        "0",
                        "Delivered"
                },

                new String[]{
                        "CGO-1292",
                        "Sapphire Dawn",
                        "Bulk",
                        "2,410 T",
                        "Busan",
                        "Chennai",
                        "28",
                        "Pending Inspection"
                },

                new String[]{
                        "CGO-1293",
                        "Meridian Bay",
                        "Container",
                        "1,520 T",
                        "Valencia",
                        "Colombo",
                        "21",
                        "Loaded"
                },

                new String[]{
                        "CGO-1294",
                        "Golden Horizon",
                        "Refrigerated",
                        "780 T",
                        "Felixstowe",
                        "Jeddah",
                        "12",
                        "Received"
                }
        );


        // =====================================================
        // TABLE CELL VALUES
        // =====================================================

        cargoId.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue()[0]
                )
        );

        ship.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue()[1]
                )
        );

        cargoType.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue()[2]
                )
        );

        weight.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue()[3]
                )
        );

        origin.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue()[4]
                )
        );

        destination.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue()[5]
                )
        );

        containers.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue()[6]
                )
        );

        status.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue()[7]
                )
        );


        // =====================================================
        // SEARCH FUNCTION
        // =====================================================

        FilteredList<String[]> filteredData =
                new FilteredList<>(cargoData);

        table.setItems(filteredData);

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    String searchText =
                            newValue.toLowerCase();

                    filteredData.setPredicate(cargo -> {

                        for (String value : cargo) {

                            if (value.toLowerCase()
                                    .contains(searchText)) {

                                return true;
                            }
                        }

                        return false;
                    });
                }
        );


        // =====================================================
        // FILTER - CARGO TYPE
        // =====================================================

        cargoTypeFilter.setOnAction(event -> {

            String selected =
                    cargoTypeFilter.getValue();

            filteredData.setPredicate(cargo -> {

                boolean searchMatch = true;

                String searchText =
                        searchField.getText().toLowerCase();

                if (!searchText.isEmpty()) {

                    searchMatch = false;

                    for (String value : cargo) {

                        if (value.toLowerCase()
                                .contains(searchText)) {

                            searchMatch = true;
                            break;
                        }
                    }
                }

                boolean typeMatch =
                        selected.equals("All Cargo Types")
                        || cargo[2].equals(selected);

                String selectedStatus =
                        statusFilter.getValue();

                boolean statusMatch =
                        selectedStatus.equals("All Status")
                        || cargo[7].equals(selectedStatus);

                return searchMatch
                        && typeMatch
                        && statusMatch;
            });
        });


        // =====================================================
        // FILTER - STATUS
        // =====================================================

        statusFilter.setOnAction(event -> {

            String selected =
                    statusFilter.getValue();

            filteredData.setPredicate(cargo -> {

                boolean searchMatch = true;

                String searchText =
                        searchField.getText().toLowerCase();

                if (!searchText.isEmpty()) {

                    searchMatch = false;

                    for (String value : cargo) {

                        if (value.toLowerCase()
                                .contains(searchText)) {

                            searchMatch = true;
                            break;
                        }
                    }
                }

                String selectedType =
                        cargoTypeFilter.getValue();

                boolean typeMatch =
                        selectedType.equals("All Cargo Types")
                        || cargo[2].equals(selectedType);

                boolean statusMatch =
                        selected.equals("All Status")
                        || cargo[7].equals(selected);

                return searchMatch
                        && typeMatch
                        && statusMatch;
            });
        });


        // =====================================================
        // ADD EVERYTHING
        // =====================================================

        content.getChildren().addAll(
                heading,
                summaryCards,
                filters,
                table
        );
    }


    // =====================================================
    // CREATE SUMMARY CARD
    // =====================================================

    private VBox createCard(
            String title,
            String value,
            String unit
    ) {

        Label titleLabel = new Label(title);

        titleLabel.setTextFill(Color.GRAY);

        Label valueLabel =
                new Label(value + " " + unit);

        valueLabel.setFont(
                Font.font("Arial", FontWeight.BOLD, 24)
        );

        VBox card =
                new VBox(10);

        card.setPadding(
                new Insets(15)
        );

        card.setPrefHeight(100);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #d9e2f0;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );

        card.getChildren().addAll(
                titleLabel,
                valueLabel
        );

        return card;
    }


    // =====================================================
    // CREATE TABLE COLUMN
    // =====================================================

    private TableColumn<String[], String> createColumn(
            String title,
            double width,
            int index
    ) {

        TableColumn<String[], String> column =
                new TableColumn<>(title);

        column.setPrefWidth(width);

        column.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue()[index]
                )
        );

        return column;
    }


    // =====================================================
    // GET PAGE CONTENT
    // =====================================================

    public VBox getContent() {
        return content;
    }
}