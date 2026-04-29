package com.duoc.LearningPlatform.dto;

import com.duoc.LearningPlatform.model.StudentEvaluation;

import java.time.LocalDateTime;

public class StudentEvaluationResponse {

    private Long id;
    private Long studentId;
    private Long evaluationId;
    private Integer score;
    private LocalDateTime evaluatedAt;
    private String studentName;
    private String evaluationName;

    public static StudentEvaluationResponse fromEntity(StudentEvaluation grade) {
        StudentEvaluationResponse response = new StudentEvaluationResponse();
        response.id = grade.getId();
        response.studentId = grade.getStudentId();
        response.evaluationId = grade.getEvaluationId();
        response.score = grade.getScore();
        response.evaluatedAt = grade.getEvaluatedAt();
        return response;
    }

    public static StudentEvaluationResponse fromEntity(StudentEvaluation grade, String studentName, String evaluationName) {
        StudentEvaluationResponse response = fromEntity(grade);
        response.studentName = studentName;
        response.evaluationName = evaluationName;
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(Long evaluationId) {
        this.evaluationId = evaluationId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEvaluationName() {
        return evaluationName;
    }

    public void setEvaluationName(String evaluationName) {
        this.evaluationName = evaluationName;
    }
}
