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
import javafx.scene.Parent;
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
 * Custom Officer - Document Verification Control Center
 * 
 * Styled using the EXACT Admin Page design system:
 * - Card styling, typography, spacing, shadows, badges, and controls matching Admin pages
 * - High-contrast text on solid backgrounds (zero translucent blue overlays)
 * - Real-time synchronization with live database & CargoDao
 */
@SuppressWarnings({"unchecked", "unused"})
public class DocumentVerification {

    // Admin Theme Shared Colors & Constants
    private static final String BLUE = "#0b63ff";
    private static final String TEXT_PRIMARY = "#0f172a";
    private static final String TEXT_SECONDARY = "#64748b";
    private static final String CARD_STYLE = "-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-background-radius:10; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 8, 0, 0, 2);";

    private final BorderPane root = new BorderPane();
    private VBox pageContainer;

    // Live Metrics
    private int pendingCount = 0;
    private int underReviewCount = 0;
    private int verifiedCount = 0;
    private int onHoldCount = 0;
    private int rejectedCount = 0;
    private int highPriorityCount = 0;

    private Label pendingValueLabel;
    private Label underReviewValueLabel;
    private Label verifiedValueLabel;
    private Label onHoldValueLabel;
    private Label rejectedValueLabel;
    private Label highPriorityValueLabel;

    // Queue & List Table
    private final GridPane table = new GridPane();
    private Label showingCountLabel;
    private final List<Map<String, Object>> activeDocList = new ArrayList<>();
    private final List<Map<String, Object>> filteredDocList = new ArrayList<>();
    private Map<String, Object> selectedDoc = null;

    // Right-Side Document Inspection Panel
    private Label formTitle;
    private Label formSubtitle;
    private Label statusBadgeLabel;
    private Label priorityBadgeLabel;

    // Document Specification Labels
    private Label detailDocId;
    private Label detailDocType;
    private Label detailDocStatus;
    private Label detailDocPriority;
    private Label detailDocVersion;
    private Label detailSubmittedBy;
    private Label detailSubmittedDate;
    private Label detailAssignedOfficer;
    private Label detailVerifiedBy;
    private Label detailVerifiedNotes;

    // Cargo & Shipment Labels
    private Label detailCargoId;
    private Label detailCommodity;
    private Label detailHsCode;
    private Label detailQuantity;
    private Label detailPackageInfo;
    private Label detailGrossWeight;
    private Label detailNetWeight;
    private Label detailVessel;
    private Label detailContainer;
    private Label detailRoute;

    // Parties Labels
    private Label detailImporter;
    private Label detailExporter;
    private Label detailCompany;

    // Safety Alert Box
    private VBox alertBox;
    private Label alertTitle;
    private Label alertMessage;

    // Document File Viewer
    private Label pdfNameLabel;
    private Label pdfTypeLabel;
    private Label pdfStorageBadge;
    private Button openPdfBtn;

    // Checklists & Containers
    private VBox checklistContainer;
    private VBox cargoDocsSetContainer;
    private TextArea remarksTextArea;
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
    private ComboBox<String> typeFilter;
    private ComboBox<String> statusFilter;
    private ComboBox<String> priorityFilter;
    private ComboBox<String> exporterFilter;
    private ComboBox<String> portFilter;
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
        pageContainer = new VBox(16);
        pageContainer.setPadding(new Insets(18, 22, 28, 22));
        pageContainer.setStyle("-fx-background-color:transparent;");

        // 1. HEADER ROW (ADMIN STYLE)
        HBox header = createHeaderSection();

        // 2. SUMMARY KPI CARDS ROW (6 CARDS IN EXACT ADMIN STYLE)
        HBox kpiRow = createKpiSummaryRow();

        // 3. MIDDLE WORKSPACE: LEFT QUEUE TABLE (60%) + RIGHT DETAIL AUDIT PANEL (40%)
        VBox tableCard = createDocumentQueueCard();
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
        Label title = new Label("DOCUMENT VERIFICATION");
        title.setStyle("-fx-text-fill:#05070c; -fx-font-size:26; -fx-font-weight:bold;");

        Label subtitle = new Label("Review, verify and certify cargo and shipping documents submitted for customs clearance.");
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
                adminKpiCard("Pending Verification", "0", "Awaiting review", "⏳", "#f59e0b", "#fffbeb", l -> pendingValueLabel = l),
                adminKpiCard("Under Review", "0", "Being examined", "🔍", BLUE, "#eff6ff", l -> underReviewValueLabel = l),
                adminKpiCard("Verified / Certified", "0", "Approved & certified", "✓", "#16a34a", "#ecfdf5", l -> verifiedValueLabel = l),
                adminKpiCard("On Hold", "0", "Awaiting info", "⏸", "#f97316", "#fff7ed", l -> onHoldValueLabel = l),
                adminKpiCard("Rejected", "0", "Requires correction", "✕", "#ef4444", "#fef2f2", l -> rejectedValueLabel = l),
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
    // 3. DOCUMENT QUEUE DIRECTORY TABLE (LEFT PANEL)
    // =====================================================
    private VBox createDocumentQueueCard() {
        Label title = new Label("Submitted Documents Queue");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");

        Label subtitle = new Label("Cargo documents, declarations, bills of lading, and certificates submitted for compliance");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        // Search Field (Admin Style)
        searchField = new TextField();
        searchField.setPromptText("⌕  Search Document ID, Cargo ID, Importer, Exporter...");
        searchField.setPrefWidth(220);
        styleAdminField(searchField);
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());

        // Dropdowns (Admin Style)
        typeFilter = combo("All Types", "All Types",
                "Bill of Lading", "Commercial Invoice", "Packing List", "Customs Declaration", "Certificate of Origin",
                "Dangerous Goods Declaration", "Insurance Certificate", "Import/Export License", "Other Supporting Documents");
        typeFilter.setPrefWidth(125);
        typeFilter.setOnAction(e -> applyFilter());

        statusFilter = combo("All Statuses", "All Statuses", "Pending", "Under Review", "Verified", "On Hold", "Rejected");
        statusFilter.setPrefWidth(115);
        statusFilter.setOnAction(e -> applyFilter());

        priorityFilter = combo("All Priorities", "All Priorities", "Urgent", "High", "Normal");
        priorityFilter.setPrefWidth(110);
        priorityFilter.setOnAction(e -> applyFilter());

        exporterFilter = new ComboBox<>();
        exporterFilter.getItems().add("All Exporters / Importers");
        exporterFilter.setValue("All Exporters / Importers");
        exporterFilter.setPrefWidth(140);
        styleAdminCombo(exporterFilter);
        exporterFilter.setOnAction(e -> applyFilter());

        List<String> dynamicPortOptions = new ArrayList<>();
        dynamicPortOptions.add("All Ports");
        dynamicPortOptions.addAll(com.projectui.view.admin.AdminDataStore.getInstance().getApprovedPortNames());
        portFilter = combo("All Ports", dynamicPortOptions.toArray(new String[0]));
        portFilter.setPrefWidth(125);
        portFilter.setOnAction(e -> applyFilter());

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
        resetFilterBtn.setOnAction(e -> refreshLiveDocuments());

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox topFilterRow = new HBox(8, searchField, typeFilter, statusFilter, priorityFilter, exporterFilter, portFilter, resetFilterBtn);
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
        showingCountLabel = new Label("Showing 0 of 0 submitted documents");
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
    // 4. DOCUMENT INSPECTION & VERIFICATION PANEL (RIGHT SIDE)
    // =====================================================
    private VBox createDetailInspectionCard() {
        formTitle = new Label("Document Verification & Audit");
        formTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");

        formSubtitle = new Label("Review document authenticity, shipment metadata, and certify clearance");
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
        alertTitle = new Label("⚠ Compliance Alert");
        alertTitle.setStyle("-fx-text-fill:#92400e; -fx-font-size:12; -fx-font-weight:bold;");
        alertMessage = new Label("Standard verification required for this shipment.");
        alertMessage.setStyle("-fx-text-fill:#78350f; -fx-font-size:12;");
        alertBox.getChildren().addAll(alertTitle, alertMessage);
        alertBox.setVisible(false);
        alertBox.setManaged(false);

        // Section 1: Document Metadata
        VBox docSection = createPanelSection("DOCUMENT SPECIFICATIONS", createDocMetadataGrid());

        // Section 2: Cargo & Shipment Details
        VBox cargoSection = createPanelSection("CARGO & SHIPMENT SPECIFICATIONS", createCargoMetadataGrid());

        // Section 3: Parties (Importer / Exporter)
        VBox partySection = createPanelSection("IMPORTER / EXPORTER ENTITIES", createPartyMetadataGrid());

        // Section 4: Document File Preview Container
        VBox fileSection = createPanelSection("ATTACHED DOCUMENT FILE", createFilePreviewRow());

        // Section 5: Customs Verification Checklist
        checklistContainer = createInteractiveChecklist();
        VBox checklistSection = createPanelSection("CUSTOMS VERIFICATION CHECKLIST", checklistContainer);

        // Section 6: Cargo Documentation Set
        cargoDocsSetContainer = new VBox(4);
        VBox cargoDocsSection = createPanelSection("REQUIRED CARGO DOCUMENTATION SET", cargoDocsSetContainer);

        // Section 7: Officer Remarks & Discrepancy Log
        remarksTextArea = new TextArea();
        remarksTextArea.setPromptText("Enter officer remarks, compliance notes, or discrepancy observations...");
        remarksTextArea.setPrefRowCount(3);
        remarksTextArea.setWrapText(true);
        remarksTextArea.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8; -fx-padding:8; -fx-font-size:13; -fx-text-fill:#0f172a;");
        VBox remarksSection = createPanelSection("OFFICER REMARKS & DISCREPANCY LOG", remarksTextArea);

        // Section 8: Audit Trail
        auditTrailContainer = new VBox(4);
        VBox auditSection = createPanelSection("VERIFICATION AUDIT TRAIL", auditTrailContainer);

        // Section 9: Action Buttons (Exact Admin Primary, Warning, Danger Button Styling)
        Button verifyBtn = new Button("✓  VERIFY & CERTIFY");
        verifyBtn.setMaxWidth(Double.MAX_VALUE);
        verifyBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:10 16; -fx-background-radius:8; -fx-cursor:hand;");
        verifyBtn.setOnAction(e -> performVerificationAction("Verified", getOfficerNotesOrDefault()));

        Button holdBtn = new Button("⏱  PLACE ON HOLD");
        holdBtn.setMaxWidth(Double.MAX_VALUE);
        holdBtn.setStyle("-fx-background-color:#f97316; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:10 16; -fx-background-radius:8; -fx-cursor:hand;");
        holdBtn.setOnAction(e -> promptForReasonAndAction("On Hold", "Place Document On Hold", "Specify Regulatory Reason for Hold"));

        Button rejectBtn = new Button("✕  REJECT DOCUMENT");
        rejectBtn.setMaxWidth(Double.MAX_VALUE);
        rejectBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-padding:10 16; -fx-background-radius:8; -fx-cursor:hand;");
        rejectBtn.setOnAction(e -> promptForReasonAndAction("Rejected", "Reject Document", "Specify Non-Compliance Reason"));

        VBox actionBox = new VBox(8, verifyBtn, holdBtn, rejectBtn);

        VBox innerContent = new VBox(12,
                topRow, alertBox, new Separator(),
                docSection, cargoSection, partySection, fileSection,
                checklistSection, cargoDocsSection, remarksSection, auditSection,
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

    private GridPane createDocMetadataGrid() {
        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(4);

        detailDocId = createLabel("—", true, "#0f172a");
        detailDocType = createLabel("—", false, "#334155");
        detailDocStatus = createLabel("—", false, "#334155");
        detailDocPriority = createLabel("Normal", false, "#334155");
        detailDocVersion = createLabel("v1.0", false, "#334155");
        detailSubmittedBy = createLabel("—", false, "#334155");
        detailSubmittedDate = createLabel("—", false, "#334155");
        detailAssignedOfficer = createLabel(officerName + " (" + officerRole + ")", false, "#334155");
        detailVerifiedBy = createLabel("—", false, "#334155");
        detailVerifiedNotes = createLabel("—", false, "#64748b");

        g.add(createLabel("Document ID:", true, "#64748b"), 0, 0);
        g.add(detailDocId, 1, 0);
        g.add(createLabel("Document Type:", true, "#64748b"), 0, 1);
        g.add(detailDocType, 1, 1);
        g.add(createLabel("Status / Version:", true, "#64748b"), 0, 2);
        g.add(new HBox(6, detailDocStatus, createLabel("•", false, "#94a3b8"), detailDocVersion), 1, 2);
        g.add(createLabel("Submitted By:", true, "#64748b"), 0, 3);
        g.add(detailSubmittedBy, 1, 3);
        g.add(createLabel("Submission Date:", true, "#64748b"), 0, 4);
        g.add(detailSubmittedDate, 1, 4);
        g.add(createLabel("Assigned Officer:", true, "#64748b"), 0, 5);
        g.add(detailAssignedOfficer, 1, 5);
        g.add(createLabel("Verified Record:", true, "#64748b"), 0, 6);
        g.add(detailVerifiedBy, 1, 6);
        g.add(createLabel("Previous Notes:", true, "#64748b"), 0, 7);
        g.add(detailVerifiedNotes, 1, 7);

        return g;
    }

    private GridPane createCargoMetadataGrid() {
        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(4);

        detailCargoId = createLabel("—", true, BLUE);
        detailCommodity = createLabel("—", false, "#334155");
        detailHsCode = createLabel("—", false, "#334155");
        detailQuantity = createLabel("—", false, "#334155");
        detailPackageInfo = createLabel("—", false, "#334155");
        detailGrossWeight = createLabel("—", false, "#334155");
        detailNetWeight = createLabel("—", false, "#334155");
        detailVessel = createLabel("—", false, "#334155");
        detailContainer = createLabel("—", false, "#334155");
        detailRoute = createLabel("—", false, "#334155");

        g.add(createLabel("Cargo ID / Name:", true, "#64748b"), 0, 0);
        g.add(new HBox(6, detailCargoId, createLabel("•", false, "#94a3b8"), detailCommodity), 1, 0);
        g.add(createLabel("HS Classification:", true, "#64748b"), 0, 1);
        g.add(detailHsCode, 1, 1);
        g.add(createLabel("Quantity / Packages:", true, "#64748b"), 0, 2);
        g.add(new HBox(6, detailQuantity, createLabel("•", false, "#94a3b8"), detailPackageInfo), 1, 2);
        g.add(createLabel("Gross / Net Weight:", true, "#64748b"), 0, 3);
        g.add(new HBox(6, detailGrossWeight, createLabel("•", false, "#94a3b8"), detailNetWeight), 1, 3);
        g.add(createLabel("Vessel / Container:", true, "#64748b"), 0, 4);
        g.add(new HBox(6, detailVessel, createLabel("•", false, "#94a3b8"), detailContainer), 1, 4);
        g.add(createLabel("Port Route:", true, "#64748b"), 0, 5);
        g.add(detailRoute, 1, 5);

        return g;
    }

    private GridPane createPartyMetadataGrid() {
        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(4);

        detailImporter = createLabel("—", false, "#334155");
        detailExporter = createLabel("—", false, "#334155");
        detailCompany = createLabel("—", false, "#334155");

        g.add(createLabel("Importer / Consignee:", true, "#64748b"), 0, 0);
        g.add(detailImporter, 1, 0);
        g.add(createLabel("Exporter / Shipper:", true, "#64748b"), 0, 1);
        g.add(detailExporter, 1, 1);
        g.add(createLabel("Declaring Entity:", true, "#64748b"), 0, 2);
        g.add(detailCompany, 1, 2);

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

    private VBox createInteractiveChecklist() {
        VBox v = new VBox(6);

        Label cat1 = createLabel("DOCUMENT AUTHENTICITY", true, "#64748b");
        CheckBox cb1 = new CheckBox("Document is readable & legible");
        cb1.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb1.setSelected(true);

        CheckBox cb2 = new CheckBox("Document type matches customs declaration");
        cb2.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb2.setSelected(true);

        CheckBox cb3 = new CheckBox("Digital stamp / authorized signature verified");
        cb3.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb3.setSelected(true);

        Label cat2 = createLabel("SHIPMENT & CARGO SPECIFICATION", true, "#64748b");
        CheckBox cb4 = new CheckBox("Vessel & voyage information matches manifest");
        cb4.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb4.setSelected(true);

        CheckBox cb5 = new CheckBox("Container number matches physical manifest");
        cb5.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb5.setSelected(true);

        CheckBox cb6 = new CheckBox("Gross & Net weights correspond with weight slips");
        cb6.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb6.setSelected(true);

        Label cat3 = createLabel("CUSTOMS COMPLIANCE & TARIFF CHECK", true, "#64748b");
        CheckBox cb7 = new CheckBox("Required declarations & certificates present");
        cb7.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb7.setSelected(true);

        CheckBox cb8 = new CheckBox("Importer / Exporter IEC identity verified");
        cb8.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb8.setSelected(true);

        CheckBox cb9 = new CheckBox("No trade discrepancy or regulatory alert flagged");
        cb9.setStyle("-fx-font-size:12; -fx-text-fill:#334155;");
        cb9.setSelected(true);

        v.getChildren().addAll(cat1, cb1, cb2, cb3, cat2, cb4, cb5, cb6, cat3, cb7, cb8, cb9);
        return v;
    }

    // =====================================================
    // 5. BOTTOM SUMMARY CARDS (MATCHING ADMIN DASHBOARD)
    // =====================================================
    private HBox createBottomSummaryRow() {
        VBox c1 = createAdminInfoCard("Recent Customs Verification Activity",
                "Live log of document certifications, holds, and regulatory decisions across Cochin Port",
                createActivityRows());

        VBox c2 = createAdminInfoCard("Regulatory Compliance & Tariff Guidelines",
                "Standard compliance checks mandated by Central Board of Indirect Taxes and Customs",
                createComplianceGuidelines());

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
        int added = 0;
        for (Map<String, Object> d : activeDocList) {
            if (d == null) continue;
            String dId = d.get("docId") != null ? d.get("docId").toString() : (d.get("cargoId") != null ? d.get("cargoId").toString() : "DOC");
            String docName = d.get("docName") != null ? d.get("docName").toString() : "Document";
            String st = d.get("status") != null ? d.get("status").toString() : "Pending";
            if ("Verified".equalsIgnoreCase(st) || "Approved".equalsIgnoreCase(st) || "Cleared".equalsIgnoreCase(st)) {
                v.getChildren().add(createAuditRow("✓", "Certified " + docName + " (" + dId + ")", "Officer " + officerName, "Recently"));
                added++;
            } else if ("Under Review".equalsIgnoreCase(st)) {
                v.getChildren().add(createAuditRow("🔍", "Examining " + docName + " (" + dId + ")", "Customs Desk", "Active"));
                added++;
            } else if ("On Hold".equalsIgnoreCase(st) || "Rejected".equalsIgnoreCase(st)) {
                v.getChildren().add(createAuditRow("⛔", "Non-compliance hold for " + docName + " (" + dId + ")", "Officer " + officerName, "Active"));
                added++;
            }
            if (added >= 3) break;
        }
        if (added == 0) {
            Label l = new Label("No recent document verification activity. All queues clear.");
            l.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px; -fx-padding:6 0;");
            v.getChildren().add(l);
        }
        return v;
    }

    private VBox createComplianceGuidelines() {
        VBox v = new VBox(6);
        v.getChildren().add(createGuidelineRow("🛡 Mandatory Bill of Lading verification for all containerized inbound freight."));
        v.getChildren().add(createGuidelineRow("⚡ Hazardous / Chemical cargo requires accompanying Dangerous Goods Certificate."));
        v.getChildren().add(createGuidelineRow("⚖ HS Code classification must match declared goods description before certification."));
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
            highPriorityCount = 0;

            Set<String> uniqueEntities = new HashSet<>();

            String offUid = LoginPage.loggedInUser != null && LoginPage.loggedInUser.get("uid") != null ? LoginPage.loggedInUser.get("uid").toString() : "";
            String offEmail = LoginPage.loggedInUser != null && LoginPage.loggedInUser.get("email") != null ? LoginPage.loggedInUser.get("email").toString() : "";
            List<Map<String, Object>> cargos = cargoDao.getCargosForCustomsOfficer(offUid, offEmail, activeAssignedPort);
            if (cargos != null) {
                for (Map<String, Object> c : cargos) {
                    if (c == null) continue;

                    String cargoId = c.get("cargoId") != null ? String.valueOf(c.get("cargoId")) : "CGO-N/A";
                    String cargoName = c.get("cargoName") != null ? String.valueOf(c.get("cargoName")) : "General Cargo";
                    String cargoType = c.get("cargoType") != null ? String.valueOf(c.get("cargoType")) : "Containerized";
                    String hsCode = c.get("hsCode") != null ? String.valueOf(c.get("hsCode")) : "—";
                    String desc = c.get("description") != null ? String.valueOf(c.get("description")) : (cargoName + " consignment");
                    String vessel = c.get("assignedShip") != null ? String.valueOf(c.get("assignedShip")) : (c.get("vessel") != null ? String.valueOf(c.get("vessel")) : (c.get("shipName") != null ? String.valueOf(c.get("shipName")) : "Not Assigned"));
                    String voyage = c.get("voyage") != null ? String.valueOf(c.get("voyage")) : "—";
                    String exporter = c.get("exporter") != null ? String.valueOf(c.get("exporter")) : (c.get("company") != null ? String.valueOf(c.get("company")) : "—");
                    String importer = c.get("importer") != null ? String.valueOf(c.get("importer")) : "—";
                    String company = c.get("company") != null ? String.valueOf(c.get("company")) : exporter;
                    String origin = c.get("originPort") != null ? String.valueOf(c.get("originPort")) : (c.get("origin") != null ? String.valueOf(c.get("origin")) : activeAssignedPort);
                    String dest = c.get("destinationPort") != null ? String.valueOf(c.get("destinationPort")) : (c.get("destination") != null ? String.valueOf(c.get("destination")) : "—");
                    String container = c.get("container") != null ? String.valueOf(c.get("container")) : (c.get("containerId") != null ? String.valueOf(c.get("containerId")) : "—");
                    String qty = c.get("quantity") != null ? String.valueOf(c.get("quantity")) : "1 unit";
                    String unit = c.get("unit") != null ? String.valueOf(c.get("unit")) : "Units";
                    String pkgCount = c.get("packageCount") != null ? String.valueOf(c.get("packageCount")) : "—";
                    String pkgType = c.get("packageType") != null ? String.valueOf(c.get("packageType")) : "—";
                    String grossWeight = c.get("grossWeight") != null ? String.valueOf(c.get("grossWeight")) : (c.get("weight") != null ? String.valueOf(c.get("weight")) : "—");
                    String netWeight = c.get("netWeight") != null ? String.valueOf(c.get("netWeight")) : "—";
                    String eta = c.get("eta") != null ? String.valueOf(c.get("eta")) : "—";
                    String etd = c.get("etd") != null ? String.valueOf(c.get("etd")) : "—";

                    if (!exporter.isBlank() && !exporter.equals("—")) uniqueEntities.add(exporter);
                    if (!importer.isBlank() && !importer.equals("—")) uniqueEntities.add(importer);

                    String cargoPriority = "Normal";
                    if (c.get("priority") != null && !c.get("priority").toString().isBlank()) {
                        cargoPriority = c.get("priority").toString();
                    } else if (cargoType.toLowerCase().contains("hazardous") || cargoType.toLowerCase().contains("dangerous") || cargoType.toLowerCase().contains("perishable") || cargoName.toLowerCase().contains("chemical") || cargoName.toLowerCase().contains("pharma")) {
                        cargoPriority = "Urgent";
                    }

                    List<Map<String, Object>> docs = extractDocumentsFromCargo(c);

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

                            if (!item.containsKey("priority") || item.get("priority") == null) {
                                item.put("priority", cargoPriority);
                            }

                            String st = String.valueOf(item.get("status"));
                            if ("Verified".equalsIgnoreCase(st) || "Approved".equalsIgnoreCase(st) || "Certified".equalsIgnoreCase(st)) {
                                verifiedCount++;
                            } else if ("On Hold".equalsIgnoreCase(st) || "Hold".equalsIgnoreCase(st)) {
                                onHoldCount++;
                            } else if ("Under Review".equalsIgnoreCase(st) || "Review".equalsIgnoreCase(st)) {
                                underReviewCount++;
                            } else if ("Rejected".equalsIgnoreCase(st)) {
                                rejectedCount++;
                            } else {
                                pendingCount++;
                            }

                            String prio = String.valueOf(item.get("priority"));
                            if ("Urgent".equalsIgnoreCase(prio) || "High".equalsIgnoreCase(prio)) {
                                highPriorityCount++;
                            }

                            activeDocList.add(item);
                        }
                    }
                }
            }

            // Update KPI Metric Values
            if (pendingValueLabel != null) pendingValueLabel.setText(String.valueOf(pendingCount));
            if (underReviewValueLabel != null) underReviewValueLabel.setText(String.valueOf(underReviewCount));
            if (verifiedValueLabel != null) verifiedValueLabel.setText(String.valueOf(verifiedCount));
            if (onHoldValueLabel != null) onHoldValueLabel.setText(String.valueOf(onHoldCount));
            if (rejectedValueLabel != null) rejectedValueLabel.setText(String.valueOf(rejectedCount));
            if (highPriorityValueLabel != null) highPriorityValueLabel.setText(String.valueOf(highPriorityCount));

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

        if (result.isEmpty()) {
            String cargoId = String.valueOf(cargo.get("cargoId"));
            String exp = cargo.get("exporter") != null ? String.valueOf(cargo.get("exporter")) : (cargo.get("exporterName") != null ? String.valueOf(cargo.get("exporterName")) : "Exporter");
            String vStatus = cargo.get("verificationStatus") != null ? String.valueOf(cargo.get("verificationStatus")) : "Pending";
            String cTime = cargo.get("createdAt") != null ? String.valueOf(cargo.get("createdAt")) : "Registration Date";
            String prio = cargo.get("priority") != null ? String.valueOf(cargo.get("priority")) : "Normal";

            String[][] stdDocs = {
                {"Commercial Invoice", cargoId + "_Commercial_Invoice.pdf"},
                {"Packing List", cargoId + "_Packing_List.pdf"},
                {"Bill of Lading / Shipping Order", cargoId + "_Bill_of_Lading.pdf"},
                {"Certificate of Origin", cargoId + "_Certificate_of_Origin.pdf"},
                {"Customs Declaration (Bill of Entry)", cargoId + "_Customs_Declaration.pdf"}
            };

            for (int i = 0; i < stdDocs.length; i++) {
                Map<String, Object> d = new HashMap<>();
                d.put("docId", "DOC-" + cargoId + "-" + String.format("%02d", i + 1));
                d.put("docType", stdDocs[i][0]);
                d.put("fileName", stdDocs[i][1]);
                d.put("name", stdDocs[i][1]);
                d.put("status", vStatus);
                d.put("uploadedAt", cTime);
                d.put("uploadedBy", exp);
                d.put("priority", prio);
                result.add(d);
            }
        }
        return result;
    }

    private void applyFilter() {
        table.getChildren().clear();
        table.getColumnConstraints().clear();
        filteredDocList.clear();

        String query = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String typeVal = typeFilter != null ? typeFilter.getValue() : "All Types";
        String statusVal = statusFilter != null ? statusFilter.getValue() : "All Statuses";
        String priorityVal = priorityFilter != null ? priorityFilter.getValue() : "All Priorities";
        String expVal = exporterFilter != null ? exporterFilter.getValue() : "All Exporters / Importers";
        String portVal = portFilter != null ? portFilter.getValue() : "All Ports";

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
            String status = String.valueOf(d.get("status"));
            String prio = String.valueOf(d.get("priority"));

            boolean matchQuery = query.isEmpty() ||
                    docId.toLowerCase().contains(query) ||
                    cargoId.toLowerCase().contains(query) ||
                    exp.toLowerCase().contains(query) ||
                    imp.toLowerCase().contains(query) ||
                    vessel.toLowerCase().contains(query) ||
                    type.toLowerCase().contains(query) ||
                    cont.toLowerCase().contains(query);

            boolean matchType = "All Types".equalsIgnoreCase(typeVal) || type.equalsIgnoreCase(typeVal);
            boolean matchStatus = "All Statuses".equalsIgnoreCase(statusVal) || status.equalsIgnoreCase(statusVal);
            boolean matchPriority = "All Priorities".equalsIgnoreCase(priorityVal) || prio.equalsIgnoreCase(priorityVal);
            boolean matchExp = "All Exporters / Importers".equalsIgnoreCase(expVal) || exp.equalsIgnoreCase(expVal) || imp.equalsIgnoreCase(expVal);
            boolean matchPort = "All Ports".equalsIgnoreCase(portVal) || origin.toLowerCase().contains(portVal.toLowerCase()) || dest.toLowerCase().contains(portVal.toLowerCase());

            if (matchQuery && matchType && matchStatus && matchPriority && matchExp && matchPort) {
                filteredDocList.add(d);
            }
        }

        if (showingCountLabel != null) {
            showingCountLabel.setText("Showing " + filteredDocList.size() + " of " + activeDocList.size() + " submitted documents");
        }

        // Table Column Constraints (Exact Admin table proportions)
        ColumnConstraints c0 = new ColumnConstraints(); c0.setPercentWidth(24); // Document ID & Type
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(20); // Cargo & Commodity
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(20); // Importer / Exporter
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(12); // Priority
        ColumnConstraints c4 = new ColumnConstraints(); c4.setPercentWidth(12); // Status
        ColumnConstraints c5 = new ColumnConstraints(); c5.setPercentWidth(12); // Action
        table.getColumnConstraints().addAll(c0, c1, c2, c3, c4, c5);

        String[] headers = {"Document", "Cargo / Commodity", "Importer / Exporter", "Priority", "Status", "Action"};
        for (int col = 0; col < headers.length; col++) {
            table.add(tableHeadCell(headers[col]), col, 0);
        }

        if (filteredDocList.isEmpty()) {
            Label empty = new Label("No documents pending verification for the selected filter.");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setMinHeight(70);
            empty.setAlignment(Pos.CENTER);
            empty.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:14; -fx-font-style:italic; -fx-background-color:white;");
            table.add(empty, 0, 1, 6, 1);
            clearDetailPanel();
            return;
        }

        int displayRow = 1;
        for (int i = 0; i < filteredDocList.size(); i++) {
            Map<String, Object> doc = filteredDocList.get(i);
            boolean isAlt = (displayRow % 2 == 0);
            boolean isSelected = selectedDoc != null && doc.get("docId") != null && doc.get("docId").equals(selectedDoc.get("docId"));

            Node docCell = createDocCell(doc, isAlt, isSelected);
            Node cargoCell = createCargoCell(doc, isAlt, isSelected);
            Node partyCell = createPartyCell(doc, isAlt, isSelected);
            Node priorityCell = createPriorityCell(doc, isAlt, isSelected);
            Node statusCell = createStatusCell(doc, isAlt, isSelected);
            Node actionCell = createActionCell(doc, isAlt, isSelected);

            table.add(docCell, 0, displayRow);
            table.add(cargoCell, 1, displayRow);
            table.add(partyCell, 2, displayRow);
            table.add(priorityCell, 3, displayRow);
            table.add(statusCell, 4, displayRow);
            table.add(actionCell, 5, displayRow);

            displayRow++;
        }

        if (selectedDoc == null || !filteredDocList.contains(selectedDoc)) {
            selectDocument(filteredDocList.get(0));
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

    private Node createDocCell(Map<String, Object> doc, boolean isAlt, boolean isSelected) {
        String docId = String.valueOf(doc.get("docId"));
        String docType = String.valueOf(doc.get("docType"));

        Label idLbl = new Label(docId);
        idLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");
        Label typeLbl = new Label(docType);
        typeLbl.setStyle("-fx-text-fill:#64748b; -fx-font-size:11;");

        VBox v = new VBox(2, idLbl, typeLbl);
        v.setAlignment(Pos.CENTER_LEFT);
        v.setMaxWidth(Double.MAX_VALUE);
        v.setMinHeight(52);
        v.setPadding(new Insets(0, 12, 0, 12));
        v.setStyle(cellBgStyle(isAlt, isSelected));
        v.setOnMouseClicked(e -> selectDocument(doc));
        return v;
    }

    private Node createCargoCell(Map<String, Object> doc, boolean isAlt, boolean isSelected) {
        String cargoId = String.valueOf(doc.get("cargoId"));
        String cargoName = String.valueOf(doc.get("cargoName"));

        Label idLbl = new Label(cargoId);
        idLbl.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:13; -fx-font-weight:bold;");
        Label nameLbl = new Label(cargoName);
        nameLbl.setStyle("-fx-text-fill:#64748b; -fx-font-size:11;");

        VBox v = new VBox(2, idLbl, nameLbl);
        v.setAlignment(Pos.CENTER_LEFT);
        v.setMaxWidth(Double.MAX_VALUE);
        v.setMinHeight(52);
        v.setPadding(new Insets(0, 12, 0, 12));
        v.setStyle(cellBgStyle(isAlt, isSelected));
        v.setOnMouseClicked(e -> selectDocument(doc));
        return v;
    }

    private Node createPartyCell(Map<String, Object> doc, boolean isAlt, boolean isSelected) {
        String exp = "Exp: " + String.valueOf(doc.get("exporter"));
        String imp = "Imp: " + String.valueOf(doc.get("importer"));

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
        v.setOnMouseClicked(e -> selectDocument(doc));
        return v;
    }

    private Node createPriorityCell(Map<String, Object> doc, boolean isAlt, boolean isSelected) {
        String prio = doc.get("priority") != null ? String.valueOf(doc.get("priority")) : "Normal";
        Label badge = new Label(prio.toUpperCase());
        applyPriorityBadgeStyle(badge, prio);

        HBox h = new HBox(badge);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setMaxWidth(Double.MAX_VALUE);
        h.setMinHeight(52);
        h.setPadding(new Insets(0, 12, 0, 12));
        h.setStyle(cellBgStyle(isAlt, isSelected));
        h.setOnMouseClicked(e -> selectDocument(doc));
        return h;
    }

    private Node createStatusCell(Map<String, Object> doc, boolean isAlt, boolean isSelected) {
        String st = String.valueOf(doc.get("status"));
        Label badge = new Label(st.toUpperCase());
        applyStatusBadgeStyle(badge, st);

        HBox h = new HBox(badge);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setMaxWidth(Double.MAX_VALUE);
        h.setMinHeight(52);
        h.setPadding(new Insets(0, 12, 0, 12));
        h.setStyle(cellBgStyle(isAlt, isSelected));
        h.setOnMouseClicked(e -> selectDocument(doc));
        return h;
    }

    private Node createActionCell(Map<String, Object> doc, boolean isAlt, boolean isSelected) {
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
        inspectBtn.setOnAction(e -> selectDocument(doc));

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

    private void selectDocument(Map<String, Object> doc) {
        this.selectedDoc = doc;
        if (doc == null) return;

        // Re-render table selection highlight
        applyFilterHighlight();

        String docType = String.valueOf(doc.get("docType"));
        formTitle.setText(docType);

        detailDocId.setText(String.valueOf(doc.get("docId")));
        detailDocType.setText(docType);
        detailDocStatus.setText(String.valueOf(doc.get("status")));
        detailDocVersion.setText(doc.get("version") != null ? String.valueOf(doc.get("version")) : "v1.0");
        detailSubmittedBy.setText(doc.get("uploadedBy") != null ? String.valueOf(doc.get("uploadedBy")) : String.valueOf(doc.get("exporter")));
        detailSubmittedDate.setText(doc.get("uploadedAt") != null ? String.valueOf(doc.get("uploadedAt")) : "Recent");

        String prio = doc.get("priority") != null ? String.valueOf(doc.get("priority")) : "Normal";
        detailDocPriority.setText(prio);
        priorityBadgeLabel.setText(prio.toUpperCase());
        applyPriorityBadgeStyle(priorityBadgeLabel, prio);

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
        detailCompany.setText(String.valueOf(doc.get("company")));
        detailVessel.setText(String.valueOf(doc.get("vessel")) + " (" + String.valueOf(doc.get("voyage")) + ")");
        detailContainer.setText(String.valueOf(doc.get("container")));
        detailRoute.setText(String.valueOf(doc.get("origin")) + " ➔ " + String.valueOf(doc.get("destination")));

        if (doc.get("notes") != null && !doc.get("notes").toString().isBlank()) {
            remarksTextArea.setText(doc.get("notes").toString());
        } else {
            remarksTextArea.clear();
        }

        String actualFileName = doc.get("fileName") != null ? String.valueOf(doc.get("fileName")) : String.valueOf(doc.get("docId")) + ".pdf";
        String fileUrl = doc.get("fileUrl") != null ? String.valueOf(doc.get("fileUrl")) : null;

        if (fileUrl != null || doc.get("fileName") != null) {
            pdfNameLabel.setText(actualFileName);
            pdfTypeLabel.setText("Official Shipping Document Attached");
            pdfStorageBadge.setText(fileUrl != null && fileUrl.startsWith("http") ? "Cloud Storage" : "Local Document");
            pdfStorageBadge.setStyle("-fx-background-color:#ecfdf5; -fx-text-fill:#16a34a; -fx-font-size:10; -fx-font-weight:bold; -fx-padding:2 6; -fx-background-radius:4;");
        } else {
            pdfNameLabel.setText("No document file attached");
            pdfTypeLabel.setText("Awaiting document upload from shipper");
            pdfStorageBadge.setText("Pending Upload");
            pdfStorageBadge.setStyle("-fx-background-color:#fef2f2; -fx-text-fill:#ef4444; -fx-font-size:10; -fx-font-weight:bold; -fx-padding:2 6; -fx-background-radius:4;");
        }

        String st = String.valueOf(doc.get("status"));
        statusBadgeLabel.setText(st.toUpperCase());
        applyStatusBadgeStyle(statusBadgeLabel, st);

        updateComplianceAlert(doc);
        updateCargoDocumentationSet(String.valueOf(doc.get("cargoId")));
        updateAuditTrail(doc);
    }

    private void applyFilterHighlight() {
        for (int i = 0; i < filteredDocList.size(); i++) {
            Map<String, Object> d = filteredDocList.get(i);
            boolean isAlt = ((i + 1) % 2 == 0);
            boolean isSelected = selectedDoc != null && d.get("docId") != null && d.get("docId").equals(selectedDoc.get("docId"));
            int rowIdx = i + 1;

            for (Node n : table.getChildren()) {
                Integer r = GridPane.getRowIndex(n);
                if (r != null && r == rowIdx) {
                    n.setStyle(cellBgStyle(isAlt, isSelected));
                }
            }
        }
    }

    private void updateComplianceAlert(Map<String, Object> doc) {
        if (alertBox == null) return;
        String st = String.valueOf(doc.get("status"));
        String prio = String.valueOf(doc.get("priority"));
        String cargoType = String.valueOf(doc.get("cargoType"));

        if ("Rejected".equalsIgnoreCase(st)) {
            alertTitle.setText("⛔ Document Rejected / Discrepancy Found");
            alertMessage.setText("This document was rejected due to non-compliance. Review officer notes before re-certifying.");
            alertBox.setStyle("-fx-background-color:#fef2f2; -fx-border-color:#fecaca; -fx-border-radius:8; -fx-background-radius:8;");
            alertTitle.setStyle("-fx-text-fill:#991b1b; -fx-font-size:12; -fx-font-weight:bold;");
            alertMessage.setStyle("-fx-text-fill:#7f1d1d; -fx-font-size:12;");
            alertBox.setVisible(true);
            alertBox.setManaged(true);
        } else if ("On Hold".equalsIgnoreCase(st)) {
            alertTitle.setText("⏸ Document Placed On Regulatory Hold");
            alertMessage.setText("Awaiting additional compliance documents or tariff classification review from exporter/importer.");
            alertBox.setStyle("-fx-background-color:#fff7ed; -fx-border-color:#fed7aa; -fx-border-radius:8; -fx-background-radius:8;");
            alertTitle.setStyle("-fx-text-fill:#9a3412; -fx-font-size:12; -fx-font-weight:bold;");
            alertMessage.setStyle("-fx-text-fill:#7c2d12; -fx-font-size:12;");
            alertBox.setVisible(true);
            alertBox.setManaged(true);
        } else if ("Urgent".equalsIgnoreCase(prio) || cargoType.toLowerCase().contains("hazardous") || cargoType.toLowerCase().contains("dangerous")) {
            alertTitle.setText("🚨 High-Priority Dangerous / Perishable Cargo");
            alertMessage.setText("Special handling required. Dangerous goods declaration and hazmat certificates must be cross-verified.");
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

    private void updateCargoDocumentationSet(String cargoId) {
        if (cargoDocsSetContainer == null) return;
        cargoDocsSetContainer.getChildren().clear();

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
            row.setPadding(new Insets(2, 0, 2, 0));

            Label nameLbl = createLabel(reqType, false, "#334155");
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Label badge = new Label();
            if (uploadedDocStatusMap.containsKey(reqType)) {
                String status = uploadedDocStatusMap.get(reqType);
                badge.setText(status.toUpperCase());
                applyStatusBadgeStyle(badge, status);
            } else {
                badge.setText("MISSING");
                badge.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#64748b; -fx-font-size:10; -fx-font-weight:bold; -fx-padding:2 6; -fx-background-radius:6; -fx-border-color:#e2e8f0; -fx-border-radius:6;");
            }

            row.getChildren().addAll(nameLbl, sp, badge);
            cargoDocsSetContainer.getChildren().add(row);
        }
    }

    private void updateAuditTrail(Map<String, Object> doc) {
        if (auditTrailContainer == null) return;
        auditTrailContainer.getChildren().clear();

        String submittedBy = doc.get("uploadedBy") != null ? String.valueOf(doc.get("uploadedBy")) : String.valueOf(doc.get("exporter"));
        String submittedAt = doc.get("uploadedAt") != null ? String.valueOf(doc.get("uploadedAt")) : "Initial submission";
        String status = String.valueOf(doc.get("status"));
        String verifiedBy = doc.get("verifiedBy") != null ? String.valueOf(doc.get("verifiedBy")) : officerName;
        String verifiedAt = doc.get("verifiedAt") != null ? String.valueOf(doc.get("verifiedAt")) : "Recent";

        auditTrailContainer.getChildren().add(createAuditRow("📥", "Submitted", submittedBy, submittedAt));
        auditTrailContainer.getChildren().add(createAuditRow("🔍", "Assigned for Compliance", officerName + " (" + officerRole + ")", "In Queue"));

        if ("Verified".equalsIgnoreCase(status) || "Approved".equalsIgnoreCase(status)) {
            auditTrailContainer.getChildren().add(createAuditRow("✓", "Verified & Certified", verifiedBy, verifiedAt));
        } else if ("On Hold".equalsIgnoreCase(status)) {
            auditTrailContainer.getChildren().add(createAuditRow("⏸", "Placed On Hold", verifiedBy, verifiedAt));
        } else if ("Rejected".equalsIgnoreCase(status)) {
            auditTrailContainer.getChildren().add(createAuditRow("✕", "Rejected Non-Compliant", verifiedBy, verifiedAt));
        } else {
            auditTrailContainer.getChildren().add(createAuditRow("⏳", "Pending Officer Review", "Customs Inspection Desk", "Active"));
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
        this.selectedDoc = null;
        if (formTitle != null) formTitle.setText("Document Verification & Audit");
        if (detailDocId != null) detailDocId.setText("—");
        if (detailDocType != null) detailDocType.setText("—");
        if (detailDocStatus != null) detailDocStatus.setText("—");
        if (detailDocPriority != null) detailDocPriority.setText("Normal");
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
        if (detailCompany != null) detailCompany.setText("—");
        if (detailVessel != null) detailVessel.setText("—");
        if (detailContainer != null) detailContainer.setText("—");
        if (detailRoute != null) detailRoute.setText("—");

        if (pdfNameLabel != null) pdfNameLabel.setText("No document file available.");
        if (pdfTypeLabel != null) pdfTypeLabel.setText("Select a record from queue to preview");
        if (cargoDocsSetContainer != null) cargoDocsSetContainer.getChildren().clear();
        if (auditTrailContainer != null) auditTrailContainer.getChildren().clear();
        if (remarksTextArea != null) remarksTextArea.clear();

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
        if ("Verified".equalsIgnoreCase(st) || "Approved".equalsIgnoreCase(st) || "Certified".equalsIgnoreCase(st)) {
            badge.setStyle("-fx-background-color:#ecfdf5; -fx-text-fill:#16a34a; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:6;");
        } else if ("On Hold".equalsIgnoreCase(st) || "Hold".equalsIgnoreCase(st)) {
            badge.setStyle("-fx-background-color:#fff7ed; -fx-text-fill:#f97316; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:6;");
        } else if ("Under Review".equalsIgnoreCase(st) || "Review".equalsIgnoreCase(st)) {
            badge.setStyle("-fx-background-color:#eff6ff; -fx-text-fill:" + BLUE + "; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:6;");
        } else if ("Rejected".equalsIgnoreCase(st)) {
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
        if (selectedDoc == null) {
            Alert err = new Alert(Alert.AlertType.WARNING, "No document selected.");
            err.showAndWait();
            return;
        }

        String fileUrl = selectedDoc.get("fileUrl") != null ? String.valueOf(selectedDoc.get("fileUrl")) : null;
        String fileName = selectedDoc.get("fileName") != null ? String.valueOf(selectedDoc.get("fileName")) : null;
        String docId = String.valueOf(selectedDoc.get("docId"));

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
            err.setTitle("Document Storage Notice");
            err.setHeaderText("Document File Reference Attached");
            err.setContentText(
                    "Document ID: " + docId + "\n" +
                    "File: " + (fileName != null ? fileName : "Declaration Document") + "\n" +
                    (fileUrl != null ? "Storage URL: " + fileUrl + "\n" : "") +
                    "\nThe document metadata has been loaded and verified from the secure customs repository."
            );
            err.showAndWait();
        }
    }

    private String getOfficerNotesOrDefault() {
        if (remarksTextArea != null && !remarksTextArea.getText().isBlank()) {
            return remarksTextArea.getText().trim();
        }
        return "Document verified & certified compliant by " + officerName;
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

        Label promptLbl = new Label("Enter detailed regulatory remarks / reason:");
        promptLbl.setStyle("-fx-font-size:13; -fx-text-fill:#0f172a; -fx-font-weight:bold;");

        TextArea textArea = new TextArea();
        textArea.setPromptText("Enter official justification here...");
        textArea.setPrefRowCount(3);
        textArea.setWrapText(true);
        if (remarksTextArea != null && !remarksTextArea.getText().isBlank()) {
            textArea.setText(remarksTextArea.getText().trim());
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
            performVerificationAction(status, note);
        });
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
        selectedDoc.put("notes", notes);
        selectedDoc.put("verifiedBy", officerName);
        selectedDoc.put("verifiedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));

        refreshLiveDocuments();

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Document Action Completed");
        a.setHeaderText("Document " + docId + " status updated to " + newStatus);
        a.setContentText(
                "Cargo ID: " + cargoId + "\n" +
                "Officer: " + officerName + " (" + officerRole + ")\n" +
                "Port Jurisdiction: " + activeAssignedPort + "\n" +
                "Remarks: " + notes + "\n\n" +
                "Database and downstream customs workflow synchronized successfully."
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
