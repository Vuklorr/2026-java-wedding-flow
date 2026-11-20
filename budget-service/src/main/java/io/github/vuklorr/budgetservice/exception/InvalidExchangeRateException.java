package io.github.vuklorr.budgetservice.exception;

public class InvalidExchangeRateException extends RuntimeException {
    public InvalidExchangeRateException(String message) {
        super(message);
    }
}
