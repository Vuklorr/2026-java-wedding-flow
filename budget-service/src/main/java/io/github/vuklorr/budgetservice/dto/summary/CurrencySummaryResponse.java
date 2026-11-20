package io.github.vuklorr.budgetservice.dto.summary;

import java.math.BigDecimal;

public record CurrencySummaryResponse (
        String currencyCode,
        BigDecimal plannedAmount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount
) {
}
