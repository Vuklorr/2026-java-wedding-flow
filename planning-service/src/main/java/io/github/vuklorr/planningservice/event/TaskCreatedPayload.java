package io.github.vuklorr.planningservice.event;

public record TaskCreatedPayload(
        Integer taskId,
        String taskName
) {
}
