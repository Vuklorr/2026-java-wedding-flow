package io.github.vuklorr.planningservice.dto;

import io.github.vuklorr.planningservice.entity.TaskPriority;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateWeddingTaskRequest(
        @NotBlank(message = "Название задачи не может быть пустым")
        String name,
        String description,
        TaskPriority priority,
        LocalDate deadline
) {
}