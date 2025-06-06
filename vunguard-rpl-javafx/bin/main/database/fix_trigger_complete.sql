-- Complete fix for recommendations table trigger
-- Run as superuser (postgres)

-- Connect to the database
\c vunguard_db;

-- Drop the incorrect trigger
DROP TRIGGER IF EXISTS update_recommendations_updated_at ON recommendations;

-- Drop the old function if it exists
DROP FUNCTION IF EXISTS update_recommendations_updated_column() CASCADE;

-- Create the correct function for recommendations table
CREATE OR REPLACE FUNCTION update_recommendations_updated_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create the correct trigger for recommendations table
CREATE TRIGGER update_recommendations_updated BEFORE UPDATE ON recommendations
    FOR EACH ROW EXECUTE FUNCTION update_recommendations_updated_column();

-- Grant necessary permissions to vunguard_user
GRANT EXECUTE ON FUNCTION update_recommendations_updated_column() TO vunguard_user;

-- Verify the trigger was created correctly
\d+ recommendations; 