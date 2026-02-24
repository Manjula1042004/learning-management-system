package com.lms.controller;

import com.lms.entity.Media;
import com.lms.entity.User;
import com.lms.service.MediaService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/media")
public class MediaController {

    private final MediaService mediaService;
    private final com.lms.service.UserService userService;
    private final com.lms.service.CourseService courseService;

    public MediaController(MediaService mediaService,
                           com.lms.service.UserService userService,
                           com.lms.service.CourseService courseService) {
        this.mediaService = mediaService;
        this.userService = userService;
        this.courseService = courseService;
    }

    /**
     * View course media library
     */
    @GetMapping("/course/{courseId}")
    public String viewCourseMedia(@PathVariable Long courseId,
                                  @RequestParam(required = false) String fileType,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {

        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check permissions
        if (!user.isAdmin() && !user.isInstructor() && !user.equals(course.getInstructor())) {
            return "redirect:/courses/" + courseId + "?error=unauthorized";
        }

        List<Media> mediaList;
        if (fileType != null && !fileType.isEmpty()) {
            mediaList = mediaService.getMediaByCourseAndType(courseId, fileType);
        } else {
            mediaList = mediaService.getMediaByCourse(courseId);
        }

        var statistics = mediaService.getCourseMediaStatistics(courseId);

        model.addAttribute("user", user);
        model.addAttribute("course", course);
        model.addAttribute("mediaList", mediaList);
        model.addAttribute("statistics", statistics);
        model.addAttribute("selectedFileType", fileType);

        return "media/library";
    }

    /**
     * Upload media form
     */
    @GetMapping("/upload/course/{courseId}")
    public String showUploadForm(@PathVariable Long courseId,
                                 @RequestParam(required = false) Long lessonId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {

        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check permissions
        if (!user.isAdmin() && !user.isInstructor() && !user.equals(course.getInstructor())) {
            return "redirect:/courses/" + courseId + "?error=unauthorized";
        }

        model.addAttribute("user", user);
        model.addAttribute("course", course);
        model.addAttribute("lessonId", lessonId);

        if (lessonId != null) {
            var lesson = course.getLessons().stream()
                    .filter(l -> l.getId().equals(lessonId))
                    .findFirst()
                    .orElse(null);
            model.addAttribute("lesson", lesson);
        }

        return "media/upload";
    }

    /**
     * Upload media file
     */
    @PostMapping("/upload/course/{courseId}")
    public String uploadMedia(@PathVariable Long courseId,
                              @RequestParam(required = false) Long lessonId,
                              @RequestParam("file") MultipartFile file,
                              @RequestParam(required = false) String description,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {

        try {
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Media media;
            if (lessonId != null) {
                media = mediaService.uploadMediaForLesson(file, lessonId, user, description);
            } else {
                media = mediaService.uploadMediaForCourse(file, courseId, user, description);
            }

            redirectAttributes.addFlashAttribute("success",
                    "File '" + media.getOriginalFileName() + "' uploaded successfully!");

            if (lessonId != null) {
                return "redirect:/courses/" + courseId + "?lesson=" + lessonId;
            } else {
                return "redirect:/media/course/" + courseId;
            }

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Upload failed: " + e.getMessage());
            return "redirect:/media/upload/course/" + courseId +
                    (lessonId != null ? "?lessonId=" + lessonId : "");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/media/upload/course/" + courseId +
                    (lessonId != null ? "?lessonId=" + lessonId : "");
        }
    }

    /**
     * Delete media file
     */
    @PostMapping("/delete/{mediaId}")
    public String deleteMedia(@PathVariable Long mediaId,
                              @RequestParam Long courseId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {

        try {
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Get media to check permissions
            var media = mediaService.getMediaByCourse(courseId).stream()
                    .filter(m -> m.getId().equals(mediaId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Media not found"));

            // Check permissions
            if (!user.isAdmin() && !user.equals(media.getUploadedBy())) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this file");
                return "redirect:/media/course/" + courseId;
            }

            mediaService.deleteMedia(mediaId);
            redirectAttributes.addFlashAttribute("success", "File deleted successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Delete failed: " + e.getMessage());
        }

        return "redirect:/media/course/" + courseId;
    }

    /**
     * Preview media file
     */
    @GetMapping("/preview/{mediaId}")
    public String previewMedia(@PathVariable Long mediaId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {

        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get media (you'll need to add a getMediaById method to MediaService)
        // For now, we'll redirect to the file URL
        // In a real implementation, you'd fetch the media entity

        return "media/preview";
    }
}