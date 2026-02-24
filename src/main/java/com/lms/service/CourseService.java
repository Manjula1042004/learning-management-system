package com.lms.service;

import com.lms.entity.Course;
import com.lms.entity.Quiz;
import com.lms.entity.CourseStatus;
import com.lms.entity.Lesson;
import com.lms.entity.Media;
import com.lms.entity.User;
import com.lms.repository.CourseRepository;
import com.lms.repository.EnrollmentRepository;
import com.lms.repository.LessonRepository;
import com.lms.repository.MediaRepository;
import com.lms.repository.QuizRepository;
import com.lms.repository.LessonProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final CloudinaryService cloudinaryService;
    private final MediaRepository mediaRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository; // Added this field

    public CourseService(CourseRepository courseRepository,
                         CloudinaryService cloudinaryService,
                         MediaRepository mediaRepository,
                         LessonRepository lessonRepository,
                         QuizRepository quizRepository,
                         EnrollmentRepository enrollmentRepository,
                         LessonProgressRepository lessonProgressRepository) { // Added parameter
        this.courseRepository = courseRepository;
        this.cloudinaryService = cloudinaryService;
        this.mediaRepository = mediaRepository;
        this.lessonRepository = lessonRepository;
        this.quizRepository = quizRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonProgressRepository = lessonProgressRepository; // Initialize it
    }

    public Course createCourse(String title, String description, Double price,
                               User instructor, MultipartFile thumbnail) throws IOException {
        Course course = new Course(title, description, price, instructor);

        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                if (cloudinaryService.isImageFile(thumbnail.getContentType())) {
                    String thumbnailUrl = cloudinaryService.uploadFile(thumbnail);
                    course.setThumbnailUrl(thumbnailUrl);
                    System.out.println("✅ Course thumbnail uploaded to Cloudinary: " + thumbnailUrl);
                } else {
                    course.setThumbnailUrl(getPlaceholderImage());
                }
            } catch (Exception e) {
                System.err.println("⚠️ Cloudinary upload failed, using placeholder: " + e.getMessage());
                course.setThumbnailUrl(getPlaceholderImage());
            }
        } else {
            course.setThumbnailUrl(getPlaceholderImage());
        }

        return courseRepository.save(course);
    }

    public Course updateCourse(Long courseId, String title, String description, Double price,
                               MultipartFile thumbnail) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        String oldThumbnailUrl = course.getThumbnailUrl();

        course.setTitle(title);
        course.setDescription(description);
        course.setPrice(price);

        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                if (cloudinaryService.isImageFile(thumbnail.getContentType())) {
                    String thumbnailUrl = cloudinaryService.uploadFile(thumbnail);
                    course.setThumbnailUrl(thumbnailUrl);
                    System.out.println("✅ Course thumbnail uploaded to Cloudinary: " + thumbnailUrl);

                    if (oldThumbnailUrl != null && oldThumbnailUrl.contains("cloudinary.com")) {
                        try {
                            cloudinaryService.deleteFile(oldThumbnailUrl);
                            System.out.println("🗑️ Old thumbnail deleted from Cloudinary: " + oldThumbnailUrl);
                        } catch (Exception e) {
                            System.err.println("⚠️ Could not delete old thumbnail: " + e.getMessage());
                        }
                    }
                } else {
                    course.setThumbnailUrl(getPlaceholderImage());
                }
            } catch (Exception e) {
                System.err.println("⚠️ Cloudinary upload failed: " + e.getMessage());
                course.setThumbnailUrl(getPlaceholderImage());
            }
        }

        return courseRepository.save(course);
    }

    public Course updateCourseStatus(Long courseId, CourseStatus status) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        course.setStatus(status);
        return courseRepository.save(course);
    }

    public List<Course> getCoursesByInstructor(User instructor) {
        return courseRepository.findByInstructor(instructor);
    }

    public List<Course> getPendingCourses() {
        return courseRepository.findByStatus(CourseStatus.PENDING);
    }

    public List<Course> getApprovedCourses() {
        return courseRepository.findByStatus(CourseStatus.APPROVED);
    }

    public List<Course> searchCourses(String query) {
        return courseRepository.searchApprovedCourses(query);
    }

    public Optional<Course> getCourseById(Long courseId) {
        return courseRepository.findById(courseId);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public CourseRepository getCourseRepository() {
        return courseRepository;
    }

    private String getPlaceholderImage() {
        String[] placeholderImages = {
                "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1517077304055-6e89abbf09b0?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1542744095-fcf48d80b0fd?w=800&h=600&fit=crop",
                "https://images.unsplash.com/photo-1545235617-9465d2a55698?w=800&h=600&fit=crop"
        };
        Random random = new Random();
        return placeholderImages[random.nextInt(placeholderImages.length)];
    }

    // Simple delete method (without permission check)
    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        courseRepository.delete(course);
    }

    // Detailed delete method with permission check and cleanup
    @Transactional
    public void deleteCourse(Long courseId, User requestingUser) {
        System.out.println("\n╔═══════════════════════════════════════════════════╗");
        System.out.println("║       🗑️  COURSE SERVICE: DELETE COURSE          ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println("📋 Course ID: " + courseId);
        System.out.println("👤 Requested by: " + requestingUser.getUsername());

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

        // Check permission
        if (!requestingUser.isAdmin() &&
                !course.getInstructor().getId().equals(requestingUser.getId())) {
            throw new RuntimeException("You don't have permission to delete this course");
        }

        System.out.println("✅ Permission granted");
        System.out.println("📚 Course: " + course.getTitle());
        System.out.println("📊 Number of lessons: " + course.getLessons().size());

        try {
            // 1. Delete all lessons manually with their associated files
            System.out.println("\n=== DELETING LESSONS ===");
            List<Lesson> lessons = lessonRepository.findByCourseOrderByOrderIndexAsc(course);
            System.out.println("Found " + lessons.size() + " lessons to delete");

            for (Lesson lesson : lessons) {
                System.out.println("Deleting lesson: " + lesson.getTitle() + " (ID: " + lesson.getId() + ")");

                // Delete video from Cloudinary if exists
                if (lesson.getVideoUrl() != null && lesson.getVideoUrl().contains("cloudinary.com")) {
                    try {
                        cloudinaryService.deleteFile(lesson.getVideoUrl());
                        System.out.println("  ✅ Video deleted from Cloudinary");
                    } catch (Exception e) {
                        System.out.println("  ⚠️ Could not delete video: " + e.getMessage());
                    }
                }

                // Delete resource from Cloudinary if exists
                if (lesson.getResourceUrl() != null && lesson.getResourceUrl().contains("cloudinary.com")) {
                    try {
                        cloudinaryService.deleteFile(lesson.getResourceUrl());
                        System.out.println("  ✅ Resource deleted from Cloudinary");
                    } catch (Exception e) {
                        System.out.println("  ⚠️ Could not delete resource: " + e.getMessage());
                    }
                }

                // Delete quizzes for this lesson
                Optional<Quiz> lessonQuiz = quizRepository.findByLessonId(lesson.getId());
                if (lessonQuiz.isPresent()) {
                    quizRepository.delete(lessonQuiz.get());
                    System.out.println("  ✅ Deleted quiz for lesson");
                }

                // Delete media files for this lesson
                List<Media> lessonMedia = mediaRepository.findByLesson(lesson);
                if (!lessonMedia.isEmpty()) {
                    for (Media media : lessonMedia) {
                        if (media.getFileUrl() != null && media.getFileUrl().contains("cloudinary.com")) {
                            try {
                                cloudinaryService.deleteFile(media.getFileUrl());
                            } catch (Exception e) {
                                // Ignore Cloudinary errors
                            }
                        }
                    }
                    mediaRepository.deleteAll(lessonMedia);
                    System.out.println("  ✅ Deleted " + lessonMedia.size() + " media files");
                }

                // Delete lesson progress records
                try {
                    int deletedCount = lessonProgressRepository.deleteByLessonId(lesson.getId());
                    System.out.println("  ✅ Deleted " + deletedCount + " lesson progress records");
                } catch (Exception e) {
                    System.out.println("  ⚠️ Could not delete lesson progress: " + e.getMessage());
                }

                // Delete the lesson itself
                lessonRepository.delete(lesson);
                System.out.println("  ✅ Lesson deleted from database");
            }

            // 2. Delete course thumbnail from Cloudinary
            System.out.println("\n=== DELETING COURSE THUMBNAIL ===");
            if (course.getThumbnailUrl() != null && course.getThumbnailUrl().contains("cloudinary.com")) {
                try {
                    cloudinaryService.deleteFile(course.getThumbnailUrl());
                    System.out.println("✅ Course thumbnail deleted from Cloudinary");
                } catch (Exception e) {
                    System.out.println("⚠️ Could not delete thumbnail: " + e.getMessage());
                }
            }

            // 3. Delete course media files
            System.out.println("\n=== DELETING COURSE MEDIA ===");
            List<Media> courseMedia = mediaRepository.findByCourse(course);
            if (!courseMedia.isEmpty()) {
                for (Media media : courseMedia) {
                    if (media.getFileUrl() != null && media.getFileUrl().contains("cloudinary.com")) {
                        try {
                            cloudinaryService.deleteFile(media.getFileUrl());
                        } catch (Exception e) {
                            // Ignore Cloudinary errors
                        }
                    }
                }
                mediaRepository.deleteAll(courseMedia);
                System.out.println("✅ Deleted " + courseMedia.size() + " course media files");
            }

            // 4. Finally delete the course
            System.out.println("\n=== DELETING COURSE ===");
            courseRepository.delete(course);

            System.out.println("\n╔═══════════════════════════════════════════════════╗");
            System.out.println("║         ✅ COURSE DELETED SUCCESSFULLY           ║");
            System.out.println("╚═══════════════════════════════════════════════════╝\n");

        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete course: " + e.getMessage());
        }
    }
}