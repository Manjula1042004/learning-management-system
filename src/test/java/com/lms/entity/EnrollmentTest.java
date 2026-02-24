package com.lms.entity;

import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EnrollmentTest {

    private User student;
    private User instructor;
    private Course course;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        student = TestDataFactory.createStudent(1L);
        instructor = TestDataFactory.createInstructor(2L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);

        enrollment = new Enrollment(student, course);
        enrollment.setId(1L);
    }

    @Test
    void constructor_ShouldCreateEnrollment() {
        Enrollment newEnrollment = new Enrollment(student, course);

        assertThat(newEnrollment.getStudent()).isEqualTo(student);
        assertThat(newEnrollment.getCourse()).isEqualTo(course);
        assertThat(newEnrollment.getStatus()).isNull();
        assertThat(newEnrollment.getProgress()).isNull();
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        enrollment.setId(2L);
        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollment.setProgress(75.5);

        LocalDateTime enrolledAt = LocalDateTime.now().minusDays(1);
        LocalDateTime completedAt = LocalDateTime.now();

        enrollment.setEnrolledAt(enrolledAt);
        enrollment.setCompletedAt(completedAt);
        enrollment.setPaymentId("PAY-123");

        assertThat(enrollment.getId()).isEqualTo(2L);
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
        assertThat(enrollment.getProgress()).isEqualTo(75.5);
        assertThat(enrollment.getEnrolledAt()).isEqualTo(enrolledAt);
        assertThat(enrollment.getCompletedAt()).isEqualTo(completedAt);
        assertThat(enrollment.getPaymentId()).isEqualTo("PAY-123");
    }

    @Test
    void student_ShouldBeAccessible() {
        assertThat(enrollment.getStudent()).isEqualTo(student);

        User newStudent = TestDataFactory.createStudent(3L);
        enrollment.setStudent(newStudent);
        assertThat(enrollment.getStudent()).isEqualTo(newStudent);
    }

    @Test
    void course_ShouldBeAccessible() {
        assertThat(enrollment.getCourse()).isEqualTo(course);

        Course newCourse = TestDataFactory.createCourse(2L, "New Course", instructor);
        enrollment.setCourse(newCourse);
        assertThat(enrollment.getCourse()).isEqualTo(newCourse);
    }

    @Test
    void prePersist_ShouldSetEnrolledAt() {
        Enrollment newEnrollment = new Enrollment();
        newEnrollment.onCreate();

        assertThat(newEnrollment.getEnrolledAt()).isNotNull();
    }
}