package io.github.vuklorr.budgetservice.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;


public record UpdatePaymentRequest(
        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 13, fraction = 2)
        BigDecimal amount
) {
}