package com.stocks.myportfolio.integration.yahoo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HistoricalQuote(
        LocalDate date,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        Long volume) {
}
