package com.projectui.controller.portauthority;

import java.util.List;

import com.projectui.dao.portauthority.ReportDAO;
import com.projectui.model.portauthority.Report;


// =====================================================
// REPORT CONTROLLER
// =====================================================

public class ReportController {

    private ReportDAO reportDAO =
            new ReportDAO();


    // =====================================================
    // SAVE REPORT
    // =====================================================

    public void saveReport(
            Report report
    ) {

        reportDAO.saveReport(
                report
        );
    }


    // =====================================================
    // GET ONE REPORT
    // =====================================================

    public Report getReport(
            String reportId
    ) {

        return reportDAO.getReport(
                reportId
        );
    }


    // =====================================================
    // UPDATE REPORT
    // =====================================================

    public void updateReport(
            Report report
    ) {

        reportDAO.updateReport(
                report
        );
    }


    // =====================================================
    // DELETE REPORT
    // =====================================================

    public void deleteReport(
            String reportId
    ) {

        reportDAO.deleteReport(
                reportId
        );
    }


    // =====================================================
    // GET ALL REPORTS
    // =====================================================

    public List<Report> getReports() {

        return reportDAO.getReports();
    }
}