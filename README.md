# Online Job Portal

A secure, role-based REST API for a job recruitment platform built with Spring Boot 3 and **stateless JWT authentication**. Supports three user tiers — Job Seekers, Employers, and Administrators — with granular access control, automated admin seeding, and a bundled vanilla frontend served out-of-the-box.

**Author:** Nikshay Kumar Singh

---

## What's New: JWT Authentication

This version replaces the previous session-based authentication with a modern **stateless JWT (JSON Web Token) implementation**:

- ✅ **Stateless Architecture** — No server-side session storage; every request carries a self-contained token.
- ✅ **Custom JWT Filter** — `JwtAuthFilter` validates tokens on every request and binds user roles into Spring Security context.
- ✅ **BCrypt Password Hashing** — All passwords hashed with `BCryptPasswordEncoder`; never stored in plaintext.
- ✅ **Granular Role-Based Access** — `SecurityConfig` enforces distinct endpoint access patterns per role (`ROLE_SEEKER`, `ROLE_EMPLOYER`, `ROLE_ADMIN`).
- ✅ **Automated Admin Seeding** — Default admin account created on startup via `DataInitializer`; no manual SQL insertion.

---

## Tech Stack

| Layer | Technology |
|:---|:---|
| Language | Java 17+ |
| Framework | Spring Boot 3.2 (Spring Web, Spring Security, Spring Data JPA) |
| Database | MySQL 8.0 |
| Authentication | Stateless JWT with `BCryptPasswordEncoder` |
| Build Tool | Maven 3.9+ |
| Utilities | Lombok, ModelMapper |

---

## Core Features

- **Stateless JWT Authentication** — Custom `JwtAuthFilter` intercepts requests, validates tokens via `JwtTokenProvider`, and binds role-based authorities into the Spring Security context.
- **Role-Based Filter Chain** — `SecurityConfig` enforces distinct endpoint access patterns per role, rejecting unauthorized requests at the framework level.
- **BCrypt Password Hashing** — All passwords hashed on registration and verified on login. No plaintext storage.
- **Automated Admin Seeding** — `DataInitializer` (using `CommandLineRunner`) creates the default administrator account on startup.
- **Service-Layer Ownership Guardrails** — Employers can only view applicants and update statuses on job listings they explicitly own. Server-side validation prevents cross-employer tampering.
- **Duplicate Application Prevention** — Unique constraint per seeker-job pair; returns conflict response on repeat submissions.
- **Cascade Deletion via JPA** — Job deletions safely cascade to associated application records through Hibernate entity graph management.
- **Global Exception Handling** — Centralized `@ControllerAdvice` handler formats all application errors into consistent JSON error responses.
- **Input Validation** — `ValidationUtils` and DTO-layer constraints sanitize incoming requests before reaching the service layer.
- **Resume Upload Support** — Job seekers upload resumes (PDF/DOCX, max 5 MB); employers download directly.
- **Bundled Frontend** — Vanilla HTML/CSS/JS UI lives in `src/main/resources/static/` and is served directly by Spring Boot at `http://localhost:8080`.

---

## Project Structure

```
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

### 2. Configuration

```bash
cd backend/src/main/resources
cp application.properties.example application.properties
```

Update `application.properties` with your MySQL credentials and JWT secret:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/job_portal
spring.datasource.username=<your_username>
spring.datasource.password=<your_password>
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
jwt.secret=<your_secure_secret_key>
jwt.expiration=86400000
```

### 3. Build and Run

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The API (and bundled frontend) will be available at **`http://localhost:8080`**.

> The default admin account is created automatically on first startup via `DataInitializer`. No manual SQL insertion is required.

---

## API Reference

### Authentication

| Method | Endpoint | Access | Description |
|:---|:---|:---|:---|
| POST | `/auth/signup` | Public | Register a new Seeker or Employer account |
| POST | `/auth/login` | Public | Authenticate and return a JWT |
| GET | `/auth/me` | Authenticated | Retrieve current user profile from token |

### Jobs

| Method | Endpoint | Access | Description |
|:---|:---|:---|:---|
| GET | `/jobs` | Public | List all job postings (supports `title`, `location`, `type` filters) |
| GET | `/jobs/{id}` | Public | Get details of a specific job |
| POST | `/jobs` | Employer | Create a new job listing |
| DELETE | `/jobs/{id}` | Employer / Admin | Delete a job listing |

### Applications

| Method | Endpoint | Access | Description |
|:---|:---|:---|:---|
| POST | `/applications` | Seeker | Apply to a job listing |
| GET | `/applications` | Seeker / Employer | Fetch applications (filtered by role and ownership) |
| POST | `/applications/update-status` | Employer | Update an applicant's status (Shortlisted / Rejected) |

### Resumes

| Method | Endpoint | Access | Description |
|:---|:---|:---|:---|
| POST | `/upload-resume` | Seeker | Upload a resume (PDF/DOCX, max 5 MB) |
| GET | `/my-resume` | Seeker | Check resume upload status |
| GET | `/resume/{seekerId}` | Employer | Download a seeker's resume |

### Administration

| Method | Endpoint | Access | Description |
|:---|:---|:---|:---|
| GET | `/admin/users` | Admin | List all registered users |
| DELETE | `/admin/jobs/{id}` | Admin | Moderate and remove any job listing |

---

## JWT Authentication Flow

```
Client                          Server
  │                               │
  │  POST /auth/signup            │
  │  { email, password, role }    │
  │──────────────────────────────>│
  │                               │  BCrypt hash password
  │                               │  Persist user with Role enum
  │  201 Created                  │
  │<──────────────────────────────│
  │                               │
  │  POST /auth/login             │
  │  { email, password }          │
  │──────────────────────────────>│
  │                               │  Verify BCrypt match
  │                               │  Generate JWT (payload: id, email, role)
  │  { token: "eyJ..." }          │
  │<──────────────────────────────│
  │                               │
  │  GET /applications            │
  │  Authorization: Bearer eyJ... │
  │──────────────────────────────>│
  │                               │  JwtAuthFilter validates token
  │                               │  Sets SecurityContext with ROLE_SEEKER
  │                               │  Controller returns filtered data
  │  200 OK                       │
  │<──────────────────────────────│
```

---

## Default Credentials (Auto-Seeded)

| Role | Username / Email    | Password  |
|:---|:--------------------|:----------|
| **Administrator** | admin@jobportal.com | Admin@123 |
| **Employer** | employer@portal.com | employer123 |  
| **Job Seeker** | seeker@portal.com   | seeker123 |

---

## Development Notes

### Security Highlights

1. **JWT Validation** — Every request is validated via `JwtAuthFilter` before reaching controllers.
2. **BCrypt Hashing** — Passwords are never stored in plaintext.
3. **Role-Based Authorization** — Spring Security enforces role-based access at the filter chain level.
4. **CORS Configuration** — Configured to allow frontend requests while restricting unauthorized domains.
5. **Input Sanitization** — `ValidationUtils` validates emails, passwords, and job data before persistence.

### Testing

Run unit tests:
```bash
cd backend
mvn test
```

Existing test suites cover:
- `UserTest.java` — User entity and password hashing validation
- `ApplicationTest.java` — Application model constraints
- `ValidationUtilsTest.java` — Email and input validation logic

---

## Future Enhancements

- Pagination for job listings and applications
- Advanced search filters (salary range, experience level, skills)
- Email notifications for application status updates
- Refresh token implementation for extended sessions
- Two-factor authentication for enhanced security
- Admin analytics dashboard

---

## License

This project is developed as part of the BCA curriculum and is intended for educational purposes.

---

## Support

For questions or issues, please refer to the accompanying documentation files:
- `codebase_overview.md` — Detailed architecture explanation
- `codebase_clarifications.md` — Common implementation notes
