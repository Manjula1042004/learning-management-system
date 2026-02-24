package com.lms.entity;

import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CourseTest {

    private User instructor;
    private Course course;
    private Lesson lesson;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        instructor = TestDataFactory.createInstructor(1L);
        course = new Course("Test Course", "Test Description", 99.99, instructor);
        course.setId(1L);

        lesson = new Lesson("Test Lesson", "Lesson Description", 30, 1, LessonType.VIDEO, course);
        lesson.setId(1L);

        enrollment = new Enrollment(TestDataFactory.createStudent(2L), course);
        enrollment.setId(1L);
    }

    @Test
    void constructor_ShouldCreateCourse() {
        Course newCourse = new Course("New Course", "New Description", 149.99, instructor);

        assertThat(newCourse.getTitle()).isEqualTo("New Course");
        assertThat(newCourse.getDescription()).isEqualTo("New Description");
        assertThat(newCourse.getPrice()).isEqualTo(149.99);
        assertThat(newCourse.getInstructor()).isEqualTo(instructor);
        assertThat(newCourse.getStatus()).isEqualTo(CourseStatus.PENDING);
        assertThat(newCourse.getLessons()).isEmpty();
        assertThat(newCourse.getEnrollments()).isEmpty();
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        course.setId(2L);
        course.setTitle("Updated Title");
        course.setDescription("Updated Description");
        course.setPrice(199.99);
        course.setStatus(CourseStatus.APPROVED);
        course.setThumbnailUrl("https://test.com/new.jpg");

        LocalDateTime now = LocalDateTime.now();
        course.setCreatedAt(now);
        course.setUpdatedAt(now);

        assertThat(course.getId()).isEqualTo(2L);
        assertThat(course.getTitle()).isEqualTo("Updated Title");
        assertThat(course.getDescription()).isEqualTo("Updated Description");
        assertThat(course.getPrice()).isEqualTo(199.99);
        assertThat(course.getStatus()).isEqualTo(CourseStatus.APPROVED);
        assertThat(course.getThumbnailUrl()).isEqualTo("https://test.com/new.jpg");
        assertThat(course.getCreatedAt()).isEqualTo(now);
        assertThat(course.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void lessons_ShouldBeManageable() {
        assertThat(course.getLessons()).isEmpty();

        course.getLessons().add(lesson);
        assertThat(course.getLessons()).hasSize(1);
        assertThat(course.getLessons()).contains(lesson);

        course.getLessons().remove(lesson);
        assertThat(course.getLessons()).isEmpty();
    }

    @Test
    void enrollments_ShouldBeManageable() {
        assertThat(course.getEnrollments()).isEmpty();

        course.getEnrollments().add(enrollment);
        assertThat(course.getEnrollments()).hasSize(1);
        assertThat(course.getEnrollments()).contains(enrollment);

        course.getEnrollments().remove(enrollment);
        assertThat(course.getEnrollments()).isEmpty();
    }

    @Test
    void prePersist_ShouldSetCreatedAndUpdatedAt() {
        Course newCourse = new Course();
        newCourse.onCreate();

        assertThat(newCourse.getCreatedAt()).isNotNull();
        assertThat(newCourse.getUpdatedAt()).isNotNull();
    }

    @Test
    void preUpdate_ShouldUpdateUpdatedAt() {
        LocalDateTime oldUpdatedAt = course.getUpdatedAt();
        course.onUpdate();

        assertThat(course.getUpdatedAt()).isNotEqualTo(oldUpdatedAt);
        assertThat(course.getUpdatedAt()).isAfterOrEqualTo(oldUpdatedAt);
    }

    @Test
    void instructor_ShouldBeAccessible() {
        assertThat(course.getInstructor()).isEqualTo(instructor);

        User newInstructor = TestDataFactory.createInstructor(3L);
        course.setInstructor(newInstructor);
        assertThat(course.getInstructor()).isEqualTo(newInstructor);
    }
}