package io.github.vuklorr.budgetservice.service.impl;

import io.github.vuklorr.budgetservice.dto.payment.CreatePaymentRequest;
import io.github.vuklorr.budgetservice.dto.payment.PaymentResponse;
import io.github.vuklorr.budgetservice.dto.payment.UpdatePaymentRequest;
import io.github.vuklorr.budgetservice.exception.BudgetItemNotFoundException;
import io.github.vuklorr.budgetservice.exception.InvalidCurrencyCodeException;
import io.github.vuklorr.budgetservice.exception.PaymentNotFoundException;
import io.github.vuklorr.budgetservice.model.BudgetItem;
import io.github.vuklorr.budgetservice.model.Payment;
import io.github.vuklorr.budgetservice.repository.BudgetItemRepository;
import io.github.vuklorr.budgetservice.repository.PaymentRepository;
import io.github.vuklorr.budgetservice.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BudgetItemRepository budgetItemRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository, BudgetItemRepository budgetItemRepository) {
        this.paymentRepository = paymentRepository;
        this.budgetItemRepository = budgetItemRepository;
    }

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest createPaymentRequest, int budgetItemId) {
        String currencyCode = createPaymentRequest.currencyCode();
        BudgetItem budgetItem = budgetItemRepository.findById(budgetItemId)
                .orElseThrow(() -> new BudgetItemNotFoundException(budgetItemId));
        try {
            Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            throw new InvalidCurrencyCodeException("Currency code is invalid");
        }

        Payment payment = getPayment(createPaymentRequest, budgetItem, currencyCode);


        return getPaymentResponse(paymentRepository.save(payment));
    }

    @Override
    public List<PaymentResponse> readAllPaymentsByBudgetItem(int budgetItemId) {
        budgetItemRepository.findById(budgetItemId)
                .orElseThrow(() -> new BudgetItemNotFoundException(budgetItemId));

        return paymentRepository
                .findByBudgetItemId(budgetItemId)
                .stream()
                .map(this::getPaymentResponse)
                .toList();

    }

    @Override
    public PaymentResponse readPaymentById(int id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
        return getPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse updatePayment(int id, UpdatePaymentRequest updatePaymentRequest) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));

        if (updatePaymentRequest.amount() != null) {
            payment.setAmount(updatePaymentRequest.amount());
        }

        paymentRepository.flush();
        return getPaymentResponse(payment);
    }

    @Override
    public void deletePayment(int id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
        paymentRepository.delete(payment);
    }

    private Payment getPayment(CreatePaymentRequest createPaymentRequest, BudgetItem budgetItem, String currencyCode) {
        Payment payment = new Payment();
        payment.setBudgetItem(budgetItem);
        payment.setAmount(
                Objects.requireNonNullElse(
                        createPaymentRequest.amount(),
                        BigDecimal.ZERO
                )
        );

        payment.setCurrencyCode(currencyCode);

        payment.setExchangeRateToItemCurrency(
                Objects.requireNonNullElse(
                        createPaymentRequest.exchangeRateToItemCurrency(),
                        BigDecimal.ZERO
                )
        );
        return payment;
    }

    private PaymentResponse getPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBudgetItem().getId(),
                payment.getAmount(),
                payment.getCurrencyCode(),
                payment.getExchangeRateToItemCurrency(),
                payment.getPaymentDate()
        );
    }
}
