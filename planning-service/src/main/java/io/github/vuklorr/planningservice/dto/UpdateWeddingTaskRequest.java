package io.github.vuklorr.planningservice.dto;

import io.github.vuklorr.planningservice.entity.TaskPriority;
import io.github.vuklorr.planningservice.entity.TaskStatus;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record UpdateWeddingTaskRequest(
        @Pattern(
                regexp = ".*\\S.*",
                message = "Название задачи не может быть пустым"
        )
        String name,
        String description,
        TaskPriority priority,
        TaskStatus status,
        LocalDate deadline
) {
}