package com.projectui.view.admin;

import javafx.scene.Parent;

/**
 * Compatibility wrapper for the administrative root component.
 * Delegates to `AdminDashboard` which provides the same API.
 */
public class DockTechAdmin {

    public Parent getRoot(Runnable logoutAction, String pageName) {
        return new AdminDashboard().getRoot(logoutAction, pageName);
    }
}
