package com.projectui.view.portauthority;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

public class ContainerManagementPage {

    private VBox content;

    private ObservableList<String[]> containerData =
                                        FXCollections.observableArrayList();

    public ContainerManagementPage() {

        // =====================================================
        // MAIN CONTENT
        // =====================================================

        content = new VBox();

        content.setSpacing(20);

        content.setPadding(
                new Insets(30)
        );

        content.setStyle(
                "-fx-background-color:#f4f8fc;"
        );


        // =====================================================
        // PAGE TITLE
        // =====================================================

        Label title =
                new Label(
                        "Container Management"
                );

        title.setStyle(
                "-fx-font-size:28px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#14213d;"
        );


        Label subtitle =
                new Label(
                        "Monitor container inventory, movement and current status."
                );

        subtitle.setStyle(
                "-fx-font-size:14px;" +
                "-fx-text-fill:#7d8da6;"
        );


        // =====================================================
        // SUMMARY CARDS
        // =====================================================

        HBox cards =
                new HBox();

        cards.setSpacing(15);


        VBox totalCard =
                createCard(
                        "TOTAL CONTAINERS",
                        "8,760",
                        "All registered units",
                        "#00a878"
                );


        VBox availableCard =
                createCard(
                        "AVAILABLE",
                        "1,850",
                        "Ready for allocation",
                        "#2779ff"
                );


        VBox allocatedCard =
                createCard(
                        "ALLOCATED",
                        "6,420",
                        "Assigned to cargo",
                        "#00a878"
                );


        VBox transitCard =
                createCard(
                        "IN TRANSIT",
                        "320",
                        "Currently moving",
                        "#ff8a00"
                );


        cards.getChildren().addAll(
                totalCard,
                availableCard,
                allocatedCard,
                transitCard
        );


        // =====================================================
        // CONTAINER STATUS DISTRIBUTION
        // =====================================================

        VBox statusBox =
                new VBox();

        statusBox.setSpacing(15);

        statusBox.setPadding(
                new Insets(20)
        );

        statusBox.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:10;" +
                "-fx-border-color:#dfe7f1;" +
                "-fx-border-radius:10;"
        );


        Label statusTitle =
                new Label(
                        "Container Status Distribution"
                );

        statusTitle.setStyle(
                "-fx-font-size:16px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#14213d;"
        );


        Label statusSubtitle =
                new Label(
                        "Breakdown of container inventory by current status"
                );

        statusSubtitle.setStyle(
                "-fx-font-size:12px;" +
                "-fx-text-fill:#7d8da6;"
        );


        // -----------------------------------------------------
        // STATUS BAR
        // -----------------------------------------------------

        HBox statusBar =
                new HBox();

        statusBar.setPrefHeight(20);


        HBox allocatedPart =
                createStatusPart(
                        "73%",
                        "#00b67a",
                        600
                );


        HBox availablePart =
                createStatusPart(
                        "21%",
                        "#2779ff",
                        175
                );


        HBox transitPart =
                createStatusPart(
                        "",
                        "#ff8a00",
                        30
                );


        HBox pendingPart =
                createStatusPart(
                        "",
                        "#9ca3af",
                        15
                );


        statusBar.getChildren().addAll(
                allocatedPart,
                availablePart,
                transitPart,
                pendingPart
        );


        // -----------------------------------------------------
        // STATUS VALUES
        // -----------------------------------------------------

        HBox statusValues =
                new HBox();

        statusValues.setSpacing(80);


        statusValues.getChildren().addAll(

                createStatusValue(
                        "6,420",
                        "Allocated",
                        "#00a878"
                ),

                createStatusValue(
                        "1,850",
                        "Available",
                        "#2779ff"
                ),

                createStatusValue(
                        "320",
                        "In Transit",
                        "#ff8a00"
                ),

                createStatusValue(
                        "170",
                        "Pending",
                        "#777777"
                )
        );


        statusBox.getChildren().addAll(
                statusTitle,
                statusSubtitle,
                statusBar,
                statusValues
        );


        // =====================================================
        // CONTAINER INVENTORY
        // =====================================================

        VBox inventoryBox =
                new VBox();

        inventoryBox.setSpacing(12);

        inventoryBox.setPadding(
                new Insets(20)
        );

        inventoryBox.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:10;" +
                "-fx-border-color:#dfe7f1;" +
                "-fx-border-radius:10;"
        );


        // -----------------------------------------------------
        // INVENTORY HEADER
        // -----------------------------------------------------

        HBox inventoryHeader =
                new HBox();

        inventoryHeader.setAlignment(
                Pos.CENTER_LEFT
        );


        VBox inventoryInfo =
                new VBox();

        inventoryInfo.setSpacing(3);


        Label inventoryTitle =
                new Label(
                        "Container Inventory"
                );

        inventoryTitle.setStyle(
                "-fx-font-size:16px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#14213d;"
        );


        Label inventorySubtitle =
                new Label(
                        "View and manage all containers in the port"
                );

        inventorySubtitle.setStyle(
                "-fx-font-size:12px;" +
                "-fx-text-fill:#7d8da6;"
        );


        inventoryInfo.getChildren().addAll(
                inventoryTitle,
                inventorySubtitle
        );


        // -----------------------------------------------------
        // SEARCH
        // -----------------------------------------------------

        TextField searchField =
                new TextField();

        searchField.setPromptText(
                "Search Container..."
        );

        searchField.setPrefWidth(
                200
        );


        // -----------------------------------------------------
        // STATUS FILTER
        // -----------------------------------------------------

        ComboBox<String> statusFilter =
                new ComboBox<>();

        statusFilter.getItems().addAll(
                "All Statuses",
                "Available",
                "Allocated",
                "In Transit",
                "Pending"
        );

        statusFilter.setValue(
                "All Statuses"
        );


        // -----------------------------------------------------
        // ADD CONTAINER BUTTON
        // -----------------------------------------------------

        Button addContainerButton =
                new Button(
                        "+ Add Container"
                );
        

        addContainerButton.setOnAction(e -> {

                Dialog<ButtonType> dialog =
                        new Dialog<>();

                dialog.setTitle("Add Container");
                dialog.setHeaderText(
                        "Register New Container"
                );


                // =====================================================
                // FORM FIELDS
                // =====================================================

                TextField cargoIdField =
                        new TextField();

                cargoIdField.setPromptText(
                        "Cargo ID"
                );


                TextField shipField =
                        new TextField();

                shipField.setPromptText(
                        "Ship"
                );


                TextField locationField =
                        new TextField();

                locationField.setPromptText(
                        "Location"
                );


                TextField sizeField =
                        new TextField();

                sizeField.setPromptText(
                        "Size (e.g. 40 ft)"
                );


                ComboBox<String> typeField =
                        new ComboBox<>();

                typeField.getItems().addAll(
                        "Dry Cargo",
                        "Reefer",
                        "Tank",
                        "Flat Rack"
                );

                typeField.setValue(
                        "Dry Cargo"
                );


                ComboBox<String> containerStatusField =
                        new ComboBox<>();

                containerStatusField.getItems().addAll(
                        "Available",
                        "Allocated",
                        "In Transit",
                        "Pending"
                );

                containerStatusField.setValue(
                        "Available"
                );


                // =====================================================
                // FORM
                // =====================================================

                VBox form =
                        new VBox(10);

                form.setPadding(
                        new Insets(20)
                );


                form.getChildren().addAll(

                        new Label("Container Type:"),
                        typeField,

                        new Label("Cargo ID:"),
                        cargoIdField,

                        new Label("Ship:"),
                        shipField,

                        new Label("Location:"),
                        locationField,

                        new Label("Size:"),
                        sizeField,

                        new Label("Status:"),
                        containerStatusField
                );


                dialog.getDialogPane().setContent(
                        form
                );


                // =====================================================
                // BUTTONS
                // =====================================================

                dialog.getDialogPane()
                        .getButtonTypes()
                        .addAll(
                                ButtonType.OK,
                                ButtonType.CANCEL
                        );


                // =====================================================
                // SHOW DIALOG
                // =====================================================

                Optional<ButtonType> result =
                        dialog.showAndWait();


                if (result.isPresent()
                        && result.get() == ButtonType.OK) {


                        String type =
                                typeField.getValue();

                        String cargoId =
                                cargoIdField.getText().trim();

                        String ship =
                                shipField.getText().trim();

                        String location =
                                locationField.getText().trim();

                        String size =
                                sizeField.getText().trim();

                        String containerStatus =
                                containerStatusField.getValue();

                        if (ship.isEmpty() ||
                                cargoId.isEmpty() ||
                                location.isEmpty() ||
                                size.isEmpty() ||
                                type == null ||
                                containerStatus == null) {

                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setTitle("Missing Information");
                                alert.setHeaderText("Incomplete Container Details");
                                alert.setContentText("Please fill in all the fields before adding the container.");
                                alert.showAndWait();

                                return;
                        }


                        // =================================================
                        // GENERATE CONTAINER ID
                        // =================================================

                        String containerId =
                                "CONT-" +
                                (1025 + containerData.size());


                        // =================================================
                        // ADD NEW CONTAINER
                        // =================================================

                        containerData.add(

                                new String[]{
                                        containerId,
                                        type,
                                        cargoId.isEmpty()
                                                ? "—"
                                                : cargoId,
                                        ship.isEmpty()
                                                ? "—"
                                                : ship,
                                        location,
                                        size,
                                        containerStatus
                                }
                        );
                }
        });


        VBox headerSpace =
                new VBox();

        HBox.setHgrow(
                headerSpace,
                javafx.scene.layout.Priority.ALWAYS
        );


        HBox controls =
                new HBox(
                        10,
                        searchField,
                        statusFilter,
                        addContainerButton
                );

        controls.setAlignment(
                Pos.CENTER_RIGHT
        );


        inventoryHeader.getChildren().addAll(
                inventoryInfo,
                headerSpace,
                controls
        );


        // =====================================================
        // CONTAINER TABLE
        // =====================================================

        TableView<String[]> table =
                new TableView<>();

        table.setPrefHeight(
                350
        );


        TableColumn<String[], String> containerId =
                createColumn(
                        "Container ID",
                        120,
                        0
                );


        TableColumn<String[], String> type =
                createColumn(
                        "Type",
                        110,
                        1
                );


        TableColumn<String[], String> cargoId =
                createColumn(
                        "Cargo ID",
                        100,
                        2
                );


        TableColumn<String[], String> ship =
                createColumn(
                        "Ship",
                        130,
                        3
                );


        TableColumn<String[], String> location =
                createColumn(
                        "Location",
                        120,
                        4
                );


        TableColumn<String[], String> size =
                createColumn(
                        "Size",
                        80,
                        5
                );


        TableColumn<String[], String> status =
                createColumn(
                        "Status",
                        120,
                        6
                );

        // =====================================================
        // ACTION COLUMN
        // =====================================================

        TableColumn<String[], String> action =
                new TableColumn<>("Action");

        action.setPrefWidth(150);

        action.setCellFactory(column -> new TableCell<String[], String>() {

        private final Button viewButton = new Button("View");
        private final Button editButton = new Button("Edit");

        {
                // =====================================================
                // VIEW BUTTON
                // =====================================================

                viewButton.setOnAction(event -> {

                String[] container =
                        getTableView().getItems().get(getIndex());

                Alert alert =
                        new Alert(Alert.AlertType.INFORMATION);

                alert.setTitle("Container Details");
                alert.setHeaderText("Container Details");

                alert.setContentText(
                        "Container ID: " + container[0] + "\n" +
                        "Type: " + container[1] + "\n" +
                        "Cargo ID: " + container[2] + "\n" +
                        "Ship: " + container[3] + "\n" +
                        "Location: " + container[4] + "\n" +
                        "Size: " + container[5] + "\n" +
                        "Status: " + container[6]
                );

                alert.showAndWait();
                });


                // =====================================================
                // EDIT BUTTON
                // =====================================================

                editButton.setOnAction(event -> {

                        String[] container =
                                getTableView().getItems().get(getIndex());

                        // =====================================================
                        // EDIT FIELDS
                        // =====================================================

                        TextField cargoIdField =
                                new TextField(container[2]);

                        TextField shipField =
                                new TextField(container[3]);

                        TextField locationField =
                                new TextField(container[4]);

                        TextField sizeField =
                                new TextField(container[5]);

                        ComboBox<String> typeBox =
                                new ComboBox<>();

                        typeBox.getItems().addAll(
                                "Dry Cargo",
                                "Reefer",
                                "Tank",
                                "Flat Rack"
                        );

                        typeBox.setValue(container[1]);


                        ComboBox<String> statusBox =
                                new ComboBox<>();

                        statusBox.getItems().addAll(
                                "Allocated",
                                "Available",
                                "In Transit",
                                "Pending"
                        );

                        statusBox.setValue(container[6]);


                        // =====================================================
                        // GRID
                        // =====================================================

                        GridPane grid =
                                new GridPane();

                        grid.setHgap(10);
                        grid.setVgap(10);

                        grid.add(new Label("Type:"), 0, 0);
                        grid.add(typeBox, 1, 0);

                        grid.add(new Label("Cargo ID:"), 0, 1);
                        grid.add(cargoIdField, 1, 1);

                        grid.add(new Label("Ship:"), 0, 2);
                        grid.add(shipField, 1, 2);

                        grid.add(new Label("Location:"), 0, 3);
                        grid.add(locationField, 1, 3);

                        grid.add(new Label("Size:"), 0, 4);
                        grid.add(sizeField, 1, 4);

                        grid.add(new Label("Status:"), 0, 5);
                        grid.add(statusBox, 1, 5);


                        // =====================================================
                        // DIALOG
                        // =====================================================

                        Dialog<ButtonType> dialog =
                                new Dialog<>();

                        dialog.setTitle("Edit Container");
                        dialog.setHeaderText("Edit Container Details");

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

                        dialog.getDialogPane()
                                .setContent(grid);


                        // =====================================================
                        // SAVE CHANGES
                        // =====================================================

                        dialog.setResultConverter(button -> {

                                if (button == okButton) {

                                String cargoId =
                                        cargoIdField.getText().trim();

                                String ship =
                                        shipField.getText().trim();

                                String location =
                                        locationField.getText().trim();

                                String size =
                                        sizeField.getText().trim();

                                String type =
                                        typeBox.getValue();

                                String status =
                                        statusBox.getValue();


                                // Validation

                                if (cargoId.isEmpty() ||
                                        ship.isEmpty() ||
                                        location.isEmpty() ||
                                        size.isEmpty() ||
                                        type == null ||
                                        status == null) {

                                        Alert alert =
                                                new Alert(
                                                        Alert.AlertType.WARNING
                                                );

                                        alert.setTitle("Missing Information");
                                        alert.setHeaderText(
                                                "Incomplete Container Details"
                                        );
                                        alert.setContentText(
                                                "Please fill in all the fields."
                                        );

                                        alert.showAndWait();

                                        return null;
                                }


                                // Update container

                                container[1] = type;
                                container[2] = cargoId;
                                container[3] = ship;
                                container[4] = location;
                                container[5] = size;
                                container[6] = status;

                                getTableView().refresh();
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

                HBox buttons =
                        new HBox(
                                5,
                                viewButton,
                                editButton
                        );

                setGraphic(buttons);
                }
        }
        });

        table.getColumns().addAll(
                containerId,
                type,
                cargoId,
                ship,
                location,
                size,
                status,
                action
        );


        // =====================================================
        // SAMPLE CONTAINER DATA
        // =====================================================



        containerData.addAll(

                new String[]{
                        "CONT-1024",
                        "Dry Cargo",
                        "CGO-1287",
                        "Oceanic Star",
                        "Terminal A",
                        "40 ft",
                        "Allocated"
                },

                new String[]{
                        "CONT-1058",
                        "Reefer",
                        "—",
                        "—",
                        "Yard B-12",
                        "20 ft",
                        "Available"
                },

                new String[]{
                        "CONT-1091",
                        "Tank",
                        "CGO-1342",
                        "Nordic Wave",
                        "Gate 4",
                        "40 ft",
                        "In Transit"
                },

                new String[]{
                        "CONT-1127",
                        "Dry Cargo",
                        "CGO-1298",
                        "Atlantic Pride",
                        "Terminal C",
                        "40 ft",
                        "Allocated"
                },

                new String[]{
                        "CONT-1163",
                        "Flat Rack",
                        "—",
                        "—",
                        "Yard A-04",
                        "20 ft",
                        "Available"
                },

                new String[]{
                        "CONT-1205",
                        "Reefer",
                        "CGO-1401",
                        "Pacific Dawn",
                        "Gate 2",
                        "40 ft",
                        "In Transit"
                },

                new String[]{
                        "CONT-1248",
                        "Dry Cargo",
                        "CGO-1355",
                        "Oceanic Star",
                        "Terminal A",
                        "20 ft",
                        "Allocated"
                }
        );


        FilteredList<String[]> filteredData =
        new FilteredList<>(
                containerData,
                data -> true
        );

        table.setItems( 
                filteredData
        );

        // =====================================================
        // SEARCH + STATUS FILTER
        // =====================================================

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    updateContainerFilter(
                            filteredData,
                            searchField.getText(),
                            statusFilter.getValue()
                    );
                }
        );


        statusFilter.valueProperty().addListener(
                (observable, oldValue, newValue) -> {

                    updateContainerFilter(
                            filteredData,
                            searchField.getText(),
                            statusFilter.getValue()
                    );
                }
        );


        // =====================================================
        // ADD TABLE
        // =====================================================

        inventoryBox.getChildren().addAll(
                inventoryHeader,
                table
        );


        // =====================================================
        // ADD EVERYTHING TO MAIN CONTENT
        // =====================================================

        content.getChildren().addAll(
                title,
                subtitle,
                cards,
                statusBox,
                inventoryBox
        );
    }


    // =====================================================
    // CREATE SUMMARY CARD
    // =====================================================

    private VBox createCard(
            String title,
            String value,
            String description,
            String descriptionColor
    ) {

        VBox card =
                new VBox();

        card.setSpacing(10);

        card.setPadding(
                new Insets(20)
        );

        card.setPrefWidth(
                220
        );

        card.setPrefHeight(
                110
        );

        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:10;" +
                "-fx-border-color:#dfe7f1;" +
                "-fx-border-radius:10;"
        );


        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-font-size:12px;" +
                "-fx-text-fill:#777777;"
        );


        Label valueLabel =
                new Label(value);

        valueLabel.setStyle(
                "-fx-font-size:25px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#14213d;"
        );


        Label descriptionLabel =
                new Label(description);

        descriptionLabel.setStyle(
                "-fx-font-size:12px;" +
                "-fx-text-fill:" +
                descriptionColor + ";"
        );


        card.getChildren().addAll(
                titleLabel,
                valueLabel,
                descriptionLabel
        );


        return card;
    }


    // =====================================================
    // CREATE STATUS BAR PART
    // =====================================================

    private HBox createStatusPart(
            String text,
            String backgroundColor,
            double width
    ) {

        HBox part =
                new HBox();

        part.setPrefWidth(
                width
        );

        part.setAlignment(
                Pos.CENTER
        );

        part.setStyle(
                "-fx-background-color:" +
                backgroundColor + ";"
        );


        if (!text.isEmpty()) {

            Label percent =
                    new Label(text);

            percent.setStyle(
                    "-fx-text-fill:white;" +
                    "-fx-font-weight:bold;" +
                    "-fx-font-size:11px;"
            );

            part.getChildren().add(
                    percent
            );
        }


        return part;
    }


    // =====================================================
    // CREATE STATUS VALUE
    // =====================================================

    private VBox createStatusValue(
            String value,
            String title,
            String valueColor
    ) {

        VBox box =
                new VBox();

        Label number =
                new Label(value);

        number.setStyle(
                "-fx-font-size:18px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:" +
                valueColor + ";"
        );


        Label label =
                new Label(title);

        label.setStyle(
                "-fx-font-size:12px;" +
                "-fx-text-fill:#777777;"
        );


        box.getChildren().addAll(
                number,
                label
        );


        return box;
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
                new TableColumn<>(
                        title
                );

        column.setPrefWidth(
                width
        );

        column.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()[index]
                        )
        );

        return column;
    }

    // =====================================================
    // UPDATE CONTAINER FILTER
    // =====================================================

    private void updateContainerFilter(
                    FilteredList<String[]> filteredData,
                    String searchText,
                    String selectedStatus) {

        String search = searchText
                        .toLowerCase()
                        .trim();


        filteredData.setPredicate(container -> {

                // -----------------------------------------
                // SEARCH
                // -----------------------------------------

                boolean matchesSearch = false;

                for (String value : container) {

                    if (value
                            .toLowerCase()
                            .contains(search)) {

                            matchesSearch = true;
                            break;
                    }
                }


                // -----------------------------------------
                // STATUS
                // -----------------------------------------

                boolean matchesStatus = selectedStatus.equals(
                                    "All Statuses"
                                        )
                                            ||
                                        container[6].equals(
                                        selectedStatus
                                        );


                    return matchesSearch && matchesStatus;
        });
    }

    // =====================================================
    // GET PAGE CONTENT
    // =====================================================

    public VBox getContent() {

        return content;
    }
}