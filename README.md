# Learning Management System (LMS)

A comprehensive Learning Management System built with Spring Boot, Thymeleaf, MySQL, and Cloudinary. The platform supports three user roles: Admin, Instructor, and Student with full-featured course management, enrollment, quiz system, and payment integration.

## 🚀 Features

### 👨‍💼 Admin Module
- User management (view, edit, delete, role assignment)
- Course approval workflow
- System-wide analytics
- User status management (enable/disable)

### 👨‍🏫 Instructor Module
- Course creation and management
- Lesson creation (VIDEO, PDF, IMAGE, TEXT, QUIZ)
- Student enrollment tracking
- Quiz creation with multiple question types
- Course analytics and performance metrics

### 👨‍🎓 Student Module
- Browse and search courses
- Course enrollment (free courses auto-enroll, paid via PayPal)
- Video lesson playback
- PDF and image resource viewing
- Quiz taking with timer
- Progress tracking across courses
- Learning dashboard

### 💳 Payment Integration
- PayPal sandbox integration
- Free courses auto-enrollment
- Paid courses require payment before access

### 📁 Media Management
- Cloudinary integration for file storage
- Support for videos, PDFs, images
- Automatic thumbnail generation
- Media library per course

## 🛠️ Technology Stack

- **Backend:** Java 17, Spring Boot 3.x, Spring Security, JPA/Hibernate
- **Frontend:** Thymeleaf, Tailwind CSS, JavaScript
- **Database:** MySQL 8
- **Media Storage:** Cloudinary
- **Payment:** PayPal SDK
- **Authentication:** JWT
- **Build Tool:** Maven
- **Deployment:** Render

## 📋 Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.8+
- Git
- Cloudinary account (free tier)
- PayPal Developer account

## 🚀 Local Development Setup

### 1. Clone the Repository
```bash
git clone https://github.com/Manjula1042004/learning-management-system.git
cd learning-management-system