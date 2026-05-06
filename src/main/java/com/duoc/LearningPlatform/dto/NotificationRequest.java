package com.duoc.LearningPlatform.dto;

import com.duoc.LearningPlatform.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NotificationRequest {

    @NotNull(message = "recipientId is required")
    private Long recipientId;

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "message is required")
    private String message;

    @NotNull(message = "notificationType is required")
    private NotificationType notificationType;

    private Long relatedCourseId;
    private Long relatedEvaluationId;
    private Long relatedPaymentId;

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public Long getRelatedCourseId() {
        return relatedCourseId;
    }

    public void setRelatedCourseId(Long relatedCourseId) {
        this.relatedCourseId = relatedCourseId;
    }

    public Long getRelatedEvaluationId() {
        return relatedEvaluationId;
    }

    public void setRelatedEvaluationId(Long relatedEvaluationId) {
        this.relatedEvaluationId = relatedEvaluationId;
    }

    public Long getRelatedPaymentId() {
        return relatedPaymentId;
    }

    public void setRelatedPaymentId(Long relatedPaymentId) {
        this.relatedPaymentId = relatedPaymentId;
    }
}
