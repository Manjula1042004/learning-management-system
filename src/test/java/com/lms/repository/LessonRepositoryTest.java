package com.lms.repository;

import com.lms.entity.*;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class LessonRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LessonRepository lessonRepository;

    private User instructor;
    private Course course;
    private Lesson lesson1;
    private Lesson lesson2;
    private Lesson lesson3;

    @BeforeEach
    void setUp() {
        instructor = TestDataFactory.createInstructor(null);
        instructor = entityManager.persistAndFlush(instructor);

        course = TestDataFactory.createCourse(null, "Test Course", instructor);
        course = entityManager.persistAndFlush(course);

        lesson1 = TestDataFactory.createLesson(null, "Introduction", course, LessonType.VIDEO);
        lesson1.setOrderIndex(1);
        lesson1 = entityManager.persistAndFlush(lesson1);

        lesson2 = TestDataFactory.createLesson(null, "Getting Started", course, LessonType.VIDEO);
        lesson2.setOrderIndex(2);
        lesson2 = entityManager.persistAndFlush(lesson2);

        lesson3 = TestDataFactory.createLesson(null, "Resources", course, LessonType.PDF);
        lesson3.setOrderIndex(3);
        lesson3 = entityManager.persistAndFlush(lesson3);
    }

    @Test
    void findByCourseOrderByOrderIndexAsc_ShouldReturnOrderedLessons() {
        List<Lesson> lessons = lessonRepository.findByCourseOrderByOrderIndexAsc(course);

        assertThat(lessons).hasSize(3);
        assertThat(lessons.get(0).getOrderIndex()).isEqualTo(1);
        assertThat(lessons.get(1).getOrderIndex()).isEqualTo(2);
        assertThat(lessons.get(2).getOrderIndex()).isEqualTo(3);
    }

    @Test
    void findByCourseAndOrderIndex_ShouldReturnCorrectLesson() {
        Optional<Lesson> found = lessonRepository.findByCourseAndOrderIndex(course, 2);

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Getting Started");
    }

    @Test
    void findMaxOrderIndexByCourse_ShouldReturnMaxOrder() {
        Integer maxOrder = lessonRepository.findMaxOrderIndexByCourse(course);

        assertThat(maxOrder).isEqualTo(3);
    }

    @Test
    void findNextLessons_ShouldReturnLessonsAfterCurrentIndex() {
        List<Lesson> nextLessons = lessonRepository.findNextLessons(course, 1);

        assertThat(nextLessons).hasSize(2);
        assertThat(nextLessons.get(0).getOrderIndex()).isEqualTo(2);
    }

    @Test
    void findByCourseIdOrderByOrderIndex_ShouldReturnOrderedLessons() {
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndex(course.getId());

        assertThat(lessons).hasSize(3);
    }

    @Test
    void save_ShouldPersistLesson() {
        Lesson newLesson = TestDataFactory.createLesson(null, "New Lesson", course, LessonType.VIDEO);
        newLesson.setOrderIndex(4);

        Lesson saved = lessonRepository.save(newLesson);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOrderIndex()).isEqualTo(4);
    }

    @Test
    void delete_ShouldRemoveLesson() {
        lessonRepository.delete(lesson2);
        entityManager.flush();

        List<Lesson> lessons = lessonRepository.findByCourseOrderByOrderIndexAsc(course);
        assertThat(lessons).hasSize(2);
    }
}