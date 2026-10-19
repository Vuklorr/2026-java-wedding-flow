package io.github.vuklorr.weddingservice.service.impl;

import io.github.vuklorr.weddingservice.dto.CreateWeddingRequest;
import io.github.vuklorr.weddingservice.dto.WeddingResponse;
import io.github.vuklorr.weddingservice.model.Wedding;
import io.github.vuklorr.weddingservice.repository.WeddingRepository;
import io.github.vuklorr.weddingservice.service.WeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WeddingServiceImpl implements WeddingService {
    private final WeddingRepository weddingRepository;
    private static final AtomicInteger WEDDING_ID_HOLDER = new AtomicInteger();

    @Autowired
    public WeddingServiceImpl(WeddingRepository weddingRepository) {
        this.weddingRepository = weddingRepository;
    }

    @Override
    public void createWedding(CreateWeddingRequest createWeddingRequest) {
        weddingRepository.createWeddingModel(new Wedding(
                WEDDING_ID_HOLDER.getAndIncrement(),
                createWeddingRequest.title(),
                createWeddingRequest.weddingDate(),
                LocalDateTime.now()
        ));
    }

    @Override
    public List<WeddingResponse> readAllWedding() {
        List<Wedding> weddingList = weddingRepository.readAllWedding();
        List<WeddingResponse> weddingResponseList = new ArrayList<>();

        for (Wedding wedding : weddingList) {
            WeddingResponse weddingResponse = getWeddingResponseByWedding(wedding);
            if (weddingResponse != null) {
                weddingResponseList.add(weddingResponse);
            }
        }
        return weddingResponseList;
    }

    @Override
    public WeddingResponse readWedding(int id) {
        Wedding wedding = weddingRepository.readWedding(id);
        return getWeddingResponseByWedding(wedding);
    }

    private WeddingResponse getWeddingResponseByWedding(Wedding wedding) {
        return wedding != null
                ? new WeddingResponse(
                wedding.getId(),
                wedding.getTitle(),
                wedding.getWeddingDate(),
                wedding.getCreatedAt())
                : null;
    }
}
