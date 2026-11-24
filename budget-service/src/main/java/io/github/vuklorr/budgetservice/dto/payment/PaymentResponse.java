package io.github.vuklorr.budgetservice.dto.payment;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
   Integer id,
   Integer budgetItemId,
   BigDecimal amount,
   String currencyCode,
   BigDecimal exchangeRateToItemCurrency,
   Instant paymentDate
) {
}
