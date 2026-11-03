package io.github.vuklorr.planningservice.service.impl;

import io.github.vuklorr.planningservice.repository.WeddingTaskRepository;
import io.github.vuklorr.planningservice.service.WeddingTaskService;
import org.springframework.stereotype.Service;

@Service
public class WeddingTaskServiceImpl implements WeddingTaskService {

    private final WeddingTaskRepository weddingTaskRepository;

    public WeddingTaskServiceImpl(WeddingTaskRepository weddingTaskRepository) {
        this.weddingTaskRepository = weddingTaskRepository;
    }
}
