package com.stocks.myportfolio.dto.response;

import java.math.BigDecimal;

public record HoldingResponse(
        Long id,
        String symbol,
        Integer quantity,
        BigDecimal averageBuyPrice,
        BigDecimal investedAmount,
        BigDecimal currentPrice,
        BigDecimal currentValue,
        BigDecimal pnl,
        BigDecimal pnlPercentage,
        BigDecimal dayChange,
        BigDecimal dayChangePercentage) {
}
