package com.vunguard;

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
        // Load custom fonts
        try {
            Font.loadFont(getClass().getResourceAsStream("fonts/Inter_18pt-Regular.ttf"), 10); // Size doesn't matter here as it's for loading
            Font.loadFont(getClass().getResourceAsStream("fonts/Inter_18pt-Bold.ttf"), 10);
            // Tambahkan varian lain jika perlu (misal Inter-Medium.ttf, dll.)
        } catch (Exception e) {
            System.err.println("Error loading custom fonts: " + e.getMessage());
            // e.printStackTrace(); // Uncomment for debugging
        }

        // Untuk sementara, kita buat scene kosong sederhana.
        // Nanti kita akan memuat FXML untuk login di sini.
        Parent root = FXMLLoader.load(getClass().getResource("views/DashboardView.fxml")); // Muat DashboardView

        Scene scene = new Scene(root, 1280, 720); // Sesuaikan ukuran jika perlu
        
        // Load CSS
        String css = getClass().getResource("styles/application.css").toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setTitle("Vanguard Asset Management");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
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
} 