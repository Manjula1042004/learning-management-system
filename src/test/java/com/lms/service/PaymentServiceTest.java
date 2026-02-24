package com.lms.service;

import com.lms.entity.*;
import com.lms.repository.PaymentRepository;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private User student;
    private User instructor;
    private Course course;
    private Payment pendingPayment;
    private Payment completedPayment;

    @BeforeEach
    void setUp() {
        student = TestDataFactory.createStudent(1L);
        instructor = TestDataFactory.createInstructor(2L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);

        pendingPayment = TestDataFactory.createPayment(1L, "PAY-001", student, course, PaymentStatus.PENDING);
        completedPayment = TestDataFactory.createPayment(2L, "PAY-002", student, course, PaymentStatus.COMPLETED);
    }

    @Test
    void createPayment_ShouldCreateNewPayment() {
        String paymentId = "PAY-123";
        Double amount = 99.99;

        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment saved = i.getArgument(0);
            saved.setId(4L);
            return saved;
        });

        Payment result = paymentService.createPayment(paymentId, amount, "USD", "Test", student, course);

        assertThat(result).isNotNull();
        assertThat(result.getPaymentId()).isEqualTo(paymentId);
        assertThat(result.getAmount()).isEqualTo(amount);
    }

    @Test
    void updatePaymentStatus_ShouldUpdateStatus() {
        String paymentId = "PAY-001";
        when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment result = paymentService.updatePaymentStatus(paymentId, PaymentStatus.COMPLETED);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void getTotalRevenue_ShouldSumCompletedPayments() {
        completedPayment.setAmount(99.99);
        Payment anotherCompleted = TestDataFactory.createPayment(4L, "PAY-004", student, course, PaymentStatus.COMPLETED);
        anotherCompleted.setAmount(149.99);
        List<Payment> completedPayments = Arrays.asList(completedPayment, anotherCompleted);

        when(paymentRepository.findByStatus(PaymentStatus.COMPLETED)).thenReturn(completedPayments);

        Double result = paymentService.getTotalRevenue();

        // Fix: Use within() with delta for floating point comparison
        assertThat(result).isCloseTo(249.98, within(0.001));
    }

    @Test
    void getRevenueByInstructor_ShouldSumInstructorPayments() {
        completedPayment.setAmount(99.99);
        Payment anotherCompleted = TestDataFactory.createPayment(4L, "PAY-004", student, course, PaymentStatus.COMPLETED);
        anotherCompleted.setAmount(149.99);
        List<Payment> completedPayments = Arrays.asList(completedPayment, anotherCompleted);

        when(paymentRepository.findByStatus(PaymentStatus.COMPLETED)).thenReturn(completedPayments);

        Double result = paymentService.getRevenueByInstructor(instructor);

        // Fix: Use within() with delta for floating point comparison
        assertThat(result).isCloseTo(249.98, within(0.001));
    }
}