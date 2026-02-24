package com.lms.repository;

import com.lms.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class QuizAttemptRepositoryFixedTest {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

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

        // DEBUG: Print all attempts to see what was saved
        List<QuizAttempt> allAttempts = quizAttemptRepository.findByEnrollment(enrollment);
        System.out.println("\n=== VERIFYING SAVED DATA ===");
        for (QuizAttempt att : allAttempts) {
            System.out.println("Attempt " + att.getAttemptNumber() +
                    ": status='" + att.getStatus() + "'");
        }

        // Test the repository method
        Long repositoryCount = quizAttemptRepository.countPassedByEnrollment(enrollment);
        System.out.println("Repository countPassedByEnrollment: " + repositoryCount);

        assertThat(repositoryCount)
                .as("Repository countPassedByEnrollment should return 1")
                .isEqualTo(1L);
    }
}