package io.github.vuklorr.planningservice;

import io.github.vuklorr.planningservice.client.WeddingClient;
import io.github.vuklorr.planningservice.dto.CreateWeddingTaskRequest;
import io.github.vuklorr.planningservice.dto.WeddingTaskResponse;
import io.github.vuklorr.planningservice.entity.TaskPriority;
import io.github.vuklorr.planningservice.entity.TaskStatus;
import io.github.vuklorr.planningservice.exception.WeddingTaskNotFoundException;
import io.github.vuklorr.planningservice.model.WeddingTask;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@Import(WeddingTaskServiceImpl.class)
class WeddingTaskRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("test_planning_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private WeddingTaskRepository repository;

    @Autowired
    private WeddingTaskService service;

    @PersistenceContext
    private EntityManager entityManager;

    @MockitoBean
    private WeddingClient weddingClient;

    @Test
    void createAndReadTaskShouldPersistEnumsAndGenerateTimestamps() {
        CreateWeddingTaskRequest request = new CreateWeddingTaskRequest(
                "Book restaurant", "50 guests", TaskPriority.HIGH, LocalDate.of(2027, 7, 15));

        WeddingTaskResponse created = service.createWeddingTask(request, 5);
        assertNotNull(created.id());
        flushAndClear();

        WeddingTaskResponse loaded = service.readWeddingTask(created.id());
        assertEquals(5, loaded.weddingId());
        assertEquals("Book restaurant", loaded.name());
        assertEquals("50 guests", loaded.description());
        assertEquals(TaskPriority.HIGH, loaded.priority());
        assertEquals(TaskStatus.NEW, loaded.status());
        assertEquals(LocalDate.of(2027, 7, 15), loaded.deadline());
        assertNotNull(loaded.createdAt());
        assertNotNull(loaded.updatedAt());
        assertEquals(loaded.createdAt(), loaded.updatedAt());
    }

    @Test
    void createWithOnlyNameShouldPersistDefaultsAndNullOptionalFields() {
        WeddingTaskResponse created = service.createWeddingTask(
                new CreateWeddingTaskRequest("Choose music", null, null, null), 5);
        flushAndClear();

        WeddingTaskResponse loaded = service.readWeddingTask(created.id());
        assertEquals(TaskPriority.MEDIUM, loaded.priority());
        assertEquals(TaskStatus.NEW, loaded.status());
        assertNull(loaded.description());
        assertNull(loaded.deadline());
    }

    @Test
    void findByWeddingIdShouldNotReturnTasksOfAnotherWedding() {
        repository.saveAndFlush(newTask(5, "First wedding task"));
        repository.saveAndFlush(newTask(6, "Another wedding task"));
        repository.saveAndFlush(newTask(5, "Second wedding task"));
        flushAndClear();

        var result = repository.findByWeddingId(5);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(task -> task.getWeddingId() == 5));
    }

    @Test
    void readMissingTaskShouldThrowNotFoundException() {
        assertThrows(WeddingTaskNotFoundException.class, () -> service.readWeddingTask(999999));
    }

    @Test
    void updateTaskShouldPreserveCreatedAtAndAdvanceUpdatedAt() throws InterruptedException {
        WeddingTask task = repository.saveAndFlush(newTask(5, "Initial name"));
        Integer id = task.getId();
        flushAndClear();

        WeddingTask persisted = repository.findById(id).orElseThrow();
        Instant createdAt = persisted.getCreatedAt();
        Instant previousUpdatedAt = persisted.getUpdatedAt();
        assertNotNull(createdAt);
        assertNotNull(previousUpdatedAt);

        // Avoid a flaky strict timestamp comparison if two operations occur in one clock tick.
        Thread.sleep(20);
        persisted.setName("Changed name");
        repository.flush(); // Execute SQL UPDATE and PostgreSQL trigger.
        entityManager.clear();

        WeddingTask updated = repository.findById(id).orElseThrow();
        assertEquals("Changed name", updated.getName());
        assertEquals(createdAt, updated.getCreatedAt());
        assertTrue(updated.getUpdatedAt().isAfter(previousUpdatedAt),
                "PostgreSQL trigger should refresh updated_at");
    }

    @Test
    void deleteTaskShouldRemoveItFromPostgres() {
        WeddingTask created = repository.saveAndFlush(newTask(5, "Delete me"));
        Integer id = created.getId();
        flushAndClear();

        service.deleteWeddingTask(id);
        flushAndClear();

        assertTrue(repository.findById(id).isEmpty());
    }

    private static WeddingTask newTask(int weddingId, String name) {
        WeddingTask task = new WeddingTask();
        task.setWeddingId(weddingId);
        task.setName(name);
        return task;
    }

    private void flushAndClear() {
        repository.flush();
        entityManager.clear();
    }
}
