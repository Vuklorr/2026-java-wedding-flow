package io.github.vuklorr.weddingservice.repository;

import io.github.vuklorr.weddingservice.model.Wedding;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class WeddingRepository {
    private final ConcurrentHashMap<Integer, Wedding> WEDDING_REPOSITORY_MAP = new ConcurrentHashMap<>();

    public void createWeddingModel(Wedding wedding) {
        WEDDING_REPOSITORY_MAP.put(wedding.getId(), wedding);
    }

    public List<Wedding> readAllWedding() {
        return new ArrayList<>(WEDDING_REPOSITORY_MAP.values());
    }

    public Wedding readWedding(int id) {
        return WEDDING_REPOSITORY_MAP.get(id);
    }


}
