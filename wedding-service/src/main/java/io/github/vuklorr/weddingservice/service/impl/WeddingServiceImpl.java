package io.github.vuklorr.weddingservice.service.impl;

import io.github.vuklorr.weddingservice.dto.CreateWeddingRequest;
import io.github.vuklorr.weddingservice.dto.UpdateWeddingDateRequest;
import io.github.vuklorr.weddingservice.dto.WeddingResponse;
import io.github.vuklorr.weddingservice.exception.WeddingNotFoundException;
import io.github.vuklorr.weddingservice.model.Wedding;
import io.github.vuklorr.weddingservice.repository.WeddingRepository;
import io.github.vuklorr.weddingservice.service.WeddingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeddingServiceImpl implements WeddingService {
    private final WeddingRepository weddingRepository;

    public WeddingServiceImpl(WeddingRepository weddingRepository) {
        this.weddingRepository = weddingRepository;
    }

    @Override
    public WeddingResponse createWedding(CreateWeddingRequest createWeddingRequest) {
        Wedding wedding = new Wedding();
        wedding.setTitle(createWeddingRequest.title());
        wedding.setWeddingDate(createWeddingRequest.weddingDate());
        wedding.setCreatedAt(Instant.now());

        wedding = weddingRepository.save(wedding);
        return new WeddingResponse(
                wedding.getId(),
                wedding.getTitle(),
                wedding.getWeddingDate(),
                wedding.getCreatedAt()
        );
    }

    @Override
    public List<WeddingResponse> readAllWedding() {
        List<Wedding> weddingList = weddingRepository.findAll();
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
        Wedding wedding = weddingRepository.findById(id).orElseThrow(() -> new WeddingNotFoundException(id));
        return getWeddingResponseByWedding(wedding);
    }

    @Override
    @Transactional
    public WeddingResponse updateWeddingDate(int id, UpdateWeddingDateRequest updateWeddingDateRequest) {
        Wedding wedding = weddingRepository.findById(id).orElse(null);

        if (wedding == null) {
            return null;
        }

        wedding.setWeddingDate(updateWeddingDateRequest.weddingDate());
        return getWeddingResponseByWedding(wedding);
    }

    @Override
    @Transactional
    public boolean deleteWedding(int id) {
        Wedding wedding = weddingRepository.findById(id).orElse(null);
        if (wedding == null) {
            return false;
        }
        weddingRepository.delete(wedding);
        return true;
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
