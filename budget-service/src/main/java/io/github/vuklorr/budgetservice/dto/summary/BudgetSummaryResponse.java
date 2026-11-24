package io.github.vuklorr.budgetservice.dto.summary;

import java.util.List;

public record BudgetSummaryResponse(
        Integer weddingId,
        List<CurrencySummaryResponse> totals
) {
}
