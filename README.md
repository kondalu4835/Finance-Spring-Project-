# Finance Dashboard Backend — Spring Boot

A complete REST API backend for a finance dashboard with JWT authentication, role-based access control, and analytics APIs, built with **Spring Boot 3**, **Spring Security**, **Spring Data JPA**, and **H2 Database**.

---

## Tech Stack

| Layer         | Technology                    |
|---------------|-------------------------------|
| Framework     | Spring Boot 3.2.3             |
| Language      | Java 17                       |
| Security      | Spring Security + JWT (jjwt)  |
| Database      | H2 (file-based, zero install) |
| ORM           | Spring Data JPA / Hibernate   |
| Validation    | Jakarta Bean Validation       |
| API Docs      | SpringDoc OpenAPI (Swagger)   |
| Build Tool    | Maven                         |
| Testing       | JUnit 5 + MockMvc             |

---

## Prerequisites

- **Java 17+** — download from https://adoptium.net
- **Maven 3.8+** — download from https://maven.apache.org OR use the included wrapper

Verify:
```bash
java -version    # must show 17 or higher
mvn -version     # must show 3.8 or higher
```

---

## All Commands

### 1. Run the application
```bash
# Option A: Using Maven (recommended)
mvn spring-boot:run

# Option B: Build JAR first, then run
mvn clean package -DskipTests
java -jar target/finance-backend-1.0.0.jar
```

Server starts at: **http://localhost:8080**

Sample data is automatically seeded on first startup:

| Email                  | Password       | Role     |
|------------------------|----------------|----------|
| admin@finance.dev      | Admin@1234     | ADMIN    |
| analyst@finance.dev    | Analyst@1234   | ANALYST  |
| viewer@finance.dev     | Viewer@1234    | VIEWER   |

### 2. Run tests
```bash
mvn test
```

### 3. Build production JAR
```bash
mvn clean package -DskipTests
# JAR is at: target/finance-backend-1.0.0.jar
```

---

## Git Setup & Push Commands

```bash
# Step 1: Initialize git in the project folder
git init

# Step 2: Create .gitignore (already included)
# Step 3: Add all files
git add .

# Step 4: First commit
git commit -m "Initial commit: Finance Dashboard Backend (Spring Boot)"

# Step 5: Create a new repo on GitHub (go to github.com → New Repository)
# Name it: finance-backend  (keep it public, no README/gitignore)

# Step 6: Link and push
git remote add origin https://github.com/YOUR_USERNAME/finance-backend.git
git branch -M main
git push -u origin main
```

---

## API Documentation (Swagger UI)

After starting the server, visit:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **H2 Console**: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./financedb`)

---

## Roles & Permissions

| Action                          | VIEWER | ANALYST | ADMIN |
|---------------------------------|:------:|:-------:|:-----:|
| Register / Login                | ✅     | ✅      | ✅    |
| View own profile (/me)          | ✅     | ✅      | ✅    |
| View financial records          | ✅     | ✅      | ✅    |
| View dashboard & analytics      | ✅     | ✅      | ✅    |
| Create records                  | ❌     | ❌      | ✅    |
| Update records                  | ❌     | ❌      | ✅    |
| Delete records (soft)           | ❌     | ❌      | ✅    |
| List all users                  | ❌     | ❌      | ✅    |
| Create users with any role      | ❌     | ❌      | ✅    |
| Update user role/status         | ❌     | ❌      | ✅    |

---

## API Reference

All protected endpoints require:
```
Authorization: Bearer <jwt-token>
```

### Auth

| Method | Endpoint             | Auth | Description                          |
|--------|----------------------|------|--------------------------------------|
| POST   | /api/auth/register   | ❌   | Register (always gets VIEWER role)   |
| POST   | /api/auth/login      | ❌   | Login, returns JWT token             |
| GET    | /api/auth/me         | ✅   | Get current user profile             |

**Login request:**
```json
{ "email": "admin@finance.dev", "password": "Admin@1234" }
```
**Login response:**
```json
{
  "success": true,
  "data": {
    "token": "<jwt>",
    "type": "Bearer",
    "user": { "id": 1, "name": "Alice Admin", "role": "ADMIN", ... }
  }
}
```

### Financial Records

| Method | Endpoint          | Role  | Description                          |
|--------|-------------------|-------|--------------------------------------|
| GET    | /api/records      | Any   | List with filters + pagination       |
| GET    | /api/records/{id} | Any   | Get single record                    |
| POST   | /api/records      | ADMIN | Create record                        |
| PATCH  | /api/records/{id} | ADMIN | Partial update                       |
| DELETE | /api/records/{id} | ADMIN | Soft delete                          |

**Query params for GET /api/records:**
- `type` — INCOME or EXPENSE
- `category` — filter by category (case-insensitive)
- `dateFrom` — YYYY-MM-DD
- `dateTo` — YYYY-MM-DD
- `page` — page number (default 0)
- `size` — page size (default 20)

**Create record body:**
```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2024-06-01",
  "notes": "June salary"
}
```

### Dashboard

| Method | Endpoint                        | Description                     |
|--------|---------------------------------|---------------------------------|
| GET    | /api/dashboard/summary          | Total income, expenses, balance |
| GET    | /api/dashboard/categories       | Category-wise breakdown         |
| GET    | /api/dashboard/trends/monthly   | Monthly trends (income/expense) |
| GET    | /api/dashboard/trends/weekly    | Weekly trends (last N weeks)    |
| GET    | /api/dashboard/recent           | Most recent records             |

**Summary response:**
```json
{
  "success": true,
  "data": {
    "totalIncome": 18500.00,
    "totalExpenses": 1920.00,
    "netBalance": 16580.00,
    "recordCount": 10
  }
}
```

### Users (Admin only)

| Method | Endpoint        | Description                    |
|--------|-----------------|--------------------------------|
| GET    | /api/users      | List users (paginated)         |
| POST   | /api/users      | Create user with specific role |
| GET    | /api/users/{id} | Get user by ID                 |
| PATCH  | /api/users/{id} | Update name, role, or status   |

### Error Response Format

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "amount": "Amount must be greater than 0",
    "date": "Date is required"
  }
}
```

| Status | Meaning                              |
|--------|--------------------------------------|
| 401    | Missing or invalid JWT               |
| 403    | Insufficient role                    |
| 404    | Resource not found                   |
| 409    | Conflict (e.g. duplicate email)      |
| 422    | Validation failed                    |
| 500    | Internal server error                |

---

## Project Structure

```
src/
└── main/
    ├── java/com/finance/
    │   ├── FinanceApplication.java       # Entry point
    │   ├── config/
    │   │   ├── SecurityConfig.java       # Spring Security + JWT filter chain
    │   │   ├── DataSeeder.java           # Auto-seeds sample data on startup
    │   │   └── OpenApiConfig.java        # Swagger/OpenAPI configuration
    │   ├── controller/
    │   │   ├── AuthController.java       # /api/auth/*
    │   │   ├── UserController.java       # /api/users/*
    │   │   ├── FinancialRecordController # /api/records/*
    │   │   ├── DashboardController.java  # /api/dashboard/*
    │   │   └── HealthController.java     # /health
    │   ├── dto/                          # Request/Response data classes
    │   ├── entity/
    │   │   ├── User.java                 # User entity (implements UserDetails)
    │   │   ├── FinancialRecord.java      # Financial record entity
    │   │   ├── Role.java                 # VIEWER / ANALYST / ADMIN
    │   │   └── RecordType.java           # INCOME / EXPENSE
    │   ├── exception/
    │   │   ├── GlobalExceptionHandler.java
    │   │   └── ResourceNotFoundException.java
    │   ├── repository/
    │   │   ├── UserRepository.java
    │   │   └── FinancialRecordRepository.java  # Custom JPQL queries
    │   ├── security/
    │   │   ├── JwtService.java           # Token generation + validation
    │   │   └── JwtAuthFilter.java        # Per-request JWT processing
    │   └── service/
    │       ├── AuthService.java
    │       ├── UserService.java
    │       ├── FinancialRecordService.java
    │       └── DashboardService.java
    └── resources/
        ├── application.properties        # Main config
        └── application-test.properties   # Test config (in-memory DB)
```

---

## Design Decisions

1. **H2 file-based database** — Zero installation, works everywhere, data persists in `financedb.mv.db`. Can be swapped to PostgreSQL/MySQL by changing one line in `application.properties`.

2. **Soft deletes** — Financial records set `deletedAt` timestamp instead of being removed. All queries filter `deletedAt IS NULL`.

3. **Spring Security method-level security** — `@PreAuthorize("hasRole('ADMIN')")` on service methods provides defence in depth alongside URL-level rules.

4. **JWT stateless auth** — 8-hour expiry, no sessions or refresh tokens (intentionally simple for assessment).

5. **Consistent API envelope** — Every response uses `ApiResponse<T>` with `success`, `message`, `data`, and `timestamp` fields.

6. **Paginated responses** — All list endpoints return `content`, `page`, `size`, `totalElements`, `totalPages`.

7. **Test profile** — `@ActiveProfiles("test")` switches to in-memory H2 and skips the prod data seeder. Tests are fully isolated.
