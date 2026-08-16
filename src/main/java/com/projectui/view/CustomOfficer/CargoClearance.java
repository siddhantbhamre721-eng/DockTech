package com.projectui.view.CustomOfficer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CargoClearance {
    private final BorderPane root = new BorderPane();
    private final Scene scene;

    Label pending = new Label("18");
    Label approved = new Label("142");
    Label rejected = new Label("07");

    public CargoClearance() {

        root.setStyle("-fx-background-color:#f4f7fb;");

        VBox main = new VBox(10);
        main.setPadding(new Insets(5,22,20,22));

        Label title = label("Cargo Clearance",27,true);
        Label sub = label(
                "Review and authorize cargo clearance.",
                13,false
        );
        sub.setTextFill(Color.web("#7b8794"));

        VBox titleBox = new VBox(2,title,sub);

       

        
        Region gap = new Region();
        HBox.setHgrow(gap,Priority.ALWAYS);

        HBox header = new HBox(15,titleBox,gap);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox cards = new HBox(12,
                card("Pending Clearance",pending,"◷","#f59e0b"),
                card("Approved",approved,"✓","#16a875"),
                card("Rejected",rejected,"×","#e53935"),
                card("Today's Clearance",
                        new Label("24"),"▣","#287cf5")
        );

        for(Node n:cards.getChildren())
            HBox.setHgrow(n,Priority.ALWAYS);

        TextField cargo = new TextField();
        cargo.setPromptText("⌕  Search Cargo...");
        cargo.setPrefWidth(250);

        HBox filter = new HBox(8,
                cargo,
                combo("Status","All Status","Approved",
                        "Pending","Rejected"),
                combo("Cargo Type","All Types","Electronics",
                        "Machinery","Food"),
                combo("Date","All Dates","Today","Yesterday"),
                combo("Exporter","All Exporters",
                        "Ocean Shipping Ltd.","Global Traders")
        );

        Button apply = new Button("☷  Apply Filter");
        apply.setPrefSize(150,42);
        apply.setStyle(btn("#287cf5"));

        filter.getChildren().add(apply);
        filter.setPadding(new Insets(8));
        filter.setStyle(box());

        VBox table = new VBox();
        table.setStyle(box());

        VBox th = new VBox(2,
                label("Clearance Table",19,true),
                label(
                        "Review cargo decisions and release status",
                        12,false
                )
        );

        th.setPadding(new Insets(15));

        String[] h={
                "CARGO ID","DOCUMENT","SHIP","EXPORTER",
                "CARGO TYPE","INSPECTION","STATUS","DATE","ACTION"
        };

        HBox headings=new HBox(8);
        headings.setPadding(new Insets(12));

        for(String s:h)
            headings.getChildren().add(col(s,95));

        table.getChildren().addAll(
                th,new Separator(),headings,

                row("CGO-1287","DOC-1024","Oceanic Star",
                        "Ocean Shipping Ltd.","Electronics",
                        "Verified","Approved",true),

                row("CGO-1290","DOC-1025","Sea Voyager",
                        "Global Traders","Machinery",
                        "Under Review","Pending",false)
        );

        HBox content = new HBox(15,table,details());
        HBox.setHgrow(table,Priority.ALWAYS);

        main.getChildren().addAll(
                header,cards,filter,content
        );

        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );
        scroll.setStyle(
                "-fx-background:#f4f7fb;" +
                "-fx-background-color:#f4f7fb;"
        );

        root.setCenter(scroll);
        scene = new Scene(root,1500,850);
    }

    private VBox card(
            String name,
            Label value,
            String icon,
            String color
    ) {

        Label t=label(name,13,false);
        t.setTextFill(Color.web("#697586"));

        Label i=new Label(icon);
        i.setTextFill(Color.web(color));
        i.setFont(Font.font(20));

        Region r=new Region();
        HBox.setHgrow(r,Priority.ALWAYS);

        value.setFont(
                Font.font("Arial",FontWeight.BOLD,28)
        );

        VBox b=new VBox(
                6,
                new HBox(t,r,i),
                value
        );

        b.setPadding(new Insets(14));
        b.setStyle(box());

        return b;
    }

    private HBox row(
            String id,
            String doc,
            String ship,
            String exporter,
            String type,
            String inspection,
            String status,
            boolean ok
    ) {

        Label st=new Label(status);

        st.setPadding(
                new Insets(5,10,5,10)
        );

        st.setStyle(ok ?
                "-fx-background-color:#e7faf3;" +
                "-fx-text-fill:#15956d;" +
                "-fx-background-radius:15;" +
                "-fx-font-weight:bold;" :

                "-fx-background-color:#fff3df;" +
                "-fx-text-fill:#ed8613;" +
                "-fx-background-radius:15;" +
                "-fx-font-weight:bold;"
        );

        Button action=new Button(
                ok ? "View" : "Review"
        );

        action.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-text-fill:#287cf5;"
        );

        action.setOnAction(e ->
                alert(
                        ok ? "Cargo Approved" : "Cargo Review",
                        id+" selected."
                )
        );

        HBox r=new HBox(8);

        r.setPadding(
                new Insets(12,15,12,15)
        );

        r.getChildren().addAll(
                cell(id,90),
                cell(doc,95),
                cell(ship,95),
                cell(exporter,115),
                cell(type,100),
                cell(inspection,95),
                st,
                cell("11 Aug 2026",80),
                action
        );

        return r;
    }

    private VBox details() {

        GridPane info=new GridPane();

        info.setHgap(25);
        info.setVgap(10);

        add(info,"Cargo ID","CGO-1287",0,0);
        add(info,"Ship","Oceanic Star",1,0);
        add(info,"Exporter",
                "Ocean Shipping Ltd.",0,1);
        add(info,"Cargo Type",
                "Electronics",1,1);
        add(info,"Weight","850 Tons",0,2);
        add(info,"Containers","120",1,2);

        Label decision=new Label("Approved");

        decision.setPadding(
                new Insets(6,12,6,12)
        );

        decision.setStyle(
                "-fx-background-color:#e7faf3;" +
                "-fx-text-fill:#15956d;" +
                "-fx-background-radius:15;" +
                "-fx-font-weight:bold;"
        );

        Button approve =
                new Button("✓  Approve Clearance");

        Button hold =
                new Button("◷  Hold Cargo");

        Button reject =
                new Button("✕  Reject Cargo");

        approve.setStyle(btn("#16a875"));
        hold.setStyle(btn("#f59e0b"));
        reject.setStyle(btn("#e53935"));

        for(Button b:new Button[]{
                approve,hold,reject
        }) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setPrefHeight(40);
        }

        approve.setOnAction(e -> {
            approved.setText("143");
            pending.setText("17");
            decision.setText("✓ Approved");
        });

        reject.setOnAction(e -> {
            rejected.setText("08");
            pending.setText("17");
            decision.setText("✕ Rejected");
        });

        hold.setOnAction(e ->
                alert(
                        "Cargo On Hold",
                        "CGO-1287 has been placed on hold."
                )
        );

        VBox docs=new VBox(
                5,
                doc("Bill of Lading"),
                doc("Commercial Invoice"),
                doc("Packing List"),
                doc("Customs Declaration")
        );

        VBox d=new VBox(
                10,
                label("Cargo Clearance",20,true),
                label(
                        "Detailed review for CGO-1287",
                        12,false
                ),
                info,
                new Separator(),
                label("Documents",15,true),
                docs,
                new Separator(),
                label(
                        "✓   Inspection Completed",
                        13,true
                ),
                new Separator(),
                label("Customs Decision",13,false),
                decision,
                approve,
                hold,
                reject
        );

        d.getChildren().get(1).setStyle(
                "-fx-text-fill:#7b8794;"
        );

        d.getChildren().get(8).setStyle(
                "-fx-text-fill:#15956d;"
        );

        d.setPadding(new Insets(18));
        d.setPrefWidth(370);
        d.setStyle(box());

        return d;
    }

    private Label label(
            String s,
            double size,
            boolean bold
    ) {

        Label l=new Label(s);

        l.setFont(
                Font.font(
                        "Arial",
                        bold ?
                                FontWeight.BOLD :
                                FontWeight.NORMAL,
                        size
                )
        );

        return l;
    }

    private Label cell(
            String s,
            double w
    ) {

        Label l=new Label(s);
        l.setPrefWidth(w);
        l.setWrapText(true);

        return l;
    }

    private Label col(
            String s,
            double w
    ) {

        Label l=cell(s,w);

        l.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        11
                )
        );

        l.setTextFill(
                Color.web("#687586")
        );

        return l;
    }

    private ComboBox<String> combo(
            String name,
            String... items
    ) {

        ComboBox<String> c=new ComboBox<>();

        c.getItems().addAll(items);
        c.setValue(name);
        c.setPrefSize(145,42);

        return c;
    }

    private void add(
            GridPane g,
            String a,
            String b,
            int c,
            int r
    ) {

        Label x=new Label(a);

        x.setTextFill(
                Color.web("#7b8794")
        );

        g.add(
                new VBox(
                        2,
                        x,
                        label(b,13,true)
                ),
                c,
                r
        );
    }

    private Label doc(String s) {

        Label l=new Label("✓   "+s);

        l.setTextFill(
                Color.web("#16a875")
        );

        return l;
    }

    private String box() {

        return
                "-fx-background-color:white;" +
                "-fx-background-radius:14;" +
                "-fx-border-color:#e1e7ee;" +
                "-fx-border-radius:14;";
    }

    private String btn(String c) {

        return
                "-fx-background-color:"+c+";" +
                "-fx-text-fill:white;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:8;";
    }

    private void alert(
            String t,
            String m
    ) {

        Alert a=new Alert(
                Alert.AlertType.INFORMATION
        );

        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }

    public BorderPane getRoot() {
        return root;
    }

    public Scene getScene() {
        return scene;
    }
    
}
