package com.lms.repository;

import com.lms.entity.LessonProgress;
import com.lms.entity.Enrollment;
import com.lms.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByEnrollmentAndLesson(Enrollment enrollment, Lesson lesson);
    List<LessonProgress> findByEnrollment(Enrollment enrollment);

    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.enrollment = :enrollment AND lp.completed = true")
    Long countCompletedLessonsByEnrollment(@Param("enrollment") Enrollment enrollment);

    @Query("SELECT lp FROM LessonProgress lp WHERE lp.enrollment = :enrollment AND lp.lesson.course = :course")
    List<LessonProgress> findByEnrollmentAndCourse(@Param("enrollment") Enrollment enrollment,
                                                   @Param("course") com.lms.entity.Course course);

    // ✅ ADD THIS METHOD - Delete all progress records for a specific lesson
    @Modifying
    @Transactional
    @Query("DELETE FROM LessonProgress lp WHERE lp.lesson.id = :lessonId")
    int deleteByLessonId(@Param("lessonId") Long lessonId);
}