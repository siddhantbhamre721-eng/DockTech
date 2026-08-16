package com.projectui.view.portauthority;

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


// =====================================================
// CONTAINER ALLOCATION PAGE
// =====================================================

public class ContainerAllocationPage {

    // =====================================================
    // MAIN PAGE
    // =====================================================

    private VBox root;


    // =====================================================
    // ALLOCATION DATA
    // =====================================================

    private ObservableList<String[]> allocationData =
            FXCollections.observableArrayList();


    // =====================================================
    // CONTAINER DATA
    // =====================================================

    private ObservableList<String[]> containerData =
            FXCollections.observableArrayList();


    // =====================================================
    // SUMMARY LABELS
    // These labels will be updated when allocation happens
    // =====================================================

    private Label totalContainersValue;
    private Label allocatedValue;
    private Label availableValue;
    private Label pendingValue;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public ContainerAllocationPage() {

        root = new VBox();

        root.setSpacing(20);
        root.setPadding(new Insets(28));

        root.setStyle(
                "-fx-background-color: #f3f8ff;"
        );


        // =================================================
        // PAGE TITLE
        // =================================================

        Label title =
                new Label("Container Allocation");

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


        Label subtitle =
                new Label(
                        "Allocate containers to cargo shipments and vessels."
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
        // ALLOCATION FLOW
        // =================================================

        VBox allocationFlow =
                createAllocationFlow();


        // =================================================
        // CONTAINER CARDS
        // =================================================

        HBox containerCards =
                createContainerCards();


        // =================================================
        // ALLOCATION RECORDS
        // =================================================

        VBox allocationRecords =
                createAllocationRecords();


        // =================================================
        // ADD EVERYTHING TO PAGE
        // =================================================

        root.getChildren().addAll(
                titleBox,
                summaryCards,
                allocationFlow,
                containerCards,
                allocationRecords
        );
    }


    // =====================================================
    // GET PAGE CONTENT
    // =====================================================

    public VBox getContent() {

        return root;
    }


    // =====================================================
    // SUMMARY CARDS
    // =====================================================

    private HBox createSummaryCards() {

        HBox cards =
                new HBox();

        cards.setSpacing(18);

        cards.setFillHeight(true);


        // -------------------------------------------------
        // TOTAL CONTAINERS
        // -------------------------------------------------

        totalContainersValue =
                new Label("8,760");

        VBox totalCard =
                createSummaryCard(
                        "Total Containers",
                        totalContainersValue,
                        "Across all terminals",
                        "#7b879c"
                );


        // -------------------------------------------------
        // ALLOCATED
        // -------------------------------------------------

        allocatedValue =
                new Label("6,420");

        VBox allocatedCard =
                createSummaryCard(
                        "Allocated",
                        allocatedValue,
                        "73% utilization",
                        "#00a878"
                );


        // -------------------------------------------------
        // AVAILABLE
        // -------------------------------------------------

        availableValue =
                new Label("1,850");

        VBox availableCard =
                createSummaryCard(
                        "Available",
                        availableValue,
                        "Ready to allocate",
                        "#2878f0"
                );


        // -------------------------------------------------
        // PENDING ALLOCATION
        // -------------------------------------------------

        pendingValue =
                new Label("490");

        VBox pendingCard =
                createSummaryCard(
                        "Pending Allocation",
                        pendingValue,
                        "Awaiting assignment",
                        "#ff8a00"
                );


        cards.getChildren().addAll(
                totalCard,
                allocatedCard,
                availableCard,
                pendingCard
        );


        // Make all cards equal width
        for (Node card : cards.getChildren()) {

            HBox.setHgrow(
                    card,
                    Priority.ALWAYS
            );
        }


        return cards;
    }


    // =====================================================
    // CREATE ONE SUMMARY CARD
    // =====================================================

    private VBox createSummaryCard(
            String heading,
            Label value,
            String subtitle,
            String subtitleColor
    ) {

        Label headingLabel =
                new Label(heading.toUpperCase());

        headingLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.NORMAL,
                        13
                )
        );

        headingLabel.setTextFill(
                Color.web("#7b879c")
        );


        value.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        28
                )
        );

        value.setTextFill(
                Color.web("#12203a")
        );


        Label subtitleLabel =
                new Label(subtitle);

        subtitleLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.NORMAL,
                        13
                )
        );

        subtitleLabel.setTextFill(
                Color.web(subtitleColor)
        );


        VBox card =
                new VBox(
                        10,
                        headingLabel,
                        value,
                        subtitleLabel
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
    // ALLOCATION FLOW
    // =====================================================

    private VBox createAllocationFlow() {

        VBox panel =
                new VBox();

        panel.setSpacing(18);

        panel.setPadding(
                new Insets(22)
        );

        panel.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #dce5f0;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );


        // -------------------------------------------------
        // HEADER
        // -------------------------------------------------

        HBox header =
                new HBox();

        header.setAlignment(
                Pos.CENTER_LEFT
        );


        VBox headingBox =
                new VBox(
                        5
                );


        Label heading =
                new Label(
                        "Allocation Flow"
                );

        heading.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        18
                )
        );

        heading.setTextFill(
                Color.web("#12203a")
        );


        Label description =
                new Label(
                        "Follow the steps to allocate a container."
                );

        description.setTextFill(
                Color.web("#7b879c")
        );


        headingBox.getChildren().addAll(
                heading,
                description
        );


        // -------------------------------------------------
        // ALLOCATE BUTTON
        // -------------------------------------------------

        Button allocateButton =
                new Button(
                        "+  Allocate Container"
                );

        allocateButton.setStyle(
                "-fx-background-color: #2878f0;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 18 10 18;" +
                "-fx-background-radius: 6;"
        );


        allocateButton.setOnAction(
                event -> showAllocateDialog()
        );


        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );


        header.getChildren().addAll(
                headingBox,
                spacer,
                allocateButton
        );


        // -------------------------------------------------
        // FLOW STEPS
        // -------------------------------------------------

        HBox flow =
                new HBox();

        flow.setSpacing(10);

        flow.setAlignment(
                Pos.CENTER
        );


        VBox step1 =
                createFlowStep(
                        "▣",
                        "Select Cargo"
                );


        Label arrow1 =
                createArrow();


        VBox step2 =
                createFlowStep(
                        "▣",
                        "Select Container"
                );


        Label arrow2 =
                createArrow();


        VBox step3 =
                createFlowStep(
                        "♙",
                        "Select Ship"
                );


        Label arrow3 =
                createArrow();


        VBox step4 =
                createFlowStep(
                        "⌖",
                        "Select Location"
                );


        Label arrow4 =
                createArrow();


        VBox step5 =
                createFlowStep(
                        "✓",
                        "Allocate"
                );


        flow.getChildren().addAll(
                step1,
                arrow1,
                step2,
                arrow2,
                step3,
                arrow3,
                step4,
                arrow4,
                step5
        );


        panel.getChildren().addAll(
                header,
                flow
        );


        return panel;
    }


    // =====================================================
    // CREATE ONE FLOW STEP
    // =====================================================

    private VBox createFlowStep(
            String icon,
            String text
    ) {

        Label iconLabel =
                new Label(icon);

        iconLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        24
                )
        );

        iconLabel.setTextFill(
                Color.web("#2878f0")
        );


        Label textLabel =
                new Label(text);

        textLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        textLabel.setTextFill(
                Color.web("#12203a")
        );


        VBox box =
                new VBox(
                        10,
                        iconLabel,
                        textLabel
                );

        box.setAlignment(
                Pos.CENTER
        );

        box.setPrefSize(
                130,
                90
        );

        box.setStyle(
                "-fx-background-color: #ffffff;" +
                "-fx-border-color: #eef2f7;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );


        return box;
    }


    // =====================================================
    // FLOW ARROW
    // =====================================================

    private Label createArrow() {

        Label arrow =
                new Label("→");

        arrow.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        25
                )
        );

        arrow.setTextFill(
                Color.web("#2878f0")
        );


        return arrow;
    }


    // =====================================================
    // CONTAINER CARDS
    // =====================================================

    private HBox createContainerCards() {

        HBox cards =
                new HBox();

        cards.setSpacing(15);


        // -------------------------------------------------
        // CONTAINER 1
        // -------------------------------------------------

        VBox card1 =
                createContainerCard(
                        "CONT-1024",
                        "40 ft Container",
                        "CGO-1287",
                        "Oceanic Star",
                        "Terminal A",
                        "Allocated"
                );


        // -------------------------------------------------
        // CONTAINER 2
        // -------------------------------------------------

        VBox card2 =
                createContainerCard(
                        "CONT-1031",
                        "20 ft Container",
                        "—",
                        "Unassigned",
                        "Yard B",
                        "Available"
                );


        // -------------------------------------------------
        // CONTAINER 3
        // -------------------------------------------------

        VBox card3 =
                createContainerCard(
                        "CONT-1048",
                        "40 ft Container",
                        "CGO-1302",
                        "Blue Marlin",
                        "Berth 4",
                        "In Transit"
                );


        // -------------------------------------------------
        // CONTAINER 4
        // -------------------------------------------------

        VBox card4 =
                createContainerCard(
                        "CONT-1055",
                        "45 ft Container",
                        "CGO-1319",
                        "Unassigned",
                        "Gate 2",
                        "Pending"
                );


        cards.getChildren().addAll(
                card1,
                card2,
                card3,
                card4
        );


        return cards;
    }


    // =====================================================
    // CREATE ONE CONTAINER CARD
    // =====================================================

    private VBox createContainerCard(
            String containerId,
            String size,
            String cargoId,
            String ship,
            String location,
            String status
    ) {

        VBox card =
                new VBox();

        card.setSpacing(8);

        card.setPadding(
                new Insets(16)
        );

        card.setPrefWidth(230);

        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #dce5f0;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );


        // -------------------------------------------------
        // CONTAINER ID
        // -------------------------------------------------

        Label idLabel =
                new Label(containerId);

        idLabel.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        15
                )
        );

        idLabel.setTextFill(
                Color.web("#12203a")
        );


        // -------------------------------------------------
        // SIZE
        // -------------------------------------------------

        Label sizeLabel =
                new Label(size);

        sizeLabel.setTextFill(
                Color.web("#7b879c")
        );


        // -------------------------------------------------
        // CARGO
        // -------------------------------------------------

        Label cargoLabel =
                new Label(
                        "Cargo: " + cargoId
                );


        // -------------------------------------------------
        // SHIP
        // -------------------------------------------------

        Label shipLabel =
                new Label(
                        "Ship: " + ship
                );


        // -------------------------------------------------
        // LOCATION
        // -------------------------------------------------

        Label locationLabel =
                new Label(
                        "Location: " + location
                );


        // -------------------------------------------------
        // STATUS
        // -------------------------------------------------

        Label statusLabel =
                new Label(status);

        statusLabel.setPadding(
                new Insets(
                        5,
                        10,
                        5,
                        10
                )
        );


        String statusColor =
                "#2878f0";


        if (status.equals("Allocated")) {

            statusColor = "#00a878";

        } else if (status.equals("In Transit")) {

            statusColor = "#ff8a00";

        } else if (status.equals("Pending")) {

            statusColor = "#8a8f98";
        }


        statusLabel.setTextFill(
                Color.web(statusColor)
        );

        statusLabel.setStyle(
                "-fx-background-color: #f2f5f9;" +
                "-fx-background-radius: 10;"
        );


        card.getChildren().addAll(
                idLabel,
                sizeLabel,
                cargoLabel,
                shipLabel,
                locationLabel,
                statusLabel
        );


        return card;
    }


    // =====================================================
    // ALLOCATION RECORDS
    // =====================================================

    private VBox createAllocationRecords() {

        VBox panel =
                new VBox();

        panel.setSpacing(15);

        panel.setPadding(
                new Insets(22)
        );

        panel.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #dce5f0;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );


        // -------------------------------------------------
        // HEADER
        // -------------------------------------------------

        HBox header =
                new HBox();

        header.setAlignment(
                Pos.CENTER_LEFT
        );


        VBox headingBox =
                new VBox(
                        5
                );


        Label heading =
                new Label(
                        "Allocation Records"
                );

        heading.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        18
                )
        );


        Label description =
                new Label(
                        "Manage current container allocations."
                );

        description.setTextFill(
                Color.web("#7b879c")
        );


        headingBox.getChildren().addAll(
                heading,
                description
        );


        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );


        Button allocateButton =
                new Button(
                        "+  Allocate Container"
                );


        allocateButton.setStyle(
                "-fx-background-color: #2878f0;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10 18 10 18;" +
                "-fx-background-radius: 6;"
        );


        allocateButton.setOnAction(
                event -> showAllocateDialog()
        );


        header.getChildren().addAll(
                headingBox,
                spacer,
                allocateButton
        );


        // =================================================
        // SEARCH AND FILTER
        // =================================================

        HBox filters =
                new HBox();

        filters.setSpacing(10);


        TextField searchField =
                new TextField();

        searchField.setPromptText(
                "Search Container..."
        );

        searchField.setPrefWidth(220);


        ComboBox<String> statusFilter =
                new ComboBox<>();

        statusFilter.getItems().addAll(
                "All Statuses",
                "Allocated",
                "Available",
                "In Transit",
                "Pending"
        );

        statusFilter.setValue(
                "All Statuses"
        );


        filters.getChildren().addAll(
                searchField,
                statusFilter
        );


        // =================================================
        // TABLE
        // =================================================

        TableView<String[]> table =
                new TableView<>();

        table.setPrefHeight(280);


        // -------------------------------------------------
        // TABLE COLUMNS
        // -------------------------------------------------

        TableColumn<String[], String> containerId =
                createColumn(
                        "Container ID",
                        110,
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
                        130,
                        2
                );


        TableColumn<String[], String> type =
                createColumn(
                        "Type",
                        80,
                        3
                );


        TableColumn<String[], String> location =
                createColumn(
                        "Location",
                        110,
                        4
                );


        TableColumn<String[], String> date =
                createColumn(
                        "Allocation Date",
                        110,
                        5
                );


        TableColumn<String[], String> status =
                createColumn(
                        "Status",
                        110,
                        6
                );


        // =================================================
        // ACTION COLUMN
        // =================================================

        TableColumn<String[], String> action =
                new TableColumn<>(
                        "Action"
                );

        action.setPrefWidth(150);


        action.setCellFactory(
                column ->
                        new TableCell<String[], String>() {

                            private final Button reallocateButton =
                                    new Button("Reallocate");

                            private final Button viewButton =
                                    new Button("View");


                            {

                                // ---------------------------------
                                // VIEW BUTTON
                                // ---------------------------------

                                viewButton.setOnAction(
                                        event -> {

                                            String[] record =
                                                    getTableView()
                                                            .getItems()
                                                            .get(getIndex());


                                            Alert alert =
                                                    new Alert(
                                                            Alert.AlertType.INFORMATION
                                                    );


                                            alert.setTitle(
                                                    "Allocation Details"
                                            );


                                            alert.setHeaderText(
                                                    "Allocation Details"
                                            );


                                            alert.setContentText(
                                                    "Container ID: " + record[0] + "\n" +
                                                    "Cargo ID: " + record[1] + "\n" +
                                                    "Ship: " + record[2] + "\n" +
                                                    "Type: " + record[3] + "\n" +
                                                    "Location: " + record[4] + "\n" +
                                                    "Allocation Date: " + record[5] + "\n" +
                                                    "Status: " + record[6]
                                            );


                                            alert.showAndWait();
                                        }
                                );


                                // ---------------------------------
                                // REALLOCATE BUTTON
                                // ---------------------------------

                                reallocateButton.setOnAction(
                                        event -> {

                                            String[] record =
                                                    getTableView()
                                                            .getItems()
                                                            .get(getIndex());


                                            TextInputDialog dialog =
                                                    new TextInputDialog(
                                                            record[4]
                                                    );


                                            dialog.setTitle(
                                                    "Reallocate Container"
                                            );


                                            dialog.setHeaderText(
                                                    "Change Container Location"
                                            );


                                            dialog.setContentText(
                                                    "New Location:"
                                            );


                                            dialog.showAndWait()
                                                    .ifPresent(
                                                            newLocation -> {

                                                                if (
                                                                        newLocation
                                                                                .trim()
                                                                                .isEmpty()
                                                                ) {

                                                                    showError(
                                                                            "Location cannot be empty."
                                                                    );

                                                                    return;
                                                                }


                                                                record[4] =
                                                                        newLocation.trim();


                                                                table.refresh();
                                                            }
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

                                    setGraphic(null);

                                } else {

                                    HBox buttons =
                                            new HBox(
                                                    5,
                                                    reallocateButton,
                                                    viewButton
                                            );

                                    setGraphic(
                                            buttons
                                    );
                                }
                            }
                        }
        );


        table.getColumns().addAll(
                containerId,
                cargoId,
                ship,
                type,
                location,
                date,
                status,
                action
        );


        // =================================================
        // SAMPLE ALLOCATION DATA
        // =================================================

        allocationData.addAll(

                new String[]{
                        "CONT-1024",
                        "CGO-1287",
                        "Oceanic Star",
                        "40 ft",
                        "Terminal A",
                        "12 Feb 2026",
                        "Allocated"
                },

                new String[]{
                        "CONT-1031",
                        "—",
                        "Unassigned",
                        "20 ft",
                        "Yard B",
                        "—",
                        "Available"
                },

                new String[]{
                        "CONT-1048",
                        "CGO-1302",
                        "Blue Marlin",
                        "40 ft",
                        "Berth 4",
                        "13 Feb 2026",
                        "In Transit"
                },

                new String[]{
                        "CONT-1055",
                        "CGO-1319",
                        "Unassigned",
                        "45 ft",
                        "Gate 2",
                        "—",
                        "Pending"
                }
        );


        // =================================================
        // FILTERED LIST
        // =================================================

        FilteredList<String[]> filteredData =
                new FilteredList<>(
                        allocationData
                );


        table.setItems(
                filteredData
        );


        // =================================================
        // SEARCH FUNCTIONALITY
        // =================================================

        searchField.textProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {

                            updateAllocationFilter(
                                    filteredData,
                                    searchField.getText(),
                                    statusFilter.getValue()
                            );
                        }
                );


        // =================================================
        // STATUS FILTER FUNCTIONALITY
        // =================================================

        statusFilter.valueProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {

                            updateAllocationFilter(
                                    filteredData,
                                    searchField.getText(),
                                    statusFilter.getValue()
                            );
                        }
                );


        panel.getChildren().addAll(
                header,
                filters,
                table
        );


        return panel;
    }


    // =====================================================
    // FILTER ALLOCATION RECORDS
    // =====================================================

    private void updateAllocationFilter(
            FilteredList<String[]> filteredData,
            String searchText,
            String selectedStatus
    ) {

        String search =
                searchText
                        .toLowerCase()
                        .trim();


        filteredData.setPredicate(
                record -> {

                    // -----------------------------------------
                    // SEARCH
                    // -----------------------------------------

                    boolean matchesSearch =
                            search.isEmpty();


                    if (!matchesSearch) {

                        for (String value : record) {

                            if (
                                    value
                                            .toLowerCase()
                                            .contains(search)
                            ) {

                                matchesSearch = true;

                                break;
                            }
                        }
                    }


                    // -----------------------------------------
                    // STATUS FILTER
                    // -----------------------------------------

                    boolean matchesStatus =
                            selectedStatus == null ||
                            selectedStatus.equals("All Statuses") ||
                            record[6].equals(selectedStatus);


                    return matchesSearch &&
                           matchesStatus;
                }
        );
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
                data -> {

                    String[] row =
                            data.getValue();

                    return new javafx.beans.property.SimpleStringProperty(
                            row[index]
                    );
                }
        );


        return column;
    }


    // =====================================================
    // ALLOCATE CONTAINER DIALOG
    // =====================================================

    private void showAllocateDialog() {

        Dialog<ButtonType> dialog =
                new Dialog<>();


        dialog.setTitle(
                "Allocate Container"
        );


        dialog.setHeaderText(
                "Allocate Container"
        );


        // =================================================
        // FORM FIELDS
        // =================================================

        TextField containerField =
                new TextField();

        containerField.setPromptText(
                "Container ID"
        );


        TextField cargoField =
                new TextField();

        cargoField.setPromptText(
                "Cargo ID"
        );


        TextField shipField =
                new TextField();

        shipField.setPromptText(
                "Ship"
        );


        TextField typeField =
                new TextField();

        typeField.setPromptText(
                "Type"
        );


        TextField locationField =
                new TextField();

        locationField.setPromptText(
                "Location"
        );


        DatePicker datePicker =
                new DatePicker();


        // =================================================
        // FORM LAYOUT
        // =================================================

        GridPane form =
                new GridPane();

        form.setHgap(10);

        form.setVgap(12);

        form.setPadding(
                new Insets(15)
        );


        form.add(
                new Label("Container ID:"),
                0,
                0
        );

        form.add(
                containerField,
                1,
                0
        );


        form.add(
                new Label("Cargo ID:"),
                0,
                1
        );

        form.add(
                cargoField,
                1,
                1
        );


        form.add(
                new Label("Ship:"),
                0,
                2
        );

        form.add(
                shipField,
                1,
                2
        );


        form.add(
                new Label("Type:"),
                0,
                3
        );

        form.add(
                typeField,
                1,
                3
        );


        form.add(
                new Label("Location:"),
                0,
                4
        );

        form.add(
                locationField,
                1,
                4
        );


        form.add(
                new Label("Allocation Date:"),
                0,
                5
        );

        form.add(
                datePicker,
                1,
                5
        );


        dialog.getDialogPane()
                .setContent(
                        form
                );


        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        ButtonType.OK,
                        ButtonType.CANCEL
                );


        // =================================================
        // OK BUTTON
        // =================================================

        Button okButton =
                (Button) dialog.getDialogPane()
                        .lookupButton(
                                ButtonType.OK
                        );


        okButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {

                    // -----------------------------------------
                    // VALIDATE EMPTY FIELDS
                    // -----------------------------------------

                    if (
                            containerField
                                    .getText()
                                    .trim()
                                    .isEmpty() ||

                            cargoField
                                    .getText()
                                    .trim()
                                    .isEmpty() ||

                            shipField
                                    .getText()
                                    .trim()
                                    .isEmpty() ||

                            typeField
                                    .getText()
                                    .trim()
                                    .isEmpty() ||

                            locationField
                                    .getText()
                                    .trim()
                                    .isEmpty() ||

                            datePicker.getValue() == null
                    ) {

                        showError(
                                "Please fill all fields."
                        );


                        event.consume();

                        return;
                    }


                    // -----------------------------------------
                    // ADD NEW ALLOCATION
                    // -----------------------------------------

                    String date =
                            datePicker
                                    .getValue()
                                    .format(
                                            java.time.format.DateTimeFormatter
                                                    .ofPattern(
                                                            "dd MMM yyyy"
                                                    )
                                    );


                    allocationData.add( 0,
                            new String[]{
                                    containerField.getText().trim(),

                                    cargoField.getText().trim(),

                                    shipField.getText().trim(),

                                    typeField.getText().trim(),

                                    locationField.getText().trim(),

                                    date,

                                    "Allocated"
                            }
                    );


                    // -----------------------------------------
                    // UPDATE SUMMARY VALUES
                    // -----------------------------------------

                    updateSummaryAfterAllocation();


                    // -----------------------------------------
                    // SUCCESS MESSAGE
                    // -----------------------------------------

                    Alert alert =
                            new Alert(
                                    Alert.AlertType.INFORMATION
                            );


                    alert.setTitle(
                            "Allocation Successful"
                    );


                    alert.setHeaderText(
                            "Container Allocated"
                    );


                    alert.setContentText(
                            "Container " +
                            containerField
                                    .getText()
                                    .trim() +
                            " has been allocated successfully."
                    );


                    alert.showAndWait();
                }
        );


        dialog.showAndWait();
    }


    // =====================================================
    // UPDATE SUMMARY AFTER ALLOCATION
    // =====================================================

    private void updateSummaryAfterAllocation() {

        int allocated =
                Integer.parseInt(
                        allocatedValue
                                .getText()
                                .replace(",", "")
                );


        int available =
                Integer.parseInt(
                        availableValue
                                .getText()
                                .replace(",", "")
                );


        int pending =
                Integer.parseInt(
                        pendingValue
                                .getText()
                                .replace(",", "")
                );


        if (available > 0) {

            available--;

            allocated++;
        }


        if (pending > 0) {

            pending--;
        }


        allocatedValue.setText(
                String.format(
                        "%,d",
                        allocated
                )
        );


        availableValue.setText(
                String.format(
                        "%,d",
                        available
                )
        );


        pendingValue.setText(
                String.format(
                        "%,d",
                        pending
                )
        );
    }


    // =====================================================
    // ERROR MESSAGE
    // =====================================================

    private void showError(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );


        alert.setTitle(
                "Invalid Input"
        );


        alert.setHeaderText(
                "Please check your input"
        );


        alert.setContentText(
                message
        );


        alert.showAndWait();
    }
}