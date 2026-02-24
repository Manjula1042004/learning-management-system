package com.lms.controller;

import com.lms.entity.*;
import com.lms.service.*;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Fixed MediaControllerTest.
 *
 * Root cause: The controller methods accept a UserDetails parameter annotated
 * with @AuthenticationPrincipal. When using standalone MockMvc (no Spring
 * Security filter chain), Spring MVC tries to bind UserDetails from the
 * request as a model attribute and fails because UserDetails is an interface
 * with no default constructor.
 *
 * Fix: Use MockMvcBuilders.standaloneSetup and set up a real
 * SecurityContext so that @AuthenticationPrincipal can be resolved, OR
 * (simpler) rely on the username obtained from Principal/Authentication and
 * mock userService.getUserByUsername to return the correct user.
 *
 * We set a UsernamePasswordAuthenticationToken in the SecurityContextHolder
 * before each request so Spring resolves @AuthenticationPrincipal correctly
 * in standalone mode.
 */
@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MediaService mediaService;

    @Mock
    private UserService userService;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private MediaController mediaController;

    private User instructor;
    private User student;
    private Course course;
    private Lesson lesson;
    private Media media;
    private MediaService.MediaStatistics statistics;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(mediaController)
                .build();

        instructor = TestDataFactory.createInstructor(1L);
        student = TestDataFactory.createStudent(2L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        lesson = TestDataFactory.createLesson(1L, "Test Lesson", course, LessonType.VIDEO);
        media = TestDataFactory.createMedia(1L, "test.mp4", instructor, course);

        statistics = new MediaService.MediaStatistics(5L, 1024000L, 2, 2, 1, 0);
    }

    /** Helper: set the SecurityContext so @AuthenticationPrincipal resolves. */
    private void setAuthentication(String username, String role) {
        org.springframework.security.core.userdetails.User principal =
                new org.springframework.security.core.userdetails.User(
                        username, "",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ------------------------------------------------------------------ //

    @Test
    void viewCourseMedia_ShouldReturnViewName() throws Exception {
        setAuthentication(instructor.getUsername(), "INSTRUCTOR");
        List<Media> mediaList = Arrays.asList(media);

        when(userService.getUserByUsername(instructor.getUsername())).thenReturn(Optional.of(instructor));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        when(mediaService.getMediaByCourse(1L)).thenReturn(mediaList);
        when(mediaService.getCourseMediaStatistics(1L)).thenReturn(statistics);

        mockMvc.perform(get("/media/course/{courseId}", 1L)
                        .principal(new UsernamePasswordAuthenticationToken(
                                instructor.getUsername(), null)))
                .andExpect(status().isOk())
                .andExpect(view().name("media/library"));
    }

    @Test
    void viewCourseMedia_WithFileType_ShouldReturnViewName() throws Exception {
        setAuthentication(instructor.getUsername(), "INSTRUCTOR");
        List<Media> mediaList = Arrays.asList(media);

        when(userService.getUserByUsername(instructor.getUsername())).thenReturn(Optional.of(instructor));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        when(mediaService.getMediaByCourseAndType(1L, "video")).thenReturn(mediaList);
        when(mediaService.getCourseMediaStatistics(1L)).thenReturn(statistics);

        mockMvc.perform(get("/media/course/{courseId}", 1L)
                        .param("fileType", "video")
                        .principal(new UsernamePasswordAuthenticationToken(
                                instructor.getUsername(), null)))
                .andExpect(status().isOk())
                .andExpect(view().name("media/library"));
    }

    @Test
    void viewCourseMedia_ShouldRedirect_WhenUnauthorized() throws Exception {
        setAuthentication(student.getUsername(), "STUDENT");

        when(userService.getUserByUsername(student.getUsername())).thenReturn(Optional.of(student));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));

        mockMvc.perform(get("/media/course/{courseId}", 1L)
                        .principal(new UsernamePasswordAuthenticationToken(
                                student.getUsername(), null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1?error=unauthorized"));
    }

    @Test
    void showUploadForm_ShouldReturnViewName() throws Exception {
        setAuthentication(instructor.getUsername(), "INSTRUCTOR");

        when(userService.getUserByUsername(instructor.getUsername())).thenReturn(Optional.of(instructor));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));

        mockMvc.perform(get("/media/upload/course/{courseId}", 1L)
                        .principal(new UsernamePasswordAuthenticationToken(
                                instructor.getUsername(), null)))
                .andExpect(status().isOk())
                .andExpect(view().name("media/upload"));
    }

    @Test
    void showUploadForm_WithLessonId_ShouldReturnViewName() throws Exception {
        setAuthentication(instructor.getUsername(), "INSTRUCTOR");

        when(userService.getUserByUsername(instructor.getUsername())).thenReturn(Optional.of(instructor));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));

        mockMvc.perform(get("/media/upload/course/{courseId}", 1L)
                        .param("lessonId", "1")
                        .principal(new UsernamePasswordAuthenticationToken(
                                instructor.getUsername(), null)))
                .andExpect(status().isOk())
                .andExpect(view().name("media/upload"));
    }

    @Test
    void uploadMedia_ForCourse_ShouldRedirect() throws Exception {
        setAuthentication(instructor.getUsername(), "INSTRUCTOR");
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.mp4", "video/mp4", "test video".getBytes());

        when(userService.getUserByUsername(instructor.getUsername())).thenReturn(Optional.of(instructor));
        when(mediaService.uploadMediaForCourse(any(), eq(1L), eq(instructor), any()))
                .thenReturn(media);

        mockMvc.perform(multipart("/media/upload/course/{courseId}", 1L)
                        .file(file)
                        .param("description", "Test video")
                        .principal(new UsernamePasswordAuthenticationToken(
                                instructor.getUsername(), null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/media/course/1"));
    }

    @Test
    void uploadMedia_ForLesson_ShouldRedirect() throws Exception {
        setAuthentication(instructor.getUsername(), "INSTRUCTOR");
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.mp4", "video/mp4", "test video".getBytes());

        when(userService.getUserByUsername(instructor.getUsername())).thenReturn(Optional.of(instructor));
        when(mediaService.uploadMediaForLesson(any(), eq(1L), eq(instructor), any()))
                .thenReturn(media);

        mockMvc.perform(multipart("/media/upload/course/{courseId}", 1L)
                        .file(file)
                        .param("lessonId", "1")
                        .param("description", "Test video")
                        .principal(new UsernamePasswordAuthenticationToken(
                                instructor.getUsername(), null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1?lesson=1"));
    }

    @Test
    void uploadMedia_ShouldHandleException_AndRedirect() throws Exception {
        setAuthentication(instructor.getUsername(), "INSTRUCTOR");
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.mp4", "video/mp4", "test video".getBytes());

        when(userService.getUserByUsername(instructor.getUsername())).thenReturn(Optional.of(instructor));
        when(mediaService.uploadMediaForCourse(any(), eq(1L), eq(instructor), any()))
                .thenThrow(new RuntimeException("Upload failed"));

        mockMvc.perform(multipart("/media/upload/course/{courseId}", 1L)
                        .file(file)
                        .principal(new UsernamePasswordAuthenticationToken(
                                instructor.getUsername(), null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/media/upload/course/1"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void deleteMedia_ShouldRedirect() throws Exception {
        setAuthentication(instructor.getUsername(), "INSTRUCTOR");
        List<Media> mediaList = Arrays.asList(media);

        when(userService.getUserByUsername(instructor.getUsername())).thenReturn(Optional.of(instructor));
        when(mediaService.getMediaByCourse(1L)).thenReturn(mediaList);
        doNothing().when(mediaService).deleteMedia(1L);

        mockMvc.perform(post("/media/delete/{mediaId}", 1L)
                        .param("courseId", "1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .principal(new UsernamePasswordAuthenticationToken(
                                instructor.getUsername(), null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/media/course/1"))
                .andExpect(flash().attributeExists("success"));

        verify(mediaService).deleteMedia(1L);
    }

    @Test
    void deleteMedia_ShouldHandleError_WhenUnauthorized() throws Exception {
        setAuthentication(instructor.getUsername(), "INSTRUCTOR");
        Media otherMedia = TestDataFactory.createMedia(1L, "test.mp4", student, course);
        List<Media> mediaList = Arrays.asList(otherMedia);

        when(userService.getUserByUsername(instructor.getUsername())).thenReturn(Optional.of(instructor));
        when(mediaService.getMediaByCourse(1L)).thenReturn(mediaList);

        mockMvc.perform(post("/media/delete/{mediaId}", 1L)
                        .param("courseId", "1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .principal(new UsernamePasswordAuthenticationToken(
                                instructor.getUsername(), null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/media/course/1"))
                .andExpect(flash().attribute("error", "You don't have permission to delete this file"));

        verify(mediaService, never()).deleteMedia(anyLong());
    }

    @Test
    void previewMedia_ShouldReturnViewName() throws Exception {
        setAuthentication(instructor.getUsername(), "INSTRUCTOR");

        when(userService.getUserByUsername(instructor.getUsername())).thenReturn(Optional.of(instructor));

        mockMvc.perform(get("/media/preview/{mediaId}", 1L)
                        .principal(new UsernamePasswordAuthenticationToken(
                                instructor.getUsername(), null)))
                .andExpect(status().isOk())
                .andExpect(view().name("media/preview"));
    }
}