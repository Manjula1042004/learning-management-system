package com.lms.repository;

import com.lms.entity.*;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    private User instructor;
    private Course course;
    private Quiz quiz;
    private Question question1;
    private Question question2;

    @BeforeEach
    void setUp() {
        // Clear repositories in correct order
        questionRepository.deleteAll();
        quizRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // Create user
        instructor = userRepository.save(TestDataFactory.createInstructor(null));

        // Create course
        course = TestDataFactory.createCourse(null, "Test Course", instructor);
        course = courseRepository.save(course);

        // Create quiz
        quiz = TestDataFactory.createQuiz(null, "Test Quiz", course, null);
        quiz = quizRepository.save(quiz);

        // Create questions
        question1 = TestDataFactory.createQuestion(null, "Question 1", "multiple_choice", quiz);
        question1.setPoints(1);
        question1 = questionRepository.save(question1);

        question2 = TestDataFactory.createQuestion(null, "Question 2", "true_false", quiz);
        question2.setPoints(2);
        question2 = questionRepository.save(question2);
    }

    @Test
    void testFindByQuizId() {
        List<Question> questions = questionRepository.findByQuizId(quiz.getId());
        assertThat(questions).hasSize(2);
        assertThat(questions).extracting(Question::getQuestionText)
                .containsExactlyInAnyOrder("Question 1", "Question 2");
    }

    @Test
    void testDeleteByQuizId() {
        questionRepository.deleteByQuizId(quiz.getId());
        List<Question> questions = questionRepository.findByQuizId(quiz.getId());
        assertThat(questions).isEmpty();
    }

    @Test
    void testCountByQuizId() {
        long count = questionRepository.countByQuizId(quiz.getId());
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testFindById() {
        Question found = questionRepository.findById(question1.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getQuestionText()).isEqualTo("Question 1");
    }
}