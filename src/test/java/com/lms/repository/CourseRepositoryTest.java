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
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    private User instructor;
    private Course approvedCourse;
    private Course pendingCourse;

    @BeforeEach
    void setUp() {
        instructor = TestDataFactory.createInstructor(null);
        instructor = userRepository.save(instructor);

        approvedCourse = TestDataFactory.createCourse(null, "Java Programming", instructor);
        approvedCourse.setStatus(CourseStatus.APPROVED);
        approvedCourse = courseRepository.save(approvedCourse);

        pendingCourse = TestDataFactory.createCourse(null, "Python Programming", instructor);
        pendingCourse.setStatus(CourseStatus.PENDING);
        pendingCourse = courseRepository.save(pendingCourse);
    }

    @Test
    void findByInstructor_ShouldReturnCoursesForInstructor() {
        List<Course> courses = courseRepository.findByInstructor(instructor);

        assertThat(courses).hasSize(2);
        assertThat(courses).extracting(Course::getTitle)
                .containsExactlyInAnyOrder("Java Programming", "Python Programming");
    }

    @Test
    void findByStatus_ShouldReturnCoursesWithGivenStatus() {
        List<Course> approvedCourses = courseRepository.findByStatus(CourseStatus.APPROVED);
        List<Course> pendingCourses = courseRepository.findByStatus(CourseStatus.PENDING);

        assertThat(approvedCourses).hasSize(1);
        assertThat(approvedCourses.get(0).getTitle()).isEqualTo("Java Programming");

        assertThat(pendingCourses).hasSize(1);
        assertThat(pendingCourses.get(0).getTitle()).isEqualTo("Python Programming");
    }

    @Test
    void findById_ShouldReturnCourse_WhenExists() {
        Optional<Course> found = courseRepository.findById(approvedCourse.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Java Programming");
    }
}