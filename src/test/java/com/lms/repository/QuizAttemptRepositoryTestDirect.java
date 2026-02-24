package com.lms.repository;

import com.lms.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class QuizAttemptRepositoryTestDirect {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void testCountPassedByEnrollment() {
        // Create and save all entities
        User instructor = new User("instructor", "inst@test.com", "pass", Role.INSTRUCTOR);
        instructor = userRepository.save(instructor);

        User student = new User("student", "stu@test.com", "pass", Role.STUDENT);
        student = userRepository.save(student);

        Course course = new Course("Test Course", "Description", 99.99, instructor);
        course = courseRepository.save(course);

        Quiz quiz = new Quiz("Test Quiz", "Quiz Description", 30, 70, 3, course, null);
        quiz = quizRepository.save(quiz);

        Enrollment enrollment = new Enrollment(student, course);
        enrollment = enrollmentRepository.save(enrollment);

        // Create first attempt (failed)
        QuizAttempt attempt1 = new QuizAttempt();
        attempt1.setQuiz(quiz);
        attempt1.setStudent(student);
        attempt1.setEnrollment(enrollment);
        attempt1.setAttemptNumber(1);
        attempt1.setStatus("failed");
        attempt1.setScore(5);
        attempt1.setPercentage(50);
        attempt1.setStartedAt(LocalDateTime.now().minusDays(2));
        attempt1.setCompletedAt(LocalDateTime.now().minusDays(1));
        quizAttemptRepository.save(attempt1);

        // Create second attempt (passed)
        QuizAttempt attempt2 = new QuizAttempt();
        attempt2.setQuiz(quiz);
        attempt2.setStudent(student);
        attempt2.setEnrollment(enrollment);
        attempt2.setAttemptNumber(2);
        attempt2.setStatus("passed");
        attempt2.setScore(9);
        attempt2.setPercentage(90);
        attempt2.setStartedAt(LocalDateTime.now().minusDays(1));
        attempt2.setCompletedAt(LocalDateTime.now());
        quizAttemptRepository.save(attempt2);

        quizAttemptRepository.flush();
        entityManager.flush();
        entityManager.clear();

        // METHOD 1: Native query
        Query nativeQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM quiz_attempts WHERE enrollment_id = ? AND status = 'passed'");
        nativeQuery.setParameter(1, enrollment.getId());
        Long nativeCount = ((Number) nativeQuery.getSingleResult()).longValue();
        System.out.println("Native query count: " + nativeCount);

        // METHOD 2: JPQL query
        Query jpqlQuery = entityManager.createQuery(
                "SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.enrollment = :enrollment AND qa.status = 'passed'");
        jpqlQuery.setParameter("enrollment", enrollment);
        Long jpqlCount = (Long) jpqlQuery.getSingleResult();
        System.out.println("JPQL count: " + jpqlCount);

        // METHOD 3: Repository method
        Long repoCount = quizAttemptRepository.countPassedByEnrollment(enrollment);
        System.out.println("Repository count: " + repoCount);

        // Verify all methods return 1
        assertThat(nativeCount).isEqualTo(1L);
        assertThat(jpqlCount).isEqualTo(1L);
        assertThat(repoCount).isEqualTo(1L);
    }
}