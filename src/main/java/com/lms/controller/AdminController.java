package com.lms.controller;

import com.lms.entity.User;
import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.entity.CourseStatus;
import com.lms.service.UserService;
import com.lms.service.CourseService;
import com.lms.service.LessonService;
import com.lms.service.PaymentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final CourseService courseService;
    private final LessonService lessonService;
    private final PaymentService paymentService;

    public AdminController(UserService userService,
                           CourseService courseService,
                           LessonService lessonService,
                           PaymentService paymentService) {
        this.userService = userService;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public String adminDashboard(@RequestParam(defaultValue = "users") String tab,
                                 @RequestParam(required = false) String search,
                                 @RequestParam(required = false) String courseSearch,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        User admin = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!admin.isAdmin()) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", admin);
        model.addAttribute("activeTab", tab);

        // Load data based on active tab
        switch (tab) {
            case "users":
                if (search != null && !search.trim().isEmpty()) {
                    var allUsers = userService.getAllUsers();
                    var filteredUsers = allUsers.stream()
                            .filter(u -> u.getUsername().toLowerCase().contains(search.toLowerCase()) ||
                                    u.getEmail().toLowerCase().contains(search.toLowerCase()))
                            .toList();
                    model.addAttribute("users", filteredUsers);
                } else {
                    model.addAttribute("users", userService.getAllUsers());
                }
                break;
            case "courses":
                if (courseSearch != null && !courseSearch.trim().isEmpty()) {
                    model.addAttribute("allCourses", courseService.searchCourses(courseSearch));
                } else {
                    model.addAttribute("allCourses", courseService.getAllCourses());
                }
                break;
            case "pending":
                model.addAttribute("pendingCourses", courseService.getPendingCourses());
                break;
            case "lessons":
                model.addAttribute("allLessons", lessonService.getAllLessons());
                break;
        }

        // Add statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.getAllUsers().size());
        stats.put("totalCourses", courseService.getAllCourses().size());
        stats.put("pendingCourses", courseService.getPendingCourses().size());
        stats.put("totalLessons", lessonService.getAllLessons().size());
        stats.put("totalAdmins", userService.getUsersByRole(com.lms.entity.Role.ADMIN).size());
        stats.put("totalInstructors", userService.getUsersByRole(com.lms.entity.Role.INSTRUCTOR).size());
        stats.put("totalStudents", userService.getUsersByRole(com.lms.entity.Role.STUDENT).size());

        try {
            Double totalRevenue = paymentService.getTotalRevenue();
            stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        } catch (Exception e) {
            stats.put("totalRevenue", 0.0);
        }

        model.addAttribute("stats", stats);
        model.addAttribute("searchTerm", search);
        model.addAttribute("courseSearchTerm", courseSearch);

        return "admin/dashboard";
    }

    // ✅ Approve Course
    @PostMapping("/courses/{id}/approve")
    public String approveCourse(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            courseService.updateCourseStatus(id, CourseStatus.APPROVED);
            redirectAttributes.addFlashAttribute("success", "Course approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error approving course: " + e.getMessage());
        }
        return "redirect:/admin?tab=pending";
    }

    // ✅ Reject Course
    @PostMapping("/courses/{id}/reject")
    public String rejectCourse(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            courseService.updateCourseStatus(id, CourseStatus.REJECTED);
            redirectAttributes.addFlashAttribute("success", "Course rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error rejecting course: " + e.getMessage());
        }
        return "redirect:/admin?tab=pending";
    }

    // ✅ Delete Course (from any tab)


    // ✅ NEW: Delete Lesson
    @PostMapping("/lessons/{id}/delete")
    public String deleteLesson(@PathVariable Long id,
                               @RequestParam(required = false) String fromTab,
                               RedirectAttributes redirectAttributes) {
        try {
            Lesson lesson = lessonService.getLessonById(id)
                    .orElseThrow(() -> new RuntimeException("Lesson not found"));
            Long courseId = lesson.getCourse().getId();

            lessonService.deleteLesson(id);
            redirectAttributes.addFlashAttribute("success", "Lesson deleted successfully!");

            // Redirect back to the same tab
            if (fromTab != null && !fromTab.isEmpty()) {
                return "redirect:/admin?tab=" + fromTab;
            }
            return "redirect:/admin?tab=lessons";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting lesson: " + e.getMessage());
            return "redirect:/admin?tab=lessons";
        }
    }

    // ✅ NEW: View Lesson Details
    @GetMapping("/lessons/{id}")
    public String viewLessonDetails(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {
        User admin = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Lesson lesson = lessonService.getLessonById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        model.addAttribute("user", admin);
        model.addAttribute("lesson", lesson);
        model.addAttribute("isAdminView", true);

        return "lessons/view-admin";
    }

    // ✅ View Course Details
    @GetMapping("/courses/{id}")
    public String viewCourseDetails(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {
        User admin = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseService.getCourseById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        model.addAttribute("user", admin);
        model.addAttribute("course", course);
        model.addAttribute("isAdminView", true);

        return "courses/view";
    }

    // ✅ User Management Methods
    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User userToEdit = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("userToEdit", userToEdit);
        model.addAttribute("roles", com.lms.entity.Role.values());
        return "admin/edit-user";
    }

    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @RequestParam String username,
                             @RequestParam String email,
                             @RequestParam com.lms.entity.Role role,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, username, email, role);
            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
        }
        return "redirect:/admin?tab=users";
    }



    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setEnabled(!user.isEnabled());
            userService.updateUserStatus(id, user.isEnabled());

            String status = user.isEnabled() ? "enabled" : "disabled";
            redirectAttributes.addFlashAttribute("success",
                    "User '" + user.getUsername() + "' " + status + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user status: " + e.getMessage());
        }
        return "redirect:/admin?tab=users";
    }

    // In AdminController.java - update the deleteUser method:
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            User currentAdmin = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (currentAdmin.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "You cannot delete your own account!");
                return "redirect:/admin?tab=users";
            }

            User userToDelete = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Use the new safe delete method
            userService.deleteUser(id);

            redirectAttributes.addFlashAttribute("success",
                    "User '" + userToDelete.getUsername() + "' deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error deleting user: " + e.getMessage());
            // Log the full error for debugging
            System.err.println("Delete user error: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin?tab=users";
    }
    // Find this method (around line 146):


    // Change it to:


    // In AdminController.java - Update the deleteCourse method
    @PostMapping("/courses/{id}/delete")
    public String deleteCourse(@PathVariable Long id,
                               @RequestParam(required = false) String fromTab,
                               @AuthenticationPrincipal UserDetails userDetails, // ADD THIS
                               RedirectAttributes redirectAttributes) {
        try {
            // Get current user
            User admin = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Use the simple delete method (or the one with user parameter if you prefer)
            courseService.deleteCourse(id); // Using simple version

            redirectAttributes.addFlashAttribute("success", "Course deleted successfully!");

            // Redirect back to the same tab
            if (fromTab != null && !fromTab.isEmpty()) {
                return "redirect:/admin?tab=" + fromTab;
            }
            return "redirect:/admin?tab=courses";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting course: " + e.getMessage());
            return "redirect:/admin?tab=courses";
        }
    }
}