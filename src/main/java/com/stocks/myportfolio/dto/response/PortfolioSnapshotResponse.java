package com.stocks.myportfolio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PortfolioSnapshotResponse(
        Long id,
        LocalDate snapshotDate,
        BigDecimal totalInvestment,
        BigDecimal currentValue,
        BigDecimal totalPnL,
        BigDecimal totalPnLPercentage,
        Integer holdingCount,
        String topGainer,
        String topLoser) {
}
