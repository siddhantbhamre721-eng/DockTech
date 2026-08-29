package com.projectui.view.ShipCaptain;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Map;

public class SettingsPage {

    private final String TEXT = "#172033";
    private final String GRAY = "#64748b";

    public VBox getContent() {
        VBox content = new VBox(22);
        content.setPadding(new Insets(25, 35, 35, 35));
        content.setStyle("-fx-background-color:#f6f8fb;");

        // HEADER
        VBox heading = new VBox(6,
                label("Ship Captain Settings", 28, true, TEXT),
                label("Manage your vessel profile and account security settings.", 14, false, GRAY)
        );

        // SUCCESS MESSAGE
        Label statusMsg = new Label();
        statusMsg.setVisible(false);
        statusMsg.setMaxWidth(Double.MAX_VALUE);
        statusMsg.setPadding(new Insets(12, 18, 12, 18));

        // GET LOGGED-IN CAPTAIN DETAILS
        Map<String, Object> user = com.projectui.view.login.LoginPage.loggedInUser;
        String nameStr = user != null && user.get("name") != null ? user.get("name").toString() : "Ship Captain";
        String emailStr = user != null && user.get("email") != null ? user.get("email").toString() : "captain@docktech.com";
        String phoneStr = user != null && user.get("phone") != null ? user.get("phone").toString() : "+91 9800000000";
        com.projectui.model.Ship assignedShip = com.projectui.dao.UserDao.getAssignedShipForCaptain(user);
        String shipStr = assignedShip != null ? assignedShip.getShipName() : (user != null && user.get("assignedShip") != null && !user.get("assignedShip").toString().isBlank() ? user.get("assignedShip").toString() : "Not Assigned");
        String portStr = user != null && user.get("assignedPort") != null ? user.get("assignedPort").toString() : "Mumbai Port";

        // PROFILE CARD
        VBox profileCard = card();

        HBox top = new HBox(
                new VBox(4,
                        label("Captain Profile", 20, true, TEXT),
                        label("Your account and assigned vessel credentials.", 13, false, GRAY)
                ),
                new Region()
        );
        HBox.setHgrow(top.getChildren().get(1), Priority.ALWAYS);

        Label avatar = new Label("CAP");
        avatar.setMinSize(52, 52);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle("-fx-background-color:#dbeafe; -fx-background-radius:50; -fx-text-fill:#1769ff; -fx-font-size:18px; -fx-font-weight:bold;");
        top.getChildren().add(avatar);

        GridPane profileGrid = new GridPane();
        profileGrid.setHgap(16);
        profileGrid.setVgap(14);

        profileGrid.add(fieldNode("Captain Name", nameStr), 0, 0);
        profileGrid.add(fieldNode("Email Address", emailStr), 1, 0);
        profileGrid.add(fieldNode("Phone Number", phoneStr), 2, 0);
        profileGrid.add(fieldNode("Assigned Ship", shipStr), 0, 1);
        profileGrid.add(fieldNode("Assigned Port", portStr), 1, 1);
        profileGrid.add(fieldNode("Role", "Ship Captain"), 2, 1);

        profileCard.getChildren().addAll(top, profileGrid);

        // SECURITY CARD
        VBox securityCard = card();

        VBox secHead = new VBox(4,
                label("Security & Password Management", 20, true, TEXT),
                label("Update your authentication password for secure access.", 13, false, GRAY)
        );

        GridPane secGrid = new GridPane();
        secGrid.setHgap(16);
        secGrid.setVgap(14);

        PasswordField currentPass = new PasswordField();
        currentPass.setPromptText("Enter current password");
        currentPass.setPrefWidth(220);
        currentPass.setStyle("-fx-font-size:13px; -fx-padding:8 12;");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("Enter new password");
        newPass.setPrefWidth(220);
        newPass.setStyle("-fx-font-size:13px; -fx-padding:8 12;");

        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirm new password");
        confirmPass.setPrefWidth(220);
        confirmPass.setStyle("-fx-font-size:13px; -fx-padding:8 12;");

        secGrid.add(new VBox(4, label("Current Password", 12, true, GRAY), currentPass), 0, 0);
        secGrid.add(new VBox(4, label("New Password", 12, true, GRAY), newPass), 1, 0);
        secGrid.add(new VBox(4, label("Confirm New Password", 12, true, GRAY), confirmPass), 2, 0);

        Button updatePassBtn = new Button("Update Password");
        updatePassBtn.setStyle("-fx-background-color:#1769ff; -fx-text-fill:white; -fx-font-size:13px; -fx-font-weight:bold; -fx-padding:8 18; -fx-background-radius:6; -fx-cursor:hand;");

        updatePassBtn.setOnAction(e -> {
            String np = newPass.getText().trim();
            String cp = confirmPass.getText().trim();

            if (np.isEmpty() || cp.isEmpty()) {
                showMsg(statusMsg, "Please enter and confirm your new password.", "#Fee2e2", "#B91c1c");
                return;
            }

            if (!np.equals(cp)) {
                showMsg(statusMsg, "New password and Confirm password do not match.", "#Fee2e2", "#B91c1c");
                return;
            }

            if (np.length() < 6) {
                showMsg(statusMsg, "Password must be at least 6 characters long.", "#Fee2e2", "#B91c1c");
                return;
            }

            String uidOrEmail = emailStr;
            if (user != null && user.get("uid") != null) {
                uidOrEmail = user.get("uid").toString();
            }

            new com.projectui.service.FirebaseAuthService().updatePassword(uidOrEmail, np)
                    .whenComplete((res, err) -> javafx.application.Platform.runLater(() -> {
                        if (err != null) {
                            showMsg(statusMsg, "Password update failed: " + err.getMessage(), "#Fee2e2", "#B91c1c");
                        } else {
                            currentPass.clear();
                            newPass.clear();
                            confirmPass.clear();
                            showMsg(statusMsg, "✓ Password updated successfully in Firebase Authentication.", "#E8f8ef", "#168548");
                        }
                    }));
        });

        securityCard.getChildren().addAll(secHead, secGrid, updatePassBtn);

        content.getChildren().addAll(heading, statusMsg, profileCard, securityCard);
        return content;
    }

    private void showMsg(Label lbl, String text, String bgHex, String fgHex) {
        lbl.setText(text);
        lbl.setStyle("-fx-background-color:" + bgHex + "; -fx-text-fill:" + fgHex + "; -fx-background-radius:8; -fx-font-size:14px; -fx-font-weight:bold;");
        lbl.setVisible(true);
    }

    private VBox card() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color:white; -fx-background-radius:10; -fx-border-color:#e2e8f0; -fx-border-radius:10;");
        return box;
    }

    private VBox fieldNode(String labelText, String valueText) {
        VBox box = new VBox(4);
        box.setPrefWidth(220);
        Label l = new Label(labelText);
        l.setStyle("-fx-font-size:12px; -fx-text-fill:#64748b; -fx-font-weight:bold;");
        Label v = new Label(valueText);
        v.setStyle("-fx-font-size:14px; -fx-text-fill:#172033; -fx-font-weight:bold; -fx-background-color:#f8fafc; -fx-padding:8 12; -fx-background-radius:6; -fx-border-color:#e2e8f0; -fx-border-radius:6;");
        box.getChildren().addAll(l, v);
        return box;
    }

    private Label label(String text, int size, boolean bold, String colorHex) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:" + size + "px; -fx-text-fill:" + colorHex + ";" + (bold ? " -fx-font-weight:bold;" : ""));
        return l;
    }
}
