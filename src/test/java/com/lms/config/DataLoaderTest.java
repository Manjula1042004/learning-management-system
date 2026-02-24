package com.lms.config;

import com.lms.entity.*;
import com.lms.repository.*;
import com.lms.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class DataLoaderTest {

    @MockBean
    private UserService userService;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private LessonService lessonService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private LessonRepository lessonRepository;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private EnrollmentRepository enrollmentRepository;

    private DataLoader dataLoader;

    private User admin;
    private User instructor1;
    private User instructor2;
    private User student1;
    private User student2;
    private Course course1;

    @BeforeEach
    void setUp() {
        // Fix: Create actual DataLoader with mocked dependencies
        dataLoader = new DataLoader(
                userService, courseRepository, lessonService, courseService,
                lessonRepository, enrollmentService, enrollmentRepository);

        admin = new User("admin", "admin@lms.com", "encoded", Role.ADMIN);
        admin.setId(1L);

        instructor1 = new User("instructor1", "inst1@lms.com", "encoded", Role.INSTRUCTOR);
        instructor1.setId(2L);

        instructor2 = new User("instructor2", "inst2@lms.com", "encoded", Role.INSTRUCTOR);
        instructor2.setId(3L);

        student1 = new User("student1", "stu1@lms.com", "encoded", Role.STUDENT);
        student1.setId(4L);

        student2 = new User("student2", "stu2@lms.com", "encoded", Role.STUDENT);
        student2.setId(5L);

        course1 = new Course("Java Course", "Java Description", 99.99, instructor1);
        course1.setId(1L);
    }

    @Test
    void run_ShouldCreateDefaultUsers() throws Exception {
        when(userService.getUserByUsername("admin")).thenReturn(Optional.empty());
        when(userService.getUserByUsername("instructor1")).thenReturn(Optional.empty());
        when(userService.getUserByUsername("student1")).thenReturn(Optional.empty());

        when(userService.createUser(eq("admin"), anyString(), anyString(), eq(Role.ADMIN)))
                .thenReturn(admin);
        when(userService.createUser(anyString(), anyString(), anyString(), eq(Role.INSTRUCTOR)))
                .thenReturn(instructor1);
        when(userService.createUser(anyString(), anyString(), anyString(), eq(Role.STUDENT)))
                .thenReturn(student1);

        when(userService.getUsersByRole(Role.INSTRUCTOR)).thenReturn(Arrays.asList(instructor1));
        when(userService.getUsersByRole(Role.STUDENT)).thenReturn(Arrays.asList(student1, student2));

        // Use lenient() for less important stubs
        lenient().when(courseRepository.findByInstructor(any(User.class))).thenReturn(Arrays.asList());
        lenient().when(courseRepository.save(any(Course.class))).thenReturn(course1);
        lenient().when(lessonRepository.findByCourseOrderByOrderIndexAsc(any(Course.class))).thenReturn(Arrays.asList());

        dataLoader.run();

        verify(userService, atLeastOnce()).createUser(anyString(), anyString(), anyString(), any(Role.class));
    }



    private void createDefaultUsers() {
        System.out.println("Creating default users...");

        // Create admin user if not exists
        Optional<User> adminOpt = userService.getUserByUsername("admin");
        if (adminOpt.isEmpty()) {
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
            try {
                Optional<User> existingUser = userService.getUserByUsername(username);
                if (existingUser.isEmpty()) {
                    User instructor = userService.createUser(username,
                            instructorEmails[i], "instructor123", Role.INSTRUCTOR);
                    instructor.setFirstName(instructorNames[i]);
                    instructor.setLastName(lastNames[i]);
                    instructor.setBio(expertise[i]);
                    instructor.setProfilePictureUrl("https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400&h=400&fit=crop");
                    System.out.println("✓ Instructor created: " + instructor.getUsername() + " - " + expertise[i]);
                } else {
                    // Fix: Use the existing user
                    User instructor = existingUser.get();
                    instructor.setFirstName(instructorNames[i]);
                    instructor.setLastName(lastNames[i]);
                    instructor.setBio(expertise[i]);
                    instructor.setProfilePictureUrl("https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400&h=400&fit=crop");
                    // Need to save if there's an update method
                    System.out.println("ℹ Instructor " + (i + 1) + " already exists, updated fields");
                }
            } catch (Exception e) {
                System.out.println("⚠ Error processing instructor " + (i + 1) + ": " + e.getMessage());
            }
        }

        // Create sample students (similar fix)
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
            try {
                Optional<User> existingUser = userService.getUserByUsername(username);
                if (existingUser.isEmpty()) {
                    User student = userService.createUser(username,
                            studentEmails[i-1], "student123", Role.STUDENT);
                    student.setFirstName(studentFirstNames[i-1]);
                    student.setLastName(studentLastNames[i-1]);
                    student.setBio("Computer Science Student | Passionate about learning new technologies");
                    System.out.println("✓ Student created: " + student.getUsername() + " - " + student.getFullName());
                } else {
                    // Fix: Use the existing user
                    User student = existingUser.get();
                    student.setFirstName(studentFirstNames[i-1]);
                    student.setLastName(studentLastNames[i-1]);
                    student.setBio("Computer Science Student | Passionate about learning new technologies");
                    System.out.println("ℹ Student " + i + " already exists, updated fields");
                }
            } catch (Exception e) {
                System.out.println("⚠ Error processing student " + i + ": " + e.getMessage());
            }
        }
    }
}