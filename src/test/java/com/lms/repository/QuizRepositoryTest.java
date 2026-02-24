package com.lms.repository;

import com.lms.entity.*;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class QuizRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    private User instructor;
    private Course course;
    private Lesson lesson;
    private Quiz quiz1;
    private Quiz quiz2;

    @BeforeEach
    void setUp() {
        // Clear repositories in correct order
        quizRepository.deleteAll();
        lessonRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // Create user
        instructor = userRepository.save(TestDataFactory.createInstructor(null));

        // Create course
        course = TestDataFactory.createCourse(null, "Test Course", instructor);
        course = courseRepository.save(course);

        // Create lesson
        lesson = TestDataFactory.createLesson(null, "Quiz Lesson", course, LessonType.QUIZ);
        lesson = lessonRepository.save(lesson);

        // Create quizzes
        quiz1 = TestDataFactory.createQuiz(null, "Quiz 1", course, lesson);
        quiz1 = quizRepository.save(quiz1);

        quiz2 = TestDataFactory.createQuiz(null, "Quiz 2", course, null);
        quiz2 = quizRepository.save(quiz2);
    }

    @Test
    void testFindByLessonId() {
        Optional<Quiz> found = quizRepository.findByLessonId(lesson.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Quiz 1");
    }

    @Test
    void testFindByCourseId() {
        List<Quiz> quizzes = quizRepository.findByCourseId(course.getId());
        assertThat(quizzes).hasSize(2);
    }

    @Test
    void testExistsByLessonId() {
        assertThat(quizRepository.existsByLessonId(lesson.getId())).isTrue();
    }

    @Test
    void testCountByCourseId() {
        long count = quizRepository.countByCourseId(course.getId());
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testDeleteByLessonId() {
        quizRepository.deleteByLessonId(lesson.getId());
        assertThat(quizRepository.existsByLessonId(lesson.getId())).isFalse();
    }
}