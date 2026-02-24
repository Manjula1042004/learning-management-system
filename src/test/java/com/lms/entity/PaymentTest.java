package com.lms.entity;

import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    private User student;
    private User instructor;
    private Course course;
    private Payment payment;

    @BeforeEach
    void setUp() {
        student = TestDataFactory.createStudent(1L);
        instructor = TestDataFactory.createInstructor(2L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);

        payment = new Payment(
                "PAY-123",
                PaymentStatus.PENDING,
                99.99,
                "USD",
                "Test payment",
                student,
                course
        );
        payment.setId(1L);
    }

    @Test
    void constructor_ShouldCreatePayment() {
        Payment newPayment = new Payment(
                "PAY-456",
                PaymentStatus.COMPLETED,
                149.99,
                "EUR",
                "Another payment",
                student,
                course
        );

        assertThat(newPayment.getPaymentId()).isEqualTo("PAY-456");
        assertThat(newPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(newPayment.getAmount()).isEqualTo(149.99);
        assertThat(newPayment.getCurrency()).isEqualTo("EUR");
        assertThat(newPayment.getDescription()).isEqualTo("Another payment");
        assertThat(newPayment.getUser()).isEqualTo(student);
        assertThat(newPayment.getCourse()).isEqualTo(course);
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        payment.setId(2L);
        payment.setPaymentId("PAY-789");
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setAmount(199.99);
        payment.setCurrency("GBP");
        payment.setDescription("Updated payment");

        LocalDateTime now = LocalDateTime.now();
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);

        assertThat(payment.getId()).isEqualTo(2L);
        assertThat(payment.getPaymentId()).isEqualTo("PAY-789");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.getAmount()).isEqualTo(199.99);
        assertThat(payment.getCurrency()).isEqualTo("GBP");
        assertThat(payment.getDescription()).isEqualTo("Updated payment");
        assertThat(payment.getCreatedAt()).isEqualTo(now);
        assertThat(payment.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void user_ShouldBeAccessible() {
        assertThat(payment.getUser()).isEqualTo(student);

        User newStudent = TestDataFactory.createStudent(3L);
        payment.setUser(newStudent);
        assertThat(payment.getUser()).isEqualTo(newStudent);
    }

    @Test
    void course_ShouldBeAccessible() {
        assertThat(payment.getCourse()).isEqualTo(course);

        Course newCourse = TestDataFactory.createCourse(2L, "New Course", instructor);
        payment.setCourse(newCourse);
        assertThat(payment.getCourse()).isEqualTo(newCourse);
    }

    @Test
    void isCompleted_ShouldReturnTrue_WhenStatusIsCompleted() {
        payment.setStatus(PaymentStatus.COMPLETED);
        assertThat(payment.isCompleted()).isTrue();

        payment.setStatus(PaymentStatus.PENDING);
        assertThat(payment.isCompleted()).isFalse();
    }

    @Test
    void isPending_ShouldReturnTrue_WhenStatusIsPending() {
        payment.setStatus(PaymentStatus.PENDING);
        assertThat(payment.isPending()).isTrue();

        payment.setStatus(PaymentStatus.COMPLETED);
        assertThat(payment.isPending()).isFalse();
    }

    @Test
    void isFailed_ShouldReturnTrue_WhenStatusIsFailed() {
        payment.setStatus(PaymentStatus.FAILED);
        assertThat(payment.isFailed()).isTrue();

        payment.setStatus(PaymentStatus.COMPLETED);
        assertThat(payment.isFailed()).isFalse();
    }

    @Test
    void prePersist_ShouldSetCreatedAndUpdatedAt() {
        Payment newPayment = new Payment();
        newPayment.onCreate();

        assertThat(newPayment.getCreatedAt()).isNotNull();
        assertThat(newPayment.getUpdatedAt()).isNotNull();
    }

    @Test
    void preUpdate_ShouldUpdateUpdatedAt() {
        LocalDateTime oldUpdatedAt = payment.getUpdatedAt();
        payment.onUpdate();

        assertThat(payment.getUpdatedAt()).isNotEqualTo(oldUpdatedAt);
        assertThat(payment.getUpdatedAt()).isAfterOrEqualTo(oldUpdatedAt);
    }

    @Test
    void toString_ShouldContainImportantFields() {
        String toString = payment.toString();

        assertThat(toString).contains("id=1");
        assertThat(toString).contains("paymentId=PAY-123");
        assertThat(toString).contains("status=PENDING");
        assertThat(toString).contains("amount=99.99");
        assertThat(toString).contains("currency=USD");
    }
}