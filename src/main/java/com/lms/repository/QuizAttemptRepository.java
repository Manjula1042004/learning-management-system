// file: QuizAttemptRepository.java
package com.lms.repository;

import com.lms.entity.QuizAttempt;
import com.lms.entity.Quiz;
import com.lms.entity.User;
import com.lms.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByStudentAndQuiz(User student, Quiz quiz);
    List<QuizAttempt> findByEnrollment(Enrollment enrollment);
    Optional<QuizAttempt> findTopByStudentAndQuizOrderByAttemptNumberDesc(User student, Quiz quiz);

    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.enrollment = :enrollment")
    Long countByEnrollment(@Param("enrollment") Enrollment enrollment);

    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.enrollment = :enrollment AND qa.status = 'passed'")
    Long countPassedByEnrollment(@Param("enrollment") Enrollment enrollment);

    @Query("SELECT AVG(qa.percentage) FROM QuizAttempt qa WHERE qa.enrollment = :enrollment")
    Double getAverageQuizScore(@Param("enrollment") Enrollment enrollment);
}