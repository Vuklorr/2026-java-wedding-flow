package io.github.vuklorr.weddingservice.service.impl;

import io.github.vuklorr.weddingservice.repository.WeddingRepository;
import io.github.vuklorr.weddingservice.service.WeddingService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WeddingServiceImpl implements WeddingService {
    private static final Map<Integer, WeddingRepository> WEDDING_REPOSITORY_MAP = new HashMap<>();
    private static final AtomicInteger WEDDING_ID_HOLDER = new AtomicInteger();

    @Override
    public void createWedding(WeddingRepository weddingRepository) {
        final int id = WEDDING_ID_HOLDER.getAndIncrement();
        weddingRepository.setId(id);
        WEDDING_REPOSITORY_MAP.put(id, weddingRepository);
    }

    @Override
    public List<WeddingRepository> readAllWedding() {
        return new ArrayList<>(WEDDING_REPOSITORY_MAP.values());
    }

    @Override
    public WeddingRepository readWedding(int id) {
        return WEDDING_REPOSITORY_MAP.get(id);
    }
}
