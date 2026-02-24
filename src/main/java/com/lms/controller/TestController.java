package com.lms.controller;

import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.entity.LessonType;
import com.lms.service.CourseService;
import com.lms.service.LessonService;
import com.lms.service.CloudinaryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/test")
public class TestController {

    private final CourseService courseService;
    private final LessonService lessonService;
    private final CloudinaryService cloudinaryService;

    public TestController(CourseService courseService, LessonService lessonService,
                          CloudinaryService cloudinaryService) {
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("/test-pdf-upload/{courseId}")
    public String testPdfUpload(@PathVariable Long courseId, RedirectAttributes redirectAttributes) {
        try {
            Course course = courseService.getCourseById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            // Create a test PDF lesson with a known working URL
            // FIXED: Use the correct method signature
            Lesson lesson = lessonService.createSimpleLesson(
                    "Test PDF Lesson",
                    "This is a test PDF lesson to check if PDFs display properly",
                    15,
                    LessonType.PDF,
                    course,
                    null, // videoUrl
                    "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf" // Working PDF URL
            );

            redirectAttributes.addFlashAttribute("success",
                    "Test PDF lesson created! ID: " + lesson.getId() +
                            "<br>PDF URL: " + lesson.getResourceUrl());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/courses/" + courseId;
    }

    @PostMapping("/upload-pdf-test")
    @ResponseBody
    public String testCloudinaryPdfUpload(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("=== PDF UPLOAD TEST ===");
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());
            System.out.println("Content type: " + file.getContentType());

            // Upload to Cloudinary
            String url = cloudinaryService.uploadPdf(file);

            return "SUCCESS! PDF uploaded to: " + url +
                    "<br><a href='" + url + "' target='_blank'>Open PDF</a>" +
                    "<br><iframe src='" + url + "' width='100%' height='600'></iframe>";

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/test-delete")
    public String testDeletePage() {
        System.out.println("📄 Test delete page loaded");
        return "test"; // test.html template
    }

    @GetMapping("/test-pdf-display/{courseId}")
    public String testPdfDisplay(@PathVariable Long courseId) {
        return "redirect:/courses/" + courseId + "?test=pdf";
    }
}