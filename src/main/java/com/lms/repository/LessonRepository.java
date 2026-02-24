package com.lms.repository;

import com.lms.entity.Lesson;
import com.lms.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    // ✅ FIND BY COURSE (ORDERED)
    List<Lesson> findByCourseOrderByOrderIndexAsc(Course course);

    // ✅ FIND BY COURSE AND ORDER INDEX
    Optional<Lesson> findByCourseAndOrderIndex(Course course, Integer orderIndex);

    // ✅ GET MAX ORDER INDEX FOR A COURSE
    @Query("SELECT MAX(l.orderIndex) FROM Lesson l WHERE l.course = :course")
    Integer findMaxOrderIndexByCourse(@Param("course") Course course);

    // ✅ FIND NEXT LESSONS (for pagination/navigation)
    @Query("SELECT l FROM Lesson l WHERE l.course = :course AND l.orderIndex > :currentIndex ORDER BY l.orderIndex ASC")
    List<Lesson> findNextLessons(@Param("course") Course course, @Param("currentIndex") Integer currentIndex);

    // ✅ FIND ALL LESSONS (for admin)
    // This is already provided by JpaRepository


        // Custom query if needed
        @Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId ORDER BY l.orderIndex ASC")
        List<Lesson> findByCourseIdOrderByOrderIndex(@Param("courseId") Long courseId);

}