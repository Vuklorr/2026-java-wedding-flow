package io.github.vuklorr.planningservice.service.impl;

import io.github.vuklorr.planningservice.dto.CreateWeddingTaskRequest;
import io.github.vuklorr.planningservice.dto.WeddingTaskResponse;
import io.github.vuklorr.planningservice.exception.WeddingTaskNotFoundException;
import io.github.vuklorr.planningservice.model.WeddingTask;
import io.github.vuklorr.planningservice.repository.WeddingTaskRepository;
import io.github.vuklorr.planningservice.service.WeddingTaskService;
import org.springframework.stereotype.Service;

@Service
public class WeddingTaskServiceImpl implements WeddingTaskService {

    private final WeddingTaskRepository weddingTaskRepository;

    public WeddingTaskServiceImpl(WeddingTaskRepository weddingTaskRepository) {
        this.weddingTaskRepository = weddingTaskRepository;
    }

    @Override
    public WeddingTaskResponse createWeddingTask(CreateWeddingTaskRequest createWeddingTaskRequest) {
        WeddingTask weddingTask = new WeddingTask();
        weddingTask.setWeddingId(createWeddingTaskRequest.weddingID());
        weddingTask.setName(createWeddingTaskRequest.name());

        return getWeddingTaskResponse(weddingTaskRepository.save(weddingTask));
    }

    @Override
    public WeddingTaskResponse readWeddingTask(int id) {
        WeddingTask weddingTask = weddingTaskRepository.findById(id).orElseThrow(() -> new WeddingTaskNotFoundException(id));
        return getWeddingTaskResponse(weddingTask);
    }

    private WeddingTaskResponse getWeddingTaskResponse(WeddingTask weddingTask) {
        return new WeddingTaskResponse(
                weddingTask.getId(),
                weddingTask.getWeddingId(),
                weddingTask.getName(),
                weddingTask.getDescription(),
                weddingTask.getPriority(),
                weddingTask.getStatus(),
                weddingTask.getDeadline(),
                weddingTask.getCreatedAt(),
                weddingTask.getUpdatedAt()

        );
    }
}
