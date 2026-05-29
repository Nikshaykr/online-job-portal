# Codebase Clarifications — Job Portal Project

This document details implementation details, trade-offs, college-project shortcuts, and architectural constraints in the Job Portal project. Use this to help an AI developer make safe and correct updates to the codebase.

---

## 1. Authentication & Security (Critical Caveat)

The project implements a **hybrid authentication approach**:
1. **HTTP Sessions:** The backend registers session variables (`userId`, `userName`, `userRole`) inside `AuthController.java` on the standard HttpSession object.
2. **Custom HTTP Headers:** The client-side `apiFetch()` utility in [app.js](file:///backend/src/main/resources/static/app.js) reads user profiles cached in browser `localStorage` and appends them to outgoing API calls via `X-User-Id` and `X-User-Role` headers.
3. **Controller Interceptors:** Endpoints retrieve user info using a fallback strategy, checking the custom headers first, and checking session attributes if the headers are absent.

```java
// Common pattern used across controllers
private Long resolveUserId(HttpServletRequest request, HttpSession session) {
    String h = request.getHeader("X-User-Id");
    if (h != null) {
        try { return Long.valueOf(h); } catch (Exception ignored) {}
    }
    return (Long) session.getAttribute("userId");
}
```

### Why was this done?
This dual approach was built to bypass CORS/Session sharing restrictions when running the frontend via a **VS Code Live Server** (hosted on port `5500` or `127.0.0.1:5500`) while the backend runs on port `8080`. Since cookies/sessions can sometimes fail cross-origin in local development, custom headers act as a reliable fallback.

> [!WARNING]
> **Production Security Risk:** Client-side header authentication is highly insecure. Users can easily spoof these headers in curl or developer tools to access any user account or admin dashboard. If refactoring for production, remove the header fallbacks entirely and enforce session cookies or implement JWT (JSON Web Tokens).

---

## 2. Password Storage
* **Implementation:** Passwords are saved as raw, unhashed strings in the database (e.g., `User.password = "123"`).
* **Rationale:** Done for simplicity as a BCA curriculum project.
* **Refactoring note:** If security needs to be upgraded, add the `spring-boot-starter-security` dependency and use `BCryptPasswordEncoder` to hash/verify passwords.

---

## 3. Resume Upload Handling
* **Location:** Resumes are saved in the `uploads/` folder located in the root of the project.
* **Disk Path Logic:** `Paths.get(uploadDir).toAbsolutePath().normalize()` is used to resolve the path on the server to prevent tomcat from writing files to a transient temp folder. The absolute system path is stored directly in `users.resume_path`.
* **File Replacement:** When a user uploads a new resume, `StandardCopyOption.REPLACE_EXISTING` overwrites the old file on disk. The path is set to `resume_{userId}.pdf` or `resume_{userId}.docx`, ensuring each seeker only ever has a single resume file.

---

## 4. Resume Download Workaround
Because the backend checks custom HTTP headers (`X-User-Role`) for permission to download resumes, a standard HTML hyperlink (`<a href="/resume/123">Download</a>`) will fail because standard anchor clicks do not support custom request headers.

### Frontend Workaround:
Inside [employer-dashboard.html](file:///backend/src/main/resources/static/employer-dashboard.html), download triggers a custom JavaScript method `addAuthHeaders(event, seekerId)`:
1. It intercepts the anchor click (`event.preventDefault()`).
2. It executes a `fetch()` request manually appending the custom `X-User-Id` and `X-User-Role` headers.
3. It converts the response stream to a binary `Blob` object.
4. It creates a temporary URL in memory (`window.URL.createObjectURL(blob)`).
5. It instantiates a hidden `<a>` node programmatically, clicks it to launch the browser download, and then releases the memory URL.

---

## 5. Live Search & Debouncing
To prevent spamming the backend database with requests on every keystroke in the browse page ([jobs.html](file:///backend/src/main/resources/static/jobs.html)), the input search listeners are wrapped in a debounce utility:

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
* **Behavior:** The API call is delayed by 500ms and resets on every keystroke. It only fires once the user stops typing for 500ms.

---

## 6. Local Server & CORS Configuration
* **Config location:** [CorsConfig.java](file:///backend/src/main/java/com/jobportal/config/CorsConfig.java)
* **Origins allowed:** `http://localhost:5500` and `http://127.0.0.1:5500` (defaults for VS Code Live Server).
* **Credentials:** Enabled (`allowCredentials(true)`) to support sessions across origins.
