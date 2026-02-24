package com.lms.repository;

import com.lms.entity.Enrollment;
import com.lms.entity.EnrollmentStatus;
import com.lms.entity.User;
import com.lms.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


    @Repository
    public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
        Optional<Enrollment> findByStudentAndCourse(User student, Course course);
        List<Enrollment> findByStudent(User student);
        List<Enrollment> findByCourse(Course course);
        List<Enrollment> findByStudentAndStatus(User student, EnrollmentStatus status);
        Boolean existsByStudentAndCourse(User student, Course course);

        @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course = :course")
        Long countByCourse(@Param("course") Course course);

        @Query("SELECT e FROM Enrollment e WHERE e.course.instructor = :instructor")
        List<Enrollment> findByInstructor(@Param("instructor") User instructor);

        @Query("SELECT e FROM Enrollment e WHERE e.course.instructor = :instructor AND e.status = :status")
        List<Enrollment> findByInstructorAndStatus(@Param("instructor") User instructor,
                                                   @Param("status") EnrollmentStatus status);
    }
