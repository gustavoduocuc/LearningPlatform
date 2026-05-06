package com.duoc.LearningPlatform.service;

import com.duoc.LearningPlatform.dto.NotificationRequest;
import com.duoc.LearningPlatform.exception.ResourceNotFoundException;
import com.duoc.LearningPlatform.model.Notification;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.repository.EvaluationRepository;
import com.duoc.LearningPlatform.repository.NotificationRepository;
import com.duoc.LearningPlatform.repository.PaymentRepository;
import com.duoc.LearningPlatform.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final EvaluationRepository evaluationRepository;
    private final PaymentRepository paymentRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            CourseService courseService,
            EvaluationRepository evaluationRepository,
            PaymentRepository paymentRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.courseService = courseService;
        this.evaluationRepository = evaluationRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Notification create(NotificationRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Notification title cannot be empty");
        }
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("Notification message cannot be empty");
        }
        if (request.getNotificationType() == null) {
            throw new IllegalArgumentException("Notification type cannot be null");
        }

        Long recipientId = request.getRecipientId();
        userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("User", recipientId));

        validateOptionalRelatedCourse(request.getRelatedCourseId());
        validateOptionalRelatedEvaluation(request.getRelatedEvaluationId());
        validateOptionalRelatedPayment(request.getRelatedPaymentId());

        Notification notification = new Notification(
                recipientId,
                request.getTitle(),
                request.getMessage(),
                request.getNotificationType(),
                request.getRelatedCourseId(),
                request.getRelatedEvaluationId(),
                request.getRelatedPaymentId());
        return notificationRepository.save(notification);
    }

    private void validateOptionalRelatedCourse(Long relatedCourseId) {
        if (relatedCourseId == null) {
            return;
        }
        courseService.getCourse(relatedCourseId);
    }

    private void validateOptionalRelatedEvaluation(Long relatedEvaluationId) {
        if (relatedEvaluationId == null) {
            return;
        }
        evaluationRepository.findById(relatedEvaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation", relatedEvaluationId));
    }

    private void validateOptionalRelatedPayment(Long relatedPaymentId) {
        if (relatedPaymentId == null) {
            return;
        }
        paymentRepository.findById(relatedPaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", relatedPaymentId));
    }

    public List<Notification> findForList(Long actorUserId, Role role) {
        if (role == Role.ADMIN) {
            return notificationRepository.findAllByOrderByCreatedAtDesc();
        }
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(actorUserId);
    }

    public List<Notification> findForMe(Long actorUserId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(actorUserId);
    }

    public Notification getForActor(Long notificationId, Long actorUserId, Role actorRole) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        assertActorCanAccessNotification(notification, actorUserId, actorRole);
        return notification;
    }

    @Transactional
    public Notification markReadForActor(Long notificationId, Long actorUserId, Role actorRole) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        assertActorCanAccessNotification(notification, actorUserId, actorRole);
        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    @Transactional
    public void deleteForActor(Long notificationId, Long actorUserId, Role actorRole) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        assertActorCanAccessNotification(notification, actorUserId, actorRole);
        notificationRepository.delete(notification);
    }

    private void assertActorCanAccessNotification(
            Notification notification, Long actorUserId, Role actorRole) {
        if (actorRole == Role.ADMIN) {
            return;
        }
        if (notification.getRecipientId().equals(actorUserId)) {
            return;
        }
        throw new AccessDeniedException("Cannot access this notification");
    }
}
