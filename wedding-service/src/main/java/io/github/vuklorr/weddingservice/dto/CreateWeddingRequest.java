package io.github.vuklorr.weddingservice.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateWeddingRequest(
        @NotBlank(message = "Название свадьбы не может быть пустым")
        String title,
        @NotNull (message = "Дата свадьбы не может быть пустым")
        @FutureOrPresent(message = "Дата свадьбы не может быть позже текуще даты")
        LocalDate weddingDate
) {
}
