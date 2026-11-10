package io.github.vuklorr.planningservice.repository;

import io.github.vuklorr.planningservice.model.WeddingTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeddingTaskRepository extends JpaRepository<WeddingTask, Integer> {
    List<WeddingTask> findByWeddingId(int weddingId);
}
