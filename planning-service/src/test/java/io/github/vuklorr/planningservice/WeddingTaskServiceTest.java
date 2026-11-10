package io.github.vuklorr.planningservice;

import io.github.vuklorr.planningservice.client.WeddingClient;
import io.github.vuklorr.planningservice.dto.CreateWeddingTaskRequest;
import io.github.vuklorr.planningservice.dto.UpdateWeddingTaskRequest;
import io.github.vuklorr.planningservice.dto.WeddingTaskResponse;
import io.github.vuklorr.planningservice.entity.TaskPriority;
import io.github.vuklorr.planningservice.entity.TaskStatus;
import io.github.vuklorr.planningservice.exception.WeddingTaskNotFoundException;
import io.github.vuklorr.planningservice.model.WeddingTask;
import io.github.vuklorr.planningservice.repository.WeddingTaskRepository;
import io.github.vuklorr.planningservice.service.impl.WeddingTaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WeddingTaskServiceTest {

    private WeddingTaskRepository repository;
    private WeddingTaskServiceImpl service;
    private WeddingClient client;

    @BeforeEach
    void setUp() {
        repository = mock(WeddingTaskRepository.class);
        client = mock(WeddingClient.class);
        service = new WeddingTaskServiceImpl(repository, client);
    }

    @Test
    void createShouldSaveAllProvidedFields() {
        CreateWeddingTaskRequest request = new CreateWeddingTaskRequest(
                "Book a venue", "50 guests", TaskPriority.HIGH, LocalDate.of(2027, 6, 15));
        when(repository.save(any(WeddingTask.class))).thenAnswer(invocation -> {
            WeddingTask task = invocation.getArgument(0);
            task.setId(42); // Mock only generated id, never supply domain fields.
            return task;
        });

        WeddingTaskResponse response = service.createWeddingTask(request, 5);

        assertEquals(42, response.id());
        assertEquals(5, response.weddingId());
        assertEquals("Book a venue", response.name());
        assertEquals("50 guests", response.description());
        assertEquals(TaskPriority.HIGH, response.priority());
        assertEquals(TaskStatus.NEW, response.status());
        assertEquals(LocalDate.of(2027, 6, 15), response.deadline());
        verify(repository).save(any(WeddingTask.class));
    }

    @Test
    void createWithoutOptionalFieldsShouldUseDefaults() {
        CreateWeddingTaskRequest request = new CreateWeddingTaskRequest("Choose music", null, null, null);
        when(repository.save(any(WeddingTask.class))).thenAnswer(invocation -> {
            WeddingTask task = invocation.getArgument(0);
            task.setId(7);
            return task;
        });

        WeddingTaskResponse result = service.createWeddingTask(request, 5);

        assertEquals(TaskPriority.MEDIUM, result.priority());
        assertEquals(TaskStatus.NEW, result.status());
        assertNull(result.description());
        assertNull(result.deadline());
    }

    @Test
    void listShouldContainOnlyTasksOfRequestedWedding() {
        when(repository.findByWeddingId(5)).thenReturn(List.of(
                existingTask(10, 5, "First"), existingTask(12, 5, "Second")));

        List<WeddingTaskResponse> result = service.readAllWeddingTasksByWeddingId(5);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(task -> task.weddingId() == 5));
        assertEquals(List.of("First", "Second"), result.stream().map(WeddingTaskResponse::name).toList());
        verify(repository).findByWeddingId(5);
        verify(repository, never()).findAll();
    }

    @Test
    void listForWeddingWithoutTasksShouldBeEmpty() {
        when(repository.findByWeddingId(5)).thenReturn(List.of());
        assertTrue(service.readAllWeddingTasksByWeddingId(5).isEmpty());
        verify(repository).findByWeddingId(5);
    }

    @Test
    void readExistingTaskShouldReturnDto() {
        when(repository.findById(10)).thenReturn(Optional.of(existingTask(10, 5, "Book venue")));

        WeddingTaskResponse result = service.readWeddingTask(10);

        assertEquals(10, result.id());
        assertEquals(5, result.weddingId());
        assertEquals("Book venue", result.name());
        assertEquals(TaskStatus.NEW, result.status());
    }

    @Test
    void readUnknownTaskShouldThrow404DomainException() {
        when(repository.findById(999)).thenReturn(Optional.empty());
        assertThrows(WeddingTaskNotFoundException.class, () -> service.readWeddingTask(999));
    }

    @Test
    void patchOnlyStatusShouldPreserveOtherFields() {
        WeddingTask task = existingTask(10, 5, "Book venue");
        when(repository.findById(10)).thenReturn(Optional.of(task));
        UpdateWeddingTaskRequest request = new UpdateWeddingTaskRequest(
                null, null, null, TaskStatus.DONE, null);

        WeddingTaskResponse result = service.updateWeddingTask(10, request);

        assertEquals("Book venue", result.name());
        assertEquals("50 guests", result.description());
        assertEquals(TaskPriority.HIGH, result.priority());
        assertEquals(TaskStatus.DONE, result.status());
        assertEquals(LocalDate.of(2027, 6, 15), result.deadline());
    }

    @Test
    void patchTwoFieldsShouldPreserveTheRest() {
        WeddingTask task = existingTask(10, 5, "Book venue");
        when(repository.findById(10)).thenReturn(Optional.of(task));
        UpdateWeddingTaskRequest request = new UpdateWeddingTaskRequest(
                "Book restaurant", null, TaskPriority.LOW, null, null);

        WeddingTaskResponse result = service.updateWeddingTask(10, request);

        assertEquals("Book restaurant", result.name());
        assertEquals("50 guests", result.description());
        assertEquals(TaskPriority.LOW, result.priority());
        assertEquals(TaskStatus.NEW, result.status());
        assertEquals(LocalDate.of(2027, 6, 15), result.deadline());
    }

    @Test
    void patchAllFieldsShouldUpdateEverythingEditable() {
        WeddingTask task = existingTask(10, 5, "Book venue");
        when(repository.findById(10)).thenReturn(Optional.of(task));
        UpdateWeddingTaskRequest request = new UpdateWeddingTaskRequest(
                "New name", "New description", TaskPriority.MEDIUM,
                TaskStatus.IN_PROGRESS, LocalDate.of(2027, 8, 1));

        WeddingTaskResponse result = service.updateWeddingTask(10, request);

        assertEquals("New name", result.name());
        assertEquals("New description", result.description());
        assertEquals(TaskPriority.MEDIUM, result.priority());
        assertEquals(TaskStatus.IN_PROGRESS, result.status());
        assertEquals(LocalDate.of(2027, 8, 1), result.deadline());
        assertEquals(5, result.weddingId());
    }

    @Test
    void patchEmptyRequestShouldNotChangeTask() {
        WeddingTask task = existingTask(10, 5, "Book venue");
        when(repository.findById(10)).thenReturn(Optional.of(task));

        WeddingTaskResponse result = service.updateWeddingTask(10,
                new UpdateWeddingTaskRequest(null, null, null, null, null));

        assertEquals("Book venue", result.name());
        assertEquals("50 guests", result.description());
        assertEquals(TaskPriority.HIGH, result.priority());
        assertEquals(TaskStatus.NEW, result.status());
        assertEquals(LocalDate.of(2027, 6, 15), result.deadline());
    }

    @Test
    void patchMissingTaskShouldThrow() {
        when(repository.findById(999)).thenReturn(Optional.empty());
        assertThrows(WeddingTaskNotFoundException.class,
                () -> service.updateWeddingTask(999,
                        new UpdateWeddingTaskRequest(null, null, null, TaskStatus.DONE, null)));
    }

    @Test
    void deleteExistingTaskShouldCallRepository() {
        WeddingTask task = existingTask(10, 5, "Book venue");
        when(repository.findById(10)).thenReturn(Optional.of(task));

        service.deleteWeddingTask(10);

        verify(repository).delete(task);
    }

    @Test
    void deleteMissingTaskShouldThrowWithoutDeleting() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(WeddingTaskNotFoundException.class, () -> service.deleteWeddingTask(999));
        verify(repository, never()).delete(any(WeddingTask.class));
    }

    private static WeddingTask existingTask(int id, int weddingId, String name) {
        WeddingTask task = new WeddingTask();
        task.setId(id);
        task.setWeddingId(weddingId);
        task.setName(name);
        task.setDescription("50 guests");
        task.setPriority(TaskPriority.HIGH);
        task.setStatus(TaskStatus.NEW);
        task.setDeadline(LocalDate.of(2027, 6, 15));
        task.setCreatedAt(Instant.parse("2026-11-01T12:00:00Z"));
        task.setUpdatedAt(Instant.parse("2026-11-01T12:00:00Z"));
        return task;
    }
}
