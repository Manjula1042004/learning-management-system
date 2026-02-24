package com.lms.service;

import com.lms.entity.Course;
import com.lms.entity.User;
import com.lms.repository.CourseRepository;
import org.springframework.stereotype.Service;

@Service
public class DemoService {

    private final CourseRepository courseRepository;

    public DemoService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * Creates demo courses without requiring file uploads
     * This is only for DataLoader/development purposes
     */
    public Course createDemoCourse(String title, String description, Double price, User instructor) {
        Course course = new Course(title, description, price, instructor);

        // Use placeholder thumbnail for demo courses
        course.setThumbnailUrl("https://ucarecdn.com/be8d3d93-3acf-4b79-9756-9da015c1c2b6/book_placeholder.png");

        return courseRepository.save(course);
    }
}