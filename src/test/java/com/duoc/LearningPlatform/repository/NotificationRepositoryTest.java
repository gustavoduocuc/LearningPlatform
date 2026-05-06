package com.duoc.LearningPlatform.repository;

import com.duoc.LearningPlatform.model.Notification;
import com.duoc.LearningPlatform.model.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationRepositoryTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Test
    void findsByRecipientIdOrderedByCreatedAtDesc() {
        Notification n1 = new Notification(2L, "A", "m", NotificationType.GENERAL, null, null, null);
        Notification n2 = new Notification(2L, "B", "m", NotificationType.GENERAL, null, null, null);
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(n1, n2));

        List<Notification> result = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(2L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(n -> n.getRecipientId().equals(2L)));
    }

    @Test
    void findsAllOrderedByCreatedAtDesc() {
        when(notificationRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        List<Notification> result = notificationRepository.findAllByOrderByCreatedAtDesc();

        assertEquals(0, result.size());
    }

    @Test
    void savesNotification() {
        Notification notification = new Notification(2L, "T", "M", NotificationType.GENERAL, null, null, null);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 200L);
            return saved;
        });

        Notification saved = notificationRepository.save(notification);

        assertEquals(200L, saved.getId());
        verify(notificationRepository).save(notification);
    }
}
