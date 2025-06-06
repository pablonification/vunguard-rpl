-- Vunguard RPL Database Schema
-- PostgreSQL Schema for JavaFX Application

-- Create database (run this separately)
-- CREATE DATABASE vunguard_db;

-- Use the database
-- \c vunguard_db;

-- Create accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('investor', 'manager', 'analyst', 'admin')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create recommendations table
CREATE TABLE IF NOT EXISTS recommendations (
    id VARCHAR(50) PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    analyst_name VARCHAR(50) NOT NULL,
    type VARCHAR(10) NOT NULL CHECK (type IN ('BUY', 'SELL')),
    target_price DECIMAL(15, 2) NOT NULL,
    current_price DECIMAL(15, 2) NOT NULL,
    confidence INTEGER NOT NULL CHECK (confidence BETWEEN 1 AND 5),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'IMPLEMENTED')),
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    implemented TIMESTAMP,
    timeframe VARCHAR(20) NOT NULL CHECK (timeframe IN ('Short Term', 'Medium Term', 'Long Term')),
    rationale TEXT,
    technical_analysis TEXT,
    fundamental_analysis TEXT,
    risks TEXT,
    FOREIGN KEY (analyst_name) REFERENCES accounts(username) ON UPDATE CASCADE
);

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    symbol VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    sector VARCHAR(50),
    market_cap DECIMAL(20, 2),
    current_price DECIMAL(15, 2),
    price_change_24h DECIMAL(15, 2),
    price_change_percentage_24h DECIMAL(5, 2),
    volume_24h DECIMAL(20, 2),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create portfolios table
CREATE TABLE IF NOT EXISTS portfolios (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    total_value DECIMAL(20, 2) DEFAULT 0,
    cash_balance DECIMAL(20, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES accounts(id) ON DELETE CASCADE
);

-- Create portfolio_holdings table
CREATE TABLE IF NOT EXISTS portfolio_holdings (
    id SERIAL PRIMARY KEY,
    portfolio_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity DECIMAL(20, 8) NOT NULL,
    average_cost DECIMAL(15, 2) NOT NULL,
    current_value DECIMAL(20, 2),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE(portfolio_id, product_id)
);

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    portfolio_id INTEGER,
    product_id INTEGER,
    type VARCHAR(20) NOT NULL CHECK (type IN ('BUY', 'SELL', 'DEPOSIT', 'WITHDRAWAL')),
    quantity DECIMAL(20, 8),
    price DECIMAL(15, 2),
    total_amount DECIMAL(20, 2) NOT NULL,
    fees DECIMAL(20, 2) DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED', 'FAILED')),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    executed_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE SET NULL,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

-- Create performance_metrics table
CREATE TABLE IF NOT EXISTS performance_metrics (
    id SERIAL PRIMARY KEY,
    portfolio_id INTEGER NOT NULL,
    date DATE NOT NULL,
    total_value DECIMAL(20, 2) NOT NULL,
    cash_balance DECIMAL(20, 2) NOT NULL,
    invested_amount DECIMAL(20, 2) NOT NULL,
    daily_return DECIMAL(10, 4),
    cumulative_return DECIMAL(10, 4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    UNIQUE(portfolio_id, date)
);

-- Create sessions table for authentication
CREATE TABLE IF NOT EXISTS user_sessions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address INET,
    user_agent TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES accounts(id) ON DELETE CASCADE
);

-- Create audit_logs table for tracking user actions
CREATE TABLE IF NOT EXISTS audit_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(50),
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES accounts(id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_accounts_username ON accounts(username);
CREATE INDEX IF NOT EXISTS idx_accounts_email ON accounts(email);
CREATE INDEX IF NOT EXISTS idx_recommendations_analyst ON recommendations(analyst_name);
CREATE INDEX IF NOT EXISTS idx_recommendations_status ON recommendations(status);
CREATE INDEX IF NOT EXISTS idx_recommendations_created ON recommendations(created);
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_portfolios_user_id ON portfolios(user_id);
CREATE INDEX IF NOT EXISTS idx_performance_metrics_portfolio_date ON performance_metrics(portfolio_id, date);
CREATE INDEX IF NOT EXISTS idx_user_sessions_token ON user_sessions(session_token);
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);

-- Create triggers for updated_at columns
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create separate function for recommendations table that uses 'updated' column
CREATE OR REPLACE FUNCTION update_recommendations_updated_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_accounts_updated_at BEFORE UPDATE ON accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_recommendations_updated BEFORE UPDATE ON recommendations
    FOR EACH ROW EXECUTE FUNCTION update_recommendations_updated_column();

CREATE TRIGGER update_portfolios_updated_at BEFORE UPDATE ON portfolios
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert default admin user
INSERT INTO accounts (username, full_name, email, password, role) 
VALUES (
    'admin', 
    'System Administrator', 
    'admin@vunguard.com', 
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- password: password
    'admin'
) ON CONFLICT (username) DO NOTHING;

-- Insert sample analyst user
INSERT INTO accounts (username, full_name, email, password, role) 
VALUES (
    'budi_analyst', 
    'Budi Santoso', 
    'budi@vunguard.com', 
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- password: password
    'analyst'
) ON CONFLICT (username) DO NOTHING;

-- Insert sample manager user
INSERT INTO accounts (username, full_name, email, password, role) 
VALUES (
    'sarah_manager', 
    'Sarah Wilson', 
    'sarah@vunguard.com', 
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- password: password
    'manager'
) ON CONFLICT (username) DO NOTHING;

-- Insert sample products
INSERT INTO products (symbol, name, sector, market_cap, current_price, price_change_24h, price_change_percentage_24h, volume_24h) 
VALUES 
    ('AAPL', 'Apple Inc.', 'Technology', 3000000000000, 175.50, 2.50, 1.45, 50000000),
    ('GOOGL', 'Alphabet Inc.', 'Technology', 1800000000000, 142.30, -1.20, -0.84, 25000000),
    ('MSFT', 'Microsoft Corporation', 'Technology', 2800000000000, 375.80, 5.20, 1.40, 35000000),
    ('TSLA', 'Tesla Inc.', 'Automotive', 800000000000, 248.90, -3.10, -1.23, 45000000),
    ('AMZN', 'Amazon.com Inc.', 'E-commerce', 1500000000000, 145.70, 1.80, 1.25, 30000000)
ON CONFLICT (symbol) DO NOTHING; 