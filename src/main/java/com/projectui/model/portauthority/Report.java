package com.projectui.model.portauthority;

public class Report {

    private String reportId;
    private String reportName;
    private String generatedDate;
    private String status;

    // =====================================================
    // CLOUDINARY PDF DATA
    // =====================================================

    private String pdfUrl;
    private String cloudinaryPublicId;


    // =====================================================
    // REQUIRED BY FIRESTORE
    // =====================================================

    public Report() {
    }


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public Report(
            String reportId,
            String reportName,
            String generatedDate,
            String status
    ) {

        this.reportId = reportId;
        this.reportName = reportName;
        this.generatedDate = generatedDate;
        this.status = status;
    }


    // =====================================================
    // GETTERS
    // =====================================================

    public String getReportId() {
        return reportId;
    }


    public String getReportName() {
        return reportName;
    }


    public String getGeneratedDate() {
        return generatedDate;
    }


    public String getStatus() {
        return status;
    }


    public String getPdfUrl() {
        return pdfUrl;
    }


    public String getCloudinaryPublicId() {
        return cloudinaryPublicId;
    }


    // =====================================================
    // SETTERS
    // =====================================================

    public void setReportId(
            String reportId
    ) {

        this.reportId = reportId;
    }


    public void setReportName(
            String reportName
    ) {

        this.reportName = reportName;
    }


    public void setGeneratedDate(
            String generatedDate
    ) {

        this.generatedDate = generatedDate;
    }


    public void setStatus(
            String status
    ) {

        this.status = status;
    }


    public void setPdfUrl(
            String pdfUrl
    ) {

        this.pdfUrl = pdfUrl;
    }


    public void setCloudinaryPublicId(
            String cloudinaryPublicId
    ) {

        this.cloudinaryPublicId =
                cloudinaryPublicId;
    }
}