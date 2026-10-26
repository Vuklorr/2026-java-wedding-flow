package io.github.vuklorr.weddingservice.exception;

public class WeddingNotFoundException extends RuntimeException {
    public WeddingNotFoundException(int id) {
        super("Wedding " + id + " not found");
    }
}
