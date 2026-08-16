package com.projectui.view.CustomOfficer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DashBoard extends Application {

    private BorderPane root;
    private Button activeButton;

    private final String BLUE="#0f5fff";
    private final String TEXT="#111827";
    private final String GRAY="#6b7280";
    private final String BG="#f3f8fc";

    private Stage stage;

    private String currentPage = "Dashboard";

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        root=new BorderPane();
        root.setStyle("-fx-background-color:#f3f8ff;");

        showPage("Dashboard", createDashboardPage());

        stage.setTitle("Dock Tech - Customs Officer");
        com.projectui.view.login.LoginPage.showRoot(stage, root);
        stage.show();
    }

    private void showPage(String pageName, javafx.scene.Parent pageContent) {
        this.currentPage = pageName;
        root.setCenter(pageContent);
        root.setLeft(createSidebar());
        root.setTop(createHeader());
    }

    // ================= SIDEBAR =================

    private VBox createSidebar() {
        java.util.Map<String, Button> menuButtons = new java.util.LinkedHashMap<>();
        Button dashboard = new Button("🏠   Dashboard");
        Button document = new Button("📄   Document Verification");
        Button cargo = new Button("🔍   Cargo Inspection");
        Button approve = new Button("✓   Approve / Reject Cargo");
        Button reports = new Button("📊   Reports & Analytics");
        Button settings = new Button("⚙   Settings");

        menuButtons.put("Dashboard", dashboard);
        menuButtons.put("Document Verification", document);
        menuButtons.put("Cargo Inspection", cargo);
        menuButtons.put("Approve / Reject Cargo", approve);
        menuButtons.put("Reports & Analytics", reports);
        menuButtons.put("Settings", settings);

        dashboard.setOnAction(e -> showPage("Dashboard", createDashboardPage()));
        document.setOnAction(e -> showPage("Document Verification", new DocumentVerification().getContent()));
        cargo.setOnAction(e -> showPage("Cargo Inspection", new CargoInspection().getContent()));
        approve.setOnAction(e -> showPage("Approve / Reject Cargo", new CargoClearance().getRoot()));
        reports.setOnAction(e -> showPage("Reports & Analytics", new ReportsAnalytics().getContent()));
        settings.setOnAction(e -> showPage("Settings", new SettingsPage().getContent()));

        return com.projectui.view.login.DashboardLayoutHelper.createSidebar(stage, "Custom Officer", currentPage, menuButtons);
    }

    // ================= HEADER =================

    private HBox createHeader() {
        String breadcrumb = "Dock Tech / Custom Officer / " + currentPage;
        return com.projectui.view.login.DashboardLayoutHelper.createHeader(stage, currentPage, breadcrumb, "Custom Officer", () -> {
            new com.projectui.view.login.LoginPage().show(stage);
        }, null);
    }

    // ================= DASHBOARD =================

    private VBox createDashboardPage() {

        VBox page=new VBox(15);
        page.setPadding(new Insets(20));
        page.setStyle("-fx-background-color:"+BG+";");

       
        VBox heading=new VBox(4,
            styled("Customs Officer Dashboard",25,true,TEXT),
            styled(
                "Monitor cargo inspections, document verification and customs clearance.",
                12,false,GRAY
            )
        );

        Region gap=new Region();
        HBox.setHgrow(gap,Priority.ALWAYS);

        HBox head=new HBox(heading,gap);
        head.setAlignment(Pos.CENTER_LEFT);

        HBox cards=new HBox(12);

        String[][] data={
            {"▣","30","Pending Documents","Documents waiting for verification","+4%","#eaf3ff"},
            {"▣","156","Verified Documents","Successfully verified","+12%","#eafaf3"},
            {"◇","18","Pending Cargo","Cargo awaiting inspection","+3%","#fff8e8"},
            {"✓","142","Approved Cargo","Cleared for movement","+9%","#eafaf3"},
            {"⊗","07","Rejected Cargo","Rejected / held cargo","-2%","#fff0f0"}
        };

        for(String[] d:data){
            VBox c=createCard(d);
            cards.getChildren().add(c);
            HBox.setHgrow(c,Priority.ALWAYS);
        }

        HBox bottom=new HBox(
            15,createRecentActivity(),createClearanceOverview()
        );

        HBox.setHgrow(bottom.getChildren().get(0),Priority.ALWAYS);
        HBox.setHgrow(bottom.getChildren().get(1),Priority.ALWAYS);

        page.getChildren().addAll(
            head,cards,createGraph(),bottom
        );

        return page;
    }

    // ================= CARD =================

    private VBox createCard(String[] d) {

        VBox card=new VBox(6);
        card.setPadding(new Insets(14));
        card.setMinHeight(135);
        card.setMaxHeight(145);
        card.setStyle(box());

        Label icon=new Label(d[0]);
        icon.setStyle(
            "-fx-font-size:17;-fx-text-fill:#287ff0;"+
            "-fx-background-color:"+d[5]+";-fx-padding:7;"+
            "-fx-background-radius:8;"
        );

        Label percent=styled(d[4],11,false,"#18a879");

        Region gap=new Region();
        HBox.setHgrow(gap,Priority.ALWAYS);

        card.getChildren().addAll(
            new HBox(icon,gap,percent),
            styled(d[1],24,true,TEXT),
            styled(d[2],12,true,"#374151"),
            styled(d[3],9,false,"#9ca3af")
        );

        return card;
    }

    // ================= GRAPH =================

    private VBox createGraph() {

        VBox box=new VBox(5);
        box.setPadding(new Insets(15));
        box.setStyle(box());

        CategoryAxis x=new CategoryAxis();
        NumberAxis y=new NumberAxis();

        BarChart<String,Number> chart=new BarChart<>(x,y);
        chart.setPrefHeight(240);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCategoryGap(18);

        String[] days={"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};

        int[][] values={
            {120,150,100,175,160,80,60},
            {90,115,85,135,125,65,50},
            {70,95,65,115,100,50,40}
        };

        for(int[] v:values){
            XYChart.Series<String,Number> s=new XYChart.Series<>();
            for(int i=0;i<days.length;i++)
                s.getData().add(new XYChart.Data<>(days[i],v[i]));
            chart.getData().add(s);
        }

        box.getChildren().addAll(
            styled("Customs Activity Overview",17,true,TEXT),
            styled("Documents & cargo processing trends",10,false,GRAY),
            chart
        );

        return box;
    }

    // ================= RECENT =================

    private VBox createRecentActivity() {

        VBox box=new VBox(9);
        box.setPadding(new Insets(15));
        box.setPrefHeight(180);
        box.setStyle(box());

        box.getChildren().addAll(
            styled("Recent Verification Activity",15,true,TEXT),
            styled("Latest document submissions and review status",9,false,GRAY),
            activity("DOC-1024","CGO-1287","Ocean Shipping Ltd.","Pending"),
            activity("DOC-1023","CGO-1290","Global Traders","Verified")
        );

        return box;
    }

    private HBox activity(String a,String b,String c,String d) {

        HBox row=new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-border-color:#eeeeee;-fx-border-radius:7;");

        Label x=styled(a,10,true,TEXT);
        Label y=styled(b,9,false,GRAY);
        Label z=styled(c,9,false,GRAY);
        Label s=styled(d,9,false,"#18a879");

        HBox.setHgrow(z,Priority.ALWAYS);
        row.getChildren().addAll(x,y,z,s);

        return row;
    }

    // ================= CLEARANCE =================

    private VBox createClearanceOverview() {

        VBox box=new VBox(10);
        box.setPadding(new Insets(15));
        box.setPrefHeight(180);
        box.setStyle(box());

        box.getChildren().addAll(
            styled("Cargo Clearance Overview",15,true,TEXT),
            styled("Current customs status distribution",9,false,GRAY),
            status("●  Pending Inspection","18","#fff7dc"),
            status("●  Under Review","12","#eaf3ff"),
            status("●  Approved","142","#e4f8ee")
        );

        return box;
    }

    private HBox status(String text,String number,String bg) {

        HBox row=new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle(
            "-fx-background-color:"+bg+";-fx-background-radius:8;"
        );

        Label a=styled(text,10,true,TEXT);
        Label b=styled(number,10,true,TEXT);

        HBox.setHgrow(a,Priority.ALWAYS);
        row.getChildren().addAll(a,b);

        return row;
    }

    // ================= SIMPLE PAGE =================

    private VBox simplePage(String title,String description) {

        VBox page=new VBox(12);
        page.setPadding(new Insets(25));
        page.setStyle("-fx-background-color:"+BG+";");

        VBox card=new VBox(10);
        card.setPadding(new Insets(25));
        card.setStyle(box());

        card.getChildren().add(
            styled("Page content will appear here.",14,false,"#374151")
        );

        page.getChildren().addAll(
            styled(title,25,true,TEXT),
            styled(description,12,false,GRAY),
            card
        );

        return page;
    }

    // ================= HELPERS =================

    private Label styled(
        String text,double size,boolean bold,String color
    ) {

        Label l=new Label(text);

        l.setStyle(
            "-fx-font-size:"+size+";"+
            "-fx-font-weight:"+(bold?"bold":"normal")+";"+
            "-fx-text-fill:"+color+";"
        );

        return l;
    }

    private String box() {

        return "-fx-background-color:white;"+
               "-fx-background-radius:15;"+
               "-fx-border-color:#e5e7eb;"+
               "-fx-border-radius:15;";
    }

    public static void main(String[] args) {
        launch(args);
    }
}