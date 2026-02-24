package com.lms.entity;

import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LessonTest {

    private User instructor;
    private Course course;
    private Lesson videoLesson;
    private Lesson pdfLesson;
    private Lesson imageLesson;
    private Lesson textLesson;
    private Lesson quizLesson;

    @BeforeEach
    void setUp() {
        instructor = TestDataFactory.createInstructor(1L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);

        videoLesson = new Lesson("Video Lesson", "Video Description", 30, 1, LessonType.VIDEO, course);
        videoLesson.setId(1L);
        videoLesson.setVideoUrl("https://youtube.com/watch?v=123");

        pdfLesson = new Lesson("PDF Lesson", "PDF Description", 15, 2, LessonType.PDF, course);
        pdfLesson.setId(2L);
        pdfLesson.setResourceUrl("https://example.com/doc.pdf");

        imageLesson = new Lesson("Image Lesson", "Image Description", 5, 3, LessonType.IMAGE, course);
        imageLesson.setId(3L);
        imageLesson.setResourceUrl("https://example.com/image.jpg");

        textLesson = new Lesson("Text Lesson", "Text Description", 10, 4, LessonType.TEXT, course);
        textLesson.setId(4L);

        quizLesson = new Lesson("Quiz Lesson", "Quiz Description", 20, 5, LessonType.QUIZ, course);
        quizLesson.setId(5L);
    }

    @Test
    void constructor_ShouldCreateLesson() {
        Lesson newLesson = new Lesson("New Lesson", "New Description", 45, 6, LessonType.VIDEO, course);

        assertThat(newLesson.getTitle()).isEqualTo("New Lesson");
        assertThat(newLesson.getDescription()).isEqualTo("New Description");
        assertThat(newLesson.getDuration()).isEqualTo(45);
        assertThat(newLesson.getOrderIndex()).isEqualTo(6);
        assertThat(newLesson.getType()).isEqualTo(LessonType.VIDEO);
        assertThat(newLesson.getCourse()).isEqualTo(course);
        assertThat(newLesson.getCompleted()).isFalse();
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        videoLesson.setId(10L);
        videoLesson.setTitle("Updated Title");
        videoLesson.setDescription("Updated Description");
        videoLesson.setDuration(60);
        videoLesson.setOrderIndex(10);
        videoLesson.setType(LessonType.PDF);
        videoLesson.setVideoUrl("https://new.com/video.mp4");
        videoLesson.setResourceUrl("https://new.com/doc.pdf");

        LocalDateTime now = LocalDateTime.now();
        videoLesson.setCreatedAt(now);
        videoLesson.setUpdatedAt(now);
        videoLesson.setCompleted(true);

        assertThat(videoLesson.getId()).isEqualTo(10L);
        assertThat(videoLesson.getTitle()).isEqualTo("Updated Title");
        assertThat(videoLesson.getDescription()).isEqualTo("Updated Description");
        assertThat(videoLesson.getDuration()).isEqualTo(60);
        assertThat(videoLesson.getOrderIndex()).isEqualTo(10);
        assertThat(videoLesson.getType()).isEqualTo(LessonType.PDF);
        assertThat(videoLesson.getVideoUrl()).isEqualTo("https://new.com/video.mp4");
        assertThat(videoLesson.getResourceUrl()).isEqualTo("https://new.com/doc.pdf");
        assertThat(videoLesson.getCreatedAt()).isEqualTo(now);
        assertThat(videoLesson.getUpdatedAt()).isEqualTo(now);
        assertThat(videoLesson.getCompleted()).isTrue();
    }

    @Test
    void course_ShouldBeAccessible() {
        assertThat(videoLesson.getCourse()).isEqualTo(course);

        Course newCourse = TestDataFactory.createCourse(2L, "New Course", instructor);
        videoLesson.setCourse(newCourse);
        assertThat(videoLesson.getCourse()).isEqualTo(newCourse);
    }

    @Test
    void quiz_ShouldBeManageable() {
        Quiz quiz = TestDataFactory.createQuiz(1L, "Test Quiz", course, videoLesson);
        videoLesson.setQuiz(quiz);

        assertThat(videoLesson.getQuiz()).isEqualTo(quiz);
    }

    @Test
    void isCompleted_ShouldReturnTrue_WhenCompleted() {
        videoLesson.setCompleted(true);
        assertThat(videoLesson.isCompleted()).isTrue();

        videoLesson.setCompleted(false);
        assertThat(videoLesson.isCompleted()).isFalse();

        videoLesson.setCompleted(null);
        assertThat(videoLesson.isCompleted()).isFalse();
    }

    @Test
    void getCompleted_ShouldNeverReturnNull() {
        videoLesson.setCompleted(null);
        assertThat(videoLesson.getCompleted()).isFalse();

        videoLesson.setCompleted(true);
        assertThat(videoLesson.getCompleted()).isTrue();
    }

    @Test
    void isExternalVideo_ShouldReturnTrue_ForYouTubeUrl() {
        videoLesson.setVideoUrl("https://www.youtube.com/watch?v=123");
        assertThat(videoLesson.isExternalVideo()).isTrue();

        videoLesson.setVideoUrl("https://youtu.be/123");
        assertThat(videoLesson.isExternalVideo()).isTrue();
    }

    @Test
    void isExternalVideo_ShouldReturnTrue_ForVimeoUrl() {
        videoLesson.setVideoUrl("https://vimeo.com/123456");
        assertThat(videoLesson.isExternalVideo()).isTrue();
    }

    @Test
    void isExternalVideo_ShouldReturnTrue_ForDirectVideoUrl() {
        videoLesson.setVideoUrl("https://example.com/video.mp4");
        assertThat(videoLesson.isExternalVideo()).isTrue();

        videoLesson.setVideoUrl("https://example.com/video.mov?param=1");
        assertThat(videoLesson.isExternalVideo()).isTrue();
    }

    @Test
    void isExternalVideo_ShouldReturnFalse_ForInvalidUrl() {
        videoLesson.setVideoUrl(null);
        assertThat(videoLesson.isExternalVideo()).isFalse();

        videoLesson.setVideoUrl("");
        assertThat(videoLesson.isExternalVideo()).isFalse();

        videoLesson.setVideoUrl("not a url");
        assertThat(videoLesson.isExternalVideo()).isFalse();
    }

    @Test
    void isYouTubeVideo_ShouldReturnTrue_ForYouTubeUrls() {
        videoLesson.setVideoUrl("https://www.youtube.com/watch?v=123");
        assertThat(videoLesson.isYouTubeVideo()).isTrue();

        videoLesson.setVideoUrl("https://youtu.be/123");
        assertThat(videoLesson.isYouTubeVideo()).isTrue();

        videoLesson.setVideoUrl("https://youtube.com/embed/123");
        assertThat(videoLesson.isYouTubeVideo()).isTrue();
    }

    @Test
    void isYouTubeVideo_ShouldReturnFalse_ForNonYouTubeUrls() {
        videoLesson.setVideoUrl("https://vimeo.com/123");
        assertThat(videoLesson.isYouTubeVideo()).isFalse();

        videoLesson.setVideoUrl(null);
        assertThat(videoLesson.isYouTubeVideo()).isFalse();
    }

    @Test
    void isVimeoVideo_ShouldReturnTrue_ForVimeoUrl() {
        videoLesson.setVideoUrl("https://vimeo.com/123456");
        assertThat(videoLesson.isVimeoVideo()).isTrue();
    }

    @Test
    void isVimeoVideo_ShouldReturnFalse_ForNonVimeoUrls() {
        videoLesson.setVideoUrl("https://youtube.com/watch?v=123");
        assertThat(videoLesson.isVimeoVideo()).isFalse();
    }

    @Test
    void isDirectVideo_ShouldReturnTrue_ForDirectVideoUrls() {
        videoLesson.setVideoUrl("https://example.com/video.mp4");
        assertThat(videoLesson.isDirectVideo()).isTrue();

        videoLesson.setVideoUrl("https://example.com/video.webm");
        assertThat(videoLesson.isDirectVideo()).isTrue();

        videoLesson.setVideoUrl("https://example.com/video.mov");
        assertThat(videoLesson.isDirectVideo()).isTrue();

        videoLesson.setVideoUrl("https://example.com/video.avi");
        assertThat(videoLesson.isDirectVideo()).isTrue();
    }

    @Test
    void isDirectVideo_ShouldReturnFalse_ForNonDirectUrls() {
        videoLesson.setVideoUrl("https://youtube.com/watch?v=123");
        assertThat(videoLesson.isDirectVideo()).isFalse();
    }

    @Test
    void isImage_ShouldReturnTrue_ForImageType() {
        assertThat(imageLesson.isImage()).isTrue();
        assertThat(videoLesson.isImage()).isFalse();
    }

    @Test
    void getImageUrl_ShouldReturnResourceUrl_ForImageType() {
        imageLesson.setResourceUrl("https://example.com/image.jpg");
        assertThat(imageLesson.getImageUrl()).isEqualTo("https://example.com/image.jpg");
    }

    @Test
    void getVideoEmbedUrl_ShouldConvertYouTubeWatchUrl() {
        videoLesson.setVideoUrl("https://www.youtube.com/watch?v=abc123");
        assertThat(videoLesson.getVideoEmbedUrl()).isEqualTo("https://www.youtube.com/embed/abc123");

        videoLesson.setVideoUrl("https://www.youtube.com/watch?v=abc123&t=30s");
        assertThat(videoLesson.getVideoEmbedUrl()).isEqualTo("https://www.youtube.com/embed/abc123");
    }

    @Test
    void getVideoEmbedUrl_ShouldConvertYouTubeShortUrl() {
        videoLesson.setVideoUrl("https://youtu.be/abc123");
        assertThat(videoLesson.getVideoEmbedUrl()).isEqualTo("https://www.youtube.com/embed/abc123");

        videoLesson.setVideoUrl("https://youtu.be/abc123?t=30");
        assertThat(videoLesson.getVideoEmbedUrl()).isEqualTo("https://www.youtube.com/embed/abc123");
    }

    @Test
    void getVideoEmbedUrl_ShouldKeepEmbedUrl() {
        String embedUrl = "https://www.youtube.com/embed/abc123";
        videoLesson.setVideoUrl(embedUrl);
        assertThat(videoLesson.getVideoEmbedUrl()).isEqualTo(embedUrl);
    }

    @Test
    void getVideoEmbedUrl_ShouldReturnOriginal_ForNonYouTubeUrls() {
        String mp4Url = "https://example.com/video.mp4";
        videoLesson.setVideoUrl(mp4Url);
        assertThat(videoLesson.getVideoEmbedUrl()).isEqualTo(mp4Url);
    }

    @Test
    void prePersist_ShouldSetCreatedAndUpdatedAt() {
        Lesson newLesson = new Lesson();
        newLesson.onCreate();

        assertThat(newLesson.getCreatedAt()).isNotNull();
        assertThat(newLesson.getUpdatedAt()).isNotNull();
    }

    @Test
    void preUpdate_ShouldUpdateUpdatedAt() {
        LocalDateTime oldUpdatedAt = videoLesson.getUpdatedAt();
        videoLesson.onUpdate();

        assertThat(videoLesson.getUpdatedAt()).isNotEqualTo(oldUpdatedAt);
        assertThat(videoLesson.getUpdatedAt()).isAfterOrEqualTo(oldUpdatedAt);
    }
}