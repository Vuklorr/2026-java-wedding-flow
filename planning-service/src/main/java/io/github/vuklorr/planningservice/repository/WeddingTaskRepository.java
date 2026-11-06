package io.github.vuklorr.planningservice.repository;

import io.github.vuklorr.planningservice.model.WeddingTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeddingTaskRepository extends JpaRepository<WeddingTask, Integer> {
}
