package io.github.vuklorr.budgetservice.repository;

import io.github.vuklorr.budgetservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByBudgetItemId(int budgetItemId);
}
