package io.github.vuklorr.budgetservice;

import io.github.vuklorr.budgetservice.controller.PaymentController;
import io.github.vuklorr.budgetservice.dto.payment.*;
import io.github.vuklorr.budgetservice.exception.*;
import io.github.vuklorr.budgetservice.handler.GlobalExceptionBudgetServiceHandler;
import io.github.vuklorr.budgetservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionBudgetServiceHandler.class)
class PaymentControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean PaymentService service;

    @Test
    void createShouldReturn201AndDto() throws Exception {
        var request = new CreatePaymentRequest(new BigDecimal("500.00"), "EUR", new BigDecimal("95.00"));
        when(service.createPayment(request, 8)).thenReturn(response());
        mvc.perform(post("/budget/items/8/payments").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"amount":500.00,"currencyCode":"EUR","exchangeRateToItemCurrency":95.00}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(31))
                .andExpect(jsonPath("$.budgetItemId").value(8))
                .andExpect(jsonPath("$.currencyCode").value("EUR"))
                .andExpect(jsonPath("$.paymentDate").value("2026-11-20T12:00:00Z"));
        verify(service).createPayment(request, 8);
    }

    @Test
    void createShouldAllowSameCurrencyWithoutRate() throws Exception {
        when(service.createPayment(new CreatePaymentRequest(new BigDecimal("10.00"), "RUB", null), 8))
                .thenReturn(response());
        mvc.perform(post("/budget/items/8/payments").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"amount":10.00,"currencyCode":"RUB"}
                        """))
                .andExpect(status().isCreated());
    }

    @Test
    void missingAmountShouldReturn400() throws Exception {
        assertInvalidCreate("""
                {"currencyCode":"RUB"}
                """);
    }

    @Test
    void zeroAmountShouldReturn400() throws Exception {
        assertInvalidCreate("""
                {"amount":0,"currencyCode":"RUB"}
                """);
    }

    @Test
    void negativeAmountShouldReturn400() throws Exception {
        assertInvalidCreate("""
                {"amount":-5,"currencyCode":"RUB"}
                """);
    }

    @Test
    void malformedCurrencyShouldReturn400() throws Exception {
        assertInvalidCreate("""
                {"amount":5,"currencyCode":"rub"}
                """);
    }

    @Test
    void excessiveAmountPrecisionShouldReturn400() throws Exception {
        assertInvalidCreate("""
                {"amount":5.123,"currencyCode":"RUB"}
                """);
    }

    @Test
    void invalidRateShouldReturn400() throws Exception {
        assertInvalidCreate("""
                {"amount":5,"currencyCode":"EUR","exchangeRateToItemCurrency":0}
                """);
    }

    @Test
    void differentCurrencyWithoutRateShouldReturn400FromHandler() throws Exception {
        when(service.createPayment(new CreatePaymentRequest(new BigDecimal("5"), "EUR", null), 8))
                .thenThrow(new InvalidExchangeRateException("Exchange rate is required"));
        mvc.perform(post("/budget/items/8/payments").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"amount":5,"currencyCode":"EUR"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void unknownParentShouldReturn404() throws Exception {
        when(service.createPayment(new CreatePaymentRequest(new BigDecimal("5"), "RUB", null), 999))
                .thenThrow(new BudgetItemNotFoundException(999));
        mvc.perform(post("/budget/items/999/payments").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"amount":5,"currencyCode":"RUB"}
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    void listShouldReturn200AndPayments() throws Exception {
        when(service.readAllPaymentsByBudgetItem(8)).thenReturn(List.of(response()));
        mvc.perform(get("/budget/items/8/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(31));
    }

    @Test
    void listShouldReturnEmptyArray() throws Exception {
        when(service.readAllPaymentsByBudgetItem(8)).thenReturn(List.of());
        mvc.perform(get("/budget/items/8/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listMissingParentShouldReturn404() throws Exception {
        when(service.readAllPaymentsByBudgetItem(999)).thenThrow(new BudgetItemNotFoundException(999));
        mvc.perform(get("/budget/items/999/payments")).andExpect(status().isNotFound());
    }

    @Test
    void getPaymentShouldReturn200() throws Exception {
        when(service.readPaymentById(31)).thenReturn(response());
        mvc.perform(get("/budget/payments/31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(31));
    }

    @Test
    void getUnknownPaymentShouldReturn404() throws Exception {
        when(service.readPaymentById(999)).thenThrow(new PaymentNotFoundException(999));
        mvc.perform(get("/budget/payments/999")).andExpect(status().isNotFound());
    }

    @Test
    void patchAmountShouldReturn200() throws Exception {
        when(service.updatePayment(31, new UpdatePaymentRequest(new BigDecimal("125.00"))))
                .thenReturn(response());
        mvc.perform(patch("/budget/payments/31").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"amount":125.00}
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void patchWithoutAmountShouldReturn400() throws Exception {
        mvc.perform(patch("/budget/payments/31").contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void patchZeroAmountShouldReturn400() throws Exception {
        mvc.perform(patch("/budget/payments/31").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"amount":0}
                        """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void patchUnknownPaymentShouldReturn404() throws Exception {
        when(service.updatePayment(999, new UpdatePaymentRequest(new BigDecimal("1"))))
                .thenThrow(new PaymentNotFoundException(999));
        mvc.perform(patch("/budget/payments/999").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"amount":1}
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteShouldReturn204() throws Exception {
        mvc.perform(delete("/budget/payments/31")).andExpect(status().isNoContent());
        verify(service).deletePayment(31);
    }

    @Test
    void deleteMissingPaymentShouldReturn404() throws Exception {
        doThrow(new PaymentNotFoundException(999)).when(service).deletePayment(999);
        mvc.perform(delete("/budget/payments/999")).andExpect(status().isNotFound());
    }

    @Test
    void malformedJsonShouldReturn400() throws Exception {
        mvc.perform(post("/budget/items/8/payments").contentType(MediaType.APPLICATION_JSON)
                .content("{"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    private void assertInvalidCreate(String json) throws Exception {
        mvc.perform(post("/budget/items/8/payments").contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    private static PaymentResponse response() {
        return new PaymentResponse(31, 8, new BigDecimal("500.00"), "EUR",
                new BigDecimal("95.00"), Instant.parse("2026-11-20T12:00:00Z"));
    }
}
