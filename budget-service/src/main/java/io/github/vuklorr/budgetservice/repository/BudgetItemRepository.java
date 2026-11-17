package io.github.vuklorr.budgetservice.repository;

import io.github.vuklorr.budgetservice.model.BudgetItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetItemRepository extends JpaRepository<BudgetItem, Integer> {
    List<BudgetItem> findByWeddingId(int weddingId);
}
