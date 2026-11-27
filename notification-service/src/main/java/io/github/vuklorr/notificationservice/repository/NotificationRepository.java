package io.github.vuklorr.notificationservice.repository;

import io.github.vuklorr.notificationservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByWeddingIdOrderByCreatedAtDesc(int weddingId);

    boolean existsByEventId(UUID eventId);
}
