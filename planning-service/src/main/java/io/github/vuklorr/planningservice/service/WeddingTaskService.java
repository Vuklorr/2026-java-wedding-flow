package io.github.vuklorr.planningservice.service;

import io.github.vuklorr.planningservice.dto.CreateWeddingTaskRequest;
import io.github.vuklorr.planningservice.dto.UpdateWeddingTaskRequest;
import io.github.vuklorr.planningservice.dto.WeddingTaskResponse;

import java.util.List;

public interface WeddingTaskService {
    WeddingTaskResponse createWeddingTask(CreateWeddingTaskRequest createWeddingTaskRequest, int idWedding);
    List<WeddingTaskResponse> readAllWeddingTasksByWeddingId(int weddingId);
    WeddingTaskResponse readWeddingTask(int id);
    WeddingTaskResponse updateWeddingTask(int id, UpdateWeddingTaskRequest updateWeddingTaskRequest);
    void deleteWeddingTask(int id);

}
