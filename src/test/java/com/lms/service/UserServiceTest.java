package com.lms.service;

import com.lms.entity.*;
import com.lms.repository.*;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private UserService userService;

    private User student;
    private User instructor;
    private User admin;
    private Course course;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        student = TestDataFactory.createStudent(1L);
        instructor = TestDataFactory.createInstructor(2L);
        admin = TestDataFactory.createAdmin(3L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        enrollment = TestDataFactory.createEnrollment(1L, student, course);
    }

    @Test
    void createUser_ShouldCreateNewUser_WhenDataIsValid() {
        String username = "newuser";
        String email = "new@test.com";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User saved = i.getArgument(0);
            saved.setId(4L);
            return saved;
        });

        User created = userService.createUser(username, email, rawPassword, Role.STUDENT);

        assertThat(created).isNotNull();
        assertThat(created.getUsername()).isEqualTo(username);
        assertThat(created.getEmail()).isEqualTo(email);
        assertThat(created.getPassword()).isEqualTo(encodedPassword);
        assertThat(created.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    void getUserByUsername_ShouldReturnUser_WhenExists() {
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(student));

        Optional<User> found = userService.getUserByUsername("student1");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("student1");
    }

    @Test
    void deleteUser_ShouldThrowException_WhenDeletingLastAdmin() {
        Long adminId = 3L;
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findByRole(Role.ADMIN)).thenReturn(Arrays.asList(admin));

        // Fix: Use assertThatThrownBy and check message contains
        assertThatThrownBy(() -> userService.deleteUser(adminId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot delete the only admin user");
    }

    @Test
    void deleteUser_ShouldDeleteStudent_WhenUserExists() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudent(student)).thenReturn(Arrays.asList(enrollment));
        when(paymentRepository.findByUser(student)).thenReturn(Arrays.asList());
        // Remove unnecessary stubbings

        userService.deleteUser(userId);

        verify(userRepository).delete(student);
    }
}