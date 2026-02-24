package com.lms.repository;

import com.lms.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {

    /**
     * Find all options for a specific question
     */
    List<Option> findByQuestionId(Long questionId);

    /**
     * Delete all options for a question
     */
    void deleteByQuestionId(Long questionId);

    /**
     * Count options for a question
     */
    long countByQuestionId(Long questionId);
}