package io.github.vuklorr.budgetservice.dto.budgetitem;

import java.math.BigDecimal;
import java.time.Instant;

public record BudgetItemResponse (
   Integer id,
   Integer weddingId,
   String name,
   BigDecimal plannedAmount,
   String currencyCode,
   Instant createdAt
) {
}
