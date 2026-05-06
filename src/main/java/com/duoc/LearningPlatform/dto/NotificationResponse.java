package com.duoc.LearningPlatform.dto;

import com.duoc.LearningPlatform.model.Notification;
import com.duoc.LearningPlatform.model.NotificationType;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private Long recipientId;
    private String title;
    private String message;
    private NotificationType notificationType;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private Long relatedCourseId;
    private Long relatedEvaluationId;
    private Long relatedPaymentId;

    public static NotificationResponse fromEntity(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.id = notification.getId();
        response.recipientId = notification.getRecipientId();
        response.title = notification.getTitle();
        response.message = notification.getMessage();
        response.notificationType = notification.getNotificationType();
        response.read = notification.isRead();
        response.createdAt = notification.getCreatedAt();
        response.readAt = notification.getReadAt();
        response.relatedCourseId = notification.getRelatedCourseId();
        response.relatedEvaluationId = notification.getRelatedEvaluationId();
        response.relatedPaymentId = notification.getRelatedPaymentId();
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
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
