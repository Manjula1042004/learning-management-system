package com.lms.service;

import com.lms.entity.*;
import com.lms.repository.*;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private LessonProgressRepository lessonProgressRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User student;
    private User instructor;
    private Course course;
    private Lesson lesson1;
    private Lesson lesson2;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        student = TestDataFactory.createStudent(1L);
        instructor = TestDataFactory.createInstructor(2L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);

        lesson1 = TestDataFactory.createLesson(1L, "Lesson 1", course, LessonType.VIDEO);
        lesson2 = TestDataFactory.createLesson(2L, "Lesson 2", course, LessonType.PDF);
        course.setLessons(new java.util.HashSet<>(Arrays.asList(lesson1, lesson2)));

        enrollment = TestDataFactory.createEnrollment(1L, student, course);
    }

    @Test
    void enrollStudent_ShouldCreateEnrollment_WhenNotEnrolled() {
        when(enrollmentRepository.existsByStudentAndCourse(student, course)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> {
            Enrollment saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        Enrollment result = enrollmentService.enrollStudent(student, course, "FREE");

        assertThat(result).isNotNull();
        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(result.getCourse()).isEqualTo(course);
        assertThat(result.getPaymentId()).isEqualTo("FREE");

        verify(lessonProgressRepository, times(2)).save(any(LessonProgress.class));
    }

    @Test
    void enrollStudent_ShouldUpdatePaymentStatus_WhenPaymentIdProvided() {
        when(enrollmentRepository.existsByStudentAndCourse(student, course)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> {
            Enrollment saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        // Fix: Use when().thenReturn() instead of doNothing()
        when(paymentService.updatePaymentStatus(eq("PAY-123"), any(PaymentStatus.class)))
                .thenReturn(TestDataFactory.createPayment(1L, "PAY-123", student, course, PaymentStatus.COMPLETED));

        Enrollment result = enrollmentService.enrollStudent(student, course, "PAY-123");

        assertThat(result).isNotNull();
        assertThat(result.getPaymentId()).isEqualTo("PAY-123");
        verify(paymentService).updatePaymentStatus("PAY-123", PaymentStatus.COMPLETED);
    }

    @Test
    void completeEnrollment_ShouldMarkAsCompleted() {
        Long enrollmentId = 1L;
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> i.getArgument(0));

        enrollmentService.completeEnrollment(enrollmentId);

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
        assertThat(enrollment.getProgress()).isEqualTo(100.0);
        assertThat(enrollment.getCompletedAt()).isNotNull();
    }

    @Test
    void getEnrollment_ShouldReturnEnrollment_WhenExists() {
        when(enrollmentRepository.findByStudentAndCourse(student, course)).thenReturn(Optional.of(enrollment));

        Optional<Enrollment> result = enrollmentService.getEnrollment(student, course);

        assertThat(result).isPresent();
    }

    @Test
    void isStudentEnrolled_ShouldReturnTrue_WhenEnrolled() {
        when(enrollmentRepository.existsByStudentAndCourse(student, course)).thenReturn(true);

        boolean result = enrollmentService.isStudentEnrolled(student, course);

        assertThat(result).isTrue();
    }
}