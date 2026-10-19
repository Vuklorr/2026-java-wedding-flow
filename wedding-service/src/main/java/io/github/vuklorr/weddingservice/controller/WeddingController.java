package io.github.vuklorr.weddingservice.controller;

import io.github.vuklorr.weddingservice.dto.CreateWeddingRequest;
import io.github.vuklorr.weddingservice.dto.WeddingResponse;
import io.github.vuklorr.weddingservice.service.WeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WeddingController {
    private final WeddingService weddingService;

    @Autowired
    public WeddingController(WeddingService weddingService) {
        this.weddingService = weddingService;
    }

    @PostMapping(value = "/weddings")
    public ResponseEntity<?> create(@RequestBody CreateWeddingRequest createWeddingRequest) {
        weddingService.createWedding(createWeddingRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(value = "/weddings")
    public ResponseEntity<List<WeddingResponse>> readAll() {
        final List<WeddingResponse> weddingResponceList = weddingService.readAllWedding();

        return weddingResponceList != null
                ? new ResponseEntity<>(weddingResponceList, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/weddings/{id}")
    public ResponseEntity<?> read(@PathVariable(name = "id") int id) {
        final WeddingResponse weddingResponse = weddingService.readWedding(id);

        return weddingResponse != null
                ? new ResponseEntity<>(weddingResponse, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
