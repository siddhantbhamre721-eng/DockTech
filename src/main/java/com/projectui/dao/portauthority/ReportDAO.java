package com.projectui.dao.portauthority;

import java.util.ArrayList;
import java.util.List;

import com.projectui.config.portauthority.FirebaseConfig;
import com.projectui.model.portauthority.Report;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;


// =====================================================
// REPORT DAO
// =====================================================

public class ReportDAO {

    private Firestore db =
            FirebaseConfig.getFirestore();


    // =====================================================
    // SAVE REPORT
    // =====================================================

    public void saveReport(
            Report report
    ) {

        try {

            db.collection("Reports")
              .document(report.getReportId())
              .create(report)
              .get();

            System.out.println(
                    "Report Data Inserted"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // GET ONE REPORT
    // =====================================================

    public Report getReport(
            String reportId
    ) {

        try {

            ApiFuture<DocumentSnapshot> future =
                    db.collection("Reports")
                      .document(reportId)
                      .get();


            DocumentSnapshot document =
                    future.get();


            if (document.exists()) {

                return document.toObject(
                        Report.class
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }


        return null;
    }


    // =====================================================
    // UPDATE REPORT
    // =====================================================

    public void updateReport(
                Report report
        ) {

            try {

                db.collection("Reports")
                .document(report.getReportId())
                .update(

                        "reportName",
                        report.getReportName(),

                        "generatedDate",
                        report.getGeneratedDate(),

                        "status",
                        report.getStatus(),

                        "pdfUrl",
                        report.getPdfUrl(),

                        "cloudinaryPublicId",
                        report.getCloudinaryPublicId()
                )
                .get();


                System.out.println(
                        "Report Data Updated"
                );

            } catch (Exception e) {

                e.printStackTrace();
            }
    }


    // =====================================================
    // DELETE REPORT
    // =====================================================

    public void deleteReport(
            String reportId
    ) {

        try {

            db.collection("Reports")
              .document(reportId)
              .delete()
              .get();


            System.out.println(
                    "Report Data Deleted"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // GET ALL REPORTS
    // =====================================================

    public List<Report> getReports() {

        List<Report> list =
                new ArrayList<>();


        try {

            ApiFuture<QuerySnapshot> future =
                    db.collection("Reports")
                      .get();


            QuerySnapshot snapshot =
                    future.get();


            for (
                    DocumentSnapshot doc :
                    snapshot.getDocuments()
            ) {

                Report report =
                        doc.toObject(
                                Report.class
                        );


                if (report != null) {

                    list.add(
                            report
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }


        return list;
    }
}