package com.duoc.LearningPlatform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 4000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "related_course_id")
    private Long relatedCourseId;

    @Column(name = "related_evaluation_id")
    private Long relatedEvaluationId;

    @Column(name = "related_payment_id")
    private Long relatedPaymentId;

    protected Notification() {}

    public Notification(
            Long recipientId,
            String title,
            String message,
            NotificationType notificationType,
            Long relatedCourseId,
            Long relatedEvaluationId,
            Long relatedPaymentId) {
        validateRecipientId(recipientId);
        validateTitle(title);
        validateMessage(message);
        if (notificationType == null) {
            throw new IllegalArgumentException("Notification type cannot be null");
        }
        this.recipientId = recipientId;
        this.title = title.trim();
        this.message = message.trim();
        this.notificationType = notificationType;
        this.read = false;
        this.relatedCourseId = relatedCourseId;
        this.relatedEvaluationId = relatedEvaluationId;
        this.relatedPaymentId = relatedPaymentId;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void markAsRead() {
        if (read) {
            return;
        }
        read = true;
        readAt = LocalDateTime.now();
    }

    private void validateRecipientId(Long recipientId) {
        if (recipientId == null) {
            throw new IllegalArgumentException("Recipient ID cannot be null");
        }
        if (recipientId <= 0) {
            throw new IllegalArgumentException("Recipient ID must be positive");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Notification title cannot be empty");
        }
    }

    private void validateMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Notification message cannot be empty");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public Long getRelatedCourseId() {
        return relatedCourseId;
    }

    public Long getRelatedEvaluationId() {
        return relatedEvaluationId;
    }

    public Long getRelatedPaymentId() {
        return relatedPaymentId;
    }
}
