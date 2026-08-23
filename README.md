# 🚀 Backend Service - NetPay Manager

This repository contains the business logic, REST API endpoints, and database versioning for the **[Your Project Name]** application. 

Built using **Spring Boot**, it follows modern software development best practices, featuring a clean, decoupled architecture ready to serve the frontend client.

## 🛠️ Tech Stack
* **Java 17 / 21** - Core development language.
* **Spring Boot 3.x** - Framework for building the RESTful API.
* **Spring Data JPA & Hibernate** - Object-Relational Mapping (ORM) and data abstraction.
* **PostgreSQL** - Relational database management system.
* **Flyway** - Database migration tool and schema version control.
* **Docker & Docker Compose** - Containerization and local development orchestration.

## 📁 Database Migrations
All database schema changes are managed via SQL scripts located under:
`src/main/resources/db/migration/`

Every time the Spring Boot application boots up, **Flyway** automatically validates and runs pending migrations safely on the PostgreSQL instance.

## ⚙️ Local Development Setup
To spin up the isolated database container, run the following command in the root folder:
```bash
docker compose up -d
```
This boots up PostgreSQL on port `5432`. The project includes **Spring Boot DevTools** for faster development via *Hot Reload*.
