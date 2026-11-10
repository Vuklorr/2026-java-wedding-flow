package io.github.vuklorr.planningservice;

import com.sun.net.httpserver.HttpServer;
import io.github.vuklorr.planningservice.client.WeddingClient;
import io.github.vuklorr.planningservice.exception.WeddingNotFoundException;
import io.github.vuklorr.planningservice.exception.WeddingServiceUnavailableException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeddingClientTest {

    private final AtomicInteger responseStatus = new AtomicInteger(200);
    private final AtomicReference<String> requestedMethod = new AtomicReference<>();
    private final AtomicReference<String> requestedPath = new AtomicReference<>();

    private HttpServer server;
    private WeddingClient weddingClient;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/weddings/", exchange -> {
            requestedMethod.set(exchange.getRequestMethod());
            requestedPath.set(exchange.getRequestURI().getPath());
            exchange.sendResponseHeaders(responseStatus.get(), -1);
            exchange.close();
        });
        server.start();

        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        weddingClient = new WeddingClient(RestClient.builder(), baseUrl);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void existingWeddingShouldMakeGetRequestAndReturnNormally() {
        responseStatus.set(200);

        assertDoesNotThrow(() -> weddingClient.ensureWeddingExists(5));

        assertEquals("GET", requestedMethod.get());
        assertEquals("/weddings/5", requestedPath.get());
    }

    @Test
    void missingWeddingShouldThrowWeddingNotFoundException() {
        responseStatus.set(404);

        WeddingNotFoundException exception = assertThrows(
                WeddingNotFoundException.class,
                () -> weddingClient.ensureWeddingExists(999));

        assertTrue(exception.getMessage().contains("999"));
        assertEquals("/weddings/999", requestedPath.get());
    }

    @Test
    void internalServerErrorShouldThrowWeddingServiceUnavailableException() {
        responseStatus.set(500);

        assertThrows(WeddingServiceUnavailableException.class,
                () -> weddingClient.ensureWeddingExists(5));
    }

    @Test
    void serviceUnavailableResponseShouldThrowWeddingServiceUnavailableException() {
        responseStatus.set(503);

        assertThrows(WeddingServiceUnavailableException.class,
                () -> weddingClient.ensureWeddingExists(5));
    }

    @Test
    void connectionFailureShouldThrowWeddingServiceUnavailableException() {
        // Keep the client configured for this port, but stop the server
        // before the first request to simulate an unavailable service.
        server.stop(0);
        server = null;

        assertThrows(WeddingServiceUnavailableException.class,
                () -> weddingClient.ensureWeddingExists(5));
    }
}
