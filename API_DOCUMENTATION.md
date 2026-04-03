# API Documentation

Complete REST API reference for the Financial Records Management System.

## Base URL

```
http://localhost:8081/api
```

## Authentication

All endpoints except authentication endpoints require a valid JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

Tokens are obtained by logging in through the `/api/auth/login` endpoint and are valid for 24 hours by default.

## Authentication Endpoints

### Register New User

Creates a new user account in the system.

**Endpoint:** `POST /api/auth/signup`

**Authentication:** Not required

**Request Body:**
```json
{
  "username": "maheshshinde",
  "email": "mahesh@gmail.com",
  "password": "mahesh@123",
  "roles": ["user"]
}
```

**Field Validation:**
- `username`: Required, 3-20 characters
- `email`: Required, valid email format
- `password`: Required, 6-40 characters
- `roles`: Optional, defaults to ["user"]

**Success Response (200 OK):**
```json
{
  "message": "User registered successfully!"
}
```

**Error Response (400 Bad Request):**
```json
{
  "username": "Username must be between 3 and 20 characters",
  "email": "Email should be valid"
}
```

### User Login

Authenticates a user and returns a JWT token for subsequent requests.

**Endpoint:** `POST /api/auth/login`

**Authentication:** Not required

**Request Body:**
```json
{
  "username": "maheshshinde",
  "password": "mahesh@123"
}
```

**Field Validation:**
- `username`: Required, non-blank
- `password`: Required, non-blank

**Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJtYWhlc2hzaGluZGUiLCJpYXQiOjE2MTYyMzkwMjJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
  "type": "Bearer",
  "id": "507f1f77bcf86cd799439011",
  "username": "maheshshinde",
  "email": "mahesh@gmail.com",
  "roles": ["ROLE_USER"]
}
```

**Error Response (401 Unauthorized):**
```json
{
  "error": "Bad credentials"
}
```

## Financial Records Endpoints

### Create Financial Record

Creates a new income or expense record for the authenticated user.

**Endpoint:** `POST /api/records`

**Authentication:** Required (USER or ADMIN role)

**Request Body:**
```json
{
  "type": "INCOME",
  "category": "Salary",
  "amount": 5000.00,
  "description": "Monthly salary payment",
  "date": "2026-04-01"
}
```

**Field Validation:**
- `type`: Required, must be "INCOME" or "EXPENSE"
- `category`: Required, non-blank string
- `amount`: Required, must be positive number
- `description`: Optional
- `date`: Required, ISO date format (YYYY-MM-DD)

**Success Response (201 Created):**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "userId": "507f191e810c19729de860ea",
  "type": "INCOME",
  "category": "Salary",
  "amount": 5000.00,
  "description": "Monthly salary payment",
  "date": "2026-04-01",
  "createdAt": "2026-04-03T10:30:00",
  "updatedAt": "2026-04-03T10:30:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "type": "Type is required",
  "amount": "Amount must be positive"
}
```

### Get All Records

Retrieves all financial records for the authenticated user.

**Endpoint:** `GET /api/records`

**Authentication:** Required (USER or ADMIN role)

**Success Response (200 OK):**
```json
[
  {
    "id": "507f1f77bcf86cd799439011",
    "userId": "507f191e810c19729de860ea",
    "type": "INCOME",
    "category": "Salary",
    "amount": 5000.00,
    "description": "Monthly salary payment",
    "date": "2026-04-01",
    "createdAt": "2026-04-03T10:30:00",
    "updatedAt": "2026-04-03T10:30:00"
  },
  {
    "id": "507f1f77bcf86cd799439012",
    "userId": "507f191e810c19729de860ea",
    "type": "EXPENSE",
    "category": "Food",
    "amount": 150.00,
    "description": "Grocery shopping",
    "date": "2026-04-02",
    "createdAt": "2026-04-03T11:00:00",
    "updatedAt": "2026-04-03T11:00:00"
  }
]
```

### Get Record by ID

Retrieves a specific financial record. Users can only access their own records.

**Endpoint:** `GET /api/records/{id}`

**Authentication:** Required (USER or ADMIN role)

**Path Parameters:**
- `id`: MongoDB ObjectId of the record

**Success Response (200 OK):**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "userId": "507f191e810c19729de860ea",
  "type": "INCOME",
  "category": "Salary",
  "amount": 5000.00,
  "description": "Monthly salary payment",
  "date": "2026-04-01",
  "createdAt": "2026-04-03T10:30:00",
  "updatedAt": "2026-04-03T10:30:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "Record not found"
}
```

### Update Record

Updates an existing financial record. Users can only update their own records.

**Endpoint:** `PUT /api/records/{id}`

**Authentication:** Required (USER or ADMIN role)

**Path Parameters:**
- `id`: MongoDB ObjectId of the record

**Request Body:**
```json
{
  "type": "INCOME",
  "category": "Salary",
  "amount": 5500.00,
  "description": "Monthly salary with bonus",
  "date": "2026-04-01"
}
```

**Success Response (200 OK):**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "userId": "507f191e810c19729de860ea",
  "type": "INCOME",
  "category": "Salary",
  "amount": 5500.00,
  "description": "Monthly salary with bonus",
  "date": "2026-04-01",
  "createdAt": "2026-04-03T10:30:00",
  "updatedAt": "2026-04-03T12:00:00"
}
```

### Delete Record

Deletes a financial record. Users can only delete their own records.

**Endpoint:** `DELETE /api/records/{id}`

**Authentication:** Required (USER or ADMIN role)

**Path Parameters:**
- `id`: MongoDB ObjectId of the record

**Success Response (204 No Content)**

No response body is returned on successful deletion.

### Filter Records

Filters financial records based on type, category, and date range. Returns only records belonging to the authenticated user.

**Endpoint:** `GET /api/records/filter`

**Authentication:** Required (USER or ADMIN role)

**Query Parameters:**
- `type` (optional): Filter by record type (INCOME or EXPENSE)
- `category` (optional): Filter by category name
- `startDate` (optional): Filter records from this date (YYYY-MM-DD)
- `endDate` (optional): Filter records until this date (YYYY-MM-DD)

**Example Requests:**
```
GET /api/records/filter?type=EXPENSE
GET /api/records/filter?category=Food
GET /api/records/filter?startDate=2026-04-01&endDate=2026-04-30
GET /api/records/filter?type=EXPENSE&startDate=2026-04-01&endDate=2026-04-30
```

**Success Response (200 OK):**
```json
[
  {
    "id": "507f1f77bcf86cd799439012",
    "userId": "507f191e810c19729de860ea",
    "type": "EXPENSE",
    "category": "Food",
    "amount": 150.00,
    "description": "Grocery shopping",
    "date": "2026-04-02",
    "createdAt": "2026-04-03T11:00:00",
    "updatedAt": "2026-04-03T11:00:00"
  }
]
```

### Get Dashboard Summary

Provides comprehensive financial analytics including totals, category breakdowns, and monthly trends.

**Endpoint:** `GET /api/records/dashboard`

**Authentication:** Required (USER or ADMIN role)

**Query Parameters:**
- `startDate` (optional): Calculate summary from this date (YYYY-MM-DD)
- `endDate` (optional): Calculate summary until this date (YYYY-MM-DD)

**Example Requests:**
```
GET /api/records/dashboard
GET /api/records/dashboard?startDate=2026-01-01&endDate=2026-12-31
```

**Success Response (200 OK):**
```json
{
  "totalIncome": 60000.00,
  "totalExpense": 35000.00,
  "balance": 25000.00,
  "totalRecords": 150,
  "incomeByCategory": {
    "Salary": 50000.00,
    "Freelance": 8000.00,
    "Investment": 2000.00
  },
  "expenseByCategory": {
    "Food": 12000.00,
    "Rent": 18000.00,
    "Transportation": 3000.00,
    "Entertainment": 2000.00
  },
  "monthlyTrend": {
    "2026-01": 2500.00,
    "2026-02": 1800.00,
    "2026-03": 3200.00,
    "2026-04": 2100.00
  }
}
```

**Field Descriptions:**
- `totalIncome`: Sum of all income records
- `totalExpense`: Sum of all expense records
- `balance`: Difference between total income and total expense
- `totalRecords`: Count of all financial records
- `incomeByCategory`: Income amounts grouped by category
- `expenseByCategory`: Expense amounts grouped by category
- `monthlyTrend`: Net balance (income - expense) for each month

## Admin Endpoints

### Get All Users

Retrieves a list of all registered users in the system. Admin access only.

**Endpoint:** `GET /api/admin/users`

**Authentication:** Required (ADMIN role only)

**Success Response (200 OK):**
```json
[
  {
    "id": "507f191e810c19729de860ea",
    "username": "maheshshinde",
    "email": "mahesh@gmail.com",
    "roles": ["ROLE_USER"],
    "active": true,
    "createdAt": "2026-04-01T10:00:00",
    "updatedAt": "2026-04-01T10:00:00"
  },
  {
    "id": "507f191e810c19729de860eb",
    "username": "adminuser",
    "email": "admin@gmail.com",
    "roles": ["ROLE_USER", "ROLE_ADMIN"],
    "active": true,
    "createdAt": "2026-04-02T10:00:00",
    "updatedAt": "2026-04-02T10:00:00"
  }
]
```

**Error Response (403 Forbidden):**
```json
{
  "error": "Access Denied"
}
```

### Get User by ID

Retrieves detailed information about a specific user. Admin access only.

**Endpoint:** `GET /api/admin/users/{id}`

**Authentication:** Required (ADMIN role only)

**Path Parameters:**
- `id`: MongoDB ObjectId of the user

**Success Response (200 OK):**
```json
{
  "id": "507f191e810c19729de860ea",
  "username": "maheshshinde",
  "email": "mahesh@gmail.com",
  "roles": ["ROLE_USER"],
  "active": true,
  "createdAt": "2026-04-01T10:00:00",
  "updatedAt": "2026-04-01T10:00:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "User not found"
}
```

### Delete User

Permanently deletes a user account from the system. Admin access only.

**Endpoint:** `DELETE /api/admin/users/{id}`

**Authentication:** Required (ADMIN role only)

**Path Parameters:**
- `id`: MongoDB ObjectId of the user

**Success Response (204 No Content)**

No response body is returned on successful deletion.

**Error Response (404 Not Found):**
```json
{
  "error": "User not found"
}
```

### Update User Status

Activates or deactivates a user account. Admin access only.

**Endpoint:** `PATCH /api/admin/users/{id}/status`

**Authentication:** Required (ADMIN role only)

**Path Parameters:**
- `id`: MongoDB ObjectId of the user

**Query Parameters:**
- `active`: Boolean value (true or false)

**Example Requests:**
```
PATCH /api/admin/users/507f191e810c19729de860ea/status?active=false
PATCH /api/admin/users/507f191e810c19729de860ea/status?active=true
```

**Success Response (200 OK):**
```json
{
  "id": "507f191e810c19729de860ea",
  "username": "maheshshinde",
  "email": "mahesh@gmail.com",
  "roles": ["ROLE_USER"],
  "active": false,
  "createdAt": "2026-04-01T10:00:00",
  "updatedAt": "2026-04-03T14:00:00"
}
```

## Error Responses

The API uses standard HTTP status codes and returns error details in JSON format.

### Validation Error (400 Bad Request)

Returned when request data fails validation.

```json
{
  "username": "Username must be between 3 and 20 characters",
  "email": "Email should be valid",
  "amount": "Amount must be positive"
}
```

### Authentication Error (401 Unauthorized)

Returned when authentication is required but not provided or invalid.

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/records"
}
```

### Authorization Error (403 Forbidden)

Returned when the authenticated user lacks permission for the requested resource.

```json
{
  "error": "Access Denied"
}
```

### Not Found Error (404 Not Found)

Returned when the requested resource does not exist.

```json
{
  "error": "Record not found"
}
```

### Server Error (500 Internal Server Error)

Returned when an unexpected error occurs on the server.

```json
{
  "error": "An unexpected error occurred",
  "message": "Error details..."
}
```

## Testing Examples

### Using cURL

Register a new user:
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

Get all records:
```bash
curl -X GET http://localhost:8081/api/records \
  -H "Authorization: Bearer TOKEN"
```

Get dashboard summary:
```bash
curl -X GET http://localhost:8081/api/records/dashboard \
  -H "Authorization: Bearer TOKEN"
```

Filter records by type and date:
```bash
curl -X GET "http://localhost:8081/api/records/filter?type=EXPENSE&startDate=2026-04-01&endDate=2026-04-30" \
  -H "Authorization: Bearer TOKEN"
```

Update a record:
```bash
curl -X PUT http://localhost:8081/api/records/507f1f77bcf86cd799439011 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"type":"INCOME","category":"Salary","amount":5500,"description":"Updated salary","date":"2026-04-01"}'
```

Delete a record:
```bash
curl -X DELETE http://localhost:8081/api/records/507f1f77bcf86cd799439011 \
  -H "Authorization: Bearer TOKEN"
```

### Using PowerShell

The project includes a PowerShell test script that tests all endpoints:

```powershell
.\test-api.ps1
```

This script will:
1. Register a new test user
2. Login and obtain JWT token
3. Create a financial record
4. Retrieve all records
5. Get a specific record
6. Update the record
7. Get dashboard summary
8. Delete the record
9. Test unauthorized access

### Using the Web Interface

1. Start the application: `mvn spring-boot:run`
2. Open browser: `http://localhost:8081`
3. Use the web interface to:
   - Register a new account
   - Login with credentials
   - Add financial records
   - View dashboard
   - Filter and manage records

## Important Notes

1. All dates must be in ISO format (YYYY-MM-DD)
2. JWT tokens expire after 24 hours (configurable in application.yaml)
3. Passwords are encrypted using BCrypt before storage
4. All monetary amounts should be positive decimal numbers
5. Users can only access their own financial records
6. Admin users can manage all users but not access user financial records
7. The `type` field accepts only two values: "INCOME" or "EXPENSE"
8. Category names are case-sensitive
9. Record IDs are MongoDB ObjectIds (24-character hexadecimal strings)
