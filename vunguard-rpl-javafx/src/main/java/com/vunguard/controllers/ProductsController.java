package com.vunguard.controllers;

import com.vunguard.models.Product;
import com.vunguard.services.ProductService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.util.List;

public class ProductsController {

    @FXML
    private VBox contentArea;
    
    @FXML
    private ScrollPane scrollPane;
    
    @FXML
    private SidebarController sidebarViewController;

    // Product data from database
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private TableView<Product> productsTable;
    private ProductService productService;

    // Add Product Dialog Fields
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> strategyComboBox;
    @FXML private ComboBox<String> riskLevelComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Stage addProductStage;

    @FXML
    private void initialize() {
        System.out.println("ProductsController initialized");
        
        // Initialize service
        try {
            productService = ProductService.getInstance();
        } catch (Exception e) {
            System.err.println("Failed to initialize ProductService: " + e.getMessage());
            e.printStackTrace();
        }
        
        if (sidebarViewController != null) {
            System.out.println("SidebarView Controller injected into ProductsController");
            try {
                sidebarViewController.setActiveButton(sidebarViewController.getProductsButton());
            } catch (Exception e) {
                System.err.println("Error setting active button: " + e.getMessage());
            }
        }
        
        // Configure ScrollPane
        if (scrollPane != null) {
            scrollPane.setFitToHeight(false);
            scrollPane.setPannable(true);
            scrollPane.getStyleClass().add("products-scroll-pane");
        }
        
        // Load products from database
        loadProductsFromDatabase();
        
        // Create products content
        setupProductsView();
    }

    private void setupProductsView() {
        if (contentArea == null) {
            System.err.println("contentArea is null! FXML mapping issue.");
            return;
        }
        
        // Clear existing content
        contentArea.getChildren().clear();
        
        // Create title and subtitle
        Label titleLabel = new Label("Products");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Label subtitleLabel = new Label("Manage your investment products and their details.");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #A0A0A0;");
        
        // Create Add Product button
        Button addProductButton = new Button("+ Add Product");
        addProductButton.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 16; -fx-font-size: 14px;");
        
        addProductButton.setOnAction(event -> {
            System.out.println("Add Product button clicked");
            showAddProductDialog();
        });
        
        // Header with Add Product button on the right
        HBox headerContainer = new HBox();
        headerContainer.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerContainer.getChildren().addAll(spacer, addProductButton);
        headerContainer.setPadding(new Insets(0, 0, 20, 0));
        
        // Create All Products section
        Label allProductsLabel = new Label("All Products");
        allProductsLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Label allProductsSubtitle = new Label("A list of all investment products in the system.");
        allProductsSubtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #A0A0A0;");
        
        // Create products table
        createProductsTable();
        
        // Add all elements to content area
        contentArea.getChildren().addAll(
            titleLabel, 
            subtitleLabel, 
            headerContainer,
            allProductsLabel,
            allProductsSubtitle,
            productsTable
        );
    }

    private void createProductsTable() {
        productsTable = new TableView<>();
        productsTable.setItems(productList);
        
        // Set table style
        productsTable.setStyle("-fx-background-color: #2A2A3A; -fx-border-color: #3A3A4A; -fx-border-radius: 8; -fx-background-radius: 8;");
        productsTable.setPrefHeight(400);
        
        // Create columns
        TableColumn<Product, String> codeColumn = new TableColumn<>("Code");
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeColumn.setPrefWidth(120);
        codeColumn.setStyle("-fx-text-fill: white;");
        
        TableColumn<Product, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);
        nameColumn.setStyle("-fx-text-fill: white;");
        
        TableColumn<Product, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setPrefWidth(350);
        descriptionColumn.setStyle("-fx-text-fill: white;");
        
        TableColumn<Product, String> strategyColumn = new TableColumn<>("Strategy");
        strategyColumn.setCellValueFactory(new PropertyValueFactory<>("strategy"));
        strategyColumn.setPrefWidth(120);
        strategyColumn.setStyle("-fx-text-fill: white;");
        
        TableColumn<Product, String> riskLevelColumn = new TableColumn<>("Risk Level");
        riskLevelColumn.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        riskLevelColumn.setPrefWidth(120);
        riskLevelColumn.setStyle("-fx-text-fill: white;");
        
        TableColumn<Product, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setPrefWidth(100);
        actionsColumn.setStyle("-fx-text-fill: white;");
        
        // Add action buttons to each row
        actionsColumn.setCellFactory(param -> new javafx.scene.control.TableCell<Product, Void>() {
            private final Button viewButton = new Button("View");
            
            {
                viewButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #60A5FA; -fx-border-color: transparent; -fx-cursor: hand;");
                viewButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    showProductDetails(product);
                });
            }
            
            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
        
        // Add columns to table
        productsTable.getColumns().addAll(codeColumn, nameColumn, descriptionColumn, strategyColumn, riskLevelColumn, actionsColumn);
        
        // Style table headers
        productsTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Product> row = new javafx.scene.control.TableRow<>();
            row.setStyle("-fx-background-color: #2A2A3A; -fx-text-fill: white;");
            return row;
        });
    }

    private void loadProductsFromDatabase() {
        if (productService == null) {
            System.err.println("ProductService not initialized, using empty data");
            return;
        }
        
        try {
            List<Product> products = productService.getAllProducts();
            productList.clear();
            productList.addAll(products);
            System.out.println("Loaded " + products.size() + " products from database");
        } catch (Exception e) {
            System.err.println("Failed to load products from database: " + e.getMessage());
            e.printStackTrace();
            
            // Show error to user
            showAlert(AlertType.ERROR, "Database Error", 
                     "Failed to load products from database. Please check your connection.");
        }
    }

    private void showAddProductDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vunguard/views/CreateProductView.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            
            addProductStage = new Stage();
            addProductStage.setTitle("Add New Product");
            addProductStage.initModality(Modality.APPLICATION_MODAL);
            addProductStage.setResizable(false);
            
            Scene scene = new Scene(root);
            addProductStage.setScene(scene);
            
            initializeAddProductDialog();
            addProductStage.showAndWait();
            
        } catch (IOException e) {
            System.err.println("Error loading Add Product dialog: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Dialog Loading Error");
            alert.setContentText("Could not load the Add Product dialog. Please try again.");
            alert.showAndWait();
        }
    }

    private void initializeAddProductDialog() {
        if (strategyComboBox != null) {
            strategyComboBox.setItems(FXCollections.observableArrayList("Growth", "Income", "Value", "Balanced"));
        }
        
        if (riskLevelComboBox != null) {
            riskLevelComboBox.setItems(FXCollections.observableArrayList("Low", "Medium", "High"));
        }
    }

    @FXML
    private void handleSaveAction(ActionEvent event) {
        if (validateInput()) {
            try {
                String code = codeField.getText().trim();
                String name = nameField.getText().trim();
                String description = descriptionField.getText().trim();
                String strategy = strategyComboBox.getValue();
                String riskLevel = riskLevelComboBox.getValue();
                
                // Create product via service
                boolean success = productService.createProduct(code, name, description, strategy, riskLevel);
                
                if (success) {
                    // Reload products from database
                    loadProductsFromDatabase();
                    
                    showAlert(AlertType.INFORMATION, "Success", "Product has been added successfully!");
                    
                    // Close dialog
                    if (addProductStage != null) {
                        addProductStage.close();
                    }
                } else {
                    showAlert(AlertType.ERROR, "Error", "Failed to create product. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("Error creating product: " + e.getMessage());
                showAlert(AlertType.ERROR, "Error", "Failed to create product: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancelAction(ActionEvent event) {
        if (addProductStage != null) {
            addProductStage.close();
        }
    }

    private boolean validateInput() {
        if (codeField.getText().trim().isEmpty()) {
            showAlert(AlertType.ERROR, "Validation Error", "Product code is required.");
            return false;
        }
        
        if (nameField.getText().trim().isEmpty()) {
            showAlert(AlertType.ERROR, "Validation Error", "Product name is required.");
            return false;
        }
        
        if (strategyComboBox.getValue() == null) {
            showAlert(AlertType.ERROR, "Validation Error", "Please select a strategy.");
            return false;
        }
        
        if (riskLevelComboBox.getValue() == null) {
            showAlert(AlertType.ERROR, "Validation Error", "Please select a risk level.");
            return false;
        }
        
        return true;
    }

    private void showProductDetails(Product product) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Product Details");
        alert.setHeaderText(product.getName());
        alert.setContentText(
            "Code: " + product.getCode() + "\n" +
            "Strategy: " + product.getStrategy() + "\n" +
            "Risk Level: " + product.getRiskLevel() + "\n\n" +
            "Description:\n" + product.getDescription()
        );
        alert.showAndWait();
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 