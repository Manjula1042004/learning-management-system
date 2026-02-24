package com.lms.controller;

import com.lms.entity.User;
import com.lms.entity.Course;
import com.lms.entity.Enrollment;
import com.lms.service.UserService;
import com.lms.service.CourseService;
import com.lms.service.EnrollmentService;
import com.lms.service.PayPalService;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/enroll")
public class EnrollmentController {

    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final PayPalService payPalService;

    public EnrollmentController(UserService userService, CourseService courseService,
                                EnrollmentService enrollmentService, PayPalService payPalService) {
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.payPalService = payPalService;
    }

    @GetMapping("/{courseId}")
    public String showEnrollmentPage(@PathVariable Long courseId,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        User student = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if already enrolled
        Optional<Enrollment> existingEnrollment = enrollmentService.getEnrollment(student, course);
        if (existingEnrollment.isPresent()) {
            redirectAttributes.addFlashAttribute("info", "You are already enrolled in this course!");
            return "redirect:/courses/" + courseId;
        }

        // If it's a FREE course, enroll directly without PayPal
        if (course.getPrice() == null || course.getPrice() == 0.0) {
            try {
                enrollmentService.enrollStudent(student, course, "FREE_ENROLLMENT");
                redirectAttributes.addFlashAttribute("success", "Successfully enrolled in the free course!");
                return "redirect:/courses/" + courseId + "?enrolled=true";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Enrollment failed: " + e.getMessage());
                return "redirect:/courses/" + courseId;
            }
        }

        // For paid courses, show checkout page
        model.addAttribute("user", student);
        model.addAttribute("course", course);
        model.addAttribute("isFree", false);

        return "enrollment/checkout";
    }

    @PostMapping("/{courseId}")
    public String processEnrollment(@PathVariable Long courseId,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    @RequestParam(required = false) String paymentMethod,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        User student = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Double-check if already enrolled
        if (enrollmentService.isStudentEnrolled(student, course)) {
            redirectAttributes.addFlashAttribute("info", "You are already enrolled in this course!");
            return "redirect:/courses/" + courseId;
        }

        // For free courses, enroll directly
        if (course.getPrice() == null || course.getPrice() == 0.0) {
            try {
                enrollmentService.enrollStudent(student, course, "FREE_ENROLLMENT");
                redirectAttributes.addFlashAttribute("success", "Successfully enrolled in the free course!");
                return "redirect:/courses/" + courseId + "?enrolled=true";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Enrollment failed: " + e.getMessage());
                return "redirect:/courses/" + courseId;
            }
        }

        // For paid courses, redirect to PayPal
        try {
            String cancelUrl = "http://localhost:8080/enroll/" + courseId + "/cancel";
            String successUrl = "http://localhost:8080/enroll/" + courseId + "/success";

            Payment payment = payPalService.createPayment(
                    course.getPrice(),
                    "USD",
                    "paypal",
                    "sale",
                    "Enrollment in: " + course.getTitle(),
                    cancelUrl,
                    successUrl
            );

            // Redirect to PayPal approval URL
            for (com.paypal.api.payments.Links link : payment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    return "redirect:" + link.getHref();
                }
            }
        } catch (PayPalRESTException e) {
            model.addAttribute("error", "Payment failed: " + e.getMessage());
            return "redirect:/enroll/" + courseId + "?error=payment_failed";
        }

        return "redirect:/enroll/" + courseId + "?error=unknown";
    }

    @GetMapping("/{courseId}/success")
    public String enrollmentSuccess(@PathVariable Long courseId,
                                    @RequestParam String paymentId,
                                    @RequestParam String PayerID,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        User student = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        try {
            // Execute payment
            Payment payment = payPalService.executePayment(paymentId, PayerID);

            if (payment.getState().equals("approved")) {
                // Enroll student
                enrollmentService.enrollStudent(student, course, paymentId);
                redirectAttributes.addFlashAttribute("success",
                        "Payment successful! You are now enrolled in " + course.getTitle());
                return "redirect:/courses/" + courseId + "?enrolled=true";
            } else {
                redirectAttributes.addFlashAttribute("error", "Payment was not approved");
                return "redirect:/courses/" + courseId;
            }
        } catch (PayPalRESTException e) {
            redirectAttributes.addFlashAttribute("error", "Payment execution failed: " + e.getMessage());
            return "redirect:/courses/" + courseId;
        }
    }

    @GetMapping("/{courseId}/cancel")
    public String enrollmentCancel(@PathVariable Long courseId,
                                   RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("info", "Payment was cancelled");
        return "redirect:/courses/" + courseId + "?enrollment_cancelled=true";
    }
}