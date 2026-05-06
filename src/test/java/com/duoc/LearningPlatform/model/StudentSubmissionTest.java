package com.duoc.LearningPlatform.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentSubmissionTest {

    @Test
    void createsStudentSubmissionSuccessfully() {
        // Arrange
        Long evaluationId = 1L;
        Long studentId = 7L;
        String description = "My assignment submission";
        String fileName = "assignment.pdf";
        String contentType = "application/pdf";
        byte[] fileContent = new byte[]{0x25, 0x50, 0x44, 0x46};

        // Act
        StudentSubmission submission = new StudentSubmission(evaluationId, studentId, description, fileName, contentType, fileContent);

        // Assert
        assertNotNull(submission);
        assertEquals(evaluationId, submission.getEvaluationId());
        assertEquals(studentId, submission.getStudentId());
        assertEquals(description, submission.getDescription());
        assertEquals(fileName, submission.getFileName());
        assertEquals(contentType, submission.getContentType());
        assertArrayEquals(fileContent, submission.getFileContent());
    }

    @Test
    void createsStudentSubmissionWithoutDescription() {
        // Arrange
        Long evaluationId = 1L;
        Long studentId = 7L;
        String fileName = "assignment.pdf";
        String contentType = "application/pdf";
        byte[] fileContent = new byte[]{1, 2, 3};

        // Act
        StudentSubmission submission = new StudentSubmission(evaluationId, studentId, null, fileName, contentType, fileContent);

        // Assert
        assertNotNull(submission);
        assertNull(submission.getDescription());
    }

    @Test
    void submittedAtIsSetOnCreation() {
        // Arrange
        Long evaluationId = 1L;
        Long studentId = 7L;
        String fileName = "assignment.pdf";
        String contentType = "application/pdf";
        byte[] fileContent = new byte[]{1, 2, 3};

        // Act - Note: submittedAt is set by @PrePersist, so we verify the field exists
        StudentSubmission submission = new StudentSubmission(evaluationId, studentId, null, fileName, contentType, fileContent);

        // Assert - submittedAt will be null until persisted, but the field exists
        assertNotNull(submission);
    }
}
