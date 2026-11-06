package io.github.vuklorr.planningservice;

import io.github.vuklorr.planningservice.dto.CreateWeddingTaskRequest;
import io.github.vuklorr.planningservice.dto.WeddingTaskResponse;
import io.github.vuklorr.planningservice.entity.TaskPriority;
import io.github.vuklorr.planningservice.entity.TaskStatus;
import io.github.vuklorr.planningservice.exception.WeddingTaskNotFoundException;
import io.github.vuklorr.planningservice.repository.WeddingTaskRepository;
import io.github.vuklorr.planningservice.service.WeddingTaskService;
import io.github.vuklorr.planningservice.service.impl.WeddingTaskServiceImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@DataJpaTest
@Testcontainers
@Import(WeddingTaskServiceImpl.class)
public class WeddingTaskRepositoryTest {

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
    private WeddingTaskRepository weddingTaskRepository;
    @Autowired
    private WeddingTaskService weddingTaskService;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void createAndReadWeddingTaskWithEmptyFields() {
        final int idWedding = 1;
        final String name = "First task";

        CreateWeddingTaskRequest createWeddingRequest =
                new CreateWeddingTaskRequest(
                        name,
                        null,
                        TaskPriority.MEDIUM,
                        TaskStatus.NEW,
                        null
                );

        WeddingTaskResponse weddingTaskResponse = weddingTaskService.createWeddingTask(createWeddingRequest, idWedding);

        assertNotNull(weddingTaskResponse.id());
        saveFlushToDB();

        WeddingTaskResponse weddingTaskActual = weddingTaskService.readWeddingTask(weddingTaskResponse.id());
        assertNotNull(weddingTaskActual.id());
        assertEquals(idWedding, weddingTaskActual.weddingId());
        assertEquals(name, weddingTaskActual.name());
        assertNull(weddingTaskActual.description());
        assertEquals(TaskPriority.MEDIUM, weddingTaskActual.priority());
        assertEquals(TaskStatus.NEW, weddingTaskActual.status());
        assertNull(weddingTaskActual.deadline());
        assertNotNull(weddingTaskActual.createdAt());
        assertNotNull(weddingTaskActual.updateAt());
        assertEquals(weddingTaskActual.createdAt(), weddingTaskActual.updateAt());
    }

    @Test
    public void shouldNotFoundWeddingTaskById() {
        final int id = 999;
        assertThrows(
                WeddingTaskNotFoundException.class,
                () -> weddingTaskService.readWeddingTask(id)
        );
    }


    private void saveFlushToDB() {
        weddingTaskRepository.flush();
        entityManager.clear();
    }
}
