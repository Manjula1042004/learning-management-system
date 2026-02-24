package com.lms.controller;

import com.lms.entity.User;
import com.lms.entity.Course;
import com.lms.entity.Enrollment;
import com.lms.service.UserService;
import com.lms.service.CourseService;
import com.lms.service.EnrollmentService;
import com.lms.service.LessonService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Controller
@RequestMapping("/instructor")
public class InstructorController {

    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final LessonService lessonService;

    public InstructorController(UserService userService,
                                CourseService courseService,
                                EnrollmentService enrollmentService,
                                LessonService lessonService) {
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.lessonService = lessonService;
    }

    @GetMapping
    public String instructorDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User instructor = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", instructor);

        // Get ONLY instructor's OWN courses
        List<Course> myCourses = courseService.getCoursesByInstructor(instructor);

        // Calculate stats based on instructor's OWN courses
        Map<String, Object> stats = new HashMap<>();
        stats.put("courseCount", myCourses.size());

        int totalStudents = 0;
        for (Course course : myCourses) {
            totalStudents += enrollmentService.getEnrollmentsByCourse(course).size();
        }
        stats.put("studentCount", totalStudents);

        Double totalRevenue = calculateInstructorRevenue(instructor);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);

        model.addAttribute("stats", stats);
        model.addAttribute("myCourses", myCourses);
        model.addAttribute("allCourses", new ArrayList<>());

        return "instructor/dashboard";
    }

    @GetMapping("/my-courses")
    public String myCourses(@RequestParam(required = false) String search,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        // Add flash attributes from redirect if any
        if (redirectAttributes.getFlashAttributes().containsKey("success")) {
            model.addAttribute("success", redirectAttributes.getFlashAttributes().get("success"));
        }
        if (redirectAttributes.getFlashAttributes().containsKey("error")) {
            model.addAttribute("error", redirectAttributes.getFlashAttributes().get("error"));
        }

        User instructor = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get ONLY courses owned by this instructor
        List<Course> myCourses = courseService.getCoursesByInstructor(instructor);

        // REMOVE DUPLICATES by title
        List<Course> uniqueCourses = myCourses.stream()
                .collect(Collectors.toMap(
                        Course::getTitle,
                        course -> course,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .collect(Collectors.toList());

        // If search is provided, filter courses
        if (search != null && !search.trim().isEmpty()) {
            uniqueCourses = uniqueCourses.stream()
                    .filter(course -> course.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                            course.getDescription().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("user", instructor);
        model.addAttribute("courses", uniqueCourses);
        model.addAttribute("search", search);
        model.addAttribute("isMyCoursesView", true);

        return "instructor/my-courses";
    }

    // FIXED: Delete Course method with proper redirect
    @PostMapping("/courses/{id}/delete")
    public String deleteCourse(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User instructor = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Get course to check ownership and get title for success message
            Course course = courseService.getCourseById(id)
                    .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));

            // Verify instructor owns this course
            if (!instructor.isAdmin() && !course.getInstructor().getId().equals(instructor.getId())) {
                redirectAttributes.addFlashAttribute("error",
                        "You don't have permission to delete this course");
                return "redirect:/instructor/my-courses";
            }

            String courseTitle = course.getTitle();

            // Delete the course
            courseService.deleteCourse(id, instructor);

            // Add success message
            redirectAttributes.addFlashAttribute("success",
                    "Course '" + courseTitle + "' has been successfully deleted!");

            System.out.println("✅ Course deleted successfully: " + courseTitle);

            // Redirect to my-courses page
            return "redirect:/instructor/my-courses";

        } catch (Exception e) {
            System.err.println("❌ Error deleting course: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("error",
                    "Error deleting course: " + e.getMessage());

            // Still redirect to my-courses page even on error
            return "redirect:/instructor/my-courses";
        }
    }

    @GetMapping("/enrolled-students")
    public String viewEnrolledStudents(@RequestParam(required = false) Long courseId,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       Model model) {
        User instructor = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!instructor.isInstructor()) {
            return "redirect:/dashboard";
        }

        // Get instructor's courses
        List<Course> myCourses = courseService.getCoursesByInstructor(instructor);

        // Get enrollments
        List<Enrollment> enrollments;
        Course selectedCourse = null;

        if (courseId != null) {
            // Get enrollments for specific course
            selectedCourse = courseService.getCourseById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            // Verify instructor owns this course
            if (!selectedCourse.getInstructor().getId().equals(instructor.getId())) {
                model.addAttribute("error", "You don't have permission to view this course");
                return "redirect:/instructor/enrolled-students";
            }

            enrollments = enrollmentService.getEnrollmentsByCourse(selectedCourse);
        } else {
            // Get all enrollments for all instructor's courses
            enrollments = enrollmentService.getEnrollmentsByInstructor(instructor);
        }

        model.addAttribute("user", instructor);
        model.addAttribute("myCourses", myCourses);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedCourse", selectedCourse);

        return "instructor/enrolled-students";
    }

    @GetMapping("/analytics")
    public String showAnalytics(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User instructor = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", instructor);
        return "instructor/analytics";
    }

    @PostMapping("/lessons/{id}/delete")
    public String deleteLesson(@PathVariable Long id,
                               @RequestParam Long courseId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User instructor = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            lessonService.deleteLesson(id);

            redirectAttributes.addFlashAttribute("success", "Lesson deleted successfully!");
            return "redirect:/courses/" + courseId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error deleting lesson: " + e.getMessage());
            return "redirect:/courses/" + courseId;
        }
    }

    private Double calculateInstructorRevenue(User instructor) {
        List<Course> courses = courseService.getCoursesByInstructor(instructor);
        double total = 0.0;
        for (Course course : courses) {
            int students = enrollmentService.getEnrollmentsByCourse(course).size();
            total += (course.getPrice() * students * 0.7);
        }
        return total;
    }
}