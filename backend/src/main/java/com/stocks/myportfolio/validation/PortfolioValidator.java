package com.stocks.myportfolio.validation;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.stocks.myportfolio.common.exception.ResourceNotFoundException;
import com.stocks.myportfolio.common.exception.ValidationException;
import com.stocks.myportfolio.entity.Stock;

@Component
public class PortfolioValidator {

    public void validateSufficientQuantity(int available, int requested) {
        if (requested > available) {
            throw new ValidationException(
                    "Insufficient quantity: available=" + available
                            + ", requested=" + requested);
        }
    }

    public void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be positive");
        }
    }

    public Stock validateStockExists(Optional<Stock> stock, String symbol) {
        return stock.orElseThrow(() ->
                new ResourceNotFoundException("Stock not found: " + symbol));
    }
}
