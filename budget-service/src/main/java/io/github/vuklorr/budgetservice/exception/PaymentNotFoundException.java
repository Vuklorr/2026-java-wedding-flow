package io.github.vuklorr.budgetservice.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(int id) {
        super("Payment " + id + " not found");
    }
}
