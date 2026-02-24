package com.lms.entity;

import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MediaTest {

    private User instructor;
    private Course course;
    private Lesson lesson;
    private Media media;

    @BeforeEach
    void setUp() {
        instructor = TestDataFactory.createInstructor(1L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        lesson = TestDataFactory.createLesson(1L, "Test Lesson", course, LessonType.VIDEO);

        media = new Media(
                "original.mp4",
                "stored_12345.mp4",
                "https://cloudinary.com/video.mp4",
                "video",
                1024L * 1024L,
                "video/mp4",
                instructor,
                course,
                lesson
        );
        media.setId(1L);
    }

    @Test
    void constructor_ShouldCreateMedia() {
        Media newMedia = new Media(
                "new.mp4",
                "stored_new.mp4",
                "https://cloudinary.com/new.mp4",
                "video",
                2048L,
                "video/mp4",
                instructor,
                course,
                null
        );

        assertThat(newMedia.getOriginalFileName()).isEqualTo("new.mp4");
        assertThat(newMedia.getStoredFileName()).isEqualTo("stored_new.mp4");
        assertThat(newMedia.getFileUrl()).isEqualTo("https://cloudinary.com/new.mp4");
        assertThat(newMedia.getFileType()).isEqualTo("video");
        assertThat(newMedia.getFileSize()).isEqualTo(2048L);
        assertThat(newMedia.getMimeType()).isEqualTo("video/mp4");
        assertThat(newMedia.getUploadedBy()).isEqualTo(instructor);
        assertThat(newMedia.getCourse()).isEqualTo(course);
        assertThat(newMedia.getLesson()).isNull();
        assertThat(newMedia.getCloudProvider()).isEqualTo("cloudinary");
        assertThat(newMedia.getIsPublic()).isTrue();
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        media.setId(2L);
        media.setOriginalFileName("updated.mp4");
        media.setStoredFileName("stored_updated.mp4");
        media.setFileUrl("https://cloudinary.com/updated.mp4");
        media.setFileType("pdf");
        media.setFileSize(512L);
        media.setMimeType("application/pdf");
        media.setCloudProvider("aws");
        media.setIsPublic(false);

        LocalDateTime now = LocalDateTime.now();
        media.setUploadedAt(now);
        media.setUpdatedAt(now);

        assertThat(media.getId()).isEqualTo(2L);
        assertThat(media.getOriginalFileName()).isEqualTo("updated.mp4");
        assertThat(media.getStoredFileName()).isEqualTo("stored_updated.mp4");
        assertThat(media.getFileUrl()).isEqualTo("https://cloudinary.com/updated.mp4");
        assertThat(media.getFileType()).isEqualTo("pdf");
        assertThat(media.getFileSize()).isEqualTo(512L);
        assertThat(media.getMimeType()).isEqualTo("application/pdf");
        assertThat(media.getCloudProvider()).isEqualTo("aws");
        assertThat(media.getIsPublic()).isFalse();
        assertThat(media.getUploadedAt()).isEqualTo(now);
        assertThat(media.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void uploadedBy_ShouldBeAccessible() {
        assertThat(media.getUploadedBy()).isEqualTo(instructor);

        User newInstructor = TestDataFactory.createInstructor(2L);
        media.setUploadedBy(newInstructor);
        assertThat(media.getUploadedBy()).isEqualTo(newInstructor);
    }

    @Test
    void course_ShouldBeAccessible() {
        assertThat(media.getCourse()).isEqualTo(course);

        Course newCourse = TestDataFactory.createCourse(2L, "New Course", instructor);
        media.setCourse(newCourse);
        assertThat(media.getCourse()).isEqualTo(newCourse);
    }

    @Test
    void lesson_ShouldBeAccessible() {
        assertThat(media.getLesson()).isEqualTo(lesson);

        Lesson newLesson = TestDataFactory.createLesson(2L, "New Lesson", course, LessonType.PDF);
        media.setLesson(newLesson);
        assertThat(media.getLesson()).isEqualTo(newLesson);
    }

    @Test
    void prePersist_ShouldSetUploadedAndUpdatedAt() {
        Media newMedia = new Media();
        newMedia.onCreate();

        assertThat(newMedia.getUploadedAt()).isNotNull();
        assertThat(newMedia.getUpdatedAt()).isNotNull();
    }

    @Test
    void preUpdate_ShouldUpdateUpdatedAt() {
        LocalDateTime oldUpdatedAt = media.getUpdatedAt();
        media.onUpdate();

        assertThat(media.getUpdatedAt()).isNotEqualTo(oldUpdatedAt);
        assertThat(media.getUpdatedAt()).isAfterOrEqualTo(oldUpdatedAt);
    }
}