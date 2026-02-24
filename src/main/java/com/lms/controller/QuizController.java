package com.lms.controller;

import com.lms.entity.*;
import com.lms.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    private final QuizService quizService;
    private final UserService userService;
    private final CourseService courseService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;

    public QuizController(QuizService quizService,
                          UserService userService,
                          CourseService courseService,
                          LessonService lessonService,
                          EnrollmentService enrollmentService) {
        this.quizService = quizService;
        this.userService = userService;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.enrollmentService = enrollmentService;
    }

    // ✅ FIXED: View quiz details with proper course isolation
    @GetMapping("/{quizId}")
    public String viewQuiz(@PathVariable Long quizId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User student = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Quiz quiz = quizService.getQuizById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // ✅ FIX: Check if student is enrolled in THIS specific course
        boolean enrolled = enrollmentService.getEnrollment(student, quiz.getCourse()).isPresent();
        if (!enrolled) {
            return "redirect:/courses/" + quiz.getCourse().getId() + "?error=not_enrolled";
        }

        // ✅ FIX: Get previous attempts for THIS specific enrollment
        List<QuizAttempt> previousAttempts = Collections.emptyList();
        Optional<Enrollment> enrollment = enrollmentService.getEnrollment(student, quiz.getCourse());
        if (enrollment.isPresent()) {
            previousAttempts = quizService.getQuizAttemptsByEnrollment(enrollment.get());
        }

        model.addAttribute("user", student);
        model.addAttribute("quiz", quiz);
        model.addAttribute("previousAttempts", previousAttempts);

        return "student/quiz";
    }

    // ✅ FIXED: Start quiz attempt with proper isolation
    @PostMapping("/start/{quizId}")
    public String startQuiz(@PathVariable Long quizId,
                            @AuthenticationPrincipal UserDetails userDetails) {
        User student = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Quiz quiz = quizService.getQuizById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // ✅ FIX: Check enrollment before starting quiz
        Optional<Enrollment> enrollment = enrollmentService.getEnrollment(student, quiz.getCourse());
        if (enrollment.isEmpty()) {
            return "redirect:/courses/" + quiz.getCourse().getId() + "?error=not_enrolled";
        }

        QuizAttempt attempt = quizService.startQuizAttempt(quiz, student);

        return "redirect:/quiz/attempt/" + attempt.getId();
    }

    // ✅ FIXED: Take quiz with proper student verification
    @GetMapping("/attempt/{attemptId}")
    public String takeQuiz(@PathVariable Long attemptId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User student = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        QuizAttempt attempt = quizService.getQuizAttemptById(attemptId)
                .orElseThrow(() -> new RuntimeException("Quiz attempt not found"));

        // ✅ FIX: Verify student owns this attempt
        if (!attempt.getStudent().getId().equals(student.getId())) {
            return "redirect:/quiz/" + attempt.getQuiz().getId() + "?error=unauthorized";
        }

        model.addAttribute("user", student);
        model.addAttribute("attempt", attempt);
        model.addAttribute("quiz", attempt.getQuiz());

        return "student/take-quiz";
    }

    // ✅ FIXED: Submit quiz with proper validation
    @PostMapping("/submit/{attemptId}")
    public String submitQuiz(@PathVariable Long attemptId,
                             @RequestParam Map<String, String> answers,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User student = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        QuizAttempt attempt = quizService.getQuizAttemptById(attemptId)
                .orElseThrow(() -> new RuntimeException("Quiz attempt not found"));

        // ✅ FIX: Verify ownership before submission
        if (!attempt.getStudent().getId().equals(student.getId())) {
            return "redirect:/quiz/" + attempt.getQuiz().getId() + "?error=unauthorized";
        }

        // Convert answer map
        Map<Long, String> answerMap = answers.entrySet().stream()
                .filter(e -> e.getKey().startsWith("question_"))
                .collect(java.util.stream.Collectors.toMap(
                        e -> Long.parseLong(e.getKey().replace("question_", "")),
                        Map.Entry::getValue
                ));

        QuizAttempt submittedAttempt = quizService.submitQuizAttempt(attemptId, answerMap);

        return "redirect:/quiz/result/" + submittedAttempt.getId();
    }

    // ✅ FIXED: View quiz result with proper isolation
    @GetMapping("/result/{attemptId}")
    public String viewResult(@PathVariable Long attemptId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        User student = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        QuizAttempt attempt = quizService.getQuizAttemptById(attemptId)
                .orElseThrow(() -> new RuntimeException("Quiz attempt not found"));

        // ✅ FIX: Verify student owns this attempt
        if (!attempt.getStudent().getId().equals(student.getId())) {
            return "redirect:/quiz/" + attempt.getQuiz().getId() + "?error=unauthorized";
        }

        model.addAttribute("user", student);
        model.addAttribute("attempt", attempt);
        model.addAttribute("quiz", attempt.getQuiz());

        return "student/quiz-result";
    }

    // Load quizzes for course details page
    @GetMapping("/courses/{courseId}")
    public String courseDetails(@PathVariable Long courseId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<Lesson> lessons = lessonService.getLessonsByCourse(course);

        // Load quiz for each quiz-type lesson
        for (Lesson lesson : lessons) {
            if (lesson.getType() == LessonType.QUIZ) {
                Optional<Quiz> quiz = quizService.getQuizByLesson(lesson.getId());
                quiz.ifPresent(lesson::setQuiz);
            }
        }

        // Check if student is enrolled
        boolean enrolled = false;
        if (user.isStudent()) {
            enrolled = enrollmentService.getEnrollment(user, course).isPresent();
        }

        model.addAttribute("user", user);
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        model.addAttribute("enrolled", enrolled);

        return "courses/details";
    }
}