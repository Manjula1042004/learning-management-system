// file: QuizAttempt.java
package com.lms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    private Integer attemptNumber;

    private Integer score; // in points

    private Integer percentage; // percentage score

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private String status; // "in_progress", "completed", "passed", "failed"

    @OneToMany(mappedBy = "quizAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentAnswer> studentAnswers = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        status = "in_progress";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public Enrollment getEnrollment() { return enrollment; }
    public void setEnrollment(Enrollment enrollment) { this.enrollment = enrollment; }
    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getPercentage() { return percentage; }
    public void setPercentage(Integer percentage) { this.percentage = percentage; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Set<StudentAnswer> getStudentAnswers() { return studentAnswers; }
    public void setStudentAnswers(Set<StudentAnswer> studentAnswers) { this.studentAnswers = studentAnswers; }

    // Helper methods
    public boolean isPassed() {
        return "passed".equals(status);
    }

    public boolean isCompleted() {
        return completedAt != null;
    }
}