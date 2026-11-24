package io.github.vuklorr.budgetservice;

import io.github.vuklorr.budgetservice.controller.BudgetSummaryController;
import io.github.vuklorr.budgetservice.dto.summary.*;
import io.github.vuklorr.budgetservice.exception.*;
import io.github.vuklorr.budgetservice.handler.GlobalExceptionBudgetServiceHandler;
import io.github.vuklorr.budgetservice.service.BudgetSummaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetSummaryController.class)
@Import(GlobalExceptionBudgetServiceHandler.class)
class BudgetSummaryControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean BudgetSummaryService service;

    @Test
    void summaryShouldReturnAmountsAndCurrency() throws Exception {
        when(service.getSummary(5)).thenReturn(new BudgetSummaryResponse(5, List.of(
                new CurrencySummaryResponse("RUB", new BigDecimal("1000.00"),
                        new BigDecimal("250.00"), new BigDecimal("750.00")))));
        mvc.perform(get("/weddings/5/budget/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weddingId").value(5))
                .andExpect(jsonPath("$.totals.length()").value(1))
                .andExpect(jsonPath("$.totals[0].currencyCode").value("RUB"))
                .andExpect(jsonPath("$.totals[0].plannedAmount").value(1000.00))
                .andExpect(jsonPath("$.totals[0].paidAmount").value(250.00))
                .andExpect(jsonPath("$.totals[0].remainingAmount").value(750.00));
        verify(service).getSummary(5);
    }

    @Test
    void emptyBudgetShouldReturnEmptyTotals() throws Exception {
        when(service.getSummary(5)).thenReturn(new BudgetSummaryResponse(5, List.of()));
        mvc.perform(get("/weddings/5/budget/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals.length()").value(0));
    }

    @Test
    void missingWeddingShouldReturn404() throws Exception {
        when(service.getSummary(999)).thenThrow(new WeddingNotFoundException(999));
        mvc.perform(get("/weddings/999/budget/summary"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unavailableWeddingServiceShouldReturn503() throws Exception {
        when(service.getSummary(5)).thenThrow(new WeddingServiceUnavailableException());
        mvc.perform(get("/weddings/5/budget/summary"))
                .andExpect(status().isServiceUnavailable());
    }
}
