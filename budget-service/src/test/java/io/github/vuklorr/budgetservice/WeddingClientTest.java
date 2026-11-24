package io.github.vuklorr.budgetservice;

import com.sun.net.httpserver.HttpServer;
import io.github.vuklorr.budgetservice.client.WeddingClient;
import io.github.vuklorr.budgetservice.exception.WeddingNotFoundException;
import io.github.vuklorr.budgetservice.exception.WeddingServiceUnavailableException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class WeddingClientTest {

    private final AtomicInteger responseStatus = new AtomicInteger(200);
    private final AtomicReference<String> requestedMethod = new AtomicReference<>();
    private final AtomicReference<String> requestedPath = new AtomicReference<>();

    private HttpServer server;
    private WeddingClient client;

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
        client = new WeddingClient(RestClient.builder(), baseUrl);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void existingWeddingShouldSendGetRequestAndSucceed() {
        responseStatus.set(200);

        assertDoesNotThrow(() -> client.ensureWeddingExists(5));

        assertEquals("GET", requestedMethod.get());
        assertEquals("/weddings/5", requestedPath.get());
    }

    @Test
    void missingWeddingShouldThrowWeddingNotFound() {
        responseStatus.set(404);

        WeddingNotFoundException exception = assertThrows(
                WeddingNotFoundException.class, () -> client.ensureWeddingExists(999));

        assertTrue(exception.getMessage().contains("999"));
        assertEquals("/weddings/999", requestedPath.get());
    }

    @Test
    void upstream500ShouldThrowWeddingServiceUnavailable() {
        responseStatus.set(500);

        assertThrows(WeddingServiceUnavailableException.class, () -> client.ensureWeddingExists(5));
    }

    @Test
    void upstream503ShouldThrowWeddingServiceUnavailable() {
        responseStatus.set(503);

        assertThrows(WeddingServiceUnavailableException.class, () -> client.ensureWeddingExists(5));
    }

    @Test
    void connectionRefusedShouldThrowWeddingServiceUnavailable() {
        // The client retains the previously assigned port; the HTTP server is stopped.
        server.stop(0);
        server = null;

        assertThrows(WeddingServiceUnavailableException.class, () -> client.ensureWeddingExists(5));
    }
}
