package io.github.vuklorr.budgetservice.service;

import io.github.vuklorr.budgetservice.dto.summary.BudgetSummaryResponse;

public interface BudgetSummaryService {
    BudgetSummaryResponse getSummary(int weddingId);
}
