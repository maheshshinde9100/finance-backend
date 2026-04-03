# Financial Records Management System

A RESTful API application for managing personal financial records with secure authentication, built using Spring Boot and MongoDB.

## Overview

This application provides a complete backend solution for tracking income and expenses with user authentication, role-based access control, and comprehensive financial analytics. It includes a simple web interface for testing and demonstration purposes.

## Technology Stack

- Spring Boot 4.0.5
- Spring Security with JWT authentication
- MongoDB (document database)
- Jakarta Validation for input validation
- Lombok for reducing boilerplate code
- Maven for dependency management
- Java 17

## Features

### Authentication and Authorization
- User registration with email validation
- JWT-based authentication
- Role-based access control (USER and ADMIN roles)
- BCrypt password encryption
- Secure token-based session management

### Financial Record Management
- Create, read, update, and delete financial records
- Support for income and expense tracking
- Category-based organization
- Date-based record management
- User-specific data isolation

### Analytics and Reporting
- Dashboard with financial summary
- Total income and expense calculation
- Balance computation
- Category-wise breakdown
- Monthly trend analysis
- Flexible date range filtering

### Admin Features
- User management (view, delete, activate/deactivate)
- System-wide user administration
- User status management

## Project Structure

```
src/main/java/com/mahesh/zorvyn/
├── config/              # Security and MongoDB configuration
├── controller/          # REST API endpoints
├── dto/                 # Data Transfer Objects
├── exception/           # Global exception handling
├── model/               # Domain models
├── repository/          # Data access layer
├── security/            # JWT and authentication components
└── service/             # Business logic layer
```

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MongoDB database (local or MongoDB Atlas)

## Installation and Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd zorvyn
```

### 2. Configure Database

Create the application configuration file from the example:

```bash
# Windows (PowerShell)
Copy-Item src/main/resources/application.yaml.example src/main/resources/application.yaml

# Linux/Mac
cp src/main/resources/application.yaml.example src/main/resources/application.yaml
```

Edit `src/main/resources/application.yaml` and configure your MongoDB connection:

```yaml
spring:
  application:
    name: zorvyn
  data:
    mongodb:
      uri: mongodb://localhost:27017/finance-backend-db

server:
  port: 8081

jwt:
  secret: your-secret-key-here-minimum-256-bits
  expiration: 86400000
```

#### MongoDB Configuration Options

**Option 1: Local MongoDB**
```yaml
uri: mongodb://localhost:27017/finance-backend-db
```

**Option 2: MongoDB Atlas (Cloud)**
```yaml
uri: mongodb+srv://username:password@cluster.mongodb.net/finance-backend-db
```

To use MongoDB Atlas:
1. Create a free account at [MongoDB Atlas](https://cloud.mongodb.com/)
2. Create a cluster
3. Create a database user with read/write permissions
4. Whitelist your IP address (or use 0.0.0.0/0 for development)
5. Get your connection string and replace username, password, and cluster name

#### JWT Secret Configuration

Generate a secure JWT secret (minimum 256 bits):

```bash
# Using OpenSSL (Linux/Mac/Git Bash)
openssl rand -base64 64

# Using PowerShell (Windows)
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

Replace `your-secret-key-here-minimum-256-bits` with the generated value.

### 3. Build the Project

```bash
mvn clean install
```

This will:
- Download all dependencies
- Compile the source code
- Run tests
- Package the application

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

You should see output similar to:
```
Started ZorvynApplication in X.XXX seconds
```

### 5. Verify Installation

Open your browser and navigate to:
```
http://localhost:8081
```

You should see the web interface for the Financial Records Management System.

## API Documentation

Complete API documentation with request/response examples is available in [API_DOCUMENTATION.md](API_DOCUMENTATION.md).

### Quick Reference

**Authentication**
- POST `/api/auth/signup` - Register new user
- POST `/api/auth/login` - Login and receive JWT token

**Financial Records**
- POST `/api/records` - Create record
- GET `/api/records` - Get all records
- GET `/api/records/{id}` - Get specific record
- PUT `/api/records/{id}` - Update record
- DELETE `/api/records/{id}` - Delete record
- GET `/api/records/filter` - Filter records
- GET `/api/records/dashboard` - Get dashboard summary

**Admin**
- GET `/api/admin/users` - List all users
- GET `/api/admin/users/{id}` - Get user details
- DELETE `/api/admin/users/{id}` - Delete user
- PATCH `/api/admin/users/{id}/status` - Update user status

## Testing

### Using the Web Interface

The application includes a complete web interface for testing all APIs.

1. Start the application:
   ```bash
   mvn spring-boot:run
   ```

2. Open your browser and navigate to:
   ```
   http://localhost:8081
   ```

3. You will see the login page. You can either:
   - Register a new account using the "Sign Up" tab
   - Login with existing credentials:
     - Username: `maheshshinde`
     - Password: `mahesh@123`

4. Once logged in, you can access:
   - **Dashboard**: View financial summary with total income, expenses, balance, and recent transactions
   - **Records**: View all financial records with filtering options (by type, category, date range)
   - **Add Record**: Create new income or expense entries
   - **Analytics**: View detailed breakdowns by category and monthly trends

5. Features available in the web interface:
   - User registration and authentication
   - Add new financial records (income/expense)
   - View all records with edit and delete options
   - Filter records by type, category, and date range
   - Dashboard with financial statistics
   - Analytics with category-wise breakdown
   - Monthly trend analysis
   - Responsive design for mobile and desktop

### Using API Test Scripts

PowerShell script for Windows:
```bash
.\test-api.ps1
```

Node.js script (cross-platform):
```bash
node test-api.js
```

Bash script for Linux/Mac:
```bash
bash test-api.sh
```

### Using cURL

Register a user:
```bash
curl -X POST http://localhost:8081/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"maheshshinde","email":"mahesh@gmail.com","password":"mahesh@123"}'
```

Login:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"maheshshinde","password":"mahesh@123"}'
```

Create a financial record (replace TOKEN with your JWT):
```bash
curl -X POST http://localhost:8081/api/records \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"type":"INCOME","category":"Salary","amount":5000,"description":"Monthly salary","date":"2026-04-01"}'
```

## Security

### Configuration Security

The following files contain sensitive information and are excluded from version control:

- `src/main/resources/application.yaml` - Contains MongoDB URI and JWT secret
- `src/test/resources/application.yaml` - Test configuration
- `.env` - Environment variables (if used)

These files are listed in `.gitignore` and will not be committed to the repository.

### Security Best Practices

1. **JWT Secret**: Use a strong, randomly generated secret of at least 256 bits in production
2. **MongoDB Credentials**: Never commit database credentials to version control
3. **HTTPS**: Always use HTTPS in production environments
4. **CORS**: Restrict allowed origins in production (currently allows all for development)
5. **Password Policy**: Passwords must be 6-40 characters (consider stronger requirements for production)
6. **Token Expiration**: JWT tokens expire after 24 hours by default
7. **IP Whitelisting**: Use MongoDB Atlas IP whitelist in production
8. **Environment Variables**: Use environment variables for sensitive configuration in production

### Production Configuration

For production deployments, use environment variables instead of hardcoded values:

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
server:
  port: ${SERVER_PORT:8080}
jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:86400000}
```

Set environment variables:

**Linux/Mac:**
```bash
export MONGODB_URI="mongodb+srv://username:password@cluster.mongodb.net/finance-backend-db"
export JWT_SECRET="your-production-secret"
export SERVER_PORT="8080"
```

**Windows (PowerShell):**
```powershell
$env:MONGODB_URI="mongodb+srv://username:password@cluster.mongodb.net/finance-backend-db"
$env:JWT_SECRET="your-production-secret"
$env:SERVER_PORT="8080"
```

### Security Features Implemented

- BCrypt password hashing with salt
- JWT-based stateless authentication
- Role-based access control (RBAC)
- Method-level security with `@PreAuthorize`
- User-specific data isolation
- Input validation on all endpoints
- Global exception handling
- Secure password storage (never stored in plain text)

## Architecture

### Layered Architecture

The application follows a standard layered architecture:

1. **Controller Layer**: Handles HTTP requests and responses
2. **Service Layer**: Contains business logic
3. **Repository Layer**: Manages data persistence
4. **Security Layer**: Handles authentication and authorization

### Key Design Patterns

- **DTO Pattern**: Separates API contracts from domain models
- **Repository Pattern**: Abstracts data access logic
- **Dependency Injection**: Manages component dependencies
- **Global Exception Handling**: Centralizes error handling

### Security Implementation

- JWT tokens for stateless authentication
- BCrypt for password hashing
- Method-level security with `@PreAuthorize`
- Custom authentication filter for token validation
- User-specific data access enforcement

## Development

### Running Tests

```bash
mvn test
```

### Building for Production

```bash
mvn clean package
java -jar target/zorvyn-0.0.1-SNAPSHOT.jar
```

## Troubleshooting

### MongoDB Connection Issues

- Verify MongoDB is running (local) or accessible (Atlas)
- Check connection string format
- Ensure IP whitelist is configured (Atlas)
- Verify network connectivity

### Port Already in Use

Change the port in `application.yaml`:
```yaml
server:
  port: 8082
```

### JWT Token Errors

- Ensure JWT secret is properly configured
- Check token expiration settings
- Verify Authorization header format: `Bearer <token>`

## License

This project is created for educational and assessment purposes.
