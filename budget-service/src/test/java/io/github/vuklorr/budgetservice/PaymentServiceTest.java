package io.github.vuklorr.budgetservice;

import io.github.vuklorr.budgetservice.dto.payment.*;
import io.github.vuklorr.budgetservice.exception.*;
import io.github.vuklorr.budgetservice.model.*;
import io.github.vuklorr.budgetservice.repository.*;
import io.github.vuklorr.budgetservice.service.impl.PaymentServiceImpl;
import io.github.vuklorr.budgetservice.validation.CurrencyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {
    private PaymentRepository payments;
    private BudgetItemRepository items;
    private PaymentServiceImpl service;
    private BudgetItem item;

    @BeforeEach
    void setUp() {
        payments = mock(PaymentRepository.class);
        items = mock(BudgetItemRepository.class);
        service = new PaymentServiceImpl(payments, items, new CurrencyValidator());
        item = new BudgetItem();
        item.setId(8);
        item.setWeddingId(5);
        item.setCurrencyCode("RUB");
        item.setPlannedAmount(new BigDecimal("200000.00"));
        when(items.findById(8)).thenReturn(Optional.of(item));
        when(payments.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(31);
            p.setPaymentDate(Instant.parse("2026-11-20T12:00:00Z"));
            return p;
        });
    }

    @Test
    void createSameCurrencyWithoutRateShouldUseOne() {
        PaymentResponse result = service.createPayment(request("100.00", "RUB", null), 8);
        assertEquals(31, result.id());
        assertEquals(8, result.budgetItemId());
        assertEquals(0, result.exchangeRateToItemCurrency().compareTo(BigDecimal.ONE));
        assertEquals(new BigDecimal("100.00"), result.amount());
        assertNotNull(result.paymentDate());
    }

    @Test
    void createSameCurrencyWithOnePointZeroShouldSucceed() {
        PaymentResponse result = service.createPayment(request("100.00", "RUB", "1.00"), 8);
        assertEquals(0, result.exchangeRateToItemCurrency().compareTo(BigDecimal.ONE));
    }

    @Test
    void createSameCurrencyWithIncorrectRateShouldFail() {
        assertThrows(InvalidExchangeRateException.class,
                () -> service.createPayment(request("100.00", "RUB", "1.10"), 8));
        verify(payments, never()).save(any());
    }

    @Test
    void createDifferentCurrencyShouldPreserveExchangeRate() {
        PaymentResponse result = service.createPayment(request("500.00", "EUR", "95.50"), 8);
        assertEquals(new BigDecimal("95.50"), result.exchangeRateToItemCurrency());
        assertEquals("EUR", result.currencyCode());
    }

    @Test
    void createDifferentCurrencyWithoutRateShouldFail() {
        assertThrows(InvalidExchangeRateException.class,
                () -> service.createPayment(request("500.00", "EUR", null), 8));
        verify(payments, never()).save(any());
    }

    @Test
    void createInvalidCurrencyShouldNotSave() {
        assertThrows(InvalidCurrencyCodeException.class,
                () -> service.createPayment(request("10.00", "ZZZ", "90.00"), 8));
        verify(payments, never()).save(any());
    }

    @Test
    void createMissingBudgetItemShouldReturnNotFound() {
        assertThrows(BudgetItemNotFoundException.class,
                () -> service.createPayment(request("10.00", "RUB", null), 999));
        verify(payments, never()).save(any());
    }

    @Test
    void listShouldReturnPaymentsForItem() {
        when(payments.findByBudgetItemId(8)).thenReturn(List.of(payment(31, "10.00"), payment(32, "20.00")));
        List<PaymentResponse> result = service.readAllPaymentsByBudgetItem(8);
        assertEquals(2, result.size());
        assertEquals(List.of(31, 32), result.stream().map(PaymentResponse::id).toList());
        verify(payments).findByBudgetItemId(8);
    }

    @Test
    void listForExistingItemWithoutPaymentsShouldBeEmpty() {
        assertTrue(service.readAllPaymentsByBudgetItem(8).isEmpty());
    }

    @Test
    void listForMissingItemShouldThrow() {
        assertThrows(BudgetItemNotFoundException.class, () -> service.readAllPaymentsByBudgetItem(999));
        verify(payments, never()).findByBudgetItemId(anyInt());
    }

    @Test
    void getPaymentShouldMapResponse() {
        when(payments.findById(31)).thenReturn(Optional.of(payment(31, "100.00")));
        PaymentResponse result = service.readPaymentById(31);
        assertEquals(31, result.id());
        assertEquals(8, result.budgetItemId());
        assertEquals("RUB", result.currencyCode());
    }

    @Test
    void getMissingPaymentShouldThrow() {
        assertThrows(PaymentNotFoundException.class, () -> service.readPaymentById(999));
    }

    @Test
    void patchShouldChangeAmountButKeepCurrencyAndRate() {
        Payment p = payment(31, "100.00");
        when(payments.findById(31)).thenReturn(Optional.of(p));
        PaymentResponse result = service.updatePayment(31, new UpdatePaymentRequest(new BigDecimal("125.00")));
        assertEquals(new BigDecimal("125.00"), result.amount());
        assertEquals("RUB", result.currencyCode());
        assertEquals(0, result.exchangeRateToItemCurrency().compareTo(BigDecimal.ONE));
        verify(payments, never()).save(any());
    }

    @Test
    void patchMissingPaymentShouldThrow() {
        assertThrows(PaymentNotFoundException.class,
                () -> service.updatePayment(999, new UpdatePaymentRequest(BigDecimal.ONE)));
    }

    @Test
    void deleteExistingPaymentShouldDeleteEntity() {
        Payment p = payment(31, "100.00");
        when(payments.findById(31)).thenReturn(Optional.of(p));
        service.deletePayment(31);
        verify(payments).delete(p);
    }

    @Test
    void deleteMissingPaymentShouldThrow() {
        assertThrows(PaymentNotFoundException.class, () -> service.deletePayment(999));
        verify(payments, never()).delete(any());
    }

    private static CreatePaymentRequest request(String amount, String currency, String rate) {
        return new CreatePaymentRequest(new BigDecimal(amount), currency,
                rate == null ? null : new BigDecimal(rate));
    }

    private Payment payment(int id, String amount) {
        Payment p = new Payment();
        p.setId(id);
        p.setBudgetItem(item);
        p.setAmount(new BigDecimal(amount));
        p.setCurrencyCode("RUB");
        p.setExchangeRateToItemCurrency(BigDecimal.ONE);
        p.setPaymentDate(Instant.parse("2026-11-20T12:00:00Z"));
        return p;
    }
}
