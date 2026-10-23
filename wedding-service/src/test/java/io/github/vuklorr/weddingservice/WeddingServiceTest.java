package io.github.vuklorr.weddingservice;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.vuklorr.weddingservice.dto.CreateWeddingRequest;
import io.github.vuklorr.weddingservice.dto.WeddingResponse;
import io.github.vuklorr.weddingservice.model.Wedding;
import io.github.vuklorr.weddingservice.repository.WeddingRepository;
import io.github.vuklorr.weddingservice.service.WeddingService;
import io.github.vuklorr.weddingservice.service.impl.WeddingServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

public class WeddingServiceTest {

    @Test
    public void createWedding_shouldGenerateIdAndCreatedAt() {
        final String title = "First wedding";
        final LocalDate date = LocalDate.of(2028, 9, 1);
        final Instant timestamp = Instant.ofEpochSecond(123456789);
        final int id = 1;
        WeddingRepository weddingRepository = mock(WeddingRepository.class);
        WeddingService weddingService = new WeddingServiceImpl(weddingRepository);

        CreateWeddingRequest createWeddingRequest =
                new CreateWeddingRequest(
                        title,
                        date
                );

        when(weddingRepository.save(any(Wedding.class)))
                .thenAnswer(invocation -> {
                    Wedding wedding = invocation.getArgument(0);
                    wedding.setId(id);
                    wedding.setTitle(title);
                    wedding.setWeddingDate(date);
                    wedding.setCreatedAt(timestamp);
                    return wedding;
                });

        WeddingResponse weddingResponse = weddingService.createWedding(createWeddingRequest);

        assertEquals(id, weddingResponse.id());
        assertEquals(title, weddingResponse.title());
        assertEquals(date, weddingResponse.weddingDate());
        assertEquals(timestamp, weddingResponse.createdAt());
    }

    @Test
    public void shouldGetSuccessWeddingById() {
        final String title = "Second wedding";
        final LocalDate date = LocalDate.of(2030, 10, 11);
        final int id = 1;
        final Instant timestamp = Instant.ofEpochSecond(123456789);
        Wedding wedding = new Wedding(id, title, date, timestamp);
        WeddingRepository weddingRepository = mock(WeddingRepository.class);
        WeddingService weddingService = new WeddingServiceImpl(weddingRepository);

        when(weddingRepository.findById(id))
                .thenReturn(Optional.of(wedding));

        WeddingResponse weddingResponse = weddingService.readWedding(wedding.getId());
        WeddingResponse expectedResponse = new WeddingResponse(id, title, date, timestamp);
        assertEquals(expectedResponse, weddingResponse);
    }

    @Test
    public void shouldNotFoundWeddingById() {
        final int id = 2;
        WeddingRepository weddingRepository = mock(WeddingRepository.class);
        WeddingService weddingService = new WeddingServiceImpl(weddingRepository);

        when(weddingRepository.findById(id))
                .thenReturn(Optional.empty());
        WeddingResponse weddingResponse = weddingService.readWedding(id);

        assertNull(weddingResponse);
    }
}
