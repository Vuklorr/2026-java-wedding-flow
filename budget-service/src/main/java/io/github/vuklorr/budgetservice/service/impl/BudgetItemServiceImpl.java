package io.github.vuklorr.budgetservice.service.impl;

import io.github.vuklorr.budgetservice.client.WeddingClient;
import io.github.vuklorr.budgetservice.dto.budgetitem.BudgetItemResponse;
import io.github.vuklorr.budgetservice.dto.budgetitem.CreateBudgetItemRequest;
import io.github.vuklorr.budgetservice.dto.budgetitem.UpdateBudgetItemRequest;
import io.github.vuklorr.budgetservice.exception.BudgetItemHasPaymentsException;
import io.github.vuklorr.budgetservice.exception.BudgetItemNotFoundException;
import io.github.vuklorr.budgetservice.model.BudgetItem;
import io.github.vuklorr.budgetservice.repository.BudgetItemRepository;
import io.github.vuklorr.budgetservice.repository.PaymentRepository;
import io.github.vuklorr.budgetservice.service.BudgetItemService;
import io.github.vuklorr.budgetservice.validation.CurrencyValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class BudgetItemServiceImpl implements BudgetItemService {

    private final BudgetItemRepository budgetItemRepository;
    private final WeddingClient weddingClient;
    private final PaymentRepository paymentRepository;
    private final CurrencyValidator currencyValidator;

    public BudgetItemServiceImpl(BudgetItemRepository budgetItemRepository, WeddingClient weddingClient, PaymentRepository paymentRepository, CurrencyValidator currencyValidator) {
        this.budgetItemRepository = budgetItemRepository;
        this.weddingClient = weddingClient;
        this.paymentRepository = paymentRepository;
        this.currencyValidator = currencyValidator;
    }

    @Override
    public BudgetItemResponse createBudgetItem(CreateBudgetItemRequest createBudgetItemRequest, int weddingId) {
        currencyValidator.validateCurrencyCode(createBudgetItemRequest.currencyCode());
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

        if (paymentRepository.existsByBudgetItemId(id)) {
            throw new BudgetItemHasPaymentsException(id);
        }

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
