package io.github.vuklorr.notificationservice.controller;

import io.github.vuklorr.notificationservice.dto.NotificationResponse;
import io.github.vuklorr.notificationservice.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(value = "/weddings/{weddingId}/notifications")
    public ResponseEntity<List<NotificationResponse>> readNotificationsByWeddingId(
            @PathVariable(name = "weddingId") int weddingId) {
        final List<NotificationResponse> notificationResponseList =
                notificationService.readAllNotificationsByWeddingId(weddingId);

        return new ResponseEntity<>(notificationResponseList, HttpStatus.OK);
    }

    @GetMapping(value = "/notifications/{id}")
    public ResponseEntity<NotificationResponse> readNotificationById(@PathVariable(name = "id") long id) {
        final NotificationResponse notificationResponse = notificationService.readNotificationById(id);
        return new ResponseEntity<>(notificationResponse, HttpStatus.OK);
    }

    @PatchMapping(value = "/notifications/{id}/read")
    public ResponseEntity<NotificationResponse> markNotificationAsRead(@PathVariable(name = "id") long id) {
        final NotificationResponse notificationResponse = notificationService.markAsRead(id);
        return new ResponseEntity<>(notificationResponse, HttpStatus.OK);
    }

    @DeleteMapping(value = "/notifications/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable(name = "id") long id) {
        notificationService.deleteNotification(id);

        return ResponseEntity.noContent().build();
    }
}
