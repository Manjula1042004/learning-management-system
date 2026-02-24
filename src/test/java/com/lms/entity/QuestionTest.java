package com.lms.entity;

import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionTest {

    private User instructor;
    private Course course;
    private Quiz quiz;
    private Question question;
    private Option option1;
    private Option option2;

    @BeforeEach
    void setUp() {
        instructor = TestDataFactory.createInstructor(1L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        quiz = TestDataFactory.createQuiz(1L, "Test Quiz", course, null);

        question = new Question();
        question.setId(1L);
        question.setQuiz(quiz);
        question.setQuestionText("What is 2+2?");
        question.setQuestionType("multiple_choice");
        question.setPoints(2);
        question.setCorrectAnswer("4");

        option1 = new Option();
        option1.setId(1L);
        option1.setQuestion(question);
        option1.setOptionText("3");
        option1.setIsCorrect(false);

        option2 = new Option();
        option2.setId(2L);
        option2.setQuestion(question);
        option2.setOptionText("4");
        option2.setIsCorrect(true);

        Set<Option> options = new HashSet<>();
        options.add(option1);
        options.add(option2);
        question.setOptions(options);
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        question.setId(2L);
        question.setQuestionText("What is 5+5?");
        question.setQuestionType("true_false");
        question.setPoints(3);
        question.setCorrectAnswer("10");

        assertThat(question.getId()).isEqualTo(2L);
        assertThat(question.getQuestionText()).isEqualTo("What is 5+5?");
        assertThat(question.getQuestionType()).isEqualTo("true_false");
        assertThat(question.getPoints()).isEqualTo(3);
        assertThat(question.getCorrectAnswer()).isEqualTo("10");
    }

    @Test
    void quiz_ShouldBeAccessible() {
        assertThat(question.getQuiz()).isEqualTo(quiz);

        Quiz newQuiz = TestDataFactory.createQuiz(2L, "New Quiz", course, null);
        question.setQuiz(newQuiz);
        assertThat(question.getQuiz()).isEqualTo(newQuiz);
    }

    @Test
    void options_ShouldBeManageable() {
        assertThat(question.getOptions()).hasSize(2);
        assertThat(question.getOptions()).contains(option1, option2);

        Option option3 = new Option();
        option3.setId(3L);
        option3.setQuestion(question);
        option3.setOptionText("5");
        option3.setIsCorrect(false);

        question.getOptions().add(option3);
        assertThat(question.getOptions()).hasSize(3);

        question.getOptions().remove(option1);
        assertThat(question.getOptions()).hasSize(2);
        assertThat(question.getOptions()).doesNotContain(option1);
    }

    @Test
    void setOptions_ShouldReplaceOptions() {
        Set<Option> newOptions = new HashSet<>();
        Option newOption = new Option();
        newOption.setId(3L);
        newOption.setQuestion(question);
        newOption.setOptionText("New Option");
        newOption.setIsCorrect(true);
        newOptions.add(newOption);

        question.setOptions(newOptions);
        assertThat(question.getOptions()).hasSize(1);
        assertThat(question.getOptions()).contains(newOption);
    }

    @Test
    void defaultValues_ShouldBeNull() {
        Question newQuestion = new Question();
        assertThat(newQuestion.getId()).isNull();
        assertThat(newQuestion.getQuiz()).isNull();
        assertThat(newQuestion.getQuestionText()).isNull();
        assertThat(newQuestion.getQuestionType()).isNull();
        assertThat(newQuestion.getPoints()).isNull();
        assertThat(newQuestion.getCorrectAnswer()).isNull();
        assertThat(newQuestion.getOptions()).isEmpty();
    }

    @Test
    void points_ShouldDefaultToOne_WhenNotSet() {
        Question newQuestion = new Question();
        newQuestion.setPoints(null);
        assertThat(newQuestion.getPoints()).isNull();
    }

    @Test
    void multipleChoiceQuestion_ShouldHaveOptions() {
        assertThat(question.getQuestionType()).isEqualTo("multiple_choice");
        assertThat(question.getOptions()).isNotEmpty();

        boolean hasCorrectOption = question.getOptions().stream()
                .anyMatch(Option::getIsCorrect);
        assertThat(hasCorrectOption).isTrue();
    }

    @Test
    void trueFalseQuestion_ShouldHaveCorrectAnswer() {
        question.setQuestionType("true_false");
        question.setCorrectAnswer("true");
        question.setOptions(new HashSet<>());

        assertThat(question.getQuestionType()).isEqualTo("true_false");
        assertThat(question.getCorrectAnswer()).isEqualTo("true");
        assertThat(question.getOptions()).isEmpty();
    }

    @Test
    void shortAnswerQuestion_ShouldHaveCorrectAnswer() {
        question.setQuestionType("short_answer");
        question.setCorrectAnswer("4");
        question.setOptions(new HashSet<>());

        assertThat(question.getQuestionType()).isEqualTo("short_answer");
        assertThat(question.getCorrectAnswer()).isEqualTo("4");
        assertThat(question.getOptions()).isEmpty();
    }
}