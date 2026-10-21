package io.github.vuklorr.weddingservice.dto;

import java.time.Instant;
import java.time.LocalDate;

public record WeddingResponse(
        Integer id,
        String title,
        LocalDate weddingDate,
        Instant createdAt
) {
}
