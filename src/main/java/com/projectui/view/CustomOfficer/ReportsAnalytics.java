package com.projectui.view.CustomOfficer;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.projectui.dao.CargoDao;
import com.projectui.dao.UserDao;
import com.projectui.view.login.LoginPage;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Custom Officer - Reports & Analytics Center
 * 
 * Styled using the EXACT Admin Reports & Analytics Page Design System:
 * - Single source of truth with DocumentVerification.java & CargoInspection.java
 * - Clean, professional visual analytics cards (No placeholder/red charts)
 * - Customs Clearance Pipeline, Attention Required & Risk Overview panels
 * - 100% dynamic calculations from live CargoDao & Firestore records
 * - Authentic Apache PDFBox PDF Export
 */
@SuppressWarnings({"unchecked", "unused"})
public class ReportsAnalytics {

    private static final String BLUE = "#0b63ff";
    private static final String CARD_STYLE = "-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:10; -fx-background-radius:10; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 8, 0, 0, 2);";

    private final VBox content = new VBox(16);
    private final CargoDao cargoDao = new CargoDao();

    // Officer Context
    private String activeAssignedPort = "Cochin Port";
    private String officerName = "Customs Officer";
    private String officerEmail = "customs@docktech.com";
    private String officerRole = "Custom Officer";

    // Accordion Expansion State Trackers
    private final boolean[] docReportExpanded = new boolean[]{true};
    private final boolean[] inspectionReportExpanded = new boolean[]{true};
    private final boolean[] complianceSummaryExpanded = new boolean[]{true};

    // Header & Telemetry
    private Label liveStatusIndicator;
    private Label lastUpdateLabel;
    private Label jurisdictionBadge;
    private HBox toastBanner;
    private Label toastMessage;

    // Top Summary Metric Cards Containers
    private final HBox liveKpiSummaryRow = new HBox(12);

    // Filter Controls
    private TextField searchField;
    private ComboBox<String> statusFilterCombo;
    private ComboBox<String> priorityFilterCombo;
    private ComboBox<String> typeFilterCombo;
    private ComboBox<String> entityFilterCombo;
    private ComboBox<String> portFilterCombo;
    private ComboBox<String> dateRangeCombo;

    // Visual Analytics Panels
    private final VBox docStatusPanel = new VBox(10);
    private final VBox inspectionStatusPanel = new VBox(10);
    private final VBox pipelineCard = new VBox(12);
    private final VBox attentionRequiredCard = new VBox(10);

    // Collapsible Sections Containers
    private final VBox docReportCard = new VBox(8);
    private final VBox inspectionReportCard = new VBox(8);
    private final VBox complianceSummaryCard = new VBox(8);

    // Master & Filtered Records (Exact same models as DocumentVerification and CargoInspection)
    private final List<Map<String, Object>> masterDocList = new ArrayList<>();
    private final List<Map<String, Object>> filteredDocList = new ArrayList<>();

    private final List<Map<String, Object>> masterCargoList = new ArrayList<>();
    private final List<Map<String, Object>> filteredCargoList = new ArrayList<>();

    public ReportsAnalytics() {
        resolveOfficerContext();
        buildUI();
        refreshAnalyticsData();

        // Listen for live Firestore updates
        CargoDao.addChangeListener(this::refreshAnalyticsData);
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
        content.setSpacing(16);
        content.setPadding(new Insets(18, 22, 28, 22));
        content.setStyle("-fx-background-color:transparent;");

        // Toast Status Banner
        toastBanner = createToastBanner();

        // 1. Header Section (Admin Style)
        HBox header = createHeaderSection();

        // 2. Telemetry / Live Status Bar
        HBox liveStatusBar = createLiveStatusBar();

        // 3. Filters Card (Admin Filter Styling)
        VBox filterCard = createFiltersCard();

        // 4. Customs Clearance Pipeline Visualization
        buildClearancePipelineShell();

        // 5. Analytics Sections (Side-by-Side Professional Cards)
        HBox analyticsRow = createAnalyticsRow();

        content.getChildren().addAll(
                header,
                liveStatusBar,
                toastBanner,
                liveKpiSummaryRow,
                filterCard,
                pipelineCard,
                analyticsRow,
                attentionRequiredCard,
                docReportCard,
                inspectionReportCard,
                complianceSummaryCard
        );
    }

    // ==========================================
    // 1. PAGE HEADER (ADMIN THEME)
    // ==========================================
    private HBox createHeaderSection() {
        Label title = new Label("Reports & Analytics");
        title.setStyle("-fx-text-fill:#05070c; -fx-font-size:26; -fx-font-weight:bold;");

        Label subtitle = new Label("Customs compliance analytics, declaration audit trails, and cargo clearance velocity.");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:14;");

        VBox titleBox = new VBox(3, title, subtitle);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Button exportBtn = new Button("📥  Export Compliance Report");
        exportBtn.setStyle(
                "-fx-background-color:" + BLUE + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:8 18;" +
                "-fx-background-radius:8;" +
                "-fx-cursor:hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(11,99,255,0.2), 6, 0, 0, 2);"
        );
        exportBtn.setOnAction(e -> exportComplianceReportPdf());

        HBox header = new HBox(12, titleBox, space, exportBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    // ==========================================
    // 2. LIVE STATUS TELEMETRY BAR
    // ==========================================
    private HBox createLiveStatusBar() {
        liveStatusIndicator = new Label("● LIVE CUSTOMS DATABASE");
        liveStatusIndicator.setStyle("-fx-text-fill:#16a34a; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-color:#ecfdf5; -fx-background-radius:10; -fx-border-color:#bbf7d0; -fx-border-radius:10;");

        jurisdictionBadge = new Label("Jurisdiction: " + activeAssignedPort);
        jurisdictionBadge.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:12; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-color:#eff6ff; -fx-background-radius:10; -fx-border-color:#dbeafe; -fx-border-radius:10;");

        lastUpdateLabel = new Label("Last synchronized: " + nowTime());
        lastUpdateLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        HBox bar = new HBox(10, liveStatusIndicator, jurisdictionBadge, lastUpdateLabel);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    // ==========================================
    // 3. TOAST NOTIFICATION BANNER
    // ==========================================
    private HBox createToastBanner() {
        Label icon = new Label("ℹ");
        icon.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:16; -fx-font-weight:bold;");

        toastMessage = new Label("Showing live customs audit and compliance analytics across " + activeAssignedPort + ".");
        toastMessage.setStyle("-fx-text-fill:#334155; -fx-font-size:13; -fx-font-weight:bold;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label close = new Label("✕");
        close.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13; -fx-cursor:hand;");
        close.setOnMouseClicked(e -> toastBanner.setVisible(false));

        HBox toast = new HBox(10, icon, toastMessage, space, close);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setPadding(new Insets(10, 16, 10, 16));
        toast.setStyle("-fx-background-color:#eff6ff; -fx-border-color:#bfdbfe; -fx-border-radius:8; -fx-background-radius:8;");
        return toast;
    }

    private void showToast(String msg, boolean isSuccess) {
        if (toastMessage != null && toastBanner != null) {
            toastMessage.setText(msg);
            toastBanner.setStyle(
                    "-fx-background-color:" + (isSuccess ? "#ecfdf5" : "#eff6ff") + ";" +
                    "-fx-border-color:" + (isSuccess ? "#a7f3d0" : "#bfdbfe") + ";" +
                    "-fx-border-radius:8;" +
                    "-fx-background-radius:8;"
            );
            toastBanner.setVisible(true);
            toastBanner.setManaged(true);
        }
    }

    // ==========================================
    // 4. FILTER CONTROLS BAR (ADMIN STYLE)
    // ==========================================
    private VBox createFiltersCard() {
        Label filterTitle = new Label("Customs Analytics & Report Filters");
        filterTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14; -fx-font-weight:bold;");

        searchField = new TextField();
        searchField.setPromptText("⌕  Search Document ID, Cargo ID, Importer, Exporter, Vessel...");
        searchField.setPrefWidth(220);
        styleAdminField(searchField);
        searchField.textProperty().addListener((obs, o, n) -> applyFilterAndRefreshViews());

        statusFilterCombo = combo("All Statuses", "All Statuses", "Pending Verification", "Under Review", "Verified", "Cleared", "On Hold", "Rejected", "Failed");
        statusFilterCombo.setPrefWidth(130);
        statusFilterCombo.setOnAction(e -> applyFilterAndRefreshViews());

        priorityFilterCombo = combo("All Priorities", "All Priorities", "Urgent", "High", "Normal");
        priorityFilterCombo.setPrefWidth(115);
        priorityFilterCombo.setOnAction(e -> applyFilterAndRefreshViews());

        typeFilterCombo = combo("All Types", "All Types", "Containerized", "Dry Bulk", "Liquid Bulk", "Breakbulk", "Ro-Ro", "Hazardous", "Perishable", "Electronics", "Machinery", "Textiles");
        typeFilterCombo.setPrefWidth(120);
        typeFilterCombo.setOnAction(e -> applyFilterAndRefreshViews());

        entityFilterCombo = new ComboBox<>();
        entityFilterCombo.getItems().add("All Exporters / Importers");
        entityFilterCombo.setValue("All Exporters / Importers");
        entityFilterCombo.setPrefWidth(145);
        styleAdminCombo(entityFilterCombo);
        entityFilterCombo.setOnAction(e -> applyFilterAndRefreshViews());

        List<String> dynamicPortOptions = new ArrayList<>();
        dynamicPortOptions.add("All Ports");
        dynamicPortOptions.addAll(com.projectui.view.admin.AdminDataStore.getInstance().getApprovedPortNames());
        portFilterCombo = combo("All Ports", dynamicPortOptions.toArray(new String[0]));
        portFilterCombo.setPrefWidth(125);
        portFilterCombo.setOnAction(e -> applyFilterAndRefreshViews());

        dateRangeCombo = combo("All Active Records", "All Active Records", "Current Shift (Today)", "This Week", "This Month");
        dateRangeCombo.setPrefWidth(135);
        dateRangeCombo.setOnAction(e -> applyFilterAndRefreshViews());

        Button refreshBtn = new Button("↻ Refresh Data");
        refreshBtn.setStyle(
                "-fx-background-color:#f1f5f9;" +
                "-fx-text-fill:#475569;" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:8 14;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );
        refreshBtn.setOnAction(e -> refreshAnalyticsData());

        HBox controlsRow = new HBox(8, searchField, statusFilterCombo, priorityFilterCombo, typeFilterCombo, entityFilterCombo, portFilterCombo, dateRangeCombo, refreshBtn);
        controlsRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        VBox card = new VBox(10, filterTitle, controlsRow);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle(CARD_STYLE);
        return card;
    }

    // ==========================================
    // 5. CLEARANCE PIPELINE & ANALYTICS PANELS
    // ==========================================
    private void buildClearancePipelineShell() {
        pipelineCard.setPadding(new Insets(16, 18, 16, 18));
        pipelineCard.setStyle(CARD_STYLE);

        attentionRequiredCard.setPadding(new Insets(16, 18, 16, 18));
        attentionRequiredCard.setStyle(CARD_STYLE);
    }

    private HBox createAnalyticsRow() {
        docStatusPanel.setPadding(new Insets(16, 18, 16, 18));
        docStatusPanel.setStyle(CARD_STYLE);
        HBox.setHgrow(docStatusPanel, Priority.ALWAYS);

        inspectionStatusPanel.setPadding(new Insets(16, 18, 16, 18));
        inspectionStatusPanel.setStyle(CARD_STYLE);
        HBox.setHgrow(inspectionStatusPanel, Priority.ALWAYS);

        HBox row = new HBox(16, docStatusPanel, inspectionStatusPanel);
        return row;
    }

    // ==========================================
    // 6. DATA SYNC & SINGLE SOURCE OF TRUTH
    // ==========================================
    private void refreshAnalyticsData() {
        Platform.runLater(() -> {
            resolveOfficerContext();
            if (jurisdictionBadge != null) {
                jurisdictionBadge.setText("Jurisdiction: " + activeAssignedPort);
            }
            if (lastUpdateLabel != null) {
                lastUpdateLabel.setText("Last synchronized: " + nowTime());
            }

            masterDocList.clear();
            masterCargoList.clear();
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
                    String createdAt = c.get("createdAt") != null ? String.valueOf(c.get("createdAt")) : "Today";
                    String updatedAt = c.get("updatedAt") != null ? String.valueOf(c.get("updatedAt")) : "Recent";
                    String verifiedBy = c.get("verifiedBy") != null ? String.valueOf(c.get("verifiedBy")) : officerName;
                    String inspectedBy = c.get("inspectedBy") != null ? String.valueOf(c.get("inspectedBy")) : officerName;

                    if (!exporter.isBlank() && !exporter.equals("—")) uniqueEntities.add(exporter);
                    if (!importer.isBlank() && !importer.equals("—")) uniqueEntities.add(importer);

                    String cargoPriority = "Normal";
                    if (c.get("priority") != null && !c.get("priority").toString().isBlank()) {
                        cargoPriority = c.get("priority").toString();
                    } else if (cargoType.toLowerCase().contains("hazardous") || cargoType.toLowerCase().contains("dangerous") || cargoType.toLowerCase().contains("perishable") || cargoName.toLowerCase().contains("chemical") || cargoName.toLowerCase().contains("pharma")) {
                        cargoPriority = "Urgent";
                    }

                    // Cargo Inspection Record Model (Exact same as CargoInspection.java)
                    Map<String, Object> cargoItem = new HashMap<>(c);
                    cargoItem.put("cargoId", cargoId);
                    cargoItem.put("cargoName", cargoName);
                    cargoItem.put("cargoType", cargoType);
                    cargoItem.put("hsCode", hsCode);
                    cargoItem.put("description", desc);
                    cargoItem.put("vessel", vessel);
                    cargoItem.put("voyage", voyage);
                    cargoItem.put("exporter", exporter);
                    cargoItem.put("importer", importer);
                    cargoItem.put("company", company);
                    cargoItem.put("origin", origin);
                    cargoItem.put("destination", dest);
                    cargoItem.put("container", container);
                    cargoItem.put("quantity", qty);
                    cargoItem.put("unit", unit);
                    cargoItem.put("packageCount", pkgCount);
                    cargoItem.put("packageType", pkgType);
                    cargoItem.put("grossWeight", grossWeight);
                    cargoItem.put("netWeight", netWeight);
                    cargoItem.put("eta", eta);
                    cargoItem.put("etd", etd);
                    cargoItem.put("priority", cargoPriority);
                    cargoItem.put("inspectionStatus", inspectStatus);
                    cargoItem.put("customsStatus", customsStatus);
                    cargoItem.put("createdAt", createdAt);
                    cargoItem.put("updatedAt", updatedAt);
                    cargoItem.put("inspectedBy", inspectedBy);
                    cargoItem.put("docId", c.get("docId") != null ? c.get("docId") : "DOC-" + cargoId + "-01");
                    masterCargoList.add(cargoItem);

                    // Document Verification Record Model (Exact same as DocumentVerification.java)
                    List<Map<String, Object>> docs = extractDocumentsFromCargo(c);
                    if (docs != null && !docs.isEmpty()) {
                        for (Map<String, Object> d : docs) {
                            Map<String, Object> docItem = new HashMap<>(d);
                            docItem.put("cargoId", cargoId);
                            docItem.put("cargoName", cargoName);
                            docItem.put("cargoType", cargoType);
                            docItem.put("hsCode", hsCode);
                            docItem.put("description", desc);
                            docItem.put("vessel", vessel);
                            docItem.put("voyage", voyage);
                            docItem.put("exporter", exporter);
                            docItem.put("importer", importer);
                            docItem.put("company", company);
                            docItem.put("origin", origin);
                            docItem.put("destination", dest);
                            docItem.put("container", container);
                            docItem.put("quantity", qty);
                            docItem.put("unit", unit);
                            docItem.put("packageCount", pkgCount);
                            docItem.put("packageType", pkgType);
                            docItem.put("grossWeight", grossWeight);
                            docItem.put("netWeight", netWeight);
                            docItem.put("eta", eta);
                            docItem.put("etd", etd);
                            docItem.put("verifiedBy", verifiedBy);

                            if (!docItem.containsKey("priority") || docItem.get("priority") == null) {
                                docItem.put("priority", cargoPriority);
                            }

                            masterDocList.add(docItem);
                        }
                    }
                }
            }

            // Populate Entity Filter Combo
            if (entityFilterCombo != null) {
                String curr = entityFilterCombo.getValue();
                entityFilterCombo.getItems().clear();
                entityFilterCombo.getItems().add("All Exporters / Importers");
                List<String> sorted = new ArrayList<>(uniqueEntities);
                Collections.sort(sorted);
                entityFilterCombo.getItems().addAll(sorted);
                if (curr != null && (curr.equals("All Exporters / Importers") || uniqueEntities.contains(curr))) {
                    entityFilterCombo.setValue(curr);
                } else {
                    entityFilterCombo.setValue("All Exporters / Importers");
                }
            }

            applyFilterAndRefreshViews();
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
            if (cargo.get("fileUrl") != null || cargo.get("documentUrl") != null || cargo.get("fileName") != null) {
                Map<String, Object> d = new HashMap<>();
                d.put("docId", "DOC-" + cargoId + "-01");
                d.put("docType", cargo.get("docType") != null ? cargo.get("docType") : "Customs Declaration");
                d.put("fileName", cargo.get("fileName") != null ? cargo.get("fileName") : cargoId + "_Declaration.pdf");
                d.put("fileUrl", cargo.get("fileUrl") != null ? cargo.get("fileUrl") : cargo.get("documentUrl"));
                d.put("status", cargo.get("verificationStatus") != null ? cargo.get("verificationStatus") : "Pending");
                d.put("uploadedAt", cargo.get("createdAt") != null ? cargo.get("createdAt") : "Recent");
                d.put("uploadedBy", cargo.get("exporter") != null ? cargo.get("exporter") : "Shipper");
                d.put("priority", cargo.get("priority") != null ? cargo.get("priority") : "Normal");
                result.add(d);
            }
        }
        return result;
    }

    private void applyFilterAndRefreshViews() {
        filteredDocList.clear();
        filteredCargoList.clear();

        String query = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String statusVal = statusFilterCombo != null ? statusFilterCombo.getValue() : "All Statuses";
        String priorityVal = priorityFilterCombo != null ? priorityFilterCombo.getValue() : "All Priorities";
        String typeVal = typeFilterCombo != null ? typeFilterCombo.getValue() : "All Types";
        String entityVal = entityFilterCombo != null ? entityFilterCombo.getValue() : "All Exporters / Importers";
        String portVal = portFilterCombo != null ? portFilterCombo.getValue() : "All Ports";

        // Filter Document Declarations
        for (Map<String, Object> d : masterDocList) {
            String docId = String.valueOf(d.get("docId"));
            String cargoId = String.valueOf(d.get("cargoId"));
            String cargoName = String.valueOf(d.get("cargoName"));
            String exp = String.valueOf(d.get("exporter"));
            String imp = String.valueOf(d.get("importer"));
            String vessel = String.valueOf(d.get("vessel"));
            String type = String.valueOf(d.get("cargoType"));
            String origin = String.valueOf(d.get("origin"));
            String dest = String.valueOf(d.get("destination"));
            String status = String.valueOf(d.get("status"));
            String prio = String.valueOf(d.get("priority"));

            boolean matchQuery = query.isEmpty() ||
                    docId.toLowerCase().contains(query) ||
                    cargoId.toLowerCase().contains(query) ||
                    cargoName.toLowerCase().contains(query) ||
                    exp.toLowerCase().contains(query) ||
                    imp.toLowerCase().contains(query) ||
                    vessel.toLowerCase().contains(query);

            boolean matchStatus = "All Statuses".equalsIgnoreCase(statusVal) || status.equalsIgnoreCase(statusVal);
            boolean matchPriority = "All Priorities".equalsIgnoreCase(priorityVal) || prio.equalsIgnoreCase(priorityVal);
            boolean matchType = "All Types".equalsIgnoreCase(typeVal) || type.toLowerCase().contains(typeVal.toLowerCase()) || cargoName.toLowerCase().contains(typeVal.toLowerCase());
            boolean matchEntity = "All Exporters / Importers".equalsIgnoreCase(entityVal) || exp.equalsIgnoreCase(entityVal) || imp.equalsIgnoreCase(entityVal);
            boolean matchPort = "All Ports".equalsIgnoreCase(portVal) || origin.toLowerCase().contains(portVal.toLowerCase()) || dest.toLowerCase().contains(portVal.toLowerCase());

            if (matchQuery && matchStatus && matchPriority && matchType && matchEntity && matchPort) {
                filteredDocList.add(d);
            }
        }

        // Filter Cargo Inspections
        for (Map<String, Object> c : masterCargoList) {
            String cargoId = String.valueOf(c.get("cargoId"));
            String cargoName = String.valueOf(c.get("cargoName"));
            String exp = String.valueOf(c.get("exporter"));
            String imp = String.valueOf(c.get("importer"));
            String vessel = String.valueOf(c.get("vessel"));
            String type = String.valueOf(c.get("cargoType"));
            String origin = String.valueOf(c.get("origin"));
            String dest = String.valueOf(c.get("destination"));
            String inspect = String.valueOf(c.get("inspectionStatus"));
            String customs = String.valueOf(c.get("customsStatus"));
            String prio = String.valueOf(c.get("priority"));

            boolean matchQuery = query.isEmpty() ||
                    cargoId.toLowerCase().contains(query) ||
                    cargoName.toLowerCase().contains(query) ||
                    exp.toLowerCase().contains(query) ||
                    imp.toLowerCase().contains(query) ||
                    vessel.toLowerCase().contains(query);

            boolean matchStatus = "All Statuses".equalsIgnoreCase(statusVal) ||
                    inspect.equalsIgnoreCase(statusVal) ||
                    customs.equalsIgnoreCase(statusVal);

            boolean matchPriority = "All Priorities".equalsIgnoreCase(priorityVal) || prio.equalsIgnoreCase(priorityVal);
            boolean matchType = "All Types".equalsIgnoreCase(typeVal) || type.toLowerCase().contains(typeVal.toLowerCase()) || cargoName.toLowerCase().contains(typeVal.toLowerCase());
            boolean matchEntity = "All Exporters / Importers".equalsIgnoreCase(entityVal) || exp.equalsIgnoreCase(entityVal) || imp.equalsIgnoreCase(entityVal);
            boolean matchPort = "All Ports".equalsIgnoreCase(portVal) || origin.toLowerCase().contains(portVal.toLowerCase()) || dest.toLowerCase().contains(portVal.toLowerCase());

            if (matchQuery && matchStatus && matchPriority && matchType && matchEntity && matchPort) {
                filteredCargoList.add(c);
            }
        }

        // Rebuild All UI Components with Consistent Data
        buildLiveSummaryKpis();
        buildClearancePipelineContent();
        buildDocumentStatusAnalytics();
        buildCargoInspectionStatusAnalytics();
        buildAttentionRequiredSection();
        buildDocumentVerificationReport();
        buildCargoInspectionReport();
        buildComplianceSummaryReport();
    }

    // ==========================================
    // 7. COMPACT SUMMARY KPI CARDS (SINGLE SOURCE OF TRUTH)
    // ==========================================
    private void buildLiveSummaryKpis() {
        liveKpiSummaryRow.getChildren().clear();

        // 1. Document counts (Exact match with DocumentVerification.java)
        int totalDocs = filteredDocList.size();
        int pendingDocs = 0;
        int verifiedDocs = 0;
        for (Map<String, Object> d : filteredDocList) {
            String st = String.valueOf(d.get("status"));
            if ("Verified".equalsIgnoreCase(st) || "Approved".equalsIgnoreCase(st) || "Certified".equalsIgnoreCase(st)) {
                verifiedDocs++;
            } else if ("On Hold".equalsIgnoreCase(st) || "Hold".equalsIgnoreCase(st) || "Under Review".equalsIgnoreCase(st) || "Review".equalsIgnoreCase(st) || "Rejected".equalsIgnoreCase(st)) {
                // Not pending
            } else {
                pendingDocs++;
            }
        }

        // 2. Cargo counts (Exact match with CargoInspection.java)
        int totalInspections = filteredCargoList.size();
        int passedInspections = 0;
        int clearedCargo = 0;
        int heldCargo = 0;

        for (Map<String, Object> c : filteredCargoList) {
            String inspect = String.valueOf(c.get("inspectionStatus"));
            String customs = String.valueOf(c.get("customsStatus"));

            if ("Passed".equalsIgnoreCase(inspect) || "Approved".equalsIgnoreCase(inspect) || "Cleared".equalsIgnoreCase(inspect)) {
                passedInspections++;
            }
            if ("Cleared".equalsIgnoreCase(customs) || "Approved".equalsIgnoreCase(customs)) {
                clearedCargo++;
            }
            if ("On Hold".equalsIgnoreCase(inspect) || "Hold".equalsIgnoreCase(inspect) || "Held".equalsIgnoreCase(customs) || "Failed".equalsIgnoreCase(inspect) || "Rejected".equalsIgnoreCase(inspect)) {
                heldCargo++;
            }
        }

        VBox c1 = compactSummaryCard("Total Declarations", String.valueOf(totalDocs), "Audited Submissions", "📋", "#0b63ff", "#eff6ff");
        VBox c2 = compactSummaryCard("Pending Verification", String.valueOf(pendingDocs), "Awaiting Officer Audit", "⏳", "#f59e0b", "#fffbeb");
        VBox c3 = compactSummaryCard("Verified & Certified", String.valueOf(verifiedDocs), "Certified Submissions", "✓", "#16a34a", "#ecfdf5");
        VBox c4 = compactSummaryCard("Physical Inspections", String.valueOf(totalInspections), "Inspection Registry", "🔍", "#0284c7", "#f0f9ff");
        VBox c5 = compactSummaryCard("Cleared for Release", String.valueOf(clearedCargo), "Terminal Handed Off", "🚢", "#10b981", "#ecfdf5");
        VBox c6 = compactSummaryCard("Held / Non-Compliant", String.valueOf(heldCargo), "Quarantined / Discrepancy", "⛔", "#ef4444", "#fef2f2");

        liveKpiSummaryRow.getChildren().addAll(c1, c2, c3, c4, c5, c6);
        for (Node node : liveKpiSummaryRow.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
            if (node instanceof VBox) {
                ((VBox) node).setMaxWidth(Double.MAX_VALUE);
            }
        }
    }

    private VBox compactSummaryCard(String title, String value, String subtitle, String iconStr, String accentColor, String bgLight) {
        Label icon = new Label(iconStr);
        icon.setStyle(
                "-fx-text-fill:" + accentColor + ";" +
                "-fx-font-size:16;" +
                "-fx-font-weight:bold;" +
                "-fx-alignment:center;" +
                "-fx-min-width:32;" +
                "-fx-min-height:32;" +
                "-fx-background-color:" + bgLight + ";" +
                "-fx-background-radius:6;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:13; -fx-font-weight:bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(8, icon, titleLabel, spacer);
        top.setAlignment(Pos.CENTER_LEFT);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:28; -fx-font-weight:800;");

        Label subLabel = new Label(subtitle);
        subLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12;");

        VBox card = new VBox(6, top, valueLabel, subLabel);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle(CARD_STYLE);
        return card;
    }

    // ==========================================
    // 8. CUSTOMS CLEARANCE PIPELINE VISUALIZATION
    // ==========================================
    private void buildClearancePipelineContent() {
        pipelineCard.getChildren().clear();

        Label pipeTitle = new Label("Customs Clearance Pipeline & Operational Flow");
        pipeTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16; -fx-font-weight:bold;");

        Label pipeSub = new Label("End-to-end regulatory throughput from document submission to terminal gate release");
        pipeSub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        int stage1 = filteredDocList.size(); // Submitted Documents
        int stage2 = (int) filteredDocList.stream().filter(d -> {
            String st = String.valueOf(d.get("status"));
            return "Verified".equalsIgnoreCase(st) || "Certified".equalsIgnoreCase(st);
        }).count();
        int stage3 = (int) filteredCargoList.stream().filter(c -> {
            String st = String.valueOf(c.get("inspectionStatus"));
            return "Passed".equalsIgnoreCase(st) || "Under Inspection".equalsIgnoreCase(st);
        }).count();
        int stage4 = (int) filteredCargoList.stream().filter(c -> {
            String st = String.valueOf(c.get("inspectionStatus"));
            return "Passed".equalsIgnoreCase(st);
        }).count();
        int stage5 = (int) filteredCargoList.stream().filter(c -> {
            String cs = String.valueOf(c.get("customsStatus"));
            return "Cleared".equalsIgnoreCase(cs) || "Approved".equalsIgnoreCase(cs);
        }).count();

        HBox stepsBox = new HBox(8,
                pipelineStep("1. Document Submitted", String.valueOf(stage1), "Declarations Received", "📥", "#0b63ff", "#eff6ff"),
                pipelineArrow(),
                pipelineStep("2. Document Verified", String.valueOf(stage2), "Certified Compliant", "▣", "#16a34a", "#ecfdf5"),
                pipelineArrow(),
                pipelineStep("3. Cargo Inspected", String.valueOf(stage3), "Physical Examination", "🔍", "#0284c7", "#f0f9ff"),
                pipelineArrow(),
                pipelineStep("4. Compliance Passed", String.valueOf(stage4), "Seal & Audit Passed", "🛡", "#8b5cf6", "#f5f3ff"),
                pipelineArrow(),
                pipelineStep("5. Terminal Released", String.valueOf(stage5), "Handed to Port Auth", "🚢", "#10b981", "#ecfdf5")
        );
        stepsBox.setAlignment(Pos.CENTER_LEFT);
        for (Node n : stepsBox.getChildren()) {
            if (n instanceof VBox) {
                HBox.setHgrow(n, Priority.ALWAYS);
                ((VBox) n).setMaxWidth(Double.MAX_VALUE);
            }
        }

        pipelineCard.getChildren().addAll(new VBox(2, pipeTitle, pipeSub), new Separator(), stepsBox);
    }

    private VBox pipelineStep(String stepTitle, String count, String subtitle, String iconStr, String color, String bg) {
        Label icon = new Label(iconStr);
        icon.setStyle("-fx-text-fill:" + color + "; -fx-font-size:14; -fx-font-weight:bold;");

        Label title = new Label(stepTitle);
        title.setStyle("-fx-text-fill:#475569; -fx-font-size:12; -fx-font-weight:bold;");

        HBox top = new HBox(6, icon, title);
        top.setAlignment(Pos.CENTER_LEFT);

        Label cnt = new Label(count);
        cnt.setStyle("-fx-text-fill:#0f172a; -fx-font-size:20; -fx-font-weight:800;");

        Label sub = new Label(subtitle);
        sub.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:11;");

        VBox box = new VBox(4, top, cnt, sub);
        box.setPadding(new Insets(10, 12, 10, 12));
        box.setStyle("-fx-background-color:" + bg + "; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");
        return box;
    }

    private Label pipelineArrow() {
        Label a = new Label("➔");
        a.setStyle("-fx-text-fill:#cbd5e1; -fx-font-size:14; -fx-font-weight:bold;");
        return a;
    }

    // ==========================================
    // 9. PROFESSIONAL STATUS & RISK ANALYTICS
    // ==========================================
    private void buildDocumentStatusAnalytics() {
        docStatusPanel.getChildren().clear();

        Label title = new Label("Document Verification Status & Throughput");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16; -fx-font-weight:bold;");

        Label subtitle = new Label("Live breakdown of all submitted import/export declaration documents");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        int total = filteredDocList.size();
        int pending = 0, underReview = 0, verified = 0, onHold = 0, rejected = 0;

        for (Map<String, Object> d : filteredDocList) {
            String st = String.valueOf(d.get("status"));
            if ("Verified".equalsIgnoreCase(st) || "Certified".equalsIgnoreCase(st) || "Approved".equalsIgnoreCase(st)) verified++;
            else if ("Under Review".equalsIgnoreCase(st) || "Review".equalsIgnoreCase(st)) underReview++;
            else if ("On Hold".equalsIgnoreCase(st) || "Hold".equalsIgnoreCase(st)) onHold++;
            else if ("Rejected".equalsIgnoreCase(st)) rejected++;
            else pending++;
        }

        if (total == 0) {
            Label empty = new Label("No documents currently require verification.");
            empty.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13; -fx-font-style:italic; -fx-padding:30 0;");
            empty.setAlignment(Pos.CENTER);
            empty.setMaxWidth(Double.MAX_VALUE);
            docStatusPanel.getChildren().addAll(new VBox(2, title, subtitle), new Separator(), empty);
            return;
        }

        VBox rows = new VBox(8);
        rows.getChildren().add(createStatusProgressBar("Pending Verification", pending, total, "#f59e0b", "#fef3c7"));
        rows.getChildren().add(createStatusProgressBar("Under Review", underReview, total, BLUE, "#eff6ff"));
        rows.getChildren().add(createStatusProgressBar("Verified & Certified", verified, total, "#16a34a", "#ecfdf5"));
        rows.getChildren().add(createStatusProgressBar("On Hold", onHold, total, "#f97316", "#fff7ed"));
        rows.getChildren().add(createStatusProgressBar("Rejected", rejected, total, "#ef4444", "#fef2f2"));

        docStatusPanel.getChildren().addAll(new VBox(2, title, subtitle), new Separator(), rows);
    }

    private void buildCargoInspectionStatusAnalytics() {
        inspectionStatusPanel.getChildren().clear();

        Label title = new Label("Cargo Inspection Status & Priority / Risk Overview");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16; -fx-font-weight:bold;");

        Label subtitle = new Label("Physical container examination status and hazard risk classification");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        int total = filteredCargoList.size();
        int pending = 0, underInspect = 0, passed = 0, failed = 0, held = 0;
        int urgentRisk = 0, highRisk = 0, normalRisk = 0;

        for (Map<String, Object> c : filteredCargoList) {
            String inspect = String.valueOf(c.get("inspectionStatus"));
            String prio = String.valueOf(c.get("priority"));

            if ("Passed".equalsIgnoreCase(inspect) || "Approved".equalsIgnoreCase(inspect) || "Cleared".equalsIgnoreCase(inspect)) passed++;
            else if ("Under Inspection".equalsIgnoreCase(inspect) || "Review".equalsIgnoreCase(inspect)) underInspect++;
            else if ("Failed".equalsIgnoreCase(inspect) || "Rejected".equalsIgnoreCase(inspect)) failed++;
            else if ("On Hold".equalsIgnoreCase(inspect) || "Hold".equalsIgnoreCase(inspect)) held++;
            else pending++;

            if ("Urgent".equalsIgnoreCase(prio)) urgentRisk++;
            else if ("High".equalsIgnoreCase(prio)) highRisk++;
            else normalRisk++;
        }

        if (total == 0) {
            Label empty = new Label("No cargo inspection records available.");
            empty.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13; -fx-font-style:italic; -fx-padding:30 0;");
            empty.setAlignment(Pos.CENTER);
            empty.setMaxWidth(Double.MAX_VALUE);
            inspectionStatusPanel.getChildren().addAll(new VBox(2, title, subtitle), new Separator(), empty);
            return;
        }

        VBox rows = new VBox(8);
        rows.getChildren().add(createStatusProgressBar("Passed / Approved", passed, total, "#16a34a", "#ecfdf5"));
        rows.getChildren().add(createStatusProgressBar("Under Physical Examination", underInspect, total, BLUE, "#eff6ff"));
        rows.getChildren().add(createStatusProgressBar("Pending Inspection Queue", pending, total, "#f59e0b", "#fef3c7"));
        rows.getChildren().add(createStatusProgressBar("On Regulatory Hold", held, total, "#f97316", "#fff7ed"));
        rows.getChildren().add(createStatusProgressBar("Failed / Rejected", failed, total, "#ef4444", "#fef2f2"));

        // Risk Summary Pills
        HBox riskPills = new HBox(8);
        riskPills.setAlignment(Pos.CENTER_LEFT);
        riskPills.setPadding(new Insets(4, 0, 0, 0));

        Label p1 = new Label("🚨 " + urgentRisk + " Urgent / Hazmat");
        p1.setStyle("-fx-background-color:#ffe4e6; -fx-text-fill:#e11d48; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:6;");

        Label p2 = new Label("⚠ " + highRisk + " High Priority");
        p2.setStyle("-fx-background-color:#fffbeb; -fx-text-fill:#d97706; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:6;");

        Label p3 = new Label("✓ " + normalRisk + " Normal Risk");
        p3.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#475569; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:6;");

        riskPills.getChildren().addAll(p1, p2, p3);

        inspectionStatusPanel.getChildren().addAll(new VBox(2, title, subtitle), new Separator(), rows, riskPills);
    }

    private HBox createStatusProgressBar(String labelText, int count, int total, String color, String bgLight) {
        Label name = new Label(labelText);
        name.setStyle("-fx-text-fill:#334155; -fx-font-size:12; -fx-font-weight:bold;");
        name.setPrefWidth(160);

        int pct = total > 0 ? (count * 100 / total) : 0;

        // Custom JavaFX progress indicator
        StackPane track = new StackPane();
        track.setPrefHeight(8);
        track.setMaxHeight(8);
        track.setStyle("-fx-background-color:#f1f5f9; -fx-background-radius:4;");
        HBox.setHgrow(track, Priority.ALWAYS);

        if (pct > 0) {
            Region fill = new Region();
            fill.setPrefHeight(8);
            fill.setMaxHeight(8);
            fill.setStyle("-fx-background-color:" + color + "; -fx-background-radius:4;");
            fill.prefWidthProperty().bind(track.widthProperty().multiply((double) pct / 100.0));
            StackPane.setAlignment(fill, Pos.CENTER_LEFT);
            track.getChildren().add(fill);
        }

        Label val = new Label(count + " (" + pct + "%)");
        val.setStyle("-fx-text-fill:#0f172a; -fx-font-size:12; -fx-font-weight:bold;");
        val.setPrefWidth(65);
        val.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(8, name, track, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ==========================================
    // 10. ATTENTION REQUIRED SECTION
    // ==========================================
    private void buildAttentionRequiredSection() {
        attentionRequiredCard.getChildren().clear();

        Label title = new Label("⚠️ Customs Attention Required");
        title.setStyle("-fx-text-fill:#991b1b; -fx-font-size:16; -fx-font-weight:bold;");

        Label subtitle = new Label("Operational exceptions, held consignments, and urgent hazardous shipments requiring immediate action");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        VBox items = new VBox(6);

        int attentionCount = 0;
        for (Map<String, Object> c : filteredCargoList) {
            String cargoId = String.valueOf(c.get("cargoId"));
            String inspect = String.valueOf(c.get("inspectionStatus"));
            String verif = String.valueOf(c.get("verificationStatus"));
            String prio = String.valueOf(c.get("priority"));
            String type = String.valueOf(c.get("cargoType"));

            if ("Failed".equalsIgnoreCase(inspect) || "Rejected".equalsIgnoreCase(inspect)) {
                items.getChildren().add(createAttentionRow("⛔", "Cargo " + cargoId + " failed physical inspection · Container seal / manifest discrepancy flagged.", "Failed Inspection", "#ef4444", "#fee2e2"));
                attentionCount++;
            } else if ("On Hold".equalsIgnoreCase(inspect) || "On Hold".equalsIgnoreCase(verif) || "Held".equalsIgnoreCase(String.valueOf(c.get("customsStatus")))) {
                items.getChildren().add(createAttentionRow("⏸", "Consignment " + cargoId + " placed on regulatory hold awaiting supplemental documentation.", "Regulatory Hold", "#f97316", "#fff7ed"));
                attentionCount++;
            } else if ("Urgent".equalsIgnoreCase(prio) || type.toLowerCase().contains("hazardous")) {
                items.getChildren().add(createAttentionRow("🚨", "Urgent Dangerous Goods consignment " + cargoId + " (" + type + ") requires immediate physical inspection.", "Urgent Hazmat", "#e11d48", "#ffe4e6"));
                attentionCount++;
            }

            if (attentionCount >= 4) break;
        }

        if (attentionCount == 0) {
            Label clean = new Label("✓ All customs records and declarations are currently compliant with no outstanding holds.");
            clean.setStyle("-fx-text-fill:#16a34a; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:10 0;");
            items.getChildren().add(clean);
        }

        attentionRequiredCard.getChildren().addAll(new VBox(2, title, subtitle), new Separator(), items);
    }

    private HBox createAttentionRow(String icon, String text, String badge, String badgeColor, String badgeBg) {
        Label ic = new Label(icon);
        ic.setStyle("-fx-font-size:13;");

        Label desc = new Label(text);
        desc.setStyle("-fx-text-fill:#334155; -fx-font-size:13;");
        HBox.setHgrow(desc, Priority.ALWAYS);

        Label b = new Label(badge);
        b.setStyle("-fx-text-fill:" + badgeColor + "; -fx-background-color:" + badgeBg + "; -fx-font-size:11; -fx-font-weight:bold; -fx-padding:2 8; -fx-background-radius:6;");

        HBox row = new HBox(8, ic, desc, b);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 8, 4, 8));
        row.setStyle("-fx-background-color:#fafcff; -fx-border-color:#f1f5f9; -fx-border-radius:6; -fx-background-radius:6;");
        return row;
    }

    // ==========================================
    // 11. COLLAPSIBLE ACCORDION: DOCUMENT VERIFICATION
    // ==========================================
    private void buildDocumentVerificationReport() {
        docReportCard.getChildren().clear();

        int totalCount = filteredDocList.size();
        HBox preview = new HBox(10);
        preview.setAlignment(Pos.CENTER_LEFT);
        preview.setPadding(new Insets(4, 0, 4, 0));
        Label prevText = new Label("📋 " + totalCount + " Customs Declarations Audited · Real-time regulatory compliance stream · Click header to expand full table");
        prevText.setStyle("-fx-text-fill:#64748b; -fx-font-size:13; -fx-font-weight:bold;");
        preview.getChildren().add(prevText);

        GridPane table = new GridPane();
        table.setHgap(0); table.setVgap(0);
        table.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        String[] headers = {"Document ID", "Cargo ID / Name", "Importer / Exporter", "Vessel", "Doc Type", "Verification Status", "Priority", "Submission Date", "Certified Officer"};
        for (int i = 0; i < headers.length; i++) {
            table.add(customTableCell(headers[i], true), i, 0);
        }

        if (filteredDocList.isEmpty()) {
            Label empty = new Label("No customs declarations match the active filter criteria.");
            empty.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13; -fx-padding:16 20;");
            table.add(empty, 0, 1, 9, 1);
        } else {
            int rowIdx = 1;
            int limit = Math.min(25, filteredDocList.size());
            for (int i = 0; i < limit; i++) {
                Map<String, Object> d = filteredDocList.get(i);
                String docId = String.valueOf(d.get("docId"));
                String cargoDesc = String.valueOf(d.get("cargoId")) + " - " + String.valueOf(d.get("cargoName"));
                String parties = "Imp: " + String.valueOf(d.get("importer")) + " / Exp: " + String.valueOf(d.get("exporter"));
                String vessel = String.valueOf(d.get("vessel"));
                String docType = String.valueOf(d.get("docType"));
                String verif = String.valueOf(d.get("status"));
                String prio = String.valueOf(d.get("priority"));
                String subDate = String.valueOf(d.get("uploadedAt"));
                String officer = String.valueOf(d.get("verifiedBy"));

                boolean isAlt = (rowIdx % 2 == 0);
                String bg = isAlt ? "#f8fafc" : "#ffffff";

                Label c1 = customTableCell(docId, false); c1.setStyle(c1.getStyle() + " -fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold; -fx-background-color:" + bg + ";");
                Label c2 = customTableCell(cargoDesc, false); c2.setStyle(c2.getStyle() + " -fx-background-color:" + bg + ";");
                Label c3 = customTableCell(parties, false); c3.setStyle(c3.getStyle() + " -fx-background-color:" + bg + ";");
                Label c4 = customTableCell(vessel, false); c4.setStyle(c4.getStyle() + " -fx-background-color:" + bg + ";");
                Label c5 = customTableCell(docType, false); c5.setStyle(c5.getStyle() + " -fx-background-color:" + bg + ";");
                
                HBox c6 = customBadgeCell(verif, isAlt);
                HBox c7 = customBadgeCell(prio, isAlt);

                Label c8 = customTableCell(subDate, false); c8.setStyle(c8.getStyle() + " -fx-background-color:" + bg + ";");
                Label c9 = customTableCell(officer, false); c9.setStyle(c9.getStyle() + " -fx-background-color:" + bg + ";");

                table.add(c1, 0, rowIdx);
                table.add(c2, 1, rowIdx);
                table.add(c3, 2, rowIdx);
                table.add(c4, 3, rowIdx);
                table.add(c5, 4, rowIdx);
                table.add(c6, 5, rowIdx);
                table.add(c7, 6, rowIdx);
                table.add(c8, 7, rowIdx);
                table.add(c9, 8, rowIdx);
                rowIdx++;
            }
        }

        VBox accordionCard = createCollapsibleCard("📋", "Live Document Verification & Declarations Report", "Real-time regulatory declarations, shipping invoices, and compliance audits", preview, table, docReportExpanded);
        docReportCard.getChildren().add(accordionCard);
    }

    // ==========================================
    // 12. COLLAPSIBLE ACCORDION: CARGO INSPECTION
    // ==========================================
    private void buildCargoInspectionReport() {
        inspectionReportCard.getChildren().clear();

        int totalCount = filteredCargoList.size();
        HBox preview = new HBox(10);
        preview.setAlignment(Pos.CENTER_LEFT);
        preview.setPadding(new Insets(4, 0, 4, 0));
        Label prevText = new Label("🔍 " + totalCount + " Physical Cargo Inspections Tracked · Seal verification and hazardous cargo audit · Click header to expand registry");
        prevText.setStyle("-fx-text-fill:#64748b; -fx-font-size:13; -fx-font-weight:bold;");
        preview.getChildren().add(prevText);

        GridPane table = new GridPane();
        table.setHgap(0); table.setVgap(0);
        table.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        String[] headers = {"Cargo ID", "Container / Weight", "Commodity Type", "Exporter / Shipper", "Inspection Status", "Customs Clearance", "Priority", "Inspector", "Last Updated"};
        for (int i = 0; i < headers.length; i++) {
            table.add(customTableCell(headers[i], true), i, 0);
        }

        if (filteredCargoList.isEmpty()) {
            Label empty = new Label("No cargo inspection records match the active filter criteria.");
            empty.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13; -fx-padding:16 20;");
            table.add(empty, 0, 1, 9, 1);
        } else {
            int rowIdx = 1;
            int limit = Math.min(25, filteredCargoList.size());
            for (int i = 0; i < limit; i++) {
                Map<String, Object> c = filteredCargoList.get(i);
                String cargoId = String.valueOf(c.get("cargoId"));
                String contWeight = String.valueOf(c.get("container")) + " (" + String.valueOf(c.get("grossWeight")) + ")";
                String type = String.valueOf(c.get("cargoType"));
                String exp = String.valueOf(c.get("exporter"));
                String inspect = String.valueOf(c.get("inspectionStatus"));
                String customs = String.valueOf(c.get("customsStatus"));
                String prio = String.valueOf(c.get("priority"));
                String inspector = c.get("inspectedBy") != null ? String.valueOf(c.get("inspectedBy")) : officerName;
                String updated = String.valueOf(c.get("updatedAt"));

                boolean isAlt = (rowIdx % 2 == 0);
                String bg = isAlt ? "#f8fafc" : "#ffffff";

                Label c1 = customTableCell(cargoId, false); c1.setStyle(c1.getStyle() + " -fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold; -fx-background-color:" + bg + ";");
                Label c2 = customTableCell(contWeight, false); c2.setStyle(c2.getStyle() + " -fx-background-color:" + bg + ";");
                Label c3 = customTableCell(type, false); c3.setStyle(c3.getStyle() + " -fx-background-color:" + bg + ";");
                Label c4 = customTableCell(exp, false); c4.setStyle(c4.getStyle() + " -fx-background-color:" + bg + ";");
                
                HBox c5 = customBadgeCell(inspect, isAlt);
                HBox c6 = customBadgeCell(customs, isAlt);
                HBox c7 = customBadgeCell(prio, isAlt);

                Label c8 = customTableCell(inspector, false); c8.setStyle(c8.getStyle() + " -fx-background-color:" + bg + ";");
                Label c9 = customTableCell(updated, false); c9.setStyle(c9.getStyle() + " -fx-background-color:" + bg + ";");

                table.add(c1, 0, rowIdx);
                table.add(c2, 1, rowIdx);
                table.add(c3, 2, rowIdx);
                table.add(c4, 3, rowIdx);
                table.add(c5, 4, rowIdx);
                table.add(c6, 5, rowIdx);
                table.add(c7, 6, rowIdx);
                table.add(c8, 7, rowIdx);
                table.add(c9, 8, rowIdx);
                rowIdx++;
            }
        }

        VBox accordionCard = createCollapsibleCard("🔍", "Live Cargo Physical Inspections & Clearance Registry", "Container examination records, seal audits, and terminal release certifications", preview, table, inspectionReportExpanded);
        inspectionReportCard.getChildren().add(accordionCard);
    }

    // ==========================================
    // 13. COLLAPSIBLE ACCORDION: COMPLIANCE SUMMARY
    // ==========================================
    private void buildComplianceSummaryReport() {
        complianceSummaryCard.getChildren().clear();

        int totalDocs = filteredDocList.size();
        int verified = (int) filteredDocList.stream().filter(d -> "Verified".equalsIgnoreCase(String.valueOf(d.get("status")))).count();
        int pending = (int) filteredDocList.stream().filter(d -> "Pending".equalsIgnoreCase(String.valueOf(d.get("status")))).count();
        int urgent = (int) filteredCargoList.stream().filter(c -> "Urgent".equalsIgnoreCase(String.valueOf(c.get("priority")))).count();
        int held = (int) filteredCargoList.stream().filter(c -> "On Hold".equalsIgnoreCase(String.valueOf(c.get("inspectionStatus"))) || "Held".equalsIgnoreCase(String.valueOf(c.get("customsStatus")))).count();

        HBox preview = new HBox(10);
        preview.setAlignment(Pos.CENTER_LEFT);
        preview.setPadding(new Insets(4, 0, 4, 0));
        Label prevText = new Label("🛡 " + verified + " Certified · " + urgent + " Urgent Hazmat Checks · " + held + " Regulatory Holds · Click header to expand risk report");
        prevText.setStyle("-fx-text-fill:#64748b; -fx-font-size:13; -fx-font-weight:bold;");
        preview.getChildren().add(prevText);

        GridPane table = new GridPane();
        table.setHgap(0); table.setVgap(0);
        table.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        String[] headers = {"Compliance Category", "Total Count", "Priority / Risk Level", "Responsible Authority", "Regulatory Status", "Last Audit"};
        for (int i = 0; i < headers.length; i++) {
            table.add(customTableCell(headers[i], true), i, 0);
        }

        Object[][] data = {
                {"Certified Customs Declarations", verified, "NORMAL", "Customs Verification Desk", "Compliant & Certified", nowTime()},
                {"Pending Document Reviews", pending, "HIGH", "Documentation Directorate", "Awaiting Officer Audit", nowTime()},
                {"Hazardous & Dangerous Cargo Audits", urgent, "CRITICAL", "Customs Hazmat Enforcement", "Active Surveillance", nowTime()},
                {"Regulatory / Discrepancy Holds", held, "HIGH", "Port Quarantine & Investigation", "Under Investigation", nowTime()},
                {"Terminal Release Authorizations", totalDocs, "NORMAL", "Port Terminal Authority", "Synchronized", nowTime()}
        };

        for (int r = 0; r < data.length; r++) {
            Label c1 = customTableCell(String.valueOf(data[r][0]), false); c1.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");
            Label c2 = customTableCell(String.valueOf(data[r][1]), false);
            HBox c3 = customBadgeCell(String.valueOf(data[r][2]), (r % 2 == 0));
            Label c4 = customTableCell(String.valueOf(data[r][3]), false);
            Label c5 = customTableCell(String.valueOf(data[r][4]), false);
            Label c6 = customTableCell(String.valueOf(data[r][5]), false);

            String bg = (r % 2 == 0) ? "#f8fafc" : "#ffffff";
            for (Node cell : List.of(c1, c2, c4, c5, c6)) {
                cell.setStyle(cell.getStyle() + "; -fx-background-color:" + bg + ";");
            }

            table.add(c1, 0, r + 1);
            table.add(c2, 1, r + 1);
            table.add(c3, 2, r + 1);
            table.add(c4, 3, r + 1);
            table.add(c5, 4, r + 1);
            table.add(c6, 5, r + 1);
        }

        VBox accordionCard = createCollapsibleCard("🛡", "Customs Compliance & Regulatory Risk Summary", "Statutory clearance metrics, risk profile evaluation, and enforcement queue", preview, table, complianceSummaryExpanded);
        complianceSummaryCard.getChildren().add(accordionCard);
    }

    // ==========================================
    // 14. APACHE PDFBOX EXPORT COMPLIANCE REPORT
    // ==========================================
    private void exportComplianceReportPdf() {
        try {
            File reportsDir = new File("documents/reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "Customs_Compliance_Report_" + activeAssignedPort.replaceAll("\\s+", "_") + "_" + timestamp + ".pdf";
            File targetPdf = new File(reportsDir, filename);

            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.A4);
                doc.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    float y = 800;

                    // 1. Header Banner
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
                    cs.setLeading(22);
                    cs.newLineAtOffset(40, y);
                    cs.showText("DOCK TECH MARITIME OPERATIONS");
                    cs.newLine();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    cs.showText("OFFICIAL CUSTOMS COMPLIANCE & AUDIT REPORT");
                    cs.endText();

                    y -= 50;

                    // 2. Metadata Box
                    cs.setLineWidth(1);
                    cs.setStrokingColor(226/255f, 232/255f, 240/255f);
                    cs.addRect(40, y - 55, 515, 60);
                    cs.stroke();

                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA, 10);
                    cs.setLeading(14);
                    cs.newLineAtOffset(50, y - 10);
                    cs.showText("Port Jurisdiction: " + activeAssignedPort + "   |   Report Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss")));
                    cs.newLine();
                    cs.showText("Auditing Officer: " + officerName + " (" + officerRole + ")   |   Email: " + officerEmail);
                    cs.newLine();
                    cs.showText("Scope: Live Declarations, Physical Inspections, Seal Audits & Clearance Records");
                    cs.endText();

                    y -= 80;

                    // 3. Executive Metrics
                    int totalDocs = filteredDocList.size();
                    int verified = (int) filteredDocList.stream().filter(d -> "Verified".equalsIgnoreCase(String.valueOf(d.get("status")))).count();
                    int pending = (int) filteredDocList.stream().filter(d -> "Pending".equalsIgnoreCase(String.valueOf(d.get("status")))).count();
                    int cleared = (int) filteredCargoList.stream().filter(c -> "Cleared".equalsIgnoreCase(String.valueOf(c.get("customsStatus")))).count();
                    int held = (int) filteredCargoList.stream().filter(c -> "On Hold".equalsIgnoreCase(String.valueOf(c.get("inspectionStatus"))) || "Held".equalsIgnoreCase(String.valueOf(c.get("customsStatus")))).count();

                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    cs.newLineAtOffset(40, y);
                    cs.showText("1. EXECUTIVE SUMMARY METRICS");
                    cs.endText();

                    y -= 20;

                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA, 10);
                    cs.setLeading(14);
                    cs.newLineAtOffset(50, y);
                    cs.showText("• Total Declarations Audited: " + totalDocs);
                    cs.newLine();
                    cs.showText("• Certified & Compliant Declarations: " + verified + " (" + (totalDocs > 0 ? (verified * 100 / totalDocs) : 0) + "%)");
                    cs.newLine();
                    cs.showText("• Pending Officer Verification: " + pending);
                    cs.newLine();
                    cs.showText("• Terminal Release Authorizations: " + cleared);
                    cs.newLine();
                    cs.showText("• Quarantined / Regulatory Holds: " + held);
                    cs.endText();

                    y -= 95;

                    // 4. Audit Table Header
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    cs.newLineAtOffset(40, y);
                    cs.showText("2. RECENT DECLARATIONS & CARGO INSPECTION AUDIT TRAIL");
                    cs.endText();

                    y -= 20;

                    cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
                    cs.beginText();
                    cs.newLineAtOffset(40, y);
                    cs.showText(String.format("%-14s %-22s %-16s %-16s %-12s %-10s", "CARGO ID", "COMMODITY", "IMPORTER", "VESSEL", "VERIF STATUS", "PRIORITY"));
                    cs.endText();

                    y -= 6;
                    cs.moveTo(40, y);
                    cs.lineTo(555, y);
                    cs.stroke();

                    y -= 14;

                    int limit = Math.min(12, filteredDocList.size());
                    cs.setFont(PDType1Font.HELVETICA, 8);
                    for (int i = 0; i < limit; i++) {
                        Map<String, Object> d = filteredDocList.get(i);
                        String cargoId = truncate(String.valueOf(d.get("cargoId")), 12);
                        String name = truncate(String.valueOf(d.get("cargoName")), 20);
                        String imp = truncate(String.valueOf(d.get("importer")), 14);
                        String vessel = truncate(String.valueOf(d.get("vessel")), 14);
                        String verif = truncate(String.valueOf(d.get("status")), 10);
                        String prio = truncate(String.valueOf(d.get("priority")), 8);

                        cs.beginText();
                        cs.newLineAtOffset(40, y);
                        cs.showText(String.format("%-14s %-22s %-16s %-16s %-12s %-10s", cargoId, name, imp, vessel, verif, prio));
                        cs.endText();

                        y -= 14;
                    }

                    if (filteredDocList.isEmpty()) {
                        cs.beginText();
                        cs.newLineAtOffset(50, y);
                        cs.showText("No records match the active filter criteria.");
                        cs.endText();
                        y -= 14;
                    }

                    y -= 30;

                    // 5. Official Customs Verification Stamp Block
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                    cs.newLineAtOffset(40, y);
                    cs.showText("3. STATUTORY REGULATORY DECLARATION");
                    cs.endText();

                    y -= 15;
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
                    cs.setLeading(12);
                    cs.newLineAtOffset(40, y);
                    cs.showText("This report is generated directly from the live Dock Tech customs ledger. All document certifications,");
                    cs.newLine();
                    cs.showText("physical inspection findings, and quarantine records comply with national maritime customs regulations.");
                    cs.endText();

                    y -= 50;
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
                    cs.newLineAtOffset(380, y);
                    cs.showText("CERTIFIED BY: " + officerName.toUpperCase());
                    cs.newLine();
                    cs.setFont(PDType1Font.HELVETICA, 8);
                    cs.showText("Central Board of Customs & Maritime Authority");
                    cs.endText();
                }

                doc.save(targetPdf);
            }

            // Log activity to central ledger
            UserDao.logActivity(officerName, officerEmail, "REPORT_EXPORTED", "Customs Compliance PDF exported for " + activeAssignedPort + " (" + targetPdf.getName() + ")");

            showToast("Report exported: " + targetPdf.getName(), true);

            // Attempt to open the generated PDF
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(targetPdf);
                } catch (Exception ignored) {}
            }

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Compliance Report Exported");
            a.setHeaderText("Official Customs PDF Generated Successfully");
            a.setContentText(
                    "Port Jurisdiction: " + activeAssignedPort + "\n" +
                    "Officer: " + officerName + " (" + officerRole + ")\n" +
                    "File Name: " + targetPdf.getName() + "\n" +
                    "Saved Location: " + targetPdf.getAbsolutePath() + "\n\n" +
                    "The report has been registered in the system audit logs."
            );
            a.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            showToast("Failed to generate PDF: " + ex.getMessage(), false);
            Alert a = new Alert(Alert.AlertType.ERROR, "Error generating report: " + ex.getMessage());
            a.showAndWait();
        }
    }

    private String truncate(String val, int maxLen) {
        if (val == null) return "—";
        if (val.length() <= maxLen) return val;
        return val.substring(0, maxLen - 1) + "…";
    }

    // ==========================================
    // REUSABLE COLLAPSIBLE ACCORDION HELPER (ADMIN STYLE)
    // ==========================================
    private VBox createCollapsibleCard(String titleIcon, String titleText, String subtitleText, Node previewNode, Node contentNode, boolean[] stateTracker) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle(CARD_STYLE);

        Label iconLbl = new Label(titleIcon);
        iconLbl.setStyle("-fx-font-size:18; -fx-text-fill:" + BLUE + ";");

        Label titleLbl = new Label(titleText);
        titleLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16; -fx-font-weight:bold;");

        Label subLbl = new Label(" • " + subtitleText);
        subLbl.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");

        HBox titleBox = new HBox(8, iconLbl, titleLbl, subLbl);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label toggleBtn = new Label(stateTracker[0] ? "▲ Collapse" : "▼ Expand Report");
        toggleBtn.setStyle(
                "-fx-background-color:#eff6ff;" +
                "-fx-text-fill:" + BLUE + ";" +
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:6 12;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );

        HBox headerBar = new HBox(12, titleBox, space, toggleBtn);
        headerBar.setAlignment(Pos.CENTER_LEFT);
        headerBar.setStyle("-fx-cursor:hand;");

        contentNode.setVisible(stateTracker[0]);
        contentNode.setManaged(stateTracker[0]);

        if (previewNode != null) {
            previewNode.setVisible(!stateTracker[0]);
            previewNode.setManaged(!stateTracker[0]);
        }

        headerBar.setOnMouseClicked(e -> {
            stateTracker[0] = !stateTracker[0];
            toggleBtn.setText(stateTracker[0] ? "▲ Collapse" : "▼ Expand Report");
            contentNode.setVisible(stateTracker[0]);
            contentNode.setManaged(stateTracker[0]);
            if (previewNode != null) {
                previewNode.setVisible(!stateTracker[0]);
                previewNode.setManaged(!stateTracker[0]);
            }
        });

        if (previewNode != null) {
            card.getChildren().addAll(headerBar, previewNode, contentNode);
        } else {
            card.getChildren().addAll(headerBar, contentNode);
        }

        return card;
    }

    private Label customTableCell(String text, boolean isHeader) {
        Label lbl = new Label(text);
        if (isHeader) {
            lbl.setStyle(
                    "-fx-text-fill:#0f172a;" +
                    "-fx-font-size:13;" +
                    "-fx-font-weight:bold;" +
                    "-fx-padding:10 14;" +
                    "-fx-background-color:#f8fafc;" +
                    "-fx-border-color:#e2e8f0;" +
                    "-fx-border-width:0 0 1 0;"
            );
            lbl.setMinHeight(44);
        } else {
            lbl.setStyle(
                    "-fx-text-fill:#334155;" +
                    "-fx-font-size:13;" +
                    "-fx-padding:10 14;" +
                    "-fx-border-color:#f1f5f9;" +
                    "-fx-border-width:0 0 1 0;"
            );
            lbl.setMinHeight(48);
        }
        lbl.setMaxWidth(Double.MAX_VALUE);
        return lbl;
    }

    private HBox customBadgeCell(String status, boolean isAlt) {
        Label badge = new Label(status != null ? status.toUpperCase() : "NORMAL");
        String textCol = "#16a34a";
        String bgCol = "#ecfdf5";

        if ("Passed".equalsIgnoreCase(status) || "Verified".equalsIgnoreCase(status) || "Certified".equalsIgnoreCase(status) || "Cleared".equalsIgnoreCase(status) || "Compliant & Certified".equalsIgnoreCase(status)) {
            textCol = "#16a34a";
            bgCol = "#ecfdf5";
        } else if ("Under Review".equalsIgnoreCase(status) || "Under Inspection".equalsIgnoreCase(status) || "Review".equalsIgnoreCase(status)) {
            textCol = "#0b63ff";
            bgCol = "#eff6ff";
        } else if ("Pending".equalsIgnoreCase(status) || "Pending Verification".equalsIgnoreCase(status) || "Awaiting Officer Audit".equalsIgnoreCase(status) || "MEDIUM".equalsIgnoreCase(status)) {
            textCol = "#d97706";
            bgCol = "#fef3c7";
        } else if ("On Hold".equalsIgnoreCase(status) || "Hold".equalsIgnoreCase(status) || "Held".equalsIgnoreCase(status) || "HIGH".equalsIgnoreCase(status)) {
            textCol = "#ea580c";
            bgCol = "#fff7ed";
        } else if ("Failed".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status) || "CRITICAL".equalsIgnoreCase(status) || "Urgent".equalsIgnoreCase(status)) {
            textCol = "#ef4444";
            bgCol = "#fee2e2";
        } else {
            textCol = "#475569";
            bgCol = "#f1f5f9";
        }

        badge.setStyle(
                "-fx-text-fill:" + textCol + ";" +
                "-fx-background-color:" + bgCol + ";" +
                "-fx-background-radius:6;" +
                "-fx-padding:3 8;" +
                "-fx-font-size:11;" +
                "-fx-font-weight:bold;"
        );

        HBox cell = new HBox(badge);
        cell.setMinHeight(48);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 14, 0, 14));
        cell.setStyle(
                "-fx-background-color:" + (isAlt ? "#f8fafc" : "#ffffff") + ";" +
                "-fx-border-color:#f1f5f9; -fx-border-width:0 0 1 0;"
        );
        return cell;
    }

    private void styleAdminField(TextField f) {
        f.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8; -fx-padding:8 12; -fx-font-size:13; -fx-text-fill:#0f172a;");
    }

    private void styleAdminCombo(ComboBox<?> cb) {
        cb.setStyle("-fx-background-radius:8; -fx-padding:6 10; -fx-font-size:13; -fx-border-color:#e6eef8; -fx-background-color:white; -fx-text-fill:#0f172a;");
    }

    private ComboBox<String> combo(String defaultValue, String... options) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(options);
        cb.setValue(defaultValue);
        styleAdminCombo(cb);
        return cb;
    }

    private String nowTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public VBox getContent() {
        return content;
    }
}
