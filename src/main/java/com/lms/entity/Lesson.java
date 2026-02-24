package com.lms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
@Entity
@Table(name = "lessons")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;



    private Integer duration; // in minutes

    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    private LessonType type;

    private String videoUrl; // UploadCare URL for video
    private String resourceUrl; // UploadCare URL for PDF/resources



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false) // ✅ Make sure nullable=false
    private Course course;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // FIXED: Changed from primitive boolean to Boolean wrapper
    @Column(name = "completed", nullable = true)
    private Boolean completed = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
    @OneToOne(mappedBy = "lesson", cascade = CascadeType.ALL)
    private Quiz quiz;

    // Getter
    public Quiz getQuiz() {
        return quiz;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Lesson() {}

    public Lesson(String title, String description, Integer duration, Integer orderIndex,
                  LessonType type, Course course) {
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.orderIndex = orderIndex;
        this.type = type;
        this.course = course;
        this.completed = false; // Set default value
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    public LessonType getType() { return type; }
    public void setType(LessonType type) { this.type = type; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getResourceUrl() { return resourceUrl; }
    public void setResourceUrl(String resourceUrl) { this.resourceUrl = resourceUrl; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // FIXED: Changed getter and setter to use Boolean
    public Boolean getCompleted() {
        return completed != null ? completed : false;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed != null ? completed : false;
    }

    // Also add a convenience method
    public boolean isCompleted() {
        return completed != null && completed;
    }




    // Helper method to extract YouTube ID
    private String extractYouTubeId(String url) {
        try {
            if (url.contains("v=")) {
                String videoId = url.split("v=")[1];
                if (videoId.contains("&")) {
                    videoId = videoId.split("&")[0];
                }
                return videoId;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    // ✅ FIXED: Better external video detection
    public boolean isExternalVideo() {
        if (this.videoUrl == null || this.videoUrl.isEmpty()) {
            return false;
        }

        String url = this.videoUrl.toLowerCase().trim();

        // Check if it's YouTube
        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            return true;
        }

        // Check if it's Vimeo
        if (url.contains("vimeo.com")) {
            return true;
        }

        // Check if it's a direct video link
        String[] videoExtensions = {".mp4", ".mov", ".avi", ".webm", ".mkv", ".flv", ".wmv"};
        for (String ext : videoExtensions) {
            if (url.endsWith(ext) || url.contains(ext + "?")) {
                return true;
            }
        }

        // Check if it's an embed URL
        if (url.contains("embed") || url.contains("player")) {
            return true;
        }

        return false;
    }

    // ✅ NEW: Check if video is from YouTube


    // ✅ NEW: Check if video is from Vimeo
    public boolean isVimeoVideo() {
        if (this.videoUrl == null) return false;
        return this.videoUrl.toLowerCase().contains("vimeo");
    }

    // ✅ NEW: Check if video is direct MP4/WebM
    public boolean isDirectVideo() {
        if (this.videoUrl == null) return false;
        String url = this.videoUrl.toLowerCase();
        return url.endsWith(".mp4") || url.endsWith(".webm") ||
                url.endsWith(".mov") || url.endsWith(".avi");
    }

    // In Lesson.java entity
// Add these methods to handle images:
    public boolean isImage() {
        return this.type == LessonType.IMAGE;
    }

    public String getImageUrl() {
        return this.resourceUrl; // For IMAGE type, resourceUrl stores the image URL
    }

// Add to the existing class - no need to change anything else

    // Add these methods to your Lesson entity class

    public boolean isYouTubeVideo() {
        if (this.videoUrl == null || this.videoUrl.isEmpty()) {
            return false;
        }
        return this.videoUrl.contains("youtube.com") || this.videoUrl.contains("youtu.be");
    }

    public String getVideoEmbedUrl() {
        if (!isYouTubeVideo()) {
            return this.videoUrl;
        }

        try {
            if (this.videoUrl.contains("youtube.com/watch?v=")) {
                // Convert watch URL to embed URL
                String videoId = this.videoUrl.split("v=")[1];
                if (videoId.contains("&")) {
                    videoId = videoId.split("&")[0];
                }
                return "https://www.youtube.com/embed/" + videoId;
            } else if (this.videoUrl.contains("youtu.be/")) {
                // Convert short URL to embed URL
                String videoId = this.videoUrl.split("youtu.be/")[1];
                if (videoId.contains("?")) {
                    videoId = videoId.split("\\?")[0];
                }
                return "https://www.youtube.com/embed/" + videoId;
            } else if (this.videoUrl.contains("youtube.com/embed/")) {
                // Already an embed URL
                return this.videoUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.videoUrl;
    }
}