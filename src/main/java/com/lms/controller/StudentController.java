package com.lms.controller;

import com.lms.entity.*;
import com.lms.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final LessonService lessonService;
    private final QuizService quizService;
    private final LessonProgressService lessonProgressService;
    private final MediaService mediaService;

    public StudentController(UserService userService,
                             CourseService courseService,
                             EnrollmentService enrollmentService,
                             LessonService lessonService,
                             QuizService quizService,
                             LessonProgressService lessonProgressService,
                             MediaService mediaService) {
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.lessonService = lessonService;
        this.quizService = quizService;
        this.lessonProgressService = lessonProgressService;
        this.mediaService = mediaService;
    }

    @GetMapping
    public String studentDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        User student = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!student.isStudent()) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", student);

        // Get student enrollments
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(student);

        // Calculate statistics
        long completedCount = 0;
        long inProgressCount = 0;
        double totalProgress = 0.0;

        Map<Long, Double> enrollmentProgress = new HashMap<>();
        List<Map<String, Object>> upcomingQuizzes = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            double progress = enrollment.getProgress() != null ? enrollment.getProgress() : 0.0;
            enrollmentProgress.put(enrollment.getId(), progress);

            if (progress >= 100.0) {
                completedCount++;
            } else if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                inProgressCount++;
            }
            totalProgress += progress;

            // Find quizzes in this course
            Course course = enrollment.getCourse();
            List<Lesson> lessons = lessonService.getLessonsByCourse(course);

            for (Lesson lesson : lessons) {
                if (lesson.getType() == LessonType.QUIZ) {
                    Optional<Quiz> quizOpt = quizService.getQuizByLesson(lesson.getId());
                    if (quizOpt.isPresent()) {
                        Quiz quiz = quizOpt.get();
                        Map<String, Object> quizInfo = new HashMap<>();
                        quizInfo.put("courseTitle", course.getTitle());
                        quizInfo.put("courseId", course.getId());
                        quizInfo.put("lessonTitle", lesson.getTitle());
                        quizInfo.put("quizId", quiz.getId());
                        quizInfo.put("duration", quiz.getDuration());
                        quizInfo.put("totalQuestions", quiz.getTotalQuestions());
                        upcomingQuizzes.add(quizInfo);
                    }
                }
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("enrolledCourses", enrollments.size());
        stats.put("completedCourses", completedCount);
        stats.put("inProgressCourses", inProgressCount);
        stats.put("averageProgress", enrollments.isEmpty() ? 0 : Math.round(totalProgress / enrollments.size()));

        List<Course> recommendedCourses = new ArrayList<>();

        model.addAttribute("enrollmentProgress", enrollmentProgress);
        model.addAttribute("activeEnrollments", enrollments);
        model.addAttribute("stats", stats);
        model.addAttribute("recommendedCourses", recommendedCourses);
        model.addAttribute("studentQuizzes", new ArrayList<>());
        model.addAttribute("upcomingQuizzes", upcomingQuizzes);
        model.addAttribute("hasQuizzes", !upcomingQuizzes.isEmpty());

        return "student/dashboard";
    }

    @GetMapping("/my-courses")
    public String myCourses(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User student = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(student);

        // Calculate statistics
        long completedCount = 0;
        long inProgressCount = 0;
        double totalProgress = 0.0;

        // Load all quizzes for enrolled courses
        for (Enrollment enrollment : enrollments) {
            Course course = enrollment.getCourse();
            List<Lesson> lessons = lessonService.getLessonsByCourse(course);

            // Load quiz for each lesson
            for (Lesson lesson : lessons) {
                if (lesson.getType() == LessonType.QUIZ) {
                    Optional<Quiz> quiz = quizService.getQuizByLesson(lesson.getId());
                    quiz.ifPresent(lesson::setQuiz);
                }
            }

            // Calculate stats
            double progress = enrollment.getProgress() != null ? enrollment.getProgress() : 0.0;
            if (progress >= 100.0) {
                completedCount++;
            } else if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                inProgressCount++;
            }
            totalProgress += progress;
        }

        model.addAttribute("user", student);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("avgProgress", enrollments.isEmpty() ? 0 : Math.round(totalProgress / enrollments.size()));

        return "student/my-courses";
    }

    @GetMapping("/progress")
    public String viewProgress(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User student = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(student);

        // Calculate statistics
        long completedCount = 0;
        long inProgressCount = 0;
        double totalProgress = 0.0;

        // Create a map of lesson progress for quick lookup in template
        Map<String, LessonProgress> lessonProgressMap = new HashMap<>();

        // Enhance enrollments with additional data and store in wrapper objects
        List<EnrollmentWrapper> enrollmentWrappers = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            EnrollmentWrapper wrapper = new EnrollmentWrapper(enrollment);
            Course course = enrollment.getCourse();

            // Calculate progress
            double progress = enrollment.getProgress() != null ? enrollment.getProgress() : 0.0;
            if (progress >= 100.0) {
                completedCount++;
            } else if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                inProgressCount++;
            }
            totalProgress += progress;

            // Calculate completed lessons count
            List<LessonProgress> progresses = lessonProgressService.getProgressByEnrollment(enrollment);
            long completedLessons = progresses.stream()
                    .filter(LessonProgress::isCompleted)
                    .count();
            wrapper.setCompletedLessons(completedLessons);
            wrapper.setTotalLessons(course.getLessons().size());

            // Calculate quiz stats
            List<QuizAttempt> quizAttempts = quizService.getQuizAttemptsByEnrollment(enrollment);
            long passedQuizzes = quizAttempts.stream()
                    .filter(a -> "passed".equals(a.getStatus()))
                    .count();
            long totalQuizzes = course.getLessons().stream()
                    .filter(l -> l.getType() == LessonType.QUIZ)
                    .count();

            wrapper.setPassedQuizzes(passedQuizzes);
            wrapper.setTotalQuizzes(totalQuizzes);
            wrapper.setQuizAttempts(quizAttempts);

            // Get last accessed time (most recent lesson progress)
            Optional<LocalDateTime> lastAccessed = progresses.stream()
                    .map(LessonProgress::getStartedAt)
                    .max(LocalDateTime::compareTo);
            wrapper.setLastAccessed(lastAccessed.orElse(enrollment.getEnrolledAt()));

            // Build lesson progress map
            for (LessonProgress progress_ : progresses) {
                String key = course.getId() + "_" + progress_.getLesson().getId();
                lessonProgressMap.put(key, progress_);
            }

            enrollmentWrappers.add(wrapper);
        }

        model.addAttribute("user", student);
        model.addAttribute("enrollments", enrollmentWrappers);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("avgProgress", enrollments.isEmpty() ? 0 : Math.round(totalProgress / enrollments.size()));
        model.addAttribute("lessonProgress", lessonProgressMap);

        return "student/progress";
    }

    // Wrapper class to add computed properties to Enrollment
    public static class EnrollmentWrapper {
        private final Enrollment enrollment;
        private long completedLessons;
        private long totalLessons;
        private long passedQuizzes;
        private long totalQuizzes;
        private List<QuizAttempt> quizAttempts;
        private LocalDateTime lastAccessed;

        public EnrollmentWrapper(Enrollment enrollment) {
            this.enrollment = enrollment;
        }

        // Getters for original enrollment properties
        public Long getId() { return enrollment.getId(); }
        public User getStudent() { return enrollment.getStudent(); }
        public Course getCourse() { return enrollment.getCourse(); }
        public EnrollmentStatus getStatus() { return enrollment.getStatus(); }
        public LocalDateTime getEnrolledAt() { return enrollment.getEnrolledAt(); }
        public LocalDateTime getCompletedAt() { return enrollment.getCompletedAt(); }
        public Double getProgress() { return enrollment.getProgress(); }
        public String getPaymentId() { return enrollment.getPaymentId(); }

        // Getters and setters for computed properties
        public long getCompletedLessons() { return completedLessons; }
        public void setCompletedLessons(long completedLessons) { this.completedLessons = completedLessons; }

        public long getTotalLessons() { return totalLessons; }
        public void setTotalLessons(long totalLessons) { this.totalLessons = totalLessons; }

        public long getPassedQuizzes() { return passedQuizzes; }
        public void setPassedQuizzes(long passedQuizzes) { this.passedQuizzes = passedQuizzes; }

        public long getTotalQuizzes() { return totalQuizzes; }
        public void setTotalQuizzes(long totalQuizzes) { this.totalQuizzes = totalQuizzes; }

        public List<QuizAttempt> getQuizAttempts() { return quizAttempts; }
        public void setQuizAttempts(List<QuizAttempt> quizAttempts) { this.quizAttempts = quizAttempts; }

        public LocalDateTime getLastAccessed() { return lastAccessed; }
        public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; }
    }

    @PostMapping("/quiz/start/{quizId}")
    public String startQuiz(@PathVariable Long quizId,
                            @AuthenticationPrincipal UserDetails userDetails) {
        User student = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Quiz quiz = quizService.getQuizById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        QuizAttempt attempt = quizService.startQuizAttempt(quiz, student);

        return "redirect:/student/quiz/attempt/" + attempt.getId();
    }

    @GetMapping("/quiz/attempt/{attemptId}")
    public String takeQuiz(@PathVariable Long attemptId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User student = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        QuizAttempt attempt = quizService.getQuizAttemptById(attemptId)
                .orElseThrow(() -> new RuntimeException("Quiz attempt not found"));

        // Verify student owns this attempt
        if (!attempt.getStudent().getId().equals(student.getId())) {
            return "redirect:/student/quiz/" + attempt.getQuiz().getId() + "?error=unauthorized";
        }

        model.addAttribute("user", student);
        model.addAttribute("attempt", attempt);
        model.addAttribute("quiz", attempt.getQuiz());

        return "student/take-quiz";
    }

    @GetMapping("/quiz/{quizId}")
    public String viewQuiz(@PathVariable Long quizId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User student = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Quiz quiz = quizService.getQuizById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // SECURITY CHECK: Ensure student can only access quizzes from courses they're enrolled in
        boolean canAccess = quizService.canStudentAccessQuiz(quizId, student);
        if (!canAccess) {
            return "redirect:/student?error=unauthorized";
        }

        // Get previous attempts
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
}