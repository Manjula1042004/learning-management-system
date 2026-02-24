package com.lms.repository;

import com.lms.entity.Media;
import com.lms.entity.User;
import com.lms.entity.Course;
import com.lms.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    List<Media> findByCourse(Course course);

    List<Media> findByLesson(Lesson lesson);

    List<Media> findByUploadedBy(User uploadedBy);

    List<Media> findByCourseAndFileType(Course course, String fileType);

    @Query("SELECT m FROM Media m WHERE m.course = :course AND m.uploadedBy = :user")
    List<Media> findByCourseAndUploader(@Param("course") Course course, @Param("user") User user);

    Optional<Media> findByStoredFileName(String storedFileName);

    @Query("SELECT COUNT(m) FROM Media m WHERE m.course = :course")
    Long countByCourse(@Param("course") Course course);

    @Query("SELECT SUM(m.fileSize) FROM Media m WHERE m.course = :course")
    Long getTotalFileSizeByCourse(@Param("course") Course course);

    @Query("SELECT m FROM Media m WHERE m.course = :course AND m.fileType = :fileType ORDER BY m.uploadedAt DESC")
    List<Media> findRecentByCourseAndType(@Param("course") Course course, @Param("fileType") String fileType);
}