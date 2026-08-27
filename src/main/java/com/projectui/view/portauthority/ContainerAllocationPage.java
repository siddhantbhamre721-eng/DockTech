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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.projectui.controller.portauthority.ContainerController;
import com.projectui.controller.portauthority.NotificationController;
import com.projectui.model.portauthority.Container;


public class ContainerAllocationPage {

    private VBox content;


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
    // LOCAL CONTAINER DATA
    // =====================================================

    private ObservableList<Container> containerData =
            FXCollections.observableArrayList();


    // =====================================================
    // SUMMARY CARD VALUES
    // =====================================================

    private Label totalContainersValue;

    private Label allocatedValue;

    private Label availableValue;

    private Label pendingValue;


    // =====================================================
    // CONTAINER CARD AREA
    // =====================================================

    private VBox containerCards;


    public ContainerAllocationPage() {


        // =====================================================
        // MAIN CONTENT
        // =====================================================

        content =
                new VBox();

        content.setSpacing(
                20
        );

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
                        "Container Allocation"
                );

        title.setStyle(
                "-fx-font-size:28px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#14213d;"
        );


        Label subtitle =
                new Label(
                        "Allocate containers to cargo and vessels."
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

        cards.setSpacing(
                15
        );


        // =====================================================
        // TOTAL CONTAINERS
        // =====================================================

        totalContainersValue =
                new Label("0");


        VBox totalCard =
                createCard(
                        "TOTAL CONTAINERS",
                        totalContainersValue,
                        "All registered containers",
                        "#00a878"
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
                        "Currently assigned",
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
        // PENDING ALLOCATION
        // =====================================================

        pendingValue =
                new Label("0");


        VBox pendingCard =
                createCard(
                        "PENDING ALLOCATION",
                        pendingValue,
                        "Awaiting allocation",
                        "#ff8a00"
                );


        cards.getChildren().addAll(
                totalCard,
                allocatedCard,
                availableCard,
                pendingCard
        );


        // =====================================================
        // AVAILABLE CONTAINERS SECTION
        // =====================================================

        VBox availableContainerBox =
                new VBox();

        availableContainerBox.setSpacing(
                15
        );

        availableContainerBox.setPadding(
                new Insets(20)
        );

        availableContainerBox.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:10;" +
                "-fx-border-color:#dfe7f1;" +
                "-fx-border-radius:10;"
        );


        Label availableTitle =
                new Label(
                        "Containers Available for Allocation"
                );

        availableTitle.setStyle(
                "-fx-font-size:16px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#14213d;"
        );


        Label availableSubtitle =
                new Label(
                        "Select a container to allocate it to cargo and a vessel."
                );

        availableSubtitle.setStyle(
                "-fx-font-size:12px;" +
                "-fx-text-fill:#7d8da6;"
        );


        // =====================================================
        // CONTAINER CARDS
        // =====================================================

        containerCards =
                new VBox();

        containerCards.setSpacing(
                10
        );


        availableContainerBox.getChildren().addAll(
                availableTitle,
                availableSubtitle,
                containerCards
        );


        // =====================================================
        // ALLOCATION RECORDS SECTION
        // =====================================================

        VBox allocationBox =
                new VBox();

        allocationBox.setSpacing(
                12
        );

        allocationBox.setPadding(
                new Insets(20)
        );

        allocationBox.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:10;" +
                "-fx-border-color:#dfe7f1;" +
                "-fx-border-radius:10;"
        );


        // =====================================================
        // HEADER
        // =====================================================

        HBox allocationHeader =
                new HBox();

        allocationHeader.setAlignment(
                Pos.CENTER_LEFT
        );


        VBox allocationInfo =
                new VBox();

        allocationInfo.setSpacing(
                3
        );


        Label allocationTitle =
                new Label(
                        "Allocation Records"
                );

        allocationTitle.setStyle(
                "-fx-font-size:16px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#14213d;"
        );


        Label allocationSubtitle =
                new Label(
                        "View and manage container allocations."
                );

        allocationSubtitle.setStyle(
                "-fx-font-size:12px;" +
                "-fx-text-fill:#7d8da6;"
        );


        allocationInfo.getChildren().addAll(
                allocationTitle,
                allocationSubtitle
        );


        // =====================================================
        // SEARCH FIELD
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
        // ALLOCATE BUTTON
        // =====================================================

        Button allocateButton =
                new Button(
                        "+ Allocate Container"
                );


        HBox headerSpace =
                new HBox();

        HBox.setHgrow(
                headerSpace,
                javafx.scene.layout.Priority.ALWAYS
        );


        HBox controls =
                new HBox(
                        10,
                        searchField,
                        statusFilter,
                        allocateButton
                );

        controls.setAlignment(
                Pos.CENTER_RIGHT
        );


        allocationHeader.getChildren().addAll(
                allocationInfo,
                headerSpace,
                controls
        );


        // =====================================================
        // ALLOCATION TABLE
        // =====================================================

        TableView<Container> table =
                new TableView<>();


        // =====================================================
        // TABLE VISUAL STYLE
        // =====================================================

        table.setFixedCellSize(
                38
        );

        table.setPrefHeight(
                360
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

        TableColumn<Container, String> containerId =
                createColumn(
                        "Container ID",
                        110,
                        Container::getContainerId
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


        TableColumn<Container, String> type =
                createColumn(
                        "Type",
                        100,
                        Container::getType
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


        TableColumn<Container, String> location =
                createColumn(
                        "Location",
                        120,
                        Container::getLocation
                );


        TableColumn<Container, String> allocationDate =
                createColumn(
                        "Allocation Date",
                        120,
                        Container::getAllocationDate
                );


        TableColumn<Container, String> status =
                createColumn(
                        "Status",
                        110,
                        Container::getStatus
                );


        // =====================================================
        // ACTION COLUMN
        // =====================================================

        TableColumn<Container, String> action =
                new TableColumn<>(
                        "Action"
                );

        action.setPrefWidth(
                180
        );


        action.setCellFactory(
                column ->
                        new TableCell<Container, String>() {


                                private final Button viewButton =
                                        new Button(
                                                "View"
                                        );


                                private final Button reallocateButton =
                                        new Button(
                                                "Reallocate"
                                        );


                                {
                                        // =============================================
                                        // ACTION BUTTON STYLING
                                        // =============================================

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


                                        reallocateButton.setStyle(
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


                                        // =============================================
                                        // VIEW
                                        // =============================================

                                        viewButton.setOnAction(
                                                event -> {

                                                    Container container =
                                                            getTableView()
                                                                    .getItems()
                                                                    .get(
                                                                            getIndex()
                                                                    );


                                                    showContainerDetails(
                                                            container
                                                    );
                                                }
                                        );


                                        // =============================================
                                        // REALLOCATE
                                        // =============================================

                                        reallocateButton.setOnAction(
                                                event -> {

                                                    Container container =
                                                            getTableView()
                                                                    .getItems()
                                                                    .get(
                                                                            getIndex()
                                                                    );


                                                    showAllocationDialog(
                                                            container,
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
                                                        reallocateButton
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


        // =====================================================
        // COLUMN VISUAL STYLING
        // =====================================================

        styleAllocationColumn(
                containerId
        );

        styleAllocationColumn(
                cargoId
        );

        styleAllocationColumn(
                type
        );

        styleAllocationColumn(
                size
        );

        styleAllocationColumn(
                weight
        );

        styleAllocationColumn(
                location
        );

        styleAllocationColumn(
                allocationDate
        );


        // =====================================================
        // CONTAINER ID STYLE
        // =====================================================

        styleAllocationBoldColumn(
                containerId
        );


        // =====================================================
        // CARGO ID STYLE
        // =====================================================

        styleAllocationBoldColumn(
                cargoId
        );


        // =====================================================
        // SHIP STYLE
        // =====================================================

        styleAllocationBoldColumn(
                ship
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
                        // ALLOCATED
                        // =============================================

                        if (item.equalsIgnoreCase(
                                "Allocated"
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
                        // AVAILABLE
                        // =============================================

                        } else if (item.equalsIgnoreCase(
                                "Available"
                        )) {

                            statusLabel.setStyle(
                                    "-fx-background-color:#dbeafe;" +
                                    "-fx-text-fill:#2563eb;" +
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
                        // IN TRANSIT
                        // =============================================

                        } else if (item.equalsIgnoreCase(
                                "In Transit"
                        )) {

                            statusLabel.setStyle(
                                    "-fx-background-color:#e0f2fe;" +
                                    "-fx-text-fill:#0369a1;" +
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


        // =====================================================
        // ADD COLUMNS TO TABLE
        // =====================================================

        table.getColumns().addAll(

                containerId,
                cargoId,
                ship,
                type,
                size,
                weight,
                location,
                allocationDate,
                status,
                action
        );


        // =====================================================
        // MODERN TABLE HEADER
        // =====================================================

        table.skinProperty().addListener(
                (observable, oldSkin, newSkin) -> {

                    if (newSkin != null) {

                        javafx.application.Platform.runLater(
                                () ->
                                        styleAllocationTableHeader(
                                                table
                                        )
                        );
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
        // FILTERED DATA
        // =====================================================

        FilteredList<Container> filteredData =
                new FilteredList<>(
                        containerData,
                        container -> true
                );


        table.setItems(
                filteredData
        );


        // =====================================================
        // INITIAL STATISTICS
        // =====================================================

        refreshStatistics();


        // =====================================================
        // INITIAL CONTAINER CARDS
        // =====================================================

        refreshContainerCards();


        // =====================================================
        // SEARCH FILTER
        // =====================================================

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    updateFilter(
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

                    updateFilter(
                            filteredData,
                            searchField.getText(),
                            statusFilter.getValue()
                    );
                }
        );


        // =====================================================
        // ALLOCATE CONTAINER BUTTON
        // =====================================================

        allocateButton.setOnAction(
                event -> {

                    showAllocationDialog(
                            null,
                            table
                    );
                }
        );


        // =====================================================
        // ADD TABLE TO SECTION
        // =====================================================

        allocationBox.getChildren().addAll(
                allocationHeader,
                table
        );


        // =====================================================
        // ADD EVERYTHING TO MAIN CONTENT
        // =====================================================

        content.getChildren().addAll(

                title,
                subtitle,

                cards,

                availableContainerBox,

                allocationBox
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

        card.setSpacing(
                10
        );

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
                new Label(
                        title
                );

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
                new Label(
                        description
                );

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
    // REFRESH STATISTICS
    // =====================================================

    private void refreshStatistics() {

        int total =
                containerData.size();


        int allocated =
                0;


        int available =
                0;


        int pending =
                0;


        // =================================================
        // COUNT BY STATUS
        // =================================================

        for (Container container : containerData) {

            if (container == null) {
                continue;
            }


            String status =
                    container.getStatus();


            if (status == null) {
                continue;
            }


            switch (status) {

                case "Allocated":

                    allocated++;

                    break;


                case "Available":

                    available++;

                    break;


                case "Pending":

                    pending++;

                    break;
            }
        }


        // =================================================
        // UPDATE CARDS
        // =================================================

        totalContainersValue.setText(
                String.valueOf(total)
        );


        allocatedValue.setText(
                String.valueOf(allocated)
        );


        availableValue.setText(
                String.valueOf(available)
        );


        pendingValue.setText(
                String.valueOf(pending)
        );
    }


    // =====================================================
    // REFRESH CONTAINER CARDS
    // =====================================================

    private void refreshContainerCards() {

        containerCards.getChildren().clear();


        // =================================================
        // ONLY SHOW AVAILABLE/PENDING CONTAINERS
        // =================================================

        int displayed =
                0;


        for (Container container : containerData) {

            if (container == null) {
                continue;
            }


            String status =
                    container.getStatus();


            if (!"Available".equals(status)
                    &&
                    !"Pending".equals(status)) {

                continue;
            }


            containerCards.getChildren().add(
                    createContainerCard(
                            container
                    )
            );


            displayed++;


            // Keep the section compact.
            if (displayed >= 5) {
                break;
            }
        }


        // =================================================
        // NO AVAILABLE CONTAINERS
        // =================================================

        if (displayed == 0) {

            Label emptyLabel =
                    new Label(
                            "No containers are currently available for allocation."
                    );


            emptyLabel.setStyle(
                    "-fx-font-size:13px;" +
                    "-fx-text-fill:#7d8da6;"
            );


            containerCards.getChildren().add(
                    emptyLabel
            );
        }
    }


    // =====================================================
    // CREATE CONTAINER CARD
    // =====================================================

    private HBox createContainerCard(
            Container container
    ) {

        HBox card =
                new HBox();


        card.setSpacing(
                15
        );


        card.setAlignment(
                Pos.CENTER_LEFT
        );


        card.setPadding(
                new Insets(12)
        );


        card.setStyle(
                "-fx-background-color:#f8fafc;" +
                "-fx-background-radius:8;" +
                "-fx-border-color:#e1e8f0;" +
                "-fx-border-radius:8;"
        );


        VBox info =
                new VBox();


        info.setSpacing(
                4
        );


        Label id =
                new Label(
                        container.getContainerId()
                );


        id.setStyle(
                "-fx-font-size:14px;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:#14213d;"
        );


        Label details =
                new Label(

                        safeValue(container.getType())
                        + "  |  "
                        + safeValue(container.getSize())
                        + "  |  "
                        + safeValue(container.getWeight())
                        + "  |  "
                        + safeValue(container.getLocation())

                );


        details.setStyle(
                "-fx-font-size:12px;" +
                "-fx-text-fill:#7d8da6;"
        );


        info.getChildren().addAll(
                id,
                details
        );


        HBox spacer =
                new HBox();


        HBox.setHgrow(
                spacer,
                javafx.scene.layout.Priority.ALWAYS
        );


        Button allocateButton =
                new Button(
                        "Allocate"
                );


        allocateButton.setOnAction(
                event -> {

                    showAllocationDialog(
                            container,
                            null
                    );
                }
        );


        card.getChildren().addAll(
                info,
                spacer,
                allocateButton
        );


        return card;
    }


    // =====================================================
    // SHOW ALLOCATION DIALOG
    // =====================================================

    private void showAllocationDialog(

            Container selectedContainer,

            TableView<Container> table

    ) {

        Dialog<ButtonType> dialog =
                new Dialog<>();


        boolean isReallocation =
                selectedContainer != null
                &&
                "Allocated".equals(
                        selectedContainer.getStatus()
                );


        if (isReallocation) {

            dialog.setTitle(
                    "Reallocate Container"
            );

            dialog.setHeaderText(
                    "Reallocate Container "
                    + selectedContainer.getContainerId()
            );

        } else {

            dialog.setTitle(
                    "Allocate Container"
            );

            dialog.setHeaderText(
                    "Allocate Container"
            );
        }


        // =====================================================
        // CONTAINER SELECTION
        // =====================================================

        ComboBox<Container> containerBox =
                new ComboBox<>();


        containerBox.setPrefWidth(
                250
        );


        // =================================================
        // ONLY AVAILABLE/PENDING CONTAINERS
        // =================================================

        for (Container container : containerData) {

            if (container == null) {
                continue;
            }


            if ("Available".equals(
                    container.getStatus()
            )
                    ||
                    "Pending".equals(
                            container.getStatus()
                    )
                    ||
                    container == selectedContainer) {

                containerBox.getItems().add(
                        container
                );
            }
        }


        containerBox.setCellFactory(
                listView ->
                        new ListCell<Container>() {

                            @Override
                            protected void updateItem(
                                    Container item,
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

                                            item.getContainerId()
                                            + " - "
                                            + safeValue(
                                                    item.getType()
                                            )
                                            + " - "
                                            + safeValue(
                                                    item.getSize()
                                            )
                                    );
                                }
                            }
                        }
        );


        containerBox.setButtonCell(
                new ListCell<Container>() {

                    @Override
                    protected void updateItem(
                            Container item,
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
                                    item.getContainerId()
                            );
                        }
                    }
                }
        );


        if (selectedContainer != null) {

            containerBox.setValue(
                    selectedContainer
            );

        } else if (!containerBox.getItems().isEmpty()) {

            containerBox.setValue(
                    containerBox.getItems().get(0)
            );
        }


        // =====================================================
        // CARGO
        // =====================================================

        TextField cargoIdField =
                new TextField();


        cargoIdField.setPromptText(
                "Cargo ID"
        );


        // =====================================================
        // SHIP
        // =====================================================

        TextField shipField =
                new TextField();


        shipField.setPromptText(
                "Ship"
        );


        // =====================================================
        // LOCATION
        // =====================================================

        TextField locationField =
                new TextField();


        locationField.setPromptText(
                "Location"
        );


        // =====================================================
        // ALLOCATION DATE PICKER
        // =====================================================

        DatePicker allocationDatePicker =
                new DatePicker();


        // =================================================
        // NEW ALLOCATION
        // =================================================

        allocationDatePicker.setValue(
                LocalDate.now()
        );


        // =====================================================
        // STATUS
        // =====================================================

        ComboBox<String> statusBox =
                new ComboBox<>();


        statusBox.getItems().addAll(
                "Allocated",
                "Pending"
        );


        statusBox.setValue(
                "Allocated"
        );


        // =================================================
        // IF REALLOCATION
        // =================================================

        if (selectedContainer != null) {

            cargoIdField.setText(
                    safeValueForField(
                            selectedContainer.getCargoId()
                    )
            );


            shipField.setText(
                    safeValueForField(
                            selectedContainer.getShip()
                    )
            );


            locationField.setText(
                    safeValueForField(
                            selectedContainer.getLocation()
                    )
            );


            // =============================================
            // LOAD EXISTING ALLOCATION DATE
            // =============================================

            String existingDate =
                    selectedContainer.getAllocationDate();


            if (existingDate != null
                    &&
                    !existingDate.isEmpty()) {

                try {

                    allocationDatePicker.setValue(
                            LocalDate.parse(
                                    existingDate
                            )
                    );

                } catch (Exception e) {

                    allocationDatePicker.setValue(
                            LocalDate.now()
                    );
                }
            }


            // =============================================
            // LOAD EXISTING STATUS
            // =============================================

            if ("Pending".equals(
                    selectedContainer.getStatus()
            )) {

                statusBox.setValue(
                        "Pending"
                );
            }
        }


        // =====================================================
        // FORM
        // =====================================================

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
                new Label("Container:"),
                0,
                0
        );


        grid.add(
                containerBox,
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


        // =====================================================
        // DATE PICKER IN FORM
        // =====================================================

        grid.add(
                new Label("Allocation Date:"),
                0,
                4
        );


        grid.add(
                allocationDatePicker,
                1,
                4
        );


        grid.add(
                new Label("Status:"),
                0,
                5
        );


        grid.add(
                statusBox,
                1,
                5
        );


        dialog.getDialogPane().setContent(
                grid
        );


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


        // =====================================================
        // SAVE ALLOCATION
        // =====================================================

        dialog.setResultConverter(
                button -> {

                    if (button != saveButton) {
                        return button;
                    }


                    // =============================================
                    // VALIDATE CONTAINER
                    // =============================================

                    Container container =
                            containerBox.getValue();


                    if (container == null) {

                        showWarning(
                                "Please select a container."
                        );

                        return null;
                    }


                    // =============================================
                    // VALIDATE REQUIRED FIELDS
                    // =============================================

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


                    if (cargoId.isEmpty()
                            ||
                            ship.isEmpty()
                            ||
                            location.isEmpty()) {

                        showWarning(
                                "Please fill in Cargo ID, Ship and Location."
                        );

                        return null;
                    }


                    // =============================================
                    // VALIDATE DATE
                    // =============================================

                    LocalDate selectedDate =
                            allocationDatePicker.getValue();


                    if (selectedDate == null) {

                        showWarning(
                                "Please select an allocation date."
                        );

                        return null;
                    }


                    // =============================================
                    // UPDATE CONTAINER
                    // =============================================

                    container.setCargoId(
                            cargoId
                    );


                    container.setShip(
                            ship
                    );


                    container.setLocation(
                            location
                    );


                    container.setStatus(
                            statusBox.getValue()
                    );


                    // =============================================
                    // SAVE SELECTED DATE
                    // =============================================

                    container.setAllocationDate(
                            selectedDate.toString()
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

                    if (isReallocation) {

                        notificationController.createNotification(

                                "Container reallocated",

                                container.getContainerId()
                                        +
                                        " was reallocated successfully.",

                                "EDIT"
                        );

                    } else {

                        notificationController.createNotification(

                                "Container allocated",

                                container.getContainerId()
                                        +
                                        " was allocated successfully.",

                                "ADD"
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


                    refreshContainerCards();


                    // =============================================
                    // SUCCESS MESSAGE
                    // =============================================

                    showSuccess(

                            isReallocation
                                    ? "Container reallocated successfully."
                                    : "Container allocated successfully."
                    );


                    return button;
                }
        );


        dialog.showAndWait();
    }


    // =====================================================
    // SHOW CONTAINER DETAILS
    // =====================================================

    private void showContainerDetails(
            Container container
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );


        alert.setTitle(
                "Container Details"
        );


        alert.setHeaderText(
                "Container "
                + container.getContainerId()
        );


        alert.setContentText(

                "Container ID: "
                + safeValue(
                        container.getContainerId()
                )

                + "\n\n"

                + "Cargo ID: "
                + safeValue(
                        container.getCargoId()
                )

                + "\n\n"

                + "Ship: "
                + safeValue(
                        container.getShip()
                )

                + "\n\n"

                + "Type: "
                + safeValue(
                        container.getType()
                )

                + "\n\n"

                + "Size: "
                + safeValue(
                        container.getSize()
                )

                + "\n\n"

                + "Weight: "
                + safeValue(
                        container.getWeight()
                )

                + "\n\n"

                + "Location: "
                + safeValue(
                        container.getLocation()
                )

                + "\n\n"

                + "Allocation Date: "
                + safeValue(
                        container.getAllocationDate()
                )

                + "\n\n"

                + "Status: "
                + safeValue(
                        container.getStatus()
                )
        );


        alert.showAndWait();
    }


    // =====================================================
    // UPDATE SEARCH + STATUS FILTER
    // =====================================================

    private void updateFilter(

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

                            contains(
                                    container.getContainerId(),
                                    search
                            )

                            ||

                            contains(
                                    container.getCargoId(),
                                    search
                            )

                            ||

                            contains(
                                    container.getShip(),
                                    search
                            )

                            ||

                            contains(
                                    container.getType(),
                                    search
                            )

                            ||

                            contains(
                                    container.getSize(),
                                    search
                            )

                            ||

                            contains(
                                    container.getWeight(),
                                    search
                            )

                            ||

                            contains(
                                    container.getLocation(),
                                    search
                            )

                            ||

                            contains(
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

                            selectedStatus.equals(
                                    container.getStatus()
                            );


                    return matchesSearch
                            &&
                            matchesStatus;
                }
        );
    }


    // =====================================================
    // SAFE STRING SEARCH
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
    // SHOW WARNING
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
    // SHOW SUCCESS
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


    // =====================================================
    // CREATE TABLE COLUMN
    // =====================================================

    private TableColumn<Container, String> createColumn(

            String title,
            double width,
            Function<Container, String> getter

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
    // STYLE NORMAL TABLE COLUMN
    // =====================================================

    private void styleAllocationColumn(
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
    // STYLE BOLD TABLE COLUMN
    // =====================================================

    private void styleAllocationBoldColumn(
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
    // MODERN TABLE HEADER STYLE
    // =====================================================

    private void styleAllocationTableHeader(
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
    // GET PAGE CONTENT
    // =====================================================

    public VBox getContent() {

        return content;
    }
}