package com.lms.service;

import com.lms.entity.Enrollment;
import com.lms.entity.EnrollmentStatus;
import com.lms.entity.LessonProgress;
import com.lms.entity.User;
import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.repository.EnrollmentRepository;
import com.lms.repository.LessonProgressRepository;
import com.lms.repository.QuizAttemptRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final PaymentService paymentService;
    private final QuizAttemptRepository quizAttemptRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             LessonProgressRepository lessonProgressRepository,
                             PaymentService paymentService,
                             QuizAttemptRepository quizAttemptRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.paymentService = paymentService;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    public Enrollment enrollStudent(User student, Course course, String paymentId) {
        // Check if already enrolled
        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new RuntimeException("Student is already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment(student, course);
        enrollment.setPaymentId(paymentId);
        enrollment = enrollmentRepository.save(enrollment);

        // Update payment status if payment ID is provided
        if (paymentId != null && !paymentId.equals("FREE") && !paymentId.startsWith("TEST-")) {
            try {
                paymentService.updatePaymentStatus(paymentId, com.lms.entity.PaymentStatus.COMPLETED);
            } catch (Exception e) {
                System.out.println("Warning: Could not update payment status: " + e.getMessage());
            }
        }

        // Initialize lesson progress for all lessons in the course
        initializeLessonProgress(enrollment);

        return enrollment;
    }

    private void initializeLessonProgress(Enrollment enrollment) {
        // Convert Set to List for iteration
        List<Lesson> lessons = new ArrayList<>(enrollment.getCourse().getLessons());
        for (Lesson lesson : lessons) {
            LessonProgress progress = new LessonProgress(enrollment, lesson);
            lessonProgressRepository.save(progress);
        }
    }

    public void completeEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollment.setCompletedAt(LocalDateTime.now());
        enrollment.setProgress(100.0);

        enrollmentRepository.save(enrollment);
    }

    public Optional<Enrollment> getEnrollment(User student, Course course) {
        return enrollmentRepository.findByStudentAndCourse(student, course);
    }

    public List<Enrollment> getEnrollmentsByStudent(User student) {
        return enrollmentRepository.findByStudent(student);
    }

    public List<Enrollment> getEnrollmentsByCourse(Course course) {
        return enrollmentRepository.findByCourse(course);
    }

    public boolean isStudentEnrolled(User student, Course course) {
        return enrollmentRepository.existsByStudentAndCourse(student, course);
    }

    public Long getEnrollmentCountByCourse(Course course) {
        return enrollmentRepository.countByCourse(course);
    }

    public List<Enrollment> getEnrollmentsByInstructor(User instructor) {
        System.out.println("=== DEBUG: Getting enrollments for instructor: " + instructor.getUsername());
        List<Enrollment> enrollments = enrollmentRepository.findByInstructor(instructor);
        System.out.println("=== DEBUG: Found " + enrollments.size() + " enrollments");
        return enrollments;
    }

    public void updateProgress(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        // Count completed lessons
        Long totalLessons = (long) enrollment.getCourse().getLessons().size();
        Long completedLessons = lessonProgressRepository.countCompletedLessonsByEnrollment(enrollment);

        // Count passed quizzes
        Long passedQuizzes = quizAttemptRepository.countPassedByEnrollment(enrollment);
        Long totalQuizzes = quizAttemptRepository.countByEnrollment(enrollment);

        // Calculate progress (70% lessons, 30% quizzes)
        double lessonProgress = totalLessons > 0 ? (completedLessons.doubleValue() / totalLessons.doubleValue()) * 70.0 : 0.0;
        double quizProgress = totalQuizzes > 0 ? (passedQuizzes.doubleValue() / totalQuizzes.doubleValue()) * 30.0 : 0.0;

        double overallProgress = lessonProgress + quizProgress;

        enrollment.setProgress(overallProgress);

        if (overallProgress >= 100.0) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(LocalDateTime.now());
        }

        enrollmentRepository.save(enrollment);
    }
    // Add these methods to EnrollmentService.java

    /**
     * Get the last accessed time for an enrollment
     */
    public LocalDateTime getLastAccessed(Enrollment enrollment) {
        List<LessonProgress> progresses = lessonProgressRepository.findByEnrollment(enrollment);
        return progresses.stream()
                .map(LessonProgress::getStartedAt)
                .max(LocalDateTime::compareTo)
                .orElse(enrollment.getEnrolledAt());
    }

    /**
     * Get completed lessons count for an enrollment
     */
    public long getCompletedLessonsCount(Enrollment enrollment) {
        return lessonProgressRepository.countCompletedLessonsByEnrollment(enrollment);
    }

    /**
     * Get total lessons count for an enrollment's course
     */
    public long getTotalLessonsCount(Enrollment enrollment) {
        return enrollment.getCourse().getLessons().size();
    }

}