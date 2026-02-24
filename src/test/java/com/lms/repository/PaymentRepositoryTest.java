package com.lms.repository;

import com.lms.entity.*;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    private User student;
    private User instructor;
    private Course course;
    private Payment payment1;
    private Payment payment2;

    @BeforeEach
    void setUp() {
        // Clear repositories in correct order
        paymentRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // Create users
        student = userRepository.save(TestDataFactory.createStudent(null));
        instructor = userRepository.save(TestDataFactory.createInstructor(null));

        // Create course
        course = TestDataFactory.createCourse(null, "Test Course", instructor);
        course.setPrice(99.99);
        course = courseRepository.save(course);

        // Create payments
        payment1 = TestDataFactory.createPayment(null, "PAY-001", student, course, PaymentStatus.COMPLETED);
        payment1.setAmount(99.99);
        payment1 = paymentRepository.save(payment1);

        payment2 = TestDataFactory.createPayment(null, "PAY-002", student, course, PaymentStatus.PENDING);
        payment2.setAmount(99.99);
        payment2 = paymentRepository.save(payment2);
    }

    @Test
    void testFindByPaymentId() {
        Optional<Payment> found = paymentRepository.findByPaymentId("PAY-001");
        assertThat(found).isPresent();
        assertThat(found.get().getPaymentId()).isEqualTo("PAY-001");
    }

    @Test
    void testFindByStatus() {
        List<Payment> completed = paymentRepository.findByStatus(PaymentStatus.COMPLETED);
        List<Payment> pending = paymentRepository.findByStatus(PaymentStatus.PENDING);

        assertThat(completed).hasSize(1);
        assertThat(pending).hasSize(1);
    }

    @Test
    void testFindByUser() {
        List<Payment> payments = paymentRepository.findByUser(student);
        assertThat(payments).hasSize(2);
    }

    @Test
    void testExistsByPaymentId() {
        assertThat(paymentRepository.existsByPaymentId("PAY-001")).isTrue();
        assertThat(paymentRepository.existsByPaymentId("PAY-999")).isFalse();
    }

    @Test
    void testCountCompletedPayments() {
        Long count = paymentRepository.countCompletedPayments();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testGetTotalRevenue() {
        Double total = paymentRepository.getTotalRevenue();
        assertThat(total).isEqualTo(99.99);
    }
}