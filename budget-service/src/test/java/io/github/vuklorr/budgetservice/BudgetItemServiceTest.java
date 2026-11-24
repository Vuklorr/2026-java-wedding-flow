package io.github.vuklorr.budgetservice;

import io.github.vuklorr.budgetservice.client.WeddingClient;
import io.github.vuklorr.budgetservice.dto.budgetitem.BudgetItemResponse;
import io.github.vuklorr.budgetservice.dto.budgetitem.CreateBudgetItemRequest;
import io.github.vuklorr.budgetservice.dto.budgetitem.UpdateBudgetItemRequest;
import io.github.vuklorr.budgetservice.exception.BudgetItemNotFoundException;
import io.github.vuklorr.budgetservice.exception.BudgetItemHasPaymentsException;
import io.github.vuklorr.budgetservice.exception.InvalidCurrencyCodeException;
import io.github.vuklorr.budgetservice.exception.WeddingNotFoundException;
import io.github.vuklorr.budgetservice.exception.WeddingServiceUnavailableException;
import io.github.vuklorr.budgetservice.model.BudgetItem;
import io.github.vuklorr.budgetservice.repository.BudgetItemRepository;
import io.github.vuklorr.budgetservice.repository.PaymentRepository;
import io.github.vuklorr.budgetservice.validation.CurrencyValidator;
import io.github.vuklorr.budgetservice.service.impl.BudgetItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BudgetItemServiceTest {

    private static final Instant CREATED_AT = Instant.parse("2026-11-18T12:00:00Z");

    private BudgetItemRepository repository;
    private PaymentRepository paymentRepository;
    private WeddingClient weddingClient;
    private BudgetItemServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(BudgetItemRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        weddingClient = mock(WeddingClient.class);
        service = new BudgetItemServiceImpl(repository, weddingClient, paymentRepository, new CurrencyValidator());
    }

    @Test
    void createShouldCheckWeddingBeforeSavingAndReturnDto() {
        when(repository.save(any(BudgetItem.class))).thenAnswer(invocation -> {
            BudgetItem item = invocation.getArgument(0);
            item.setId(12);
            item.setCreatedAt(CREATED_AT);
            return item;
        });

        BudgetItemResponse response = service.createBudgetItem(
                new CreateBudgetItemRequest("Restaurant", new BigDecimal("200000.00"), "RUB"), 5);

        assertEquals(12, response.id());
        assertEquals(5, response.weddingId());
        assertEquals("Restaurant", response.name());
        assertEquals(new BigDecimal("200000.00"), response.plannedAmount());
        assertEquals("RUB", response.currencyCode());
        assertEquals(CREATED_AT, response.createdAt());

        ArgumentCaptor<BudgetItem> itemCaptor = ArgumentCaptor.forClass(BudgetItem.class);
        InOrder order = inOrder(weddingClient, repository);
        order.verify(weddingClient).ensureWeddingExists(5);
        order.verify(repository).save(itemCaptor.capture());
        assertEquals(5, itemCaptor.getValue().getWeddingId());
        assertEquals("RUB", itemCaptor.getValue().getCurrencyCode());
    }

    @Test
    void createWithoutAmountShouldUseZero() {
        when(repository.save(any(BudgetItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BudgetItemResponse response = service.createBudgetItem(
                new CreateBudgetItemRequest("Photographer", null, "EUR"), 5);

        assertEquals(BigDecimal.ZERO, response.plannedAmount());
        verify(repository).save(any(BudgetItem.class));
    }

    @Test
    void createWithInvalidCurrencyShouldFailBeforeCallingWeddingService() {
        assertThrows(InvalidCurrencyCodeException.class,
                () -> service.createBudgetItem(
                        new CreateBudgetItemRequest("Restaurant", BigDecimal.TEN, "ZZZ"), 5));

        verifyNoInteractions(weddingClient, repository);
    }

    @Test
    void createWithMissingWeddingShouldNotSave() {
        doThrow(new WeddingNotFoundException(999)).when(weddingClient).ensureWeddingExists(999);

        assertThrows(WeddingNotFoundException.class,
                () -> service.createBudgetItem(new CreateBudgetItemRequest("Venue", BigDecimal.TEN, "RUB"), 999));
        verifyNoInteractions(repository);
    }

    @Test
    void createWhenWeddingServiceIsUnavailableShouldNotSave() {
        doThrow(new WeddingServiceUnavailableException()).when(weddingClient).ensureWeddingExists(5);

        assertThrows(WeddingServiceUnavailableException.class,
                () -> service.createBudgetItem(new CreateBudgetItemRequest("Venue", BigDecimal.TEN, "RUB"), 5));
        verifyNoInteractions(repository);
    }

    @Test
    void listShouldReturnOnlyItemsOfRequestedWedding() {
        when(repository.findByWeddingId(5)).thenReturn(List.of(
                item(12, 5, "Restaurant", "RUB", "200000.00"),
                item(13, 5, "Photographer", "EUR", "500.00")));

        List<BudgetItemResponse> responses = service.readAllBudgetItemsByWeddingId(5);

        assertEquals(2, responses.size());
        assertEquals("Restaurant", responses.get(0).name());
        assertEquals("EUR", responses.get(1).currencyCode());
        assertTrue(responses.stream().allMatch(response -> response.weddingId() == 5));
        InOrder order = inOrder(weddingClient, repository);
        order.verify(weddingClient).ensureWeddingExists(5);
        order.verify(repository).findByWeddingId(5);
    }

    @Test
    void listShouldReturnEmptyListForWeddingWithoutItems() {
        when(repository.findByWeddingId(5)).thenReturn(List.of());

        assertTrue(service.readAllBudgetItemsByWeddingId(5).isEmpty());
        verify(weddingClient).ensureWeddingExists(5);
    }

    @Test
    void listForUnknownWeddingShouldNotQueryRepository() {
        doThrow(new WeddingNotFoundException(999)).when(weddingClient).ensureWeddingExists(999);

        assertThrows(WeddingNotFoundException.class,
                () -> service.readAllBudgetItemsByWeddingId(999));
        verifyNoInteractions(repository);
    }

    @Test
    void readExistingItemShouldMapAllFields() {
        when(repository.findById(12)).thenReturn(Optional.of(item(12, 5, "Restaurant", "RUB", "200000.00")));

        BudgetItemResponse response = service.readBudgetItemById(12);

        assertEquals(12, response.id());
        assertEquals(5, response.weddingId());
        assertEquals("Restaurant", response.name());
        assertEquals(new BigDecimal("200000.00"), response.plannedAmount());
        assertEquals(CREATED_AT, response.createdAt());
    }

    @Test
    void readMissingItemShouldThrowNotFound() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(BudgetItemNotFoundException.class, () -> service.readBudgetItemById(999));
    }

    @Test
    void updateNameOnlyShouldPreserveAmountAndCurrency() {
        BudgetItem persisted = item(12, 5, "Before", "RUB", "1000.00");
        when(repository.findById(12)).thenReturn(Optional.of(persisted));

        BudgetItemResponse response = service.updateBudgetItem(12, new UpdateBudgetItemRequest("After", null));

        assertEquals("After", response.name());
        assertEquals(new BigDecimal("1000.00"), response.plannedAmount());
        assertEquals("RUB", response.currencyCode());
        assertEquals(CREATED_AT, response.createdAt());
        verify(repository).flush();
        verify(repository, never()).save(any());
    }

    @Test
    void updateAmountOnlyShouldPreserveName() {
        BudgetItem persisted = item(12, 5, "Restaurant", "RUB", "1000.00");
        when(repository.findById(12)).thenReturn(Optional.of(persisted));

        BudgetItemResponse response = service.updateBudgetItem(
                12, new UpdateBudgetItemRequest(null, new BigDecimal("2500.50")));

        assertEquals("Restaurant", response.name());
        assertEquals(new BigDecimal("2500.50"), response.plannedAmount());
    }

    @Test
    void updateBothFieldsShouldReturnUpdatedValues() {
        when(repository.findById(12)).thenReturn(Optional.of(item(12, 5, "Before", "EUR", "100.00")));

        BudgetItemResponse response = service.updateBudgetItem(
                12, new UpdateBudgetItemRequest("After", new BigDecimal("300.00")));

        assertEquals("After", response.name());
        assertEquals(new BigDecimal("300.00"), response.plannedAmount());
        assertEquals("EUR", response.currencyCode());
    }

    @Test
    void updateWithNoFieldsShouldNotChangeExistingValues() {
        when(repository.findById(12)).thenReturn(Optional.of(item(12, 5, "Before", "RUB", "1000.00")));

        BudgetItemResponse response = service.updateBudgetItem(12, new UpdateBudgetItemRequest(null, null));

        assertEquals("Before", response.name());
        assertEquals(new BigDecimal("1000.00"), response.plannedAmount());
    }

    @Test
    void updateUnknownItemShouldThrowBeforeFlushing() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(BudgetItemNotFoundException.class,
                () -> service.updateBudgetItem(999, new UpdateBudgetItemRequest("New", null)));
        verify(repository, never()).flush();
    }

    @Test
    void deleteExistingItemShouldCallRepositoryDelete() {
        BudgetItem persisted = item(12, 5, "Restaurant", "RUB", "1000.00");
        when(repository.findById(12)).thenReturn(Optional.of(persisted));

        service.deleteBudgetItem(12);

        verify(repository).delete(persisted);
    }

    @Test
    void deleteItemWithPaymentsShouldThrowConflict() {
        when(repository.findById(12)).thenReturn(Optional.of(
                item(12, 5, "Restaurant", "RUB", "1000.00")));
        when(paymentRepository.existsByBudgetItemId(12)).thenReturn(true);

        assertThrows(BudgetItemHasPaymentsException.class, () -> service.deleteBudgetItem(12));
        verify(repository, never()).delete(any());
    }

    @Test
    void deleteUnknownItemShouldThrowBeforeDeleting() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(BudgetItemNotFoundException.class, () -> service.deleteBudgetItem(999));
        verify(repository, never()).delete(any());
    }

    private static BudgetItem item(int id, int weddingId, String name, String currency, String amount) {
        BudgetItem item = new BudgetItem();
        item.setId(id);
        item.setWeddingId(weddingId);
        item.setName(name);
        item.setCurrencyCode(currency);
        item.setPlannedAmount(new BigDecimal(amount));
        item.setCreatedAt(CREATED_AT);
        return item;
    }
}
