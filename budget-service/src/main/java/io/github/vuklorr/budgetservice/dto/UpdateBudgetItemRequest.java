package io.github.vuklorr.budgetservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;


public record UpdateBudgetItemRequest(
        @Pattern(
                regexp = ".*\\S.*",
                message = "Название объекта бюджета не может быть пустым"
        )
        String name,

        @DecimalMin(value = "0.00")
        @Digits(integer = 13, fraction = 2)
        BigDecimal plannedAmount
) {
}