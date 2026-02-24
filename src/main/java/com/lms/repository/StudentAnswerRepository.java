// file: StudentAnswerRepository.java
package com.lms.repository;

import com.lms.entity.StudentAnswer;
import com.lms.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    List<StudentAnswer> findByQuizAttempt(QuizAttempt quizAttempt);
}