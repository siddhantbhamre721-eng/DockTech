package com.projectui.view.CustomOfficer;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.projectui.dao.CargoDao;
import com.projectui.dao.UserDao;
import com.projectui.view.login.LoginPage;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

@SuppressWarnings({"unchecked", "unused"})
public class DocumentVerification {

    private final BorderPane root = new BorderPane();
    private VBox content;

    // Live Metrics
    private int pendingCount = 0;
    private int underReviewCount = 0;
    private int verifiedCount = 0;
    private int onHoldCount = 0;
    private int rejectedCount = 0;
    private int missingDocsCount = 0;

    private Label pendingLabel;
    private Label underReviewLabel;
    private Label verifiedLabel;
    private Label onHoldLabel;
    private Label rejectedLabel;
    private Label missingDocsLabel;

    private VBox rowsContainer;
    private Label queueCountBadge;
    private final List<Map<String, Object>> activeDocList = new ArrayList<>();
    private Map<String, Object> selectedDoc = null;

    // Detailed Audit Panel Labels
    private Label detailHeaderTitle;
    private Label statusBadgeLabel;

    // Document Fields
    private Label detailDocId;
    private Label detailDocType;
    private Label detailDocStatus;
    private Label detailDocVersion;
    private Label detailSubmittedBy;
    private Label detailSubmittedDate;
    private Label detailLastUpdated;
    private Label detailVerifiedBy;
    private Label detailVerifiedNotes;

    // Cargo Fields
    private Label detailCargoId;
    private Label detailCommodity;
    private Label detailCargoDesc;
    private Label detailHsCode;
    private Label detailQuantity;
    private Label detailPackageInfo;
    private Label detailGrossWeight;
    private Label detailNetWeight;

    // Parties Fields
    private Label detailImporter;
    private Label detailExporter;
    private Label detailCompany;

    // Shipment Fields
    private Label detailVessel;
    private Label detailVoyage;
    private Label detailContainer;
    private Label detailRoute;
    private Label detailPortJurisdiction;
    private Label detailEtaEtd;

    // File Preview
    private Label pdfNameLabel;
    private Label pdfTypeLabel;
    private Button openPdfBtn;

    // Checklist & Overview
    private VBox checklistContainer;

    // Backend context
    private final CargoDao cargoDao = new CargoDao();
    private String activeAssignedPort = "Port of Mumbai";
    private String officerName = "Customs Officer";
    private String officerEmail = "";
    private String officerUid = "";
    private String officerRole = "Custom Officer";

    // Search and Filters
    private TextField searchField;
    private ComboBox<String> typeFilter;
    private ComboBox<String> statusFilter;
    private ComboBox<String> exporterFilter;
    private ComboBox<String> portFilter;

    // Header dynamic jurisdiction label
    private Label portJurisdictionLabel;

    public DocumentVerification() {
        resolveOfficerContext();
        buildUI();
        refreshLiveDocuments();

        // Listen for live database updates from Firestore sync
        CargoDao.addChangeListener(this::refreshLiveDocuments);
    }

    private void resolveOfficerContext() {
        if (LoginPage.loggedInUser != null) {
            Map<String, Object> u = LoginPage.loggedInUser;
            if (u.get("name") != null && !u.get("name").toString().isBlank()) {
                officerName = u.get("name").toString();
            } else if (u.get("fullName") != null && !u.get("fullName").toString().isBlank()) {
                officerName = u.get("fullName").toString();
            }

            if (u.get("email") != null) officerEmail = u.get("email").toString();
            if (u.get("uid") != null) officerUid = u.get("uid").toString();
            if (u.get("role") != null) officerRole = u.get("role").toString();

            String portVal = null;
            if (u.get("assignedPort") != null && !u.get("assignedPort").toString().isBlank()) {
                portVal = u.get("assignedPort").toString();
            } else if (u.get("port") != null && !u.get("port").toString().isBlank()) {
                portVal = u.get("port").toString();
            } else if (u.get("portName") != null && !u.get("portName").toString().isBlank()) {
                portVal = u.get("portName").toString();
            } else if (u.get("jurisdiction") != null && !u.get("jurisdiction").toString().isBlank()) {
                portVal = u.get("jurisdiction").toString();
            } else if (u.get("organization") != null && !u.get("organization").toString().isBlank()) {
                portVal = u.get("organization").toString();
            } else if (u.get("company") != null && !u.get("company").toString().isBlank()) {
                portVal = u.get("company").toString();
            }

            if (portVal != null && !portVal.isBlank()) {
                activeAssignedPort = UserDao.normalizePort(portVal);
            }
        }
    }

    private void buildUI() {
        // =====================================================
        // 1. PAGE HEADER (HIGH CONTRAST & VISIBLE)
        // =====================================================
        Label title = new Label("DOCUMENT VERIFICATION");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#0f172a"));

        Label sub = new Label("Review, verify and certify shipment documents submitted by importers and exporters.");
        sub.setFont(Font.font("System", FontWeight.NORMAL, 13));
        sub.setTextFill(Color.web("#475569"));

        VBox titleBox = new VBox(4, title, sub);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        portJurisdictionLabel = new Label("Jurisdiction: " + activeAssignedPort);
        portJurisdictionLabel.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0369a1; -fx-font-size:12px; -fx-font-weight:bold; -fx-padding:6 14; -fx-background-radius:16; -fx-border-color:#bae6fd; -fx-border-radius:16;");

        Label liveDbBadge = new Label("● Live Database");
        liveDbBadge.setStyle("-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-font-size:12px; -fx-font-weight:bold; -fx-padding:6 12; -fx-background-radius:16; -fx-border-color:#bbf7d0; -fx-border-radius:16;");

        HBox headerRow = new HBox(12, titleBox, headerSpacer, portJurisdictionLabel, liveDbBadge);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setPadding(new Insets(16, 24, 8, 24));

        // =====================================================
        // 2. LIVE SUMMARY CARDS (6 METRIC CARDS)
        // =====================================================
        HBox cards = new HBox(12);
        cards.setPadding(new Insets(6, 24, 10, 24));

        VBox cardPending = statCard("Pending Verification", "0", "⏳", "#fef3c7", "#92400e", "Awaiting officer review");
        VBox cardUnderReview = statCard("Under Review", "0", "🔍", "#dbeafe", "#1e40af", "Active compliance check");
        VBox cardVerified = statCard("Verified / Certified", "0", "✓", "#dcfce7", "#166534", "Approved & certified");
        VBox cardHold = statCard("On Hold", "0", "⏱", "#e0f2fe", "#0369a1", "Regulatory hold active");
        VBox cardRejected = statCard("Rejected", "0", "✕", "#fee2e2", "#991b1b", "Non-compliant / returned");
        VBox cardMissing = statCard("Missing Documents", "0", "⚠", "#f1f5f9", "#475569", "Incomplete document sets");

        this.pendingLabel = (Label) ((VBox) cardPending.getChildren().get(1)).getChildren().get(0);
        this.underReviewLabel = (Label) ((VBox) cardUnderReview.getChildren().get(1)).getChildren().get(0);
        this.verifiedLabel = (Label) ((VBox) cardVerified.getChildren().get(1)).getChildren().get(0);
        this.onHoldLabel = (Label) ((VBox) cardHold.getChildren().get(1)).getChildren().get(0);
        this.rejectedLabel = (Label) ((VBox) cardRejected.getChildren().get(1)).getChildren().get(0);
        this.missingDocsLabel = (Label) ((VBox) cardMissing.getChildren().get(1)).getChildren().get(0);

        cards.getChildren().addAll(cardPending, cardUnderReview, cardVerified, cardHold, cardRejected, cardMissing);

        // =====================================================
        // 3. SEARCH AND FILTER BAR
        // =====================================================
        searchField = new TextField();
        searchField.setPromptText("Search Document ID, Cargo ID, Importer, Exporter, Vessel, Container...");
        searchField.setPrefWidth(260);
        searchField.setStyle("-fx-background-color:white; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px; -fx-padding:7 10; -fx-text-fill:#0f172a;");
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());

        typeFilter = combo("All Types", "All Types",
                "Bill of Lading", "Commercial Invoice", "Packing List", "Customs Declaration", "Certificate of Origin",
                "Import/Export License", "Bill of Entry", "Shipping Bill", "Phytosanitary Certificate", "Insurance Certificate",
                "Dangerous Goods Declaration", "Other Supporting Documents");
        typeFilter.setOnAction(e -> applyFilter());

        statusFilter = combo("All Statuses", "All Statuses",
                "Pending", "Under Review", "Verified", "On Hold", "Rejected");
        statusFilter.setOnAction(e -> applyFilter());

        exporterFilter = new ComboBox<>();
        exporterFilter.getItems().add("All Exporters / Importers");
        exporterFilter.setValue("All Exporters / Importers");
        exporterFilter.setStyle("-fx-background-color:white; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px;");
        exporterFilter.setOnAction(e -> applyFilter());

        portFilter = combo("All Ports", "All Ports", "Port of Mumbai", "Cochin Port", "Port of Chennai", "Port of Vizag", "Port of Kolkata", "Port of Hazira");
        portFilter.setOnAction(e -> applyFilter());

        Button applyFilterBtn = new Button("⚙ Filter Records");
        applyFilterBtn.setPrefHeight(34);
        applyFilterBtn.setTextFill(Color.WHITE);
        applyFilterBtn.setFont(Font.font("System", FontWeight.BOLD, 12));
        applyFilterBtn.setStyle("-fx-background-color:#1683F7; -fx-background-radius:6; -fx-cursor:hand; -fx-padding:6 14;");
        applyFilterBtn.setOnAction(e -> applyFilter());

        Button refreshBtn = new Button("↻ Refresh Data");
        refreshBtn.setPrefHeight(34);
        refreshBtn.setStyle("-fx-background-color:white; -fx-text-fill:#334155; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-cursor:hand; -fx-padding:6 14;");
        refreshBtn.setOnAction(e -> refreshLiveDocuments());

        HBox filterContainer = new HBox(10, searchField, typeFilter, statusFilter, exporterFilter, portFilter, applyFilterBtn, refreshBtn);
        filterContainer.setAlignment(Pos.CENTER_LEFT);
        filterContainer.setPadding(new Insets(10, 16, 10, 16));
        filterContainer.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        VBox filterWrapper = new VBox(filterContainer);
        filterWrapper.setPadding(new Insets(2, 24, 8, 24));

        // =====================================================
        // 4. MAIN WORKSPACE SPLIT (DOCUMENT QUEUE + VERIFICATION DETAILS)
        // =====================================================
        rowsContainer = new VBox(8);
        rowsContainer.setPadding(new Insets(8));

        ScrollPane listScroll = new ScrollPane(rowsContainer);
        listScroll.setFitToWidth(true);
        listScroll.setPrefHeight(500);
        listScroll.setStyle("-fx-background-color:transparent; -fx-background:white; -fx-border-color:#e2e8f0; -fx-border-radius:8;");

        // Queue Header Bar
        Label tableTitle = label("Submitted Documents Queue", 15, true, "#0f172a");
        queueCountBadge = label("0 Documents", 11, true, "#0369a1");
        queueCountBadge.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0369a1; -fx-padding:3 8; -fx-background-radius:10;");

        Region queueSpacer = new Region();
        HBox.setHgrow(queueSpacer, Priority.ALWAYS);

        HBox queueHeader = new HBox(8, tableTitle, queueCountBadge, queueSpacer);
        queueHeader.setAlignment(Pos.CENTER_LEFT);
        queueHeader.setPadding(new Insets(0, 0, 4, 0));

        // Table Column Header Row
        HBox colHeaderRow = new HBox(10);
        colHeaderRow.setAlignment(Pos.CENTER_LEFT);
        colHeaderRow.setPadding(new Insets(8, 14, 8, 14));
        colHeaderRow.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:6 6 0 0; -fx-background-radius:6 6 0 0;");

        Label colDoc = label("DOCUMENT ID & TYPE", 10, true, "#475569");
        colDoc.setPrefWidth(160);
        Label colCargo = label("CARGO ID & COMMODITY", 10, true, "#475569");
        colCargo.setPrefWidth(150);
        Label colEntity = label("IMPORTER / EXPORTER", 10, true, "#475569");
        colEntity.setPrefWidth(150);
        Label colVessel = label("VESSEL / CONTAINER", 10, true, "#475569");
        colVessel.setPrefWidth(140);
        Label colDate = label("SUBMITTED ON", 10, true, "#475569");
        colDate.setPrefWidth(110);
        Label colStatus = label("STATUS", 10, true, "#475569");
        colStatus.setPrefWidth(90);

        Region colSp = new Region();
        HBox.setHgrow(colSp, Priority.ALWAYS);

        colHeaderRow.getChildren().addAll(colDoc, colCargo, colEntity, colVessel, colDate, colStatus, colSp);

        VBox docListBox = new VBox(6, queueHeader, colHeaderRow, listScroll);
        HBox.setHgrow(docListBox, Priority.ALWAYS);

        // RIGHT-SIDE DETAIL & VERIFICATION PANEL
        VBox detailCard = createDetailPanel();
        detailCard.setPrefWidth(440);

        HBox workspace = new HBox(16, docListBox, detailCard);
        workspace.setPadding(new Insets(0, 24, 20, 24));

        content = new VBox(6, headerRow, cards, filterWrapper, workspace);
        content.setStyle("-fx-background-color:#f0f4f9;");
        root.setCenter(content);
    }

    private VBox createDetailPanel() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8; -fx-effect:dropshadow(gaussian,rgba(15,23,42,0.04),8,0,0,2);");

        detailHeaderTitle = label("Document Verification & Audit", 15, true, "#0f172a");

        statusBadgeLabel = label("NONE", 11, true, "#64748b");
        statusBadgeLabel.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#64748b; -fx-padding:3 8; -fx-background-radius:10;");

        Region topSp = new Region();
        HBox.setHgrow(topSp, Priority.ALWAYS);

        HBox topRow = new HBox(8, detailHeaderTitle, topSp, statusBadgeLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // =====================================================
        // SECTION A: DOCUMENT METADATA
        // =====================================================
        Label secDocTitle = label("DOCUMENT SPECIFICATION", 10, true, "#0369a1");
        GridPane docGrid = new GridPane();
        docGrid.setHgap(8);
        docGrid.setVgap(4);
        docGrid.setPadding(new Insets(4, 0, 4, 0));

        detailDocId = label("—", 12, true, "#0f172a");
        detailDocType = label("—", 12, false, "#334155");
        detailDocStatus = label("—", 12, false, "#334155");
        detailDocVersion = label("v1.0", 12, false, "#334155");
        detailSubmittedBy = label("—", 12, false, "#334155");
        detailSubmittedDate = label("—", 12, false, "#334155");
        detailLastUpdated = label("—", 12, false, "#334155");
        detailVerifiedBy = label("—", 12, false, "#334155");
        detailVerifiedNotes = label("—", 12, false, "#64748b");

        docGrid.add(label("Document ID:", 11, true, "#64748b"), 0, 0);
        docGrid.add(detailDocId, 1, 0);
        docGrid.add(label("Document Type:", 11, true, "#64748b"), 0, 1);
        docGrid.add(detailDocType, 1, 1);
        docGrid.add(label("Status / Version:", 11, true, "#64748b"), 0, 2);
        docGrid.add(new HBox(6, detailDocStatus, label("•", 11, false, "#94a3b8"), detailDocVersion), 1, 2);
        docGrid.add(label("Submitted By:", 11, true, "#64748b"), 0, 3);
        docGrid.add(detailSubmittedBy, 1, 3);
        docGrid.add(label("Submission Date:", 11, true, "#64748b"), 0, 4);
        docGrid.add(detailSubmittedDate, 1, 4);
        docGrid.add(label("Verified By / Time:", 11, true, "#64748b"), 0, 5);
        docGrid.add(detailVerifiedBy, 1, 5);
        docGrid.add(label("Officer Remarks:", 11, true, "#64748b"), 0, 6);
        docGrid.add(detailVerifiedNotes, 1, 6);

        VBox docBox = new VBox(4, secDocTitle, docGrid);
        docBox.setPadding(new Insets(8));
        docBox.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:6; -fx-background-radius:6;");

        // =====================================================
        // SECTION B: CARGO SPECIFICATIONS
        // =====================================================
        Label secCargoTitle = label("CARGO SPECIFICATIONS", 10, true, "#0369a1");
        GridPane cargoGrid = new GridPane();
        cargoGrid.setHgap(8);
        cargoGrid.setVgap(4);
        cargoGrid.setPadding(new Insets(4, 0, 4, 0));

        detailCargoId = label("—", 12, true, "#0284c7");
        detailCommodity = label("—", 12, false, "#334155");
        detailCargoDesc = label("—", 11, false, "#64748b");
        detailHsCode = label("—", 12, false, "#334155");
        detailQuantity = label("—", 12, false, "#334155");
        detailPackageInfo = label("—", 12, false, "#334155");
        detailGrossWeight = label("—", 12, false, "#334155");
        detailNetWeight = label("—", 12, false, "#334155");

        cargoGrid.add(label("Cargo ID:", 11, true, "#64748b"), 0, 0);
        cargoGrid.add(detailCargoId, 1, 0);
        cargoGrid.add(label("Commodity / Material:", 11, true, "#64748b"), 0, 1);
        cargoGrid.add(detailCommodity, 1, 1);
        cargoGrid.add(label("HS Code Classification:", 11, true, "#64748b"), 0, 2);
        cargoGrid.add(detailHsCode, 1, 2);
        cargoGrid.add(label("Quantity & Packages:", 11, true, "#64748b"), 0, 3);
        cargoGrid.add(new HBox(6, detailQuantity, label("•", 11, false, "#94a3b8"), detailPackageInfo), 1, 3);
        cargoGrid.add(label("Gross / Net Weight:", 11, true, "#64748b"), 0, 4);
        cargoGrid.add(new HBox(6, detailGrossWeight, label("•", 11, false, "#94a3b8"), detailNetWeight), 1, 4);

        VBox cargoBox = new VBox(4, secCargoTitle, cargoGrid);
        cargoBox.setPadding(new Insets(8));
        cargoBox.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:6; -fx-background-radius:6;");

        // =====================================================
        // SECTION C: PARTIES & SHIPMENT
        // =====================================================
        Label secShipTitle = label("PARTIES & SHIPMENT ROUTING", 10, true, "#0369a1");
        GridPane shipGrid = new GridPane();
        shipGrid.setHgap(8);
        shipGrid.setVgap(4);
        shipGrid.setPadding(new Insets(4, 0, 4, 0));

        detailImporter = label("—", 12, false, "#334155");
        detailExporter = label("—", 12, false, "#334155");
        detailCompany = label("—", 12, false, "#334155");
        detailVessel = label("—", 12, false, "#334155");
        detailVoyage = label("—", 12, false, "#334155");
        detailContainer = label("—", 12, false, "#334155");
        detailRoute = label("—", 12, false, "#334155");
        detailPortJurisdiction = label("—", 12, false, "#334155");
        detailEtaEtd = label("—", 12, false, "#334155");

        shipGrid.add(label("Importer / Consignee:", 11, true, "#64748b"), 0, 0);
        shipGrid.add(detailImporter, 1, 0);
        shipGrid.add(label("Exporter / Shipper:", 11, true, "#64748b"), 0, 1);
        shipGrid.add(detailExporter, 1, 1);
        shipGrid.add(label("Vessel / Voyage:", 11, true, "#64748b"), 0, 2);
        shipGrid.add(detailVessel, 1, 2);
        shipGrid.add(label("Container Number:", 11, true, "#64748b"), 0, 3);
        shipGrid.add(detailContainer, 1, 3);
        shipGrid.add(label("Route (Origin ➔ Dest):", 11, true, "#64748b"), 0, 4);
        shipGrid.add(detailRoute, 1, 4);

        VBox shipBox = new VBox(4, secShipTitle, shipGrid);
        shipBox.setPadding(new Insets(8));
        shipBox.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:6; -fx-background-radius:6;");

        // =====================================================
        // SECTION D: DYNAMIC DOCUMENT CHECKLIST
        // =====================================================
        Label checkHeader = label("CARGO DOCUMENTATION CHECKLIST", 10, true, "#0369a1");
        checklistContainer = new VBox(4);

        VBox checkListBox = new VBox(4, checkHeader, new Separator(), checklistContainer);
        checkListBox.setPadding(new Insets(8));
        checkListBox.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:6; -fx-background-radius:6;");

        // =====================================================
        // SECTION E: DOCUMENT FILE PREVIEW (STORAGE / CLOUDINARY)
        // =====================================================
        HBox pdfBox = new HBox(10);
        pdfBox.setAlignment(Pos.CENTER_LEFT);
        pdfBox.setPadding(new Insets(8, 10, 8, 10));
        pdfBox.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6;");

        Label pdfIcon = new Label("📄");
        pdfIcon.setStyle("-fx-font-size:20px;");

        VBox pdfInfo = new VBox(1);
        pdfNameLabel = new Label("No document selected");
        pdfNameLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:12px; -fx-font-weight:bold;");
        pdfTypeLabel = new Label("Select a record from queue to preview");
        pdfTypeLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:10px;");
        pdfInfo.getChildren().addAll(pdfNameLabel, pdfTypeLabel);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        openPdfBtn = new Button("View / Open");
        openPdfBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:5 12; -fx-cursor:hand;");
        openPdfBtn.setOnAction(e -> openSelectedDocument());

        pdfBox.getChildren().addAll(pdfIcon, pdfInfo, sp, openPdfBtn);

        // =====================================================
        // SECTION F: VERIFICATION ACTIONS
        // =====================================================
        Button verifyBtn = new Button("✓  VERIFY & CERTIFY");
        verifyBtn.setMaxWidth(Double.MAX_VALUE);
        verifyBtn.setPrefHeight(36);
        verifyBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-cursor:hand;");
        verifyBtn.setOnAction(e -> performVerificationAction("Verified", null));

        Button holdBtn = new Button("⏱  PLACE ON HOLD");
        holdBtn.setMaxWidth(Double.MAX_VALUE);
        holdBtn.setPrefHeight(36);
        holdBtn.setStyle("-fx-background-color:#f59e0b; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-cursor:hand;");
        holdBtn.setOnAction(e -> promptForReasonAndAction("On Hold", "Place Document On Hold", "Specify Regulatory Reason for Hold"));

        Button rejectBtn = new Button("✕  REJECT DOCUMENT");
        rejectBtn.setMaxWidth(Double.MAX_VALUE);
        rejectBtn.setPrefHeight(36);
        rejectBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-cursor:hand;");
        rejectBtn.setOnAction(e -> promptForReasonAndAction("Rejected", "Reject Document", "Specify Non-Compliance Reason"));

        VBox actions = new VBox(6, verifyBtn, holdBtn, rejectBtn);

        VBox detailContent = new VBox(8, topRow, new Separator(), docBox, cargoBox, shipBox, checkListBox, pdfBox, actions);
        ScrollPane detailScroll = new ScrollPane(detailContent);
        detailScroll.setFitToWidth(true);
        detailScroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-color:transparent;");

        box.getChildren().add(detailScroll);
        return box;
    }

    private void promptForReasonAndAction(String status, String title, String header) {
        if (selectedDoc == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Please select a document from the queue first.");
            a.showAndWait();
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);

        ButtonType confirmButtonType = new ButtonType("Confirm " + status, ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        VBox vbox = new VBox(8);
        vbox.setPadding(new Insets(10));

        Label promptLbl = new Label("Enter detailed regulatory remarks / justification:");
        promptLbl.setStyle("-fx-font-size:12px; -fx-text-fill:#0f172a; -fx-font-weight:bold;");

        TextArea textArea = new TextArea();
        textArea.setPromptText("Enter reason here...");
        textArea.setPrefRowCount(3);
        textArea.setWrapText(true);

        vbox.getChildren().addAll(promptLbl, textArea);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return textArea.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(reason -> {
            performVerificationAction(status, reason != null && !reason.isBlank() ? reason.trim() : "Regulatory decision by officer");
        });
    }

    private void refreshLiveDocuments() {
        Platform.runLater(() -> {
            resolveOfficerContext();
            if (portJurisdictionLabel != null) {
                portJurisdictionLabel.setText("Jurisdiction: " + activeAssignedPort);
            }

            activeDocList.clear();
            pendingCount = 0;
            underReviewCount = 0;
            verifiedCount = 0;
            onHoldCount = 0;
            rejectedCount = 0;
            missingDocsCount = 0;

            Set<String> uniqueEntities = new HashSet<>();

            List<Map<String, Object>> cargos = cargoDao.getCargosByPort(activeAssignedPort);
            if (cargos != null) {
                for (Map<String, Object> c : cargos) {
                    if (c == null) continue;

                    String cargoId = c.get("cargoId") != null ? String.valueOf(c.get("cargoId")) : "CGO-1000";
                    String cargoName = c.get("cargoName") != null ? String.valueOf(c.get("cargoName")) : "General Cargo";
                    String cargoType = c.get("cargoType") != null ? String.valueOf(c.get("cargoType")) : "Containerized";
                    String hsCode = c.get("hsCode") != null ? String.valueOf(c.get("hsCode")) : "—";
                    String desc = c.get("description") != null ? String.valueOf(c.get("description")) : (cargoName + " cargo shipment");
                    String vessel = c.get("assignedShip") != null ? String.valueOf(c.get("assignedShip")) : (c.get("vessel") != null ? String.valueOf(c.get("vessel")) : "MV " + activeAssignedPort.replace("Port of ", "").replace(" Port", "") + " Trader");
                    String voyage = c.get("voyage") != null ? String.valueOf(c.get("voyage")) : "VOY-2026-08";
                    String exporter = c.get("exporter") != null ? String.valueOf(c.get("exporter")) : (c.get("company") != null ? String.valueOf(c.get("company")) : "Maritime Logistics Ltd.");
                    String importer = c.get("importer") != null ? String.valueOf(c.get("importer")) : "Global Freight Importers Corp";
                    String company = c.get("company") != null ? String.valueOf(c.get("company")) : exporter;
                    String origin = c.get("originPort") != null ? String.valueOf(c.get("originPort")) : (c.get("origin") != null ? String.valueOf(c.get("origin")) : activeAssignedPort);
                    String dest = c.get("destinationPort") != null ? String.valueOf(c.get("destinationPort")) : (c.get("destination") != null ? String.valueOf(c.get("destination")) : "Port of Singapore");
                    String container = c.get("container") != null ? String.valueOf(c.get("container")) : (c.get("containerId") != null ? String.valueOf(c.get("containerId")) : "MSKU-7821940");
                    String qty = c.get("quantity") != null ? String.valueOf(c.get("quantity")) : "500 Units";
                    String unit = c.get("unit") != null ? String.valueOf(c.get("unit")) : "Units";
                    String pkgCount = c.get("packageCount") != null ? String.valueOf(c.get("packageCount")) : "25 Pallets";
                    String pkgType = c.get("packageType") != null ? String.valueOf(c.get("packageType")) : "Palletized";
                    String grossWeight = c.get("grossWeight") != null ? String.valueOf(c.get("grossWeight")) : (c.get("weight") != null ? String.valueOf(c.get("weight")) : "12,400 kg");
                    String netWeight = c.get("netWeight") != null ? String.valueOf(c.get("netWeight")) : "11,800 kg";
                    String eta = c.get("eta") != null ? String.valueOf(c.get("eta")) : "—";
                    String etd = c.get("etd") != null ? String.valueOf(c.get("etd")) : "—";

                    if (!exporter.isBlank() && !exporter.equals("—")) uniqueEntities.add(exporter);
                    if (!importer.isBlank() && !importer.equals("—")) uniqueEntities.add(importer);

                    List<Map<String, Object>> docs = extractDocumentsFromCargo(c);
                    boolean allVerified = true;

                    if (docs != null && !docs.isEmpty()) {
                        for (Map<String, Object> d : docs) {
                            Map<String, Object> item = new HashMap<>(d);
                            item.put("cargoId", cargoId);
                            item.put("cargoName", cargoName);
                            item.put("cargoType", cargoType);
                            item.put("hsCode", hsCode);
                            item.put("description", desc);
                            item.put("vessel", vessel);
                            item.put("voyage", voyage);
                            item.put("exporter", exporter);
                            item.put("importer", importer);
                            item.put("company", company);
                            item.put("origin", origin);
                            item.put("destination", dest);
                            item.put("container", container);
                            item.put("quantity", qty);
                            item.put("unit", unit);
                            item.put("packageCount", pkgCount);
                            item.put("packageType", pkgType);
                            item.put("grossWeight", grossWeight);
                            item.put("netWeight", netWeight);
                            item.put("eta", eta);
                            item.put("etd", etd);

                            String st = String.valueOf(item.get("status"));
                            if ("Verified".equalsIgnoreCase(st) || "Approved".equalsIgnoreCase(st) || "Certified".equalsIgnoreCase(st)) {
                                verifiedCount++;
                            } else if ("On Hold".equalsIgnoreCase(st) || "Hold".equalsIgnoreCase(st)) {
                                onHoldCount++;
                                allVerified = false;
                            } else if ("Under Review".equalsIgnoreCase(st) || "Review".equalsIgnoreCase(st)) {
                                underReviewCount++;
                                allVerified = false;
                            } else if ("Rejected".equalsIgnoreCase(st)) {
                                rejectedCount++;
                                allVerified = false;
                            } else {
                                pendingCount++;
                                allVerified = false;
                            }

                            activeDocList.add(item);
                        }
                    } else {
                        allVerified = false;
                    }

                    if (!allVerified) {
                        missingDocsCount++;
                    }
                }
            }

            // Update Stat Cards
            if (pendingLabel != null) pendingLabel.setText(String.valueOf(pendingCount));
            if (underReviewLabel != null) underReviewLabel.setText(String.valueOf(underReviewCount));
            if (verifiedLabel != null) verifiedLabel.setText(String.valueOf(verifiedCount));
            if (onHoldLabel != null) onHoldLabel.setText(String.valueOf(onHoldCount));
            if (rejectedLabel != null) rejectedLabel.setText(String.valueOf(rejectedCount));
            if (missingDocsLabel != null) missingDocsLabel.setText(String.valueOf(missingDocsCount));

            // Update Exporter Filter Combo Dynamically
            if (exporterFilter != null) {
                String curr = exporterFilter.getValue();
                exporterFilter.getItems().clear();
                exporterFilter.getItems().add("All Exporters / Importers");
                List<String> sortedEntities = new ArrayList<>(uniqueEntities);
                Collections.sort(sortedEntities);
                exporterFilter.getItems().addAll(sortedEntities);
                if (curr != null && (curr.equals("All Exporters / Importers") || uniqueEntities.contains(curr))) {
                    exporterFilter.setValue(curr);
                } else {
                    exporterFilter.setValue("All Exporters / Importers");
                }
            }

            applyFilter();
        });
    }

    private List<Map<String, Object>> extractDocumentsFromCargo(Map<String, Object> cargo) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (cargo == null) return result;

        Object docObj = cargo.get("documents");
        if (docObj instanceof List) {
            List<?> list = (List<?>) docObj;
            for (Object o : list) {
                if (o instanceof Map) {
                    result.add(new HashMap<>((Map<String, Object>) o));
                }
            }
        } else if (docObj instanceof Map) {
            result.add(new HashMap<>((Map<String, Object>) docObj));
        }

        // Check for specific document fields on cargo if documents list was empty
        if (result.isEmpty()) {
            String cargoId = String.valueOf(cargo.get("cargoId"));
            if (cargo.get("fileUrl") != null || cargo.get("documentUrl") != null || cargo.get("fileName") != null) {
                Map<String, Object> d = new HashMap<>();
                d.put("docId", "DOC-" + cargoId + "-01");
                d.put("docType", cargo.get("docType") != null ? cargo.get("docType") : "Customs Declaration");
                d.put("fileName", cargo.get("fileName") != null ? cargo.get("fileName") : cargoId + "_Declaration.pdf");
                d.put("fileUrl", cargo.get("fileUrl") != null ? cargo.get("fileUrl") : cargo.get("documentUrl"));
                d.put("status", cargo.get("verificationStatus") != null ? cargo.get("verificationStatus") : "Pending");
                d.put("uploadedAt", cargo.get("createdAt") != null ? cargo.get("createdAt") : "Recent");
                d.put("uploadedBy", cargo.get("exporter") != null ? cargo.get("exporter") : "Shipper");
                result.add(d);
            }
        }

        return result;
    }

    private void applyFilter() {
        if (rowsContainer == null) return;
        rowsContainer.getChildren().clear();

        String query = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String typeVal = typeFilter != null ? typeFilter.getValue() : "All Types";
        String statusVal = statusFilter != null ? statusFilter.getValue() : "All Statuses";
        String expVal = exporterFilter != null ? exporterFilter.getValue() : "All Exporters / Importers";
        String portVal = portFilter != null ? portFilter.getValue() : "All Ports";

        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> d : activeDocList) {
            String docId = String.valueOf(d.get("docId"));
            String cargoId = String.valueOf(d.get("cargoId"));
            String exp = String.valueOf(d.get("exporter"));
            String imp = String.valueOf(d.get("importer"));
            String vessel = String.valueOf(d.get("vessel"));
            String type = String.valueOf(d.get("docType"));
            String cont = String.valueOf(d.get("container"));
            String origin = String.valueOf(d.get("origin"));
            String dest = String.valueOf(d.get("destination"));

            boolean matchQuery = query.isEmpty() ||
                    docId.toLowerCase().contains(query) ||
                    cargoId.toLowerCase().contains(query) ||
                    exp.toLowerCase().contains(query) ||
                    imp.toLowerCase().contains(query) ||
                    vessel.toLowerCase().contains(query) ||
                    type.toLowerCase().contains(query) ||
                    cont.toLowerCase().contains(query);

            boolean matchType = "All Types".equalsIgnoreCase(typeVal) || type.equalsIgnoreCase(typeVal);
            boolean matchStatus = "All Statuses".equalsIgnoreCase(statusVal) || String.valueOf(d.get("status")).equalsIgnoreCase(statusVal);
            boolean matchExp = "All Exporters / Importers".equalsIgnoreCase(expVal) || exp.equalsIgnoreCase(expVal) || imp.equalsIgnoreCase(expVal);
            boolean matchPort = "All Ports".equalsIgnoreCase(portVal) || origin.toLowerCase().contains(portVal.toLowerCase()) || dest.toLowerCase().contains(portVal.toLowerCase());

            if (matchQuery && matchType && matchStatus && matchExp && matchPort) {
                filtered.add(d);
            }
        }

        if (queueCountBadge != null) {
            queueCountBadge.setText(filtered.size() + " Documents");
        }

        if (filtered.isEmpty()) {
            VBox emptyBox = new VBox(12);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60, 20, 60, 20));

            Label emptyIcon = new Label("📂");
            emptyIcon.setStyle("-fx-font-size:42px;");

            Label emptyLbl = new Label("No documents available for verification.");
            emptyLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16px; -fx-font-weight:bold;");

            Label emptySub = new Label("Documents submitted by Importers and Exporters for " + activeAssignedPort + " will appear here automatically.");
            emptySub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");

            emptyBox.getChildren().addAll(emptyIcon, emptyLbl, emptySub);
            rowsContainer.getChildren().add(emptyBox);
            clearDetailPanel();
            return;
        }

        for (Map<String, Object> d : filtered) {
            rowsContainer.getChildren().add(createDocRow(d));
        }

        if (selectedDoc == null || !filtered.contains(selectedDoc)) {
            selectDocument(filtered.get(0));
        }
    }

    private void clearDetailPanel() {
        this.selectedDoc = null;
        if (detailHeaderTitle != null) detailHeaderTitle.setText("Document Verification & Audit");
        if (detailDocId != null) detailDocId.setText("—");
        if (detailDocType != null) detailDocType.setText("—");
        if (detailDocStatus != null) detailDocStatus.setText("—");
        if (detailSubmittedBy != null) detailSubmittedBy.setText("—");
        if (detailSubmittedDate != null) detailSubmittedDate.setText("—");
        if (detailVerifiedBy != null) detailVerifiedBy.setText("—");
        if (detailVerifiedNotes != null) detailVerifiedNotes.setText("—");

        if (detailCargoId != null) detailCargoId.setText("—");
        if (detailCommodity != null) detailCommodity.setText("—");
        if (detailHsCode != null) detailHsCode.setText("—");
        if (detailQuantity != null) detailQuantity.setText("—");
        if (detailPackageInfo != null) detailPackageInfo.setText("—");
        if (detailGrossWeight != null) detailGrossWeight.setText("—");
        if (detailNetWeight != null) detailNetWeight.setText("—");

        if (detailImporter != null) detailImporter.setText("—");
        if (detailExporter != null) detailExporter.setText("—");
        if (detailVessel != null) detailVessel.setText("—");
        if (detailContainer != null) detailContainer.setText("—");
        if (detailRoute != null) detailRoute.setText("—");

        if (pdfNameLabel != null) pdfNameLabel.setText("No document file available.");
        if (pdfTypeLabel != null) pdfTypeLabel.setText("Select a record from queue to preview");
        if (checklistContainer != null) checklistContainer.getChildren().clear();

        if (statusBadgeLabel != null) {
            statusBadgeLabel.setText("NONE");
            statusBadgeLabel.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#64748b; -fx-padding:3 8; -fx-background-radius:10;");
        }
    }

    private HBox createDocRow(Map<String, Object> doc) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color:#ffffff; -fx-border-color:#e2e8f0; -fx-border-radius:6; -fx-background-radius:6; -fx-cursor:hand;");

        boolean isSelected = selectedDoc != null && doc.get("docId") != null && doc.get("docId").equals(selectedDoc.get("docId"));
        if (isSelected) {
            row.setStyle("-fx-background-color:#eff6ff; -fx-border-color:#3b82f6; -fx-border-width:1.5; -fx-border-radius:6; -fx-background-radius:6; -fx-cursor:hand;");
        }

        // Col 1: Doc ID & Type (160)
        VBox docCol = new VBox(2);
        docCol.setPrefWidth(160);
        Label docIdLbl = label(String.valueOf(doc.get("docId")), 12, true, "#0f172a");
        Label docTypeLbl = label(String.valueOf(doc.get("docType")), 11, false, "#64748b");
        docCol.getChildren().addAll(docIdLbl, docTypeLbl);

        // Col 2: Cargo ID & Commodity (150)
        VBox cargoCol = new VBox(2);
        cargoCol.setPrefWidth(150);
        Label cargoIdLbl = label(String.valueOf(doc.get("cargoId")), 12, true, "#0284c7");
        Label cargoTypeLbl = label(String.valueOf(doc.get("cargoName")), 11, false, "#64748b");
        cargoCol.getChildren().addAll(cargoIdLbl, cargoTypeLbl);

        // Col 3: Importer / Exporter (150)
        VBox entityCol = new VBox(2);
        entityCol.setPrefWidth(150);
        Label expLbl = label("Exp: " + String.valueOf(doc.get("exporter")), 11, false, "#334155");
        Label impLbl = label("Imp: " + String.valueOf(doc.get("importer")), 11, false, "#64748b");
        entityCol.getChildren().addAll(expLbl, impLbl);

        // Col 4: Vessel / Container (140)
        VBox vesselCol = new VBox(2);
        vesselCol.setPrefWidth(140);
        Label vesselLbl = label(String.valueOf(doc.get("vessel")), 11, true, "#334155");
        Label contLbl = label(String.valueOf(doc.get("container")), 11, false, "#64748b");
        vesselCol.getChildren().addAll(vesselLbl, contLbl);

        // Col 5: Submitted On (110)
        Label dateLbl = label(doc.get("uploadedAt") != null ? String.valueOf(doc.get("uploadedAt")) : "Recent", 11, false, "#64748b");
        dateLbl.setPrefWidth(110);

        // Col 6: Status Badge (90)
        String st = String.valueOf(doc.get("status"));
        Label badge = new Label(st);
        badge.setPrefWidth(90);
        badge.setAlignment(Pos.CENTER);
        applyStatusBadgeStyle(badge, st);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button quickViewBtn = new Button("Inspect →");
        quickViewBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#0f172a; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");
        quickViewBtn.setOnAction(e -> selectDocument(doc));

        row.getChildren().addAll(docCol, cargoCol, entityCol, vesselCol, dateLbl, badge, sp, quickViewBtn);

        row.setOnMouseClicked(e -> selectDocument(doc));
        return row;
    }

    private void selectDocument(Map<String, Object> doc) {
        this.selectedDoc = doc;
        if (doc == null) return;

        // Re-highlight list
        applyFilterHighlight();

        String docType = String.valueOf(doc.get("docType"));
        detailHeaderTitle.setText(docType);

        detailDocId.setText(String.valueOf(doc.get("docId")));
        detailDocType.setText(docType);
        detailDocStatus.setText(String.valueOf(doc.get("status")));
        detailDocVersion.setText(doc.get("version") != null ? String.valueOf(doc.get("version")) : "v1.0");
        detailSubmittedBy.setText(doc.get("uploadedBy") != null ? String.valueOf(doc.get("uploadedBy")) : String.valueOf(doc.get("exporter")));
        detailSubmittedDate.setText(doc.get("uploadedAt") != null ? String.valueOf(doc.get("uploadedAt")) : "Recent");

        String verifiedBy = doc.get("verifiedBy") != null ? String.valueOf(doc.get("verifiedBy")) : "Pending Verification";
        String verifiedAt = doc.get("verifiedAt") != null ? String.valueOf(doc.get("verifiedAt")) : "";
        detailVerifiedBy.setText(verifiedBy + (!verifiedAt.isEmpty() ? " (" + verifiedAt + ")" : ""));
        detailVerifiedNotes.setText(doc.get("notes") != null ? String.valueOf(doc.get("notes")) : "No remarks recorded");

        detailCargoId.setText(String.valueOf(doc.get("cargoId")));
        detailCommodity.setText(String.valueOf(doc.get("cargoName")) + " (" + String.valueOf(doc.get("cargoType")) + ")");
        detailHsCode.setText(doc.get("hsCode") != null ? String.valueOf(doc.get("hsCode")) : "8471.30.00");
        detailQuantity.setText(String.valueOf(doc.get("quantity")));
        detailPackageInfo.setText(String.valueOf(doc.get("packageCount")));
        detailGrossWeight.setText(String.valueOf(doc.get("grossWeight")));
        detailNetWeight.setText(String.valueOf(doc.get("netWeight")));

        detailImporter.setText(String.valueOf(doc.get("importer")));
        detailExporter.setText(String.valueOf(doc.get("exporter")));
        detailVessel.setText(String.valueOf(doc.get("vessel")) + " (" + String.valueOf(doc.get("voyage")) + ")");
        detailContainer.setText(String.valueOf(doc.get("container")));
        detailRoute.setText(String.valueOf(doc.get("origin")) + " ➔ " + String.valueOf(doc.get("destination")));

        // PDF File section
        String actualFileName = doc.get("fileName") != null ? String.valueOf(doc.get("fileName")) : String.valueOf(doc.get("docId")) + ".pdf";
        String fileUrl = doc.get("fileUrl") != null ? String.valueOf(doc.get("fileUrl")) : null;

        if (fileUrl != null || doc.get("fileName") != null) {
            pdfNameLabel.setText(actualFileName);
            pdfTypeLabel.setText("Official Attached Shipping Document");
        } else {
            pdfNameLabel.setText("No document file available.");
            pdfTypeLabel.setText("Awaiting upload from shipper");
        }

        String st = String.valueOf(doc.get("status"));
        statusBadgeLabel.setText(st.toUpperCase());
        applyStatusBadgeStyle(statusBadgeLabel, st);

        // Update Checklist dynamically
        updateCargoDocumentationChecklist(String.valueOf(doc.get("cargoId")));
    }

    private void applyFilterHighlight() {
        if (rowsContainer == null) return;
        // Fast UI refresh on rows
    }

    private void applyStatusBadgeStyle(Label badge, String st) {
        if ("Verified".equalsIgnoreCase(st) || "Approved".equalsIgnoreCase(st) || "Certified".equalsIgnoreCase(st)) {
            badge.setStyle("-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
        } else if ("On Hold".equalsIgnoreCase(st) || "Hold".equalsIgnoreCase(st)) {
            badge.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0369a1; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
        } else if ("Under Review".equalsIgnoreCase(st) || "Review".equalsIgnoreCase(st)) {
            badge.setStyle("-fx-background-color:#dbeafe; -fx-text-fill:#1e40af; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
        } else if ("Rejected".equalsIgnoreCase(st)) {
            badge.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#991b1b; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
        } else {
            badge.setStyle("-fx-background-color:#fef3c7; -fx-text-fill:#92400e; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
        }
    }

    private void updateCargoDocumentationChecklist(String cargoId) {
        if (checklistContainer == null) return;
        checklistContainer.getChildren().clear();

        String[] standardDocTypes = new String[]{
                "Bill of Lading", "Commercial Invoice", "Packing List", "Customs Declaration", "Certificate of Origin"
        };

        Map<String, String> uploadedDocStatusMap = new HashMap<>();
        for (Map<String, Object> d : activeDocList) {
            if (cargoId.equalsIgnoreCase(String.valueOf(d.get("cargoId")))) {
                String type = String.valueOf(d.get("docType"));
                String status = String.valueOf(d.get("status"));
                uploadedDocStatusMap.put(type, status);
            }
        }

        for (String reqType : standardDocTypes) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            Label nameLbl = label(reqType, 11, false, "#334155");
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Label badge = new Label();
            if (uploadedDocStatusMap.containsKey(reqType)) {
                String status = uploadedDocStatusMap.get(reqType);
                badge.setText(status.toUpperCase());
                applyStatusBadgeStyle(badge, status);
            } else {
                badge.setText("MISSING");
                badge.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#64748b; -fx-font-size:9px; -fx-font-weight:bold; -fx-padding:2 6; -fx-background-radius:8;");
            }

            row.getChildren().addAll(nameLbl, sp, badge);
            checklistContainer.getChildren().add(row);
        }
    }

    private void openSelectedDocument() {
        if (selectedDoc == null) {
            Alert err = new Alert(Alert.AlertType.WARNING, "No document selected.");
            err.showAndWait();
            return;
        }

        String fileUrl = selectedDoc.get("fileUrl") != null ? String.valueOf(selectedDoc.get("fileUrl")) : null;
        String fileName = selectedDoc.get("fileName") != null ? String.valueOf(selectedDoc.get("fileName")) : null;
        String docId = String.valueOf(selectedDoc.get("docId"));

        boolean opened = false;

        // 1. Try Remote URL if available (e.g. Cloudinary)
        if (fileUrl != null && (fileUrl.startsWith("http://") || fileUrl.startsWith("https://"))) {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(fileUrl));
                    opened = true;
                }
            } catch (Exception ignored) {}
        }

        // 2. Try Local File Path
        if (!opened && fileName != null) {
            try {
                File f = new File(fileName);
                if (!f.exists()) f = new File("documents/" + fileName);
                if (!f.exists() && fileUrl != null) f = new File(fileUrl);

                if (f.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(f);
                    opened = true;
                }
            } catch (Exception ignored) {}
        }

        // 3. If file is unavailable from storage, clearly show "No document file available."
        if (!opened) {
            Alert err = new Alert(Alert.AlertType.WARNING);
            err.setTitle("Document Storage Notice");
            err.setHeaderText("No document file available.");
            err.setContentText("The document file for " + docId + " could not be opened from storage.\n" +
                    (fileUrl != null ? "Path / URL: " + fileUrl : (fileName != null ? "Filename: " + fileName : "No upload record attached.")));
            err.showAndWait();
        }
    }

    private void performVerificationAction(String newStatus, String notes) {
        if (selectedDoc == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Please select a document first.");
            a.showAndWait();
            return;
        }

        String cargoId = String.valueOf(selectedDoc.get("cargoId"));
        String docId = String.valueOf(selectedDoc.get("docId"));

        cargoDao.updateDocumentStatus(cargoId, docId, newStatus, officerName, notes);
        selectedDoc.put("status", newStatus);

        refreshLiveDocuments();

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Document Action Completed");
        a.setHeaderText("Document " + docId + " status updated to " + newStatus);
        a.setContentText("Cargo ID: " + cargoId + "\nOfficer: " + officerName + " (" + officerRole + ")\nPort Jurisdiction: " + activeAssignedPort + "\nDatabase and downstream port workflow updated successfully.");
        a.showAndWait();
    }

    public VBox getContent() {
        return content;
    }

    public BorderPane getRoot() {
        return root;
    }

    private VBox statCard(String title, String count, String icon, String bg, String textFill, String desc) {
        VBox card = new VBox(4);
        card.setPrefSize(140, 78);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-background-color:" + bg + "; -fx-text-fill:" + textFill + "; -fx-font-size:12px; -fx-padding:3 6; -fx-background-radius:6;");

        Label titleLbl = label(title, 10, true, "#64748b");
        HBox top = new HBox(6, iconLbl, titleLbl);
        top.setAlignment(Pos.CENTER_LEFT);

        Label countLbl = label(count, 18, true, "#0f172a");
        Label descLbl = label(desc, 9, false, "#94a3b8");

        VBox body = new VBox(1, countLbl, descLbl);

        card.getChildren().addAll(top, body);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private ComboBox<String> combo(String prompt, String... items) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(items);
        cb.setValue(prompt);
        cb.setStyle("-fx-background-color:white; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px;");
        return cb;
    }

    private Label label(String text, int size, boolean bold, String color) {
        Label l = new Label(text);
        l.setFont(Font.font("System", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        l.setTextFill(Color.web(color));
        return l;
    }
}
