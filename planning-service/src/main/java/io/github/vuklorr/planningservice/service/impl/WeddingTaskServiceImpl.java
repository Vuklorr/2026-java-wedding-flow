package io.github.vuklorr.planningservice.service.impl;

import io.github.vuklorr.planningservice.dto.CreateWeddingTaskRequest;
import io.github.vuklorr.planningservice.dto.WeddingTaskResponse;
import io.github.vuklorr.planningservice.exception.WeddingTaskNotFoundException;
import io.github.vuklorr.planningservice.model.WeddingTask;
import io.github.vuklorr.planningservice.repository.WeddingTaskRepository;
import io.github.vuklorr.planningservice.service.WeddingTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WeddingTaskServiceImpl implements WeddingTaskService {

    private final WeddingTaskRepository weddingTaskRepository;

    public WeddingTaskServiceImpl(WeddingTaskRepository weddingTaskRepository) {
        this.weddingTaskRepository = weddingTaskRepository;
    }

    @Override
    public WeddingTaskResponse createWeddingTask(CreateWeddingTaskRequest createWeddingTaskRequest, int idWedding) {
        WeddingTask weddingTask = new WeddingTask();
        weddingTask.setWeddingId(idWedding);
        weddingTask.setName(createWeddingTaskRequest.name());
        weddingTask.setDescription(createWeddingTaskRequest.description());
        weddingTask.setPriority(createWeddingTaskRequest.priority());
        weddingTask.setStatus(createWeddingTaskRequest.status());
        weddingTask.setDeadline(createWeddingTaskRequest.deadline());

        return getWeddingTaskResponse(weddingTaskRepository.save(weddingTask));
    }

    @Override
    public List<WeddingTaskResponse> readAllWeddingTasksByWeddingId(int weddingId) {
        return weddingTaskRepository
                .findAll()
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
    public WeddingTaskResponse updateWeddingTask(int id, CreateWeddingTaskRequest createWeddingTaskRequest) {
        WeddingTask weddingTask = weddingTaskRepository.findById(id).orElseThrow(() -> new WeddingTaskNotFoundException(id));
        weddingTask.setName(createWeddingTaskRequest.name());
        weddingTask.setDescription(createWeddingTaskRequest.description());
        weddingTask.setPriority(createWeddingTaskRequest.priority());
        weddingTask.setStatus(createWeddingTaskRequest.status());
        weddingTask.setDeadline(createWeddingTaskRequest.deadline());

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
