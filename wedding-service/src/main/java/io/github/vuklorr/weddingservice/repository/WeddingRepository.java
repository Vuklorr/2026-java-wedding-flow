package io.github.vuklorr.weddingservice.repository;

import io.github.vuklorr.weddingservice.model.Wedding;
import org.springframework.data.jpa.repository.JpaRepository;




public interface WeddingRepository extends JpaRepository<Wedding, Integer> {}
