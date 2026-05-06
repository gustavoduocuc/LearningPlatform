package com.duoc.LearningPlatform.dto;

import com.duoc.LearningPlatform.model.StudentSubmission;

import java.time.LocalDateTime;

public class SubmissionResponse {

    private Long id;
    private Long evaluationId;
    private Long studentId;
    private String studentName;
    private String description;
    private String fileName;
    private String contentType;
    private LocalDateTime submittedAt;

    public static SubmissionResponse fromEntity(StudentSubmission submission, String studentName) {
        SubmissionResponse response = new SubmissionResponse();
        response.setId(submission.getId());
        response.setEvaluationId(submission.getEvaluationId());
        response.setStudentId(submission.getStudentId());
        response.setStudentName(studentName);
        response.setDescription(submission.getDescription());
        response.setFileName(submission.getFileName());
        response.setContentType(submission.getContentType());
        response.setSubmittedAt(submission.getSubmittedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(Long evaluationId) {
        this.evaluationId = evaluationId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
