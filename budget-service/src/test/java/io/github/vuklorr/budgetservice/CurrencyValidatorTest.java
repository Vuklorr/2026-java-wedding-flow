package io.github.vuklorr.budgetservice;

import io.github.vuklorr.budgetservice.exception.InvalidCurrencyCodeException;
import io.github.vuklorr.budgetservice.validation.CurrencyValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyValidatorTest {
    private final CurrencyValidator validator = new CurrencyValidator();

    @Test
    void standardCurrencyCodesShouldBeAccepted() {
        assertDoesNotThrow(() -> validator.validateCurrencyCode("RUB"));
        assertDoesNotThrow(() -> validator.validateCurrencyCode("EUR"));
        assertDoesNotThrow(() -> validator.validateCurrencyCode("USD"));
    }

    @Test
    void invalidCurrencyCodeShouldFail() {
        assertThrows(InvalidCurrencyCodeException.class, () -> validator.validateCurrencyCode("ZZZ"));
    }

    @Test
    void missingCurrencyShouldFail() {
        assertThrows(InvalidCurrencyCodeException.class, () -> validator.validateCurrencyCode(null));
    }

    @Test
    void blankCurrencyShouldFail() {
        assertThrows(InvalidCurrencyCodeException.class, () -> validator.validateCurrencyCode("  "));
    }
}
