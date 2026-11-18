package io.github.vuklorr.budgetservice.service.impl;

import io.github.vuklorr.budgetservice.client.WeddingClient;
import io.github.vuklorr.budgetservice.dto.BudgetItemResponse;
import io.github.vuklorr.budgetservice.dto.CreateBudgetItemRequest;
import io.github.vuklorr.budgetservice.dto.UpdateBudgetItemRequest;
import io.github.vuklorr.budgetservice.exception.BudgetItemNotFoundException;
import io.github.vuklorr.budgetservice.exception.InvalidCurrencyCodeException;
import io.github.vuklorr.budgetservice.model.BudgetItem;
import io.github.vuklorr.budgetservice.repository.BudgetItemRepository;
import io.github.vuklorr.budgetservice.service.BudgetItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;
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
        String currencyCode = createBudgetItemRequest.currencyCode();

        try {
            Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            throw new InvalidCurrencyCodeException("Currency code is invalid");
        }

        weddingClient.ensureWeddingExists(weddingId);

        BudgetItem budgetItem = new BudgetItem();
        budgetItem.setWeddingId(weddingId);
        budgetItem.setName(createBudgetItemRequest.name());
        budgetItem.setPlannedAmount(
                Objects.requireNonNullElse(
                        createBudgetItemRequest.plannedAmount(),
                        BigDecimal.ZERO
                )
        );

        budgetItem.setCurrencyCode(createBudgetItemRequest.currencyCode());

        return getBudgetResponse(budgetItemRepository.save(budgetItem));
    }

    @Override
    public List<BudgetItemResponse> readAllBudgetItemsByWeddingId(int weddingId) {
        weddingClient.ensureWeddingExists(weddingId);

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
    @Transactional
    public BudgetItemResponse updateBudgetItem(int id, UpdateBudgetItemRequest updateBudgetItemRequest) {
        BudgetItem budgetItem = budgetItemRepository.findById(id).orElseThrow(() -> new BudgetItemNotFoundException(id));

        if (updateBudgetItemRequest.name() != null) {
            budgetItem.setName(updateBudgetItemRequest.name());
        }

        if (updateBudgetItemRequest.plannedAmount() != null) {
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
