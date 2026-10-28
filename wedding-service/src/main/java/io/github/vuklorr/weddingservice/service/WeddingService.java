package io.github.vuklorr.weddingservice.service;

import io.github.vuklorr.weddingservice.dto.CreateWeddingRequest;
import io.github.vuklorr.weddingservice.dto.UpdateWeddingDateRequest;
import io.github.vuklorr.weddingservice.dto.WeddingResponse;

import java.util.List;

public interface WeddingService {
    WeddingResponse createWedding(CreateWeddingRequest createWeddingRequest);
    List<WeddingResponse> readAllWedding();
    WeddingResponse readWedding(int id);
    WeddingResponse updateWeddingDate(int id, UpdateWeddingDateRequest updateWeddingDateRequest);
    void deleteWedding(int id);
}
