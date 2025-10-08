# SmartCampus AI - Backend Service

This repository contains the source code for the backend REST API of the SmartCampus AI project, a proactive educational ERP designed to enhance student success.

## ✨ Features

° Comprehensive REST API: Provides complete CRUD operations for all core modules, including admissions, user management, academics, and risk analytics.

° Secure Authentication: Utilizes Spring Security and JSON Web Tokens (JWTs) for stateless, secure user authentication.

° Role-Based Access Control (RBAC): Granular permission system for different user roles like administrators, teachers, and students.

° Service-Oriented Architecture: Clean separation of concerns between Controller, Service, and Repository layers.

° Data Seeding: Includes a command-line runner to seed the database with initial data, such as the first admin user.

## 🛠️ Tech Stack

° Framework: Spring Boot

° Language: Java

° Security: Spring Security, JWT

° Database: PostgreSQL with Hibernate (JPA)

° Build Tool: Maven

° Containerization: Docker

## 🚀 Getting Started

### Prerequisites

JDK 17 or later

Maven

PostgreSQL Database

### Installation
1. Clone the repository:
git clone https://github.com/viru0909-dev/ERP_Backend.git
2. Configure your database credentials in application.properties.
3. Run the application:
mvn spring-boot:run

