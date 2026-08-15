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
                
        
        TableColumn<String[], String> action =
                new TableColumn<>("Action");

        action.setPrefWidth(120);

        action.setCellFactory(column -> new TableCell<>() {

                private final Button viewButton = new Button("View");
                private final Button editButton = new Button("Edit");

                {
                        viewButton.setOnAction(e -> {

                        String[] cargo = getTableView().getItems().get(getIndex());

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);

                        alert.setTitle("Cargo Details");
                        alert.setHeaderText("Cargo Details");

                        alert.setContentText(
                                "Cargo ID: " + cargo[0] + "\n" +
                                "Ship: " + cargo[1] + "\n" +
                                "Cargo Type: " + cargo[2] + "\n" +
                                "Weight: " + cargo[3] + "\n" +
                                "Origin: " + cargo[4] + "\n" +
                                "Destination: " + cargo[5] + "\n" +
                                "Containers: " + cargo[6] + "\n" +
                                "Status: " + cargo[7]
                        );

                        alert.showAndWait();
                        });

                        editButton.setOnAction(e -> {

                                String[] cargo = getTableView().getItems().get(getIndex());

                                Dialog<ButtonType> dialog = new Dialog<>();

                                dialog.setTitle("Edit Cargo");
                                dialog.setHeaderText("Edit Cargo Details");

                                ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                                dialog.getDialogPane().getButtonTypes().addAll(
                                        okButton,
                                        cancelButton
                                );

                                GridPane grid = new GridPane();
                                grid.setHgap(10);
                                grid.setVgap(10);
                                grid.setPadding(new Insets(20, 20, 10, 20));

                                TextField shipField = new TextField(cargo[1]);
                                ComboBox<String> cargoTypeBox = new ComboBox<>();

                                cargoTypeBox.getItems().addAll(
                                        "Container",
                                        "Bulk",
                                        "Refrigerated",
                                        "Liquid"
                                );

                                cargoTypeBox.setValue(cargo[2]);

                                TextField weightField = new TextField(cargo[3]);
                                TextField originField = new TextField(cargo[4]);
                                TextField destinationField = new TextField(cargo[5]);
                                TextField containersField = new TextField(cargo[6]);

                                ComboBox<String> statusBox = new ComboBox<>();

                                statusBox.getItems().addAll(
                                        "Received",
                                        "Loading",
                                        "Loaded",
                                        "In Transit",
                                        "Delivered",
                                        "Pending Inspection"
                                );

                                statusBox.setValue(cargo[7]);

                                grid.add(new Label("Ship:"), 0, 0);
                                grid.add(shipField, 1, 0);

                                grid.add(new Label("Cargo Type:"), 0, 1);
                                grid.add(cargoTypeBox, 1, 1);

                                grid.add(new Label("Weight:"), 0, 2);
                                grid.add(weightField, 1, 2);

                                grid.add(new Label("Origin:"), 0, 3);
                                grid.add(originField, 1, 3);

                                grid.add(new Label("Destination:"), 0, 4);
                                grid.add(destinationField, 1, 4);

                                grid.add(new Label("Containers:"), 0, 5);
                                grid.add(containersField, 1, 5);

                                grid.add(new Label("Status:"), 0, 6);
                                grid.add(statusBox, 1, 6);

                                dialog.getDialogPane().setContent(grid);

                                dialog.setResultConverter(button -> {

                                        if (button == okButton) {

                                        cargo[1] = shipField.getText().trim();
                                        cargo[2] = cargoTypeBox.getValue();
                                        cargo[3] = weightField.getText().trim();
                                        cargo[4] = originField.getText().trim();
                                        cargo[5] = destinationField.getText().trim();
                                        cargo[6] = containersField.getText().trim();
                                        cargo[7] = statusBox.getValue();

                                        table.refresh();
                                        }

                                        return button;
                                });

                                dialog.showAndWait();
                        });
                }

                @Override
                protected void updateItem(String item, boolean empty) {

                        super.updateItem(item, empty);

                        if (empty) {

                                setGraphic(null);

                        } else {

                                HBox buttons = new HBox(8);

                                buttons.getChildren().addAll(
                                        viewButton,
                                        editButton
                                );

                                setGraphic(buttons);
                        }
                }
        });

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

        // =====================================================
        // ADD CARGO BUTTON
        // =====================================================

        addCargoButton.setOnAction(event -> {

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Add Cargo");
        dialog.setHeaderText("Register New Cargo");

        // -------------------------------------------------
        // FORM FIELDS
        // -------------------------------------------------

        TextField shipField = new TextField();
        shipField.setPromptText("Ship name");

        ComboBox<String> cargoTypeField = new ComboBox<>();

        cargoTypeField.getItems().addAll(
                "Container",
                "Bulk",
                "Refrigerated",
                "Liquid"
        );

        cargoTypeField.setPromptText("Cargo type");

        TextField weightField = new TextField();
        weightField.setPromptText("e.g. 1500 T");

        TextField originField = new TextField();
        originField.setPromptText("Origin");

        TextField destinationField = new TextField();
        destinationField.setPromptText("Destination");

        TextField containersField = new TextField();
        containersField.setPromptText("Number of containers");

        ComboBox<String> statusField = new ComboBox<>();

        statusField.getItems().addAll(
                "Received",
                "Loading",
                "Loaded",
                "In Transit",
                "Delivered",
                "Pending Inspection"
        );

        statusField.setPromptText("Status");


        // -------------------------------------------------
        // FORM LAYOUT
        // -------------------------------------------------

        GridPane form = new GridPane();

        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(15));

        form.add(new Label("Ship:"), 0, 0);
        form.add(shipField, 1, 0);

        form.add(new Label("Cargo Type:"), 0, 1);
        form.add(cargoTypeField, 1, 1);

        form.add(new Label("Weight:"), 0, 2);
        form.add(weightField, 1, 2);

        form.add(new Label("Origin:"), 0, 3);
        form.add(originField, 1, 3);

        form.add(new Label("Destination:"), 0, 4);
        form.add(destinationField, 1, 4);

        form.add(new Label("Containers:"), 0, 5);
        form.add(containersField, 1, 5);

        form.add(new Label("Status:"), 0, 6);
        form.add(statusField, 1, 6);


        dialog.getDialogPane().setContent(form);

        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL
        );


        // -------------------------------------------------
        // HANDLE ADD
        // -------------------------------------------------

        dialog.setResultConverter(button -> {

                if (button == ButtonType.OK) {

                        String newShip =
                                shipField.getText().trim();

                        String newCargoType =
                                cargoTypeField.getValue();

                        String newWeight =
                                weightField.getText().trim();

                        String newOrigin =
                                originField.getText().trim();

                        String newDestination =
                                destinationField.getText().trim();

                        String newContainers =
                                containersField.getText().trim();

                        String newStatus =
                                statusField.getValue();


                        // Basic validation
                        if (newShip.isEmpty()
                                        || newCargoType == null
                                        || newWeight.isEmpty()
                                        || newOrigin.isEmpty()
                                        || newDestination.isEmpty()
                                        || newContainers.isEmpty()
                                        || newStatus == null) {

                                Alert alert = new Alert(
                                        Alert.AlertType.WARNING
                                );

                                alert.setTitle("Missing Information");
                                alert.setHeaderText(null);
                                alert.setContentText(
                                        "Please fill in all cargo details."
                                );

                                alert.showAndWait();

                                return null;
                        }


                        // Generate next Cargo ID
                        int nextId =
                                1295 + cargoData.size() - 8;

                        String cargoIdValue =
                                "CGO-" + nextId;


                        // Add new cargo to the original list
                        cargoData.add(
                                new String[]{
                                        cargoIdValue,
                                        newShip,
                                        newCargoType,
                                        newWeight,
                                        newOrigin,
                                        newDestination,
                                        newContainers,
                                        newStatus
                                }
                        );
                }

                return button;
        });

        dialog.showAndWait();
        });

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