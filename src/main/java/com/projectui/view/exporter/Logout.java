package com.projectui.view.exporter;

import com.projectui.view.login.LoginPage;
import com.projectui.view.login.LogoutModal;

public class Logout {

    public void showLogout() {
        LogoutModal.show(ExporterDashboard.dashboardStage, () -> {
            if (ExporterDashboard.dashboardStage != null) {
                new LoginPage().show(ExporterDashboard.dashboardStage);
            }
        });
    }
}
