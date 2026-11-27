package io.github.vuklorr.notificationservice.service.impl;

import io.github.vuklorr.notificationservice.dto.NotificationResponse;
import io.github.vuklorr.notificationservice.exception.NotificationNotFoundException;
import io.github.vuklorr.notificationservice.model.Notification;
import io.github.vuklorr.notificationservice.repository.NotificationRepository;
import io.github.vuklorr.notificationservice.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> readAllNotificationsByWeddingId(int weddingId) {
        return notificationRepository
                .findByWeddingIdOrderByCreatedAtDesc(weddingId)
                .stream()
                .map(this::getNotificationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse readNotificationById(long id) {
        Notification notification = findNotificationOrThrow(id);
        return getNotificationResponse(notification);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(long id) {
        Notification notification = findNotificationOrThrow(id);

        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
        }
        return getNotificationResponse(notification);
    }

    @Override
    @Transactional
    public void deleteNotification(long id) {
        Notification notification = findNotificationOrThrow(id);
        notificationRepository.delete(notification);
    }

    private NotificationResponse getNotificationResponse(Notification notification) {
        return new NotificationResponse(
                   notification.getId(),
                   notification.getEventId(),
                   notification.getWeddingId(),
                   notification.getType(),
                   notification.getTitle(),
                   notification.getMessage(),
                   notification.getCreatedAt(),
                   notification.getReadAt(),
            notification.getReadAt() != null
        );
    }

    private Notification findNotificationOrThrow(long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }
}
