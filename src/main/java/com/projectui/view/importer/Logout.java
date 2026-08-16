package com.projectui.view.importer;



import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Logout {

    public void showLogout() {

        Stage stage =
                new Stage();

        stage.initModality(
                Modality.APPLICATION_MODAL
        );

        stage.initOwner(
                ImporterDashboard.importerStage
        );

        VBox root =
                new VBox(20);

        root.setAlignment(
                Pos.CENTER
        );

        root.setPadding(
                new Insets(40)
        );

        root.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:25px;"
        );

        // =====================================================
        // ICON
        // =====================================================

        Text icon =
                new Text("⇥");

        icon.setStyle(
                "-fx-fill:#287FF0;" +
                "-fx-font-size:40px;"
        );

        // =====================================================
        // TITLE
        // =====================================================

        Text title =
                new Text(
                        "Logout from Dock Tech?"
                );

        title.setStyle(
                "-fx-font-size:24px;" +
                "-fx-font-weight:bold;"
        );

        // =====================================================
        // MESSAGE
        // =====================================================

        Text message =
                new Text(
                        "Are you sure you want to logout from your\n" +
                        "Importer account?"
                );

        message.setTextAlignment(
                javafx.scene.text.TextAlignment.CENTER
        );

        message.setStyle(
                "-fx-fill:#778394;" +
                "-fx-font-size:14px;"
        );

        // =====================================================
        // BUTTONS
        // =====================================================

        HBox buttons =
                new HBox(12);

        buttons.setAlignment(
                Pos.CENTER
        );

        Button cancel =
                new Button(
                        "Cancel"
                );

        cancel.setPrefWidth(145);

        cancel.setPrefHeight(45);

        cancel.setStyle(
                "-fx-background-color:#F1F4F8;" +
                "-fx-background-radius:10px;" +
                "-fx-font-weight:bold;"
        );

        cancel.setOnAction(
                e -> stage.close()
        );

        Button logout =
                new Button(
                        "Logout"
                );

        logout.setPrefWidth(145);

        logout.setPrefHeight(45);

        logout.setStyle(
                "-fx-background-color:#287FF0;" +
                "-fx-text-fill:white;" +
                "-fx-background-radius:10px;" +
                "-fx-font-weight:bold;"
        );

        logout.setOnAction(event -> {
            stage.close();
            if (ImporterDashboard.importerStage != null) {
                new com.projectui.view.login.LoginPage().show(ImporterDashboard.importerStage);
            }
        });

        buttons.getChildren().addAll(
                cancel,
                logout
        );

        root.getChildren().addAll(
                icon,
                title,
                message,
                buttons
        );

        Scene scene =
                new Scene(
                        root,
                        520,
                        350
                );

        scene.setFill(
                Color.TRANSPARENT
        );

        stage.setScene(scene);

        stage.setTitle(
                "Logout Confirmation"
        );

        stage.setResizable(false);

        stage.showAndWait();
    }
}
