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

    @Override
    public void start(Stage stage) {
        root=new BorderPane();
        root.setStyle("-fx-background-color:#f3f8ff;");
        root.setLeft(createSidebar());
        root.setTop(createHeader());
        root.setCenter(createDashboardPage());

        stage.setTitle("Dock Tech - Customs Officer");
        stage.setScene(new Scene(root,1200,700));
        stage.show();
    }

    // ================= SIDEBAR =================

    private VBox createSidebar() {
        VBox side=new VBox(8);
        side.setPrefWidth(220);
        side.setPadding(new Insets(22,14,20,14));
        side.setStyle("-fx-background-color:#050911;");

        Label logo=new Label("⚓  DOCK TECH");
        logo.setStyle("-fx-text-fill:white;-fx-font-size:18;-fx-font-weight:bold;");

        Label sub=new Label("SMART PORT\nOPERATIONS");
        sub.setStyle("-fx-text-fill:#9aaac0;-fx-font-size:9;-fx-font-weight:bold;");

        VBox logoBox=new VBox(3,logo,sub);
        logoBox.setPadding(new Insets(0,0,22,10));

        Button dashboard=nav("⌂","Dashboard");
        Button document=nav("▣","Document Verification");
        Button cargo=nav("◇","Cargo Inspection");
        Button approve=nav("✓","Approve / Reject Cargo");
        Button reports=nav("▣","Reports & Analytics");
        Button settings=nav("⚙","Settings");

        dashboard.setOnAction(e->{active(dashboard);root.setCenter(createDashboardPage());});
        document.setOnAction(e->{active(document);root.setCenter(new DocumentVerification().getContent());});
        cargo.setOnAction(e->{active(cargo);root.setCenter(new CargoInspection().getContent());});
        approve.setOnAction(e->{active(approve);root.setCenter(new CargoClearance().getRoot());});
        reports.setOnAction(e->{active(reports);root.setCenter(simplePage("Reports & Analytics","View customs activity and analytics reports."));});
        settings.setOnAction(e->{active(settings);root.setCenter(simplePage("Settings","Manage application settings."));});

        Region gap=new Region();
        VBox.setVgrow(gap,Priority.ALWAYS);

        side.getChildren().addAll(
            logoBox,dashboard,document,cargo,approve,reports,settings,gap
        );

        activeButton=dashboard;
        active(dashboard);
        return side;
    }

    private Button nav(String icon,String text) {
        Button b=new Button(icon+"   "+text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(42);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(0,8,0,12));
        b.setStyle(navStyle(false));

        b.setOnMouseEntered(e->{if(b!=activeButton)b.setStyle(navStyle(true));});
        b.setOnMouseExited(e->{if(b!=activeButton)b.setStyle(navStyle(false));});
        return b;
    }

    private void active(Button b) {
        if(activeButton!=null) activeButton.setStyle(navStyle(false));
        activeButton=b;
        b.setStyle(navStyle(true,true));
    }

    private String navStyle(boolean hover){return navStyle(hover,false);}

    private String navStyle(boolean active,boolean selected) {
        return "-fx-background-color:"+
            (selected?BLUE:(active?"#10264a":"transparent"))+
            ";-fx-text-fill:"+(active||selected?"white":"#aebbd0")+
            ";-fx-font-size:11;-fx-font-weight:bold;"+
            "-fx-background-radius:8;-fx-cursor:hand;";
    }

    // ================= HEADER =================

    private HBox createHeader() {

        HBox h=new HBox(12);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(12,22,12,25));
        h.setPrefHeight(65);
        h.setStyle("-fx-background-color:white;-fx-border-color:#e5ebf3;-fx-border-width:0 0 1 0;");

        VBox info=new VBox(3,
            styled("Dashboard",17,true,TEXT),
            styled("Custom Officer / Admin / Dashboard",10,false,"#8a93a1")
        );

        Region gap=new Region();
        HBox.setHgrow(gap,Priority.ALWAYS);

        // SEARCH
        TextField search=new TextField();
        search.setPromptText("⌕  Search...");
        search.setPrefSize(175,34);

        Button searchBtn=new Button("🔍");
        searchBtn.setPrefSize(40,34);
        searchBtn.setStyle(
            "-fx-background-color:#0f5fff;-fx-text-fill:white;"+
            "-fx-background-radius:6;-fx-cursor:hand;"
        );

        searchBtn.setOnAction(e->{
            if(!search.getText().trim().isEmpty()){
                Alert a=new Alert(Alert.AlertType.INFORMATION);
                a.setHeaderText("Search");
                a.setContentText("Searching for: "+search.getText().trim());
                a.showAndWait();
            }
        });

        search.setOnAction(e->searchBtn.fire());

        HBox searchBox=new HBox(5,search,searchBtn);

        // STATUS
        Label status=new Label("● System Operational");
        status.setStyle(
            "-fx-text-fill:#168548;-fx-background-color:#e7f7ee;"+
            "-fx-background-radius:14;-fx-padding:6 10;"+
            "-fx-font-size:10;-fx-font-weight:bold;"
        );

        // DATE & TIME
        VBox dateTime=new VBox(1);
        dateTime.setAlignment(Pos.CENTER_RIGHT);

        Label date=new Label();
        date.setStyle("-fx-text-fill:#4b5563;-fx-font-size:10;-fx-font-weight:bold;");

        Label time=new Label();
        time.setStyle("-fx-text-fill:#0f5fff;-fx-font-size:12;-fx-font-weight:bold;");

        dateTime.getChildren().addAll(date,time);

        Timeline clock=new Timeline(
            new KeyFrame(Duration.ZERO,e->{
                LocalDateTime now=LocalDateTime.now();
                date.setText(now.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
                time.setText(now.format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
            }),
            new KeyFrame(Duration.seconds(1))
        );

        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        Label bell=new Label("🔔");
        bell.setStyle("-fx-font-size:17;");

        Label avatar=new Label("A");
        avatar.setMinSize(32,32);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle(
            "-fx-background-color:#dbeafe;-fx-background-radius:50;"+
            "-fx-text-fill:#0f5fff;-fx-font-weight:bold;"
        );

        VBox user=new VBox(1,
            styled("Custom Officer",11,true,TEXT),
            styled("Administrator",9,false,"#8a93a1")
        );

        h.getChildren().addAll(
            info,gap,searchBox,status,dateTime,bell,avatar,user
        );

        return h;
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