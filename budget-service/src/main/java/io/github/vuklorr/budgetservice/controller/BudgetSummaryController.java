package io.github.vuklorr.budgetservice.controller;

import io.github.vuklorr.budgetservice.dto.summary.BudgetSummaryResponse;
import io.github.vuklorr.budgetservice.service.BudgetSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BudgetSummaryController {

    private final BudgetSummaryService budgetSummaryService;
    public BudgetSummaryController(BudgetSummaryService budgetSummaryService) {
        this.budgetSummaryService = budgetSummaryService;
    }

    @GetMapping("/weddings/{weddingId}/budget/summary")
    public ResponseEntity<BudgetSummaryResponse> getSummary(
            @PathVariable int weddingId
    ) {
        return ResponseEntity.ok(
                budgetSummaryService.getSummary(weddingId)
        );
    }
}
