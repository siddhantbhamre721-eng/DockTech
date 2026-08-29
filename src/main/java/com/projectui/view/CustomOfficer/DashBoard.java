package com.projectui.view.CustomOfficer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.projectui.dao.CargoDao;
import com.projectui.dao.NotificationDao;
import com.projectui.dao.UserDao;
import com.projectui.model.Notification;
import com.projectui.view.login.DashboardLayoutHelper;
import com.projectui.view.login.LoginPage;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

@SuppressWarnings({"unchecked", "unused", "deprecation"})
public class DashBoard extends Application {

    private Stage stage;
    private BorderPane root;
    private ScrollPane centerScrollPane;
    private VBox mainContentArea;

    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private String currentPage = "Dashboard";
    private Label headerTitleLabel;
    private Label breadcrumbLabel;
    private Label notifBadgeLabel;

    private final CargoDao cargoDao = new CargoDao();
    private final UserDao userDao = new UserDao();
    private String activeAssignedPort = "Port of Mumbai";
    private String officerUid = "";
    private String officerEmail = "";
    private String officerName = "Customs Officer";

    // Dashboard UI Components for Live Refresh
    private Label pendingVerifCountLabel;
    private Label verifiedDocsCountLabel;
    private Label pendingCargoCountLabel;
    private Label approvedCargoCountLabel;
    private Label rejectedCargoCountLabel;

    // Assigned Duty Container
    private VBox assignedDutyCard;
    private VBox assignedDutyContentBox;

    // Live Activity Table
    private TableView<CustomsCargoItem> activityTable;
    private final ObservableList<CustomsCargoItem> tableMasterList = FXCollections.observableArrayList();
    private TextField searchField;
    private ComboBox<String> statusFilterCombo;

    // Clearance Breakdown Progress Bars
    private HBox clearedBarFill;
    private HBox inspectionBarFill;
    private HBox pendingBarFill;
    private HBox heldBarFill;
    private Label clearedPercentLabel;
    private Label inspectionPercentLabel;
    private Label pendingPercentLabel;
    private Label heldPercentLabel;

    // Live Alerts
    private VBox alertsListBox;
    private final List<String> liveNotificationList = new ArrayList<>();
    private final List<Notification> officerNotifications = new ArrayList<>();

    private Timeline autoSyncTimeline;
    private Runnable logoutAction;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        Parent r = getRoot(stage, null);
        stage.setTitle("Dock Tech - Customs Officer Dashboard (" + activeAssignedPort + ")");
        LoginPage.showRoot(stage, r);
    }

    public Parent getRoot(Stage stage, Runnable logoutAction) {
        this.stage = stage;
        this.logoutAction = logoutAction;
        return getRoot();
    }

    public Parent getRoot() {
        resolveOfficerContext();

        root = new BorderPane();
        root.setStyle("-fx-background-color:#f5f8fc;");

        root.setLeft(createSidebar());
        root.setTop(createHeader());

        mainContentArea = new VBox(18);
        mainContentArea.setPadding(new Insets(20, 24, 24, 24));

        centerScrollPane = new ScrollPane();
        centerScrollPane.setContent(mainContentArea);
        centerScrollPane.setFitToWidth(true);
        centerScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        centerScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        centerScrollPane.setStyle("-fx-background-color:#f5f8fc; -fx-background:transparent; -fx-border-color:transparent;");

        root.setCenter(centerScrollPane);

        navigateTo("Dashboard", createDashboardContent(), "Dock Tech / Custom Officer / Dashboard");

        // Preserve window size and responsive behavior matching Admin Dashboard
        root.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene == null) return;
            newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                if (newWin == null) return;
                if (newWin instanceof Stage s) {
                    this.stage = s;
                    root.prefWidthProperty().bind(s.widthProperty());
                    root.prefHeightProperty().bind(s.heightProperty());
                }
            });
        });

        // Initialize Background Auto-Sync Polling (every 10 seconds)
        startAutoSyncTimeline();

        return root;
    }

    private void resolveOfficerContext() {
        if (LoginPage.loggedInUser != null) {
            Map<String, Object> u = LoginPage.loggedInUser;
            if (u.get("uid") != null) officerUid = u.get("uid").toString();
            if (u.get("email") != null) officerEmail = u.get("email").toString();
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

    // =====================================================
    // SIDEBAR (MATCHING ADMIN & PORT AUTHORITY DIMENSIONS)
    // =====================================================

    private VBox createSidebar() {
        VBox nav = new VBox(10);
        nav.setPrefWidth(215);
        nav.setPadding(new Insets(20, 12, 18, 12));
        nav.setStyle("-fx-background-color:#051029;");

        // BRAND / LOGO
        Label brand = new Label("DOCK TECH");
        brand.setStyle("-fx-text-fill:white; -fx-font-size:18px; -fx-font-weight:bold;");

        Label small = new Label("SMART PORT OPERATIONS");
        small.setStyle("-fx-text-fill:#9aaac0; -fx-font-size:10px; -fx-font-weight:bold;");

        Label logoIcon = new Label("⚓");
        logoIcon.setStyle("-fx-text-fill:white; -fx-font-size:20px; -fx-font-weight:bold; -fx-padding:0 4 0 0;");

        VBox textBox = new VBox(2, brand, small);
        HBox brandBox = new HBox(8, logoIcon, textBox);
        brandBox.setPadding(new Insets(0, 0, 22, 6));

        // CUSTOMS OFFICER NAVIGATION ITEMS
        Button dashboard = createNavButton("⌂", "Dashboard");
        Button document = createNavButton("▣", "Document Verification");
        Button cargo = createNavButton("◇", "Cargo Inspection");
        Button clearance = createNavButton("✓", "Approve / Reject Cargo");
        Button reports = createNavButton("📊", "Reports & Analytics");
        Button settings = createNavButton("⚙", "Settings");

        navButtons.clear();
        navButtons.put("Dashboard", dashboard);
        navButtons.put("Document Verification", document);
        navButtons.put("Cargo Inspection", cargo);
        navButtons.put("Approve / Reject Cargo", clearance);
        navButtons.put("Reports & Analytics", reports);
        navButtons.put("Settings", settings);

        // ROUTING
        dashboard.setOnAction(e -> navigateTo("Dashboard", createDashboardContent(), "Dock Tech / Custom Officer / Dashboard"));
        document.setOnAction(e -> navigateTo("Document Verification", new DocumentVerification().getContent(), "Dock Tech / Custom Officer / Document Verification"));
        cargo.setOnAction(e -> navigateTo("Cargo Inspection", new CargoInspection().getContent(), "Dock Tech / Custom Officer / Cargo Inspection"));
        clearance.setOnAction(e -> navigateTo("Approve / Reject Cargo", new CargoClearance().getContent(), "Dock Tech / Custom Officer / Approve / Reject Cargo"));
        reports.setOnAction(e -> navigateTo("Reports & Analytics", new ReportsAnalytics().getContent(), "Dock Tech / Custom Officer / Reports & Analytics"));
        settings.setOnAction(e -> navigateTo("Settings", new SettingsPage().getContent(), "Dock Tech / Custom Officer / Settings"));

        Region gap = new Region();
        VBox.setVgrow(gap, Priority.ALWAYS);

        nav.getChildren().addAll(brandBox, dashboard, document, cargo, clearance, reports, settings, gap);
        return nav;
    }

    private Button createNavButton(String icon, String title) {
        Button b = new Button(icon + "  " + title);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(40);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(0, 10, 0, 14));
        b.setStyle(getNavButtonStyle(false));

        b.setOnMouseEntered(e -> {
            if (!currentPage.equalsIgnoreCase(title)) {
                b.setStyle(getNavButtonStyle(true));
            }
        });
        b.setOnMouseExited(e -> {
            if (!currentPage.equalsIgnoreCase(title)) {
                b.setStyle(getNavButtonStyle(false));
            }
        });
        return b;
    }

    private void navigateTo(String title, Node content, String breadcrumb) {
        this.currentPage = title;
        if (headerTitleLabel != null) headerTitleLabel.setText(title);
        if (breadcrumbLabel != null) breadcrumbLabel.setText(breadcrumb);

        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(title)) {
                entry.getValue().setStyle(getActiveNavButtonStyle());
            } else {
                entry.getValue().setStyle(getNavButtonStyle(false));
            }
        }

        mainContentArea.getChildren().clear();
        if (content != null) {
            mainContentArea.getChildren().add(content);
        }
    }

    private String getNavButtonStyle(boolean hover) {
        return "-fx-background-color:" + (hover ? "rgba(15,95,255,0.18)" : "transparent") + ";" +
                "-fx-text-fill:" + (hover ? "#ffffff" : "#cbd5e1") + ";" +
                "-fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-cursor:hand;";
    }

    private String getActiveNavButtonStyle() {
        return "-fx-background-color:#0F66D0; -fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-cursor:hand;";
    }

    // =====================================================
    // HEADER (MATCHING ADMIN & PORT AUTHORITY SIZING)
    // =====================================================

    private HBox createHeader() {
        HBox header = new HBox(14);
        header.setPrefHeight(60);
        header.setPadding(new Insets(10, 24, 10, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:white; -fx-border-color:transparent transparent #e8edf3 transparent;");

        headerTitleLabel = new Label(currentPage);
        headerTitleLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16px; -fx-font-weight:bold;");

        breadcrumbLabel = new Label("Dock Tech / Custom Officer / Dashboard");
        breadcrumbLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px;");

        VBox titleBox = new VBox(2, headerTitleLabel, breadcrumbLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Operational Status Pill
        Label statusPill = new Label("● Operational — " + activeAssignedPort);
        statusPill.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0369a1; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:5 12; -fx-background-radius:14;");

        // Live Clock
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm:ss a");
        Label timeLabel = new Label(LocalDateTime.now().format(timeFmt));
        timeLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px; -fx-padding:0 6 0 6;");

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            try {
                timeLabel.setText(LocalDateTime.now().format(timeFmt));
            } catch (Exception ignored) {}
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        // Notification Bell with dynamic badge
        StackPane bellStack = new StackPane();
        Label bell = new Label("🔔");
        bell.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16px; -fx-cursor:hand;");

        notifBadgeLabel = new Label("0");
        notifBadgeLabel.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-size:9px; -fx-font-weight:bold; -fx-background-radius:8; -fx-padding:1 4;");
        notifBadgeLabel.setTranslateX(8);
        notifBadgeLabel.setTranslateY(-8);
        notifBadgeLabel.setVisible(false);

        bellStack.getChildren().addAll(bell, notifBadgeLabel);
        bellStack.setCursor(javafx.scene.Cursor.HAND);
        bellStack.setOnMouseClicked(e -> showNotificationsModal());

        // Circular profile avatar button showing initials (matching Admin)
        String initials = "CO";
        String officerPhone = "+91 98765 43210";

        if (LoginPage.loggedInUser != null) {
            Map<String, Object> u = LoginPage.loggedInUser;
            if (u.get("phone") != null) officerPhone = u.get("phone").toString();

            String[] parts = officerName.trim().split("\\s+");
            if (parts.length > 0 && !parts[0].isEmpty()) {
                String a = parts[0].substring(0, 1);
                String b = parts.length > 1 && !parts[parts.length - 1].isEmpty() ? parts[parts.length - 1].substring(0, 1) : "";
                initials = (a + b).toUpperCase();
            }
        }

        final String finalName = officerName;
        final String finalEmail = officerEmail;
        final String finalPhone = officerPhone;
        final String finalInitials = initials;

        Button profile = new Button(initials);
        profile.setStyle("-fx-background-color:#0F66D0; -fx-text-fill:white; -fx-font-size:14; -fx-font-weight:bold; -fx-background-radius:999; -fx-min-width:38; -fx-min-height:38; -fx-pref-width:38; -fx-pref-height:38; -fx-cursor:hand;");

        profile.setOnAction(evt -> {
            // Build compact popup dropdown matching Admin implementation
            Popup popup = new Popup();
            popup.setAutoHide(true);

            VBox box = new VBox(8);
            box.setStyle("-fx-background-color:white; -fx-padding:14; -fx-background-radius:10; -fx-effect: dropshadow(gaussian, rgba(14,30,70,0.12), 8, 0.5, 0, 3);");

            // Header with circular avatar and labels
            Label bigAvatar = new Label(finalInitials);
            bigAvatar.setStyle("-fx-background-color:#0F66D0; -fx-text-fill:white; -fx-font-size:18; -fx-font-weight:bold; -fx-alignment:center; -fx-min-width:48; -fx-min-height:48; -fx-background-radius:999;");
            Label nameLbl = new Label(finalName.isBlank() ? "Customs Officer" : finalName);
            nameLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:15; -fx-font-weight:bold;");
            Label roleLbl = new Label("Customs Officer — " + activeAssignedPort);
            roleLbl.setStyle("-fx-text-fill:#6b7280; -fx-font-size:13;");

            HBox headerBox = new HBox(10, bigAvatar, new VBox(2, nameLbl, roleLbl));
            headerBox.setAlignment(Pos.CENTER_LEFT);

            // Menu options matching Admin
            Button viewProfile = new Button("👤  View Profile");
            viewProfile.setStyle("-fx-background-color:transparent; -fx-text-fill:#0f172a; -fx-font-size:14; -fx-alignment:center-left; -fx-padding:8 6; -fx-cursor:hand;");
            viewProfile.setOnAction(ae -> {
                popup.hide();
                Window owner = profile.getScene() == null ? null : profile.getScene().getWindow();
                DashboardLayoutHelper.showCustomPopup(
                        owner,
                        finalName,
                        finalEmail,
                        finalPhone,
                        "Custom Officer",
                        activeAssignedPort,
                        "CO-" + Math.abs(finalEmail.hashCode() % 899 + 100),
                        "Active",
                        finalInitials
                );
            });

            Button changePass = new Button("🔒  Change Password");
            changePass.setStyle("-fx-background-color:transparent; -fx-text-fill:#0f172a; -fx-font-size:14; -fx-alignment:center-left; -fx-padding:8 6; -fx-cursor:hand;");
            changePass.setOnAction(ae -> {
                popup.hide();
                navigateTo("Settings", new SettingsPage().getContent(), "Dock Tech / Custom Officer / Settings");
            });

            Button settingsBtn = new Button("⚙  Settings");
            settingsBtn.setStyle("-fx-background-color:transparent; -fx-text-fill:#0f172a; -fx-font-size:14; -fx-alignment:center-left; -fx-padding:8 6; -fx-cursor:hand;");
            settingsBtn.setOnAction(ae -> {
                popup.hide();
                navigateTo("Settings", new SettingsPage().getContent(), "Dock Tech / Custom Officer / Settings");
            });

            Separator sep = new Separator();

            Button logoutBtn = new Button("🚪  Logout");
            logoutBtn.setStyle("-fx-background-color:transparent; -fx-text-fill:#ef4444; -fx-font-size:14; -fx-alignment:center-left; -fx-padding:8 6; -fx-cursor:hand; -fx-font-weight:bold;");
            logoutBtn.setOnAction(ae -> {
                popup.hide();
                showLogoutConfirmation();
            });

            box.getChildren().addAll(headerBox, viewProfile, changePass, settingsBtn, sep, logoutBtn);
            popup.getContent().add(box);

            // Position popup below avatar, right-aligned
            Bounds b = profile.localToScreen(profile.getBoundsInLocal());
            double popupWidth = 220;
            double x = b.getMaxX() - popupWidth;
            double y = b.getMaxY() + 6;
            popup.show(profile.getScene().getWindow(), x, y);
        });

        header.getChildren().addAll(titleBox, spacer, statusPill, timeLabel, bellStack, profile);
        return header;
    }

    private void showLogoutConfirmation() {
        Stage ownerStage = null;
        if (root != null && root.getScene() != null && root.getScene().getWindow() instanceof Stage s) {
            ownerStage = s;
        } else if (stage != null) {
            ownerStage = stage;
        }
        final Stage finalOwnerStage = ownerStage;
        com.projectui.view.login.LogoutModal.show(ownerStage, () -> {
            if (autoSyncTimeline != null) {
                autoSyncTimeline.stop();
            }
            if (logoutAction != null) {
                logoutAction.run();
            } else if (finalOwnerStage != null) {
                new LoginPage().start(finalOwnerStage);
            }
        });
    }

    // =====================================================
    // MAIN DASHBOARD CONTENT (CUSTOMS OFFICER SPECIFIC)
    // =====================================================

    private VBox createDashboardContent() {
        VBox container = new VBox(18);

        // TOP BANNER
        HBox banner = new HBox(12);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(14, 18, 14, 18));
        banner.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        Label bannerTitle = new Label("Customs Verification & Cargo Inspection Overview");
        bannerTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16px; -fx-font-weight:bold;");

        Label bannerSub = new Label("Live regulatory oversight, declaration audits, and container clearance for " + activeAssignedPort);
        bannerSub.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");

        VBox bannerText = new VBox(3, bannerTitle, bannerSub);

        Region bannerSpacer = new Region();
        HBox.setHgrow(bannerSpacer, Priority.ALWAYS);

        Button refreshBtn = new Button("↻ Refresh Data");
        refreshBtn.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#0f172a; -fx-font-size:12px; -fx-font-weight:bold; -fx-background-radius:6; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-padding:6 12; -fx-cursor:hand;");
        refreshBtn.setOnAction(e -> refreshDashboardData());

        banner.getChildren().addAll(bannerText, bannerSpacer, refreshBtn);

        // ASSIGNED DUTIES SECTION (FROM PORT AUTHORITY)
        assignedDutyCard = createAssignedDutySection();

        // SUMMARY METRIC CARDS
        HBox summaryCards = createSummaryCards();

        // MAIN ACTIVITY TABLE & SECTION
        VBox tableSection = createActivityTableSection();

        // BOTTOM OPERATIONAL SECTION (Clearance Breakdown & Real-Time Alerts)
        HBox bottomSection = createBottomAnalyticsAndAlerts();

        container.getChildren().addAll(banner, assignedDutyCard, summaryCards, tableSection, bottomSection);

        // Load Real Database Records
        refreshDashboardData();
        return container;
    }

    // =====================================================
    // ASSIGNED DUTIES SECTION (LIVE SYNC WITH PORT AUTHORITY)
    // =====================================================

    private VBox createAssignedDutySection() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:white; -fx-border-color:#0284c7; -fx-border-width:1.5; -fx-border-radius:8; -fx-background-radius:8; -fx-effect:dropshadow(gaussian,rgba(2,132,199,0.06),10,0,0,3);");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label badge = new Label("PORT AUTHORITY ASSIGNMENT");
        badge.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0369a1; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:10;");

        Label title = new Label("Assigned Operational Duty & Task");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14px; -fx-font-weight:bold;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label jurisdiction = new Label("Jurisdiction: " + activeAssignedPort);
        jurisdiction.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px;");

        topRow.getChildren().addAll(badge, title, sp, jurisdiction);

        assignedDutyContentBox = new VBox(10);
        card.getChildren().addAll(topRow, assignedDutyContentBox);

        return card;
    }

    private void renderAssignedDutyContent(Map<String, Object> profile) {
        if (assignedDutyContentBox == null) return;
        assignedDutyContentBox.getChildren().clear();

        String duty = profile.get("duty") != null ? profile.get("duty").toString() : "Not Assigned";
        String assignedShip = profile.get("assignedShip") != null ? profile.get("assignedShip").toString() : "General Port Area";
        String assignStatus = profile.get("assignmentStatus") != null ? profile.get("assignmentStatus").toString() : "Standby";
        String assignedBy = profile.get("assignedBy") != null ? profile.get("assignedBy").toString() : (activeAssignedPort + " Authority");
        String updatedAt = profile.get("updatedAt") != null ? profile.get("updatedAt").toString() : "Recent";
        String priority = profile.get("dutyPriority") != null ? profile.get("dutyPriority").toString() : "High Priority";

        boolean hasActiveDuty = !duty.isBlank() && !duty.equalsIgnoreCase("Not Assigned") && !duty.equalsIgnoreCase("Standby");

        if (hasActiveDuty) {
            HBox dutyRow = new HBox(16);
            dutyRow.setAlignment(Pos.CENTER_LEFT);
            dutyRow.setPadding(new Insets(10, 14, 10, 14));
            dutyRow.setStyle("-fx-background-color:#f0fdf4; -fx-border-color:#bbf7d0; -fx-border-radius:6; -fx-background-radius:6;");

            VBox d1 = new VBox(2);
            Label l1 = new Label("Operational Duty");
            l1.setStyle("-fx-text-fill:#166534; -fx-font-size:10px; -fx-font-weight:bold;");
            Label v1 = new Label(duty);
            v1.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13px; -fx-font-weight:bold;");
            d1.getChildren().addAll(l1, v1);

            VBox d2 = new VBox(2);
            Label l2 = new Label("Target Vessel / Area");
            l2.setStyle("-fx-text-fill:#166534; -fx-font-size:10px; -fx-font-weight:bold;");
            Label v2 = new Label(assignedShip);
            v2.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13px; -fx-font-weight:bold;");
            d2.getChildren().addAll(l2, v2);

            VBox d3 = new VBox(2);
            Label l3 = new Label("Assigned By");
            l3.setStyle("-fx-text-fill:#166534; -fx-font-size:10px; -fx-font-weight:bold;");
            Label v3 = new Label(assignedBy);
            v3.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13px; -fx-font-weight:bold;");
            d3.getChildren().addAll(l3, v3);

            VBox d4 = new VBox(2);
            Label l4 = new Label("Priority & Status");
            l4.setStyle("-fx-text-fill:#166534; -fx-font-size:10px; -fx-font-weight:bold;");
            Label v4 = new Label(priority + " • " + assignStatus);
            v4.setStyle("-fx-text-fill:#15803d; -fx-font-size:13px; -fx-font-weight:bold;");
            d4.getChildren().addAll(l4, v4);

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Button ackBtn = new Button("Mark In Progress");
            ackBtn.setStyle("-fx-background-color:#0284c7; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:6 12; -fx-cursor:hand;");
            ackBtn.setOnAction(e -> {
                UserDao.assignPersonnelDutyExtended(officerUid, assignedShip, duty, "In Progress", assignedBy, priority, null);
                refreshDashboardData();
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Duty Acknowledged");
                a.setHeaderText("Duty Status Updated");
                a.setContentText("Operational Duty '" + duty + "' is now IN PROGRESS under " + activeAssignedPort + ".");
                a.showAndWait();
            });

            Button completeBtn = new Button("Complete Duty");
            completeBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:6 12; -fx-cursor:hand;");
            completeBtn.setOnAction(e -> {
                UserDao.assignPersonnelDutyExtended(officerUid, "General Port Duty", "Standby / Available", "Available", assignedBy, "Normal", null);
                refreshDashboardData();
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Duty Completed");
                a.setHeaderText("Duty Marked as Completed");
                a.setContentText("Duty '" + duty + "' has been successfully completed and archived. Status set to Standby.");
                a.showAndWait();
            });

            dutyRow.getChildren().addAll(d1, d2, d3, d4, sp, ackBtn, completeBtn);
            assignedDutyContentBox.getChildren().add(dutyRow);
        } else {
            HBox standbyRow = new HBox(12);
            standbyRow.setAlignment(Pos.CENTER_LEFT);
            standbyRow.setPadding(new Insets(10, 14, 10, 14));
            standbyRow.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0; -fx-border-radius:6; -fx-background-radius:6;");

            Label standbyLbl = new Label("ℹ️  No active direct duty assigned by Port Authority. Officer is on general standby oversight for " + activeAssignedPort + ".");
            standbyLbl.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");

            standbyRow.getChildren().add(standbyLbl);
            assignedDutyContentBox.getChildren().add(standbyRow);
        }
    }

    private HBox createSummaryCards() {
        HBox row = new HBox(14);

        pendingVerifCountLabel = new Label("0");
        verifiedDocsCountLabel = new Label("0");
        pendingCargoCountLabel = new Label("0");
        approvedCargoCountLabel = new Label("0");
        rejectedCargoCountLabel = new Label("0");

        VBox c1 = createMetricCard("📋", "Pending Verification", pendingVerifCountLabel, "Documents awaiting review", "#f59e0b", "#fef3c7");
        VBox c2 = createMetricCard("✓", "Verified Documents", verifiedDocsCountLabel, "Successfully verified & certified", "#10b981", "#d1fae5");
        VBox c3 = createMetricCard("🔍", "Pending Inspection", pendingCargoCountLabel, "Cargo waiting for physical check", "#0284c7", "#e0f2fe");
        VBox c4 = createMetricCard("🚢", "Approved / Cleared", approvedCargoCountLabel, "Cleared for movement & export", "#16a34a", "#dcfce7");
        VBox c5 = createMetricCard("⛔", "Rejected / Held", rejectedCargoCountLabel, "Quarantined / regulatory hold", "#ef4444", "#fee2e2");

        HBox.setHgrow(c1, Priority.ALWAYS);
        HBox.setHgrow(c2, Priority.ALWAYS);
        HBox.setHgrow(c3, Priority.ALWAYS);
        HBox.setHgrow(c4, Priority.ALWAYS);
        HBox.setHgrow(c5, Priority.ALWAYS);

        row.getChildren().addAll(c1, c2, c3, c4, c5);
        return row;
    }

    private VBox createMetricCard(String icon, String title, Label countLabel, String subtext, String iconColor, String iconBg) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8; -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.03),8,0,0,2);");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size:16px; -fx-text-fill:" + iconColor + "; -fx-background-color:" + iconBg + "; -fx-background-radius:6; -fx-padding:6 10;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px; -fx-font-weight:bold;");

        HBox topRow = new HBox(8, iconLabel, titleLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        countLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:24px; -fx-font-weight:bold;");

        Label subLabel = new Label(subtext);
        subLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:11px;");

        card.getChildren().addAll(topRow, countLabel, subLabel);
        return card;
    }

    private VBox createActivityTableSection() {
        VBox section = new VBox(12);
        section.setPadding(new Insets(16));
        section.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        // Controls bar
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label tableTitle = new Label("Live Customs Work Queue & Cargo Inspection Activity");
        tableTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:15px; -fx-font-weight:bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Search Cargo ID, Vessel, Importer...");
        searchField.setPrefWidth(240);
        searchField.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-padding:6 10; -fx-font-size:12px;");
        searchField.textProperty().addListener((obs, o, n) -> applyTableFilters());

        statusFilterCombo = new ComboBox<>();
        statusFilterCombo.getItems().addAll("All Statuses", "Pending", "Under Inspection", "Cleared", "Held", "Rejected");
        statusFilterCombo.setValue("All Statuses");
        statusFilterCombo.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px;");
        statusFilterCombo.setOnAction(e -> applyTableFilters());

        topRow.getChildren().addAll(tableTitle, spacer, searchField, statusFilterCombo);

        // TABLE VIEW
        activityTable = new TableView<>();
        activityTable.setPrefHeight(280);
        activityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        activityTable.setStyle("-fx-background-color:transparent; -fx-border-color:#e2e8f0; -fx-border-radius:6; -fx-font-size:12px;");
        activityTable.setPlaceholder(new Label("No customs records awaiting inspection under " + activeAssignedPort));

        TableColumn<CustomsCargoItem, String> colId = new TableColumn<>("Cargo ID");
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cargoId));
        colId.setPrefWidth(90);

        TableColumn<CustomsCargoItem, String> colType = new TableColumn<>("Cargo Type");
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cargoType));
        colType.setPrefWidth(130);

        TableColumn<CustomsCargoItem, String> colVessel = new TableColumn<>("Vessel / Ship");
        colVessel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().vesselName));
        colVessel.setPrefWidth(130);

        TableColumn<CustomsCargoItem, String> colCompany = new TableColumn<>("Importer / Exporter");
        colCompany.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().company));
        colCompany.setPrefWidth(140);

        TableColumn<CustomsCargoItem, String> colVerif = new TableColumn<>("Verification Status");
        colVerif.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().verificationStatus));
        colVerif.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    if ("Verified".equalsIgnoreCase(item) || "Approved".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:12;");
                    } else if ("Pending".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color:#fef3c7; -fx-text-fill:#92400e; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:12;");
                    } else {
                        badge.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#991b1b; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:12;");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        colVerif.setPrefWidth(120);

        TableColumn<CustomsCargoItem, String> colInspect = new TableColumn<>("Inspection Status");
        colInspect.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().inspectionStatus));
        colInspect.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    if ("Cleared".equalsIgnoreCase(item) || "Passed".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:12;");
                    } else if ("Under Inspection".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#0369a1; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:12;");
                    } else if ("Pending".equalsIgnoreCase(item)) {
                        badge.setStyle("-fx-background-color:#fef3c7; -fx-text-fill:#92400e; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:12;");
                    } else {
                        badge.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#991b1b; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:12;");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        colInspect.setPrefWidth(120);

        TableColumn<CustomsCargoItem, String> colTime = new TableColumn<>("Last Updated");
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().lastUpdated));
        colTime.setPrefWidth(100);

        TableColumn<CustomsCargoItem, Void> colAction = new TableColumn<>("Actions");
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button clearBtn = new Button("Clear");
            private final Button holdBtn = new Button("Hold");

            {
                clearBtn.setStyle("-fx-background-color:#10b981; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");
                holdBtn.setStyle("-fx-background-color:#f59e0b; -fx-text-fill:white; -fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:4; -fx-padding:3 8; -fx-cursor:hand;");

                clearBtn.setOnAction(e -> {
                    CustomsCargoItem item = getTableView().getItems().get(getIndex());
                    updateCargoClearance(item.cargoId, "Cleared", "Verified");
                });

                holdBtn.setOnAction(e -> {
                    CustomsCargoItem item = getTableView().getItems().get(getIndex());
                    updateCargoClearance(item.cargoId, "Held", "Flagged");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox actions = new HBox(6, clearBtn, holdBtn);
                    actions.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(actions);
                }
            }
        });
        colAction.setPrefWidth(120);

        activityTable.getColumns().addAll(colId, colType, colVessel, colCompany, colVerif, colInspect, colTime, colAction);
        section.getChildren().addAll(topRow, activityTable);
        return section;
    }

    private HBox createBottomAnalyticsAndAlerts() {
        HBox row = new HBox(16);

        // CLEARANCE BREAKDOWN CARD (DYNAMIC PERCENTAGES)
        VBox breakdownCard = new VBox(12);
        breakdownCard.setPadding(new Insets(16));
        breakdownCard.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        Label card1Title = new Label("Customs Clearance Status Breakdown (" + activeAssignedPort + ")");
        card1Title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14px; -fx-font-weight:bold;");

        clearedPercentLabel = new Label("0%");
        inspectionPercentLabel = new Label("0%");
        pendingPercentLabel = new Label("0%");
        heldPercentLabel = new Label("0%");

        clearedBarFill = new HBox();
        inspectionBarFill = new HBox();
        pendingBarFill = new HBox();
        heldBarFill = new HBox();

        VBox statsList = new VBox(10);
        statsList.getChildren().addAll(
                createDynamicProgressBar("Cleared & Released Cargo", clearedPercentLabel, clearedBarFill, "#10b981"),
                createDynamicProgressBar("Under Active Physical Inspection", inspectionPercentLabel, inspectionBarFill, "#0284c7"),
                createDynamicProgressBar("Pending Declaration / Document Review", pendingPercentLabel, pendingBarFill, "#f59e0b"),
                createDynamicProgressBar("Quarantined / Regulatory Hold", heldPercentLabel, heldBarFill, "#ef4444")
        );

        breakdownCard.getChildren().addAll(card1Title, statsList);
        HBox.setHgrow(breakdownCard, Priority.ALWAYS);

        // REAL-TIME COMPLIANCE ALERTS CARD
        VBox alertsCard = new VBox(12);
        alertsCard.setPadding(new Insets(16));
        alertsCard.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        Label card2Title = new Label("Live Duty & Compliance Alerts (" + activeAssignedPort + ")");
        card2Title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:14px; -fx-font-weight:bold;");

        alertsListBox = new VBox(8);
        alertsCard.getChildren().addAll(card2Title, alertsListBox);
        HBox.setHgrow(alertsCard, Priority.ALWAYS);

        row.getChildren().addAll(breakdownCard, alertsCard);
        return row;
    }

    private HBox createDynamicProgressBar(String labelText, Label percentLabel, HBox fillBox, String barColor) {
        VBox v = new VBox(4);
        HBox labelRow = new HBox();
        Label l = new Label(labelText);
        l.setStyle("-fx-text-fill:#334155; -fx-font-size:11px; -fx-font-weight:bold;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        percentLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:11px; -fx-font-weight:bold;");
        labelRow.getChildren().addAll(l, sp, percentLabel);

        HBox barBg = new HBox();
        barBg.setPrefHeight(6);
        barBg.setStyle("-fx-background-color:#f1f5f9; -fx-background-radius:3;");

        fillBox.setPrefHeight(6);
        fillBox.setPrefWidth(0);
        fillBox.setStyle("-fx-background-color:" + barColor + "; -fx-background-radius:3;");
        barBg.getChildren().add(fillBox);

        v.getChildren().addAll(labelRow, barBg);
        return new HBox(v);
    }

    private HBox createAlertRow(String icon, String title, String detail, String bg, String textColor) {
        HBox alert = new HBox(10);
        alert.setPadding(new Insets(8, 12, 8, 12));
        alert.setAlignment(Pos.CENTER_LEFT);
        alert.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:6;");

        Label ic = new Label(icon);
        ic.setStyle("-fx-font-size:14px;");

        VBox txt = new VBox(2);
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:" + textColor + "; -fx-font-size:11px; -fx-font-weight:bold;");
        Label d = new Label(detail);
        d.setStyle("-fx-text-fill:" + textColor + "; -fx-font-size:10px;");
        txt.getChildren().addAll(t, d);

        alert.getChildren().addAll(ic, txt);
        return alert;
    }

    // =====================================================
    // LIVE BACKEND DATA INTEGRATION & SYNCHRONIZATION
    // =====================================================

    private void refreshDashboardData() {
        resolveOfficerContext();
        tableMasterList.clear();
        liveNotificationList.clear();

        // 1. Fetch Assigned Duties from User Profile (Live Sync with Port Authority)
        Map<String, Object> activeProfile = LoginPage.loggedInUser != null ? LoginPage.loggedInUser : userDao.getCachedUserProfile(officerUid, officerEmail);
        if (activeProfile == null) activeProfile = new java.util.HashMap<>();
        renderAssignedDutyContent(activeProfile);

        String duty = activeProfile.get("duty") != null ? activeProfile.get("duty").toString() : "";
        if (!duty.isBlank() && !duty.equalsIgnoreCase("Not Assigned") && !duty.equalsIgnoreCase("Standby")) {
            liveNotificationList.add("Port Authority Assigned Duty: " + duty + " (" + activeAssignedPort + ")");
        }
        updateNotificationBadge();

        // 2. Fetch Cargo Records for Port
        int pendingVerifCount = 0;
        int verifiedDocsCount = 0;
        int pendingCargoCount = 0;
        int approvedCargoCount = 0;
        int rejectedCargoCount = 0;

        String normPort = UserDao.normalizePort(activeAssignedPort).toLowerCase();

        List<Map<String, Object>> allCargos = cargoDao.getAllCargos();
        if (allCargos != null) {
            for (Map<String, Object> c : allCargos) {
                if (c == null) continue;

                String origin = c.get("originPort") != null ? c.get("originPort").toString() : "";
                String dest = c.get("destinationPort") != null ? c.get("destinationPort").toString() : "";
                String originNorm = UserDao.normalizePort(origin).toLowerCase();
                String destNorm = UserDao.normalizePort(dest).toLowerCase();

                // Check port match
                if (originNorm.contains(normPort) || destNorm.contains(normPort) || normPort.contains("mumbai")) {
                    String id = c.get("cargoId") != null ? c.get("cargoId").toString() : "CGO-1000";
                    String name = c.get("cargoName") != null ? c.get("cargoName").toString() : "General Cargo";
                    String type = c.get("cargoType") != null ? c.get("cargoType").toString() : "Containerized";
                    String status = c.get("status") != null ? c.get("status").toString() : "Pending";

                    String verifStatus = "Pending";
                    String inspectStatus = "Pending";

                    if ("Cleared".equalsIgnoreCase(status) || "Approved".equalsIgnoreCase(status) || "Registered".equalsIgnoreCase(status)) {
                        verifStatus = "Verified";
                        inspectStatus = "Cleared";
                        approvedCargoCount++;
                        verifiedDocsCount++;
                    } else if ("In Transit".equalsIgnoreCase(status) || "Under Inspection".equalsIgnoreCase(status)) {
                        verifStatus = "Verified";
                        inspectStatus = "Under Inspection";
                        pendingCargoCount++;
                    } else if ("Rejected".equalsIgnoreCase(status) || "Held".equalsIgnoreCase(status)) {
                        verifStatus = "Flagged";
                        inspectStatus = "Held";
                        rejectedCargoCount++;
                        liveNotificationList.add("Cargo Quarantined/Held: " + id + " (" + name + ")");
                    } else {
                        verifStatus = "Pending";
                        inspectStatus = "Pending";
                        pendingVerifCount++;
                    }

                    String vesselName = "MV " + activeAssignedPort.replace("Port of ", "").replace(" Port", "") + " Trader";
                    String company = c.get("company") != null ? c.get("company").toString() : "Maritime Logistics Ltd.";

                    tableMasterList.add(new CustomsCargoItem(
                            id, type + " (" + name + ")", vesselName, company, verifStatus, inspectStatus, "Today, " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                    ));
                }
            }
        }

        // Summary Counts
        if (pendingVerifCountLabel != null) pendingVerifCountLabel.setText(String.valueOf(pendingVerifCount));
        if (verifiedDocsCountLabel != null) verifiedDocsCountLabel.setText(String.valueOf(verifiedDocsCount));
        if (pendingCargoCountLabel != null) pendingCargoCountLabel.setText(String.valueOf(pendingCargoCount));
        if (approvedCargoCountLabel != null) approvedCargoCountLabel.setText(String.valueOf(approvedCargoCount));
        if (rejectedCargoCountLabel != null) rejectedCargoCountLabel.setText(String.valueOf(rejectedCargoCount));

        // Calculate Real Clearance Breakdown Percentages
        int total = approvedCargoCount + pendingCargoCount + pendingVerifCount + rejectedCargoCount;
        if (total > 0) {
            double pCleared = (double) approvedCargoCount / total;
            double pInspect = (double) pendingCargoCount / total;
            double pPending = (double) pendingVerifCount / total;
            double pHeld = (double) rejectedCargoCount / total;

            if (clearedPercentLabel != null) clearedPercentLabel.setText(String.format("%.0f%%", pCleared * 100));
            if (inspectionPercentLabel != null) inspectionPercentLabel.setText(String.format("%.0f%%", pInspect * 100));
            if (pendingPercentLabel != null) pendingPercentLabel.setText(String.format("%.0f%%", pPending * 100));
            if (heldPercentLabel != null) heldPercentLabel.setText(String.format("%.0f%%", pHeld * 100));

            if (clearedBarFill != null) clearedBarFill.setPrefWidth(220 * pCleared);
            if (inspectionBarFill != null) inspectionBarFill.setPrefWidth(220 * pInspect);
            if (pendingBarFill != null) pendingBarFill.setPrefWidth(220 * pPending);
            if (heldBarFill != null) heldBarFill.setPrefWidth(220 * pHeld);
        } else {
            if (clearedPercentLabel != null) clearedPercentLabel.setText("0%");
            if (inspectionPercentLabel != null) inspectionPercentLabel.setText("0%");
            if (pendingPercentLabel != null) pendingPercentLabel.setText("0%");
            if (heldPercentLabel != null) heldPercentLabel.setText("0%");
        }

        // Load real notifications from NotificationDao
        String uid = LoginPage.loggedInUser != null && LoginPage.loggedInUser.get("uid") != null ? LoginPage.loggedInUser.get("uid").toString() : "";
        if (!uid.isBlank()) {
            officerNotifications.clear();
            officerNotifications.addAll(new NotificationDao().getNotificationsForUser(uid));
        }

        // Render Live Alerts
        renderLiveAlerts(pendingCargoCount, rejectedCargoCount);
        updateNotificationBadge();
        applyTableFilters();
    }

    private void renderLiveAlerts(int pendingInspection, int heldCount) {
        if (alertsListBox == null) return;
        alertsListBox.getChildren().clear();

        boolean hasAlert = false;
        if (heldCount > 0) {
            alertsListBox.getChildren().add(createAlertRow("⛔", "Regulatory Quarantine Watch", heldCount + " cargo shipments placed on regulatory hold under " + activeAssignedPort + ".", "#fee2e2", "#991b1b"));
            hasAlert = true;
        }

        if (pendingInspection > 0) {
            alertsListBox.getChildren().add(createAlertRow("⚠️", "Physical Inspection Required", pendingInspection + " containers awaiting physical customs inspection at dock yard.", "#fef3c7", "#92400e"));
            hasAlert = true;
        }

        if (!hasAlert) {
            HBox noAlert = new HBox(8);
            noAlert.setPadding(new Insets(10));
            Label l = new Label("✓  No active regulatory alerts. All customs declaration queues clear.");
            l.setStyle("-fx-text-fill:#166534; -fx-font-size:11px;");
            noAlert.getChildren().add(l);
            alertsListBox.getChildren().add(noAlert);
        }
    }

    private void updateNotificationBadge() {
        if (notifBadgeLabel == null) return;
        int unread = 0;
        for (Notification n : officerNotifications) {
            if (n != null && !n.isRead()) unread++;
        }
        if (unread > 0) {
            notifBadgeLabel.setText(String.valueOf(unread));
            notifBadgeLabel.setVisible(true);
        } else if (!liveNotificationList.isEmpty()) {
            notifBadgeLabel.setText(String.valueOf(liveNotificationList.size()));
            notifBadgeLabel.setVisible(true);
        } else {
            notifBadgeLabel.setVisible(false);
        }
    }

    private void showNotificationsModal() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Customs Officer Notifications (" + activeAssignedPort + ")");
        alert.setHeaderText("Active Notifications & Port Authority Directives");

        StringBuilder sb = new StringBuilder();
        if (!officerNotifications.isEmpty()) {
            for (Notification n : officerNotifications) {
                if (n == null) continue;
                sb.append(n.getTitle()).append(" [").append(n.getTimestamp()).append("]\n");
                sb.append(n.getMessage()).append("\n\n");
            }
        } else if (!liveNotificationList.isEmpty()) {
            for (String n : liveNotificationList) {
                sb.append("• ").append(n).append("\n");
            }
        } else {
            sb.append("• No new notifications from Port Authority.\n• Customs channels operating normally.");
        }

        alert.setContentText(sb.toString().trim());

        // Mark unread as read
        NotificationDao dao = new NotificationDao();
        for (Notification n : officerNotifications) {
            if (n != null && !n.isRead() && n.getId() != null) {
                n.setRead(true);
                dao.markAsRead(n.getId());
            }
        }
        updateNotificationBadge();

        alert.showAndWait();
    }

    private void startAutoSyncTimeline() {
        if (autoSyncTimeline != null) autoSyncTimeline.stop();
        autoSyncTimeline = new Timeline(new KeyFrame(Duration.seconds(10), ev -> {
            try {
                refreshDashboardData();
            } catch (Exception ignored) {}
        }));
        autoSyncTimeline.setCycleCount(Timeline.INDEFINITE);
        autoSyncTimeline.play();
    }

    private void applyTableFilters() {
        if (activityTable == null) return;
        String query = searchField != null && searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String statusFilter = statusFilterCombo != null && statusFilterCombo.getValue() != null ? statusFilterCombo.getValue() : "All Statuses";

        ObservableList<CustomsCargoItem> filtered = FXCollections.observableArrayList();
        for (CustomsCargoItem item : tableMasterList) {
            boolean matchQuery = query.isEmpty() ||
                    item.cargoId.toLowerCase().contains(query) ||
                    item.cargoType.toLowerCase().contains(query) ||
                    item.vesselName.toLowerCase().contains(query) ||
                    item.company.toLowerCase().contains(query);

            boolean matchStatus = "All Statuses".equalsIgnoreCase(statusFilter) ||
                    item.inspectionStatus.equalsIgnoreCase(statusFilter) ||
                    item.verificationStatus.equalsIgnoreCase(statusFilter);

            if (matchQuery && matchStatus) {
                filtered.add(item);
            }
        }
        activityTable.setItems(filtered);
    }

    private void updateCargoClearance(String cargoId, String newInspectionStatus, String newVerifStatus) {
        cargoDao.saveCargo(cargoId, null, null, null, null, activeAssignedPort, null, newInspectionStatus);

        for (CustomsCargoItem item : tableMasterList) {
            if (item.cargoId.equalsIgnoreCase(cargoId)) {
                item.inspectionStatus = newInspectionStatus;
                item.verificationStatus = newVerifStatus;
                item.lastUpdated = "Just now";
                break;
            }
        }
        refreshDashboardData();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Customs Clearance Updated");
        alert.setHeaderText("Cargo " + cargoId + " status updated");
        alert.setContentText("Cargo ID: " + cargoId + "\nInspection Status: " + newInspectionStatus + "\nVerification: " + newVerifStatus + "\nPort: " + activeAssignedPort);
        alert.showAndWait();
    }

    // =====================================================
    // DATA MODEL
    // =====================================================

    public static class CustomsCargoItem {
        public String cargoId;
        public String cargoType;
        public String vesselName;
        public String company;
        public String verificationStatus;
        public String inspectionStatus;
        public String lastUpdated;

        public CustomsCargoItem(String cargoId, String cargoType, String vesselName, String company, String verificationStatus, String inspectionStatus, String lastUpdated) {
            this.cargoId = cargoId;
            this.cargoType = cargoType;
            this.vesselName = vesselName;
            this.company = company;
            this.verificationStatus = verificationStatus;
            this.inspectionStatus = inspectionStatus;
            this.lastUpdated = lastUpdated;
        }
    }
}