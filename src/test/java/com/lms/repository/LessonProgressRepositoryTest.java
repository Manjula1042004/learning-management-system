package com.lms.repository;

import com.lms.entity.*;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LessonProgressRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private User student;
    private User instructor;
    private Course course;
    private Lesson lesson1;
    private Lesson lesson2;
    private Enrollment enrollment;
    private LessonProgress progress1;
    private LessonProgress progress2;

    @BeforeEach
    void setUp() {
        // Clear repositories in correct order
        lessonProgressRepository.deleteAll();
        lessonRepository.deleteAll();
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // Create users
        student = userRepository.save(TestDataFactory.createStudent(null));
        instructor = userRepository.save(TestDataFactory.createInstructor(null));

        // Create course
        course = TestDataFactory.createCourse(null, "Test Course", instructor);
        course = courseRepository.save(course);

        // Create lessons
        lesson1 = TestDataFactory.createLesson(null, "Lesson 1", course, LessonType.VIDEO);
        lesson1.setOrderIndex(1);
        lesson1 = lessonRepository.save(lesson1);

        lesson2 = TestDataFactory.createLesson(null, "Lesson 2", course, LessonType.VIDEO);
        lesson2.setOrderIndex(2);
        lesson2 = lessonRepository.save(lesson2);

        // Create enrollment
        enrollment = TestDataFactory.createEnrollment(null, student, course);
        enrollment = enrollmentRepository.save(enrollment);

        // Create progress records
        progress1 = TestDataFactory.createLessonProgress(null, enrollment, lesson1);
        progress1.setWatchTime(10.0);
        progress1 = lessonProgressRepository.save(progress1);

        progress2 = TestDataFactory.createLessonProgress(null, enrollment, lesson2);
        progress2.setCompleted(true);
        progress2.setCompletedAt(LocalDateTime.now());
        progress2.setWatchTime(25.0);
        progress2 = lessonProgressRepository.save(progress2);
    }

    @Test
    void testFindByEnrollmentAndLesson() {
        Optional<LessonProgress> found = lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson1);
        assertThat(found).isPresent();
        assertThat(found.get().getEnrollment().getId()).isEqualTo(enrollment.getId());
        assertThat(found.get().getLesson().getId()).isEqualTo(lesson1.getId());
    }

    @Test
    void testFindByEnrollment() {
        List<LessonProgress> progresses = lessonProgressRepository.findByEnrollment(enrollment);
        assertThat(progresses).hasSize(2);
        assertThat(progresses).extracting(p -> p.getLesson().getTitle())
                .containsExactlyInAnyOrder("Lesson 1", "Lesson 2");
    }

    @Test
    void testCountCompletedLessonsByEnrollment() {
        Long count = lessonProgressRepository.countCompletedLessonsByEnrollment(enrollment);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testFindByEnrollmentAndCourse() {
        List<LessonProgress> progresses = lessonProgressRepository.findByEnrollmentAndCourse(enrollment, course);
        assertThat(progresses).hasSize(2);
    }

    @Test
    void testDeleteByLessonId() {
        // First verify progress exists
        Optional<LessonProgress> beforeDelete = lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson1);
        assertThat(beforeDelete).isPresent();

        // Delete by lesson ID
        int deletedCount = lessonProgressRepository.deleteByLessonId(lesson1.getId());

        // Verify delete operation
        assertThat(deletedCount).isEqualTo(1);

        // Flush to ensure delete is committed
        lessonProgressRepository.flush();

        // Verify progress is gone
        Optional<LessonProgress> afterDelete = lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson1);
        assertThat(afterDelete).isEmpty();

        // Verify other progress still exists
        Optional<LessonProgress> otherProgress = lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson2);
        assertThat(otherProgress).isPresent();
    }
}