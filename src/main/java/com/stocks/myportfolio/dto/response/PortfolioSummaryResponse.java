package com.stocks.myportfolio.dto.response;

import java.math.BigDecimal;

public record PortfolioSummaryResponse(
        long totalHoldings,
        BigDecimal totalInvestment,
        BigDecimal currentValue,
        BigDecimal totalPnL,
        BigDecimal totalPnLPercentage,
        BigDecimal dayPnL,
        BigDecimal dayPnLPercentage) {
}
