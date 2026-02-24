package com.lms.repository;

import com.lms.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Find all questions for a specific quiz
     */
    List<Question> findByQuizId(Long quizId);

    /**
     * Delete all questions for a quiz
     */
    void deleteByQuizId(Long quizId);

    /**
     * Count questions for a quiz
     */
    long countByQuizId(Long quizId);
}