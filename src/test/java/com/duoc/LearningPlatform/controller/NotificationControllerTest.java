package com.duoc.LearningPlatform.controller;

import com.duoc.LearningPlatform.dto.NotificationRequest;
import com.duoc.LearningPlatform.dto.NotificationResponse;
import com.duoc.LearningPlatform.model.Notification;
import com.duoc.LearningPlatform.model.NotificationType;
import com.duoc.LearningPlatform.model.Role;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.service.NotificationService;
import com.duoc.LearningPlatform.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NotificationController notificationController;

    @Test
    void adminCreatesNotification() {
        NotificationRequest request = new NotificationRequest();
        request.setRecipientId(2L);
        request.setTitle("Aviso");
        request.setMessage("Mensaje");
        request.setNotificationType(NotificationType.NEW_COURSE);

        Notification saved = new Notification(2L, "Aviso", "Mensaje", NotificationType.NEW_COURSE, null, null, null);
        ReflectionTestUtils.setField(saved, "id", 50L);
        when(notificationService.create(request)).thenReturn(saved);

        ResponseEntity<NotificationResponse> response = notificationController.createNotification(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(50L, response.getBody().getId());
        verify(notificationService).create(request);
    }

    @Test
    void listsNotificationsForActor() {
        User student = new User("Ana", "ana@duoc.cl", "x", Role.STUDENT);
        ReflectionTestUtils.setField(student, "id", 2L);
        when(authentication.getName()).thenReturn("ana@duoc.cl");
        when(userService.findByEmail("ana@duoc.cl")).thenReturn(Optional.of(student));

        Notification n = new Notification(2L, "T", "M", NotificationType.GENERAL, null, null, null);
        ReflectionTestUtils.setField(n, "id", 1L);
        when(notificationService.findForList(2L, Role.STUDENT)).thenReturn(List.of(n));

        ResponseEntity<List<NotificationResponse>> response = notificationController.listNotifications(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
    }

    @Test
    void listsMyNotifications() {
        User professor = new User("P", "prof@duoc.cl", "x", Role.PROFESSOR);
        ReflectionTestUtils.setField(professor, "id", 5L);
        when(authentication.getName()).thenReturn("prof@duoc.cl");
        when(userService.findByEmail("prof@duoc.cl")).thenReturn(Optional.of(professor));

        Notification n = new Notification(5L, "T", "M", NotificationType.GENERAL, null, null, null);
        ReflectionTestUtils.setField(n, "id", 9L);
        when(notificationService.findForMe(5L)).thenReturn(List.of(n));

        ResponseEntity<List<NotificationResponse>> response = notificationController.listMyNotifications(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(9L, response.getBody().get(0).getId());
        verify(notificationService).findForMe(5L);
    }

    @Test
    void getsNotificationById() {
        User student = new User("Ana", "ana@duoc.cl", "x", Role.STUDENT);
        ReflectionTestUtils.setField(student, "id", 2L);
        when(authentication.getName()).thenReturn("ana@duoc.cl");
        when(userService.findByEmail("ana@duoc.cl")).thenReturn(Optional.of(student));

        Notification n = new Notification(2L, "T", "M", NotificationType.GENERAL, null, null, null);
        ReflectionTestUtils.setField(n, "id", 7L);
        when(notificationService.getForActor(eq(7L), eq(2L), eq(Role.STUDENT))).thenReturn(n);

        ResponseEntity<NotificationResponse> response = notificationController.getNotification(7L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(7L, response.getBody().getId());
    }

    @Test
    void marksNotificationAsRead() {
        User student = new User("Ana", "ana@duoc.cl", "x", Role.STUDENT);
        ReflectionTestUtils.setField(student, "id", 2L);
        when(authentication.getName()).thenReturn("ana@duoc.cl");
        when(userService.findByEmail("ana@duoc.cl")).thenReturn(Optional.of(student));

        Notification n = new Notification(2L, "T", "M", NotificationType.GENERAL, null, null, null);
        ReflectionTestUtils.setField(n, "id", 7L);
        n.markAsRead();
        when(notificationService.markReadForActor(7L, 2L, Role.STUDENT)).thenReturn(n);

        ResponseEntity<NotificationResponse> response = notificationController.markAsRead(7L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isRead());
    }

    @Test
    void deletesNotification() {
        User admin = new User("A", "admin@duoc.cl", "x", Role.ADMIN);
        ReflectionTestUtils.setField(admin, "id", 1L);
        when(authentication.getName()).thenReturn("admin@duoc.cl");
        when(userService.findByEmail("admin@duoc.cl")).thenReturn(Optional.of(admin));

        ResponseEntity<Void> response = notificationController.deleteNotification(7L, authentication);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(notificationService).deleteForActor(7L, 1L, Role.ADMIN);
    }
}
