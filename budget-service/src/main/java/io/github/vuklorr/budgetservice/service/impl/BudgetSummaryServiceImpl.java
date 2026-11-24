package io.github.vuklorr.budgetservice.service.impl;

import io.github.vuklorr.budgetservice.client.WeddingClient;
import io.github.vuklorr.budgetservice.dto.summary.BudgetSummaryResponse;
import io.github.vuklorr.budgetservice.dto.summary.CurrencySummaryResponse;
import io.github.vuklorr.budgetservice.model.BudgetItem;
import io.github.vuklorr.budgetservice.model.Payment;
import io.github.vuklorr.budgetservice.repository.BudgetItemRepository;
import io.github.vuklorr.budgetservice.repository.PaymentRepository;
import io.github.vuklorr.budgetservice.service.BudgetSummaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BudgetSummaryServiceImpl implements BudgetSummaryService {

    private final WeddingClient weddingClient;
    private final BudgetItemRepository budgetItemRepository;
    private final PaymentRepository paymentRepository;

    public BudgetSummaryServiceImpl(WeddingClient weddingClient, BudgetItemRepository budgetItemRepository, PaymentRepository paymentRepository) {
        this.weddingClient = weddingClient;
        this.budgetItemRepository = budgetItemRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetSummaryResponse getSummary(int weddingId) {
        weddingClient.ensureWeddingExists(weddingId);
        List<BudgetItem> budgetItems =
                budgetItemRepository.findByWeddingId(weddingId);

        if (budgetItems.isEmpty()) {
            return new BudgetSummaryResponse(weddingId, List.of());
        }

        List<Integer> budgetItemIds = budgetItems.stream()
                .map(BudgetItem::getId)
                .toList();

        List<Payment> payments =
                paymentRepository.findByBudgetItemIdIn(budgetItemIds);

        Map<Integer, String> currencyByItemId = budgetItems.stream()
                .collect(Collectors.toMap(
                        BudgetItem::getId,
                        BudgetItem::getCurrencyCode
                ));

        Map<String, BigDecimal> plannedByCurrency = new HashMap<>();

        for (BudgetItem item : budgetItems) {
            plannedByCurrency.merge(
                    item.getCurrencyCode(),
                    item.getPlannedAmount(),
                    BigDecimal::add
            );
        }

        Map<String, BigDecimal> paidByCurrency = new HashMap<>();

        for (Payment payment : payments) {
            String currencyCode = currencyByItemId.get(
                    payment.getBudgetItem().getId()
            );

            BigDecimal convertedAmount = payment.getAmount()
                    .multiply(payment.getExchangeRateToItemCurrency());

            paidByCurrency.merge(
                    currencyCode,
                    convertedAmount,
                    BigDecimal::add
            );
        }

        List<CurrencySummaryResponse> totals =
                plannedByCurrency.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(entry -> {
                            String currencyCode = entry.getKey();

                            BigDecimal plannedAmount = entry.getValue()
                                    .setScale(2, RoundingMode.HALF_UP);

                            BigDecimal paidAmount = paidByCurrency
                                    .getOrDefault(currencyCode, BigDecimal.ZERO)
                                    .setScale(2, RoundingMode.HALF_UP);

                            BigDecimal remainingAmount =
                                    plannedAmount.subtract(paidAmount);

                            return new CurrencySummaryResponse(
                                    currencyCode,
                                    plannedAmount,
                                    paidAmount,
                                    remainingAmount
                            );
                        })
                        .toList();

        return new BudgetSummaryResponse(weddingId, totals);
    }
}
