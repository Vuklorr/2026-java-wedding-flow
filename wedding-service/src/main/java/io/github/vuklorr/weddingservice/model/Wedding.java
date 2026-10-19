package io.github.vuklorr.weddingservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wedding {
    private Integer id;
    private String title;
    private LocalDate weddingDate;
    private LocalDateTime createdAt;
}
