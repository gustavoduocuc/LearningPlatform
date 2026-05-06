package com.duoc.LearningPlatform.service;

import com.duoc.LearningPlatform.dto.NotificationRequest;
import com.duoc.LearningPlatform.exception.ResourceNotFoundException;
import com.duoc.LearningPlatform.model.Course;
import com.duoc.LearningPlatform.model.Evaluation;
import com.duoc.LearningPlatform.model.Notification;
import com.duoc.LearningPlatform.model.NotificationType;
import com.duoc.LearningPlatform.model.Payment;
import com.duoc.LearningPlatform.model.PaymentMethod;
import com.duoc.LearningPlatform.model.PaymentStatus;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.repository.EvaluationRepository;
import com.duoc.LearningPlatform.repository.NotificationRepository;
import com.duoc.LearningPlatform.repository.PaymentRepository;
import com.duoc.LearningPlatform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final long recipientUserId = 2L;
    private static final long otherUserId = 3L;
    private static final long adminActorId = 1L;
    private static final long persistedNotificationId = 100L;
    private static final long relatedCourseId = 10L;
    private static final long relatedEvaluationId = 20L;
    private static final long relatedPaymentId = 30L;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void persistsNotificationWhenRecipientExistsAndNoRelatedEntities() {
        User recipient = studentNamedAna(recipientUserId);
        when(userRepository.findById(recipientUserId)).thenReturn(Optional.of(recipient));
        stubSaveAssignsId(persistedNotificationId);

        NotificationRequest request = welcomeGeneralNotificationRequest(recipientUserId);

        Notification result = notificationService.create(request);

        assertEquals(persistedNotificationId, result.getId());
        assertEquals(recipientUserId, result.getRecipientId());
        assertEquals(NotificationType.GENERAL, result.getNotificationType());
        assertFalse(result.isRead());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void rejectsCreateWhenRecipientUserDoesNotExist() {
        when(userRepository.findById(recipientUserId)).thenReturn(Optional.empty());

        NotificationRequest request = minimalGeneralNotificationRequest(recipientUserId);

        assertThrows(ResourceNotFoundException.class, () -> notificationService.create(request));

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void rejectsCreateWhenTitleIsBlank() {
        NotificationRequest request = minimalGeneralNotificationRequest(recipientUserId);
        request.setTitle("   ");

        assertThrows(IllegalArgumentException.class, () -> notificationService.create(request));

        verify(userRepository, never()).findById(any());
    }

    @Test
    void rejectsCreateWhenMessageIsBlank() {
        NotificationRequest request = minimalGeneralNotificationRequest(recipientUserId);
        request.setMessage("");

        assertThrows(IllegalArgumentException.class, () -> notificationService.create(request));

        verify(userRepository, never()).findById(any());
    }

    @Test
    void rejectsCreateWhenRelatedCourseDoesNotExist() {
        User recipient = studentNamedAna(recipientUserId);
        when(userRepository.findById(recipientUserId)).thenReturn(Optional.of(recipient));
        when(courseService.getCourse(relatedCourseId)).thenThrow(new ResourceNotFoundException("Course", relatedCourseId));

        NotificationRequest request = notificationRequestWithRelatedCourse(
                recipientUserId, NotificationType.NEW_COURSE, relatedCourseId);

        assertThrows(ResourceNotFoundException.class, () -> notificationService.create(request));

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void rejectsCreateWhenRelatedEvaluationDoesNotExist() {
        User recipient = studentNamedAna(recipientUserId);
        when(userRepository.findById(recipientUserId)).thenReturn(Optional.of(recipient));
        when(evaluationRepository.findById(relatedEvaluationId)).thenReturn(Optional.empty());

        NotificationRequest request = notificationRequestWithRelatedEvaluation(
                recipientUserId, NotificationType.EVALUATION_CREATED, relatedEvaluationId);

        assertThrows(ResourceNotFoundException.class, () -> notificationService.create(request));

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void rejectsCreateWhenRelatedPaymentDoesNotExist() {
        User recipient = studentNamedAna(recipientUserId);
        when(userRepository.findById(recipientUserId)).thenReturn(Optional.of(recipient));
        when(paymentRepository.findById(relatedPaymentId)).thenReturn(Optional.empty());

        NotificationRequest request = notificationRequestWithRelatedPayment(
                recipientUserId, NotificationType.PAYMENT_APPROVED, relatedPaymentId);

        assertThrows(ResourceNotFoundException.class, () -> notificationService.create(request));

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void listsOnlyNotificationsAddressedToStudentWhenActorIsStudent() {
        Notification forRecipient = unreadGeneralNotificationForRecipient(recipientUserId);
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientUserId)).thenReturn(List.of(forRecipient));

        List<Notification> result = notificationService.findForList(recipientUserId, Role.STUDENT);

        assertEquals(1, result.size());
        assertEquals(recipientUserId, result.get(0).getRecipientId());
    }

    @Test
    void listsEveryNotificationWhenActorIsAdmin() {
        Notification first = unreadGeneralNotificationForRecipient(recipientUserId);
        Notification second = unreadGeneralNotificationForRecipient(otherUserId);
        when(notificationRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(first, second));

        List<Notification> result = notificationService.findForList(adminActorId, Role.ADMIN);

        assertEquals(2, result.size());
    }

    @Test
    void deniesGetWhenActorIsNotRecipientAndNotAdmin() {
        Notification notification = persistedUnreadNotification(persistedNotificationId, recipientUserId);
        when(notificationRepository.findById(persistedNotificationId)).thenReturn(Optional.of(notification));

        assertThrows(AccessDeniedException.class,
                () -> notificationService.getForActor(persistedNotificationId, otherUserId, Role.STUDENT));
    }

    @Test
    void returnsNotificationWhenActorIsRecipient() {
        Notification notification = persistedUnreadNotification(persistedNotificationId, recipientUserId);
        when(notificationRepository.findById(persistedNotificationId)).thenReturn(Optional.of(notification));

        Notification result = notificationService.getForActor(persistedNotificationId, recipientUserId, Role.STUDENT);

        assertEquals(persistedNotificationId, result.getId());
    }

    @Test
    void returnsNotificationWhenActorIsAdmin() {
        Notification notification = persistedUnreadNotification(persistedNotificationId, recipientUserId);
        when(notificationRepository.findById(persistedNotificationId)).thenReturn(Optional.of(notification));

        Notification result = notificationService.getForActor(persistedNotificationId, adminActorId, Role.ADMIN);

        assertEquals(persistedNotificationId, result.getId());
    }

    @Test
    void marksNotificationReadWhenActorIsRecipient() {
        Notification notification = persistedUnreadNotification(persistedNotificationId, recipientUserId);
        when(notificationRepository.findById(persistedNotificationId)).thenReturn(Optional.of(notification));
        stubSaveReturnsPersistedArgument();

        Notification result = notificationService.markReadForActor(
                persistedNotificationId, recipientUserId, Role.STUDENT);

        assertTrue(result.isRead());
        assertNotNull(result.getReadAt());
        verify(notificationRepository).save(notification);
    }

    @Test
    void deniesMarkReadWhenActorIsNotRecipientAndNotAdmin() {
        Notification notification = persistedUnreadNotification(persistedNotificationId, recipientUserId);
        when(notificationRepository.findById(persistedNotificationId)).thenReturn(Optional.of(notification));

        assertThrows(AccessDeniedException.class,
                () -> notificationService.markReadForActor(persistedNotificationId, otherUserId, Role.PROFESSOR));

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markReadLeavesReadTimestampUnchangedWhenAlreadyRead() {
        Notification notification = persistedUnreadNotification(persistedNotificationId, recipientUserId);
        notification.markAsRead();
        LocalDateTime readAtAfterFirstMark = notification.getReadAt();
        when(notificationRepository.findById(persistedNotificationId)).thenReturn(Optional.of(notification));
        stubSaveReturnsPersistedArgument();

        Notification result = notificationService.markReadForActor(
                persistedNotificationId, recipientUserId, Role.STUDENT);

        assertTrue(result.isRead());
        assertEquals(readAtAfterFirstMark, result.getReadAt());
    }

    @Test
    void deletesNotificationWhenActorIsAdmin() {
        Notification notification = persistedUnreadNotification(persistedNotificationId, recipientUserId);
        when(notificationRepository.findById(persistedNotificationId)).thenReturn(Optional.of(notification));

        notificationService.deleteForActor(persistedNotificationId, adminActorId, Role.ADMIN);

        verify(notificationRepository).delete(notification);
    }

    @Test
    void deletesNotificationWhenActorIsRecipientStudent() {
        Notification notification = persistedUnreadNotification(persistedNotificationId, recipientUserId);
        when(notificationRepository.findById(persistedNotificationId)).thenReturn(Optional.of(notification));

        notificationService.deleteForActor(persistedNotificationId, recipientUserId, Role.STUDENT);

        verify(notificationRepository).delete(notification);
    }

    @Test
    void deniesDeleteWhenActorIsAnotherStudent() {
        Notification notification = persistedUnreadNotification(persistedNotificationId, recipientUserId);
        when(notificationRepository.findById(persistedNotificationId)).thenReturn(Optional.of(notification));

        assertThrows(AccessDeniedException.class,
                () -> notificationService.deleteForActor(persistedNotificationId, otherUserId, Role.STUDENT));

        verify(notificationRepository, never()).delete(any());
    }

    @Test
    void persistsRelatedEntityIdsWhenAllOptionalReferencesExist() {
        User recipient = studentNamedAna(recipientUserId);
        when(userRepository.findById(recipientUserId)).thenReturn(Optional.of(recipient));
        when(courseService.getCourse(relatedCourseId)).thenReturn(new Course("Course title", "Description", 1L));
        LocalDateTime futureApplicationDate = LocalDateTime.now().plusDays(1);
        when(evaluationRepository.findById(relatedEvaluationId))
                .thenReturn(Optional.of(new Evaluation(relatedCourseId, "Exam", 100, futureApplicationDate)));
        Payment payment = new Payment(
                recipientUserId,
                relatedCourseId,
                new BigDecimal("10"),
                PaymentMethod.CREDIT_CARD,
                PaymentStatus.APPROVED,
                "ref");
        when(paymentRepository.findById(relatedPaymentId)).thenReturn(Optional.of(payment));
        stubSaveAssignsId(persistedNotificationId);

        NotificationRequest request = fullContextPaymentApprovedRequest(recipientUserId);

        Notification result = notificationService.create(request);

        assertEquals(relatedCourseId, result.getRelatedCourseId());
        assertEquals(relatedEvaluationId, result.getRelatedEvaluationId());
        assertEquals(relatedPaymentId, result.getRelatedPaymentId());

        ArgumentCaptor<Notification> savedNotification = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(savedNotification.capture());
        assertEquals(relatedPaymentId, savedNotification.getValue().getRelatedPaymentId());
    }

    private void stubSaveAssignsId(long id) {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification saved = invocation.getArgument(0);
            assignEntityId(saved, id);
            return saved;
        });
    }

    private void stubSaveReturnsPersistedArgument() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static NotificationRequest welcomeGeneralNotificationRequest(long recipientId) {
        NotificationRequest request = new NotificationRequest();
        request.setRecipientId(recipientId);
        request.setTitle("Bienvenida");
        request.setMessage("Tienes un curso nuevo");
        request.setNotificationType(NotificationType.GENERAL);
        return request;
    }

    private static NotificationRequest minimalGeneralNotificationRequest(long recipientId) {
        NotificationRequest request = new NotificationRequest();
        request.setRecipientId(recipientId);
        request.setTitle("Title");
        request.setMessage("Message");
        request.setNotificationType(NotificationType.GENERAL);
        return request;
    }

    private static NotificationRequest notificationRequestWithRelatedCourse(
            long recipientId, NotificationType type, long courseId) {
        NotificationRequest request = minimalGeneralNotificationRequest(recipientId);
        request.setNotificationType(type);
        request.setRelatedCourseId(courseId);
        return request;
    }

    private static NotificationRequest notificationRequestWithRelatedEvaluation(
            long recipientId, NotificationType type, long evaluationId) {
        NotificationRequest request = minimalGeneralNotificationRequest(recipientId);
        request.setNotificationType(type);
        request.setRelatedEvaluationId(evaluationId);
        return request;
    }

    private static NotificationRequest notificationRequestWithRelatedPayment(
            long recipientId, NotificationType type, long paymentId) {
        NotificationRequest request = minimalGeneralNotificationRequest(recipientId);
        request.setNotificationType(type);
        request.setRelatedPaymentId(paymentId);
        return request;
    }

    private static NotificationRequest fullContextPaymentApprovedRequest(long recipientId) {
        NotificationRequest request = minimalGeneralNotificationRequest(recipientId);
        request.setNotificationType(NotificationType.PAYMENT_APPROVED);
        request.setRelatedCourseId(relatedCourseId);
        request.setRelatedEvaluationId(relatedEvaluationId);
        request.setRelatedPaymentId(relatedPaymentId);
        return request;
    }

    private static User studentNamedAna(long id) {
        User user = new User("Ana", "ana@duoc.cl", "password", Role.STUDENT);
        assignEntityId(user, id);
        return user;
    }

    private static Notification unreadGeneralNotificationForRecipient(long recipientId) {
        return new Notification(
                recipientId,
                "Title",
                "Message",
                NotificationType.GENERAL,
                null,
                null,
                null);
    }

    private static Notification persistedUnreadNotification(long notificationId, long recipientId) {
        Notification notification = unreadGeneralNotificationForRecipient(recipientId);
        assignEntityId(notification, notificationId);
        return notification;
    }

    private static void assignEntityId(Object entity, long id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }
}
