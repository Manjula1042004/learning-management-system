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

@WebMvcTest(InstructorController.class)
@WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
class InstructorControllerTest {

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

    private User instructor;
    private Course course;
    private Enrollment enrollment;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        instructor = TestDataFactory.createInstructor(1L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        enrollment = TestDataFactory.createEnrollment(1L, TestDataFactory.createStudent(2L), course);
        lesson = TestDataFactory.createLesson(1L, "Test Lesson", course, LessonType.VIDEO);
    }

    @Test
    void instructorDashboard_ShouldReturnDashboardView() throws Exception {
        List<Course> myCourses = Arrays.asList(course);
        List<Enrollment> enrollments = Arrays.asList(enrollment);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getCoursesByInstructor(instructor)).thenReturn(myCourses);
        when(enrollmentService.getEnrollmentsByCourse(course)).thenReturn(enrollments);

        mockMvc.perform(get("/instructor"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/dashboard"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attributeExists("myCourses"))
                .andExpect(model().attributeExists("allCourses"));
    }

    @Test
    void myCourses_ShouldShowInstructorCourses() throws Exception {
        List<Course> myCourses = Arrays.asList(course);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getCoursesByInstructor(instructor)).thenReturn(myCourses);

        mockMvc.perform(get("/instructor/my-courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/my-courses"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("courses"));
    }

    @Test
    void myCourses_WithSearch_ShouldFilterCourses() throws Exception {
        List<Course> myCourses = Arrays.asList(course);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getCoursesByInstructor(instructor)).thenReturn(myCourses);

        mockMvc.perform(get("/instructor/my-courses").param("search", "Test"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/my-courses"));
    }

    @Test
    void viewEnrolledStudents_ShouldShowAllEnrollments() throws Exception {
        List<Course> myCourses = Arrays.asList(course);
        List<Enrollment> enrollments = Arrays.asList(enrollment);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getCoursesByInstructor(instructor)).thenReturn(myCourses);
        when(enrollmentService.getEnrollmentsByInstructor(instructor)).thenReturn(enrollments);

        mockMvc.perform(get("/instructor/enrolled-students"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/enrolled-students"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("myCourses"))
                .andExpect(model().attributeExists("enrollments"));
    }

    @Test
    void viewEnrolledStudents_WithCourseFilter_ShouldShowCourseEnrollments() throws Exception {
        List<Course> myCourses = Arrays.asList(course);
        List<Enrollment> enrollments = Arrays.asList(enrollment);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getCoursesByInstructor(instructor)).thenReturn(myCourses);
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        when(enrollmentService.getEnrollmentsByCourse(course)).thenReturn(enrollments);

        mockMvc.perform(get("/instructor/enrolled-students").param("courseId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/enrolled-students"));
    }

    @Test
    void deleteCourse_ShouldDeleteCourseAndRedirect() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        doNothing().when(courseService).deleteCourse(anyLong(), any(User.class));

        mockMvc.perform(post("/instructor/courses/{id}/delete", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/instructor/my-courses"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void deleteCourse_ShouldHandleError_WhenNotOwner() throws Exception {
        User otherInstructor = TestDataFactory.createInstructor(2L);
        Course otherCourse = TestDataFactory.createCourse(1L, "Other Course", otherInstructor);

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(otherCourse));

        mockMvc.perform(post("/instructor/courses/{id}/delete", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/instructor/my-courses"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void deleteLesson_ShouldDeleteLessonAndRedirect() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        doNothing().when(lessonService).deleteLesson(anyLong());

        mockMvc.perform(post("/instructor/lessons/{id}/delete", 1L)
                        .param("courseId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1"))
                .andExpect(flash().attributeExists("success"));

        verify(lessonService).deleteLesson(1L);
    }

    @Test
    void deleteLesson_ShouldHandleError() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        doThrow(new RuntimeException("Delete failed")).when(lessonService).deleteLesson(anyLong());

        mockMvc.perform(post("/instructor/lessons/{id}/delete", 1L)
                        .param("courseId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void analytics_ShouldReturnAnalyticsView() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));

        mockMvc.perform(get("/instructor/analytics"))
                .andExpect(status().isOk())
                .andExpect(view().name("instructor/analytics"))
                .andExpect(model().attributeExists("user"));
    }
}