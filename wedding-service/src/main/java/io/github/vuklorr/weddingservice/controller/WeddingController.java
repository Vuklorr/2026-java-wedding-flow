package io.github.vuklorr.weddingservice.controller;

import io.github.vuklorr.weddingservice.dto.CreateWeddingRequest;
import io.github.vuklorr.weddingservice.dto.WeddingResponse;
import io.github.vuklorr.weddingservice.service.WeddingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
public class WeddingController {
    private final WeddingService weddingService;

    public WeddingController(WeddingService weddingService) {
        this.weddingService = weddingService;
    }

    @PostMapping(value = "/weddings")
    public ResponseEntity<?> create(@Valid @RequestBody CreateWeddingRequest createWeddingRequest) {
        final WeddingResponse weddingResponse = weddingService.createWedding(createWeddingRequest);

        return weddingResponse != null
                ? new ResponseEntity<>(weddingResponse, HttpStatus.CREATED)
                : new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/weddings")
    public ResponseEntity<List<WeddingResponse>> readAll() {
        final List<WeddingResponse> weddingResponseList = weddingService.readAllWedding();

        return new ResponseEntity<>(weddingResponseList, HttpStatus.OK);
    }

    @GetMapping(value = "/weddings/{id}")
    public ResponseEntity<?> read(@PathVariable(name = "id") int id) {
        final WeddingResponse weddingResponse = weddingService.readWedding(id);

        return weddingResponse != null
                ? new ResponseEntity<>(weddingResponse, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
