package com.lms.service;

import com.lms.entity.*;
import com.lms.repository.*;
import com.lms.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private StudentAnswerRepository studentAnswerRepository;

    @Mock
    private EnrollmentService enrollmentService;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private QuizService quizService;

    @Captor
    private ArgumentCaptor<Quiz> quizCaptor;

    @Captor
    private ArgumentCaptor<Question> questionCaptor;

    private User instructor;
    private User student;
    private Course course;
    private Lesson lesson;
    private Quiz quiz;
    private Question question;
    private Option option1;
    private Option option2;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        instructor = TestDataFactory.createInstructor(1L);
        student = TestDataFactory.createStudent(2L);
        course = TestDataFactory.createCourse(1L, "Test Course", instructor);
        lesson = TestDataFactory.createLesson(1L, "Quiz Lesson", course, LessonType.QUIZ);

        quiz = TestDataFactory.createQuiz(1L, "Test Quiz", course, lesson);

        question = TestDataFactory.createQuestion(1L, "What is 2+2?", "multiple_choice", quiz);
        option1 = TestDataFactory.createOption(1L, "3", false, question);
        option2 = TestDataFactory.createOption(2L, "4", true, question);

        Set<Option> options = new HashSet<>();
        options.add(option1);
        options.add(option2);
        question.setOptions(options);

        Set<Question> questions = new HashSet<>();
        questions.add(question);
        quiz.setQuestions(questions);
        quiz.setTotalQuestions(1);

        enrollment = TestDataFactory.createEnrollment(1L, student, course);
    }

    @Test
    void createQuiz_ShouldCreateQuizWithQuestions() {
        // Arrange
        Quiz newQuiz = new Quiz();
        newQuiz.setTitle("New Quiz");
        newQuiz.setDescription("Description");
        newQuiz.setDuration(30);
        newQuiz.setPassingScore(70);
        newQuiz.setMaxAttempts(3);

        Question newQuestion = new Question();
        newQuestion.setQuestionText("Test Question");
        newQuestion.setQuestionType("multiple_choice");
        newQuestion.setPoints(1);

        Option newOption1 = new Option();
        newOption1.setOptionText("Answer 1");
        newOption1.setIsCorrect(false);

        Option newOption2 = new Option();
        newOption2.setOptionText("Answer 2");
        newOption2.setIsCorrect(true);

        Set<Option> newOptions = new HashSet<>();
        newOptions.add(newOption1);
        newOptions.add(newOption2);
        newQuestion.setOptions(newOptions);

        Set<Question> newQuestions = new HashSet<>();
        newQuestions.add(newQuestion);
        newQuiz.setQuestions(newQuestions);

        when(quizRepository.save(any(Quiz.class))).thenAnswer(i -> {
            Quiz saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(questionRepository.save(any(Question.class))).thenAnswer(i -> {
            Question saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(optionRepository.save(any(Option.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Quiz created = quizService.createQuiz(newQuiz, course, lesson);

        // Assert
        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(2L);
        assertThat(created.getTitle()).isEqualTo("New Quiz");
        assertThat(created.getTotalQuestions()).isEqualTo(1);

        verify(quizRepository, times(2)).save(any(Quiz.class));
        verify(questionRepository).save(any(Question.class));
        verify(optionRepository, times(2)).save(any(Option.class));
    }

    @Test
    void createQuiz_ShouldCreateEmptyQuiz_WhenNoQuestions() {
        // Arrange
        Quiz newQuiz = new Quiz();
        newQuiz.setTitle("Empty Quiz");
        newQuiz.setDescription("Description");
        newQuiz.setDuration(30);
        newQuiz.setPassingScore(70);
        newQuiz.setMaxAttempts(3);
        newQuiz.setQuestions(new HashSet<>());

        when(quizRepository.save(any(Quiz.class))).thenAnswer(i -> {
            Quiz saved = i.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // Act
        Quiz created = quizService.createQuiz(newQuiz, course, lesson);

        // Assert
        assertThat(created).isNotNull();
        assertThat(created.getTotalQuestions()).isEqualTo(0);
        verify(questionRepository, never()).save(any(Question.class));
    }



    @Test
    void deleteQuiz_ShouldDeleteQuizAndRelatedData() {
        // Arrange
        Long quizId = 1L;
        when(questionRepository.findByQuizId(quizId)).thenReturn(Arrays.asList(question));
        when(optionRepository.findByQuestionId(question.getId())).thenReturn(Arrays.asList(option1, option2));

        // Act
        quizService.deleteQuiz(quizId);

        // Assert
        verify(optionRepository).deleteAll(anyList());
        verify(questionRepository).deleteAll(anyList());
        verify(quizRepository).deleteById(quizId);
    }

    @Test
    void getQuizById_ShouldReturnQuiz_WhenExists() {
        // Arrange
        Long quizId = 1L;
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));

        // Act
        Optional<Quiz> result = quizService.getQuizById(quizId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(quizId);
        assertThat(result.get().getTitle()).isEqualTo("Test Quiz");
    }

    @Test
    void getQuizByLesson_ShouldReturnQuiz_WhenExists() {
        // Arrange
        Long lessonId = 1L;
        when(quizRepository.findByLessonId(lessonId)).thenReturn(Optional.of(quiz));

        // Act
        Optional<Quiz> result = quizService.getQuizByLesson(lessonId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getLesson().getId()).isEqualTo(lessonId);
    }

    @Test
    void getQuizzesByCourse_ShouldReturnCourseQuizzes() {
        // Arrange
        Long courseId = 1L;
        List<Quiz> quizzes = Arrays.asList(quiz);
        when(quizRepository.findByCourseId(courseId)).thenReturn(quizzes);

        // Act
        List<Quiz> result = quizService.getQuizzesByCourse(courseId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCourse().getId()).isEqualTo(courseId);
    }

    @Test
    void startQuizAttempt_ShouldCreateNewAttempt() {
        // Arrange
        when(enrollmentService.getEnrollment(student, course)).thenReturn(Optional.of(enrollment));
        when(quizAttemptRepository.findByStudentAndQuiz(student, quiz)).thenReturn(Arrays.asList());
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(i -> {
            QuizAttempt saved = i.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // Act
        QuizAttempt attempt = quizService.startQuizAttempt(quiz, student);

        // Assert
        assertThat(attempt).isNotNull();
        assertThat(attempt.getQuiz()).isEqualTo(quiz);
        assertThat(attempt.getStudent()).isEqualTo(student);
        assertThat(attempt.getEnrollment()).isEqualTo(enrollment);
        assertThat(attempt.getAttemptNumber()).isEqualTo(1);
        assertThat(attempt.getStatus()).isEqualTo("in_progress");
    }

    @Test
    void startQuizAttempt_ShouldThrowException_WhenStudentNotEnrolled() {
        // Arrange
        when(enrollmentService.getEnrollment(student, course)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> quizService.startQuizAttempt(quiz, student))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Student not enrolled in this course");
    }

    @Test
    void startQuizAttempt_ShouldThrowException_WhenMaxAttemptsReached() {
        // Arrange
        QuizAttempt attempt1 = new QuizAttempt();
        QuizAttempt attempt2 = new QuizAttempt();
        QuizAttempt attempt3 = new QuizAttempt();
        List<QuizAttempt> previousAttempts = Arrays.asList(attempt1, attempt2, attempt3);

        when(enrollmentService.getEnrollment(student, course)).thenReturn(Optional.of(enrollment));
        when(quizAttemptRepository.findByStudentAndQuiz(student, quiz)).thenReturn(previousAttempts);

        // Act & Assert
        assertThatThrownBy(() -> quizService.startQuizAttempt(quiz, student))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Maximum attempts reached for this quiz");
    }

    @Test
    void submitQuizAttempt_ShouldCalculateScore_ForMultipleChoice() {
        // Arrange
        QuizAttempt attempt = TestDataFactory.createQuizAttempt(1L, quiz, student, enrollment);
        attempt.setStatus("in_progress");

        Map<Long, String> answers = new HashMap<>();
        answers.put(question.getId(), "4"); // Correct answer

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(studentAnswerRepository.save(any(StudentAnswer.class))).thenAnswer(i -> i.getArgument(0));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        QuizAttempt result = quizService.submitQuizAttempt(1L, answers);

        // Assert
        assertThat(result.getScore()).isEqualTo(1);
        assertThat(result.getPercentage()).isEqualTo(100);
        assertThat(result.getStatus()).isEqualTo("passed");
        assertThat(result.getCompletedAt()).isNotNull();

        verify(studentAnswerRepository, times(1)).save(any(StudentAnswer.class));
        verify(quizAttemptRepository).save(attempt);
    }

    @Test
    void submitQuizAttempt_ShouldCalculateScore_ForIncorrectAnswer() {
        // Arrange
        QuizAttempt attempt = TestDataFactory.createQuizAttempt(1L, quiz, student, enrollment);
        attempt.setStatus("in_progress");

        Map<Long, String> answers = new HashMap<>();
        answers.put(question.getId(), "3"); // Wrong answer

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(studentAnswerRepository.save(any(StudentAnswer.class))).thenAnswer(i -> i.getArgument(0));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        QuizAttempt result = quizService.submitQuizAttempt(1L, answers);

        // Assert
        assertThat(result.getScore()).isEqualTo(0);
        assertThat(result.getPercentage()).isEqualTo(0);
        assertThat(result.getStatus()).isEqualTo("failed");
    }

    @Test
    void getQuizAttemptById_ShouldReturnAttempt_WhenExists() {
        // Arrange
        Long attemptId = 1L;
        QuizAttempt attempt = TestDataFactory.createQuizAttempt(attemptId, quiz, student, enrollment);
        when(quizAttemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));

        // Act
        Optional<QuizAttempt> result = quizService.getQuizAttemptById(attemptId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(attemptId);
    }

    @Test
    void getQuizAttemptsByEnrollment_ShouldReturnAttempts() {
        // Arrange
        List<QuizAttempt> attempts = Arrays.asList(
                TestDataFactory.createQuizAttempt(1L, quiz, student, enrollment)
        );
        when(quizAttemptRepository.findByEnrollment(enrollment)).thenReturn(attempts);

        // Act
        List<QuizAttempt> result = quizService.getQuizAttemptsByEnrollment(enrollment);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void canStudentAccessQuiz_ShouldReturnTrue_WhenStudentEnrolled() {
        // Arrange
        Long quizId = 1L;
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(enrollmentService.getEnrollment(student, course)).thenReturn(Optional.of(enrollment));

        // Act
        boolean result = quizService.canStudentAccessQuiz(quizId, student);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void canStudentAccessQuiz_ShouldReturnFalse_WhenStudentNotEnrolled() {
        // Arrange
        Long quizId = 1L;
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(enrollmentService.getEnrollment(student, course)).thenReturn(Optional.empty());

        // Act
        boolean result = quizService.canStudentAccessQuiz(quizId, student);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void canStudentAccessQuiz_ShouldReturnFalse_WhenQuizNotFound() {
        // Arrange
        Long quizId = 99L;
        when(quizRepository.findById(quizId)).thenReturn(Optional.empty());

        // Act
        boolean result = quizService.canStudentAccessQuiz(quizId, student);

        // Assert
        assertThat(result).isFalse();
    }
    @Test
    void updateQuiz_ShouldUpdateQuizAndReplaceQuestions() {
        // Arrange
        when(quizRepository.findById(quiz.getId())).thenReturn(Optional.of(quiz));
        when(questionRepository.findByQuizId(quiz.getId())).thenReturn(Arrays.asList(question));
        when(optionRepository.findByQuestionId(question.getId())).thenReturn(Arrays.asList(option1, option2));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(i -> i.getArgument(0));
        when(questionRepository.save(any(Question.class))).thenAnswer(i -> i.getArgument(0));
        when(optionRepository.save(any(Option.class))).thenAnswer(i -> i.getArgument(0));

        Quiz updatedQuizData = new Quiz();
        updatedQuizData.setId(quiz.getId());
        updatedQuizData.setTitle("Updated Quiz");
        updatedQuizData.setDescription("Updated Description");
        updatedQuizData.setDuration(45);
        updatedQuizData.setPassingScore(80);
        updatedQuizData.setMaxAttempts(5);

        Question updatedQuestion = new Question();
        updatedQuestion.setQuestionText("Updated Question?");
        updatedQuestion.setQuestionType("multiple_choice");
        updatedQuestion.setPoints(2);

        Option updatedOption = new Option();
        updatedOption.setOptionText("Correct Answer");
        updatedOption.setIsCorrect(true);

        Set<Option> updatedOptions = new HashSet<>();
        updatedOptions.add(updatedOption);
        updatedQuestion.setOptions(updatedOptions);

        Set<Question> updatedQuestions = new HashSet<>();
        updatedQuestions.add(updatedQuestion);
        updatedQuizData.setQuestions(updatedQuestions);

        // Act
        Quiz result = quizService.updateQuiz(updatedQuizData);

        // Assert
        assertThat(result.getTitle()).isEqualTo("Updated Quiz");
        assertThat(result.getDuration()).isEqualTo(45);
        assertThat(result.getPassingScore()).isEqualTo(80);
        assertThat(result.getMaxAttempts()).isEqualTo(5);
        assertThat(result.getTotalQuestions()).isEqualTo(1);

        // Fix: Verify that questions were deleted properly (not deleteByQuizId)
        verify(questionRepository, atLeast(1)).delete(any(Question.class));
        verify(optionRepository, atLeast(1)).deleteAll(anyList());
    }
}