package io.github.vuklorr.notificationservice.service;

import io.github.vuklorr.notificationservice.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> readAllNotificationsByWeddingId(int weddingId);
    NotificationResponse readNotificationById(long id);
    NotificationResponse markAsRead(long id);
    void deleteNotification(long id);
}
