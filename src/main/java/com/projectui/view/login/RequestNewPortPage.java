package com.projectui.view.login;

import java.util.LinkedHashMap;
import java.util.Map;

import com.projectui.view.admin.AdminDataStore;
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
    private final TextField latField = createField("e.g. 21.1122");
    private final TextField lngField = createField("e.g. 72.6411");
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

    private com.projectui.service.CloudinaryService.DocumentMetadata aadhaarDocMeta = new com.projectui.service.CloudinaryService.DocumentMetadata("Aadhaar_Card.pdf", "https://res.cloudinary.com/snmxmesm/raw/upload/v1724800000/docktech_port_documents/Aadhaar_Card.pdf", "doc_aadhaar_01", "pdf", null);
    private com.projectui.service.CloudinaryService.DocumentMetadata supportingDocMeta = new com.projectui.service.CloudinaryService.DocumentMetadata("Port_Proposal_Clearance.pdf", "https://res.cloudinary.com/snmxmesm/raw/upload/v1724800000/docktech_port_documents/Port_Proposal_Clearance.pdf", "doc_proposal_01", "pdf", null);

    private Label errorLabel;

    {
        locationField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !locationField.getText().trim().isEmpty()) {
                if (latField.getText().trim().isEmpty() || lngField.getText().trim().isEmpty()) {
                    handleGetCoordinates(null);
                }
            }
        });
    }

    public void show(Stage stage) {
        this.stage = stage;

        BorderPane root = new BorderPane();

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

        HBox topHeader = DashboardLayoutHelper.createHeader(
                stage,
                "Request New Port",
                "Dock Tech / Port Operations / Request New Port",
                "Requester",
                () -> new LoginPage().show(stage),
                null
        );

        VBox content = new VBox(24);
        content.setPadding(new Insets(28, 32, 36, 32));
        content.setStyle("-fx-background-color: " + BG_COLOR + ";");

        VBox headerBanner = new VBox(6);
        Label title = new Label("Establish a New Port");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        Label subtitle = new Label("Submit a formal port proposal for Dock Tech Admin review and Port Authority account provisioning.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT_MUTED + ";");
        headerBanner.getChildren().addAll(title, subtitle);

        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill:#ef4444; -fx-font-size:13px; -fx-font-weight:bold;");
        errorLabel.setVisible(false);

        renderStepper();
        renderStepContent();

        content.getChildren().addAll(headerBanner, errorLabel, stepperRow, stepContentContainer);

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

        Button getCoordsBtn = new Button("📍 Get Coordinates");
        getCoordsBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8px 14px; -fx-background-radius: 6px; -fx-cursor: hand;");

        HBox locationBox = new HBox(8, locationField, getCoordsBtn);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(locationField, Priority.ALWAYS);

        getCoordsBtn.setOnAction(e -> handleGetCoordinates(getCoordsBtn));
        locationField.setOnAction(e -> handleGetCoordinates(getCoordsBtn));

        VBox grid = new VBox(14,
                new HBox(14, formGroup("Port Name *", portNameField), formGroup("Port Code", portCodeField)),
                new HBox(14, formGroup("Location / City *", locationBox), formGroup("Port Capacity (%)", capacityField)),
                new HBox(14, formGroup("Latitude (-90 to +90) *", latField), formGroup("Longitude (-180 to +180) *", lngField)),
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
            String pName = portNameField.getText().trim();
            String pLoc = locationField.getText().trim();
            if (pName.isEmpty() || pLoc.isEmpty()) {
                showError("⚠️ Port Name and Location are required.");
                return;
            }

            String latText = latField.getText().trim();
            String lngText = lngField.getText().trim();
            if (latText.isEmpty() || lngText.isEmpty()) {
                showError("⚠️ Please enter or click 'Get Coordinates' to retrieve Latitude and Longitude.");
                return;
            }
            try {
                double lat = Double.parseDouble(latText);
                if (lat < -90.0 || lat > 90.0) {
                    showError("⚠️ Latitude must be between -90 and +90 degrees.");
                    return;
                }
            } catch (NumberFormatException ex) {
                showError("⚠️ Invalid Latitude numeric format.");
                return;
            }
            try {
                double lng = Double.parseDouble(lngText);
                if (lng < -180.0 || lng > 180.0) {
                    showError("⚠️ Longitude must be between -180 and +180 degrees.");
                    return;
                }
            } catch (NumberFormatException ex) {
                showError("⚠️ Invalid Longitude numeric format.");
                return;
            }

            clearError();
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
        Label stepTitle = new Label("Step 3: Port Establishment Verification Documents");
        stepTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        Label notice = new Label("Upload documents using preset 'docktech_port_documents'. Files will be stored securely on Cloudinary.");
        notice.setStyle("-fx-text-fill: #0b63ff; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label aadhaarStatusLbl = new Label("Status: " + (aadhaarDocMeta.cloudinaryUrl.isEmpty() ? "Not uploaded" : "✓ Uploaded (" + aadhaarDocMeta.fileName + ")"));
        aadhaarStatusLbl.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 12px; -fx-font-weight: bold;");

        Button uploadAadhaarBtn = new Button("📤 Choose & Upload Aadhaar Card");
        uploadAadhaarBtn.setStyle(primaryBtnStyle());
        uploadAadhaarBtn.setOnAction(e -> {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Select Aadhaar Card File");
            chooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Document Files", "*.pdf", "*.jpg", "*.jpeg", "*.png")
            );
            java.io.File selected = chooser.showOpenDialog(card.getScene().getWindow());
            if (selected != null) {
                uploadAadhaarBtn.setText("⏳ Uploading to Cloudinary...");
                uploadAadhaarBtn.setDisable(true);
                com.projectui.service.CloudinaryService.uploadDocument(selected, "NPR-REQ", "aadhaar").whenComplete((meta, err) -> javafx.application.Platform.runLater(() -> {
                    uploadAadhaarBtn.setDisable(false);
                    uploadAadhaarBtn.setText("📤 Choose & Upload Aadhaar Card");
                    if (meta != null && err == null) {
                        this.aadhaarDocMeta = meta;
                        aadhaarStatusLbl.setText("✓ Uploaded to Cloudinary: " + meta.fileName);
                    } else if (err != null) {
                        aadhaarStatusLbl.setText("❌ Upload failed: " + err.getMessage());
                    }
                }));
            }
        });

        VBox aadhaarCardBox = new VBox(8, new Label("1. Aadhaar Card Verification *"), uploadAadhaarBtn, aadhaarStatusLbl);
        aadhaarCardBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-padding: 14;");

        Label supportingStatusLbl = new Label("Status: " + (supportingDocMeta.cloudinaryUrl.isEmpty() ? "Not uploaded" : "✓ Uploaded (" + supportingDocMeta.fileName + ")"));
        supportingStatusLbl.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 12px; -fx-font-weight: bold;");

        Button uploadSupportingBtn = new Button("📤 Choose & Upload Port Establishment Proposal");
        uploadSupportingBtn.setStyle(primaryBtnStyle());
        uploadSupportingBtn.setOnAction(e -> {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Select Port Establishment Proposal File");
            chooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Document Files", "*.pdf", "*.jpg", "*.jpeg", "*.png")
            );
            java.io.File selected = chooser.showOpenDialog(card.getScene().getWindow());
            if (selected != null) {
                uploadSupportingBtn.setText("⏳ Uploading to Cloudinary...");
                uploadSupportingBtn.setDisable(true);
                com.projectui.service.CloudinaryService.uploadDocument(selected, "NPR-REQ", "proposal").whenComplete((meta, err) -> javafx.application.Platform.runLater(() -> {
                    uploadSupportingBtn.setDisable(false);
                    uploadSupportingBtn.setText("📤 Choose & Upload Port Establishment Proposal");
                    if (meta != null && err == null) {
                        this.supportingDocMeta = meta;
                        supportingStatusLbl.setText("✓ Uploaded to Cloudinary: " + meta.fileName);
                    } else if (err != null) {
                        supportingStatusLbl.setText("❌ Upload failed: " + err.getMessage());
                    }
                }));
            }
        });

        VBox supportingCardBox = new VBox(8, new Label("2. Port Establishment Proposal Document *"), uploadSupportingBtn, supportingStatusLbl);
        supportingCardBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-padding: 14;");

        VBox docList = new VBox(14, notice, aadhaarCardBox, supportingCardBox);

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

        double lat = 21.1122;
        double lng = 72.6411;
        try {
            if (!latField.getText().trim().isEmpty()) lat = Double.parseDouble(latField.getText().trim());
            if (!lngField.getText().trim().isEmpty()) lng = Double.parseDouble(lngField.getText().trim());
        } catch (Exception ignored) {}

        final double finalLat = lat;
        final double finalLng = lng;

        VBox summaryBox = new VBox(12);
        summaryBox.setPadding(new Insets(16));
        summaryBox.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        summaryBox.getChildren().addAll(
                summaryRow("⚓  Port Name & Code", pName + " (" + pCode + ")"),
                summaryRow("📍  Location & Address", pLoc + " • " + pAddr),
                summaryRow("🌐  Geographic Coordinates", String.format("%.4f° N, %.4f° E", finalLat, finalLng)),
                summaryRow("🛡️  Proposed Port Authority", paName + " <" + paEmail + "> (" + paPhone + ")"),
                summaryRow("👤  Requester Contact", rName + " <" + rEmail + "> (" + rPhone + ")"),
                summaryRow("📄  Aadhaar Card", aadhaarDocMeta.fileName),
                summaryRow("📁  Supporting Document", supportingDocMeta.fileName)
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

            submitBtn.setText("⏳ Submitting Request to Firestore...");
            submitBtn.setDisable(true);

            System.out.println("Saving Port Establishment Request... requestId=" + reqId + " portName=" + pName + " status=PENDING");

            requestDao.savePortRequestFull(
                    reqId, pName, pCode, pLoc, pAddr, "New Port Proposal",
                    rName, rEmail, rPhone, paName, paEmail,
                    aadhaarDocMeta.fileName + ", " + supportingDocMeta.fileName,
                    aadhaarDocMeta.fileName, aadhaarDocMeta.cloudinaryUrl, aadhaarDocMeta.publicId, aadhaarDocMeta.resourceType, aadhaarDocMeta.format, aadhaarDocMeta.uploadedAt,
                    supportingDocMeta.fileName, supportingDocMeta.cloudinaryUrl, supportingDocMeta.publicId, supportingDocMeta.resourceType, supportingDocMeta.format, supportingDocMeta.uploadedAt
            ).whenComplete((resData, err) -> javafx.application.Platform.runLater(() -> {
                if (err != null) {
                    System.err.println("Firestore save failed requestId=" + reqId + " exception=" + err.getMessage());
                    submitBtn.setText("SUBMIT PORT REQUEST (PENDING ADMIN APPROVAL)");
                    submitBtn.setDisable(false);
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR,
                            "Failed to save Port Establishment Request to Firestore: " + err.getMessage()
                    );
                    alert.showAndWait();
                    return;
                }

                System.out.println("Firestore save successful requestId=" + reqId);

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
            }));
        });

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox btnRow = new HBox(12, backBtn, space, submitBtn);

        card.getChildren().addAll(stepTitle, summaryBox, notice, btnRow);
    }

    private void handleGetCoordinates(Button btn) {
        String query = locationField.getText().trim();
        if (query.isEmpty()) {
            if (errorLabel != null) {
                errorLabel.setStyle("-fx-text-fill:#ef4444; -fx-font-size:13px; -fx-font-weight:bold;");
                showError("⚠️ Please enter a Location / City to find coordinates.");
            }
            return;
        }

        if (errorLabel != null) {
            errorLabel.setStyle("-fx-text-fill:#0284c7; -fx-font-size:13px; -fx-font-weight:bold;");
            showError("⏳ Finding coordinates for '" + query + "' via Geocoding API...");
        }
        if (btn != null) btn.setDisable(true);

        com.projectui.service.GeocodingService.getInstance().searchPortLocation(query).whenComplete((locations, err) -> {
            javafx.application.Platform.runLater(() -> {
                if (btn != null) btn.setDisable(false);
                if (err != null || locations == null || locations.isEmpty()) {
                    if (errorLabel != null) {
                        errorLabel.setStyle("-fx-text-fill:#ef4444; -fx-font-size:13px; -fx-font-weight:bold;");
                        showError("⚠️ Location not found. Please enter a valid city or location (e.g. Gujarat, India).");
                    }
                } else {
                    com.projectui.service.GeocodingService.GeocodedLocation loc = locations.get(0);
                    latField.setText(String.format(java.util.Locale.US, "%.4f", loc.lat));
                    lngField.setText(String.format(java.util.Locale.US, "%.4f", loc.lng));
                    if (errorLabel != null) {
                        errorLabel.setStyle("-fx-text-fill:#16a34a; -fx-font-size:13px; -fx-font-weight:bold;");
                        showError("✓ Coordinates obtained: Lat " + String.format(java.util.Locale.US, "%.4f", loc.lat) + ", Lng " + String.format(java.util.Locale.US, "%.4f", loc.lng));
                    }
                }
            });
        });
    }

    private void showError(String msg) {
        if (errorLabel != null) {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
        }
    }

    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        }
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
