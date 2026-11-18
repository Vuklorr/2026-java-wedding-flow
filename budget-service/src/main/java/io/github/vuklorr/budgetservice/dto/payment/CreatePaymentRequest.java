package io.github.vuklorr.budgetservice.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @DecimalMin(value = "0.00")
        @Digits(integer = 13, fraction = 2)
        BigDecimal amount,

        @Pattern(
                regexp = "[A-Z]{3}",
                message = "Код курса валюты должен иметь 3 буквенных символа"
        )
        @NotBlank
        String currencyCode,

        @DecimalMin(value = "0.00")
        @Digits(integer = 10, fraction = 10)
        BigDecimal exchangeRateToItemCurrency
) {
}
