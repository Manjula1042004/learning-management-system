package com.lms.service;

import com.lms.entity.*;
import com.lms.repository.LessonProgressRepository;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonProgressServiceTest {

    @Mock
    private LessonProgressRepository lessonProgressRepository;

    @Mock
    private EnrollmentService enrollmentService;

    @InjectMocks
    private LessonProgressService lessonProgressService;

    @Captor
    private ArgumentCaptor<LessonProgress> progressCaptor;

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
        lesson.setDuration(30);
        enrollment = TestDataFactory.createEnrollment(1L, student, course);
        progress = TestDataFactory.createLessonProgress(1L, enrollment, lesson);
    }



    @Test
    void markLessonAsCompleted_ShouldThrowException_WhenProgressNotFound() {
        when(lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonProgressService.markLessonAsCompleted(enrollment, lesson))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Lesson progress not found");
    }

    @Test
    void updateWatchTime_ShouldUpdateWatchTime() {
        Double watchTime = 15.5;

        when(lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson))
                .thenReturn(Optional.of(progress));
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(enrollmentService).updateProgress(enrollment.getId());

        LessonProgress result = lessonProgressService.updateWatchTime(enrollment, lesson, watchTime);

        assertThat(result.getWatchTime()).isEqualTo(watchTime);
        assertThat(result.isCompleted()).isFalse();

        verify(lessonProgressRepository).save(progress);
    }

    @Test
    void updateWatchTime_ShouldMarkComplete_WhenWatchTimeReaches90Percent() {
        Double watchTime = 27.0; // 90% of 30 minutes

        when(lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson))
                .thenReturn(Optional.of(progress));
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(enrollmentService).updateProgress(enrollment.getId());

        LessonProgress result = lessonProgressService.updateWatchTime(enrollment, lesson, watchTime);

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getCompletedAt()).isNotNull();
    }

    @Test
    void updateWatchTime_ShouldNotMarkComplete_WhenWatchTimeBelow90Percent() {
        Double watchTime = 20.0; // 66% of 30 minutes

        when(lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson))
                .thenReturn(Optional.of(progress));
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(enrollmentService).updateProgress(enrollment.getId());

        LessonProgress result = lessonProgressService.updateWatchTime(enrollment, lesson, watchTime);

        assertThat(result.isCompleted()).isFalse();
        assertThat(result.getCompletedAt()).isNull();
    }

    @Test
    void updateWatchTime_ShouldThrowException_WhenProgressNotFound() {
        when(lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonProgressService.updateWatchTime(enrollment, lesson, 15.0))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Lesson progress not found");
    }

    @Test
    void getLessonProgress_ShouldReturnProgress_WhenExists() {
        when(lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson))
                .thenReturn(Optional.of(progress));

        Optional<LessonProgress> result = lessonProgressService.getLessonProgress(enrollment, lesson);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(progress.getId());
    }

    @Test
    void getLessonProgress_ShouldReturnEmpty_WhenNotExists() {
        when(lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson))
                .thenReturn(Optional.empty());

        Optional<LessonProgress> result = lessonProgressService.getLessonProgress(enrollment, lesson);

        assertThat(result).isEmpty();
    }

    @Test
    void getProgressByEnrollment_ShouldReturnAllProgress() {
        List<LessonProgress> progresses = Arrays.asList(progress);
        when(lessonProgressRepository.findByEnrollment(enrollment)).thenReturn(progresses);

        List<LessonProgress> result = lessonProgressService.getProgressByEnrollment(enrollment);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEnrollment()).isEqualTo(enrollment);
    }

    @Test
    void getCourseProgress_ShouldReturnEnrollmentProgress() {
        enrollment.setProgress(75.5);

        Double result = lessonProgressService.getCourseProgress(enrollment);

        assertThat(result).isEqualTo(75.5);
    }
    @Test
    void markLessonAsCompleted_ShouldMarkCompleteAndUpdateProgress() {
        when(lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson))
                .thenReturn(Optional.of(progress));
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(enrollmentService).updateProgress(enrollment.getId());

        LessonProgress result = lessonProgressService.markLessonAsCompleted(enrollment, lesson);

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.getCompletedAt()).isNotNull();
        // Fix: Compare as Double
        assertThat(result.getWatchTime()).isEqualTo(30.0);
    }
}