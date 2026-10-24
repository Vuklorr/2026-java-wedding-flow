package io.github.vuklorr.weddingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateWeddingRequest(
        @NotBlank(message = "Название свадьбы не может быть пустой")
        String title,
        @NotNull (message = "Дата свадьбы не может быть пустой")
        LocalDate weddingDate
) {
}
