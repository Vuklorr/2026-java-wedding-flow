package io.github.vuklorr.planningservice.service;

import io.github.vuklorr.planningservice.dto.CreateWeddingTaskRequest;
import io.github.vuklorr.planningservice.dto.WeddingTaskResponse;

public interface WeddingTaskService {
    WeddingTaskResponse createWeddingTask(CreateWeddingTaskRequest createWeddingTaskRequest);
    WeddingTaskResponse readWeddingTask(int id);


}
