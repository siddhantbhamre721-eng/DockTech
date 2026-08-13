package com.projectui.view.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class UserManagementPage extends AdminTheme {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private final List<UserAccount> users = new ArrayList<>();
    private final GridPane table = new GridPane();

    private Label totalValue;
    private Label activeValue;
    private Label pendingValue;
    private TextField search;
    private TextField name;
    private TextField email;
    private ComboBox<String> role;
    private ComboBox<String> status;
    private Label formTitle;
    private Label formSubtitle;
    private VBox detailBox;
    private Button save;
    private int editingIndex = -1;
    private final boolean startAddOnOpen;

    public UserManagementPage() {
        this(false);
    }

    public UserManagementPage(boolean startAddOnOpen) {
        this.startAddOnOpen = startAddOnOpen;
        users.add(new UserAccount("Aarav Mehta", "aarav@docktech.com", "Admin", "Active", "01 Jan 2026", now()));
        users.add(new UserAccount("John Smith", "john@docktech.com", "Port Authority", "Active", "18 Feb 2026", now()));
        users.add(new UserAccount("Rahul Patil", "rahul@docktech.com", "Ship Captain", "Active", "04 Mar 2026", "11 Aug 2026, 20:10"));
        users.add(new UserAccount("Alex Kumar", "alex@docktech.com", "Customs Officer", "Pending", "25 Jul 2026", "Pending approval"));
        users.add(new UserAccount("Meera Rao", "meera@docktech.com", "Exporter", "Active", "09 May 2026", now()));
        users.add(new UserAccount("Nisha Shah", "nisha@docktech.com", "Importer", "Deactivated", "16 Jun 2026", "05 Aug 2026, 17:35"));
    }

    public Parent getRoot() {
        Button add = primaryButton("+  Add User");
        add.setOnAction(event -> startAdd());

        VBox page = pageContent();
        page.getChildren().addAll(
                pageHeader("User Management", "Manage Dock Tech users, roles, and account status.", add),
                new HBox(16,
                        statBox("Total Users", "0", TEXT, label -> totalValue = label),
                        statBox("Active Users", "0", "#0aa072", label -> activeValue = label),
                        statBox("Pending Users", "0", "#f97316", label -> pendingValue = label)
                ),
                new HBox(18, userListCard(), userFormCard())
        );

        refreshTable();
        refreshStats();
        if (startAddOnOpen) {
            startAdd();
        } else if (!users.isEmpty()) {
            startEdit(0);
        }
        return page;
    }

    private VBox userListCard() {
        search = new TextField();
        search.setPromptText("⌕  Search users");
        search.setPrefWidth(155);
        styleField(search);
        search.setStyle(search.getStyle() + "-fx-background-color:#f2f2f5; -fx-border-color:transparent;");
        search.textProperty().addListener((obs, oldValue, newValue) -> refreshTable());

        Label title = new Label("User List");
        title.setStyle("-fx-text-fill:#05070c; -fx-font-size:17; -fx-font-weight:bold;");

        Label subtitle = new Label("View and edit registered users");
        subtitle.setStyle("-fx-text-fill:#747884; -fx-font-size:12;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox top = new HBox(14, new VBox(8, title, subtitle), space, search);
        top.setAlignment(Pos.CENTER_LEFT);

        styleTable();

        VBox card = new VBox(20, top, horizontalScroll(table));
        card.setPrefWidth(700);
        card.setPadding(new Insets(22));
        card.setStyle(CARD_STYLE);
        return card;
    }

    private VBox userFormCard() {
        formTitle = new Label("Add / Edit User");
        formTitle.setStyle("-fx-text-fill:#05070c; -fx-font-size:21; -fx-font-weight:bold;");

        formSubtitle = new Label("Update user details");
        formSubtitle.setStyle("-fx-text-fill:#747884; -fx-font-size:14;");

        name = new TextField();
        email = new TextField();
        role = combo("Role", "Admin", "Port Authority", "Customs Officer", "Ship Captain", "Exporter", "Importer");
        status = combo("Status", "Active", "Pending", "Deactivated");
        styleField(name);
        styleField(email);
        role.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && name.isEditable()) {
                showRoleCapabilities(newValue);
            }
        });

        detailBox = new VBox(8);

        save = primaryButton("Save");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setOnAction(event -> saveUser());

        VBox card = new VBox(10, formTitle, formSubtitle);
        card.setPrefWidth(330);
        card.setPadding(new Insets(22));
        card.setStyle(CARD_STYLE);
        card.getChildren().addAll(
                labeledControl("Name", name),
                labeledControl("Email", email),
                labeledControl("Role", role),
                labeledControl("Status", status),
                detailBox,
                save
        );
        return card;
    }

    private void refreshTable() {
        table.getChildren().clear();
        styleTable();

        String[] headers = {"Name", "Email", "Role", "Status", "Action"};
        for (int col = 0; col < headers.length; col++) {
            table.add(cell(headers[col], true), col, 0);
        }

        int displayRow = 1;
        for (int row = 0; row < users.size(); row++) {
            UserAccount user = users.get(row);
            if (!matches(user)) {
                continue;
            }

            table.add(cell(user.name, false), 0, displayRow);
            table.add(cell(user.email, false), 1, displayRow);
            table.add(cell(user.role, false), 2, displayRow);
            table.add(statusBadge(user.status), 3, displayRow);
            table.add(actionCell(row), 4, displayRow);
            displayRow++;
        }

        if (displayRow == 1) {
            Label empty = cell("No users found", false);
            empty.setMinWidth(780);
            table.add(empty, 0, 1, 5, 1);
        }
    }

    private void startAdd() {
        editingIndex = -1;
        formTitle.setText("Add User");
        formSubtitle.setText("Register a new Dock Tech user");
        name.clear();
        email.clear();
        role.setValue("Port Authority");
        status.setValue("Pending");
        showRoleCapabilities("Port Authority");
        setFormEditable(true);
        save.setText("Save");
        save.setOnAction(event -> saveUser());
        name.requestFocus();
    }

    private void startView(int index) {
        editingIndex = index;
        UserAccount user = users.get(index);
        formTitle.setText("View User");
        formSubtitle.setText("User details");
        fillForm(user);
        showUserDetails(user);
        setFormEditable(false);
        save.setText("Edit");
        save.setOnAction(event -> startEdit(index));
    }

    private void startEdit(int index) {
        editingIndex = index;
        UserAccount user = users.get(index);
        formTitle.setText("Add / Edit User");
        formSubtitle.setText("Update user details");
        fillForm(user);
        showRoleCapabilities(user.role);
        setFormEditable(true);
        save.setText("Save");
        save.setOnAction(event -> saveUser());
    }

    private void deactivateUser(int index) {
        detailBox.getChildren().clear();
        detailBox.getChildren().addAll(
                sectionLabel("Deactivate User"),
                activityLine("Are you sure you want to deactivate this user?"),
                confirmDeactivateRow(index)
        );
    }

    private void saveUser() {
        String nameValue = name.getText().trim();
        String emailValue = email.getText().trim();
        if (nameValue.isEmpty()) {
            formSubtitle.setText("Name cannot be empty.");
            return;
        }
        if (!emailValue.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            formSubtitle.setText("Invalid email address.");
            return;
        }
        if (role.getValue() == null) {
            formSubtitle.setText("Please select a role.");
            return;
        }
        if (status.getValue() == null) {
            formSubtitle.setText("Please select a status.");
            return;
        }

        if (editingIndex >= 0 && editingIndex < users.size()) {
            UserAccount old = users.get(editingIndex);
            old.name = nameValue;
            old.email = emailValue;
            old.role = role.getValue();
            old.status = status.getValue();
            formSubtitle.setText("User updated successfully");
        } else {
            users.add(new UserAccount(nameValue, emailValue, role.getValue(), status.getValue(), now(), "Not logged in yet"));
            editingIndex = users.size() - 1;
            formSubtitle.setText("User added successfully");
        }

        refreshTable();
        refreshStats();
        startEdit(editingIndex);
    }

    private void refreshStats() {
        totalValue.setText(String.valueOf(users.size()));
        activeValue.setText(String.valueOf(countStatus("Active")));
        pendingValue.setText(String.valueOf(countStatus("Pending")));
    }

    private String now() {
        return LocalDateTime.now().format(DATE_TIME);
    }

    private int countStatus(String statusValue) {
        int count = 0;
        for (UserAccount user : users) {
            if (statusValue.equals(user.status)) {
                count++;
            }
        }
        return count;
    }

    private void fillForm(UserAccount user) {
        name.setText(user.name);
        email.setText(user.email);
        role.setValue(user.role);
        status.setValue(user.status);
    }

    private void showUserDetails(UserAccount user) {
        detailBox.getChildren().clear();
        detailBox.getChildren().addAll(
                detailRow("Account Created", user.created),
                detailRow("Last Login", user.lastLogin),
                sectionLabel("Role Capabilities")
        );
        for (String capability : capabilitiesFor(user.role)) {
            detailBox.getChildren().add(activityLine(capability));
        }
        detailBox.getChildren().addAll(
                sectionLabel("Recent Activity"),
                activityLine("Logged in"),
                activityLine("Viewed Port Mumbai"),
                activityLine("Updated vessel status")
        );
    }

    private void setFormEditable(boolean editable) {
        name.setEditable(editable);
        email.setEditable(editable);
        role.setDisable(!editable);
        status.setDisable(!editable);
        name.setOpacity(1);
        email.setOpacity(1);
        role.setOpacity(1);
        status.setOpacity(1);
    }

    private boolean matches(UserAccount user) {
        String query = search == null || search.getText() == null ? "" : search.getText().trim().toLowerCase();
        return query.isEmpty()
                || user.name.toLowerCase().contains(query)
                || user.email.toLowerCase().contains(query)
                || user.role.toLowerCase().contains(query)
                || user.status.toLowerCase().contains(query);
    }

    private void showRoleCapabilities(String roleValue) {
        detailBox.getChildren().clear();
        detailBox.getChildren().add(sectionLabel("Role Capabilities"));
        for (String capability : capabilitiesFor(roleValue)) {
            detailBox.getChildren().add(activityLine(capability));
        }
    }

    private void styleTable() {
        table.setHgap(0);
        table.setVgap(0);
        table.setPadding(new Insets(0));
        table.setStyle("-fx-background-color:white; -fx-border-color:#dfe3ea; -fx-border-radius:16; -fx-background-radius:16;");
    }

    private Label cell(String text, boolean header) {
        Label cell = new Label(text);
        cell.setMinWidth(132);
        cell.setMinHeight(header ? 48 : 54);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 14, 0, 14));
        cell.setStyle(
                "-fx-background-color:" + (header ? "#f7f9fc" : "white") + ";" +
                "-fx-text-fill:" + (header ? TEXT : "#111827") + ";" +
                "-fx-font-size:12;" +
                "-fx-font-weight:" + (header ? "bold" : "normal") + ";" +
                "-fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;"
        );
        return cell;
    }

    private HBox statusBadge(String text) {
        Label badge = new Label(text);
        badge.setPadding(new Insets(5, 12, 5, 12));
        badge.setStyle("-fx-background-color:" + statusBackground(text) + "; -fx-background-radius:12; -fx-text-fill:" + statusColor(text) + "; -fx-font-size:11; -fx-font-weight:bold;");

        HBox cell = new HBox(badge);
        cell.setMinWidth(132);
        cell.setMinHeight(54);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 12, 0, 12));
        cell.setStyle("-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return cell;
    }

    private HBox actionCell(int index) {
        Button view = linkButton("View");
        Button edit = linkButton("Edit");
        Button deactivate = linkButton("Deactivate");
        view.setOnAction(event -> startView(index));
        edit.setOnAction(event -> startEdit(index));
        deactivate.setOnAction(event -> deactivateUser(index));

        HBox cell = new HBox(8, view, divider(), edit, divider(), deactivate);
        cell.setMinWidth(188);
        cell.setMinHeight(54);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPadding(new Insets(0, 8, 0, 8));
        cell.setStyle("-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return cell;
    }

    private Button linkButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:transparent; -fx-text-fill:" + BLUE + "; -fx-font-size:11; -fx-font-weight:bold; -fx-cursor:hand; -fx-padding:0;");
        return button;
    }

    private Label divider() {
        Label divider = new Label("|");
        divider.setStyle("-fx-text-fill:#d1d5db; -fx-font-size:14;");
        return divider;
    }

    private VBox labeledControl(String labelText, Parent field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill:#111827; -fx-font-size:14; -fx-font-weight:bold;");
        return new VBox(8, label, field);
    }

    private HBox detailRow(String labelText, String value) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill:#747884; -fx-font-size:13; -fx-font-weight:bold;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill:#111827; -fx-font-size:13; -fx-font-weight:bold;");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox row = new HBox(10, label, space, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 0, 6, 0));
        row.setStyle("-fx-border-color:#e5e7eb; -fx-border-width:0 0 1 0;");
        return row;
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:#05070c; -fx-font-size:18; -fx-font-weight:bold;");
        return label;
    }

    private Label activityLine(String text) {
        Label label = new Label("• " + text);
        label.setStyle("-fx-text-fill:#747884; -fx-font-size:13;");
        return label;
    }

    private HBox confirmDeactivateRow(int index) {
        Button cancel = secondaryButton("Cancel");
        Button deactivate = dangerButton("Deactivate");
        cancel.setPrefWidth(110);
        deactivate.setPrefWidth(130);

        cancel.setOnAction(event -> startView(index));
        deactivate.setOnAction(event -> {
            UserAccount user = users.get(index);
            user.status = "Deactivated";
            formSubtitle.setText("User deactivated successfully");
            refreshTable();
            refreshStats();
            startView(index);
        });

        HBox row = new HBox(10, cancel, deactivate);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private String[] capabilitiesFor(String roleValue) {
        if ("Admin".equals(roleValue)) {
            return new String[]{"Manage ports", "Manage users", "View reports", "Manage settings"};
        }
        if ("Port Authority".equals(roleValue)) {
            return new String[]{"View ports", "Manage port operations", "View vessels", "View reports"};
        }
        if ("Customs Officer".equals(roleValue)) {
            return new String[]{"View cargo", "Manage inspections", "View documents"};
        }
        if ("Ship Captain".equals(roleValue)) {
            return new String[]{"View assigned vessel", "View port information", "Track vessel"};
        }
        if ("Exporter".equals(roleValue)) {
            return new String[]{"Manage cargo", "Upload documents", "Track shipment"};
        }
        return new String[]{"View cargo", "Track shipment", "View documents"};
    }

    private VBox statBox(String title, String value, String color, LabelSetter setter) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#475467; -fx-font-size:12; -fx-font-weight:bold;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill:" + color + "; -fx-font-size:28; -fx-font-weight:bold;");
        setter.set(valueLabel);

        VBox card = new VBox(6, titleLabel, valueLabel);
        card.setPrefWidth(210);
        card.setMinHeight(112);
        card.setPadding(new Insets(18, 20, 16, 20));
        card.setStyle(CARD_STYLE);
        return card;
    }

    private String statusColor(String value) {
        if ("Active".equals(value)) return "#0aa072";
        if ("Deactivated".equals(value)) return "#ef4444";
        return "#d97706";
    }

    private String statusBackground(String value) {
        if ("Active".equals(value)) return "#c8f5df";
        if ("Deactivated".equals(value)) return "#fee2e2";
        return "#fff3cf";
    }

    private interface LabelSetter {
        void set(Label label);
    }

    public static class UserAccount {
        public String name;
        public String email;
        public String role;
        public String status;
        public String created;
        public String lastLogin;

        public UserAccount(String name, String email, String role, String status, String created, String lastLogin) {
            this.name = name;
            this.email = email;
            this.role = role;
            this.status = status;
            this.created = created;
            this.lastLogin = lastLogin;
        }
    }
}
