package io.github.vuklorr.weddingservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateWeddingDateRequest(
        @NotNull (message = "Название свадьбы не может быть пустым")
        LocalDate weddingDate
) {
}
