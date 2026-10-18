package io.github.vuklorr.weddingservice.service;

import io.github.vuklorr.weddingservice.repository.WeddingRepository;

import java.util.List;

public interface WeddingService {
    void createWedding(WeddingRepository weddingRepository);
    List<WeddingRepository> readAllWedding();
    WeddingRepository readWedding(int id);
}
