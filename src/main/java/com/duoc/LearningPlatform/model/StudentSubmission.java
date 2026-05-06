package com.duoc.LearningPlatform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "student_submissions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "evaluation_id"}, name = "uk_student_evaluation_submission")
        })
public class StudentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evaluation_id", nullable = false)
    private Long evaluationId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(length = 500)
    private String description;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Lob
    @Column(name = "file_content", nullable = false)
    private byte[] fileContent;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    protected StudentSubmission() {}

    public StudentSubmission(Long evaluationId, Long studentId, String description,
                             String fileName, String contentType, byte[] fileContent) {
        this.evaluationId = evaluationId;
        this.studentId = studentId;
        this.description = description;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileContent = fileContent;
    }

    @PrePersist
    public void onCreate() {
        submittedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getEvaluationId() {
        return evaluationId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getDescription() {
        return description;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}
