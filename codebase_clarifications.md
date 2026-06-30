# Codebase Clarifications — Job Portal Project

This document details implementation specifics, architectural decisions, and behavioral nuances in the Job Portal codebase. Use this to help an AI developer make safe and correct updates.

---

## 1. Authentication & Security

The project uses a **stateless JWT authentication architecture**:

1. **Token Generation:** On successful login (`POST /auth/login`), `JwtTokenProvider` generates a signed JWT whose subject is the `userId` and which carries `userId`, `userName`, `userEmail`, and `userRole` as claims. The token is returned to the client inside the `LoginResponseDto` (`{ token, id, name, role }`).
2. **Token Transmission:** The frontend stores the login response object in `localStorage` under the key `user` (a JSON object `{ token, id, name, role }`) and attaches the token to every API call via the standard `Authorization: Bearer <token>` header using the `apiFetch()` utility in `app.js`.
3. **Request Filtering:** `JwtAuthFilter` extends `OncePerRequestFilter`. Its `shouldNotFilter` bypasses only `/auth/login` and `/auth/signup` (so `/auth/me` still runs the filter). It extracts the token from the `Authorization` header, validates the signature and expiration via `JwtTokenProvider`, and constructs a `UsernamePasswordAuthenticationToken` with the user's authorities. It also sets request attributes `userId`, `userName`, and `userRole` — some controllers read these attributes instead of `@AuthenticationPrincipal`.
4. **Authority Binding:** The `Role` enum values (`SEEKER`, `EMPLOYER`, `ADMIN`) are prefixed with `ROLE_` when set as granted authorities — e.g., `ROLE_SEEKER`. This follows Spring Security's default role-voting convention.
5. **Context Propagation:** The authentication object is set on the `SecurityContextHolder`, making it accessible in controllers via `@AuthenticationPrincipal`, `SecurityContextHolder.getContext().getAuthentication()`, or the request attributes set by the filter.

### SecurityConfig Filter Chain & Method Security

`SecurityConfig` only distinguishes public from authenticated endpoints in the filter chain; the actual per-role authorization is done with **method security** (`@EnableMethodSecurity` on `SecurityConfig` plus `@PreAuthorize` annotations on controller methods).

**Filter chain (`authorizeHttpRequests`):**
- `OPTIONS /**` — permitAll (CORS preflight).
- `/auth/**` — permitAll.
- Static pages (the `*.html` files) plus `/style.css` and `/app.js` — permitAll.
- `GET /jobs` and `GET /jobs/**` — permitAll.
- `anyRequest().authenticated()` — every other endpoint merely requires authentication.

**`@PreAuthorize` annotations (the real per-role rules):**
- `JobController`: `POST /jobs` → `hasRole('EMPLOYER')`; `DELETE /jobs/{id}` → `hasAnyRole('EMPLOYER','ADMIN')`; GET endpoints public.
- `ApplicationController`: `POST /applications` → `hasRole('SEEKER')`; `GET /applications` → `hasAnyRole('SEEKER','EMPLOYER')`; `POST /applications/update-status` → `hasRole('EMPLOYER')`.
- `ResumeController`: `POST /upload-resume` and `GET /my-resume` → `hasRole('SEEKER')`; `GET /resume/{seekerId}` → `hasAnyRole('ADMIN','EMPLOYER')`.
- `AdminController`: class-level `hasRole('ADMIN')`.

> **Note:** CSRF is disabled because this is a stateless JWT API — there are no session cookies to protect.

---

## 2. Password Storage

- **Implementation:** Passwords are hashed using `BCryptPasswordEncoder` configured as a Spring bean in `AppConfig` (or `SecurityConfig`).
- **Registration Flow:** `AuthService` encodes the raw password via `passwordEncoder.encode()` before persisting the `User` entity.
- **Login Flow:** `AuthService` verifies using `passwordEncoder.matches(rawInput, storedHash)`.
- No plaintext passwords exist anywhere in the system.

---

## 3. Role Model

- The `Role` Java enum defines three values: `SEEKER`, `EMPLOYER`, `ADMIN`.
- The `User` entity holds a `@Enumerated(EnumType.STRING)` field, storing the role as a clean string (`"SEEKER"`, not an ordinal) in the database.
- In `JwtAuthFilter`, the role is converted to a `SimpleGrantedAuthority` with the `ROLE_` prefix to integrate with Spring Security's `hasRole()` expressions used in the `@PreAuthorize` method-security annotations.

---

## 4. Admin Seeding

- `DataInitializer` implements `CommandLineRunner` and executes on application startup.
- It checks whether an admin user already exists (by email or role) before creating one, making it idempotent — safe to run on every boot without duplicating records.
- The admin password is BCrypt-hashed at creation time, not stored as plaintext.
- This completely replaces the old manual SQL `INSERT INTO users` approach.

---

## 5. Resume Upload Handling

- **Location:** Resumes are saved in the `uploads/` folder located in the project root.
- **Disk Path Logic:** `Paths.get(uploadDir).toAbsolutePath().normalize()` resolves the path to prevent Tomcat from writing to a transient temp folder. The absolute system path is stored in `User.resumePath`.
- **File Replacement:** When a user re-uploads, `StandardCopyOption.REPLACE_EXISTING` overwrites the old file. Files are named `resume_{userId}.pdf` or `resume_{userId}.docx`, ensuring each seeker has exactly one resume.
- **Validation:** Only `PDF` and `DOCX` MIME types are accepted, with a 5 MB size cap enforced at the controller level.

---

## 6. Resume Download

### Access Control

`GET /resume/{seekerId}` is restricted to `ADMIN` and `EMPLOYER` (`@PreAuthorize`), with an additional service-level ownership check: an EMPLOYER may only download the resume of a candidate who has applied to one of **their own** job postings (verified via `applicationRepository.existsByJobEmployerIdAndSeekerId(...)`); ADMINs may download any resume. A 403 is returned if an employer requests a resume for a seeker who has not applied to one of their jobs.

### Frontend Mechanics

Since resume downloads require JWT authentication (via the `Authorization` header), a standard HTML `<a href="/resume/123">` link won't work because anchor clicks cannot attach custom headers.

### Frontend Implementation

Inside `employer-dashboard.html`, the download triggers a JavaScript method:
1. Intercepts the anchor click (`event.preventDefault()`).
2. Executes a `fetch()` call with the `Authorization: Bearer <token>` header.
3. Converts the response to a binary `Blob`.
4. Creates a temporary in-memory URL (`window.URL.createObjectURL(blob)`).
5. Programmatically creates and clicks a hidden `<a>` element to trigger the browser download, then revokes the object URL.

---

## 7. Service-Layer Ownership Guardrails

Employer-facing endpoints perform explicit ownership validation in the service layer:
- **Viewing applicants:** `ApplicationService` verifies that the `job.employer` matches the authenticated employer before returning applicant data. Returns a 403 if mismatched.
- **Updating status:** `ApplicationService` performs the same ownership check before allowing status changes.
- **Deleting jobs:** `JobService` ensures only the owning employer (or an admin) can delete a job listing.

This means even if a malicious employer guesses another employer's job ID, the service layer rejects the operation regardless of what the security filter chain allowed.

---

## 8. Duplicate Application Prevention

- `ApplicationService` checks for an existing `Application` via `applicationRepository.existsByJobIdAndSeekerId(...)` before creating a new one.
- If a duplicate is found, a conflict response (typically 409) is returned.
- This is enforced both at the service level (allowing a clean error message to be returned to the client) **and** by a database unique constraint `uq_application_job_seeker` on (`job_id`, `seeker_id`) as a safety net.

---

## 9. Cascade Deletion via JPA

- The `Job` entity defines a cascade relationship (e.g., `CascadeType.ALL` or `@OneToMany(cascade = CascadeType.REMOVE)`) on its `applications` collection.
- When a job is deleted (by its owner or an admin), Hibernate automatically issues `DELETE` statements for all associated `Application` records before deleting the `Job` itself.
- This prevents `foreign key constraint violation` errors that would occur if applications were deleted independently or out of order.

---

## 10. DTO Pattern & ModelMapper

- All controller methods accept request DTOs and return response DTOs — entities are never exposed directly to the API layer.
- `ModelMapper` (configured as a bean in `AppConfig`) handles entity-to-DTO and DTO-to-entity conversion.
- This decouples the API contract from the database schema, allowing fields to be renamed or restructured in the entity without breaking client integrations.

---

## 11. Global Exception Handling

- `GlobalExceptionHandler` is a `@ControllerAdvice` class that catches all unhandled exceptions across all controllers.
- It formats errors into a consistent JSON structure (e.g., `{"error": "message", "status": 404}`).
- Handles common cases: `EntityNotFoundException`, `MethodArgumentNotValidException` (DTO validation failures), custom business exceptions, and a catch-all `Exception` handler for unexpected errors.

---

## 12. Input Validation

- `ValidationUtils` provides reusable validation helper methods used across services.
- DTOs use `jakarta.validation` annotations (`@NotBlank`, `@Email`, `@Size`, etc.) for declarative constraint definition.
- Validation is triggered automatically by Spring when `@Valid` is used on controller method parameters.

---

## 13. Live Search & Debouncing

To prevent spamming the backend on every keystroke in the browse page (`jobs.html`), search input listeners are wrapped in a debounce utility:

```javascript
function debounce(fn, delay) {
  let timer;
  return function () {
    clearTimeout(timer);
    timer = setTimeout(fn, delay);
  };
}
const debouncedSearch = debounce(loadJobs, 500);
```

- **Behavior:** The API call is delayed by 500ms and resets on every keystroke. It only fires once the user stops typing for 500ms.

---

## 14. CORS Configuration

- **Config location:** CorsConfig.java
- **Purpose:** Allows the bundled static frontend (served from `http://localhost:8080`) and local dev servers to communicate with the API.
- **Allowed origins:** exactly `http://localhost:5500`, `http://127.0.0.1:5500`, and `http://localhost:8080`. There are no production domains.
- **Key settings:** Allowed methods are GET, POST, PUT, DELETE, OPTIONS, PATCH; `allowedHeaders` is `*` (so JWT-bearing requests pass the preflight `OPTIONS` check); `allowCredentials` is true.