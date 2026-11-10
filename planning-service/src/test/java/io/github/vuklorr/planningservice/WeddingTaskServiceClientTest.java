package io.github.vuklorr.planningservice;

import io.github.vuklorr.planningservice.client.WeddingClient;
import io.github.vuklorr.planningservice.dto.CreateWeddingTaskRequest;
import io.github.vuklorr.planningservice.dto.WeddingTaskResponse;
import io.github.vuklorr.planningservice.exception.WeddingNotFoundException;
import io.github.vuklorr.planningservice.exception.WeddingServiceUnavailableException;
import io.github.vuklorr.planningservice.model.WeddingTask;
import io.github.vuklorr.planningservice.repository.WeddingTaskRepository;
import io.github.vuklorr.planningservice.service.impl.WeddingTaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WeddingTaskServiceClientTest {

    private WeddingTaskRepository repository;
    private WeddingClient weddingClient;
    private WeddingTaskServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(WeddingTaskRepository.class);
        weddingClient = mock(WeddingClient.class);
        service = new WeddingTaskServiceImpl(repository, weddingClient);
    }

    @Test
    void existingWeddingShouldBeCheckedBeforeTaskIsSaved() {
        when(repository.save(any(WeddingTask.class))).thenAnswer(invocation -> {
            WeddingTask task = invocation.getArgument(0);
            task.setId(12);
            return task;
        });

        WeddingTaskResponse response = service.createWeddingTask(request(), 5);

        assertEquals(12, response.id());
        assertEquals(5, response.weddingId());

        InOrder order = inOrder(weddingClient, repository);
        order.verify(weddingClient).ensureWeddingExists(5);
        order.verify(repository).save(any(WeddingTask.class));
    }

    @Test
    void missingWeddingShouldNotSaveTask() {
        doThrow(new WeddingNotFoundException(999))
                .when(weddingClient).ensureWeddingExists(999);

        assertThrows(WeddingNotFoundException.class,
                () -> service.createWeddingTask(request(), 999));

        verify(weddingClient).ensureWeddingExists(999);
        verifyNoInteractions(repository);
    }

    @Test
    void unavailableWeddingServiceShouldNotSaveTask() {
        doThrow(new WeddingServiceUnavailableException())
                .when(weddingClient).ensureWeddingExists(5);

        assertThrows(WeddingServiceUnavailableException.class,
                () -> service.createWeddingTask(request(), 5));

        verify(weddingClient).ensureWeddingExists(5);
        verifyNoInteractions(repository);
    }

    private static CreateWeddingTaskRequest request() {
        return new CreateWeddingTaskRequest("Choose music", null, null, null);
    }
}
