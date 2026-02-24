package com.lms.repository;

import com.lms.entity.*;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OptionRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private User instructor;
    private Course course;
    private Quiz quiz;
    private Question question;
    private Option option1;
    private Option option2;

    @BeforeEach
    void setUp() {
        // Clear repositories in correct order
        optionRepository.deleteAll();
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

        // Create question
        question = TestDataFactory.createQuestion(null, "Test Question", "multiple_choice", quiz);
        question = questionRepository.save(question);

        // Create options
        option1 = TestDataFactory.createOption(null, "Option A", false, question);
        option1 = optionRepository.save(option1);

        option2 = TestDataFactory.createOption(null, "Option B", true, question);
        option2 = optionRepository.save(option2);
    }

    @Test
    void testFindByQuestionId() {
        List<Option> options = optionRepository.findByQuestionId(question.getId());
        assertThat(options).hasSize(2);
        assertThat(options).extracting(Option::getOptionText)
                .containsExactlyInAnyOrder("Option A", "Option B");
    }

    @Test
    void testDeleteByQuestionId() {
        optionRepository.deleteByQuestionId(question.getId());
        List<Option> options = optionRepository.findByQuestionId(question.getId());
        assertThat(options).isEmpty();
    }

    @Test
    void testCountByQuestionId() {
        long count = optionRepository.countByQuestionId(question.getId());
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testSaveAndFindById() {
        Option found = optionRepository.findById(option1.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getOptionText()).isEqualTo("Option A");
    }
}