package io.github.vuklorr.weddingservice;

import io.github.vuklorr.weddingservice.dto.CreateWeddingRequest;
import io.github.vuklorr.weddingservice.dto.UpdateWeddingDateRequest;
import io.github.vuklorr.weddingservice.dto.WeddingResponse;
import io.github.vuklorr.weddingservice.exception.WeddingNotFoundException;
import io.github.vuklorr.weddingservice.repository.WeddingRepository;
import io.github.vuklorr.weddingservice.service.WeddingService;
import io.github.vuklorr.weddingservice.service.impl.WeddingServiceImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@DataJpaTest
@Testcontainers
@Import(WeddingServiceImpl.class)
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
    @Autowired
    private WeddingService weddingService;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void createAndReadWedding_shouldGenerateIdAndCreatedAt() {
        final String title = "First wedding";
        final LocalDate date = LocalDate.of(2028, 9, 1);

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
        final int id = 999;
        assertThrows(
                WeddingNotFoundException.class,
                () -> weddingService.readWedding(id)
        );
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void shouldUpdateWeddingDateSuccess() {
        final String title = "First wedding";
        final LocalDate date = LocalDate.of(2028, 9, 1);
        final LocalDate newDate = LocalDate.of(2011, 3, 5);

        CreateWeddingRequest createWeddingRequest =
                new CreateWeddingRequest(
                        title,
                        date
                );

        WeddingResponse weddingResponse = weddingService.createWedding(createWeddingRequest);

        assertNotNull(weddingResponse.id());
        weddingService.updateWeddingDate(weddingResponse.id(), new UpdateWeddingDateRequest(newDate));
        WeddingResponse weddingActual = weddingService.readWedding(weddingResponse.id());
        assertNotEquals(date, weddingActual.weddingDate());
        assertEquals(newDate, weddingActual.weddingDate());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void shouldDeleteWeddingSuccess() {
        final String title = "First wedding";
        final LocalDate date = LocalDate.of(2028, 9, 1);

        CreateWeddingRequest createWeddingRequest =
                new CreateWeddingRequest(
                        title,
                        date
                );

        WeddingResponse weddingResponse = weddingService.createWedding(createWeddingRequest);
        assertNotNull(weddingResponse.id());
        weddingService.deleteWedding(weddingResponse.id());
        assertThrows(
                WeddingNotFoundException.class,
                () -> weddingService.readWedding(weddingResponse.id())
        );
    }

    @Test
    public void shouldDeleteWeddingNotFoundId() {
        final int id = 0;

        assertThrows(
                WeddingNotFoundException.class,
                () -> weddingService.deleteWedding(id)
        );
    }

    @Test
    public void shouldUpdateDateWeddingNotFoundId() {
        final int id = 0;
        final LocalDate date = LocalDate.of(2028, 9, 1);
        assertThrows(
                WeddingNotFoundException.class,
                () -> weddingService.updateWeddingDate(id, new UpdateWeddingDateRequest(date))
        );
    }

    private void saveFlushToDB() {
        weddingRepository.flush();
        entityManager.clear();
    }
}
