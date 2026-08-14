package com.projectui.ShipCaptain;

import java.io.File;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

public class UploadDocuments {
    private final BorderPane root;
    private final Scene scene;

    private ComboBox<String> documentType;
    private Label fileName;
    private File selectedFile;

    // Main upload content
    private VBox main;

    public UploadDocuments() {

        root = new BorderPane();

        root.setStyle(
                "-fx-background-color:#f3f8fc;");

        // =====================================================
        // SIDEBAR
        // =====================================================

        VBox sidebar = new VBox(8);

        sidebar.setPrefWidth(290);

        sidebar.setPadding(
                new Insets(30, 22, 25, 22));

        sidebar.setStyle(
                "-fx-background-color:#071a38;");

        Label logo = label(
                "⚓  DOCK TECH",
                22,
                true);

        logo.setTextFill(Color.WHITE);

        Label logoSub = label(
                "SMART PORT OPERATIONS",
                10,
                true);

        logoSub.setTextFill(
                Color.web("#8192ad"));

        VBox logoBox = new VBox(
                5,
                logo,
                logoSub);

        logoBox.setPadding(
                new Insets(0, 5, 30, 5));

        Button dashboard = sideButton(
                "⌂",
                "Dashboard");

        Button assigned = sideButton(
                "▣",
                "View Assigned Ship");

        Button update = sideButton(
                "⟳",
                "Update Ship Status");

        Button loading = sideButton(
                "◉",
                "Confirm Loading");

        Button upload = sideButton(
                "▤",
                "Upload Documents");

        Button sos = sideButton(
                "△",
                "Send SOS");

        upload.setStyle(
                activeStyle());

        Region space = new Region();

        VBox.setVgrow(
                space,
                Priority.ALWAYS);

        Button logout = sideButton(
                "↪",
                "Logout");

        // =====================================================
        // PROFILE
        // =====================================================

        HBox profile = new HBox(12);

        profile.setAlignment(
                Pos.CENTER_LEFT);

        profile.setPadding(
                new Insets(12));

        profile.setStyle(
                "-fx-background-color:#10284c;" +
                        "-fx-background-radius:12;");

        Label avatar = new Label("JS");

        avatar.setPrefSize(
                45,
                45);

        avatar.setAlignment(
                Pos.CENTER);

        avatar.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13));

        avatar.setTextFill(
                Color.WHITE);

        avatar.setStyle(
                "-fx-background-color:#13b9e8;" +
                        "-fx-background-radius:50;");

        Label name = label(
                "John Smith",
                12,
                true);

        name.setTextFill(
                Color.WHITE);

        Label role = label(
                "Ship Captain",
                11,
                false);

        role.setTextFill(
                Color.web("#8da0bd"));

        profile.getChildren().addAll(
                avatar,
                new VBox(
                        3,
                        name,
                        role));

        sidebar.getChildren().addAll(
                logoBox,
                dashboard,
                assigned,
                update,
                loading,
                upload,
                sos,
                space,
                logout,
                profile);

        root.setLeft(sidebar);

        // =====================================================
        // HEADER
        // =====================================================

        HBox header = new HBox(18);

        header.setAlignment(
                Pos.CENTER_LEFT);

        header.setPadding(
                new Insets(
                        22,
                        35,
                        18,
                        35));

        header.setPrefHeight(
                105);

        header.setStyle(
                "-fx-background-color:white;" +
                        "-fx-border-color:#e5ebf2;");

        VBox heading = new VBox(
                3,
                label(
                        "Upload Documents",
                        29,
                        true),
                label(
                        "Upload required ship and voyage documents.",
                        12,
                        false));

        Region headerSpace = new Region();

        HBox.setHgrow(
                headerSpace,
                Priority.ALWAYS);

        TextField search = new TextField();

        search.setPromptText(
                "⌕  Search...");

        search.setPrefSize(
                250,
                42);

        search.setStyle(
                fieldStyle());

        Label notification = label(
                "♧",
                22,
                false);

        Label user = label(
                "JS   Ship Captain  ⌄",
                12,
                true);

        header.getChildren().addAll(
                heading,
                headerSpace,
                search,
                notification,
                user);

        root.setTop(header);

        // =====================================================
        // MAIN CONTENT
        // =====================================================

        main = new VBox(24);

        main.setPadding(
                new Insets(
                        35,
                        70,
                        40,
                        70));

        // =====================================================
        // DOCUMENT TYPE CARD
        // =====================================================

        VBox typeCard = new VBox(10);

        typeCard.setPadding(
                new Insets(25));

        typeCard.setStyle(
                cardStyle());

        Label typeTitle = label(
                "▣  Document Type",
                18,
                true);

        Label typeSub = label(
                "Choose the type of document you want to upload.",
                12,
                false);

        documentType = new ComboBox<>();

        documentType.setPromptText(
                "Select Document Type");

        documentType.setMaxWidth(
                Double.MAX_VALUE);

        documentType.setPrefHeight(
                45);

        documentType.getItems().addAll(
                "Bill of Lading",
                "Cargo Manifest",
                "Ship Clearance",
                "Port Entry Permit",
                "Safety Certificate");

        typeCard.getChildren().addAll(
                typeTitle,
                typeSub,
                documentType);

        // =====================================================
        // FILE CARD
        // =====================================================

        VBox fileCard = new VBox(15);

        fileCard.setPadding(
                new Insets(30));

        fileCard.setStyle(
                cardStyle());

        Label fileTitle = label(
                "↑  Select Document",
                18,
                true);

        Label fileSub = label(
                "Choose a file from your device or drag and drop it here.",
                12,
                false);

        VBox dropBox = new VBox(12);

        dropBox.setAlignment(
                Pos.CENTER);

        dropBox.setPrefHeight(
                280);

        dropBox.setStyle(
                "-fx-background-color:#f2f9ff;" +
                        "-fx-border-color:#b9d5f5;" +
                        "-fx-border-style:dashed;" +
                        "-fx-border-radius:12;" +
                        "-fx-background-radius:12;");

        Label documentIcon = label(
                "▤",
                40,
                true);

        documentIcon.setTextFill(
                Color.web("#287cf5"));

        Label dragText = label(
                "Drag & drop your document here",
                17,
                true);

        Label supported = label(
                "PDF, DOC, DOCX or image files are supported",
                11,
                false);

        Button chooseFile = new Button(
                "↑  Choose File");

        chooseFile.setPrefSize(
                145,
                42);

        chooseFile.setStyle(
                "-fx-background-color:white;" +
                        "-fx-border-color:#287cf5;" +
                        "-fx-text-fill:#287cf5;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:8;" +
                        "-fx-border-radius:8;");

        fileName = label(
                "No file selected",
                11,
                false);

        fileName.setTextFill(
                Color.web("#7d8999"));

        // =====================================================
        // CHOOSE FILE
        // =====================================================

        chooseFile.setOnAction(e -> {

            FileChooser chooser = new FileChooser();

            chooser.setTitle(
                    "Choose Document");

            chooser.getExtensionFilters()
                    .add(
                            new FileChooser.ExtensionFilter(
                                    "Documents",
                                    "*.pdf",
                                    "*.doc",
                                    "*.docx",
                                    "*.png",
                                    "*.jpg",
                                    "*.jpeg"));

            selectedFile = chooser.showOpenDialog(
                    null);

            if (selectedFile != null) {

                fileName.setText(
                        "✓  "
                                + selectedFile.getName());

                fileName.setTextFill(
                        Color.web("#15956d"));
            }
        });

        dropBox.getChildren().addAll(
                documentIcon,
                dragText,
                supported,
                chooseFile);

        fileCard.getChildren().addAll(
                fileTitle,
                fileSub,
                dropBox,
                fileName);

        // =====================================================
        // ACTION BUTTONS
        // =====================================================

        HBox actionBox = new HBox(12);

        Button uploadButton = new Button(
                "↑  Upload Document");

        uploadButton.setMaxWidth(
                Double.MAX_VALUE);

        uploadButton.setPrefHeight(
                48);

        uploadButton.setStyle(
                buttonStyle("#287cf5"));

        HBox.setHgrow(
                uploadButton,
                Priority.ALWAYS);

        Button rejectButton = new Button(
                "✕  Reject");

        rejectButton.setPrefWidth(
                140);

        rejectButton.setPrefHeight(
                48);

        rejectButton.setStyle(
                "-fx-background-color:#fff1f1;" +
                        "-fx-text-fill:#dc3545;" +
                        "-fx-border-color:#dc3545;" +
                        "-fx-border-radius:8;" +
                        "-fx-background-radius:8;" +
                        "-fx-font-weight:bold;");

        actionBox.getChildren().addAll(
                uploadButton,
                rejectButton);

        // =====================================================
        // UPLOAD ACTION
        // =====================================================

        uploadButton.setOnAction(e -> {

            if (documentType.getValue() == null) {

                showAlert(
                        "Document Type Required",
                        "Please select a document type first.");

                return;
            }

            if (selectedFile == null) {

                showAlert(
                        "File Required",
                        "Please choose a document first.");

                return;
            }

            uploadButton.setText(
                    "✓  Document Uploaded");

            uploadButton.setStyle(
                    buttonStyle("#16a875"));

            showUploadSuccess();
        });

        // =====================================================
        // REJECT ACTION
        // =====================================================

        rejectButton.setOnAction(e -> {

            if (selectedFile == null) {

                showAlert(
                        "No Document",
                        "There is no selected document to reject.");

                return;
            }

            Alert confirm = new Alert(
                    Alert.AlertType.CONFIRMATION);

            confirm.setTitle(
                    "Reject Document");

            confirm.setHeaderText(
                    "Reject selected document?");

            confirm.setContentText(
                    selectedFile.getName()
                            + "\n\nThis document will be removed.");

            ButtonType yes = new ButtonType(
                    "Reject",
                    ButtonBar.ButtonData.YES);

            ButtonType cancel = new ButtonType(
                    "Cancel",
                    ButtonBar.ButtonData.CANCEL_CLOSE);

            confirm.getButtonTypes().setAll(
                    yes,
                    cancel);

            confirm.showAndWait().ifPresent(
                    result -> {

                        if (result == yes) {

                            selectedFile = null;

                            fileName.setText(
                                    "✕  Document rejected — choose another file");

                            fileName.setTextFill(
                                    Color.web("#dc3545"));

                            uploadButton.setText(
                                    "↑  Upload Document");

                            uploadButton.setStyle(
                                    buttonStyle("#287cf5"));

                            showRejectSuccess();
                        }
                    });
        });

        main.getChildren().addAll(
                typeCard,
                fileCard,
                actionBox);

        root.setCenter(main);

        // =====================================================
        // SCENE
        // =====================================================

        scene = new Scene(
                root,
                1450,
                850);
    }

    // =========================================================
    // IMPORTANT:
    // DASHBOARD KE CENTER ME SIRF YE CONTENT AAYEGA.
    // SIDEBAR + HEADER DUPLICATE NAHI HOGA.
    // =========================================================

    public VBox getUploadContent() {

        VBox content = new VBox(24);

        content.setPadding(
                new Insets(
                        25,
                        35,
                        35,
                        35));

        Label title = label(
                "Upload Documents",
                29,
                true);

        Label subtitle = label(
                "Upload required ship and voyage documents.",
                12,
                false);

        VBox heading = new VBox(
                3,
                title,
                subtitle);

        content.getChildren().add(
                heading);

        content.getChildren().addAll(
                main.getChildren());

        return content;
    }

    // =========================================================
    // ORIGINAL FULL SCENE
    // =========================================================

    public Scene getUploadScene() {
        return scene;
    }

    // =========================================================
    // SUCCESS POPUP
    // =========================================================

    private void showUploadSuccess() {

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION);

        alert.setTitle(
                "Upload Successful");

        alert.setHeaderText(
                "✓  Document Uploaded Successfully!");

        alert.setContentText(
                "Your document has been uploaded successfully."
                        + "\n\n"
                        + "Document Type: "
                        + documentType.getValue()
                        + "\n"
                        + "File: "
                        + selectedFile.getName());

        alert.showAndWait();
    }

    // =========================================================
    // REJECT POPUP
    // =========================================================

    private void showRejectSuccess() {

        Alert alert = new Alert(
                Alert.AlertType.WARNING);

        alert.setTitle(
                "Upload Rejected");

        alert.setHeaderText(
                "✕  Upload Rejected");

        alert.setContentText(
                "Your document could not be uploaded."
                        + "\n\n"
                        + "Reason for rejection:"
                        + "\nThe selected document was rejected by the captain.");

        alert.showAndWait();
    }

    // =========================================================
    // SIDEBAR BUTTON
    // =========================================================

    private Button sideButton(
            String icon,
            String text) {

        Button button = new Button(
                icon + "   " + text);

        button.setMaxWidth(
                Double.MAX_VALUE);

        button.setPrefHeight(
                45);

        button.setAlignment(
                Pos.CENTER_LEFT);

        button.setPadding(
                new Insets(
                        0,
                        10,
                        0,
                        15));

        button.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-text-fill:#aebbd0;" +
                        "-fx-font-size:12;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:9;" +
                        "-fx-cursor:hand;");

        return button;
    }

    // =========================================================
    // ACTIVE
    // =========================================================

    private String activeStyle() {

        return "-fx-background-color:#287cf5;" +
                "-fx-text-fill:white;" +
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:9;" +
                "-fx-cursor:hand;";
    }

    // =========================================================
    // LABEL
    // =========================================================

    private Label label(
            String text,
            double size,
            boolean bold) {

        Label label = new Label(text);

        label.setFont(
                Font.font(
                        "Arial",
                        bold
                                ? FontWeight.BOLD
                                : FontWeight.NORMAL,
                        size));

        label.setTextFill(
                Color.web("#111827"));

        return label;
    }

    // =========================================================
    // CARD
    // =========================================================

    private String cardStyle() {

        return "-fx-background-color:white;" +
                "-fx-background-radius:16;" +
                "-fx-border-color:#e1e7ee;" +
                "-fx-border-radius:16;";
    }

    // =========================================================
    // FIELD
    // =========================================================

    private String fieldStyle() {

        return "-fx-background-color:white;" +
                "-fx-border-color:#e1e7ee;" +
                "-fx-border-radius:9;" +
                "-fx-background-radius:9;";
    }

    // =========================================================
    // BUTTON
    // =========================================================

    private String buttonStyle(
            String color) {

        return "-fx-background-color:" + color + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:8;" +
                "-fx-cursor:hand;";
    }

    // =========================================================
    // ALERT
    // =========================================================

    private void showAlert(
            String title,
            String message) {

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION);

        alert.setTitle(
                title);

        alert.setHeaderText(
                null);

        alert.setContentText(
                message);

        alert.showAndWait();
    }

}
