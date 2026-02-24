package com.lms.entity;

import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private User student;
    private User instructor;
    private User admin;

    @BeforeEach
    void setUp() {
        student = new User("student1", "student1@test.com", "password", Role.STUDENT);
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setPhone("1234567890");
        student.setBio("Test student");
        student.setProfilePictureUrl("https://test.com/pic.jpg");
        student.setEnabled(true);
        student.setCreatedAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());

        instructor = new User("instructor1", "instructor1@test.com", "password", Role.INSTRUCTOR);
        instructor.setId(2L);

        admin = new User("admin1", "admin1@test.com", "password", Role.ADMIN);
        admin.setId(3L);
    }

    @Test
    void constructor_ShouldCreateUser() {
        User newUser = new User("newuser", "new@test.com", "password", Role.STUDENT);

        assertThat(newUser.getUsername()).isEqualTo("newuser");
        assertThat(newUser.getEmail()).isEqualTo("new@test.com");
        assertThat(newUser.getPassword()).isEqualTo("password");
        assertThat(newUser.getRole()).isEqualTo(Role.STUDENT);
        assertThat(newUser.isEnabled()).isTrue();
    }

    @Test
    void constructor_WithAllFields_ShouldCreateUser() {
        User newUser = new User(
                "newuser",
                "new@test.com",
                "password",
                Role.INSTRUCTOR,
                "Jane",
                "Smith",
                "9876543210"
        );

        assertThat(newUser.getUsername()).isEqualTo("newuser");
        assertThat(newUser.getEmail()).isEqualTo("new@test.com");
        assertThat(newUser.getPassword()).isEqualTo("password");
        assertThat(newUser.getRole()).isEqualTo(Role.INSTRUCTOR);
        assertThat(newUser.getFirstName()).isEqualTo("Jane");
        assertThat(newUser.getLastName()).isEqualTo("Smith");
        assertThat(newUser.getPhone()).isEqualTo("9876543210");
        assertThat(newUser.isEnabled()).isTrue();
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        student.setId(10L);
        student.setUsername("updateduser");
        student.setEmail("updated@test.com");
        student.setPassword("newpassword");
        student.setRole(Role.ADMIN);
        student.setEnabled(false);
        student.setFirstName("Updated");
        student.setLastName("User");
        student.setPhone("5555555555");
        student.setBio("Updated bio");
        student.setProfilePictureUrl("https://test.com/new.jpg");

        LocalDateTime now = LocalDateTime.now();
        student.setCreatedAt(now);
        student.setUpdatedAt(now);
        student.setLastLogin(now);

        assertThat(student.getId()).isEqualTo(10L);
        assertThat(student.getUsername()).isEqualTo("updateduser");
        assertThat(student.getEmail()).isEqualTo("updated@test.com");
        assertThat(student.getPassword()).isEqualTo("newpassword");
        assertThat(student.getRole()).isEqualTo(Role.ADMIN);
        assertThat(student.isEnabled()).isFalse();
        assertThat(student.getFirstName()).isEqualTo("Updated");
        assertThat(student.getLastName()).isEqualTo("User");
        assertThat(student.getPhone()).isEqualTo("5555555555");
        assertThat(student.getBio()).isEqualTo("Updated bio");
        assertThat(student.getProfilePictureUrl()).isEqualTo("https://test.com/new.jpg");
        assertThat(student.getCreatedAt()).isEqualTo(now);
        assertThat(student.getUpdatedAt()).isEqualTo(now);
        assertThat(student.getLastLogin()).isEqualTo(now);
    }

    @Test
    void getFullName_ShouldReturnFullName_WhenBothNamesPresent() {
        assertThat(student.getFullName()).isEqualTo("John Doe");
    }

    @Test
    void getFullName_ShouldReturnFirstName_WhenOnlyFirstNamePresent() {
        student.setLastName(null);
        assertThat(student.getFullName()).isEqualTo("John");
    }

    @Test
    void getFullName_ShouldReturnLastName_WhenOnlyLastNamePresent() {
        student.setFirstName(null);
        student.setLastName("Doe");
        assertThat(student.getFullName()).isEqualTo("Doe");
    }

    @Test
    void getFullName_ShouldReturnUsername_WhenNoNamesPresent() {
        student.setFirstName(null);
        student.setLastName(null);
        assertThat(student.getFullName()).isEqualTo("student1");
    }

    @Test
    void isAdmin_ShouldReturnTrue_ForAdminRole() {
        assertThat(admin.isAdmin()).isTrue();
        assertThat(student.isAdmin()).isFalse();
        assertThat(instructor.isAdmin()).isFalse();
    }

    @Test
    void isInstructor_ShouldReturnTrue_ForInstructorRole() {
        assertThat(instructor.isInstructor()).isTrue();
        assertThat(student.isInstructor()).isFalse();
        assertThat(admin.isInstructor()).isFalse();
    }

    @Test
    void isStudent_ShouldReturnTrue_ForStudentRole() {
        assertThat(student.isStudent()).isTrue();
        assertThat(instructor.isStudent()).isFalse();
        assertThat(admin.isStudent()).isFalse();
    }

    @Test
    void updateLastLogin_ShouldSetLastLogin() {
        student.updateLastLogin();
        assertThat(student.getLastLogin()).isNotNull();
    }

    @Test
    void prePersist_ShouldSetCreatedAndUpdatedAt() {
        User newUser = new User();
        newUser.onCreate();

        assertThat(newUser.getCreatedAt()).isNotNull();
        assertThat(newUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void preUpdate_ShouldUpdateUpdatedAt() {
        LocalDateTime oldUpdatedAt = student.getUpdatedAt();
        student.onUpdate();

        assertThat(student.getUpdatedAt()).isNotEqualTo(oldUpdatedAt);
        assertThat(student.getUpdatedAt()).isAfterOrEqualTo(oldUpdatedAt);
    }

    @Test
    void toString_ShouldContainImportantFields() {
        String toString = student.toString();

        assertThat(toString).contains("id=1");
        assertThat(toString).contains("username=student1");
        assertThat(toString).contains("email=student1@test.com");
        assertThat(toString).contains("role=STUDENT");
        assertThat(toString).contains("enabled=true");
    }
}