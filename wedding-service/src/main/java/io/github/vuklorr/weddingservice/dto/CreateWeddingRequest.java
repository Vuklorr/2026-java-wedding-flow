package io.github.vuklorr.weddingservice.dto;

import java.time.LocalDate;

public record CreateWeddingRequest(
        String title,
        LocalDate weddingDate
) {
}
