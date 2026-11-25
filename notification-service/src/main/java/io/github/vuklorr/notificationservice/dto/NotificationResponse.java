package io.github.vuklorr.notificationservice.dto;

import io.github.vuklorr.notificationservice.entity.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    Long id,
    UUID eventId,
    Integer weddingId,
    NotificationType type,
    String title,
    String message,
    Instant createAt,
    Instant readAt,
    Boolean isRead
) {
}
