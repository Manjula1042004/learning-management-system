package com.lms.controller;

import com.lms.entity.*;
import com.lms.service.*;
import com.lms.testutil.TestDataFactory;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnrollmentController.class)
@WithMockUser(username = "student1", roles = "STUDENT")
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private PayPalService payPalService;

    private User student;
    private User instructor;
    private Course freeCourse;
    private Course paidCourse;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        student = TestDataFactory.createStudent(1L);
        instructor = TestDataFactory.createInstructor(2L);

        freeCourse = TestDataFactory.createCourse(1L, "Free Course", instructor);
        freeCourse.setPrice(0.0);

        paidCourse = TestDataFactory.createCourse(2L, "Paid Course", instructor);
        paidCourse.setPrice(99.99);

        enrollment = TestDataFactory.createEnrollment(1L, student, paidCourse);
    }

    @Test
    void showEnrollmentPage_ShouldShowCheckoutPage_WhenNotEnrolled() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(freeCourse));
        when(enrollmentService.getEnrollment(student, freeCourse)).thenReturn(Optional.empty());

        mockMvc.perform(get("/enroll/{courseId}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("enrollment/checkout"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("course"));
    }

    @Test
    void showEnrollmentPage_ShouldRedirect_WhenAlreadyEnrolled() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(freeCourse));
        when(enrollmentService.getEnrollment(student, freeCourse)).thenReturn(Optional.of(enrollment));

        mockMvc.perform(get("/enroll/{courseId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1"));
    }

    @Test
    void processEnrollment_ForFreeCourse_ShouldEnrollDirectly() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(freeCourse));
        when(enrollmentService.enrollStudent(student, freeCourse, "FREE_ENROLLMENT")).thenReturn(enrollment);

        mockMvc.perform(post("/enroll/{courseId}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1?enrolled=true"));

        verify(enrollmentService).enrollStudent(student, freeCourse, "FREE_ENROLLMENT");
    }

    @Test
    void processEnrollment_ForPaidCourse_ShouldRedirectToPayPal() throws Exception {
        Payment mockPayment = new Payment();
        Links approvalLink = new Links();
        approvalLink.setRel("approval_url");
        approvalLink.setHref("https://paypal.com/approve");
        mockPayment.setLinks(Arrays.asList(approvalLink));

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(courseService.getCourseById(2L)).thenReturn(Optional.of(paidCourse));
        when(payPalService.createPayment(anyDouble(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString())).thenReturn(mockPayment);

        mockMvc.perform(post("/enroll/{courseId}", 2L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://paypal.com/approve"));
    }

    @Test
    void processEnrollment_ShouldHandlePayPalError() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(courseService.getCourseById(2L)).thenReturn(Optional.of(paidCourse));
        when(payPalService.createPayment(anyDouble(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString())).thenThrow(new PayPalRESTException("Payment failed"));

        mockMvc.perform(post("/enroll/{courseId}", 2L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/enroll/2?error=payment_failed"));
    }

    @Test
    void enrollmentSuccess_ShouldCompleteEnrollment_WhenPaymentApproved() throws Exception {
        Payment mockPayment = new Payment();
        mockPayment.setState("approved");

        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(courseService.getCourseById(2L)).thenReturn(Optional.of(paidCourse));
        when(payPalService.executePayment(anyString(), anyString())).thenReturn(mockPayment);
        when(enrollmentService.enrollStudent(student, paidCourse, "PAY-123")).thenReturn(enrollment);

        mockMvc.perform(get("/enroll/{courseId}/success", 2L)
                        .param("paymentId", "PAY-123")
                        .param("PayerID", "PAYER-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/2?enrolled=true"));

        verify(enrollmentService).enrollStudent(student, paidCourse, "PAY-123");
    }

    @Test
    void enrollmentSuccess_ShouldRedirectToError_WhenPaymentFailed() throws Exception {
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(student));
        when(courseService.getCourseById(2L)).thenReturn(Optional.of(paidCourse));
        when(payPalService.executePayment(anyString(), anyString())).thenThrow(new PayPalRESTException("Execution failed"));

        mockMvc.perform(get("/enroll/{courseId}/success", 2L)
                        .param("paymentId", "PAY-123")
                        .param("PayerID", "PAYER-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/2"));
    }

    @Test
    void enrollmentCancel_ShouldRedirectToCourse() throws Exception {
        mockMvc.perform(get("/enroll/{courseId}/cancel", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/1?enrollment_cancelled=true"));
    }
}