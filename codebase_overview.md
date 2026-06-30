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
| `createdAt` | LocalDateTime | Timestamp of account creation (auto-set via Hibernate `@CreationTimestamp`) |

The `User` entity implements Spring Security's `UserDetails` interface.

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
| `employer` | User (`@ManyToOne`) | Required, mapped via join column `employer_id` to User (Employer) |
| `postedDate` | LocalDateTime | Timestamp of job creation |

### 4.3. Application (`applications` table)

Represents a job seeker applying for a job listing.

| Column | Type | Notes |
|:---|:---|:---|
| `id` | Long | Primary Key, Auto-increment |
| `seeker` | User (`@ManyToOne`) | Required, mapped via join column `seeker_id` to User (Seeker) |
| `job` | Job (`@ManyToOne`) | Required, mapped via join column `job_id` to Job |
| `status` | String | Defaults to `'Applied'`; values `'Applied'`, `'Shortlisted'`, or `'Rejected'` |
| `appliedDate` | LocalDate | Timestamp of application submission |

**Constraint:** A DB unique constraint `uq_application_job_seeker` on (`job_id`, `seeker_id`) prevents duplicate applications.

---

## 5. Key Security Components

### 5.1. JwtTokenProvider

Responsible for JWT token generation and validation.

**Key Methods (public):**
* `generateToken(User user)` — Creates a signed JWT with user claims and expiration.
* `validateToken(String token)` — Verifies token signature and expiration; throws exception if invalid.
* `getUserIdFromToken(token)`, `getUserNameFromToken(token)`, `getUserRoleFromToken(token)` — Extract individual claims from a valid token.
* `getExpirationDateFromUser(...)` — Returns the token expiration.
* (Claim extraction is backed by a private `getClaimsFromToken(...)` helper — it is not part of the public API.)

**Configuration:**
* Token subject: `userId`; additional claims: `userId`, `userName`, `userEmail`, `userRole`
* Secret key: Read via `@Value("${jwt.secretKey}")` from `application.properties`; signing key built with `Keys.hmacShaKeyFor(...)`
* Expiration: Read via `@Value("${jwt.expiration}")` (default: 86400000 ms = 24 hours)
* Algorithm: HMAC-SHA256 (HS256)

### 5.2. JwtAuthFilter

Custom servlet filter that intercepts every HTTP request.

**Workflow:**
1. Extract `Authorization` header (format: `Bearer <token>`)
2. Validate token via `JwtTokenProvider.validateToken()`
3. If valid, extract claims and build `UsernamePasswordAuthenticationToken`
4. Set `SecurityContext` with extracted authorities (`ROLE_SEEKER`, `ROLE_EMPLOYER`, `ROLE_ADMIN`), and also set request attributes `userId`, `userName`, `userRole` (some controllers read these attributes instead of `@AuthenticationPrincipal`)
5. If invalid or missing, request proceeds unauthenticated and is rejected at authorization time before reaching protected controllers

**Integration:** Registered in `SecurityConfig` filter chain; runs before `UsernamePasswordAuthenticationFilter`. `shouldNotFilter` bypasses only `/auth/login` and `/auth/signup` (so `/auth/me` still runs the filter).

### 5.3. SecurityConfig

Spring Security configuration using stateless architecture.

**Key Settings:**
* **Session Management:** `sessionCreationPolicy(SessionCreationPolicy.STATELESS)` — No server-side sessions.
* **Method Security:** `@EnableMethodSecurity` is set on `SecurityConfig`; per-role authorization is enforced by `@PreAuthorize` annotations on controller methods (see below), **not** by the filter chain.
* **Filter Chain (authorizeHttpRequests):**
    - `OPTIONS /**` — permitAll (CORS preflight).
    - `/auth/**` — permitAll.
    - Static pages (the `*.html` files) plus `/style.css` and `/app.js` — permitAll.
    - `GET /jobs` and `GET /jobs/**` — permitAll.
    - `anyRequest().authenticated()` — every other endpoint requires authentication only.
* **`@PreAuthorize` rules (the actual per-role authorization):**
    - `JobController`: `POST /jobs` → `hasRole('EMPLOYER')`; `DELETE /jobs/{id}` → `hasAnyRole('EMPLOYER','ADMIN')`; GET endpoints public.
    - `ApplicationController`: `POST /applications` → `hasRole('SEEKER')`; `GET /applications` → `hasAnyRole('SEEKER','EMPLOYER')`; `POST /applications/update-status` → `hasRole('EMPLOYER')`.
    - `ResumeController`: `POST /upload-resume` and `GET /my-resume` → `hasRole('SEEKER')`; `GET /resume/{seekerId}` → `hasAnyRole('ADMIN','EMPLOYER')`.
    - `AdminController`: class-level `hasRole('ADMIN')`.
* **Password Encoder:** `BCryptPasswordEncoder` bean.
* **CORS:** Allowed origins are exactly `http://localhost:5500`, `http://127.0.0.1:5500`, and `http://localhost:8080` (no production domains). Allowed methods: GET, POST, PUT, DELETE, OPTIONS, PATCH; `allowedHeaders` is `*`; `allowCredentials` is true.

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
  "role": "seeker"
}
```

> The `role` value is lowercase (`"seeker"` / `"employer"`) — the `Role` enum is annotated with `@JsonProperty("seeker")` etc.

**Server-Side:**
1. Validate input via `ValidationUtils` (email format, password strength).
2. Check if email already exists in database.
3. Hash password via `BCryptPasswordEncoder`.
4. Persist user with `Role` enum.

**Response (`SignUpResponseDto`):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "seeker",
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

**Response (`LoginResponseDto`):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "id": 1,
  "name": "John Doe",
  "role": "seeker"
}
```

> The login response contains `id` and `name`; it does **not** include `email` or a `userId` field.

**Client-Side (Frontend):**
1. Store the response object in `localStorage` under the key `user` (a JSON object `{ token, id, name, role }`).
2. All subsequent requests include header: `Authorization: Bearer <token>` (added by the `apiFetch()` helper in `app.js`).

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

**Response (`UserResponseDto`):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "seeker",
  "createdAt": "2024-06-20T10:30:00"
}
```

> `/auth/me` does **not** return `resumePath` — the server deliberately hides the raw stored path. Use `/my-resume`, which returns only a filename.

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
    "postedById": 5,
    "postedByName": "Tech Corp HR",
    "postedDate": "2024-06-15T10:30:00"
  }
]
```

#### Get Single Job (`GET /jobs/{id}`)

Returns full job details (no auth required).

#### Create Job (`POST /jobs`)

**Auth Required:** `ROLE_EMPLOYER` only (`@PreAuthorize("hasRole('EMPLOYER')")`)

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
1. Extract employer from `SecurityContext` (authenticated user).
2. Validate input via `ValidationUtils`.
3. Persist job with the `employer` (`@ManyToOne`) association and current date/time.

**Response:**
```json
{
  "id": 10,
  "title": "Java Developer",
  "company": "TechCorp",
  "postedById": 5,
  "postedByName": "Tech Corp HR",
  "postedDate": "2024-06-20T14:05:00"
}
```

#### Delete Job (`DELETE /jobs/{id}`)

**Auth Required:** Employer who owns the job or Admin

**Server-Side Validation:**
1. Fetch job by ID.
2. Verify requestor is either the job owner (`job.employer`) or admin.
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
2. Check if application already exists via `applicationRepository.existsByJobIdAndSeekerId(...)` (seeker + job pair); a DB unique constraint `uq_application_job_seeker` also enforces this at the database level.
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

**Auth Required:** Employer or Admin (`@PreAuthorize("hasAnyRole('ADMIN','EMPLOYER')")`)

**Server-Side:**
1. Verify requester is allowed: ADMINs may download any resume; an EMPLOYER may only download the resume of a candidate who has applied to one of their own job postings (checked via `applicationRepository.existsByJobEmployerIdAndSeekerId(...)`). Otherwise return 403.
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
│                                        │  Generate JWT (subject=userId; claims: userId, userName, userEmail, userRole)
│                                        │
│                200 OK                  │
│      { token, id, name, role }         │
│<───────────────────────────────────────┤
│                                        │
│  Store response in localStorage        │
│  (localStorage.user = { token,id,... }) │
│                                        │
│  GET /applications                     │
│  Authorization: Bearer <token>         │
├───────────────────────────────────────>│
│                                        │  JwtAuthFilter intercepts
│                                        │  Validates token signature & expiration
│                                        │  Extracts userId, userName, userRole
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
1. **Store User:** After successful login, save the response object to `localStorage` under the key `user` (a JSON object `{ token, id, name, role }`).
2. **Retrieve Token:** On page load / per request, parse `localStorage.user` and read its `token`.
3. **Add to Requests:** The `apiFetch(path, options)` helper reads the stored `user` object and, if present, sets the `Authorization: Bearer <user.token>` header on API calls.
4. **Handle 401:** If token is expired or invalid, redirect to login page.

> Note: `app.js` still contains a leftover `logout()` that POSTs to a non-existent `/logout` endpoint with `credentials:'include'`; these are legacy leftovers from the old session-based design.

**Example:**
```javascript
// Login
const response = await fetch('/auth/login', { ... });
const data = await response.json(); // { token, id, name, role }
localStorage.setItem('user', JSON.stringify(data));

// Subsequent API call (via the apiFetch helper)
const user = JSON.parse(localStorage.getItem('user'));
const headers = {
  'Authorization': `Bearer ${user.token}`
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