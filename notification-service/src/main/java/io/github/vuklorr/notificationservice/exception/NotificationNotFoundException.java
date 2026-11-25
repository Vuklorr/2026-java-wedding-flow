package io.github.vuklorr.notificationservice.exception;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(long notificationId) {
        super("Notification id: " + notificationId + " not found");
    }

}
