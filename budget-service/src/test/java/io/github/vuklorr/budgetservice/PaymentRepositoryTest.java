package io.github.vuklorr.budgetservice;

import io.github.vuklorr.budgetservice.model.*;
import io.github.vuklorr.budgetservice.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
class PaymentRepositoryTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("test_budget_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired BudgetItemRepository items;
    @Autowired PaymentRepository payments;
    @Autowired EntityManager entityManager;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void paymentShouldPersistAssociationAndGeneratePaymentDate() {
        BudgetItem item = items.saveAndFlush(item(5, "RUB"));
        Payment created = payments.saveAndFlush(payment(item, "500.00", "EUR", "95.00"));
        assertNotNull(created.getId());
        assertNotNull(created.getPaymentDate());
        entityManager.clear();

        Payment loaded = payments.findById(created.getId()).orElseThrow();
        assertEquals(item.getId(), loaded.getBudgetItem().getId());
        assertEquals(new BigDecimal("500.00"), loaded.getAmount());
        assertEquals("EUR", loaded.getCurrencyCode());
        assertEquals(0, loaded.getExchangeRateToItemCurrency().compareTo(new BigDecimal("95.00")));
        assertNotNull(loaded.getPaymentDate());
    }

    @Test
    void findByBudgetItemIdShouldNotReturnPaymentsOfOtherItem() {
        BudgetItem first = items.saveAndFlush(item(5, "RUB"));
        BudgetItem second = items.saveAndFlush(item(5, "EUR"));
        payments.saveAndFlush(payment(first, "10.00", "RUB", "1"));
        payments.saveAndFlush(payment(first, "20.00", "RUB", "1"));
        payments.saveAndFlush(payment(second, "30.00", "EUR", "1"));
        entityManager.clear();

        assertEquals(2, payments.findByBudgetItemId(first.getId()).size());
        assertEquals(1, payments.findByBudgetItemId(second.getId()).size());
        assertTrue(payments.existsByBudgetItemId(first.getId()));
    }

    @Test
    void findByBudgetItemIdsShouldFetchSeveralItemsAndExcludeOthers() {
        BudgetItem first = items.saveAndFlush(item(5, "RUB"));
        BudgetItem second = items.saveAndFlush(item(5, "EUR"));
        BudgetItem excluded = items.saveAndFlush(item(6, "USD"));
        payments.saveAndFlush(payment(first, "10.00", "RUB", "1"));
        payments.saveAndFlush(payment(second, "20.00", "EUR", "1"));
        payments.saveAndFlush(payment(excluded, "30.00", "USD", "1"));
        entityManager.clear();

        var found = payments.findByBudgetItemIdIn(java.util.List.of(first.getId(), second.getId()));
        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(payment ->
                payment.getBudgetItem().getId().equals(first.getId()) ||
                payment.getBudgetItem().getId().equals(second.getId())));
    }

    @Test
    void paymentZeroAmountShouldViolateDatabaseConstraint() {
        BudgetItem item = items.saveAndFlush(item(5, "RUB"));
        assertThrows(DataIntegrityViolationException.class,
                () -> payments.saveAndFlush(payment(item, "0.00", "RUB", "1")));
    }

    @Test
    void paymentZeroRateShouldViolateDatabaseConstraint() {
        BudgetItem item = items.saveAndFlush(item(5, "RUB"));
        assertThrows(DataIntegrityViolationException.class,
                () -> payments.saveAndFlush(payment(item, "10.00", "RUB", "0")));
    }

    @Test
    void deletingItemWithPaymentShouldBeBlockedByForeignKey() {
        BudgetItem item = items.saveAndFlush(item(5, "RUB"));

        payments.saveAndFlush(
                payment(item, "10.00", "RUB", "1")
        );

        Integer itemId = item.getId();

        entityManager.flush();
        entityManager.clear();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        "DELETE FROM budget_item WHERE id = ?",
                        itemId
                )
        );
    }

    @Test
    void deletingPaymentShouldKeepParentItem() {
        BudgetItem item = items.saveAndFlush(item(5, "RUB"));
        Payment payment = payments.saveAndFlush(payment(item, "10.00", "RUB", "1"));
        payments.delete(payment);
        payments.flush();
        entityManager.clear();
        assertTrue(payments.findById(payment.getId()).isEmpty());
        assertTrue(items.findById(item.getId()).isPresent());
    }

    private static BudgetItem item(int weddingId, String currencyCode) {
        BudgetItem item = new BudgetItem();
        item.setWeddingId(weddingId);
        item.setName("Venue");
        item.setPlannedAmount(new BigDecimal("1000.00"));
        item.setCurrencyCode(currencyCode);
        return item;
    }

    private static Payment payment(BudgetItem item, String amount, String currency, String rate) {
        Payment payment = new Payment();
        payment.setBudgetItem(item);
        payment.setAmount(new BigDecimal(amount));
        payment.setCurrencyCode(currency);
        payment.setExchangeRateToItemCurrency(new BigDecimal(rate));
        return payment;
    }
}
