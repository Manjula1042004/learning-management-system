package com.lms.controller;

import com.lms.entity.User;
import com.lms.service.UserService;
import com.lms.service.CourseService;
import com.lms.service.EnrollmentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class DashboardController {

    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    public DashboardController(UserService userService, CourseService courseService,
                               EnrollmentService enrollmentService) {
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Redirect to role-specific dashboard
        return switch (user.getRole()) {
            case ADMIN -> "redirect:/admin";
            case INSTRUCTOR -> "redirect:/instructor";
            case STUDENT -> "redirect:/student";
        };
    }

    // REMOVED the adminDashboard method from here - it's in AdminController
    // REMOVED the instructorDashboard method from here - it's in InstructorController
    // REMOVED the studentDashboard method from here - it's in StudentController
}