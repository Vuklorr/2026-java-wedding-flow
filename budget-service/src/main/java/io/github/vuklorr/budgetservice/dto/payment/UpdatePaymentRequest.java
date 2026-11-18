package io.github.vuklorr.budgetservice.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;


public record UpdatePaymentRequest(
        @DecimalMin(value = "0.00")
        @Digits(integer = 13, fraction = 2)
        BigDecimal amount
) {
}