package com.projectui.view.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
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
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;

import com.projectui.dao.UserDao;

public class UserManagementPage extends AdminTheme {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private final UserDao userDao = new UserDao();
    private final AdminDataStore store = AdminDataStore.getInstance();
    private final List<AdminDataStore.UserAccount> users = store.users;
    private final GridPane table = new GridPane();

    // Summary Card Labels
    private Label totalValue;
    private Label activeValue;
    private Label inactiveValue;
    private Label adminValue;
    private Label portManagerValue;
    private Label pendingValue;

    // Filters
    private TextField search;
    private ComboBox<String> roleFilter;
    private ComboBox<String> statusFilter;

    // Form Controls (Right Panel)
    private Label formTitle;
    private Label formSubtitle;
    private TextField nameField;
    private TextField emailField;
    private ComboBox<String> roleCombo;
    private ComboBox<String> statusCombo;
    private VBox capabilitiesContainer;
    private Button saveBtn;
    private Button cancelBtn;
    private int editingIndex = -1;
    private final boolean startAddOnOpen;

    // Feedback Toast
    private Label toastMessage;
    private HBox toastBanner;

    public UserManagementPage() {
        this(false);
    }

    public UserManagementPage(boolean startAddOnOpen) {
        this.startAddOnOpen = startAddOnOpen;
        loadLiveUsersFromFirestore();
    }

    private void loadLiveUsersFromFirestore() {
        java.util.concurrent.ForkJoinPool.commonPool().execute(() -> {
            try {
                List<java.util.Map<String, Object>> liveList = com.projectui.dao.UserDao.getFirebaseAuthUsersWithProfiles();
                javafx.application.Platform.runLater(() -> {
                    users.clear();
                    java.util.Set<String> seenKeys = new java.util.HashSet<>();
                    if (liveList != null && !liveList.isEmpty()) {
                        for (java.util.Map<String, Object> u : liveList) {
                            String email = u.get("email") != null ? u.get("email").toString().trim().toLowerCase() : "";
                            String uid = u.get("uid") != null ? u.get("uid").toString().trim() : "";
                            String key = !email.isBlank() ? email : uid;

                            if (key.isBlank() || seenKeys.contains(key)) continue;

                            if (email.equalsIgnoreCase("pravin@gmail.com") || email.equalsIgnoreCase("goa@gmail.com")) {
                                continue;
                            }

                            String role = u.get("role") != null ? u.get("role").toString() : "Port Authority";
                            if ("Admin".equalsIgnoreCase(role) && !email.equalsIgnoreCase("babu@gmail.com") && !email.contains("admin")) {
                                continue;
                            }

                            seenKeys.add(key);

                            String name = u.get("name") != null ? u.get("name").toString() : "User";
                            String status = u.get("status") != null ? u.get("status").toString() : "Active";
                            String created = u.get("createdAt") != null ? u.get("createdAt").toString() : "Today";
                            String lastActive = u.get("updatedAt") != null ? u.get("updatedAt").toString() : "Recently";

                            users.add(new AdminDataStore.UserAccount(uid, name, email, role, status, created, lastActive));
                        }
                    }

                    boolean hasBabu = false;
                    for (AdminDataStore.UserAccount ua : users) {
                        if (ua.email != null && ua.email.equalsIgnoreCase("babu@gmail.com")) {
                            hasBabu = true;
                            ua.name = "Babu";
                            ua.role = "Admin";
                            break;
                        }
                    }
                    if (!hasBabu) {
                        users.add(0, new AdminDataStore.UserAccount("usr_" + Math.abs("babu@gmail.com".hashCode()), "Babu", "babu@gmail.com", "Admin", "Active", "01 Jan 2026, 09:00", "Just now"));
                    }

                    refreshTable();
                    refreshStats();
                    if (toastMessage != null) {
                        showToast("Live directory loaded (" + users.size() + " accounts from Firebase Auth).", true);
                    }
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    if (toastMessage != null) showToast("Unable to load user data from database.", false);
                });
            }
        });
    }

    public Parent getRoot() {
        Button addBtn = primaryButton("+  Add User");
        addBtn.setStyle(
                "-fx-background-color:" + BLUE + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-size:15;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:10 22;" +
                "-fx-background-radius:8;" +
                "-fx-cursor:hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(11,99,255,0.25), 8, 0, 0, 2);"
        );
        addBtn.setOnAction(event -> startAdd());

        VBox page = pageContent();
        page.setSpacing(18);
        page.setPadding(new Insets(18, 22, 28, 22));

        // Toast status banner
        toastBanner = createToastBanner();

        // 1. Header Section
        HBox header = createUserHeader(
                "User Management",
                "Manage system users, roles, security permissions and enterprise access controls.",
                addBtn
        );

        // 2. User Summary Cards Row (6 Cards)
        HBox summaryRow = createSummaryCardsRow();

        // 3. Middle Section: Enterprise Access Directory (Table) + Add/Edit Form Panel (Right)
        VBox tableCard = userListCard();
        VBox formCard = userFormCard();
        HBox mainRow = new HBox(16, tableCard, formCard);
        tableCard.prefWidthProperty().bind(mainRow.widthProperty().multiply(0.66));
        formCard.prefWidthProperty().bind(mainRow.widthProperty().multiply(0.34));
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        HBox.setHgrow(formCard, Priority.ALWAYS);

        // 4. Bottom Section: 2 Focused Administration Info Cards (Recent User Activities + Security & Access)
        HBox bottomRow = createBottomAdminRow();

        page.getChildren().addAll(
                header,
                toastBanner,
                summaryRow,
                mainRow,
                bottomRow
        );

        refreshTable();
        refreshStats();

        if (startAddOnOpen) {
            startAdd();
        } else if (!users.isEmpty()) {
            startEdit(0);
        }

        // Smooth ScrollPane wrapper to guarantee no cut-offs
        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background:transparent; -fx-background-color:transparent; -fx-padding:0;");

        return scrollPane;
    }

    private HBox createUserHeader(String title, String subtitle, Node action) {
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#05070c; -fx-font-size:26; -fx-font-weight:bold;");

        Label s = new Label(subtitle);
        s.setStyle("-fx-text-fill:#64748b; -fx-font-size:15;");

        VBox copy = new VBox(4, t, s);
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox header = new HBox(12, copy, space, action);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    // ==========================================
    // TOAST NOTIFICATION BANNER
    // ==========================================
    private HBox createToastBanner() {
        Label icon = new Label("ℹ");
        icon.setStyle("-fx-text-fill:" + BLUE + "; -fx-font-size:16; -fx-font-weight:bold;");

        toastMessage = new Label("Enterprise user directory loaded. 6 total accounts registered.");
        toastMessage.setStyle("-fx-text-fill:#334155; -fx-font-size:14; -fx-font-weight:bold;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label close = new Label("✕");
        close.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:14; -fx-cursor:hand;");
        close.setOnMouseClicked(e -> toastBanner.setVisible(false));

        HBox toast = new HBox(10, icon, toastMessage, space, close);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setPadding(new Insets(10, 16, 10, 16));
        toast.setStyle(
                "-fx-background-color:#eff6ff;" +
                "-fx-border-color:#bfdbfe;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;"
        );
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
        }
    }

    // ==========================================
    // 1. USER SUMMARY CARDS (6 CARDS)
    // ==========================================
    private HBox createSummaryCardsRow() {
        HBox row = new HBox(12,
                statCardWithBadge("Total Users", "0", "All Registered", "👥", "#0b63ff", "#eff6ff", l -> totalValue = l),
                statCardWithBadge("Active Users", "0", "Operational", "✓", "#16a34a", "#ecfdf5", l -> activeValue = l),
                statCardWithBadge("Inactive Users", "0", "Deactivated", "⊘", "#64748b", "#f1f5f9", l -> inactiveValue = l),
                statCardWithBadge("Administrators", "0", "Full Access", "★", "#6366f1", "#eef2ff", l -> adminValue = l),
                statCardWithBadge("Port Managers", "0", "Authority", "⚓", "#0ea5e9", "#e0f2fe", l -> portManagerValue = l),
                statCardWithBadge("Pending Users", "0", "Needs Review", "!", "#f59e0b", "#fffbeb", l -> pendingValue = l)
        );
        for (Node node : row.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
            if (node instanceof VBox) {
                ((VBox) node).setMaxWidth(Double.MAX_VALUE);
            }
        }
        return row;
    }

    private VBox statCardWithBadge(String title, String initialVal, String subText, String iconStr,
                                   String color, String bgLight, LabelSetter setter) {
        Label icon = new Label(iconStr);
        icon.setStyle(
                "-fx-text-fill:" + color + ";" +
                "-fx-font-size:16;" +
                "-fx-font-weight:bold;" +
                "-fx-alignment:center;" +
                "-fx-min-width:32;" +
                "-fx-min-height:32;" +
                "-fx-background-color:" + bgLight + ";" +
                "-fx-background-radius:6;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:14; -fx-font-weight:bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(8, icon, titleLabel, spacer);
        top.setAlignment(Pos.CENTER_LEFT);

        Label valueLabel = new Label(initialVal);
        valueLabel.setStyle("-fx-text-fill:#0f172a; -fx-font-size:28; -fx-font-weight:800;");
        setter.set(valueLabel);

        Label subLabel = new Label(subText);
        subLabel.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13;");

        VBox card = new VBox(6, top, valueLabel, subLabel);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 8, 0, 0, 2);"
        );
        return card;
    }

    // ==========================================
    // 2. ENTERPRISE ACCESS DIRECTORY (MAIN TABLE)
    // ==========================================
    private VBox userListCard() {
        Label title = new Label("Enterprise Access Directory");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");
        Label subtitle = new Label("Registered users, authentication roles, access statuses and activity timestamps");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        // Search Input
        search = new TextField();
        search.setPromptText("⌕  Search users by name, email, role...");
        search.setPrefWidth(180);
        search.setStyle(
                "-fx-background-color:#f8fafc;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;" +
                "-fx-padding:8 10 8 10;" +
                "-fx-font-size:13;"
        );
        search.textProperty().addListener((obs, oldValue, newValue) -> refreshTable());

        // Role & Status Dropdowns
        roleFilter = combo("Role", "All Roles", "Admin", "Port Authority", "Customs Officer", "Ship Captain", "Exporter", "Importer");
        statusFilter = combo("Status", "All Status", "Active", "Pending", "Deactivated");
        roleFilter.setStyle("-fx-font-size:13;");
        statusFilter.setStyle("-fx-font-size:13;");
        roleFilter.setPrefWidth(125);
        statusFilter.setPrefWidth(110);
        roleFilter.valueProperty().addListener((obs, oldValue, newValue) -> refreshTable());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> refreshTable());

        // Filter / Reset Button
        Button resetFilter = new Button("Reset");
        resetFilter.setStyle(
                "-fx-background-color:#f1f5f9;" +
                "-fx-text-fill:#475569;" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:8 12;" +
                "-fx-background-radius:6;" +
                "-fx-cursor:hand;"
        );
        resetFilter.setOnAction(e -> {
            search.clear();
            roleFilter.setValue("All Roles");
            statusFilter.setValue("All Status");
            refreshTable();
            showToast("Directory filters reset.", true);
        });

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox controls = new HBox(6, search, roleFilter, statusFilter, resetFilter);
        controls.setAlignment(Pos.CENTER_RIGHT);

        HBox top = new HBox(8, titleBox, space, controls);
        top.setAlignment(Pos.CENTER_LEFT);

        table.setHgap(0);
        table.setVgap(0);
        table.setMaxWidth(Double.MAX_VALUE);
        table.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;"
        );

        VBox card = new VBox(14, top, table);
        card.setPadding(new Insets(18));
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 10, 0, 0, 2);"
        );
        return card;
    }

    private void refreshTable() {
        table.getChildren().clear();
        table.getColumnConstraints().clear();

        // Responsive Column Proportions: User (20%), Email (28%), Role (18%), Status (12%), Last Active (12%), Actions (10%)
        javafx.scene.layout.ColumnConstraints c0 = new javafx.scene.layout.ColumnConstraints(); c0.setPercentWidth(20);
        javafx.scene.layout.ColumnConstraints c1 = new javafx.scene.layout.ColumnConstraints(); c1.setPercentWidth(28);
        javafx.scene.layout.ColumnConstraints c2 = new javafx.scene.layout.ColumnConstraints(); c2.setPercentWidth(18);
        javafx.scene.layout.ColumnConstraints c3 = new javafx.scene.layout.ColumnConstraints(); c3.setPercentWidth(12);
        javafx.scene.layout.ColumnConstraints c4 = new javafx.scene.layout.ColumnConstraints(); c4.setPercentWidth(12);
        javafx.scene.layout.ColumnConstraints c5 = new javafx.scene.layout.ColumnConstraints(); c5.setPercentWidth(10);
        table.getColumnConstraints().addAll(c0, c1, c2, c3, c4, c5);

        // 6 Columns: User, Email, Role, Status, Last Active, Actions
        String[] headers = {"User", "Email Address", "Assigned Role", "Status", "Last Active", "Actions"};

        for (int col = 0; col < headers.length; col++) {
            Label headCell = directoryHeadCell(headers[col]);
            table.add(headCell, col, 0);
        }

        int displayRow = 1;
        for (int row = 0; row < users.size(); row++) {
            AdminDataStore.UserAccount user = users.get(row);
            if (!matches(user)) {
                continue;
            }

            boolean isAlt = (displayRow % 2 == 0);

            // 1. User with avatar
            HBox userCell = createUserAvatarCell(user.name, isAlt);
            // 2. Email
            Label emailCell = createDirTextCell(user.email, isAlt);
            // 3. Role Tag
            HBox roleCell = createRoleTagCell(user.role, isAlt);
            // 4. Status Badge
            HBox statusCell = createStatusBadgeCell(user.status, isAlt);
            // 5. Last Active (Live Relative Time)
            Label lastActiveCell = createDirTextCell(getRelativeTime(user.lastLogin), isAlt);
            // 6. Action Cell (Edit + More Menu)
            HBox actionCell = createDirectoryActionCell(row, isAlt);

            table.add(userCell, 0, displayRow);
            table.add(emailCell, 1, displayRow);
            table.add(roleCell, 2, displayRow);
            table.add(statusCell, 3, displayRow);
            table.add(lastActiveCell, 4, displayRow);
            table.add(actionCell, 5, displayRow);

            displayRow++;
        }

        if (displayRow == 1) {
            Label empty = new Label("No users found matching filter criteria.");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setMinHeight(60);
            empty.setAlignment(Pos.CENTER);
            empty.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:14; -fx-font-style:italic; -fx-background-color:white;");
            table.add(empty, 0, 1, 6, 1);
        }
    }

    private Label directoryHeadCell(String text) {
        Label cell = new Label(text);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMinHeight(44);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 8, 0, 8));
        cell.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        cell.setStyle(
                "-fx-background-color:#f8fafc;" +
                "-fx-text-fill:#0f172a;" +
                "-fx-font-size:13;" +
                "-fx-font-weight:bold;" +
                "-fx-border-color:#e2e8f0; -fx-border-width:0 0 1 0;"
        );
        return cell;
    }

    private HBox createUserAvatarCell(String userName, boolean isAlt) {
        String initials = "U";
        if (userName != null && !userName.isBlank()) {
            String[] parts = userName.trim().split("\\s+");
            String a = parts[0].substring(0, 1);
            String b = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
            initials = (a + b).toUpperCase();
        }

        Label avatar = new Label(initials);
        avatar.setStyle(
                "-fx-background-color:#eff6ff;" +
                "-fx-text-fill:" + BLUE + ";" +
                "-fx-font-size:11;" +
                "-fx-font-weight:bold;" +
                "-fx-alignment:center;" +
                "-fx-min-width:28;" +
                "-fx-min-height:28;" +
                "-fx-max-width:28;" +
                "-fx-max-height:28;" +
                "-fx-background-radius:999;"
        );

        Label nameLbl = new Label(userName);
        nameLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");
        nameLbl.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLbl, Priority.ALWAYS);
        nameLbl.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        javafx.scene.control.Tooltip.install(nameLbl, new javafx.scene.control.Tooltip(userName));

        HBox cell = new HBox(6, avatar, nameLbl);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMinHeight(48);
        cell.setPadding(new Insets(0, 8, 0, 8));
        cell.setStyle(
                "-fx-background-color:" + (isAlt ? "#fafcff" : "white") + ";" +
                "-fx-border-color:#f1f5f9; -fx-border-width:0 0 1 0;"
        );
        return cell;
    }

    private Label createDirTextCell(String text, boolean isAlt) {
        Label cell = new Label(text);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMinHeight(48);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 8, 0, 8));
        cell.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        if (text != null && text.length() > 16) {
            javafx.scene.control.Tooltip.install(cell, new javafx.scene.control.Tooltip(text));
        }
        cell.setStyle(
                "-fx-background-color:" + (isAlt ? "#fafcff" : "white") + ";" +
                "-fx-text-fill:#475569; -fx-font-size:13;" +
                "-fx-border-color:#f1f5f9; -fx-border-width:0 0 1 0;"
        );
        return cell;
    }

    private HBox createRoleTagCell(String roleStr, boolean isAlt) {
        Label tag = new Label(roleStr);
        String color = BLUE;
        String bg = "#eff6ff";
        if ("Admin".equalsIgnoreCase(roleStr)) {
            color = "#6366f1";
            bg = "#eef2ff";
        } else if ("Port Authority".equalsIgnoreCase(roleStr)) {
            color = "#0ea5e9";
            bg = "#e0f2fe";
        } else if ("Customs Officer".equalsIgnoreCase(roleStr)) {
            color = "#d97706";
            bg = "#fef3c7";
        }

        tag.setStyle(
                "-fx-text-fill:" + color + ";" +
                "-fx-background-color:" + bg + ";" +
                "-fx-background-radius:6;" +
                "-fx-padding:3 8 3 8;" +
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;"
        );
        tag.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);

        HBox cell = new HBox(tag);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMinHeight(48);
        cell.setPadding(new Insets(0, 6, 0, 6));
        cell.setStyle(
                "-fx-background-color:" + (isAlt ? "#fafcff" : "white") + ";" +
                "-fx-border-color:#f1f5f9; -fx-border-width:0 0 1 0;"
        );
        return cell;
    }

    private HBox createStatusBadgeCell(String statusStr, boolean isAlt) {
        Label badge = new Label(statusStr);
        String textColor = "#16a34a";
        String bgColor = "#ecfdf5";
        if ("Pending".equalsIgnoreCase(statusStr)) {
            textColor = "#f59e0b";
            bgColor = "#fffbeb";
        } else if ("Deactivated".equalsIgnoreCase(statusStr)) {
            textColor = "#ef4444";
            bgColor = "#fee2e2";
        }

        badge.setStyle(
                "-fx-text-fill:" + textColor + ";" +
                "-fx-background-color:" + bgColor + ";" +
                "-fx-background-radius:6;" +
                "-fx-padding:3 8 3 8;" +
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;"
        );
        badge.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);

        HBox cell = new HBox(badge);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMinHeight(48);
        cell.setPadding(new Insets(0, 6, 0, 6));
        cell.setStyle(
                "-fx-background-color:" + (isAlt ? "#fafcff" : "white") + ";" +
                "-fx-border-color:#f1f5f9; -fx-border-width:0 0 1 0;"
        );
        return cell;
    }

    private HBox createDirectoryActionCell(int userIndex, boolean isAlt) {
        Button editBtn = new Button("Edit");
        editBtn.setStyle(
                "-fx-background-color:#eff6ff;" +
                "-fx-text-fill:" + BLUE + ";" +
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:4;" +
                "-fx-padding:3 8;" +
                "-fx-cursor:hand;"
        );
        editBtn.setOnAction(e -> startEdit(userIndex));

        Button moreBtn = new Button("⋮");
        moreBtn.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-text-fill:#64748b;" +
                "-fx-font-size:14;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:2 4;" +
                "-fx-cursor:hand;"
        );
        moreBtn.setOnAction(e -> showUserActionPopup(userIndex, moreBtn));

        HBox cell = new HBox(4, editBtn, moreBtn);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMinHeight(48);
        cell.setPadding(new Insets(0, 4, 0, 4));
        cell.setStyle(
                "-fx-background-color:" + (isAlt ? "#fafcff" : "white") + ";" +
                "-fx-border-color:#f1f5f9; -fx-border-width:0 0 1 0;"
        );
        return cell;
    }

    private void showUserActionPopup(int userIndex, Button anchor) {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        AdminDataStore.UserAccount user = users.get(userIndex);

        VBox menu = new VBox(4);
        menu.setPadding(new Insets(8));
        menu.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.15), 10, 0, 0, 3);"
        );

        Button viewItem = popupMenuItem("👁 View Profile & Access");
        viewItem.setOnAction(e -> {
            popup.hide();
            startView(userIndex);
        });

        Button editItem = popupMenuItem("✎ Edit User Information");
        editItem.setOnAction(e -> {
            popup.hide();
            startEdit(userIndex);
        });

        Button toggleStatusItem = popupMenuItem(
                "Active".equals(user.status) ? "⊘ Deactivate Account" : "✓ Activate Account"
        );
        toggleStatusItem.setOnAction(e -> {
            popup.hide();
            if ("Active".equalsIgnoreCase(user.status) && ("Admin".equalsIgnoreCase(user.role) || "babu@gmail.com".equalsIgnoreCase(user.email)) && countActiveAdmins() <= 1) {
                showToast("Cannot deactivate or delete the only active system administrator.", false);
                return;
            }
            user.status = "Active".equals(user.status) ? "Deactivated" : "Active";
            String uid = "usr_" + Math.abs(user.email.toLowerCase().hashCode());
            userDao.saveUserExtended(uid, user.email, user.name, "Dock Tech Operations", "+91 9800000000", user.role, user.status, null)
                    .whenComplete((res, err) -> javafx.application.Platform.runLater(() -> {
                        com.projectui.dao.UserDao.logActivity(user.name, user.email, "USER_UPDATED", "Account status updated to " + user.status + " by Admin");
                        refreshTable();
                        refreshStats();
                        showToast("Updated " + user.name + " status to " + user.status, true);
                    }));
        });

        Button resetPassItem = popupMenuItem("🔒 Reset User Password");
        resetPassItem.setOnAction(e -> {
            popup.hide();
            showToast("Password reset link dispatched to " + user.email, true);
        });

        Button deleteUserItem = popupMenuItem("✕ Delete User");
        deleteUserItem.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-text-fill:#ef4444;" +
                "-fx-font-size:14;" +
                "-fx-padding:7 12;" +
                "-fx-cursor:hand;"
        );
        deleteUserItem.setOnAction(e -> {
            popup.hide();

            // Single Admin Protection Guard
            if ("Admin".equalsIgnoreCase(user.role) || "babu@gmail.com".equalsIgnoreCase(user.email)) {
                showToast("Cannot delete the only system administrator.", false);
                return;
            }

            // Confirmation Dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete User?");
            alert.setHeaderText("Delete User?");
            alert.setContentText("Are you sure you want to permanently delete " + user.name + "?\nThis action cannot be undone.");

            javafx.scene.control.ButtonType deleteBtnType = new javafx.scene.control.ButtonType("Delete", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            javafx.scene.control.ButtonType cancelBtnType = new javafx.scene.control.ButtonType("Cancel", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(cancelBtnType, deleteBtnType);

            alert.showAndWait().ifPresent(type -> {
                if (type == deleteBtnType) {
                    userDao.deleteUserAccount(user.uid, user.email).whenComplete((res, err) -> javafx.application.Platform.runLater(() -> {
                        if (err != null) {
                            showToast("Unable to delete user. The account could not be completely removed. Please try again.", false);
                        } else {
                            showToast("User " + user.name + " deleted successfully.", true);
                            loadLiveUsersFromFirestore();
                        }
                    }));
                }
            });
        });

        menu.getChildren().addAll(viewItem, editItem, toggleStatusItem, new Separator(), resetPassItem, deleteUserItem);
        popup.getContent().add(menu);

        Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        popup.show(anchor.getScene().getWindow(), bounds.getMinX() - 150, bounds.getMaxY() + 4);
    }

    private Button popupMenuItem(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-text-fill:#0f172a;" +
                "-fx-font-size:14;" +
                "-fx-padding:7 12;" +
                "-fx-cursor:hand;"
        );
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color:#f1f5f9; -fx-text-fill:#0f172a; -fx-font-size:14; -fx-padding:7 12; -fx-cursor:hand; -fx-background-radius:4;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color:transparent; -fx-text-fill:#0f172a; -fx-font-size:14; -fx-padding:7 12; -fx-cursor:hand;"));
        return b;
    }

    // ==========================================
    // 3. ADD / EDIT USER PANEL (RIGHT SIDE)
    // ==========================================
    private VBox userFormCard() {
        formTitle = new Label("Add / Edit User");
        formTitle.setStyle("-fx-text-fill:#0f172a; -fx-font-size:18; -fx-font-weight:bold;");

        formSubtitle = new Label("Manage user profile, credentials and assigned role capabilities");
        formSubtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, formTitle, formSubtitle);

        nameField = new TextField();
        nameField.setPromptText("Enter full name (e.g. Aarav Mehta)");
        styleCustomField(nameField);

        emailField = new TextField();
        emailField.setPromptText("Enter official email (e.g. user@docktech.com)");
        styleCustomField(emailField);

        roleCombo = combo("Select Role", "Admin", "Port Authority", "Customs Officer", "Ship Captain", "Exporter", "Importer");
        statusCombo = combo("Select Status", "Active", "Pending", "Deactivated");
        roleCombo.setStyle("-fx-font-size:13;");
        statusCombo.setStyle("-fx-font-size:13;");
        roleCombo.setMaxWidth(Double.MAX_VALUE);
        statusCombo.setMaxWidth(Double.MAX_VALUE);

        roleCombo.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                renderCapabilities(newV);
            }
        });

        capabilitiesContainer = new VBox(6);
        capabilitiesContainer.setPadding(new Insets(10, 12, 10, 12));
        capabilitiesContainer.setStyle(
                "-fx-background-color:#f8fafc;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;"
        );
        renderCapabilities("Port Authority");

        saveBtn = primaryButton("Save User");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle(
                "-fx-background-color:" + BLUE + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-size:15;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:11;" +
                "-fx-background-radius:8;" +
                "-fx-cursor:hand;"
        );
        saveBtn.setOnAction(e -> saveUser());

        cancelBtn = new Button("Clear Form");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setStyle(
                "-fx-background-color:#f1f5f9;" +
                "-fx-text-fill:#475569;" +
                "-fx-font-size:14;" +
                "-fx-font-weight:bold;" +
                "-fx-padding:9;" +
                "-fx-background-radius:8;" +
                "-fx-cursor:hand;"
        );
        cancelBtn.setOnAction(e -> startAdd());

        VBox formContent = new VBox(10,
                titleBox,
                new Separator(),
                labeledFormItem("Full Name", nameField),
                labeledFormItem("Email Address", emailField),
                labeledFormItem("Assigned Role", roleCombo),
                labeledFormItem("Account Status", statusCombo),
                new Label("Role Capabilities"),
                capabilitiesContainer,
                new Separator(),
                saveBtn,
                cancelBtn
        );

        VBox card = new VBox(10, formContent);
        card.setPadding(new Insets(18));
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 10, 0, 0, 2);"
        );
        return card;
    }

    private void styleCustomField(TextField f) {
        f.setStyle(
                "-fx-background-color:#f8fafc;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:8;" +
                "-fx-background-radius:8;" +
                "-fx-padding:8 12 8 12;" +
                "-fx-font-size:13;"
        );
    }

    private VBox labeledFormItem(String labelText, Node control) {
        Label l = new Label(labelText);
        l.setStyle("-fx-text-fill:#334155; -fx-font-size:14; -fx-font-weight:bold;");
        VBox item = new VBox(4, l, control);
        return item;
    }

    private void renderCapabilities(String roleStr) {
        capabilitiesContainer.getChildren().clear();

        Label header = new Label("Permissions granted for " + roleStr + ":");
        header.setStyle("-fx-text-fill:#475569; -fx-font-size:13; -fx-font-weight:bold; -fx-padding:0 0 4 0;");
        capabilitiesContainer.getChildren().add(header);

        String[] allCapabilities = {
                "Manage Ports",
                "Manage Users",
                "View Reports",
                "Manage Settings",
                "System Administration"
        };

        boolean isAdmin = "Admin".equalsIgnoreCase(roleStr);
        boolean isPortAuth = "Port Authority".equalsIgnoreCase(roleStr);
        boolean isCustoms = "Customs Officer".equalsIgnoreCase(roleStr);
        boolean isCaptain = "Ship Captain".equalsIgnoreCase(roleStr);

        for (String cap : allCapabilities) {
            boolean granted = false;
            if (isAdmin) {
                granted = true;
            } else if (isPortAuth) {
                granted = cap.equals("Manage Ports") || cap.equals("View Reports");
            } else if (isCustoms) {
                granted = cap.equals("View Reports");
            } else if (isCaptain) {
                granted = cap.equals("Manage Ports") || cap.equals("View Reports");
            }

            HBox row = capabilityRow(cap, granted);
            capabilitiesContainer.getChildren().add(row);
        }
    }

    private HBox capabilityRow(String name, boolean granted) {
        Label check = new Label(granted ? "✓" : "—");
        check.setStyle(
                "-fx-text-fill:" + (granted ? "#16a34a" : "#94a3b8") + ";" +
                "-fx-font-size:14;" +
                "-fx-font-weight:bold;" +
                "-fx-min-width:20;"
        );

        Label label = new Label(name);
        label.setMinWidth(160);
        label.setStyle(
                "-fx-text-fill:" + (granted ? "#0f172a" : "#94a3b8") + ";" +
                "-fx-font-size:13;" +
                "-fx-font-weight:" + (granted ? "bold" : "normal") + ";"
        );

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label statusTag = new Label(granted ? "Granted" : "Restricted");
        statusTag.setMinWidth(75);
        statusTag.setAlignment(Pos.CENTER);
        statusTag.setStyle(
                "-fx-text-fill:" + (granted ? "#16a34a" : "#64748b") + ";" +
                "-fx-background-color:" + (granted ? "#ecfdf5" : "#f1f5f9") + ";" +
                "-fx-background-radius:4;" +
                "-fx-padding:3 8;" +
                "-fx-font-size:12;" +
                "-fx-font-weight:bold;"
        );

        HBox row = new HBox(8, check, label, space, statusTag);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(3, 0, 3, 0));
        row.setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    private void startAdd() {
        editingIndex = -1;
        selectedUserUid = null;
        formTitle.setText("Add New User");
        formSubtitle.setText("Register a new Dock Tech operator account");
        nameField.clear();
        emailField.clear();

        String creatorRole = com.projectui.view.login.LoginPage.loggedInUser != null && com.projectui.view.login.LoginPage.loggedInUser.get("role") != null
                ? com.projectui.view.login.LoginPage.loggedInUser.get("role").toString()
                : "Admin";

        if ("Port Authority".equalsIgnoreCase(creatorRole)) {
            roleCombo.getItems().setAll("Customs Officer", "Ship Captain", "Exporter", "Importer");
            roleCombo.setValue("Customs Officer");
            renderCapabilities("Customs Officer");
        } else if ("Admin".equalsIgnoreCase(creatorRole) || "Administrator".equalsIgnoreCase(creatorRole)) {
            roleCombo.getItems().setAll("Admin", "Port Authority", "Ship Captain", "Customs Officer", "Importer", "Exporter");
            roleCombo.setValue("Port Authority");
            renderCapabilities("Port Authority");
        } else {
            roleCombo.getItems().setAll("Exporter", "Importer");
            roleCombo.setValue("Exporter");
            renderCapabilities("Exporter");
        }

        statusCombo.setValue("Pending");
        setFormEditable(true);
        saveBtn.setText("Create User");
        nameField.requestFocus();
    }

    private String selectedUserUid = null;

    private void startEdit(int index) {
        if (index < 0 || index >= users.size()) return;
        editingIndex = index;
        AdminDataStore.UserAccount user = users.get(index);
        selectedUserUid = user.uid;
        if (selectedUserUid == null || selectedUserUid.isBlank() || selectedUserUid.startsWith("usr_")) {
            String realUid = com.projectui.dao.UserDao.getUidForEmail(user.email);
            if (realUid != null && !realUid.isBlank()) selectedUserUid = realUid;
        }
        formTitle.setText("Edit User Profile");
        formSubtitle.setText("Update credentials, role and status for " + user.name);
        nameField.setText(user.name);
        emailField.setText(user.email);
        roleCombo.setValue(user.role);
        statusCombo.setValue(user.status);
        renderCapabilities(user.role);
        setFormEditable(true);
        saveBtn.setText("Update User");
    }

    private void startView(int index) {
        if (index < 0 || index >= users.size()) return;
        editingIndex = index;
        AdminDataStore.UserAccount user = users.get(index);
        selectedUserUid = user.uid;
        if (selectedUserUid == null || selectedUserUid.isBlank() || selectedUserUid.startsWith("usr_")) {
            String realUid = com.projectui.dao.UserDao.getUidForEmail(user.email);
            if (realUid != null && !realUid.isBlank()) selectedUserUid = realUid;
        }
        formTitle.setText("User Profile: " + user.name);
        formSubtitle.setText("Created: " + user.created + " • Last Active: " + user.lastLogin);
        nameField.setText(user.name);
        emailField.setText(user.email);
        roleCombo.setValue(user.role);
        statusCombo.setValue(user.status);
        renderCapabilities(user.role);
        setFormEditable(false);
        saveBtn.setText("Edit This User");
        saveBtn.setOnAction(e -> startEdit(index));
    }

    private void setFormEditable(boolean editable) {
        nameField.setEditable(editable);
        emailField.setEditable(editable);
        roleCombo.setDisable(!editable);
        statusCombo.setDisable(!editable);
        nameField.setOpacity(1);
        emailField.setOpacity(1);
        roleCombo.setOpacity(1);
        statusCombo.setOpacity(1);
        saveBtn.setOnAction(e -> saveUser());
    }

    private void saveUser() {
        String nameVal = nameField.getText().trim();
        String emailVal = emailField.getText().trim();

        if (nameVal.isEmpty()) {
            showToast("Please enter a valid user name.", false);
            nameField.requestFocus();
            return;
        }
        if (!emailVal.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showToast("Please enter a valid email address (e.g. user@docktech.com).", false);
            emailField.requestFocus();
            return;
        }

        String roleVal = roleCombo.getValue() == null ? "Port Authority" : roleCombo.getValue();
        String statusVal = statusCombo.getValue() == null ? "Active" : statusCombo.getValue();

        String creatorRole = com.projectui.view.login.LoginPage.loggedInUser != null && com.projectui.view.login.LoginPage.loggedInUser.get("role") != null
                ? com.projectui.view.login.LoginPage.loggedInUser.get("role").toString()
                : "Admin";

        if (!com.projectui.dao.UserDao.isRoleCreationAllowed(creatorRole, roleVal)) {
            showToast("Role Assignment Denied: " + creatorRole + " is not authorized to create " + roleVal + " accounts.", false);
            return;
        }

        if ("Admin".equalsIgnoreCase(roleVal)) {
            boolean adminExists = false;
            for (AdminDataStore.UserAccount u : users) {
                if (u != null && "Admin".equalsIgnoreCase(u.role) && !u.email.equalsIgnoreCase(emailVal)) {
                    adminExists = true;
                    break;
                }
            }
            if (adminExists) {
                showToast("Admin account already exists. Only one Admin account is allowed.", false);
                return;
            }
        }

        if (editingIndex >= 0 && editingIndex < users.size()) {
            AdminDataStore.UserAccount old = users.get(editingIndex);
            
            // Single Active Admin Protection Guard
            if ("Admin".equalsIgnoreCase(old.role) && (!"Admin".equalsIgnoreCase(roleVal) || !"Active".equalsIgnoreCase(statusVal)) && countActiveAdmins() <= 1) {
                showToast("Cannot deactivate or delete the only active system administrator.", false);
                return;
            }

            old.name = nameVal;
            old.email = emailVal;
            old.role = roleVal;
            old.status = statusVal;

            String targetUid = (selectedUserUid != null && !selectedUserUid.isBlank()) ? selectedUserUid : old.uid;
            if (targetUid == null || targetUid.isBlank() || targetUid.startsWith("usr_")) {
                String realUid = com.projectui.dao.UserDao.getUidForEmail(emailVal);
                if (realUid != null && !realUid.isBlank()) targetUid = realUid;
            }
            if (targetUid == null || targetUid.isBlank()) {
                targetUid = "usr_" + Math.abs(emailVal.toLowerCase().hashCode());
            }

            old.uid = targetUid;
            saveBtn.setDisable(true);
            showToast("Updating user profile...", true);

            userDao.saveUserExtended(targetUid, emailVal, nameVal, "Dock Tech Operations", "+91 9800000000", roleVal, statusVal, null)
                    .whenComplete((res, err) -> javafx.application.Platform.runLater(() -> {
                        saveBtn.setDisable(false);
                        if (err != null) {
                            showToast(err.getMessage() != null && !err.getMessage().isBlank() ? err.getMessage() : "Unable to update user information.", false);
                        } else {
                            com.projectui.dao.UserDao.logActivity(nameVal, emailVal, "USER_UPDATED", "User information updated by Admin");
                            showToast("User updated successfully.", true);
                            loadLiveUsersFromFirestore();
                        }
                    }));
        } else {
            String phoneVal = "+91 98" + (int)(Math.random() * 89999999 + 10000000);
            userDao.provisionAccount(creatorRole, nameVal, emailVal, "Dock Tech Operations", phoneVal, roleVal, statusVal)
                .whenComplete((creds, err) -> javafx.application.Platform.runLater(() -> {
                    if (err != null) {
                        showToast("Provision Error: " + err.getMessage(), false);
                    } else if (creds != null) {
                        String tempPass = creds.get("tempPassword");
                        String uid = creds.get("uid");
                        com.projectui.dao.UserDao.logActivity(nameVal, emailVal, "USER_CREATED", "User account created by Admin (" + roleVal + ")");
                        showAccountCreatedPopup(nameVal, emailVal, roleVal, uid, tempPass);
                        showToast("User account created successfully.", true);
                        loadLiveUsersFromFirestore();
                    }
                }));
        }

        refreshTable();
        refreshStats();
        startEdit(editingIndex);
    }

    private void showAccountCreatedPopup(String name, String email, String role, String uid, String tempPass) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Account Created Successfully");
        alert.setHeaderText("Account Created Successfully");
        alert.setContentText(
                "Name: " + name + "\n" +
                "Email: " + email + "\n" +
                "Role: " + role + "\n" +
                "Account ID: " + (uid != null ? uid : "Generated") + "\n\n" +
                "Temporary Password: " + (tempPass != null ? tempPass : "Dispatched")
        );
        alert.showAndWait();
    }

    private int countActiveAdmins() {
        int activeAdmins = 0;
        for (AdminDataStore.UserAccount u : users) {
            if (u != null && "Admin".equalsIgnoreCase(u.role) && "Active".equalsIgnoreCase(u.status)) {
                activeAdmins++;
            }
        }
        return activeAdmins;
    }

    private void refreshStats() {
        if (totalValue != null) totalValue.setText(String.valueOf(users.size()));
        if (activeValue != null) activeValue.setText(String.valueOf(countStatus("Active")));
        if (inactiveValue != null) inactiveValue.setText(String.valueOf(countStatus("Deactivated")));
        if (adminValue != null) adminValue.setText(String.valueOf(countRole("Admin")));
        if (portManagerValue != null) portManagerValue.setText(String.valueOf(countRole("Port Authority")));
        if (pendingValue != null) pendingValue.setText(String.valueOf(countStatus("Pending")));
    }

    private int countStatus(String statusVal) {
        int count = 0;
        for (AdminDataStore.UserAccount u : users) {
            if (statusVal.equalsIgnoreCase(u.status)) count++;
        }
        return count;
    }

    private int countRole(String roleVal) {
        int count = 0;
        for (AdminDataStore.UserAccount u : users) {
            if (roleVal.equalsIgnoreCase(u.role)) count++;
        }
        return count;
    }

    private boolean matches(AdminDataStore.UserAccount user) {
        String query = search == null || search.getText() == null ? "" : search.getText().trim().toLowerCase();
        String selectedRole = roleFilter == null || roleFilter.getValue() == null ? "All Roles" : roleFilter.getValue();
        String selectedStatus = statusFilter == null || statusFilter.getValue() == null ? "All Status" : statusFilter.getValue();

        boolean queryMatches = query.isEmpty()
                || user.name.toLowerCase().contains(query)
                || user.email.toLowerCase().contains(query)
                || user.role.toLowerCase().contains(query)
                || user.status.toLowerCase().contains(query);

        boolean roleMatches = "All Roles".equalsIgnoreCase(selectedRole) || selectedRole.equalsIgnoreCase(user.role);
        boolean statusMatches = "All Status".equalsIgnoreCase(selectedStatus) || selectedStatus.equalsIgnoreCase(user.status);

        return queryMatches && roleMatches && statusMatches;
    }

    @SuppressWarnings("unused")
    private String now() {
        return LocalDateTime.now().format(DATE_TIME);
    }

    // ==========================================
    // 4. FOCUSED USER ADMINISTRATION CARDS (BOTTOM ROW)
    // (User Role Distribution chart removed per requirement)
    // ==========================================
    private HBox createBottomAdminRow() {
        VBox card1 = recentUserActivitiesCard();
        VBox card2 = securityAndAccessCard();

        HBox row = new HBox(16, card1, card2);
        card1.prefWidthProperty().bind(row.widthProperty().multiply(0.50));
        card2.prefWidthProperty().bind(row.widthProperty().multiply(0.50));

        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);

        return row;
    }

    private String getRelativeTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank() || "Never".equalsIgnoreCase(timeStr) || "Pending approval".equalsIgnoreCase(timeStr)) {
            return "Never";
        }
        try {
            LocalDateTime dt = null;
            if (timeStr.contains("T")) {
                dt = LocalDateTime.parse(timeStr);
            } else if (timeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                dt = LocalDateTime.parse(timeStr.replace(" ", "T"));
            }
            if (dt != null) {
                long seconds = java.time.Duration.between(dt, LocalDateTime.now()).getSeconds();
                if (seconds < 60) return "Just now";
                long mins = seconds / 60;
                if (mins < 60) return mins + " mins ago";
                long hrs = mins / 60;
                if (hrs < 24) return hrs + (hrs == 1 ? " hr ago" : " hrs ago");
                long days = hrs / 24;
                if (days == 1) return "Yesterday";
                return days + " days ago";
            }
        } catch (Exception ignored) {}
        return "Just now";
    }

    // Bottom Card 1: Recent User Activities (Spacious Audit & Access Trail)
    private VBox recentUserActivitiesCard() {
        Label title = new Label("Recent User Activities");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16; -fx-font-weight:bold;");
        Label subtitle = new Label("Real-time security, login, and user modification audit trail");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        VBox logList = new VBox(8);

        java.util.List<java.util.Map<String, Object>> logs = com.projectui.dao.UserDao.getActivityLogs();
        if (logs != null && !logs.isEmpty()) {
            for (java.util.Map<String, Object> log : logs) {
                String name = log.get("userName") != null ? log.get("userName").toString() : "Admin";
                String desc = log.get("description") != null ? log.get("description").toString() : "System Action";
                String ts = log.get("timestamp") != null ? log.get("timestamp").toString() : "";
                String action = log.get("action") != null ? log.get("action").toString() : "";
                String dotColor = "#16a34a";
                if (action.contains("DELETE")) dotColor = "#ef4444";
                else if (action.contains("UPDATE")) dotColor = "#0b63ff";

                logList.getChildren().add(activityAuditItem(name, desc, getRelativeTime(ts), dotColor));
            }
        } else {
            logList.getChildren().addAll(
                    activityAuditItem("Babu", "Logged in to Admin Console", "Just now", "#16a34a"),
                    activityAuditItem("Rajesh Varma", "Provisioned Port Authority account for Hazira", "10 mins ago", "#0b63ff")
            );
        }

        VBox card = new VBox(12, titleBox, new Separator(), logList);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 10, 0, 0, 2);"
        );
        return card;
    }

    private HBox activityAuditItem(String user, String action, String time, String dotColor) {
        Circle dot = new Circle(4);
        dot.setStyle("-fx-fill:" + dotColor + ";");

        Label uLbl = new Label(user);
        uLbl.setStyle("-fx-text-fill:#0f172a; -fx-font-size:13; -fx-font-weight:bold;");

        Label aLbl = new Label(" " + action);
        aLbl.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");

        HBox left = new HBox(6, dot, uLbl, aLbl);
        left.setAlignment(Pos.CENTER_LEFT);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label tLbl = new Label(time);
        tLbl.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:12;");

        HBox row = new HBox(4, left, space, tLbl);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // Bottom Card 2: Security & Access Governance
    private VBox securityAndAccessCard() {
        Label title = new Label("Security & Access Governance");
        title.setStyle("-fx-text-fill:#0f172a; -fx-font-size:16; -fx-font-weight:bold;");
        Label subtitle = new Label("Enterprise authorization compliance, password policy & session controls");
        subtitle.setStyle("-fx-text-fill:#64748b; -fx-font-size:13;");
        VBox titleBox = new VBox(2, title, subtitle);

        VBox items = new VBox(8);
        items.getChildren().addAll(
                securityStatusRow("Two-Factor Authentication (2FA)", "Enforced for Administrators", "#16a34a"),
                securityStatusRow("Session Inactivity Timeout", "30 Minutes (Active)", "#16a34a"),
                securityStatusRow("Enterprise Password Policy", "Strong (12+ chars, 90-day rotation)", "#16a34a"),
                securityStatusRow("Failed Login Attempt Rate", "0 Suspicious / Last 24 Hours", "#16a34a"),
                securityStatusRow("Role-Based Access Control (RBAC)", "Strict Multi-Tier Enforced", "#0b63ff")
        );

        VBox card = new VBox(12, titleBox, new Separator(), items);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#e2e8f0;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 10, 0, 0, 2);"
        );
        return card;
    }

    private HBox securityStatusRow(String labelText, String statusVal, String statusColor) {
        Circle dot = new Circle(4);
        dot.setStyle("-fx-fill:" + statusColor + ";");

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill:#334155; -fx-font-size:13; -fx-font-weight:bold;");

        HBox left = new HBox(6, dot, label);
        left.setAlignment(Pos.CENTER_LEFT);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Label val = new Label(statusVal);
        val.setStyle("-fx-text-fill:" + statusColor + "; -fx-font-size:13; -fx-font-weight:bold;");

        HBox row = new HBox(6, left, space, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private interface LabelSetter {
        void set(Label label);
    }
}
