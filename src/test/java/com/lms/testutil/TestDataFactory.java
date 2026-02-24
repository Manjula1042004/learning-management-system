package com.lms.testutil;

import com.lms.entity.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class TestDataFactory {

    public static User createUser(Long id, String username, String email, Role role) {
        User user = new User(username, email, "password123", role);
        user.setId(id);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    public static User createStudent(Long id) {
        return createUser(id, "student" + id, "student" + id + "@test.com", Role.STUDENT);
    }

    public static User createInstructor(Long id) {
        User instructor = createUser(id, "instructor" + id, "instructor" + id + "@test.com", Role.INSTRUCTOR);
        instructor.setBio("Experienced instructor");
        return instructor;
    }

    public static User createAdmin(Long id) {
        return createUser(id, "admin" + id, "admin" + id + "@test.com", Role.ADMIN);
    }

    public static Course createCourse(Long id, String title, User instructor) {
        Course course = new Course(title, "Test course description", 99.99, instructor);
        course.setId(id);
        course.setStatus(CourseStatus.APPROVED);
        course.setThumbnailUrl("https://test.com/thumbnail.jpg");
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        course.setLessons(new HashSet<>());
        course.setEnrollments(new HashSet<>());
        return course;
    }

    public static Course createPendingCourse(Long id, String title, User instructor) {
        Course course = createCourse(id, title, instructor);
        course.setStatus(CourseStatus.PENDING);
        return course;
    }

    public static Lesson createLesson(Long id, String title, Course course, LessonType type) {
        Lesson lesson = new Lesson(title, "Test lesson description", 30, 1, type, course);
        lesson.setId(id);
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());

        if (type == LessonType.VIDEO) {
            lesson.setVideoUrl("https://youtube.com/test");
        } else if (type == LessonType.PDF) {
            lesson.setResourceUrl("https://test.com/test.pdf");
        }

        return lesson;
    }

    public static Enrollment createEnrollment(Long id, User student, Course course) {
        Enrollment enrollment = new Enrollment(student, course);
        enrollment.setId(id);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setProgress(0.0);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setPaymentId("PAY-" + id);
        return enrollment;
    }

    public static LessonProgress createLessonProgress(Long id, Enrollment enrollment, Lesson lesson) {
        LessonProgress progress = new LessonProgress(enrollment, lesson);
        progress.setId(id);
        progress.setCompleted(false);
        progress.setWatchTime(0.0);
        progress.setStartedAt(LocalDateTime.now());
        return progress;
    }

    public static Quiz createQuiz(Long id, String title, Course course, Lesson lesson) {
        Quiz quiz = new Quiz(title, "Test quiz description", 30, 70, 3, course, lesson);
        quiz.setId(id);
        quiz.setTotalQuestions(0);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setUpdatedAt(LocalDateTime.now());
        quiz.setQuestions(new HashSet<>());
        return quiz;
    }

    public static Question createQuestion(Long id, String text, String type, Quiz quiz) {
        Question question = new Question();
        question.setId(id);
        question.setQuestionText(text);
        question.setQuestionType(type);
        question.setPoints(1);
        question.setQuiz(quiz);
        question.setOptions(new HashSet<>());
        return question;
    }

    public static Option createOption(Long id, String text, boolean isCorrect, Question question) {
        Option option = new Option();
        option.setId(id);
        option.setOptionText(text);
        option.setIsCorrect(isCorrect);
        option.setQuestion(question);
        return option;
    }

    public static Media createMedia(Long id, String fileName, User uploader, Course course) {
        Media media = new Media(
                fileName,
                "stored_" + fileName,
                "https://cloudinary.com/" + fileName,
                "video",
                1024L,
                "video/mp4",
                uploader,
                course,
                null
        );
        media.setId(id);
        media.setUploadedAt(LocalDateTime.now());
        media.setUpdatedAt(LocalDateTime.now());
        return media;
    }

    public static Payment createPayment(Long id, String paymentId, User user, Course course, PaymentStatus status) {
        return new Payment(
                paymentId,
                status,
                course.getPrice(),
                "USD",
                "Payment for " + course.getTitle(),
                user,
                course
        );
    }

    public static QuizAttempt createQuizAttempt(Long id, Quiz quiz, User student, Enrollment enrollment) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setId(id);
        attempt.setQuiz(quiz);
        attempt.setStudent(student);
        attempt.setEnrollment(enrollment);
        attempt.setAttemptNumber(1);
        attempt.setStatus("in_progress");
        attempt.setStartedAt(LocalDateTime.now());
        return attempt;
    }
    // Add this method to your existing TestDataFactory.java
    public static StudentAnswer createStudentAnswer(Long id, QuizAttempt attempt, Question question,
                                                    String answer, boolean isCorrect, int points) {
        StudentAnswer studentAnswer = new StudentAnswer();
        studentAnswer.setId(id);
        studentAnswer.setQuizAttempt(attempt);
        studentAnswer.setQuestion(question);
        studentAnswer.setAnswerText(answer);
        studentAnswer.setIsCorrect(isCorrect);
        studentAnswer.setPointsEarned(points);
        return studentAnswer;
    }
}