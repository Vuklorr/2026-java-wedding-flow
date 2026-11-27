package io.github.vuklorr.planningservice.event;

import java.time.Instant;
import java.util.UUID;

public record TaskCreatedEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        int weddingId,
        TaskCreatedPayload payload
) {
}
