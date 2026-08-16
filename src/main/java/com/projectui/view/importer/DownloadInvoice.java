package com.projectui.view.importer;



import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DownloadInvoice {

    public Scene getDownloadInvoiceScene(
            Runnable back
    ) {

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#F4F8FC;");
        root.setTop(createHeader("Download Invoice", "Dock Tech / Importer / Download Invoice"));
        root.setLeft(createSidebar(back));

        VBox content = new VBox(22);
        content.setPadding(new Insets(18));
        content.setStyle("-fx-background-color:#F4F8FC;");

        // Top header is handled in root BorderPane layout

        // =====================================================
        // TITLE
        // =====================================================

        Text title =
                new Text(
                        "Download Invoice"
                );

        title.setStyle(
                "-fx-font-size:30px;" +
                "-fx-font-weight:bold;" +
                "-fx-fill:#101A2B;"
        );

        Text subtitle =
                new Text(
                        "View and download invoices for your shipments."
                );

        subtitle.setStyle(
                "-fx-font-size:15px;" +
                "-fx-fill:#778394;"
        );

        // =====================================================
        // SEARCH
        // =====================================================

        HBox search =
                new HBox(10);

        TextField field =
                new TextField();

        field.setPromptText(
                "Search invoice..."
        );

        field.setPrefWidth(350);

        field.setPrefHeight(42);

        Button searchButton =
                new Button("Search");

        searchButton.setPrefWidth(90);

        searchButton.setPrefHeight(42);

        searchButton.setStyle(
                "-fx-background-color:#287FF0;" +
                "-fx-text-fill:white;" +
                "-fx-background-radius:8px;"
        );

        search.getChildren().addAll(
                field,
                searchButton
        );

        // =====================================================
        // FILTERS
        // =====================================================

        HBox filters =
                new HBox(10);

        Button all =
                filterButton(
                        "All Invoices",
                        true
                );

        Button paid =
                filterButton(
                        "Paid",
                        false
                );

        Button pending =
                filterButton(
                        "Pending",
                        false
                );

        filters.getChildren().addAll(
                all,
                paid,
                pending
        );

        // =====================================================
        // INVOICE TABLE
        // =====================================================

        VBox table =
                new VBox();

        table.setStyle(
                cardStyle()
        );

        HBox header =
                invoiceRow(
                        "INVOICE",
                        "SHIPMENT",
                        "DATE",
                        "AMOUNT",
                        "STATUS",
                        "ACTION",
                        true
                );

        HBox row1 =
                invoiceRow(
                        "INV-1024",
                        "SHP-1024",
                        "11 Aug 2026",
                        "₹ 48,500",
                        "Paid",
                        "⇩  Download Invoice",
                        false
                );

        HBox row2 =
                invoiceRow(
                        "INV-1023",
                        "SHP-1023",
                        "08 Aug 2026",
                        "₹ 32,800",
                        "Paid",
                        "⇩  Download Invoice",
                        false
                );

        HBox row3 =
                invoiceRow(
                        "INV-1022",
                        "SHP-1022",
                        "05 Aug 2026",
                        "₹ 27,400",
                        "Pending",
                        "⇩  Download Invoice",
                        false
                );

        table.getChildren().addAll(
                header,
                row1,
                row2,
                row3
        );

        // =====================================================
        // PAGINATION
        // =====================================================

        HBox pagination =
                new HBox(10);

        pagination.setAlignment(
                Pos.CENTER_RIGHT
        );

        Button previous =
                new Button("Previous");

        Button next =
                new Button("Next");

        previous.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#DCE3EA;" +
                "-fx-border-radius:7px;" +
                "-fx-background-radius:7px;"
        );

        next.setStyle(
                "-fx-background-color:#287FF0;" +
                "-fx-text-fill:white;" +
                "-fx-background-radius:7px;"
        );

        pagination.getChildren().addAll(
                previous,
                next
        );

        content.getChildren().addAll(
                title,
                subtitle,
                search,
                filters,
                table,
                pagination
        );

        root.setCenter(content);

        return new Scene(root);
    }

    // =========================================================
    // SIDEBAR
    // =========================================================

    private VBox createSidebar(Runnable back) {
        java.util.Map<String, Button> menuButtons = new java.util.LinkedHashMap<>();
        Button dashboard = new Button("🏠   Dashboard");
        Button track = new Button("📥   Track Shipment");
        Button invoice = new Button("📄   Download Invoice");
        Button delivery = new Button("📦   Confirm Delivery");

        menuButtons.put("Dashboard", dashboard);
        menuButtons.put("Track Shipment", track);
        menuButtons.put("Download Invoice", invoice);
        menuButtons.put("Confirm Delivery", delivery);

        dashboard.setOnAction(e -> back.run());

        track.setOnAction(e -> {
            TrackShipment page = new TrackShipment();
            com.projectui.view.login.LoginPage.showRoot(ImporterDashboard.importerStage, page.getTrackShipmentScene(back).getRoot());
        });

        delivery.setOnAction(e -> {
            ConfirmDelivery page = new ConfirmDelivery();
            com.projectui.view.login.LoginPage.showRoot(ImporterDashboard.importerStage, page.getConfirmDeliveryScene(back).getRoot());
        });

        return com.projectui.view.login.DashboardLayoutHelper.createSidebar(ImporterDashboard.importerStage, "Importer", "Download Invoice", menuButtons);
    }

    private HBox createHeader(String title, String breadcrumb) {
        return com.projectui.view.login.DashboardLayoutHelper.createHeader(ImporterDashboard.importerStage, title, breadcrumb, "Importer", () -> {
            Logout page = new Logout();
            page.showLogout();
        }, null);
    }

    // =========================================================
    // MENU BUTTON
    // =========================================================

    private Button menuButton(
            String text,
            boolean active
    ) {

        Button button =
                new Button(text);

        button.setPrefWidth(245);

        button.setPrefHeight(45);

        button.setStyle(
                active
                        ?
                        "-fx-background-color:#287FF0;" +
                        "-fx-text-fill:white;" +
                        "-fx-background-radius:9px;" +
                        "-fx-font-size:15px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-alignment:CENTER_LEFT;" +
                        "-fx-padding:0 15;"
                        :
                        "-fx-background-color:transparent;" +
                        "-fx-text-fill:#C1CBD6;" +
                        "-fx-font-size:15px;" +
                        "-fx-alignment:CENTER_LEFT;" +
                        "-fx-padding:0 15;"
        );

        return button;
    }

    // =========================================================
    // FILTER BUTTON
    // =========================================================

    private Button filterButton(
            String text,
            boolean active
    ) {

        Button button =
                new Button(text);

        button.setPrefHeight(38);

        button.setStyle(
                active
                        ?
                        "-fx-background-color:#287FF0;" +
                        "-fx-text-fill:white;" +
                        "-fx-background-radius:8px;" +
                        "-fx-padding:0 18;"
                        :
                        "-fx-background-color:white;" +
                        "-fx-text-fill:#667486;" +
                        "-fx-border-color:#DCE3EA;" +
                        "-fx-border-radius:8px;" +
                        "-fx-background-radius:8px;" +
                        "-fx-padding:0 18;"
        );

        return button;
    }

    // =========================================================
    // INVOICE ROW
    // =========================================================

    private HBox invoiceRow(
            String invoice,
            String shipment,
            String date,
            String amount,
            String status,
            String action,
            boolean header
    ) {

        HBox row =
                new HBox();

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        row.setPadding(
                new Insets(15)
        );

        if (header) {

            row.setStyle(
                    "-fx-background-color:#F8FAFC;"
            );

        } else {

            row.setStyle(
                    "-fx-border-color:#EEF1F4;" +
                    "-fx-border-width:0 0 1 0;"
            );
        }

        row.getChildren().addAll(

                cell(
                        invoice,
                        120,
                        header
                ),

                cell(
                        shipment,
                        120,
                        header
                ),

                cell(
                        date,
                        120,
                        header
                ),

                cell(
                        amount,
                        110,
                        header
                ),

                statusCell(
                        status,
                        100,
                        header
                ),

                header
                        ? cell(
                                action,
                                210,
                                true
                        )
                        : actionButtons()
        );

        return row;
    }

    private Label cell(
            String text,
            double width,
            boolean header
    ) {

        Label label =
                new Label(text);

        label.setPrefWidth(width);

        label.setStyle(
                header
                        ?
                        "-fx-text-fill:#778394;" +
                        "-fx-font-size:10px;" +
                        "-fx-font-weight:bold;"
                        :
                        "-fx-text-fill:#253447;" +
                        "-fx-font-size:12px;"
        );

        return label;
    }

    private Label statusCell(
            String text,
            double width,
            boolean header
    ) {

        Label label =
                new Label(text);

        label.setPrefWidth(width);

        if (header) {

            label.setStyle(
                    "-fx-text-fill:#778394;" +
                    "-fx-font-size:10px;" +
                    "-fx-font-weight:bold;"
            );

        } else if (text.equals("Paid")) {

            label.setStyle(
                    "-fx-text-fill:#168548;" +
                    "-fx-font-weight:bold;"
            );

        } else {

            label.setStyle(
                    "-fx-text-fill:#C56A16;" +
                    "-fx-font-weight:bold;"
            );
        }

        return label;
    }

    // =========================================================
    // ACTION BUTTONS
    // =========================================================

    private HBox actionButtons() {

        HBox actions =
                new HBox(12);

        actions.setAlignment(
                Pos.CENTER_LEFT
        );

        Button view =
                new Button("◉  View");

        view.setPrefWidth(80);

        view.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-text-fill:#0879E8;" +
                "-fx-font-size:16px;" +
                "-fx-padding:0;"
        );

        Button download =
                new Button("⇩  Download Invoice");

        download.setPrefWidth(210);

        download.setStyle(
                "-fx-background-color:#0879E8;" +
                "-fx-text-fill:white;" +
                "-fx-background-radius:7px;"
        );

        actions.getChildren().addAll(
                view,
                download
        );

        return actions;
    }

    // =========================================================
    // HEADER
    // =========================================================

    private String cardStyle() {

        return
                "-fx-background-color:white;" +
                "-fx-background-radius:18px;" +
                "-fx-border-color:#E2E7ED;" +
                "-fx-border-radius:18px;";
    }
}
