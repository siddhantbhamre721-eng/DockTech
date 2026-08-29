package com.projectui.view.CustomOfficer;

import java.util.List;
import java.util.Map;

import com.projectui.dao.CargoDao;
import com.projectui.dao.UserDao;
import com.projectui.view.login.LoginPage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

@SuppressWarnings({"unchecked", "unused"})
public class CargoInspection {
    private final VBox content;

    private int pendingCount = 0;
    private int underInspectionCount = 0;
    private int approvedCount = 0;
    private int rejectedCount = 0;

    private Label pendingLabel;
    private Label underLabel;
    private Label approvedLabel;
    private Label rejectedLabel;

    private GridPane tableGrid;
    private final CargoDao cargoDao = new CargoDao();
    private String activeAssignedPort = "Port of Mumbai";
    private String officerName = "Customs Officer";

    private TextField searchField;
    private ComboBox<String> statusFilterCombo;

    public CargoInspection() {
        resolveOfficerContext();

        content = new VBox(20);
        content.setPadding(new Insets(20, 28, 28, 28));
        content.setStyle("-fx-background-color:#f4f7fb;");

        // HEADER
        Label title = new Label("Cargo Physical Inspection (" + activeAssignedPort + ")");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#111827"));

        Label subtitle = new Label("Conduct physical container audits, hazardous compliance checks, and authorize clearance.");
        subtitle.setTextFill(Color.web("#64748b"));
        subtitle.setFont(Font.font("Arial", 13));

        VBox heading = new VBox(4, title, subtitle);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Button refreshBtn = new Button("↻ Refresh Data");
        refreshBtn.setStyle("-fx-background-color:white; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:6 12; -fx-font-size:12px; -fx-font-weight:bold; -fx-cursor:hand;");
        refreshBtn.setOnAction(e -> refreshInspectionData());

        HBox header = new HBox(heading, space, refreshBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        // STAT CARDS
        HBox cards = new HBox(15);
        VBox c1 = createCard("Pending Inspection", "0", "◷", "#f59e0b", "#fef3c7");
        VBox c2 = createCard("Under Inspection", "0", "⌕", "#0284c7", "#e0f2fe");
        VBox c3 = createCard("Passed & Approved", "0", "✓", "#10b981", "#d1fae5");
        VBox c4 = createCard("Failed / Rejected", "0", "✕", "#ef4444", "#fee2e2");

        pendingLabel = (Label) c1.getChildren().get(1);
        underLabel = (Label) c2.getChildren().get(1);
        approvedLabel = (Label) c3.getChildren().get(1);
        rejectedLabel = (Label) c4.getChildren().get(1);

        cards.getChildren().addAll(c1, c2, c3, c4);

        // FILTER BAR
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(12, 16, 12, 16));
        filterBar.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        searchField = new TextField();
        searchField.setPromptText("Search Cargo ID, Category, Exporter...");
        searchField.setPrefWidth(260);
        searchField.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px;");
        searchField.textProperty().addListener((obs, o, n) -> renderTableRows());

        statusFilterCombo = new ComboBox<>();
        statusFilterCombo.getItems().addAll("All Statuses", "Pending", "Under Inspection", "Passed", "Failed", "Hold");
        statusFilterCombo.setValue("All Statuses");
        statusFilterCombo.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px;");
        statusFilterCombo.setOnAction(e -> renderTableRows());

        Region filterGap = new Region();
        HBox.setHgrow(filterGap, Priority.ALWAYS);

        filterBar.getChildren().addAll(searchField, statusFilterCombo, filterGap);

        // TABLE BOX
        VBox tableBox = new VBox(14);
        tableBox.setPadding(new Insets(18));
        tableBox.setStyle("-fx-background-color:white; -fx-background-radius:8; -fx-border-color:#e2e8f0; -fx-border-radius:8;");

        Label tableTitle = new Label("Live Cargo Inspection Registry");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        tableTitle.setTextFill(Color.web("#0f172a"));

        tableGrid = new GridPane();
        tableGrid.setHgap(15);
        tableGrid.setVgap(10);
        tableGrid.setPadding(new Insets(6, 0, 6, 0));

        ScrollPane scroll = new ScrollPane(tableGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-color:transparent;");

        tableBox.getChildren().addAll(tableTitle, scroll);

        content.getChildren().addAll(header, cards, filterBar, tableBox);

        refreshInspectionData();
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

    private void refreshInspectionData() {
        resolveOfficerContext();
        pendingCount = 0;
        underInspectionCount = 0;
        approvedCount = 0;
        rejectedCount = 0;

        List<Map<String, Object>> cargos = cargoDao.getCargosByPort(activeAssignedPort);
        if (cargos != null) {
            for (Map<String, Object> c : cargos) {
                if (c == null) continue;
                String inspectStatus = c.get("inspectionStatus") != null ? String.valueOf(c.get("inspectionStatus")) : "Pending";

                if ("Passed".equalsIgnoreCase(inspectStatus) || "Approved".equalsIgnoreCase(inspectStatus) || "Cleared".equalsIgnoreCase(inspectStatus)) {
                    approvedCount++;
                } else if ("Under Inspection".equalsIgnoreCase(inspectStatus)) {
                    underInspectionCount++;
                } else if ("Failed".equalsIgnoreCase(inspectStatus) || "Rejected".equalsIgnoreCase(inspectStatus) || "Hold".equalsIgnoreCase(inspectStatus)) {
                    rejectedCount++;
                } else {
                    pendingCount++;
                }
            }
        }

        if (pendingLabel != null) pendingLabel.setText(String.valueOf(pendingCount));
        if (underLabel != null) underLabel.setText(String.valueOf(underInspectionCount));
        if (approvedLabel != null) approvedLabel.setText(String.valueOf(approvedCount));
        if (rejectedLabel != null) rejectedLabel.setText(String.valueOf(rejectedCount));

        renderTableRows();
    }

    private void renderTableRows() {
        if (tableGrid == null) return;
        tableGrid.getChildren().clear();

        String[] headers = {
                "Cargo ID", "Cargo Name", "Exporter / Shipper", "Category",
                "Weight", "Inspection Status", "Customs Status", "Actions"
        };

        for (int i = 0; i < headers.length; i++) {
            Label l = new Label(headers[i]);
            l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            l.setTextFill(Color.web("#475569"));
            l.setPadding(new Insets(8, 6, 8, 6));
            tableGrid.add(l, i, 0);
        }

        String query = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String statusFilter = statusFilterCombo != null ? statusFilterCombo.getValue() : "All Statuses";

        List<Map<String, Object>> cargos = cargoDao.getCargosByPort(activeAssignedPort);
        int rowIndex = 1;

        if (cargos != null) {
            for (Map<String, Object> c : cargos) {
                if (c == null) continue;
                String cargoId = String.valueOf(c.get("cargoId"));
                String name = c.get("cargoName") != null ? String.valueOf(c.get("cargoName")) : "General Cargo";
                String exporter = c.get("exporter") != null ? String.valueOf(c.get("exporter")) : (c.get("company") != null ? String.valueOf(c.get("company")) : "Maritime Corp");
                String type = c.get("cargoType") != null ? String.valueOf(c.get("cargoType")) : "General";
                String weight = c.get("weight") != null ? String.valueOf(c.get("weight")) : "1,000 kg";
                String inspectStatus = c.get("inspectionStatus") != null ? String.valueOf(c.get("inspectionStatus")) : "Pending";
                String customsStatus = c.get("customsStatus") != null ? String.valueOf(c.get("customsStatus")) : "Pending";

                boolean matchQuery = query.isEmpty() ||
                        cargoId.toLowerCase().contains(query) ||
                        name.toLowerCase().contains(query) ||
                        exporter.toLowerCase().contains(query) ||
                        type.toLowerCase().contains(query);

                boolean matchStatus = "All Statuses".equalsIgnoreCase(statusFilter) ||
                        inspectStatus.equalsIgnoreCase(statusFilter) ||
                        customsStatus.equalsIgnoreCase(statusFilter);

                if (matchQuery && matchStatus) {
                    addGridRow(rowIndex++, cargoId, name, exporter, type, weight, inspectStatus, customsStatus);
                }
            }
        }

        if (rowIndex == 1) {
            Label empty = new Label("No cargo inspection records found for " + activeAssignedPort);
            empty.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13px; -fx-padding:15;");
            tableGrid.add(empty, 0, 1, 8, 1);
        }
    }

    private void addGridRow(int row, String id, String name, String company, String type, String weight, String inspectStatus, String customsStatus) {
        Label lId = new Label(id);
        lId.setStyle("-fx-font-weight:bold; -fx-text-fill:#0f172a; -fx-font-size:12px;");

        Label lName = new Label(name);
        lName.setStyle("-fx-text-fill:#334155; -fx-font-size:12px;");

        Label lComp = new Label(company);
        lComp.setStyle("-fx-text-fill:#334155; -fx-font-size:12px;");

        Label lType = new Label(type);
        lType.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");

        Label lWeight = new Label(weight);
        lWeight.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");

        Label badgeInspect = new Label(inspectStatus);
        if ("Passed".equalsIgnoreCase(inspectStatus) || "Approved".equalsIgnoreCase(inspectStatus)) {
            badgeInspect.setStyle("-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
        } else if ("Under Inspection".equalsIgnoreCase(inspectStatus)) {
            badgeInspect.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0369a1; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
        } else if ("Failed".equalsIgnoreCase(inspectStatus) || "Rejected".equalsIgnoreCase(inspectStatus)) {
            badgeInspect.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#991b1b; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");
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

        // Action Buttons
        HBox actions = new HBox(6);
        Button startBtn = new Button("Inspect");
        startBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 6; -fx-cursor:hand;");
        startBtn.setOnAction(e -> {
            cargoDao.updateInspectionStatus(id, "Under Inspection", officerName, "Physical inspection initiated by officer", null);
            refreshInspectionData();
        });

        Button passBtn = new Button("Pass");
        passBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 6; -fx-cursor:hand;");
        passBtn.setOnAction(e -> {
            cargoDao.updateInspectionStatus(id, "Passed", officerName, "Physical inspection verified. Cargo conforms to declaration.", null);
            refreshInspectionData();
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Inspection Passed");
            a.setHeaderText("Cargo " + id + " Inspection Passed");
            a.setContentText("Inspection certified by " + officerName + ". Cargo is now eligible for final Customs Clearance.");
            a.showAndWait();
        });

        Button failBtn = new Button("Fail");
        failBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:10px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 6; -fx-cursor:hand;");
        failBtn.setOnAction(e -> {
            TextInputDialog td = new TextInputDialog("Discrepancy in seal number / cargo weight");
            td.setTitle("Record Inspection Failure");
            td.setHeaderText("Specify Physical Inspection Failure Reason");
            td.setContentText("Failure Reason:");
            td.showAndWait().ifPresent(reason -> {
                cargoDao.updateInspectionStatus(id, "Failed", officerName, "Physical inspection failed: " + reason, reason);
                refreshInspectionData();
            });
        });

        actions.getChildren().addAll(startBtn, passBtn, failBtn);

        tableGrid.add(lId, 0, row);
        tableGrid.add(lName, 1, row);
        tableGrid.add(lComp, 2, row);
        tableGrid.add(lType, 3, row);
        tableGrid.add(lWeight, 4, row);
        tableGrid.add(badgeInspect, 5, row);
        tableGrid.add(badgeCustoms, 6, row);
        tableGrid.add(actions, 7, row);
    }

    private VBox createCard(String title, String count, String icon, String color, String bg) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);

        Label ic = new Label(icon);
        ic.setStyle("-fx-font-size:14px; -fx-text-fill:" + color + "; -fx-background-color:" + bg + "; -fx-background-radius:6; -fx-padding:4 8;");

        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px; -fx-font-weight:bold;");
        top.getChildren().addAll(ic, t);

        Label c = new Label(count);
        c.setStyle("-fx-text-fill:#0f172a; -fx-font-size:22px; -fx-font-weight:bold;");

        card.getChildren().addAll(top, c);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    public VBox getContent() {
        return content;
    }
}
