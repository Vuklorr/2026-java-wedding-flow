package io.github.vuklorr.budgetservice.controller;

import io.github.vuklorr.budgetservice.dto.payment.CreatePaymentRequest;
import io.github.vuklorr.budgetservice.dto.payment.PaymentResponse;
import io.github.vuklorr.budgetservice.dto.payment.UpdatePaymentRequest;
import io.github.vuklorr.budgetservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(value = "/budget/items/{itemId}/payments")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable(name = "itemId") int itemId,
            @Valid @RequestBody CreatePaymentRequest createPaymentRequest

    ) {
        final PaymentResponse paymentResponse = paymentService.createPayment(createPaymentRequest, itemId);
        return new ResponseEntity<>(paymentResponse, HttpStatus.CREATED);
    }

    @GetMapping(value = "/budget/items/{itemId}/payments")
    public ResponseEntity<List<PaymentResponse>> readAllPayments(@PathVariable(name = "itemId") int itemId) {
        final List<PaymentResponse> paymentResponses = paymentService.readAllPaymentsByBudgetItem(itemId);

        return new ResponseEntity<>(paymentResponses, HttpStatus.OK);
    }

    @GetMapping(value = "/budget/payments/{paymentId}")
    public ResponseEntity<PaymentResponse> readPayment(@PathVariable(name = "paymentId") int id) {
        final PaymentResponse paymentResponse = paymentService.readPaymentById(id);

        return new ResponseEntity<>(paymentResponse, HttpStatus.OK);
    }

    @PatchMapping(value = "/budget/payments/{paymentId}")
    public ResponseEntity<PaymentResponse> updatePayment(
            @PathVariable(name = "paymentId") int id,
            @Valid @RequestBody UpdatePaymentRequest updatePaymentRequest) {
        final PaymentResponse paymentResponse = paymentService.updatePayment(id, updatePaymentRequest);

        return new ResponseEntity<>(paymentResponse, HttpStatus.OK);
    }

    @DeleteMapping(value = "/budget/payments/{paymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable(name = "paymentId") int id) {
        paymentService.deletePayment(id);

        return ResponseEntity.noContent().build();
    }
}
