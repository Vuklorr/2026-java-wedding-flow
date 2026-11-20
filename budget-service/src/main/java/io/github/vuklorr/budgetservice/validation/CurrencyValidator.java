package io.github.vuklorr.budgetservice.validation;

import io.github.vuklorr.budgetservice.exception.InvalidCurrencyCodeException;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class CurrencyValidator {

    public void validateCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new InvalidCurrencyCodeException(currencyCode);
        }

        try {
            Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            throw new InvalidCurrencyCodeException(currencyCode);
        }
    }
}
