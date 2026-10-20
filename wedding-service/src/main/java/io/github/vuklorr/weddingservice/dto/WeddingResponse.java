package io.github.vuklorr.weddingservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WeddingResponse(
        Integer id,
        String title,
        LocalDate weddingDate,
        LocalDateTime createdAt
) {
}
