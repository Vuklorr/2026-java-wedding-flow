package io.github.vuklorr.weddingservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateWeddingDateRequest(
        @NotNull (message = "Дата свадьбы не может быть пустой")
        LocalDate weddingDate
) {
}
