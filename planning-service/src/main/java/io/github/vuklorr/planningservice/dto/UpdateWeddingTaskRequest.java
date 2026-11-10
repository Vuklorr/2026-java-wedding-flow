package io.github.vuklorr.planningservice.dto;

import io.github.vuklorr.planningservice.entity.TaskPriority;
import io.github.vuklorr.planningservice.entity.TaskStatus;

import java.time.LocalDate;

public record UpdateWeddingTaskRequest(
        String name,
        String description,
        TaskPriority priority,
        TaskStatus status,
        LocalDate deadline
) {
}