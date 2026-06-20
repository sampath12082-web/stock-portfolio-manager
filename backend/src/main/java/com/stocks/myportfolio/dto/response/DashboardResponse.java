package com.stocks.myportfolio.dto.response;
import java.math.BigDecimal;

public record DashboardResponse(
        BigDecimal investedAmount,
        BigDecimal currentValue,
        BigDecimal unrealizedPnL,
        BigDecimal unrealizedPnLPercentage,
        BigDecimal totalDeposited
) {
}
