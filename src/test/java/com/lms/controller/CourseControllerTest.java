package com.lms.controller;

import com.lms.entity.*;
import com.lms.service.*;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @MockBean
    private UserService userService;

    @MockBean
    private LessonService lessonService;

    @MockBean
    private EnrollmentService enrollmentService;

    private User instructor;
    private User student;
    private User admin;
    private Course course;
    private List<Lesson> lessons;

    @BeforeEach
    void setUp() {
        instructor = TestDataFactory.createInstructor(1L);
        student = TestDataFactory.createStudent(2L);
        admin = TestDataFactory.createAdmin(3L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);

        Lesson lesson1 = TestDataFactory.createLesson(1L, "Lesson 1", course, LessonType.VIDEO);
        Lesson lesson2 = TestDataFactory.createLesson(2L, "Lesson 2", course, LessonType.PDF);
        lessons = Arrays.asList(lesson1, lesson2);
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    void listCourses_ShouldShowAllCourses_ForInstructor() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getApprovedCourses()).thenReturn(Arrays.asList(course));

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/list"))
                .andExpect(model().attributeExists("courses"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "student1", roles = "STUDENT")
    void listCourses_ShouldShowApprovedCourses_ForStudent() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(courseService.getApprovedCourses()).thenReturn(Arrays.asList(course));

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/list"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void listCourses_ShouldShowAllCourses_ForAdmin() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(admin));
        when(courseService.getAllCourses()).thenReturn(Arrays.asList(course));

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/list"));
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    void listCourses_ShouldFilterCourses_WhenSearchProvided() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getApprovedCourses()).thenReturn(Arrays.asList(course));

        mockMvc.perform(get("/courses").param("search", "Test"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/list"))
                .andExpect(model().attributeExists("search"));
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    void showCreateForm_ShouldReturnCreateView() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));

        mockMvc.perform(get("/courses/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/create"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("course"));
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    void createCourse_ShouldCreateCourseAndRedirect() throws Exception {
        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail", "test.jpg", "image/jpeg", "test image".getBytes());

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.createCourse(anyString(), anyString(), anyDouble(),
                any(User.class), any())).thenReturn(course);

        mockMvc.perform(multipart("/courses/create")
                        .file(thumbnail)
                        .param("title", "New Course")
                        .param("description", "Description")
                        .param("price", "99.99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1"));
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    void viewCourse_ShouldShowCourseDetails() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        when(lessonService.getLessonsByCourse(course)).thenReturn(lessons);
        when(enrollmentService.isStudentEnrolled(instructor, course)).thenReturn(false);

        mockMvc.perform(get("/courses/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/view"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeExists("lessons"))
                .andExpect(model().attribute("isOwner", true));
    }

    @Test
    @WithMockUser(username = "student1", roles = "STUDENT")
    void viewCourse_ShouldShowCourseForStudent_WhenNotEnrolled() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        when(lessonService.getLessonsByCourse(course)).thenReturn(lessons);
        when(enrollmentService.isStudentEnrolled(student, course)).thenReturn(false);

        mockMvc.perform(get("/courses/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/view"))
                .andExpect(model().attribute("enrolled", false))
                .andExpect(model().attribute("isOwner", false));
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    void viewCourse_WithLessonParam_ShouldShowSelectedLesson() throws Exception {
        Lesson selectedLesson = lessons.get(0);
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        when(lessonService.getLessonsByCourse(course)).thenReturn(lessons);
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(selectedLesson));
        when(enrollmentService.isStudentEnrolled(instructor, course)).thenReturn(false);

        mockMvc.perform(get("/courses/{id}", 1L).param("lesson", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/view"))
                .andExpect(model().attributeExists("selectedLesson"));
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    void showEditForm_ShouldReturnEditView_ForOwner() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));

        mockMvc.perform(get("/courses/{id}/edit", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/edit"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("course"));
    }

    @Test
    @WithMockUser(username = "student1", roles = "STUDENT")
    void showEditForm_ShouldRedirect_WhenNotOwner() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));

        mockMvc.perform(get("/courses/{id}/edit", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    void updateCourse_ShouldUpdateCourseAndRedirect() throws Exception {
        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail", "new.jpg", "image/jpeg", "new image".getBytes());

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        when(courseService.updateCourse(anyLong(), anyString(), anyString(),
                anyDouble(), any())).thenReturn(course);

        mockMvc.perform(multipart("/courses/{id}/edit", 1L)
                        .file(thumbnail)
                        .param("title", "Updated Title")
                        .param("description", "Updated Description")
                        .param("price", "149.99")
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        }))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1"));
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    void deleteCourse_ShouldDeleteCourseAndRedirect_ForInstructor() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        doNothing().when(courseService).deleteCourse(anyLong(), any(User.class));

        mockMvc.perform(post("/courses/{id}/delete", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/instructor/my-courses"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteCourse_ShouldDeleteCourseAndRedirect_ForAdmin() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(admin));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        doNothing().when(courseService).deleteCourse(anyLong(), any(User.class));

        mockMvc.perform(post("/courses/{id}/delete", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?tab=courses"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    void deleteCourse_ShouldHandleError_WhenExceptionThrown() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(instructor));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        doThrow(new RuntimeException("Delete failed")).when(courseService)
                .deleteCourse(anyLong(), any(User.class));

        mockMvc.perform(post("/courses/{id}/delete", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    void approveCourse_ShouldApproveCourseAndRedirect() throws Exception {
        // Use doAnswer instead of doNothing
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            CourseStatus status = invocation.getArgument(1);
            return null;
        }).when(courseService).updateCourseStatus(anyLong(), any(CourseStatus.class));

        mockMvc.perform(post("/courses/{id}/approve", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?tab=pending"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    @WithMockUser(username = "instructor1", roles = "INSTRUCTOR")
    void rejectCourse_ShouldRejectCourseAndRedirect() throws Exception {
        // Use doAnswer instead of doNothing
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            CourseStatus status = invocation.getArgument(1);
            return null;
        }).when(courseService).updateCourseStatus(anyLong(), any(CourseStatus.class));

        mockMvc.perform(post("/courses/{id}/reject", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?tab=pending"))
                .andExpect(flash().attributeExists("success"));
    }
}