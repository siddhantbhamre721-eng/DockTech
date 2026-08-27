package com.projectui.view.portauthority;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.projectui.controller.portauthority.ContainerController;
import com.projectui.controller.portauthority.NotificationController;
import com.projectui.model.portauthority.Container;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;


public class ContainerManagementPage {

    private VBox content;

    private ObservableList<Container> containerData =
            FXCollections.observableArrayList();


    // =====================================================
    // CONTAINER CONTROLLER
    // =====================================================

    private ContainerController containerController =
            new ContainerController();


    // =====================================================
    // NOTIFICATION CONTROLLER
    // =====================================================

    private NotificationController notificationController =
            new NotificationController();


    // =====================================================
    // SUMMARY CARD VALUES
    // =====================================================

    private Label totalContainersValue;

    private Label availableValue;

    private Label allocatedValue;

    private Label inTransitValue;

    private Label pendingValue;


    // =====================================================
    // STATUS DISTRIBUTION VALUES
    // =====================================================

    private Label allocatedStatusValue;

    private Label availableStatusValue;

    private Label transitStatusValue;

    private Label pendingStatusValue;


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


        // =====================================================
        // TOTAL CONTAINERS
        // =====================================================

        totalContainersValue =
                new Label("0");

        VBox totalCard =
                createCard(
                        "TOTAL CONTAINERS",
                        totalContainersValue,
                        "All registered units",
                        "#00a878"
                );


        // =====================================================
        // AVAILABLE
        // =====================================================

        availableValue =
                new Label("0");

        VBox availableCard =
                createCard(
                        "AVAILABLE",
                        availableValue,
                        "Ready for allocation",
                        "#2779ff"
                );


        // =====================================================
        // ALLOCATED
        // =====================================================

        allocatedValue =
                new Label("0");

        VBox allocatedCard =
                createCard(
                        "ALLOCATED",
                        allocatedValue,
                        "Assigned to cargo",
                        "#00a878"
                );


        // =====================================================
        // IN TRANSIT
        // =====================================================

        inTransitValue =
                new Label("0");

        VBox transitCard =
                createCard(
                        "IN TRANSIT",
                        inTransitValue,
                        "Currently moving",
                        "#ff8a00"
                );


        // =====================================================
        // PENDING
        // =====================================================

        pendingValue =
                new Label("0");

        VBox pendingCard =
                createCard(
                        "PENDING",
                        pendingValue,
                        "Awaiting processing",
                        "#d1f421"
                );


        cards.getChildren().addAll(
                totalCard,
                availableCard,
                allocatedCard,
                transitCard,
                pendingCard
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


        // =====================================================
        // STATUS BAR
        // =====================================================

        HBox statusBar =
                new HBox();

        statusBar.setPrefHeight(20);


        HBox allocatedPart =
                createStatusPart(
                        "",
                        "#00b67a",
                        0
                );


        HBox availablePart =
                createStatusPart(
                        "",
                        "#2779ff",
                        0
                );


        HBox transitPart =
                createStatusPart(
                        "",
                        "#ff8a00",
                        0
                );


        HBox pendingPart =
                createStatusPart(
                        "",
                        "#9ca3af",
                        0
                );


        statusBar.getChildren().addAll(
                allocatedPart,
                availablePart,
                transitPart,
                pendingPart
        );


        // =====================================================
        // STATUS DISTRIBUTION VALUES
        // =====================================================

        allocatedStatusValue =
                new Label("0");

        availableStatusValue =
                new Label("0");

        transitStatusValue =
                new Label("0");

        pendingStatusValue =
                new Label("0");


        HBox statusValues =
                new HBox();

        statusValues.setSpacing(80);


        statusValues.getChildren().addAll(

                createStatusValue(
                        allocatedStatusValue,
                        "Allocated",
                        "#00a878"
                ),

                createStatusValue(
                        availableStatusValue,
                        "Available",
                        "#2779ff"
                ),

                createStatusValue(
                        transitStatusValue,
                        "In Transit",
                        "#ff8a00"
                ),

                createStatusValue(
                        pendingStatusValue,
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


        // =====================================================
        // INVENTORY HEADER
        // =====================================================

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


        // =====================================================
        // SEARCH
        // =====================================================

        TextField searchField =
                new TextField();

        searchField.setPromptText(
                "Search Container..."
        );

        searchField.setPrefWidth(
                200
        );


        // =====================================================
        // STATUS FILTER
        // =====================================================

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


        // =====================================================
        // ADD CONTAINER BUTTON
        // =====================================================

        Button addContainerButton =
                new Button(
                        "+ Add Container"
                );


        // =====================================================
        // ADD CONTAINER ACTION
        // =====================================================

        addContainerButton.setOnAction(e -> {

                Dialog<ButtonType> dialog =
                        new Dialog<>();

                dialog.setTitle(
                        "Add Container"
                );

                dialog.setHeaderText(
                        "Register New Container"
                );


                // =================================================
                // FORM FIELDS
                // =================================================

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


                TextField weightField =
                        new TextField();

                weightField.setPromptText(
                        "Weight (e.g. 28 T)"
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


                // =================================================
                // FORM
                // =================================================

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

                        new Label("Weight:"),
                        weightField,

                        new Label("Status:"),
                        containerStatusField
                );


                dialog.getDialogPane().setContent(
                        form
                );


                // =================================================
                // BUTTONS
                // =================================================

                dialog.getDialogPane()
                        .getButtonTypes()
                        .addAll(
                                ButtonType.OK,
                                ButtonType.CANCEL
                        );


                // =================================================
                // SHOW DIALOG
                // =================================================

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

                        String weight =
                                weightField.getText().trim();

                        String containerStatus =
                                containerStatusField.getValue();


                        // =================================================
                        // VALIDATION
                        // =================================================

                        if (ship.isEmpty() ||
                                cargoId.isEmpty() ||
                                location.isEmpty() ||
                                size.isEmpty() ||
                                weight.isEmpty() ||
                                type == null ||
                                containerStatus == null) {

                                Alert alert =
                                        new Alert(
                                                Alert.AlertType.WARNING
                                        );

                                alert.setTitle(
                                        "Missing Information"
                                );

                                alert.setHeaderText(
                                        "Incomplete Container Details"
                                );

                                alert.setContentText(
                                        "Please fill in all the fields before adding the container."
                                );

                                alert.showAndWait();

                                return;
                        }


                        // =================================================
                        // GENERATE CONTAINER ID
                        // =================================================

                        String containerId =
                                generateContainerId();


                        // =================================================
                        // CREATE NEW CONTAINER
                        // =================================================

                        Container container =
                                new Container(

                                        containerId,
                                        type,
                                        cargoId,
                                        ship,
                                        location,
                                        size,
                                        weight,
                                        containerStatus
                                );


                        // =================================================
                        // SAVE TO FIREBASE
                        // =================================================

                        containerController.saveContainer(
                                container
                        );


                        // =================================================
                        // CREATE NOTIFICATION
                        // =================================================

                        notificationController.createNotification(

                                "Container registered",

                                container.getContainerId()
                                        +
                                        " was added successfully.",

                                "ADD"
                        );


                        // =================================================
                        // ADD TO LOCAL LIST
                        // =================================================

                        containerData.add(
                                container
                        );


                        // =================================================
                        // REFRESH STATISTICS
                        // =================================================

                        refreshStatistics(

                                containerData,

                                allocatedPart,
                                availablePart,
                                transitPart,
                                pendingPart
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

        TableView<Container> table =
                new TableView<>();


        // =====================================================
        // MODERN TABLE BASE STYLE
        // =====================================================

        table.setPrefHeight(360);

        table.setMinHeight(360);

        table.setMaxHeight(360);

        table.setFixedCellSize(38);

        table.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-border-color:transparent;" +
                "-fx-table-cell-border-color:transparent;" +
                "-fx-font-size:10;"
        );


        // =====================================================
        // TABLE COLUMNS
        // =====================================================

        TableColumn<Container, String> containerId =
                createColumn(
                        "Container ID",
                        120,
                        Container::getContainerId
                );


        TableColumn<Container, String> type =
                createColumn(
                        "Type",
                        110,
                        Container::getType
                );


        TableColumn<Container, String> cargoId =
                createColumn(
                        "Cargo ID",
                        100,
                        Container::getCargoId
                );


        TableColumn<Container, String> ship =
                createColumn(
                        "Ship",
                        130,
                        Container::getShip
                );


        TableColumn<Container, String> location =
                createColumn(
                        "Location",
                        120,
                        Container::getLocation
                );


        TableColumn<Container, String> size =
                createColumn(
                        "Size",
                        80,
                        Container::getSize
                );


        TableColumn<Container, String> weight =
                createColumn(
                        "Weight",
                        90,
                        Container::getWeight
                );


        TableColumn<Container, String> status =
                createColumn(
                        "Status",
                        120,
                        Container::getStatus
                );


        // =====================================================
        // STYLE NORMAL COLUMNS
        // =====================================================

        styleContainerColumn(type);

        styleContainerColumn(cargoId);

        styleContainerColumn(location);

        styleContainerColumn(size);

        styleContainerColumn(weight);


        // =====================================================
        // CONTAINER ID STYLE
        // =====================================================

        containerId.setCellFactory(
                column -> new TableCell<Container, String>() {

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

                            setText(null);

                            return;
                        }


                        setText(item);


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
        // SHIP STYLE
        // =====================================================

        ship.setCellFactory(
                column -> new TableCell<Container, String>() {

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

                            setText(null);

                            return;
                        }


                        setText(item);


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
        // STATUS BADGES
        // =====================================================

        status.setCellFactory(
                column -> new TableCell<Container, String>() {

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

                            setGraphic(null);

                            setText(null);

                            return;
                        }


                        statusLabel.setText(item);


                        // =============================================
                        // AVAILABLE
                        // =============================================

                        if (item.equalsIgnoreCase("Available")) {

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
                        // ALLOCATED
                        // =============================================

                        else if (item.equalsIgnoreCase("Allocated")) {

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
                        // IN TRANSIT
                        // =============================================

                        else if (item.equalsIgnoreCase("In Transit")) {

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
                        // PENDING
                        // =============================================

                        else if (item.equalsIgnoreCase("Pending")) {

                            statusLabel.setStyle(
                                    "-fx-background-color:#f1f5f9;" +
                                    "-fx-text-fill:#64748b;" +
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


        // =====================================================
        // ACTION COLUMN
        // =====================================================

        TableColumn<Container, String> action =
                new TableColumn<>("Action");


        action.setPrefWidth(
                210
        );


        action.setCellFactory(
                column ->
                        new TableCell<Container, String>() {


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
                                        // VIEW BUTTON
                                        // =================================================

                                        viewButton.setOnAction(event -> {

                                                Container container =
                                                        getTableView()
                                                                .getItems()
                                                                .get(getIndex());


                                                Alert alert =
                                                        new Alert(
                                                                Alert.AlertType.INFORMATION
                                                        );


                                                alert.setTitle(
                                                        "Container Details"
                                                );


                                                alert.setHeaderText(
                                                        "Container Details"
                                                );


                                                alert.setContentText(

                                                        "Container ID: "
                                                        + container.getContainerId()
                                                        + "\n"

                                                        + "Type: "
                                                        + container.getType()
                                                        + "\n"

                                                        + "Cargo ID: "
                                                        + container.getCargoId()
                                                        + "\n"

                                                        + "Ship: "
                                                        + container.getShip()
                                                        + "\n"

                                                        + "Location: "
                                                        + container.getLocation()
                                                        + "\n"

                                                        + "Size: "
                                                        + container.getSize()
                                                        + "\n"

                                                        + "Weight: "
                                                        + container.getWeight()
                                                        + "\n"

                                                        + "Status: "
                                                        + container.getStatus()
                                                );


                                                alert.showAndWait();
                                        });


                                        // =================================================
                                        // EDIT BUTTON
                                        // =================================================

                                        editButton.setOnAction(event -> {

                                                Container container =
                                                        getTableView()
                                                                .getItems()
                                                                .get(getIndex());


                                                // =================================================
                                                // EDIT FIELDS
                                                // =================================================

                                                TextField cargoIdField =
                                                        new TextField(
                                                                container.getCargoId()
                                                        );


                                                TextField shipField =
                                                        new TextField(
                                                                container.getShip()
                                                        );


                                                TextField locationField =
                                                        new TextField(
                                                                container.getLocation()
                                                        );


                                                TextField sizeField =
                                                        new TextField(
                                                                container.getSize()
                                                        );


                                                TextField weightField =
                                                        new TextField(
                                                                container.getWeight()
                                                        );


                                                ComboBox<String> typeBox =
                                                        new ComboBox<>();


                                                typeBox.getItems().addAll(
                                                        "Dry Cargo",
                                                        "Reefer",
                                                        "Tank",
                                                        "Flat Rack"
                                                );


                                                typeBox.setValue(
                                                        container.getType()
                                                );


                                                ComboBox<String> statusBox =
                                                        new ComboBox<>();


                                                statusBox.getItems().addAll(
                                                        "Allocated",
                                                        "Available",
                                                        "In Transit",
                                                        "Pending"
                                                );


                                                statusBox.setValue(
                                                        container.getStatus()
                                                );


                                                // =================================================
                                                // GRID
                                                // =================================================

                                                GridPane grid =
                                                        new GridPane();

                                                grid.setHgap(10);

                                                grid.setVgap(10);


                                                grid.add(
                                                        new Label("Type:"),
                                                        0,
                                                        0
                                                );


                                                grid.add(
                                                        typeBox,
                                                        1,
                                                        0
                                                );


                                                grid.add(
                                                        new Label("Cargo ID:"),
                                                        0,
                                                        1
                                                );


                                                grid.add(
                                                        cargoIdField,
                                                        1,
                                                        1
                                                );


                                                grid.add(
                                                        new Label("Ship:"),
                                                        0,
                                                        2
                                                );


                                                grid.add(
                                                        shipField,
                                                        1,
                                                        2
                                                );


                                                grid.add(
                                                        new Label("Location:"),
                                                        0,
                                                        3
                                                );


                                                grid.add(
                                                        locationField,
                                                        1,
                                                        3
                                                );


                                                grid.add(
                                                        new Label("Size:"),
                                                        0,
                                                        4
                                                );


                                                grid.add(
                                                        sizeField,
                                                        1,
                                                        4
                                                );


                                                grid.add(
                                                        new Label("Weight:"),
                                                        0,
                                                        5
                                                );


                                                grid.add(
                                                        weightField,
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


                                                // =================================================
                                                // DIALOG
                                                // =================================================

                                                Dialog<ButtonType> dialog =
                                                        new Dialog<>();


                                                dialog.setTitle(
                                                        "Edit Container"
                                                );


                                                dialog.setHeaderText(
                                                        "Edit Container Details"
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


                                                dialog.getDialogPane()
                                                        .setContent(
                                                                grid
                                                        );


                                                // =================================================
                                                // SAVE CHANGES
                                                // =================================================

                                                dialog.setResultConverter(button -> {

                                                        if (button == okButton) {


                                                                String cargoId =
                                                                        cargoIdField
                                                                                .getText()
                                                                                .trim();


                                                                String ship =
                                                                        shipField
                                                                                .getText()
                                                                                .trim();


                                                                String location =
                                                                        locationField
                                                                                .getText()
                                                                                .trim();


                                                                String size =
                                                                        sizeField
                                                                                .getText()
                                                                                .trim();


                                                                String weight =
                                                                        weightField
                                                                                .getText()
                                                                                .trim();


                                                                String type =
                                                                        typeBox.getValue();


                                                                String containerStatus =
                                                                        statusBox.getValue();


                                                                // =============================================
                                                                // VALIDATION
                                                                // =============================================

                                                                if (cargoId.isEmpty() ||
                                                                        ship.isEmpty() ||
                                                                        location.isEmpty() ||
                                                                        size.isEmpty() ||
                                                                        weight.isEmpty() ||
                                                                        type == null ||
                                                                        containerStatus == null) {


                                                                        Alert alert =
                                                                                new Alert(
                                                                                        Alert.AlertType.WARNING
                                                                                );


                                                                        alert.setTitle(
                                                                                "Missing Information"
                                                                        );


                                                                        alert.setHeaderText(
                                                                                "Incomplete Container Details"
                                                                        );


                                                                        alert.setContentText(
                                                                                "Please fill in all the fields."
                                                                        );


                                                                        alert.showAndWait();


                                                                        return null;
                                                                }


                                                                // =============================================
                                                                // UPDATE CONTAINER OBJECT
                                                                // =============================================

                                                                container.setType(
                                                                        type
                                                                );


                                                                container.setCargoId(
                                                                        cargoId
                                                                );


                                                                container.setShip(
                                                                        ship
                                                                );


                                                                container.setLocation(
                                                                        location
                                                                );


                                                                container.setSize(
                                                                        size
                                                                );


                                                                container.setWeight(
                                                                        weight
                                                                );


                                                                container.setStatus(
                                                                        containerStatus
                                                                );


                                                                // =============================================
                                                                // UPDATE FIREBASE
                                                                // =============================================

                                                                containerController.updateContainer(
                                                                        container
                                                                );


                                                                // =============================================
                                                                // CREATE NOTIFICATION
                                                                // =============================================

                                                                notificationController.createNotification(

                                                                        "Container record updated",

                                                                        container.getContainerId()
                                                                                +
                                                                                " was updated successfully.",

                                                                        "EDIT"
                                                                );


                                                                // =============================================
                                                                // REFRESH TABLE
                                                                // =============================================

                                                                getTableView()
                                                                        .refresh();


                                                                // =============================================
                                                                // REFRESH STATISTICS
                                                                // =============================================

                                                                refreshStatistics(

                                                                        containerData,

                                                                        allocatedPart,
                                                                        availablePart,
                                                                        transitPart,
                                                                        pendingPart
                                                                );
                                                        }


                                                        return button;
                                                });


                                                dialog.showAndWait();
                                        });


                                        // =================================================
                                        // DELETE BUTTON
                                        // =================================================

                                        deleteButton.setOnAction(event -> {

                                                Container container =
                                                        getTableView()
                                                                .getItems()
                                                                .get(getIndex());


                                                showDeleteConfirmation(

                                                        container,

                                                        table,

                                                        allocatedPart,
                                                        availablePart,
                                                        transitPart,
                                                        pendingPart
                                                );
                                        });
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


                                                setStyle(
                                                        "-fx-background-color:white;" +
                                                        "-fx-border-color:transparent transparent #e8edf2 transparent;" +
                                                        "-fx-padding:0 4 0 4;"
                                                );
                                        }
                                }
                        }
        );


        // =====================================================
        // ADD COLUMNS TO TABLE
        // =====================================================

        table.getColumns().addAll(
                containerId,
                type,
                cargoId,
                ship,
                location,
                size,
                weight,
                status,
                action
        );


        // =====================================================
        // MODERN TABLE HEADER
        // =====================================================

        table.skinProperty().addListener(
                (observable, oldSkin, newSkin) -> {

                    if (newSkin != null) {

                        Platform.runLater(() -> {

                            styleContainerTableHeader(
                                    table
                            );
                        });
                    }
                }
        );


        // =====================================================
        // LOAD CONTAINERS FROM FIREBASE
        // =====================================================

        List<Container> containers =
                containerController.getContainers();


        // =====================================================
        // ADD FIREBASE DATA TO LOCAL LIST
        // =====================================================

        containerData.addAll(
                containers
        );


        // =====================================================
        // INITIAL STATISTICS
        // =====================================================

        refreshStatistics(

                containerData,

                allocatedPart,
                availablePart,
                transitPart,
                pendingPart
        );


        // =====================================================
        // FILTERED DATA
        // =====================================================

        FilteredList<Container> filteredData =
                new FilteredList<>(
                        containerData,
                        data -> true
                );


        table.setItems(
                filteredData
        );


        // =====================================================
        // SEARCH FILTER
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


        // =====================================================
        // STATUS FILTER
        // =====================================================

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
        // INVENTORY BOX
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
            Label valueLabel,
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


        Label percent =
                new Label(
                        text
                );


        percent.setStyle(
                "-fx-text-fill:white;" +
                "-fx-font-weight:bold;" +
                "-fx-font-size:11px;"
        );


        part.getChildren().add(
                percent
        );


        return part;
    }


    // =====================================================
    // CREATE STATUS VALUE
    // =====================================================

    private VBox createStatusValue(

            Label number,
            String title,
            String valueColor

    ) {

        VBox box =
                new VBox();


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
    // REFRESH ALL STATISTICS
    // =====================================================

    private void refreshStatistics(

            List<Container> containers,

            HBox allocatedPart,
            HBox availablePart,
            HBox transitPart,
            HBox pendingPart

    ) {

        int total =
                containers.size();


        int available = 0;

        int allocated = 0;

        int inTransit = 0;

        int pending = 0;


        // =================================================
        // COUNT CONTAINERS BY STATUS
        // =================================================

        for (Container container : containers) {

            if (container == null) {
                continue;
            }


            String status =
                    container.getStatus();


            if (status == null) {
                continue;
            }


            switch (status) {

                case "Available":

                    available++;

                    break;


                case "Allocated":

                    allocated++;

                    break;


                case "In Transit":

                    inTransit++;

                    break;


                case "Pending":

                    pending++;

                    break;
            }
        }


        // =================================================
        // UPDATE SUMMARY CARDS
        // =================================================

        totalContainersValue.setText(
                String.valueOf(total)
        );


        availableValue.setText(
                String.valueOf(available)
        );


        allocatedValue.setText(
                String.valueOf(allocated)
        );


        inTransitValue.setText(
                String.valueOf(inTransit)
        );


        pendingValue.setText(
                String.valueOf(pending)
        );


        // =================================================
        // UPDATE STATUS DISTRIBUTION VALUES
        // =================================================

        allocatedStatusValue.setText(
                String.valueOf(allocated)
        );


        availableStatusValue.setText(
                String.valueOf(available)
        );


        transitStatusValue.setText(
                String.valueOf(inTransit)
        );


        pendingStatusValue.setText(
                String.valueOf(pending)
        );


        // =================================================
        // UPDATE STATUS BAR
        // =================================================

        updateStatusBar(

                allocatedPart,
                availablePart,
                transitPart,
                pendingPart,

                allocated,
                available,
                inTransit,
                pending
        );
    }


    // =====================================================
    // UPDATE STATUS BAR
    // =====================================================

    private void updateStatusBar(

            HBox allocatedPart,
            HBox availablePart,
            HBox transitPart,
            HBox pendingPart,

            int allocated,
            int available,
            int inTransit,
            int pending

    ) {

        int total =
                allocated
                + available
                + inTransit
                + pending;


        // =================================================
        // NO CONTAINERS
        // =================================================

        if (total == 0) {

            allocatedPart.setPrefWidth(0);

            availablePart.setPrefWidth(0);

            transitPart.setPrefWidth(0);

            pendingPart.setPrefWidth(0);


            setPercentage(
                    allocatedPart,
                    ""
            );


            setPercentage(
                    availablePart,
                    ""
            );


            setPercentage(
                    transitPart,
                    ""
            );


            setPercentage(
                    pendingPart,
                    ""
            );


            return;
        }


        // =================================================
        // TOTAL BAR WIDTH
        // =================================================

        double totalBarWidth =
                820;


        // =================================================
        // CALCULATE WIDTHS
        // =================================================

        double allocatedWidth =
                (allocated / (double) total)
                * totalBarWidth;


        double availableWidth =
                (available / (double) total)
                * totalBarWidth;


        double transitWidth =
                (inTransit / (double) total)
                * totalBarWidth;


        double pendingWidth =
                (pending / (double) total)
                * totalBarWidth;


        // =================================================
        // APPLY WIDTHS
        // =================================================

        allocatedPart.setPrefWidth(
                allocatedWidth
        );


        availablePart.setPrefWidth(
                availableWidth
        );


        transitPart.setPrefWidth(
                transitWidth
        );


        pendingPart.setPrefWidth(
                pendingWidth
        );


        // =================================================
        // CALCULATE PERCENTAGES
        // =================================================

        int allocatedPercent =
                (int) Math.round(
                        (allocated / (double) total) * 100
                );


        int availablePercent =
                (int) Math.round(
                        (available / (double) total) * 100
                );


        int transitPercent =
                (int) Math.round(
                        (inTransit / (double) total) * 100
                );


        int pendingPercent =
                (int) Math.round(
                        (pending / (double) total) * 100
                );


        // =================================================
        // UPDATE PERCENTAGE TEXT
        // =================================================

        setPercentage(
                allocatedPart,
                allocatedPercent + "%"
        );


        setPercentage(
                availablePart,
                availablePercent + "%"
        );


        setPercentage(
                transitPart,
                transitPercent + "%"
        );


        setPercentage(
                pendingPart,
                pendingPercent + "%"
        );
    }


    // =====================================================
    // SET STATUS BAR PERCENTAGE
    // =====================================================

    private void setPercentage(

            HBox part,
            String text

    ) {

        if (part.getChildren().isEmpty()) {
            return;
        }


        Label label =
                (Label) part.getChildren().get(0);


        label.setText(
                text
        );
    }


    // =====================================================
    // GENERATE CONTAINER ID
    // =====================================================

    private String generateContainerId() {

        int highestId =
                1024;


        for (Container container : containerData) {

            try {

                String id =
                        container.getContainerId();


                if (id == null) {
                    continue;
                }


                if (id.startsWith("CONT-")) {

                    int number =
                            Integer.parseInt(
                                    id.substring(5)
                            );


                    if (number > highestId) {

                        highestId =
                                number;
                    }
                }

            } catch (Exception e) {

                // Ignore invalid container IDs
            }
        }


        return "CONT-" +
                (highestId + 1);
    }


    // =====================================================
    // SHOW DELETE CONFIRMATION
    // =====================================================

    private void showDeleteConfirmation(

            Container container,

            TableView<Container> table,

            HBox allocatedPart,
            HBox availablePart,
            HBox transitPart,
            HBox pendingPart

    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );


        alert.setTitle(
                "Delete Container"
        );


        alert.setHeaderText(
                "Delete Container?"
        );


        alert.setContentText(

                "Are you sure you want to delete "

                + container.getContainerId()

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


                        // =============================================
                        // DELETE FROM FIREBASE
                        // =============================================

                        containerController.deleteContainer(
                                container.getContainerId()
                        );


                        // =============================================
                        // CREATE NOTIFICATION
                        // =============================================

                        notificationController.createNotification(

                                "Container deleted",

                                container.getContainerId()
                                        +
                                        " was deleted from the system.",

                                "DELETE"
                        );


                        // =============================================
                        // REMOVE FROM LOCAL LIST
                        // =============================================

                        containerData.remove(
                                container
                        );


                        // =============================================
                        // REFRESH TABLE
                        // =============================================

                        table.refresh();


                        // =============================================
                        // REFRESH STATISTICS
                        // =============================================

                        refreshStatistics(

                                containerData,

                                allocatedPart,
                                availablePart,
                                transitPart,
                                pendingPart
                        );


                        // =============================================
                        // SUCCESS MESSAGE
                        // =============================================

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
                                "Container deleted successfully."
                        );


                        success.showAndWait();
                    }
                });
    }


    // =====================================================
    // CREATE TABLE COLUMN
    // =====================================================

    private TableColumn<Container, String> createColumn(

            String title,
            double width,
            Function<Container, String> valueGetter

    ) {

        TableColumn<Container, String> column =
                new TableColumn<>(
                        title
                );


        column.setPrefWidth(
                width
        );


        column.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                valueGetter.apply(
                                        data.getValue()
                                )
                        )
        );


        return column;
    }


    // =====================================================
    // STYLE NORMAL CONTAINER COLUMN
    // =====================================================

    private void styleContainerColumn(
            TableColumn<Container, String> column
    ) {

        column.setCellFactory(
                col -> new TableCell<Container, String>() {

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

                            setText(null);

                            return;
                        }


                        setText(item);


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
    // STYLE TABLE HEADER
    // =====================================================

    private void styleContainerTableHeader(
            TableView<Container> table
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
        // INDIVIDUAL HEADERS
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
        // EMPTY HEADER AREA
        // =================================================

        Node filler =
                table.lookup(".filler");


        if (filler != null) {

            filler.setStyle(
                    "-fx-background-color:#f1f5f9;" +
                    "-fx-border-color:transparent;"
            );
        }
    }


    // =====================================================
    // UPDATE CONTAINER FILTER
    // =====================================================

    private void updateContainerFilter(

            FilteredList<Container> filteredData,

            String searchText,

            String selectedStatus

    ) {

        String search =
                searchText
                        .toLowerCase()
                        .trim();


        filteredData.setPredicate(
                container -> {


                        // =============================================
                        // SEARCH
                        // =============================================

                        boolean matchesSearch =

                                safeContains(
                                        container.getContainerId(),
                                        search
                                )

                                ||

                                safeContains(
                                        container.getType(),
                                        search
                                )

                                ||

                                safeContains(
                                        container.getCargoId(),
                                        search
                                )

                                ||

                                safeContains(
                                        container.getShip(),
                                        search
                                )

                                ||

                                safeContains(
                                        container.getLocation(),
                                        search
                                )

                                ||

                                safeContains(
                                        container.getSize(),
                                        search
                                )

                                ||

                                safeContains(
                                        container.getWeight(),
                                        search
                                )

                                ||

                                safeContains(
                                        container.getStatus(),
                                        search
                                );


                        // =============================================
                        // STATUS FILTER
                        // =============================================

                        boolean matchesStatus =

                                selectedStatus == null

                                ||

                                selectedStatus.equals(
                                        "All Statuses"
                                )

                                ||

                                safeEquals(
                                        container.getStatus(),
                                        selectedStatus
                                );


                        // =============================================
                        // FINAL RESULT
                        // =============================================

                        return matchesSearch
                                && matchesStatus;
                }
        );
    }


    // =====================================================
    // SAFE STRING CONTAINS
    // =====================================================

    private boolean safeContains(

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
    // SAFE STRING EQUALS
    // =====================================================

    private boolean safeEquals(

            String value,
            String expected

    ) {

        if (value == null ||
                expected == null) {

            return false;
        }


        return value.equals(
                expected
        );
    }


    // =====================================================
    // GET PAGE CONTENT
    // =====================================================

    public VBox getContent() {

        return content;
    }
}