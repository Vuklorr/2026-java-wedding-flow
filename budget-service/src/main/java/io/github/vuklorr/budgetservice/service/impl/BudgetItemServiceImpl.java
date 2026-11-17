package io.github.vuklorr.budgetservice.service.impl;

import io.github.vuklorr.budgetservice.client.WeddingClient;
import io.github.vuklorr.budgetservice.dto.BudgetItemResponse;
import io.github.vuklorr.budgetservice.dto.CreateBudgetItemRequest;
import io.github.vuklorr.budgetservice.dto.UpdateBudgetItemRequest;
import io.github.vuklorr.budgetservice.exception.BudgetItemNotFoundException;
import io.github.vuklorr.budgetservice.model.BudgetItem;
import io.github.vuklorr.budgetservice.repository.BudgetItemRepository;
import io.github.vuklorr.budgetservice.service.BudgetItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class BudgetItemServiceImpl implements BudgetItemService {

    private final BudgetItemRepository budgetItemRepository;
    private final WeddingClient weddingClient;

    public BudgetItemServiceImpl(BudgetItemRepository budgetItemRepository, WeddingClient weddingClient) {
        this.budgetItemRepository = budgetItemRepository;
        this.weddingClient = weddingClient;
    }

    @Override
    public BudgetItemResponse createBudgetItem(CreateBudgetItemRequest createBudgetItemRequest, int weddingId) {
        weddingClient.ensureWeddingExists(weddingId);

        BudgetItem budgetItem = new BudgetItem();
        budgetItem.setWeddingId(weddingId);
        budgetItem.setName(createBudgetItemRequest.name());
        budgetItem.setPlannedAmount(createBudgetItemRequest.plannedAmount());
        budgetItem.setCurrencyCode(createBudgetItemRequest.currencyCode());

        return getBudgetResponse(budgetItemRepository.save(budgetItem));
    }

    @Override
    public List<BudgetItemResponse> readAllBudgetItemsByWeddingId(int weddingId) {
        return budgetItemRepository
                .findByWeddingId(weddingId)
                .stream()
                .map(this::getBudgetResponse)
                .toList();
    }

    @Override
    public BudgetItemResponse readBudgetItemById(int id) {
        BudgetItem budgetItem = budgetItemRepository.findById(id).orElseThrow(() -> new BudgetItemNotFoundException(id));
        return getBudgetResponse(budgetItem);
    }

    @Override
    public BudgetItemResponse updateBudgetItem(int id, UpdateBudgetItemRequest updateBudgetItemRequest) {
        BudgetItem budgetItem = budgetItemRepository.findById(id).orElseThrow(() -> new BudgetItemNotFoundException(id));

        if (!Objects.isNull(updateBudgetItemRequest.name())) {
            budgetItem.setName(updateBudgetItemRequest.name());
        }

        if (!Objects.isNull(updateBudgetItemRequest.plannedAmount())) {
            budgetItem.setPlannedAmount(updateBudgetItemRequest.plannedAmount());
        }

        budgetItemRepository.flush();
        return getBudgetResponse(budgetItem);
    }

    @Override
    public void deleteBudgetItem(int id) {
        BudgetItem budgetItem = budgetItemRepository.findById(id).orElseThrow(() -> new BudgetItemNotFoundException(id));
        budgetItemRepository.delete(budgetItem);
    }

    private BudgetItemResponse getBudgetResponse(BudgetItem budgetItem) {
        return new BudgetItemResponse(
                budgetItem.getId(),
                budgetItem.getWeddingId(),
                budgetItem.getName(),
                budgetItem.getPlannedAmount(),
                budgetItem.getCurrencyCode(),
                budgetItem.getCreatedAt()
        );

    }
}
