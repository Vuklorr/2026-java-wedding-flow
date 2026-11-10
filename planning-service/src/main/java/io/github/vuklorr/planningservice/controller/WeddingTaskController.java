package io.github.vuklorr.planningservice.controller;

import io.github.vuklorr.planningservice.dto.CreateWeddingTaskRequest;
import io.github.vuklorr.planningservice.dto.UpdateWeddingTaskRequest;
import io.github.vuklorr.planningservice.dto.WeddingTaskResponse;
import io.github.vuklorr.planningservice.service.WeddingTaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WeddingTaskController {
    private final WeddingTaskService weddingTaskService;

    public WeddingTaskController(WeddingTaskService weddingTaskService) {
        this.weddingTaskService = weddingTaskService;
    }

    @PostMapping(value = "/weddings/{weddingId}/tasks")
    public ResponseEntity<?> create(
            @PathVariable(name = "weddingId") int weddingId,
            @Valid @RequestBody CreateWeddingTaskRequest createWeddingTaskRequest
    ) {
        final WeddingTaskResponse weddingTaskResponse = weddingTaskService.createWeddingTask(createWeddingTaskRequest, weddingId);
        return new ResponseEntity<>(weddingTaskResponse, HttpStatus.CREATED);
    }

    @GetMapping(value = "/weddings/{weddingId}/tasks")
    public ResponseEntity<List<WeddingTaskResponse>> readAll(@PathVariable(name = "weddingId") int weddingId) {
        final List<WeddingTaskResponse> weddingResponseList = weddingTaskService.readAllWeddingTasksByWeddingId(weddingId);

        return new ResponseEntity<>(weddingResponseList, HttpStatus.OK);
    }

    @GetMapping(value = "/tasks/{id}")
    public ResponseEntity<?> read(@PathVariable(name = "id") int id) {
        final WeddingTaskResponse weddingTaskResponse = weddingTaskService.readWeddingTask(id);

        return new ResponseEntity<>(weddingTaskResponse, HttpStatus.OK);
    }

    @PatchMapping(value = "/tasks/{id}")
    public ResponseEntity<?> updateWeddingTask(
            @PathVariable(name = "id") int id,
            @Valid @RequestBody UpdateWeddingTaskRequest updateWeddingTaskRequest) {
        final WeddingTaskResponse weddingTaskResponse = weddingTaskService.updateWeddingTask(id, updateWeddingTaskRequest);

        return new ResponseEntity<>(weddingTaskResponse, HttpStatus.OK);
    }

    @DeleteMapping(value = "/tasks/{id}")
    public ResponseEntity<?> deleteWedding(@PathVariable(name = "id") int id) {
        weddingTaskService.deleteWeddingTask(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
