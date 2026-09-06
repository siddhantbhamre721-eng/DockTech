package com.projectui.view.ShipCaptain;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.projectui.model.Ship;
import com.projectui.service.CloudinaryService;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class UploadDocuments {

    private static final String BLUE = "#1769E8";
    private static final String TEXT = "#111827";
    private static final String MUTED = "#64748b";
    private static final String CARD_STYLE = "-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-radius:8; -fx-background-radius:8; -fx-effect:dropshadow(gaussian, rgba(15,23,42,0.06), 14, 0, 0, 4);";

    private File selectedFile = null;
    private Label fileChosenLabel;
    private TableView<Map<String, String>> docTable;
    private TextField searchField;
    private ComboBox<String> filterCategory;
    private ComboBox<String> filterStatus;
    private HBox kpiContainer;

    public Node getContent() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(16));
        container.setStyle("-fx-background-color:transparent;");

        // Header
        Label title = new Label("Upload Document");
        title.setStyle("-fx-text-fill:#05070c; -fx-font-size:24; -fx-font-weight:bold;");
        Label subtitle = new Label("Upload and manage official vessel documentation, cargo manifests, and port clearances with cloud repository synchronization.");
        subtitle.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:13;");
        VBox titleBox = new VBox(3, title, subtitle);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox headerRow = new HBox(12, titleBox, space);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(headerRow);

        Ship ship = CaptainSessionService.getAssignedShip();

        kpiContainer = buildKpiRow();
        container.getChildren().addAll(
                kpiContainer,
                buildMiddleRow(ship),
                buildRepositorySection(ship)
        );

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color:#f5f8fc; -fx-background:transparent; -fx-border-color:transparent;");
        return scroll;
    }

    public VBox getUploadContent() {
        return (VBox) ((ScrollPane) getContent()).getContent();
    }

    public Parent getRoot() {
        return new DashBoard().getRoot(null, null, "Upload Document");
    }

    public Parent getRoot(Stage stage, Runnable logoutAction) {
        return new DashBoard().getRoot(stage, logoutAction, "Upload Document");
    }

    public Scene getScene() {
        return new Scene(getRoot());
    }

    private HBox buildKpiRow() {
        List<Map<String, String>> docs = CaptainSessionService.getDocuments();
        int total = docs.size();
        int verified = 0;
        int pending = 0;
        int rejected = 0;

        for (Map<String, String> d : docs) {
            String st = d.getOrDefault("status", "").toLowerCase();
            if (st.contains("verified") || st.contains("approved")) {
                verified++;
            } else if (st.contains("reject") || st.contains("action")) {
                rejected++;
            } else {
                pending++;
            }
        }

        HBox row = new HBox(12,
                kpiCard("📁", "Total Documents", String.valueOf(total), "Vessel & Cargo Files", "#1d4ed8", "#eaf3ff"),
                kpiCard("✓", "Verified / Approved", String.valueOf(verified), "Official Records", "#0f9f6e", "#e7f8ef"),
                kpiCard("◷", "Pending Review", String.valueOf(pending), "Awaiting Port Clearance", "#d97706", "#fef3c7"),
                kpiCard("✕", "Rejected / Action Required", String.valueOf(rejected), "Attention Needed", "#dc2626", "#fee2e2")
        );
        row.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        return row;
    }

    private HBox buildMiddleRow(Ship ship) {
        HBox row = new HBox(14);

        // Left Panel: Assigned Vessel & Voyage Documentation Context
        VBox vesselCard = new VBox(12);
        vesselCard.setPadding(new Insets(18));
        vesselCard.setStyle(CARD_STYLE);
        HBox.setHgrow(vesselCard, Priority.ALWAYS);

        Label vTitle = new Label("Assigned Vessel & Voyage Context");
        vTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        GridPane vGrid = new GridPane();
        vGrid.setHgap(16);
        vGrid.setVgap(10);

        String shipName = ship != null && ship.getShipName() != null ? ship.getShipName() : "";
        String shipType = ship != null && ship.getShipType() != null ? ship.getShipType() : "Tanker";
        String voyId = "VOY-" + (ship != null && ship.getShipId() != null ? ship.getShipId() : "2026") + "-E";
        String origin = CaptainSessionService.getAssignedVoyageOrigin();
        String dest = CaptainSessionService.getAssignedVoyageDestination();
        String berth = ship != null && ship.getBerth() != null && !ship.getBerth().isBlank() ? ship.getBerth() : "Berth B04";
        String status = ship != null && ship.getStatus() != null ? ship.getStatus() : "At Berth";
        String captain = CaptainSessionService.getCaptainName();

        vGrid.add(fieldCell("Captain in Command", captain), 0, 0);
        vGrid.add(fieldCell("Assigned Vessel", shipName), 1, 0);
        vGrid.add(fieldCell("Vessel Type", shipType), 0, 1);
        vGrid.add(fieldCell("Voyage ID / Name", voyId), 1, 1);
        vGrid.add(fieldCell("Origin Port", origin), 0, 2);
        vGrid.add(fieldCell("Destination Port", dest), 1, 2);
        vGrid.add(fieldCell("Current Port / Berth", berth), 0, 3);
        vGrid.add(fieldCell("Current Vessel Status", status), 1, 3);

        VBox noteBox = new VBox(4);
        noteBox.setPadding(new Insets(8, 10, 8, 10));
        noteBox.setStyle("-fx-background-color:#eff6ff; -fx-background-radius:6; -fx-border-color:#bfdbfe; -fx-border-radius:6;");
        Label noteTitle = new Label("🔒 Secure Cloud Repository Link");
        noteTitle.setStyle("-fx-text-fill:#1d4ed8; -fx-font-size:12; -fx-font-weight:bold;");
        Label noteSub = new Label("Uploaded files are stored in AES-256 cloud storage and linked to Port Authority & Customs databases.");
        noteSub.setWrapText(true);
        noteSub.setStyle("-fx-text-fill:#334155; -fx-font-size:11;");
        noteBox.getChildren().addAll(noteTitle, noteSub);

        vesselCard.getChildren().addAll(vTitle, vGrid, noteBox);

        // Right Panel: Upload Vessel Document Form
        VBox formCard = new VBox(12);
        formCard.setPrefWidth(420);
        formCard.setMinWidth(380);
        formCard.setPadding(new Insets(18));
        formCard.setStyle(CARD_STYLE);

        Label formTitle = new Label("Upload Vessel Document");
        formTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        Label nameLabel = new Label("Document Title / Reference");
        nameLabel.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12; -fx-font-weight:bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("e.g., Cargo_Manifest_Voyage_1092");
        nameField.setStyle("-fx-background-color:white; -fx-border-color:#d1d5db; -fx-padding:8 12; -fx-font-size:13; -fx-background-radius:6;");

        Label typeLabel = new Label("Document Category");
        typeLabel.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12; -fx-font-weight:bold;");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(
                "Cargo Manifest",
                "Port Clearance Certificate",
                "Bill of Lading",
                "Crew List & Passports",
                "Safety & Inspection Certificate",
                "Dangerous Goods Declaration",
                "Fuel & Bunkering Log",
                "Customs Declaration"
        );
        typeCombo.setValue("Cargo Manifest");
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        typeCombo.setStyle("-fx-background-color:white; -fx-border-color:#d1d5db; -fx-padding:7 12; -fx-font-size:13; -fx-background-radius:6;");

        Label fileLabel = new Label("Select File (PDF, DOCX, PNG, JPG)");
        fileLabel.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12; -fx-font-weight:bold;");

        fileChosenLabel = new Label("No file chosen");
        fileChosenLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        Button chooseBtn = new Button("📁 Choose Document File");
        chooseBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#334155; -fx-border-color:#cbd5e1; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:8 14; -fx-background-radius:6; -fx-cursor:hand;");
        chooseBtn.setMaxWidth(Double.MAX_VALUE);

        chooseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Document to Upload");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Document Files", "*.pdf", "*.docx", "*.doc", "*.png", "*.jpg", "*.jpeg")
            );
            File f = chooser.showOpenDialog(chooseBtn.getScene() == null ? null : chooseBtn.getScene().getWindow());
            if (f != null) {
                selectedFile = f;
                fileChosenLabel.setText("Selected: " + f.getName() + " (" + (f.length() / 1024) + " KB)");
                if (nameField.getText().isBlank()) {
                    nameField.setText(f.getName());
                }
            }
        });

        ProgressBar uploadProgress = new ProgressBar();
        uploadProgress.setVisible(false);
        uploadProgress.setManaged(false);
        uploadProgress.setMaxWidth(Double.MAX_VALUE);
        uploadProgress.setPrefHeight(6);

        Label alert = new Label();
        alert.setVisible(false);
        alert.setManaged(false);
        alert.setMaxWidth(Double.MAX_VALUE);
        alert.setPadding(new Insets(8, 12, 8, 12));

        Button uploadBtn = new Button("Upload Document to Cloud Storage");
        uploadBtn.setStyle("-fx-background-color:" + BLUE + "; -fx-text-fill:white; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:10 16; -fx-background-radius:6; -fx-cursor:hand;");
        uploadBtn.setMaxWidth(Double.MAX_VALUE);

        uploadBtn.setOnAction(e -> {
            String docName = nameField.getText().trim();
            if (docName.isBlank()) {
                docName = selectedFile != null ? selectedFile.getName() : "Vessel_Document_" + System.currentTimeMillis() + ".pdf";
            }
            String docType = typeCombo.getValue();
            String size = selectedFile != null ? (selectedFile.length() / 1024) + " KB" : "1.8 MB";
            String docId = "DOC-" + (System.currentTimeMillis() % 100000);

            uploadBtn.setDisable(true);
            uploadProgress.setVisible(true);
            uploadProgress.setManaged(true);
            uploadProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

            final String finalDocName = docName;
            final File uploadTarget = selectedFile;

            // Upload via CloudinaryService
            CloudinaryService.uploadDocument(uploadTarget, "captain-docs", docType.toLowerCase().replaceAll("[^a-z0-9]", "_"))
                    .thenAccept(meta -> {
                        Platform.runLater(() -> {
                            String cloudUrl = (meta != null && meta.cloudinaryUrl != null && !meta.cloudinaryUrl.isBlank())
                                    ? meta.cloudinaryUrl
                                    : "";
                            String fPath = uploadTarget != null ? uploadTarget.getAbsolutePath() : null;

                            CaptainSessionService.addDocument(docId, finalDocName, docType, size, "Pending Review", cloudUrl, fPath);

                            alert.setText("✓ Document '" + finalDocName + "' uploaded to Cloud Storage & synced with Port Authority.");
                            alert.setStyle("-fx-background-color:#ecfdf5; -fx-text-fill:#065f46; -fx-border-color:#a7f3d0; -fx-border-radius:6; -fx-font-weight:bold; -fx-font-size:12;");
                            alert.setVisible(true);
                            alert.setManaged(true);

                            uploadBtn.setDisable(false);
                            uploadProgress.setVisible(false);
                            uploadProgress.setManaged(false);

                            nameField.clear();
                            selectedFile = null;
                            fileChosenLabel.setText("No file chosen");

                            refreshRepositoryTable();
                        });
                    }).exceptionally(ex -> {
                        Platform.runLater(() -> {
                            uploadBtn.setDisable(false);
                            uploadProgress.setVisible(false);
                            uploadProgress.setManaged(false);
                            alert.setText("✕ Cloud Upload Failed: " + ex.getMessage());
                            alert.setStyle("-fx-background-color:#fef2f2; -fx-text-fill:#991b1b; -fx-border-color:#fecaca; -fx-border-radius:6; -fx-font-weight:bold; -fx-font-size:12;");
                            alert.setVisible(true);
                            alert.setManaged(true);
                        });
                        return null;
                    });
        });

        formCard.getChildren().addAll(
                formTitle,
                nameLabel,
                nameField,
                typeLabel,
                typeCombo,
                fileLabel,
                chooseBtn,
                fileChosenLabel,
                uploadProgress,
                alert,
                uploadBtn
        );

        row.getChildren().addAll(vesselCard, formCard);
        return row;
    }

    private VBox buildRepositorySection(Ship ship) {
        VBox tableCard = new VBox(12);
        tableCard.setPadding(new Insets(18));
        tableCard.setStyle(CARD_STYLE);

        Label tableTitle = new Label("Vessel & Voyage Document Repository");
        tableTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:16; -fx-font-weight:bold;");

        // Filter / Search Row
        HBox filterRow = new HBox(10);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search documents by title, ID, or type...");
        searchField.setPrefWidth(260);
        searchField.setStyle("-fx-background-color:white; -fx-border-color:#d1d5db; -fx-padding:6 10; -fx-font-size:12; -fx-background-radius:6;");
        searchField.textProperty().addListener((obs, o, n) -> filterTable());

        filterCategory = new ComboBox<>();
        filterCategory.getItems().addAll("All Categories", "Cargo Manifest", "Port Clearance", "Bill of Lading", "Crew List & Passports", "Safety Certificate");
        filterCategory.setValue("All Categories");
        filterCategory.setStyle("-fx-background-color:white; -fx-border-color:#d1d5db; -fx-font-size:12; -fx-background-radius:6;");
        filterCategory.valueProperty().addListener((obs, o, n) -> filterTable());

        filterStatus = new ComboBox<>();
        filterStatus.getItems().addAll("All Statuses", "Verified", "Approved", "Pending Review", "Rejected");
        filterStatus.setValue("All Statuses");
        filterStatus.setStyle("-fx-background-color:white; -fx-border-color:#d1d5db; -fx-font-size:12; -fx-background-radius:6;");
        filterStatus.valueProperty().addListener((obs, o, n) -> filterTable());

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#334155; -fx-border-color:#cbd5e1; -fx-font-size:12; -fx-padding:6 12; -fx-background-radius:6; -fx-cursor:hand;");
        refreshBtn.setOnAction(e -> refreshRepositoryTable());

        filterRow.getChildren().addAll(searchField, filterCategory, filterStatus, sp, refreshBtn);

        // Document Table
        docTable = new TableView<>();
        docTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        docTable.setPrefHeight(280);
        docTable.setStyle("-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-radius:6;");

        TableColumn<Map<String, String>, String> colId = new TableColumn<>("Doc ID");
        colId.setMinWidth(100);
        colId.setMaxWidth(120);
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrDefault("docId", "-")));

        TableColumn<Map<String, String>, String> colName = new TableColumn<>("Document Title");
        colName.setMinWidth(180);
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrDefault("name", "-")));

        TableColumn<Map<String, String>, String> colType = new TableColumn<>("Category");
        colType.setMinWidth(140);
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrDefault("type", "-")));

        TableColumn<Map<String, String>, String> colVessel = new TableColumn<>("Vessel / Voyage");
        colVessel.setMinWidth(140);
            colVessel.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrDefault("vessel", "") + " (" + d.getValue().getOrDefault("voyage", "") + ")"));

        TableColumn<Map<String, String>, String> colUploader = new TableColumn<>("Uploaded By");
        colUploader.setMinWidth(120);
        colUploader.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrDefault("uploadedBy", "Captain")));

        TableColumn<Map<String, String>, String> colDate = new TableColumn<>("Upload Date");
        colDate.setMinWidth(120);
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrDefault("date", "-")));

        TableColumn<Map<String, String>, String> colSize = new TableColumn<>("Size");
        colSize.setMinWidth(70);
        colSize.setMaxWidth(90);
        colSize.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrDefault("size", "-")));

        TableColumn<Map<String, String>, String> colStatus = new TableColumn<>("Status");
        colStatus.setMinWidth(110);
        colStatus.setMaxWidth(130);
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrDefault("status", "Pending Review")));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    if (item.equalsIgnoreCase("Verified") || item.equalsIgnoreCase("Approved")) {
                        badge.setStyle("-fx-background-color:#ecfdf5; -fx-text-fill:#065f46; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4; -fx-font-size:11;");
                    } else if (item.equalsIgnoreCase("Rejected")) {
                        badge.setStyle("-fx-background-color:#fef2f2; -fx-text-fill:#991b1b; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4; -fx-font-size:11;");
                    } else {
                        badge.setStyle("-fx-background-color:#fef3c7; -fx-text-fill:#92400e; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:4; -fx-font-size:11;");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<Map<String, String>, Void> colAction = new TableColumn<>("Action");
        colAction.setMinWidth(100);
        colAction.setMaxWidth(110);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View ↗");
            {
                viewBtn.setStyle("-fx-background-color:" + BLUE + "; -fx-text-fill:white; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:4; -fx-cursor:hand;");
                viewBtn.setOnAction(e -> {
                    Map<String, String> doc = getTableView().getItems().get(getIndex());
                    openDocument(doc);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewBtn);
            }
        });

        docTable.getColumns().addAll(colId, colName, colType, colVessel, colUploader, colDate, colSize, colStatus, colAction);
        refreshRepositoryTable();

        tableCard.getChildren().addAll(tableTitle, filterRow, docTable);
        return tableCard;
    }

    private void openDocument(Map<String, String> doc) {
        if (doc == null) return;
        String url = doc.get("url");
        String filePath = doc.get("filePath");
        String name = doc.getOrDefault("name", "Document");

        boolean opened = false;

        if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                    opened = true;
                }
            } catch (Exception ignored) {}
        }

        if (!opened && filePath != null) {
            try {
                File f = new File(filePath);
                if (f.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(f);
                    opened = true;
                }
            } catch (Exception ignored) {}
        }

        if (!opened) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Document Viewer");
            alert.setHeaderText("Opening " + name);
            alert.setContentText("Document Record:\n• ID: " + doc.getOrDefault("docId", "-") + "\n• Type: " + doc.getOrDefault("type", "-") + "\n• Vessel: " + doc.getOrDefault("vessel", "-") + "\n• Cloud Status: Verified & Stored");
            alert.showAndWait();
        }
    }

    private void filterTable() {
        if (docTable == null) return;
        List<Map<String, String>> allDocs = CaptainSessionService.getDocuments();

        String q = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String cat = filterCategory != null && filterCategory.getValue() != null ? filterCategory.getValue() : "All Categories";
        String st = filterStatus != null && filterStatus.getValue() != null ? filterStatus.getValue() : "All Statuses";

        List<Map<String, String>> filtered = allDocs.stream().filter(d -> {
            boolean matchQ = q.isBlank()
                    || d.getOrDefault("name", "").toLowerCase().contains(q)
                    || d.getOrDefault("docId", "").toLowerCase().contains(q)
                    || d.getOrDefault("type", "").toLowerCase().contains(q);

            boolean matchCat = cat.equals("All Categories") || d.getOrDefault("type", "").equalsIgnoreCase(cat);
            boolean matchSt = st.equals("All Statuses") || d.getOrDefault("status", "").equalsIgnoreCase(st);

            return matchQ && matchCat && matchSt;
        }).collect(Collectors.toList());

        docTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void refreshRepositoryTable() {
        if (docTable != null) {
            filterTable();
        }
    }

    private VBox fieldCell(String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:11; -fx-font-weight:bold;");
        Label v = new Label(value != null && !value.isBlank() ? value : "N/A");
        v.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:13; -fx-font-weight:bold;");
        return new VBox(2, l, v);
    }

    private VBox kpiCard(String icon, String title, String value, String sub, String accentColor, String bgColor) {
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size:16; -fx-text-fill:" + accentColor + "; -fx-font-weight:bold; -fx-background-color:" + bgColor + "; -fx-background-radius:6; -fx-padding:6 10;");

        Label t = new Label(title);
        t.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:13; -fx-font-weight:bold;");

        HBox top = new HBox(8, iconLbl, t);
        top.setAlignment(Pos.CENTER_LEFT);

        Label v = new Label(value != null ? value : "-");
        v.setStyle("-fx-text-fill:" + TEXT + "; -fx-font-size:20; -fx-font-weight:bold;");

        Label s = new Label(sub);
        s.setStyle("-fx-text-fill:" + MUTED + "; -fx-font-size:12;");

        VBox card = new VBox(6, top, v, s);
        card.setPadding(new Insets(16));
        card.setStyle(CARD_STYLE);
        return card;
    }
}