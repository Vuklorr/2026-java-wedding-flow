package io.github.vuklorr.planningservice.service.impl;

import io.github.vuklorr.planningservice.client.WeddingClient;
import io.github.vuklorr.planningservice.dto.CreateWeddingTaskRequest;
import io.github.vuklorr.planningservice.dto.UpdateWeddingTaskRequest;
import io.github.vuklorr.planningservice.dto.WeddingTaskResponse;
import io.github.vuklorr.planningservice.entity.TaskPriority;
import io.github.vuklorr.planningservice.entity.TaskStatus;
import io.github.vuklorr.planningservice.exception.WeddingTaskNotFoundException;
import io.github.vuklorr.planningservice.model.WeddingTask;
import io.github.vuklorr.planningservice.repository.WeddingTaskRepository;
import io.github.vuklorr.planningservice.service.WeddingTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class WeddingTaskServiceImpl implements WeddingTaskService {

    private final WeddingTaskRepository weddingTaskRepository;
    private final WeddingClient weddingClient;

    public WeddingTaskServiceImpl(WeddingTaskRepository weddingTaskRepository, WeddingClient weddingClient) {
        this.weddingTaskRepository = weddingTaskRepository;
        this.weddingClient = weddingClient;
    }

    @Override
    public WeddingTaskResponse createWeddingTask(CreateWeddingTaskRequest createWeddingTaskRequest, int idWedding) {
        weddingClient.ensureWeddingExists(idWedding);

        WeddingTask weddingTask = new WeddingTask();
        weddingTask.setWeddingId(idWedding);
        weddingTask.setName(createWeddingTaskRequest.name());
        weddingTask.setDescription(createWeddingTaskRequest.description());
        weddingTask.setPriority(Objects.requireNonNullElse(createWeddingTaskRequest.priority(), TaskPriority.MEDIUM));
        weddingTask.setStatus(TaskStatus.NEW);
        weddingTask.setDeadline(createWeddingTaskRequest.deadline());

        return getWeddingTaskResponse(weddingTaskRepository.save(weddingTask));
    }

    @Override
    public List<WeddingTaskResponse> readAllWeddingTasksByWeddingId(int weddingId) {
        return weddingTaskRepository
                .findByWeddingId(weddingId)
                .stream()
                .map(this::getWeddingTaskResponse)
                .toList();
    }

    @Override
    public WeddingTaskResponse readWeddingTask(int id) {
        WeddingTask weddingTask = weddingTaskRepository.findById(id).orElseThrow(() -> new WeddingTaskNotFoundException(id));
        return getWeddingTaskResponse(weddingTask);
    }

    @Override
    @Transactional
    public WeddingTaskResponse updateWeddingTask(int id, UpdateWeddingTaskRequest updateWeddingTaskRequest) {
        WeddingTask weddingTask = weddingTaskRepository.findById(id).orElseThrow(() -> new WeddingTaskNotFoundException(id));

        if (!Objects.isNull(updateWeddingTaskRequest.name())) {
            weddingTask.setName(updateWeddingTaskRequest.name());
        }

        if (!Objects.isNull(updateWeddingTaskRequest.description())) {
            weddingTask.setDescription(updateWeddingTaskRequest.description());
        }

        if (!Objects.isNull(updateWeddingTaskRequest.priority())) {
            weddingTask.setPriority(updateWeddingTaskRequest.priority());
        }

        if (!Objects.isNull(updateWeddingTaskRequest.status())) {
            weddingTask.setStatus(updateWeddingTaskRequest.status());
        }

        if (!Objects.isNull(updateWeddingTaskRequest.deadline())) {
            weddingTask.setDeadline(updateWeddingTaskRequest.deadline());
        }

        weddingTaskRepository.flush();
        return getWeddingTaskResponse(weddingTask);
    }

    @Override
    @Transactional
    public void deleteWeddingTask(int id) {
        WeddingTask weddingTask = weddingTaskRepository.findById(id).orElseThrow(() -> new WeddingTaskNotFoundException(id));
        weddingTaskRepository.delete(weddingTask);
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
