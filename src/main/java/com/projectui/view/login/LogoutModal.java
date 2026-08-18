package com.projectui.view.login;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class LogoutModal {

    public static void show(Window ownerWindow, Runnable onLogoutConfirm) {
        Stage ownerStage = null;
        if (ownerWindow instanceof Stage s) {
            ownerStage = s;
        } else if (ownerWindow != null && ownerWindow.getScene() != null && ownerWindow.getScene().getWindow() instanceof Stage s) {
            ownerStage = s;
        }

        Stage modal = new Stage();
        modal.initStyle(StageStyle.TRANSPARENT);
        modal.initModality(Modality.APPLICATION_MODAL);
        if (ownerStage != null) {
            modal.initOwner(ownerStage);
        }

        // Semi-transparent backdrop overlay keeping current page visible
        StackPane overlayRoot = new StackPane();
        overlayRoot.setStyle("-fx-background-color: rgba(2, 11, 36, 0.55);");

        // Modal card container
        VBox modalCard = new VBox(20);
        modalCard.setAlignment(Pos.CENTER);
        modalCard.setPadding(new Insets(32, 40, 32, 40));
        modalCard.setMaxWidth(460);
        modalCard.setMaxHeight(290);
        modalCard.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 20px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.30), 30, 0, 0, 10);"
        );

        // Logout icon
        Label iconLabel = new Label("⇥");
        iconLabel.setMinSize(56, 56);
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setStyle(
            "-fx-text-fill: #287FF0;" +
            "-fx-background-color: rgba(40, 127, 240, 0.12);" +
            "-fx-background-radius: 28px;" +
            "-fx-font-size: 30px;" +
            "-fx-font-weight: bold;"
        );

        // Title
        Label title = new Label("Logout from Dock Tech?");
        title.setStyle(
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #0F172A;"
        );

        // Message
        Label message = new Label("Are you sure you want to logout?");
        message.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #64748B;" +
            "-fx-text-alignment: center;"
        );

        // Buttons row
        HBox buttons = new HBox(14);
        buttons.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(145);
        cancelBtn.setPrefHeight(45);
        cancelBtn.setStyle(
            "-fx-background-color: #F1F4F8;" +
            "-fx-text-fill: #475569;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10px;" +
            "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> modal.close());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setPrefWidth(145);
        logoutBtn.setPrefHeight(45);
        logoutBtn.setStyle(
            "-fx-background-color: #287FF0;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(40, 127, 240, 0.25), 10, 0, 0, 4);"
        );
        logoutBtn.setOnAction(e -> {
            modal.close();
            if (onLogoutConfirm != null) {
                onLogoutConfirm.run();
            }
        });

        buttons.getChildren().addAll(cancelBtn, logoutBtn);
        modalCard.getChildren().addAll(iconLabel, title, message, buttons);
        overlayRoot.getChildren().add(modalCard);

        Scene scene = new Scene(overlayRoot);
        scene.setFill(Color.TRANSPARENT);
        LoginPage.applyInvisibleScrollbar(scene);
        LoginPage.applyInvisibleScrollbar(modal);

        modal.setScene(scene);

        if (ownerStage != null && ownerStage.isShowing()) {
            modal.setX(ownerStage.getX());
            modal.setY(ownerStage.getY());
            modal.setWidth(ownerStage.getWidth());
            modal.setHeight(ownerStage.getHeight());
        } else {
            modal.setWidth(600);
            modal.setHeight(400);
        }

        modal.show();
    }
}
