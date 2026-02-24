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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@WithMockUser(username = "student1", roles = "STUDENT")
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private LessonService lessonService;

    @MockBean
    private QuizService quizService;

    @MockBean
    private LessonProgressService lessonProgressService;

    @MockBean
    private MediaService mediaService;

    private User student;
    private User instructor;
    private Course course;
    private Enrollment enrollment;
    private Lesson lesson;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        student = TestDataFactory.createStudent(1L);
        instructor = TestDataFactory.createInstructor(2L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        enrollment = TestDataFactory.createEnrollment(1L, student, course);
        lesson = TestDataFactory.createLesson(1L, "Test Lesson", course, LessonType.VIDEO);
        quiz = TestDataFactory.createQuiz(1L, "Test Quiz", course, lesson);
    }

    @Test
    void studentDashboard_ShouldReturnDashboardView() throws Exception {
        List<Enrollment> enrollments = Arrays.asList(enrollment);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(enrollmentService.getEnrollmentsByStudent(student)).thenReturn(enrollments);

        mockMvc.perform(get("/student"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/dashboard"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("enrollmentProgress"))
                .andExpect(model().attributeExists("activeEnrollments"))
                .andExpect(model().attributeExists("stats"));
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    void studentDashboard_ShouldRedirect_WhenUserNotStudent() throws Exception {
        User instructor = TestDataFactory.createInstructor(1L);
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));

        mockMvc.perform(get("/student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void myCourses_ShouldShowEnrolledCourses() throws Exception {
        List<Enrollment> enrollments = Arrays.asList(enrollment);
        List<Lesson> lessons = Arrays.asList(lesson);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(enrollmentService.getEnrollmentsByStudent(student)).thenReturn(enrollments);
        when(lessonService.getLessonsByCourse(course)).thenReturn(lessons);

        mockMvc.perform(get("/student/my-courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/my-courses"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("enrollments"));
    }

    @Test
    void learningProgress_ShouldShowProgressDetails() throws Exception {
        List<Enrollment> enrollments = Arrays.asList(enrollment);
        List<Lesson> lessons = Arrays.asList(lesson);
        LessonProgress progress = TestDataFactory.createLessonProgress(1L, enrollment, lesson);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(enrollmentService.getEnrollmentsByStudent(student)).thenReturn(enrollments);
        when(lessonService.getLessonsByCourse(course)).thenReturn(lessons);
        when(lessonProgressService.getLessonProgress(enrollment, lesson)).thenReturn(Optional.of(progress));

        mockMvc.perform(get("/student/progress"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/progress"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("enrollments"))
                .andExpect(model().attributeExists("completedCount"))
                .andExpect(model().attributeExists("inProgressCount"))
                .andExpect(model().attributeExists("avgProgress"))
                .andExpect(model().attributeExists("lessonProgress"));
    }

    @Test
    void viewQuiz_ShouldShowQuiz_WhenStudentHasAccess() throws Exception {
        List<QuizAttempt> attempts = Arrays.asList();

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(quizService.getQuizById(1L)).thenReturn(Optional.of(quiz));
        when(quizService.canStudentAccessQuiz(1L, student)).thenReturn(true);
        when(enrollmentService.getEnrollment(student, course)).thenReturn(Optional.of(enrollment));
        when(quizService.getQuizAttemptsByEnrollment(enrollment)).thenReturn(attempts);

        mockMvc.perform(get("/student/quiz/{quizId}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("student/quiz"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("quiz"))
                .andExpect(model().attributeExists("previousAttempts"));
    }

    @Test
    void viewQuiz_ShouldRedirect_WhenStudentNoAccess() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(quizService.getQuizById(1L)).thenReturn(Optional.of(quiz));
        when(quizService.canStudentAccessQuiz(1L, student)).thenReturn(false);

        mockMvc.perform(get("/student/quiz/{quizId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student?error=unauthorized"));
    }

    @Test
    void startQuiz_ShouldCreateAttemptAndRedirect() throws Exception {
        QuizAttempt attempt = TestDataFactory.createQuizAttempt(1L, quiz, student, enrollment);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(quizService.getQuizById(1L)).thenReturn(Optional.of(quiz));
        when(quizService.startQuizAttempt(quiz, student)).thenReturn(attempt);

        mockMvc.perform(post("/student/quiz/start/{quizId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/quiz/attempt/1"));
    }

    @Test
    void takeQuiz_ShouldShowQuizAttempt_WhenOwnedByStudent() throws Exception {
        QuizAttempt attempt = TestDataFactory.createQuizAttempt(1L, quiz, student, enrollment);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(quizService.getQuizAttemptById(1L)).thenReturn(Optional.of(attempt));

        mockMvc.perform(get("/student/quiz/attempt/{attemptId}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("student/take-quiz"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("attempt"))
                .andExpect(model().attributeExists("quiz"));
    }

    @Test
    void takeQuiz_ShouldRedirect_WhenAttemptNotOwnedByStudent() throws Exception {
        User otherStudent = TestDataFactory.createStudent(2L);
        QuizAttempt attempt = TestDataFactory.createQuizAttempt(1L, quiz, otherStudent, enrollment);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(quizService.getQuizAttemptById(1L)).thenReturn(Optional.of(attempt));

        mockMvc.perform(get("/student/quiz/attempt/{attemptId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/quiz/1?error=unauthorized"));
    }
}