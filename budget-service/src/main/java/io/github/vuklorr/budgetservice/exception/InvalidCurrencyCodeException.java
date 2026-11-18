package io.github.vuklorr.budgetservice.exception;

public class InvalidCurrencyCodeException extends RuntimeException {
    public InvalidCurrencyCodeException(String code) {
        super("Invalid currency code: " + code);
    }
}