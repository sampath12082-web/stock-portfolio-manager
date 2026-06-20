package com.stocks.myportfolio.dto.response.mf;

import java.math.BigDecimal;

public record MfHoldingResponse(
        Long id,
        String schemeCode,
        String schemeName,
        String fundHouse,
        BigDecimal units,
        BigDecimal averageNav,
        BigDecimal investedAmount,
        BigDecimal currentNav,
        BigDecimal currentValue,
        BigDecimal pnl,
        BigDecimal pnlPercentage) {
}
