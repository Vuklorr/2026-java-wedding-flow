package io.github.vuklorr.weddingservice;


import io.github.vuklorr.weddingservice.dto.CreateWeddingRequest;
import io.github.vuklorr.weddingservice.dto.UpdateWeddingDateRequest;
import io.github.vuklorr.weddingservice.dto.WeddingResponse;
import io.github.vuklorr.weddingservice.repository.WeddingRepository;
import io.github.vuklorr.weddingservice.service.WeddingService;
import io.github.vuklorr.weddingservice.service.impl.WeddingServiceImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


@DataJpaTest
@Testcontainers
public class WeddingRepositoryTest {

    @Container
    private static final PostgreSQLContainer
            POSTGRES = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("test_wedding_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private WeddingRepository weddingRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void createAndReadWedding_shouldGenerateIdAndCreatedAt() {
        final String title = "First wedding";
        final LocalDate date = LocalDate.of(2028, 9, 1);
        WeddingService weddingService = new WeddingServiceImpl(weddingRepository);

        CreateWeddingRequest createWeddingRequest =
                new CreateWeddingRequest(
                        title,
                        date
                );

        WeddingResponse weddingResponse = weddingService.createWedding(createWeddingRequest);

        assertNotNull(weddingResponse.id());
        saveFlushToDB();

        WeddingResponse weddingActual = weddingService.readWedding(weddingResponse.id());
        assertNotNull(weddingActual.id());
        assertEquals(title, weddingActual.title());
        assertEquals(date, weddingActual.weddingDate());
        assertNotNull(weddingActual.createdAt());
    }


    @Test
    public void shouldNotFoundWeddingById() {
        final int id = 0;
        WeddingService weddingService = new WeddingServiceImpl(weddingRepository);
        WeddingResponse weddingResponse = weddingService.readWedding(id);

        assertNull(weddingResponse);
    }

    @Test
    public void shouldUpdateWeddingDateSuccess() {
        final String title = "First wedding";
        final LocalDate date = LocalDate.of(2028, 9, 1);
        final LocalDate newDate = LocalDate.of(2011, 3, 5);
        WeddingService weddingService = new WeddingServiceImpl(weddingRepository);

        CreateWeddingRequest createWeddingRequest =
                new CreateWeddingRequest(
                        title,
                        date
                );

        WeddingResponse weddingResponse = weddingService.createWedding(createWeddingRequest);

        assertNotNull(weddingResponse.id());
        saveFlushToDB();
        weddingService.updateWeddingDate(weddingResponse.id(), new UpdateWeddingDateRequest(newDate));
        saveFlushToDB();
        WeddingResponse weddingActual = weddingService.readWedding(weddingResponse.id());
        assertNotEquals(date, weddingActual.weddingDate());
        assertEquals(newDate, weddingActual.weddingDate());
    }

    private void saveFlushToDB() {
        weddingRepository.flush();
        entityManager.clear();
    }
}
