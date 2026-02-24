package com.lms.controller;

import com.lms.entity.*;
import com.lms.service.*;
import com.lms.testutil.TestDataFactory;
import com.lms.testutil.WithMockCustomUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WithMockCustomUser(username = "admin", role = "ADMIN")
class AdminControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private CourseService courseService;

    @Mock
    private LessonService lessonService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;
    private User admin;
    private User instructor;
    private User student;
    private Course course;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        admin = TestDataFactory.createAdmin(1L);
        instructor = TestDataFactory.createInstructor(2L);
        student = TestDataFactory.createStudent(3L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        lesson = TestDataFactory.createLesson(1L, "Test Lesson", course, LessonType.VIDEO);
    }

    @Test
    void approveCourse_ShouldApproveCourseAndRedirect() throws Exception {
        // Fix: Use doAnswer instead of doNothing
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            CourseStatus status = invocation.getArgument(1);
            return null;
        }).when(courseService).updateCourseStatus(anyLong(), any(CourseStatus.class));

        mockMvc.perform(post("/admin/courses/{id}/approve", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?tab=pending"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void rejectCourse_ShouldRejectCourseAndRedirect() throws Exception {
        // Fix: Use doAnswer instead of doNothing
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            CourseStatus status = invocation.getArgument(1);
            return null;
        }).when(courseService).updateCourseStatus(anyLong(), any(CourseStatus.class));

        mockMvc.perform(post("/admin/courses/{id}/reject", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?tab=pending"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void updateUser_ShouldUpdateUserAndRedirect() throws Exception {
        // Fix: Use doAnswer instead of doNothing for non-void methods
        when(userService.updateUser(anyLong(), anyString(), anyString(), any(Role.class)))
                .thenReturn(student); // Return a value

        mockMvc.perform(post("/admin/users/{id}/edit", 1L)
                        .param("username", "updateduser")
                        .param("email", "updated@test.com")
                        .param("role", "INSTRUCTOR"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?tab=users"))
                .andExpect(flash().attributeExists("success"));
    }

    // ... rest of the test methods remain the same
}