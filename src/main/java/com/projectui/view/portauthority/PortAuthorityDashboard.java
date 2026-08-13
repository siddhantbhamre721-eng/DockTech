package com.projectui.view.portauthority;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PortAuthorityDashboard extends Application {

    @Override
        public void start(Stage stage) {

                PortAuthorityLayout layout =
                        new PortAuthorityLayout();

                DashboardPage dashboardPage =
                        new DashboardPage();

                layout.setContent(
                        dashboardPage.getRoot(),
                        "Dashboard",
                        "Dock Tech / Port Authority / Dashboard"
                        );

                Scene scene = new Scene(
                                layout.getRoot()
                                );

                stage.setTitle(
                        "Dock Tech - Port Authority"
                        );

                stage.setScene(scene);

                stage.setMaximized(true);

                stage.show();
        }

}