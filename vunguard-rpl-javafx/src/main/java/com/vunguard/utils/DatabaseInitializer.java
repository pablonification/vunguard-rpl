package com.vunguard.utils;

import com.vunguard.config.DatabaseConfig;
import com.vunguard.dao.UserDAO;
import com.vunguard.models.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class DatabaseInitializer {
    private static final String SCHEMA_FILE = "/database/schema.sql";
    
    public static void initializeDatabase() {
        System.out.println("Initializing database...");
        
        try {
            // Test database connection
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            
            if (!dbConfig.testConnection()) {
                System.err.println("Failed to connect to database. Please check your database configuration.");
                return;
            }
            
            System.out.println("Database connection successful!");
            
            // Load and execute schema if needed
            // loadSchema();
            
            // Ensure default users exist
            ensureDefaultUsers();
            
            System.out.println("Database initialization completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void loadSchema() {
        System.out.println("Loading database schema...");
        
        try (InputStream inputStream = DatabaseInitializer.class.getResourceAsStream(SCHEMA_FILE)) {
            if (inputStream == null) {
                System.out.println("Schema file not found, skipping schema loading");
                return;
            }
            
            String schemaScript = readInputStream(inputStream);
            executeScript(schemaScript);
            
            System.out.println("Schema loaded successfully!");
            
        } catch (IOException | SQLException e) {
            System.err.println("Failed to load schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
    
    private static void executeScript(String script) throws SQLException {
        DatabaseConfig dbConfig = DatabaseConfig.getInstance();
        
        try (Connection connection = dbConfig.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Split script by semicolons and execute each statement
            String[] statements = script.split(";");
            
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty() && !sql.startsWith("--")) {
                    try {
                        statement.execute(sql);
                    } catch (SQLException e) {
                        // Log but continue with other statements
                        System.err.println("Warning: Failed to execute SQL statement: " + sql);
                        System.err.println("Error: " + e.getMessage());
                    }
                }
            }
        }
    }
    
    private static void ensureDefaultUsers() {
        System.out.println("Ensuring default users exist...");
        
        try {
            UserDAO userDAO = new UserDAO();
            
            // Check if admin user exists
            User adminUser = userDAO.findByUsername("admin");
            if (adminUser == null) {
                System.out.println("Creating default admin user...");
                User admin = new User();
                admin.setUsername("admin");
                admin.setFullName("System Administrator");
                admin.setEmail("admin@vunguard.com");
                admin.setRole("admin");
                admin.setCreatedAt(LocalDateTime.now());
                
                int adminId = userDAO.createUser(admin, "password");
                System.out.println("Admin user created with ID: " + adminId);
            } else {
                System.out.println("Admin user already exists");
            }
            
            // Check if analyst user exists
            User analystUser = userDAO.findByUsername("budi_analyst");
            if (analystUser == null) {
                System.out.println("Creating default analyst user...");
                User analyst = new User();
                analyst.setUsername("budi_analyst");
                analyst.setFullName("Budi Santoso");
                analyst.setEmail("budi@vunguard.com");
                analyst.setRole("analyst");
                analyst.setCreatedAt(LocalDateTime.now());
                
                int analystId = userDAO.createUser(analyst, "password");
                System.out.println("Analyst user created with ID: " + analystId);
            } else {
                System.out.println("Analyst user already exists");
            }
            
            // Check if manager user exists
            User managerUser = userDAO.findByUsername("sarah_manager");
            if (managerUser == null) {
                System.out.println("Creating default manager user...");
                User manager = new User();
                manager.setUsername("sarah_manager");
                manager.setFullName("Sarah Wilson");
                manager.setEmail("sarah@vunguard.com");
                manager.setRole("manager");
                manager.setCreatedAt(LocalDateTime.now());
                
                int managerId = userDAO.createUser(manager, "password");
                System.out.println("Manager user created with ID: " + managerId);
            } else {
                System.out.println("Manager user already exists");
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to create default users: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static boolean checkDatabaseConnection() {
        try {
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            return dbConfig.testConnection();
        } catch (Exception e) {
            System.err.println("Database connection check failed: " + e.getMessage());
            return false;
        }
    }
    
    public static void printDatabaseInfo() {
        try {
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            
            try (Connection connection = dbConfig.getConnection()) {
                String url = connection.getMetaData().getURL();
                String user = connection.getMetaData().getUserName();
                String dbProduct = connection.getMetaData().getDatabaseProductName();
                String dbVersion = connection.getMetaData().getDatabaseProductVersion();
                
                System.out.println("=== Database Information ===");
                System.out.println("URL: " + url);
                System.out.println("User: " + user);
                System.out.println("Product: " + dbProduct);
                System.out.println("Version: " + dbVersion);
                System.out.println("============================");
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to get database info: " + e.getMessage());
        }
    }
} 