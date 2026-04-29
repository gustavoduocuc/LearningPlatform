package com.duoc.LearningPlatform.dto;

import com.duoc.LearningPlatform.model.Evaluation;

import java.time.LocalDateTime;

public class EvaluationResponse {

    private Long id;
    private Long courseId;
    private String name;
    private int maximumScore;
    private LocalDateTime applicationDate;
    private String courseTitle;

    public static EvaluationResponse fromEntity(Evaluation evaluation) {
        EvaluationResponse response = new EvaluationResponse();
        response.id = evaluation.getId();
        response.courseId = evaluation.getCourseId();
        response.name = evaluation.getName();
        response.maximumScore = evaluation.getMaximumScore();
        response.applicationDate = evaluation.getApplicationDate();
        return response;
    }

    public static EvaluationResponse fromEntity(Evaluation evaluation, String courseTitle) {
        EvaluationResponse response = fromEntity(evaluation);
        response.courseTitle = courseTitle;
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public int getMaximumScore() {
        return maximumScore;
    }

    public void setMaximumScore(int maximumScore) {
        this.maximumScore = maximumScore;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }
}
