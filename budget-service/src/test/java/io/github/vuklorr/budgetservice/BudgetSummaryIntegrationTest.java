package io.github.vuklorr.budgetservice;

import io.github.vuklorr.budgetservice.client.WeddingClient;
import io.github.vuklorr.budgetservice.dto.summary.*;
import io.github.vuklorr.budgetservice.model.*;
import io.github.vuklorr.budgetservice.repository.*;
import io.github.vuklorr.budgetservice.service.impl.BudgetSummaryServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@DataJpaTest
@Testcontainers
@Import(BudgetSummaryServiceImpl.class)
class BudgetSummaryIntegrationTest {
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

    @Autowired BudgetSummaryServiceImpl service;
    @Autowired BudgetItemRepository items;
    @Autowired PaymentRepository payments;
    @Autowired EntityManager entityManager;
    @MockitoBean WeddingClient weddingClient;

    @Test
    void summaryShouldReadActualPostgresDataAcrossCurrencies() {
        BudgetItem venue = items.saveAndFlush(item(5, "RUB", "200000.00"));
        BudgetItem photo = items.saveAndFlush(item(5, "RUB", "30000.00"));
        BudgetItem hotel = items.saveAndFlush(item(5, "EUR", "1000.00"));
        BudgetItem otherWedding = items.saveAndFlush(item(6, "RUB", "99999.00"));

        payments.saveAndFlush(payment(venue, "500.00", "EUR", "95.00"));
        payments.saveAndFlush(payment(venue, "10000.00", "RUB", "1"));
        payments.saveAndFlush(payment(photo, "5000.00", "RUB", "1"));
        payments.saveAndFlush(payment(hotel, "300.00", "EUR", "1"));
        payments.saveAndFlush(payment(otherWedding, "1000.00", "RUB", "1"));
        entityManager.clear();

        BudgetSummaryResponse result = service.getSummary(5);
        assertEquals(5, result.weddingId());
        assertEquals(List.of("EUR", "RUB"), result.totals().stream()
                .map(CurrencySummaryResponse::currencyCode).toList());
        CurrencySummaryResponse eur = result.totals().get(0);
        CurrencySummaryResponse rub = result.totals().get(1);
        assertMoney("1000.00", eur.plannedAmount());
        assertMoney("300.00", eur.paidAmount());
        assertMoney("700.00", eur.remainingAmount());
        assertMoney("230000.00", rub.plannedAmount());
        assertMoney("62500.00", rub.paidAmount());
        assertMoney("167500.00", rub.remainingAmount());
        verify(weddingClient).ensureWeddingExists(5);
    }

    @Test
    void summaryShouldIncludeItemWithoutPayments() {
        items.saveAndFlush(item(5, "RUB", "300.00"));
        entityManager.clear();
        CurrencySummaryResponse total = service.getSummary(5).totals().getFirst();
        assertMoney("300.00", total.plannedAmount());
        assertMoney("0.00", total.paidAmount());
        assertMoney("300.00", total.remainingAmount());
    }

    private static BudgetItem item(int weddingId, String currency, String planned) {
        BudgetItem item = new BudgetItem();
        item.setWeddingId(weddingId);
        item.setName("Expense");
        item.setCurrencyCode(currency);
        item.setPlannedAmount(new BigDecimal(planned));
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

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(new BigDecimal(expected), actual);
    }
}
