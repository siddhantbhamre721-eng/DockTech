package com.projectui;

import com.projectui.view.Homepage;
import com.projectui.view.exporter.ExporterDashboard;
import com.projectui.view.exporter.ExporterDashboard;
import com.projectui.view.importer.ImporterDashboard;

import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        Application.launch(ExporterDashboard.class,args);
        //Application.launch(ImporterDashboard.class,args);
    }
}