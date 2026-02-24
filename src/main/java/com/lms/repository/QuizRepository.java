package com.lms.repository;

import com.lms.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    /**
     * Find quiz by lesson ID
     */
    Optional<Quiz> findByLessonId(Long lessonId);

    /**
     * Find all quizzes by course ID
     */
    List<Quiz> findByCourseId(Long courseId);

    /**
     * Find all quizzes by course ID and ordered by creation date
     */
    List<Quiz> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    /**
     * Delete quiz by lesson ID
     */
    void deleteByLessonId(Long lessonId);

    /**
     * Check if quiz exists for a lesson
     */
    boolean existsByLessonId(Long lessonId);

    /**
     * Count quizzes by course ID
     */
    long countByCourseId(Long courseId);
}