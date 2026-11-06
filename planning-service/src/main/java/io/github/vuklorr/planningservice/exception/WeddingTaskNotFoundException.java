package io.github.vuklorr.planningservice.exception;

public class WeddingTaskNotFoundException extends RuntimeException {
    public WeddingTaskNotFoundException(int id) {
        super("Wedding Task " + id + " not found");
    }
}
