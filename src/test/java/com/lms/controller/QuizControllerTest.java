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

@WebMvcTest(QuizController.class)
@WithMockUser(username = "student1", roles = "STUDENT")
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizService quizService;

    @MockBean
    private UserService userService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private LessonService lessonService;

    @MockBean
    private EnrollmentService enrollmentService;

    private User student;
    private User instructor;
    private Course course;
    private Lesson lesson;
    private Quiz quiz;
    private Enrollment enrollment;
    private QuizAttempt attempt;

    @BeforeEach
    void setUp() {
        student = TestDataFactory.createStudent(1L);
        instructor = TestDataFactory.createInstructor(2L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        lesson = TestDataFactory.createLesson(1L, "Quiz Lesson", course, LessonType.QUIZ);
        quiz = TestDataFactory.createQuiz(1L, "Test Quiz", course, lesson);
        enrollment = TestDataFactory.createEnrollment(1L, student, course);
        attempt = TestDataFactory.createQuizAttempt(1L, quiz, student, enrollment);
    }

    @Test
    void viewQuiz_ShouldShowQuiz_WhenEnrolled() throws Exception {
        List<QuizAttempt> previousAttempts = Arrays.asList();

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(quizService.getQuizById(1L)).thenReturn(Optional.of(quiz));
        when(enrollmentService.getEnrollment(student, course)).thenReturn(Optional.of(enrollment));
        when(quizService.getQuizAttemptsByEnrollment(enrollment)).thenReturn(previousAttempts);

        mockMvc.perform(get("/quiz/{quizId}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("student/quiz"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("quiz"))
                .andExpect(model().attributeExists("previousAttempts"));
    }

    @Test
    void viewQuiz_ShouldRedirect_WhenNotEnrolled() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(quizService.getQuizById(1L)).thenReturn(Optional.of(quiz));
        when(enrollmentService.getEnrollment(student, course)).thenReturn(Optional.empty());

        mockMvc.perform(get("/quiz/{quizId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1?error=not_enrolled"));
    }

    @Test
    void startQuiz_ShouldCreateAttemptAndRedirect() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(quizService.getQuizById(1L)).thenReturn(Optional.of(quiz));
        when(enrollmentService.getEnrollment(student, course)).thenReturn(Optional.of(enrollment));
        when(quizService.startQuizAttempt(quiz, student)).thenReturn(attempt);

        mockMvc.perform(post("/quiz/start/{quizId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz/attempt/1"));
    }

    @Test
    void startQuiz_ShouldRedirect_WhenNotEnrolled() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(quizService.getQuizById(1L)).thenReturn(Optional.of(quiz));
        when(enrollmentService.getEnrollment(student, course)).thenReturn(Optional.empty());

        mockMvc.perform(post("/quiz/start/{quizId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1?error=not_enrolled"));
    }

    @Test
    void takeQuiz_ShouldShowQuizAttempt_WhenOwnedByStudent() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(quizService.getQuizAttemptById(1L)).thenReturn(Optional.of(attempt));

        mockMvc.perform(get("/quiz/attempt/{attemptId}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("student/take-quiz"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("attempt"))
                .andExpect(model().attributeExists("quiz"));
    }

    @Test
    void takeQuiz_ShouldRedirect_WhenNotOwnedByStudent() throws Exception {
        User otherStudent = TestDataFactory.createStudent(3L);
        QuizAttempt otherAttempt = TestDataFactory.createQuizAttempt(1L, quiz, otherStudent, enrollment);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(quizService.getQuizAttemptById(1L)).thenReturn(Optional.of(otherAttempt));

        mockMvc.perform(get("/quiz/attempt/{attemptId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz/1?error=unauthorized"));
    }

    @Test
    void submitQuiz_ShouldSubmitAnswersAndRedirect() throws Exception {
        Map<String, String> answers = new HashMap<>();
        answers.put("question_1", "4");
        answers.put("question_2", "true");

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(quizService.getQuizAttemptById(1L)).thenReturn(Optional.of(attempt));
        when(quizService.submitQuizAttempt(eq(1L), anyMap())).thenReturn(attempt);

        mockMvc.perform(post("/quiz/submit/{attemptId}", 1L)
                        .param("question_1", "4")
                        .param("question_2", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz/result/1"));
    }

    @Test
    void submitQuiz_ShouldRedirect_WhenNotOwnedByStudent() throws Exception {
        User otherStudent = TestDataFactory.createStudent(3L);
        QuizAttempt otherAttempt = TestDataFactory.createQuizAttempt(1L, quiz, otherStudent, enrollment);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(quizService.getQuizAttemptById(1L)).thenReturn(Optional.of(otherAttempt));

        mockMvc.perform(post("/quiz/submit/{attemptId}", 1L)
                        .param("question_1", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz/1?error=unauthorized"));
    }

    @Test
    void viewResult_ShouldShowResult_WhenOwnedByStudent() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(quizService.getQuizAttemptById(1L)).thenReturn(Optional.of(attempt));

        mockMvc.perform(get("/quiz/result/{attemptId}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("student/quiz-result"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("attempt"))
                .andExpect(model().attributeExists("quiz"));
    }

    @Test
    void viewResult_ShouldRedirect_WhenNotOwnedByStudent() throws Exception {
        User otherStudent = TestDataFactory.createStudent(3L);
        QuizAttempt otherAttempt = TestDataFactory.createQuizAttempt(1L, quiz, otherStudent, enrollment);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(quizService.getQuizAttemptById(1L)).thenReturn(Optional.of(otherAttempt));

        mockMvc.perform(get("/quiz/result/{attemptId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz/1?error=unauthorized"));
    }

    @Test
    void courseDetails_ShouldShowCourseWithQuizzes() throws Exception {
        List<Lesson> lessons = Arrays.asList(lesson);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        when(lessonService.getLessonsByCourse(course)).thenReturn(lessons);
        when(quizService.getQuizByLesson(1L)).thenReturn(Optional.of(quiz));
        when(enrollmentService.getEnrollment(student, course)).thenReturn(Optional.of(enrollment));

        mockMvc.perform(get("/quiz/courses/{courseId}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/details"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeExists("lessons"))
                .andExpect(model().attribute("enrolled", true));
    }
}