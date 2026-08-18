package com.projectui.view.CustomOfficer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

@SuppressWarnings("unused")
public class DocumentVerification {
    private final BorderPane root = new BorderPane();
    private VBox content;

    private int pendingCount = 28;
    private int verifiedCount = 156;
    private int rejectedCount = 7;
    private int todayCount = 14;

    private Label pendingLabel;
    private Label verifiedLabel;
    private Label rejectedLabel;
    private Label todayLabel;

    private VBox rowsContainer;
    private Map<String, String[]> documentMap = new LinkedHashMap<>();
    private String selectedDocId = "DOC-1024";

    // Verification Detail Labels
    private Label detailTitle;
    private Label detailDocId;
    private Label detailCargoId;
    private Label detailShip;
    private Label detailExporter;
    private Label detailType;
    private Label pdfNameLabel;

    public DocumentVerification() {
        // Initialize default document dataset
        initData();

        // PAGE HEADING
        Label title = label("Document Verification", 28, true, "#111827");
        Label sub = label("Review and verify cargo and shipment documentation.", 14, false, "#64748b");

        VBox heading = new VBox(6, title, sub);
        heading.setPadding(new Insets(10, 25, 10, 25));

        // CARDS
        HBox cards = new HBox(18);
        cards.setPadding(new Insets(10, 25, 15, 25));

        VBox cardPending = statCard("Pending Review", String.valueOf(pendingCount), "◷", "#FFF0C7");
        VBox cardVerified = statCard("Verified", String.valueOf(verifiedCount), "✓", "#D8F8E8");
        VBox cardRejected = statCard("Rejected", String.valueOf(rejectedCount), "×", "#FFE0E6");
        VBox cardToday = statCard("Today's Submissions", String.valueOf(todayCount), "▤", "#DDEEFF");

        this.pendingLabel = (Label) ((VBox) cardPending.getChildren().get(1)).getChildren().get(0);
        this.verifiedLabel = (Label) ((VBox) cardVerified.getChildren().get(1)).getChildren().get(0);
        this.rejectedLabel = (Label) ((VBox) cardRejected.getChildren().get(1)).getChildren().get(0);
        this.todayLabel = (Label) ((VBox) cardToday.getChildren().get(1)).getChildren().get(0);

        cards.getChildren().addAll(cardPending, cardVerified, cardRejected, cardToday);

        // FILTER BAR
        ComboBox<String> typeFilter = combo("Document Type",
                "Bill of Lading", "Customs Declaration", "Commercial Invoice", "Packing List", "Certificate of Origin", "Import/Export License", 140);

        ComboBox<String> statusFilter = combo("Status",
                "Pending", "Verified", "Rejected", 105);

        ComboBox<String> dateFilter = combo("Date",
                "Today", "This Week", "This Month", 100);

        ComboBox<String> exporterFilter = combo("Exporter",
                "Ocean Shipping Ltd.", "Global Traders", "Pacific Cargo Co.", 130);

        Button applyFilterBtn = new Button("⚙  Apply Filter");
        applyFilterBtn.setPrefSize(115, 40);
        applyFilterBtn.setTextFill(Color.WHITE);
        applyFilterBtn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        applyFilterBtn.setStyle("-fx-background-color:#1683F7; -fx-background-radius:10; -fx-cursor:hand;");

        applyFilterBtn.setOnAction(e -> applyFilter(
                typeFilter.getValue(),
                statusFilter.getValue(),
                exporterFilter.getValue()
        ));

        HBox filterBox = new HBox(10, typeFilter, statusFilter, dateFilter, exporterFilter, applyFilterBtn);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(12));
        filterBox.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#E4EAF0; -fx-border-radius:12;");

        HBox filterWrap = new HBox(filterBox);
        filterWrap.setPadding(new Insets(0, 25, 15, 25));

        // TABLE
        VBox table = new VBox();
        table.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#E4EAF0; -fx-border-radius:12;");

        Label tt = label("Document Verification List", 18, true, "#111827");
        Label ts = label("Latest cargo and shipment documentation", 13, false, "#64748b");

        VBox th = new VBox(4, tt, ts);
        Label viewAll = label("View All", 13, true, "#1683F7");
        viewAll.setStyle("-fx-cursor:hand;");
        viewAll.setOnMouseClicked(e -> renderTableRows(documentMap.values()));

        Region hsp = new Region();
        HBox.setHgrow(hsp, Priority.ALWAYS);

        HBox head = new HBox(th, hsp, viewAll);
        head.setPadding(new Insets(18));
        head.setAlignment(Pos.CENTER_LEFT);

        HBox columns = new HBox();
        columns.setPadding(new Insets(12, 15, 12, 15));
        columns.setStyle("-fx-border-color:#E7EDF2; -fx-border-width:1 0 1 0; -fx-background-color:#F8FAFC;");

        String[] heads = {"Document ID", "Cargo ID", "Ship", "Exporter", "Document Type", "Status", "Action"};
        double[] widths = {95, 85, 110, 130, 140, 80, 80};

        for (int i = 0; i < heads.length; i++)
            columns.getChildren().add(column(heads[i], widths[i]));

        rowsContainer = new VBox();
        renderTableRows(documentMap.values());

        table.getChildren().addAll(head, columns, rowsContainer);
        HBox.setHgrow(table, Priority.ALWAYS);

        // VERIFICATION RIGHT PANEL
        VBox verify = new VBox(14);
        verify.setPrefWidth(310);
        verify.setPadding(new Insets(20));
        verify.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#E4EAF0; -fx-border-radius:12;");

        Label verifyPanelTitle = label("Document Verification", 18, true, "#111827");
        detailTitle = label("Reviewing DOC-1024", 13, false, "#64748b");

        detailDocId = label("DOC-1024", 13, true, "#111827");
        detailCargoId = label("CGO-1287", 13, true, "#111827");
        detailShip = label("Oceanic Star", 13, true, "#111827");
        detailExporter = label("Ocean Shipping Ltd.", 13, true, "#111827");
        detailType = label("Bill of Lading", 13, true, "#111827");

        verify.getChildren().addAll(
                verifyPanelTitle,
                detailTitle,
                detailRow("Document ID", detailDocId),
                detailRow("Cargo ID", detailCargoId),
                detailRow("Ship", detailShip),
                detailRow("Exporter", detailExporter),
                detailRow("Document Type", detailType)
        );

        // PDF PREVIEW BOX
        pdfNameLabel = new Label("Bill of Lading.pdf");
        pdfNameLabel.setTextFill(Color.WHITE);
        pdfNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        Label pdfIcon = new Label("📄");
        pdfIcon.setTextFill(Color.WHITE);
        pdfIcon.setFont(Font.font(32));

        Button openPdfBtn = new Button("👁 Open / Load PDF");
        openPdfBtn.setStyle("-fx-background-color:#1683F7; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:6; -fx-cursor:hand;");
        openPdfBtn.setOnAction(e -> handleOpenPdf());

        VBox pdfBox = new VBox(8, pdfIcon, pdfNameLabel, openPdfBtn);
        pdfBox.setAlignment(Pos.CENTER);
        pdfBox.setPadding(new Insets(15));
        pdfBox.setPrefHeight(140);
        pdfBox.setStyle("-fx-background-color:#1e293b; -fx-background-radius:12; -fx-cursor:hand;");
        pdfBox.setOnMouseClicked(e -> handleOpenPdf());

        VBox checks = new VBox(8);
        checks.getChildren().add(label("Verification Checklist", 14, true, "#111827"));

        String[] checkNames = {
                "Document Number Valid",
                "Exporter Information Valid",
                "Cargo Information Matches",
                "Quantity Verified",
                "Additional Inspection Required"
        };

        for (int i = 0; i < checkNames.length; i++) {
            CheckBox c = new CheckBox(checkNames[i]);
            c.setSelected(i < 4);
            c.setStyle("-fx-font-size: 13px;");
            checks.getChildren().add(c);
        }

        Button verifyBtn = actionButton("✓  Verify Document", "#10B981");
        Button rejectBtn = actionButton("×  Reject Document", "#EF233C");

        verifyBtn.setOnAction(e -> handleVerifyStatus("Verified"));
        rejectBtn.setOnAction(e -> handleVerifyStatus("Rejected"));

        HBox actionBox = new HBox(10, verifyBtn, rejectBtn);
        HBox.setHgrow(verifyBtn, Priority.ALWAYS);
        HBox.setHgrow(rejectBtn, Priority.ALWAYS);

        verify.getChildren().addAll(
                pdfBox,
                checks,
                actionBox
        );

        // MAIN CONTENT LAYOUT
        HBox main = new HBox(18, table, verify);
        main.setPadding(new Insets(0, 25, 25, 25));
        HBox.setHgrow(table, Priority.ALWAYS);

        content = new VBox(heading, cards, filterWrap, main);
        content.setStyle("-fx-background-color:#F3FAFF;");
        VBox.setVgrow(main, Priority.ALWAYS);

        root.setCenter(content);
        root.setStyle("-fx-background-color:#F3FAFF;");
    }

    private void initData() {
        documentMap.put("DOC-1024", new String[]{"DOC-1024", "CGO-1287", "Oceanic Star", "Ocean Shipping Ltd.", "Bill of Lading", "Pending"});
        documentMap.put("DOC-1023", new String[]{"DOC-1023", "CGO-1290", "Sea Voyager", "Global Traders", "Customs Declaration", "Verified"});
        documentMap.put("DOC-1022", new String[]{"DOC-1022", "CGO-1288", "Pacific Dawn", "Pacific Cargo Co.", "Commercial Invoice", "Verified"});
        documentMap.put("DOC-1021", new String[]{"DOC-1021", "CGO-1301", "Coral Queen", "Ocean Shipping Ltd.", "Packing List", "Pending"});
        documentMap.put("DOC-1020", new String[]{"DOC-1020", "CGO-1295", "Blue Marlin", "Global Traders", "Certificate of Origin", "Verified"});
        documentMap.put("DOC-1019", new String[]{"DOC-1019", "CGO-1299", "Silver Wave", "Pacific Cargo Co.", "Import/Export License", "Rejected"});
    }

    private void renderTableRows(Collection<String[]> dataRows) {
        rowsContainer.getChildren().clear();
        for (String[] r : dataRows) {
            rowsContainer.getChildren().add(documentRow(r));
        }
    }

    private void selectDocument(String[] r) {
        this.selectedDocId = r[0];
        detailTitle.setText("Reviewing " + r[0]);
        detailDocId.setText(r[0]);
        detailCargoId.setText(r[1]);
        detailShip.setText(r[2]);
        detailExporter.setText(r[3]);
        detailType.setText(r[4]);
        pdfNameLabel.setText(r[4] + ".pdf");
    }

    private void handleVerifyStatus(String newStatus) {
        String[] rowData = documentMap.get(selectedDocId);
        if (rowData != null) {
            String oldStatus = rowData[5];
            rowData[5] = newStatus;

            // Recalculate statistics
            if ("Pending".equalsIgnoreCase(oldStatus) && !"Pending".equalsIgnoreCase(newStatus)) {
                if (pendingCount > 0) pendingCount--;
            }
            if ("Verified".equalsIgnoreCase(newStatus) && !"Verified".equalsIgnoreCase(oldStatus)) {
                verifiedCount++;
            }
            if ("Rejected".equalsIgnoreCase(newStatus) && !"Rejected".equalsIgnoreCase(oldStatus)) {
                rejectedCount++;
            }

            pendingLabel.setText(String.valueOf(pendingCount));
            verifiedLabel.setText(String.valueOf(verifiedCount));
            rejectedLabel.setText(String.valueOf(rejectedCount));

            renderTableRows(documentMap.values());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Document Action");
            alert.setHeaderText(null);
            alert.setContentText("Document " + selectedDocId + " has been marked as " + newStatus + ".");
            alert.showAndWait();
        }
    }

    private void handleOpenPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Document PDF");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = fileChooser.showOpenDialog(root.getScene() != null ? root.getScene().getWindow() : null);
        if (file != null) {
            pdfNameLabel.setText(file.getName());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("PDF Loaded");
            alert.setHeaderText("Document Attached");
            alert.setContentText("File successfully attached for review:\n" + file.getAbsolutePath());
            alert.showAndWait();
        } else {
            // Preview dialog if no file picked
            Stage previewStage = new Stage();
            if (root.getScene() != null && root.getScene().getWindow() != null) {
                previewStage.initOwner(root.getScene().getWindow());
                previewStage.initModality(Modality.WINDOW_MODAL);
            }
            previewStage.setTitle("PDF Document Preview - " + selectedDocId);

            VBox previewBox = new VBox(15);
            previewBox.setPadding(new Insets(25));
            previewBox.setAlignment(Pos.CENTER);
            previewBox.setStyle("-fx-background-color: #1e293b;");

            Label pIcon = new Label("📄");
            pIcon.setFont(Font.font(48));
            pIcon.setTextFill(Color.WHITE);

            Label pTitle = new Label(detailType.getText() + " (" + selectedDocId + ")");
            pTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            pTitle.setTextFill(Color.WHITE);

            Label pSub = new Label("Exporter: " + detailExporter.getText() + " | Vessel: " + detailShip.getText());
            pSub.setFont(Font.font("Arial", 14));
            pSub.setTextFill(Color.LIGHTGRAY);

            TextArea mockDocContent = new TextArea(
                    "==========================================================\n" +
                    "                 OFFICIAL CUSTOMS DOCUMENT\n" +
                    "==========================================================\n" +
                    "Document ID: " + selectedDocId + "\n" +
                    "Cargo Ref:   " + detailCargoId.getText() + "\n" +
                    "Vessel Name: " + detailShip.getText() + "\n" +
                    "Exporter:    " + detailExporter.getText() + "\n" +
                    "Doc Type:    " + detailType.getText() + "\n" +
                    "----------------------------------------------------------\n" +
                    "STATUS: VERIFICATION PENDING\n" +
                    "This document certifies that the cargo manifests and specifications\n" +
                    "have been submitted to customs authority for review.\n" +
                    "=========================================================="
            );
            mockDocContent.setEditable(false);
            mockDocContent.setPrefHeight(200);
            mockDocContent.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 13px;");

            Button closeBtn = new Button("Close Preview");
            closeBtn.setPrefSize(120, 36);
            closeBtn.setStyle("-fx-background-color:#1683F7; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand;");
            closeBtn.setOnAction(ev -> previewStage.close());

            previewBox.getChildren().addAll(pIcon, pTitle, pSub, mockDocContent, closeBtn);

            Scene pScene = new Scene(previewBox, 500, 420);
            com.projectui.view.login.LoginPage.applyInvisibleScrollbar(pScene);
            com.projectui.view.login.LoginPage.applyInvisibleScrollbar(previewStage);
            previewStage.setScene(pScene);
            previewStage.show();
        }
    }

    private void applyFilter(String type, String status, String exporter) {
        List<String[]> filtered = new ArrayList<>();
        for (String[] r : documentMap.values()) {
            boolean matchesType = "Document Type".equalsIgnoreCase(type) || type == null || r[4].equalsIgnoreCase(type);
            boolean matchesStatus = "Status".equalsIgnoreCase(status) || status == null || r[5].equalsIgnoreCase(status);
            boolean matchesExporter = "Exporter".equalsIgnoreCase(exporter) || exporter == null || r[3].equalsIgnoreCase(exporter);

            if (matchesType && matchesStatus && matchesExporter) {
                filtered.add(r);
            }
        }
        renderTableRows(filtered);
    }

    // ================= HELPERS =================

    private Label label(String text, double size, boolean bold, String color) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        l.setTextFill(Color.web(color));
        return l;
    }

    private VBox statCard(String title, String value, String icon, String bg) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setPrefHeight(105);
        card.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#E4EAF0; -fx-border-radius:12;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label t = label(title, 13, false, "#64748b");
        Label n = label(value, 28, true, "#111827");
        Label i = label(icon, 18, true, "#1683F7");
        i.setPrefSize(38, 38);
        i.setAlignment(Pos.CENTER);
        i.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:9;");

        Region s = new Region();
        HBox.setHgrow(s, Priority.ALWAYS);

        VBox valBox = new VBox(n);
        card.getChildren().addAll(new HBox(t, s, i), valBox);
        return card;
    }

    private ComboBox<String> combo(String defaultValue, String a, String b, String c, double width) {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(defaultValue, a, b, c);
        box.setValue(defaultValue);
        box.setPrefSize(width, 40);
        box.setStyle("-fx-font-size: 13px;");
        return box;
    }

    private ComboBox<String> combo(String defaultValue, String a, String b, String c, String d, String e, String f, double width) {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(defaultValue, a, b, c, d, e, f);
        box.setValue(defaultValue);
        box.setPrefSize(width, 40);
        box.setStyle("-fx-font-size: 13px;");
        return box;
    }

    private Label column(String text, double width) {
        Label l = label(text, 12, true, "#64748b");
        l.setPrefWidth(width);
        l.setWrapText(true);
        return l;
    }

    private HBox documentRow(String[] r) {
        HBox row = new HBox();
        row.setPrefHeight(55);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 15, 10, 15));
        row.setStyle("-fx-border-color:#EEF2F5; -fx-border-width:0 0 1 0; -fx-cursor:hand;");

        for (int i = 0; i < 5; i++)
            row.getChildren().add(cell(r[i], new double[]{95, 85, 110, 130, 140}[i]));

        row.getChildren().add(statusCell(r[5]));

        Button btn = verifyButton();
        btn.setOnAction(e -> {
            selectDocument(r);
        });
        row.getChildren().add(btn);

        row.setOnMouseClicked(e -> selectDocument(r));

        return row;
    }

    private Label statusCell(String status) {
        Label l = label(status, 12, true,
                status.equalsIgnoreCase("Verified") ? "#10B981" :
                status.equalsIgnoreCase("Rejected") ? "#EF233C" : "#F59E0B");
        l.setPrefWidth(80);
        return l;
    }

    private Button verifyButton() {
        Button b = new Button("Review");
        b.setPrefSize(70, 32);
        b.setTextFill(Color.WHITE);
        b.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        b.setStyle("-fx-background-color:#1683F7; -fx-background-radius:7; -fx-cursor:hand;");
        return b;
    }

    private Button actionButton(String text, String color) {
        Button b = new Button(text);
        b.setPrefSize(130, 48);
        b.setTextFill(Color.WHITE);
        b.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        b.setStyle("-fx-background-color:" + color + "; -fx-background-radius:10; -fx-cursor:hand;");
        return b;
    }

    private Label cell(String text, double width) {
        Label l = label(text, 13, false, "#303B49");
        l.setPrefWidth(width);
        l.setWrapText(true);
        return l;
    }

    private HBox detailRow(String name, Label valLabel) {
        Label n = label(name, 12, false, "#64748b");
        n.setPrefWidth(110);
        HBox row = new HBox(5, n, valLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    public BorderPane getRoot() {
        return root;
    }

    public VBox getContent() {
        return content;
    }
}
