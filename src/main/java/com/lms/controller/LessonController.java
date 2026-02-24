package com.lms.controller;

import com.lms.entity.Lesson;
import com.lms.entity.LessonType;
import com.lms.entity.Course;
import com.lms.entity.User;
import com.lms.entity.Quiz;
import com.lms.service.LessonService;
import com.lms.service.CourseService;
import com.lms.service.UserService;
import com.lms.service.MediaService;
import com.lms.service.QuizService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Optional;

@Controller
@RequestMapping("/lessons")
public class LessonController {

    private final LessonService lessonService;
    private final CourseService courseService;
    private final UserService userService;
    private final MediaService mediaService;
    private final QuizService quizService;

    public LessonController(LessonService lessonService,
                            CourseService courseService,
                            UserService userService,
                            MediaService mediaService,
                            QuizService quizService) {
        this.lessonService = lessonService;
        this.courseService = courseService;
        this.userService = userService;
        this.mediaService = mediaService;
        this.quizService = quizService;
    }

    // ✅ TEST ENDPOINT - To verify controller is loading
    @GetMapping("/test-controller")
    @ResponseBody
    public String testController() {
        return "✅ LessonController is WORKING! Time: " + java.time.LocalDateTime.now();
    }

    // ═══════════════════════════════════════════════════════════
    // ✅ GET: SHOW CREATE LESSON FORM
    // ═══════════════════════════════════════════════════════════
    @GetMapping("/create/{courseId}")
    public String showCreateForm(@PathVariable Long courseId, Model model) {
        System.out.println("📄 Showing create lesson form for course ID: " + courseId);

        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        model.addAttribute("course", course);
        model.addAttribute("lessonTypes", LessonType.values());

        return "lessons/create";
    }

    // ═══════════════════════════════════════════════════════════
    // ✅✅✅ SINGLE EDIT METHOD - NO DUPLICATES! ✅✅✅
    // ═══════════════════════════════════════════════════════════
    @GetMapping("/edit/{lessonId}")
    public String showEditLesson(@PathVariable Long lessonId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║  📝 SHOW EDIT LESSON FORM                                 ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("📋 Lesson ID: " + lessonId);

        Lesson lesson = lessonService.getLessonById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        System.out.println("✅ Lesson found: " + lesson.getTitle());
        System.out.println("   • Type: " + lesson.getType());

        // ✅✅✅ CHECK IF LESSON IS QUIZ TYPE ✅✅✅
        if (lesson.getType() == LessonType.QUIZ) {
            System.out.println("\n⚠️  QUIZ TYPE DETECTED - Redirecting to quiz edit page");

            // Find the quiz associated with this lesson
            Optional<Quiz> quizOptional = quizService.getQuizByLesson(lessonId);

            if (quizOptional.isPresent()) {
                Quiz quiz = quizOptional.get();
                System.out.println("✅ Quiz found: " + quiz.getTitle() + " (ID: " + quiz.getId() + ")");
                System.out.println("🔀 Redirecting to: /instructor/quiz/edit/" + quiz.getId());
                System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

                // Redirect to quiz edit page
                return "redirect:/instructor/quiz/edit/" + quiz.getId();
            } else {
                System.out.println("❌ No quiz found for this lesson!");
                System.out.println("   This is a QUIZ type lesson but has no associated quiz.");
                System.out.println("   Creating a new quiz for this lesson...");
                System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

                // Redirect to create quiz with this lesson
                redirectAttributes.addFlashAttribute("info",
                        "This quiz lesson doesn't have questions yet. Please create the quiz.");
                return "redirect:/instructor/quiz/create/" + lesson.getCourse().getId() +
                        "?lessonId=" + lessonId;
            }
        }

        // ✅ For non-QUIZ lessons, show regular edit form
        System.out.println("✅ Regular lesson type - Showing edit form");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

        model.addAttribute("lesson", lesson);
        model.addAttribute("lessonTypes", LessonType.values());

        return "lessons/edit";
    }

    // ═══════════════════════════════════════════════════════════
    // ✅ POST: UPDATE LESSON
    // ═══════════════════════════════════════════════════════════
    @PostMapping("/edit/{lessonId}")
    public String updateLesson(@PathVariable Long lessonId,
                               @RequestParam String title,
                               @RequestParam String description,
                               @RequestParam Integer duration,
                               @RequestParam LessonType type,
                               @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                               @RequestParam(value = "videoUrl", required = false) String videoUrl,
                               @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile,
                               @RequestParam(value = "pdfUrl", required = false) String pdfUrl,
                               RedirectAttributes redirectAttributes) {

        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║  📝 UPDATE LESSON ENDPOINT CALLED                         ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("📋 Lesson ID: " + lessonId);
        System.out.println("📋 Title: " + title);
        System.out.println("📋 Type: " + type);

        try {
            Lesson lesson = lessonService.updateLesson(lessonId, title, description, duration, type,
                    videoFile, videoUrl, pdfFile, pdfUrl);

            System.out.println("✅ Lesson updated successfully!");
            redirectAttributes.addFlashAttribute("success", "Lesson updated successfully!");

            return "redirect:/courses/" + lesson.getCourse().getId();

        } catch (Exception e) {
            System.err.println("❌ Error updating lesson: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error updating lesson: " + e.getMessage());
            return "redirect:/lessons/edit/" + lessonId;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ✅✅✅ POST: CREATE LESSON - MAIN METHOD ✅✅✅
    // ═══════════════════════════════════════════════════════════
    @PostMapping("/create/{courseId}")
    public String createLesson(@PathVariable Long courseId,
                               @RequestParam String title,
                               @RequestParam String description,
                               @RequestParam Integer duration,
                               @RequestParam LessonType type,
                               @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                               @RequestParam(value = "videoUrl", required = false) String videoUrl,
                               @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile,
                               @RequestParam(value = "pdfUrl", required = false) String pdfUrl,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               @RequestParam(value = "imageUrl", required = false) String imageUrl,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {

        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║  📝 CREATE LESSON ENDPOINT CALLED                         ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("📋 Course ID: " + courseId);
        System.out.println("📋 Title: " + title);
        System.out.println("📋 Type: " + type);
        System.out.println("📋 Duration: " + duration);
        System.out.println("📋 Description: " + description);

        try {
            // Get the course
            Course course = courseService.getCourseById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

            System.out.println("✅ Course found: " + course.getTitle());

            // ✅✅✅ SPECIAL HANDLING FOR QUIZ TYPE ✅✅✅
            if (type == LessonType.QUIZ) {
                System.out.println("\n🎯 QUIZ TYPE DETECTED!");
                System.out.println("   → Creating lesson first...");

                // Create the lesson for QUIZ type
                Lesson createdLesson = lessonService.createLesson(
                        title,
                        description,
                        duration,
                        type,
                        course,
                        null, null, null, null, null, null
                );

                System.out.println("✅ Quiz lesson created successfully!");
                System.out.println("   • Lesson ID: " + createdLesson.getId());
                System.out.println("   • Lesson Title: " + createdLesson.getTitle());
                System.out.println("🔀 Redirecting to quiz creation page...");

                // Redirect to quiz creation page
                redirectAttributes.addFlashAttribute("success", "Quiz lesson created! Now add questions.");
                return "redirect:/instructor/quiz/create/" + courseId + "?lessonId=" + createdLesson.getId();
            }

            // ═══════════════════════════════════════════════════════════
            // VALIDATION FOR OTHER LESSON TYPES
            // ═══════════════════════════════════════════════════════════

            // Validation for VIDEO type
            if (type == LessonType.VIDEO) {
                boolean hasVideoFile = videoFile != null && !videoFile.isEmpty();
                boolean hasVideoUrl = videoUrl != null && !videoUrl.trim().isEmpty();

                System.out.println("🎥 VIDEO validation:");
                System.out.println("   • Has video file: " + hasVideoFile);
                System.out.println("   • Has video URL: " + hasVideoUrl);

                if (!hasVideoFile && !hasVideoUrl) {
                    throw new IllegalArgumentException("Video lesson requires either a video file or video URL");
                }
                if (hasVideoFile && hasVideoUrl) {
                    throw new IllegalArgumentException("Please provide either a video file OR a video URL, not both");
                }
            }

            // Validation for PDF type
            if (type == LessonType.PDF) {
                boolean hasPdfFile = pdfFile != null && !pdfFile.isEmpty();
                boolean hasPdfUrl = pdfUrl != null && !pdfUrl.trim().isEmpty();

                System.out.println("📄 PDF validation:");
                System.out.println("   • Has PDF file: " + hasPdfFile);
                System.out.println("   • Has PDF URL: " + hasPdfUrl);

                if (!hasPdfFile && !hasPdfUrl) {
                    throw new IllegalArgumentException("PDF lesson requires either a PDF file or PDF URL");
                }
                if (hasPdfFile && hasPdfUrl) {
                    throw new IllegalArgumentException("Please provide either a PDF file OR a PDF URL, not both");
                }
            }

            // Validation for IMAGE type
            if (type == LessonType.IMAGE) {
                boolean hasImageFile = imageFile != null && !imageFile.isEmpty();
                boolean hasImageUrl = imageUrl != null && !imageUrl.trim().isEmpty();

                System.out.println("🖼️ IMAGE validation:");
                System.out.println("   • Has image file: " + hasImageFile);
                System.out.println("   • Has image URL: " + hasImageUrl);

                if (!hasImageFile && !hasImageUrl) {
                    throw new IllegalArgumentException("Image lesson requires either an image file or image URL");
                }
                if (hasImageFile && hasImageUrl) {
                    throw new IllegalArgumentException("Please provide either an image file OR an image URL, not both");
                }
            }

            // ═══════════════════════════════════════════════════════════
            // CREATE THE LESSON (for non-QUIZ types)
            // ═══════════════════════════════════════════════════════════
            System.out.println("\n💾 Creating lesson...");

            Lesson createdLesson = lessonService.createLesson(
                    title,
                    description,
                    duration,
                    type,
                    course,
                    videoFile,
                    videoUrl,
                    pdfFile,
                    pdfUrl,
                    imageFile,
                    imageUrl
            );

            System.out.println("✅ Lesson created successfully!");
            System.out.println("   • Lesson ID: " + createdLesson.getId());
            System.out.println("   • Lesson Title: " + createdLesson.getTitle());
            System.out.println("   • Lesson Type: " + createdLesson.getType());
            System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

            redirectAttributes.addFlashAttribute("success", "Lesson created successfully!");
            return "redirect:/courses/" + courseId + "?lesson=" + createdLesson.getId();

        } catch (IllegalArgumentException e) {
            System.err.println("\n❌ VALIDATION ERROR:");
            System.err.println("   • " + e.getMessage());
            System.err.println("╚═══════════════════════════════════════════════════════════╝\n");

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/lessons/create/" + courseId;

        } catch (Exception e) {
            System.err.println("\n❌ ERROR CREATING LESSON:");
            System.err.println("   • Type: " + e.getClass().getSimpleName());
            System.err.println("   • Message: " + e.getMessage());
            System.err.println("   • Stack trace:");
            e.printStackTrace();
            System.err.println("╚═══════════════════════════════════════════════════════════╝\n");

            redirectAttributes.addFlashAttribute("error", "Error creating lesson: " + e.getMessage());
            return "redirect:/lessons/create/" + courseId;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ✅✅✅ POST: DELETE LESSON ✅✅✅
    // ═══════════════════════════════════════════════════════════
    @PostMapping("/delete/{lessonId}")
    public String deleteLesson(@PathVariable Long lessonId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {

        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║  🔥 DELETE LESSON ENDPOINT CALLED                         ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("📋 Lesson ID: " + lessonId);
        System.out.println("👤 User: " + (userDetails != null ? userDetails.getUsername() : "NULL"));

        try {
            // Get lesson to find course ID
            Lesson lesson = lessonService.getLessonById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Lesson not found"));

            Long courseId = lesson.getCourse().getId();
            String lessonTitle = lesson.getTitle();

            System.out.println("✅ Lesson found: " + lessonTitle);
            System.out.println("🗑️ Deleting lesson...");

            // Delete the lesson
            lessonService.deleteLesson(lessonId);

            System.out.println("✅ LESSON DELETED SUCCESSFULLY!");
            System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

            redirectAttributes.addFlashAttribute("success", "Lesson '" + lessonTitle + "' deleted successfully!");
            return "redirect:/courses/" + courseId;

        } catch (Exception e) {
            System.err.println("\n❌ DELETE FAILED:");
            System.err.println("   • Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("╚═══════════════════════════════════════════════════════════╝\n");

            redirectAttributes.addFlashAttribute("error", "Failed to delete lesson: " + e.getMessage());
            return "redirect:/courses";
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ✅ TEST ENDPOINTS (for debugging)
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/test-delete/{lessonId}")
    @ResponseBody
    public String testDelete(@PathVariable Long lessonId) {
        return "✅ Delete endpoint is accessible! Lesson ID: " + lessonId + " | Time: " + java.time.LocalDateTime.now();
    }
}