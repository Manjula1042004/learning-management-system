package com.lms.entity;

import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StudentAnswerTest {

    private User student;
    private User instructor;
    private Course course;
    private Quiz quiz;
    private Question question;
    private Enrollment enrollment;
    private QuizAttempt quizAttempt;
    private StudentAnswer studentAnswer;

    @BeforeEach
    void setUp() {
        student = TestDataFactory.createStudent(1L);
        instructor = TestDataFactory.createInstructor(2L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        quiz = TestDataFactory.createQuiz(1L, "Test Quiz", course, null);
        question = TestDataFactory.createQuestion(1L, "Test Question", "multiple_choice", quiz);
        enrollment = TestDataFactory.createEnrollment(1L, student, course);
        quizAttempt = TestDataFactory.createQuizAttempt(1L, quiz, student, enrollment);

        studentAnswer = new StudentAnswer();
        studentAnswer.setId(1L);
        studentAnswer.setQuizAttempt(quizAttempt);
        studentAnswer.setQuestion(question);
        studentAnswer.setAnswerText("4");
        studentAnswer.setIsCorrect(true);
        studentAnswer.setPointsEarned(1);
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        studentAnswer.setId(2L);
        studentAnswer.setAnswerText("5");
        studentAnswer.setIsCorrect(false);
        studentAnswer.setPointsEarned(0);

        assertThat(studentAnswer.getId()).isEqualTo(2L);
        assertThat(studentAnswer.getAnswerText()).isEqualTo("5");
        assertThat(studentAnswer.getIsCorrect()).isFalse();
        assertThat(studentAnswer.getPointsEarned()).isEqualTo(0);
    }

    @Test
    void quizAttempt_ShouldBeAccessible() {
        assertThat(studentAnswer.getQuizAttempt()).isEqualTo(quizAttempt);

        QuizAttempt newAttempt = TestDataFactory.createQuizAttempt(2L, quiz, student, enrollment);
        studentAnswer.setQuizAttempt(newAttempt);
        assertThat(studentAnswer.getQuizAttempt()).isEqualTo(newAttempt);
    }

    @Test
    void question_ShouldBeAccessible() {
        assertThat(studentAnswer.getQuestion()).isEqualTo(question);

        Question newQuestion = TestDataFactory.createQuestion(2L, "New Question", "true_false", quiz);
        studentAnswer.setQuestion(newQuestion);
        assertThat(studentAnswer.getQuestion()).isEqualTo(newQuestion);
    }

    @Test
    void setIsCorrect_ShouldHandleNull() {
        studentAnswer.setIsCorrect(null);
        assertThat(studentAnswer.getIsCorrect()).isNull();

        studentAnswer.setIsCorrect(true);
        assertThat(studentAnswer.getIsCorrect()).isTrue();
    }

    @Test
    void setPointsEarned_ShouldUpdateValue() {
        studentAnswer.setPointsEarned(2);
        assertThat(studentAnswer.getPointsEarned()).isEqualTo(2);

        studentAnswer.setPointsEarned(0);
        assertThat(studentAnswer.getPointsEarned()).isEqualTo(0);
    }

    @Test
    void defaultValues_ShouldBeNull() {
        StudentAnswer newAnswer = new StudentAnswer();
        assertThat(newAnswer.getId()).isNull();
        assertThat(newAnswer.getQuizAttempt()).isNull();
        assertThat(newAnswer.getQuestion()).isNull();
        assertThat(newAnswer.getAnswerText()).isNull();
        assertThat(newAnswer.getIsCorrect()).isNull();
        assertThat(newAnswer.getPointsEarned()).isNull();
    }

    @Test
    void correctAnswer_ShouldHaveCorrectFlags() {
        assertThat(studentAnswer.getIsCorrect()).isTrue();
        assertThat(studentAnswer.getPointsEarned()).isEqualTo(1);
    }

    @Test
    void incorrectAnswer_ShouldHaveCorrectFlags() {
        studentAnswer.setIsCorrect(false);
        studentAnswer.setPointsEarned(0);

        assertThat(studentAnswer.getIsCorrect()).isFalse();
        assertThat(studentAnswer.getPointsEarned()).isEqualTo(0);
    }
}