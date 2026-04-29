package com.duoc.LearningPlatform.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class EvaluationRequest {

    @NotNull(message = "courseId is required")
    private Long courseId;

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "maximumScore is required")
    @Positive(message = "maximumScore must be positive")
    private Integer maximumScore;

    @NotNull(message = "applicationDate is required")
    @FutureOrPresent(message = "applicationDate must be in the present or future")
    private LocalDateTime applicationDate;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaximumScore() {
        return maximumScore;
    }

    public void setMaximumScore(Integer maximumScore) {
        this.maximumScore = maximumScore;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }
}
