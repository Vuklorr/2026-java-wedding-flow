package io.github.vuklorr.budgetservice;

import io.github.vuklorr.budgetservice.client.WeddingClient;
import io.github.vuklorr.budgetservice.dto.summary.*;
import io.github.vuklorr.budgetservice.exception.WeddingNotFoundException;
import io.github.vuklorr.budgetservice.model.*;
import io.github.vuklorr.budgetservice.repository.*;
import io.github.vuklorr.budgetservice.service.impl.BudgetSummaryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class BudgetSummaryServiceTest {
    private WeddingClient weddings;
    private BudgetItemRepository items;
    private PaymentRepository payments;
    private BudgetSummaryServiceImpl service;

    @BeforeEach
    void setUp() {
        weddings = mock(WeddingClient.class);
        items = mock(BudgetItemRepository.class);
        payments = mock(PaymentRepository.class);
        service = new BudgetSummaryServiceImpl(weddings, items, payments);
    }

    @Test
    void emptyBudgetShouldReturnEmptyTotalsWithoutQueryingPayments() {
        BudgetSummaryResponse result = service.getSummary(5);
        assertEquals(5, result.weddingId());
        assertTrue(result.totals().isEmpty());
        verify(weddings).ensureWeddingExists(5);
        verifyNoInteractions(payments);
    }

    @Test
    void missingWeddingShouldNotAccessRepositories() {
        doThrow(new WeddingNotFoundException(999)).when(weddings).ensureWeddingExists(999);
        assertThrows(WeddingNotFoundException.class, () -> service.getSummary(999));
        verifyNoInteractions(items, payments);
    }

    @Test
    void unpaidItemShouldShowWholePlannedAmountRemaining() {
        when(items.findByWeddingId(5)).thenReturn(List.of(item(1, "RUB", "200.00")));
        CurrencySummaryResponse total = service.getSummary(5).totals().getFirst();
        assertMoney("200.00", total.plannedAmount());
        assertMoney("0.00", total.paidAmount());
        assertMoney("200.00", total.remainingAmount());
    }

    @Test
    void shouldCombineSeveralItemsOfSameCurrencyAndPayments() {
        BudgetItem venue = item(1, "RUB", "200.00");
        BudgetItem photo = item(2, "RUB", "100.00");
        when(items.findByWeddingId(5)).thenReturn(List.of(venue, photo));
        when(payments.findByBudgetItemIdIn(List.of(1, 2))).thenReturn(List.of(
                payment(venue, "50.00", "1"),
                payment(venue, "20.00", "1"),
                payment(photo, "30.00", "1")
        ));
        CurrencySummaryResponse total = service.getSummary(5).totals().getFirst();
        assertMoney("300.00", total.plannedAmount());
        assertMoney("100.00", total.paidAmount());
        assertMoney("200.00", total.remainingAmount());
        verify(payments, times(1)).findByBudgetItemIdIn(anyList());
    }

    @Test
    void differentCurrenciesShouldConvertAndGroupByItemCurrency() {
        BudgetItem venue = item(1, "RUB", "200000.00");
        BudgetItem hotel = item(2, "EUR", "1000.00");
        when(items.findByWeddingId(5)).thenReturn(List.of(venue, hotel));
        when(payments.findByBudgetItemIdIn(List.of(1, 2))).thenReturn(List.of(
                payment(venue, "500.00", "95.00"),
                payment(venue, "10000.00", "1"),
                payment(hotel, "300.00", "1")
        ));
        List<CurrencySummaryResponse> totals = service.getSummary(5).totals();
        assertEquals(List.of("EUR", "RUB"), totals.stream().map(CurrencySummaryResponse::currencyCode).toList());
        assertMoney("1000.00", totals.get(0).plannedAmount());
        assertMoney("300.00", totals.get(0).paidAmount());
        assertMoney("700.00", totals.get(0).remainingAmount());
        assertMoney("200000.00", totals.get(1).plannedAmount());
        assertMoney("57500.00", totals.get(1).paidAmount());
        assertMoney("142500.00", totals.get(1).remainingAmount());
    }

    @Test
    void overpaymentShouldProduceNegativeRemaining() {
        BudgetItem item = item(1, "RUB", "100.00");
        when(items.findByWeddingId(5)).thenReturn(List.of(item));
        when(payments.findByBudgetItemIdIn(List.of(1)))
                .thenReturn(List.of(payment(item, "120.00", "1")));
        assertMoney("-20.00", service.getSummary(5).totals().getFirst().remainingAmount());
    }

    @Test
    void fractionalConvertedPaymentShouldRoundHalfUpOnlyAtEnd() {
        BudgetItem item = item(1, "RUB", "1.00");
        when(items.findByWeddingId(5)).thenReturn(List.of(item));
        when(payments.findByBudgetItemIdIn(List.of(1))).thenReturn(List.of(
                payment(item, "0.01", "1.5000000000"),
                payment(item, "0.01", "1.5000000000")
        ));
        CurrencySummaryResponse total = service.getSummary(5).totals().getFirst();
        assertMoney("0.03", total.paidAmount());
        assertMoney("0.97", total.remainingAmount());
    }

    private static BudgetItem item(int id, String currency, String planned) {
        BudgetItem item = new BudgetItem();
        item.setId(id);
        item.setWeddingId(5);
        item.setCurrencyCode(currency);
        item.setPlannedAmount(new BigDecimal(planned));
        return item;
    }

    private static Payment payment(BudgetItem item, String amount, String rate) {
        Payment payment = new Payment();
        payment.setBudgetItem(item);
        payment.setAmount(new BigDecimal(amount));
        payment.setExchangeRateToItemCurrency(new BigDecimal(rate));
        return payment;
    }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(new BigDecimal(expected), actual);
    }
}
