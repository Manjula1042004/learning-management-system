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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProgressController.class)
@WithMockUser(username = "student1", roles = "STUDENT")
class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private LessonService lessonService;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private LessonProgressService lessonProgressService;

    private User student;
    private User instructor;
    private Course course;
    private Lesson lesson;
    private Enrollment enrollment;
    private LessonProgress progress;

    @BeforeEach
    void setUp() {
        student = TestDataFactory.createStudent(1L);
        instructor = TestDataFactory.createInstructor(2L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        lesson = TestDataFactory.createLesson(1L, "Test Lesson", course, LessonType.VIDEO);
        enrollment = TestDataFactory.createEnrollment(1L, student, course);
        progress = TestDataFactory.createLessonProgress(1L, enrollment, lesson);
    }

    @Test
    void updateWatchTime_ShouldUpdateProgressAndReturnOk() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(lesson));
        when(enrollmentService.getEnrollment(student, course)).thenReturn(Optional.of(enrollment));
        when(lessonProgressService.updateWatchTime(enrollment, lesson, 15.5)).thenReturn(progress);

        mockMvc.perform(post("/progress/update-watch-time/{lessonId}", 1L)
                        .param("watchTime", "15.5"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        verify(lessonProgressService).updateWatchTime(enrollment, lesson, 15.5);
    }

    @Test
    void updateWatchTime_ShouldThrowException_WhenUserNotFound() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/progress/update-watch-time/{lessonId}", 1L)
                        .param("watchTime", "15.5"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void markLessonAsCompleted_ShouldCompleteLessonAndRedirect() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(lesson));
        when(enrollmentService.getEnrollment(student, course)).thenReturn(Optional.of(enrollment));
        when(lessonProgressService.markLessonAsCompleted(enrollment, lesson)).thenReturn(progress);

        mockMvc.perform(post("/progress/complete/{lessonId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1?lesson=1&completed=true"));

        verify(lessonProgressService).markLessonAsCompleted(enrollment, lesson);
    }

    @Test
    void markLessonAsCompleted_ShouldThrowException_WhenEnrollmentNotFound() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(lesson));
        when(enrollmentService.getEnrollment(student, course)).thenReturn(Optional.empty());

        mockMvc.perform(post("/progress/complete/{lessonId}", 1L))
                .andExpect(status().is5xxServerError());
    }
}