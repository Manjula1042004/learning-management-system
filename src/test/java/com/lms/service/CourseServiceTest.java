package com.lms.service;

import com.lms.entity.*;
import com.lms.repository.*;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private CourseService courseService;

    @Captor
    private ArgumentCaptor<Course> courseCaptor;

    private User instructor;
    private User admin;
    private Course course;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        instructor = TestDataFactory.createInstructor(1L);
        admin = TestDataFactory.createAdmin(2L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        lesson = TestDataFactory.createLesson(1L, "Test Lesson", course, LessonType.VIDEO);
    }

    @Test
    void createCourse_ShouldCreateCourse_WhenValidDataProvided() throws IOException {
        // Arrange
        String title = "New Course";
        String description = "New Description";
        Double price = 99.99;
        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail", "test.jpg", "image/jpeg", "test image".getBytes());

        when(cloudinaryService.isImageFile(anyString())).thenReturn(true);
        when(cloudinaryService.uploadFile(any(MultipartFile.class))).thenReturn("https://cloudinary.com/test.jpg");
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> {
            Course saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // Act
        Course created = courseService.createCourse(title, description, price, instructor, thumbnail);

        // Assert
        assertThat(created).isNotNull();
        assertThat(created.getTitle()).isEqualTo(title);
        assertThat(created.getDescription()).isEqualTo(description);
        assertThat(created.getPrice()).isEqualTo(price);
        assertThat(created.getInstructor()).isEqualTo(instructor);
        assertThat(created.getThumbnailUrl()).isEqualTo("https://cloudinary.com/test.jpg");
        assertThat(created.getStatus()).isEqualTo(CourseStatus.PENDING);

        verify(courseRepository).save(courseCaptor.capture());
        Course captured = courseCaptor.getValue();
        assertThat(captured.getTitle()).isEqualTo(title);
        assertThat(captured.getInstructor()).isEqualTo(instructor);
    }

    @Test
    void createCourse_ShouldUsePlaceholder_WhenNoThumbnailProvided() throws IOException {
        // Arrange
        String title = "New Course";
        String description = "New Description";
        Double price = 99.99;

        when(courseRepository.save(any(Course.class))).thenAnswer(i -> {
            Course saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // Act
        Course created = courseService.createCourse(title, description, price, instructor, null);

        // Assert
        assertThat(created).isNotNull();
        assertThat(created.getThumbnailUrl()).isNotNull();
        assertThat(created.getThumbnailUrl()).startsWith("https://images.unsplash.com/");

        verify(cloudinaryService, never()).uploadFile(any());
    }

    @Test
    void updateCourse_ShouldUpdateCourseDetails() throws IOException {
        // Arrange
        Long courseId = 1L;
        String updatedTitle = "Updated Title";
        String updatedDescription = "Updated Description";
        Double updatedPrice = 149.99;
        MockMultipartFile newThumbnail = new MockMultipartFile(
                "thumbnail", "new.jpg", "image/jpeg", "new image".getBytes());

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(cloudinaryService.isImageFile(anyString())).thenReturn(true);
        when(cloudinaryService.uploadFile(any(MultipartFile.class))).thenReturn("https://cloudinary.com/new.jpg");
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Course updated = courseService.updateCourse(courseId, updatedTitle, updatedDescription,
                updatedPrice, newThumbnail);

        // Assert
        assertThat(updated.getTitle()).isEqualTo(updatedTitle);
        assertThat(updated.getDescription()).isEqualTo(updatedDescription);
        assertThat(updated.getPrice()).isEqualTo(updatedPrice);
        assertThat(updated.getThumbnailUrl()).isEqualTo("https://cloudinary.com/new.jpg");

        verify(courseRepository).save(course);
    }

    @Test
    void updateCourse_ShouldThrowException_WhenCourseNotFound() {
        // Arrange
        Long courseId = 99L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courseService.updateCourse(courseId, "title", "desc", 99.99, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Course not found");
    }

    @Test
    void updateCourseStatus_ShouldChangeStatus() {
        // Arrange
        Long courseId = 1L;
        CourseStatus newStatus = CourseStatus.APPROVED;

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Course updated = courseService.updateCourseStatus(courseId, newStatus);

        // Assert
        assertThat(updated.getStatus()).isEqualTo(newStatus);
    }

    @Test
    void getCoursesByInstructor_ShouldReturnInstructorCourses() {
        // Arrange
        List<Course> instructorCourses = Arrays.asList(course);
        when(courseRepository.findByInstructor(instructor)).thenReturn(instructorCourses);

        // Act
        List<Course> result = courseService.getCoursesByInstructor(instructor);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInstructor()).isEqualTo(instructor);
    }

    @Test
    void getPendingCourses_ShouldReturnPendingCourses() {
        // Arrange
        Course pendingCourse = TestDataFactory.createPendingCourse(2L, "Pending Course", instructor);
        List<Course> pendingCourses = Arrays.asList(pendingCourse);
        when(courseRepository.findByStatus(CourseStatus.PENDING)).thenReturn(pendingCourses);

        // Act
        List<Course> result = courseService.getPendingCourses();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(CourseStatus.PENDING);
    }

    @Test
    void getApprovedCourses_ShouldReturnApprovedCourses() {
        // Arrange
        List<Course> approvedCourses = Arrays.asList(course);
        when(courseRepository.findByStatus(CourseStatus.APPROVED)).thenReturn(approvedCourses);

        // Act
        List<Course> result = courseService.getApprovedCourses();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(CourseStatus.APPROVED);
    }

    @Test
    void searchCourses_ShouldReturnMatchingCourses() {
        // Arrange
        String query = "java";
        List<Course> searchResults = Arrays.asList(course);
        when(courseRepository.searchApprovedCourses(query)).thenReturn(searchResults);

        // Act
        List<Course> result = courseService.searchCourses(query);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void getCourseById_ShouldReturnCourse_WhenExists() {
        // Arrange
        Long courseId = 1L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // Act
        Optional<Course> result = courseService.getCourseById(courseId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(courseId);
    }

    @Test
    void getAllCourses_ShouldReturnAllCourses() {
        // Arrange
        List<Course> allCourses = Arrays.asList(course);
        when(courseRepository.findAll()).thenReturn(allCourses);

        // Act
        List<Course> result = courseService.getAllCourses();

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void deleteCourse_ShouldDeleteCourse_WhenUserHasPermission() {
        // Arrange
        Long courseId = 1L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(lessonRepository.findByCourseOrderByOrderIndexAsc(course)).thenReturn(Arrays.asList(lesson));
        when(quizRepository.findByLessonId(lesson.getId())).thenReturn(Optional.empty());
        when(mediaRepository.findByLesson(lesson)).thenReturn(Arrays.asList());
        when(mediaRepository.findByCourse(course)).thenReturn(Arrays.asList());

        // Act
        courseService.deleteCourse(courseId, instructor);

        // Assert
        verify(courseRepository).delete(course);
        verify(lessonRepository).delete(lesson);
    }

    @Test
    void deleteCourse_ShouldThrowException_WhenUserDoesNotHavePermission() {
        // Arrange
        Long courseId = 1L;
        User unauthorizedUser = TestDataFactory.createStudent(3L);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // Act & Assert
        assertThatThrownBy(() -> courseService.deleteCourse(courseId, unauthorizedUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("You don't have permission to delete this course");
    }

    @Test
    void deleteCourse_ShouldThrowException_WhenCourseNotFound() {
        // Arrange
        Long courseId = 99L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courseService.deleteCourse(courseId, instructor))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Course not found with ID: " + courseId);
    }
}