#!/bin/bash

# Vunguard RPL JavaFX Setup Script for Fedora Linux
# This script automates the setup process for Fedora systems

set -e  # Exit on any error

echo "=== Vunguard RPL JavaFX Setup for Fedora ==="
echo "This script will install and configure all necessary components."
echo

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Check if running as root
if [[ $EUID -eq 0 ]]; then
   print_error "This script should not be run as root"
   exit 1
fi

# Step 1: Update system and install Java
# print_step "1. Installing Java 17 and dependencies..."
# sudo dnf update -y
# sudo dnf install java-17-openjdk java-17-openjdk-devel git wget -y

# # Verify Java installation
# if java --version >/dev/null 2>&1; then
#     JAVA_VERSION=$(java --version | head -n1)
#     print_status "Java installed: $JAVA_VERSION"
# else
#     print_error "Java installation failed"
#     exit 1
# fi

# Step 2: Install PostgreSQL
print_step "2. Installing PostgreSQL..."
sudo dnf install postgresql postgresql-server postgresql-contrib -y

# Initialize PostgreSQL if not already done
if [ ! -f /var/lib/pgsql/data/postgresql.conf ]; then
    print_status "Initializing PostgreSQL database..."
    sudo postgresql-setup --initdb
fi

# Start PostgreSQL
print_status "Starting PostgreSQL service..."
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Wait for PostgreSQL to be ready
sleep 3

# Step 3: Configure PostgreSQL
print_step "3. Configuring PostgreSQL..."

# Backup pg_hba.conf
sudo cp /var/lib/pgsql/data/pg_hba.conf /var/lib/pgsql/data/pg_hba.conf.backup

# Update authentication method
sudo sed -i 's/local   all             all                                     peer/local   all             all                                     md5/' /var/lib/pgsql/data/pg_hba.conf
sudo sed -i 's/host    all             all             127.0.0.1\/32            ident/host    all             all             127.0.0.1\/32            md5/' /var/lib/pgsql/data/pg_hba.conf
sudo sed -i 's/host    all             all             ::1\/128                 ident/host    all             all             ::1\/128                 md5/' /var/lib/pgsql/data/pg_hba.conf

# Restart PostgreSQL
sudo systemctl restart postgresql

# Wait for restart
sleep 5

# Step 4: Create database and user
print_step "4. Creating database and user..."

# Temporary allow trust authentication for initial setup
print_status "Configuring temporary trust authentication..."
sudo sed -i 's/peer/trust/g' /var/lib/pgsql/data/pg_hba.conf
sudo sed -i 's/ident/trust/g' /var/lib/pgsql/data/pg_hba.conf
sudo sed -i 's/md5/trust/g' /var/lib/pgsql/data/pg_hba.conf
sudo systemctl restart postgresql

# Wait for restart
sleep 3

# Set password for postgres user (no password prompt now)
print_status "Setting postgres user password..."
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'postgres';" || {
    print_error "Failed to set postgres password"
    exit 1
}

# Create database and user
print_status "Creating database and user..."

# Check if database exists and create if not
sudo -u postgres psql -lqt | cut -d \| -f 1 | grep -qw vunguard_db || sudo -u postgres createdb vunguard_db

# Create user if not exists and set password
sudo -u postgres psql << EOF
DO \$\$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'vunguard_user') THEN
      CREATE USER vunguard_user;
   END IF;
END
\$\$;
ALTER USER vunguard_user WITH PASSWORD 'vunguard_password';
GRANT ALL PRIVILEGES ON DATABASE vunguard_db TO vunguard_user;
ALTER USER vunguard_user CREATEDB;

-- Connect to vunguard_db and grant schema permissions
\c vunguard_db;
GRANT CREATE ON SCHEMA public TO vunguard_user;
GRANT USAGE ON SCHEMA public TO vunguard_user;
GRANT ALL ON ALL TABLES IN SCHEMA public TO vunguard_user;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO vunguard_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO vunguard_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO vunguard_user;
\q
EOF

# Restore proper authentication
print_status "Restoring md5 authentication..."
sudo sed -i 's/trust/md5/g' /var/lib/pgsql/data/pg_hba.conf
sudo systemctl restart postgresql

# Wait for restart
sleep 3

print_status "Database 'vunguard_db' and user 'vunguard_user' created successfully"

# Step 5: Setup environment variables
print_step "5. Setting up environment variables..."

# Remove existing entries if any
grep -v "DATABASE_URL\|POSTGRES_URL" ~/.bashrc > ~/.bashrc.tmp || true
mv ~/.bashrc.tmp ~/.bashrc

# Add new environment variables
echo "" >> ~/.bashrc
echo "# Vunguard RPL Database Configuration" >> ~/.bashrc
echo 'export DATABASE_URL="jdbc:postgresql://localhost:5432/vunguard_db"' >> ~/.bashrc
echo 'export POSTGRES_URL="jdbc:postgresql://localhost:5432/vunguard_db"' >> ~/.bashrc

# Source the changes
source ~/.bashrc

print_status "Environment variables added to ~/.bashrc"

# Step 6: Test database connection
print_step "6. Testing database connection..."

if PGPASSWORD=vunguard_password psql -U vunguard_user -d vunguard_db -h localhost -c "SELECT version();" >/dev/null 2>&1; then
    print_status "Database connection test successful"
else
    print_error "Database connection test failed"
    print_warning "Please check PostgreSQL configuration"
fi

# Step 7: Setup database schema
print_step "7. Setting up database schema..."

if [ -f "src/main/resources/database/schema.sql" ]; then
    print_status "Creating database schema with postgres user..."
    PGPASSWORD=postgres psql -U postgres -d vunguard_db -h localhost -f src/main/resources/database/schema.sql
    
    # Grant permissions on created tables to vunguard_user
    print_status "Granting permissions to vunguard_user..."
    PGPASSWORD=postgres psql -U postgres -d vunguard_db -h localhost << 'EOF'
GRANT ALL ON ALL TABLES IN SCHEMA public TO vunguard_user;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO vunguard_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO vunguard_user;
EOF
    
    print_status "Database schema created successfully"
    
    # Show created tables
    print_status "Created tables:"
    PGPASSWORD=vunguard_password psql -U vunguard_user -d vunguard_db -h localhost -c "\dt"
    
    # Show default users
    print_status "Default users created:"
    PGPASSWORD=vunguard_password psql -U vunguard_user -d vunguard_db -h localhost -c "SELECT username, role, email FROM accounts;"
else
    print_warning "Schema file not found. Please run this script from the project root directory."
fi

# Step 8: Create logs directory
print_step "8. Creating logs directory..."
mkdir -p logs
print_status "Logs directory created"

# Step 9: Build application
print_step "9. Building application..."

if [ -f "gradlew" ]; then
    chmod +x gradlew
    print_status "Building with Gradle..."
    ./gradlew clean build
    
    if [ $? -eq 0 ]; then
        print_status "Build successful!"
    else
        print_warning "Build completed with warnings. Check output above."
    fi
else
    print_warning "Gradle wrapper not found. You may need to install Gradle manually."
fi

# Final instructions
echo
echo "=== Setup Complete! ==="
echo
print_status "Your Vunguard RPL JavaFX application is now set up on Fedora!"
echo
echo "Default login credentials:"
echo "  - Username: admin, Password: password (Administrator)"
echo "  - Username: budi_analyst, Password: password (Analyst)"
echo "  - Username: sarah_manager, Password: password (Manager)"
echo
echo "To run the application:"
echo "  cd $(pwd)"
echo "  ./gradlew run"
echo
echo "To check database status:"
echo "  sudo systemctl status postgresql"
echo
echo "To connect to database manually:"
echo "  PGPASSWORD=vunguard_password psql -U vunguard_user -d vunguard_db -h localhost"
echo
print_warning "Remember to restart your terminal or run 'source ~/.bashrc' to load environment variables"
echo 