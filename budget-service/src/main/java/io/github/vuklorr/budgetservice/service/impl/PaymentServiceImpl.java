package io.github.vuklorr.budgetservice.service.impl;

import io.github.vuklorr.budgetservice.dto.payment.CreatePaymentRequest;
import io.github.vuklorr.budgetservice.dto.payment.PaymentResponse;
import io.github.vuklorr.budgetservice.dto.payment.UpdatePaymentRequest;
import io.github.vuklorr.budgetservice.exception.BudgetItemNotFoundException;
import io.github.vuklorr.budgetservice.exception.InvalidExchangeRateException;
import io.github.vuklorr.budgetservice.exception.PaymentNotFoundException;
import io.github.vuklorr.budgetservice.model.BudgetItem;
import io.github.vuklorr.budgetservice.model.Payment;
import io.github.vuklorr.budgetservice.repository.BudgetItemRepository;
import io.github.vuklorr.budgetservice.repository.PaymentRepository;
import io.github.vuklorr.budgetservice.service.PaymentService;
import io.github.vuklorr.budgetservice.validation.CurrencyValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final CurrencyValidator currencyValidator;

    public PaymentServiceImpl(PaymentRepository paymentRepository, BudgetItemRepository budgetItemRepository, CurrencyValidator currencyValidator) {
        this.paymentRepository = paymentRepository;
        this.budgetItemRepository = budgetItemRepository;
        this.currencyValidator = currencyValidator;
    }

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest createPaymentRequest, int budgetItemId) {
        String currencyCode = createPaymentRequest.currencyCode();
        BudgetItem budgetItem = budgetItemRepository.findById(budgetItemId)
                .orElseThrow(() -> new BudgetItemNotFoundException(budgetItemId));

        currencyValidator.validateCurrencyCode(createPaymentRequest.currencyCode());
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
        payment.setAmount(createPaymentRequest.amount());

        payment.setCurrencyCode(currencyCode);

        payment.setExchangeRateToItemCurrency(getExchangeRateToItemCurrency(
                currencyCode,
                budgetItem.getCurrencyCode(),
                createPaymentRequest
        ));

        return payment;
    }

    private BigDecimal getExchangeRateToItemCurrency(
            String currencyCode,
            String budgetCurrencyCode,
            CreatePaymentRequest request
    ) {
        BigDecimal exchangeRate = request.exchangeRateToItemCurrency();

        if (currencyCode.equals(budgetCurrencyCode)) {
            if (exchangeRate != null
                    && exchangeRate.compareTo(BigDecimal.ONE) != 0) {
                throw new InvalidExchangeRateException(
                        "Exchange rate must be 1 for the same currency"
                );
            }

            return BigDecimal.ONE;
        }

        if (exchangeRate == null) {
            throw new InvalidExchangeRateException(
                    "Exchange rate is required for different currencies"
            );
        }

        return exchangeRate;
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
