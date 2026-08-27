package com.projectui.service.portauthority;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.projectui.model.portauthority.Report;
import com.projectui.model.portauthority.Ship;
import com.projectui.model.portauthority.Cargo;
import com.projectui.model.portauthority.Container;
import com.projectui.model.portauthority.Payment;


// =========================================================
// DOCKTECH PDF REPORT GENERATOR
// =========================================================

public class PdfReportGenerator {


    // =====================================================
    // GENERATE OVERALL REPORT
    // =====================================================

    public static void generateReport(

            Report report,

            File file,

            List<Ship> ships,

            List<Cargo> cargos,

            List<Container> containers,

            List<Payment> payments

    ) throws Exception {


        // =================================================
        // CREATE A4 PORTRAIT DOCUMENT
        // =================================================

        Document document =
                new Document(
                        PageSize.A4,
                        40,
                        40,
                        45,
                        45
                );


        PdfWriter.getInstance(
                document,
                new FileOutputStream(file)
        );


        document.open();


        // =================================================
        // FONTS
        // =================================================

        Font brandFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        21
                );


        Font titleFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        17
                );


        Font sectionFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        14
                );


        Font recordTitleFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        11
                );


        Font labelFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        9
                );


        Font normalFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        9
                );


        Font smallFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        8
                );


        Font footerFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_OBLIQUE,
                        8
                );


        // =================================================
        // PAGE 1
        // REPORT INFORMATION + EXECUTIVE SUMMARY
        // =================================================

        addMainHeader(
                document,
                brandFont,
                titleFont
        );


        // -------------------------------------------------
        // REPORT INFORMATION
        // -------------------------------------------------

        addSectionHeading(
                document,
                "1. Report Information",
                sectionFont
        );


        PdfPTable reportInfo =
                new PdfPTable(2);


        reportInfo.setWidthPercentage(
                100
        );


        reportInfo.setWidths(
                new float[]{
                        30,
                        70
                }
        );


        addInfoRow(
                reportInfo,
                "Report ID",
                safe(
                        report.getReportId()
                ),
                labelFont,
                normalFont
        );


        addInfoRow(
                reportInfo,
                "Report Name",
                safe(
                        report.getReportName()
                ),
                labelFont,
                normalFont
        );


        addInfoRow(
                reportInfo,
                "Status",
                safe(
                        report.getStatus()
                ),
                labelFont,
                normalFont
        );


        addInfoRow(
                reportInfo,
                "Generated Date",
                LocalDate.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "dd MMM yyyy"
                                )
                        ),
                labelFont,
                normalFont
        );


        document.add(
                reportInfo
        );


        document.add(
                new Paragraph(" ")
        );


        // -------------------------------------------------
        // EXECUTIVE SUMMARY
        // -------------------------------------------------

        addSectionHeading(
                document,
                "2. Executive Summary",
                sectionFont
        );


        int totalShips =
                ships == null
                        ? 0
                        : ships.size();


        double totalCargoWeight =
                calculateCargoWeight(
                        cargos
                );


        int allocatedContainers =
                calculateAllocatedContainers(
                        containers
                );


        double totalRevenue =
                calculateRevenue(
                        payments
                );


        PdfPTable summaryTable =
                new PdfPTable(2);


        summaryTable.setWidthPercentage(
                100
        );


        summaryTable.setWidths(
                new float[]{
                        50,
                        50
                }
        );


        addSummaryCell(
                summaryTable,
                "TOTAL SHIPS",
                String.valueOf(
                        totalShips
                ),
                labelFont,
                titleFont
        );


        addSummaryCell(
                summaryTable,
                "CARGO WEIGHT",
                formatNumber(
                        totalCargoWeight
                )
                + " Tons",
                labelFont,
                titleFont
        );


        addSummaryCell(
                summaryTable,
                "ALLOCATED CONTAINERS",
                String.valueOf(
                        allocatedContainers
                ),
                labelFont,
                titleFont
        );


        addSummaryCell(
                summaryTable,
                "TOTAL REVENUE",
                formatCurrency(
                        totalRevenue
                ),
                labelFont,
                titleFont
        );


        document.add(
                summaryTable
        );


        document.add(
                new Paragraph(" ")
        );


        addFooter(
                document,
                footerFont
        );


        // =================================================
        // PAGE 2
        // SHIP OPERATIONS
        // =================================================

        document.newPage();


        addSectionHeading(
                document,
                "3. Ship Operations",
                sectionFont
        );


        document.add(
                new Paragraph(
                        "Complete list of ships recorded in the "
                        + "port authority system.",
                        normalFont
                )
        );


        document.add(
                new Paragraph(" ")
        );


        if (ships == null || ships.isEmpty()) {

            addNoDataMessage(
                    document,
                    "No ship records available.",
                    normalFont
            );

        } else {

            int shipNumber =
                    1;


            for (Ship ship : ships) {

                if (ship == null) {
                    continue;
                }


                addRecordTitle(
                        document,
                        "Ship " + shipNumber
                                + " — "
                                + safe(
                                        ship.getShipId()
                                ),
                        recordTitleFont
                );


                PdfPTable table =
                        createRecordTable();


                addInfoRow(
                        table,
                        "Ship ID",
                        safe(
                                ship.getShipId()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Ship Name",
                        safe(
                                ship.getShipName()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Ship Type",
                        safe(
                                ship.getShipType()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "IMO Number",
                        safe(
                                ship.getImoNumber()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Company",
                        safe(
                                ship.getCompany()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Arrival",
                        safe(
                                ship.getArrival()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Departure",
                        safe(
                                ship.getDeparture()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Berth",
                        safe(
                                ship.getBerth()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Status",
                        safe(
                                ship.getStatus()
                        ),
                        labelFont,
                        normalFont
                );


                document.add(
                        table
                );


                document.add(
                        new Paragraph(" ")
                );


                shipNumber++;
            }
        }


        addFooter(
                document,
                footerFont
        );


        // =================================================
        // PAGE 3
        // CARGO OPERATIONS
        // =================================================

        document.newPage();


        addSectionHeading(
                document,
                "4. Cargo Operations",
                sectionFont
        );


        document.add(
                new Paragraph(
                        "Complete list of cargo handled by the port.",
                        normalFont
                )
        );


        document.add(
                new Paragraph(" ")
        );


        if (cargos == null || cargos.isEmpty()) {

            addNoDataMessage(
                    document,
                    "No cargo records available.",
                    normalFont
            );

        } else {

            int cargoNumber =
                    1;


            for (Cargo cargo : cargos) {

                if (cargo == null) {
                    continue;
                }


                addRecordTitle(
                        document,
                        "Cargo " + cargoNumber
                                + " — "
                                + safe(
                                        cargo.getCargoId()
                                ),
                        recordTitleFont
                );


                PdfPTable table =
                        createRecordTable();


                addInfoRow(
                        table,
                        "Cargo ID",
                        safe(
                                cargo.getCargoId()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Ship ID",
                        safe(
                                cargo.getShip()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Cargo Type",
                        safe(
                                cargo.getCargoType()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Weight",
                        safe(
                                cargo.getWeight()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Origin",
                        safe(
                                cargo.getOrigin()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Destination",
                        safe(
                                cargo.getDestination()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Containers",
                        safe(
                                cargo.getContainers()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Status",
                        safe(
                                cargo.getStatus()
                        ),
                        labelFont,
                        normalFont
                );


                document.add(
                        table
                );


                document.add(
                        new Paragraph(" ")
                );


                cargoNumber++;
            }
        }


        addFooter(
                document,
                footerFont
        );


        // =================================================
        // PAGE 4
        // CONTAINER OPERATIONS
        // =================================================

        document.newPage();


        addSectionHeading(
                document,
                "5. Container Operations",
                sectionFont
        );


        document.add(
                new Paragraph(
                        "Complete container allocation and movement records.",
                        normalFont
                )
        );


        document.add(
                new Paragraph(" ")
        );


        if (
                containers == null
                ||
                containers.isEmpty()
        ) {

            addNoDataMessage(
                    document,
                    "No container records available.",
                    normalFont
            );

        } else {

            int containerNumber =
                    1;


            for (Container container : containers) {

                if (container == null) {
                    continue;
                }


                addRecordTitle(
                        document,
                        "Container " + containerNumber
                                + " — "
                                + safe(
                                        container.getContainerId()
                                ),
                        recordTitleFont
                );


                PdfPTable table =
                        createRecordTable();


                addInfoRow(
                        table,
                        "Container ID",
                        safe(
                                container.getContainerId()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Type",
                        safe(
                                container.getType()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Cargo ID",
                        safe(
                                container.getCargoId()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Ship ID",
                        safe(
                                container.getShip()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Location",
                        safe(
                                container.getLocation()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Size",
                        safe(
                                container.getSize()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Weight",
                        safe(
                                container.getWeight()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Status",
                        safe(
                                container.getStatus()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Allocation Date",
                        safe(
                                container.getAllocationDate()
                        ),
                        labelFont,
                        normalFont
                );


                document.add(
                        table
                );


                document.add(
                        new Paragraph(" ")
                );


                containerNumber++;
            }
        }


        addFooter(
                document,
                footerFont
        );


        // =================================================
        // PAGE 5
        // PAYMENT TRANSACTIONS
        // =================================================

        document.newPage();


        addSectionHeading(
                document,
                "6. Payment Transactions",
                sectionFont
        );


        document.add(
                new Paragraph(
                        "Complete list of financial transactions "
                        + "recorded by the system.",
                        normalFont
                )
        );


        document.add(
                new Paragraph(" ")
        );


        if (
                payments == null
                ||
                payments.isEmpty()
        ) {

            addNoDataMessage(
                    document,
                    "No payment records available.",
                    normalFont
            );

        } else {

            int paymentNumber =
                    1;


            for (Payment payment : payments) {

                if (payment == null) {
                    continue;
                }


                addRecordTitle(
                        document,
                        "Payment " + paymentNumber
                                + " — "
                                + safe(
                                        payment.getPaymentId()
                                ),
                        recordTitleFont
                );


                PdfPTable table =
                        createRecordTable();


                addInfoRow(
                        table,
                        "Payment ID",
                        safe(
                                payment.getPaymentId()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Cargo ID",
                        safe(
                                payment.getCargoId()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Ship",
                        safe(
                                payment.getShip()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Customer",
                        safe(
                                payment.getCustomer()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Amount",
                        formatCurrency(
                                payment.getAmount()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Date",
                        safe(
                                payment.getDate()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Method",
                        safe(
                                payment.getMethod()
                        ),
                        labelFont,
                        normalFont
                );


                addInfoRow(
                        table,
                        "Status",
                        safe(
                                payment.getStatus()
                        ),
                        labelFont,
                        normalFont
                );


                document.add(
                        table
                );


                document.add(
                        new Paragraph(" ")
                );


                paymentNumber++;
            }
        }


        addFooter(
                document,
                footerFont
        );


        // =================================================
        // PAGE 6
        // REPORT OVERVIEW
        // =================================================

        document.newPage();


        addSectionHeading(
                document,
                "7. Report Overview",
                sectionFont
        );


        Paragraph overview =
                new Paragraph(
                        "This report provides a consolidated overview "
                        + "of ship operations, cargo handling, container "
                        + "operations and payment transactions recorded "
                        + "in the DockTech Port Authority system.",
                        normalFont
                );


        overview.setLeading(
                14
        );


        document.add(
                overview
        );


        document.add(
                new Paragraph(" ")
        );


        PdfPTable overviewTable =
                new PdfPTable(2);


        overviewTable.setWidthPercentage(
                100
        );


        overviewTable.setWidths(
                new float[]{
                        45,
                        55
                }
        );


        addInfoRow(
                overviewTable,
                "Total Ship Records",
                String.valueOf(
                        totalShips
                ),
                labelFont,
                normalFont
        );


        addInfoRow(
                overviewTable,
                "Total Cargo Weight",
                formatNumber(
                        totalCargoWeight
                )
                + " Tons",
                labelFont,
                normalFont
        );


        addInfoRow(
                overviewTable,
                "Allocated Containers",
                String.valueOf(
                        allocatedContainers
                ),
                labelFont,
                normalFont
        );


        addInfoRow(
                overviewTable,
                "Payment Transactions",
                String.valueOf(
                        payments == null
                                ? 0
                                : payments.size()
                ),
                labelFont,
                normalFont
        );


        addInfoRow(
                overviewTable,
                "Total Revenue",
                formatCurrency(
                        totalRevenue
                ),
                labelFont,
                normalFont
        );


        document.add(
                overviewTable
        );


        document.add(
                new Paragraph(" ")
        );


        Paragraph closing =
                new Paragraph(
                        "End of Report",
                        recordTitleFont
                );


        closing.setAlignment(
                Element.ALIGN_CENTER
        );


        closing.setSpacingBefore(
                30
        );


        document.add(
                closing
        );


        addFooter(
                document,
                footerFont
        );


        // =================================================
        // CLOSE DOCUMENT
        // =================================================

        document.close();
    }


    // =====================================================
    // MAIN HEADER
    // =====================================================

    private static void addMainHeader(

            Document document,

            Font brandFont,

            Font titleFont

    ) throws Exception {


        Paragraph brand =
                new Paragraph(
                        "DOCKTECH PORT AUTHORITY",
                        brandFont
                );


        brand.setAlignment(
                Element.ALIGN_CENTER
        );


        brand.setSpacingAfter(
                5
        );


        document.add(
                brand
        );


        Paragraph subtitle =
                new Paragraph(
                        "Overall Port Operations Report",
                        titleFont
                );


        subtitle.setAlignment(
                Element.ALIGN_CENTER
        );


        subtitle.setSpacingAfter(
                20
        );


        document.add(
                subtitle
        );
    }


    // =====================================================
    // SECTION HEADING
    // =====================================================

    private static void addSectionHeading(

            Document document,

            String text,

            Font font

    ) throws Exception {


        Paragraph heading =
                new Paragraph(
                        text,
                        font
                );


        heading.setSpacingAfter(
                10
        );


        document.add(
                heading
        );
    }


    // =====================================================
    // RECORD TITLE
    // =====================================================

    private static void addRecordTitle(

            Document document,

            String text,

            Font font

    ) throws Exception {


        Paragraph title =
                new Paragraph(
                        text,
                        font
                );


        title.setSpacingBefore(
                5
        );


        title.setSpacingAfter(
                5
        );


        document.add(
                title
        );
    }


    // =====================================================
    // CREATE RECORD TABLE
    // =====================================================

    private static PdfPTable createRecordTable()
            throws Exception {


        PdfPTable table =
                new PdfPTable(2);


        table.setWidthPercentage(
                100
        );


        table.setWidths(
                new float[]{
                        30,
                        70
                }
        );


        return table;
    }


    // =====================================================
    // INFORMATION ROW
    // =====================================================

    private static void addInfoRow(

            PdfPTable table,

            String key,

            String value,

            Font keyFont,

            Font valueFont

    ) {


        PdfPCell left =
                new PdfPCell(
                        new Phrase(
                                key,
                                keyFont
                        )
                );


        PdfPCell right =
                new PdfPCell(
                        new Phrase(
                                safe(value),
                                valueFont
                        )
                );


        left.setPadding(
                7
        );


        right.setPadding(
                7
        );


        table.addCell(
                left
        );


        table.addCell(
                right
        );
    }


    // =====================================================
    // SUMMARY CELL
    // =====================================================

    private static void addSummaryCell(

            PdfPTable table,

            String heading,

            String value,

            Font headingFont,

            Font valueFont

    ) {


        PdfPCell cell =
                new PdfPCell();


        cell.setPadding(
                12
        );


        Paragraph headingParagraph =
                new Paragraph(
                        heading,
                        headingFont
                );


        Paragraph valueParagraph =
                new Paragraph(
                        value,
                        valueFont
                );


        valueParagraph.setSpacingBefore(
                5
        );


        cell.addElement(
                headingParagraph
        );


        cell.addElement(
                valueParagraph
        );


        table.addCell(
                cell
        );
    }


    // =====================================================
    // NO DATA MESSAGE
    // =====================================================

    private static void addNoDataMessage(

            Document document,

            String message,

            Font font

    ) throws Exception {


        PdfPTable table =
                new PdfPTable(1);


        table.setWidthPercentage(
                100
        );


        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                message,
                                font
                        )
                );


        cell.setPadding(
                15
        );


        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );


        table.addCell(
                cell
        );


        document.add(
                table
        );
    }


    // =====================================================
    // FOOTER
    // =====================================================

    private static void addFooter(

            Document document,

            Font font

    ) throws Exception {


        Paragraph footer =
                new Paragraph(
                        "Generated by DockTech Port Authority System",
                        font
                );


        footer.setAlignment(
                Element.ALIGN_CENTER
        );


        footer.setSpacingBefore(
                20
        );


        document.add(
                footer
        );
    }


    // =====================================================
    // CALCULATE CARGO WEIGHT
    // =====================================================

    private static double calculateCargoWeight(

            List<Cargo> cargos

    ) {


        double total =
                0;


        if (cargos == null) {
            return 0;
        }


        for (Cargo cargo : cargos) {

            if (cargo == null) {
                continue;
            }


            String weight =
                    cargo.getWeight();


            if (weight == null) {
                continue;
            }


            try {

                String numeric =
                        weight.replaceAll(
                                "[^0-9.\\-]",
                                ""
                        );


                if (!numeric.isEmpty()) {

                    total +=
                            Double.parseDouble(
                                    numeric
                            );
                }

            } catch (Exception ignored) {
                // Ignore invalid weight values.
            }
        }


        return total;
    }


    // =====================================================
    // CALCULATE ALLOCATED CONTAINERS
    // =====================================================

    private static int calculateAllocatedContainers(

            List<Container> containers

    ) {


        int count =
                0;


        if (containers == null) {
            return 0;
        }


        for (Container container : containers) {

            if (container == null) {
                continue;
            }


            if (
                    "Allocated".equalsIgnoreCase(
                            safe(
                                    container.getStatus()
                            )
                    )
            ) {

                count++;
            }
        }


        return count;
    }


    // =====================================================
    // CALCULATE REVENUE
    // =====================================================

    private static double calculateRevenue(

            List<Payment> payments

    ) {


        double total =
                0;


        if (payments == null) {
            return 0;
        }


        for (Payment payment : payments) {

            if (payment == null) {
                continue;
            }


            total +=
                    payment.getAmount();
        }


        return total;
    }


    // =====================================================
    // FORMAT NUMBER
    // =====================================================

    private static String formatNumber(

            double value

    ) {


        return String.format(
                Locale.US,
                "%,.2f",
                value
        );
    }


    // =====================================================
    // FORMAT CURRENCY
    // =====================================================

    private static String formatCurrency(

            double amount

    ) {


        return String.format(
                Locale.US,
                "₹%,.2f",
                amount
        );
    }


    // =====================================================
    // SAFE STRING
    // =====================================================

    private static String safe(

            String value

    ) {


        if (
                value == null
                ||
                value.trim().isEmpty()
        ) {

            return "—";
        }


        return value;
    }
}