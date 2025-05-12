-- Create recommendations table
CREATE TABLE IF NOT EXISTS recommendations (
    id SERIAL PRIMARY KEY,
    analyst_id INTEGER NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    target_price DECIMAL(20,2) NOT NULL,
    current_price DECIMAL(20,2) NOT NULL,
    confidence INTEGER NOT NULL CHECK (confidence BETWEEN 1 AND 5),
    timeframe VARCHAR(50) NOT NULL,
    rationale TEXT NOT NULL,
    technical_analysis TEXT,
    fundamental_analysis TEXT,
    risks TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    implemented_at TIMESTAMP WITH TIME ZONE,

    -- Indexes for common queries
    INDEX idx_recommendations_analyst (analyst_id),
    INDEX idx_recommendations_product (product_id),
    INDEX idx_recommendations_status (status),
    INDEX idx_recommendations_created_at (created_at)
); 