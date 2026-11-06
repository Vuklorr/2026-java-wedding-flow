package io.github.vuklorr.planningservice.dto;

import io.github.vuklorr.planningservice.entity.TaskPriority;
import io.github.vuklorr.planningservice.entity.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;

public record WeddingTaskResponse (
        Integer id,
        Integer weddingId,
        String name,
        String description,
        TaskPriority priority,
        TaskStatus status,
        LocalDate deadline,
        Instant createdAt,
        Instant updateAt
) {
}