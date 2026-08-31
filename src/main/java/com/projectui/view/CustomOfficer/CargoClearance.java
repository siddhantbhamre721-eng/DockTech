package com.projectui.view.CustomOfficer;

import java.util.List;
import java.util.Map;

import com.projectui.dao.CargoDao;
import com.projectui.dao.UserDao;
import com.projectui.view.login.LoginPage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
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
public class CargoClearance {
    private final BorderPane root = new BorderPane();
    private VBox content;

    private final Label pendingLabel = new Label("0");
    private final Label approvedLabel = new Label("0");
    private final Label rejectedLabel = new Label("0");
    private final Label totalLabel = new Label("0");

    private GridPane clearanceGrid;
    private final CargoDao cargoDao = new CargoDao();
    private String activeAssignedPort = "Port of Mumbai";
    private String officerName = "Customs Officer";

    private TextField searchField;
    private ComboBox<String> statusFilterCombo;
    private ComboBox<String> typeFilterCombo;

    public CargoClearance() {
        resolveOfficerContext();
        buildUI();
        refreshClearanceData();
    }

    private void resolveOfficerContext() {
        if (LoginPage.loggedInUser != null) {
            Map<String, Object> u = LoginPage.loggedInUser;
            if (u.get("name") != null) officerName = u.get("name").toString();

            String portVal = null;
            if (u.get("assignedPort") != null && !u.get("assignedPort").toString().isBlank()) {
                portVal = u.get("assignedPort").toString();
            } else if (u.get("port") != null && !u.get("port").toString().isBlank()) {
                portVal = u.get("port").toString();
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
        root.setStyle("-fx-background-color:#f4f7fb;");

        content = new VBox(16);
        content.setPadding(new Insets(18, 26, 26, 26));

        // HEADER
        Label title = label("Approve / Reject Cargo Clearance (" + activeAssignedPort + ")", 26, true);
        Label sub = label("Final regulatory adjudication and terminal release certification.", 13, false);
        sub.setTextFill(Color.web("#64748b"));

        VBox titleBox = new VBox(3, title, sub);

        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);

        Button refreshBtn = new Button("↻ Refresh Data");
        refreshBtn.setStyle("-fx-background-color:white; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:6 12; -fx-font-size:12px; -fx-font-weight:bold; -fx-cursor:hand;");
        refreshBtn.setOnAction(e -> refreshClearanceData());

        HBox header = new HBox(15, titleBox, gap, refreshBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        // SUMMARY METRIC CARDS
        HBox cards = new HBox(14,
                card("Pending Clearance", pendingLabel, "◷", "#f59e0b", "#fef3c7"),
                card("Cleared & Released", approvedLabel, "✓", "#10b981", "#d1fae5"),
                card("Rejected / Held", rejectedLabel, "×", "#ef4444", "#fee2e2"),
                card("Total Shipments", totalLabel, "▣", "#0284c7", "#e0f2fe")
        );

        for (Node n : cards.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
        }

        // FILTER BAR
        searchField = new TextField();
        searchField.setPromptText("⌕  Search Cargo ID, Vessel, Company...");
        searchField.setPrefWidth(260);
        searchField.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px;");
        searchField.textProperty().addListener((obs, o, n) -> renderClearanceTable());

        statusFilterCombo = combo("All Status", "All Status", "Pending", "Cleared", "Held", "Rejected");
        statusFilterCombo.setOnAction(e -> renderClearanceTable());

        typeFilterCombo = combo("All Types", "All Types", "Electronics", "Machinery", "Textiles", "Automotive", "Perishables", "General Cargo");
        typeFilterCombo.setOnAction(e -> renderClearanceTable());

        HBox filter = new HBox(10, searchField, statusFilterCombo, typeFilterCombo);
        filter.setPadding(new Insets(12));
        filter.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        // CLEARANCE REGISTRY TABLE
        VBox tableBox = new VBox(12);
        tableBox.setPadding(new Insets(18));
        tableBox.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        Label tableHead = label("Official Customs Clearance Adjudication Table", 16, true);
        Label tableSub = label("Authorize release for vessel loading/unloading or enforce regulatory hold.", 12, false);
        tableSub.setTextFill(Color.web("#64748b"));

        clearanceGrid = new GridPane();
        clearanceGrid.setHgap(15);
        clearanceGrid.setVgap(10);
        clearanceGrid.setPadding(new Insets(8, 0, 8, 0));

        ScrollPane scroll = new ScrollPane(clearanceGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-color:transparent;");

        tableBox.getChildren().addAll(tableHead, tableSub, new Separator(), scroll);

        content.getChildren().addAll(header, cards, filter, tableBox);
        root.setCenter(content);
    }

    private void refreshClearanceData() {
        resolveOfficerContext();

        int pendingCount = 0;
        int approvedCount = 0;
        int rejectedCount = 0;
        int totalCount = 0;

        List<Map<String, Object>> cargos = cargoDao.getCargosByPort(activeAssignedPort);
        if (cargos != null) {
            for (Map<String, Object> c : cargos) {
                if (c == null) continue;
                String st = c.get("customsStatus") != null ? String.valueOf(c.get("customsStatus")) : "Pending";

                if ("Cleared".equalsIgnoreCase(st) || "Approved".equalsIgnoreCase(st)) {
                    approvedCount++;
                } else if ("Rejected".equalsIgnoreCase(st) || "Held".equalsIgnoreCase(st)) {
                    rejectedCount++;
                } else {
                    pendingCount++;
                }
                totalCount++;
            }
        }

        pendingLabel.setText(String.valueOf(pendingCount));
        approvedLabel.setText(String.valueOf(approvedCount));
        rejectedLabel.setText(String.valueOf(rejectedCount));
        totalLabel.setText(String.valueOf(totalCount));

        renderClearanceTable();
    }

    private void renderClearanceTable() {
        if (clearanceGrid == null) return;
        clearanceGrid.getChildren().clear();

        String[] headers = {
                "Cargo ID", "Cargo / Commodity", "Vessel / Ship", "Importer / Exporter",
                "Docs Status", "Inspection", "Clearance Decision", "Actions"
        };

        for (int i = 0; i < headers.length; i++) {
            Label l = new Label(headers[i]);
            l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            l.setTextFill(Color.web("#475569"));
            l.setPadding(new Insets(8, 6, 8, 6));
            clearanceGrid.add(l, i, 0);
        }

        String query = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String statusVal = statusFilterCombo != null ? statusFilterCombo.getValue() : "All Status";
        String typeVal = typeFilterCombo != null ? typeFilterCombo.getValue() : "All Types";

        String officerUid = LoginPage.loggedInUser != null && LoginPage.loggedInUser.get("uid") != null ? LoginPage.loggedInUser.get("uid").toString() : "";
        String officerEmail = LoginPage.loggedInUser != null && LoginPage.loggedInUser.get("email") != null ? LoginPage.loggedInUser.get("email").toString() : "";
        List<Map<String, Object>> cargos = cargoDao.getCargosForCustomsOfficer(officerUid, officerEmail, activeAssignedPort);
        int rowIndex = 1;

        if (cargos != null) {
            for (Map<String, Object> c : cargos) {
                if (c == null) continue;

                String cargoId = String.valueOf(c.get("cargoId"));
                String name = c.get("cargoName") != null ? String.valueOf(c.get("cargoName")) : "General Cargo";
                String type = c.get("cargoType") != null ? String.valueOf(c.get("cargoType")) : "General";
                String orig = c.get("originPort") != null ? c.get("originPort").toString() : "—";
                String dest = c.get("destinationPort") != null ? c.get("destinationPort").toString() : "—";

                String vessel = c.get("assignedShip") != null ? String.valueOf(c.get("assignedShip")) : (c.get("vessel") != null ? String.valueOf(c.get("vessel")) : "Not Assigned");
                String capName = c.get("assignedCaptainName") != null ? c.get("assignedCaptainName").toString() : (c.get("assignedCaptain") != null ? c.get("assignedCaptain").toString() : "");
                String vesselDisplay = vessel + (!capName.isBlank() && !capName.equalsIgnoreCase("Ship Captain") ? (" (Cap: " + capName + ")") : "");

                String company = c.get("exporter") != null ? String.valueOf(c.get("exporter")) : (c.get("company") != null ? String.valueOf(c.get("company")) : "Maritime Logistics");
                String docStatus = c.get("verificationStatus") != null ? String.valueOf(c.get("verificationStatus")) : "Pending";
                String inspectStatus = c.get("inspectionStatus") != null ? String.valueOf(c.get("inspectionStatus")) : "Pending";
                String customsStatus = c.get("customsClearanceStatus") != null ? String.valueOf(c.get("customsClearanceStatus")) : (c.get("customsStatus") != null ? String.valueOf(c.get("customsStatus")) : "Pending");

                boolean matchQuery = query.isEmpty() ||
                        cargoId.toLowerCase().contains(query) ||
                        name.toLowerCase().contains(query) ||
                        vessel.toLowerCase().contains(query) ||
                        company.toLowerCase().contains(query);

                boolean matchStatus = "All Status".equalsIgnoreCase(statusVal) || customsStatus.equalsIgnoreCase(statusVal);
                boolean matchType = "All Types".equalsIgnoreCase(typeVal) || type.equalsIgnoreCase(typeVal) || name.toLowerCase().contains(typeVal.toLowerCase());

                if (matchQuery && matchStatus && matchType) {
                    addClearanceRow(rowIndex++, cargoId, name + " (" + type + ") • " + orig + " ➔ " + dest, vesselDisplay, company, docStatus, inspectStatus, customsStatus);
                }
            }
        }

        if (rowIndex == 1) {
            Label empty = new Label("No shipments awaiting customs clearance decision for " + activeAssignedPort);
            empty.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13px; -fx-padding:15;");
            clearanceGrid.add(empty, 0, 1, 8, 1);
        }
    }

    private void addClearanceRow(int row, String id, String cargoDesc, String vessel, String company, String docStatus, String inspectStatus, String customsStatus) {
        Label lId = new Label(id);
        lId.setStyle("-fx-font-weight:bold; -fx-text-fill:#0f172a; -fx-font-size:12px;");

        Label lDesc = new Label(cargoDesc);
        lDesc.setStyle("-fx-text-fill:#334155; -fx-font-size:12px;");

        Label lVessel = new Label(vessel);
        lVessel.setStyle("-fx-text-fill:#334155; -fx-font-size:12px;");

        Label lCompany = new Label(company);
        lCompany.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");

        Label badgeDocs = new Label(docStatus);
        if ("Verified".equalsIgnoreCase(docStatus) || "Approved".equalsIgnoreCase(docStatus)) {
            badgeDocs.setStyle("-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
        } else {
            badgeDocs.setStyle("-fx-background-color:#fef3c7; -fx-text-fill:#92400e; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
        }

        Label badgeInspect = new Label(inspectStatus);
        if ("Passed".equalsIgnoreCase(inspectStatus) || "Approved".equalsIgnoreCase(inspectStatus)) {
            badgeInspect.setStyle("-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
        } else if ("Under Inspection".equalsIgnoreCase(inspectStatus)) {
            badgeInspect.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0369a1; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
        } else {
            badgeInspect.setStyle("-fx-background-color:#fef3c7; -fx-text-fill:#92400e; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
        }

        Label badgeCustoms = new Label(customsStatus);
        if ("Cleared".equalsIgnoreCase(customsStatus) || "Approved".equalsIgnoreCase(customsStatus)) {
            badgeCustoms.setStyle("-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
        } else if ("Held".equalsIgnoreCase(customsStatus)) {
            badgeCustoms.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#991b1b; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
        } else {
            badgeCustoms.setStyle("-fx-background-color:#fef3c7; -fx-text-fill:#92400e; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
        }

        // ACTIONS
        HBox actions = new HBox(6);
        Button approveBtn = new Button("Approve & Clear");
        approveBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 6; -fx-cursor:hand;");
        approveBtn.setOnAction(e -> {
            cargoDao.updateCustomsClearance(id, "Cleared", officerName, "Regulatory clearance granted for vessel operation");
            refreshClearanceData();
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Cargo Cleared");
            a.setHeaderText("Cargo " + id + " Approved and Cleared");
            a.setContentText("Customs Release Order issued by " + officerName + " (" + activeAssignedPort + ").\nPort Authority & Ship Captain have been notified for cargo loading/unloading.");
            a.showAndWait();
        });

        Button holdBtn = new Button("Hold");
        holdBtn.setStyle("-fx-background-color:#f59e0b; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 6; -fx-cursor:hand;");
        holdBtn.setOnAction(e -> {
            TextInputDialog td = new TextInputDialog("Pending duty payment verification / quarantine order");
            td.setTitle("Place Cargo on Customs Hold");
            td.setHeaderText("Specify Reason for Regulatory Hold on " + id);
            td.setContentText("Hold Reason:");
            td.showAndWait().ifPresent(reason -> {
                cargoDao.updateCustomsClearance(id, "Held", officerName, reason);
                refreshClearanceData();
            });
        });

        Button rejectBtn = new Button("Reject");
        rejectBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 6; -fx-cursor:hand;");
        rejectBtn.setOnAction(e -> {
            TextInputDialog td = new TextInputDialog("Prohibited goods / fraudulent declaration");
            td.setTitle("Reject Cargo Clearance");
            td.setHeaderText("Specify Reason for Cargo Rejection: " + id);
            td.setContentText("Rejection Reason:");
            td.showAndWait().ifPresent(reason -> {
                cargoDao.updateCustomsClearance(id, "Rejected", officerName, reason);
                refreshClearanceData();
            });
        });

        actions.getChildren().addAll(approveBtn, holdBtn, rejectBtn);

        clearanceGrid.add(lId, 0, row);
        clearanceGrid.add(lDesc, 1, row);
        clearanceGrid.add(lVessel, 2, row);
        clearanceGrid.add(lCompany, 3, row);
        clearanceGrid.add(badgeDocs, 4, row);
        clearanceGrid.add(badgeInspect, 5, row);
        clearanceGrid.add(badgeCustoms, 6, row);
        clearanceGrid.add(actions, 7, row);
    }

    private VBox card(String title, Label countLbl, String icon, String color, String bg) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label ic = new Label(icon);
        ic.setStyle("-fx-font-size:14px; -fx-text-fill:" + color + "; -fx-background-color:" + bg + "; -fx-background-radius:6; -fx-padding:4 8;");

        Label t = label(title, 12, true);
        t.setTextFill(Color.web("#64748b"));
        top.getChildren().addAll(ic, t);

        countLbl.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        countLbl.setTextFill(Color.web("#0f172a"));

        card.getChildren().addAll(top, countLbl);
        return card;
    }

    private ComboBox<String> combo(String prompt, String... items) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(items);
        cb.setValue(prompt);
        cb.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px;");
        return cb;
    }

    private Label label(String text, int size, boolean bold) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        l.setTextFill(Color.web("#0f172a"));
        return l;
    }

    public Parent getRoot() {
        return root;
    }

    public VBox getContent() {
        return content;
    }
}
