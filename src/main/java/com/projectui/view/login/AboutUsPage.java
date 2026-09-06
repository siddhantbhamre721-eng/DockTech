package com.projectui.view.login;

import java.io.File;
import java.io.InputStream;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class AboutUsPage {

    private Stage stage;

    // Design Tokens matching DockTech dark maritime theme
    private static final String BG_NAVY = "#020B24";
    private static final String CARD_BG = "rgba(15, 29, 61, 0.85)";
    private static final String CARD_BORDER = "#1E3A8A";
    private static final String ACCENT_CYAN = "#38BDF8";
    private static final String ACCENT_BLUE = "#0B63FF";
    private static final String TEXT_WHITE = "#FFFFFF";
    private static final String TEXT_MUTED = "#94A3B8";

    public void show(Stage stage) {
        this.stage = stage;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_NAVY + ";");

        // Top Navigation Bar with Back button - matching Dashboard header height & padding
        HBox navBar = createTopNavBar();
        root.setTop(navBar);

        // Main Scrollable Content - matching Dashboard margins & padding (Insets(18, 20, 20, 20), spacing 14)
        VBox contentContainer = new VBox(14);
        contentContainer.setAlignment(Pos.TOP_CENTER);
        contentContainer.setPadding(new Insets(18, 20, 20, 20));
        contentContainer.setMaxWidth(1100);

        // Header Section
        VBox headerBox = createHeaderSection();

        // 1. Special Thanks (Shashi Sir / Core2web)
        VBox specialThanksCard = createSpecialThanksCard();

        // 2. Project Description Card
        VBox projectDescCard = createProjectDescriptionCard();

        // 3. Mentorship & Guidance Row (Instructors, Super Mentors, Mentors & TeamLeads)
        HBox mentorshipRow = createMentorshipRow();

        // 4. Team Info Card
        VBox teamCard = createTeamInfoCard();

        // 5. Footer
        VBox footerBox = createFooter();

        contentContainer.getChildren().addAll(
                headerBox,
                specialThanksCard,
                projectDescCard,
                mentorshipRow,
                teamCard,
                footerBox
        );

        StackPane centerWrapper = new StackPane(contentContainer);
        centerWrapper.setAlignment(Pos.TOP_CENTER);
        centerWrapper.setStyle("-fx-background-color: transparent;");

        ScrollPane scrollPane = new ScrollPane(centerWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: " + BG_NAVY + "; -fx-background-color: " + BG_NAVY + "; -fx-viewport-border: transparent; -fx-border-color: transparent;");
        LoginPage.applyInvisibleScrollbar(scrollPane);

        root.setCenter(scrollPane);

        LoginPage.showRoot(stage, root);
        stage.setTitle("About Us — DockTech Smart Maritime Operations");
        if (!stage.isMaximized()) {
            stage.setMaximized(true);
        }
        stage.show();
    }

    private HBox createTopNavBar() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 18, 12, 18));
        bar.setStyle("-fx-background-color: rgba(6, 16, 46, 0.95); -fx-border-color: rgba(30, 58, 138, 0.5); -fx-border-width: 0 0 1 0;");

        Button backBtn = new Button("← Back to Login");
        backBtn.setStyle("-fx-background-color: #0F2356; -fx-text-fill: #38BDF8; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 18; -fx-background-radius: 8; -fx-border-color: #1E40AF; -fx-border-radius: 8; -fx-cursor: hand;");
        backBtn.setOnMouseEntered(e -> backBtn.setStyle("-fx-background-color: #1E40AF; -fx-text-fill: #FFFFFF; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 18; -fx-background-radius: 8; -fx-border-color: #38BDF8; -fx-border-radius: 8; -fx-cursor: hand;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle("-fx-background-color: #0F2356; -fx-text-fill: #38BDF8; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 18; -fx-background-radius: 8; -fx-border-color: #1E40AF; -fx-border-radius: 8; -fx-cursor: hand;"));
        backBtn.setOnAction(e -> new LoginPage().show(stage));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label brandBadge = new Label("⚓ DockTech • Smart Maritime Operations");
        brandBadge.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-color: rgba(30, 58, 138, 0.3); -fx-background-radius: 20;");

        bar.getChildren().addAll(backBtn, spacer, brandBadge);
        return bar;
    }

    private VBox createHeaderSection() {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10, 0, 10, 0));

        Label title = new Label("About Us");
        title.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_WHITE + ";");

        Label subtitle = new Label("People behind DockTech – Smart Maritime Operations");
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: " + ACCENT_CYAN + "; -fx-font-weight: 500;");

        box.getChildren().addAll(title, subtitle);
        return box;
    }

    private VBox createSpecialThanksCard() {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: " + CARD_BG + "; -fx-border-color: " + CARD_BORDER + "; -fx-border-radius: 14; -fx-background-radius: 14; -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.4), 14, 0, 0, 6);");
        card.setPadding(new Insets(24));

        HBox row = new HBox(28);
        row.setAlignment(Pos.CENTER_LEFT);

        // Left: Image of Shashi Sir / Core2web (cleanly displayed without any background photo)
        Image photo = loadShashiSirImage();
        ImageView photoView = new ImageView();
        if (photo != null && !photo.isError()) {
            photoView.setImage(photo);
            photoView.setFitWidth(260);
            photoView.setFitHeight(270);
            photoView.setPreserveRatio(true);
            photoView.setSmooth(true);

            Rectangle clip = new Rectangle(260, 270);
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            photoView.setClip(clip);
        }

        VBox photoContainer = new VBox(photoView);
        photoContainer.setAlignment(Pos.CENTER);
        photoContainer.setStyle("-fx-background-color: transparent;");

        // Right: Special Thanks details
        VBox infoBox = new VBox(10);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label badge = new Label("SPECIAL THANKS");
        badge.setStyle("-fx-background-color: rgba(56, 189, 248, 0.15); -fx-text-fill: #38BDF8; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 16; -fx-border-color: rgba(56, 189, 248, 0.4); -fx-border-radius: 16;");

        Label shashiTitle = new Label("Thanks to Shashi sir");
        shashiTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_WHITE + ";");

        Label orgLbl = new Label("Core2web");
        orgLbl.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #60A5FA;");

        Label appreciationMsg = new Label(
                "We express our heartfelt gratitude to Shashi Sir and Core2web for their invaluable guidance, " +
                "continuous support, and for providing us the opportunity to work on this project. His inspiring mentorship " +
                "and motivation have been the cornerstone of our journey in building DockTech – Smart Maritime Operations."
        );
        appreciationMsg.setWrapText(true);
        appreciationMsg.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT_MUTED + "; -fx-line-spacing: 4px;");

        infoBox.getChildren().addAll(badge, shashiTitle, orgLbl, appreciationMsg);

        row.getChildren().addAll(photoContainer, infoBox);
        card.getChildren().add(row);
        return card;
    }

    private VBox createProjectDescriptionCard() {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: " + CARD_BG + "; -fx-border-color: " + CARD_BORDER + "; -fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 24;");

        Label title = new Label("Project Description");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_WHITE + ";");

        Label desc = new Label(
                "DockTech – Smart Maritime Operations is a comprehensive port management system designed to digitize and streamline port operations. " +
                "The platform helps in managing ports, vessels, cargo, berth allocation, documentation, payments and real-time marine data with a modern and user-friendly interface. " +
                "Our goal is to build a smarter, safer and more efficient maritime ecosystem using technology."
        );
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT_MUTED + "; -fx-line-spacing: 4px;");

        card.getChildren().addAll(title, desc);
        return card;
    }

    private HBox createMentorshipRow() {
        HBox row = new HBox(18);
        row.setAlignment(Pos.CENTER);

        // 1. Thanks to Instructors
        VBox instructorsCard = createSectionCard("Thanks to Instructors", new String[]{
                "Sachin Sir",
                "Pramod Sir",
                "Akshay Sir"
        }, null);
        HBox.setHgrow(instructorsCard, Priority.ALWAYS);

        // 2. Thanks to Super Mentors
        VBox superMentorsCard = createSectionCard("Thanks to Super Mentors", new String[]{
                "Shiv Sir",
                "Subodh Sir"
        }, null);
        HBox.setHgrow(superMentorsCard, Priority.ALWAYS);

        // 3. Thanks to Mentors & TeamLeads
        VBox mentorsCard = createSectionCard("Thanks to mentors & TeamLeads", null,
                "For their continuous support, guidance and encouragement throughout this journey.");
        HBox.setHgrow(mentorsCard, Priority.ALWAYS);

        row.getChildren().addAll(instructorsCard, superMentorsCard, mentorsCard);
        return row;
    }

    private VBox createSectionCard(String heading, String[] items, String descriptionText) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: " + CARD_BG + "; -fx-border-color: " + CARD_BORDER + "; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20;");
        card.setAlignment(Pos.TOP_LEFT);

        Label title = new Label(heading);
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + ACCENT_CYAN + ";");

        card.getChildren().add(title);

        if (items != null) {
            VBox itemsBox = new VBox(8);
            for (String item : items) {
                HBox itemRow = new HBox(8);
                itemRow.setAlignment(Pos.CENTER_LEFT);
                Label bullet = new Label("•");
                bullet.setStyle("-fx-text-fill: #38BDF8; -fx-font-size: 16px; -fx-font-weight: bold;");
                Label nameLbl = new Label(item);
                nameLbl.setStyle("-fx-text-fill: " + TEXT_WHITE + "; -fx-font-size: 14px; -fx-font-weight: 500;");
                itemRow.getChildren().addAll(bullet, nameLbl);
                itemsBox.getChildren().add(itemRow);
            }
            card.getChildren().add(itemsBox);
        }

        if (descriptionText != null) {
            Label descLbl = new Label(descriptionText);
            descLbl.setWrapText(true);
            descLbl.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px; -fx-line-spacing: 3px;");
            card.getChildren().add(descLbl);
        }

        return card;
    }

    private VBox createTeamInfoCard() {
        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: " + CARD_BG + "; -fx-border-color: " + CARD_BORDER + "; -fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 24;");

        Label title = new Label("Team Info");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_WHITE + ";");

        HBox membersRow = new HBox(16);
        membersRow.setAlignment(Pos.CENTER);

        String[] teamMembers = {
                "Siddhant Sunil Bhamre",
                "Pravin Vittal Chavan",
                "Om Dipak Kanawade",
                "Raghunath Arjun Mahapure"
        };

        for (String member : teamMembers) {
            VBox memberBox = new VBox(10);
            memberBox.setAlignment(Pos.CENTER);
            memberBox.setPadding(new Insets(16));
            memberBox.setStyle("-fx-background-color: rgba(6, 16, 46, 0.7); -fx-border-color: #1E3A8A; -fx-border-radius: 10; -fx-background-radius: 10;");
            HBox.setHgrow(memberBox, Priority.ALWAYS);

            Label avatar = new Label("👤");
            avatar.setStyle("-fx-font-size: 26px;");

            Label name = new Label(member);
            name.setWrapText(true);
            name.setAlignment(Pos.CENTER);
            name.setStyle("-fx-text-fill: " + TEXT_WHITE + "; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-alignment: center;");

            Label role = new Label("Core Developer");
            role.setStyle("-fx-text-fill: " + ACCENT_CYAN + "; -fx-font-size: 11px;");

            memberBox.getChildren().addAll(avatar, name, role);
            membersRow.getChildren().add(memberBox);
        }

        card.getChildren().addAll(title, membersRow);
        return card;
    }

    private VBox createFooter() {
        VBox footer = new VBox(10);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 10, 0));

        Label text = new Label("Together We Learn | Together We Build | Together We Grow ❤️");
        text.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #E2E8F0; -fx-effect: dropshadow(gaussian, rgba(56, 189, 248, 0.3), 8, 0, 0, 0);");

        footer.getChildren().add(text);
        return footer;
    }

    private Image loadShashiSirImage() {
        try {
            InputStream stream = getClass().getResourceAsStream("/Assets/images/shashi_sir.png");
            if (stream == null) {
                stream = getClass().getResourceAsStream("/assets/images/shashi_sir.png");
            }
            if (stream != null) {
                return new Image(stream);
            }
        } catch (Exception ignored) {}

        File f = new File("src/main/resources/Assets/images/shashi_sir.png");
        if (f.exists()) {
            return new Image(f.toURI().toString());
        }
        File f2 = new File("src/main/resources/assets/images/shashi_sir.png");
        if (f2.exists()) {
            return new Image(f2.toURI().toString());
        }
        return null;
    }
}
