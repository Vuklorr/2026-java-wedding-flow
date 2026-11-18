package io.github.vuklorr.budgetservice.controller;

import io.github.vuklorr.budgetservice.dto.BudgetItemResponse;
import io.github.vuklorr.budgetservice.dto.CreateBudgetItemRequest;
import io.github.vuklorr.budgetservice.dto.UpdateBudgetItemRequest;
import io.github.vuklorr.budgetservice.service.BudgetItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BudgetItemController {
    private final BudgetItemService budgetItemService;

    public BudgetItemController(BudgetItemService budgetItemService) {
        this.budgetItemService = budgetItemService;
    }

    @PostMapping(value = "/weddings/{weddingId}/budget/items")
    public ResponseEntity<BudgetItemResponse> createBudgetItem(
            @PathVariable(name = "weddingId") int weddingId,
            @Valid @RequestBody CreateBudgetItemRequest createBudgetItemRequest

    ) {
        final BudgetItemResponse budgetItemResponse = budgetItemService.createBudgetItem(createBudgetItemRequest, weddingId);
        return new ResponseEntity<>(budgetItemResponse, HttpStatus.CREATED);
    }

    @GetMapping(value = "/weddings/{weddingId}/budget/items")
    public ResponseEntity<List<BudgetItemResponse>> readAllBudgetItems(@PathVariable(name = "weddingId") int weddingId) {
        final List<BudgetItemResponse> budgetItemResponseList = budgetItemService.readAllBudgetItemsByWeddingId(weddingId);

        return new ResponseEntity<>(budgetItemResponseList, HttpStatus.OK);
    }

    @GetMapping(value = "/budget/items/{itemId}")
    public ResponseEntity<BudgetItemResponse> readItem(@PathVariable(name = "itemId") int id) {
        final BudgetItemResponse budgetItemResponse = budgetItemService.readBudgetItemById(id);

        return new ResponseEntity<>(budgetItemResponse, HttpStatus.OK);
    }

    @PatchMapping(value = "/budget/items/{itemId}")
    public ResponseEntity<BudgetItemResponse> updateBudgetItem(
            @PathVariable(name = "itemId") int id,
            @Valid @RequestBody UpdateBudgetItemRequest updateBudgetItemRequest) {
        final BudgetItemResponse budgetItemResponse = budgetItemService.updateBudgetItem(id, updateBudgetItemRequest);

        return new ResponseEntity<>(budgetItemResponse, HttpStatus.OK);
    }

    @DeleteMapping(value = "/budget/items/{itemId}")
    public ResponseEntity<Void> deleteBudgetItem(@PathVariable(name = "itemId") int id) {
        budgetItemService.deleteBudgetItem(id);

        return ResponseEntity.noContent().build();
    }
}
