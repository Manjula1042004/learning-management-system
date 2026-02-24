package com.lms.controller;

import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.entity.LessonType;
import com.lms.service.CourseService;
import com.lms.service.LessonService;
import com.lms.repository.CourseRepository;
import com.lms.repository.LessonRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class TestVideoController {

    private final CourseService courseService;
    private final LessonService lessonService;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    public TestVideoController(CourseService courseService,
                               LessonService lessonService,
                               CourseRepository courseRepository,
                               LessonRepository lessonRepository) {
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
    }



    @GetMapping("/test/fix-video-lessons/{courseId}")
    public String fixVideoLessons(@PathVariable Long courseId) {
        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Get all lessons for the course
        var lessons = course.getLessons();

        int fixedCount = 0;
        for (Lesson lesson : lessons) {
            // If lesson is VIDEO type but has no video URL, add one
            if (lesson.getType() == LessonType.VIDEO &&
                    (lesson.getVideoUrl() == null || lesson.getVideoUrl().isEmpty())) {

                // Assign a sample video URL based on lesson order
                String videoUrl;
                switch (lesson.getOrderIndex() % 3) {
                    case 0:
                        videoUrl = "https://www.youtube.com/watch?v=qz0aGYrrlhU"; // HTML tutorial
                        break;
                    case 1:
                        videoUrl = "https://www.youtube.com/watch?v=1Rs2ND1ryYc"; // CSS tutorial
                        break;
                    default:
                        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"; // MP4
                }

                lesson.setVideoUrl(videoUrl);
                fixedCount++;
            }
        }

        // Save the course to persist changes
        courseRepository.save(course);

        return "redirect:/courses/" + courseId + "?fixed=" + fixedCount;
    }

    @GetMapping("/test/fix-all-videos")
    @Transactional
    public String fixAllVideoLessons() {
        List<Course> courses = courseRepository.findAll();
        int totalFixed = 0;

        for (Course course : courses) {
            // Use repository to get lessons
            List<Lesson> lessons = lessonRepository.findByCourseOrderByOrderIndexAsc(course);

            for (Lesson lesson : lessons) {
                if (lesson.getType() == LessonType.VIDEO &&
                        (lesson.getVideoUrl() == null || lesson.getVideoUrl().isEmpty())) {

                    // Assign appropriate video URL based on lesson title
                    String videoUrl;
                    String title = lesson.getTitle().toLowerCase();

                    if (title.contains("java")) {
                        videoUrl = "https://www.youtube.com/watch?v=eIrMbAQSU34";
                    } else if (title.contains("python")) {
                        videoUrl = "https://www.youtube.com/watch?v=_uQrJ0TkZlc";
                    } else if (title.contains("javascript")) {
                        videoUrl = "https://www.youtube.com/watch?v=PkZNo7MFNFg";
                    } else if (title.contains("react")) {
                        videoUrl = "https://www.youtube.com/watch?v=w7ejDZ8SWv8";
                    } else if (title.contains("html")) {
                        videoUrl = "https://www.youtube.com/watch?v=qz0aGYrrlhU";
                    } else if (title.contains("css")) {
                        videoUrl = "https://www.youtube.com/watch?v=1Rs2ND1ryYc";
                    } else if (title.contains("database") || title.contains("sql")) {
                        videoUrl = "https://www.youtube.com/watch?v=HXV3zeQKqGY";
                    } else if (title.contains("data science") || title.contains("machine learning")) {
                        videoUrl = "https://www.youtube.com/watch?v=KNAWp2S3w94";
                    } else if (title.contains("mobile") || title.contains("react native")) {
                        videoUrl = "https://www.youtube.com/watch?v=0-S5a0eXPoc";
                    } else {
                        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
                    }

                    lesson.setVideoUrl(videoUrl);
                    lessonRepository.save(lesson);
                    totalFixed++;
                }
            }
        }

        return "redirect:/courses?fixed=" + totalFixed;
    }

    // In TestVideoController.java - Update these calls
    @GetMapping("/test/add-sample-video/{courseId}")
    public String addSampleVideoLesson(@PathVariable Long courseId,
                                       @RequestParam(defaultValue = "YouTube Sample") String title) {
        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        try {
            // Use the new simple method
            lessonService.createVideoLesson(
                    title,
                    "This is a sample video lesson for testing video playback functionality.",
                    15,
                    course,
                    "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
            );

            // Use the new simple method
            lessonService.createVideoLesson(
                    "Sample Direct Video Lesson",
                    "This is a sample direct MP4 video lesson for testing HTML5 video player.",
                    10,
                    course,
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            );

            return "redirect:/courses/" + courseId + "?video_added=true";

        } catch (Exception e) {
            return "redirect:/courses/" + courseId + "?error=" + e.getMessage();
        }
    }
    @GetMapping("/test/video-test/{courseId}")
    @ResponseBody
    public String testVideoPlayback(@PathVariable Long courseId) {
        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        StringBuilder result = new StringBuilder();
        result.append("<h1>Video Test for Course: ").append(course.getTitle()).append("</h1>");

        List<Lesson> lessons = lessonRepository.findByCourseOrderByOrderIndexAsc(course);

        for (Lesson lesson : lessons) {
            if (lesson.getType() == LessonType.VIDEO) {
                result.append("<h3>").append(lesson.getTitle()).append("</h3>");
                result.append("<p>URL: ").append(lesson.getVideoUrl()).append("</p>");

                if (lesson.getVideoUrl() != null) {
                    if (lesson.getVideoUrl().contains("youtube.com/watch?v=")) {
                        String videoId = lesson.getVideoUrl().split("v=")[1];
                        if (videoId.contains("&")) videoId = videoId.split("&")[0];
                        String embedUrl = "https://www.youtube.com/embed/" + videoId;
                        result.append("<p><strong>Converted to:</strong> ").append(embedUrl).append("</p>");
                        result.append("<iframe width='560' height='315' src='").append(embedUrl).append("' frameborder='0' allowfullscreen></iframe><hr>");
                    } else if (lesson.getVideoUrl().contains("youtube.com/embed/")) {
                        result.append("<iframe width='560' height='315' src='").append(lesson.getVideoUrl()).append("' frameborder='0' allowfullscreen></iframe><hr>");
                    } else {
                        result.append("<video width='560' height='315' controls><source src='").append(lesson.getVideoUrl()).append("' type='video/mp4'></video><hr>");
                    }
                }
            }
        }

        return result.toString();
    }
}