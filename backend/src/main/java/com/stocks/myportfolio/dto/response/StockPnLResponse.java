package com.stocks.myportfolio.dto.response;

import java.math.BigDecimal;

public record StockPnLResponse(
        String symbol,
        String companyName,
        Integer quantity,
        BigDecimal averageBuyPrice,
        BigDecimal currentPrice,
        BigDecimal investedAmount,
        BigDecimal currentValue,
        BigDecimal pnl,
        BigDecimal pnlPercentage) {
}
