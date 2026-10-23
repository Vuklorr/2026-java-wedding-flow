package io.github.vuklorr.weddingservice;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import io.github.vuklorr.weddingservice.dto.CreateWeddingRequest;
import io.github.vuklorr.weddingservice.dto.WeddingResponse;
import io.github.vuklorr.weddingservice.repository.WeddingRepository;
import io.github.vuklorr.weddingservice.service.WeddingService;
import io.github.vuklorr.weddingservice.service.impl.WeddingServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class WeddingServiceTest {

    @Test
    public void createWedding_shouldGenerateIdAndCreatedAt() {
        final String title = "First wedding";
        final LocalDate date = LocalDate.of(2028, 9, 1);
        WeddingRepository weddingRepository = mock(WeddingRepository.class);
        WeddingService weddingService = new WeddingServiceImpl(weddingRepository);

        CreateWeddingRequest createWeddingRequest =
                new CreateWeddingRequest(
                        title,
                        date
                );

        WeddingResponse weddingResponse = weddingService.createWedding(createWeddingRequest);

        assertNotNull(weddingResponse.id());
        assertEquals(title, weddingResponse.title());
        assertEquals(date, weddingResponse.weddingDate());
        assertNotNull(weddingResponse.createdAt());
    }

    @Test
    public void shouldGetSuccessWeddingById() {
        final String title = "Second wedding";
        final LocalDate date = LocalDate.of(2030, 10, 11);
        WeddingRepository weddingRepository = mock(WeddingRepository.class);
        WeddingService weddingService = new WeddingServiceImpl(weddingRepository);

        CreateWeddingRequest createWeddingRequest =
                new CreateWeddingRequest(
                        title,
                        date
                );

        WeddingResponse createWeddingResponse = weddingService.createWedding(createWeddingRequest);
        WeddingResponse weddingResponse = weddingService.readWedding(createWeddingResponse.id());

        assertEquals(createWeddingResponse, weddingResponse);
    }

    @Test
    public void shouldNotFoundWeddingById() {
        final String title = "Found wedding";
        final LocalDate date = LocalDate.of(2035, 2, 2);
        WeddingRepository weddingRepository = mock(WeddingRepository.class);
        WeddingService weddingService = new WeddingServiceImpl(weddingRepository);

        CreateWeddingRequest createWeddingRequest =
                new CreateWeddingRequest(
                        title,
                        date
                );

        WeddingResponse createWeddingResponse = weddingService.createWedding(createWeddingRequest);
        WeddingResponse weddingResponse = weddingService.readWedding(createWeddingResponse.id() + 1);

        assertNull(weddingResponse);
    }
}
