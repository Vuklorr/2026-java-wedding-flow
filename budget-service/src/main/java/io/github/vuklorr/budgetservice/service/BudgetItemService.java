package io.github.vuklorr.budgetservice.service;

import io.github.vuklorr.budgetservice.dto.BudgetItemResponse;
import io.github.vuklorr.budgetservice.dto.CreateBudgetItemRequest;
import io.github.vuklorr.budgetservice.dto.UpdateBudgetItemRequest;

import java.util.List;

public interface BudgetItemService {
    BudgetItemResponse createBudgetItem(CreateBudgetItemRequest createBudgetItemRequest, int weddingId);
    List<BudgetItemResponse> readAllBudgetItemsByWeddingId(int weddingId);
    BudgetItemResponse readBudgetItemById(int id);
    BudgetItemResponse updateBudgetItem(int id, UpdateBudgetItemRequest updateBudgetItemRequest);
    void deleteBudgetItem(int id);

}
