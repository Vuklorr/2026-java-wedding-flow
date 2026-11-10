package io.github.vuklorr.planningservice;

import io.github.vuklorr.planningservice.controller.WeddingTaskController;
import io.github.vuklorr.planningservice.dto.CreateWeddingTaskRequest;
import io.github.vuklorr.planningservice.dto.UpdateWeddingTaskRequest;
import io.github.vuklorr.planningservice.dto.WeddingTaskResponse;
import io.github.vuklorr.planningservice.entity.TaskPriority;
import io.github.vuklorr.planningservice.entity.TaskStatus;
import io.github.vuklorr.planningservice.exception.WeddingTaskNotFoundException;
import io.github.vuklorr.planningservice.service.WeddingTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeddingTaskController.class)
class WeddingTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeddingTaskService service;

    @Test
    void createShouldReturn201AndTask() throws Exception {
        CreateWeddingTaskRequest request = new CreateWeddingTaskRequest(
                "Book restaurant", "50 guests", TaskPriority.HIGH, LocalDate.of(2027, 7, 15));
        when(service.createWeddingTask(request, 5)).thenReturn(response(12, 5, "Book restaurant"));

        mockMvc.perform(post("/weddings/{weddingId}/tasks", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Book restaurant","description":"50 guests",
                                 "priority":"HIGH","deadline":"2027-07-15"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.weddingId").value(5))
                .andExpect(jsonPath("$.name").value("Book restaurant"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("NEW"));

        verify(service).createWeddingTask(request, 5);
    }

    @Test
    void createShouldAcceptNameOnly() throws Exception {
        CreateWeddingTaskRequest request = new CreateWeddingTaskRequest("Choose music", null, null, null);
        when(service.createWeddingTask(request, 5)).thenReturn(
                new WeddingTaskResponse(13, 5, "Choose music", null, TaskPriority.MEDIUM,
                        TaskStatus.NEW, null, TIME, TIME));

        mockMvc.perform(post("/weddings/{weddingId}/tasks", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Choose music"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void createWithBlankNameShouldReturn400() throws Exception {
        mockMvc.perform(post("/weddings/{weddingId}/tasks", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"   "}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void createWithoutNameShouldReturn400() throws Exception {
        mockMvc.perform(post("/weddings/{weddingId}/tasks", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"priority":"HIGH"}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void createWithUnknownPriorityShouldReturn400() throws Exception {
        mockMvc.perform(post("/weddings/{weddingId}/tasks", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Task","priority":"URGENT"}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void listShouldReturnOnlyServiceResultsForRequestedWedding() throws Exception {
        when(service.readAllWeddingTasksByWeddingId(5)).thenReturn(
                List.of(response(12, 5, "Book restaurant"), response(13, 5, "Choose music")));

        mockMvc.perform(get("/weddings/{weddingId}/tasks", 5))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].weddingId").value(5))
                .andExpect(jsonPath("$[1].name").value("Choose music"));
        verify(service).readAllWeddingTasksByWeddingId(5);
    }

    @Test
    void listWithoutTasksShouldReturnEmptyArray() throws Exception {
        when(service.readAllWeddingTasksByWeddingId(99)).thenReturn(List.of());
        mockMvc.perform(get("/weddings/{weddingId}/tasks", 99))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void readExistingTaskShouldReturn200() throws Exception {
        when(service.readWeddingTask(12)).thenReturn(response(12, 5, "Book restaurant"));
        mockMvc.perform(get("/tasks/{id}", 12))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.name").value("Book restaurant"));
    }

    @Test
    void readUnknownTaskShouldReturn404() throws Exception {
        when(service.readWeddingTask(999)).thenThrow(new WeddingTaskNotFoundException(999));
        mockMvc.perform(get("/tasks/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void patchStatusOnlyShouldReturn200() throws Exception {
        UpdateWeddingTaskRequest request = new UpdateWeddingTaskRequest(
                null, null, null, TaskStatus.DONE, null);
        WeddingTaskResponse updated = new WeddingTaskResponse(
                12, 5, "Book restaurant", "50 guests", TaskPriority.HIGH,
                TaskStatus.DONE, LocalDate.of(2027, 7, 15), TIME, TIME);
        when(service.updateWeddingTask(12, request)).thenReturn(updated);

        mockMvc.perform(patch("/tasks/{id}", 12)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"DONE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Book restaurant"))
                .andExpect(jsonPath("$.status").value("DONE"));
        verify(service).updateWeddingTask(12, request);
    }

    @Test
    void patchTwoFieldsShouldReturn200() throws Exception {
        UpdateWeddingTaskRequest request = new UpdateWeddingTaskRequest(
                "New name", null, TaskPriority.LOW, null, null);
        WeddingTaskResponse updated = new WeddingTaskResponse(
                12, 5, "New name", "50 guests", TaskPriority.LOW,
                TaskStatus.NEW, LocalDate.of(2027, 7, 15), TIME, TIME);
        when(service.updateWeddingTask(12, request)).thenReturn(updated);

        mockMvc.perform(patch("/tasks/{id}", 12)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New name","priority":"LOW"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"))
                .andExpect(jsonPath("$.priority").value("LOW"));
    }

    @Test
    void patchAllFieldsShouldReturn200() throws Exception {
        UpdateWeddingTaskRequest request = new UpdateWeddingTaskRequest(
                "New name", "New description", TaskPriority.LOW,
                TaskStatus.IN_PROGRESS, LocalDate.of(2027, 9, 1));
        WeddingTaskResponse updated = new WeddingTaskResponse(
                12, 5, "New name", "New description", TaskPriority.LOW,
                TaskStatus.IN_PROGRESS, LocalDate.of(2027, 9, 1), TIME, TIME);
        when(service.updateWeddingTask(12, request)).thenReturn(updated);

        mockMvc.perform(patch("/tasks/{id}", 12)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New name","description":"New description",
                                 "priority":"LOW","status":"IN_PROGRESS","deadline":"2027-09-01"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.deadline").value("2027-09-01"));
    }

    @Test
    void patchUnknownTaskShouldReturn404() throws Exception {
        UpdateWeddingTaskRequest request = new UpdateWeddingTaskRequest(
                null, null, null, TaskStatus.DONE, null);
        when(service.updateWeddingTask(999, request)).thenThrow(new WeddingTaskNotFoundException(999));

        mockMvc.perform(patch("/tasks/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"DONE"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchWithUnknownStatusShouldReturn400() throws Exception {
        mockMvc.perform(patch("/tasks/{id}", 12)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"INVALID"}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void deleteExistingTaskShouldReturn204() throws Exception {
        mockMvc.perform(delete("/tasks/{id}", 12))
                .andExpect(status().isNoContent());
        verify(service).deleteWeddingTask(12);
    }

    @Test
    void deleteUnknownTaskShouldReturn404() throws Exception {
        doThrow(new WeddingTaskNotFoundException(999)).when(service).deleteWeddingTask(999);
        mockMvc.perform(delete("/tasks/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchWithBlankNameShouldReturn400() throws Exception {
        mockMvc.perform(patch("/tasks/{id}", 12)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "   "}
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }


    private static final Instant TIME = Instant.parse("2026-11-01T12:00:00Z");

    private static WeddingTaskResponse response(int id, int weddingId, String name) {
        return new WeddingTaskResponse(
                id, weddingId, name, "50 guests", TaskPriority.HIGH, TaskStatus.NEW,
                LocalDate.of(2027, 7, 15), TIME, TIME);
    }
}
