package io.github.vuklorr.notificationservice.repository;

import io.github.vuklorr.notificationservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
}
