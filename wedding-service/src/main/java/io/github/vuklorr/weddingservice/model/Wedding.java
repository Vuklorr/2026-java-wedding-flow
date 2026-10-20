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

    @NotNull(message = "id свадьбы не может быть пустым")
    private Integer id;

    @NotNull(message = "Название свадьбы не может быть пустым")
    private String title;

    @NotNull (message = "Дата свадьбы не может быть пустым")
    @FutureOrPresent (message = "Дата свадьбы не может быть позже текуще даты")
    private LocalDate weddingDate;

    @NotNull (message = "Дата создания не может быть пустым")
    @FutureOrPresent (message = "Дата создания не может быть позже текуще даты")
    private LocalDateTime createdAt;
}
