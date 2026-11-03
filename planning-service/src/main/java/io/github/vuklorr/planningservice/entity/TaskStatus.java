package io.github.vuklorr.planningservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStatus {
    NEW("Новый"),
    IN_PROGRESS("В процессе"),
    DONE("Завершён");

    private final String status;


}
