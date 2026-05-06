package com.duoc.LearningPlatform.repository;

import com.duoc.LearningPlatform.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<Notification> findAllByOrderByCreatedAtDesc();
}
