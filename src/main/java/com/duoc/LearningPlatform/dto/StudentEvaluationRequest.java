package com.duoc.LearningPlatform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class StudentEvaluationRequest {

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotNull(message = "score is required")
    @PositiveOrZero(message = "score must be positive or zero")
    private Integer score;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
