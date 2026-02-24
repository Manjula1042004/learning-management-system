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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LessonControllerTest {

    @Mock
    private LessonService lessonService;

    @Mock
    private CourseService courseService;

    @Mock
    private UserService userService;

    @Mock
    private MediaService mediaService;

    @Mock
    private QuizService quizService;

    @InjectMocks
    private LessonController lessonController;

    private MockMvc mockMvc;
    private User instructor;
    private Course course;
    private Lesson videoLesson;
    private Lesson pdfLesson;
    private Lesson quizLesson;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(lessonController).build();

        instructor = TestDataFactory.createInstructor(1L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        videoLesson = TestDataFactory.createLesson(1L, "Video Lesson", course, LessonType.VIDEO);
        pdfLesson = TestDataFactory.createLesson(2L, "PDF Lesson", course, LessonType.PDF);
        quizLesson = TestDataFactory.createLesson(3L, "Quiz Lesson", course, LessonType.QUIZ);
    }

    @Test
    void testController_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/lessons/test-controller"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("WORKING")));
    }

    @Test
    @WithMockCustomUser(username = "instructor1", role = "INSTRUCTOR")
    void showCreateForm_ShouldReturnCreateView() throws Exception {
        // Arrange
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));

        // Act & Assert
        mockMvc.perform(get("/lessons/create/{courseId}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("lessons/create"))
                .andExpect(model().attributeExists("course"))
                .andExpect(model().attributeExists("lessonTypes"));
    }

    @Test
    @WithMockCustomUser(username = "instructor1", role = "INSTRUCTOR")
    void showEditLesson_ForVideoLesson_ShouldReturnEditView() throws Exception {
        // Arrange
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(videoLesson));

        // Act & Assert
        mockMvc.perform(get("/lessons/edit/{lessonId}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("lessons/edit"))
                .andExpect(model().attributeExists("lesson"))
                .andExpect(model().attributeExists("lessonTypes"));
    }

    @Test
    @WithMockCustomUser(username = "instructor1", role = "INSTRUCTOR")
    void showEditLesson_ForQuizLesson_ShouldRedirectToQuizEdit() throws Exception {
        // Arrange
        Quiz quiz = TestDataFactory.createQuiz(1L, "Test Quiz", course, quizLesson);
        when(lessonService.getLessonById(3L)).thenReturn(Optional.of(quizLesson));
        when(quizService.getQuizByLesson(3L)).thenReturn(Optional.of(quiz));

        // Act & Assert
        mockMvc.perform(get("/lessons/edit/{lessonId}", 3L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/instructor/quiz/edit/1"));
    }

    @Test
    @WithMockCustomUser(username = "instructor1", role = "INSTRUCTOR")
    void showEditLesson_ForQuizLessonWithoutQuiz_ShouldRedirectToQuizCreate() throws Exception {
        // Arrange
        when(lessonService.getLessonById(3L)).thenReturn(Optional.of(quizLesson));
        when(quizService.getQuizByLesson(3L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/lessons/edit/{lessonId}", 3L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/instructor/quiz/create/1?lessonId=3"));
    }

    @Test
    @WithMockCustomUser(username = "instructor1", role = "INSTRUCTOR")
    void createLesson_ForVideoWithFile_ShouldCreateLesson() throws Exception {
        // Arrange
        MockMultipartFile videoFile = new MockMultipartFile(
                "videoFile", "test.mp4", "video/mp4", "test video".getBytes());

        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        when(lessonService.createLesson(anyString(), anyString(), anyInt(),
                any(LessonType.class), any(Course.class), any(), any(),
                any(), any(), any(), any())).thenReturn(videoLesson);

        // Act & Assert
        mockMvc.perform(multipart("/lessons/create/{courseId}", 1L)
                        .file(videoFile)
                        .param("title", "New Video Lesson")
                        .param("description", "Description")
                        .param("duration", "30")
                        .param("type", "VIDEO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1?lesson=1"));
    }

    @Test
    @WithMockCustomUser(username = "instructor1", role = "INSTRUCTOR")
    void createLesson_ForQuiz_ShouldCreateLessonAndRedirectToQuizCreate() throws Exception {
        // Arrange
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        when(lessonService.createLesson(anyString(), anyString(), anyInt(),
                eq(LessonType.QUIZ), any(Course.class), any(), any(),
                any(), any(), any(), any())).thenReturn(quizLesson);

        // Act & Assert
        mockMvc.perform(multipart("/lessons/create/{courseId}", 1L)
                        .param("title", "New Quiz Lesson")
                        .param("description", "Description")
                        .param("duration", "20")
                        .param("type", "QUIZ"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/instructor/quiz/create/1?lessonId=3"));
    }

    @Test
    @WithMockCustomUser(username = "instructor1", role = "INSTRUCTOR")
    void createLesson_ForVideoWithNoContent_ShouldReturnError() throws Exception {
        // Arrange
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(course));
        when(lessonService.createLesson(anyString(), anyString(), anyInt(),
                eq(LessonType.VIDEO), any(Course.class), any(), any(),
                any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Video lesson requires either a video file or video URL"));

        // Act & Assert
        mockMvc.perform(multipart("/lessons/create/{courseId}", 1L)
                        .param("title", "Invalid Video")
                        .param("description", "Description")
                        .param("duration", "30")
                        .param("type", "VIDEO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/lessons/create/1"));
    }

    @Test
    @WithMockCustomUser(username = "instructor1", role = "INSTRUCTOR")
    void updateLesson_ShouldUpdateLessonAndRedirect() throws Exception {
        // Arrange
        MockMultipartFile videoFile = new MockMultipartFile(
                "videoFile", "updated.mp4", "video/mp4", "updated video".getBytes());

        when(lessonService.updateLesson(anyLong(), anyString(), anyString(),
                anyInt(), any(LessonType.class), any(), any(), any(), any()))
                .thenReturn(videoLesson);

        // Act & Assert
        mockMvc.perform(multipart("/lessons/edit/{lessonId}", 1L)
                        .file(videoFile)
                        .param("title", "Updated Lesson")
                        .param("description", "Updated Description")
                        .param("duration", "45")
                        .param("type", "VIDEO")
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        }))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1"));
    }

    @Test
    @WithMockCustomUser(username = "instructor1", role = "INSTRUCTOR")
    void deleteLesson_ShouldDeleteLessonAndRedirect() throws Exception {
        // Arrange
        when(lessonService.getLessonById(1L)).thenReturn(Optional.of(videoLesson));
        doNothing().when(lessonService).deleteLesson(1L);

        // Act & Assert
        mockMvc.perform(post("/lessons/delete/{lessonId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1"));
    }

    @Test
    @WithMockCustomUser(username = "instructor1", role = "INSTRUCTOR")
    void deleteLesson_ShouldHandleError_WhenLessonNotFound() throws Exception {
        // Arrange
        when(lessonService.getLessonById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/lessons/delete/{lessonId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    void testDeleteEndpoint_ShouldReturnTestMessage() throws Exception {
        mockMvc.perform(get("/lessons/test-delete/{lessonId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Delete endpoint is accessible")));
    }
}