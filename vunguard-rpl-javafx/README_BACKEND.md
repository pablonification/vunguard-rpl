# Vunguard RPL JavaFX Backend Setup

## Overview

Aplikasi Vunguard RPL JavaFX menggunakan arsitektur backend yang lengkap dengan:

- **Database Layer**: PostgreSQL dengan HikariCP connection pooling
- **DAO Layer**: Data Access Objects untuk operasi database
- **Service Layer**: Business logic dan authentication
- **Configuration**: Aplikasi dan database configuration management

## Prerequisites

1. **Java 17** atau lebih tinggi
2. **PostgreSQL 12** atau lebih tinggi
3. **Gradle** untuk build management

## Database Setup

### 1. Install PostgreSQL

```bash
# Ubuntu/Debian
sudo apt-get install postgresql postgresql-contrib

# macOS with Homebrew
brew install postgresql

# Windows
# Download dan install dari https://www.postgresql.org/download/windows/
```

### 2. Create Database dan User

```sql
-- Connect ke PostgreSQL sebagai superuser
sudo -u postgres psql

-- Create database
CREATE DATABASE vunguard_db;

-- Create user (opsional)
CREATE USER vunguard_user WITH PASSWORD 'vunguard_password';
GRANT ALL PRIVILEGES ON DATABASE vunguard_db TO vunguard_user;

-- Exit
\q
```

### 3. Run Database Schema

```bash
# Navigate ke project directory
cd vunguard-rpl-javafx

# Run schema script
psql -U postgres -d vunguard_db -f src/main/resources/database/schema.sql

# Atau dengan user yang dibuat
psql -U vunguard_user -d vunguard_db -f src/main/resources/database/schema.sql
```

## Configuration

### 1. Environment Variables

Set environment variables untuk database connection:

```bash
# Untuk development lokal
export DATABASE_URL="jdbc:postgresql://localhost:5432/vunguard_db"
export POSTGRES_URL="jdbc:postgresql://localhost:5432/vunguard_db"

# Untuk production (jika menggunakan Neon atau cloud database)
export DATABASE_URL="postgresql://username:password@hostname:port/database?sslmode=require"
```

### 2. Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
database.url=jdbc:postgresql://localhost:5432/vunguard_db
database.username=postgres
database.password=password

# Application Configuration
app.name=Vunguard RPL
app.version=1.0.0
app.debug=false

# Security Configuration
jwt.secret=your_jwt_secret_here_change_in_production
password.minLength=6
```

## Building dan Running

### 1. Build Dependencies

```bash
# Install dependencies
./gradlew build

# Atau refresh dependencies
./gradlew clean build
```

### 2. Run Application

```bash
# Run aplikasi
./gradlew run

# Atau dengan specific main class
./gradlew run --main-class=com.vunguard.Main
```

### 3. Package Application

```bash
# Create JAR file
./gradlew jar

# Create distribution
./gradlew distZip
```

## Backend Architecture

### 1. Database Layer (`com.vunguard.config`)

- `DatabaseConfig.java`: Manajemen koneksi database dengan HikariCP
- `AppConfig.java`: Konfigurasi aplikasi dari properties file

### 2. DAO Layer (`com.vunguard.dao`)

- `BaseDAO.java`: Base class dengan operasi database umum
- `UserDAO.java`: User/Account data access operations
- `RecommendationDAO.java`: Recommendation data access operations

### 3. Service Layer (`com.vunguard.services`)

- `AuthenticationService.java`: Authentication dan session management
- `RecommendationService.java`: Business logic untuk recommendations

### 4. Models (`com.vunguard.models`)

- `User.java`: User entity model
- `Recommendation.java`: Recommendation entity model
- `Product.java`, `Portfolio.java`, `Transaction.java`: Other entity models

## Default Users

Schema akan membuat users default:

| Username | Password | Role | Email |
|----------|----------|------|-------|
| admin | password | admin | admin@vunguard.com |
| budi_analyst | password | analyst | budi@vunguard.com |
| sarah_manager | password | manager | sarah@vunguard.com |

## API dan Database Operations

### Authentication
```java
// Login
AuthenticationService authService = AuthenticationService.getInstance();
String sessionToken = authService.login("username", "password");

// Check permissions
boolean canApprove = authService.canApproveRecommendations();
```

### Recommendations
```java
// Get recommendations
RecommendationService recService = RecommendationService.getInstance();
List<Recommendation> recommendations = recService.getAllRecommendations();

// Create recommendation
boolean created = recService.createRecommendation(
    "Product Name", "BUY", 100.0, 95.0, 4, 
    "Long Term", "Analysis...", "Technical...", 
    "Fundamental...", "Risks..."
);
```

## Troubleshooting

### 1. Database Connection Issues

```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Check if database exists
psql -U postgres -l

# Test connection
psql -U postgres -d vunguard_db -c "SELECT version();"
```

### 2. Dependency Issues

```bash
# Clean and rebuild
./gradlew clean
./gradlew build --refresh-dependencies

# Check dependency tree
./gradlew dependencies
```

### 3. Common Errors

**Error: "relation does not exist"**
- Solution: Run schema.sql script

**Error: "password authentication failed"**
- Solution: Check PostgreSQL user credentials

**Error: "connection refused"**
- Solution: Check if PostgreSQL service is running

**Error: "HikariCP connection failed"**
- Solution: Check DATABASE_URL environment variable

## Development Tips

1. **Database Changes**: Update `schema.sql` dan restart aplikasi
2. **Configuration**: Gunakan environment variables untuk production
3. **Logging**: Check logs di `logs/vunguard.log` jika file logging enabled
4. **Testing**: Gunakan in-memory H2 database untuk unit tests
5. **Security**: Change default JWT secret dan passwords di production

## Production Deployment

1. **Database**: Gunakan managed PostgreSQL service (AWS RDS, Google Cloud SQL, dll)
2. **Environment**: Set proper environment variables
3. **Security**: Enable SSL/TLS connections
4. **Monitoring**: Add application monitoring dan logging
5. **Backup**: Setup database backup schedule

## Extensions

Backend ini dapat diperluas dengan:

- **REST API**: Tambah Spring Boot untuk REST endpoints
- **Real-time Data**: WebSocket untuk live price updates
- **Caching**: Redis untuk performance improvement
- **Message Queue**: RabbitMQ untuk async processing
- **Security**: OAuth2/JWT enhancement
- **Monitoring**: Metrics dan health checks 