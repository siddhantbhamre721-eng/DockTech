package com.projectui.view.login;

import java.util.LinkedHashMap;
import java.util.Map;

import com.projectui.view.admin.AdminDataStore;
import com.projectui.view.admin.AdminDataStore.PortRequest;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RequestNewPortPage {

    private static final String BLUE = "#0b63ff";
    private static final String BG_COLOR = "#f8fafc";
    private static final String TEXT_PRIMARY = "#0f172a";
    private static final String TEXT_MUTED = "#64748b";

    private Stage stage;
    private int currentStep = 1;

    // Form fields across steps
    private final TextField portNameField = createField("e.g. Port of Hazira");
    private final TextField portCodeField = createField("e.g. IN-HAZ-01");
    private final TextField locationField = createField("e.g. Hazira, Surat, Gujarat");
    private final TextArea addressField = createTextArea("Enter port address and terminal location details...");
    private final TextField cargoTypeField = createField("e.g. Containers, Liquid Bulk, Automobiles");
    private final TextField capacityField = createField("e.g. 85%");

    private final TextField paNameField = createField("Proposed Port Authority Full Name");
    private final TextField paEmailField = createField("pa_hazira@docktech.com");
    private final TextField paPhoneField = createField("+91 98765 43210");
    private final TextField paOrgField = createField("e.g. Gujarat Maritime Board");

    private final TextField reqNameField = createField("Requester Full Name");
    private final TextField reqEmailField = createField("requester@docktech.com");
    private final TextField reqPhoneField = createField("+91 98123 45678");

    private final VBox stepContentContainer = new VBox(20);
    private final HBox stepperRow = new HBox(12);

    public void show(Stage stage) {
        this.stage = stage;

        BorderPane root = new BorderPane();

        // 1. Sidebar Navigation (Reusing exact application shell component)
        Map<String, Button> menuButtons = new LinkedHashMap<>();

        Button backLoginBtn = new Button("🔑  Back to Login");
        backLoginBtn.setOnAction(e -> new LoginPage().show(stage));
        menuButtons.put("Back to Login", backLoginBtn);

        Button requestPortBtn = new Button("⚓  Request New Port");
        menuButtons.put("Request New Port", requestPortBtn);

        Button helpBtn = new Button("❓  Help & Support");
        helpBtn.setOnAction(e -> new LoginPage().show(stage));
        menuButtons.put("Help & Support", helpBtn);

        VBox sidebar = DashboardLayoutHelper.createSidebar(stage, "Requester", "Request New Port", menuButtons);

        // 2. Top Header (Reusing exact application shell component)
        HBox topHeader = DashboardLayoutHelper.createHeader(
                stage,
                "Request New Port",
                "Dock Tech / Port Operations / Request New Port",
                "Requester",
                () -> new LoginPage().show(stage),
                null
        );

        // 3. Main Content Area (4-Step Guided Workflow)
        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 32, 36, 32));
        content.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // Header Banner
        VBox headerBanner = new VBox(6);
        Label title = new Label("Establish a New Port");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        Label subtitle = new Label("Submit a formal port proposal for Dock Tech Admin review and Port Authority account provisioning.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT_MUTED + ";");
        headerBanner.getChildren().addAll(title, subtitle);

        // Render Stepper & Active Step View
        renderStepper();
        renderStepContent();

        content.getChildren().addAll(headerBanner, stepperRow, stepContentContainer);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + BG_COLOR + "; -fx-border-color: transparent;");

        root.setTop(topHeader);
        root.setLeft(sidebar);
        root.setCenter(scrollPane);

        LoginPage.showRoot(stage, root);
        stage.setTitle("Dock Tech - Request New Port");
    }

    private void renderStepper() {
        stepperRow.getChildren().clear();
        stepperRow.setAlignment(Pos.CENTER_LEFT);

        stepperRow.getChildren().addAll(
                stepTile(1, "Port Details"),
                stepDivider(),
                stepTile(2, "Port Authority Details"),
                stepDivider(),
                stepTile(3, "Upload Documents"),
                stepDivider(),
                stepTile(4, "Review & Submit")
        );
    }

    private HBox stepTile(int stepIndex, String title) {
        boolean active = currentStep == stepIndex;
        boolean completed = currentStep > stepIndex;

        HBox tile = new HBox(10);
        tile.setAlignment(Pos.CENTER_LEFT);
        tile.setPadding(new Insets(10, 16, 10, 16));

        String bg = active ? "#0B63FF" : (completed ? "#ECFDF5" : "#FFFFFF");
        String border = active ? "#0B63FF" : (completed ? "#A7F3D0" : "#E2E8F0");
        String numBg = active ? "white" : (completed ? "#10B981" : "#F1F5F9");
        String numFg = active ? "#0B63FF" : (completed ? "white" : "#64748B");
        String textFg = active ? "white" : (completed ? "#047857" : "#475569");

        tile.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + border + "; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        Label num = new Label(completed ? "✓" : String.valueOf(stepIndex));
        num.setStyle("-fx-background-color: " + numBg + "; -fx-text-fill: " + numFg + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-alignment: center; -fx-min-width: 24px; -fx-min-height: 24px; -fx-background-radius: 999px;");

        Label label = new Label(title);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + textFg + ";");

        tile.getChildren().addAll(num, label);
        return tile;
    }

    private Region stepDivider() {
        Region r = new Region();
        r.setPrefWidth(24);
        r.setPrefHeight(2);
        r.setStyle("-fx-background-color: #E2E8F0;");
        return r;
    }

    private void renderStepContent() {
        stepContentContainer.getChildren().clear();

        VBox card = new VBox(20);
        card.setPadding(new Insets(28));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14px; -fx-border-color: #E2E8F0; -fx-border-radius: 14px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 10, 0, 0, 2);");

        if (currentStep == 1) {
            renderStep1(card);
        } else if (currentStep == 2) {
            renderStep2(card);
        } else if (currentStep == 3) {
            renderStep3(card);
        } else if (currentStep == 4) {
            renderStep4(card);
        }

        stepContentContainer.getChildren().add(card);
    }

    // Step 1: Port Details
    private void renderStep1(VBox card) {
        Label stepTitle = new Label("Step 1: Port Details");
        stepTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        VBox grid = new VBox(14,
                new HBox(14, formGroup("Port Name *", portNameField), formGroup("Port Code", portCodeField)),
                new HBox(14, formGroup("Location / City *", locationField), formGroup("Port Capacity (%)", capacityField)),
                formGroup("Cargo Handling Types", cargoTypeField),
                formGroup("Port Address & Terminal Details *", addressField)
        );

        grid.getChildren().forEach(node -> {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                for (var child : row.getChildren()) {
                    HBox.setHgrow(child, Priority.ALWAYS);
                }
            }
        });

        Button nextBtn = new Button("Next: Port Authority Details →");
        nextBtn.setStyle(primaryBtnStyle());
        nextBtn.setOnAction(e -> {
            if (portNameField.getText().trim().isEmpty() || locationField.getText().trim().isEmpty()) {
                return;
            }
            currentStep = 2;
            renderStepper();
            renderStepContent();
        });

        HBox btnRow = new HBox(nextBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(stepTitle, grid, btnRow);
    }

    // Step 2: Port Authority Details
    private void renderStep2(VBox card) {
        Label stepTitle = new Label("Step 2: Designated Port Authority Details");
        stepTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        VBox grid = new VBox(14,
                new HBox(14, formGroup("Proposed Port Authority Name *", paNameField), formGroup("Proposed Port Authority Email *", paEmailField)),
                new HBox(14, formGroup("Phone Number *", paPhoneField), formGroup("Organization / Board", paOrgField)),
                new Label("Requester Contact Information"),
                new HBox(14, formGroup("Requester Name *", reqNameField), formGroup("Requester Email *", reqEmailField)),
                formGroup("Requester Phone *", reqPhoneField)
        );

        grid.getChildren().forEach(node -> {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                for (var child : row.getChildren()) {
                    HBox.setHgrow(child, Priority.ALWAYS);
                }
            }
        });

        Button backBtn = new Button("← Back");
        backBtn.setStyle(secondaryBtnStyle());
        backBtn.setOnAction(e -> {
            currentStep = 1;
            renderStepper();
            renderStepContent();
        });

        Button nextBtn = new Button("Next: Upload Documents →");
        nextBtn.setStyle(primaryBtnStyle());
        nextBtn.setOnAction(e -> {
            currentStep = 3;
            renderStepper();
            renderStepContent();
        });

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox btnRow = new HBox(12, backBtn, space, nextBtn);

        card.getChildren().addAll(stepTitle, grid, btnRow);
    }

    // Step 3: Upload Documents
    private void renderStep3(VBox card) {
        Label stepTitle = new Label("Step 3: Port Establishment Documents");
        stepTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        VBox docList = new VBox(12,
                docCard("📄 Port Establishment Proposal Document (.pdf)", "Hazira_Port_Proposal_2026.pdf", "Uploaded"),
                docCard("📄 Environmental Impact & Safety Clearance (.pdf)", "EIA_Clearance_Hazira.pdf", "Uploaded"),
                docCard("📄 Maritime Authority Authorization Letter (.pdf)", "Maritime_Auth_Letter.pdf", "Uploaded")
        );

        Button backBtn = new Button("← Back");
        backBtn.setStyle(secondaryBtnStyle());
        backBtn.setOnAction(e -> {
            currentStep = 2;
            renderStepper();
            renderStepContent();
        });

        Button nextBtn = new Button("Next: Review & Submit →");
        nextBtn.setStyle(primaryBtnStyle());
        nextBtn.setOnAction(e -> {
            currentStep = 4;
            renderStepper();
            renderStepContent();
        });

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox btnRow = new HBox(12, backBtn, space, nextBtn);

        card.getChildren().addAll(stepTitle, docList, btnRow);
    }

    // Step 4: Review & Submit
    private void renderStep4(VBox card) {
        Label stepTitle = new Label("Step 4: Review & Submit Request");
        stepTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        String pName = portNameField.getText().trim().isEmpty() ? "Port of Hazira" : portNameField.getText().trim();
        String pCode = portCodeField.getText().trim().isEmpty() ? "IN-HAZ-01" : portCodeField.getText().trim();
        String pLoc = locationField.getText().trim().isEmpty() ? "Hazira, Gujarat" : locationField.getText().trim();
        String pAddr = addressField.getText().trim().isEmpty() ? "Hazira Complex, Surat" : addressField.getText().trim();

        String paName = paNameField.getText().trim().isEmpty() ? "Rajesh Varma" : paNameField.getText().trim();
        String paEmail = paEmailField.getText().trim().isEmpty() ? "pa_hazira@docktech.com" : paEmailField.getText().trim();
        String paPhone = paPhoneField.getText().trim().isEmpty() ? "+91 98765 11223" : paPhoneField.getText().trim();

        String rName = reqNameField.getText().trim().isEmpty() ? "Vikram Shah" : reqNameField.getText().trim();
        String rEmail = reqEmailField.getText().trim().isEmpty() ? "vikram@haziraport.com" : reqEmailField.getText().trim();
        String rPhone = reqPhoneField.getText().trim().isEmpty() ? "+91 98123 45678" : reqPhoneField.getText().trim();

        VBox summaryBox = new VBox(12);
        summaryBox.setPadding(new Insets(16));
        summaryBox.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        summaryBox.getChildren().addAll(
                summaryRow("⚓  Port Name & Code", pName + " (" + pCode + ")"),
                summaryRow("📍  Location & Address", pLoc + " • " + pAddr),
                summaryRow("🛡️  Proposed Port Authority", paName + " <" + paEmail + "> (" + paPhone + ")"),
                summaryRow("👤  Requester Contact", rName + " <" + rEmail + "> (" + rPhone + ")"),
                summaryRow("📄  Attached Documents", "3 Documents Verified & Attached")
        );

        Label notice = new Label("● Status will be set to PENDING upon submission. Dock Tech Admin review and approval is required before port creation or Port Authority provisioning.");
        notice.setStyle("-fx-background-color: #FFF7ED; -fx-text-fill: #C2410C; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 10px 14px; -fx-background-radius: 8px;");

        Button backBtn = new Button("← Back");
        backBtn.setStyle(secondaryBtnStyle());
        backBtn.setOnAction(e -> {
            currentStep = 3;
            renderStepper();
            renderStepContent();
        });

        Button submitBtn = new Button("SUBMIT PORT REQUEST (PENDING ADMIN APPROVAL)");
        submitBtn.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-cursor: hand;");

        submitBtn.setOnAction(e -> {
            com.projectui.dao.PortRequestDao requestDao = new com.projectui.dao.PortRequestDao();
            String reqId = "NPR-" + (int)(Math.random() * 8999 + 1000);

            requestDao.savePortRequest(
                    reqId, pName, pCode, pLoc, pAddr, "New Port Proposal",
                    rName, rEmail, rPhone, paName, paEmail, "Hazira_Proposal.pdf, EIA_Clearance.pdf"
            );

            card.getChildren().clear();
            VBox successBox = new VBox(16);
            successBox.setAlignment(Pos.CENTER);
            successBox.setPadding(new Insets(30));

            Label icon = new Label("✓");
            icon.setStyle("-fx-background-color:#DCFCE7; -fx-text-fill:#16A34A; -fx-font-size:32px; -fx-font-weight:bold; -fx-alignment:center; -fx-min-width:64; -fx-min-height:64; -fx-background-radius:999;");

            Label msgTitle = new Label("Request Submitted Successfully");
            msgTitle.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#0F172A;");

            VBox detailsBox = new VBox(8);
            detailsBox.setAlignment(Pos.CENTER);
            detailsBox.setPadding(new Insets(16));
            detailsBox.setStyle("-fx-background-color:#F8FAFC; -fx-border-color:#E2E8F0; -fx-border-radius:10px; -fx-background-radius:10px;");

            Label reqIdLbl = new Label("Request ID: " + reqId);
            reqIdLbl.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#0B63FF;");

            Label statusLbl = new Label("Status: PENDING");
            statusLbl.setStyle("-fx-background-color:#FFF7ED; -fx-text-fill:#C2410C; -fx-font-size:12px; -fx-font-weight:bold; -fx-padding:4 12; -fx-background-radius:12;");

            Label msgSub = new Label("Your New Port Request has been submitted to\nDock Tech Admin for verification and approval.\nYou will receive an email when the request\nis approved or rejected.");
            msgSub.setStyle("-fx-font-size:14px; -fx-text-fill:#64748B; -fx-text-alignment:center; -fx-line-spacing:3px;");

            detailsBox.getChildren().addAll(reqIdLbl, statusLbl, msgSub);

            Button returnBtn = new Button("← Return to Login");
            returnBtn.setStyle(primaryBtnStyle());
            returnBtn.setOnAction(evt -> new LoginPage().show(stage));

            successBox.getChildren().addAll(icon, msgTitle, detailsBox, returnBtn);
            card.getChildren().add(successBox);
        });

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox btnRow = new HBox(12, backBtn, space, submitBtn);

        card.getChildren().addAll(stepTitle, summaryBox, notice, btnRow);
    }

    private HBox docCard(String name, String fileName, String status) {
        HBox row = new HBox(12);
        row.setPadding(new Insets(14));
        row.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        row.setAlignment(Pos.CENTER_LEFT);

        VBox text = new VBox(2,
                new Label(name),
                new Label("File: " + fileName)
        );

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label badge = new Label("✓ " + status);
        badge.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #15803D; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4px 10px; -fx-background-radius: 12px;");

        row.getChildren().addAll(text, space, badge);
        return row;
    }

    private HBox summaryRow(String label, String val) {
        HBox r = new HBox(10);
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #64748B;");
        Label v = new Label(val);
        v.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        r.getChildren().addAll(l, space, v);
        return r;
    }

    private VBox formGroup(String labelText, javafx.scene.Node field) {
        VBox group = new VBox(6);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        group.getChildren().addAll(label, field);
        return group;
    }

    private TextField createField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(42);
        tf.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 0 12px; -fx-font-size: 13px;");
        return tf;
    }

    private TextArea createTextArea(String prompt) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setPrefRowCount(3);
        ta.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CBD5E1; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 8px 12px; -fx-font-size: 13px;");
        return ta;
    }

    private String primaryBtnStyle() {
        return "-fx-background-color: " + BLUE + "; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10px 18px; -fx-background-radius: 8px; -fx-cursor: hand;";
    }

    private String secondaryBtnStyle() {
        return "-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10px 18px; -fx-background-radius: 8px; -fx-cursor: hand;";
    }
}
