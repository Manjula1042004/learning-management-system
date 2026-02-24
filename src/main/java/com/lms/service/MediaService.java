package com.lms.service;

import com.lms.entity.Media;
import com.lms.entity.User;
import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.repository.MediaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MediaService {

    private final MediaRepository mediaRepository;
    private final CloudinaryService cloudinaryService;
    private final CourseService courseService;
    private final LessonService lessonService;

    public MediaService(MediaRepository mediaRepository,
                        CloudinaryService cloudinaryService,
                        CourseService courseService,
                        LessonService lessonService) {
        this.mediaRepository = mediaRepository;
        this.cloudinaryService = cloudinaryService;
        this.courseService = courseService;
        this.lessonService = lessonService;
    }

    /**
     * Upload media file for a course
     */
    public Media uploadMediaForCourse(MultipartFile file,
                                      Long courseId,
                                      User uploader,
                                      String description) throws IOException {

        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Validate file
        validateFile(file);

        // Upload to Cloudinary
        String fileUrl = cloudinaryService.uploadFile(file);

        // Determine file type
        String fileType = determineFileType(file.getContentType());

        // Generate unique stored file name
        String storedFileName = generateStoredFileName(file.getOriginalFilename());

        // Create media entity
        Media media = new Media(
                file.getOriginalFilename(),
                storedFileName,
                fileUrl,
                fileType,
                file.getSize(),
                file.getContentType(),
                uploader,
                course,
                null // No lesson association yet
        );

        return mediaRepository.save(media);
    }

    /**
     * Upload media file for a specific lesson
     */
    public Media uploadMediaForLesson(MultipartFile file,
                                      Long lessonId,
                                      User uploader,
                                      String description) throws IOException {

        Lesson lesson = lessonService.getLessonById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        // Validate file
        validateFile(file);

        // Upload to Cloudinary
        String fileUrl = cloudinaryService.uploadFile(file);

        // Determine file type
        String fileType = determineFileType(file.getContentType());

        // Generate unique stored file name
        String storedFileName = generateStoredFileName(file.getOriginalFilename());

        // Create media entity
        Media media = new Media(
                file.getOriginalFilename(),
                storedFileName,
                fileUrl,
                fileType,
                file.getSize(),
                file.getContentType(),
                uploader,
                lesson.getCourse(),
                lesson
        );

        return mediaRepository.save(media);
    }

    /**
     * Get all media for a course
     */
    public List<Media> getMediaByCourse(Long courseId) {
        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return mediaRepository.findByCourse(course);
    }

    /**
     * Get media by type for a course
     */
    public List<Media> getMediaByCourseAndType(Long courseId, String fileType) {
        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return mediaRepository.findByCourseAndFileType(course, fileType);
    }

    /**
     * Get media by lesson
     */
    public List<Media> getMediaByLesson(Long lessonId) {
        Lesson lesson = lessonService.getLessonById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        return mediaRepository.findByLesson(lesson);
    }

    /**
     * Delete media file
     */
    public void deleteMedia(Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found"));

        // Delete from Cloudinary if it's a Cloudinary URL
        if (media.getFileUrl() != null && media.getFileUrl().contains("cloudinary.com")) {
            try {
                cloudinaryService.deleteFile(media.getFileUrl());
            } catch (Exception e) {
                System.err.println("Failed to delete from Cloudinary: " + e.getMessage());
            }
        }

        mediaRepository.delete(media);
    }

    /**
     * Update media description or visibility
     */
    public Media updateMedia(Long mediaId, String description, Boolean isPublic) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found"));

        media.setIsPublic(isPublic);
        return mediaRepository.save(media);
    }

    /**
     * Get course media statistics
     */
    public MediaStatistics getCourseMediaStatistics(Long courseId) {
        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Long totalFiles = mediaRepository.countByCourse(course);
        Long totalSize = mediaRepository.getTotalFileSizeByCourse(course);

        List<Media> videos = mediaRepository.findByCourseAndFileType(course, "video");
        List<Media> pdfs = mediaRepository.findByCourseAndFileType(course, "pdf");
        List<Media> images = mediaRepository.findByCourseAndFileType(course, "image");
        List<Media> audio = mediaRepository.findByCourseAndFileType(course, "audio");

        return new MediaStatistics(totalFiles, totalSize,
                videos.size(), pdfs.size(), images.size(), audio.size());
    }

    // Helper methods
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        long maxFileSize = 500 * 1024 * 1024; // 500MB
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds 500MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("Could not determine file type");
        }

        // Validate file type
        if (!cloudinaryService.isVideoFile(contentType) &&
                !cloudinaryService.isImageFile(contentType) &&
                !cloudinaryService.isPdfFile(contentType) &&
                !isAudioFile(contentType)) {
            throw new IllegalArgumentException("Unsupported file type. Supported: video, image, pdf, audio");
        }
    }

    private String determineFileType(String contentType) {
        if (contentType == null) return "other";

        if (contentType.startsWith("video/")) {
            return "video";
        } else if (contentType.startsWith("image/")) {
            return "image";
        } else if (contentType.equals("application/pdf")) {
            return "pdf";
        } else if (contentType.startsWith("audio/")) {
            return "audio";
        } else {
            return "other";
        }
    }

    private boolean isAudioFile(String contentType) {
        return contentType != null && contentType.startsWith("audio/");
    }

    private String generateStoredFileName(String originalFileName) {
        String extension = "";
        int i = originalFileName.lastIndexOf('.');
        if (i > 0) {
            extension = originalFileName.substring(i);
        }
        return "media_" + UUID.randomUUID().toString() + extension;
    }

    // Statistics DTO
    public static class MediaStatistics {
        private Long totalFiles;
        private Long totalSize; // in bytes
        private Integer videoCount;
        private Integer pdfCount;
        private Integer imageCount;
        private Integer audioCount;

        // Constructor
        public MediaStatistics(Long totalFiles, Long totalSize,
                               Integer videoCount, Integer pdfCount,
                               Integer imageCount, Integer audioCount) {
            this.totalFiles = totalFiles;
            this.totalSize = totalSize;
            this.videoCount = videoCount;
            this.pdfCount = pdfCount;
            this.imageCount = imageCount;
            this.audioCount = audioCount;
        }

        // Getters
        public Long getTotalFiles() { return totalFiles; }
        public Long getTotalSize() { return totalSize; }
        public Integer getVideoCount() { return videoCount; }
        public Integer getPdfCount() { return pdfCount; }
        public Integer getImageCount() { return imageCount; }
        public Integer getAudioCount() { return audioCount; }

        // Getter method for formatted total size
        public String getFormattedTotalSize() {
            if (totalSize == null) return "0 B";

            if (totalSize < 1024) {
                return totalSize + " B";
            } else if (totalSize < 1024 * 1024) {
                return String.format("%.2f KB", totalSize / 1024.0);
            } else if (totalSize < 1024 * 1024 * 1024) {
                return String.format("%.2f MB", totalSize / (1024.0 * 1024.0));
            } else {
                return String.format("%.2f GB", totalSize / (1024.0 * 1024.0 * 1024.0));
            }
        }
    }
}