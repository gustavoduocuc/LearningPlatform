package com.duoc.LearningPlatform.controller;

import com.duoc.LearningPlatform.dto.NotificationRequest;
import com.duoc.LearningPlatform.dto.NotificationResponse;
import com.duoc.LearningPlatform.model.Notification;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.service.NotificationService;
import com.duoc.LearningPlatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        Notification saved = notificationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(NotificationResponse.fromEntity(saved));
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> listNotifications(Authentication authentication) {
        User actor = currentUser(authentication);
        List<NotificationResponse> body = notificationService
                .findForList(actor.getId(), actor.getRole())
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>> listMyNotifications(Authentication authentication) {
        User actor = currentUser(authentication);
        List<NotificationResponse> body = notificationService
                .findForMe(actor.getId())
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(
            @PathVariable Long id,
            Authentication authentication) {
        User actor = currentUser(authentication);
        Notification notification = notificationService.getForActor(id, actor.getId(), actor.getRole());
        return ResponseEntity.ok(NotificationResponse.fromEntity(notification));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        User actor = currentUser(authentication);
        Notification updated = notificationService.markReadForActor(id, actor.getId(), actor.getRole());
        return ResponseEntity.ok(NotificationResponse.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {
        User actor = currentUser(authentication);
        notificationService.deleteForActor(id, actor.getId(), actor.getRole());
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }
}
