# Online Job Portal

A secure, role-based REST API for a job recruitment platform built with Spring Boot 3 and stateless JWT authentication. Supports three user tiers — Job Seekers, Employers, and Administrators — with granular access control, automated admin seeding, and a bundled vanilla frontend served out-of-the-box.

**Author:** Nikshay Kumar Singh

---

## Core Functionality

- **Stateless JWT Authentication** — Custom `JwtAuthFilter` intercepts requests, validates tokens via `JwtTokenProvider`, and binds `ROLE_SEEKER`, `ROLE_EMPLOYER`, or `ROLE_ADMIN` authorities into the Spring Security context.
- **Role-Based Filter Chain** — `SecurityConfig` enforces distinct endpoint access patterns per role, rejecting unauthorized requests at the framework level before they reach any controller.
- **BCrypt Password Hashing** — All passwords are hashed on registration and verified on login using a configured `BCryptPasswordEncoder` bean. No plaintext storage.
- **Automated Admin Seeding** — A `DataInitializer` component (using `CommandLineRunner`) creates the default administrator account on application startup, eliminating manual database setup.
- **Service-Layer Ownership Guardrails** — Employers can only view applicants and update statuses on job listings they explicitly own. Server-side validation rejects cross-employer tampering.
- **Duplicate Application Prevention** — The application flow enforces a unique constraint per seeker-job pair, returning a clear conflict response on repeat submissions.
- **Cascade Deletion via JPA** — Job deletions safely cascade to associated application records through Hibernate entity graph management, preventing foreign key constraint failures.
- **Global Exception Handling** — A centralized `@ControllerAdvice` handler catches and formats all application errors into consistent JSON error responses.
- **Input Validation** — `ValidationUtils` and DTO-layer constraints sanitize incoming requests before they reach the service layer.
- **Bundled Frontend** — A vanilla HTML/CSS/JS UI lives in `src/main/resources/static/` and is served directly by Spring Boot at `http://localhost:8080` with no separate build step or dev server required.

---

## Tech Stack

| Layer | Technology |
|:---|:---|
| Language | Java 21 |
| Framework | Spring Boot 3.2 (Spring Web, Spring Security, Spring Data JPA) |
| Database | MySQL 8.0 |
| Authentication | Stateless JWT with `BCryptPasswordEncoder` |
| Build Tool | Maven 3.9+ |
| Utilities | Lombok, ModelMapper |

---

## Project Structure

```text
job-portal/
└── backend/                   # Main Application Root
    ├── src/main/java/         # Backend Source (API & Logic)
    └── src/main/resources/
        ├── static/            # Integrated Frontend (HTML, CSS, JS)
        └── application.properties
```

---

## Getting Started

### Prerequisites

- JDK 17 or higher
- Maven 3.9+
- Running MySQL instance

### 1. Database Setup

```sql
CREATE DATABASE job_portal;
```

### 2. Environment Configuration
Navigate to the backend configuration directory:
```bash
cd backend/src/main/resources
cp application.properties.example application.properties
# Configure your MySQL credentials in application.properties
```

### 3. Execution
**Starting the Backend:**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
*The API (and bundled frontend) will be available at http://localhost:8080.*

**Launching the Frontend:**
Open `frontend/index.html` using a local web server (e.g., VS Code Live Server).
*Default URL: `http://127.0.0.1:5500`*

*The default admin account is created automatically on first startup via DataInitializer. No manual SQL insertion is required.*

---

## Access Credentials

| Role | Username / Email    | Password  |
|:---|:--------------------|:----------|
| **Administrator** | admin@jobportal.com | Admin@123 |
| **Employer** | employer@portal.com | employer123 |  
| **Job Seeker** | seeker@portal.com   | seeker123 |

---

## API Documentation

## *Authentication*

| Method | Endpoint | Access | Purpose |
|:---|:---|:---|:---|
| POST | `/register` | Public | User Account Creation |
| POST | `/login` | Public | Authentication |
| GET | `/jobs` | Public | Browse Listings |
| POST | `/jobs/add` | Employer | Create Job Posting |
| POST | `/apply` | Seeker | Submit Application |
| GET | `/my-applications` | Seeker | View Personal History |
| GET | `/job-applicants/{id}`| Employer | Review Applications |
| POST | `/update-status` | Employer | Selection Management |
| GET | `/admin/users` | Admin | User Auditing |
| DELETE | `/admin/jobs/{id}` | Admin | Content Moderation |
