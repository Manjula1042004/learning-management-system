package com.lms.controller;

import com.lms.entity.*;
import com.lms.service.*;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstructorQuizController.class)
@WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
class InstructorQuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizService quizService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private LessonService lessonService;

    @MockBean
    private UserService userService;

    private User instructor;
    private User otherInstructor;
    private Course course;
    private Lesson lesson;
    private Quiz quiz;
    private Question question;
    private Option option;

    @BeforeEach
    void setUp() {
        instructor = TestDataFactory.createInstructor(1L);
        otherInstructor = TestDataFactory.createInstructor(2L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        lesson = TestDataFactory.createLesson(1L, "Quiz Lesson", course, LessonType.QUIZ);

        quiz = TestDataFactory.createQuiz(1L, "Test Quiz", course, lesson);

        question = TestDataFactory.createQuestion(1L, "What is 2+2?", "multiple_choice", quiz);
        option = TestDataFactory.createOption(1L, "4", true, question);

        Set<Option> options = new HashSet<>();
        options.add(option);
        question.setOptions(options);

        Set<Question> questions = new HashSet<>();
        questions.add(question);
        quiz.setQuestions(questions);
    }

    @Test
    void showEditQuizForm_ShouldReturnEditPage_WhenOwner() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(quizService.getQuizById(1L)).thenReturn(Optional.of(quiz));

        mockMvc.perform(get("/instructor/quiz/edit/{quizId}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/quiz/edit"))
                .andExpect(model().attributeExists("quiz"))
                .andExpect(model().attributeExists("course"));
    }

    @Test
    void showEditQuizForm_ShouldRedirect_WhenNotOwner() throws Exception {
        Course otherCourse = TestDataFactory.createCourse(1L, "Other Course", otherInstructor);
        Quiz otherQuiz = TestDataFactory.createQuiz(1L, "Other Quiz", otherCourse, lesson);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(quizService.getQuizById(1L)).thenReturn(Optional.of(otherQuiz));

        mockMvc.perform(get("/instructor/quiz/edit/{quizId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void updateQuiz_ShouldUpdateAndRedirect_WhenOwner() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(quizService.getQuizById(1L)).thenReturn(Optional.of(quiz));
        when(quizService.updateQuiz(any(Quiz.class))).thenReturn(quiz);

        mockMvc.perform(post("/instructor/quiz/edit/{quizId}", 1L)
                        .param("title", "Updated Quiz")
                        .param("description", "Updated Description")
                        .param("duration", "45")
                        .param("passingScore", "80")
                        .param("maxAttempts", "5")
                        .param("questionText", "What is 2+2?")
                        .param("questionType", "multiple_choice")
                        .param("points", "1")
                        .param("optionText", "4")
                        .param("isCorrect", "q0_o0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void updateQuiz_ShouldRedirect_WhenNotOwner() throws Exception {
        Course otherCourse = TestDataFactory.createCourse(1L, "Other Course", otherInstructor);
        Quiz otherQuiz = TestDataFactory.createQuiz(1L, "Other Quiz", otherCourse, lesson);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(quizService.getQuizById(1L)).thenReturn(Optional.of(otherQuiz));

        mockMvc.perform(post("/instructor/quiz/edit/{quizId}", 1L)
                        .param("title", "Updated Quiz")
                        .param("description", "Updated Description")
                        .param("duration", "45")
                        .param("passingScore", "80"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void deleteQuiz_ShouldDeleteAndRedirect_WhenOwner() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(quizService.getQuizById(1L)).thenReturn(Optional.of(quiz));
        doNothing().when(quizService).deleteQuiz(1L);

        mockMvc.perform(post("/instructor/quiz/delete/{quizId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1"))
                .andExpect(flash().attributeExists("success"));

        verify(quizService).deleteQuiz(1L);
    }

    @Test
    void deleteQuiz_ShouldRedirect_WhenNotOwner() throws Exception {
        Course otherCourse = TestDataFactory.createCourse(1L, "Other Course", otherInstructor);
        Quiz otherQuiz = TestDataFactory.createQuiz(1L, "Other Quiz", otherCourse, lesson);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(quizService.getQuizById(1L)).thenReturn(Optional.of(otherQuiz));

        mockMvc.perform(post("/instructor/quiz/delete/{quizId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"))
                .andExpect(flash().attributeExists("error"));

        verify(quizService, never()).deleteQuiz(anyLong());
    }

    @Test
    void showCreateQuizForm_ShouldReturnCreatePage() throws Exception {
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));

        mockMvc.perform(get("/instructor/quiz/create/{courseId}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/quiz/create"))
                .andExpect(model().attributeExists("course"));
    }

    @Test
    void showCreateQuizForm_WithLessonId_ShouldIncludeLesson() throws Exception {
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(lesson));

        mockMvc.perform(get("/instructor/quiz/create/{courseId}", 1L)
                        .param("lessonId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/quiz/create"))
                .andExpect(model().attributeExists("lesson"));
    }

    @Test
    void createQuiz_ShouldCreateQuizAndRedirect() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        when(lessonService.getLessonById(anyLong())).thenReturn(Optional.of(lesson));
        when(quizService.createQuiz(any(Quiz.class), eq(course), eq(lesson))).thenReturn(quiz);

        mockMvc.perform(post("/instructor/quiz/create/{courseId}", 1L)
                        .param("lessonId", "1")
                        .param("title", "New Quiz")
                        .param("description", "Quiz Description")
                        .param("duration", "30")
                        .param("passingScore", "70")
                        .param("maxAttempts", "3")
                        .param("questionText", "What is 2+2?")
                        .param("questionType", "multiple_choice")
                        .param("points", "1")
                        .param("optionText", "4")
                        .param("isCorrect", "q0_o0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void createQuiz_ShouldCreateEmptyQuiz_WhenNoQuestions() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        when(quizService.createQuiz(any(Quiz.class), eq(course), isNull())).thenReturn(quiz);

        mockMvc.perform(post("/instructor/quiz/create/{courseId}", 1L)
                        .param("title", "Empty Quiz")
                        .param("description", "Description")
                        .param("duration", "30")
                        .param("passingScore", "70"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void createQuiz_ShouldHandleError() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        when(quizService.createQuiz(any(Quiz.class), eq(course), isNull()))
                .thenThrow(new RuntimeException("Creation failed"));

        mockMvc.perform(post("/instructor/quiz/create/{courseId}", 1L)
                        .param("title", "New Quiz")
                        .param("description", "Description")
                        .param("duration", "30")
                        .param("passingScore", "70"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/instructor/quiz/create/1"))
                .andExpect(flash().attributeExists("error"));
    }
}