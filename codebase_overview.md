# Codebase Overview — Job Portal Project (JWT Authentication)

This document provides a structured overview of the Online Job Portal project with stateless JWT authentication, helping developers and AI agents understand its architecture, technologies, data models, security components, and component relations.

---

## 1. Project Context & Purpose

* **Context:** A final-year BCA college project with enterprise-grade authentication.
* **Goal:** A comprehensive job board system allowing job seekers to upload resumes and apply for job listings, employers to manage job postings and track applicant status, and administrators to moderate the platform — all secured with stateless JWT tokens.
* **Architecture:** A Spring Boot 3.2 REST API backend with stateless JWT authentication, integrated with a vanilla HTML5/CSS3/JavaScript frontend (stored in static resources). No server-side session storage.

---

## 2. Technical Stack

| Component | Technology |
|:---|:---|
| **Java Version** | Java 17+ |
| **Framework** | Spring Boot 3.2.x (Web, Security, Data JPA) |
| **Authentication** | Stateless JWT with `JwtTokenProvider` and custom `JwtAuthFilter` |
| **Password Hashing** | BCryptPasswordEncoder |
| **Database** | MySQL 8.0 |
| **Build Tool** | Maven 3.9+ |
| **Frontend** | Vanilla HTML5, CSS3 (Custom styles in `style.css`), ES6+ JavaScript with localStorage token management |

**Key Upgrade from Previous Version:** Session-based auth → Stateless JWT auth

---

## 3. Directory Structure

```text
nikshaykr-online-job-portal/
├── README.md                            # Project documentation
├── codebase_overview.md                 # This file
├── codebase_clarifications.md           # Implementation details
└── backend/                             # Spring Boot Root
    ├── pom.xml                          # Maven build script with dependencies
    └── src/main/
        ├── java/com/jobportal/
        │   ├── JobPortalApplication.java         # Entry point
        │   ├── config/
        │   │   ├── AppConfig.java                # General beans (ModelMapper, etc.)
        │   │   ├── CorsConfig.java               # CORS configuration for frontend
        │   │   ├── DataInitializer.java          # Auto-seeds default admin account
        │   │   ├── SecurityConfig.java           # Spring Security with JWT filter chain
        │   │   └── WebConfig.java                # Web-related configurations
        │   ├── controller/
        │   │   ├── AuthController.java           # /auth endpoints (signup, login, profile)
        │   │   ├── JobController.java            # /jobs endpoints (CRUD & search)
        │   │   ├── ApplicationController.java    # /applications endpoints (apply, status)
        │   │   ├── ResumeController.java         # /upload-resume, /my-resume, /resume/{id}
        │   │   └── AdminController.java          # /admin endpoints (user list, moderation)
        │   ├── dto/                              # Request/Response Data Transfer Objects
        │   │   ├── LoginRequestDto.java
        │   │   ├── LoginResponseDto.java
        │   │   ├── SignUpRequestDto.java
        │   │   ├── SignUpResponseDto.java
        │   │   ├── CreateJobRequestDto.java
        │   │   ├── JobResponseDto.java
        │   │   ├── ApplyJobRequestDto.java
        │   │   ├── ApplicationResponseDto.java
        │   │   ├── UpdateStatusRequestDto.java
        │   │   └── UserResponseDto.java
        │   ├── model/                            # Hibernate JPA Entities
        │   │   ├── User.java                     # Users (Seeker, Employer, Admin)
        │   │   ├── Job.java                      # Job listings
        │   │   ├── Application.java              # Job applications
        │   │   └── Role.java                     # Enum for ROLE_SEEKER, ROLE_EMPLOYER, ROLE_ADMIN
        │   ├── repository/                       # Spring Data JPA Repositories
        │   │   ├── UserRepository.java
        │   │   ├── JobRepository.java
        │   │   └── ApplicationRepository.java
        │   ├── security/
        │   │   ├── JwtTokenProvider.java         # Generates & validates JWT tokens
        │   │   ├── JwtAuthFilter.java            # Intercepts requests, validates tokens
        │   │   └── GlobalExceptionHandler.java   # @ControllerAdvice for error responses
        │   ├── service/
        │   │   ├── AuthService.java              # Signup, login, token validation logic
        │   │   ├── JobService.java               # Job CRUD, filtering, ownership checks
        │   │   └── ApplicationService.java       # Application logic, status updates
        │   └── util/
        │       └── ValidationUtils.java          # Email, password, job data validation
        └── resources/
            ├── application.properties            # DB, JWT secret, expiration config
            └── static/                           # Integrated Frontend (served at root)
                ├── index.html                    # Seeker landing & job browsing
                ├── login.html                    # Login UI (sends credentials to /auth/login)
                ├── register.html                 # Registration UI (sends to /auth/signup)
                ├── jobs.html                     # Browse, filter, search jobs
                ├── seeker-dashboard.html         # Seeker: view my applications, upload resume
                ├── employer-dashboard.html       # Employer: create jobs, review applicants
                ├── admin-dashboard.html          # Admin: user list, job moderation
                ├── app.js                        # Shared JS logic (token storage, API calls)
                └── style.css                     # Custom layout & design stylesheet

└── uploads/                             # Local folder for resume files (created at runtime)
```

---

## 4. Database Schema & Data Models

All database tables are mapped via Spring Data JPA. The schema configuration uses `spring.jpa.hibernate.ddl-auto=update`, which automatically generates and updates tables in MySQL.

### 4.1. User (`users` table)

Represents accounts for Seekers, Employers, and Admins.

| Column | Type | Notes |
|:---|:---|:---|
| `id` | Long | Primary Key, Auto-increment |
| `name` | String | Required, user's full name |
| `email` | String | Required, Unique, used for login |
| `password` | String | Required, BCrypt hashed (never plaintext) |
| `role` | Enum | Required, `ROLE_SEEKER`, `ROLE_EMPLOYER`, or `ROLE_ADMIN` |
| `resumePath` | String | Nullable, path to uploaded resume file |
| `createdAt` | LocalDateTime | Timestamp of account creation |

**Security Note:** Passwords are hashed via `BCryptPasswordEncoder` during signup and verified on login. Database stores only the hash, never plaintext.

### 4.2. Job (`jobs` table)

Created by employers to advertise vacancies.

| Column | Type | Notes |
|:---|:---|:---|
| `id` | Long | Primary Key, Auto-increment |
| `title` | String | Required, job title |
| `company` | String | Required, company name |
| `location` | String | Required, job location |
| `type` | String | Required, e.g., `'Full-time'`, `'Part-time'`, `'Internship'`, `'Remote'` |
| `description` | Text | Nullable, detailed job description |
| `employerId` | Long | Required, Foreign Key to User (Employer) |
| `postedDate` | LocalDate | Timestamp of job creation |

### 4.3. Application (`applications` table)

Represents a job seeker applying for a job listing.

| Column | Type | Notes |
|:---|:---|:---|
| `id` | Long | Primary Key, Auto-increment |
| `seekerId` | Long | Required, Foreign Key to User (Seeker) |
| `jobId` | Long | Required, Foreign Key to Job |
| `status` | String | Required, `'Applied'`, `'Shortlisted'`, or `'Rejected'` |
| `appliedDate` | LocalDate | Timestamp of application submission |

**Constraint:** Unique pair of (`seekerId`, `jobId`) prevents duplicate applications.

---

## 5. Key Security Components

### 5.1. JwtTokenProvider

Responsible for JWT token generation and validation.

**Key Methods:**
* `generateToken(userId, email, role)` — Creates a signed JWT with user claims and expiration.
* `validateToken(token)` — Verifies token signature and expiration; throws exception if invalid.
* `getClaimsFromToken(token)` — Extracts user ID, email, and role from a valid token.

**Configuration:**
* Token payload includes: `userId`, `email`, `role`
* Secret key: Set via `jwt.secret` in `application.properties`
* Expiration: Configurable via `jwt.expiration` (default: 86400000 ms = 24 hours)
* Algorithm: HMAC-SHA256

### 5.2. JwtAuthFilter

Custom servlet filter that intercepts every HTTP request.

**Workflow:**
1. Extract `Authorization` header (format: `Bearer <token>`)
2. Validate token via `JwtTokenProvider.validateToken()`
3. If valid, extract claims and build `UsernamePasswordAuthenticationToken`
4. Set `SecurityContext` with extracted authorities (`ROLE_SEEKER`, `ROLE_EMPLOYER`, `ROLE_ADMIN`)
5. If invalid or missing, request is rejected before reaching controllers

**Integration:** Registered in `SecurityConfig` filter chain; runs before `UsernamePasswordAuthenticationFilter`.

### 5.3. SecurityConfig

Spring Security configuration using stateless architecture.

**Key Settings:**
* **Session Management:** `sessionCreationPolicy(SessionCreationPolicy.STATELESS)` — No server-side sessions.
* **Filter Chain:**
    - `POST /auth/signup` — Public, no auth required.
    - `POST /auth/login` — Public, no auth required.
    - `GET /jobs` — Public, no auth required.
    - `POST /jobs`, `DELETE /jobs/{id}` — Requires `ROLE_EMPLOYER` or `ROLE_ADMIN`.
    - `POST /applications` — Requires `ROLE_SEEKER`.
    - `GET /admin/**` — Requires `ROLE_ADMIN`.
    - All other endpoints — Requires authentication.
* **Password Encoder:** `BCryptPasswordEncoder` bean.
* **CORS:** Configured to allow frontend requests from localhost and production domains.

---

## 6. Main Functional Flows & API Routes

### 6.1. Authentication Flow (JWT-Based)

#### Registration (`POST /auth/signup`)

**Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "role": "SEEKER"
}
```

**Server-Side:**
1. Validate input via `ValidationUtils` (email format, password strength).
2. Check if email already exists in database.
3. Hash password via `BCryptPasswordEncoder`.
4. Persist user with `Role` enum.

**Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "SEEKER",
  "message": "User registered successfully"
}
```

#### Login (`POST /auth/login`)

**Request:**
```json
{
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Server-Side:**
1. Fetch user by email from database.
2. Verify password via `BCryptPasswordEncoder.matches()`.
3. If credentials valid, generate JWT via `JwtTokenProvider.generateToken()`.
4. Return token to client.

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "john@example.com",
  "role": "SEEKER"
}
```

**Client-Side (Frontend):**
1. Store token in `localStorage` with key `authToken`.
2. All subsequent requests include header: `Authorization: Bearer <token>`.

#### Get Current User (`GET /auth/me`)

**Request:**
```
GET /auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Server-Side:**
1. `JwtAuthFilter` validates token and sets `SecurityContext`.
2. Controller retrieves user ID from `SecurityContext`.
3. Fetch full user details from database.

**Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "SEEKER",
  "resumePath": "/uploads/resume_1.pdf"
}
```

### 6.2. Job Listing & Search Flow

#### Browse Jobs (`GET /jobs`)

**Query Parameters:**
* `title` (optional) — Filter by job title (case-insensitive, partial match).
* `location` (optional) — Filter by location.
* `type` (optional) — Filter by job type (Full-time, Part-time, etc.).

**Example:**
```
GET /jobs?title=Java&location=Bangalore
```

**Response:**
```json
[
  {
    "id": 1,
    "title": "Java Developer",
    "company": "TechCorp",
    "location": "Bangalore",
    "type": "Full-time",
    "description": "...",
    "employerId": 5,
    "postedDate": "2024-06-15"
  }
]
```

#### Get Single Job (`GET /jobs/{id}`)

Returns full job details (no auth required).

#### Create Job (`POST /jobs`)

**Auth Required:** `ROLE_EMPLOYER` or `ROLE_ADMIN`

**Request:**
```json
{
  "title": "Java Developer",
  "company": "TechCorp",
  "location": "Bangalore",
  "type": "Full-time",
  "description": "Seeking experienced Java developer..."
}
```

**Server-Side:**
1. Extract employer ID from `SecurityContext` (authenticated user).
2. Validate input via `ValidationUtils`.
3. Persist job with `employerId` and current date.

**Response:**
```json
{
  "id": 10,
  "title": "Java Developer",
  "company": "TechCorp",
  "employerId": 5,
  "postedDate": "2024-06-20"
}
```

#### Delete Job (`DELETE /jobs/{id}`)

**Auth Required:** Employer who owns the job or Admin

**Server-Side Validation:**
1. Fetch job by ID.
2. Verify requestor is either the job owner (`employerId`) or admin.
3. If validation fails, return 403 Forbidden.
4. Delete job; cascade to associated applications.

### 6.3. Job Application Flow

#### Apply to Job (`POST /applications`)

**Auth Required:** `ROLE_SEEKER`

**Request:**
```json
{
  "jobId": 1
}
```

**Server-Side:**
1. Extract seeker ID from `SecurityContext`.
2. Check if application already exists (seeker + job pair).
3. If duplicate, return 409 Conflict.
4. Create application with status `'Applied'`.

**Response:**
```json
{
  "id": 5,
  "seekerId": 1,
  "jobId": 1,
  "status": "Applied",
  "appliedDate": "2024-06-20"
}
```

#### My Applications (`GET /applications`)

**Auth Required:** Authenticated user (Seeker or Employer)

**Response (for Seeker):**
Returns all applications by the current seeker, with joined job details.

```json
[
  {
    "id": 5,
    "jobId": 1,
    "jobTitle": "Java Developer",
    "company": "TechCorp",
    "location": "Bangalore",
    "status": "Applied",
    "appliedDate": "2024-06-20"
  }
]
```

**Response (for Employer):**
Returns all applications for jobs posted by the current employer.

#### Update Application Status (`POST /applications/update-status`)

**Auth Required:** `ROLE_EMPLOYER` (job owner) or `ROLE_ADMIN`

**Request:**
```json
{
  "applicationId": 5,
  "status": "Shortlisted"
}
```

**Server-Side Validation:**
1. Fetch application.
2. Verify employer owns the associated job.
3. Update status to `'Shortlisted'` or `'Rejected'`.

### 6.4. Resume Management Flow

#### Upload Resume (`POST /upload-resume`)

**Auth Required:** `ROLE_SEEKER`

**Request:** Multipart form-data with file.

**Validations:**
* File type: PDF or DOCX only.
* File size: Max 5 MB.

**Server-Side:**
1. Validate file.
2. Save to `uploads/` directory with name `resume_{userId}.{ext}`.
3. Update user's `resumePath` in database.

**Response:**
```json
{
  "message": "Resume uploaded successfully",
  "resumePath": "/uploads/resume_1.pdf"
}
```

#### Check Resume Status (`GET /my-resume`)

**Auth Required:** `ROLE_SEEKER`

**Response:**
```json
{
  "hasResume": true,
  "resumePath": "/uploads/resume_1.pdf"
}
```

#### Download Resume (`GET /resume/{seekerId}`)

**Auth Required:** Employer or Admin

**Server-Side:**
1. Verify requester is allowed (job owner of applicant or admin).
2. Stream file from disk.
3. Set header `Content-Disposition: attachment; filename=resume_{seekerId}.pdf`.

---

## 7. Authentication Flow Diagram (JWT)

```
Client (Browser)                        Server (Spring Boot)
│                                        │
│            POST /auth/signup           │
│        { email, password, role }       │
├───────────────────────────────────────>│
│                                        │  BCrypt hash password
│                                        │  Persist user + Role enum
│                                        │
│                201 Created             │
│            { id, email, role }         │
│<───────────────────────────────────────┤
│                                        │
│             POST /auth/login           │
│            { email, password }         │
├───────────────────────────────────────>│
│                                        │  Verify BCrypt match
│                                        │  Generate JWT (id, email, role, exp)
│                                        │
│                200 OK                  │
│        { token, userId, role }         │
│<───────────────────────────────────────┤
│                                        │
│  Store token in localStorage           │
│  (Local storage: authToken = token)    │
│                                        │
│  GET /applications                     │
│  Authorization: Bearer <token>         │
├───────────────────────────────────────>│
│                                        │  JwtAuthFilter intercepts
│                                        │  Validates token signature & expiration
│                                        │  Extracts id, email, role
│                                        │  Sets SecurityContext with ROLE_SEEKER
│                                        │  Controller processes request
│                                        │
│                200 OK                  │
│         [ applications data ]          │
│<───────────────────────────────────────┤
│                                        │
```

---

## 8. Global Exception Handling

A centralized `GlobalExceptionHandler` (using `@ControllerAdvice`) catches all exceptions and returns formatted JSON responses:

**Example Error Response:**
```json
{
  "status": 400,
  "message": "Invalid email format",
  "timestamp": "2024-06-20T10:30:00Z"
}
```

**Common Status Codes:**
* `200 OK` — Successful request
* `201 Created` — Resource created (signup)
* `400 Bad Request` — Validation failure
* `401 Unauthorized` — Missing or invalid token
* `403 Forbidden` — Insufficient permissions
* `404 Not Found` — Resource not found
* `409 Conflict` — Duplicate application
* `500 Internal Server Error` — Server-side error

---

## 9. Data Initialization

The `DataInitializer` bean (implementing `CommandLineRunner`) runs once on application startup and automatically seeds a default admin account:

**Credentials:**
* Email: `admin@jobportal.com`
* Password: BCrypt hashed (stored securely)
* Role: `ROLE_ADMIN`

This eliminates manual SQL insertion and simplifies the first-run experience.

---

## 10. Frontend Integration

The frontend (`static/`) manages JWT tokens via `localStorage`:

**Key JavaScript Functions (in `app.js`):**
1. **Store Token:** After successful login, save token to `localStorage.authToken`.
2. **Retrieve Token:** On page load, check `localStorage` for existing token.
3. **Add to Requests:** Attach `Authorization: Bearer <token>` header to all API calls.
4. **Handle 401:** If token is expired or invalid, redirect to login page.

**Example:**
```javascript
// Login
const response = await fetch('/auth/login', { ... });
const { token } = await response.json();
localStorage.authToken = token;

// Subsequent API call
const headers = {
  'Authorization': `Bearer ${localStorage.authToken}`
};
fetch('/applications', { headers });
```

---

## 11. Key Improvements Over Session-Based Architecture

| Aspect | Session-Based (Previous) | JWT (Current) |
|:---|:---|:---|
| **Token Storage** | Server-side session store | Stateless (client-side) |
| **Scalability** | Limited (session affinity needed) | Highly scalable (no server memory) |
| **API Calls** | Session ID in cookie | JWT in Authorization header |
| **Password Storage** | Plaintext or weak hashing | BCrypt hashing |
| **Logout** | Immediate (server-side invalidation) | Token-based (expiration) |
| **Mobile-Friendly** | Less suitable | Native support |

---

## 12. Technology & Design Patterns

* **Layered Architecture:** Controller → Service → Repository → Database
* **Dependency Injection:** Spring's `@Autowired` for loose coupling
* **DTO Pattern:** Request/Response objects decouple API contract from domain models
* **Repository Pattern:** `Spring Data JPA` abstracts database queries
* **Filter Pattern:** `JwtAuthFilter` intercepts and processes all HTTP requests
* **Exception Handling:** `@ControllerAdvice` centralizes error management

---

## Summary

The Job Portal is a **stateless, JWT-authenticated REST API** built on Spring Boot 3.2 with three user roles and comprehensive job management capabilities. Security is handled via BCrypt password hashing and JWT tokens. The integrated vanilla frontend communicates via standard HTTP methods and localStorage for token persistence. This architecture is production-ready, scalable, and follows modern REST API best practices.