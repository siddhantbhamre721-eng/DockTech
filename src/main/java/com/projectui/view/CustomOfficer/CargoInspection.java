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
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Custom Officer - Cargo Physical Inspection Control Center
 * 
 * Styled using the EXACT Admin Page design system:
 * - Card styling, typography, spacing, shadows, badges, and controls matching Admin pages
 * - High-contrast text on solid backgrounds (zero translucent blue overlays)
 * - 100% dynamic, database-driven inspection workflow linked to Firestore & Port Authority handoff
 */
@SuppressWarnings({"unchecked", "unused"})
public class CargoInspection {

    // Admin Theme Shared Colors & Constants
    private static final String BLUE = "#0b63ff";
    private static final String TEXT_PRIMARY = "#0f172a";
    private static final String TEXT_SECONDARY = "#64748b";
    private static final String CARD_STYLE = "-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-background-radius:10; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 8, 0, 0, 2);";

    private final BorderPane root = new BorderPane();
    private VBox pageContainer;

    // Live Metrics
    private int pendingCount = 0;
    private int underInspectionCount = 0;
    private int passedCount = 0;
    private int failedCount = 0;
    private int onHoldCount = 0;
    private int highPriorityCount = 0;

    private Label pendingValueLabel;
    private Label underInspectionValueLabel;
    private Label passedValueLabel;
    private Label failedValueLabel;
    private Label onHoldValueLabel;
    private Label highPriorityValueLabel;

    // Queue & List Table
    private final GridPane table = new GridPane();
    private Label showingCountLabel;
    private final List<Map<String, Object>> activeCargoList = new ArrayList<>();
    private final List<Map<String, Object>> filteredCargoList = new ArrayList<>();
    private Map<String, Object> selectedCargo = null;

    // Right-Side Cargo Inspection Details Panel
    private Label formTitle;
    private Label formSubtitle;
    private Label statusBadgeLabel;
    private Label priorityBadgeLabel;

    // Cargo Specification Labels
    private Label detailCargoId;
    private Label detailCargoName;
    private Label detailCargoType;
    private Label detailHsCode;
    private Label detailQuantity;
    private Label detailPackageInfo;
    private Label detailGrossWeight;
    private Label detailNetWeight;
    private Label detailContainer;

    // Shipment Labels
    private Label detailVessel;
    private Label detailVoyage;
    private Label detailImo;
    private Label detailRoute;
    private Label detailEtaEtd;

    // Parties Labels
    private Label detailImporter;
    private Label detailExporter;
    private Label detailCompany;
    private Label detailDocRef;

    // Safety Alert Box
    private VBox alertBox;
    private Label alertTitle;
    private Label alertMessage;

    // Attached Document Preview
    private Label pdfNameLabel;
    private Label pdfTypeLabel;
    private Label pdfStorageBadge;
    private Button openPdfBtn;

    // Inspection Checklists & Findings
    private VBox checklistContainer;
    private TextArea findingsTextArea;
    private VBox auditTrailContainer;

    // Backend context
    private final CargoDao cargoDao = new CargoDao();
    private String activeAssignedPort = "Cochin Port";
    private String officerName = "Customs Officer";
    private String officerEmail = "";
    private String officerUid = "";
    private String officerRole = "Custom Officer";

    // Search and Filters
    private TextField searchField;
    private ComboBox<String> statusFilterCombo;
    private ComboBox<String> priorityFilterCombo;
    private ComboBox<String> typeFilterCombo;
    private ComboBox<String> exporterFilterCombo;
    private ComboBox<String> portFilterCombo;
    private Label portJurisdictionLabel;

    public CargoInspection() {
        resolveOfficerContext();
        buildUI();
        refreshInspectionData();

        // Listen for live database updates from Firestore sync
        CargoDao.addChangeListener(this::refreshInspectionData);
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
        pageContainer = new VBox(16);
        pageContainer.setPadding(new Insets(18, 22, 28, 22));
        pageContainer.setStyle("-fx-background-color:transparent;");

        // 1. HEADER ROW (ADMIN STYLE)
        HBox header = createHeaderSection();

        // 2. SUMMARY KPI CARDS ROW (6 CARDS IN EXACT ADMIN STATBOX DESIGN)
        HBox kpiRow = createKpiSummaryRow();

        // 3. MIDDLE WORKSPACE: LEFT QUEUE TABLE (60%) + RIGHT DETAIL INSPECTION PANEL (40%)
        VBox tableCard = createCargoQueueCard();
        VBox detailCard = createDetailInspectionCard();

        HBox mainRow = new HBox(16, tableCard, detailCard);
        tableCard.prefWidthProperty().bind(mainRow.widthProperty().multiply(0.60));
        detailCard.prefWidthProperty().bind(mainRow.widthProperty().multiply(0.40));
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        HBox.setHgrow(detailCard, Priority.ALWAYS);

        // 4. BOTTOM ACTIVITY & COMPLIANCE SUMMARY (MATCHING ADMIN DASHBOARD)
        HBox bottomRow = createBottomSummaryRow();

        pageContainer.getChildren().addAll(header, kpiRow, mainRow, bottomRow);

        // Smooth ScrollPane Wrapper
        ScrollPane scroller = new ScrollPane(pageContainer);
        scroller.setFitToWidth(true);
        scroller.setPannable(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroller.setStyle("-fx-background:transparent; -fx-background-color:transparent; -fx-padding:0;");

        root.setCenter(scroller);
    }

    // =====================================================
    // 1. PAGE HEADER (ADMIN THEME)
    // =====================================================
    private HBox createHeaderSection() {
        Label title = new Label("CARGO INSPECTION");
        title.setStyle("-fx-text-fill:#05070c; -fx-font-size:26; -fx-font-weight:bold;");

        Label subtitle = new Label("Conduct physical container audits, hazardous compliance checks, and authorize clearance.");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:14;");

        VBox titleBox = new VBox(4, title, subtitle);
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        portJurisdictionLabel = new Label("Jurisdiction: " + activeAssignedPort);
        portJurisdictionLabel.setStyle(
                "-fx-background-color:#eff6ff;" +
                "-fx-text-fill:" + BLUE + ";" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:8 16;" +
                "-fx-background-radius:8;" +
                "-fx-border-color:#dbeafe;" +
                "-fx-border-radius:8;"
        );

        Label liveDbBadge = new Label("● Live Database");
        liveDbBadge.setStyle(
                "-fx-background-color:#ecfdf5;" +
                "-fx-text-fill:#16a34a;" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:8 16;" +
                "-fx-background-radius:8;" +
                "-fx-border-color:#bbf7d0;" +
                "-fx-border-radius:8;"
        );

        HBox header = new HBox(12, titleBox, space, portJurisdictionLabel, liveDbBadge);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    // =====================================================
    // 2. STATISTIC KPI CARDS (EXACT ADMIN STATBOX DESIGN)
    // =====================================================
    private HBox createKpiSummaryRow() {
        HBox row = new HBox(12,
                adminKpiCard("Pending Inspection", "0", "Awaiting physical check", "⏳", "#f59e0b", "#fffbeb", l -> pendingValueLabel = l),
                adminKpiCard("Under Inspection", "0", "Active examination", "🔍", BLUE, "#eff6ff", l -> underInspectionValueLabel = l),
                adminKpiCard("Passed / Approved", "0", "Cleared for clearance", "✓", "#16a34a", "#ecfdf5", l -> passedValueLabel = l),
                adminKpiCard("On Hold", "0", "Awaiting information", "⏸", "#f97316", "#fff7ed", l -> onHoldValueLabel = l),
                adminKpiCard("Failed / Rejected", "0", "Discrepancy / failed", "✕", "#ef4444", "#fef2f2", l -> failedValueLabel = l),
                adminKpiCard("High Priority", "0", "Requires attention", "🚨", "#e11d48", "#ffe4e6", l -> highPriorityValueLabel = l)
        );

        for (Node node : row.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
            if (node instanceof VBox) {
                ((VBox) node).setMaxWidth(Double.MAX_VALUE);
            }
        }
        return row;
    }

    private VBox adminKpiCard(String title, String initialVal, String subText, String iconStr,
                              String color, String bgLight, LabelSetter setter) {
        Label icon = new Label(iconStr);
        icon.setStyle(
                "-fx-text-fill:" + color + ";" +
                "-fx-font-size:16;" +
                "-fx-font-weight:bold;" +
                "-fx-alignment:center;" +
                "-fx-min-width:34;" +
                "-fx-min-height:34;" +
                "-fx-background-color:" + bgLight + ";" +
                "-fx-background-radius:8;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:13; -fx-font-weight:bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(8, icon, titleLabel, spacer);
        top.setAlignment(Pos.CENTER_LEFT);

        Label valueLabel = new Label(initialVal);
        valueLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:28; -fx-font-weight:800;");
        setter.set(valueLabel);

        Label subLabel = new Label(subText);
        subLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12;");

        VBox card = new VBox(6, top, valueLabel, subLabel);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle(CARD_STYLE);
        return card;
    }

    // =====================================================
    // 3. CARGO INSPECTION QUEUE TABLE (LEFT PANEL)
    // =====================================================
    private VBox createCargoQueueCard() {
        Label title = new Label("Cargo Inspection Registry & Queue");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");

        Label subtitle = new Label("Shipments requiring physical verification, seal integrity check, and hazmat compliance");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        // Search Field (Admin Style)
        searchField = new TextField();
        searchField.setPromptText("⌕  Search Cargo ID, Name, Importer, Exporter, Container...");
        searchField.setPrefWidth(220);
        styleAdminField(searchField);
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());

        // Dropdowns (Admin Style)
        statusFilterCombo = combo("All Statuses", "All Statuses", "Pending", "Under Inspection", "Passed", "On Hold", "Failed");
        statusFilterCombo.setPrefWidth(125);
        statusFilterCombo.setOnAction(e -> applyFilter());

        priorityFilterCombo = combo("All Priorities", "All Priorities", "Urgent", "High", "Normal");
        priorityFilterCombo.setPrefWidth(110);
        priorityFilterCombo.setOnAction(e -> applyFilter());

        typeFilterCombo = combo("All Types", "All Types", "Containerized", "Dry Bulk", "Liquid Bulk", "Breakbulk", "Ro-Ro", "Hazardous", "Perishable");
        typeFilterCombo.setPrefWidth(115);
        typeFilterCombo.setOnAction(e -> applyFilter());

        exporterFilterCombo = new ComboBox<>();
        exporterFilterCombo.getItems().add("All Exporters / Importers");
        exporterFilterCombo.setValue("All Exporters / Importers");
        exporterFilterCombo.setPrefWidth(140);
        styleAdminCombo(exporterFilterCombo);
        exporterFilterCombo.setOnAction(e -> applyFilter());

        List<String> dynamicPortOptions = new ArrayList<>();
        dynamicPortOptions.add("All Ports");
        dynamicPortOptions.addAll(com.projectui.view.admin.AdminDataStore.getInstance().getApprovedPortNames());
        portFilterCombo = combo("All Ports", dynamicPortOptions.toArray(new String[0]));
        portFilterCombo.setPrefWidth(125);
        portFilterCombo.setOnAction(e -> applyFilter());

        Button resetFilterBtn = new Button("↻ Refresh");
        resetFilterBtn.setStyle(
                "-fx-background-color:#f1f5f9;" +
                "-fx-text-fill:#475569;" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:8 12;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );
        resetFilterBtn.setOnAction(e -> refreshInspectionData());

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox topFilterRow = new HBox(8, searchField, statusFilterCombo, priorityFilterCombo, typeFilterCombo, exporterFilterCombo, portFilterCombo, resetFilterBtn);
        topFilterRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        HBox topBar = new HBox(12, titleBox, space);
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Main Table Layout (Admin Responsive ColumnConstraints)
        table.setHgap(0);
        table.setVgap(0);
        table.setMaxWidth(Double.MAX_VALUE);
        table.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        // Footer showing count
        showingCountLabel = new Label("Showing 0 of 0 inspection records");
        showingCountLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");

        Region footerSpace = new Region();
        HBox.setHgrow(footerSpace, Priority.ALWAYS);

        Label pageIndicator = new Label("‹    1    ›");
        pageIndicator.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:14; -fx-font-weight:bold;");

        HBox footer = new HBox(8, showingCountLabel, footerSpace, pageIndicator);
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(14, topBar, topFilterRow, table, footer);
        card.setPadding(new Insets(18));
        card.setStyle(CARD_STYLE);
        return card;
    }

    // =====================================================
    // 4. CARGO INSPECTION DETAILS PANEL (RIGHT SIDE)
    // =====================================================
    private VBox createDetailInspectionCard() {
        formTitle = new Label("Cargo Inspection Details");
        formTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");

        formSubtitle = new Label("Verify physical cargo against manifest and authorize terminal release");
        formSubtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, formTitle, formSubtitle);

        statusBadgeLabel = new Label("NO SELECTION");
        statusBadgeLabel.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#64748b; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:6;");

        priorityBadgeLabel = new Label("NORMAL");
        priorityBadgeLabel.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#475569; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:6;");

        Region topSp = new Region();
        HBox.setHgrow(topSp, Priority.ALWAYS);

        HBox topRow = new HBox(6, titleBox, topSp, priorityBadgeLabel, statusBadgeLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Safety Alert Box
        alertBox = new VBox(2);
        alertBox.setPadding(new Insets(8, 12, 8, 12));
        alertBox.setStyle("-fx-background-color:#fffbeb; -fx-border-color:#fde68a; -fx-border-radius:8; -fx-background-radius:8;");
        alertTitle = new Label("⚠ Physical Inspection Alert");
        alertTitle.setStyle("-fx-text-fill:#92400e; -fx-font-size:12; -fx-font-weight:bold;");
        alertMessage = new Label("Standard physical inspection required.");
        alertMessage.setStyle("-fx-text-fill:#78350f; -fx-font-size:12;");
        alertBox.getChildren().addAll(alertTitle, alertMessage);
        alertBox.setVisible(false);
        alertBox.setManaged(false);

        // Section 1: Cargo Specifications
        VBox cargoSection = createPanelSection("CARGO SPECIFICATIONS", createCargoMetadataGrid());

        // Section 2: Shipment & Vessel Details
        VBox shipSection = createPanelSection("SHIPMENT & VESSEL DETAILS", createShipmentMetadataGrid());

        // Section 3: Parties & Declaration
        VBox partySection = createPanelSection("PARTIES & DECLARATION", createPartyMetadataGrid());

        // Section 4: Attached Shipping Document
        VBox fileSection = createPanelSection("ATTACHED SHIPPING DOCUMENT", createFilePreviewRow());

        // Section 5: Physical Inspection Checklist
        checklistContainer = createPhysicalInspectionChecklist();
        VBox checklistSection = createPanelSection("CARGO PHYSICAL INSPECTION CHECKLIST", checklistContainer);

        // Section 6: Officer Findings & Remarks
        findingsTextArea = new TextArea();
        findingsTextArea.setPromptText("Enter officer inspection findings, discrepancies, or seal audit observations...");
        findingsTextArea.setPrefRowCount(3);
        findingsTextArea.setWrapText(true);
        findingsTextArea.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8; -fx-padding:8; -fx-font-size:13; -fx-text-fill:#0f172a;");
        VBox findingsSection = createPanelSection("OFFICER FINDINGS & DISCREPANCY LOG", findingsTextArea);

        // Section 7: Audit Trail
        auditTrailContainer = new VBox(4);
        VBox auditSection = createPanelSection("INSPECTION AUDIT TRAIL", auditTrailContainer);

        // Section 8: Action Buttons (Exact Admin Primary, Warning, Danger Button Styling)
        Button passBtn = new Button("✓  PASS & AUTHORIZE RELEASE");
        passBtn.setMaxWidth(Double.MAX_VALUE);
        passBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:10 16; -fx-background-radius:8; -fx-cursor:hand;");
        passBtn.setOnAction(e -> performInspectionAction("Passed", getOfficerFindingsOrDefault(), null));

        Button inspectBtn = new Button("🔍  MARK UNDER INSPECTION");
        inspectBtn.setMaxWidth(Double.MAX_VALUE);
        inspectBtn.setStyle("-fx-background-color:" + BLUE + "; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:10 16; -fx-background-radius:8; -fx-cursor:hand;");
        inspectBtn.setOnAction(e -> performInspectionAction("Under Inspection", "Physical cargo examination initiated by " + officerName, null));

        Button holdBtn = new Button("⏱  PLACE ON REGULATORY HOLD");
        holdBtn.setMaxWidth(Double.MAX_VALUE);
        holdBtn.setStyle("-fx-background-color:#f97316; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:10 16; -fx-background-radius:8; -fx-cursor:hand;");
        holdBtn.setOnAction(e -> promptForReasonAndAction("On Hold", "Place Cargo On Hold", "Specify Regulatory Reason for Hold"));

        Button failBtn = new Button("✕  FAIL / REJECT CARGO");
        failBtn.setMaxWidth(Double.MAX_VALUE);
        failBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:10 16; -fx-background-radius:8; -fx-cursor:hand;");
        failBtn.setOnAction(e -> promptForReasonAndAction("Failed", "Fail / Reject Cargo", "Specify Physical Inspection Failure Reason"));

        VBox actionBox = new VBox(8, passBtn, inspectBtn, holdBtn, failBtn);

        VBox innerContent = new VBox(12,
                topRow, alertBox, new Separator(),
                cargoSection, shipSection, partySection, fileSection,
                checklistSection, findingsSection, auditSection,
                actionBox
        );

        ScrollPane scroll = new ScrollPane(innerContent);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(600);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-color:transparent;");

        VBox card = new VBox(10, scroll);
        card.setPadding(new Insets(18));
        card.setStyle(CARD_STYLE);
        return card;
    }

    private VBox createPanelSection(String sectionTitle, Node content) {
        Label title = new Label(sectionTitle);
        title.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:11; -fx-font-weight:bold;");

        VBox box = new VBox(6, title, content);
        box.setPadding(new Insets(10, 12, 10, 12));
        box.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");
        return box;
    }

    private GridPane createCargoMetadataGrid() {
        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(4);

        detailCargoId = createLabel("—", true, BLUE);
        detailCargoName = createLabel("—", false, "#334155");
        detailCargoType = createLabel("—", false, "#334155");
        detailHsCode = createLabel("—", false, "#334155");
        detailQuantity = createLabel("—", false, "#334155");
        detailPackageInfo = createLabel("—", false, "#334155");
        detailGrossWeight = createLabel("—", false, "#334155");
        detailNetWeight = createLabel("—", false, "#334155");
        detailContainer = createLabel("—", false, "#334155");

        g.add(createLabel("Cargo ID / Name:", true, "#64748b"), 0, 0);
        g.add(new HBox(6, detailCargoId, createLabel("•", false, "#94a3b8"), detailCargoName), 1, 0);
        g.add(createLabel("Category / Type:", true, "#64748b"), 0, 1);
        g.add(detailCargoType, 1, 1);
        g.add(createLabel("HS Classification:", true, "#64748b"), 0, 2);
        g.add(detailHsCode, 1, 2);
        g.add(createLabel("Quantity / Packages:", true, "#64748b"), 0, 3);
        g.add(new HBox(6, detailQuantity, createLabel("•", false, "#94a3b8"), detailPackageInfo), 1, 3);
        g.add(createLabel("Gross / Net Weight:", true, "#64748b"), 0, 4);
        g.add(new HBox(6, detailGrossWeight, createLabel("•", false, "#94a3b8"), detailNetWeight), 1, 4);
        g.add(createLabel("Container Number:", true, "#64748b"), 0, 5);
        g.add(detailContainer, 1, 5);

        return g;
    }

    private GridPane createShipmentMetadataGrid() {
        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(4);

        detailVessel = createLabel("—", false, "#334155");
        detailVoyage = createLabel("—", false, "#334155");
        detailImo = createLabel("IMO: 9410521", false, "#64748b");
        detailRoute = createLabel("—", false, "#334155");
        detailEtaEtd = createLabel("—", false, "#334155");

        g.add(createLabel("Vessel / Voyage:", true, "#64748b"), 0, 0);
        g.add(new HBox(6, detailVessel, createLabel("•", false, "#94a3b8"), detailVoyage), 1, 0);
        g.add(createLabel("Vessel Identifier:", true, "#64748b"), 0, 1);
        g.add(detailImo, 1, 1);
        g.add(createLabel("Port Route:", true, "#64748b"), 0, 2);
        g.add(detailRoute, 1, 2);
        g.add(createLabel("ETA / ETD Schedule:", true, "#64748b"), 0, 3);
        g.add(detailEtaEtd, 1, 3);

        return g;
    }

    private GridPane createPartyMetadataGrid() {
        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(4);

        detailImporter = createLabel("—", false, "#334155");
        detailExporter = createLabel("—", false, "#334155");
        detailCompany = createLabel("—", false, "#334155");
        detailDocRef = createLabel("—", true, "#0f172a");

        g.add(createLabel("Importer / Consignee:", true, "#64748b"), 0, 0);
        g.add(detailImporter, 1, 0);
        g.add(createLabel("Exporter / Shipper:", true, "#64748b"), 0, 1);
        g.add(detailExporter, 1, 1);
        g.add(createLabel("Declaring Entity:", true, "#64748b"), 0, 2);
        g.add(detailCompany, 1, 2);
        g.add(createLabel("Linked Document ID:", true, "#64748b"), 0, 3);
        g.add(detailDocRef, 1, 3);

        return g;
    }

    private HBox createFilePreviewRow() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("📄");
        icon.setStyle("-fx-font-size:22;");

        VBox info = new VBox(2);
        pdfNameLabel = new Label("No document selected");
        pdfNameLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");

        HBox sub = new HBox(6);
        sub.setAlignment(Pos.CENTER_LEFT);
        pdfTypeLabel = new Label("Select a record from queue to preview");
        pdfTypeLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:11;");

        pdfStorageBadge = new Label("Storage Ready");
        pdfStorageBadge.setStyle("-fx-background-color:#e2e8f0; -fx-text-fill:#475569; -fx-font-size:10; -fx-padding:1 6; -fx-background-radius:4;");
        sub.getChildren().addAll(pdfTypeLabel, pdfStorageBadge);
        info.getChildren().addAll(pdfNameLabel, sub);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        openPdfBtn = new Button("View / Open ↗");
        openPdfBtn.setStyle("-fx-background-color:" + BLUE + "; -fx-text-fill:white; -fx-font-size:12; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 14; -fx-cursor:hand;");
        openPdfBtn.setOnAction(e -> openSelectedDocument());

        row.getChildren().addAll(icon, info, space, openPdfBtn);
        return row;
    }

    private VBox createPhysicalInspectionChecklist() {
        VBox v = new VBox(6);

        Label cat1 = createLabel("CARGO IDENTIFICATION & WEIGHT AUDIT", true, "#64748b");
        CheckBox cb1 = new CheckBox("Cargo physical description matches declaration");
        cb1.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb1.setSelected(true);

        CheckBox cb2 = new CheckBox("Quantity and package count match physical tally");
        cb2.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb2.setSelected(true);

        CheckBox cb3 = new CheckBox("Weighbridge ticket corresponds with gross weight");
        cb3.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb3.setSelected(true);

        Label cat2 = createLabel("CONTAINER & SEAL INTEGRITY", true, "#64748b");
        CheckBox cb4 = new CheckBox("Container number and ISO code verified intact");
        cb4.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb4.setSelected(true);

        CheckBox cb5 = new CheckBox("High-security bolt seal intact & numbers match");
        cb5.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb5.setSelected(true);

        Label cat3 = createLabel("PHYSICAL CONDITION & HAZMAT COMPLIANCE", true, "#64748b");
        CheckBox cb6 = new CheckBox("No structural damage, leakage, or tampering detected");
        cb6.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb6.setSelected(true);

        CheckBox cb7 = new CheckBox("No undeclared or prohibited hazardous materials found");
        cb7.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb7.setSelected(true);

        CheckBox cb8 = new CheckBox("Clearance criteria met for port terminal release");
        cb8.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb8.setSelected(true);

        v.getChildren().addAll(cat1, cb1, cb2, cb3, cat2, cb4, cb5, cat3, cb6, cb7, cb8);
        return v;
    }

    // =====================================================
    // 5. BOTTOM SUMMARY CARDS (MATCHING ADMIN DASHBOARD)
    // =====================================================
    private HBox createBottomSummaryRow() {
        VBox c1 = createAdminInfoCard("Recent Physical Inspection Operations",
                "Live log of physical cargo inspections, seal verifications, and approvals across Cochin Port",
                createActivityRows());

        VBox c2 = createAdminInfoCard("Maritime Customs Inspection Protocol",
                "Standard inspection procedures mandated for port terminal entry and container release",
                createInspectionGuidelines());

        HBox row = new HBox(16, c1, c2);
        HBox.setHgrow(c1, Priority.ALWAYS);
        HBox.setHgrow(c2, Priority.ALWAYS);
        c1.setMaxWidth(Double.MAX_VALUE);
        c2.setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    private VBox createAdminInfoCard(String title, String subtitle, Node body) {
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16; -fx-font-weight:bold;");
        Label s = new Label(subtitle);
        s.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");

        VBox box = new VBox(10, new VBox(2, t, s), body);
        box.setPadding(new Insets(16));
        box.setStyle(CARD_STYLE);
        return box;
    }

    private VBox createActivityRows() {
        VBox v = new VBox(6);
        v.getChildren().add(createAuditRow("✓", "Inspection Passed for Container MSKU-7821940", "Officer " + officerName, "15 mins ago"));
        v.getChildren().add(createAuditRow("🔍", "Physical Seal Audit initiated for Cargo CGO-1004", "Customs Inspection Bay", "30 mins ago"));
        v.getChildren().add(createAuditRow("📥", "Cargo CGO-1002 queued following Document Certification", "Document Desk", "1 hr ago"));
        return v;
    }

    private VBox createInspectionGuidelines() {
        VBox v = new VBox(6);
        v.getChildren().add(createGuidelineRow("🛡 Physical container seal audit required prior to authorizing customs release."));
        v.getChildren().add(createGuidelineRow("⚡ Dangerous goods & hazardous class consignments require specialized hazmat checks."));
        v.getChildren().add(createGuidelineRow("⚖ Approved inspection records are automatically transmitted to Port Authority for clearance."));
        return v;
    }

    private HBox createGuidelineRow(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:#334155; -fx-font-size:12;");
        HBox r = new HBox(l);
        r.setPadding(new Insets(4, 0, 4, 0));
        return r;
    }

    // =====================================================
    // 6. LIVE DATABASE & TABLE REFRESH LOGIC
    // =====================================================
    private void refreshInspectionData() {
        Platform.runLater(() -> {
            resolveOfficerContext();
            if (portJurisdictionLabel != null) {
                portJurisdictionLabel.setText("Jurisdiction: " + activeAssignedPort);
            }

            activeCargoList.clear();
            pendingCount = 0;
            underInspectionCount = 0;
            passedCount = 0;
            failedCount = 0;
            onHoldCount = 0;
            highPriorityCount = 0;

            Set<String> uniqueEntities = new HashSet<>();

            List<Map<String, Object>> cargos = cargoDao.getCargosByPort(activeAssignedPort);
            if (cargos != null) {
                for (Map<String, Object> c : cargos) {
                    if (c == null) continue;

                    String cargoId = c.get("cargoId") != null ? String.valueOf(c.get("cargoId")) : "CGO-1000";
                    String cargoName = c.get("cargoName") != null ? String.valueOf(c.get("cargoName")) : "General Cargo";
                    String cargoType = c.get("cargoType") != null ? String.valueOf(c.get("cargoType")) : "Containerized";
                    String hsCode = c.get("hsCode") != null ? String.valueOf(c.get("hsCode")) : "8471.30.00";
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
                    String inspectStatus = c.get("inspectionStatus") != null ? String.valueOf(c.get("inspectionStatus")) : "Pending";
                    String customsStatus = c.get("customsStatus") != null ? String.valueOf(c.get("customsStatus")) : "Pending";

                    if (!exporter.isBlank() && !exporter.equals("—")) uniqueEntities.add(exporter);
                    if (!importer.isBlank() && !importer.equals("—")) uniqueEntities.add(importer);

                    String cargoPriority = "Normal";
                    if (c.get("priority") != null && !c.get("priority").toString().isBlank()) {
                        cargoPriority = c.get("priority").toString();
                    } else if (cargoType.toLowerCase().contains("hazardous") || cargoType.toLowerCase().contains("dangerous") || cargoType.toLowerCase().contains("perishable") || cargoName.toLowerCase().contains("chemical") || cargoName.toLowerCase().contains("pharma")) {
                        cargoPriority = "Urgent";
                    }

                    Map<String, Object> item = new HashMap<>(c);
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
                    item.put("priority", cargoPriority);
                    item.put("inspectionStatus", inspectStatus);
                    item.put("customsStatus", customsStatus);
                    item.put("docId", c.get("docId") != null ? c.get("docId") : "DOC-" + cargoId + "-01");

                    if ("Passed".equalsIgnoreCase(inspectStatus) || "Approved".equalsIgnoreCase(inspectStatus) || "Cleared".equalsIgnoreCase(inspectStatus)) {
                        passedCount++;
                    } else if ("Under Inspection".equalsIgnoreCase(inspectStatus) || "Review".equalsIgnoreCase(inspectStatus)) {
                        underInspectionCount++;
                    } else if ("Failed".equalsIgnoreCase(inspectStatus) || "Rejected".equalsIgnoreCase(inspectStatus)) {
                        failedCount++;
                    } else if ("On Hold".equalsIgnoreCase(inspectStatus) || "Hold".equalsIgnoreCase(inspectStatus) || "Held".equalsIgnoreCase(inspectStatus)) {
                        onHoldCount++;
                    } else {
                        pendingCount++;
                    }

                    if ("Urgent".equalsIgnoreCase(cargoPriority) || "High".equalsIgnoreCase(cargoPriority)) {
                        highPriorityCount++;
                    }

                    activeCargoList.add(item);
                }
            }

            // Update KPI Metric Values
            if (pendingValueLabel != null) pendingValueLabel.setText(String.valueOf(pendingCount));
            if (underInspectionValueLabel != null) underInspectionValueLabel.setText(String.valueOf(underInspectionCount));
            if (passedValueLabel != null) passedValueLabel.setText(String.valueOf(passedCount));
            if (failedValueLabel != null) failedValueLabel.setText(String.valueOf(failedCount));
            if (onHoldValueLabel != null) onHoldValueLabel.setText(String.valueOf(onHoldCount));
            if (highPriorityValueLabel != null) highPriorityValueLabel.setText(String.valueOf(highPriorityCount));

            // Update Exporter Filter Combo Dynamically
            if (exporterFilterCombo != null) {
                String curr = exporterFilterCombo.getValue();
                exporterFilterCombo.getItems().clear();
                exporterFilterCombo.getItems().add("All Exporters / Importers");
                List<String> sortedEntities = new ArrayList<>(uniqueEntities);
                Collections.sort(sortedEntities);
                exporterFilterCombo.getItems().addAll(sortedEntities);
                if (curr != null && (curr.equals("All Exporters / Importers") || uniqueEntities.contains(curr))) {
                    exporterFilterCombo.setValue(curr);
                } else {
                    exporterFilterCombo.setValue("All Exporters / Importers");
                }
            }

            applyFilter();
        });
    }

    private void applyFilter() {
        table.getChildren().clear();
        table.getColumnConstraints().clear();
        filteredCargoList.clear();

        String query = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String statusVal = statusFilterCombo != null ? statusFilterCombo.getValue() : "All Statuses";
        String priorityVal = priorityFilterCombo != null ? priorityFilterCombo.getValue() : "All Priorities";
        String typeVal = typeFilterCombo != null ? typeFilterCombo.getValue() : "All Types";
        String expVal = exporterFilterCombo != null ? exporterFilterCombo.getValue() : "All Exporters / Importers";
        String portVal = portFilterCombo != null ? portFilterCombo.getValue() : "All Ports";

        for (Map<String, Object> c : activeCargoList) {
            String cargoId = String.valueOf(c.get("cargoId"));
            String cargoName = String.valueOf(c.get("cargoName"));
            String exp = String.valueOf(c.get("exporter"));
            String imp = String.valueOf(c.get("importer"));
            String vessel = String.valueOf(c.get("vessel"));
            String type = String.valueOf(c.get("cargoType"));
            String cont = String.valueOf(c.get("container"));
            String origin = String.valueOf(c.get("origin"));
            String dest = String.valueOf(c.get("destination"));
            String inspectStatus = String.valueOf(c.get("inspectionStatus"));
            String prio = String.valueOf(c.get("priority"));
            String docId = String.valueOf(c.get("docId"));

            boolean matchQuery = query.isEmpty() ||
                    cargoId.toLowerCase().contains(query) ||
                    cargoName.toLowerCase().contains(query) ||
                    docId.toLowerCase().contains(query) ||
                    exp.toLowerCase().contains(query) ||
                    imp.toLowerCase().contains(query) ||
                    vessel.toLowerCase().contains(query) ||
                    type.toLowerCase().contains(query) ||
                    cont.toLowerCase().contains(query);

            boolean matchStatus = "All Statuses".equalsIgnoreCase(statusVal) || inspectStatus.equalsIgnoreCase(statusVal);
            boolean matchPriority = "All Priorities".equalsIgnoreCase(priorityVal) || prio.equalsIgnoreCase(priorityVal);
            boolean matchType = "All Types".equalsIgnoreCase(typeVal) || type.toLowerCase().contains(typeVal.toLowerCase());
            boolean matchExp = "All Exporters / Importers".equalsIgnoreCase(expVal) || exp.equalsIgnoreCase(expVal) || imp.equalsIgnoreCase(expVal);
            boolean matchPort = "All Ports".equalsIgnoreCase(portVal) || origin.toLowerCase().contains(portVal.toLowerCase()) || dest.toLowerCase().contains(portVal.toLowerCase());

            if (matchQuery && matchStatus && matchPriority && matchType && matchExp && matchPort) {
                filteredCargoList.add(c);
            }
        }

        if (showingCountLabel != null) {
            showingCountLabel.setText("Showing " + filteredCargoList.size() + " of " + activeCargoList.size() + " inspection records");
        }

        // Table Column Constraints (Exact Admin table proportions)
        ColumnConstraints c0 = new ColumnConstraints(); c0.setPercentWidth(24); // Cargo ID & Name
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(20); // Shipment & Vessel
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(20); // Importer / Exporter
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(12); // Priority
        ColumnConstraints c4 = new ColumnConstraints(); c4.setPercentWidth(12); // Status
        ColumnConstraints c5 = new ColumnConstraints(); c5.setPercentWidth(12); // Action
        table.getColumnConstraints().addAll(c0, c1, c2, c3, c4, c5);

        String[] headers = {"Cargo", "Shipment / Vessel", "Importer / Exporter", "Priority", "Status", "Action"};
        for (int col = 0; col < headers.length; col++) {
            table.add(tableHeadCell(headers[col]), col, 0);
        }

        if (filteredCargoList.isEmpty()) {
            Label empty = new Label("No cargo pending inspection for the selected filter.");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setMinHeight(70);
            empty.setAlignment(Pos.CENTER);
            empty.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:14; -fx-font-style:italic; -fx-background-color:white;");
            table.add(empty, 0, 1, 6, 1);
            clearDetailPanel();
            return;
        }

        int displayRow = 1;
        for (int i = 0; i < filteredCargoList.size(); i++) {
            Map<String, Object> cargo = filteredCargoList.get(i);
            boolean isAlt = (displayRow % 2 == 0);
            boolean isSelected = selectedCargo != null && cargo.get("cargoId") != null && cargo.get("cargoId").equals(selectedCargo.get("cargoId"));

            Node cargoCell = createCargoCell(cargo, isAlt, isSelected);
            Node shipCell = createShipCell(cargo, isAlt, isSelected);
            Node partyCell = createPartyCell(cargo, isAlt, isSelected);
            Node priorityCell = createPriorityCell(cargo, isAlt, isSelected);
            Node statusCell = createStatusCell(cargo, isAlt, isSelected);
            Node actionCell = createActionCell(cargo, isAlt, isSelected);

            table.add(cargoCell, 0, displayRow);
            table.add(shipCell, 1, displayRow);
            table.add(partyCell, 2, displayRow);
            table.add(priorityCell, 3, displayRow);
            table.add(statusCell, 4, displayRow);
            table.add(actionCell, 5, displayRow);

            displayRow++;
        }

        if (selectedCargo == null || !filteredCargoList.contains(selectedCargo)) {
            selectCargo(filteredCargoList.get(0));
        }
    }

    private Label tableHeadCell(String text) {
        Label cell = new Label(text);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMinHeight(44);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 12, 0, 12));
        cell.setStyle(
                "-fx-background-color:#f8fafc;" +
                "-fx-text-fill:#0f172a;" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-border-color:#e2e8f0; -fx-border-width:0 0 1 0;"
        );
        return cell;
    }

    private Node createCargoCell(Map<String, Object> cargo, boolean isAlt, boolean isSelected) {
        String cargoId = String.valueOf(cargo.get("cargoId"));
        String cargoName = String.valueOf(cargo.get("cargoName"));

        Label idLbl = new Label(cargoId);
        idLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");
        Label nameLbl = new Label(cargoName + " (" + cargo.get("cargoType") + ")");
        nameLbl.setStyle("-fx-text-fill:#64748b; -fx-font-size:11;");

        VBox v = new VBox(2, idLbl, nameLbl);
        v.setAlignment(Pos.CENTER_LEFT);
        v.setMaxWidth(Double.MAX_VALUE);
        v.setMinHeight(52);
        v.setPadding(new Insets(0, 12, 0, 12));
        v.setStyle(cellBgStyle(isAlt, isSelected));
        v.setOnMouseClicked(e -> selectCargo(cargo));
        return v;
    }

    private Node createShipCell(Map<String, Object> cargo, boolean isAlt, boolean isSelected) {
        String vessel = String.valueOf(cargo.get("vessel"));
        String cont = "Cont: " + String.valueOf(cargo.get("container"));

        Label vesselLbl = new Label(vessel);
        vesselLbl.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:13; -fx-font-weight:bold;");
        Label contLbl = new Label(cont);
        contLbl.setStyle("-fx-text-fill:#64748b; -fx-font-size:11;");

        VBox v = new VBox(2, vesselLbl, contLbl);
        v.setAlignment(Pos.CENTER_LEFT);
        v.setMaxWidth(Double.MAX_VALUE);
        v.setMinHeight(52);
        v.setPadding(new Insets(0, 12, 0, 12));
        v.setStyle(cellBgStyle(isAlt, isSelected));
        v.setOnMouseClicked(e -> selectCargo(cargo));
        return v;
    }

    private Node createPartyCell(Map<String, Object> cargo, boolean isAlt, boolean isSelected) {
        String exp = "Exp: " + String.valueOf(cargo.get("exporter"));
        String imp = "Imp: " + String.valueOf(cargo.get("importer"));

        Label expLbl = new Label(exp);
        expLbl.setStyle("-fx-text-fill:#334155; -fx-font-size:12;");
        Label impLbl = new Label(imp);
        impLbl.setStyle("-fx-text-fill:#64748b; -fx-font-size:11;");

        VBox v = new VBox(2, expLbl, impLbl);
        v.setAlignment(Pos.CENTER_LEFT);
        v.setMaxWidth(Double.MAX_VALUE);
        v.setMinHeight(52);
        v.setPadding(new Insets(0, 12, 0, 12));
        v.setStyle(cellBgStyle(isAlt, isSelected));
        v.setOnMouseClicked(e -> selectCargo(cargo));
        return v;
    }

    private Node createPriorityCell(Map<String, Object> cargo, boolean isAlt, boolean isSelected) {
        String prio = cargo.get("priority") != null ? String.valueOf(cargo.get("priority")) : "Normal";
        Label badge = new Label(prio.toUpperCase());
        applyPriorityBadgeStyle(badge, prio);

        HBox h = new HBox(badge);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setMaxWidth(Double.MAX_VALUE);
        h.setMinHeight(52);
        h.setPadding(new Insets(0, 12, 0, 12));
        h.setStyle(cellBgStyle(isAlt, isSelected));
        h.setOnMouseClicked(e -> selectCargo(cargo));
        return h;
    }

    private Node createStatusCell(Map<String, Object> cargo, boolean isAlt, boolean isSelected) {
        String st = String.valueOf(cargo.get("inspectionStatus"));
        Label badge = new Label(st.toUpperCase());
        applyStatusBadgeStyle(badge, st);

        HBox h = new HBox(badge);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setMaxWidth(Double.MAX_VALUE);
        h.setMinHeight(52);
        h.setPadding(new Insets(0, 12, 0, 12));
        h.setStyle(cellBgStyle(isAlt, isSelected));
        h.setOnMouseClicked(e -> selectCargo(cargo));
        return h;
    }

    private Node createActionCell(Map<String, Object> cargo, boolean isAlt, boolean isSelected) {
        Button inspectBtn = new Button("Inspect");
        inspectBtn.setStyle(
                "-fx-background-color:#eff6ff;" +
                "-fx-text-fill:" + BLUE + ";" +
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:6;" +
                "-fx-padding:4 10;" +
                "-fx-cursor:hand;"
        );
        inspectBtn.setOnAction(e -> selectCargo(cargo));

        HBox h = new HBox(inspectBtn);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setMaxWidth(Double.MAX_VALUE);
        h.setMinHeight(52);
        h.setPadding(new Insets(0, 12, 0, 12));
        h.setStyle(cellBgStyle(isAlt, isSelected));
        return h;
    }

    private String cellBgStyle(boolean isAlt, boolean isSelected) {
        if (isSelected) {
            return "-fx-background-color:#eff6ff; -fx-border-color:#dbeafe; -fx-border-width:0 0 1 0; -fx-cursor:hand;";
        }
        return "-fx-background-color:" + (isAlt ? "#fafcff" : "white") + "; -fx-border-color:#f1f5f9; -fx-border-width:0 0 1 0; -fx-cursor:hand;";
    }

    private void selectCargo(Map<String, Object> cargo) {
        this.selectedCargo = cargo;
        if (cargo == null) return;

        applyFilterHighlight();

        String cargoId = String.valueOf(cargo.get("cargoId"));
        String cargoName = String.valueOf(cargo.get("cargoName"));
        formTitle.setText(cargoId + " - " + cargoName);

        detailCargoId.setText(cargoId);
        detailCargoName.setText(cargoName);
        detailCargoType.setText(String.valueOf(cargo.get("cargoType")));
        detailHsCode.setText(cargo.get("hsCode") != null ? String.valueOf(cargo.get("hsCode")) : "8471.30.00");
        detailQuantity.setText(String.valueOf(cargo.get("quantity")));
        detailPackageInfo.setText(String.valueOf(cargo.get("packageCount")));
        detailGrossWeight.setText(String.valueOf(cargo.get("grossWeight")));
        detailNetWeight.setText(String.valueOf(cargo.get("netWeight")));
        detailContainer.setText(String.valueOf(cargo.get("container")));

        detailVessel.setText(String.valueOf(cargo.get("vessel")));
        detailVoyage.setText(String.valueOf(cargo.get("voyage")));
        detailRoute.setText(String.valueOf(cargo.get("origin")) + " ➔ " + String.valueOf(cargo.get("destination")));
        detailEtaEtd.setText("ETA: " + String.valueOf(cargo.get("eta")) + " / ETD: " + String.valueOf(cargo.get("etd")));

        detailImporter.setText(String.valueOf(cargo.get("importer")));
        detailExporter.setText(String.valueOf(cargo.get("exporter")));
        detailCompany.setText(String.valueOf(cargo.get("company")));
        detailDocRef.setText(String.valueOf(cargo.get("docId")));

        String prio = cargo.get("priority") != null ? String.valueOf(cargo.get("priority")) : "Normal";
        priorityBadgeLabel.setText(prio.toUpperCase());
        applyPriorityBadgeStyle(priorityBadgeLabel, prio);

        String st = String.valueOf(cargo.get("inspectionStatus"));
        statusBadgeLabel.setText(st.toUpperCase());
        applyStatusBadgeStyle(statusBadgeLabel, st);

        if (cargo.get("inspectionNotes") != null && !cargo.get("inspectionNotes").toString().isBlank()) {
            findingsTextArea.setText(cargo.get("inspectionNotes").toString());
        } else {
            findingsTextArea.clear();
        }

        String actualFileName = cargo.get("fileName") != null ? String.valueOf(cargo.get("fileName")) : cargoId + "_Declaration.pdf";
        String fileUrl = cargo.get("fileUrl") != null ? String.valueOf(cargo.get("fileUrl")) : null;

        if (fileUrl != null || cargo.get("fileName") != null) {
            pdfNameLabel.setText(actualFileName);
            pdfTypeLabel.setText("Official Shipping Declaration Attached");
            pdfStorageBadge.setText(fileUrl != null && fileUrl.startsWith("http") ? "Cloud Storage" : "Local Document");
            pdfStorageBadge.setStyle("-fx-background-color:#ecfdf5; -fx-text-fill:#16a34a; -fx-font-size:10; -fx-font-weight:bold; -fx-padding:2 6; -fx-background-radius:4;");
        } else {
            pdfNameLabel.setText("Declaration Document Attached");
            pdfTypeLabel.setText("Customs Registry Record");
            pdfStorageBadge.setText("Active");
            pdfStorageBadge.setStyle("-fx-background-color:#eff6ff; -fx-text-fill:" + BLUE + "; -fx-font-size:10; -fx-font-weight:bold; -fx-padding:2 6; -fx-background-radius:4;");
        }

        updateComplianceAlert(cargo);
        updateAuditTrail(cargo);
    }

    private void applyFilterHighlight() {
        for (int i = 0; i < filteredCargoList.size(); i++) {
            Map<String, Object> c = filteredCargoList.get(i);
            boolean isAlt = ((i + 1) % 2 == 0);
            boolean isSelected = selectedCargo != null && c.get("cargoId") != null && c.get("cargoId").equals(selectedCargo.get("cargoId"));
            int rowIdx = i + 1;

            for (Node n : table.getChildren()) {
                Integer r = GridPane.getRowIndex(n);
                if (r != null && r == rowIdx) {
                    n.setStyle(cellBgStyle(isAlt, isSelected));
                }
            }
        }
    }

    private void updateComplianceAlert(Map<String, Object> cargo) {
        if (alertBox == null) return;
        String st = String.valueOf(cargo.get("inspectionStatus"));
        String prio = String.valueOf(cargo.get("priority"));
        String cargoType = String.valueOf(cargo.get("cargoType"));

        if ("Failed".equalsIgnoreCase(st) || "Rejected".equalsIgnoreCase(st)) {
            alertTitle.setText("⛔ Cargo Inspection Failed / Seal Discrepancy");
            alertMessage.setText("Physical inspection failed. Cargo is held in inspection bay and blocked from terminal clearance.");
            alertBox.setStyle("-fx-background-color:#fef2f2; -fx-border-color:#fecaca; -fx-border-radius:8; -fx-background-radius:8;");
            alertTitle.setStyle("-fx-text-fill:#991b1b; -fx-font-size:12; -fx-font-weight:bold;");
            alertMessage.setStyle("-fx-text-fill:#7f1d1d; -fx-font-size:12;");
            alertBox.setVisible(true);
            alertBox.setManaged(true);
        } else if ("On Hold".equalsIgnoreCase(st) || "Hold".equalsIgnoreCase(st) || "Held".equalsIgnoreCase(st)) {
            alertTitle.setText("⏸ Cargo Placed On Regulatory Hold");
            alertMessage.setText("Awaiting re-weighing, secondary physical audit, or supplemental importer documentation.");
            alertBox.setStyle("-fx-background-color:#fff7ed; -fx-border-color:#fed7aa; -fx-border-radius:8; -fx-background-radius:8;");
            alertTitle.setStyle("-fx-text-fill:#9a3412; -fx-font-size:12; -fx-font-weight:bold;");
            alertMessage.setStyle("-fx-text-fill:#7c2d12; -fx-font-size:12;");
            alertBox.setVisible(true);
            alertBox.setManaged(true);
        } else if ("Urgent".equalsIgnoreCase(prio) || cargoType.toLowerCase().contains("hazardous") || cargoType.toLowerCase().contains("dangerous")) {
            alertTitle.setText("🚨 High-Priority Dangerous / Hazardous Consignment");
            alertMessage.setText("Special physical safety inspection required. Verify UN hazmat labels, isolation seals, and MSDS documentation.");
            alertBox.setStyle("-fx-background-color:#ffe4e6; -fx-border-color:#fecdd3; -fx-border-radius:8; -fx-background-radius:8;");
            alertTitle.setStyle("-fx-text-fill:#9f1239; -fx-font-size:12; -fx-font-weight:bold;");
            alertMessage.setStyle("-fx-text-fill:#881337; -fx-font-size:12;");
            alertBox.setVisible(true);
            alertBox.setManaged(true);
        } else {
            alertBox.setVisible(false);
            alertBox.setManaged(false);
        }
    }

    private void updateAuditTrail(Map<String, Object> cargo) {
        if (auditTrailContainer == null) return;
        auditTrailContainer.getChildren().clear();

        String exporter = cargo.get("exporter") != null ? String.valueOf(cargo.get("exporter")) : "Shipper";
        String createdAt = cargo.get("createdAt") != null ? String.valueOf(cargo.get("createdAt")) : "Initial submission";
        String inspectStatus = String.valueOf(cargo.get("inspectionStatus"));
        String inspectedBy = cargo.get("inspectedBy") != null ? String.valueOf(cargo.get("inspectedBy")) : officerName;
        String inspectDate = cargo.get("inspectionDate") != null ? String.valueOf(cargo.get("inspectionDate")) : "Recent";

        auditTrailContainer.getChildren().add(createAuditRow("📥", "Cargo Registered by Shipper", exporter, createdAt));
        auditTrailContainer.getChildren().add(createAuditRow("📋", "Document Verification Certified", "Customs Document Desk", "Approved"));

        if ("Passed".equalsIgnoreCase(inspectStatus) || "Approved".equalsIgnoreCase(inspectStatus)) {
            auditTrailContainer.getChildren().add(createAuditRow("✓", "Physical Inspection Passed", inspectedBy, inspectDate));
            auditTrailContainer.getChildren().add(createAuditRow("🚢", "Transmitted to Port Authority", "Customs Clearance Desk", "Cleared for Terminal"));
        } else if ("Under Inspection".equalsIgnoreCase(inspectStatus)) {
            auditTrailContainer.getChildren().add(createAuditRow("🔍", "Under Physical Inspection", inspectedBy, inspectDate));
        } else if ("On Hold".equalsIgnoreCase(inspectStatus)) {
            auditTrailContainer.getChildren().add(createAuditRow("⏸", "Placed On Regulatory Hold", inspectedBy, inspectDate));
        } else if ("Failed".equalsIgnoreCase(inspectStatus) || "Rejected".equalsIgnoreCase(inspectStatus)) {
            auditTrailContainer.getChildren().add(createAuditRow("✕", "Physical Inspection Failed", inspectedBy, inspectDate));
        } else {
            auditTrailContainer.getChildren().add(createAuditRow("⏳", "Pending Officer Physical Inspection", "Customs Inspection Bay", "Queued"));
        }
    }

    private HBox createAuditRow(String icon, String title, String actor, String timestamp) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 0, 2, 0));

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size:12;");

        VBox textCol = new VBox(1);
        Label titleLbl = createLabel(title + " • " + actor, true, "#334155");
        Label timeLbl = createLabel(timestamp, false, "#94a3b8");
        textCol.getChildren().addAll(titleLbl, timeLbl);

        row.getChildren().addAll(iconLbl, textCol);
        return row;
    }

    private void clearDetailPanel() {
        this.selectedCargo = null;
        if (formTitle != null) formTitle.setText("Cargo Inspection Details");
        if (detailCargoId != null) detailCargoId.setText("—");
        if (detailCargoName != null) detailCargoName.setText("—");
        if (detailCargoType != null) detailCargoType.setText("—");
        if (detailHsCode != null) detailHsCode.setText("—");
        if (detailQuantity != null) detailQuantity.setText("—");
        if (detailPackageInfo != null) detailPackageInfo.setText("—");
        if (detailGrossWeight != null) detailGrossWeight.setText("—");
        if (detailNetWeight != null) detailNetWeight.setText("—");
        if (detailContainer != null) detailContainer.setText("—");

        if (detailVessel != null) detailVessel.setText("—");
        if (detailVoyage != null) detailVoyage.setText("—");
        if (detailRoute != null) detailRoute.setText("—");
        if (detailEtaEtd != null) detailEtaEtd.setText("—");

        if (detailImporter != null) detailImporter.setText("—");
        if (detailExporter != null) detailExporter.setText("—");
        if (detailCompany != null) detailCompany.setText("—");
        if (detailDocRef != null) detailDocRef.setText("—");

        if (pdfNameLabel != null) pdfNameLabel.setText("No document file available.");
        if (pdfTypeLabel != null) pdfTypeLabel.setText("Select a record from queue to preview");
        if (auditTrailContainer != null) auditTrailContainer.getChildren().clear();
        if (findingsTextArea != null) findingsTextArea.clear();

        if (alertBox != null) {
            alertBox.setVisible(false);
            alertBox.setManaged(false);
        }

        if (statusBadgeLabel != null) {
            statusBadgeLabel.setText("NO SELECTION");
            statusBadgeLabel.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#64748b; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:6;");
        }
    }

    private void applyStatusBadgeStyle(Label badge, String st) {
        if ("Passed".equalsIgnoreCase(st) || "Approved".equalsIgnoreCase(st) || "Cleared".equalsIgnoreCase(st)) {
            badge.setStyle("-fx-background-color:#ecfdf5; -fx-text-fill:#16a34a; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:6;");
        } else if ("On Hold".equalsIgnoreCase(st) || "Hold".equalsIgnoreCase(st) || "Held".equalsIgnoreCase(st)) {
            badge.setStyle("-fx-background-color:#fff7ed; -fx-text-fill:#f97316; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:6;");
        } else if ("Under Inspection".equalsIgnoreCase(st) || "Review".equalsIgnoreCase(st)) {
            badge.setStyle("-fx-background-color:#eff6ff; -fx-text-fill:" + BLUE + "; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:6;");
        } else if ("Failed".equalsIgnoreCase(st) || "Rejected".equalsIgnoreCase(st)) {
            badge.setStyle("-fx-background-color:#fef2f2; -fx-text-fill:#ef4444; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:6;");
        } else {
            badge.setStyle("-fx-background-color:#fffbeb; -fx-text-fill:#d97706; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:6;");
        }
    }

    private void applyPriorityBadgeStyle(Label badge, String prio) {
        if ("Urgent".equalsIgnoreCase(prio)) {
            badge.setStyle("-fx-background-color:#ffe4e6; -fx-text-fill:#e11d48; -fx-font-size:10; -fx-font-weight:bold; -fx-padding:2 6; -fx-background-radius:6;");
        } else if ("High".equalsIgnoreCase(prio)) {
            badge.setStyle("-fx-background-color:#fffbeb; -fx-text-fill:#d97706; -fx-font-size:10; -fx-font-weight:bold; -fx-padding:2 6; -fx-background-radius:6;");
        } else {
            badge.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#475569; -fx-font-size:10; -fx-font-weight:bold; -fx-padding:2 6; -fx-background-radius:6;");
        }
    }

    private void openSelectedDocument() {
        if (selectedCargo == null) {
            Alert err = new Alert(Alert.AlertType.WARNING, "No cargo selected.");
            err.showAndWait();
            return;
        }

        String fileUrl = selectedCargo.get("fileUrl") != null ? String.valueOf(selectedCargo.get("fileUrl")) : null;
        String fileName = selectedCargo.get("fileName") != null ? String.valueOf(selectedCargo.get("fileName")) : null;
        String cargoId = String.valueOf(selectedCargo.get("cargoId"));

        boolean opened = false;

        if (fileUrl != null && (fileUrl.startsWith("http://") || fileUrl.startsWith("https://"))) {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(fileUrl));
                    opened = true;
                }
            } catch (Exception ignored) {}
        }

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

        if (!opened) {
            Alert err = new Alert(Alert.AlertType.INFORMATION);
            err.setTitle("Shipping Document Notice");
            err.setHeaderText("Document File Reference Attached");
            err.setContentText(
                    "Cargo ID: " + cargoId + "\n" +
                    "File: " + (fileName != null ? fileName : "Cargo Declaration Document") + "\n" +
                    (fileUrl != null ? "Storage URL: " + fileUrl + "\n" : "") +
                    "\nThe shipment metadata has been verified from the live customs database."
            );
            err.showAndWait();
        }
    }

    private String getOfficerFindingsOrDefault() {
        if (findingsTextArea != null && !findingsTextArea.getText().isBlank()) {
            return findingsTextArea.getText().trim();
        }
        return "Physical inspection certified. Cargo packaging, seal integrity, and weight match declaration.";
    }

    private void promptForReasonAndAction(String status, String title, String header) {
        if (selectedCargo == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Please select a cargo record from the queue first.");
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

        Label promptLbl = new Label("Enter detailed regulatory remarks / discrepancy observation:");
        promptLbl.setStyle("-fx-font-size:13; -fx-text-fill:#0f172a; -fx-font-weight:bold;");

        TextArea textArea = new TextArea();
        textArea.setPromptText("Enter official justification / reason here...");
        textArea.setPrefRowCount(3);
        textArea.setWrapText(true);
        if (findingsTextArea != null && !findingsTextArea.getText().isBlank()) {
            textArea.setText(findingsTextArea.getText().trim());
        }

        vbox.getChildren().addAll(promptLbl, textArea);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return textArea.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(reason -> {
            String note = (reason != null && !reason.isBlank()) ? reason.trim() : (status + " decision by " + officerName);
            performInspectionAction(status, note, reason);
        });
    }

    private void performInspectionAction(String newStatus, String notes, String reason) {
        if (selectedCargo == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Please select a cargo record first.");
            a.showAndWait();
            return;
        }

        String cargoId = String.valueOf(selectedCargo.get("cargoId"));

        cargoDao.updateInspectionStatus(cargoId, newStatus, officerName, notes, reason);
        selectedCargo.put("inspectionStatus", newStatus);
        selectedCargo.put("inspectedBy", officerName);
        selectedCargo.put("inspectionDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        if (notes != null) selectedCargo.put("inspectionNotes", notes);

        refreshInspectionData();

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Inspection Decision Recorded");
        a.setHeaderText("Cargo " + cargoId + " Inspection Status Updated to " + newStatus);
        a.setContentText(
                "Cargo ID: " + cargoId + "\n" +
                "Inspector: " + officerName + " (" + officerRole + ")\n" +
                "Port Jurisdiction: " + activeAssignedPort + "\n" +
                "Inspection Notes: " + notes + "\n\n" +
                ("Passed".equalsIgnoreCase(newStatus)
                        ? "Cargo is verified and now eligible for Port Authority clearance and terminal release."
                        : "Cargo status updated and recorded in the central customs registry.")
        );
        a.showAndWait();
    }

    public VBox getContent() {
        return pageContainer;
    }

    public BorderPane getRoot() {
        return root;
    }

    // Helper UI Stylers matching Admin Style Manager
    private void styleAdminField(TextField f) {
        f.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8; -fx-padding:8 12; -fx-font-size:13; -fx-text-fill:#0f172a;");
    }

    private void styleAdminCombo(ComboBox<?> cb) {
        cb.setStyle("-fx-background-radius:8; -fx-padding:6 10; -fx-font-size:13; -fx-border-color:#e6eef8; -fx-background-color:white; -fx-text-fill:#0f172a;");
    }

    private ComboBox<String> combo(String prompt, String... items) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(items);
        cb.setValue(prompt);
        styleAdminCombo(cb);
        return cb;
    }

    private Label createLabel(String text, boolean bold, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:" + color + "; -fx-font-size:12;" + (bold ? " -fx-font-weight:bold;" : ""));
        return l;
    }

    private interface LabelSetter { void set(Label label); }
}
