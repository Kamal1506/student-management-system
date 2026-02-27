# 🎓 Student Management System

A secure, role-based academic management platform built with **Java**, **Spring Boot**, **Spring Security (JWT)**, **Hibernate**, and **MySQL**.

---

## 🏗 Tech Stack

| Layer       | Technology                        |
|-------------|-----------------------------------|
| Language    | Java 17                           |
| Framework   | Spring Boot 3.x                   |
| Security    | Spring Security + JWT (jjwt)      |
| ORM         | Hibernate / Spring Data JPA       |
| Database    | MySQL 8                           |
| Build Tool  | Maven                             |
| Frontend    | HTML, CSS, Vanilla JS             |

---

## 👥 User Roles

| Role    | Permissions                                              |
|---------|----------------------------------------------------------|
| ADMIN   | Full CRUD — students, teachers, courses, enrollments     |
| TEACHER | Manage assignments, grade exams, view enrolled students  |
| STUDENT | View courses, assignments, grades, mark tasks complete   |

---

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/kamal1506/student-management-system.git
cd student-management-system
```

### 2. Set up environment variables
```bash
# Copy the example file
cp .env.example .env

# Edit .env with your actual values
nano .env   # or open in any editor
```

### 3. Create the MySQL database
```sql
mysql -u root -p
CREATE DATABASE sms_db;
EXIT;
```

### 4. Configure application.properties
```bash
# Copy the template
cp src/main/resources/application.properties.template \
   src/main/resources/application.properties

# The template already reads from environment variables.
# Just make sure your .env values are set correctly.
```

### 5. Generate a JWT secret
```bash
openssl rand -base64 32
# Paste the output as JWT_SECRET in your .env
```

### 6. Run the application
```bash
./mvnw spring-boot:run
```

Hibernate will **auto-create all database tables** on first startup.

---

## 🗄 Database Schema

```
users          → login accounts (username, password, role)
students       → student profiles (links to users)
teachers       → teacher profiles (links to users)
courses        → course catalog (links to teachers)
enrollments    → student ↔ course mapping
assignments    → created by teachers per course
exam_results   → grades per student per course
```

---

## 🔐 API Endpoints

### Auth (Public)
```
POST /api/auth/register   → Create user account
POST /api/auth/login      → Returns JWT token
```

### Admin (Token required, ADMIN role)
```
GET    /api/admin/students         → List all students
POST   /api/admin/students         → Add student
PUT    /api/admin/students/{id}    → Update student
DELETE /api/admin/students/{id}    → Delete student
POST   /api/admin/teachers         → Add teacher
POST   /api/admin/enroll           → Enroll student in course
GET    /api/admin/stats            → Dashboard statistics
```

### Teacher (Token required, TEACHER role)
```
GET    /api/teacher/courses                    → My courses
GET    /api/teacher/courses/{id}/students      → Students in course
POST   /api/teacher/assignments                → Create assignment
POST   /api/teacher/grades                     → Grade student
```

### Student (Token required, STUDENT role)
```
GET    /api/student/courses                    → My enrolled courses
GET    /api/student/assignments                → My assignments
PUT    /api/student/assignments/{id}/complete  → Mark complete
GET    /api/student/grades                     → My grades
```

---

## 🔑 Authentication

All protected endpoints require a Bearer token in the Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Get a token by calling `POST /api/auth/login` with valid credentials.

---

## 📁 Project Structure

```
src/main/java/com/sms/
├── config/         → SecurityConfig, CorsConfig
├── controller/     → REST controllers per role
├── dto/            → Request / Response objects
├── enums/          → Role, AssignmentStatus
├── exception/      → GlobalExceptionHandler
├── model/          → JPA entities
├── repository/     → Spring Data JPA interfaces
├── security/       → JwtUtil, JwtAuthFilter, UserDetailsService
└── service/        → Business logic
```

---

## ⚙️ Environment Variables Reference

| Variable        | Description                      | Example                          |
|-----------------|----------------------------------|----------------------------------|
| `DB_URL`        | MySQL JDBC connection URL        | `jdbc:mysql://localhost:3306/sms_db` |
| `DB_USERNAME`   | MySQL username                   | `root`                           |
| `DB_PASSWORD`   | MySQL password                   | `yourpassword`                   |
| `JWT_SECRET`    | Base64-encoded signing secret    | output of `openssl rand -base64 32` |
| `JWT_EXPIRATION`| Token expiry in milliseconds     | `86400000` (24 hours)            |

---

## 🛡 Security Notes

- Passwords are hashed using **BCrypt** — never stored as plain text
- JWT tokens are **stateless** — no server-side sessions
- Each role can only access its own endpoints
- CORS is configured for local development — update origins for production

---

## 📄 License

MIT License — feel free to use and modify for your own projects.