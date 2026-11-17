package io.github.vuklorr.budgetservice.exception;

public class BudgetItemNotFoundException extends RuntimeException {
    public BudgetItemNotFoundException(int id) {
        super("Budget item " + id + " not found");
    }
}
