package com.lms.entity;

import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class QuizTest {

    private User instructor;
    private Course course;
    private Lesson lesson;
    private Quiz quiz;
    private Question question1;
    private Question question2;

    @BeforeEach
    void setUp() {
        instructor = TestDataFactory.createInstructor(1L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        lesson = TestDataFactory.createLesson(1L, "Quiz Lesson", course, LessonType.QUIZ);

        quiz = new Quiz(
                "Test Quiz",
                "Test Description",
                30,
                70,
                3,
                course,
                lesson
        );
        quiz.setId(1L);

        question1 = new Question();
        question1.setId(1L);
        question1.setQuiz(quiz);
        question1.setQuestionText("Question 1");
        question1.setQuestionType("multiple_choice");
        question1.setPoints(1);

        question2 = new Question();
        question2.setId(2L);
        question2.setQuiz(quiz);
        question2.setQuestionText("Question 2");
        question2.setQuestionType("true_false");
        question2.setPoints(2);

        Set<Question> questions = new HashSet<>();
        questions.add(question1);
        questions.add(question2);
        quiz.setQuestions(questions);
        quiz.setTotalQuestions(2);
    }

    @Test
    void constructor_ShouldCreateQuiz() {
        Quiz newQuiz = new Quiz(
                "New Quiz",
                "New Description",
                45,
                80,
                5,
                course,
                null
        );

        assertThat(newQuiz.getTitle()).isEqualTo("New Quiz");
        assertThat(newQuiz.getDescription()).isEqualTo("New Description");
        assertThat(newQuiz.getDuration()).isEqualTo(45);
        assertThat(newQuiz.getPassingScore()).isEqualTo(80);
        assertThat(newQuiz.getMaxAttempts()).isEqualTo(5);
        assertThat(newQuiz.getCourse()).isEqualTo(course);
        assertThat(newQuiz.getLesson()).isNull();
        assertThat(newQuiz.getTotalQuestions()).isEqualTo(0);
        assertThat(newQuiz.getQuestions()).isEmpty();
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        quiz.setId(2L);
        quiz.setTitle("Updated Quiz");
        quiz.setDescription("Updated Description");
        quiz.setDuration(60);
        quiz.setPassingScore(75);
        quiz.setMaxAttempts(4);
        quiz.setTotalQuestions(5);

        LocalDateTime now = LocalDateTime.now();
        quiz.setCreatedAt(now);
        quiz.setUpdatedAt(now);

        assertThat(quiz.getId()).isEqualTo(2L);
        assertThat(quiz.getTitle()).isEqualTo("Updated Quiz");
        assertThat(quiz.getDescription()).isEqualTo("Updated Description");
        assertThat(quiz.getDuration()).isEqualTo(60);
        assertThat(quiz.getPassingScore()).isEqualTo(75);
        assertThat(quiz.getMaxAttempts()).isEqualTo(4);
        assertThat(quiz.getTotalQuestions()).isEqualTo(5);
        assertThat(quiz.getCreatedAt()).isEqualTo(now);
        assertThat(quiz.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void course_ShouldBeAccessible() {
        assertThat(quiz.getCourse()).isEqualTo(course);

        Course newCourse = TestDataFactory.createCourse(2L, "New Course", instructor);
        quiz.setCourse(newCourse);
        assertThat(quiz.getCourse()).isEqualTo(newCourse);
    }

    @Test
    void lesson_ShouldBeAccessible() {
        assertThat(quiz.getLesson()).isEqualTo(lesson);

        Lesson newLesson = TestDataFactory.createLesson(2L, "New Lesson", course, LessonType.QUIZ);
        quiz.setLesson(newLesson);
        assertThat(quiz.getLesson()).isEqualTo(newLesson);
    }

    @Test
    void questions_ShouldBeManageable() {
        assertThat(quiz.getQuestions()).hasSize(2);
        assertThat(quiz.getTotalQuestions()).isEqualTo(2);

        Question question3 = new Question();
        question3.setId(3L);
        question3.setQuiz(quiz);
        question3.setQuestionText("Question 3");

        quiz.getQuestions().add(question3);
        assertThat(quiz.getQuestions()).hasSize(3);

        quiz.setTotalQuestions(3);
        assertThat(quiz.getTotalQuestions()).isEqualTo(3);

        quiz.getQuestions().remove(question1);
        assertThat(quiz.getQuestions()).hasSize(2);
        assertThat(quiz.getQuestions()).doesNotContain(question1);
    }

    @Test
    void setQuestions_ShouldReplaceQuestions() {
        Set<Question> newQuestions = new HashSet<>();
        Question newQuestion = new Question();
        newQuestion.setId(3L);
        newQuestion.setQuiz(quiz);
        newQuestion.setQuestionText("New Question");
        newQuestions.add(newQuestion);

        quiz.setQuestions(newQuestions);
        quiz.setTotalQuestions(1);

        assertThat(quiz.getQuestions()).hasSize(1);
        assertThat(quiz.getQuestions()).contains(newQuestion);
        assertThat(quiz.getTotalQuestions()).isEqualTo(1);
    }

    @Test
    void prePersist_ShouldSetCreatedAndUpdatedAt() {
        Quiz newQuiz = new Quiz();
        newQuiz.onCreate();

        assertThat(newQuiz.getCreatedAt()).isNotNull();
        assertThat(newQuiz.getUpdatedAt()).isNotNull();
    }

    @Test
    void preUpdate_ShouldUpdateUpdatedAt() {
        LocalDateTime oldUpdatedAt = quiz.getUpdatedAt();
        quiz.onUpdate();

        assertThat(quiz.getUpdatedAt()).isNotEqualTo(oldUpdatedAt);
        assertThat(quiz.getUpdatedAt()).isAfterOrEqualTo(oldUpdatedAt);
    }

    @Test
    void defaultValues_ShouldBeSet() {
        Quiz newQuiz = new Quiz();
        assertThat(newQuiz.getMaxAttempts()).isNull(); // Not set in default constructor
        assertThat(newQuiz.getTotalQuestions()).isNull(); // Not set in default constructor
    }
}