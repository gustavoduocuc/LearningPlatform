package com.duoc.LearningPlatform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private String name;

    @Column(name = "maximum_score", nullable = false)
    private int maximumScore;

    @Column(name = "application_date", nullable = false)
    private LocalDateTime applicationDate;

    protected Evaluation() {}

    public Evaluation(Long courseId, String name, int maximumScore, LocalDateTime applicationDate) {
        validateCourseId(courseId);
        validateName(name);
        validateMaximumScore(maximumScore);
        validateApplicationDate(applicationDate);
        this.courseId = courseId;
        this.name = name;
        this.maximumScore = maximumScore;
        this.applicationDate = applicationDate;
    }

    public void updateDetails(String name, int maximumScore, LocalDateTime applicationDate) {
        validateName(name);
        validateMaximumScore(maximumScore);
        validateApplicationDate(applicationDate);
        this.name = name;
        this.maximumScore = maximumScore;
        this.applicationDate = applicationDate;
    }

    private void validateCourseId(Long courseId) {
        if (courseId == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }
        if (courseId <= 0) {
            throw new IllegalArgumentException("Course ID must be positive");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Evaluation name cannot be empty");
        }
    }

    private void validateMaximumScore(int maximumScore) {
        if (maximumScore <= 0) {
            throw new IllegalArgumentException("Maximum score must be positive");
        }
    }

    private void validateApplicationDate(LocalDateTime applicationDate) {
        if (applicationDate == null) {
            throw new IllegalArgumentException("Application date cannot be null");
        }
        if (applicationDate.isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new IllegalArgumentException("Application date cannot be in the past");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getName() {
        return name;
    }

    public int getMaximumScore() {
        return maximumScore;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }
}
