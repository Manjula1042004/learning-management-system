package com.lms.entity;

import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OptionTest {

    private Quiz quiz;
    private Question question;
    private Option option;

    @BeforeEach
    void setUp() {
        User instructor = TestDataFactory.createInstructor(1L);
        Course course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        quiz = TestDataFactory.createQuiz(1L, "Test Quiz", course, null);
        question = TestDataFactory.createQuestion(1L, "Test Question", "multiple_choice", quiz);

        option = new Option();
        option.setId(1L);
        option.setQuestion(question);
        option.setOptionText("Test Option");
        option.setIsCorrect(true);
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        option.setId(2L);
        option.setOptionText("Updated Option");
        option.setIsCorrect(false);

        assertThat(option.getId()).isEqualTo(2L);
        assertThat(option.getOptionText()).isEqualTo("Updated Option");
        assertThat(option.getIsCorrect()).isFalse();
    }

    @Test
    void question_ShouldBeAccessible() {
        assertThat(option.getQuestion()).isEqualTo(question);

        Question newQuestion = TestDataFactory.createQuestion(2L, "New Question", "multiple_choice", quiz);
        option.setQuestion(newQuestion);
        assertThat(option.getQuestion()).isEqualTo(newQuestion);
    }

    @Test
    void setIsCorrect_ShouldHandleNull() {
        option.setIsCorrect(null);
        assertThat(option.getIsCorrect()).isNull();

        option.setIsCorrect(true);
        assertThat(option.getIsCorrect()).isTrue();
    }

    @Test
    void defaultValues_ShouldBeNull() {
        Option newOption = new Option();
        assertThat(newOption.getId()).isNull();
        assertThat(newOption.getQuestion()).isNull();
        assertThat(newOption.getOptionText()).isNull();
        assertThat(newOption.getIsCorrect()).isNull();
    }
}