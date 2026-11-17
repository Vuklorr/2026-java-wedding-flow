package io.github.vuklorr.budgetservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CreateBudgetItemRequest(
        @NotBlank(message = "Название объекта бюджета не может быть пустым")
        String name,

        @Pattern(
                regexp = "^.{1,15}\\.{0,2}$",
                message = "Планируемая сумма не может быть пустой и имеет максимально 15 символов до точки и 2 символа после"
        )
        BigDecimal plannedAmount,

        @Pattern(
                regexp = "[A-Z]{3}",
                message = "Код курса валюты должен иметь 3 буквенных символа"
        )
        String currencyCode
) {
}
