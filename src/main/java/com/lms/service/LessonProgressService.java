package com.lms.service;

import com.lms.entity.LessonProgress;
import com.lms.entity.Enrollment;
import com.lms.entity.Lesson;
import com.lms.repository.LessonProgressRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LessonProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final EnrollmentService enrollmentService;

    public LessonProgressService(LessonProgressRepository lessonProgressRepository,
                                 EnrollmentService enrollmentService) {
        this.lessonProgressRepository = lessonProgressRepository;
        this.enrollmentService = enrollmentService;
    }

    public LessonProgress markLessonAsCompleted(Enrollment enrollment, Lesson lesson) {
        LessonProgress progress = lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson)
                .orElseThrow(() -> new RuntimeException("Lesson progress not found"));

        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        progress.setWatchTime((double) lesson.getDuration());

        LessonProgress savedProgress = lessonProgressRepository.save(progress);

        // Update overall course progress
        enrollmentService.updateProgress(enrollment.getId());

        return savedProgress;
    }

    public LessonProgress updateWatchTime(Enrollment enrollment, Lesson lesson, Double watchTime) {
        LessonProgress progress = lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson)
                .orElseThrow(() -> new RuntimeException("Lesson progress not found"));

        progress.setWatchTime(watchTime);

        // Mark as completed if watch time is at least 90% of lesson duration
        if (!progress.isCompleted() && watchTime >= (lesson.getDuration() * 0.9)) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }

        LessonProgress savedProgress = lessonProgressRepository.save(progress);

        // Update overall course progress
        enrollmentService.updateProgress(enrollment.getId());

        return savedProgress;
    }

    public Optional<LessonProgress> getLessonProgress(Enrollment enrollment, Lesson lesson) {
        return lessonProgressRepository.findByEnrollmentAndLesson(enrollment, lesson);
    }

    public List<LessonProgress> getProgressByEnrollment(Enrollment enrollment) {
        return lessonProgressRepository.findByEnrollment(enrollment);
    }

    public Double getCourseProgress(Enrollment enrollment) {
        return enrollment.getProgress();
    }
}