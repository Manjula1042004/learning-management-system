package com.lms.controller;

import com.lms.entity.*;
import com.lms.service.CourseService;
import com.lms.service.LessonService;
import com.lms.service.QuizService;
import com.lms.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/instructor/quiz")
public class InstructorQuizController {

    private final QuizService quizService;
    private final CourseService courseService;
    private final LessonService lessonService;
    private final UserService userService;

    public InstructorQuizController(QuizService quizService,
                                    CourseService courseService,
                                    LessonService lessonService,
                                    UserService userService) {
        this.quizService = quizService;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.userService = userService;
    }

    // ═══════════════════════════════════════════════════════════
    // GET: SHOW EDIT QUIZ FORM
    // ═══════════════════════════════════════════════════════════
    @GetMapping("/edit/{quizId}")
    public String showEditQuizForm(@PathVariable Long quizId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║  📝 SHOW EDIT QUIZ FORM                                   ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("📋 Quiz ID: " + quizId);
        System.out.println("📋 User: " + userDetails.getUsername());

        try {
            // Get current user
            User instructor = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            System.out.println("✅ User loaded: " + instructor.getUsername());

            // Get quiz with all related data
            Quiz quiz = quizService.getQuizById(quizId)
                    .orElseThrow(() -> new RuntimeException("Quiz not found with ID: " + quizId));

            System.out.println("✅ Quiz loaded: " + quiz.getTitle());
            System.out.println("   • Course: " + quiz.getCourse().getTitle());
            System.out.println("   • Questions: " + quiz.getQuestions().size());

            // Verify ownership
            if (!quiz.getCourse().getInstructor().getId().equals(instructor.getId())) {
                System.out.println("❌ Permission denied - not course owner");
                redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this quiz");
                return "redirect:/courses";
            }

            System.out.println("✅ Permission verified");

            // Add data to model
            model.addAttribute("quiz", quiz);
            model.addAttribute("course", quiz.getCourse());

            // Log questions for debugging
            if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
                System.out.println("\n📋 Questions in quiz:");
                int i = 1;
                for (Question q : quiz.getQuestions()) {
                    System.out.println("   " + i + ". " + q.getQuestionText() + " (" + q.getQuestionType() + ")");
                    if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                        System.out.println("      Options: " + q.getOptions().size());
                    }
                    i++;
                }
            } else {
                System.out.println("\n⚠️  No questions in this quiz");
            }

            System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
            System.out.println("║  ✅ RENDERING EDIT PAGE                                   ║");
            System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

            return "instructor/quiz/edit";

        } catch (Exception e) {
            System.err.println("\n╔═══════════════════════════════════════════════════════════╗");
            System.err.println("║  ❌ ERROR LOADING QUIZ                                    ║");
            System.err.println("╚═══════════════════════════════════════════════════════════╝");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("error", "Error loading quiz: " + e.getMessage());
            return "redirect:/courses";
        }
    }

    // ═══════════════════════════════════════════════════════════
    // POST: UPDATE QUIZ (EDIT)
    // ═══════════════════════════════════════════════════════════
    @PostMapping("/edit/{quizId}")
    public String updateQuiz(@PathVariable Long quizId,
                             @RequestParam String title,
                             @RequestParam String description,
                             @RequestParam Integer duration,
                             @RequestParam Integer passingScore,
                             @RequestParam(required = false) Integer maxAttempts,
                             @RequestParam(value = "questionText", required = false) List<String> questionTexts,
                             @RequestParam(value = "questionType", required = false) List<String> questionTypes,
                             @RequestParam(value = "points", required = false) List<Integer> points,
                             @RequestParam(value = "optionText", required = false) List<String> optionTexts,
                             @RequestParam(value = "isCorrect", required = false) List<String> isCorrectOptions,
                             @RequestParam(value = "correctAnswer", required = false) List<String> correctAnswers,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {

        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║  📝 UPDATE QUIZ ENDPOINT CALLED                           ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("📋 Quiz ID: " + quizId);
        System.out.println("📋 New Title: " + title);
        System.out.println("📋 Questions count: " + (questionTexts != null ? questionTexts.size() : 0));

        try {
            User instructor = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Quiz quiz = quizService.getQuizById(quizId)
                    .orElseThrow(() -> new RuntimeException("Quiz not found"));

            // Verify ownership
            if (!quiz.getCourse().getInstructor().getId().equals(instructor.getId())) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this quiz");
                return "redirect:/courses";
            }

            // Update basic quiz info
            quiz.setTitle(title);
            quiz.setDescription(description);
            quiz.setDuration(duration);
            quiz.setPassingScore(passingScore);
            quiz.setMaxAttempts(maxAttempts != null ? maxAttempts : 3);

            // Process questions
            Set<Question> questions = new HashSet<>();

            boolean hasQuestions = questionTexts != null && !questionTexts.isEmpty()
                    && questionTexts.stream().anyMatch(q -> q != null && !q.trim().isEmpty());

            if (hasQuestions) {
                System.out.println("✅ Processing " + questionTexts.size() + " questions...");
                int optionIndex = 0;

                for (int i = 0; i < questionTexts.size(); i++) {
                    String questionText = questionTexts.get(i);

                    if (questionText == null || questionText.trim().isEmpty()) {
                        continue;
                    }

                    Question question = new Question();
                    question.setQuestionText(questionText);
                    question.setQuestionType(questionTypes.get(i));
                    question.setPoints(points != null && i < points.size() && points.get(i) != null ? points.get(i) : 1);

                    // Handle options for multiple choice
                    if ("multiple_choice".equals(questionTypes.get(i)) && optionTexts != null) {
                        Set<Option> options = new HashSet<>();

                        while (optionIndex < optionTexts.size()) {
                            String optText = optionTexts.get(optionIndex);

                            if (optText == null || optText.trim().isEmpty()) {
                                optionIndex++;
                                break;
                            }

                            Option option = new Option();
                            option.setOptionText(optText);

                            boolean isCorrect = isCorrectOptions != null &&
                                    isCorrectOptions.contains("q" + i + "_o" + options.size());
                            option.setIsCorrect(isCorrect);

                            options.add(option);
                            optionIndex++;

                            if (optionIndex >= optionTexts.size()) {
                                break;
                            }
                        }

                        question.setOptions(options);
                    }
                    else if (("true_false".equals(questionTypes.get(i)) || "short_answer".equals(questionTypes.get(i)))
                            && correctAnswers != null && i < correctAnswers.size()) {
                        question.setCorrectAnswer(correctAnswers.get(i));
                    }

                    questions.add(question);
                }

                quiz.setQuestions(questions);
            } else {
                quiz.setQuestions(new HashSet<>());
            }

            // Update quiz using service
            Quiz updatedQuiz = quizService.updateQuiz(quiz);

            System.out.println("✅ QUIZ UPDATED SUCCESSFULLY!");
            System.out.println("   • Quiz ID: " + updatedQuiz.getId());
            System.out.println("   • Questions: " + updatedQuiz.getTotalQuestions());
            System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

            redirectAttributes.addFlashAttribute("success", "Quiz updated successfully!");
            return "redirect:/courses/" + quiz.getCourse().getId();

        } catch (Exception e) {
            System.err.println("❌ ERROR UPDATING QUIZ: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error updating quiz: " + e.getMessage());
            return "redirect:/instructor/quiz/edit/" + quizId;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // POST: DELETE QUIZ
    // ═══════════════════════════════════════════════════════════
    @PostMapping("/delete/{quizId}")
    public String deleteQuiz(@PathVariable Long quizId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║  🔥 DELETE QUIZ ENDPOINT CALLED                           ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("📋 Quiz ID: " + quizId);

        try {
            User instructor = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Quiz quiz = quizService.getQuizById(quizId)
                    .orElseThrow(() -> new RuntimeException("Quiz not found"));

            // Verify ownership
            if (!quiz.getCourse().getInstructor().getId().equals(instructor.getId())) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this quiz");
                return "redirect:/courses";
            }

            Long courseId = quiz.getCourse().getId();
            String quizTitle = quiz.getTitle();

            System.out.println("🗑️ Deleting quiz: " + quizTitle);
            quizService.deleteQuiz(quizId);

            System.out.println("✅ QUIZ DELETED SUCCESSFULLY!");
            System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

            redirectAttributes.addFlashAttribute("success", "Quiz '" + quizTitle + "' deleted successfully!");
            return "redirect:/courses/" + courseId;

        } catch (Exception e) {
            System.err.println("❌ ERROR DELETING QUIZ: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error deleting quiz: " + e.getMessage());
            return "redirect:/courses";
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GET: SHOW CREATE QUIZ FORM
    // ═══════════════════════════════════════════════════════════
    @GetMapping("/create/{courseId}")
    public String showCreateQuizForm(@PathVariable Long courseId,
                                     @RequestParam(required = false) Long lessonId,
                                     @RequestParam(required = false) Long quizId,
                                     Model model) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║  📝 SHOW CREATE QUIZ FORM                                 ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("📋 Course ID: " + courseId);
        System.out.println("📋 Lesson ID: " + lessonId);

        Course course = courseService.getCourseById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        model.addAttribute("course", course);
        model.addAttribute("lessonId", lessonId);
        model.addAttribute("quizId", quizId);

        if (lessonId != null) {
            Lesson lesson = lessonService.getLessonById(lessonId).orElse(null);
            model.addAttribute("lesson", lesson);
        }

        return "instructor/quiz/create";
    }

    // ═══════════════════════════════════════════════════════════
    // POST: CREATE QUIZ
    // ═══════════════════════════════════════════════════════════
    @PostMapping("/create/{courseId}")
    public String createQuiz(@PathVariable Long courseId,
                             @RequestParam(required = false) Long lessonId,
                             @RequestParam String title,
                             @RequestParam String description,
                             @RequestParam Integer duration,
                             @RequestParam Integer passingScore,
                             @RequestParam(required = false) Integer maxAttempts,
                             @RequestParam(value = "questionText", required = false) List<String> questionTexts,
                             @RequestParam(value = "questionType", required = false) List<String> questionTypes,
                             @RequestParam(value = "points", required = false) List<Integer> points,
                             @RequestParam(value = "optionText", required = false) List<String> optionTexts,
                             @RequestParam(value = "isCorrect", required = false) List<String> isCorrectOptions,
                             @RequestParam(value = "correctAnswer", required = false) List<String> correctAnswers,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {

        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║  🎯 CREATE QUIZ CONTROLLER - FORM SUBMISSION            ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");

        System.out.println("📋 Course ID: " + courseId);
        System.out.println("📋 Lesson ID: " + lessonId);
        System.out.println("📋 Title: " + title);
        System.out.println("📋 Questions submitted: " + (questionTexts != null ? questionTexts.size() : 0));

        try {
            User instructor = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Course course = courseService.getCourseById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            Lesson lesson = null;
            if (lessonId != null) {
                lesson = lessonService.getLessonById(lessonId).orElse(null);
            }

            // ✅ Create Quiz object
            Quiz quiz = new Quiz();
            quiz.setTitle(title);
            quiz.setDescription(description);
            quiz.setDuration(duration);
            quiz.setPassingScore(passingScore);
            quiz.setMaxAttempts(maxAttempts != null ? maxAttempts : 3);
            quiz.setTotalQuestions(0);

            // ✅ Process questions if any
            Set<Question> questions = new HashSet<>();

            boolean hasQuestions = questionTexts != null && !questionTexts.isEmpty()
                    && questionTexts.stream().anyMatch(q -> q != null && !q.trim().isEmpty());

            if (hasQuestions) {
                System.out.println("✅ Processing questions...");
                int questionCount = 0;
                int optionIndex = 0;

                for (int i = 0; i < questionTexts.size(); i++) {
                    String questionText = questionTexts.get(i);

                    if (questionText == null || questionText.trim().isEmpty()) {
                        continue;
                    }

                    Question question = new Question();
                    question.setQuestionText(questionText);
                    question.setQuestionType(questionTypes.get(i));
                    question.setPoints(points != null && i < points.size() && points.get(i) != null ? points.get(i) : 1);
                    question.setCorrectAnswer(correctAnswers != null && i < correctAnswers.size() ? correctAnswers.get(i) : "");

                    // ✅ Process options for multiple choice
                    if ("multiple_choice".equals(questionTypes.get(i)) && optionTexts != null) {
                        Set<Option> options = new HashSet<>();

                        // Check if this is the start of options for this question
                        while (optionIndex < optionTexts.size()) {
                            String optText = optionTexts.get(optionIndex);

                            if (optText == null || optText.trim().isEmpty()) {
                                optionIndex++;
                                break;
                            }

                            Option option = new Option();
                            option.setOptionText(optText);

                            // Determine if this option is correct
                            boolean isCorrect = isCorrectOptions != null &&
                                    isCorrectOptions.contains("q" + i + "_o" + options.size());
                            option.setIsCorrect(isCorrect);

                            options.add(option);
                            optionIndex++;

                            if (optionIndex >= optionTexts.size()) {
                                break;
                            }
                        }

                        question.setOptions(options);
                        System.out.println("   Question " + (i+1) + " has " + options.size() + " options");
                    }

                    questions.add(question);
                    questionCount++;
                }

                quiz.setQuestions(questions);
                quiz.setTotalQuestions(questionCount);
                System.out.println("✅ Created " + questionCount + " questions");
            } else {
                System.out.println("ℹ️  No questions provided - creating empty quiz");
                quiz.setQuestions(new HashSet<>());
            }

            // ✅ Save quiz using service
            Quiz createdQuiz = quizService.createQuiz(quiz, course, lesson);

            System.out.println("✅✅✅ QUIZ CREATED SUCCESSFULLY!");
            System.out.println("   • Quiz ID: " + createdQuiz.getId());
            System.out.println("   • Total Questions: " + createdQuiz.getTotalQuestions());
            System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

            redirectAttributes.addFlashAttribute("success", "Quiz created successfully!");
            return "redirect:/courses/" + courseId;

        } catch (Exception e) {
            System.err.println("❌ ERROR CREATING QUIZ: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error creating quiz: " + e.getMessage());
            return "redirect:/instructor/quiz/create/" + courseId;
        }
    }

}