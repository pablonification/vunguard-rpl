package com.vunguard.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static DatabaseConfig instance;
    private HikariDataSource dataSource;
    
    private DatabaseConfig() {
        initializeDataSource();
    }
    
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }
    
    private void initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            
            // Always load application.properties for username/password
            Properties props = new Properties();
            try {
                props.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
                logger.info("Successfully loaded application.properties");
            } catch (Exception e) {
                logger.warn("Could not load application.properties, using default values", e);
            }
            
            // Get database configuration - prefer environment variables for URL
            String dbUrl = System.getenv("DATABASE_URL");
            if (dbUrl == null) {
                dbUrl = System.getenv("POSTGRES_URL");
            }
            if (dbUrl == null) {
                dbUrl = props.getProperty("database.url", "jdbc:postgresql://localhost:5432/vunguard_db");
            }
            
            // Always get username/password from properties (more secure than env vars)
            String dbUser = props.getProperty("database.username", "vunguard_user");
            String dbPassword = props.getProperty("database.password", "vunguard_password");
            
            logger.info("Database config - URL: {}, User: {}, Password: {}", dbUrl, dbUser, (dbPassword != null ? "***SET***" : "NULL"));
            
            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            
            // Additional HikariCP configurations
            config.setMaximumPoolSize(20);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setLeakDetectionThreshold(60000);
            
            // PostgreSQL driver
            config.setDriverClassName("org.postgresql.Driver");
            
            this.dataSource = new HikariDataSource(config);
            
            logger.info("Database connection pool initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    public DataSource getDataSource() {
        return dataSource;
    }
    
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool shutdown completed");
        }
    }
    
    // Test database connection
    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection.isValid(5);
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
} 