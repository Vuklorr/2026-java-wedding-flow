package io.github.vuklorr.planningservice.exception;

public class WeddingServiceUnavailableException extends RuntimeException {
    public WeddingServiceUnavailableException() {
        super("Wedding Service is unavailable");
    }
}
