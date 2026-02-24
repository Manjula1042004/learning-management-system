package com.lms.repository;

import com.lms.entity.*;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class StudentAnswerRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    private User instructor;
    private User student;
    private Course course;
    private Quiz quiz;
    private Question question;
    private Enrollment enrollment;
    private QuizAttempt attempt;
    private StudentAnswer answer;

    @BeforeEach
    void setUp() {
        // Clear repositories in correct order
        studentAnswerRepository.deleteAll();
        quizAttemptRepository.deleteAll();
        enrollmentRepository.deleteAll();
        questionRepository.deleteAll();
        quizRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // Create users
        instructor = userRepository.save(TestDataFactory.createInstructor(null));
        student = userRepository.save(TestDataFactory.createStudent(null));

        // Create course
        course = TestDataFactory.createCourse(null, "Test Course", instructor);
        course = courseRepository.save(course);

        // Create quiz
        quiz = TestDataFactory.createQuiz(null, "Test Quiz", course, null);
        quiz = quizRepository.save(quiz);

        // Create question
        question = TestDataFactory.createQuestion(null, "Test Question", "multiple_choice", quiz);
        question = questionRepository.save(question);

        // Create enrollment
        enrollment = TestDataFactory.createEnrollment(null, student, course);
        enrollment = enrollmentRepository.save(enrollment);

        // Create attempt
        attempt = TestDataFactory.createQuizAttempt(null, quiz, student, enrollment);
        attempt = quizAttemptRepository.save(attempt);

        // Create answer
        answer = new StudentAnswer();
        answer.setQuizAttempt(attempt);
        answer.setQuestion(question);
        answer.setAnswerText("4");
        answer.setIsCorrect(true);
        answer.setPointsEarned(1);
        answer = studentAnswerRepository.save(answer);
    }

    @Test
    void testSaveAndFindById() {
        Optional<StudentAnswer> found = studentAnswerRepository.findById(answer.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getAnswerText()).isEqualTo("4");
        assertThat(found.get().getIsCorrect()).isTrue();
    }

    @Test
    void testDelete() {
        studentAnswerRepository.delete(answer);
        Optional<StudentAnswer> found = studentAnswerRepository.findById(answer.getId());
        assertThat(found).isEmpty();
    }
}