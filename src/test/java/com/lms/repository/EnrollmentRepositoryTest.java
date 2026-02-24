package com.lms.repository;

import com.lms.config.TestConfig;
import com.lms.entity.*;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class EnrollmentRepositoryTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    private User student;
    private User instructor;
    private Course course;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        // Clear all data
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // Create fresh test data
        student = userRepository.save(TestDataFactory.createStudent(null));
        instructor = userRepository.save(TestDataFactory.createInstructor(null));

        course = TestDataFactory.createCourse(null, "Test Course", instructor);
        course = courseRepository.save(course);

        enrollment = TestDataFactory.createEnrollment(null, student, course);
        enrollment = enrollmentRepository.save(enrollment);
    }

    @Test
    void testFindByStudentAndCourse() {
        Optional<Enrollment> found = enrollmentRepository.findByStudentAndCourse(student, course);
        assertThat(found).isPresent();
        assertThat(found.get().getStudent().getId()).isEqualTo(student.getId());
    }

    @Test
    void testFindByStudent() {
        List<Enrollment> enrollments = enrollmentRepository.findByStudent(student);
        assertThat(enrollments).hasSize(1);
    }

    @Test
    void testFindByCourse() {
        List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);
        assertThat(enrollments).hasSize(1);
    }

    @Test
    void testExistsByStudentAndCourse() {
        assertThat(enrollmentRepository.existsByStudentAndCourse(student, course)).isTrue();
    }

    @Test
    void testCountByCourse() {
        assertThat(enrollmentRepository.countByCourse(course)).isEqualTo(1);
    }
}