package io.github.vuklorr.weddingservice.controller;

import io.github.vuklorr.weddingservice.repository.WeddingRepository;
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
    public ResponseEntity<?> create(@RequestBody WeddingRepository weddingRepository) {
        weddingService.createWedding(weddingRepository);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(value = "/weddings")
    public ResponseEntity<List<WeddingRepository>> readAll() {
        final List<WeddingRepository> weddingRepositoryList = weddingService.readAllWedding();

        return weddingRepositoryList != null
                ? new ResponseEntity<>(weddingRepositoryList, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/weddings/{id}")
    public ResponseEntity<?> read(@PathVariable(name = "id") int id) {
        final WeddingRepository weddingRepository = weddingService.readWedding(id);

        return weddingRepository != null
                ? new ResponseEntity<>(weddingRepository, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
