package com.lms.service;

import com.lms.entity.Course;
import com.lms.entity.User;
import com.lms.repository.CourseRepository;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private DemoService demoService;

    @Captor
    private ArgumentCaptor<Course> courseCaptor;

    private User instructor;

    @BeforeEach
    void setUp() {
        instructor = TestDataFactory.createInstructor(1L);
    }

    @Test
    void createDemoCourse_ShouldCreateCourseWithPlaceholderThumbnail() {
        String title = "Demo Course";
        String description = "Demo Description";
        Double price = 99.99;

        when(courseRepository.save(any(Course.class))).thenAnswer(i -> {
            Course saved = i.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Course result = demoService.createDemoCourse(title, description, price, instructor);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getPrice()).isEqualTo(price);
        assertThat(result.getInstructor()).isEqualTo(instructor);
        assertThat(result.getThumbnailUrl()).isEqualTo("https://ucarecdn.com/be8d3d93-3acf-4b79-9756-9da015c1c2b6/book_placeholder.png");
        assertThat(result.getStatus()).isNull(); // Not set in demo service

        verify(courseRepository).save(courseCaptor.capture());
        Course captured = courseCaptor.getValue();
        assertThat(captured.getThumbnailUrl()).contains("placeholder");
    }

    @Test
    void createDemoCourse_ShouldReturnSavedCourse() {
        String title = "Demo Course";
        String description = "Demo Description";
        Double price = 99.99;

        Course expectedCourse = new Course(title, description, price, instructor);
        expectedCourse.setId(1L);
        expectedCourse.setThumbnailUrl("https://ucarecdn.com/be8d3d93-3acf-4b79-9756-9da015c1c2b6/book_placeholder.png");

        when(courseRepository.save(any(Course.class))).thenReturn(expectedCourse);

        Course result = demoService.createDemoCourse(title, description, price, instructor);

        assertThat(result).isEqualTo(expectedCourse);
    }
}