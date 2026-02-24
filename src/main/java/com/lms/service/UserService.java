package com.lms.service;

import com.lms.entity.User;
import com.lms.entity.Role;
import com.lms.entity.Course;
import com.lms.entity.CourseStatus;
import com.lms.entity.Lesson;
import com.lms.entity.LessonType;
import com.lms.entity.Enrollment;
import com.lms.entity.LessonProgress;
import com.lms.entity.QuizAttempt;
import com.lms.entity.StudentAnswer;
import com.lms.entity.Payment;
import com.lms.entity.Media;
import com.lms.repository.UserRepository;
import com.lms.repository.CourseRepository;
import com.lms.repository.LessonRepository;
import com.lms.repository.EnrollmentRepository;
import com.lms.repository.LessonProgressRepository;
import com.lms.repository.QuizAttemptRepository;
import com.lms.repository.StudentAnswerRepository;
import com.lms.repository.PaymentRepository;
import com.lms.repository.MediaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseRepository courseRepository;
    private final CourseService courseService;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final PaymentRepository paymentRepository;
    private final MediaRepository mediaRepository;

    // Single constructor with all dependencies
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       CourseRepository courseRepository,
                       CourseService courseService,
                       LessonRepository lessonRepository,
                       EnrollmentRepository enrollmentRepository,
                       LessonProgressRepository lessonProgressRepository,
                       QuizAttemptRepository quizAttemptRepository,
                       StudentAnswerRepository studentAnswerRepository,
                       PaymentRepository paymentRepository,
                       MediaRepository mediaRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.courseRepository = courseRepository;
        this.courseService = courseService;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.paymentRepository = paymentRepository;
        this.mediaRepository = mediaRepository;
    }

    // ✅ NEW: Safe delete user method
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("🗑️ Deleting user: " + user.getUsername() + " (ID: " + userId + ")");

        try {
            // Handle different user roles
            if (user.isInstructor()) {
                handleInstructorDeletion(user);
            } else if (user.isStudent()) {
                handleStudentDeletion(user);
            } else if (user.isAdmin()) {
                // Check if there's at least one other admin
                long adminCount = userRepository.findByRole(Role.ADMIN).size();
                if (adminCount <= 1) {
                    throw new RuntimeException("Cannot delete the only admin user");
                }
            }

            // Delete user (cascading will handle the rest)
            userRepository.delete(user);
            System.out.println("✅ User deleted successfully: " + user.getUsername());

        } catch (Exception e) {
            System.err.println("❌ Error deleting user: " + e.getMessage());
            throw new RuntimeException("Cannot delete user: " + e.getMessage());
        }
    }

    private void handleInstructorDeletion(User instructor) {
        System.out.println("👨‍🏫 Handling instructor deletion for: " + instructor.getUsername());

        // Get instructor's courses
        List<Course> instructorCourses = courseRepository.findByInstructor(instructor);

        if (!instructorCourses.isEmpty()) {
            System.out.println("⚠ Instructor has " + instructorCourses.size() + " courses");

            // Option 1: Delete all courses (with all associated data)
            for (Course course : instructorCourses) {
                // Delete course (this will cascade to lessons, enrollments, etc.)
                courseRepository.delete(course);
                System.out.println("🗑️ Deleted course: " + course.getTitle());
            }

            // Option 2: Or reassign courses to another instructor
            // List<User> otherInstructors = userRepository.findByRole(Role.INSTRUCTOR)
            //         .stream().filter(i -> !i.getId().equals(instructor.getId())).toList();
            // if (!otherInstructors.isEmpty()) {
            //     User newInstructor = otherInstructors.get(0);
            //     for (Course course : instructorCourses) {
            //         course.setInstructor(newInstructor);
            //         courseRepository.save(course);
            //     }
            // }
        }
    }

    private void handleStudentDeletion(User student) {
        System.out.println("👨‍🎓 Handling student deletion for: " + student.getUsername());

        // Get student's enrollments
        List<Enrollment> studentEnrollments = enrollmentRepository.findByStudent(student);

        if (!studentEnrollments.isEmpty()) {
            System.out.println("⚠ Student has " + studentEnrollments.size() + " enrollments");

            // Delete enrollments (this will cascade to lesson progress via entity cascade)
            for (Enrollment enrollment : studentEnrollments) {
                // First delete quiz attempts (which depend on enrollment)
                List<QuizAttempt> quizAttempts = quizAttemptRepository.findByEnrollment(enrollment);
                for (QuizAttempt attempt : quizAttempts) {
                    // Delete student answers first
                    studentAnswerRepository.deleteAll(attempt.getStudentAnswers());
                    quizAttemptRepository.delete(attempt);
                }

                // Delete lesson progress records
                List<LessonProgress> lessonProgresses = lessonProgressRepository.findByEnrollment(enrollment);
                lessonProgressRepository.deleteAll(lessonProgresses);

                // Now delete the enrollment
                enrollmentRepository.delete(enrollment);
            }
        }

        // Delete student's payments
        List<Payment> studentPayments = paymentRepository.findByUser(student);
        if (!studentPayments.isEmpty()) {
            paymentRepository.deleteAll(studentPayments);
        }
    }

    private Course createCourseWithLessons(User instructor, String title, String description,
                                           double price, String thumbnail) {
        // Create course
        Course course = new Course(title, description, price, instructor);
        course.setThumbnailUrl(thumbnail);
        course.setStatus(CourseStatus.APPROVED);
        course = courseRepository.save(course);

        // Add lessons
        addDefaultLessonsToCourse(course);

        return course;
    }

    private void addDefaultLessonsToCourse(Course course) {
        List<Lesson> lessons = new ArrayList<>();

        // Lesson 1: Video
        Lesson lesson1 = new Lesson();
        lesson1.setTitle("Introduction to the Course");
        lesson1.setDescription("Welcome and course overview");
        lesson1.setDuration(30);
        lesson1.setType(LessonType.VIDEO);
        lesson1.setVideoUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        lesson1.setCourse(course);
        lesson1.setOrderIndex(1);
        lessons.add(lesson1);

        // Lesson 2: Video
        Lesson lesson2 = new Lesson();
        lesson2.setTitle("Getting Started");
        lesson2.setDescription("Setup and installation guide");
        lesson2.setDuration(25);
        lesson2.setType(LessonType.VIDEO);
        lesson2.setVideoUrl("https://www.youtube.com/watch?v=zOjov-2OZ0E");
        lesson2.setCourse(course);
        lesson2.setOrderIndex(2);
        lessons.add(lesson2);

        // Lesson 3: PDF
        Lesson lesson3 = new Lesson();
        lesson3.setTitle("Course Materials PDF");
        lesson3.setDescription("Download course syllabus and materials");
        lesson3.setDuration(15);
        lesson3.setType(LessonType.PDF);
        lesson3.setResourceUrl("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf");
        lesson3.setCourse(course);
        lesson3.setOrderIndex(3);
        lessons.add(lesson3);

        // Lesson 4: Video
        Lesson lesson4 = new Lesson();
        lesson4.setTitle("Practice Session");
        lesson4.setDescription("Hands-on practice exercises");
        lesson4.setDuration(40);
        lesson4.setType(LessonType.VIDEO);
        lesson4.setVideoUrl("https://www.youtube.com/watch?v=8jLOx1hD3_o");
        lesson4.setCourse(course);
        lesson4.setOrderIndex(4);
        lessons.add(lesson4);

        // Save all lessons
        lessonRepository.saveAll(lessons);
        System.out.println("✅ Added " + lessons.size() + " lessons to course: " + course.getTitle());
    }

    // --- EXISTING METHODS (KEEP THESE AS IS) ---

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(Long userId, String username, String email, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public User updateUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(enabled);
        return userRepository.save(user);
    }

    public User updateUserRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(role);
        return userRepository.save(user);
    }

    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.searchUsers(query);
    }

    @Transactional
    private void assignEnhancedCoursesToInstructor(User instructor) {
        System.out.println("🎯 Assigning ENHANCED courses to new instructor: " + instructor.getUsername());

        try {
            // Create enhanced course 1: Java Masterclass
            Course course1 = createEnhancedCourse(
                    instructor,
                    "Java Programming Masterclass 2024 - Zero to Hero",
                    "Become a Java expert! Learn Java from basics to advanced. Master OOP, Collections, Multithreading, Streams API, Spring Boot, and build 5+ real-world projects. 50+ hours of comprehensive content with hands-on exercises.",
                    149.99,
                    "https://images.unsplash.com/photo-1542831371-29b0f74f9713?w=1200&h=675&fit=crop",
                    "Java"
            );

            // Create enhanced course 2: Spring Boot
            Course course2 = createEnhancedCourse(
                    instructor,
                    "Spring Boot & Microservices - Complete Developer Guide",
                    "Master Spring Boot, REST APIs, JPA/Hibernate, Security, Microservices architecture, Docker, Kubernetes, and build enterprise-grade applications. Learn industry best practices and deployment strategies.",
                    169.99,
                    "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=1200&h=675&fit=crop",
                    "Spring"
            );

            System.out.println("✅ Created 2 enhanced courses for " + instructor.getUsername());

        } catch (Exception e) {
            System.out.println("⚠ Error creating courses for new instructor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Course createEnhancedCourse(User instructor, String title, String description,
                                        double price, String thumbnailUrl, String courseType) {
        Course course = new Course(title, description, price, instructor);
        course.setThumbnailUrl(thumbnailUrl);
        course.setStatus(CourseStatus.APPROVED);
        course = courseRepository.save(course);

        // Create sample lessons
        createSampleLessonsForCourse(course, courseType);

        return course;
    }

    private void createSampleLessonsForCourse(Course course, String courseType) {
        // Create 5 sample lessons for new course
        for (int i = 1; i <= 5; i++) {
            Lesson lesson = new Lesson();
            lesson.setTitle(courseType + " Tutorial - Part " + i);
            lesson.setDescription("Learn " + courseType + " concepts in this comprehensive tutorial.");
            lesson.setDuration(30 + (i * 5));
            lesson.setType(LessonType.VIDEO);
            lesson.setOrderIndex(i);
            lesson.setCourse(course);

            // Set video URL based on course type
            if (courseType.equals("Java")) {
                lesson.setVideoUrl("https://www.youtube.com/watch?v=eIrMbAQSU34");
            } else if (courseType.equals("Spring")) {
                lesson.setVideoUrl("https://www.youtube.com/watch?v=vtPkZShrvXQ");
            } else {
                lesson.setVideoUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
            }

            lessonRepository.save(lesson);
        }
    }

    public User createUser(String username, String email, String password, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User(username, email, passwordEncoder.encode(password), role);
        user = userRepository.save(user);

        // ✅ REMOVED: Don't auto-assign courses to new instructors
        // if (role == Role.INSTRUCTOR) {
        //     assignEnhancedCoursesToInstructor(user);
        // }

        System.out.println("✅ New " + role + " registered: " + username);
        System.out.println("✅ Instructor can now see ALL " + courseRepository.count() + " system courses");

        return user;
    }
}