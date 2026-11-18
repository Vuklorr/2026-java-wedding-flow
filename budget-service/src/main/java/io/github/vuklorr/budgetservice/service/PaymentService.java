package io.github.vuklorr.budgetservice.service;

import io.github.vuklorr.budgetservice.dto.payment.CreatePaymentRequest;
import io.github.vuklorr.budgetservice.dto.payment.PaymentResponse;
import io.github.vuklorr.budgetservice.dto.payment.UpdatePaymentRequest;

import java.util.List;

public interface PaymentService {
    PaymentResponse createPayment(CreatePaymentRequest createPaymentRequest, int budgetItemId);
    List<PaymentResponse> readAllPaymentsByBudgetItem(int budgetItemId);
    PaymentResponse readPaymentById(int id);
    PaymentResponse updatePayment(int id, UpdatePaymentRequest updatePaymentRequest);
    void deletePayment(int id);

}
