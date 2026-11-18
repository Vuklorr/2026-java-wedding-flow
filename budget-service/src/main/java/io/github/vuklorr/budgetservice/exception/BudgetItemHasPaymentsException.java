package io.github.vuklorr.budgetservice.exception;

public class BudgetItemHasPaymentsException extends RuntimeException {
    public BudgetItemHasPaymentsException(int budgetId) {
        super("Budget item " + budgetId + " has payments");
    }
}
