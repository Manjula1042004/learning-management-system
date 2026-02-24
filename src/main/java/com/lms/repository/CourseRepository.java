package com.lms.repository;

import com.lms.entity.Course;
import com.lms.entity.CourseStatus;
import com.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructor(User instructor);
    List<Course> findByStatus(CourseStatus status);
    List<Course> findByInstructorAndStatus(User instructor, CourseStatus status);

    @Query("SELECT c FROM Course c WHERE c.status = 'APPROVED' AND " +
            "(LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Course> searchApprovedCourses(@Param("query") String query);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.instructor = :instructor")
    Long countByInstructor(@Param("instructor") User instructor);
}