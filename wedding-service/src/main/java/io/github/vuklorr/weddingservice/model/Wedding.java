package io.github.vuklorr.weddingservice.model;

import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wedding {
    private Integer id;
    private String title;
    private LocalDate weddingDate;
    private LocalDateTime createdAt;
}
