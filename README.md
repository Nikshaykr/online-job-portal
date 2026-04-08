# Online Job Portal

A comprehensive web-based platform designed to facilitate the job recruitment process. This project was developed as part of the BCA final year curriculum to demonstrate full-stack development capabilities using Java Spring Boot and vanilla web technologies.

**Project Contributors:** Deepak Rajpoot & Nikshay Kumar Singh  
**Institution:** Gagan College of Management & Technology, Aligarh  
**Batch:** 2023-2026

---

## Core Functionality

*   **Role-Based Access Control:** Secure login and registration for Job Seekers, Employers, and Administrators.
*   **Job Management:** Employers can create, update, and manage job listings.
*   **Application Lifecycle:** Job seekers can search for opportunities, apply with resumes, and track their application status in real-time.
*   **Applicant Tracking:** Employers can review candidate profiles, download resumes, and update application statuses (Shortlisted/Rejected).
*   **Administration:** A dedicated dashboard for managing system users and monitoring active job listings.
*   **Resume Support:** Integration for PDF and DOCX file uploads for candidate profiles.

---

## Technical Specifications

| Component | Technology |
|:---|:---|
| **Frontend** | HTML5, CSS3 (Custom Styles), JavaScript (ES6+) |
| **Backend** | Java 21, Spring Boot 3.2 |
| **Database** | MySQL 8.0 |
| **Persistence** | Spring Data JPA / Hibernate |
| **Build Tool** | Maven 3.9+ |

---

## Project Architecture

```text
job-portal/
├── backend/               # Spring Boot Application
│   ├── src/main/java/     # Source code (MVC Pattern)
│   └── src/main/resources/# Configuration & Properties
└── frontend/              # User Interface
    ├── *.html             # Specialized views for Seeker, Employer, and Admin
    ├── style.css          # Design and Layout
    └── app.js             # Client-side logic and API integration
```

---

## Installation and Setup

### 1. Database Initialization
Create a new MySQL schema for the application:
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
mvn spring-boot:run
```
*The server will initialize at `http://localhost:8080`*

**Launching the Frontend:**
Open `frontend/index.html` using a local web server (e.g., VS Code Live Server).
*Default URL: `http://127.0.0.1:5500`*

### 4. Administrative Setup
To access the admin panel, seed the initial administrator account:
```sql
USE job_portal;
INSERT INTO users (name, email, password, role)
VALUES ('System Admin', 'admin@portal.com', 'admin123', 'admin');
```

---

## Access Credentials

| Role | Username / Email | Password |
|:---|:---|:---|
| **Administrator** | admin@portal.com | admin123 |
| **Employer** | employer@test.com | 123 |
| **Job Seeker** | seeker@test.com | 123 |

---

## API Documentation

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