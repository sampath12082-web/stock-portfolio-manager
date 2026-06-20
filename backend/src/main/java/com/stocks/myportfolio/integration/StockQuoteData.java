package com.stocks.myportfolio.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockQuoteData(
        String symbol,
        BigDecimal ltp,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal previousClose,
        Long volume,
        LocalDateTime fetchedAt) {
}
