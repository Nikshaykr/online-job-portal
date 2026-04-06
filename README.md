# 🚀 Online Job Portal

A full-stack Online Job Portal web application built as a BCA final year college project.

**Built by:** Deepak Rajpoot & Nikshay Kumar Singh  
**College:** Gagan College of Management & Technology, Aligarh  
**Course:** Bachelor of Computer Application (2023-26)

---

## 📌 Features

- ✅ Register and Login for 3 user roles (Job Seeker, Employer, Admin)
- ✅ Employers can post job listings
- ✅ Job Seekers can search, filter and apply for jobs
- ✅ Employers can view applicants and shortlist/reject them
- ✅ Application status tracking for job seekers
- ✅ Admin panel to manage all users and jobs
- ✅ Resume upload for job seekers (PDF/DOCX)
- ✅ Clean dark-themed responsive UI

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Frontend | HTML5, CSS3, JavaScript |
| Backend | Java 21, Spring Boot 3.2 |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA + Hibernate |
| Build Tool | Maven |

---

## 📁 Project Structure

```
job-portal/
├── backend/               ← Spring Boot Maven project
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/jobportal/
│       │   ├── model/         User.java, Job.java, Application.java
│       │   ├── repository/    UserRepository, JobRepository, ApplicationRepository
│       │   ├── controller/    AuthController, JobController, ApplicationController, AdminController
│       │   └── config/        CorsConfig.java
│       └── resources/
│           └── application.properties.example
└── frontend/              ← Static HTML pages
    ├── index.html
    ├── register.html
    ├── login.html
    ├── jobs.html
    ├── seeker-dashboard.html
    ├── employer-dashboard.html
    ├── admin-dashboard.html
    ├── style.css
    └── app.js
```

---

## ⚙️ How to Run

### Prerequisites
- Java 21
- Maven
- MySQL 8.0
- VS Code with Live Server extension

### Step 1 — Setup Database
```sql
CREATE DATABASE job_portal;
```

### Step 2 — Configure application.properties
```bash
cd backend/src/main/resources
cp application.properties.example application.properties
# Edit application.properties and set your MySQL password
```

### Step 3 — Start Backend
```bash
cd backend
mvn spring-boot:run
```
Backend runs at `http://localhost:8080`

### Step 4 — Start Frontend
Open `frontend/index.html` with VS Code Live Server.  
Frontend runs at `http://127.0.0.1:5500`

### Step 5 — Seed Admin User
```sql
USE job_portal;
INSERT INTO users (name, email, password, role)
VALUES ('Admin', 'admin@portal.com', 'admin123', 'admin');
```

---

## 👥 Test Accounts

| Role | Email | Password |
|---|---|---|
| Job Seeker | seeker@test.com | 123 |
| Employer | employer@test.com | 123 |
| Admin | admin@portal.com | admin123 |

---

## 📡 API Endpoints

| Method | URL | Access | Description |
|---|---|---|---|
| POST | /register | Public | Register new user |
| POST | /login | Public | Login |
| POST | /logout | Any | Logout |
| GET | /jobs | Public | Get all jobs |
| POST | /jobs/add | Employer | Post a job |
| POST | /apply | Seeker | Apply to a job |
| GET | /my-applications | Seeker | Get my applications |
| GET | /job-applicants/{id} | Employer | View applicants |
| POST | /update-status | Employer | Shortlist/Reject |
| GET | /admin/users | Admin | All users |
| GET | /admin/jobs | Admin | All jobs |
| DELETE | /admin/jobs/{id} | Admin | Delete job |