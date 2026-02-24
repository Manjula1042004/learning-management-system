package com.lms.entity;

import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class QuizAttemptTest {

    private User student;
    private User instructor;
    private Course course;
    private Quiz quiz;
    private Enrollment enrollment;
    private QuizAttempt quizAttempt;
    private StudentAnswer answer1;
    private StudentAnswer answer2;

    @BeforeEach
    void setUp() {
        student = TestDataFactory.createStudent(1L);
        instructor = TestDataFactory.createInstructor(2L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        quiz = TestDataFactory.createQuiz(1L, "Test Quiz", course, null);
        enrollment = TestDataFactory.createEnrollment(1L, student, course);

        quizAttempt = new QuizAttempt();
        quizAttempt.setId(1L);
        quizAttempt.setQuiz(quiz);
        quizAttempt.setStudent(student);
        quizAttempt.setEnrollment(enrollment);
        quizAttempt.setAttemptNumber(1);
        quizAttempt.setStatus("in_progress");
        quizAttempt.setStartedAt(LocalDateTime.now());

        answer1 = new StudentAnswer();
        answer1.setId(1L);
        answer1.setQuizAttempt(quizAttempt);
        answer1.setAnswerText("4");
        answer1.setIsCorrect(true);
        answer1.setPointsEarned(1);

        answer2 = new StudentAnswer();
        answer2.setId(2L);
        answer2.setQuizAttempt(quizAttempt);
        answer2.setAnswerText("3");
        answer2.setIsCorrect(false);
        answer2.setPointsEarned(0);

        Set<StudentAnswer> answers = new HashSet<>();
        answers.add(answer1);
        answers.add(answer2);
        quizAttempt.setStudentAnswers(answers);
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        quizAttempt.setId(2L);
        quizAttempt.setAttemptNumber(2);
        quizAttempt.setScore(85);
        quizAttempt.setPercentage(85);
        quizAttempt.setStatus("passed");

        LocalDateTime completedAt = LocalDateTime.now();
        quizAttempt.setCompletedAt(completedAt);

        assertThat(quizAttempt.getId()).isEqualTo(2L);
        assertThat(quizAttempt.getAttemptNumber()).isEqualTo(2);
        assertThat(quizAttempt.getScore()).isEqualTo(85);
        assertThat(quizAttempt.getPercentage()).isEqualTo(85);
        assertThat(quizAttempt.getStatus()).isEqualTo("passed");
        assertThat(quizAttempt.getCompletedAt()).isEqualTo(completedAt);
    }

    @Test
    void quiz_ShouldBeAccessible() {
        assertThat(quizAttempt.getQuiz()).isEqualTo(quiz);

        Quiz newQuiz = TestDataFactory.createQuiz(2L, "New Quiz", course, null);
        quizAttempt.setQuiz(newQuiz);
        assertThat(quizAttempt.getQuiz()).isEqualTo(newQuiz);
    }

    @Test
    void student_ShouldBeAccessible() {
        assertThat(quizAttempt.getStudent()).isEqualTo(student);

        User newStudent = TestDataFactory.createStudent(3L);
        quizAttempt.setStudent(newStudent);
        assertThat(quizAttempt.getStudent()).isEqualTo(newStudent);
    }

    @Test
    void enrollment_ShouldBeAccessible() {
        assertThat(quizAttempt.getEnrollment()).isEqualTo(enrollment);

        Enrollment newEnrollment = TestDataFactory.createEnrollment(2L, student, course);
        quizAttempt.setEnrollment(newEnrollment);
        assertThat(quizAttempt.getEnrollment()).isEqualTo(newEnrollment);
    }

    @Test
    void studentAnswers_ShouldBeManageable() {
        assertThat(quizAttempt.getStudentAnswers()).hasSize(2);
        assertThat(quizAttempt.getStudentAnswers()).contains(answer1, answer2);

        StudentAnswer answer3 = new StudentAnswer();
        answer3.setId(3L);
        answer3.setQuizAttempt(quizAttempt);

        quizAttempt.getStudentAnswers().add(answer3);
        assertThat(quizAttempt.getStudentAnswers()).hasSize(3);

        quizAttempt.getStudentAnswers().remove(answer1);
        assertThat(quizAttempt.getStudentAnswers()).hasSize(2);
        assertThat(quizAttempt.getStudentAnswers()).doesNotContain(answer1);
    }

    @Test
    void setStudentAnswers_ShouldReplaceAnswers() {
        Set<StudentAnswer> newAnswers = new HashSet<>();
        StudentAnswer newAnswer = new StudentAnswer();
        newAnswer.setId(3L);
        newAnswer.setQuizAttempt(quizAttempt);
        newAnswer.setAnswerText("New Answer");
        newAnswers.add(newAnswer);

        quizAttempt.setStudentAnswers(newAnswers);
        assertThat(quizAttempt.getStudentAnswers()).hasSize(1);
        assertThat(quizAttempt.getStudentAnswers()).contains(newAnswer);
    }

    @Test
    void prePersist_ShouldSetStartedAtAndStatus() {
        QuizAttempt newAttempt = new QuizAttempt();
        newAttempt.onCreate();

        assertThat(newAttempt.getStartedAt()).isNotNull();
        assertThat(newAttempt.getStatus()).isEqualTo("in_progress");
    }

    @Test
    void isPassed_ShouldReturnTrue_WhenStatusIsPassed() {
        quizAttempt.setStatus("passed");
        assertThat(quizAttempt.isPassed()).isTrue();

        quizAttempt.setStatus("failed");
        assertThat(quizAttempt.isPassed()).isFalse();

        quizAttempt.setStatus("in_progress");
        assertThat(quizAttempt.isPassed()).isFalse();
    }

    @Test
    void isCompleted_ShouldReturnTrue_WhenCompletedAtIsSet() {
        assertThat(quizAttempt.isCompleted()).isFalse();

        quizAttempt.setCompletedAt(LocalDateTime.now());
        assertThat(quizAttempt.isCompleted()).isTrue();
    }

    @Test
    void defaultValues_ShouldBeNull() {
        QuizAttempt newAttempt = new QuizAttempt();
        assertThat(newAttempt.getId()).isNull();
        assertThat(newAttempt.getQuiz()).isNull();
        assertThat(newAttempt.getStudent()).isNull();
        assertThat(newAttempt.getEnrollment()).isNull();
        assertThat(newAttempt.getAttemptNumber()).isNull();
        assertThat(newAttempt.getScore()).isNull();
        assertThat(newAttempt.getPercentage()).isNull();
        assertThat(newAttempt.getStudentAnswers()).isEmpty();
    }
}