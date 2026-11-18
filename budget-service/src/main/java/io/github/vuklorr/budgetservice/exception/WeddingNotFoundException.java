package io.github.vuklorr.budgetservice.exception;

public class WeddingNotFoundException extends RuntimeException {
    public WeddingNotFoundException(int id) {
        super("Wedding " + id + " not found");
    }
}
