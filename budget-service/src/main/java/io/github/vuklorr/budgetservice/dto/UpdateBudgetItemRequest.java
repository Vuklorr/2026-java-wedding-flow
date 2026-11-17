package io.github.vuklorr.budgetservice.dto;

import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;


public record UpdateBudgetItemRequest(
        @Pattern(
                regexp = ".*\\S.*",
                message = "Название задачи не может быть пустым"
        )
        String name,

        @Pattern(
                regexp = "^.{1,15}\\.{0,2}$",
                message = "Планируемая сумма не может быть пустой и имеет максимально 15 символов до точки и 2 символа после"
        )
        BigDecimal plannedAmount
) {
}