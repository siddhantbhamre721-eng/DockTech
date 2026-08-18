package com.projectui.view.importer;

import com.projectui.view.login.LoginPage;
import com.projectui.view.login.LogoutModal;

public class Logout {

    public void showLogout() {
        LogoutModal.show(ImporterDashboard.importerStage, () -> {
            if (ImporterDashboard.importerStage != null) {
                new LoginPage().show(ImporterDashboard.importerStage);
            }
        });
    }
}
