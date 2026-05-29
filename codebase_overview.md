# Codebase Overview — Job Portal Project

This document provides a simple and structured overview of the Online Job Portal project to help an AI agent or developer quickly understand its architecture, technologies, data models, and component relations.

---

## 1. Project Context & Purpose
* **Context:** A final-year BCA college project.
* **Goal:** A comprehensive job board system allowing job seekers to upload resumes and apply for job listings, employers to manage job postings and track applicant status, and administrators to moderate the platform.
* **Architecture:** A Spring Boot backend integrated with a vanilla HTML5/CSS3/JavaScript frontend (stored in static resources of the webapp).

---

## 2. Technical Stack
* **Java Version:** Java 17/21
* **Framework:** Spring Boot 3.2.x (Web, JPA, Session)
* **Database:** MySQL 8.0
* **Build Tool:** Maven 3.9+
* **Frontend:** Vanilla HTML5, CSS3 (Custom styles in `style.css`), ES6+ JavaScript (Shared logic in `app.js`)

---

## 3. Directory Structure
```text
job-portal/
├── backend/                             # Spring Boot Root
│   ├── pom.xml                          # Maven build script with project dependencies
│   └── src/main/
│       ├── java/com/jobportal/
│       │   ├── JobPortalApplication.java # Entry point
│       │   ├── config/                  # Configuration beans (CORS filters)
│       │   ├── controller/              # REST controllers for APIs
│       │   ├── model/                   # Hibernate JPA Entities (User, Job, Application)
│       │   └── repository/              # Spring Data JPA Repository Interfaces
│       └── resources/
│           ├── application.properties   # App configurations (DB, Port, Session, Upload size)
│           └── static/                  # Integrated Frontend (HTML, CSS, JS)
│               ├── index.html           # Seeker landing & Job browsing
│               ├── login.html           # Authentication UI
│               ├── register.html        # Registration UI
│               ├── jobs.html            # Browse, filter, search, & view job details
│               ├── seeker-dashboard.html # Seeker application & resume tracker
│               ├── employer-dashboard.html # Employer portal (manage jobs, update applicant statuses)
│               ├── admin-dashboard.html  # System statistics & moderation panel
│               ├── app.js               # Global Javascript helper utility script
│               └── style.css            # Custom layout & design theme stylesheet
└── uploads/                             # Local folder storing uploaded seeker resumes (PDF/DOCX)
```

---

## 4. Database Schema & Data Models

All database tables are mapped via Spring Data JPA. The schema configuration uses `spring.jpa.hibernate.ddl-auto=update`, which automatically generates and updates tables in MySQL.

### 4.1. User (`users` table)
Represents accounts for Seekers, Employers, and Admins.
* `id` (Long, Primary Key, Auto-increment)
* `name` (String, Required)
* `email` (String, Required, Unique)
* `password` (String, Required, Plain text)
* `role` (String, Required) — `'seeker'`, `'employer'`, or `'admin'`
* `resumePath` (String, Nullable) — Disk path pointing to the user's uploaded resume.

### 4.2. Job (`jobs` table)
Created by employers to advertise vacancies.
* `id` (Long, Primary Key, Auto-increment)
* `title` (String, Required)
* `company` (String, Required)
* `location` (String, Required)
* `type` (String, Required) — e.g. `'Full-time'`, `'Part-time'`, `'Internship'`, `'Remote'`
* `description` (Text, Nullable)
* `employerId` (Long, Required, Foreign Key User ID)
* `postedDate` (LocalDate)

### 4.3. Application (`applications` table)
Represents a job seeker applying for a job listing.
* `id` (Long, Primary Key, Auto-increment)
* `seekerId` (Long, Required, Foreign Key User ID)
* `jobId` (Long, Required, Foreign Key Job ID)
* `status` (String, Required) — `'Applied'`, `'Shortlisted'`, or `'Rejected'`
* `appliedDate` (LocalDate)

---

## 5. Main Functional Flows & API Routes

### 5.1. Authentication Flow
* **Registration (`POST /register`):** Creates new users if the email is not already taken.
* **Login (`POST /login`):** Validates email and plaintext password. If correct, caches the session attributes (`userId`, `userName`, `userRole`) and returns user metadata to be cached in the client's browser `localStorage`.
* **Logout (`POST /logout`):** Invalidates session variables on the server.
* **Session Verification (`GET /me`):** Authenticates client session and returns user info.

### 5.2. Job Listing & Search Flow
* **Browse Jobs (`GET /jobs`):** Retrieves all job postings. Supports parameter filtering via `title`, `location`, and `type`. Resolves partial-match case-insensitive queries.
* **Get Single Job (`GET /jobs/{id}`):** Returns job details for modals.
* **Post Job (`POST /jobs/add`):** Restricts requests to `employer` role. Stores listing with creation date.
* **Delete Job (`DELETE /jobs/{id}`):** Allows admins or employers to delete job postings.

### 5.3. Job Application Flow
* **Apply (`POST /apply`):** Job Seekers apply to a job (restricted to role `seeker`). Checks for duplicate applications to prevent multiple applications for the same job.
* **My Applications (`GET /my-applications`):** Retrieves applications for the currently logged-in seeker, joining job title, company, and location attributes.
* **Review Applicants (`GET /job-applicants/{jobId}`):** Allows employers to retrieve all candidates who applied to their job posting, displaying candidate names, emails, and dates.
* **Update Status (`POST /update-status`):** Allows employers to mark applications as `'Shortlisted'` or `'Rejected'`.

### 5.4. Resume Management Flow
* **Upload Resume (`POST /upload-resume`):** Accepts multipart form-data. Restricts uploads to PDF and DOCX, capping size at 5MB. Saves to `uploads/` directory relative to execution, naming file as `resume_{userId}.pdf/docx`, and updates the user's `resumePath`.
* **Check Status (`GET /my-resume`):** Returns whether a resume is present.
* **Download Resume (`GET /resume/{seekerId}`):** Allows employers to request a resume download. Streams binary data and forces browser download using `Content-Disposition: attachment`.
