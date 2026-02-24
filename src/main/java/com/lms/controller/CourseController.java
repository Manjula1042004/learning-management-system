package com.lms.controller;

import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.entity.CourseStatus;
import com.lms.entity.User;
import com.lms.service.CourseService;
import com.lms.service.UserService;
import com.lms.service.LessonService;
import com.lms.service.EnrollmentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;

    public CourseController(CourseService courseService, UserService userService,
                            LessonService lessonService, EnrollmentService enrollmentService) {
        this.courseService = courseService;
        this.userService = userService;
        this.lessonService = lessonService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public String listCourses(@RequestParam(required = false) String search,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Course> courses;
        if (user.getRole().name().equals("ADMIN")) {
            courses = courseService.getAllCourses();
        } else {
            courses = courseService.getApprovedCourses();
        }

        // Apply search filter if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            courses = courses.stream()
                    .filter(c -> c.getTitle().toLowerCase().contains(searchLower) ||
                            c.getDescription().toLowerCase().contains(searchLower))
                    .toList();
        }

        model.addAttribute("courses", courses);
        model.addAttribute("user", user);
        model.addAttribute("search", search);
        return "courses/list";
    }

    @GetMapping("/create")
    public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("course", new Course());
        return "courses/create";
    }

    @PostMapping("/create")
    public String createCourse(@RequestParam String title,
                               @RequestParam String description,
                               @RequestParam Double price,
                               @RequestParam("thumbnail") MultipartFile thumbnail,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User instructor = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Course course = courseService.createCourse(title, description, price, instructor, thumbnail);
            redirectAttributes.addFlashAttribute("success", "Course created successfully!");
            return "redirect:/courses/" + course.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating course: " + e.getMessage());
            return "redirect:/courses/create";
        }
    }

    // FIXED: viewCourse method with proper permissions for instructors
    @GetMapping("/{id}")
    public String viewCourse(@PathVariable Long id,
                             @RequestParam(required = false) Long lesson,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseService.getCourseById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if user is the instructor of this course OR admin
        boolean isInstructor = user.isInstructor() && course.getInstructor().getId().equals(user.getId());
        boolean isAdmin = user.isAdmin();
        boolean isOwner = isAdmin || isInstructor;

        // Check if student is enrolled
        boolean enrolled = false;
        if (user.isStudent()) {
            enrolled = enrollmentService.isStudentEnrolled(user, course);
        }

        // IMPORTANT: Determine if user can view lessons
        // Instructors can ALWAYS view their own course lessons
        // Admins can ALWAYS view any course lessons
        // Students can ONLY view if enrolled
        boolean canViewLessons = isOwner || enrolled;

        // Get selected lesson if any
        Lesson selectedLesson = null;
        if (lesson != null) {
            selectedLesson = lessonService.getLessonById(lesson).orElse(null);

            // SECURITY CHECK: If user tries to access a lesson without permission
            if (!canViewLessons && selectedLesson != null) {
                if (user.isStudent()) {
                    redirectAttributes.addFlashAttribute("error",
                            "You must enroll in this course to view lessons.");
                } else {
                    redirectAttributes.addFlashAttribute("error",
                            "You don't have permission to view this lesson.");
                }
                return "redirect:/courses/" + id;
            }
        }

        // Get lessons for this course
        List<Lesson> lessons = lessonService.getLessonsByCourse(course);

        model.addAttribute("user", user);
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        model.addAttribute("selectedLesson", selectedLesson);
        model.addAttribute("enrolled", enrolled);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isInstructor", user.isInstructor());
        model.addAttribute("canViewLessons", canViewLessons); // Add this flag for template

        // Add flag for free course
        model.addAttribute("isFree", course.getPrice() == null || course.getPrice() == 0.0);

        return "courses/view";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Course course = courseService.getCourseById(id)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            // Check if user has permission to edit
            if (!user.isAdmin() && !course.getInstructor().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this course");
                return "redirect:/courses/" + id;
            }

            model.addAttribute("user", user);
            model.addAttribute("course", course);
            return "courses/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading course: " + e.getMessage());
            return "redirect:/courses";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateCourse(@PathVariable Long id,
                               @RequestParam String title,
                               @RequestParam String description,
                               @RequestParam Double price,
                               @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Course course = courseService.getCourseById(id)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            // Check if user has permission to edit
            if (!user.isAdmin() && !course.getInstructor().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this course");
                return "redirect:/courses/" + id;
            }

            Course updatedCourse = courseService.updateCourse(id, title, description, price, thumbnail);
            redirectAttributes.addFlashAttribute("success", "Course updated successfully!");
            return "redirect:/courses/" + updatedCourse.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating course: " + e.getMessage());
            return "redirect:/courses/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCourse(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {

        System.out.println("\n╔═══════════════════════════════════════════════════╗");
        System.out.println("║       🔥 DELETE COURSE CONTROLLER CALLED         ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println("📋 Path: " + request.getRequestURI());
        System.out.println("📋 Method: " + request.getMethod());
        System.out.println("📋 Course ID: " + id);
        System.out.println("👤 User: " + (userDetails != null ? userDetails.getUsername() : "null"));

        try {
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Course course = courseService.getCourseById(id)
                    .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));

            System.out.println("✅ User authenticated: " + user.getUsername());
            System.out.println("✅ User role: " + user.getRole());
            System.out.println("📚 Course found: " + course.getTitle());

            // Check if user has permission to delete
            if (!user.isAdmin() && !course.getInstructor().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this course");
                return "redirect:/courses/" + id;
            }

            // Use the detailed delete method with permission check
            courseService.deleteCourse(id, user);

            String redirectUrl;
            if (user.isAdmin()) {
                redirectUrl = "redirect:/admin?tab=courses";
            } else {
                redirectUrl = "redirect:/instructor/my-courses";
            }

            redirectAttributes.addFlashAttribute("success",
                    "Course '" + course.getTitle() + "' deleted successfully!");
            System.out.println("✅ Course deleted successfully!");
            return redirectUrl;

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error deleting course: " + e.getMessage());
            return "redirect:/courses";
        }
    }

    // Test endpoint for debugging
    @GetMapping("/{id}/delete-now")
    @ResponseBody
    public String testDeleteNow(@PathVariable Long id) {
        return "Test delete endpoint for course ID: " + id + " - Use POST method for actual deletion.";
    }

    @PostMapping("/{id}/approve")
    public String approveCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            courseService.updateCourseStatus(id, CourseStatus.APPROVED);
            redirectAttributes.addFlashAttribute("success", "Course approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error approving course: " + e.getMessage());
        }
        return "redirect:/admin?tab=pending";
    }

    @PostMapping("/{id}/reject")
    public String rejectCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            courseService.updateCourseStatus(id, CourseStatus.REJECTED);
            redirectAttributes.addFlashAttribute("success", "Course rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error rejecting course: " + e.getMessage());
        }
        return "redirect:/admin?tab=pending";
    }
}