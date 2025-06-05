package com.vunguard.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.event.ActionEvent;

public class SupportController {

    @FXML
    private VBox contentArea;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private SidebarController sidebarViewController;

    // Contact form fields
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField subjectField;
    @FXML
    private TextArea messageArea;
    @FXML
    private Button submitButton;

    @FXML
    private void initialize() {
        System.out.println("SupportController initialized");
        
        if (sidebarViewController != null) {
            System.out.println("SidebarView Controller injected into SupportController");
            // Set the support button as active in the sidebar
            try {
                sidebarViewController.setActiveButton(sidebarViewController.getSupportButton());
            } catch (Exception e) {
                System.err.println("Error setting active button: " + e.getMessage());
            }
        } else {
            System.out.println("SidebarView Controller NOT injected.");
        }

        // Configure ScrollPane
        if (scrollPane != null) {
            scrollPane.setFitToHeight(false);
            scrollPane.setPannable(true);
            scrollPane.getStyleClass().add("dashboard-scroll-pane");
        }

        setupSupportContent();
    }

    private void setupSupportContent() {
        if (contentArea == null) {
            System.err.println("ContentArea is null!");
            return;
        }

        contentArea.getChildren().clear();
        contentArea.setSpacing(30);
        contentArea.setPadding(new Insets(30));

        // Header Section
        VBox headerSection = createHeaderSection();
        contentArea.getChildren().add(headerSection);

        // FAQ Section
        VBox faqSection = createFAQSection();
        contentArea.getChildren().add(faqSection);        // Contact Section
        VBox contactSection = createContactSection();
        contentArea.getChildren().add(contactSection);
    }

    private VBox createHeaderSection() {
        VBox headerSection = new VBox(15);
        headerSection.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label("Support Center");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Inter 18pt';");

        Label subtitleLabel = new Label("Get help with your Vanguard Asset Management account");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #A0A0A0; -fx-font-family: 'Inter 18pt';");

        headerSection.getChildren().addAll(titleLabel, subtitleLabel);
        return headerSection;
    }

    private VBox createFAQSection() {
        VBox faqSection = new VBox(20);
        faqSection.setStyle("-fx-background-color: #1E1E2C; -fx-padding: 25; -fx-background-radius: 10;");

        Label faqTitle = new Label("Frequently Asked Questions");
        faqTitle.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Inter 18pt';");

        // FAQ Items
        VBox faqItems = new VBox(15);
        
        faqItems.getChildren().addAll(
            createFAQItem("How do I create a new portfolio?", 
                "To create a new portfolio, navigate to the Portfolios page and click the 'Create Portfolio' button. Fill in the required information including portfolio name, initial asset value, and cash balance."),
            
            createFAQItem("How can I track my investment performance?", 
                "Visit the Performance page to view detailed analytics of your portfolios, including returns, asset allocation, and benchmark comparisons with interactive charts and metrics."),
            
            createFAQItem("How do I add transactions to my portfolio?", 
                "Go to the Transactions page and click 'New Transaction'. Select the portfolio, transaction type (buy/sell), and enter the transaction details."),
            
            createFAQItem("Can I export my portfolio data?", 
                "Currently, you can view detailed portfolio information through the application. Export functionality will be available in future updates."),
            
            createFAQItem("How do I change my account settings?", 
                "Account settings can be accessed through the Accounts page (for admin users) or by contacting support for regular users."),
            
            createFAQItem("What investment products are supported?", 
                "Vanguard Asset Management supports various investment products including stocks, bonds, mutual funds, and ETFs with different risk levels.")
        );

        faqSection.getChildren().addAll(faqTitle, faqItems);
        return faqSection;
    }

    private VBox createFAQItem(String question, String answer) {
        VBox faqItem = new VBox(8);
        faqItem.setStyle("-fx-background-color: #2A2A3A; -fx-padding: 15; -fx-background-radius: 8;");

        Label questionLabel = new Label(question);
        questionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Inter 18pt';");

        Label answerLabel = new Label(answer);
        answerLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #D0D0D0; -fx-font-family: 'Inter 18pt'; -fx-wrap-text: true;");
        answerLabel.setWrapText(true);

        faqItem.getChildren().addAll(questionLabel, answerLabel);
        return faqItem;
    }

    private VBox createContactSection() {
        VBox contactSection = new VBox(20);
        contactSection.setStyle("-fx-background-color: #1E1E2C; -fx-padding: 25; -fx-background-radius: 10;");

        Label contactTitle = new Label("Contact Support");
        contactTitle.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Inter 18pt';");

        Label contactSubtitle = new Label("Send us a message and we'll get back to you as soon as possible");
        contactSubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #A0A0A0; -fx-font-family: 'Inter 18pt';");

        // Contact Form
        VBox contactForm = createContactForm();

        contactSection.getChildren().addAll(contactTitle, contactSubtitle, contactForm);
        return contactSection;
    }

    private VBox createContactForm() {
        VBox form = new VBox(15);
        form.setMaxWidth(500);

        // Name Field
        VBox nameGroup = new VBox(5);
        Label nameLabel = new Label("Full Name *");
        nameLabel.setStyle("-fx-text-fill: #B0B0B0; -fx-font-family: 'Inter 18pt'; -fx-font-size: 14px;");
        
        nameField = new TextField();
        nameField.setPromptText("Enter your full name");
        nameField.setStyle("-fx-background-color: #2A2A3A; -fx-text-fill: white; -fx-font-family: 'Inter 18pt'; " +
                          "-fx-background-radius: 5; -fx-border-color: #3A3A4A; -fx-border-radius: 5; " +
                          "-fx-padding: 10; -fx-font-size: 14px;");
        nameField.setPrefHeight(40);
        nameGroup.getChildren().addAll(nameLabel, nameField);

        // Email Field
        VBox emailGroup = new VBox(5);
        Label emailLabel = new Label("Email Address *");
        emailLabel.setStyle("-fx-text-fill: #B0B0B0; -fx-font-family: 'Inter 18pt'; -fx-font-size: 14px;");
        
        emailField = new TextField();
        emailField.setPromptText("Enter your email address");
        emailField.setStyle("-fx-background-color: #2A2A3A; -fx-text-fill: white; -fx-font-family: 'Inter 18pt'; " +
                           "-fx-background-radius: 5; -fx-border-color: #3A3A4A; -fx-border-radius: 5; " +
                           "-fx-padding: 10; -fx-font-size: 14px;");
        emailField.setPrefHeight(40);
        emailGroup.getChildren().addAll(emailLabel, emailField);

        // Subject Field
        VBox subjectGroup = new VBox(5);
        Label subjectLabel = new Label("Subject *");
        subjectLabel.setStyle("-fx-text-fill: #B0B0B0; -fx-font-family: 'Inter 18pt'; -fx-font-size: 14px;");
        
        subjectField = new TextField();
        subjectField.setPromptText("Brief description of your issue");
        subjectField.setStyle("-fx-background-color: #2A2A3A; -fx-text-fill: white; -fx-font-family: 'Inter 18pt'; " +
                             "-fx-background-radius: 5; -fx-border-color: #3A3A4A; -fx-border-radius: 5; " +
                             "-fx-padding: 10; -fx-font-size: 14px;");
        subjectField.setPrefHeight(40);
        subjectGroup.getChildren().addAll(subjectLabel, subjectField);

        // Message Area
        VBox messageGroup = new VBox(5);
        Label messageLabel = new Label("Message *");
        messageLabel.setStyle("-fx-text-fill: #B0B0B0; -fx-font-family: 'Inter 18pt'; -fx-font-size: 14px;");
        
        messageArea = new TextArea();
        messageArea.setPromptText("Describe your issue or question in detail...");
        messageArea.setStyle("-fx-background-color: #2A2A3A; -fx-text-fill: white; -fx-font-family: 'Inter 18pt'; " +
                            "-fx-background-radius: 5; -fx-border-color: #3A3A4A; -fx-border-radius: 5; " +
                            "-fx-padding: 10; -fx-font-size: 14px;");
        messageArea.setPrefHeight(120);
        messageArea.setWrapText(true);
        messageGroup.getChildren().addAll(messageLabel, messageArea);

        // Submit Button
        submitButton = new Button("Send Message");
        submitButton.setStyle("-fx-background-color: #4ADE80; -fx-text-fill: #0F172A; -fx-font-family: 'Inter 18pt'; " +
                             "-fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 5; -fx-font-size: 14px;");
        submitButton.setPrefHeight(45);
        submitButton.setOnAction(this::handleSubmitAction);

        // Enable/disable submit button based on form validation
        submitButton.disableProperty().bind(
            nameField.textProperty().isEmpty()
            .or(emailField.textProperty().isEmpty())
            .or(subjectField.textProperty().isEmpty())
            .or(messageArea.textProperty().isEmpty())
        );

        form.getChildren().addAll(nameGroup, emailGroup, subjectGroup, messageGroup, submitButton);
        return form;
    }@FXML
    private void handleSubmitAction(ActionEvent event) {
        if (validateContactForm()) {
            // Simulate sending the message
            showSuccessAlert("Message sent successfully! We'll get back to you within 24 hours.");
            clearContactForm();
        }
    }

    private boolean validateContactForm() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String subject = subjectField.getText().trim();
        String message = messageArea.getText().trim();

        if (name.isEmpty() || email.isEmpty() || subject.isEmpty() || message.isEmpty()) {
            showAlert("Please fill in all required fields.", AlertType.ERROR);
            return false;
        }

        // Basic email validation
        if (!email.contains("@") || !email.contains(".")) {
            showAlert("Please enter a valid email address.", AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void clearContactForm() {
        nameField.clear();
        emailField.clear();
        subjectField.clear();
        messageArea.clear();
    }

    private void showAlert(String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Support");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert dialog
        alert.getDialogPane().setStyle("-fx-background-color: #1E1E2C;");
        
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            if (type == AlertType.ERROR) {
                okButton.setStyle("-fx-background-color: #F87171; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
            } else {
                okButton.setStyle("-fx-background-color: #0A84FF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
            }
        }
        
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert dialog
        alert.getDialogPane().setStyle("-fx-background-color: #1E1E2C;");
        
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle("-fx-background-color: #4ADE80; -fx-text-fill: #0F172A; -fx-font-weight: bold; -fx-background-radius: 5;");
        }
        
        alert.showAndWait();
    }
}
