package io.github.vuklorr.weddingservice.repository;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
public class WeddingRepository {
    private Integer id;
    private String title;
    private LocalDate weddingDate;
    private LocalDateTime createdAt;
}
