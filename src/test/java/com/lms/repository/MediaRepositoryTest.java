package com.lms.repository;

import com.lms.entity.*;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MediaRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    private User instructor;
    private User student;
    private Course course;
    private Lesson lesson;
    private Media media1;
    private Media media2;

    @BeforeEach
    void setUp() {
        // Clear repositories in correct order
        mediaRepository.deleteAll();
        lessonRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // Create users first
        instructor = userRepository.save(TestDataFactory.createInstructor(null));
        student = userRepository.save(TestDataFactory.createStudent(null));

        // Create course
        course = TestDataFactory.createCourse(null, "Test Course", instructor);
        course = courseRepository.save(course);

        // Create lesson
        lesson = TestDataFactory.createLesson(null, "Test Lesson", course, LessonType.VIDEO);
        lesson = lessonRepository.save(lesson);

        // Create media
        media1 = TestDataFactory.createMedia(null, "video1.mp4", instructor, course);
        media1.setLesson(lesson);
        media1 = mediaRepository.save(media1);

        media2 = TestDataFactory.createMedia(null, "document.pdf", instructor, course);
        media2.setFileType("pdf");
        media2 = mediaRepository.save(media2);
    }

    @Test
    void testFindByCourse() {
        List<Media> mediaList = mediaRepository.findByCourse(course);
        assertThat(mediaList).hasSize(2);
    }

    @Test
    void testFindByLesson() {
        List<Media> mediaList = mediaRepository.findByLesson(lesson);
        assertThat(mediaList).hasSize(1);
        assertThat(mediaList.get(0).getOriginalFileName()).isEqualTo("video1.mp4");
    }

    @Test
    void testFindByUploadedBy() {
        List<Media> mediaList = mediaRepository.findByUploadedBy(instructor);
        assertThat(mediaList).hasSize(2);
    }

    @Test
    void testFindByCourseAndFileType() {
        List<Media> videos = mediaRepository.findByCourseAndFileType(course, "video");
        List<Media> pdfs = mediaRepository.findByCourseAndFileType(course, "pdf");

        assertThat(videos).hasSize(1);
        assertThat(pdfs).hasSize(1);
    }

    @Test
    void testFindByStoredFileName() {
        Optional<Media> found = mediaRepository.findByStoredFileName(media1.getStoredFileName());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(media1.getId());
    }

    @Test
    void testCountByCourse() {
        Long count = mediaRepository.countByCourse(course);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testDelete() {
        mediaRepository.delete(media1);
        List<Media> mediaList = mediaRepository.findByCourse(course);
        assertThat(mediaList).hasSize(1);
    }
}