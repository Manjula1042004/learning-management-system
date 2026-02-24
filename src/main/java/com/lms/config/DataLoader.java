package com.lms.config;

import com.lms.entity.User;
import com.lms.entity.Role;
import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.entity.LessonType;
import com.lms.entity.CourseStatus;
import com.lms.entity.Enrollment;
import com.lms.entity.EnrollmentStatus;
import com.lms.service.UserService;
import com.lms.service.CourseService;
import com.lms.service.LessonService;
import com.lms.service.EnrollmentService;
import com.lms.repository.CourseRepository;
import com.lms.repository.LessonRepository;
import com.lms.repository.EnrollmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserService userService;
    private final CourseRepository courseRepository;
    private final LessonService lessonService;
    private final CourseService courseService;
    private final LessonRepository lessonRepository;
    private final EnrollmentService enrollmentService;
    private final EnrollmentRepository enrollmentRepository;

    public DataLoader(UserService userService, CourseRepository courseRepository,
                      LessonService lessonService, CourseService courseService,
                      LessonRepository lessonRepository,
                      EnrollmentService enrollmentService,
                      EnrollmentRepository enrollmentRepository) {
        this.userService = userService;
        this.courseRepository = courseRepository;
        this.lessonService = lessonService;
        this.courseService = courseService;
        this.lessonRepository = lessonRepository;
        this.enrollmentService = enrollmentService;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("==========================================");
        System.out.println("🚀 PROFESSIONAL LMS - CORRECTED CONTENT");
        System.out.println("==========================================");

        // 1. Create users first
        System.out.println("\n1️⃣ CREATING DEFAULT USERS...");
        createDefaultUsers();

        // 2. DELETE the old free course if it exists
        System.out.println("\n2️⃣ CLEANING UP OLD COURSES...");
        deleteOldFreeCourse();

        // 3. Create JAVA & SPRING BOOT as the DEFAULT FREE COURSE
        System.out.println("\n3️⃣ CREATING JAVA & SPRING BOOT AS DEFAULT FREE COURSE...");
        createJavaSpringAsDefaultFreeCourse();

        // 4. Create other PROFESSIONAL backend courses (PAID courses)
        System.out.println("\n4️⃣ CREATING OTHER PAID COURSES...");
        createOtherPaidCourses();

        // 5. Fix any URL issues
        System.out.println("\n5️⃣ SETTING UP VIDEO LINKS...");
        convertYouTubeUrlsToEmbed();

        System.out.println("\n==========================================");
        System.out.println("🎉 DATA LOADING COMPLETE!");
        System.out.println("✅ Java & Spring Boot Masterclass is now FREE and auto-enrolled for all students");
        System.out.println("✅ 5 Other Professional PAID courses (require PayPal payment)");
        System.out.println("✅ Students must pay to access other courses");
        System.out.println("==========================================");
    }

    // ============================================================
    // DELETE OLD FREE COURSE
    // ============================================================
    @Transactional
    private void deleteOldFreeCourse() {
        System.out.println("\n🧹 CHECKING FOR OLD FREE COURSE TO DELETE...");

        // Find all courses with "FREE" in the title or price = 0
        List<Course> allCourses = courseRepository.findAll();

        for (Course course : allCourses) {
            if (course.getTitle().contains("Introduction to Learning Management Systems") ||
                    (course.getPrice() == 0.0 && !course.getTitle().contains("Java"))) {

                System.out.println("🗑️ Found old free course to delete: " + course.getTitle());

                try {
                    // Delete the course (cascade will handle lessons and enrollments)
                    courseRepository.delete(course);
                    System.out.println("✅ Deleted old course: " + course.getTitle());
                } catch (Exception e) {
                    System.out.println("⚠️ Error deleting course: " + e.getMessage());
                }
            }
        }
    }

    // ============================================================
    // CREATE JAVA & SPRING BOOT AS DEFAULT FREE COURSE
    // ============================================================
    @Transactional
    private void createJavaSpringAsDefaultFreeCourse() {
        System.out.println("\n🎯 CREATING JAVA & SPRING BOOT AS DEFAULT FREE COURSE...");

        // Find or create an instructor for the default course
        User defaultInstructor = userService.getUserByUsername("instructor1")
                .orElseGet(() -> {
                    try {
                        return userService.createUser("default_instructor", "default@lms.com", "instructor123", Role.INSTRUCTOR);
                    } catch (Exception e) {
                        return null;
                    }
                });

        if (defaultInstructor == null) {
            System.out.println("⚠ Could not find/create default instructor");
            return;
        }

        // Check if Java course already exists
        String javaCourseTitle = "Java & Spring Boot Masterclass 2024 (FREE)";
        List<Course> existingCourses = courseRepository.findByInstructor(defaultInstructor);

        // First, try to find existing Java course to update it
        Course javaCourse = null;
        for (Course course : existingCourses) {
            if (course.getTitle().contains("Java & Spring Boot Masterclass")) {
                javaCourse = course;
                break;
            }
        }

        if (javaCourse == null) {
            try {
                // Create the Java course as FREE
                javaCourse = new Course(
                        javaCourseTitle,
                        "Master Java programming and Spring Boot framework from beginner to advanced. Learn REST APIs, Spring Security, JPA/Hibernate, Microservices, and build real-world applications. Includes hands-on projects and industry best practices. THIS COURSE IS NOW FREE FOR ALL STUDENTS!",
                        0.0, // FREE now
                        defaultInstructor
                );
                javaCourse.setThumbnailUrl("https://images.unsplash.com/photo-1542831371-29b0f74f9713?w=1200&h=675&fit=crop");
                javaCourse.setStatus(CourseStatus.APPROVED);
                javaCourse = courseRepository.save(javaCourse);

                System.out.println("✅ Java & Spring Boot course created as FREE: " + javaCourse.getTitle());

                // Add lessons to the Java course
                createJavaCourseLessons(javaCourse);

            } catch (Exception e) {
                System.out.println("❌ Error creating Java course: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        } else {
            // Update existing Java course to be FREE
            javaCourse.setPrice(0.0);
            javaCourse.setTitle("Java & Spring Boot Masterclass 2024 (FREE)");
            javaCourse.setDescription("Master Java programming and Spring Boot framework from beginner to advanced. Learn REST APIs, Spring Security, JPA/Hibernate, Microservices, and build real-world applications. Includes hands-on projects and industry best practices. THIS COURSE IS NOW FREE FOR ALL STUDENTS!");
            courseRepository.save(javaCourse);
            System.out.println("✅ Updated existing Java course to FREE: " + javaCourse.getTitle());
        }

        // Auto-enroll all existing students in the Java free course
        List<User> allStudents = userService.getUsersByRole(Role.STUDENT);
        int enrolledCount = 0;

        for (User student : allStudents) {
            if (!enrollmentService.isStudentEnrolled(student, javaCourse)) {
                try {
                    enrollmentService.enrollStudent(student, javaCourse, "FREE_JAVA_COURSE");
                    enrolledCount++;
                } catch (Exception e) {
                    System.out.println("⚠ Could not enroll student " + student.getUsername() + ": " + e.getMessage());
                }
            }
        }

        System.out.println("✅ Auto-enrolled " + enrolledCount + " students in Java & Spring Boot FREE course");
    }

    private void createJavaCourseLessons(Course course) {
        List<ProfessionalLesson> lessons = new ArrayList<>();

        lessons.add(new ProfessionalLesson(
                "1. Java Introduction & Setup",
                "Learn Java basics, install JDK, setup IDE (Eclipse/IntelliJ)",
                25, LessonType.VIDEO,
                "https://www.youtube.com/watch?v=eIrMbAQSU34", null, null
        ));

        lessons.add(new ProfessionalLesson(
                "2. Object-Oriented Programming in Java",
                "Classes, Objects, Inheritance, Polymorphism concepts",
                28, LessonType.VIDEO,
                "https://www.youtube.com/watch?v=8cm1x4bC610", null, null
        ));

        lessons.add(new ProfessionalLesson(
                "3. Spring Boot Introduction",
                "Spring Boot setup, auto-configuration, starters",
                22, LessonType.VIDEO,
                "https://www.youtube.com/watch?v=vtPkZShrvXQ", null, null
        ));

        lessons.add(new ProfessionalLesson(
                "4. Spring REST API Development",
                "Create RESTful APIs with Spring MVC",
                30, LessonType.VIDEO,
                "https://www.youtube.com/watch?v=9SGDpanrc8U", null, null
        ));

        lessons.add(new ProfessionalLesson(
                "5. Spring Data JPA & Hibernate",
                "Database connectivity with JPA and Hibernate",
                35, LessonType.VIDEO,
                "https://www.youtube.com/watch?v=8jazNUpO3lQ", null, null
        ));

        lessons.add(new ProfessionalLesson(
                "6. Spring Security Implementation",
                "Authentication and authorization with Spring Security",
                32, LessonType.VIDEO,
                "https://www.youtube.com/watch?v=her_7pa0vrg", null, null
        ));

        lessons.add(new ProfessionalLesson(
                "7. Microservices with Spring Boot",
                "Building microservices architecture",
                40, LessonType.VIDEO,
                "https://www.youtube.com/watch?v=BnknNTN8icw", null, null
        ));

        lessons.add(new ProfessionalLesson(
                "8. Spring Boot Testing",
                "Unit testing and integration testing",
                25, LessonType.VIDEO,
                "https://www.youtube.com/watch?v=Geq60OVyBPg", null, null
        ));

        lessons.add(new ProfessionalLesson(
                "9. Deployment with Docker",
                "Containerize Spring Boot applications",
                28, LessonType.VIDEO,
                "https://www.youtube.com/watch?v=5TGBJp_9r1M", null, null
        ));

        lessons.add(new ProfessionalLesson(
                "10. Real Project Building",
                "Build complete Java Spring application",
                45, LessonType.VIDEO,
                "https://www.youtube.com/watch?v=9rG3x9Jdmr4", null, null
        ));

        // Add a PDF resource
        lessons.add(new ProfessionalLesson(
                "Java Spring Boot Cheat Sheet",
                "Quick reference for Spring Boot annotations",
                5, LessonType.PDF,
                null, "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf", null
        ));

        try {
            for (ProfessionalLesson lesson : lessons) {
                lessonService.createLesson(
                        lesson.title,
                        lesson.description,
                        lesson.duration,
                        lesson.type,
                        course,
                        null, lesson.videoUrl, null, lesson.pdfUrl, null, null
                );
            }
            System.out.println("✅ Added " + lessons.size() + " lessons to Java & Spring Boot course");
        } catch (Exception e) {
            System.out.println("⚠ Error creating lessons for Java course: " + e.getMessage());
        }
    }

    // ============================================================
    // USER CREATION METHOD
    // ============================================================
    private void createDefaultUsers() {
        System.out.println("Creating default users...");

        // Create admin user if not exists
        if (userService.getUserByUsername("admin").isEmpty()) {
            try {
                User admin = userService.createUser("admin", "admin@lms.com", "admin123", Role.ADMIN);
                admin.setFirstName("System");
                admin.setLastName("Administrator");
                admin.setBio("System Administrator with full access");
                System.out.println("✓ Admin user created: " + admin.getUsername());
            } catch (Exception e) {
                System.out.println("⚠ Admin user already exists or error: " + e.getMessage());
            }
        }

        // Create sample instructors
        String[] instructorNames = {"john", "emma", "david", "sarah", "michael"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones"};
        String[] expertise = {
                "Java & Spring Boot Expert | 10+ years experience",
                "Full Stack Web Developer | React & Node.js Specialist",
                "Data Scientist & ML Engineer | Python Expert",
                "Cloud & DevOps Engineer | AWS Certified",
                "Mobile App Developer | React Native & Flutter"
        };

        String[] instructorEmails = {
                "john.smith@lms.com",
                "emma.johnson@lms.com",
                "david.williams@lms.com",
                "sarah.brown@lms.com",
                "michael.jones@lms.com"
        };

        for (int i = 0; i < instructorNames.length; i++) {
            String username = "instructor" + (i + 1);
            if (userService.getUserByUsername(username).isEmpty()) {
                try {
                    User instructor = userService.createUser(username,
                            instructorEmails[i], "instructor123", Role.INSTRUCTOR);
                    instructor.setFirstName(instructorNames[i]);
                    instructor.setLastName(lastNames[i]);
                    instructor.setBio(expertise[i]);
                    instructor.setProfilePictureUrl("https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400&h=400&fit=crop");
                    System.out.println("✓ Instructor created: " + instructor.getUsername() + " - " + expertise[i]);
                } catch (Exception e) {
                    System.out.println("⚠ Instructor " + (i + 1) + " already exists: " + e.getMessage());
                }
            }
        }

        // Create sample students
        String[] studentFirstNames = {"Alice", "Bob", "Charlie", "Diana", "Ethan", "Fiona", "George", "Hannah"};
        String[] studentLastNames = {"Taylor", "Miller", "Davis", "Wilson", "Moore", "Clark", "Lewis", "Walker"};
        String[] studentEmails = {
                "alice.taylor@student.com",
                "bob.miller@student.com",
                "charlie.davis@student.com",
                "diana.wilson@student.com",
                "ethan.moore@student.com",
                "fiona.clark@student.com",
                "george.lewis@student.com",
                "hannah.walker@student.com"
        };

        for (int i = 1; i <= 8; i++) {
            String username = "student" + i;
            if (userService.getUserByUsername(username).isEmpty()) {
                try {
                    User student = userService.createUser(username,
                            studentEmails[i-1], "student123", Role.STUDENT);
                    student.setFirstName(studentFirstNames[i-1]);
                    student.setLastName(studentLastNames[i-1]);
                    student.setBio("Computer Science Student | Passionate about learning new technologies");
                    System.out.println("✓ Student created: " + student.getUsername() + " - " + student.getFullName());
                } catch (Exception e) {
                    System.out.println("⚠ Student " + i + " already exists: " + e.getMessage());
                }
            }
        }
    }

    // ============================================================
    // CREATE OTHER PAID COURSES (all except Java)
    // ============================================================
    @Transactional
    private void createOtherPaidCourses() {
        System.out.println("\n🎯 CREATING OTHER PAID COURSES...");

        List<User> allInstructors = userService.getUsersByRole(Role.INSTRUCTOR);
        System.out.println("Found " + allInstructors.size() + " instructors");

        if (allInstructors.isEmpty()) {
            System.out.println("⚠ No instructors found. Creating courses for admin...");
            User admin = userService.getUserByUsername("admin").orElseThrow();
            allInstructors.add(admin);
        }

        // Get all paid course templates (excluding Java)
        List<ProfessionalCourseTemplate> paidTemplates = getOtherPaidCourseTemplates();
        System.out.println("Prepared " + paidTemplates.size() + " other paid courses");

        int instructorIndex = 0;
        for (ProfessionalCourseTemplate template : paidTemplates) {
            User instructor = allInstructors.get(instructorIndex % allInstructors.size());

            try {
                System.out.println("\n📚 Creating PAID course: " + template.title);
                System.out.println("   Instructor: " + instructor.getUsername());
                System.out.println("   Price: $" + template.price);

                List<Course> existingCourses = courseRepository.findByInstructor(instructor);
                boolean alreadyHasCourse = existingCourses.stream()
                        .anyMatch(c -> c.getTitle().equals(template.title));

                if (!alreadyHasCourse) {
                    Course course = createCourseFromTemplate(template, instructor);
                    createLessonsForProfessionalCourse(course, template.lessons);
                    System.out.println("   ✅ Created PAID course successfully with " + template.lessons.size() + " lessons");
                    System.out.println("   ⚠ NOTE: This course is NOT auto-enrolled - students must pay via PayPal");
                } else {
                    System.out.println("   ⚠ Instructor already has this course, skipping");
                }

            } catch (Exception e) {
                System.out.println("   ❌ Error: " + e.getMessage());
                e.printStackTrace();
            }

            instructorIndex++;
        }

        System.out.println("\n✅ OTHER PAID COURSES CREATED!");
    }

    // ============================================================
    // PAID COURSE TEMPLATES (all except Java)
    // ============================================================
    private List<ProfessionalCourseTemplate> getOtherPaidCourseTemplates() {
        List<ProfessionalCourseTemplate> templates = new ArrayList<>();

        // COURSE 2: Python Django Backend Development (PAID)
        templates.add(new ProfessionalCourseTemplate(
                "Python Django Backend Development - Complete Course",
                "Learn Python web development with Django framework. Build RESTful APIs, work with databases, implement authentication, and deploy applications. Perfect for full-stack developers and backend engineers.",
                129.99,
                "https://images.unsplash.com/photo-1526379879527-8559ecfcaec7?w=1200&h=675&fit=crop",
                "PYTHON_DJANGO",
                getPythonDjangoLessonsWithRelevantVideos()
        ));

        // COURSE 3: Node.js & Express.js Backend (PAID)
        templates.add(new ProfessionalCourseTemplate(
                "Node.js & Express.js - Modern Backend Development",
                "Master Node.js runtime, Express.js framework, MongoDB, authentication, and building scalable REST APIs. Learn async programming, error handling, and deployment strategies.",
                139.99,
                "https://images.unsplash.com/photo-1633356122102-3fe601e05bd2?w=1200&h=675&fit=crop",
                "NODE_EXPRESS",
                getNodeExpressLessonsWithRelevantVideos()
        ));

        // COURSE 4: PHP Laravel Backend (PAID)
        templates.add(new ProfessionalCourseTemplate(
                "PHP Laravel - Complete Backend Developer Guide",
                "Learn modern PHP with Laravel framework. Master Eloquent ORM, Blade templates, API development, authentication, and build enterprise-grade applications following MVC pattern.",
                119.99,
                "https://images.unsplash.com/photo-1551650975-87deedd944c3?w=1200&h=675&fit=crop",
                "PHP_LARAVEL",
                getPhpLaravelLessonsWithRelevantVideos()
        ));

        // COURSE 5: .NET Core Backend (PAID)
        templates.add(new ProfessionalCourseTemplate(
                ".NET Core & C# - Enterprise Backend Development",
                "Build robust backend systems with .NET Core and C#. Learn Entity Framework Core, Web APIs, authentication/authorization, clean architecture, and microservices patterns.",
                159.99,
                "https://images.unsplash.com/photo-1618401471353-b98afee0b2eb?w=1200&h=675&fit=crop",
                "DOTNET_CORE",
                getDotNetCoreLessonsWithRelevantVideos()
        ));

        // COURSE 6: Database Design & SQL (PAID)
        templates.add(new ProfessionalCourseTemplate(
                "Database Design & SQL Mastery - From Basics to Advanced",
                "Master relational database design, SQL queries, optimization, normalization, transactions, and working with MySQL/PostgreSQL. Essential for every backend developer.",
                99.99,
                "https://images.unsplash.com/photo-1545062080-a71640ea75a1?w=1200&h=675&fit=crop",
                "DATABASE_SQL",
                getDatabaseSqlLessonsWithRelevantVideos()
        ));

        return templates;
    }

    // ============================================================
    // PYTHON DJANGO LESSONS
    // ============================================================
    private List<ProfessionalLesson> getPythonDjangoLessonsWithRelevantVideos() {
        List<ProfessionalLesson> lessons = new ArrayList<>();
        lessons.add(new ProfessionalLesson("1. Python Basics for Web Development", "Python syntax, functions, and data structures", 20, LessonType.VIDEO, "https://www.youtube.com/watch?v=rfscVS0vtbw", null, null));
        lessons.add(new ProfessionalLesson("2. Django Framework Introduction", "Django installation and project setup", 25, LessonType.VIDEO, "https://www.youtube.com/watch?v=rHux0gMZ3Eg", null, null));
        lessons.add(new ProfessionalLesson("3. Django Models & Database", "Creating models and database migrations", 28, LessonType.VIDEO, "https://www.youtube.com/watch?v=q5k8zH-7KxI", null, null));
        lessons.add(new ProfessionalLesson("4. Django Views & URL Routing", "Function-based and class-based views", 22, LessonType.VIDEO, "https://www.youtube.com/watch?v=Kc2m2QzkbCE", null, null));
        lessons.add(new ProfessionalLesson("5. Django Templates & Frontend", "Template language and frontend integration", 24, LessonType.VIDEO, "https://www.youtube.com/watch?v=HshbjK1vDtY", null, null));
        lessons.add(new ProfessionalLesson("6. Django REST Framework APIs", "Building REST APIs with DRF", 35, LessonType.VIDEO, "https://www.youtube.com/watch?v=c708Nf0cHrs", null, null));
        lessons.add(new ProfessionalLesson("7. User Authentication & Permissions", "User registration, login, and permissions", 30, LessonType.VIDEO, "https://www.youtube.com/watch?v=UmljXZIikDc", null, null));
        lessons.add(new ProfessionalLesson("8. Django Testing", "Writing tests for Django applications", 26, LessonType.VIDEO, "https://www.youtube.com/watch?v=6-xH3L0MrM8", null, null));
        lessons.add(new ProfessionalLesson("9. Django Deployment", "Deploy Django apps to production", 32, LessonType.VIDEO, "https://www.youtube.com/watch?v=Y4M4z7j0LEI", null, null));
        lessons.add(new ProfessionalLesson("10. Building a Blog with Django", "Complete Django project tutorial", 45, LessonType.VIDEO, "https://www.youtube.com/watch?v=UmljXZIikDc", null, null));
        return lessons;
    }

    // ============================================================
    // NODE.JS & EXPRESS.JS LESSONS
    // ============================================================
    private List<ProfessionalLesson> getNodeExpressLessonsWithRelevantVideos() {
        List<ProfessionalLesson> lessons = new ArrayList<>();
        lessons.add(new ProfessionalLesson("1. Node.js Fundamentals", "Node.js runtime, modules, and NPM", 22, LessonType.VIDEO, "https://www.youtube.com/watch?v=TlB_eWDSMt4", null, null));
        lessons.add(new ProfessionalLesson("2. Express.js Framework Setup", "Creating Express server and routing", 25, LessonType.VIDEO, "https://www.youtube.com/watch?v=L72fhGm1tfE", null, null));
        lessons.add(new ProfessionalLesson("3. MongoDB with Mongoose", "Connect Node.js to MongoDB database", 30, LessonType.VIDEO, "https://www.youtube.com/watch?v=DZBGEVgL2eE", null, null));
        lessons.add(new ProfessionalLesson("4. REST API Development", "Build RESTful APIs with Node.js", 35, LessonType.VIDEO, "https://www.youtube.com/watch?v=fgTGADljAeg", null, null));
        lessons.add(new ProfessionalLesson("5. JWT Authentication", "Implement JWT authentication in Node.js", 32, LessonType.VIDEO, "https://www.youtube.com/watch?v=mbsmsi7l3r4", null, null));
        lessons.add(new ProfessionalLesson("6. Middleware & Error Handling", "Express middleware and error handling", 28, LessonType.VIDEO, "https://www.youtube.com/watch?v=9U3IhLAnSxM", null, null));
        lessons.add(new ProfessionalLesson("7. File Upload with Multer", "Handle file uploads in Node.js", 26, LessonType.VIDEO, "https://www.youtube.com/watch?v=srPXMt1Q0nY", null, null));
        lessons.add(new ProfessionalLesson("8. WebSockets with Socket.io", "Real-time communication with Socket.io", 34, LessonType.VIDEO, "https://www.youtube.com/watch?v=8Y6mWhcdSUM", null, null));
        lessons.add(new ProfessionalLesson("9. Testing Node.js Applications", "Unit testing with Jest and Mocha", 29, LessonType.VIDEO, "https://www.youtube.com/watch?v=r4xbosQuI3w", null, null));
        lessons.add(new ProfessionalLesson("10. Deployment & Performance", "Deploy Node.js apps and optimization", 38, LessonType.VIDEO, "https://www.youtube.com/watch?v=7Uaw5hPCFXc", null, null));
        return lessons;
    }

    // ============================================================
    // PHP LARAVEL LESSONS
    // ============================================================
    private List<ProfessionalLesson> getPhpLaravelLessonsWithRelevantVideos() {
        List<ProfessionalLesson> lessons = new ArrayList<>();
        lessons.add(new ProfessionalLesson("1. PHP Modern Features", "PHP 8+ features and Composer basics", 20, LessonType.VIDEO, "https://www.youtube.com/watch?v=a7_WFUlFS94", null, null));
        lessons.add(new ProfessionalLesson("2. Laravel Installation & Setup", "Install Laravel and understand project structure", 22, LessonType.VIDEO, "https://www.youtube.com/watch?v=2bz3KvCbSDg", null, null));
        lessons.add(new ProfessionalLesson("3. Laravel Eloquent ORM", "Database operations with Eloquent", 30, LessonType.VIDEO, "https://www.youtube.com/watch?v=ImtZ5yENzgE", null, null));
        lessons.add(new ProfessionalLesson("4. Laravel Blade Templates", "Create dynamic views with Blade", 25, LessonType.VIDEO, "https://www.youtube.com/watch?v=EU7PRmCpx-0", null, null));
        lessons.add(new ProfessionalLesson("5. Laravel Authentication", "User authentication with Laravel", 28, LessonType.VIDEO, "https://www.youtube.com/watch?v=3naf-KR0Wrc", null, null));
        lessons.add(new ProfessionalLesson("6. Laravel API Development", "Build REST APIs with Laravel", 32, LessonType.VIDEO, "https://www.youtube.com/watch?v=YGqCZjdgJJk", null, null));
        lessons.add(new ProfessionalLesson("7. Laravel Testing", "Write tests for Laravel applications", 26, LessonType.VIDEO, "https://www.youtube.com/watch?v=J9Tj2k7LGaM", null, null));
        lessons.add(new ProfessionalLesson("8. Laravel Queues & Jobs", "Background jobs and queue processing", 29, LessonType.VIDEO, "https://www.youtube.com/watch?v=3Lx_5DA7Tck", null, null));
        lessons.add(new ProfessionalLesson("9. Laravel Security Best Practices", "Security measures in Laravel", 24, LessonType.VIDEO, "https://www.youtube.com/watch?v=WTSlZt8Wr5U", null, null));
        lessons.add(new ProfessionalLesson("10. Laravel Project - E-commerce", "Build e-commerce site with Laravel", 45, LessonType.VIDEO, "https://www.youtube.com/watch?v=3naf-KR0Wrc", null, null));
        return lessons;
    }

    // ============================================================
    // .NET CORE & C# LESSONS
    // ============================================================
    private List<ProfessionalLesson> getDotNetCoreLessonsWithRelevantVideos() {
        List<ProfessionalLesson> lessons = new ArrayList<>();
        lessons.add(new ProfessionalLesson("1. C# Fundamentals", "C# syntax, OOP, and .NET Core basics", 25, LessonType.VIDEO, "https://www.youtube.com/watch?v=GhQdlIFylQ8", null, null));
        lessons.add(new ProfessionalLesson("2. ASP.NET Core Introduction", "ASP.NET Core architecture and setup", 28, LessonType.VIDEO, "https://www.youtube.com/watch?v=C5cnZ-gZy2I", null, null));
        lessons.add(new ProfessionalLesson("3. Entity Framework Core", "Database operations with EF Core", 32, LessonType.VIDEO, "https://www.youtube.com/watch?v=qkJ9keBmQWo", null, null));
        lessons.add(new ProfessionalLesson("4. ASP.NET Core Web APIs", "Create REST APIs with ASP.NET Core", 35, LessonType.VIDEO, "https://www.youtube.com/watch?v=fmvcAzHpsk8", null, null));
        lessons.add(new ProfessionalLesson("5. Authentication with JWT", "JWT authentication in .NET Core", 30, LessonType.VIDEO, "https://www.youtube.com/watch?v=mgeuh8k3I4g", null, null));
        lessons.add(new ProfessionalLesson("6. Dependency Injection", "DI and IoC in ASP.NET Core", 26, LessonType.VIDEO, "https://www.youtube.com/watch?v=GT8E8cuWY2w", null, null));
        lessons.add(new ProfessionalLesson("7. .NET Core Testing", "Unit testing with xUnit/NUnit", 24, LessonType.VIDEO, "https://www.youtube.com/watch?v=Y72j6bA3a1E", null, null));
        lessons.add(new ProfessionalLesson("8. Microservices with .NET", "Microservices architecture in .NET", 38, LessonType.VIDEO, "https://www.youtube.com/watch?v=DgVjEo3OGBI", null, null));
        lessons.add(new ProfessionalLesson("9. .NET Core Deployment", "Deploy .NET apps to production", 29, LessonType.VIDEO, "https://www.youtube.com/watch?v=HqAN7Xl_PyA", null, null));
        lessons.add(new ProfessionalLesson("10. Real Project - Todo API", "Build complete Todo API with .NET Core", 40, LessonType.VIDEO, "https://www.youtube.com/watch?v=fmvcAzHpsk8", null, null));
        return lessons;
    }

    // ============================================================
    // DATABASE & SQL LESSONS
    // ============================================================
    private List<ProfessionalLesson> getDatabaseSqlLessonsWithRelevantVideos() {
        List<ProfessionalLesson> lessons = new ArrayList<>();
        lessons.add(new ProfessionalLesson("1. Database Fundamentals", "RDBMS concepts and database types", 20, LessonType.VIDEO, "https://www.youtube.com/watch?v=ztHopE5Wnpc", null, null));
        lessons.add(new ProfessionalLesson("2. SQL Basics - SELECT Queries", "Basic SELECT statements and filtering", 25, LessonType.VIDEO, "https://www.youtube.com/watch?v=HXV3zeQKqGY", null, null));
        lessons.add(new ProfessionalLesson("3. Joins & Relationships", "INNER, LEFT, RIGHT, FULL joins", 30, LessonType.VIDEO, "https://www.youtube.com/watch?v=9yeOJ0ZMUYw", null, null));
        lessons.add(new ProfessionalLesson("4. Aggregate Functions", "GROUP BY, HAVING, aggregate functions", 28, LessonType.VIDEO, "https://www.youtube.com/watch?v=Wr71PX_A_MA", null, null));
        lessons.add(new ProfessionalLesson("5. Database Normalization", "Normal forms (1NF, 2NF, 3NF, BCNF)", 32, LessonType.VIDEO, "https://www.youtube.com/watch?v=GFQaEYEc8_8", null, null));
        lessons.add(new ProfessionalLesson("6. Indexes & Optimization", "Create indexes and optimize queries", 26, LessonType.VIDEO, "https://www.youtube.com/watch?v=-qMS6V1kPzA", null, null));
        lessons.add(new ProfessionalLesson("7. Transactions & ACID", "Database transactions and ACID properties", 24, LessonType.VIDEO, "https://www.youtube.com/watch?v=MLKvP3nz35Y", null, null));
        lessons.add(new ProfessionalLesson("8. Stored Procedures & Functions", "Create and use stored procedures", 29, LessonType.VIDEO, "https://www.youtube.com/watch?v=5K_6GCcEZ-U", null, null));
        lessons.add(new ProfessionalLesson("9. NoSQL Databases", "MongoDB basics and NoSQL concepts", 34, LessonType.VIDEO, "https://www.youtube.com/watch?v=EE8ZTQxa0AM", null, null));
        lessons.add(new ProfessionalLesson("10. Database Security", "SQL injection prevention and security", 27, LessonType.VIDEO, "https://www.youtube.com/watch?v=ciNHn38EyRc", null, null));
        return lessons;
    }

    private Course createCourseFromTemplate(ProfessionalCourseTemplate template, User instructor) {
        Course course = new Course(template.title, template.description, template.price, instructor);
        course.setThumbnailUrl(template.thumbnailUrl);
        course.setStatus(CourseStatus.APPROVED);
        return courseRepository.save(course);
    }

    @Transactional
    private void createLessonsForProfessionalCourse(Course course, List<ProfessionalLesson> lessons) throws Exception {
        System.out.println("   Creating " + lessons.size() + " lessons...");

        int lessonCount = 0;
        for (ProfessionalLesson professionalLesson : lessons) {
            lessonCount++;
            try {
                if (professionalLesson.type == LessonType.PDF) {
                    lessonService.createLesson(
                            professionalLesson.title,
                            professionalLesson.description,
                            professionalLesson.duration,
                            professionalLesson.type,
                            course,
                            null, null, null, professionalLesson.pdfUrl, null, null
                    );
                } else {
                    lessonService.createLesson(
                            professionalLesson.title,
                            professionalLesson.description,
                            professionalLesson.duration,
                            professionalLesson.type,
                            course,
                            null, professionalLesson.videoUrl, null, null, null, null
                    );
                }
            } catch (Exception e) {
                System.out.println("   ⚠ Error creating lesson " + lessonCount + ": " + e.getMessage());
            }
        }
    }

    @Transactional
    private void convertYouTubeUrlsToEmbed() {
        System.out.println("🔄 Converting YouTube URLs to embed format...");

        List<Course> courses = courseRepository.findAll();
        int convertedCount = 0;

        for (Course course : courses) {
            List<Lesson> lessons = lessonRepository.findByCourseOrderByOrderIndexAsc(course);

            for (Lesson lesson : lessons) {
                if (lesson.getType() == LessonType.VIDEO && lesson.getVideoUrl() != null) {
                    String videoUrl = lesson.getVideoUrl();

                    if (videoUrl.contains("youtube.com/watch?v=")) {
                        String videoId = videoUrl.substring(videoUrl.indexOf("v=") + 2);
                        if (videoId.contains("&")) {
                            videoId = videoId.substring(0, videoId.indexOf("&"));
                        }
                        String embedUrl = "https://www.youtube.com/embed/" + videoId;
                        lesson.setVideoUrl(embedUrl);
                        lessonRepository.save(lesson);
                        convertedCount++;
                    }
                }
            }
        }

        System.out.println("✅ Converted " + convertedCount + " YouTube URLs to embed format");
    }

    // Helper Classes
    private static class ProfessionalLesson {
        String title;
        String description;
        int duration;
        LessonType type;
        String videoUrl;
        String pdfUrl;
        String imageUrl;

        ProfessionalLesson(String title, String description, int duration,
                           LessonType type, String videoUrl, String pdfUrl, String imageUrl) {
            this.title = title;
            this.description = description;
            this.duration = duration;
            this.type = type;
            this.videoUrl = videoUrl;
            this.pdfUrl = pdfUrl;
            this.imageUrl = imageUrl;
        }
    }

    private static class ProfessionalCourseTemplate {
        String title;
        String description;
        double price;
        String thumbnailUrl;
        String courseType;
        List<ProfessionalLesson> lessons;

        ProfessionalCourseTemplate(String title, String description, double price,
                                   String thumbnailUrl, String courseType, List<ProfessionalLesson> lessons) {
            this.title = title;
            this.description = description;
            this.price = price;
            this.thumbnailUrl = thumbnailUrl;
            this.courseType = courseType;
            this.lessons = lessons;
        }
    }
}