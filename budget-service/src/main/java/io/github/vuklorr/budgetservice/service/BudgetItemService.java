package io.github.vuklorr.budgetservice.service;

import io.github.vuklorr.budgetservice.dto.budgetitem.BudgetItemResponse;
import io.github.vuklorr.budgetservice.dto.budgetitem.CreateBudgetItemRequest;
import io.github.vuklorr.budgetservice.dto.budgetitem.UpdateBudgetItemRequest;

import java.util.List;

public interface BudgetItemService {
    BudgetItemResponse createBudgetItem(CreateBudgetItemRequest createBudgetItemRequest, int weddingId);
    List<BudgetItemResponse> readAllBudgetItemsByWeddingId(int weddingId);
    BudgetItemResponse readBudgetItemById(int id);
    BudgetItemResponse updateBudgetItem(int id, UpdateBudgetItemRequest updateBudgetItemRequest);
    void deleteBudgetItem(int id);

}
