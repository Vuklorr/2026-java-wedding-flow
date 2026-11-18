package io.github.vuklorr.budgetservice.exception;

public class WeddingServiceUnavailableException extends RuntimeException {
    public WeddingServiceUnavailableException() {
        super("Wedding Service is unavailable");
    }
}
