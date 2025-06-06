package com.vunguard;

import com.vunguard.config.DatabaseConfig;
import com.vunguard.utils.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.io.IOException;

public class Main extends Application {

    private static Stage primaryStageRef;

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStageRef = primaryStage;
        
        // Initialize backend components
        initializeBackend();
        
        // Load custom fonts
        try {
            Font.loadFont(getClass().getResourceAsStream("fonts/Inter_18pt-Regular.ttf"), 10); // Size doesn't matter here as it's for loading
            Font.loadFont(getClass().getResourceAsStream("fonts/Inter_18pt-Bold.ttf"), 10);
            // Tambahkan varian lain jika perlu (misal Inter-Medium.ttf, dll.)
        } catch (Exception e) {
            System.err.println("Error loading custom fonts: " + e.getMessage());
            // e.printStackTrace(); // Uncomment for debugging
        }

        // Start with login screen initially
        Parent root = FXMLLoader.load(getClass().getResource("views/LoginView.fxml"));

        Scene scene = new Scene(root, 800, 600);
        
        // Load CSS
        String css = getClass().getResource("styles/application.css").toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setTitle("Vanguard Asset Management - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void initializeBackend() {
        System.out.println("=== Vunguard RPL Application Starting ===");
        
        try {
            // Initialize database
            DatabaseInitializer.printDatabaseInfo();
            DatabaseInitializer.initializeDatabase();
            
            System.out.println("Backend initialization completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Backend initialization failed: " + e.getMessage());
            e.printStackTrace();
            
            // Show error dialog but continue with application startup
            System.err.println("Application will continue without backend functionality");
        }
    }

    public static void main(String[] args) {
        // Add shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down application...");
            try {
                DatabaseConfig.getInstance().shutdown();
            } catch (Exception e) {
                System.err.println("Error during shutdown: " + e.getMessage());
            }
        }));
        
        launch(args);
    }

    public static void loadLoginScene() throws IOException {
        if (primaryStageRef != null) {
            Parent root = FXMLLoader.load(Main.class.getResource("views/LoginView.fxml"));
            Scene scene = new Scene(root, 800, 600);
            
            // Load CSS
            String css = Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            primaryStageRef.setScene(scene);
            primaryStageRef.setTitle("Vanguard Asset Management - Login");
        }
    }

    public static void loadDashboardScene() throws IOException {
        if (primaryStageRef != null) {
            Parent root = FXMLLoader.load(Main.class.getResource("views/DashboardView.fxml"));
            Scene scene = new Scene(root, 1280, 720);
            
            // Load CSS
            String css = Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            primaryStageRef.setScene(scene);
            primaryStageRef.setTitle("Vanguard Asset Management - Dashboard");
        }
    }
    
    public static void loadProductsScene() throws IOException {
        if (primaryStageRef != null) {
            Parent root = FXMLLoader.load(Main.class.getResource("views/ProductsView.fxml"));
            Scene scene = new Scene(root, 1280, 720);
            
            // Load CSS
            String css = Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            primaryStageRef.setScene(scene);
            primaryStageRef.setTitle("Vanguard Asset Management - Products");
        }
    }
    
    public static void loadPortfolioScene() throws IOException {
        if (primaryStageRef != null) {
            Parent root = FXMLLoader.load(Main.class.getResource("views/PortfolioView.fxml"));
            Scene scene = new Scene(root, 1280, 720);
            
            // Load CSS
            String css = Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            primaryStageRef.setScene(scene);
            primaryStageRef.setTitle("Vanguard Asset Management - Portfolios");
        }
    }
    
    public static void loadTransactionScene() throws IOException {
        if (primaryStageRef != null) {
            Parent root = FXMLLoader.load(Main.class.getResource("views/TransactionView.fxml"));
            Scene scene = new Scene(root, 1280, 720);
            
            // Load CSS
            String css = Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            primaryStageRef.setScene(scene);
            primaryStageRef.setTitle("Vanguard Asset Management - Transactions");
        }
    }
    
    public static void loadAccountsScene() throws IOException {
        if (primaryStageRef != null) {
            Parent root = FXMLLoader.load(Main.class.getResource("views/AccountsView.fxml"));
            Scene scene = new Scene(root, 1280, 720);
            
            // Load CSS
            String css = Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            primaryStageRef.setScene(scene);
            primaryStageRef.setTitle("Vanguard Asset Management - Accounts");
        }
    }

    public static void loadRegistrationScene() throws IOException {
        if (primaryStageRef != null) {
            Parent root = FXMLLoader.load(Main.class.getResource("views/RegistrationView.fxml"));
            Scene scene = new Scene(root, 800, 600);
        
            // Load CSS
            String css = Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(css);
        
            primaryStageRef.setScene(scene);
            primaryStageRef.setTitle("Vanguard Asset Management - Registration");
        }
    }

    public static void loadPerformanceScene() throws IOException {
        if (primaryStageRef != null) {
            Parent root = FXMLLoader.load(Main.class.getResource("views/PerformanceView.fxml"));
            Scene scene = new Scene(root, 1280, 720);
            
            // Load CSS
            String css = Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            primaryStageRef.setScene(scene);
            primaryStageRef.setTitle("Vanguard Asset Management - Performance");
        }
    }

    public static void loadSupportScene() throws IOException {
        if (primaryStageRef != null) {
            Parent root = FXMLLoader.load(Main.class.getResource("views/SupportView.fxml"));
            Scene scene = new Scene(root, 1280, 720);
            
            // Load CSS
            String css = Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            primaryStageRef.setScene(scene);
            primaryStageRef.setTitle("Vanguard Asset Management - Support");
        }
    }

    public static void loadReviewRecommendationsScene() throws IOException {
        if (primaryStageRef != null) {
            Parent root = FXMLLoader.load(Main.class.getResource("views/ReviewRecommendationsView.fxml"));
            Scene scene = new Scene(root, 1280, 720);
            
            // Load CSS
            String css = Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            primaryStageRef.setScene(scene);
            primaryStageRef.setTitle("Vanguard Asset Management - Review Recommendations");
        }
    }

    public static void loadInvestmentRecommendationsScene() throws IOException {
        if (primaryStageRef != null) {
            Parent root = FXMLLoader.load(Main.class.getResource("views/InvestmentRecommendationsView.fxml"));
            Scene scene = new Scene(root, 1280, 720);
            
            // Load CSS
            String css = Main.class.getResource("styles/application.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            primaryStageRef.setScene(scene);
            primaryStageRef.setTitle("Vanguard Asset Management - Investment Recommendations");
        }
    }
}