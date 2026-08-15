package com.projectui.CustomOfficer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DocumentVerification {
    private final BorderPane root = new BorderPane();
    private VBox content;

    public DocumentVerification() {

        // TOP BAR
        TextField search = new TextField();
        search.setPromptText("⌕  Search anything...");
        search.setPrefSize(195,40);
        search.setStyle("-fx-background-color:white;-fx-border-color:#E3E8EF;-fx-border-radius:10;-fx-background-radius:10;");

        Button notification = new Button("♧");
        notification.setPrefSize(42,40);
        notification.setStyle("-fx-background-color:white;-fx-border-color:#E3E8EF;-fx-border-radius:10;-fx-background-radius:10;");

        Label avatar = new Label("CO");
        avatar.setPrefSize(30,30);
        avatar.setAlignment(Pos.CENTER);
        avatar.setTextFill(Color.WHITE);
        avatar.setFont(Font.font("Arial",FontWeight.BOLD,9));
        avatar.setStyle("-fx-background-color:#1683F7;-fx-background-radius:50%;");

        Label name = new Label("Customs\nOfficer");
        name.setFont(Font.font("Arial",FontWeight.BOLD,10));

        HBox user = new HBox(8,avatar,name);
        user.setAlignment(Pos.CENTER_LEFT);
        user.setPrefSize(135,40);
        user.setPadding(new Insets(5,10,5,6));
        user.setStyle("-fx-background-color:white;-fx-border-color:#E3E8EF;-fx-border-radius:10;-fx-background-radius:10;");

        Region sp = new Region();
        HBox.setHgrow(sp,Priority.ALWAYS);

        HBox top = new HBox(15,sp,search,notification,user);
        top.setAlignment(Pos.CENTER_RIGHT);
        top.setPadding(new Insets(20,25,15,25));
        top.setPrefHeight(75);
        top.setStyle("-fx-background-color:white;");

        // PAGE HEADING
        Label title = label("Document Verification",24,true,"#111827");
        Label sub = label("Review and verify cargo and shipment documentation.",11,false,"#8A94A3");

        VBox heading = new VBox(4,title,sub);
        heading.setPadding(new Insets(5,25,8,25));

        // CARDS
        HBox cards = new HBox(15);
        cards.setPadding(new Insets(10,25,15,25));

        cards.getChildren().addAll(
            statCard("Pending Review","28","◷","#FFF0C7"),
            statCard("Verified","156","✓","#D8F8E8"),
            statCard("Rejected","07","×","#FFE0E6"),
            statCard("Today's Submissions","14","▤","#DDEEFF")
        );

       

        ComboBox<String> type = combo("Document Type",
                "Bill of Lading","Customs Declaration","Commercial Invoice",115);

        ComboBox<String> status = combo("Status",
                "Pending","Verified","Rejected",95);

        ComboBox<String> date = combo("Date",
                "Today","This Week","This Month",90);

        ComboBox<String> exporter = combo("Exporter",
                "Ocean Shipping Ltd.","Global Traders","Pacific Cargo Co.",110);

        Button filter = new Button("⚙  Apply Filter");
        filter.setPrefSize(105,40);
        filter.setTextFill(Color.WHITE);
        filter.setFont(Font.font("Arial",FontWeight.BOLD,10));
        filter.setStyle("-fx-background-color:#1683F7;-fx-background-radius:10;");

        HBox filterBox = new HBox(8,type,status,date,exporter,filter);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(10));
        filterBox.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#E4EAF0;-fx-border-radius:12;");

        HBox filterWrap = new HBox(filterBox);
        filterWrap.setPadding(new Insets(0,25,15,25));

        // TABLE
        VBox table = new VBox();
        table.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#E4EAF0;-fx-border-radius:12;");

        Label tt = label("Document Verification",16,true,"#111827");
        Label ts = label("Latest cargo and shipment documentation",11,false,"#8A94A3");

        VBox th = new VBox(3,tt,ts);
        Label view = label("View All",11,true,"#1683F7");

        Region hsp = new Region();
        HBox.setHgrow(hsp,Priority.ALWAYS);

        HBox head = new HBox(th,hsp,view);
        head.setPadding(new Insets(18));

        HBox columns = new HBox();
        columns.setPadding(new Insets(12,15,12,15));
        columns.setStyle("-fx-border-color:#E7EDF2;-fx-border-width:1 0 1 0;");

        String[] heads = {"Document ID","Cargo ID","Ship","Exporter","Document Type","Status","Action"};
        double[] widths = {85,75,90,110,120,70,70};

        for(int i=0;i<heads.length;i++)
            columns.getChildren().add(column(heads[i],widths[i]));

        VBox rows = new VBox();

        String[][] data = {
            {"DOC-1024","CGO-1287","Oceanic Star","Ocean Shipping Ltd.","Bill of Lading","Pending"},
            {"DOC-1023","CGO-1290","Sea Voyager","Global Traders","Customs Declaration","Verified"},
            {"DOC-1022","CGO-1288","Pacific Dawn","Pacific Cargo Co.","Commercial Invoice","Verified"},
            {"DOC-1021","CGO-1301","Coral Queen","Ocean Shipping Ltd.","Packing List","Pending"},
            {"DOC-1020","CGO-1295","Blue Marlin","Global Traders","Certificate of Origin","Verified"},
            {"DOC-1019","CGO-1299","Silver Wave","Pacific Cargo Co.","Import/Export License","Rejected"}
        };

        for(String[] r:data) rows.getChildren().add(documentRow(r));

        table.getChildren().addAll(head,columns,rows);
        HBox.setHgrow(table,Priority.ALWAYS);

        // VERIFICATION
        VBox verify = new VBox(12);
        verify.setPrefWidth(275);
        verify.setPadding(new Insets(18));
        verify.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#E4EAF0;-fx-border-radius:12;");

        verify.getChildren().addAll(
            label("Document Verification",16,true,"#111827"),
            label("Reviewing DOC-1024",11,false,"#8A94A3"),
            detail("Document ID","DOC-1024"),
            detail("Cargo ID","CGO-1287"),
            detail("Ship","Oceanic Star"),
            detail("Exporter","Ocean Shipping Ltd."),
            detail("Document Type","Bill of Lading"),
            detail("Submitted","11 Aug 2026")
        );

        VBox pdf = new VBox(8,new Label("▤"),new Label("Bill of Lading.pdf"));
        pdf.setAlignment(Pos.CENTER);
        pdf.setPrefHeight(140);
        pdf.setStyle("-fx-background-color:#263646;-fx-background-radius:12;");
        ((Label)pdf.getChildren().get(0)).setTextFill(Color.WHITE);
        ((Label)pdf.getChildren().get(0)).setFont(Font.font(28));
        ((Label)pdf.getChildren().get(1)).setTextFill(Color.WHITE);

        VBox checks = new VBox(8);
        checks.getChildren().add(label("Verification Checklist",13,true,"#111827"));

        String[] checkNames = {
            "Document Number Valid",
            "Exporter Information Valid",
            "Cargo Information Matches",
            "Quantity Verified",
            "Additional Inspection Required"
        };

        for(int i=0;i<checkNames.length;i++){
            CheckBox c = new CheckBox(checkNames[i]);
            c.setSelected(i<4);
            checks.getChildren().add(c);
        }

        Button verifyBtn = actionButton("✓  Verify Document","#10B981");
        Button rejectBtn = actionButton("×  Reject Document","#EF233C");

        verify.getChildren().addAll(
            pdf,
            checks,
            new HBox(10,verifyBtn,rejectBtn)
        );

        // MAIN
        HBox main = new HBox(15,table,verify);
        main.setPadding(new Insets(0,25,25,25));
        HBox.setHgrow(table,Priority.ALWAYS);

        content = new VBox(heading,cards,filterWrap,main);
        content.setStyle("-fx-background-color:#F3FAFF;");
        VBox.setVgrow(main,Priority.ALWAYS);

        VBox center = new VBox(top,content);
        VBox.setVgrow(content,Priority.ALWAYS);

        root.setCenter(center);
        root.setStyle("-fx-background-color:#F3FAFF;");
    }

    // ================= HELPERS =================

    private Label label(String text,double size,boolean bold,String color){
        Label l = new Label(text);
        l.setFont(Font.font("Arial",bold?FontWeight.BOLD:FontWeight.NORMAL,size));
        l.setTextFill(Color.web(color));
        return l;
    }

    private VBox statCard(String title,String value,String icon,String bg){
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setPrefHeight(105);
        card.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#E4EAF0;-fx-border-radius:12;");
        HBox.setHgrow(card,Priority.ALWAYS);

        Label t = label(title,11,false,"#8A94A3");
        Label n = label(value,25,true,"#111827");
        Label i = label(icon,16,true,"#1683F7");
        i.setPrefSize(35,35);
        i.setAlignment(Pos.CENTER);
        i.setStyle("-fx-background-color:"+bg+";-fx-background-radius:9;");

        Region s = new Region();
        HBox.setHgrow(s,Priority.ALWAYS);

        card.getChildren().addAll(new HBox(t,s,i),n);
        return card;
    }

    private ComboBox<String> combo(String value,String a,String b,String c,double width){
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(value,a,b,c);
        box.setValue(value);
        box.setPrefSize(width,40);
        return box;
    }

    private Label column(String text,double width){
        Label l = label(text,10,true,"#6D7785");
        l.setPrefWidth(width);
        l.setWrapText(true);
        return l;
    }

    private HBox documentRow(String[] r){
        HBox row = new HBox();
        row.setPrefHeight(65);
        row.setPadding(new Insets(10,15,10,15));
        row.setStyle("-fx-border-color:#EEF2F5;-fx-border-width:0 0 1 0;");

        for(int i=0;i<5;i++)
            row.getChildren().add(cell(r[i],new double[]{85,75,90,110,120}[i]));

        row.getChildren().add(statusCell(r[5]));
        row.getChildren().add(verifyButton());
        return row;
    }

    private Label statusCell(String status){
        Label l = label(status,10,true,
                status.equals("Verified") ? "#10B981" :
                status.equals("Rejected") ? "#EF233C" : "#F59E0B");
        l.setPrefWidth(70);
        return l;
    }

    private Button verifyButton(){
        Button b = new Button("Verify");
        b.setPrefSize(65,30);
        b.setTextFill(Color.WHITE);
        b.setFont(Font.font("Arial",FontWeight.BOLD,10));
        b.setStyle("-fx-background-color:#1683F7;-fx-background-radius:7;");
        return b;
    }

    private Button actionButton(String text,String color){
        Button b = new Button(text);
        b.setPrefSize(120,50);
        b.setTextFill(Color.WHITE);
        b.setFont(Font.font("Arial",FontWeight.BOLD,12));
        b.setStyle("-fx-background-color:"+color+";-fx-background-radius:10;");
        return b;
    }

    private Label cell(String text,double width){
        Label l = label(text,10,false,"#303B49");
        l.setPrefWidth(width);
        l.setWrapText(true);
        return l;
    }

    private HBox detail(String name,String value){
        Label n = label(name,10,false,"#8A94A3");
        n.setPrefWidth(105);
        Label v = label(value,10,true,"#111827");
        v.setWrapText(true);
        HBox row = new HBox(5,n,v);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    public BorderPane getRoot(){
         return root;
         }
    public VBox getContent(){
         return content;
         }
}
    

