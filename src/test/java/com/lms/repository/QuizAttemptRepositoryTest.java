package com.lms.repository;

import com.lms.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class QuizAttemptRepositoryTest {

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
        // Create and save a student
        User student = new User();
        student.setUsername("student1");
        student.setEmail("student1@test.com");
        student.setPassword("pass");
        student.setRole(Role.STUDENT);
        student.setEnabled(true);
        student = userRepository.save(student);

        // Create and save an instructor
        User instructor = new User();
        instructor.setUsername("instructor1");
        instructor.setEmail("instructor1@test.com");
        instructor.setPassword("pass");
        instructor.setRole(Role.INSTRUCTOR);
        instructor.setEnabled(true);
        instructor = userRepository.save(instructor);

        // Create and save a course
        Course course = new Course();
        course.setTitle("Test Course");
        course.setDescription("Description");
        course.setPrice(99.99);
        course.setInstructor(instructor);
        course.setStatus(CourseStatus.APPROVED);
        course = courseRepository.save(course);

        // Create and save a quiz
        Quiz quiz = new Quiz();
        quiz.setTitle("Test Quiz");
        quiz.setDescription("Quiz Description");
        quiz.setDuration(30);
        quiz.setPassingScore(70);
        quiz.setMaxAttempts(3);
        quiz.setCourse(course);
        quiz = quizRepository.save(quiz);

        // Create and save an enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setProgress(0.0);
        enrollment = enrollmentRepository.save(enrollment);

        // Create and save a passed attempt
        QuizAttempt passedAttempt = new QuizAttempt();
        passedAttempt.setQuiz(quiz);
        passedAttempt.setStudent(student);
        passedAttempt.setEnrollment(enrollment);
        passedAttempt.setAttemptNumber(1);
        passedAttempt.setStatus("passed");
        passedAttempt.setScore(10);
        passedAttempt.setPercentage(100);
        passedAttempt.setStartedAt(LocalDateTime.now());
        passedAttempt.setCompletedAt(LocalDateTime.now());
        quizAttemptRepository.save(passedAttempt);

        quizAttemptRepository.flush();

        // Count passed attempts
        Long count = quizAttemptRepository.countPassedByEnrollment(enrollment);

        // Verify
        assertThat(count).isEqualTo(1L);
    }
}