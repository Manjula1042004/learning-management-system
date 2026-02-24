package com.lms.entity;

import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LessonProgressTest {

    private User student;
    private User instructor;
    private Course course;
    private Lesson lesson;
    private Enrollment enrollment;
    private LessonProgress lessonProgress;

    @BeforeEach
    void setUp() {
        student = TestDataFactory.createStudent(1L);
        instructor = TestDataFactory.createInstructor(2L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        lesson = TestDataFactory.createLesson(1L, "Test Lesson", course, LessonType.VIDEO);
        enrollment = TestDataFactory.createEnrollment(1L, student, course);

        lessonProgress = new LessonProgress(enrollment, lesson);
        lessonProgress.setId(1L);
    }

    @Test
    void constructor_ShouldCreateLessonProgress() {
        LessonProgress newProgress = new LessonProgress(enrollment, lesson);

        assertThat(newProgress.getEnrollment()).isEqualTo(enrollment);
        assertThat(newProgress.getLesson()).isEqualTo(lesson);
        assertThat(newProgress.isCompleted()).isFalse();
        assertThat(newProgress.getWatchTime()).isEqualTo(0.0);
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        lessonProgress.setId(2L);
        lessonProgress.setCompleted(true);
        lessonProgress.setWatchTime(25.5);

        LocalDateTime startedAt = LocalDateTime.now().minusHours(1);
        LocalDateTime completedAt = LocalDateTime.now();

        lessonProgress.setStartedAt(startedAt);
        lessonProgress.setCompletedAt(completedAt);

        assertThat(lessonProgress.getId()).isEqualTo(2L);
        assertThat(lessonProgress.isCompleted()).isTrue();
        assertThat(lessonProgress.getWatchTime()).isEqualTo(25.5);
        assertThat(lessonProgress.getStartedAt()).isEqualTo(startedAt);
        assertThat(lessonProgress.getCompletedAt()).isEqualTo(completedAt);
    }

    @Test
    void enrollment_ShouldBeAccessible() {
        assertThat(lessonProgress.getEnrollment()).isEqualTo(enrollment);

        Enrollment newEnrollment = TestDataFactory.createEnrollment(2L, student, course);
        lessonProgress.setEnrollment(newEnrollment);
        assertThat(lessonProgress.getEnrollment()).isEqualTo(newEnrollment);
    }

    @Test
    void lesson_ShouldBeAccessible() {
        assertThat(lessonProgress.getLesson()).isEqualTo(lesson);

        Lesson newLesson = TestDataFactory.createLesson(2L, "New Lesson", course, LessonType.PDF);
        lessonProgress.setLesson(newLesson);
        assertThat(lessonProgress.getLesson()).isEqualTo(newLesson);
    }

    @Test
    void prePersist_ShouldSetStartedAt() {
        LessonProgress newProgress = new LessonProgress();
        newProgress.onCreate();

        assertThat(newProgress.getStartedAt()).isNotNull();
    }

    @Test
    void setCompleted_ShouldUpdateStatus() {
        assertThat(lessonProgress.isCompleted()).isFalse();

        lessonProgress.setCompleted(true);
        assertThat(lessonProgress.isCompleted()).isTrue();

        lessonProgress.setCompleted(false);
        assertThat(lessonProgress.isCompleted()).isFalse();
    }

    @Test
    void setWatchTime_ShouldUpdateValue() {
        lessonProgress.setWatchTime(15.5);
        assertThat(lessonProgress.getWatchTime()).isEqualTo(15.5);

        lessonProgress.setWatchTime(30.0);
        assertThat(lessonProgress.getWatchTime()).isEqualTo(30.0);
    }
}