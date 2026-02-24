package com.lms.service;

import com.lms.entity.*;
import com.lms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final EnrollmentService enrollmentService;
    private final CourseService courseService; // Added this line

    public QuizService(QuizRepository quizRepository,
                       QuestionRepository questionRepository,
                       OptionRepository optionRepository,
                       QuizAttemptRepository quizAttemptRepository,
                       StudentAnswerRepository studentAnswerRepository,
                       EnrollmentService enrollmentService,
                       CourseService courseService) { // Added this parameter
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.enrollmentService = enrollmentService;
        this.courseService = courseService; // Initialize it
    }

    // ✅✅✅ FIXED: CREATE QUIZ METHOD ✅✅✅
    @Transactional
    public Quiz createQuiz(Quiz quiz, Course course, Lesson lesson) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║  💾 CREATE QUIZ SERVICE - FIXED VERSION                 ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("📋 Course: " + course.getTitle());
        System.out.println("📋 Quiz Title: " + quiz.getTitle());

        // Step 1: Create the quiz first WITHOUT questions
        Quiz newQuiz = new Quiz();
        newQuiz.setTitle(quiz.getTitle());
        newQuiz.setDescription(quiz.getDescription());
        newQuiz.setDuration(quiz.getDuration());
        newQuiz.setPassingScore(quiz.getPassingScore());
        newQuiz.setMaxAttempts(quiz.getMaxAttempts());
        newQuiz.setCourse(course);
        newQuiz.setLesson(lesson);

        // Save the quiz first (this will generate an ID)
        Quiz savedQuiz = quizRepository.save(newQuiz);
        System.out.println("✅ Step 1: Quiz saved with ID: " + savedQuiz.getId());

        // Step 2: Create and save questions and options
        Set<Question> savedQuestions = new HashSet<>();

        if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
            System.out.println("📝 Step 2: Creating " + quiz.getQuestions().size() + " questions...");

            for (Question questionData : quiz.getQuestions()) {
                // Create NEW Question entity
                Question question = new Question();
                question.setQuiz(savedQuiz); // Link to saved quiz
                question.setQuestionText(questionData.getQuestionText());
                question.setQuestionType(questionData.getQuestionType());
                question.setPoints(questionData.getPoints() != null ? questionData.getPoints() : 1);
                question.setCorrectAnswer(questionData.getCorrectAnswer());

                // Save question first
                Question savedQuestion = questionRepository.save(question);
                System.out.println("   ✅ Saved question: " + savedQuestion.getQuestionText());

                // Create and save options for multiple choice questions
                if ("multiple_choice".equals(questionData.getQuestionType())
                        && questionData.getOptions() != null
                        && !questionData.getOptions().isEmpty()) {

                    Set<Option> savedOptions = new HashSet<>();

                    for (Option optionData : questionData.getOptions()) {
                        // Create NEW Option entity
                        Option option = new Option();
                        option.setQuestion(savedQuestion); // Link to saved question
                        option.setOptionText(optionData.getOptionText());
                        option.setIsCorrect(optionData.getIsCorrect() != null ? optionData.getIsCorrect() : false);

                        // Save option
                        Option savedOption = optionRepository.save(option);
                        savedOptions.add(savedOption);
                        System.out.println("      ✓ Option: " + savedOption.getOptionText()
                                + " (Correct: " + savedOption.getIsCorrect() + ")");
                    }

                    savedQuestion.setOptions(savedOptions);
                }

                savedQuestions.add(savedQuestion);
            }

            savedQuiz.setQuestions(savedQuestions);
            savedQuiz.setTotalQuestions(savedQuestions.size());
        } else {
            savedQuiz.setTotalQuestions(0);
            System.out.println("ℹ️  No questions provided - creating empty quiz");
        }

        // Save final quiz with question count
        Quiz finalQuiz = quizRepository.save(savedQuiz);

        System.out.println("\n✅✅✅ QUIZ CREATION COMPLETE!");
        System.out.println("   • Quiz ID: " + finalQuiz.getId());
        System.out.println("   • Total Questions: " + finalQuiz.getTotalQuestions());
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

        return finalQuiz;
    }

    // ✅✅✅ FIXED: UPDATE QUIZ METHOD ✅✅✅
    @Transactional
    public Quiz updateQuiz(Quiz quizData) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║  🔄 UPDATE QUIZ SERVICE - FIXED VERSION                 ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("📋 Quiz ID to update: " + quizData.getId());

        // Step 1: Get existing quiz
        Quiz existingQuiz = quizRepository.findById(quizData.getId())
                .orElseThrow(() -> new RuntimeException("Quiz not found with ID: " + quizData.getId()));

        System.out.println("✅ Found quiz: " + existingQuiz.getTitle());

        // Step 2: Update basic quiz info
        existingQuiz.setTitle(quizData.getTitle());
        existingQuiz.setDescription(quizData.getDescription());
        existingQuiz.setDuration(quizData.getDuration());
        existingQuiz.setPassingScore(quizData.getPassingScore());
        existingQuiz.setMaxAttempts(quizData.getMaxAttempts());

        // Step 3: Delete existing questions and options
        System.out.println("🗑️  Deleting old questions and options...");
        List<Question> oldQuestions = questionRepository.findByQuizId(existingQuiz.getId());

        for (Question oldQuestion : oldQuestions) {
            // Delete options first
            List<Option> oldOptions = optionRepository.findByQuestionId(oldQuestion.getId());
            if (!oldOptions.isEmpty()) {
                optionRepository.deleteAll(oldOptions);
            }
            // Then delete question
            questionRepository.delete(oldQuestion);
        }

        System.out.println("✅ Deleted " + oldQuestions.size() + " old questions");

        // Step 4: Add new questions
        Set<Question> newQuestions = new HashSet<>();

        if (quizData.getQuestions() != null && !quizData.getQuestions().isEmpty()) {
            System.out.println("📝 Adding " + quizData.getQuestions().size() + " new questions...");

            for (Question questionData : quizData.getQuestions()) {
                Question newQuestion = new Question();
                newQuestion.setQuiz(existingQuiz);
                newQuestion.setQuestionText(questionData.getQuestionText());
                newQuestion.setQuestionType(questionData.getQuestionType());
                newQuestion.setPoints(questionData.getPoints() != null ? questionData.getPoints() : 1);
                newQuestion.setCorrectAnswer(questionData.getCorrectAnswer());

                Question savedQuestion = questionRepository.save(newQuestion);
                System.out.println("   ✅ Saved question: " + savedQuestion.getQuestionText());

                // Add options for multiple choice
                if ("multiple_choice".equals(questionData.getQuestionType())
                        && questionData.getOptions() != null
                        && !questionData.getOptions().isEmpty()) {

                    Set<Option> newOptions = new HashSet<>();

                    for (Option optionData : questionData.getOptions()) {
                        Option newOption = new Option();
                        newOption.setQuestion(savedQuestion);
                        newOption.setOptionText(optionData.getOptionText());
                        newOption.setIsCorrect(optionData.getIsCorrect() != null ? optionData.getIsCorrect() : false);

                        Option savedOption = optionRepository.save(newOption);
                        newOptions.add(savedOption);
                        System.out.println("      ✓ Option: " + savedOption.getOptionText());
                    }

                    savedQuestion.setOptions(newOptions);
                }

                newQuestions.add(savedQuestion);
            }

            existingQuiz.setQuestions(newQuestions);
            existingQuiz.setTotalQuestions(newQuestions.size());
        } else {
            existingQuiz.setTotalQuestions(0);
            System.out.println("ℹ️  No new questions provided");
        }

        // Step 5: Save updated quiz
        Quiz updatedQuiz = quizRepository.save(existingQuiz);

        System.out.println("\n✅✅✅ QUIZ UPDATED SUCCESSFULLY!");
        System.out.println("   • Quiz ID: " + updatedQuiz.getId());
        System.out.println("   • Total Questions: " + updatedQuiz.getTotalQuestions());
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

        return updatedQuiz;
    }

    // ✅ DELETE QUIZ METHOD (already works)
    @Transactional
    public void deleteQuiz(Long quizId) {
        System.out.println("🗑️ Deleting quiz with ID: " + quizId);

        // Delete all questions and options first
        List<Question> questions = questionRepository.findByQuizId(quizId);
        for (Question question : questions) {
            List<Option> options = optionRepository.findByQuestionId(question.getId());
            optionRepository.deleteAll(options);
        }
        questionRepository.deleteAll(questions);

        // Delete the quiz
        quizRepository.deleteById(quizId);
        System.out.println("✅ Quiz deleted successfully");
    }

    // ✅ Other existing methods (keep these)
    public Optional<Quiz> getQuizById(Long quizId) {
        return quizRepository.findById(quizId);
    }

    public Optional<Quiz> getQuizByLesson(Long lessonId) {
        return quizRepository.findByLessonId(lessonId);
    }

    public List<Quiz> getQuizzesByCourse(Long courseId) {
        return quizRepository.findByCourseId(courseId);
    }

    // Start quiz attempt
    public QuizAttempt startQuizAttempt(Quiz quiz, User student) {
        Enrollment enrollment = enrollmentService.getEnrollment(student, quiz.getCourse())
                .orElseThrow(() -> new RuntimeException("Student not enrolled in this course"));

        List<QuizAttempt> previousAttempts = quizAttemptRepository.findByStudentAndQuiz(student, quiz);
        int attemptNumber = previousAttempts.size() + 1;

        if (attemptNumber > quiz.getMaxAttempts()) {
            throw new RuntimeException("Maximum attempts reached for this quiz");
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudent(student);
        attempt.setEnrollment(enrollment);
        attempt.setAttemptNumber(attemptNumber);
        attempt.setStatus("in_progress");

        return quizAttemptRepository.save(attempt);
    }

    // Submit quiz answers
    public QuizAttempt submitQuizAttempt(Long attemptId, Map<Long, String> answers) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Quiz attempt not found"));

        Quiz quiz = attempt.getQuiz();
        int totalScore = 0;
        int maxScore = 0;

        for (Question question : quiz.getQuestions()) {
            maxScore += question.getPoints();

            String studentAnswer = answers.get(question.getId());
            if (studentAnswer != null) {
                StudentAnswer studentAnswerEntity = new StudentAnswer();
                studentAnswerEntity.setQuizAttempt(attempt);
                studentAnswerEntity.setQuestion(question);
                studentAnswerEntity.setAnswerText(studentAnswer);

                boolean isCorrect = checkAnswer(question, studentAnswer);
                studentAnswerEntity.setIsCorrect(isCorrect);

                if (isCorrect) {
                    studentAnswerEntity.setPointsEarned(question.getPoints());
                    totalScore += question.getPoints();
                }

                studentAnswerRepository.save(studentAnswerEntity);
            }
        }

        int percentage = maxScore > 0 ? (totalScore * 100) / maxScore : 0;

        attempt.setScore(totalScore);
        attempt.setPercentage(percentage);
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setStatus(percentage >= quiz.getPassingScore() ? "passed" : "failed");

        QuizAttempt submittedAttempt = quizAttemptRepository.save(attempt);
        enrollmentService.updateProgress(attempt.getEnrollment().getId());

        return submittedAttempt;
    }

    private boolean checkAnswer(Question question, String studentAnswer) {
        if ("multiple_choice".equals(question.getQuestionType())) {
            for (Option option : question.getOptions()) {
                if (option.getOptionText().equals(studentAnswer) && option.getIsCorrect()) {
                    return true;
                }
            }
            return false;
        } else if ("true_false".equals(question.getQuestionType())) {
            return question.getCorrectAnswer().equalsIgnoreCase(studentAnswer);
        } else if ("short_answer".equals(question.getQuestionType())) {
            return question.getCorrectAnswer().trim().equalsIgnoreCase(studentAnswer.trim());
        }
        return false;
    }

    public Optional<QuizAttempt> getQuizAttemptById(Long attemptId) {
        return quizAttemptRepository.findById(attemptId);
    }

    public List<QuizAttempt> getQuizAttemptsByEnrollment(Enrollment enrollment) {
        return quizAttemptRepository.findByEnrollment(enrollment);
    }

    // In QuizService.java - Add these methods
    public List<Quiz> getQuizzesForStudentCourse(Long courseId, User student) {
        // Check if student is enrolled in the course
        Optional<Enrollment> enrollment = enrollmentService.getEnrollment(student,
                courseService.getCourseById(courseId).orElseThrow());

        if (enrollment.isEmpty()) {
            return Collections.emptyList();
        }

        // Return only quizzes for this course
        return quizRepository.findByCourseId(courseId);
    }

    public boolean canStudentAccessQuiz(Long quizId, User student) {
        Optional<Quiz> quiz = quizRepository.findById(quizId);
        if (quiz.isEmpty()) {
            return false;
        }

        // Check if student is enrolled in the quiz's course
        return enrollmentService.getEnrollment(student, quiz.get().getCourse()).isPresent();
    }
}