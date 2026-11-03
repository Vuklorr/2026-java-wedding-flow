package io.github.vuklorr.planningservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateWeddingTaskRequest(
        @NotNull (message = "ID свадьбы не может быть пустым")
        Integer weddingID,
        @NotBlank(message = "Название задачи не может быть пустым")
        String name
) {
}