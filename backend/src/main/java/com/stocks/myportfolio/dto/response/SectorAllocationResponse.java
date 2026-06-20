package com.stocks.myportfolio.dto.response;

import java.math.BigDecimal;

public record SectorAllocationResponse(
        String sector,
        BigDecimal investedAmount,
        BigDecimal currentValue,
        BigDecimal percentage,
        int holdingCount) {
}
