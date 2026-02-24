package com.lms.service;

import com.lms.entity.Lesson;
import com.lms.entity.LessonType;
import com.lms.entity.Course;
import com.lms.entity.User;
import com.lms.entity.Quiz;
import com.lms.entity.Media;
import com.lms.repository.LessonRepository;
import com.lms.repository.QuizRepository;
import com.lms.repository.MediaRepository;
import com.lms.repository.LessonProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CloudinaryService cloudinaryService;
    private final QuizRepository quizRepository;
    private final MediaRepository mediaRepository;
    private final LessonProgressRepository lessonProgressRepository;

    public LessonService(LessonRepository lessonRepository,
                         CloudinaryService cloudinaryService,
                         QuizRepository quizRepository,
                         MediaRepository mediaRepository,
                         LessonProgressRepository lessonProgressRepository) {
        this.lessonRepository = lessonRepository;
        this.cloudinaryService = cloudinaryService;
        this.quizRepository = quizRepository;
        this.mediaRepository = mediaRepository;
        this.lessonProgressRepository = lessonProgressRepository;
    }

    // CREATE LESSON with Cloudinary integration
    public Lesson createLesson(String title, String description, Integer duration,
                               LessonType type, Course course,
                               MultipartFile videoFile, String videoUrl,
                               MultipartFile pdfFile, String pdfUrl,
                               MultipartFile imageFile, String imageUrl) throws IOException {

        System.out.println("=== DEBUG LessonService.createLesson ===");
        System.out.println("Title: " + title);
        System.out.println("Type: " + type);

        // Validate based on lesson type
        if (type == LessonType.VIDEO) {
            boolean hasVideoFile = videoFile != null && !videoFile.isEmpty();
            boolean hasVideoUrl = videoUrl != null && !videoUrl.trim().isEmpty();

            if (!hasVideoFile && !hasVideoUrl) {
                throw new IllegalArgumentException("Video lesson requires either a video file or video URL");
            }

            if (hasVideoFile && hasVideoUrl) {
                throw new IllegalArgumentException("Please provide either a video file OR a video URL, not both");
            }
        }

        if (type == LessonType.PDF) {
            boolean hasPdfFile = pdfFile != null && !pdfFile.isEmpty();
            boolean hasPdfUrl = pdfUrl != null && !pdfUrl.trim().isEmpty();

            if (!hasPdfFile && !hasPdfUrl) {
                throw new IllegalArgumentException("PDF lesson requires either a PDF file or PDF URL");
            }

            if (hasPdfFile && hasPdfUrl) {
                throw new IllegalArgumentException("Please provide either a PDF file OR a PDF URL, not both");
            }
        }

        if (type == LessonType.IMAGE) {
            boolean hasImageFile = imageFile != null && !imageFile.isEmpty();
            boolean hasImageUrl = imageUrl != null && !imageUrl.trim().isEmpty();

            System.out.println("IMAGE Validation - File: " + hasImageFile + ", URL: " + hasImageUrl);

            if (!hasImageFile && !hasImageUrl) {
                throw new IllegalArgumentException("Image lesson requires either an image file or image URL");
            }

            if (hasImageFile && hasImageUrl) {
                throw new IllegalArgumentException("Please provide either an image file OR an image URL, not both");
            }
        }

        Integer maxOrderIndex = lessonRepository.findMaxOrderIndexByCourse(course);
        int newOrderIndex = (maxOrderIndex != null ? maxOrderIndex : 0) + 1;

        Lesson lesson = new Lesson(title, description, duration, newOrderIndex, type, course);

        // Handle VIDEO content
        if (type == LessonType.VIDEO) {
            String finalVideoUrl = null;

            if (videoFile != null && !videoFile.isEmpty()) {
                if (!cloudinaryService.isVideoFile(videoFile.getContentType())) {
                    throw new IllegalArgumentException("Uploaded file must be a video (MP4, MOV, AVI, WebM, etc.)");
                }
                finalVideoUrl = cloudinaryService.uploadFile(videoFile);
                System.out.println("✅ Video uploaded to Cloudinary: " + finalVideoUrl);
            } else if (videoUrl != null && !videoUrl.trim().isEmpty()) {
                if (!isValidVideoUrl(videoUrl)) {
                    throw new IllegalArgumentException("Invalid video URL. Supported: YouTube, Vimeo, or direct video links (.mp4, .mov, etc.)");
                }
                finalVideoUrl = videoUrl;
                System.out.println("✅ External video URL stored: " + videoUrl);
            }

            lesson.setVideoUrl(finalVideoUrl);
            lesson.setResourceUrl(null);
        }

        // Handle PDF content
        if (type == LessonType.PDF) {
            String finalPdfUrl = null;

            if (pdfFile != null && !pdfFile.isEmpty()) {
                System.out.println("Uploading PDF file to Cloudinary...");
                if (!cloudinaryService.isPdfFile(pdfFile.getContentType())) {
                    throw new IllegalArgumentException("File must be a PDF");
                }
                finalPdfUrl = cloudinaryService.uploadFile(pdfFile);
                System.out.println("✅ PDF uploaded to Cloudinary: " + finalPdfUrl);
            } else if (pdfUrl != null && !pdfUrl.trim().isEmpty()) {
                System.out.println("Using PDF URL: " + pdfUrl);
                if (!isValidUrl(pdfUrl)) {
                    throw new IllegalArgumentException("Invalid PDF URL");
                }
                finalPdfUrl = pdfUrl;
                System.out.println("✅ Using external PDF URL: " + pdfUrl);
            }

            lesson.setResourceUrl(finalPdfUrl);
            lesson.setVideoUrl(null);
        }

        // Handle IMAGE content
        if (type == LessonType.IMAGE) {
            String finalImageUrl = null;

            if (imageFile != null && !imageFile.isEmpty()) {
                System.out.println("Uploading image file to Cloudinary...");
                if (!cloudinaryService.isImageFile(imageFile.getContentType())) {
                    throw new IllegalArgumentException("File must be an image (JPG, PNG, GIF, etc.)");
                }
                finalImageUrl = cloudinaryService.uploadFile(imageFile);
                System.out.println("✅ Image uploaded to Cloudinary: " + finalImageUrl);
            } else if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                System.out.println("Using image URL: " + imageUrl);
                if (!isValidUrl(imageUrl)) {
                    throw new IllegalArgumentException("Invalid image URL");
                }
                finalImageUrl = imageUrl;
                System.out.println("✅ Using external image URL: " + imageUrl);
            }

            lesson.setResourceUrl(finalImageUrl);
            lesson.setVideoUrl(null);
        }

        // For TEXT lessons, no file handling needed
        if (type == LessonType.TEXT) {
            lesson.setResourceUrl(null);
            lesson.setVideoUrl(null);
        }

        return lessonRepository.save(lesson);
    }

    // UPDATE LESSON
    public Lesson updateLesson(Long lessonId, String title, String description, Integer duration,
                               LessonType type, MultipartFile videoFile, String videoUrl,
                               MultipartFile pdfFile, String pdfUrl) throws IOException {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        // Store old URLs for cleanup
        String oldVideoUrl = lesson.getVideoUrl();
        String oldResourceUrl = lesson.getResourceUrl();

        // Update basic fields
        lesson.setTitle(title);
        lesson.setDescription(description);
        lesson.setDuration(duration);
        lesson.setType(type);

        // Handle VIDEO content update
        if (type == LessonType.VIDEO) {
            String finalVideoUrl = null;

            // New file upload
            if (videoFile != null && !videoFile.isEmpty()) {
                if (!cloudinaryService.isVideoFile(videoFile.getContentType())) {
                    throw new IllegalArgumentException("Video file must be a video format");
                }
                finalVideoUrl = cloudinaryService.uploadFile(videoFile);
                System.out.println("✅ New video uploaded to Cloudinary: " + finalVideoUrl);
            }
            // New URL
            else if (videoUrl != null && !videoUrl.trim().isEmpty()) {
                if (!isValidVideoUrl(videoUrl)) {
                    throw new IllegalArgumentException("Invalid video URL");
                }
                finalVideoUrl = videoUrl;
                System.out.println("✅ Video URL updated: " + videoUrl);
            } else {
                // Keep existing
                finalVideoUrl = oldVideoUrl;
            }

            // Clean up old Cloudinary file if replaced
            if (oldVideoUrl != null && !oldVideoUrl.equals(finalVideoUrl) &&
                    isCloudinaryUrl(oldVideoUrl)) {
                cloudinaryService.deleteFile(oldVideoUrl);
                System.out.println("🗑️ Old Cloudinary video deleted: " + oldVideoUrl);
            }

            lesson.setVideoUrl(finalVideoUrl);
            lesson.setResourceUrl(null);
        }

        // Handle PDF content update
        if (type == LessonType.PDF) {
            String finalPdfUrl = null;

            // New file upload
            if (pdfFile != null && !pdfFile.isEmpty()) {
                if (!cloudinaryService.isPdfFile(pdfFile.getContentType())) {
                    throw new IllegalArgumentException("File must be a PDF");
                }
                finalPdfUrl = cloudinaryService.uploadFile(pdfFile);
                System.out.println("✅ New PDF uploaded to Cloudinary: " + finalPdfUrl);
            }
            // New URL
            else if (pdfUrl != null && !pdfUrl.trim().isEmpty()) {
                if (!isValidUrl(pdfUrl)) {
                    throw new IllegalArgumentException("Invalid PDF URL");
                }
                finalPdfUrl = pdfUrl;
                System.out.println("✅ PDF URL updated: " + pdfUrl);
            } else {
                // Keep existing
                finalPdfUrl = oldResourceUrl;
            }

            // Clean up old Cloudinary file if replaced
            if (oldResourceUrl != null && !oldResourceUrl.equals(finalPdfUrl) &&
                    isCloudinaryUrl(oldResourceUrl)) {
                cloudinaryService.deleteFile(oldResourceUrl);
                System.out.println("🗑️ Old Cloudinary PDF deleted: " + oldResourceUrl);
            }

            lesson.setResourceUrl(finalPdfUrl);
            lesson.setVideoUrl(null);
        }

        return lessonRepository.save(lesson);
    }

    // DELETE LESSON - COMPLETE VERSION
    @Transactional
    public void deleteLesson(Long lessonId) {
        System.out.println("\n╔═══════════════════════════════════════════════════╗");
        System.out.println("║       🗑️  LESSON SERVICE: DELETE LESSON          ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println("📋 Lesson ID: " + lessonId);

        try {
            // Step 1: Find the lesson
            System.out.println("\n📍 Step 1: Finding lesson in database...");
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Lesson not found with ID: " + lessonId));

            System.out.println("   ✅ Lesson found:");
            System.out.println("      • ID: " + lesson.getId());
            System.out.println("      • Title: " + lesson.getTitle());
            System.out.println("      • Type: " + lesson.getType());

            Course course = lesson.getCourse();
            System.out.println("      • Course: " + course.getTitle() + " (ID: " + course.getId() + ")");

            // Step 2: Delete lesson progress records FIRST
            System.out.println("\n📍 Step 2: Deleting lesson progress records...");
            int deletedProgressCount = lessonProgressRepository.deleteByLessonId(lessonId);
            System.out.println("   ✅ Deleted " + deletedProgressCount + " progress record(s)");

            // ✅ FIXED: Step 3: Delete associated quizzes using findByLessonId
            System.out.println("\n📍 Step 3: Deleting associated quizzes...");
            Optional<Quiz> lessonQuiz = quizRepository.findByLessonId(lesson.getId());
            if (lessonQuiz.isPresent()) {
                quizRepository.delete(lessonQuiz.get());
                System.out.println("   ✅ Deleted quiz for lesson");
            } else {
                System.out.println("   ℹ️  No quizzes to delete");
            }

            // Step 4: Delete associated media files
            System.out.println("\n📍 Step 4: Deleting associated media files...");
            List<Media> lessonMedia = mediaRepository.findByLesson(lesson);
            if (!lessonMedia.isEmpty()) {
                for (Media media : lessonMedia) {
                    if (media.getFileUrl() != null && media.getFileUrl().contains("cloudinary.com")) {
                        try {
                            cloudinaryService.deleteFile(media.getFileUrl());
                            System.out.println("   ✅ Deleted media from Cloudinary: " + media.getFileUrl());
                        } catch (Exception e) {
                            System.err.println("   ⚠️  Could not delete media: " + e.getMessage());
                        }
                    }
                }
                mediaRepository.deleteAll(lessonMedia);
                System.out.println("   ✅ Deleted " + lessonMedia.size() + " media file(s)");
            } else {
                System.out.println("   ℹ️  No media files to delete");
            }

            // Step 5: Delete video from Cloudinary
            System.out.println("\n📍 Step 5: Deleting video content...");
            if (lesson.getVideoUrl() != null && lesson.getVideoUrl().contains("cloudinary.com")) {
                try {
                    cloudinaryService.deleteFile(lesson.getVideoUrl());
                    System.out.println("   ✅ Video deleted from Cloudinary");
                } catch (Exception e) {
                    System.err.println("   ⚠️  Could not delete video: " + e.getMessage());
                }
            } else {
                System.out.println("   ℹ️  No Cloudinary video to delete");
            }

            // Step 6: Delete resource from Cloudinary
            System.out.println("\n📍 Step 6: Deleting resource content...");
            if (lesson.getResourceUrl() != null && lesson.getResourceUrl().contains("cloudinary.com")) {
                try {
                    cloudinaryService.deleteFile(lesson.getResourceUrl());
                    System.out.println("   ✅ Resource deleted from Cloudinary");
                } catch (Exception e) {
                    System.err.println("   ⚠️  Could not delete resource: " + e.getMessage());
                }
            } else {
                System.out.println("   ℹ️  No Cloudinary resource to delete");
            }

            // Step 7: Delete the lesson from database
            System.out.println("\n📍 Step 7: Deleting lesson from database...");
            lessonRepository.delete(lesson);
            System.out.println("   ✅ Lesson deleted from database");

            // Step 8: Reorder remaining lessons
            System.out.println("\n📍 Step 8: Reordering remaining lessons...");
            reorderLessons(course);

            System.out.println("\n╔═══════════════════════════════════════════════════╗");
            System.out.println("║         ✅ LESSON DELETED SUCCESSFULLY            ║");
            System.out.println("╚═══════════════════════════════════════════════════╝\n");

        } catch (Exception e) {
            System.err.println("\n╔═══════════════════════════════════════════════════╗");
            System.err.println("║         ❌ LESSON DELETE FAILED                   ║");
            System.err.println("╚═══════════════════════════════════════════════════╝");
            System.err.println("Error Type: " + e.getClass().getSimpleName());
            System.err.println("Error Message: " + e.getMessage());
            System.err.println("\nStack Trace:");
            e.printStackTrace();
            throw new RuntimeException("Failed to delete lesson: " + e.getMessage(), e);
        }
    }

    private void reorderLessons(Course course) {
        List<Lesson> lessons = lessonRepository.findByCourseOrderByOrderIndexAsc(course);

        for (int i = 0; i < lessons.size(); i++) {
            lessons.get(i).setOrderIndex(i + 1);
        }

        lessonRepository.saveAll(lessons);
        System.out.println("   ✅ Reordered " + lessons.size() + " lesson(s)");
    }

    // Helper method to check if URL is from Cloudinary
    private boolean isCloudinaryUrl(String url) {
        return url != null && url.contains("cloudinary.com");
    }

    // URL Validation Helpers
    private boolean isValidVideoUrl(String url) {
        if (url == null) return false;
        return isYouTubeUrl(url) || isVimeoUrl(url) || isDirectVideoUrl(url);
    }

    private boolean isYouTubeUrl(String url) {
        if (url == null) return false;
        String youtubeRegex = "^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/).+$";
        return Pattern.matches(youtubeRegex, url);
    }

    private boolean isVimeoUrl(String url) {
        if (url == null) return false;
        String vimeoRegex = "^(https?://)?(www\\.)?vimeo\\.com/.+$";
        return Pattern.matches(vimeoRegex, url);
    }

    private boolean isDirectVideoUrl(String url) {
        if (url == null) return false;
        String videoRegex = "^(https?://).+\\.(mp4|mov|avi|webm|mkv|flv|wmv)(\\?.*)?$";
        return Pattern.matches(videoRegex, url.toLowerCase());
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Other service methods
    public Optional<Lesson> getLessonById(Long lessonId) {
        return lessonRepository.findById(lessonId);
    }

    public List<Lesson> getLessonsByCourse(Course course) {
        return lessonRepository.findByCourseOrderByOrderIndexAsc(course);
    }

    public List<Lesson> getNextLessons(Course course, Integer currentIndex) {
        return lessonRepository.findNextLessons(course, currentIndex);
    }

    public Optional<Lesson> getLessonByCourseAndOrder(Course course, Integer orderIndex) {
        return lessonRepository.findByCourseAndOrderIndex(course, orderIndex);
    }

    public List<Lesson> getAllLessons() {
        return lessonRepository.findAll();
    }

    // Simple createLesson method for testing
    public Lesson createSimpleLesson(String title, String description, Integer duration,
                                     LessonType type, Course course,
                                     String videoUrl, String resourceUrl) throws IOException {
        return createLesson(title, description, duration, type, course,
                null, videoUrl, null, resourceUrl, null, null);
    }

    // Simple createLesson for PDF only
    public Lesson createPdfLesson(String title, String description, Integer duration,
                                  Course course, String pdfUrl) throws IOException {
        return createLesson(title, description, duration, LessonType.PDF, course,
                null, null, null, pdfUrl, null, null);
    }

    // Simple createLesson for video only
    public Lesson createVideoLesson(String title, String description, Integer duration,
                                    Course course, String videoUrl) throws IOException {
        return createLesson(title, description, duration, LessonType.VIDEO, course,
                null, videoUrl, null, null, null, null);
    }
}