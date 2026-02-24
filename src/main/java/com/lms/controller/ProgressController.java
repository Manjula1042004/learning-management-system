package com.lms.controller;

import com.lms.entity.User;
import com.lms.entity.Lesson;
import com.lms.entity.Enrollment;
import com.lms.service.UserService;
import com.lms.service.LessonService;
import com.lms.service.EnrollmentService;
import com.lms.service.LessonProgressService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/progress")
public class ProgressController {

    private final UserService userService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;
    private final LessonProgressService lessonProgressService;

    public ProgressController(UserService userService, LessonService lessonService,
                              EnrollmentService enrollmentService, LessonProgressService lessonProgressService) {
        this.userService = userService;
        this.lessonService = lessonService;
        this.enrollmentService = enrollmentService;
        this.lessonProgressService = lessonProgressService;
    }


    @PostMapping("/update-watch-time/{lessonId}")
    @ResponseBody
    public String updateWatchTime(@PathVariable Long lessonId,
                                  @RequestParam Double watchTime,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        User student = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Lesson lesson = lessonService.getLessonById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        Enrollment enrollment = enrollmentService.getEnrollment(student, lesson.getCourse())
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        lessonProgressService.updateWatchTime(enrollment, lesson, watchTime);

        return "OK";
    }

    // In ProgressController.java
    @PostMapping("/complete/{lessonId}")
    public String markLessonAsCompleted(@PathVariable Long lessonId,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        User student = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Lesson lesson = lessonService.getLessonById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));  // ✅ Fixed

        Enrollment enrollment = enrollmentService.getEnrollment(student, lesson.getCourse())
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        lessonProgressService.markLessonAsCompleted(enrollment, lesson);

        return "redirect:/courses/" + lesson.getCourse().getId() + "?lesson=" + lessonId + "&completed=true";
    }
}